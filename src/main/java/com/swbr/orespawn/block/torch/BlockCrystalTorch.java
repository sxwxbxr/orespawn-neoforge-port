package com.swbr.orespawn.block.torch;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockCrystalTorch}, standing half: Crystal Torch
 * ({@code crystaltorch}, OreSpawnMain.java:1600, light 0.99 -> 14), tab Decorations
 * (BlockCrystalTorch.java:13-15). The wall half is {@link BlockCrystalWallTorch} under
 * {@code crystaltorch_wall} (DECISIONS R18, torch ids); one {@code StandingAndWallBlockItem} places both.
 *
 * <p>The point of the class: the crystal blocks are not "normal cubes" in 1.7.10 (see
 * {@code block.tree.CrystalCube}), so a vanilla torch cannot attach to them. This torch names them
 * as valid supports (:50-53): Crystal Stone, Crystal Grass, Crystal Tree Wood, Crystal Planks - on
 * top ({@code canPlaceTorchOn}, :59-62) and on the side ({@code isItSolidOnSide}, :55-57), in
 * addition to whatever vanilla accepts.
 *
 * <p>Placement (:64-86): the original resolved the side from the clicked face and fell back to
 * {@code BlockTorch.onBlockAdded}'s first-valid-side search. PORT: 1.21.1's
 * {@code StandingAndWallBlockItem} does the same through {@code canSurvive}, trying the clicked face
 * first and then the player's look directions; the fallback order differs from the original's fixed
 * west-east-north-south-down, the result is a torch on some valid side in both.
 *
 * <p><b>And then it falls off (R18, kept 1:1).</b> Only {@code canPlaceBlockAt} and {@code onBlockPlaced}
 * were overridden. {@code BlockTorch.onNeighborBlockChange} ({@code func_150108_b}) was inherited, and
 * after its {@code dropTorchIfCantStay} pass (which does use the crystal-aware {@code canPlaceBlockAt})
 * it re-checked the attached side with vanilla {@code World.isSideSolid} (meta 1-4) or the private
 * {@code canPlaceTorchOn} ({@code func_150107_m}, meta 5) - both false for a block whose
 * {@code renderAsNormalBlock} is false. So a crystal torch on Crystal Planks or a Crystal Tree Log
 * survived exactly until the next neighbour update, then dropped as an item. {@link #neighborChanged}
 * reproduces the vanilla-only re-check; {@code canSurvive} stays crystal-aware for placement and for
 * the "support gone entirely" case that {@code updateShape} handles like {@code dropTorchIfCantStay}.
 * Crystal Stone and Crystal Grass answered {@code current_dimension == DimensionID5} there - the port
 * keeps them normal cubes (see {@code block.plant.CrystalGrass}), so on those the torch stays, as it did
 * in the single-player Crystal dimension.
 *
 * <p>{@code randomDisplayTick} (:18-48): 1/4 per display tick, a firework spark and a flame with
 * random velocity at the flame position.
 */
public class BlockCrystalTorch extends TorchBlock {

    /**
     * {@code OreSpawnMain.CrystalStone} ({@code crystalstone}), registered by the W02 ores porter;
     * referenced by id so this class does not depend on that holder's field name.
     */
    public static final ResourceKey<Block> CRYSTAL_STONE = ResourceKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "crystalstone"));

    public BlockCrystalTorch(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /** {@code isCrystalBlock} (BlockCrystalTorch.java:50-53). */
    static boolean isCrystalBlock(BlockGetter world, BlockPos pos) {
        final BlockState l = world.getBlockState(pos);
        return l.is(CRYSTAL_STONE)
                || l.is(ModBlocks.CRYSTAL_GRASS)
                || l.is(ModBlocks.CRYSTAL_TREE_LOG)
                || l.is(ModBlocks.CRYSTAL_PLANKS);
    }

    /**
     * {@code canPlaceTorchOn} (BlockCrystalTorch.java:59-62): a crystal block, a solid top surface or a
     * block that allows torches on top (fences, glass ...) - the last two are 1.21.1's
     * {@code canSupportCenter}.
     */
    static boolean canPlaceTorchOn(LevelReader world, BlockPos pos) {
        return isCrystalBlock(world, pos) || Block.canSupportCenter(world, pos, Direction.UP);
    }

    /** The standing torch's share of {@code canPlaceBlockAt} (:65): {@code canPlaceTorchOn(x, y - 1, z)}. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canPlaceTorchOn(level, pos.below());
    }

    /**
     * Inherited {@code BlockTorch.onNeighborBlockChange}, meta 5 branch: {@code !func_150107_m(x, y - 1, z)}
     * - the vanilla check without the crystal list - drops the torch (see the class comment).
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!Block.canSupportCenter(level, pos.below(), Direction.UP)) {
            dropTorch(state, level, pos);
        }
    }

    /** {@code dropBlockAsItem(world, x, y, z, meta, 0)} then {@code setBlockToAir} (flag 3). */
    static void dropTorch(BlockState state, Level level, BlockPos pos) {
        dropResources(state, level, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    /** {@code randomDisplayTick} (BlockCrystalTorch.java:18-48), standing branch: (0.5, 0.7, 0.5). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 1) {
            return;
        }
        spawnParticles(level, random, pos.getX() + 0.5f, pos.getY() + 0.7f, pos.getZ() + 0.5f);
    }

    /** The two particles of every branch of {@code randomDisplayTick}: {@code fireworksSpark} and {@code flame}. */
    static void spawnParticles(Level world, RandomSource rand, double x, double y, double z) {
        world.addParticle(ParticleTypes.FIREWORK, x, y, z,
                (rand.nextFloat() - rand.nextFloat()) / 8.0f,
                rand.nextFloat() / 8.0f,
                (rand.nextFloat() - rand.nextFloat()) / 8.0f);
        world.addParticle(ParticleTypes.FLAME, x, y, z,
                (rand.nextFloat() - rand.nextFloat()) / 60.0f,
                rand.nextFloat() / 10.0f,
                (rand.nextFloat() - rand.nextFloat()) / 60.0f);
    }
}
