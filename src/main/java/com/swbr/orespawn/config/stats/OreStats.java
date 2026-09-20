package com.swbr.orespawn.config.stats;

import com.swbr.orespawn.config.OreSpawnConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Port of {@code OreStats} (OreStats.java:5-8) together with the reader
 * {@code OreSpawnMain.get_orestats} (OreSpawnMain.java:5770-5802), category OreSpawnORES.
 *
 * <p>Read at placement time by the config-driven placement modifiers of W12 (DECISIONS R12), so
 * the runtime form is the normal one. Depths are the absolute 1.7.10 values (R18: Y-Baender).
 */
public record OreStats(int rate, int clumpsize, int mindepth, int maxdepth) {

    private static final OreSpawnConfig.Ores C = OreSpawnConfig.ORES;

    /**
     * {@code get_orestats}: rate clamped to [default/2, default*2]; clumpsize the same and then at
     * least 1; mindepth and maxdepth at least 0; if {@code maxdepth - mindepth < 10} both depths are
     * reset to their defaults (OreSpawnMain.java:5772-5800).
     */
    public static OreStats read(StatSource source, ModConfigSpec.IntValue rateValue,
            ModConfigSpec.IntValue clumpValue, ModConfigSpec.IntValue minValue, ModConfigSpec.IntValue maxValue) {
        int rate = rateValue.getDefault();
        int r = source.read(rateValue);
        if (r < rate / 2) {
            r = rate / 2;
        }
        if (r > rate * 2) {
            r = rate * 2;
        }
        int clumpsize = clumpValue.getDefault();
        int c = source.read(clumpValue);
        if (c < clumpsize / 2) {
            c = clumpsize / 2;
        }
        if (c > clumpsize * 2) {
            c = clumpsize * 2;
        }
        if (c < 1) {
            c = 1;
        }
        int min = minValue.getDefault();
        int mindepth = source.read(minValue);
        if (mindepth < 0) {
            mindepth = 0;
        }
        int max = maxValue.getDefault();
        int maxdepth = source.read(maxValue);
        if (maxdepth < 0) {
            maxdepth = 0;
        }
        if (maxdepth - mindepth < 10) {
            mindepth = min;
            maxdepth = max;
        }
        return new OreStats(r, c, mindepth, maxdepth);
    }

    /** {@code OreSpawnMain.Ruby_stats}: prefix {@code Ruby} (OreSpawnMain.java:1244). */
    public static OreStats Ruby_stats(StatSource source) {
        return read(source,
                C.Ruby_rate,
                C.Ruby_clumpsize,
                C.Ruby_mindepth,
                C.Ruby_maxdepth);
    }

    /** Runtime form of {@link #Ruby_stats(StatSource)}. */
    public static OreStats Ruby_stats() {
        return Ruby_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.BlkRuby_stats}: prefix {@code BlockRuby} (OreSpawnMain.java:1245). */
    public static OreStats BlkRuby_stats(StatSource source) {
        return read(source,
                C.BlockRuby_rate,
                C.BlockRuby_clumpsize,
                C.BlockRuby_mindepth,
                C.BlockRuby_maxdepth);
    }

    /** Runtime form of {@link #BlkRuby_stats(StatSource)}. */
    public static OreStats BlkRuby_stats() {
        return BlkRuby_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Uranium_stats}: prefix {@code Uranium} (OreSpawnMain.java:1246). */
    public static OreStats Uranium_stats(StatSource source) {
        return read(source,
                C.Uranium_rate,
                C.Uranium_clumpsize,
                C.Uranium_mindepth,
                C.Uranium_maxdepth);
    }

    /** Runtime form of {@link #Uranium_stats(StatSource)}. */
    public static OreStats Uranium_stats() {
        return Uranium_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Titanium_stats}: prefix {@code Titanium} (OreSpawnMain.java:1247). */
    public static OreStats Titanium_stats(StatSource source) {
        return read(source,
                C.Titanium_rate,
                C.Titanium_clumpsize,
                C.Titanium_mindepth,
                C.Titanium_maxdepth);
    }

