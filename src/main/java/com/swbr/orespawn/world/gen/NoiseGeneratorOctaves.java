package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.util.Mth;

/**
 * Port of the 1.7.10 vanilla {@code NoiseGeneratorOctaves} ({@code awk}, client-1.7.10.jar). Sums
 * {@link NoiseGeneratorImproved} octaves with halving frequency; x and z offsets are wrapped at
 * {@code 16777216} exactly as the bytecode does (the 1.21.1 {@code PerlinNoise.wrap} wraps at 2^25 around
 * the rounded value instead, which is why the modern class is not used).
 */
public class NoiseGeneratorOctaves {

    private final NoiseGeneratorImproved[] generatorCollection;
    private final int octaves;

    /** {@code awk(Random, int)}: one {@link NoiseGeneratorImproved} per octave, in order. */
    public NoiseGeneratorOctaves(final Random rand, final int octaves) {
        this.octaves = octaves;
        this.generatorCollection = new NoiseGeneratorImproved[octaves];
        for (int i = 0; i < octaves; ++i) {
            this.generatorCollection[i] = new NoiseGeneratorImproved(rand);
        }
    }

    /**
     * {@code awk.a([DIIIIIIDDD)} ({@code func_76304_a}). A {@code null} array is allocated with
     * {@code xSize * ySize * zSize} entries, anything else is zeroed first (the length is not checked,
     * as in the original).
     */
    public double[] generateNoiseOctaves(double[] noiseArray, final int xOffset, final int yOffset, final int zOffset,
                                         final int xSize, final int ySize, final int zSize,
                                         final double xScale, final double yScale, final double zScale) {
        if (noiseArray == null) {
            noiseArray = new double[xSize * ySize * zSize];
        } else {
            for (int i = 0; i < noiseArray.length; ++i) {
                noiseArray[i] = 0.0;
            }
        }
        double d3 = 1.0;
        for (int i = 0; i < this.octaves; ++i) {
            double d0 = (double) xOffset * d3 * xScale;
            final double d1 = (double) yOffset * d3 * yScale;
            double d2 = (double) zOffset * d3 * zScale;
            long k = Mth.lfloor(d0);
            long l = Mth.lfloor(d2);
            d0 -= (double) k;
            d2 -= (double) l;
            k %= 16777216L;
            l %= 16777216L;
            d0 += (double) k;
            d2 += (double) l;
            this.generatorCollection[i].populateNoiseArray(noiseArray, d0, d1, d2, xSize, ySize, zSize,
                    xScale * d3, yScale * d3, zScale * d3, d3);
            d3 /= 2.0;
        }
        return noiseArray;
    }

    /**
     * {@code awk.a([DIIIIDDD)} ({@code func_76305_a}): the 2D form, forwarded with {@code yOffset = 10},
     * {@code ySize = 1} and {@code yScale = 1.0}. The last argument was never read by the original.
     */
    public double[] generateNoiseOctaves(final double[] noiseArray, final int xOffset, final int zOffset,
                                         final int xSize, final int zSize, final double xScale, final double zScale,
                                         final double unused) {
        return this.generateNoiseOctaves(noiseArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1.0, zScale);
    }
}
