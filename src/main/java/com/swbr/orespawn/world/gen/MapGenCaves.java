package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code MapGenCaves} ({@code aqw}, client-1.7.10.jar), the cave carver of the
 * noise-terrain dimensions (ChunkProviderOreSpawn2.java:169). Line by line from the bytecode; the 1.21.1
 * {@code CaveWorldCarver} is a descendant with different shapes.
 *
 * <p>Two 1.7.10 quirks are kept on purpose: the carve loop reads and writes the array slot one block
 * <em>above</em> the Y it tests ({@code idx} starts at {@code maxY}, {@code y} at {@code maxY - 1}), and lava
 * fills everything carved below Y 10. {@code minecraft:flowing_water} and {@code minecraft:water} are one
 * block in 1.21.1, so the water test is a single comparison.
 */
public class MapGenCaves extends MapGenBase {

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();

    /** {@code aqw.a(JII[Block;DDD)} ({@code func_151542_a}): a round room. */
    protected void generateLargeCaveNode(final long seed, final int chunkX, final int chunkZ, final BlockState[] blocks,
                                         final double x, final double y, final double z) {
        this.generateCaveNode(seed, chunkX, chunkZ, blocks, x, y, z, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F,
                -1, -1, 0.5);
    }

    /** {@code aqw.a(JII[Block;DDDFFFIID)} ({@code func_151541_a}): one tunnel, recursing at its branch point. */
    protected void generateCaveNode(final long seed, final int chunkX, final int chunkZ, final BlockState[] blocks,
                                    double x, double y, double z, final float width, float yaw, float pitch,
                                    int step, int maxSteps, final double heightScale) {
        final double d19 = (double) (chunkX * 16 + 8);
        final double d21 = (double) (chunkZ * 16 + 8);
        float f23 = 0.0F;
        float f24 = 0.0F;
        final Random random = new Random(seed);
        if (maxSteps <= 0) {
            final int m = this.range * 16 - 16;
            maxSteps = m - random.nextInt(m / 4);
        }
        boolean isRoom = false;
        if (step == -1) {
            step = maxSteps / 2;
            isRoom = true;
        }
        final int branchPoint = random.nextInt(maxSteps / 2) + maxSteps / 4;
        final boolean steep = random.nextInt(6) == 0;
        for (; step < maxSteps; ++step) {
            final double d29 = 1.5 + (double) (Mth.sin((float) step * 3.1415927F / (float) maxSteps) * width * 1.0F);
            final double d31 = d29 * heightScale;
            final float f33 = Mth.cos(pitch);
            final float f34 = Mth.sin(pitch);
            x += (double) (Mth.cos(yaw) * f33);
            y += (double) f34;
            z += (double) (Mth.sin(yaw) * f33);
            if (steep) {
                pitch *= 0.92F;
            } else {
                pitch *= 0.7F;
            }
            pitch += f24 * 0.1F;
            yaw += f23 * 0.1F;
            f24 *= 0.9F;
            f23 *= 0.75F;
            f24 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f23 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (!isRoom && step == branchPoint && width > 1.0F && maxSteps > 0) {
                this.generateCaveNode(random.nextLong(), chunkX, chunkZ, blocks, x, y, z,
                        random.nextFloat() * 0.5F + 0.5F, yaw - 1.5707964F, pitch / 3.0F, step, maxSteps, 1.0);
                this.generateCaveNode(random.nextLong(), chunkX, chunkZ, blocks, x, y, z,
                        random.nextFloat() * 0.5F + 0.5F, yaw + 1.5707964F, pitch / 3.0F, step, maxSteps, 1.0);
                return;
            }
            if (!isRoom && random.nextInt(4) == 0) {
                continue;
            }
            final double d35 = x - d19;
            final double d37 = z - d21;
            final double d39 = (double) (maxSteps - step);
            final double d41 = (double) (width + 2.0F + 16.0F);
            if (d35 * d35 + d37 * d37 - d39 * d39 > d41 * d41) {
                return;
            }
            if (x < d19 - 16.0 - d29 * 2.0 || z < d21 - 16.0 - d29 * 2.0
                    || x > d19 + 16.0 + d29 * 2.0 || z > d21 + 16.0 + d29 * 2.0) {
                continue;
            }
            int minX = Mth.floor(x - d29) - chunkX * 16 - 1;
            int maxX = Mth.floor(x + d29) - chunkX * 16 + 1;
            int minY = Mth.floor(y - d31) - 1;
            int maxY = Mth.floor(y + d31) + 1;
            int minZ = Mth.floor(z - d29) - chunkZ * 16 - 1;
            int maxZ = Mth.floor(z + d29) - chunkZ * 16 + 1;
            if (minX < 0) {
                minX = 0;
            }
            if (maxX > 16) {
                maxX = 16;
            }
            if (minY < 1) {
                minY = 1;
            }
            if (maxY > 248) {
                maxY = 248;
            }
            if (minZ < 0) {
                minZ = 0;
            }
            if (maxZ > 16) {
                maxZ = 16;
            }
            boolean water = false;
            for (int i42 = minX; !water && i42 < maxX; ++i42) {
                for (int i43 = minZ; !water && i43 < maxZ; ++i43) {
                    for (int i44 = maxY + 1; !water && i44 >= minY - 1; --i44) {
                        final int i45 = (i42 * 16 + i43) * 256 + i44;
                        if (i44 >= 0 && i44 < 256) {
                            final BlockState block = blocks[i45];
                            if (block == WATER) {
                                water = true;
                            }
                            if (i44 != minY - 1 && i42 != minX && i42 != maxX - 1 && i43 != minZ && i43 != maxZ - 1) {
                                i44 = minY;
                            }
                        }
                    }
                }
            }
            if (water) {
                continue;
            }
            for (int i42 = minX; i42 < maxX; ++i42) {
                final double d43 = ((double) (i42 + chunkX * 16) + 0.5 - x) / d29;
                for (int i45 = minZ; i45 < maxZ; ++i45) {
                    final double d46 = ((double) (i45 + chunkZ * 16) + 0.5 - z) / d29;
                    int i48 = (i42 * 16 + i45) * 256 + maxY;
                    boolean grass = false;
                    if (d43 * d43 + d46 * d46 < 1.0) {
                        for (int i50 = maxY - 1; i50 >= minY; --i50) {
                            final double d51 = ((double) i50 + 0.5 - y) / d31;
                            if (d51 > -0.7 && d43 * d43 + d51 * d51 + d46 * d46 < 1.0) {
                                final BlockState block = blocks[i48];
                                if (block == GRASS) {
                                    grass = true;
                                }
                                if (block == STONE || block == DIRT || block == GRASS) {
                                    if (i50 < 10) {
                                        blocks[i48] = LAVA;
                                    } else {
                                        blocks[i48] = null;
                                        if (grass && blocks[i48 - 1] == DIRT) {
                                            blocks[i48 - 1] = this.biomeTopBlock;
                                        }
                                    }
                                }
                            }
                            --i48;
                        }
                    }
                }
            }
            if (isRoom) {
                break;
            }
        }
    }

