package com.swbr.orespawn.world.dimension.mining;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.world.dimension.ChunkTerrainCache;
import com.swbr.orespawn.world.gen.BiomeDecorator;
import com.swbr.orespawn.world.gen.BiomeGenHills;
import com.swbr.orespawn.world.gen.ChunkOreGenerator;
import com.swbr.orespawn.world.gen.LegacyChunk;
import com.swbr.orespawn.world.gen.LegacyWorld;
import com.swbr.orespawn.world.gen.MapGenCaves;
import com.swbr.orespawn.world.gen.MapGenRavine;
import com.swbr.orespawn.world.gen.MapGenStronghold;
import com.swbr.orespawn.world.gen.NoiseGeneratorOctaves;
import com.swbr.orespawn.world.gen.NoiseGeneratorPerlin;
import com.swbr.orespawn.world.gen.WorldGenDungeons;
import com.swbr.orespawn.world.gen.WorldGenLakes;
import com.swbr.orespawn.world.structure.LegacyStructurePass;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Port of {@code ChunkProviderOreSpawn2} (ChunkProviderOreSpawn2.java:16-430), the generator of the Mining
 * dimension {@code orespawn:mining} ("Dimension-Extreme", DECISIONS R13; verhalten/world-03.md
 * "ChunkProviderOreSpawn2"). Registered under the chunk generator type {@code orespawn:mining}.
 *
 * <p>Where the original's steps land in 1.21.1:
 * <table>
 * <tr><th>1.7.10</th><th>port</th></tr>
 * <tr><td>{@code provideChunk}: density terrain, Extreme Hills surface, {@code MapGenCaves},
 * {@code MapGenRavine}, {@code ChunkOreGenerator} 1x or 3x (:163-179)</td>
 * <td>{@link #fillFromNoise}, all of it in one pass on a 1.7.10 block array, so the shared {@code this.rand}
 * keeps its order from surface to ores</td></tr>
 * <tr><td>{@code populate} (:273-328)</td><td>{@link #applyBiomeDecoration}, the shared 1.7.10 Java decorator
 * ({@link BiomeGenHills#decorate}, {@link BiomeDecorator}; DECISIONS R21)</td></tr>
 * <tr><td>{@code getPossibleCreatures} dino list (:349-413)</td><td>{@code world.spawn.ConfigSpawnsBiomeModifier}: entries of {@code orespawn:mining} in {@code data/orespawn/spawn_table/spawns.json}, together with the addSpawn entries of the shared {@code extremeHills} biome</td></tr>
 * <tr><td>{@code WorldProviderOreSpawn2}</td><td>{@link WorldProviderOreSpawn2}, dimension and biome JSON</td></tr>
 * </table>
 *
 * <p><b>Seed.</b> 1.21.1 hands a chunk generator no world seed before {@code applyCarvers}; the noise
 * generators are built from it in the constructor of the original. The port captures it in
 * {@link #createState}, which {@code ChunkMap} calls with the level seed before any chunk is generated.
 */
public class ChunkProviderOreSpawn2 extends ChunkGenerator {

    public static final MapCodec<ChunkProviderOreSpawn2> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource))
            .apply(instance, instance.stable(ChunkProviderOreSpawn2::new)));

    /**
     * {@code BiomeGenBase.height_MidHills} of {@code extremeHills}: rootHeight 1.0, heightVariation 0.5
     * (client-1.7.10.jar {@code ahu.<clinit>}, verhalten/world-03.md "Höhen je Biom"). The dimension has one
     * biome, so every entry of {@code biomesForGeneration} carries these two numbers.
     */
    private static final float ROOT_HEIGHT = 1.0F;
    private static final float HEIGHT_VARIATION = 0.5F;

    /** {@code b0 = 63} in {@code func_147424_a} (:90): water below this Y. */
    private static final int SEA_LEVEL = 63;

    /** {@code parabolicField} (:71-77). */
    private static final float[] PARABOLIC_FIELD = new float[25];

    static {
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                final float f = 10.0F / (float) Math.sqrt((double) ((float) (j * j + k * k) + 0.2F));
                PARABOLIC_FIELD[j + 2 + (k + 2) * 5] = f;
            }
        }
    }

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();

    /** Seed-bound noise, built once per level in {@link #createState}. */
    private volatile Terrain terrain;

    public ChunkProviderOreSpawn2(final BiomeSource biomeSource) {
        super(biomeSource);
    }

    /**
     * The noise fields of the constructor (:48-87), created from {@code new Random(seed)} in the original
     * order. {@code TerrainGen.getModdedNoiseGenerators} (:78-86) let other Forge mods swap them; there is no
     * such hook in the port.
     */
    private static final class Terrain {
        final long seed;
        /** {@code field_147431_j}, 16 octaves, the lower density limit. */
        final NoiseGeneratorOctaves minLimitNoise;
        /** {@code field_147432_k}, 16 octaves, the upper density limit. */
        final NoiseGeneratorOctaves maxLimitNoise;
        /** {@code field_147429_l}, 8 octaves, the blend between the two. */
        final NoiseGeneratorOctaves mainNoise;
        /** {@code field_147430_m}, 4 simplex octaves, the surface depth noise ({@code stoneNoise}). */
        final NoiseGeneratorPerlin surfaceNoise;
        /** {@code noiseGen6}, 16 octaves, the depth noise. */
        final NoiseGeneratorOctaves depthNoise;
        /** fix2 (R26): chunk terrain arrays for {@link #getBaseHeight} and {@link #getBaseColumn}. */
        final ChunkTerrainCache<BlockState[]> baseChunks = new ChunkTerrainCache<>(32);

        Terrain(final long seed) {
            this.seed = seed;
            final Random rand = new Random(seed);
            this.minLimitNoise = new NoiseGeneratorOctaves(rand, 16);
            this.maxLimitNoise = new NoiseGeneratorOctaves(rand, 16);
            this.mainNoise = new NoiseGeneratorOctaves(rand, 8);
            this.surfaceNoise = new NoiseGeneratorPerlin(rand, 4);
            // noiseGen5 (10 octaves) is never read, but it consumes the seed stream before noiseGen6.
            new NoiseGeneratorOctaves(rand, 10);
            this.depthNoise = new NoiseGeneratorOctaves(rand, 16);
            // mobSpawnerNoise (8 octaves) would come last and is never read: not built.
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

    private Terrain terrain() {
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
     * {@code provideChunk} (:163-179). Everything the original did before {@code new Chunk(...)} runs on a
     * 1.7.10 block array ({@code (x * 16 + z) * 256 + y}, {@code null} = air), which is then written into the
     * proto chunk; the ore passes run on the chunk, as in the original.
     *
     * <p>PORT: 1.21.1 splits generation into noise, surface and carver stages; the original's surface and
     * cave passes are ports that write into the same array, so they run here and {@link #buildSurface} and
     * {@link #applyCarvers} are empty.
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final Blender blender, final RandomState randomState,
                                                        final StructureManager structureManager, final ChunkAccess chunk) {
        final Terrain t = this.terrain();
        final int par1 = chunk.getPos().x;
        final int par2 = chunk.getPos().z;
        // this.rand.setSeed(par1 * 341873128712L + par2 * 132897987541L) (:164); LegacyRandomSource is the
        // java.util.Random LCG, and it is the type ChunkOreGenerator takes.
        final RandomSource rand = new LegacyRandomSource((long) par1 * 341873128712L + (long) par2 * 132897987541L);
        final BlockState[] ablock = new BlockState[65536];
        generateTerrain(t, par1, par2, ablock);
        final BlockState topBlock = replaceBlocksForBiome(t, par1, par2, ablock, rand);
        new MapGenCaves().generate(t.seed, par1, par2, ablock, topBlock);
        new MapGenRavine().generate(t.seed, par1, par2, ablock, topBlock);
        LegacyChunk.fillChunk(chunk, ablock);
        ChunkOreGenerator.generateOresInChunk(rand, par1 * 16, par2 * 16, chunk);
        if (TweakStats.LessOre() == 0) {
            ChunkOreGenerator.generateOresInChunk(rand, par1 * 16, par2 * 16, chunk);
            ChunkOreGenerator.generateOresInChunk(rand, par1 * 16, par2 * 16, chunk);
        }
        // chunk.generateSkylightMap() (:177): the 1.21.1 light engine lights the chunk in its own stage.
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * {@code func_147424_a} (:89-146): trilinear interpolation of the 5x33x5 density field into stone, water
     * below Y 63, and air.
     */
    private static void generateTerrain(final Terrain t, final int chunkX, final int chunkZ,
                                        final BlockState[] blocks) {
        final byte b0 = SEA_LEVEL;
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
                            final short short1 = 256;
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
     * {@code func_147423_a} (:181-267): the 5x33x5 density field. The AMPLIFIED branch (:207-210) needs the
     * 1.7.10 overworld {@code WorldType}; 1.21.1 has no world type a custom dimension could read, so the
     * default branch is taken.
     */
    private static double[] initializeNoiseField(final Terrain t, final int noiseX, final int noiseY,
                                                 final int noiseZ) {
        final double[] density = new double[825];
        final double[] depthRegion = t.depthNoise.generateNoiseOctaves(null, noiseX, noiseZ, 5, 5,
                200.0, 200.0, 0.5);
        final double[] mainRegion = t.mainNoise.generateNoiseOctaves(null, noiseX, noiseY, noiseZ,
                5, 33, 5, 8.555150000000001, 4.277575000000001, 8.555150000000001);
        final double[] minLimitRegion = t.minLimitNoise.generateNoiseOctaves(null, noiseX, noiseY,
                noiseZ, 5, 33, 5, 684.412, 684.412, 684.412);
        final double[] maxLimitRegion = t.maxLimitNoise.generateNoiseOctaves(null, noiseX, noiseY,
                noiseZ, 5, 33, 5, 684.412, 684.412, 684.412);
        int l = 0;
        int i1 = 0;
        for (int j1 = 0; j1 < 5; ++j1) {
            for (int k1 = 0; k1 < 5; ++k1) {
                float f = 0.0F;
                float f2 = 0.0F;
                float f3 = 0.0F;
                final byte b0 = 2;
                for (int l2 = -b0; l2 <= b0; ++l2) {
                    for (int i2 = -b0; i2 <= b0; ++i2) {
                        final float f4 = ROOT_HEIGHT;
                        final float f5 = HEIGHT_VARIATION;
                        float f6 = PARABOLIC_FIELD[l2 + 2 + (i2 + 2) * 5] / (f4 + 2.0F);
                        // biomegenbase2.rootHeight > biomegenbase.rootHeight: never, single biome (:212-214).
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
                    // MathHelper.denormalizeClamp (qh.b(DDD)) = Mth.clampedLerp(start, end, delta).
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
     * {@code replaceBlocksForBiome} (:148-157) with {@code BiomeGenHills.genTerrainBlocks} per column. The
     * stone noise is read as {@code [l + k * 16]} and the column coordinates are passed untransposed, so
     * together with {@code genBiomeTerrain} the surface is mirrored along the chunk diagonal - 1.7.10
     * behaviour, kept.
     *
     * @return the top block of the last column, which the carvers read (see
     *         {@link BiomeGenHills#genTerrainBlocks})
     */
    private static BlockState replaceBlocksForBiome(final Terrain t, final int chunkX, final int chunkZ,
                                                    final BlockState[] blocks, final RandomSource rand) {
        final double d0 = 0.03125;
        final double[] stoneNoise = t.surfaceNoise.getRegion(new double[256], (double) (chunkX * 16),
                (double) (chunkZ * 16), 16, 16, d0 * 2.0, d0 * 2.0, 1.0);
        BlockState topBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                topBlock = BiomeGenHills.genTerrainBlocks(BiomeGenHills.TYPE_NORMAL, rand, blocks,
                        chunkX * 16 + k, chunkZ * 16 + l, stoneNoise[l + k * 16],
                        WorldProviderOreSpawn2.TEMPERATURE);
            }
        }
        return topBlock;
    }

    // ---------------------------------------------------------------------------------------------------
    // populate
    // ---------------------------------------------------------------------------------------------------

    /**
     * The 1.7.10 {@code populate} of this chunk ({@link #populate}), then the structure pieces, then
     * {@code OreSpawnWorld.generate}, then {@link LegacyStructurePass} (DECISIONS R24).
     *
     * <p>Mineshaft and stronghold pieces (:283-287) are placed by {@code super.applyBiomeDecoration}. PORT (R24): the
     * original placed them before the populate; now they come after it, because the same call places the
     * {@code orespawn:legacy} structures on {@code top_layer_modification}, and those must not be overwritten by lakes,
     * dungeons, ores and trees of the populate (FML ran {@code OreSpawnWorld.generate} after {@code populate}). The biome
     * JSON lists no features (DECISIONS R21), so that call places nothing else.
     * DECISIONS R18: the original provider calls both generators, so they are in. Mineshaft starts come from the
     * vanilla {@code mineshafts} set through the biome tag {@code has_structure/mineshaft}. Strongholds come from
     * {@code orespawn:strongholds}: the default {@code MapGenStronghold} of :54 has exactly three positions on its
     * own 1.7.10 ring ({@link MapGenStronghold}); the vanilla {@code strongholds} set would place up to 128 on the
     * 1.21.1 rings, so the biome is not in {@code has_structure/stronghold}. Scattered features (:286): none can
     * start in Extreme Hills, nothing to port.
     *
     * <p><b>Populate window.</b> 1.7.10 populated chunk P once its +x/+z neighbours existed, starting every step at
     * {@code P * 16 + 8 .. P * 16 + 23} (ores at {@code P * 16 + 0 .. 15}, vein centre +8). The widest reach of any
     * step is 8 blocks (lake box, flower, grass and pumpkin spread; a big oak's branches of at most 5 plus
     * leaves of 2), so every read and write stays inside chunks P and P + 1 on both axes. That is within the
     * 1.21.1 write radius of 1 around the decorated chunk, so decorating chunk C runs the populate of C itself.
     * Utopia runs the populate of C - (1, 1) instead (its javadoc); both stay inside the radius.
     * {@code R21GameTests} checks this on a 5x5 field of full chunks.
     */
    @Override
    public void applyBiomeDecoration(final WorldGenLevel level, final ChunkAccess chunk,
                                     final StructureManager structureManager) {
        final ChunkPos pos = chunk.getPos();
        this.populate(level, pos.x, pos.z);
        super.applyBiomeDecoration(level, chunk, structureManager);
        // FML ran OreSpawnWorld.generate after populate (OreSpawnMain.java:5035): the Mining branch
        // (OreSpawnWorld.java:44-99) starts with OreSpawnWorldOres.generateMining on the FML chunk random.
        com.swbr.orespawn.world.OreSpawnWorld.generate(level, pos.x, pos.z);
        LegacyStructurePass.apply(level, this, pos);
    }

    /**
     * {@code populate} (:273-328), before the structures (DECISIONS R24); {@code OreSpawnWorld.generate}, which FML ran
     * right after it, follows in {@link #applyBiomeDecoration}. Order inside as in the original, with these PORT
     * deviations:
     * <ul>
     * <li>{@code WorldGenDungeons} (:306-311) is the shared port {@link WorldGenDungeons}; its chest loot table and
     * spawner pick are documented there.</li>
     * <li>{@code decorate} (:312) is {@link BiomeGenHills#decorate}: the whole Extreme Hills
     * {@link BiomeDecorator}, then emeralds and silverfish.</li>
     * <li>{@code performWorldGenSpawning} (:313) is {@link #spawnOriginalMobs}.</li>
     * <li>Ice and snow (:314-326): {@code isBlockFreezable} and {@code func_147478_e} return false first thing
     * above temperature 0.15; the biome has 0.8 (WorldProviderOreSpawn2.java:22) and stays above 0.15 at every
     * height ({@link LegacyWorld#isBlockFreezable}). The loop draws no random number and is omitted.</li>
     * <li>{@code BlockFalling.fallInstantly} (:274, :327) has no 1.21.1 equivalent; a falling block placed here
     * gets its scheduled tick as through {@code onBlockAdded} ({@link LegacyWorld#setBlock}).</li>
     * </ul>
     *
     * @param par2 chunk x of the populated chunk
     * @param par3 chunk z of the populated chunk
     */
    private void populate(final WorldGenLevel level, final int par2, final int par3) {
        final int k = par2 * 16;
        final int l = par3 * 16;
        final long worldSeed = level.getSeed();
        final Random rand = LegacyChunk.populateRandom(worldSeed, par2, par3);
        // BiomeGenBase.topBlock is only compared with mycelium by WorldGenLakes; Extreme Hills never has it.
        final LegacyWorld world = new LegacyWorld(level, Blocks.GRASS_BLOCK.defaultBlockState());
        final boolean flag = false;
        // biomegenbase != desert && != desertHills: always true here (:288).
        if (!flag && rand.nextInt(4) == 0) {
            final int k2 = k + rand.nextInt(16) + 8;
            final int l2 = rand.nextInt(256);
            final int i2 = l + rand.nextInt(16) + 8;
            new WorldGenLakes(WATER).generate(world, rand, k2, l2, i2);
        }
        if (!flag && rand.nextInt(8) == 0) {
            final int k2 = k + rand.nextInt(16) + 8;
            final int l2 = rand.nextInt(rand.nextInt(248) + 8);
            final int i2 = l + rand.nextInt(16) + 8;
            if (l2 < 63 || rand.nextInt(10) == 0) {
                new WorldGenLakes(LAVA).generate(world, rand, k2, l2, i2);
            }
        }
        for (int k2 = 0; k2 < 8; ++k2) {
            final int l2 = k + rand.nextInt(16) + 8;
            final int i2 = rand.nextInt(256);
            final int j2 = l + rand.nextInt(16) + 8;
            new WorldGenDungeons().generate(world, rand, l2, i2, j2);
        }
        BiomeGenHills.decorate(world, rand, k, l);
        // The ore part of OreSpawnWorld.generate (OreSpawnWorldOres.generateMining) now runs inside
        // OreSpawnWorld.generate, called by applyBiomeDecoration after this method (W12).
    }

    /**
     * {@code SpawnerAnimals.performWorldGenSpawning} (:313), as {@code NoiseBasedChunkGenerator} does it.
     *
     * <p>The dino list of {@code getPossibleCreatures} (:349-413) lives in the {@code orespawn:mining} entries of the
     * W12 spawn table (tools/gen_spawns.py); chunk generation only reads CREATURE, as 1.7.10 did, and
     * {@code world.spawn.ChunkGenerationSpawns} skips the placement predicates like {@code performWorldGenSpawning}.
     */
    @Override
    public void spawnOriginalMobs(final WorldGenRegion level) {
        final ChunkPos chunkpos = level.getCenter();
        final Holder<Biome> holder = level.getBiome(chunkpos.getWorldPosition().atY(level.getMaxBuildHeight() - 1));
        final WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(level.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(level, holder, chunkpos, worldgenrandom);
    }

    /** Surface pass runs in {@link #fillFromNoise} (see there). */
    @Override
    public void buildSurface(final WorldGenRegion level, final StructureManager structureManager,
                             final RandomState random, final ChunkAccess chunk) {
    }

    /** {@code MapGenCaves} and {@code MapGenRavine} run in {@link #fillFromNoise} (see there). */
    @Override
    public void applyCarvers(final WorldGenRegion level, final long seed, final RandomState random,
                             final BiomeManager biomeManager, final StructureManager structureManager,
                             final ChunkAccess chunk, final GenerationStep.Carving step) {
    }

    /** {@code Block[65536]} (:165): 256 blocks high. */
    @Override
    public int getGenDepth() {
        return 256;
    }

    /** Water up to and including Y 62 (:90, :128). */
    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    /**
     * Height of the density terrain alone, like the vanilla noise generator (no surface, caves or ores). fix2 (R26):
     * the chunk array is cached, see {@link #getBaseColumn}.
     */
    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types type, final LevelHeightAccessor level,
                             final RandomState random) {
        final BlockState[] blocks = baseTerrain(this.terrain(), x >> 4, z >> 4);
        final int lx = x & 15;
        final int lz = z & 15;
        for (int y = Math.min(255, level.getMaxBuildHeight() - 1); y >= Math.max(0, level.getMinBuildHeight()); --y) {
            final BlockState state = blocks[lx << 12 | lz << 8 | y];
            if (state != null && type.isOpaque().test(state)) {
                return y + 1;
            }
        }
        return level.getMinBuildHeight();
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

    /**
     * {@code func_147416_a} (:415-417): {@code "Stronghold"} answers from {@code strongholdGenerator}, the three
     * 1.7.10 ring positions ({@link MapGenStronghold#findNearestMapStructure}); everything else as vanilla.
     */
    @Override
    @Nullable
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(final ServerLevel level, final HolderSet<Structure> structure,
                                                                   final BlockPos pos, final int searchRadius,
                                                                   final boolean skipKnownStructures) {
        return MapGenStronghold.findNearestMapStructure(level, structure, pos, skipKnownStructures,
                super.findNearestMapStructure(level, structure, pos, searchRadius, skipKnownStructures));
    }

    @Override
    public void addDebugScreenInfo(final List<String> info, final RandomState random, final BlockPos pos) {
    }
}
