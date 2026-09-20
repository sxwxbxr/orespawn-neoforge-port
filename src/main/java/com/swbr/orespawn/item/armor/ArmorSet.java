package com.swbr.orespawn.item.armor;

import com.swbr.orespawn.config.stats.ArmorStats;
import com.swbr.orespawn.config.stats.StatSource;
import java.util.function.Function;

/**
 * The fourteen armor sets of the original, in the order of the {@code armor_material} index that
 * {@code ItemOreSpawnArmor}'s constructor derived from the {@code ArmorMaterial} enum constant
 * (ItemOreSpawnArmor.java:24-63; manifest {@code armor_materials}, verhalten/itemblock-01.md
 * section 12).
 *
 * <p>Each set owns three things the original spread over {@code OreSpawnMain}: the
 * {@code EnumHelper.addArmorMaterial} constant (:1432-1445), the {@code <Set>_armorstats} holder
 * (:1160-1173) and the texture prefix passed to {@code proxy.setArmorPrefix("<prefix>")}
 * (:1446-1501), which is also the file name of the two worn-armor layers.
 */
public enum ArmorSet {
    /** {@code armorULTIMATE}, index 0 - the default branch of the constructor (:24). */
    ULTIMATE(0, "ultimate", ArmorStats::Ultimate_armorstats),
    /** {@code armorLAVAEEL} (:25-27). */
    LAVAEEL(1, "lavaeel", ArmorStats::LavaEel_armorstats),
    /** {@code armorMOTHSCALE} (:28-30). */
    MOTHSCALE(2, "mothscale", ArmorStats::MothScale_armorstats),
    /** {@code armorEMERALD} (:31-33); the fallback branch of {@code getArmorTexture} (:295, :337). */
    EMERALD(3, "emerald", ArmorStats::Emerald_armorstats),
    /** {@code armorEXPERIENCE} (:34-36); read by {@code ExperienceSword} as index 4. */
    EXPERIENCE(4, "experience", ArmorStats::Experience_armorstats),
    /** {@code armorRUBY} (:37-39). */
    RUBY(5, "ruby", ArmorStats::Ruby_armorstats),
    /** {@code armorAMETHYST} (:40-42). */
    AMETHYST(6, "amethyst", ArmorStats::Amethyst_armorstats),
    /** {@code armorPINK} (:43-45). */
    PINK(7, "pink", ArmorStats::Pink_armorstats),
    /** {@code armorTIGERSEYE} (:46-48). */
    TIGERSEYE(8, "tigerseye", ArmorStats::TigersEye_armorstats),
    /** {@code armorPEACOCK} (:49-51); glides with its boots (:345-353). */
    PEACOCK(9, "peacock", ArmorStats::Peacock_armorstats),
    /** {@code armorMOBZILLA} (:52-54). */
    MOBZILLA(10, "mobzilla", ArmorStats::Mobzilla_armorstats),
    /** {@code armorROYAL} (:55-57); glides with its boots when RoyalGlideEnable != 0 (:345-353). */
    ROYAL(11, "royal", ArmorStats::Royal_armorstats),
    /** {@code armorLAPIS} (:58-60). */
    LAPIS(12, "lapis", ArmorStats::Lapis_armorstats),
    /** {@code armorQUEEN} (:61-63); glides faster with its boots when RoyalGlideEnable != 0 (:354-362). */
    QUEEN(13, "queen", ArmorStats::Queen_armorstats);

    private final int index;
    private final String prefix;
    private final Function<StatSource, ArmorStats> stats;

    ArmorSet(int index, String prefix, Function<StatSource, ArmorStats> stats) {
        this.index = index;
        this.prefix = prefix;
        this.stats = stats;
    }

    /** The original {@code armor_material} field value (0 = Ultimate ... 13 = Queen). */
    public int index() {
        return index;
    }

    /**
     * The texture prefix: item ids are {@code <prefix>_helmet|_chest|_leggings|_boots}, the worn
     * layers are {@code textures/models/armor/<prefix>_layer_1.png} and {@code _layer_2.png}
     * (copied there by {@code tools/assets.py} from the original {@code <prefix>_1.png} /
     * {@code _2.png}), and the armor material is registered as {@code orespawn:<prefix>}.
     */
    public String prefix() {
        return prefix;
    }

    /**
     * {@code OreSpawnMain.<Set>_armorstats}, read through the clamping reader of
     * {@link ArmorStats}. {@link StatSource#EARLY} while materials and items are registered
     * (DECISIONS R3), {@link StatSource#RUNTIME} for the enchantment levels in game logic.
     */
    public ArmorStats stats(StatSource source) {
        return stats.apply(source);
    }

    /** The set with the given original index, or {@code null} for an index the original never used. */
    public static ArmorSet byIndex(int index) {
        for (ArmorSet set : values()) {
            if (set.index == index) {
                return set;
            }
        }
        return null;
    }
}
