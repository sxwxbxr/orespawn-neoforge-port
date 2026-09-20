package com.swbr.orespawn.world.dimension.village;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.world.dimension.utopia.ChunkProviderOreSpawn;
import com.swbr.orespawn.world.gen.BiomeDecorator;
import com.swbr.orespawn.world.gen.LegacyChunk;
import com.swbr.orespawn.world.gen.LegacyWorld;
import com.swbr.orespawn.world.gen.MapGenStronghold;
import com.swbr.orespawn.world.gen.WorldGenDungeons;
import com.swbr.orespawn.world.gen.WorldGenLakes;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Port of {@code ChunkProviderOreSpawn3} (ChunkProviderOreSpawn3.java:16-366), the generator of the
 * VillageMania dimension {@code orespawn:village} ("Dimension-VillageMania"; verhalten/world-03.md
 * "ChunkProviderOreSpawn3"). Registered under the chunk generator type {@code orespawn:village}.
 *
 * <p>The original is a copy of {@code ChunkProviderOreSpawn} - same noise, surface, caves, ravines and one ore
 * pass - with three differences, which are all this class overrides:
 * <ul>
 * <li>{@code provideChunk} also starts {@link MapGenMoreVillages} and the scattered features (:166-169): in
 * 1.21.1 the structure set {@code orespawn:village_mania} places the villages; scattered features need a
 * desert, jungle or swamp biome and never start here.</li>
 * <li>{@code populate} (:268-324): structures, water lake, lava lake, eight dungeons, the decorator, spawns,
 * ice and snow - see {@link #populate}.</li>
 * <li>{@code getPossibleCreatures} (:345-348): the biome list, or the witch list inside a witch hut. There are
 * no witch huts (above), so the biome list is returned unchanged; {@link #hasNaturalMonsters} is true.</li>
 * </ul>
 * Makeup string "MiningDimension" (:341-343), a copy-paste name of the original, has no port.
 */
public class ChunkProviderOreSpawn3 extends ChunkProviderOreSpawn {

    public static final MapCodec<ChunkProviderOreSpawn3> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource))
            .apply(instance, instance.stable(ChunkProviderOreSpawn3::new)));

    public ChunkProviderOreSpawn3(final BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /** {@code getPossibleCreatures} (:345-348) keeps the biome's monster list. */
    @Override
    protected boolean hasNaturalMonsters() {
        return true;
    }

    /**
     * {@code func_147416_a} (:350-352): {@code "Stronghold"} answers from {@code strongholdGenerator}, the three
     * 1.7.10 ring positions ({@link MapGenStronghold#findNearestMapStructure}); everything else as vanilla.
     */
    @Override
    @Nullable
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(final ServerLevel level, final HolderSet<Structure> structure,
                                                                   final BlockPos pos, final int searchRadius,
                                                                   final boolean skipKnownStructures) {
        return MapGenStronghold.findNearestMapStructure(level, structure, pos, skipKnownStructures,
                super.findNearestMapStructure(level, structure, pos, searchRadius, skipKnownStructures));
    }

    /**
     * {@code populate} (:268-324), in the original order. PORT deviations:
     * <ul>
     * <li>Structure pieces: {@code mineshaftGenerator}, {@code villageGenerator}, {@code strongholdGenerator}
     * and {@code scatteredFeatureGenerator.generateStructuresInChunk} (:278-283) are placed by
     * {@code super.applyBiomeDecoration} after this method runs (DECISIONS R24, see
     * {@code ChunkProviderOreSpawn.applyBiomeDecoration}); the village flag only reads the structure starts. The villages come from
     * {@code orespawn:village_mania}; mineshafts from the vanilla {@code mineshafts} set through the biome tag
     * {@code has_structure/mineshaft} (DECISIONS R18: the original provider calls both generators).
     * Strongholds come from {@code orespawn:strongholds}, exactly the three ring positions of the default
     * {@code MapGenStronghold} of :48 ({@link MapGenStronghold}), not from the vanilla {@code strongholds} set
     * with its up to 128 positions on the 1.21.1 rings. In 1.7.10 their starts were only created by
     * {@code recreateStructures} for chunks loaded from disk, so they appeared in fragments; 1.21.1 generates
     * whole structures (not rebuildable, Fall 3).</li>
     * <li>The village flag of :280 is {@link MapGenMoreVillages#generateStructuresInChunk}.</li>
     * <li>{@code BlockFalling.fallInstantly = true} (:269, :323): no 1.21.1 equivalent. It only mattered for
     * falling-block ticks run during populate, and none were run.</li>
     * <li>Ice and snow (:310-322): {@code isBlockFreezable} and {@code canSnowAt} both return false first thing
     * above temperature 0.15, and the biome has 0.7 at every height ({@link LegacyWorld#isBlockFreezable}); the
     * loop draws no random number, so it is omitted.</li>
     * </ul>
     */
    @Override
    protected void populate(final WorldGenLevel level, final StructureManager structureManager, final int par2,
                            final int par3) {
        final int k = par2 * 16;
        final int l = par3 * 16;
        // the biome at (k + 16, l + 16) (:272) is always orespawn:villages, never desert or desertHills (:284)
        final Random rand = LegacyChunk.populateRandom(level.getSeed(), par2, par3);
        final LegacyWorld world = new LegacyWorld(level, TOP_BLOCK);
        final boolean flag = MapGenMoreVillages.generateStructuresInChunk(level, structureManager, par2, par3);
        if (!flag && rand.nextInt(4) == 0) {
            final int k2 = k + rand.nextInt(16) + 8;
            final int l2 = rand.nextInt(256);
            final int i2 = l + rand.nextInt(16) + 8;
            new WorldGenLakes(WATER).generate(world, rand, k2, l2, i2);
        }
        if (!flag && rand.nextInt(8) == 0) {
            final int k2 = k + rand.nextInt(16) + 8;
            final int l2 = rand.nextInt(rand.nextInt(248) + 8);
            final int i2 = l + rand.nextInt(16) + 8;
            if (l2 < 63 || rand.nextInt(10) == 0) {
                new WorldGenLakes(LAVA).generate(world, rand, k2, l2, i2);
            }
        }
        for (int k2 = 0; k2 < 8; ++k2) {
            final int l2 = k + rand.nextInt(16) + 8;
            final int i2 = rand.nextInt(256);
            final int j2 = l + rand.nextInt(16) + 8;
            new WorldGenDungeons().generate(world, rand, l2, i2, j2);
        }
        BiomeDecorator.utopianPlains().decorateChunk(world, rand, k, l);
        // SpawnerAnimals.performWorldGenSpawning (:309) is spawnOriginalMobs.
    }
}
