package com.swbr.orespawn.entity.monster;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityLunaMoth;
import com.swbr.orespawn.entity.portal.EntityAnt;
import com.swbr.orespawn.registry.ModSounds;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.LeafMonster} (LeafMonster.java:15-257), id {@code leaf_monster} ("Leaf Monster",
 * OreSpawnMain.java:3775, tracking 64/1/false). An ambush monster that looks like a pile of leaf blocks: while it does
 * not attack it sits on the block centre, facing a multiple of 90 degrees (verhalten/entity-09.md).
 *
 * <p>Values: {@code LeafMonster_stats} (6/2/1), speed 0.25 re-set every tick (:23, :81), {@code experienceValue} 5
 * (:26), {@code fireResistance} 0 (:27), fall damage {@code ceil(d - 3)} capped at 2 (:66-78). Tasks: swimming, panic,
 * revenge only - it never wanders (:28-30). State: DataWatcher 20 {@code attacking}, a {@code Byte} in the bytecode
 * ({@code javap}). No NBT.
 */
public class LeafMonster extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code LeafMonsterModel} (pile or upright figure). */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(LeafMonster.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    /** Set when {@link #tick()} wrote the snapped position without the box; see {@link #move}. */
    private boolean snapped;

    /** {@code LeafMonster(World)} (:20-32). {@code setSize(1.0f, 2.5f)} (:24) is the entity type's size (R9). */
    public LeafMonster(final EntityType<? extends LeafMonster> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.25f;
        // PORT: getNavigator().setAvoidsWater(true) (:25) - water malus -1 (Chipmunk, W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 5;
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, LegacyPanic.legacyPanic(this, 1.350000023841858));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.LeafMonster_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:34-39) at registration time (R3), armor from {@code getTotalArmorValue} (:58-60). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.LeafMonster_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:41-44). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code getAttacking} (:46-48). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:50-52). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /** {@code fireResistance = 0} (:27). */
    @Override
    protected int getFireImmuneTicks() {
        return 0;
    }

    /** {@code mygetMaxHealth} (:54-56). */
    public int mygetMaxHealth() {
        return MobStats.LeafMonster_stats().health();
    }

    /** {@code getTotalArmorValue} (:58-60) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.LeafMonster_stats().defense();
    }

    /**
     * {@code fall} (:66-78): {@code ceil(distance - 3)}; above 2 the big fall sound and a cap of 2, otherwise the small
     * one; then fall damage. Nothing else of the vanilla fall (multiplier, landing sound) runs.
     */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        float i = (float) Mth.ceil(par1 - 3.0f);
        if (i > 0.0f) {
            if (i > 2.0f) {
                this.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0f, 1.0f);
                i = 2.0f;
            } else {
                this.playSound(SoundEvents.GENERIC_SMALL_FALL, 1.0f, 1.0f);
            }
            this.hurt(this.damageSources().fall(), i);
            return true;
        }
        return false;
    }

    /**
     * {@code onUpdate} (:80-109), both sides: speed, the vanilla tick, then - while not attacking - the camouflage. The
     * position is cut to whole blocks with {@code (int)} and moved 0.5 away from zero on each positive or negative axis
     * ({@code posY} only cut); pitch 0; yaw and head yaw {@code (int) yawHead / 90 * 90}.
     *
     * <p>Kept 1:1 (R18): the {@code (int)} casts and the sign branches are one rounding rule, which lands on the column
     * centre except for x or z in (-1, 1), where neither branch fires and the monster sits on 0.0. R20 is not applied:
     * the casts are a position snap whose branches compensate the truncation, not a block lookup.
     *
     * <p>PORT: 1.7.10 wrote {@code posX/posY/posZ} without the bounding box, and its {@code moveEntity} rebuilt the
     * position from the box on the next move - the snap was visible (tracker, renderer, other AIs) but never moved the
     * physical box. The fields are written with {@code setPosRaw}; 1.21.1's {@code move} continues from the position
     * rather than the box, so {@link #move} restores the box position first, as {@code moveEntity} did.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.getAttacking() == 0) {
            int px = (int) this.getX();
            final int py = (int) this.getY();
            final int pz = (int) this.getZ();
            double posX = px;
            final double posY = py;
            double posZ = pz;
            if (posX > 0.0) {
                posX += 0.5;
            }
            if (posZ > 0.0) {
                posZ += 0.5;
            }
            if (posX < 0.0) {
                posX -= 0.5;
            }
            if (posZ < 0.0) {
                posZ -= 0.5;
            }
            this.setPosRaw(posX, posY, posZ);
            this.snapped = true;
            this.setXRot(0.0f);
            px = (int) this.getYHeadRot();
            px /= 90;
            final float n = (float) (px * 90);
            this.setYHeadRot(n);
            this.setYRot(n);
        }
    }

    /**
     * 1.7.10 {@code Entity.moveEntity} took the position from the bounding box ({@code posX = (minX + maxX) / 2},
     * {@code posY = minY}); after a snap in {@link #tick()} the box still holds the unsnapped position.
     */
    @Override
    public void move(final MoverType type, final Vec3 pos) {
        if (this.snapped) {
            this.snapped = false;
            final AABB bb = this.getBoundingBox();
            this.setPosRaw((bb.minX + bb.maxX) / 2.0, bb.minY, (bb.minZ + bb.maxZ) / 2.0);
        }
        super.move(type, pos);
    }

    /** {@code getLivingSound} (:111-113): none. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:115-117). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LEAVES_HIT.get();
    }

    /** {@code getDeathSound} (:119-121). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LEAVES_DEATH.get();
    }

    /** {@code getSoundVolume} (:123-125). */
    @Override
    protected float getSoundVolume() {
        return 0.65f;
    }

    /** {@code getSoundPitch} (:127-129). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getDropItem} (:131-140): oak log ({@code Blocks.log} meta 0), oak leaves ({@code Blocks.leaves} meta 0) or rotten flesh. */
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(3);
        if (i == 0) {
            return Items.OAK_LOG;
        }
        if (i == 1) {
            return Items.OAK_LEAVES;
        }
        return Items.ROTTEN_FLESH;
    }

    /**
     * The inherited 1.7.10 {@code EntityLiving.dropFewItems}: {@code getDropItem} once, 0..2 of it plus
     * {@code rand(looting + 1)}; then the equipment roll (R10).
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code updateAITasks} (:142-164), after the selectors: 1 in 100 forget the revenge target; 1 in 4 look for prey -
     * with one face it, set {@code attacking}, walk at 1.25 and within squared 5 hit with (1/8 or 1/10); without one
     * clear {@code attacking} and hide again.
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(100) == 1) {
            this.setLastHurtByMob((LivingEntity) null);
        }
        if (this.level().random.nextInt(4) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                this.setAttacking(1);
                this.getNavigation().moveTo(e, 1.25);
                if (this.distanceToSqr(e) < 5.0 && (this.random.nextInt(8) == 0 || this.random.nextInt(10) == 1)) {
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /** {@code isSuitableTarget} (:166-195): visible and living; ants, butterflies, moths and non-creative players only. */
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
        if (par1EntityLiving instanceof EntityAnt) {
            return true;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return true;
        }
        if (par1EntityLiving instanceof EntityLunaMoth) {
            return true;
        }
        if (par1EntityLiving instanceof Player p) {
            if (!p.getAbilities().instabuild) {
                return true;
            }
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:197-214): the first suitable entity in {@code expand(4, 6, 4)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0, 6.0, 4.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:216-247). A spawner allows it; otherwise the monster light test, night,
     * in the Islands dimension only y 20 or below, elsewhere y 50 or above, and at most 4 Leaf Monsters within
     * {@code expand(20, 10, 20)}.
     *
     * <p>PORT: spawner scan → {@link MobSpawnType#SPAWNER} (README 5.9); {@code isValidLightLevel} →
     * {@link LegacyLightLevel#isValidLightLevel} (not {@code Monster.isDarkEnoughToSpawn}, which adds the dimension's
     * block-light limit and light test; W07 review, as Molenoid); the whole rule lives here ({@link #checkSpawnRules} answers {@code true}).
     * {@code findBuddies} counted in the box of the not-yet-added entity: the type's spawn box at the position.
     */
    public static boolean checkLeafMonsterSpawnRules(final EntityType<LeafMonster> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (MonsterSupport.isDangerDimension(level.getLevel())) {
            if (pos.getY() > 20.0) {
                return false;
            }
        } else if (pos.getY() < 50.0) {
            return false;
        }
        return level.getEntitiesOfClass(LeafMonster.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 10.0, 20.0)).size() <= 4;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkLeafMonsterSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code canDespawn} (:254-256). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }
}
