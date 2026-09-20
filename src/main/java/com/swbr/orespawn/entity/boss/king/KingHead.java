package com.swbr.orespawn.entity.boss.king;

import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.util.Royalty;
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
 * Port of {@code danger.orespawn.KingHead} (KingHead.java:9-160, verhalten/entity-01.md): the invisible second hitbox of
 * The King's three heads, an {@code EntityLiving} registered 128/10/true (OreSpawnMain.java:3955). It has no life of its
 * own: every server tick it sits 12 blocks above and 30 blocks in front of the first King within 32 blocks and copies
 * his health; a hit on it goes unchanged to the first King within 48/32/48. Without a King it removes itself. The King
 * spawns it (TheKing.java:456-458); it stays a separate entity (R9).
 *
 * <p>{@code onUpdate} does not call {@code super.onUpdate()} (:116-159): no vanilla living tick, no movement, no death,
 * no air, no fire. {@link #tick()} is overridden the same way. The client half is the boat-style interpolation of the
 * original ({@code setPositionAndRotation2}, :89-104, :122-136), fed by {@link #lerpTo}.
 *
 * <p>Health is virtual (R4): the attribute holds {@code min(TheKing_health, 1024)} and the mirror goes through
 * {@link VirtualHealth#mirrorHealth}, so a 7000-point King and his 7000-point head carry the same original value.
 *
 * <p>PORT: the server position is set with {@link #setPos}, which moves the bounding box with it. 1.7.10 assigned
 * {@code posX/posY/posZ} directly (:145-147), which left the server bounding box where the head was spawned (catalogue
 * 6.4 names the same for GodzillaHead). 1.21.1 checks a melee hit against that bounding box
 * ({@code ServerGamePacketListenerImpl.handleInteract}: {@code canInteractWithEntity(entity.getBoundingBox(), ...)}),
 * where 1.7.10 checked the distance to the position; a stale box would make the head unhittable in melee, which it
 * was not (R18 case 3). The box searches for the King (:138, :72) therefore follow the head, too.
 */
public class KingHead extends Mob implements VirtualHealth, Royalty, NoStepTrigger {

    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    /** {@code KingHead(World)} (:21-27) with {@code applyEntityAttributes} (:29-35). */
    public KingHead(final EntityType<? extends KingHead> type, final Level par1World) {
        super(type, par1World);
        // setSize(19.9f, 10.0f) (:23) is the entity type's size (R9).
        this.noPhysics = true;
        // fireResistance 10000 (:25) is getFireImmuneTicks(); isImmuneToFire (:26) is fireImmune() of the type.
        // applyEntityAttributes: the config is loaded by now (R3, PitchBlack precedent).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(MobStats.TheKing_stats().health()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(1.3300000429153442);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0);
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code applyEntityAttributes} (:29-35) with the manifest defaults: {@code TheKing_health} 7000 (R4-clamped), speed
     * 1.33, attack damage registered with 0.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(7000.0))
                .add(Attributes.MOVEMENT_SPEED, 1.3300000429153442)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** R4: {@code TheKing_stats.health} (:31). */
    @Override
    public double getOriginalMaxHealth() {
        return MobStats.TheKing_stats().health();
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

    /** {@code fall} (:41-42) is empty. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:44-45) is empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code canTriggerWalking} (:47-49) {@code false}: no step sounds; the other half is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    // entityInit (:51-53) only calls super.

    /** {@code canBePushed} (:55-57). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /**
     * {@code attackEntityFrom} (:59-82): {@code inWall} is ignored, and so is every hit whose attacker or direct source
     * is a King or a King head; anything else goes unchanged to the first King within 48/32/48, whose answer is
     * returned. The head itself never loses health.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        Entity e = par1DamageSource.getEntity();
        if (e != null && (e instanceof TheKing || e instanceof KingHead)) {
            return false;
        }
        e = par1DamageSource.getDirectEntity();
        if (e != null && (e instanceof TheKing || e instanceof KingHead)) {
            return false;
        }
        final List<TheKing> var5 = this.level().getEntitiesOfClass(TheKing.class, this.getBoundingBox().inflate(48.0, 32.0, 48.0));
        if (!var5.isEmpty()) {
            final TheKing var8 = var5.get(0);
            ret = var8.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /**
     * {@code canBeCollidedWith} (:84-86) {@code true} - in 1.7.10 that meant "can be hit by the crosshair", which is
     * {@code isPickable} in 1.21.1 and already true for a living entity. 1.21.1's {@code canBeCollidedWith} (solid like
     * a boat) is not meant and stays false.
     */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /**
     * {@code setPositionAndRotation2} (:88-104): the client aims at the new position in 6 steps ({@code steps + 8} with a
     * rider, which the head never has) and restores the last velocity packet.
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

    /** {@code setVelocity} (:106-114): the velocity packet. */
    @Override
    public void lerpMotion(final double par1, final double par3, final double par5) {
        this.velocityX = par1;
        this.velocityY = par3;
        this.velocityZ = par5;
        this.setDeltaMovement(par1, par3, par5);
    }

    /**
     * {@code onUpdate} (:116-159), without the vanilla tick. {@code isAirBorne = true} is {@code hasImpulse};
     * {@code setFire(0)} never lowers the fire timer in 1.7.10 and does nothing, {@code igniteForTicks(0)} likewise.
     */
    @Override
    public void tick() {
        if (this.isRemoved()) {
            return;
        }
        this.hasImpulse = true;
        this.igniteForTicks(0);
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
                this.setRot(this.getYRot(), this.getXRot());
                --this.boatPosRotationIncrements;
            }
        } else {
            final List<TheKing> var5 = this.level().getEntitiesOfClass(TheKing.class, this.getBoundingBox().inflate(32.0, 32.0, 32.0));
            if (!var5.isEmpty()) {
                final TheKing var8 = var5.get(0);
                // PORT: setPos instead of the direct field writes, see the class comment.
                this.setPos(var8.getX() - 30.0 * Math.sin(Math.toRadians(var8.getYHeadRot())),
                        var8.getY() + 12.0,
                        var8.getZ() + 30.0 * Math.cos(Math.toRadians(var8.getYHeadRot())));
                this.setYRot(var8.getYRot());
                this.setYHeadRot(var8.getYHeadRot());
                final Vec3 km = var8.getDeltaMovement();
                this.setDeltaMovement(km.x, km.y, km.z);
                VirtualHealth.mirrorHealth(var8, this);
            } else {
                this.discard();
            }
        }
    }

    /**
     * {@code onStruckByLightning} is not overridden in the original; 1.7.10's {@code Entity.onStruckByLightning} only
     * called {@code dealFireDamage(5)}, a no-op for this fire-immune head. PORT: 1.21.1's default {@code thunderHit}
     * calls {@code hurt(lightning, 5)}, which {@link #hurt} would forward to The King; this override restores the
     * original no-op.
     */
    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightning) {
    }
}
