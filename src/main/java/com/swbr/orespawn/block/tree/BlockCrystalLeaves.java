package com.swbr.orespawn.block.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockCrystalLeaves}, one class behind three blocks
 * (OreSpawnMain.java:1627, :1629, :1630; light opacity 1, grass sound, tab Decorations):
 *
 * <table>
 * <tr><th>id</th><th>original field</th><th>hardness</th><th>sapling drop</th></tr>
 * <tr><td>{@code crystaltreeleaves}</td><td>{@code MyCrystalLeaves}</td><td>0.2</td><td>{@code crystalsapling}</td></tr>
 * <tr><td>{@code crystaltreeleaves2}</td><td>{@code MyCrystalLeaves2}</td><td>0.25</td><td>{@code crystalsapling2}</td></tr>
 * <tr><td>{@code crystaltreeleaves3}</td><td>{@code MyCrystalLeaves3}</td><td>0.25</td><td>{@code crystalsapling3}</td></tr>
 * </table>
 *
 * Drops (BlockCrystalLeaves.java:24-41, loot tables {@code blocks/crystaltreeleaves*}): two
 * independent rolls, {@code crystalapple} 1/100 and the matching sapling 1/50.
 *
 * <p>{@code updateTick} (:47-75) is the apple-leaf search with the Islands variant (radius 1,
 * chance 1/100 instead of 2 and 1/20) and without the scary-leaf conversion.
 *
 * <p>Look: with fancy graphics the block rendered as a cross ({@code getRenderType() == 1},
 * :90-95), tinted {@code 0xDDDDDD} on every face and in the inventory (:103-116). Fast graphics is
 * gone (DECISIONS R18), so the model is a tinted cross and {@link #COLOR} is registered as block and
 * item colour in {@code client.BlockRenderTypes}.
 */
public class BlockCrystalLeaves extends OreSpawnLeaves {

    /** {@code 14540253} (BlockCrystalLeaves.java:105, :110, :115). */
    public static final int COLOR = 14540253;

    /** Which of the three {@code this == OreSpawnMain.X} identities this instance is. */
    public enum Kind {
        RED, YELLOW, BLUE
    }

    private final Kind kind;

    public BlockCrystalLeaves(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    private static boolean inIslands(ServerLevel level) {
        return level.dimension().equals(DIMENSION_ISLANDS);
    }

    /** {@code var7 = 2}, 1 in DimensionID4 (BlockCrystalLeaves.java:48, :53). */
    @Override
    protected int decayRadius(ServerLevel level) {
        return inIslands(level) ? 1 : 2;
    }

    /** {@code chance = 20}, 100 in DimensionID4 (BlockCrystalLeaves.java:50, :52). */
    private static int fruitChance(ServerLevel level) {
        return inIslands(level) ? 100 : 20;
    }

    @Override
    protected void sustained(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        final int chance = fruitChance(level);
        final BlockState bid = level.getBlockState(pos.below());
        if (bid.isAir() && random.nextInt(chance) == 3) {
            dropBelow(state, level, pos);
        }
    }
}
