package com.swbr.orespawn.world.structure;

import com.swbr.orespawn.world.util.FastBlocks;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;

/**
 * The block writer every ported builder goes through: {@code OreSpawnWorld}, the tree and maze classes and, from
 * W13 on, {@code GenericDungeon}. It stands in for the 1.7.10 {@code World} those classes received, with the
 * handful of calls they made on it: {@code getBlock}, {@code isAirBlock}, {@code OreSpawnMain.setBlockFast},
 * {@code world.setBlock} for spawners and chests, {@code getTileEntity}, {@code spawnEntityInWorld} and
 * {@code getBiomeGenForCoords}.
 *
 * <p><b>Clip.</b> 1.21.1 lets world generation touch only a small window around the chunk being generated
 * (DECISIONS R12). A writer carries an optional {@link BoundingBox}; writes outside it are dropped silently and
 * report {@code false}, the {@code StructurePiece.placeBlock} rule. The ways to get one:
 * <ul>
 * <li>{@link #forPiece}: a direct writer clipped to the box a chunk hands to {@code StructurePiece.postProcess}.
 * Until W12 {@link LegacyStructurePiece} ran its builder through it once per chunk; since R23 it records instead
 * (below).</li>
 * <li>{@link #forFeature}: inside a feature or a chunk generator's decoration step, clipped to the 3x3 chunks
 * around the decorated chunk ({@code ChunkStep.blockStateWriteRadius} 1 for {@code FEATURES}).</li>
 * <li>{@link #unclipped}: at run time on a live {@link ServerLevel} (DungeonSpawnerBlock, ItemMagicApple,
 * ItemRandomDungeon), where the original wrote wherever it wanted and loaded chunks on the way.</li>
 * <li>A <b>recording</b> writer (DECISIONS R23, {@link StructureRecording}): what an {@code orespawn:legacy} piece
 * hands its builder. The builder runs once per structure, unclipped; writes, block entity data and entities go into a
 * log, reads answer the builder's own writes first and the generator's base terrain otherwise, and every chunk replays
 * only its part of the log. {@link #clip()} is the piece box there; {@link #contains} tests it, but writes outside it
 * are still logged (and never replayed), so reads and chest rolls stay those of one complete run.</li>
 * </ul>
 *
 * <p>Builders use the same calls in every mode. {@link #level()} is the escape hatch: on a recording writer it is the
 * generation region of whichever chunk happened to trigger the build, and anything read or written through it
 * bypasses the log - use it for registry and height queries only.
 *
 * <p><b>Reads</b> of a direct writer outside the clip are answered from the level while the chunk is reachable;
 * inside a {@link WorldGenRegion} a chunk beyond its dependency radius answers air instead of throwing
 * ({@code WorldGenRegion.getChunk} raises "Requested chunk unavailable during world generation"). A direct writer run
 * again per chunk therefore reads a different world in each run; that is what the recording writer removes.
 *
 * <p><b>World generation side effects.</b> A proto chunk calls no {@code onPlace}; to keep what the 1.7.10 write
 * did on a live world, a write into a {@link WorldGenRegion} schedules the tick a falling block (delay 2), a
 * leaves block (delay 1, see {@code LegacyWorld}) or a fluid (its own delay) would have scheduled on placement.
 */
public final class StructureWriter {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final LevelAccessor level;
    @Nullable
    private final BoundingBox clip;
    /** Set on a recording writer (DECISIONS R23); {@code null} on a direct one. */
    @Nullable
    private final StructureRecording.Recorder recorder;
    /** Flags added to every direct write ({@link #withoutShapeUpdates}); 0 otherwise. */
    private final int extraFlags;

    public StructureWriter(final LevelAccessor level, @Nullable final BoundingBox clip) {
        this(level, clip, null);
    }

    StructureWriter(final LevelAccessor level, @Nullable final BoundingBox clip,
                    @Nullable final StructureRecording.Recorder recorder) {
        this(level, clip, recorder, 0);
    }

