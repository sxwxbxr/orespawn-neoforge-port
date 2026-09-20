package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code MapGenBase} ({@code aqx}, client-1.7.10.jar): the seeded 17 by 17
 * chunk scan that lets caves and ravines started in neighbouring chunks reach into the chunk being
 * generated. Subclasses carve only into the {@code blocks} array of that one chunk, so the result is
 * chunk-local and deterministic, as in 1.7.10.
 *
 * <p>The block array uses the 1.7.10 layout {@code (x * 16 + z) * 256 + y} with {@code null} for air
 * (client-1.7.10.jar {@code apx.<init>(ahb, aji[], byte[], int, int)}: {@code x * k * 16 | z * k | y}).
 *
 * <p>Not thread-safe: {@link #rand} is instance state, exactly like the original field. Use one instance
 * per chunk.
 */
public class MapGenBase {

    /** {@code aqx.a}: the radius in chunks that is scanned, 8. */
    protected int range = 8;

    /** {@code aqx.b}. */
    protected Random rand = new Random();

    /**
     * PORT: stands in for {@code worldObj.getBiomeGenForCoords(x, z).topBlock} ({@code aqx.c}, read by the
     * cave and ravine carvers to restore grass). Every OreSpawn dimension has a single biome, so the value
     * is the same for every column; the caller passes it in instead of a world.
     */
    protected BlockState biomeTopBlock = Blocks.GRASS_BLOCK.defaultBlockState();

    /**
     * {@code aqx.a(IChunkProvider, World, int, int, Block[])} ({@code func_151539_a}).
     *
     * @param worldSeed the world seed ({@code ahb.H()})
     * @param chunkX chunk being generated
     * @param chunkZ chunk being generated
     * @param blocks its block array, carved in place
     * @param biomeTopBlock the biome's top block at the time the original read it
     */
    public void generate(final long worldSeed, final int chunkX, final int chunkZ, final BlockState[] blocks,
                         final BlockState biomeTopBlock) {
        final int k = this.range;
        this.biomeTopBlock = biomeTopBlock;
        this.rand.setSeed(worldSeed);
        final long l = this.rand.nextLong();
        final long i1 = this.rand.nextLong();
        for (int j1 = chunkX - k; j1 <= chunkX + k; ++j1) {
            for (int k1 = chunkZ - k; k1 <= chunkZ + k; ++k1) {
                final long l1 = (long) j1 * l;
                final long i2 = (long) k1 * i1;
                this.rand.setSeed(l1 ^ i2 ^ worldSeed);
                this.recursiveGenerate(j1, k1, chunkX, chunkZ, blocks);
            }
        }
    }

    /** {@code aqx.a(World, int, int, int, int, Block[])} ({@code func_151538_a}); empty in the base class. */
    protected void recursiveGenerate(final int chunkX, final int chunkZ, final int originalChunkX,
                                     final int originalChunkZ, final BlockState[] blocks) {
    }
}
