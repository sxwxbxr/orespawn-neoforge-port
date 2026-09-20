package com.swbr.orespawn.world.gen;

import java.util.Random;

/**
 * Port of the 1.7.10 vanilla {@code NoiseGeneratorSimplex} ({@code awo}, client-1.7.10.jar), the octave
 * type of {@link NoiseGeneratorPerlin}. Line by line from the bytecode, including {@code fastFloor}
 * returning {@code d - 1} for exact non-positive integers.
 */
public class NoiseGeneratorSimplex {

    /** {@code awo.e}: the twelve 3D gradient vectors, of which only x and y are read. */
    private static final int[][] GRAD3 = {
            {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
            {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
            {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};

    /** {@code awo.a}. */
    public static final double SQRT_3 = Math.sqrt(3.0);
    /** {@code awo.g}: the skew factor F2. */
    private static final double F2 = 0.5 * (SQRT_3 - 1.0);
    /** {@code awo.h}: the unskew factor G2. */
    private static final double G2 = (3.0 - SQRT_3) / 6.0;

    private final int[] permutations = new int[512];
    public final double xo;
    public final double yo;
    public final double zo;

    /** {@code awo(Random)}: same draw order as {@link NoiseGeneratorImproved}. */
    public NoiseGeneratorSimplex(final Random rand) {
        this.xo = rand.nextDouble() * 256.0;
        this.yo = rand.nextDouble() * 256.0;
        this.zo = rand.nextDouble() * 256.0;
        for (int i = 0; i < 256; ++i) {
            this.permutations[i] = i;
        }
        for (int i = 0; i < 256; ++i) {
            final int j = rand.nextInt(256 - i) + i;
            final int k = this.permutations[i];
            this.permutations[i] = this.permutations[j];
            this.permutations[j] = k;
            this.permutations[i + 256] = this.permutations[i];
        }
    }

    /** {@code awo.a(D)}. */
    private static int fastFloor(final double value) {
        return value > 0.0 ? (int) value : (int) value - 1;
    }

    /** {@code awo.a([IDD)}. */
    private static double dot(final int[] grad, final double x, final double y) {
        return (double) grad[0] * x + (double) grad[1] * y;
    }

    /** {@code awo.a(DD)} ({@code func_151605_a}): a single 2D sample. */
    public double getValue(final double x, final double y) {
        final double d11 = 0.5 * (SQRT_3 - 1.0);
        final double d13 = (x + y) * d11;
        final int i15 = fastFloor(x + d13);
        final int i16 = fastFloor(y + d13);
        final double d17 = (3.0 - SQRT_3) / 6.0;
        final double d19 = (double) (i15 + i16) * d17;
        final double d21 = (double) i15 - d19;
        final double d23 = (double) i16 - d19;
        final double d25 = x - d21;
        final double d27 = y - d23;
        final int i29;
        final int i30;
        if (d25 > d27) {
            i29 = 1;
            i30 = 0;
        } else {
            i29 = 0;
            i30 = 1;
        }
        final double d31 = d25 - (double) i29 + d17;
        final double d33 = d27 - (double) i30 + d17;
        final double d35 = d25 - 1.0 + 2.0 * d17;
        final double d37 = d27 - 1.0 + 2.0 * d17;
        final int i39 = i15 & 255;
        final int i40 = i16 & 255;
        final int i41 = this.permutations[i39 + this.permutations[i40]] % 12;
        final int i42 = this.permutations[i39 + i29 + this.permutations[i40 + i30]] % 12;
        final int i43 = this.permutations[i39 + 1 + this.permutations[i40 + 1]] % 12;
        double d44 = 0.5 - d25 * d25 - d27 * d27;
        final double d5;
        if (d44 < 0.0) {
            d5 = 0.0;
        } else {
            d44 *= d44;
            d5 = d44 * d44 * dot(GRAD3[i41], d25, d27);
        }
        double d46 = 0.5 - d31 * d31 - d33 * d33;
        final double d7;
        if (d46 < 0.0) {
            d7 = 0.0;
        } else {
            d46 *= d46;
            d7 = d46 * d46 * dot(GRAD3[i42], d31, d33);
        }
        double d48 = 0.5 - d35 * d35 - d37 * d37;
        final double d9;
        if (d48 < 0.0) {
            d9 = 0.0;
        } else {
            d48 *= d48;
            d9 = d48 * d48 * dot(GRAD3[i43], d35, d37);
        }
        return 70.0 * (d5 + d7 + d9);
    }

    /**
     * {@code awo.a([DDDIIDDD)} ({@code func_151606_a}): adds {@code 70 * noise * amplitude} for a
     * {@code xSize} by {@code zSize} region into {@code noiseArray}, z as the outer loop, so the index is
     * {@code z * xSize + x}.
     */
    public void add(final double[] noiseArray, final double xOffset, final double zOffset, final int xSize,
                    final int zSize, final double xScale, final double zScale, final double amplitude) {
        int index = 0;
        for (int j = 0; j < zSize; ++j) {
            final double d16 = (zOffset + (double) j) * zScale + this.yo;
            for (int k = 0; k < xSize; ++k) {
                final double d19 = (xOffset + (double) k) * xScale + this.xo;
                final double d27 = (d19 + d16) * F2;
                final int i29 = fastFloor(d19 + d27);
                final int i30 = fastFloor(d16 + d27);
                final double d31 = (double) (i29 + i30) * G2;
                final double d33 = (double) i29 - d31;
                final double d35 = (double) i30 - d31;
                final double d37 = d19 - d33;
                final double d39 = d16 - d35;
                final int i41;
                final int i42;
                if (d37 > d39) {
                    i41 = 1;
                    i42 = 0;
                } else {
                    i41 = 0;
                    i42 = 1;
                }
                final double d43 = d37 - (double) i41 + G2;
                final double d45 = d39 - (double) i42 + G2;
                final double d47 = d37 - 1.0 + 2.0 * G2;
                final double d49 = d39 - 1.0 + 2.0 * G2;
                final int i51 = i29 & 255;
                final int i52 = i30 & 255;
                final int i53 = this.permutations[i51 + this.permutations[i52]] % 12;
                final int i54 = this.permutations[i51 + i41 + this.permutations[i52 + i42]] % 12;
                final int i55 = this.permutations[i51 + 1 + this.permutations[i52 + 1]] % 12;
                double d56 = 0.5 - d37 * d37 - d39 * d39;
                final double d21;
                if (d56 < 0.0) {
                    d21 = 0.0;
                } else {
                    d56 *= d56;
                    d21 = d56 * d56 * dot(GRAD3[i53], d37, d39);
                }
                double d58 = 0.5 - d43 * d43 - d45 * d45;
                final double d23;
                if (d58 < 0.0) {
                    d23 = 0.0;
                } else {
                    d58 *= d58;
                    d23 = d58 * d58 * dot(GRAD3[i54], d43, d45);
                }
                double d60 = 0.5 - d47 * d47 - d49 * d49;
                final double d25;
                if (d60 < 0.0) {
                    d25 = 0.0;
                } else {
                    d60 *= d60;
                    d25 = d60 * d60 * dot(GRAD3[i55], d47, d49);
                }
                noiseArray[index++] += 70.0 * (d21 + d23 + d25) * amplitude;
            }
        }
    }
}
