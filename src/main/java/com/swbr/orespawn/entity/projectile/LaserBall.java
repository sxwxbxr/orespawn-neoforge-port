package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.entity.arthropod.SpitBug;
import com.swbr.orespawn.entity.arthropod.TrooperBug;
import com.swbr.orespawn.entity.robot.GiantRobot;
import com.swbr.orespawn.entity.robot.Robot2;
import com.swbr.orespawn.entity.robot.Robot3;
import com.swbr.orespawn.entity.robot.Robot4;
import com.swbr.orespawn.entity.robot.Robot5;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Port of {@code danger.orespawn.LaserBall} ("Robot Laser Charge", entity {@code laser_ball}):
 * the energy ball of the robots, the Ray Gun and the dispenser, and the base of {@link Acid},
 * {@link IceBall} and {@link DeadIrukandji}, which only set a mode flag (LaserBall.java:78-93).
 *
 * <p>Behaviour in verhalten/entity-09.md. Numbers and the order of the immunity checks are the
 * original's. The flags live in plain fields and are neither saved nor synchronised, exactly like
 * the original: a reloaded Ray Gun shot is an ordinary laser ball, and the client never knows
 * {@code is_special}, so it draws the normal trail and the normal ten impact puffs for it - that is
 * what a 1.7.10 client showed too. The subclass flags come back through the subclass constructors
 * on both sides.
 *
 * <p>Entity hits run on the server only and block hits on both sides; that is where the original's
 * particles became visible, because {@code World.spawnParticle} did nothing on the server
 * ({@code Level.addParticle} is a no-op on {@code ServerLevel} as well).
 */
public class LaserBall extends LegacyThrowable {

    private float my_rotation = 0.0F;
    private final int my_index = 81;
    private int is_special = 0;
    private int is_iceball = 0;
    private int is_acid = 0;
    private int is_irukandji = 0;
    private int ticksalive = 0;

    /** Registry factory; {@code LaserBall(World)}. */
    public LaserBall(EntityType<? extends LaserBall> type, Level level) {
        super(type, level);
    }

    protected LaserBall(EntityType<? extends LaserBall> type, Level level, LivingEntity thrower) {
        super(type, level, thrower);
    }

    protected LaserBall(EntityType<? extends LaserBall> type, Level level, double x, double y, double z) {
        super(type, level, x, y, z);
    }

    /** {@code LaserBall(World, EntityLivingBase)} - ItemLaserBall, ItemRayGun. */
    public LaserBall(Level level, LivingEntity thrower) {
        this(ModEntities.LASER_BALL.get(), level, thrower);
    }

    /** {@code LaserBall(World, double, double, double)} - dispenser, Robot3/4/5, GiantRobot. */
    public LaserBall(Level level, double x, double y, double z) {
        this(ModEntities.LASER_BALL.get(), level, x, y, z);
    }

    // PORT: the (World, int) and (World, EntityLivingBase, int) overloads ignored their int and have
    // no caller in 20.2 (grep "new LaserBall("); they are not carried over.

    /** Spinner tile 81 of {@code spinners.png} (RenderItemUrchin.java:24-27). */
    public int getLaserBallIndex() {
        return this.my_index;
    }

    public void setSpecial() {
        this.is_special = 1;
    }

    public void setIceBall() {
        this.is_iceball = 1;
    }

    public void setAcid() {
        this.is_acid = 1;
    }

    public void setIrukandji() {
        this.is_irukandji = 1;
        this.is_acid = 1;
    }

