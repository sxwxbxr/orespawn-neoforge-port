package com.swbr.orespawn.world.structure;

import com.swbr.orespawn.OreSpawn;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * The single piece of an {@code orespawn:legacy} structure: a builder id, the origin the original build method
 * received, a builder switch and a seed.
 *
 * <p>{@link #postProcess} runs for every chunk the bounding box touches, in any order, possibly on different
 * threads. The first of them runs the whole builder once with a fresh {@code java.util.Random(seed)} against a
 * recording writer; every chunk then replays the part of that recording inside the chunk box and the piece box
 * (DECISIONS R23, {@link StructureRecording}). Reads and chest rolls happen in the one run, so the union over all
 * chunks is the complete structure without seams, the same on every run (DECISIONS R12, R18 "Math.random": seed plus
 * ChunkPos - the seed comes from the world seed, the start chunk and the dispatcher id,
 * {@link LegacyStructureContext#pieceSeed}).
 *
 * <p>The random a 1.21.1 piece receives is deliberately ignored: it is seeded per chunk and would give every chunk
 * a different structure.
 */
public final class LegacyStructurePiece extends StructurePiece {

    private final String builder;
    private final int originX;
    private final int originY;
    private final int originZ;
    private final int variant;
    private final long seed;

    public LegacyStructurePiece(final LegacyStructureRegistry.Placement placement, final long seed) {
        super(OreSpawnStructures.LEGACY_PIECE.get(), 0, placement.box());
        this.builder = placement.builder();
        this.originX = placement.x();
        this.originY = placement.y();
        this.originZ = placement.z();
        this.variant = placement.variant();
        this.seed = seed;
    }

    public LegacyStructurePiece(final CompoundTag tag) {
        super(OreSpawnStructures.LEGACY_PIECE.get(), tag);
        this.builder = tag.getString("Builder");
        this.originX = tag.getInt("X");
        this.originY = tag.getInt("Y");
        this.originZ = tag.getInt("Z");
        this.variant = tag.getInt("Variant");
        this.seed = tag.getLong("Seed");
    }

    @Override
    protected void addAdditionalSaveData(final StructurePieceSerializationContext context, final CompoundTag tag) {
        tag.putString("Builder", this.builder);
        tag.putInt("X", this.originX);
        tag.putInt("Y", this.originY);
        tag.putInt("Z", this.originZ);
        tag.putInt("Variant", this.variant);
        tag.putLong("Seed", this.seed);
    }

    public String builder() {
        return this.builder;
    }

    public BlockPos origin() {
        return new BlockPos(this.originX, this.originY, this.originZ);
    }

    public int variant() {
        return this.variant;
    }

    @Override
    public void postProcess(final WorldGenLevel level, final StructureManager structureManager,
                            final ChunkGenerator generator, final RandomSource random, final BoundingBox box,
                            final ChunkPos chunkPos, final BlockPos pos) {
        final StructureRecording recording = this.recording(level, generator);
        if (recording != null) {
            recording.replay(level, box);
        }
    }

    /**
     * Writes the blocks and block entity data of this piece inside {@code clip} once more, without spawning its entities
     * again ({@link LegacyStructurePass}, DECISIONS R24).
     */
    public void reassert(final WorldGenLevel level, final ChunkGenerator generator, final BoundingBox clip) {
        final StructureRecording recording = this.recording(level, generator);
        if (recording != null) {
            recording.replayBlocks(level, clip);
        }
    }

    /** The cached recording of this piece, built on a miss; {@code null} for an unknown builder. */
    @Nullable
    public StructureRecording recording(final WorldGenLevel level, final ChunkGenerator generator) {
        final Optional<LegacyStructureRegistry.Builder> build = LegacyStructureRegistry.builder(this.builder);
        if (build.isEmpty()) {
            OreSpawn.LOG.warn("orespawn:legacy piece names unknown builder {}", this.builder);
            return null;
        }
        final StructureRecording.Key key = StructureRecording.Key.of(level, this.builder, this.originX, this.originY,
                this.originZ, this.variant, this.seed, this.boundingBox);
        final LegacyStructureRegistry.Builder builder = build.get();
        return StructureRecording.obtain(level, generator, key, writer ->
                builder.build(writer, new Random(key.seed()), key.x(), key.y(), key.z(), key.variant()));
    }
}
