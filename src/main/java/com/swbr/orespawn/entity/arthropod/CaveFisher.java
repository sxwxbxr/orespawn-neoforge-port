package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
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
 * Port of {@code danger.orespawn.CaveFisher} (CaveFisher.java:15-261): {@code cave_fisher}, the small cave hunter of the
 * Mining dimension and Utopia that attacks players, animals and companions but never a monster (verhalten/entity-05.md).
 * Registered as "CaveFisher" 32/1/false; it has no Overworld {@code addSpawn}, its lists are monster lists.
 *
 * <p>Values: health, attack and defense from {@link MobStats#CaveFisher_stats()} (10/4/4), speed 0.2 re-set every tick
 * (:25, :68), XP 10 (:28), {@code fireResistance} 10 (:29), not fire immune (:30). DataWatcher 20 = attacking; it stays 1
 * while the fisher walks to a target and only a search without result clears it (:159-165). No NBT. Cactus does not hurt
 * it (:169-175).
 *
 * <p>Not carried over: {@code interact} (:137-139) is {@code Mob.mobInteract}'s {@code PASS}; {@code attackEntityAsMob}
 * (:141-143) only calls {@code super}; {@code isAIEnabled}, {@code onLivingUpdate} and {@code canDespawn} are the 1.21.1
 * defaults.
 */
public class CaveFisher extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:49): claw animation of {@code ModelCaveFisher}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(CaveFisher.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side note pad of {@code ModelCaveFisher} (:50-60), never synchronised or saved. */
    private RenderInfo renderdata;
    private float moveSpeed;

    /** {@code CaveFisher(World)} (:21-38) with {@code applyEntityAttributes} (:40-45) and {@code entityInit} (:47-61). */
    public CaveFisher(final EntityType<? extends CaveFisher> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.moveSpeed = 0.2f;
        // setSize(1.35f, 0.75f) (:26) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(false) (:27) is a water path malus of 0 (vanilla default 8, W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 10;
        // fireResistance = 10 (:29) is getFireImmuneTicks; isImmuneToFire = false (:30) is the type default.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.CaveFisher_stats().attack());
        this.setHealth(this.getMaxHealth());
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:40-45): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.2f)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
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
        return MobStats.CaveFisher_stats().health();
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
        return MobStats.CaveFisher_stats().defense();
    }

    /** {@code getLivingSound} (:103-105). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:107-109). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRYO_HURT.get();
    }

    /** {@code getDeathSound} (:111-113). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:115-117). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:119-121). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:123-135), rolled on the world random each call. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(6);
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

    /** {@code updateAITasks} (:145-167): a 1/8 target check with a 1/7-or-1/8 bite at distance squared below 8. */
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

    /** {@code attackEntityFrom} (:169-175): cactus damage is ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /** {@code isSuitableTarget} (:177-213), checks in the original order. */
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
        if (par1EntityLiving instanceof CaveFisher) {
            return false;
        }
        if (ArthropodSupport.isEnderReaper(par1EntityLiving)) {
            return false;
        }
        if (ArthropodSupport.isEnderKnight(par1EntityLiving)) {
            return false;
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

    /** {@code findSomethingToAttack} (:215-232): {@code expand(10, 3, 10)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 10.0, 3.0, 10.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:234-236). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:238-240). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:242-260) - a valid light level and Y at most 50.
     *
     * <p>PORT: the "CaveFisher" spawner scan (x/z -2..1, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9).
     */
    public static boolean checkCaveFisherSpawnRules(final EntityType<CaveFisher> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        return LegacyLightLevel.isValidLightLevel(level, pos, random) && pos.getY() <= 50.0;
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
