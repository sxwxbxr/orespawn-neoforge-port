package com.swbr.orespawn.world.structure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * The two lookup tables behind the one generic structure type {@code orespawn:legacy} ({@link LegacyStructure}).
 * No original class: 1.7.10 called its builders directly from {@code OreSpawnWorld.generate}; 1.21.1 splits that
 * call into "where" (structure start, before any block exists) and "what" (piece post-processing, chunk by chunk).
 *
 * <ul>
 * <li>A {@link Dispatcher} is the "where": the placement rule of one {@code OreSpawnWorld.addXxx} (or of a whole
 * chained branch of {@code generate}), evaluated in {@code Structure.findGenerationPoint} against the generator's
 * base columns ({@link SurfaceProbe}). It names the builder, the origin the original passed and a bounding box.
 * The structure JSON refers to it by id: {@code "dispatcher": "orespawn:overworld_dungeon"}.</li>
 * <li>A {@link Builder} is the "what": the block placement of the original method ({@code GenericDungeon.makeXxx},
 * {@code BasiliskMaze.buildBasiliskMaze}, {@code Trees.FairyCastleTree}, ...), written against a
 * {@link StructureWriter}. It must be a pure function of its arguments, the random it is given and what the writer
 * reads: the piece runs it once against a recording writer and every chunk its box touches replays its part of the
 * recording (DECISIONS R23, {@link StructureRecording}); after a cache eviction or a restart it runs again and must
 * record the same.</li>
 * </ul>
 *
 * <p>Registration pattern for a later wave (W13 {@code GenericDungeon}): register the builder under the id the
 * dispatcher already names, e.g. {@code LegacyStructureRegistry.registerBuilder("orespawn:make_play_pool",
 * (world, random, x, y, z, variant) -> GenericDungeon.makePlayPool(world, x, y, z))}, from a static
 * {@code bootstrap()} that the mod constructor reaches before the server starts. Registering an id twice replaces
 * the earlier builder, so a wave can take over a placeholder.
 */
public final class LegacyStructureRegistry {

    /** Block placement of one original build method. */
    @FunctionalInterface
    public interface Builder {
        /**
         * @param world   recording writer of the piece (world generation) or a direct writer (run time, tests)
         * @param random  a fresh {@code Random(seed)}; the only random a builder may draw from
         * @param x       origin as the original passed it
         * @param variant builder-specific switch the dispatcher rolled (e.g. King or Queen altar)
         */
        void build(StructureWriter world, Random random, int x, int y, int z, int variant);
    }

    /**
     * Placement rule of one structure; empty when the original would have placed nothing in this chunk. More than one
     * placement where one {@code generate} call of the original built more than one thing (Overworld: one of the six
     * water or pond builds and, independently, the nest-to-duck-pond chain).
     */
    @FunctionalInterface
    public interface Dispatcher {
        List<Placement> locate(LegacyStructureContext context);
    }

    /**
     * Outcome of a dispatcher.
     *
     * @param builder builder id
     * @param box     every block the builder may write; the piece clips to it (DECISIONS R12)
     */
    public record Placement(String builder, int x, int y, int z, int variant, BoundingBox box) {

        /** A placement whose box is the origin plus the inclusive extents of the original loops. */
        public static Placement of(final String builder, final int x, final int y, final int z, final int variant,
                                   final int minDx, final int minDy, final int minDz,
                                   final int maxDx, final int maxDy, final int maxDz) {
            return new Placement(builder, x, y, z, variant,
                    new BoundingBox(x + minDx, y + minDy, z + minDz, x + maxDx, y + maxDy, z + maxDz));
        }
    }

    private static final Map<String, Builder> BUILDERS = new ConcurrentHashMap<>();
    private static final Map<String, Dispatcher> DISPATCHERS = new ConcurrentHashMap<>();

    private LegacyStructureRegistry() {
    }

    public static void registerBuilder(final String id, final Builder builder) {
        BUILDERS.put(id, builder);
    }

    public static void registerDispatcher(final String id, final Dispatcher dispatcher) {
        DISPATCHERS.put(id, dispatcher);
    }

    public static Optional<Builder> builder(final String id) {
        return Optional.ofNullable(BUILDERS.get(id));
    }

    public static Optional<Dispatcher> dispatcher(final String id) {
        return Optional.ofNullable(DISPATCHERS.get(id));
    }
}
