package com.swbr.orespawn.block.torch;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The wall half of {@link BlockExtremeTorch} ({@code extremetorch_wall}, DECISIONS R18): metadata
 * 1-4 of the 1.7.10 block. Support is vanilla's. The Cephadrome summoning checks the block below
 * the torch's own position, wall-mounted or not (BlockExtremeTorch.java:64), so it is shared.
 */
public class BlockExtremeWallTorch extends WallTorchBlock {

    public BlockExtremeWallTorch(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /** {@code randomDisplayTick} (BlockExtremeTorch.java:20-51), wall branches: offset 0.271 towards the wall, 0.213 up. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        final Direction toWall = state.getValue(FACING).getOpposite();
        final double var7 = pos.getX() + 0.5f;
        final double var8 = pos.getY() + 0.7f;
        final double var9 = pos.getZ() + 0.5f;
        final double var10 = 0.213;
        final double var11 = 0.271;
        BlockExtremeTorch.spawnParticles(level,
                var7 + var11 * toWall.getStepX(), var8 + var10, var9 + var11 * toWall.getStepZ());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockExtremeTorch.onBlockPlacedBy(level, pos, placer);
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
