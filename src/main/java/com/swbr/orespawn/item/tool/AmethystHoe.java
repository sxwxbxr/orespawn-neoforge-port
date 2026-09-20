package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.AmethystHoe} (AmethystHoe.java:9-30): an {@code ItemHoe} on
 * {@code toolAMETHYST}, stack 1, {@code setMaxDamage(2000)} (:14), tab Tools (:15); registered as
 * {@code amethysthoe} without a harvest level (OreSpawnMain.java:1339). Vanilla tilling.
 *
 * <p>{@code getDamageVsEntity} (:18-20) and {@code getMaterialName} (:22-24) are dead; a 1.7.10
 * hoe had no attack modifier ({@link OreSpawnTiers#hoe}).
 */
public class AmethystHoe extends HoeItem {

    public AmethystHoe(Material material, Item.Properties props) {
        super(material.withUses(2000), props.attributes(OreSpawnTiers.hoe(material)));
    }
}