    /** LaserBall.java:95-177. */
    @Override
    protected void onImpact(HitResult result) {
        Entity entityHit = LegacyProjectiles.entityHit(result);
        if (entityHit != null) {
            final float var2 = 16.0F;
            if (this.is_irukandji != 0) {
                entityHit.hurt(this.damageSources().thrown(this, this.getThrower()), 100.0F);
                this.discard();
                return;
            }
            // Acid does not hurt the bugs that spit it (LaserBall.java:103-112).
            if (this.is_acid != 0) {
                if (entityHit instanceof TrooperBug) {
                    this.discard();
                    return;
                }
                if (entityHit instanceof SpitBug) {
                    this.discard();
                    return;
                }
            }
            // Robots are immune to plain laser balls, five checks in the original order (LaserBall.java:113-134).
            // Robot1 (Bomb-Omb) is not among them in the original.
            if (this.is_iceball == 0 && this.is_acid == 0) {
                if (entityHit instanceof Robot2) {
                    this.discard();
                    return;
                }
                if (entityHit instanceof Robot3) {
                    this.discard();
                    return;
                }
                if (entityHit instanceof Robot4) {
                    this.discard();
                    return;
                }
                if (entityHit instanceof Robot5) {
                    this.discard();
                    return;
                }
                if (entityHit instanceof GiantRobot) {
                    this.discard();
                    return;
                }
            }
            if (entityHit instanceof Dragon d && this.is_acid == 0) { // LaserBall.java:135-145
                if (d.isVehicle()) {
                    this.discard();
                    return;
                }
                if (d.getDragonType() != 0 && this.is_iceball != 0) {
                    this.discard();
                    return;
                }
            }
            if (entityHit instanceof Player d2 && this.is_acid == 0) {
                if (d2.getVehicle() != null) {
                    this.discard();
                    return;
                }
            }
            entityHit.hurt(this.damageSources().thrown(this, this.getThrower()), var2);
            if (this.is_iceball == 0) {
                entityHit.igniteForSeconds(1.0F);
            }
        } else if (this.is_irukandji != 0 && !this.level().isClientSide) {
            this.spawnAtLocation(ModItems.DEAD_IRUKANDJI.get());
        }
        if (this.is_acid == 0) {
            int mx = 10;
            if (this.is_special != 0) {
                mx = 20;
            }
            for (int var3 = 0; var3 < mx; ++var3) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat() - this.random.nextFloat(), this.getZ() + this.random.nextFloat(), 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getY() + this.random.nextFloat() - this.random.nextFloat(),
                        this.getZ() + this.random.nextFloat() - this.random.nextFloat(), 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(),
                        this.level().random.nextGaussian(), this.level().random.nextGaussian(), this.level().random.nextGaussian());
            }
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.5F);
            if (!this.level().isClientSide && (this.is_special != 0 || this.is_iceball != 0)) {
                // createExplosion(this, ..., 3.0f, mobGriefing): unattributed, see LegacyProjectiles.explodeUnattributed.
                LegacyProjectiles.explodeUnattributed(this.level(), this.getX(), this.getY(), this.getZ(), 3.0F);
            }
        }
        this.discard();
    }

    /** LaserBall.java:179-209: the 200-tick limit is counted before the flight step. */
    @Override
    public void tick() {
        ++this.ticksalive;
        if (this.ticksalive > 200) {
            this.discard();
            return;
        }
        super.tick();
        this.my_rotation += 50.0F;
        while (this.my_rotation > 360.0F) {
            this.my_rotation -= 360.0F;
        }
        final float my_rotation = this.my_rotation;
        this.xRotO = my_rotation;
        this.setXRot(my_rotation);
        if (this.is_acid != 0) {
            return;
        }
        int mx = 4;
        if (this.is_special != 0) {
            mx = 10;
        }
        if (this.is_iceball != 0 && this.is_special == 0) {
            mx = 2;
        }
        for (int i = 0; i < mx; ++i) {
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.level().random.nextGaussian() / 2.0,
                    this.level().random.nextGaussian() / 2.0, this.level().random.nextGaussian() / 2.0);
            if (this.is_iceball == 0) {
                this.level().addParticle(LegacyProjectiles.reddust(this.level().random.nextGaussian() / 10.0, this.level().random.nextGaussian() / 10.0,
                        this.level().random.nextGaussian() / 10.0), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }
}
