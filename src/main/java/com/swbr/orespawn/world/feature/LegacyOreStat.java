package com.swbr.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.swbr.orespawn.config.stats.OreStats;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;

/**
 * The {@code OreSpawnMain.*_stats} records {@code OreSpawnWorld.generateOres} reads, by the name the placed-feature
 * JSON uses. Values are read at placement time from the loaded COMMON config ({@link OreStats}, clamps of
 * {@code get_orestats} included), so a changed config applies to chunks generated afterwards, as the original read
 * its statics on every populate.
 */
public enum LegacyOreStat implements StringRepresentable {
    RUBY("ruby", OreStats::Ruby_stats),
    BLOCK_RUBY("block_ruby", OreStats::BlkRuby_stats),
    URANIUM("uranium", OreStats::Uranium_stats),
    TITANIUM("titanium", OreStats::Titanium_stats),
    AMETHYST("amethyst", OreStats::Amethyst_stats),
    SALT("salt", OreStats::Salt_stats),
    SPAWN_ORES("spawn_ores", OreStats::SpawnOres_stats),
    DIAMOND("diamond", OreStats::Diamond_stats),
    BLOCK_DIAMOND("block_diamond", OreStats::BlkDiamond_stats),
    EMERALD("emerald", OreStats::Emerald_stats),
    BLOCK_EMERALD("block_emerald", OreStats::BlkEmerald_stats),
    GOLD("gold", OreStats::Gold_stats),
    BLOCK_GOLD("block_gold", OreStats::BlkGold_stats);

    public static final Codec<LegacyOreStat> CODEC = StringRepresentable.fromEnum(LegacyOreStat::values);

    private final String name;
    private final Supplier<OreStats> stats;

    LegacyOreStat(final String name, final Supplier<OreStats> stats) {
        this.name = name;
        this.stats = stats;
    }

    public OreStats get() {
        return this.stats.get();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
