package com.swbr.orespawn.loot;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.OreSpawn;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Codec registrations for the data-driven parts of {@code OreSpawnMain.make_some_more_things}
 * that 1.21.1 cannot express with vanilla types: the chest loot (OreSpawnMain.java:5051-5062) and
 * the config-dependent Miner's Dream recipe (OreSpawnMain.java:3018-3025).
 *
 * <p>Both registers must be attached to the mod bus in the mod constructor, after {@link #init()}
 * class-loaded this holder (the pattern of every holder in {@code registry}).
 */
public final class ModLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, OreSpawn.MOD_ID);

    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, OreSpawn.MOD_ID);

    /** {@code orespawn:legacy_chest_content}, used by data/orespawn/loot_modifiers/*.json. */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<LegacyChestContentModifier>> LEGACY_CHEST_CONTENT =
            LOOT_MODIFIERS.register("legacy_chest_content", () -> LegacyChestContentModifier.CODEC);

    /** {@code orespawn:miners_dream_expensive}, used by data/orespawn/recipe/minersdream_from_*.json. */
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<MinersDreamExpensiveCondition>> MINERS_DREAM_EXPENSIVE =
            CONDITION_CODECS.register("miners_dream_expensive", () -> MinersDreamExpensiveCondition.CODEC);

    /** Class-loads the holder; see {@code OreSpawn}'s constructor. */
    public static void init() {}

    private ModLootModifiers() {}
}
