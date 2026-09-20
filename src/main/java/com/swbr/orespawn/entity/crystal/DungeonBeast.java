package com.swbr.orespawn.entity.crystal;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.herbivore.Peacock;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.DungeonBeast} (DungeonBeast.java:15-295): {@code dungeon_beast}, the horned
 * quadruped of the Crystal dungeons (verhalten/entity-06.md). {@code EntityMob} with wander, watch and look-idle tasks;
 * the target search and the bite are written by hand in the AI step.
 *
 * <p>Values: health, attack and defense from {@link MobStats#DungeonBeast_stats()} (65/12/6), speed 0.29 re-set every
 * tick (:25, :68), XP 60 (:28), {@code fireResistance} 10 (:29), not fire immune (:30). DataWatcher 20 = attacking, read
 * by {@code ModelDungeonBeast} for the tail and the jaws. No NBT. Suffocation and cactus do not hurt it (:169-178).
 *
 * <p>Not carried over: {@code interact} (:137-139) is {@code Mob.mobInteract}'s {@code PASS}; {@code attackEntityAsMob}
 * (:141-143) and {@code onLivingUpdate} (:99-101) only call {@code super}; {@code getLivingSound} (:103-105) is the
 * default {@code null}; {@code isAIEnabled} and {@code canDespawn} (:63-65, only the persistence test) are the 1.21.1
 * defaults.
 */
public class DungeonBeast extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:49): the tail swings hard and the jaws snap every cycle while it is 1. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(DungeonBeast.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side note pad of {@code ModelDungeonBeast} (:18, :50-60), never synchronised or saved. */
    private RenderInfo renderdata;
    private float moveSpeed;

    /** {@code DungeonBeast(World)} (:21-38) with {@code applyEntityAttributes} (:40-45) and {@code entityInit} (:47-61). */
    public DungeonBeast(final EntityType<? extends DungeonBeast> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.moveSpeed = 0.29f;
        // setSize(1.15f, 1.1f) (:26) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:27) is a water path malus of -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 60;
        // fireResistance = 10 (:29) is getFireImmuneTicks; isImmuneToFire = false (:30) is the type default.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.DungeonBeast_stats().attack());
        this.setHealth(this.getMaxHealth());
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        // PORT: EntityAISwimming is FloatGoal, as in every W01-W07 mob (W07 "Offen": no 1.7.10 port in entity.ai).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06), CaveFisher (W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:40-45): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 65.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.29f)
                .add(Attributes.ATTACK_DAMAGE, 12.0);
    }

    /** {@code entityInit} (:47-49). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 10} (:29). */
    @Override
    protected int getFireImmuneTicks() {
        return 10;
    }

    /** {@code onUpdate} (:67-70), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:72-74). */
    public int mygetMaxHealth() {
        return MobStats.DungeonBeast_stats().health();
    }

    /** {@code getRenderInfo} (:76-78). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:80-89). */
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

    /** {@code getTotalArmorValue} (:91-93), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.DungeonBeast_stats().defense();
    }

    /** {@code getLivingSound} (:103-105): none. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:107-109). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DBHIT.get();
    }

    /** {@code getDeathSound} (:111-113). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DBDEAD.get();
    }

    /** {@code getSoundVolume} (:115-117). */
    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    /** {@code getSoundPitch} (:119-121). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:123-135): one roll of the world random per call; {@code Blocks.log} meta 0 is oak. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(4);
        if (i == 1) {
            return ModItems.CRYSTAL_PINK_INGOT.get();
        }
        if (i == 2) {
            return ModItems.CRYSTAL_APPLE.get();
        }
        if (i == 3) {
            return Items.OAK_LOG;
        }
        return null;
    }

    /**
     * 1.7.10 {@code onDeath}: the inherited {@code EntityLiving.dropFewItems} with {@link #getDropItem()} (0..2 plus
     * {@code rand(looting + 1)}), then {@code dropEquipment} ({@code Mob.dropCustomDeathLoot}).
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code updateAITasks} (:145-167): after the goals, a 1/8 target check; below distance squared 8 {@code attacking = 1}
     * and a bite with 1/7-or-1/8, otherwise a path at 1.2; without a target {@code attacking = 0}.
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

    /** {@code attackEntityFrom} (:169-178): suffocation returns {@code false}, cactus is ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return ret;
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /** {@code isSuitableTarget} (:180-228), checks in the original order. */
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
        if (CrystalSupport.isOreSpawnType(par1EntityLiving, "rat")) {
            return false;
        }
        if (par1EntityLiving instanceof DungeonBeast) {
            return false;
        }
        if (par1EntityLiving instanceof Rotator) {
            return false;
        }
        if (par1EntityLiving instanceof Peacock) {
            return false;
        }
        if (CrystalSupport.isOreSpawnType(par1EntityLiving, "irukandji")) {
            return false;
        }
        if (CrystalSupport.isOreSpawnType(par1EntityLiving, "skate")) {
            return false;
        }
        if (par1EntityLiving instanceof Whale) {
            return false;
        }
        if (par1EntityLiving instanceof Flounder) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:230-247): {@code expand(16, 3, 16)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 3.0, 16.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:249-251). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:253-255). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:257-294) - a valid light level; in the Crystal dimension additionally Y
     * 25..28 and at least 6 air blocks in the 3x3 layer above the feet.
     *
     * <p>PORT: the "Dungeon Beast" spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9).
     * {@code isAir()} also accepts cave and void air, which 1.7.10 generated as plain air.
     */
    public static boolean checkDungeonBeastSpawnRules(final EntityType<DungeonBeast> type, final ServerLevelAccessor level,
                                                      final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        int sc = 0;
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (level.getLevel().dimension().equals(WorldProviderOreSpawn5.DIMENSION)) {
            if (pos.getY() > 28.0 || pos.getY() < 25.0) {
                return false;
            }
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
        }
        return true;
    }

    /** The override replaced {@code EntityCreature}'s path-weight test, which 1.21.1 asks here; the predicate holds the rule. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
