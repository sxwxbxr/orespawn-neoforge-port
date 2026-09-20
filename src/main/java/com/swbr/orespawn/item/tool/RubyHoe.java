package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.RubyHoe} (RubyHoe.java:9-30): an {@code ItemHoe} on
 * {@code toolRUBY}, stack 1, {@code setMaxDamage(1500)} (:14), tab Tools (:15); registered as
 * {@code rubyhoe} without a harvest level (OreSpawnMain.java:1334). {@code getDamageVsEntity} and
 * {@code getMaterialName} (:18-24) are dead.
 */
public class RubyHoe extends HoeItem {

    public RubyHoe(Material material, Item.Properties props) {
        super(material.withUses(1500), props.attributes(OreSpawnTiers.hoe(material)));
    }
}
