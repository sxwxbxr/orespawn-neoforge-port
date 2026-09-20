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
 * The wall half of {@link KrakenRepellent} ({@code krakenrepellent_wall}, DECISIONS R18): metadata 1-4 of the 1.7.10
 * block. Same tick chain, same repelling box, same "never drops" rule; register with
 * {@link KrakenRepellent#originalProperties()}.
 */
public class KrakenRepellentWall extends WallTorchBlock {

    public KrakenRepellentWall(final Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /**
     * {@code randomDisplayTick} (:18-51), wall branches (metadata 1-4): 0.271 towards the supporting block, 0.413 up
     * from the flame point. Metadata 1 (support at x - 1) is {@code FACING} east here, so the offset follows
     * {@code FACING.getOpposite()} (BlockExtremeWallTorch precedent, W02).
     */
    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
        final Direction toWall = state.getValue(FACING).getOpposite();
        final double var7 = pos.getX() + 0.5f;
        final double var8 = pos.getY() + 0.7f;
        final double var9 = pos.getZ() + 0.5f;
        final double var10 = 0.413;
        final double var11 = 0.271;
        KrakenRepellent.spawnParticles(level, var7 + var11 * toWall.getStepX(), var8 + var10, var9 + var11 * toWall.getStepZ());
    }

    /** {@code updateTick} (:57-62). */
    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        KrakenRepellent.repelTick(this, level, pos);
    }

    /** A random tick of {@code BlockTorch} ran {@code updateTick} (:57-62) as well. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        KrakenRepellent.repelTick(this, level, pos);
    }

    /** {@code onBlockAdded} (:64-66). */
    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        level.scheduleTick(pos, this, KrakenRepellent.TICK_RATE);
    }

    /** {@code onNeighborBlockChange} (:68-70), without the torch's support check. */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block neighborBlock,
                                   final BlockPos neighborPos, final boolean movedByPiston) {
        level.scheduleTick(pos, this, KrakenRepellent.TICK_RATE);
    }

    /** PORT: see {@link KrakenRepellent#updateShape} - the wall repellent does not fall off either. */
    @Override
    protected BlockState updateShape(final BlockState state, final Direction direction, final BlockState neighborState,
                                     final LevelAccessor level, final BlockPos pos, final BlockPos neighborPos) {
        return state;
    }
}
