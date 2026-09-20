package com.swbr.orespawn.world.dimension;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.level.ChunkPos;

/**
 * A small, thread-safe least-recently-used cache of generated terrain arrays per chunk, for
 * {@code ChunkGenerator.getBaseColumn} and {@code getBaseHeight} of the OreSpawn dimension generators (DECISIONS R26:
 * allowed, a pure speed-up with the same blocks).
 *
 * <p>Why: the structure passes ({@code StructureRecording}, {@code GenericDungeon}) ask {@code getBaseColumn} for every
 * column of a large footprint, and each call used to run the whole terrain generation of that column's chunk again -
 * about 2600 full runs for one King or Queen altar (BUGHUNT2 section 3, first disputed point).
 *
 * <p>The loader must be a pure function of the chunk coordinates (it is: the noise tables are fixed per seed and each
 * chunk's own {@code Random} is seeded from its coordinates), so a value computed twice by two racing worker threads
 * is identical and either copy may be kept. Loading runs outside the lock; the cached arrays are shared and must only
 * be read. One cache belongs to one seed's noise object, so a new seed starts empty.
 *
 * @param <T> the terrain array type of the generator
 */
public final class ChunkTerrainCache<T> {

    /** Computes the terrain of one chunk. */
    @FunctionalInterface
    public interface Loader<T> {
        T load(int chunkX, int chunkZ);
    }

    private final Map<Long, T> entries;

    /** @param capacity chunks kept; a 256-high chunk array is roughly 260 KB of references */
    public ChunkTerrainCache(final int capacity) {
        this.entries = new LinkedHashMap<>(capacity + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final Map.Entry<Long, T> eldest) {
                return this.size() > capacity;
            }
        };
    }

    /** The cached terrain of chunk ({@code chunkX}, {@code chunkZ}), loading it on a miss. Never mutate the result. */
    public T get(final int chunkX, final int chunkZ, final Loader<T> loader) {
        final long key = ChunkPos.asLong(chunkX, chunkZ);
        synchronized (this.entries) {
            final T cached = this.entries.get(key);
            if (cached != null) {
                return cached;
            }
        }
        final T loaded = loader.load(chunkX, chunkZ);
        synchronized (this.entries) {
            final T raced = this.entries.putIfAbsent(key, loaded);
            return raced != null ? raced : loaded;
        }
    }
}
