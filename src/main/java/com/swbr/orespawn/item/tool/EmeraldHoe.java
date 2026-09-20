package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.EmeraldHoe} (EmeraldHoe.java:9-30): an {@code ItemHoe} on
 * {@code toolEMERALD}, stack 1, {@code setMaxDamage(1300)} (:14), tab Tools (:15); registered as
 * {@code emeraldhoe} (OreSpawnMain.java:1323). {@code getDamageVsEntity} and
 * {@code getMaterialName} (:18-24) are dead.
 */
public class EmeraldHoe extends HoeItem {

    public EmeraldHoe(Material material, Item.Properties props) {
        super(material.withUses(1300), props.attributes(OreSpawnTiers.hoe(material)));
    }
}
