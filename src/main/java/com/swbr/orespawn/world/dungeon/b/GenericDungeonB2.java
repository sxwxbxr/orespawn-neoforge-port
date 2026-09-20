package com.swbr.orespawn.world.dungeon.b;

import static com.swbr.orespawn.world.dungeon.b.GenericDungeonB.FastSetBlock;

import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Optional;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code GenericDungeon} lines 3218-4083: Ender Castle with {@code makeAColumn}, Damsel in Distress, Inca Pyramid
 * with {@code makepoolalter}, {@code makeincagraves} and {@code makeincagrave} (verhalten/world-01.md). See
 * {@link GenericDungeonB} for the conventions.
 *
 * <p>The block comparisons of the original ({@code bid == Blocks.bedrock}, {@code bid != Blocks.air}) are kept as
 * comparisons of the chosen state, in the original order.
 */
public final class GenericDungeonB2 {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private GenericDungeonB2() {
    }

    /**
     * {@code makeEnderCastle} (:3218-3502). The egg blocks of the battlement (y 10) roll {@code nextInt(4)} per block
     * in the original loop order.
     */
    public static void makeEnderCastle(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                       final int cposz) {
        final int width = 22;
        final int height = 12;
        final WeightedRandomChestContent[] chestContents = LootListsB.EnderCastleContentsList;
        final Block enderKnightEgg = GenericDungeonB.block("oreenderknight").getBlock();
        final Block enderReaperEgg = GenericDungeonB.block("oreenderreaper").getBlock();
        final Block endermanEgg = GenericDungeonB.block("oreenderman").getBlock();
        final Block enderDragonEgg = GenericDungeonB.block("oreenderdragon").getBlock();
        final Block eyeOfEnder = GenericDungeonB.block("blockeyeofender").getBlock();
        final Block enderPearlBlock = GenericDungeonB.block("blockenderpearl").getBlock();
        Block bid = Blocks.OBSIDIAN;
        for (int i = -3; i <= width + 3; ++i) {
            for (int k = -3; k <= width + 3; ++k) {
                for (int j = 0; j <= 1; ++j) {
                    bid = Blocks.AIR;
                    if (j == 0) {
                        bid = Blocks.OBSIDIAN;
                    }
                    if (j == 1 && (i == -3 || i == width + 3 || (k == width + 3 | k == -3))) {
                        bid = Blocks.IRON_BARS;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        for (int i = 0; i <= width; ++i) {
            for (int k = 0; k <= width; ++k) {
                for (int j = 1; j <= height; ++j) {
                    bid = Blocks.AIR;
                    if (i == 0 || i == width || (k == width | k == 0)) {
                        bid = Blocks.BEDROCK;
                    }
                    if (j == height && bid == Blocks.BEDROCK && (i + k & 0x1) == 0x0) {
                        bid = Blocks.AIR;
                    }
                    if (j == height - 2 && bid == Blocks.BEDROCK && (i + k & 0x1) == 0x0) {
                        final int which = rand.nextInt(4);
                        if (which == 0) {
                            bid = enderKnightEgg;
                        }
                        if (which == 1) {
                            bid = enderReaperEgg;
                        }
                        if (which == 2) {
                            bid = endermanEgg;
                        }
                        if (which == 3) {
                            bid = enderDragonEgg;
                        }
                    }
                    if (j == 7 && bid == Blocks.BEDROCK && (i + k & 0x1) != 0x0) {
                        bid = eyeOfEnder;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        for (int i = -1; i <= width + 1; ++i) {
            for (int k = -1; k <= width + 1; ++k) {
                for (int j = 1; j <= height - 1; ++j) {
                    bid = Blocks.AIR;
                    if (j == 6 || j > 8) {
                        if (i == -1 || i == width + 1 || (k == width + 1 | k == -1)) {
                            bid = Blocks.BEDROCK;
                        }
                        if (j == 6 && bid != Blocks.AIR && rand.nextInt(2) == 1) {
                            FastSetBlock(world, cposx + i, cposy + j - 1, cposz + k, enderPearlBlock.defaultBlockState());
                            if (rand.nextInt(3) == 1) {
                                FastSetBlock(world, cposx + i, cposy + j - 2, cposz + k, enderPearlBlock.defaultBlockState());
                            }
                        }
                    }
                    if (j == 7) {
                        if (i == -1 || i == width + 1 || (k == width + 1 | k == -1)) {
                            bid = Blocks.BEDROCK;
                        }
                        if (bid == Blocks.BEDROCK && (i + k & 0x1) == 0x0) {
                            bid = Blocks.AIR;
                        }
                    }
                    if (bid != Blocks.AIR) {
                        FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                    }
                }
            }
        }
        makeAColumn(world, cposx - 2, cposy, cposz - 2, height + 1, 0);
        makeAColumn(world, cposx + width - 2, cposy, cposz - 2, height + 1, 1);
        makeAColumn(world, cposx - 2, cposy, cposz + width - 2, height + 1, 2);
        makeAColumn(world, cposx + width - 2, cposy, cposz + width - 2, height + 1, 3);
        final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        int j = 8;
        for (int i = 1; i <= width - 1; ++i) {
            for (int k = 1; k <= width - 1; ++k) {
                BlockState floor = obsidian;
                if (i == width / 2 || k == width / 2 || i == k || i == width - k) {
                    floor = bedrock;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, floor);
            }
        }
        j = 9;
        for (int i = -2; i <= 2; ++i) {
            for (int k = -2; k <= 2; ++k) {
                FastSetBlock(world, cposx + i + width / 2, cposy + j, cposz + k + width / 2, Blocks.LAVA.defaultBlockState());
            }
        }
        for (int m = -1; m <= 1; ++m) {
            FastSetBlock(world, cposx + width / 2 + m, cposy + j, cposz + width / 2 + 3, bedrock);
            FastSetBlock(world, cposx + width / 2 + m, cposy + j, cposz + width / 2 - 3, bedrock);
            FastSetBlock(world, cposx + width / 2 + 3, cposy + j, cposz + width / 2 + m, bedrock);
            FastSetBlock(world, cposx + width / 2 - 3, cposy + j, cposz + width / 2 + m, bedrock);
        }
        FastSetBlock(world, cposx + width / 2 - 2, cposy + j, cposz + width / 2 - 2, bedrock);
        FastSetBlock(world, cposx + width / 2 + 2, cposy + j, cposz + width / 2 + 2, bedrock);
        FastSetBlock(world, cposx + width / 2 - 2, cposy + j, cposz + width / 2 + 2, bedrock);
        FastSetBlock(world, cposx + width / 2 + 2, cposy + j, cposz + width / 2 - 2, bedrock);
        FastSetBlock(world, cposx + width / 2, cposy + j, cposz + width / 2, bedrock);
        // world.setBlock(ender_chest, 2, 2): front north
        world.setBlock(cposx + width / 2, cposy + j + 1, cposz + width / 2, Blocks.ENDER_CHEST, 2, Block.UPDATE_CLIENTS);
        FastSetBlock(world, cposx + width / 2, cposy + j + 2, cposz + width / 2, obsidian);
        FastSetBlock(world, cposx + width / 2, cposy + j + 3, cposz + width / 2, bedrock);
        FastSetBlock(world, cposx + width / 2 - 1, cposy + j + 3, cposz + width / 2, bedrock);
        FastSetBlock(world, cposx + width / 2 + 1, cposy + j + 3, cposz + width / 2, bedrock);
        FastSetBlock(world, cposx + width / 2, cposy + j + 3, cposz + width / 2 - 1, bedrock);
        FastSetBlock(world, cposx + width / 2, cposy + j + 3, cposz + width / 2 + 1, bedrock);
        final BlockState torch = Blocks.TORCH.defaultBlockState();
        FastSetBlock(world, cposx + width / 2 - 1, cposy + j + 4, cposz + width / 2, torch);
        FastSetBlock(world, cposx + width / 2 + 1, cposy + j + 4, cposz + width / 2, torch);
        FastSetBlock(world, cposx + width / 2, cposy + j + 4, cposz + width / 2 - 1, torch);
        FastSetBlock(world, cposx + width / 2, cposy + j + 4, cposz + width / 2 + 1, torch);
        FastSetBlock(world, cposx + width / 2, cposy + j + 4, cposz + width / 2, bedrock);
        FastSetBlock(world, cposx + width / 2, cposy + j + 5, cposz + width / 2, bedrock);
        FastSetBlock(world, cposx + width / 2, cposy + j + 6, cposz + width / 2, Blocks.DRAGON_EGG.defaultBlockState());
        world.setSpawner(cposx + width / 2 + 5, cposy + j, cposz + width / 2 + 5, "orespawn:ender_reaper");
        world.setSpawner(cposx + width / 2 + 5, cposy + j + 1, cposz + width / 2 + 5, "orespawn:ender_knight");
        world.setSpawner(cposx + width / 2 - 5, cposy + j, cposz + width / 2 + 5, "orespawn:ender_reaper");
        world.setSpawner(cposx + width / 2 - 5, cposy + j + 1, cposz + width / 2 + 5, "orespawn:ender_knight");
        world.setSpawner(cposx + width / 2 + 5, cposy + j, cposz + width / 2 - 5, "orespawn:ender_reaper");
        world.setSpawner(cposx + width / 2 + 5, cposy + j + 1, cposz + width / 2 - 5, "orespawn:ender_knight");
        world.setSpawner(cposx + width / 2 - 5, cposy + j, cposz + width / 2 - 5, "orespawn:ender_reaper");
        world.setSpawner(cposx + width / 2 - 5, cposy + j + 1, cposz + width / 2 - 5, "orespawn:ender_knight");
        j = 4;
        final BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        for (int i = 1; i <= width - 1; ++i) {
            for (int k = 1; k <= width - 1; ++k) {
                BlockState level = AIR;
                if (i <= 5 || k <= 5 || i >= width - 5 || k >= width - 5) {
                    level = bedrock;
                }
                if (!level.isAir()) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, level);
                }
                if (i == 5 && k >= 5 && k <= width - 5) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 3, cposz + k, bars);
                }
                if (i == width - 5 && k >= 5 && k <= width - 5) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 3, cposz + k, bars);
                }
                if (k == 5 && i >= 5 && i <= width - 5) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 3, cposz + k, bars);
                }
                if (k == width - 5 && i >= 5 && i <= width - 5) {
                    FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k, bars);
                    FastSetBlock(world, cposx + i, cposy + j + 3, cposz + k, bars);
                }
            }
        }
        j = 3;
        int k = width / 2;
        int i = width - 6;
        for (int m = -1; m <= 1; ++m) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + m, bedrock);
        }
        j = 2;
        k = width / 2;
        i = width - 7;
        for (int m = -1; m <= 1; ++m) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + m, bedrock);
        }
        j = 1;
        k = width / 2;
        i = width - 8;
        for (int m = -1; m <= 1; ++m) {
            FastSetBlock(world, cposx + i, cposy + j, cposz + k + m, bedrock);
        }
        j = 4;
        i = width - 5;
        for (int m = -1; m <= 1; ++m) {
            FastSetBlock(world, cposx + i, cposy + j + 1, cposz + k + m, AIR);
            FastSetBlock(world, cposx + i, cposy + j + 2, cposz + k + m, AIR);
            FastSetBlock(world, cposx + i, cposy + j + 3, cposz + k + m, AIR);
        }
        j = 1;
        world.setSpawner(cposx + width / 2, cposy + j, cposz + width / 2, "orespawn:ender_reaper");
        world.setSpawner(cposx + width / 2, cposy + j + 1, cposz + width / 2, "orespawn:ender_knight");
        j = 5;
        world.setSpawner(cposx + 1, cposy + j, cposz + width / 2 - 1, "orespawn:cave_fisher");
        world.setSpawner(cposx + 1, cposy + j, cposz + width / 2 + 1, "orespawn:cave_fisher");
        world.setChest(cposx + 1, cposy + j, cposz + width / 2, 2, chestContents, 6 + rand.nextInt(5), rand);
        world.setSpawner(cposx + width / 2 - 1, cposy + j, cposz + 1, "orespawn:cave_fisher");
        world.setSpawner(cposx + width / 2 + 1, cposy + j, cposz + 1, "orespawn:cave_fisher");
        world.setChest(cposx + width / 2, cposy + j, cposz + 1, 3, chestContents, 6 + rand.nextInt(5), rand);
        world.setSpawner(cposx + width / 2 - 1, cposy + j, cposz + width - 1, "orespawn:cave_fisher");
        world.setSpawner(cposx + width / 2 + 1, cposy + j, cposz + width - 1, "orespawn:cave_fisher");
        world.setChest(cposx + width / 2, cposy + j, cposz + width - 1, 4, chestContents, 6 + rand.nextInt(5), rand);
    }

    /** {@code makeAColumn} (:3504-3628): corner tower of the Ender Castle with its nether-brick spiral. */
    private static void makeAColumn(final StructureWriter world, final int cposx, final int cposy, final int cposz,
                                    final int height, final int dir) {
        final int width = 4;
        final int halfwidth = 2;
        int step = dir;
        final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        for (int i = -2; i <= width + 2; ++i) {
            for (int k = -2; k <= width + 2; ++k) {
                final int j = height + 2;
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, obsidian);
            }
        }
        for (int i = -2; i <= width + 2; ++i) {
            for (int k = -2; k <= width + 2; ++k) {
                Block bid = Blocks.AIR;
                if (i == -2 || i == width + 2 || (k == width + 2 | k == -2)) {
                    bid = Blocks.OBSIDIAN;
                }
                final int j = height + 3;
                if (bid != Blocks.AIR && (i + k & 0x1) == 0x0) {
                    bid = Blocks.AIR;
                }
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
            }
        }
        int i;
        for (i = 0; i <= width; ++i) {
            for (int k = 0; k <= width; ++k) {
                for (int j = 1; j <= height + 2; ++j) {
                    Block bid = Blocks.AIR;
                    if (i == 0 || i == width || (k == width | k == 0)) {
                        bid = Blocks.OBSIDIAN;
                    }
                    if ((j % 3 == 0 || j % 3 == 1) && j != height + 2 && bid == Blocks.OBSIDIAN && (i == halfwidth || k == halfwidth)) {
                        bid = Blocks.IRON_BARS;
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid.defaultBlockState());
                }
            }
        }
        if (dir == 0) {
            for (int j = 1; j <= 2; ++j) {
                FastSetBlock(world, cposx + width, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx + width - 1, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx + width, cposy + j, cposz + width - 1, AIR);
            }
            for (int j = 9; j <= 10; ++j) {
                FastSetBlock(world, cposx + width, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx + width - 1, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx + width, cposy + j, cposz + width - 1, AIR);
            }
        }
        if (dir == 1) {
            for (int j = 1; j <= 2; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx + 1, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx, cposy + j, cposz + width - 1, AIR);
            }
            for (int j = 9; j <= 10; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx + 1, cposy + j, cposz + width, AIR);
                FastSetBlock(world, cposx, cposy + j, cposz + width - 1, AIR);
            }
            if (++step > 3) {
                step = 0;
            }
        }
        if (dir == 2) {
            for (int j = 1; j <= 2; ++j) {
                FastSetBlock(world, cposx + width, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx + width - 1, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx + width, cposy + j, cposz + 1, AIR);
            }
            for (int j = 9; j <= 10; ++j) {
                FastSetBlock(world, cposx + width, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx + width - 1, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx + width, cposy + j, cposz + 1, AIR);
            }
            if (++step > 3) {
                step = 0;
            }
            if (++step > 3) {
                step = 0;
            }
        }
        if (dir == 3) {
            for (int j = 1; j <= 2; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx + 1, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx, cposy + j, cposz + 1, AIR);
            }
            for (int j = 9; j <= 10; ++j) {
                FastSetBlock(world, cposx, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx + 1, cposy + j, cposz, AIR);
                FastSetBlock(world, cposx, cposy + j, cposz + 1, AIR);
            }
            if (++step > 3) {
                step = 0;
            }
            if (++step > 3) {
                step = 0;
            }
        }
        final BlockState netherBrick = Blocks.NETHER_BRICKS.defaultBlockState();
        int k = 0;
        for (int j = 1; j <= height + 2; ++j) {
            if (step == 0) {
                k = (i = 1);
            }
            if (step == 1) {
                i = 1;
                k = 3;
            }
            if (step == 2) {
                i = 3;
                k = 3;
            }
            if (step == 3) {
                i = 3;
                k = 1;
            }
            if (++step > 3) {
                step = 0;
            }
            FastSetBlock(world, cposx + i, cposy + j, cposz + k, netherBrick);
        }
    }

    /**
     * {@code makeDamselInDistress} (:3630-3735): cobblestone house with moss rolls per block, iron-bar cell, two
     * Scorpion spawners, a chest and a Girlfriend in the cell. {@code world.isRemote} (:3641) is never true here.
     */
    public static void makeDamselInDistress(final StructureWriter world, final Random rand, final int cposx,
                                            final int cposy, final int cposz) {
        final WeightedRandomChestContent[] chestContents = LootListsB.DamselContentsList;
        final int width;
        final int length = width = 4;
        final int height = 5;
        Block bid;
        for (int i = -width; i <= width; ++i) {
            for (int j = -length; j <= length; ++j) {
                for (int k = 0; k < height; ++k) {
                    bid = Blocks.AIR;
                    if (k == 0) {
                        bid = Blocks.COBBLESTONE;
                    }
                    if (i == -width || i == width) {
                        bid = Blocks.COBBLESTONE;
                    }
                    if (j == -length || j == length) {
                        bid = Blocks.COBBLESTONE;
                    }
                    if (bid == Blocks.COBBLESTONE && rand.nextInt(8) == 1) {
                        bid = Blocks.MOSSY_COBBLESTONE;
                    }
                    if ((k == 1 || k == 2 || k == 3) && (i == 0 || i == -1 || i == 1) && j == -length) {
                        bid = Blocks.AIR;
                    }
                    FastSetBlock(world, cposx + i, cposy + k, cposz + j, bid.defaultBlockState());
                }
            }
        }
        for (int i = -width + 1; i <= width - 1; ++i) {
            for (int j = -length; j <= length - 1; ++j) {
                final int k = height;
                bid = Blocks.COBBLESTONE;
                if (rand.nextInt(8) == 1) {
                    bid = Blocks.MOSSY_COBBLESTONE;
                }
                FastSetBlock(world, cposx + i, cposy + k, cposz + j, bid.defaultBlockState());
            }
        }
        for (int i = -width + 2; i <= width - 2; ++i) {
            for (int j = -length; j <= length - 2; ++j) {
                final int k = height + 1;
                bid = Blocks.COBBLESTONE;
                if (rand.nextInt(8) == 1) {
                    bid = Blocks.MOSSY_COBBLESTONE;
                }
                FastSetBlock(world, cposx + i, cposy + k, cposz + j, bid.defaultBlockState());
            }
        }
        int k = height;
        int j = -length;
        for (int m = width; m >= 0; --m) {
            for (int i = m; i >= 0; --i) {
                bid = Blocks.COBBLESTONE;
                if (rand.nextInt(8) == 1) {
                    bid = Blocks.MOSSY_COBBLESTONE;
                }
                FastSetBlock(world, cposx + i, cposy + k, cposz + j, bid.defaultBlockState());
                bid = Blocks.COBBLESTONE;
                if (rand.nextInt(8) == 1) {
                    bid = Blocks.MOSSY_COBBLESTONE;
                }
                FastSetBlock(world, cposx - i, cposy + k, cposz + j, bid.defaultBlockState());
            }
            ++k;
        }
        for (int i = -width + 1; i < width; ++i) {
            for (j = 1; j < height; ++j) {
                k = length - 3;
                FastSetBlock(world, cposx - i, cposy + j, cposz + k, Blocks.IRON_BARS.defaultBlockState());
            }
        }
        world.setSpawner(cposx - width + 1, cposy + 1, cposz - length + 1, "orespawn:scorpion");
        world.setSpawner(cposx + width - 1, cposy + 1, cposz - length + 1, "orespawn:scorpion");
        world.setChest(cposx + width - 1, cposy + 1, cposz + length - 1, 2, chestContents, 10 + rand.nextInt(5), rand);
        // EntityList.createEntityByName("Girlfriend") never returned null in 1.7.10, so the yaw was always drawn.
        world.spawnEntity("orespawn:girlfriend", (double) (cposx - width + 2), (double) (cposy + 1),
                (double) (cposz + length - 1), rand.nextFloat() * 360.0f, 0.0f);
    }

    /**
     * {@code makeIncaPyramid} (:3737-3994): ten-step pyramid, four stairways that fill stone down to the first
     * non-air block, the temple on top (lit redstone lamps, R18), five water altars, four Creeper Repellents, a
     * Molenoid spawner, a trapdoor over a ladder shaft and 24 graves inside. {@code world.isRemote} (:3748) is never
     * true here.
     */
    public static void makeIncaPyramid(final StructureWriter world, final Random rand, int cposx, int cposy, int cposz) {
        BlockState bid;
        final int width = 21;
        final int depth = 11;
        final int height = 9;
        final int basewidth = 41;
        final int basedepth = 31;
        final int baseheight = 10;
        final BlockState stone = Blocks.STONE.defaultBlockState();
        final BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        final BlockState mossy = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        final BlockState stonebrick = Blocks.STONE_BRICKS.defaultBlockState();
        // stone_slab metadata 0 = smooth stone slab (verhalten/world-01.md, "Blockzustände")
        final BlockState slab = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        for (int j = 0; j < baseheight; ++j) {
            for (int i = 0; i < basewidth - j * 2; ++i) {
                for (int k = 0; k < basedepth - j * 2; ++k) {
                    bid = AIR;
                    if (i == 0 || k == 0 || i == basewidth - j * 2 - 1 || k == basedepth - j * 2 - 1) {
                        bid = stone;
                        if (rand.nextInt(2) == 0) {
                            bid = cobblestone;
                        }
                        if (rand.nextInt(4) == 0) {
                            bid = mossy;
                        }
                    }
                    if (j == 0) {
                        bid = stonebrick;
                    }
                    if (k == 1 && j % 3 == 2 && i != 0 && i != basewidth - j * 2 - 1) {
                        bid = LegacyMeta.torch(Blocks.TORCH, 3);
                    }
                    FastSetBlock(world, cposx + i + j, cposy + j, cposz + k + j, bid);
                    if (k == basedepth - j * 2 - 1 && j % 3 == 2 && i != 0 && i != basewidth - j * 2 - 1) {
                        FastSetBlock(world, cposx + i + j, cposy + j, cposz + k + j - 1, LegacyMeta.torch(Blocks.TORCH, 4));
                    }
                }
            }
        }
        for (int m = 0; m < baseheight * 2 - 1; ++m) {
            final int i = -baseheight + m;
            for (int p = -2; p <= 2; ++p) {
                int k = basedepth / 2;
                k += p;
                int j = m / 2;
                incaStairColumn(world, cposx + i, cposy, cposz + k, j, p, m, baseheight, stonebrick, slab, stone);
            }
        }
        for (int m = 0; m < baseheight * 2 - 1; ++m) {
            final int i = basewidth + baseheight - m - 1;
            for (int p = -2; p <= 2; ++p) {
                int k = basedepth / 2;
                k += p;
                int j = m / 2;
                incaStairColumn(world, cposx + i, cposy, cposz + k, j, p, m, baseheight, stonebrick, slab, stone);
            }
        }
        for (int m = 0; m < baseheight * 2 - 1; ++m) {
            final int k = -baseheight + m;
            for (int p = -2; p <= 2; ++p) {
                int i = basewidth / 2;
                i += p;
                int j = m / 2;
                incaStairColumn(world, cposx + i, cposy, cposz + k, j, p, m, baseheight, stonebrick, slab, stone);
            }
        }
        for (int m = 0; m < baseheight * 2 - 1; ++m) {
            final int k = basedepth + baseheight - m - 1;
            for (int p = -2; p <= 2; ++p) {
                int i = basewidth / 2;
                i += p;
                int j = m / 2;
                incaStairColumn(world, cposx + i, cposy, cposz + k, j, p, m, baseheight, stonebrick, slab, stone);
            }
        }
        cposx += baseheight;
        cposy += baseheight;
        cposz += baseheight;
        final BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < depth; ++k) {
                    bid = AIR;
                    if (i == 0 || k == 0 || i == width - 1 || k == depth - 1) {
                        bid = stone;
                        if (rand.nextInt(2) == 0) {
                            bid = cobblestone;
                        }
                        if (rand.nextInt(4) == 0) {
                            bid = mossy;
                        }
                    }
                    if (j == 0 || j == height - 1) {
                        bid = stonebrick;
                    }
                    if (j == 1 || j == 2 || j == 3) {
                        if ((k == 0 || k == depth - 1) && i >= width / 2 - 1 && i <= width / 2 + 1) {
                            if (j == 3) {
                                bid = fence;
                            } else {
                                bid = AIR;
                            }
                        }
                        if ((i == 0 || i == width - 1) && k >= depth / 2 - 1 && k <= depth / 2 + 1) {
                            if (j == 3) {
                                bid = fence;
                            } else {
                                bid = AIR;
                            }
                        }
                    }
                    if ((j == height - 3 || j == height - 2) && (i + k) % 2 == 1) {
                        if (j == height - 3) {
                            if (!bid.isAir()) {
                                bid = GenericDungeonB.litRedstoneLamp();
                            }
                        } else {
                            bid = AIR;
                        }
                    }
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        int j = height;
        for (int i = -1; i <= width; ++i) {
            for (int k = -1; k <= depth; ++k) {
                if ((i == -1 || k == -1 || i == width || k == depth) && (i + k & 0x1) == 0x1) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, slab);
                }
            }
        }
        makepoolalter(world, cposx + 1, cposy, cposz + 1);
        makepoolalter(world, cposx + width - 2, cposy, cposz + depth - 2);
        makepoolalter(world, cposx + 1, cposy, cposz + depth - 2);
        makepoolalter(world, cposx + width - 2, cposy, cposz + 1);
        makepoolalter(world, cposx + width / 2, cposy, cposz + depth / 2);
        final Optional<BlockState> repellent = GenericDungeonB.optionalBlock("creeperrepellent");
        if (repellent.isPresent()) {
            world.setBlock(cposx + width / 2 - 1, cposy + 2, cposz + depth / 2 - 1, repellent.get(), Block.UPDATE_CLIENTS);
            world.setBlock(cposx + width / 2 + 1, cposy + 2, cposz + depth / 2 + 1, repellent.get(), Block.UPDATE_CLIENTS);
            world.setBlock(cposx + width / 2 - 1, cposy + 2, cposz + depth / 2 + 1, repellent.get(), Block.UPDATE_CLIENTS);
            world.setBlock(cposx + width / 2 + 1, cposy + 2, cposz + depth / 2 - 1, repellent.get(), Block.UPDATE_CLIENTS);
        }
        world.setSpawner(cposx + width / 2 - 2, cposy + 1, cposz + depth / 2, "orespawn:molenoid");
        FastSetBlock(world, cposx + width / 2 + 2, cposy + 1, cposz + depth / 2, GenericDungeonB.trapdoor(3));
        FastSetBlock(world, cposx + width / 2 + 2, cposy, cposz + depth / 2, AIR);
        final int i = cposx + width / 2 + 2;
        final int k = cposz + depth / 2;
        for (j = 1; j < baseheight; ++j) {
            FastSetBlock(world, i, cposy - j, k + 1, cobblestone);
            world.setBlock(i, cposy - j, k, Blocks.LADDER, 2, Block.UPDATE_CLIENTS);
        }
        makeincagraves(world, rand, cposx - baseheight, cposy - baseheight, cposz - baseheight, basewidth, basedepth);
    }

    /**
     * The body of the four stairway loops of {@code makeIncaPyramid} (:3782-3807 and its three copies, which differ
     * only in how column {@code (x, z)} is derived): edge rail of stone bricks with a torch at both ends, slab steps
     * on odd {@code m}, then stone down to the first non-air block. Factored out because the four copies are
     * identical statement for statement.
     */
    private static void incaStairColumn(final StructureWriter world, final int x, final int cposy, final int z, int j,
                                        final int p, final int m, final int baseheight, final BlockState stonebrick,
                                        final BlockState slab, final BlockState stone) {
        if (p < -1 || p > 1) {
            if (GenericDungeonB.isAir(world.getBlock(x, cposy + j + 1, z))) {
                FastSetBlock(world, x, cposy + j + 1, z, stonebrick);
                if (m == 0 || m == baseheight * 2 - 2) {
                    FastSetBlock(world, x, cposy + j + 2, z, Blocks.TORCH.defaultBlockState());
                }
            }
        } else if (m % 2 == 1) {
            if (GenericDungeonB.isAir(world.getBlock(x, cposy + j + 1, z))) {
                FastSetBlock(world, x, cposy + j + 1, z, slab);
            }
        }
        while (j >= 0) {
            if (!GenericDungeonB.isAir(world.getBlock(x, cposy + j, z))) {
                break;
            }
            FastSetBlock(world, x, cposy + j, z, stone);
            --j;
        }
    }

    /** {@code makepoolalter} (:3996-4003): 3x3 cobblestone with a water source in the middle. */
    private static void makepoolalter(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        for (int i = -1; i <= 1; ++i) {
            for (int k = -1; k <= 1; ++k) {
                FastSetBlock(world, cposx + i, cposy + 1, cposz + k, Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        FastSetBlock(world, cposx, cposy + 1, cposz, Blocks.WATER.defaultBlockState());
    }

    /** {@code makeincagraves} (:4005-4018): four rows of six graves. */
    private static void makeincagraves(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                       final int cposz, final int width, final int depth) {
        for (int i = 5; i < width - 5; i += 6) {
            makeincagrave(world, rand, cposx + i, cposy, cposz + 5, 1);
        }
        for (int i = 5; i < width - 5; i += 6) {
            makeincagrave(world, rand, cposx + i, cposy, cposz + 10, 1);
        }
        for (int i = 5; i < width - 5; i += 6) {
            makeincagrave(world, rand, cposx + i, cposy, cposz + 20, 3);
        }
        for (int i = 5; i < width - 5; i += 6) {
            makeincagrave(world, rand, cposx + i, cposy, cposz + 25, 3);
        }
    }

    /**
     * {@code makeincagrave} (:4020-4083): grass with poppies and dandelions on both sides, a stone headstone with two
     * slabs, a Ghost spawner on one grave in three, and a chest at the head (dir 1) or foot (dir 3).
     */
    private static void makeincagrave(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                      final int cposz, final int dir) {
        final WeightedRandomChestContent[] chestContents = LootListsB.IncaPyramidContentsList;
        final BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        final BlockState poppy = Blocks.POPPY.defaultBlockState();
        final BlockState dandelion = Blocks.DANDELION.defaultBlockState();
        final BlockState stone = Blocks.STONE.defaultBlockState();
        final BlockState slab = Blocks.SMOOTH_STONE_SLAB.defaultBlockState();
        if (dir == 1) {
            FastSetBlock(world, cposx - 1, cposy, cposz, grass);
            FastSetBlock(world, cposx - 1, cposy + 1, cposz, poppy);
            FastSetBlock(world, cposx - 1, cposy, cposz + 1, grass);
            FastSetBlock(world, cposx - 1, cposy + 1, cposz + 1, dandelion);
            FastSetBlock(world, cposx - 1, cposy, cposz + 2, grass);
            FastSetBlock(world, cposx - 1, cposy + 1, cposz + 2, poppy);
            FastSetBlock(world, cposx + 1, cposy, cposz, grass);
            FastSetBlock(world, cposx + 1, cposy + 1, cposz, poppy);
            FastSetBlock(world, cposx + 1, cposy, cposz + 1, grass);
            FastSetBlock(world, cposx + 1, cposy + 1, cposz + 1, dandelion);
            FastSetBlock(world, cposx + 1, cposy, cposz + 2, grass);
            FastSetBlock(world, cposx + 1, cposy + 1, cposz + 2, poppy);
            FastSetBlock(world, cposx, cposy + 1, cposz, stone);
            FastSetBlock(world, cposx, cposy + 1, cposz + 1, slab);
            FastSetBlock(world, cposx, cposy + 1, cposz + 2, slab);
            if (rand.nextInt(3) == 1) {
                world.setSpawner(cposx, cposy + 2, cposz, "orespawn:ghost");
            }
            world.setChest(cposx, cposy + 1, cposz - 1, 2, chestContents, 10 + rand.nextInt(5), rand);
        }
        if (dir == 3) {
            FastSetBlock(world, cposx - 1, cposy, cposz, grass);
            FastSetBlock(world, cposx - 1, cposy + 1, cposz, poppy);
            FastSetBlock(world, cposx - 1, cposy, cposz - 1, grass);
            FastSetBlock(world, cposx - 1, cposy + 1, cposz - 1, dandelion);
            FastSetBlock(world, cposx - 1, cposy, cposz - 2, grass);
            FastSetBlock(world, cposx - 1, cposy + 1, cposz - 2, poppy);
            FastSetBlock(world, cposx + 1, cposy, cposz, grass);
            FastSetBlock(world, cposx + 1, cposy + 1, cposz, poppy);
            FastSetBlock(world, cposx + 1, cposy, cposz - 1, grass);
            FastSetBlock(world, cposx + 1, cposy + 1, cposz - 1, dandelion);
            FastSetBlock(world, cposx + 1, cposy, cposz - 2, grass);
            FastSetBlock(world, cposx + 1, cposy + 1, cposz - 2, poppy);
            FastSetBlock(world, cposx, cposy + 1, cposz, stone);
            FastSetBlock(world, cposx, cposy + 1, cposz - 1, slab);
            FastSetBlock(world, cposx, cposy + 1, cposz - 2, slab);
            if (rand.nextInt(3) == 1) {
                world.setSpawner(cposx, cposy + 2, cposz, "orespawn:ghost");
            }
            world.setChest(cposx, cposy + 1, cposz + 1, 2, chestContents, 10 + rand.nextInt(5), rand);
        }
    }
}
