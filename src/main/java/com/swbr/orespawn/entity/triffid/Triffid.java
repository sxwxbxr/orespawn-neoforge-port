package com.swbr.orespawn.entity.triffid;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ender.EnderReaper;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Triffid} (Triffid.java:15-346, verhalten/entity-13.md): the stationary carnivorous plant
 * of the floating islands. Registered as "Triffid" 64/1/false (OreSpawnMain.java:3679), {@code EntityMob} without a
 * spawn list; the {@code Island} entity plants it, a Greenhouse Dungeon spawner holds it.
 *
 * <p>Values: health, attack and defense from {@link MobStats#Triffid_stats()} (100/20/12), speed 0.13 every tick, XP 50
 * (:30, {@code EntityMob} - no {@code EntityAnimal} XP rule), {@code fireResistance} 75 (:31), not pushable (:195-197).
 * With 100 health there is no virtual health (the config clamp stops at 200).
 *
 * <p>It can only be hurt while it is open (DataWatcher 21). Every hit - accepted or refused - starts a 300-tick
 * {@code hurt_timer} during which it stands still, stays closed and does not look for prey. DataWatcher 20 is
 * {@code attacking}; the {@code RenderInfo} of the original is client scratch space that {@code ModelTriffid} never
 * reads. No NBT: {@code hurt_timer} is lost on reload, as in the original.
 *
 * <p>Not carried over: {@code getDropItem} (:170-176) is dead because {@code dropFewItems} is overridden;
 * {@code getTriffidHealth} (:146-148) has no caller in the original.
 */
public class Triffid extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, the tongue lashes while it is 1. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Triffid.class, EntityDataSerializers.INT);
    /** DataWatcher 21: open (1) or closed (0); the leaves spread while it is 1. */
    private static final EntityDataAccessor<Integer> DATA_OPEN_CLOSED = SynchedEntityData.defineId(Triffid.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    private int hurt_timer;
    private float moveSpeed;

    /** {@code Triffid(World)} (:22-39) with {@code applyEntityAttributes} (:58-63). */
    public Triffid(final EntityType<? extends Triffid> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.hurt_timer = 0;
        this.moveSpeed = 0.13f;
        // setSize(2.0f, 4.0f) (:28) is the entity type's size (R9).
        // getNavigator().setAvoidsWater(true) (:29): water is impassable for the path finder, malus -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 50;
        // fireResistance = 75 (:31) is getFireImmuneTicks; isImmuneToFire = false (:32) is the type default.
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent).
        // PORT: EntityAISwimming is FloatGoal, as in every earlier wave (W07 open point 3).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard/GiantRobot precedent).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // PORT: applyEntityAttributes (:58-63) read Triffid_stats during construction; the attribute supplier is built
        // before the config loads (R3), so the runtime values go in here, followed by the full health (GiantRobot).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Triffid_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:58-63) with the config defaults (manifest 100 / 0.13 / 20); see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.13f)
                .add(Attributes.ATTACK_DAMAGE, 20.0);
    }

    /** {@code entityInit} (:41-56); the {@code RenderInfo} zeroing has no counterpart (see the class comment). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_OPEN_CLOSED, 0);
    }

    /** {@code fireResistance = 75} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 75;
    }

    // canDespawn (:65-67) = !isNoDespawnRequired(): the Monster default - island Triffids despawn and are replanted.

    /**
     * {@code onUpdate} (:69-108): speed, the vanilla tick; with 1/100 a walk towards the side with more ground one block
     * below along both axes (the island centre); unless hurt, the body yaw turns to the nearest prey. Both sides, as in
     * the original. {@code (int)} casts of coordinates are {@code Mth.floor} (R20).
     *
     * <p>PORT: {@code tryMoveToXYZ(ix, posY, iz, 1.0)} is {@code moveTo}; {@code rotationYaw} is {@code setYRot}.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.level().random.nextInt(100) == 1) {
            int ix = Mth.floor(this.getX());
            int iz = Mth.floor(this.getZ());
            for (int k = -5; k <= 5; ++k) {
                final BlockState bid = this.level().getBlockState(
                        new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()) + k));
                if (!bid.isAir()) {
                    if (k < 0) {
                        --iz;
                    }
                    if (k > 0) {
                        ++iz;
                    }
                }
            }
            for (int k = -5; k <= 5; ++k) {
                final BlockState bid = this.level().getBlockState(
                        new BlockPos(Mth.floor(this.getX()) + k, Mth.floor(this.getY()) - 1, Mth.floor(this.getZ())));
                if (!bid.isAir()) {
                    if (k < 0) {
                        --ix;
                    }
                    if (k > 0) {
                        ++ix;
                    }
                }
            }
            this.getNavigation().moveTo((double) ix, this.getY(), (double) iz, 1.0);
        }
        if (this.hurt_timer <= 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.setYRot((float) Math.toDegrees(Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX())) - 90.0f);
                while (this.getYRot() < 0.0f) {
                    this.setYRot(this.getYRot() + 360.0f);
                }
            }
        }
    }

    /** {@code mygetMaxHealth} (:110-112). */
    public int mygetMaxHealth() {
        return MobStats.Triffid_stats().health();
    }

    /** {@code getTotalArmorValue} (:129-131). */
    public int getTotalArmorValue() {
        return MobStats.Triffid_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /**
     * {@code onLivingUpdate} (:137-144): after the vanilla living update (movement included), a hurt Triffid loses its
     * horizontal motion on the server.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.hurt_timer > 0) {
            final double n = 0.0;
            final Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(n, m.y, n);
        }
    }

    /** {@code getLivingSound} (:150-152). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TRIFFID_LIVING.get();
    }

    /** {@code getHurtSound} (:154-156). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.TRIFFID_HIT.get();
    }

    /** {@code getDeathSound} (:158-160). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TRIFFID_DEAD.get();
    }

    /** {@code getSoundVolume} (:162-164). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:166-168). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code dropItemRand} (:178-186): x and z shifted by {@code OreSpawnRand} -2..+2, one block up, spawned directly
     * (no pickup delay, no drop capture), as in the original.
     */
    private ItemStack dropItemRand(final Item index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(3) - OreSpawn.OreSpawnRand.nextInt(3),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(3) - OreSpawn.OreSpawnRand.nextInt(3),
                is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:188-193): 4-9 Green Goo and one item frame. Looting ignored. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 4 + this.level().random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(ModItems.GREEN_GOO.get(), 1);
        }
        this.dropItemRand(Items.ITEM_FRAME, 1);
    }

    /** {@code canBePushed} (:195-197). */
    @Override
    public boolean isPushable() {
        return false;
    }

    // attackEntityAsMob (:199-202) only called super: Mob.doHurtTarget.

    /**
     * {@code attackEntityFrom} (:204-216): while the timer runs or the plant is closed, the hit is refused and the timer
     * starts over; otherwise the damage goes through, the timer starts and the plant closes.
     *
     * <p>A refused hit returns before {@code super}, so neither the invulnerability window nor the hurt sound is touched.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.hurt_timer > 0 || this.getOpenClosed() == 0) {
            this.hurt_timer = 300;
            this.setAttacking(0);
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        this.hurt_timer = 300;
        this.setOpenClosed(0);
        this.setAttacking(0);
        return ret;
    }

    /**
     * {@code updateAITasks} (:218-259): after the task list (already run by {@code serverAiStep}), the timer counts down
     * and keeps the plant closed; 1/250 heals one point below full health; 1/80 without a timer opens (1/8) or closes the
     * plant; 1/10 without a timer looks for prey, opens, and within five blocks turns and strikes.
     *
     * <p>PORT: the move, look and jump helpers of {@code super.updateAITasks()} run after this method in 1.21.1
     * (EntityCannonFodder precedent). {@code setFire(0)} is {@code igniteForTicks(0)}: it raises negative fire ticks to
     * 0 and does <em>not</em> put out a burning plant - both versions only raise the counter.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
            this.igniteForTicks(0);
            this.setOpenClosed(0);
        }
        if (this.level().random.nextInt(250) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
        if (this.level().random.nextInt(80) == 2 && this.hurt_timer <= 0) {
            if (this.level().random.nextInt(8) == 1) {
                this.setOpenClosed(1);
            } else {
                this.setOpenClosed(0);
            }
        }
        if (this.level().random.nextInt(10) == 1 && this.hurt_timer <= 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.setOpenClosed(1);
                if (this.distanceToSqr(e) < 25.0) {
                    this.setYRot((float) Math.toDegrees(Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX())) - 90.0f);
                    while (this.getYRot() < 0.0f) {
                        this.setYRot(this.getYRot() + 360.0f);
                    }
                    this.setAttacking(1);
                    this.doHurtTarget(e);
                } else {
                    this.setAttacking(0);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:261-306): alive, not ignorable, visible; never a creeper, Ender Reaper, another Triffid,
     * Terrible or Lurking Terror, Nightmare or Dragon, nor a creative player.
     *
     * <p>PORT: TerribleTerror and LurkingTerror (this wave, another porter) and Dragon (W09) are matched by registry id
     * ({@code DinoSupport.isOreSpawnType} precedent); none has a subclass in the original.
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Creeper) {
            return false;
        }
        if (par1EntityLiving instanceof EnderReaper) {
            return false;
        }
        if (par1EntityLiving instanceof Triffid) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "terrible_terror")) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "lurking_terror")) {
            return false;
        }
        if (par1EntityLiving instanceof PitchBlack) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "dragon")) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:308-325): {@code PlayNicely} disables it; box grown by 10/8/10. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 8.0, 10.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:327-329). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:331-333). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getOpenClosed} (:335-337). */
    public final int getOpenClosed() {
        return this.entityData.get(DATA_OPEN_CLOSED);
    }

    /** {@code setOpenClosed} (:339-341). */
    public final void setOpenClosed(final int par1) {
        this.entityData.set(DATA_OPEN_CLOSED, par1);
    }

    /** Placement predicate: {@code getCanSpawnHere} (:343-345) is always {@code true}. */
    public static boolean checkTriffidSpawnRules(final EntityType<Triffid> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }

    /** The override replaced {@code EntityMob}'s light test and {@code EntityCreature}'s path weight: always {@code true}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code instanceof} for a class of another porter or a later wave, by registry id. */
    private static boolean isOreSpawnType(final Entity entity, final String id) {
        final ResourceLocation key = EntityType.getKey(entity.getType());
        return key.getNamespace().equals(OreSpawn.MOD_ID) && key.getPath().equals(id);
    }
}
