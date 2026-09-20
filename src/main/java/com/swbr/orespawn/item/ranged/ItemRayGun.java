package com.swbr.orespawn.item.ranged;

import com.swbr.orespawn.entity.projectile.LaserBall;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemRayGun} ({@code raygun}, "A Freakin' Ray Gun!", tabCombat,
 * stack 1, 50 durability): each shot is a special {@link LaserBall} at triple speed from beside the
 * head, knocks the player back and costs one durability; the last point is never spent, so 49 shots
 * (verhalten/itemblock-01.md).
 *
 * <p>Swing and knockback run on both sides, as in the original: the client applies the push to its
 * own movement, which is what actually moves the player. {@code getMaterialName} is dead and not
 * carried over.
 */
public class ItemRayGun extends Item {

    public ItemRayGun(Item.Properties properties) {
        super(properties.durability(50)); // ItemRayGun.java:14-15: stack 1, setMaxDamage(50)
    }

    /** ItemRayGun.java:19-42. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getMaxDamage() - stack.getDamageValue() <= 1) {
            return InteractionResultHolder.pass(stack);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 3.5F, 0.5F);
        if (!level.isClientSide) {
            final double xzoff = 1.0;
            final double yoff = 1.55;
            final LaserBall lb = new LaserBall(level, player);
            lb.setSpecial();
            lb.moveTo(player.getX() - xzoff * Math.sin(Math.toRadians(player.getYHeadRot() + 45.0F)), player.getY() + yoff,
                    player.getZ() + xzoff * Math.cos(Math.toRadians(player.getYHeadRot() + 45.0F)), player.getYHeadRot(), player.getXRot());
            lb.setDeltaMovement(lb.getDeltaMovement().scale(3.0));
            level.addFreshEntity(lb);
        }
        player.swing(hand);
        player.push(Math.cos(Math.toRadians(player.getYHeadRot() - 90.0F)) * 1.5, 0.3, Math.sin(Math.toRadians(player.getYHeadRot() - 90.0F)) * 1.5);
        // damageItem(1, player): no damage in creative, Unbreaking applies - the same in 1.21.1.
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        return InteractionResultHolder.consume(stack);
    }
}
