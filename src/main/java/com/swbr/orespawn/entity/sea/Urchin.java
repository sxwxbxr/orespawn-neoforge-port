package com.swbr.orespawn.entity.sea;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.cow.CrystalCow;
import com.swbr.orespawn.entity.critter.CritterSupport;
import com.swbr.orespawn.entity.herbivore.Peacock;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Urchin} (Urchin.java:15-314), id {@code crystal_urchin} ("Crystal Urchin",
 * OreSpawnMain.java:3901, tracking 64/1/false). A fiery crystal creature of the Crystal dimension: sets its prey on
 * fire, hurts itself in water, fades away by day unless it came from a spawner (verhalten/entity-13.md).
 *
 * <p>Values: {@code Urchin_stats} (25/10/4), speed 0.3 re-set every tick (:26, :69-70), {@code experienceValue} 20
 * (:30), {@code fireResistance} 1000 (:31); {@code isImmuneToFire} (:32) is {@code EntityType.Builder.fireImmune()}.
 * State: DataWatcher 20 {@code attacking} (a {@code Byte} in the bytecode, {@code javap}); {@code was_spawnered} is
 * volatile, as in the original - a spawner urchin that is reloaded counts as natural again.
 *
 * <p>Not carried over: the {@code RenderInfo} scratch data (:18, :49-106) - {@code ModelUrchin} never reads it.
 */
