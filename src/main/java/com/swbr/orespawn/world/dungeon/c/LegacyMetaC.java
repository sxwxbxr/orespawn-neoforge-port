package com.swbr.orespawn.world.dungeon.c;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;

/**
 * Metadata conversions the W12 {@code LegacyMeta} does not cover and only porter c's builders need. No original class.
 */
final class LegacyMetaC {

    private LegacyMetaC() {
    }

    /**
     * {@code Blocks.piston_extension} ({@code BlockPistonMoving}) with metadata: the low three bits are the
     * {@code Facing} index of the piston (0 down, 1 up, 2 north, 3 south, 4 west, 5 east, the order of
     * {@link Direction#from3DDataValue}), bit 8 sticky - the same layout as {@code BlockPistonBase}
     * ({@code LegacyMeta.piston}). DECISIONS R18: {@code piston_extension} becomes {@code moving_piston}. Without a
     * block entity the block has no shape and renders nothing, as the 1.7.10 block without its tile entity did.
     */
    static BlockState movingPiston(final int meta) {
        return Blocks.MOVING_PISTON.defaultBlockState()
                .setValue(MovingPistonBlock.FACING, Direction.from3DDataValue(meta & 7))
                .setValue(MovingPistonBlock.TYPE, (meta & 8) != 0 ? PistonType.STICKY : PistonType.DEFAULT);
    }
}
