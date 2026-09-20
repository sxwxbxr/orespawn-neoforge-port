package com.swbr.orespawn.block.misc;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code BlockCrystal} (BlockCrystal.java:9-31), two storage blocks of the Crystal dimension:
 * {@code crystalpink_block} Pink Tourmaline Block (OreSpawnMain.java:1284) and {@code tigerseye_block}
 * Tiger's Eye Block (:1286). Glowing, translucent-textured cubes without behaviour.
 *
 * <p>{@code isOpaqueCube} and {@code renderAsNormalBlock} both false (:19-25): light passes
 * ({@link NonOpaqueBlock}), no face culling, nothing spawns on it, no redstone through it
 * ({@link Legacy#notANormalCube}); cutout rendering per DECISIONS R18. Drops itself.
 */
public class BlockCrystal extends NonOpaqueBlock {

    public BlockCrystal(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * {@code Material.rock}, hardness 4, then resistance 4 (:12-16). {@code setHardness(4)} first raised the
     * resistance to 20, {@code setResistance(4)} then overwrote it with 12 - stored order matters, and the
     * result is the plain {@code setResistance(4)} value.
     */
    public static BlockBehaviour.Properties originalProperties() {
        int light = Legacy.light(0.4f);
        return Legacy.notANormalCube(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(4.0f, Legacy.resistance(4.0f))
                .lightLevel(state -> light)
                .requiresCorrectToolForDrops());
    }
}
