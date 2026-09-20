package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.CliffRacer} (CliffRacer.java:10-156), id {@code cliff_racer} ("Cliff Racer",
 * OreSpawnMain.java:3667-3671, tracking 32/1/false). A passive {@code EntityAnimal} flyer of the Islands and
 * Chaos dimensions, waypoint flight without navigation (catalogue 5.3).
 *
 * <p>Values: health 5, speed 0.33, attack 1.0 (never used), hitbox 0.75 x 0.5 (entity type).
 * {@code experienceValue = 5} (:20) is never read: {@code EntityAnimal.getExperiencePoints} gives
 * {@code 1 + rand(3)}, inherited from {@code Animal}. No DataWatcher, no NBT, no breeding.
 *
 * <p>Not carried over: {@code getNavigator().setAvoidsWater(false)} (:19) is the default;
 * {@code canTriggerWalking = true} and {@code doesEntityNotTriggerPressurePlate = false} (:121-133) are the
 * 1.21.1 defaults; {@code isAIEnabled}.
 */
public class CliffRacer extends Animal {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    /** Constructor (:14-23). {@code setSize(0.75f, 0.5f)} is the entity type's size. */
    public CliffRacer(final EntityType<? extends CliffRacer> type, final Level par1World) {
        super(type, par1World);
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:25-31). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33000001311302185)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    /** {@code fireResistance = 5} (:22). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /** {@code canDespawn} (:33-35): {@code !isNoDespawnRequired()}; {@code Animal} would answer {@code false}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code getSoundVolume} (:37-39). */
    @Override
    protected float getSoundVolume() {
        return 0.45f;
    }

    /** {@code getSoundPitch} (:41-43). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:45-47). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CLIFFRACER.get();
    }

    /** {@code getHurtSound} (:49-51). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:53-55). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:57-59). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:61-62), empty. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:64-66). */
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code onUpdate} (:72-75), both sides: {@code motionY *= 0.6} after the move. */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
    }

    /** {@code canSeeTarget} (:77-79): ray from 0.75 above the feet. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return CritterSupport.canSeeTarget(this, 0.75, pX, pY, pZ);
    }

    /**
     * {@code updateAITasks} (:81-119): with 1 in 300, or closer than {@code distSq 2.1}, a new visible air
     * waypoint x/z ±(5..14), y ±5 (50 tries); steer 0.4/0.3 horizontally, 0.7/0.2 vertically, yaw a sixth.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            // R20: Mth.floor for the (int) casts.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.random.nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0; --keep_trying) {
                zdir = this.random.nextInt(10) + 5;
                xdir = this.random.nextInt(10) + 5;
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir,
                        Mth.floor(this.getY()) + this.random.nextInt(11) - 5,
                        Mth.floor(this.getZ()) + zdir);
                bid = this.level().getBlockState(this.currentFlightTarget);
                if (bid.isAir() && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bid = Blocks.STONE.defaultBlockState();
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.4 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.4 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.4 - motion.x) * 0.3;
        final double motionY = motion.y + (Math.signum(var2) * 0.7 - motion.y) * 0.2;
        final double motionZ = motion.z + (Math.signum(var3) * 0.4 - motion.z) * 0.3;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + var5 / 6.0f);
    }

    /** {@code fall} and {@code updateFallState} (:125-129), both empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code getCanSpawnHere} (:135-137): Y 50 and above, nothing else. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0;
    }

    /** PORT: the collision and liquid half of 1.7.10's {@code getCanSpawnHere}, which the override replaced. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:135-137) as the placement predicate. */
    public static boolean checkCliffRacerSpawnRules(final EntityType<CliffRacer> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0;
    }

    /** {@code getDropItem} (:139-151): {@code rand(8)} on the world random - raw chicken, uranium or titanium nugget, else nothing. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(8);
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

    /** {@code getDropItem} through vanilla {@code dropFewItems}; equipment afterwards (super). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, this.getDropItem());
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code createChild} (:153-155). */
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
}
