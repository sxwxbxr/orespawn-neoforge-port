package com.swbr.orespawn.block.repellent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The wall half of {@link CreeperRepellent} ({@code creeperrepellent_wall}, DECISIONS R18 torch ids): metadata 1-4 of
 * the 1.7.10 block. Same tick chain, repel scan and no-drop behaviour; only the particle point differs.
 */
public class CreeperRepellentWall extends WallTorchBlock {

    public CreeperRepellentWall(final Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /**
     * {@code randomDisplayTick} (:19-46), wall branches: 0.271 towards the supporting block, 0.413 above the flame point.
     * Metadata 1 (support to the west, facing east) is {@code x - 0.271}, as {@code BlockExtremeWallTorch}.
     */
    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
        final Direction toWall = state.getValue(FACING).getOpposite();
        final double var7 = pos.getX() + 0.5f;
        final double var8 = pos.getY() + 0.7f;
        final double var9 = pos.getZ() + 0.5f;
        final double var10 = 0.413;
        final double var11 = 0.271;
        CreeperRepellent.spawnParticles(level, var7 + var11 * toWall.getStepX(), var8 + var10, var9 + var11 * toWall.getStepZ());
    }

    /** {@code onBlockAdded} (:65-67). */
    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState,
                           final boolean movedByPiston) {
        level.scheduleTick(pos, this, CreeperRepellent.TICK_RATE);
    }

    /** {@code onNeighborBlockChange} (:69-71). */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block neighborBlock,
                                   final BlockPos neighborPos, final boolean movedByPiston) {
        level.scheduleTick(pos, this, CreeperRepellent.TICK_RATE);
    }

    /** No drop when the wall goes (see {@link CreeperRepellent}). */
    @Override
    protected BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState,
                                     final LevelAccessor level, final BlockPos currentPos, final BlockPos facingPos) {
        return state;
    }

    /** {@code setTickRandomly(true)} of {@code BlockTorch}. */
    @Override
    protected boolean isRandomlyTicking(final BlockState state) {
        return true;
    }

    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        CreeperRepellent.updateTick(this, level, pos);
    }

    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        CreeperRepellent.updateTick(this, level, pos);
    }
}
