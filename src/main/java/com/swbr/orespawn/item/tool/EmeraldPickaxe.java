package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.EmeraldPickaxe} (EmeraldPickaxe.java:12-51): an
 * {@code ItemPickaxe} on {@code toolEMERALD}, stack 1, {@code setMaxDamage(1300)} (:20), tab Tools
 * (:21); registered as {@code emeraldpickaxe} (OreSpawnMain.java:1321).
 *
 * <p>The one behaviour: {@code onUsingTick} (:24-29) adds Silk Touch I whenever the stack has no
 * Silk Touch, and {@code onUpdate} (:31-33) calls it every inventory tick. There is <em>no</em>
 * {@code onCreated}: a freshly crafted pickaxe gets the enchantment on its first tick, not in the
 * crafting slot. Kept 1:1 - no {@code onCraftedBy} here; the tick fallback of R7 is exactly the
 * original path.
 *
 * <p>{@code weaponDamage = 10}, both {@code getDamageVsEntity} (:35-41) and {@code getMaterialName}
 * (:43-45) are dead.
 */
public class EmeraldPickaxe extends PickaxeItem {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.SILK_TOUCH, 1));

    public EmeraldPickaxe(Material material, Item.Properties props) {
        super(material.withUses(1300), props.attributes(OreSpawnTiers.pickaxe(material)));
    }

    /** {@code onUpdate} -> {@code onUsingTick}: Silk Touch I when {@code getEnchantmentLevel(silkTouch) <= 0}. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.SILK_TOUCH, ENCHANTMENTS);
    }
}
