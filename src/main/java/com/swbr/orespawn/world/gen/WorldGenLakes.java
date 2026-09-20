package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenLakes} ({@code arx}, client-1.7.10.jar), used by the populate step of
 * the Mining and VillageMania dimensions (ChunkProviderOreSpawn2.java:288-301, ChunkProviderOreSpawn3.java:288,
 * :295). A 16x8x16 blob of four to seven ellipsoids, shifted by -8 on x and z and anchored four blocks below the
 * first non-air block.
 *
 * <p>Not the 1.21.1 {@code LakeFeature}: that one lost the "sink through air until {@code y <= 5}" loop at the
 * start, so a lake rolled at a random height above the terrain would be placed in mid-air instead of on the
 * ground below. With the populate origin at chunk +8..+23 the box stays inside the populated chunk and its
 * +x/+z neighbours.
 *
 * <p>The mycelium check of the grass pass is kept; no OreSpawn biome has mycelium as top block.
 */
public class WorldGenLakes extends WorldGenerator {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState ICE = Blocks.ICE.defaultBlockState();
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState MYCELIUM = Blocks.MYCELIUM.defaultBlockState();

    private final BlockState block;

    /** {@code arx(Block)}: still water or still lava. */
    public WorldGenLakes(final BlockState block) {
        this.block = block;
    }

    private static int index(final int x, final int z, final int y) {
        return (x * 16 + z) * 8 + y;
    }

    /** The "outside the shape but touching it" test used twice by the original (arx @362-555, @1007-1200). */
    private static boolean isEdge(final boolean[] shape, final int x, final int z, final int y) {
        return !shape[index(x, z, y)]
                && (x < 15 && shape[index(x + 1, z, y)]
                || x > 0 && shape[index(x - 1, z, y)]
                || z < 15 && shape[index(x, z + 1, y)]
                || z > 0 && shape[index(x, z - 1, y)]
                || y < 7 && shape[index(x, z, y + 1)]
                || y > 0 && shape[index(x, z, y - 1)]);
    }

    /** {@code arx.a(World, Random, int, int, int)} ({@code func_76484_a}). */
    @Override
    public boolean generate(final LegacyWorld world, final Random rand, int x, int y, int z) {
        x -= 8;
        z -= 8;
        while (y > 5 && world.isAirBlock(x, y, z)) {
            --y;
        }
        if (y <= 4) {
            return false;
        }
        y -= 4;
        final boolean[] shape = new boolean[2048];
        final int blobs = rand.nextInt(4) + 4;
        for (int i = 0; i < blobs; ++i) {
            final double sizeX = rand.nextDouble() * 6.0 + 3.0;
            final double sizeY = rand.nextDouble() * 4.0 + 2.0;
            final double sizeZ = rand.nextDouble() * 6.0 + 3.0;
            final double centerX = rand.nextDouble() * (16.0 - sizeX - 2.0) + 1.0 + sizeX / 2.0;
            final double centerY = rand.nextDouble() * (8.0 - sizeY - 4.0) + 2.0 + sizeY / 2.0;
            final double centerZ = rand.nextDouble() * (16.0 - sizeZ - 2.0) + 1.0 + sizeZ / 2.0;
            for (int bx = 1; bx < 15; ++bx) {
                for (int bz = 1; bz < 15; ++bz) {
                    for (int by = 1; by < 7; ++by) {
                        final double dx = ((double) bx - centerX) / (sizeX / 2.0);
                        final double dy = ((double) by - centerY) / (sizeY / 2.0);
                        final double dz = ((double) bz - centerZ) / (sizeZ / 2.0);
                        final double distance = dx * dx + dy * dy + dz * dz;
                        if (distance < 1.0) {
                            shape[index(bx, bz, by)] = true;
                        }
                    }
                }
            }
        }
        for (int bx = 0; bx < 16; ++bx) {
            for (int bz = 0; bz < 16; ++bz) {
                for (int by = 0; by < 8; ++by) {
                    if (isEdge(shape, bx, bz, by)) {
                        final BlockState current = world.getBlock(x + bx, y + by, z + bz);
                        if (by >= 4 && LegacyWorld.isLiquid(current)) {
                            return false;
                        }
                        if (by < 4 && !LegacyWorld.isSolid(current) && current != this.block) {
                            return false;
                        }
                    }
                }
            }
        }
        for (int bx = 0; bx < 16; ++bx) {
            for (int bz = 0; bz < 16; ++bz) {
                for (int by = 0; by < 8; ++by) {
                    if (shape[index(bx, bz, by)]) {
                        world.setBlock(x + bx, y + by, z + bz, by >= 4 ? AIR : this.block);
                    }
                }
            }
        }
        for (int bx = 0; bx < 16; ++bx) {
            for (int bz = 0; bz < 16; ++bz) {
                for (int by = 4; by < 8; ++by) {
                    if (shape[index(bx, bz, by)] && world.getBlock(x + bx, y + by - 1, z + bz).is(Blocks.DIRT)
                            && world.hasSkyLight(x + bx, y + by, z + bz)) {
                        if (world.getBiomeTopBlock(x + bx, z + bz) == MYCELIUM) {
                            world.setBlock(x + bx, y + by - 1, z + bz, MYCELIUM);
                        } else {
                            world.setBlock(x + bx, y + by - 1, z + bz, GRASS);
                        }
                    }
                }
            }
        }
        if (this.block.is(Blocks.LAVA)) {
            for (int bx = 0; bx < 16; ++bx) {
                for (int bz = 0; bz < 16; ++bz) {
                    for (int by = 0; by < 8; ++by) {
                        if (isEdge(shape, bx, bz, by) && (by < 4 || rand.nextInt(2) != 0)
                                && LegacyWorld.isSolid(world.getBlock(x + bx, y + by, z + bz))) {
                            world.setBlock(x + bx, y + by, z + bz, STONE);
                        }
                    }
                }
            }
        }
        if (this.block.is(Blocks.WATER)) {
            for (int bx = 0; bx < 16; ++bx) {
                for (int bz = 0; bz < 16; ++bz) {
                    final int by = 4;
                    if (world.isBlockFreezable(x + bx, y + by, z + bz)) {
                        world.setBlock(x + bx, y + by, z + bz, ICE);
                    }
                }
            }
        }
        return true;
    }
}
