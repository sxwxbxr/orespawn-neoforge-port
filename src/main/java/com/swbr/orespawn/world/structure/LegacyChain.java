package com.swbr.orespawn.world.structure;

import java.util.Random;

/**
 * The replacement of {@code OreSpawnWorld.recently_placed} (DECISIONS R18, "recently_placed-Abstandszähler"):
 * turns the per-chunk chances of an original placement branch into one decision per structure-set cell.
 *
 * <p><b>What the original did.</b> Every {@code generate} call in any dimension counted a static field down by one;
 * a big build set it to 50 (King/Queen altar: 100), and the branches below only rolled while it was 0. With
 * a per-chunk chance {@code P} that some build of a branch happens, the expected number of generated chunks between
 * two builds is {@code B + 1/P} ({@code B} = 50 or 100) - as long as chunks are generated one after another in one
 * place, which is how a single player explored.
 *
 * <p><b>What the port does.</b> A structure set with {@code random_spread} asks one chunk per
 * {@code spacing x spacing} cell. That chunk passes the gate with probability {@code cell / (B + 1/P)}, so the
 * expected density is again one build per {@code B + 1/P} chunks. Which build it is follows the conditional
 * distribution of the original rolls: entry {@code k} of a chain of independent rolls ({@code if (!addA() && !addB())})
 * with chances {@code c_k} is chosen with {@code c_k * prod_{j<k}(1 - c_j) / P}. Sets that never read the counter use
 * {@code spacing 1} and {@code B = 0}: the gate is then exactly {@code P} and the whole thing is the original roll.
 *
 * <p>Not reproducible and therefore not reproduced: the order dependence on chunk generation (a parallel generator
 * has no "next 50 chunks"), the counter's leak across dimensions, and the fallthrough of a chain whose chosen build
 * finds no surface (the original then let the next entry roll; here the cell stays empty).
 */
public final class LegacyChain {

    private LegacyChain() {
    }

    /**
     * Chances of a group of builds that exclude each other (a {@code nextInt(n)} selection followed by each build's
     * own roll), converted to the chances of an equivalent chain of independent rolls: the {@code k}-th becomes
     * {@code p_k / (1 - sum_{j<k} p_j)}, so that {@link #pick} gives each exactly its {@code p_k}.
     */
    public static double[] exclusive(final double... p) {
        final double[] c = new double[p.length];
        double taken = 0.0;
        for (int k = 0; k < p.length; ++k) {
            final double left = 1.0 - taken;
            c[k] = left <= 0.0 ? 0.0 : Math.min(1.0, p[k] / left);
            taken += p[k];
        }
        return c;
    }

    /** Concatenation of chance arrays, in call order. */
    public static double[] then(final double[] first, final double... next) {
        final double[] all = new double[first.length + next.length];
        System.arraycopy(first, 0, all, 0, first.length);
        System.arraycopy(next, 0, all, first.length, next.length);
        return all;
    }

    /**
     * One cell's decision.
     *
     * @param chances       chain chances in the original call order, 0 for a build that is not eligible here
     *                      (biome, config switch)
     * @param cellArea      {@code spacing * spacing} of the structure set
     * @param blockedChunks the {@code recently_placed} value the builds of this branch set (50, 100), 0 for none
     * @return index of the chosen build, or -1
     */
    public static int pick(final Random random, final double[] chances, final int cellArea, final int blockedChunks) {
        double none = 1.0;
        for (final double c : chances) {
            none *= 1.0 - c;
        }
        final double p = 1.0 - none;
        if (p <= 0.0) {
            return -1;
        }
        final double gate = Math.min(1.0, cellArea / (blockedChunks + 1.0 / p));
        if (random.nextDouble() >= gate) {
            return -1;
        }
        double u = random.nextDouble() * p;
        double reach = 1.0;
        for (int k = 0; k < chances.length; ++k) {
            final double pk = chances[k] * reach;
            if (u < pk) {
                return k;
            }
            u -= pk;
            reach *= 1.0 - chances[k];
        }
        for (int k = chances.length - 1; k >= 0; --k) {
            if (chances[k] > 0.0) {
                return k;
            }
        }
        return -1;
    }

    /** {@code 1.0 / n}: the chance of {@code random.nextInt(n) == k}. */
    public static double oneIn(final int n) {
        return n <= 0 ? 0.0 : 1.0 / n;
    }
}
