package com.swbr.orespawn.world.dimension.danger;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.structure.LegacyStructurePass;
import com.swbr.orespawn.world.util.FastBlocks;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Port of {@code ChunkProviderOreSpawn4} (ChunkProviderOreSpawn4.java:13-225), the generator of the Islands
 * dimension {@code orespawn:danger} ("Dimension-Islands", DECISIONS R13; verhalten/world-03.md
 * "ChunkProviderOreSpawn4"). Registered under the chunk generator type {@code orespawn:danger}.
 *
 * <p>A flat floor - bedrock at Y 0, dirt 1-6, grass 7 - with 1..10 scraggly apple trees per chunk, cut at the
 * chunk border. No noise, no ores, no caves, and {@code populate} only reseeds its random: no decorator, no
 * initial spawns. The moving islands and the D4 structures are {@code OreSpawnWorld} (W08, W12), whose
 * {@code D4BigSpaceCheck(world, x, 7, z)} relies on the grass layer at exactly Y 7.
 */
public class ChunkProviderOreSpawn4 extends ChunkGenerator {

    public static final MapCodec<ChunkProviderOreSpawn4> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource))
            .apply(instance, instance.stable(ChunkProviderOreSpawn4::new)));

    /** {@code cachedBlockIDs} (:21-35): only indices 0..7 are set, metadata all 0 (:22). */
    private static final int LAYERS = 8;

    /** Captured in {@link #createState}; see {@code ChunkProviderOreSpawn2} for why. */
    private volatile Long seed;

    public ChunkProviderOreSpawn4(final BiomeSource biomeSource) {
        super(biomeSource);
    }

    /** {@code cachedBlockIDs[j]} (:25-35). */
    private static BlockState layer(final int j) {
        if (j == 0) {
            return Blocks.BEDROCK.defaultBlockState();
        } else if (j == 7) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        } else {
            return Blocks.DIRT.defaultBlockState();
        }
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(final HolderLookup<StructureSet> structureSetLookup,
                                                    final RandomState randomState, final long seed) {
        this.seed = seed;
        return super.createState(structureSetLookup, randomState, seed);
    }

    private long seed() {
        final Long s = this.seed;
        if (s != null) {
            return s;
        }
        // PORT: only reachable if a chunk is requested before ChunkMap created the generator state.
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getWorldData().worldGenOptions().seed() : 0L;
    }

    /**
     * {@code provideChunk} (:42-64): the eight layers, then {@code addScragglyTrees} on the same chunk.
     *
     * <p>PORT (DECISIONS R18, seed plus chunk position): the original's {@code this.random} was seeded in the
     * constructor and reseeded only in {@code populate} (:73-76), so tree positions and shapes depended on
     * the order chunks were generated in (verhalten/world-03.md "Determinismus"). The port seeds a fresh
     * random per chunk with exactly that {@code populate} formula for this chunk.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final Blender blender, final RandomState randomState,
                                                        final StructureManager structureManager, final ChunkAccess chunk) {
        final Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        final Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int k = 0; k < LAYERS; ++k) {
            if (chunk.isOutsideBuildHeight(k)) {
                continue;
            }
            final BlockState block = layer(k);
            final LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(k));
            for (int i1 = 0; i1 < 16; ++i1) {
                for (int j1 = 0; j1 < 16; ++j1) {
                    section.setBlockState(i1, k & 15, j1, block, false);
                    oceanFloor.update(i1, k, j1, block);
                    worldSurface.update(i1, k, j1, block);
                }
            }
        }
        final int par1 = chunk.getPos().x;
        final int par2 = chunk.getPos().z;
        final long worldSeed = this.seed();
        final RandomSource random = new LegacyRandomSource(worldSeed);
        final long i1 = random.nextLong() / 2L * 2L + 1L;
        final long j1 = random.nextLong() / 2L * 2L + 1L;
        random.setSeed((long) par1 * i1 + (long) par2 * j1 ^ worldSeed);
        addScragglyTrees(random, par1 * 16, par2 * 16, chunk);
        // chunk.generateSkylightMap() (:62): the 1.21.1 light engine lights the chunk in its own stage.
        return CompletableFuture.completedFuture(chunk);
    }

    /** {@code addScragglyTrees} (:114-135). */
    public static void addScragglyTrees(final RandomSource random, final int chunkX, final int chunkZ,
                                        final ChunkAccess chunk) {
        int howmany = 1 + random.nextInt(10);
        final int lessLag = TweakStats.LessLag();
        if (lessLag == 1) {
            howmany /= 2;
        }
        if (lessLag == 2) {
            howmany /= 4;
        }
        if (howmany == 0) {
            return;
        }
        for (int i = 0; i < howmany; ++i) {
            final int posX = 2 + chunkX + random.nextInt(12);
            final int posZ = 2 + chunkZ + random.nextInt(12);
            for (int posY = 20; posY > 2; --posY) {
                if (FastBlocks.getBlockInChunk(chunk, posX, posY - 1, posZ).is(Blocks.GRASS_BLOCK)) {
                    scragglyTreeWithBranches(random, posX, posY, posZ, chunk);
                    break;
                }
            }
        }
    }

    /** {@code makeScragglyBranch} (:137-179). */
    private static void makeScragglyBranch(final RandomSource random, int x, int y, int z, final int len,
                                           final int biasx, final int biasz, final ChunkAccess chunk) {
        for (int k = 0; k < len; ++k) {
            int ix = random.nextInt(2) - random.nextInt(2) + biasx;
            int iz = random.nextInt(2) - random.nextInt(2) + biasz;
            if (ix > 1) {
                ix = 1;
            }
            if (ix < -1) {
                ix = -1;
            }
            if (iz > 1) {
                iz = 1;
            }
            if (iz < -1) {
                iz = -1;
            }
            final int iy = random.nextInt(3) > 0 ? 1 : 0;
            x += ix;
            z += iz;
            y += iy;
            BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y, z);
            if (!isTreeSpace(bid)) {
                return;
            }
            FastBlocks.setBlockInChunk(chunk, x, y, z, log());
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (random.nextInt(2) == 1) {
                        bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                        if (bid.isAir()) {
                            FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves());
                        }
                    }
                }
            }
            if (random.nextInt(2) == 1) {
                bid = FastBlocks.getBlockInChunk(chunk, x, y + 1, z);
                if (bid.isAir()) {
                    FastBlocks.setBlockInChunk(chunk, x, y + 1, z, leaves());
                }
            }
        }
    }

    /** {@code ScragglyTreeWithBranches} (:181-224). */
    private static void scragglyTreeWithBranches(final RandomSource random, int x, int y, int z, final ChunkAccess chunk) {
        final int i = 1 + random.nextInt(3);
        final int j = i + random.nextInt(12);
        for (int k = 0; k < i; ++k) {
            final BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y + k, z);
            if (k >= 1 && !isTreeSpace(bid)) {
                return;
            }
            FastBlocks.setBlockInChunk(chunk, x, y + k, z, log());
        }
        y += i - 1;
        for (int k = i; k < j; ++k) {
            final int ix = random.nextInt(2) - random.nextInt(2);
            final int iz = random.nextInt(2) - random.nextInt(2);
            final int iy = random.nextInt(4) > 0 ? 1 : 0;
            x += ix;
            z += iz;
            y += iy;
            BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y, z);
            if (!isTreeSpace(bid)) {
                break;
            }
            FastBlocks.setBlockInChunk(chunk, x, y, z, log());
            if (random.nextInt(4) == 1) {
                // Argument order is evaluation order: length, then x bias, then z bias (:205).
                final int len = random.nextInt(1 + j - k);
                final int biasx = random.nextInt(2) - random.nextInt(2);
                final int biasz = random.nextInt(2) - random.nextInt(2);
                makeScragglyBranch(random, x, y, z, len, biasx, biasz, chunk);
            }
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (random.nextInt(2) == 1) {
                        bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                        if (bid.isAir()) {
                            FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves());
                        }
                    }
                }
            }
            if (random.nextInt(2) == 1) {
                bid = FastBlocks.getBlockInChunk(chunk, x, y + 1, z);
                if (bid.isAir()) {
                    FastBlocks.setBlockInChunk(chunk, x, y + 1, z, leaves());
                }
            }
        }
    }

    /**
     * {@code bid == Blocks.air || bid == Blocks.log || bid == MyAppleLeaves}. {@code Blocks.log} matched all four
     * 1.7.10 wood types; only oak is ever placed here, so oak is the only log that can be met.
     */
    private static boolean isTreeSpace(final BlockState bid) {
        return bid.isAir() || bid.is(Blocks.OAK_LOG) || bid.is(ModBlocks.LEAVES_APPLE.get());
    }

    /** {@code Blocks.log}, metadata 0: oak, upright. */
    private static BlockState log() {
        return Blocks.OAK_LOG.defaultBlockState();
    }

    /** {@code OreSpawnMain.MyAppleLeaves}, metadata 0. */
    private static BlockState leaves() {
        return ModBlocks.LEAVES_APPLE.get().defaultBlockState();
    }

    /**
     * {@code populate} (:70-77) only reseeds the random; nothing is placed. The vanilla pass still runs: the
     * {@code orespawn:islands} biome lists no features, so it only places structure pieces - which is where the
     * W12 ports of the {@code OreSpawnWorld} D4 structures land for this dimension. Suppressing it would
     * silently drop them. {@link LegacyStructurePass} runs last (DECISIONS R24).
     */
    @Override
    public void applyBiomeDecoration(final net.minecraft.world.level.WorldGenLevel level, final ChunkAccess chunk,
                                     final StructureManager structureManager) {
        super.applyBiomeDecoration(level, chunk, structureManager);
        // Islands branch of OreSpawnWorld.generate (:119-175); the D4 builds come as structures through super.
        com.swbr.orespawn.world.OreSpawnWorld.generate(level, chunk.getPos().x, chunk.getPos().z);
        LegacyStructurePass.apply(level, this, chunk.getPos());
    }

    /** {@code populate} never calls {@code performWorldGenSpawning}; {@code FlatLevelSource} does the same. */
    @Override
    public void spawnOriginalMobs(final WorldGenRegion level) {
    }

    @Override
    public void buildSurface(final WorldGenRegion level, final StructureManager structureManager,
                             final RandomState random, final ChunkAccess chunk) {
    }

    @Override
    public void applyCarvers(final WorldGenRegion level, final long seed, final RandomState random,
                             final BiomeManager biomeManager, final StructureManager structureManager,
                             final ChunkAccess chunk, final GenerationStep.Carving step) {
    }

    /** {@code new Chunk(world, x, z)}: 256 blocks high. */
    @Override
    public int getGenDepth() {
        return 256;
    }

    /** No water anywhere; 63 is the 1.7.10 {@code World} default the rest of the game reads. */
    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    /** The eight layers only; trees are not part of the base height, as with vanilla generators. */
    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types type, final LevelHeightAccessor level,
                             final RandomState random) {
        for (int i = Math.min(LAYERS, level.getMaxBuildHeight()) - 1; i >= Math.max(0, level.getMinBuildHeight()); --i) {
            if (type.isOpaque().test(layer(i))) {
                return i + 1;
            }
        }
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor height, final RandomState random) {
        final BlockState[] column = new BlockState[height.getHeight()];
        for (int i = 0; i < column.length; ++i) {
            final int y = height.getMinBuildHeight() + i;
            column[i] = y >= 0 && y < LAYERS ? layer(y) : Blocks.AIR.defaultBlockState();
        }
        return new NoiseColumn(height.getMinBuildHeight(), column);
    }

    @Override
    public void addDebugScreenInfo(final List<String> info, final RandomState random, final BlockPos pos) {
    }
}
