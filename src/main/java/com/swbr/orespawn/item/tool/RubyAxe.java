package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.RubyAxe} (RubyAxe.java:9-33): an {@code ItemAxe} on
 * {@code toolRUBY}, stack 1, {@code setMaxDamage(1500)} (:17), tab Tools (:18); registered as
 * {@code rubyaxe} with harvest level 5 (OreSpawnMain.java:1335) - above diamond, netherite tag (R6).
 *
 * <p>{@code weaponDamage = 12} / {@code getDamageVsEntity} (:15, :21-23) and {@code getMaterialName}
 * (:25-27) are dead. Attack {@code 3 + 16}.
 */
public class RubyAxe extends AxeItem {

    public RubyAxe(Material material, Item.Properties props) {
        super(material.withUses(1500), props.attributes(OreSpawnTiers.axe(material)));
    }
}
