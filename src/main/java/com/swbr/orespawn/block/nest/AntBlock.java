package com.swbr.orespawn.block.nest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.ore.OreBasicStone;
import com.swbr.orespawn.config.stats.MobSwitches;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Port of {@code danger.orespawn.AntBlock} (AntBlock.java:17-133), five nests
 * (OreSpawnMain.java:5943-5948, registered :1847-1852, all {@code CreativeTabs.tabBlock}):
 * <ul>
 *   <li>{@code antblock} Ant Nest (+115) - "Ant", {@code BlackAntEnable}</li>
 *   <li>{@code redantblock} Red Ant Nest (+116) - "Red Ant", {@code RedAntEnable}</li>
 *   <li>{@code rainbowantblock} Rainbow Ant Nest (+117) - "Rainbow Ant", {@code RainbowedAntEnable}
 *       (the key is spelt that way; the field was {@code RainbowAntEnable})</li>
 *   <li>{@code unstableantblock} Unstable Ant Nest (+118) - "Unstable Ant", {@code UnstableAntEnable}</li>
 *   <li>{@code termiteblock} Termite Nest (+120) - "Termite", {@code TermiteEnable}</li>
 * </ul>
 * The original chose the creature with {@code this == OreSpawnMain.MyAntBlock} and friends, Rainbow
 * Ant being the final {@code else} (:55-79); each block instance matches exactly one branch, so the
 * port hands every registration its creature and switch instead. A permanent spawner without a cap:
 * every random tick without rain and with air on top releases 2 to 7 creatures.
 *
 * <p>Base class: the original extended {@code BlockGrass} but overrode its tick (no spreading), its
 * icons (no snowy side) and its drop. What it kept is {@code IGrowable} - bone meal - which is ported
 * below from the bytecode of {@code BlockGrass.func_149853_b} ({@code alh.b}). The 1.21.1
 * {@code GrassBlock} is not a usable parent: its bone-meal walk tests {@code is(this)} where 1.7.10
 * tested {@code Blocks.grass}, and it carries the {@code SNOWY} property and the spreading tick.
 *
 * <p>Material grass ({@code alh.<init>}: {@code awt.b}), no hardness anywhere - neither in the class
 * nor on the registration line - so hardness 0 and resistance 0; the sound stays the
 * {@code Block} default {@code soundTypeStone}, because {@code BlockGrass}' own grass sound was set on
 * the vanilla {@code Blocks} line, not in its constructor. No harvest tool. Drops itself (:84-86).
 *
 * <p>Tint: the world colour is the 3x3 biome-grass average (:111-125), the inventory colour
 * {@code ColorizerGrass.getGrassColor(0.5, 1.0)} (:99-109). Both are client registrations
 * ({@code client.BlockRenderTypes}); every face carries the tint index, because
 * {@code RenderBlocks.renderStandardBlockWithColorMultiplier} spared the bottom and the sides only for
 * {@code Blocks.grass} itself ({@code blm.d}, the {@code getstatic ajn.c} test).
 */
public class AntBlock extends Block implements BonemealableBlock {

    private final Supplier<EntityType<? extends Mob>> critter;
    private final ModConfigSpec.IntValue enable;

    /**
     * @param critter the creature this nest releases
     * @param enable  the OreSpawnMOBS switch checked before every single spawn
     */
    public AntBlock(final BlockBehaviour.Properties properties, final Supplier<EntityType<? extends Mob>> critter,
                    final ModConfigSpec.IntValue enable) {
        super(properties);
        this.critter = critter;
        this.enable = enable;
    }

