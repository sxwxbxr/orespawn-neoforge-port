package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenReed} ({@code ash}): 20 tries in a spread of 4 on the given Y;
 * a try needs air and water next to the block below, then stacks {@code 2 + nextInt(nextInt(3) + 1)}
 * sugar cane as far as {@code BlockReed.canBlockStay} allows.
 */
public class WorldGenReed extends WorldGenerator {

    private static final BlockState SUGAR_CANE = Blocks.SUGAR_CANE.defaultBlockState();

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        for (int i = 0; i < 20; ++i) {
            final int bx = x + rand.nextInt(4) - rand.nextInt(4);
            final int by = y;
            final int bz = z + rand.nextInt(4) - rand.nextInt(4);
            if (world.isAirBlock(bx, by, bz)
                    && (LegacyWorld.isWater(world.getBlock(bx - 1, by - 1, bz))
                    || LegacyWorld.isWater(world.getBlock(bx + 1, by - 1, bz))
                    || LegacyWorld.isWater(world.getBlock(bx, by - 1, bz - 1))
                    || LegacyWorld.isWater(world.getBlock(bx, by - 1, bz + 1)))) {
                final int height = 2 + rand.nextInt(rand.nextInt(3) + 1);
                for (int h = 0; h < height; ++h) {
                    if (world.canReedStay(bx, by + h, bz)) {
                        world.setBlock(bx, by + h, bz, SUGAR_CANE);
                    }
                }
            }
        }
        return true;
    }
}