    private StructureWriter(final LevelAccessor level, @Nullable final BoundingBox clip,
                            @Nullable final StructureRecording.Recorder recorder, final int extraFlags) {
        this.level = level;
        this.clip = clip;
        this.recorder = recorder;
        this.extraFlags = extraFlags;
    }

    /**
     * Tests only: a direct writer on a live level that writes the way a generation region does, without neighbour
     * shape updates ({@code Block.UPDATE_KNOWN_SHAPE} added to every write). On a live level those updates take off
     * whatever a build leaves unsupported - a crystal torch the Fairy Castle tree sets on its own leaves - and whether
     * that happens depends on the order of the later writes nearby, which differs between one unclipped run and a
     * replay chunk by chunk. {@code null} clip = unclipped.
     */
    public static StructureWriter withoutShapeUpdates(final LevelAccessor level, @Nullable final BoundingBox clip) {
        return new StructureWriter(level, clip, null, Block.UPDATE_KNOWN_SHAPE);
    }

    /**
     * Inside {@code StructurePiece.postProcess}: {@code box} is the chunk box handed to the piece. A direct writer: a
     * builder run through it once per chunk sees a different world in every run. Pieces of this port go through
     * {@link StructureRecording#postProcess} instead (DECISIONS R23); this stays for a piece whose placement provably
     * reads nothing and rolls nothing.
     */
    public static StructureWriter forPiece(final LevelAccessor level, final BoundingBox box) {
        return new StructureWriter(level, box);
    }

    /**
     * Inside a feature or {@code ChunkGenerator.applyBiomeDecoration}: the 3x3 chunks around chunk
     * {@code (chunkX, chunkZ)}, full build height.
     */
    public static StructureWriter forFeature(final LevelAccessor level, final int chunkX, final int chunkZ) {
        final BoundingBox box = new BoundingBox(
                (chunkX - 1) << 4, level.getMinBuildHeight(), (chunkZ - 1) << 4,
                ((chunkX + 2) << 4) - 1, level.getMaxBuildHeight() - 1, ((chunkZ + 2) << 4) - 1);
        return new StructureWriter(level, box);
    }

    /** A live level at run time, no clip. */
    public static StructureWriter unclipped(final LevelAccessor level) {
        return new StructureWriter(level, null);
    }

    public LevelAccessor level() {
        return this.level;
    }

    @Nullable
    public BoundingBox clip() {
        return this.clip;
    }

    /** A copy of this writer with the clip narrowed to {@code box} (intersection with the current clip). */
    public StructureWriter narrowed(final BoundingBox box) {
        if (this.clip == null) {
            return new StructureWriter(this.level, box, this.recorder, this.extraFlags);
        }
        if (!this.clip.intersects(box)) {
            return new StructureWriter(this.level, new BoundingBox(0, 0, 0, -1, -1, -1), this.recorder, this.extraFlags);
        }
        final BoundingBox both = new BoundingBox(
                Math.max(this.clip.minX(), box.minX()), Math.max(this.clip.minY(), box.minY()),
                Math.max(this.clip.minZ(), box.minZ()), Math.min(this.clip.maxX(), box.maxX()),
                Math.min(this.clip.maxY(), box.maxY()), Math.min(this.clip.maxZ(), box.maxZ()));
        return new StructureWriter(this.level, both, this.recorder, this.extraFlags);
    }

    /** Whether this writer records into a {@link StructureRecording} (DECISIONS R23). */
    public boolean isRecording() {
        return this.recorder != null;
    }

    /** Whether a write at the position would pass the clip. */
    public boolean contains(final int x, final int y, final int z) {
        return this.clip == null || this.clip.isInside(x, y, z);
    }

    /** Whether a write at the position is taken at all: always on a recording writer (the replay clips), else the clip. */
    private boolean accepts(final int x, final int y, final int z) {
        return this.recorder != null || this.contains(x, y, z);
    }

