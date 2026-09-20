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
 * Port of {@code danger.orespawn.Alosaurus} (Alosaurus.java:15-261): {@code alosaurus}, a hostile dinosaur that bites
 * almost anything alive ("Alosaurus", OreSpawnMain.java:3391, tracking 64/1/false). {@code EntityMob} is
 * {@link Monster}.
 *
 * <p>Values: health, attack and armor from {@code Alosaurus_stats} (110/18/8 by default), speed 0.35 re-set every tick
 * (:23, :54), hitbox 1.9 x 3.6 (:24, entity type), {@code experienceValue = 40} (:26) - {@code EntityMob} reads it, so
 * it is {@code xpReward} - and {@code fireResistance = 100} (:27). DataWatcher 20 {@code attacking} (:46) drives the
 * jaw in the model. No NBT of its own.
 *
 * <p>Not carried over: {@code onLivingUpdate} (:70-72) and {@code initCreature} (:115-116) only call super or nothing;
 * {@code isAIEnabled} (every 1.21.1 mob runs goals); {@code interact} returning {@code false} (:118-120) is
 * {@code Mob.mobInteract}'s {@code PASS}; {@code getDropItem} (:97-99) is unused because {@code dropFewItems} is
 * overridden.
 */
public class Alosaurus extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking} (:46, :214-220). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Alosaurus.class, EntityDataSerializers.INT);

    private final GenericTargetSorter TargetSorter;
    private float moveSpeed;

    /** {@code Alosaurus(World)} (:20-35). Goals are added on both sides, as the 1.7.10 constructor did (W04 precedent). */
    public Alosaurus(final EntityType<? extends Alosaurus> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.35f;
        // setSize(1.9f, 3.6f) (:24) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:25) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 40;
        // fireResistance = 100 (:27) is getFireImmuneTicks().
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(this, 1.0, false) walked through the doors of a 1.7.10 village the mob stood
        // in. 1.21.1 has no door list; MoveThroughVillageGoal is its successor over village POIs (only near a village,
        // not night-only, no door breaking). No shared 1.7.10 port of the goal exists in entity.ai yet.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not
        // seen for 60 ticks (Lizard precedent, W06). Nothing reads the target: the bite runs in customServerAiStep.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        DinoSupport.applyStats(this, MobStats.Alosaurus_stats());
    }

    /**
     * {@code applyEntityAttributes} (:37-42) with the manifest defaults; the constructor writes the configured
     * {@code Alosaurus_stats} over them ({@link DinoSupport#applyStats}).
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 110.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.ARMOR, 8.0);
    }

    /** {@code entityInit} (:44-47). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 100} (:27). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /**
     * {@code canDespawn} (:49-51): {@code !isNoDespawnRequired()}. 1.21.1's {@code Mob.checkDespawn} asks
     * {@code isPersistenceRequired()} before this method, so the inherited {@code true} is the same rule.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:53-56), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:58-60). */
    public int mygetMaxHealth() {
        return MobStats.Alosaurus_stats().health();
    }

    /** {@code getTotalArmorValue} (:62-64) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Alosaurus_stats().defense();
    }

    /** {@code getLivingSound} (:74-79). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ALO_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:81-83). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:85-87). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:89-91). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:93-95). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code Mob.dropCustomDeathLoot}). Both arguments
     * of the override below are unused.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:106-113): 10 gold nuggets and 6 raw beef, each through {@code dropItemRand} (:101-104). */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var4 = 0; var4 < 10; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.GOLD_NUGGET, 1), 1.0);
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.BEEF, 1), 1.0);
        }
    }

    /**
     * {@code attackEntityAsMob} (:122-136): the {@code EntityMob} bite ({@code Mob.doHurtTarget}: attack attribute,
     * enchantment damage, knockback and post-attack effects), then a push of 1.2 away and 0.1 up (0.2 for a player or a
     * removed target).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                DinoSupport.legacyKnockback(this, par1Entity, 1.2, 0.1);
            }
            return true;
        }
        return false;
    }

    /**
     * {@code updateAITasks} (:138-161): with 1 in 5 per tick on the world random, the nearest suitable target is faced
     * (10 degrees each way) and bitten when it is within {@code (4 + width / 2)}; otherwise the dinosaur walks to it at
     * 1.25. {@code attacking} drops to 0 only when no target is found.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(5) == 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (4.0f + e.getBbWidth() / 2.0f) * (4.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
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

    /** {@code isSuitableTarget} (:163-194). */
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
        if (par1EntityLiving instanceof Alosaurus) {
            return false;
        }
        if (par1EntityLiving instanceof Cryolophosaurus) {
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
        return true;
    }

    /** {@code findSomethingToAttack} (:196-212): {@code PlayNicely} off, every living entity in {@code expand(12, 5, 12)}, sorted. */
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

    /** {@code getAttacking} (:214-216). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:218-220). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate for {@code RegisterSpawnPlacementsEvent}, the part of {@code getCanSpawnHere} (:222-260) that needs
     * no entity: spawner, {@code posY >= 50}, night, air in the 2x2 column x/z -1..0 at y+1..5, no other Alosaurus in
     * {@code expand(16, 8, 16)} of the type's box at the block centre.
     *
     * <p>PORT: the light roll ({@code isValidLightLevel}, :238) runs once, in {@link #checkSpawnRules}; 1.21.1 asks the
     * placement predicate and then the instance, and rolling in both would square the chance. PORT: the spawner branch
     * (:223-237, a block scan for a spawner naming "Alosaurus" in x/z -3..2, y 0..4) is {@code MobSpawnType.SPAWNER}
     * (catalogue 5.9): a spawner spawn is always allowed, a natural spawn next to such a spawner no longer is.
     */
    public static boolean checkAlosaurusSpawnRules(final EntityType<Alosaurus> type, final ServerLevelAccessor level,
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
        if (!DinoSupport.airAround(level, pos.getX(), pos.getY(), pos.getZ(), -1, 1, -1, 1, 1, 6)) {
            return false;
        }
        return level.getEntitiesOfClass(Alosaurus.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 8.0, 16.0)).isEmpty();
    }

    /** {@code getCanSpawnHere} (:222-260) on the positioned instance, in the original order. */
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
        if (!DinoSupport.airAround(level, DinoSupport.floor(this.getX()), DinoSupport.floor(this.getY()),
                DinoSupport.floor(this.getZ()), -1, 1, -1, 1, 1, 6)) {
            return false;
        }
        // findNearestEntityWithinAABB(Alosaurus.class, expand(16, 8, 16), this) skips the asking entity.
        return level.getEntitiesOfClass(Alosaurus.class, this.getBoundingBox().inflate(16.0, 8.0, 16.0),
                other -> other != this).isEmpty();
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
