package com.swbr.orespawn.world.structure;

import com.swbr.orespawn.world.OreSpawnWorld;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * DECISIONS R24: the {@code orespawn:legacy} structures are written last. No original class - in 1.7.10
 * {@code OreSpawnWorld.generate} ran after the provider's populate and built a structure in one go on the live world.
 *
 * <p><b>Why the generation step alone is not enough.</b> The structure sets stand on {@code top_layer_modification}
 * and the six OreSpawn generators run their legacy populate before {@code super.applyBiomeDecoration}, so inside the
 * decoration of one chunk the structure part of that chunk comes after trees, ores, lakes and village parts. But a
 * 1.21.1 decoration writes into the 3x3 chunks around the decorated one, and the neighbours are decorated in any order:
 * a neighbour decorated <em>later</em> still puts its trees, lakes and populate steps (Utopia populates C-(1,1), Mining
 * writes into C+1) over the structure part that is already there. Which neighbour comes last depends on the order in
 * which the chunk system schedules them, so the holes appeared at random positions - the dimension GameTest failed once
 * with a Kyuubi dungeon block turned into air.
 *
 * <p><b>What.</b> At the very end of the decoration of chunk C this pass writes the blocks and block entity data of
 * every {@code orespawn:legacy} piece once more, clipped to the 3x3 chunks C may write into (the same Y range as
 * {@code ChunkGenerator.getWritableArea}). The last decoration that touches any block of a chunk is one of its 3x3
 * neighbours, and that one ends with this pass, so the recorded write wins no matter the order. Entities are not
 * spawned again: the regular piece placement in the chunk's own decoration spawns them once. Replaying is
 * idempotent - the recording is the same log in every chunk (DECISIONS R23).
 *
 * <p><b>Order.</b> Structures in the order of the calls in {@code OreSpawnWorld.generate} (the constants of
 * {@link OreSpawnWorld}), not in the alphabetical registry order: Islands D4 builds before the Cloud Shark, the Fairy
 * tree before the Crystal dungeon chain, the Utopia trees before the King altar. PORT: 1.7.10 built structures of
 * different chunks in the order the chunks were populated, which is not reproducible; starts of the same structure are
 * written in the order of their start chunk (x, then z). Unknown {@code orespawn:legacy} structures follow in registry
 * order.
 *
 * <p>Called by the six OreSpawn chunk generators after {@code OreSpawnWorld.generate}, and in the vanilla dimensions by
 * the feature {@code orespawn:orespawn_world}, the last feature of {@code top_layer_modification}.
 */
public final class LegacyStructurePass {

    /** The structure ids in the order of {@code OreSpawnWorld.generate} (:28-213). */
    private static final List<String> ORDER = List.of(
            OreSpawnWorld.OVERWORLD_DUNGEON,
            OreSpawnWorld.END_DUNGEON,
            OreSpawnWorld.UTOPIA_HUGE_TREE,
            OreSpawnWorld.UTOPIA_OTHER_TREES,
            OreSpawnWorld.UTOPIA_KING_ALTAR,
            OreSpawnWorld.MINING_DUNGEON,
            OreSpawnWorld.VILLAGE_DUNGEON,
            OreSpawnWorld.ISLANDS_DUNGEON,
            OreSpawnWorld.ISLANDS_CLOUD_SHARK,
            OreSpawnWorld.CRYSTAL_FAIRY_TREE,
            OreSpawnWorld.CRYSTAL_DUNGEON);

    /** Chessboard distance of a structure start the FEATURES step may read ({@code ChunkPyramid}: STRUCTURE_STARTS, 8). */
    private static final int START_RADIUS = 8;

    private LegacyStructurePass() {
    }

    /**
     * Writes every {@code orespawn:legacy} piece that intersects the 3x3 chunks around {@code center} once more, blocks
     * and block entity data only.
     *
     * @param level     the generation region of the decoration of {@code center}
     * @param generator the chunk generator of the level (base-terrain reads of a recording built on a cache miss)
     */
    public static void apply(final WorldGenLevel level, final ChunkGenerator generator, final ChunkPos center) {
        final List<Structure> structures = ordered(level.registryAccess().registryOrThrow(Registries.STRUCTURE));
        if (structures.isEmpty()) {
            return;
        }
        final BoundingBox region = new BoundingBox(
                (center.x - 1) << 4, level.getMinBuildHeight() + 1, (center.z - 1) << 4,
                ((center.x + 1) << 4) + 15, level.getMaxBuildHeight() - 1, ((center.z + 1) << 4) + 15);
        for (final Structure structure : structures) {
            final LongOpenHashSet seen = new LongOpenHashSet();
            final LongArrayList references = new LongArrayList();
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    final ChunkAccess chunk = level.getChunk(center.x + dx, center.z + dz, ChunkStatus.STRUCTURE_REFERENCES);
                    for (final long reference : chunk.getReferencesForStructure(structure)) {
                        if (seen.add(reference)) {
                            references.add(reference);
                        }
                    }
                }
            }
            if (references.isEmpty()) {
                continue;
            }
            sortByStartChunk(references);
            for (final long reference : references) {
                final int startX = ChunkPos.getX(reference);
                final int startZ = ChunkPos.getZ(reference);
                if (Math.max(Math.abs(startX - center.x), Math.abs(startZ - center.z)) > START_RADIUS
                        || !level.hasChunk(startX, startZ)) {
                    continue;
                }
                final StructureStart start = level.getChunk(startX, startZ, ChunkStatus.STRUCTURE_STARTS)
                        .getStartForStructure(structure);
                if (start == null || !start.isValid() || !start.getBoundingBox().intersects(region)) {
                    continue;
                }
                for (final StructurePiece piece : start.getPieces()) {
                    if (piece instanceof LegacyStructurePiece legacy && piece.getBoundingBox().intersects(region)) {
                        legacy.reassert(level, generator, region);
                    }
                }
            }
        }
    }

    /** Sorts packed start-chunk positions into the write order of the pass: x, then z. */
    public static void sortByStartChunk(final LongArrayList references) {
        references.sort((long a, long b) -> {
            final int byX = Integer.compare(ChunkPos.getX(a), ChunkPos.getX(b));
            return byX != 0 ? byX : Integer.compare(ChunkPos.getZ(a), ChunkPos.getZ(b));
        });
    }

    /** The {@code orespawn:legacy} structures of the registry, in {@link #ORDER}, unknown ones after in registry order. */
    public static List<Structure> ordered(final Registry<Structure> registry) {
        final List<Structure> known = new ArrayList<>();
        for (final String id : ORDER) {
            final Structure structure = registry.get(ResourceLocation.parse(id));
            if (structure instanceof LegacyStructure) {
                known.add(structure);
            }
        }
        final List<Structure> result = new ArrayList<>(known);
        for (final Map.Entry<net.minecraft.resources.ResourceKey<Structure>, Structure> entry : registry.entrySet()) {
            if (entry.getValue() instanceof LegacyStructure && !known.contains(entry.getValue())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
