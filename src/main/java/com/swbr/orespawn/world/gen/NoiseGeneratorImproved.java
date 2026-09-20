package com.swbr.orespawn.world.gen;

import java.util.Random;

/**
 * Port of the 1.7.10 vanilla {@code NoiseGeneratorImproved} ({@code awj} in client-1.7.10.jar, read with
 * {@code javap -c}). The four noise-terrain dimensions (Utopia, Mining, Village, Crystal) copy
 * {@code ChunkProviderGenerate}, and every one of their terrain shapes hangs off this class and its seed
 * consumption; 1.21.1's {@code ImprovedNoise} is a descendant with a different octave wrap and cannot stand
 * in for it (verhalten/world-03.md, "Portierung").
 *
 * <p>Deliberately a line-by-line copy of the bytecode, including the per-column y cache of the 3D branch
 * ({@code y == 0 || py != lastY}) and the 2D branch that ignores {@code yOff} and {@code yCoord}.
 * Instances are immutable after construction; the output array belongs to the caller, so one instance may be
 * shared across chunk-generation threads.
 */
public class NoiseGeneratorImproved {

    private final int[] permutations = new int[512];

    public final double xCoord;
    public final double yCoord;
    public final double zCoord;

    /** {@code awj.e/f/g}: x, y and z gradient factors of the 16 hashes. */
    private static final double[] GRAD_X = {1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
    private static final double[] GRAD_Y = {1.0, 1.0, -1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0};
    private static final double[] GRAD_Z = {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 0.0, 1.0, 0.0, -1.0};
    /** {@code awj.h/i}: x and z factors of the 2D gradient. */
    private static final double[] GRAD_2X = {1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0, 0.0};
    private static final double[] GRAD_2Z = {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 0.0, 1.0, 0.0, -1.0};

    /** {@code awj(Random)}: three coordinate offsets, then the shuffled permutation table. */
    public NoiseGeneratorImproved(final Random rand) {
        this.xCoord = rand.nextDouble() * 256.0;
        this.yCoord = rand.nextDouble() * 256.0;
        this.zCoord = rand.nextDouble() * 256.0;
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

    /** {@code awj.b(DDD)}: {@code a + t * (b - a)}. */
    public final double lerp(final double t, final double a, final double b) {
        return a + t * (b - a);
    }

    /** {@code awj.a(IDD)} ({@code func_76309_a}). */
    public final double grad2(final int hash, final double x, final double z) {
        final int j = hash & 15;
        return GRAD_2X[j] * x + GRAD_2Z[j] * z;
    }

    /** {@code awj.a(IDDD)} ({@code func_76310_a}). */
    public final double grad(final int hash, final double x, final double y, final double z) {
        final int j = hash & 15;
        return GRAD_X[j] * x + GRAD_Y[j] * y + GRAD_Z[j] * z;
    }

    /**
     * {@code awj.a([DDDDIIIDDDD)} ({@code func_76308_a}, "populateNoiseArray"). Adds this octave into
     * {@code noiseArray} in x, z, y order; {@code ySize == 1} takes the 2D branch.
     */
    public void populateNoiseArray(final double[] noiseArray, final double xOffset, final double yOffset,
                                   final double zOffset, final int xSize, final int ySize, final int zSize,
                                   final double xScale, final double yScale, final double zScale,
                                   final double noiseScale) {
        final int[] p = this.permutations;
        if (ySize == 1) {
            int i19;
            int i20;
            int i21;
            int i22;
            double d23;
            double d25;
            int index = 0;
            final double inv = 1.0 / noiseScale;
            for (int x = 0; x < xSize; ++x) {
                double dx = xOffset + (double) x * xScale + this.xCoord;
                int ix = (int) dx;
                if (dx < (double) ix) {
                    --ix;
                }
                final int px = ix & 255;
                dx -= (double) ix;
                final double fx = dx * dx * dx * (dx * (dx * 6.0 - 15.0) + 10.0);
                for (int z = 0; z < zSize; ++z) {
                    double dz = zOffset + (double) z * zScale + this.zCoord;
                    int iz = (int) dz;
                    if (dz < (double) iz) {
                        --iz;
                    }
                    final int pz = iz & 255;
                    dz -= (double) iz;
                    final double fz = dz * dz * dz * (dz * (dz * 6.0 - 15.0) + 10.0);
                    i19 = p[px] + 0;
                    i20 = p[i19] + pz;
                    i21 = p[px + 1] + 0;
                    i22 = p[i21] + pz;
                    d23 = this.lerp(fx, this.grad2(p[i20], dx, dz), this.grad(p[i22], dx - 1.0, 0.0, dz));
                    d25 = this.lerp(fx, this.grad(p[i20 + 1], dx, 0.0, dz - 1.0),
                            this.grad(p[i22 + 1], dx - 1.0, 0.0, dz - 1.0));
                    final double value = this.lerp(fz, d23, d25);
                    noiseArray[index++] += value * inv;
                }
            }
            return;
        }

        int index = 0;
        final double inv = 1.0 / noiseScale;
        int lastY = -1;
        int i23;
        int i24 = 0;
        int i25 = 0;
        int i26;
        int i27 = 0;
        int i28 = 0;
        double d29 = 0.0;
        double d31 = 0.0;
        double d33 = 0.0;
        double d35 = 0.0;
        for (int x = 0; x < xSize; ++x) {
            double dx = xOffset + (double) x * xScale + this.xCoord;
            int ix = (int) dx;
            if (dx < (double) ix) {
                --ix;
            }
            final int px = ix & 255;
            dx -= (double) ix;
            final double fx = dx * dx * dx * (dx * (dx * 6.0 - 15.0) + 10.0);
            for (int z = 0; z < zSize; ++z) {
                double dz = zOffset + (double) z * zScale + this.zCoord;
                int iz = (int) dz;
                if (dz < (double) iz) {
                    --iz;
                }
                final int pz = iz & 255;
                dz -= (double) iz;
                final double fz = dz * dz * dz * (dz * (dz * 6.0 - 15.0) + 10.0);
                for (int y = 0; y < ySize; ++y) {
                    double dy = yOffset + (double) y * yScale + this.yCoord;
                    int iy = (int) dy;
                    if (dy < (double) iy) {
                        --iy;
                    }
                    final int py = iy & 255;
                    dy -= (double) iy;
                    final double fy = dy * dy * dy * (dy * (dy * 6.0 - 15.0) + 10.0);
                    if (y == 0 || py != lastY) {
                        lastY = py;
                        i23 = p[px] + py;
                        i24 = p[i23] + pz;
                        i25 = p[i23 + 1] + pz;
                        i26 = p[px + 1] + py;
                        i27 = p[i26] + pz;
                        i28 = p[i26 + 1] + pz;
                        d29 = this.lerp(fx, this.grad(p[i24], dx, dy, dz), this.grad(p[i27], dx - 1.0, dy, dz));
                        d31 = this.lerp(fx, this.grad(p[i25], dx, dy - 1.0, dz),
                                this.grad(p[i28], dx - 1.0, dy - 1.0, dz));
                        d33 = this.lerp(fx, this.grad(p[i24 + 1], dx, dy, dz - 1.0),
                                this.grad(p[i27 + 1], dx - 1.0, dy, dz - 1.0));
                        d35 = this.lerp(fx, this.grad(p[i25 + 1], dx, dy - 1.0, dz - 1.0),
                                this.grad(p[i28 + 1], dx - 1.0, dy - 1.0, dz - 1.0));
                    }
                    final double d58 = this.lerp(fy, d29, d31);
                    final double d60 = this.lerp(fy, d33, d35);
                    final double d62 = this.lerp(fz, d58, d60);
                    noiseArray[index++] += d62 * inv;
                }
            }
        }
    }
}
