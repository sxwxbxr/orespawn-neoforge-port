package com.swbr.orespawn.item.ranged;

import com.swbr.orespawn.entity.projectile.SunspotUrchin;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemSunspotUrchin} ({@code sunspoturchin}, "Sunspot Urchin",
 * tabCombat, stack 64): right click throws a {@link SunspotUrchin} (verhalten/itemblock-02.md).
 * See {@link ItemAcid} for the result type.
 */
public class ItemSunspotUrchin extends Item {

    public ItemSunspotUrchin(Item.Properties properties) {
        super(properties.stacksTo(64)); // ItemSunspotUrchin.java:14
    }

    /** ItemSunspotUrchin.java:18-27. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        // PORT: Item.itemRand (one static Random) -> the level's random, as in 1.21.1's SnowballItem.
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            level.addFreshEntity(new SunspotUrchin(level, player));
        }
        return InteractionResultHolder.consume(stack);
    }
}
