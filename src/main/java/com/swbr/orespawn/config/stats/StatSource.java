package com.swbr.orespawn.config.stats;

import com.swbr.orespawn.config.EarlyConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Where a stat holder takes its raw value from.
 *
 * <p>The original read everything once in preInit, before any material or item existed. NeoForge
 * loads the COMMON spec only after the registry events (DECISIONS R3), so the same clamp code
 * has to run against two sources: the loaded spec at runtime, and the TOML file on disk while
 * tiers and armor materials are being registered. {@link ModConfigSpec.ConfigValue#get()}
 * throws before the config is loaded ({@code ModConfigSpec.java:1235}), which is why the early
 * path is not merely "try get() first".
 */
public enum StatSource {

    /** The loaded spec. Use after {@code OreSpawnConfig.SPEC.isLoaded()}, i.e. in game logic. */
    RUNTIME {
        @Override
        public int read(ModConfigSpec.IntValue value) {
            return value.get();
        }
    },

    /** The file on disk, or the default when the file or key does not exist yet. */
    EARLY {
        @Override
        public int read(ModConfigSpec.IntValue value) {
            return EarlyConfig.get(value);
        }
    };

    public abstract int read(ModConfigSpec.IntValue value);
}
