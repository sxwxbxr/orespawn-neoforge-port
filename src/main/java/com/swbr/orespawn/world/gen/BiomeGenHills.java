package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the parts of the 1.7.10 vanilla {@code BiomeGenHills} ({@code aid}, client-1.7.10.jar) that the
 * Mining dimension runs: {@code genTerrainBlocks}, the tree pick and {@code decorate}.
 * WorldProviderOreSpawn2.java:21 uses exactly {@code BiomeGenBase.extremeHills}, built as
 * {@code new aid(3, false)} ({@code ahu.<clinit>}), so the variant field {@code aH} is {@code aE = 0}: neither
 * the gravel variant ({@code aG = 2}) nor the one without stone tops ({@code aF = 1}), and the decorator keeps
 * {@code treesPerChunk = 0}.
 */
public final class BiomeGenHills {

    /** {@code aid.aE}: plain Extreme Hills. */
    public static final int TYPE_NORMAL = 0;
    /** {@code aid.aF}: the tree variant (constructor flag {@code true}). */
    public static final int TYPE_TREES = 1;
    /** {@code aid.aG}: the mutated "M" variant. */
    public static final int TYPE_MUTATED = 2;

    private static final BlockState EMERALD_ORE = Blocks.EMERALD_ORE.defaultBlockState();

    /** {@code aid.aC}: {@code WorldGenMinable(monster_egg, 8)}, meta 0 = infested stone, target stone. */
    private static final WorldGenMinable SILVERFISH_GEN =
            new WorldGenMinable(Blocks.INFESTED_STONE.defaultBlockState(), 8);

    private BiomeGenHills() {
    }

    /**
     * {@code aid.a(World, Random, Block[], byte[], int, int, double)} ({@code func_150573_a},
     * "genTerrainBlocks", @161-216): picks the top and filler block for this column, then runs
     * {@link BiomeGenBase#genBiomeTerrain}.
     *
     * @return the top block this call left in the biome's {@code topBlock} field. The field lived on the
     *         shared biome object, so the cave and ravine carvers of the same chunk read the value of the
     *         <em>last</em> column; callers keep it for them.
     */
    public static BlockState genTerrainBlocks(final int variant, final RandomSource rand, final BlockState[] blocks,
                                              final int x, final int z, final double noise, final float temperature) {
        BlockState topBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState fillerBlock = Blocks.DIRT.defaultBlockState();
        if ((noise < -1.0 || noise > 2.0) && variant == TYPE_MUTATED) {
            topBlock = Blocks.GRAVEL.defaultBlockState();
            fillerBlock = Blocks.GRAVEL.defaultBlockState();
        } else if (noise > 1.0 && variant != TYPE_TREES) {
            topBlock = Blocks.STONE.defaultBlockState();
            fillerBlock = Blocks.STONE.defaultBlockState();
        }
        BiomeGenBase.genBiomeTerrain(rand, blocks, x, z, noise, topBlock, fillerBlock, temperature);
        return topBlock;
    }

    /**
     * {@code aid.a(Random)} ({@code func_150567_a}): the spruce ({@code aid.aD = new asn(false)}) when
     * {@code nextInt(3) > 0}, otherwise {@link BiomeGenBase#getRandomWorldGenForTrees}. PORT: a fresh generator per
     * call, see there.
     */
    public static WorldGenAbstractTree getRandomWorldGenForTrees(final Random rand) {
        return rand.nextInt(3) > 0 ? new WorldGenTaiga2(false) : BiomeGenBase.getRandomWorldGenForTrees(rand);
    }

    /**
     * {@code aid.a(World, Random, int, int)} ({@code func_76728_a}): {@code super.decorate}, i.e. the whole
     * {@link BiomeDecorator#extremeHills} decorator, then 3..8 emerald ores in stone at Y 4..31 and seven
     * silverfish veins at Y 0..63.
     *
     * @param chunkX block x of the chunk corner (not +8)
     * @param chunkZ block z of the chunk corner
     */
    public static void decorate(final LegacyWorld world, final Random rand, final int chunkX, final int chunkZ) {
        BiomeDecorator.extremeHills().decorateChunk(world, rand, chunkX, chunkZ);
        final int k = 3 + rand.nextInt(6);
        for (int l = 0; l < k; ++l) {
            final int i1 = chunkX + rand.nextInt(16);
            final int j1 = rand.nextInt(28) + 4;
            final int k1 = chunkZ + rand.nextInt(16);
            if (world.getBlock(i1, j1, k1).is(Blocks.STONE)) {
                world.setBlock(i1, j1, k1, EMERALD_ORE);
            }
        }
        for (int l = 0; l < 7; ++l) {
            final int i1 = chunkX + rand.nextInt(16);
            final int j1 = rand.nextInt(64);
            final int k1 = chunkZ + rand.nextInt(16);
            SILVERFISH_GEN.generate(world, rand, i1, j1, k1);
        }
    }
}
