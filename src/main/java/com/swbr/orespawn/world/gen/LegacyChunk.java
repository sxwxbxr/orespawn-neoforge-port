package com.swbr.orespawn.world.gen;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Helpers (no original class) shared by the ports of {@code ChunkProviderOreSpawnN}: the
 * {@code new Chunk(world, Block[], byte[], x, z)} step, the populate and per-chunk seeds, and the base-height
 * queries 1.21.1 asks of a generator. One copy for all six generators (DECISIONS R21).
 *
 * <p>The 1.7.10 generators fill a flat block array of {@code 16 * 16 * k} entries (k = 256 for Utopia, Mining,
 * VillageMania and Crystal, 128 for Chaos) and hand it to the {@code Chunk} constructor, which reads index
 * {@code x * k * 16 | z * k | y} and skips {@code null} and air. The ports keep that array, so the index
 * arithmetic of the original generators - including the x/z transposition in their surface pass - stays
 * literally the same.
 *
 * <p>The world seed is not derived here: every generator takes it from {@code ChunkGenerator.createState}
 * (DECISIONS R21), which {@code ChunkMap} calls with the level seed before any chunk is generated - the value
 * 1.7.10 saw as {@code worldObj.getSeed()}.
 */
public final class LegacyChunk {

    /** Salt for the {@link Random} that stands in for {@code world.rand} (DECISIONS R18). */
    public static final long SALT_WORLD_RAND = 0x576F726C6452616EL;
    /** Salt for the {@link Random} that stands in for {@code Math.random()} (DECISIONS R18). */
    public static final long SALT_MATH_RANDOM = 0x4D61746852616E64L;
    /** Salt for a generator-owned {@link Random} that 1.7.10 seeded once and never reseeded. */
    public static final long SALT_GENERATOR_RANDOM = 0x47656E52616E646FL;

    private LegacyChunk() {
    }

    /**
     * The populate random of the 1.7.10 chunk providers (ChunkProviderOreSpawn.java:272-275,
     * ChunkProviderOreSpawn2.java:278-281, ChunkProviderOreSpawn3.java:273-276): two odd longs from
     * {@code new Random(worldSeed)}, then {@code setSeed(x * a + z * b ^ worldSeed)}.
     *
     * @param chunkX 1.7.10 chunk x of the populated chunk
     * @param chunkZ 1.7.10 chunk z of the populated chunk
     */
    public static Random populateRandom(final long worldSeed, final int chunkX, final int chunkZ) {
        final Random rand = new Random(worldSeed);
        final long i1 = rand.nextLong() / 2L * 2L + 1L;
        final long j1 = rand.nextLong() / 2L * 2L + 1L;
        rand.setSeed((long) chunkX * i1 + (long) chunkZ * j1 ^ worldSeed);
        return rand;
    }

    /**
     * A {@link Random} for one chunk, from the world seed, the chunk position and a salt. Replaces
     * {@code world.rand}, {@code Math.random()} and never-reseeded generator fields in chunk
     * generation (DECISIONS R18: chunk generation must be deterministic).
     */
    public static Random chunkRandom(final long worldSeed, final int chunkX, final int chunkZ, final long salt) {
        return new Random(chunkSeed(worldSeed, chunkX, chunkZ, salt));
    }

    /**
     * {@link #chunkRandom} as a {@link RandomSource}, for callees ported onto that type
     * ({@link ChunkOreGenerator}). {@code RandomSource.create(long)} is the {@code java.util.Random} LCG, so
     * {@code nextInt}/{@code nextFloat}/{@code nextDouble} draw the same sequence as {@link #chunkRandom} with the
     * same arguments.
     */
    public static RandomSource chunkRandomSource(final long worldSeed, final int chunkX, final int chunkZ, final long salt) {
        return RandomSource.create(chunkSeed(worldSeed, chunkX, chunkZ, salt));
    }