    /** Runtime form of {@link #Titanium_stats(StatSource)}. */
    public static OreStats Titanium_stats() {
        return Titanium_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Amethyst_stats}: prefix {@code Amethyst} (OreSpawnMain.java:1248). */
    public static OreStats Amethyst_stats(StatSource source) {
        return read(source,
                C.Amethyst_rate,
                C.Amethyst_clumpsize,
                C.Amethyst_mindepth,
                C.Amethyst_maxdepth);
    }

    /** Runtime form of {@link #Amethyst_stats(StatSource)}. */
    public static OreStats Amethyst_stats() {
        return Amethyst_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Salt_stats}: prefix {@code Salt} (OreSpawnMain.java:1249). */
    public static OreStats Salt_stats(StatSource source) {
        return read(source,
                C.Salt_rate,
                C.Salt_clumpsize,
                C.Salt_mindepth,
                C.Salt_maxdepth);
    }

    /** Runtime form of {@link #Salt_stats(StatSource)}. */
    public static OreStats Salt_stats() {
        return Salt_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.SpawnOres_stats}: prefix {@code SpawnOres} (OreSpawnMain.java:1250). */
    public static OreStats SpawnOres_stats(StatSource source) {
        return read(source,
                C.SpawnOres_rate,
                C.SpawnOres_clumpsize,
                C.SpawnOres_mindepth,
                C.SpawnOres_maxdepth);
    }

    /** Runtime form of {@link #SpawnOres_stats(StatSource)}. */
    public static OreStats SpawnOres_stats() {
        return SpawnOres_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Diamond_stats}: prefix {@code Diamond} (OreSpawnMain.java:1251). */
    public static OreStats Diamond_stats(StatSource source) {
        return read(source,
                C.Diamond_rate,
                C.Diamond_clumpsize,
                C.Diamond_mindepth,
                C.Diamond_maxdepth);
    }

    /** Runtime form of {@link #Diamond_stats(StatSource)}. */
    public static OreStats Diamond_stats() {
        return Diamond_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.BlkDiamond_stats}: prefix {@code BlockDiamond} (OreSpawnMain.java:1252). */
    public static OreStats BlkDiamond_stats(StatSource source) {
        return read(source,
                C.BlockDiamond_rate,
                C.BlockDiamond_clumpsize,
                C.BlockDiamond_mindepth,
                C.BlockDiamond_maxdepth);
    }

    /** Runtime form of {@link #BlkDiamond_stats(StatSource)}. */
    public static OreStats BlkDiamond_stats() {
        return BlkDiamond_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Emerald_stats}: prefix {@code Emerald} (OreSpawnMain.java:1253). */
    public static OreStats Emerald_stats(StatSource source) {
        return read(source,
                C.Emerald_rate,
                C.Emerald_clumpsize,
                C.Emerald_mindepth,
                C.Emerald_maxdepth);
    }

    /** Runtime form of {@link #Emerald_stats(StatSource)}. */
    public static OreStats Emerald_stats() {
        return Emerald_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.BlkEmerald_stats}: prefix {@code BlockEmerald} (OreSpawnMain.java:1254). */
    public static OreStats BlkEmerald_stats(StatSource source) {
        return read(source,
                C.BlockEmerald_rate,
                C.BlockEmerald_clumpsize,
                C.BlockEmerald_mindepth,
                C.BlockEmerald_maxdepth);
    }

    /** Runtime form of {@link #BlkEmerald_stats(StatSource)}. */
    public static OreStats BlkEmerald_stats() {
        return BlkEmerald_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Gold_stats}: prefix {@code Gold} (OreSpawnMain.java:1255). */
    public static OreStats Gold_stats(StatSource source) {
        return read(source,
                C.Gold_rate,
                C.Gold_clumpsize,
                C.Gold_mindepth,
                C.Gold_maxdepth);
    }

    /** Runtime form of {@link #Gold_stats(StatSource)}. */
    public static OreStats Gold_stats() {
        return Gold_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.BlkGold_stats}: prefix {@code BlockGold} (OreSpawnMain.java:1256). */
    public static OreStats BlkGold_stats(StatSource source) {
        return read(source,
                C.BlockGold_rate,
                C.BlockGold_clumpsize,
                C.BlockGold_mindepth,
                C.BlockGold_maxdepth);
    }

    /** Runtime form of {@link #BlkGold_stats(StatSource)}. */
    public static OreStats BlkGold_stats() {
        return BlkGold_stats(StatSource.RUNTIME);
    }
}
