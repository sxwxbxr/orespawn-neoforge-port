package com.swbr.orespawn.entity.boss.prince;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.dragon.Spyro;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.leon.Leon;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.portal.EntityAINearestAttackableTarget;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.entity.waterdragon.GammaMetroid;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.util.Royalty;
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
import net.minecraft.world.food.FoodProperties;
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
 * Port of {@code danger.orespawn.ThePrinceTeen} (ThePrinceTeen.java:19-1498), id {@code the_young_prince} ("The Young
 * Prince", OreSpawnMain.java:4065, tracking 64/1/false). The rideable teenage stage of the royal line on
 * {@code EntityTameable}: 1500 health, a 45-point bite with knockback, fireballs, ice balls and thunderbolts that the
 * rider fires with the strafe keys; grows into the Young Adult Prince, a diamond turns it back into {@link ThePrince}
 * (verhalten/entity-02.md, "ThePrinceTeen").
 *
 * <h2>Two phases</h2>
 * {@code activity} 0 is a ground mob with vanilla movement and goals. {@code activity} 1 is flight: the original's
 * {@code onLivingUpdate} then skips {@code super.onLivingUpdate} (no goals, no gravity, no vanilla movement, no growth)
 * and moves the prince itself - {@link #fly_without_rider()} or, with a rider, the server-side steering in
 * {@link #aiStep()}. As W09 Dragon, {@link #aiStep()} calls {@code super.aiStep()} only on the ground. In flight
 * {@code noClip} ({@code noPhysics}) is set.
 *
 * <h2>Riding</h2>
 * As W09 Dragon (and W04 Elevator, W06 Ostrich): the server steers from {@code Player.zza}/{@code xxa}, which
 * {@code ServerGamePacketListenerImpl.handlePlayerInput} writes on the server while riding (catalogue 6.3); the fly-up
 * key is {@link RiderKeys#isFlyUp(Entity)} per rider (R15) instead of the global {@code flyup_keystate}.
 *
 * <h2>Virtual health (R4)</h2>
 * The original 1500 is above the 1.21.1 clamp. The attribute holds {@link LegacyCombatMath#attributeMaxHealth};
 * {@code CombatEvents} scales incoming damage and every {@code heal} (both written in original units here). The three
 * places that <em>read</em> health against 1500 - regeneration, the flee threshold, beef and food - read
 * {@link VirtualHealth#originalHealth}.
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>A tamed prince takes a player's hit and answers {@code false} without targeting the player (:357-362).</li>
 *   <li>{@code this.posY += ...} before {@code moveEntity} is overwritten by the move but read by the block scans and
 *       the rider's muzzle of the same tick; a local {@code posY} keeps both (W09 Dragon).</li>
 *   <li>Beef heals on both sides, its particles only appear on the client (:1186-1202).</li>
 *   <li>{@code experienceValue = 300} (:79) is never read (W04 lesson); {@code RenderInfo} is a client scratch pad the
 *       model writes.</li>
 *   <li>Growth ({@code kill_count > 25 && day_count > 10}) and the sunrise count only run on the ground.</li>
 * </ul>
 *
 * <p>Not carried over: {@code getTrackingRange} 64, {@code getUpdateFrequency} 10, {@code sendsVelocityUpdates}
 * (:122-132) override nothing (registration 64/1/false counts, W09 Dragon); {@code isAIEnabled} (:235) is the 1.21.1
 * default; {@code canBreatheUnderwater} (:239) is the {@code minecraft:can_breathe_under_water} tag;
 * {@code canTriggerWalking} true and {@code doesEntityNotTriggerPressurePlate} false (:173, :538) are the defaults, so
 * no {@code NoStepTrigger} (R20); {@code updateAITick} (:469-474) skips nothing ({@code EntityLiving.updateAITick} is
 * empty); {@code updateit}, {@code playing} are never read; {@code setVelocity} (:562-565) only calls {@code super}.
 */
public class ThePrinceTeen extends TamableAnimal implements Royalty, LegacyArmor, VirtualHealth {

    /** DataWatcher 20: attacking (0/1). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    /** DataWatcher 21: activity (0 ground, 1 flight). */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    /** DataWatcher 22: head1ext. */
    private static final EntityDataAccessor<Integer> DATA_HEAD1_EXT = SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    /** DataWatcher 23: head2ext. */
    private static final EntityDataAccessor<Integer> DATA_HEAD2_EXT = SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    /** DataWatcher 24: ThePrinceTeenFire (0 out, 1 lit), initial 1. */
    private static final EntityDataAccessor<Integer> DATA_FIRE = SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);
    /** DataWatcher 25: head3ext. */
    private static final EntityDataAccessor<Integer> DATA_HEAD3_EXT = SynchedEntityData.defineId(ThePrinceTeen.class, EntityDataSerializers.INT);

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
    private float moveSpeed;
    private float deltasmooth;
    private int which_attack;
    private int fireballticker;
    private int head1ext;
    private int head2ext;
    private int head3ext;
    private int head1dir;
    private int head2dir;
    private int head3dir;
    private int kill_count;
    private int day_count;
    private int is_day;

    /**
     * {@code ThePrinceTeen(World)} (:52-97). {@code setSize(3.25f, 4.25f)} is the entity type's size (R9);
     * {@code fireResistance = 1000} is {@link #getFireImmuneTicks()}, {@code isImmuneToFire} the type's
     * {@code fireImmune()}. The {@code entityInit} defaults (:177-201) are {@link #defineSynchedData}.
     */
    public ThePrinceTeen(final EntityType<? extends ThePrinceTeen> type, final Level par1World) {
        super(type, par1World);
        this.renderdata = new RenderInfo();
        this.hurt_timer = 0;
        this.wing_sound = 0;
        this.currentFlightTarget = null;
        this.target_in_sight = false;
        this.owner_flying = 0;
        this.flyaway = 0;
        this.moveSpeed = 0.32f;
        this.deltasmooth = 0.0f;
        this.which_attack = 0;
        this.fireballticker = 0;
        this.head1ext = 0;
        this.head2ext = 0;
        this.head3ext = 0;
        this.head1dir = 1;
        this.head2dir = 1;
        this.head3dir = 1;
        this.kill_count = 0;
        this.day_count = 0;
        this.is_day = 0;
        // getNavigator().setAvoidsWater(true) (:78): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 1.1f, 12.0f, 2.0f));
        this.goalSelector.addGoal(2, new EntityAITempt(this, 1.25, stack -> stack.is(Items.BEEF), false));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.75f));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Mob.class, 9.0f));
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
     * {@code applyEntityAttributes} (:110-116): health {@link #mygetMaxHealth()} 1500 behind the R4 clamp, attack 50
     * (never read: {@link #doHurtTarget} strikes with a fixed 45), speed 0.32 (rewritten every tick in {@link #tick()}).
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W09 Dragon); 1.21.1 defaults
     * to 0.6. It matters for the explicit {@code moveEntity} of both flight modes.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(1500.0))
                .add(Attributes.MOVEMENT_SPEED, (double) 0.32f)
                .add(Attributes.ATTACK_DAMAGE, 50.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** R4: {@code mygetMaxHealth()} 1500. */
    @Override
    public double getOriginalMaxHealth() {
        return this.mygetMaxHealth();
    }

    /** {@code shouldRiderSit} (:118-120). */
    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    /** {@code getHead1Ext} (:134-136): DataWatcher 22. */
    public int getHead1Ext() {
        return this.entityData.get(DATA_HEAD1_EXT);
    }

    /** {@code getHead2Ext} (:138-140): DataWatcher 23. */
    public int getHead2Ext() {
        return this.entityData.get(DATA_HEAD2_EXT);
    }

    /** {@code getHead3Ext} (:142-144): DataWatcher 25. */
    public int getHead3Ext() {
        return this.entityData.get(DATA_HEAD3_EXT);
    }

    /** {@code setHead1Ext} (:146-151): server only. */
    public void setHead1Ext(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_HEAD1_EXT, par1);
    }

    /** {@code setHead2Ext} (:153-158): server only. */
    public void setHead2Ext(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_HEAD2_EXT, par1);
    }

    /** {@code setHead3Ext} (:160-165): server only. */
    public void setHead3Ext(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_HEAD3_EXT, par1);
    }

    /** {@code fall} (:167-168): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:170-171): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code entityInit} (:177-201): 20 = 0, 21 = 0, 24 = 1, 22/23/25 = 0; {@code setActivity(0)}, {@code setAttacking(0)},
     * {@code setThePrinceTeenFire(1)} write the same defaults; tame flags from {@code TamableAnimal}.
     */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_FIRE, 1);
        builder.define(DATA_HEAD1_EXT, 0);
        builder.define(DATA_HEAD2_EXT, 0);
        builder.define(DATA_HEAD3_EXT, 0);
    }

    /** {@code fireResistance = 1000} (:80). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code mygetMaxHealth} (:203-205), in original units. */
    public int mygetMaxHealth() {
        return 1500;
    }

    /** {@code getThePrinceTeenHealth} (:207-209), in original units (R4). */
    public int getThePrinceTeenHealth() {
        return (int) VirtualHealth.originalHealth(this);
    }

    /** {@code getRenderInfo} (:211-213): the model's note pad (plain data, no client imports). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:215-224): copies the fields. */
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

    /** {@code getTotalArmorValue} (:226-228) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 18;
    }

    /** {@code jump} (:230-233): {@code super.jump()} first, so the extra 0.25 survives. */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
    }

    /** {@code getLivingSound} (:243-251): a roar only in flight, without a rider, not sitting. */
    @Nullable
    @Override
    public SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        if (this.getActivity() == 1 && !this.isVehicle()) {
            return ModSounds.ROAR.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:253-255). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:257-259). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:261-263). */
    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    /** {@code getSoundPitch} (:265-267). */
    @Override
    public float getVoicePitch() {
        return 0.75f;
    }

    /** {@code canBePushed} (:269-271). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code getMountedYOffset} (:273-275). */
    public double getMountedYOffset() {
        return 2.75;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}; {@code getDropItem} (:277-279) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:291-293): one Prince egg through {@code dropItemRand} (:281-289), ±1 on {@code OreSpawnRand}, y+1. */
    protected void dropFewItems(final boolean par1, final int par2) {
        ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.EGG_THE_PRINCE.get(), 1), 2);
    }

    /**
     * {@code attackEntityAsMob} (:295-317): a fixed 45 (twice against the Kraken), then a push of 1.75 away and 0.1 up,
     * doubled upward for a player or a removed target; a living target left at 0 health counts as a kill. Always
     * {@code true}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 1.75;
        double inair = 0.1;
        float iskraken = 1.0f;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            if (par1Entity instanceof Kraken) {
                iskraken = 2.0f;
            }
            par1Entity.hurt(this.damageSources().mobAttack(this), iskraken * 45.0f);
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            // isDead: the 1.7.10 removal flag, not the health.
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            // EntityLiving is Mob.
            if (par1Entity instanceof Mob e) {
                if (e.getHealth() <= 0.0f) {
                    ++this.kill_count;
                }
            }
        }
        return true;
    }

    /**
     * {@code attackEntityFrom} (:319-369) in the original order: 20 ticks of immunity after every hit; cactus, fire,
     * lava and suffocation ignored; any other hit wakes it and makes it fly; fireball entities are destroyed; other Young
     * Princes and Spyros do not hurt it; a tamed prince takes a player's hit but answers {@code false}; any other living
     * attacker becomes the target.
     *
     * <p>PORT: {@code getDamageType()} is {@link DamageSource#getMsgId()}, the same string. {@code setAttackTarget(e)} and
     * {@code setTarget(e)} are one {@code setTarget} in 1.21.1. The amount stays in original units; {@code CombatEvents}
     * scales it after {@code super.hurt} is reached (R4).
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
        if (e != null && e instanceof BetterFireball) {
            e.discard();
            return ret;
        }
        // EntitySmallFireball is the vanilla small fireball.
        if (e != null && e instanceof SmallFireball) {
            e.discard();
            return ret;
        }
        if (e != null && e instanceof ThePrinceTeen) {
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
     * {@code updateAITasks} (:371-411) after the vanilla block, on the ground only: with 1 in 10 take off when something
     * to attack is in sight, grow into the Young Adult Prince, count sunrises. {@code updateAITick} (:469-474) skipped the
     * animal's tick while ridden.
     *
     * <p>PORT: 1.21.1 {@code Mob.serverAiStep} is final; the original ran this after the move, look and jump helpers,
     * here it runs right before them. None of the three draws a random number.
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
            } else {
                this.setAttacking(0);
            }
        }
        if (this.kill_count > 25 && this.day_count > 10) {
            // "The Young Adult Prince" is W10's the_young_adult_prince (w10-prince-adult), looked up by registry id.
            final Mob ent = PrinceSupport.spawnCreature(this.level(), PrinceSupport.THE_YOUNG_ADULT_PRINCE, this.getX(), this.getY(), this.getZ());
            if (ent != null) {
                // PORT: (ThePrinceAdult) ent - setTamed and func_152115_b are TamableAnimal's; the cast is kept at that level.
                if (this.isTame() && ent instanceof TamableAnimal d) {
                    d.setTame(true, false);
                    d.setOwnerUUID(this.getOwnerUUID()); // func_152115_b(func_152113_b())
                }
                this.discard();
            }
        }
        if (this.is_day == 0) {
            this.is_day = 1;
            if (!HerbivoreSupport.isDaytime(this.level())) {
                this.is_day = -1;
            }
        } else {
            if (this.is_day == -1 && HerbivoreSupport.isDaytime(this.level())) {
                ++this.day_count;
            }
            this.is_day = 1;
            if (!HerbivoreSupport.isDaytime(this.level())) {
                this.is_day = -1;
            }
        }
    }

    /**
     * 1.7.10 {@code despawnEntity} ran inside {@code updateAITasks}, which a flying prince never reaches. 1.21.1 asks
     * {@code checkDespawn} from the level tick; a flying prince therefore stays, as in the original (W09 Dragon).
     */
    @Override
    public void checkDespawn() {
        if (this.getActivity() != 0) {
            return;
        }
        super.checkDespawn();
    }

    /**
     * {@code always_do} (:413-439), every server tick in both phases: regenerate, forget the target with 1 in 250,
     * follow a flying owner into the air, take off or land at random.
     */
    public void always_do() {
        if (this.level().random.nextInt(250) == 1 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth()) {
            this.heal(2.0f);
        }
        if (this.level().random.nextInt(250) == 0) {
            this.setTarget(null);
        }
        if (this.isSitting()) {
            return;
        }
        this.owner_flying = 0;
        // PORT: (EntityPlayer) getOwner() threw for a non-player owner; 1.21.1 getOwner is any LivingEntity (R18 case 1).
        if (this.isTame() && this.getOwner() instanceof Player p && !this.isVehicle() && !this.isSitting()) {
            if (p.getAbilities().flying) {
                this.setActivity(this.owner_flying = 1);
            }
        }
        if (this.level().random.nextInt(50) == 1 && !this.isSitting() && !this.target_in_sight && !this.isVehicle()) {
            if (this.level().random.nextInt(15) == 1) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
    }

    /**
     * {@code fly_with_rider} (:441-467): with 1 in 5, not Peaceful, look for a target; bite it within
     * {@code (8 + width/2)^2}, or shoot at it between 10 and 25 blocks with the fire lit.
     */
    public void fly_with_rider() {
        LivingEntity e = null;
        if (this.isRemoved()) {
            return;
        }
        if (this.isSitting()) {
            return;
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.level().random.nextInt(5) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setAttacking(1);
                if (this.distanceToSqr(e) < (8.0f + e.getBbWidth() / 2.0f) * (8.0f + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                } else if (this.distanceToSqr(e) > 100.0 && this.distanceToSqr(e) < 625.0 && !this.isInWater()
                        && this.getThePrinceTeenFire() != 0) {
                    this.shoot_something(e.getX(), e.getY(), e.getZ());
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:476-517): monsters, Mothra and the Kraken in sight, wild Leonopteryx, Water Dragons and
     * WTF?s; never royalty. {@code EntityMob} is {@link Monster} (util.MyUtils).
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
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
        if (MyUtils.isRoyalty(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof Mothra) {
            return true;
        }
        if (par1EntityLiving instanceof Kraken) {
            return true;
        }
        if (par1EntityLiving instanceof Leon l) {
            return !l.isTame();
        }
        if (par1EntityLiving instanceof WaterDragon i) {
            return !i.isTame();
        }
        if (par1EntityLiving instanceof GammaMetroid j) {
            return !j.isTame();
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:519-536): nothing under {@code PlayNicely}; else the first suitable one by the sorter in ±25/20/25. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(25.0, 20.0, 25.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** Spawn predicate: {@code getCanSpawnHere} (:542-544) is {@code false}. No natural spawns (manifest). */
    public static boolean checkThePrinceTeenSpawnRules(final EntityType<ThePrinceTeen> type, final ServerLevelAccessor level,
                                                       final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return false;
    }

    /**
     * {@code getCanSpawnHere} (:542-544): {@code false}, so a mob spawner never releases one either (1.7.10 spawners asked
     * the same method). Eggs and {@code /summon} do not ask.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return false;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return false;
    }

    /**
     * {@code canSeeTarget} (:546-548): {@code rayTraceBlocks(from, to, false) == null} from 0.75 above the feet. 1.7.10
     * traced blocks by their selection box and ignored liquids ({@code ClipContext.Block.OUTLINE}, W06 GoldFish).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code setPositionAndRotation2} (:550-560), client: the vanilla interpolation target ({@code super}) plus the boat
     * copy that {@link #aiStep()} applies in flight.
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

    /** The pending interpolation target for rotation-only packets; in flight the boat target answers (W09 Dragon). */
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
     * {@code onUpdate} (:567-663): the speed attribute every tick, the vanilla update, {@code noClip} in flight, the head
     * swings rolled and synchronised by the server, the immunity countdown, wing beats every 21 ticks in flight,
     * buoyancy in water, and take-off when a tamed ground prince is more than 20 blocks from its owner.
     */
    @Override
    public void tick() {
        LivingEntity e = null;
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.getActivity() != 0) {
            this.noPhysics = true;
        } else {
            this.noPhysics = false;
        }
        if (!this.level().isClientSide) {
            if (this.level().random.nextInt(10) == 1) {
                final int i = this.level().random.nextInt(3);
                if (i == 0) {
                    this.head1dir = 2;
                }
                if (i == 1) {
                    this.head1dir = -2;
                }
                if (i == 2) {
                    this.head1dir = 0;
                }
            }
            if (this.level().random.nextInt(10) == 1) {
                final int i = this.level().random.nextInt(3);
                if (i == 0) {
                    this.head2dir = 2;
                }
                if (i == 1) {
                    this.head2dir = -2;
                }
                if (i == 2) {
                    this.head2dir = 0;
                }
            }
            if (this.level().random.nextInt(10) == 1) {
                final int i = this.level().random.nextInt(3);
                if (i == 0) {
                    this.head3dir = 2;
                }
                if (i == 1) {
                    this.head3dir = -2;
                }
                if (i == 2) {
                    this.head3dir = 0;
                }
            }
            this.head1ext += this.head1dir;
            if (this.head1ext < 0) {
                this.head1ext = 0;
            }
            if (this.head1ext > 60) {
                this.head1ext = 60;
            }
            this.head2ext += this.head2dir;
            if (this.head2ext < 0) {
                this.head2ext = 0;
            }
            if (this.head2ext > 60) {
                this.head2ext = 60;
            }
            this.head3ext += this.head3dir;
            if (this.head3ext < 0) {
                this.head3ext = 0;
            }
            if (this.head3ext > 60) {
                this.head3ext = 60;
            }
            this.setHead1Ext(this.head1ext);
            this.setHead2Ext(this.head2ext);
            this.setHead3Ext(this.head3ext);
        }
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.getActivity() == 1) {
            ++this.wing_sound;
            if (this.wing_sound > 20) {
                if (!this.level().isClientSide) {
                    PrinceSupport.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 0.5f, 1.0f);
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
            if (this.distanceToSqr(e) > 400.0) {
                this.setActivity(1);
            }
        }
    }

    /**
     * {@code fly_without_rider} (:665-835): follow or hunt, pick a visible air block as the next waypoint, lift over what
     * is below the flight path, steer towards the waypoint and move.
     */
    private void fly_without_rider() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 10;
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
            // :685 (int) casts -> Mth.floor (DECISIONS R20), here and below.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(posY), Mth.floor(this.getZ()));
        }
        if (this.isVehicle()) {
            return;
        }
        if (this.isTame() && this.getOwner() != null) {
            e = this.getOwner();
            has_owner = 1;
            ox = e.getX();
            oy = e.getY();
            oz = e.getZ();
            if (this.distanceToSqr(e) > 400.0) {
                toofar = 1;
                this.target_in_sight = false;
                this.setAttacking(0);
                this.setSitting(false);
                this.flyaway = 0;
                do_new = 1;
            }
        }
        if (this.isSitting()) {
            return;
        }
        // :708-716: the second branch catches every case the first one leaves, the third is unreachable.
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
        if (this.flyaway > 0) {
            --this.flyaway;
        }
        if (toofar == 0 && this.flyaway == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(7) == 1) {
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                if (this.isTame() && VirtualHealth.originalHealth(this) / this.mygetMaxHealth() < 0.25f) {
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
                    if (this.distanceToSqr(e) < (8.0f + e.getBbWidth() / 2.0f) * (8.0f + e.getBbWidth() / 2.0f)) {
                        this.doHurtTarget(e);
                        this.flyaway = 5 + this.level().random.nextInt(15);
                        do_new = 1;
                    } else if (this.distanceToSqr(e) < 400.0 && !this.isInWater() && this.getThePrinceTeenFire() != 0
                            && this.level().random.nextInt(2) == 1) {
                        this.shoot_something(e.getX(), e.getY(), e.getZ());
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
                if (has_owner == 1) {
                    gox = Mth.floor(ox);
                    goy = Mth.floor(oy);
                    goz = Mth.floor(oz);
                    if (this.owner_flying == 0) {
                        zdir = this.level().random.nextInt(14) + 5;
                        xdir = this.level().random.nextInt(14) + 5;
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
                if (this.distanceToSqr(e) > 64.0) {
                    speed_factor = 3.5;
                }
            }
        }
        motionX += (Math.signum(var1) - motionX) * 0.15 * speed_factor;
        motionY += (Math.signum(var2) - motionY) * 0.21 * speed_factor;
        motionZ += (Math.signum(var3) - motionZ) * 0.15 * speed_factor;
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.setZza((float) (0.75 * speed_factor));
        this.setYRot(this.getYRot() + var5 / 4.0f);
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
     * {@code onLivingUpdate} (:837-1127). On the ground the vanilla update runs first; in flight it does not (goals,
     * gravity and vanilla movement stay off). The client then interpolates a flying prince over the packet's step count;
     * the server steers a ridden one or flies alone, and always ends with {@link #always_do()}.
     */
    @Override
    public void aiStep() {
        // :840-841: never read, but each draws from the entity random every tick, on both sides.
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
                    this.fly_without_rider();
                }
            }
            this.always_do();
        }
    }

    /** The server branch of {@code onLivingUpdate} with a rider (:881-1120). */
    private void flyWithRider(final Entity rider) {
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.95;
        double gh = 1.0;
        double pi = 3.1415926545;
        double deltav = 0.0;
        int dist = 2;
        // PORT: (EntityPlayer) this.riddenByEntity (:882) threw for any other rider; 1.21.1's /ride can seat a mob. A
        // non-player rider reads as no forward and no strafe input (R18 case 1, W09 Dragon).
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
        // :897 (int) posX, (int)((float) posY - (float) gh), (int) posZ -> Mth.floor (DECISIONS R20), here and below.
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
        // :945-948: relative_g is recomputed and clamped but never read again.
        relative_g = Math.abs(relative_g) * velocity;
        if (relative_g > 50.0) {
            relative_g = 0.0;
        }
        this.setXRot(2.0f * (float) velocity);
        this.setRot(this.getYRot(), this.getXRot());
        double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        // rr = atan2(rider.motionZ, rider.motionX) (:952) and rt = 0 (:955) are never read.
        final double rhm = Math.atan2(motionZ, motionX);
        final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        pi = 3.1415926545;
        deltav = 0.0;
        final float im = pp != null ? pp.zza : 0.0f; // moveForward
        // OreSpawnMain.flyup_keystate != 0 (:959): per rider (R15).
        if (RiderKeys.isFlyUp(rider)) {
            motionY += 0.035;
            motionY += velocity * 0.046;
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
        if (this.fireballticker == 0 && pp != null && (pp.xxa < -0.001f || pp.xxa > 0.001f)) {
            double yoff = 1.5;
            final double xzoff = 7.5;
            ++this.which_attack;
            if (this.which_attack > 2) {
                this.which_attack = 0;
            }
            if (this.which_attack == 0) {
                yoff += this.getHead1Ext() * 0.04f;
                double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() - 10.0f));
                double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() - 10.0f));
                final BetterFireball bf = new BetterFireball(this.level(), this, 0.0, 0.0, 0.0);
                bf.setNotMe();
                bf.setPos(cx, posY + yoff, cz);
                cx = Math.cos(Math.toRadians(pp.getYHeadRot() + 90.0f));
                cz = Math.sin(Math.toRadians(pp.getYHeadRot() + 90.0f));
                final double cy = -Math.sin(Math.toRadians(pp.getXRot()));
                final double d12 = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
                bf.setAcceleration(cx / d12 * 0.1, cy / d12 * 0.1, cz / d12 * 0.1);
                bf.setDeltaMovement(motionX, motionY, motionZ);
                bf.setPos(bf.getX() - motionX * 3.0, bf.getY() - motionY * 3.0, bf.getZ() - motionZ * 3.0);
                PrinceSupport.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(bf);
            }
            if (this.which_attack == 1) {
                yoff += this.getHead3Ext() * 0.04f;
                final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() + 10.0f));
                final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() + 10.0f));
                final IceBall var2 = new IceBall(this.level(), cx, posY + yoff, cz);
                var2.moveTo(cx, posY + yoff, cz, pp.getYRot() + 90.0f, pp.getXRot());
                var2.setIceMaker(1);
                final double var3 = Math.cos(Math.toRadians(pp.getYRot() + 90.0f));
                final double var4 = -Math.sin(Math.toRadians(pp.getXRot()));
                final double var5 = Math.sin(Math.toRadians(pp.getYRot() + 90.0f));
                final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                var2.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 5.0f);
                var2.setPos(var2.getX() - motionX * 3.0, var2.getY() - motionY * 3.0, var2.getZ() - motionZ * 3.0);
                final Vec3 im2 = var2.getDeltaMovement();
                var2.setDeltaMovement(im2.x * 2.0, im2.y * 2.0, im2.z * 2.0);
                PrinceSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(var2);
            }
            if (this.which_attack == 2) {
                yoff += this.getHead2Ext() * 0.04f;
                final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
                final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
                // The thrower constructor: the rider is the thrower (verhalten/entity-02.md).
                final ThunderBolt lb = new ThunderBolt(this.level(), pp);
                lb.moveTo(cx, posY + yoff, cz, pp.getYRot() + 90.0f, pp.getXRot());
                final Vec3 lm = lb.getDeltaMovement();
                lb.setDeltaMovement(lm.x * 3.0, lm.y * 3.0, lm.z * 3.0);
                PrinceSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(lb);
            }
            this.fireballticker = 10;
        }
        this.legacyMoveEntity(motionX, motionY, motionZ);
        final Vec3 moved = this.getDeltaMovement();
        this.setDeltaMovement(moved.x * 0.985, moved.y * 0.94, moved.z * 0.985);
        if (!this.level().isClientSide) {
            final List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(3.25, 4.0, 3.25));
            if (list != null && !list.isEmpty()) {
                for (int l = 0; l < list.size(); ++l) {
                    final Entity listEntity = list.get(l);
                    // canBePushed -> isPushable (1.7.10 EntityLivingBase: !isDead).
                    if (listEntity != rider && !listEntity.isRemoved() && listEntity.isPushable()) {
                        applyEntityCollision(listEntity, this);
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
     * {@code self.applyEntityCollision(other)}, 1.7.10 {@code Entity.applyEntityCollision}: unless one rides the other,
     * both are pushed apart by 0.05 along the axis of the larger offset, {@code self} away from {@code other}.
     *
     * <p>PORT: 1.21.1's {@code Entity.push(Entity)} skips a vehicle or an unpushable entity; the ridden prince is both, but
     * 1.7.10 pushed it. Same as W09 {@code DragonSupport.applyEntityCollision} (package-private there).
     */
    private static void applyEntityCollision(final Entity self, final Entity other) {
        if (other.getFirstPassenger() != self && other.getVehicle() != self) {
            double d0 = other.getX() - self.getX();
            double d1 = other.getZ() - self.getZ();
            double d2 = Math.max(Math.abs(d0), Math.abs(d1));
            if (d2 >= 0.009999999776482582) {
                d2 = (double) (float) Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0 / d2;
                if (d3 > 1.0) {
                    d3 = 1.0;
                }
                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05000000074505806;
                d1 *= 0.05000000074505806;
                self.push(-d0, 0.0, -d1);
                other.push(d0, 0.0, d1);
            }
        }
    }

    /**
     * {@code updateRiderPosition} (:1129-1134): 0.65 along the yaw, {@code posY + 2.75 + rider.getYOffset()}. A
     * server-side 1.7.10 player had {@code getYOffset() = -0.5}; 1.21.1 positions passengers by their feet (W09 Dragon).
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
     * {@code playTameEffect} (:1136-1147): 20 hearts or smoke puffs in a 5-block cloud; on the server (where 1.7.10's
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
     * {@code interact} (:1149-1303), both sides like the original. A {@code true} answer is {@code sidedSuccess}. The
     * click order around it (name tag, a stranger's lead) is restored by {@link PrinceInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return this.legacyInteract(par1EntityPlayer) ? InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
    }

    /** The body of {@code interact(EntityPlayer)} (:1149-1303). {@code func_152114_e} is "is owner". */
    boolean legacyInteract(final Player par1EntityPlayer) {
        final boolean isRemote = this.level().isClientSide;
        // :1150-1154 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (!var2.isEmpty() && var2.is(Items.DIAMOND_BLOCK) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
            if (!isRemote) {
                // PORT: setTamed + owner without TamableAnimal.tame's advancement trigger, as in W09 Dragon.
                this.setTame(true, false);
                this.setOwnerUUID(par1EntityPlayer.getUUID());
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.mygetMaxHealth() - VirtualHealth.originalHealth(this));
                this.kill_count = 1000;
                this.day_count = 1000;
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame()) {
            if (!this.isOwnedBy(par1EntityPlayer)) {
                return false;
            }
            if (var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 25.0) {
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
                if (this.mygetMaxHealth() > VirtualHealth.originalHealth(this)) {
                    this.heal(this.mygetMaxHealth() - VirtualHealth.originalHealth(this));
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            // ItemFood is an item with a food component; func_150905_g (getHealAmount) is nutrition (W04 Girlfriend).
            final FoodProperties food = var2.isEmpty() ? null : var2.getFoodProperties(this);
            if (!var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 25.0 && food != null) {
                if (!isRemote) {
                    final FoodProperties var3 = food;
                    if (this.mygetMaxHealth() > VirtualHealth.originalHealth(this)) {
                        this.heal((float) (var3.nutrition() * 10));
                    }
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.ICE) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    this.setThePrinceTeenFire(0);
                    par1EntityPlayer.sendSystemMessage(Component.literal("Fireballs extinguished."));
                }
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.FLINT_AND_STEEL) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    this.setThePrinceTeenFire(1);
                    par1EntityPlayer.sendSystemMessage(Component.literal("Fireballs lit!"));
                }
                // --stackSize on a damageable item: the whole flint and steel goes (R18).
                HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
                return true;
            }
            if (!var2.isEmpty() && var2.is(Items.DIAMOND) && par1EntityPlayer.distanceToSqr(this) < 25.0 && !isRemote) {
                ThePrince d = null;
                d = PrinceSupport.spawnCreature(this.level(), ModEntities.THE_PRINCE.get(), this.getX(), this.getY(), this.getZ());
                if (d != null) {
                    if (this.isTame()) {
                        d.setTame(true, false);
                        d.setOwnerUUID(par1EntityPlayer.getUUID());
                        d.set_ok_to_grow();
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
            if (!var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 16.0) {
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

    /** {@code isWheat} (:1305-1307): overrides nothing in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.BEEF);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}, not overridden: wheat. Nothing reads it, the prince never breeds. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(Items.WHEAT);
    }

    /** {@code getAttacking} (:1309-1311). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:1313-1318): server only. */
    public void setAttacking(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getActivity} (:1320-1322). */
    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    /** {@code setActivity} (:1324-1329): server only. */
    public void setActivity(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code getThePrinceTeenFire} (:1331-1333). */
    public int getThePrinceTeenFire() {
        return this.entityData.get(DATA_FIRE);
    }

    /** {@code setThePrinceTeenFire} (:1335-1340): server only. */
    public void setThePrinceTeenFire(final int par1) {
        if (this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_FIRE, par1);
    }

    /** {@code createChild} (:1353-1355). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** {@code canDespawn} (:1357-1359): only without a rider, not persistent and wild. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.isVehicle() && !this.isTame();
    }

    /** {@code writeEntityToNBT} (:1361-1368). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("ThePrinceTeenAttacking", this.getAttacking());
        par1NBTTagCompound.putInt("ThePrinceTeenActivity", this.getActivity());
        par1NBTTagCompound.putInt("ThePrinceTeenFire", this.getThePrinceTeenFire());
        par1NBTTagCompound.putInt("SpyroKill", this.kill_count);
        par1NBTTagCompound.putInt("SpyroDay", this.day_count);
    }

    /** {@code readEntityFromNBT} (:1370-1377). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setAttacking(par1NBTTagCompound.getInt("ThePrinceTeenAttacking"));
        this.setActivity(par1NBTTagCompound.getInt("ThePrinceTeenActivity"));
        this.setThePrinceTeenFire(par1NBTTagCompound.getInt("ThePrinceTeenFire"));
        this.kill_count = par1NBTTagCompound.getInt("SpyroKill");
        this.day_count = par1NBTTagCompound.getInt("SpyroDay");
    }

    /**
     * {@code shoot_something} (:1379-1421): {@code rand(3)} picks fireball, thunderbolt or ice ball; each only fires when
     * the target lies within 0.5 rad of the heading.
     */
    private void shoot_something(final double x, final double y, final double z) {
        double rr = 0.0;
        double rhdir = 0.0;
        double rdd = 0.0;
        final double pi = 3.1415926545;
        final int which = this.level().random.nextInt(3);
        if (which == 0) {
            rr = Math.atan2(z - this.getZ(), x - this.getX());
            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
            if (rdd > pi) {
                rdd -= pi * 2.0;
            }
            rdd = Math.abs(rdd);
            if (rdd < 0.5) {
                this.firecanon(x, y, z);
            }
        } else if (which == 1) {
            rr = Math.atan2(z - this.getZ(), x - this.getX());
            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
            if (rdd > pi) {
                rdd -= pi * 2.0;
            }
            rdd = Math.abs(rdd);
            if (rdd < 0.5) {
                this.firecanonl(x, y, z);
            }
        } else {
            rr = Math.atan2(z - this.getZ(), x - this.getX());
            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
            if (rdd > pi) {
                rdd -= pi * 2.0;
            }
            rdd = Math.abs(rdd);
            if (rdd < 0.5) {
                this.firecanoni(x, y, z);
            }
        }
    }

    /** {@code firecanon} (:1423-1438): a big fireball 6 ahead and 3.5 up, spread ±5/±3/±5. */
    private void firecanon(final double x, final double y, final double z) {
        final double yoff = 3.5;
        final double xzoff = 6.0;
        BetterFireball bf = null;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        final float r1 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        final float r2 = 3.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        final float r3 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        bf = new BetterFireball(this.level(), this, x - cx + r1, y + 0.25 - (this.getY() + yoff) + r2, z - cz + r3);
        bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
        bf.setPos(cx, this.getY() + yoff, cz);
        bf.setBig();
        PrinceSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(bf);
    }

    /** {@code firecanonl} (:1440-1467): a thunderbolt without thrower, heading 1.4/4.0, motion ×3. The spread rolls are unused. */
    private void firecanonl(final double x, final double y, final double z) {
        final double yoff = 3.5;
        final double xzoff = 6.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        PrinceSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        // r1..r3 (:1450-1452): drawn, never read.
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        final ThunderBolt lb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
        lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
        var3 = x - lb.getX();
        var4 = y + 0.25 - lb.getY();
        var5 = z - lb.getZ();
        var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
        lb.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 4.0f);
        final Vec3 lm = lb.getDeltaMovement();
        lb.setDeltaMovement(lm.x * 3.0, lm.y * 3.0, lm.z * 3.0);
        this.level().addFreshEntity(lb);
    }

    /** {@code firecanoni} (:1469-1497): an ice ball that makes ice, heading 1.4/4.0, motion ×3. The spread rolls are unused. */
    private void firecanoni(final double x, final double y, final double z) {
        final double yoff = 3.5;
        final double xzoff = 6.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        PrinceSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        // r1..r3 (:1479-1481): drawn, never read.
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        this.level().random.nextFloat();
        final IceBall lb = new IceBall(this.level(), cx, this.getY() + yoff, cz);
        lb.setIceMaker(1);
        lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
        var3 = x - lb.getX();
        var4 = y + 0.25 - lb.getY();
        var5 = z - lb.getZ();
        var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
        lb.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 4.0f);
        final Vec3 lm = lb.getDeltaMovement();
        lb.setDeltaMovement(lm.x * 3.0, lm.y * 3.0, lm.z * 3.0);
        this.level().addFreshEntity(lb);
    }

    /** 1.7.10 {@code EntityTameable.setSitting}: one synced flag; 1.21.1 splits it into order and pose. */
    public void setSitting(final boolean sitting) {
        HerbivoreSupport.setSitting(this, sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /** {@code onDeath}: not overridden; R22, as {@link ThePrince#die}. */
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
     * vanilla 1.7.10 goal ({@link EntityAINearestAttackableTarget}, W05) with chance 0, sight required, and the selector
     * {@code entity instanceof IMob} ({@link Enemy}). {@code EntityLiving} is {@link Mob}. Same as W09 Dragon.
     */
    private static final class NearestMobTarget extends EntityAINearestAttackableTarget<Mob> {
        NearestMobTarget(final ThePrinceTeen prince) {
            super(prince, Mob.class, 0, true);
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
