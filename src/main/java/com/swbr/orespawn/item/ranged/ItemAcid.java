package com.swbr.orespawn.item.ranged;

import com.swbr.orespawn.entity.projectile.Acid;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemAcid} ({@code acid}, "Acid", tabCombat, stack 64): right click
 * throws an {@link Acid} (verhalten/itemblock-01.md). Dispensed by
 * {@code dispenser.MyDispenserBehaviorAcid}.
 *
 * <p>The result is {@code consume}: a 1.7.10 item use reset the equip animation but never swung the
 * arm, which is what {@code CONSUME} does on the client and {@code SUCCESS} would not.
 */
public class ItemAcid extends Item {

    public ItemAcid(Item.Properties properties) {
        super(properties.stacksTo(64)); // ItemAcid.java:14
    }

    /** ItemAcid.java:18-27. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        // "random.bow" -> entity.arrow.shoot (random/bow); playSoundAtEntity broadcast from the server.
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 3.0F, 1.0F);
        if (!level.isClientSide) {
            level.addFreshEntity(new Acid(level, player));
        }
        return InteractionResultHolder.consume(stack);
    }
}
