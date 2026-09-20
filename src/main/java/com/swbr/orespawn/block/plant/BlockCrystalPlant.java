package com.swbr.orespawn.block.plant;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockCrystalPlant}, one class behind three saplings
 * (OreSpawnMain.java:1631-1633; tab Decorations, random ticks, BlockCrystalPlant.java:14-19):
 *
 * <table>
 * <tr><th>id</th><th>original field</th><th>tree</th><th>leaves</th><th>drop</th></tr>
 * <tr><td>{@code crystalsapling}</td><td>{@code MyCrystalPlant}</td><td>{@code TallCrystalTree}</td><td>{@code crystaltreeleaves}</td><td>{@code crystalsapling}</td></tr>
 * <tr><td>{@code crystalsapling2}</td><td>{@code MyCrystalPlant2}</td><td>{@code ScragglyCrystalTreeWithBranches}</td><td>{@code crystaltreeleaves2}</td><td>{@code crystalsapling3} (!)</td></tr>
 * <tr><td>{@code crystalsapling3}</td><td>{@code MyCrystalPlant3}</td><td>{@code TallCrystalTreeBlue}</td><td>{@code crystaltreeleaves3}</td><td>{@code crystalsapling3}</td></tr>
 * </table>
 *
 * The yellow sapling drops the blue one: {@code getItemDropped} (:54-62) lacks a {@code return} in
 * its second branch. Kept 1:1 (DECISIONS R18, "gelbe Kristallpflanze droppt blau"); the loot table
 * {@code blocks/crystalsapling2} names {@code crystalsapling3}.
 *
 * <ul>
 *   <li>Ground: grass, dirt, farmland, crystal grass (:21-24).</li>
 *   <li>Client: 1/30 per display tick, 10 happy-villager particles (:26-33).</li>
 *   <li>Random tick: 1/5 the sapling becomes air and its tree grows (:35-52); no light or space
 *       check, and a tree that hits an obstacle stops where it is without cleaning up.</li>
 * </ul>
 * The three tree builders (:80-289) are ported below as instance methods, every write through
 * {@code setBlockFast(..., 2)} like the original.
 */
public class BlockCrystalPlant extends ReedLikePlant {

    /** Which of the three {@code this == OreSpawnMain.X} identities this instance is. */
    public enum Kind {
        RED, YELLOW, BLUE
    }

    private final Kind kind;

