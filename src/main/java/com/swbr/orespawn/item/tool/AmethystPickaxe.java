package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

/**
 * Port of {@code danger.orespawn.AmethystPickaxe} (AmethystPickaxe.java:9-37): an
 * {@code ItemPickaxe} on {@code toolAMETHYST}, stack 1, {@code setMaxDamage(2000)} (:17), tab Tools
 * (:18); registered as {@code amethystpickaxe} with harvest level 4 (OreSpawnMain.java:1337), which
 * is the netherite tag (R6).
 *
 * <p>{@code weaponDamage = 12} with both {@code getDamageVsEntity} variants (:15, :21-27) and
 * {@code getMaterialName} (:29-31) are dead. Attack {@code 2 + material}.
 */
public class AmethystPickaxe extends PickaxeItem {

    public AmethystPickaxe(Material material, Item.Properties props) {
        super(material.withUses(2000), props.attributes(OreSpawnTiers.pickaxe(material)));
    }
}
