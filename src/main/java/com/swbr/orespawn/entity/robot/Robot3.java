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
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Robot3} ("Robo-Gunner", Robot3.java:15-346, verhalten/entity-11.md): the
 * artillery robot that fires a Laser Ball every 35 ticks at anything within 16 blocks it is facing.
 *
 * <p>Health, attack and defense from {@code Robot3_stats} (80/16/14, :45-47, :75-77, :94-96; the attack is used by
 * no code path), speed 0.35 set every tick (:27, :70-73), {@code experienceValue} 60 (:30), {@code fireResistance}
 * 40 (:31), fire immune (:32, the entity type), hitbox 2.5 x 5.0 (:28).
 *
 * <p>Original behaviour kept (R18): the reload is reset to 35 even without a target; there is no melee; the
 * {@code +0.25} jump boost is overwritten; the shot spreads with the 1.7.10 Gaussian of
 * {@link LaserBall#setThrowableHeading}; Laser Balls never hurt it (in {@code LaserBall}).
 */
public class Robot3 extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by the model for the arm pumping. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Robot3.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side scratch data of ModelRobot3 ({@code ri1} = arms pumping). */
    private RenderInfo renderdata;
    private int reload_ticker;
    private float moveSpeed;

    /** {@code Robot3(World)} (:22-41). */
    public Robot3(final EntityType<? extends Robot3> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.reload_ticker = 0;
        this.moveSpeed = 0.35f;
        // setSize(2.5f, 5.0f) (:28) is the entity type's size.
        // PORT: getNavigator().setAvoidsWater(true) (:29) - water is impassable for the path finder, malus -1 (W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 60;
        // fireResistance = 40 (:31) is getFireImmuneTicks; isImmuneToFire (:32) is EntityType.Builder.fireImmune().
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
        // PORT: applyEntityAttributes (:43-48) read Robot3_stats during construction; the attribute supplier is built
        // before the config loads (R3), so the runtime values go in here, followed by the full health (Girlfriend).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Robot3_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:43-48) with the config defaults (manifest 80 / 0.35 / 16); see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, 16.0);
    }

    /** {@code entityInit} (:50-64). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    // canDespawn (:66-68) = !isNoDespawnRequired(): the Mob default.

    /** {@code onUpdate} (:70-73). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:75-77). */
    public int mygetMaxHealth() {
        return MobStats.Robot3_stats().health();
    }

    /** {@code getRenderInfo} (:79-81). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:83-92). */
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

    /** {@code getTotalArmorValue} (:94-96). */
    public int getTotalArmorValue() {
        return MobStats.Robot3_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code fireResistance = 40} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 40;
    }

    // onLivingUpdate (:102-104) only called super.

    /** {@code jump} (:106-109): the added 0.25 is overwritten by the jump speed {@code super} assigns (Ostrich precedent). */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
        super.jumpFromGround();
    }

    /** {@code getLivingSound} (:111-116): one call in four. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ROBOT_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:118-120). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ROBOT_HURT.get();
    }

    /** {@code getDeathSound} (:122-124). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT_DEATH.get();
    }

    /** {@code getSoundVolume} (:126-128). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:130-132). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:134-136, iron ingot) is never read: dropFewItems below does not call super.

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:148-198): 5-10 stacks of four Laser Balls, then 5-14 redstone rolls. Looting ignored. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var5 = 5 + this.level().random.nextInt(6), var6 = 0; var6 < var5; ++var6) {
            RobotSupport.dropItemRand(this, ModItems.LASER_BALL.get(), 4);
        }
        for (int i = 5 + this.level().random.nextInt(10), var6 = 0; var6 < i; ++var6) {
            final int var7 = this.level().random.nextInt(15);
            RobotSupport.dropRedstoneRoll(this, var7);
        }
    }

    // interact (:200-202) returned false; attackEntityAsMob (:204-206) only called super.

    /** 1.21.1 runs this after the goal selectors and the navigation (Mob.serverAiStep); see {@link #updateAITasks}. */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:208-265). PORT: runs before the move, look and jump controls of the same tick instead of
     * after them (EntityCannonFodder precedent). {@code faceEntity} is {@code lookAt}, {@code rotationYawHead} is
     * {@code getYHeadRot()}, {@code setLocationAndAngles} is {@code moveTo}, {@code MathHelper.sqrt_double} is
     * {@code (float) Math.sqrt}.
     */
    protected void updateAITasks() {
        // isDead is the removed flag, not the health.
        if (this.isRemoved()) {
            return;
        }
        if (this.reload_ticker > 0) {
            --this.reload_ticker;
            if (this.reload_ticker < 25) {
                this.setAttacking(0);
            }
        }
        if (this.reload_ticker == 0) {
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
            this.reload_ticker = 35;
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < 256.0) {
                    final double rdd = RobotSupport.headingDifference(this, e, this.getYHeadRot());
                    if (rdd < 0.5) {
                        final double yoff = 3.0;
                        final double xzoff = 1.75;
                        final LaserBall var2 = new LaserBall(this.level(), e.getX() - this.getX(), e.getY() - (this.getY() + yoff), e.getZ() - this.getZ());
                        var2.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + yoff,
                                this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYHeadRot())), this.getYHeadRot(), this.getXRot());
                        final double var3 = e.getX() - var2.getX();
                        final double var4 = e.getY() - var2.getY();
                        final double var5 = e.getZ() - var2.getZ();
                        final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                        var2.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 5.0f);
                        // "fireworks.launch"
                        RobotSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.0f, 1.0f);
                        this.level().addFreshEntity(var2);
                        this.setAttacking(1);
                    }
                    this.getNavigation().moveTo(e, 0.5);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /** {@code attackEntityFrom} (:267-273): cactus does nothing. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /** {@code findSomethingToAttack} (:304-317), box 16/3/16. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return RobotSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 3.0, 16.0);
    }

    /** {@code getAttacking} (:319-321). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:323-325). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * {@code getCanSpawnHere} (:327-345) as the placement predicate: y at least 50, night, a clear column 1..5 above,
     * valid light. Unlike Robot2, Robot4 and Robot5 there is no spawner branch, so a spawner spawn is tested too.
     */
    public static boolean checkRobot3SpawnRules(final EntityType<Robot3> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
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
