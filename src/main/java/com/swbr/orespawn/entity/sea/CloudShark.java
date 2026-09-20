package com.swbr.orespawn.entity.sea;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.aquatic.GoldFish;
import com.swbr.orespawn.entity.critter.CliffRacer;
import com.swbr.orespawn.entity.critter.Cockateil;
import com.swbr.orespawn.entity.critter.CritterSupport;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.portal.EntityAnt;
import com.swbr.orespawn.entity.rock.RockBase;
import com.swbr.orespawn.registry.ModSounds;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.CloudShark} (CloudShark.java:13-254), id {@code cloud_shark} ("Cloud Shark",
 * OreSpawnMain.java:3759, tracking 64/1/false). A hostile {@code EntityMob} flyer of the Islands and Chaos dimensions
 * that holds Y 120-140 on waypoints and bites small flyers and players (verhalten/entity-06.md).
 *
 * <p>Values: {@code CloudShark_stats} (15/6/5), speed 0.3, {@code experienceValue} 5 (:23, {@link Monster}'s
 * {@code xpReward}), {@code fireResistance} 5 (:25). No AI tasks, no DataWatcher, no NBT; all movement is
 * {@link #customServerAiStep} with {@link InsectSupport.LegacyMoveControl} (CliffRacer precedent, W06).
 *
 * <p>Not carried over: {@code canTriggerWalking = true} and {@code doesEntityNotTriggerPressurePlate = false}
 * (:151-163) are the 1.21.1 defaults (R20: no {@code NoStepTrigger}); {@code isAIEnabled} (:77-79). The water
 * breathing of {@code canBreatheUnderwater} (:251-253) is the tag {@code minecraft:can_breathe_under_water}.
 */
public class CloudShark extends Monster implements LegacyArmor {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;

    /** Constructor (:18-27). {@code setSize(1.0f, 0.75f)} is the entity type's size (R9). */
    public CloudShark(final EntityType<? extends CloudShark> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.xpReward = 5;
        this.TargetSorter = new GenericTargetSorter(this);
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
        final MobStats stats = MobStats.CloudShark_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:29-34) at registration time (R3); armor from {@code getTotalArmorValue} (:86-88). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.CloudShark_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code fireResistance = 5} (:25). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /**
     * {@code attackEntityAsMob} (:36-40): the attribute damage as mob damage, nothing else - no enchantment bonus, no
     * knockback, no {@code setLastAttacker}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final float f = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return par1Entity.hurt(this.damageSources().mobAttack(this), f);
    }

    /** {@code canDespawn} (:42-44): not persistent and not daytime - it despawns at night only. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !InsectSupport.isDaytime(this.level());
    }

    /** {@code getSoundVolume} (:46-48). */
    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    /** {@code getSoundPitch} (:50-52). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:54-56): {@code "splash"} has no namespace and is silent (R18). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:58-60). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LITTLE_SPLAT.get();
    }

    /** {@code getDeathSound} (:62-64). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BIG_SPLAT.get();
    }

    /** {@code canBePushed} (:66-68). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:70-71), empty. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:73-75). */
    public int mygetMaxHealth() {
        return MobStats.CloudShark_stats().health();
    }

    /** {@code onUpdate} (:81-84), both sides: {@code motionY *= 0.6} after the move. */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
    }

    /** {@code getTotalArmorValue} (:86-88) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.CloudShark_stats().defense();
    }

    /** {@code canSeeTarget} (:90-92): ray from 0.75 above the feet. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return CritterSupport.canSeeTarget(this, 0.75, pX, pY, pZ);
    }

    /**
     * {@code updateAITasks} (:94-149). Height band: below Y 120 the waypoint goes 2 up, above 140 2 down. With 1 in 300
     * or closer than {@code distSq 2.1} a new visible air waypoint x/z ±(8..17), y {@code rand(5) - 2 + updown}
     * (50 tries). With 1 in 9 look for prey; the waypoint becomes its position and within {@code distSq 9} it bites.
     * Steer 0.5/0.3 horizontally, 0.7/0.2 vertically, a quarter of the yaw difference.
     *
     * <p>R20: every {@code (int)} cast of a coordinate is {@code Mth.floor}.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        int updown = 0;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (Mth.floor(this.getY()) < 120) {
            updown = 2;
        }
        if (Mth.floor(this.getY()) > 140) {
            updown = -2;
        }
        if (this.random.nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0; --keep_trying) {
                zdir = this.random.nextInt(10) + 8;
                xdir = this.random.nextInt(10) + 8;
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir,
                        Mth.floor(this.getY()) + this.random.nextInt(5) - 2 + updown,
                        Mth.floor(this.getZ()) + zdir);
                bid = this.level().getBlockState(this.currentFlightTarget);
                if (bid.isAir() && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bid = Blocks.STONE.defaultBlockState();
                }
            }
        }
        if (this.random.nextInt(9) == 2) {
            LivingEntity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < 9.0) {
                    this.doHurtTarget(e);
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.5 - motion.x) * 0.30000000149011613;
        final double motionY = motion.y + (Math.signum(var2) * 0.699999988079071 - motion.y) * 0.20000000149011612;
        final double motionZ = motion.z + (Math.signum(var3) * 0.5 - motion.z) * 0.30000000149011613;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + var5 / 4.0f);
    }

    /** {@code fall} and {@code updateFallState} (:155-159), both empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code attackEntityFrom} (:165-172): after the hit, the attacker's position becomes the waypoint. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final boolean ret = super.hurt(par1DamageSource, par2);
        final Entity e = par1DamageSource.getEntity();
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
        }
        return ret;
    }

    /** {@code getCanSpawnHere} (:174-176): always. */
    public static boolean checkCloudSharkSpawnRules(final EntityType<CloudShark> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkCloudSharkSpawnRules}; {@code Monster}'s light weight is not asked. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code isSuitableTarget} (:178-216), in this order: alive, visible, never a rock or an ant, then butterflies,
     * birds, mosquitoes, fireflies, non-creative players, gold fish and cliff racers.
     *
     * <p>{@code EntityButterfly} includes its subclass {@code EntityLunaMoth}, and Mothra (W08) once it extends it -
     * exactly as the original's {@code instanceof}. {@code Cockateil} includes {@code RubyBird}.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof RockBase) {
            return false;
        }
        if (par1EntityLiving instanceof EntityAnt) {
            return false;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return true;
        }
        if (par1EntityLiving instanceof Cockateil) {
            return true;
        }
        if (par1EntityLiving instanceof EntityMosquito) {
            return true;
        }
        if (par1EntityLiving instanceof Firefly) {
            return true;
        }
        if (par1EntityLiving instanceof Player p) {
            if (!p.getAbilities().instabuild) {
                return true;
            }
        }
        return par1EntityLiving instanceof GoldFish || par1EntityLiving instanceof CliffRacer;
    }

    /** {@code findSomethingToAttack} (:218-235): nothing with {@code PlayNicely}; else the first suitable in {@code expand(12, 10, 12)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 10.0, 12.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getDropItem} (:237-249): {@code rand(3)} on the world random - paper, string or bone. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(3);
        if (i == 0) {
            return Items.PAPER;
        }
        if (i == 1) {
            return Items.STRING;
        }
        if (i == 2) {
            return Items.BONE;
        }
        return null;
    }

    /** Vanilla 1.7.10 {@code dropFewItems} with {@link #getDropItem}, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, this.getDropItem());
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }
}
