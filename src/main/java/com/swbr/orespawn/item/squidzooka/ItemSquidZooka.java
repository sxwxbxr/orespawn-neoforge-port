package com.swbr.orespawn.item.squidzooka;

import com.swbr.orespawn.entity.sea.AttackSquid;
import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.ItemSquidZooka} (ItemSquidZooka.java:12-68), registry id {@code squidzookasmall}
 * ("SquidZooka!", OreSpawnMain.java:1421, legacy id 9317). A bazooka that fires a live Attack Squid
 * (verhalten/itemblock-02.md). Stack 1, durability 100, tab Combat (:15-17). The in-hand model is the
 * {@code RenderSquidZooka} BEWLR in {@code client.item.squidzooka}; GUI and ground keep the flat
 * {@code squidzookasmall} sprite, the original's {@code handleRenderType == false} fallback.
 *
 * <p>Not carried over: {@code getMaterialName} (:61-63, dead); {@code registerIcons} (:65-67) is the item model. The
 * refill with ink sacs is a recipe, not this class.
 */
public class ItemSquidZooka extends Item {

    /** {@code maxStackSize = 1; setMaxDamage(100)} (:15-16). */
    public ItemSquidZooka(final Item.Properties props) {
        super(props.durability(100));
    }

    /**
     * {@code onItemRightClick} (:20-48), both sides. With at most one use left nothing happens. Otherwise
     * {@code random.explode} 0.5/0.5; on the server an Attack Squid appears 2.5 blocks beside the head yaw + 15 degrees
     * at eye level + 1.65, marked as shot, and flies along the look vector at 3.6 with ±0.05 noise (world random);
     * then the swing, a recoil of 0.45 back along {@code yawHead - 90} and 0.1 up, and one point of wear.
     *
     * <p>PORT (R18 case 1): the original dereferenced the spawned entity without a null check (:34); the motion is only
     * set when the squid exists. {@code playSoundAtEntity} is a server broadcast to every player in range, the user
     * included; the original also played it on the client, so the user heard it twice. The swing is
     * {@code Player.swing(hand)} and the holder is {@code consume}, so the client does not swing a second time.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level par2World, final Player par3EntityPlayer, final InteractionHand hand) {
        final ItemStack par1ItemStack = par3EntityPlayer.getItemInHand(hand);
        if (par1ItemStack.getMaxDamage() - par1ItemStack.getDamageValue() <= 1) {
            return InteractionResultHolder.pass(par1ItemStack);
        }
        par2World.playSound(null, par3EntityPlayer.getX(), par3EntityPlayer.getY(), par3EntityPlayer.getZ(),
                SoundEvents.GENERIC_EXPLODE, par3EntityPlayer.getSoundSource(), 0.5f, 0.5f);
        if (!par2World.isClientSide) {
            final double xzoff = 2.5;
            final double yoff = 1.65;
            final Entity e = AttackSquid.spawnCreature(par2World, ModEntities.ATTACK_SQUID.get(),
                    par3EntityPlayer.getX() - xzoff * Math.sin(Math.toRadians(par3EntityPlayer.getYHeadRot() + 15.0f)),
                    par3EntityPlayer.getY() + yoff,
                    par3EntityPlayer.getZ() + xzoff * Math.cos(Math.toRadians(par3EntityPlayer.getYHeadRot() + 15.0f)));
            if (e instanceof AttackSquid a) {
                a.setWasShot();
            }
            if (e != null) {
                final float f = 3.6f;
                double motionX = -Mth.sin(par3EntityPlayer.getYRot() / 180.0f * 3.1415927f) * Mth.cos(par3EntityPlayer.getXRot() / 180.0f * 3.1415927f) * f;
                double motionZ = Mth.cos(par3EntityPlayer.getYRot() / 180.0f * 3.1415927f) * Mth.cos(par3EntityPlayer.getXRot() / 180.0f * 3.1415927f) * f;
                double motionY = -Mth.sin(par3EntityPlayer.getXRot() / 180.0f * 3.1415927f) * f;
                motionX += (par2World.random.nextFloat() - par2World.random.nextFloat()) * 0.05;
                motionY += (par2World.random.nextFloat() - par2World.random.nextFloat()) * 0.05;
                motionZ += (par2World.random.nextFloat() - par2World.random.nextFloat()) * 0.05;
                e.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
            }
        }
        par3EntityPlayer.swing(hand);
        par3EntityPlayer.push(Math.cos(Math.toRadians(par3EntityPlayer.getYHeadRot() - 90.0f)) * 0.45, 0.1,
                Math.sin(Math.toRadians(par3EntityPlayer.getYHeadRot() - 90.0f)) * 0.45);
        par1ItemStack.hurtAndBreak(1, par3EntityPlayer, LivingEntity.getSlotForHand(hand));
        return InteractionResultHolder.consume(par1ItemStack);
    }
}
