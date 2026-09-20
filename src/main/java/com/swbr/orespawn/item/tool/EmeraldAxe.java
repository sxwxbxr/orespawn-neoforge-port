package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.EmeraldAxe} (EmeraldAxe.java:9-33): an {@code ItemAxe} on
 * {@code toolEMERALD}, stack 1, {@code setMaxDamage(1300)} (:17), tab Tools (:18); registered as
 * {@code emeraldaxe} without a {@code setHarvestLevel} call (OreSpawnMain.java:1324) - the tier's
 * level 3 (diamond tag) applies.
 *
 * <p>{@code weaponDamage = 10} / {@code getDamageVsEntity} (:15, :21-23) and {@code getMaterialName}
 * (:25-27) are dead. Attack {@code 3 + material}.
 */
public class EmeraldAxe extends AxeItem {

    public EmeraldAxe(Material material, Item.Properties props) {
        super(material.withUses(1300), props.attributes(OreSpawnTiers.axe(material)));
    }
}
