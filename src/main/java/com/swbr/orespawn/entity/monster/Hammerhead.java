package com.swbr.orespawn.entity.monster;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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
 * Port of {@code danger.orespawn.Hammerhead} (Hammerhead.java:16-302), id {@code hammerhead} ("Hammerhead",
 * OreSpawnMain.java:4049, tracking 64/1/false). A huge hammer-headed beast that attacks players and monsters and throws
 * them high (verhalten/entity-09.md).
 *
 * <p>Values: {@code Hammerhead_stats} (240/75/20), speed 0.35 re-set every tick (:25, :57), {@code experienceValue}
 * 350 (:29), {@code fireResistance} 100 (:30), cactus immune (:158). State: DataWatcher 20 {@code attacking}, a
 * {@code Byte} in the bytecode ({@code javap}); the revenge memory {@code rt} is not saved. No NBT.
 */
public class Hammerhead extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code HammerheadModel} for the nodding head. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(Hammerhead.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    @Nullable
    private LivingEntity rt;

    /** {@code Hammerhead(World)} (:22-38). {@code setSize(3.0f, 5.0f)} (:27) is the entity type's size (R9). */
    public Hammerhead(final EntityType<? extends Hammerhead> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.35f;
        this.rt = null;
        // PORT: getNavigator().setAvoidsWater(true) (:28) - water malus -1 (Chipmunk, W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 350;
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, MonsterSupport.legacyMoveThroughVillage(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        final MobStats stats = MobStats.Hammerhead_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:40-45) at registration time (R3), armor from {@code getTotalArmorValue} (:65-67). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Hammerhead_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:47-50). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 100} (:30). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code canDespawn} (:52-54). */
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
        return MobStats.Hammerhead_stats().health();
    }

    /** {@code getTotalArmorValue} (:65-67) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Hammerhead_stats().defense();
    }

    /** {@code getLivingSound} (:77-82): 1 in 3 from the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return ModSounds.HAMMERHEAD_LIVING.get();
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
        return ModSounds.HAMMERHEAD_DEATH.get();
    }

    /** {@code getSoundVolume} (:92-94). */
    @Override
    protected float getSoundVolume() {
        return 1.2f;
    }

    /** {@code getSoundPitch} (:96-98). */
    @Override
    public float getVoicePitch() {
        return 0.9f;
    }

    // getDropItem (:100-102) beef: unused. initCreature (:133-134) overrode nothing. interact (:136-138) false: default.

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:109-131), spread ±4, y+2: 8 bottles o' enchanting, 10 experience catchers, 16 creeper
     * launchers, 4 creeper repellents, 6 beef, 2 experience tree seeds, and with 1 in 3 (world random) the Attitude
     * Adjuster.
     *
     * <p>PORT: {@code CreeperRepellent} is registered since W10 (catalogue README 4.14). Its item is
     * resolved by registry id, and if that id were unknown the four drops are skipped; the {@code OreSpawnRand} draws
     * are skipped with them.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var4 = 0; var4 < 8; ++var4) {
            MonsterSupport.dropItemRand(this, Items.EXPERIENCE_BOTTLE, 1, 5, 2.0);
        }
        for (int var4 = 0; var4 < 10; ++var4) {
            MonsterSupport.dropItemRand(this, ModItems.EXPERIENCE_CATCHER.get(), 1, 5, 2.0);
        }
        for (int var4 = 0; var4 < 16; ++var4) {
            MonsterSupport.dropItemRand(this, ModItems.CREEPER_LAUNCHER.get(), 1, 5, 2.0);
        }
        final Item creeperRepellent = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "creeperrepellent"));
        if (creeperRepellent != Items.AIR) {
            for (int var4 = 0; var4 < 4; ++var4) {
                MonsterSupport.dropItemRand(this, creeperRepellent, 1, 5, 2.0);
            }
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            MonsterSupport.dropItemRand(this, Items.BEEF, 1, 5, 2.0);
        }
        for (int var4 = 0; var4 < 2; ++var4) {
            MonsterSupport.dropItemRand(this, ModItems.EXPERIENCETREE_SEED.get(), 1, 5, 2.0);
        }
        if (this.level().random.nextInt(3) == 1) {
            MonsterSupport.dropItemRand(this, ModItems.HAMMY.get(), 1, 5, 2.0);
        }
    }

    /** {@code attackEntityAsMob} (:140-154): the vanilla hit, then knockback 1.1 / 0.85 (1.7 for players and dead entities). */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                MonsterSupport.legacyKnockback(this, par1Entity, 1.1, 0.85);
            }
            return true;
        }
        return false;
    }

    /** {@code attackEntityFrom} (:156-166): cactus ignored; otherwise the hit, and a living source becomes {@code rt}. */
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
     * {@code updateAITasks} (:168-207), after the selectors: 1 in 3 take {@code rt} (none with {@code PlayNicely}); forget
     * it when dead ({@code isDead}, i.e. removed) or with 1 in 250, skip it while not visible; otherwise search. With a
     * target face it; within squared {@code (7 + width/2)^2} set {@code attacking} and hit with (1/3 or 1/4); out of reach
     * walk at 1.25. No target clears {@code attacking}.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(3) == 1) {
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
                if (this.distanceToSqr(e) < (7.0f + e.getBbWidth() / 2.0f) * (7.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(3) == 1 || this.level().random.nextInt(4) == 1) {
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

    /** {@code isSuitableTarget} (:209-234): visible, no Hammerhead, a non-creative player, any monster, or {@code isAttackableNonMob}. */
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
        if (par1EntityLiving instanceof Hammerhead) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /** {@code findSomethingToAttack} (:236-253): the first suitable entity in {@code expand(18, 9, 18)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(18.0, 9.0, 18.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:255-257). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:259-261). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:263-301). A spawner allows it; otherwise the monster light test, y at
     * least 50, night, air in x/z -1..0 on y+1..5 and no other Hammerhead within {@code expand(16, 8, 16)}.
     *
     * <p>PORT: spawner scan → {@link MobSpawnType#SPAWNER} (README 5.9); {@code isValidLightLevel} →
     * {@link LegacyLightLevel#isValidLightLevel} (not {@code Monster.isDarkEnoughToSpawn}, which adds the dimension's
     * block-light limit and light test; W07 review, as Molenoid); the whole rule lives here ({@link #checkSpawnRules} answers {@code true}).
     */
    public static boolean checkHammerheadSpawnRules(final EntityType<Hammerhead> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        for (int k = -1; k < 1; ++k) {
            for (int j = -1; j < 1; ++j) {
                for (int i = 1; i < 6; ++i) {
                    if (!MonsterSupport.isAir(level, pos.getX() + j, pos.getY() + i, pos.getZ() + k)) {
                        return false;
                    }
                }
            }
        }
        return level.getEntitiesOfClass(Hammerhead.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 8.0, 16.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkHammerheadSpawnRules}. */
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
