package com.swbr.orespawn.item.material;

import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemSalt}: a plain {@code Item} on the Materials tab
 * (ItemSalt.java:11), stack size 64, no behaviour. The original used the class for 27 items
 * (OreSpawnMain.java:1374-1384, :1422-1431, :1503, :1521-1525, :1613; manifest class
 * {@code ItemSalt}): the boss scales, teeth and other mob drops, the three Big Bertha parts,
 * salt, ruby, amethyst, the two nuggets and the dead stink bug.
 *
 * <p>The tab goes through {@code ModCreativeTabs.add(OriginalTab.MATERIALS, holder)} in the
 * registry holder; each icon is {@code models/item/<id>.json} on the original texture
 * (ItemSalt.java:15-16 registered {@code OreSpawn:<unlocalizedName>}).
 */
public class ItemSalt extends Item {

    public ItemSalt(Item.Properties props) {
        super(props);
    }
}
