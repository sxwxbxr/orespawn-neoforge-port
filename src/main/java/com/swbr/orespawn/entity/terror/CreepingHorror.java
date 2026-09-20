package com.swbr.orespawn.entity.terror;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.ender.EnderReaper;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.monster.LeafMonster;
import com.swbr.orespawn.entity.rock.RockBase;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
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
 * Port of {@code danger.orespawn.CreepingHorror} (CreepingHorror.java:12-205), id {@code creeping_horror} ("Creeping
 * Horror", 64/1/false): a nocturnal scorpion-like crawler that attacks almost anything and falls apart by day
 * (verhalten/entity-06.md).
 *
 * <p>Values: {@code CreepingHorror_stats} (10/3/2), speed 0.25 re-set every tick (:15, :59), XP 5 (:23),
 * {@code fireResistance} 10 (:24). No DataWatcher, no NBT. Despawns only by day (:202-204).
 */
public class CreepingHorror extends Monster implements LegacyArmor {

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;

    /** {@code CreepingHorror(World)} (:17-33) with {@code applyEntityAttributes} (:35-40). */
    public CreepingHorror(final EntityType<? extends CreepingHorror> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.25f;
        // setSize(0.75f, 0.5f) (:21) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:22) - water malus -1 (Chipmunk, W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 5;
        // fireResistance = 10 (:24) is getFireImmuneTicks.
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // EntityAIPanic(1.35): while a revenge target is set or it burns (LegacyPanic.legacyPanic).
        this.goalSelector.addGoal(1, LegacyPanic.legacyPanic(this, 1.350000023841858));
        // PORT (R18 case 3): EntityAIMoveThroughVillage(1.0, false) is vanilla's POI-based MoveThroughVillageGoal, the
        // same reading as MonsterSupport.legacyMoveThroughVillage (W07, package-private there): not nocturnal, visited
        // distance 4, no doors.
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(3, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.CreepingHorror_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:35-40) at registration time (R3), armor from {@code getTotalArmorValue} (:50-52). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.CreepingHorror_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code fireResistance = 10} (:24). */
    @Override
    protected int getFireImmuneTicks() {
        return 10;
    }

    /** {@code mygetMaxHealth} (:46-48). */
    public int mygetMaxHealth() {
        return MobStats.CreepingHorror_stats().health();
    }

    /** {@code getTotalArmorValue} (:50-52), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.CreepingHorror_stats().defense();
    }

    /**
     * {@code onUpdate} (:58-72): speed re-set, the vanilla tick, then - unless persistent - with a day time of at most
     * 11000 a 1/500 chance per tick to vanish without death or drops ({@code setDead}).
     *
     * <p>PORT (R18 case 4): the vanishing runs on the server only. 1.7.10 ran it on the client as well, where
     * {@code isNoDespawnRequired} was never synchronised (always {@code false}) and the client's own roll removed the
     * entity locally while the server kept it - an invisible Creeping Horror that still attacks.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.isPersistenceRequired()) {
            return;
        }
        if (this.level().isClientSide) {
            return;
        }
        long t = this.level().getDayTime();
        t %= 24000L;
        if (t > 11000L) {
            return;
        }
        if (this.level().random.nextInt(500) == 1) {
            this.discard();
        }
    }

    /** {@code getLivingSound} (:74-76). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CREEPINGHORROR_LIVING.get();
    }

    /** {@code getHurtSound} (:78-80). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CREEPINGHORROR_HIT.get();
    }

    /** {@code getDeathSound} (:82-84). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CREEPINGHORROR_DEAD.get();
    }

    /** {@code getSoundVolume} (:86-88). */
    @Override
    protected float getSoundVolume() {
        return 0.65f;
    }

    /** {@code getSoundPitch} (:90-92). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:94-103): one roll on the world random - rotten flesh, bone or string. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(3);
        if (i == 0) {
            return Items.ROTTEN_FLESH;
        }
        if (i == 1) {
            return Items.BONE;
        }
        return Items.STRING;
    }

    /** 1.7.10 {@code onDeath}: the inherited {@code EntityLiving.dropFewItems} with {@link #getDropItem()} (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code updateAITasks} (:105-122), after the goals: 1/200 forget the revenge target; 1/5 look for a target, walk to
     * it at 1.25 and, within squared 5, hit with {@code rand(12) == 0 || rand(14) == 1} (vanilla
     * {@code attackEntityAsMob} = {@code doHurtTarget}).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob((LivingEntity) null);
        }
        if (this.level().random.nextInt(5) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.getNavigation().moveTo(e, 1.25);
                if (this.distanceToSqr(e) < 5.0 && (this.random.nextInt(12) == 0 || this.random.nextInt(14) == 1)) {
                    this.doHurtTarget(e);
                }
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:124-177): every visible living entity except the listed kinds and creative players -
     * farm animals included.
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
        if (par1EntityLiving instanceof CreepingHorror) {
            return false;
        }
        if (par1EntityLiving instanceof RockBase) {
            return false;
        }
        if (par1EntityLiving instanceof EnderReaper) {
            return false;
        }
        if (par1EntityLiving instanceof LeafMonster) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "dragon")) {
            return false;
        }
        if (par1EntityLiving instanceof TerribleTerror) {
            return false;
        }
        if (par1EntityLiving instanceof LurkingTerror) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "nightmare")) {
            return false;
        }
        if (par1EntityLiving instanceof Firefly) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "island")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "island_too")) {
            return false;
        }
        if (TerrorSupport.isCreative(par1EntityLiving)) {
            return false;
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:179-196): {@code expand(16, 4, 16)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 4.0, 16.0, t -> this.isSuitableTarget(t, false));
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:198-200): the monster light test, night, and the Chaos dimension or y at
     * most 15. No spawner branch in this class. PORT: {@link LegacyLightLevel#isValidLightLevel} with the spawner's random.
     */
    public static boolean checkCreepingHorrorSpawnRules(final EntityType<CreepingHorror> type, final ServerLevelAccessor level,
                                                        final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return LegacyLightLevel.isValidLightLevel(level, pos, random) && !InsectSupport.isDaytime(level.getLevel())
                && (TerrorSupport.isChaos(level.getLevel()) || pos.getY() <= 15.0);
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkCreepingHorrorSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code canDespawn} (:202-204): not persistent and daytime. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && InsectSupport.isDaytime(this.level());
    }
}
