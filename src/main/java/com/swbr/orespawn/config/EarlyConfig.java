package com.swbr.orespawn.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.swbr.orespawn.OreSpawn;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Reads config values that must be known while items and blocks are being registered.
 *
 * <p>NeoForge loads COMMON configs only after the registry events
 * ({@code CommonModLoader.begin}: "Registry initialization" runs before "Config loading"), but the
 * original read {@code OreSpawn.cfg} in preInit, before it created a single tool material. This class
 * parses the same TOML file the {@link OreSpawnConfig#SPEC} owns, directly from disk, and falls back
 * to the spec's default when the file or the key does not exist yet - which is exactly the first
 * start, when the defaults are also what the spec will write.
 *
 * <p>Changing one of these values needs a restart. So did the original.
 */
public final class EarlyConfig {

    private static UnmodifiableConfig file;

    private EarlyConfig() {}

    /** The value on disk for {@code value}'s path, or its default. */
    public static synchronized int get(ModConfigSpec.IntValue value) {
        if (file == null) {
            file = load();
        }
        Object raw = file.get(value.getPath());
        return raw instanceof Number n ? n.intValue() : value.getDefault();
    }

    private static UnmodifiableConfig load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(OreSpawn.MOD_ID + "-common.toml");
        if (!Files.isRegularFile(path)) {
            return Config.inMemory();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            return new TomlParser().parse(reader);
        } catch (Exception e) {
            // A broken file must not stop the game from loading; the spec will report and correct it
            // once NeoForge loads it, and until then the defaults are the safe values.
            OreSpawn.LOG.warn("Could not read {} early, using defaults for registration-time values", path, e);
            return Config.inMemory();
        }
    }
}
