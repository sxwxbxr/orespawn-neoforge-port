package com.swbr.orespawn.entity.robot;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Robot1} ("Bomb-Omb", Robot1.java:13-218, verhalten/entity-11.md): the small
 * wind-up robot that walks up to anything alive and blows itself up.
 *
 * <p>Health fixed 5 (:71-73), armour fixed 2 (:90-92), attack 4 never used - there is no melee task (:43),
 * speed 0.2 set every tick (:23, :66-69), {@code experienceValue} 5 (:26, an {@code EntityMob}, so it counts),
 * {@code fireResistance} 5 (:27), fire immune (:28, the entity type), hitbox 0.5 x 0.5 (:24).
 *
 * <p>Original behaviour kept (R18): the target search and the particles run on both sides; the explosion ends in
 * {@code setDead}, which is no death - no drops, no experience. Gunpowder drops only when it is really killed.
 */
public class Robot1 extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, defined but never set (:48, :207-213). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Robot1.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side scratch data (RenderInfo is a plain data class, GhostSkelly precedent); ModelRobot1 ignores it. */
    private RenderInfo renderdata;
    private float moveSpeed;

    /** {@code Robot1(World)} (:19-37). */
    public Robot1(final EntityType<? extends Robot1> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.moveSpeed = 0.2f;
        // setSize(0.5f, 0.5f) (:24) is the entity type's size.
        // PORT: getNavigator().setAvoidsWater(true) (:25) - water is impassable for the path finder, malus -1 (W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 5;
        // fireResistance = 5 (:27) is getFireImmuneTicks; isImmuneToFire (:28) is EntityType.Builder.fireImmune().
        this.TargetSorter = new GenericTargetSorter(this);
        // entityInit (:46-60) zeroed the render data; a new RenderInfo is all zero.
        this.renderdata = new RenderInfo();
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 10, 1.0));
        // PORT: EntityAIMoveThroughVillage(0.9, false) walked through the doors of a 1.7.10 village; the 1.21.1 goal
        // walks towards unvisited village POIs (distance 4, no door handling) - there is no door list to port.
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 0.8999999761581421, false, 4, () -> false));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not
        // seen for 60 ticks (Lizard precedent).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:39-44): health 5, speed 0.2, attack 4.0 - all fixed. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.2f)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    /** {@code entityInit} (:46-60). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    // canDespawn (:62-64) = !isNoDespawnRequired(): the Mob default (removeWhenFarAway true, persistence honoured).

    /** {@code onUpdate} (:66-69). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:71-73). */
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code getRenderInfo} (:75-77). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:79-88). */
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

    /** {@code getTotalArmorValue} (:90-92). */
    public int getTotalArmorValue() {
        return 2;
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code fireResistance = 5} (:27). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /**
     * {@code onLivingUpdate} (:98-114): after the vanilla living update, one tick in eight looks for a target; within
     * squared distance 5 the server explodes (strength 2.5, mobGriefing) one time in eighteen and removes the robot.
     * Smoke and lava particles and the path follow every find.
     *
     * <p>PORT: {@code tryMoveToEntityLiving} runs on the server only. On the 1.7.10 client it computed a path that
     * the client never followed (navigation is ticked by the server AI only); skipping it changes nothing visible
     * and no random draw.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().random.nextInt(8) == 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                if (this.distanceToSqr(e) < 5.0 && !this.level().isClientSide && this.level().random.nextInt(18) == 1) {
                    // createExplosion(this, ..., 2.5f, mobGriefing): ExplosionInteraction.MOB asks the same rule.
                    this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.5f, Level.ExplosionInteraction.MOB);
                    // setDead(): removed without dying - no drops, no experience.
                    this.discard();
                }
                for (int i = 0; i < 2; ++i) {
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 0.0, 0.0, 0.0);
                    this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY() + 1.0, this.getZ(), 0.0, 0.0, 0.0);
                }
                if (!this.level().isClientSide) {
                    this.getNavigation().moveTo(e, 1.2);
                }
            }
        }
    }

    /** {@code getLivingSound} (:116-118). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KYUUBI_LIVING.get();
    }

    /** {@code getHurtSound} (:120-122). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SCORPION_HIT.get();
    }

    /** {@code getDeathSound} (:124-126). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT1_DEATH.get();
    }

    /** {@code getSoundVolume} (:128-130). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:132-134). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code getDropItem} (:136-138) through the inherited 1.7.10 {@code EntityLiving.dropFewItems}: {@code rand(3)}
     * gunpowder plus {@code rand(looting + 1)} (R10, Java drops).
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CannonFodderSupport.vanillaDropFewItems(this, Items.GUNPOWDER, CannonFodderSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    // interact (:140-142) returned false: Mob.mobInteract already passes. attackEntityAsMob (:144-146) only called
    // super; updateAITasks (:148-153) only skipped a dead robot, which no longer ticks.

    /** {@code attackEntityFrom} (:155-161): cactus does nothing. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /** {@code findSomethingToAttack} (:192-205), box 8/3/8. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return RobotSupport.findSomethingToAttack(this, this.TargetSorter, 8.0, 3.0, 8.0);
    }

    /** {@code getAttacking} (:207-209). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:211-213). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * {@code getCanSpawnHere} (:215-217) as the placement predicate: y at least 50, valid light, night - in this
     * order, so the light rolls happen even by day.
     */
    public static boolean checkRobot1SpawnRules(final EntityType<Robot1> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && LegacyLightLevel.isValidLightLevel(level, pos, random) && !InsectSupport.isDaytime(level.getLevel());
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
