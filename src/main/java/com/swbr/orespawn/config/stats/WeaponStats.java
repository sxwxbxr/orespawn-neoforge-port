package com.swbr.orespawn.config.stats;

import com.swbr.orespawn.config.OreSpawnConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Port of {@code WeaponStats} (WeaponStats.java:5-9) together with the reader
 * {@code OreSpawnMain.get_weaponstats} (OreSpawnMain.java:5701-5736), category OreSpawnWEAPONS.
 *
 * <p>Tiers are built while items register, before the COMMON config is loaded; W03 reads them
 * with {@link StatSource#EARLY} (DECISIONS R3). {@code BerthaHit} reads {@code damage} of Bertha,
 * Royal and Attitude at runtime (BerthaHit.java:73, 84, 94) and uses the runtime form.
 *
 * <p>{@code damage} is the 1.7.10 material damage; DECISIONS R6 maps it to the tier's attack
 * damage bonus with a sword modifier of 3.
 */
public record WeaponStats(int harvestlevel, int maxuses, int efficiency, int damage, int enchantability) {

    private static final OreSpawnConfig.Weapons C = OreSpawnConfig.WEAPONS;

    /**
     * {@code get_weaponstats}: a harvest level below default-1 is reset to the default itself
     * (not to default-1) and has no upper bound; the other four are clamped to
     * [default/2, default*2] (OreSpawnMain.java:5703-5734).
     */
    public static WeaponStats read(StatSource source, ModConfigSpec.IntValue harvestValue,
            ModConfigSpec.IntValue maxUsesValue, ModConfigSpec.IntValue efficiencyValue,
            ModConfigSpec.IntValue damageValue, ModConfigSpec.IntValue enchantabilityValue) {
        int harvest = harvestValue.getDefault();
        int harvestlevel = source.read(harvestValue);
        if (harvestlevel < harvest - 1) {
            harvestlevel = harvest;
        }
        return new WeaponStats(harvestlevel,
                halfToDouble(source, maxUsesValue),
                halfToDouble(source, efficiencyValue),
                halfToDouble(source, damageValue),
                halfToDouble(source, enchantabilityValue));
    }

    /** Clamp to [default/2, default*2] (OreSpawnMain.java:5708-5734). */
    private static int halfToDouble(StatSource source, ModConfigSpec.IntValue value) {
        int def = value.getDefault();
        int v = source.read(value);
        if (v < def / 2) {
            v = def / 2;
        }
        if (v > def * 2) {
            v = def * 2;
        }
        return v;
    }

    /** {@code OreSpawnMain.ultimate_stats}: prefix {@code Ultimate} (OreSpawnMain.java:1174). */
    public static WeaponStats ultimate_stats(StatSource source) {
        return read(source,
                C.Ultimate_harvestlevel,
                C.Ultimate_maxuses,
                C.Ultimate_efficiency,
                C.Ultimate_damage,
                C.Ultimate_enchantability);
    }

    /** Runtime form of {@link #ultimate_stats(StatSource)}. */
    public static WeaponStats ultimate_stats() {
        return ultimate_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.nightmare_stats}: prefix {@code Nightmare} (OreSpawnMain.java:1175). */
    public static WeaponStats nightmare_stats(StatSource source) {
        return read(source,
                C.Nightmare_harvestlevel,
                C.Nightmare_maxuses,
                C.Nightmare_efficiency,
                C.Nightmare_damage,
                C.Nightmare_enchantability);
    }

    /** Runtime form of {@link #nightmare_stats(StatSource)}. */
    public static WeaponStats nightmare_stats() {
        return nightmare_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.bertha_stats}: prefix {@code Bertha} (OreSpawnMain.java:1176). */
    public static WeaponStats bertha_stats(StatSource source) {
        return read(source,
                C.Bertha_harvestlevel,
                C.Bertha_maxuses,
                C.Bertha_efficiency,
                C.Bertha_damage,
                C.Bertha_enchantability);
    }

    /** Runtime form of {@link #bertha_stats(StatSource)}. */
    public static WeaponStats bertha_stats() {
        return bertha_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.crystalwood_stats}: prefix {@code CrystalWood} (OreSpawnMain.java:1177). */
    public static WeaponStats crystalwood_stats(StatSource source) {
        return read(source,
                C.CrystalWood_harvestlevel,
                C.CrystalWood_maxuses,
                C.CrystalWood_efficiency,
                C.CrystalWood_damage,
                C.CrystalWood_enchantability);
    }

    /** Runtime form of {@link #crystalwood_stats(StatSource)}. */
    public static WeaponStats crystalwood_stats() {
        return crystalwood_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.crystalstone_stats}: prefix {@code CrystalStone} (OreSpawnMain.java:1178). */
    public static WeaponStats crystalstone_stats(StatSource source) {
        return read(source,
                C.CrystalStone_harvestlevel,
                C.CrystalStone_maxuses,
                C.CrystalStone_efficiency,
                C.CrystalStone_damage,
                C.CrystalStone_enchantability);
    }

    /** Runtime form of {@link #crystalstone_stats(StatSource)}. */
    public static WeaponStats crystalstone_stats() {
        return crystalstone_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.crystalpink_stats}: prefix {@code Pink} (OreSpawnMain.java:1179). */
    public static WeaponStats crystalpink_stats(StatSource source) {
        return read(source,
                C.Pink_harvestlevel,
                C.Pink_maxuses,
                C.Pink_efficiency,
                C.Pink_damage,
                C.Pink_enchantability);
    }

    /** Runtime form of {@link #crystalpink_stats(StatSource)}. */
    public static WeaponStats crystalpink_stats() {
        return crystalpink_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.tigerseye_stats}: prefix {@code TigersEye} (OreSpawnMain.java:1180). */
    public static WeaponStats tigerseye_stats(StatSource source) {
        return read(source,
                C.TigersEye_harvestlevel,
                C.TigersEye_maxuses,
                C.TigersEye_efficiency,
                C.TigersEye_damage,
                C.TigersEye_enchantability);
    }

    /** Runtime form of {@link #tigerseye_stats(StatSource)}. */
    public static WeaponStats tigerseye_stats() {
        return tigerseye_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.ruby_stats}: prefix {@code Ruby} (OreSpawnMain.java:1181). */
    public static WeaponStats ruby_stats(StatSource source) {
        return read(source,
                C.Ruby_harvestlevel,
                C.Ruby_maxuses,
                C.Ruby_efficiency,
                C.Ruby_damage,
                C.Ruby_enchantability);
    }

    /** Runtime form of {@link #ruby_stats(StatSource)}. */
    public static WeaponStats ruby_stats() {
        return ruby_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.amethyst_stats}: prefix {@code Amethyst} (OreSpawnMain.java:1182). */
    public static WeaponStats amethyst_stats(StatSource source) {
        return read(source,
                C.Amethyst_harvestlevel,
                C.Amethyst_maxuses,
                C.Amethyst_efficiency,
                C.Amethyst_damage,
                C.Amethyst_enchantability);
    }

    /** Runtime form of {@link #amethyst_stats(StatSource)}. */
    public static WeaponStats amethyst_stats() {
        return amethyst_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.emerald_stats}: prefix {@code Emerald} (OreSpawnMain.java:1183). */
    public static WeaponStats emerald_stats(StatSource source) {
        return read(source,
                C.Emerald_harvestlevel,
                C.Emerald_maxuses,
                C.Emerald_efficiency,
                C.Emerald_damage,
                C.Emerald_enchantability);
    }

    /** Runtime form of {@link #emerald_stats(StatSource)}. */
    public static WeaponStats emerald_stats() {
        return emerald_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.royal_stats}: prefix {@code Royal} (OreSpawnMain.java:1184). */
    public static WeaponStats royal_stats(StatSource source) {
        return read(source,
                C.Royal_harvestlevel,
                C.Royal_maxuses,
                C.Royal_efficiency,
                C.Royal_damage,
                C.Royal_enchantability);
    }

    /** Runtime form of {@link #royal_stats(StatSource)}. */
    public static WeaponStats royal_stats() {
        return royal_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.hammy_stats}: prefix {@code Attitude} (OreSpawnMain.java:1185). */
    public static WeaponStats hammy_stats(StatSource source) {
        return read(source,
                C.Attitude_harvestlevel,
                C.Attitude_maxuses,
                C.Attitude_efficiency,
                C.Attitude_damage,
                C.Attitude_enchantability);
    }

    /** Runtime form of {@link #hammy_stats(StatSource)}. */
    public static WeaponStats hammy_stats() {
        return hammy_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.battleaxe_stats}: prefix {@code BattleAxe} (OreSpawnMain.java:1186). */
    public static WeaponStats battleaxe_stats(StatSource source) {
        return read(source,
                C.BattleAxe_harvestlevel,
                C.BattleAxe_maxuses,
                C.BattleAxe_efficiency,
                C.BattleAxe_damage,
                C.BattleAxe_enchantability);
    }

    /** Runtime form of {@link #battleaxe_stats(StatSource)}. */
    public static WeaponStats battleaxe_stats() {
        return battleaxe_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.chainsaw_stats}: prefix {@code Chainsaw} (OreSpawnMain.java:1187). */
    public static WeaponStats chainsaw_stats(StatSource source) {
        return read(source,
                C.Chainsaw_harvestlevel,
                C.Chainsaw_maxuses,
                C.Chainsaw_efficiency,
                C.Chainsaw_damage,
                C.Chainsaw_enchantability);
    }

    /** Runtime form of {@link #chainsaw_stats(StatSource)}. */
    public static WeaponStats chainsaw_stats() {
        return chainsaw_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.queenbattleaxe_stats}: prefix {@code QueenBattleAxe} (OreSpawnMain.java:1188). */
    public static WeaponStats queenbattleaxe_stats(StatSource source) {
        return read(source,
                C.QueenBattleAxe_harvestlevel,
                C.QueenBattleAxe_maxuses,
                C.QueenBattleAxe_efficiency,
                C.QueenBattleAxe_damage,
                C.QueenBattleAxe_enchantability);
    }

    /** Runtime form of {@link #queenbattleaxe_stats(StatSource)}. */
    public static WeaponStats queenbattleaxe_stats() {
        return queenbattleaxe_stats(StatSource.RUNTIME);
    }
}
