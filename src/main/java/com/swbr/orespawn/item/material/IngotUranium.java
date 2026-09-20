package com.swbr.orespawn.item.material;

import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.IngotUranium}: a plain {@code Item} on the Materials tab
 * (IngotUranium.java:11), stack size 64, no behaviour. The original reused the class as a bare
 * instance carrier for three items (OreSpawnMain.java:1276, :1285, :1287):
 * {@code ingoturanium} "Uranium Ingot", {@code crystalpink_ingot} "Pink Tourmaline Ingot" and
 * {@code tigerseye_ingot} "Tiger's Eye Ingot".
 *
 * <p>The tab goes through {@code ModCreativeTabs.add(OriginalTab.MATERIALS, holder)} in the
 * registry holder; each icon is {@code models/item/<id>.json} on the original texture
 * (IngotUranium.java:15-16 registered {@code OreSpawn:<unlocalizedName>}).
 */
public class IngotUranium extends Item {

    public IngotUranium(Item.Properties props) {
        super(props);
    }
}
