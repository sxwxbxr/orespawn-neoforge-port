package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port of {@code danger.orespawn.BigHammer} (BigHammer.java:9-44): an {@code ItemSword} on
 * {@code toolAMETHYST}, stack 1, {@code setMaxDamage(9000)} (:19; the material says 2000), tab
 * Combat (:20); registered as {@code bighammer} (OreSpawnMain.java:1330). Attack {@code 4 + 11}
 * in 1.7.10 (R6).
 *
 * <p>{@code hitEntity} (:27-34, live): server-side {@code addVelocity(0, |rand * 2 / 3|, 0)} - zero
 * to 0.667 upwards - then {@code damageItem(1)} (vanilla {@code postHurtEnemy}). The unused local
 * {@code var2 = 5} (:28) has no effect. Dead: {@code weaponDamage = 15} (:17),
 * {@code getMaterialName} (:23-25).
 *
 * <p>{@code getMaxItemUseDuration} = 3000 (:36-38, live as {@code func_77626_a}): the inherited
 * {@code ItemSword} right-click block, so the hammer is a {@link BlockingSword} (DECISIONS R19).
 */
public class BigHammer extends BlockingSword {

    public BigHammer(Material material, Item.Properties props) {
        super(material.withUses(9000), props.attributes(OreSpawnTiers.sword(material)), 3000);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null && !target.level().isClientSide) {
            target.push(0.0, Math.abs(target.level().random.nextFloat() * 2.0f / 3.0f), 0.0);
            // PORT: Player.attack sends the knockback velocity packet before hurtEnemy runs
            // (Player.java, attack: hurtMarked block precedes hurtEnemy); flagging the entity again
            // makes ServerEntity broadcast the added motion - for players as well as mobs.
            target.hurtMarked = true;
        }
        return true;
    }
}
