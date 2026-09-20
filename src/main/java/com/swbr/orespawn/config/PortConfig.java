package com.swbr.orespawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Switches that exist only because of the port, kept apart from the original's 624 keys in
 * {@link OreSpawnConfig}: the original file is a faithful copy of {@code OreSpawn.cfg}, and a key
 * the original never had does not belong in it.
 *
 * <p>COMMON, one file {@value #FILE_NAME}. Registered from the mod constructor:
 * {@code container.registerConfig(ModConfig.Type.COMMON, PortConfig.SPEC, PortConfig.FILE_NAME)}.
 */
public final class PortConfig {

    /** File name under {@code config/}; the second argument of {@code registerConfig}. */
    public static final String FILE_NAME = "orespawn-port.toml";

    public static final ModConfigSpec SPEC;

    /**
     * docs/DECISIONS.md R5: use the 1.7.10 armor formula {@code damage * (25 - armor) / 25} for
     * OreSpawn entities and for players wearing OreSpawn armor, instead of the 1.21.1 formula
     * that caps at 80 % and clamps armor to 30. Default {@code true} - the bosses' 21-26 armor and
     * the 42/48-point Royal and Queen sets only mean what they meant under the original formula.
     */
    public static final ModConfigSpec.BooleanValue LEGACY_ARMOR_FORMULA;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Switches added by the 1.21.1 port. The original OreSpawn keys live in orespawn-common.toml.")
                .push("combat");
        LEGACY_ARMOR_FORMULA = b.comment(
                        "Use the 1.7.10 armor formula damage * (25 - armor) / 25 for OreSpawn mobs and for",
                        "players wearing at least one OreSpawn armor piece. 25 armor points block every",
                        "blockable hit, as in 1.7.10. false = vanilla 1.21.1 armor for everyone.")
                .define("legacyArmorFormula", true);
        b.pop();
        SPEC = b.build();
    }

    private PortConfig() {}

    /**
     * {@link #LEGACY_ARMOR_FORMULA} if the file has been loaded, its default otherwise.
     * {@code ConfigValue.get()} throws before the config is loaded; the damage event cannot fire
     * that early in a normal game, but a guard is cheaper than a crash report.
     */
    public static boolean legacyArmorFormula() {
        return SPEC.isLoaded() ? LEGACY_ARMOR_FORMULA.get() : LEGACY_ARMOR_FORMULA.getDefault();
    }
}
