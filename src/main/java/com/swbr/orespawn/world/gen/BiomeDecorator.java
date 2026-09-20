package com.swbr.orespawn.world.gen;

import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code BiomeDecorator} ({@code aia}, client-1.7.10.jar; Vineflower 1.10.1 output,
 * checked against {@code javap -c}) for the biomes whose {@code decorate} the OreSpawn dimensions run. The only
 * copy of this class in the port (DECISIONS R21).
 *
 * <table>
 * <tr><th>biome</th><th>decorated by</th><th>counts other than {@code aia.<init>}</th></tr>
 * <tr><td>{@code BiomeGenUtopianPlains}</td><td>{@code ChunkProviderOreSpawn.populate} (ChunkProviderOreSpawn.java:277),
 * {@code ChunkProviderOreSpawn3.populate} (ChunkProviderOreSpawn3.java:308)</td><td>trees -999, flowers 4, grass 6
 * (BiomeGenUtopianPlains.java:57-59)</td></tr>
 * <tr><td>{@code BiomeGenBase.extremeHills}, {@code new aid(3, false)} ({@code ahu.<clinit>})</td>
 * <td>{@code ChunkProviderOreSpawn2.populate} (ChunkProviderOreSpawn2.java:312) through {@link BiomeGenHills#decorate}</td>
 * <td>none: only the constructor flag {@code true} sets trees to 3 ({@code aid.<init>})</td></tr>
 * </table>
 *
 * <p>{@code aia.<init>} sets flowers ({@code y}) 2, grass ({@code z}) 1, gravel disks ({@code E}) 1, sand disks
 * ({@code F}) 3, clay disks ({@code G}) 1, {@code generateLakes} ({@code I}) true, everything else 0. The counts
 * that stay 0 in both ported biomes - water lilies ({@code w}), dead bushes ({@code A}), mushrooms ({@code B}),
 * reeds ({@code C}), cacti ({@code D}), huge mushrooms ({@code H}) - run no iteration and draw no random number,
 * so their generators are not ported. The tree loop always draws its {@code nextInt(10)}; Utopia never reaches a
 * tree with -999, Extreme Hills reaches one in about one chunk of ten.
 *
 * <p>Reach, for the 1.21.1 write radius: every step starts at {@code chunk + 8 .. chunk + 23} except the ores
 * ({@code chunk + 0 .. 15}, vein centre {@code + 8}); the widest spread is 8 blocks (flowers, grass, mushrooms,
 * pumpkins) and a big oak's branches plus leaves (5 + 2). Everything stays inside the populated chunk and its +x/+z
 * neighbours.
 *
 * <p>PORT: {@code decorateChunk} threw "Already decorating!!" when re-entered; a decorator here is created per
 * populate call and never shared, so the fields are only reset in a {@code finally}.
 */
public class BiomeDecorator {

    protected LegacyWorld currentWorld;
    protected Random randomGenerator;
    protected int chunk_X;
    protected int chunk_Z;

    /** {@code aia.e}. */
    protected final WorldGenerator clayGen = new WorldGenClay(4);
    /** {@code aia.f}. */
    protected final WorldGenerator sandGen = new WorldGenSand(Blocks.SAND.defaultBlockState(), 7);
    /** {@code aia.g}. */
    protected final WorldGenerator gravelAsSandGen = new WorldGenSand(Blocks.GRAVEL.defaultBlockState(), 6);
    /** {@code aia.h} .. {@code aia.o}: the vanilla ores. */
    protected final WorldGenerator dirtGen = new WorldGenMinable(Blocks.DIRT.defaultBlockState(), 32);
    protected final WorldGenerator gravelGen = new WorldGenMinable(Blocks.GRAVEL.defaultBlockState(), 32);
    protected final WorldGenerator coalGen = new WorldGenMinable(Blocks.COAL_ORE.defaultBlockState(), 16);
    protected final WorldGenerator ironGen = new WorldGenMinable(Blocks.IRON_ORE.defaultBlockState(), 8);
    protected final WorldGenerator goldGen = new WorldGenMinable(Blocks.GOLD_ORE.defaultBlockState(), 8);
    protected final WorldGenerator redstoneGen = new WorldGenMinable(Blocks.REDSTONE_ORE.defaultBlockState(), 7);
    protected final WorldGenerator diamondGen = new WorldGenMinable(Blocks.DIAMOND_ORE.defaultBlockState(), 7);
    protected final WorldGenerator lapisGen = new WorldGenMinable(Blocks.LAPIS_ORE.defaultBlockState(), 6);
    /** {@code aia.p}. */
    protected final WorldGenFlowers yellowFlowerGen = new WorldGenFlowers(Blocks.DANDELION.defaultBlockState());
    /** {@code aia.q}, {@code aia.r}. */
    protected final WorldGenerator mushroomBrownGen = new WorldGenFlowers(Blocks.BROWN_MUSHROOM.defaultBlockState());
    protected final WorldGenerator mushroomRedGen = new WorldGenFlowers(Blocks.RED_MUSHROOM.defaultBlockState());
    /** {@code aia.t}. */
    protected final WorldGenerator reedGen = new WorldGenReed();

    /** {@code aia.x}. */
    protected final int treesPerChunk;
    /** {@code aia.y}. */
    protected final int flowersPerChunk;
    /** {@code aia.z}. */
    protected final int grassPerChunk;
    /** {@code aia.E}: gravel disks. */
    protected final int sandPerChunk = 1;
    /** {@code aia.F}: sand disks. */
    protected final int sandPerChunk2 = 3;
    /** {@code aia.G}. */
    protected final int clayPerChunk = 1;
    /** {@code aia.I}. */
    protected final boolean generateLakes = true;
    /** {@code biome.a(Random)} ({@code BiomeGenBase.func_150567_a}) of the decorated biome. */
    private final Function<Random, WorldGenAbstractTree> treeGenerator;

    protected BiomeDecorator(final int treesPerChunk, final int flowersPerChunk, final int grassPerChunk,
                             final Function<Random, WorldGenAbstractTree> treeGenerator) {
        this.treesPerChunk = treesPerChunk;
        this.flowersPerChunk = flowersPerChunk;
        this.grassPerChunk = grassPerChunk;
        this.treeGenerator = treeGenerator;
    }

    /** The decorator of {@code BiomeGenUtopianPlains} (BiomeGenUtopianPlains.java:57-59); the biome keeps the base tree pick. */
    public static BiomeDecorator utopianPlains() {
        return new BiomeDecorator(-999, 4, 6, BiomeGenBase::getRandomWorldGenForTrees);
    }

    /** The decorator of {@code BiomeGenBase.extremeHills}: the {@code aia.<init>} counts and the Extreme Hills tree pick. */
    public static BiomeDecorator extremeHills() {
        return new BiomeDecorator(0, 2, 1, BiomeGenHills::getRandomWorldGenForTrees);
    }

    /** {@code aia.a(World, Random, BiomeGenBase, int, int)} ({@code decorateChunk}); block coordinates of the chunk corner. */
    public void decorateChunk(final LegacyWorld world, final Random rand, final int chunkX, final int chunkZ) {
        this.currentWorld = world;
        this.randomGenerator = rand;
        this.chunk_X = chunkX;
        this.chunk_Z = chunkZ;
        try {
            this.genDecorations();
        } finally {
            this.currentWorld = null;
            this.randomGenerator = null;
        }
    }

    /** {@code aia.a(BiomeGenBase)} ({@code genDecorations}), in bytecode order. */
    protected void genDecorations() {
        final LegacyWorld world = this.currentWorld;
        final Random rand = this.randomGenerator;
        this.generateOres();

        for (int i = 0; i < this.sandPerChunk2; ++i) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            this.sandGen.generate(world, rand, x, world.getTopSolidOrLiquidBlock(x, z), z);
        }
        for (int i = 0; i < this.clayPerChunk; ++i) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            this.clayGen.generate(world, rand, x, world.getTopSolidOrLiquidBlock(x, z), z);
        }
        for (int i = 0; i < this.sandPerChunk; ++i) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            this.gravelAsSandGen.generate(world, rand, x, world.getTopSolidOrLiquidBlock(x, z), z);
        }

        int trees = this.treesPerChunk;
        if (rand.nextInt(10) == 0) {
            ++trees;
        }
        for (int i = 0; i < trees; ++i) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            final int y = world.getHeightValue(x, z);
            final WorldGenAbstractTree tree = this.treeGenerator.apply(rand);
            tree.setScale(1.0, 1.0, 1.0);
            if (tree.generate(world, rand, x, y, z)) {
                tree.postGenerate(world, rand, x, y, z);
            }
        }

        // bigMushroomsPerChunk (aia.H) = 0

        for (int i = 0; i < this.flowersPerChunk; ++i) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            final int y = this.nextInt(world.getHeightValue(x, z) + 32);
            // biome.func_150572_a names a flower; its material is never air for dandelion or poppy, so the
            // "material != air" test before setFlower always passes.
            final BlockState flower = BiomeGenBase.pickFlower(rand);
            this.yellowFlowerGen.setFlower(flower);
            this.yellowFlowerGen.generate(world, rand, x, y, z);
        }

        for (int i = 0; i < this.grassPerChunk; ++i) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            final int y = this.nextInt(world.getHeightValue(x, z) * 2);
            BiomeGenBase.getRandomWorldGenForGrass(rand).generate(world, rand, x, y, z);
        }

        // deadBushPerChunk (aia.A) = 0, waterlilyPerChunk (aia.w) = 0, mushroomsPerChunk (aia.B) = 0

        if (rand.nextInt(4) == 0) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            final int y = this.nextInt(world.getHeightValue(x, z) * 2);
            this.mushroomBrownGen.generate(world, rand, x, y, z);
        }
        if (rand.nextInt(8) == 0) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            final int y = this.nextInt(world.getHeightValue(x, z) * 2);
            this.mushroomRedGen.generate(world, rand, x, y, z);
        }

        // reedsPerChunk (aia.C) = 0, then ten fixed tries
        for (int i = 0; i < 10; ++i) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            final int y = this.nextInt(world.getHeightValue(x, z) * 2);
            this.reedGen.generate(world, rand, x, y, z);
        }

        if (rand.nextInt(32) == 0) {
            final int x = this.chunk_X + rand.nextInt(16) + 8;
            final int z = this.chunk_Z + rand.nextInt(16) + 8;
            final int y = this.nextInt(world.getHeightValue(x, z) * 2);
            new WorldGenPumpkin().generate(world, rand, x, y, z);
        }

        // cactiPerChunk (aia.D) = 0

        if (this.generateLakes) {
            for (int i = 0; i < 50; ++i) {
                final int x = this.chunk_X + rand.nextInt(16) + 8;
                final int y = rand.nextInt(rand.nextInt(248) + 8);
                final int z = this.chunk_Z + rand.nextInt(16) + 8;
                new WorldGenLiquids(false).generate(world, rand, x, y, z);
            }
            for (int i = 0; i < 20; ++i) {
                final int x = this.chunk_X + rand.nextInt(16) + 8;
                final int y = rand.nextInt(rand.nextInt(rand.nextInt(240) + 8) + 8);
                final int z = this.chunk_Z + rand.nextInt(16) + 8;
                new WorldGenLiquids(true).generate(world, rand, x, y, z);
            }
        }
    }

    /**
     * Forge 1.7.10's guard in {@code BiomeDecorator.java.patch}: every height-based Y draw of {@code genDecorations}
     * goes through it, so an empty column (height 0, e.g. the Chaos void) draws nothing instead of throwing
     * {@code IllegalArgumentException} from {@code Random.nextInt(0)}. For heights above 0 no number changes.
     */
    private int nextInt(final int i) {
        if (i <= 1) {
            return 0;
        }
        return this.randomGenerator.nextInt(i);
    }

    /** {@code aia.a(int, WorldGenerator, int, int)} ({@code genStandardOre1}): Y uniform in [min, max). */
    protected void genStandardOre1(final int count, final WorldGenerator generator, final int minY, final int maxY) {
        final Random rand = this.randomGenerator;
        for (int i = 0; i < count; ++i) {
            final int x = this.chunk_X + rand.nextInt(16);
            final int y = rand.nextInt(maxY - minY) + minY;
            final int z = this.chunk_Z + rand.nextInt(16);
            generator.generate(this.currentWorld, rand, x, y, z);
        }
    }

    /** {@code aia.b(int, WorldGenerator, int, int)} ({@code genStandardOre2}): triangular height distribution. */
    protected void genStandardOre2(final int count, final WorldGenerator generator, final int center, final int spread) {
        final Random rand = this.randomGenerator;
        for (int i = 0; i < count; ++i) {
            final int x = this.chunk_X + rand.nextInt(16);
            final int y = rand.nextInt(spread) + rand.nextInt(spread) + (center - spread);
            final int z = this.chunk_Z + rand.nextInt(16);
            generator.generate(this.currentWorld, rand, x, y, z);
        }
    }

    /** {@code aia.a()} ({@code generateOres}). */
    protected void generateOres() {
        this.genStandardOre1(20, this.dirtGen, 0, 256);
        this.genStandardOre1(10, this.gravelGen, 0, 256);
        this.genStandardOre1(20, this.coalGen, 0, 128);
        this.genStandardOre1(20, this.ironGen, 0, 64);
        this.genStandardOre1(2, this.goldGen, 0, 32);
        this.genStandardOre1(8, this.redstoneGen, 0, 16);
        this.genStandardOre1(1, this.diamondGen, 0, 16);
        this.genStandardOre2(1, this.lapisGen, 16, 16);
    }
}
