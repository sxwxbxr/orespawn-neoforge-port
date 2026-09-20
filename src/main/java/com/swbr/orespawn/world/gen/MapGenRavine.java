package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code MapGenRavine} ({@code aqs}, client-1.7.10.jar), the ravine carver of
 * the noise-terrain dimensions (ChunkProviderOreSpawn2.java:170). Same array-slot offset as
 * {@link MapGenCaves}; below Y 10 it places {@code flowing_lava}, which is {@code minecraft:lava} in 1.21.1.
 */
public class MapGenRavine extends MapGenBase {

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState();

    /** {@code aqs.d}: the per-Y width multiplier table. */
    private final float[] widthByY = new float[1024];

    /** {@code aqs.a(JII[Block;DDDFFFIID)} ({@code func_151540_a}). */
    protected void generateRavine(final long seed, final int chunkX, final int chunkZ, final BlockState[] blocks,
                                  double x, double y, double z, final float width, float yaw, float pitch,
                                  int step, int maxSteps, final double heightScale) {
        final Random random = new Random(seed);
        final double d20 = (double) (chunkX * 16 + 8);
        final double d22 = (double) (chunkZ * 16 + 8);
        float f24 = 0.0F;
        float f25 = 0.0F;
        if (maxSteps <= 0) {
            final int m = this.range * 16 - 16;
            maxSteps = m - random.nextInt(m / 4);
        }
        boolean isRoom = false;
        if (step == -1) {
            step = maxSteps / 2;
            isRoom = true;
        }
        float f27 = 1.0F;
        for (int i = 0; i < 256; ++i) {
            if (i == 0 || random.nextInt(3) == 0) {
                f27 = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
            }
            this.widthByY[i] = f27 * f27;
        }
        for (; step < maxSteps; ++step) {
            double d28 = 1.5 + (double) (Mth.sin((float) step * 3.1415927F / (float) maxSteps) * width * 1.0F);
            double d30 = d28 * heightScale;
            d28 *= (double) random.nextFloat() * 0.25 + 0.75;
            d30 *= (double) random.nextFloat() * 0.25 + 0.75;
            final float f32 = Mth.cos(pitch);
            final float f33 = Mth.sin(pitch);
            x += (double) (Mth.cos(yaw) * f32);
            y += (double) f33;
            z += (double) (Mth.sin(yaw) * f32);
            pitch *= 0.7F;
            pitch += f25 * 0.05F;
            yaw += f24 * 0.05F;
            f25 *= 0.8F;
            f24 *= 0.5F;
            f25 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f24 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (!isRoom && random.nextInt(4) == 0) {
                continue;
            }
            final double d34 = x - d20;
            final double d36 = z - d22;
            final double d38 = (double) (maxSteps - step);
            final double d40 = (double) (width + 2.0F + 16.0F);
            if (d34 * d34 + d36 * d36 - d38 * d38 > d40 * d40) {
                return;
            }
            if (x < d20 - 16.0 - d28 * 2.0 || z < d22 - 16.0 - d28 * 2.0
                    || x > d20 + 16.0 + d28 * 2.0 || z > d22 + 16.0 + d28 * 2.0) {
                continue;
            }
            int minX = Mth.floor(x - d28) - chunkX * 16 - 1;
            int maxX = Mth.floor(x + d28) - chunkX * 16 + 1;
            int minY = Mth.floor(y - d30) - 1;
            int maxY = Mth.floor(y + d30) + 1;
            int minZ = Mth.floor(z - d28) - chunkZ * 16 - 1;
            int maxZ = Mth.floor(z + d28) - chunkZ * 16 + 1;
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
            for (int i41 = minX; !water && i41 < maxX; ++i41) {
                for (int i42 = minZ; !water && i42 < maxZ; ++i42) {
                    for (int i43 = maxY + 1; !water && i43 >= minY - 1; --i43) {
                        final int i44 = (i41 * 16 + i42) * 256 + i43;
                        if (i43 >= 0 && i43 < 256) {
                            final BlockState block = blocks[i44];
                            if (block == WATER) {
                                water = true;
                            }
                            if (i43 != minY - 1 && i41 != minX && i41 != maxX - 1 && i42 != minZ && i42 != maxZ - 1) {
                                i43 = minY;
                            }
                        }
                    }
                }
            }
            if (water) {
                continue;
            }
            for (int i41 = minX; i41 < maxX; ++i41) {
                final double d42 = ((double) (i41 + chunkX * 16) + 0.5 - x) / d28;
                for (int i44 = minZ; i44 < maxZ; ++i44) {
                    final double d45 = ((double) (i44 + chunkZ * 16) + 0.5 - z) / d28;
                    int i47 = (i41 * 16 + i44) * 256 + maxY;
                    boolean grass = false;
                    if (d42 * d42 + d45 * d45 < 1.0) {
                        for (int i49 = maxY - 1; i49 >= minY; --i49) {
                            final double d50 = ((double) i49 + 0.5 - y) / d30;
                            if ((d42 * d42 + d45 * d45) * (double) this.widthByY[i49] + d50 * d50 / 6.0 < 1.0) {
                                final BlockState block = blocks[i47];
                                if (block == GRASS) {
                                    grass = true;
                                }
                                if (block == STONE || block == DIRT || block == GRASS) {
                                    if (i49 < 10) {
                                        blocks[i47] = LAVA;
                                    } else {
                                        blocks[i47] = null;
                                        if (grass && blocks[i47 - 1] == DIRT) {
                                            blocks[i47 - 1] = this.biomeTopBlock;
                                        }
                                    }
                                }
                            }
                            --i47;
                        }
                    }
                }
            }
            if (isRoom) {
                break;
            }
        }
    }

    /** {@code aqs.a(World, int, int, int, int, Block[])} ({@code func_151538_a}): one ravine in 50 chunks. */
    @Override
    protected void recursiveGenerate(final int chunkX, final int chunkZ, final int originalChunkX,
                                     final int originalChunkZ, final BlockState[] blocks) {
        if (this.rand.nextInt(50) != 0) {
            return;
        }
        final double d0 = (double) (chunkX * 16 + this.rand.nextInt(16));
        final double d1 = (double) (this.rand.nextInt(this.rand.nextInt(40) + 8) + 20);
        final double d2 = (double) (chunkZ * 16 + this.rand.nextInt(16));
        final int b0 = 1;
        for (int i = 0; i < b0; ++i) {
            final float f = this.rand.nextFloat() * 3.1415927F * 2.0F;
            final float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
            final float f2 = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
            this.generateRavine(this.rand.nextLong(), originalChunkX, originalChunkZ, blocks, d0, d1, d2, f2, f, f1,
                    0, 0, 3.0);
        }
    }
}
