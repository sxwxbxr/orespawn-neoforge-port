package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.UltimateAxe} (UltimateAxe.java:13-73): an {@code ItemAxe} on
 * {@code toolULTIMATE}, stack 1, {@code setMaxDamage(3000)} (:21), tab Tools (:22); registered as
 * {@code ultimateaxe} with harvest level 10 (OreSpawnMain.java:1311).
 *
 * <ul>
 *   <li>{@code onCreated}: Efficiency V (:25-27). {@code onUsingTick} adds it again when it is
 *       missing (:29-34), {@code onUpdate} calls that every tick (:36-38) - R7.</li>
 *   <li>{@code onLeftClickEntity} (:40-53): the PvP guard, {@link UltimatePvp}.</li>
 *   <li>Dead: {@code weaponDamage = 15} with {@code getDamageVsEntity} (1 for Girlfriend and
 *       players, :55-63) and {@code getMaterialName} (:65-67).</li>
 * </ul>
 */
public class UltimateAxe extends AxeItem {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.EFFICIENCY, 5));

    public UltimateAxe(Material material, Item.Properties props) {
        super(material.withUses(3000), props.attributes(OreSpawnTiers.axe(material)));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.EFFICIENCY, ENCHANTMENTS);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return UltimatePvp.protects(entity);
    }
}
