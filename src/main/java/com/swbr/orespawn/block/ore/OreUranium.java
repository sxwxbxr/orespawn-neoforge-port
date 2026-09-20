package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code OreUranium} (OreUranium.java:13-113): Uranium Ore, id {@code oreuranium}
 * (OreSpawnMain.java:1274). Line-identical to {@link OreTitanium} except for hardness 10, resistance 1
 * (:22-23) and a glow counter of 10 instead of 5 (:50).
 */
public class OreUranium extends OreTitanium {

    public OreUranium(BlockBehaviour.Properties properties) {
        super(properties, 10);
    }

    /** {@code Material.rock}, hardness 10, resistance 1, random ticks (:19-26). */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(10.0f, Legacy.resistance(1.0f))
                .requiresCorrectToolForDrops()
                .randomTicks();
    }
}
