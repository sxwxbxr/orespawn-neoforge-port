package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenClay} ({@code arj}): like {@link WorldGenSand}, but one block
 * up and down, and it replaces dirt and clay (not grass).
 */
public class WorldGenClay extends WorldGenerator {

    private static final BlockState CLAY = Blocks.CLAY.defaultBlockState();
    private final int radius;

    /** {@code arj(int)}. */
    public WorldGenClay(final int radius) {
        this.radius = radius;
    }

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        if (!LegacyWorld.isWater(world.getBlock(x, y, z))) {
            return false;
        }
        final int r = rand.nextInt(this.radius - 2) + 2;
        final int height = 1;
        for (int bx = x - r; bx <= x + r; ++bx) {
            for (int bz = z - r; bz <= z + r; ++bz) {
                final int dx = bx - x;
                final int dz = bz - z;
                if (dx * dx + dz * dz <= r * r) {
                    for (int by = y - height; by <= y + height; ++by) {
                        final BlockState current = world.getBlock(bx, by, bz);
                        if (current.is(Blocks.DIRT) || current.is(Blocks.CLAY)) {
                            world.setBlock(bx, by, bz, CLAY);
                        }
                    }
                }
            }
        }
        return true;
    }
}
