package com.swbr.orespawn.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockExperiencePlant}: Experience Tree Sapling
 * ({@code experiencesapling}, OreSpawnMain.java:1612). A {@code BlockReed} with random ticks and
 * <em>no</em> creative tab (BlockExperiencePlant.java:13-17); planted by {@code experiencetree_seed}
 * (W02 materials porter, OreSpawnMain.java:1611).
 *
 * <ul>
 *   <li>Ground: grass, dirt, farmland (:19-22).</li>
 *   <li>Client: 1/20 per display tick, 20 happy-villager particles (:24-31).</li>
 *   <li>Random tick: 1/10 the sapling becomes air and {@code OreSpawnTrees.ExperienceTree} grows
 *       from the block below (:33-42).</li>
 *   <li>Drops one of itself (:44-50) - loot table {@code blocks/experiencesapling}.</li>
 * </ul>
 */
public class BlockExperiencePlant extends ReedLikePlant {

    public BlockExperiencePlant(Properties properties) {
        super(properties);
    }

    /** {@code canPlaceBlockAt} (BlockExperiencePlant.java:19-22). */
    @Override
    protected boolean mayPlaceOn(BlockState soil) {
        return isGrass(soil) || isDirt(soil) || isFarmland(soil);
    }

    /** {@code randomDisplayTick} (BlockExperiencePlant.java:24-31). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 20; ++j1) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(),
                    0.0, 0.0, 0.0);
        }
    }

    /** {@code updateTick} (BlockExperiencePlant.java:33-42); server side by construction. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) != 1) {
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        com.swbr.orespawn.world.tree.Trees.ExperienceTree(level, pos.getX(), pos.getY() - 1, pos.getZ()); // Trees.java:294, BlockExperiencePlant.java:33-42
    }
}
