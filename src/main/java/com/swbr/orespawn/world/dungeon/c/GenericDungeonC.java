package com.swbr.orespawn.world.dungeon.c;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.dungeon.GenericDungeonHelpers;
import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * Port of the {@code danger.orespawn.GenericDungeon} build methods whose bodies start on lines 4867-5733,
 * 6056-6419 and 7032-7111 (W13 split, porter c): Crystal Battle Tower, Girlfriend and Monster Island, Greenhouse,
 * Nightmare Rookery, Stinky House, Rubber Ducky Pond, White House with its six private parts, Frog Pond, Pumpkin,
 * Round Rotator, Rainbow, Spider Hangout and Red Ant Hangout. The Queen altar and the Queen castle live in
 * {@link GenericDungeonQueen}.
 *
 * <p>Every method keeps the original name and takes the W12 {@link StructureWriter} in place of {@code World} and the
 * {@code Random} in place of {@code world.rand}; the order of every block write and every random draw is the
 * original's, so a seeded {@code Random} rebuilds the same structure in every chunk run (DECISIONS R12, R18).
 *
 * <p>Write paths, as in {@code FastBlocks}: {@code this.FastSetBlock} and {@code OreSpawnMain.setBlockFast(..., 2)}
 * become {@code world.setBlock(x, y, z, state)} (flag 2); {@code world.setBlock(block, meta, 3)} and the three-argument
 * {@code world.setBlock} keep flag 3. PORT: {@code this.FastSetBlock} is a one-line helper of the original
 * (GenericDungeon.java:219-221) and is inlined instead of called through {@code GenericDungeonHelpers}.
 * {@code world.setBlock(mob_spawner)} plus {@code setEntityName} is {@link StructureWriter#setSpawner}, with the
 * legacy entity name mapped to its registry id (verhalten/world-01.md, "Spawner- und Entity-Namen").
 *
 * <p>Loot lists and the picture arrays are constructor state of the original and come from
 * {@link GenericDungeonHelpers} under their original field names.
 */
public final class GenericDungeonC {

    private GenericDungeonC() {
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeCrystalBattleTower (:4867-4991)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeCrystalBattleTower} (:4867-4991): five floors of r 10 in crystal stone, a spawner pair and a chest each. */
    public static void makeCrystalBattleTower(final StructureWriter world, final Random random,
                                              final int cposx, final int cposy, final int cposz) {
        WeightedRandomChestContent[] chestContents = null;
        float radius = 10.0f;
        for (int j = 0; j <= 20; ++j) {
            BlockState blk = crystalStone();
            if (j % 5 == 0) {
                for (float currad = 0.0f; currad < radius; currad += 0.33f) {
                    for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                        final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
                        final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
                        // (int) cast toward zero kept 1:1 (verhalten/world-01.md "Kreise", DECISIONS R20 exception)
                        world.setBlock((int) (cposx + curx + 0.5f), cposy + j, (int) (cposz + curz + 0.5f), blk);
                    }
                }
            } else {
                final float currad = 10.0f;
                for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                    final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
                    final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
                    blk = crystalStone();
                    if (j % 5 >= 1 && j % 5 <= 3 && (curdeg < 10.0f || curdeg > 350.0f)) {
                        blk = AIR;
                    }
                    world.setBlock((int) (cposx + curx + 0.5f), cposy + j, (int) (cposz + curz + 0.5f), blk);
                }
            }
        }
        radius = 10.0f;
        for (int j = 21; j <= 22; ++j) {
            final BlockState blk = ModBlocks.CRYSTALCRYSTAL.get().defaultBlockState();
            final float currad = 10.0f;
            for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
                final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
                final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
                world.setBlock((int) (cposx + curx + 0.5f), cposy + j, (int) (cposz + curz + 0.5f), blk);
            }
        }
        int j = 1;
        chestContents = GenericDungeonHelpers.CrystalBattleTowerRatContentsList;
        world.setSpawner(cposx, cposy + j + 1, cposz, RAT);
        world.setSpawner(cposx, cposy + j + 2, cposz, RAT);
        world.setChest(cposx, cposy + j, cposz, -1, chestContents, 5 + random.nextInt(5), random);
        j = 6;
        chestContents = GenericDungeonHelpers.CrystalBattleTowerDungeonBeastContentsList;
        world.setSpawner(cposx, cposy + j + 1, cposz, DUNGEON_BEAST);
        world.setSpawner(cposx, cposy + j + 2, cposz, DUNGEON_BEAST);
        world.setChest(cposx, cposy + j, cposz, -1, chestContents, 5 + random.nextInt(5), random);
        j = 11;
        chestContents = GenericDungeonHelpers.CrystalBattleTowerUrchinContentsList;
        world.setSpawner(cposx, cposy + j + 1, cposz, "orespawn:crystal_urchin");
        world.setSpawner(cposx, cposy + j + 2, cposz, "orespawn:crystal_urchin");
        world.setChest(cposx, cposy + j, cposz, -1, chestContents, 5 + random.nextInt(5), random);
        j = 16;
        chestContents = GenericDungeonHelpers.CrystalBattleTowerRotatorContentsList;
        world.setSpawner(cposx, cposy + j + 1, cposz, ROTATOR);
        world.setSpawner(cposx, cposy + j + 2, cposz, ROTATOR);
        world.setChest(cposx, cposy + j, cposz, -1, chestContents, 5 + random.nextInt(5), random);
        j = 21;
        chestContents = GenericDungeonHelpers.CrystalBattleTowerVortexContentsList;
        world.setSpawner(cposx, cposy + j + 1, cposz, "orespawn:vortex");
        world.setSpawner(cposx, cposy + j + 2, cposz, "orespawn:vortex");
        world.setChest(cposx, cposy + j, cposz, -1, chestContents, 6 + random.nextInt(6), random);
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeGirlfriendIsland (:4993-5057) and makeMonsterIsland (:5196-5264)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeGirlfriendIsland} (:4993-5057): sand island with a palm, four spawners in the crown, two chests. */
    public static void makeGirlfriendIsland(final StructureWriter world, final Random random,
                                            final int cposx, final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.DamselContentsList;
        island(world, cposx, cposy, cposz);
        world.setSpawner(cposx + 1, cposy + 3, cposz, "orespawn:girlfriend");
        world.setSpawner(cposx - 1, cposy + 3, cposz, "orespawn:boyfriend");
        world.setSpawner(cposx, cposy + 3, cposz + 1, "orespawn:gold_fish");
        world.setSpawner(cposx, cposy + 3, cposz - 1, "orespawn:gold_fish");
        world.setChest(cposx, cposy + 1, cposz - 1, -1, chestContents, 4 + random.nextInt(5), random);
        world.setChest(cposx, cposy + 1, cposz + 1, -1, chestContents, 4 + random.nextInt(5), random);
    }

    /** {@code makeMonsterIsland} (:5196-5264): the Girlfriend island with four Sea Viper or four Sea Monster spawners. */
    public static void makeMonsterIsland(final StructureWriter world, final Random random,
                                         final int cposx, final int cposy, final int cposz) {
        String monster = "orespawn:sea_viper";
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.MonsterIslandContentsList;
        if (random.nextInt(2) == 0) {
            monster = "orespawn:sea_monster";
        }
        island(world, cposx, cposy, cposz);
        world.setSpawner(cposx + 1, cposy + 3, cposz, monster);
        world.setSpawner(cposx - 1, cposy + 3, cposz, monster);
        world.setSpawner(cposx, cposy + 3, cposz + 1, monster);
        world.setSpawner(cposx, cposy + 3, cposz - 1, monster);
        world.setChest(cposx, cposy + 1, cposz - 1, -1, chestContents, 4 + random.nextInt(5), random);
        world.setChest(cposx, cposy + 1, cposz + 1, -1, chestContents, 4 + random.nextInt(5), random);
    }

    /**
     * The island and palm block writes shared word for word by :4998-5026 and :5205-5233. PORT: extracted into one
     * method because the two original bodies are textually identical; order and positions unchanged, no random draw.
     */
    private static void island(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        for (int i = -5; i <= 5; ++i) {
            int k = 3;
            if (i == -5 || i == 5) {
                k = 1;
            }
            if (i == -4 || i == 4) {
                k = 2;
            }
            if (i == -3 || i == 3) {
                k = 2;
            }
            for (int j = -k; j <= k; ++j) {
                world.setBlock(cposx + i, cposy, cposz + j, Blocks.SAND.defaultBlockState());
                world.setBlock(cposx + i, cposy - 1, cposz + j, Blocks.STONE.defaultBlockState());
            }
        }
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                world.setBlock(cposx + i, cposy + 3, cposz + j, LEAVES);
            }
        }
        world.setBlock(cposx, cposy + 4, cposz, LEAVES);
        world.setBlock(cposx, cposy + 3, cposz, LOG);
        world.setBlock(cposx, cposy + 2, cposz, LOG);
        world.setBlock(cposx, cposy + 1, cposz, LOG);
        world.setBlock(cposx + 1, cposy + 3, cposz + 1, LOG);
        world.setBlock(cposx - 1, cposy + 3, cposz - 1, LOG);
        world.setBlock(cposx + 1, cposy + 3, cposz - 1, LOG);
        world.setBlock(cposx - 1, cposy + 3, cposz + 1, LOG);
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeGreenhouseDungeon (:5059-5194)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeGreenhouseDungeon} (:5059-5194): glass house with iron roof, planted beds, Triffid spawners on the roof. */
    public static void makeGreenhouseDungeon(final StructureWriter world, final Random random,
                                             final int cposx, final int cposy, final int cposz) {
        final int height = 7;
        final int width = 15;
        final int length = 23;
        int t = 0;
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.GreenhouseContentsList;
        for (int i = 0; i < length; ++i) {
            for (int k = 0; k < width; ++k) {
                for (int j = 0; j < height; ++j) {
                    BlockState blk = AIR;
                    if (i == 0 || k == 0 || i == length - 1 || k == width - 1) {
                        blk = Blocks.GLASS.defaultBlockState();
                    }
                    if (j == height - 1) {
                        blk = Blocks.IRON_BLOCK.defaultBlockState();
                        if (i % 4 == 3 && k % 4 == 3) {
                            blk = Blocks.GLOWSTONE.defaultBlockState();
                        }
                        if (k % 4 == 1) {
                            blk = Blocks.GLASS.defaultBlockState();
                        }
                    }
                    if (j == 0) {
                        blk = Blocks.GRASS_BLOCK.defaultBlockState();
                        if (i != 0 && k != 0 && i != length - 1 && k != width - 1 && i % 3 == 2) {
                            blk = Blocks.WATER.defaultBlockState();
                        }
                    }
                    if (j == 1 && i != 0 && k != 0 && i != length - 1 && k != width - 1 && i % 3 != 2
                            && random.nextInt(3) != 1) {
                        blk = Blocks.FARMLAND.defaultBlockState();
                        world.setBlock(cposx + i, cposy + j - 1, cposz + k, blk);
                        t = random.nextInt(20);
                        blk = AIR;
                        if (t == 0) {
                            blk = Blocks.DANDELION.defaultBlockState();
                        }
                        if (t == 1) {
                            // Blocks.red_flower meta 0 = poppy
                            blk = Blocks.POPPY.defaultBlockState();
                        }
                        if (t == 2) {
                            blk = Blocks.BROWN_MUSHROOM.defaultBlockState();
                        }
                        if (t == 3) {
                            blk = Blocks.RED_MUSHROOM.defaultBlockState();
                        }
                        if (t == 4) {
                            blk = Blocks.WHEAT.defaultBlockState();
                        }
                        if (t == 5) {
                            blk = Blocks.CARROTS.defaultBlockState();
                        }
                        if (t == 6) {
                            blk = Blocks.POTATOES.defaultBlockState();
                        }
                        if (t == 7) {
                            // Blocks.reeds
                            blk = Blocks.SUGAR_CANE.defaultBlockState();
                        }
                        if (t == 9) {
                            blk = ModBlocks.CORN_0.get().defaultBlockState();
                        }
                        if (t == 10) {
                            blk = ModBlocks.TOMATO_0.get().defaultBlockState();
                        }
                        if (t == 11) {
                            blk = ModBlocks.STRAWBERRY_PLANT.get().defaultBlockState();
                        }
                        if (t == 12) {
                            blk = ModBlocks.BUTTERFLY_PLANT.get().defaultBlockState();
                        }
                        if (t == 13) {
                            blk = ModBlocks.MOTH_PLANT.get().defaultBlockState();
                        }
                        if (t == 14) {
                            blk = ModBlocks.RADISH_PLANT.get().defaultBlockState();
                        }
                        if (t == 15) {
                            blk = ModBlocks.LETTUCE_0.get().defaultBlockState();
                        }
                        if (t == 16) {
                            blk = ModBlocks.FLOWER_PINK.get().defaultBlockState();
                        }
                        if (t == 17) {
                            blk = ModBlocks.FLOWER_BLUE.get().defaultBlockState();
                        }
                        if (t == 18) {
                            blk = ModBlocks.QUINOA_0.get().defaultBlockState();
                        }
                        if (t == 19) {
                            blk = ModBlocks.RICE_PLANT.get().defaultBlockState();
                        }
                    }
                    world.setBlock(cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        for (int i = 0; i < length; ++i) {
            for (int k = 0; k < width; ++k) {
                for (int j = height; j <= height + 6; ++j) {
                    world.setBlock(cposx + i, cposy + j, cposz + k, AIR);
                }
            }
        }
        world.setBlock(cposx + width / 2, cposy + 1, cposz, AIR);
        world.setBlock(cposx + width / 2, cposy + 2, cposz, AIR);
        world.setBlock(cposx + width / 2 - 1, cposy + 1, cposz, AIR);
        world.setBlock(cposx + width / 2 - 1, cposy + 2, cposz, AIR);
        LegacyDoor.placeDoorBlock(world, cposx + width / 2, cposy + 1, cposz, 3, Blocks.IRON_DOOR);
        LegacyDoor.placeDoorBlock(world, cposx + width / 2 - 1, cposy + 1, cposz, 3, Blocks.IRON_DOOR);
        world.setBlock(cposx + width / 2 - 2, cposy + 2, cposz, Blocks.STONE.defaultBlockState());
        world.setBlock(cposx + width / 2 + 1, cposy + 2, cposz, Blocks.STONE.defaultBlockState());
        world.setBlock(cposx + width / 2 - 2, cposy + 2, cposz - 1, LegacyMeta.state(Blocks.STONE_BUTTON, 4), Block.UPDATE_CLIENTS);
        world.setBlock(cposx + width / 2 + 1, cposy + 2, cposz - 1, LegacyMeta.state(Blocks.STONE_BUTTON, 4), Block.UPDATE_CLIENTS);
        final int i = length / 2;
        final int k = width / 2;
        int j = height + 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:triffid");
        j = height + 2;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:triffid");
        j = height;
        world.setChest(cposx + i, cposy + j, cposz + k, -1, chestContents, 5 + random.nextInt(5), random);
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeNightmareRookery (:5266-5348)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeNightmareRookery} (:5266-5348): two passes of wandering stone columns, a nest on every column of 19+. */
    public static void makeNightmareRookery(final StructureWriter world, final Random random,
                                            final int cposx, final int cposy, final int cposz) {
        final String monster = "orespawn:nightmare";
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.NightmareRookeryContentsList;
        int h;
        int k;
        // :5274, decompiler form "for (int j = (k = (h = 0)), i = -5; ...)": j, k and h start at 0.
        h = 0;
        k = 0;
        for (int i = -5; i <= 20; ++i) {
            k += random.nextInt(3) - 1;
            h = random.nextInt(20) + 1;
            rookeryColumn(world, random, cposx, cposy, cposz, i, k, h, monster, chestContents);
        }
        for (int i = -5; i <= 20; ++i) {
            k += random.nextInt(3) - 1;
            h = random.nextInt(20) + 1;
            rookeryColumn(world, random, cposx, cposy, cposz, i, k, h, monster, chestContents);
        }
    }

    /**
     * The {@code while (j < h)} body shared word for word by :5277-5309 and :5314-5346. PORT: extracted because both
     * bodies are identical; the draw order per height stays stone, four side draws, then the nest check.
     */
    private static void rookeryColumn(final StructureWriter world, final Random random, final int cposx, final int cposy,
                                      final int cposz, final int i, final int k, final int h, final String monster,
                                      final WeightedRandomChestContent[] chestContents) {
        final BlockState stone = Blocks.STONE.defaultBlockState();
        int j = 0;
        while (j < h) {
            world.setBlock(cposx + i, cposy + j, cposz + k, stone);
            if (random.nextInt(j + 5) == 1) {
                world.setBlock(cposx + i + 1, cposy + j, cposz + k, stone);
            }
            if (random.nextInt(j + 5) == 1) {
                world.setBlock(cposx + i - 1, cposy + j, cposz + k, stone);
            }
            if (random.nextInt(j + 5) == 1) {
                world.setBlock(cposx + i, cposy + j, cposz + k + 1, stone);
            }
            if (random.nextInt(j + 5) == 1) {
                world.setBlock(cposx + i, cposy + j, cposz + k - 1, stone);
            }
            if (j >= 18) {
                world.setSpawner(cposx + i, cposy + j + 2, cposz + k, monster);
                world.setChest(cposx + i, cposy + j + 1, cposz + k, -1, chestContents, 4 + random.nextInt(5), random);
                break;
            }
            ++j;
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeStinkyHouse (:5350-5414)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeStinkyHouse} (:5350-5414): fenced yard with dead bushes, a decayed plank house, two spawners, a chest. */
    public static void makeStinkyHouse(final StructureWriter world, final Random random,
                                       final int cposx, final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.StinkyHouseContentsList;
        final int height = 2;
        final int width = 9;
        final int length = 12;
        final int yardwidth = 16;
        final int yardlength = 24;
        final Block fence = Blocks.OAK_FENCE;
        for (int i = 0; i <= yardlength; ++i) {
            for (int k = 0; k <= yardwidth; ++k) {
                Block bid = Blocks.AIR;
                if (i == 0 || i == yardlength || k == 0 || k == yardwidth) {
                    bid = fence;
                }
                if (bid == fence && random.nextInt(3) == 1) {
                    bid = Blocks.AIR;
                }
                if (bid == Blocks.AIR && random.nextInt(10) == 1) {
                    bid = Blocks.DEAD_BUSH;
                }
                if (bid != Blocks.AIR) {
                    world.setBlock(cposx + i - 5, cposy + 1, cposz + k - 4, bid.defaultBlockState());
                }
            }
        }
        for (int i = 0; i <= length; ++i) {
            for (int k = 0; k <= width; ++k) {
                for (int j = 0; j <= height; ++j) {
                    Block bid = Blocks.AIR;
                    if (i == 0 || i == length || k == 0 || k == width) {
                        bid = Blocks.OAK_PLANKS;
                    }
                    if (bid == Blocks.OAK_PLANKS && j == 1 && (i == 1 || i == length - 1 || k == 1 || k == width - 1)) {
                        bid = Blocks.GLASS_PANE;
                    }
                    if (j == height) {
                        bid = Blocks.OAK_PLANKS;
                    }
                    if (random.nextInt(10) == 1) {
                        bid = Blocks.AIR;
                    }
                    if ((j == 0 || j == 1) && i == 0 && (k == width / 2 || k == width / 2 + 1)) {
                        bid = Blocks.AIR;
                    }
                    world.setBlock(cposx + i, cposy + j + 1, cposz + k, bid.defaultBlockState());
                }
            }
        }
        world.setSpawner(cposx + 2, cposy + 1, cposz + 2, "orespawn:stink_bug");
        world.setSpawner(cposx + length - 2, cposy + 1, cposz + width - 2, "orespawn:stinky");
        world.setChest(cposx + length / 2, cposy + 1, cposz + width / 2, -1, chestContents, 8 + random.nextInt(5), random);
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeRubberDuckyPond (:5416-5454)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeRubberDuckyPond} (:5416-5454): duck spawners over a double chest and a water column, sand-rimmed pond. */
    public static void makeRubberDuckyPond(final StructureWriter world, final Random random,
                                           final int cposx, final int cposy, final int cposz) {
        BlockState bid = AIR;
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.RubberDuckyContentsList;
        for (int i = 0; i < 2; ++i) {
            world.setSpawner(cposx + i, cposy + 6, cposz, "orespawn:rubber_ducky");
        }
        // world.setBlock(chest, 0, 2) at x and x + 1, which 1.7.10 joined into one double chest; only the east half
        // (x + 1) is filled. The join goes through the shared helper (see its PORT note).
        GenericDungeonHelpers.doubleChestAlongX(world, cposx, cposy + 5, cposz);
        world.fillChest(cposx + 1, cposy + 5, cposz, chestContents, 8 + random.nextInt(5), random);
        world.setBlock(cposx, cposy + 4, cposz, Blocks.GLASS.defaultBlockState());
        world.setBlock(cposx + 1, cposy + 4, cposz, Blocks.GLASS.defaultBlockState());
        for (int i = 0; i < 2; ++i) {
            world.setBlock(cposx + i, cposy + 3, cposz, WATER, Block.UPDATE_ALL);
        }
        // Blocks.flowing_water meta 0: a full flowing block is a source level in 1.21.1 as well.
        world.setBlock(cposx - 1, cposy + 3, cposz, WATER, Block.UPDATE_ALL);
        world.setBlock(cposx + 2, cposy + 3, cposz, WATER, Block.UPDATE_ALL);
        for (int i = 0; i < 12; ++i) {
            for (int k = 0; k < 11; ++k) {
                bid = WATER;
                if (i == 0 || k == 0 || i == 11 || k == 10) {
                    bid = Blocks.SAND.defaultBlockState();
                }
                world.setBlock(cposx + i - 5, cposy, cposz + k - 5, bid);
                bid = AIR;
                world.setBlock(cposx + i - 5, cposy + 1, cposz + k - 5, bid);
                world.setBlock(cposx + i - 5, cposy + 2, cposz + k - 5, bid);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeWhiteHouse (:5456-5467) and its parts (:5469-5732)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeWhiteHouse} (:5456-5467): two fountains, walkway, base, walls, roof and interior, in that order. */
    public static void makeWhiteHouse(final StructureWriter world, final Random random,
                                      final int cposx, final int cposy, final int cposz) {
        makefountain(world, cposx - 5, cposy, cposz - 15);
        makefountain(world, cposx + 15, cposy, cposz - 15);
        makewalkway(world, cposx + 7, cposy, cposz - 15);
        makewhbase(world, cposx - 4, cposy, cposz - 6);
        makewhwalls(world, cposx - 3, cposy + 2, cposz - 5);
        makewhroof(world, cposx - 4, cposy, cposz - 6);
        makewhinterior(world, random, cposx - 1, cposy + 2, cposz - 3);
    }

    /** {@code makefountain} (:5469-5497). */
    private static void makefountain(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        BlockState bid = AIR;
        for (int i = 0; i < 7; ++i) {
            for (int k = 0; k < 5; ++k) {
                for (int j = 0; j < 15; ++j) {
                    bid = WATER;
                    if (i == 0 || k == 0 || i == 6 || k == 4) {
                        bid = QUARTZ;
                    }
                    if (j == 0) {
                        bid = QUARTZ;
                    }
                    if (j == 1 && i == 3 && k == 2) {
                        bid = Blocks.GLOWSTONE.defaultBlockState();
                    }
                    if (j > 1) {
                        bid = AIR;
                        if (j <= 4 && i == 3 && k == 2) {
                            bid = QUARTZ;
                        }
                    }
                    world.setBlock(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        world.setBlock(cposx + 3, cposy + 5, cposz + 2, WATER, Block.UPDATE_ALL);
        world.setBlock(cposx + 2, cposy + 5, cposz + 2, WATER, Block.UPDATE_ALL);
        world.setBlock(cposx + 4, cposy + 5, cposz + 2, WATER, Block.UPDATE_ALL);
    }

    /** {@code makewalkway} (:5499-5518). */
    private static void makewalkway(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        BlockState bid = AIR;
        for (int i = 0; i < 3; ++i) {
            for (int k = 0; k < 10; ++k) {
                for (int j = 0; j < 15; ++j) {
                    bid = QUARTZ;
                    if (j == 1) {
                        bid = AIR;
                        if (k > 6) {
                            bid = QUARTZ;
                        }
                    }
                    if (j > 1) {
                        bid = AIR;
                    }
                    world.setBlock(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
    }

    /** {@code makewhbase} (:5520-5537). */
    private static void makewhbase(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        for (int i = 0; i < 25; ++i) {
            for (int k = 0; k < 25; ++k) {
                world.setBlock(cposx + i, cposy + 1, cposz + k, QUARTZ);
                if ((i == 0 || i == 24) && (k == 0 || k == 24)) {
                    world.setBlock(cposx + i, cposy + 2, cposz + k, crystalTorch());
                }
            }
        }
        for (int i = 1; i < 24; ++i) {
            for (int k = 1; k < 24; ++k) {
                world.setBlock(cposx + i, cposy + 2, cposz + k, QUARTZ);
            }
        }
    }

    /** {@code makewhwalls} (:5539-5589): the window pattern differs per wall, the k == 22 side is a checker board. */
    private static void makewhwalls(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        final BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
        BlockState bid = AIR;
        for (int i = 0; i < 23; ++i) {
            for (int k = 0; k < 23; ++k) {
                for (int j = 0; j < 6; ++j) {
                    bid = AIR;
                    if (i == 0 || k == 0 || i == 22 || k == 22) {
                        bid = QUARTZ;
                    }
                    if (j != 0 && bid != AIR) {
                        if (k == 22) {
                            if ((j & 0x1) == 0x1) {
                                if ((i & 0x1) == 0x0 || (k & 0x1) == 0x0) {
                                    bid = pane;
                                }
                            } else if ((i & 0x1) == 0x1 || (k & 0x1) == 0x1) {
                                bid = pane;
                            }
                        } else if (k != 0) {
                            if ((j & 0x1) == 0x1) {
                                if (i == 2 || k == 2 || i == 20 || k == 20) {
                                    bid = pane;
                                }
                            } else if (i == 1 || k == 1 || i == 21 || k == 21) {
                                bid = pane;
                            }
                            if (j > 0 && j < 5 && k > 7 && k < 15) {
                                bid = pane;
                            }
                        } else if ((j & 0x1) == 0x1) {
                            if (i == 2 || k == 2 || i == 20 || k == 20) {
                                bid = pane;
                            }
                        } else if (i == 1 || k == 1 || i == 21 || k == 21) {
                            bid = pane;
                        }
                    }
                    world.setBlock(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        world.setBlock(cposx + 11, cposy, cposz, AIR);
        world.setBlock(cposx + 11, cposy + 1, cposz, AIR);
        LegacyDoor.placeDoorBlock(world, cposx + 11, cposy, cposz, 3, Blocks.IRON_DOOR);
        world.setBlock(cposx + 12, cposy + 1, cposz - 1, LegacyMeta.state(Blocks.STONE_BUTTON, 4), Block.UPDATE_CLIENTS);
    }

    /** {@code makewhroof} (:5591-5635): hollow step pyramid, emerald checker rim and cap, crystal torches, fence mast. */
    private static void makewhroof(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        BlockState bid = AIR;
        for (int j = 0; j < 13; ++j) {
            for (int i = 0; i < 25 - 2 * j; ++i) {
                for (int k = 0; k < 25 - 2 * j; ++k) {
                    bid = AIR;
                    if (i == 0 || k == 0 || i == 24 - 2 * j || k == 24 - 2 * j) {
                        bid = QUARTZ;
                    }
                    if (j == 0 && bid != AIR && (i + k & 0x1) == 0x1) {
                        bid = Blocks.EMERALD_BLOCK.defaultBlockState();
                    }
                    if (j == 12) {
                        bid = Blocks.EMERALD_BLOCK.defaultBlockState();
                    }
                    world.setBlock(cposx + i + j, cposy + 8 + j, cposz + k + j, bid);
                    if ((i == 0 || i == 24 - 2 * j) && (k == 0 || k == 24 - 2 * j)) {
                        world.setBlock(cposx + i + j, cposy + 8 + j + 1, cposz + k + j, crystalTorch());
                    }
                }
            }
        }
        bid = Blocks.OAK_FENCE.defaultBlockState();
        for (int dy = 11; dy >= 0; --dy) {
            // :5614-5625, twelve identical writes from 8 + 11 down to 8 + 0
            world.setBlock(cposx + 12, cposy + 8 + dy, cposz + 12, bid);
        }
        world.setBlock(cposx + 11, cposy + 8, cposz + 12, bid);
        world.setBlock(cposx + 13, cposy + 8, cposz + 12, bid);
        world.setBlock(cposx + 12, cposy + 8, cposz + 11, bid);
        world.setBlock(cposx + 12, cposy + 8, cposz + 13, bid);
        bid = crystalTorch();
        world.setBlock(cposx + 11, cposy + 8 + 1, cposz + 12, bid);
        world.setBlock(cposx + 13, cposy + 8 + 1, cposz + 12, bid);
        world.setBlock(cposx + 12, cposy + 8 + 1, cposz + 11, bid);
        world.setBlock(cposx + 12, cposy + 8 + 1, cposz + 13, bid);
    }

    /**
     * {@code makewhinterior} (:5637-5732): six benches of quartz stairs, each with two rows of
     * {@code piston_extension} meta 1, and four Criminal cells with a chest under the spawner.
     *
     * <p>{@code piston_extension} is {@code moving_piston} in 1.21.1 (DECISIONS R18); its metadata is the facing
     * index (1 = up), bit 8 sticky - here unset, so {@code type=normal}.
     */
    private static void makewhinterior(final StructureWriter world, final Random random,
                                       final int cposx, final int cposy, final int cposz) {
        int zoff = 1;
        int xoff = 0;
        bench(world, cposx, cposy, cposz, xoff, zoff);
        xoff = 11;
        bench(world, cposx, cposy, cposz, xoff, zoff);
        zoff = 7;
        xoff = 0;
        bench(world, cposx, cposy, cposz, xoff, zoff);
        xoff = 11;
        bench(world, cposx, cposy, cposz, xoff, zoff);
        zoff = 13;
        xoff = 0;
        bench(world, cposx, cposy, cposz, xoff, zoff);
        xoff = 11;
        bench(world, cposx, cposy, cposz, xoff, zoff);
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.WhiteHouseContentsList;
        zoff = 18;
        for (final int x : new int[] {2, 6, 12, 16}) {
            xoff = x;
            world.setSpawner(cposx + xoff, cposy + 1, cposz + zoff, "orespawn:criminal");
            world.setChest(cposx + xoff, cposy, cposz + zoff, -1, chestContents, 3 + random.nextInt(5), random);
        }
    }

    /** One of the six identical eight-long bench loops of :5640-5693. PORT: extracted, writes unchanged. */
    private static void bench(final StructureWriter world, final int cposx, final int cposy, final int cposz,
                              final int xoff, final int zoff) {
        final BlockState back = LegacyMeta.state(Blocks.QUARTZ_STAIRS, 3);
        final BlockState front = LegacyMeta.state(Blocks.QUARTZ_STAIRS, 2);
        final BlockState moving = LegacyMetaC.movingPiston(1);
        for (int i = 0; i < 8; ++i) {
            world.setBlock(cposx + xoff + i, cposy, cposz + zoff, back);
            world.setBlock(cposx + xoff + i, cposy, cposz + zoff + 1, moving);
            world.setBlock(cposx + xoff + i, cposy, cposz + zoff + 2, moving);
            world.setBlock(cposx + xoff + i, cposy, cposz + zoff + 3, front);
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeFrogPond (:6056-6077)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeFrogPond} (:6056-6077): Frog spawner over a 7x7 pond with lily pads round the spawner. */
    public static void makeFrogPond(final StructureWriter world, final Random random,
                                    final int cposx, final int cposy, final int cposz) {
        world.setSpawner(cposx, cposy + 2, cposz, "orespawn:frog");
        for (int i = -3; i <= 3; ++i) {
            for (int j = -3; j <= 3; ++j) {
                world.setBlock(cposx + i, cposy, cposz + j, WATER, Block.UPDATE_ALL);
            }
        }
        world.setBlock(cposx, cposy + 1, cposz, WATER, Block.UPDATE_ALL);
        world.setBlock(cposx - 1, cposy + 1, cposz, WATER, Block.UPDATE_ALL);
        world.setBlock(cposx + 1, cposy + 1, cposz, WATER, Block.UPDATE_ALL);
        world.setBlock(cposx, cposy + 1, cposz - 1, WATER, Block.UPDATE_ALL);
        world.setBlock(cposx, cposy + 1, cposz + 1, WATER, Block.UPDATE_ALL);
        final BlockState lily = Blocks.LILY_PAD.defaultBlockState();
        world.setBlock(cposx - 1, cposy + 2, cposz, lily, Block.UPDATE_ALL);
        world.setBlock(cposx + 1, cposy + 2, cposz, lily, Block.UPDATE_ALL);
        world.setBlock(cposx, cposy + 2, cposz - 1, lily, Block.UPDATE_ALL);
        world.setBlock(cposx, cposy + 2, cposz + 1, lily, Block.UPDATE_ALL);
    }

    // ------------------------------------------------------------------------------------------------------------
    // makePumpkin (:6079-6217)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makePumpkin} (:6079-6217): hollow orange clay pumpkin, face cut into z = 0, green stem, fire, two spawners. */
    public static void makePumpkin(final StructureWriter world, final Random random,
                                   final int cposx, final int cposy, final int cposz) {
        final int width = 14;
        final int depth = 12;
        final int height = 14;
        final int dark_green = 13;
        final int orange = 1;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                for (int k = 0; k < depth; ++k) {
                    boolean clay = false;
                    if (j == 0 || j == height - 1) {
                        clay = true;
                    }
                    if (i == 0 || i == width - 1) {
                        clay = true;
                    }
                    if (k == 0 || k == depth - 1) {
                        clay = true;
                    }
                    // stained_hardened_clay meta 1 (orange); air with meta 0 otherwise
                    world.setBlock(cposx + i, cposy + j, cposz + k, clay ? LegacyMeta.stainedClay(orange) : AIR);
                }
            }
        }
        // Face, left half (:6109-6143) with i = width / 2 - 1, then right half (:6144-6178) with i = width / 2.
        int i = width / 2 - 1;
        final int k = 0;
        faceAir(world, cposx + i, cposy, cposz + k, 1);
        i = width / 2;
        faceAir(world, cposx + i, cposy, cposz + k, -1);
        final int stemZ = depth / 2 - 1;
        for (int j = 0; j < 4; ++j) {
            for (i = 0; i < 3; ++i) {
                world.setBlock(cposx + width / 2 - i - j, cposy + height + j, cposz + stemZ, LegacyMeta.stainedClay(dark_green));
            }
        }
        for (int j = 0; j < 5; ++j) {
            for (i = 0; i < 2; ++i) {
                for (int kk = 0; kk < 2; ++kk) {
                    world.setBlock(cposx + width / 2 + i - 1, cposy + j + 1, cposz + depth / 2 + kk - 1,
                            Blocks.OAK_PLANKS.defaultBlockState());
                }
            }
        }
        int j = 5;
        for (i = 0; i < 2; ++i) {
            for (int kk = 0; kk < 2; ++kk) {
                world.setBlock(cposx + width / 2 + i - 1, cposy + j + 1, cposz + depth / 2 + kk - 1,
                        Blocks.NETHERRACK.defaultBlockState());
            }
        }
        j = 6;
        int kk = 0;
        for (i = 0; i < 2; ++i) {
            world.setBlock(cposx + width / 2 + i - 1, cposy + j + 1, cposz + depth / 2 + kk - 1, Blocks.FIRE.defaultBlockState());
        }
        j = 6;
        kk = 1;
        i = 0;
        world.setSpawner(cposx + width / 2 + i - 1, cposy + j + 1, cposz + depth / 2 + kk - 1, "orespawn:ghost_pumpkin_skelly");
        i = 1;
        world.setSpawner(cposx + width / 2 + i - 1, cposy + j + 1, cposz + depth / 2 + kk - 1, "orespawn:ghost_pumpkin_skelly");
    }

    /**
     * One half of the pumpkin face, :6109-6143 ({@code sign} +1, offsets added to {@code x}) and :6144-6178
     * ({@code sign} -1, offsets subtracted). PORT: the two halves are mirror images of each other with the same write
     * order; extracted into one method.
     */
    private static void faceAir(final StructureWriter world, final int x, final int cposy, final int z, final int sign) {
        final int[][] rows = {
                {11, 3, 4, 5},
                {10, 3, 4, 5},
                {9, 3, 4, 5},
                {8, 2, 3},
                {7, 2, 3},
                {4, 1, 4},
                {3, 1, 2, 3, 4},
                {2, 1, 2, 3, 4},
                {1, 2}};
        for (final int[] row : rows) {
            for (int n = 1; n < row.length; ++n) {
                world.setBlock(x + sign * row[n], cposy + row[0], z, AIR);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeRoundRotator (:6219-6289)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeRoundRotator} (:6219-6289): upright bedrock ring r 6 and pink crystal ring r 2 in the x-y plane. */
    public static void makeRoundRotator(final StructureWriter world, final Random random,
                                        final int cposx, final int cposy, final int cposz) {
        float radius = 6.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
            final float curx = (float) (radius * Math.cos(Math.toRadians(curdeg)));
            final float cury = (float) (radius * Math.sin(Math.toRadians(curdeg)));
            world.setBlock((int) (cposx + curx + 0.5f), (int) (cposy + 6 + cury + 0.5f), cposz, Blocks.BEDROCK.defaultBlockState());
        }
        radius = 2.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
            final float curx = (float) (radius * Math.cos(Math.toRadians(curdeg)));
            final float cury = (float) (radius * Math.sin(Math.toRadians(curdeg)));
            world.setBlock((int) (cposx + curx + 0.5f), (int) (cposy + 6 + cury + 0.5f), cposz,
                    ModBlocks.CRYSTALPINK_BLOCK.get().defaultBlockState());
        }
        world.setSpawner(cposx + 1, cposy + 6 + 1, cposz, ROTATOR);
        world.setSpawner(cposx - 1, cposy + 6 - 1, cposz, ROTATOR);
        world.setSpawner(cposx + 1, cposy + 6 - 1, cposz, ROTATOR);
        world.setSpawner(cposx - 1, cposy + 6 + 1, cposz, ROTATOR);
        world.setSpawner(cposx + 5, cposy + 6, cposz, DUNGEON_BEAST);
        world.setSpawner(cposx - 5, cposy + 6, cposz, DUNGEON_BEAST);
        world.setSpawner(cposx, cposy + 6 - 5, cposz, DUNGEON_BEAST);
        world.setSpawner(cposx, cposy + 6 + 5, cposz, DUNGEON_BEAST);
        final BlockState blk = ModBlocks.CRYSTALCOAL.get().defaultBlockState();
        world.setBlock(cposx + 1, cposy + 6, cposz, blk);
        world.setBlock(cposx - 1, cposy + 6, cposz, blk);
        world.setBlock(cposx, cposy + 6 + 1, cposz, blk);
        world.setBlock(cposx, cposy + 6 - 1, cposz, blk);
        // world.setBlock(chest) (flag 3) + setBlockMetadataWithNotify(2, 3); list and draw inside "chest != null",
        // drawn unconditionally (GenericDungeonHelpers, draw rule)
        world.setBlock(cposx, cposy + 6, cposz, LegacyMeta.chest(2), Block.UPDATE_ALL);
        world.fillChest(cposx, cposy + 6, cposz, GenericDungeonHelpers.CrystalBattleTowerVortexContentsList,
                6 + random.nextInt(6), random);
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeRainbow (:6291-6419)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeRainbow} (:6291-6419): a white clay cloud with rain at y 26..35, eight coloured arches above it. */
    public static void makeRainbow(final StructureWriter world, final Random random,
                                   final int cposx, final int cposy, final int cposz) {
        int width = 12;
        int depth = 1;
        int blk_color = 0;
        int j = 35;
        for (int i = -width; i < width; ++i) {
            for (int k = -depth; k <= depth; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, LegacyMeta.stainedClay(blk_color));
            }
        }
        int k = 0;
        for (int i = -width + 1; i < width; i += 3) {
            world.setBlock(cposx + i, cposy + j, cposz + k, WATER);
            // Blocks.flowing_water meta 0
            world.setBlock(cposx + i, cposy + j - 1, cposz + k, WATER);
        }
        cloudRing(world, cposx, cposy + 26, cposz, 13, 2, blk_color);
        cloudRing(world, cposx, cposy + 27, cposz, 14, 3, blk_color);
        cloudRing(world, cposx, cposy + 28, cposz, 13, 2, blk_color);
        j = 29;
        width = 12;
        depth = 1;
        for (int i = -width; i < width; ++i) {
            for (k = -depth; k <= depth; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, LegacyMeta.stainedClay(blk_color));
            }
        }
        j = 30;
        for (int m = 3; m < 11; ++m) {
            blk_color = GenericDungeonHelpers.blkcolors[m - 3];
            final BlockState clay = LegacyMeta.stainedClay(blk_color);
            for (int i = 0; i < m; ++i) {
                world.setBlock(cposx + m, cposy + j + i, cposz, clay);
                world.setBlock(cposx - (m + 1), cposy + j + i, cposz, clay);
            }
            for (int i = -(m + 1); i <= m; ++i) {
                world.setBlock(cposx + i, cposy + j + m, cposz, clay);
            }
        }
        world.setSpawner(cposx + 2, cposy + j, cposz, CLOUD_SHARK);
        world.setSpawner(cposx - 3, cposy + j, cposz, CLOUD_SHARK);
        world.setSpawner(cposx + 2, cposy + j + 1, cposz, CLOUD_SHARK);
        world.setSpawner(cposx - 3, cposy + j + 1, cposz, CLOUD_SHARK);
        world.setSpawner(cposx + 2, cposy + j + 2, cposz, CLOUD_SHARK);
        world.setSpawner(cposx - 3, cposy + j + 2, cposz, CLOUD_SHARK);
        final WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.RainbowContentsList;
        // world.setBlock(chest) (flag 3) + setBlockMetadataWithNotify(2, 3) at x, then at x - 1: a north-facing double
        // chest in 1.7.10. PORT: 1.21.1 joins two chests only when one already carries a type (see
        // GenericDungeonHelpers.doubleChestAlongX); facing north, the west half is LEFT and the east half RIGHT
        // (ChestBlock.getConnectedDirection: LEFT connects clockwise, north -> east). Both halves are filled.
        final BlockState north = LegacyMeta.chest(2);
        world.setBlock(cposx, cposy + j, cposz, north.setValue(ChestBlock.TYPE, ChestType.RIGHT), Block.UPDATE_ALL);
        world.fillChest(cposx, cposy + j, cposz, chestContents, 10 + random.nextInt(5), random);
        world.setBlock(cposx - 1, cposy + j, cposz, north.setValue(ChestBlock.TYPE, ChestType.LEFT), Block.UPDATE_ALL);
        world.fillChest(cposx - 1, cposy + j, cposz, chestContents, 10 + random.nextInt(5), random);
    }

    /** The three hollow cloud rings :6312-6356 (y 26, 27, 28). PORT: extracted, the three loops differ only in size. */
    private static void cloudRing(final StructureWriter world, final int cposx, final int y, final int cposz,
                                  final int width, final int depth, final int blk_color) {
        for (int i = -width; i < width; ++i) {
            for (int k = -depth; k <= depth; ++k) {
                boolean clay = false;
                if (i == -width || i == width - 1) {
                    clay = true;
                }
                if (k == -depth || k == depth) {
                    clay = true;
                }
                world.setBlock(cposx + i, y, cposz + k, clay ? LegacyMeta.stainedClay(blk_color) : AIR);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeSpiderHangout (:7032-7084) and makeRedAntHangout (:7086-7110)
    // ------------------------------------------------------------------------------------------------------------

    /** {@code makeSpiderHangout} (:7032-7084): cleared 20x20 room on gravel, Spider Driver spawners, one Robot Spider. */
    public static void makeSpiderHangout(final StructureWriter world, final Random random,
                                         final int cposx, final int cposy, final int cposz) {
        for (int i = 0; i < 20; ++i) {
            for (int j = -1; j < 20; ++j) {
                for (int k = 0; k < 20; ++k) {
                    BlockState blk = AIR;
                    if (j == -1) {
                        blk = Blocks.STONE.defaultBlockState();
                    }
                    if (j == 0) {
                        blk = Blocks.GRAVEL.defaultBlockState();
                    }
                    world.setBlock(cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        final String driver = "orespawn:spider_driver";
        for (int j = 1; j < 4; ++j) {
            world.setSpawner(cposx, cposy + j, cposz, driver);
            world.setSpawner(cposx + 19, cposy + j, cposz + 19, driver);
            world.setSpawner(cposx + 19, cposy + j, cposz, driver);
            world.setSpawner(cposx, cposy + j, cposz + 19, driver);
        }
        spawnRobot(world, random, "orespawn:robot_spider", cposx + 10, cposy + 1, cposz + 10);
    }

    /** {@code makeRedAntHangout} (:7086-7110): cleared 16x16 room on gravel with red ant blocks, one Robot Red Ant. */
    public static void makeRedAntHangout(final StructureWriter world, final Random random,
                                         final int cposx, final int cposy, final int cposz) {
        for (int i = 0; i < 16; ++i) {
            for (int j = -1; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    BlockState blk = AIR;
                    if (j == -1) {
                        blk = Blocks.STONE.defaultBlockState();
                    }
                    if (j == 0) {
                        blk = Blocks.GRAVEL.defaultBlockState();
                        if ((i < 3 || i > 12) && (k < 3 || k > 12)) {
                            blk = ModBlocks.REDANTBLOCK.get().defaultBlockState();
                        }
                    }
                    world.setBlock(cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        spawnRobot(world, random, "orespawn:robot_red_ant", cposx + 8, cposy + 1, cposz + 8);
    }

    /**
     * {@code EntityList.createEntityByName(name, world)}, then {@code setLocationAndAngles(x, y, z,
     * rand.nextFloat() * 360, 0)} and {@code spawnEntityInWorld} - the yaw is drawn only when the entity exists.
     * An unknown id spawns nothing and draws no yaw, as with an unknown name in 1.7.10.
     */
    private static void spawnRobot(final StructureWriter world, final Random random, final String id,
                                   final int x, final int y, final int z) {
        if (StructureWriter.entityType(id).isEmpty()) {
            return;
        }
        final float yaw = random.nextFloat() * 360.0f;
        world.spawnEntity(id, x, y, z, yaw, 0.0f);
    }

    // ------------------------------------------------------------------------------------------------------------
    // shared constants
    // ------------------------------------------------------------------------------------------------------------

    static final BlockState AIR = Blocks.AIR.defaultBlockState();
    /** {@code Blocks.water} and {@code Blocks.flowing_water} meta 0: both a source block in 1.21.1. */
    static final BlockState WATER = Blocks.WATER.defaultBlockState();
    /** {@code Blocks.quartz_block} meta 0. */
    static final BlockState QUARTZ = Blocks.QUARTZ_BLOCK.defaultBlockState();
    /** {@code Blocks.leaves} meta 0 (oak). */
    static final BlockState LEAVES = Blocks.OAK_LEAVES.defaultBlockState();
    /** {@code Blocks.log} meta 0 (oak, upright). */
    static final BlockState LOG = Blocks.OAK_LOG.defaultBlockState();

    static final String RAT = "orespawn:rat";
    static final String DUNGEON_BEAST = "orespawn:dungeon_beast";
    static final String ROTATOR = "orespawn:rotator";
    static final String CLOUD_SHARK = "orespawn:cloud_shark";

    static BlockState crystalStone() {
        return ModBlocks.CRYSTALSTONE.get().defaultBlockState();
    }

    /** {@code OreSpawnMain.CrystalTorch} meta 0: standing. */
    static BlockState crystalTorch() {
        return ModBlocks.CRYSTAL_TORCH.get().defaultBlockState();
    }
}
