package com.swbr.orespawn.item.ranged;

import com.swbr.orespawn.entity.projectile.IceBall;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemIceBall} ({@code iceball}, "Ice Ball", tabCombat, stack 64):
 * right click throws an {@link IceBall} without the ice-maker flag (verhalten/itemblock-01.md).
 * See {@link ItemAcid} for the result type.
 */
public class ItemIceBall extends Item {

    public ItemIceBall(Item.Properties properties) {
        super(properties.stacksTo(64)); // ItemIceBall.java:14
    }

    /** ItemIceBall.java:18-27. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 3.0F, 1.0F);
        if (!level.isClientSide) {
            level.addFreshEntity(new IceBall(level, player));
        }
        return InteractionResultHolder.consume(stack);
    }
}
