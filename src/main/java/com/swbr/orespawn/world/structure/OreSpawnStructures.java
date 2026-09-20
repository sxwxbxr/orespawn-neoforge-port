package com.swbr.orespawn.world.structure;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.OreSpawnWorld;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry holders of the structure infrastructure: the structure type {@code orespawn:legacy} and its piece type
 * {@code orespawn:legacy}. Lives next to the classes it registers because W12 owns this package, not
 * {@code registry/}; the integrator may move it.
 *
 * <p>Both registries must be filled before datapack registries are decoded (structure JSON names the type, saved
 * chunks name the piece), so {@link #STRUCTURE_TYPES} and {@link #STRUCTURE_PIECES} are attached to the mod bus in
 * the mod constructor. {@link #init()} also fills the dispatcher and builder tables of
 * {@link LegacyStructureRegistry} for {@code OreSpawnWorld}.
 */
public final class OreSpawnStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, OreSpawn.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, OreSpawn.MOD_ID);

    private static final StructureType<LegacyStructure> LEGACY_TYPE = () -> LegacyStructure.CODEC;

    /** {@code orespawn:legacy}, see {@link LegacyStructure}. */
    public static final DeferredHolder<StructureType<?>, StructureType<LegacyStructure>> LEGACY =
            STRUCTURE_TYPES.register("legacy", () -> LEGACY_TYPE);

    private static final StructurePieceType.ContextlessType LEGACY_PIECE_TYPE = LegacyStructurePiece::new;

    /** {@code orespawn:legacy}, see {@link LegacyStructurePiece}. */
    public static final DeferredHolder<StructurePieceType, StructurePieceType> LEGACY_PIECE =
            STRUCTURE_PIECES.register("legacy", () -> LEGACY_PIECE_TYPE);

    private OreSpawnStructures() {
    }

    /** Loads the holders and registers the {@code OreSpawnWorld} dispatchers and builders. */
    public static void init() {
        OreSpawnWorld.bootstrap();
    }
}
