package com.swbr.orespawn.entity.ghost;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.renderer.RenderInfo;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.GhostSkelly} (GhostSkelly.java:12-207), id {@code ghost_pumpkin_skelly}
 * ("Ghost Pumpkin Skelly", OreSpawnMain.java:3715/:3719, tracking 64/1/false). The larger, chain-rattling ghost:
 * the same flight as {@link Ghost}, five hit points, and a {@link RenderInfo} note pad the model uses for its
 * spinning pumpkin head (verhalten/entity-08.md). Every target search skips it ({@link Ignoreable}).
 *
 * <p>{@link RenderInfo} is a plain data class without client imports (its package is {@code client.renderer} only
 * because W01 put it there); it lives in the entity like the original's field and is only written by the model.
 *
 * <p>Not carried over: {@code initCreature} (:196-197) is empty and overrides nothing; {@code isAIEnabled}
 * (:106-108) is the 1.21.1 default.
 */
public class GhostSkelly extends AmbientCreature implements Ignoreable, NoStepTrigger {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private RenderInfo renderdata;

    /**
     * {@code GhostSkelly(World)} (:17-25) and {@code entityInit} (:35-48), whose zeroing of a fresh
     * {@link RenderInfo} the constructor already is. {@code setSize(1.5f, 2.0f)} is the entity type's size.
     */
    public GhostSkelly(final EntityType<? extends GhostSkelly> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.renderdata = new RenderInfo();
        // PORT: getNavigator().setAvoidsWater(false) (:22) is a water path malus of 0 (W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 10;
        this.noPhysics = true;
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /**
     * {@code applyEntityAttributes} (:27-33): health {@link #mygetMaxHealth()} = 5, speed 0.1, attack 0.
     * PORT: {@code STEP_HEIGHT} 0.5 as in {@link Ghost#createAttributes()}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code getRenderInfo} (:50-52). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:54-63): copies the fields. */
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

    /** {@code canDespawn} (:65-67). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code getSoundVolume} (:69-71). */
    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    /** {@code getSoundPitch} (:73-75). */
    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    /** {@code getLivingSound} (:77-82): the chains with 1/2 from the world random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(2) == 0) {
            return ModSounds.CHAIN_RATTLES.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:84-86). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:88-90). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:92-94). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code collideWithEntity} (:96-97). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code collideWithNearbyEntities} (:99-100). */
    @Override
    protected void pushEntities() {
    }

    /** {@code mygetMaxHealth} (:102-104). */
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code onUpdate} (:110-116). */
    @Override
    public void tick() {
        if (this.isPersistenceRequired()) {
            this.noPhysics = false;
        }
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.65, 1.0));
    }

    /**
     * {@code updateAITasks} (:118-161), the same flight as {@link Ghost#customServerAiStep()}; the only difference
     * is the target height {@code (int)(target.posY + 1.0)} (:133).
     */
    @Override
    protected void customServerAiStep() {
        BlockState bid = Blocks.AIR.defaultBlockState();
        int i = 0;
        int j = 0;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        // :127-148 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20).
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
                        Mth.floor(target.getY() + 1.0),
                        Mth.floor(target.getZ()) + this.random.nextInt(3) - this.random.nextInt(3));
            } else {
                for (i = 0; i < 3; ++i) {
                    bid = this.level().getBlockState(new BlockPos(posX, posY + i, posZ));
                    if (bid.isAir()) {
                        break;
                    }
                }
                for (j = -1; j >= -3; --j) {
                    bid = this.level().getBlockState(new BlockPos(posX, posY + j, posZ));
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

    /** {@code canTriggerWalking} (:163-165) {@code false}; the other half is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} (:167-168). */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:170-171). */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:173-175). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** {@code getCanSpawnHere} (:177-194): a "Ghost Pumpkin Skelly" spawner nearby, otherwise night. */
    public static boolean checkGhostSkellySpawnRules(final EntityType<GhostSkelly> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (GhostSupport.spawnerNearby(level, pos, OreSpawn.MOD_ID + ":ghost_pumpkin_skelly")) {
            return true;
        }
        return !InsectSupport.isDaytime(level.getLevel());
    }

    /** The whole rule is the placement predicate. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code attackEntityFrom} (:199-206): suffocation is ignored. */
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
