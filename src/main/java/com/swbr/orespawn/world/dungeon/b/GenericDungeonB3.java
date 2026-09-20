package com.swbr.orespawn.world.dungeon.b;

import static com.swbr.orespawn.world.dungeon.b.GenericDungeonB.FastSetBlock;

import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code GenericDungeon} lines 4085-4865: Robot Lab with {@code makerobopillar}, {@code makerobomain},
 * {@code makerobotower}, {@code makeroboaltar}, {@code makeroborailway}, {@code makeroboassemblyline} and
 * {@code makerobotreasureroom}; King Altar with {@code makekingcolumn}, {@code makekingbackground} and
 * {@code makekingcenteraltar}; Leonopteryx Nest; Cephadrome Altar (verhalten/world-01.md). See
 * {@link GenericDungeonB} for the conventions.
 */
public final class GenericDungeonB3 {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /**
     * {@code king} (GenericDungeon.java:96): run lengths of the King picture, alternating stone and quartz from the
     * start of each row; -1 fills the rest of the row with stone and starts the next one.
     */
    private static final int[] king = {-1, -1, 24, 3, -1, 24, 5, -1, 17, 12, -1, 16, 15, -1, 15, 14, -1, 15, 6, 3, 5,
        -1, 14, 6, 4, 3, -1, 14, 5, -1, 14, 5, -1, 12, 9, -1, 11, 11, -1, 8, 17, -1, 5, 23, -1, 3, 27, -1, 2, 29, -1, 1,
        31, -1, 0, 33, -1, 13, 6, -1, 12, 9, -1, 11, 3, 1, 2, 1, 4, -1, 10, 3, 2, 2, 3, 2, -1, 10, 2, 4, 2, 3, 2, -1, 9,
        2, 5, 2, 4, 6, -1, 9, 2, 5, 2, 6, 4, -1, 8, 2, 6, 1, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 15, 2,
        -1, -1, -1};

    private GenericDungeonB3() {
    }

    // ------------------------------------------------------------------------------------------------------------
    // Robot Lab
    // ------------------------------------------------------------------------------------------------------------

    /**
     * {@code makeRobotLab} (:4085-4132): quartz lab with a double iron door and two buttons, the main hall behind it
     * and six sniper pillars along the walls. {@code world.isRemote} (:4091) is never true here.
     */
    public static void makeRobotLab(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                    final int cposz) {
        final int width = 10;
        final int length = 20;
        final int height = 5;
        for (int j = 0; j <= height; ++j) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < length; ++k) {
                    Block bid = Blocks.AIR;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) {
                        bid = Blocks.QUARTZ_BLOCK;
                    }
                    if (j == 0) {
                        bid = Blocks.QUARTZ_BLOCK;
                        if (i == width / 2 || i == width / 2 - 1) {
                            bid = Blocks.IRON_BLOCK;
                        }
                    }
                    if (j == height) {
                        bid = Blocks.QUARTZ_BLOCK;
                        if (i == 0 || k == 0 || i == width - 1 || k == length - 1) {
                            bid = Blocks.AIR;
                        }
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        FastSetBlock(world, cposx + width / 2, cposy + 1, cposz, AIR);
        FastSetBlock(world, cposx + width / 2, cposy + 2, cposz, AIR);
        FastSetBlock(world, cposx + width / 2 - 1, cposy + 1, cposz, AIR);
        FastSetBlock(world, cposx + width / 2 - 1, cposy + 2, cposz, AIR);
        GenericDungeonB.placeDoorBlock(world, cposx + width / 2, cposy + 1, cposz, 3, Blocks.IRON_DOOR);
        GenericDungeonB.placeDoorBlock(world, cposx + width / 2 - 1, cposy + 1, cposz, 3, Blocks.IRON_DOOR);
        world.setBlock(cposx + width / 2 - 2, cposy + 2, cposz - 1, Blocks.STONE_BUTTON, 4, Block.UPDATE_CLIENTS);
        world.setBlock(cposx + width / 2 + 1, cposy + 2, cposz - 1, Blocks.STONE_BUTTON, 4, Block.UPDATE_CLIENTS);
        makerobomain(world, rand, cposx, cposy, cposz + length - 1);
        makerobopillar(world, cposx, cposy, cposz + length / 3, 0);
        makerobopillar(world, cposx, cposy, cposz + length * 2 / 3, 0);
        makerobopillar(world, cposx, cposy, cposz + (length - 1), 0);
        makerobopillar(world, cposx + width - 1, cposy, cposz + length / 3, 1);
        makerobopillar(world, cposx + width - 1, cposy, cposz + length * 2 / 3, 1);
        makerobopillar(world, cposx + width - 1, cposy, cposz + (length - 1), 1);
    }

