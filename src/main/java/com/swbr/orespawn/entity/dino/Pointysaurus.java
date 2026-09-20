package com.swbr.orespawn.entity.dino;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Pointysaurus} (Pointysaurus.java:16-297): {@code pointysaurus} (OreSpawnMain.java:4105,
 * tracking 64/1/false). A horned quadruped that hunts only players and hits back at whatever hurt it. {@code EntityMob}
 * is {@link Monster}.
 *
 * <p>Values: health, attack and armor from {@code Pointysaurus_stats} (80/10/16 by default), speed 0.35 re-set every tick
 * (:25, :57), hitbox 2.9 x 2.9 (:27, entity type), {@code experienceValue = 40} (:29, {@code xpReward}),
 * {@code fireResistance = 100} (:30), immune to cactus (:149). DataWatcher 20 {@code attacking} (:49). {@code rt} is not
 * saved.
 *
 * <p>Not carried over: {@code onLivingUpdate} (:73-75), {@code initCreature} (:124-125), {@code isAIEnabled},
 * {@code interact} returning {@code false} (:127-129) and the unused {@code getDropItem} (:100-102).
 */
public class Pointysaurus extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking} (:49, :252-258). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Pointysaurus.class, EntityDataSerializers.INT);

    private final GenericTargetSorter TargetSorter;
    private float moveSpeed;
    @Nullable
    private LivingEntity rt;

    /** {@code Pointysaurus(World)} (:22-38). Goals on both sides, as the 1.7.10 constructor added them. */
    public Pointysaurus(final EntityType<? extends Pointysaurus> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.35f;
        this.rt = null;
        // setSize(2.9f, 2.9f) (:27) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:28) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 40;
        // fireResistance = 100 (:30) is getFireImmuneTicks().
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(this, 1.0, false) (:33) -> MoveThroughVillageGoal over village POIs, see Alosaurus.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) (:37) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard precedent, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        DinoSupport.applyStats(this, MobStats.Pointysaurus_stats());
    }

    /** {@code applyEntityAttributes} (:40-45) with the manifest defaults; the constructor applies the configured stats. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 16.0);
    }

    /** {@code entityInit} (:47-50). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 100} (:30). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code canDespawn} (:52-54): {@code !isNoDespawnRequired()}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:56-59), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:61-63). */
    public int mygetMaxHealth() {
        return MobStats.Pointysaurus_stats().health();
    }

    /** {@code getTotalArmorValue} (:65-67) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Pointysaurus_stats().defense();
    }

    /** {@code getLivingSound} (:77-82). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ALO_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:84-86). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:88-90). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:92-94). */
    @Override
    protected float getSoundVolume() {
        return 0.9f;
    }

    /** {@code getSoundPitch} (:96-98). */
    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code Mob.dropCustomDeathLoot}). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:109-122) through {@code dropItemRand} (:104-107), two blocks up: 10 leather, 6 raw beef,
     * 6 rotten flesh, 6 string.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var4 = 0; var4 < 10; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.LEATHER, 1), 2.0);
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.BEEF, 1), 2.0);
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.ROTTEN_FLESH, 1), 2.0);
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.STRING, 1), 2.0);
        }
    }

    /** {@code attackEntityAsMob} (:131-145): the {@code EntityMob} bite, then 0.8 away and 0.1 up (0.2 for players and removed targets). */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                DinoSupport.legacyKnockback(this, par1Entity, 0.8, 0.1);
            }
            return true;
        }
        return false;
    }

    /** {@code attackEntityFrom} (:147-157): cactus does nothing; otherwise super, and a living source becomes {@code rt}. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
            final Entity e = par1DamageSource.getEntity();
            if (e != null && e instanceof LivingEntity) {
                this.rt = (LivingEntity) e;
            }
        }
        return ret;
    }

    /**
     * {@code updateAITasks} (:159-198): with 1 in 6 (world random), {@code rt} first (forgotten when removed or with 1 in
     * 250, skipped while unseen), else the nearest suitable player. Bite within {@code (4 + width / 2)} with
     * {@code rand(5) == 0 || rand(6) == 1}, else walk at 1.25.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(6) == 0) {
            LivingEntity e = null;
            e = this.rt;
            if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
                e = null;
            }
            if (e != null) {
                if (e.isRemoved() || this.level().random.nextInt(250) == 1) {
                    e = null;
                    this.rt = null;
                }
                if (e != null && !this.getSensing().hasLineOfSight(e)) {
                    e = null;
                }
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (4.0f + e.getBbWidth() / 2.0f) * (4.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(5) == 0 || this.level().random.nextInt(6) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.25);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /** {@code isSuitableTarget} (:200-231): only players not in creative mode. {@code EntityMob} is {@link Monster}. */
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Pointysaurus) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return false;
        }
        if (par1EntityLiving instanceof VelocityRaptor) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player) {
            return !DinoSupport.isCreativePlayer(par1EntityLiving);
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:233-250): {@code PlayNicely} off, every living entity in {@code expand(12, 5, 12)}, sorted. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 5.0, 12.0)));
        var5.sort(this.TargetSorter);
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8 = null;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:252-254). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:256-258). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: the entity-free part of {@code getCanSpawnHere} (:260-296) - spawner, {@code posY >= 50}, night,
     * air in the 2x2 column x/z -1..0 at y+1..5. No neighbour count in this class.
     *
     * <p>PORT: the light roll runs once, in {@link #checkSpawnRules}; the spawner block scan is
     * {@code MobSpawnType.SPAWNER} (see Alosaurus).
     */
    public static boolean checkPointysaurusSpawnRules(final EntityType<Pointysaurus> type, final ServerLevelAccessor level,
                                                      final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        return DinoSupport.airAround(level, pos.getX(), pos.getY(), pos.getZ(), -1, 1, -1, 1, 1, 6);
    }

    /** {@code getCanSpawnHere} (:260-296) on the positioned instance, in the original order. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (reason == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, BlockPos.containing(this.getX(), this.getBoundingBox().minY, this.getZ()),
                this.random)) {
            return false;
        }
        if (this.getY() < 50.0) {
            return false;
        }
        if (HerbivoreSupport.isDaytime(this.level())) {
            return false;
        }
        return DinoSupport.airAround(level, DinoSupport.floor(this.getX()), DinoSupport.floor(this.getY()),
                DinoSupport.floor(this.getZ()), -1, 1, -1, 1, 1, 6);
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
