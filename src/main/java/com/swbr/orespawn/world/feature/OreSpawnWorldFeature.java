package com.swbr.orespawn.world.feature;

import com.swbr.orespawn.world.OreSpawnWorld;
import com.swbr.orespawn.world.structure.LegacyStructurePass;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * {@code orespawn:orespawn_world}: the per-chunk call of {@code OreSpawnWorld.generate} in the vanilla dimensions
 * (Overworld, Nether, End). Placed without placement modifiers, so the origin is the chunk corner, and added through
 * the biome modifiers {@code orespawn:orespawn_world_{overworld,nether,end}} at the last decoration step
 * ({@code top_layer_modification}): FML 1.7.10 ran the generator after the vanilla decoration of the chunk
 * ({@code GameRegistry.registerWorldGenerator(..., 10)}, OreSpawnMain.java:5035).
 *
 * <p>After the generator the feature runs {@link LegacyStructurePass} (DECISIONS R24): being the last feature of the
 * last step, it rewrites the {@code orespawn:legacy} structures over whatever this chunk's decoration put into the 3x3
 * chunks around it. That is why the End has the feature too, although {@code generateEnd} places no small decoration.
 *
 * <p>The six OreSpawn dimensions do not use this feature: their chunk generators call
 * {@link OreSpawnWorld#generate} directly after their ported 1.7.10 populate, where FML called it too.
 */
public final class OreSpawnWorldFeature extends Feature<NoneFeatureConfiguration> {

    public OreSpawnWorldFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final BlockPos origin = context.origin();
        OreSpawnWorld.generate(context.level(), origin.getX() >> 4, origin.getZ() >> 4);
        LegacyStructurePass.apply(context.level(), context.chunkGenerator(), new ChunkPos(origin));
        return true;
    }
}
