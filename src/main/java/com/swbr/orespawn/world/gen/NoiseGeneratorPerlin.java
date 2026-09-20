package com.swbr.orespawn.world.gen;

import java.util.Random;

/**
 * Port of the 1.7.10 vanilla {@code NoiseGeneratorPerlin} ({@code awl}, client-1.7.10.jar): octaves of
 * {@link NoiseGeneratorSimplex}. The noise-terrain generators use it for the surface depth noise
 * ({@code stoneNoise}), {@code BiomeGenBase} for its height-dependent temperature.
 */
public class NoiseGeneratorPerlin {

    private final NoiseGeneratorSimplex[] noiseLevels;
    private final int levels;

    /** {@code awl(Random, int)}. */
    public NoiseGeneratorPerlin(final Random rand, final int levels) {
        this.levels = levels;
        this.noiseLevels = new NoiseGeneratorSimplex[levels];
        for (int i = 0; i < levels; ++i) {
            this.noiseLevels[i] = new NoiseGeneratorSimplex(rand);
        }
    }

    /** {@code awl.a(DD)} ({@code func_151601_a}). */
    public double getValue(final double x, final double z) {
        double value = 0.0;
        double scale = 1.0;
        for (int i = 0; i < this.levels; ++i) {
            value += this.noiseLevels[i].getValue(x * scale, z * scale) / scale;
            scale /= 2.0;
        }
        return value;
    }

    /** {@code awl.a([DDDIIDDD)} ({@code func_151599_a}): persistence 0.5. */
    public double[] getRegion(final double[] noiseArray, final double x, final double z, final int xSize,
                              final int zSize, final double xScale, final double zScale, final double lacunarity) {
        return this.getRegion(noiseArray, x, z, xSize, zSize, xScale, zScale, lacunarity, 0.5);
    }

    /**
     * {@code awl.a([DDDIIDDDD)} ({@code func_151600_a}). The result is indexed {@code z * xSize + x};
     * callers of 1.7.10 read it as {@code z + x * 16}, and that transposition is original behaviour.
     */
    public double[] getRegion(double[] noiseArray, final double x, final double z, final int xSize, final int zSize,
                              final double xScale, final double zScale, final double lacunarity,
                              final double persistence) {
        if (noiseArray != null && noiseArray.length >= xSize * zSize) {
            for (int i = 0; i < noiseArray.length; ++i) {
                noiseArray[i] = 0.0;
            }
        } else {
            noiseArray = new double[xSize * zSize];
        }
        double d16 = 1.0;
        double d18 = 1.0;
        for (int i = 0; i < this.levels; ++i) {
            this.noiseLevels[i].add(noiseArray, x, z, xSize, zSize, xScale * d18 * d16, zScale * d18 * d16,
                    0.55 / d16);
            d18 *= lacunarity;
            d16 *= persistence;
        }
        return noiseArray;
    }
}
