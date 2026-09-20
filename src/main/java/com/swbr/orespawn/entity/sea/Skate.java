package com.swbr.orespawn.entity.sea;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.critter.CritterSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModSounds;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Skate} (Skate.java:14-339), id {@code skate} ("Skate", OreSpawnMain.java:3893,
 * tracking 32/1/false). A small flat water monster that only hunts players and dries out on land
 * (verhalten/entity-12.md).
 *
 * <p>Values: {@code Skate_stats} (8/8/4), speed 0.25 re-set every tick (:28, :62-65), {@code experienceValue} 10 (:35),
 * {@code fireResistance} 3 (:36). State: DataWatcher 20 {@code attacking} (a {@code Byte} in the bytecode,
 * {@code javap}); no NBT.
 *
 * <p>Not carried over: {@code getAttackStrength} 4 (:87-90, no caller), {@code buddy} (:16, never set),
 * {@code initCreature} (:116-117), {@code onLivingUpdate} (:83-85, only {@code super}). The water breathing of
 * {@code canBreatheUnderwater} (:71-73) is the tag {@code minecraft:can_breathe_under_water}.
 */
public class Skate extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(Skate.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    private final SeaSupport.WaterScan scan = new SeaSupport.WaterScan();

    /** Constructor (:24-44). {@code setSize(0.75f, 0.25f)} is the entity type's size (R9). */
    public Skate(final EntityType<? extends Skate> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.25f;
        // PORT: getNavigator().setAvoidsWater(false) (:34) is a water path malus of 0 (Whale, W06).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 10;
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Skate_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:46-51) at registration time (R3); armor from {@code getTotalArmorValue} (:75-77). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Skate_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** The tasks of the constructor (:39-43), same priorities. */
    @Override
    protected void registerGoals() {
        // PORT: EntityAISwimming is FloatGoal, as in W01-W07.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code entityInit} (:53-56). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 3} (:36). */
    @Override
    protected int getFireImmuneTicks() {
        return 3;
    }

    /** {@code canDespawn} (:58-60). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:62-65), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:67-69). */
    public int mygetMaxHealth() {
        return MobStats.Skate_stats().health();
    }

    /** {@code getTotalArmorValue} (:75-77) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Skate_stats().defense();
    }

    /** {@code getLivingSound} (:92-94). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:96-98). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:100-102). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RATDEAD.get();
    }

    /** {@code getSoundVolume} (:104-106). */
    @Override
    protected float getSoundVolume() {
        return 0.33f;
    }

    /** {@code getSoundPitch} (:108-110). */
    @Override
    public float getVoicePitch() {
        return 1.75f;
    }

    /** {@code getDropItem} (:112-114): string. */
    protected Item getDropItem() {
        return Items.STRING;
    }

    /** Vanilla 1.7.10 {@code dropFewItems} with {@link #getDropItem}, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, this.getDropItem());
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code attackEntityFrom} (:119-137): ignored while dead and from another Skate; a mob attacker becomes the target
     * with a 1.2 path; then the hit.
     *
     * <p>PORT: {@code setTarget(e)} (the old-AI {@code entityToAttack}, :133) has no reader with AI tasks and is
     * dropped; {@code e instanceof EntityLiving} is {@link Mob}; {@code isDead} is {@link #isRemoved()}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.isRemoved()) {
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Skate) {
            return false;
        }
        if (e != null && e instanceof Mob) {
            if (e instanceof Skate) {
                return false;
            }
            this.setTarget((LivingEntity) e);
            this.getNavigation().moveTo(e, 1.2);
            ret = true;
        }
        ret = super.hurt(par1DamageSource, par2);
        return ret;
    }

    /**
     * {@code updateAITasks} (:222-276), after the goal selectors: on land with 1 in 10 search water (shells up to 11,
     * vertical cap 5) and walk there at 1.33, or with none lose 1 health with 1 in 25 and vanish without drops at 0;
     * with 1 in 8 look for a player, within {@code distSq 4} set {@code attacking} and bite with (1/4 or 1/5), else walk
     * at 1.2.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (!this.isInWater() && this.level().random.nextInt(10) == 0) {
            this.scan.search(this, 5);
            if (this.scan.closest < 99999) {
                this.getNavigation().moveTo((double) this.scan.tx, (double) (this.scan.ty - 1), (double) this.scan.tz, 1.33);
            } else {
                if (this.level().random.nextInt(25) == 1) {
                    SeaSupport.legacyHeal(this, -1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }
        if (this.level().random.nextInt(8) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                if (this.distanceToSqr(e) < 4.0) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
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

    /** {@code isSuitableTarget} (:278-296): alive, visible, a player unless creative; nothing else. */
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
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return false;
    }

    /**
     * {@code findSomethingToAttack} (:298-321): nothing with {@code PlayNicely}; a living attack target is kept;
     * otherwise it is cleared and the first suitable entity in {@code expand(10, 4, 10)} is taken.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 4.0, 10.0));
        var5.sort(this.TargetSorter);
        final LivingEntity e = this.getTarget();
        if (e != null && e.isAlive()) {
            return e;
        }
        this.setTarget(null);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:323-325). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:327-329). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /** {@code findBuddies} (:331-334) around a spawn position, before the entity exists: Skates in {@code expand(16, 8, 16)}. */
    private static int findBuddies(final EntityType<Skate> type, final LevelAccessor level, final BlockPos pos) {
        return level.getEntitiesOfClass(Skate.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 8.0, 16.0)).size();
    }

    /**
     * {@code getCanSpawnHere} (:336-338) as the placement predicate: Y 50 or higher, daytime, 1 in 30 on the world
     * random and at most 6 Skates nearby. 1.7.10 asked it once per attempt with the mob not yet in the world, so the
     * count never included itself.
     */
    public static boolean checkSkateSpawnRules(final EntityType<Skate> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(level.getLevel()) && random.nextInt(30) == 1
                && findBuddies(type, level, pos) <= 6;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkSkateSpawnRules}; {@code Monster}'s light weight is not asked. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
