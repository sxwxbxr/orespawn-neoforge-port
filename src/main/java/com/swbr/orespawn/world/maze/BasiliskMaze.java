package com.swbr.orespawn.world.maze;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import com.swbr.orespawn.world.tree.LegacyRandom;
import com.swbr.orespawn.world.tree.LegacyWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BasiliskMaze} (BasiliskMaze.java:14-473), the singleton {@code OreSpawnMain.BMaze}
 * (OreSpawnMain.java:5442): a sandstone pyramid over a spiral shaft, an antechamber, a 30x30 obsidian maze and a
 * hall with lava, teleport traps, 2-4 chests and three persistent Basilisks (verhalten/world-04.md, "BasiliskMaze").
 * Up to 73x42x34 blocks: world generation places it through a {@code LegacyStructurePiece} ({@link com.swbr.orespawn.world.tree.TreeBuilders}); the Random Dungeon
 * Spawner builds it in place.
 *
 * <p>PORT, randomness (DECISIONS R18): the maze layout came from {@code Math.random()} (:249-251). The port takes it
 * from a {@link Random} argument ({@code mathRandom}), seeded from the structure seed in world generation, so that
 * every chunk of the structure sees the same layout. {@code (int) (Math.random() * n + 1.0)} is kept as
 * {@code (int) (mathRandom.nextDouble() * n + 1.0)}. {@code java.awt.Point} and {@code java.util.Vector} become a
 * record and an {@link ArrayList} with the same {@code indexOf}/{@code remove(index)}/{@code add} order.
 */
public final class BasiliskMaze {

    public static final int WTOP = 1;
    public static final int WRGT = 2;
    public static final int WBOT = 4;
    public static final int WLFT = 8;

    /** {@code EntityList} name "Basilisk" (:409). */
    public static final String BASILISK = "orespawn:basilisk";

