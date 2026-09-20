package com.swbr.orespawn.entity.sea;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.SeaViper} (SeaViper.java:18-630), id {@code sea_viper} ("Sea Viper",
 * OreSpawnMain.java:4009, tracking 64/1/false). The code twin of {@code SeaViper}: a serpent miniboss with a
 * poison bite and a day-time spawn (verhalten/entity-12.md).
 *
 * <p>Values: {@code SeaViper_stats} (160/22/12), start speed 0.35 (:41), then 0.75 in water and 0.25 on land re-set
 * every tick (:84-87, :118-123), {@code experienceValue} 120 (:44), {@code fireResistance} 30 (:45). State:
 * DataWatcher 20 {@code attacking} (a {@code Byte} in the bytecode, {@code javap}); no NBT.
 *
 * <p>Not carried over: the {@code RenderInfo} scratch data (:21, :63-114) - {@code ModelSeaViper} never reads it;
 * {@code stream_count} (:22, never used); {@code getSeaViperHealth} (:126-128, no caller); {@code getDropItem} chicken
 * (:153-155, shadowed by {@code dropFewItems}); {@code initCreature} (:338-339). The water breathing of
 * {@code canBreatheUnderwater} (:627-629) is the tag {@code minecraft:can_breathe_under_water}.
 */
