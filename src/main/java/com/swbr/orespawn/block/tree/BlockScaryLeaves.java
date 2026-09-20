package com.swbr.orespawn.block.tree;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockScaryLeaves}, one class behind three blocks
 * (OreSpawnMain.java:1618-1620):
 *
 * <table>
 * <tr><th>id</th><th>original field</th><th>hardness</th><th>drop (BlockScaryLeaves.java:27-36)</th></tr>
 * <tr><td>{@code leaves_scary}</td><td>{@code MyScaryLeaves}</td><td>0.2</td><td>nothing</td></tr>
 * <tr><td>{@code leaves_cherry}</td><td>{@code MyCherryLeaves}</td><td>0.15</td><td>{@code cherries} 1/25</td></tr>
 * <tr><td>{@code leaves_peach}</td><td>{@code MyPeachLeaves}</td><td>0.15</td><td>{@code peach} 1/25</td></tr>
 * </table>
 *
 * {@code quantityDropped} (:38-46, cherry {@code nextInt(4)}, peach {@code nextInt(1)}) was never
 * consulted: the override of {@code dropBlockAsItemWithChance} spawns the stack directly. The
 * catalogue's "peach never drops" reading (DECISIONS R18 list) comes from that dead method; the
 * live path drops a peach one time in 25, and so does the loot table.
 *
 * <p>{@code updateTick} (:48-76): radius 2. Sustained: Scary Leaves before noon become Apple
 * Leaves (:59-63); then, with air below, 1/20 the drop rolls fall below the leaf (:64-67) - for
 * Scary Leaves that is a roll for nothing, kept as written.
 */
public class BlockScaryLeaves extends OreSpawnLeaves {

    /** Which of the three {@code this == OreSpawnMain.X} identities this instance is. */
    public enum Kind {
        SCARY, CHERRY, PEACH
    }

    private final Kind kind;

    public BlockScaryLeaves(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    protected void sustained(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        long t = level.getDayTime();
        t %= 24000L;
        if (this.kind == Kind.SCARY && t < 12000L) {
            // setBlockFast(world, x, y, z, MyAppleLeaves, 0, 3)
            FastBlocks.setBlockFast(level, pos.getX(), pos.getY(), pos.getZ(),
                    ModBlocks.LEAVES_APPLE.get().defaultBlockState(), 3);
        }
        final BlockState bid = level.getBlockState(pos.below());
        if (bid.isAir() && random.nextInt(20) == 3) {
            dropBelow(state, level, pos);
        }
    }

    // randomDisplayTick (BlockScaryLeaves.java:78-79) was empty; Block.animateTick is empty too.
}
