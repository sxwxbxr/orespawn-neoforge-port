package com.swbr.orespawn.config.stats;

import com.swbr.orespawn.config.OreSpawnConfig;

/**
 * The OreSpawnTWEAKS and OreSpawnWEAPONS values that {@code OreSpawnMain.preInit} clamped or
 * coupled after reading (OreSpawnMain.java:1189-1243). Everything else in those categories is
 * used unclamped and is read straight from {@link OreSpawnConfig#TWEAKS} by its consumer.
 *
 * <p>The original clamped the static field but never wrote the clamped value back
 * ({@code config.save()} at :1257 saves the Property objects), so the file keeps the user's
 * number and the game runs on the clamped one. Reading through here reproduces exactly that.
 */
public final class TweakStats {

    private static final OreSpawnConfig.Tweaks T = OreSpawnConfig.TWEAKS;
    private static final OreSpawnConfig.Weapons W = OreSpawnConfig.WEAPONS;

    private TweakStats() {}

    /** {@code OreSpawnMain.UltimateSwordMagic}: key UltimateSwordEnchantmentLevel, clamped to [1, 10] (:1189-1196). */
    public static int UltimateSwordMagic() {
        int v = W.UltimateSwordEnchantmentLevel.get();
        if (v < 1) {
            v = 1;
        }
        if (v > 10) {
            v = 10;
        }
        return v;
    }

    /** {@code OreSpawnMain.UltimateBowDamage}: clamped to [2, 20] (:1190, :1197-1202). */
    public static int UltimateBowDamage() {
        int v = W.UltimateBowDamage.get();
        if (v < 2) {
            v = 2;
        }
        if (v > 20) {
            v = 20;
        }
        return v;
    }

    /** {@code OreSpawnMain.LessLag}: clamped to [0, 2] (:1142, :1221-1226). */
    public static int LessLag() {
        int v = T.LessLag.get();
        if (v < 0) {
            v = 0;
        }
        if (v > 2) {
            v = 2;
        }
        return v;
    }

    /** {@code OreSpawnMain.LessOre}: the config value, forced to 1 when LessLag is 2 (:1141, :1242). */
    public static int LessOre() {
        int v = T.LessOre.get();
        if (LessLag() == 2) {
            v = 1;
        }
        return v;
    }

    /**
     * {@code OreSpawnMain.IslandSpeedFactor}: clamped to [1, 5] (:1203-1208), then capped at 2 by
     * LessLag 1 and at 1 by LessLag 2 (:1227-1241).
     */
    public static int IslandSpeedFactor() {
        return lessLagCap(clamp(T.IslandSpeedFactor.get(), 1, 5));
    }

    /**
     * {@code OreSpawnMain.IslandSizeFactor}: clamped to [1, 5] (:1209-1214), then capped at 2 by
     * LessLag 1 and at 1 by LessLag 2 (:1227-1241).
     */
    public static int IslandSizeFactor() {
        return lessLagCap(clamp(T.IslandSizeFactor.get(), 1, 5));
    }

    /** {@code OreSpawnMain.NightmareSize}: clamped to [0, 5] (:1145, :1215-1220). */
    public static int NightmareSize() {
        return clamp(T.NightmareSize.get(), 0, 5);
    }

    private static int lessLagCap(int v) {
        int lessLag = LessLag();
        if (lessLag == 1 && v > 2) {
            v = 2;
        }
        if (lessLag == 2 && v > 1) {
            v = 1;
        }
        return v;
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) {
            v = min;
        }
        if (v > max) {
            v = max;
        }
        return v;
    }
}