    // ------------------------------------------------------------------------------------------------
    // reads
    // ------------------------------------------------------------------------------------------------

    /** Whether the chunk of the column can be read without leaving the generation region. */
    public boolean canRead(final int x, final int z) {
        if (this.recorder != null) {
            return true;
        }
        if (this.level instanceof WorldGenRegion region) {
            return region.hasChunk(x >> 4, z >> 4);
        }
        return true;
    }

    /**
     * {@code World.getBlock}: air outside the build height and outside a generation region's reach. Recording: the
     * builder's own last write, else the base terrain ({@link StructureRecording}).
     */
    public BlockState getBlock(final int x, final int y, final int z) {
        if (this.recorder != null) {
            return this.recorder.getBlock(x, y, z);
        }
        if (this.level.isOutsideBuildHeight(y) || !this.canRead(x, z)) {
            return AIR;
        }
        return this.level.getBlockState(new BlockPos(x, y, z));
    }

    /** {@code World.isAirBlock}: material air. */
    public boolean isAirBlock(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).isAir();
    }

    /** {@code getBlock(x, y, z) == block}. */
    public boolean is(final int x, final int y, final int z, final Block block) {
        return this.getBlock(x, y, z).is(block);
    }

    /**
     * {@code World.getTileEntity}; {@code null} outside the reach. Recording: a detached block entity of a block the
     * builder wrote, whose final data every chunk replay copies into the real one.
     */
    @Nullable
    public BlockEntity getTileEntity(final int x, final int y, final int z) {
        if (this.recorder != null) {
            return this.recorder.getTileEntity(x, y, z);
        }
        if (this.level.isOutsideBuildHeight(y) || !this.canRead(x, z)) {
            return null;
        }
        return this.level.getBlockEntity(new BlockPos(x, y, z));
    }

    /**
     * {@code World.getBiomeGenForCoords(x, z)}: 1.7.10 biomes were 2D, the port asks at the generation surface
     * ({@code WORLD_SURFACE_WG}) of the column. Air-reach rules as {@link #getBlock}.
     */
    public Holder<Biome> getBiomeGenForCoords(final int x, final int z) {
        if (this.recorder != null) {
            // Recording: the noise biome at the base-terrain surface, independent of which chunks exist.
            return this.recorder.getBiome(x, z);
        }
        final int y = this.canRead(x, z)
                ? this.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z)
                : this.level.getMinBuildHeight();
        return this.level.getBiome(new BlockPos(x, y, z));
    }

    /** {@code getBiomeGenForCoords(x, z).biomeName.equals(name)} with the name mapped to a biome tag. */
    public boolean biomeIs(final int x, final int z, final TagKey<Biome> legacyName) {
        return this.getBiomeGenForCoords(x, z).is(legacyName);
    }

    // ------------------------------------------------------------------------------------------------
    // writes
    // ------------------------------------------------------------------------------------------------

    /**
     * {@code OreSpawnMain.setBlockFast(world, x, y, z, block, meta, flags)} ({@link FastBlocks#setBlockFast}), clipped.
     */
    public boolean setBlock(final int x, final int y, final int z, final BlockState state, final int flags) {
        if (this.recorder != null) {
            return this.recorder.setBlock(x, y, z, state, flags);
        }
        if (!this.contains(x, y, z)) {
            return false;
        }
        if (!this.canRead(x, z)) {
            return false;
        }
        final boolean changed = FastBlocks.setBlockFast(this.level, x, y, z, state, flags | this.extraFlags);
        if (changed && !(this.level instanceof Level)) {
            this.scheduleGenerationTicks(new BlockPos(x, y, z), state);
        }
        return changed;
    }

    /** {@code setBlockFast(..., meta 0, 2)}: the form nearly every builder used. */
    public boolean setBlock(final int x, final int y, final int z, final BlockState state) {
        return this.setBlock(x, y, z, state, Block.UPDATE_CLIENTS);
    }

    /** {@code setBlockFast(block, meta, flags)} with the metadata converted by {@link LegacyMeta#state}. */
    public boolean setBlock(final int x, final int y, final int z, final Block block, final int meta, final int flags) {
        return this.setBlock(x, y, z, LegacyMeta.state(block, meta), flags);
    }

    /** {@code world.setBlockToAir}. */
    public boolean setBlockToAir(final int x, final int y, final int z) {
        return this.setBlock(x, y, z, AIR, Block.UPDATE_ALL);
    }

    private void scheduleGenerationTicks(final BlockPos pos, final BlockState state) {
        final Block block = state.getBlock();
        // PORT: 1.7.10 rendered the connections of fences, panes, iron bars, walls, stairs corners and redstone wire from
        // the neighbours on the fly. On a live level Level.setBlock's neighbour shape updates rebuild them; a
        // WorldGenRegion runs none, so every such block of a generated structure stayed in its default (unconnected)
        // state. Marked for the post-processing a chunk runs when it becomes FULL (LevelChunk.postProcessGeneration ->
        // Block.updateFromNeighbourShapes), the way StructurePiece.placeBlock marks its SHAPE_CHECK_BLOCKS.
        if (block instanceof CrossCollisionBlock || block instanceof WallBlock || block instanceof StairBlock
                || block instanceof RedStoneWireBlock) {
            this.level.getChunk(pos).markPosForPostprocessing(pos);
        }
        if (block instanceof MushroomBlock) {
            this.unmarkPostProcessing(pos);
        }
        if (block instanceof FallingBlock) {
            this.level.scheduleTick(pos, block, 2);
        } else if (block instanceof LeavesBlock) {
            this.level.scheduleTick(pos, block, 1);
        }
        final FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            this.level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(this.level));
        }
    }

    /**
     * PORT: 1.7.10 {@code BlockMushroom} checked {@code canBlockStay} only on a neighbour change
     * ({@code BlockBush.onNeighborBlockChange}); its {@code updateTick} spreads and never drops, so a mushroom a build set
     * into bright light (the Greenhouse on farmland under glowstone) stayed. A 1.21.1 generation region marks every
     * mushroom for post-processing ({@code Blocks.BROWN_MUSHROOM/RED_MUSHROOM hasPostProcess}, {@code WorldGenRegion.setBlock}),
     * and {@code LevelChunk.postProcessGeneration} removes one that cannot survive as soon as the chunk becomes FULL - which
     * a build on the live level never sees. The mark of a mushroom this writer set is dropped again.
     */
    private void unmarkPostProcessing(final BlockPos pos) {
        final ChunkAccess chunk = this.level.getChunk(pos);
        final ShortList[] lists = chunk.getPostProcessing();
        final int index = chunk.getSectionIndex(pos.getY());
        if (index < 0 || index >= lists.length || lists[index] == null) {
            return;
        }
        final short packed = ProtoChunk.packOffsetCoordinates(pos);
        while (lists[index].rem(packed)) {
            // WorldGenRegion.setBlock marks again on every write of the same position
        }
    }

    /**
     * {@code setBlock(x, y, z, Blocks.mob_spawner, 0, 2)} and {@code getTileEntity(...).func_145881_a().setEntityName(name)}.
     *
     * <p>PORT: the 1.7.10 spawner name (e.g. {@code "Dungeon Beast"}) is given as the 1.21.1 registry id
     * ({@code "orespawn:dungeon_beast"}, manifest). An id that is not registered - an entity of a wave that is not
     * ported yet (W09-W11) - leaves an empty spawner; the call site carries the {@code // PORT: TODO Wnn}.
     *
     * @return whether the spawner block was written
     */
    public boolean setSpawner(final int x, final int y, final int z, final String entityId) {
        // A live level reports false for a write that changes nothing; what counts is the block afterwards.
        this.setBlock(x, y, z, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
        if (!this.accepts(x, y, z) || !this.getBlock(x, y, z).is(Blocks.SPAWNER)) {
            return false;
        }
        final Optional<EntityType<?>> type = entityType(entityId);
        if (type.isPresent() && this.getTileEntity(x, y, z) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type.get(), this.level.getRandom());
        }
        return true;
    }

    /**
     * {@code setBlock(chest, 0, 2)}, optionally {@code setBlockMetadataWithNotify(meta, 3)}, then
     * {@link WeightedRandomChestContent#generateChestContents} with {@code draws} draws.
     *
     * @param meta  the 1.7.10 chest metadata ({@link LegacyMeta#chest}); pass -1 for "never set" (south)
     * @return whether the chest was written; it is filled only then
     */
    public boolean setChest(final int x, final int y, final int z, final int meta,
                            @Nullable final WeightedRandomChestContent[] contents, final int draws, final Random random) {
        this.setBlock(x, y, z, LegacyMeta.chest(meta), Block.UPDATE_CLIENTS);
        if (!this.accepts(x, y, z) || !this.getBlock(x, y, z).is(Blocks.CHEST)) {
            return false;
        }
        if (contents != null) {
            this.fillChest(x, y, z, contents, draws, random);
        }
        return true;
    }

    /** {@code WeightedRandomChestContent.generateChestContents(rand, list, (IInventory) getTileEntity(x, y, z), draws)}. */
    public boolean fillChest(final int x, final int y, final int z, final WeightedRandomChestContent[] contents,
                             final int draws, final Random random) {
        if (!this.accepts(x, y, z)) {
            return false;
        }
        if (this.getTileEntity(x, y, z) instanceof Container container) {
            WeightedRandomChestContent.generateChestContents(random, contents, container, draws);
            return true;
        }
        return false;
    }

    /**
     * {@code EntityList.createEntityByName(name, world)} + {@code setLocationAndAngles} + {@code spawnEntityInWorld};
     * {@code playLivingSound} only on a live level (a generation region has no listener and runs off-thread).
     *
     * @return the entity, or {@code null} when the id is not registered or the position is outside the clip
     */
    @Nullable
    public Entity spawnEntity(final String entityId, final double x, final double y, final double z,
                              final float yaw, final float pitch) {
        return this.spawnEntity(entityId, x, y, z, yaw, pitch, entity -> { });
    }

    /**
     * As {@link #spawnEntity(String, double, double, double, float, float)}, with {@code beforeAdd} applied before
     * {@code addFreshEntity}. On a {@code WorldGenRegion} that call ends in {@code ProtoChunk.addEntity}, which
     * saves the entity to NBT immediately, so state set on the returned entity afterwards is never stored.
     */
    @Nullable
    public Entity spawnEntity(final String entityId, final double x, final double y, final double z,
                              final float yaw, final float pitch,
                              final java.util.function.Consumer<Entity> beforeAdd) {
        if (this.recorder != null) {
            return this.recorder.spawnEntity(entityId, x, y, z, yaw, pitch, beforeAdd);
        }
        if (!this.contains((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z))
                || !this.canRead((int) Math.floor(x), (int) Math.floor(z))) {
            return null;
        }
        final Optional<EntityType<?>> type = entityType(entityId);
        if (type.isEmpty() || !(this.level instanceof ServerLevelAccessor server)) {
            return null;
        }
        final Entity entity = type.get().create(server.getLevel());
        if (entity == null) {
            return null;
        }
        entity.moveTo(x, y, z, yaw, pitch);
        beforeAdd.accept(entity);
        this.level.addFreshEntity(entity);
        if (this.level instanceof Level && entity instanceof Mob mob) {
            mob.playAmbientSound();
        }
        return entity;
    }

    /** The registered entity type of a manifest id, if that wave is ported. */
    public static Optional<EntityType<?>> entityType(final String entityId) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(entityId));
    }
}
