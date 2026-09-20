package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Port of {@code danger.orespawn.MantisClaw} (MantisClaw.java:9-45): an {@code ItemSword} on
 * {@code toolEMERALD}, stack 1, {@code setMaxDamage(1000)} (:19; the material says 1300), tab
 * Combat (:20); registered as {@code mantisclaw} (OreSpawnMain.java:1329). Attack {@code 4 + 6}
 * in 1.7.10 (R6).
 *
 * <p>{@code hitEntity} (:27-35, live): server-side the target loses one health point and the
 * attacker gains one, then {@code damageItem(1)} (vanilla {@code postHurtEnemy}). Right-click
 * blocks for 3000 ticks ({@code getMaxItemUseDuration} :37-39, live - {@link BlockingSword}).
 * Dead: {@code weaponDamage = 10} (:17), {@code getMaterialName} (:23-25).
 */
public class MantisClaw extends BlockingSword {

    public MantisClaw(Material material, Item.Properties props) {
        super(material.withUses(1000), props.attributes(OreSpawnTiers.sword(material)), 3000);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target != null && attacker != null && !target.level().isClientSide) {
            // PORT: target.heal(-1.0f) - NeoForge's LivingEntity.heal returns early for amounts <= 0
            // (LivingEntity.java:1119), so the 1.7.10 body of heal is written out: setHealth(health - 1)
            // when the target still has health (R18, Hydrolisc ruling).
            float f1 = target.getHealth();
            if (f1 > 0.0f) {
                target.setHealth(f1 - 1.0f);
            }
            attacker.heal(1.0f);
        }
        return true;
    }
}
