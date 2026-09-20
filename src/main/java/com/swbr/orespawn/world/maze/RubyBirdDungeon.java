package com.swbr.orespawn.world.maze;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import com.swbr.orespawn.world.tree.LegacyRandom;
import com.swbr.orespawn.world.tree.LegacyWriter;
import java.util.Random;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.RubyBirdDungeon} (RubyBirdDungeon.java:11-85), the singleton
 * {@code OreSpawnMain.RubyDungeon} (OreSpawnMain.java:5443): a 10x5x10 room of mossy and plain cobblestone with
 * some ruby ore, a Ruby Bird spawner in the middle and one chest (verhalten/world-04.md, "RubyBirdDungeon").
 *
 * <p>The footprint (origin chunk+0..7, extent +9) stays inside the 3x3-chunk window, so world generation places it
 * as a feature ({@link RubyBirdDungeonFeature}, DECISIONS R12); the Random Dungeon Spawner builds it in place.
 */
public final class RubyBirdDungeon {

    /** {@code EntityList} name "Ruby Bird" (:72). */
    public static final String RUBY_BIRD = "orespawn:ruby_bird";

    /** {@code chestContentsList} (RubyBirdDungeon.java:16): 14 entries, weight sum 215. */
    public static final WeightedRandomChestContent[] chestContentsList = {
        WeightedRandomChestContent.byId("orespawn:cageempty", 3, 10, 20),
        WeightedRandomChestContent.byId("orespawn:ruby", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:cookedbacon", 6, 12, 20),
        WeightedRandomChestContent.byId("orespawn:buttercandy", 6, 12, 20),
        WeightedRandomChestContent.byId("orespawn:rubypickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubyshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubyhoe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubyaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubysword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:thunderstaff", 1, 1, 5),
    };

    private RubyBirdDungeon() {
    }

    /** {@code setThisBlock} (:19-29): 1/20 ruby ore, else 1/2 mossy cobblestone, else cobblestone. */
    private static void setThisBlock(final LegacyWriter world, final Random rand, final int cposx, final int cposy,
                                     final int cposz) {
        if (rand.nextInt(20) == 1) {
            FastSetBlock(world, cposx, cposy, cposz, ModBlocks.ORERUBY.get().defaultBlockState());
        } else if (rand.nextInt(2) == 1) {
            FastSetBlock(world, cposx, cposy, cposz, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        } else {
            FastSetBlock(world, cposx, cposy, cposz, Blocks.COBBLESTONE.defaultBlockState());
        }
    }

    /** {@code makeDungeon} with the live level, for {@code DungeonSpawnerBlock}. */
    public static void makeDungeon(final Level world, final int cposx, final int cposy, final int cposz) {
        makeDungeon(LegacyWriter.live(world), new LegacyRandom(world.random), cposx, cposy, cposz);
    }

    /**
     * {@code makeDungeon(world, x, y, z)} (:31-80): clear 10x5x10, mossy floor, random ceiling and walls (the walls
     * overwrite the floor and ceiling rims and roll again for them), spawner at (x+5, y+1, z+5) and chest at
     * (x+5, y+1, z+1) through {@code world.setBlock}, 4-10 draws.
     */
    public static void makeDungeon(final LegacyWriter world, final Random rand, final int cposx, final int cposy,
                                   final int cposz) {
        final int width = 10;
        final int height = 5;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                for (int k = 0; k < width; ++k) {
                    FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = 0;
            for (int k = 0; k < width; ++k) {
                FastSetBlock(world, cposx + i, cposy + j, cposz + k, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            }
        }
        for (int i = 0; i < width; ++i) {
            final int j = height - 1;
            for (int k = 0; k < width; ++k) {
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int k = 0;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
                k = width - 1;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
            }
        }
        for (int k = 0; k < width; ++k) {
            for (int j = 0; j < height; ++j) {
                int i = 0;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
                i = width - 1;
                setThisBlock(world, rand, cposx + i, cposy + j, cposz + k);
            }
        }
        world.placeSpawner(cposx + width / 2, cposy + 1, cposz + width / 2, RUBY_BIRD);
        final Container chest = world.placeChest(cposx + width / 2, cposy + 1, cposz + 1);
        // Original: if (chest != null) generateChestContents(...). PORT: drawn in any case (LegacyWriter#placeChest).
        LegacyWriter.generateChestContents(rand, chestContentsList, chest, 4 + rand.nextInt(7));
    }

    /** {@code FastSetBlock} (:82-84): {@code setBlockFast(world, x, y, z, id, 0, 2)}. */
    public static void FastSetBlock(final LegacyWriter world, final int ix, final int iy, final int iz, final BlockState id) {
        world.setBlockFast(ix, iy, iz, id);
    }
}
