package com.swbr.orespawn.block.misc;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Port of {@code BlockUranium} (BlockUranium.java:11-76): Uranium Block, id {@code blockuranium}
 * (OreSpawnMain.java:1278). Line-identical to {@link BlockTitanium} except for light 0.2 (:18).
 */
public class BlockUranium extends BlockTitanium {

    public BlockUranium(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.rock}, hardness 5, resistance 5, light 0.2 (:14-18). */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockTitanium.originalProperties(0.2f);
    }
}
