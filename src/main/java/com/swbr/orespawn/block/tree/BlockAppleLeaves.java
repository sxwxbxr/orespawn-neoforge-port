package com.swbr.orespawn.block.tree;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockAppleLeaves}: Apple Tree Leaves ({@code leaves_apple},
 * OreSpawnMain.java:1605, hardness 0.2, light opacity 1, grass sound).
 *
 * <p>Drops (BlockAppleLeaves.java:25-40, loot table {@code blocks/leaves_apple}): four independent
 * server-side rolls, apple 1/25, golden apple 1/500, enchanted golden apple 1/1000,
 * {@code magicapple} 1/10000. {@code quantityDropped} 1 (:42-44) was never consulted.
 *
 * <p>{@code updateTick} (:46-79): radius 2 and fruit chance 1/20, in the Islands dimension radius 1
 * and 1/100 (:50-53). Sustained: with air below and the roll hit, the drop rolls fall below the
 * leaf (:62-65); then, in the Islands dimension after noon, the leaf becomes Scary Leaves
 * (:66-70). {@code BlockScaryLeaves} turns them back in the morning.
 */
public class BlockAppleLeaves extends OreSpawnLeaves {

    public BlockAppleLeaves(Properties properties) {
        super(properties);
    }

    private static boolean inIslands(ServerLevel level) {
        return level.dimension().equals(DIMENSION_ISLANDS);
    }

    /** {@code var7 = 2}, 1 in DimensionID4 (BlockAppleLeaves.java:47, :52). */
    @Override
    protected int decayRadius(ServerLevel level) {
        return inIslands(level) ? 1 : 2;
    }

    /** {@code chance = 20}, 100 in DimensionID4 (BlockAppleLeaves.java:49, :51). */
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
        long t = level.getDayTime();
        t %= 24000L;
        if (t > 12000L && inIslands(level)) {
            // setBlockFast(world, x, y, z, MyScaryLeaves, 0, 3)
            FastBlocks.setBlockFast(level, pos.getX(), pos.getY(), pos.getZ(),
                    ModBlocks.LEAVES_SCARY.get().defaultBlockState(), 3);
        }
    }
}
