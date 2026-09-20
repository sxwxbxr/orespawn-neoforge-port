package com.swbr.orespawn.world.spawn;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

/**
 * The spawn checks of the chunk generation pass, made to match 1.7.10.
 *
 * <p><b>Finding (javap on {@code reference/jar/mcp/client-1.7.10.jar}, class {@code aho} =
 * {@code SpawnerAnimals}, method {@code a(ahb, ahu, int, int, int, int, Random)} =
 * {@code performWorldGenSpawning}, joined.srg:9941):</b> the method reads
 * {@code biome.getSpawnableList(EnumCreatureType.creature)} ({@code sx.b}, joined.srg:7925), picks an entry, and for
 * each group member calls only {@code canCreatureTypeSpawnAtLocation} ({@code aho.a(sx, ahb, int, int, int)},
 * joined.srg:9943), then {@code newInstance}, {@code setLocationAndAngles}, {@code spawnEntityInWorld} and
 * {@code onSpawnWithEgg}. There is <em>no</em> call to {@code getCanSpawnHere} ({@code func_70601_bi}, obfuscated
 * {@code by()} on {@code sw}/{@code td}/{@code wf}, joined.srg:20127/20196/20655) and no collision check. The
 * vanilla jar is unpatched; whether Forge 10.13 patched {@code performWorldGenSpawning} cannot be checked in this
 * repo (no Forge 1.7.10 jar or patch set here) - unbelegt.
 *
 * <p>1.21.1 {@code NaturalSpawner.spawnMobsForChunkGeneration} (NaturalSpawner.java:347-420) asks
 * {@code SpawnPlacements.isSpawnPositionOk} (the equivalent of {@code canCreatureTypeSpawnAtLocation}, kept),
 * {@code noCollision} (kept, see below), {@code SpawnPlacements.checkSpawnRules(..., CHUNK_GENERATION, ...)} and
 * {@code EventHooks.checkSpawnPosition} = {@code Mob.checkSpawnRules && Mob.checkSpawnObstruction}. The last two
 * are where the ports of {@code getCanSpawnHere} live, so both are forced to succeed for OreSpawn entity types
 * during chunk generation, through the two NeoForge events that exist for exactly this. Natural spawning
 * ({@code MobSpawnType.NATURAL}) keeps every predicate, as 1.7.10 {@code findChunksForSpawning} did.
 *
 * <p>PORT: the {@code noCollision} test of 1.21.1 (NaturalSpawner.java:376) stays; it runs before the events and
 * has no hook. 1.7.10 spawned into blocks and let the entity push itself out.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class ChunkGenerationSpawns {

    private ChunkGenerationSpawns() {}

    /** {@code SpawnPlacements.checkSpawnRules} with {@code CHUNK_GENERATION}: 1.7.10 asked no predicate here. */
    @SubscribeEvent
    public static void onSpawnPlacementCheck(final MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() == MobSpawnType.CHUNK_GENERATION && isOreSpawn(event.getEntityType())) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
        }
    }

    /** {@code Mob.checkSpawnRules} and {@code checkSpawnObstruction} with {@code CHUNK_GENERATION}: same reason. */
    @SubscribeEvent
    public static void onPositionCheck(final MobSpawnEvent.PositionCheck event) {
        if (event.getSpawnType() == MobSpawnType.CHUNK_GENERATION && isOreSpawn(event.getEntity().getType())) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
        }
    }

    private static boolean isOreSpawn(final EntityType<?> type) {
        return OreSpawn.MOD_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace());
    }
}
