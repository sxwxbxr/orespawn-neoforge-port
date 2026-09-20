package com.swbr.orespawn.world.dungeon;

import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * The shared state and private helpers of {@code danger.orespawn.GenericDungeon} (GenericDungeon.java:13-221), the
 * singleton {@code OreSpawnMain.MyDungeon} (OreSpawnMain.java:5444). The builders themselves live in
 * {@code world.dungeon.a}, {@code .b} and {@code .c} (the class was split by line range in W13); all of them call
 * the helpers here by their original names, so a ported body reads like the original.
 *
 * <p>What lives here (verhalten/world-01.md, "Gemeinsame Mechanik" and "Loot-Tabellen"):
 * <ul>
 * <li>the 38 chest lists of the constructor (:58-95), the two picture arrays {@link #king}/{@link #queen} (:96-97)
 * and {@link #blkcolors} (:98);</li>
 * <li>{@link #FastSetBlock} (:219-221), {@link #setThisBlock} (:101-108), {@link #getChestTileEntity} and
 * {@link #getSpawnerTileEntity} (:110-130);</li>
 * <li>{@link #fill_chests} (:770-832), called by {@code addLevelDecorations} (King castle, dungeon-a) and
 * {@code addLevelDecorationsQ} (Queen castle, dungeon-c);</li>
 * <li>the glue every builder needs the same way: {@code world.setBlock} with metadata, spawners by their
 * {@code EntityList} name, chests with metadata, fixed chest slots, double chests.</li>
 * </ul>
 *
 * <p><b>World and random.</b> The world is a {@link StructureWriter} (clipped inside a structure piece or a feature,
 * unclipped at run time from {@code DungeonSpawnerBlock}); {@code world.rand} is the {@link Random} the builder
 * receives.
 *
 * <p><b>Draw rule.</b> PORT: the original drew the mob of a spawner or the draw count of a chest inside
 * {@code if (tileentity != null)}. After a successful {@code world.setBlock} of a spawner or chest that tile entity
 * always exists; the ported builders therefore draw unconditionally, whatever the clip of the current chunk run lets
 * through, so every run of a builder sees the same sequence (DECISIONS R12, R23). On a live level the result is the
 * original's.
 *
 * <p><b>Height.</b> PORT: {@code OreSpawnMain.setBlockFast} and 1.7.10 {@code World.setBlock} rejected y outside 0..255;
 * the writer clips at the dimension's build height instead, the repository rule of {@code FastBlocks.setBlockFast}.
 * Identical in the OreSpawn dimensions.
 */
public final class GenericDungeonHelpers {

    private GenericDungeonHelpers() {
    }

    // ------------------------------------------------------------------------------------------------
    // EntityList names of spawners and entities (verhalten/world-01.md, "Spawner- und Entity-Namen")
    // ------------------------------------------------------------------------------------------------

    /**
     * Legacy {@code EntityList} name to 1.21.1 registry id, from the catalogue table (manifest
     * {@code entities[].name}). {@code Blaze} and {@code Silverfish} are vanilla, {@code EntityEnderCrystal} is
     * {@code minecraft:end_crystal}.
     *
     * <p>Every type resolves since W11. An unknown id would still leave an empty spawner
     * ({@link StructureWriter#setSpawner}, W12 convention), like a name 1.7.10's EntityList did not know.
     */
    private static final Map<String, String> ENTITY_IDS = Map.ofEntries(
            Map.entry("Alien", "orespawn:alien"),
            Map.entry("Alosaurus", "orespawn:alosaurus"),
            Map.entry("Attack Squid", "orespawn:attack_squid"),
            Map.entry("Basilisk", "orespawn:basilisk"),
            Map.entry("Bee", "orespawn:bee"),
            Map.entry("Boyfriend", "orespawn:boyfriend"),
            Map.entry("Brutalfly", "orespawn:brutalfly"),
            Map.entry("Butterfly", "orespawn:butterfly"),
            Map.entry("CaterKiller", "orespawn:cater_killer"),
            Map.entry("CaveFisher", "orespawn:cave_fisher"),
            Map.entry("Cloud Shark", "orespawn:cloud_shark"),
            Map.entry("Criminal", "orespawn:criminal"),
            Map.entry("Cryolophosaurus", "orespawn:cryolophosaurus"),
            Map.entry("Crystal Urchin", "orespawn:crystal_urchin"),
            Map.entry("Dungeon Beast", "orespawn:dungeon_beast"),
            Map.entry("Emperor Scorpion", "orespawn:emperor_scorpion"),
            Map.entry("Ender Knight", "orespawn:ender_knight"),
            Map.entry("Ender Reaper", "orespawn:ender_reaper"),
            Map.entry("Frog", "orespawn:frog"),
            Map.entry("Ghost", "orespawn:ghost"),
            Map.entry("Ghost Pumpkin Skelly", "orespawn:ghost_pumpkin_skelly"),
            Map.entry("Girlfriend", "orespawn:girlfriend"),
            Map.entry("Gold Fish", "orespawn:gold_fish"),
            Map.entry("Hammerhead", "orespawn:hammerhead"),
            Map.entry("Hercules Beetle", "orespawn:hercules_beetle"),
            Map.entry("Jumpy Bug", "orespawn:jumpy_bug"),
            Map.entry("Kyuubi", "orespawn:kyuubi"),
            Map.entry("Large Worm", "orespawn:large_worm"),
            Map.entry("Leaf Monster", "orespawn:leaf_monster"),
            Map.entry("Leonopteryx", "orespawn:leonopteryx"),
            Map.entry("Lurking Terror", "orespawn:lurking_terror"),
            Map.entry("Mantis", "orespawn:mantis"),
            Map.entry("Molenoid", "orespawn:molenoid"),
            Map.entry("Mothra", "orespawn:mothra"),
            Map.entry("Nastysaurus", "orespawn:nastysaurus"),
            Map.entry("Nightmare", "orespawn:nightmare"),
            Map.entry("Rat", "orespawn:rat"),
            Map.entry("Robo-Pounder", "orespawn:robo_pounder"),
            Map.entry("Robo-Sniper", "orespawn:robo_sniper"),
            Map.entry("Robo-Warrior", "orespawn:robo_warrior"),
            Map.entry("Robot Red Ant", "orespawn:robot_red_ant"),
            Map.entry("Robot Spider", "orespawn:robot_spider"),
            Map.entry("Rotator", "orespawn:rotator"),
            Map.entry("Rubber Ducky", "orespawn:rubber_ducky"),
            Map.entry("Scorpion", "orespawn:scorpion"),
            Map.entry("Sea Monster", "orespawn:sea_monster"),
            Map.entry("Sea Viper", "orespawn:sea_viper"),
            Map.entry("Spider Driver", "orespawn:spider_driver"),
            Map.entry("Spit Bug", "orespawn:spit_bug"),
            Map.entry("Stink Bug", "orespawn:stink_bug"),
            Map.entry("Stinky", "orespawn:stinky"),
            Map.entry("T. Rex", "orespawn:t_rex"),
            Map.entry("Terrible Terror", "orespawn:terrible_terror"),
            Map.entry("Triffid", "orespawn:triffid"),
            Map.entry("Vortex", "orespawn:vortex"),
            Map.entry("WTF?", "orespawn:wtf"),
            Map.entry("Water Dragon", "orespawn:water_dragon"),
            Map.entry("Blaze", "minecraft:blaze"),
            Map.entry("Silverfish", "minecraft:silverfish"),
            Map.entry("EntityEnderCrystal", "minecraft:end_crystal"));

    /**
     * The registry id of a legacy {@code EntityList} name.
     *
     * @throws IllegalArgumentException for a name that is not in the catalogue table - a porting mistake, not a world
     *                                  state; the GameTests that run the builders surface it
     */
    public static String entityId(final String legacyName) {
        final String id = ENTITY_IDS.get(legacyName);
        if (id == null) {
            throw new IllegalArgumentException("GenericDungeon: unknown EntityList name " + legacyName);
        }
        return id;
    }

    // ------------------------------------------------------------------------------------------------
    // blocks and items by id
    // ------------------------------------------------------------------------------------------------

    /**
     * A block by registry id ({@code "orespawn:crystalstone"}), air when it is not registered. OreSpawn blocks are
     * looked up by id rather than through holders because part of them (the {@code OreGenericEgg} family) is
     * registered in a loop without fields.
     */
    public static Block block(final String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id)).orElse(Blocks.AIR);
    }

    /**
     * {@code new ItemStack(item, count)} for an item by registry id; empty when the item is not registered (an item of
     * a wave that is not ported yet - the call site carries the {@code PORT: TODO}).
     */
    public static ItemStack stack(final String id, final int count) {
        final Optional<Item> item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id));
        return item.filter(i -> i != Items.AIR).map(i -> new ItemStack(i, count)).orElse(ItemStack.EMPTY);
    }

    /** {@code new ItemStack(item, count)} for a vanilla item. */
    public static ItemStack stack(final ItemLike item, final int count) {
        return new ItemStack(item, count);
    }

    // ------------------------------------------------------------------------------------------------
    // block writes
    // ------------------------------------------------------------------------------------------------

    /**
     * {@code FastSetBlock(world, ix, iy, iz, id)} (:219-221) = {@code OreSpawnMain.setBlockFast(world, ix, iy, iz, id,
     * 0, 2)}: metadata 0 converted by {@link LegacyMeta#state}, flag 2.
     */
    public static void FastSetBlock(final StructureWriter world, final int ix, final int iy, final int iz,
                                    final Block id) {
        if (!world.contains(ix, iy, iz)) {
            return;
        }
        world.setBlock(ix, iy, iz, LegacyMeta.state(id, 0), Block.UPDATE_CLIENTS);
    }

    /** {@link #FastSetBlock(StructureWriter, int, int, int, Block)} with a state the caller already converted. */
    public static void FastSetBlock(final StructureWriter world, final int ix, final int iy, final int iz,
                                    final BlockState state) {
        world.setBlock(ix, iy, iz, state, Block.UPDATE_CLIENTS);
    }

    /** {@code OreSpawnMain.setBlockFast(world, x, y, z, block, meta, flags)}. */
    public static void setBlockFast(final StructureWriter world, final int x, final int y, final int z,
                                    final Block block, final int meta, final int flags) {
        if (!world.contains(x, y, z)) {
            return;
        }
        world.setBlock(x, y, z, block, meta, flags);
    }

    /** 1.7.10 {@code world.setBlock(x, y, z, block)}: metadata 0, flags 3. */
    public static void setBlock(final StructureWriter world, final int x, final int y, final int z, final Block block) {
        setBlock(world, x, y, z, block, 0, Block.UPDATE_ALL);
    }

    /** 1.7.10 {@code world.setBlock(x, y, z, block, meta, flags)}, metadata converted by {@link LegacyMeta#state}. */
    public static void setBlock(final StructureWriter world, final int x, final int y, final int z, final Block block,
                                final int meta, final int flags) {
        if (!world.contains(x, y, z)) {
            return;
        }
        world.setBlock(x, y, z, block, meta, flags);
    }

    /** {@code setThisBlock} (:101-108): {@code world.rand.nextInt(2) == 1} mossy cobblestone, else cobblestone. */
    public static void setThisBlock(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                    final int cposz) {
        if (rand.nextInt(2) == 1) {
            FastSetBlock(world, cposx, cposy, cposz, Blocks.MOSSY_COBBLESTONE);
        } else {
            FastSetBlock(world, cposx, cposy, cposz, Blocks.COBBLESTONE);
        }
    }

    // ------------------------------------------------------------------------------------------------
    // spawners
    // ------------------------------------------------------------------------------------------------

    /**
     * The spawner idiom of every builder: {@code world.setBlock(x, y, z, Blocks.mob_spawner, 0, 2)},
     * {@code getSpawnerTileEntity}, {@code func_145881_a().setEntityName(legacyName)}.
     */
    public static void spawner(final StructureWriter world, final int x, final int y, final int z,
                               final String legacyName) {
        world.setSpawner(x, y, z, entityId(legacyName));
    }

    /** {@code getSpawnerTileEntity} (:121-130): the spawner block entity, {@code null} for any other. */
    @Nullable
    public static SpawnerBlockEntity getSpawnerTileEntity(final StructureWriter world, final int cposx,
                                                          final int cposy, final int cposz) {
        final BlockEntity t = world.getTileEntity(cposx, cposy, cposz);
        return t instanceof SpawnerBlockEntity spawner ? spawner : null;
    }

    // ------------------------------------------------------------------------------------------------
    // chests
    // ------------------------------------------------------------------------------------------------

    /** {@code getChestTileEntity} (:110-119): the chest block entity, {@code null} for any other. */
    @Nullable
    public static ChestBlockEntity getChestTileEntity(final StructureWriter world, final int cposx, final int cposy,
                                                      final int cposz) {
        final BlockEntity t = world.getTileEntity(cposx, cposy, cposz);
        return t instanceof ChestBlockEntity chest ? chest : null;
    }

    /**
     * {@code world.setBlock(x, y, z, Blocks.chest, 0, 2)}, {@code setBlockMetadataWithNotify(x, y, z, meta, 3)} when
     * {@code meta >= 0}, then {@code generateChestContents(world.rand, contents, chest, draws)}. The caller evaluates
     * {@code draws} before the call, as the original evaluated the argument before the fill.
     *
     * @param meta 1.7.10 chest metadata ({@link LegacyMeta#chest}), -1 where the original never set it
     */
    public static void chest(final StructureWriter world, final Random rand, final int x, final int y, final int z,
                             final int meta, final WeightedRandomChestContent[] contents, final int draws) {
        world.setChest(x, y, z, meta, contents, draws, rand);
    }

    /**
     * A chest whose slots the original filled by hand ({@code chest.setInventorySlotContents}): only the block,
     * {@code world.setBlock(chest, 0, flags)} plus {@code setBlockMetadataWithNotify(meta, 3)} when {@code meta >= 0}.
     */
    public static void emptyChest(final StructureWriter world, final int x, final int y, final int z, final int meta,
                                  final int flags) {
        if (!world.contains(x, y, z)) {
            return;
        }
        world.setBlock(x, y, z, LegacyMeta.chest(meta), flags);
    }

    /**
     * {@code chest.setInventorySlotContents(slot, stack)} on the chest at the position: the single place a builder
     * writes into an inventory without the weighted filler. An empty stack (unregistered item) changes nothing.
     */
    public static void setChestSlot(final StructureWriter world, final int x, final int y, final int z, final int slot,
                                    final ItemStack stack) {
        if (stack.isEmpty() || !world.contains(x, y, z)) {
            return;
        }
        if (world.getTileEntity(x, y, z) instanceof Container container && slot < container.getContainerSize()) {
            container.setItem(slot, stack);
        }
    }

    /**
     * Two chests the original set side by side along x without metadata ({@code world.setBlock(chest, 0, 2)} twice),
     * which 1.7.10 joined into one double chest when the second was added. PORT: 1.21.1 joins two chests only when one
     * of them already has a {@code type}; both halves are written with their type, facing south (the
     * {@link LegacyMeta#chest} default for "never set"): the west one {@code RIGHT}, the east one {@code LEFT}
     * ({@code ChestBlock.getConnectedDirection}: a south-facing {@code LEFT} chest connects to the west). Only the
     * west half is filled, as in the original (verhalten/world-01.md, Portrisiken 6).
     */
    public static void doubleChestAlongX(final StructureWriter world, final int westX, final int y, final int z) {
        final BlockState base = LegacyMeta.chest(-1);
        if (world.contains(westX, y, z)) {
            world.setBlock(westX, y, z, base.setValue(ChestBlock.TYPE, ChestType.RIGHT), Block.UPDATE_CLIENTS);
        }
        if (world.contains(westX + 1, y, z)) {
            world.setBlock(westX + 1, y, z, base.setValue(ChestBlock.TYPE, ChestType.LEFT), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * {@code WeightedRandomChestContent.generateChestContents(world.rand, contents, chest, draws)} on a chest that
     * already stands at the position.
     */
    public static void generateChestContents(final StructureWriter world, final Random rand,
                                             final WeightedRandomChestContent[] contents, final int x, final int y,
                                             final int z, final int draws) {
        world.fillChest(x, y, z, contents, draws, rand);
    }

    /**
     * {@code fill_chests} (:770-832): four chests inside a Challenge Dungeon level at (1, 1, w/2) meta 5, (w-2, 1, w/2)
     * meta 4, (w/2, 1, 1) meta 3 and (w/2, 1, w-2) meta 2, each with {@code 5 + nextInt(7)} draws from the list of the
     * reward level; reward 6 puts the Prince egg and the Royal set into fixed slots instead. Shared by
     * {@code addLevelDecorations} and {@code addLevelDecorationsQ} (decor 2-6 of the Queen castle).
     */
    public static void fill_chests(final StructureWriter world, final Random rand, final int cposx, final int cposy,
                                   final int cposz, final int width, final int height, final int decor,
                                   final int reward) {
        WeightedRandomChestContent[] chestContents = level1ContentsList;
        if (reward == 2) {
            chestContents = level2ContentsList;
        }
        if (reward == 3) {
            chestContents = level3ContentsList;
        }
        if (reward == 4) {
            chestContents = level4ContentsList;
        }
        if (reward == 5) {
            chestContents = level5ContentsList;
        }
        // :786-796
        rewardChest(world, rand, cposx + 1, cposy + 1, cposz + width / 2, 5, chestContents, reward,
                stack("orespawn:eggtheprince", 1), ItemStack.EMPTY);
        // :797-808
        rewardChest(world, rand, cposx + width - 2, cposy + 1, cposz + width / 2, 4, chestContents, reward,
                stack("orespawn:royal_helmet", 1), stack("orespawn:royal_chest", 1));
        // :809-820
        rewardChest(world, rand, cposx + width / 2, cposy + 1, cposz + 1, 3, chestContents, reward,
                stack("orespawn:royal_leggings", 1), stack("orespawn:royal_boots", 1));
        // :821-831
        rewardChest(world, rand, cposx + width / 2, cposy + 1, cposz + width - 2, 2, chestContents, reward,
                stack("orespawn:royalsmall", 1), ItemStack.EMPTY);
    }

    /** One chest of {@link #fill_chests}: reward 6 fills slots 1 and 2, any other reward draws {@code 5 + nextInt(7)}. */
    private static void rewardChest(final StructureWriter world, final Random rand, final int x, final int y,
                                    final int z, final int meta, final WeightedRandomChestContent[] chestContents,
                                    final int reward, final ItemStack slot1, final ItemStack slot2) {
        if (reward == 6) {
            emptyChest(world, x, y, z, meta, Block.UPDATE_CLIENTS);
            setChestSlot(world, x, y, z, 1, slot1);
            setChestSlot(world, x, y, z, 2, slot2);
        } else {
            chest(world, rand, x, y, z, meta, chestContents, 5 + rand.nextInt(7));
        }
    }

    // ------------------------------------------------------------------------------------------------
    // pictures (:96-98)
    // ------------------------------------------------------------------------------------------------

    /** {@code king} (:96): the run-length picture of the King altar. */
    public static final int[] king = {
        -1, -1, 24, 3, -1, 24, 5, -1, 17, 12, -1, 16, 15, -1, 15, 14, -1, 15, 6, 3, 5, -1, 14, 6, 4, 3, -1, 14, 5,
        -1, 14, 5, -1, 12, 9, -1, 11, 11, -1, 8, 17, -1, 5, 23, -1, 3, 27, -1, 2, 29, -1, 1, 31, -1, 0, 33, -1, 13,
        6, -1, 12, 9, -1, 11, 3, 1, 2, 1, 4, -1, 10, 3, 2, 2, 3, 2, -1, 10, 2, 4, 2, 3, 2, -1, 9, 2, 5, 2, 4, 6, -1,
        9, 2, 5, 2, 6, 4, -1, 8, 2, 6, 1, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 15, 2, -1, -1, -1
    };

    /** {@code queen} (:97): the picture of the Queen altar; the original holds the same numbers as {@link #king}. */
    public static final int[] queen = {
        -1, -1, 24, 3, -1, 24, 5, -1, 17, 12, -1, 16, 15, -1, 15, 14, -1, 15, 6, 3, 5, -1, 14, 6, 4, 3, -1, 14, 5,
        -1, 14, 5, -1, 12, 9, -1, 11, 11, -1, 8, 17, -1, 5, 23, -1, 3, 27, -1, 2, 29, -1, 1, 31, -1, 0, 33, -1, 13,
        6, -1, 12, 9, -1, 11, 3, 1, 2, 1, 4, -1, 10, 3, 2, 2, 3, 2, -1, 10, 2, 4, 2, 3, 2, -1, 9, 2, 5, 2, 4, 6, -1,
        9, 2, 5, 2, 6, 4, -1, 8, 2, 6, 1, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 8, 2, 5, 2, -1, 15, 2, -1, -1, -1
    };

    /** {@code blkcolors} (:98): {@code stained_hardened_clay} metadata of the altar pictures. */
    public static final int[] blkcolors = {
        14, 1, 4, 5, 3, 11, 10, 6
    };

    // ------------------------------------------------------------------------------------------------
    // chest lists (:58-95). Generated from the source and checked entry by entry against the catalogue
    // (verhalten/world-01.md, "Loot-Tabellen"): order, min, max, weight; OreSpawn fields through manifest
    // "field", vanilla items flattened (dye@0 ink_sac, fish cod, cooked_fished cooked_cod, log oak_log, leaves
    // oak_leaves, sapling oak_sapling, planks oak_planks, red_flower poppy, yellow_flower dandelion,
    // wooden_button oak_button, spawn_egg@61 blaze_spawn_egg). Entries by id, resolved at fill time.
    // ------------------------------------------------------------------------------------------------


    /** {@code RainbowContentsList} (GenericDungeon.java:58): 6 entries, weight sum 150. */
    public static final WeightedRandomChestContent[] RainbowContentsList = {
        WeightedRandomChestContent.byId("orespawn:magicapple", 1, 1, 25),
        WeightedRandomChestContent.byId("orespawn:eggcloudshark", 4, 10, 25),
        WeightedRandomChestContent.byId("minecraft:bone", 2, 16, 25),
        WeightedRandomChestContent.byId("minecraft:string", 2, 16, 25),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 3, 10, 25),
        WeightedRandomChestContent.byId("minecraft:experience_bottle", 4, 10, 25),
    };

    /** {@code WhiteHouseContentsList} (GenericDungeon.java:59): 11 entries, weight sum 325. */
    public static final WeightedRandomChestContent[] WhiteHouseContentsList = {
        WeightedRandomChestContent.byId("orespawn:corndog_cooked", 6, 12, 35),
        WeightedRandomChestContent.byId("orespawn:uranium_nugget", 2, 6, 10),
        WeightedRandomChestContent.byId("orespawn:titanium_nugget", 2, 6, 10),
        WeightedRandomChestContent.byId("orespawn:amethyst", 2, 6, 35),
        WeightedRandomChestContent.byId("orespawn:ruby", 2, 6, 25),
        WeightedRandomChestContent.byId("orespawn:eggcriminal", 4, 10, 35),
        WeightedRandomChestContent.byId("minecraft:emerald", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:porkchop", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:cooked_porkchop", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:diamond", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:gold_ingot", 6, 16, 35),
    };

    /** {@code RubberDuckyContentsList} (GenericDungeon.java:60): 13 entries, weight sum 455. */
    public static final WeightedRandomChestContent[] RubberDuckyContentsList = {
        WeightedRandomChestContent.byId("orespawn:deadstinkbug", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:firefish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:sunfish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:sparkfish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:greenfish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:bluefish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:pinkfish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:rockfish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:woodfish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:greyfish", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:eggrubberducky", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:peacockfeather", 4, 10, 35),
        WeightedRandomChestContent.byId("minecraft:feather", 6, 16, 35),
    };

    /** {@code StinkyHouseContentsList} (GenericDungeon.java:61): 7 entries, weight sum 215. */
    public static final WeightedRandomChestContent[] StinkyHouseContentsList = {
        WeightedRandomChestContent.byId("orespawn:deadstinkbug", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:eggstinky", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:eggstink", 4, 10, 35),
        WeightedRandomChestContent.byId("minecraft:bone", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:coal", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:string", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 3, 10, 35),
    };

    /** {@code NightmareRookeryContentsList} (GenericDungeon.java:62): 10 entries, weight sum 270. */
    public static final WeightedRandomChestContent[] NightmareRookeryContentsList = {
        WeightedRandomChestContent.byId("orespawn:deadstinkbug", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:flower_black", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:flower_scary", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:eggnightmare", 4, 10, 25),
        WeightedRandomChestContent.byId("orespawn:antrobotkit", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:spiderrobotkit", 1, 1, 10),
        WeightedRandomChestContent.byId("minecraft:bone", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:string", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:experience_bottle", 4, 10, 35),
    };

    /** {@code MonsterIslandContentsList} (GenericDungeon.java:63): 14 entries, weight sum 450. */
    public static final WeightedRandomChestContent[] MonsterIslandContentsList = {
        WeightedRandomChestContent.byId("orespawn:creeperrepellent", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:krakenrepellent", 4, 10, 35),
        WeightedRandomChestContent.byId("minecraft:ink_sac", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:bone", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:string", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:porkchop", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:beef", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:chicken", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:cod", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:experience_bottle", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:bacon", 6, 16, 35),
        WeightedRandomChestContent.byId("orespawn:rawpeacock", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:oak_log", 6, 16, 25),
    };

    /** {@code GreenhouseContentsList} (GenericDungeon.java:64): 7 entries, weight sum 215. */
    public static final WeightedRandomChestContent[] GreenhouseContentsList = {
        WeightedRandomChestContent.byId("orespawn:greengoo", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:creeperrepellent", 4, 10, 35),
        WeightedRandomChestContent.byId("minecraft:flower_pot", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:oak_sapling", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:oak_leaves", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:dirt", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:oak_log", 6, 16, 25),
    };

    /** {@code CrystalBattleTowerRatContentsList} (GenericDungeon.java:65): 7 entries, weight sum 245. */
    public static final WeightedRandomChestContent[] CrystalBattleTowerRatContentsList = {
        WeightedRandomChestContent.byId("minecraft:cooked_porkchop", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:beef", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:cooked_chicken", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:cooked_cod", 3, 10, 35),
        WeightedRandomChestContent.byId("orespawn:blt_sandwich", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:salad", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:corndog_cooked", 4, 10, 35),
    };

    /** {@code CrystalBattleTowerDungeonBeastContentsList} (GenericDungeon.java:66): 4 entries, weight sum 90. */
    public static final WeightedRandomChestContent[] CrystalBattleTowerDungeonBeastContentsList = {
        WeightedRandomChestContent.byId("minecraft:ink_sac", 6, 16, 25),
        WeightedRandomChestContent.byId("orespawn:squidzookasmall", 1, 1, 25),
        WeightedRandomChestContent.byId("minecraft:gold_nugget", 5, 15, 15),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
    };

    /** {@code CrystalBattleTowerUrchinContentsList} (GenericDungeon.java:67): 5 entries, weight sum 55. */
    public static final WeightedRandomChestContent[] CrystalBattleTowerUrchinContentsList = {
        WeightedRandomChestContent.byId("orespawn:pink_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:fairysword", 1, 1, 15),
    };

    /** {@code CrystalBattleTowerRotatorContentsList} (GenericDungeon.java:68): 5 entries, weight sum 55. */
    public static final WeightedRandomChestContent[] CrystalBattleTowerRotatorContentsList = {
        WeightedRandomChestContent.byId("orespawn:tigerseye_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:ratsword", 1, 1, 15),
    };

    /** {@code CrystalBattleTowerVortexContentsList} (GenericDungeon.java:69): 5 entries, weight sum 60. */
    public static final WeightedRandomChestContent[] CrystalBattleTowerVortexContentsList = {
        WeightedRandomChestContent.byId("orespawn:crystalcoal", 6, 10, 10),
        WeightedRandomChestContent.byId("orespawn:crystalcoal", 6, 10, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_sword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_block", 4, 8, 15),
        WeightedRandomChestContent.byId("orespawn:poisonsword", 1, 1, 15),
    };

    /** {@code RobotContentsList} (GenericDungeon.java:70): 23 entries, weight sum 755. */
    public static final WeightedRandomChestContent[] RobotContentsList = {
        WeightedRandomChestContent.byId("minecraft:redstone", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:repeater", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:minecart", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:fire_charge", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:hopper_minecart", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:redstone_block", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:rail", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:detector_rail", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:sticky_piston", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:piston", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:redstone_torch", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:tnt", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:rail", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:lever", 1, 10, 35),
        WeightedRandomChestContent.byId("orespawn:antrobotkit", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:spiderrobotkit", 1, 1, 10),
        WeightedRandomChestContent.byId("minecraft:iron_door", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:redstone_torch", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:oak_button", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:iron_bars", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:comparator", 1, 10, 35),
        WeightedRandomChestContent.byId("minecraft:activator_rail", 1, 10, 35),
        WeightedRandomChestContent.byId("orespawn:raygun", 1, 1, 35),
    };

    /** {@code IncaPyramidContentsList} (GenericDungeon.java:71): 14 entries, weight sum 480. */
    public static final WeightedRandomChestContent[] IncaPyramidContentsList = {
        WeightedRandomChestContent.byId("minecraft:golden_sword", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:golden_boots", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:golden_leggings", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:golden_helmet", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:golden_chestplate", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:dandelion", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:poppy", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:gold_nugget", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:gold_ingot", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:experience_bottle", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:corn_seed", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 25),
        WeightedRandomChestContent.byId("minecraft:bone", 4, 10, 35),
        WeightedRandomChestContent.byId("minecraft:gold_block", 4, 10, 35),
    };

    /** {@code DamselContentsList} (GenericDungeon.java:72): 9 entries, weight sum 315. */
    public static final WeightedRandomChestContent[] DamselContentsList = {
        WeightedRandomChestContent.byId("minecraft:iron_pickaxe", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:iron_sword", 1, 1, 35),
        WeightedRandomChestContent.byId("minecraft:cooked_porkchop", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:beef", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:cooked_chicken", 3, 10, 35),
        WeightedRandomChestContent.byId("minecraft:cooked_cod", 3, 10, 35),
        WeightedRandomChestContent.byId("orespawn:blt_sandwich", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:salad", 4, 10, 35),
        WeightedRandomChestContent.byId("orespawn:corndog_cooked", 4, 10, 35),
    };

    /** {@code EnderCastleContentsList} (GenericDungeon.java:73): 8 entries, weight sum 270. */
    public static final WeightedRandomChestContent[] EnderCastleContentsList = {
        WeightedRandomChestContent.byId("minecraft:ender_chest", 2, 4, 35),
        WeightedRandomChestContent.byId("minecraft:diamond_block", 2, 4, 35),
        WeightedRandomChestContent.byId("minecraft:dragon_egg", 1, 1, 35),
        WeightedRandomChestContent.byId("orespawn:blockenderpearl", 3, 6, 35),
        WeightedRandomChestContent.byId("orespawn:blockeyeofender", 3, 6, 35),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 25),
        WeightedRandomChestContent.byId("minecraft:ender_pearl", 2, 4, 35),
        WeightedRandomChestContent.byId("minecraft:ender_eye", 2, 4, 35),
    };

    /** {@code BouncyContentsList} (GenericDungeon.java:74): 7 entries, weight sum 180. */
    public static final WeightedRandomChestContent[] BouncyContentsList = {
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:cod", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:bone", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:string", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:poppy", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:dandelion", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:ender_pearl", 2, 4, 20),
    };

    /** {@code SpitBugContentsList} (GenericDungeon.java:75): 15 entries, weight sum 295. */
    public static final WeightedRandomChestContent[] SpitBugContentsList = {
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:cod", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:bone", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:string", 6, 16, 25),
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

    /** {@code GraveContentsList} (GenericDungeon.java:76): 4 entries, weight sum 140. */
    public static final WeightedRandomChestContent[] GraveContentsList = {
        WeightedRandomChestContent.byId("minecraft:ender_eye", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:poppy", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:dandelion", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:ender_pearl", 6, 16, 35),
    };

    /** {@code HospitalContentsList} (GenericDungeon.java:77): 6 entries, weight sum 210. */
    public static final WeightedRandomChestContent[] HospitalContentsList = {
        WeightedRandomChestContent.byId("minecraft:ender_chest", 2, 4, 35),
        WeightedRandomChestContent.byId("minecraft:diamond_block", 2, 4, 35),
        WeightedRandomChestContent.byId("minecraft:dragon_egg", 1, 1, 35),
        WeightedRandomChestContent.byId("orespawn:blockenderpearl", 3, 6, 35),
        WeightedRandomChestContent.byId("minecraft:ender_pearl", 2, 4, 35),
        WeightedRandomChestContent.byId("minecraft:ender_eye", 2, 4, 35),
    };

    /** {@code MiniContentsList} (GenericDungeon.java:78): 6 entries, weight sum 190. */
    public static final WeightedRandomChestContent[] MiniContentsList = {
        WeightedRandomChestContent.byId("minecraft:golden_apple", 6, 16, 35),
        WeightedRandomChestContent.byId("orespawn:crystalapple", 6, 16, 35),
        WeightedRandomChestContent.byId("orespawn:cookedbacon", 6, 16, 35),
        WeightedRandomChestContent.byId("orespawn:firefish", 6, 16, 35),
        WeightedRandomChestContent.byId("orespawn:instantgarden", 2, 4, 25),
        WeightedRandomChestContent.byId("orespawn:instantshelter", 2, 4, 25),
    };

    /** {@code LeafMonsterContentsList} (GenericDungeon.java:79): 9 entries, weight sum 255. */
    public static final WeightedRandomChestContent[] LeafMonsterContentsList = {
        WeightedRandomChestContent.byId("minecraft:flower_pot", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:oak_sapling", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:flower_pot", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:oak_sapling", 6, 16, 35),
        WeightedRandomChestContent.byId("minecraft:oak_leaves", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:dirt", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:oak_log", 6, 16, 25),
        WeightedRandomChestContent.byId("orespawn:poisonsword", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
    };

    /** {@code CloudSharkContentsList} (GenericDungeon.java:80): 6 entries, weight sum 140. */
    public static final WeightedRandomChestContent[] CloudSharkContentsList = {
        WeightedRandomChestContent.byId("minecraft:cod", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:bone", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:string", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:paper", 6, 16, 25),
        WeightedRandomChestContent.byId("orespawn:experiencetree_seed", 1, 2, 15),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
    };

    /** {@code WaterDragonContentsList} (GenericDungeon.java:81): 7 entries, weight sum 145. */
    public static final WeightedRandomChestContent[] WaterDragonContentsList = {
        WeightedRandomChestContent.byId("minecraft:cod", 6, 16, 25),
        WeightedRandomChestContent.byId("orespawn:ultimateaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimatepickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ultimateshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 25),
        WeightedRandomChestContent.byId("minecraft:iron_block", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
    };

    /** {@code SquidContentsList} (GenericDungeon.java:82): 4 entries, weight sum 80. */
    public static final WeightedRandomChestContent[] SquidContentsList = {
        WeightedRandomChestContent.byId("minecraft:ink_sac", 6, 16, 25),
        WeightedRandomChestContent.byId("orespawn:squidzookasmall", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:gold_nugget", 5, 15, 15),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
    };

    /** {@code KnightContentsList} (GenericDungeon.java:83): 5 entries, weight sum 95. */
    public static final WeightedRandomChestContent[] KnightContentsList = {
        WeightedRandomChestContent.byId("minecraft:paper", 2, 8, 20),
        WeightedRandomChestContent.byId("minecraft:oak_planks", 4, 8, 20),
        WeightedRandomChestContent.byId("minecraft:ender_eye", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:ender_pearl", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
    };

    /** {@code AlienWTFContentsList} (GenericDungeon.java:84): 18 entries, weight sum 255. */
    public static final WeightedRandomChestContent[] AlienWTFContentsList = {
        WeightedRandomChestContent.byId("minecraft:diamond_block", 1, 2, 15),
        WeightedRandomChestContent.byId("orespawn:ruby", 1, 1, 20),
        WeightedRandomChestContent.byId("orespawn:amethyst", 1, 1, 20),
        WeightedRandomChestContent.byId("orespawn:ingoturanium", 1, 2, 5),
        WeightedRandomChestContent.byId("orespawn:ingottitanium", 1, 2, 5),
        WeightedRandomChestContent.byId("orespawn:ultimate_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:ultimate_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:ultimate_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:ultimate_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:ultimatebow", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:nightmaresword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 15),
        WeightedRandomChestContent.byId("orespawn:raygun", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:cageempty", 1, 10, 20),
        WeightedRandomChestContent.byId("orespawn:corndog_cooked", 1, 10, 20),
        WeightedRandomChestContent.byId("orespawn:cookedbacon", 1, 5, 20),
        WeightedRandomChestContent.byId("orespawn:popcorn_bag", 2, 8, 20),
        WeightedRandomChestContent.byId("orespawn:firefish", 2, 8, 15),
    };

    /** {@code shadowContentsList} (GenericDungeon.java:85): 22 entries, weight sum 320. */
    public static final WeightedRandomChestContent[] shadowContentsList = {
        WeightedRandomChestContent.byId("minecraft:glowstone_dust", 2, 8, 20),
        WeightedRandomChestContent.byId("minecraft:nether_wart", 4, 8, 20),
        WeightedRandomChestContent.byId("minecraft:blaze_rod", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:blaze_powder", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:fire_charge", 4, 8, 15),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:ink_sac", 6, 16, 25),
        WeightedRandomChestContent.byId("orespawn:ruby", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:experiencetree_seed", 2, 4, 15),
        WeightedRandomChestContent.byId("orespawn:elevator", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:nightmaresword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:poisonsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ratsword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:rubysword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:bighammer", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:squidzookasmall", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ingottitanium", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:ingoturanium", 1, 1, 5),
        WeightedRandomChestContent.byId("orespawn:ultimatesword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:ultimatebow", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:eggenderreaper", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:eggnightmare", 2, 8, 15),
    };

    /** {@code kyuubiContentsList} (GenericDungeon.java:86): 7 entries, weight sum 110. */
    public static final WeightedRandomChestContent[] kyuubiContentsList = {
        WeightedRandomChestContent.byId("minecraft:redstone", 2, 8, 10),
        WeightedRandomChestContent.byId("minecraft:redstone_block", 4, 8, 15),
        WeightedRandomChestContent.byId("minecraft:quartz", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:coal", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:nightmaresword", 1, 1, 20),
        WeightedRandomChestContent.byId("orespawn:poisonsword", 1, 1, 20),
        WeightedRandomChestContent.byId("orespawn:eggkyuubi", 2, 8, 15),
    };

    /** {@code blazeContentsList} (GenericDungeon.java:87): 9 entries, weight sum 130. */
    public static final WeightedRandomChestContent[] blazeContentsList = {
        WeightedRandomChestContent.byId("minecraft:blaze_rod", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:blaze_powder", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:fire_charge", 4, 8, 15),
        WeightedRandomChestContent.byId("minecraft:flint_and_steel", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:lavaeel_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("minecraft:blaze_spawn_egg", 2, 8, 15), // Items.spawn_egg meta 61
    };

    /** {@code beeContentsList} (GenericDungeon.java:88): 12 entries, weight sum 150. */
    public static final WeightedRandomChestContent[] beeContentsList = {
        WeightedRandomChestContent.byId("minecraft:sugar", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:dandelion", 4, 8, 15),
        WeightedRandomChestContent.byId("minecraft:gold_nugget", 5, 15, 15),
        WeightedRandomChestContent.byId("minecraft:paper", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:fairysword", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:buttercandy", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 10),
        WeightedRandomChestContent.byId("orespawn:eggbee", 2, 8, 15),
    };

    /** {@code mantisContentsList} (GenericDungeon.java:89): 11 entries, weight sum 135. */
    public static final WeightedRandomChestContent[] mantisContentsList = {
        WeightedRandomChestContent.byId("orespawn:mantisclaw", 1, 1, 10),
        WeightedRandomChestContent.byId("minecraft:gold_nugget", 4, 8, 15),
        WeightedRandomChestContent.byId("orespawn:uranium_nugget", 1, 3, 5),
        WeightedRandomChestContent.byId("orespawn:titanium_nugget", 1, 3, 5),
        WeightedRandomChestContent.byId("orespawn:eggmantis", 2, 4, 20),
        WeightedRandomChestContent.byId("orespawn:tigerseye_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("minecraft:rotten_flesh", 6, 16, 25),
        WeightedRandomChestContent.byId("minecraft:diamond", 1, 3, 15),
    };

    /** {@code level1ContentsList} (GenericDungeon.java:90): 11 entries, weight sum 165. */
    public static final WeightedRandomChestContent[] level1ContentsList = {
        WeightedRandomChestContent.byId("minecraft:emerald", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:minersdream", 4, 8, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldpickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldhoe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_boots", 1, 1, 15),
    };

    /** {@code level2ContentsList} (GenericDungeon.java:91): 17 entries, weight sum 235. */
    public static final WeightedRandomChestContent[] level2ContentsList = {
        WeightedRandomChestContent.byId("minecraft:experience_bottle", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:experience_bottle", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:creeperlauncher", 2, 10, 15),
        WeightedRandomChestContent.byId("orespawn:pink_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:pink_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:fairysword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldpickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldhoe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_boots", 1, 1, 15),
    };

    /** {@code level3ContentsList} (GenericDungeon.java:92): 17 entries, weight sum 235. */
    public static final WeightedRandomChestContent[] level3ContentsList = {
        WeightedRandomChestContent.byId("orespawn:squidzookasmall", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ratsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst", 2, 8, 15),
        WeightedRandomChestContent.byId("minecraft:ink_sac", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:tigerseye_helmet", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_chest", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_leggings", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:tigerseye_boots", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:amethystpickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethystshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethysthoe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethystaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethystsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:amethyst_boots", 1, 1, 15),
    };

    /** {@code level4ContentsList} (GenericDungeon.java:93): 17 entries, weight sum 255. */
    public static final WeightedRandomChestContent[] level4ContentsList = {
        WeightedRandomChestContent.byId("orespawn:ruby", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:magicapple", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:raygun", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:creeperrepellent", 4, 10, 15),
        WeightedRandomChestContent.byId("orespawn:krakenrepellent", 4, 10, 15),
        WeightedRandomChestContent.byId("orespawn:experiencecatcher", 4, 10, 15),
        WeightedRandomChestContent.byId("orespawn:zookeeper", 10, 16, 15),
        WeightedRandomChestContent.byId("orespawn:rubypickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubyshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubyhoe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubyaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:rubysword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:thunderstaff", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:ruby_boots", 1, 1, 15),
    };

    /** {@code level5ContentsList} (GenericDungeon.java:94): 87 entries, weight sum 1285. */
    public static final WeightedRandomChestContent[] level5ContentsList = {
        WeightedRandomChestContent.byId("orespawn:nightmaresword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:poisonsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:eggwitherskeleton", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggenderdragon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggsnowgolem", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggirongolem", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggwitherboss", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggredcow", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egggoldcow", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggenchantedcow", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggmothra", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggalosaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcryolophosaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcamarasaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggvelocityraptor", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egghydrolisc", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbasilisc", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggdragonfly", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggemperorscorpion", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggscorpion", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcavefisher", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggspyro", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbaryonyx", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcockateil", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egggammametroid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggkyuubi", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggalien", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggattacksquid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggwaterdragon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcephadrome", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggkraken", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egglizard", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggdragon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbee", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggtrooper", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggspit", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggstink", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggostrich", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egggazelle", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggchipmunk", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcreepinghorror", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggterribleterror", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcliffracer", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggtriffid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggnightmare", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egglurkingterror", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggsmallworm", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggmediumworm", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egglargeworm", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggtrex", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egggodzilla", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggmantis", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egghercules", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggvortex", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrat", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggdungeonbeast", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggfairy", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggwhale", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggskate", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggirukandji", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrobot1", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrobot2", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrobot3", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrobot4", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrobot5", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcriminal", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcoin", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggboyfriend", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggeasterbunny", 1, 4, 5),
        WeightedRandomChestContent.byId("orespawn:eggmolenoid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggseamonster", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggseaviper", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcaterkiller", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggleon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egghammerhead", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrubberducky", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggnastysaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggpointysaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbrutalfly", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcricket", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggfrog", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:antrobotkit", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:spiderrobotkit", 1, 1, 10),
        WeightedRandomChestContent.byId("orespawn:eggrobot6", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggspiderdriver", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcrab", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcassowary", 1, 4, 15),
    };

    /** {@code chestContentsList} (GenericDungeon.java:95): 91 entries, weight sum 1380. */
    public static final WeightedRandomChestContent[] chestContentsList = {
        WeightedRandomChestContent.byId("orespawn:cookedbacon", 6, 12, 20),
        WeightedRandomChestContent.byId("orespawn:buttercandy", 6, 12, 20),
        WeightedRandomChestContent.byId("minecraft:emerald", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldpickaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldshovel", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldhoe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldaxe", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emeraldsword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:emerald_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:mothscale", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:mothscale_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:mothscale_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:mothscale_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:mothscale_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel", 2, 8, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:lavaeel_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_chest", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_leggings", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_helmet", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experience_boots", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:experiencesword", 1, 1, 15),
        WeightedRandomChestContent.byId("orespawn:eggwitherskeleton", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggenderdragon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggsnowgolem", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggirongolem", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggwitherboss", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggredcow", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egggoldcow", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggenchantedcow", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggmothra", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggalosaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcryolophosaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcamarasaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggvelocityraptor", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egghydrolisc", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbasilisc", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggdragonfly", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggemperorscorpion", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggscorpion", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcavefisher", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggspyro", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbaryonyx", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcockateil", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egggammametroid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggkyuubi", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggalien", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggattacksquid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggwaterdragon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcephadrome", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggkraken", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egglizard", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggdragon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbee", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggtrooper", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggspit", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggstink", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggostrich", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egggazelle", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggchipmunk", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcreepinghorror", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggterribleterror", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcliffracer", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggtriffid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggnightmare", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egglurkingterror", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggsmallworm", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggmediumworm", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egglargeworm", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcassowary", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggmolenoid", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggseamonster", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggseaviper", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcaterkiller", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggleon", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:egghammerhead", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrubberducky", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggnastysaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggpointysaurus", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggbrutalfly", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcricket", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggfrog", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggrobot6", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggspiderdriver", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:eggcrab", 1, 4, 15),
        WeightedRandomChestContent.byId("orespawn:cageempty", 3, 10, 20),
    };
}
