package com.swbr.orespawn.item.spawnegg;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Mod-bus wiring for every {@link ItemSpawnEgg}, whichever wave registered it.
 *
 * <p>The original did this per egg by hand: 115 {@code dispenseBehaviorRegistry.putObject} calls
 * ({@code OreSpawnMain.java:5300-5414}, {@code LizardEgg} twice) and {@code CreativeTabs.tabMisc}
 * in the item constructor. Here both walk {@link ItemSpawnEgg#all()}, so a later wave only has
 * to construct its eggs in {@code ModItems} and the dispenser and the creative tab follow.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class SpawnEggSetup {

    /** Dispenser behaviour for all eggs. {@code DispenserBlock.registerBehavior} writes a plain
     *  map, so it runs on the main thread via {@code enqueueWork}. */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ItemSpawnEgg.DispenserBehaviorOreSpawnEgg behavior = new ItemSpawnEgg.DispenserBehaviorOreSpawnEgg();
            for (ItemSpawnEgg egg : ItemSpawnEgg.all()) {
                DispenserBlock.registerBehavior(egg, behavior);
            }
        });
    }

    /**
     * PORT: {@code CreativeTabs.tabMisc} (ItemSpawnEgg.java:20) no longer exists; the vanilla
     * spawn-egg tab is the closest home. Registration order is kept.
     */
    @SubscribeEvent
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            for (ItemSpawnEgg egg : ItemSpawnEgg.all()) {
                event.accept(egg);
            }
        }
    }

    private SpawnEggSetup() {}
}
