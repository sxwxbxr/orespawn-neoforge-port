package com.swbr.orespawn.registry;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.dimension.chaos.ChunkProviderOreSpawn6;
import com.swbr.orespawn.world.dimension.crystal.ChunkProviderOreSpawn5;
import com.swbr.orespawn.world.dimension.danger.ChunkProviderOreSpawn4;
import com.swbr.orespawn.world.dimension.mining.ChunkProviderOreSpawn2;
import com.swbr.orespawn.world.dimension.utopia.ChunkProviderOreSpawn;
import com.swbr.orespawn.world.dimension.village.ChunkProviderOreSpawn3;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Chunk generator codecs of the six OreSpawn dimensions (DECISIONS R13), one per
 * {@code ChunkProviderOreSpawnN}. {@code data/orespawn/dimension/<id>.json} names the generator type
 * {@code orespawn:<id>}; the registered value must be the very {@code MapCodec} each generator's
 * {@code codec()} returns, otherwise the dimension codec cannot dispatch and the datapack
 * dimension fails to decode - the level then silently does not exist.
 *
 * <p>Order = {@code DimensionID} .. {@code DimensionID6} of {@code OreSpawnMain}.
 */
public final class ModChunkGenerators {

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, OreSpawn.MOD_ID);

    /** Dimension-Utopia, {@code WorldProviderOreSpawn} / {@code ChunkProviderOreSpawn}. */
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChunkProviderOreSpawn>> UTOPIA =
            CHUNK_GENERATORS.register("utopia", () -> ChunkProviderOreSpawn.CODEC);
    /** Dimension-Extreme, {@code ChunkProviderOreSpawn2}. */
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChunkProviderOreSpawn2>> MINING =
            CHUNK_GENERATORS.register("mining", () -> ChunkProviderOreSpawn2.CODEC);
    /** Dimension-VillageMania, {@code ChunkProviderOreSpawn3}. */
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChunkProviderOreSpawn3>> VILLAGE =
            CHUNK_GENERATORS.register("village", () -> ChunkProviderOreSpawn3.CODEC);
    /** Dimension-Islands, {@code ChunkProviderOreSpawn4}. */
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChunkProviderOreSpawn4>> DANGER =
            CHUNK_GENERATORS.register("danger", () -> ChunkProviderOreSpawn4.CODEC);
    /** Dimension-Crystal, {@code ChunkProviderOreSpawn5}. */
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChunkProviderOreSpawn5>> CRYSTAL =
            CHUNK_GENERATORS.register("crystal", () -> ChunkProviderOreSpawn5.CODEC);
    /** Dimension-Chaos, {@code ChunkProviderOreSpawn6}. */
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChunkProviderOreSpawn6>> CHAOS =
            CHUNK_GENERATORS.register("chaos", () -> ChunkProviderOreSpawn6.CODEC);

    private ModChunkGenerators() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
