package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.EmeraldSword} (EmeraldSword.java:11-55): an {@code ItemSword} on
 * {@code toolEMERALD}, stack 1, {@code setMaxDamage(1300)} (:21), tab Combat (:22). Two
 * registrations: {@code emeraldsword} (OreSpawnMain.java:1320) and {@code rosesword} (:1361).
 *
 * <p>Blocks for 3000 ticks: {@code getMaxItemUseDuration} (:47-49) is live in the jar as
 * {@code func_77626_a} (checked in EmeraldSword.class), DECISIONS R19.
 *
 * <p>Everything else in the class is inert: {@code onCreated}, {@code onUsingTick} and
 * {@code onUpdate} are empty overrides (:25-32), {@code weaponDamage = 15} with
 * {@code getDamageVsEntity} (:19, :34-36), {@code getMaterialName} (:38-40) and the
 * {@code EntityLiving}-typed {@code hitEntity} (:42-45) are dead.
 */
public class EmeraldSword extends BlockingSword {

    public EmeraldSword(Material material, Item.Properties props) {
        super(material.withUses(1300), props.attributes(OreSpawnTiers.sword(material)), 3000);
    }
}
