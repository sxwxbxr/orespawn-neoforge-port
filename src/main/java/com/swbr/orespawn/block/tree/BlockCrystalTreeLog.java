package com.swbr.orespawn.block.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.BlockCrystalTreeLog}: Crystal Tree Wood ({@code crystaltreelog},
 * OreSpawnMain.java:1628, hardness 0.2, wood sound). A {@code BlockRotatedPillar} of
 * {@code Material.wood}, tab Blocks (BlockCrystalTreeLog.java:20-23); the second constructor
 * argument (20) was unused.
 *
 * <ul>
 *   <li>{@code canSustainLeaves}/{@code isWood} true (:29-35) - {@link CanSustainLeaves}; the
 *       {@code #minecraft:logs} tag membership covers {@code isWood} for vanilla consumers.</li>
 *   <li>{@code isOpaqueCube}/{@code renderAsNormalBlock} false (:37-43) - {@link CrystalCube}.</li>
 *   <li>Drops itself with meta 0 (:25-27, :45-47) - loot table {@code blocks/crystaltreelog}.</li>
 *   <li>Side and top textures (:50-63) - {@code block/crystaltreelog} with {@code _top}.</li>
 * </ul>
 */
public class BlockCrystalTreeLog extends RotatedPillarBlock implements CanSustainLeaves {

    public BlockCrystalTreeLog(Properties properties) {
        super(CrystalCube.notANormalCube(properties));
    }

    /** {@code isNormalCube() == false}: the Crystal Torch attaches by name instead (BlockCrystalTorch.java:52). */
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
