package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenMinable} ({@code ase}, client-1.7.10.jar): the world-writing ore vein
 * of the {@link BiomeDecorator} ores, the Extreme Hills silverfish pass ({@link BiomeGenHills#decorate}) and the
 * Mining lapis of {@link OreSpawnWorldOres}. A line of {@code numberOfBlocks + 1} ellipsoids between two points
 * spread by {@code numberOfBlocks / 8} around {@code (x + 8, z + 8)}; only the target block (default stone) is
 * replaced, written with flag 2 like {@code setBlock(x, y, z, block, 0, 2)}. Same algorithm as
 * {@link ChunkOreGenerator#generateBlockOre}, but through the world, so veins cross chunk borders.
 *
 * <p>The 1.21.1 {@code OreFeature} is not a stand-in: it clamps differently and skips air-exposed positions the
 * original never tested.
 */
public class WorldGenMinable extends WorldGenerator {

    private final BlockState minableBlock;
    private final int numberOfBlocks;
    private final Block target;

    /** {@code ase(Block, int)}: target {@code Blocks.stone}. */
    public WorldGenMinable(final BlockState minableBlock, final int numberOfBlocks) {
        this(minableBlock, numberOfBlocks, Blocks.STONE);
    }

    /** {@code ase(Block, int, Block)}. */
    public WorldGenMinable(final BlockState minableBlock, final int numberOfBlocks, final Block target) {
        this.minableBlock = minableBlock;
        this.numberOfBlocks = numberOfBlocks;
        this.target = target;
    }

    /** {@code ase.a(World, Random, int, int, int)} ({@code func_76484_a}). */
    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        final float angle = rand.nextFloat() * (float) Math.PI;
        final double x1 = (double) ((float) (x + 8) + Mth.sin(angle) * (float) this.numberOfBlocks / 8.0F);
        final double x2 = (double) ((float) (x + 8) - Mth.sin(angle) * (float) this.numberOfBlocks / 8.0F);
        final double z1 = (double) ((float) (z + 8) + Mth.cos(angle) * (float) this.numberOfBlocks / 8.0F);
        final double z2 = (double) ((float) (z + 8) - Mth.cos(angle) * (float) this.numberOfBlocks / 8.0F);
        final double y1 = (double) (y + rand.nextInt(3) - 2);
        final double y2 = (double) (y + rand.nextInt(3) - 2);
        for (int l = 0; l <= this.numberOfBlocks; ++l) {
            final double cx = x1 + (x2 - x1) * (double) l / (double) this.numberOfBlocks;
            final double cy = y1 + (y2 - y1) * (double) l / (double) this.numberOfBlocks;
            final double cz = z1 + (z2 - z1) * (double) l / (double) this.numberOfBlocks;
            final double size = rand.nextDouble() * (double) this.numberOfBlocks / 16.0;
            final double rxz = (double) (Mth.sin((float) l * (float) Math.PI / (float) this.numberOfBlocks) + 1.0F) * size + 1.0;
            final double ry = (double) (Mth.sin((float) l * (float) Math.PI / (float) this.numberOfBlocks) + 1.0F) * size + 1.0;
            final int minX = Mth.floor(cx - rxz / 2.0);
            final int minY = Mth.floor(cy - ry / 2.0);
            final int minZ = Mth.floor(cz - rxz / 2.0);
            final int maxX = Mth.floor(cx + rxz / 2.0);
            final int maxY = Mth.floor(cy + ry / 2.0);
            final int maxZ = Mth.floor(cz + rxz / 2.0);
            for (int bx = minX; bx <= maxX; ++bx) {
                final double dx = ((double) bx + 0.5 - cx) / (rxz / 2.0);
                if (dx * dx < 1.0) {
                    for (int by = minY; by <= maxY; ++by) {
                        final double dy = ((double) by + 0.5 - cy) / (ry / 2.0);
                        if (dx * dx + dy * dy < 1.0) {
                            for (int bz = minZ; bz <= maxZ; ++bz) {
                                final double dz = ((double) bz + 0.5 - cz) / (rxz / 2.0);
                                if (dx * dx + dy * dy + dz * dz < 1.0 && world.getBlock(bx, by, bz).is(this.target)) {
                                    world.setBlock(bx, by, bz, this.minableBlock);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
