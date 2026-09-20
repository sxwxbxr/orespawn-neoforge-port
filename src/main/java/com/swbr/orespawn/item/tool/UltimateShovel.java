package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.UltimateShovel} (UltimateShovel.java:13-70): an {@code ItemSpade}
 * on {@code toolULTIMATE}, stack 1, {@code setMaxDamage(3000)} (:18), tab Tools (:19); registered
 * as {@code ultimateshovel} with harvest level 10 (OreSpawnMain.java:1309).
 *
 * <ul>
 *   <li>{@code onCreated}: Efficiency V (:22-24); re-added by {@code onUsingTick}/{@code onUpdate}
 *       when missing (:26-35) - R7.</li>
 *   <li>{@code onLeftClickEntity} (:37-50): {@link UltimatePvp}.</li>
 *   <li>Dead: {@code getDamageVsEntity} (:52-60), {@code getMaterialName} (:62-64).</li>
 * </ul>
 * Path flattening: see {@link AmethystShovel}.
 */
public class UltimateShovel extends ShovelItem {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.EFFICIENCY, 5));

    public UltimateShovel(Material material, Item.Properties props) {
        super(material.withUses(3000), props.attributes(OreSpawnTiers.shovel(material)));
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
