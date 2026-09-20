package com.swbr.orespawn.item.ranged;

import com.swbr.orespawn.entity.projectile.ThunderBolt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemThunderStaff} ({@code thunderstaff}, "Thunder Staff", tabCombat,
 * stack 1, 50 charges): each use fires a {@link ThunderBolt} at triple speed from beside the head,
 * pushes the player back and costs one charge; the last charge is never spent. During a
 * thunderstorm a staff recharges one point every 50 ticks (verhalten/itemblock-02.md).
 *
 * <p>No sound of its own: the original played none on use. {@code getMaterialName} is dead.
 */
public class ItemThunderStaff extends Item {

    /** ItemThunderStaff.java:16: the recharge period in ticks. */
    private static final int RECHARGE_TICKS = 50;

    public ItemThunderStaff(Item.Properties properties) {
        super(properties.durability(50)); // ItemThunderStaff.java:17-18: stack 1, setMaxDamage(50)
    }

    /** ItemThunderStaff.java:22-41. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getMaxDamage() - stack.getDamageValue() <= 1) {
            return InteractionResultHolder.pass(stack);
        }
        final double xzoff = 1.0;
        final double yoff = 1.55;
        // PORT: spawned on the server only. The original called spawnEntityInWorld without an isRemote
        // check, so the throwing client ran a local ghost bolt with its own particles and client-side
        // lightning (verhalten/itemblock-02.md); the server's bolt reaches every client in the port.
        if (!level.isClientSide) {
            final ThunderBolt lb = new ThunderBolt(level, player);
            lb.moveTo(player.getX() - xzoff * Math.sin(Math.toRadians(player.getYHeadRot() + 45.0F)), player.getY() + yoff,
                    player.getZ() + xzoff * Math.cos(Math.toRadians(player.getYHeadRot() + 45.0F)), player.getYHeadRot(), player.getXRot());
            lb.setDeltaMovement(lb.getDeltaMovement().scale(3.0));
            level.addFreshEntity(lb);
        }
        player.swing(hand);
        player.push(Math.cos(Math.toRadians(player.getYHeadRot() - 90.0F)) * 0.5, 0.15, Math.sin(Math.toRadians(player.getYHeadRot() - 90.0F)) * 0.5);
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        return InteractionResultHolder.consume(stack);
    }

    /**
     * ItemThunderStaff.java:43-53: while it rains and thunders, one point of damage is repaired
     * every 50 ticks.
     *
     * <p>PORT (R18 case 2): the original counted down a single {@code ticker} field on the item
     * singleton, so every staff in every player's inventory - and in single player the client copy as
     * well - shortened everyone else's wait. The port derives the period from the level's game time
     * on the server: one staff recharges at the original rate of 1 per 50 thundering ticks, several
     * staffs no longer speed each other up. The phase differs - the original's countdown paused
     * outside storms and fired at once on the first damaged tick after reaching zero.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide) {
            return;
        }
        if (level.isRaining() && level.isThundering()) {
            if (level.getGameTime() % RECHARGE_TICKS == 0L && stack.getDamageValue() > 0) {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        }
    }
}
