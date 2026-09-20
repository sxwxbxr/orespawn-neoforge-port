package com.swbr.orespawn.world.gen;

import com.swbr.orespawn.config.stats.OreStats;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Port of {@code ChunkOreGenerator} (ChunkOreGenerator.java:10-658; verhalten/world-02.md "ChunkOreGenerator"):
 * the ore pass that the Utopia, Mining, VillageMania and Chaos chunk providers run on the raw chunk right
 * after terrain, surface, caves and ravines.
 *
 * <p>Shared by the four dimension generators of W05. The original had one instance
 * ({@code OreSpawnMain.Chunker}, OreSpawnMain.java:5447) and no state, so the port is static. Call it from the
 * generator's chunk-local phase (the port of {@code provideChunk}), with the random the original passed:
 * <ul>
 * <li>Utopia (ChunkProviderOreSpawn.java:170), VillageMania (ChunkProviderOreSpawn3.java:171): once,
 * {@code this.rand} after the surface pass;</li>
 * <li>Mining (ChunkProviderOreSpawn2.java:172-176): once, and twice more with {@code LessOre == 0};</li>
 * <li>Chaos (ChunkProviderOreSpawn6.java:194): once, {@code worldObj.rand} in the original - a chunk-seeded
 * random in the port (DECISIONS R18).</li>
 * </ul>
 *
 * <p><b>The chunk clip is original behaviour.</b> Reads and writes go through {@link FastBlocks#getBlockInChunk}
 * and {@link FastBlocks#setBlockInChunk} (the ports of {@code getBlockIDInChunk} and
 * {@code setBlockIDWithMetadataInChunk}), which answer air for and drop everything outside the chunk. With
 * the vein centre at {@code 3 + chunk + nextInt(10) + 8} = local 11..20, part of most veins is cut at the +X
 * and +Z border; do not "fix" that with a world write. Only {@code minecraft:stone} is replaced (:647), Y is
 * rolled over 0..127 and veins outside the configured band are dropped (absolute Y, DECISIONS R18).
 */
public final class ChunkOreGenerator {

    /** The seven rare spawn ores of the {@code nextInt(104) < 7} branch, in switch order (:29-58). */
    private static final String[] RARE_SPAWN_ORES = {
            "orebrutalfly", "orenastysaurus", "orepointysaurus", "orecricket", "orefrog", "orespiderdriver",
            "orecrab"};

    /**
     * The 98 spawn ores of the {@code nextInt(98)} branch, in switch order (:62-457). Registry ids from the
     * {@code setBlockName} of each {@code OreSpawnMain.My*SpawnBlock} field (R2: lower-cased, so
     * {@code oreMOTHRA} is {@code oremothra} and {@code MyPitchBlackSpawnBlock} is {@code orenightmare}).
     */
    private static final String[] SPAWN_ORES = {
            "orespider", "orebat", "orecow", "orepig", "oresquid", "orechicken", "orecreeper", "oreskeleton",
            "orezombie", "oreslime", "oreghast", "orezombiepigman", "oreenderman", "orecavespider",
            "oresilverfish", "oremagmacube", "orewitch", "oresheep", "orewolf", "oremooshroom", "oreocelot",
            "oreblaze", "orewitherskeleton", "oreenderdragon", "oresnowgolem", "oreirongolem", "orewitherboss",
            "oregirlfriend", "oreredcow", "oregoldcow", "oreenchantedcow", "oremothra", "orealosaurus",
            "orecryolophosaurus", "orecamarasaurus", "orevelocityraptor", "orehydrolisc", "orebasilisc",
            "oredragonfly", "oreemperorscorpion", "orescorpion", "orecavefisher", "orespyro", "orebaryonyx",
            "oregammametroid", "orecockateil", "orekyuubi", "orealien", "oreattacksquid", "orewaterdragon",
            "orekraken", "orelizard", "orecephadrome", "oredragon", "orebee", "orehorse", "oretrooper",
            "orespit", "orestink", "oreostrich", "oregazelle", "orechipmunk", "orecreepinghorror",
            "oreterribleterror", "orecliffracer", "oretriffid", "orenightmare", "orelurkingterror",
            "oregodzillapart", "oregodzilla", "oresmallworm", "oremediumworm", "orelargeworm", "orecassowary",
            "orecloudshark", "oregoldfish", "oreleafmonster", "oretshirt", "oreenderknight", "oreenderreaper",
            "orebeaver", "oretrex", "orehercules", "oremantis", "orestinky", "oreboyfriend", "orethekingpart",
            "oreeasterbunny", "orecaterkiller", "oremolenoid", "oreseamonster", "oreseaviper", "oreleon",
            "orehammerhead", "orerubberducky", "orevillager", "orecriminal", "orethequeenpart"};

    private ChunkOreGenerator() {
    }

    /**
     * {@code generateOresInChunk(World, Random, int, int, Chunk)} (:12-615). The unused {@code World}
     * parameter of the original is dropped.
     *
     * @param random the chunk provider's random, consumed in the original order
     * @param chunkX block x of the chunk corner ({@code chunkX * 16} in the callers)
     * @param chunkZ block z of the chunk corner
     * @param chunk the chunk being generated
     */
    public static void generateOresInChunk(final RandomSource random, final int chunkX, final int chunkZ,
                                           final ChunkAccess chunk) {
        final int lessOre = TweakStats.LessOre();
        final OreStats spawnOres = OreStats.SpawnOres_stats();
        if (spawnOres.rate() > 0) {
            int patchy = spawnOres.rate() + random.nextInt(30);
            if (random.nextInt(20) == 0) {
                patchy += 30;
            }
            if (lessOre != 0) {
                patchy /= 3;
            }
            for (int i = 0; i < patchy; ++i) {
                final int randPosX = 3 + chunkX + random.nextInt(10);
                final int randPosY = random.nextInt(128);
                final int randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY <= spawnOres.maxdepth() && randPosY >= spawnOres.mindepth()) {
                    if (random.nextInt(104) < 7) {
                        final int j = random.nextInt(7);
                        generateBlockOre(random, randPosX, randPosY, randPosZ, chunk, spawnOre(RARE_SPAWN_ORES[j]),
                                spawnOres.clumpsize());
                    } else {
                        final int j = random.nextInt(98);
                        generateBlockOre(random, randPosX, randPosY, randPosZ, chunk, spawnOre(SPAWN_ORES[j]),
                                spawnOres.clumpsize());
                    }
                }
            }
        }
        final OreStats uranium = OreStats.Uranium_stats();
        if (uranium.rate() > 0) {
            int patchy = uranium.rate() + random.nextInt(9);
            if (lessOre != 0) {
                patchy /= 3;
            }
            banded(random, chunkX, chunkZ, chunk, patchy, uranium, ModBlocks.OREURANIUM.get());
        }
        final OreStats titanium = OreStats.Titanium_stats();
        if (titanium.rate() > 0) {
            int patchy = titanium.rate() + random.nextInt(9);
            if (lessOre != 0) {
                patchy /= 3;
            }
            banded(random, chunkX, chunkZ, chunk, patchy, titanium, ModBlocks.ORETITANIUM.get());
        }
        final OreStats amethyst = OreStats.Amethyst_stats();
        if (amethyst.rate() > 0) {
            int patchy = amethyst.rate() + random.nextInt(12);
            if (lessOre != 0) {
                patchy /= 3;
            }
            banded(random, chunkX, chunkZ, chunk, patchy, amethyst, ModBlocks.OREAMETHYST.get());
        }
        final OreStats salt = OreStats.Salt_stats();
        if (salt.rate() > 0) {
            int patchy = salt.rate() + random.nextInt(9);
            if (lessOre != 0) {
                patchy /= 3;
            }
            banded(random, chunkX, chunkZ, chunk, patchy, salt, ModBlocks.ORESALT.get());
        }
        int patchy = 4 + random.nextInt(4);
        if (lessOre != 0) {
            patchy /= 2;
        }
        for (int i = 0; i < patchy; ++i) {
            final int randPosX = 3 + chunkX + random.nextInt(10);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 3 + chunkZ + random.nextInt(10);
            if (randPosY <= 50 && randPosY >= 5) {
                generateBlockOre(random, randPosX, randPosY, randPosZ, chunk,
                        ModBlocks.REDANTTROLL.get().defaultBlockState(), 4);
            }
        }
        patchy = 4 + random.nextInt(4);
        if (lessOre != 0) {
            patchy /= 2;
        }
        for (int i = 0; i < patchy; ++i) {
            final int randPosX = 3 + chunkX + random.nextInt(10);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 3 + chunkZ + random.nextInt(10);
            if (randPosY <= 50 && randPosY >= 5) {
                generateBlockOre(random, randPosX, randPosY, randPosZ, chunk,
                        ModBlocks.TERMITETROLL.get().defaultBlockState(), 4);
            }
        }
        if (lessOre == 0) {
            exact(random, chunkX, chunkZ, chunk, OreStats.Diamond_stats(), Blocks.DIAMOND_ORE);
            exact(random, chunkX, chunkZ, chunk, OreStats.BlkDiamond_stats(), Blocks.DIAMOND_BLOCK);
            exact(random, chunkX, chunkZ, chunk, OreStats.Emerald_stats(), Blocks.EMERALD_ORE);
            exact(random, chunkX, chunkZ, chunk, OreStats.BlkEmerald_stats(), Blocks.EMERALD_BLOCK);
            exact(random, chunkX, chunkZ, chunk, OreStats.Gold_stats(), Blocks.GOLD_ORE);
            exact(random, chunkX, chunkZ, chunk, OreStats.BlkGold_stats(), Blocks.GOLD_BLOCK);
            exact(random, chunkX, chunkZ, chunk, OreStats.BlkRuby_stats(), ModBlocks.BLOCKRUBY.get());
        }
    }

    /** The loop body shared by Uranium, Titanium, Amethyst and Salt (:468-475 and its three copies). */
    private static void banded(final RandomSource random, final int chunkX, final int chunkZ, final ChunkAccess chunk,
                               final int patchy, final OreStats stats, final Block block) {
        for (int i = 0; i < patchy; ++i) {
            final int randPosX = 3 + chunkX + random.nextInt(10);
            final int randPosY = random.nextInt(128);
            final int randPosZ = 3 + chunkZ + random.nextInt(10);
            if (randPosY <= stats.maxdepth() && randPosY >= stats.mindepth()) {
                generateBlockOre(random, randPosX, randPosY, randPosZ, chunk, block.defaultBlockState(),
                        stats.clumpsize());
            }
        }
    }

    /** The {@code LessOre == 0} blocks with exactly {@code rate} tries (:544-613). */
    private static void exact(final RandomSource random, final int chunkX, final int chunkZ, final ChunkAccess chunk,
                              final OreStats stats, final Block block) {
        if (stats.rate() > 0) {
            for (int i = 0; i < stats.rate(); ++i) {
                final int randPosX = 3 + chunkX + random.nextInt(10);
                final int randPosY = random.nextInt(128);
                final int randPosZ = 3 + chunkZ + random.nextInt(10);
                if (randPosY <= stats.maxdepth() && randPosY >= stats.mindepth()) {
                    generateBlockOre(random, randPosX, randPosY, randPosZ, chunk, block.defaultBlockState(),
                            stats.clumpsize());
                }
            }
        }
    }

    private static BlockState spawnOre(final String id) {
        return ModBlocks.DRIED_EGGS.get(id).get().defaultBlockState();
    }

    /**
     * {@code generateBlockOre(World, Random, int, int, int, Chunk, Block, int)} (:617-657): the
     * {@code WorldGenMinable} vein, clipped to {@code chunk}. The unused {@code World} parameter is dropped.
     */
    public static boolean generateBlockOre(final RandomSource par2Random, final int par3, final int par4, final int par5,
                                           final ChunkAccess chunk, final BlockState newbid, final int numberOfBlocks) {
        final float f = par2Random.nextFloat() * 3.1415927F;
        final double d0 = (double) ((float) (par3 + 8) + Mth.sin(f) * (float) numberOfBlocks / 8.0F);
        final double d2 = (double) ((float) (par3 + 8) - Mth.sin(f) * (float) numberOfBlocks / 8.0F);
        final double d3 = (double) ((float) (par5 + 8) + Mth.cos(f) * (float) numberOfBlocks / 8.0F);
        final double d4 = (double) ((float) (par5 + 8) - Mth.cos(f) * (float) numberOfBlocks / 8.0F);
        final double d5 = (double) (par4 + par2Random.nextInt(3) - 2);
        final double d6 = (double) (par4 + par2Random.nextInt(3) - 2);
        for (int l = 0; l <= numberOfBlocks; ++l) {
            final double d7 = d0 + (d2 - d0) * (double) l / (double) numberOfBlocks;
            final double d8 = d5 + (d6 - d5) * (double) l / (double) numberOfBlocks;
            final double d9 = d3 + (d4 - d3) * (double) l / (double) numberOfBlocks;
            final double d10 = par2Random.nextDouble() * (double) numberOfBlocks / 16.0;
            final double d11 = (double) (Mth.sin((float) l * 3.1415927F / (float) numberOfBlocks) + 1.0F) * d10 + 1.0;
            final double d12 = (double) (Mth.sin((float) l * 3.1415927F / (float) numberOfBlocks) + 1.0F) * d10 + 1.0;
            final int i1 = Mth.floor(d7 - d11 / 2.0);
            final int j1 = Mth.floor(d8 - d12 / 2.0);
            final int k1 = Mth.floor(d9 - d11 / 2.0);
            final int l2 = Mth.floor(d7 + d11 / 2.0);
            final int i2 = Mth.floor(d8 + d12 / 2.0);
            final int j2 = Mth.floor(d9 + d11 / 2.0);
            for (int k2 = i1; k2 <= l2; ++k2) {
                final double d13 = ((double) k2 + 0.5 - d7) / (d11 / 2.0);
                if (d13 * d13 < 1.0) {
                    for (int l3 = j1; l3 <= i2; ++l3) {
                        final double d14 = ((double) l3 + 0.5 - d8) / (d12 / 2.0);
                        if (d13 * d13 + d14 * d14 < 1.0) {
                            for (int i3 = k1; i3 <= j2; ++i3) {
                                final double d15 = ((double) i3 + 0.5 - d9) / (d11 / 2.0);
                                final BlockState bid = FastBlocks.getBlockInChunk(chunk, k2, l3, i3);
                                if (d13 * d13 + d14 * d14 + d15 * d15 < 1.0 && bid.is(Blocks.STONE)) {
                                    FastBlocks.setBlockInChunk(chunk, k2, l3, i3, newbid);
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
