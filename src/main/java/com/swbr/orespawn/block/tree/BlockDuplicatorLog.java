package com.swbr.orespawn.block.tree;

import com.swbr.orespawn.config.OreSpawnConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockDuplicatorLog}: Duplicator Tree Wood ({@code duplicatortreelog},
 * OreSpawnMain.java:1608, hardness 0.2, wood sound). A {@code Material.wood} block, tab Blocks, random
 * ticks (BlockDuplicatorLog.java:14-18), that grows the Duplicator Tree around itself on every random
 * tick while {@code DuplicatorTreeEnable} is set (:24-31).
 *
 * <p>{@code tickRate()} without parameters (:20-22) overrode nothing in 1.7.10 and is dead.
 * {@code canSustainLeaves}/{@code isWood} true (:41-47) - {@link CanSustainLeaves}. Drops one of itself
 * (:49-51) - loot table {@code blocks/duplicatortreelog}.
 */
public class BlockDuplicatorLog extends Block implements CanSustainLeaves {

    public BlockDuplicatorLog(Properties properties) {
        super(properties);
    }

    /** {@code setTickRandomly(true)} (BlockDuplicatorLog.java:17). */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** {@code updateTick} (BlockDuplicatorLog.java:24-31); server only, which a random tick already is. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (OreSpawnConfig.TWEAKS.DuplicatorTreeEnable.get() != 0) {
            com.swbr.orespawn.world.tree.Trees.DuplicatorTree(level, pos.getX(), pos.getY(), pos.getZ()); // Trees.java:122
        }
    }
}
