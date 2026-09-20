package com.swbr.orespawn.world.dungeon.a;

import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.FastSetBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.block;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.fill_chests;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.setBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.spawner;

import com.swbr.orespawn.world.structure.StructureWriter;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * {@code GenericDungeon.makeEnormousCastle} (GenericDungeon.java:223-417) with {@code buildLevel} (:419-522) and
 * {@code addLevelDecorations} (:524-768): the King's Challenge Dungeon, a tower of up to six caged levels on a stone
 * base with a quartz platform, a bridge and a staircase down to the ground (verhalten/world-01.md,
 * "makeEnormousCastle"). The Queen version {@code makeEnormousCastleQ} is ported by dungeon-c.
 *
 * <p>Placement: Islands, {@code addD4Castle} (structure {@code orespawn:make_enormous_castle}, variant 1), and the
 * Random Dungeon Spawner (case 2). Extent x -37..55, y -1..80, z -28..55.
 */
public final class EnormousCastle {

    private EnormousCastle() {
    }

    /** {@code makeEnormousCastle(world, cposx, cposy, cposz)} (:223-417). */
    public static void makeEnormousCastle(final StructureWriter world, final Random rand, final int cposx,
                                          final int cposy, final int cposz) {
        final int width = 28;
        final int height = 16;
        final int platformwidth = 11;
        int level = 0;
        // :228 world.isRemote - builders run on the server only.
        level = 1 + rand.nextInt(6);
        if (level <= 3 && rand.nextInt(3) != 1) {
            level += 3;
        }
        for (int i = -20; i < width + 4; ++i) {
            for (int j = 1; j < height + 10; ++j) {
                for (int k = -4; k < width + 4; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = 0;
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.STONE);
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height;
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.BEDROCK);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 1; j < height; ++j) {
                int k = 0;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.IRON_BARS);
                k = width - 1;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.IRON_BARS);
            }
        }
        for (int k = 0; k < width; ++k) {
            for (int j = 1; j < height; ++j) {
                int i = 0;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.IRON_BARS);
                i = width - 1;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.IRON_BARS);
            }
        }
        // :270-273 world.setBlock(..., OreSpawnMain.ExtremeTorch): metadata 0, flags 3. BlockTorch.onBlockAdded then
        // chose the floor (neither iron bars nor air is a solid side), which LegacyMeta.torch(0) already is.
        final Block extremeTorch = block("orespawn:extremetorch");
        setBlock(world, cposx + 1, cposy + 1, cposz + 1, extremeTorch);
        setBlock(world, cposx + 1, cposy + 1, cposz + width - 2, extremeTorch);
        setBlock(world, cposx + width - 2, cposy + 1, cposz + 1, extremeTorch);
        setBlock(world, cposx + width - 2, cposy + 1, cposz + width - 2, extremeTorch);
        for (int i = -4; i < width + 4; ++i) {
            for (int k = -4; k < width + 4; ++k) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    FastSetBlock(world, cposx + i, cposy, cposz + k, Blocks.STONE);
                }
                if (i == -4 || k == -4 || i == width + 3 || k == width + 3) {
                    FastSetBlock(world, cposx + i, cposy + 1, cposz + k, Blocks.NETHER_BRICK_FENCE);
                }
            }
        }
        for (int j = 0; j < 4; ++j) {
            spawner(world, cposx - 3, cposy + 1 + j, cposz - 3, "Terrible Terror");
            spawner(world, cposx - 3, cposy + 1 + j, cposz + width + 2, "Terrible Terror");
            spawner(world, cposx + width + 2, cposy + 1 + j, cposz - 3, "Terrible Terror");
            spawner(world, cposx + width + 2, cposy + 1 + j, cposz + width + 2, "Terrible Terror");
        }
        spawner(world, cposx + width / 2, cposy + 2, cposz + width / 2, "Emperor Scorpion");
        spawner(world, cposx + width / 2, cposy + 3, cposz + width / 2, "Emperor Scorpion");
        spawner(world, cposx + width / 2, cposy + 4, cposz + width / 2, "Emperor Scorpion");
        int j = height;
        buildLevel(world, rand, cposx + 1, cposy + j, cposz + 1, width - 2, 10, 4, "Cloud Shark", 1, -1, 5, 1, level);
        j += 10;
        if (level >= 2) {
            buildLevel(world, rand, cposx + 1, cposy + j, cposz + 1, width - 2, 10, 4, "Lurking Terror", 0, 0, 4, 2, level);
        }
        j += 10;
        if (level >= 3) {
            buildLevel(world, rand, cposx + 2, cposy + j, cposz + 2, width - 4, 9, 4, "Rotator", 1, 1, 4, 3, level);
        }
        j += 9;
        if (level >= 4) {
            buildLevel(world, rand, cposx + 2, cposy + j, cposz + 2, width - 4, 9, 3, "Bee", 0, 0, 4, 4, level);
        }
        j += 9;
        if (level >= 5) {
            buildLevel(world, rand, cposx + 3, cposy + j, cposz + 3, width - 6, 8, 3, "Mantis", 1, 1, 4, 5, level);
        }
        j += 8;
        if (level >= 6) {
            buildLevel(world, rand, cposx + 3, cposy + j, cposz + 3, width - 6, 16, 3, "Mothra", 0, 0, 3, 6, level);
        }
        j += 16;
        for (int i = 0; i < platformwidth; ++i) {
            j = height;
            for (int k = -(platformwidth / 2); k <= platformwidth / 2; ++k) {
                FastSetBlock(world, cposx + i - 20, cposy + j, cposz + k + width / 2, Blocks.QUARTZ_BLOCK);
                if ((i == 0 || i == platformwidth - 1 || k == -(platformwidth / 2) || k == platformwidth / 2)
                        && (i != 0 || k < -1 || k > 1)) {
                    FastSetBlock(world, cposx + i - 20, cposy + j + 1, cposz + k + width / 2, Blocks.NETHER_BRICK_FENCE);
                }
            }
        }
        for (int i = -10; i <= -3; ++i) {
            j = height;
            for (int k = -2; k < 3; ++k) {
                if (i == -3 || i == -10) {
                    if (k != -2 && k != 2) {
                        FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + width / 2, Blocks.AIR);
                    } else {
                        FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + width / 2, Blocks.NETHERRACK);
                        FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k + width / 2, Blocks.NETHERRACK);
                        FastSetBlock(world, cposx + i, cposy + j + 3, cposz + k + width / 2, Blocks.FIRE);
                    }
                } else {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k + width / 2, Blocks.QUARTZ_BLOCK);
                    if (k == -2 || k == 2) {
                        FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + width / 2, Blocks.NETHER_BRICK_FENCE);
                    }
                }
            }
        }
        int i = -21;
        for (j = height; j >= 0; --j) {
            for (int k = -2; k < 3; ++k) {
                for (int t = 0; t < 6; ++t) {
                    FastSetBlock(world, cposx + i, cposy + j + t + 1, cposz + k + width / 2, Blocks.AIR);
                }
                if (j == 0) {
                    if (k != -2 && k != 2) {
                        FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + width / 2, Blocks.AIR);
                    } else {
                        FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + width / 2, Blocks.NETHERRACK);
                        FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k + width / 2, Blocks.NETHERRACK);
                        FastSetBlock(world, cposx + i, cposy + j + 3, cposz + k + width / 2, Blocks.FIRE);
                    }
                } else {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k + width / 2, Blocks.QUARTZ_BLOCK);
                    if (k == -2 || k == 2) {
                        FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + width / 2, Blocks.NETHER_BRICK_FENCE);
                    }
                }
            }
            --i;
        }
        if (level >= 6) {
            final int span = width * 3;
            for (int tries = 0; tries < 100; ++tries) {
                j = -1;
                i = rand.nextInt(span);
                int k = rand.nextInt(span);
                if (i < span / 4 || i > span * 3 / 4 || k < span / 4 || k > span * 3 / 4) {
                    i -= span / 2;
                    k -= span / 2;
                    spawner(world, cposx + i + width / 2, cposy + j, cposz + k + width / 2, "Large Worm");
                }
            }
        }
    }

    /** {@code buildLevel} (:419-522): one caged level with outer ring, stair, hole and four corner spawner columns. */
    public static void buildLevel(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                  final int cposz, final int width, final int height, final int pw, final String critter,
                                  final int stepside, final int stepoff, final int holelen, final int decor,
                                  final int level) {
        for (int i = -pw; i < width + pw; ++i) {
            for (int j = 1; j < height; ++j) {
                for (int k = -pw; k < width + pw; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = 0;
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.BEDROCK);
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height;
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.BEDROCK);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 1; j < height; ++j) {
                int k = 0;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.BEDROCK);
                k = width - 1;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.BEDROCK);
            }
        }
        for (int k = 0; k < width; ++k) {
            for (int j = 1; j < height; ++j) {
                Block blk = Blocks.BEDROCK;
                if (k == 0 || k == width - 1) {
                    blk = Blocks.GOLD_BLOCK;
                }
                int i = 0;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                i = width - 1;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = -pw; i < width + pw; ++i) {
            for (int k = -pw; k < width + pw; ++k) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    FastSetBlock(world, cposx + i, cposy, cposz + k, Blocks.STONE);
                }
                if (i == -pw || k == -pw || i == width + (pw - 1) || k == width + (pw - 1)) {
                    FastSetBlock(world, cposx + i, cposy + 1, cposz + k, Blocks.NETHER_BRICK_FENCE);
                }
            }
        }
        int i = -(height / 2);
        i += width / 2;
        for (int j = 1; j < height; ++j) {
            if (stepside != 0) {
                final int k = -1;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.STONE);
            } else {
                final int k = width;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.STONE);
            }
            ++i;
        }
        if (stepoff >= 0) {
            int k;
            if (stepside == 0) {
                k = -1;
                k -= stepoff;
            } else {
                k = width;
                k += stepoff;
            }
            i = width / 2;
            final int j = 0;
            for (int l = 0; l < holelen; ++l) {
                FastSetBlock(world, cposx + i + l, cposy + j, cposz + k, Blocks.AIR);
            }
        }
        for (int j = 0; j < 4; ++j) {
            spawner(world, cposx - (pw - 1), cposy + j + 1, cposz - (pw - 1), critter);
            spawner(world, cposx - (pw - 1), cposy + j + 1, cposz + width + (pw - 2), critter);
            spawner(world, cposx + width + (pw - 2), cposy + j + 1, cposz - (pw - 1), critter);
            spawner(world, cposx + width + (pw - 2), cposy + j + 1, cposz + width + (pw - 2), critter);
        }
        addLevelDecorations(world, rand, cposx, cposy, cposz, width, height, decor, level);
    }

    /**
     * {@code addLevelDecorations} (:524-768): the boss spawner pair of the level, its bedrock cross, the floor and
     * ceiling holes and the reward chests. {@code difficulty} is the castle's level count.
     */
    public static void addLevelDecorations(final StructureWriter world, final Random rand, final int cposx,
                                           final int cposy, final int cposz, final int width, final int height,
                                           final int decor, final int difficulty) {
        int reward = 1;
        String critter = "Alosaurus";
        if (decor == 6) {
            FastSetBlock(world, cposx, cposy + height, cposz, Blocks.NETHERRACK);
            FastSetBlock(world, cposx, cposy + height + 1, cposz, Blocks.FIRE);
            FastSetBlock(world, cposx, cposy + height, cposz + width - 1, Blocks.NETHERRACK);
            FastSetBlock(world, cposx, cposy + height + 1, cposz + width - 1, Blocks.FIRE);
            FastSetBlock(world, cposx + width - 1, cposy + height, cposz, Blocks.NETHERRACK);
            FastSetBlock(world, cposx + width - 1, cposy + height + 1, cposz, Blocks.FIRE);
            FastSetBlock(world, cposx + width - 1, cposy + height, cposz + width - 1, Blocks.NETHERRACK);
            FastSetBlock(world, cposx + width - 1, cposy + height + 1, cposz + width - 1, Blocks.FIRE);
            FastSetBlock(world, cposx + width / 2, cposy + height, cposz + width / 2, Blocks.AIR);
            spawner(world, cposx + width / 2 - 1, cposy + height + 2, cposz + width / 2, "Nightmare");
            spawner(world, cposx + width / 2 + 1, cposy + height + 2, cposz + width / 2, "Nightmare");
            spawner(world, cposx + width / 2, cposy + height + 2, cposz + width / 2 - 1, "Nightmare");
            spawner(world, cposx + width / 2, cposy + height + 2, cposz + width / 2 + 1, "Nightmare");
            for (int i = 1; i < width - 1; ++i) {
                for (int j = 1; j < 5; ++j) {
                    for (int k = 1; k < width - 1; ++k) {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.DIRT);
                    }
                }
            }
            spawner(world, cposx + width / 2, cposy + 2, cposz + width / 2, "Large Worm");
            spawner(world, cposx + width / 2, cposy + 3, cposz + width / 2, "Large Worm");
            spawner(world, cposx + width / 2, cposy + 4, cposz + width / 2, "Large Worm");
            for (int j = 0; j < 10; ++j) {
                FastSetBlock(world, cposx + 1, cposy + j, cposz + 1, Blocks.AIR);
            }
            fill_chests(world, rand, cposx, cposy + 4, cposz, width, height, decor, reward);
        }
        if (decor == 5) {
            if (difficulty == 5) {
                critter = "Alosaurus";
                reward = 1;
            }
            if (difficulty == 6) {
                critter = "T. Rex";
                reward = 2;
            }
            bossPair(world, cposx, cposy, cposz, width, critter);
            FastSetBlock(world, cposx + width - 2, cposy, cposz + width - 2, Blocks.AIR);
            FastSetBlock(world, cposx + 1, cposy + height, cposz + 1, Blocks.AIR);
            fill_chests(world, rand, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 4) {
            if (difficulty == 4) {
                critter = "Alosaurus";
                reward = 1;
            }
            if (difficulty == 5) {
                critter = "T. Rex";
                reward = 2;
            }
            if (difficulty == 6) {
                critter = "Basilisk";
                reward = 3;
            }
            bossPair(world, cposx, cposy, cposz, width, critter);
            FastSetBlock(world, cposx + 1, cposy, cposz + 1, Blocks.AIR);
            FastSetBlock(world, cposx + width - 2, cposy + height, cposz + width - 2, Blocks.AIR);
            fill_chests(world, rand, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 3) {
            if (difficulty == 3) {
                critter = "Alosaurus";
                reward = 1;
            }
            if (difficulty == 4) {
                critter = "T. Rex";
                reward = 2;
            }
            if (difficulty == 5) {
                critter = "Basilisk";
                reward = 3;
            }
            if (difficulty == 6) {
                critter = "Hercules Beetle";
                reward = 4;
            }
            bossPair(world, cposx, cposy, cposz, width, critter);
            FastSetBlock(world, cposx + width - 2, cposy, cposz + width - 2, Blocks.AIR);
            FastSetBlock(world, cposx + 1, cposy + height, cposz + 1, Blocks.AIR);
            fill_chests(world, rand, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 2) {
            if (difficulty == 2) {
                critter = "Alosaurus";
                reward = 1;
            }
            if (difficulty == 3) {
                critter = "T. Rex";
                reward = 2;
            }
            if (difficulty == 4) {
                critter = "Basilisk";
                reward = 3;
            }
            if (difficulty == 5) {
                critter = "Hercules Beetle";
                reward = 4;
            }
            if (difficulty == 6) {
                critter = "Jumpy Bug";
                reward = 5;
            }
            bossPair(world, cposx, cposy, cposz, width, critter);
            FastSetBlock(world, cposx + 1, cposy, cposz + 1, Blocks.AIR);
            FastSetBlock(world, cposx + width - 2, cposy + height, cposz + width - 2, Blocks.AIR);
            fill_chests(world, rand, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 1) {
            if (difficulty == 1) {
                critter = "Alosaurus";
            }
            if (difficulty == 2) {
                critter = "T. Rex";
            }
            if (difficulty == 3) {
                critter = "Basilisk";
            }
            if (difficulty == 4) {
                critter = "Hercules Beetle";
            }
            if (difficulty == 5) {
                critter = "Jumpy Bug";
            }
            if (difficulty == 6) {
                critter = "Hammerhead";
            }
            reward = difficulty;
            bossPair(world, cposx, cposy, cposz, width, critter);
            final Block rtp = block("orespawn:blockteleport");
            FastSetBlock(world, cposx + width / 2 - 1, cposy + 1, cposz + width / 2 - 1, rtp);
            FastSetBlock(world, cposx + width / 2 + 1, cposy + 1, cposz + width / 2 + 1, rtp);
            FastSetBlock(world, cposx + width / 2 + 1, cposy + 1, cposz + width / 2 - 1, rtp);
            FastSetBlock(world, cposx + width / 2 - 1, cposy + 1, cposz + width / 2 + 1, rtp);
            FastSetBlock(world, cposx + 1, cposy + height, cposz + 1, Blocks.AIR);
            fill_chests(world, rand, cposx, cposy, cposz, width, height, decor, reward);
        }
    }

    /**
     * The block decor 1-5 repeat verbatim (:594-609, :627-642, :664-679, :705-720, :745-760): two spawners at
     * (w/2, 2..3, w/2) and a bedrock cross around them at y 1..4.
     */
    private static void bossPair(final StructureWriter world, final int cposx, final int cposy, final int cposz,
                                 final int width, final String critter) {
        spawner(world, cposx + width / 2, cposy + 2, cposz + width / 2, critter);
        spawner(world, cposx + width / 2, cposy + 3, cposz + width / 2, critter);
        for (int j = 1; j < 5; ++j) {
            FastSetBlock(world, cposx + width / 2 - 1, cposy + j, cposz + width / 2, Blocks.BEDROCK);
            FastSetBlock(world, cposx + width / 2 + 1, cposy + j, cposz + width / 2, Blocks.BEDROCK);
            FastSetBlock(world, cposx + width / 2, cposy + j, cposz + width / 2 - 1, Blocks.BEDROCK);
            FastSetBlock(world, cposx + width / 2, cposy + j, cposz + width / 2 + 1, Blocks.BEDROCK);
        }
    }
}
