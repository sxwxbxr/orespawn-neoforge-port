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
import com.swbr.orespawn.registry.ModItems;
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
 * Port of {@code danger.orespawn.TRex} (TRex.java:16-298): {@code t_rex}, "T. Rex" (OreSpawnMain.java:3925, tracking
 * 64/1/false). {@code EntityMob} is {@link Monster}.
 *
 * <p>Values: health, attack and armor from {@code TRex_stats} (160/22/14 by default), speed 0.38 re-set every tick
 * (:25, :57), hitbox 2.0 x 4.2 (:27, entity type), {@code experienceValue = 150} (:29, {@code xpReward}),
 * {@code fireResistance = 100} (:30), immune to cactus (:146). DataWatcher 20 {@code attacking} (:49). The revenge
 * target {@code rt} (:20) is not saved, as in the original.
 *
 * <p>Not carried over: {@code onLivingUpdate} (:73-75), {@code initCreature} (:121-122), {@code isAIEnabled},
 * {@code interact} returning {@code false} (:124-126) and the unused {@code getDropItem} (:100-102).
 */
public class TRex extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking} (:49, :251-257). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(TRex.class, EntityDataSerializers.INT);

    private final GenericTargetSorter TargetSorter;
    private float moveSpeed;
    @Nullable
    private LivingEntity rt;

    /** {@code TRex(World)} (:22-38). Goals on both sides, as the 1.7.10 constructor added them. */
    public TRex(final EntityType<? extends TRex> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.38f;
        this.rt = null;
        // setSize(2.0f, 4.2f) (:27) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:28) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 150;
        // fireResistance = 100 (:30) is getFireImmuneTicks().
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(this, 1.0, false) (:33) -> MoveThroughVillageGoal over village POIs, see Alosaurus.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) (:37) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard precedent, W06). The bite reads rt, not this target.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        DinoSupport.applyStats(this, MobStats.TRex_stats());
    }

    /** {@code applyEntityAttributes} (:40-45) with the manifest defaults; the constructor applies the configured stats. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 160.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.38f)
                .add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.ARMOR, 14.0);
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
        return MobStats.TRex_stats().health();
    }

    /** {@code getTotalArmorValue} (:65-67) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.TRex_stats().defense();
    }

    /** {@code getLivingSound} (:77-82). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.TREX_LIVING.get();
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
        return ModSounds.TREX_DEATH.get();
    }

    /** {@code getSoundVolume} (:92-94). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:96-98). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code Mob.dropCustomDeathLoot}). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:109-119) through {@code dropItemRand} (:104-107): a T. Rex tooth, an item frame, 7 raw beef,
     * then {@code 2 + rand(4)} pairs of uranium and titanium nuggets (world random).
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        DinoSupport.dropItemRand(this, new ItemStack(ModItems.TREX_TOOTH.get(), 1), 1.0);
        DinoSupport.dropItemRand(this, new ItemStack(Items.ITEM_FRAME, 1), 1.0);
        for (int var4 = 0; var4 < 7; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.BEEF, 1), 1.0);
        }
        for (int var4 = 2 + this.level().random.nextInt(4), i = 0; i < var4; ++i) {
            DinoSupport.dropItemRand(this, new ItemStack(ModItems.URANIUM_NUGGET.get(), 1), 1.0);
            DinoSupport.dropItemRand(this, new ItemStack(ModItems.TITANIUM_NUGGET.get(), 1), 1.0);
        }
    }

    /** {@code attackEntityAsMob} (:128-142): the {@code EntityMob} bite, then 1.2 away and 0.1 up (0.2 for players and removed targets). */
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
     * {@code attackEntityFrom} (:144-154): cactus does nothing; any other hit goes to super and a living source becomes
     * {@code rt}, whether the hit landed or not. Runs on the client too, like the original.
     */
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
     * {@code updateAITasks} (:156-195): with 1 in 5 (world random) the target is {@code rt} unless {@code PlayNicely};
     * {@code rt} is dropped when it is removed or with 1 in 200, and skipped for this round when out of sight; otherwise
     * the nearest suitable target. Face it, bite within {@code (4 + width / 2)} with {@code rand(4) == 0 || rand(5) == 1},
     * else walk at 1.25; no target clears {@code attacking}.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(5) == 1) {
            LivingEntity e = null;
            e = this.rt;
            if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
                e = null;
            }
            if (e != null) {
                if (e.isRemoved() || this.level().random.nextInt(200) == 1) {
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

    /** {@code isSuitableTarget} (:197-230). */
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
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof TRex) {
            return false;
        }
        if (par1EntityLiving instanceof Cryolophosaurus) {
            return false;
        }
        if (par1EntityLiving instanceof VelocityRaptor) {
            return false;
        }
        if (par1EntityLiving instanceof Player && DinoSupport.isCreativePlayer(par1EntityLiving)) {
            return false;
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:232-249): {@code PlayNicely} off, every living entity in {@code expand(20, 6, 20)}, sorted. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0, 6.0, 20.0)));
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

    /** {@code getAttacking} (:251-253). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:255-257). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: the entity-free part of {@code getCanSpawnHere} (:259-297) - spawner, {@code posY >= 50}, night,
     * air in the 3x3 column x/z -1..1 at y+1..5, no other T. Rex in {@code expand(24, 12, 24)}.
     *
     * <p>PORT: the light roll runs once, in {@link #checkSpawnRules}; the spawner block scan for "T. Rex" is
     * {@code MobSpawnType.SPAWNER} (see Alosaurus).
     */
    public static boolean checkTRexSpawnRules(final EntityType<TRex> type, final ServerLevelAccessor level,
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
        if (!DinoSupport.airAround(level, pos.getX(), pos.getY(), pos.getZ(), -1, 2, -1, 2, 1, 6)) {
            return false;
        }
        return level.getEntitiesOfClass(TRex.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(24.0, 12.0, 24.0)).isEmpty();
    }

    /** {@code getCanSpawnHere} (:259-297) on the positioned instance, in the original order. */
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
                DinoSupport.floor(this.getZ()), -1, 2, -1, 2, 1, 6)) {
            return false;
        }
        return level.getEntitiesOfClass(TRex.class, this.getBoundingBox().inflate(24.0, 12.0, 24.0),
                other -> other != this).isEmpty();
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
