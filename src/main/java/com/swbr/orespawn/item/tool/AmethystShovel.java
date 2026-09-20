package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

/**
 * Port of {@code danger.orespawn.AmethystShovel} (AmethystShovel.java:9-30): an {@code ItemSpade}
 * on {@code toolAMETHYST}, stack 1, {@code setMaxDamage(2000)} (:14), tab Tools (:15); registered
 * as {@code amethystshovel} with harvest level 4 (OreSpawnMain.java:1338).
 *
 * <p>{@code getDamageVsEntity} (:18-20) and {@code getMaterialName} (:22-24) are dead. Attack
 * {@code 1 + material}.
 *
 * <p>PORT: {@code ShovelItem.useOn} flattens grass into a dirt path; 1.7.10 had no paths at all
 * (catalogue itemblock-01, AmethystShovel). Accepted as the vanilla behaviour of the class.
 */
public class AmethystShovel extends ShovelItem {

    public AmethystShovel(Material material, Item.Properties props) {
        super(material.withUses(2000), props.attributes(OreSpawnTiers.shovel(material)));
    }
}
