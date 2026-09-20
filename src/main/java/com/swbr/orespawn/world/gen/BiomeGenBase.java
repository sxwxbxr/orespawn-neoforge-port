package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the parts of the 1.7.10 vanilla {@code BiomeGenBase} ({@code ahu}, client-1.7.10.jar) that the ports
 * call: {@code genBiomeTerrain}, the surface pass every noise-terrain dimension runs per column, and the three
 * picks {@link BiomeDecorator} asks the biome for. Operates on the 1.7.10 block array
 * ({@code (x * 16 + z) * 256 + y}, {@code null} = air, see {@link MapGenBase}).
 *
 * <p><b>The column is transposed, and that is original behaviour.</b> The method indexes
 * {@code (z & 15) * 16 + (x & 15)}, while the chunk array is {@code x * 16 + z}
 * ({@code apx.<init>} @64-81). A caller that passes {@code (chunkX * 16 + k, chunkZ * 16 + l)} therefore
 * writes into array column {@code (l, k)}; 1.7.10 worlds show this as surface noise mirrored along the
 * chunk diagonal. Keep it.
 */
public final class BiomeGenBase {

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState ICE = Blocks.ICE.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState SANDSTONE = Blocks.SANDSTONE.defaultBlockState();

    private BiomeGenBase() {
    }

    /**
     * {@code ahu.a(Random)} ({@code func_150567_a}): the big oak ({@code ahu.aA = new ard(false)}) in one call of
     * ten, otherwise the small oak ({@code ahu.az = new asq(false)}).
     *
     * <p>PORT: the original handed out the two generator objects of the biome singleton, shared by every world
     * and every chunk. {@link WorldGenBigTree} stores its rolled {@code heightLimit} in that object and never
     * clears it, so after the first big oak of a biome every later one had that height - fixed by whichever
     * chunk the server happened to decorate first. Chunk generation must be deterministic and runs on several
     * worker threads (DECISIONS R18 case 3), so every call builds a fresh generator and each big oak rolls its
     * own height. The random numbers drawn from the decorator random are the same.
     */
    public static WorldGenAbstractTree getRandomWorldGenForTrees(final Random rand) {
        return rand.nextInt(10) == 0 ? new WorldGenBigTree(false) : new WorldGenTrees(false);
    }

    /**
     * {@code ahu.a(Random, int, int, int)} ({@code func_150572_a}): dandelion in two calls of three, poppy
     * otherwise, both metadata 0. Neither ported biome overrides it.
     */
    public static BlockState pickFlower(final Random rand) {
        return rand.nextInt(3) > 0 ? Blocks.DANDELION.defaultBlockState() : Blocks.POPPY.defaultBlockState();
    }

    /**
     * {@code ahu.b(Random)} ({@code getRandomWorldGenForGrass}): {@code new asp(tallgrass, 1)}, no random draw;
     * {@code tallgrass} metadata 1 is 1.21.1 {@code short_grass}. Neither ported biome overrides it.
     */
    public static WorldGenerator getRandomWorldGenForGrass(final Random rand) {
        return new WorldGenTallGrass(Blocks.SHORT_GRASS.defaultBlockState());
    }

    /**
     * {@code ahu.b(...)}, "genBiomeTerrain".
     *
     * @param rand the chunk provider's random ({@code this.rand})
     * @param blocks the chunk block array
     * @param x block x as passed by {@code replaceBlocksForBiome}
     * @param z block z as passed by {@code replaceBlocksForBiome}
     * @param noise the surface depth noise of this call
     * @param topBlock {@code ahu.ai} at the time of the call
     * @param fillerBlock {@code ahu.ak} at the time of the call
     * @param temperature the biome temperature {@code ahu.ao}
     */
    public static void genBiomeTerrain(final RandomSource rand, final BlockState[] blocks, final int x, final int z,
                                       final double noise, final BlockState topBlock, final BlockState fillerBlock,
                                       final float temperature) {
        BlockState block = topBlock;
        BlockState block1 = fillerBlock;
        int k = -1;
        final int l = (int) (noise / 3.0 + 3.0 + rand.nextDouble() * 0.25);
        final int i1 = x & 15;
        final int j1 = z & 15;
        final int k1 = blocks.length / 256;
        for (int l1 = 255; l1 >= 0; --l1) {
            final int i2 = (j1 * 16 + i1) * k1 + l1;
            if (l1 <= 0 + rand.nextInt(5)) {
                blocks[i2] = BEDROCK;
            } else {
                final BlockState block2 = blocks[i2];
                if (block2 == null || block2.isAir()) {
                    k = -1;
                } else if (block2 == STONE) {
                    if (k == -1) {
                        if (l <= 0) {
                            block = null;
                            block1 = STONE;
                        } else if (l1 >= 59 && l1 <= 64) {
                            block = topBlock;
                            block1 = fillerBlock;
                        }
                        if (l1 < 63 && (block == null || block.isAir())) {
                            // getFloatTemperature(x, y, z) (ahu @659-698) only departs from the base
                            // temperature above y 64; this branch is guarded by y < 63.
                            if (temperature < 0.15F) {
                                block = ICE;
                            } else {
                                block = WATER;
                            }
                        }
                        k = l;
                        if (l1 >= 62) {
                            blocks[i2] = block;
                        } else if (l1 < 56 - l) {
                            block = null;
                            block1 = STONE;
                            blocks[i2] = GRAVEL;
                        } else {
                            blocks[i2] = block1;
                        }
                    } else if (k > 0) {
                        --k;
                        blocks[i2] = block1;
                        if (k == 0 && block1 == SAND) {
                            k = rand.nextInt(4) + Math.max(0, l1 - 63);
                            block1 = SANDSTONE;
                        }
                    }
                }
            }
        }
    }
}
