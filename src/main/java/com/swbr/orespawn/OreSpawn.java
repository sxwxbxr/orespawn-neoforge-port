package com.swbr.orespawn;

import com.mojang.logging.LogUtils;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.PortConfig;
import com.swbr.orespawn.item.armor.OreSpawnArmorMaterials;
import com.swbr.orespawn.platform.Holidays;
import com.swbr.orespawn.loot.ModLootModifiers;
import com.swbr.orespawn.registry.ModAttachments;
import com.swbr.orespawn.registry.ModBiomeModifiers;
import com.swbr.orespawn.registry.ModBlockEntities;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModChunkGenerators;
import com.swbr.orespawn.registry.ModCreativeTabs;
import com.swbr.orespawn.registry.ModDataComponents;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModMenus;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.registry.ModStructurePlacements;
import com.swbr.orespawn.world.feature.OreSpawnFeatures;
import com.swbr.orespawn.world.structure.OreSpawnStructures;
import java.util.Random;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Mod entry point - the successor of {@code OreSpawnMain} (OreSpawnMain.java:24-6575).
 *
 * <p>A private port of TheyCallMeDanger's OreSpawn 1.7.10 (v20.3). Numbers, names and models come
 * from the original jar wherever the jar has them; {@code docs/DECISIONS.md} is binding for every
 * deviation.
 *
 * <p>What {@code OreSpawnMain} did and where it went:
 * <ul>
 *   <li>Config reading with clamps (preInit, :1128-1260): {@link OreSpawnConfig} (generated) plus
 *       the clamping readers in {@code config.stats}.</li>
 *   <li>Block, item, entity, tile entity, GUI and dispenser registration (:1273-5065): the
 *       {@code registry} holders, filled wave by wave.</li>
 *   <li>Recipes, chest loot (:2324-3056, :5051-5062): generated recipe JSON and {@code loot.ModLootModifiers} (W12, R10, R14).</li>
 *   <li>Natural spawns and the holiday date check (:4178-4642): {@link Holidays} here, the spawn
 *       table as the biome modifier {@code orespawn:config_spawns} (W12, R11).</li>
 *   <li>Proxies: {@code client.ClientSetup} and {@code platform.CommonSetup}.</li>
 *   <li>Global statics (:6203-6574): the ones read by other classes stay here under their original
 *       names.</li>
 *   <li>{@code setBlockFast} and friends (:5499-5631): {@code world.util} (W01).</li>
 * </ul>
 * OreSpawnMain had no event handlers, no tick handler, no login or chat logic and no commands
 * (verhalten/core-01a.md, section 8): there is nothing of that kind to port.
 */
@Mod(OreSpawn.MOD_ID)
public final class OreSpawn {

    public static final String MOD_ID = "orespawn";
    public static final Logger LOG = LogUtils.getLogger();

    /**
     * {@code OreSpawnMain.OreSpawnRand = new Random(151L)} (OreSpawnMain.java:194, :6366), shared
     * by 74 classes: entities, plants and the world generator. {@link Random} is thread-safe, so
     * the shared instance survives 1.21.1's parallel chunk generation without a crash; only the
     * sequence is no longer reproducible - it never was across sessions in 1.7.10 either.
     * Worldgen code that must be deterministic uses its own seeded source instead (R18, Math.random).
     */
    public static final Random OreSpawnRand = new Random(151L);

    /** {@code OreSpawnMain.godzilla_has_spawned} (:48, :6221); written by Godzilla (W10). */
    public static int godzilla_has_spawned = 0;

    /** {@code OreSpawnMain.valentines_day} (:50, :6223); 1 on February 14th, see {@link Holidays}. */
    public static int valentines_day = 0;

    /** {@code OreSpawnMain.easter_day} (:51, :6224); 1 on April 20th, see {@link Holidays}. */
    public static int easter_day = 0;

    /**
     * 1 on October 31st. PORT: the original had no field for this - it added the Ghost and
     * GhostSkelly spawn entries inline when the date matched (OreSpawnMain.java:4181-4226). The
     * biome modifier of W12 reads this flag instead.
     */
    public static int halloween = 0;

    // Not carried over from OreSpawnMain: current_dimension (:49) and FastGraphicsLeaves (:180)
    // were written by the client-only GirlfriendOverlayGui and read by common code; on a
    // dedicated server they stayed 0 (verhalten/core-01a.md, section 9). Dimension checks use
    // level.dimension() (catalogue 5.16); the fast-graphics texture swap has no 1.21.1
    // counterpart and is dropped (R18, "schnelle Grafik").
    // flyup_keystate (:32) is a per-player attachment now (R15), registered by the network port.

    public OreSpawn(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, OreSpawnConfig.SPEC);
        // Port-only switches (legacyArmorFormula, R5) live in their own file so the original's
        // key set stays a faithful copy of OreSpawn.cfg.
        container.registerConfig(ModConfig.Type.COMMON, PortConfig.SPEC, PortConfig.FILE_NAME);

        // Class-load every holder before the DeferredRegisters are attached: a holder that is
        // never touched registers nothing, and the failure is silent.
        ModBlocks.init();
        // The 14 armor materials (Registries.ARMOR_MATERIAL, W03) live in item.armor; ArmorItem
        // stores the Holder and resolves it lazily, so the order relative to ModItems is free.
        OreSpawnArmorMaterials.init();
        ModItems.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModMenus.init();
        ModCreativeTabs.init();
        ModAttachments.init();
        ModDataComponents.init();
        ModSounds.init();
        ModChunkGenerators.init();
        ModStructurePlacements.init();
        // W12: feature and placement modifier types, the orespawn:legacy structure and piece type (world
        // packages, not registry/), the config_spawns biome modifier codec, the chest loot modifier and
        // the Miner's Dream recipe condition. OreSpawnStructures.init() also runs OreSpawnWorld.bootstrap()
        // -> TreeBuilders.bootstrap(), which fills the structure builder tables.
        OreSpawnFeatures.init();
        OreSpawnStructures.init();
        ModBiomeModifiers.init();
        ModLootModifiers.init();

        ModBlocks.BLOCKS.register(modBus);
        OreSpawnArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        // W05: the six dimension generator codecs; they must be in the registry before the
        // datapack dimensions under data/orespawn/dimension/ are decoded.
        ModChunkGenerators.CHUNK_GENERATORS.register(modBus);
        // The 1.7.10 stronghold rings of Mining and VillageMania (structure_set orespawn:strongholds).
        ModStructurePlacements.STRUCTURE_PLACEMENTS.register(modBus);
        // W12: everything the worldgen JSON, biome modifiers, loot modifiers and recipe conditions name
        // must be registered before datapacks and saved chunks are decoded.
        OreSpawnFeatures.FEATURES.register(modBus);
        OreSpawnFeatures.PLACEMENT_MODIFIERS.register(modBus);
        OreSpawnStructures.STRUCTURE_TYPES.register(modBus);
        OreSpawnStructures.STRUCTURE_PIECES.register(modBus);
        ModBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modBus);
        ModLootModifiers.CONDITION_CODECS.register(modBus);

        // The original read the calendar once while loading (OreSpawnMain.java:4178-4180); the
        // client did so too, which is why RenderGirlfriend and Shoes could read valentines_day.
        // Evaluated again at server start (R18), see platform.GameEvents.
        Holidays.evaluate();
    }
}
