package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.util.MyUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.ThunderBolt}, the bolt of the Thunder Staff and the royal family.
 * The original was never registered (no {@code registerModEntity}, no renderer); the port needs an
 * {@code EntityType} and uses the id {@code thunder_bolt} (catalogue 6.6, wave plan W04).
 *
 * <p>An entity hit deals 20 as thrown damage and immediately 20 as mob damage from the thrower, then
 * one second of fire; the second hit lands inside the hurt-invulnerability window and only counts
 * where it exceeds the first (catalogue 6.4, 1:1 through {@code LivingEntity.hurt}). Every impact
 * except on royalty explodes with strength 3 and calls down a real lightning bolt one block above
 * (verhalten/entity-13.md).
 *
 * <p>PORT: a thrower-less bolt (the royal coordinate constructor) passed {@code causeMobDamage(null)};
 * 1.21.1's {@code mobAttack(null)} builds the same unattributed source, and its death message no
 * longer dereferences the missing entity.
 */
public class ThunderBolt extends LegacyThrowable {

    /** Registry factory; {@code ThunderBolt(World)}. */
    public ThunderBolt(EntityType<? extends ThunderBolt> type, Level level) {
        super(type, level);
    }

    /** {@code ThunderBolt(World, EntityLivingBase)} - ItemThunderStaff. */
    public ThunderBolt(Level level, LivingEntity thrower) {
        super(ModEntities.THUNDER_BOLT.get(), level, thrower);
    }

    /** {@code ThunderBolt(World, double, double, double)} - TheKing, TheQueen, the princes and the princess. */
    public ThunderBolt(Level level, double x, double y, double z) {
        super(ModEntities.THUNDER_BOLT.get(), level, x, y, z);
    }

    // PORT: the (World, EntityLivingBase, int) overload ignored its int and has no caller in 20.2.

    /**
     * PORT (BUGHUNT2 2.6): the King's and the Queen's heads are no candidates. The royal salvos start inside the
     * head box that R25 moved to the mouth; hitting it would end every salvo on the royalty discard below. Other
     * candidates are unchanged.
     */
    @Override
    protected boolean canHitCandidate(Entity candidate) {
        return !LegacyProjectiles.isRoyalHead(candidate);
    }

    /** ThunderBolt.java:27-49. */
    @Override
    protected void onImpact(HitResult result) {
        Entity entityHit = LegacyProjectiles.entityHit(result);
        if (entityHit != null) {
            final float var2 = 40.0F;
            if (MyUtils.isRoyalty(entityHit)) {
                this.discard();
                return;
            }
            entityHit.hurt(this.damageSources().thrown(this, this.getThrower()), var2 / 2.0F);
            entityHit.hurt(this.damageSources().mobAttack(this.getThrower()), var2 / 2.0F);
            entityHit.igniteForSeconds(1.0F);
        }
        for (int mx = 20, var3 = 0; var3 < mx; ++var3) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                    this.getY() + this.random.nextFloat() - this.random.nextFloat(), this.getZ() + this.random.nextFloat(), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                    this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                    this.getZ() + this.random.nextFloat() - this.random.nextFloat(), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(),
                    this.level().random.nextGaussian(), this.level().random.nextGaussian(), this.level().random.nextGaussian());
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.5F);
        if (!this.level().isClientSide) {
            // createExplosion(this, ..., 3.0f, mobGriefing): unattributed, see LegacyProjectiles.explodeUnattributed.
            LegacyProjectiles.explodeUnattributed(this.level(), this.getX(), this.getY(), this.getZ(), 3.0F);
        }
        // PORT: addWeatherEffect ran on both sides; the client's copy was a second, purely visual bolt next
        // to the server's, which reaches every client anyway. Only the server spawns it.
        if (!this.level().isClientSide) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(this.level());
            if (bolt != null) {
                bolt.moveTo(this.getX(), this.getY() + 1.0, this.getZ(), 0.0F, 0.0F);
                this.level().addFreshEntity(bolt);
            }
        }
        this.discard();
    }

    /** ThunderBolt.java:50-56. */
    @Override
    public void tick() {
        super.tick();
        for (int mx = 4, i = 0; i < mx; ++i) {
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.level().random.nextGaussian() / 10.0,
                    this.level().random.nextGaussian() / 10.0, this.level().random.nextGaussian() / 10.0);
        }
    }
}
