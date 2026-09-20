package com.swbr.orespawn.entity.robot;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.GiantRobot} ("Jeffery", GiantRobot.java:15-370, verhalten/entity-08.md): the ten
 * blocks tall robot boss that shoots Laser Balls from its head and flings anything within eight blocks.
 *
 * <p>Health, attack and defense from {@code Jeffery_stats} (550/40/18, :45-47, :78-80, :86-88), speed 0.55 set every
 * tick (:27, :73-76), {@code experienceValue} = health / 2 (:30), {@code fireResistance} 40 (:31), fire immune (:32,
 * the entity type), hitbox 3.0 x 9.75 (:28).
 *
 * <p>{@link VirtualHealth} (R4): the config clamp allows up to twice the default, 1100, above the 1.21.1 attribute
 * limit of 1024 (MobStats; W01 hand-off). At the default of 550 the scale is exactly 1.
 *
 * <p>Original behaviour kept (R18): the melee blow has no cooldown of its own (one tick in five while in range);
 * cactus damage is ignored but a {@code Mob} source is still made the target; the {@code +0.25} jump boost is
 * overwritten; Laser Balls never hurt it (in {@code LaserBall}).
 */
public class GiantRobot extends Monster implements LegacyArmor, VirtualHealth {

    /** DataWatcher 20: {@code attacking}, read by the model for the punching pose. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(GiantRobot.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side leg angles of ModelGiantRobot (plain data, no client imports). */
    private RenderGiantRobotInfo renderdata;
    private int reload_ticker;
    private float moveSpeed;

