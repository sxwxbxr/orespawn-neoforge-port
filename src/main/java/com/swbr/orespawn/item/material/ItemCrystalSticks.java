package com.swbr.orespawn.item.material;

import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemCrystalSticks}: a plain {@code Item} on the Materials tab
 * (ItemCrystalSticks.java:11), stack size 64, no behaviour. One instance: {@code crystalsticks}
 * "Crystal Shards" (OreSpawnMain.java:1390).
 *
 * <p>The tab goes through {@code ModCreativeTabs.add(OriginalTab.MATERIALS, holder)} in the
 * registry holder; the icon is {@code models/item/crystalsticks.json} on the original texture
 * (ItemCrystalSticks.java:15-16 registered {@code OreSpawn:<unlocalizedName>}).
 */
public class ItemCrystalSticks extends Item {

    public ItemCrystalSticks(Item.Properties props) {
        super(props);
    }
}
