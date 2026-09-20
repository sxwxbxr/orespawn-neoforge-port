package com.swbr.orespawn.registry;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.spawn.ConfigSpawnsBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Biome modifier codecs; {@code data/orespawn/neoforge/biome_modifier/*.json} name them by id. The ore and
 * {@code orespawn_world} modifiers of W12 use NeoForge's own {@code neoforge:add_features}, so this is the only
 * OreSpawn codec so far.
 */
public final class ModBiomeModifiers {

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, OreSpawn.MOD_ID);

    /** {@code orespawn:config_spawns} (R11) - the value must be the very instance ConfigSpawnsBiomeModifier.codec() returns. */
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ConfigSpawnsBiomeModifier>> CONFIG_SPAWNS =
            BIOME_MODIFIER_SERIALIZERS.register("config_spawns", () -> ConfigSpawnsBiomeModifier.CODEC);

    private ModBiomeModifiers() {
    }

    /** Loads this class so every holder above is registered. */
    public static void init() {
    }
}
