package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenTallGrass} ({@code asp}): walks down through air and leaves,
 * then 128 tries in a spread of 8/4/8. {@code BlockTallGrass.canBlockStay} ({@code anz.j}) is the
 * {@code BlockBush} rule (grass, dirt or farmland below).
 */
public class WorldGenTallGrass extends WorldGenerator {

    private final BlockState grass;

    /** {@code asp(Block, int)}: {@code tallgrass} with metadata 1 is 1.21.1 {@code short_grass}. */
    public WorldGenTallGrass(final BlockState grass) {
        this.grass = grass;
    }

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, int y, final int z) {
        BlockState block;
        while (((block = world.getBlock(x, y, z)).isAir() || LegacyWorld.isLeaves(block)) && y > 0) {
            --y;
        }
        for (int i = 0; i < 128; ++i) {
            final int bx = x + rand.nextInt(8) - rand.nextInt(8);
            final int by = y + rand.nextInt(4) - rand.nextInt(4);
            final int bz = z + rand.nextInt(8) - rand.nextInt(8);
            if (world.isAirBlock(bx, by, bz) && world.canBushStay(bx, by, bz)) {
                world.setBlock(bx, by, bz, this.grass);
            }
        }
        return true;
    }
}
