package com.swbr.orespawn.block.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Port of {@code danger.orespawn.BlockSkyTreeLog}: Sky Tree Wood ({@code skytreelog},
 * OreSpawnMain.java:1607, hardness 0.2, wood sound). A {@code Material.wood} block, tab Blocks
 * (BlockSkyTreeLog.java:15-18), whose whole connected trunk falls when a player breaks one block.
 *
 * <p>{@code canSustainLeaves}/{@code isWood} true (:28-34) - {@link CanSustainLeaves}. Drops itself
 * (:36-38) - loot table {@code blocks/skytreelog}.
 *
 * <p><b>Double drop, 1:1 (DECISIONS R18, catalogue 6.4).</b> In 1.7.10 {@code tryHarvestBlock} ran
 * {@code removedByPlayer} (block to air), then {@link #onBlockDestroyedByPlayer} (which dropped one log
 * itself, :68), then {@code harvestBlock} (a second log). The 1.21.1 flow is the same shape:
 * {@link #onDestroyedByPlayer} is the {@code removedByPlayer} hook and drops the first log,
 * {@code playerDestroy} drops the second in survival. Creative mode ran the hook too, so a creative
 * player fells the tree with drops and gets one log from the block itself - as before.
 */
public class BlockSkyTreeLog extends Block implements CanSustainLeaves {

    public BlockSkyTreeLog(Properties properties) {
        super(properties);
    }

    /**
     * {@code breakRecursor(world, x, y, z, xf, yf, zf, recursion)} (BlockSkyTreeLog.java:40-63).
     * Walks the 26 neighbours of {@code (x, y, z)}, skipping the caller position and - from depth 1 on -
     * everything inside the caller's own 3x3x3 cube; every sky tree log found becomes air silently (flag 2, R26),
     * drops one log and recurses. Depth cap 1000.
     */
    public void breakRecursor(final Level world, final int x, final int y, final int z,
                              final int xf, final int yf, final int zf, final int recursion) {
        final int var7 = 1;
        if (recursion > 1000) {
            return;
        }
        for (int var8 = -var7; var8 <= var7; ++var8) {
            for (int var9 = -var7; var9 <= var7; ++var9) {
                for (int var10 = -var7; var10 <= var7; ++var10) {
                    if (var8 != 0 || var9 != 0 || var10 != 0) {
                        if (x + var8 != xf || y + var9 != yf || z + var10 != zf) {
                            if (recursion <= 0
                                    || x + var8 < xf - var7 || x + var8 > xf + var7
                                    || y + var9 < yf - var7 || y + var9 > yf + var7
                                    || z + var10 < zf - var7 || z + var10 > zf + var7) {
                                final BlockPos at = new BlockPos(x + var8, y + var9, z + var10);
                                final BlockState var11 = world.getBlockState(at);
                                if (var11.is(this)) {
                                    // PORT: R26 - 1.7.10 flag 2 meant no neighbour and no shape updates; in 1.21.1
                                    // UPDATE_CLIENTS alone still runs updateNeighbourShapes (leaf decay, falling
                                    // sand, water). UPDATE_KNOWN_SHAPE restores the silent write: the crown floats.
                                    world.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                                    // dropBlockAsItem(world, x, y, z, 0, 0): one log by getItemDropped.
                                    dropResources(var11, world, at);
                                    this.breakRecursor(world, x + var8, y + var9, z + var10, x, y, z, recursion + 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * {@code onBlockDestroyedByPlayer} (BlockSkyTreeLog.java:65-69): air (UPDATE_ALL, R26), the recursion, then
     * one log. Runs on both sides like the original; {@code dropResources} is a no-op on the client.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       boolean willHarvest, FluidState fluid) {
        // PORT: R26 - Forge 1.7.10 removedByPlayer had already set this block to air with flag 3 before
        // onBlockDestroyedByPlayer's flag-2 write (a no-op on air). The mined block therefore notifies its
        // neighbours (a rail on it drops); only the recursive trunk blocks are silent.
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        this.breakRecursor(level, pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ(), 0);
        dropResources(state, level, pos);
        return true;
    }
}
