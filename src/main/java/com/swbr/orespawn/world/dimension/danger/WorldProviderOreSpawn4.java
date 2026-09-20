package com.swbr.orespawn.world.dimension.danger;

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
 * Port of {@code WorldProviderOreSpawn4} (WorldProviderOreSpawn4.java:10-61), the provider of the Islands
 * dimension ("Dimension-Islands", {@code OreSpawnMain.DimensionID4}; DECISIONS R13 id {@code orespawn:danger},
 * which the Unstable Ant leads to).
 *
 * <table>
 * <tr><th>1.7.10</th><th>port</th></tr>
 * <tr><td>constructor: {@code new BiomeGenUtopianPlains(BiomeIslandsID)}, colour 353825, name "Islands",
 * 0.7/0.5 (:14-16); {@code registerWorldChunkManager}: {@code setIslandCreatures()},
 * {@code WorldChunkManagerHell(MyPlains, 0.01)}, then 0.8/0.01 (:26-31)</td>
 * <td>{@code data/orespawn/dimension/danger.json}, fixed biome source on {@code orespawn:islands}
 * (temperature 0.8, downfall 0.01 - the second call wins). {@code setIslandCreatures}
 * replaces all four spawn lists (BiomeGenUtopianPlains.java:62-118); the biome JSON ships them empty and
 * {@code world.spawn.ConfigSpawnsBiomeModifier} fills {@code orespawn:islands} from the W12 spawn table.</td></tr>
 * <tr><td>{@code createChunkGenerator} (:58-60)</td><td>{@link ChunkProviderOreSpawn4}, type {@code orespawn:danger}</td></tr>
 * <tr><td>{@code canRespawnHere} true (:22-24)</td><td>{@link #onPlayerRespawn}</td></tr>
 * <tr><td>{@code setWorldTime} (:33-56)</td><td>not ported, sleeping never reached it:
 * {@link com.swbr.orespawn.world.gen.LegacyWorldProvider}</td></tr>
 * <tr><td>{@code getDimensionName} (:18-20)</td><td>{@link #DIMENSION_NAME}</td></tr>
 * </table>
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class WorldProviderOreSpawn4 {

    /** {@code orespawn:danger}. Replaces every {@code OreSpawnMain.DimensionID4} comparison. */
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "danger"));

    public static final ResourceKey<DimensionType> DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "danger"));

    /** The dimension's only biome, "Islands" ({@code BiomeIslandsID}). */
    public static final ResourceKey<Biome> BIOME =
            ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "islands"));

    /** {@code getDimensionName()} (:18-20). */
    public static final String DIMENSION_NAME = "Dimension-Islands";

    /** {@code setTemperatureRainfall(0.8f, 0.01f)} (:29), which overrides the 0.7/0.5 of :15. */
    public static final float TEMPERATURE = 0.8F;
    public static final float RAINFALL = 0.01F;

    private WorldProviderOreSpawn4() {
    }

    /** {@code canRespawnHere() == true} (:22-24). */
    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerRespawnPositionEvent event) {
        LegacyWorldProvider.canRespawnHere(event, DIMENSION);
    }
}
