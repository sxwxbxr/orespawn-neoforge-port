package com.swbr.orespawn.block.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.CrystalWood}: the Crystal Planks ({@code crystalplanks},
 * OreSpawnMain.java:1531, {@code (1.5f, 4.0f)}). A plain {@code Material.wood} block, tab Blocks,
 * no random ticks (CrystalWood.java:12-18), that is never an opaque or normal cube
 * (CrystalWood.java:25-31) - see {@link CrystalCube} for everything that follows from that.
 *
 * <p>Hardness, resistance and the (default, stone) step sound sit on the registration line, as in
 * the original. Fuel value 300 in the Crystal Furnace comes with W03 (verhalten/itemblock-03.md).
 */
public class CrystalWood extends Block {

    public CrystalWood(Properties properties) {
        super(CrystalCube.notANormalCube(properties));
    }

    /** {@code isNormalCube() == false}: no torch, lever or door attaches (CrystalWood.java:29-31). */
    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    /** {@code lightOpacity} was 0 because {@code isOpaqueCube()} was false at construction. */
    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
