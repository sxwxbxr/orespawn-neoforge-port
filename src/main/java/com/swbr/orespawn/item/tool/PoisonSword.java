package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.PoisonSword} (PoisonSword.java:13-64): an {@code ItemSword} on
 * {@code toolEMERALD}, stack 1, {@code setMaxDamage(1300)} (:23), tab Combat (:24); registered as
 * {@code poisonsword} (OreSpawnMain.java:1326). Attack {@code 4 + 6} in 1.7.10 (R6).
 *
 * <ul>
 *   <li>{@code onCreated}: Sharpness I (:27-29).</li>
 *   <li>{@code onUsingTick} re-adds Sharpness I when missing (:31-36) - but this class has
 *       <em>no</em> {@code onUpdate}, so in 1.7.10 that only ran while the player was blocking with
 *       the sword: {@link #onUseTick}. PORT: per R7 the fallback <em>also</em> runs on the
 *       inventory tick like the other pre-enchanted items, so a creative or loot copy does not
 *       need a block first.</li>
 *   <li>{@code hitEntity} (:42-54, live - {@code EntityLivingBase} signature): Poison, Wither and
 *       Weakness on the target, each {@code (10 + rand(10)) * 20} ticks with a fresh roll,
 *       amplifier 0; then {@code damageItem(1)} (vanilla {@code postHurtEnemy}).</li>
 *   <li>Right-click blocks for 3000 ticks ({@code getMaxItemUseDuration} :56-58, live -
 *       {@link BlockingSword}).</li>
 *   <li>Dead: {@code weaponDamage = 15} (:21), {@code getMaterialName} (:38-40).</li>
 * </ul>
 */
public class PoisonSword extends BlockingSword {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.SHARPNESS, 1));

    public PoisonSword(Material material, Item.Properties props) {
        super(material.withUses(1300), props.attributes(OreSpawnTiers.sword(material)), 3000);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    /** {@code onUsingTick} (:31-36): every tick of a block. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        PreEnchant.restore(stack, level, Enchantments.SHARPNESS, ENCHANTMENTS);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.SHARPNESS, ENCHANTMENTS);
    }

    /**
     * {@code hitEntity} (:42-54). Server only, as in 1.7.10: {@code attackEntityFrom} returned
     * false on the client, so {@code hitEntity} never ran there; 1.21.1 calls {@code hurtEnemy}
     * only from a {@code ServerLevel} ({@code Player.attack}).
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        int var2 = 5;
        if (target != null) {
            RandomSource rand = target.level().random;
            var2 = 10 + rand.nextInt(10);
            target.addEffect(new MobEffectInstance(MobEffects.POISON, var2 * 20, 0));
            var2 = 10 + rand.nextInt(10);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, var2 * 20, 0));
            var2 = 10 + rand.nextInt(10);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, var2 * 20, 0));
        }
        return true;
    }
}
