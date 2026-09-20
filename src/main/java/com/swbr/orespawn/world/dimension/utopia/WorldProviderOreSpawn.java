package com.swbr.orespawn.world.dimension.utopia;

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
 * Port of {@code WorldProviderOreSpawn} (WorldProviderOreSpawn.java:10-60), the provider of the Utopia dimension
 * ({@code OreSpawnMain.DimensionID}, registered at OreSpawnMain.java:5039-5040; DECISIONS R13).
 *
 * <table>
 * <tr><th>1.7.10</th><th>port</th></tr>
 * <tr><td>constructor: {@code new BiomeGenUtopianPlains(BiomeUtopiaID)}, map colour 353825, name "Utopia",
 * 0.7 / 0.5 (:14-16)</td><td>{@code data/orespawn/worldgen/biome/utopia.json}; the map colour has no 1.21.1
 * field</td></tr>
 * <tr><td>{@code registerWorldChunkManager}: {@code WorldChunkManagerHell(MyPlains, 0.5)}, 0.7 / 0.5 again
 * (:51-55)</td><td>{@code data/orespawn/dimension/utopia.json}, fixed biome source</td></tr>
 * <tr><td>{@code createChunkGenerator}: {@code ChunkProviderOreSpawn(world, seed, true)} (:57-59)</td>
 * <td>{@link ChunkProviderOreSpawn}, type {@code orespawn:utopia}</td></tr>
 * <tr><td>{@code canRespawnHere} true (:22-24)</td><td>{@link #onPlayerRespawn}</td></tr>
 * <tr><td>{@code setWorldTime} (:26-49)</td><td>not ported, sleeping never reached it:
 * {@link com.swbr.orespawn.world.gen.LegacyWorldProvider}</td></tr>
 * <tr><td>{@code getDimensionName} (:18-20)</td><td>{@link #DIMENSION_NAME}; 1.21.1 shows the level id</td></tr>
 * <tr><td>sky, fog, clouds, stars: not overridden</td><td>{@code dimension_type} effects
 * {@code minecraft:overworld}; the biome's sky colour is 1.21.1's {@code calculateSkyColor(0.7)} = 7972607
 * (verhalten/world-03.md). PORT: 1.7.10 derived the sky colour from the temperature at the player's
 * position, 1.21.1 stores one value per biome.</td></tr>
 * </table>
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class WorldProviderOreSpawn {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "utopia");

    /** {@code orespawn:utopia}. Replaces every {@code OreSpawnMain.DimensionID} comparison. */
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, ID);

    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, ID);

    /** The dimension's only biome, {@code BiomeGenUtopianPlains} "Utopia" ({@code BiomeUtopiaID}, default 120). */
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, ID);

    /** {@code getDimensionName()} (:18-20). */
    public static final String DIMENSION_NAME = "Dimension-Utopia";

    /** {@code setTemperatureRainfall(0.7f, 0.5f)} (:15, :53); read by the surface pass. */
    public static final float TEMPERATURE = 0.7F;
    public static final float RAINFALL = 0.5F;

    private WorldProviderOreSpawn() {
    }

    /** {@code canRespawnHere() == true} (:22-24). */
    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerRespawnPositionEvent event) {
        LegacyWorldProvider.canRespawnHere(event, DIMENSION);
    }
}
