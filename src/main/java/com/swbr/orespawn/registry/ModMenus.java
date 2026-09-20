package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.menu.ContainerCrystalFurnace;
import com.swbr.orespawn.menu.ContainerCrystalWorkbench;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu types - the successor of {@code OreSpawnGUIHandler}
 * ({@code NetworkRegistry.INSTANCE.registerGuiHandler}, OreSpawnMain.java:5064), which served two
 * GUIs: the Crystal Furnace and the Crystal Workbench (DECISIONS R15). The screens are registered
 * in {@code client.ClientSetup} on {@code RegisterMenuScreensEvent}.
 */
public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, OreSpawn.MOD_ID);

    /**
     * OreSpawnGUIHandler GUI id 0 (OreSpawnGUIHandler.java:13-18, :29-33): the Crystal Furnace.
     * The client factory reads the BlockPos written by {@code player.openMenu(provider, pos)}.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCrystalFurnace>> CRYSTAL_FURNACE =
            MENUS.register("crystalfurnace", () -> IMenuTypeExtension.create(ContainerCrystalFurnace::new));

    /** OreSpawnGUIHandler GUI id 1 (:19-21, :35-37): the Crystal Workbench. */
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCrystalWorkbench>> CRYSTAL_WORKBENCH =
            MENUS.register("crystalworkbench", () -> IMenuTypeExtension.create(ContainerCrystalWorkbench::new));

    private ModMenus() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
