package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.CrystalHoe} (CrystalHoe.java:8-20): an {@code ItemHoe} on the
 * material passed in, stack 1, tab Tools, material durability. Four registrations
 * (OreSpawnMain.java:1344, :1349, :1354, :1359): {@code crystalwoodhoe}, {@code crystalpinkhoe},
 * {@code crystalstonehoe}, {@code tigerseye_hoe}.
 */
public class CrystalHoe extends HoeItem {

    public CrystalHoe(Material material, Item.Properties props) {
        super(material, props.attributes(OreSpawnTiers.hoe(material)));
    }
}
