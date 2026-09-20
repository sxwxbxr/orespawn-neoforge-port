package com.swbr.orespawn.world.dimension.chaos;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.gen.LegacyWorldProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;

/**
 * Port of {@code WorldProviderOreSpawn6} (WorldProviderOreSpawn6.java:10-62), the provider of the
 * Chaos dimension ({@code OreSpawnMain.DimensionID6}, DECISIONS R13: {@code orespawn:chaos}).
 *
 * <ul>
 *   <li>{@code getDimensionName} "Dimension-Chaos" (:18-20): {@link #DIMENSION_NAME}.</li>
 *   <li>{@code registerWorldChunkManager} (:26-32): one {@code BiomeGenUtopianPlains} "Chaos",
 *       temperature/rainfall finally 0.8/0.01 (:28-29), decorator with 2 flowers, 4 grass and 1 tree
 *       per chunk ({@code setChaosCreatures}, BiomeGenUtopianPlains.java:263-268): datapack biome
 *       {@code worldgen/biome/chaos.json}. Its spawn lists come from
 *       {@code world.spawn.ConfigSpawnsBiomeModifier} (data/orespawn/spawn_table/spawns.json). The first
 *       {@code dimensionId = DimensionID4} (:30) is overwritten on the next line and has no effect.</li>
 *   <li>{@code canRespawnHere} true (:22-24): {@link LegacyWorldProvider#canRespawnHere} - a player who
 *       dies over the void respawns over the void (DECISIONS R18: 1:1).</li>
 *   <li>{@code setWorldTime} (:34-57): not ported - sleeping never reached its night skip; see
 *       {@link LegacyWorldProvider}.</li>
 *   <li>{@code createChunkGenerator} (:59-61): {@link ChunkProviderOreSpawn6}, codec type
 *       {@code orespawn:chaos}.</li>
 *   <li>Sky: the overworld's ({@code effects: minecraft:overworld}); no ceiling, void below.</li>
 * </ul>
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class WorldProviderOreSpawn6 {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "chaos");
    /** {@code OreSpawnMain.DimensionID6}. */
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ID);
    public static final ResourceKey<LevelStem> LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, ID);
    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, ID);
    /** {@code BiomeGenUtopianPlains(BiomeChaosID)} "Chaos". */
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, ID);
    /** {@code getDimensionName()}. */
    public static final String DIMENSION_NAME = "Dimension-Chaos";

    private WorldProviderOreSpawn6() {
    }

    /** {@code canRespawnHere()}. */
    public static boolean canRespawnHere() {
        return true;
    }

    /** {@code world.provider.dimensionId == OreSpawnMain.DimensionID6}. */
    public static boolean isChaos(final Level level) {
        return level.dimension().equals(DIMENSION);
    }

    @SubscribeEvent
    public static void onRespawnPosition(final PlayerRespawnPositionEvent event) {
        if (canRespawnHere()) {
            LegacyWorldProvider.canRespawnHere(event, DIMENSION);
        }
    }
}
