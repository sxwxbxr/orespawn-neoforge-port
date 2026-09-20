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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.SeaMonster} (SeaMonster.java:17-616), id {@code sea_monster} ("Sea Monster",
 * OreSpawnMain.java:4001, tracking 64/1/false). The long-necked water miniboss: fast in water, heals there, walks back
 * to water on land and slowly dies without it (verhalten/entity-12.md).
 *
 * <p>Values: {@code SeaMonster_stats} (110/14/8), speed 0.55 in water and 0.25 on land re-set every tick (:81-84,
 * :115-120), {@code experienceValue} 150 (:41), {@code fireResistance} 30 (:42). State: DataWatcher 20
 * {@code attacking} (a {@code Byte} in the bytecode, {@code javap}); no NBT.
 *
 * <p>Not carried over: the {@code RenderInfo} scratch data (:20, :60-111) - {@code ModelSeaMonster} never reads it;
 * {@code getSeaMonsterHealth} (:123-125, no caller); {@code getDropItem} fish (:150-152, shadowed by
 * {@code dropFewItems}); {@code initCreature} (:334-335). The water breathing of {@code canBreatheUnderwater}
 * (:613-615) is the tag {@code minecraft:can_breathe_under_water}.
 */
public class SeaMonster extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code SeaMonsterModel}. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(SeaMonster.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private int hurt_timer;
    private float moveSpeed;
    private final SeaSupport.WaterScan scan = new SeaSupport.WaterScan();

    /** Constructor (:28-52). {@code setSize(1.25f, 2.5f)} is the entity type's size (R9). */
    public SeaMonster(final EntityType<? extends SeaMonster> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.hurt_timer = 0;
        this.moveSpeed = 0.25f;
        // PORT: getNavigator().setAvoidsWater(false) (:40) is a water path malus of 0 (Whale, W06).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 150;
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.SeaMonster_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:54-59) at registration time (R3); armor from {@code getTotalArmorValue} (:105-107). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.SeaMonster_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** The tasks of the constructor (:46-51), same priorities. */
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

    /** {@code entityInit} (:61-75). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 30} (:42). */
    @Override
    protected int getFireImmuneTicks() {
        return 30;
    }

    /** {@code canDespawn} (:77-79). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:81-84), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:86-88). */
    public int mygetMaxHealth() {
        return MobStats.SeaMonster_stats().health();
    }

    /** {@code getTotalArmorValue} (:105-107) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.SeaMonster_stats().defense();
    }

    /** {@code onLivingUpdate} (:113-121), both sides: 0.55 in water, else 0.25. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater()) {
            this.moveSpeed = 0.55f;
        } else {
            this.moveSpeed = 0.25f;
        }
    }

    /** {@code getLivingSound} (:127-132): 1 in 3 on the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return ModSounds.SEAMONSTER_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:134-136). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SEAMONSTER_HIT.get();
    }

    /** {@code getDeathSound} (:138-140). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SEAMONSTER_DEATH.get();
    }

    /** {@code getSoundVolume} (:142-144). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:146-148). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code dropFewItems} (:164-332), spread 2, one block up: a Sea Monster Scale, an item frame, 9..14 fish (world
     * random), then the {@code rand(20)} iron table. PORT: {@code Items.fish} meta 0 is raw cod.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        ArthropodSupport.dropItemRand(this, SeaSupport.stack(ModItems.SEA_MONSTER_SCALE.get()), 2);
        ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.ITEM_FRAME), 2);
        for (int var5 = 9 + this.level().random.nextInt(6), var6 = 0; var6 < var5; ++var6) {
            ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.COD), 2);
        }
        SeaSupport.ironTable(this);
    }

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code interact} (:337-339): false. */
    @Override
    protected InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /** {@code attackEntityAsMob} (:341-355): the vanilla hit, then knockback 0.6 / 0.1 (0.2 for players and dead entities). */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                SeaSupport.legacyKnockback(this, par1Entity, 0.6, 0.1);
            }
            return true;
        }
        return false;
    }

    /**
     * {@code attackEntityFrom} (:357-376): cactus ignored; a hit only counts while {@code hurt_timer <= 0}, which then
     * blocks every further hit for 8 AI ticks; a mob attacker (not another Sea Monster, which returns {@code false})
     * becomes the target with a 1.2 path, even while blocked.
     *
     * <p>PORT: {@code setTarget(e)} (the old-AI {@code entityToAttack}, :372) has no reader with AI tasks and is
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
            this.hurt_timer = 8;
        }
        if (e != null && e instanceof Mob) {
            if (e instanceof SeaMonster) {
                return false;
            }
            this.setTarget((LivingEntity) e);
            this.getNavigation().moveTo(e, 1.2);
        }
        return ret;
    }

    /**
     * {@code updateAITasks} (:462-522), after the goal selectors: count {@code hurt_timer} down; on land with 1 in 25
     * search water (shells up to 11, vertical cap 10) and walk there at 1.33, or with none lose 1 health with 1 in 40
     * and vanish without drops at 0; with 1 in 5 look for a target, face it, within {@code (4 + width/2)^2} set
     * {@code attacking} and bite with (1/4 or 1/5), else walk at 1.0; in water below full health with 1 in 120 heal 1.
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
                if (this.level().random.nextInt(40) == 1) {
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
                if (this.distanceToSqr(e) < (4.0f + e.getBbWidth() / 2.0f) * (4.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.0);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.level().random.nextInt(120) == 1 && this.isInWater() && this.getHealth() < this.mygetMaxHealth()) {
            // playSound("splash", 1.5f, rand * 0.2f + 0.9f): no namespace, silent (R18). The pitch argument still
            // drew from the world random.
            this.level().random.nextFloat();
            this.heal(1.0f);
        }
    }

    /**
     * {@code isSuitableTarget} (:524-549): alive, visible; a player unless creative; never another Sea Monster; any
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
        if (par1EntityLiving instanceof SeaMonster) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /**
     * {@code findSomethingToAttack} (:551-573): nothing with {@code PlayNicely}; a living attack target is kept;
     * otherwise it is cleared and the first suitable entity in {@code expand(16, 4, 16)} is taken.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(16.0, 4.0, 16.0));
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

    /** {@code getAttacking} (:575-577). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:579-581). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:583-611). A spawner allows it; otherwise Y at least 50, night, the
     * monster light test and no other Sea Monster within {@code expand(16, 5, 16)}.
     *
     * <p>PORT: the scan for a "Sea Monster" spawner is {@link MobSpawnType#SPAWNER} (catalogue README 5.9);
     * {@code isValidLightLevel} is {@link LegacyLightLevel}; the whole rule lives here ({@link #checkSpawnRules}
     * answers {@code true}).
     */
    public static boolean checkSeaMonsterSpawnRules(final EntityType<SeaMonster> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        return level.getEntitiesOfClass(SeaMonster.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 5.0, 16.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkSeaMonsterSpawnRules}. */
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
