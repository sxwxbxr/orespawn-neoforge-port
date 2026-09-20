package com.swbr.orespawn.world.tree;

import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.StructureWriter;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@link LegacyWriter} over the shared {@link StructureWriter}. No original class; see {@link LegacyWriter} for why
 * the tree and maze generators need a view with stable reads.
 *
 * <p>Clipped writer (structure piece, feature): every write of this run is remembered; a read answers the remembered
 * state, else the level inside the clip, else air. PORT: the original read whatever the neighbouring chunk held at
 * that moment. Where a read inside one chunk meets terrain (a branch through a hill) while the run of the
 * neighbouring chunk assumes air, the two runs can take different paths from there and the structure shows a seam at
 * that border; the generators grow in open air above their base, where both answers agree.
 *
 * <p>Unclipped writer (live level): reads and writes go straight through, nothing is remembered.
 */
final class StructureLegacyWriter implements LegacyWriter {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final StructureWriter world;
    private final boolean clipped;
    @Nullable
    private final Long2ObjectOpenHashMap<BlockState> written;

    StructureLegacyWriter(final StructureWriter world) {
        this.world = world;
        this.clipped = world.clip() != null;
        this.written = this.clipped ? new Long2ObjectOpenHashMap<>() : null;
    }

    @Override
    public BlockState getBlock(final int x, final int y, final int z) {
        if (!this.clipped) {
            return this.world.getBlock(x, y, z);
        }
        if (this.world.level().isOutsideBuildHeight(y)) {
            return AIR;
        }
        final BlockState mine = this.written.get(BlockPos.asLong(x, y, z));
        if (mine != null) {
            return mine;
        }
        return this.world.contains(x, y, z) ? this.world.getBlock(x, y, z) : AIR;
    }

    private void remember(final int x, final int y, final int z, final BlockState state) {
        if (this.clipped && !this.world.level().isOutsideBuildHeight(y)) {
            this.written.put(BlockPos.asLong(x, y, z), state);
        }
    }

    private boolean write(final int x, final int y, final int z, final BlockState state) {
        this.remember(x, y, z, state);
        final boolean changed = this.world.setBlock(x, y, z, state);
        if (changed && this.world.level() instanceof Level && state.getBlock() instanceof LeavesBlock) {
            // StructureWriter schedules this tick itself in world generation only; see LegacyWriter.
            this.world.level().scheduleTick(new BlockPos(x, y, z), state.getBlock(), 1);
        }
        return changed;
    }

    @Override
    public void setBlockFast(final int x, final int y, final int z, final BlockState state) {
        this.write(x, y, z, state);
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final BlockState state) {
        this.write(x, y, z, state);
    }

    @Override
    public void placeSpawner(final int x, final int y, final int z, final String entityId) {
        this.remember(x, y, z, Blocks.SPAWNER.defaultBlockState());
        // An unregistered id leaves the spawner empty; every OreSpawn type is registered since W11.
        this.world.setSpawner(x, y, z, entityId);
    }

    @Nullable
    @Override
    public Container placeChest(final int x, final int y, final int z) {
        // PORT: 1.7.10 turned a chest placed with world.setBlock away from a solid neighbour (BlockChest.onBlockAdded)
        // and left one placed with setBlockFast at metadata 0. Both become LegacyMeta.chest(-1), the shared
        // "never rotated" state of the port.
        final BlockState chest = LegacyMeta.chest(-1);
        this.remember(x, y, z, chest);
        if (!this.world.setBlock(x, y, z, chest)) {
            return null;
        }
        return this.world.getTileEntity(x, y, z) instanceof Container container ? container : null;
    }

    @Nullable
    @Override
    public Entity spawnEntity(final String entityId, final double x, final double y, final double z,
                              final Random yawRand, final java.util.function.Consumer<Entity> beforeAdd) {
        if (StructureWriter.entityType(entityId).isEmpty()) {
            // Unknown id: skipped like a name EntityList did not know (every OreSpawn type is registered since W11).
            return null;
        }
        final float yaw = yawRand.nextFloat() * 360.0f;
        return this.world.spawnEntity(entityId, x, y, z, yaw, 0.0f, beforeAdd);
    }
}
