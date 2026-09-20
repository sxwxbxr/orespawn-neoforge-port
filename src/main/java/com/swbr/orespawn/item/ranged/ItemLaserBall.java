package com.swbr.orespawn.item.ranged;

import com.swbr.orespawn.entity.projectile.LaserBall;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemLaserBall} ({@code laserball}, "Robot Laser Charge", tabCombat,
 * stack 64): right click throws a plain {@link LaserBall}, without {@code setSpecial}
 * (verhalten/itemblock-01.md). See {@link ItemAcid} for the result type.
 */
public class ItemLaserBall extends Item {

    public ItemLaserBall(Item.Properties properties) {
        super(properties.stacksTo(64)); // ItemLaserBall.java:14
    }

    /** ItemLaserBall.java:18-27. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        // "fireworks.launch" -> entity.firework_rocket.launch.
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 3.0F, 1.0F);
        if (!level.isClientSide) {
            level.addFreshEntity(new LaserBall(level, player));
        }
        return InteractionResultHolder.consume(stack);
    }
}
