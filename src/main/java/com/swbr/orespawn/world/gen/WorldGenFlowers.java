package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenFlowers} ({@code aro}): 64 tries in a spread of 8/4/8. The
 * decorator uses one instance for flowers and two for mushrooms; the stay rule follows the block class
 * ({@code BlockBush} for flowers, {@code BlockMushroom} for mushrooms).
 */
public class WorldGenFlowers extends WorldGenerator {

    private BlockState flower;

    /** {@code aro(Block)}. */
    public WorldGenFlowers(final BlockState flower) {
        this.flower = flower;
    }

    /** {@code aro.a(Block, int)} ({@code func_150550_a}): the flower block and its metadata. */
    public void setFlower(final BlockState flower) {
        this.flower = flower;
    }

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        for (int i = 0; i < 64; ++i) {
            final int bx = x + rand.nextInt(8) - rand.nextInt(8);
            final int by = y + rand.nextInt(4) - rand.nextInt(4);
            final int bz = z + rand.nextInt(8) - rand.nextInt(8);
            // (!world.provider.hasNoSky || by < 255): every decorated OreSpawn dimension has a sky, so the second test never runs.
            if (world.isAirBlock(bx, by, bz) && this.canBlockStay(world, bx, by, bz)) {
                world.setBlock(bx, by, bz, this.flower);
            }
        }
        return true;
    }

    private boolean canBlockStay(final LegacyWorld world, final int x, final int y, final int z) {
        if (this.flower.is(Blocks.BROWN_MUSHROOM) || this.flower.is(Blocks.RED_MUSHROOM)) {
            return world.canMushroomStay(x, y, z);
        }
        return world.canBushStay(x, y, z);
    }
}
