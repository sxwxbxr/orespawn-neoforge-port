package com.swbr.orespawn.entity.insect;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.Ignoreable;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.EntityMosquito} (EntityMosquito.java:11-142), id {@code mosquito}. A
 * nuisance {@code EntityAmbientCreature}: it swarms around players but never attacks. 5 XP.
 *
 * <p>Not carried over: {@code initCreature} (:140, empty); {@code canDespawn = !isNoDespawnRequired()}
 * (:35-37) is 1.21.1's default. {@code getCanSpawnHere} is {@code true} (:136-138): the spawn predicate
 * and the instance checks accept every position.
 */
public class EntityMosquito extends AmbientCreature implements Ignoreable, NoStepTrigger {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    /** Constructor (:15-21): {@code setSize(0.2f, 0.2f)} is the entity type's size; {@code experienceValue = 5}. */
    public EntityMosquito(final EntityType<? extends EntityMosquito> type, final Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0f); // setAvoidsWater(true), :19
        this.xpReward = 5;
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:23-29): health {@link #mygetMaxHealth()} = 2, speed 0.1, attack 0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code getSoundVolume} (:39-41). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code getSoundPitch} (:43-45). */
    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    /** {@code getLivingSound} (:47-49): {@code orespawn:mosquito} on the vanilla 80-tick talk interval. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.MOSQUITO.get();
    }

    /** {@code getHurtSound} (:51-53). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:55-57). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:59-61). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code collideWithEntity} (:63-64). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code collideWithNearbyEntities} (:66-67). */
    @Override
    protected void pushEntities() {
    }

    /** {@code mygetMaxHealth} (:69-71). */
    public int mygetMaxHealth() {
        return 2;
    }

    /** {@code onUpdate} (:77-80). */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6000000238418579, 1.0));
    }

    /**
     * {@code updateAITasks} (:82-120): a new target with 1/20 or when within {@code distSq < 3}; then with
     * 1/4 on the shared {@code OreSpawnRand} two blocks above the nearest player in {@code expand(10, 6, 10)},
     * otherwise (or without a player) a random air block x/z ±5, up to 50 tries.
     */
    @Override
    protected void customServerAiStep() {
        int keep_trying = 50;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.random.nextInt(20) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 3.0f) {
            Player target = null;
            if (OreSpawn.OreSpawnRand.nextInt(4) == 0) {
                target = this.findNearestPlayer();
                if (target != null) {
                    this.currentFlightTarget.set(Mth.floor(target.getX()), Mth.floor(target.getY()) + 2, Mth.floor(target.getZ()));
                } else {
                    for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0;
                            bid = this.level().getBlockState(this.currentFlightTarget), --keep_trying) {
                        this.currentFlightTarget.set(
                                Mth.floor(this.getX()) + this.random.nextInt(6) - this.random.nextInt(6),
                                Mth.floor(this.getY()) + this.random.nextInt(6) - 2,
                                Mth.floor(this.getZ()) + this.random.nextInt(6) - this.random.nextInt(6));
                    }
                }
            } else {
                for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0;
                        bid = this.level().getBlockState(this.currentFlightTarget), --keep_trying) {
                    this.currentFlightTarget.set(
                            Mth.floor(this.getX()) + this.random.nextInt(6) - this.random.nextInt(6),
                            Mth.floor(this.getY()) + this.random.nextInt(6) - 2,
                            Mth.floor(this.getZ()) + this.random.nextInt(6) - this.random.nextInt(6));
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.5 - motion.x) * 0.10000000149011612;
        final double motionY = motion.y + (Math.signum(var2) * 0.699999988079071 - motion.y) * 0.10000000149011612;
        final double motionZ = motion.z + (Math.signum(var3) * 0.5 - motion.z) * 0.10000000149011612;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.3f;
        this.setYRot(this.getYRot() + var5);
    }

    /**
     * 1.7.10 {@code World.findNearestEntityWithinAABB(EntityPlayer.class, boundingBox.expand(10, 6, 10), this)}:
     * the last of equally near players wins ({@code <=}). Spectators, which 1.7.10 did not have, are left
     * out by {@code getEntitiesOfClass}.
     */
    @Nullable
    private Player findNearestPlayer() {
        Player entity1 = null;
        double d0 = Double.MAX_VALUE;
        for (final Player entity2 : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0, 6.0, 10.0))) {
            // The 1.7.10 exclusion of the searching entity; javac rejects Player != EntityMosquito as incomparable,
            // so compare as Entity - always true here, as it was in the original.
            if ((Entity) entity2 != this) {
                final double d1 = this.distanceToSqr(entity2);
                if (d1 <= d0) {
                    entity1 = entity2;
                    d0 = d1;
                }
            }
        }
        return entity1;
    }

    /** {@code canTriggerWalking} (:122-124) {@code false}; see {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} and {@code updateFallState} (:126-130). */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:132-134). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** {@code getCanSpawnHere} (:136-138) replaced the collision and liquid test with {@code true}. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:136-138) as the placement predicate. */
    public static boolean checkMosquitoSpawnRules(final EntityType<EntityMosquito> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }
}
