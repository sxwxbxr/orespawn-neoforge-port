package com.swbr.orespawn.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code MoleDirtBlock} (MoleDirtBlock.java:14-45): Molenoid Dirt, id {@code moledirt}
 * (OreSpawnMain.java:1545 - hardness 0.6, gravel sound 1.0/1.0). The dirt the Molenoid throws up: it
 * vanishes on its next random tick, its top sits at 14/16, and anything inside it is slowed to 30 %.
 * Drops itself when mined (no override).
 */
public class MoleDirtBlock extends Block {

    /** {@code getCollisionBoundingBoxFromPool} (:29-32): {@code f = 0.125}, top at {@code y + 1 - f}. */
    private static final VoxelShape COLLISION = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

    public MoleDirtBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * {@code Material.ground}, random ticks (:17-19); hardness 0.6 and the gravel sound come from the
     * registration chain (OreSpawnMain.java:1545). Only {@code setHardness} was called, so the resistance
     * is the hardness. Ground material needs no tool.
     */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.6f, Legacy.resistanceFromHardnessOnly(0.6f))
                .sound(SoundType.GRAVEL)
                .randomTicks();
    }

    /** {@code updateTick} (:22-27): server only, block to air with flag 2 (clients, no neighbour update), no drop. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    /** {@code onEntityCollidedWithBlock} (:34-39): every entity, horizontal motion times 0.3. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity != null) {
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x * 0.3, motion.y, motion.z * 0.3);
        }
    }
}
