package com.swbr.orespawn.world.gen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.registry.ModStructurePlacements;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

/**
 * The stronghold positions of 1.7.10 {@code MapGenStronghold} (client-1.7.10.jar {@code aug}), as a structure
 * placement of type {@code orespawn:legacy_stronghold}. {@code ChunkProviderOreSpawn2} and
 * {@code ChunkProviderOreSpawn3} create it with the default constructor (ChunkProviderOreSpawn2.java:54,
 * ChunkProviderOreSpawn3.java:48): three positions, distance 32, spread 3 ({@code aug.<init>}: {@code g = new
 * agu[3]}, {@code h = 32.0}, {@code i = 3}). Used by {@code data/orespawn/worldgen/structure_set/strongholds.json}.
 *
 * <p>Why not the vanilla {@code minecraft:concentric_rings}: 1.21.1 rewrote the ring algorithm. With
 * {@code (32, 3, 3)} it puts the first ring at {@code 4 * 32 + (r - 0.5) * 80} = 88-168 chunks, where 1.7.10 put
 * it at {@code (1.25 + r) * 32} = 40-72 chunks, and it draws the random numbers in a different order. The
 * vanilla {@code strongholds} set (128 positions, StructureSets.java:170-175) is not used either, see
 * {@code data/orespawn/worldgen/structure/stronghold.json}.
 *
 * <p>The option names {@code distance}, {@code count} and {@code spread} are the keys of the 1.7.10
 * {@code MapGenStronghold(Map)} constructor, with its lower bounds (1.0, 1, 1).
 */
public class MapGenStronghold extends StructurePlacement {