    /** {@code aqw.a(World, int, int, int, int, Block[])} ({@code func_151538_a}). */
    @Override
    protected void recursiveGenerate(final int chunkX, final int chunkZ, final int originalChunkX,
                                     final int originalChunkZ, final BlockState[] blocks) {
        int i = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);
        if (this.rand.nextInt(7) != 0) {
            i = 0;
        }
        for (int j = 0; j < i; ++j) {
            final double d0 = (double) (chunkX * 16 + this.rand.nextInt(16));
            final double d1 = (double) this.rand.nextInt(this.rand.nextInt(120) + 8);
            final double d2 = (double) (chunkZ * 16 + this.rand.nextInt(16));
            int k = 1;
            if (this.rand.nextInt(4) == 0) {
                this.generateLargeCaveNode(this.rand.nextLong(), originalChunkX, originalChunkZ, blocks, d0, d1, d2);
                k += this.rand.nextInt(4);
            }
            for (int l = 0; l < k; ++l) {
                final float f = this.rand.nextFloat() * 3.1415927F * 2.0F;
                final float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
                if (this.rand.nextInt(10) == 0) {
                    f2 *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
                }
                this.generateCaveNode(this.rand.nextLong(), originalChunkX, originalChunkZ, blocks, d0, d1, d2, f2, f,
                        f1, 0, 0, 1.0);
            }
        }
    }
}