    /** {@code makerobopillar} (:4134-4166): 3x5x3 quartz with redstone blocks and a Robo-Sniper spawner on one side. */
    private static void makerobopillar(final StructureWriter world, final int cposx, final int cposy, final int cposz,
                                       final int dir) {
        for (int j = 0; j < 5; ++j) {
            for (int i = -1; i < 2; ++i) {
                for (int k = -1; k < 2; ++k) {
                    Block bid = Blocks.QUARTZ_BLOCK;
                    if (j == 2 || j == 3) {
                        if (k == 0 && (i == -1 || i == 1)) {
                            bid = Blocks.REDSTONE_BLOCK;
                        }
                        if (i == 0 && (k == -1 || k == 1)) {
                            bid = Blocks.REDSTONE_BLOCK;
                        }
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        if (dir == 0) {
            world.setSpawner(cposx + 1, cposy + 1, cposz, "orespawn:robo_sniper");
        }
        if (dir == 1) {
            world.setSpawner(cposx - 1, cposy + 1, cposz, "orespawn:robo_sniper");
        }
    }

    /** {@code makerobomain} (:4168-4205): the 30x10x30 hall with its five fittings. */
    public static void makerobomain(final StructureWriter world, final Random rand, int cposx, final int cposy,
                                    final int cposz) {
        final int width = 30;
        final int length = 30;
        final int height = 9;
        cposx -= 10;
        for (int j = 0; j <= height; ++j) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < length; ++k) {
                    Block bid = Blocks.AIR;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) {
                        bid = Blocks.QUARTZ_BLOCK;
                    }
                    if (j == 0) {
                        bid = Blocks.QUARTZ_BLOCK;
                        if (i == width / 2 || i == width / 2 - 1) {
                            bid = Blocks.IRON_BLOCK;
                        }
                    }
                    if (j == height) {
                        bid = Blocks.QUARTZ_BLOCK;
                        if (i == 0 || k == 0 || i == width - 1 || k == length - 1) {
                            bid = Blocks.AIR;
                        }
                    }
                    if ((j == 1 || j == 2 || j == 3) && k == 0 && i >= width / 3 && i < width * 2 / 3) {
                        bid = Blocks.AIR;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        makeroboaltar(world, cposx + width / 2 - 4, cposy, cposz + 6);
        makeroborailway(world, cposx + 3, cposy, cposz + 10);
        makeroboassemblyline(world, cposx + width - 4, cposy, cposz + 4);
        makerobotreasureroom(world, rand, cposx + 9, cposy, cposz + 18);
        makerobotower(world, cposx + width / 2 - 6, cposy + height, cposz + length / 2 - 6);
    }

    /** {@code makerobotower} (:4207-4261): roof platform, four sniper pillars and the mast. */
    public static void makerobotower(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        for (int j = 0; j < 2; ++j) {
            for (int i = 0; i < 12; ++i) {
                for (int k = 0; k < 12; ++k) {
                    Block bid = Blocks.AIR;
                    if (j == 1) {
                        if (i == 0 || k == 0 || i == 11 || k == 11) {
                            bid = Blocks.IRON_BARS;
                        }
                        if (i == 0 && (k == 0 || k == 11)) {
                            bid = Blocks.REDSTONE_BLOCK;
                        }
                        if (i == 11 && (k == 0 || k == 11)) {
                            bid = Blocks.REDSTONE_BLOCK;
                        }
                    }
                    if (j == 0) {
                        bid = Blocks.QUARTZ_BLOCK;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        makerobopillar(world, cposx + 4, cposy + 1, cposz + 4, 1);
        makerobopillar(world, cposx + 7, cposy + 1, cposz + 7, 0);
        makerobopillar(world, cposx + 4, cposy + 1, cposz + 7, 1);
        makerobopillar(world, cposx + 7, cposy + 1, cposz + 4, 0);
        for (int j = 5; j < 35; ++j) {
            for (int i = 0; i < 2; ++i) {
                for (int k = 0; k < 3; ++k) {
                    Block bid;
                    if (j < 15) {
                        bid = Blocks.QUARTZ_BLOCK;
                    } else if (j < 25) {
                        bid = Blocks.QUARTZ_BLOCK;
                        if (k == 2) {
                            bid = Blocks.IRON_BARS;
                        }
                    } else {
                        bid = Blocks.QUARTZ_BLOCK;
                        if (k == 1) {
                            bid = Blocks.IRON_BARS;
                        }
                        if (k == 2) {
                            bid = Blocks.AIR;
                        }
                    }
                    FastSetBlock(world, cposx + i + 5, cposy + j, cposz + k + 5, bid.defaultBlockState());
                }
            }
        }
    }

    /** {@code makeroboaltar} (:4263-4296). */
    public static void makeroboaltar(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        BlockState bid = Blocks.IRON_BLOCK.defaultBlockState();
        for (int i = 0; i < 8; ++i) {
            for (int k = 0; k < 8; ++k) {
                FastSetBlock(world, cposx + i, cposy, cposz + k, bid);
            }
        }
        bid = Blocks.QUARTZ_BLOCK.defaultBlockState();
        for (int i = 0; i < 6; ++i) {
            for (int k = 0; k < 6; ++k) {
                FastSetBlock(world, cposx + i + 1, cposy + 1, cposz + k + 1, bid);
            }
        }
        final BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        final BlockState torch = Blocks.TORCH.defaultBlockState();
        FastSetBlock(world, cposx + 2, cposy + 1, cposz + 2, redstone);
        FastSetBlock(world, cposx + 2, cposy + 2, cposz + 2, torch);
        FastSetBlock(world, cposx + 5, cposy + 1, cposz + 5, redstone);
        FastSetBlock(world, cposx + 5, cposy + 2, cposz + 5, torch);
        FastSetBlock(world, cposx + 5, cposy + 1, cposz + 2, redstone);
        FastSetBlock(world, cposx + 5, cposy + 2, cposz + 2, torch);
        FastSetBlock(world, cposx + 2, cposy + 1, cposz + 5, redstone);
        FastSetBlock(world, cposx + 2, cposy + 2, cposz + 5, torch);
        world.setSpawner(cposx + 3, cposy + 2, cposz + 3, "orespawn:robo_pounder");
        world.setSpawner(cposx + 4, cposy + 2, cposz + 4, "orespawn:robo_pounder");
    }

    /**
     * {@code makeroborailway} (:4298-4331): two rail tracks along z with powered rails and floor levers (metadata 5)
     * every fourth block. Rail metadata 0 is the north-south straight, the default of both rail blocks.
     */
    public static void makeroborailway(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        final BlockState rail = Blocks.RAIL.defaultBlockState();
        final BlockState golden = Blocks.POWERED_RAIL.defaultBlockState();
        final BlockState lever = LegacyMeta.lever(5);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 0, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 0, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 1, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 1, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 2, golden);
        FastSetBlock(world, cposx + 1, cposy + 1, cposz + 2, lever);
        FastSetBlock(world, cposx + 2, cposy + 1, cposz + 2, lever);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 2, golden);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 3, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 3, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 4, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 4, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 5, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 5, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 6, golden);
        FastSetBlock(world, cposx + 1, cposy + 1, cposz + 6, lever);
        FastSetBlock(world, cposx + 2, cposy + 1, cposz + 6, lever);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 6, golden);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 7, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 7, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 8, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 8, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 9, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 9, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 10, golden);
        FastSetBlock(world, cposx + 1, cposy + 1, cposz + 10, lever);
        FastSetBlock(world, cposx + 2, cposy + 1, cposz + 10, lever);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 10, golden);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 11, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 11, rail);
        FastSetBlock(world, cposx + 0, cposy + 1, cposz + 12, rail);
        FastSetBlock(world, cposx + 3, cposy + 1, cposz + 12, rail);
    }

    /**
     * {@code makeroboassemblyline} (:4333-4346): quartz belt, quartz stairs (metadata 1, west), sticky pistons
     * (metadata 3, south) with white carpet on top, powered floor levers (metadata 13 = 5 | 8).
     */
    public static void makeroboassemblyline(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        for (int k = 0; k < 24; ++k) {
            if (k % 3 == 1) {
                world.setBlock(cposx - 2, cposy + 1, cposz + k, Blocks.QUARTZ_STAIRS, 1, Block.UPDATE_CLIENTS);
                world.setBlock(cposx, cposy + 2, cposz + k, Blocks.STICKY_PISTON, 3, Block.UPDATE_CLIENTS);
                FastSetBlock(world, cposx, cposy + 3, cposz + k, LegacyMeta.carpet(0));
            }
            if (k % 3 == 0) {
                FastSetBlock(world, cposx, cposy + 2, cposz + k, LegacyMeta.lever(13));
            }
            FastSetBlock(world, cposx, cposy + 1, cposz + k, Blocks.QUARTZ_BLOCK.defaultBlockState());
            FastSetBlock(world, cposx + 1, cposy + 1, cposz + k, Blocks.QUARTZ_BLOCK.defaultBlockState());
        }
    }

    /** {@code makerobotreasureroom} (:4348-4389): Robo-Warrior spawner and two chests of 10-14 draws. */
    public static void makerobotreasureroom(final StructureWriter world, final Random rand, final int cposx,
                                            final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = LootListsB.RobotContentsList;
        for (int j = 1; j < 7; ++j) {
            for (int i = 0; i < 12; ++i) {
                for (int k = 0; k < 8; ++k) {
                    Block bid = Blocks.AIR;
                    if (i == 0 || k == 0 || i == 11 || k == 7) {
                        bid = Blocks.QUARTZ_BLOCK;
                    }
                    if (j == 2 && i == 11) {
                        bid = Blocks.IRON_BARS;
                    }
                    if (j == 3 && bid != Blocks.AIR) {
                        bid = Blocks.IRON_BARS;
                    }
                    if ((j == 1 || j == 2 || j == 3) && k == 0 && (i == 1 || i == 2)) {
                        bid = Blocks.AIR;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        world.setSpawner(cposx + 10, cposy + 1, cposz + 1, "orespawn:robo_warrior");
        world.setChest(cposx + 8, cposy + 1, cposz + 1, 2, chestContents, 10 + rand.nextInt(5), rand);
        world.setChest(cposx + 6, cposy + 1, cposz + 1, 2, chestContents, 10 + rand.nextInt(5), rand);
    }

    // ------------------------------------------------------------------------------------------------------------
    // King Altar
    // ------------------------------------------------------------------------------------------------------------

    /**
     * {@code makeKingAltar} (:4391-4441): clears 61x59x61, grass floor with dirt filled up to nine blocks below where
     * there was air, tall grass or water, four columns, a two-layer quartz roof, the picture wall and the centre altar
     * with the King egg. {@code world.isRemote} (:4396) is never true here.
     *
     * <p>PORT: {@code Blocks.tallgrass} was the one block of dead shrub, grass and fern; 1.21.1 has
     * {@code short_grass} and {@code fern} (the category rule of R22). {@code Blocks.water} was the still-water block
     * only; 1.21.1 water covers flowing water as well.
     */
    public static void makeKingAltar(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                     final int cposz) {
        final int width = 51;
        final int length = 51;
        final int height = 48;
        for (int j = 0; j <= height + 10; ++j) {
            for (int i = -5; i < width + 5; ++i) {
                for (int k = -5; k < length + 5; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, AIR);
                }
            }
        }
        int j = 0;
        final BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        final BlockState dirt = Blocks.DIRT.defaultBlockState();
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < length; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, grass);
                for (int v = 1; v < 10; ++v) {
                    final BlockState bid = world.getBlock(cposx + i, cposy + j - v, cposz + k);
                    if (GenericDungeonB.isAir(bid) || bid.is(Blocks.SHORT_GRASS) || bid.is(Blocks.FERN)
                            // Blocks.tallgrass meta 0 (shrub) became dead_bush in 1.13; same check as makeQueenAltar.
                            || bid.is(Blocks.DEAD_BUSH) || bid.is(Blocks.WATER)) {
                        FastSetBlock(world, cposx + i, cposy + j - v, cposz + k, dirt);
                    }
                }
            }
        }
        makekingcolumn(world, cposx + 1, cposy + 1, cposz + 1);
        makekingcolumn(world, cposx + width - 8, cposy + 1, cposz + length - 8);
        makekingcolumn(world, cposx + 1, cposy + 1, cposz + length - 8);
        makekingcolumn(world, cposx + width - 8, cposy + 1, cposz + 1);
        final BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        j = height - 1;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < length; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, quartz);
            }
        }
        j = height;
        for (int i = -1; i <= width; ++i) {
            for (int k = -1; k <= length; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, quartz);
            }
        }
        makekingbackground(world, cposx + 4, cposy + 10, cposz + 9);
        makekingcenteraltar(world, rand, cposx + width / 2, cposy, cposz + length / 2);
    }

    /**
     * {@code makekingcolumn} (:4443-4508): 7x7 quartz plates at both ends, a hollow 5x5 shaft of 44 layers in a
     * four-layer gold and emerald pattern; the plain quartz of the shaft is metadata 2, the pillar.
     */
    private static void makekingcolumn(final StructureWriter world, int cposx, int cposy, int cposz) {
        final int width = 5;
        final int length = 5;
        final int height = 44;
        int j = 0;
        final BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        for (int i = 0; i < width + 2; ++i) {
            for (int k = 0; k < length + 2; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, quartz);
                FastSetBlock(world, cposx + i, cposy + j + height + 1, cposz + k, quartz);
            }
        }
        ++cposx;
        ++cposz;
        ++cposy;
        for (j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < length; ++k) {
                    Block bid = Blocks.AIR;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) {
                        bid = Blocks.QUARTZ_BLOCK;
                    }
                    if (j % 4 == 0 && bid != Blocks.AIR && (i == 2 || k == 2)) {
                        bid = Blocks.GOLD_BLOCK;
                    }
                    if (j % 4 == 1 && bid != Blocks.AIR) {
                        if (i == 1 || k == 1) {
                            bid = Blocks.GOLD_BLOCK;
                        }
                        if (i == 3 || k == 3) {
                            bid = Blocks.GOLD_BLOCK;
                        }
                    }
                    if (j % 4 == 2 && bid != Blocks.AIR) {
                        if (i == 1 || k == 1) {
                            bid = Blocks.GOLD_BLOCK;
                        }
                        if (i == 3 || k == 3) {
                            bid = Blocks.GOLD_BLOCK;
                        }
                        if (i == 2 || k == 2) {
                            bid = Blocks.EMERALD_BLOCK;
                        }
                    }
                    if (j % 4 == 3 && bid != Blocks.AIR) {
                        if (i == 1 || k == 1) {
                            bid = Blocks.GOLD_BLOCK;
                        }
                        if (i == 3 || k == 3) {
                            bid = Blocks.GOLD_BLOCK;
                        }
                    }
                    // quartz_block metadata 2 = vertical quartz pillar
                    final BlockState state = bid == Blocks.QUARTZ_BLOCK
                            ? Blocks.QUARTZ_PILLAR.defaultBlockState()
                            : bid.defaultBlockState();
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, state);
                }
            }
        }
    }

    /** {@code makekingbackground} (:4510-4562): the run-length picture in the plane x = cposx with its frame. */
    private static void makekingbackground(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        int curz = 0;
        int cury = 0;
        final int height = 33;
        final int width = 33;
        final BlockState stone = Blocks.STONE.defaultBlockState();
        final BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        BlockState bid = stone;
        for (int m = 0; m < king.length; ++m) {
            final int v = king[m];
            if (v < 0) {
                bid = stone;
                while (curz < width) {
                    FastSetBlock(world, cposx, cposy + cury, cposz + curz, bid);
                    ++curz;
                }
                ++cury;
                curz = 0;
            } else {
                for (int n = 0; n < v; ++n) {
                    FastSetBlock(world, cposx, cposy + cury, cposz + curz, bid);
                    ++curz;
                }
                if (bid == stone) {
                    bid = quartz;
                } else {
                    bid = stone;
                }
            }
        }
        final BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        for (int i = 0; i < width; ++i) {
            FastSetBlock(world, cposx, cposy - 1, cposz + i, gold);
        }
        for (int i = 0; i < width; ++i) {
            FastSetBlock(world, cposx, cposy + height, cposz + i, gold);
        }
        for (int i = -1; i <= height; ++i) {
            FastSetBlock(world, cposx, cposy + i, cposz - 1, gold);
        }
        for (int i = -1; i <= height; ++i) {
            FastSetBlock(world, cposx, cposy + i, cposz + width, gold);
        }
        final BlockState diamond = Blocks.DIAMOND_BLOCK.defaultBlockState();
        FastSetBlock(world, cposx, cposy - 2, cposz - 2, diamond);
        FastSetBlock(world, cposx, cposy + height + 1, cposz + width + 1, diamond);
        FastSetBlock(world, cposx, cposy - 2, cposz + width + 1, diamond);
        FastSetBlock(world, cposx, cposy + height + 1, cposz - 2, diamond);
        final BlockState crystalTorch = GenericDungeonB.block("crystaltorch");
        FastSetBlock(world, cposx, cposy - 1, cposz - 2, crystalTorch);
        FastSetBlock(world, cposx, cposy + height + 2, cposz + width + 1, crystalTorch);
        FastSetBlock(world, cposx, cposy - 1, cposz + width + 1, crystalTorch);
        FastSetBlock(world, cposx, cposy + height + 2, cposz - 2, crystalTorch);
    }

    /**
     * {@code makekingcenteraltar} (:4564-4714): stepped quartz cross, lapis ends, crystal torches, the chest with the
     * King egg in slot 13.
     */
    private static void makekingcenteraltar(final StructureWriter world, final Random rand, final int cposx,
                                            final int cposy, final int cposz) {
        final BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        final BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState();
        final BlockState crystalTorch = GenericDungeonB.block("crystaltorch");
        int width = 10;
        int length = 10;
        int j = 0;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 6;
        length = 20;
        j = 0;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 20;
        length = 6;
        j = 0;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 8;
        length = 8;
        j = 1;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 4;
        length = 18;
        j = 1;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                BlockState bid = quartz;
                if (i == width && (k == -length || k == length)) {
                    bid = lapis;
                }
                if (i == -width && (k == -length || k == length)) {
                    bid = lapis;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        width = 18;
        length = 4;
        j = 1;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                BlockState bid = quartz;
                if (i == width && (k == -length || k == length)) {
                    bid = lapis;
                }
                if (i == -width && (k == -length || k == length)) {
                    bid = lapis;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        width = 7;
        length = 7;
        j = 2;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, quartz);
                if (i == width && (k == -length || k == length)) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, crystalTorch);
                }
                if (i == -width && (k == -length || k == length)) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, crystalTorch);
                }
            }
        }
        width = 3;
        length = 17;
        j = 2;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 17;
        length = 3;
        j = 2;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 6;
        length = 6;
        j = 3;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 2;
        length = 16;
        j = 3;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 16;
        length = 2;
        j = 3;
        fill(world, cposx, cposy + j, cposz, width, length, quartz);
        width = 2;
        length = 2;
        j = 4;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, quartz);
                if (i == width && (k == -length || k == length)) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, crystalTorch);
                }
                if (i == -width && (k == -length || k == length)) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, crystalTorch);
                }
            }
        }
        // world.setBlock(chest) + setBlockMetadataWithNotify(2, 3): front north
        world.setChest(cposx, cposy + j, cposz, 2, null, 0, rand);
        GenericDungeonB.setSlot(world, cposx, cposy + j, cposz, 13, GenericDungeonB.stack("eggtheking", 1));
    }

    /** The plain {@code for i in -width..width, k in -length..length: FastSetBlock(bid)} loops of the centre altar. */
    private static void fill(final StructureWriter world, final int cposx, final int y, final int cposz, final int width,
                             final int length, final BlockState bid) {
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                FastSetBlock(world, cposx + i, y, cposz + k, bid);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Leonopteryx Nest, Cephadrome Altar
    // ------------------------------------------------------------------------------------------------------------

    /**
     * {@code makeLeonNest} (:4716-4767): half sphere of radius 10 downwards with a random shell of leaves, log,
     * planks, dirt, cobblestone and mossy cobblestone, air above, Leonopteryx spawner at the bottom.
     */
    public static void makeLeonNest(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                    final int cposz) {
        final int rad = 10;
        int dist;
        for (int j = 0; j <= rad; ++j) {
            for (int i = -rad; i <= rad; ++i) {
                for (int k = -rad; k <= rad; ++k) {
                    BlockState bid = AIR;
                    dist = j * j + i * i + k * k;
                    dist = (int) Math.sqrt(dist);
                    if (dist <= rad) {
                        if (dist >= rad - 2) {
                            final int which = rand.nextInt(6);
                            if (which == 0) {
                                bid = Blocks.OAK_LEAVES.defaultBlockState();
                            }
                            if (which == 1) {
                                bid = LegacyMeta.log(0);
                            }
                            if (which == 2) {
                                bid = LegacyMeta.planks(0);
                            }
                            if (which == 3) {
                                bid = Blocks.DIRT.defaultBlockState();
                            }
                            if (which == 4) {
                                bid = Blocks.COBBLESTONE.defaultBlockState();
                            }
                            if (which == 5) {
                                bid = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                            }
                        }
                        FastSetBlock(world, cposx + i, cposy - j, cposz + k, bid);
                    }
                }
            }
        }
        for (int j = 1; j <= 5; ++j) {
            for (int i = -rad; i <= rad; ++i) {
                for (int k = -rad; k <= rad; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, AIR);
                }
            }
        }
        world.setSpawner(cposx, cposy - (rad - 4), cposz, "orespawn:leonopteryx");
    }

    /** {@code makeCephadromeAltar} (:4769-4865): stepped altar, eye of ender block in the middle, extreme torches. */
    public static void makeCephadromeAltar(final StructureWriter world, final Random rand, final int cposx,
                                           final int cposy, final int cposz) {
        final BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        final BlockState stonebrick = Blocks.STONE_BRICKS.defaultBlockState();
        final BlockState endStone = Blocks.END_STONE.defaultBlockState();
        int width = 4;
        int length = 4;
        int j = 0;
        BlockState bid = cobblestone;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        width = 3;
        length = 3;
        j = 1;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                bid = cobblestone;
                if (k == 0 || i == 0) {
                    bid = stonebrick;
                }
                if ((k == -length || k == length) && (i == -width || i == width)) {
                    bid = stonebrick;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        width = 3;
        length = 3;
        j = 2;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                bid = AIR;
                if ((k == -length || k == length) && (i == -width || i == width)) {
                    bid = stonebrick;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        width = 3;
        length = 3;
        j = 3;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                bid = AIR;
                if ((k == -length || k == length) && (i == -width || i == width)) {
                    bid = endStone;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        final BlockState extremeTorch = GenericDungeonB.block("extremetorch");
        width = 3;
        length = 3;
        j = 4;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                bid = AIR;
                if ((k == -length || k == length) && (i == -width || i == width)) {
                    bid = extremeTorch;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        width = 2;
        length = 2;
        j = 2;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                bid = cobblestone;
                if (k == 0 || i == 0) {
                    bid = stonebrick;
                }
                if ((k == -length || k == length) && (i == -width || i == width)) {
                    bid = stonebrick;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
        final BlockState eye = GenericDungeonB.block("blockeyeofender");
        width = 1;
        length = 1;
        j = 3;
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                bid = cobblestone;
                if (k == 0 && i == 0) {
                    bid = eye;
                }
                if ((k == -length || k == length) && (i == -width || i == width)) {
                    bid = endStone;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
            }
        }
    }
}