    /** {@code AntBlock(int)} (:19-22): {@code setTickRandomly(true)}; Material grass, hardness 0. */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .instabreak()
                .randomTicks();
    }

    /** {@code updateTick} (:46-82), server only. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel par1World, final BlockPos pos, final RandomSource par5Random) {
        int howmany = 0;
        if (!par1World.isClientSide) {
            if (par1World.isRaining()) {
                return;
            }
            // PORT: "bid == Blocks.air" is isAir(): cave and void air did not exist in 1.7.10.
            final BlockState bid = par1World.getBlockState(pos.above());
            if (bid.isAir()) {
                howmany = OreSpawn.OreSpawnRand.nextInt(6) + 2;
                for (int i = 0; i < howmany; ++i) {
                    if (MobSwitches.enabled(this.enable)) {
                        spawnCreature(par1World, this.critter.get(), pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
                    }
                }
            }
        }
    }

    /**
     * {@code spawnCreature} (:88-97): create, random yaw from the level's random, add, ambient sound
     * (ants have none). Identical to {@code OreBasicStone.spawnCreature}, which it reuses.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<? extends Mob> type,
                                       final double par2, final double par4, final double par6) {
        return OreBasicStone.spawnCreature(par0World, type, par2, par4, par6);
    }

    /** {@code BlockGrass.func_149851_a} ({@code alh.a(ahb,III,Z)}): always true, air on top or not. */
    @Override
    public boolean isValidBonemealTarget(final LevelReader level, final BlockPos pos, final BlockState state) {
        return true;
    }

    /** {@code BlockGrass.func_149852_a} ({@code alh.a(ahb,Random,III)}): always true. */
    @Override
    public boolean isBonemealSuccess(final Level level, final RandomSource random, final BlockPos pos, final BlockState state) {
        return true;
    }

    /**
     * {@code BlockGrass.func_149853_b} ({@code alh.b}, bytecode offsets 0-310): 128 random walks
     * from the block above, {@code l / 16} steps each; a step must keep {@code Blocks.grass} below and
     * no normal cube at the spot. At the end of the walk, on air: 7 in 8 tall grass if it can stay, 1 in
     * 8 the biome's flower if it can stay, both with flag 3.
     *
     * <p>On a lone nest nothing grows: the short walks end right above the nest, where neither tall
     * grass nor a flower can stay ({@code BlockBush.canPlaceBlockOn}: grass, dirt, farmland only), and
     * the long walks need real grass below. Bone meal is still used up.
     *
     * <p>PORT: {@code Blocks.tallgrass} meta 1 is {@code SHORT_GRASS}; {@code isNormalCube()} is
     * {@code isCollisionShapeFullBlock}, the test 1.21.1's own {@code GrassBlock} uses in the same place.
     * The flower name of {@code BiomeGenBase.func_150572_a} has no 1.21.1 counterpart; the biome's first
     * flower feature is placed instead, exactly as {@code GrassBlock.performBonemeal} does, and its
     * placement filter plays the part of {@code canBlockStay}.
     */
    @Override
    public void performBonemeal(final ServerLevel level, final RandomSource random, final BlockPos pos, final BlockState state) {
        for (int l = 0; l < 128; ++l) {
            int i1 = pos.getX();
            int j1 = pos.getY() + 1;
            int k1 = pos.getZ();
            int l1 = 0;
            while (true) {
                if (l1 >= l / 16) {
                    final BlockPos at = new BlockPos(i1, j1, k1);
                    if (level.getBlockState(at).isAir()) {
                        if (random.nextInt(8) != 0) {
                            final BlockState tallgrass = Blocks.SHORT_GRASS.defaultBlockState();
                            if (tallgrass.canSurvive(level, at)) {
                                level.setBlock(at, tallgrass, Block.UPDATE_ALL);
                            }
                        } else {
                            final List<ConfiguredFeature<?, ?>> flowers =
                                    level.getBiome(at).value().getGenerationSettings().getFlowerFeatures();
                            if (!flowers.isEmpty()) {
                                final Holder<PlacedFeature> flower = ((RandomPatchConfiguration) flowers.get(0).config()).feature();
                                flower.value().place(level, level.getChunkSource().getGenerator(), random, at);
                            }
                        }
                    }
                    break;
                }
                i1 += random.nextInt(3) - 1;
                j1 += (random.nextInt(3) - 1) * random.nextInt(3) / 2;
                k1 += random.nextInt(3) - 1;
                final BlockPos step = new BlockPos(i1, j1, k1);
                if (!level.getBlockState(step.below()).is(Blocks.GRASS_BLOCK)
                        || level.getBlockState(step).isCollisionShapeFullBlock(level, step)) {
                    break;
                }
                ++l1;
            }
        }
    }
}