    /** {@code chestContentsList} (BasiliskMaze.java:23): 31 entries, weight sum 495. */
    public static final WeightedRandomChestContent[] chestContentsList = {
        WeightedRandomChestContent.byId("minecraft:ender_pearl", 3, 6, 15),
        WeightedRandomChestContent.byId("minecraft:diamond", 15, 25, 20),
        WeightedRandomChestContent.byId("minecraft:blaze_rod", 4, 12, 15),
        WeightedRandomChestContent.byId("orespawn:cageempty", 3, 10, 20),
        WeightedRandomChestContent.byId("orespawn:cagegirlfriend", 2, 4, 15),
        WeightedRandomChestContent.byId("minecraft:iron_ingot", 2, 20, 20),
        WeightedRandomChestContent.byId("minecraft:gold_ingot", 4, 16, 20),
        WeightedRandomChestContent.byId("orespawn:ingoturanium", 2, 8, 20),
        WeightedRandomChestContent.byId("orespawn:ingottitanium", 2, 6, 20),
        WeightedRandomChestContent.byId("orespawn:sunfish", 2, 8, 20),
        WeightedRandomChestContent.byId("orespawn:firefish", 3, 8, 20),
        WeightedRandomChestContent.byId("orespawn:lavaeel", 5, 24, 20),
        WeightedRandomChestContent.byId("orespawn:corndog_cooked", 6, 12, 20),
        WeightedRandomChestContent.byId("minecraft:diamond_pickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:diamond_sword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimatepickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimatesword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimatefishingrod", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimatebow", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:diamond_chestplate", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:diamond_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:diamond_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:diamond_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimate_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimate_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimate_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimate_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:thunderstaff", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:magicapple", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:golden_apple", 2, 4, 15),
    };

    /** {@code java.awt.Point}: equality by both coordinates, which {@code Vector.indexOf} relied on. */
    private record Point(int x, int y) {
    }

    private BasiliskMaze() {
    }

    private static BlockState obsidian() {
        return Blocks.OBSIDIAN.defaultBlockState();
    }

    private static BlockState bedrock() {
        return Blocks.BEDROCK.defaultBlockState();
    }

    private static BlockState air() {
        return Blocks.AIR.defaultBlockState();
    }

    private static BlockState sandstone() {
        return Blocks.SANDSTONE.defaultBlockState();
    }

    /** {@code buildBasiliskMaze} with the live level, for {@code DungeonSpawnerBlock}. */
    public static void buildBasiliskMaze(final Level world, final int x, final int y, final int z) {
        // PORT: Math.random() had its own global stream; at runtime the level's random stands in for both.
        buildBasiliskMaze(LegacyWriter.live(world), new LegacyRandom(world.random), new LegacyRandom(world.random), x, y, z);
    }

    /**
     * {@code buildBasiliskMaze(world, x, y, z)} (BasiliskMaze.java:26-33): {@code depth} 20-29, hall origin
     * O = (x+3, y-depth-4, z-20); clear, maze, openings, castle, entrance - in that order.
     *
     * @param rand       {@code world.rand}
     * @param mathRandom stands in for {@code Math.random()} (class comment)
     */
    public static void buildBasiliskMaze(final LegacyWriter world, final Random rand, final Random mathRandom,
                                         final int x, final int y, final int z) {
        final int depth = 20 + rand.nextInt(10);
        clearArea(world, x + 3, y - depth - 4, z - 20);
        makeMaze(world, mathRandom, x + 3, y - depth - 3, z - 20, 10, 10, 3, 0);
        openMaze(world, x + 3, y - depth - 3, z - 20, 10, 10, 3);
        buildCastle(world, rand, x + 3, y - depth - 4, z - 20);
        makeEntrance(world, x, y, z, depth);
    }

    /** {@code makeMaze} (:35-105): randomized Prim on a {@code xw} x {@code zw} grid, walls 3 high. */
    private static void makeMaze(final LegacyWriter world, final Random mathRandom, final int xx, final int yy,
                                 final int zz, final int xw, final int zw, final int csz, final int b) {
        final int gridw = xw;
        final int gridh = zw;
        int cellsize = csz;
        if (cellsize < 3) {
            cellsize = 3;
        }
        final int[][] cells = new int[gridw][gridh];
        final int full = 15;
        for (int x = 0; x < gridw; ++x) {
            for (int y = 0; y < gridh; ++y) {
                cells[x][y] = full;
            }
        }
        final int left = 128;
        final int right = 32;
        for (int y = 0; y < gridh; ++y) {
            cells[0][y] |= left;
            cells[gridw - 1][y] |= right;
        }
        final int top = 16;
        final int bottom = 64;
        for (int x = 0; x < gridw; ++x) {
            cells[x][0] |= top;
            cells[x][gridh - 1] |= bottom;
        }
        final List<Point> outlist = new ArrayList<>(gridw * gridh);
        final List<Point> inlist = new ArrayList<>(10);
        final List<Point> frontlist = new ArrayList<>(10);
        for (int x = 0; x < gridw; ++x) {
            for (int y = 0; y < gridh; ++y) {
                outlist.add(new Point(x, y));
            }
        }
        Point current_cell = rndElement(mathRandom, outlist);
        inlist.add(current_cell);
        moveNbrs(current_cell, cells, outlist, frontlist);
        while (!frontlist.isEmpty()) {
            current_cell = rndElement(mathRandom, frontlist);
            inlist.add(current_cell);
            moveNbrs(current_cell, cells, outlist, frontlist);
            final int dir = findInNbr(mathRandom, current_cell, cells, inlist);
            removeWall(current_cell, dir, cells);
        }
        for (int x = 0; x < gridw; ++x) {
            for (int y = 0; y < gridh; ++y) {
                final int val = cells[x][y];
                if ((val & 0x1) != 0x0) {
                    drawSide(world, x * cellsize, y * cellsize, (x + 1) * cellsize, y * cellsize, xx, yy, zz, cellsize, gridh, gridw, b);
                }
                if ((val & 0x2) != 0x0) {
                    drawSide(world, (x + 1) * cellsize - 1, y * cellsize, (x + 1) * cellsize - 1, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw, b);
                }
                if ((val & 0x4) != 0x0) {
                    drawSide(world, x * cellsize, (y + 1) * cellsize - 1, (x + 1) * cellsize, (y + 1) * cellsize - 1, xx, yy, zz, cellsize, gridh, gridw, b);
                }
                if ((val & 0x8) != 0x0) {
                    drawSide(world, x * cellsize, y * cellsize, x * cellsize, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw, b);
                }
            }
        }
    }

    /** {@code drawSide} (:107-142): obsidian ({@code bb == 0}) or bedrock, 3 high, clipped to the grid. */
    private static void drawSide(final LegacyWriter world, int fromx, int fromz, int tox, int toz, final int x,
                                 final int y, final int z, final int cellsize, final int gridh, final int gridw,
                                 final int bb) {
        BlockState blk = obsidian();
        if (bb != 0) {
            blk = bedrock();
        }
        if (fromx > tox) {
            final int i = fromx;
            fromx = tox;
            tox = i;
        }
        if (fromz > toz) {
            final int i = fromz;
            fromz = toz;
            toz = i;
        }
        if (fromx == tox) {
            final int i = fromx;
            for (int j = fromz; j <= toz; ++j) {
                if (j < cellsize * gridh) {
                    world.setBlockFast(i + x, y, j + z, blk);
                    world.setBlockFast(i + x, y + 1, j + z, blk);
                    world.setBlockFast(i + x, y + 2, j + z, blk);
                }
            }
        } else {
            final int j = fromz;
            for (int i = fromx; i <= tox; ++i) {
                if (i < cellsize * gridw) {
                    world.setBlockFast(i + x, y, j + z, blk);
                    world.setBlockFast(i + x, y + 1, j + z, blk);
                    world.setBlockFast(i + x, y + 2, j + z, blk);
                }
            }
        }
    }

    /** {@code findInNbr} (:144-188). */
    private static int findInNbr(final Random mathRandom, final Point p, final int[][] cells, final List<Point> inlist) {
        int d = rnd(mathRandom, 4) - 1;
        for (int k = 0; k < 4; ++k) {
            switch (d) {
                case 0: {
                    if ((cells[p.x][p.y] & 0x10) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Point(p.x, p.y - 1)) >= 0) {
                        return 1;
                    }
                    break;
                }
                case 1: {
                    if ((cells[p.x][p.y] & 0x20) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Point(p.x + 1, p.y)) >= 0) {
                        return 2;
                    }
                    break;
                }
                case 2: {
                    if ((cells[p.x][p.y] & 0x40) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Point(p.x, p.y + 1)) >= 0) {
                        return 4;
                    }
                    break;
                }
                case 3: {
                    if ((cells[p.x][p.y] & 0x80) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Point(p.x - 1, p.y)) >= 0) {
                        return 8;
                    }
                    break;
                }
                default:
                    break;
            }
            d = (d + 1) % 4;
        }
        return 0;
    }

    /** {@code moveNbrs} (:190-207). */
    private static void moveNbrs(final Point p, final int[][] cells, final List<Point> outlist, final List<Point> frontlist) {
        if ((cells[p.x][p.y] & 0x10) == 0x0) {
            movePoint(new Point(p.x, p.y - 1), outlist, frontlist);
        }
        if ((cells[p.x][p.y] & 0x20) == 0x0) {
            movePoint(new Point(p.x + 1, p.y), outlist, frontlist);
        }
        if ((cells[p.x][p.y] & 0x40) == 0x0) {
            movePoint(new Point(p.x, p.y + 1), outlist, frontlist);
        }
        if ((cells[p.x][p.y] & 0x80) == 0x0) {
            movePoint(new Point(p.x - 1, p.y), outlist, frontlist);
        }
    }

    /** {@code movePoint} (:209-215). */
    private static void movePoint(final Point p, final List<Point> v, final List<Point> w) {
        final int i = v.indexOf(p);
        if (i >= 0) {
            v.remove(i);
            w.add(p);
        }
    }

    /** {@code removeWall} (:217-247). */
    private static void removeWall(final Point p, final int d, final int[][] cells) {
        cells[p.x][p.y] ^= d;
        switch (d) {
            case 1:
                cells[p.x][p.y - 1] ^= 0x4;
                break;
            case 2:
                cells[p.x + 1][p.y] ^= 0x8;
                break;
            case 4:
                cells[p.x][p.y + 1] ^= 0x1;
                break;
            case 8:
                cells[p.x - 1][p.y] ^= 0x2;
                break;
            default:
                break;
        }
    }

    /** {@code rnd(n)} (:249-251): {@code (int) (Math.random() * n + 1.0)}. */
    private static int rnd(final Random mathRandom, final int n) {
        return (int) (mathRandom.nextDouble() * n + 1.0);
    }

    /** {@code rndElement} (:253-258). */
    private static Point rndElement(final Random mathRandom, final List<Point> v) {
        final int i = rnd(mathRandom, v.size()) - 1;
        final Point s = v.get(i);
        v.remove(i);
        return s;
    }

    /**
     * {@code spawnCreature(world, name, x, y, z)} (:260-269): create by name, random yaw from {@code world.rand},
     * spawn, living sound.
     */
    @Nullable
    private static Entity spawnCreature(final LegacyWriter par0World, final Random rand, final String par1,
                                        final double par2, final double par4, final double par6) {
        return par0World.spawnEntity(par1, par2, par4, par6, rand, entity -> {
            if (entity instanceof Mob mob) {
                mob.setPersistenceRequired(); // func_110163_bv, see buildBasiliskMaze
            }
        });
    }

    /** {@code clearArea} (:271-290): 30x5x30 maze, 30x7x30 hall, 5x6x30 antechamber of air. */
    private static void clearArea(final LegacyWriter world, final int x, final int y, final int z) {
        for (int i = 0; i < 60; ++i) {
            int hi = 5;
            if (i >= 30) {
                hi = 7;
            }
            for (int j = 0; j < hi; ++j) {
                for (int k = 0; k < 30; ++k) {
                    world.setBlockFast(x + i, y + j, z + k, air());
                }
            }
        }
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 6; ++j) {
                for (int k = 0; k < 30; ++k) {
                    world.setBlockFast(x - i, y + j, z + k, air());
                }
            }
        }
    }

    /** {@code openMaze} (:292-311): entrance at x 0 and exit at x 29, beside the first air found one block inward. */
    private static void openMaze(final LegacyWriter world, final int xx, final int yy, final int zz, final int xw,
                                 final int zw, final int csz) {
        for (int i = 0; i < zw * csz; ++i) {
            final BlockState bid = world.getBlock(xx + 1, yy, zz + i);
            if (bid.isAir()) {
                world.setBlockFast(xx, yy, zz + i, air());
                world.setBlockFast(xx, yy + 1, zz + i, air());
                world.setBlockFast(xx, yy + 2, zz + i, air());
                break;
            }
        }
        for (int i = zw * csz - 1; i >= 0; --i) {
            final BlockState bid = world.getBlock(xx + xw * csz - 2, yy, zz + i);
            if (bid.isAir()) {
                world.setBlockFast(xx + xw * csz - 1, yy, zz + i, air());
                world.setBlockFast(xx + xw * csz - 1, yy + 1, zz + i, air());
                world.setBlockFast(xx + xw * csz - 1, yy + 2, zz + i, air());
                break;
            }
        }
    }

    /**
     * {@code buildCastle} (:313-424). The floating standing torches (redstone torches at x 30, torches over the
     * chests) are placed as in the original; with flag 2 nothing checks their support until a neighbour changes.
     *
     * <p>PORT: lava written into a live level gets its fluid tick from {@code LiquidBlock.onPlace}; the original's
     * {@code setBlockFast} skipped {@code onBlockAdded}, so its lava stood until a neighbour changed. The pits are
     * framed by the obsidian floor, so the difference only shows where the ground below the floor is open.
     */
    private static void buildCastle(final LegacyWriter world, final Random rand, final int x, final int y, final int z) {
        for (int i = 0; i < 60; ++i) {
            for (int k = 0; k < 30; ++k) {
                world.setBlockFast(x + i, y, z + k, obsidian());
            }
        }
        for (int i = 0; i < 80; ++i) {
            final int lx = x + rand.nextInt(28) + 1;
            final int lz = z + rand.nextInt(28) + 1;
            world.setBlockFast(lx, y, lz, Blocks.LAVA.defaultBlockState());
        }
        for (int i = 0; i < 20; ++i) {
            final int tx = x + 30 + rand.nextInt(28) + 1;
            final int tz = z + rand.nextInt(28) + 1;
            world.setBlockFast(tx, y, tz, ModBlocks.BLOCKTELEPORT.get().defaultBlockState());
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 0; k < 30; ++k) {
                world.setBlockFast(x + i, y + 4, z + k, bedrock());
            }
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 0; k < 30; ++k) {
                world.setBlockFast(x + i + 30, y + 6, z + k, bedrock());
            }
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 0; k < 5; ++k) {
                world.setBlockFast(x + 59, y + k + 1, z + i, obsidian());
                world.setBlockFast(x + 60, y + k + 1, z + i, bedrock());
                world.setBlockFast(x + 61, y + k + 1, z + i, bedrock());
            }
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 0; k < 5; ++k) {
                world.setBlockFast(x + 30 + i, y + k + 1, z, obsidian());
                world.setBlockFast(x + 30 + i, y + k + 1, z - 1, bedrock());
                world.setBlockFast(x + 30 + i, y + k + 1, z - 2, bedrock());
            }
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 0; k < 5; ++k) {
                world.setBlockFast(x + 30 + i, y + k + 1, z + 29, obsidian());
                world.setBlockFast(x + 30 + i, y + k + 1, z + 30, bedrock());
                world.setBlockFast(x + 30 + i, y + k + 1, z + 31, bedrock());
            }
        }
        for (int i = 0; i < 30; ++i) {
            world.setBlockFast(x + 30, y + 5, z + i, obsidian());
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 0; k < 4; ++k) {
                world.setBlockFast(x - 4 + k, y, z + i, sandstone());
            }
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 0; k < 4; ++k) {
                world.setBlockFast(x - 4 + k, y + 5, z + i, obsidian());
            }
        }
        for (int i = 0; i < 30; ++i) {
            for (int k = 1; k < 5; ++k) {
                world.setBlockFast(x - 5, y + k, z + i, Blocks.IRON_ORE.defaultBlockState());
            }
        }
        for (int i = 0; i < 5; ++i) {
            for (int k = 1; k < 5; ++k) {
                world.setBlockFast(x - 4 + i, y + k, z - 1, Blocks.IRON_ORE.defaultBlockState());
            }
        }
        for (int i = 0; i < 5; ++i) {
            for (int k = 1; k < 5; ++k) {
                world.setBlockFast(x - 4 + i, y + k, z + 30, Blocks.IRON_ORE.defaultBlockState());
            }
        }
        for (int k = 0; k < 4; ++k) {
            world.setBlockFast(x - 4, y + 1 + k, z, sandstone());
        }
        for (int k = 0; k < 4; ++k) {
            world.setBlockFast(x - 4, y + 1 + k, z + 15, sandstone());
        }
        for (int k = 0; k < 4; ++k) {
            world.setBlockFast(x - 4, y + 1 + k, z + 29, sandstone());
        }
        final BlockState extremeTorch = ModBlocks.EXTREME_TORCH.get().defaultBlockState();
        world.setBlockFast(x - 3, y + 3, z, extremeTorch);
        world.setBlockFast(x - 3, y + 3, z + 15, extremeTorch);
        world.setBlockFast(x - 3, y + 3, z + 29, extremeTorch);
        world.setBlockFast(x + 30, y + 4, z + 2, Blocks.REDSTONE_TORCH.defaultBlockState());
        world.setBlockFast(x + 30, y + 4, z + 15, Blocks.REDSTONE_TORCH.defaultBlockState());
        world.setBlockFast(x + 30, y + 4, z + 27, Blocks.REDSTONE_TORCH.defaultBlockState());
        for (int i = 2 + rand.nextInt(3), k = 0; k < i; ++k) {
            world.setBlockFast(x + 58, y + 4, z + 2 + k * 2, Blocks.TORCH.defaultBlockState());
            final Container chest = world.placeChest(x + 58, y + 1, z + 2 + k * 2);
            // Original: if (chest != null) generateChestContents(...). PORT: drawn in any case (LegacyWriter#placeChest).
            LegacyWriter.generateChestContents(rand, chestContentsList, chest, 5 + rand.nextInt(6));
        }
        // Original: ent = spawnCreature(...); if (ent != null) ent.func_110163_bv(). PORT: the flag is set before the
        // entity is added, because in world generation WorldGenRegion.addFreshEntity -> ProtoChunk.addEntity saves
        // the entity to NBT at once and a flag set afterwards would never be stored (the guards would despawn).
        spawnCreature(world, rand, BASILISK, x + 45.0, y + 1.01, z + 15.0);
        spawnCreature(world, rand, BASILISK, x + 46.0, y + 1.01, z + 15.0);
        spawnCreature(world, rand, BASILISK, x + 47.0, y + 1.01, z + 15.0);
    }

    /**
     * {@code makeEntrance} (:426-472): nine hollow sandstone rings from y+0 to y+8, then a 4x4 bedrock shaft down to
     * y-depth+1 with a 2x2 air core and one obsidian step per level going round.
     */
    public static void makeEntrance(final LegacyWriter world, final int x, final int y, final int z, final int depth) {
        int j;
        int width;
        for (width = (j = 8); j >= 0; --j) {
            for (int i = 0; i < j * 2 + 4; ++i) {
                world.setBlockFast(x + i - j, y + width - j, z - j, sandstone());
                world.setBlockFast(x + i - j, y + width - j, z + j + 3, sandstone());
                world.setBlockFast(x - j, y + width - j, z + i - j, sandstone());
                world.setBlockFast(x + j + 3, y + width - j, z + i - j, sandstone());
            }
        }
        int k = 0;
        for (j = width; j > -depth; --j) {
            for (int i = 0; i < 4; ++i) {
                world.setBlockFast(x + i, y + j, z, bedrock());
                world.setBlockFast(x + i, y + j, z + 3, bedrock());
                world.setBlockFast(x, y + j, z + i, bedrock());
                world.setBlockFast(x + 3, y + j, z + i, bedrock());
            }
            for (int l = 0; l < 2; ++l) {
                for (int m = 0; m < 2; ++m) {
                    world.setBlockFast(x + 1 + l, y + j, z + 1 + m, air());
                }
            }
            switch (k) {
                case 0:
                    world.setBlockFast(x + 1, y + j, z + 1, obsidian());
                    break;
                case 1:
                    world.setBlockFast(x + 2, y + j, z + 1, obsidian());
                    break;
                case 2:
                    world.setBlockFast(x + 2, y + j, z + 2, obsidian());
                    break;
                default:
                    world.setBlockFast(x + 1, y + j, z + 2, obsidian());
                    break;
            }
            if (++k > 3) {
                k = 0;
            }
        }
    }
}
