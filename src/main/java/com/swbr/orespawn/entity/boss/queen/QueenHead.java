package com.swbr.orespawn.entity.boss.queen;

import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.stats.MobStats;
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

/**
 * Port of {@code danger.orespawn.QueenHead} (QueenHead.java:9-160), id {@code queen_head} ("QueenHead",
 * OreSpawnMain.java:3969, tracking 128/10/true), verhalten/entity-01.md "QueenHead": the invisible second hit box of
 * {@link TheQueen}, a separate entity (R9). A character-for-character copy of {@code KingHead}.
 *
 * <p>The server tick places it 12 blocks above the first Queen within 32 blocks and 30 blocks ahead along her head
 * yaw, copies her yaw, motion and health (in original units, R4), and removes it when no Queen is near. Every hit
 * except {@code inWall} and hits by the Queen or another head goes unchanged to the first Queen within 48/32/48, so
 * each head hit also makes her angry. The head itself never takes damage. No drops, no watched data, no NBT.
 *
 * <p>Original quirks kept (R18): {@code canTriggerWalQueen()} (:47-49) is a search-and-replace accident of
 * {@code canTriggerWalking}, overrides nothing, and so the head keeps the default {@code true}: no
 * {@link com.swbr.orespawn.entity.NoStepTrigger} (R20), unlike the King's head. Its tick has no {@code super}, so no
 * step ever happens either way.
 *
 * <p>PORT (R18 case 3): 1.7.10 wrote {@code posX/Y/Z} directly, which left the server bounding box at the spawn
 * point: the head then only "found" its Queen while she flew within 32 blocks of that point and was re-spawned by her
 * otherwise, and melee hits still landed because 1.7.10 checked the attack by distance. 1.21.1 validates a player's
 * attack against the target's bounding box ({@code ServerGamePacketListenerImpl.handleInteract},
 * {@code canInteractWithEntity(getBoundingBox(), ...)}), so a stale box would make the head unhittable. The port
 * moves the box with the position ({@code setPos}); the head therefore stays with its Queen instead of being
 * re-spawned.
 *
 * <p>R25 (heads alike, as {@code KingHead}): {@code setPos}, lightning without effect ({@link #thunderHit}). The R22
 * categories of {@code MyCanSee} are not in this class: the head has none, and {@code TheQueen.MyCanSee}
 * (TheQueen.java:868-922) lets only air through - no leaves or other category to widen; her horse check is
 * {@code AbstractHorse} there.
 */
