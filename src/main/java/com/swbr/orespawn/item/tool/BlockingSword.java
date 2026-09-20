package com.swbr.orespawn.item.tool;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * The 1.7.10 {@code ItemSword} right-click block, for the five OreSpawn swords that overrode
 * {@code getMaxItemUseDuration} (live in the jar as {@code func_77626_a}): Ultimate Sword and the
 * three big weapons 9000, Nightmare Sword 5000, Experience Sword, Poison Sword and Mantis Claw 3000.
 * No original class of its own - it is the part of {@code ItemSword} that {@code SwordItem} lost:
 *
 * <ul>
 *   <li>{@code getItemUseAction} = {@code EnumAction.block} -> {@link UseAnim#BLOCK};</li>
 *   <li>{@code onItemRightClick}: {@code setItemInUse(stack, getMaxItemUseDuration(stack))} ->
 *       {@link ItemUtils#startUsingInstantly}; the block ends by itself after the duration, exactly
 *       as the 1.7.10 use counter ran out;</li>
 *   <li>{@code EntityPlayer.damageEntity}: {@code if (!source.isUnblockable() && isBlocking() &&
 *       damage > 0) damage = (1 + damage) * 0.5} before armor - wired in
 *       {@code combat.CombatEvents.applySwordBlock}, which tests {@link #isBlockingWith};</li>
 *   <li>{@code onUsingTick} ran every tick of the block -> {@link #onUseTick}, overridden by the
 *       swords that re-add their enchantments there.</li>
 * </ul>
 *
 * <p>PORT: NeoForge's {@code LivingEntity.isBlocking()} is the shield test ({@code SHIELD_BLOCK}
 * ability plus five ticks) and stays false for these swords, so vanilla neither blocks arrows
 * fully nor damages the sword as a shield - the 1.7.10 block did neither. The 1.7.10
 * {@code isBlocking()} was {@code isUsingItem() && useAction == block}, which is
 * {@link #isBlockingWith}.
 */
public abstract class BlockingSword extends SwordItem {

    private final int useDuration;

    protected BlockingSword(Tier tier, Item.Properties props, int useDuration) {
        super(tier, props);
        this.useDuration = useDuration;
    }

    /** {@code EntityPlayer.isBlocking()} of 1.7.10 for these swords. */
    public static boolean isBlockingWith(LivingEntity entity) {
        return entity.isUsingItem() && entity.getUseItem().getItem() instanceof BlockingSword;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    /** The class's {@code getMaxItemUseDuration}. */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return useDuration;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
