package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.entity.sea.AttackSquid;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.WaterBall} ("WaterDragon Charge", entity {@code water_ball}):
 * 2 damage, 5 against creepers, puts the target out and makes it drop a charge one time in ten.
 * WaterDragon, AttackSquid, non-zero Dragon types and riding players are skipped with an early
 * return, so the ball flies on through them (verhalten/entity-14.md).
 *
 * <p>The early return stays in {@link #onImpact} instead of 1.21.1's {@code canHitEntity}: an immune
 * target that is the nearest intercept still shadows a block or another entity behind it for that
 * tick, as in 1.7.10.
 */
public class WaterBall extends LegacyThrowable {

    private float my_rotation = 0.0F;
    private final int my_index = 49;

    /** Registry factory; {@code WaterBall(World)}. */
    public WaterBall(EntityType<? extends WaterBall> type, Level level) {
        super(type, level);
    }

    /** {@code WaterBall(World, EntityLivingBase)} - ItemWaterBall. */
    public WaterBall(Level level, LivingEntity thrower) {
        super(ModEntities.WATER_BALL.get(), level, thrower);
    }

    /** {@code WaterBall(World, double, double, double)} - dispenser, WaterDragon, AttackSquid, Dragon. */
    public WaterBall(Level level, double x, double y, double z) {
        super(ModEntities.WATER_BALL.get(), level, x, y, z);
    }

    /** Spinner tile 49 (RenderItemUrchin.java:16-19). */
    public int getWaterBallIndex() {
        return this.my_index;
    }

    /** WaterBall.java:37-75. */
    @Override
    protected void onImpact(HitResult result) {
        Entity entityHit = LegacyProjectiles.entityHit(result);
        if (entityHit != null) {
            float var2 = 2.0F;
            if (entityHit instanceof Creeper) {
                var2 = 5.0F;
            }
            if (entityHit instanceof WaterDragon) { // WaterBall.java:43-45
                return;
            }
            if (entityHit instanceof AttackSquid) { // WaterBall.java:46-48
                return;
            }
            if (entityHit instanceof Dragon d) { // WaterBall.java:49-54
                if (d.getDragonType() != 0) {
                    return;
                }
            }
            if (entityHit instanceof Player d2) {
                if (d2.getVehicle() != null) {
                    return;
                }
            }
            entityHit.hurt(this.damageSources().thrown(this, this.getThrower()), var2);
            if (this.level().random.nextInt(10) == 1) {
                entityHit.spawnAtLocation(ModItems.WATER_BALL.get());
            }
            entityHit.clearFire();
        }
        for (int var3 = 0; var3 < 8; ++var3) {
            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                    this.getY() + this.random.nextFloat() - this.random.nextFloat(), this.getZ() + this.random.nextFloat(), 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.SPLASH, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                    this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                    this.getZ() + this.random.nextFloat() - this.random.nextFloat(), 0.0, 0.0, 0.0);
        }
        // "random.splash" -> entity.generic.splash, which plays the same random/splash file.
        this.playSound(SoundEvents.GENERIC_SPLASH, 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.5F);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    /** WaterBall.java:77-87. */
    @Override
    public void tick() {
        super.tick();
        this.my_rotation += 30.0F;
        while (this.my_rotation > 360.0F) {
            this.my_rotation -= 360.0F;
        }
        final float my_rotation = this.my_rotation;
        this.xRotO = my_rotation;
        this.setXRot(my_rotation);
        this.level().addParticle(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
    }
}