public class Urchin extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code UrchinModel}. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(Urchin.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    private int was_spawnered;

    /** Constructor (:22-40). {@code setSize(1.35f, 2.1f)} is the entity type's size (R9). */
    public Urchin(final EntityType<? extends Urchin> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.3f;
        this.was_spawnered = 0;
        // PORT: getNavigator().setAvoidsWater(true) (:29) - water malus -1 (Chipmunk, W06; Hammerhead, W07).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 20;
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Urchin_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:42-47) at registration time (R3); armor from {@code getTotalArmorValue} (:103-105). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Urchin_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** The tasks of the constructor (:35-39), same priorities. */
    @Override
    protected void registerGoals() {
        // PORT: EntityAISwimming is FloatGoal, as in W01-W07.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code entityInit} (:49-63). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 1000} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code canDespawn} (:65-67): not persistent and not from a spawner. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && this.was_spawnered == 0;
    }

    /**
     * {@code onUpdate} (:69-83): speed, the tick, then - unless persistent or from a spawner - vanish with 1 in 400
     * (world random) during the first half of the day ({@code worldTime % 24000 < 12000}).
     *
     * <p>PORT (R18 case 4): the fade-out runs on the server only. 1.7.10 rolled it on both sides with separate randoms,
     * and the client never knew {@code isNoDespawnRequired} or {@code was_spawnered}; a client-side
     * {@code discard()} in 1.21.1 would leave a live server entity invisible.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.isPersistenceRequired()) {
            return;
        }
        if (this.was_spawnered != 0) {
            return;
        }
        long t = this.level().getDayTime();
        t %= 24000L;
        if (t < 12000L && this.level().random.nextInt(400) == 1) {
            this.discard();
        }
    }

    /** {@code mygetMaxHealth} (:85-87). */
    public int mygetMaxHealth() {
        return MobStats.Urchin_stats().health();
    }

    /** {@code getTotalArmorValue} (:103-105) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Urchin_stats().defense();
    }

    /**
     * {@code onLivingUpdate} (:111-124), both sides: with 1 in 3 (world random) a flame above the body; in water, of
     * that 1 in 5, it attacks itself and smokes. The client's self-attack is refused by {@code LivingEntity.hurt}, as
     * 1.7.10's {@code attackEntityFrom} refused it on a remote world.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().random.nextInt(3) == 1) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.75, this.getZ(), 0.0,
                    (double) (this.level().random.nextFloat() / 10.0f), 0.0);
            if (this.isInWater() && this.level().random.nextInt(5) == 1) {
                this.doHurtTarget(this);
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.75, this.getZ(), 0.0,
                        (double) (this.level().random.nextFloat() / 10.0f), 0.0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.75, this.getZ(), 0.0,
                        (double) (this.level().random.nextFloat() / 10.0f), 0.0);
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 2.0, this.getZ(), 0.0,
                        (double) (this.level().random.nextFloat() / 10.0f), 0.0);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.0, this.getZ(), 0.0,
                        (double) (this.level().random.nextFloat() / 10.0f), 0.0);
            }
        }
    }

    /** {@code getLivingSound} (:126-128). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KYUUBI_LIVING.get();
    }

    /** {@code getHurtSound} (:130-132). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.GLASSHIT.get();
    }

    /** {@code getDeathSound} (:134-136). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLASSDEAD.get();
    }

    /** {@code getSoundVolume} (:138-140). */
    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }

    /** {@code getSoundPitch} (:142-144). */
    @Override
    public float getVoicePitch() {
        return 1.25f;
    }

    /** {@code getDropItem} (:146-155): {@code rand(3)} on the world random - nothing, a pink crystal ingot or a crystal apple. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(3);
        if (i == 1) {
            return ModItems.CRYSTAL_PINK_INGOT.get();
        }
        if (i == 2) {
            return ModItems.CRYSTAL_APPLE.get();
        }
        return null;
    }

    /** Vanilla 1.7.10 {@code dropFewItems} with {@link #getDropItem}, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, this.getDropItem());
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code interact} (:157-159): false. */
    @Override
    protected InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /** {@code attackEntityAsMob} (:161-164): the target burns for 5 seconds, then the vanilla hit. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        par1Entity.igniteForSeconds(5.0f);
        return super.doHurtTarget(par1Entity);
    }

    /**
     * {@code updateAITasks} (:166-188), after the goal selectors: with 1 in 8 look for a target, within {@code distSq 8}
     * set {@code attacking} and strike with (1/7 or 1/8), else walk at 1.2; no target clears {@code attacking}.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(8) == 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                if (this.distanceToSqr(e) < 8.0) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(7) == 0 || this.level().random.nextInt(8) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.2);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /** {@code attackEntityFrom} (:190-196): cactus ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /**
     * {@code isSuitableTarget} (:198-249), in this order: alive, not ignoreable, visible, none of the Crystal and water
     * creatures (Vortex, Rotator, Peacock, Crystal Cow, Irukandji, Skate, Whale, Flounder, Urchin), no creative player;
     * everything else.
     *
     * <p>{@code Vortex} and {@code Rotator} are ported in parallel in this wave ({@code entity.crystal}); they are
     * matched by registry id ({@code vortex}, {@code rotator}), the W07 precedent. Neither has a subclass, so the set
     * is the original's.
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
        if (SeaSupport.isType(par1EntityLiving, "vortex")) {
            return false;
        }
        if (SeaSupport.isType(par1EntityLiving, "rotator")) {
            return false;
        }
        if (par1EntityLiving instanceof Peacock) {
            return false;
        }
        if (par1EntityLiving instanceof CrystalCow) {
            return false;
        }
        if (par1EntityLiving instanceof Irukandji) {
            return false;
        }
        if (par1EntityLiving instanceof Skate) {
            return false;
        }
        if (par1EntityLiving instanceof Whale) {
            return false;
        }
        if (par1EntityLiving instanceof Flounder) {
            return false;
        }
        if (par1EntityLiving instanceof Urchin) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:251-267): nothing with {@code PlayNicely}; else the first suitable in {@code expand(16, 3, 16)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0, 3.0, 16.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:269-271). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:273-275). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:277-313). A spawner allows it; otherwise at least 6 of the 9 blocks one
     * above the feet (x/z -1..1) are air, the monster light test passes and the world time is in the night half
     * ({@code worldTime % 24000 >= 13000}).
     *
     * <p>PORT: the "Crystal Urchin" spawner scan (x/z -2..2, y +1..+3) is {@link MobSpawnType#SPAWNER} (catalogue 5.9);
     * its side effect {@code was_spawnered = 1} is set in {@link #checkSpawnRules}, which spawners ask with that type
     * (same as Rotator in this wave). {@code Blocks.air} is any air block; {@code isValidLightLevel} is
     * {@link LegacyLightLevel}; R20: the {@code (int)} casts are the block position.
     */
    public static boolean checkUrchinSpawnRules(final EntityType<Urchin> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        int sc = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int j = -1; j <= 1; ++j) {
                if (level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + 1, pos.getZ() + k)).isAir()) {
                    ++sc;
                }
            }
        }
        if (sc < 6) {
            return false;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        long t = level.getLevel().getDayTime();
        t %= 24000L;
        return t >= 13000L;
    }

    /**
     * The override replaced {@code EntityCreature}'s path-weight test, which 1.21.1 asks here; the predicate holds the
     * rule. A spawner asks with {@link MobSpawnType#SPAWNER}: that is the original's spawner branch, which marked the
     * urchin.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (reason == MobSpawnType.SPAWNER) {
            this.was_spawnered = 1;
        }
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
