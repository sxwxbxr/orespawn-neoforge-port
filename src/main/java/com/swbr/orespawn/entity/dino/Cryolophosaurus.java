package com.swbr.orespawn.entity.dino;

import com.swbr.orespawn.entity.waterdragon.GammaMetroid;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.critter.CritterSupport;
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.rock.RockBase;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Cryolophosaurus} (Cryolophosaurus.java:12-211): {@code cryolophosaurus}, a small
 * hostile dinosaur ("Cryolophosaurus", OreSpawnMain.java:3399, tracking 64/1/false). {@code EntityMob} is
 * {@link Monster}.
 *
 * <p>Values: health, attack and armor from {@code Cryolophosaurus_stats} (10/3/1 by default), speed 0.25 re-set every
 * tick (:20, :67), hitbox 0.75 x 0.75 (:21, entity type), {@code experienceValue = 10} (:23, {@code xpReward}),
 * {@code fireResistance = 10} (:24). No DataWatcher entry, no NBT.
 *
 * <p>Not carried over: {@code entityInit} (:42-44) and {@code onLivingUpdate} (:62-64) only call super;
 * {@code initCreature} (:108-109) is empty; {@code isAIEnabled}; {@code interact} returning {@code false} (:111-113) is
 * the default {@code PASS}.
 */
public class Cryolophosaurus extends Monster implements LegacyArmor {

    private final GenericTargetSorter TargetSorter;
    private float moveSpeed;

    /** {@code Cryolophosaurus(World)} (:17-33). Goals on both sides, as the 1.7.10 constructor added them. */
    public Cryolophosaurus(final EntityType<? extends Cryolophosaurus> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.25f;
        // setSize(0.75f, 0.75f) (:21) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:22) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 10;
        // fireResistance = 10 (:24) is getFireImmuneTicks().
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // EntityAIPanic(this, 1.35) (:26): the 1.7.10 rule (revenge target or burning), shared W06 port.
        this.goalSelector.addGoal(1, LegacyPanic.legacyPanic(this, 1.350000023841858));
        // PORT: EntityAIMoveThroughVillage(this, 1.0, false) (:27) -> MoveThroughVillageGoal over village POIs, see Alosaurus.
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(3, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) (:31) did not check sight; HurtByTargetGoal forgets an unseen
        // target after 60 ticks (Lizard precedent, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        DinoSupport.applyStats(this, MobStats.Cryolophosaurus_stats());
    }

    /** {@code applyEntityAttributes} (:35-40) with the manifest defaults; the constructor applies the configured stats. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 1.0);
    }

    /** {@code fireResistance = 10} (:24). */
    @Override
    protected int getFireImmuneTicks() {
        return 10;
    }

    /** {@code canDespawn} (:46-48): {@code !isNoDespawnRequired()}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code mygetMaxHealth} (:50-52). */
    public int mygetMaxHealth() {
        return MobStats.Cryolophosaurus_stats().health();
    }

    /** {@code getTotalArmorValue} (:54-56) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Cryolophosaurus_stats().defense();
    }

    /** {@code onUpdate} (:66-69), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code getLivingSound} (:71-76). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(6) == 0) {
            return ModSounds.CRYO_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:78-80). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRYO_HURT.get();
    }

    /** {@code getDeathSound} (:82-84). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:86-88). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:90-92). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:94-106): {@code rand(10)} on the world random - raw chicken, uranium or titanium nugget, else nothing. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(10);
        if (i == 0) {
            return Items.CHICKEN;
        }
        if (i == 1) {
            return ModItems.URANIUM_NUGGET.get();
        }
        if (i == 2) {
            return ModItems.TITANIUM_NUGGET.get();
        }
        return null;
    }

    /** {@code getDropItem} through the vanilla {@code EntityLiving.dropFewItems}; equipment afterwards (super). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, this.getDropItem());
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code updateAITasks} (:115-132): the revenge target is forgotten with 1 in 200; with 1 in 5 (world random) the
     * nearest suitable target is walked to at 1.25 and, within squared distance 5, bitten when
     * {@code rand(12) == 0 || rand(14) == 1} on the entity's own random. No attack override: the plain
     * {@code EntityMob} bite ({@code Mob.doHurtTarget}).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
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
     * {@code isSuitableTarget} (:134-187). No {@code isIgnoreable} test here: the list names its own exclusions, sight
     * first.
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
        if (par1EntityLiving instanceof Alosaurus) {
            return false;
        }
        if (par1EntityLiving instanceof TRex) {
            return false;
        }
        if (par1EntityLiving instanceof Cryolophosaurus) {
            return false;
        }
        if (par1EntityLiving instanceof Ghost) {
            return false;
        }
        if (par1EntityLiving instanceof GhostSkelly) {
            return false;
        }
        // PORT: instanceof CaveFisher (:162) by registry id - W07 arthropod porter (DinoSupport.isOreSpawnType).
        if (DinoSupport.isOreSpawnType(par1EntityLiving, "cave_fisher")) {
            return false;
        }
        if (par1EntityLiving instanceof GammaMetroid) { // :165
            return false;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return false;
        }
        if (par1EntityLiving instanceof Firefly) {
            return false;
        }
        if (par1EntityLiving instanceof EntityMosquito) {
            return false;
        }
        if (par1EntityLiving instanceof RockBase) {
            return false;
        }
        if (par1EntityLiving instanceof Player && DinoSupport.isCreativePlayer(par1EntityLiving)) {
            return false;
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:189-206): {@code PlayNicely} off, every living entity in {@code expand(9, 2, 9)}, sorted. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(9.0, 2.0, 9.0)));
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

    /**
     * Spawn predicate for {@code RegisterSpawnPlacementsEvent}: the entity-free half of {@code getCanSpawnHere} (:208-210),
     * night or {@code posY <= 50}. There is no spawner branch in this class.
     *
     * <p>PORT: {@code isValidLightLevel} rolls once, in {@link #checkSpawnRules} (see Alosaurus).
     */
    public static boolean checkCryolophosaurusSpawnRules(final EntityType<Cryolophosaurus> type, final ServerLevelAccessor level,
                                                         final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return !HerbivoreSupport.isDaytime(level.getLevel()) || pos.getY() <= 50.0;
    }

    /** {@code getCanSpawnHere} (:208-210): {@code isValidLightLevel() && (!isDaytime() || posY <= 50)}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return LegacyLightLevel.isValidLightLevel(level, BlockPos.containing(this.getX(), this.getBoundingBox().minY, this.getZ()),
                this.random) && (!HerbivoreSupport.isDaytime(this.level()) || this.getY() <= 50.0);
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
