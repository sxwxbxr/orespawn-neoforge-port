package com.swbr.orespawn.world.dungeon.a;

import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.FastSetBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.blazeContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.chest;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.kyuubiContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.shadowContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.spawner;

import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The two nether-themed Mining dungeons of {@code GenericDungeon}: {@code makeKyuubiDungeon} with
 * {@code addlavasquare}, {@code addkyuubi} and {@code addblaze} (GenericDungeon.java:1137-1404), and
 * {@code makeShadowDungeon} with {@code fill_shadow_chests} (:1496-1608); verhalten/world-01.md,
 * "makeKyuubiDungeon", "makeShadowDungeon".
 */
public final class NetherDungeons {

    private NetherDungeons() {
    }

    /**
     * {@code makeKyuubiDungeon} (:1137-1253): a sandstone entrance and a stone shaft down to a water pool, a lava-walled
     * tunnel into a netherrack hall with the Kyuubi altar, the Blaze tower, lava squares and fire. Extent x 0..34,
     * y -22..5, z -15..14.
     */
    public static void makeKyuubiDungeon(final StructureWriter world, final Random rand, final int cposx,
                                         final int cposy, final int cposz) {
        final int width = 5;
        final int height = 5;
        final int depth = 20;
        final int length = 12;
        final int rwidth = 30;
        final int rheight = 18;
        final int rlength = 20;
        // :1145 world.isRemote - builders run on the server only.
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < 5; ++j) {
                for (int k = 0; k < width; ++k) {
                    FastSetBlock(world, cposx + i, cposy - j, cposz + k, Blocks.AIR);
                }
            }
        }
        int j = height;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.SANDSTONE);
            }
        }
        FastSetBlock(world, cposx + width / 2, cposy + j, cposz + width / 2, Blocks.AIR);
        Block blk = Blocks.SANDSTONE;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                for (j = 0; j < height; ++j) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                    } else {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                    }
                }
            }
        }
        blk = Blocks.STONE;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                for (j = -1; j > -depth; --j) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                    } else {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                    }
                }
            }
        }
        for (int i = 1; i < width - 1; ++i) {
            for (int k = 1; k < width - 1; ++k) {
                for (j = -depth; j > -(depth + 2); --j) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.WATER);
                }
            }
        }
        for (int i = 1; i < width - 1; ++i) {
            for (int k = 1; k < width - 1; ++k) {
                j = -(depth + 2);
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.STONE);
            }
        }
        int x = cposx + width + length - 2;
        int z = cposz - rwidth / 2;
        int y = cposy - depth;
        blk = Blocks.NETHERRACK;
        for (int i = 0; i < rlength; ++i) {
            for (int k = 0; k < rwidth; ++k) {
                for (j = 0; j < rheight; ++j) {
                    if (k == 0 || k == rwidth - 1 || j == 0 || j == rheight - 1 || i == 0 || i == rlength - 1) {
                        FastSetBlock(world, x + i, y + j, z + k, blk);
                    } else {
                        FastSetBlock(world, x + i, y + j, z + k, Blocks.AIR);
                    }
                }
            }
        }
        x = cposx + width - 1;
        z = cposz;
        y = cposy - depth;
        for (int i = 0; i < length; ++i) {
            for (int k = 0; k < width; ++k) {
                for (j = 0; j < width; ++j) {
                    if (k == 0 || k == width - 1 || j == 0 || j == width - 1) {
                        blk = Blocks.STONE;
                        if (j > 0 && j < width - 1) {
                            blk = Blocks.LAVA;
                        }
                        FastSetBlock(world, x + i, y + j, z + k, blk);
                    } else {
                        FastSetBlock(world, x + i, y + j, z + k, Blocks.AIR);
                    }
                }
            }
        }
        x = cposx + width + length - 2;
        z = cposz - rwidth / 2;
        y = cposy - depth;
        ++y;
        addlavasquare(world, x + 2, y, z + 2);
        addlavasquare(world, x + 4, y, z + 6);
        addlavasquare(world, x + 12, y, z + 10);
        addlavasquare(world, x + 6, y, z + 15);
        addlavasquare(world, x + 3, y, z + 22);
        addkyuubi(world, rand, x + rlength / 4, y, z + rwidth * 3 / 4 - 3);
        addblaze(world, rand, x + rlength * 2 / 3 - 3, y, z + rwidth / 4 - 2);
        FastSetBlock(world, x + 7, y, z + 1, Blocks.FIRE);
        FastSetBlock(world, x + 5, y, z + 9, Blocks.FIRE);
        FastSetBlock(world, x + 2, y, z + 12, Blocks.FIRE);
        FastSetBlock(world, x + 16, y, z + 18, Blocks.FIRE);
        FastSetBlock(world, x + 2, y, z + 27, Blocks.FIRE);
        FastSetBlock(world, x + 18, y, z + 28, Blocks.FIRE);
    }

    /** {@code addlavasquare} (:1255-1261): lava with four netherrack neighbours. */
    private static void addlavasquare(final StructureWriter world, final int x, final int y, final int z) {
        FastSetBlock(world, x - 1, y, z, Blocks.NETHERRACK);
        FastSetBlock(world, x + 1, y, z, Blocks.NETHERRACK);
        FastSetBlock(world, x, y, z + 1, Blocks.NETHERRACK);
        FastSetBlock(world, x, y, z - 1, Blocks.NETHERRACK);
        FastSetBlock(world, x, y, z, Blocks.LAVA);
    }

    /** {@code addkyuubi} (:1263-1303): nether brick rings round lava, three Kyuubi spawners, one chest on top. */
    private static void addkyuubi(final StructureWriter world, final Random rand, final int x, final int y,
                                  final int z) {
        int width = 9;
        final WeightedRandomChestContent[] chestContents = kyuubiContentsList;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                    FastSetBlock(world, x + i, y, z + k, Blocks.NETHER_BRICKS);
                } else {
                    FastSetBlock(world, x + i, y, z + k, Blocks.LAVA);
                }
            }
        }
        width = 7;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                    FastSetBlock(world, x + i + 1, y + 1, z + k + 1, Blocks.NETHER_BRICKS);
                } else {
                    FastSetBlock(world, x + i + 1, y + 1, z + k + 1, Blocks.LAVA);
                }
            }
        }
        for (int j = 0; j < 3; ++j) {
            spawner(world, x + 4, y + j + 2, z + 4, "Kyuubi");
        }
        chest(world, rand, x + 4, y + 5, z + 4, 2, chestContents, 7 + rand.nextInt(7));
    }

    /** {@code addblaze} (:1305-1404): a stepped obsidian tower, eight Blaze spawners near the top, four chests. */
    private static void addblaze(final StructureWriter world, final Random rand, final int x, final int y,
                                 final int z) {
        int width = 7;
        int height = 4;
        int xx = x;
        int yy = y;
        int zz = z;
        final WeightedRandomChestContent[] chestContents = blazeContentsList;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                for (int j = 0; j < height; ++j) {
                    FastSetBlock(world, xx + i, yy + j, zz + k, Blocks.OBSIDIAN);
                }
            }
        }
        ++xx;
        yy += height;
        ++zz;
        width = 5;
        height = 1;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                for (int j = 0; j < height; ++j) {
                    FastSetBlock(world, xx + i, yy + j, zz + k, Blocks.OBSIDIAN);
                }
            }
        }
        ++xx;
        yy += height;
        ++zz;
        width = 3;
        height = 6;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                for (int j = 0; j < height; ++j) {
                    FastSetBlock(world, xx + i, yy + j, zz + k, Blocks.OBSIDIAN);
                }
            }
        }
        ++xx;
        yy += height;
        ++zz;
        width = 1;
        height = 5;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < width; ++k) {
                for (int j = 0; j < height; ++j) {
                    FastSetBlock(world, xx + i, yy + j, zz + k, Blocks.OBSIDIAN);
                }
            }
        }
        for (int j = 0; j < 2; ++j) {
            spawner(world, xx - 1, yy + height + j - 3, zz, "Blaze");
            spawner(world, xx + 1, yy + height + j - 3, zz, "Blaze");
            spawner(world, xx, yy + height + j - 3, zz - 1, "Blaze");
            spawner(world, xx, yy + height + j - 3, zz + 1, "Blaze");
        }
        chest(world, rand, x, y + 4, z + 3, 4, chestContents, 4 + rand.nextInt(5));
        chest(world, rand, x + 3, y + 4, z, 2, chestContents, 3 + rand.nextInt(5));
        chest(world, rand, x + 3, y + 4, z + 6, 3, chestContents, 5 + rand.nextInt(5));
        chest(world, rand, x + 6, y + 4, z + 3, 5, chestContents, 6 + rand.nextInt(5));
    }

    /**
     * {@code makeShadowDungeon} (:1496-1577): two stacked stepped pyramids of obsidian and bedrock rings, the lower one
     * with soul sand crosses, sixteen Ender Reaper / Nightmare spawners and eight chests. Extent x 0..18, y -9..9,
     * z 0..18.
     */
    public static void makeShadowDungeon(final StructureWriter world, final Random rand, final int cposx,
                                         final int cposy, final int cposz) {
        final int totalwidth = 19;
        String whichmob = null;
        // :1500 world.isRemote - builders run on the server only.
        int yoff;
        int xoff;
        int zoff = xoff = (yoff = 0);
        for (int width = totalwidth; width > 0; width -= 2) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < width; ++k) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        Block blk = Blocks.OBSIDIAN;
                        if ((yoff & 0x1) != 0x0) {
                            blk = Blocks.BEDROCK;
                        }
                        if ((k >= width / 2 - 1 && k <= width / 2 + 1) || (i >= width / 2 - 1 && i <= width / 2 + 1)) {
                            blk = Blocks.SOUL_SAND;
                        }
                        FastSetBlock(world, cposx + i + xoff, cposy - yoff, cposz + k + zoff, blk);
                    } else {
                        FastSetBlock(world, cposx + i + xoff, cposy - yoff, cposz + k + zoff, Blocks.AIR);
                    }
                }
            }
            if (width <= 15 && width >= 9) {
                if ((yoff & 0x1) != 0x0) {
                    fill_shadow_chests(world, rand, cposx + xoff, cposy - yoff, cposz + zoff, width, 0);
                    whichmob = "Ender Reaper";
                } else {
                    whichmob = "Nightmare";
                }
                spawner(world, cposx + xoff + 1, cposy - yoff, cposz + zoff + 1, whichmob);
                spawner(world, cposx + xoff + width - 2, cposy - yoff, cposz + zoff + 1, whichmob);
                spawner(world, cposx + xoff + 1, cposy - yoff, cposz + zoff + width - 2, whichmob);
                spawner(world, cposx + xoff + width - 2, cposy - yoff, cposz + zoff + width - 2, whichmob);
            }
            ++xoff;
            ++zoff;
            ++yoff;
        }
        zoff = (xoff = (yoff = 0));
        for (int width = totalwidth; width > 0; width -= 2) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < width; ++k) {
                    if (k == 0 || k == width - 1 || i == 0 || i == width - 1) {
                        Block blk = Blocks.OBSIDIAN;
                        if ((yoff & 0x1) != 0x0) {
                            blk = Blocks.BEDROCK;
                        }
                        FastSetBlock(world, cposx + i + xoff, cposy + yoff, cposz + k + zoff, blk);
                    } else {
                        FastSetBlock(world, cposx + i + xoff, cposy + yoff, cposz + k + zoff, Blocks.AIR);
                    }
                }
            }
            ++xoff;
            ++zoff;
            ++yoff;
        }
    }

    /** {@code fill_shadow_chests} (:1579-1608): four chests on one layer, {@code 3 + nextInt(7)} draws each. */
    private static void fill_shadow_chests(final StructureWriter world, final Random rand, final int cposx,
                                           final int cposy, final int cposz, final int width, final int height) {
        final WeightedRandomChestContent[] chestContents = shadowContentsList;
        final int j = height;
        chest(world, rand, cposx + 1, cposy + j, cposz + width / 2, 5, chestContents, 3 + rand.nextInt(7));
        chest(world, rand, cposx + width - 2, cposy + j, cposz + width / 2, 4, chestContents, 3 + rand.nextInt(7));
        chest(world, rand, cposx + width / 2, cposy + j, cposz + 1, 3, chestContents, 3 + rand.nextInt(7));
        chest(world, rand, cposx + width / 2, cposy + j, cposz + width - 2, 2, chestContents, 3 + rand.nextInt(7));
    }
}
