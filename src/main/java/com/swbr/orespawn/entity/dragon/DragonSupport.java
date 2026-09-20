package com.swbr.orespawn.entity.dragon;

import com.swbr.orespawn.OreSpawn;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers {@link Dragon} and {@link Spyro} share. No original class: each method stands for a 1.7.10 idiom both
 * originals wrote inline ({@code spawnCreature}, {@code playSoundAtEntity}, {@code func_152115_b}, the vanilla small
 * fireball, {@code Entity.applyEntityCollision}).
 */
final class DragonSupport {

    /** Registry id of W10's {@code Kraken} ("The Kraken"); the class does not exist yet. */
    private static final ResourceLocation KRAKEN = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_kraken");

    private DragonSupport() {
    }

    /**
     * {@code instanceof Kraken} (Dragon.java:348, :533). PORT: the Kraken class is W10's; the
     * registry id is the same test (the Kraken has no subclass in the original).
     */
    static boolean isKraken(@Nullable final Entity e) {
        return e != null && KRAKEN.equals(EntityType.getKey(e.getType()));
    }

    /**
     * {@code func_152115_b(String)}: the owner as a UUID string; the empty string clears it. Both originals only ever
     * pass {@code getUniqueID().toString()}, {@code func_152113_b()} or {@code ""}.
     */
    @Nullable
    static UUID parseOwner(@Nullable final String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * {@code spawnCreature(World, String, x, y, z)} (Dragon.java:1431-1440, Spyro.java:712-721):
     * {@code EntityList.createEntityByName} (no {@code onSpawnWithEgg}), {@code setLocationAndAngles} with a random
     * yaw from the world random, {@code spawnEntityInWorld}, {@code playLivingSound}.
     */
    @Nullable
    static <T extends Mob> T spawnCreature(final Level par0World, final EntityType<T> type, final double par2,
                                           final double par4, final double par6) {
        final T var8 = type.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            var8.playAmbientSound();
        }
        return var8;
    }

    /** {@code worldObj.playSoundAtEntity(entity, name, volume, pitch)}: heard by every player, the entity's own included. */
    static void playSoundAtEntity(final Entity entity, final SoundEvent sound, final float volume, final float pitch) {
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(), volume, pitch);
    }

    /** {@code bid == Blocks.lava || bid == Blocks.flowing_lava}: 1.21.1 has one lava block for source and flow. */
    static boolean isLava(final BlockState bid) {
        return bid.is(Blocks.LAVA);
    }

    /**
     * {@code new EntitySmallFireball(world, shooter, dx, dy, dz)}.
     *
     * <p>PORT: the 1.7.10 {@code EntityFireball} constructor added {@code nextGaussian() * 0.4} to each direction
     * component on the fireball's own random before normalising; 1.21.1's {@link SmallFireball} takes the direction as
     * is, so the spread is drawn here from the shooter's random (Kyuubi, W07; MothSupport, W08). The flight follows
     * 1.21.1 hurting-projectile physics; damage and entity fire are the vanilla small fireball in both versions. A block
     * hit lights air without the 1.21.1 mobGriefing gate, as 1.7.10 {@code EntitySmallFireball.onImpact} did; a fireball
     * reloaded from disk is a plain vanilla one again. R21: whoever moves the shared projectile helpers to
     * {@code entity.projectile} merges this copy with Kyuubi's and MothSupport's.
     */
    static SmallFireball legacySmallFireball(final LivingEntity owner, final double dx, final double dy, final double dz) {
        final double ax = dx + owner.getRandom().nextGaussian() * 0.4;
        final double ay = dy + owner.getRandom().nextGaussian() * 0.4;
        final double az = dz + owner.getRandom().nextGaussian() * 0.4;
        return new SmallFireball(owner.level(), owner, new Vec3(ax, ay, az)) {
            @Override
            protected void onHitBlock(final BlockHitResult result) {
                super.onHitBlock(result);
                if (!this.level().isClientSide) {
                    final BlockPos blockpos = result.getBlockPos().relative(result.getDirection());
                    if (this.level().isEmptyBlock(blockpos)) {
                        this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
                    }
                }
            }
        };
    }

    /**
     * {@code self.applyEntityCollision(other)}, 1.7.10 {@code Entity.applyEntityCollision}: unless one rides the other,
     * both are pushed apart by 0.05 along the axis of the larger offset, {@code self} away from {@code other}.
     *
     * <p>PORT: 1.21.1's {@code Entity.push(Entity)} skips an entity that is a vehicle or not pushable; the ridden
     * Dragon is both, but 1.7.10 pushed it. So this is the 1.7.10 method with the 1.21.1 velocity setter.
     * {@code entityCollisionReduction} was 0 for every entity the dragon can meet; minecart and boat overrides of
     * {@code applyEntityCollision} are not reproduced.
     */
    static void applyEntityCollision(final Entity self, final Entity other) {
        if (other.getFirstPassenger() != self && other.getVehicle() != self) {
            double d0 = other.getX() - self.getX();
            double d1 = other.getZ() - self.getZ();
            double d2 = Math.max(Math.abs(d0), Math.abs(d1));
            if (d2 >= 0.009999999776482582) {
                d2 = (double) (float) Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0 / d2;
                if (d3 > 1.0) {
                    d3 = 1.0;
                }
                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806;
                d1 *= 0.05000000074505806;
                self.push(-d0, 0.0, -d1);
                other.push(d0, 0.0, d1);
            }
        }
    }
}
