package com.swbr.orespawn.entity.dino;

import com.swbr.orespawn.client.renderer.RenderInfo;
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
 * Port of {@code danger.orespawn.Nastysaurus} (Nastysaurus.java:16-327): {@code nastysaurus} (OreSpawnMain.java:4097,
 * tracking 128/1/false). {@code EntityMob} is {@link Monster}.
 *
 * <p>Values: health, attack and armor from {@code Nastysaurus_stats} (200/32/17 by default), speed 0.35 re-set every
 * tick (:26, :85), hitbox 2.2 x 4.6 (:29, entity type), {@code experienceValue = 40} (:31, {@code xpReward}),
 * {@code fireResistance = 100} (:32), immune to cactus (:177). DataWatcher 20 {@code attacking} (:51). The revenge target
 * {@code rt} is not saved.
 *
 * <p>{@link RenderInfo} (:20, :52-78) is the model's per-entity note pad for the idle jaw snap. It is a plain data class
 * without client imports; the GhostSkelly port (W06) keeps it on the entity the same way.
 *
 * <p>Not carried over: {@code onLivingUpdate} (:101-103), {@code initCreature} (:152-153), {@code isAIEnabled},
 * {@code interact} returning {@code false} (:155-157) and the unused {@code getDropItem} (:128-130).
 */
public class Nastysaurus extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking} (:51, :280-286). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Nastysaurus.class, EntityDataSerializers.INT);

    private final GenericTargetSorter TargetSorter;
    private float moveSpeed;
    private RenderInfo renderdata;
    @Nullable
    private LivingEntity rt;

    /**
     * {@code Nastysaurus(World)} (:23-40). {@code entityInit} (:49-63) zeroed a fresh {@link RenderInfo} before the
     * constructor replaced it with another fresh one; a new instance here is the same state.
     */
    public Nastysaurus(final EntityType<? extends Nastysaurus> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.35f;
        this.renderdata = new RenderInfo();
        this.rt = null;
        // setSize(2.2f, 4.6f) (:29) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:30) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 40;
        // fireResistance = 100 (:32) is getFireImmuneTicks().
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(this, 1.0, false) (:35) -> MoveThroughVillageGoal over village POIs, see Alosaurus.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) (:39) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard precedent, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        DinoSupport.applyStats(this, MobStats.Nastysaurus_stats());
    }

    /** {@code applyEntityAttributes} (:42-47) with the manifest defaults; the constructor applies the configured stats. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, 32.0)
                .add(Attributes.ARMOR, 17.0);
    }

    /** {@code entityInit} (:49-63), the DataWatcher part. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code getRenderInfo} (:65-67). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:69-78): copies the fields. */
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

    /** {@code fireResistance = 100} (:32). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code canDespawn} (:80-82): {@code !isNoDespawnRequired()}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:84-87), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:89-91). */
    public int mygetMaxHealth() {
        return MobStats.Nastysaurus_stats().health();
    }

    /** {@code getTotalArmorValue} (:93-95) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Nastysaurus_stats().defense();
    }

    /** {@code getLivingSound} (:105-110). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ALO_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:112-114). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:116-118). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:120-122). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:124-126). */
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
     * {@code dropFewItems} (:137-150) through {@code dropItemRand} (:132-135), three blocks up: 10 each of iron ingots,
     * rotten flesh, leather and string.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var4 = 0; var4 < 10; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.IRON_INGOT, 1), 3.0);
        }
        for (int var4 = 0; var4 < 10; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.ROTTEN_FLESH, 1), 3.0);
        }
        for (int var4 = 0; var4 < 10; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.LEATHER, 1), 3.0);
        }
        for (int var4 = 0; var4 < 10; ++var4) {
            DinoSupport.dropItemRand(this, new ItemStack(Items.STRING, 1), 3.0);
        }
    }

    /** {@code attackEntityAsMob} (:159-173): the {@code EntityMob} bite, then 1.2 away and 0.1 up (0.2 for players and removed targets). */
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

    /** {@code attackEntityFrom} (:175-185): cactus does nothing; otherwise super, and a living source becomes {@code rt}. */
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
     * {@code updateAITasks} (:187-226): like the T. Rex with {@code rand(5) == 0}, {@code rt} forgotten with 1 in 250 and a
     * bite range of {@code (4.5 + width / 2)}.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(5) == 0) {
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
                if (this.distanceToSqr(e) < (4.5f + e.getBbWidth() / 2.0f) * (4.5f + e.getBbWidth() / 2.0f)) {
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

    /** {@code isSuitableTarget} (:228-259). */
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
        if (par1EntityLiving instanceof Nastysaurus) {
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

    /** {@code findSomethingToAttack} (:261-278): {@code PlayNicely} off, every living entity in {@code expand(32, 8, 32)}, sorted. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(32.0, 8.0, 32.0)));
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

    /** {@code getAttacking} (:280-282). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:284-286). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: the entity-free part of {@code getCanSpawnHere} (:288-326) - spawner, {@code posY >= 50}, night,
     * air in the 2x2 column x/z -1..0 at y+1..5, no other Nastysaurus in {@code expand(16, 8, 16)}.
     *
     * <p>PORT: the light roll runs once, in {@link #checkSpawnRules}; the spawner block scan is
     * {@code MobSpawnType.SPAWNER} (see Alosaurus).
     */
    public static boolean checkNastysaurusSpawnRules(final EntityType<Nastysaurus> type, final ServerLevelAccessor level,
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
        return level.getEntitiesOfClass(Nastysaurus.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 8.0, 16.0)).isEmpty();
    }

    /** {@code getCanSpawnHere} (:288-326) on the positioned instance, in the original order. */
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
        return level.getEntitiesOfClass(Nastysaurus.class, this.getBoundingBox().inflate(16.0, 8.0, 16.0),
                other -> other != this).isEmpty();
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
