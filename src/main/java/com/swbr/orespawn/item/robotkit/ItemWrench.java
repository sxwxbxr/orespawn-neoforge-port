package com.swbr.orespawn.item.robotkit;

import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.entity.antrobot.AntRobot;
import com.swbr.orespawn.entity.spiderrobot.SpiderRobot;
import com.swbr.orespawn.registry.ModItems;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemWrench} (ItemWrench.java:11-92, verhalten/itemblock-02.md): {@code wrench}
 * "Wrench" (BaseItemID + 472), creative tab tools, durability 100 (:14-17). Left-clicking an unridden Spider Robot packs
 * it into a {@code spiderrobotkit} whose damage is the robot's missing health; a Red Ant Robot only if it is owned or at
 * half health or less, and it becomes owned on the way.
 *
 * <p>The hook is NeoForge's {@code IItemExtension#onLeftClickEntity}, which {@code Player.attack} asks on both sides
 * ({@code CommonHooks.onPlayerAttackTarget}), as Forge 1.7.10 did; {@code true} cancels the hit.
 *
 * <p>PORT: {@code Item.setMaxDamage(100)} left the 1.7.10 stack limit at 64 (undamaged wrenches stacked); 1.21.1 refuses
 * an item that is both damageable and stackable ({@code Item.Properties}, "Item cannot have both durability and be
 * stackable"), so the wrench stacks to 1 (ItemCreeperLauncher precedent).
 */
public class ItemWrench extends Item {

    /** {@code ItemWrench(int)} (:13-17). */
    public ItemWrench(final Item.Properties props) {
        // PORT: CreativeTabs.tabTools (:15) is filed by the registry holder (ModCreativeTabs.OriginalTab.TOOLS).
        super(props.durability(100));
    }

    /**
     * {@code onLeftClickEntity} (:19-76), on both sides.
     *
     * <p>PORT: {@code setDead()} ran on both sides (:23, :53); in 1.21.1 removing the entity on the client ahead of the
     * server would leave it invisible for good if the server refused the hit (the tracker would never send it again), so
     * {@code discard()} runs on the server only (R18 case 4). The drop was already server-only (:79-81). Particles stay
     * {@code addParticle} on both sides - only the attacking client shows them, as in 1.7.10, where the server's
     * {@code spawnParticle} did nothing.
     */
    @Override
    public boolean onLeftClickEntity(final ItemStack stack, final Player player, final Entity entity) {
        final Level world = player.level();
        if (entity != null && entity instanceof SpiderRobot && entity.getFirstPassenger() == null) {
            final LivingEntity e = (LivingEntity) entity;
            // maxHealth - health (:22), in original units (R4).
            final float h = (float) VirtualHealth.originalMaxHealth(e) - VirtualHealth.originalHealth(e);
            if (!world.isClientSide) {
                e.discard();
            }
            this.dropItem(world, e, ModItems.SPIDER_ROBOT_KIT.get(), 1, (int) h);
            this.particles(world, entity);
            this.playExplode(world, player);
        } else {
            if (entity == null || !(entity instanceof AntRobot) || entity.getFirstPassenger() != null) {
                return false;
            }
            final AntRobot e2 = (AntRobot) entity;
            if (e2.getOwned() == 0) {
                if (e2.getHealth() / e2.getMaxHealth() > 0.5f) {
                    return false;
                }
                e2.setOwned();
            }
            final float h = (float) VirtualHealth.originalMaxHealth(e2) - VirtualHealth.originalHealth(e2);
            if (!world.isClientSide) {
                e2.discard();
            }
            this.dropItem(world, e2, ModItems.ANT_ROBOT_KIT.get(), 1, (int) h);
            this.particles(world, entity);
            this.playExplode(world, player);
        }
        // damageItem(2, player) (:71); creative players take no damage in both versions. The empty-slot cleanup
        // (:72-74) is automatic in 1.21.1.
        stack.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);
        return true;
    }

    /**
     * The particle loop of :25-38 and :56-69: eight rounds of {@code smoke}, {@code explode} (poof) and
     * {@code reddust} (redstone dust) at {@code x +- 3}, {@code y + 0.25..2.25}, {@code z +- 3}, no motion.
     */
    private void particles(final Level world, final Entity entity) {
        for (int var3 = 0; var3 < 8; ++var3) {
            float f1 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            float f2 = 0.25f + world.random.nextFloat() * 2.0f;
            float f3 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            world.addParticle(ParticleTypes.SMOKE, (double) ((float) entity.getX() + f1), (double) ((float) entity.getY() + f2),
                    (double) ((float) entity.getZ() + f3), 0.0, 0.0, 0.0);
            f1 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            f2 = 0.25f + world.random.nextFloat() * 2.0f;
            f3 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            world.addParticle(ParticleTypes.POOF, (double) ((float) entity.getX() + f1), (double) ((float) entity.getY() + f2),
                    (double) ((float) entity.getZ() + f3), 0.0, 0.0, 0.0);
            f1 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            f2 = 0.25f + world.random.nextFloat() * 2.0f;
            f3 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            world.addParticle(DustParticleOptions.REDSTONE, (double) ((float) entity.getX() + f1), (double) ((float) entity.getY() + f2),
                    (double) ((float) entity.getZ() + f3), 0.0, 0.0, 0.0);
        }
    }

    /**
     * {@code player.worldObj.playSoundAtEntity(player, "random.explode", 0.5f, 1.5f)} (:39, :70), on both sides.
     * PORT: the standard 1.21.1 split - the server excludes the acting player, the client plays it for him - gives every
     * player one playback instead of the attacker's two (OneUse.playExplode precedent, W03).
     */
    private void playExplode(final Level world, final Player player) {
        world.playSound(player, player, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.5f, 1.5f);
    }

    /** {@code dropItem} (:78-86): server only, a kit with the missing health as damage at the robot, one block up. */
    private void dropItem(final Level world, final LivingEntity e, final Item index, final int par1, final int par2) {
        if (world.isClientSide) {
            return;
        }
        final ItemStack is = new ItemStack(index, par1);
        is.setDamageValue(par2);
        final ItemEntity var3 = new ItemEntity(world, e.getX(), e.getY() + 1.0, e.getZ(), is);
        world.addFreshEntity(var3);
    }
}
