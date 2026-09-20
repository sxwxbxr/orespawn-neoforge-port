package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenerator} ({@code arn}): the populate-time feature contract, with the
 * world replaced by {@link LegacyWorld}.
 */
public abstract class WorldGenerator {

    /**
     * {@code arn.a} ({@code doBlockNotify}): {@code true} only for generators built for a player action (saplings,
     * bone meal). Every generator the decorators build passes {@code false}.
     */
    private final boolean doBlockNotify;

    /** {@code arn()}. */
    protected WorldGenerator() {
        this(false);
    }

    /** {@code arn(boolean)}. */
    protected WorldGenerator(final boolean doBlockNotify) {
        this.doBlockNotify = doBlockNotify;
    }

    /** {@code arn.a(World, Random, int, int, int)} ({@code generate}). */
    public abstract boolean generate(LegacyWorld world, Random rand, int x, int y, int z);

    /** {@code arn.a(double, double, double)} ({@code setScale}): empty; {@link WorldGenBigTree} overrides it. */
    public void setScale(final double scaleHeight, final double scaleWidth, final double scaleLeafDensity) {
    }

    /**
     * {@code arn.a(World, int, int, int, Block, int)} ({@code setBlockAndNotifyAdequately}):
     * {@code setBlock(x, y, z, block, meta, 3)} with {@code doBlockNotify}, flag 2 without. The metadata is part
     * of the passed state.
     */
    protected void setBlockAndNotifyAdequately(final LegacyWorld world, final int x, final int y, final int z,
                                               final BlockState state) {
        world.setBlock(x, y, z, state, this.doBlockNotify ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS);
    }
}
