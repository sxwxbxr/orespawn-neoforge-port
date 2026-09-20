package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenLiquids} ({@code asm}): a water or lava spring in a stone wall -
 * stone above and below, the position air or stone, exactly three stone and one air neighbour on the sides.
 *
 * <p>PORT: the original then set {@code world.scheduledUpdatesAreImmediate} and called
 * {@code updateTick} on the flowing block, so the whole stream flowed out during generation. 1.21.1 has no
 * immediate-update switch; the source gets a fluid tick with delay 0 instead, as 1.21.1's own spring
 * feature does, and flows as soon as the chunk ticks. Any random numbers the immediate flow of lava would
 * have drawn are not drawn.
 */
public class WorldGenLiquids extends WorldGenerator {

    private final BlockState liquid;
    private final Fluid fluid;

    /** {@code asm(Block)}; the 1.7.10 flowing blocks become the 1.21.1 source block plus its fluid. */
    public WorldGenLiquids(final boolean lava) {
        this.liquid = lava ? Blocks.LAVA.defaultBlockState() : Blocks.WATER.defaultBlockState();
        this.fluid = lava ? Fluids.LAVA : Fluids.WATER;
    }

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        if (!world.getBlock(x, y + 1, z).is(Blocks.STONE)) {
            return false;
        }
        if (!world.getBlock(x, y - 1, z).is(Blocks.STONE)) {
            return false;
        }
        final BlockState here = world.getBlock(x, y, z);
        if (!here.isAir() && !here.is(Blocks.STONE)) {
            return false;
        }
        int stone = 0;
        if (world.getBlock(x - 1, y, z).is(Blocks.STONE)) {
            ++stone;
        }
        if (world.getBlock(x + 1, y, z).is(Blocks.STONE)) {
            ++stone;
        }
        if (world.getBlock(x, y, z - 1).is(Blocks.STONE)) {
            ++stone;
        }
        if (world.getBlock(x, y, z + 1).is(Blocks.STONE)) {
            ++stone;
        }
        int air = 0;
        if (world.isAirBlock(x - 1, y, z)) {
            ++air;
        }
        if (world.isAirBlock(x + 1, y, z)) {
            ++air;
        }
        if (world.isAirBlock(x, y, z - 1)) {
            ++air;
        }
        if (world.isAirBlock(x, y, z + 1)) {
            ++air;
        }
        if (stone == 3 && air == 1) {
            world.setBlock(x, y, z, this.liquid);
            world.scheduleFluidTick(x, y, z, this.fluid, 0);
        }
        return true;
    }
}
