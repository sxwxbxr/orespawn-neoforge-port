package com.swbr.orespawn.world.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Port of the block write helpers of {@code OreSpawnMain} (OreSpawnMain.java:5499-5631):
 * {@code setBlockFast}, {@code setBlockSuperFast}, {@code setBlockIDWithMetadataInChunk} and
 * {@code getBlockIDInChunk}. Structures, trees, mazes, islands and the dimension chunk providers
 * write through these; nothing else in the original touched {@code World.setBlock} except spawners
 * and chests, which need a block entity.
 *
 * <p>The original wrote straight into {@code ExtendedBlockStorage}: no light update, no
 * {@code onBlockAdded}, no tile entity, no support check, and the chunk was not even marked dirty.
 * Its {@code flags} argument then decided what the world got told - and those bits are the same
 * numbers in 1.21.1:
 *
 * <table>
 * <tr><th>bit</th><th>1.7.10</th><th>1.21.1 ({@link Block})</th></tr>
 * <tr><td>1</td><td>{@code notifyBlockChange} (neighbour block updates)</td><td>{@link Block#UPDATE_NEIGHBORS}</td></tr>
 * <tr><td>2</td><td>{@code markBlockForUpdate} (send to clients)</td><td>{@link Block#UPDATE_CLIENTS}</td></tr>
 * <tr><td>4</td><td>suppress the client re-render</td><td>{@link Block#UPDATE_INVISIBLE}</td></tr>
 * </table>
 *
 * The flags are therefore passed through unchanged: a call with flag 2 - the overwhelming majority
 * (GenericDungeon, Trees, RubyBirdDungeon, OreSpawnWorld) - reaches the clients and sends no
 * neighbour block update, exactly as before; flag 3 (NightmareDungeon, the leaf classes) updates
 * neighbours too. {@code Level.markAndNotifyBlock} tests the same three bits.
 *
 * <p>What cannot be reproduced and is deliberately not:
 * <ul>
 * <li>{@code onPlace}/{@code onRemove}, block entities and lighting now run inside
 * {@code LevelChunk.setBlockState}; that is the difference between a floating chest that has no
 * inventory and one that does. Keep.</li>
 * <li>Neighbour <em>shape</em> updates (a 1.13 concept, {@code updateNeighbourShapes}) run whenever
 * bit 16 is absent, regardless of bit 1. Fences, walls, panes and double chests take their connected
 * state from this - the 1.7.10 renderer computed those connections on the fly. Keep, and note that a
 * block whose support is written <em>after</em> it may still be removed by its own
 * {@code updateShape}.</li>
 * <li>The missing dirty mark (verhalten/core-01a.md, port note): not rebuilt.</li>
 * </ul>
 *
 * <p>Worldgen callers write into a {@code WorldGenLevel}, which is a {@link LevelAccessor}; use the
 * overload with a {@link BoundingBox} from a {@code StructurePiece}, because a {@code WorldGenRegion}
 * logs an error and drops any write outside its 3x3 window ({@code WorldGenRegion.ensureCanWrite}),
 * whereas the original wrote across chunk borders without noticing (DECISIONS R12).
 */
public final class FastBlocks {

    /** World floor and ceiling of 1.7.10; {@code setBlockFast} rejected {@code y < 0} and {@code y >= 256}. */
    public static final int LEGACY_MIN_Y = 0;
    public static final int LEGACY_MAX_Y = 256;

    private FastBlocks() {
    }

    /**
     * True if {@code y} would have been rejected by the original helper. The port clips at the
     * dimension's build height instead (see {@link #setBlockFast}); a worldgen porter who wants the
     * 1.7.10 clip on purpose (verhalten/world-01.md, "Höhen": Bee Hive reaches 30 blocks below the
     * surface) adds this check at the call site.
     */
    public static boolean isOutsideLegacyHeight(final int y) {
        return y < LEGACY_MIN_Y || y >= LEGACY_MAX_Y;
    }

    /**
     * Port of {@code OreSpawnMain.setBlockFast(world, x, y, z, block, meta, flags)}
     * (OreSpawnMain.java:5499-5524). Rejects x/z outside {@code [-30000000, 30000000)} and y outside
     * the level; writes the state with the original flag bits.
     *
     * @return whether the block was written; {@code false} on rejection or when the chunk refused it
     */
    public static boolean setBlockFast(final LevelAccessor world, final int par1, final int par2, final int par3,
                                       final BlockState state, final int par6) {
        if (par1 < -Level.MAX_LEVEL_SIZE || par3 < -Level.MAX_LEVEL_SIZE
                || par1 >= Level.MAX_LEVEL_SIZE || par3 >= Level.MAX_LEVEL_SIZE) {
            return false;
        }
        // PORT: the original rejected y < 0 and y >= 256 (OreSpawnMain.java:5503-5508). Those were the
        // 1.7.10 world limits, not a design choice; the 1.21.1 overworld reaches -64..320 and R18 puts
        // surface searches on the heightmap. Clip at the dimension's build height instead - identical
        // in the OreSpawn dimensions (min_y 0, height 256). See isOutsideLegacyHeight for the 1:1 clip.
        if (world.isOutsideBuildHeight(par2)) {
            return false;
        }
        return world.setBlock(new BlockPos(par1, par2, par3), state, par6);
    }

    /**
     * {@link #setBlockFast(LevelAccessor, int, int, int, BlockState, int)} clipped to a structure
     * piece's bounding box. Positions outside the box are dropped and return {@code false}; that is the
     * {@code StructurePiece.placeBlock} rule (DECISIONS R12), the original had no such clip.
     */
    public static boolean setBlockFast(final LevelAccessor world, @Nullable final BoundingBox box,
                                       final int par1, final int par2, final int par3,
                                       final BlockState state, final int par6) {
        if (box != null && !box.isInside(par1, par2, par3)) {
            return false;
        }
        return setBlockFast(world, par1, par2, par3, state, par6);
    }

    /**
     * Port of {@code OreSpawnMain.setBlockSuperFast(world, x, y, z, block, meta, flags, refChunk)}
     * (OreSpawnMain.java:5526-5557). Identical to {@link #setBlockFast} when the target lies in a
     * different chunk than {@code refChunk} (or {@code refChunk} is null, as {@code ItemAppleSeed}
     * passes it). Inside {@code refChunk} the original wrote without any notification and reported
     * {@code true} regardless.
     */
    public static boolean setBlockSuperFast(final LevelAccessor world, final int par1, final int par2, final int par3,
                                            final BlockState state, final int par6,
                                            @Nullable final ChunkAccess refChunk) {
        if (par1 < -Level.MAX_LEVEL_SIZE || par3 < -Level.MAX_LEVEL_SIZE
                || par1 >= Level.MAX_LEVEL_SIZE || par3 >= Level.MAX_LEVEL_SIZE) {
            return false;
        }
        if (world.isOutsideBuildHeight(par2)) {
            // PORT: build height instead of 0..255, see setBlockFast.
            return false;
        }
        final BlockPos pos = new BlockPos(par1, par2, par3);
        final boolean sameChunk = refChunk != null
                && (par1 >> 4) == refChunk.getPos().x
                && (par3 >> 4) == refChunk.getPos().z;
        if (!sameChunk) {
            return world.setBlock(pos, state, par6);
        }
        // PORT: the original skipped markBlockForUpdate here, so the client never learned about the
        // block until the chunk was resent (DECISIONS R18, case 4: client and server saw different
        // things). Written with UPDATE_CLIENTS and still without neighbour updates; the return value
        // stays true like the original's.
        world.setBlock(pos, state, Block.UPDATE_CLIENTS);
        return true;
    }

    /**
     * Port of {@code OreSpawnMain.setBlockIDWithMetadataInChunk(chunk, x, y, z, block, meta)}
     * (OreSpawnMain.java:5600-5631) for the chunk generators and the crystal maze. World coordinates;
     * anything outside the world border, outside this chunk or outside the height range is
     * <em>dropped</em>. Air into a section that has no storage yet is not written either
     * (OreSpawnMain.java:5617-5620), so a caller cannot rely on this to clear blocks that were never
     * set.
     *
     * @return whether the block was written
     */
    public static boolean setBlockInChunk(final ChunkAccess chunk, final int par1, final int par2, final int par3,
                                          @Nullable final BlockState state) {
        if (par1 < -Level.MAX_LEVEL_SIZE || par3 < -Level.MAX_LEVEL_SIZE
                || par1 >= Level.MAX_LEVEL_SIZE || par3 >= Level.MAX_LEVEL_SIZE) {
            return false;
        }
        if (par1 >> 4 != chunk.getPos().x) {
            return false;
        }
        if (par3 >> 4 != chunk.getPos().z) {
            return false;
        }
        // PORT: build height instead of 0..255, see setBlockFast.
        if (chunk.isOutsideBuildHeight(par2)) {
            return false;
        }
        if (chunk.getSection(chunk.getSectionIndex(par2)).hasOnlyAir()) {
            if (state == null || state.isAir()) {
                return false;
            }
        }
        // PORT: no metadata in 1.21.1; the caller passes the complete BlockState. isMoving false, as
        // every non-piston write.
        chunk.setBlockState(new BlockPos(par1, par2, par3), state, false);
        return true;
    }

    /**
     * Port of {@code OreSpawnMain.getBlockIDInChunk(chunk, x, y, z)} (OreSpawnMain.java:5584-5598):
     * air for anything outside the world border, outside this chunk or outside the height range.
     */
    public static BlockState getBlockInChunk(final ChunkAccess chunk, final int par1, final int par2, final int par3) {
        if (par1 < -Level.MAX_LEVEL_SIZE || par3 < -Level.MAX_LEVEL_SIZE
                || par1 >= Level.MAX_LEVEL_SIZE || par3 >= Level.MAX_LEVEL_SIZE) {
            return Blocks.AIR.defaultBlockState();
        }
        if (par1 >> 4 != chunk.getPos().x) {
            return Blocks.AIR.defaultBlockState();
        }
        if (par3 >> 4 != chunk.getPos().z) {
            return Blocks.AIR.defaultBlockState();
        }
        // PORT: build height instead of 0..255, see setBlockFast.
        if (chunk.isOutsideBuildHeight(par2)) {
            return Blocks.AIR.defaultBlockState();
        }
        return chunk.getBlockState(new BlockPos(par1, par2, par3));
    }
}
