package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenTrees} ({@code asq}, client-1.7.10.jar), the small oak of
 * {@link BiomeGenBase#getRandomWorldGenForTrees} ({@code ahu.az = new asq(false)}).
 *
 * <p>PORT: only {@code asq(boolean)} is ported, which is {@code this(notify, 4, 0, 0, false)}: minimum height 4,
 * oak log and oak leaves, no vines. The five-argument constructor with vines and cocoa (jungle biomes) has no
 * caller in the OreSpawn dimensions; with {@code vinesGrow == false} its branches are skipped before they draw a
 * random number ({@code this.b && ...}), so leaving them out changes no sequence.
 */
public class WorldGenTrees extends WorldGenAbstractTree {

    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();

    /** {@code asq.a} ({@code minTreeHeight}). */
    private final int minTreeHeight;
    /** {@code Blocks.log} with {@code asq.c} ({@code metaWood}) 0. */
    private final BlockState wood;
    /** {@code Blocks.leaves} with {@code asq.d} ({@code metaLeaves}) 0. */
    private final BlockState leaves;

    /** {@code asq(boolean)}. */
    public WorldGenTrees(final boolean doBlockNotify) {
        super(doBlockNotify);
        this.minTreeHeight = 4;
        this.wood = Blocks.OAK_LOG.defaultBlockState();
        this.leaves = Blocks.OAK_LEAVES.defaultBlockState();
    }

    /** {@code asq.a(World, Random, int, int, int)}. */
    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        final int l = rand.nextInt(3) + this.minTreeHeight;
        boolean flag = true;
        if (y >= 1 && y + l + 1 <= 256) {
            for (int i1 = y; i1 <= y + 1 + l; ++i1) {
                int b0 = 1;
                if (i1 == y) {
                    b0 = 0;
                }
                if (i1 >= y + 1 + l - 2) {
                    b0 = 2;
                }
                for (int j1 = x - b0; j1 <= x + b0 && flag; ++j1) {
                    for (int k1 = z - b0; k1 <= z + b0 && flag; ++k1) {
                        if (i1 >= 0 && i1 < 256) {
                            if (!this.isReplaceable(world.getBlock(j1, i1, k1))) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }
            if (!flag) {
                return false;
            }
            final BlockState below = world.getBlock(x, y - 1, z);
            if (isSoil(below) && y < 256 - l - 1) {
                this.setBlockAndNotifyAdequately(world, x, y - 1, z, DIRT);
                final int b0 = 3;
                final int b1 = 0;
                for (int k1 = y - b0 + l; k1 <= y + l; ++k1) {
                    final int i3 = k1 - (y + l);
                    final int l1 = b1 + 1 - i3 / 2;
                    for (int i2 = x - l1; i2 <= x + l1; ++i2) {
                        final int j2 = i2 - x;
                        for (int k2 = z - l1; k2 <= z + l1; ++k2) {
                            final int l2 = k2 - z;
                            if (Math.abs(j2) != l1 || Math.abs(l2) != l1 || rand.nextInt(2) != 0 && i3 != 0) {
                                final BlockState block = world.getBlock(i2, k1, k2);
                                if (block.isAir() || LegacyWorld.isLeaves(block)) {
                                    this.setBlockAndNotifyAdequately(world, i2, k1, k2, this.leaves);
                                }
                            }
                        }
                    }
                }
                for (int k1 = 0; k1 < l; ++k1) {
                    final BlockState block = world.getBlock(x, y + k1, z);
                    if (block.isAir() || LegacyWorld.isLeaves(block)) {
                        this.setBlockAndNotifyAdequately(world, x, y + k1, z, this.wood);
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }
}
