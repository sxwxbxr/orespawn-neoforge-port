package com.swbr.orespawn.entity.moth;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Brutalfly} (Brutalfly.java:17-455), id {@code brutalfly} ("Brutalfly",
 * OreSpawnMain.java:4089, tracking 128/1/false). The magma moth a wounded CaterKiller turns into: an {@code EntityMob}
 * without a single AI task that steers itself through the air, shoots fireballs at players and bites monsters
 * (verhalten/entity-05.md).
 *
 * <p>Values: {@code Brutalfly_stats} (110/10/6), speed 0.35 (:39, :51), {@code experienceValue} 100 (:42),
 * {@code isImmuneToFire} (:43) as the type's {@code fireImmune()}, {@code fireResistance} 500 (:44). {@code setSize(5.0f,
 * 2.0f)} (:40) is the type's size. No DataWatcher entry, no NBT (:276-282); {@code currentFlightTarget} is volatile.
 * {@code canTriggerWalking} is {@code false} (:249-251): {@link NoStepTrigger} (R20).
 *
 * <p>Not carried over: {@code canDespawn} (:55-57) is the 1.21.1 default; {@code entityInit} (:59-61),
 * {@code onLivingUpdate} (:63-65), {@code isAIEnabled} (:109-111) and {@code initCreature} (:324-325) change nothing.
 */
public class Brutalfly extends Monster implements LegacyArmor, NoStepTrigger {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private int lastX;
    private int lastZ;
    private int lastY;
    private int stuck_count;
    private int wing_sound;
    private int health_ticker;
    private GenericTargetSorter TargetSorter;
    private float moveSpeed;

