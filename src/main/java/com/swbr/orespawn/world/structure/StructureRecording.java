package com.swbr.orespawn.world.structure;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * DECISIONS R23: the seamless form of a structure build. No original class - 1.7.10 ran a build method once, on a
 * live world, and never split it into chunks.
 *
 * <p><b>Why.</b> A 1.21.1 structure piece is post-processed once for every chunk its box touches, in any order and on
 * any worker thread. W12 ran the whole builder again in every chunk and clipped its writes to that chunk. A builder
 * that reads blocks (a foundation that fills down to the first non-air block) or rolls chests then saw a different
 * world in each run: the part of the structure the neighbouring chunk had already written, or not; a chest outside
 * the clip that was never filled and so never drew its numbers. Every later random number shifted, and the structure
 * showed seams at chunk borders.
 *
 * <p><b>What.</b> A build now runs <em>once</em> per structure piece, completely and unclipped, against a recording
 * {@link StructureWriter}. The recording is the write log of that one run:
 * <ul>
 * <li>every block write, in order, with its flags;</li>
 * <li>the final state of every block entity the builder touched (chest contents, spawner entity), as NBT;</li>
 * <li>every entity the builder spawned, as NBT, taken after the build ended - state the builder set after
 * {@code spawnEntity} is kept;</li>
 * </ul>
 * and each chunk only <em>replays</em> the part of the log inside its box ({@link #replay}). Reads and random draws
 * happen in the one run, so no chunk can change them.
 *
 * <p><b>Reads during the run</b> (PORT, R18 case 3): the builder's own earlier writes first; otherwise the chunk
 * generator's base column ({@link ChunkGenerator#getBaseColumn}: terrain and fluids, no surface blocks, no carvers, no
 * features, no other structures). That is the only world state that is the same no matter which chunks exist when the
 * first chunk of the structure is generated, and it is what a rebuild after a server restart sees again - the
 * recording is a cache, not saved data. 1.7.10 read the finished, decorated world at that moment. For the reads the
 * original builders make ({@code == Blocks.air}, {@code tallgrass}, {@code water} under a foundation) the difference is
 * grass and dirt answering as stone (both non-air), tall grass answering as air (treated alike by the original) and
 * carver caves answering solid.
 *
 * <p><b>Cache.</b> Recordings are kept per {@link Key} (world seed, dimension, builder, origin, variant, seed, box) in a
 * small least-recently-used cache. A miss, including one after a restart, simply builds again; since the run depends
 * only on the key and the base terrain, it records the same log.
 */
public final class StructureRecording {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /** Largest number of recordings kept. The biggest builds (Enormous Castle, square Magic Apple) log a few 100k writes. */
    private static final int MAX_CACHED = 24;

    private static final Map<Key, Slot> CACHE = new ConcurrentHashMap<>();
    private static final AtomicLong CLOCK = new AtomicLong();
    private static final AtomicLong BUILDS = new AtomicLong();

    /**
     * Identity of one build. Two pieces with equal keys record the same log.
     *
     * @param worldSeed the level's seed; base terrain depends on it
     * @param dimension the level's dimension id; so does the generator
     */
    public record Key(long worldSeed, String dimension, String builder, int x, int y, int z, int variant, long seed,
                      int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        public static Key of(final WorldGenLevel level, final String builder, final int x, final int y, final int z,
                             final int variant, final long seed, final BoundingBox box) {
            return new Key(level.getSeed(), level.getLevel().dimension().location().toString(), builder, x, y, z,
                    variant, seed, box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
        }

        public BoundingBox box() {
            return new BoundingBox(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
        }
    }

    /** One recorded block entity: its type, for the replay check, and its NBT without metadata. */
    private record BlockEntityRecord(long pos, BlockEntityType<?> type, CompoundTag tag) {
    }

    /** One recorded entity: the block position of its feet (the clip test of {@code spawnEntity}) and its NBT. */
    private record EntityRecord(int x, int y, int z, CompoundTag tag) {
    }

    // ------------------------------------------------------------------------------------------------
    // frozen log
    // ------------------------------------------------------------------------------------------------

    private final BoundingBox pieceBox;
    private final long[] positions;
    private final BlockState[] states;
    private final int[] flags;
    /** Chunk key -> indices into the write arrays, in write order. */
    private final Long2ObjectOpenHashMap<int[]> writesByChunk;
    private final Long2ObjectOpenHashMap<List<BlockEntityRecord>> blockEntitiesByChunk;
    private final Long2ObjectOpenHashMap<List<EntityRecord>> entitiesByChunk;
    private final int blockEntityCount;
    private final int entityCount;

    private StructureRecording(final Recorder recorder, final HolderLookup.Provider registries) {
        this.pieceBox = recorder.pieceBox;
        final int n = recorder.size;
        this.positions = java.util.Arrays.copyOf(recorder.positions, n);
        this.states = java.util.Arrays.copyOf(recorder.states, n);
        this.flags = java.util.Arrays.copyOf(recorder.flags, n);

        final Long2ObjectOpenHashMap<IntArrayList> byChunk = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < n; ++i) {
            final long pos = this.positions[i];
            final long chunk = chunkKey(BlockPos.getX(pos), BlockPos.getZ(pos));
            IntArrayList list = byChunk.get(chunk);
            if (list == null) {
                list = new IntArrayList();
                byChunk.put(chunk, list);
            }
            list.add(i);
        }
        this.writesByChunk = new Long2ObjectOpenHashMap<>(byChunk.size());
        for (final Long2ObjectMap.Entry<IntArrayList> e : byChunk.long2ObjectEntrySet()) {
            this.writesByChunk.put(e.getLongKey(), e.getValue().toIntArray());
        }

        this.blockEntitiesByChunk = new Long2ObjectOpenHashMap<>();
        int bes = 0;
        for (final Long2ObjectMap.Entry<BlockEntity> e : recorder.blockEntities.long2ObjectEntrySet()) {
            final long pos = e.getLongKey();
            final BlockEntity be = e.getValue();
            final CompoundTag tag = be.saveWithoutMetadata(registries);
            listFor(this.blockEntitiesByChunk, chunkKey(BlockPos.getX(pos), BlockPos.getZ(pos)))
                    .add(new BlockEntityRecord(pos, be.getType(), tag));
            bes++;
        }
        this.blockEntityCount = bes;

        this.entitiesByChunk = new Long2ObjectOpenHashMap<>();
        int ents = 0;
        for (final Entity entity : recorder.entities) {
            final CompoundTag tag = new CompoundTag();
            // Entity.save: false for a passenger (its vehicle's tag carries it) or a removed entity.
            if (!entity.save(tag)) {
                continue;
            }
            final int ex = Mth.floor(entity.getX());
            final int ey = Mth.floor(entity.getY());
            final int ez = Mth.floor(entity.getZ());
            listFor(this.entitiesByChunk, chunkKey(ex, ez)).add(new EntityRecord(ex, ey, ez, tag));
            ents++;
        }
        this.entityCount = ents;
    }

    private static <T> List<T> listFor(final Long2ObjectOpenHashMap<List<T>> map, final long key) {
        List<T> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        return list;
    }

    private static long chunkKey(final int blockX, final int blockZ) {
        return net.minecraft.world.level.ChunkPos.asLong(blockX >> 4, blockZ >> 4);
    }

    // ------------------------------------------------------------------------------------------------
    // entry points
    // ------------------------------------------------------------------------------------------------

    /**
     * {@code StructurePiece.postProcess} of a recorded build: obtains the recording for {@code key} (building it on a
     * miss) and replays the part inside {@code chunkBox} and {@code key.box()}.
     *
     * @param build the builder call; it receives the recording writer, must draw only from randoms it creates from
     *              the key, and runs at most once per cache entry
     */
    public static void postProcess(final WorldGenLevel level, final ChunkGenerator generator, final BoundingBox chunkBox,
                                   final Key key, final Consumer<StructureWriter> build) {
        final StructureRecording recording = obtain(level, generator, key, build);
        recording.replay(level, chunkBox);
    }

    /** The cached recording of {@code key}, built on a miss. */
    public static StructureRecording obtain(final WorldGenLevel level, final ChunkGenerator generator, final Key key,
                                            final Consumer<StructureWriter> build) {
        final Slot slot = CACHE.computeIfAbsent(key, k -> new Slot());
        slot.lastUse = CLOCK.incrementAndGet();
        final StructureRecording recording = slot.get(() -> record(level, generator, key.box(), build));
        trim();
        return recording;
    }

    /** One uncached run: builds the log of {@code build} with base-terrain reads of {@code generator}. */
    public static StructureRecording record(final WorldGenLevel level, final ChunkGenerator generator,
                                            final BoundingBox pieceBox, final Consumer<StructureWriter> build) {
        final RandomState randomState = level.getLevel().getChunkSource().randomState();
        final Recorder recorder = new Recorder(level, generator, randomState, pieceBox);
        build.accept(new StructureWriter(level, pieceBox, recorder));
        BUILDS.incrementAndGet();
        return new StructureRecording(recorder, level.registryAccess());
    }

    /** Drops every cached recording; the next {@link #postProcess} of any structure builds again. */
    public static void clearCache() {
        CACHE.clear();
    }

    /** How many recordings have been built since the server JVM started (tests: "built once per start"). */
    public static long builds() {
        return BUILDS.get();
    }

    private static void trim() {
        while (CACHE.size() > MAX_CACHED) {
            Key oldest = null;
            long oldestUse = Long.MAX_VALUE;
            for (final Map.Entry<Key, Slot> e : CACHE.entrySet()) {
                final Slot slot = e.getValue();
                if (slot.value != null && slot.lastUse < oldestUse) {
                    oldestUse = slot.lastUse;
                    oldest = e.getKey();
                }
            }
            if (oldest == null) {
                return; // all remaining slots are still building
            }
            CACHE.remove(oldest);
        }
    }

    /** A cache cell that builds its value once; other threads asking for the same key wait for that build. */
    private static final class Slot {
        volatile long lastUse;
        @Nullable
        volatile StructureRecording value;

        StructureRecording get(final java.util.function.Supplier<StructureRecording> build) {
            StructureRecording v = this.value;
            if (v != null) {
                return v;
            }
            synchronized (this) {
                v = this.value;
                if (v == null) {
                    v = build.get();
                    this.value = v;
                }
                return v;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------
    // replay
    // ------------------------------------------------------------------------------------------------

    /**
     * Writes the part of the log inside {@code clip} and the piece box: blocks in the recorded order through the
     * direct writer ({@link StructureWriter#setBlock}, generation ticks included), then the block entity data, then the
     * entities. Safe to call concurrently for different chunks.
     */
    public void replay(final WorldGenLevel level, final BoundingBox clip) {
        this.replay(level, clip, true);
    }

    /**
     * {@link #replay} without the entities: blocks and block entity data only. For writing the same part of the log a
     * second time ({@link LegacyStructurePass}, DECISIONS R24) - blocks and block entity data are idempotent, a spawned
     * entity is not.
     */
    public void replayBlocks(final WorldGenLevel level, final BoundingBox clip) {
        this.replay(level, clip, false, false);
    }

    /**
     * Tests only: {@link #replay} on a live level without neighbour shape updates, the way a generation region writes
     * ({@link StructureWriter#withoutShapeUpdates}).
     */
    public void replayWithoutShapeUpdates(final WorldGenLevel level, final BoundingBox clip) {
        this.replay(level, clip, true, true);
    }

    private void replay(final WorldGenLevel level, final BoundingBox clip, final boolean entities) {
        this.replay(level, clip, entities, false);
    }

    private void replay(final WorldGenLevel level, final BoundingBox clip, final boolean entities,
                        final boolean withoutShapeUpdates) {
        if (!clip.intersects(this.pieceBox)) {
            return;
        }
        final BoundingBox box = new BoundingBox(
                Math.max(clip.minX(), this.pieceBox.minX()), Math.max(clip.minY(), this.pieceBox.minY()),
                Math.max(clip.minZ(), this.pieceBox.minZ()), Math.min(clip.maxX(), this.pieceBox.maxX()),
                Math.min(clip.maxY(), this.pieceBox.maxY()), Math.min(clip.maxZ(), this.pieceBox.maxZ()));
        final StructureWriter direct = withoutShapeUpdates ? StructureWriter.withoutShapeUpdates(level, box)
                : new StructureWriter(level, box);
        final int cx0 = box.minX() >> 4;
        final int cx1 = box.maxX() >> 4;
        final int cz0 = box.minZ() >> 4;
        final int cz1 = box.maxZ() >> 4;

        for (int cx = cx0; cx <= cx1; ++cx) {
            for (int cz = cz0; cz <= cz1; ++cz) {
                final int[] indices = this.writesByChunk.get(net.minecraft.world.level.ChunkPos.asLong(cx, cz));
                if (indices == null) {
                    continue;
                }
                for (final int i : indices) {
                    final long pos = this.positions[i];
                    final int x = BlockPos.getX(pos);
                    final int y = BlockPos.getY(pos);
                    final int z = BlockPos.getZ(pos);
                    if (box.isInside(x, y, z)) {
                        direct.setBlock(x, y, z, this.states[i], this.flags[i]);
                    }
                }
            }
        }

        final HolderLookup.Provider registries = level.registryAccess();
        for (int cx = cx0; cx <= cx1; ++cx) {
            for (int cz = cz0; cz <= cz1; ++cz) {
                final List<BlockEntityRecord> records =
                        this.blockEntitiesByChunk.get(net.minecraft.world.level.ChunkPos.asLong(cx, cz));
                if (records == null) {
                    continue;
                }
                for (final BlockEntityRecord record : records) {
                    final BlockPos pos = BlockPos.of(record.pos());
                    if (!box.isInside(pos) || !direct.canRead(pos.getX(), pos.getZ())) {
                        continue;
                    }
                    final BlockEntity real = level.getBlockEntity(pos);
                    if (real != null && real.getType() == record.type()) {
                        real.loadWithComponents(record.tag().copy(), registries);
                        real.setChanged();
                    }
                }
            }
        }

        if (!entities) {
            return;
        }
        final ServerLevel server = level.getLevel();
        for (int cx = cx0; cx <= cx1; ++cx) {
            for (int cz = cz0; cz <= cz1; ++cz) {
                final List<EntityRecord> records =
                        this.entitiesByChunk.get(net.minecraft.world.level.ChunkPos.asLong(cx, cz));
                if (records == null) {
                    continue;
                }
                for (final EntityRecord record : records) {
                    if (!box.isInside(record.x(), record.y(), record.z()) || !direct.canRead(record.x(), record.z())) {
                        continue;
                    }
                    final Entity entity = EntityType.loadEntityRecursive(record.tag().copy(), server, Function.identity());
                    if (entity == null) {
                        continue;
                    }
                    // A log may be replayed into more than one place (tests) and is rebuilt with fresh UUIDs after
                    // an eviction anyway; a UUID per replay keeps addFreshEntity from refusing a duplicate.
                    entity.getSelfAndPassengers().forEach(e -> e.setUUID(Mth.createInsecureUUID(level.getRandom())));
                    level.addFreshEntityWithPassengers(entity);
                    if (level instanceof Level && entity instanceof Mob mob) {
                        mob.playAmbientSound();
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------
    // introspection (tests, logging)
    // ------------------------------------------------------------------------------------------------

    /** Number of logged block writes, overwrites included. */
    public int writes() {
        return this.positions.length;
    }

    /** Number of logged block writes outside {@code box}; a correct placement box gives 0 (DECISIONS R12). */
    public int writesOutside(final BoundingBox box) {
        int count = 0;
        for (final long pos : this.positions) {
            if (!box.isInside(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos))) {
                count++;
            }
        }
        return count;
    }

    /**
     * The last logged state of every position inside {@code clip}, keyed by {@link BlockPos#asLong} - what the log leaves
     * behind, without the neighbour updates a replay on a live level would add (tests).
     */
    public Long2ObjectOpenHashMap<BlockState> finalStates(final BoundingBox clip) {
        final Long2ObjectOpenHashMap<BlockState> result = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < this.positions.length; ++i) {
            final long pos = this.positions[i];
            if (clip.isInside(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos))) {
                result.put(pos, this.states[i]);
            }
        }
        return result;
    }

    public int blockEntities() {
        return this.blockEntityCount;
    }

    public int entities() {
        return this.entityCount;
    }

    public BoundingBox pieceBox() {
        return this.pieceBox;
    }

    // ------------------------------------------------------------------------------------------------
    // recorder
    // ------------------------------------------------------------------------------------------------

    /**
     * The mutable side of a recording, behind a recording {@link StructureWriter}. Confined to the thread that runs
     * the build; dropped when the recording is frozen.
     */
    static final class Recorder {

        private final WorldGenLevel level;
        private final ChunkGenerator generator;
        private final RandomState randomState;
        final BoundingBox pieceBox;

        private long[] positions = new long[4096];
        private BlockState[] states = new BlockState[4096];
        private int[] flags = new int[4096];
        private int size;

        /** Current state of every written position; the first answer of a read. */
        private final Long2ObjectOpenHashMap<BlockState> current = new Long2ObjectOpenHashMap<>();
        /** Detached block entities, created on the first {@code getTileEntity} of a written block. */
        private final Long2ObjectLinkedOpenHashMap<BlockEntity> blockEntities = new Long2ObjectLinkedOpenHashMap<>();
        private final List<Entity> entities = new ArrayList<>();
        private final Long2ObjectOpenHashMap<NoiseColumn> columns = new Long2ObjectOpenHashMap<>();

        Recorder(final WorldGenLevel level, final ChunkGenerator generator, final RandomState randomState,
                 final BoundingBox pieceBox) {
            this.level = level;
            this.generator = generator;
            this.randomState = randomState;
            this.pieceBox = pieceBox;
        }

        private NoiseColumn column(final int x, final int z) {
            final long key = net.minecraft.world.level.ChunkPos.asLong(x, z);
            NoiseColumn column = this.columns.get(key);
            if (column == null) {
                column = this.generator.getBaseColumn(x, z, this.level, this.randomState);
                this.columns.put(key, column);
            }
            return column;
        }

        /** Own earlier write, else base terrain; air outside the build height. */
        BlockState getBlock(final int x, final int y, final int z) {
            if (this.level.isOutsideBuildHeight(y)) {
                return AIR;
            }
            final BlockState mine = this.current.get(BlockPos.asLong(x, y, z));
            if (mine != null) {
                return mine;
            }
            return this.column(x, z).getBlock(y);
        }

        /**
         * {@code OreSpawnMain.setBlockFast} into the log: the rejections of {@code FastBlocks.setBlockFast} (world
         * border, build height), no clip. Reports {@code true} for an accepted write, as a generation region does.
         */
        boolean setBlock(final int x, final int y, final int z, final BlockState state, final int flag) {
            if (x < -Level.MAX_LEVEL_SIZE || z < -Level.MAX_LEVEL_SIZE
                    || x >= Level.MAX_LEVEL_SIZE || z >= Level.MAX_LEVEL_SIZE) {
                return false;
            }
            if (this.level.isOutsideBuildHeight(y)) {
                return false;
            }
            final long pos = BlockPos.asLong(x, y, z);
            if (this.size == this.positions.length) {
                final int grown = this.size * 2;
                this.positions = java.util.Arrays.copyOf(this.positions, grown);
                this.states = java.util.Arrays.copyOf(this.states, grown);
                this.flags = java.util.Arrays.copyOf(this.flags, grown);
            }
            this.positions[this.size] = pos;
            this.states[this.size] = state;
            this.flags[this.size] = flag;
            this.size++;
            final BlockState old = this.current.put(pos, state);
            // LevelChunk.setBlockState / BlockBehaviour.onRemove: a block entity survives only a rewrite of the same
            // block that still has one.
            if (this.blockEntities.containsKey(pos)
                    && (!state.hasBlockEntity() || old == null || !state.is(old.getBlock()))) {
                this.blockEntities.remove(pos);
            }
            return true;
        }

        /** {@code World.getTileEntity} on the log: a detached block entity of a written block, {@code null} otherwise. */
        @Nullable
        BlockEntity getTileEntity(final int x, final int y, final int z) {
            if (this.level.isOutsideBuildHeight(y)) {
                return null;
            }
            final long pos = BlockPos.asLong(x, y, z);
            BlockEntity be = this.blockEntities.get(pos);
            if (be != null) {
                return be;
            }
            final BlockState state = this.current.get(pos);
            if (state == null || !state.hasBlockEntity() || !(state.getBlock() instanceof EntityBlock block)) {
                return null;
            }
            be = block.newBlockEntity(new BlockPos(x, y, z), state);
            if (be != null) {
                this.blockEntities.put(pos, be);
            }
            return be;
        }

        /** The biome at the base-terrain surface of the column (the same answer {@link SurfaceProbe#getBiome} gives). */
        Holder<Biome> getBiome(final int x, final int z) {
            int y = this.level.getMinBuildHeight();
            for (int yy = this.level.getMaxBuildHeight() - 1; yy >= this.level.getMinBuildHeight(); --yy) {
                if (!this.column(x, z).getBlock(yy).isAir()) {
                    y = yy + 1;
                    break;
                }
            }
            return this.generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                    QuartPos.fromBlock(z), this.randomState.sampler());
        }

        /**
         * {@code createEntityByName} + {@code setLocationAndAngles}; the entity is created on the server level, never
         * added, and saved to the log when the build ends.
         */
        @Nullable
        Entity spawnEntity(final String entityId, final double x, final double y, final double z, final float yaw,
                           final float pitch, final Consumer<Entity> beforeAdd) {
            final Optional<EntityType<?>> type = StructureWriter.entityType(entityId);
            if (type.isEmpty()) {
                return null;
            }
            final Entity entity = type.get().create(this.level.getLevel());
            if (entity == null) {
                return null;
            }
            entity.moveTo(x, y, z, yaw, pitch);
            beforeAdd.accept(entity);
            this.entities.add(entity);
            return entity;
        }
    }
}
