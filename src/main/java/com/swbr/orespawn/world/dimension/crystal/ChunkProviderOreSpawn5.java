package com.swbr.orespawn.world.dimension.crystal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.dimension.ChunkTerrainCache;
import com.swbr.orespawn.world.gen.BiomeDecorator;
import com.swbr.orespawn.world.gen.BiomeGenBase;
import com.swbr.orespawn.world.gen.LegacyChunk;
import com.swbr.orespawn.world.gen.LegacyWorld;
import com.swbr.orespawn.world.gen.NoiseGeneratorOctaves;
import com.swbr.orespawn.world.gen.NoiseGeneratorPerlin;
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
import net.minecraft.util.Mth;
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
 * Port of {@code ChunkProviderOreSpawn5} (ChunkProviderOreSpawn5.java:15-813), the generator of the
 * Crystal dimension (verhalten/world-03.md "ChunkProviderOreSpawn5"). Codec type
 * {@code orespawn:crystal}.
 *
 * <p>A copy of the 1.7.10 {@code ChunkProviderGenerate} density field (5x33x5, one biome of height
 * 0.1/0.5) that fills with crystal stone ("Kyanite") and water below Y 63, its own surface pass
 * (crystal grass on land between Y 59 and 64, bedrock on Y 0..4), then - still inside
 * {@code provideChunk}, in this order - the {@link CrystalMaze}, pink tourmaline and tiger's eye,
 * crystal trees, the crystal ores, flowers, rice and quinoa. All of it writes only into its own chunk,
 * so every vein and tree is clipped at the chunk border; that clip is gameplay (about 29 % of each
 * ore vein survives, verhalten/world-03.md) and kept.
 *
 * <p>Mapping onto 1.21.1: everything {@code provideChunk} did runs in {@link #fillFromNoise}, because
 * the features depend on the exact random sequence the surface pass leaves behind in {@code rand}.
 * {@link #buildSurface} and {@link #applyCarvers} are empty (there were no caves). {@code populate}
 * (:315-328) is {@link #applyBiomeDecoration}: the Crystal decorator as the shared 1.7.10 Java
 * {@link BiomeDecorator} (DECISIONS R21; {@code worldgen/biome/crystal.json} lists no feature), then the
 * initial spawns, which is {@link #spawnOriginalMobs}.
 *
 * <p>Random sources: {@code rand} keeps the original chunk seed {@code x * 341873128712 + z *
 * 132897987541} (:205), which does not involve the world seed. The world seed feeds the noise tables as
 * before; it comes from {@link #createState}, the same path as in the other five generators (DECISIONS
 * R21). PORT: {@code world.rand}
 * (maze holes, tree shapes) and {@code Math.random()} (maze layout) are per-chunk seeded
 * {@link Random}s (DECISIONS R18).
 *
 * <p>PORT: {@code TerrainGen.getModdedNoiseGenerators} (:60-68), a Forge hook for mods to swap the
 * noise generators, has no counterpart; the generators are the vanilla ones it returned by default.
 * {@code WorldType.AMPLIFIED} (:249-252) is never true - 1.21.1 has no world type on a derived level.
 */
public final class ChunkProviderOreSpawn5 extends ChunkGenerator {

    public static final MapCodec<ChunkProviderOreSpawn5> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    Codec.FLOAT.optionalFieldOf("root_height", 0.1F).forGetter(generator -> generator.rootHeight),
                    Codec.FLOAT.optionalFieldOf("height_variation", 0.5F).forGetter(generator -> generator.heightVariation))
            .apply(instance, ChunkProviderOreSpawn5::new));

    /** {@code BiomeGenBase.rootHeight} of the Crystal biome, {@code setHeight(new Height(0.1F, 0.5F))} (WorldProviderOreSpawn5.java:28). */
    private final float rootHeight;
    /** {@code BiomeGenBase.heightVariation} of the Crystal biome. */
    private final float heightVariation;
    /** {@code parabolicField} (:29, :53-59). */
    private final float[] parabolicField;
    /** Noise tables of the world seed, built in {@link #createState}; immutable, replaced as a whole. */
    private volatile Noises noises;

    public ChunkProviderOreSpawn5(final BiomeSource biomeSource, final float rootHeight, final float heightVariation) {
        super(biomeSource);
        this.rootHeight = rootHeight;
        this.heightVariation = heightVariation;
        this.parabolicField = new float[25];
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                final float f = 10.0f / Mth.sqrt(j * j + k * k + 0.2f);
                this.parabolicField[j + 2 + (k + 2) * 5] = f;
            }
        }
    }

    /**
     * The noise generators of the constructor (:44-51), drawn from {@code new Random(worldSeed)} in the
     * original order. {@code noiseGen5} is never read but must be built, because it draws before
     * {@code noiseGen6}; {@code mobSpawnerNoise} is never read and drawn last, so it is left out.
     * Names are the later MCP names of the unnamed 1.7.10 fields.
     */
    private static final class Noises {
        final long seed;
        /** {@code field_147431_j}. */
        final NoiseGeneratorOctaves minLimitPerlinNoise;
        /** {@code field_147432_k}. */
        final NoiseGeneratorOctaves maxLimitPerlinNoise;
        /** {@code field_147429_l}. */
        final NoiseGeneratorOctaves mainPerlinNoise;
        /** {@code field_147430_m}. */
        final NoiseGeneratorPerlin surfaceNoise;
        /** {@code noiseGen6}. */
        final NoiseGeneratorOctaves depthNoise;
        /** fix2 (R26): chunk terrain arrays for {@code getBaseHeight} and {@code getBaseColumn}. */
        final ChunkTerrainCache<Block[]> baseChunks = new ChunkTerrainCache<>(32);

        Noises(final long seed) {
            this.seed = seed;
            final Random rand = new Random(seed);
            this.minLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
            this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(rand, 16);
            this.mainPerlinNoise = new NoiseGeneratorOctaves(rand, 8);
            this.surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
            new NoiseGeneratorOctaves(rand, 10); // noiseGen5
            this.depthNoise = new NoiseGeneratorOctaves(rand, 16);
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

    /** {@code provideChunk(par1, par2)} (:204-221). */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final Blender blender, final RandomState randomState,
                                                        final StructureManager structureManager, final ChunkAccess chunk) {
        final int par1 = chunk.getPos().x;
        final int par2 = chunk.getPos().z;
        final Noises n = this.noises();
        final long seed = n.seed;
        final Random rand = new Random(par1 * 341873128712L + par2 * 132897987541L);
        final Block[] ablock = this.generateTerrain(n, par1, par2, rand);
        LegacyChunk.fillChunk(chunk, ablock);
        final Random worldRand = LegacyChunk.chunkRandom(seed, par1, par2, LegacyChunk.SALT_WORLD_RAND);
        final Random mathRandom = LegacyChunk.chunkRandom(seed, par1, par2, LegacyChunk.SALT_MATH_RANDOM);
        final CrystalMaze cm = new CrystalMaze(mathRandom, worldRand);
        cm.buildCrystalMaze(par1 * 16, 25, par2 * 16, chunk);
        this.generateCrystals(rand, par1 * 16, par2 * 16, chunk);
        this.addCrystalTrees(worldRand, rand, par1 * 16, par2 * 16, chunk);
        this.generateCrystalOres(rand, par1 * 16, par2 * 16, chunk);
        this.addCrystalFlowers(rand, par1 * 16, par2 * 16, chunk);
        this.addRice(rand, par1 * 16, par2 * 16, chunk);
        this.addQuinoa(rand, par1 * 16, par2 * 16, chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    /** Terrain and surface of one chunk: {@code func_147424_a} then {@code replaceBlocksForBiome} (:206-209). */
    private Block[] generateTerrain(final Noises n, final int par1, final int par2, final Random rand) {
        final Block[] ablock = new Block[65536];
        this.generateBlocks(n, par1, par2, ablock);
        this.replaceBlocksForBiome(n, par1, par2, ablock, rand);
        return ablock;
    }

    /** {@code func_147424_a} (:71-128): density field to crystal stone, water below Y 63, or air. */
    private void generateBlocks(final Noises n, final int p1, final int p2, final Block[] p3) {
        final byte b0 = 63;
        final Block crystalStone = ModBlocks.CRYSTALSTONE.get();
        final double[] field = this.initializeDensity(n, p1 * 4, 0, p2 * 4);
        for (int k = 0; k < 4; ++k) {
            final int l = k * 5;
            final int i1 = (k + 1) * 5;
            for (int j1 = 0; j1 < 4; ++j1) {
                final int k2 = (l + j1) * 33;
                final int l2 = (l + j1 + 1) * 33;
                final int i2 = (i1 + j1) * 33;
                final int j2 = (i1 + j1 + 1) * 33;
                for (int k3 = 0; k3 < 32; ++k3) {
                    final double d0 = 0.125;
                    double d2 = field[k2 + k3];
                    double d3 = field[l2 + k3];
                    double d4 = field[i2 + k3];
                    double d5 = field[j2 + k3];
                    final double d6 = (field[k2 + k3 + 1] - d2) * d0;
                    final double d7 = (field[l2 + k3 + 1] - d3) * d0;
                    final double d8 = (field[i2 + k3 + 1] - d4) * d0;
                    final double d9 = (field[j2 + k3 + 1] - d5) * d0;
                    for (int l3 = 0; l3 < 8; ++l3) {
                        final double d10 = 0.25;
                        double d11 = d2;
                        double d12 = d3;
                        final double d13 = (d4 - d2) * d10;
                        final double d14 = (d5 - d3) * d10;
                        for (int i3 = 0; i3 < 4; ++i3) {
                            int j3 = i3 + k * 4 << 12 | 0 + j1 * 4 << 8 | k3 * 8 + l3;
                            final short short1 = 256;
                            j3 -= short1;
                            final double d15 = 0.25;
                            final double d16 = (d12 - d11) * d15;
                            double d17 = d11 - d16;
                            for (int k4 = 0; k4 < 4; ++k4) {
                                if ((d17 += d16) > 0.0) {
                                    p3[j3 += short1] = crystalStone;
                                } else if (k3 * 8 + l3 < b0) {
                                    p3[j3 += short1] = Blocks.WATER;
                                } else {
                                    p3[j3 += short1] = null;
                                }
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
     * {@code func_147423_a} (:223-309): the 5x33x5 density field. {@code biomesForGeneration} holds the
     * single Crystal biome everywhere ({@code WorldChunkManagerHell}), so every neighbour has this
     * generator's height and the halving for a higher neighbour (:254-256) never applies.
     */
    private double[] initializeDensity(final Noises n, final int p1, final int p2, final int p3) {
        final double[] field = new double[825];
        final double[] depth = n.depthNoise.generateNoiseOctaves(null, p1, p3, 5, 5, 200.0, 200.0, 0.5);
        final double[] main = n.mainPerlinNoise.generateNoiseOctaves(null, p1, p2, p3, 5, 33, 5,
                8.555150000000001, 4.277575000000001, 8.555150000000001);
        final double[] minLimit = n.minLimitPerlinNoise.generateNoiseOctaves(null, p1, p2, p3, 5, 33, 5, 684.412, 684.412, 684.412);
        final double[] maxLimit = n.maxLimitPerlinNoise.generateNoiseOctaves(null, p1, p2, p3, 5, 33, 5, 684.412, 684.412, 684.412);
        int l = 0;
        int i1 = 0;
        for (int j1 = 0; j1 < 5; ++j1) {
            for (int k1 = 0; k1 < 5; ++k1) {
                float f = 0.0f;
                float f2 = 0.0f;
                float f3 = 0.0f;
                final byte b0 = 2;
                for (int l2 = -b0; l2 <= b0; ++l2) {
                    for (int i2 = -b0; i2 <= b0; ++i2) {
                        final float f4 = this.rootHeight;
                        final float f5 = this.heightVariation;
                        final float f6 = this.parabolicField[l2 + 2 + (i2 + 2) * 5] / (f4 + 2.0f);
                        f += f5 * f6;
                        f2 += f4 * f6;
                        f3 += f6;
                    }
                }
                f /= f3;
                f2 /= f3;
                f = f * 0.9f + 0.1f;
                f2 = (f2 * 4.0f - 1.0f) / 8.0f;
                double d6 = depth[i1] / 8000.0;
                if (d6 < 0.0) {
                    d6 = -d6 * 0.3;
                }
                d6 = d6 * 3.0 - 2.0;
                if (d6 < 0.0) {
                    d6 /= 2.0;
                    if (d6 < -1.0) {
                        d6 = -1.0;
                    }
                    d6 /= 1.4;
                    d6 /= 2.0;
                } else {
                    if (d6 > 1.0) {
                        d6 = 1.0;
                    }
                    d6 /= 8.0;
                }
                ++i1;
                double d7 = f2;
                final double d8 = f;
                d7 += d6 * 0.2;
                d7 = d7 * 8.5 / 8.0;
                final double d9 = 8.5 + d7 * 4.0;
                for (int j2 = 0; j2 < 33; ++j2) {
                    double d10 = (j2 - d9) * 12.0 * 128.0 / 256.0 / d8;
                    if (d10 < 0.0) {
                        d10 *= 4.0;
                    }
                    final double d11 = minLimit[l] / 512.0;
                    final double d12 = maxLimit[l] / 512.0;
                    final double d13 = (main[l] / 10.0 + 1.0) / 2.0;
                    double d14 = denormalizeClamp(d11, d12, d13) - d10;
                    if (j2 > 29) {
                        final double d15 = (j2 - 29) / 3.0f;
                        d14 = d14 * (1.0 - d15) + -10.0 * d15;
                    }
                    field[l] = d14;
                    ++l;
                }
            }
        }
        return field;
    }

    /** {@code MathHelper.denormalizeClamp} ({@code func_151238_b}), javap: {@code a + (b - a) * t}. */
    private static double denormalizeClamp(final double a, final double b, final double t) {
        return t < 0.0 ? a : (t > 1.0 ? b : a + (b - a) * t);
    }

    /** {@code replaceBlocksForBiome} (:130-138). */
    private void replaceBlocksForBiome(final Noises n, final int p1, final int p2, final Block[] p3, final Random rand) {
        final double d0 = 0.03125;
        final double[] stoneNoise = n.surfaceNoise.getRegion(null, (double) (p1 * 16), (double) (p2 * 16), 16, 16,
                d0 * 2.0, d0 * 2.0, 1.0);
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                this.genBiomeTerrain(rand, p3, p1 * 16 + k, p2 * 16 + l, stoneNoise[l + k * 16]);
            }
        }
    }

    /**
     * {@code MygenBiomeTerrain} (:144-198). The column index {@code (z * 16 + x) * 256 + y} transposes
     * x and z against the {@code Chunk} constructor - vanilla 1.7.10 did the same - and is kept.
     */
    private void genBiomeTerrain(final Random rand, final Block[] blocks, final int x, final int z, final double noise) {
        final Block crystalStone = ModBlocks.CRYSTALSTONE.get();
        final Block crystalGrass = ModBlocks.CRYSTAL_GRASS.get();
        Block block = crystalGrass;
        Block block2 = crystalStone;
        int k = -1;
        final int l = (int) (noise / 3.0 + 3.0 + rand.nextDouble() * 0.25);
        final int i1 = x & 0xF;
        final int j1 = z & 0xF;
        final int k2 = blocks.length / 256;
        for (int l2 = 255; l2 >= 0; --l2) {
            final int i2 = (j1 * 16 + i1) * k2 + l2;
            if (l2 <= 0 + rand.nextInt(5)) {
                blocks[i2] = Blocks.BEDROCK;
            } else {
                final Block block3 = blocks[i2];
                if (block3 != null && block3 != Blocks.AIR) {
                    if (block3 == crystalStone) {
                        if (k == -1) {
                            if (l <= 0) {
                                block = null;
                                block2 = crystalStone;
                            } else if (l2 >= 59 && l2 <= 64) {
                                block = crystalGrass;
                                block2 = crystalStone;
                            }
                            if (l2 < 63 && (block == null || block == Blocks.AIR)) {
                                block = Blocks.WATER;
                            }
                            k = l;
                            if (l2 >= 62) {
                                blocks[i2] = block;
                            } else {
                                blocks[i2] = block2;
                            }
                        } else if (k > 0) {
                            --k;
                            blocks[i2] = block2;
                        }
                    }
                } else {
                    k = -1;
                }
            }
        }
    }

    /** {@code generateCrystals} (:361-364). */
    private void generateCrystals(final Random random, final int chunkX, final int chunkZ, final ChunkAccess chunk) {
        this.addPinkTourmaline(random, chunkX, chunkZ, chunk);
        this.addTigersEye(random, chunkX, chunkZ, chunk);
    }

    /**
     * {@code addPinkTourmaline} (:366-393). Replaces every block in its path, air and water included.
     * PORT: the {@code (int)} casts of the float column position are {@link Mth#floor} (DECISIONS R20).
     */
    private void addPinkTourmaline(final Random random, final int chunkX, final int chunkZ, final ChunkAccess chunk) {
        if (random.nextInt(30) != 1) {
            return;
        }
        final BlockState crystal = ModBlocks.CRYSTALCRYSTAL.get().defaultBlockState();
        final int randPosX = 3 + chunkX + random.nextInt(10);
        final int randPosY = 30 + random.nextInt(5);
        final int randPosZ = 3 + chunkZ + random.nextInt(10);
        for (int patchy = 1 + random.nextInt(10), i = 0; i < patchy; ++i) {
            final float dx = random.nextFloat() - random.nextFloat();
            final float dz = random.nextFloat() - random.nextFloat();
            final float dy = 0.5f + random.nextFloat() / 2.0f;
            final int width = random.nextInt(2);
            final int length = 1 + width * 3 + random.nextInt(15);
            float rx = (float) randPosX;
            float ry = (float) randPosY;
            float rz = (float) randPosZ;
            for (int iy = 0; iy <= length; ++iy) {
                for (int ix = 0; ix <= width; ++ix) {
                    for (int iz = 0; iz <= width; ++iz) {
                        FastBlocks.setBlockInChunk(chunk, Mth.floor(rx + ix), Mth.floor(ry), Mth.floor(rz + iz), crystal);
                    }
                }
                ry += dy;
                rx += dx;
                rz += dz;
            }
        }
    }

    /** {@code addTigersEye} (:395-422). PORT: {@link Mth#floor} for the casts, as above. */
    private void addTigersEye(final Random random, final int chunkX, final int chunkZ, final ChunkAccess chunk) {
        if (random.nextInt(30) != 1) {
            return;
        }
        final BlockState tigersEye = ModBlocks.TIGERSEYE.get().defaultBlockState();
        final int randPosX = 3 + chunkX + random.nextInt(10);
        final int randPosY = 5 + random.nextInt(5);
        final int randPosZ = 3 + chunkZ + random.nextInt(10);
        for (int patchy = 1 + random.nextInt(5), i = 0; i < patchy; ++i) {
            final float dx = random.nextFloat() - random.nextFloat();
            final float dz = random.nextFloat() - random.nextFloat();
            final float dy = 0.5f + random.nextFloat() / 2.0f;
            final int width = 0;
            final int length = width * 3 + random.nextInt(6);
            float rx = (float) randPosX;
            float ry = (float) randPosY;
            float rz = (float) randPosZ;
            for (int iy = 0; iy <= length; ++iy) {
                for (int ix = 0; ix <= width; ++ix) {
                    for (int iz = 0; iz <= width; ++iz) {
                        FastBlocks.setBlockInChunk(chunk, Mth.floor(rx + ix), Mth.floor(ry), Mth.floor(rz + iz), tigersEye);
                    }
                }
                ry += dy;
                rx += dx;
                rz += dz;
            }
        }
    }

    /**
     * {@code addCrystalTrees} (:424-452). Positions from {@code rand}, shapes from {@code world.rand}.
     *
     * <p>The search from Y 128 down to 41 stays literal although DECISIONS R18 moves surface searches
     * onto {@code WORLD_SURFACE_WG}: that rule exists because the 1.21.1 overworld reaches Y 320, and
     * this dimension keeps the 1.7.10 height of 256 ({@code dimension_type/crystal.json}), so the scan
     * covers exactly what it covered before. A heightmap would change results under overhangs and under
     * the canopy of a tree placed earlier in the same chunk.
     */
    private void addCrystalTrees(final Random worldRand, final Random random, final int chunkX, final int chunkZ,
                                 final ChunkAccess chunk) {
        if (random.nextInt(5) != 0) {
            return;
        }
        final Block crystalGrass = ModBlocks.CRYSTAL_GRASS.get();
        int howmany = 0;
        final int what = random.nextInt(5);
        howmany = random.nextInt(8);
        if (what != 0) {
            howmany *= 2;
        }
        for (int i = 0; i < howmany; ++i) {
            final int posX = 4 + chunkX + random.nextInt(8);
            final int posZ = 4 + chunkZ + random.nextInt(8);
            int posY = 128;
            while (posY > 40) {
                if (FastBlocks.getBlockInChunk(chunk, posX, posY, posZ).is(Blocks.AIR)
                        && FastBlocks.getBlockInChunk(chunk, posX, posY - 1, posZ).is(crystalGrass)) {
                    if (what == 0) {
                        this.tallCrystalTree(worldRand, posX, posY, posZ, chunk);
                        break;
                    }
                    this.scragglyCrystalTreeWithBranches(worldRand, posX, posY, posZ, chunk);
                    break;
                } else {
                    --posY;
                }
            }
        }
    }

    /** {@code makeScragglyCrystalBranch} (:454-496). */
    private void makeScragglyCrystalBranch(final Random worldRand, int x, int y, int z, final int len, final int biasx,
                                           final int biasz, final ChunkAccess chunk) {
        final Block log = ModBlocks.CRYSTAL_TREE_LOG.get();
        final Block leaves2 = ModBlocks.CRYSTAL_TREE_LEAVES2.get();
        for (int k = 0; k < len; ++k) {
            int ix = worldRand.nextInt(2) - worldRand.nextInt(2) + biasx;
            int iz = worldRand.nextInt(2) - worldRand.nextInt(2) + biasz;
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
            final int iy = worldRand.nextInt(3) > 0 ? 1 : 0;
            x += ix;
            z += iz;
            y += iy;
            BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y, z);
            if (!bid.is(Blocks.AIR) && !bid.is(log) && !bid.is(leaves2)) {
                return;
            }
            FastBlocks.setBlockInChunk(chunk, x, y, z, log.defaultBlockState());
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (worldRand.nextInt(2) == 1) {
                        bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                        if (bid.is(Blocks.AIR)) {
                            FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves2.defaultBlockState());
                        }
                    }
                }
            }
            if (worldRand.nextInt(2) == 1) {
                bid = FastBlocks.getBlockInChunk(chunk, x, y + 1, z);
                if (bid.is(Blocks.AIR)) {
                    FastBlocks.setBlockInChunk(chunk, x, y + 1, z, leaves2.defaultBlockState());
                }
            }
        }
    }

    /** {@code ScragglyCrystalTreeWithBranches} (:498-541). */
    private void scragglyCrystalTreeWithBranches(final Random worldRand, int x, int y, int z, final ChunkAccess chunk) {
        final Block log = ModBlocks.CRYSTAL_TREE_LOG.get();
        final Block leaves2 = ModBlocks.CRYSTAL_TREE_LEAVES2.get();
        final int i = 1 + worldRand.nextInt(2);
        final int j = i + worldRand.nextInt(8);
        for (int k = 0; k < i; ++k) {
            final BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y + k, z);
            if (k >= 1 && !bid.is(Blocks.AIR) && !bid.is(log) && !bid.is(leaves2)) {
                return;
            }
            FastBlocks.setBlockInChunk(chunk, x, y + k, z, log.defaultBlockState());
        }
        y += i - 1;
        for (int k = i; k < j; ++k) {
            final int ix = worldRand.nextInt(2) - worldRand.nextInt(2);
            final int iz = worldRand.nextInt(2) - worldRand.nextInt(2);
            final int iy = worldRand.nextInt(4) > 0 ? 1 : 0;
            x += ix;
            z += iz;
            y += iy;
            BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y, z);
            if (!bid.is(Blocks.AIR) && !bid.is(log) && !bid.is(leaves2)) {
                break;
            }
            FastBlocks.setBlockInChunk(chunk, x, y, z, log.defaultBlockState());
            if (worldRand.nextInt(4) == 1) {
                this.makeScragglyCrystalBranch(worldRand, x, y, z, worldRand.nextInt(1 + j - k),
                        worldRand.nextInt(2) - worldRand.nextInt(2), worldRand.nextInt(2) - worldRand.nextInt(2), chunk);
            }
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (worldRand.nextInt(2) == 1) {
                        bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                        if (bid.is(Blocks.AIR)) {
                            FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves2.defaultBlockState());
                        }
                    }
                }
            }
            if (worldRand.nextInt(2) == 1) {
                bid = FastBlocks.getBlockInChunk(chunk, x, y + 1, z);
                if (bid.is(Blocks.AIR)) {
                    FastBlocks.setBlockInChunk(chunk, x, y + 1, z, leaves2.defaultBlockState());
                }
            }
        }
    }

    /** {@code TallCrystalTree} (:543-608); {@code LessLag} shortens the trunk. */
    private void tallCrystalTree(final Random worldRand, final int x, int y, final int z, final ChunkAccess chunk) {
        final Block log = ModBlocks.CRYSTAL_TREE_LOG.get();
        final Block leaves = ModBlocks.CRYSTAL_TREE_LEAVES.get();
        final int lessLag = TweakStats.LessLag();
        int i = 10 + worldRand.nextInt(12);
        if (lessLag == 1) {
            i -= 2;
        }
        if (lessLag == 2) {
            i -= 4;
        }
        final int j = i + worldRand.nextInt(18 - lessLag * 2);
        for (int k = 0; k < i; ++k) {
            final BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y + k, z);
            if (k >= 1 && !bid.is(Blocks.AIR) && !bid.is(log) && !bid.is(leaves)) {
                return;
            }
            FastBlocks.setBlockInChunk(chunk, x, y + k, z, log.defaultBlockState());
        }
        y += i - 1;
        for (int k = i; k < j; ++k) {
            ++y;
            BlockState bid = FastBlocks.getBlockInChunk(chunk, x, y, z);
            if (!bid.is(Blocks.AIR) && !bid.is(log) && !bid.is(leaves)) {
                break;
            }
            FastBlocks.setBlockInChunk(chunk, x, y, z, log.defaultBlockState());
            if (k % 4 == 0) {
                for (int m = -1; m < 2; ++m) {
                    for (int n = -1; n < 2; ++n) {
                        if (worldRand.nextInt(2) == 1) {
                            bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                            if (bid.is(Blocks.AIR)) {
                                FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
        ++y;
        for (int m = -1; m < 2; ++m) {
            for (int n = -1; n < 2; ++n) {
                if (worldRand.nextInt(2) == 1) {
                    final BlockState bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                    if (bid.is(Blocks.AIR)) {
                        FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, log.defaultBlockState());
                    }
                }
            }
        }
        for (int m = -3; m < 4; ++m) {
            for (int n = -3; n < 4; ++n) {
                final BlockState bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                if (bid.is(Blocks.AIR)) {
                    FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves.defaultBlockState());
                }
            }
        }
        ++y;
        for (int m = -1; m < 2; ++m) {
            for (int n = -1; n < 2; ++n) {
                final BlockState bid = FastBlocks.getBlockInChunk(chunk, x + m, y, z + n);
                if (bid.is(Blocks.AIR)) {
                    FastBlocks.setBlockInChunk(chunk, x + m, y, z + n, leaves.defaultBlockState());
                }
            }
        }
    }

    /** {@code generateCrystalOres} (:610-696). {@code LessOre} is not read here, as in the original. */
    private void generateCrystalOres(final Random random, final int chunkX, final int chunkZ, final ChunkAccess chunk) {
        final Block crystalStone = ModBlocks.CRYSTALSTONE.get();
        int patchy = 25 + random.nextInt(30);
        if (random.nextInt(20) == 0) {
            patchy += 30;
        }
        for (int i = 0; i < patchy; ++i) {
            final int randPosX = 2 + chunkX + random.nextInt(12);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 2 + chunkZ + random.nextInt(12);
            if (randPosY > 45) {
                final int j = random.nextInt(11);
                BlockState b = Blocks.AIR.defaultBlockState();
                switch (j) {
                    case 0 -> b = driedEgg("oreurchin");
                    case 1 -> b = driedEgg("oreflounder");
                    case 2 -> b = driedEgg("oreskate");
                    case 3 -> b = driedEgg("orerotator");
                    case 4 -> b = driedEgg("orepeacock");
                    case 5 -> b = driedEgg("orefairy");
                    case 6 -> b = driedEgg("oredungeonbeast");
                    case 7 -> b = driedEgg("orevortex");
                    case 8 -> b = driedEgg("orerat");
                    case 9 -> b = driedEgg("orewhale");
                    case 10 -> b = driedEgg("oreirukandji");
                    default -> {
                    }
                }
                this.generateOre(random, randPosX, randPosY, randPosZ, chunk, b, 4, crystalStone);
            }
        }
        patchy = 3 + random.nextInt(8);
        for (int i = 0; i < patchy; ++i) {
            final int randPosX = 2 + chunkX + random.nextInt(12);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 2 + chunkZ + random.nextInt(12);
            this.generateOre(random, randPosX, randPosY, randPosZ, chunk, ModBlocks.CRYSTALCOAL.get().defaultBlockState(), 6, crystalStone);
        }
        patchy = 15 + random.nextInt(20);
        for (int i = 0; i < patchy; ++i) {
            final int randPosX = 2 + chunkX + random.nextInt(12);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 2 + chunkZ + random.nextInt(12);
            if (randPosY < 25) {
                this.generateOre(random, randPosX, randPosY, randPosZ, chunk, ModBlocks.CRYSTALRAT.get().defaultBlockState(), 6, crystalStone);
            }
        }
        patchy = 12 + random.nextInt(20);
        for (int i = 0; i < patchy; ++i) {
            final int randPosX = 2 + chunkX + random.nextInt(12);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 2 + chunkZ + random.nextInt(12);
            if (randPosY < 25) {
                this.generateOre(random, randPosX, randPosY, randPosZ, chunk, ModBlocks.CRYSTALFAIRY.get().defaultBlockState(), 6, crystalStone);
            }
        }
    }

    /** The {@code OreSpawnMain.My*SpawnBlock} of {@code generateCrystalOres}, by manifest id (W02 {@code DRIED_EGGS}). */
    private static BlockState driedEgg(final String id) {
        return ModBlocks.DRIED_EGGS.get(id).get().defaultBlockState();
    }

    /**
     * {@code generateOre} (:698-738): the 1.7.10 {@code WorldGenMinable} line of spheres around
     * {@code x + 8}/{@code z + 8}, replacing only {@code oldbid}, clipped to the chunk. The arithmetic
     * mixes float and double exactly as the original expressions do.
     */
    private boolean generateOre(final Random par2Random, final int par3, final int par4, final int par5, final ChunkAccess chunk,
                                final BlockState newbid, final int numberOfBlocks, final Block oldbid) {
        final float f = par2Random.nextFloat() * 3.1415927f;
        final double d0 = par3 + 8 + Mth.sin(f) * numberOfBlocks / 8.0f;
        final double d2 = par3 + 8 - Mth.sin(f) * numberOfBlocks / 8.0f;
        final double d3 = par5 + 8 + Mth.cos(f) * numberOfBlocks / 8.0f;
        final double d4 = par5 + 8 - Mth.cos(f) * numberOfBlocks / 8.0f;
        final double d5 = par4 + par2Random.nextInt(3) - 2;
        final double d6 = par4 + par2Random.nextInt(3) - 2;
        for (int l = 0; l <= numberOfBlocks; ++l) {
            final double d7 = d0 + (d2 - d0) * l / numberOfBlocks;
            final double d8 = d5 + (d6 - d5) * l / numberOfBlocks;
            final double d9 = d3 + (d4 - d3) * l / numberOfBlocks;
            final double d10 = par2Random.nextDouble() * numberOfBlocks / 16.0;
            final double d11 = (Mth.sin(l * 3.1415927f / numberOfBlocks) + 1.0f) * d10 + 1.0;
            final double d12 = (Mth.sin(l * 3.1415927f / numberOfBlocks) + 1.0f) * d10 + 1.0;
            final int i1 = Mth.floor(d7 - d11 / 2.0);
            final int j1 = Mth.floor(d8 - d12 / 2.0);
            final int k1 = Mth.floor(d9 - d11 / 2.0);
            final int l2 = Mth.floor(d7 + d11 / 2.0);
            final int i2 = Mth.floor(d8 + d12 / 2.0);
            final int j2 = Mth.floor(d9 + d11 / 2.0);
            for (int k2 = i1; k2 <= l2; ++k2) {
                final double d13 = (k2 + 0.5 - d7) / (d11 / 2.0);
                if (d13 * d13 < 1.0) {
                    for (int l3 = j1; l3 <= i2; ++l3) {
                        final double d14 = (l3 + 0.5 - d8) / (d12 / 2.0);
                        if (d13 * d13 + d14 * d14 < 1.0) {
                            for (int i3 = k1; i3 <= j2; ++i3) {
                                final double d15 = (i3 + 0.5 - d9) / (d11 / 2.0);
                                final BlockState bid = FastBlocks.getBlockInChunk(chunk, k2, l3, i3);
                                if (d13 * d13 + d14 * d14 + d15 * d15 < 1.0 && bid.is(oldbid)) {
                                    FastBlocks.setBlockInChunk(chunk, k2, l3, i3, newbid);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /** {@code addRice} (:740-754). */
    private void addRice(final Random random, final int chunkX, final int chunkZ, final ChunkAccess chunk) {
        if (random.nextInt(10) != 0) {
            return;
        }
        final Block crystalGrass = ModBlocks.CRYSTAL_GRASS.get();
        for (int i = 0; i < 5; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 128; posY > 40; --posY) {
                if (FastBlocks.getBlockInChunk(chunk, posX, posY, posZ).is(Blocks.AIR)
                        && FastBlocks.getBlockInChunk(chunk, posX, posY - 1, posZ).is(crystalGrass)) {
                    FastBlocks.setBlockInChunk(chunk, posX, posY, posZ, ModBlocks.RICE_PLANT.get().defaultBlockState());
                    break;
                }
            }
        }
    }

    /** {@code addQuinoa} (:756-770). */
    private void addQuinoa(final Random random, final int chunkX, final int chunkZ, final ChunkAccess chunk) {
        if (random.nextInt(20) != 0) {
            return;
        }
        final Block crystalGrass = ModBlocks.CRYSTAL_GRASS.get();
        for (int i = 0; i < 5; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 128; posY > 40; --posY) {
                if (FastBlocks.getBlockInChunk(chunk, posX, posY, posZ).is(Blocks.AIR)
                        && FastBlocks.getBlockInChunk(chunk, posX, posY - 1, posZ).is(crystalGrass)) {
                    FastBlocks.setBlockInChunk(chunk, posX, posY, posZ, ModBlocks.QUINOA_0.get().defaultBlockState());
                    break;
                }
            }
        }
    }

    /** {@code addCrystalFlowers} (:772-808): one colour per chunk. */
    private void addCrystalFlowers(final Random random, final int chunkX, final int chunkZ, final ChunkAccess chunk) {
        if (random.nextInt(3) != 0) {
            return;
        }
        final Block crystalGrass = ModBlocks.CRYSTAL_GRASS.get();
        int howmany = 0;
        howmany = 1 + random.nextInt(13);
        final int what = random.nextInt(4);
        for (int i = 0; i < howmany; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            int posY = 128;
            while (posY > 40) {
                if (FastBlocks.getBlockInChunk(chunk, posX, posY, posZ).is(Blocks.AIR)
                        && FastBlocks.getBlockInChunk(chunk, posX, posY - 1, posZ).is(crystalGrass)) {
                    if (what == 0) {
                        FastBlocks.setBlockInChunk(chunk, posX, posY, posZ, ModBlocks.CRYSTAL_FLOWER_RED.get().defaultBlockState());
                        break;
                    }
                    if (what == 1) {
                        FastBlocks.setBlockInChunk(chunk, posX, posY, posZ, ModBlocks.CRYSTAL_FLOWER_GREEN.get().defaultBlockState());
                        break;
                    }
                    if (what == 2) {
                        FastBlocks.setBlockInChunk(chunk, posX, posY, posZ, ModBlocks.CRYSTAL_FLOWER_BLUE.get().defaultBlockState());
                        break;
                    }
                    if (what == 3) {
                        FastBlocks.setBlockInChunk(chunk, posX, posY, posZ, ModBlocks.CRYSTAL_FLOWER_YELLOW.get().defaultBlockState());
                        break;
                    }
                    break;
                } else {
                    --posY;
                }
            }
        }
    }

    /**
     * {@code populate} (:315-328): the populate seed from the world seed (:320-323), then
     * {@code biomegenbase.decorate(world, rand, k, l)} (:325) with the biome read at {@code (k + 16, l + 16)} (:319),
     * which in this single-biome dimension is always {@code orespawn:crystal}. {@code BiomeGenBase.decorate} is
     * {@code theBiomeDecorator.decorateChunk}, i.e. the shared {@link BiomeDecorator#decorateChunk} with the counts
     * {@code setCrystalCreatures} leaves on it ({@link #crystalDecorator}).
     *
     * <p>What that does in this terrain, with the 1.7.10 target checks of the shared generators: the vanilla ores
     * replace only {@code stone}, the sand/clay/gravel disks only dirt, grass or clay next to water, the pumpkin needs
     * vanilla grass, the reeds grass/dirt/sand, the springs stone - Kyanite, Crystal Grass and bedrock match none of
     * them, so all of these draw their random numbers and place nothing. What remains are the two unconditional
     * mushroom patches ({@code nextInt(4)}, {@code nextInt(8)}), which stay wherever it is dark above an opaque block -
     * the maze floor on Y 24 among others. Before R21 the same steps ran as vanilla 1.21.1 placed features
     * (other random sequence and spread).
     *
     * <p>{@code this.rand} of the original is reseeded here from nothing but the world seed and the chunk position,
     * so {@link LegacyChunk#populateRandom} is exactly that random, not a replacement.
     *
     * <p>Order: the decorator first, then {@code super}, which places structure pieces and the placed features of
     * biome modifiers - where {@code OreSpawnWorld.generate} (:176, FML world generator, run after {@code populate})
     * ends up; {@link LegacyStructurePass} runs last (DECISIONS R24). Populate window: as in {@code ChunkProviderOreSpawn2}, every read and write stays in chunks C and C + 1,
     * inside the 1.21.1 write radius, so decorating chunk C runs the populate of C itself.
     *
     * <p>PORT: {@code BlockFalling.fallInstantly = true} around the decorator (:316, :327) has no counterpart. It only
     * matters for sand and gravel placed by the decorator, and in this terrain the decorator places neither (see
     * above); {@link LegacyWorld} would schedule their falling tick.
     */
    @Override
    public void applyBiomeDecoration(final WorldGenLevel level, final ChunkAccess chunk, final StructureManager structureManager) {
        final int par2 = chunk.getPos().x;
        final int par3 = chunk.getPos().z;
        final int k = par2 * 16;
        final int l = par3 * 16;
        final Random rand = LegacyChunk.populateRandom(level.getSeed(), par2, par3);
        // BiomeGenUtopianPlains keeps the BiomeGenBase default topBlock (grass); only WorldGenLakes reads it, and the
        // decorator runs no lake.
        crystalDecorator().decorateChunk(new LegacyWorld(level, Blocks.GRASS_BLOCK.defaultBlockState()), rand, k, l);
        super.applyBiomeDecoration(level, chunk, structureManager);
        // OreSpawnWorld.generate ran after populate under FML (OreSpawnMain.java:5035).
        com.swbr.orespawn.world.OreSpawnWorld.generate(level, chunk.getPos().x, chunk.getPos().z);
        LegacyStructurePass.apply(level, this, chunk.getPos());
    }

    /**
     * The decorator of the Crystal biome: {@code new BiomeGenUtopianPlains(BiomeCrystalID)} sets trees -999, flowers 4,
     * grass 6 (BiomeGenUtopianPlains.java:57-59), then {@code setCrystalCreatures} (WorldProviderOreSpawn5.java:27)
     * overwrites flowers, grass, trees, big mushrooms, mushrooms and reeds with -999 (BiomeGenUtopianPlains.java:184-189).
     * The -999 loops, like the counts 0 of dead bushes, water lilies and cacti, run no iteration and draw nothing,
     * which is what {@link BiomeDecorator} does for them; the tree loop still draws its {@code nextInt(10)}. The biome
     * does not override the tree pick, so it is {@link BiomeGenBase#getRandomWorldGenForTrees} (never reached).
     * A fresh instance per populate call, never shared between threads.
     */
    private static BiomeDecorator crystalDecorator() {
        return new BiomeDecorator(-999, -999, -999, BiomeGenBase::getRandomWorldGenForTrees) {
        };
    }

    /** No caves or ravines in {@code provideChunk}. */
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
     * {@code SpawnerAnimals.performWorldGenSpawning(world, biome, k + 8, l + 8, 16, 16, rand)} in
     * {@code populate} (:326). PORT: seeded like vanilla 1.21.1
     * ({@code NoiseBasedChunkGenerator.spawnOriginalMobs}); in 1.7.10 the spawns drew from the populate
     * random right after {@code decorate} ({@link #applyBiomeDecoration}), which a separate worldgen step
     * cannot continue.
     */
    @Override
    public void spawnOriginalMobs(final WorldGenRegion level) {
        final ChunkPos chunkpos = level.getCenter();
        final Holder<Biome> holder = level.getBiome(chunkpos.getWorldPosition().atY(level.getMaxBuildHeight() - 1));
        final WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(level.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(level, holder, chunkpos, worldgenrandom);
    }

    /** {@code new Block[65536]} (:206): 256 blocks high. */
    @Override
    public int getGenDepth() {
        return 256;
    }

    /** {@code b0 = 63} (:72): water fills Y 0..62. */
    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    /** Terrain and surface of the column, without maze and features (vanilla also answers from noise only). */
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
        return n.baseChunks.get(x >> 4, z >> 4, (par1, par2) -> {
            final Random rand = new Random(par1 * 341873128712L + par2 * 132897987541L);
            return this.generateTerrain(n, par1, par2, rand);
        });
    }

    @Override
    public void addDebugScreenInfo(final List<String> info, final RandomState random, final BlockPos pos) {
    }
}
