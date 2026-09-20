package com.swbr.orespawn.entity.worm;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.WormSmall} (WormSmall.java:13-240): {@code small_worm} ("Small Worm",
 * OreSpawnMain.java:3727, tracking 32/1/false), the first stage of the worm family. It sits in the ground with
 * {@code noClip}, comes up for 25..74 ticks while a player is within 8 blocks, goes down for 100..249, bites and
 * steals boots while up. It is killed without drops the moment its test block is anything but grass, dirt or stone.
 *
 * <p>Values: health/attack/defense from {@code WormSmall_stats} (10/3/0), speed 0.1, hitbox 0.25 x 1.0,
 * {@code experienceValue = 0} ({@code EntityMob} reads it, so no XP), no DataWatcher entry, no NBT, never despawns.
 * Brood of {@link WormLarge}, egg {@code eggsmallworm}.
 *
 * <p><b>{@code posY +=}.</b> The original writes {@code posY} directly after the move. 1.7.10's {@code moveEntity}
 * takes {@code posY} back from the bounding box ({@code sa.d}, disassembled: {@code posY = boundingBox.minY + yOffset -
 * ySize}), so on the server the offset only lives until the next move: it is what clients and the save see, not
 * extra climb. 1.21.1 keeps the position as the truth and rebuilds the box from it, so a plain {@code setPos} would add
 * the offset to every tick's climb (0.17 a tick becomes 0.27 while digging up). {@link #legacyAddPosY} therefore
 * writes the raw position and {@link #move} snaps it back to the box first - the 1.7.10 order.
 */
public class WormSmall extends Monster implements LegacyArmor, NoStepTrigger {

    public int upcount;
    public int downcount;
    /** A raw {@code posY +=} is waiting to be dropped by the next {@link #move}. Transient, like the offset. */
    private boolean legacyPosYPending;

    /** {@code WormSmall(World)} (:18-26). */
    public WormSmall(final EntityType<? extends WormSmall> type, final Level par1World) {
        super(type, par1World);
        // applyEntityAttributes ran in the EntityLivingBase constructor, followed by setHealth(getMaxHealth()).
        this.applyEntityAttributes();
        this.setHealth(this.getMaxHealth());
        this.upcount = 50;
        this.downcount = 0;
        // setSize(0.25f, 1.0f) (:22) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:23) - water is impassable for the path finder, a malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 0;
        this.noPhysics = true;
    }

    /**
     * Attribute set for {@code EntityAttributeCreationEvent} with the config defaults; the constructor sets the bases
     * from the loaded config like {@code applyEntityAttributes} (:28-33) did on every construction.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    /** {@code applyEntityAttributes} (:28-33). */
    private void applyEntityAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.WormSmall_stats().attack());
    }

    // entityInit (:35-37) adds nothing. isAIEnabled (:81-83): every 1.21.1 mob runs its AI step.

    /** {@code canDespawn} (:39-41). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getSoundVolume} (:43-45). */
    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    /** {@code getSoundPitch} (:47-49): a constant, no random spread. */
    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    /** {@code getLivingSound} (:51-53). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:55-57). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LITTLE_SPLAT.get();
    }

    /** {@code getDeathSound} (:59-61). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:63-65). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:67-68): empty. */
    @Override
    protected void doPush(final Entity entity) {
    }

    /** {@code collideWithNearbyEntities} (:70-71): empty. */
    @Override
    protected void pushEntities() {
    }

    /** Inherited {@code applyEntityCollision}; see {@link WormSupport#legacyPush}. */
    @Override
    public void push(final Entity entity) {
        WormSupport.legacyPush(this, entity);
    }

    /** {@code mygetMaxHealth} (:73-75). */
    public int mygetMaxHealth() {
        return MobStats.WormSmall_stats().health();
    }

    /** {@code getTotalArmorValue} (:77-79). */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.WormSmall_stats().defense();
    }

    /**
     * {@code onLivingUpdate} (:85-149): after the vanilla step, dig up or down.
     *
     * <p>PORT (R18 case 4): the original ran this on both sides, {@code setDead} included. A client-side
     * {@code discard()} leaves a ghost the server never removes, and the client does not simulate a mob it does not
     * control; the logic runs on the server only. {@code (int)} coordinates are {@code Mth.floor} (R20).
     */
    @Override
    public void aiStep() {
        Player target = null;
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        target = WormSupport.findNearestEntityWithinAABB(this, Player.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        if (target != null || OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            if (this.upcount > 0) {
                --this.upcount;
                if (this.upcount == 0) {
                    this.downcount = 100 + this.level().random.nextInt(150);
                }
                if (target != null) {
                    this.pointAtEntity(target);
                }
                BlockState bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY() + 0.25), Mth.floor(this.getZ()));
                if (WormSupport.isTallGrass(bid)) {
                    bid = Blocks.AIR.defaultBlockState();
                }
                if (!bid.isAir()) {
                    if (!WormSupport.isGrassDirtOrStone(bid)) {
                        this.discard();
                    }
                    this.addMotionY(0.15000000596046448);
                    this.legacyAddPosY(0.10000000149011612);
                }
            } else {
                if (this.downcount > 0) {
                    --this.downcount;
                } else {
                    this.upcount = 25 + this.level().random.nextInt(50);
                }
                BlockState bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY()) + 2, Mth.floor(this.getZ()));
                if (WormSupport.isTallGrass(bid)) {
                    bid = Blocks.AIR.defaultBlockState();
                }
                if (!bid.isAir()) {
                    if (!WormSupport.isGrassDirtOrStone(bid)) {
                        this.discard();
                    }
                    this.addMotionY(0.20000000298023224);
                    this.legacyAddPosY(0.05000000074505806);
                }
            }
        } else {
            this.upcount = this.level().random.nextInt(50);
            this.downcount = 0;
            BlockState bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY()) + 2, Mth.floor(this.getZ()));
            if (WormSupport.isTallGrass(bid)) {
                bid = Blocks.AIR.defaultBlockState();
            }
            if (!bid.isAir()) {
                if (!WormSupport.isGrassDirtOrStone(bid)) {
                    this.discard();
                }
                this.addMotionY(0.10000000149011612);
                this.legacyAddPosY(0.05000000074505806);
            }
        }
        final Vec3 motion = this.getDeltaMovement();
        // motionY -= 0.01; motionX = 0.0; motionZ = 0.0; moveForward = 0.0f (:145-148).
        this.setDeltaMovement(0.0, motion.y - 0.01, 0.0);
        this.zza = 0.0f;
    }

    /**
     * {@code onUpdate} (:151-157): a persistent (name-tagged) worm stops clipping before it moves; afterwards the
     * vertical motion is damped.
     */
    @Override
    public void tick() {
        if (this.isPersistenceRequired()) {
            this.noPhysics = false;
        }
        super.tick();
        final Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.75, motion.z);
    }

    /** Drops a pending raw {@code posY} offset the way 1.7.10's {@code moveEntity} did (class Javadoc). */
    @Override
    public void move(final MoverType type, final Vec3 pos) {
        if (this.legacyPosYPending) {
            this.legacyPosYPending = false;
            this.setPosRaw(this.getX(), this.getBoundingBox().minY, this.getZ());
        }
        super.move(type, pos);
    }

    /** {@code posY += d} without touching the bounding box (class Javadoc). */
    private void legacyAddPosY(final double d) {
        this.setPosRaw(this.getX(), this.getY() + d, this.getZ());
        this.legacyPosYPending = true;
    }

    /** {@code motionY += d}. */
    private void addMotionY(final double d) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, d, 0.0));
    }

    /** {@code worldObj.getBlock(x, y, z)}. */
    private BlockState blockAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /** {@code pointAtEntity} (:159-167): yaw and head yaw straight at the entity. */
    public void pointAtEntity(final LivingEntity e) {
        final double d1 = e.getX() - this.getX();
        final double d2 = e.getZ() - this.getZ();
        final float d3 = (float) Math.atan2(d2, d1);
        final float n;
        final float f2 = n = (float) (d3 * 180.0 / 3.141592653589793) - 90.0f;
        this.setYHeadRot(n);
        this.setYRot(n);
    }

    /**
     * {@code updateAITasks} (:169-205): bite a non-creative player within {@code expand(1.5, 4, 1.5)} one tick in 15
     * while up, and then one bite in six takes the boots.
     *
     * <p>PORT: {@code Mob.serverAiStep} is final; the part after {@code super.updateAITasks()} runs here, which is
     * after the navigation tick but before the move, look and jump controls (1.7.10 ran them first). With no goals the
     * only visible effect is that the head turns towards the body by up to 10 degrees after {@link #pointAtEntity}
     * instead of before; the model ignores head yaw.
     */
    @Override
    protected void customServerAiStep() {
        Player target = null;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return;
        }
        target = WormSupport.findNearestEntityWithinAABB(this, Player.class, this.getBoundingBox().inflate(1.5, 4.0, 1.5));
        if (target != null && target.getAbilities().instabuild) {
            target = null;
        }
        if (target != null) {
            this.pointAtEntity(target);
            if (this.upcount > 0 && this.level().random.nextInt(15) == 1 && !target.getAbilities().instabuild) {
                // EntityMob.attackEntityAsMob: attack attribute, held-item enchantments (none), mob damage.
                super.doHurtTarget(target);
                if (this.level().random.nextInt(6) == 1) {
                    // getEquipmentInSlot(1) of a player is armorInventory[0], the boots.
                    final ItemStack boots = target.getItemBySlot(EquipmentSlot.FEET);
                    if (!boots.isEmpty()) {
                        WormSupport.stealAndThrow(this, target, EquipmentSlot.FEET, boots, 20);
                    }
                }
            }
        }
    }

    /** {@code canTriggerWalking} (:207-209): no step sounds; the block side is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} (:211-212): empty. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:214-215): empty, so no fall distance, no {@code onFallenUpon}. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:217-219). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * {@code getCanSpawnHere} (:221-223): only at night. There are no natural spawns (manifest), so this answers the
     * spawner, which asks the positioned instance ({@code EventHooks.checkSpawnPositionSpawner}).
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return !HerbivoreSupport.isDaytime(this.level());
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    // initCreature (:225-226) is empty and never called in 1.7.10.

    /** {@code attackEntityFrom} (:228-235): immune to suffocation. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return ret;
        }
        ret = super.hurt(par1DamageSource, par2);
        return ret;
    }

    // getDropItem (:237-239) is null: EntityLiving.dropFewItems drops nothing, no dropCustomDeathLoot of its own.
}
