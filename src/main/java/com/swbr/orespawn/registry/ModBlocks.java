package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.crop.BlockCorn;
import com.swbr.orespawn.block.crop.BlockDuctTape;
import com.swbr.orespawn.block.crop.BlockLettuce;
import com.swbr.orespawn.block.crop.BlockPizza;
import com.swbr.orespawn.block.crop.BlockQuinoa;
import com.swbr.orespawn.block.crop.BlockRadish;
import com.swbr.orespawn.block.crop.BlockRice;
import com.swbr.orespawn.block.crop.BlockStrawberry;
import com.swbr.orespawn.block.crop.BlockTomato;
import com.swbr.orespawn.block.misc.BlockCrystal;
import com.swbr.orespawn.block.misc.BlockRuby;
import com.swbr.orespawn.block.misc.BlockTitanium;
import com.swbr.orespawn.block.misc.BlockUranium;
import com.swbr.orespawn.block.misc.Lavafoam;
import com.swbr.orespawn.block.misc.MoleDirtBlock;
import com.swbr.orespawn.block.misc.RTPBlock;
import com.swbr.orespawn.block.insectplant.BlockButterflyPlant;
import com.swbr.orespawn.block.insectplant.BlockFireflyPlant;
import com.swbr.orespawn.block.insectplant.BlockMosquitoPlant;
import com.swbr.orespawn.block.insectplant.BlockMothPlant;
import com.swbr.orespawn.block.nest.AntBlock;
import com.swbr.orespawn.block.nest.CrystalAntBlock;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.block.ore.OreAmethyst;
import com.swbr.orespawn.block.ore.OreBasicStone;
import com.swbr.orespawn.block.ore.OreCrystal;
import com.swbr.orespawn.block.ore.OreCrystalCrystal;
import com.swbr.orespawn.block.ore.OreGenericEgg;
import com.swbr.orespawn.block.ore.OreRuby;
import com.swbr.orespawn.block.ore.OreSalt;
import com.swbr.orespawn.block.ore.OreTitanium;
import com.swbr.orespawn.block.ore.OreUranium;
import com.swbr.orespawn.block.plant.BlockCrystalPlant;
import com.swbr.orespawn.block.plant.BlockExperiencePlant;
import com.swbr.orespawn.block.plant.CrystalGrass;
import com.swbr.orespawn.block.plant.MyBlockFlower;
import com.swbr.orespawn.block.torch.BlockCrystalTorch;
import com.swbr.orespawn.block.torch.BlockCrystalWallTorch;
import com.swbr.orespawn.block.torch.BlockExtremeTorch;
import com.swbr.orespawn.block.torch.BlockExtremeWallTorch;
import com.swbr.orespawn.block.tree.BlockAppleLeaves;
import com.swbr.orespawn.block.tree.BlockCrystalLeaves;
import com.swbr.orespawn.block.tree.BlockCrystalTreeLog;
import com.swbr.orespawn.block.tree.BlockDuplicatorLog;
import com.swbr.orespawn.block.tree.BlockExperienceLeaves;
import com.swbr.orespawn.block.tree.BlockScaryLeaves;
import com.swbr.orespawn.block.tree.BlockSkyTreeLog;
import com.swbr.orespawn.block.tree.CrystalWood;
import com.swbr.orespawn.block.workshop.CrystalFurnace;
import com.swbr.orespawn.block.workshop.CrystalWorkbench;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Blocks. 211 in the original (OreSpawnMain.java:1640-1852, manifest {@code counts.blocks}),
 * registered here wave by wave; the ids are the manifest's ({@code legacy_registry -> id},
 * DECISIONS R2).
 *
 * <p>Order of the holders = legacy block id ({@code BaseBlockID + n}, manifest {@code ctor_args[0]}),
 * which is the order the 1.7.10 creative tabs listed the blocks in. The block items and their tab
 * entries live in {@link ModItems}; this class never touches {@code ModItems}, so the two static
 * initialisers cannot form a cycle whichever is loaded first.
 *
 * <p>Two ids need care when they come: {@code crystalfurnace} is one block with {@code LIT} instead
 * of the original pair (W03), and the torches have a {@code _wall} twin (R2, R18) - done for the
 * two W02 torches below.
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreSpawn.MOD_ID);

    // ---- W02 (w02-ores): ores, storage blocks, misc blocks and the 121 OreGenericEgg blocks.
    // Every one of them was CreativeTabs.tabBlock. The original numbers live in each class's
    // originalProperties() next to their source lines.

    /**
     * The 119 Ancient Dried Spawn Egg blocks (OreSpawnMain.java:5908-6028), keyed by id, in legacy-id
     * order (0-96, then 119, 122, 125, 250-261, 300-306). Must stay above the named holders: static
     * initialisers run in textual order and legacy ids 0-96 precede the ores at 100+.
     */
    public static final Map<String, DeferredBlock<OreGenericEgg>> DRIED_EGGS;

    static {
        Map<String, DeferredBlock<OreGenericEgg>> eggs = new LinkedHashMap<>();
        for (String id : OreGenericEgg.DRIED_EGG_IDS) {
            eggs.put(id, BLOCKS.registerBlock(id, OreGenericEgg::new, OreGenericEgg.originalProperties()));
        }
        DRIED_EGGS = Collections.unmodifiableMap(eggs);
    }

    /** Salt Ore - OreSalt (OreSpawnMain.java:1502, legacy id +100). */
    public static final DeferredBlock<OreSalt> ORESALT = BLOCKS.registerBlock("oresalt", OreSalt::new, OreSalt.originalProperties());
    /** Uranium Ore - OreUranium (OreSpawnMain.java:1274, legacy id +101). */
    public static final DeferredBlock<OreUranium> OREURANIUM = BLOCKS.registerBlock("oreuranium", OreUranium::new, OreUranium.originalProperties());
    /** Titanium Ore - OreTitanium (OreSpawnMain.java:1275, legacy id +102). */
    public static final DeferredBlock<OreTitanium> ORETITANIUM = BLOCKS.registerBlock("oretitanium", OreTitanium::new, OreTitanium.originalProperties());
    /** Amethyst Ore - OreAmethyst (OreSpawnMain.java:1522, legacy id +103). */
    public static final DeferredBlock<OreAmethyst> OREAMETHYST = BLOCKS.registerBlock("oreamethyst", OreAmethyst::new, OreAmethyst.originalProperties());
    /** Ruby Ore - OreRuby (OreSpawnMain.java:1520, legacy id +104). */
    public static final DeferredBlock<OreRuby> ORERUBY = BLOCKS.registerBlock("oreruby", OreRuby::new, OreRuby.originalProperties());
    /** Random Teleport Block - RTPBlock (OreSpawnMain.java:1541, legacy id +105). */
    public static final DeferredBlock<RTPBlock> BLOCKTELEPORT = BLOCKS.registerBlock("blockteleport", RTPBlock::new, RTPBlock.originalProperties());
    /** Lava Foam - Lavafoam (OreSpawnMain.java:1281, legacy id +106). */
    public static final DeferredBlock<Lavafoam> LAVAFOAM = BLOCKS.registerBlock("lavafoam", Lavafoam::new, Lavafoam.originalProperties());
    /** Uranium Block - BlockUranium (OreSpawnMain.java:1278, legacy id +107). */
    public static final DeferredBlock<BlockUranium> BLOCKURANIUM = BLOCKS.registerBlock("blockuranium", BlockUranium::new, BlockUranium.originalProperties());
    /** Titanium Block - BlockTitanium (OreSpawnMain.java:1279, legacy id +108). */
    public static final DeferredBlock<BlockTitanium> BLOCKTITANIUM = BLOCKS.registerBlock("blocktitanium", BlockTitanium::new, BlockTitanium.originalProperties());
    /** Ruby Block - BlockRuby (OreSpawnMain.java:1282, legacy id +109). */
    public static final DeferredBlock<BlockRuby> BLOCKRUBY = BLOCKS.registerBlock("blockruby", p -> new BlockRuby(p, false), BlockRuby.originalProperties());
    /** Amethyst Block - BlockRuby (OreSpawnMain.java:1283, legacy id +110). */
    public static final DeferredBlock<BlockRuby> BLOCKAMETHYST = BLOCKS.registerBlock("blockamethyst", p -> new BlockRuby(p, false), BlockRuby.originalProperties());
    /** Ender-Pearl Block - OreGenericEgg (OreSpawnMain.java:1634, legacy id +111); 9 pearls, recipe in W12. */
    public static final DeferredBlock<OreGenericEgg> BLOCKENDERPEARL = BLOCKS.registerBlock(OreGenericEgg.ENDER_PEARL_ID, OreGenericEgg::new, OreGenericEgg.originalProperties());
    /** Eye-of-Ender Block - OreGenericEgg (OreSpawnMain.java:1635, legacy id +112); BlockExtremeTorch checks it below itself (by id). */
    public static final DeferredBlock<OreGenericEgg> BLOCKEYEOFENDER = BLOCKS.registerBlock(OreGenericEgg.EYE_OF_ENDER_ID, OreGenericEgg::new, OreGenericEgg.originalProperties());

    // ---- W02 (w02-trees-crystal): OreSpawnMain.java:1528-1633. The numbers are the registration
    // line's: a bare setHardness(h) is strength(h); setHardness(h).setResistance(r) is
    // strength(h, r * 0.6) because 1.7.10 stored r * 3 and divided by 5 in explosions (catalogue:
    // crystalgrass "R 2 -> Explosion 1.2"). Step sound as the class or line set it - CrystalGrass and
    // CrystalWood never called setStepSound and kept the Block default, stone. Flags that the original
    // class itself set (random ticks, no collision, non-cube semantics) live in the class constructors.

    /** Sky Tree Log (legacy id +113). */
    public static final DeferredBlock<BlockSkyTreeLog> SKY_TREE_LOG = BLOCKS.registerBlock("skytreelog", BlockSkyTreeLog::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.2f).sound(SoundType.WOOD)); // :1607
    /** Duplicator Tree Log (legacy id +114). */
    public static final DeferredBlock<BlockDuplicatorLog> DUPLICATOR_TREE_LOG = BLOCKS.registerBlock("duplicatortreelog", BlockDuplicatorLog::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.2f).sound(SoundType.WOOD)); // :1608
    // ---- W05 (w05-teleport-ants): the nests, OreSpawnMain.java:5943-5948 (registered :1847-1852), all tabBlock.
    // AntBlock takes its creature and OreSpawnMOBS switch per instance instead of the original identity chain; the
    // suppliers are lazy, so ModBlocks does not initialise ModEntities early. +119 and +122 are dried egg blocks.
    public static final DeferredBlock<AntBlock> ANTBLOCK = BLOCKS.registerBlock("antblock", p -> new AntBlock(p, () -> ModEntities.ANT.get(), OreSpawnConfig.MOBS.BlackAntEnable), AntBlock.originalProperties()); // +115
    public static final DeferredBlock<AntBlock> REDANTBLOCK = BLOCKS.registerBlock("redantblock", p -> new AntBlock(p, () -> ModEntities.RED_ANT.get(), OreSpawnConfig.MOBS.RedAntEnable), AntBlock.originalProperties()); // +116
    public static final DeferredBlock<AntBlock> RAINBOWANTBLOCK = BLOCKS.registerBlock("rainbowantblock", p -> new AntBlock(p, () -> ModEntities.RAINBOW_ANT.get(), OreSpawnConfig.MOBS.RainbowedAntEnable), AntBlock.originalProperties()); // +117
    public static final DeferredBlock<AntBlock> UNSTABLEANTBLOCK = BLOCKS.registerBlock("unstableantblock", p -> new AntBlock(p, () -> ModEntities.UNSTABLE_ANT.get(), OreSpawnConfig.MOBS.UnstableAntEnable), AntBlock.originalProperties()); // +118
    public static final DeferredBlock<AntBlock> TERMITEBLOCK = BLOCKS.registerBlock("termiteblock", p -> new AntBlock(p, () -> ModEntities.TERMITE.get(), OreSpawnConfig.MOBS.TermiteEnable), AntBlock.originalProperties()); // +120
    public static final DeferredBlock<CrystalAntBlock> CRYSTALTERMITEBLOCK = BLOCKS.registerBlock("crystaltermiteblock", CrystalAntBlock::new, CrystalAntBlock.originalProperties()); // +121
    /** Molenoid Dirt - MoleDirtBlock (OreSpawnMain.java:1545, legacy id +123). */
    public static final DeferredBlock<MoleDirtBlock> MOLEDIRT = BLOCKS.registerBlock("moledirt", MoleDirtBlock::new, MoleDirtBlock.originalProperties());
    /** Mobzilla Scale Block - BlockRuby (OreSpawnMain.java:1280, legacy id +124). */
    public static final DeferredBlock<BlockRuby> BLOCKMOBZILLASCALE = BLOCKS.registerBlock("blockmobzillascale", p -> new BlockRuby(p, true), BlockRuby.originalProperties());
    /** Apple Leaves (legacy id +150). */
    public static final DeferredBlock<BlockAppleLeaves> LEAVES_APPLE = BLOCKS.registerBlock("leaves_apple", BlockAppleLeaves::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2f).sound(SoundType.GRASS)); // :1605
    /** Experience Leaves (legacy id +151). */
    public static final DeferredBlock<BlockExperienceLeaves> LEAVES_EXPERIENCE = BLOCKS.registerBlock("leaves_experience", BlockExperienceLeaves::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2f).sound(SoundType.GRASS)); // :1609
    /** Scary Leaves (legacy id +152). */
    public static final DeferredBlock<BlockScaryLeaves> LEAVES_SCARY = BLOCKS.registerBlock("leaves_scary", p -> new BlockScaryLeaves(BlockScaryLeaves.Kind.SCARY, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2f).sound(SoundType.GRASS)); // :1618

    // ---- W02 (w02-crops, block.crop): ids from the manifest, registration lines from OreSpawnMain.java.
    // The four stages of corn, quinoa, tomato and lettuce were four blocks each in 1.7.10 and stay so
    // (stage index 0..3 = MyCornPlant1..4 etc.); the stages share one Properties recipe, the Block
    // constructor copies it. No block items: the seeds are the items (ModItems).

    /** Strawberry plant (legacy id +153). */
    public static final DeferredBlock<BlockStrawberry> STRAWBERRY_PLANT = BLOCKS.registerBlock("strawberry_plant", BlockStrawberry::new, BlockStrawberry.PROPERTIES); // :1548
    // ---- W05 (w05-flyers): the four spawn plants (manifest ctor_args 2854-2857). No block items: the seeds place them.
    public static final DeferredBlock<BlockFireflyPlant> FIREFLY_PLANT = BLOCKS.registerBlock("firefly_plant", BlockFireflyPlant::new, BlockFireflyPlant.PROPERTIES); // +154, :1556
    public static final DeferredBlock<BlockButterflyPlant> BUTTERFLY_PLANT = BLOCKS.registerBlock("butterfly_plant", BlockButterflyPlant::new, BlockButterflyPlant.PROPERTIES); // +155, :1550
    public static final DeferredBlock<BlockMothPlant> MOTH_PLANT = BLOCKS.registerBlock("moth_plant", BlockMothPlant::new, BlockMothPlant.PROPERTIES); // +156, :1552
    public static final DeferredBlock<BlockMosquitoPlant> MOSQUITO_PLANT = BLOCKS.registerBlock("mosquito_plant", BlockMosquitoPlant::new, BlockMosquitoPlant.PROPERTIES); // +157, :1554
    /** Experience sapling - BlockExperiencePlant (legacy id +158); in no creative tab, as in the original. */
    public static final DeferredBlock<BlockExperiencePlant> EXPERIENCE_SAPLING = BLOCKS.registerBlock("experiencesapling", BlockExperiencePlant::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1612
    // Flowers: Material.plants had no hardness (0) and the grass step sound.
    public static final DeferredBlock<MyBlockFlower> FLOWER_PINK = BLOCKS.registerBlock("flower_pink", p -> new MyBlockFlower(MyBlockFlower.Kind.PINK, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1614 setHardness(0.0f), legacy +159
    public static final DeferredBlock<MyBlockFlower> FLOWER_BLUE = BLOCKS.registerBlock("flower_blue", p -> new MyBlockFlower(MyBlockFlower.Kind.BLUE, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1615, legacy +160
    public static final DeferredBlock<MyBlockFlower> FLOWER_BLACK = BLOCKS.registerBlock("flower_black", p -> new MyBlockFlower(MyBlockFlower.Kind.BLACK, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1616, legacy +161
    public static final DeferredBlock<MyBlockFlower> FLOWER_SCARY = BLOCKS.registerBlock("flower_scary", p -> new MyBlockFlower(MyBlockFlower.Kind.SCARY, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1617, legacy +162
    public static final DeferredBlock<BlockCorn> CORN_0 = BLOCKS.registerBlock("corn_0", p -> new BlockCorn(0, p), BlockCorn.PROPERTIES); // :1567, legacy +163
    public static final DeferredBlock<BlockCorn> CORN_1 = BLOCKS.registerBlock("corn_1", p -> new BlockCorn(1, p), BlockCorn.PROPERTIES); // :1568
    public static final DeferredBlock<BlockCorn> CORN_2 = BLOCKS.registerBlock("corn_2", p -> new BlockCorn(2, p), BlockCorn.PROPERTIES); // :1569
    public static final DeferredBlock<BlockCorn> CORN_3 = BLOCKS.registerBlock("corn_3", p -> new BlockCorn(3, p), BlockCorn.PROPERTIES); // :1570
    public static final DeferredBlock<BlockTomato> TOMATO_0 = BLOCKS.registerBlock("tomato_0", p -> new BlockTomato(0, p), BlockTomato.PROPERTIES); // :1577, legacy +167
    public static final DeferredBlock<BlockTomato> TOMATO_1 = BLOCKS.registerBlock("tomato_1", p -> new BlockTomato(1, p), BlockTomato.PROPERTIES); // :1578
    public static final DeferredBlock<BlockTomato> TOMATO_2 = BLOCKS.registerBlock("tomato_2", p -> new BlockTomato(2, p), BlockTomato.PROPERTIES); // :1579
    public static final DeferredBlock<BlockTomato> TOMATO_3 = BLOCKS.registerBlock("tomato_3", p -> new BlockTomato(3, p), BlockTomato.PROPERTIES); // :1580
    public static final DeferredBlock<BlockLettuce> LETTUCE_0 = BLOCKS.registerBlock("lettuce_0", p -> new BlockLettuce(0, p), BlockLettuce.PROPERTIES); // :1582, legacy +171
    public static final DeferredBlock<BlockLettuce> LETTUCE_1 = BLOCKS.registerBlock("lettuce_1", p -> new BlockLettuce(1, p), BlockLettuce.PROPERTIES); // :1583
    public static final DeferredBlock<BlockLettuce> LETTUCE_2 = BLOCKS.registerBlock("lettuce_2", p -> new BlockLettuce(2, p), BlockLettuce.PROPERTIES); // :1584
    public static final DeferredBlock<BlockLettuce> LETTUCE_3 = BLOCKS.registerBlock("lettuce_3", p -> new BlockLettuce(3, p), BlockLettuce.PROPERTIES); // :1585
    /** Radish plant (legacy id +175). */
    public static final DeferredBlock<BlockRadish> RADISH_PLANT = BLOCKS.registerBlock("radish_plant", BlockRadish::new, BlockRadish.PROPERTIES); // :1558
    public static final DeferredBlock<BlockScaryLeaves> LEAVES_CHERRY = BLOCKS.registerBlock("leaves_cherry", p -> new BlockScaryLeaves(BlockScaryLeaves.Kind.CHERRY, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.15f).sound(SoundType.GRASS)); // :1619, legacy +176
    public static final DeferredBlock<BlockScaryLeaves> LEAVES_PEACH = BLOCKS.registerBlock("leaves_peach", p -> new BlockScaryLeaves(BlockScaryLeaves.Kind.PEACH, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.15f).sound(SoundType.GRASS)); // :1620, legacy +177
    /** Rice plant (legacy id +178). */
    public static final DeferredBlock<BlockRice> RICE_PLANT = BLOCKS.registerBlock("rice_plant", BlockRice::new, BlockRice.PROPERTIES); // :1564
    public static final DeferredBlock<BlockQuinoa> QUINOA_0 = BLOCKS.registerBlock("quinoa_0", p -> new BlockQuinoa(0, p), BlockQuinoa.PROPERTIES); // :1572, legacy +179
    public static final DeferredBlock<BlockQuinoa> QUINOA_1 = BLOCKS.registerBlock("quinoa_1", p -> new BlockQuinoa(1, p), BlockQuinoa.PROPERTIES); // :1573
    public static final DeferredBlock<BlockQuinoa> QUINOA_2 = BLOCKS.registerBlock("quinoa_2", p -> new BlockQuinoa(2, p), BlockQuinoa.PROPERTIES); // :1574
    public static final DeferredBlock<BlockQuinoa> QUINOA_3 = BLOCKS.registerBlock("quinoa_3", p -> new BlockQuinoa(3, p), BlockQuinoa.PROPERTIES); // :1575
    // legacy ids +190, +191: the repellents (W10).
    /** Kraken Repellent (legacy id +190, OreSpawnMain.java:1590 setLightLevel(0.8f) -> 12) and its wall twin (R18). */
    public static final DeferredBlock<com.swbr.orespawn.block.repellent.KrakenRepellent> KRAKEN_REPELLENT = BLOCKS.registerBlock("krakenrepellent", com.swbr.orespawn.block.repellent.KrakenRepellent::new,
            com.swbr.orespawn.block.repellent.KrakenRepellent.originalProperties());
    public static final DeferredBlock<com.swbr.orespawn.block.repellent.KrakenRepellentWall> KRAKEN_REPELLENT_WALL = BLOCKS.registerBlock("krakenrepellent_wall", com.swbr.orespawn.block.repellent.KrakenRepellentWall::new,
            com.swbr.orespawn.block.repellent.KrakenRepellent.originalProperties());
    /** Creeper Repellent (legacy id +191) and its wall twin (R18); BlockTorch: Material.circuits, hardness 0, stone step sound, :1592 setLightLevel(0.8f) -> 12. */
    public static final DeferredBlock<com.swbr.orespawn.block.repellent.CreeperRepellent> CREEPER_REPELLENT = BLOCKS.registerBlock("creeperrepellent", com.swbr.orespawn.block.repellent.CreeperRepellent::new,
            BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 12).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<com.swbr.orespawn.block.repellent.CreeperRepellentWall> CREEPER_REPELLENT_WALL = BLOCKS.registerBlock("creeperrepellent_wall", com.swbr.orespawn.block.repellent.CreeperRepellentWall::new,
            BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 12).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY));
    // Torches: BlockTorch was Material.circuits (no push mobility), hardness 0; light from the line. The step sound
    // is Block's default soundTypeStone: BlockTorch's constructor (client-1.7.10.jar, aoc.<init>) sets only random
    // ticks and the tab, vanilla's own torch got soundTypeWood on its Blocks line, and :1589/:1600 set only the light.
    /** Extreme Torch (legacy id +192) and its wall twin (R18). */
    public static final DeferredBlock<BlockExtremeTorch> EXTREME_TORCH = BLOCKS.registerBlock("extremetorch", BlockExtremeTorch::new,
            BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 15).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)); // :1589 setLightLevel(1.0f)
    public static final DeferredBlock<BlockExtremeWallTorch> EXTREME_TORCH_WALL = BLOCKS.registerBlock("extremetorch_wall", BlockExtremeWallTorch::new,
            BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 15).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)); // R18 wall twin
    /** Island Block - IslandBlock (OreSpawnMain.java:1591, legacy id +193): spawns the floating islands (W08). */
    public static final DeferredBlock<com.swbr.orespawn.block.island.IslandBlock> ISLAND = BLOCKS.registerBlock("island", com.swbr.orespawn.block.island.IslandBlock::new, com.swbr.orespawn.block.island.IslandBlock.originalProperties()); // :1591 setLightLevel(0.9f) -> 13
    /** Pizza (legacy id +194). */
    public static final DeferredBlock<BlockPizza> PIZZA = BLOCKS.registerBlock("pizza", BlockPizza::new, BlockPizza.PROPERTIES); // :1288
    /** The King Spawner Block - KingSpawnerBlock (OreSpawnMain.java:1601, legacy id +195, registered :1842): summons The King in guard mode (W10). */
    public static final DeferredBlock<com.swbr.orespawn.block.spawner.KingSpawnerBlock> KINGSPAWNER = BLOCKS.registerBlock("kingspawner", com.swbr.orespawn.block.spawner.KingSpawnerBlock::new, com.swbr.orespawn.block.spawner.KingSpawnerBlock.originalProperties()); // :1601 setLightLevel(0.9f) -> 13
    /** Random Dungeon Spawner - DungeonSpawnerBlock (OreSpawnMain.java:1604, legacy id +196, registered :1844); no block item, placed only by randomdungeon. */
    public static final DeferredBlock<com.swbr.orespawn.block.spawner.DungeonSpawnerBlock> DUNGEONSPAWNER = BLOCKS.registerBlock("dungeonspawner", com.swbr.orespawn.block.spawner.DungeonSpawnerBlock::new, com.swbr.orespawn.block.spawner.DungeonSpawnerBlock.originalProperties()); // :1604 setLightLevel(0.9f) -> 13
    /** The Queen Spawner Block - QueenSpawnerBlock (OreSpawnMain.java:1602, legacy id +197, registered :1843). */
    public static final DeferredBlock<com.swbr.orespawn.block.spawner.QueenSpawnerBlock> QUEENSPAWNER = BLOCKS.registerBlock("queenspawner", com.swbr.orespawn.block.spawner.QueenSpawnerBlock::new, com.swbr.orespawn.block.spawner.QueenSpawnerBlock.originalProperties()); // :1602 setLightLevel(0.9f) -> 13
    /** Duct Tape (legacy id +198). */
    public static final DeferredBlock<BlockDuctTape> DUCTTAPE = BLOCKS.registerBlock("ducttape", BlockDuctTape::new, BlockDuctTape.PROPERTIES); // :1290
    /** Kyanite - OreBasicStone (OreSpawnMain.java:1526, legacy id +200). */
    public static final DeferredBlock<OreBasicStone> CRYSTALSTONE = BLOCKS.registerBlock("crystalstone", OreBasicStone::new, OreBasicStone.originalProperties(2.0f, 10.0f));
    /** Crystal Energy - OreCrystal (OreSpawnMain.java:1527, legacy id +201). */
    public static final DeferredBlock<OreCrystal> CRYSTALCOAL = BLOCKS.registerBlock("crystalcoal", OreCrystal::new, OreCrystal.originalProperties(0.6f, 6.0f, 20.0f));
    /** Crystal Grass (legacy id +202). */
    public static final DeferredBlock<CrystalGrass> CRYSTAL_GRASS = BLOCKS.registerBlock("crystalgrass", CrystalGrass::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6f, 1.2f).sound(SoundType.STONE)); // :1528 (0.6f, 2.0f)
    public static final DeferredBlock<MyBlockFlower> CRYSTAL_FLOWER_RED = BLOCKS.registerBlock("crystalflower_red", p -> new MyBlockFlower(MyBlockFlower.Kind.CRYSTAL_RED, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1623, legacy +203
    public static final DeferredBlock<MyBlockFlower> CRYSTAL_FLOWER_GREEN = BLOCKS.registerBlock("crystalflower_green", p -> new MyBlockFlower(MyBlockFlower.Kind.CRYSTAL_GREEN, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1624, legacy +204
    public static final DeferredBlock<MyBlockFlower> CRYSTAL_FLOWER_BLUE = BLOCKS.registerBlock("crystalflower_blue", p -> new MyBlockFlower(MyBlockFlower.Kind.CRYSTAL_BLUE, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1625, legacy +205
    public static final DeferredBlock<MyBlockFlower> CRYSTAL_FLOWER_YELLOW = BLOCKS.registerBlock("crystalflower_yellow", p -> new MyBlockFlower(MyBlockFlower.Kind.CRYSTAL_YELLOW, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.GRASS)); // :1626, legacy +206
    /** Crystal Tree Log (legacy id +207). */
    public static final DeferredBlock<BlockCrystalTreeLog> CRYSTAL_TREE_LOG = BLOCKS.registerBlock("crystaltreelog", BlockCrystalTreeLog::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.2f).sound(SoundType.WOOD)); // :1628
    /** Crystal Tree Leaves, red (legacy id +208). */
    public static final DeferredBlock<BlockCrystalLeaves> CRYSTAL_TREE_LEAVES = BLOCKS.registerBlock("crystaltreeleaves", p -> new BlockCrystalLeaves(BlockCrystalLeaves.Kind.RED, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.2f).sound(SoundType.GRASS)); // :1627
    /** Pink Tourmaline - OreCrystalCrystal (OreSpawnMain.java:1529, legacy id +209). */
    public static final DeferredBlock<OreCrystalCrystal> CRYSTALCRYSTAL = BLOCKS.registerBlock("crystalcrystal", p -> new OreCrystalCrystal(p, false), OreCrystalCrystal.originalProperties(0.4f, 12.0f, 40.0f));
    /** Crystal Planks - CrystalWood (legacy id +210). */
    public static final DeferredBlock<CrystalWood> CRYSTAL_PLANKS = BLOCKS.registerBlock("crystalplanks", CrystalWood::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f, 2.4f).sound(SoundType.STONE)); // :1531 (1.5f, 4.0f)
    // ---- W03 (w03-workshop)
    /** Crystal Workbench - CrystalWorkbench (OreSpawnMain.java:1532, legacy id +211, {@code (1.0f, 5.0f)}). */
    public static final DeferredBlock<CrystalWorkbench> CRYSTALWORKBENCH = BLOCKS.registerBlock("crystalworkbench", CrystalWorkbench::new, CrystalWorkbench.originalProperties());
    /** Crystal Furnace - CrystalFurnace (OreSpawnMain.java:1533-1534, legacy ids +212 off and +213 on: one block with LIT, DECISIONS R2, {@code (2.0f, 10.0f)}). */
    public static final DeferredBlock<CrystalFurnace> CRYSTALFURNACE = BLOCKS.registerBlock("crystalfurnace", CrystalFurnace::new, CrystalFurnace.originalProperties());
    /** Crystal Torch (legacy id +214) and its wall twin (R18). */
    public static final DeferredBlock<BlockCrystalTorch> CRYSTAL_TORCH = BLOCKS.registerBlock("crystaltorch", BlockCrystalTorch::new,
            BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 14).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)); // :1600 setLightLevel(0.99f) -> (int)(15 * 0.99) = 14; stone sound, see the torch note above
    public static final DeferredBlock<BlockCrystalWallTorch> CRYSTAL_TORCH_WALL = BLOCKS.registerBlock("crystaltorch_wall", BlockCrystalWallTorch::new,
            BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 14).sound(SoundType.STONE).pushReaction(PushReaction.DESTROY)); // R18 wall twin
    /** Crystal Tree Leaves, yellow (legacy id +215). */
    public static final DeferredBlock<BlockCrystalLeaves> CRYSTAL_TREE_LEAVES2 = BLOCKS.registerBlock("crystaltreeleaves2", p -> new BlockCrystalLeaves(BlockCrystalLeaves.Kind.YELLOW, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.25f).sound(SoundType.GRASS)); // :1629
    /** Pink Tourmaline Block - BlockCrystal (OreSpawnMain.java:1284, legacy id +216). */
    public static final DeferredBlock<BlockCrystal> CRYSTALPINK_BLOCK = BLOCKS.registerBlock("crystalpink_block", BlockCrystal::new, BlockCrystal.originalProperties());
    /** Tiger's Eye - OreCrystalCrystal (OreSpawnMain.java:1530, legacy id +217). */
    public static final DeferredBlock<OreCrystalCrystal> TIGERSEYE = BLOCKS.registerBlock("tigerseye", p -> new OreCrystalCrystal(p, true), OreCrystalCrystal.originalProperties(0.5f, 15.0f, 60.0f));
    /** Tiger's Eye Block - BlockCrystal (OreSpawnMain.java:1286, legacy id +218). */
    public static final DeferredBlock<BlockCrystal> TIGERSEYE_BLOCK = BLOCKS.registerBlock("tigerseye_block", BlockCrystal::new, BlockCrystal.originalProperties());
    /** Crystalized Rats - OreBasicStone (OreSpawnMain.java:1537, legacy id +219): 1 + nextInt(10) Rats. */
    public static final DeferredBlock<OreBasicStone> CRYSTALRAT = BLOCKS.registerBlock("crystalrat", p -> new OreBasicStone(p, () -> ModEntities.RAT.get(), 1, 10), OreBasicStone.originalProperties(2.5f, 14.0f));
    /** Crystalized Fairies - OreBasicStone (OreSpawnMain.java:1538, legacy id +220): 1 + nextInt(6) Fairies. */
    public static final DeferredBlock<OreBasicStone> CRYSTALFAIRY = BLOCKS.registerBlock("crystalfairy", p -> new OreBasicStone(p, () -> ModEntities.FAIRY.get(), 1, 6), OreBasicStone.originalProperties(2.5f, 14.0f));
    /** Crystal Tree Leaves, blue (legacy id +221). */
    public static final DeferredBlock<BlockCrystalLeaves> CRYSTAL_TREE_LEAVES3 = BLOCKS.registerBlock("crystaltreeleaves3", p -> new BlockCrystalLeaves(BlockCrystalLeaves.Kind.BLUE, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(0.25f).sound(SoundType.GRASS)); // :1630
    // Saplings: BlockReed / Material.plants had no hardness (0). The step sound is Block's default soundTypeStone:
    // BlockReed's constructor (client-1.7.10.jar, ane.<init>) sets only the bounds and random ticks, vanilla's
    // sugar cane got soundTypeGrass on its Blocks line, and :1631-1633 set only the name.
    public static final DeferredBlock<BlockCrystalPlant> CRYSTAL_SAPLING = BLOCKS.registerBlock("crystalsapling", p -> new BlockCrystalPlant(BlockCrystalPlant.Kind.RED, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.STONE)); // :1631, legacy +222
    public static final DeferredBlock<BlockCrystalPlant> CRYSTAL_SAPLING2 = BLOCKS.registerBlock("crystalsapling2", p -> new BlockCrystalPlant(BlockCrystalPlant.Kind.YELLOW, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.STONE)); // :1632, legacy +223
    public static final DeferredBlock<BlockCrystalPlant> CRYSTAL_SAPLING3 = BLOCKS.registerBlock("crystalsapling3", p -> new BlockCrystalPlant(BlockCrystalPlant.Kind.BLUE, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.STONE)); // :1633, legacy +224
    /** Red Ant Troll Block - OreBasicStone (OreSpawnMain.java:1539, legacy id +225): 15 + nextInt(6) Red Ants. */
    public static final DeferredBlock<OreBasicStone> REDANTTROLL = BLOCKS.registerBlock("redanttroll", p -> new OreBasicStone(p, () -> ModEntities.RED_ANT.get(), 15, 6), OreBasicStone.originalProperties(2.5f, 14.0f));
    /** Termite Troll Block - OreBasicStone (OreSpawnMain.java:1540, legacy id +226): 15 + nextInt(6) Termites. */
    public static final DeferredBlock<OreBasicStone> TERMITETROLL = BLOCKS.registerBlock("termitetroll", p -> new OreBasicStone(p, () -> ModEntities.TERMITE.get(), 15, 6), OreBasicStone.originalProperties(2.5f, 14.0f));

    private ModBlocks() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
