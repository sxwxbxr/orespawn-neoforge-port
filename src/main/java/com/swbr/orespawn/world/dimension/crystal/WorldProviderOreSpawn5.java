package com.swbr.orespawn.world.dimension.crystal;

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
 * Port of {@code WorldProviderOreSpawn5} (WorldProviderOreSpawn5.java:10-62), the provider of the
 * Crystal dimension ({@code OreSpawnMain.DimensionID5}, DECISIONS R13: {@code orespawn:crystal}).
 *
 * <p>Where each method went:
 * <ul>
 *   <li>{@code getDimensionName} "Dimension-Crystal" (:18-20): {@link #DIMENSION_NAME}; the id is
 *       {@code orespawn:crystal}.</li>
 *   <li>{@code registerWorldChunkManager} (:26-32): one {@code BiomeGenUtopianPlains} "Crystal" with
 *       temperature/rainfall finally 0.8/0.01 (:29-30) and height 0.1/0.5 (:28). Datapack biome
 *       {@code data/orespawn/worldgen/biome/crystal.json}, fixed biome source in
 *       {@code dimension/crystal.json}; the height went into the generator codec
 *       ({@code root_height}, {@code height_variation}), because 1.21.1 biomes have none. The spawn
 *       lists of {@code setCrystalCreatures} come from {@code world.spawn.ConfigSpawnsBiomeModifier}
 *       (entries of {@code orespawn:crystal} in data/orespawn/spawn_table/spawns.json).</li>
 *   <li>{@code canRespawnHere} true (:22-24): {@link LegacyWorldProvider#canRespawnHere}.</li>
 *   <li>{@code setWorldTime} (:34-57): not ported - sleeping never reached its night skip; see
 *       {@link LegacyWorldProvider}.</li>
 *   <li>{@code createChunkGenerator} (:59-61): {@link ChunkProviderOreSpawn5}, codec type
 *       {@code orespawn:crystal}.</li>
 *   <li>Sky, fog, clouds: not overridden, so the overworld's - {@code effects: minecraft:overworld} in
 *       {@code dimension_type/crystal.json}, {@code min_y} 0 and height 256 so the maze stays at Y
 *       24..28 (verhalten/world-04.md).</li>
 * </ul>
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class WorldProviderOreSpawn5 {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "crystal");
    /** {@code OreSpawnMain.DimensionID5}. */
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ID);
    public static final ResourceKey<LevelStem> LEVEL_STEM = ResourceKey.create(Registries.LEVEL_STEM, ID);
    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, ID);
    /** {@code BiomeGenUtopianPlains(BiomeCrystalID)} "Crystal". */
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, ID);
    /** {@code getDimensionName()}. */
    public static final String DIMENSION_NAME = "Dimension-Crystal";

    private WorldProviderOreSpawn5() {
    }

    /** {@code canRespawnHere()}. */
    public static boolean canRespawnHere() {
        return true;
    }

    /** {@code world.provider.dimensionId == OreSpawnMain.DimensionID5}. */
    public static boolean isCrystal(final Level level) {
        return level.dimension().equals(DIMENSION);
    }

    @SubscribeEvent
    public static void onRespawnPosition(final PlayerRespawnPositionEvent event) {
        if (canRespawnHere()) {
            LegacyWorldProvider.canRespawnHere(event, DIMENSION);
        }
    }
}
