package com.swbr.orespawn.entity.dragon;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.ender.EnderReaper;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.monster.LeafMonster;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.portal.EntityAINearestAttackableTarget;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.WaterBall;
import com.swbr.orespawn.entity.terror.CreepingHorror;
import com.swbr.orespawn.entity.terror.LurkingTerror;
import com.swbr.orespawn.entity.terror.TerribleTerror;
import com.swbr.orespawn.entity.triffid.Triffid;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Dragon} (Dragon.java:19-1473), id {@code dragon} ("Dragon", OreSpawnMain.java:3575,
 * tracking 128/1/true). A tameable, rideable fire or ice dragon on {@code EntityTameable}; a diamond turns a tamed one
 * into a Baby Dragon ({@link Spyro}) and back (verhalten/entity-06.md).
 *
 * <h2>Two phases</h2>
 * {@code activity} 0 is a ground mob with vanilla movement and goals. {@code activity} 1 is flight: the original's
 * {@code onLivingUpdate} then skips {@code super.onLivingUpdate} (no goals, no gravity, no vanilla movement) and moves
 * the dragon itself - {@link #flyWithoutRider()} or, with a rider, the server-side steering in {@link #aiStep()}. The
 * port keeps exactly that split: {@link #aiStep()} calls {@code super.aiStep()} only on the ground.
 *
 * <h2>Riding</h2>
 * As for the W04 Elevator and the W06 Ostrich: the original steered on the server only and the client interpolated the
 * tracked position. A player rider is no {@code getControllingPassenger} in 1.21.1 ({@code Mob} only hands control to a
 * {@code Mob}), so no client simulates the dragon; the rider input is {@code Player.zza}/{@code xxa}, which
 * {@code ServerGamePacketListenerImpl.handlePlayerInput} writes on the server every tick while riding
 * ({@code ServerPlayer.setPlayerInput}, catalogue 6.3: verified for both). The fly-up key is
 * {@link RiderKeys#isFlyUp(Entity)} (R15). PORT: the wave plan named {@code tickRidden}/{@code getRiddenInput}; that
 * path runs the physics on the rider's client, where neither the per-player key state nor the fireball code lives, and
 * would break the 1:1 server order, so the precedent of W04/W06 is followed.
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>A tamed dragon takes a player's hit and answers {@code false} without targeting the player (:407-412).</li>
 *   <li>{@code this.posY += ...} before {@code moveEntity} (:824, :912, :931) is overwritten by {@code moveEntity}, which
 *       recomputes {@code posY} from the unchanged bounding box, but the block scans and the fireball muzzle of the same
 *       tick read the raised value. The port keeps a local {@code posY} for those reads and never moves the entity by
 *       it (bytecode {@code sa.d(DDD)V}, W04 Elevator).</li>
 *   <li>{@code experienceValue = 100} (:79) is never read: 1.7.10 {@code EntityAnimal.getExperiencePoints} gives
 *       {@code 1 + rand.nextInt(3)}, as {@code Animal.getBaseExperienceReward} still does (W04 lesson).</li>
 *   <li>The {@code BetterFireball} immunity (:385) needs a damage source whose entity is the fireball itself; the
 *       dragon ignores {@code onFire} before, so the branch is kept as written.</li>
 * </ul>
 *
 * <p>Not carried over: {@code getTrackingRange} 128, {@code getUpdateFrequency} 10, {@code sendsVelocityUpdates}
 * (:122-132) override nothing (the registration 128/1/true counts, W06 Ostrich); {@code isAIEnabled} (:198) is the
 * 1.21.1 default; {@code canBreatheUnderwater} (:285) is the {@code minecraft:can_breathe_under_water} tag (final in
 * 1.21.1, W04); {@code canTriggerWalking} and {@code doesEntityNotTriggerPressurePlate} (:140, :555) keep the defaults,
 * so no {@code NoStepTrigger} (R20); {@code updateit}, {@code color}, {@code playing} are never read.
 */
public class Dragon extends TamableAnimal implements AttackableNonMob, LegacyArmor {

    /** DataWatcher 20: attacking (0/1). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    /** DataWatcher 21: activity (0 ground, 1 flight). */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    /** DataWatcher 22: dragon type (0 fire, 1 ice). */
    private static final EntityDataAccessor<Integer> DATA_DRAGON_TYPE = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);
    /** DataWatcher 24: DragonFire (0 off, 1 lit, 2 supercharged), initial 1. */
    private static final EntityDataAccessor<Integer> DATA_DRAGON_FIRE = SynchedEntityData.defineId(Dragon.class, EntityDataSerializers.INT);

    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    @SuppressWarnings("unused")
    private double boatYawHead;
    private GenericTargetSorter TargetSorter;
    private RenderInfo renderdata;
    private int hurt_timer;
    private int wing_sound;
    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private boolean target_in_sight;
    private int owner_flying;
    private int flyaway;
    private int stuck_count;
    private int lastX;
    private int lastZ;
    private int unstick_timer;
    private int fireballticker;
    private float moveSpeed;
    private float deltasmooth;
    private int dragontype;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /**
     * {@code Dragon(World)} (:52-97). {@code setSize(1.5f, 1.25f)} is the entity type's size (R9);
     * {@code fireResistance = 1000} is {@link #getFireImmuneTicks()}, {@code isImmuneToFire} the type's
     * {@code fireImmune()}. The {@code entityInit} defaults (:144-164) are {@link #defineSynchedData}.
     */
    public Dragon(final EntityType<? extends Dragon> type, final Level par1World) {
        super(type, par1World);
        this.hurt_timer = 0;
        this.wing_sound = 0;
        this.currentFlightTarget = null;
        this.target_in_sight = false;
        this.owner_flying = 0;
        this.flyaway = 0;
        this.stuck_count = 0;
        this.lastX = 0;
        this.lastZ = 0;
        this.unstick_timer = 0;
        this.fireballticker = 0;
        this.moveSpeed = 0.32f;
        this.deltasmooth = 0.0f;
        this.dragontype = 0;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // getNavigator().setAvoidsWater(true) (:78): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 1.1f, 12.0f, 2.0f));
        this.goalSelector.addGoal(2, new EntityAITempt(this, 1.25, stack -> stack.is(Items.BEEF), false));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Player.class, 9.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        // :89; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(6, new EntityAIMoveIndoors(this));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(1, new NearestMobTarget(this));
        }
        // EntityAIHurtByTarget(this, false): no call for help.
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
    }

    /**
     * {@code applyEntityAttributes} (:110-116): health {@link #mygetMaxHealth()} 200, speed 0.32, attack 35 (never read:
     * {@link #doHurtTarget} strikes with a fixed 35).
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, W06 Ostrich);
     * 1.21.1 defaults to 0.6. It matters for the explicit {@code moveEntity} of both flight modes.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.32f)
                .add(Attributes.ATTACK_DAMAGE, 35.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code entityInit} (:144-164): 20 = 0, 21 = 0, 22 = 0, 24 = 1; tame flags from {@code TamableAnimal}. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_DRAGON_TYPE, 0);
        builder.define(DATA_DRAGON_FIRE, 1);
    }

    /** {@code fireResistance = 1000} (:80). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code shouldRiderSit} (:118-120). */
    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    /** {@code fall} (:134-135): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:137-138): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code mygetMaxHealth} (:166-168). */
    public int mygetMaxHealth() {
        return 200;
    }

    /** {@code getDragonHealth} (:170-172). */
    public int getDragonHealth() {
        return (int) this.getHealth();
    }

    /** {@code getRenderInfo} (:174-176): the model's note pad (plain data, no client imports). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:178-187): copies the fields. */
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

    /** {@code getTotalArmorValue} (:189-191) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 14;
    }

    /** {@code jump} (:193-196): {@code super.jump()} first, so the extra 0.25 survives. */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
    }

    /** {@code scan_it} (:202-283): one shell of the lava search cube, the nearest lava block wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (DragonSupport.isLava(bid)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (DragonSupport.isLava(bid)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x - dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = this.level().getBlockState(new BlockPos(x + i, y + dy, z + j));
                if (DragonSupport.isLava(bid)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.level().getBlockState(new BlockPos(x + i, y - dy, z + j));
                if (DragonSupport.isLava(bid)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y - dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
            }
        }
        for (int i = -dx; i <= dx; ++i) {
            for (int j = -dy; j <= dy; ++j) {
                BlockState bid = this.level().getBlockState(new BlockPos(x + i, y + j, z + dz));
                if (DragonSupport.isLava(bid)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                bid = this.level().getBlockState(new BlockPos(x + i, y + j, z - dz));
                if (DragonSupport.isLava(bid)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z - dz;
                        ++found;
                    }
                }
            }
        }
        return found != 0;
    }

    /** {@code getLivingSound} (:289-297): a roar only while attacking, without a rider, not sitting. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        if (this.getAttacking() == 1 && !this.isVehicle()) {
            return ModSounds.ROAR.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:299-301). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:303-305). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:307-309). */
    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    /** {@code getSoundPitch} (:311-313). */
    @Override
    public float getVoicePitch() {
        return 0.75f;
    }

    /** {@code canBePushed} (:315-317). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code getMountedYOffset} (:319-321). */
    public double getMountedYOffset() {
        return 1.3;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}; {@code getDropItem} (:323-325) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:337-341): {@code 1 + rand(6)} raw beef, each through {@code dropItemRand} (:327-335), spread
     * ±4 from {@code OreSpawnRand} at y+1.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 1 + this.level().random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(Items.BEEF, 1), 5);
        }
    }

    /**
     * {@code attackEntityAsMob} (:343-359): a fixed 35 (twice against the Kraken), then a push of 1.75 away and 0.1 up,
     * doubled upward for a player or a removed target. Always {@code true}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 1.75;
        double inair = 0.1;
        float iskraken = 1.0f;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            // PORT: instanceof Kraken (:348), by registry id (same set).
            if (DragonSupport.isKraken(par1Entity)) {
                iskraken = 2.0f;
            }
            par1Entity.hurt(this.damageSources().mobAttack(this), iskraken * 35.0f);
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            // isDead: the 1.7.10 removal flag, not the health.
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return true;
    }

    /**
     * {@code attackEntityFrom} (:361-419) in the original order: 20 ticks of immunity after every hit; cactus, fire,
     * lava and suffocation ignored; any other hit wakes it up and makes it fly; the element's own projectiles are
     * destroyed; dragons do not hurt each other; a tamed dragon takes a player's hit but answers {@code false}; any other
     * living attacker becomes the target.
     *
     * <p>PORT: {@code getDamageType()} is {@link DamageSource#getMsgId()}, the same string; {@code inFire} therefore also
     * covers the campfire (1.21.1 registers it with the message id {@code inFire}). {@code setTarget(e)} (the pre-AI
     * {@code entityToAttack}) has no 1.21.1 field and is the same {@code setTarget} call.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        Entity e = null;
        if (this.hurt_timer > 0) {
            return false;
        }
        final String type = par1DamageSource.getMsgId();
        if (type.equals("cactus")) {
            return ret;
        }
        if (type.equals("inFire")) {
            return ret;
        }
        if (type.equals("onFire")) {
            return ret;
        }
        if (type.equals("lava")) {
            return ret;
        }
        if (type.equals("inWall")) {
            return ret;
        }
        this.setSitting(false);
        this.setActivity(1);
        e = par1DamageSource.getEntity();
        if (e != null && e instanceof BetterFireball && this.dragontype == 0) {
            e.discard();
            return ret;
        }
        if (e != null && e instanceof IceBall && this.dragontype != 0) {
            e.discard();
            return ret;
        }
        if (e != null && e instanceof WaterBall && this.dragontype != 0) {
            e.discard();
            return ret;
        }
        if (e != null && e instanceof SmallFireball && this.dragontype == 0) {
            e.discard();
            return ret;
        }
        if (e != null && e instanceof Dragon) {
            return false;
        }
        if (e != null && e instanceof Spyro) {
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        this.hurt_timer = 20;
        if (e != null && e instanceof LivingEntity living) {
            if (this.isTame() && e instanceof Player) {
                return false;
            }
            this.setTarget(living);
            this.getNavigation().moveTo(e, 1.2);
            ret = true;
        }
        return ret;
    }

    /**
     * {@code updateAITick} (:525-530) followed by the tail of {@code updateAITasks} (:421-430): while ridden, skip the
     * animal's tick; then, on the ground without a rider, not sitting and not Peaceful, with 1 in 10 take off when
     * something to attack is in sight.
     *
     * <p>PORT: 1.21.1 {@code Mob.serverAiStep} is final; the original ran its search after the move, look and jump
     * helpers, here it runs right before them. None of the three draws a random number.
     */
    @Override
    protected void customServerAiStep() {
        if (!this.isVehicle()) {
            super.customServerAiStep();
        }
        LivingEntity e = null;
        if (!this.isSitting() && this.getActivity() == 0 && !this.isVehicle()
                && this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(10) == 1) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setActivity(1);
            }
        }
    }

    /**
     * 1.7.10 {@code despawnEntity} ran inside {@code updateAITasks}, which a flying dragon never reaches. 1.21.1 asks
     * {@code checkDespawn} from the level tick; a flying dragon therefore stays, as in the original.
     */
    @Override
    public void checkDespawn() {
        if (this.getActivity() != 0) {
            return;
        }
        super.checkDespawn();
    }

    /**
     * {@code always_do} (:432-488), every server tick in both phases: regenerate, follow a flying or distant owner into
     * the air, take off or land at random, and look for lava to bathe in.
     */
    public void always_do() {
        Player p = null;
        if (this.level().random.nextInt(250) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(2.0f);
        }
        if (this.isSitting()) {
            return;
        }
        this.owner_flying = 0;
        if (this.isTame() && this.getOwner() != null && !this.isVehicle() && !this.isSitting()) {
            p = (Player) this.getOwner();
            if (p.getAbilities().flying) {
                this.setActivity(this.owner_flying = 1);
            }
        }
        if (this.isTame() && this.getOwner() != null && !this.isSitting()) {
            p = (Player) this.getOwner();
            if (this.distanceToSqr(p) > 400.0) {
                this.setActivity(1);
            }
        }
        if (this.level().random.nextInt(50) == 1 && !this.isSitting() && !this.target_in_sight && !this.isVehicle()) {
            if (this.level().random.nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
        if (this.level().random.nextInt(25) == 0 && !this.target_in_sight && !this.isVehicle()) {
            this.closest = 99999;
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 11; ++i) {
                int j = i;
                if (j > 4) {
                    j = 4;
                }
                // :472 (int) posX, (int) posY - 1, (int) posZ -> Mth.floor (DECISIONS R20).
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 6) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.setActivity(0);
                this.getNavigation().moveTo((double) this.tx, (double) (this.ty - 1), (double) this.tz, 1.0);
                if (this.isInLava()) {
                    this.heal(1.0f);
                    // playSound("splash", ...) (:484): an event without namespace, silent (R18); its pitch is still drawn.
                    this.level().random.nextFloat();
                }
            }
        }
    }

    /**
     * {@code fly_with_rider} (:490-523): with 1 in 7, not Peaceful, drop the target with 1 in 250, look for one and strike
     * it within {@code (7 + width/2)^2}.
     */
    public void fly_with_rider() {
        LivingEntity e = null;
        final int freq = 7;
        if (this.isRemoved()) {
            return;
        }
        if (this.isSitting()) {
            return;
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.level().random.nextInt(freq) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            if (this.level().random.nextInt(250) == 0) {
                this.setTarget(null);
            }
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.setAttacking(1);
                if (this.distanceToSqr(e) < (7.0f + e.getBbWidth() / 2.0f) * (7.0f + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                }
                return;
            }
            this.setAttacking(0);
        }
    }

    /**
     * {@code isSuitableTarget} (:532-534): monsters, Mothra and the Kraken in sight, never a player, and none of the six
     * night creatures. {@code EntityMob} is {@link Monster} (util.MyUtils).
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        // PORT: instanceof Kraken (:533), by registry id (same set).
        return this.level().getDifficulty() != Difficulty.PEACEFUL && par1EntityLiving != null && par1EntityLiving != this
                && par1EntityLiving.isAlive() && this.getSensing().hasLineOfSight(par1EntityLiving)
                && !(par1EntityLiving instanceof LurkingTerror) && !(par1EntityLiving instanceof EnderReaper)
                && !(par1EntityLiving instanceof TerribleTerror) && !(par1EntityLiving instanceof LeafMonster)
                && !(par1EntityLiving instanceof CreepingHorror) && !(par1EntityLiving instanceof Triffid)
                && (par1EntityLiving instanceof Monster || par1EntityLiving instanceof Mothra
                    || DragonSupport.isKraken(par1EntityLiving) || (par1EntityLiving instanceof Player && false));
    }

    /** {@code findSomethingToAttack} (:536-553): nothing under {@code PlayNicely}; else the first suitable one by the sorter in ±20. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0, 20.0, 20.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:559-566) - by day, no other dragon within {@code expand(16, 6, 16)}, and
     * the Islands dimension or Y at least 50. The entity is not there yet, so the box is the type's spawn box at the block
     * centre and "other" is every dragon.
     */
    public static boolean checkDragonSpawnRules(final EntityType<Dragon> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final ServerLevel world = level.getLevel();
        if (!HerbivoreSupport.isDaytime(world)) {
            return false;
        }
        final boolean none = level.getEntitiesOfClass(Dragon.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 6.0, 16.0)).isEmpty();
        return none && (world.dimension() == InsectSupport.DIMENSION_ISLANDS || pos.getY() >= 50.0);
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkDragonSpawnRules}; {@code EntityAnimal}'s grass test was overridden. */
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
     * {@code canSeeTarget} (:568-570): {@code rayTraceBlocks(from, to, false) == null} from 0.75 above the feet. 1.7.10
     * traced blocks by their selection box and ignored liquids ({@code ClipContext.Block.OUTLINE}, W06 GoldFish).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code setPositionAndRotation2} (:572-582), client: the vanilla interpolation target ({@code super}) plus the
     * boat copy that {@link #aiStep()} applies in flight.
     */
    @Override
    public void lerpTo(final double par1, final double par3, final double par5, final float par7, final float par8, final int par9) {
        super.lerpTo(par1, par3, par5, par7, par8, par9);
        this.boatPosRotationIncrements = par9;
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
        this.boatYawHead = par7;
    }

    /**
     * The pending interpolation target for rotation-only packets. 1.7.10 used the tracked position; in flight the
     * vanilla counter never runs down, so the boat target answers (W06 Ostrich).
     */
    @Override
    public double lerpTargetX() {
        if (this.getActivity() != 0) {
            return this.boatPosRotationIncrements > 0 ? this.boatX : this.getX();
        }
        return super.lerpTargetX();
    }

    @Override
    public double lerpTargetY() {
        if (this.getActivity() != 0) {
            return this.boatPosRotationIncrements > 0 ? this.boatY : this.getY();
        }
        return super.lerpTargetY();
    }

    @Override
    public double lerpTargetZ() {
        if (this.getActivity() != 0) {
            return this.boatPosRotationIncrements > 0 ? this.boatZ : this.getZ();
        }
        return super.lerpTargetZ();
    }

    /**
     * {@code onUpdate} (:589-617): the speed attribute every tick, the vanilla update, the immunity countdown, wing beats
     * every 21 ticks in flight, buoyancy in water, and take-off when a tamed ground dragon is more than 12 blocks from
     * its owner.
     */
    @Override
    public void tick() {
        LivingEntity e = null;
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.getActivity() == 1) {
            ++this.wing_sound;
            if (this.wing_sound > 20) {
                if (!this.level().isClientSide) {
                    DragonSupport.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 0.5f, 1.0f);
                }
                this.wing_sound = 0;
            }
        }
        if (this.isInWater()) {
            final Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, m.y + 0.07, m.z);
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.getActivity() == 0 && this.isTame() && this.getOwner() != null && !this.isSitting()) {
            e = this.getOwner();
            if (this.distanceToSqr(e) > 144.0) {
                this.setActivity(1);
            }
        }
    }

    /**
     * {@code fly_without_rider} (:619-846): unstick, follow or hunt, pick a visible air block as the next waypoint, lift
     * over what is below the flight path, steer towards the waypoint and move.
     */
    private void flyWithoutRider() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        int do_new = 0;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        int has_owner = 0;
        LivingEntity e = null;
        double speed_factor = 0.5;
        double var1 = 0.0;
        double var2 = 0.0;
        double var3 = 0.0;
        final double yoff = 1.25;
        final double xzoff = 2.25;
        double obstruction_factor = 0.0;
        final Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        // See the class comment: the posY the original raised before moveEntity.
        double posY = this.getY();
        final double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        int toofar = 0;
        if (this.currentFlightTarget == null) {
            do_new = 1;
            // :645 (int) casts -> Mth.floor (DECISIONS R20), here and below.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(posY), Mth.floor(this.getZ()));
        }
        if (this.isSitting()) {
            return;
        }
        if (this.isVehicle()) {
            return;
        }
        if (this.unstick_timer > 0) {
            --this.unstick_timer;
        }
        if (this.lastX == Mth.floor(this.getX()) && this.lastZ == Mth.floor(this.getZ())) {
            ++this.stuck_count;
            if (this.stuck_count > 50) {
                this.stuck_count = 0;
                this.unstick_timer = 100;
                this.target_in_sight = false;
                this.setAttacking(0);
                do_new = 1;
            }
        } else {
            this.stuck_count = 0;
            this.lastX = Mth.floor(this.getX());
            this.lastZ = Mth.floor(this.getZ());
        }
        // :671-679: the second branch catches every case the first one leaves, the third is unreachable.
        if (posY < this.currentFlightTarget.getY() + 2.0) {
            motionY *= 0.7;
        } else if (posY > this.currentFlightTarget.getY() - 2.0) {
            motionY *= 0.5;
        } else {
            motionY *= 0.61;
        }
        if (this.level().random.nextInt(300) == 1) {
            do_new = 1;
        }
        if (this.isTame() && this.getOwner() != null) {
            e = this.getOwner();
            has_owner = 1;
            ox = e.getX();
            oy = e.getY();
            oz = e.getZ();
            if (this.distanceToSqr(e) > 144.0) {
                toofar = 1;
                this.target_in_sight = false;
                this.setAttacking(0);
                this.flyaway = 0;
                do_new = 1;
            }
        }
        if (this.flyaway > 0) {
            --this.flyaway;
        }
        if (toofar == 0 && this.unstick_timer == 0 && this.flyaway == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.level().random.nextInt(9) == 1) {
            e = this.findSomethingToAttack();
            if (e != null) {
                if (this.isTame() && this.getHealth() / this.mygetMaxHealth() < 0.25f) {
                    this.setActivity(1);
                    this.setAttacking(0);
                    this.target_in_sight = false;
                    do_new = 0;
                    this.currentFlightTarget.set(Mth.floor(this.getX() + (this.getX() - e.getX())), Mth.floor(posY + 1.0),
                            Mth.floor(this.getZ() + (this.getZ() - e.getZ())));
                } else {
                    this.setActivity(1);
                    this.setAttacking(1);
                    this.target_in_sight = true;
                    this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                    do_new = 0;
                    if (this.distanceToSqr(e) < (5.0f + e.getBbWidth() / 2.0f) * (5.0f + e.getBbWidth() / 2.0f)) {
                        this.doHurtTarget(e);
                        this.flyaway = 5 + this.level().random.nextInt(10);
                        do_new = 1;
                    } else if (this.distanceToSqr(e) < 256.0 && !this.isInWater() && this.getDragonFire() >= 1) {
                        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
                        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
                        if (this.dragontype == 0) {
                            if (this.getDragonFire() == 1) {
                                final SmallFireball sf = DragonSupport.legacySmallFireball(this, e.getX() - cx,
                                        e.getY() + e.getBbHeight() / 2.0f - (posY + yoff), e.getZ() - cz);
                                sf.moveTo(cx, posY + yoff, cz, this.getYRot(), 0.0f);
                                sf.setPos(cx, posY + yoff, cz);
                                DragonSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f,
                                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                                this.level().addFreshEntity(sf);
                            } else {
                                final BetterFireball bf = new BetterFireball(this.level(), this, e.getX() - cx,
                                        e.getY() + e.getBbHeight() / 2.0f - (posY + yoff), e.getZ() - cz);
                                bf.moveTo(cx, posY + yoff, cz, this.getYRot(), 0.0f);
                                bf.setPos(cx, posY + yoff, cz);
                                DragonSupport.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f,
                                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                                this.level().addFreshEntity(bf);
                            }
                        } else if (this.getDragonFire() == 1) {
                            // new WaterBall(world, dx, dy, dz): the coordinate constructor, repositioned right away (:741-742).
                            final WaterBall wb = new WaterBall(this.level(), e.getX() - this.getX(),
                                    e.getY() + e.getBbHeight() / 2.0f - (posY + yoff), e.getZ() - this.getZ());
                            wb.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())), posY + yoff,
                                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())), this.getYHeadRot(), this.getXRot());
                            var2 = e.getX() - wb.getX();
                            var3 = e.getY() + 0.25 - wb.getY();
                            final double var4 = e.getZ() - wb.getZ();
                            final float var5 = (float) Math.sqrt(var2 * var2 + var4 * var4) * 0.2f;
                            wb.setThrowableHeading(var2, var3 + var5, var4, 1.4f, 5.0f);
                            DragonSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f,
                                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                            this.level().addFreshEntity(wb);
                        } else {
                            final IceBall ib = new IceBall(this.level(), e.getX() - this.getX(),
                                    e.getY() + e.getBbHeight() / 2.0f - (posY + yoff), e.getZ() - this.getZ());
                            ib.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())), posY + yoff,
                                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())), this.getYHeadRot(), this.getXRot());
                            ib.setSpecial();
                            ib.setIceBall();
                            var2 = e.getX() - ib.getX();
                            var3 = e.getY() + 0.25 - ib.getY();
                            final double var4 = e.getZ() - ib.getZ();
                            final float var5 = (float) Math.sqrt(var2 * var2 + var4 * var4) * 0.2f;
                            ib.setThrowableHeading(var2, var3 + var5, var4, 1.4f, 5.0f);
                            DragonSupport.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f,
                                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                            this.level().addFreshEntity(ib);
                        }
                    }
                }
            } else {
                this.target_in_sight = false;
                this.setAttacking(this.flyaway = 0);
            }
        }
        // ChunkCoordinates.getDistanceSquared(int, int, int) is a float of the int differences.
        final int cdx = this.currentFlightTarget.getX() - Mth.floor(this.getX());
        final int cdy = this.currentFlightTarget.getY() - Mth.floor(posY);
        final int cdz = this.currentFlightTarget.getZ() - Mth.floor(this.getZ());
        if ((float) (cdx * cdx + cdy * cdy + cdz * cdz) < 2.1f) {
            do_new = 1;
        }
        if ((do_new != 0 && !this.target_in_sight) || (do_new != 0 && this.flyaway != 0)) {
            for (boolean airFound = false; !airFound && keep_trying != 0; --keep_trying) {
                int gox = Mth.floor(this.getX());
                int goy = Mth.floor(posY);
                int goz = Mth.floor(this.getZ());
                if (has_owner == 1 && this.unstick_timer == 0) {
                    gox = Mth.floor(ox);
                    goy = Mth.floor(oy);
                    goz = Mth.floor(oz);
                    if (this.owner_flying == 0) {
                        zdir = this.level().random.nextInt(10) + 4;
                        xdir = this.level().random.nextInt(10) + 4;
                    } else {
                        zdir = this.level().random.nextInt(6);
                        xdir = this.level().random.nextInt(6);
                    }
                } else {
                    zdir = this.level().random.nextInt(10) + 16;
                    xdir = this.level().random.nextInt(10) + 16;
                }
                if (this.level().random.nextInt(2) == 1) {
                    zdir = -zdir;
                }
                if (this.level().random.nextInt(2) == 1) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(gox + xdir, goy + this.level().random.nextInt(9 + this.owner_flying * 2) - 4, goz + zdir);
                airFound = this.level().getBlockState(this.currentFlightTarget).isAir();
                if (airFound && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                        this.currentFlightTarget.getZ())) {
                    airFound = false;
                }
            }
        }
        obstruction_factor = 0.0;
        int dist = 2;
        dist += (int) (velocity * 4.0);
        for (int k = 1; k < dist; ++k) {
            for (int i = 1; i < dist * 2; ++i) {
                final double dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                final double dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                final BlockState bid = this.level().getBlockState(
                        new BlockPos(Mth.floor(this.getX() + dx), Mth.floor(posY) - k, Mth.floor(this.getZ() + dz)));
                if (!bid.isAir()) {
                    obstruction_factor += 0.05;
                }
            }
        }
        motionY += obstruction_factor * 0.05;
        posY += obstruction_factor * 0.05;
        speed_factor = 0.5;
        var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        var2 = this.currentFlightTarget.getY() + 0.1 - posY;
        var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.owner_flying != 0) {
            speed_factor = 1.75;
            if (this.isTame() && this.getOwner() != null) {
                e = this.getOwner();
                if (this.distanceToSqr(e) > 49.0) {
                    speed_factor = 3.5;
                }
            }
        }
        motionX += (Math.signum(var1) - motionX) * 0.15 * speed_factor;
        motionY += (Math.signum(var2) - motionY) * 0.21 * speed_factor;
        motionZ += (Math.signum(var3) - motionZ) * 0.15 * speed_factor;
        final float var6 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var7 = Mth.wrapDegrees(var6 - this.getYRot());
        this.setZza((float) (0.75 * speed_factor));
        this.setYRot(this.getYRot() + var7 / 4.0f);
        this.legacyMoveEntity(motionX, motionY, motionZ);
    }

    /**
     * {@code moveEntity(motionX, motionY, motionZ)}: 1.21.1's {@code move} zeroes the blocked motion components like
     * 1.7.10's did (W04 Elevator), so the motion is stored first and read back by the caller.
     */
    private void legacyMoveEntity(final double motionX, final double motionY, final double motionZ) {
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
    }

    /**
     * {@code onLivingUpdate} (:848-1171). On the ground the vanilla update runs first; in flight it does not (goals,
     * gravity and vanilla movement stay off). The client then interpolates a flying dragon over the packet's step
     * count; the server steers a ridden one or flies alone, and always ends with {@link #always_do()}.
     */
    @Override
    public void aiStep() {
        // :851-852: never read, but each draws from the entity random every tick, on both sides.
        @SuppressWarnings("unused")
        final double d6 = this.random.nextFloat() * 2.0f - 1.0f;
        @SuppressWarnings("unused")
        final double d7 = (this.random.nextInt(2) * 2 - 1) * 0.7;
        if (this.getActivity() == 0) {
            super.aiStep();
        } else if (this.isRemoved()) {
            super.aiStep();
            return;
        }
        if (this.isRemoved()) {
            return;
        }
        if (this.level().isClientSide) {
            if (this.boatPosRotationIncrements > 0 && this.getActivity() != 0) {
                final double d8 = this.getX() + (this.boatX - this.getX()) / this.boatPosRotationIncrements;
                final double d9 = this.getY() + (this.boatY - this.getY()) / this.boatPosRotationIncrements;
                final double d10 = this.getZ() + (this.boatZ - this.getZ()) / this.boatPosRotationIncrements;
                this.setPos(d8, d9, d10);
                this.setXRot(this.getXRot() + (float) ((this.boatPitch - this.getXRot()) / this.boatPosRotationIncrements));
                double d11 = Mth.wrapDegrees(this.boatYaw - this.getYRot());
                final Entity rider = this.getFirstPassenger();
                if (rider != null) {
                    d11 = Mth.wrapDegrees(rider.getYRot() - (double) this.getYRot());
                }
                this.setYRot(this.getYRot() + (float) (d11 / this.boatPosRotationIncrements));
                this.setRot(this.getYRot(), this.getXRot());
                this.setYHeadRot(this.getYRot());
                --this.boatPosRotationIncrements;
            }
        } else {
            if (this.getActivity() != 0) {
                if (this.fireballticker > 0) {
                    --this.fireballticker;
                }
                final Entity rider = this.getFirstPassenger();
                if (rider != null) {
                    this.flyWithRider(rider);
                } else {
                    this.flyWithoutRider();
                }
            }
            this.always_do();
        }
    }

    /** The server branch of {@code onLivingUpdate} with a rider (:893-1164). */
    private void flyWithRider(final Entity rider) {
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.95;
        double gh = 1.0;
        double pi = 3.1415926545;
        double deltav = 0.0;
        int dist = 2;
        // PORT: (EntityPlayer) this.riddenByEntity (:894) threw for any other rider; 1.21.1's /ride can seat a mob. A
        // non-player rider reads as no forward and no strafe input (R18 case 1, W04 Elevator, W06 Ostrich).
        final Player pp = rider instanceof Player player ? player : null;
        final Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        // See the class comment: the posY the original raised before moveEntity.
        double posY = this.getY();
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
        gh = 1.25;
        // :909 (int) posX, (int)((float) posY - (float) gh), (int) posZ -> Mth.floor (DECISIONS R20), here and below.
        BlockState bid = this.level().getBlockState(
                new BlockPos(Mth.floor(this.getX()), Mth.floor((float) posY - (float) gh), Mth.floor(this.getZ())));
        if (!bid.isAir()) {
            motionY += 0.03;
            posY += 0.1;
        } else {
            motionY -= 0.018;
        }
        obstruction_factor = 0.0;
        dist = 3;
        dist += (int) (velocity * 7.0);
        for (int k = 1; k < dist; ++k) {
            for (int i = 1; i < dist * 2; ++i) {
                final double dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                final double dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                bid = this.level().getBlockState(new BlockPos(Mth.floor(this.getX() + dx), Mth.floor(posY) - k, Mth.floor(this.getZ() + dz)));
                if (!bid.isAir()) {
                    obstruction_factor += 0.05;
                }
            }
        }
        motionY += obstruction_factor * 0.07;
        posY += obstruction_factor * 0.07;
        if (motionY > 2.0) {
            motionY = 2.0;
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
        // :957-960: relative_g is recomputed and clamped but never read again.
        relative_g = Math.abs(relative_g) * velocity;
        if (relative_g > 50.0) {
            relative_g = 0.0;
        }
        this.setXRot(2.0f * (float) velocity);
        this.setRot(this.getYRot(), this.getXRot());
        double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        // rr = atan2(rider.motionZ, rider.motionX) (:964) and rt = 0 (:967) are never read.
        final double rhm = Math.atan2(motionZ, motionX);
        final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        pi = 3.1415926545;
        deltav = 0.0;
        final float im = pp != null ? pp.zza : 0.0f; // moveForward
        // OreSpawnMain.flyup_keystate != 0 (:971): per rider (R15).
        if (RiderKeys.isFlyUp(rider)) {
            motionY += 0.03;
            motionY += velocity * 0.036;
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
                deltav = 0.025;
                if (max_speed > 1.0) {
                    deltav += 0.05;
                }
                if (this.deltasmooth < 0.0f) {
                    this.deltasmooth = 0.0f;
                }
                this.deltasmooth += (float) (deltav / 10.0);
                if (this.deltasmooth > deltav) {
                    this.deltasmooth = (float) deltav;
                }
            } else {
                max_speed = 0.35;
                deltav = -0.02;
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
        if (this.fireballticker == 0 && pp != null) {
            final double xzoff = 4.0;
            final double yoff = -0.25;
            final float strafe = pp.xxa; // moveStrafing
            if (this.getDragonType() == 0) {
                if (strafe > 0.001f) {
                    final BetterFireball bf = new BetterFireball(this.level(), this, 0.0, 0.0, 0.0);
                    bf.setNotMe();
                    bf.setSmall();
                    double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
                    double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
                    bf.setPos(cx, posY + yoff, cz);
                    cx = Math.cos(Math.toRadians(pp.getYHeadRot() + 90.0f));
                    cz = Math.sin(Math.toRadians(pp.getYHeadRot() + 90.0f));
                    final double cy = -Math.sin(Math.toRadians(pp.getXRot()));
                    final double d12 = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
                    bf.setAcceleration(cx / d12 * 0.15, cy / d12 * 0.15, cz / d12 * 0.15);
                    bf.setDeltaMovement(motionX, motionY, motionZ);
                    bf.setPos(bf.getX() - motionX * 9.0, bf.getY() - motionY * 9.0, bf.getZ() - motionZ * 9.0);
                    DragonSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f,
                            1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                    this.level().addFreshEntity(bf);
                    this.fireballticker = 10;
                }
                if (strafe < -0.001f) {
                    final BetterFireball bf = new BetterFireball(this.level(), this, 0.0, 0.0, 0.0);
                    bf.setNotMe();
                    double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
                    double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
                    bf.setPos(cx, posY + yoff, cz);
                    cx = Math.cos(Math.toRadians(pp.getYHeadRot() + 90.0f));
                    cz = Math.sin(Math.toRadians(pp.getYHeadRot() + 90.0f));
                    final double cy = -Math.sin(Math.toRadians(pp.getXRot()));
                    final double d12 = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
                    bf.setAcceleration(cx / d12 * 0.1, cy / d12 * 0.1, cz / d12 * 0.1);
                    bf.setDeltaMovement(motionX, motionY, motionZ);
                    bf.setPos(bf.getX() - motionX * 9.0, bf.getY() - motionY * 9.0, bf.getZ() - motionZ * 9.0);
                    DragonSupport.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f,
                            1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                    this.level().addFreshEntity(bf);
                    this.fireballticker = 20;
                }
            } else {
                if (strafe > 0.001f) {
                    final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
                    final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
                    final WaterBall var2 = new WaterBall(this.level(), cx, posY + yoff, cz);
                    var2.moveTo(cx, posY + yoff, cz, pp.getYRot() + 90.0f, pp.getXRot());
                    final double var3 = Math.cos(Math.toRadians(pp.getYHeadRot() + 90.0f));
                    final double var4 = -Math.sin(Math.toRadians(pp.getXRot()));
                    final double var5 = Math.sin(Math.toRadians(pp.getYHeadRot() + 90.0f));
                    final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                    var2.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 5.0f);
                    var2.setPos(var2.getX() - motionX * 7.0, var2.getY() - motionY * 7.0, var2.getZ() - motionZ * 7.0);
                    DragonSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f,
                            1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                    this.level().addFreshEntity(var2);
                    this.fireballticker = 5;
                }
                if (strafe < -0.001f) {
                    final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
                    final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
                    final IceBall var7 = new IceBall(this.level(), cx, posY + yoff, cz);
                    var7.moveTo(cx, posY + yoff, cz, pp.getYRot() + 90.0f, pp.getXRot());
                    var7.setSpecial();
                    var7.setIceBall();
                    final double var3 = Math.cos(Math.toRadians(pp.getYRot() + 90.0f));
                    final double var4 = -Math.sin(Math.toRadians(pp.getXRot()));
                    final double var5 = Math.sin(Math.toRadians(pp.getYRot() + 90.0f));
                    final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                    var7.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 5.0f);
                    var7.setPos(var7.getX() - motionX * 7.0, var7.getY() - motionY * 7.0, var7.getZ() - motionZ * 7.0);
                    final Vec3 im2 = var7.getDeltaMovement();
                    var7.setDeltaMovement(im2.x * 2.0, im2.y * 2.0, im2.z * 2.0);
                    DragonSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 0.75f,
                            1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                    this.level().addFreshEntity(var7);
                    this.fireballticker = 15;
                }
            }
        }
        this.legacyMoveEntity(motionX, motionY, motionZ);
        final Vec3 moved = this.getDeltaMovement();
        this.setDeltaMovement(moved.x * 0.985, moved.y * 0.94, moved.z * 0.985);
        if (!this.level().isClientSide) {
            final List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(2.25, 2.0, 2.25));
            if (list != null && !list.isEmpty()) {
                for (int l = 0; l < list.size(); ++l) {
                    final Entity listEntity = list.get(l);
                    // canBePushed -> isPushable (1.7.10 EntityLivingBase: !isDead).
                    if (listEntity != rider && !listEntity.isRemoved() && listEntity.isPushable()) {
                        DragonSupport.applyEntityCollision(listEntity, this);
                    }
                }
            }
        }
        this.fly_with_rider();
        if (rider.isRemoved()) {
            rider.stopRiding();
        }
    }

    /**
     * {@code updateRiderPosition} (:1173-1178): 0.65 along the yaw, {@code posY + 1.3 + rider.getYOffset()}. A
     * server-side 1.7.10 player had {@code getYOffset() = -0.5}; 1.21.1 positions passengers by their feet (W04
     * Elevator, W06 Ostrich). A non-player {@code /ride} passenger gets 0.
     */
    @Override
    protected void positionRider(final Entity passenger, final Entity.MoveFunction callback) {
        final float f = 0.65f;
        final double yOffset = passenger instanceof Player ? -0.5 : 0.0;
        callback.accept(passenger,
                this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                this.getY() + this.getMountedYOffset() + yOffset,
                this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())));
    }

    /**
     * {@code playTameEffect} (:1180-1191): 20 hearts or smoke puffs in a 5-block cloud; on the server (where 1.7.10's
     * {@code spawnParticle} did nothing, as 1.21.1's does) and on the client through entity events 6 and 7.
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

    /**
     * {@code interact} (:1193-1392), both sides like the original. A {@code true} answer is {@code sidedSuccess}. The
     * click order around it (name tag, a stranger's lead) is restored by {@link DragonInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return this.legacyInteract(par1EntityPlayer) ? InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
    }

    /** The body of {@code interact(EntityPlayer)} (:1193-1392). {@code func_152114_e} is "is owner". */
    boolean legacyInteract(final Player par1EntityPlayer) {
        final boolean isRemote = this.level().isClientSide;
        // :1195-1198 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (!this.isTame()) {
            if (!var2.isEmpty() && var2.is(Items.BEEF) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (!isRemote) {
                    if (this.level().random.nextInt(5) == 1) {
                        // PORT: setTamed + owner without TamableAnimal.tame's advancement trigger, as in Girlfriend.
                        this.setTame(true, false);
                        this.setOwnerUUID(par1EntityPlayer.getUUID());
                        this.spawnTamingParticles(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.mygetMaxHealth() - this.getHealth());
                    } else {
                        this.spawnTamingParticles(false);
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
        } else {
            if (!this.isOwnedBy(par1EntityPlayer)) {
                return false;
            }
            if (var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 16.0) {
                if (!isRemote) {
                    // PORT: 1.7.10 mountEntity replaced a current rider; 1.21.1 startRiding refuses while the seat is
                    // taken (Entity.canAddPassenger), W06 Ostrich.
                    par1EntityPlayer.startRiding(this);
                    this.setActivity(1);
                    this.setSitting(false);
                }
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.BEEF) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.mygetMaxHealth() > this.getHealth()) {
                    this.heal(this.mygetMaxHealth() - this.getHealth());
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (!isRemote) {
                    this.setTame(false, false);
                    this.setOwnerUUID(DragonSupport.parseOwner("")); // func_152115_b("")
                    this.spawnTamingParticles(false);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.ICE) && par1EntityPlayer.distanceToSqr(this) < 25.0 && this.isOwnedBy(par1EntityPlayer)) {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    this.setDragonFire(0);
                    par1EntityPlayer.sendSystemMessage(Component.literal("Dragon fireballs extinguished."));
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.FLINT_AND_STEEL) && par1EntityPlayer.distanceToSqr(this) < 25.0
                    && this.isOwnedBy(par1EntityPlayer)) {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    this.setDragonFire(1);
                    par1EntityPlayer.sendSystemMessage(Component.literal("Dragon fireballs lit!"));
                }
                // --stackSize on a damageable item: the whole flint and steel goes (R18).
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.GUNPOWDER) && par1EntityPlayer.distanceToSqr(this) < 25.0
                    && this.isOwnedBy(par1EntityPlayer) && this.getDragonFire() > 0) {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    this.setDragonFire(2);
                    par1EntityPlayer.sendSystemMessage(Component.literal("Dragon fireballs supercharged!"));
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.SNOWBALL) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                // setDragonType has no client guard (:1442-1444): both sides write, the server's value wins.
                this.setDragonType(this.dragontype = 1);
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.COAL) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                this.setDragonType(this.dragontype = 0);
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.DIAMOND) && par1EntityPlayer.distanceToSqr(this) < 25.0
                    && this.isOwnedBy(par1EntityPlayer) && !isRemote) {
                Spyro d = null;
                d = DragonSupport.spawnCreature(this.level(), ModEntities.BABY_DRAGON.get(), this.getX(), this.getY(), this.getZ());
                if (d != null) {
                    if (this.isTame()) {
                        d.setTame(true, false);
                        d.setOwnerUUID(DragonSupport.parseOwner(par1EntityPlayer.getUUID().toString()));
                    }
                    this.discard();
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 25.0
                    && this.isOwnedBy(par1EntityPlayer)) {
                this.setCustomName(var2.getHoverName());
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (!this.isSitting()) {
                    this.setSitting(true);
                    this.setActivity(0);
                } else {
                    this.setSitting(false);
                    this.setActivity(0);
                }
                return true;
            }
        }
        return false;
    }

    /** {@code isWheat} (:1394-1396): overrides nothing in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.BEEF);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}, not overridden: wheat. Nothing reads it, the dragon never breeds. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(Items.WHEAT);
    }

    /** {@code getAttacking} (:1398-1400). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:1402-1407): server only. */
    public void setAttacking(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getActivity} (:1409-1411). */
    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    /** {@code setActivity} (:1413-1418): server only. */
    public void setActivity(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code getDragonFire} (:1420-1422). */
    public int getDragonFire() {
        return this.entityData.get(DATA_DRAGON_FIRE);
    }

    /** {@code setDragonFire} (:1424-1429): server only. */
    public void setDragonFire(final int par1) {
        if (this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_DRAGON_FIRE, par1);
    }

    /** {@code setDragonType} (:1442-1444): no side check. */
    public void setDragonType(final int par1) {
        this.entityData.set(DATA_DRAGON_TYPE, par1);
    }

    /** {@code getDragonType} (:1446-1448), read by {@code RenderDragon} for the texture. */
    public int getDragonType() {
        return this.entityData.get(DATA_DRAGON_TYPE);
    }

    /** {@code createChild} (:1450-1452). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** {@code canDespawn} (:1454-1456): only without a rider, not persistent and wild. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.isVehicle() && !this.isTame();
    }

    /** {@code writeEntityToNBT} (:1458-1464). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("DragonAttacking", this.getAttacking());
        par1NBTTagCompound.putInt("DragonActivity", this.getActivity());
        par1NBTTagCompound.putInt("DragonFire", this.getDragonFire());
        par1NBTTagCompound.putInt("DragonType", this.getDragonType());
    }

    /** {@code readEntityFromNBT} (:1466-1472). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setAttacking(par1NBTTagCompound.getInt("DragonAttacking"));
        this.setActivity(par1NBTTagCompound.getInt("DragonActivity"));
        this.setDragonFire(par1NBTTagCompound.getInt("DragonFire"));
        this.setDragonType(this.dragontype = par1NBTTagCompound.getInt("DragonType"));
    }

    /** 1.7.10 {@code EntityTameable.setSitting}: one synced flag; 1.21.1 splits it into order and pose. */
    public void setSitting(final boolean sitting) {
        HerbivoreSupport.setSitting(this, sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * {@code onDeath}: not overridden, and 1.7.10 {@code EntityTameable} sent the owner no chat message.
     *
     * <p>PORT: R22 (owner death message of tame OreSpawn animals 1:1 off) - {@code TamableAnimal.die} in 1.21.1 sends
     * {@code getCombatTracker().getDeathMessage()} to the owner. Java cannot skip one super level, so this is
     * {@code LivingEntity.die} line by line (NeoForge 21.1 sources) without the owner message, as in
     * {@code Camarasaurus} (fix1).
     */
    @Override
    public void die(final DamageSource damageSource) {
        if (net.neoforged.neoforge.common.CommonHooks.onLivingDeath(this, damageSource)) {
            return;
        }
        if (!this.isRemoved() && !this.dead) {
            final Entity entity = damageSource.getEntity();
            final LivingEntity livingentity = this.getKillCredit();
            if (this.deathScore >= 0 && livingentity != null) {
                livingentity.awardKillScore(this, this.deathScore, damageSource);
            }
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            if (!this.level().isClientSide && this.hasCustomName()) {
                com.mojang.logging.LogUtils.getLogger().info("Named entity {} died: {}", this,
                        this.getCombatTracker().getDeathMessage().getString());
            }
            this.dead = true;
            this.getCombatTracker().recheckStatus();
            if (this.level() instanceof ServerLevel serverlevel) {
                if (entity == null || entity.killedEntity(serverlevel, this)) {
                    this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.ENTITY_DIE);
                    this.dropAllDeathLoot(serverlevel, damageSource);
                    this.createWitherRose(livingentity);
                }
                this.level().broadcastEntityEvent(this, (byte) 3);
            }
            this.setPose(net.minecraft.world.entity.Pose.DYING);
        }
    }

    /**
     * {@code EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)} (:91): the
     * vanilla 1.7.10 goal ({@link EntityAINearestAttackableTarget}, W05) with chance 0 (every evaluation), sight
     * required, and the selector {@code entity instanceof IMob} ({@link Enemy}). {@code EntityLiving} is {@link Mob}.
     */
    private static final class NearestMobTarget extends EntityAINearestAttackableTarget<Mob> {
        NearestMobTarget(final Dragon dragon) {
            super(dragon, Mob.class, 0, true);
            this.targetConditions = TargetingConditions.forCombat().selector(e -> e instanceof Enemy);
        }
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