    public BlockCrystalPlant(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** {@code canPlaceBlockAt} (BlockCrystalPlant.java:21-24). */
    @Override
    protected boolean mayPlaceOn(BlockState soil) {
        return isGrass(soil) || isDirt(soil) || isFarmland(soil) || soil.is(ModBlocks.CRYSTAL_GRASS);
    }

    /** {@code randomDisplayTick} (BlockCrystalPlant.java:26-33). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(30) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 10; ++j1) {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + random.nextFloat(), pos.getY() + random.nextFloat(), pos.getZ() + random.nextFloat(),
                    0.0, 0.0, 0.0);
        }
    }

    /** {@code updateTick} (BlockCrystalPlant.java:35-52); server side by construction. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) != 1) {
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        switch (this.kind) {
            case RED -> this.TallCrystalTree(level, pos.getX(), pos.getY(), pos.getZ());
            case YELLOW -> this.ScragglyCrystalTreeWithBranches(level, pos.getX(), pos.getY(), pos.getZ());
            case BLUE -> this.TallCrystalTreeBlue(level, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    // ---------------------------------------------------------------- tree builders (:80-289)

    private static BlockState log() {
        return ModBlocks.CRYSTAL_TREE_LOG.get().defaultBlockState();
    }

    private static BlockState leaves(Kind kind) {
        return switch (kind) {
            case RED -> ModBlocks.CRYSTAL_TREE_LEAVES.get().defaultBlockState();
            case YELLOW -> ModBlocks.CRYSTAL_TREE_LEAVES2.get().defaultBlockState();
            case BLUE -> ModBlocks.CRYSTAL_TREE_LEAVES3.get().defaultBlockState();
        };
    }

    private static BlockState getBlock(Level world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z));
    }

    /** {@code bid != Blocks.air && bid != MyCrystalTreeLog && bid != <own leaves>}: something else is in the way. */
    private static boolean blocked(BlockState bid, BlockState leaves) {
        return !bid.isAir() && !bid.is(log().getBlock()) && !bid.is(leaves.getBlock());
    }

    private static void setBlockFast(Level world, int x, int y, int z, BlockState state) {
        FastBlocks.setBlockFast(world, x, y, z, state, 2);
    }

    /** {@code TallCrystalTree} (BlockCrystalPlant.java:80-139): trunk 10..21, crown rings every 4th block, 7x7 canopy. */
    public void TallCrystalTree(final Level world, final int x, int y, final int z) {
        final RandomSource rand = world.random;
        final BlockState leaves = leaves(Kind.RED);
        final int i = 10 + rand.nextInt(12);
        final int j = i + rand.nextInt(18);
        for (int k = 0; k < i; ++k) {
            final BlockState bid = getBlock(world, x, y + k, z);
            if (k >= 1 && blocked(bid, leaves)) {
                return;
            }
            setBlockFast(world, x, y + k, z, log());
        }
        y += i - 1;
        for (int k = i; k < j; ++k) {
            ++y;
            BlockState bid = getBlock(world, x, y, z);
            if (blocked(bid, leaves)) {
                break;
            }
            setBlockFast(world, x, y, z, log());
            if (k % 4 == 0) {
                for (int m = -1; m < 2; ++m) {
                    for (int n = -1; n < 2; ++n) {
                        if (rand.nextInt(2) == 1) {
                            bid = getBlock(world, x + m, y, z + n);
                            if (bid.isAir()) {
                                setBlockFast(world, x + m, y, z + n, leaves);
                            }
                        }
                    }
                }
            }
        }
        ++y;
        for (int m = -1; m < 2; ++m) {
            for (int n = -1; n < 2; ++n) {
                if (rand.nextInt(2) == 1) {
                    final BlockState bid = getBlock(world, x + m, y, z + n);
                    if (bid.isAir()) {
                        setBlockFast(world, x + m, y, z + n, log());
                    }
                }
            }
        }
        for (int m = -3; m < 4; ++m) {
            for (int n = -3; n < 4; ++n) {
                final BlockState bid = getBlock(world, x + m, y, z + n);
                if (bid.isAir()) {
                    setBlockFast(world, x + m, y, z + n, leaves);
                }
            }
        }
        ++y;
        for (int m = -1; m < 2; ++m) {
            for (int n = -1; n < 2; ++n) {
                final BlockState bid = getBlock(world, x + m, y, z + n);
                if (bid.isAir()) {
                    setBlockFast(world, x + m, y, z + n, leaves);
                }
            }
        }
    }

    /** {@code makeScragglyCrystalBranch} (BlockCrystalPlant.java:141-183). */
    public void makeScragglyCrystalBranch(final Level world, int x, int y, int z, final int len, final int biasx, final int biasz) {
        final RandomSource rand = world.random;
        final BlockState leaves = leaves(Kind.YELLOW);
        for (int k = 0; k < len; ++k) {
            int ix = rand.nextInt(2) - rand.nextInt(2) + biasx;
            int iz = rand.nextInt(2) - rand.nextInt(2) + biasz;
            if (ix > 1) {
                ix = 1;
            }
            if (ix < -1) {
                ix = -1;
            }
            if (iz > 1) {
                iz = 1;
            }
            if (iz < -1) {
                iz = -1;
            }
            final int iy = (rand.nextInt(3) > 0) ? 1 : 0;
            x += ix;
            z += iz;
            y += iy;
            BlockState bid = getBlock(world, x, y, z);
            if (blocked(bid, leaves)) {
                return;
            }
            setBlockFast(world, x, y, z, log());
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (rand.nextInt(2) == 1) {
                        bid = getBlock(world, x + m, y, z + n);
                        if (bid.isAir()) {
                            setBlockFast(world, x + m, y, z + n, leaves);
                        }
                    }
                }
            }
            if (rand.nextInt(2) == 1) {
                bid = getBlock(world, x, y + 1, z);
                if (bid.isAir()) {
                    setBlockFast(world, x, y + 1, z, leaves);
                }
            }
        }
    }

    /** {@code ScragglyCrystalTreeWithBranches} (BlockCrystalPlant.java:185-228): trunk 1..2, wandering top with branches. */
    public void ScragglyCrystalTreeWithBranches(final Level world, int x, int y, int z) {
        final RandomSource rand = world.random;
        final BlockState leaves = leaves(Kind.YELLOW);
        final int i = 1 + rand.nextInt(2);
        final int j = i + rand.nextInt(8);
        for (int k = 0; k < i; ++k) {
            final BlockState bid = getBlock(world, x, y + k, z);
            if (k >= 1 && blocked(bid, leaves)) {
                return;
            }
            setBlockFast(world, x, y + k, z, log());
        }
        y += i - 1;
        for (int k = i; k < j; ++k) {
            final int ix = rand.nextInt(2) - rand.nextInt(2);
            final int iz = rand.nextInt(2) - rand.nextInt(2);
            final int iy = (rand.nextInt(4) > 0) ? 1 : 0;
            x += ix;
            z += iz;
            y += iy;
            BlockState bid = getBlock(world, x, y, z);
            if (blocked(bid, leaves)) {
                break;
            }
            setBlockFast(world, x, y, z, log());
            if (rand.nextInt(4) == 1) {
                this.makeScragglyCrystalBranch(world, x, y, z, rand.nextInt(1 + j - k),
                        rand.nextInt(2) - rand.nextInt(2), rand.nextInt(2) - rand.nextInt(2));
            }
            for (int m = -1; m < 2; ++m) {
                for (int n = -1; n < 2; ++n) {
                    if (rand.nextInt(2) == 1) {
                        bid = getBlock(world, x + m, y, z + n);
                        if (bid.isAir()) {
                            setBlockFast(world, x + m, y, z + n, leaves);
                        }
                    }
                }
            }
            if (rand.nextInt(2) == 1) {
                bid = getBlock(world, x, y + 1, z);
                if (bid.isAir()) {
                    setBlockFast(world, x, y + 1, z, leaves);
                }
            }
        }
    }

    /** {@code TallCrystalTreeBlue} (BlockCrystalPlant.java:230-289): trunk 5..10, rings every 3rd block, same crown. */
    public void TallCrystalTreeBlue(final Level world, final int x, int y, final int z) {
        final RandomSource rand = world.random;
        final BlockState leaves = leaves(Kind.BLUE);
        final int i = 5 + rand.nextInt(6);
        final int j = 2 + i + rand.nextInt(12);
        for (int k = 0; k < i; ++k) {
            final BlockState bid = getBlock(world, x, y + k, z);
            if (k >= 1 && blocked(bid, leaves)) {
                return;
            }
            setBlockFast(world, x, y + k, z, log());
        }
        y += i - 1;
        for (int k = i; k < j; ++k) {
            ++y;
            BlockState bid = getBlock(world, x, y, z);
            if (blocked(bid, leaves)) {
                break;
            }
            setBlockFast(world, x, y, z, log());
            if (k % 3 == 0) {
                for (int m = -1; m < 2; ++m) {
                    for (int n = -1; n < 2; ++n) {
                        if (rand.nextInt(2) == 1) {
                            bid = getBlock(world, x + m, y, z + n);
                            if (bid.isAir()) {
                                setBlockFast(world, x + m, y, z + n, leaves);
                            }
                        }
                    }
                }
            }
        }
        ++y;
        for (int m = -1; m < 2; ++m) {
            for (int n = -1; n < 2; ++n) {
                if (rand.nextInt(2) == 1) {
                    final BlockState bid = getBlock(world, x + m, y, z + n);
                    if (bid.isAir()) {
                        setBlockFast(world, x + m, y, z + n, log());
                    }
                }
            }
        }
        for (int m = -3; m < 4; ++m) {
            for (int n = -3; n < 4; ++n) {
                final BlockState bid = getBlock(world, x + m, y, z + n);
                if (bid.isAir()) {
                    setBlockFast(world, x + m, y, z + n, leaves);
                }
            }
        }
        ++y;
        for (int m = -1; m < 2; ++m) {
            for (int n = -1; n < 2; ++n) {
                final BlockState bid = getBlock(world, x + m, y, z + n);
                if (bid.isAir()) {
                    setBlockFast(world, x + m, y, z + n, leaves);
                }
            }
        }
    }
}