    private static long chunkSeed(final long worldSeed, final int chunkX, final int chunkZ, final long salt) {
        return worldSeed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L) ^ salt;
    }

    /**
     * Port of {@code new Chunk(world, Block[] blocks, byte[] meta, x, z)} as far as the OreSpawn
     * generators use it (all metadata 0): every non-null, non-air entry is written into its section.
     * Written like {@code NoiseBasedChunkGenerator.doFill}: sections are acquired once, written without
     * per-block locks, the two worldgen heightmaps updated alongside. Sections are released before this
     * returns, so later {@code ChunkAccess.setBlockState} calls (the maze, trees, ores) may lock again.
     */
    public static void fillChunk(final ChunkAccess chunk, final Block[] blocks) {
        final int k = blocks.length / 256;
        final Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        final Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        final Set<LevelChunkSection> acquired = Collections.newSetFromMap(new IdentityHashMap<>());
        try {
            for (int l = 0; l < 16; ++l) {
                for (int i1 = 0; i1 < 16; ++i1) {
                    for (int j1 = 0; j1 < k; ++j1) {
                        final Block block = blocks[l * k * 16 | i1 * k | j1];
                        if (block == null || block == Blocks.AIR || chunk.isOutsideBuildHeight(j1)) {
                            continue;
                        }
                        final LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(j1));
                        if (acquired.add(section)) {
                            section.acquire();
                        }
                        final BlockState state = block.defaultBlockState();
                        section.setBlockState(l, j1 & 15, i1, state, false);
                        oceanFloor.update(l, j1, i1, state);
                        worldSurface.update(l, j1, i1, state);
                    }
                }
            }
        } finally {
            for (final LevelChunkSection section : acquired) {
                section.release();
            }
        }
    }

    /**
     * {@link #fillChunk(ChunkAccess, Block[])} for the generators whose array holds block states
     * (Utopia, VillageMania, Mining: {@code null} and air entries are skipped the same way).
     */
    public static void fillChunk(final ChunkAccess chunk, final BlockState[] blocks) {
        final int k = blocks.length / 256;
        final Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        final Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        final Set<LevelChunkSection> acquired = Collections.newSetFromMap(new IdentityHashMap<>());
        try {
            for (int l = 0; l < 16; ++l) {
                for (int i1 = 0; i1 < 16; ++i1) {
                    for (int j1 = 0; j1 < k; ++j1) {
                        final BlockState state = blocks[l * k * 16 | i1 * k | j1];
                        if (state == null || state.isAir() || chunk.isOutsideBuildHeight(j1)) {
                            continue;
                        }
                        final LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(j1));
                        if (acquired.add(section)) {
                            section.acquire();
                        }
                        section.setBlockState(l, j1 & 15, i1, state, false);
                        oceanFloor.update(l, j1, i1, state);
                        worldSurface.update(l, j1, i1, state);
                    }
                }
            }
        } finally {
            for (final LevelChunkSection section : acquired) {
                section.release();
            }
        }
    }

    /**
     * {@code ChunkGenerator.getBaseHeight} over a legacy block array: one above the highest block of the
     * column that the heightmap type counts, or the bottom of the level. Features (maze, trees, ores)
     * are not included, the same as vanilla's noise-only answer.
     */
    public static int baseHeight(final Block[] blocks, final int x, final int z, final Heightmap.Types type,
                                 final LevelHeightAccessor level) {
        final int k = blocks.length / 256;
        final int base = (x & 15) * k * 16 | (z & 15) * k;
        final Predicate<BlockState> opaque = type.isOpaque();
        for (int y = k - 1; y >= 0; --y) {
            final Block block = blocks[base | y];
            if (block != null && opaque.test(block.defaultBlockState())) {
                return y + 1;
            }
        }
        return level.getMinBuildHeight();
    }

    /** {@code ChunkGenerator.getBaseColumn} over a legacy block array; array y is world y. */
    public static NoiseColumn baseColumn(final Block[] blocks, final int x, final int z, final LevelHeightAccessor height) {
        final int k = blocks.length / 256;
        final int base = (x & 15) * k * 16 | (z & 15) * k;
        final int minY = height.getMinBuildHeight();
        final BlockState[] column = new BlockState[height.getHeight()];
        for (int i = 0; i < column.length; ++i) {
            final int y = minY + i;
            final Block block = y >= 0 && y < k ? blocks[base | y] : null;
            column[i] = block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
        }
        return new NoiseColumn(minY, column);
    }
}
