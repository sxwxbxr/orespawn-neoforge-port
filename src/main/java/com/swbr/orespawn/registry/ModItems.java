package com.swbr.orespawn.registry;

import com.swbr.orespawn.item.robotkit.ItemSpiderRobotKit;
import com.swbr.orespawn.item.robotkit.ItemWrench;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.ore.OreGenericEgg;
import com.swbr.orespawn.item.armor.ArmorSet;
import com.swbr.orespawn.item.armor.ItemOreSpawnArmor;
import com.swbr.orespawn.item.bertha.Bertha;
import com.swbr.orespawn.item.bow.ItemIrukandjiArrow;
import com.swbr.orespawn.item.cage.CritterCage;
import com.swbr.orespawn.item.bow.SkateBow;
import com.swbr.orespawn.item.bow.UltimateBow;
import com.swbr.orespawn.item.bow.UltimateFishingRod;
import com.swbr.orespawn.item.companion.ItemShoes;
import com.swbr.orespawn.item.crop.ItemAppleSeed;
import com.swbr.orespawn.item.crop.ItemCornCob;
import com.swbr.orespawn.item.crop.ItemDuctTape;
import com.swbr.orespawn.item.crop.ItemExperienceTreeSeed;
import com.swbr.orespawn.item.crop.ItemLettuce;
import com.swbr.orespawn.item.crop.ItemPizza;
import com.swbr.orespawn.item.crop.ItemRadish;
import com.swbr.orespawn.item.crop.ItemStrawberry;
import com.swbr.orespawn.item.crop.ItemStrawberrySeed;
import com.swbr.orespawn.item.crop.ItemTomato;
import com.swbr.orespawn.item.fairy.FairySword;
import com.swbr.orespawn.item.ratsword.RatSword;
import com.swbr.orespawn.item.squidzooka.ItemSquidZooka;
import com.swbr.orespawn.item.food.ItemFireFish;
import com.swbr.orespawn.item.insectseed.ItemButterflySeed;
import com.swbr.orespawn.item.insectseed.ItemFireflySeed;
import com.swbr.orespawn.item.insectseed.ItemMosquitoSeed;
import com.swbr.orespawn.item.insectseed.ItemMothSeed;
import com.swbr.orespawn.item.food.ItemGenericFish;
import com.swbr.orespawn.item.food.ItemLavaEel;
import com.swbr.orespawn.item.food.ItemPopcorn;
import com.swbr.orespawn.item.food.ItemSparkFish;
import com.swbr.orespawn.item.food.ItemSunFish;
import com.swbr.orespawn.item.material.IngotTitanium;
import com.swbr.orespawn.item.material.IngotUranium;
import com.swbr.orespawn.item.material.ItemCrystalSticks;
import com.swbr.orespawn.item.material.ItemSalt;
import com.swbr.orespawn.item.ranged.ItemAcid;
import com.swbr.orespawn.item.ranged.ItemIceBall;
import com.swbr.orespawn.item.ranged.ItemIrukandji;
import com.swbr.orespawn.item.ranged.ItemLaserBall;
import com.swbr.orespawn.item.ranged.ItemRayGun;
import com.swbr.orespawn.item.ranged.ItemSunspotUrchin;
import com.swbr.orespawn.item.ranged.ItemThunderStaff;
import com.swbr.orespawn.item.ranged.ItemWaterBall;
import com.swbr.orespawn.item.rock.ItemRock;
import com.swbr.orespawn.item.rock.ItemSifter;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.item.tool.AmethystAxe;
import com.swbr.orespawn.item.tool.AmethystHoe;
import com.swbr.orespawn.item.tool.AmethystPickaxe;
import com.swbr.orespawn.item.tool.AmethystShovel;
import com.swbr.orespawn.item.tool.AmethystSword;
import com.swbr.orespawn.item.tool.BigHammer;
import com.swbr.orespawn.item.tool.CrystalAxe;
import com.swbr.orespawn.item.tool.CrystalHoe;
import com.swbr.orespawn.item.tool.CrystalPickaxe;
import com.swbr.orespawn.item.tool.CrystalShovel;
import com.swbr.orespawn.item.tool.CrystalSword;
import com.swbr.orespawn.item.tool.EmeraldAxe;
import com.swbr.orespawn.item.tool.EmeraldHoe;
import com.swbr.orespawn.item.tool.EmeraldPickaxe;
import com.swbr.orespawn.item.tool.EmeraldShovel;
import com.swbr.orespawn.item.tool.EmeraldSword;
import com.swbr.orespawn.item.tool.ExperienceSword;
import com.swbr.orespawn.item.tool.MantisClaw;
import com.swbr.orespawn.item.tool.NightmareSword;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import com.swbr.orespawn.item.tool.PoisonSword;
import com.swbr.orespawn.item.tool.RubyAxe;
import com.swbr.orespawn.item.tool.RubyHoe;
import com.swbr.orespawn.item.tool.RubyPickaxe;
import com.swbr.orespawn.item.tool.RubyShovel;
import com.swbr.orespawn.item.tool.RubySword;
import com.swbr.orespawn.item.tool.UltimateAxe;
import com.swbr.orespawn.item.tool.UltimateHoe;
import com.swbr.orespawn.item.tool.UltimatePickaxe;
import com.swbr.orespawn.item.tool.UltimateShovel;
import com.swbr.orespawn.item.tool.UltimateSword;
import com.swbr.orespawn.item.utility.ExperienceCatcher;
import com.swbr.orespawn.item.utility.InstantGarden;
import com.swbr.orespawn.item.utility.InstantShelter;
import com.swbr.orespawn.item.utility.ItemCreeperLauncher;
import com.swbr.orespawn.item.utility.ItemMinersDream;
import com.swbr.orespawn.item.utility.ItemNetherLost;
import com.swbr.orespawn.item.utility.ItemZooKeeper;
import com.swbr.orespawn.item.utility.StepAccross;
import com.swbr.orespawn.item.utility.StepDown;
import com.swbr.orespawn.item.utility.StepUp;
import com.swbr.orespawn.item.utility.ZooCage;
import com.swbr.orespawn.item.vehicle.ItemElevator;
import com.swbr.orespawn.registry.ModCreativeTabs.OriginalTab;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Items. 473 in the original (OreSpawnMain.java:1775-2323, manifest {@code counts.items}),
 * registered here wave by wave under the manifest ids (DECISIONS R2).
 *
 * <p>Two items were named after their block in 1.7.10 and carry the manifest's renamed ids:
 * {@code pizza_item} and {@code ducttape_item} (R2). Spawn eggs are their own {@code Item} per egg
 * with the original icon texture, never {@code DeferredSpawnEggItem} (R9).
 *
 * <p>Order matters: a 1.7.10 creative tab listed its content by numeric id, block items (id =
 * block id) before items. {@link ModCreativeTabs#add} keeps insertion order, so the holders below
 * are written in legacy-id order - block items by {@code BaseBlockID + n}, items by
 * {@code BaseItemID + n} (manifest {@code ctor_args[0]}) - and each holder files itself under the
 * tab its original class named. This class depends on {@link ModBlocks}; {@code ModBlocks} never
 * refers back, so there is no initialisation cycle.
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawn.MOD_ID);

    /** Registers a plain item and files it under {@code tab}. */
    private static <I extends Item> DeferredItem<I> item(OriginalTab tab, String id,
            Function<Item.Properties, ? extends I> factory) {
        DeferredItem<I> holder = ITEMS.registerItem(id, factory);
        ModCreativeTabs.add(tab, holder);
        return holder;
    }

    /** Registers the {@code ItemBlock} of a block under the block's id and files it under {@code tab}. */
    private static DeferredItem<BlockItem> blockItem(OriginalTab tab, DeferredBlock<? extends Block> block) {
        DeferredItem<BlockItem> holder = ITEMS.registerSimpleBlockItem(block);
        ModCreativeTabs.add(tab, holder);
        return holder;
    }

    /**
     * Armor (w03-armor): one {@link ItemOreSpawnArmor} per (set, piece), tabCombat
     * (ItemOreSpawnArmor.java:23); durability and stack size 1 are set in the item constructor.
     */
    private static DeferredItem<ItemOreSpawnArmor> armor(String id, ArmorSet set, ArmorItem.Type type) {
        return item(OriginalTab.COMBAT, id, p -> new ItemOreSpawnArmor(set, type, p));
    }

    // Spawn eggs for the five vanilla mobs (OreSpawnMain.java:2004-2008, :5183-5187; manifest ids
    // 192-196). Registered unconditionally like the original; each egg keeps its own icon via
    // models/item/<id>.json (R9, corrected). Registration order = creative-tab order. The dispenser
    // behaviour and the SPAWN_EGGS tab entry follow through item.spawnegg.SpawnEggSetup, which
    // walks ItemSpawnEgg.all() - do not add eggs to ModCreativeTabs as well.
    public static final DeferredItem<ItemSpawnEgg> EGG_WITHER_SKELETON = ITEMS.registerItem(
            // PORT: case 192 was a vanilla skeleton with skelly_type = 1; 1.21.1 has a separate type (R18).
            "eggwitherskeleton", p -> new ItemSpawnEgg(() -> EntityType.WITHER_SKELETON, p));
    public static final DeferredItem<ItemSpawnEgg> EGG_ENDER_DRAGON = ITEMS.registerItem(
            "eggenderdragon", p -> new ItemSpawnEgg(() -> EntityType.ENDER_DRAGON, p));
    public static final DeferredItem<ItemSpawnEgg> EGG_SNOW_GOLEM = ITEMS.registerItem(
            "eggsnowgolem", p -> new ItemSpawnEgg(() -> EntityType.SNOW_GOLEM, p));
    public static final DeferredItem<ItemSpawnEgg> EGG_IRON_GOLEM = ITEMS.registerItem(
            "eggirongolem", p -> new ItemSpawnEgg(() -> EntityType.IRON_GOLEM, p));
    public static final DeferredItem<ItemSpawnEgg> EGG_WITHER_BOSS = ITEMS.registerItem(
            "eggwitherboss", p -> new ItemSpawnEgg(() -> EntityType.WITHER, p));

    // =====================================================================================
    // W02 block items, legacy block-id order (the tabBlock / tabDecorations / tabRedstone order).
    // =====================================================================================

    /**
     * The 119 Ancient Dried Spawn Egg block items, keyed by id ({@link ModBlocks#DRIED_EGGS}). The
     * legacy ids sit in four runs - 0-96, 119/122/125, 250-261, 300-306 - so the eggs are filed in
     * four slices around the blocks whose ids fall between them; the map itself keeps
     * {@code OreGenericEgg.DRIED_EGG_IDS} order.
     */
    public static final Map<String, DeferredItem<BlockItem>> DRIED_EGG_ITEMS;
    private static final Map<String, DeferredItem<BlockItem>> DRIED_EGG_ITEMS_MUTABLE = new LinkedHashMap<>();

    /** Files the dried eggs with indices {@code [from, to)} of {@code DRIED_EGG_IDS} under tabBlock. */
    private static void driedEggItems(int from, int to) {
        for (String id : OreGenericEgg.DRIED_EGG_IDS.subList(from, to)) {
            DRIED_EGG_ITEMS_MUTABLE.put(id, blockItem(OriginalTab.BLOCK, ModBlocks.DRIED_EGGS.get(id)));
        }
    }

    static {
        driedEggItems(0, 97); // legacy ids +0 to +96
    }

    public static final DeferredItem<BlockItem> ORESALT = blockItem(OriginalTab.BLOCK, ModBlocks.ORESALT); // +100
    public static final DeferredItem<BlockItem> OREURANIUM = blockItem(OriginalTab.BLOCK, ModBlocks.OREURANIUM); // +101
    public static final DeferredItem<BlockItem> ORETITANIUM = blockItem(OriginalTab.BLOCK, ModBlocks.ORETITANIUM); // +102
    public static final DeferredItem<BlockItem> OREAMETHYST = blockItem(OriginalTab.BLOCK, ModBlocks.OREAMETHYST); // +103
    public static final DeferredItem<BlockItem> ORERUBY = blockItem(OriginalTab.BLOCK, ModBlocks.ORERUBY); // +104
    public static final DeferredItem<BlockItem> BLOCKTELEPORT = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKTELEPORT); // +105
    public static final DeferredItem<BlockItem> LAVAFOAM = blockItem(OriginalTab.BLOCK, ModBlocks.LAVAFOAM); // +106
    public static final DeferredItem<BlockItem> BLOCKURANIUM = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKURANIUM); // +107
    public static final DeferredItem<BlockItem> BLOCKTITANIUM = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKTITANIUM); // +108
    public static final DeferredItem<BlockItem> BLOCKRUBY = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKRUBY); // +109
    public static final DeferredItem<BlockItem> BLOCKAMETHYST = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKAMETHYST); // +110
    public static final DeferredItem<BlockItem> BLOCKENDERPEARL = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKENDERPEARL); // +111
    public static final DeferredItem<BlockItem> BLOCKEYEOFENDER = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKEYEOFENDER); // +112
    public static final DeferredItem<BlockItem> SKY_TREE_LOG = blockItem(OriginalTab.BLOCK, ModBlocks.SKY_TREE_LOG); // +113, BlockSkyTreeLog.java:17
    public static final DeferredItem<BlockItem> DUPLICATOR_TREE_LOG = blockItem(OriginalTab.BLOCK, ModBlocks.DUPLICATOR_TREE_LOG); // +114, BlockDuplicatorLog.java:16
    // W05 nests: GameRegistry.registerBlock gave each an ItemBlock in tabBlock (AntBlock.java:21, CrystalAntBlock.java:23).
    public static final DeferredItem<BlockItem> ANTBLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.ANTBLOCK); // +115
    public static final DeferredItem<BlockItem> REDANTBLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.REDANTBLOCK); // +116
    public static final DeferredItem<BlockItem> RAINBOWANTBLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.RAINBOWANTBLOCK); // +117
    public static final DeferredItem<BlockItem> UNSTABLEANTBLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.UNSTABLEANTBLOCK); // +118

    static {
        driedEggItems(97, 98); // +119 oregodzilla
    }

    public static final DeferredItem<BlockItem> TERMITEBLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.TERMITEBLOCK); // +120
    public static final DeferredItem<BlockItem> CRYSTALTERMITEBLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTALTERMITEBLOCK); // +121

    static {
        driedEggItems(98, 99); // +122 oretheking
    }

    public static final DeferredItem<BlockItem> MOLEDIRT = blockItem(OriginalTab.BLOCK, ModBlocks.MOLEDIRT); // +123
    public static final DeferredItem<BlockItem> BLOCKMOBZILLASCALE = blockItem(OriginalTab.BLOCK, ModBlocks.BLOCKMOBZILLASCALE); // +124

    static {
        driedEggItems(99, 100); // +125 orethequeen
    }

    // Leaves: the 1.7.10 BlockLeaves constructor set tabDecorations.
    public static final DeferredItem<BlockItem> LEAVES_APPLE = blockItem(OriginalTab.DECORATIONS, ModBlocks.LEAVES_APPLE); // +150
    public static final DeferredItem<BlockItem> LEAVES_EXPERIENCE = blockItem(OriginalTab.DECORATIONS, ModBlocks.LEAVES_EXPERIENCE); // +151
    public static final DeferredItem<BlockItem> LEAVES_SCARY = blockItem(OriginalTab.DECORATIONS, ModBlocks.LEAVES_SCARY); // +152
    // +153 strawberry_plant, +163..+182 crops: no ItemBlock, the seed items below plant them.
    /** +158: BlockExperiencePlant never called setCreativeTab (:13-17) - in no tab, as in the original. */
    public static final DeferredItem<BlockItem> EXPERIENCE_SAPLING = ITEMS.registerSimpleBlockItem(ModBlocks.EXPERIENCE_SAPLING);
    public static final DeferredItem<BlockItem> FLOWER_PINK = blockItem(OriginalTab.DECORATIONS, ModBlocks.FLOWER_PINK); // +159, MyBlockFlower.java:22
    public static final DeferredItem<BlockItem> FLOWER_BLUE = blockItem(OriginalTab.DECORATIONS, ModBlocks.FLOWER_BLUE); // +160
    public static final DeferredItem<BlockItem> FLOWER_BLACK = blockItem(OriginalTab.DECORATIONS, ModBlocks.FLOWER_BLACK); // +161
    public static final DeferredItem<BlockItem> FLOWER_SCARY = blockItem(OriginalTab.DECORATIONS, ModBlocks.FLOWER_SCARY); // +162
    public static final DeferredItem<BlockItem> LEAVES_CHERRY = blockItem(OriginalTab.DECORATIONS, ModBlocks.LEAVES_CHERRY); // +176
    public static final DeferredItem<BlockItem> LEAVES_PEACH = blockItem(OriginalTab.DECORATIONS, ModBlocks.LEAVES_PEACH); // +177
    // The two torches are one item each that places the standing block or its _wall twin (DECISIONS R18).
    public static final DeferredItem<StandingAndWallBlockItem> KRAKEN_REPELLENT = item(OriginalTab.REDSTONE, "krakenrepellent",
            p -> new StandingAndWallBlockItem(ModBlocks.KRAKEN_REPELLENT.get(), ModBlocks.KRAKEN_REPELLENT_WALL.get(), p, Direction.DOWN)); // +190, KrakenRepellent.java:15, W10
    public static final DeferredItem<StandingAndWallBlockItem> CREEPER_REPELLENT = item(OriginalTab.REDSTONE, "creeperrepellent",
            p -> new StandingAndWallBlockItem(ModBlocks.CREEPER_REPELLENT.get(), ModBlocks.CREEPER_REPELLENT_WALL.get(), p, Direction.DOWN)); // +191, CreeperRepellent.java:16, W10
    public static final DeferredItem<StandingAndWallBlockItem> EXTREME_TORCH = item(OriginalTab.REDSTONE, "extremetorch",
            p -> new StandingAndWallBlockItem(ModBlocks.EXTREME_TORCH.get(), ModBlocks.EXTREME_TORCH_WALL.get(), p, Direction.DOWN)); // +192, BlockExtremeTorch.java:16
    public static final DeferredItem<BlockItem> ISLAND = blockItem(OriginalTab.DECORATIONS, ModBlocks.ISLAND); // +193, IslandBlock.java:19 setCreativeTab(tabDecorations), W08
    public static final DeferredItem<BlockItem> KINGSPAWNER = blockItem(OriginalTab.DECORATIONS, ModBlocks.KINGSPAWNER); // +195, KingSpawnerBlock.java:19 setCreativeTab(tabDecorations), W10
    public static final DeferredItem<BlockItem> QUEENSPAWNER = blockItem(OriginalTab.DECORATIONS, ModBlocks.QUEENSPAWNER); // +197, QueenSpawnerBlock.java:19, W10
    // +194 pizza and +198 ducttape: their items carry the manifest's renamed ids and sit at item ids 9204 / 9458 below.
    public static final DeferredItem<BlockItem> CRYSTALSTONE = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTALSTONE); // +200
    public static final DeferredItem<BlockItem> CRYSTALCOAL = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTALCOAL); // +201
    public static final DeferredItem<BlockItem> CRYSTAL_GRASS = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTAL_GRASS); // +202, CrystalGrass.java:25
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_RED = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_FLOWER_RED); // +203, MyBlockFlower.java:22
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_GREEN = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_FLOWER_GREEN); // +204
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_BLUE = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_FLOWER_BLUE); // +205
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER_YELLOW = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_FLOWER_YELLOW); // +206
    public static final DeferredItem<BlockItem> CRYSTAL_TREE_LOG = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTAL_TREE_LOG); // +207, BlockCrystalTreeLog.java:22
    public static final DeferredItem<BlockItem> CRYSTAL_TREE_LEAVES = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_TREE_LEAVES); // +208, BlockCrystalLeaves.java:17
    public static final DeferredItem<BlockItem> CRYSTALCRYSTAL = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTALCRYSTAL); // +209
    public static final DeferredItem<BlockItem> CRYSTAL_PLANKS = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTAL_PLANKS); // +210, CrystalWood.java:14
    // W03 (w03-workshop): the lit furnace variant (+213) is folded into the one block (R2), so exactly one furnace item.
    public static final DeferredItem<BlockItem> CRYSTALWORKBENCH = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTALWORKBENCH); // +211, CrystalWorkbench.java:19 tabDecorations
    public static final DeferredItem<BlockItem> CRYSTALFURNACE = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTALFURNACE); // +212, CrystalFurnace.java:33 tabDecorations (the lit +213 block had no tab)
    public static final DeferredItem<StandingAndWallBlockItem> CRYSTAL_TORCH = item(OriginalTab.DECORATIONS, "crystaltorch",
            p -> new StandingAndWallBlockItem(ModBlocks.CRYSTAL_TORCH.get(), ModBlocks.CRYSTAL_TORCH_WALL.get(), p, Direction.DOWN)); // +214, BlockCrystalTorch.java:14
    public static final DeferredItem<BlockItem> CRYSTAL_TREE_LEAVES2 = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_TREE_LEAVES2); // +215
    public static final DeferredItem<BlockItem> CRYSTALPINK_BLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTALPINK_BLOCK); // +216
    public static final DeferredItem<BlockItem> TIGERSEYE = blockItem(OriginalTab.BLOCK, ModBlocks.TIGERSEYE); // +217
    public static final DeferredItem<BlockItem> TIGERSEYE_BLOCK = blockItem(OriginalTab.BLOCK, ModBlocks.TIGERSEYE_BLOCK); // +218
    public static final DeferredItem<BlockItem> CRYSTALRAT = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTALRAT); // +219
    public static final DeferredItem<BlockItem> CRYSTALFAIRY = blockItem(OriginalTab.BLOCK, ModBlocks.CRYSTALFAIRY); // +220
    public static final DeferredItem<BlockItem> CRYSTAL_TREE_LEAVES3 = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_TREE_LEAVES3); // +221
    public static final DeferredItem<BlockItem> CRYSTAL_SAPLING = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_SAPLING); // +222, BlockCrystalPlant.java:18
    public static final DeferredItem<BlockItem> CRYSTAL_SAPLING2 = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_SAPLING2); // +223
    public static final DeferredItem<BlockItem> CRYSTAL_SAPLING3 = blockItem(OriginalTab.DECORATIONS, ModBlocks.CRYSTAL_SAPLING3); // +224
    public static final DeferredItem<BlockItem> REDANTTROLL = blockItem(OriginalTab.BLOCK, ModBlocks.REDANTTROLL); // +225
    public static final DeferredItem<BlockItem> TERMITETROLL = blockItem(OriginalTab.BLOCK, ModBlocks.TERMITETROLL); // +226

    static {
        driedEggItems(100, OreGenericEgg.DRIED_EGG_IDS.size()); // +250 to +261, +300 to +306
        DRIED_EGG_ITEMS = Collections.unmodifiableMap(DRIED_EGG_ITEMS_MUTABLE);
    }

    // =====================================================================================
    // W02 items, legacy item-id order (manifest ctor_args[0], BaseItemID = 9000).
    //  - item.material (w02-materials-food): plain items, tabMaterials in the original class
    //    (IngotTitanium/IngotUranium/ItemSalt/ItemCrystalSticks.java:11).
    //  - item.food (w02-materials-food): (heal, saturationModifier) verbatim from OreSpawnMain.java;
    //    the modifier needs no conversion (item.food.FoodValues); tabFood from 1.7.10 ItemFood's ctor.
    //  - item.crop (w02-crops): ItemFood -> tabFood; ItemStrawberrySeed / ItemAppleSeed /
    //    ItemExperienceTreeSeed -> tabDecorations; OreSpawnMain.java:1289 tabFood, :1291 tabTools.
    //    The block references sit inside the factories: ModBlocks is initialised by then.
    // =====================================================================================

    // W11 (w11-cages): CritterCage, OreSpawnMain.java:5069-5182. new CritterCage(cage_id, p): the second
    // original ctor argument; tabMisc -> OriginalTab.MISC. Legacy ids 9000-9099 first, the rest at their ids below.
    public static final DeferredItem<CritterCage> CAGE_EMPTY = item(OriginalTab.MISC, "cageempty", p -> new CritterCage(160, p)); // 9000
    public static final DeferredItem<CritterCage> CAGE_SPIDER = item(OriginalTab.MISC, "cagespider", p -> new CritterCage(161, p)); // 9001
    public static final DeferredItem<CritterCage> CAGE_BAT = item(OriginalTab.MISC, "cagebat", p -> new CritterCage(162, p)); // 9002
    public static final DeferredItem<CritterCage> CAGE_COW = item(OriginalTab.MISC, "cagecow", p -> new CritterCage(163, p)); // 9003
    public static final DeferredItem<CritterCage> CAGE_PIG = item(OriginalTab.MISC, "cagepig", p -> new CritterCage(164, p)); // 9004
    public static final DeferredItem<CritterCage> CAGE_SQUID = item(OriginalTab.MISC, "cagesquid", p -> new CritterCage(165, p)); // 9005
    public static final DeferredItem<CritterCage> CAGE_CHICKEN = item(OriginalTab.MISC, "cagechicken", p -> new CritterCage(166, p)); // 9006
    public static final DeferredItem<CritterCage> CAGE_CREEPER = item(OriginalTab.MISC, "cagecreeper", p -> new CritterCage(167, p)); // 9007
    public static final DeferredItem<CritterCage> CAGE_SKELETON = item(OriginalTab.MISC, "cageskeleton", p -> new CritterCage(168, p)); // 9008
    public static final DeferredItem<CritterCage> CAGE_ZOMBIE = item(OriginalTab.MISC, "cagezombie", p -> new CritterCage(169, p)); // 9009
    public static final DeferredItem<CritterCage> CAGE_SLIME = item(OriginalTab.MISC, "cageslime", p -> new CritterCage(170, p)); // 9010
    public static final DeferredItem<CritterCage> CAGE_GHAST = item(OriginalTab.MISC, "cageghast", p -> new CritterCage(171, p)); // 9011
    public static final DeferredItem<CritterCage> CAGE_ZOMBIEPIGMAN = item(OriginalTab.MISC, "cagezombiepigman", p -> new CritterCage(172, p)); // 9012
    public static final DeferredItem<CritterCage> CAGE_ENDERMAN = item(OriginalTab.MISC, "cageenderman", p -> new CritterCage(173, p)); // 9013
    public static final DeferredItem<CritterCage> CAGE_CAVESPIDER = item(OriginalTab.MISC, "cagecavespider", p -> new CritterCage(174, p)); // 9014
    public static final DeferredItem<CritterCage> CAGE_SILVERFISH = item(OriginalTab.MISC, "cagesilverfish", p -> new CritterCage(175, p)); // 9015
    public static final DeferredItem<CritterCage> CAGE_MAGMACUBE = item(OriginalTab.MISC, "cagemagmacube", p -> new CritterCage(176, p)); // 9016
    public static final DeferredItem<CritterCage> CAGE_WITCH = item(OriginalTab.MISC, "cagewitch", p -> new CritterCage(177, p)); // 9017
    public static final DeferredItem<CritterCage> CAGE_SHEEP = item(OriginalTab.MISC, "cagesheep", p -> new CritterCage(178, p)); // 9018
    public static final DeferredItem<CritterCage> CAGE_WOLF = item(OriginalTab.MISC, "cagewolf", p -> new CritterCage(179, p)); // 9019
    public static final DeferredItem<CritterCage> CAGE_MOOSHROOM = item(OriginalTab.MISC, "cagemooshroom", p -> new CritterCage(180, p)); // 9020
    public static final DeferredItem<CritterCage> CAGE_OCELOT = item(OriginalTab.MISC, "cageocelot", p -> new CritterCage(181, p)); // 9021
    public static final DeferredItem<CritterCage> CAGE_BLAZE = item(OriginalTab.MISC, "cageblaze", p -> new CritterCage(182, p)); // 9022
    public static final DeferredItem<CritterCage> CAGE_GIRLFRIEND = item(OriginalTab.MISC, "cagegirlfriend", p -> new CritterCage(183, p)); // 9023
    public static final DeferredItem<CritterCage> CAGE_WITHERSKELETON = item(OriginalTab.MISC, "cagewitherskeleton", p -> new CritterCage(188, p)); // 9024
    public static final DeferredItem<CritterCage> CAGE_ENDERDRAGON = item(OriginalTab.MISC, "cageenderdragon", p -> new CritterCage(184, p)); // 9025
    public static final DeferredItem<CritterCage> CAGE_SNOWGOLEM = item(OriginalTab.MISC, "cagesnowgolem", p -> new CritterCage(185, p)); // 9026
    public static final DeferredItem<CritterCage> CAGE_IRONGOLEM = item(OriginalTab.MISC, "cageirongolem", p -> new CritterCage(186, p)); // 9027
    public static final DeferredItem<CritterCage> CAGE_WITHERBOSS = item(OriginalTab.MISC, "cagewitherboss", p -> new CritterCage(187, p)); // 9028
    public static final DeferredItem<CritterCage> CAGE_REDCOW = item(OriginalTab.MISC, "cageredcow", p -> new CritterCage(189, p)); // 9029
    public static final DeferredItem<CritterCage> CAGE_GOLDCOW = item(OriginalTab.MISC, "cagegoldcow", p -> new CritterCage(190, p)); // 9030
    public static final DeferredItem<CritterCage> CAGE_ENCHANTEDCOW = item(OriginalTab.MISC, "cageenchantedcow", p -> new CritterCage(191, p)); // 9031
    public static final DeferredItem<CritterCage> CAGE_MOTHRA = item(OriginalTab.MISC, "cagemothra", p -> new CritterCage(208, p)); // 9032
    public static final DeferredItem<CritterCage> CAGE_ALOSAURUS = item(OriginalTab.MISC, "cagealosaurus", p -> new CritterCage(209, p)); // 9033
    public static final DeferredItem<CritterCage> CAGE_CRYOLOPHOSAURUS = item(OriginalTab.MISC, "cagecryolophosaurus", p -> new CritterCage(210, p)); // 9034
    public static final DeferredItem<CritterCage> CAGE_CAMARASAURUS = item(OriginalTab.MISC, "cagecamarasaurus", p -> new CritterCage(211, p)); // 9035
    public static final DeferredItem<CritterCage> CAGE_VELOCITYRAPTOR = item(OriginalTab.MISC, "cagevelocityraptor", p -> new CritterCage(212, p)); // 9036
    public static final DeferredItem<CritterCage> CAGE_HYDROLISC = item(OriginalTab.MISC, "cagehydrolisc", p -> new CritterCage(213, p)); // 9037
    public static final DeferredItem<CritterCage> CAGE_BASILISC = item(OriginalTab.MISC, "cagebasilisc", p -> new CritterCage(214, p)); // 9038
    public static final DeferredItem<CritterCage> CAGE_DRAGONFLY = item(OriginalTab.MISC, "cagedragonfly", p -> new CritterCage(220, p)); // 9039
    public static final DeferredItem<CritterCage> CAGE_SCORPION = item(OriginalTab.MISC, "cagescorpion", p -> new CritterCage(224, p)); // 9040
    public static final DeferredItem<CritterCage> CAGE_EMPERORSCORPION = item(OriginalTab.MISC, "cageemperorscorpion", p -> new CritterCage(222, p)); // 9041
    public static final DeferredItem<CritterCage> CAGE_SPYRO = item(OriginalTab.MISC, "cagespyro", p -> new CritterCage(228, p)); // 9042
    public static final DeferredItem<CritterCage> CAGE_BARYONYX = item(OriginalTab.MISC, "cagebaryonyx", p -> new CritterCage(230, p)); // 9043
    public static final DeferredItem<CritterCage> CAGE_GAMMAMETROID = item(OriginalTab.MISC, "cagegammametroid", p -> new CritterCage(232, p)); // 9044
    public static final DeferredItem<CritterCage> CAGE_CAVEFISHER = item(OriginalTab.MISC, "cagecavefisher", p -> new CritterCage(226, p)); // 9045
    public static final DeferredItem<CritterCage> CAGE_COCKATEIL = item(OriginalTab.MISC, "cagecockateil", p -> new CritterCage(234, p)); // 9046
    public static final DeferredItem<CritterCage> CAGE_KYUUBI = item(OriginalTab.MISC, "cagekyuubi", p -> new CritterCage(236, p)); // 9047
    public static final DeferredItem<CritterCage> CAGE_ALIEN = item(OriginalTab.MISC, "cagealien", p -> new CritterCage(238, p)); // 9048
    public static final DeferredItem<CritterCage> CAGE_ATTACKSQUID = item(OriginalTab.MISC, "cageattacksquid", p -> new CritterCage(240, p)); // 9049
    public static final DeferredItem<CritterCage> CAGE_WATERDRAGON = item(OriginalTab.MISC, "cagewaterdragon", p -> new CritterCage(242, p)); // 9050
    public static final DeferredItem<CritterCage> CAGE_KRAKEN = item(OriginalTab.MISC, "cagekraken", p -> new CritterCage(244, p)); // 9051
    public static final DeferredItem<CritterCage> CAGE_LIZARD = item(OriginalTab.MISC, "cagelizard", p -> new CritterCage(246, p)); // 9052
    public static final DeferredItem<CritterCage> CAGE_CEPHADROME = item(OriginalTab.MISC, "cagecephadrome", p -> new CritterCage(248, p)); // 9053
    public static final DeferredItem<CritterCage> CAGE_DRAGON = item(OriginalTab.MISC, "cagedragon", p -> new CritterCage(250, p)); // 9054
    public static final DeferredItem<CritterCage> CAGE_BEE = item(OriginalTab.MISC, "cagebee", p -> new CritterCage(252, p)); // 9055
    public static final DeferredItem<CritterCage> CAGE_HORSE = item(OriginalTab.MISC, "cagehorse", p -> new CritterCage(253, p)); // 9056
    public static final DeferredItem<CritterCage> CAGE_FIREFLY = item(OriginalTab.MISC, "cagefirefly", p -> new CritterCage(255, p)); // 9057
    public static final DeferredItem<CritterCage> CAGE_CHIPMUNK = item(OriginalTab.MISC, "cagechipmunk", p -> new CritterCage(256, p)); // 9058
    public static final DeferredItem<CritterCage> CAGE_GAZELLE = item(OriginalTab.MISC, "cagegazelle", p -> new CritterCage(257, p)); // 9059
    public static final DeferredItem<CritterCage> CAGE_OSTRICH = item(OriginalTab.MISC, "cageostrich", p -> new CritterCage(258, p)); // 9060
    public static final DeferredItem<CritterCage> CAGE_TROOPER = item(OriginalTab.MISC, "cagetrooper", p -> new CritterCage(259, p)); // 9061
    public static final DeferredItem<CritterCage> CAGE_SPIT = item(OriginalTab.MISC, "cagespit", p -> new CritterCage(260, p)); // 9062
    public static final DeferredItem<CritterCage> CAGE_STINK = item(OriginalTab.MISC, "cagestink", p -> new CritterCage(261, p)); // 9063
    public static final DeferredItem<CritterCage> CAGE_CREEPINGHORROR = item(OriginalTab.MISC, "cagecreepinghorror", p -> new CritterCage(268, p)); // 9064
    public static final DeferredItem<CritterCage> CAGE_TERRIBLETERROR = item(OriginalTab.MISC, "cageterribleterror", p -> new CritterCage(269, p)); // 9065
    public static final DeferredItem<CritterCage> CAGE_CLIFFRACER = item(OriginalTab.MISC, "cagecliffracer", p -> new CritterCage(270, p)); // 9066
    public static final DeferredItem<CritterCage> CAGE_TRIFFID = item(OriginalTab.MISC, "cagetriffid", p -> new CritterCage(271, p)); // 9067
    public static final DeferredItem<CritterCage> CAGE_NIGHTMARE = item(OriginalTab.MISC, "cagenightmare", p -> new CritterCage(272, p)); // 9068
    public static final DeferredItem<CritterCage> CAGE_LURKINGTERROR = item(OriginalTab.MISC, "cagelurkingterror", p -> new CritterCage(273, p)); // 9069
    public static final DeferredItem<CritterCage> CAGE_SMALLWORM = item(OriginalTab.MISC, "cagesmallworm", p -> new CritterCage(281, p)); // 9070
    public static final DeferredItem<CritterCage> CAGE_MEDIUMWORM = item(OriginalTab.MISC, "cagemediumworm", p -> new CritterCage(282, p)); // 9071
    public static final DeferredItem<CritterCage> CAGE_LARGEWORM = item(OriginalTab.MISC, "cagelargeworm", p -> new CritterCage(283, p)); // 9072
    public static final DeferredItem<CritterCage> CAGE_CASSOWARY = item(OriginalTab.MISC, "cagecassowary", p -> new CritterCage(284, p)); // 9073
    public static final DeferredItem<CritterCage> CAGE_CLOUDSHARK = item(OriginalTab.MISC, "cagecloudshark", p -> new CritterCage(285, p)); // 9074
    public static final DeferredItem<CritterCage> CAGE_GOLDFISH = item(OriginalTab.MISC, "cagegoldfish", p -> new CritterCage(286, p)); // 9075
    public static final DeferredItem<CritterCage> CAGE_LEAFMONSTER = item(OriginalTab.MISC, "cageleafmonster", p -> new CritterCage(287, p)); // 9076
    public static final DeferredItem<CritterCage> CAGE_ENDERKNIGHT = item(OriginalTab.MISC, "cageenderknight", p -> new CritterCage(296, p)); // 9077
    public static final DeferredItem<CritterCage> CAGE_ENDERREAPER = item(OriginalTab.MISC, "cageenderreaper", p -> new CritterCage(297, p)); // 9078
    public static final DeferredItem<CritterCage> CAGE_BEAVER = item(OriginalTab.MISC, "cagebeaver", p -> new CritterCage(300, p)); // 9079
    public static final DeferredItem<CritterCage> CAGE_URCHIN = item(OriginalTab.MISC, "cageurchin", p -> new CritterCage(323, p)); // 9080
    public static final DeferredItem<CritterCage> CAGE_FLOUNDER = item(OriginalTab.MISC, "cageflounder", p -> new CritterCage(319, p)); // 9081
    public static final DeferredItem<CritterCage> CAGE_SKATE = item(OriginalTab.MISC, "cageskate", p -> new CritterCage(322, p)); // 9082
    public static final DeferredItem<CritterCage> CAGE_ROTATOR = item(OriginalTab.MISC, "cagerotator", p -> new CritterCage(313, p)); // 9083
    public static final DeferredItem<CritterCage> CAGE_PEACOCK = item(OriginalTab.MISC, "cagepeacock", p -> new CritterCage(315, p)); // 9084
    public static final DeferredItem<CritterCage> CAGE_FAIRY = item(OriginalTab.MISC, "cagefairy", p -> new CritterCage(316, p)); // 9085
    public static final DeferredItem<CritterCage> CAGE_DUNGEONBEAST = item(OriginalTab.MISC, "cagedungeonbeast", p -> new CritterCage(317, p)); // 9086
    public static final DeferredItem<CritterCage> CAGE_VORTEX = item(OriginalTab.MISC, "cagevortex", p -> new CritterCage(314, p)); // 9087
    public static final DeferredItem<CritterCage> CAGE_RAT = item(OriginalTab.MISC, "cagerat", p -> new CritterCage(318, p)); // 9088
    public static final DeferredItem<CritterCage> CAGE_WHALE = item(OriginalTab.MISC, "cagewhale", p -> new CritterCage(320, p)); // 9089
    public static final DeferredItem<CritterCage> CAGE_IRUKANDJI = item(OriginalTab.MISC, "cageirukandji", p -> new CritterCage(321, p)); // 9090
    public static final DeferredItem<CritterCage> CAGE_TREX = item(OriginalTab.MISC, "cagetrex", p -> new CritterCage(345, p)); // 9091
    public static final DeferredItem<CritterCage> CAGE_HERCULES = item(OriginalTab.MISC, "cagehercules", p -> new CritterCage(346, p)); // 9092
    public static final DeferredItem<CritterCage> CAGE_MANTIS = item(OriginalTab.MISC, "cagemantis", p -> new CritterCage(347, p)); // 9093
    public static final DeferredItem<CritterCage> CAGE_STINKY = item(OriginalTab.MISC, "cagestinky", p -> new CritterCage(348, p)); // 9094
    public static final DeferredItem<CritterCage> CAGE_BOYFRIEND = item(OriginalTab.MISC, "cageboyfriend", p -> new CritterCage(215, p)); // 9095
    public static final DeferredItem<CritterCage> CAGE_EASTERBUNNY = item(OriginalTab.MISC, "cageeasterbunny", p -> new CritterCage(150, p)); // 9096
    public static final DeferredItem<CritterCage> CAGE_CATERKILLER = item(OriginalTab.MISC, "cagecaterkiller", p -> new CritterCage(151, p)); // 9097
    public static final DeferredItem<CritterCage> CAGE_MOLENOID = item(OriginalTab.MISC, "cagemolenoid", p -> new CritterCage(152, p)); // 9098
    public static final DeferredItem<CritterCage> CAGE_SEAMONSTER = item(OriginalTab.MISC, "cageseamonster", p -> new CritterCage(153, p)); // 9099

    // W06 spawn eggs at their legacy item ids (manifest ctor_args[0]); tab and dispenser come from SpawnEggSetup (R9).
    // EGG_COIN (Coin drops) and EGG_PEACOCK (Peacock lays it) are referenced by name.
    public static final DeferredItem<ItemSpawnEgg> EGG_GIRLFRIEND = ITEMS.registerItem("egggirlfriend", p -> new ItemSpawnEgg(ModEntities.GIRLFRIEND, p)); // 9105, W04, OreSpawnMain.java:5188 (egg index 197), added in fix1
    public static final DeferredItem<ItemSpawnEgg> EGG_RED_COW = ITEMS.registerItem("eggredcow", p -> new ItemSpawnEgg(ModEntities.APPLE_COW, p)); // 9106
    public static final DeferredItem<ItemSpawnEgg> EGG_GOLD_COW = ITEMS.registerItem("egggoldcow", p -> new ItemSpawnEgg(ModEntities.GOLDEN_APPLE_COW, p)); // 9107
    public static final DeferredItem<ItemSpawnEgg> EGG_ENCHANTED_COW = ITEMS.registerItem("eggenchantedcow", p -> new ItemSpawnEgg(ModEntities.ENCHANTED_GOLDEN_APPLE_COW, p)); // 9108
    public static final DeferredItem<ItemSpawnEgg> EGG_MOTHRA = ITEMS.registerItem("eggmothra", p -> new ItemSpawnEgg(ModEntities.MOTHRA, p)); // 9109, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_ALOSAURUS = ITEMS.registerItem("eggalosaurus", p -> new ItemSpawnEgg(ModEntities.ALOSAURUS, p)); // 9110, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_CRYOLOPHOSAURUS = ITEMS.registerItem("eggcryolophosaurus", p -> new ItemSpawnEgg(ModEntities.CRYOLOPHOSAURUS, p)); // 9111, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_CAMARASAURUS = ITEMS.registerItem("eggcamarasaurus", p -> new ItemSpawnEgg(ModEntities.CAMARASAURUS, p)); // 9112
    public static final DeferredItem<ItemSpawnEgg> EGG_VELOCITY_RAPTOR = ITEMS.registerItem("eggvelocityraptor", p -> new ItemSpawnEgg(ModEntities.VELOCITY_RAPTOR, p)); // 9113
    public static final DeferredItem<ItemSpawnEgg> EGG_HYDROLISC = ITEMS.registerItem("egghydrolisc", p -> new ItemSpawnEgg(ModEntities.HYDROLISC, p)); // 9114
    public static final DeferredItem<ItemSpawnEgg> EGG_BASILISK = ITEMS.registerItem("eggbasilisc", p -> new ItemSpawnEgg(ModEntities.BASILISK, p)); // 9115, W07, OreSpawnMain.java:5199 (egg index 207), added in fix1
    public static final DeferredItem<ItemSpawnEgg> EGG_DRAGONFLY = ITEMS.registerItem("eggdragonfly", p -> new ItemSpawnEgg(ModEntities.DRAGONFLY, p)); // 9116
    public static final DeferredItem<ItemSpawnEgg> EGG_EMPEROR_SCORPION = ITEMS.registerItem("eggemperorscorpion", p -> new ItemSpawnEgg(ModEntities.EMPEROR_SCORPION, p)); // 9117, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_SCORPION = ITEMS.registerItem("eggscorpion", p -> new ItemSpawnEgg(ModEntities.SCORPION, p)); // 9118, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_CAVE_FISHER = ITEMS.registerItem("eggcavefisher", p -> new ItemSpawnEgg(ModEntities.CAVE_FISHER, p)); // 9119, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_SPYRO = ITEMS.registerItem("eggspyro", p -> new ItemSpawnEgg(ModEntities.BABY_DRAGON, p)); // 9120, W09, "Spawn Baby Dragon"
    public static final DeferredItem<ItemSpawnEgg> EGG_BARYONYX = ITEMS.registerItem("eggbaryonyx", p -> new ItemSpawnEgg(ModEntities.BARYONYX, p)); // 9121
    public static final DeferredItem<ItemSpawnEgg> EGG_GAMMA_METROID = ITEMS.registerItem("egggammametroid", p -> new ItemSpawnEgg(ModEntities.WTF, p)); // 9122, W09
    public static final DeferredItem<ItemSpawnEgg> EGG_COCKATEIL = ITEMS.registerItem("eggcockateil", p -> new ItemSpawnEgg(ModEntities.BIRD, p)); // 9123, "Bird"
    public static final DeferredItem<ItemSpawnEgg> EGG_KYUUBI = ITEMS.registerItem("eggkyuubi", p -> new ItemSpawnEgg(ModEntities.KYUUBI, p)); // 9124, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_ALIEN = ITEMS.registerItem("eggalien", p -> new ItemSpawnEgg(ModEntities.ALIEN, p)); // 9125, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_ATTACK_SQUID = ITEMS.registerItem("eggattacksquid", p -> new ItemSpawnEgg(ModEntities.ATTACK_SQUID, p)); // 9126, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_WATER_DRAGON = ITEMS.registerItem("eggwaterdragon", p -> new ItemSpawnEgg(ModEntities.WATER_DRAGON, p)); // 9127, W09
    public static final DeferredItem<ItemSpawnEgg> EGG_KRAKEN = ITEMS.registerItem("eggkraken", p -> new ItemSpawnEgg(ModEntities.THE_KRAKEN, p)); // 9128, W10
    public static final DeferredItem<ItemSpawnEgg> EGG_LIZARD = ITEMS.registerItem("egglizard", p -> new ItemSpawnEgg(ModEntities.LIZARD, p)); // 9129
    public static final DeferredItem<ItemSpawnEgg> EGG_CEPHADROME = ITEMS.registerItem("eggcephadrome", p -> new ItemSpawnEgg(ModEntities.CEPHADROME, p)); // 9130, W09
    public static final DeferredItem<ItemSpawnEgg> EGG_DRAGON = ITEMS.registerItem("eggdragon", p -> new ItemSpawnEgg(ModEntities.DRAGON, p)); // 9131, W09
    public static final DeferredItem<ItemSpawnEgg> EGG_BEE = ITEMS.registerItem("eggbee", p -> new ItemSpawnEgg(ModEntities.BEE, p)); // 9132, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_TROOPER_BUG = ITEMS.registerItem("eggtrooper", p -> new ItemSpawnEgg(ModEntities.JUMPY_BUG, p)); // 9133, W07, "Spawn Jumpy Bug"
    public static final DeferredItem<ItemSpawnEgg> EGG_SPIT_BUG = ITEMS.registerItem("eggspit", p -> new ItemSpawnEgg(ModEntities.SPIT_BUG, p)); // 9134, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_STINK_BUG = ITEMS.registerItem("eggstink", p -> new ItemSpawnEgg(ModEntities.STINK_BUG, p)); // 9135
    public static final DeferredItem<ItemSpawnEgg> EGG_OSTRICH = ITEMS.registerItem("eggostrich", p -> new ItemSpawnEgg(ModEntities.OSTRICH, p)); // 9136
    public static final DeferredItem<ItemSpawnEgg> EGG_GAZELLE = ITEMS.registerItem("egggazelle", p -> new ItemSpawnEgg(ModEntities.GAZELLE, p)); // 9137
    public static final DeferredItem<ItemSpawnEgg> EGG_CHIPMUNK = ITEMS.registerItem("eggchipmunk", p -> new ItemSpawnEgg(ModEntities.CHIPMUNK, p)); // 9138
    public static final DeferredItem<ItemSpawnEgg> EGG_CREEPING_HORROR = ITEMS.registerItem("eggcreepinghorror", p -> new ItemSpawnEgg(ModEntities.CREEPING_HORROR, p)); // 9139, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_TERRIBLE_TERROR = ITEMS.registerItem("eggterribleterror", p -> new ItemSpawnEgg(ModEntities.TERRIBLE_TERROR, p)); // 9140, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_CLIFF_RACER = ITEMS.registerItem("eggcliffracer", p -> new ItemSpawnEgg(ModEntities.CLIFF_RACER, p)); // 9141
    public static final DeferredItem<ItemSpawnEgg> EGG_TRIFFID = ITEMS.registerItem("eggtriffid", p -> new ItemSpawnEgg(ModEntities.TRIFFID, p)); // 9142, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_NIGHTMARE = ITEMS.registerItem("eggnightmare", p -> new ItemSpawnEgg(ModEntities.NIGHTMARE, p)); // 9143, W08, "Spawn Nightmare!!!"
    public static final DeferredItem<ItemSpawnEgg> EGG_LURKING_TERROR = ITEMS.registerItem("egglurkingterror", p -> new ItemSpawnEgg(ModEntities.LURKING_TERROR, p)); // 9144, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_GODZILLA = ITEMS.registerItem("egggodzilla", p -> new ItemSpawnEgg(ModEntities.MOBZILLA, p)); // 9145, W10, "Spawn Mobzilla"
    public static final DeferredItem<ItemSpawnEgg> EGG_SMALL_WORM = ITEMS.registerItem("eggsmallworm", p -> new ItemSpawnEgg(ModEntities.SMALL_WORM, p)); // 9146, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_MEDIUM_WORM = ITEMS.registerItem("eggmediumworm", p -> new ItemSpawnEgg(ModEntities.MEDIUM_WORM, p)); // 9147, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_LARGE_WORM = ITEMS.registerItem("egglargeworm", p -> new ItemSpawnEgg(ModEntities.LARGE_WORM, p)); // 9148, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_CASSOWARY = ITEMS.registerItem("eggcassowary", p -> new ItemSpawnEgg(ModEntities.CASSOWARY, p)); // 9149

    public static final DeferredItem<ItemSalt> URANIUM_NUGGET = item(OriginalTab.MATERIALS, "uranium_nugget", ItemSalt::new); // 9150
    public static final DeferredItem<ItemSalt> TITANIUM_NUGGET = item(OriginalTab.MATERIALS, "titanium_nugget", ItemSalt::new); // 9151
    public static final DeferredItem<IngotUranium> INGOT_URANIUM = item(OriginalTab.MATERIALS, "ingoturanium", IngotUranium::new); // 9152
    public static final DeferredItem<IngotTitanium> INGOT_TITANIUM = item(OriginalTab.MATERIALS, "ingottitanium", IngotTitanium::new); // 9153
    public static final DeferredItem<ItemSalt> GREEN_GOO = item(OriginalTab.MATERIALS, "greengoo", ItemSalt::new); // 9154
    public static final DeferredItem<ItemSalt> DEAD_STINK_BUG = item(OriginalTab.MATERIALS, "deadstinkbug", ItemSalt::new); // 9155
    public static final DeferredItem<ItemSalt> MOTH_SCALE = item(OriginalTab.MATERIALS, "mothscale", ItemSalt::new); // 9156
    public static final DeferredItem<ItemLavaEel> LAVA_EEL = item(OriginalTab.FOOD, "lavaeel", p -> new ItemLavaEel(2, 0.6f, p)); // 9157
    public static final DeferredItem<ItemSalt> NIGHTMARE_SCALE = item(OriginalTab.MATERIALS, "nightmarescale", ItemSalt::new); // 9158
    public static final DeferredItem<ItemSalt> EMPEROR_SCORPION_SCALE = item(OriginalTab.MATERIALS, "emperorscorpionscale", ItemSalt::new); // 9159
    public static final DeferredItem<ItemSalt> BASILISK_SCALE = item(OriginalTab.MATERIALS, "basiliskscale", ItemSalt::new); // 9160
    public static final DeferredItem<ItemSalt> WATER_DRAGON_SCALE = item(OriginalTab.MATERIALS, "waterdragonscale", ItemSalt::new); // 9161
    public static final DeferredItem<ItemSalt> JUMPY_BUG_SCALE = item(OriginalTab.MATERIALS, "jumpybugscale", ItemSalt::new); // 9162
    public static final DeferredItem<ItemSalt> KRAKEN_TOOTH = item(OriginalTab.MATERIALS, "krakentooth", ItemSalt::new); // 9163
    public static final DeferredItem<ItemSalt> GODZILLA_SCALE = item(OriginalTab.MATERIALS, "godzillascale", ItemSalt::new); // 9164
    public static final DeferredItem<ItemSpawnEgg> EGG_CLOUD_SHARK = ITEMS.registerItem("eggcloudshark", p -> new ItemSpawnEgg(ModEntities.CLOUD_SHARK, p)); // 9165, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_GOLD_FISH = ITEMS.registerItem("egggoldfish", p -> new ItemSpawnEgg(ModEntities.GOLD_FISH, p)); // 9166, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_LEAF_MONSTER = ITEMS.registerItem("eggleafmonster", p -> new ItemSpawnEgg(ModEntities.LEAF_MONSTER, p)); // 9167, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_TSHIRT = ITEMS.registerItem("eggtshirt", p -> new ItemSpawnEgg(ModEntities.T_SHIRT, p)); // 9168, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_ENDER_KNIGHT = ITEMS.registerItem("eggenderknight", p -> new ItemSpawnEgg(ModEntities.ENDER_KNIGHT, p)); // 9169, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_ENDER_REAPER = ITEMS.registerItem("eggenderreaper", p -> new ItemSpawnEgg(ModEntities.ENDER_REAPER, p)); // 9170, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_BEAVER = ITEMS.registerItem("eggbeaver", p -> new ItemSpawnEgg(ModEntities.BEAVER, p)); // 9171, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_ROBOT5 = ITEMS.registerItem("eggrobot5", p -> new ItemSpawnEgg(ModEntities.ROBO_SNIPER, p)); // 9172, W07, "Robo-Sniper"
    public static final DeferredItem<ItemSpawnEgg> EGG_COIN = ITEMS.registerItem("eggcoin", p -> new ItemSpawnEgg(ModEntities.COIN, p)); // 9173, W06
    public static final DeferredItem<CritterCage> CAGE_SEAVIPER = item(OriginalTab.MISC, "cageseaviper", p -> new CritterCage(154, p)); // 9174
    public static final DeferredItem<ItemFireFish> FIRE_FISH = item(OriginalTab.FOOD, "firefish", p -> new ItemFireFish(4, 0.6f, p)); // 9175
    public static final DeferredItem<ItemSunFish> SUN_FISH = item(OriginalTab.FOOD, "sunfish", p -> new ItemSunFish(ItemSunFish.Variant.SUN_FISH, 6, 0.6f, p)); // 9176
    public static final DeferredItem<ItemSparkFish> SPARK_FISH = item(OriginalTab.FOOD, "sparkfish", p -> new ItemSparkFish(1, 0.2f, p)); // 9177
    public static final DeferredItem<ItemSalt> SALT = item(OriginalTab.MATERIALS, "salt", ItemSalt::new); // 9178
    public static final DeferredItem<ItemPopcorn> POPCORN = item(OriginalTab.FOOD, "popcorn", p -> new ItemPopcorn(1, 0.5f, p)); // 9179
    public static final DeferredItem<ItemPopcorn> POPCORN_BUTTERED = item(OriginalTab.FOOD, "popcorn_buttered", p -> new ItemPopcorn(2, 0.6f, p)); // 9180
    public static final DeferredItem<ItemPopcorn> POPCORN_BUTTERED_SALTED = item(OriginalTab.FOOD, "popcorn_buttered_salted", p -> new ItemPopcorn(3, 0.75f, p)); // 9181
    public static final DeferredItem<ItemPopcorn> POPCORN_BAG = item(OriginalTab.FOOD, "popcorn_bag", p -> new ItemPopcorn(10, 1.25f, p)); // 9182
    public static final DeferredItem<ItemPopcorn> BUTTER = item(OriginalTab.FOOD, "butter", p -> new ItemPopcorn(1, 0.5f, p)); // 9183
    public static final DeferredItem<ItemStrawberry> STRAWBERRY = item(OriginalTab.FOOD, "strawberry", p -> new ItemStrawberry(2, 0.65f, p)); // 9184, :1547
    public static final DeferredItem<ItemCornCob> CORN_SEED = item(OriginalTab.FOOD, "corn_seed", p -> new ItemCornCob(ModBlocks.CORN_0.get(), 6, 0.75f, p)); // 9185, :1571
    public static final DeferredItem<ItemPopcorn> CORNDOG_COOKED = item(OriginalTab.FOOD, "corndog_cooked", p -> new ItemPopcorn(16, 2.5f, p)); // 9186
    public static final DeferredItem<ItemPopcorn> CORNDOG_RAW = item(OriginalTab.FOOD, "corndog_raw", p -> new ItemPopcorn(4, 0.6f, p)); // 9187
    public static final DeferredItem<ItemSunFish> BUTTER_CANDY = item(OriginalTab.FOOD, "buttercandy", p -> new ItemSunFish(ItemSunFish.Variant.BUTTER_CANDY, 4, 0.5f, p)); // 9188
    public static final DeferredItem<ItemSunFish> COOKED_BACON = item(OriginalTab.FOOD, "cookedbacon", p -> new ItemSunFish(ItemSunFish.Variant.BACON, 14, 1.5f, p)); // 9189
    public static final DeferredItem<ItemPopcorn> BACON = item(OriginalTab.FOOD, "bacon", p -> new ItemPopcorn(8, 1.0f, p)); // 9190
    public static final DeferredItem<ItemGenericFish> GREEN_FISH = item(OriginalTab.FOOD, "greenfish", p -> new ItemGenericFish(3, 0.5f, p)); // 9191
    public static final DeferredItem<ItemGenericFish> BLUE_FISH = item(OriginalTab.FOOD, "bluefish", p -> new ItemGenericFish(4, 0.4f, p)); // 9192
    public static final DeferredItem<ItemGenericFish> PINK_FISH = item(OriginalTab.FOOD, "pinkfish", p -> new ItemGenericFish(4, 0.6f, p)); // 9193
    public static final DeferredItem<ItemGenericFish> ROCK_FISH = item(OriginalTab.FOOD, "rockfish", p -> new ItemGenericFish(3, 0.7f, p)); // 9194
    public static final DeferredItem<ItemGenericFish> WOOD_FISH = item(OriginalTab.FOOD, "woodfish", p -> new ItemGenericFish(5, 0.7f, p)); // 9195
    public static final DeferredItem<ItemGenericFish> GREY_FISH = item(OriginalTab.FOOD, "greyfish", p -> new ItemGenericFish(5, 0.5f, p)); // 9196
    public static final DeferredItem<ItemTomato> TOMATO_SEED = item(OriginalTab.FOOD, "tomato_seed", p -> new ItemTomato(ModBlocks.TOMATO_0.get(), 4, 0.55f, p)); // 9197, :1581
    public static final DeferredItem<ItemLettuce> LETTUCE_SEED = item(OriginalTab.FOOD, "lettuce_seed", p -> new ItemLettuce(ModBlocks.LETTUCE_0.get(), 3, 0.45f, p)); // 9198, :1586
    public static final DeferredItem<ItemRadish> RADISH = item(OriginalTab.FOOD, "radish", p -> new ItemRadish(ModBlocks.RADISH_PLANT.get(), 2, 0.45f, p)); // 9199, :1559
    public static final DeferredItem<ItemPopcorn> SALAD = item(OriginalTab.FOOD, "salad", p -> new ItemPopcorn(10, 0.95f, p)); // 9200
    public static final DeferredItem<ItemPopcorn> BLT_SANDWICH = item(OriginalTab.FOOD, "blt_sandwich", p -> new ItemPopcorn(12, 0.95f, p)); // 9201
    public static final DeferredItem<ItemStrawberry> CHERRIES = item(OriginalTab.FOOD, "cherries", p -> new ItemStrawberry(3, 0.45f, p)); // 9202, :1560
    public static final DeferredItem<ItemStrawberry> PEACH = item(OriginalTab.FOOD, "peach", p -> new ItemStrawberry(4, 0.55f, p)); // 9203, :1561
    public static final DeferredItem<ItemPizza> PIZZA_ITEM = item(OriginalTab.FOOD, "pizza_item", p -> new ItemPizza(ModBlocks.PIZZA.get(), p)); // 9204, :1289 stack 1
    public static final DeferredItem<ItemPopcorn> CHEESE = item(OriginalTab.FOOD, "cheese", p -> new ItemPopcorn(4, 0.5f, p)); // 9205
    public static final DeferredItem<ItemPopcorn> RAW_PEACOCK = item(OriginalTab.FOOD, "rawpeacock", p -> new ItemPopcorn(6, 0.7f, p)); // 9206
    public static final DeferredItem<ItemPopcorn> COOKED_PEACOCK = item(OriginalTab.FOOD, "cookedpeacock", p -> new ItemPopcorn(12, 1.4f, p)); // 9207
    public static final DeferredItem<ItemSunFish> CRYSTAL_APPLE = item(OriginalTab.FOOD, "crystalapple", p -> new ItemSunFish(ItemSunFish.Variant.CRYSTAL_APPLE, 5, 0.85f, p)); // 9208
    public static final DeferredItem<ItemRadish> RICE = item(OriginalTab.FOOD, "rice", p -> new ItemRadish(ModBlocks.RICE_PLANT.get(), 5, 0.65f, p)); // 9209, :1565
    public static final DeferredItem<ItemStrawberrySeed> STRAWBERRY_SEED = item(OriginalTab.DECORATIONS, "strawberry_seed", p -> new ItemStrawberrySeed(ModBlocks.STRAWBERRY_PLANT.get(), p)); // 9210, :1549
    public static final DeferredItem<ItemAppleSeed> APPLETREE_SEED = item(OriginalTab.DECORATIONS, "appletree_seed", p -> new ItemAppleSeed(ItemAppleSeed.Kind.APPLE, p)); // 9211, :1606 stack 16
    // W05 (w05-flyers): the insect seeds, tabDecorations (ItemButterflySeed.java:13 etc.).
    public static final DeferredItem<ItemButterflySeed> BUTTERFLY_SEED = item(OriginalTab.DECORATIONS, "butterfly_seed", p -> new ItemButterflySeed(ModBlocks.BUTTERFLY_PLANT.get(), p)); // 9212, :1551
    public static final DeferredItem<ItemMothSeed> MOTH_SEED = item(OriginalTab.DECORATIONS, "moth_seed", p -> new ItemMothSeed(ModBlocks.MOTH_PLANT.get(), p)); // 9213, :1553
    public static final DeferredItem<ItemMosquitoSeed> MOSQUITO_SEED = item(OriginalTab.DECORATIONS, "mosquito_seed", p -> new ItemMosquitoSeed(ModBlocks.MOSQUITO_PLANT.get(), p)); // 9214, :1555
    public static final DeferredItem<ItemFireflySeed> FIREFLY_SEED = item(OriginalTab.DECORATIONS, "firefly_seed", p -> new ItemFireflySeed(ModBlocks.FIREFLY_PLANT.get(), p)); // 9215, :1557
    public static final DeferredItem<ItemExperienceTreeSeed> EXPERIENCETREE_SEED = item(OriginalTab.DECORATIONS, "experiencetree_seed", ItemExperienceTreeSeed::new); // 9216, :1611 stack 1
    public static final DeferredItem<ItemAppleSeed> CHERRYTREE_SEED = item(OriginalTab.DECORATIONS, "cherrytree_seed", p -> new ItemAppleSeed(ItemAppleSeed.Kind.CHERRY, p)); // 9217, :1621
    public static final DeferredItem<ItemAppleSeed> PEACHTREE_SEED = item(OriginalTab.DECORATIONS, "peachtree_seed", p -> new ItemAppleSeed(ItemAppleSeed.Kind.PEACH, p)); // 9218, :1622
    public static final DeferredItem<ItemSpawnEgg> EGG_ROTATOR = ITEMS.registerItem("eggrotator", p -> new ItemSpawnEgg(ModEntities.ROTATOR, p)); // 9219, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_PEACOCK = ITEMS.registerItem("eggpeacock", p -> new ItemSpawnEgg(ModEntities.PEACOCK, p)); // 9220, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_FAIRY = ITEMS.registerItem("eggfairy", p -> new ItemSpawnEgg(ModEntities.FAIRY, p)); // 9221, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_DUNGEON_BEAST = ITEMS.registerItem("eggdungeonbeast", p -> new ItemSpawnEgg(ModEntities.DUNGEON_BEAST, p)); // 9222, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_VORTEX = ITEMS.registerItem("eggvortex", p -> new ItemSpawnEgg(ModEntities.VORTEX, p)); // 9223, W08
    public static final DeferredItem<ItemCornCob> QUINOA = item(OriginalTab.FOOD, "quinoa", p -> new ItemCornCob(ModBlocks.QUINOA_0.get(), 7, 0.85f, p)); // 9224, :1576
    public static final DeferredItem<ItemSpawnEgg> EGG_TREX = ITEMS.registerItem("eggtrex", p -> new ItemSpawnEgg(ModEntities.T_REX, p)); // 9225, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_HERCULES_BEETLE = ITEMS.registerItem("egghercules", p -> new ItemSpawnEgg(ModEntities.HERCULES_BEETLE, p)); // 9226, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_MANTIS = ITEMS.registerItem("eggmantis", p -> new ItemSpawnEgg(ModEntities.MANTIS, p)); // 9227, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_STINKY = ITEMS.registerItem("eggstinky", p -> new ItemSpawnEgg(ModEntities.STINKY, p)); // 9228, W09

    // =====================================================================================
    // W03 items, interleaved with the W02 holders by legacy item id (manifest ctor_args[0]).
    //  - item.utility (w03-utility): one-use builders, cages, catchers; tab from the original class.
    //  - item.tool (w03-tools): every constructor takes (Material, Properties) like the original
    //    (id, ToolMaterial); classes that called setMaxDamage wrap the tier via Material.withUses.
    //    Registration lines OreSpawnMain.java:1307-1361.
    //  - item.armor (w03-armor): armor(id, set, type), OreSpawnMain.java:1446-1501.
    //  W04 holders sit among them by the same rule: item.ranged (w04-throwables), item.rock (w04-rocks),
    //  item.bow / item.bertha (w04-bows-bertha), item.companion (w04-companions), item.vehicle
    //  (w04-hoverboard). Stack size and durability are set inside the item constructors.
    // =====================================================================================

    public static final DeferredItem<ItemZooKeeper> ZOO_KEEPER = item(OriginalTab.DECORATIONS, "zookeeper", ItemZooKeeper::new); // 9230, :1589, ItemZooKeeper.java:13
    public static final DeferredItem<StepUp> STEP_UP = item(OriginalTab.TOOLS, "step_up", StepUp::new); // 9232, :1590, StepUp.java:17
    public static final DeferredItem<StepDown> STEP_DOWN = item(OriginalTab.TOOLS, "step_down", StepDown::new); // 9233, :1591
    public static final DeferredItem<StepAccross> STEP_ACCROSS = item(OriginalTab.TOOLS, "step_accross", StepAccross::new); // 9234, :1592
    public static final DeferredItem<ItemElevator> ELEVATOR = item(OriginalTab.TRANSPORT, "elevator", ItemElevator::new); // 9235, :1566, ItemElevator.java:13-16
    public static final DeferredItem<com.swbr.orespawn.item.magic.ItemMagicApple> MAGIC_APPLE = item(OriginalTab.DECORATIONS, "magicapple", com.swbr.orespawn.item.magic.ItemMagicApple::new); // 9236, :1587, ItemMagicApple.java:31-32
    public static final DeferredItem<ItemMinersDream> MINERS_DREAM =item(OriginalTab.REDSTONE, "minersdream", ItemMinersDream::new); // 9237, :1588, ItemMinersDream.java:17
    public static final DeferredItem<ExperienceCatcher> EXPERIENCE_CATCHER = item(OriginalTab.TOOLS, "experiencecatcher", ExperienceCatcher::new); // 9238, :1610, ExperienceCatcher.java:19
    public static final DeferredItem<ItemIceBall> ICE_BALL = item(OriginalTab.COMBAT, "iceball", ItemIceBall::new); // 9239, :1395
    public static final DeferredItem<ItemThunderStaff> THUNDER_STAFF = item(OriginalTab.COMBAT, "thunderstaff", ItemThunderStaff::new); // 9240, :1409, stack 1 / 50 durability in the item constructor
    public static final DeferredItem<PoisonSword> POISON_SWORD = item(OriginalTab.COMBAT, "poisonsword", p -> new PoisonSword(Material.REALEMERALD, p)); // 9241, :1326
    public static final DeferredItem<ItemLaserBall> LASER_BALL = item(OriginalTab.COMBAT, "laserball", ItemLaserBall::new); // 9242, :1394
    public static final DeferredItem<ItemRayGun> RAY_GUN = item(OriginalTab.COMBAT, "raygun", ItemRayGun::new); // 9243, :1408, stack 1 / 50 durability in the item constructor
    public static final DeferredItem<ItemWaterBall> WATER_BALL = item(OriginalTab.COMBAT, "waterball", ItemWaterBall::new); // 9244, :1393 (9245 has no item in the manifest)
    public static final DeferredItem<ItemSunspotUrchin> SUNSPOT_URCHIN = item(OriginalTab.COMBAT, "sunspoturchin", ItemSunspotUrchin::new); // 9246, :1391
    public static final DeferredItem<ItemAcid> ACID = item(OriginalTab.COMBAT, "acid", ItemAcid::new); // 9247, :1411
    // ItemShoes(shoe type) from manifest ctor_args[1], tabDecorations, stack 64.
    public static final DeferredItem<ItemShoes> RED_HEELS = item(OriginalTab.DECORATIONS, "redheels", p -> new ItemShoes(2, p)); // 9248, :1362
    public static final DeferredItem<ItemShoes> BLACK_HEELS = item(OriginalTab.DECORATIONS, "blackheels", p -> new ItemShoes(3, p)); // 9249, :1363
    public static final DeferredItem<ItemShoes> SLIPPERS = item(OriginalTab.DECORATIONS, "slippers", p -> new ItemShoes(4, p)); // 9250, :1364
    public static final DeferredItem<ItemShoes> BOOTS = item(OriginalTab.DECORATIONS, "boots", p -> new ItemShoes(5, p)); // 9251, :1365
    public static final DeferredItem<ItemCreeperLauncher> CREEPER_LAUNCHER = item(OriginalTab.REDSTONE, "creeperlauncher", ItemCreeperLauncher::new); // 9252, :1388, ItemCreeperLauncher.java:14
    public static final DeferredItem<ItemNetherLost> NETHER_LOST = item(OriginalTab.DECORATIONS, "netherlost", ItemNetherLost::new); // 9253, :1389, ItemNetherLost.java:19
    public static final DeferredItem<ItemCrystalSticks> CRYSTAL_STICKS = item(OriginalTab.MATERIALS, "crystalsticks", ItemCrystalSticks::new); // 9254
    public static final DeferredItem<ItemSalt> PEACOCK_FEATHER = item(OriginalTab.MATERIALS, "peacockfeather", ItemSalt::new); // 9255
    public static final DeferredItem<RatSword> RAT_SWORD = item(OriginalTab.COMBAT, "ratsword", p -> new RatSword(Material.REALEMERALD, p)); // 9256, :1327, RatSword.java:21, W08
    public static final DeferredItem<FairySword> FAIRY_SWORD = item(OriginalTab.COMBAT, "fairysword", p -> new FairySword(Material.REALEMERALD, p)); // 9257, :1328, FairySword.java:21, W07
    public static final DeferredItem<ItemIrukandji> DEAD_IRUKANDJI = item(OriginalTab.COMBAT, "deadirukandji", ItemIrukandji::new); // 9258, :1412
    public static final DeferredItem<ItemShoes> GAME_CONTROLLER = item(OriginalTab.DECORATIONS, "gamecontroller", p -> new ItemShoes(6, p)); // 9259, :1366
    public static final DeferredItem<ItemSalt> AMETHYST = item(OriginalTab.MATERIALS, "amethyst", ItemSalt::new); // 9260
    public static final DeferredItem<AmethystSword> AMETHYST_SWORD = item(OriginalTab.COMBAT, "amethystsword", p -> new AmethystSword(Material.AMETHYST, p)); // 9261, :1336
    public static final DeferredItem<AmethystPickaxe> AMETHYST_PICKAXE = item(OriginalTab.TOOLS, "amethystpickaxe", p -> new AmethystPickaxe(Material.AMETHYST, p)); // 9262, :1337
    public static final DeferredItem<AmethystShovel> AMETHYST_SHOVEL = item(OriginalTab.TOOLS, "amethystshovel", p -> new AmethystShovel(Material.AMETHYST, p)); // 9263, :1338
    public static final DeferredItem<AmethystHoe> AMETHYST_HOE = item(OriginalTab.TOOLS, "amethysthoe", p -> new AmethystHoe(Material.AMETHYST, p)); // 9264, :1339
    public static final DeferredItem<AmethystAxe> AMETHYST_AXE = item(OriginalTab.TOOLS, "amethystaxe", p -> new AmethystAxe(Material.AMETHYST, p)); // 9265, :1340
    public static final DeferredItem<ItemOreSpawnArmor> AMETHYST_HELMET = armor("amethyst_helmet", ArmorSet.AMETHYST, ArmorItem.Type.HELMET); // 9266, :1470
    public static final DeferredItem<ItemOreSpawnArmor> AMETHYST_CHEST = armor("amethyst_chest", ArmorSet.AMETHYST, ArmorItem.Type.CHESTPLATE); // 9267, :1471
    public static final DeferredItem<ItemOreSpawnArmor> AMETHYST_LEGGINGS = armor("amethyst_leggings", ArmorSet.AMETHYST, ArmorItem.Type.LEGGINGS); // 9268, :1472
    public static final DeferredItem<ItemOreSpawnArmor> AMETHYST_BOOTS = armor("amethyst_boots", ArmorSet.AMETHYST, ArmorItem.Type.BOOTS); // 9269, :1473
    public static final DeferredItem<ItemSalt> RUBY = item(OriginalTab.MATERIALS, "ruby", ItemSalt::new); // 9270
    public static final DeferredItem<RubySword> RUBY_SWORD = item(OriginalTab.COMBAT, "rubysword", p -> new RubySword(Material.RUBY, p)); // 9271, :1331
    public static final DeferredItem<RubyPickaxe> RUBY_PICKAXE = item(OriginalTab.TOOLS, "rubypickaxe", p -> new RubyPickaxe(Material.RUBY, p)); // 9272, :1332
    public static final DeferredItem<RubyShovel> RUBY_SHOVEL = item(OriginalTab.TOOLS, "rubyshovel", p -> new RubyShovel(Material.RUBY, p)); // 9273, :1333
    public static final DeferredItem<RubyHoe> RUBY_HOE = item(OriginalTab.TOOLS, "rubyhoe", p -> new RubyHoe(Material.RUBY, p)); // 9274, :1334
    public static final DeferredItem<RubyAxe> RUBY_AXE = item(OriginalTab.TOOLS, "rubyaxe", p -> new RubyAxe(Material.RUBY, p)); // 9275, :1335
    public static final DeferredItem<ItemOreSpawnArmor> RUBY_HELMET = armor("ruby_helmet", ArmorSet.RUBY, ArmorItem.Type.HELMET); // 9276, :1466
    public static final DeferredItem<ItemOreSpawnArmor> RUBY_CHEST = armor("ruby_chest", ArmorSet.RUBY, ArmorItem.Type.CHESTPLATE); // 9277, :1467
    public static final DeferredItem<ItemOreSpawnArmor> RUBY_LEGGINGS = armor("ruby_leggings", ArmorSet.RUBY, ArmorItem.Type.LEGGINGS); // 9278, :1468
    public static final DeferredItem<ItemOreSpawnArmor> RUBY_BOOTS = armor("ruby_boots", ArmorSet.RUBY, ArmorItem.Type.BOOTS); // 9279, :1469
    public static final DeferredItem<EmeraldSword> EMERALD_SWORD = item(OriginalTab.COMBAT, "emeraldsword", p -> new EmeraldSword(Material.REALEMERALD, p)); // 9280, :1320
    public static final DeferredItem<EmeraldPickaxe> EMERALD_PICKAXE = item(OriginalTab.TOOLS, "emeraldpickaxe", p -> new EmeraldPickaxe(Material.REALEMERALD, p)); // 9281, :1321
    public static final DeferredItem<EmeraldShovel> EMERALD_SHOVEL = item(OriginalTab.TOOLS, "emeraldshovel", p -> new EmeraldShovel(Material.REALEMERALD, p)); // 9282, :1322
    public static final DeferredItem<EmeraldHoe> EMERALD_HOE = item(OriginalTab.TOOLS, "emeraldhoe", p -> new EmeraldHoe(Material.REALEMERALD, p)); // 9283, :1323
    public static final DeferredItem<EmeraldAxe> EMERALD_AXE = item(OriginalTab.TOOLS, "emeraldaxe", p -> new EmeraldAxe(Material.REALEMERALD, p)); // 9284, :1324
    public static final DeferredItem<ItemOreSpawnArmor> EMERALD_HELMET = armor("emerald_helmet", ArmorSet.EMERALD, ArmorItem.Type.HELMET); // 9285, :1458
    public static final DeferredItem<ItemOreSpawnArmor> EMERALD_CHEST = armor("emerald_chest", ArmorSet.EMERALD, ArmorItem.Type.CHESTPLATE); // 9286, :1459
    public static final DeferredItem<ItemOreSpawnArmor> EMERALD_LEGGINGS = armor("emerald_leggings", ArmorSet.EMERALD, ArmorItem.Type.LEGGINGS); // 9287, :1460
    public static final DeferredItem<ItemOreSpawnArmor> EMERALD_BOOTS = armor("emerald_boots", ArmorSet.EMERALD, ArmorItem.Type.BOOTS); // 9288, :1461
    public static final DeferredItem<ExperienceSword> EXPERIENCE_SWORD = item(OriginalTab.COMBAT, "experiencesword", p -> new ExperienceSword(Material.REALEMERALD, p)); // 9289, :1325
    public static final DeferredItem<ItemOreSpawnArmor> EXPERIENCE_HELMET = armor("experience_helmet", ArmorSet.EXPERIENCE, ArmorItem.Type.HELMET); // 9290, :1462
    public static final DeferredItem<ItemOreSpawnArmor> EXPERIENCE_CHEST = armor("experience_chest", ArmorSet.EXPERIENCE, ArmorItem.Type.CHESTPLATE); // 9291, :1463
    public static final DeferredItem<ItemOreSpawnArmor> EXPERIENCE_LEGGINGS = armor("experience_leggings", ArmorSet.EXPERIENCE, ArmorItem.Type.LEGGINGS); // 9292, :1464
    public static final DeferredItem<ItemOreSpawnArmor> EXPERIENCE_BOOTS = armor("experience_boots", ArmorSet.EXPERIENCE, ArmorItem.Type.BOOTS); // 9293, :1465
    public static final DeferredItem<ItemOreSpawnArmor> MOTHSCALE_HELMET = armor("mothscale_helmet", ArmorSet.MOTHSCALE, ArmorItem.Type.HELMET); // 9294, :1454
    public static final DeferredItem<ItemOreSpawnArmor> MOTHSCALE_CHEST = armor("mothscale_chest", ArmorSet.MOTHSCALE, ArmorItem.Type.CHESTPLATE); // 9295, :1455
    public static final DeferredItem<ItemOreSpawnArmor> MOTHSCALE_LEGGINGS = armor("mothscale_leggings", ArmorSet.MOTHSCALE, ArmorItem.Type.LEGGINGS); // 9296, :1456
    public static final DeferredItem<ItemOreSpawnArmor> MOTHSCALE_BOOTS = armor("mothscale_boots", ArmorSet.MOTHSCALE, ArmorItem.Type.BOOTS); // 9297, :1457
    public static final DeferredItem<ItemOreSpawnArmor> LAVAEEL_HELMET = armor("lavaeel_helmet", ArmorSet.LAVAEEL, ArmorItem.Type.HELMET); // 9298, :1450
    public static final DeferredItem<ItemOreSpawnArmor> LAVAEEL_CHEST = armor("lavaeel_chest", ArmorSet.LAVAEEL, ArmorItem.Type.CHESTPLATE); // 9299, :1451
    public static final DeferredItem<ItemOreSpawnArmor> LAVAEEL_LEGGINGS = armor("lavaeel_leggings", ArmorSet.LAVAEEL, ArmorItem.Type.LEGGINGS); // 9300, :1452
    public static final DeferredItem<ItemOreSpawnArmor> LAVAEEL_BOOTS = armor("lavaeel_boots", ArmorSet.LAVAEEL, ArmorItem.Type.BOOTS); // 9301, :1453
    public static final DeferredItem<UltimateSword> ULTIMATE_SWORD = item(OriginalTab.COMBAT, "ultimatesword", p -> new UltimateSword(UltimateSword.Variant.ULTIMATE_SWORD, Material.ULTIMATE, p)); // 9302, :1307
    public static final DeferredItem<UltimateBow> ULTIMATE_BOW = item(OriginalTab.COMBAT, "ultimatebow", UltimateBow::new); // 9303, :1367
    public static final DeferredItem<UltimateFishingRod> ULTIMATE_FISHING_ROD = item(OriginalTab.TOOLS, "ultimatefishingrod", UltimateFishingRod::new); // 9304, :1369
    public static final DeferredItem<UltimatePickaxe> ULTIMATE_PICKAXE = item(OriginalTab.TOOLS, "ultimatepickaxe", p -> new UltimatePickaxe(Material.ULTIMATE, p)); // 9305, :1308
    public static final DeferredItem<UltimateShovel> ULTIMATE_SHOVEL = item(OriginalTab.TOOLS, "ultimateshovel", p -> new UltimateShovel(Material.ULTIMATE, p)); // 9306, :1309
    public static final DeferredItem<UltimateHoe> ULTIMATE_HOE = item(OriginalTab.TOOLS, "ultimatehoe", p -> new UltimateHoe(Material.ULTIMATE, p)); // 9307, :1310
    public static final DeferredItem<UltimateAxe> ULTIMATE_AXE = item(OriginalTab.TOOLS, "ultimateaxe", p -> new UltimateAxe(Material.ULTIMATE, p)); // 9308, :1311
    public static final DeferredItem<ItemOreSpawnArmor> ULTIMATE_HELMET = armor("ultimate_helmet", ArmorSet.ULTIMATE, ArmorItem.Type.HELMET); // 9309, :1446
    public static final DeferredItem<ItemOreSpawnArmor> ULTIMATE_CHEST = armor("ultimate_chest", ArmorSet.ULTIMATE, ArmorItem.Type.CHESTPLATE); // 9310, :1447
    public static final DeferredItem<ItemOreSpawnArmor> ULTIMATE_LEGGINGS = armor("ultimate_leggings", ArmorSet.ULTIMATE, ArmorItem.Type.LEGGINGS); // 9311, :1448
    public static final DeferredItem<ItemOreSpawnArmor> ULTIMATE_BOOTS = armor("ultimate_boots", ArmorSet.ULTIMATE, ArmorItem.Type.BOOTS); // 9312, :1449
    // The Bertha family: one class, the variant selects the in-hand BEWLR (client.item.bertha, R8).
    public static final DeferredItem<Bertha> BERTHA = item(OriginalTab.COMBAT, "berthasmall", p -> new Bertha(Bertha.Variant.BERTHA, Material.BERTHA, p)); // 9313, :1313
    public static final DeferredItem<Bertha> SLICE = item(OriginalTab.COMBAT, "slicesmall", p -> new Bertha(Bertha.Variant.SLICE, Material.BERTHA, p)); // 9314, :1314
    public static final DeferredItem<MantisClaw> MANTIS_CLAW = item(OriginalTab.COMBAT, "mantisclaw", p -> new MantisClaw(Material.REALEMERALD, p)); // 9315, :1329
    public static final DeferredItem<BigHammer> BIG_HAMMER = item(OriginalTab.COMBAT, "bighammer", p -> new BigHammer(Material.AMETHYST, p)); // 9316, :1330
    public static final DeferredItem<ItemSquidZooka> SQUID_ZOOKA = item(OriginalTab.COMBAT, "squidzookasmall", ItemSquidZooka::new); // 9317, :1421, W08
    public static final DeferredItem<Bertha> ROYAL = item(OriginalTab.COMBAT, "royalsmall", p -> new Bertha(Bertha.Variant.ROYAL, Material.ROYAL, p)); // 9318, :1315
    public static final DeferredItem<Bertha> HAMMY = item(OriginalTab.COMBAT, "hammysmall", p -> new Bertha(Bertha.Variant.HAMMY, Material.HAMMY, p)); // 9319, :1316
    // ZooCage takes cage_size from manifest ctor_args[1]: 3, 5, 9, 13, 17 (ZooCage.java:19).
    public static final DeferredItem<ZooCage> ZOO_CAGE2 = item(OriginalTab.DECORATIONS, "zoo2", p -> new ZooCage(3, p)); // 9320, :1593
    public static final DeferredItem<ZooCage> ZOO_CAGE4 = item(OriginalTab.DECORATIONS, "zoo4", p -> new ZooCage(5, p)); // 9321, :1594
    public static final DeferredItem<ZooCage> ZOO_CAGE6 = item(OriginalTab.DECORATIONS, "zoo6", p -> new ZooCage(9, p)); // 9322, :1595
    public static final DeferredItem<ZooCage> ZOO_CAGE8 = item(OriginalTab.DECORATIONS, "zoo8", p -> new ZooCage(13, p)); // 9323, :1596
    public static final DeferredItem<ZooCage> ZOO_CAGE10 = item(OriginalTab.DECORATIONS, "zoo10", p -> new ZooCage(17, p)); // 9324, :1597
    public static final DeferredItem<ItemSifter> SIFTER = item(OriginalTab.DECORATIONS, "sifter", ItemSifter::new); // 9325, :1420, ItemSifter.java:18 (durability 600 in the constructor)
    public static final DeferredItem<NightmareSword> NIGHTMARE_SWORD = item(OriginalTab.COMBAT, "nightmaresword", p -> new NightmareSword(Material.NIGHTMARE, p)); // 9326, :1312
    public static final DeferredItem<InstantShelter> INSTANT_SHELTER = item(OriginalTab.REDSTONE, "instantshelter", InstantShelter::new); // 9327, :1598, InstantShelter.java:18
    public static final DeferredItem<InstantGarden> INSTANT_GARDEN = item(OriginalTab.REDSTONE, "instantgarden", InstantGarden::new); // 9328, :1599, InstantGarden.java:17
    public static final DeferredItem<CrystalSword> CRYSTALWOOD_SWORD = item(OriginalTab.COMBAT, "crystalwoodsword", p -> new CrystalSword(Material.CRYSTALWOOD, p)); // 9329, :1341
    public static final DeferredItem<CrystalPickaxe> CRYSTALWOOD_PICKAXE = item(OriginalTab.TOOLS, "crystalwoodpickaxe", p -> new CrystalPickaxe(Material.CRYSTALWOOD, p)); // 9330, :1342
    public static final DeferredItem<CrystalShovel> CRYSTALWOOD_SHOVEL = item(OriginalTab.TOOLS, "crystalwoodshovel", p -> new CrystalShovel(Material.CRYSTALWOOD, p)); // 9331, :1343
    public static final DeferredItem<CrystalHoe> CRYSTALWOOD_HOE = item(OriginalTab.TOOLS, "crystalwoodhoe", p -> new CrystalHoe(Material.CRYSTALWOOD, p)); // 9332, :1344
    public static final DeferredItem<CrystalAxe> CRYSTALWOOD_AXE = item(OriginalTab.TOOLS, "crystalwoodaxe", p -> new CrystalAxe(Material.CRYSTALWOOD, p)); // 9333, :1345
    public static final DeferredItem<CrystalSword> CRYSTALPINK_SWORD = item(OriginalTab.COMBAT, "crystalpinksword", p -> new CrystalSword(Material.CRYSTALPINK, p)); // 9334, :1346
    public static final DeferredItem<CrystalPickaxe> CRYSTALPINK_PICKAXE = item(OriginalTab.TOOLS, "crystalpinkpickaxe", p -> new CrystalPickaxe(Material.CRYSTALPINK, p)); // 9335, :1347
    public static final DeferredItem<CrystalShovel> CRYSTALPINK_SHOVEL = item(OriginalTab.TOOLS, "crystalpinkshovel", p -> new CrystalShovel(Material.CRYSTALPINK, p)); // 9336, :1348
    public static final DeferredItem<CrystalHoe> CRYSTALPINK_HOE = item(OriginalTab.TOOLS, "crystalpinkhoe", p -> new CrystalHoe(Material.CRYSTALPINK, p)); // 9337, :1349
    public static final DeferredItem<CrystalAxe> CRYSTALPINK_AXE = item(OriginalTab.TOOLS, "crystalpinkaxe", p -> new CrystalAxe(Material.CRYSTALPINK, p)); // 9338, :1350
    public static final DeferredItem<CrystalSword> CRYSTALSTONE_SWORD = item(OriginalTab.COMBAT, "crystalstonesword", p -> new CrystalSword(Material.CRYSTALSTONE, p)); // 9339, :1351
    public static final DeferredItem<CrystalPickaxe> CRYSTALSTONE_PICKAXE = item(OriginalTab.TOOLS, "crystalstonepickaxe", p -> new CrystalPickaxe(Material.CRYSTALSTONE, p)); // 9340, :1352
    public static final DeferredItem<CrystalShovel> CRYSTALSTONE_SHOVEL = item(OriginalTab.TOOLS, "crystalstoneshovel", p -> new CrystalShovel(Material.CRYSTALSTONE, p)); // 9341, :1353
    public static final DeferredItem<CrystalHoe> CRYSTALSTONE_HOE = item(OriginalTab.TOOLS, "crystalstonehoe", p -> new CrystalHoe(Material.CRYSTALSTONE, p)); // 9342, :1354
    public static final DeferredItem<CrystalAxe> CRYSTALSTONE_AXE = item(OriginalTab.TOOLS, "crystalstoneaxe", p -> new CrystalAxe(Material.CRYSTALSTONE, p)); // 9343, :1355
    public static final DeferredItem<ItemOreSpawnArmor> PINK_HELMET = armor("pink_helmet", ArmorSet.PINK, ArmorItem.Type.HELMET); // 9344, :1474
    public static final DeferredItem<ItemOreSpawnArmor> PINK_CHEST = armor("pink_chest", ArmorSet.PINK, ArmorItem.Type.CHESTPLATE); // 9345, :1475
    public static final DeferredItem<ItemOreSpawnArmor> PINK_LEGGINGS = armor("pink_leggings", ArmorSet.PINK, ArmorItem.Type.LEGGINGS); // 9346, :1476
    public static final DeferredItem<ItemOreSpawnArmor> PINK_BOOTS = armor("pink_boots", ArmorSet.PINK, ArmorItem.Type.BOOTS); // 9347, :1477
    public static final DeferredItem<IngotUranium> CRYSTAL_PINK_INGOT = item(OriginalTab.MATERIALS, "crystalpink_ingot", IngotUranium::new); // 9348
    public static final DeferredItem<CrystalSword> TIGERSEYE_SWORD = item(OriginalTab.COMBAT, "tigerseye_sword", p -> new CrystalSword(Material.TIGERSEYE, p)); // 9349, :1356
    public static final DeferredItem<CrystalPickaxe> TIGERSEYE_PICKAXE = item(OriginalTab.TOOLS, "tigerseye_pickaxe", p -> new CrystalPickaxe(Material.TIGERSEYE, p)); // 9350, :1357
    public static final DeferredItem<CrystalShovel> TIGERSEYE_SHOVEL = item(OriginalTab.TOOLS, "tigerseye_shovel", p -> new CrystalShovel(Material.TIGERSEYE, p)); // 9351, :1358
    public static final DeferredItem<CrystalHoe> TIGERSEYE_HOE = item(OriginalTab.TOOLS, "tigerseye_hoe", p -> new CrystalHoe(Material.TIGERSEYE, p)); // 9352, :1359
    public static final DeferredItem<CrystalAxe> TIGERSEYE_AXE = item(OriginalTab.TOOLS, "tigerseye_axe", p -> new CrystalAxe(Material.TIGERSEYE, p)); // 9353, :1360
    public static final DeferredItem<ItemOreSpawnArmor> TIGERSEYE_HELMET = armor("tigerseye_helmet", ArmorSet.TIGERSEYE, ArmorItem.Type.HELMET); // 9354, :1478
    public static final DeferredItem<ItemOreSpawnArmor> TIGERSEYE_CHEST = armor("tigerseye_chest", ArmorSet.TIGERSEYE, ArmorItem.Type.CHESTPLATE); // 9355, :1479
    public static final DeferredItem<ItemOreSpawnArmor> TIGERSEYE_LEGGINGS = armor("tigerseye_leggings", ArmorSet.TIGERSEYE, ArmorItem.Type.LEGGINGS); // 9356, :1480
    public static final DeferredItem<ItemOreSpawnArmor> TIGERSEYE_BOOTS = armor("tigerseye_boots", ArmorSet.TIGERSEYE, ArmorItem.Type.BOOTS); // 9357, :1481
    public static final DeferredItem<IngotUranium> TIGERSEYE_INGOT = item(OriginalTab.MATERIALS, "tigerseye_ingot", IngotUranium::new); // 9358
    public static final DeferredItem<ItemOreSpawnArmor> PEACOCK_HELMET = armor("peacock_helmet", ArmorSet.PEACOCK, ArmorItem.Type.HELMET); // 9359, :1483
    public static final DeferredItem<ItemOreSpawnArmor> PEACOCK_CHEST = armor("peacock_chest", ArmorSet.PEACOCK, ArmorItem.Type.CHESTPLATE); // 9360, :1484
    public static final DeferredItem<ItemOreSpawnArmor> PEACOCK_LEGGINGS = armor("peacock_leggings", ArmorSet.PEACOCK, ArmorItem.Type.LEGGINGS); // 9370, :1485
    public static final DeferredItem<ItemOreSpawnArmor> PEACOCK_BOOTS = armor("peacock_boots", ArmorSet.PEACOCK, ArmorItem.Type.BOOTS); // 9371, :1482
    public static final DeferredItem<ItemIrukandjiArrow> IRUKANDJI_ARROW = item(OriginalTab.COMBAT, "irukandjiarrow", ItemIrukandjiArrow::new); // 9372, :1413
    public static final DeferredItem<SkateBow> SKATE_BOW = item(OriginalTab.COMBAT, "skatebow", SkateBow::new); // 9373, :1368
    public static final DeferredItem<ItemSpawnEgg> EGG_RAT = ITEMS.registerItem("eggrat", p -> new ItemSpawnEgg(ModEntities.RAT, p)); // 9374, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_FLOUNDER = ITEMS.registerItem("eggflounder", p -> new ItemSpawnEgg(ModEntities.FLOUNDER, p)); // 9375, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_WHALE = ITEMS.registerItem("eggwhale", p -> new ItemSpawnEgg(ModEntities.WHALE, p)); // 9376, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_IRUKANDJI = ITEMS.registerItem("eggirukandji", p -> new ItemSpawnEgg(ModEntities.IRUKANDJI, p)); // 9377, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_SKATE = ITEMS.registerItem("eggskate", p -> new ItemSpawnEgg(ModEntities.SKATE, p)); // 9378, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_URCHIN = ITEMS.registerItem("eggurchin", p -> new ItemSpawnEgg(ModEntities.CRYSTAL_URCHIN, p)); // 9379, W08, "Spawn Crystal Urchin"
    public static final DeferredItem<ItemSpawnEgg> EGG_ROBOT1 = ITEMS.registerItem("eggrobot1", p -> new ItemSpawnEgg(ModEntities.BOMB_OMB, p)); // 9380, W07, "Bomb-Omb"
    public static final DeferredItem<ItemSpawnEgg> EGG_ROBOT2 = ITEMS.registerItem("eggrobot2", p -> new ItemSpawnEgg(ModEntities.ROBO_POUNDER, p)); // 9381, W07, "Robo-Pounder"
    public static final DeferredItem<ItemSpawnEgg> EGG_ROBOT3 = ITEMS.registerItem("eggrobot3", p -> new ItemSpawnEgg(ModEntities.ROBO_GUNNER, p)); // 9382, W07, "Robo-Gunner"
    public static final DeferredItem<ItemSpawnEgg> EGG_ROBOT4 = ITEMS.registerItem("eggrobot4", p -> new ItemSpawnEgg(ModEntities.ROBO_WARRIOR, p)); // 9383, W07, "Robo-Warrior"
    public static final DeferredItem<ItemSpawnEgg> EGG_GHOST = ITEMS.registerItem("eggghost", p -> new ItemSpawnEgg(ModEntities.GHOST, p)); // 9384, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_GHOST_SKELLY = ITEMS.registerItem("eggghostskelly", p -> new ItemSpawnEgg(ModEntities.GHOST_PUMPKIN_SKELLY, p)); // 9385, W06
    // W05 spawn eggs at their legacy item ids (manifest ctor_args 9386-9394, egg index 330-338); tab and dispenser
    // come from SpawnEggSetup (R9).
    public static final DeferredItem<ItemSpawnEgg> EGG_BROWN_ANT = ITEMS.registerItem("eggbrownant", p -> new ItemSpawnEgg(ModEntities.ANT, p)); // 9386
    public static final DeferredItem<ItemSpawnEgg> EGG_RED_ANT = ITEMS.registerItem("eggredant", p -> new ItemSpawnEgg(ModEntities.RED_ANT, p)); // 9387
    public static final DeferredItem<ItemSpawnEgg> EGG_RAINBOW_ANT = ITEMS.registerItem("eggrainbowant", p -> new ItemSpawnEgg(ModEntities.RAINBOW_ANT, p)); // 9388
    public static final DeferredItem<ItemSpawnEgg> EGG_UNSTABLE_ANT = ITEMS.registerItem("eggunstableant", p -> new ItemSpawnEgg(ModEntities.UNSTABLE_ANT, p)); // 9389
    public static final DeferredItem<ItemSpawnEgg> EGG_TERMITE = ITEMS.registerItem("eggtermite", p -> new ItemSpawnEgg(ModEntities.TERMITE, p)); // 9390
    public static final DeferredItem<ItemSpawnEgg> EGG_BUTTERFLY = ITEMS.registerItem("eggbutterfly", p -> new ItemSpawnEgg(ModEntities.BUTTERFLY, p)); // 9391
    public static final DeferredItem<ItemSpawnEgg> EGG_MOTH = ITEMS.registerItem("eggmoth", p -> new ItemSpawnEgg(ModEntities.MOTH, p)); // 9392
    public static final DeferredItem<ItemSpawnEgg> EGG_MOSQUITO = ITEMS.registerItem("eggmosquito", p -> new ItemSpawnEgg(ModEntities.MOSQUITO, p)); // 9393
    public static final DeferredItem<ItemSpawnEgg> EGG_FIREFLY = ITEMS.registerItem("eggfirefly", p -> new ItemSpawnEgg(ModEntities.FIREFLY, p)); // 9394
    public static final DeferredItem<ItemOreSpawnArmor> MOBZILLA_HELMET = armor("mobzilla_helmet", ArmorSet.MOBZILLA, ArmorItem.Type.HELMET); // 9395, :1486
    public static final DeferredItem<ItemOreSpawnArmor> MOBZILLA_CHEST = armor("mobzilla_chest", ArmorSet.MOBZILLA, ArmorItem.Type.CHESTPLATE); // 9396, :1487
    public static final DeferredItem<ItemOreSpawnArmor> MOBZILLA_LEGGINGS = armor("mobzilla_leggings", ArmorSet.MOBZILLA, ArmorItem.Type.LEGGINGS); // 9397, :1488
    public static final DeferredItem<ItemOreSpawnArmor> MOBZILLA_BOOTS = armor("mobzilla_boots", ArmorSet.MOBZILLA, ArmorItem.Type.BOOTS); // 9398, :1489
    public static final DeferredItem<ItemSpawnEgg> EGG_BOYFRIEND = ITEMS.registerItem("eggboyfriend", p -> new ItemSpawnEgg(ModEntities.BOYFRIEND, p)); // 9399, W04, OreSpawnMain.java:5273 (egg index 349), added in fix1
    public static final DeferredItem<ItemSpawnEgg> EGG_THE_KING = ITEMS.registerItem("eggtheking", p -> new ItemSpawnEgg(ModEntities.THE_KING, p)); // 9400, W10, OreSpawnMain.java:5274 (egg index 350), "Spawn The King"
    public static final DeferredItem<ItemSpawnEgg> EGG_THE_PRINCE = ITEMS.registerItem("eggtheprince", p -> new ItemSpawnEgg(ModEntities.THE_PRINCE, p)); // 9401, W10
    public static final DeferredItem<ItemOreSpawnArmor> ROYAL_HELMET = armor("royal_helmet", ArmorSet.ROYAL, ArmorItem.Type.HELMET); // 9402, :1490
    public static final DeferredItem<ItemOreSpawnArmor> ROYAL_CHEST = armor("royal_chest", ArmorSet.ROYAL, ArmorItem.Type.CHESTPLATE); // 9403, :1491
    public static final DeferredItem<ItemOreSpawnArmor> ROYAL_LEGGINGS = armor("royal_leggings", ArmorSet.ROYAL, ArmorItem.Type.LEGGINGS); // 9404, :1492
    public static final DeferredItem<ItemOreSpawnArmor> ROYAL_BOOTS = armor("royal_boots", ArmorSet.ROYAL, ArmorItem.Type.BOOTS); // 9405, :1493
    public static final DeferredItem<ItemSalt> BB_HANDLE = item(OriginalTab.MATERIALS, "bbhandle", ItemSalt::new); // 9406
    public static final DeferredItem<ItemSalt> BB_GUARD = item(OriginalTab.MATERIALS, "bbguard", ItemSalt::new); // 9407
    public static final DeferredItem<ItemSalt> BB_BLADE = item(OriginalTab.MATERIALS, "bbblade", ItemSalt::new); // 9408
    public static final DeferredItem<ItemSalt> MOLENOID_NOSE = item(OriginalTab.MATERIALS, "molenoidnose", ItemSalt::new); // 9409
    public static final DeferredItem<ItemSalt> SEA_MONSTER_SCALE = item(OriginalTab.MATERIALS, "seamonsterscale", ItemSalt::new); // 9410
    public static final DeferredItem<ItemSalt> WORM_TOOTH = item(OriginalTab.MATERIALS, "wormtooth", ItemSalt::new); // 9411
    public static final DeferredItem<ItemSalt> TREX_TOOTH = item(OriginalTab.MATERIALS, "trextooth", ItemSalt::new); // 9412
    public static final DeferredItem<ItemSalt> CATERKILLER_JAW = item(OriginalTab.MATERIALS, "caterkillerjaw", ItemSalt::new); // 9413
    public static final DeferredItem<ItemSalt> SEA_VIPER_TONGUE = item(OriginalTab.MATERIALS, "seavipertongue", ItemSalt::new); // 9414
    public static final DeferredItem<ItemSalt> VORTEX_EYE = item(OriginalTab.MATERIALS, "vortexeye", ItemSalt::new); // 9415
    public static final DeferredItem<ItemSpawnEgg> EGG_EASTER_BUNNY = ITEMS.registerItem("eggeasterbunny", p -> new ItemSpawnEgg(ModEntities.EASTER_BUNNY, p)); // 9416, W11, "Spawn Easter Bunny"
    public static final DeferredItem<ItemSpawnEgg> EGG_MOLENOID = ITEMS.registerItem("eggmolenoid", p -> new ItemSpawnEgg(ModEntities.MOLENOID, p)); // 9417, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_SEA_MONSTER = ITEMS.registerItem("eggseamonster", p -> new ItemSpawnEgg(ModEntities.SEA_MONSTER, p)); // 9418, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_SEA_VIPER = ITEMS.registerItem("eggseaviper", p -> new ItemSpawnEgg(ModEntities.SEA_VIPER, p)); // 9419, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_CATER_KILLER = ITEMS.registerItem("eggcaterkiller", p -> new ItemSpawnEgg(ModEntities.CATER_KILLER, p)); // 9420, W08
    public static final DeferredItem<com.swbr.orespawn.item.magic.ItemRandomDungeon> RANDOM_DUNGEON = item(OriginalTab.REDSTONE, "randomdungeon", p -> new com.swbr.orespawn.item.magic.ItemRandomDungeon(ModBlocks.DUNGEONSPAWNER, p)); // 9421, :1603, ItemRandomDungeon.java:21-22
    // The three big weapons: UltimateSword variants whose in-hand renderers are the BEWLRs of client.item (R8),
    // wired in client.ClientSetup through BigWeaponRenderers.
    public static final DeferredItem<UltimateSword> BATTLE_AXE = item(OriginalTab.COMBAT, "battleaxesmall", p -> new UltimateSword(UltimateSword.Variant.BATTLE_AXE, Material.BATTLE, p)); // 9422, :1317
    public static final DeferredItem<CritterCage> CAGE_LEON = item(OriginalTab.MISC, "cageleon", p -> new CritterCage(357, p)); // 9423
    public static final DeferredItem<ItemSpawnEgg> EGG_LEON = ITEMS.registerItem("eggleon", p -> new ItemSpawnEgg(ModEntities.LEONOPTERYX, p)); // 9424, W09
    public static final DeferredItem<CritterCage> CAGE_HAMMERHEAD = item(OriginalTab.MISC, "cagehammerhead", p -> new CritterCage(359, p)); // 9425
    public static final DeferredItem<ItemSpawnEgg> EGG_HAMMERHEAD = ITEMS.registerItem("egghammerhead", p -> new ItemSpawnEgg(ModEntities.HAMMERHEAD, p)); // 9426, W07
    public static final DeferredItem<CritterCage> CAGE_RUBBERDUCKY = item(OriginalTab.MISC, "cagerubberducky", p -> new CritterCage(361, p)); // 9427
    public static final DeferredItem<ItemSpawnEgg> EGG_RUBBER_DUCKY = ITEMS.registerItem("eggrubberducky", p -> new ItemSpawnEgg(ModEntities.RUBBER_DUCKY, p)); // 9428, W09
    public static final DeferredItem<CritterCage> CAGE_CRYSTALCOW = item(OriginalTab.MISC, "cagecrystalcow", p -> new CritterCage(216, p)); // 9429
    public static final DeferredItem<CritterCage> CAGE_VILLAGER = item(OriginalTab.MISC, "cagevillager", p -> new CritterCage(217, p)); // 9430
    public static final DeferredItem<ItemSpawnEgg> EGG_CRYSTAL_COW = ITEMS.registerItem("eggcrystalcow", p -> new ItemSpawnEgg(ModEntities.CRYSTAL_APPLE_COW, p)); // 9431, W06
    public static final DeferredItem<CritterCage> CAGE_CRIMINAL = item(OriginalTab.MISC, "cagecriminal", p -> new CritterCage(218, p)); // 9433
    public static final DeferredItem<ItemSpawnEgg> EGG_CRIMINAL = ITEMS.registerItem("eggcriminal", p -> new ItemSpawnEgg(ModEntities.CRIMINAL, p)); // 9434, W07
    // ItemRock(type) from ItemRock.java:24-59; ItemRock.byType is filled by the constructor.
    public static final DeferredItem<ItemRock> ROCK = item(OriginalTab.COMBAT, "rock", p -> new ItemRock(2, p)); // 9435
    public static final DeferredItem<ItemRock> ROCK_SMALL = item(OriginalTab.COMBAT, "rocksmall", p -> new ItemRock(1, p)); // 9436
    public static final DeferredItem<ItemRock> ROCK_RED = item(OriginalTab.COMBAT, "rockred", p -> new ItemRock(3, p)); // 9437
    public static final DeferredItem<ItemRock> ROCK_GREEN = item(OriginalTab.COMBAT, "rockgreen", p -> new ItemRock(4, p)); // 9438
    public static final DeferredItem<ItemRock> ROCK_BLUE = item(OriginalTab.COMBAT, "rockblue", p -> new ItemRock(5, p)); // 9439
    public static final DeferredItem<ItemRock> ROCK_PURPLE = item(OriginalTab.COMBAT, "rockpurple", p -> new ItemRock(6, p)); // 9440
    public static final DeferredItem<ItemRock> ROCK_SPIKEY = item(OriginalTab.COMBAT, "rockspikey", p -> new ItemRock(7, p)); // 9441
    public static final DeferredItem<ItemRock> ROCK_TNT = item(OriginalTab.COMBAT, "rocktnt", p -> new ItemRock(8, p)); // 9442
    public static final DeferredItem<ItemRock> ROCK_CRYSTAL_RED = item(OriginalTab.COMBAT, "rockcrystalred", p -> new ItemRock(9, p)); // 9443
    public static final DeferredItem<ItemRock> ROCK_CRYSTAL_GREEN = item(OriginalTab.COMBAT, "rockcrystalgreen", p -> new ItemRock(10, p)); // 9444
    public static final DeferredItem<ItemRock> ROCK_CRYSTAL_BLUE = item(OriginalTab.COMBAT, "rockcrystalblue", p -> new ItemRock(11, p)); // 9445
    public static final DeferredItem<ItemRock> ROCK_CRYSTAL_TNT = item(OriginalTab.COMBAT, "rockcrystaltnt", p -> new ItemRock(12, p)); // 9446
    public static final DeferredItem<UltimateSword> CHAINSAW = item(OriginalTab.COMBAT, "chainsawsmall", p -> new UltimateSword(UltimateSword.Variant.CHAINSAW, Material.CHAINSAW, p)); // 9447, :1318
    public static final DeferredItem<ItemSpawnEgg> EGG_THE_QUEEN = ITEMS.registerItem("eggthequeen", p -> new ItemSpawnEgg(ModEntities.THE_QUEEN, p)); // 9448, W10, egg index 366
    public static final DeferredItem<ItemOreSpawnArmor> LAPIS_HELMET = armor("lapis_helmet", ArmorSet.LAPIS, ArmorItem.Type.HELMET); // 9449, :1494
    public static final DeferredItem<ItemOreSpawnArmor> LAPIS_CHEST = armor("lapis_chest", ArmorSet.LAPIS, ArmorItem.Type.CHESTPLATE); // 9450, :1495
    public static final DeferredItem<ItemOreSpawnArmor> LAPIS_LEGGINGS = armor("lapis_leggings", ArmorSet.LAPIS, ArmorItem.Type.LEGGINGS); // 9451, :1496
    public static final DeferredItem<ItemOreSpawnArmor> LAPIS_BOOTS = armor("lapis_boots", ArmorSet.LAPIS, ArmorItem.Type.BOOTS); // 9452, :1497
    public static final DeferredItem<ItemSalt> QUEEN_SCALE = item(OriginalTab.MATERIALS, "queenscale", ItemSalt::new); // 9453
    public static final DeferredItem<ItemOreSpawnArmor> QUEEN_HELMET = armor("queen_helmet", ArmorSet.QUEEN, ArmorItem.Type.HELMET); // 9454, :1498
    public static final DeferredItem<ItemOreSpawnArmor> QUEEN_CHEST = armor("queen_chest", ArmorSet.QUEEN, ArmorItem.Type.CHESTPLATE); // 9455, :1499
    public static final DeferredItem<ItemOreSpawnArmor> QUEEN_LEGGINGS = armor("queen_leggings", ArmorSet.QUEEN, ArmorItem.Type.LEGGINGS); // 9456, :1500
    public static final DeferredItem<ItemOreSpawnArmor> QUEEN_BOOTS = armor("queen_boots", ArmorSet.QUEEN, ArmorItem.Type.BOOTS); // 9457, :1501
    public static final DeferredItem<ItemDuctTape> DUCTTAPE_ITEM = item(OriginalTab.TOOLS, "ducttape_item", p -> new ItemDuctTape(ModBlocks.DUCTTAPE.get(), p)); // 9458, :1291 stack 1
    public static final DeferredItem<ItemSpawnEgg> EGG_BRUTALFLY = ITEMS.registerItem("eggbrutalfly", p -> new ItemSpawnEgg(ModEntities.BRUTALFLY, p)); // 9459, W08
    public static final DeferredItem<ItemSpawnEgg> EGG_NASTYSAURUS = ITEMS.registerItem("eggnastysaurus", p -> new ItemSpawnEgg(ModEntities.NASTYSAURUS, p)); // 9460, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_POINTYSAURUS = ITEMS.registerItem("eggpointysaurus", p -> new ItemSpawnEgg(ModEntities.POINTYSAURUS, p)); // 9461, W07
    public static final DeferredItem<ItemSpawnEgg> EGG_CRICKET = ITEMS.registerItem("eggcricket", p -> new ItemSpawnEgg(ModEntities.CRICKET, p)); // 9462, W06
    public static final DeferredItem<ItemSpawnEgg> EGG_THE_PRINCESS = ITEMS.registerItem("eggtheprincess", p -> new ItemSpawnEgg(ModEntities.THE_PRINCESS, p)); // 9463, W10
    public static final DeferredItem<ItemSpawnEgg> EGG_FROG = ITEMS.registerItem("eggfrog", p -> new ItemSpawnEgg(ModEntities.FROG, p)); // 9464, W06
    public static final DeferredItem<CritterCage> CAGE_BRUTALFLY = item(OriginalTab.MISC, "cagebrutalfly", p -> new CritterCage(373, p)); // 9465
    public static final DeferredItem<CritterCage> CAGE_NASTYSAURUS = item(OriginalTab.MISC, "cagenastysaurus", p -> new CritterCage(374, p)); // 9466
    public static final DeferredItem<CritterCage> CAGE_POINTYSAURUS = item(OriginalTab.MISC, "cagepointysaurus", p -> new CritterCage(375, p)); // 9467
    public static final DeferredItem<CritterCage> CAGE_CRICKET = item(OriginalTab.MISC, "cagecricket", p -> new CritterCage(376, p)); // 9468
    public static final DeferredItem<CritterCage> CAGE_FROG = item(OriginalTab.MISC, "cagefrog", p -> new CritterCage(377, p)); // 9469
    public static final DeferredItem<UltimateSword> QUEEN_BATTLE_AXE = item(OriginalTab.COMBAT, "queenbattleaxesmall", p -> new UltimateSword(UltimateSword.Variant.QUEEN_BATTLE_AXE, Material.QUEENBATTLE, p)); // 9470, :1319
    // W09 robot kits (ItemSpiderRobotKit reads its durability through EarlyConfig), wrench and robot eggs. :1385-1386
    public static final DeferredItem<ItemSpiderRobotKit> SPIDER_ROBOT_KIT = item(OriginalTab.TOOLS, "spiderrobotkit", p -> new ItemSpiderRobotKit(ItemSpiderRobotKit.Kit.SPIDER_ROBOT, p)); // 9471, :1385
    public static final DeferredItem<ItemWrench> WRENCH = item(OriginalTab.TOOLS, "wrench", ItemWrench::new); // 9472
    public static final DeferredItem<ItemSpiderRobotKit> ANT_ROBOT_KIT = item(OriginalTab.TOOLS, "antrobotkit", p -> new ItemSpiderRobotKit(ItemSpiderRobotKit.Kit.ANT_ROBOT, p)); // 9473, :1386
    public static final DeferredItem<ItemSpawnEgg> EGG_JEFFERY = ITEMS.registerItem("eggrobot6", p -> new ItemSpawnEgg(ModEntities.JEFFERY, p)); // 9474, W07, "Jeffery"
    public static final DeferredItem<ItemSpawnEgg> EGG_ANT_ROBOT = ITEMS.registerItem("eggantrobot", p -> new ItemSpawnEgg(ModEntities.ROBOT_RED_ANT, p)); // 9475, W09, "Spawn Red Ant Robot"
    public static final DeferredItem<ItemSpawnEgg> EGG_SPIDER_ROBOT = ITEMS.registerItem("eggspiderrobot", p -> new ItemSpawnEgg(ModEntities.ROBOT_SPIDER, p)); // 9476, W09, "Robot Spider"
    public static final DeferredItem<ItemSpawnEgg> EGG_SPIDER_DRIVER = ITEMS.registerItem("eggspiderdriver", p -> new ItemSpawnEgg(ModEntities.SPIDER_DRIVER, p)); // 9477, W09, "Spider Driver"
    public static final DeferredItem<CritterCage> CAGE_SPIDERDRIVER = item(OriginalTab.MISC, "cagespiderdriver", p -> new CritterCage(382, p)); // 9478
    public static final DeferredItem<ItemPopcorn> CRAB_MEAT = item(OriginalTab.FOOD, "crabmeat", p -> new ItemPopcorn(4, 0.25f, p)); // 9479
    public static final DeferredItem<ItemSunFish> COOKED_CRAB_MEAT = item(OriginalTab.FOOD, "cookedcrabmeat", p -> new ItemSunFish(ItemSunFish.Variant.CRAB_MEAT, 6, 0.75f, p)); // 9480
    public static final DeferredItem<ItemPopcorn> CRABBY_PATTY = item(OriginalTab.FOOD, "crabbypatty", p -> new ItemPopcorn(16, 2.35f, p)); // 9481
    public static final DeferredItem<ItemSpawnEgg> EGG_CRAB = ITEMS.registerItem("eggcrab", p -> new ItemSpawnEgg(ModEntities.CRAB, p)); // 9482, W08
    public static final DeferredItem<CritterCage> CAGE_CRAB = item(OriginalTab.MISC, "cagecrab", p -> new CritterCage(384, p)); // 9483
    public static final DeferredItem<EmeraldSword> ROSE_SWORD = item(OriginalTab.COMBAT, "rosesword", p -> new EmeraldSword(Material.REALEMERALD, p)); // 9484, :1361
    public static final DeferredItem<ItemSunFish> HEART = item(OriginalTab.FOOD, "heart", p -> new ItemSunFish(ItemSunFish.Variant.LOVE, 8, 0.95f, p)); // 9485

    private ModItems() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
