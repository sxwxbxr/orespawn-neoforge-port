package com.swbr.orespawn.world.dungeon.a;

import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.CloudSharkContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.FastSetBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.LeafMonsterContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.MiniContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.SquidContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.WaterDragonContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.block;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.chest;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.chestContentsList;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.doubleChestAlongX;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.emptyChest;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.generateChestContents;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.setBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.setChestSlot;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.setThisBlock;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.spawner;
import static com.swbr.orespawn.world.dungeon.GenericDungeonHelpers.stack;

import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The small builders of the first part of {@code GenericDungeon}: {@code makeDungeon} (GenericDungeon.java:132-217),
 * {@code makeRotatorStation} (:834-857), {@code makeHauntedHouse} (:938-1053), {@code makePlayPool} (:1969-1992),
 * {@code makeWaterDragonLair} (:1994-2084), {@code makeCloudSharkDungeon} (:2086-2118),
 * {@code makeLeafMonsterDungeon} (:2120-2260) and {@code makeMiniDungeon} (:2262-2433); verhalten/world-01.md,
 * section of the same names.
 */
public final class SmallDungeons {

    private SmallDungeons() {
    }

    /**
     * {@code makeDungeon} (:132-217): the Generic Dungeon, a 12x6x12 cobblestone room with one spawner of twelve
     * possible mobs and one chest. Extent x 0..11, y 0..5, z 0..11.
     */
    public static void makeDungeon(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                   final int cposz) {
        final int width = 12;
        final int height = 6;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                for (int k = 0; k < width; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = 0;
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.MOSSY_COBBLESTONE);
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height - 1;
            for (int k = 0; k < width; ++k) {
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int k = 0;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
                k = width - 1;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
            }
        }
        for (int k = 0; k < width; ++k) {
            for (int j = 0; j < height; ++j) {
                int i = 0;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
                i = width - 1;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
            }
        }
        // :170-210 PORT: nextInt(12) drawn whether or not the spawner block entity is reachable
        // (GenericDungeonHelpers, draw rule).
        final int t = rand.nextInt(12);
        final String[] mobs = {"Scorpion", "Alien", "Cryolophosaurus", "WTF?", "Kyuubi", "Bee", "Cloud Shark",
            "Lurking Terror", "Terrible Terror", "Rotator", "Rat", "Dungeon Beast"};
        spawner(world, cposx + width / 2, cposy + 1, cposz + width / 2, mobs[t]);
        chest(world, rand, cposx + width / 2, cposy + 1, cposz + 1, -1, chestContentsList, 5 + rand.nextInt(7));
    }

    /**
     * {@code makeRotatorStation} (:834-857): crystal stone, two Rotator spawners, crystal stone and a chest with a
     * Rotator egg stack and two crystal coal stacks, one column. Extent y 4..8.
     */
    public static void makeRotatorStation(final StructureWriter world, final Random rand, final int cposx,
                                          final int cposy, final int cposz) {
        final Block crystalStone = block("orespawn:crystalstone");
        setBlock(world, cposx, cposy + 4, cposz, crystalStone, 0, Block.UPDATE_CLIENTS);
        spawner(world, cposx, cposy + 5, cposz, "Rotator");
        spawner(world, cposx, cposy + 6, cposz, "Rotator");
        setBlock(world, cposx, cposy + 7, cposz, crystalStone, 0, Block.UPDATE_CLIENTS);
        emptyChest(world, cposx, cposy + 8, cposz, 2, Block.UPDATE_CLIENTS);
        setChestSlot(world, cposx, cposy + 8, cposz, 1, stack("orespawn:eggrotator", 1 + rand.nextInt(5)));
        setChestSlot(world, cposx, cposy + 8, cposz, 2, stack("orespawn:crystalcoal", 4 + rand.nextInt(16)));
        setChestSlot(world, cposx, cposy + 8, cposz, 3, stack("orespawn:crystalcoal", 4 + rand.nextInt(16)));
    }

    /**
     * {@code makeHauntedHouse} (:938-1053): a 7x5x7 plank house with a glass band, furnace, crafting table, a chest of
     * coin-flip survival gear and three spawners in one corner column. Extent x -3..3, y 0..4, z -3..3.
     */
    public static void makeHauntedHouse(final StructureWriter world, final Random rand, final int cposx,
                                        final int cposy, final int cposz) {
        int deltax = 0;
        final int deltaz = 0;
        int stuffdir = 0;
        final int width;
        final int length = width = 3;
        final int height = 3;
        deltax = 1;
        stuffdir = 2;
        final int x = cposx;
        final int z = cposz;
        final int y = cposy;
        // :954 world.isRemote - builders run on the server only.
        for (int i = -width; i <= width; ++i) {
            for (int j = -length; j <= length; ++j) {
                for (int k = 0; k <= height + 1; ++k) {
                    if (k == height + 1) {
                        setBlock(world, x + i, y + k, z + j, Blocks.OAK_PLANKS);
                    } else if (k == 0) {
                        setBlock(world, x + i, y + k, z + j, Blocks.COBBLESTONE);
                    } else if (i == width || j == length || i == -width || j == -length) {
                        if (k == height) {
                            setBlock(world, x + i, y + k, z + j, Blocks.GLASS);
                        } else if ((k == 1 || k == 2) && i == deltax * width && j == deltaz * length) {
                            setBlock(world, x + i, y + k, z + j, Blocks.AIR);
                        } else {
                            setBlock(world, x + i, y + k, z + j, Blocks.OAK_PLANKS);
                        }
                    } else {
                        setBlock(world, x + i, y + k, z + j, Blocks.AIR);
                    }
                }
            }
        }
        int i = 2;
        final int k = 1;
        final int j = length - 1;
        // :986-987 furnace, then setBlockMetadataWithNotify(stuffdir, 3)
        setBlock(world, x + i * deltax + j * deltaz, y + k, z + i * deltaz + j * deltax, Blocks.FURNACE, stuffdir,
                Block.UPDATE_ALL);
        i = 1;
        setBlock(world, x + i * deltax + j * deltaz, y + k, z + i * deltaz + j * deltax, Blocks.CRAFTING_TABLE);
        i = 0;
        final int cx = x + i * deltax + j * deltaz;
        final int cy = y + k;
        final int cz = z + i * deltaz + j * deltax;
        emptyChest(world, cx, cy, cz, stuffdir, Block.UPDATE_ALL);
        // :994-1036 each slot on its own coin flip. PORT: drawn without the chest != null guard
        // (GenericDungeonHelpers, draw rule).
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 0, stack(Items.COMPASS, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 1, stack(Items.MAP, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 2, stack(Items.COOKED_PORKCHOP, 8));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 3, stack(Items.TORCH, 32));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 4, stack(Items.COAL, 16));
        }
        if (rand.nextInt(2) == 0) {
            // Items.bed: the 1.7.10 bed item placed the red bed.
            setChestSlot(world, cx, cy, cz, 5, stack(Items.RED_BED, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 6, stack(Items.RED_BED, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 7, stack(Items.OAK_DOOR, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 8, stack(Items.IRON_PICKAXE, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 9, stack(Items.IRON_SWORD, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 10, stack(Items.IRON_AXE, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 11, stack(Items.BUCKET, 1));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 12, stack("orespawn:oresalt", 4));
        }
        if (rand.nextInt(2) == 0) {
            setChestSlot(world, cx, cy, cz, 13, stack(Items.CHEST, 1));
        }
        spawner(world, cposx, cposy + 1, cposz, "Rat");
        spawner(world, cposx, cposy + 2, cposz, "Ghost");
        spawner(world, cposx, cposy + 3, cposz, "Ghost Pumpkin Skelly");
    }

    /**
     * {@code makePlayPool} (:1969-1992): four Attack Squid spawners, a double chest and a strip of water sixteen blocks
     * above the sea. Extent x -1..4, y 16..18, z 0.
     */
    public static void makePlayPool(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                    final int cposz) {
        final WeightedRandomChestContent[] chestContents = SquidContentsList;
        for (int i = 0; i < 4; ++i) {
            spawner(world, cposx + i, cposy + 16, cposz, "Attack Squid");
        }
        doubleChestAlongX(world, cposx + 1, cposy + 17, cposz);
        generateChestContents(world, rand, chestContents, cposx + 1, cposy + 17, cposz, 3 + rand.nextInt(5));
        for (int i = 0; i < 4; ++i) {
            setBlock(world, cposx + i, cposy + 18, cposz, Blocks.WATER, 0, Block.UPDATE_ALL);
        }
        // :1990-1991 flowing_water metadata 0 is a source block.
        setBlock(world, cposx - 1, cposy + 18, cposz, Blocks.WATER, 0, Block.UPDATE_ALL);
        setBlock(world, cposx + 4, cposy + 18, cposz, Blocks.WATER, 0, Block.UPDATE_ALL);
    }

    /**
     * {@code makeWaterDragonLair} (:1994-2084): a bedrock disc with an iron ring and spokes on top of a glowstone,
     * lapis and egg-block wall of radius 10, round a sand island with a palm, four Water Dragon spawners and a chest.
     * Extent x -10..10, y -1..7, z -10..10.
     *
     * <p>The circles keep the original {@code (int)(cpos + r * cos + 0.5f)} float arithmetic and cast toward zero
     * (DECISIONS R18 and R20: the offset on negative coordinates is part of the original).
     */
    public static void makeWaterDragonLair(final StructureWriter world, final Random rand, final int cposx,
                                           final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = WaterDragonContentsList;
        for (float radius = 10.0f, currad = 0.0f; currad < radius; currad += 0.33f) {
            for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
                final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
                Block blk = Blocks.BEDROCK;
                if (currad > 5.0f && currad < 6.0f) {
                    blk = Blocks.IRON_BLOCK;
                }
                FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 7, (int) (cposz + curz + 0.5f), blk);
            }
        }
        for (int i = 1; i < 10; ++i) {
            FastSetBlock(world, (int) (cposx + i + 0.5f), cposy + 7, (int) (cposz + 0.5f), Blocks.IRON_BLOCK);
            FastSetBlock(world, (int) (cposx - i + 0.5f), cposy + 7, (int) (cposz + 0.5f), Blocks.IRON_BLOCK);
            FastSetBlock(world, (int) (cposx + 0.5f), cposy + 7, (int) (cposz + i + 0.5f), Blocks.IRON_BLOCK);
            FastSetBlock(world, (int) (cposx + 0.5f), cposy + 7, (int) (cposz - i + 0.5f), Blocks.IRON_BLOCK);
        }
        FastSetBlock(world, (int) (cposx + 0.5f), cposy + 7, (int) (cposz + 0.5f), Blocks.AIR);
        FastSetBlock(world, (int) (cposx + 1 + 0.5f), cposy + 7, (int) (cposz + 0.5f), Blocks.GLOWSTONE);
        FastSetBlock(world, (int) (cposx - 1 + 0.5f), cposy + 7, (int) (cposz + 0.5f), Blocks.GLOWSTONE);
        FastSetBlock(world, (int) (cposx + 0.5f), cposy + 7, (int) (cposz + 1 + 0.5f), Blocks.GLOWSTONE);
        FastSetBlock(world, (int) (cposx + 0.5f), cposy + 7, (int) (cposz - 1 + 0.5f), Blocks.GLOWSTONE);
        final float currad = 10.0f;
        final Block waterDragonEggBlock = block("orespawn:orewaterdragon");
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
            final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
            final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 1, (int) (cposz + curz + 0.5f), Blocks.GLOWSTONE);
            Block blk = Blocks.LAPIS_BLOCK;
            if (rand.nextInt(2) == 0) {
                blk = waterDragonEggBlock;
            }
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 2, (int) (cposz + curz + 0.5f), blk);
            blk = Blocks.LAPIS_BLOCK;
            if (rand.nextInt(2) == 0) {
                blk = waterDragonEggBlock;
            }
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 3, (int) (cposz + curz + 0.5f), blk);
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 4, (int) (cposz + curz + 0.5f), Blocks.GLOWSTONE);
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 5, (int) (cposz + curz + 0.5f), Blocks.BEDROCK);
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 6, (int) (cposz + curz + 0.5f), Blocks.BEDROCK);
        }
        for (int i = -3; i <= 3; ++i) {
            for (int j = -3; j <= 3; ++j) {
                FastSetBlock(world, cposx + i, cposy, cposz + j, Blocks.SAND);
                FastSetBlock(world, cposx + i, cposy - 1, cposz + j, Blocks.STONE);
            }
        }
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                FastSetBlock(world, cposx + i, cposy + 3, cposz + j, Blocks.OAK_LEAVES);
            }
        }
        FastSetBlock(world, cposx, cposy + 4, cposz, Blocks.OAK_LEAVES);
        FastSetBlock(world, cposx, cposy + 3, cposz, Blocks.OAK_LOG);
        FastSetBlock(world, cposx, cposy + 2, cposz, Blocks.OAK_LOG);
        FastSetBlock(world, cposx, cposy + 1, cposz, Blocks.OAK_LOG);
        FastSetBlock(world, cposx + 1, cposy + 3, cposz + 1, Blocks.OAK_LOG);
        FastSetBlock(world, cposx - 1, cposy + 3, cposz - 1, Blocks.OAK_LOG);
        FastSetBlock(world, cposx + 1, cposy + 3, cposz - 1, Blocks.OAK_LOG);
        FastSetBlock(world, cposx - 1, cposy + 3, cposz + 1, Blocks.OAK_LOG);
        spawner(world, cposx + 1, cposy + 3, cposz, "Water Dragon");
        spawner(world, cposx - 1, cposy + 3, cposz, "Water Dragon");
        spawner(world, cposx, cposy + 3, cposz + 1, "Water Dragon");
        spawner(world, cposx, cposy + 3, cposz - 1, "Water Dragon");
        chest(world, rand, cposx, cposy + 1, cposz - 1, -1, chestContents, 4 + rand.nextInt(5));
    }

    /**
     * {@code makeCloudSharkDungeon} (:2086-2118): two glowstone blocks, four Cloud Shark spawners and a chest in the
     * sky. Extent -1..1 on every axis.
     */
    public static void makeCloudSharkDungeon(final StructureWriter world, final Random rand, final int cposx,
                                             final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = CloudSharkContentsList;
        FastSetBlock(world, cposx, cposy, cposz, Blocks.GLOWSTONE);
        FastSetBlock(world, cposx, cposy - 1, cposz, Blocks.GLOWSTONE);
        spawner(world, cposx + 1, cposy, cposz, "Cloud Shark");
        spawner(world, cposx - 1, cposy, cposz, "Cloud Shark");
        spawner(world, cposx, cposy, cposz + 1, "Cloud Shark");
        spawner(world, cposx, cposy, cposz - 1, "Cloud Shark");
        chest(world, rand, cposx, cposy + 1, cposz, -1, chestContents, 4 + rand.nextInt(5));
    }

    /**
     * {@code makeLeafMonsterDungeon} (:2120-2260): a hollow 4x4 oak trunk with ladders, a platform and a leaf crown,
     * four Leaf Monster spawners and a double chest. Extent x -3..6, y -4..16, z -3..6.
     */
    public static void makeLeafMonsterDungeon(final StructureWriter world, final Random rand, final int cposx,
                                              final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = LeafMonsterContentsList;
        for (int i = -2; i < 6; ++i) {
            for (int k = -3; k < 2; ++k) {
                for (int j = 0; j < 4; ++j) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR);
                }
            }
        }
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 4; ++k) {
                for (int j = -1; j > -5; --j) {
                    final Block blk = Blocks.OAK_LOG;
                    final BlockState bid = world.getBlock(cposx + i, cposy + j, cposz + k);
                    // :2137 bid == Blocks.air || bid == Blocks.tallgrass. PORT: tallgrass is the flattened family
                    // dead_bush (meta 0), short_grass (meta 1) and fern (meta 2) (DECISIONS R22).
                    if (bid.isAir() || bid.is(Blocks.SHORT_GRASS) || bid.is(Blocks.FERN) || bid.is(Blocks.DEAD_BUSH)) {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                    }
                }
            }
        }
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 4; ++k) {
                for (int j = 0; j < 10; ++j) {
                    Block blk = Blocks.OAK_LOG;
                    if (j < 2 && (k == 0 || k == 1) && (i == 1 || i == 2)) {
                        blk = Blocks.AIR;
                    }
                    if (k == 1 && (i == 1 || i == 2)) {
                        blk = Blocks.AIR;
                    }
                    if (k == 2) {
                        if (i == 1) {
                            continue;
                        }
                        if (i == 2) {
                            continue;
                        }
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 4; ++k) {
                for (int j = 0; j < 10; ++j) {
                    if (k == 2 && (i == 1 || i == 2)) {
                        final Block blk = Blocks.LADDER;
                        setBlock(world, cposx + i, cposy + j, cposz + k, blk, 2, Block.UPDATE_ALL);
                    }
                }
            }
        }
        FastSetBlock(world, cposx + 1, cposy + 2, cposz - 1, Blocks.OAK_LEAVES);
        FastSetBlock(world, cposx + 2, cposy + 2, cposz - 1, Blocks.OAK_LEAVES);
        for (int i = -3; i < 7; ++i) {
            for (int k = -3; k < 7; ++k) {
                final int j = 9;
                if (i < 0 || i > 3 || k < 0 || k > 3) {
                    Block blk = Blocks.OAK_LOG;
                    if (i == -3 || i == 6 || k == -3 || k == 6) {
                        blk = Blocks.OAK_LEAVES;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        for (int i = -3; i < 7; ++i) {
            for (int k = -3; k < 7; ++k) {
                for (int j = 10; j < 13; ++j) {
                    Block blk = Blocks.AIR;
                    if (i == -3 || i == 6 || k == -3 || k == 6) {
                        blk = Blocks.OAK_LEAVES;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        for (int i = -2; i < 6; ++i) {
            for (int k = -2; k < 6; ++k) {
                final int j = 13;
                Block blk = Blocks.AIR;
                if (i == -2 || i == 5 || k == -2 || k == 5) {
                    blk = Blocks.OAK_LOG;
                }
                if (i == -1 || i == 4 || k == -1 || k == 4) {
                    blk = Blocks.OAK_LEAVES;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = -1; i < 5; ++i) {
            for (int k = -1; k < 5; ++k) {
                final int j = 14;
                final Block blk = Blocks.OAK_LEAVES;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 4; ++k) {
                final int j = 15;
                final Block blk = Blocks.OAK_LOG;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = 1; i < 3; ++i) {
            for (int k = 1; k < 3; ++k) {
                final int j = 16;
                final Block blk = Blocks.OAK_LEAVES;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        spawner(world, cposx - 2, cposy + 10, cposz - 2, "Leaf Monster");
        spawner(world, cposx + 5, cposy + 10, cposz + 5, "Leaf Monster");
        spawner(world, cposx - 2, cposy + 10, cposz + 5, "Leaf Monster");
        spawner(world, cposx + 5, cposy + 10, cposz - 2, "Leaf Monster");
        doubleChestAlongX(world, cposx + 1, cposy + 10, cposz + 5);
        generateChestContents(world, rand, chestContents, cposx + 1, cposy + 10, cposz + 5, 12 + rand.nextInt(5));
    }

    /**
     * {@code makeMiniDungeon} (:2262-2433): an iron-bar cage with a grass roof, a plank stair with fences and torches,
     * twelve Butterfly spawners on the roof, four corner pillars with spawners, six floor spawners and a chest. Extent
     * x -6..9, y 0..11, z 0..9.
     */
    public static void makeMiniDungeon(final StructureWriter world, final Random rand, final int cposx,
                                       final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = MiniContentsList;
        for (int i = 0; i < 10; ++i) {
            for (int k = 0; k < 10; ++k) {
                for (int j = 0; j < 7; ++j) {
                    Block blk = Blocks.AIR;
                    if (i == 0 || k == 0 || i == 9 || k == 9) {
                        blk = Blocks.IRON_BARS;
                    }
                    if (i == 0 && k == 0) {
                        blk = Blocks.COBBLESTONE;
                    }
                    if (i == 9 && k == 9) {
                        blk = Blocks.COBBLESTONE;
                    }
                    if (i == 0 && k == 9) {
                        blk = Blocks.COBBLESTONE;
                    }
                    if (i == 9 && k == 0) {
                        blk = Blocks.COBBLESTONE;
                    }
                    if (j == 0) {
                        blk = Blocks.COBBLESTONE;
                    }
                    if (j == 6 && (i == 0 || k == 0 || i == 9 || k == 9)) {
                        blk = Blocks.COBBLESTONE;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        for (int i = 1; i < 9; ++i) {
            for (int k = 1; k < 9; ++k) {
                final int j = 7;
                Block blk = Blocks.AIR;
                if (i == 1 || i == 8 || k == 1 || k == 8) {
                    blk = Blocks.GRASS_BLOCK;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = 2; i < 8; ++i) {
            for (int k = 2; k < 8; ++k) {
                final int j = 8;
                Block blk = Blocks.AIR;
                if (i == 2 || i == 7 || k == 2 || k == 7) {
                    blk = Blocks.GRASS_BLOCK;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        int i = -6;
        int j = 1;
        int k = 3;
        for (int m = 0; m < 6; ++m) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.OAK_PLANKS);
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + 1, Blocks.OAK_PLANKS);
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + 2, Blocks.OAK_PLANKS);
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + 3, Blocks.OAK_PLANKS);
            FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, Blocks.OAK_FENCE);
            FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + 3, Blocks.OAK_FENCE);
            FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k, Blocks.TORCH);
            FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k + 3, Blocks.TORCH);
            ++i;
            ++j;
        }
        for (i = 3; i < 7; ++i) {
            for (k = 3; k < 7; ++k) {
                j = 9;
                if (i == 3 || i == 6 || k == 3 || k == 6) {
                    spawner(world, cposx + i, cposy + j, cposz + k, "Butterfly");
                }
            }
        }
        k = (i = 0);
        for (j = 7; j < 11; ++j) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.COBBLESTONE);
        }
        spawner(world, cposx + i, cposy + j, cposz + k, "Terrible Terror");
        k = (i = 9);
        for (j = 7; j < 11; ++j) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.COBBLESTONE);
        }
        spawner(world, cposx + i, cposy + j, cposz + k, "Butterfly");
        i = 0;
        k = 9;
        for (j = 7; j < 11; ++j) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.COBBLESTONE);
        }
        spawner(world, cposx + i, cposy + j, cposz + k, "Terrible Terror");
        i = 9;
        k = 0;
        for (j = 7; j < 11; ++j) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.COBBLESTONE);
        }
        spawner(world, cposx + i, cposy + j, cposz + k, "Butterfly");
        k = (i = 1);
        j = 1;
        spawner(world, cposx + i, cposy + j, cposz + k, "Terrible Terror");
        k = (i = 8);
        j = 1;
        spawner(world, cposx + i, cposy + j, cposz + k, "Terrible Terror");
        i = 8;
        k = 1;
        j = 1;
        spawner(world, cposx + i, cposy + j, cposz + k, "Butterfly");
        i = 1;
        k = 8;
        j = 1;
        spawner(world, cposx + i, cposy + j, cposz + k, "Butterfly");
        i = 4;
        k = 4;
        j = 1;
        spawner(world, cposx + i, cposy + j, cposz + k, "Lurking Terror");
        i = 5;
        k = 5;
        j = 1;
        spawner(world, cposx + i, cposy + j, cposz + k, "Lurking Terror");
        chest(world, rand, cposx + 3, cposy + 1, cposz + 3, -1, chestContents, 4 + rand.nextInt(5));
    }
}
