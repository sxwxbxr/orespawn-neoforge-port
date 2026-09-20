package com.swbr.orespawn.entity.robot;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.projectile.LaserBall;
import com.swbr.orespawn.entity.projectile.LegacyProjectiles;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Robot4} ("Robo-Warrior", Robot4.java:16-435, verhalten/entity-11.md): shield on
 * the right arm, laser cannon on the left; knocks close targets away and shoots the rest, and ignores every hit for
 * 65 ticks after one lands.
 *
 * <p>Health, attack and defense from {@code Robot4_stats} (170/12/18, :48-50, :79-81, :102-104), speed 0.34 set
 * every tick (:30, :74-77), {@code experienceValue} 120 (:33), {@code fireResistance} 120 (:34), fire immune (:35,
 * the entity type), hitbox 2.5 x 4.0 (:31).
 *
 * <p>Original behaviour kept (R18): the shield flag (DataWatcher 21) is only ever written by the client model, so
 * the server reads 0 and the shield never blocks ({@code DECISIONS} R18, "Robot4-Schild"); {@link #getAttackStrength}
 * is dead code; the {@code +0.25} jump boost is overwritten; Laser Balls never hurt it (in {@code LaserBall}).
 */
public class Robot4 extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);
    /** DataWatcher 21: {@code shielding}, set by the client model only (see the class comment). */
    private static final EntityDataAccessor<Integer> DATA_SHIELDING = SynchedEntityData.defineId(Robot4.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side scratch data; ModelRobot4 does not use it. */
    private RenderInfo renderdata;
    private int reload_ticker;
    private int was_attacked_ticker;
    private float moveSpeed;

    /** {@code Robot4(World)} (:24-44). */
    public Robot4(final EntityType<? extends Robot4> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.reload_ticker = 0;
        this.was_attacked_ticker = 0;
        this.moveSpeed = 0.34f;
        // setSize(2.5f, 4.0f) (:31) is the entity type's size.
        // PORT: getNavigator().setAvoidsWater(true) (:32) - water is impassable for the path finder, malus -1 (W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 120;
        // fireResistance = 120 (:34) is getFireImmuneTicks; isImmuneToFire (:35) is EntityType.Builder.fireImmune().
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        // PORT: EntityAIMoveThroughVillage(0.9, false) walked through 1.7.10 village doors; the 1.21.1 goal walks towards
        // unvisited village POIs (distance 4, no door handling).
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 0.8999999761581421, false, 4, () -> false));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard precedent).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // PORT: applyEntityAttributes (:46-51) read Robot4_stats during construction; the attribute supplier is built
        // before the config loads (R3), so the runtime values go in here, followed by the full health (Girlfriend).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Robot4_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:46-51) with the config defaults (manifest 170 / 0.34 / 12); see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 170.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.34f)
                .add(Attributes.ATTACK_DAMAGE, 12.0);
    }

    /** {@code entityInit} (:53-68). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_SHIELDING, 0);
    }

    // canDespawn (:70-72) = !isNoDespawnRequired(): the Mob default.

    /** {@code onUpdate} (:74-77). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:79-81). */
    public int mygetMaxHealth() {
        return MobStats.Robot4_stats().health();
    }

    /** {@code getRobot4Health} (:83-85). */
    public int getRobot4Health() {
        return (int) this.getHealth();
    }

    /** {@code getRenderInfo} (:87-89). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:91-100). */
    public void setRenderInfo(final RenderInfo r) {
        this.renderdata.rf1 = r.rf1;
        this.renderdata.rf2 = r.rf2;
        this.renderdata.rf3 = r.rf3;
        this.renderdata.rf4 = r.rf4;
        this.renderdata.ri1 = r.ri1;
        this.renderdata.ri2 = r.ri2;
        this.renderdata.ri3 = r.ri3;
        this.renderdata.ri4 = r.ri4;
    }

    /** {@code getTotalArmorValue} (:102-104). */
    public int getTotalArmorValue() {
        return MobStats.Robot4_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code fireResistance = 120} (:34). */
    @Override
    protected int getFireImmuneTicks() {
        return 120;
    }

    /** {@code jump} (:110-113): the added 0.25 is overwritten by the jump speed {@code super} assigns (Ostrich precedent). */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
        super.jumpFromGround();
    }

    /**
     * {@code onLivingUpdate} (:115-125): client only, smoke behind the head one tick in three and red dust at the
     * cannon shoulder while attacking. {@code reddust} with a zero red component is drawn red, green from the
     * random ({@link LegacyProjectiles#reddust}); the random draws keep the original argument order.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            if (this.random.nextInt(3) == 1) {
                final double px = this.getX() - 1.25 * Math.sin(Math.toRadians(this.getYRot() + 180.0f));
                final double py = this.getY() + 3.0 + this.level().random.nextFloat();
                final double pz = this.getZ() + 1.25 * Math.cos(Math.toRadians(this.getYRot() + 180.0f));
                this.level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0, this.level().random.nextFloat() / 2.0, 0.0);
            }
            if (this.getAttacking() != 0) {
                final double px = this.getX() - 1.55 * Math.sin(Math.toRadians(this.getYRot() + 35.0f));
                final double py = this.getY() + 2.25 + this.level().random.nextFloat();
                final double pz = this.getZ() + 1.55 * Math.cos(Math.toRadians(this.getYRot() + 35.0f));
                final double green = (double) this.level().random.nextFloat();
                this.level().addParticle(LegacyProjectiles.reddust(0.0, green, 0.0), px, py, pz, 0.0, 0.0, 0.0);
            }
        }
    }

    /**
     * {@code getAttackStrength} (:127-139): never called by 1.7.10, and the nesting only ever yields 15 on EASY.
     * Kept for the mapping.
     */
    public int getAttackStrength(final Entity par1Entity) {
        int var2 = 0;
        if (this.level().getDifficulty() == Difficulty.EASY) {
            var2 = 15;
            if (this.level().getDifficulty() == Difficulty.NORMAL) {
                var2 = 20;
            } else if (this.level().getDifficulty() == Difficulty.HARD) {
                var2 = 25;
            }
        }
        return var2;
    }

    /** {@code getLivingSound} (:141-146): one call in four. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ROBOT_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:148-150). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ROBOT_HURT.get();
    }

    /** {@code getDeathSound} (:152-154). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT_DEATH.get();
    }

    /** {@code getSoundVolume} (:156-158). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:160-162). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:164-166, quartz) is never read: dropFewItems below does not call super.

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:178-230): 5-14 stacks of four Laser Balls, one Ray Gun, one item frame, then 10-24
     * redstone rolls. Looting ignored.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var5 = 5 + this.level().random.nextInt(10), var6 = 0; var6 < var5; ++var6) {
            RobotSupport.dropItemRand(this, ModItems.LASER_BALL.get(), 4);
        }
        RobotSupport.dropItemRand(this, ModItems.RAY_GUN.get(), 1);
        RobotSupport.dropItemRand(this, Items.ITEM_FRAME, 1);
        for (int i = 10 + this.level().random.nextInt(15), var6 = 0; var6 < i; ++var6) {
            final int var7 = this.level().random.nextInt(15);
            RobotSupport.dropRedstoneRoll(this, var7);
        }
    }

    // interact (:232-234) returned false: Mob.mobInteract already passes.

    /**
     * {@code attackEntityAsMob} (:236-247): before the blow, a living target is pushed away (2.0 horizontal, 0.12 up,
     * doubled up for a player or a removed entity), then the vanilla mob attack.
     *
     * <p>{@code isDead} is the removed flag; {@code addVelocity} is {@code push}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final double ks = 2.0;
            double inair = 0.12;
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return super.doHurtTarget(par1Entity);
    }

    /** 1.21.1 runs this after the goal selectors and the navigation (Mob.serverAiStep); see {@link #updateAITasks}. */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:249-318). PORT: runs before the move, look and jump controls of the same tick instead of
     * after them (EntityCannonFodder precedent). The shot starts at the left shoulder, {@code rotationYaw + 45}
     * degrees; beyond squared distance 65 it is the exploding special shot with a 30-tick reload.
     */
    protected void updateAITasks() {
        // isDead is the removed flag, not the health.
        if (this.isRemoved()) {
            return;
        }
        if (this.reload_ticker > 0) {
            --this.reload_ticker;
        }
        if (this.was_attacked_ticker > 0) {
            --this.was_attacked_ticker;
        }
        if (this.reload_ticker == 0 && this.level().random.nextInt(8) == 1) {
            LivingEntity e = null;
            if (this.level().random.nextInt(50) == 1) {
                this.setTarget((LivingEntity) null);
            }
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget((LivingEntity) null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < 256.0) {
                    final float reach = 3.0f + e.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(e) < reach * reach) {
                        this.doHurtTarget(e);
                    } else {
                        final double rdd = RobotSupport.headingDifference(this, e, this.getYHeadRot());
                        if (rdd < 0.5) {
                            final double yoff = 2.0;
                            final double xzoff = 1.75;
                            final LaserBall var2 = new LaserBall(this.level(), e.getX() - this.getX(), e.getY() - (this.getY() + yoff), e.getZ() - this.getZ());
                            var2.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() + 45.0f)), this.getY() + yoff,
                                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() + 45.0f)), this.getYRot(), this.getXRot());
                            final double var3 = e.getX() - var2.getX();
                            final double var4 = e.getY() - var2.getY();
                            final double var5 = e.getZ() - var2.getZ();
                            final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                            var2.setThrowableHeading(var3, var4 + var6, var5, 2.0f, 4.0f);
                            if (this.distanceToSqr(e) > 65.0) {
                                var2.setSpecial();
                                this.reload_ticker = 30;
                                RobotSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.5f, 0.5f);
                            } else {
                                this.reload_ticker = 10;
                                RobotSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 2.5f, 1.0f);
                            }
                            this.level().addFreshEntity(var2);
                        }
                        this.setAttacking(1);
                    }
                    this.getNavigation().moveTo(e, 0.75);
                }
            }
        }
        if (this.reload_ticker <= 0 && this.was_attacked_ticker <= 0) {
            this.setAttacking(0);
        }
    }

    /**
     * {@code attackEntityFrom} (:320-339): cactus does nothing; while shielding or within 65 ticks of the last
     * accepted hit nothing lands. Otherwise the lock restarts, the robot attacks, and a {@code Mob} attacker (not a
     * player) becomes the target and makes the call answer {@code true}.
     *
     * <p>PORT: {@code setTarget(Entity)} set the old-AI {@code entityToAttack}; it has no counterpart.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.CACTUS)) {
            return false;
        }
        if (this.getShielding() != 0 || this.was_attacked_ticker != 0) {
            return false;
        }
        this.was_attacked_ticker = 65;
        this.setAttacking(1);
        ret = super.hurt(par1DamageSource, par2);
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Mob) {
            this.setTarget((LivingEntity) e);
            this.getNavigation().moveTo(e, 1.2);
            ret = true;
        }
        return ret;
    }

    /** {@code findSomethingToAttack} (:370-383), box 16/4/16. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return RobotSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 4.0, 16.0);
    }

    /** {@code getAttacking} (:385-387). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:389-391). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getShielding} (:393-395). */
    public final int getShielding() {
        return this.entityData.get(DATA_SHIELDING);
    }

    /** {@code setShielding} (:397-399). Called by the client model only; on the client it changes the local copy. */
    public final void setShielding(final int par1) {
        this.entityData.set(DATA_SHIELDING, par1);
    }

    /**
     * {@code getCanSpawnHere} (:401-434) as the placement predicate: a "Robo-Warrior" spawner allows it; otherwise y
     * at least 50, night, a clear column 1..5 above, valid light.
     *
     * <p>PORT: the spawner is recognised by {@link MobSpawnType#SPAWNER} (wave rule W07) instead of the x/z -3..2,
     * y 0..4 block scan; see {@code Robot2.checkRobot2SpawnRules} for the difference.
     */
    public static boolean checkRobot4SpawnRules(final EntityType<Robot4> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (!RobotSupport.columnIsClear(level, pos, 6)) {
            return false;
        }
        return LegacyLightLevel.isValidLightLevel(level, pos, random);
    }

    /** The whole rule is the placement predicate; the override did not call {@code EntityMob.getCanSpawnHere}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override also dropped {@code EntityLiving}'s collision and liquid test (Ghost precedent). */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
