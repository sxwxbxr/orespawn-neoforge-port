package com.swbr.orespawn.item.bow;

import com.swbr.orespawn.entity.arrow.IrukandjiArrow;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.SkateBow} (SkateBow.java:12-85), registry id {@code skatebow}
 * ("Skate String Bow", OreSpawnMain.java:1368). The bow of the {@link IrukandjiArrow}: a vanilla
 * draw curve capped at 1.75, one {@code irukandjiarrow} per shot unless creative or Infinity
 * (verhalten/itemblock-02.md). Stack 1, durability 300, tab Combat (:14-18), use duration 9000 with
 * the bow pose (:64-70), enchantability 50 (:77-79). {@code onCreated}, {@code onUsingTick}
 * (:20-24) are empty, {@code onFoodEaten} (:60-62) is dead. Power is never read. Forge's
 * {@code ArrowNockEvent}/{@code ArrowLooseEvent} were not fired, and their successors are not
 * either.
 */
public class SkateBow extends Item {

    /** {@code maxStackSize = 1; setMaxDamage(300)} (:15-16). */
    public SkateBow(Item.Properties props) {
        super(props.durability(300));
    }

    /**
     * {@code onPlayerStoppedUsing} (:26-58), both sides; only the server spawns. PORT:
     * {@code Item.itemRand} is the level's random.
     */
    @Override
    public void releaseUsing(ItemStack par1ItemStack, Level par2World, LivingEntity livingEntity, int par4) {
        if (!(livingEntity instanceof Player par3EntityPlayer)) {
            return; // PORT: the 1.7.10 signature only admitted players
        }
        final int var6 = this.getUseDuration(par1ItemStack, livingEntity) - par4;
        final boolean flag = par3EntityPlayer.getAbilities().instabuild
                || PreEnchant.level(par1ItemStack, par2World, Enchantments.INFINITY) > 0;
        if (flag || hasItem(par3EntityPlayer, ModItems.IRUKANDJI_ARROW.get())) {
            float f = var6 / 20.0f;
            f = (f * f + f * 2.0f) / 3.0f;
            if (f < 0.1) {
                return;
            }
            if (f > 1.75f) {
                f = 1.75f;
            }
            final IrukandjiArrow var7 = new IrukandjiArrow(par2World, par3EntityPlayer, f);
            if (par2World.random.nextInt(20) == 1) {
                var7.setCritArrow(true);
            }
            final int var8 = PreEnchant.level(par1ItemStack, par2World, Enchantments.PUNCH);
            if (var8 > 0) {
                var7.setKnockbackStrength(var8);
            }
            if (PreEnchant.level(par1ItemStack, par2World, Enchantments.FLAME) > 0) {
                var7.igniteForSeconds(100.0f);
            }
            par1ItemStack.hurtAndBreak(1, par3EntityPlayer, LivingEntity.getSlotForHand(par3EntityPlayer.getUsedItemHand()));
            par2World.playSound(null, par3EntityPlayer.getX(), par3EntityPlayer.getY(), par3EntityPlayer.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f / (par2World.getRandom().nextFloat() * 0.4f + 1.2f) + 0.5f);
            if (!flag) {
                consumeInventoryItem(par3EntityPlayer, ModItems.IRUKANDJI_ARROW.get());
            }
            if (!par2World.isClientSide) {
                par2World.addFreshEntity(var7);
            }
        }
    }

    /**
     * {@code InventoryPlayer.hasItem(item)}: the 36 main slots only, item type only. PORT: the
     * off-hand did not exist and is not searched.
     */
    static boolean hasItem(Player player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    /** {@code InventoryPlayer.consumeInventoryItem(item)}: one from the first main slot holding it. */
    static boolean consumeInventoryItem(Player player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    /** {@code getMaxItemUseDuration} (:64-66). */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 9000;
    }

    /** {@code getItemUseAction} (:68-70). */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    /** {@code onItemRightClick} (:72-75): draws with or without ammunition. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    /** {@code getItemEnchantability} (:77-79). */
    @Override
    public int getEnchantmentValue() {
        return 50;
    }
}
