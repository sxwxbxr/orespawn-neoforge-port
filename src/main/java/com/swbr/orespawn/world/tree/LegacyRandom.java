package com.swbr.orespawn.world.tree;

import java.util.Random;
import net.minecraft.util.RandomSource;

/**
 * A {@link Random} that draws its bits from a 1.21.1 {@link RandomSource}. No original class.
 *
 * <p>The generators of this package (ported from {@code Trees}, {@code BasiliskMaze},
 * {@code RubyBirdDungeon}, {@code ItemMagicApple}) take a {@code java.util.Random}, as the originals
 * did ({@code world.rand}, {@code OreSpawnMain.OreSpawnRand}). The world generator hands them a seeded
 * {@code Random} ({@code LegacyStructurePiece}, {@link TreeBuilders}), so a given seed yields the 1.7.10 sequence of
 * {@code nextInt} values. Runtime callers - a block tick, an item use - only have
 * {@code level.random}; wrapping it keeps them on the level's random stream instead of creating a
 * second, unseeded one. Every {@code Random} method funnels through {@link #next(int)}.
 */
public final class LegacyRandom extends Random {

    private static final long serialVersionUID = 1L;

    private final transient RandomSource source;

    public LegacyRandom(final RandomSource source) {
        super(0L);
        this.source = source;
    }

    @Override
    protected int next(final int bits) {
        if (this.source == null) {
            // Only reachable from Random's own constructor (setSeed), before the field is assigned.
            return super.next(bits);
        }
        return (int) (this.source.nextLong() >>> (64 - bits));
    }
}