    /** {@code Brutalfly(World)} (:29-46). */
    public Brutalfly(final EntityType<? extends Brutalfly> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.lastX = 0;
        this.lastZ = 0;
        this.lastY = 0;
        this.stuck_count = 0;
        this.wing_sound = 0;
        this.health_ticker = 100;
        this.TargetSorter = null;
        this.moveSpeed = 0.35f;
        // PORT: getNavigator().setAvoidsWater(true) (:41) - water malus -1 (Hammerhead, W07).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 100;
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Brutalfly_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
        // The forward push of the AI step must survive the move control (InsectSupport.LegacyMoveControl, W05).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:48-53) at registration time (R3); armor from {@code getTotalArmorValue} (:67-69). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Brutalfly_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code fireResistance = 500} (:44). */
    @Override
    protected int getFireImmuneTicks() {
        return 500;
    }

    /** {@code getTotalArmorValue} (:67-69) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Brutalfly_stats().defense();
    }

    /** {@code getBrutalflyHealth} (:71-73). */
    public int getBrutalflyHealth() {
        return (int) this.getHealth();
    }

    /** {@code getSoundVolume} (:75-77). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:79-81). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:83-85). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:87-89). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:91-93): {@code "random.explode"}. */
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    /** {@code canBePushed} (:95-97): always, not only while alive. */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:99-100). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code collideWithNearbyEntities} (:102-103). */
    @Override
    protected void pushEntities() {
    }

    /** {@code mygetMaxHealth} (:105-107). */
    public int mygetMaxHealth() {
        return MobStats.Brutalfly_stats().health();
    }

    /**
     * {@code onUpdate} (:113-130), both sides: {@code motionY *= 0.6} after the tick, the wing sound every 31st tick on
     * the server, and +1 health every 100 ticks while below {@code mygetMaxHealth}.
     */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        ++this.wing_sound;
        if (this.wing_sound > 30) {
            if (!this.level().isClientSide) {
                ArthropodSupport.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 1.0f, 1.0f);
            }
            this.wing_sound = 0;
        }
        --this.health_ticker;
        if (this.health_ticker <= 0) {
            if (this.getHealth() < this.mygetMaxHealth()) {
                this.heal(1.0f);
            }
            this.health_ticker = 100;
        }
    }

    /** {@code canSeeTarget} (:132-134): no block (outline shape, no liquids) between y+0.75 and the target. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:136-247): stuck detection; a new flight target when stuck for more than 30 ticks, with
     * 1/200 or when within 3 blocks (lowered over deep ground, 30 tries); then - not {@code else} as in Mothra - with 1/6
     * the nearest visible survival player within 30/20/30 and a shot with 1/{@code shoot}, or with 1/3 a suitable target:
     * farther than 5 blocks a shot with 1/{@code shoot}, closer the vanilla melee hit. Then steer with an eighth of the
     * yaw error.
     *
     * <p>The player branch does not ask {@code PlayNicely} (original behaviour, verhalten/entity-05.md). {@code (int)}
     * casts are {@link Mth#floor} (R20); the original mixes {@code worldObj.rand} (level) and {@code this.rand} (entity),
     * both kept.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 30;
        int shoot = 3;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.lastX == Mth.floor(this.getX()) && this.lastY == Mth.floor(this.getY()) && this.lastZ == Mth.floor(this.getZ())) {
            ++this.stuck_count;
        } else {
            this.stuck_count = 0;
            this.lastX = Mth.floor(this.getX());
            this.lastY = Mth.floor(this.getY());
            this.lastZ = Mth.floor(this.getZ());
        }
        if (this.level().getDifficulty() == Difficulty.HARD) {
            shoot = 2;
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.stuck_count > 30 || this.level().random.nextInt(200) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 9.0f) {
            int down = 0;
            int dist = 20;
            for (int i = -5; i <= 5; i += 5) {
                for (int j = -5; j <= 5; j += 5) {
                    int k = 1;
                    while (k < 20) {
                        if (!MothSupport.isAir(this.level(), Mth.floor(this.getX()) + j, Mth.floor(this.getY()) - k, Mth.floor(this.getZ()) + i)) {
                            if (k < dist) {
                                dist = k;
                                break;
                            }
                            break;
                        } else {
                            ++k;
                        }
                    }
                }
            }
            if (dist > 10) {
                down = dist - 10 + 1;
            }
            // Block bid = Blocks.stone (:184): "not air yet".
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                xdir = 1;
                zdir = 1;
                if (this.level().random.nextInt(2) == 0) {
                    xdir = -1;
                }
                if (this.level().random.nextInt(2) == 0) {
                    zdir = -1;
                }
                int newz = this.random.nextInt(20) + 8;
                newz *= zdir;
                int newx = this.random.nextInt(20) + 8;
                newx *= xdir;
                this.currentFlightTarget.set(Mth.floor(this.getX()) + newx, Mth.floor(this.getY()) + this.level().random.nextInt(7) - 1 - down,
                        Mth.floor(this.getZ()) + newz);
                bidIsAir = MothSupport.isAir(this.level(), this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                        this.currentFlightTarget.getZ());
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
            }
            this.stuck_count = 0;
        }
        if (this.level().random.nextInt(6) == 0) {
            Player target = null;
            target = MothSupport.findNearestPlayer(this, 30.0, 20.0, 30.0);
            if (target != null) {
                if (!target.getAbilities().instabuild) {
                    if (this.getSensing().hasLineOfSight(target)) {
                        this.currentFlightTarget.set(Mth.floor(target.getX()), Mth.floor(target.getY()) + 4, Mth.floor(target.getZ()));
                        if (this.random.nextInt(shoot) == 0) {
                            this.attackWithSomething(target);
                        }
                    }
                } else {
                    target = null;
                }
            }
            if (target == null && this.level().random.nextInt(3) == 0) {
                LivingEntity e = null;
                e = this.findSomethingToAttack();
                if (e != null) {
                    this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 5, Mth.floor(e.getZ()));
                    if (this.distanceToSqr(e) > 25.0) {
                        if (this.level().random.nextInt(shoot) == 0) {
                            this.attackWithSomething(e);
                        }
                    } else {
                        this.doHurtTarget(e);
                    }
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.30001;
        final double motionY = m.y + (Math.signum(var2) * 0.7 - m.y) * 0.20001;
        final double motionZ = m.z + (Math.signum(var3) * 0.5 - m.z) * 0.30001;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + var5 / 8.0f);
    }

    /**
     * {@code canTriggerWalking} (:249-251) {@code false}: no step sounds or events; {@link NoStepTrigger} keeps OreSpawn's
     * {@code stepOn} blocks quiet too (R20).
     */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} (:253-254) is empty: no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:256-257) is empty: no fall distance, no landing effects. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:259-261). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** {@code attackEntityFrom} (:263-274): damage from another Brutalfly is refused; any source entity becomes the flight target, y+2. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Brutalfly) {
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 2, Mth.floor(e.getZ()));
        }
        return ret;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:284-322). A "Brutalfly" spawner allows it; otherwise Y at least 70, the
     * monster light test, night, air in x -3..2, z -4..3, y +1..+9, and no other Brutalfly within
     * {@code expand(64, 32, 64)}.
     *
     * <p>PORT: the spawner scan (x/z -2..2, y +1..+3) is {@link MobSpawnType#SPAWNER} (catalogue 5.9);
     * {@code isValidLightLevel} is {@link LegacyLightLevel} with the spawn's random.
     */
    public static boolean checkBrutalflySpawnRules(final EntityType<Brutalfly> type, final ServerLevelAccessor level,
                                                   final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 70.0) {
            return false;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        for (int k = -4; k < 4; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 1; i < 10; ++i) {
                    if (!MothSupport.isAir(level, pos.getX() + j, pos.getY() + i, pos.getZ() + k)) {
                        return false;
                    }
                }
            }
        }
        return level.getEntitiesOfClass(Brutalfly.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(64.0, 32.0, 64.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkBrutalflySpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:332-345): 20 {@code largeexplode} particles, 53 gold nuggets spread ±7 at y+1
     * ({@code dropItemRand}, :327-330), then 20 butterflies at +0.5/+1/+0.5 with their living sound.
     *
     * <p>PORT: the particles were spawned on the server and never reached a client (1.7.10 {@code World.spawnParticle});
     * only their {@code rand.nextFloat()} draws are kept.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 0; i < 20; ++i) {
            this.random.nextFloat();
            this.random.nextFloat();
            this.random.nextFloat();
        }
        for (int var4 = 0; var4 < 53; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(Items.GOLD_NUGGET, 1), 8);
        }
        for (int var4 = 0; var4 < 20; ++var4) {
            ItemSpawnEgg.spawnSomething(ModEntities.BUTTERFLY.get(), this.level(), this.getX() + 0.5, this.getY() + 1.0, this.getZ() + 0.5);
        }
    }

    /** {@code attackWithSomething} (:361-401): the fireball by difficulty, then +1 health while below {@code mygetMaxHealth}. No Peaceful check. */
    private void attackWithSomething(final LivingEntity par1) {
        MothSupport.fireAt(this, par1);
        if (this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    /**
     * {@code isSuitableTarget} (:403-437): alive; never Brutalfly, Mothra, Vortex or {@code isIgnoreable}; visible; any
     * monster; a player out of creative mode; nothing else.
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
        if (par1EntityLiving instanceof Brutalfly) {
            return false;
        }
        if (par1EntityLiving instanceof Mothra) {
            return false;
        }
        if (MothSupport.isType(par1EntityLiving, MothSupport.VORTEX)) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:439-454): nothing with {@code PlayNicely}; the first suitable entity in {@code expand(25, 20, 25)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(25.0, 20.0, 25.0)));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }
}
