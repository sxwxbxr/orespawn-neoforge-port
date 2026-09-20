package com.swbr.orespawn.world.dungeon.b;

import static com.swbr.orespawn.world.dungeon.b.GenericDungeonB.FastSetBlock;

import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * {@code GenericDungeon} lines 2435-3216: Gold Fish Bowl, Ender Reaper Graveyard with {@code makeAGrave}, Urchin
 * Spawner, Spit Bug Lair, Igloo, Ender Dragon Hospital, Crystal Haunted House, Bouncy Castle
 * (verhalten/world-01.md, "Strukturen im Detail"). See {@link GenericDungeonB} for the conventions.
 *
 * <p>Random draws that the original made inside {@code if (chest != null)} are made unconditionally here: in 1.7.10
 * {@code world.setBlock(chest)} always left a chest, so the draws always happened; a clipped writer must not skip
 * them, or every later draw of the run shifts.
 */
public final class GenericDungeonB1 {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private GenericDungeonB1() {
    }

    /** {@code makeGoldFishBowl} (:2435-2513). */
    public static void makeGoldFishBowl(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                        final int cposz) {
        int j = 1;
        BlockState blk = Blocks.GLASS.defaultBlockState();
        for (int i = 0; i < 5; ++i) {
            for (int k = 0; k < 5; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        j = 2;
        for (int i = -1; i < 6; ++i) {
            for (int k = -1; k < 6; ++k) {
                blk = Blocks.SAND.defaultBlockState();
                if (i == -1 || k == -1 || i == 5 || k == 5) {
                    blk = Blocks.GLASS.defaultBlockState();
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        j = 3;
        for (int i = -1; i < 6; ++i) {
            for (int k = -1; k < 6; ++k) {
                blk = Blocks.WATER.defaultBlockState();
                if (i == -1 || k == -1 || i == 5 || k == 5) {
                    blk = Blocks.GLASS.defaultBlockState();
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        int k = 0;
        int i = 0;
        blk = Blocks.GLOWSTONE.defaultBlockState();
        FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
        k = 4;
        i = 4;
        FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
        k = 4;
        i = 0;
        FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
        k = 0;
        i = 4;
        FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
        j = 4;
        for (i = -1; i < 6; ++i) {
            for (k = -1; k < 6; ++k) {
                blk = Blocks.WATER.defaultBlockState();
                if (i == -1 || k == -1 || i == 5 || k == 5) {
                    blk = Blocks.GLASS.defaultBlockState();
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (j = 5; j < 8; ++j) {
            for (i = -1; i < 6; ++i) {
                for (k = -1; k < 6; ++k) {
                    blk = AIR;
                    if (i == -1 || k == -1 || i == 5 || k == 5) {
                        blk = Blocks.GLASS.defaultBlockState();
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        j = 8;
        blk = Blocks.GLASS.defaultBlockState();
        for (i = 0; i < 5; ++i) {
            for (k = 0; k < 5; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        i = 2;
        k = 2;
        j = 6;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:gold_fish");
    }

    /** {@code makeEnderReaperGraveyard} (:2515-2586). */
    public static void makeEnderReaperGraveyard(final StructureWriter world, final Random rand, final int cposx,
                                                final int cposy, final int cposz) {
        final int width = 11;
        final int length = 13;
        for (int j = 1; j < 5; ++j) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < length; ++k) {
                    if (GenericDungeonB.isAir(world.getBlock(cposx + i, cposy - j, cposz + k))) {
                        FastSetBlock(world, cposx + i, cposy - j, cposz + k, Blocks.END_STONE.defaultBlockState());
                    }
                }
            }
        }
        int j = 0;
        BlockState blk = Blocks.END_STONE.defaultBlockState();
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < length; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (j = 1; j < 5; ++j) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < length; ++k) {
                    blk = AIR;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) {
                        blk = Blocks.IRON_BARS.defaultBlockState();
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
                }
            }
        }
        int i = 1;
        int k = 1;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        i = width - 2;
        k = length - 2;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        i = 1;
        k = length - 2;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        i = width - 2;
        k = 1;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        makeAGrave(world, rand, cposx, cposy, cposz, 1, 6);
        makeAGrave(world, rand, cposx, cposy, cposz, 3, 4);
        makeAGrave(world, rand, cposx, cposy, cposz, 5, 4);
        makeAGrave(world, rand, cposx, cposy, cposz, 7, 4);
        makeAGrave(world, rand, cposx, cposy, cposz, 3, 8);
        makeAGrave(world, rand, cposx, cposy, cposz, 5, 8);
        makeAGrave(world, rand, cposx, cposy, cposz, 7, 8);
        makeAGrave(world, rand, cposx, cposy, cposz, 9, 6);
    }

    /** {@code makeAGrave} (:2588-2599): headstone, footstone, chest in the floor with 3-5 draws. */
    public static void makeAGrave(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                  final int cposz, final int xoff, final int zoff) {
        final WeightedRandomChestContent[] chestContents = LootListsB.GraveContentsList;
        FastSetBlock(world, cposx + xoff, cposy + 1, cposz + zoff - 1, Blocks.OBSIDIAN.defaultBlockState());
        FastSetBlock(world, cposx + xoff, cposy, cposz + zoff + 1, Blocks.OBSIDIAN.defaultBlockState());
        world.setChest(cposx + xoff, cposy, cposz + zoff, 0, chestContents, 3 + rand.nextInt(3), rand);
    }

    /**
     * {@code makeUrchinSpawner} (:2601-2658): three crystal rays of random direction, width and length, three Crystal
     * Urchin spawners above the origin, a chest below it with fixed slots.
     *
     * <p>The ray positions keep the {@code (int)} cast of a {@code float} (toward zero, R20 exception for the
     * GenericDungeon casts); the piece box of W12 carries one block of margin for the negative side.
     */
    public static void makeUrchinSpawner(final StructureWriter world, final Random rand, final int cposx,
                                         final int cposy, final int cposz) {
        for (int patchy = 3, i = 0; i < patchy; ++i) {
            BlockState bid = GenericDungeonB.block("crystalstone");
            if (i == 1) {
                bid = GenericDungeonB.block("crystalcrystal");
            }
            if (i == 2) {
                bid = GenericDungeonB.block("tigerseye");
            }
            final float dx = rand.nextFloat() - rand.nextFloat();
            final float dz = rand.nextFloat() - rand.nextFloat();
            final float dy = 0.5f + rand.nextFloat() / 2.0f;
            final int width = rand.nextInt(2);
            int length = 10 + width * 3 + rand.nextInt(5);
            if (i != 0) {
                length /= 2;
            }
            float rx = (float) cposx;
            float ry = (float) cposy;
            float rz = (float) cposz;
            for (int iy = 0; iy <= length; ++iy) {
                for (int ix = 0; ix <= width; ++ix) {
                    for (int iz = 0; iz <= width; ++iz) {
                        world.setBlock((int) (rx + ix), (int) ry, (int) (rz + iz), bid, Block.UPDATE_CLIENTS);
                    }
                }
                ry += dy;
                rx += dx;
                rz += dz;
            }
        }
        world.setSpawner(cposx, cposy + 1, cposz, "orespawn:crystal_urchin");
        world.setSpawner(cposx, cposy + 2, cposz, "orespawn:crystal_urchin");
        world.setSpawner(cposx, cposy + 3, cposz, "orespawn:crystal_urchin");
        world.setBlock(cposx, cposy, cposz, AIR, Block.UPDATE_CLIENTS);
        world.setChest(cposx, cposy - 1, cposz, 2, null, 0, rand);
        final int eggs = 1 + rand.nextInt(5);
        final int coal1 = 4 + rand.nextInt(16);
        final int coal2 = 4 + rand.nextInt(16);
        GenericDungeonB.setSlot(world, cposx, cposy - 1, cposz, 1, GenericDungeonB.stack("eggurchin", eggs));
        GenericDungeonB.setSlot(world, cposx, cposy - 1, cposz, 2, GenericDungeonB.stack("crystalcoal", coal1));
        GenericDungeonB.setSlot(world, cposx, cposy - 1, cposz, 3, GenericDungeonB.stack("crystalcoal", coal2));
    }

    /** {@code makeSpitBugLair} (:2660-2718). */
    public static void makeSpitBugLair(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                       final int cposz) {
        final int green = 5;
        final int dark_green = 13;
        final int width = 9;
        final WeightedRandomChestContent[] chestContents = LootListsB.SpitBugContentsList;
        final BlockState darkGreen = LegacyMeta.stainedClay(dark_green);
        final BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        for (int i = 0; i < width; ++i) {
            FastSetBlock(world, cposx + i, cposy + width - i + 2, cposz, darkGreen);
            FastSetBlock(world, cposx + i, cposy + width - i + 1, cposz, darkGreen);
            FastSetBlock(world, cposx + i, cposy + width - i, cposz, mossy);
            FastSetBlock(world, cposx - i, cposy + width - i + 2, cposz, darkGreen);
            FastSetBlock(world, cposx - i, cposy + width - i + 1, cposz, darkGreen);
            FastSetBlock(world, cposx - i, cposy + width - i, cposz, mossy);
        }
        FastSetBlock(world, cposx, cposy + width + 3, cposz, Blocks.EMERALD_ORE.defaultBlockState());
        FastSetBlock(world, cposx, cposy + width + 2, cposz, Blocks.EMERALD_ORE.defaultBlockState());
        FastSetBlock(world, cposx, cposy + width + 1, cposz, Blocks.EMERALD_ORE.defaultBlockState());
        world.setSpawner(cposx, cposy + width + 0, cposz, "orespawn:spit_bug");
        world.setSpawner(cposx, cposy + width - 1, cposz, "orespawn:spit_bug");
        world.setSpawner(cposx, cposy + width - 2, cposz, "orespawn:spit_bug");
        final BlockState greenClay = LegacyMeta.stainedClay(green);
        // stonebrick meta 3 = chiseled stone bricks (verhalten/world-01.md, "Blockzustände")
        final BlockState chiseled = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        for (int i = 0; i < width; ++i) {
            for (int j = -i; j <= i; ++j) {
                FastSetBlock(world, cposx - width + i + 1, cposy, cposz + j, greenClay);
                FastSetBlock(world, cposx + width - i - 1, cposy, cposz + j, greenClay);
                if (j == -i || j == i) {
                    FastSetBlock(world, cposx - width + i + 1, cposy + 1, cposz + j, darkGreen);
                    FastSetBlock(world, cposx + width - i - 1, cposy + 1, cposz + j, darkGreen);
                    FastSetBlock(world, cposx - width + i + 1, cposy + 2, cposz + j, chiseled);
                    FastSetBlock(world, cposx + width - i - 1, cposy + 2, cposz + j, chiseled);
                } else {
                    FastSetBlock(world, cposx - width + i + 1, cposy + 1, cposz + j, AIR);
                    FastSetBlock(world, cposx + width - i - 1, cposy + 1, cposz + j, AIR);
                    FastSetBlock(world, cposx - width + i + 1, cposy + 2, cposz + j, AIR);
                    FastSetBlock(world, cposx + width - i - 1, cposy + 2, cposz + j, AIR);
                }
            }
        }
        world.setChest(cposx, cposy + 1, cposz, 0, chestContents, 4 + rand.nextInt(4), rand);
    }

    /**
     * {@code makeIgloo} (:2720-2832): dome of float circles with the {@code (int)} cast of the original, oak door
     * on the west side, three spawners, a chest of sixteen coin-flip slots.
     */
    public static void makeIgloo(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                 final int cposz) {
        // Blocks.snow is the full snow block (snow_block); Blocks.snow_layer is the layer.
        final BlockState snow = Blocks.SNOW_BLOCK.defaultBlockState();
        final BlockState ice = Blocks.ICE.defaultBlockState();
        float currad = 6.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
            final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
            final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 1, (int) (cposz + curz + 0.5f), snow);
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 2, (int) (cposz + curz + 0.5f), ice);
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 3, (int) (cposz + curz + 0.5f), snow);
        }
        currad = 5.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
            final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
            final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 4, (int) (cposz + curz + 0.5f), ice);
        }
        currad = 4.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 5.0f) {
            final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
            final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 5, (int) (cposz + curz + 0.5f), snow);
        }
        currad = 3.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 10.0f) {
            final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
            final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 5, (int) (cposz + curz + 0.5f), ice);
        }
        currad = 2.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 15.0f) {
            final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
            final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 5, (int) (cposz + curz + 0.5f), snow);
        }
        currad = 1.0f;
        for (float curdeg = 0.0f; curdeg < 360.0f; curdeg += 15.0f) {
            final float curx = (float) (currad * Math.cos(Math.toRadians(curdeg)));
            final float curz = (float) (currad * Math.sin(Math.toRadians(curdeg)));
            FastSetBlock(world, (int) (cposx + curx + 0.5f), cposy + 5, (int) (cposz + curz + 0.5f), ice);
        }
        FastSetBlock(world, (int) (cposx - 6.0f + 0.5f), cposy, (int) (cposz + 0.5f), Blocks.OAK_PLANKS.defaultBlockState());
        FastSetBlock(world, (int) (cposx - 6.0f + 0.5f), cposy + 1, (int) (cposz + 0.5f), AIR);
        FastSetBlock(world, (int) (cposx - 6.0f + 0.5f), cposy + 2, (int) (cposz + 0.5f), AIR);
        GenericDungeonB.placeDoorBlock(world, (int) (cposx - 6.0f + 0.5f), cposy + 1, (int) (cposz + 0.5f), 2, Blocks.OAK_DOOR);
        world.setSpawner(cposx + 2, cposy + 1, cposz - 4, "orespawn:rat");
        world.setSpawner(cposx - 1, cposy + 1, cposz + 1, "orespawn:ghost");
        world.setSpawner(cposx + 3, cposy + 1, cposz + 4, "orespawn:ghost_pumpkin_skelly");
        final int cx = cposx - 3;
        final int cy = cposy + 1;
        final int cz = cposz - 3;
        world.setChest(cx, cy, cz, 2, null, 0, rand);
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 0, GenericDungeonB.stack(Items.COMPASS, 1));
        }
        if (rand.nextInt(2) == 0) {
            // Items.map is the empty map ("map", ItemEmptyMap); the filled one was Items.filled_map.
            GenericDungeonB.setSlot(world, cx, cy, cz, 1, GenericDungeonB.stack(Items.MAP, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 2, GenericDungeonB.stack(Items.COOKED_PORKCHOP, 8));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 3, GenericDungeonB.stack(Blocks.TORCH, 32));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 4, GenericDungeonB.stack(Items.COAL, 16));
        }
        if (rand.nextInt(2) == 0) {
            // Items.bed: the one 1.7.10 bed was red.
            GenericDungeonB.setSlot(world, cx, cy, cz, 5, GenericDungeonB.stack(Items.RED_BED, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 6, GenericDungeonB.stack(Items.RED_BED, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 7, GenericDungeonB.stack(Items.OAK_DOOR, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 8, GenericDungeonB.stack(Items.IRON_PICKAXE, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 9, GenericDungeonB.stack(Items.IRON_SWORD, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 10, GenericDungeonB.stack(Items.IRON_AXE, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 11, GenericDungeonB.stack(Items.BUCKET, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 13, GenericDungeonB.stack(Blocks.CHEST, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 14, GenericDungeonB.stack(Items.GOLD_NUGGET, 6));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 15, GenericDungeonB.stack(Items.GOLD_NUGGET, 8));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 16, GenericDungeonB.stack(Items.GOLD_NUGGET, 10));
        }
    }

    /**
     * {@code makeEnderDragonHospital} (:2834-3006).
     *
     * <p>The four ender crystals stand in the block that becomes bedrock right after, as in the original (the
     * catalogue leaves "1:1 or one block higher" open; the code is 1:1).
     */
    public static void makeEnderDragonHospital(final StructureWriter world, final Random rand, final int cposx,
                                               final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = LootListsB.HospitalContentsList;
        final BlockState eye = GenericDungeonB.block("blockeyeofender");
        for (int i = 0; i < 10; ++i) {
            for (int k = 0; k < 10; ++k) {
                for (int j = 0; j < 7; ++j) {
                    Block blk = Blocks.AIR;
                    if (i == 0 || k == 0 || i == 9 || k == 9) {
                        blk = Blocks.IRON_BARS;
                    }
                    if (i == 0 && k == 0) {
                        blk = Blocks.OBSIDIAN;
                    }
                    if (i == 9 && k == 9) {
                        blk = Blocks.OBSIDIAN;
                    }
                    if (i == 0 && k == 9) {
                        blk = Blocks.OBSIDIAN;
                    }
                    if (i == 9 && k == 0) {
                        blk = Blocks.OBSIDIAN;
                    }
                    if (j == 0) {
                        blk = Blocks.END_STONE;
                    }
                    if (j == 6 && (i == 0 || k == 0 || i == 9 || k == 9)) {
                        blk = Blocks.END_STONE;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk.defaultBlockState());
                }
            }
        }
        for (int i = 1; i < 9; ++i) {
            for (int k = 1; k < 9; ++k) {
                final int j = 7;
                BlockState blk = AIR;
                if (i == 1 || i == 8 || k == 1 || k == 8) {
                    blk = eye;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = 2; i < 8; ++i) {
            for (int k = 2; k < 8; ++k) {
                final int j = 8;
                BlockState blk = AIR;
                if (i == 2 || i == 7 || k == 2 || k == 7) {
                    blk = eye;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = 3; i < 7; ++i) {
            for (int k = 3; k < 7; ++k) {
                final int j = 9;
                BlockState blk = AIR;
                if (i == 3 || i == 6 || k == 3 || k == 6) {
                    blk = eye;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, blk);
            }
        }
        int i = -6;
        int j = 1;
        int k = 3;
        final BlockState endStone = Blocks.END_STONE.defaultBlockState();
        final BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        final BlockState glowstone = Blocks.GLOWSTONE.defaultBlockState();
        for (int m = 0; m < 6; ++m) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k, endStone);
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + 1, endStone);
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + 2, endStone);
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + 3, endStone);
            FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, bars);
            FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + 3, bars);
            FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k, glowstone);
            FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k + 3, glowstone);
            ++i;
            ++j;
        }
        final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        FastSetBlock(world, cposx + 0, cposy + 7, cposz + 0, obsidian);
        FastSetBlock(world, cposx + 0, cposy + 7, cposz + 9, obsidian);
        FastSetBlock(world, cposx + 9, cposy + 7, cposz + 0, obsidian);
        FastSetBlock(world, cposx + 9, cposy + 7, cposz + 9, obsidian);
        FastSetBlock(world, cposx + 0, cposy + 8, cposz + 0, obsidian);
        FastSetBlock(world, cposx + 0, cposy + 8, cposz + 9, obsidian);
        FastSetBlock(world, cposx + 9, cposy + 8, cposz + 0, obsidian);
        FastSetBlock(world, cposx + 9, cposy + 8, cposz + 9, obsidian);
        final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        // new EntityEnderCrystal(world) + setLocationAndAngles + spawnEntityInWorld; the yaw draw comes first.
        world.spawnEntity("minecraft:end_crystal", (double) (cposx + 0.5f), (double) (cposy + 9), (double) (cposz + 0.5f),
                rand.nextFloat() * 360.0f, 0.0f);
        FastSetBlock(world, cposx, cposy + 9, cposz, bedrock);
        world.spawnEntity("minecraft:end_crystal", (double) (cposx + 0.5f), (double) (cposy + 9), (double) (cposz + 9.5f),
                rand.nextFloat() * 360.0f, 0.0f);
        FastSetBlock(world, cposx, cposy + 9, cposz + 9, bedrock);
        world.spawnEntity("minecraft:end_crystal", (double) (cposx + 9.5f), (double) (cposy + 9), (double) (cposz + 0.5f),
                rand.nextFloat() * 360.0f, 0.0f);
        FastSetBlock(world, cposx + 9, cposy + 9, cposz, bedrock);
        world.spawnEntity("minecraft:end_crystal", (double) (cposx + 9.5f), (double) (cposy + 9), (double) (cposz + 9.5f),
                rand.nextFloat() * 360.0f, 0.0f);
        FastSetBlock(world, cposx + 9, cposy + 9, cposz + 9, bedrock);
        i = 3;
        k = 3;
        j = 9;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        i = 3;
        k = 6;
        j = 9;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        i = 6;
        k = 3;
        j = 9;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        i = 6;
        k = 6;
        j = 9;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:ender_reaper");
        i = 1;
        k = 1;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:nightmare");
        i = 1;
        k = 8;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:nightmare");
        i = 8;
        k = 1;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:nightmare");
        i = 8;
        k = 8;
        j = 1;
        world.setSpawner(cposx + i, cposy + j, cposz + k, "orespawn:nightmare");
        world.setChest(cposx + 4, cposy + 1, cposz + 4, 0, chestContents, 6 + rand.nextInt(5), rand);
    }

    /**
     * {@code makeCrystalHauntedHouse} (:3008-3115): 7x5x7 crystal-plank house, crystal furnace, crystal workbench,
     * a chest with fixed slots, Rat/Ghost/Ghost Pumpkin Skelly spawners stacked in the middle. {@code world.isRemote}
     * (:3023) is never true in world generation.
     */
    public static void makeCrystalHauntedHouse(final StructureWriter world, final Random rand, final int cposx,
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
        final BlockState planks = GenericDungeonB.block("crystalplanks");
        final BlockState stone = GenericDungeonB.block("crystalstone");
        for (int i = -width; i <= width; ++i) {
            for (int j = -length; j <= length; ++j) {
                for (int k = 0; k <= height + 1; ++k) {
                    if (k == height + 1) {
                        GenericDungeonB.setBlockNotify(world, x + i, y + k, z + j, planks);
                    } else if (k == 0) {
                        GenericDungeonB.setBlockNotify(world, x + i, y + k, z + j, stone);
                    } else if (i == width || j == length || i == -width || j == -length) {
                        if (k == height) {
                            GenericDungeonB.setBlockNotify(world, x + i, y + k, z + j, Blocks.GLASS.defaultBlockState());
                        } else if ((k == 1 || k == 2) && i == deltax * width && j == deltaz * length) {
                            GenericDungeonB.setBlockNotify(world, x + i, y + k, z + j, AIR);
                        } else {
                            GenericDungeonB.setBlockNotify(world, x + i, y + k, z + j, planks);
                        }
                    } else {
                        GenericDungeonB.setBlockNotify(world, x + i, y + k, z + j, AIR);
                    }
                }
            }
        }
        int i = 2;
        final int k = 1;
        final int j = length - 1;
        // setBlock(CrystalFurnaceBlock) then setBlockMetadataWithNotify(stuffdir = 2): front north.
        BlockState furnace = GenericDungeonB.block("crystalfurnace");
        if (furnace.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            furnace = furnace.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.from3DDataValue(stuffdir));
        }
        GenericDungeonB.setBlockNotify(world, x + i * deltax + j * deltaz, y + k, z + i * deltaz + j * deltax, furnace);
        i = 1;
        GenericDungeonB.setBlockNotify(world, x + i * deltax + j * deltaz, y + k, z + i * deltaz + j * deltax,
                GenericDungeonB.block("crystalworkbench"));
        i = 0;
        final int cx = x + i * deltax + j * deltaz;
        final int cy = y + k;
        final int cz = z + i * deltaz + j * deltax;
        world.setChest(cx, cy, cz, stuffdir, null, 0, rand);
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 0, GenericDungeonB.stack(Items.COMPASS, 1));
        }
        if (rand.nextInt(3) != 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 2, GenericDungeonB.stack("cookedpeacock", 8));
        }
        if (rand.nextInt(3) != 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 3, GenericDungeonB.stack("crystaltorch", 32));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 4, GenericDungeonB.stack("crystalcoal", 16));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 5, GenericDungeonB.stack(Items.RED_BED, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 6, GenericDungeonB.stack(Items.RED_BED, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 7, GenericDungeonB.stack(Items.OAK_DOOR, 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 8, GenericDungeonB.stack("crystalpinkpickaxe", 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 9, GenericDungeonB.stack("crystalpinksword", 1));
        }
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 10, GenericDungeonB.stack("crystalpinkaxe", 1));
        }
        GenericDungeonB.setSlot(world, cx, cy, cz, 11, GenericDungeonB.stack("krakenrepellent", 1));
        if (rand.nextInt(2) == 0) {
            GenericDungeonB.setSlot(world, cx, cy, cz, 13, GenericDungeonB.stack(Blocks.CHEST, 1));
        }
        world.setSpawner(cposx, cposy + 1, cposz, "orespawn:rat");
        world.setSpawner(cposx, cposy + 2, cposz, "orespawn:ghost");
        world.setSpawner(cposx, cposy + 3, cposz, "orespawn:ghost_pumpkin_skelly");
    }

    /** {@code makeBouncyCastle} (:3117-3216): lavafoam box, nine spawners in three rows, one chest. */
    public static void makeBouncyCastle(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                        final int cposz) {
        final WeightedRandomChestContent[] chestContents = LootListsB.BouncyContentsList;
        final int width;
        final int length = width = 4;
        final int height = 5;
        final BlockState lavafoam = GenericDungeonB.block("lavafoam");
        for (int i = -width; i <= width; ++i) {
            for (int j = -length; j <= length; ++j) {
                for (int k = 0; k < height; ++k) {
                    BlockState bid = AIR;
                    if (k == height - 1 || k == 0) {
                        bid = lavafoam;
                    }
                    if (i == -width || i == width) {
                        bid = lavafoam;
                    }
                    if (j == -length || j == length) {
                        bid = lavafoam;
                    }
                    if ((i == -width || i == width) && (j == -length || j == length)) {
                        bid = LegacyMeta.stainedClay(14);
                    }
                    if ((k == 1 || k == 2) && i == 0 && j == -length) {
                        bid = AIR;
                    }
                    world.setBlock(cposx + i, cposy + k, cposz + j, bid, Block.UPDATE_CLIENTS);
                }
            }
        }
        world.setSpawner(cposx - 1, cposy + 3, cposz + length - 1, "minecraft:silverfish");
        world.setSpawner(cposx, cposy + 3, cposz + length - 1, "orespawn:rat");
        world.setSpawner(cposx + 1, cposy + 3, cposz + length - 1, "orespawn:scorpion");
        world.setSpawner(cposx + width - 1, cposy + 3, cposz - 1, "minecraft:silverfish");
        world.setSpawner(cposx + width - 1, cposy + 3, cposz, "orespawn:rat");
        world.setSpawner(cposx + width - 1, cposy + 3, cposz + 1, "orespawn:scorpion");
        world.setSpawner(cposx - width + 1, cposy + 3, cposz - 1, "minecraft:silverfish");
        world.setSpawner(cposx - width + 1, cposy + 3, cposz, "orespawn:rat");
        world.setSpawner(cposx - width + 1, cposy + 3, cposz + 1, "orespawn:scorpion");
        world.setChest(cposx + width - 1, cposy + 3, cposz + length - 1, 2, chestContents, 6 + rand.nextInt(5), rand);
    }
}
