package com.swbr.orespawn.entity.robot;

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
 * Port of {@code danger.orespawn.Robot5} ("Robo-Sniper", Robot5.java:16-335, verhalten/entity-11.md): the small
 * wheeled gun that shoots a Laser Ball every 20 ticks at targets up to 30 blocks away and keeps about six blocks of
 * distance.
 *
 * <p>Health, attack and defense from {@code Robot5_stats} (20/5/6, :43-45, :62-64, :66-68; the attack is unused),
 * speed 0.3 set every tick (:26, :57-60), {@code experienceValue} 20 (:29), {@code fireResistance} 40 (:30), fire
 * immune (:31, the entity type), hitbox 1.0 x 2.25 (:27). Sounds at volume 0.5 (:98-100).
 *
 * <p>Original behaviour kept (R18): the reload is reset to 20 even without a target; the {@code +0.25} jump boost is
 * overwritten; Laser Balls never hurt it (in {@code LaserBall}). Unlike its siblings it has no {@code RenderInfo}.
 */
public class Robot5 extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by nothing but kept like the original (:50). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Robot5.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    private int reload_ticker;
    private float moveSpeed;

    /** {@code Robot5(World)} (:22-39). */
    public Robot5(final EntityType<? extends Robot5> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.reload_ticker = 0;
        this.moveSpeed = 0.3f;
        // setSize(1.0f, 2.25f) (:27) is the entity type's size.
        // PORT: getNavigator().setAvoidsWater(true) (:28) - water is impassable for the path finder, malus -1 (W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 20;
        // fireResistance = 40 (:30) is getFireImmuneTicks; isImmuneToFire (:31) is EntityType.Builder.fireImmune().
        this.TargetSorter = new GenericTargetSorter(this);
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
        // PORT: applyEntityAttributes (:41-46) read Robot5_stats during construction; the attribute supplier is built
        // before the config loads (R3), so the runtime values go in here, followed by the full health (Girlfriend).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Robot5_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:41-46) with the config defaults (manifest 20 / 0.3 / 5); see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    /** {@code entityInit} (:48-51). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    // canDespawn (:53-55) = !isNoDespawnRequired(): the Mob default.

    /** {@code onUpdate} (:57-60). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:62-64). */
    public int mygetMaxHealth() {
        return MobStats.Robot5_stats().health();
    }

    /** {@code getTotalArmorValue} (:66-68). */
    public int getTotalArmorValue() {
        return MobStats.Robot5_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code fireResistance = 40} (:30). */
    @Override
    protected int getFireImmuneTicks() {
        return 40;
    }

    // onLivingUpdate (:74-76) only called super.

    /** {@code jump} (:78-81): the added 0.25 is overwritten by the jump speed {@code super} assigns (Ostrich precedent). */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
        super.jumpFromGround();
    }

    /** {@code getLivingSound} (:83-88): one call in four. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ROBOT_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:90-92). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ROBOT_HURT.get();
    }

    /** {@code getDeathSound} (:94-96). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT_DEATH.get();
    }

    /** {@code getSoundVolume} (:98-100): half volume. */
    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    /** {@code getSoundPitch} (:102-104). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:106-108, iron ingot) is never read: dropFewItems below does not call super.

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:120-170): 5-10 stacks of four Laser Balls, then 2-6 redstone rolls. Looting ignored. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var5 = 5 + this.level().random.nextInt(6), var6 = 0; var6 < var5; ++var6) {
            RobotSupport.dropItemRand(this, ModItems.LASER_BALL.get(), 4);
        }
        for (int i = 2 + this.level().random.nextInt(5), var6 = 0; var6 < i; ++var6) {
            final int var7 = this.level().random.nextInt(15);
            RobotSupport.dropRedstoneRoll(this, var7);
        }
    }

    // interact (:172-174) returned false; attackEntityAsMob (:176-178) only called super.

    /** 1.21.1 runs this after the goal selectors and the navigation (Mob.serverAiStep); see {@link #updateAITasks}. */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:180-239). PORT: runs before the move, look and jump controls of the same tick instead of
     * after them (EntityCannonFodder precedent). Walks at the target only beyond squared distance 36.
     */
    protected void updateAITasks() {
        // isDead is the removed flag, not the health.
        if (this.isRemoved()) {
            return;
        }
        if (this.reload_ticker > 0) {
            --this.reload_ticker;
            if (this.reload_ticker < 15) {
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
            this.reload_ticker = 20;
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < 900.0) {
                    final double rdd = RobotSupport.headingDifference(this, e, this.getYHeadRot());
                    if (rdd < 0.5) {
                        final double yoff = 1.6;
                        final double xzoff = 1.6;
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
                    if (this.distanceToSqr(e) > 36.0) {
                        this.getNavigation().moveTo(e, 0.5);
                    }
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /** {@code attackEntityFrom} (:241-247): cactus does nothing. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /** {@code findSomethingToAttack} (:278-291), box 30/6/30. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return RobotSupport.findSomethingToAttack(this, this.TargetSorter, 30.0, 6.0, 30.0);
    }

    /** {@code getAttacking} (:293-295). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:297-299). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * {@code getCanSpawnHere} (:301-334) as the placement predicate: a "Robo-Sniper" spawner allows it; otherwise y
     * at least 50, night, a clear column 1..2 above, valid light.
     *
     * <p>PORT: the spawner is recognised by {@link MobSpawnType#SPAWNER} (wave rule W07) instead of the x/z -3..2,
     * y 0..4 block scan; see {@code Robot2.checkRobot2SpawnRules} for the difference.
     */
    public static boolean checkRobot5SpawnRules(final EntityType<Robot5> type, final ServerLevelAccessor level,
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
        if (!RobotSupport.columnIsClear(level, pos, 3)) {
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
