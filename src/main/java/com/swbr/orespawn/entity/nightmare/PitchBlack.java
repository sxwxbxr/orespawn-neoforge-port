package com.swbr.orespawn.entity.nightmare;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.ender.EnderReaper;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.monster.LeafMonster;
import com.swbr.orespawn.entity.triffid.Triffid;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.world.dimension.chaos.WorldProviderOreSpawn6;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
 * Port of {@code danger.orespawn.PitchBlack} ("Nightmare", PitchBlack.java:19-576, verhalten/entity-11.md): the flying,
 * size-rolled night boss. Registered as "Nightmare" 64/1/false (OreSpawnMain.java:3687), {@code EntityMob}, spawned from
 * {@code spawnableMonsterList} in Islands and Chaos.
 *
 * <p><b>Scale.</b> {@code entityInit} rolls 0.5 / 1 / 2 / 3 / 4 (or takes {@code NightmareSize}) into DataWatcher 22 as
 * {@code int(scale * 10.0001)} (:73-117, :152-162). Health {@code Nightmare_health * scale}, attack
 * {@code Nightmare_attack * scale}, speed {@code 0.2 + 0.1 * scale} (every tick), armor
 * {@code Nightmare_defense + (int)(2 * scale)}, hitbox {@code 2.5 * scale x 3.5 * scale} (every tick, :222).
 *
 * <p><b>Virtual health (R4).</b> At the default of 250 the largest size has 1000 health, but the config clamp allows
 * {@code Nightmare_health} up to 500, and 500 x 4 = 2000 lies above the 1024 clamp. The class is therefore a
 * {@link VirtualHealth}: the attribute takes {@code min(original, 1024)}, the original maximum of the roll is kept in
 * {@link #originalMaxHealth} and saved. At every value that fits the scale is exactly 1.
 *
 * <p><b>Two modes.</b> Activity 0 walks with the vanilla task list; activity 1 flies with the hand-written steering of
 * {@code updateAITasks} (:303-377), and then <em>nothing</em> of {@code EntityLiving.updateAITasks} runs - no sensing
 * cache clear, no tasks, no navigator, no move/look/jump helper, no despawn check. {@code Mob.serverAiStep} is final
 * in 1.21.1, so each of these is gated on its own: {@link GroundTask} wraps every goal, {@link #getSensing()} is a cache
 * cleared only on the ground, the three controls skip their tick in the air, and {@link #checkDespawn()} does not run
 * the distance rules in the air.
 *
 * <p>Original faults kept (R18): a fresh Nightmare gives 200 XP and has 25 fire-immune ticks, because the constructor
 * body overwrote what {@code entityInit} had set (:38-40 after :118-119); only a loaded one uses {@code 100 * scale} and
 * {@code 25 * scale} (:127-128). The spawner branch caps the scale at 1 after health and attack were already set from
 * the rolled scale (:420-424). The sound pitch goes negative for small sizes (:192); both sound engines clamp it.
 */
public class PitchBlack extends Monster implements LegacyArmor, VirtualHealth {

    /** DataWatcher 20: {@code attacking}, jaw and claw animation. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);
    /** DataWatcher 21: {@code activity}, 0 on the ground, 1 in flight. */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);
    /** DataWatcher 22: the scale times ten, as an int. */
    private static final EntityDataAccessor<Integer> DATA_SCALE = SynchedEntityData.defineId(PitchBlack.class, EntityDataSerializers.INT);

    /** PORT: NBT key of the original maximum health for R4; the 1.7.10 class had no such key (see the class comment). */
    private static final String ORIGINAL_MAX_HEALTH_KEY = "OreSpawnPortOriginalMaxHealth";

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    private float MyMoveSpeed;
    private int damage_ticker;
    private int wing_sound;
    /** {@code Entity.fireResistance}: 25 from the constructor, {@code 25 * scale} after loading (:40, :128). */
    private int fireResistance;
    /** R4: {@code mygetMaxHealth()} at the moment the attribute was written (constructor) or as loaded. */
    private double originalMaxHealth;
    /** {@code EntityLiving.senses}: see {@link #getSensing()}. */
    private final Sensing legacySenses;

    /** {@code PitchBlack(World)} (:28-47) with {@code applyEntityAttributes} (:49-55). */
    public PitchBlack(final EntityType<? extends PitchBlack> type, final Level par1World) {
        super(type, par1World);
        // entityInit (:73-117) ran inside the Entity constructor, before everything below.
        this.rollScale();
        this.legacySenses = new Sensing(this);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        // this.renderdata = new RenderInfo() (:32): client scratch space, kept by PitchBlackModel per entity.
        this.MyMoveSpeed = 0.2f;
        this.damage_ticker = 0;
        this.wing_sound = 0;
        // setSize(2.0f, 3.0f) (:36) is the entity type's size (R9); tick() applies the scaled size from the first tick on.
        // getNavigator().setAvoidsWater(false) (:37): a water path malus of 0 (W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        // experienceValue = 200 (:38) overwrites the 100 * scale of entityInit (R18: a fresh Nightmare gives 200 XP).
        this.xpReward = 200;
        // isImmuneToFire = false (:39) is the type default; fireResistance = 25 (:40) overwrites 25 * scale as well.
        this.fireResistance = 25;
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent). Each is wrapped in
        // GroundTask, because updateAITasks ran the task list only while activity was 0 (:310-313).
        this.goalSelector.addGoal(0, new GroundTask(this, new FloatGoal(this)));
        // PORT: EntityAIMoveThroughVillage(1.0, false) - no 1.7.10 port in entity.ai yet; the 1.21.1 POI goal with
        // distance 4 and no doors, as MonsterSupport.legacyMoveThroughVillage builds it (W07 open point 2).
        this.goalSelector.addGoal(1, new GroundTask(this, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false)));
        this.goalSelector.addGoal(2, new GroundTask(this, new MyEntityAIWanderALot(this, 16, 1.0)));
        this.goalSelector.addGoal(3, new GroundTask(this, new EntityAIWatchClosest(this, Player.class, 10.0f)));
        this.goalSelector.addGoal(4, new GroundTask(this, new EntityAILookIdle(this)));
        this.moveControl = new GroundMoveControl(this);
        this.lookControl = new GroundLookControl(this);
        this.jumpControl = new GroundJumpControl(this);
        // applyEntityAttributes (:49-55) ran inside the EntityLivingBase constructor, after entityInit had rolled the
        // scale, followed by setHealth(getMaxHealth()). The attribute supplier is built before the config loads (R3), so
        // the runtime values go in here (Girlfriend/GiantRobot precedent). MAX_HEALTH takes the R4 clamp.
        this.originalMaxHealth = this.mygetMaxHealth();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.originalMaxHealth));
        this.MyMoveSpeed = 0.2f;
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) (this.MyMoveSpeed + 0.1f * this.getPitchBlackScale()));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) (this.getPitchBlackScale() * MobStats.PitchBlack_stats().attack()));
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:49-55) with the config defaults at scale 1 (manifest 250 / 0.3 / 30); see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.MOVEMENT_SPEED, (double) (0.2f + 0.1f))
                .add(Attributes.ATTACK_DAMAGE, 30.0);
    }

    /**
     * {@code entityInit} (:57-121), watcher part: the three watchers, all defined as 0. The scale roll of the same method
     * is {@link #rollScale()}, called first thing in the constructor.
     *
     * <p>PORT: the roll cannot go into the definition. {@code ServerEntity} sends only values that differ from the
     * defined one when a client starts tracking the entity ({@code getNonDefaultValues}); a rolled value defined as the
     * default would never reach the client, which rolls its own. 1.7.10 sent every watched object in the spawn packet.
     */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_SCALE, 0);
    }

    /**
     * The scale roll of {@code entityInit} (:73-117): 0.5, then 1 with 1/4, 2 with 1/8, 3 with 1/32, 4 with 1/64 on the
     * world random, later hits overwriting earlier ones; {@code NightmareSize} 1..5 forces 0.5/1/2/3/4. The
     * {@code OreSpawnRand} branch is the original's fallback for a missing world. {@code experienceValue},
     * {@code fireResistance} and {@code setSize} of the roll are overwritten by the constructor, see there;
     * {@code RenderInfo} zeroing lives in the model.
     */
    private void rollScale() {
        float t = 0.5f;
        if (this.level() != null) {
            if (this.level().random.nextInt(4) == 1) {
                t = 1.0f;
            }
            if (this.level().random.nextInt(8) == 2) {
                t = 2.0f;
            }
            if (this.level().random.nextInt(32) == 3) {
                t = 3.0f;
            }
            if (this.level().random.nextInt(64) == 4) {
                t = 4.0f;
            }
        } else {
            if (OreSpawn.OreSpawnRand.nextInt(4) == 1) {
                t = 1.0f;
            }
            if (OreSpawn.OreSpawnRand.nextInt(8) == 2) {
                t = 2.0f;
            }
            if (OreSpawn.OreSpawnRand.nextInt(32) == 3) {
                t = 3.0f;
            }
            if (OreSpawn.OreSpawnRand.nextInt(64) == 4) {
                t = 4.0f;
            }
        }
        final int size = TweakStats.NightmareSize();
        if (size == 1) {
            t = 0.5f;
        }
        if (size == 2) {
            t = 1.0f;
        }
        if (size == 3) {
            t = 2.0f;
        }
        if (size == 4) {
            t = 3.0f;
        }
        if (size == 5) {
            t = 4.0f;
        }
        this.setPitchBlackScale(t);
    }

    /** {@code readEntityFromNBT} (:123-129). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setPitchBlackScale(par1NBTTagCompound.getFloat("Fscale"));
        this.refreshDimensions();
        this.xpReward = (int) (100.0f * this.getPitchBlackScale());
        this.fireResistance = (int) (25.0f * this.getPitchBlackScale());
        // PORT (R4): the saved attribute base already holds min(original, 1024); the original maximum comes back from its
        // own key. A save without it (written before this key existed) falls back to the current scale.
        if (par1NBTTagCompound.contains(ORIGINAL_MAX_HEALTH_KEY)) {
            this.originalMaxHealth = par1NBTTagCompound.getDouble(ORIGINAL_MAX_HEALTH_KEY);
        } else {
            this.originalMaxHealth = this.mygetMaxHealth();
        }
    }

    /** {@code writeEntityToNBT} (:131-134). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putFloat("Fscale", this.getPitchBlackScale());
        par1NBTTagCompound.putDouble(ORIGINAL_MAX_HEALTH_KEY, this.originalMaxHealth);
    }

    /** {@code getAttacking} (:136-138). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:140-142). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getActivity} (:144-146). */
    public final int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    /** {@code setActivity} (:148-150). */
    public final void setActivity(final int par1) {
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code getPitchBlackScale} (:152-156). */
    public float getPitchBlackScale() {
        final int i = this.entityData.get(DATA_SCALE);
        final float f = (float) i;
        return f / 10.0f;
    }

    /** {@code setPitchBlackScale} (:158-162). */
    public void setPitchBlackScale(final float par1) {
        final float f = par1 * 10.0001f;
        final int i = (int) f;
        this.entityData.set(DATA_SCALE, i);
    }

    /** {@code getTotalArmorValue} (:164-166). */
    public int getTotalArmorValue() {
        return MobStats.PitchBlack_stats().defense() + (int) (2.0f * this.getPitchBlackScale());
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code fireResistance} (:40, :128). */
    @Override
    protected int getFireImmuneTicks() {
        return this.fireResistance;
    }

    /**
     * The hitbox of {@code setSize(2.5f * scale, 3.5f * scale)} (:120, :126, :222), R9.
     *
     * <p>PORT: 1.7.10 {@code setSize} grew the box from its minimum corner and pushed a grown entity back by the
     * difference; {@code refreshDimensions} keeps the box centred and moves the entity to a free spot instead.
     */
    @Override
    protected EntityDimensions getDefaultDimensions(final Pose pose) {
        return EntityDimensions.scalable(2.5f * this.getPitchBlackScale(), 3.5f * this.getPitchBlackScale());
    }

    /**
     * {@code canDespawn} (:183-185): only by day, and not when persistent - {@code isPersistenceRequired} is checked by
     * {@link net.minecraft.world.entity.Mob#checkDespawn} before this is asked. {@code isDaytime} is
     * {@link InsectSupport#isDaytime}.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return InsectSupport.isDaytime(this.level());
    }

    /**
     * {@code despawnEntity} ran inside {@code EntityLiving.updateAITasks}, which the flying Nightmare skips (:310-313).
     * Peaceful removal was {@code EntityMob.onUpdate} and happened in either mode.
     *
     * <p>PORT (R18 case 3): 1.21.1 runs the despawn check from the level for every mob on every tick; in flight only the
     * peaceful branch is kept.
     */
    @Override
    public void checkDespawn() {
        if (this.getActivity() != 0) {
            if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
                this.discard();
            }
            return;
        }
        super.checkDespawn();
    }

    /** {@code getSoundVolume} (:187-189). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:191-193), negative for scales below 2.8: both sound engines clamp to 0.5..2.0. */
    @Override
    public float getVoicePitch() {
        return 1.0f - 0.7f * (4.0f / this.getPitchBlackScale());
    }

    /** {@code getLivingSound} (:195-200): one call in five on the world random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(5) != 2) {
            return null;
        }
        return ModSounds.PITCHBLACK_LIVING.get();
    }

    /** {@code getHurtSound} (:202-204). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.PITCHBLACK_HIT.get();
    }

    /** {@code getDeathSound} (:206-208). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PITCHBLACK_DEAD.get();
    }

    /** {@code mygetMaxHealth} (:210-212), in original units. */
    public int mygetMaxHealth() {
        return (int) (MobStats.PitchBlack_stats().health() * this.getPitchBlackScale());
    }

    /**
     * R4: the original maximum of this Nightmare's roll.
     *
     * <p>PORT: the client never receives the saved value; it uses the synchronised scale, which differs only after the
     * spawner cap.
     */
    @Override
    public double getOriginalMaxHealth() {
        if (this.level().isClientSide) {
            return this.mygetMaxHealth();
        }
        return this.originalMaxHealth;
    }

    /**
     * The sensing cache of 1.7.10 {@code EntityLiving}, cleared at the start of {@code updateAITasks} - which the flying
     * Nightmare never reaches. {@code isSuitableTarget} and the watch goal read it.
     *
     * <p>PORT: {@code Mob.sensing} is private and cleared by the final {@code serverAiStep} on every tick; this cache is
     * cleared in {@link #aiStep()} under the original condition instead.
     */
    @Override
    public Sensing getSensing() {
        return this.legacySenses;
    }

    /**
     * {@code EntityLiving.updateAITasks} cleared the senses first, and only for an entity whose AI step runs
     * ({@code isClientWorld}, not dead) and - here - on the ground. Nothing between this point and the goal selector of
     * the same tick can change the activity.
     */
    @Override
    public void aiStep() {
        if (!this.isImmobile() && this.isEffectiveAi() && this.getActivity() == 0) {
            this.legacySenses.tick();
        }
        super.aiStep();
    }

    /**
     * {@code onUpdate} (:218-267): speed, the vanilla tick, the scaled size, the wing beat every 21 ticks, the vertical
     * damping, the take-off/landing roll and the target check on the ground. Everything but the sound and the 1/250 roll
     * runs on both sides, as in the original. {@code (int)} casts of coordinates are {@code Mth.floor} (R20).
     */
    @Override
    public void tick() {
        this.MyMoveSpeed = 0.2f;
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) (this.MyMoveSpeed + 0.1f * this.getPitchBlackScale()));
        super.tick();
        // setSize (:222) changed nothing while the size was unchanged; refreshDimensions posts an event, so ask first.
        if (this.getBbWidth() != 2.5f * this.getPitchBlackScale() || this.getBbHeight() != 3.5f * this.getPitchBlackScale()) {
            this.refreshDimensions();
        }
        ++this.wing_sound;
        if (this.wing_sound > 20) {
            if (!this.level().isClientSide) {
                this.playSoundAtEntity(ModSounds.MOTHRA_WINGS.get(), 1.0f, 1.0f);
            }
            this.wing_sound = 0;
        }
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        if (!this.level().isClientSide && this.level().random.nextInt(250) == 1) {
            this.heal(1.0f + this.getPitchBlackScale());
            if (this.level().random.nextInt(5) == 0) {
                boolean bidIsAir = true;
                if (this.getY() > 10.0) {
                    for (int i = 0; i < 10; ++i) {
                        final BlockState bid = this.level().getBlockState(
                                new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) - i, Mth.floor(this.getZ())));
                        bidIsAir = bid.isAir();
                        if (!bidIsAir) {
                            break;
                        }
                    }
                } else {
                    bidIsAir = false; // Blocks.stone
                }
                if (!bidIsAir) {
                    Entity e = null;
                    e = this.findSomethingToAttack();
                    if (e == null) {
                        this.setActivity(0);
                    }
                }
            } else {
                this.setActivity(1);
                this.getNavigation().stop();
            }
        }
        if (this.getActivity() == 0 && this.level().random.nextInt(10) == 1) {
            Entity e2 = null;
            e2 = this.findSomethingToAttack();
            if (e2 != null) {
                this.setActivity(1);
                this.getNavigation().stop();
            }
        }
    }

    /**
     * {@code attackEntityAsMob} (:269-297): against the Ender Dragon an explosion source on the head (1/8) or the body,
     * always reported as a hit; against everything else mob damage {@code Nightmare_attack * scale}, and on a hit a
     * living target is flung {@code 1.15 * scale} away and {@code 0.08 * scale} up (doubled for players and removed
     * entities).
     *
     * <p>PORT: {@code attackEntityFromPart(dragonPartBody, ...)} - the body part is private in 1.21.1 and reached through
     * {@code getSubEntities()}, where it is the third part as in 1.7.10. {@code addVelocity} is {@code push} with
     * {@code hurtMarked}, so a server player receives the impulse (MonsterSupport precedent).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        boolean var4 = false;
        if (par1Entity != null && par1Entity instanceof EnderDragon dr) {
            DamageSource var5 = null;
            var5 = this.damageSources().explosion(null, null);
            if (this.level().random.nextInt(8) == 1) {
                dr.hurt(dr.head, var5, MobStats.PitchBlack_stats().attack() * this.getPitchBlackScale());
            } else {
                dr.hurt(dragonBody(dr), var5, MobStats.PitchBlack_stats().attack() * this.getPitchBlackScale());
            }
            var4 = true;
        } else {
            var4 = par1Entity.hurt(this.damageSources().mobAttack(this), MobStats.PitchBlack_stats().attack() * this.getPitchBlackScale());
            if (var4 && par1Entity != null && par1Entity instanceof LivingEntity) {
                final double ks = 1.15 * this.getPitchBlackScale();
                double inair = 0.08 * this.getPitchBlackScale();
                final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
                if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                    inair *= 2.0;
                }
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
                par1Entity.hurtMarked = true;
            }
        }
        return var4;
    }

    /** {@code dragonPartBody}: the part named "body" (index 2 of {@code getSubEntities()}). */
    private static EnderDragonPart dragonBody(final EnderDragon dr) {
        for (final EnderDragonPart part : dr.getSubEntities()) {
            if ("body".equals(part.name)) {
                return part;
            }
        }
        return dr.getSubEntities()[2];
    }

    /**
     * {@code canSeeTarget} (:299-301): no block between a point 0.75 above the feet and the target. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded - the outline shape (Bee
     * precedent).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:303-377). On the ground the task list, navigator and helpers of {@code super} already ran
     * in {@code serverAiStep} before this call; in flight the waypoint, attack and steering below.
     *
     * <p>PORT: in 1.7.10 the move, look and jump helpers ran inside {@code super.updateAITasks()}, before the
     * {@code return}; in 1.21.1 they run after this method (EntityCannonFodder precedent). {@code this.rand} is the
     * entity random, {@code moveForward} is {@code zza}, {@code isDead} is the removed flag.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        if (this.damage_ticker > 0) {
            --this.damage_ticker;
        }
        if (this.getActivity() == 0) {
            super.customServerAiStep();
            return;
        }
        if (this.isRemoved()) {
            return;
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.getActivity() == 0) {
            return;
        }
        final RandomSource rand = this.getRandom();
        if (rand.nextInt(150) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            // Block bid = Blocks.stone (:324): "not air yet".
            boolean bidIsAir = false;
            while (!bidIsAir && keep_trying > 0) {
                zdir = rand.nextInt(20) + 5 * (int) this.getPitchBlackScale();
                xdir = rand.nextInt(20) + 5 * (int) this.getPitchBlackScale();
                if (rand.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (rand.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir, Mth.floor(this.getY()) + rand.nextInt(11) - 5, Mth.floor(this.getZ()) + zdir);
                final BlockState bid = this.level().getBlockState(this.currentFlightTarget);
                // PORT: isAir() also accepts cave and void air, which 1.7.10 generated as plain air (Bee precedent).
                bidIsAir = bid.isAir();
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
                --keep_trying;
            }
        } else if (rand.nextInt(8) == 0) {
            Entity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                double d1 = 5.0 + e.getBbWidth() / 2.0f;
                d1 += this.getPitchBlackScale();
                d1 *= d1;
                this.setAttacking(1);
                if (e instanceof EnderDragon && d1 < 100.0) {
                    d1 = 100.0;
                }
                // PORT: Godzilla and GodzillaHead are W10 classes; matched by registry id until they exist.
                if (isOreSpawnType(e, "mobzilla") && d1 < 100.0) {
                    d1 = 100.0;
                }
                if (isOreSpawnType(e, "mobzilla_head") && d1 < 100.0) {
                    d1 = 100.0;
                }
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 2.0), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < d1) {
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.4 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.4 - this.getZ();
        final double myspeed = 0.5f + this.getPitchBlackScale() / 10.0f;
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * myspeed - m.x) * 0.33;
        final double motionY = m.y + (Math.signum(var2) * 0.699999988079071 - m.y) * 0.20000000149011612;
        final double motionZ = m.z + (Math.signum(var3) * myspeed - m.z) * 0.33;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.1f + (float) myspeed;
        this.setYRot(this.getYRot() + var5 / 5.0f);
    }

    // canTriggerWalking (:379-381) returns true: no NoStepTrigger (R20). doesEntityNotTriggerPressurePlate (:389-391)
    // returns false: the 1.21.1 default of isIgnoringBlockTriggers.

    /** {@code fall(float)} (:383-384) is empty: no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:386-387) is empty: the fall distance never grows. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code attackEntityFrom} (:393-407): refused for 20 ticks after every accepted call; the attacker becomes the
     * waypoint (+2 up), and the Nightmare takes off.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.damage_ticker > 0) {
            return ret;
        }
        this.damage_ticker = 20;
        ret = super.hurt(par1DamageSource, par2);
        final Entity e = par1DamageSource.getEntity();
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 2.0), Mth.floor(e.getZ()));
        }
        this.setActivity(1);
        this.getNavigation().stop();
        return ret;
    }

    /**
     * Placement predicate for {@code RegisterSpawnPlacementsEvent}: always {@code true}. Every part of
     * {@code getCanSpawnHere} (:409-462) needs the rolled scale or the entity's own random and bounding box, so the whole
     * rule is {@link #checkSpawnRules}, which natural spawning and spawners ask after creating the entity.
     */
    public static boolean checkPitchBlackSpawnRules(final EntityType<PitchBlack> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }

    /**
     * {@code getCanSpawnHere} (:409-462): next to a "Nightmare" spawner the scale is capped at 1 and the spawn allowed;
     * otherwise valid light, night, in Chaos no other Nightmare within 16, and a Nightmare above scale 1.1 needs air in
     * x/z +-1 (+-2 above scale 3.1), y +1 .. +3 (+6).
     *
     * <p>PORT: the spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9, W07 precedent).
     * {@code isValidLightLevel} used {@code this.rand}; it gets the entity random. {@code (int) posX + j} is
     * {@code Mth.floor} (R20).
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (reason == MobSpawnType.SPAWNER) {
            Float t = this.getPitchBlackScale();
            if (t > 1.0f) {
                t = 1.0f;
            }
            this.setPitchBlackScale(t);
            return true;
        }
        final BlockPos lightPos = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getBoundingBox().minY), Mth.floor(this.getZ()));
        if (!LegacyLightLevel.isValidLightLevel(level, lightPos, this.getRandom())) {
            return false;
        }
        if (InsectSupport.isDaytime(this.level())) {
            return false;
        }
        if (this.level().dimension() == WorldProviderOreSpawn6.DIMENSION) {
            final List<PitchBlack> list = level.getEntitiesOfClass(PitchBlack.class, this.getBoundingBox().inflate(16.0, 16.0, 16.0), p -> p != this);
            if (!list.isEmpty()) {
                return false;
            }
        }
        if (this.getPitchBlackScale() < 1.1f) {
            return true;
        }
        int ix = 1;
        if (this.getPitchBlackScale() > 3.1f) {
            ix = 2;
        }
        final int iy = ix * 3;
        for (int k = -ix; k <= ix; ++k) {
            for (int j = -ix; j <= ix; ++j) {
                for (int i = 1; i <= iy; ++i) {
                    final BlockState bid = level.getBlockState(
                            new BlockPos(Mth.floor(this.getX()) + j, Mth.floor(this.getY()) + i, Mth.floor(this.getZ()) + k));
                    if (!bid.isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code isSuitableTarget} (:464-518): alive, not ignorable, visible through the sensing cache; never another
     * Nightmare, Ender Reaper, Leaf Monster, one of the three Terrors, an island or a Triffid, nor a creative player.
     *
     * <p>PORT: TerribleTerror, LurkingTerror, CreepingHorror, Island and IslandToo are written in parallel in this wave;
     * they are matched by registry id ({@code DinoSupport.isOreSpawnType} precedent). None of them has a subclass in the
     * original, so the set is the same.
     */
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof PitchBlack) {
            return false;
        }
        if (par1EntityLiving instanceof EnderReaper) {
            return false;
        }
        if (par1EntityLiving instanceof LeafMonster) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "terrible_terror")) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "lurking_terror")) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "creeping_horror")) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "island")) {
            return false;
        }
        if (isOreSpawnType(par1EntityLiving, "island_too")) {
            return false;
        }
        if (par1EntityLiving instanceof Triffid) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@code findSomethingToAttack} (:520-537): {@code PlayNicely} disables it; the box grows by
     * {@code 16 + 6 * scale} horizontally and {@code 10 + 4 * scale} vertically.
     */
    @Nullable
    private Entity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final double d1 = 16.0 + this.getPitchBlackScale() * 6.0f;
        final double d2 = 10.0 + this.getPitchBlackScale() * 4.0f;
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(d1, d2, d1));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var7 : var5) {
            if (this.isSuitableTarget(var7, false)) {
                return var7;
            }
        }
        return null;
    }

    // getDropItem (:539-541, nightmare scale) is never read: dropFewItems below does not call super.

    /**
     * {@code dropItemRand} (:543-551): x and z shifted by {@code OreSpawnRand} 0..4 times the scale, one block up, spawned
     * directly (no pickup delay, no drop capture), as in the original.
     */
    private ItemStack dropItemRand(final Item index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(5) * this.getPitchBlackScale() - OreSpawn.OreSpawnRand.nextInt(5) * this.getPitchBlackScale(),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(5) * this.getPitchBlackScale() - OreSpawn.OreSpawnRand.nextInt(5) * this.getPitchBlackScale(),
                is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:553-575): rotten flesh {@code 3 + rand(2 + (int)(5 * scale))} times, each with a 1/10 chance
     * each of feather, string, flint or raw beef; one Nightmare Scale and one item frame; Zoo Keepers
     * {@code 2 + (int) scale + rand(2 + (int)(5 * scale))} times. Looting ignored.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 3 + this.level().random.nextInt(2 + (int) (5.0f * this.getPitchBlackScale())), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.ROTTEN_FLESH, 1);
            final int j = this.level().random.nextInt(10);
            if (j == 0) {
                this.dropItemRand(Items.FEATHER, 1);
            }
            if (j == 1) {
                this.dropItemRand(Items.STRING, 1);
            }
            if (j == 2) {
                this.dropItemRand(Items.FLINT, 1);
            }
            if (j == 3) {
                this.dropItemRand(Items.BEEF, 1);
            }
        }
        this.dropItemRand(ModItems.NIGHTMARE_SCALE.get(), 1);
        this.dropItemRand(Items.ITEM_FRAME, 1);
        for (int i = 2 + (int) this.getPitchBlackScale() + this.level().random.nextInt(2 + (int) (5.0f * this.getPitchBlackScale())), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(ModItems.ZOO_KEEPER.get(), 1);
        }
    }

    /**
     * {@code World.playSoundAtEntity(entity, name, volume, pitch)}: at the entity for every nearby player
     * (RobotSupport precedent).
     */
    private void playSoundAtEntity(final SoundEvent sound, final float volume, final float pitch) {
        this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), volume, pitch);
    }

    /** {@code instanceof} for a class of another porter or a later wave, by registry id. */
    private static boolean isOreSpawnType(final Entity entity, final String id) {
        final ResourceLocation key = EntityType.getKey(entity.getType());
        return key.getNamespace().equals(OreSpawn.MOD_ID) && key.getPath().equals(id);
    }

    /**
     * A task of the 1.7.10 list, which {@code updateAITasks} ticked only while {@code activity == 0} (:310-313). In the
     * air nothing of it is asked: {@code shouldExecute} is not evaluated (no random draws), a running task keeps running
     * without {@code updateTask} and without {@code continueExecuting}, and is asked again after landing - as the
     * 1.7.10 {@code EntityAITasks} would have been.
     */
    static final class GroundTask extends Goal {

        private final PitchBlack owner;
        private final Goal task;

        GroundTask(final PitchBlack owner, final Goal task) {
            this.owner = owner;
            this.task = task;
            this.setFlags(task.getFlags());
        }

        /** The wrapped goal, for tests and debugging. */
        Goal getTask() {
            return this.task;
        }

        @Override
        public boolean canUse() {
            return this.owner.getActivity() == 0 && this.task.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.owner.getActivity() != 0 || this.task.canContinueToUse();
        }

        @Override
        public boolean isInterruptable() {
            return this.task.isInterruptable();
        }

        @Override
        public void start() {
            this.task.start();
        }

        @Override
        public void stop() {
            this.task.stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return this.task.requiresUpdateEveryTick();
        }

        @Override
        public void tick() {
            if (this.owner.getActivity() == 0) {
                this.task.tick();
            }
        }

        @Override
        public String toString() {
            return this.task.toString();
        }
    }

    /** {@code EntityMoveHelper}: updated inside {@code super.updateAITasks()} only, so not in flight. */
    static final class GroundMoveControl extends MoveControl {

        private final PitchBlack owner;

        GroundMoveControl(final PitchBlack owner) {
            super(owner);
            this.owner = owner;
        }

        @Override
        public void tick() {
            if (this.owner.getActivity() == 0) {
                super.tick();
            }
        }
    }

    /** {@code EntityLookHelper}: see {@link GroundMoveControl}. */
    static final class GroundLookControl extends LookControl {

        private final PitchBlack owner;

        GroundLookControl(final PitchBlack owner) {
            super(owner);
            this.owner = owner;
        }

        @Override
        public void tick() {
            if (this.owner.getActivity() == 0) {
                super.tick();
            }
        }
    }

    /** {@code EntityJumpHelper}: see {@link GroundMoveControl}. */
    static final class GroundJumpControl extends JumpControl {

        private final PitchBlack owner;

        GroundJumpControl(final PitchBlack owner) {
            super(owner);
            this.owner = owner;
        }

        @Override
        public void tick() {
            if (this.owner.getActivity() == 0) {
                super.tick();
            }
        }
    }
}
