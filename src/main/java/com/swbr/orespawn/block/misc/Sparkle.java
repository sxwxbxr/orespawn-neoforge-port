package com.swbr.orespawn.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * The six-position particle loop that OreTitanium, OreUranium, BlockTitanium, BlockUranium and Lavafoam
 * carry as a private {@code sparkle(World, int, int, int)} (OreTitanium.java:70-99, BlockTitanium.java:32-70,
 * Lavafoam.java:35-70 - the loops are line-identical, only the particle choice differs). One position per
 * face, pushed 1/16 outside the block when the neighbour on that face is not an opaque cube, and only
 * emitted when it ended up outside the block.
 *
 * <p>No original class; the callers keep their own particle choice in the {@link Emitter}.
 */
public final class Sparkle {

    /** What to spawn at one accepted position. */
    @FunctionalInterface
    public interface Emitter {
        void emit(Level level, double x, double y, double z);
    }

    private Sparkle() {}

    /**
     * Runs the loop for the block at {@code pos}. Face order as in the original: 0 up, 1 down, 2 +z (south),
     * 3 -z (north), 4 +x (east), 5 -x (west).
     */
    public static void sixSides(Level level, BlockPos pos, Emitter emitter) {
        RandomSource random = level.random;
        final double outset = 0.0625;
        int bx = pos.getX();
        int by = pos.getY();
        int bz = pos.getZ();
        for (int face = 0; face < 6; ++face) {
            double x = bx + random.nextFloat();
            double y = by + random.nextFloat();
            double z = bz + random.nextFloat();
            if (face == 0 && !isOpaqueCube(level, pos.above())) {
                y = by + 1 + outset;
            }
            if (face == 1 && !isOpaqueCube(level, pos.below())) {
                y = by + 0 - outset;
            }
            if (face == 2 && !isOpaqueCube(level, pos.south())) {
                z = bz + 1 + outset;
            }
            if (face == 3 && !isOpaqueCube(level, pos.north())) {
                z = bz + 0 - outset;
            }
            if (face == 4 && !isOpaqueCube(level, pos.east())) {
                x = bx + 1 + outset;
            }
            if (face == 5 && !isOpaqueCube(level, pos.west())) {
                x = bx + 0 - outset;
            }
            // The y lower bound reads "< 0.0" instead of "< by" in every original copy (OreTitanium.java:95).
            // Above y = 0 that only means a particle pushed below the block is never accepted; below y = 0,
            // which 1.7.10 did not have, every position passes. Kept as written (R18).
            if (x < bx || x > bx + 1 || y < 0.0 || y > by + 1 || z < bz || z > bz + 1) {
                emitter.emit(level, x, y, z);
            }
        }
    }

    /** 1.7.10 {@code Block.isOpaqueCube()}: a full, opaque cube. */
    private static boolean isOpaqueCube(Level level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender(level, pos);
    }
}
