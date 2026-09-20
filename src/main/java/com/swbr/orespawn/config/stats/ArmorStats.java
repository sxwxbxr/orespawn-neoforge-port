package com.swbr.orespawn.config.stats;

import com.swbr.orespawn.config.OreSpawnConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Port of {@code ArmorStats} (ArmorStats.java:5-18) together with the reader
 * {@code OreSpawnMain.get_armorstats} (OreSpawnMain.java:5633-5699), category OreSpawnARMOR.
 *
 * <p>Armor materials are registry entries in 1.21.1 and exist before the COMMON config is loaded,
 * so W03 reads them with {@link StatSource#EARLY} (DECISIONS R3). The enchantment levels are
 * applied at runtime by {@code ItemOreSpawnArmor} (R7) and may use {@link StatSource#RUNTIME}.
 *
 * <p>Sums above the ARMOR clamp of 30 (Ultimate 34, Mobzilla 38, Royal 42, Queen 48) stay as the
 * original numbers; the legacy armor formula in {@code combat} consumes them (R5).
 */
public record ArmorStats(int durability, int head_protection, int chest_protection, int leg_protection,
        int boot_protection, int enchantability, int e_respiration, int e_aquaaffinity, int e_protection,
        int e_fireprotection, int e_blastprotection, int e_projectileprotection, int e_unbreaking,
        int e_featherfalling) {

    private static final OreSpawnConfig.Armor C = OreSpawnConfig.ARMOR;

    /**
     * {@code get_armorstats}: durability and enchantability clamped to [default/2, default*2]; the
     * four protection values only upwards to default-2, no upper bound; the eight enchantment
     * levels only upwards to default/2 (integer division), no upper bound
     * (OreSpawnMain.java:5636-5697).
     */
    public static ArmorStats read(StatSource source, ModConfigSpec.IntValue durabilityValue,
            ModConfigSpec.IntValue headValue, ModConfigSpec.IntValue chestValue, ModConfigSpec.IntValue legValue,
            ModConfigSpec.IntValue bootsValue, ModConfigSpec.IntValue enchantabilityValue,
            ModConfigSpec.IntValue respirationValue, ModConfigSpec.IntValue aquaAffinityValue,
            ModConfigSpec.IntValue protectionValue, ModConfigSpec.IntValue fireProtectionValue,
            ModConfigSpec.IntValue blastProtectionValue, ModConfigSpec.IntValue projectileProtectionValue,
            ModConfigSpec.IntValue unbreakingValue, ModConfigSpec.IntValue featherFallingValue) {
        int dura = durabilityValue.getDefault();
        int durability = source.read(durabilityValue);
        if (durability < dura / 2) {
            durability = dura / 2;
        }
        if (durability > dura * 2) {
            durability = dura * 2;
        }
        int head = headValue.getDefault();
        int headProtection = source.read(headValue);
        if (headProtection < head - 2) {
            headProtection = head - 2;
        }
        int chest = chestValue.getDefault();
        int chestProtection = source.read(chestValue);
        if (chestProtection < chest - 2) {
            chestProtection = chest - 2;
        }
        int leg = legValue.getDefault();
        int legProtection = source.read(legValue);
        if (legProtection < leg - 2) {
            legProtection = leg - 2;
        }
        int boots = bootsValue.getDefault();
        int bootProtection = source.read(bootsValue);
        if (bootProtection < boots - 2) {
            bootProtection = boots - 2;
        }
        int enchant = enchantabilityValue.getDefault();
        int enchantability = source.read(enchantabilityValue);
        if (enchantability < enchant / 2) {
            enchantability = enchant / 2;
        }
        if (enchantability > enchant * 2) {
            enchantability = enchant * 2;
        }
        return new ArmorStats(durability, headProtection, chestProtection, legProtection, bootProtection,
                enchantability,
                atLeastHalfDefault(source, respirationValue),
                atLeastHalfDefault(source, aquaAffinityValue),
                atLeastHalfDefault(source, protectionValue),
                atLeastHalfDefault(source, fireProtectionValue),
                atLeastHalfDefault(source, blastProtectionValue),
                atLeastHalfDefault(source, projectileProtectionValue),
                atLeastHalfDefault(source, unbreakingValue),
                atLeastHalfDefault(source, featherFallingValue));
    }

    /** The {@code e_*} clamp: only a floor of default/2 (OreSpawnMain.java:5667-5697). */
    private static int atLeastHalfDefault(StatSource source, ModConfigSpec.IntValue value) {
        int def = value.getDefault();
        int v = source.read(value);
        if (v < def / 2) {
            v = def / 2;
        }
        return v;
    }

    /**
     * Sum of the eight enchantment levels. {@code ItemOreSpawnArmor} uses it as the "already
     * enchanted" marker (ItemOreSpawnArmor.java:197-198, verhalten/core-02.md).
     */
    public int enchantmentSum() {
        return e_respiration + e_aquaaffinity + e_protection + e_fireprotection + e_blastprotection
                + e_projectileprotection + e_unbreaking + e_featherfalling;
    }

    /** {@code OreSpawnMain.Amethyst_armorstats}: prefix {@code Amethyst} (OreSpawnMain.java:1160). */
    public static ArmorStats Amethyst_armorstats(StatSource source) {
        return read(source,
                C.Amethyst_durability,
                C.Amethyst_head_damage_reduce,
                C.Amethyst_chest_damage_reduce,
                C.Amethyst_leggings_damage_reduce,
                C.Amethyst_boots_damage_reduce,
                C.Amethyst_enchantability,
                C.Amethyst_enchant_respiration,
                C.Amethyst_enchant_aquaaffinity,
                C.Amethyst_enchant_protection,
                C.Amethyst_enchant_fireprotection,
                C.Amethyst_enchant_blastprotection,
                C.Amethyst_enchant_projectileprotection,
                C.Amethyst_enchant_unbreaking,
                C.Amethyst_enchant_featherfalling);
    }

    /** Runtime form of {@link #Amethyst_armorstats(StatSource)}. */
    public static ArmorStats Amethyst_armorstats() {
        return Amethyst_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Emerald_armorstats}: prefix {@code Emerald} (OreSpawnMain.java:1161). */
    public static ArmorStats Emerald_armorstats(StatSource source) {
        return read(source,
                C.Emerald_durability,
                C.Emerald_head_damage_reduce,
                C.Emerald_chest_damage_reduce,
                C.Emerald_leggings_damage_reduce,
                C.Emerald_boots_damage_reduce,
                C.Emerald_enchantability,
                C.Emerald_enchant_respiration,
                C.Emerald_enchant_aquaaffinity,
                C.Emerald_enchant_protection,
                C.Emerald_enchant_fireprotection,
                C.Emerald_enchant_blastprotection,
                C.Emerald_enchant_projectileprotection,
                C.Emerald_enchant_unbreaking,
                C.Emerald_enchant_featherfalling);
    }

    /** Runtime form of {@link #Emerald_armorstats(StatSource)}. */
    public static ArmorStats Emerald_armorstats() {
        return Emerald_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Experience_armorstats}: prefix {@code Experience} (OreSpawnMain.java:1162). */
    public static ArmorStats Experience_armorstats(StatSource source) {
        return read(source,
                C.Experience_durability,
                C.Experience_head_damage_reduce,
                C.Experience_chest_damage_reduce,
                C.Experience_leggings_damage_reduce,
                C.Experience_boots_damage_reduce,
                C.Experience_enchantability,
                C.Experience_enchant_respiration,
                C.Experience_enchant_aquaaffinity,
                C.Experience_enchant_protection,
                C.Experience_enchant_fireprotection,
                C.Experience_enchant_blastprotection,
                C.Experience_enchant_projectileprotection,
                C.Experience_enchant_unbreaking,
                C.Experience_enchant_featherfalling);
    }

    /** Runtime form of {@link #Experience_armorstats(StatSource)}. */
    public static ArmorStats Experience_armorstats() {
        return Experience_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.MothScale_armorstats}: prefix {@code MothScale} (OreSpawnMain.java:1163). */
    public static ArmorStats MothScale_armorstats(StatSource source) {
        return read(source,
                C.MothScale_durability,
                C.MothScale_head_damage_reduce,
                C.MothScale_chest_damage_reduce,
                C.MothScale_leggings_damage_reduce,
                C.MothScale_boots_damage_reduce,
                C.MothScale_enchantability,
                C.MothScale_enchant_respiration,
                C.MothScale_enchant_aquaaffinity,
                C.MothScale_enchant_protection,
                C.MothScale_enchant_fireprotection,
                C.MothScale_enchant_blastprotection,
                C.MothScale_enchant_projectileprotection,
                C.MothScale_enchant_unbreaking,
                C.MothScale_enchant_featherfalling);
    }

    /** Runtime form of {@link #MothScale_armorstats(StatSource)}. */
    public static ArmorStats MothScale_armorstats() {
        return MothScale_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.LavaEel_armorstats}: prefix {@code LavaEel} (OreSpawnMain.java:1164). */
    public static ArmorStats LavaEel_armorstats(StatSource source) {
        return read(source,
                C.LavaEel_durability,
                C.LavaEel_head_damage_reduce,
                C.LavaEel_chest_damage_reduce,
                C.LavaEel_leggings_damage_reduce,
                C.LavaEel_boots_damage_reduce,
                C.LavaEel_enchantability,
                C.LavaEel_enchant_respiration,
                C.LavaEel_enchant_aquaaffinity,
                C.LavaEel_enchant_protection,
                C.LavaEel_enchant_fireprotection,
                C.LavaEel_enchant_blastprotection,
                C.LavaEel_enchant_projectileprotection,
                C.LavaEel_enchant_unbreaking,
                C.LavaEel_enchant_featherfalling);
    }

    /** Runtime form of {@link #LavaEel_armorstats(StatSource)}. */
    public static ArmorStats LavaEel_armorstats() {
        return LavaEel_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Ultimate_armorstats}: prefix {@code Ultimate} (OreSpawnMain.java:1165). */
    public static ArmorStats Ultimate_armorstats(StatSource source) {
        return read(source,
                C.Ultimate_durability,
                C.Ultimate_head_damage_reduce,
                C.Ultimate_chest_damage_reduce,
                C.Ultimate_leggings_damage_reduce,
                C.Ultimate_boots_damage_reduce,
                C.Ultimate_enchantability,
                C.Ultimate_enchant_respiration,
                C.Ultimate_enchant_aquaaffinity,
                C.Ultimate_enchant_protection,
                C.Ultimate_enchant_fireprotection,
                C.Ultimate_enchant_blastprotection,
                C.Ultimate_enchant_projectileprotection,
                C.Ultimate_enchant_unbreaking,
                C.Ultimate_enchant_featherfalling);
    }

    /** Runtime form of {@link #Ultimate_armorstats(StatSource)}. */
    public static ArmorStats Ultimate_armorstats() {
        return Ultimate_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Pink_armorstats}: prefix {@code Pink} (OreSpawnMain.java:1166). */
    public static ArmorStats Pink_armorstats(StatSource source) {
        return read(source,
                C.Pink_durability,
                C.Pink_head_damage_reduce,
                C.Pink_chest_damage_reduce,
                C.Pink_leggings_damage_reduce,
                C.Pink_boots_damage_reduce,
                C.Pink_enchantability,
                C.Pink_enchant_respiration,
                C.Pink_enchant_aquaaffinity,
                C.Pink_enchant_protection,
                C.Pink_enchant_fireprotection,
                C.Pink_enchant_blastprotection,
                C.Pink_enchant_projectileprotection,
                C.Pink_enchant_unbreaking,
                C.Pink_enchant_featherfalling);
    }

    /** Runtime form of {@link #Pink_armorstats(StatSource)}. */
    public static ArmorStats Pink_armorstats() {
        return Pink_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.TigersEye_armorstats}: prefix {@code TigersEye} (OreSpawnMain.java:1167). */
    public static ArmorStats TigersEye_armorstats(StatSource source) {
        return read(source,
                C.TigersEye_durability,
                C.TigersEye_head_damage_reduce,
                C.TigersEye_chest_damage_reduce,
                C.TigersEye_leggings_damage_reduce,
                C.TigersEye_boots_damage_reduce,
                C.TigersEye_enchantability,
                C.TigersEye_enchant_respiration,
                C.TigersEye_enchant_aquaaffinity,
                C.TigersEye_enchant_protection,
                C.TigersEye_enchant_fireprotection,
                C.TigersEye_enchant_blastprotection,
                C.TigersEye_enchant_projectileprotection,
                C.TigersEye_enchant_unbreaking,
                C.TigersEye_enchant_featherfalling);
    }

    /** Runtime form of {@link #TigersEye_armorstats(StatSource)}. */
    public static ArmorStats TigersEye_armorstats() {
        return TigersEye_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Peacock_armorstats}: prefix {@code Peacock} (OreSpawnMain.java:1168). */
    public static ArmorStats Peacock_armorstats(StatSource source) {
        return read(source,
                C.Peacock_durability,
                C.Peacock_head_damage_reduce,
                C.Peacock_chest_damage_reduce,
                C.Peacock_leggings_damage_reduce,
                C.Peacock_boots_damage_reduce,
                C.Peacock_enchantability,
                C.Peacock_enchant_respiration,
                C.Peacock_enchant_aquaaffinity,
                C.Peacock_enchant_protection,
                C.Peacock_enchant_fireprotection,
                C.Peacock_enchant_blastprotection,
                C.Peacock_enchant_projectileprotection,
                C.Peacock_enchant_unbreaking,
                C.Peacock_enchant_featherfalling);
    }

    /** Runtime form of {@link #Peacock_armorstats(StatSource)}. */
    public static ArmorStats Peacock_armorstats() {
        return Peacock_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Mobzilla_armorstats}: prefix {@code Mobzilla} (OreSpawnMain.java:1169). */
    public static ArmorStats Mobzilla_armorstats(StatSource source) {
        return read(source,
                C.Mobzilla_durability,
                C.Mobzilla_head_damage_reduce,
                C.Mobzilla_chest_damage_reduce,
                C.Mobzilla_leggings_damage_reduce,
                C.Mobzilla_boots_damage_reduce,
                C.Mobzilla_enchantability,
                C.Mobzilla_enchant_respiration,
                C.Mobzilla_enchant_aquaaffinity,
                C.Mobzilla_enchant_protection,
                C.Mobzilla_enchant_fireprotection,
                C.Mobzilla_enchant_blastprotection,
                C.Mobzilla_enchant_projectileprotection,
                C.Mobzilla_enchant_unbreaking,
                C.Mobzilla_enchant_featherfalling);
    }

    /** Runtime form of {@link #Mobzilla_armorstats(StatSource)}. */
    public static ArmorStats Mobzilla_armorstats() {
        return Mobzilla_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Ruby_armorstats}: prefix {@code Ruby} (OreSpawnMain.java:1170). */
    public static ArmorStats Ruby_armorstats(StatSource source) {
        return read(source,
                C.Ruby_durability,
                C.Ruby_head_damage_reduce,
                C.Ruby_chest_damage_reduce,
                C.Ruby_leggings_damage_reduce,
                C.Ruby_boots_damage_reduce,
                C.Ruby_enchantability,
                C.Ruby_enchant_respiration,
                C.Ruby_enchant_aquaaffinity,
                C.Ruby_enchant_protection,
                C.Ruby_enchant_fireprotection,
                C.Ruby_enchant_blastprotection,
                C.Ruby_enchant_projectileprotection,
                C.Ruby_enchant_unbreaking,
                C.Ruby_enchant_featherfalling);
    }

    /** Runtime form of {@link #Ruby_armorstats(StatSource)}. */
    public static ArmorStats Ruby_armorstats() {
        return Ruby_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Royal_armorstats}: prefix {@code Royal} (OreSpawnMain.java:1171). */
    public static ArmorStats Royal_armorstats(StatSource source) {
        return read(source,
                C.Royal_durability,
                C.Royal_head_damage_reduce,
                C.Royal_chest_damage_reduce,
                C.Royal_leggings_damage_reduce,
                C.Royal_boots_damage_reduce,
                C.Royal_enchantability,
                C.Royal_enchant_respiration,
                C.Royal_enchant_aquaaffinity,
                C.Royal_enchant_protection,
                C.Royal_enchant_fireprotection,
                C.Royal_enchant_blastprotection,
                C.Royal_enchant_projectileprotection,
                C.Royal_enchant_unbreaking,
                C.Royal_enchant_featherfalling);
    }

    /** Runtime form of {@link #Royal_armorstats(StatSource)}. */
    public static ArmorStats Royal_armorstats() {
        return Royal_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Lapis_armorstats}: prefix {@code Lapis} (OreSpawnMain.java:1172). */
    public static ArmorStats Lapis_armorstats(StatSource source) {
        return read(source,
                C.Lapis_durability,
                C.Lapis_head_damage_reduce,
                C.Lapis_chest_damage_reduce,
                C.Lapis_leggings_damage_reduce,
                C.Lapis_boots_damage_reduce,
                C.Lapis_enchantability,
                C.Lapis_enchant_respiration,
                C.Lapis_enchant_aquaaffinity,
                C.Lapis_enchant_protection,
                C.Lapis_enchant_fireprotection,
                C.Lapis_enchant_blastprotection,
                C.Lapis_enchant_projectileprotection,
                C.Lapis_enchant_unbreaking,
                C.Lapis_enchant_featherfalling);
    }

    /** Runtime form of {@link #Lapis_armorstats(StatSource)}. */
    public static ArmorStats Lapis_armorstats() {
        return Lapis_armorstats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Queen_armorstats}: prefix {@code Queen} (OreSpawnMain.java:1173). */
    public static ArmorStats Queen_armorstats(StatSource source) {
        return read(source,
                C.Queen_durability,
                C.Queen_head_damage_reduce,
                C.Queen_chest_damage_reduce,
                C.Queen_leggings_damage_reduce,
                C.Queen_boots_damage_reduce,
                C.Queen_enchantability,
                C.Queen_enchant_respiration,
                C.Queen_enchant_aquaaffinity,
                C.Queen_enchant_protection,
                C.Queen_enchant_fireprotection,
                C.Queen_enchant_blastprotection,
                C.Queen_enchant_projectileprotection,
                C.Queen_enchant_unbreaking,
                C.Queen_enchant_featherfalling);
    }

    /** Runtime form of {@link #Queen_armorstats(StatSource)}. */
    public static ArmorStats Queen_armorstats() {
        return Queen_armorstats(StatSource.RUNTIME);
    }
}
