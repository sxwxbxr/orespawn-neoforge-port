package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.AmethystAxe} (AmethystAxe.java:9-33): an {@code ItemAxe} on
 * {@code toolAMETHYST}, stack 1, {@code setMaxDamage(2000)} (:17), tab Tools (:18).
 * Registered as {@code amethystaxe} with {@code setHarvestLevel("axe", amethyst_stats.harvestlevel)}
 * (OreSpawnMain.java:1340) - the level is the tier's tag here.
 *
 * <p>Dead in the jar and not ported: {@code weaponDamage = 12} with {@code getDamageVsEntity(Entity)}
 * (:15, :21-23) and {@code getMaterialName()} (:25-27) override nothing (catalogue itemblock-01
 * section 0.1). Attack is {@code 3 + material}, see {@link OreSpawnTiers#axe}.
 */
public class AmethystAxe extends AxeItem {

    public AmethystAxe(Material material, Item.Properties props) {
        // setMaxDamage(2000): fixed, independent of Amethyst_maxuses (catalogue itemblock-01, 0.2).
        super(material.withUses(2000), props.attributes(OreSpawnTiers.axe(material)));
    }
}
