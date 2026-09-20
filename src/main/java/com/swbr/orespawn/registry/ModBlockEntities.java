package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.entity.TileEntityCrystalFurnace;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block entity types. The original had exactly one: {@code TileEntityCrystalFurnace}
 * ({@code GameRegistry.registerTileEntity}, OreSpawnMain.java:5063).
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OreSpawn.MOD_ID);

    /**
     * {@code TileEntityCrystalFurnace} (GameRegistry.registerTileEntity, OreSpawnMain.java:5063).
     * No manifest id for the tile entity; registered under the block's id (W03, w03-workshop).
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCrystalFurnace>> CRYSTAL_FURNACE =
            BLOCK_ENTITY_TYPES.register("crystalfurnace",
                    () -> BlockEntityType.Builder.of(TileEntityCrystalFurnace::new, ModBlocks.CRYSTALFURNACE.get()).build(null));

    private ModBlockEntities() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
