package com.swbr.orespawn.world.feature;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry holders of the W12 features and placement modifiers. Lives in {@code world.feature} because W12 owns this
 * package, not {@code registry/}; the integrator may move it. Both registers must be attached to the mod bus in the
 * mod constructor: the configured and placed feature JSON under {@code data/orespawn/worldgen/} name these ids.
 */
public final class OreSpawnFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, OreSpawn.MOD_ID);

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, OreSpawn.MOD_ID);

    /** {@link OreSpawnWorldFeature}. */
    public static final DeferredHolder<Feature<?>, OreSpawnWorldFeature> ORESPAWN_WORLD =
            FEATURES.register("orespawn_world", OreSpawnWorldFeature::new);

    /** {@link LegacyMinableFeature}. */
    public static final DeferredHolder<Feature<?>, LegacyMinableFeature> LEGACY_MINABLE =
            FEATURES.register("legacy_minable", LegacyMinableFeature::new);

    /** {@link LegacyRubyOreFeature}. */
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> LEGACY_RUBY_ORE =
            FEATURES.register("legacy_ruby_ore", LegacyRubyOreFeature::new);

    /** {@link com.swbr.orespawn.world.maze.RubyBirdDungeonFeature}: RubyBirdDungeon.makeDungeon at the placement origin. */
    public static final DeferredHolder<Feature<?>, com.swbr.orespawn.world.maze.RubyBirdDungeonFeature> RUBY_BIRD_DUNGEON =
            FEATURES.register("ruby_bird_dungeon", com.swbr.orespawn.world.maze.RubyBirdDungeonFeature::new);

    private static final PlacementModifierType<LegacyCountPlacement> LEGACY_COUNT_TYPE = () -> LegacyCountPlacement.CODEC;
    private static final PlacementModifierType<LegacyOrePlacement> LEGACY_ORE_POSITION_TYPE = () -> LegacyOrePlacement.CODEC;

    /** {@code orespawn:legacy_count}, {@link LegacyCountPlacement}. */
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<LegacyCountPlacement>> LEGACY_COUNT =
            PLACEMENT_MODIFIERS.register("legacy_count", () -> LEGACY_COUNT_TYPE);

    /** {@code orespawn:legacy_ore_position}, {@link LegacyOrePlacement}. */
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<LegacyOrePlacement>> LEGACY_ORE_POSITION =
            PLACEMENT_MODIFIERS.register("legacy_ore_position", () -> LEGACY_ORE_POSITION_TYPE);

    private OreSpawnFeatures() {
    }

    /** Loads this class so every holder above is registered. */
    public static void init() {
    }
}
