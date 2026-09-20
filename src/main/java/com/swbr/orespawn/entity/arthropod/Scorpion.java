package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
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
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Scorpion} (Scorpion.java:15-279): {@code scorpion}, the small desert and dark forest
 * enemy that hunts players, animals, spiders, creepers and Velocity Raptors and serves the Emperor Scorpion
 * (verhalten/entity-11.md). Registered as "Scorpion" 32/1/false, {@code EntityMob} in an {@code ambient} spawn list.
 *
 * <p>Values: health, attack and defense from {@link MobStats#Scorpion_stats()} (15/4/10), speed 0.2 re-set every tick
 * (:25, :69), XP 10 (:28), {@code fireResistance} 100 (:29), not fire immune (:30). DataWatcher 20 = attacking (claw and
 * tail animation), no NBT of its own. Cactus does not hurt it (:173-179).
 *
 * <p>Not carried over: {@code interact} (:138-140) returns {@code false}, which is {@code Mob.mobInteract}'s
 * {@code PASS}; {@code attackEntityAsMob} (:142-144) only calls {@code super}; {@code isAIEnabled} (:96-98) and
 * {@code onLivingUpdate} (:100-102) are the 1.21.1 defaults; {@code canDespawn} (:64-66) = {@code !isNoDespawnRequired()}
 * is {@code Mob.checkDespawn}'s persistence test.
 */
public class Scorpion extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:50): 1 while a target is in reach, read by {@code ModelScorpion}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Scorpion.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side note pad of {@code ModelScorpion} (:51-61), never synchronised or saved. */
    private RenderInfo renderdata;
    private float moveSpeed;

    /** {@code Scorpion(World)} (:21-39) with {@code applyEntityAttributes} (:41-46) and {@code entityInit} (:48-62). */
    public Scorpion(final EntityType<? extends Scorpion> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.moveSpeed = 0.2f;
        // setSize(0.85f, 0.55f) (:26) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:27) is a water path malus of -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 10;
        // fireResistance = 100 (:29) is getFireImmuneTicks; isImmuneToFire = false (:30) is the type default.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Scorpion_stats().attack());
        this.setHealth(this.getMaxHealth());
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(1.0, false) walked to the doors of a 1.7.10 village; villages are POI-based in
        // 1.21.1, so the vanilla MoveThroughVillageGoal (not only at night, POI distance 4 as the zombie, no doors) stands in.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:41-46): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.2f)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    /** {@code entityInit} (:48-50). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 100} (:29). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:68-71), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:73-75). */
    public int mygetMaxHealth() {
        return MobStats.Scorpion_stats().health();
    }

    /** {@code getRenderInfo} (:77-79). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:81-90). */
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

    /** {@code getTotalArmorValue} (:92-94), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Scorpion_stats().defense();
    }

    /** {@code getLivingSound} (:104-106). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:108-110). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SCORPION_HIT.get();
    }

    /** {@code getDeathSound} (:112-114). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:116-118). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:120-122). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:124-136), rolled on the world random each call. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(10);
        if (i == 0) {
            return Items.GOLD_NUGGET;
        }
        if (i == 1) {
            return ModItems.URANIUM_NUGGET.get();
        }
        if (i == 2) {
            return ModItems.TITANIUM_NUGGET.get();
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

    /** {@code updateAITasks} (:146-171): after the goals, a 1/6 target check with a 1/5-or-1/6 bite in reach. */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(6) == 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                if (this.distanceToSqr(e) < 9.0) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(5) == 0 || this.level().random.nextInt(6) == 1) {
                        this.doHurtTarget(e);
                        if (!this.level().isClientSide && this.level().random.nextInt(3) == 1) {
                            ArthropodSupport.playSoundAtEntity(e, ModSounds.SCORPION_ATTACK.get(), 0.75f, 1.5f);
                        }
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.2);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /** {@code attackEntityFrom} (:173-179): cactus damage is ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /** {@code isSuitableTarget} (:181-232), checks in the original order. */
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
        if (par1EntityLiving instanceof Ghost) {
            return false;
        }
        if (par1EntityLiving instanceof GhostSkelly) {
            return false;
        }
        if (par1EntityLiving instanceof VelocityRaptor) {
            return true;
        }
        if (par1EntityLiving instanceof Spider) {
            return true;
        }
        if (par1EntityLiving instanceof CaveSpider) {
            return true;
        }
        if (par1EntityLiving instanceof Scorpion) {
            return false;
        }
        if (par1EntityLiving instanceof EmperorScorpion) {
            return false;
        }
        if (par1EntityLiving instanceof Creeper) {
            return true;
        }
        if (par1EntityLiving instanceof Monster) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:234-251): {@code expand(8, 3, 8)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 8.0, 3.0, 8.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:253-255). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:257-259). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:261-278) - a valid light level, and night or Y at most 50.
     *
     * <p>PORT: the original allowed any spawn with a "Scorpion" spawner in x/z -3..2, y 0..4; that is
     * {@link MobSpawnType#SPAWNER} here (catalogue 5.9). A natural spawn next to such a spawner now needs the light
     * rule, and a spawner-spawned scorpion outside the scan box no longer does. {@code (int)} casts are the block
     * position (R20).
     */
    public static boolean checkScorpionSpawnRules(final EntityType<Scorpion> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        return LegacyLightLevel.isValidLightLevel(level, pos, random)
                && (!InsectSupport.isDaytime(level.getLevel()) || pos.getY() <= 50.0);
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
