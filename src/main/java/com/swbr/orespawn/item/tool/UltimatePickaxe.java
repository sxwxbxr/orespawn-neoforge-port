package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import com.swbr.orespawn.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.UltimatePickaxe} (UltimatePickaxe.java:16-131): an
 * {@code ItemPickaxe} on {@code toolULTIMATE}, stack 1, {@code setMaxDamage(3000)} (:24), tab
 * Tools (:25); registered as {@code ultimatepickaxe} with harvest level 10 (OreSpawnMain.java:1308).
 *
 * <ul>
 *   <li>{@code onCreated}: Efficiency V and Fortune V (:28-31); both re-added by
 *       {@code onUsingTick}/{@code onUpdate} when Efficiency is missing (:33-43) - R7.</li>
 *   <li>{@code onLeftClickEntity} (:62-75): {@link UltimatePvp}.</li>
 *   <li>{@code onBlockDestroyed} (:87-121): bonus drops, see {@link #mineBlock}.</li>
 *   <li>Dead in the jar: {@code canHarvestBlock(Block) -> true} (:45-47, wrong signature),
 *       {@code getDamageVsEntity} (:49-60), {@code getMaterialName} (:123-125).</li>
 * </ul>
 */
public class UltimatePickaxe extends PickaxeItem {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.EFFICIENCY, 5),
            PreEnchant.entry(Enchantments.FORTUNE, 5));

    public UltimatePickaxe(Material material, Item.Properties props) {
        super(material.withUses(3000), props.attributes(OreSpawnTiers.pickaxe(material)));
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

    /** {@code dropItemAnItem} (:77-85): an item entity at the block corner, no spread. */
    private static void dropItemAnItem(Level level, BlockPos pos, Item item, int count) {
        ItemStack is = new ItemStack(item, count);
        ItemEntity var3 = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), is);
        level.addFreshEntity(var3);
    }

    /**
     * {@code onBlockDestroyed} (:87-121), replacing the vanilla body: one durability when the block
     * had hardness (:88-90), then server-side bonus drops in addition to the normal ones.
     *
     * <p>PORT: R22 - {@code Blocks.iron_ore}, {@code gold_ore} and {@code stone} were the whole iron
     * ore, gold ore and rock categories of 1.7.10. The port rolls the ingot bonus on
     * {@code #minecraft:iron_ores} and {@code #minecraft:gold_ores} (deepslate and nether gold ore
     * included) and the gem bonus on {@code #minecraft:base_stone_overworld} (stone, granite,
     * diorite, andesite, tuff, deepslate).
     */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0f) {
            stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
        }
        if (!level.isClientSide) {
            RandomSource rand = level.random;
            if (state.is(BlockTags.IRON_ORES) && rand.nextInt(2) != 0) {
                dropItemAnItem(level, pos, Items.IRON_INGOT, 1 + rand.nextInt(2));
            }
            if (state.is(BlockTags.GOLD_ORES) && rand.nextInt(2) != 0) {
                dropItemAnItem(level, pos, Items.GOLD_INGOT, 1 + rand.nextInt(2));
            }
            if (state.is(BlockTags.BASE_STONE_OVERWORLD) && rand.nextInt(100) == 2) {
                int i = rand.nextInt(10);
                if (i == 0) {
                    dropItemAnItem(level, pos, Items.DIAMOND, 1);
                }
                if (i == 1) {
                    dropItemAnItem(level, pos, Items.EMERALD, 1);
                }
                if (i == 2) {
                    dropItemAnItem(level, pos, ModItems.AMETHYST.get(), 1);
                }
                if (i == 3) {
                    dropItemAnItem(level, pos, ModItems.RUBY.get(), 1);
                }
                if (i == 4) {
                    dropItemAnItem(level, pos, ModItems.URANIUM_NUGGET.get(), 1);
                }
                if (i == 5) {
                    dropItemAnItem(level, pos, ModItems.TITANIUM_NUGGET.get(), 1);
                }
                // 6-9: nothing.
            }
        }
        return true;
    }
}
