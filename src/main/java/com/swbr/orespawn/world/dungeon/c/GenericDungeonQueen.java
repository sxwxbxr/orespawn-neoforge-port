package com.swbr.orespawn.world.dungeon.c;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.world.dungeon.GenericDungeonHelpers;
import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the Queen half of {@code danger.orespawn.GenericDungeon} (W13 split, porter c): {@code makeQueenAltar} with
 * {@code makequeencolumn}, {@code makequeenbackground} and {@code makequeencenteraltar} (:5734-6054), and the Queen
 * challenge castle {@code makeEnormousCastleQ}, {@code buildLevelQ}, {@code addLevelDecorationsQ} and
 * {@code fill_chestsQ} (:6421-7030).
 *
 * <p>Conventions as in {@link GenericDungeonC}. The King versions ({@code makeKingAltar}, {@code makeEnormousCastle},
 * {@code buildLevel}, {@code addLevelDecorations}) belong to the other W13 porters; the Queen castle shares only
 * {@code fill_chests} (:770-832) with them, called through {@link GenericDungeonHelpers}.
 */
public final class GenericDungeonQueen {

    private static final BlockState AIR = GenericDungeonC.AIR;

    private GenericDungeonQueen() {
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeQueenAltar (:5734-5784)
    // ------------------------------------------------------------------------------------------------------------

    /**
     * {@code makeQueenAltar} (:5734-5784): clears 61x59x61, lays a 51x51 lawn with dirt fill below, four columns, an
     * obsidian roof, the ruby picture wall and the centre altar with the Queen egg.
     *
     * <p>The {@code world.isRemote} return (:5739) is dropped: the writer only exists on the server.
     */
    public static void makeQueenAltar(final StructureWriter world, final Random random,
                                      final int cposx, final int cposy, final int cposz) {
        BlockState bid = AIR;
        final int width = 51;
        final int length = 51;
        final int height = 48;
        for (int j = 0; j <= height + 10; ++j) {
            for (int i = -5; i < width + 5; ++i) {
                for (int k = -5; k < length + 5; ++k) {
                    bid = AIR;
                    world.setBlock(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
        int j = 0;
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < length; ++k) {
                bid = Blocks.GRASS_BLOCK.defaultBlockState();
                world.setBlock(cposx + i, cposy + j, cposz + k, bid);
                for (int v = 1; v < 10; ++v) {
                    bid = world.getBlock(cposx + i, cposy + j - v, cposz + k);
                    if (isAirTallgrassOrWater(bid)) {
                        bid = Blocks.DIRT.defaultBlockState();
                        world.setBlock(cposx + i, cposy + j - v, cposz + k, bid);
                    }
                }
            }
        }
        makequeencolumn(world, cposx + 1, cposy + 1, cposz + 1);
        makequeencolumn(world, cposx + width - 8, cposy + 1, cposz + length - 8);
        makequeencolumn(world, cposx + 1, cposy + 1, cposz + length - 8);
        makequeencolumn(world, cposx + width - 8, cposy + 1, cposz + 1);
        j = height - 1;
        bid = Blocks.OBSIDIAN.defaultBlockState();
        for (int i = 0; i < width; ++i) {
            for (int k = 0; k < length; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, bid);
            }
        }
        j = height;
        for (int i = -1; i <= width; ++i) {
            for (int k = -1; k <= length; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, bid);
            }
        }
        makequeenbackground(world, cposx + 4, cposy + 10, cposz + 9);
        makequeencenteraltar(world, random, cposx + width / 2, cposy, cposz + length / 2);
    }

    /**
     * {@code bid == Blocks.air || bid == Blocks.tallgrass || bid == Blocks.water} (:5756).
     *
     * <p>PORT: {@code Blocks.tallgrass} was one block whose metadata 0/1/2 the 1.13 flattening split into
     * {@code dead_bush}, {@code short_grass} and {@code fern}; all three stand for it. Air is any air block (1.21.1 adds
     * cave and void air). {@code Blocks.water} was the still block only; 1.21.1 has one water block for still and
     * flowing water, so flowing water counts as well (DECISIONS R22, the category of the original).
     */
    private static boolean isAirTallgrassOrWater(final BlockState state) {
        return state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.DEAD_BUSH)
                || state.is(Blocks.WATER);
    }

