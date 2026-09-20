package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenBigTree} ({@code ard}, client-1.7.10.jar), the big oak of
 * {@link BiomeGenBase#getRandomWorldGenForTrees} ({@code ahu.aA = new ard(false)}). Member names are the MCP
 * names of the obfuscated fields and methods; the arithmetic, including the {@code byte} loop counters, follows
 * the decompiled bytecode.
 *
 * <p>PORT: {@link BiomeGenBase#getRandomWorldGenForTrees} builds one instance per tree, so {@link #heightLimit}
 * is rolled for every tree; in 1.7.10 the biome's single instance kept the first rolled (or shortened) value for
 * every later big oak. See there for why.
 */
public class WorldGenBigTree extends WorldGenAbstractTree {

    /** {@code ard.a} ({@code otherCoordPairs}). */
    private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};
    /** {@code Blocks.leaves}, metadata 0. */
    private static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState();

    /** {@code ard.b} ({@code rand}): reseeded from the decorator random at the start of every {@link #generate}. */
    private final Random rand = new Random();
    /** {@code ard.c} ({@code worldObj}). */
    private LegacyWorld worldObj;
    /** {@code ard.d} ({@code basePos}). */
    private final int[] basePos = {0, 0, 0};
    /** {@code ard.e} ({@code heightLimit}). */
    private int heightLimit;
    /** {@code ard.f} ({@code height}). */
    private int height;
    /** {@code ard.g} ({@code heightAttenuation}). */
    private final double heightAttenuation = 0.618;
    // ard.h (branchDensity, 1.0) is never read.
    /** {@code ard.i} ({@code branchSlope}). */
    private final double branchSlope = 0.381;
    /** {@code ard.j} ({@code scaleWidth}). */
    private double scaleWidth = 1.0;
    /** {@code ard.k} ({@code leafDensity}). */
    private double leafDensity = 1.0;
    /** {@code ard.l} ({@code trunkSize}). */
    private final int trunkSize = 1;
    /** {@code ard.m} ({@code heightLimitLimit}). */
    private int heightLimitLimit = 12;
    /** {@code ard.n} ({@code leafDistanceLimit}). */
    private int leafDistanceLimit = 4;
    /** {@code ard.o} ({@code leafNodes}). */
    private int[][] leafNodes;

    /** {@code ard(boolean)}. */
    public WorldGenBigTree(final boolean doBlockNotify) {
        super(doBlockNotify);
    }

    /** {@code ard.a()} ({@code generateLeafNodeList}). */
    void generateLeafNodeList() {
        this.height = (int) ((double) this.heightLimit * this.heightAttenuation);
        if (this.height >= this.heightLimit) {
            this.height = this.heightLimit - 1;
        }
        int i = (int) (1.382 + Math.pow(this.leafDensity * (double) this.heightLimit / 13.0, 2.0));
        if (i < 1) {
            i = 1;
        }
        final int[][] aint = new int[i * this.heightLimit][4];
        int j = this.basePos[1] + this.heightLimit - this.leafDistanceLimit;
        int k = 1;
        final int l = this.basePos[1] + this.height;
        int i1 = j - this.basePos[1];
        aint[0][0] = this.basePos[0];
        aint[0][1] = j;
        aint[0][2] = this.basePos[2];
        aint[0][3] = l;
        --j;
        while (i1 >= 0) {
            int j1 = 0;
            final float f = this.layerSize(i1);
            if (f < 0.0F) {
                --j;
                --i1;
            } else {
                final double d0 = 0.5;
                for (; j1 < i; ++j1) {
                    final double d1 = this.scaleWidth * (double) f * ((double) this.rand.nextFloat() + 0.328);
                    final double d2 = (double) this.rand.nextFloat() * 2.0 * 3.14159;
                    final int k1 = Mth.floor(d1 * Math.sin(d2) + (double) this.basePos[0] + d0);
                    final int l1 = Mth.floor(d1 * Math.cos(d2) + (double) this.basePos[2] + d0);
                    final int[] aint1 = {k1, j, l1};
                    final int[] aint2 = {k1, j + this.leafDistanceLimit, l1};
                    if (this.checkBlockLine(aint1, aint2) == -1) {
                        final int[] aint3 = {this.basePos[0], this.basePos[1], this.basePos[2]};
                        final double d3 = Math.sqrt(Math.pow((double) Math.abs(this.basePos[0] - aint1[0]), 2.0)
                                + Math.pow((double) Math.abs(this.basePos[2] - aint1[2]), 2.0));
                        final double d4 = d3 * this.branchSlope;
                        if ((double) aint1[1] - d4 > (double) l) {
                            aint3[1] = l;
                        } else {
                            // PORT: (int) -> Mth.floor (DECISIONS R20); equal for every Y >= 0.
                            aint3[1] = Mth.floor((double) aint1[1] - d4);
                        }
                        if (this.checkBlockLine(aint3, aint1) == -1) {
                            aint[k][0] = k1;
                            aint[k][1] = j;
                            aint[k][2] = l1;
                            aint[k][3] = aint3[1];
                            ++k;
                        }
                    }
                }
                --j;
                --i1;
            }
        }
        this.leafNodes = new int[k][4];
        System.arraycopy(aint, 0, this.leafNodes, 0, k);
    }

    /**
     * {@code ard.a(int, int, int, float, byte, Block)} ({@code func_150529_a}, "genTreeLayer"): a disc of radius
     * {@code size} across the two axes other than {@code axis}, written only into air and leaves.
     */
    void genTreeLayer(final int x, final int y, final int z, final float size, final byte axis, final BlockState block) {
        final int i = (int) ((double) size + 0.618);
        final byte b1 = OTHER_COORD_PAIRS[axis];
        final byte b2 = OTHER_COORD_PAIRS[axis + 3];
        final int[] aint = {x, y, z};
        final int[] aint1 = {0, 0, 0};
        int j = -i;
        aint1[axis] = aint[axis];
        for (; j <= i; ++j) {
            aint1[b1] = aint[b1] + j;
            int k = -i;
            while (k <= i) {
                final double d0 = Math.pow((double) Math.abs(j) + 0.5, 2.0) + Math.pow((double) Math.abs(k) + 0.5, 2.0);
                if (d0 > (double) (size * size)) {
                    ++k;
                } else {
                    aint1[b2] = aint[b2] + k;
                    final BlockState existing = this.worldObj.getBlock(aint1[0], aint1[1], aint1[2]);
                    if (!existing.isAir() && !LegacyWorld.isLeaves(existing)) {
                        ++k;
                    } else {
                        this.setBlockAndNotifyAdequately(this.worldObj, aint1[0], aint1[1], aint1[2], block);
                        ++k;
                    }
                }
            }
        }
    }

    /** {@code ard.a(int)} ({@code layerSize}): the canopy radius at height {@code y} above the base, or -1.618 below 30 %. */
    float layerSize(final int y) {
        if ((double) y < (double) ((float) this.heightLimit) * 0.3) {
            return -1.618F;
        }
        final float f = (float) this.heightLimit / 2.0F;
        final float f1 = (float) this.heightLimit / 2.0F - (float) y;
        final float f2;
        if (f1 == 0.0F) {
            f2 = f;
        } else if (Math.abs(f1) >= f) {
            f2 = 0.0F;
        } else {
            f2 = (float) Math.sqrt(Math.pow((double) Math.abs(f), 2.0) - Math.pow((double) Math.abs(f1), 2.0));
        }
        return f2 * 0.5F;
    }

    /** {@code ard.b(int)} ({@code leafSize}): 2 at the bottom and top layer of a leaf node, 3 in between. */
    float leafSize(final int y) {
        if (y < 0 || y >= this.leafDistanceLimit) {
            return -1.0F;
        }
        return y != 0 && y != this.leafDistanceLimit - 1 ? 3.0F : 2.0F;
    }

    /** {@code ard.a(int, int, int)} ({@code generateLeafNode}). */
    void generateLeafNode(final int x, final int y, final int z) {
        int i = y;
        for (final int j = y + this.leafDistanceLimit; i < j; ++i) {
            final float f = this.leafSize(i - y);
            this.genTreeLayer(x, i, z, f, (byte) 1, LEAVES);
        }
    }

    /**
     * {@code ard.a(int[], int[], Block)} ({@code func_150530_a}, "placeBlockLine"), always with {@code Blocks.log}.
     * The metadata is 4 when the line runs mostly along x, 8 along z, otherwise 0: the log axis.
     */
    void placeBlockLine(final int[] from, final int[] to) {
        final int[] aint = {0, 0, 0};
        byte b0 = 0;
        byte b1;
        for (b1 = 0; b0 < 3; ++b0) {
            aint[b0] = to[b0] - from[b0];
            if (Math.abs(aint[b0]) > Math.abs(aint[b1])) {
                b1 = b0;
            }
        }
        if (aint[b1] != 0) {
            final byte b2 = OTHER_COORD_PAIRS[b1];
            final byte b3 = OTHER_COORD_PAIRS[b1 + 3];
            final byte b4 = aint[b1] > 0 ? (byte) 1 : (byte) -1;
            final double d0 = (double) aint[b2] / (double) aint[b1];
            final double d1 = (double) aint[b3] / (double) aint[b1];
            final int[] aint1 = {0, 0, 0};
            byte k = 0;
            for (final int l = aint[b1] + b4; k != l; k += b4) {
                aint1[b1] = Mth.floor((double) (from[b1] + k) + 0.5);
                aint1[b2] = Mth.floor((double) from[b2] + (double) k * d0 + 0.5);
                aint1[b3] = Mth.floor((double) from[b3] + (double) k * d1 + 0.5);
                int b5 = 0;
                final int i1 = Math.abs(aint1[0] - from[0]);
                final int j1 = Math.abs(aint1[2] - from[2]);
                final int k1 = Math.max(i1, j1);
                if (k1 > 0) {
                    if (i1 == k1) {
                        b5 = 4;
                    } else if (j1 == k1) {
                        b5 = 8;
                    }
                }
                this.setBlockAndNotifyAdequately(this.worldObj, aint1[0], aint1[1], aint1[2], log(b5));
            }
        }
    }

    /** {@code ard.b()} ({@code generateLeaves}). */
    void generateLeaves() {
        int i = 0;
        for (final int j = this.leafNodes.length; i < j; ++i) {
            this.generateLeafNode(this.leafNodes[i][0], this.leafNodes[i][1], this.leafNodes[i][2]);
        }
    }

    /** {@code ard.c(int)} ({@code leafNodeNeedsBase}). */
    boolean leafNodeNeedsBase(final int y) {
        return !((double) y < (double) this.heightLimit * 0.2);
    }

    /** {@code ard.c()} ({@code generateTrunk}). */
    void generateTrunk() {
        final int i = this.basePos[0];
        final int j = this.basePos[1];
        final int k = this.basePos[1] + this.height;
        final int l = this.basePos[2];
        final int[] aint = {i, j, l};
        final int[] aint1 = {i, k, l};
        this.placeBlockLine(aint, aint1);
        if (this.trunkSize == 2) {
            ++aint[0];
            ++aint1[0];
            this.placeBlockLine(aint, aint1);
            ++aint[2];
            ++aint1[2];
            this.placeBlockLine(aint, aint1);
            aint[0] += -1;
            aint1[0] += -1;
            this.placeBlockLine(aint, aint1);
        }
    }

    /** {@code ard.d()} ({@code generateLeafNodeBases}). */
    void generateLeafNodeBases() {
        int i = 0;
        final int j = this.leafNodes.length;
        for (final int[] aint = {this.basePos[0], this.basePos[1], this.basePos[2]}; i < j; ++i) {
            final int[] aint1 = this.leafNodes[i];
            final int[] aint2 = {aint1[0], aint1[1], aint1[2]};
            aint[1] = aint1[3];
            final int k = aint[1] - this.basePos[1];
            if (this.leafNodeNeedsBase(k)) {
                this.placeBlockLine(aint, aint2);
            }
        }
    }

    /**
     * {@code ard.a(int[], int[])} ({@code checkBlockLine}): -1 if every block on the line is replaceable
     * ({@link #isReplaceable}), otherwise the distance to the first one that is not.
     */
    int checkBlockLine(final int[] from, final int[] to) {
        final int[] aint = {0, 0, 0};
        byte b0 = 0;
        byte b1;
        for (b1 = 0; b0 < 3; ++b0) {
            aint[b0] = to[b0] - from[b0];
            if (Math.abs(aint[b0]) > Math.abs(aint[b1])) {
                b1 = b0;
            }
        }
        if (aint[b1] == 0) {
            return -1;
        }
        final byte b2 = OTHER_COORD_PAIRS[b1];
        final byte b3 = OTHER_COORD_PAIRS[b1 + 3];
        final byte b4 = aint[b1] > 0 ? (byte) 1 : (byte) -1;
        final double d0 = (double) aint[b2] / (double) aint[b1];
        final double d1 = (double) aint[b3] / (double) aint[b1];
        final int[] aint1 = {0, 0, 0};
        byte i = 0;
        int j;
        for (j = aint[b1] + b4; i != j; i += b4) {
            aint1[b1] = from[b1] + i;
            aint1[b2] = Mth.floor((double) from[b2] + (double) i * d0);
            aint1[b3] = Mth.floor((double) from[b3] + (double) i * d1);
            final BlockState block = this.worldObj.getBlock(aint1[0], aint1[1], aint1[2]);
            if (!this.isReplaceable(block)) {
                break;
            }
        }
        return i == j ? -1 : Math.abs(i);
    }

    /** {@code ard.e()} ({@code validTreeLocation}): soil under the base, and at least 6 free blocks upwards. */
    boolean validTreeLocation() {
        final int[] aint = {this.basePos[0], this.basePos[1], this.basePos[2]};
        final int[] aint1 = {this.basePos[0], this.basePos[1] + this.heightLimit - 1, this.basePos[2]};
        final BlockState below = this.worldObj.getBlock(this.basePos[0], this.basePos[1] - 1, this.basePos[2]);
        if (!below.is(Blocks.GRASS_BLOCK) && !isDirt(below) && !below.is(Blocks.FARMLAND)) {
            return false;
        }
        final int i = this.checkBlockLine(aint, aint1);
        if (i == -1) {
            return true;
        } else if (i < 6) {
            return false;
        }
        this.heightLimit = i;
        return true;
    }

    /** {@code ard.a(double, double, double)} ({@code setScale}); the decorator passes 1, 1, 1. */
    @Override
    public void setScale(final double scaleHeight, final double scaleWidth, final double scaleLeafDensity) {
        this.heightLimitLimit = (int) (scaleHeight * 12.0);
        if (scaleHeight > 0.5) {
            this.leafDistanceLimit = 5;
        }
        this.scaleWidth = scaleWidth;
        this.leafDensity = scaleLeafDensity;
    }

    /** {@code ard.a(World, Random, int, int, int)} ({@code generate}). */
    @Override
    public boolean generate(final LegacyWorld world, final Random par2Random, final int x, final int y, final int z) {
        this.worldObj = world;
        final long seed = par2Random.nextLong();
        this.rand.setSeed(seed);
        this.basePos[0] = x;
        this.basePos[1] = y;
        this.basePos[2] = z;
        if (this.heightLimit == 0) {
            this.heightLimit = 5 + this.rand.nextInt(this.heightLimitLimit);
        }
        if (!this.validTreeLocation()) {
            return false;
        }
        this.generateLeafNodeList();
        this.generateLeaves();
        this.generateTrunk();
        this.generateLeafNodeBases();
        return true;
    }

    /** {@code Blocks.log}, metadata 0 (upright), 4 (along x) or 8 (along z). */
    private static BlockState log(final int meta) {
        final Direction.Axis axis = meta == 4 ? Direction.Axis.X : meta == 8 ? Direction.Axis.Z : Direction.Axis.Y;
        return Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }
}
