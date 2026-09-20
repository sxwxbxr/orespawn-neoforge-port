package com.swbr.orespawn.world.dungeon.a;

import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.AlienWTFContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.FastSetBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.KnightContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.chest;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.setBlockFast;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.spawner;

import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * {@code makeAlienWTFDungeon} with {@code makePart} (GenericDungeon.java:1610-1830) and {@code makeEnderKnightDungeon}
 * with {@code makeShelves} (:1832-1967); verhalten/world-01.md, "makeAlienWTFDungeon" and "makeEnderKnightDungeon".
 */
public final class AlienAndKnightDungeons {

    private AlienAndKnightDungeons() {
    }

    /**
     * {@code makeAlienWTFDungeon} (:1610-1732): a lapis ore core 17 blocks down, a spiral shaft up to the surface and
     * four rooms of rising difficulty behind lapis corridors. Extent x -19..17, y -17..2, z -21..15.
     *
     * <p>The original has no {@code world.isRemote} check here (world-01.md, "Rolle"); nothing to port.
     */
    public static void makeAlienWTFDungeon(final StructureWriter world, final Random rand, int cposx, int cposy,
                                           int cposz) {
        final int width = 5;
        final int height = 5;
        int xwidth = 3;
        int zwidth = 6;
        final int depth = 20;
        cposy -= depth - 3;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                for (int k = 0; k < width; ++k) {
                    if (i == 0 || j == 0 || k == 0 || i == width - 1 || j == height - 1 || k == width - 1) {
                        FastSetBlock(world, cposx + i - 2, cposy + j, cposz + k - 2, Blocks.LAPIS_ORE);
                    } else {
                        FastSetBlock(world, cposx + i - 2, cposy + j, cposz + k - 2, Blocks.AIR);
                    }
                }
            }
        }
        int s = 0;
        --cposx;
        --cposz;
        for (int j = 3; j < depth; ++j) {
            for (int i = 0; i < 4; ++i) {
                for (int k = 0; k < 4; ++k) {
                    Block blk = Blocks.AIR;
                    if (i == 0 || k == 0 || i == 3 || k == 3) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    setBlockFast(world, cposx + i, cposy + j, cposz + k, blk, 0, 2);
                }
            }
            switch (s) {
                case 0: {
                    setBlockFast(world, cposx + 1, cposy + j, cposz + 1, Blocks.STONE, 0, 2);
                    break;
                }
                case 1: {
                    setBlockFast(world, cposx + 2, cposy + j, cposz + 1, Blocks.STONE, 0, 2);
                    break;
                }
                case 2: {
                    setBlockFast(world, cposx + 2, cposy + j, cposz + 2, Blocks.STONE, 0, 2);
                    break;
                }
                default: {
                    setBlockFast(world, cposx + 1, cposy + j, cposz + 2, Blocks.STONE, 0, 2);
                    break;
                }
            }
            if (++s > 3) {
                s = 0;
            }
        }
        ++cposx;
        ++cposz;
        makePart(world, rand, cposx, cposy, cposz + 7, 9, 5, 1, 1, 1);
        for (int i = 0; i < xwidth; ++i) {
            for (int k = 0; k < zwidth; ++k) {
                for (int j = 0; j < 4; ++j) {
                    Block blk = Blocks.AIR;
                    if (j == 0 || j == 3) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    if (i == 0 || i == xwidth - 1) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k + 2, blk);
                }
            }
        }
        makePart(world, rand, cposx + 7, cposy, cposz, 11, 6, 1, -1, 2);
        xwidth = 6;
        zwidth = 3;
        for (int i = 0; i < xwidth; ++i) {
            for (int k = 0; k < zwidth; ++k) {
                for (int j = 0; j < 4; ++j) {
                    Block blk = Blocks.AIR;
                    if (j == 0 || j == 3) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    if (k == 0 || k == zwidth - 1) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    FastSetBlock(world, cposx + i + 2, cposy + j, cposz - k, blk);
                }
            }
        }
        makePart(world, rand, cposx - 7, cposy, cposz, 13, 7, -1, 1, 3);
        xwidth = 6;
        zwidth = 3;
        for (int i = 0; i < xwidth; ++i) {
            for (int k = 0; k < zwidth; ++k) {
                for (int j = 0; j < 4; ++j) {
                    Block blk = Blocks.AIR;
                    if (j == 0 || j == 3) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    if (k == 0 || k == zwidth - 1) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    FastSetBlock(world, cposx - i - 2, cposy + j, cposz + k, blk);
                }
            }
        }
        makePart(world, rand, cposx, cposy, cposz - 7, 15, 8, -1, -1, 4);
        xwidth = 3;
        zwidth = 6;
        for (int i = 0; i < xwidth; ++i) {
            for (int k = 0; k < zwidth; ++k) {
                for (int j = 0; j < 4; ++j) {
                    Block blk = Blocks.AIR;
                    if (j == 0 || j == 3) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    if (i == 0 || i == xwidth - 1) {
                        blk = Blocks.LAPIS_ORE;
                    }
                    FastSetBlock(world, cposx - i, cposy + j, cposz - k - 2, blk);
                }
            }
        }
    }

    /**
     * {@code makePart} (:1734-1830): one room growing in direction (dx, dz) - quartz floor with an obsidian cross,
     * obsidian shell, {@code difficulty} pairs of Alien/WTF? spawners and up to four chests.
     */
    private static void makePart(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                 final int cposz, final int width, final int height, final int dx, final int dz,
                                 final int difficulty) {
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                for (int k = 0; k < width; ++k) {
                    FastSetBlock(world, cposx + i * dx, cposy + j, cposz + k * dz, Blocks.AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = 0;
            for (int k = 0; k < width; ++k) {
                Block blk = Blocks.QUARTZ_BLOCK;
                if (i == width / 2 || k == width / 2) {
                    blk = Blocks.OBSIDIAN;
                }
                FastSetBlock(world, cposx + i * dx, cposy + j, cposz + k * dz, blk);
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height;
            for (int k = 0; k < width; ++k) {
                final Block blk = Blocks.OBSIDIAN;
                FastSetBlock(world, cposx + i * dx, cposy + j, cposz + k * dz, blk);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                final Block blk = Blocks.OBSIDIAN;
                int k = 0;
                FastSetBlock(world, cposx + i * dx, cposy + j, cposz + k * dz, blk);
                k = width - 1;
                FastSetBlock(world, cposx + i * dx, cposy + j, cposz + k * dz, blk);
            }
        }
        for (int k = 0; k < width; ++k) {
            for (int j = 0; j < height; ++j) {
                int i = 0;
                FastSetBlock(world, cposx + i * dx, cposy + j, cposz + k * dz, Blocks.OBSIDIAN);
                i = width - 1;
                FastSetBlock(world, cposx + i * dx, cposy + j, cposz + k * dz, Blocks.OBSIDIAN);
            }
        }
        for (int j = 0; j < difficulty; ++j) {
            // :1781, :1792 PORT: nextInt(2) drawn without the tile entity guard (GenericDungeonHelpers, draw rule).
            int t = rand.nextInt(2);
            spawner(world, cposx + dx * width / 2, cposy + j + 2, cposz + dz * width / 2, t == 0 ? "Alien" : "WTF?");
            t = rand.nextInt(2);
            spawner(world, cposx + dx * width / 2 + dx, cposy + j + 2, cposz + dz * width / 2 + dz,
                    t == 0 ? "Alien" : "WTF?");
        }
        final WeightedRandomChestContent[] chestContents = AlienWTFContentsList;
        chest(world, rand, cposx + width * dx / 2, cposy + 1, cposz + dz, -1, chestContents, 3 + rand.nextInt(5));
        if (difficulty > 1) {
            chest(world, rand, cposx + width * dx / 2, cposy + 1, cposz + (width - 2) * dz, -1, chestContents,
                    3 + rand.nextInt(5));
        }
        if (difficulty > 2) {
            chest(world, rand, cposx + dx, cposy + 1, cposz + width / 2 * dz, -1, chestContents, 3 + rand.nextInt(5));
        }
        if (difficulty > 3) {
            chest(world, rand, cposx + (width - 2) * dx, cposy + 1, cposz + width / 2 * dz, -1, chestContents,
                    3 + rand.nextInt(5));
        }
    }

    /**
     * {@code makeEnderKnightDungeon} (:1832-1940): the Ender Knight Outpost, a hall of obsidian and end stone cross
     * sections with random shelves and two Ender Knight spawners. Extent x 0..12, y 0..5, z -2..6.
     */
    public static void makeEnderKnightDungeon(final StructureWriter world, final Random rand, int cposx,
                                              final int cposy, int cposz) {
        final int height = 6;
        int zwidth = 5;
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 5; ++k) {
                for (int j = 0; j < 5; ++j) {
                    FastSetBlock(world, cposx, cposy + j, cposz + k, Blocks.AIR);
                }
            }
            ++cposx;
        }
        zwidth = 5;
        for (int k = 0; k < zwidth; ++k) {
            for (int j = 0; j < height; ++j) {
                Block blk = Blocks.OBSIDIAN;
                if (k == 2 && j >= 1 && j <= 3) {
                    blk = Blocks.AIR;
                }
                FastSetBlock(world, cposx, cposy + j, cposz + k, blk);
            }
        }
        ++cposx;
        --cposz;
        zwidth = 7;
        for (int k = 0; k < zwidth; ++k) {
            for (int j = 0; j < height; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz + k, crossSection(k, j, zwidth, height));
            }
            if (k == 1 || k == 2 || k == zwidth - 3 || k == zwidth - 2) {
                makeShelves(world, rand, cposx, cposy + 1, cposz + k);
            }
        }
        --cposz;
        for (int m = 0; m < 5; ++m) {
            ++cposx;
            zwidth = 9;
            for (int k = 0; k < zwidth; ++k) {
                for (int j = 0; j < height; ++j) {
                    FastSetBlock(world, cposx, cposy + j, cposz + k, crossSection(k, j, zwidth, height));
                }
                if (k == 1 || k == 2 || k == zwidth - 3 || k == zwidth - 2) {
                    makeShelves(world, rand, cposx, cposy + 1, cposz + k);
                }
                if (m == 2 && k == 4) {
                    spawner(world, cposx, cposy + 2, cposz + k, "Ender Knight");
                    spawner(world, cposx, cposy + 3, cposz + k, "Ender Knight");
                }
            }
        }
        ++cposz;
        ++cposx;
        zwidth = 7;
        for (int k = 0; k < zwidth; ++k) {
            for (int j = 0; j < height; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz + k, crossSection(k, j, zwidth, height));
            }
            if (k == 1 || k == 2 || k == zwidth - 3 || k == zwidth - 2) {
                makeShelves(world, rand, cposx, cposy + 1, cposz + k);
            }
        }
        ++cposz;
        ++cposx;
        zwidth = 5;
        for (int k = 0; k < zwidth; ++k) {
            for (int j = 0; j < height; ++j) {
                final Block blk = Blocks.OBSIDIAN;
                FastSetBlock(world, cposx, cposy + j, cposz + k, blk);
            }
        }
    }

    /**
     * The block choice the three hall loops of {@code makeEnderKnightDungeon} repeat verbatim (:1859-1868,
     * :1881-1890, :1915-1924): air, obsidian floor and ceiling, end stone inside the floor, obsidian side walls.
     */
    private static Block crossSection(final int k, final int j, final int zwidth, final int height) {
        Block blk = Blocks.AIR;
        if (j == 0 || j == height - 1) {
            blk = Blocks.OBSIDIAN;
        }
        if (j == 0 && k > 0 && k < zwidth - 1) {
            blk = Blocks.END_STONE;
        }
        if (k == 0 || k == zwidth - 1) {
            blk = Blocks.OBSIDIAN;
        }
        return blk;
    }

    /**
     * {@code makeShelves} (:1942-1967): {@code nextInt(4)} - 0 a chest ({@code 3 + nextInt(5)} draws), 1 a bookshelf
     * column, 2 a cobweb column, each {@code 1 + nextInt(4)} high, 3 nothing.
     */
    private static void makeShelves(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                    final int cposz) {
        final int i = rand.nextInt(4);
        Block blk = Blocks.AIR;
        if (i == 0) {
            final WeightedRandomChestContent[] chestContents = KnightContentsList;
            chest(world, rand, cposx, cposy, cposz, -1, chestContents, 3 + rand.nextInt(5));
        }
        if (i == 1) {
            blk = Blocks.BOOKSHELF;
            for (int k = 1 + rand.nextInt(4), j = 0; j < k; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz, blk);
            }
        }
        if (i == 2) {
            blk = Blocks.COBWEB;
            for (int k = 1 + rand.nextInt(4), j = 0; j < k; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz, blk);
            }
        }
    }
}
