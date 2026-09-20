package com.swbr.orespawn.item.utility;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemZooKeeper} (ItemZooKeeper.java:10-49): {@code zookeeper}
 * "ZooKeeper Shard" (OreSpawnMain.java:1589), creative tab {@code tabDecorations},
 * {@code setMaxDamage(1)} at the vanilla stack size of 64 (:12-15). Hitting a mob with it makes
 * the mob persistent - it never despawns again - and the shard is spent.
 *
 * <p>PORT: 1.21.1 refuses an item that is both damageable and stackable (see
 * {@link ItemCreeperLauncher}). The original spent the shard through {@code damageItem(2)} on a
 * durability of 1 (:36-39): it broke on the first hit, and {@code damageItem} skipped creative
 * players. The port shrinks the stack by one outside creative mode instead - the same outcome
 * for the player, and the stack of 64 stays.
 */
public class ItemZooKeeper extends Item {

    /** {@code ItemZooKeeper(id)} (:12-15); the durability of 1 is not carried, see the class comment. */
    public ItemZooKeeper(final Item.Properties props) {
        super(props);
    }

    /**
     * {@code onLeftClickEntity} (:17-43), run on both sides as in 1.7.10. Eight rounds of a
     * {@code smoke}, an {@code explode} and a {@code reddust} particle at {@code x +- 3},
     * {@code y + 0.25..2.25}, {@code z +- 3}, no motion (:18-31); {@code random.explode} 0.5 / 1.5
     * (:32). For an {@code EntityLiving} - a {@link Mob} - {@code func_110163_bv} =
     * {@code enablePersistence} (:35, methods.csv), the shard is spent, {@code true} cancels the
     * hit (:40). Anything else answers {@code false} after the particles and the sound (:42).
     *
     * <p>PORT: the original read {@code entity.posX} before its null test (:22 against :33);
     * 1.21.1 never passes null here, so nothing changes.
     */
    @Override
    public boolean onLeftClickEntity(final ItemStack stack, final Player player, final Entity entity) {
        final Level world = player.level();
        for (int var3 = 0; var3 < 8; ++var3) { // :18
            float f1 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            float f2 = 0.25f + world.random.nextFloat() * 2.0f;
            float f3 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f; // :19-21
            world.addParticle(ParticleTypes.SMOKE, (float) entity.getX() + f1, (float) entity.getY() + f2, (float) entity.getZ() + f3, 0.0, 0.0, 0.0); // :22
            f1 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            f2 = 0.25f + world.random.nextFloat() * 2.0f;
            f3 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f; // :23-25
            world.addParticle(ParticleTypes.POOF, (float) entity.getX() + f1, (float) entity.getY() + f2, (float) entity.getZ() + f3, 0.0, 0.0, 0.0); // :26 "explode"
            f1 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f;
            f2 = 0.25f + world.random.nextFloat() * 2.0f;
            f3 = world.random.nextFloat() * 3.0f - world.random.nextFloat() * 3.0f; // :27-29
            world.addParticle(DustParticleOptions.REDSTONE, (float) entity.getX() + f1, (float) entity.getY() + f2, (float) entity.getZ() + f3, 0.0, 0.0, 0.0); // :30
        }
        OneUse.playExplode(world, player, 0.5f, 1.5f); // :32
        if (entity != null && entity instanceof Mob e) { // :33-34 EntityLiving
            e.setPersistenceRequired(); // :35 func_110163_bv
            // :36-39 damageItem(2) on maxDamage 1 -> broken, slot cleared; see the class comment.
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return true; // :40
        }
        return false; // :42
    }
}
