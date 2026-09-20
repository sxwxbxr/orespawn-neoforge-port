package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenSand} ({@code asj}): a sand or gravel disk under water. Only
 * starts when the given position is water, then replaces dirt and grass within radius
 * {@code 2 + nextInt(radius - 2)} and two blocks up and down.
 */
public class WorldGenSand extends WorldGenerator {

    private final BlockState block;
    private final int radius;

    /** {@code asj(Block, int)}. */
    public WorldGenSand(final BlockState block, final int radius) {
        this.block = block;
        this.radius = radius;
    }

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        if (!LegacyWorld.isWater(world.getBlock(x, y, z))) {
            return false;
        }
        final int r = rand.nextInt(this.radius - 2) + 2;
        final int height = 2;
        for (int bx = x - r; bx <= x + r; ++bx) {
            for (int bz = z - r; bz <= z + r; ++bz) {
                final int dx = bx - x;
                final int dz = bz - z;
                if (dx * dx + dz * dz <= r * r) {
                    for (int by = y - height; by <= y + height; ++by) {
                        final BlockState current = world.getBlock(bx, by, bz);
                        if (current.is(Blocks.DIRT) || current.is(Blocks.GRASS_BLOCK)) {
                            world.setBlock(bx, by, bz, this.block);
                        }
                    }
                }
            }
        }
        return true;
    }
}
