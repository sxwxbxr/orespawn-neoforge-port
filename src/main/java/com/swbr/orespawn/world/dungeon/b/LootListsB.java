package com.swbr.orespawn.world.dungeon.b;

import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * The eight chest lists of {@code GenericDungeon} (built in its constructor, :70-77) that the methods of lines
 * 2401-4800 fill from. Order, counts and weights as in the source; the draw order of
 * {@link WeightedRandomChestContent#getRandomItem} depends on the entry order, so it stays. Item names from
 * verhalten/world-01.md, "Loot-Tabellen" (1.7.10 item to 1.21.1 id), checked against GenericDungeon.java:70-77.
 *
 * <p>OreSpawn items are named by registry id ({@link WeightedRandomChestContent#byId}) and resolved when a chest
 * is filled: a missing id (a wave that is not ported yet) still takes part in the draw and places nothing.
 *
 * <p>PORT: {@code DamselContentsList} is also used by {@code makeGirlfriendIsland} (:4997, porter
 * {@code w13-dungeon-c}), which may hold its own copy; both copies are this one line of the constructor.
 */
public final class LootListsB {

    private LootListsB() {
    }

    /** {@code RobotContentsList} (:70): 23 entries, weight sum 755. */
    public static final WeightedRandomChestContent[] RobotContentsList = {
        WeightedRandomChestContent.of(Items.REDSTONE, 1, 10, 35),
        WeightedRandomChestContent.of(Items.REPEATER, 1, 10, 35),
        WeightedRandomChestContent.of(Items.MINECART, 1, 1, 35),
        WeightedRandomChestContent.of(Items.FIRE_CHARGE, 1, 10, 35),
        WeightedRandomChestContent.of(Items.HOPPER_MINECART, 1, 1, 35),
        WeightedRandomChestContent.of(Blocks.REDSTONE_BLOCK, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.RAIL, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.DETECTOR_RAIL, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.STICKY_PISTON, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.PISTON, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.REDSTONE_TORCH, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.TNT, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.RAIL, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.LEVER, 1, 10, 35),
        WeightedRandomChestContent.byId("orespawn:antrobotkit", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:spiderrobotkit", 1, 1, 10),
        WeightedRandomChestContent.of(Items.IRON_DOOR, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.REDSTONE_TORCH, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.OAK_BUTTON, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.IRON_BARS, 1, 10, 35),
        WeightedRandomChestContent.of(Items.COMPARATOR, 1, 10, 35),
        WeightedRandomChestContent.of(Blocks.ACTIVATOR_RAIL, 1, 10, 35),
        WeightedRandomChestContent.byId("orespawn:raygun", 1, 1, 35),
    };

    /** {@code IncaPyramidContentsList} (:71): 14 entries, weight sum 480. */
    public static final WeightedRandomChestContent[] IncaPyramidContentsList = {
        WeightedRandomChestContent.of(Items.GOLDEN_SWORD, 1, 1, 35),
        WeightedRandomChestContent.of(Items.GOLDEN_BOOTS, 1, 1, 35),
        WeightedRandomChestContent.of(Items.GOLDEN_LEGGINGS, 1, 1, 35),
        WeightedRandomChestContent.of(Items.GOLDEN_HELMET, 1, 1, 35),
        WeightedRandomChestContent.of(Items.GOLDEN_CHESTPLATE, 1, 1, 35),
        WeightedRandomChestContent.of(Blocks.DANDELION, 3, 10, 35),
        WeightedRandomChestContent.of(Blocks.POPPY, 3, 10, 35),
        WeightedRandomChestContent.of(Items.GOLD_NUGGET, 3, 10, 35),
        WeightedRandomChestContent.of(Items.GOLD_INGOT, 3, 10, 35),
        WeightedRandomChestContent.of(Items.EXPERIENCE_BOTTLE, 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:corn_seed", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 25),
        WeightedRandomChestContent.of(Items.BONE, 4, 10, 35),
        WeightedRandomChestContent.of(Blocks.GOLD_BLOCK, 4, 10, 35),
    };

    /** {@code DamselContentsList} (:72): 9 entries, weight sum 315. */
    public static final WeightedRandomChestContent[] DamselContentsList = {
        WeightedRandomChestContent.of(Items.IRON_PICKAXE, 1, 1, 35),
        WeightedRandomChestContent.of(Items.IRON_SWORD, 1, 1, 35),
        WeightedRandomChestContent.of(Items.COOKED_PORKCHOP, 3, 10, 35),
        WeightedRandomChestContent.of(Items.BEEF, 3, 10, 35),
        WeightedRandomChestContent.of(Items.COOKED_CHICKEN, 3, 10, 35),
        WeightedRandomChestContent.of(Items.COOKED_COD, 3, 10, 35),
        WeightedRandomChestContent.byId("orespawn:blt_sandwich", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:salad", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:corndog_cooked", 4, 10, 35),
    };

    /** {@code EnderCastleContentsList} (:73): 8 entries, weight sum 270. */
    public static final WeightedRandomChestContent[] EnderCastleContentsList = {
        WeightedRandomChestContent.of(Blocks.ENDER_CHEST, 2, 4, 35),
        WeightedRandomChestContent.of(Blocks.DIAMOND_BLOCK, 2, 4, 35),
        WeightedRandomChestContent.of(Blocks.DRAGON_EGG, 1, 1, 35),
        WeightedRandomChestContent.byId("orespawn:blockenderpearl", 3, 6, 35),
        WeightedRandomChestContent.byId("orespawn:blockeyeofender", 3, 6, 35),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 25),
        WeightedRandomChestContent.of(Items.ENDER_PEARL, 2, 4, 35),
        WeightedRandomChestContent.of(Items.ENDER_EYE, 2, 4, 35),
    };

    /** {@code BouncyContentsList} (:74): 7 entries, weight sum 180. */
    public static final WeightedRandomChestContent[] BouncyContentsList = {
        WeightedRandomChestContent.of(Items.ROTTEN_FLESH, 6, 16, 35),
        WeightedRandomChestContent.of(Items.COD, 6, 16, 25),
        WeightedRandomChestContent.of(Items.BONE, 6, 16, 25),
        WeightedRandomChestContent.of(Items.STRING, 6, 16, 25),
        WeightedRandomChestContent.of(Blocks.POPPY, 6, 16, 25),
        WeightedRandomChestContent.of(Blocks.DANDELION, 6, 16, 25),
        WeightedRandomChestContent.of(Items.ENDER_PEARL, 2, 4, 20),
    };

    /** {@code SpitBugContentsList} (:75): 15 entries, weight sum 295. */
    public static final WeightedRandomChestContent[] SpitBugContentsList = {
        WeightedRandomChestContent.of(Items.ROTTEN_FLESH, 6, 16, 35),
        WeightedRandomChestContent.of(Items.COD, 6, 16, 25),
        WeightedRandomChestContent.of(Items.BONE, 6, 16, 25),
        WeightedRandomChestContent.of(Items.STRING, 6, 16, 25),
        WeightedRandomChestContent.byId("orespawn:amethystpickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethystshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethysthoe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethystaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethystsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:instantgarden", 2, 4, 25),
        WeightedRandomChestContent.byId("orespawn:instantshelter", 2, 4, 25),
    };

    /** {@code GraveContentsList} (:76): 4 entries, weight sum 140. */
    public static final WeightedRandomChestContent[] GraveContentsList = {
        WeightedRandomChestContent.of(Items.ENDER_EYE, 6, 16, 35),
        WeightedRandomChestContent.of(Blocks.POPPY, 6, 16, 35),
        WeightedRandomChestContent.of(Blocks.DANDELION, 6, 16, 35),
        WeightedRandomChestContent.of(Items.ENDER_PEARL, 6, 16, 35),
    };

    /** {@code HospitalContentsList} (:77): 6 entries, weight sum 210. */
    public static final WeightedRandomChestContent[] HospitalContentsList = {
        WeightedRandomChestContent.of(Blocks.ENDER_CHEST, 2, 4, 35),
        WeightedRandomChestContent.of(Blocks.DIAMOND_BLOCK, 2, 4, 35),
        WeightedRandomChestContent.of(Blocks.DRAGON_EGG, 1, 1, 35),
        WeightedRandomChestContent.byId("orespawn:blockenderpearl", 3, 6, 35),
        WeightedRandomChestContent.of(Items.ENDER_PEARL, 2, 4, 35),
        WeightedRandomChestContent.of(Items.ENDER_EYE, 2, 4, 35),
    };
}
