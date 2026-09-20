package com.swbr.orespawn.world.dimension.utopia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.world.dimension.ChunkTerrainCache;
import com.swbr.orespawn.world.gen.BiomeDecorator;
import com.swbr.orespawn.world.gen.BiomeGenBase;
import com.swbr.orespawn.world.gen.ChunkOreGenerator;
import com.swbr.orespawn.world.gen.LegacyChunk;
import com.swbr.orespawn.world.gen.LegacyWorld;
import com.swbr.orespawn.world.gen.MapGenCaves;
import com.swbr.orespawn.world.gen.MapGenRavine;
import com.swbr.orespawn.world.gen.NoiseGeneratorOctaves;
import com.swbr.orespawn.world.gen.NoiseGeneratorPerlin;
import com.swbr.orespawn.world.structure.LegacyStructurePass;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
 * Port of {@code ChunkProviderOreSpawn} (ChunkProviderOreSpawn.java:16-365), the generator of the Utopia
 * dimension {@code orespawn:utopia} ("Dimension-Utopia", DECISIONS R13; verhalten/world-03.md
 * "ChunkProviderOreSpawn"). Registered under the chunk generator type {@code orespawn:utopia};
 * {@code ChunkProviderOreSpawn3} (VillageMania) extends it, because the original is a line-for-line copy of
 * this class plus villages, lakes and dungeons.
 *
 * <table>
 * <tr><th>1.7.10</th><th>port</th></tr>
 * <tr><td>constructor: seven noise generators from {@code new Random(seed)} (:47-85)</td>
 * <td>{@link Terrain}, built in {@link #createState}</td></tr>
 * <tr><td>{@code provideChunk}: density terrain, {@code genTerrainBlocks} of the plains biome,
 * {@code MapGenCaves}, {@code MapGenRavine}, {@code ChunkOreGenerator} once (:161-173)</td>
 * <td>{@link #fillFromNoise}, in one pass on a 1.7.10 block array (shared world/gen ports)</td></tr>
 * <tr><td>{@code populate}: {@code biomegenbase.decorate}, {@code performWorldGenSpawning} (:267-280)</td>
 * <td>{@link #applyBiomeDecoration} ({@link BiomeDecorator}), {@link #spawnOriginalMobs}</td></tr>
 * <tr><td>{@code getPossibleCreatures}: no {@code monster} list at all (:315-317)</td>
 * <td>{@link #getMobsAt}</td></tr>
 * <tr><td>stronghold, mineshaft, scattered feature generators, only in {@code recreateStructures} (:358-364)</td>
 * <td>nothing: the original never placed a block with them (verhalten/world-03.md)</td></tr>
 * </table>
 *
 * <p><b>Seed.</b> 1.21.1 gives a generator no world seed before the carver stage. The noise is built in
 * {@link #createState}, which {@code ChunkMap} calls with the level seed before any chunk is generated (the
 * same approach as {@code ChunkProviderOreSpawn2}).
 *
 * <p><b>Populate window.</b> 1.7.10 populated chunk P once its +X/+Z neighbours existed and wrote into the
 * 16x16 area starting at {@code P * 16 + 8}, i.e. across four chunks. 1.21.1 lets a chunk's decoration write
 * only into the 3x3 chunks around it. PORT: decorating chunk C runs the 1.7.10 populate of chunk
 * {@code C - (1, 1)}; its area {@code (C - 1) * 16 + 8 .. C * 16 + 7} plus the widest feature reach (8 blocks)
 * stays inside chunks C-1 and C. Every original populate runs exactly once, with its own seed.
 */
public class ChunkProviderOreSpawn extends ChunkGenerator {

    public static final MapCodec<ChunkProviderOreSpawn> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource))
            .apply(instance, instance.stable(ChunkProviderOreSpawn::new)));

    /**
     * {@code BiomeGenBase.height_Default}, rootHeight 0.1 and heightVariation 0.2: {@code BiomeGenUtopianPlains}
     * never sets a height (client-1.7.10.jar {@code ahu.<clinit>}/{@code <init>}, verhalten/world-03.md
     * "Höhen je Biom"). One biome, so every entry of {@code biomesForGeneration} carries these numbers.
     */
    protected static final float ROOT_HEIGHT = 0.1F;
    protected static final float HEIGHT_VARIATION = 0.2F;

    /** {@code b0 = 63} in {@code func_147424_a} (:88): water below this Y. */
    protected static final int SEA_LEVEL = 63;

    /** {@code parabolicField} (:69-75). */
    private static final float[] PARABOLIC_FIELD = new float[25];

    static {
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                final float f = 10.0F / (float) Math.sqrt((double) ((float) (j * j + k * k) + 0.2F));
                PARABOLIC_FIELD[j + 2 + (k + 2) * 5] = f;
            }
        }
    }

    protected static final BlockState STONE = Blocks.STONE.defaultBlockState();
    protected static final BlockState WATER = Blocks.WATER.defaultBlockState();
    protected static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
    /** {@code BiomeGenBase.topBlock}: {@code BiomeGenUtopianPlains} keeps the {@code ahu.<init>} default grass. */
    protected static final BlockState TOP_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
    /** {@code BiomeGenBase.fillerBlock}: default dirt. */
    protected static final BlockState FILLER_BLOCK = Blocks.DIRT.defaultBlockState();

    /** Seed-bound noise, built once per level in {@link #createState}. */
    private volatile Terrain terrain;

    public ChunkProviderOreSpawn(final BiomeSource biomeSource) {
        super(biomeSource);
    }

    /**
     * The noise fields of the constructor (:60-67), created from {@code new Random(seed)} in the original
     * order. {@code TerrainGen.getModdedNoiseGenerators} (:76-84) has no port. Also holds the column-height
     * cache for {@link #getBaseHeight}, so a new seed starts with an empty cache.
     */
    protected static final class Terrain {
        final long seed;
        /** {@code field_147431_j}, 16 octaves. */
        final NoiseGeneratorOctaves minLimitNoise;
        /** {@code field_147432_k}, 16 octaves. */
        final NoiseGeneratorOctaves maxLimitNoise;
        /** {@code field_147429_l}, 8 octaves. */
        final NoiseGeneratorOctaves mainNoise;
        /** {@code field_147430_m}, 4 simplex octaves: the surface depth noise ({@code stoneNoise}). */
        final NoiseGeneratorPerlin surfaceNoise;
        /** {@code noiseGen6}, 16 octaves: the depth noise. */
        final NoiseGeneratorOctaves depthNoise;
        /** Chunk key to {@code [topStoneY, topWaterY]} per column of the bare density terrain. */
        final Map<Long, short[]> columnTops = new ConcurrentHashMap<>();
        /** fix2 (R26): chunk terrain arrays for {@link #getBaseColumn}. */
        final ChunkTerrainCache<BlockState[]> baseChunks = new ChunkTerrainCache<>(32);

        Terrain(final long seed) {
            this.seed = seed;
            final Random rand = new Random(seed);
            this.minLimitNoise = new NoiseGeneratorOctaves(rand, 16);
            this.maxLimitNoise = new NoiseGeneratorOctaves(rand, 16);
            this.mainNoise = new NoiseGeneratorOctaves(rand, 8);
            this.surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
            // noiseGen5 (10 octaves, :65) is never read, but it consumes the seed stream before noiseGen6.
            new NoiseGeneratorOctaves(rand, 10);
            this.depthNoise = new NoiseGeneratorOctaves(rand, 16);
            // mobSpawnerNoise (8 octaves, :67) comes last and is never read: not built.
        }
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(final HolderLookup<StructureSet> structureSetLookup,
                                                    final RandomState randomState, final long seed) {
        this.terrain = new Terrain(seed);
        return super.createState(structureSetLookup, randomState, seed);
    }

    protected final Terrain terrain() {
        Terrain t = this.terrain;
        if (t == null) {
            // PORT: only reachable if a caller asks for terrain before ChunkMap created the generator state.
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            t = new Terrain(server != null ? server.getWorldData().worldGenOptions().seed() : 0L);
            this.terrain = t;
        }
        return t;
    }

    // ---------------------------------------------------------------------------------------------------
    // provideChunk
    // ---------------------------------------------------------------------------------------------------

    /**
     * {@code provideChunk} (:161-173). Everything before {@code new Chunk(...)} runs on a 1.7.10 block array
     * ({@code (x * 16 + z) * 256 + y}, {@code null} = air), which is then written into the proto chunk; the ore
     * pass runs on the chunk, as in the original.
     *
     * <p>PORT: 1.21.1 splits generation into noise, surface and carver stages; the original's surface and cave
     * passes write into the same array and share {@code this.rand} with the ores, so they all run here and
     * {@link #buildSurface} and {@link #applyCarvers} are empty. The VillageMania copy additionally started
     * village and scattered-feature structures here (ChunkProviderOreSpawn3.java:166-169); in 1.21.1 structure
     * starts are their own stage, driven by the structure sets.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final Blender blender, final RandomState randomState,
                                                        final StructureManager structureManager, final ChunkAccess chunk) {
        final Terrain t = this.terrain();
        final int par1 = chunk.getPos().x;
        final int par2 = chunk.getPos().z;
        // this.rand.setSeed(par1 * 341873128712L + par2 * 132897987541L) (:162); LegacyRandomSource is the
        // java.util.Random LCG and the type ChunkOreGenerator takes.
        final RandomSource rand = new LegacyRandomSource((long) par1 * 341873128712L + (long) par2 * 132897987541L);
        final BlockState[] ablock = new BlockState[65536];
        generateTerrain(t, par1, par2, ablock);
        replaceBlocksForBiome(t, par1, par2, ablock, rand);
        new MapGenCaves().generate(t.seed, par1, par2, ablock, TOP_BLOCK);
        new MapGenRavine().generate(t.seed, par1, par2, ablock, TOP_BLOCK);
        LegacyChunk.fillChunk(chunk, ablock);
        // OreSpawnMain.Chunker.generateOresInChunk(worldObj, rand, par1 * 16, par2 * 16, chunk) (:170), once.
        ChunkOreGenerator.generateOresInChunk(rand, par1 * 16, par2 * 16, chunk);
        // chunk.generateSkylightMap() (:171): the 1.21.1 light engine lights the chunk in its own stage.
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * {@code func_147424_a} (:87-144): trilinear interpolation of the 5x33x5 density field into stone, water
     * below Y 63, and air.
     */
    protected static void generateTerrain(final Terrain t, final int chunkX, final int chunkZ, final BlockState[] blocks) {
        final int b0 = SEA_LEVEL;
        final double[] density = initializeNoiseField(t, chunkX * 4, 0, chunkZ * 4);
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
                    double d2 = density[k2 + k3];
                    double d3 = density[l2 + k3];
                    double d4 = density[i2 + k3];
                    double d5 = density[j2 + k3];
                    final double d6 = (density[k2 + k3 + 1] - d2) * d0;
                    final double d7 = (density[l2 + k3 + 1] - d3) * d0;
                    final double d8 = (density[i2 + k3 + 1] - d4) * d0;
                    final double d9 = (density[j2 + k3 + 1] - d5) * d0;
                    for (int l3 = 0; l3 < 8; ++l3) {
                        final double d10 = 0.25;
                        double d11 = d2;
                        double d12 = d3;
                        final double d13 = (d4 - d2) * d10;
                        final double d14 = (d5 - d3) * d10;
                        for (int i3 = 0; i3 < 4; ++i3) {
                            int j3 = (i3 + k * 4) << 12 | (0 + j1 * 4) << 8 | (k3 * 8 + l3);
                            final int short1 = 256;
                            j3 -= short1;
                            final double d15 = 0.25;
                            final double d16 = (d12 - d11) * d15;
                            double d17 = d11 - d16;
                            for (int k4 = 0; k4 < 4; ++k4) {
                                if ((d17 += d16) > 0.0) {
                                    blocks[j3 += short1] = STONE;
                                } else if (k3 * 8 + l3 < b0) {
                                    blocks[j3 += short1] = WATER;
                                } else {
                                    blocks[j3 += short1] = null;
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
     * {@code func_147423_a} (:175-261): the 5x33x5 density field. The AMPLIFIED branch (:201-204) read the
     * overworld's 1.7.10 {@code WorldType}; PORT: 1.21.1 has no world type a custom dimension could read, the
     * default branch is taken.
     */
    protected static double[] initializeNoiseField(final Terrain t, final int noiseX, final int noiseY, final int noiseZ) {
        final double[] density = new double[825];
        final double[] depthRegion = t.depthNoise.generateNoiseOctaves(null, noiseX, noiseZ, 5, 5, 200.0, 200.0, 0.5);
        final double[] mainRegion = t.mainNoise.generateNoiseOctaves(null, noiseX, noiseY, noiseZ, 5, 33, 5,
                8.555150000000001, 4.277575000000001, 8.555150000000001);
        final double[] minLimitRegion = t.minLimitNoise.generateNoiseOctaves(null, noiseX, noiseY, noiseZ, 5, 33, 5,
                684.412, 684.412, 684.412);
        final double[] maxLimitRegion = t.maxLimitNoise.generateNoiseOctaves(null, noiseX, noiseY, noiseZ, 5, 33, 5,
                684.412, 684.412, 684.412);
        int l = 0;
        int i1 = 0;
        for (int j1 = 0; j1 < 5; ++j1) {
            for (int k1 = 0; k1 < 5; ++k1) {
                float f = 0.0F;
                float f2 = 0.0F;
                float f3 = 0.0F;
                final int b0 = 2;
                for (int l2 = -b0; l2 <= b0; ++l2) {
                    for (int i2 = -b0; i2 <= b0; ++i2) {
                        final float f4 = ROOT_HEIGHT;
                        final float f5 = HEIGHT_VARIATION;
                        final float f6 = PARABOLIC_FIELD[l2 + 2 + (i2 + 2) * 5] / (f4 + 2.0F);
                        // biomegenbase2.rootHeight > biomegenbase.rootHeight halves f6 (:206-208): never, one biome.
                        f += f5 * f6;
                        f2 += f4 * f6;
                        f3 += f6;
                    }
                }
                f /= f3;
                f2 /= f3;
                f = f * 0.9F + 0.1F;
                f2 = (f2 * 4.0F - 1.0F) / 8.0F;
                double d6 = depthRegion[i1] / 8000.0;
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
                double d7 = (double) f2;
                final double d8 = (double) f;
                d7 += d6 * 0.2;
                d7 = d7 * 8.5 / 8.0;
                final double d9 = 8.5 + d7 * 4.0;
                for (int j2 = 0; j2 < 33; ++j2) {
                    double d10 = ((double) j2 - d9) * 12.0 * 128.0 / 256.0 / d8;
                    if (d10 < 0.0) {
                        d10 *= 4.0;
                    }
                    final double d11 = minLimitRegion[l] / 512.0;
                    final double d12 = maxLimitRegion[l] / 512.0;
                    final double d13 = (mainRegion[l] / 10.0 + 1.0) / 2.0;
                    // MathHelper.denormalizeClamp (qh.b(DDD)) is Mth.clampedLerp(start, end, delta).
                    double d14 = Mth.clampedLerp(d11, d12, d13) - d10;
                    if (j2 > 29) {
                        final double d15 = (double) ((float) (j2 - 29) / 3.0F);
                        d14 = d14 * (1.0 - d15) + -10.0 * d15;
                    }
                    density[l] = d14;
                    ++l;
                }
            }
        }
        return density;
    }

    /**
     * {@code replaceBlocksForBiome} (:146-155) with {@code BiomeGenBase.genTerrainBlocks} of the plains biome
     * per column (grass on dirt, temperature 0.7). The stone noise is read as {@code [l + k * 16]}; together with
     * the transposed index of {@code genBiomeTerrain} that is original behaviour (see {@link BiomeGenBase}).
     */
    protected static void replaceBlocksForBiome(final Terrain t, final int chunkX, final int chunkZ,
                                                final BlockState[] blocks, final RandomSource rand) {
        final double d0 = 0.03125;
        final double[] stoneNoise = t.surfaceNoise.getRegion(new double[256], (double) (chunkX * 16),
                (double) (chunkZ * 16), 16, 16, d0 * 2.0, d0 * 2.0, 1.0);
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeGenBase.genBiomeTerrain(rand, blocks, chunkX * 16 + k, chunkZ * 16 + l, stoneNoise[l + k * 16],
                        TOP_BLOCK, FILLER_BLOCK, WorldProviderOreSpawn.TEMPERATURE);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------
    // populate
    // ---------------------------------------------------------------------------------------------------

    /**
     * The 1.7.10 {@code populate} of the chunk diagonally behind this one (class javadoc, "Populate window"), then
     * the structure pieces ({@code super}), then {@code OreSpawnWorld.generate}, then
     * {@link LegacyStructurePass} (DECISIONS R24).
     *
     * <p>PORT (R24): the vanilla structure pieces (mineshafts, strongholds, in VillageMania the villages) came before
     * the populate in 1.7.10 and now come after it. The order is chosen for the {@code orespawn:legacy} structures:
     * their structure sets are on {@code top_layer_modification}, the last step of {@code super}, and FML ran
     * {@code OreSpawnWorld.generate} after {@code populate}, so trees, flowers and village parts must not punch holes
     * into them. The class javadoc of {@link LegacyStructurePass} explains why the pass is needed on top.
     */
    @Override
    public void applyBiomeDecoration(final WorldGenLevel level, final ChunkAccess chunk,
                                     final StructureManager structureManager) {
        final ChunkPos pos = chunk.getPos();
        this.populate(level, structureManager, pos.x - 1, pos.z - 1);
        super.applyBiomeDecoration(level, chunk, structureManager);
        // FML ran OreSpawnWorld.generate after populate (OreSpawnMain.java:5035); decorated chunk C, not the populate chunk C-(1,1)
        com.swbr.orespawn.world.OreSpawnWorld.generate(level, pos.x, pos.z);
        LegacyStructurePass.apply(level, this, pos);
    }

    /**
     * {@code populate} (:267-280). {@code BlockFalling.fallInstantly} is set to false before and after (:268,
     * :279) and has no 1.21.1 equivalent; the biome is read at {@code (k + 16, l + 16)} (:271), which in a
     * single-biome dimension is always {@code orespawn:utopia}.
     *
     * @param par2 1.7.10 chunk x of the populated chunk
     * @param par3 1.7.10 chunk z of the populated chunk
     */
    protected void populate(final WorldGenLevel level, final StructureManager structureManager, final int par2,
                            final int par3) {
        final int k = par2 * 16;
        final int l = par3 * 16;
        // The populate seed of :272-275: (x * a + z * b) ^ seed with two odd longs from the world seed.
        final Random rand = LegacyChunk.populateRandom(level.getSeed(), par2, par3);
        BiomeDecorator.utopianPlains().decorateChunk(new LegacyWorld(level, TOP_BLOCK), rand, k, l);
        // SpawnerAnimals.performWorldGenSpawning (:278) is spawnOriginalMobs.
    }

    /**
     * {@code SpawnerAnimals.performWorldGenSpawning} (:278), as {@code NoiseBasedChunkGenerator} does it.
     *
     * <p>PORT: the original spawned into the 16x16 area at {@code (k + 8, l + 8)} with the populate random
     * after the decorator; the 1.21.1 routine uses the chunk itself and its own decoration seed. The creature
     * list of the biome comes from {@code world.spawn.ConfigSpawnsBiomeModifier} (entries of {@code orespawn:utopia}
     * in data/orespawn/spawn_table/spawns.json).
     */
    @Override
    public void spawnOriginalMobs(final WorldGenRegion level) {
        final ChunkPos chunkpos = level.getCenter();
        final Holder<Biome> holder = level.getBiome(chunkpos.getWorldPosition().atY(level.getMaxBuildHeight() - 1));
        final WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(level.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(level, holder, chunkpos, worldgenrandom);
    }

    /**
     * {@code getPossibleCreatures} (:301-348): {@code monster} returns {@code null}, so nothing hostile spawns
     * naturally in Utopia - not even the vanilla base monsters the biome lists. The other lists are the biome's,
     * filtered for {@code EntityHorse}, which the biome never contains (verhalten/world-03.md); the ambient
     * list was additionally cached on first use, which changes nothing either.
     */
    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(final Holder<Biome> biome,
                                                                      final StructureManager structureManager,
                                                                      final MobCategory category, final BlockPos pos) {
        if (category == MobCategory.MONSTER && !this.hasNaturalMonsters()) {
            return WeightedRandomList.create();
        }
        return super.getMobsAt(biome, structureManager, category, pos);
    }

    /** False for Utopia (:315-317); {@code ChunkProviderOreSpawn3} returns the biome list unchanged. */
    protected boolean hasNaturalMonsters() {
        return false;
    }

    /** Surface pass runs in {@link #fillFromNoise}. */
    @Override
    public void buildSurface(final WorldGenRegion level, final StructureManager structureManager,
                             final RandomState random, final ChunkAccess chunk) {
    }

    /** {@code MapGenCaves} and {@code MapGenRavine} run in {@link #fillFromNoise}. */
    @Override
    public void applyCarvers(final WorldGenRegion level, final long seed, final RandomState random,
                             final BiomeManager biomeManager, final StructureManager structureManager,
                             final ChunkAccess chunk, final GenerationStep.Carving step) {
    }

    /** {@code Block[65536]} (:163): 256 blocks high. */
    @Override
    public int getGenDepth() {
        return 256;
    }

    /** Water up to and including Y 62 (:88, :126). */
    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    /**
     * Height of the bare density terrain, like the vanilla noise generator (no surface, caves or ores). Village
     * jigsaw pieces ask this for every piece, so the per-column tops of a chunk are cached with the noise.
     */
    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types type, final LevelHeightAccessor level,
                             final RandomState random) {
        final short[] tops = columnTops(this.terrain(), x >> 4, z >> 4);
        final int column = ((x & 15) * 16 + (z & 15)) * 2;
        int top = -1;
        if (type.isOpaque().test(STONE)) {
            top = Math.max(top, tops[column]);
        }
        if (type.isOpaque().test(WATER)) {
            top = Math.max(top, tops[column + 1]);
        }
        if (top < level.getMinBuildHeight() || top >= level.getMaxBuildHeight()) {
            return level.getMinBuildHeight();
        }
        return top + 1;
    }

    private static short[] columnTops(final Terrain t, final int chunkX, final int chunkZ) {
        final long key = ChunkPos.asLong(chunkX, chunkZ);
        short[] tops = t.columnTops.get(key);
        if (tops != null) {
            return tops;
        }
        final BlockState[] blocks = new BlockState[65536];
        generateTerrain(t, chunkX, chunkZ, blocks);
        tops = new short[512];
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                short topStone = -1;
                short topWater = -1;
                for (int y = 255; y >= 0 && (topStone < 0 || topWater < 0); --y) {
                    final BlockState state = blocks[x << 12 | z << 8 | y];
                    if (state == STONE && topStone < 0) {
                        topStone = (short) y;
                    } else if (state == WATER && topWater < 0) {
                        topWater = (short) y;
                    }
                }
                tops[(x * 16 + z) * 2] = topStone;
                tops[(x * 16 + z) * 2 + 1] = topWater;
            }
        }
        if (t.columnTops.size() > 1024) {
            t.columnTops.clear();
        }
        t.columnTops.put(key, tops);
        return tops;
    }

    /**
     * The bare density terrain of the column. fix2 (DECISIONS R26): the chunk's terrain array comes from
     * {@link ChunkTerrainCache}, so a structure pass asking for every column of its footprint generates each chunk
     * once instead of once per column; same blocks.
     */
    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor height, final RandomState random) {
        final BlockState[] blocks = baseTerrain(this.terrain(), x >> 4, z >> 4);
        final int lx = x & 15;
        final int lz = z & 15;
        final BlockState[] column = new BlockState[height.getHeight()];
        for (int i = 0; i < column.length; ++i) {
            final int y = height.getMinBuildHeight() + i;
            final BlockState state = y >= 0 && y < 256 ? blocks[lx << 12 | lz << 8 | y] : null;
            column[i] = state == null ? Blocks.AIR.defaultBlockState() : state;
        }
        return new NoiseColumn(height.getMinBuildHeight(), column);
    }

    /** The cached {@code generateTerrain} array of one chunk; read only. */
    private static BlockState[] baseTerrain(final Terrain t, final int chunkX, final int chunkZ) {
        return t.baseChunks.get(chunkX, chunkZ, (cx, cz) -> {
            final BlockState[] blocks = new BlockState[65536];
            generateTerrain(t, cx, cz, blocks);
            return blocks;
        });
    }

    @Override
    public void addDebugScreenInfo(final List<String> info, final RandomState random, final BlockPos pos) {
    }
}
