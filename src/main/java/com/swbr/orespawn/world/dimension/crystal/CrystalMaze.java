package com.swbr.orespawn.world.dimension.crystal;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Port of {@code CrystalMaze} (CrystalMaze.java:10-305; verhalten/world-04.md "CrystalMaze"): the
 * 16x5x16 bedrock labyrinth that {@link ChunkProviderOreSpawn5} builds into every chunk of the Crystal
 * dimension at world Y 24..28 - floor 24, walls 25..27, ceiling 28, four crystal stone holes in the
 * ceiling and one in the floor. Prim's algorithm over 4x4 cells of 4 blocks; the outer rows are opened
 * again, so the corridors connect across every chunk border.
 *
 * <p>Every write goes through {@link FastBlocks#setBlockInChunk}, which drops anything outside the
 * chunk exactly as {@code setBlockIDWithMetadataInChunk} did.
 *
 * <p>PORT: the layout came from {@code Math.random()} (:274-276) and the holes from {@code world.rand}
 * (:51-56), so no two generations of a chunk matched. 1.21.1 chunk generation must be deterministic
 * (DECISIONS R18, "Math.random in Labyrinthen"): both are {@link Random} instances the generator seeds
 * from the world seed and the chunk position. The draw order inside the class is unchanged.
 *
 * <p>PORT: {@code java.awt.Point} and {@code java.util.Vector} became the {@link Cell} record and
 * {@link ArrayList} (catalogue port note). {@code addElement}, {@code removeElementAt}, {@code indexOf}
 * and {@code elementAt} have the same order and equality semantics there.
 *
 * <p>Not ported: {@code clearArea} (:285-304), an unused copy from {@code BasiliskMaze}.
 */
public final class CrystalMaze {

    public static final int WTOP = 1;
    public static final int WRGT = 2;
    public static final int WBOT = 4;
    public static final int WLFT = 8;

    /** Replaces {@code Math.random()} (layout). */
    private final Random mathRandom;
    /** Replaces {@code world.rand} (the crystal stone holes). */
    private final Random worldRand;

    /** A maze cell, {@code java.awt.Point} with value equality. */
    private record Cell(int x, int y) {
    }

    public CrystalMaze(final Random mathRandom, final Random worldRand) {
        this.mathRandom = mathRandom;
        this.worldRand = worldRand;
    }

    /** {@code buildCrystalMaze(world, x, y, z, chunk)} (:17-27); the world was only used for its random. */
    public void buildCrystalMaze(final int x, final int y, final int z, final ChunkAccess chunk) {
        final BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 3; ++k) {
                    FastBlocks.setBlockInChunk(chunk, x + j, y + k, z + i, air);
                }
            }
        }
        this.makeMaze(x, y, z, 4, 4, 4, 1, chunk);
        this.openCrystalMaze(x, y, z, 4, 4, 4, chunk);
    }

    /** {@code openCrystalMaze} (:29-58). */
    private void openCrystalMaze(final int xx, final int yy, final int zz, final int xw, final int zw, final int csz,
                                 final ChunkAccess chunk) {
        final BlockState air = Blocks.AIR.defaultBlockState();
        final BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        final BlockState crystalStone = ModBlocks.CRYSTALSTONE.get().defaultBlockState();
        for (int i = 0; i < zw * csz; ++i) {
            FastBlocks.setBlockInChunk(chunk, xx, yy, zz + i, air);
            FastBlocks.setBlockInChunk(chunk, xx, yy + 1, zz + i, air);
            FastBlocks.setBlockInChunk(chunk, xx, yy + 2, zz + i, air);
            FastBlocks.setBlockInChunk(chunk, xx + i, yy, zz, air);
            FastBlocks.setBlockInChunk(chunk, xx + i, yy + 1, zz, air);
            FastBlocks.setBlockInChunk(chunk, xx + i, yy + 2, zz, air);
            FastBlocks.setBlockInChunk(chunk, xx + zw * csz - 1, yy, zz + i, air);
            FastBlocks.setBlockInChunk(chunk, xx + zw * csz - 1, yy + 1, zz + i, air);
            FastBlocks.setBlockInChunk(chunk, xx + zw * csz - 1, yy + 2, zz + i, air);
            FastBlocks.setBlockInChunk(chunk, xx + i, yy, zz + zw * csz - 1, air);
            FastBlocks.setBlockInChunk(chunk, xx + i, yy + 1, zz + zw * csz - 1, air);
            FastBlocks.setBlockInChunk(chunk, xx + i, yy + 2, zz + zw * csz - 1, air);
        }
        for (int i = 0; i < zw * csz; ++i) {
            for (int j = 0; j < zw * csz; ++j) {
                FastBlocks.setBlockInChunk(chunk, xx + j, yy - 1, zz + i, bedrock);
                FastBlocks.setBlockInChunk(chunk, xx + j, yy + 3, zz + i, bedrock);
            }
        }
        for (int k = 0; k < 4; ++k) {
            final int i = this.worldRand.nextInt(zw * csz);
            final int j = this.worldRand.nextInt(zw * csz);
            FastBlocks.setBlockInChunk(chunk, xx + j, yy + 3, zz + i, crystalStone);
        }
        final int i = this.worldRand.nextInt(zw * csz);
        final int j = this.worldRand.nextInt(zw * csz);
        FastBlocks.setBlockInChunk(chunk, xx + j, yy - 1, zz + i, crystalStone);
    }

    /** {@code makeMaze} (:60-130). */
    private void makeMaze(final int xx, final int yy, final int zz, final int xw, final int zw, final int csz, final int b,
                          final ChunkAccess chunk) {
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
        final List<Cell> outlist = new ArrayList<>(gridw * gridh);
        final List<Cell> inlist = new ArrayList<>(10);
        final List<Cell> frontlist = new ArrayList<>(10);
        for (int x = 0; x < gridw; ++x) {
            for (int y = 0; y < gridh; ++y) {
                outlist.add(new Cell(x, y));
            }
        }
        Cell currentCell = this.rndElement(outlist);
        inlist.add(currentCell);
        this.moveNbrs(currentCell, cells, outlist, frontlist);
        while (!frontlist.isEmpty()) {
            currentCell = this.rndElement(frontlist);
            inlist.add(currentCell);
            this.moveNbrs(currentCell, cells, outlist, frontlist);
            final int dir = this.findInNbr(currentCell, cells, inlist);
            this.removeWall(currentCell, dir, cells);
        }
        for (int x = 0; x < gridw; ++x) {
            for (int y = 0; y < gridh; ++y) {
                final int val = cells[x][y];
                if ((val & 0x1) != 0x0) {
                    this.drawSide(x * cellsize, y * cellsize, (x + 1) * cellsize, y * cellsize, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
                }
                if ((val & 0x2) != 0x0) {
                    this.drawSide((x + 1) * cellsize - 1, y * cellsize, (x + 1) * cellsize - 1, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
                }
                if ((val & 0x4) != 0x0) {
                    this.drawSide(x * cellsize, (y + 1) * cellsize - 1, (x + 1) * cellsize, (y + 1) * cellsize - 1, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
                }
                if ((val & 0x8) != 0x0) {
                    this.drawSide(x * cellsize, y * cellsize, x * cellsize, (y + 1) * cellsize, xx, yy, zz, cellsize, gridh, gridw, b, chunk);
                }
            }
        }
    }

    /** {@code drawSide} (:132-167): {@code bb != 0} means bedrock, otherwise obsidian. */
    private void drawSide(int fromx, int fromz, int tox, int toz, final int x, final int y, final int z, final int cellsize,
                          final int gridh, final int gridw, final int bb, final ChunkAccess chunk) {
        BlockState blk = Blocks.OBSIDIAN.defaultBlockState();
        if (bb != 0) {
            blk = Blocks.BEDROCK.defaultBlockState();
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
                    FastBlocks.setBlockInChunk(chunk, i + x, y, j + z, blk);
                    FastBlocks.setBlockInChunk(chunk, i + x, y + 1, j + z, blk);
                    FastBlocks.setBlockInChunk(chunk, i + x, y + 2, j + z, blk);
                }
            }
        } else {
            final int j = fromz;
            for (int i = fromx; i <= tox; ++i) {
                if (i < cellsize * gridw) {
                    FastBlocks.setBlockInChunk(chunk, i + x, y, j + z, blk);
                    FastBlocks.setBlockInChunk(chunk, i + x, y + 1, j + z, blk);
                    FastBlocks.setBlockInChunk(chunk, i + x, y + 2, j + z, blk);
                }
            }
        }
    }

    /** {@code findInNbr} (:169-213). */
    private int findInNbr(final Cell p, final int[][] cells, final List<Cell> inlist) {
        int d = this.rnd(4) - 1;
        for (int k = 0; k < 4; ++k) {
            switch (d) {
                case 0: {
                    if ((cells[p.x()][p.y()] & 0x10) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Cell(p.x(), p.y() - 1)) >= 0) {
                        return 1;
                    }
                    break;
                }
                case 1: {
                    if ((cells[p.x()][p.y()] & 0x20) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Cell(p.x() + 1, p.y())) >= 0) {
                        return 2;
                    }
                    break;
                }
                case 2: {
                    if ((cells[p.x()][p.y()] & 0x40) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Cell(p.x(), p.y() + 1)) >= 0) {
                        return 4;
                    }
                    break;
                }
                case 3: {
                    if ((cells[p.x()][p.y()] & 0x80) != 0x0) {
                        break;
                    }
                    if (inlist.indexOf(new Cell(p.x() - 1, p.y())) >= 0) {
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

    /** {@code moveNbrs} (:215-232). */
    private void moveNbrs(final Cell p, final int[][] cells, final List<Cell> outlist, final List<Cell> frontlist) {
        if ((cells[p.x()][p.y()] & 0x10) == 0x0) {
            this.movePoint(new Cell(p.x(), p.y() - 1), outlist, frontlist);
        }
        if ((cells[p.x()][p.y()] & 0x20) == 0x0) {
            this.movePoint(new Cell(p.x() + 1, p.y()), outlist, frontlist);
        }
        if ((cells[p.x()][p.y()] & 0x40) == 0x0) {
            this.movePoint(new Cell(p.x(), p.y() + 1), outlist, frontlist);
        }
        if ((cells[p.x()][p.y()] & 0x80) == 0x0) {
            this.movePoint(new Cell(p.x() - 1, p.y()), outlist, frontlist);
        }
    }

    /** {@code movePoint} (:234-240). */
    private void movePoint(final Cell p, final List<Cell> v, final List<Cell> w) {
        final int i = v.indexOf(p);
        if (i >= 0) {
            v.remove(i);
            w.add(p);
        }
    }

    /** {@code removeWall} (:242-272). */
    private void removeWall(final Cell p, final int d, final int[][] cells) {
        cells[p.x()][p.y()] ^= d;
        switch (d) {
            case 1:
                cells[p.x()][p.y() - 1] ^= 0x4;
                break;
            case 2:
                cells[p.x() + 1][p.y()] ^= 0x8;
                break;
            case 4:
                cells[p.x()][p.y() + 1] ^= 0x1;
                break;
            case 8:
                cells[p.x() - 1][p.y()] ^= 0x2;
                break;
            default:
                break;
        }
    }

    /** {@code rnd} (:274-276): {@code (int) (Math.random() * n + 1.0)}, see the class note. */
    private int rnd(final int n) {
        return (int) (this.mathRandom.nextDouble() * n + 1.0);
    }

    /** {@code rndElement} (:278-283). */
    private Cell rndElement(final List<Cell> v) {
        final int i = this.rnd(v.size()) - 1;
        final Cell s = v.get(i);
        v.remove(i);
        return s;
    }
}
