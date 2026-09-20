package com.swbr.orespawn.block.tree;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/**
 * The shared half of OreSpawn's four {@code BlockLeaves} subclasses (BlockAppleLeaves,
 * BlockScaryLeaves, BlockExperienceLeaves, BlockCrystalLeaves). Each of them replaced
 * {@code updateTick} with the same search: a cube of radius {@code r} around the leaf, only the
 * layers at and below it, only positions with Manhattan distance 3 or less, looking for a block
 * that {@link CanSustainLeaves can sustain leaves}. Found: the class does its own thing
 * ({@link #sustained}) and returns. Not found: the leaf drops its loot and turns to air
 * ({@code removeLeaves}, e.g. BlockAppleLeaves.java:81-84).
 *
 * <p>This is not vanilla decay (catalogue README 4.3): no distance property, no {@code persistent}
 * flag, range 3 instead of 7, and only downwards - so a leaf a player places without a log below
 * or beside it decays like any other, exactly as in 1.7.10. Hence a plain {@link Block} with random
 * ticks, not {@code LeavesBlock}.
 *
 * <p>Drops: every class overrode {@code dropBlockAsItemWithChance} without calling {@code super}
 * (no sapling, no fortune, independent rolls) and left {@code quantityDropped} unused. The rolls
 * are the block's loot table; decay ({@code dropBlockAsItem(world, x, y, z, 0, 0)}) and the "fruit
 * falls from the canopy" roll ({@code dropBlockAsItemWithChance(world, x, y - 1, z, 0, 0.0f, 0)})
 * both go through the same table via {@link Block#dropResources}, at the block's own position or
 * one below it. Shears give the leaf block, as 1.7.10 Forge's {@code IShearable} did for every
 * {@code BlockLeaves}. Silk Touch is the one place the four classes differ, because 1.7.10
 * {@code Block.canSilkHarvest} was {@code renderAsNormalBlock() && !hasTileEntity} (client-1.7.10.jar,
 * {@code aji.E}) and {@code BlockLeaves} did not override it: the apple, scary, cherry, peach and
 * experience leaves inherit {@code renderAsNormalBlock() == true} and give the leaf block, while
 * {@code BlockCrystalLeaves} answers {@code FastGraphicsLeaves != 0} (BlockCrystalLeaves.java:86-88) -
 * a client static that a dedicated server never set and fancy graphics left at 0 - so Silk Touch fell
 * through to the fruit rolls there. The crystal loot tables treat Silk Touch like an empty hand.
 *
 * <p>Rendering: {@code isOpaqueCube} followed {@code OreSpawnMain.FastGraphicsLeaves}, a client
 * static; the fast-graphics texture swap is dropped (DECISIONS R18, "schnelle Grafik"), the leaves
 * are always the fancy, non-occluding kind. The 1.7.10 leaf material was not a normal cube either
 * (translucent material): no spawns on it, no suffocation, no redstone through it - the same flags
 * vanilla 1.21.1 leaves carry.
 */
public abstract class OreSpawnLeaves extends Block {

    /**
     * {@code OreSpawnMain.DimensionID4}, the Islands dimension ({@code orespawn:danger}, DECISIONS R13),
     * read by the apple and crystal leaves for their decay radius and fruit chance. Alias of the
     * W05 dimension holder, kept under this name for its callers.
     */
    public static final ResourceKey<Level> DIMENSION_ISLANDS = com.swbr.orespawn.world.dimension.danger.WorldProviderOreSpawn4.DIMENSION;

    protected OreSpawnLeaves(Properties properties) {
        super(properties
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .pushReaction(PushReaction.DESTROY));
    }

    /** {@code BlockLeaves} set {@code tickRandomly} in its constructor; every subclass inherits it. */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** Search radius {@code var7}; 2 for every class, 1 in the Islands dimension for apple and crystal leaves. */
    protected int decayRadius(ServerLevel level) {
        return 2;
    }

    /**
     * The common {@code updateTick} (BlockAppleLeaves.java:46-79, BlockScaryLeaves.java:48-76,
     * BlockExperienceLeaves.java:35-74, BlockCrystalLeaves.java:47-75). A random tick is server-side
     * by construction, which covers the original's {@code isRemote} test.
     */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        final int var7 = this.decayRadius(level);
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        // checkChunksExist(x - r, y - r, z - r, x + r, y + r, z + r)
        if (!level.hasChunksAt(x - var7, y - var7, z - var7, x + var7, y + var7, z + var7)) {
            return;
        }
        for (int var8 = -var7; var8 <= var7; ++var8) {
            for (int var9 = -var7; var9 <= 0; ++var9) {
                for (int var10 = -var7; var10 <= var7; ++var10) {
                    final int totaldist = Math.abs(var8) + Math.abs(var9) + Math.abs(var10);
                    if (totaldist <= 3) {
                        final BlockState bid = level.getBlockState(pos.offset(var8, var9, var10));
                        if (!bid.isAir() && CanSustainLeaves.canSustainLeaves(bid)) {
                            this.sustained(state, level, pos, random);
                            return;
                        }
                    }
                }
            }
        }
        this.removeLeaves(state, level, pos);
    }

    /** What the class does once a supporting block is found; the original's code before its {@code return}. */
    protected abstract void sustained(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);

    /** {@code removeLeaves} (e.g. BlockAppleLeaves.java:81-84): the class's own drop rolls, then air with flag 2. */
    protected void removeLeaves(BlockState state, ServerLevel level, BlockPos pos) {
        dropResources(state, level, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    /**
     * {@code dropBlockAsItemWithChance(world, x, y - 1, z, 0, 0.0f, 0)}: the class's own rolls, spawned
     * at the block below the leaf. The chance argument was ignored by every override.
     */
    protected static void dropBelow(BlockState state, ServerLevel level, BlockPos pos) {
        dropResources(state, level, pos.below());
    }
}
