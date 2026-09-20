package com.swbr.orespawn.block.tree;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the Forge 1.7.10 hook {@code Block.canSustainLeaves(world, x, y, z)}, which the four
 * OreSpawn leaf classes ask of every block in their search cube before deciding whether to decay
 * (BlockAppleLeaves.java:61, BlockScaryLeaves.java:58, BlockExperienceLeaves.java:45,
 * BlockCrystalLeaves.java:62).
 *
 * <p>In 1.7.10 the hook returned {@code true} for {@code BlockLog} (every vanilla log, including the
 * all-bark metas) and for the three OreSpawn logs, which override it (BlockSkyTreeLog.java:28-30,
 * BlockDuplicatorLog.java:41-43, BlockCrystalTreeLog.java:29-31). 1.21.1 has no such method: the
 * vanilla half is {@code #minecraft:logs}, the OreSpawn half is this marker, implemented by the same
 * three classes that overrode the method.
 */
public interface CanSustainLeaves {

    /** {@code bid.canSustainLeaves(world, x, y, z)} for the block state at a search position. */
    static boolean canSustainLeaves(BlockState state) {
        return state.is(BlockTags.LOGS) || state.getBlock() instanceof CanSustainLeaves;
    }
}
