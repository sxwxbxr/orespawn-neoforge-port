package com.swbr.orespawn.world.dungeon.a;

import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.FastSetBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.beeContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.chest;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.mantisContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.spawner;

import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The three insect nests of {@code GenericDungeon}: {@code makeBeeHive} (GenericDungeon.java:859-936, Mining),
 * {@code makeMantisHive} (:1055-1135, Overworld forests) and {@code makeSmallBeeHive} (:1406-1494, Overworld forests),
 * verhalten/world-01.md, "makeBeeHive", "makeMantisHive", "makeSmallBeeHive".
 */
public final class Hives {

    private Hives() {
    }

    /**
     * {@code makeBeeHive} (:859-904): a 10x31x10 shaft of coal and gold ore rings down from the origin, four Bee
     * spawners and 56 chests. Extent x 0..9, y -30..0, z 0..9.
     */
    public static void makeBeeHive(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                   final int cposz) {
        final int width = 10;
        final int height = 30;
        // :862 world.isRemote - builders run on the server only.
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < 5; ++j) {
                for (int k = 0; k < width; ++k) {
                    FastSetBlock(world, cposx + i, cposy - j, cposz + k, Blocks.AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height;
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy - j, cposz + k, Blocks.COAL_ORE);
            }
        }
        Block blk = Blocks.COAL_ORE;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                for (int j = 1; j < height; ++j) {
                    if (k == 0 || i == 0 || k == width - 1 || i == width - 1) {
                        blk = Blocks.COAL_ORE;
                        if ((j & 0x1) == 0x1) {
                            blk = Blocks.GOLD_ORE;
                        }
                        FastSetBlock(world, cposx + i, cposy - j, cposz + k, blk);
                    } else {
                        FastSetBlock(world, cposx + i, cposy - j, cposz + k, Blocks.AIR);
                    }
                }
            }
        }
        for (int j = 0; j < 4; ++j) {
            spawner(world, cposx + width / 2, cposy - 2 - j * (height / 4), cposz + width / 2, "Bee");
        }
        fill_beehive_chests(world, rand, cposx, cposy, cposz, width, height);
    }

    /** {@code fill_beehive_chests} (:906-936): four chests every second level, {@code 1 + nextInt(5)} draws each. */
    private static void fill_beehive_chests(final StructureWriter world, final Random rand, final int cposx,
                                            final int cposy, final int cposz, final int width, final int height) {
        final WeightedRandomChestContent[] chestContents = beeContentsList;
        for (int j = 2; j < height - 1; j += 2) {
            chest(world, rand, cposx + 1, cposy - j, cposz + width / 2, 5, chestContents, 1 + rand.nextInt(5));
            chest(world, rand, cposx + width - 2, cposy - j, cposz + width / 2, 4, chestContents, 1 + rand.nextInt(5));
            chest(world, rand, cposx + width / 2, cposy - j, cposz + 1, 3, chestContents, 1 + rand.nextInt(5));
            chest(world, rand, cposx + width / 2, cposy - j, cposz + width - 2, 2, chestContents, 1 + rand.nextInt(5));
        }
    }

    /**
     * {@code makeMantisHive} (:1055-1104): an upside-down stepped pyramid of gold and emerald ore rings, twelve chests
     * and three Mantis spawners. Extent x 0..12, y -6..19, z 0..12.
     */
    public static void makeMantisHive(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                      final int cposz) {
        int width = 13;
        // :1058 world.isRemote - builders run on the server only.
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < 20; ++j) {
                for (int k = 0; k < width; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                }
            }
        }
        int yoff;
        int xoff;
        int zoff = xoff = (yoff = 0);
        while (width > 0) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < width; ++k) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        Block blk = Blocks.GOLD_ORE;
                        if ((yoff & 0x1) != 0x0) {
                            blk = Blocks.EMERALD_ORE;
                        }
                        FastSetBlock(world, cposx + i + xoff, cposy - yoff, cposz + k + zoff, blk);
                    } else {
                        FastSetBlock(world, cposx + i + xoff, cposy - yoff, cposz + k + zoff, Blocks.AIR);
                    }
                }
            }
            if (width <= 11 && width >= 7) {
                fill_mantishive_chests(world, rand, cposx + xoff, cposy - yoff, cposz + zoff, width, 0);
            }
            ++xoff;
            ++zoff;
            ++yoff;
            width -= 2;
        }
        --xoff;
        --zoff;
        --yoff;
        for (int j = 4; j < 7; ++j) {
            // :1098 the z of the spawner is cposz + yoff, not + zoff; both are 6 here, so the column is where it looks.
            spawner(world, cposx + xoff, cposy + j - yoff, cposz + yoff, "Mantis");
        }
    }

    /** {@code fill_mantishive_chests} (:1106-1135): four chests on one layer, {@code 3 + nextInt(7)} draws each. */
    private static void fill_mantishive_chests(final StructureWriter world, final Random rand, final int cposx,
                                               final int cposy, final int cposz, final int width, final int height) {
        final WeightedRandomChestContent[] chestContents = mantisContentsList;
        final int j = height;
        chest(world, rand, cposx + 1, cposy + j, cposz + width / 2, 5, chestContents, 3 + rand.nextInt(7));
        chest(world, rand, cposx + width - 2, cposy + j, cposz + width / 2, 4, chestContents, 3 + rand.nextInt(7));
        chest(world, rand, cposx + width / 2, cposy + j, cposz + 1, 3, chestContents, 3 + rand.nextInt(7));
        chest(world, rand, cposx + width / 2, cposy + j, cposz + width - 2, 2, chestContents, 3 + rand.nextInt(7));
    }

    /**
     * {@code makeSmallBeeHive} (:1406-1494): a sponge hive on a mossy cobblestone stalk, three Bee spawners and one
     * chest. Extent x -3..9, y 1..21, z -3..9.
     */
    public static void makeSmallBeeHive(final StructureWriter world, final Random rand, final int cposx,
                                        final int cposy, final int cposz) {
        final int width = 7;
        final int height = 21;
        // :1412 world.isRemote - builders run on the server only.
        for (int i = -3; i < width + 3; ++i) {
            for (int j = height * 2 / 3; j < height; ++j) {
                for (int k = -3; k < width + 3; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                int j = height * 2 / 3;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.SPONGE);
                int blk = rand.nextInt(height / 3);
                blk *= 2;
                blk -= Math.abs(i - width / 2);
                blk -= Math.abs(k - width / 2);
                if (blk < 1) {
                    blk = 1;
                }
                if (i == width / 2 && k == width / 2) {
                    blk = height * 2 / 3;
                }
                for (j = 0; j < blk; ++j) {
                    FastSetBlock(world, cposx + i, cposy + height * 2 / 3 - j, cposz + k, Blocks.MOSSY_COBBLESTONE);
                }
            }
        }
        int j = height * 2 / 3;
        for (int blk = 0; blk < height / 6; ++blk) {
            ++j;
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < width; ++k) {
                    if (k == 0 || i == 0 || k == width - 1 || i == width - 1) {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.SPONGE);
                    } else {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                    }
                }
            }
            ++j;
            for (int i = -1; i < width + 1; ++i) {
                for (int k = -1; k < width + 1; ++k) {
                    if (k == -1 || i == -1 || k == width || i == width) {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.SPONGE);
                    } else {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                    }
                }
            }
        }
        ++j;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.SPONGE);
            }
        }
        j = height * 2 / 3 + 1;
        for (int i = -1; i < 1; ++i) {
            for (int k = 2; k < 4; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, Blocks.AIR);
                FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k, Blocks.AIR);
            }
        }
        for (int blk = 0; blk < 3; ++blk) {
            spawner(world, cposx + 1, cposy + blk + j, cposz + 1, "Bee");
        }
        final WeightedRandomChestContent[] chestContents = beeContentsList;
        chest(world, rand, cposx + width / 2, cposy + j, cposz + width / 2, 5, chestContents, 7 + rand.nextInt(5));
    }
}
