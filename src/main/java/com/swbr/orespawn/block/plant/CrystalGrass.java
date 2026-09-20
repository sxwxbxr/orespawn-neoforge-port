package com.swbr.orespawn.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

/**
 * Port of {@code danger.orespawn.CrystalGrass}: Crystal Grass ({@code crystalgrass},
 * OreSpawnMain.java:1528, {@code (0.6f, 2.0f)}), the surface block of the Crystal dimension. A
 * {@code Material.grass} block, tab Blocks, no random ticks - it never spreads (CrystalGrass.java:20-26).
 *
 * <ul>
 *   <li>{@code canSustainPlant} is {@code true} for every plant of every type (:58-60) -
 *       {@link #canSustainPlant} answers {@link TriState#TRUE}. This is what lets rice, quinoa, the
 *       crystal saplings and the flowers grow on it.</li>
 *   <li>{@code isBlockSolidOnSide} (:28-30) and {@code getBlockTexture} (:41-52) carry 1.6 names and
 *       were dead code.</li>
 *   <li>Drops itself (:54-56) - loot table {@code blocks/crystalgrass}.</li>
 * </ul>
 *
 * <p><b>Opacity (DECISIONS R18, catalogue 6.7).</b> {@code isOpaqueCube}/{@code renderAsNormalBlock}
 * returned {@code current_dimension == DimensionID5} (:62-68) - a static the client GUI wrote each
 * frame and a dedicated server never did. Two things follow, and they are decided differently:
 * <ol>
 *   <li>The light opacity was fixed at construction, when the static was still 0: 0, light passes
 *       as through glass, everywhere and on every side. Unambiguous, kept ({@link #getLightBlock},
 *       {@link #propagatesSkylightDown}).</li>
 *   <li>Everything that asked at runtime - face culling, torch support, spawning, redstone -
 *       saw a normal cube in single player inside the Crystal dimension (the shared static) and a
 *       non-cube on a dedicated server or anywhere else. R18 case 4 (client and server disagreed).
 *       PORT: the block is the normal cube the single-player Crystal dimension had, since that is
 *       where it exists naturally; {@code CrystalWood} and {@code BlockCrystalTreeLog}, whose answer
 *       was {@code false} unconditionally, keep the non-cube semantics (see {@code block.tree.CrystalCube}).</li>
 * </ol>
 * The textures have transparent pixels (design/design-blocks.md), so the model declares the cutout
 * render type.
 */
public class CrystalGrass extends Block {

    public CrystalGrass(Properties properties) {
        super(properties);
    }

    /** {@code canSustainPlant(...) { return true; }} (CrystalGrass.java:58-60). */
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition,
                                    Direction facing, BlockState plant) {
        return TriState.TRUE;
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
