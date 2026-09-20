package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.NightmareSword} (NightmareSword.java:12-57): an {@code ItemSword}
 * on {@code toolNIGHTMARE}, stack 1, {@code setMaxDamage(1200)} (:17; the material says 1800), tab
 * Combat (:18); registered as {@code nightmaresword} (OreSpawnMain.java:1312). Attack
 * {@code 4 + 26} in 1.7.10 (R6).
 *
 * <p>{@code onCreated}: Sharpness I, Knockback III, Fire Aspect I (:21-25). {@code onUsingTick}
 * adds all three again when Knockback is missing (:27-34) - it ran while blocking, see
 * {@link #onUseTick} - and {@code onUpdate} calls it every tick (:36-38) - R7. Right-click
 * blocks for 5000 ticks ({@code getMaxItemUseDuration} :49-51, live - {@link BlockingSword}).
 * Dead: {@code getMaterialName} (:40-42), the {@code EntityLiving}-typed {@code hitEntity}
 * (:44-47).
 */
public class NightmareSword extends BlockingSword {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.SHARPNESS, 1),
            PreEnchant.entry(Enchantments.KNOCKBACK, 3),
            PreEnchant.entry(Enchantments.FIRE_ASPECT, 1));

    public NightmareSword(Material material, Item.Properties props) {
        super(material.withUses(1200), props.attributes(OreSpawnTiers.sword(material)), 5000);
    }

    /** {@code onUsingTick} (:27-34) as called every tick of a block. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        PreEnchant.restore(stack, level, Enchantments.KNOCKBACK, ENCHANTMENTS);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.KNOCKBACK, ENCHANTMENTS);
    }
}
