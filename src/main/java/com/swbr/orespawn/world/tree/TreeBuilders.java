package com.swbr.orespawn.world.tree;

import com.swbr.orespawn.item.magic.ItemMagicApple;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.maze.BasiliskMaze;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry.Placement;
import java.util.Random;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The builders of this wave's large generators for the shared structure type {@code orespawn:legacy}
 * ({@link LegacyStructureRegistry}), plus the placement boxes their dispatchers hand out. No original class: 1.7.10
 * called {@code OreSpawnTrees.WindTree(...)} etc. straight from {@code OreSpawnWorld.generate}; 1.21.1 splits that
 * into a dispatcher ("where", in {@code OreSpawnWorld}) and these builders ("what").
 *
 * <p>Every builder runs once per chunk its box touches with a fresh {@code Random(seed)} and a clipped writer, so it
 * wraps the writer in {@link LegacyWriter#of} (stable reads, chest and yaw draws outside the clip). The random stands
 * in for both {@code world.rand} and {@code OreSpawnRand} (and, for the maze, {@code Math.random}, see there).
 *
 * <p>Boxes are the inclusive extents of the original loops at their largest random values (verhalten/world-04.md,
 * "Generatoren: Maße und Palette", recomputed from the source). PORT: a 1.21.1 structure is referenced only by chunks
 * within 8 chunks of its start chunk ({@code ChunkGenerator.createReferences}); what a Big Square Tree of the Magic
 * Apple writes beyond that (its branches can reach about 170 blocks) is not generated in world generation. The item
 * at run time builds it complete.
 *
 * <p>Call {@link #bootstrap()} once before the server starts (from {@code OreSpawnWorld.bootstrap}).
 */
public final class TreeBuilders {

    public static final String WIND_TREE = "orespawn:wind_tree";
    public static final String SKY_TREE = "orespawn:sky_tree";
    public static final String FAIRY_TREE = "orespawn:fairy_tree";
    public static final String FAIRY_CASTLE_TREE = "orespawn:fairy_castle_tree";
    public static final String BASILISK_MAZE = "orespawn:basilisk_maze";
    public static final String MAGIC_APPLE_TREE = "orespawn:magic_apple_tree";

    /** Magic Apple tree shapes (variant bits 0-1). */
    public static final int SHAPE_SQUARE = 0;
    public static final int SHAPE_CIRCULAR = 1;
    public static final int SHAPE_ROUND = 2;

    /**
     * Magic Apple block sets (variant bits 10-12), the argument triples {@code (ID, leafID, stepID)} of
     * {@code OreSpawnWorld.addHugeTree} (OreSpawnWorld.java:1931-1949) and {@code ItemMagicApple.onItemUse}.
     */
    public static final int BLOCKS_LOG_LEAVES_MOSSY = 0;
    public static final int BLOCKS_LOG_APPLE_LEAVES_MOSSY = 1;
    public static final int BLOCKS_GOLD_EMERALD_DIAMOND = 2;
    public static final int BLOCKS_OBSIDIAN_RUBY_AMETHYST = 3;
    public static final int BLOCKS_LOG_LEAVES_IRON_ORE = 4;

    private TreeBuilders() {
    }

    /** Registers the six builders. Idempotent. */
    public static void bootstrap() {
        LegacyStructureRegistry.registerBuilder(WIND_TREE, (world, random, x, y, z, variant) ->
                Trees.WindTree(LegacyWriter.of(world), random, x, y, z, variant, false));
        LegacyStructureRegistry.registerBuilder(SKY_TREE, (world, random, x, y, z, variant) ->
                Trees.SkyTree(LegacyWriter.of(world), random, x, y, z, false));
        LegacyStructureRegistry.registerBuilder(FAIRY_TREE, (world, random, x, y, z, variant) ->
                Trees.FairyTree(LegacyWriter.of(world), random, x, y, z));
        LegacyStructureRegistry.registerBuilder(FAIRY_CASTLE_TREE, (world, random, x, y, z, variant) ->
                Trees.FairyCastleTree(LegacyWriter.of(world), random, x, y, z));
        LegacyStructureRegistry.registerBuilder(BASILISK_MAZE, (world, random, x, y, z, variant) -> {
            // PORT (R18, Math.random): the maze layout random is the first long of the structure random, drawn in
            // every run before anything else.
            final Random mathRandom = new Random(random.nextLong());
            BasiliskMaze.buildBasiliskMaze(LegacyWriter.of(world), random, mathRandom, x, y, z);
        });
        LegacyStructureRegistry.registerBuilder(MAGIC_APPLE_TREE, (world, random, x, y, z, variant) ->
                buildMagicAppleTree(LegacyWriter.of(world), random, x, y, z, variant));
    }

    // ---------------------------------------------------------------------------------------------------
    // placements

    /** {@code WindTree(world, x, y, z, dir)}: trunk 40-47, branches up to 37 in +dir, one block to each side. */
    public static Placement windTree(final int x, final int y, final int z, final int dir) {
        return switch (dir) {
            case 1 -> Placement.of(WIND_TREE, x, y, z, dir, -37, 0, -1, 0, 48, 1);
            case 2 -> Placement.of(WIND_TREE, x, y, z, dir, -1, 0, 0, 1, 48, 37);
            case 3 -> Placement.of(WIND_TREE, x, y, z, dir, -1, 0, -37, 1, 48, 0);
            default -> Placement.of(WIND_TREE, x, y, z, dir, 0, 0, -1, 37, 48, 1);
        };
    }

    /** {@code SkyTree(world, x, y, z)}: branches up to 34 each way, top at absolute Y 205 at most. */
    public static Placement skyTree(final int x, final int y, final int z) {
        return Placement.of(SKY_TREE, x, y, z, 0, -34, 0, -34, 34, Math.max(1, 205 - y), 34);
    }

    /** {@code FairyTree(world, x, y, z)}: eight crystal branches of up to 23 plus leaves, crown up to y+15. */
    public static Placement fairyTree(final int x, final int y, final int z) {
        return Placement.of(FAIRY_TREE, x, y, z, 0, -27, 0, -27, 27, 16, 27);
    }

    /** {@code FairyCastleTree(world, x, y, z)}: platforms up to 42 out, y+2..y+37. */
    public static Placement fairyCastleTree(final int x, final int y, final int z) {
        return Placement.of(FAIRY_CASTLE_TREE, x, y, z, 0, -43, 0, -43, 43, 38, 43);
    }

    /** {@code buildBasiliskMaze(world, x, y, z)}: x-8..x+64, y-33..y+8 (depth 29), z-22..z+11. */
    public static Placement basiliskMaze(final int x, final int y, final int z) {
        return Placement.of(BASILISK_MAZE, x, y, z, 0, -8, -33, -22, 64, 8, 11);
    }

    /**
     * A Magic Apple tree ({@code MakeBigSquareTree}, {@code MakeBigCircularTree}, {@code MakeBigRoundTree}).
     *
     * @param treeType   the {@code tree_type} argument, -1..3
     * @param radius     {@code t_radius}, 1..15
     * @param badCritters {@code bad_critters} ({@code no_critters}); ignored by the round tree
     * @param blockSet   one of the {@code BLOCKS_*} constants
     */
    public static Placement magicAppleTree(final int x, final int y, final int z, final int shape, final int treeType,
                                           final int radius, final boolean badCritters, final int blockSet) {
        final int variant = (shape & 3) | ((treeType + 1) & 7) << 2 | (radius & 15) << 5 | (badCritters ? 1 : 0) << 9
                | (blockSet & 7) << 10;
        return switch (shape) {
            // rad shrinks by 0.01 * nextInt(15) per level and may take a long time to reach 0: up to the build limit.
            case SHAPE_CIRCULAR -> Placement.of(MAGIC_APPLE_TREE, x, y, z, variant, -55, -20, -55, 55, 512, 55);
            case SHAPE_ROUND -> Placement.of(MAGIC_APPLE_TREE, x, y, z, variant, -45, -20, -45, 45, 512, 45);
            default -> Placement.of(MAGIC_APPLE_TREE, x, y, z, variant, -175, -20, -175, 175, 215, 175);
        };
    }

    private static void buildMagicAppleTree(final LegacyWriter world, final Random random, final int x, final int y,
                                            final int z, final int variant) {
        final int shape = variant & 3;
        final int treeType = ((variant >> 2) & 7) - 1;
        final int radius = (variant >> 5) & 15;
        final boolean badCritters = ((variant >> 9) & 1) != 0;
        final Block id;
        final Block leaf;
        final Block step;
        switch ((variant >> 10) & 7) {
            case BLOCKS_LOG_APPLE_LEAVES_MOSSY -> {
                id = Blocks.OAK_LOG;
                leaf = ModBlocks.LEAVES_APPLE.get();
                step = Blocks.MOSSY_COBBLESTONE;
            }
            case BLOCKS_GOLD_EMERALD_DIAMOND -> {
                id = Blocks.GOLD_BLOCK;
                leaf = Blocks.EMERALD_BLOCK;
                step = Blocks.DIAMOND_BLOCK;
            }
            case BLOCKS_OBSIDIAN_RUBY_AMETHYST -> {
                id = Blocks.OBSIDIAN;
                leaf = ModBlocks.BLOCKRUBY.get();
                step = ModBlocks.BLOCKAMETHYST.get();
            }
            case BLOCKS_LOG_LEAVES_IRON_ORE -> {
                id = Blocks.OAK_LOG;
                leaf = Blocks.OAK_LEAVES;
                step = Blocks.IRON_ORE;
            }
            default -> {
                id = Blocks.OAK_LOG;
                leaf = Blocks.OAK_LEAVES;
                step = Blocks.MOSSY_COBBLESTONE;
            }
        }
        // One seeded random for OreSpawnRand and world.rand alike; neither was reproducible in 1.7.10.
        switch (shape) {
            case SHAPE_CIRCULAR -> ItemMagicApple.MakeBigCircularTree(world, random, x, y, z, id, leaf, step, treeType,
                    radius, badCritters);
            case SHAPE_ROUND -> ItemMagicApple.MakeBigRoundTree(world, random, x, y, z, id, leaf, step, treeType, radius);
            default -> ItemMagicApple.MakeBigSquareTree(world, random, random, x, y, z, id, leaf, step, treeType, radius,
                    badCritters);
        }
    }
}
