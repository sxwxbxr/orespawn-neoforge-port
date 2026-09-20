package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.Ignoreable;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Cricket} (Cricket.java:9-140), id {@code cricket} ("Cricket",
 * OreSpawnMain.java:4109-4113, tracking 32/1/false). A tiny {@code EntityAnimal} that wanders, hops every
 * now and then and chirps; {@code ModelCricket} rubs its legs while {@link #getSinging()} is set.
 *
 * <p>Values: health 3, speed 0.15 re-set every tick, attack 0, hitbox 0.1 (entity type).
 * {@code experienceValue = 1} (:21) is never read ({@code EntityAnimal.getExperiencePoints} is
 * {@code 1 + rand(3)}, inherited). No drops ({@code dropFewItems} empty, :115-116 - equipment still drops,
 * as {@code dropEquipment} did), no breeding.
 *
 * <p>Not carried over: {@code isAIEnabled}; {@code canTriggerWalking = true} (:118-120) is the default.
 */
public class Cricket extends Animal implements Ignoreable {

    /** DataWatcher 20: {@code singing}, 40 after a chirp, read by the model (:37, :44-50). */
    private static final EntityDataAccessor<Integer> DATA_SINGING =
            SynchedEntityData.defineId(Cricket.class, EntityDataSerializers.INT);

    public double moveSpeed;
    private int singing;
    private int jumpcount;

    /** Constructor (:15-25). {@code setSize(0.1f, 0.1f)} is the entity type's size. */
    public Cricket(final EntityType<? extends Cricket> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.15000000596046448;
        this.singing = 0;
        this.jumpcount = 0;
        // getNavigator().setAvoidsWater(true) (:22): water is impassable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, LegacyPanic.legacyPanic(this, 1.4));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 8, 1.0));
    }

    /** {@code applyEntityAttributes} (:27-33). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15000000596046448)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code entityInit} (:35-38). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SINGING, 0);
    }

    /** {@code canDespawn} (:40-42): {@code !isNoDespawnRequired()}; {@code Animal} would answer {@code false}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code getSinging} (:44-46). */
    public int getSinging() {
        return this.entityData.get(DATA_SINGING);
    }

    /** {@code setSinging} (:48-50). */
    public void setSinging(final int par1) {
        this.entityData.set(DATA_SINGING, par1);
    }

    /**
     * {@code jumpAround} (:52-61): up 0.55..0.9, sideways 0.3..0.55 in a random direction, all on the world
     * random, and {@code isAirBorne}.
     *
     * <p>PORT: {@code posY += 0.25} (:54) is left out. In 1.7.10 {@code Entity.moveEntity} rebuilt
     * {@code posY} from the bounding box at its end ({@code sa.d}, pc 1320-1340: {@code boundingBox.minY +
     * yOffset - ySize}), and the box never saw the 0.25, so the next move undid it - the write only shifted
     * the reported position for the rest of one tick. 1.21.1's {@code Entity.move} continues from
     * {@code getY()} (Entity.java:652), so the same write would be a real 0.25-block lift through any ceiling
     * (R18 case 3).
     */
    private void jumpAround() {
        final Vec3 motion = this.getDeltaMovement();
        final double motionY = motion.y + (0.55f + Math.abs(this.level().random.nextFloat() * 0.35f));
        final float f = 0.3f + Math.abs(this.level().random.nextFloat() * 0.25f);
        final float d = (float) (this.level().random.nextFloat() * 3.141592653589793 * 2.0);
        final double motionX = motion.x + f * Math.sin(d);
        final double motionZ = motion.z + f * Math.cos(d);
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
    }

    /**
     * {@code onUpdate} (:63-80): speed base first; after the move, on the server, count the chirp down and
     * hop with 1 in 50 once the 50-tick pause is over.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
        if (!this.level().isClientSide) {
            if (this.singing != 0) {
                --this.singing;
                if (this.singing <= 0) {
                    this.setSinging(0);
                }
            }
            if (this.jumpcount > 0) {
                --this.jumpcount;
            }
            if (this.jumpcount == 0 && this.level().random.nextInt(50) == 1) {
                this.jumpAround();
                this.jumpcount = 50;
            }
        }
    }

    /** {@code mygetMaxHealth} (:86-88). */
    public int mygetMaxHealth() {
        return 3;
    }

    /**
     * {@code getLivingSound} (:90-99), including its side effect: on the server half the calls are silent,
     * the other half start 40 ticks of singing. The getter is only reached through
     * {@code Mob.playAmbientSound} (Mob.java:275-277), the same callers as 1.7.10's {@code playLivingSound}
     * (living-sound timer, spawn egg), so the side effect stays where the original had it.
     */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (!this.level().isClientSide) {
            if (this.level().random.nextInt(2) == 0) {
                return null;
            }
            this.setSinging(this.singing = 40);
        }
        return ModSounds.CRICKET.get();
    }

    /** {@code getHurtSound} (:101-103). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:105-107). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code getSoundVolume} (:109-111). */
    @Override
    protected float getSoundVolume() {
        return 0.7f;
    }

    /** {@code playStepSound} (:113-114), empty. */
    @Override
    protected void playStepSound(final BlockPos pos, final BlockState block) {
    }

    /** {@code fall} and {@code updateFallState} (:122-126), both empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code createChild} (:128-130). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }

    /** {@code getCanSpawnHere} (:132-134): Y 30 and above and at most 5 crickets in {@code expand(20, 10, 20)}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 30.0 && findBuddies(level, this.getBoundingBox()) <= 5;
    }

    /** PORT: the collision and liquid half of 1.7.10's {@code getCanSpawnHere}, which the override replaced. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code getCanSpawnHere} (:132-134) as the placement predicate; the count uses the box the cricket will
     * occupy at the block centre, where natural spawning places it.
     */
    public static boolean checkCricketSpawnRules(final EntityType<Cricket> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 30.0
                && findBuddies(level, type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) <= 5;
    }

    /** {@code findBuddies} (:136-139). */
    private static int findBuddies(final LevelAccessor level, final AABB boundingBox) {
        final List<Cricket> var5 = level.getEntitiesOfClass(Cricket.class, boundingBox.inflate(20.0, 10.0, 20.0));
        return var5.size();
    }
}