    public static final MapCodec<MapGenStronghold> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.doubleRange(1.0, Double.MAX_VALUE).optionalFieldOf("distance", 32.0).forGetter(p -> p.distance),
                    Codec.intRange(1, 4095).optionalFieldOf("count", 3).forGetter(p -> p.count),
                    Codec.intRange(1, 1023).optionalFieldOf("spread", 3).forGetter(p -> p.spread))
            .apply(instance, MapGenStronghold::new));

    /** {@code field_82671_h}. */
    private final double distance;
    /** {@code structureCoords.length}. */
    private final int count;
    /** {@code field_82672_i}, the start value. */
    private final int spread;

    /** The positions of the last seed asked for; one world seed per server, so one entry is enough. */
    private volatile Coords coords;

    private record Coords(long seed, List<ChunkPos> chunks) {
    }

    public MapGenStronghold(final double distance, final int count, final int spread) {
        // No locate offset, frequency, salt or exclusion zone: MapGenStronghold had none of them.
        super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty());
        this.distance = distance;
        this.count = count;
        this.spread = spread;
    }

    @Override
    public StructurePlacementType<?> type() {
        return ModStructurePlacements.LEGACY_STRONGHOLD.get();
    }

    /** {@code canSpawnStructureAtCoords} (aug.a(II)Z @247-298): the chunk is one of the ring positions. */
    @Override
    protected boolean isPlacementChunk(final ChunkGeneratorStructureState structureState, final int x, final int z) {
        for (final ChunkPos c : structureCoords(structureState.getLevelSeed())) {
            if (c.x == x && c.z == z) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@code getCoordList} (aug.o_): {@code ChunkCoordIntPair.func_151349_a(64)}, the chunk centre at Y 64 -
     * where an Eye of Ender pointed while no stronghold start was known.
     */
    @Override
    public BlockPos getLocatePos(final ChunkPos chunkPos) {
        return new BlockPos((chunkPos.x << 4) + 8, 64, (chunkPos.z << 4) + 8);
    }

    /** The ring positions for this world seed, computed once. */
    public List<ChunkPos> structureCoords(final long seed) {
        Coords c = this.coords;
        if (c == null || c.seed() != seed) {
            c = new Coords(seed, computeCoords(seed, this.distance, this.count, this.spread));
            this.coords = c;
        }
        return c.chunks();
    }

    /**
     * {@code canSpawnStructureAtCoords} (aug.a(II)Z @0-242), line for line. The random source is
     * {@code new Random()} seeded with {@code worldObj.getSeed()}, which 1.21.1 hands the placement as the level
     * seed.
     *
     * <p>{@code getWorldChunkManager().findBiomePosition(x, z, 112, allowed, random)} is
     * {@code WorldChunkManagerHell.findBiomePosition} (aie.a(IIILjava/util/List;Ljava/util/Random;)): both
     * dimensions that use this placement have a single biome, and that biome is allowed - {@code aug.<init>}
     * admits every biome with {@code rootHeight > 0}, and {@code extremeHills} has 1.0, {@code BiomeGenUtopianPlains}
     * the {@code BiomeGenBase} default 0.1 ({@code ahu.<init>}, {@code am = height_Default.rootHeight}). So the
     * lookup always succeeds and always draws two {@code nextInt(225)}, x first.
     *
     * <p>The ring-growth branch {@code l == field_82672_i} (@199-233) compares the position index with the
     * spread; with the default count 3 and spread 3 it never runs, but it is kept for other option values.
     */
    static List<ChunkPos> computeCoords(final long seed, final double distance, final int count, final int initialSpread) {
        final Random random = new Random();
        random.setSeed(seed);
        double d0 = random.nextDouble() * Math.PI * 2.0D;
        int k = 1;
        int spread = initialSpread;
        final List<ChunkPos> out = new ArrayList<>(count);
        for (int l = 0; l < count; ++l) {
            final double d1 = (1.25D * (double) k + random.nextDouble()) * distance * (double) k;
            int i1 = (int) Math.round(Math.cos(d0) * d1);
            int j1 = (int) Math.round(Math.sin(d0) * d1);
            final int radius = 112;
            final int px = (i1 << 4) + 8 - radius + random.nextInt(radius * 2 + 1);
            final int pz = (j1 << 4) + 8 - radius + random.nextInt(radius * 2 + 1);
            i1 = px >> 4;
            j1 = pz >> 4;
            out.add(new ChunkPos(i1, j1));
            d0 += (Math.PI * 2D) * (double) k / (double) spread;
            if (l == spread) {
                k += 2 + random.nextInt(5);
                spread += 1 + random.nextInt(2);
            }
        }
        return List.copyOf(out);
    }

    /**
     * {@code IChunkProvider.func_147416_a("Stronghold", ...)} of ChunkProviderOreSpawn2.java:416 and
     * ChunkProviderOreSpawn3.java:351, for the generators' {@code findNearestMapStructure}: vanilla's search
     * only knows {@code concentric_rings} and {@code random_spread} (ChunkGenerator.java:157-176) and skips this
     * placement, so its result {@code vanilla} is merged with ours here - the nearer one wins.
     */
    @Nullable
    public static Pair<BlockPos, Holder<Structure>> findNearestMapStructure(final ServerLevel level,
                                                                          final HolderSet<Structure> structures,
                                                                          final BlockPos pos,
                                                                          final boolean skipKnownStructures,
                                                                          @Nullable final Pair<BlockPos, Holder<Structure>> vanilla) {
        final ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        Pair<BlockPos, Holder<Structure>> best = vanilla;
        double bestDistance = vanilla == null ? Double.MAX_VALUE : pos.distSqr(vanilla.getFirst());
        for (final Holder<Structure> holder : structures) {
            for (final StructurePlacement placement : state.getPlacementsForStructure(holder)) {
                if (placement instanceof MapGenStronghold stronghold) {
                    final BlockPos found = stronghold.getNearestInstance(level, state, holder.value(), pos, skipKnownStructures);
                    if (found != null && pos.distSqr(found) < bestDistance) {
                        bestDistance = pos.distSqr(found);
                        best = Pair.of(found, holder);
                    }
                }
            }
        }
        return best;
    }

    /**
     * {@code MapGenStructure.func_151545_a} (ave.a(Lahb;III)Lagt;): the centre of the first piece of the nearest
     * stronghold start that exists; only if there is none, the nearest {@link #getLocatePos} of the ring
     * positions. Distances are three-dimensional, the first of equal distances wins.
     *
     * <p>PORT: "exists" was the generator's {@code structureMap} (starts saved in {@code Stronghold.dat}); in
     * 1.21.1 it is the chunk's saved structure start, asked through {@code StructureCheck} so that no chunk is
     * generated for the search. A ring chunk that has a start is then loaded to {@code STRUCTURE_STARTS} to read
     * the piece, as vanilla does (ChunkGenerator.getStructureGeneratingAt). {@code skipKnownStructures} (explorer
     * maps; Eye of Ender and {@code /locate} pass false) only filters what {@code StructureCheck} filters - 1.7.10
     * had no such notion. The 1.7.10 lookup also created a start when the searcher stood inside a ring chunk
     * ({@code func_151538_a} on the searcher's chunk); in 1.21.1 that chunk has its start already.
     */
    @Nullable
    private BlockPos getNearestInstance(final ServerLevel level, final ChunkGeneratorStructureState state,
                                        final Structure structure, final BlockPos pos, final boolean skipKnownStructures) {
        final List<ChunkPos> coords = structureCoords(state.getLevelSeed());
        final StructureManager structureManager = level.structureManager();
        BlockPos nearest = null;
        double d0 = Double.MAX_VALUE;
        for (final ChunkPos chunkPos : coords) {
            if (structureManager.checkStructurePresence(chunkPos, structure, this, skipKnownStructures)
                    != StructureCheckResult.START_PRESENT) {
                continue;
            }
            final ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
            final StructureStart start = structureManager.getStartForStructure(SectionPos.bottomOf(chunk), structure, chunk);
            if (start == null || !start.isValid()) {
                continue;
            }
            final BlockPos centre = start.getPieces().get(0).getBoundingBox().getCenter();
            final double d1 = centre.distSqr(pos);
            if (d1 < d0) {
                d0 = d1;
                nearest = centre;
            }
        }
        if (nearest != null) {
            return nearest;
        }
        for (final ChunkPos chunkPos : coords) {
            final BlockPos candidate = getLocatePos(chunkPos);
            final double d1 = candidate.distSqr(pos);
            if (d1 < d0) {
                d0 = d1;
                nearest = candidate;
            }
        }
        return nearest;
    }
}
