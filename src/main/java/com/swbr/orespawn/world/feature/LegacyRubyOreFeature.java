package com.swbr.orespawn.world.feature;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.structure.StructureWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * {@code orespawn:legacy_ruby_ore}: the Ruby step of {@code OreSpawnWorld.generateOres} (OreSpawnWorld.java:876-886),
 * one attempt at the position {@code orespawn:legacy_ore_position} rolled:
 * <pre>{@code
 * for (m = randPosY; m > 5; --m)
 *     if (getBlock(x, m, z) == lava || == flowing_lava)
 *         if (getBlock(x, m - 1, z) == stone) { setBlockFast(x, m - 1, z, MyOreRubyBlock, 0, 2); break; }
 * }</pre>
 * The scan does not stop at lava over something else; it goes on down, as in the original.
 *
 * <p>PORT: {@code Blocks.stone} is the stone category of DECISIONS R22 ({@code #minecraft:stone_ore_replaceables} and
 * {@code #minecraft:deepslate_ore_replaceables}, as the veins of {@link LegacyMinableFeature}). The Mining dimension's
 * copy ({@code OreSpawnWorldOres.generateRuby}) keeps {@code minecraft:stone}: that dimension has no other stone.
 */
public final class LegacyRubyOreFeature extends Feature<NoneFeatureConfiguration> {

    public LegacyRubyOreFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final BlockPos origin = context.origin();
        final int randPosX = origin.getX();
        final int randPosY = origin.getY();
        final int randPosZ = origin.getZ();
        final StructureWriter world = StructureWriter.forFeature(context.level(), randPosX >> 4, randPosZ >> 4);
        final BlockState ruby = ModBlocks.ORERUBY.get().defaultBlockState();
        for (int m = randPosY; m > 5; --m) {
            BlockState bid = world.getBlock(randPosX, m, randPosZ);
            if (bid.is(Blocks.LAVA)) {
                bid = world.getBlock(randPosX, m - 1, randPosZ);
                if (bid.is(BlockTags.STONE_ORE_REPLACEABLES) || bid.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
                    world.setBlock(randPosX, m - 1, randPosZ, ruby);
                    return true;
                }
            }
        }
        return false;
    }
}
