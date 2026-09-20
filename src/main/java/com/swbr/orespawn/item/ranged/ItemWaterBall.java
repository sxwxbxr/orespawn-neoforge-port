package com.swbr.orespawn.item.ranged;

import com.swbr.orespawn.entity.projectile.WaterBall;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemWaterBall} ({@code waterball}, "WaterDragon Charge", tabCombat,
 * stack 64): right click throws a {@link WaterBall} (verhalten/itemblock-02.md). See
 * {@link ItemAcid} for the result type.
 */
public class ItemWaterBall extends Item {

    public ItemWaterBall(Item.Properties properties) {
        super(properties.stacksTo(64)); // ItemWaterBall.java:14
    }

    /** ItemWaterBall.java:18-27. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        // PORT: Item.itemRand -> the level's random (see ItemSunspotUrchin).
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            level.addFreshEntity(new WaterBall(level, player));
        }
        return InteractionResultHolder.consume(stack);
    }
}
