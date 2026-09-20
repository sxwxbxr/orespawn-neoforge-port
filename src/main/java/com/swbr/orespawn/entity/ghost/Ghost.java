package com.swbr.orespawn.entity.ghost;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.Ignoreable;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Ghost} (Ghost.java:12-178), id {@code ghost} ("Ghost", OreSpawnMain.java:3707/:3711,
 * tracking 32/1/false). A harmless night {@code EntityAmbientCreature} that floats through walls towards the nearest
 * player or drifts about (verhalten/entity-08.md). Every target search skips it ({@link Ignoreable},
 * MyUtils.java:17-19). The Halloween spawns (OreSpawnMain.java:4181-4226) come with the W12 biome modifier.
 *
 * <p>Not carried over: {@code initCreature} (:167-168) is empty and overrides nothing in 1.7.10;
 * {@code isAIEnabled} (:78-80) is the 1.21.1 default.
 */
public class Ghost extends AmbientCreature implements Ignoreable, NoStepTrigger {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;

    /**
     * {@code Ghost(World)} (:16-23). {@code setSize(0.5f, 1.5f)} is the entity type's size; {@code noClip} is
     * {@link #noPhysics}. The move helper is the W05 flyers' {@link InsectSupport.LegacyMoveControl}: 1.7.10 ran the
     * move helper (which zeroes {@code moveForward}) before the subclass set 0.05, 1.21.1 runs it after.
     */
    public Ghost(final EntityType<? extends Ghost> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        // PORT: getNavigator().setAvoidsWater(false) (:20) is a water path malus of 0 (W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 5;
        this.noPhysics = true;
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /**
     * {@code applyEntityAttributes} (:25-31): health {@link #mygetMaxHealth()} = 2, speed 0.1, attack 0.
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator); it matters
     * only once persistence has switched {@code noClip} off.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code canDespawn} (:37-39); persistence is checked by {@code Mob.checkDespawn} before this. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code getSoundVolume} (:41-43). */
    @Override
    protected float getSoundVolume() {
        return 0.3f;
    }

    /** {@code getSoundPitch} (:45-47). */
    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    /** {@code getLivingSound} (:49-54): the ghost sound with 1/2 from the world random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(2) == 0) {
            return ModSounds.GHOST_SOUND.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:56-58). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:60-62). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:64-66). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code collideWithEntity} (:68-69). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code collideWithNearbyEntities} (:71-72). */
    @Override
    protected void pushEntities() {
    }

    /** {@code mygetMaxHealth} (:74-76). */
    public int mygetMaxHealth() {
        return 2;
    }

    /** {@code onUpdate} (:82-88): a persistent (e.g. name-tagged) ghost collides again; vertical damping after the tick. */
    @Override
    public void tick() {
        if (this.isPersistenceRequired()) {
            this.noPhysics = false;
        }
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.65, 1.0));
    }

    /**
     * {@code updateAITasks} (:90-132): a new target with 1/40 or within squared distance 2 - the nearest player within
     * 16 (±2 on x/z, one block above) or a random spot ±9 around, its height from the air above and the ground below
     * - then steer towards it (x/z 0.1 at 0.05, y 0.7 at 0.1), push forward 0.05 and turn a sixth of the way.
     */
    @Override
    protected void customServerAiStep() {
        int i = 0;
        int j = 0;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        // :98-119 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20); the position does not change inside this method.
        final int posX = Mth.floor(this.getX());
        final int posY = Mth.floor(this.getY());
        final int posZ = Mth.floor(this.getZ());
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(posX, posY, posZ);
        }
        if (this.level().random.nextInt(40) == 1
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, posX, posY, posZ) < 2.0f) {
            Player target = null;
            target = GhostSupport.findNearestPlayer(this, this.getBoundingBox().inflate(16.0, 16.0, 16.0));
            if (target != null) {
                this.currentFlightTarget.set(
                        Mth.floor(target.getX()) + this.random.nextInt(3) - this.random.nextInt(3),
                        Mth.floor(target.getY()) + 1,
                        Mth.floor(target.getZ()) + this.random.nextInt(3) - this.random.nextInt(3));
            } else {
                for (i = 0; i < 3; ++i) {
                    final BlockState bid = this.level().getBlockState(new BlockPos(posX, posY + i, posZ));
                    if (bid.isAir()) {
                        break;
                    }
                }
                for (j = -1; j >= -3; --j) {
                    final BlockState bid = this.level().getBlockState(new BlockPos(posX, posY + j, posZ));
                    if (!bid.isAir()) {
                        break;
                    }
                }
                this.currentFlightTarget.set(
                        posX + this.random.nextInt(10) - this.random.nextInt(10),
                        posY + i + j + this.random.nextInt(4) + 1,
                        posZ + this.random.nextInt(10) - this.random.nextInt(10));
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.1 - motion.x) * 0.05;
        final double motionY = motion.y + (Math.signum(var2) * 0.7 - motion.y) * 0.1;
        final double motionZ = motion.z + (Math.signum(var3) * 0.1 - motion.z) * 0.05;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.05f;
        this.setYRot(this.getYRot() + var5 / 6.0f);
    }

    /** {@code canTriggerWalking} (:134-136) {@code false}: no step sounds; the other half is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} (:138-139): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:141-142): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:144-146). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * {@code getCanSpawnHere} (:148-165) as the placement predicate: a "Ghost" spawner nearby always allows it,
     * otherwise only at night ({@code !isDaytime}).
     */
    public static boolean checkGhostSpawnRules(final EntityType<Ghost> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (GhostSupport.spawnerNearby(level, pos, OreSpawn.MOD_ID + ":ghost")) {
            return true;
        }
        return !InsectSupport.isDaytime(level.getLevel());
    }

    /** The whole rule is the placement predicate; {@code Mob}'s walk-target test was not part of the override. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code attackEntityFrom} (:170-177): suffocation is ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return ret;
        }
        ret = super.hurt(par1DamageSource, par2);
        return ret;
    }
}