public class QueenHead extends Mob implements Royalty, VirtualHealth {

    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    /** {@code QueenHead(World)} (:21-27) with {@code applyEntityAttributes} (:29-35). */
    public QueenHead(final EntityType<? extends QueenHead> type, final Level par1World) {
        super(type, par1World);
        // setSize(19.9f, 10.0f) (:23) is the entity type's size (R9).
        this.noPhysics = true;
        // fireResistance = 10000 (:25): getFireImmuneTicks(); isImmuneToFire = true (:26): fireImmune() on the type.
        // applyEntityAttributes (:29-35): the attribute supplier is built before the config loads (R3), so the runtime
        // health goes in here, clamped by R4 (PitchBlack precedent).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(MobStats.TheQueen_stats().health()));
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code applyEntityAttributes} (:29-35) with the config default: health {@code TheQueen_health} (6000, R4 clamp),
     * speed 1.33, and {@code attackDamage} registered with 0.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(6000.0))
                .add(Attributes.MOVEMENT_SPEED, 1.3300000429153442)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** R4: {@code TheQueen_stats.health}, the attribute value of the original. */
    @Override
    public double getOriginalMaxHealth() {
        return MobStats.TheQueen_stats().health();
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

    /** {@code updateFallState} (:44-45). */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    // canTriggerWalQueen (:47-49): overrides nothing, see the class comment. entityInit (:51-53) only calls super.

    /** {@code canBePushed} (:55-57). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /**
     * {@code attackEntityFrom} (:59-82): {@code inWall} and hits whose entity or direct source is a Queen or a head are
     * refused; everything else is handed unchanged to the first Queen in {@code expand(48, 32, 48)}, whose answer is
     * returned. Without a Queen the hit is refused.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        Entity e = par1DamageSource.getEntity();
        if (e != null && (e instanceof TheQueen || e instanceof QueenHead)) {
            return false;
        }
        e = par1DamageSource.getDirectEntity();
        if (e != null && (e instanceof TheQueen || e instanceof QueenHead)) {
            return false;
        }
        final List<TheQueen> var5 = this.level().getEntitiesOfClass(TheQueen.class, this.getBoundingBox().inflate(48.0, 32.0, 48.0));
        if (!var5.isEmpty()) {
            final TheQueen var8 = var5.get(0);
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
     * {@code setPositionAndRotation2} (:88-104): no vanilla interpolation; six steps (a rider adds eight to the packet's
     * count), the motion from the last velocity packet.
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

    /** Rotation-only packets start from the pending target (Leon precedent). */
    @Override
    public double lerpTargetX() {
        return this.boatPosRotationIncrements > 0 ? this.boatX : super.lerpTargetX();
    }

    @Override
    public double lerpTargetY() {
        return this.boatPosRotationIncrements > 0 ? this.boatY : super.lerpTargetY();
    }

    @Override
    public double lerpTargetZ() {
        return this.boatPosRotationIncrements > 0 ? this.boatZ : super.lerpTargetZ();
    }

    /** {@code setVelocity} (:106-114): the motion and a copy for {@link #lerpTo}. */
    @Override
    public void lerpMotion(final double par1, final double par3, final double par5) {
        this.setDeltaMovement(par1, par3, par5);
        this.velocityX = par1;
        this.velocityY = par3;
        this.velocityZ = par5;
    }

    /**
     * {@code onUpdate} (:116-159), without {@code super}: airborne, never burning; the client interpolates, the server
     * follows the first Queen within {@code expand(32, 32, 32)} or removes the head.
     *
     * <p>PORT: {@code isAirBorne = true} is {@code hasImpulse}, which makes the tracker send the motion as 1.7.10's did.
     * {@code setFire(0)} is {@code igniteForSeconds(0)}, which leaves the remaining fire ticks untouched when larger -
     * the 1.7.10 {@code setFire} also only raised them. The position is set with {@code setPos} (class comment).
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
                this.setRot(this.getYRot(), this.getXRot());
                --this.boatPosRotationIncrements;
            }
        } else {
            final List<TheQueen> var5 = this.level().getEntitiesOfClass(TheQueen.class, this.getBoundingBox().inflate(32.0, 32.0, 32.0));
            if (!var5.isEmpty()) {
                final TheQueen var8 = var5.get(0);
                final double posY = var8.getY() + 12.0;
                final double posX = var8.getX() - 30.0 * Math.sin(Math.toRadians(var8.getYHeadRot()));
                final double posZ = var8.getZ() + 30.0 * Math.cos(Math.toRadians(var8.getYHeadRot()));
                this.setPos(posX, posY, posZ);
                this.setYRot(var8.getYRot());
                this.setYHeadRot(var8.getYHeadRot());
                this.setDeltaMovement(var8.getDeltaMovement());
                VirtualHealth.mirrorHealth(var8, this);
            } else {
                this.discard();
            }
        }
    }

    /**
     * {@code onStruckByLightning} is not overridden in the original. 1.7.10's {@code Entity.onStruckByLightning} called
     * {@code dealFireDamage(5)} - nothing for this {@code isImmuneToFire} head - and raised the {@code fire} counter,
     * which nothing read: {@code onUpdate} skips {@code onEntityUpdate} and {@code isBurning} is false while immune.
     * PORT (R25): 1.21.1's default {@code thunderHit} calls {@code hurt(lightning, 5)}, which {@link #hurt} would forward
     * to The Queen; this override restores the original no-op (the unobservable counter included).
     */
    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightning) {
    }
}
