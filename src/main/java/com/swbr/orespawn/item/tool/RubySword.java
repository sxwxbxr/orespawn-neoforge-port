package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.RubySword} (RubySword.java:9-44): an {@code ItemSword} on
 * {@code toolRUBY}, stack 1, {@code setMaxDamage(1500)} (:19), tab Combat (:20); registered as
 * {@code rubysword} (OreSpawnMain.java:1331). Attack {@code 4 + 16} in 1.7.10 (R6 conversion in
 * {@link OreSpawnTiers#sword}).
 *
 * <p>Blocks for 4000 ticks: {@code getMaxItemUseDuration} (:36-38) is live in the jar as
 * {@code func_77626_a} (checked in RubySword.class), DECISIONS R19.
 *
 * <p>Dead: {@code weaponDamage = 18} / {@code getDamageVsEntity} (:17, :23-25),
 * {@code getMaterialName} (:27-29), the {@code EntityLiving}-typed {@code hitEntity} (:31-34).
 */
public class RubySword extends BlockingSword {

    public RubySword(Material material, Item.Properties props) {
        super(material.withUses(1500), props.attributes(OreSpawnTiers.sword(material)), 4000);
    }
}
