package com.swbr.orespawn.entity.boss.king;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.util.Royalty;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.PurplePower} (PurplePower.java:16-318, verhalten/entity-01.md): the homing energy orb
 * of the royal family, an {@code EntityLiving} with its own steering, registered 64/1/true (OreSpawnMain.java:3114).
 * It wanders through blocks, picks the nearest suitable living target, hits it once on contact and vanishes.
 *
 * <p>Types (DataWatcher 20): 0 The Queen's orb (quarter health minus one, then an eighth of the maximum as mob damage),
 * 1-3 The Princess's orbs (fifteen sixteenths health, 5 damage, then fire, poison or weakness), 10 the Ultimate King's
 * orb (like 0 plus an explosion of 9.1 at the target). Types 1-3 spare players and tamed animals. A type 10 orb that
 * dies of old age (1/2500 per server tick) explodes where it is. There is no cap on the number of orbs (R18).
 *
 * <p>Armor 25 blocks every blockable hit under the 1.7.10 formula (R5, {@link LegacyArmor}); a hit is capped at 10.
 *
 * <p>{@code setHealth} on the target is in original units ({@link VirtualHealth#setOriginalHealth}), so the orbs
 * take The King's and The Queen's 7000/6000 points exactly as in 1.7.10 (R4); the {@code getMaxHealth() / 8} damage
 * reads the original maximum for the same reason. The effect for ordinary targets is unchanged.
 */
public class PurplePower extends Mob implements LegacyArmor, Royalty, NoStepTrigger {

    /** DataWatcher 20: {@code purple_type}. */
    private static final EntityDataAccessor<Integer> DATA_PURPLE_TYPE = SynchedEntityData.defineId(PurplePower.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    private int purple_type;

    /** {@code PurplePower(World)} (:22-33) with {@code applyEntityAttributes} (:35-41). */
    public PurplePower(final EntityType<? extends PurplePower> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.purple_type = 0;
        // setSize(0.75f, 0.75f) (:27) is the entity type's size (R9).
        this.xpReward = 35;
        // isImmuneToFire (:29) is fireImmune() of the type; fireResistance 25 (:30) is getFireImmuneTicks().
        this.TargetSorter = new GenericTargetSorter(this);
        this.noPhysics = true;
        this.moveControl = new TheKing.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:35-41): {@code mygetMaxHealth()} 1000, speed 0.25, attack damage 500. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 500.0);
    }

    /** {@code entityInit} (:43-46). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PURPLE_TYPE, 0);
    }

    /** {@code setPurpleType} (:48-57): server only. */
    public void setPurpleType(final int par1) {
        if (this.level() == null) {
            return;
        }
        if (this.level().isClientSide) {
            return;
        }
        this.purple_type = par1;
        this.entityData.set(DATA_PURPLE_TYPE, par1);
    }

    /** {@code getPurpleType} (:59-61). */
    public int getPurpleType() {
        return this.entityData.get(DATA_PURPLE_TYPE);
    }

    /** {@code canDespawn} (:63-65). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getSoundVolume} (:67-69); there is no sound to play. */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:71-73). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:75-77). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:79-81). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:83-85). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:87-89). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code collideWithEntity} (:91-92) is empty. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:94-96). */
    public int mygetMaxHealth() {
        return 1000;
    }

    /** {@code fireResistance = 25} (:30). */
    @Override
    protected int getFireImmuneTicks() {
        return 25;
    }

    /**
     * {@code onUpdate} (:102-126): the vanilla tick, the damped climb, the client's firework sparks, the type handshake
     * (client reads the watcher, server rewrites it from the field) and death of old age, exploding for type 10.
     *
     * <p>{@code newExplosion(null, x, y + 0.25, z, 9.1, true, mobGriefing)}: fire always, blocks only with
     * {@code mobGriefing} - {@code ExplosionInteraction.MOB} with no entity is exactly that gamerule
     * ({@code Level.explode}: {@code EventHooks.canEntityGrief(level, null)}).
     */
    @Override
    public void tick() {
        super.tick();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        final Level world = this.level();
        if (this.getPurpleType() == 0) {
            if (world.isClientSide && world.random.nextInt(4) == 1) {
                world.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.25, this.getZ(),
                        (double) ((world.random.nextFloat() - world.random.nextFloat()) / 2.0f),
                        (double) ((world.random.nextFloat() - world.random.nextFloat()) / 2.0f),
                        (double) ((world.random.nextFloat() - world.random.nextFloat()) / 2.0f));
            }
        } else if (world.isClientSide && world.random.nextInt(6) == 1) {
            world.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() + 0.6499999761581421, this.getZ(),
                    (double) ((world.random.nextFloat() - world.random.nextFloat()) / 5.0f),
                    (double) ((world.random.nextFloat() - world.random.nextFloat()) / 5.0f),
                    (double) ((world.random.nextFloat() - world.random.nextFloat()) / 5.0f));
        }
        if (world.isClientSide) {
            this.purple_type = this.getPurpleType();
        } else {
            this.setPurpleType(this.purple_type);
        }
        if (!world.isClientSide && world.random.nextInt(2500) == 1) {
            if (this.getPurpleType() == 10) {
                world.explode(null, this.getX(), this.getY() + 0.25, this.getZ(), 9.1f, true, Level.ExplosionInteraction.MOB);
            }
            this.discard();
        }
    }

    /**
     * {@code canSeeTarget} (:128-130): no block between a point 0.55 above the feet and the target. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded - the outline shape
     * (PitchBlack precedent).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.55, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:132-184): a new waypoint with 1/300 or on arrival (up to 50 tries for an air block in
     * sight), otherwise 1/7 a target search outside peaceful - the target becomes the waypoint and is hit on contact,
     * after which the orb is gone; peaceful removes it; then the steering.
     *
     * <p>{@code this.rand} is {@link #getRandom()}; {@code moveForward} is {@code zza}; the move helper of
     * {@code super.updateAITasks()} runs first ({@link TheKing.LegacyMoveControl}). {@code (int)} casts of coordinates
     * are {@link Mth#floor} (R20).
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        LivingEntity e = null;
        if (this.isRemoved()) {
            return;
        }
        ((TheKing.LegacyMoveControl) this.moveControl).legacyTick();
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.getRandom().nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            // Block bid = Blocks.stone (:145): "not air yet".
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                zdir = this.getRandom().nextInt(10) + 8;
                xdir = this.getRandom().nextInt(10) + 8;
                if (this.getRandom().nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.getRandom().nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir, Mth.floor(this.getY()) + this.getRandom().nextInt(20) - 10,
                        Mth.floor(this.getZ()) + zdir);
                final BlockState bid = this.level().getBlockState(this.currentFlightTarget);
                // PORT: isAir() also accepts cave and void air, which 1.7.10 generated as plain air (Bee precedent).
                bidIsAir = bid.isAir();
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
            }
        } else if (this.getRandom().nextInt(7) == 2 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + e.getBbHeight() / 2.0f), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < (4.0f + e.getBbWidth() / 2.0f) * (4.0f + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                    this.discard();
                }
            }
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.4 - motion.x) * 0.2;
        final double motionY = motion.y + (Math.signum(var2) * 0.699999988079071 - motion.y) * 0.20000000149011612;
        final double motionZ = motion.z + (Math.signum(var3) * 0.4 - motion.z) * 0.2;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + var5 / 4.0f);
    }

    /** {@code canTriggerWalking} (:186-188) {@code false}: no step sounds; the other half is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} (:190-191) is empty. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:193-194) is empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:196-198). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * {@code attackEntityFrom} (:200-215): a source whose {@code getEntity()} is an arrow is refused; the rest is capped
     * at 10 and taken, and the attacker's middle becomes the waypoint.
     *
     * <p>PORT: {@code EntityArrow} is {@link AbstractArrow}. An arrow with a shooter reports the shooter in both
     * versions and hits. An ownerless arrow (dispenser, shooter gone) built {@code causeArrowDamage(this, this)} in
     * 1.7.10, so its {@code getEntity()} was the arrow itself and the orb refused it. 1.21.1's
     * {@code damageSources().arrow(arrow, null)} reports {@code null} instead, so the direct arrow stands in for the
     * missing cause here to keep that immunity.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        final Entity e = par1DamageSource.getEntity() == null && par1DamageSource.getDirectEntity() instanceof AbstractArrow
                ? par1DamageSource.getDirectEntity() : par1DamageSource.getEntity();
        float dm = par2;
        if (e != null && e instanceof AbstractArrow) {
            return false;
        }
        if (dm > 10.0f) {
            dm = 10.0f;
        }
        ret = super.hurt(par1DamageSource, dm);
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + e.getBbHeight() / 2.0f), Mth.floor(e.getZ()));
        }
        return ret;
    }

    /** {@code getCanSpawnHere} (:217-219): always. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getTotalArmorValue} (:221-223). */
    public int getTotalArmorValue() {
        return 25;
    }

    /** R5. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /**
     * {@code isSuitableTarget} (:225-256): not in peaceful, alive, not ignorable, in sight (sensing cache); players out
     * of creative only for types 0 and 10 (and negative types); tamed animals are spared by types other than 0 and 10;
     * never royalty. Everything else living is a target.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild && (this.getPurpleType() <= 0 || this.getPurpleType() == 10);
        }
        if (this.getPurpleType() != 0 && this.getPurpleType() != 10 && par1EntityLiving instanceof TamableAnimal e) {
            if (e.isTame()) {
                return false;
            }
        }
        return !MyUtils.isRoyalty(par1EntityLiving);
    }

    /** {@code findSomethingToAttack} (:258-275): nothing under {@code PlayNicely}; else the first suitable in 32/24/32. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(32.0, 24.0, 32.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * {@code attackEntityAsMob} (:277-303). Types 0 and 10: health to a quarter minus one, then an eighth of the maximum
     * as mob damage, and type 10 explodes at the target (9.1, fire, blocks by {@code mobGriefing}). Other types:
     * fifteen sixteenths of the health, 5 mob damage, and fire for 10 seconds (1), poison (2) or weakness (3) for
     * 50 ticks.
     *
     * <p>{@code setHealth} runs in original units (R4, see the class comment). A result at or below zero kills without
     * a damage source, as in 1.7.10.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        boolean var4 = false;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final LivingEntity e = (LivingEntity) par1Entity;
            if (this.getPurpleType() == 0 || this.getPurpleType() == 10) {
                VirtualHealth.setOriginalHealth(e, VirtualHealth.originalHealth(e) / 4.0f - 1.0f);
                var4 = e.hurt(this.damageSources().mobAttack(this), (float) VirtualHealth.originalMaxHealth(e) / 8.0f);
                if (this.getPurpleType() == 10) {
                    this.level().explode(null, e.getX(), e.getY() - 0.25, e.getZ(), 9.1f, true, Level.ExplosionInteraction.MOB);
                }
            } else {
                VirtualHealth.setOriginalHealth(e, VirtualHealth.originalHealth(e) * 15.0f / 16.0f);
                var4 = e.hurt(this.damageSources().mobAttack(this), 5.0f);
                if (this.getPurpleType() == 1) {
                    e.igniteForSeconds(10.0f);
                }
                if (this.getPurpleType() == 2) {
                    e.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
                }
                if (this.getPurpleType() == 3) {
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0));
                }
            }
        }
        return var4;
    }

    // getDropItem (:305-307) is null: no drops. 1.7.10 dropFewItems of EntityLiving dropped getDropItem only.

    /** {@code writeEntityToNBT} (:309-312). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("PurpleType", this.purple_type);
    }

    /** {@code readEntityFromNBT} (:314-317). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.purple_type = par1NBTTagCompound.getInt("PurpleType");
    }
}
