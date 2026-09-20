package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.gen.MapGenStronghold;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Structure placement types of the port. {@code data/orespawn/worldgen/structure_set/*.json} names them by id;
 * like the chunk generator codecs they must be registered before the datapack registries are decoded.
 */
public final class ModStructurePlacements {

    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, OreSpawn.MOD_ID);

    private static final StructurePlacementType<MapGenStronghold> LEGACY_STRONGHOLD_TYPE = () -> MapGenStronghold.CODEC;

    /** 1.7.10 {@code MapGenStronghold} ring positions, used by Mining and VillageMania. */
    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<MapGenStronghold>> LEGACY_STRONGHOLD =
            STRUCTURE_PLACEMENTS.register("legacy_stronghold", () -> LEGACY_STRONGHOLD_TYPE);

    private ModStructurePlacements() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
