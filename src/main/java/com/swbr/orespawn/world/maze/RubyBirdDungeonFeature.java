package com.swbr.orespawn.world.maze;

import com.swbr.orespawn.world.tree.LegacyRandom;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.tree.LegacyWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * {@link RubyBirdDungeon} as a {@link Feature} (DECISIONS R12; verhalten/world-04.md, "RubyBirdDungeon",
 * "Portierung"): the room is built with its origin at the placement position. No original class.
 *
 * <p>Where the room goes is {@code OreSpawnWorld}'s rule, not this class's: the lava search of
 * {@code addRubyDungeon} (Utopia, 1/15, eight tries at chunk+0..7, Y 50 down to 6) and the grass search of
 * {@code addD4RubyDungeon} (Islands). {@code OreSpawnWorld} either places this feature at the found position or
 * calls {@link RubyBirdDungeon#makeDungeon(com.swbr.orespawn.world.tree.LegacyWriter, java.util.Random, int, int, int)}
 * with its own populate random; the origin must lie in chunk+0..7 so the +9 extent stays in the 3x3 window.
 */
public class RubyBirdDungeonFeature extends Feature<NoneFeatureConfiguration> {

    public RubyBirdDungeonFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final BlockPos origin = context.origin();
        // Clipped to the 3x3 chunks around the origin chunk, the feature window (StructureWriter.forFeature).
        RubyBirdDungeon.makeDungeon(
                LegacyWriter.of(StructureWriter.forFeature(context.level(), origin.getX() >> 4, origin.getZ() >> 4)),
                new LegacyRandom(context.random()),
                origin.getX(), origin.getY(), origin.getZ());
        return true;
    }
}
