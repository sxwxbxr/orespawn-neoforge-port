package com.swbr.orespawn.world.dimension.mining;

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
 * Port of {@code WorldProviderOreSpawn2} (WorldProviderOreSpawn2.java:10-54), the provider of the Mining
 * dimension ("Dimension-Extreme", {@code OreSpawnMain.DimensionID2}; DECISIONS R13).
 *
 * <table>
 * <tr><th>1.7.10</th><th>port</th></tr>
 * <tr><td>{@code registerWorldChunkManager}: {@code WorldChunkManagerHell(extremeHills, 0.01)}, then
 * temperature 0.8 / rainfall 0.01 (:20-24)</td><td>{@code data/orespawn/dimension/mining.json} with a fixed
 * biome source on {@code orespawn:mining}, whose JSON copies {@code windswept_hills} with 0.8/0.01
 * (verhalten/world-03.md). PORT: the original set these numbers on the <em>global</em> Extreme Hills object,
 * so overworld Extreme Hills lost their 0.2/0.3 as soon as the provider loaded; datapack biomes are
 * immutable and that side effect is not rebuilt (verhalten/world-03.md "Portierung").</td></tr>
 * <tr><td>{@code createChunkGenerator} (:51-53)</td><td>{@link ChunkProviderOreSpawn2}, type
 * {@code orespawn:mining}</td></tr>
 * <tr><td>{@code canRespawnHere} true (:16-18)</td><td>{@link #onPlayerRespawn}</td></tr>
 * <tr><td>{@code setWorldTime} (:26-49)</td><td>not ported, sleeping never reached it:
 * {@link com.swbr.orespawn.world.gen.LegacyWorldProvider}</td></tr>
 * <tr><td>{@code getDimensionName} (:12-14)</td><td>{@link #DIMENSION_NAME}; 1.21.1 shows the level id</td></tr>
 * <tr><td>sky, fog, clouds: not overridden</td><td>{@code dimension_type} effects {@code minecraft:overworld}</td></tr>
 * </table>
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class WorldProviderOreSpawn2 {

    /** {@code orespawn:mining}. Replaces every {@code OreSpawnMain.DimensionID2} comparison. */
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "mining"));

    public static final ResourceKey<DimensionType> DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "mining"));

    /** The dimension's only biome, the Extreme Hills copy. */
    public static final ResourceKey<Biome> BIOME =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "mining"));

    /** {@code getDimensionName()} (:12-14). */
    public static final String DIMENSION_NAME = "Dimension-Extreme";

    /** {@code setTemperatureRainfall(0.8f, 0.01f)} (:22); read by the surface pass. */
    public static final float TEMPERATURE = 0.8F;
    public static final float RAINFALL = 0.01F;

    private WorldProviderOreSpawn2() {
    }

    /** {@code canRespawnHere() == true} (:16-18). */
    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerRespawnPositionEvent event) {
        LegacyWorldProvider.canRespawnHere(event, DIMENSION);
    }
}
