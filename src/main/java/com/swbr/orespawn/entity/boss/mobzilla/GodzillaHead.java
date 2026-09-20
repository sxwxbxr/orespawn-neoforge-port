package com.swbr.orespawn.entity.boss.mobzilla;

import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.NoStepTrigger;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.GodzillaHead} (GodzillaHead.java:9-160), id {@code mobzilla_head} ("MobzillaHead",
 * OreSpawnMain.java:3781, tracking 128/10/true). The invisible stand-in for Mobzilla's head: it follows the first
 * {@link Godzilla} within 32 blocks, 16 up and 17 ahead along its head yaw, mirrors its health, and hands every hit to it
 * unchanged, so a player can reach a boss 25 blocks tall (verhalten/entity-03.md). Spawned only by Godzilla (Godzilla.java:350).
 *
 * <p>Values: size 9.9 x 10, {@code noClip} (:23-24), {@code fireResistance} 10000 and {@code isImmuneToFire} (:25-26, the
 * latter {@code fireImmune()} on the type), health {@code Godzilla_stats.health} (4000, R4: virtual), speed 1.33, attack 0.
 * No DataWatcher entries, no NBT.
 *
 * <p>{@code onUpdate} (:116-159) never calls {@code super}: no movement, no living tick, no death, no despawn roll. The
 * port's {@link #tick} does the same.
 *
 * <p>R25 (heads alike): position by {@code setPos} (see {@link #tick}), lightning without effect (see
 * {@link #thunderHit}). The R22 {@code MyCanSee} categories do not reach this head: neither {@code GodzillaHead} nor
 * {@code Godzilla} has a {@code MyCanSee}; Godzilla's own R22 categories (skeletons, terrain) live in its body class.
 */
public class GodzillaHead extends Mob implements VirtualHealth, NoStepTrigger {

    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    /** {@code GodzillaHead(World)} (:21-27). */
    public GodzillaHead(final EntityType<? extends GodzillaHead> type, final Level par1World) {
        super(type, par1World);
        this.noPhysics = true;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.getOriginalMaxHealth()));
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:29-35) at registration time (R3); MAX_HEALTH clamped per R4. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(MobStats.Godzilla_stats(StatSource.EARLY).health()))
                .add(Attributes.MOVEMENT_SPEED, 1.3300000429153442)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** R4: {@code Godzilla_stats.health} (:31). */
    @Override
    public double getOriginalMaxHealth() {
        return MobStats.Godzilla_stats().health();
    }

    /** {@code fireResistance = 10000} (:25). */
    @Override
    protected int getFireImmuneTicks() {
        return 10000;
    }

    /** {@code canDespawn} (:37-39). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code fall} (:41-42): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:44-45): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code canTriggerWalking} (:47-49) {@code false}: no step sounds; the other half is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code canBePushed} (:55-57). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /**
     * {@code attackEntityFrom} (:59-82): suffocation is ignored, and so is anything caused or thrown by Mobzilla or a head;
     * every other hit goes unchanged to the first Mobzilla within 32 blocks, whose {@code hurt} applies its cap, timer and
     * retaliation. The head itself never takes damage.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        Entity e = par1DamageSource.getEntity();
        if (e != null && (e instanceof Godzilla || e instanceof GodzillaHead)) {
            return false;
        }
        e = par1DamageSource.getDirectEntity();
        if (e != null && (e instanceof Godzilla || e instanceof GodzillaHead)) {
            return false;
        }
        final List<Godzilla> var5 = this.level().getEntitiesOfClass(Godzilla.class, this.getBoundingBox().inflate(32.0, 32.0, 32.0));
        if (!var5.isEmpty()) {
            final Godzilla var8 = var5.get(0);
            ret = var8.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /**
     * {@code canBeCollidedWith} (:84-86) {@code true} - in 1.7.10 "can be hit by the crosshair", which is
     * {@code isPickable} in 1.21.1 (as {@code KingHead}: not while removed, the 1.21.1 living default).
     */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /**
     * {@code setPositionAndRotation2} (:88-104), client: remember the target, 6 steps (8 more than the packet's when
     * ridden), and take the remembered velocity.
     */
    @Override
    public void lerpTo(final double par1, final double par3, final double par5, final float par7, final float par8, final int par9) {
        if (this.isVehicle()) {
            this.boatPosRotationIncrements = par9 + 8;
        } else {
            this.boatPosRotationIncrements = 6;
        }
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
        this.setDeltaMovement(this.velocityX, this.velocityY, this.velocityZ);
    }

    /** {@code setVelocity} (:106-114), client. */
    @Override
    public void lerpMotion(final double par1, final double par3, final double par5) {
        this.setDeltaMovement(par1, par3, par5);
        this.velocityX = par1;
        this.velocityY = par3;
        this.velocityZ = par5;
    }

    /**
     * {@code onUpdate} (:116-159), without {@code super}: airborne and {@code setFire(0)} (a no-op in both versions) every
     * tick; on the client the boat-style interpolation, on the server the coupling to the first Mobzilla within 32 blocks -
     * or {@code setDead} without one.
     *
     * <p>PORT (R18 case 3): the position is set with {@code setPos}, not by writing the fields. In 1.7.10 the direct write
     * left the server bounding box at the spawn point (verhalten/entity-03.md, derived), which the 1.7.10 server never used
     * for melee; 1.21.1 validates a player's attack against the server bounding box
     * ({@code ServerGamePacketListenerImpl.handleInteract}), so a stale box would make the head unhittable once Mobzilla
     * walks away. {@code isAirBorne} is {@code hasImpulse} (velocity packet on the next tracker update). The health mirror
     * goes through {@link VirtualHealth#mirrorHealth} (R4).
     */
    @Override
    public void tick() {
        if (this.isRemoved()) {
            return;
        }
        this.hasImpulse = true;
        this.igniteForSeconds(0.0f);
        if (this.level().isClientSide) {
            if (this.boatPosRotationIncrements > 0) {
                final double d4 = this.getX() + (this.boatX - this.getX()) / this.boatPosRotationIncrements;
                final double d5 = this.getY() + (this.boatY - this.getY()) / this.boatPosRotationIncrements;
                final double d6 = this.getZ() + (this.boatZ - this.getZ()) / this.boatPosRotationIncrements;
                this.setPos(d4, d5, d6);
                this.setXRot(this.getXRot() + (float) ((this.boatPitch - this.getXRot()) / this.boatPosRotationIncrements));
                double d7 = Mth.wrapDegrees(this.boatYaw - this.getYRot());
                final Entity rider = this.getFirstPassenger();
                if (rider != null) {
                    d7 = Mth.wrapDegrees(rider.getYRot() - (double) this.getYRot());
                }
                this.setYRot(this.getYRot() + (float) (d7 / this.boatPosRotationIncrements));
                // setRotation(rotationYaw, rotationPitch) (:130): both angles modulo 360, as KingHead.
                this.setRot(this.getYRot(), this.getXRot());
                --this.boatPosRotationIncrements;
            }
        } else {
            final List<Godzilla> var5 = this.level().getEntitiesOfClass(Godzilla.class, this.getBoundingBox().inflate(32.0, 32.0, 32.0));
            if (!var5.isEmpty()) {
                final Godzilla var8 = var5.get(0);
                final double posY = var8.getY() + 16.0;
                final double posX = var8.getX() - 17.0 * Math.sin(Math.toRadians(var8.getYHeadRot()));
                final double posZ = var8.getZ() + 17.0 * Math.cos(Math.toRadians(var8.getYHeadRot()));
                this.setPos(posX, posY, posZ);
                this.setYRot(var8.getYRot());
                this.setYHeadRot(var8.getYHeadRot());
                final Vec3 m = var8.getDeltaMovement();
                this.setDeltaMovement(m.x, m.y, m.z);
                VirtualHealth.mirrorHealth(var8, this);
            } else {
                this.discard();
            }
        }
    }

    /**
     * {@code onStruckByLightning} is not overridden in the original. 1.7.10's {@code Entity.onStruckByLightning} called
     * {@code dealFireDamage(5)} - nothing for an {@code isImmuneToFire} entity - and then raised the {@code fire} counter,
     * which nothing of this head ever read: its {@code onUpdate} skips {@code onEntityUpdate} (no burning flag, no fire
     * damage) and {@code isBurning} is false while immune. The strike had no effect at all.
     *
     * <p>PORT (R25, as {@code KingHead}): 1.21.1's default {@code thunderHit} ends in {@code hurt(lightningBolt, 5)},
     * which {@link #hurt} would hand to Mobzilla; this override is the original no-op. The fire counter increment is left
     * out too - unobservable in both versions ({@code isOnFire} is false for a fire-immune entity, and the tick never
     * burns), and {@code KingHead}/{@code QueenHead} leave it out the same way.
     */
    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightning) {
    }
}
