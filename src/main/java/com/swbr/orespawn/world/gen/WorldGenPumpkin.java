package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenPumpkin} ({@code asg}): 64 tries in a spread of 8/4/8, air on
 * grass.
 *
 * <p>PORT: the 1.7.10 pumpkin carried its face direction as metadata {@code nextInt(4)}; the 1.21.1
 * {@code minecraft:pumpkin} has no facing (only the carved pumpkin has, and that one would drop the wrong
 * item). The random number is still drawn so the rest of the decorator keeps its sequence.
 */
public class WorldGenPumpkin extends WorldGenerator {

    private static final BlockState PUMPKIN = Blocks.PUMPKIN.defaultBlockState();

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        for (int i = 0; i < 64; ++i) {
            final int bx = x + rand.nextInt(8) - rand.nextInt(8);
            final int by = y + rand.nextInt(4) - rand.nextInt(4);
            final int bz = z + rand.nextInt(8) - rand.nextInt(8);
            if (world.isAirBlock(bx, by, bz) && world.getBlock(bx, by - 1, bz).is(Blocks.GRASS_BLOCK)
                    && world.canPumpkinPlace(bx, by, bz)) {
                rand.nextInt(4);
                world.setBlock(bx, by, bz, PUMPKIN);
            }
        }
        return true;
    }
}
