package com.swbr.orespawn.item.utility;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemCreeperLauncher} (ItemCreeperLauncher.java:11-49):
 * {@code creeperlauncher} "Creeper Launcher" (OreSpawnMain.java:1388), creative tab
 * {@code tabRedstone}, {@code setMaxDamage(1)} at the vanilla stack size of 64 (:13-16). Hitting a
 * creeper with it launches the creeper 4.5 blocks per tick straight up and uses one launcher.
 *
 * <p>PORT: 1.21.1 refuses an item that is both damageable and stackable
 * ({@code Item.Properties.durability} forces stack size 1, {@code ItemStack.validateComponents}
 * rejects the pair). The original never damaged the item - it decremented the stack (:37-39) -
 * so the durability is dropped and the stack of 64 kept (verhalten/itemblock-01.md, port note).
 */
public class ItemCreeperLauncher extends Item {

    /** {@code ItemCreeperLauncher(id)} (:13-16); the durability of 1 is not carried, see the class comment. */
    public ItemCreeperLauncher(final Item.Properties props) {
        super(props);
    }

    /**
     * {@code onLeftClickEntity} (:18-43), run on both sides as in 1.7.10 ({@code Player.attack}
     * asks {@code CommonHooks.onPlayerAttackTarget} first). Only creepers: six rounds of a
     * {@code smoke}, an {@code explode} and a {@code reddust} particle at {@code x +- 1},
     * {@code y + 0.25..6.25}, rising at a quarter of their height per tick (:20-33 - a no-op on the
     * server then and now); {@code fireworks.launch} 2.0 / 1.2 (:34); {@code addVelocity(0, 4.5, 0)}
     * (:35-36); one launcher less outside creative (:37-39). {@code true} cancels the hit (:40).
     */
    @Override
    public boolean onLeftClickEntity(final ItemStack stack, final Player player, final Entity entity) {
        if (entity != null && entity instanceof Creeper) { // :19
            final Level world = player.level();
            for (int var3 = 0; var3 < 6; ++var3) { // :20
                float f1 = world.random.nextFloat() - world.random.nextFloat();
                float f2 = 0.25f + world.random.nextFloat() * 6.0f;
                float f3 = world.random.nextFloat() - world.random.nextFloat(); // :21-23
                world.addParticle(ParticleTypes.SMOKE, (float) entity.getX() + f1, (float) entity.getY() + f2, (float) entity.getZ() + f3, 0.0, f2 / 4.0f, 0.0); // :24
                f1 = world.random.nextFloat() - world.random.nextFloat();
                f2 = 0.25f + world.random.nextFloat() * 6.0f;
                f3 = world.random.nextFloat() - world.random.nextFloat(); // :25-27
                world.addParticle(ParticleTypes.POOF, (float) entity.getX() + f1, (float) entity.getY() + f2, (float) entity.getZ() + f3, 0.0, f2 / 4.0f, 0.0); // :28 "explode"
                f1 = world.random.nextFloat() - world.random.nextFloat();
                f2 = 0.25f + world.random.nextFloat() * 6.0f;
                f3 = world.random.nextFloat() - world.random.nextFloat(); // :29-31
                world.addParticle(DustParticleOptions.REDSTONE, (float) entity.getX() + f1, (float) entity.getY() + f2, (float) entity.getZ() + f3, 0.0, f2 / 4.0f, 0.0); // :32
            }
            // :34 - see OneUse.playExplode for the one-playback-per-player split.
            world.playSound(player, player, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 2.0f, 1.2f);
            final LivingEntity e = (LivingEntity) entity; // :35 EntityLiving cast - a creeper always is one
            e.push(0.0, 4.5, 0.0); // :36 addVelocity
            // PORT: 1.7.10 tracked living entities with velocity updates, so the server-side push
            // reached the clients on its own; 1.21.1 sends motion only when asked.
            e.hurtMarked = true;
            if (!player.getAbilities().instabuild) { // :37-39
                stack.shrink(1);
            }
            return true; // :40
        }
        return false; // :42
    }
}
