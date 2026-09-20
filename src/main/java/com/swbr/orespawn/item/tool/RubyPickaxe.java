package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

/**
 * Port of {@code danger.orespawn.RubyPickaxe} (RubyPickaxe.java:9-37): an {@code ItemPickaxe} on
 * {@code toolRUBY}, stack 1, {@code setMaxDamage(1500)} (:17), tab Tools (:18); registered as
 * {@code rubypickaxe} with harvest level 5 (OreSpawnMain.java:1332). Both {@code getDamageVsEntity}
 * variants (:21-27) and {@code getMaterialName} (:29-31) are dead. Attack {@code 2 + 16}.
 */
public class RubyPickaxe extends PickaxeItem {

    public RubyPickaxe(Material material, Item.Properties props) {
        super(material.withUses(1500), props.attributes(OreSpawnTiers.pickaxe(material)));
    }
}
