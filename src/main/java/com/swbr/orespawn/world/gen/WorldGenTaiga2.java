package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenTaiga2} ({@code asn}, client-1.7.10.jar): the spruce that
 * {@link BiomeGenHills#getRandomWorldGenForTrees} picks in two calls of three ({@code aid.aD = new asn(false)}).
 */
public class WorldGenTaiga2 extends WorldGenAbstractTree {

    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    /** {@code Blocks.leaves}, metadata 1. */
    private static final BlockState SPRUCE_LEAVES = Blocks.SPRUCE_LEAVES.defaultBlockState();
    /** {@code Blocks.log}, metadata 1. */
    private static final BlockState SPRUCE_LOG = Blocks.SPRUCE_LOG.defaultBlockState();

    /** {@code asn(boolean)}. */
    public WorldGenTaiga2(final boolean doBlockNotify) {
        super(doBlockNotify);
    }

    /** {@code asn.a(World, Random, int, int, int)}. */
    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        final int l = rand.nextInt(4) + 6;
        final int i1 = 1 + rand.nextInt(2);
        final int j1 = l - i1;
        final int k1 = 2 + rand.nextInt(2);
        boolean flag = true;
        if (y >= 1 && y + l + 1 <= 256) {
            for (int l1 = y; l1 <= y + 1 + l && flag; ++l1) {
                final int radius;
                if (l1 - y < i1) {
                    radius = 0;
                } else {
                    radius = k1;
                }
                for (int i2 = x - radius; i2 <= x + radius && flag; ++i2) {
                    for (int j2 = z - radius; j2 <= z + radius && flag; ++j2) {
                        if (l1 >= 0 && l1 < 256) {
                            final BlockState block = world.getBlock(i2, l1, j2);
                            if (!block.isAir() && !LegacyWorld.isLeaves(block)) {
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
                int l3 = rand.nextInt(2);
                int i2 = 1;
                int b0 = 0;
                for (int j2 = 0; j2 <= j1; ++j2) {
                    final int i4 = y + l - j2;
                    for (int l2 = x - l3; l2 <= x + l3; ++l2) {
                        final int i3 = l2 - x;
                        for (int j3 = z - l3; j3 <= z + l3; ++j3) {
                            final int k3 = j3 - z;
                            if ((Math.abs(i3) != l3 || Math.abs(k3) != l3 || l3 <= 0) && !world.isFullBlock(l2, i4, j3)) {
                                this.setBlockAndNotifyAdequately(world, l2, i4, j3, SPRUCE_LEAVES);
                            }
                        }
                    }
                    if (l3 >= i2) {
                        l3 = b0;
                        b0 = 1;
                        ++i2;
                        if (i2 > k1) {
                            i2 = k1;
                        }
                    } else {
                        ++l3;
                    }
                }
                final int j4 = rand.nextInt(3);
                for (int j2 = 0; j2 < l - j4; ++j2) {
                    final BlockState block = world.getBlock(x, y + j2, z);
                    if (block.isAir() || LegacyWorld.isLeaves(block)) {
                        this.setBlockAndNotifyAdequately(world, x, y + j2, z, SPRUCE_LOG);
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }
}
