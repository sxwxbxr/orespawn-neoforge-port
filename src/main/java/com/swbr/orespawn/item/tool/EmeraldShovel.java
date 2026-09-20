package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

/**
 * Port of {@code danger.orespawn.EmeraldShovel} (EmeraldShovel.java:9-30): an {@code ItemSpade}
 * on {@code toolEMERALD}, stack 1, {@code setMaxDamage(1300)} (:14), tab Tools (:15); registered
 * as {@code emeraldshovel} (OreSpawnMain.java:1322). {@code getDamageVsEntity} and
 * {@code getMaterialName} (:18-24) are dead. Path flattening: see {@link AmethystShovel}.
 */
public class EmeraldShovel extends ShovelItem {

    public EmeraldShovel(Material material, Item.Properties props) {
        super(material.withUses(1300), props.attributes(OreSpawnTiers.shovel(material)));
    }
}
