package com.swbr.orespawn.item.bow;

import com.swbr.orespawn.entity.arrow.UltimateFishHook;
import com.swbr.orespawn.item.enchant.PreEnchant;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Port of {@code danger.orespawn.UltimateFishingRod} (UltimateFishingRod.java:12-59), registry id
 * {@code ultimatefishingrod} (OreSpawnMain.java:1369). Casts an {@link UltimateFishHook}, which
 * fishes in water and lava (verhalten/itemblock-02.md). Durability 3000, stack 1, tab Tools
 * (:14-18); {@code isFull3D} and {@code shouldRotateAroundWhenRendering} (:20-22, :35-37) are the
 * {@code item/handheld_rod} model. A plain {@code Item} like the original, not a
 * {@code FishingRodItem} (that one has enchantability 1; this class kept 0).
 *
 * <ul>
 *   <li>{@code onCreated}: Unbreaking II (:24-26).</li>
 *   <li>{@code onUsingTick} re-adds it (:28-33) - dead, a rod has no use duration. PORT: per R7 the
 *       fallback runs on the inventory tick instead, as for the other pre-enchanted items.</li>
 *   <li>{@link #canPerformAction}: {@code ItemAbilities.FISHING_ROD_CAST}, so that
 *       {@code FishingHookRenderer} draws the line from this hand (catalogue 6.3).</li>
 * </ul>
 */
public class UltimateFishingRod extends Item {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(PreEnchant.entry(Enchantments.UNBREAKING, 2));

    /** {@code setMaxDamage(3000); setMaxStackSize(1)} (:15-16). */
    public UltimateFishingRod(Item.Properties props) {
        super(props.durability(3000));
    }

    /** {@code onCreated} (:24-26). */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    /** {@code onUsingTick} (:28-33); never called in 1.7.10 or here. */
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        PreEnchant.restore(stack, level, Enchantments.UNBREAKING, ENCHANTMENTS);
    }

    /** R7 fallback, see class comment. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.UNBREAKING, ENCHANTMENTS);
    }

    /**
     * {@code onItemRightClick} (:39-53), both sides: reel in (the hook reports the durability cost,
     * 0 on the client) or cast (sound {@code random.bow} 0.5, the server spawns the hook), and swing
     * either way.
     *
     * <p>PORT: returns {@code CONSUME} because the swing is explicit here, as in the original;
     * {@code SUCCESS} would make the client swing a second time.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level par2World, Player par3EntityPlayer, InteractionHand hand) {
        ItemStack par1ItemStack = par3EntityPlayer.getItemInHand(hand);
        if (par3EntityPlayer.fishing != null) {
            final int var4 = par3EntityPlayer.fishing.retrieve(par1ItemStack);
            par1ItemStack.hurtAndBreak(var4, par3EntityPlayer, LivingEntity.getSlotForHand(hand));
            par3EntityPlayer.swing(hand);
        } else {
            par2World.playSound(null, par3EntityPlayer.getX(), par3EntityPlayer.getY(), par3EntityPlayer.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5f, 0.4f / (par2World.getRandom().nextFloat() * 0.4f + 0.8f));
            if (!par2World.isClientSide) {
                par2World.addFreshEntity(new UltimateFishHook(par2World, par3EntityPlayer));
            }
            par3EntityPlayer.swing(hand);
        }
        return InteractionResultHolder.consume(par1ItemStack);
    }

    /** The line renderer and {@code FishingHook} look for this ability instead of {@code Items.FISHING_ROD}. */
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_FISHING_ROD_ACTIONS.contains(itemAbility);
    }
}
