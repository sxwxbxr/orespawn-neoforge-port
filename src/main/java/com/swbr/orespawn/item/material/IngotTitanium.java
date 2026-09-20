package com.swbr.orespawn.item.material;

import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.IngotTitanium}: a plain {@code Item} on the Materials tab
 * (IngotTitanium.java:11), stack size 64, no behaviour. One instance: {@code ingottitanium}
 * "Titanium Ingot" (OreSpawnMain.java:1277).
 *
 * <p>The tab goes through {@code ModCreativeTabs.add(OriginalTab.MATERIALS, holder)} in the
 * registry holder; the icon is {@code models/item/ingottitanium.json} on the original texture
 * (IngotTitanium.java:15-16 registered {@code OreSpawn:<unlocalizedName>}).
 */
public class IngotTitanium extends Item {

    public IngotTitanium(Item.Properties props) {
        super(props);
    }
}
