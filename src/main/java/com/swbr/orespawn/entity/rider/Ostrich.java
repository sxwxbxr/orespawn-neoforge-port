package com.swbr.orespawn.entity.rider;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import com.swbr.orespawn.entity.cannonfodder.EntityCannonFodder;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Ostrich} (Ostrich.java:17-589), id {@code ostrich} ("Ostrich",
 * OreSpawnMain.java:3595/:3599, tracking 64/1/true). A rideable, tameable battle mob on {@link EntityCannonFodder}:
 * an empty hand mounts it (tamed or not), the rider steers with the look direction and the forward key, it climbs
 * walls by lifting itself over obstacles ahead, and the fly-up key makes it leap (verhalten/entity-10.md). The battle
 * AI (4 damage, scan 1/5, swing "1/8 or 1/7") is the base class's.
 *
 * <h2>Where the riding physics run</h2>
 * As in the W04 {@code Elevator}: the original moved a ridden ostrich on the server only ({@code onLivingUpdate},
 * :396-536) and let the client interpolate over a fixed 10 steps (:333-344, :381-395). 1.21.1's
 * {@code tickRidden} would run on the rider's client; the fly-up key state lives on the server (R15). So the port
 * keeps the original split: {@link #aiStep()} is the whole {@code onLivingUpdate}, a player rider is no
 * {@code getControllingPassenger} ({@code Mob} only hands control to a {@code Mob}), and the rider input comes from
 * {@code Player.zza}, which 1.21.1 sends to the server every tick while riding (W04 Elevator). The fly-up key is
 * {@link RiderKeys#isFlyUp(Entity)} (R15).
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>{@code attackEntityFrom} always answers {@code false} but applies every non-cactus hit.</li>
 *   <li>{@code jump()} adds 0.25 before {@code super.jump()}, which assigns the jump speed and erases it (1.7.10
 *       {@code EntityLivingBase.jump} and 1.21.1 {@code jumpFromGround} both assign).</li>
 *   <li>{@code experienceValue = 10} (:45) is never read; 1.7.10 {@code EntityAnimal.getExperiencePoints} gave
 *       {@code 1 + rand.nextInt(3)}, as {@code Animal.getBaseExperienceReward} still does (W04 lesson).</li>
 *   <li>The owner's name tag toggles sitting instead (the any-item branch comes first, :216-229); the click order
 *       around {@link #legacyInteract} is restored for the whole family by {@code CannonFodderInteractEvents}.</li>
 *   <li>{@code this.posY += obstruction_factor} (:424) is overwritten by {@code moveEntity}, which recomputes
 *       {@code posY} from the bounding box at its end (bytecode {@code sa.d(DDD)V}, W04 Elevator); only the motion
 *       part lifts the ostrich.</li>
 *   <li>The rider return in {@code updateAITick} (:134-136) is unreachable: with a rider {@code onLivingUpdate} never
 *       reaches {@code updateAITasks}, and {@link #aiStep()} never reaches {@code serverAiStep}. Kept as written.</li>
 * </ul>
 *
 * <p>Not carried over: {@code writeEntityToNBT}/{@code readEntityFromNBT} (:107-115) only call {@code super};
 * {@code getTrackingRange} 128, {@code getUpdateFrequency} 10, {@code sendsVelocityUpdates} (:296-306) override
 * nothing (the registration is 64/1/true); {@code isAIEnabled} (:140-142) is the 1.21.1 default;
 * {@code canBreatheUnderwater} (:144-146) returns the default {@code false} (final in 1.21.1).
 */
public class Ostrich extends EntityCannonFodder {

    private float moveSpeed;
    private RenderInfo renderdata;
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    @SuppressWarnings("unused")
    private double boatYawHead;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    float deltasmooth;
    private int didjump;

    /**
     * {@code Ostrich(World)} (:34-58) with {@code entityInit} (:69-84), whose {@link RenderInfo} reset the
     * constructor repeats and whose {@code setSitting(false)} the constructor repeats as well (the flags are not
     * built while 1.21.1 defines synced data, W04 precedent). {@code setSize(0.85f, 2.1f)} is the entity type's size;
     * {@code fireResistance = 100} is {@link #getFireImmuneTicks()}. Goals in the original priorities, added in the
     * constructor on both sides like the original.
     */
    public Ostrich(final EntityType<? extends Ostrich> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.2f;
        this.renderdata = new RenderInfo();
        this.deltasmooth = 0.0f;
        this.didjump = 0;
        this.moveSpeed = 0.38f;
        // getNavigator().setAvoidsWater(true) (:43): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        this.renderdata = new RenderInfo();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        // EntityMob.class is Monster.class (same reading as util.MyUtils).
        this.goalSelector.addGoal(3, new MyEntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.899999976158142));
        this.goalSelector.addGoal(4, new EntityAITempt(this, 1.2000000476837158, stack -> stack.is(Items.APPLE), false));
        this.goalSelector.addGoal(5, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(7, new EntityAIWatchClosest(this, Mob.class, 5.0f));
        this.goalSelector.addGoal(8, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(9, new EntityAILookIdle(this));
        // :57; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(10, new EntityAIMoveIndoors(this));
    }

    /**
     * {@code applyEntityAttributes} (:60-67): health {@link #mygetMaxHealth()} = 25, attack 6.0 (never used by a
     * goal; the battle AI strikes with 4). The speed field was still 0 when the 1.7.10 constructor chain ran this;
     * {@link #tick()} writes 0.38 before the first use, so the supplier carries 0.38.
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, javap on
     * {@code sv.<init>}); 1.21.1 defaults to 0.6. It matters for the ridden {@code moveEntity}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.38f)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code fireResistance = 100} (:42). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code getRenderInfo} (:86-88): the model's note pad (plain data, no client imports). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:90-99): copies the fields. */
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

    /** {@code onUpdate} (:101-105): the speed attribute is rewritten every tick, then the base update. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /**
     * {@code attackEntityFrom} (:117-122): cactus is ignored, every other hit applies, and the answer is always
     * {@code false} - so an attacking player sees a miss (no knockback, no sweep) and arrows bounce, as in 1.7.10.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            super.hurt(par1DamageSource, par2);
        }
        return false;
    }

    /**
     * {@code updateAITick} (:124-138), called by {@code EntityCannonFodder.updateAITasks} before its battle AI: drop
     * the revenge target with 1/200, heal 1 with 1/250, return while ridden, else {@code super} (EntityAnimal's
     * {@code inLove} reset).
     */
    @Override
    protected void updateAITick() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        if (this.level().random.nextInt(250) == 0) {
            this.heal(1.0f);
        }
        if (this.isVehicle()) {
            return;
        }
        super.updateAITick();
    }

    /** {@code mygetMaxHealth} (:148-150). */
    public int mygetMaxHealth() {
        return 25;
    }

    /** {@code getOstrichHealth} (:152-154). */
    public int getOstrichHealth() {
        return (int) this.getHealth();
    }

    /**
     * {@code interact} (:156-249) in the original order: first {@code EntityCannonFodder.interact} (which starts with
     * the animal's breeding and, for an activated mob, swallows every click), then apple, dead bush, the owner's
     * any-item sit toggle, name tag, and the empty hand that mounts.
     */
    @Override
    protected boolean legacyInteract(final Player par1EntityPlayer) {
        final boolean isRemote = this.level().isClientSide;
        // :158-162 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (super.legacyInteract(par1EntityPlayer)) {
            return true;
        }
        if (!var2.isEmpty() && var2.is(Items.APPLE) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(2) == 0) {
                        this.setTame(true, false);
                        this.setOwnerName(par1EntityPlayer.getUUID().toString()); // func_152115_b
                        this.spawnTamingParticles(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.mygetMaxHealth() - this.getHealth());
                    } else {
                        this.spawnTamingParticles(false);
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
            } else if (this.isOwnedBy(par1EntityPlayer)) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.mygetMaxHealth() > this.getHealth()) {
                    this.heal(this.mygetMaxHealth() - this.getHealth());
                }
            }
            // Consumed whoever owns it (:191-197).
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                this.setOwnerName(""); // func_152115_b("")
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (!var2.isEmpty() && this.isTame() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!isRemote) {
                if (!this.isSitting()) {
                    // :219 (int) posX, (int) posY - 1, (int) posZ -> Mth.floor (DECISIONS R20).
                    final BlockState bid = this.level().getBlockState(
                            new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ())));
                    if (RiderSupport.isSittingGround(bid)) {
                        this.setSitting(true);
                    }
                } else {
                    this.setSitting(false);
                }
            }
            return true;
        }
        // Original quirk kept (R18): unreachable for the owner, the branch above took the name tag.
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            this.setCustomName(var2.getHoverName());
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!isRemote) {
                // PORT: 1.7.10 mountEntity replaced a current rider and left that rider half-mounted; 1.21.1
                // startRiding refuses while the seat is taken (Entity.canAddPassenger), so a second player stays off.
                par1EntityPlayer.startRiding(this);
                this.setSitting(false);
            }
            return true;
        }
        return false;
    }

    /** {@code getLivingSound} (:251-256): silent either way. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        return null;
    }

    /** {@code getHurtSound} (:258-260). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRYO_HURT.get();
    }

    /** {@code getDeathSound} (:262-264). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:266-268). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code getDropItem} (:270-272). */
    @Nullable
    @Override
    protected Item getDropItem() {
        return Items.FEATHER;
    }

    /**
     * {@code dropFewItems} (:274-286): tamed, 2-6 poppies ({@code red_flower} meta 0); wild, the vanilla roll of
     * {@link #getDropItem()} feathers ({@code super}).
     */
    @Override
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.POPPY, 1));
            }
        } else {
            super.dropFewItems(par1, par2);
        }
    }

    /** {@code getSoundPitch} (:288-290). */
    @Override
    public float getVoicePitch() {
        return this.isBaby() ? ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f)
                : ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f);
    }

    /** {@code shouldRiderSit} (:292-294). */
    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    /** {@code jump} (:308-311): the added 0.25 is overwritten by the jump speed that {@code super} assigns. */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
        super.jumpFromGround();
    }

    /** {@code getMountedYOffset} (:313-315). */
    public double getMountedYOffset() {
        return 1.4;
    }

    /**
     * {@code getCanSpawnHere} (:317-330) as the placement predicate: at or above Y 50, by day, 1/4 from the world
     * random, and no other ostrich within ±16/6/16. The box is the one the ostrich will occupy at the block centre,
     * where natural spawning places it; with no entity yet, "except this one" needs no test.
     */
    public static boolean checkOstrichSpawnRules(final EntityType<Ostrich> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final ServerLevel world = level.getLevel();
        if (pos.getY() < 50.0) {
            return false;
        }
        if (!InsectSupport.isDaytime(world)) {
            return false;
        }
        if (world.random.nextInt(4) != 1) {
            return false;
        }
        return level.getEntitiesOfClass(Ostrich.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 6.0, 16.0)).isEmpty();
    }

    /**
     * The whole rule is the placement predicate. 1.21.1 asks the predicate and this method for every spawn; with the
     * 1/4 roll in both, an ostrich would spawn at 1/16 of the original rate. {@code Mob}'s walk-target test was not
     * part of the override either.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code setPositionAndRotation2} (:332-344): every move packet interpolates over a fixed 10 steps, applied in
     * {@link #aiStep()}. No {@code super}: the vanilla lerp is not used by this entity.
     */
    @Override
    public void lerpTo(final double par1, final double par3, final double par5, final float par7, final float par8,
                       final int par9) {
        this.boatPosRotationIncrements = 10;
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
        this.boatYawHead = par9; // the int step count, never read (as in the original)
        this.setDeltaMovement(this.velocityX, this.velocityY, this.velocityZ);
    }

    /** The pending interpolation target, for rotation-only packets (1.7.10 used the tracked position; W04 Elevator). */
    @Override
    public double lerpTargetX() {
        return this.boatPosRotationIncrements > 0 ? this.boatX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.boatPosRotationIncrements > 0 ? this.boatY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.boatPosRotationIncrements > 0 ? this.boatZ : this.getZ();
    }

    @Override
    public float lerpTargetYRot() {
        return this.getYRot();
    }

    @Override
    public float lerpTargetXRot() {
        return this.getXRot();
    }

    /** {@code setVelocity} (:346-354), client only. */
    @Override
    public void lerpMotion(final double par1, final double par3, final double par5) {
        this.setDeltaMovement(par1, par3, par5);
        this.velocityX = par1;
        this.velocityY = par3;
        this.velocityZ = par5;
    }

    /**
     * 1.7.10 {@code S19PacketEntityHeadLook} set {@code rotationYawHead} directly. 1.21.1 stores it for
     * {@code LivingEntity.aiStep} to lerp, which this entity's client never reaches (see {@link #aiStep()}); setting
     * it here keeps the 1.7.10 behaviour and a turning head.
     */
    @Override
    public void lerpHeadTo(final float yaw, final int steps) {
        this.setYHeadRot(yaw);
    }

    /**
     * {@code onLivingUpdate} (:356-537). A server-side ostrich without a rider runs the vanilla update; every other
     * case is this method only: an unridden client ostrich turns towards its motion, the client interpolates, and a
     * ridden server ostrich runs {@link #riddenUpdate(Entity)}.
     */
    @Override
    public void aiStep() {
        // :359-360: never read, but each draws from the entity random every tick, on both sides.
        @SuppressWarnings("unused")
        final double d6 = this.random.nextFloat() * 2.0f - 1.0f;
        @SuppressWarnings("unused")
        final double d7 = (this.random.nextInt(2) * 2 - 1) * 0.7;
        final Entity rider = this.getFirstPassenger();
        if (rider == null && !this.level().isClientSide) {
            super.aiStep();
            return;
        }
        if (this.isRemoved()) {
            return;
        }
        if (rider == null) {
            final Vec3 m = this.getDeltaMovement();
            final float var7 = (float) (Math.atan2(m.z, m.x) * 180.0 / 3.141592653589793) - 90.0f;
            final float var8 = Mth.wrapDegrees(var7 - this.getYRot());
            this.setYRot(this.getYRot() + var8 / 5.0f);
        }
        if (this.level().isClientSide) {
            if (this.boatPosRotationIncrements > 0) {
                final double d8 = this.getX() + (this.boatX - this.getX()) / this.boatPosRotationIncrements;
                final double d9 = this.getY() + (this.boatY - this.getY()) / this.boatPosRotationIncrements;
                final double d10 = this.getZ() + (this.boatZ - this.getZ()) / this.boatPosRotationIncrements;
                this.setPos(d8, d9, d10);
                this.setXRot(this.getXRot() + (float) ((this.boatPitch - this.getXRot()) / this.boatPosRotationIncrements));
                double d11 = Mth.wrapDegrees(this.boatYaw - this.getYRot());
                if (rider != null) {
                    d11 = Mth.wrapDegrees(rider.getYRot() - (double) this.getYRot());
                }
                this.setYRot(this.getYRot() + (float) (d11 / this.boatPosRotationIncrements));
                this.setRot(this.getYRot(), this.getXRot());
                --this.boatPosRotationIncrements;
            }
        } else if (rider != null) {
            this.riddenUpdate(rider);
        }
    }

    /**
     * The server branch of {@code onLivingUpdate} with a rider (:396-536): clamp the motion, lift over obstacles
     * ahead, follow the rider's yaw, leap on the fly-up key, accelerate or brake with the forward key, move, then
     * gravity and damping.
     */
    private void riddenUpdate(final Entity rider) {
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.75;
        double pi = 3.1415926545;
        double deltav = 0.0;
        int dist = 2;
        // PORT: (EntityPlayer) this.riddenByEntity (:397) threw for any other rider; 1.21.1's /ride can seat a mob.
        // A non-player rider reads as no forward input (R18 case 1, W04 Elevator).
        final Player pp = rider instanceof Player player ? player : null;
        final Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        if (motionX < -2.0) {
            motionX = -2.0;
        }
        if (motionX > 2.0) {
            motionX = 2.0;
        }
        if (motionZ < -2.0) {
            motionZ = -2.0;
        }
        if (motionZ > 2.0) {
            motionZ = 2.0;
        }
        final double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        obstruction_factor = 0.0;
        dist = 1 + (int) (velocity * 10.0);
        for (int k = 0; k < dist; ++k) {
            for (int i = 1; i < dist * 2; ++i) {
                final double dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                final double dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                // :417 (int)(posX + dx), (int) posY - 1 + k, (int)(posZ + dz) -> Mth.floor (DECISIONS R20).
                final BlockState bid = this.level().getBlockState(
                        new BlockPos(Mth.floor(this.getX() + dx), Mth.floor(this.getY()) - 1 + k, Mth.floor(this.getZ() + dz)));
                if (!bid.isAir()) {
                    obstruction_factor += 0.075;
                }
            }
        }
        motionY += obstruction_factor;
        // this.posY += obstruction_factor (:424): overwritten by moveEntity, see the class comment.
        if (motionY > 4.0) {
            motionY = 4.0;
        }
        double d8 = rider.getYRot();
        d8 %= 360.0;
        while (d8 < 0.0) {
            d8 += 360.0;
        }
        double d9 = this.getYRot();
        d9 %= 360.0;
        while (d9 < 0.0) {
            d9 += 360.0;
        }
        relative_g = (d8 - d9) % 180.0;
        while (relative_g < 0.0) {
            relative_g += 180.0;
        }
        if (relative_g > 90.0) {
            relative_g -= 180.0;
        }
        if (velocity > 0.01) {
            d8 = 1.85 - velocity;
            d8 = Math.abs(d8);
            if (d8 < 0.01) {
                d8 = 0.01;
            }
            if (d8 > 0.9) {
                d8 = 0.9;
            }
            this.setYRot(rider.getYRot() + (float) (relative_g * d8));
        } else {
            this.setYRot(rider.getYRot());
        }
        this.setXRot(2.0f * (float) velocity);
        this.setRot(this.getYRot(), this.getXRot());
        double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        // rr = atan2(rider.motionZ, rider.motionX) (:453) and rt = 0 (:456) are never read.
        final double rhm = Math.atan2(motionZ, motionX);
        final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        pi = 3.1415926545;
        deltav = 0.0;
        final float im = pp != null ? pp.zza : 0.0f; // moveForward
        // OreSpawnMain.flyup_keystate != 0 (:460): per rider (R15). The lock counts down only while released.
        if (RiderKeys.isFlyUp(rider)) {
            if (this.didjump == 0) {
                ++motionY;
                motionY += velocity * 6.0;
                this.didjump = 20;
            }
        } else if (this.didjump > 0) {
            --this.didjump;
        }
        double rdv = Math.abs(rhm - rhdir) % (pi * 2.0);
        if (rdv > pi) {
            rdv -= pi * 2.0;
        }
        rdv = Math.abs(rdv);
        if (Math.abs(newvelocity) < 0.01) {
            rdv = 0.0;
        }
        if (rdv > 1.5) {
            newvelocity = -newvelocity;
        }
        if (Math.abs(im) > 0.001f) {
            if (im > 0.0f) {
                deltav = 0.045;
                if (this.deltasmooth < 0.0f) {
                    this.deltasmooth = 0.0f;
                }
                this.deltasmooth += (float) (deltav / 10.0);
                if (this.deltasmooth > deltav) {
                    this.deltasmooth = (float) deltav;
                }
            } else {
                max_speed = 0.25;
                deltav = -0.03;
                if (this.deltasmooth > 0.0f) {
                    this.deltasmooth = 0.0f;
                }
                this.deltasmooth += (float) (deltav / 10.0);
                if (this.deltasmooth < deltav) {
                    this.deltasmooth = (float) deltav;
                }
            }
            newvelocity += this.deltasmooth;
            if (newvelocity >= 0.0) {
                if (newvelocity > max_speed) {
                    newvelocity = max_speed;
                }
                motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
            } else {
                if (newvelocity < -max_speed) {
                    newvelocity = -max_speed;
                }
                newvelocity = -newvelocity;
                motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * newvelocity;
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * newvelocity;
            }
        } else if (newvelocity >= 0.0) {
            motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
            motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
        } else {
            motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * (newvelocity * -1.0);
            motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * (newvelocity * -1.0);
        }
        // moveEntity (:528): 1.21.1's move zeroes the blocked motion components like 1.7.10's did (W04 Elevator).
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
        final Vec3 moved = this.getDeltaMovement();
        motionX = moved.x;
        motionY = moved.y;
        motionZ = moved.z;
        motionY -= 0.25;
        motionX *= 0.95;
        motionY *= 0.85;
        motionZ *= 0.95;
        this.setDeltaMovement(motionX, motionY, motionZ);
        if (rider.isRemoved()) {
            rider.stopRiding();
        }
    }

    /**
     * {@code updateRiderPosition} (:539-544): 0.15 forward along the yaw, {@code posY + 1.4 + rider.getYOffset()}.
     * A server-side 1.7.10 player had {@code getYOffset() = -0.5}, so the rider's feet sit 0.9 above the ostrich's;
     * 1.21.1 positions passengers by their feet (same reading as the W04 Elevator). A non-player {@code /ride}
     * passenger gets 0.
     */
    @Override
    protected void positionRider(final Entity passenger, final Entity.MoveFunction callback) {
        final float f = -0.15f;
        final double yOffset = passenger instanceof Player ? -0.5 : 0.0;
        callback.accept(passenger,
                this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                this.getY() + this.getMountedYOffset() + yOffset,
                this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())));
    }

    /**
     * {@code playTameEffect} (:546-557): 20 hearts or smoke puffs in a 5-block cloud. Called on the server by the
     * interactions (where 1.7.10's {@code spawnParticle} did nothing, as 1.21.1's server does), on the client by the
     * interactions and by entity events 6 and 7; the base class's hat and guard branches reach it too.
     */
    @Override
    protected void spawnTamingParticles(final boolean par1) {
        ParticleOptions s = ParticleTypes.HEART;
        if (!par1) {
            s = ParticleTypes.SMOKE;
        }
        for (int i = 0; i < 20; ++i) {
            final double d0 = this.random.nextGaussian() * 0.08;
            final double d2 = this.random.nextGaussian() * 0.08;
            final double d3 = this.random.nextGaussian() * 0.08;
            this.level().addParticle(s,
                    this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 2.5f,
                    this.getY() + 0.5 + this.random.nextFloat() * 1.5,
                    this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 2.5f,
                    d0, d2, d3);
        }
    }

    /** {@code fall} (:559-560): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:562-563): no fall bookkeeping (also no farmland trampling, as in 1.7.10). */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code canDespawn} (:565-571): a baby becomes persistent and never despawns; otherwise only without a rider,
     * not persistent and wild. {@code Animal} would answer {@code false}.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired(); // func_110163_bv
            return false;
        }
        return !this.isVehicle() && !this.isPersistenceRequired() && !this.isTame();
    }

    /** {@code createChild} (:573-576). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /**
     * {@code spawnBabyAnimal} (:578-580): a new ostrich of this type ({@code getType()} keeps this class free of the
     * registry holder's field name, as the base class's clone does).
     */
    @Nullable
    public Ostrich spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        final Entity baby = this.getType().create(this.level());
        return baby instanceof Ostrich ostrich ? ostrich : null;
    }

    /** {@code isWheat} (:582-584): overrides nothing in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:586-588): the Crystal Apple. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }
}
