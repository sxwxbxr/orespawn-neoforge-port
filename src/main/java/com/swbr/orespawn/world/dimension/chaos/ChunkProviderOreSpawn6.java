package com.swbr.orespawn.world.dimension.chaos;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.dimension.ChunkTerrainCache;
import com.swbr.orespawn.world.gen.BiomeDecorator;
import com.swbr.orespawn.world.gen.BiomeGenBase;
import com.swbr.orespawn.world.gen.ChunkOreGenerator;
import com.swbr.orespawn.world.gen.LegacyChunk;
import com.swbr.orespawn.world.gen.LegacyWorld;
import com.swbr.orespawn.world.gen.NoiseGeneratorOctaves;
import com.swbr.orespawn.world.structure.LegacyStructurePass;
import com.swbr.orespawn.world.util.FastBlocks;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Port of {@code ChunkProviderOreSpawn6} (ChunkProviderOreSpawn6.java:15-457), the generator of the
 * Chaos dimension (verhalten/world-03.md "ChunkProviderOreSpawn6"). Codec type {@code orespawn:chaos}.
 *
 * <p>The vanilla 1.7.10 {@code ChunkProviderHell} with the density test inverted: density above zero
 * is <em>air</em>, below it stone. The profile makes the bottom and top cells strongly positive, so the
 * result is floating stone layers roughly between Y 24 and 111 with void underneath. No bedrock, no
 * lava sea ({@code b2 = 32} is declared and never read, :65), no water from the surface pass (its water
 * test compares against {@code Blocks.air}, which the column block never is, :154-156). The surface
 * pass puts grass on dirt only in Y 60..65 where the soul-sand or gravel noise is positive. After the
 * surface come the OreSpawn ores ({@code ChunkOreGenerator}) and the scraggly apple trees.
 *
 * <p>Mapping onto 1.21.1: {@code provideChunk} (:187-198) is {@link #fillFromNoise}; {@code populate}
 * (:299-306) is {@link #applyBiomeDecoration} (the whole Chaos decorator as the shared 1.7.10 Java
 * {@link BiomeDecorator}, DECISIONS R21; {@code worldgen/biome/chaos.json} lists no feature) and
 * {@link #spawnOriginalMobs}.
 *
 * <p>Random sources, all PORT (DECISIONS R18): {@code worldObj.rand} (ores, tree chance and count,
 * decorator, initial spawns) and the generator field {@code random} (tree positions and shapes, seeded
 * once from the world seed and never reseeded, so it depended on chunk order) are seeded per chunk.
 * {@code hellRNG} keeps its original per-chunk seed (:188).
 *
 * <p>PORT: {@code TerrainGen.getModdedNoiseGenerators} (:52-60) has no counterpart (see
 * {@code ChunkProviderOreSpawn5}). {@code netherNoiseGen6}/{@code 7} are drawn last and only feed
 * {@code noiseData4}/{@code 5}, which never reach the density (:209-210, :229-255); neither is built.
 */
public final class ChunkProviderOreSpawn6 extends ChunkGenerator {

    public static final MapCodec<ChunkProviderOreSpawn6> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource))
            .apply(instance, ChunkProviderOreSpawn6::new));

    /** Noise tables of the world seed, built in {@link #createState}; immutable, replaced as a whole. */
    private volatile Noises noises;

    public ChunkProviderOreSpawn6(final BiomeSource biomeSource) {
        super(biomeSource);
    }

    /** The constructor's generators (:45-51), drawn from {@code hellRNG = new Random(worldSeed)} in order. */
    private static final class Noises {
        final long seed;
        final NoiseGeneratorOctaves netherNoiseGen1;
        final NoiseGeneratorOctaves netherNoiseGen2;
        final NoiseGeneratorOctaves netherNoiseGen3;
        final NoiseGeneratorOctaves slowsandGravelNoiseGen;
        final NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
        /** fix2 (R26): chunk terrain arrays for {@code getBaseHeight} and {@code getBaseColumn}. */
        final ChunkTerrainCache<Block[]> baseChunks = new ChunkTerrainCache<>(32);

        Noises(final long seed) {
            this.seed = seed;
            final Random hellRNG = new Random(seed);
            this.netherNoiseGen1 = new NoiseGeneratorOctaves(hellRNG, 16);
            this.netherNoiseGen2 = new NoiseGeneratorOctaves(hellRNG, 16);
            this.netherNoiseGen3 = new NoiseGeneratorOctaves(hellRNG, 8);
            this.slowsandGravelNoiseGen = new NoiseGeneratorOctaves(hellRNG, 4);
            this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(hellRNG, 4);
        }
    }

    /**
     * The world seed, as {@code worldObj.getSeed()} in the original constructor: {@code ChunkMap} calls this with
     * the level seed before any chunk is generated. One seed path for all six generators (DECISIONS R21).
     */
    @Override
    public ChunkGeneratorStructureState createState(final HolderLookup<StructureSet> structureSetLookup,
                                                    final RandomState randomState, final long seed) {
        this.noises = new Noises(seed);
        return super.createState(structureSetLookup, randomState, seed);
    }

    private Noises noises() {
        Noises n = this.noises;
        if (n == null) {
            // PORT: only reachable if a chunk is requested before ChunkMap created the generator state.
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            n = new Noises(server != null ? server.getWorldData().worldGenOptions().seed() : 0L);
            this.noises = n;
        }
        return n;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /** {@code provideChunk(x, z)} (:187-198). */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final Blender blender, final RandomState randomState,
                                                        final StructureManager structureManager, final ChunkAccess chunk) {
        final int p1 = chunk.getPos().x;
        final int p2 = chunk.getPos().z;
        final Noises n = this.noises();
        final long seed = n.seed;
        final Random hellRNG = new Random(p1 * 341873128712L + p2 * 132897987541L);
        final Block[] ablock = this.generateTerrain(n, p1, p2, hellRNG);
        LegacyChunk.fillChunk(chunk, ablock);
        final RandomSource worldRand = LegacyChunk.chunkRandomSource(seed, p1, p2, LegacyChunk.SALT_WORLD_RAND);
        final RandomSource random = LegacyChunk.chunkRandomSource(seed, p1, p2, LegacyChunk.SALT_GENERATOR_RANDOM);
        ChunkOreGenerator.generateOresInChunk(worldRand, p1 * 16, p2 * 16, chunk);
        this.addScragglyTrees(worldRand, random, p1 * 16, p2 * 16, chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    /** Terrain and surface of one chunk: {@code func_147419_a} then {@code replaceBiomeBlocks} (:189-192). */
    private Block[] generateTerrain(final Noises n, final int p1, final int p2, final Random hellRNG) {
        final Block[] ablock = new Block[32768];
        this.generateBlocks(n, p1, p2, ablock);
        this.replaceBiomeBlocks(n, p1, p2, ablock, hellRNG);
        return ablock;
    }

    /** {@code func_147419_a} (:63-115): density above zero is air, otherwise stone. */
    private void generateBlocks(final Noises n, final int p1, final int p2, final Block[] p3) {
        final byte b0 = 4;
        final int k = b0 + 1;
        final byte b3 = 17;
        final int l = b0 + 1;
        final double[] noiseField = this.initializeNoiseField(n, p1 * b0, 0, p2 * b0, k, b3, l);
        for (int i1 = 0; i1 < b0; ++i1) {
            for (int j1 = 0; j1 < b0; ++j1) {
                for (int k2 = 0; k2 < 16; ++k2) {
                    final double d0 = 0.125;
                    double d2 = noiseField[((i1 + 0) * l + j1 + 0) * b3 + k2 + 0];
                    double d3 = noiseField[((i1 + 0) * l + j1 + 1) * b3 + k2 + 0];
                    double d4 = noiseField[((i1 + 1) * l + j1 + 0) * b3 + k2 + 0];
                    double d5 = noiseField[((i1 + 1) * l + j1 + 1) * b3 + k2 + 0];
                    final double d6 = (noiseField[((i1 + 0) * l + j1 + 0) * b3 + k2 + 1] - d2) * d0;
                    final double d7 = (noiseField[((i1 + 0) * l + j1 + 1) * b3 + k2 + 1] - d3) * d0;
                    final double d8 = (noiseField[((i1 + 1) * l + j1 + 0) * b3 + k2 + 1] - d4) * d0;
                    final double d9 = (noiseField[((i1 + 1) * l + j1 + 1) * b3 + k2 + 1] - d5) * d0;
                    for (int l2 = 0; l2 < 8; ++l2) {
                        final double d10 = 0.25;
                        double d11 = d2;
                        double d12 = d3;
                        final double d13 = (d4 - d2) * d10;
                        final double d14 = (d5 - d3) * d10;
                        for (int i2 = 0; i2 < 4; ++i2) {
                            int j2 = i2 + i1 * 4 << 11 | 0 + j1 * 4 << 7 | k2 * 8 + l2;
                            final short short1 = 128;
                            final double d15 = 0.25;
                            double d16 = d11;
                            final double d17 = (d12 - d11) * d15;
                            for (int k3 = 0; k3 < 4; ++k3) {
                                Block block = Blocks.STONE;
                                if (d16 > 0.0) {
                                    block = null;
                                }
                                p3[j2] = block;
                                j2 += short1;
                                d16 += d17;
                            }
                            d11 += d13;
                            d12 += d14;
                        }
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                        d5 += d9;
                    }
                }
            }
        }
    }

    /**
     * {@code initializeNoiseField} (:200-293): 5x17x5. The per-column {@code d4}/{@code d6} (:229-255)
     * are computed from {@code noiseData4}/{@code 5} and never used, so they are left out; the bottom
     * ramp with {@code d5 = 0.0} is dead ({@code j2 < 0} never holds) and kept as written.
     */
    private double[] initializeNoiseField(final Noises n, final int p2, final int p3, final int p4, final int p5,
                                          final int p6, final int p7) {
        int k1 = 0;
        final double[] adouble1 = new double[p6];
        final double[] field = new double[p5 * p6 * p7];
        final double d0 = 684.412;
        final double d2 = 2053.236;
        final double[] noiseData1 = n.netherNoiseGen3.generateNoiseOctaves(null, p2, p3, p4, p5, p6, p7, d0 / 80.0, d2 / 60.0, d0 / 80.0);
        final double[] noiseData2 = n.netherNoiseGen1.generateNoiseOctaves(null, p2, p3, p4, p5, p6, p7, d0, d2, d0);
        final double[] noiseData3 = n.netherNoiseGen2.generateNoiseOctaves(null, p2, p3, p4, p5, p6, p7, d0, d2, d0);
        for (int i2 = 0; i2 < p6; ++i2) {
            adouble1[i2] = Math.cos(i2 * 3.141592653589793 * 6.0 / p6) * 2.0;
            double d3 = i2;
            if (i2 > p6 / 2) {
                d3 = p6 - 1 - i2;
            }
            if (d3 < 4.0) {
                d3 = 4.0 - d3;
                adouble1[i2] -= d3 * d3 * d3 * 10.0;
            }
        }
        for (int i2 = 0; i2 < p5; ++i2) {
            for (int k2 = 0; k2 < p7; ++k2) {
                final double d5 = 0.0;
                for (int j2 = 0; j2 < p6; ++j2) {
                    double d7;
                    final double d8 = adouble1[j2];
                    final double d9 = noiseData2[k1] / 512.0;
                    final double d10 = noiseData3[k1] / 512.0;
                    final double d11 = (noiseData1[k1] / 10.0 + 1.0) / 2.0;
                    if (d11 < 0.0) {
                        d7 = d9;
                    } else if (d11 > 1.0) {
                        d7 = d10;
                    } else {
                        d7 = d9 + (d10 - d9) * d11;
                    }
                    d7 -= d8;
                    if (j2 > p6 - 4) {
                        final double d12 = (j2 - (p6 - 4)) / 3.0f;
                        d7 = d7 * (1.0 - d12) + -10.0 * d12;
                    }
                    if (j2 < d5) {
                        double d12 = (d5 - j2) / 4.0;
                        if (d12 < 0.0) {
                            d12 = 0.0;
                        }
                        if (d12 > 1.0) {
                            d12 = 1.0;
                        }
                        d7 = d7 * (1.0 - d12) + -10.0 * d12;
                    }
                    field[k1] = d7;
                    ++k1;
                }
            }
        }
        return field;
    }

    /**
     * {@code replaceBiomeBlocks} (:117-181). Both {@code hellRNG.nextInt(5)} of the edge test run only
     * while the first comparison holds ({@code &&}); the column index transposes x and z against the
     * {@code Chunk} constructor, as vanilla did. Kept: {@code block == Blocks.air} can never be true, so no
     * water.
     */
    private void replaceBiomeBlocks(final Noises n, final int p1, final int p2, final Block[] p3, final Random hellRNG) {
        final byte b0 = 64;
        final double d0 = 0.03125;
        final double[] slowsandNoise = n.slowsandGravelNoiseGen.generateNoiseOctaves(null, p1 * 16, p2 * 16, 0, 16, 16, 1, d0, d0, 1.0);
        final double[] gravelNoise = n.slowsandGravelNoiseGen.generateNoiseOctaves(null, p1 * 16, 109, p2 * 16, 16, 1, 16, d0, 1.0, d0);
        final double[] netherrackExclusivityNoise = n.netherrackExculsivityNoiseGen.generateNoiseOctaves(null, p1 * 16, p2 * 16, 0,
                16, 16, 1, d0 * 2.0, d0 * 2.0, d0 * 2.0);
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                final boolean flag = slowsandNoise[k + l * 16] + hellRNG.nextDouble() * 0.2 > 0.0;
                final boolean flag2 = gravelNoise[k + l * 16] + hellRNG.nextDouble() * 0.2 > 0.0;
                final int i1 = (int) (netherrackExclusivityNoise[k + l * 16] / 3.0 + 3.0 + hellRNG.nextDouble() * 0.25);
                int j1 = -1;
                Block block = Blocks.GRASS_BLOCK;
                Block block2 = Blocks.DIRT;
                for (int k2 = 127; k2 >= 0; --k2) {
                    final int l2 = (l * 16 + k) * 128 + k2;
                    if (k2 < 127 - hellRNG.nextInt(5) && k2 > 0 + hellRNG.nextInt(5)) {
                        final Block block3 = p3[l2];
                        if (block3 != null && block3 != Blocks.AIR) {
                            if (block3 == Blocks.STONE) {
                                if (j1 == -1) {
                                    if (i1 <= 0) {
                                        block = null;
                                        block2 = Blocks.STONE;
                                    } else if (k2 >= b0 - 4 && k2 <= b0 + 1) {
                                        block = Blocks.STONE;
                                        block2 = Blocks.STONE;
                                        if (flag2) {
                                            block = Blocks.GRASS_BLOCK;
                                            block2 = Blocks.DIRT;
                                        }
                                        if (flag) {
                                            block = Blocks.GRASS_BLOCK;
                                            block2 = Blocks.DIRT;
                                        }
                                    }
                                    if (k2 < b0 && block == Blocks.AIR) {
                                        block = Blocks.WATER;
                                    }
                                    j1 = i1;
                                    if (k2 >= b0 - 1) {
                                        p3[l2] = block;
                                    } else {
                                        p3[l2] = block2;
                                    }
                                } else if (j1 > 0) {
                                    --j1;
                                    p3[l2] = block2;
                                }
                            }
                        } else {
                            j1 = -1;
                        }
                    } else {
                        p3[l2] = null;
                    }
                }
            }
        }
    }

    /**
     * {@code addScragglyTrees} (:343-367): count and chance from {@code world.rand}, positions from the
     * generator's {@code random}. The root search from Y 120 down to 51 stays literal (the dimension keeps
     * the 1.7.10 height of 256; see {@code ChunkProviderOreSpawn5.addCrystalTrees} on DECISIONS R18).
     */
    private void addScragglyTrees(final RandomSource worldRand, final RandomSource random, final int chunkX, final int chunkZ,
                                  final ChunkAccess chunk) {
        int howmany = 1 + worldRand.nextInt(5);
        if (worldRand.nextInt(4) != 0) {
            return;
        }
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
            for (int posY = 120; posY > 50; --posY) {
                if (FastBlocks.getBlockInChunk(chunk, posX, posY - 1, posZ).is(Blocks.GRASS_BLOCK)) {
                    this.scragglyTreeWithBranches(random, posX, posY, posZ, chunk);
                    break;
                }
            }
        }
    }

    /** {@code makeScragglyBranch} (:369-411). {@code Blocks.log} meta 0 is the oak log. */
    private void makeScragglyBranch(final RandomSource random, int x, int y, int z, final int len, final int biasx, final int biasz,
                                    final ChunkAccess chunk) {
        final Block leaves = ModBlocks.LEAVES_APPLE.get();
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
            if (!bid.is(Blocks.AIR) && !bid.is(Blocks.OAK_LOG) && !bid.is(leaves)) {
                return;
            }
            FastBlocks.setBlockInChunk(chunk, x, y, z, Blocks.OAK_LOG.defaultBlockState());
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (random.nextInt(2) == 1) {
                        bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                        if (bid.is(Blocks.AIR)) {
                            FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves.defaultBlockState());
                        }
                    }
                }
            }
            if (random.nextInt(2) == 1) {
                bid = FastBlocks.getBlockInChunk(chunk, x, y + 1, z);
                if (bid.is(Blocks.AIR)) {
                    FastBlocks.setBlockInChunk(chunk, x, y + 1, z, leaves.defaultBlockState());
                }
            }
        }
    }

    /** {@code ScragglyTreeWithBranches} (:413-456). */
    private void scragglyTreeWithBranches(final RandomSource random, int x, int y, int z, final ChunkAccess chunk) {
        final Block leaves = ModBlocks.LEAVES_APPLE.get();
        final int i = 1 + random.nextInt(3);
        final int j = i + random.nextInt(12);
        for (int k = 0; k < i; ++k) {
            final BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y + k, z);
            if (k >= 1 && !bid.is(Blocks.AIR) && !bid.is(Blocks.OAK_LOG) && !bid.is(leaves)) {
                return;
            }
            FastBlocks.setBlockInChunk(chunk, x, y + k, z, Blocks.OAK_LOG.defaultBlockState());
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
            if (!bid.is(Blocks.AIR) && !bid.is(Blocks.OAK_LOG) && !bid.is(leaves)) {
                break;
            }
            FastBlocks.setBlockInChunk(chunk, x, y, z, Blocks.OAK_LOG.defaultBlockState());
            if (random.nextInt(4) == 1) {
                this.makeScragglyBranch(random, x, y, z, random.nextInt(1 + j - k), random.nextInt(2) - random.nextInt(2),
                        random.nextInt(2) - random.nextInt(2), chunk);
            }
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (random.nextInt(2) == 1) {
                        bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                        if (bid.is(Blocks.AIR)) {
                            FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves.defaultBlockState());
                        }
                    }
                }
            }
            if (random.nextInt(2) == 1) {
                bid = FastBlocks.getBlockInChunk(chunk, x, y + 1, z);
                if (bid.is(Blocks.AIR)) {
                    FastBlocks.setBlockInChunk(chunk, x, y + 1, z, leaves.defaultBlockState());
                }
            }
        }
    }

    /**
     * {@code populate} (:299-306): {@code biome.decorate(world, world.rand, x, z)} with the Chaos decorator, the biome
     * read at {@code (x + 16, z + 16)} (:303), which in this single-biome dimension is always {@code orespawn:chaos}.
     * {@code BiomeGenBase.decorate} is {@code theBiomeDecorator.decorateChunk}, i.e. the shared
     * {@link BiomeDecorator#decorateChunk} with the counts {@code setChaosCreatures} leaves on it
     * (see {@link #chaosDecorator}). Ores, sand/clay/gravel disks, trees, flowers, grass, the two unconditional
     * mushroom patches, ten reed patches, the pumpkin patch and 50 water / 20 lava springs run in 1.7.10 order and
     * with 1.7.10 target checks; before R21 everything after the ore step was vanilla 1.21.1 placed features
     * (other random sequence, other density and shapes).
     *
     * <p>Order against the rest of the decoration step: the decorator first, then {@code super}. {@code super} places
     * structure pieces and the placed features of biome modifiers, which is where {@code OreSpawnWorld.generate}
     * (:192, FML world generator) ends up - and FML ran it after {@code populate}. The biome JSON contributes no
     * feature any more, and the Chaos biome has no vanilla structure. {@link LegacyStructurePass} runs last
     * (DECISIONS R24); Chaos has no {@code orespawn:legacy} structure today, the call keeps the six generators alike.
     *
     * <p>Populate window: every step starts at {@code x * 16 + 8 .. x * 16 + 23} (ores {@code + 0 .. 15}, vein centre
     * +8) and spreads at most 8 blocks (flowers, grass, mushrooms, pumpkin) or a big oak's 5 + 2; all reads and writes
     * stay in chunks C and C + 1, inside the 1.21.1 write radius, as for Mining ({@code ChunkProviderOreSpawn2}).
     * So decorating chunk C runs the populate of C itself.
     *
     * <p>PORT: {@code world.rand} is replaced by the populate seed of the 1.7.10 overworld generator
     * ({@code nextLong() / 2 * 2 + 1}, chunk coordinates, world seed), as {@code ChunkProviderOreSpawn2}
     * does it (DECISIONS R18). {@code BlockFalling.fallInstantly = false} (:300) has no counterpart and needs none:
     * {@link LegacyWorld} schedules the falling tick {@code onBlockAdded} scheduled in 1.7.10.
     */
    @Override
    public void applyBiomeDecoration(final WorldGenLevel level, final ChunkAccess chunk, final StructureManager structureManager) {
        final int par2 = chunk.getPos().x;
        final int par3 = chunk.getPos().z;
        final int var4 = par2 * 16;
        final int var5 = par3 * 16;
        final Random rand = LegacyChunk.populateRandom(level.getSeed(), par2, par3);
        // BiomeGenUtopianPlains keeps the BiomeGenBase default topBlock (grass); only WorldGenLakes reads it, and the
        // decorator runs no lake.
        chaosDecorator().decorateChunk(new LegacyWorld(level, Blocks.GRASS_BLOCK.defaultBlockState()), rand, var4, var5);
        super.applyBiomeDecoration(level, chunk, structureManager);
        // OreSpawnWorld.generate ran after populate under FML (OreSpawnMain.java:5035).
        com.swbr.orespawn.world.OreSpawnWorld.generate(level, chunk.getPos().x, chunk.getPos().z);
        LegacyStructurePass.apply(level, this, chunk.getPos());
    }

    /**
     * The decorator of the Chaos biome: {@code new BiomeGenUtopianPlains(BiomeChaosID)} sets trees -999, flowers 4,
     * grass 6 (BiomeGenUtopianPlains.java:57-59), then {@code setChaosCreatures} (WorldProviderOreSpawn6.java:27)
     * overwrites flowers 2, grass 4, trees 1, big mushrooms, mushrooms and reeds -999
     * (BiomeGenUtopianPlains.java:263-268). The -999 loops, like the counts 0 of dead bushes, water lilies and cacti,
     * run no iteration and draw nothing, which is what {@link BiomeDecorator} does for them. The biome does not
     * override the tree pick ({@code func_150567_a}), so it is {@link BiomeGenBase#getRandomWorldGenForTrees}.
     * A fresh instance per populate call, never shared between threads.
     */
    private static BiomeDecorator chaosDecorator() {
        return new BiomeDecorator(1, 2, 4, BiomeGenBase::getRandomWorldGenForTrees) {
        };
    }

    /** No caves or ravines. */
    @Override
    public void applyCarvers(final WorldGenRegion level, final long seed, final RandomState random, final BiomeManager biomeManager,
                             final StructureManager structureManager, final ChunkAccess chunk, final GenerationStep.Carving step) {
    }

    /** The surface pass ran inside {@code provideChunk}; see {@link #fillFromNoise}. */
    @Override
    public void buildSurface(final WorldGenRegion level, final StructureManager structureManager, final RandomState random,
                             final ChunkAccess chunk) {
    }

    /**
     * {@code SpawnerAnimals.performWorldGenSpawning(world, biome, x + 8, z + 8, 16, 16, world.rand)} (:305).
     * PORT: seeded as in {@code NoiseBasedChunkGenerator.spawnOriginalMobs}.
     */
    @Override
    public void spawnOriginalMobs(final WorldGenRegion level) {
        final ChunkPos chunkpos = level.getCenter();
        final Holder<Biome> holder = level.getBiome(chunkpos.getWorldPosition().atY(level.getMaxBuildHeight() - 1));
        final WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(level.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(level, holder, chunkpos, worldgenrandom);
    }

    /** The 1.7.10 chunk was 256 high; the generator array ({@code Block[32768]}, :189) fills only Y 0..127. */
    @Override
    public int getGenDepth() {
        return 256;
    }

    /**
     * PORT: 1.7.10 had no generator sea level; the surface pass works around {@code b0 = 64} and sets no
     * water. 63 is the 1.21.1 overworld value and the Y above which 1.7.10 {@code EntityBat} refused to
     * spawn, which is what {@code Bat.checkBatSpawnRules} now reads from here.
     */
    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    /** Terrain and surface of the column, without ores and trees. */
    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types type, final LevelHeightAccessor level,
                             final RandomState random) {
        return LegacyChunk.baseHeight(this.columnChunk(x, z, random), x, z, type, level);
    }

    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor height, final RandomState random) {
        return LegacyChunk.baseColumn(this.columnChunk(x, z, random), x, z, height);
    }

    /**
     * The chunk array behind {@link #getBaseHeight} and {@link #getBaseColumn}. fix2 (DECISIONS R26): cached per chunk
     * in {@link ChunkTerrainCache}, so a structure pass asking for every column of its footprint generates each chunk
     * once instead of once per column; same blocks (the {@code Random} is seeded from the chunk coordinates).
     */
    private Block[] columnChunk(final int x, final int z, final RandomState randomState) {
        final Noises n = this.noises();
        return n.baseChunks.get(x >> 4, z >> 4, (p1, p2) -> {
            final Random hellRNG = new Random(p1 * 341873128712L + p2 * 132897987541L);
            return this.generateTerrain(n, p1, p2, hellRNG);
        });
    }

    @Override
    public void addDebugScreenInfo(final List<String> info, final RandomState random, final BlockPos pos) {
    }
}
