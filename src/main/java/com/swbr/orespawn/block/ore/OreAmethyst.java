package com.swbr.orespawn.block.ore;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Port of {@code OreAmethyst} (OreAmethyst.java:12-43): Amethyst Ore, id {@code oreamethyst}
 * (OreSpawnMain.java:1522). Line-identical to {@link OreRuby}; the only difference, the drop item
 * {@code amethyst} (:27-29), lives in the loot table {@code orespawn:blocks/oreamethyst}.
 */
public class OreAmethyst extends OreRuby {

    public OreAmethyst(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.rock}, hardness 10, resistance 4 (:15-17) - the same as Ruby Ore. */
    public static BlockBehaviour.Properties originalProperties() {
        return OreRuby.originalProperties();
    }
}
