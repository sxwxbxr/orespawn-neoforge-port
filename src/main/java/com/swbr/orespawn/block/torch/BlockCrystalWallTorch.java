package com.swbr.orespawn.block.torch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The wall half of {@link BlockCrystalTorch} ({@code crystaltorch_wall}, DECISIONS R18): metadata 1-4
 * of the 1.7.10 block as a {@code WallTorchBlock}. {@code FACING} is the direction the torch points
 * away from its wall: meta 1 (wall at x-1, particles at {@code x - 0.271}) is {@code EAST}, meta 2
 * {@code WEST}, meta 3 (wall at z-1) {@code SOUTH}, meta 4 {@code NORTH}.
 *
 * <p>Support (BlockCrystalTorch.java:55-57, :73-84): the block behind the torch is a crystal block or
 * reports a solid face - the latter is vanilla's {@code WallTorchBlock.canSurvive}. That holds for
 * placement only: on the next neighbour update the inherited {@code BlockTorch.onNeighborBlockChange}
 * asked vanilla {@code isSideSolid} alone and dropped the torch (see {@link BlockCrystalTorch}).
 */
public class BlockCrystalWallTorch extends WallTorchBlock {

    public BlockCrystalWallTorch(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /** {@code isItSolidOnSide}: {@code isCrystalBlock(...) || world.isSideSolid(...)} (BlockCrystalTorch.java:55-57). */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        final Direction facing = state.getValue(FACING);
        final BlockPos attached = pos.relative(facing.getOpposite());
        return BlockCrystalTorch.isCrystalBlock(level, attached) || WallTorchBlock.canSurvive(level, pos, facing);
    }

    /**
     * Inherited {@code BlockTorch.onNeighborBlockChange}, meta 1-4 branches: {@code !isSideSolid(...)} on
     * the attached side - the vanilla check without the crystal list - drops the torch.
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!WallTorchBlock.canSurvive(level, pos, state.getValue(FACING))) {
            BlockCrystalTorch.dropTorch(state, level, pos);
        }
    }

    /** {@code randomDisplayTick} (BlockCrystalTorch.java:18-48), wall branches: offset 0.271 towards the wall, 0.213 up. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 1) {
            return;
        }
        final Direction toWall = state.getValue(FACING).getOpposite();
        final double var7 = pos.getX() + 0.5f;
        final double var8 = pos.getY() + 0.7f;
        final double var9 = pos.getZ() + 0.5f;
        final double var10 = 0.213;
        final double var11 = 0.271;
        BlockCrystalTorch.spawnParticles(level, random,
                var7 + var11 * toWall.getStepX(), var8 + var10, var9 + var11 * toWall.getStepZ());
    }
}