    /** {@code GiantRobot(World)} (:22-41). */
    public GiantRobot(final EntityType<? extends GiantRobot> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderGiantRobotInfo();
        this.reload_ticker = 0;
        this.moveSpeed = 0.55f;
        // setSize(3.0f, 9.75f) (:28) is the entity type's size.
        // PORT: getNavigator().setAvoidsWater(true) (:29) - water is impassable for the path finder, malus -1 (W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = MobStats.Jeffery_stats().health() / 2;
        // fireResistance = 40 (:31) is getFireImmuneTicks; isImmuneToFire (:32) is EntityType.Builder.fireImmune().
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderGiantRobotInfo();
        // entityInit -> initLegData (:50-67), which ran before the field initialisers in 1.7.10.
        this.initLegData();
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
        // PORT: applyEntityAttributes (:43-48) read Jeffery_stats during construction; the attribute supplier is built
        // before the config loads (R3), so the runtime values go in here, followed by the full health (Girlfriend).
        // MAX_HEALTH takes the R4 clamp of the original value.
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.mygetMaxHealth()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Jeffery_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:43-48) with the config defaults (manifest 550 / 0.55 / 40); see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(550.0))
                .add(Attributes.MOVEMENT_SPEED, (double) 0.55f)
                .add(Attributes.ATTACK_DAMAGE, 40.0);
    }

    /** {@code entityInit} (:50-54). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code initLegData} (:56-67). */
    private void initLegData() {
        if (this.renderdata == null) {
            this.renderdata = new RenderGiantRobotInfo();
        }
        this.renderdata.hipydisplayangle = 0.0f;
        this.renderdata.hipxdisplayangle = 0.0f;
        this.renderdata.gpcounter = 2000000;
        this.renderdata.thighdisplayangle[0] = 0.0f;
        this.renderdata.thighdisplayangle[1] = 0.0f;
        this.renderdata.shindisplayangle[0] = 0.0f;
        this.renderdata.shindisplayangle[1] = 0.0f;
    }

    // canDespawn (:69-71) = !isNoDespawnRequired(): the Mob default - the boss despawns.

    /** {@code onUpdate} (:73-76). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:78-80), in original units. */
    public int mygetMaxHealth() {
        return MobStats.Jeffery_stats().health();
    }

    /** R4: the original maximum, {@code Jeffery_stats.health}. */
    @Override
    public double getOriginalMaxHealth() {
        return this.mygetMaxHealth();
    }

    /** {@code getRenderGiantRobotInfo} (:82-84). */
    public RenderGiantRobotInfo getRenderGiantRobotInfo() {
        return this.renderdata;
    }

    /** {@code getTotalArmorValue} (:86-88). */
    public int getTotalArmorValue() {
        return MobStats.Jeffery_stats().defense();
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

    // onLivingUpdate (:94-96) only called super.

    /** {@code jump} (:98-101): the added 0.25 is overwritten by the jump speed {@code super} assigns (Ostrich precedent). */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
        super.jumpFromGround();
    }

    /** {@code getLivingSound} (:103-108): one call in four. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ROBOT_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:110-112). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ROBOT_HURT.get();
    }

    /** {@code getDeathSound} (:114-116). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT_DEATH.get();
    }

    /** {@code getSoundVolume} (:118-120). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:122-124). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:126-128, iron ingot) is never read: dropFewItems below does not call super.

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:140-190): 15-29 stacks of four Laser Balls, then 10-19 rolls of {@code rand(12)}:
     * Spider Robot Kit, Ant Robot Kit, Ray Gun, redstone block, dispenser, sticky piston, piston, lever, iron block,
     * detector rail, nothing, nothing. Looting ignored.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var5 = 15 + this.level().random.nextInt(15), var6 = 0; var6 < var5; ++var6) {
            RobotSupport.dropItemRand(this, ModItems.LASER_BALL.get(), 4);
        }
        for (int i = 10 + this.level().random.nextInt(10), var6 = 0; var6 < i; ++var6) {
            final int var7 = this.level().random.nextInt(12);
            switch (var7) {
                case 0 -> RobotSupport.dropItemRand(this, ModItems.SPIDER_ROBOT_KIT.get(), 1); // :149
                case 1 -> RobotSupport.dropItemRand(this, ModItems.ANT_ROBOT_KIT.get(), 1); // :153
                case 2 -> RobotSupport.dropItemRand(this, ModItems.RAY_GUN.get(), 1);
                case 3 -> RobotSupport.dropItemRand(this, Blocks.REDSTONE_BLOCK, 1);
                case 4 -> RobotSupport.dropItemRand(this, Blocks.DISPENSER, 1);
                case 5 -> RobotSupport.dropItemRand(this, Blocks.STICKY_PISTON, 1);
                case 6 -> RobotSupport.dropItemRand(this, Blocks.PISTON, 1);
                case 7 -> RobotSupport.dropItemRand(this, Blocks.LEVER, 1);
                case 8 -> RobotSupport.dropItemRand(this, Blocks.IRON_BLOCK, 1);
                case 9 -> RobotSupport.dropItemRand(this, Blocks.DETECTOR_RAIL, 1);
                default -> {
                }
            }
        }
    }

    // interact (:192-194) returned false: Mob.mobInteract already passes.

    /**
     * {@code attackEntityAsMob} (:196-210): the vanilla mob attack; if it lands, a living target is flung away (2.2
     * horizontal, 0.25 up, doubled up for a player or a removed entity).
     *
     * <p>{@code isDead} is the removed flag; {@code addVelocity} is {@code push}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 2.2;
        double inair = 0.25;
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
                if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                    inair *= 2.0;
                }
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            return true;
        }
        return false;
    }

    /** 1.21.1 runs this after the goal selectors and the navigation (Mob.serverAiStep); see {@link #updateAITasks}. */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:212-284). PORT: runs before the move, look and jump controls of the same tick instead of
     * after them (EntityCannonFodder precedent). One tick in five: shoot from ten blocks up when reloaded (special
     * shot beyond squared distance 100), and punch within eight blocks plus half the target's width.
     */
    protected void updateAITasks() {
        // isDead is the removed flag, not the health.
        if (this.isRemoved()) {
            return;
        }
        if (this.reload_ticker > 0) {
            --this.reload_ticker;
        }
        if (this.level().random.nextInt(5) == 0) {
            LivingEntity e = null;
            if (this.level().random.nextInt(100) == 1) {
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
                    final double rdd = RobotSupport.headingDifference(this, e, this.getYHeadRot());
                    if (rdd < 0.5) {
                        if (this.reload_ticker == 0) {
                            final double yoff = 10.0;
                            final double xzoff = 3.75;
                            final LaserBall var2 = new LaserBall(this.level(), e.getX() - this.getX(), e.getY() - (this.getY() + yoff), e.getZ() - this.getZ());
                            var2.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + yoff,
                                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYHeadRot())), this.getYHeadRot(), this.getXRot());
                            final double var3 = e.getX() - var2.getX();
                            final double var4 = e.getY() - var2.getY();
                            final double var5 = e.getZ() - var2.getZ();
                            final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                            var2.setThrowableHeading(var3, var4 + var6, var5, 2.0f, 4.0f);
                            if (this.distanceToSqr(e) > 100.0) {
                                var2.setSpecial();
                                this.reload_ticker = 25;
                                RobotSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 3.5f, 0.5f);
                            } else {
                                this.reload_ticker = 10;
                                RobotSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 2.5f, 1.0f);
                            }
                            this.level().addFreshEntity(var2);
                        }
                        final float reach = 8.0f + e.getBbWidth() / 2.0f;
                        if (this.distanceToSqr(e) < reach * reach) {
                            this.setAttacking(1);
                            this.doHurtTarget(e);
                        } else {
                            this.setAttacking(0);
                        }
                    }
                    this.getNavigation().moveTo(e, 0.5);
                } else {
                    this.setAttacking(0);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * {@code attackEntityFrom} (:286-297): cactus does nothing, but a {@code Mob} source becomes the target either
     * way - a player never does here, only through {@code HurtByTargetGoal}.
     *
     * <p>PORT: {@code setTarget(Entity)} set the old-AI {@code entityToAttack}; it has no counterpart.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Mob) {
            this.setTarget((LivingEntity) e);
        }
        return ret;
    }

    /** {@code findSomethingToAttack} (:328-341), box 16/12/16. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return RobotSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 12.0, 16.0);
    }

    /** {@code getAttacking} (:343-345). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:347-349). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * {@code getCanSpawnHere} (:351-369) as the placement predicate: y at least 50, night, a clear column 1..5 above,
     * valid light. No spawner branch.
     */
    public static boolean checkGiantRobotSpawnRules(final EntityType<GiantRobot> type, final ServerLevelAccessor level,
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
