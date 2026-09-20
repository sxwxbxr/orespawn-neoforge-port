package com.swbr.orespawn.world.dimension.village;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.gen.LegacyWorldProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;

/**
 * Port of {@code WorldProviderOreSpawn3} (WorldProviderOreSpawn3.java:10-62), the provider of the VillageMania
 * dimension ({@code OreSpawnMain.DimensionID3}, OreSpawnMain.java:5043-5044; DECISIONS R13).
 *
 * <table>
 * <tr><th>1.7.10</th><th>port</th></tr>
 * <tr><td>constructor: {@code new BiomeGenUtopianPlains(BiomeVillageID)}, colour 353825, name "Villages",
 * 0.7 / 0.5 (:14-16)</td><td>{@code data/orespawn/worldgen/biome/villages.json}</td></tr>
 * <tr><td>{@code registerWorldChunkManager}: {@code setVillageCreatures()} (:27)</td><td>{@code
 * setVillageCreatures} is appended to the Utopia list, duplicates stay (verhalten/world-02.md); entries of
 * {@code orespawn:villages} in the W12 spawn table, applied by {@code world.spawn.ConfigSpawnsBiomeModifier}</td></tr>
 * <tr><td>{@code WorldChunkManagerHell(MyPlains, 0.5)}, 0.7 / 0.5 again (:28-30)</td>
 * <td>{@code data/orespawn/dimension/village.json}, fixed biome source</td></tr>
 * <tr><td>{@code BiomeManager.addVillageBiome(MyPlains, true)} (:31)</td><td>the biome list of the structure
 * {@code orespawn:village_mania}; {@link MapGenMoreVillages} ignored the check anyway</td></tr>
 * <tr><td>{@code createChunkGenerator}: {@code ChunkProviderOreSpawn3(world, seed, true)} (:59-61)</td>
 * <td>{@link ChunkProviderOreSpawn3}, type {@code orespawn:village}</td></tr>
 * <tr><td>{@code canRespawnHere} true (:22-24)</td><td>{@link #onPlayerRespawn}</td></tr>
 * <tr><td>{@code setWorldTime} (:34-57)</td><td>not ported, sleeping never reached it:
 * {@link com.swbr.orespawn.world.gen.LegacyWorldProvider}</td></tr>
 * <tr><td>{@code getDimensionName} (:18-20)</td><td>{@link #DIMENSION_NAME}</td></tr>
 * </table>
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class WorldProviderOreSpawn3 {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "village");

    /** {@code orespawn:village}. Replaces every {@code OreSpawnMain.DimensionID3} comparison. */
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ID);

    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, ID);

    /** The dimension's only biome, {@code BiomeGenUtopianPlains} "Villages" ({@code BiomeVillageID}, default 123). */
    public static final ResourceKey<Biome> BIOME =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "villages"));

    /** {@code getDimensionName()} (:18-20). */
    public static final String DIMENSION_NAME = "Dimension-VillageMania";

    /** {@code setTemperatureRainfall(0.7f, 0.5f)} (:15, :29). */
    public static final float TEMPERATURE = 0.7F;
    public static final float RAINFALL = 0.5F;

    private WorldProviderOreSpawn3() {
    }

    /** {@code canRespawnHere() == true} (:22-24). */
    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerRespawnPositionEvent event) {
        LegacyWorldProvider.canRespawnHere(event, DIMENSION);
    }
}
