package com.swbr.orespawn.world.gen;

import java.util.Random;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenAbstractTree} ({@code arc}, client-1.7.10.jar): the contract of the
 * tree loop in {@link BiomeDecorator}.
 */
public abstract class WorldGenAbstractTree extends WorldGenerator {

    protected WorldGenAbstractTree(final boolean doBlockNotify) {
        super(doBlockNotify);
    }

    /**
     * {@code arc.a(Block)} ({@code func_150523_a}): material air or leaves, or one of grass, dirt, {@code log},
     * {@code log2}, sapling, vine. Those comparisons are by block, so every metadata counts: dirt with coarse
     * dirt and podzol, both log blocks with their bark-on-all-sides variant (1.21.1 {@code *_wood}), all six
     * saplings.
     */
    protected boolean isReplaceable(final BlockState block) {
        return block.isAir() || LegacyWorld.isLeaves(block) || block.is(Blocks.GRASS_BLOCK) || isDirt(block)
                || isLog(block) || isSapling(block) || block.is(Blocks.VINE);
    }

    /** {@code arc.b(World, Random, int, int, int)} ({@code func_150524_b}): empty for every 1.7.10 tree. */
    public void postGenerate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
    }

    /** {@code block == Blocks.grass || block == Blocks.dirt || block == Blocks.farmland}: the soil test under every trunk. */
    protected static boolean isSoil(final BlockState block) {
        return block.is(Blocks.GRASS_BLOCK) || isDirt(block) || block.is(Blocks.FARMLAND);
    }

    /**
     * {@code Blocks.dirt} with metadata 0 (dirt), 1 (coarse dirt) and 2 (podzol).
     *
     * <p>PORT (R22 with its 2026-09-14 addendum): the whole {@code #minecraft:dirt} tag, mycelium, moss, mud and
     * rooted dirt included. Every caller ({@link #isReplaceable}, {@link #isSoil}, {@code WorldGenBigTree}) tests
     * {@code Blocks.grass} in the same disjunction, so the grass block inside the tag changes no result.
     */
    protected static boolean isDirt(final BlockState block) {
        return block.is(BlockTags.DIRT);
    }

    /** {@code Blocks.log} (oak, spruce, birch, jungle) and {@code Blocks.log2} (acacia, dark oak), any metadata. */
    private static boolean isLog(final BlockState block) {
        return block.is(Blocks.OAK_LOG) || block.is(Blocks.SPRUCE_LOG) || block.is(Blocks.BIRCH_LOG)
                || block.is(Blocks.JUNGLE_LOG) || block.is(Blocks.ACACIA_LOG) || block.is(Blocks.DARK_OAK_LOG)
                || block.is(Blocks.OAK_WOOD) || block.is(Blocks.SPRUCE_WOOD) || block.is(Blocks.BIRCH_WOOD)
                || block.is(Blocks.JUNGLE_WOOD) || block.is(Blocks.ACACIA_WOOD) || block.is(Blocks.DARK_OAK_WOOD);
    }

    /** {@code Blocks.sapling}, any metadata. */
    private static boolean isSapling(final BlockState block) {
        return block.is(Blocks.OAK_SAPLING) || block.is(Blocks.SPRUCE_SAPLING) || block.is(Blocks.BIRCH_SAPLING)
                || block.is(Blocks.JUNGLE_SAPLING) || block.is(Blocks.ACACIA_SAPLING) || block.is(Blocks.DARK_OAK_SAPLING);
    }
}
