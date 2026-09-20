package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative tabs, as in the original: OreSpawn 1.7.10 defined <em>no</em> tab of its own. Every
 * item and block called {@code setCreativeTab} with one of nine vanilla tabs (grep over
 * {@code reference/src-20.2}: 118 calls in 117 files; OreSpawnMain.java:1289 and :1291 set
 * {@code tabFood} and {@code tabTools} for Pizza and Duct Tape, the rest sits in the item and
 * block classes). So {@link #TABS} stays empty and {@link OriginalTab} maps the 1.7.10 tabs onto
 * their 1.21.1 successors.
 *
 * <p>Later waves register each item with the tab its original class named:
 * {@code ModCreativeTabs.add(OriginalTab.COMBAT, ModItems.ULTIMATESWORD)}. Items appear in the
 * vanilla tab in registration order, after the vanilla entries.
 */
public final class ModCreativeTabs {

    /** Empty on purpose - see the class comment. Wired so a later decision needs no plumbing. */
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OreSpawn.MOD_ID);

    /**
     * The nine 1.7.10 {@code CreativeTabs} the original used, each with the 1.21.1 tab that holds
     * the same vanilla content today.
     */
    public enum OriginalTab {
        /** {@code tabBlock}: ores, storage blocks, logs, stone-like blocks. */
        BLOCK(CreativeModeTabs.BUILDING_BLOCKS),
        /**
         * {@code tabDecorations}: torches, workbench, furnace, saplings, flowers, leaves, cages.
         * PORT: 1.21.1 splits this between FUNCTIONAL_BLOCKS and NATURAL_BLOCKS; one target keeps
         * the original grouping intact instead of guessing per item.
         */
        DECORATIONS(CreativeModeTabs.FUNCTIONAL_BLOCKS),
        /** {@code tabRedstone}: repellents, instant builders, Miner's Dream, Random Dungeon. */
        REDSTONE(CreativeModeTabs.REDSTONE_BLOCKS),
        /** {@code tabTransport}: the Elevator. 1.21.1 keeps minecarts and boats under Tools. */
        TRANSPORT(CreativeModeTabs.TOOLS_AND_UTILITIES),
        /**
         * {@code tabMisc}: spawn eggs and Critter Cages. PORT: the Misc tab is gone in 1.21.1;
         * vanilla eggs live in SPAWN_EGGS, and a cage is a mob in item form.
         */
        MISC(CreativeModeTabs.SPAWN_EGGS),
        /** {@code tabFood}. */
        FOOD(CreativeModeTabs.FOOD_AND_DRINKS),
        /** {@code tabTools}. */
        TOOLS(CreativeModeTabs.TOOLS_AND_UTILITIES),
        /** {@code tabCombat}: swords, armor, projectiles, bows. */
        COMBAT(CreativeModeTabs.COMBAT),
        /** {@code tabMaterials}: ingots, salt, crystal sticks. */
        MATERIALS(CreativeModeTabs.INGREDIENTS);

        private final ResourceKey<CreativeModeTab> target;

        OriginalTab(ResourceKey<CreativeModeTab> target) {
            this.target = target;
        }

        public ResourceKey<CreativeModeTab> target() {
            return target;
        }
    }

    private static final Map<OriginalTab, List<Supplier<? extends ItemLike>>> ENTRIES = new EnumMap<>(OriginalTab.class);

    private ModCreativeTabs() {}

    /**
     * Files {@code item} under the tab its original class named. Call from the registry holder's
     * static initialiser, right after the {@code DeferredItem} it refers to.
     */
    public static synchronized void add(OriginalTab tab, Supplier<? extends ItemLike> item) {
        ENTRIES.computeIfAbsent(tab, t -> new ArrayList<>()).add(item);
    }

    /** Mod-bus listener, attached in {@code platform.CommonSetup}. */
    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        for (OriginalTab tab : OriginalTab.values()) {
            if (!event.getTabKey().equals(tab.target())) {
                continue;
            }
            List<Supplier<? extends ItemLike>> entries;
            synchronized (ModCreativeTabs.class) {
                entries = ENTRIES.getOrDefault(tab, List.of());
            }
            for (Supplier<? extends ItemLike> entry : entries) {
                event.accept(entry.get());
            }
        }
    }

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
