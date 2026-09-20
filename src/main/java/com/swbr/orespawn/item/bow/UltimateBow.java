package com.swbr.orespawn.item.bow;

import com.swbr.orespawn.entity.arrow.UltimateArrow;
import com.swbr.orespawn.item.enchant.PreEnchant;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.UltimateBow} (UltimateBow.java:13-83), registry id
 * {@code ultimatebow} (OreSpawnMain.java:1367). A bow without ammunition and without draw time:
 * every release fires one {@link UltimateArrow} at speed 3.0 (4.5 blocks per tick), whatever the
 * charge (verhalten/itemblock-02.md). Stack 1, durability 1000, tab Combat (:15-19), use duration
 * 9000 with the bow pose (:62-68), enchantability 50 (:75-77).
 *
 * <ul>
 *   <li>{@code onCreated}: Power V, Flame III, Punch II, Infinity I (:21-26).</li>
 *   <li>{@code onUsingTick} re-adds them while drawing when Infinity is missing (:28-36); the class
 *       has no {@code onUpdate}. PORT: per R7 the fallback also runs on the inventory tick (as
 *       W03's {@code PoisonSword}), so a creative or loot bow does not need a first draw.</li>
 *   <li>Power is added but never read - the arrow's damage comes from {@code UltimateBowDamage}.</li>
 *   <li>{@code onFoodEaten} (:58-60) is dead (no eat action).</li>
 * </ul>
 */
public class UltimateBow extends Item {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.POWER, 5),
            PreEnchant.entry(Enchantments.FLAME, 3),
            PreEnchant.entry(Enchantments.PUNCH, 2),
            PreEnchant.entry(Enchantments.INFINITY, 1));

    /** {@code maxStackSize = 1; setMaxDamage(1000)} (:16-17). */
    public UltimateBow(Item.Properties props) {
        super(props.durability(1000));
    }

    /** {@code onCreated} (:21-26). */
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    /** {@code onUsingTick} (:28-36). */
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        PreEnchant.restore(stack, level, Enchantments.INFINITY, ENCHANTMENTS);
    }

    /** R7 fallback, see class comment. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.INFINITY, ENCHANTMENTS);
    }

    /**
     * {@code onPlayerStoppedUsing} (:38-56). Runs on both sides like the original; only the server
     * spawns the arrow.
     *
     * <p>PORT: {@code Item.itemRand} (a static random) is the level's random; the sound is
     * {@code random.bow} = {@code entity.arrow.shoot}, played for everyone near the shooter.
     */
    @Override
    public void releaseUsing(ItemStack par1ItemStack, Level par2World, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player par3EntityPlayer)) {
            return; // PORT: the 1.7.10 signature only admitted players
        }
        final UltimateArrow var8 = new UltimateArrow(par2World, par3EntityPlayer, 3.0f);
        if (par2World.random.nextInt(4) == 1) {
            var8.setCritArrow(true);
        }
        final int var9 = PreEnchant.level(par1ItemStack, par2World, Enchantments.PUNCH);
        if (var9 > 0) {
            var8.setKnockbackStrength(var9);
        }
        if (PreEnchant.level(par1ItemStack, par2World, Enchantments.FLAME) > 0) {
            var8.igniteForSeconds(100.0f);
        }
        par1ItemStack.hurtAndBreak(1, par3EntityPlayer, LivingEntity.getSlotForHand(par3EntityPlayer.getUsedItemHand()));
        par2World.playSound(null, par3EntityPlayer.getX(), par3EntityPlayer.getY(), par3EntityPlayer.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f / (par2World.getRandom().nextFloat() * 0.4f + 1.2f) + 0.5f);
        var8.pickup = AbstractArrow.Pickup.CREATIVE_ONLY; // canBePickedUp = 2
        if (!par2World.isClientSide) {
            par2World.addFreshEntity(var8);
        }
    }

    /** {@code getMaxItemUseDuration} (:62-64). */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 9000;
    }

    /** {@code getItemUseAction} (:66-68). */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    /** {@code onItemRightClick} (:70-73): always starts drawing. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    /** {@code getItemEnchantability} (:75-77). */
    @Override
    public int getEnchantmentValue() {
        return 50;
    }
}