    /** {@code makequeencolumn} (:5786-5848): 7x7 obsidian slabs at both ends, a 5x5 hollow shaft 44 high with a pattern. */
    private static void makequeencolumn(final StructureWriter world, int cposx, int cposy, int cposz) {
        BlockState bid = AIR;
        final int width = 5;
        final int length = 5;
        final int height = 44;
        // world.isRemote return (:5792) dropped, see makeQueenAltar
        int j = 0;
        bid = Blocks.OBSIDIAN.defaultBlockState();
        for (int i = 0; i < width + 2; ++i) {
            for (int k = 0; k < length + 2; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, bid);
                world.setBlock(cposx + i, cposy + j + height + 1, cposz + k, bid);
            }
        }
        ++cposx;
        ++cposz;
        ++cposy;
        final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        final BlockState redstone = Blocks.REDSTONE_BLOCK.defaultBlockState();
        final BlockState amethyst = ModBlocks.BLOCKAMETHYST.get().defaultBlockState();
        for (j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                for (int k = 0; k < length; ++k) {
                    bid = AIR;
                    if (i == 0 || k == 0 || i == width - 1 || k == length - 1) {
                        bid = obsidian;
                    }
                    if (j % 4 == 0 && bid != AIR && (i == 2 || k == 2)) {
                        bid = redstone;
                    }
                    if (j % 4 == 1 && bid != AIR) {
                        if (i == 1 || k == 1) {
                            bid = redstone;
                        }
                        if (i == 3 || k == 3) {
                            bid = redstone;
                        }
                    }
                    if (j % 4 == 2 && bid != AIR) {
                        if (i == 1 || k == 1) {
                            bid = redstone;
                        }
                        if (i == 3 || k == 3) {
                            bid = redstone;
                        }
                        if (i == 2 || k == 2) {
                            bid = amethyst;
                        }
                    }
                    if (j % 4 == 3 && bid != AIR) {
                        if (i == 1 || k == 1) {
                            bid = redstone;
                        }
                        if (i == 3 || k == 3) {
                            bid = redstone;
                        }
                    }
                    world.setBlock(cposx + i, cposy + j, cposz + k, bid);
                }
            }
        }
    }

    /**
     * {@code makequeenbackground} (:5850-5902): the run-length picture {@code queen} in the plane x = cposx, stone and
     * ruby block alternating per run, a -1 fills the row with stone; diamond frame, crystal torches on the corners.
     */
    private static void makequeenbackground(final StructureWriter world, final int cposx, final int cposy, final int cposz) {
        final BlockState stone = Blocks.STONE.defaultBlockState();
        final BlockState ruby = ModBlocks.BLOCKRUBY.get().defaultBlockState();
        final BlockState diamond = Blocks.DIAMOND_BLOCK.defaultBlockState();
        int curz = 0;
        int cury = 0;
        final int height = 33;
        final int width = 33;
        BlockState bid = stone;
        final int[] queen = GenericDungeonHelpers.queen;
        for (int m = 0; m < queen.length; ++m) {
            final int v = queen[m];
            if (v < 0) {
                bid = stone;
                while (curz < width) {
                    world.setBlock(cposx, cposy + cury, cposz + curz, bid);
                    ++curz;
                }
                ++cury;
                curz = 0;
            } else {
                for (int n = 0; n < v; ++n) {
                    world.setBlock(cposx, cposy + cury, cposz + curz, bid);
                    ++curz;
                }
                if (bid == stone) {
                    bid = ruby;
                } else {
                    bid = stone;
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            world.setBlock(cposx, cposy - 1, cposz + i, diamond);
        }
        for (int i = 0; i < width; ++i) {
            world.setBlock(cposx, cposy + height, cposz + i, diamond);
        }
        for (int i = -1; i <= height; ++i) {
            world.setBlock(cposx, cposy + i, cposz - 1, diamond);
        }
        for (int i = -1; i <= height; ++i) {
            world.setBlock(cposx, cposy + i, cposz + width, diamond);
        }
        world.setBlock(cposx, cposy - 2, cposz - 2, diamond);
        world.setBlock(cposx, cposy + height + 1, cposz + width + 1, diamond);
        world.setBlock(cposx, cposy - 2, cposz + width + 1, diamond);
        world.setBlock(cposx, cposy + height + 1, cposz - 2, diamond);
        world.setBlock(cposx, cposy - 1, cposz - 2, GenericDungeonC.crystalTorch());
        world.setBlock(cposx, cposy + height + 2, cposz + width + 1, GenericDungeonC.crystalTorch());
        world.setBlock(cposx, cposy - 1, cposz + width + 1, GenericDungeonC.crystalTorch());
        world.setBlock(cposx, cposy + height + 2, cposz - 2, GenericDungeonC.crystalTorch());
    }

    /** {@code makequeencenteraltar} (:5904-6054): stepped obsidian cross, amethyst ends, torches, the Queen egg chest. */
    private static void makequeencenteraltar(final StructureWriter world, final Random random,
                                             final int cposx, final int cposy, final int cposz) {
        final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        final BlockState amethyst = ModBlocks.BLOCKAMETHYST.get().defaultBlockState();
        // y 0 (:5909-5932)
        slab(world, cposx, cposy, cposz, 10, 10, obsidian);
        slab(world, cposx, cposy, cposz, 6, 20, obsidian);
        slab(world, cposx, cposy, cposz, 20, 6, obsidian);
        // y 1 (:5933-5966)
        slab(world, cposx, cposy + 1, cposz, 8, 8, obsidian);
        endedSlab(world, cposx, cposy + 1, cposz, 4, 18, obsidian, amethyst);
        endedSlab(world, cposx, cposy + 1, cposz, 18, 4, obsidian, amethyst);
        // y 2 (:5967-5997)
        torchSlab(world, cposx, cposy + 2, cposz, 7, 7, obsidian);
        slab(world, cposx, cposy + 2, cposz, 3, 17, obsidian);
        slab(world, cposx, cposy + 2, cposz, 17, 3, obsidian);
        // y 3 (:5998-6021)
        slab(world, cposx, cposy + 3, cposz, 6, 6, obsidian);
        slab(world, cposx, cposy + 3, cposz, 2, 16, obsidian);
        slab(world, cposx, cposy + 3, cposz, 16, 2, obsidian);
        // y 4 (:6022-6047)
        final int j = 4;
        torchSlab(world, cposx, cposy + j, cposz, 2, 2, obsidian);
        // world.setBlock(chest) + setBlockMetadataWithNotify(2, 3), slot 13 = TheQueenEgg (:6048-6053)
        world.setBlock(cposx, cposy + j, cposz, LegacyMeta.chest(2), Block.UPDATE_ALL);
        GenericDungeonHelpers.setChestSlot(world, cposx, cposy + j, cposz, 13, GenericDungeonHelpers.stack("orespawn:eggthequeen", 1));
    }

    /**
     * The plain {@code for i in -width..width, k in -length..length} slab of the centre altar. PORT: the original
     * spells each of these out with its own {@code width}/{@code length} assignment; extracted, order unchanged.
     */
    private static void slab(final StructureWriter world, final int cposx, final int y, final int cposz,
                             final int width, final int length, final BlockState bid) {
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                world.setBlock(cposx + i, y, cposz + k, bid);
            }
        }
    }

    /** A slab whose four corners are {@code end} (:5941-5966). */
    private static void endedSlab(final StructureWriter world, final int cposx, final int y, final int cposz,
                                  final int width, final int length, final BlockState bid, final BlockState end) {
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                BlockState b = bid;
                if (i == width && (k == -length || k == length)) {
                    b = end;
                }
                if (i == -width && (k == -length || k == length)) {
                    b = end;
                }
                world.setBlock(cposx + i, y, cposz + k, b);
            }
        }
    }

    /** A slab with a crystal torch above each corner, written right after the corner block (:5970-5980, :6025-6035). */
    private static void torchSlab(final StructureWriter world, final int cposx, final int y, final int cposz,
                                  final int width, final int length, final BlockState bid) {
        for (int i = -width; i <= width; ++i) {
            for (int k = -length; k <= length; ++k) {
                world.setBlock(cposx + i, y, cposz + k, bid);
                if (i == width && (k == -length || k == length)) {
                    world.setBlock(cposx + i, y + 1, cposz + k, GenericDungeonC.crystalTorch());
                }
                if (i == -width && (k == -length || k == length)) {
                    world.setBlock(cposx + i, y + 1, cposz + k, GenericDungeonC.crystalTorch());
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // makeEnormousCastleQ (:6421-6615)
    // ------------------------------------------------------------------------------------------------------------

    /**
     * {@code makeEnormousCastleQ} (:6421-6615): the Queen challenge castle - obsidian ground floor, up to six levels by
     * {@code level = 1 + nextInt(6)} (+3 with 2/3 when at most 3), amethyst platform, bridge and stairs, and at level 6
     * a hundred Large Worm spawner tries under the ground ring.
     *
     * <p>The {@code world.isRemote} return (:6426) is dropped: the writer only exists on the server.
     */
    public static void makeEnormousCastleQ(final StructureWriter world, final Random random,
                                           final int cposx, final int cposy, final int cposz) {
        final int width = 28;
        final int height = 16;
        final int platformwidth = 11;
        int level = 0;
        level = 1 + random.nextInt(6);
        if (level <= 3 && random.nextInt(3) != 1) {
            level += 3;
        }
        final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        final BlockState bars = Blocks.IRON_BARS.defaultBlockState();
        final BlockState fence = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
        final BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        final BlockState fire = Blocks.FIRE.defaultBlockState();
        final BlockState amethyst = ModBlocks.BLOCKAMETHYST.get().defaultBlockState();
        for (int i = -20; i < width + 4; ++i) {
            for (int j = 1; j < height + 10; ++j) {
                for (int k = -4; k < width + 4; ++k) {
                    world.setBlock(cposx + i, cposy + j, cposz + k, AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = 0;
            for (int k = 0; k < width; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, obsidian);
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height;
            for (int k = 0; k < width; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, bedrock);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 1; j < height; ++j) {
                int k = 0;
                world.setBlock(cposx + i, cposy + j, cposz + k, bars);
                k = width - 1;
                world.setBlock(cposx + i, cposy + j, cposz + k, bars);
            }
        }
        for (int k = 0; k < width; ++k) {
            for (int j = 1; j < height; ++j) {
                int i = 0;
                world.setBlock(cposx + i, cposy + j, cposz + k, bars);
                i = width - 1;
                world.setBlock(cposx + i, cposy + j, cposz + k, bars);
            }
        }
        // world.setBlock(x, y, z, ExtremeTorch): flag 3
        final BlockState torch = ModBlocks.EXTREME_TORCH.get().defaultBlockState();
        world.setBlock(cposx + 1, cposy + 1, cposz + 1, torch, Block.UPDATE_ALL);
        world.setBlock(cposx + 1, cposy + 1, cposz + width - 2, torch, Block.UPDATE_ALL);
        world.setBlock(cposx + width - 2, cposy + 1, cposz + 1, torch, Block.UPDATE_ALL);
        world.setBlock(cposx + width - 2, cposy + 1, cposz + width - 2, torch, Block.UPDATE_ALL);
        for (int i = -4; i < width + 4; ++i) {
            for (int k = -4; k < width + 4; ++k) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    world.setBlock(cposx + i, cposy, cposz + k, obsidian);
                }
                if (i == -4 || k == -4 || i == width + 3 || k == width + 3) {
                    world.setBlock(cposx + i, cposy + 1, cposz + k, fence);
                }
            }
        }
        for (int j = 0; j < 4; ++j) {
            world.setSpawner(cposx - 3, cposy + 1 + j, cposz - 3, "orespawn:lurking_terror");
            world.setSpawner(cposx - 3, cposy + 1 + j, cposz + width + 2, "orespawn:lurking_terror");
            world.setSpawner(cposx + width + 2, cposy + 1 + j, cposz - 3, "orespawn:lurking_terror");
            world.setSpawner(cposx + width + 2, cposy + 1 + j, cposz + width + 2, "orespawn:lurking_terror");
        }
        world.setSpawner(cposx + width / 2, cposy + 2, cposz + width / 2, "orespawn:emperor_scorpion");
        world.setSpawner(cposx + width / 2, cposy + 3, cposz + width / 2, "orespawn:emperor_scorpion");
        world.setSpawner(cposx + width / 2, cposy + 4, cposz + width / 2, "orespawn:emperor_scorpion");
        int j = height;
        buildLevelQ(world, random, cposx + 1, cposy + j, cposz + 1, width - 2, 10, 4, GenericDungeonC.ROTATOR, 1, -1, 5, 1, level);
        j += 10;
        if (level >= 2) {
            buildLevelQ(world, random, cposx + 1, cposy + j, cposz + 1, width - 2, 10, 4, "orespawn:bee", 0, 0, 4, 2, level);
        }
        j += 10;
        if (level >= 3) {
            buildLevelQ(world, random, cposx + 2, cposy + j, cposz + 2, width - 4, 9, 4, "orespawn:mantis", 1, 1, 4, 3, level);
        }
        j += 9;
        if (level >= 4) {
            buildLevelQ(world, random, cposx + 2, cposy + j, cposz + 2, width - 4, 9, 3, "orespawn:mothra", 0, 0, 4, 4, level);
        }
        j += 9;
        if (level >= 5) {
            buildLevelQ(world, random, cposx + 3, cposy + j, cposz + 3, width - 6, 8, 3, "orespawn:brutalfly", 1, 1, 4, 5, level);
        }
        j += 8;
        if (level >= 6) {
            buildLevelQ(world, random, cposx + 3, cposy + j, cposz + 3, width - 6, 16, 3, "orespawn:vortex", 0, 0, 3, 6, level);
        }
        j += 16;
        for (int i = 0; i < platformwidth; ++i) {
            j = height;
            for (int k = -(platformwidth / 2); k <= platformwidth / 2; ++k) {
                world.setBlock(cposx + i - 20, cposy + j, cposz + k + width / 2, amethyst);
                if ((i == 0 || i == platformwidth - 1 || k == -(platformwidth / 2) || k == platformwidth / 2)
                        && (i != 0 || k < -1 || k > 1)) {
                    world.setBlock(cposx + i - 20, cposy + j + 1, cposz + k + width / 2, fence);
                }
            }
        }
        for (int i = -10; i <= -3; ++i) {
            j = height;
            for (int k = -2; k < 3; ++k) {
                if (i == -3 || i == -10) {
                    if (k != -2 && k != 2) {
                        world.setBlock(cposx + i, cposy + j + 1, cposz + k + width / 2, AIR);
                    } else {
                        world.setBlock(cposx + i, cposy + j + 1, cposz + k + width / 2, netherrack);
                        world.setBlock(cposx + i, cposy + j + 2, cposz + k + width / 2, netherrack);
                        world.setBlock(cposx + i, cposy + j + 3, cposz + k + width / 2, fire);
                    }
                } else {
                    world.setBlock(cposx + i, cposy + j, cposz + k + width / 2, amethyst);
                    if (k == -2 || k == 2) {
                        world.setBlock(cposx + i, cposy + j + 1, cposz + k + width / 2, fence);
                    }
                }
            }
        }
        int i = -21;
        for (j = height; j >= 0; --j) {
            for (int k = -2; k < 3; ++k) {
                for (int t = 0; t < 6; ++t) {
                    world.setBlock(cposx + i, cposy + j + t + 1, cposz + k + width / 2, AIR);
                }
                if (j == 0) {
                    if (k != -2 && k != 2) {
                        world.setBlock(cposx + i, cposy + j + 1, cposz + k + width / 2, AIR);
                    } else {
                        world.setBlock(cposx + i, cposy + j + 1, cposz + k + width / 2, netherrack);
                        world.setBlock(cposx + i, cposy + j + 2, cposz + k + width / 2, netherrack);
                        world.setBlock(cposx + i, cposy + j + 3, cposz + k + width / 2, fire);
                    }
                } else {
                    world.setBlock(cposx + i, cposy + j, cposz + k + width / 2, amethyst);
                    if (k == -2 || k == 2) {
                        world.setBlock(cposx + i, cposy + j + 1, cposz + k + width / 2, fence);
                    }
                }
            }
            --i;
        }
        if (level >= 6) {
            final int span = width * 3;
            for (int tries = 0; tries < 100; ++tries) {
                j = -1;
                i = random.nextInt(span);
                int k = random.nextInt(span);
                if (i < span / 4 || i > span * 3 / 4 || k < span / 4 || k > span * 3 / 4) {
                    i -= span / 2;
                    k -= span / 2;
                    world.setSpawner(cposx + i + width / 2, cposy + j, cposz + k + width / 2, "orespawn:large_worm");
                }
            }
        }
    }

    /**
     * {@code buildLevelQ} (:6617-6720): one castle level - bedrock box with ruby corner columns, obsidian ring with a
     * nether brick fence, an outside stair, an optional hole, 16 corner spawners of {@code critter}, then the
     * decorations.
     *
     * @param critter registry id of the corner spawners (the original passed the legacy name)
     */
    public static void buildLevelQ(final StructureWriter world, final Random random, final int cposx, final int cposy,
                                   final int cposz, final int width, final int height, final int pw,
                                   final String critter, final int stepside, final int stepoff, final int holelen,
                                   final int decor, final int level) {
        final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        final BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        for (int i = -pw; i < width + pw; ++i) {
            for (int j = 1; j < height; ++j) {
                for (int k = -pw; k < width + pw; ++k) {
                    world.setBlock(cposx + i, cposy + j, cposz + k, AIR);
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = 0;
            for (int k = 0; k < width; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, bedrock);
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height;
            for (int k = 0; k < width; ++k) {
                world.setBlock(cposx + i, cposy + j, cposz + k, bedrock);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 1; j < height; ++j) {
                int k = 0;
                world.setBlock(cposx + i, cposy + j, cposz + k, bedrock);
                k = width - 1;
                world.setBlock(cposx + i, cposy + j, cposz + k, bedrock);
            }
        }
        for (int k = 0; k < width; ++k) {
            for (int j = 1; j < height; ++j) {
                BlockState blk = bedrock;
                if (k == 0 || k == width - 1) {
                    blk = ModBlocks.BLOCKRUBY.get().defaultBlockState();
                }
                int i = 0;
                world.setBlock(cposx + i, cposy + j, cposz + k, blk);
                i = width - 1;
                world.setBlock(cposx + i, cposy + j, cposz + k, blk);
            }
        }
        for (int i = -pw; i < width + pw; ++i) {
            for (int k = -pw; k < width + pw; ++k) {
                if (i < 0 || k < 0 || i >= width || k >= width) {
                    world.setBlock(cposx + i, cposy, cposz + k, obsidian);
                }
                if (i == -pw || k == -pw || i == width + (pw - 1) || k == width + (pw - 1)) {
                    world.setBlock(cposx + i, cposy + 1, cposz + k, Blocks.NETHER_BRICK_FENCE.defaultBlockState());
                }
            }
        }
        int i = -(height / 2);
        i += width / 2;
        for (int j = 1; j < height; ++j) {
            if (stepside != 0) {
                final int k = -1;
                world.setBlock(cposx + i, cposy + j, cposz + k, obsidian);
            } else {
                final int k = width;
                world.setBlock(cposx + i, cposy + j, cposz + k, obsidian);
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
                world.setBlock(cposx + i + l, cposy + j, cposz + k, AIR);
            }
        }
        for (int j = 0; j < 4; ++j) {
            world.setSpawner(cposx - (pw - 1), cposy + j + 1, cposz - (pw - 1), critter);
            world.setSpawner(cposx - (pw - 1), cposy + j + 1, cposz + width + (pw - 2), critter);
            world.setSpawner(cposx + width + (pw - 2), cposy + j + 1, cposz - (pw - 1), critter);
            world.setSpawner(cposx + width + (pw - 2), cposy + j + 1, cposz + width + (pw - 2), critter);
        }
        addLevelDecorationsQ(world, random, cposx, cposy, cposz, width, height, decor, level);
    }

    /**
     * {@code addLevelDecorationsQ} (:6722-6966). The spawner mob and the reward tier depend on {@code difficulty - decor}:
     * 0 T. Rex (tier 1), 1 Nastysaurus (2), 2 Basilisk (3), 3 Hercules Beetle (4), 4 Jumpy Bug (5), 5 CaterKiller
     * (decor 1 only, tier 6). Decor 6 is the worm floor with Nightmare spawners on the roof; decor 1 adds the random
     * teleport blocks and uses {@link #fill_chestsQ}, every other decor the shared King {@code fill_chests}.
     *
     * <p>Catalogue 6.5 listed the Nastysaurus branch (:6789-6928) as unread; it is read here: {@code "Nastysaurus"} is
     * the critter at {@code difficulty - decor == 1} for decor 1 to 5, exactly as the table above. Where the King
     * castle has T. Rex, the Queen castle has Nastysaurus, one tier harder at every level.
     */
    public static void addLevelDecorationsQ(final StructureWriter world, final Random random, final int cposx,
                                            final int cposy, final int cposz, final int width, final int height,
                                            final int decor, final int difficulty) {
        int reward = 1;
        String critter = T_REX;
        final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        if (decor == 6) {
            final BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
            final BlockState fire = Blocks.FIRE.defaultBlockState();
            world.setBlock(cposx, cposy + height, cposz, netherrack);
            world.setBlock(cposx, cposy + height + 1, cposz, fire);
            world.setBlock(cposx, cposy + height, cposz + width - 1, netherrack);
            world.setBlock(cposx, cposy + height + 1, cposz + width - 1, fire);
            world.setBlock(cposx + width - 1, cposy + height, cposz, netherrack);
            world.setBlock(cposx + width - 1, cposy + height + 1, cposz, fire);
            world.setBlock(cposx + width - 1, cposy + height, cposz + width - 1, netherrack);
            world.setBlock(cposx + width - 1, cposy + height + 1, cposz + width - 1, fire);
            world.setBlock(cposx + width / 2, cposy + height, cposz + width / 2, AIR);
            world.setSpawner(cposx + width / 2 - 1, cposy + height + 2, cposz + width / 2, NIGHTMARE);
            world.setSpawner(cposx + width / 2 + 1, cposy + height + 2, cposz + width / 2, NIGHTMARE);
            world.setSpawner(cposx + width / 2, cposy + height + 2, cposz + width / 2 - 1, NIGHTMARE);
            world.setSpawner(cposx + width / 2, cposy + height + 2, cposz + width / 2 + 1, NIGHTMARE);
            final BlockState dirt = Blocks.DIRT.defaultBlockState();
            for (int i = 1; i < width - 1; ++i) {
                for (int j = 1; j < 5; ++j) {
                    for (int k = 1; k < width - 1; ++k) {
                        world.setBlock(cposx + i, cposy + j, cposz + k, dirt);
                    }
                }
            }
            world.setSpawner(cposx + width / 2, cposy + 2, cposz + width / 2, "orespawn:large_worm");
            world.setSpawner(cposx + width / 2, cposy + 3, cposz + width / 2, "orespawn:large_worm");
            world.setSpawner(cposx + width / 2, cposy + 4, cposz + width / 2, "orespawn:large_worm");
            for (int j = 0; j < 10; ++j) {
                world.setBlock(cposx + 1, cposy + j, cposz + 1, AIR);
            }
            GenericDungeonHelpers.fill_chests(world, random, cposx, cposy + 4, cposz, width, height, decor, reward);
        }
        if (decor == 5) {
            if (difficulty == 5) {
                critter = T_REX;
                reward = 1;
            }
            if (difficulty == 6) {
                critter = NASTYSAURUS;
                reward = 2;
            }
            spawnerCross(world, cposx, cposy, cposz, width, critter, bedrock);
            world.setBlock(cposx + width - 2, cposy, cposz + width - 2, AIR);
            world.setBlock(cposx + 1, cposy + height, cposz + 1, AIR);
            GenericDungeonHelpers.fill_chests(world, random, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 4) {
            if (difficulty == 4) {
                critter = T_REX;
                reward = 1;
            }
            if (difficulty == 5) {
                critter = NASTYSAURUS;
                reward = 2;
            }
            if (difficulty == 6) {
                critter = BASILISK;
                reward = 3;
            }
            spawnerCross(world, cposx, cposy, cposz, width, critter, bedrock);
            world.setBlock(cposx + 1, cposy, cposz + 1, AIR);
            world.setBlock(cposx + width - 2, cposy + height, cposz + width - 2, AIR);
            GenericDungeonHelpers.fill_chests(world, random, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 3) {
            if (difficulty == 3) {
                critter = T_REX;
                reward = 1;
            }
            if (difficulty == 4) {
                critter = NASTYSAURUS;
                reward = 2;
            }
            if (difficulty == 5) {
                critter = BASILISK;
                reward = 3;
            }
            if (difficulty == 6) {
                critter = HERCULES_BEETLE;
                reward = 4;
            }
            spawnerCross(world, cposx, cposy, cposz, width, critter, bedrock);
            world.setBlock(cposx + width - 2, cposy, cposz + width - 2, AIR);
            world.setBlock(cposx + 1, cposy + height, cposz + 1, AIR);
            GenericDungeonHelpers.fill_chests(world, random, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 2) {
            if (difficulty == 2) {
                critter = T_REX;
                reward = 1;
            }
            if (difficulty == 3) {
                critter = NASTYSAURUS;
                reward = 2;
            }
            if (difficulty == 4) {
                critter = BASILISK;
                reward = 3;
            }
            if (difficulty == 5) {
                critter = HERCULES_BEETLE;
                reward = 4;
            }
            if (difficulty == 6) {
                critter = JUMPY_BUG;
                reward = 5;
            }
            spawnerCross(world, cposx, cposy, cposz, width, critter, bedrock);
            world.setBlock(cposx + 1, cposy, cposz + 1, AIR);
            world.setBlock(cposx + width - 2, cposy + height, cposz + width - 2, AIR);
            GenericDungeonHelpers.fill_chests(world, random, cposx, cposy, cposz, width, height, decor, reward);
        }
        if (decor == 1) {
            if (difficulty == 1) {
                critter = T_REX;
            }
            if (difficulty == 2) {
                critter = NASTYSAURUS;
            }
            if (difficulty == 3) {
                critter = BASILISK;
            }
            if (difficulty == 4) {
                critter = HERCULES_BEETLE;
            }
            if (difficulty == 5) {
                critter = JUMPY_BUG;
            }
            if (difficulty == 6) {
                critter = "orespawn:cater_killer";
            }
            reward = difficulty;
            spawnerCross(world, cposx, cposy, cposz, width, critter, bedrock);
            final BlockState rtp = ModBlocks.BLOCKTELEPORT.get().defaultBlockState();
            world.setBlock(cposx + width / 2 - 1, cposy + 1, cposz + width / 2 - 1, rtp);
            world.setBlock(cposx + width / 2 + 1, cposy + 1, cposz + width / 2 + 1, rtp);
            world.setBlock(cposx + width / 2 + 1, cposy + 1, cposz + width / 2 - 1, rtp);
            world.setBlock(cposx + width / 2 - 1, cposy + 1, cposz + width / 2 + 1, rtp);
            world.setBlock(cposx + 1, cposy + height, cposz + 1, AIR);
            fill_chestsQ(world, random, cposx, cposy, cposz, width, height, decor, reward);
        }
    }

    /**
     * The two centre spawners and the bedrock cross y 1..4 around them, spelled out identically in every decor branch
     * 1-5 of :6722-6966. PORT: extracted; spawner, spawner, then the cross, as in each branch.
     */
    private static void spawnerCross(final StructureWriter world, final int cposx, final int cposy, final int cposz,
                                     final int width, final String critter, final BlockState bedrock) {
        world.setSpawner(cposx + width / 2, cposy + 2, cposz + width / 2, critter);
        world.setSpawner(cposx + width / 2, cposy + 3, cposz + width / 2, critter);
        for (int j = 1; j < 5; ++j) {
            world.setBlock(cposx + width / 2 - 1, cposy + j, cposz + width / 2, bedrock);
            world.setBlock(cposx + width / 2 + 1, cposy + j, cposz + width / 2, bedrock);
            world.setBlock(cposx + width / 2, cposy + j, cposz + width / 2 - 1, bedrock);
            world.setBlock(cposx + width / 2, cposy + j, cposz + width / 2 + 1, bedrock);
        }
    }

    /**
     * {@code fill_chestsQ} (:6968-7030): the four chests of a decor-1 level, facing inwards; tier 6 places the fixed
     * Queen reward (Princess egg, Queen armour, Royal Bertha) in slots 1-2 instead of rolling loot.
     */
    private static void fill_chestsQ(final StructureWriter world, final Random random, final int cposx, final int cposy,
                                     final int cposz, final int width, final int height, final int decor,
                                     final int reward) {
        WeightedRandomChestContent[] chestContents = GenericDungeonHelpers.level1ContentsList;
        if (reward == 2) {
            chestContents = GenericDungeonHelpers.level2ContentsList;
        }
        if (reward == 3) {
            chestContents = GenericDungeonHelpers.level3ContentsList;
        }
        if (reward == 4) {
            chestContents = GenericDungeonHelpers.level4ContentsList;
        }
        if (reward == 5) {
            chestContents = GenericDungeonHelpers.level5ContentsList;
        }
        int x = cposx + 1;
        int z = cposz + width / 2;
        world.setBlock(x, cposy + 1, z, LegacyMeta.chest(5), Block.UPDATE_CLIENTS);
        if (reward == 6) {
            GenericDungeonHelpers.setChestSlot(world, x, cposy + 1, z, 1, GenericDungeonHelpers.stack("orespawn:eggtheprincess", 1));
        } else {
            world.fillChest(x, cposy + 1, z, chestContents, 5 + random.nextInt(7), random);
        }
        x = cposx + width - 2;
        z = cposz + width / 2;
        world.setBlock(x, cposy + 1, z, LegacyMeta.chest(4), Block.UPDATE_CLIENTS);
        if (reward == 6) {
            GenericDungeonHelpers.setChestSlot(world, x, cposy + 1, z, 1, new ItemStack(ModItems.QUEEN_HELMET.get()));
            GenericDungeonHelpers.setChestSlot(world, x, cposy + 1, z, 2, new ItemStack(ModItems.QUEEN_CHEST.get()));
        } else {
            world.fillChest(x, cposy + 1, z, chestContents, 5 + random.nextInt(7), random);
        }
        x = cposx + width / 2;
        z = cposz + 1;
        world.setBlock(x, cposy + 1, z, LegacyMeta.chest(3), Block.UPDATE_CLIENTS);
        if (reward == 6) {
            GenericDungeonHelpers.setChestSlot(world, x, cposy + 1, z, 1, new ItemStack(ModItems.QUEEN_LEGGINGS.get()));
            GenericDungeonHelpers.setChestSlot(world, x, cposy + 1, z, 2, new ItemStack(ModItems.QUEEN_BOOTS.get()));
        } else {
            world.fillChest(x, cposy + 1, z, chestContents, 5 + random.nextInt(7), random);
        }
        x = cposx + width / 2;
        z = cposz + width - 2;
        world.setBlock(x, cposy + 1, z, LegacyMeta.chest(2), Block.UPDATE_CLIENTS);
        if (reward == 6) {
            GenericDungeonHelpers.setChestSlot(world, x, cposy + 1, z, 1, new ItemStack(ModItems.ROYAL.get()));
        } else {
            world.fillChest(x, cposy + 1, z, chestContents, 5 + random.nextInt(7), random);
        }
    }

    private static final String T_REX = "orespawn:t_rex";
    private static final String NASTYSAURUS = "orespawn:nastysaurus";
    private static final String BASILISK = "orespawn:basilisk";
    private static final String HERCULES_BEETLE = "orespawn:hercules_beetle";
    private static final String JUMPY_BUG = "orespawn:jumpy_bug";
    private static final String NIGHTMARE = "orespawn:nightmare";
}
