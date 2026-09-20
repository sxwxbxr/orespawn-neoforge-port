package com.swbr.orespawn.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.OreSpawn;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * The one structure type of the port, {@code orespawn:legacy}. Every OreSpawn build that does not fit the 3x3-chunk
 * window of a feature, or that took part in {@code OreSpawnWorld.recently_placed}, is a JSON structure of this type
 * (DECISIONS R12):
 *
 * <pre>{@code
 * {
 *   "type": "orespawn:legacy",
 *   "dispatcher": "orespawn:overworld_dungeon",
 *   "biomes": "#minecraft:is_overworld",
 *   "step": "top_layer_modification",
 *   "spawn_overrides": {},
 *   "terrain_adaptation": "none"
 * }
 * }</pre>
 *
 * <p>{@code findGenerationPoint} runs the named {@link LegacyStructureRegistry.Dispatcher}; its placement becomes one
 * {@link LegacyStructurePiece}. The structure set of the JSON decides how often a chunk is asked at all - that is
 * the replacement of {@code recently_placed} (DECISIONS R18), documented per set in {@code OreSpawnWorld}.
 *
 * <p>Convention for the data files: structure and structure set share a name, the dispatcher id is
 * {@code orespawn:<name>}, {@code step} is always {@code top_layer_modification} (DECISIONS R24, after every
 * vanilla feature; {@link LegacyStructurePass} writes them once more at the end of each decoration), biomes name the dimension's biome list (a vanilla dimension tag or the fixed OreSpawn
 * biome), {@code terrain_adaptation} is always {@code none} (the original never adapted terrain), and a structure
 * that needs its own biome filter reads it in the dispatcher through {@link LegacyBiomeNames}.
 */
public final class LegacyStructure extends Structure {

    public static final MapCodec<LegacyStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Codec.STRING.fieldOf("dispatcher").forGetter(LegacyStructure::dispatcher)
    ).apply(instance, LegacyStructure::new));

    private final String dispatcher;

    public LegacyStructure(final StructureSettings settings, final String dispatcher) {
        super(settings);
        this.dispatcher = dispatcher;
    }

    public String dispatcher() {
        return this.dispatcher;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(final GenerationContext context) {
        final Optional<LegacyStructureRegistry.Dispatcher> rule = LegacyStructureRegistry.dispatcher(this.dispatcher);
        if (rule.isEmpty()) {
            OreSpawn.LOG.warn("orespawn:legacy structure names unknown dispatcher {}", this.dispatcher);
            return Optional.empty();
        }
        final LegacyStructureContext legacy = LegacyStructureContext.create(context, this.dispatcher);
        final List<LegacyStructureRegistry.Placement> placements = rule.get().locate(legacy);
        if (placements.isEmpty()) {
            return Optional.empty();
        }
        final List<LegacyStructurePiece> pieces = new ArrayList<>(placements.size());
        for (int i = 0; i < placements.size(); ++i) {
            pieces.add(new LegacyStructurePiece(placements.get(i), legacy.pieceSeed(i)));
        }
        final LegacyStructureRegistry.Placement first = placements.get(0);
        return Optional.of(new GenerationStub(new BlockPos(first.x(), first.y(), first.z()),
                builder -> pieces.forEach(builder::addPiece)));
    }

    @Override
    public StructureType<?> type() {
        return OreSpawnStructures.LEGACY.get();
    }
}
