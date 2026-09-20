package com.swbr.orespawn.entity.dino;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.stats.MobStats;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

/**
 * Helpers shared by the W07 carnivorous dinosaurs and the basilisk. None of them has an original class: they are the
 * pieces of 1.7.10 vanilla ({@code EntityMob.isValidLightLevel}, {@code World.getBlock == Blocks.air},
 * {@code Entity.addVelocity}) and the code blocks the six OreSpawn classes repeat verbatim.
 */
public final class DinoSupport {

    private DinoSupport() {}

    /**
     * {@code applyEntityAttributes}: {@code maxHealth = mygetMaxHealth()}, {@code attackDamage = X_stats.attack}, run in
     * the 1.7.10 constructor. The attribute supplier is built before the COMMON config exists (DECISIONS R3), so the
     * constructor writes the config values over the manifest defaults and refills health, like the 1.7.10 constructor
     * started at the configured maximum. A saved entity then restores its own attribute bases and health from NBT,
     * exactly as {@code readEntityFromNBT} did after {@code applyEntityAttributes}.
     *
     * <p>{@code getTotalArmorValue} is read by the R5 formula through {@code LegacyArmor}; the {@code ARMOR} base carries
     * the same number for the vanilla reduction when {@code legacyArmorFormula} is off (Hydrolisc precedent, W06).
     */
    static void applyStats(final Mob mob, final MobStats stats) {
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) stats.health());
        mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        mob.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        mob.setHealth(mob.getMaxHealth());
    }

    /**
     * {@code worldObj.getBlock(x, y, z) != Blocks.air} inverted.
     *
     * <p>PORT: 1.7.10 had one air block; {@code isAir()} also accepts cave and void air, which 1.21.1 carvers write where
     * 1.7.10 caves had plain air.
     */
    static boolean isAir(final LevelAccessor level, final int x, final int y, final int z) {
        return level.getBlockState(new BlockPos(x, y, z)).isAir();
    }

    /**
     * The spawn column tests of {@code getCanSpawnHere}: every block in {@code k} from {@code kFrom} (inclusive) to
     * {@code kTo} (exclusive), same for {@code j} and {@code i}, at {@code (x + j, y + i, z + k)} must be air, loop order
     * k, j, i as in the original. {@code (int) posX} and friends are {@code Mth.floor} (R20).
     */
    static boolean airAround(final LevelAccessor level, final int x, final int y, final int z, final int kFrom,
                             final int kTo, final int jFrom, final int jTo, final int iFrom, final int iTo) {
        for (int k = kFrom; k < kTo; ++k) {
            for (int j = jFrom; j < jTo; ++j) {
                for (int i = iFrom; i < iTo; ++i) {
                    if (!isAir(level, x + j, y + i, z + k)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@code capabilities.isCreativeMode}. In 1.7.10 that flag was set in creative mode only; 1.21.1's
     * {@code abilities.instabuild} is the same flag (W06 precedent).
     */
    static boolean isCreativePlayer(final LivingEntity entity) {
        return entity instanceof Player p && p.getAbilities().instabuild;
    }

    /**
     * An {@code instanceof} against an OreSpawn class that a later or a parallel wave ports, by registry id.
     *
     * <p>PORT: none of these classes has a subclass in the original, so comparing the registered type is the same test,
     * and it compiles before the class exists ({@code cave_fisher} and {@code leaf_monster} arrive with W07's other
     * porters, {@code wtf} - GammaMetroid - with W09).
     */
    static boolean isOreSpawnType(final Entity entity, final String id) {
        final ResourceLocation key = EntityType.getKey(entity.getType());
        return key.getNamespace().equals(OreSpawn.MOD_ID) && key.getPath().equals(id);
    }

    /**
     * The knockback block of the {@code attackEntityAsMob} overrides:
     * {@code f3 = atan2(target.z - z, target.x - x)}, doubled vertical push for a dead target or a player, then
     * {@code addVelocity(cos(f3) * ks, inair, sin(f3) * ks)}.
     *
     * <p>{@code isDead} is {@code isRemoved()}: both become true only once the corpse is gone. {@code addVelocity} is
     * {@code push(x, y, z)}; a player hit by the same attack is already {@code hurtMarked}, so the velocity reaches the
     * client as 1.7.10's {@code velocityChanged} did.
     */
    static void legacyKnockback(final Mob self, final Entity par1Entity, final double ks, final double inairBase) {
        double inair = inairBase;
        final float f3 = (float) Math.atan2(par1Entity.getZ() - self.getZ(), par1Entity.getX() - self.getX());
        if (par1Entity.isRemoved() || par1Entity instanceof Player) {
            inair *= 2.0;
        }
        par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
    }

    /**
     * {@code dropItemRand(item, n)}: a stack placed up to three blocks off on the shared {@code OreSpawnRand}, raised
     * by {@code dy}, added straight to the level - no pickup delay and not part of the captured death drops, because
     * the original bypassed {@code entityDropItem} as well.
     */
    static ItemStack dropItemRand(final Mob self, final ItemStack is, final double dy) {
        final ItemEntity var3 = new ItemEntity(self.level(),
                self.getX() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                self.getY() + dy,
                self.getZ() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                is);
        self.level().addFreshEntity(var3);
        return is;
    }

    /** {@code (int) this.posX} etc. on a block position for the spawn tests (R20). */
    static int floor(final double d) {
        return Mth.floor(d);
    }
}
