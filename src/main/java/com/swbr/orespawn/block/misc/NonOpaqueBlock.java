package com.swbr.orespawn.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base for the OreSpawn blocks whose {@code isOpaqueCube()} returned {@code false}: BlockRuby, BlockCrystal,
 * OreCrystal and OreCrystalCrystal.
 *
 * <p>Engine fact behind it: the 1.7.10 {@code Block} constructor set
 * {@code lightOpacity = isOpaqueCube() ? 255 : 0}, so these blocks let block light and sky light through
 * untouched. In 1.21.1 a full-cube {@code noOcclusion()} block still counts as light block 1 and stops the
 * sky column ({@code propagatesSkylightDown} is false for a full shape); the two overrides restore the
 * original 0. Rendering side (no face culling) comes from {@code noOcclusion()} in the properties.
 *
 * <p>No original class.
 */
public class NonOpaqueBlock extends Block {

    public NonOpaqueBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
