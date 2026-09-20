package com.swbr.orespawn.block.tree;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * What {@code isOpaqueCube() == false} and {@code renderAsNormalBlock() == false} meant for a full
 * cube in 1.7.10, spelled out as 1.21.1 properties. {@code CrystalWood} (CrystalWood.java:25-31) and
 * {@code BlockCrystalTreeLog} (BlockCrystalTreeLog.java:37-43) return {@code false} from both
 * unconditionally, and Forge 1.7.10 derived four things from that single answer:
 *
 * <table>
 * <tr><th>1.7.10 consumer</th><th>effect</th><th>1.21.1 counterpart</th></tr>
 * <tr><td>{@code Block.isNormalCube()} = {@code material.isOpaque() && renderAsNormalBlock() && !canProvidePower()}</td>
 *     <td>{@code isSideSolid} false on every face: no vanilla torch, lever, ladder, door, rail or
 *         pressure plate attaches - the reason {@code BlockCrystalTorch} lists these blocks as
 *         supports by name (BlockCrystalTorch.java:50-53)</td>
 *     <td>{@code getBlockSupportShape} = empty ({@code SupportType} checks)</td></tr>
 * <tr><td>{@code SpawnerAnimals.canCreatureTypeSpawnAtLocation} via {@code doesBlockHaveSolidTopSurface}</td>
 *     <td>no natural spawn on the block</td>
 *     <td>{@code isValidSpawn} never</td></tr>
 * <tr><td>{@code World.isBlockNormalCubeDefault} in redstone wiring</td>
 *     <td>does not conduct redstone</td>
 *     <td>{@code isRedstoneConductor} never</td></tr>
 * <tr><td>{@code EntityLivingBase.isEntityInsideOpaqueBlock}</td>
 *     <td>no suffocation, no view blocking</td>
 *     <td>{@code isSuffocating}/{@code isViewBlocking} never</td></tr>
 * <tr><td>{@code Block} constructor: {@code lightOpacity = isOpaqueCube() ? 255 : 0}</td>
 *     <td>light passes as through glass</td>
 *     <td>{@code getLightBlock} 0, {@code propagatesSkylightDown} true - overridden in the class,
 *         because these are methods, not properties</td></tr>
 * <tr><td>renderer</td><td>no face culling against neighbours</td>
 *     <td>{@code noOcclusion()} plus cutout render type (DECISIONS R18)</td></tr>
 * </table>
 *
 * <p>{@code CrystalGrass} shares the pattern only on the client and only outside the Crystal
 * dimension (CrystalGrass.java:62-68 reads a client static); see that class for the ruling.
 */
final class CrystalCube {

    private CrystalCube() {}

    /** The property half of the table above; the two light methods are overridden per class. */
    static BlockBehaviour.Properties notANormalCube(BlockBehaviour.Properties properties) {
        return properties
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }
}
