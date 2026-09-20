package com.swbr.orespawn.world.gen;

import com.swbr.orespawn.config.stats.OreStats;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Partial port of {@code OreSpawnWorld} for W05: {@code generateRuby} (OreSpawnWorld.java:316-337) and the ore
 * block of the Mining branch of {@code generate} (OreSpawnWorld.java:43-64), both of which belong to the
 * Mining dimension's ore generation. Everything else in that branch - the structure roll, the generic
 * dungeon, lava and water, ants, mosquitos, veggies, rocks (:65-98) - stays with W12, which owns
 * {@code OreSpawnWorld}; W12 may absorb this file and call {@link #generateRuby} for the overworld too.
 *
 * <p>Writes and reads go through {@link LegacyWorld} and draw from a {@link Random}, like every other
 * populate-time helper in this package (DECISIONS R21).
 */
public final class OreSpawnWorldOres {

    private OreSpawnWorldOres() {
    }

    /**
     * The random {@code OreSpawnWorld.generate} received: FML 1.7.10 {@code GameRegistry.generateWorld} seeds a
     * {@code Random} per chunk from the world seed and hands the same seed to every {@code IWorldGenerator}.
     *
     * <p>PORT: the formula below (including the operator-precedence quirk {@code >> 2 + 1L}, which is a shift
     * by 3) is FML's, reproduced from its 1.7.10 source; no FML jar lies in {@code reference/}, so it is not
     * checked against bytecode here. DECISIONS R18 only requires world seed plus chunk position, which holds
     * either way.
     */
    public static Random fmlChunkRandom(final long worldSeed, final int chunkX, final int chunkZ) {
        final Random fmlRandom = new Random(worldSeed);
        final long xSeed = fmlRandom.nextLong() >> (2 + 1L);
        final long zSeed = fmlRandom.nextLong() >> (2 + 1L);
        final long chunkSeed = (xSeed * (long) chunkX + zSeed * (long) chunkZ) ^ worldSeed;
        fmlRandom.setSeed(chunkSeed);
        return fmlRandom;
    }

    /**
     * OreSpawnWorld.java:44-64: Ruby ore once, and with {@code LessOre == 0} twice more plus 45 lapis veins of
     * size 7 and 25 of size 4, each only below Y 50.
     *
     * @param chunkX chunk x (not block x)
     * @param chunkZ chunk z
     */
    public static void generateMining(final LegacyWorld world, final Random random, final int chunkX,
                                      final int chunkZ) {
        generateRuby(world, random, chunkX * 16, chunkZ * 16);
        if (TweakStats.LessOre() == 0) {
            generateRuby(world, random, chunkX * 16, chunkZ * 16);
            generateRuby(world, random, chunkX * 16, chunkZ * 16);
            final WorldGenMinable lapis7 = new WorldGenMinable(Blocks.LAPIS_ORE.defaultBlockState(), 7);
            for (int i = 0; i < 45; ++i) {
                final int randPosX = chunkX * 16 + random.nextInt(16);
                final int randPosY = random.nextInt(128);
                final int randPosZ = chunkZ * 16 + random.nextInt(16);
                if (randPosY < 50) {
                    lapis7.generate(world, random, randPosX, randPosY, randPosZ);
                }
            }
            final WorldGenMinable lapis4 = new WorldGenMinable(Blocks.LAPIS_ORE.defaultBlockState(), 4);
            for (int i = 0; i < 25; ++i) {
                final int randPosX = chunkX * 16 + random.nextInt(16);
                final int randPosY = random.nextInt(128);
                final int randPosZ = chunkZ * 16 + random.nextInt(16);
                if (randPosY < 50) {
                    lapis4.generate(world, random, randPosX, randPosY, randPosZ);
                }
            }
        }
    }

    /**
     * OreSpawnWorld.java:316-337: Ruby ore replaces the stone directly under the first lava found when
     * scanning down from a random height to Y 6.
     *
     * @param chunkX block x of the chunk corner
     * @param chunkZ block z of the chunk corner
     */
    public static void generateRuby(final LegacyWorld world, final Random random, final int chunkX,
                                    final int chunkZ) {
        final OreStats ruby = OreStats.Ruby_stats();
        if (ruby.rate() <= 0) {
            return;
        }
        final BlockState rubyOre = ModBlocks.ORERUBY.get().defaultBlockState();
        for (int patchy = ruby.rate() + random.nextInt(7), i = 0; i < patchy; ++i) {
            final int randPosX = 3 + chunkX + random.nextInt(10);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 3 + chunkZ + random.nextInt(10);
            if (randPosY <= ruby.maxdepth() && randPosY >= ruby.mindepth()) {
                for (int m = randPosY; m > 5; --m) {
                    BlockState bid = world.getBlock(randPosX, m, randPosZ);
                    // Blocks.lava || Blocks.flowing_lava: both are minecraft:lava in 1.21.1.
                    if (bid.is(Blocks.LAVA)) {
                        bid = world.getBlock(randPosX, m - 1, randPosZ);
                        if (bid.is(Blocks.STONE)) {
                            FastBlocks.setBlockFast(world.level(), randPosX, m - 1, randPosZ, rubyOre, 2);
                            break;
                        }
                    }
                }
            }
        }
    }
}
