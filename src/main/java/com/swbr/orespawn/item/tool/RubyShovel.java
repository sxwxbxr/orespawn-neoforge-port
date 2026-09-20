package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

/**
 * Port of {@code danger.orespawn.RubyShovel} (RubyShovel.java:9-30): an {@code ItemSpade} on
 * {@code toolRUBY}, stack 1, {@code setMaxDamage(1500)} (:14), tab Tools (:15); registered as
 * {@code rubyshovel} with harvest level 5 (OreSpawnMain.java:1333). {@code getDamageVsEntity} and
 * {@code getMaterialName} (:18-24) are dead. Attack {@code 1 + 16}. Path flattening: see
 * {@link AmethystShovel}.
 */
public class RubyShovel extends ShovelItem {

    public RubyShovel(Material material, Item.Properties props) {
        super(material.withUses(1500), props.attributes(OreSpawnTiers.shovel(material)));
    }
}