public class SeaViper extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code SeaViperModel}. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(SeaViper.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private int hurt_timer;
    private float moveSpeed;
    private final SeaSupport.WaterScan scan = new SeaSupport.WaterScan();

    /** Constructor (:30-55). {@code setSize(1.5f, 2.5f)} is the entity type's size (R9). */
    public SeaViper(final EntityType<? extends SeaViper> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.hurt_timer = 0;
        this.moveSpeed = 0.25f;
        this.moveSpeed = 0.35f;
        // PORT: getNavigator().setAvoidsWater(false) (:43) is a water path malus of 0 (Whale, W06).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 120;
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.SeaViper_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:57-62) at registration time (R3); armor from {@code getTotalArmorValue} (:108-110). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.SeaViper_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** The tasks of the constructor (:49-54), same priorities. */
    @Override
    protected void registerGoals() {
        // PORT: EntityAISwimming is FloatGoal, as in W01-W07.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 10.0f));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code entityInit} (:64-78). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 30} (:45). */
    @Override
    protected int getFireImmuneTicks() {
        return 30;
    }

    /** {@code canDespawn} (:80-82). */
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
        return MobStats.SeaViper_stats().health();
    }

    /** {@code getTotalArmorValue} (:108-110) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.SeaViper_stats().defense();
    }

    /** {@code onLivingUpdate} (:116-124), both sides: 0.75 in water, else 0.25. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater()) {
            this.moveSpeed = 0.75f;
        } else {
            this.moveSpeed = 0.25f;
        }
    }

    /** {@code getLivingSound} (:130-135): 1 in 2 on the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(2) == 0) {
            return ModSounds.SEAVIPER_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:137-139). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SEAVIPER_HIT.get();
    }

    /** {@code getDeathSound} (:141-143). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SEAVIPER_DEATH.get();
    }

    /** {@code getSoundVolume} (:145-147). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:149-151). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code dropFewItems} (:167-336), spread 2, one block up: a Sea Viper Tongue, an item frame, 9..14 rounds of one
     * fish and one raw chicken (world random), then the {@code rand(20)} iron table. PORT: {@code Items.fish} meta 0 is
     * raw cod.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        ArthropodSupport.dropItemRand(this, SeaSupport.stack(ModItems.SEA_VIPER_TONGUE.get()), 2);
        ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.ITEM_FRAME), 2);
        for (int var5 = 9 + this.level().random.nextInt(6), var6 = 0; var6 < var5; ++var6) {
            ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.COD), 2);
            ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.CHICKEN), 2);
        }
        SeaSupport.ironTable(this);
    }

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code interact} (:341-343): false. */
    @Override
    protected InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     * {@code attackEntityAsMob} (:345-371): the vanilla hit, then knockback 0.8 / 0.14 (0.28 for players and dead
     * entities) and with 1 in 2 (world random) poison for {@code var2 * 20} ticks.
     *
     * <p>Original bug kept (R18): the difficulty ladder is nested inside the EASY branch, so EASY gives 8 seconds and
     * NORMAL and HARD keep the 6 of the declaration - the 10 and 12 are unreachable.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        int var2 = 6;
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                SeaSupport.legacyKnockback(this, par1Entity, 0.8, 0.14);
                if (this.level().getDifficulty() == Difficulty.EASY) {
                    var2 = 8;
                    if (this.level().getDifficulty() == Difficulty.NORMAL) {
                        var2 = 10;
                    } else if (this.level().getDifficulty() == Difficulty.HARD) {
                        var2 = 12;
                    }
                }
                if (this.level().random.nextInt(2) == 1) {
                    ((LivingEntity) par1Entity).addEffect(new MobEffectInstance(MobEffects.POISON, var2 * 20, 0));
                }
            }
            return true;
        }
        return false;
    }

    /**
     * {@code attackEntityFrom} (:373-392): cactus ignored; a hit only counts while {@code hurt_timer <= 0}, which then
     * blocks every further hit for 5 AI ticks; a mob attacker (not another Sea Viper, which returns {@code false})
     * becomes the target with a 1.2 path, even while blocked.
     *
     * <p>PORT: {@code setTarget(e)} (the old-AI {@code entityToAttack}, :389) has no reader with AI tasks and is
     * dropped; {@code e instanceof EntityLiving} is {@link Mob}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.CACTUS)) {
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (this.hurt_timer <= 0) {
            ret = super.hurt(par1DamageSource, par2);
            this.hurt_timer = 5;
        }
        if (e != null && e instanceof Mob) {
            if (e instanceof SeaViper) {
                return false;
            }
            this.setTarget((LivingEntity) e);
            this.getNavigation().moveTo(e, 1.2);
        }
        return ret;
    }

    /**
     * {@code updateAITasks} (:479-539), after the goal selectors: count {@code hurt_timer} down; on land with 1 in 25
     * search water (shells up to 11, vertical cap 10) and walk there at 1.33, or with none lose 1 health with 1 in 150
     * and vanish without drops at 0; with 1 in 5 look for a target, face it, within {@code (4.5 + width/2)^2} set
     * {@code attacking} and bite with (1/2 or 1/4), else walk at 1.5; in water below full health with 1 in 100 heal 1.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (!this.isInWater() && this.level().random.nextInt(25) == 0) {
            this.scan.search(this, 10);
            if (this.scan.closest < 99999) {
                this.getNavigation().moveTo((double) this.scan.tx, (double) (this.scan.ty - 1), (double) this.scan.tz, 1.33);
            } else {
                if (this.level().random.nextInt(150) == 1) {
                    SeaSupport.legacyHeal(this, -1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }
        if (this.level().random.nextInt(5) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (4.5f + e.getBbWidth() / 2.0f) * (4.5f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(2) == 0 || this.level().random.nextInt(4) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.5);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.level().random.nextInt(100) == 1 && this.isInWater() && this.getHealth() < this.mygetMaxHealth()) {
            // playSound("splash", 1.5f, rand * 0.2f + 0.9f): no namespace, silent (R18). The pitch argument still
            // drew from the world random.
            this.level().random.nextFloat();
            this.heal(1.0f);
        }
    }

    /**
     * {@code isSuitableTarget} (:541-566): alive, visible; a player unless creative; never another Sea Viper; any
     * monster; otherwise {@code MyUtils.isAttackableNonMob}.
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
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof SeaViper) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /**
     * {@code findSomethingToAttack} (:568-590): nothing with {@code PlayNicely}; a living attack target is kept;
     * otherwise it is cleared and the first suitable entity in {@code expand(18, 4, 18)} is taken.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(18.0, 4.0, 18.0));
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

    /** {@code getAttacking} (:592-594). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:596-598). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:600-625). A spawner allows it; otherwise Y at least 50, daytime and
     * no other Sea Viper within {@code expand(16, 5, 16)}. No light test.
     *
     * <p>PORT: the scan for a "Sea Viper" spawner is {@link MobSpawnType#SPAWNER} (catalogue README 5.9); the whole
     * rule lives here ({@link #checkSpawnRules} answers {@code true}).
     */
    public static boolean checkSeaViperSpawnRules(final EntityType<SeaViper> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (!InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        return level.getEntitiesOfClass(SeaViper.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 5.0, 16.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkSeaViperSpawnRules}. */
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
