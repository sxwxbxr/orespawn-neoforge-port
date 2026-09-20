package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.AmethystSword} (AmethystSword.java:9-44): an {@code ItemSword} on
 * {@code toolAMETHYST}, stack 1, {@code setMaxDamage(2000)} (:19), tab Combat (:20); registered as
 * {@code amethystsword} (OreSpawnMain.java:1336). Attack {@code 4 + 11} in 1.7.10, see
 * {@link OreSpawnTiers#sword} for the R6 conversion.
 *
 * <p>Blocks for 3500 ticks: {@code getMaxItemUseDuration} (:36-38) is live in the jar as
 * {@code func_77626_a} (checked in AmethystSword.class), DECISIONS R19.
 *
 * <p>Not ported, dead in the jar: {@code weaponDamage = 18} with {@code getDamageVsEntity} (:17,
 * :23-25), {@code getMaterialName} (:27-29) and {@code hitEntity(ItemStack, EntityLiving,
 * EntityLiving)} (:31-34) - the real signature takes {@code EntityLivingBase}, so vanilla
 * {@code ItemSword.hitEntity} (one durability per hit, now {@code SwordItem.postHurtEnemy}) ran.
 */
public class AmethystSword extends BlockingSword {

    public AmethystSword(Material material, Item.Properties props) {
        super(material.withUses(2000), props.attributes(OreSpawnTiers.sword(material)), 3500);
    }
}
