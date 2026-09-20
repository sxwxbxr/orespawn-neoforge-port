package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item data components. NBT on items is gone in 1.21.1; whatever the original stored on a stack
 * ({@code stackTagCompound}, e.g. the shot count of a kit)
 * becomes a component registered here by the wave that ports the item.
 *
 * <p>Empty in W01: nothing in this wave carries item state.
 */
public final class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(OreSpawn.MOD_ID);

    private ModDataComponents() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
