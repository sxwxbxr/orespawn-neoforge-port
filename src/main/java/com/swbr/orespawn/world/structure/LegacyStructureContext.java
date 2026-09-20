package com.swbr.orespawn.world.structure;

import com.swbr.orespawn.world.gen.OreSpawnWorldOres;
import java.util.Random;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * What a {@link LegacyStructureRegistry.Dispatcher} gets instead of the {@code (World, Random, chunkX, chunkZ)} of
 * {@code OreSpawnWorld.generate}.
 *
 * <p>{@link #random()} is the stand-in for both the FML {@code random} and {@code world.rand}: a
 * {@code java.util.Random} seeded from the world seed and the chunk position exactly as FML 1.7.10 seeded the
 * generator random ({@link OreSpawnWorldOres#fmlChunkRandom}), then salted with the dispatcher id so that the
 * structure rolls are not the same numbers the decoration of the same chunk draws. Deterministic per world seed
 * and chunk (DECISIONS R18, "Math.random" and "world.rand": seed plus ChunkPos).
 *
 * @param chunkX block x of the chunk corner, as the original's {@code chunkX * 16}
 * @param chunkZ block z of the chunk corner
 */
public record LegacyStructureContext(Structure.GenerationContext generation, SurfaceProbe probe, Random random,
                                     int chunkX, int chunkZ, long baseSeed) {

    public static LegacyStructureContext create(final Structure.GenerationContext generation, final String dispatcherId) {
        final long fml = OreSpawnWorldOres.fmlChunkRandom(generation.seed(), generation.chunkPos().x,
                generation.chunkPos().z).nextLong();
        final long base = fml ^ (long) dispatcherId.hashCode() * 0x9E3779B97F4A7C15L;
        return new LegacyStructureContext(generation, new SurfaceProbe(generation), new Random(base),
                generation.chunkPos().getMinBlockX(), generation.chunkPos().getMinBlockZ(), base);
    }

    /**
     * Seed of the {@code index}-th piece. Independent of how many numbers the dispatcher drew, so a rule outcome read
     * from a cache and one computed afresh build the same structure.
     */
    public long pieceSeed(final int index) {
        return new Random(this.baseSeed + 0x2545F4914F6CDD1DL * (index + 1)).nextLong();
    }
}
