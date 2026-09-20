package com.swbr.orespawn.entity.leon;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.portal.EntityAINearestAttackableTarget;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
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
 * Port of {@code danger.orespawn.Leon} (Leon.java:20-1149), id {@code leonopteryx} ("Leonopteryx",
 * OreSpawnMain.java:4041, {@code registerModEntity(..., 64, 1, false)}). The tameable, rideable flying dragon: on the
 * ground it runs the vanilla AI, in the air ({@code activity != 0}) its own flight code without
 * {@code super.onLivingUpdate} - unridden it picks waypoints around itself or its owner and dives at targets, ridden it
 * follows the rider's look with the forward key and the fly-up key, and strikes on its own (verhalten/entity-10.md).
 *
 * <h2>Values</h2>
 * Health 250 ({@link #mygetMaxHealth()}), speed 0.25 re-set every tick, attack 55 (fixed in {@link #doHurtTarget}),
 * armor 16, hitbox 3.5 x 8.25 (type size). The {@code Leonopteryx_health/attack/defense} config keys are read by
 * {@code MobStats.Leon_stats} but never used here - left dead (DECISIONS R18). {@code fireResistance = 10} is
 * {@link #getFireImmuneTicks()}. {@code experienceValue = 300} (:65) is never read: 1.7.10
 * {@code EntityAnimal.getExperiencePoints} returns {@code 1 + rand.nextInt(3)}, which {@code Animal.getBaseExperienceReward}
 * still does (W04 lesson; bytecode {@code wf}). Registration 64/1/false wins over the non-overriding
 * {@code getTrackingRange}/{@code getUpdateFrequency}/{@code sendsVelocityUpdates} (:106-116).
 *
 * <h2>Where the physics run</h2>
 * As in the W04 Elevator and the W06 Ostrich: the original moved the flying Leon on the server only and let the client
 * interpolate. {@code tickRidden} would run on the rider's client, while the fly-up key state lives on the server (R15),
 * so the port keeps the original split: {@link #aiStep()} is the whole {@code onLivingUpdate}, a player rider is no
 * {@code getControllingPassenger} ({@code Mob} only hands control to a {@code Mob}), and the forward input is
 * {@code Player.zza}, which 1.21.1 sends to the server every tick while the player rides ({@code LocalPlayer.tick}
 * {@code ServerboundPlayerInputPacket}, applied by {@code ServerPlayer.setPlayerInput}; checked for the W04 Elevator,
 * catalogue 6.3).
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>A diamond block tames the Leon for whoever clicks, even when it belongs to another player.</li>
 *   <li>{@code attackEntityFrom} answers {@code false} for a tame Leon hit by a player, after the hit was applied.</li>
 *   <li>{@code flyaway} is never set above 0; the branches behind it are dead but written out.</li>
 *   <li>Field writes to {@code posY} before {@code moveEntity} only affect later reads in the same step, never the
 *       position ({@code moveEntity} recomputes {@code posY} from the bounding box, W04 Elevator); they are locals.</li>
 *   <li>The rider return in {@code updateAITick} (:359-361) is unreachable in practice (a mounted Leon flies).</li>
 * </ul>
 */
public class Leon extends TamableAnimal implements AttackableNonMob, LegacyArmor {

    /** DataWatcher 20 {@code attacking} (:130). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Leon.class, EntityDataSerializers.INT);
    /** DataWatcher 21 {@code activity}: 0 on the ground, otherwise flying (:131). */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(Leon.class, EntityDataSerializers.INT);
    /** DataWatcher 22 {@code beingRidden} (:132). */
    private static final EntityDataAccessor<Integer> DATA_BEING_RIDDEN = SynchedEntityData.defineId(Leon.class, EntityDataSerializers.INT);

    /** {@code OreSpawnMain.KrakenRepellent} as an item and {@code Kraken}, both W10 - by registry id. */
    private static final ResourceLocation KRAKEN_REPELLENT_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "krakenrepellent");
    private static final ResourceLocation KRAKEN_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_kraken");

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
    private float moveSpeed;
    private float deltasmooth;

    /**
     * The 1.7.10 {@code EntityLiving.senses} cache. {@code EntityLiving.updateAITasks} cleared it at its start, which a
     * flying Leon never reaches, so in flight {@code isSuitableTarget} sees the answers cached on the ground.
     *
     * <p>PORT: {@code Mob.sensing} is private and cleared by the final {@code serverAiStep}; this cache is cleared in
     * {@link #aiStep()} under the original condition instead (W08 PitchBlack precedent).
     */
    private final Sensing legacySenses;

    /** {@code Leon(World)} (:47-81). */
    public Leon(final EntityType<? extends Leon> type, final Level par1World) {
        super(type, par1World);
        this.legacySenses = new Sensing(this);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
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
        this.moveSpeed = 0.25f;
        this.deltasmooth = 0.0f;
        // setSize(3.5f, 8.25f) (:63) is the entity type's size (R9).
        // getNavigator().setAvoidsWater(true) (:64): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        // experienceValue = 300 (:65): dead, see the class comment. fireResistance = 10 (:66): getFireImmuneTicks().
        // setSitting(this.isImmuneToFire = false) (:67); isImmuneToFire false is the type default.
        this.setSitting(false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 1.1f, 16.0f, 2.0f));
        this.goalSelector.addGoal(2, new EntityAITempt(this, 1.25, stack -> stack.is(Items.BEEF), false));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.75f));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Mob.class, 9.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(1, new MobSelectorTarget(this));
        }
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06).
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        // riddenByEntity = null (:78): no passenger yet.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
    }

    /**
     * {@code EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)} (:75): the
     * W05 port of the 1.7.10 vanilla goal (1.7.10 cadence and box) with {@code IMob.mobSelector}, i.e.
     * {@code instanceof Enemy}. {@code nearbyOnly} false is the default of that port.
     */
    private static final class MobSelectorTarget extends EntityAINearestAttackableTarget<Mob> {
        MobSelectorTarget(final Leon leon) {
            super(leon, Mob.class, 0, true);
            this.targetConditions = TargetingConditions.forCombat().selector(e -> e instanceof Enemy);
        }
    }

    /**
     * {@code applyEntityAttributes} (:94-100): health 250, speed 0.25, attack 55. Armor 16 of {@code getTotalArmorValue}
     * (:173-175) is also the {@code ARMOR} base (Hydrolisc precedent); the R5 formula reads {@link #getLegacyArmorValue()}.
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator); it matters for
     * the flight {@code moveEntity}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, 55.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code fireResistance = 10} (:66). */
    @Override
    protected int getFireImmuneTicks() {
        return 10;
    }

    /** {@code getTotalArmorValue} (:173-175) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 16;
    }

    /** {@code entityInit} (:128-148): three ints, 0. The {@code RenderInfo} reset is the constructor's fresh instance. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
        builder.define(DATA_BEING_RIDDEN, 0);
    }

    /** See {@link #legacySenses}. */
    @Override
    public Sensing getSensing() {
        return this.legacySenses != null ? this.legacySenses : super.getSensing();
    }

    /** {@code shouldRiderSit} (:102-104). */
    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    /** {@code fall} (:118-119): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:121-122): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    // canTriggerWalking (:124-126) returns true: no NoStepTrigger (R20).

    /** {@code mygetMaxHealth} (:150-152). */
    public int mygetMaxHealth() {
        return 250;
    }

    /** {@code getLeonHealth} (:154-156). */
    public int getLeonHealth() {
        return (int) this.getHealth();
    }

    /** {@code getRenderInfo} (:158-160): the model's note pad (plain data, no client imports). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:162-171): copies the fields. */
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

    /** {@code jump} (:177-180): {@code super} first, then +0.25 on top of the jump speed. */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
    }

    // isAIEnabled (:182-184): every 1.21.1 mob runs goals. canBreatheUnderwater (:186-188): the default false.

    /** {@code getLivingSound} (:190-198): only flying and unridden. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        if (this.getActivity() == 1 && !this.isVehicle()) {
            return ModSounds.LEON_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:200-202). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LEON_HIT.get();
    }

    /** {@code getDeathSound} (:204-206). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LEON_DEATH.get();
    }

    /** {@code getSoundVolume} (:208-210). */
    @Override
    protected float getSoundVolume() {
        return 1.75f;
    }

    /** {@code getSoundPitch} (:212-214). */
    @Override
    public float getVoicePitch() {
        return 0.85f;
    }

    /** {@code canBePushed} (:216-218). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code getMountedYOffset} (:220-222). */
    public double getMountedYOffset() {
        return 3.75;
    }

    /**
     * {@code dropItemRand} (:228-236): a single-item entity up to 3 blocks off on the shared {@code OreSpawnRand}, two
     * blocks up.
     *
     * <p>PORT: {@code Item.getItemFromBlock(OreSpawnMain.KrakenRepellent)} is looked up by registry id
     * ({@code krakenrepellent}, registered since W10); without the block the four {@code OreSpawnRand} draws still happen but no
     * item is dropped.
     */
    private void dropItemRand(@Nullable final Item index, final int par1) {
        final double x = this.getX() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4);
        final double y = this.getY() + 2.0;
        final double z = this.getZ() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4);
        if (index == null) {
            return;
        }
        final ItemEntity var3 = new ItemEntity(this.level(), x, y, z, new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}; {@code getDropItem} beef (:224-226) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:238-251): 4-9 raw chicken, 16-21 feathers, 2-7 Kraken Repellent, 1/5 a Battle Axe. Looting ignored. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 4 + this.level().random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.CHICKEN, 1);
        }
        for (int i = 16 + this.level().random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.FEATHER, 1);
        }
        final Optional<Item> repellent = BuiltInRegistries.ITEM.getOptional(KRAKEN_REPELLENT_ID);
        for (int i = 2 + this.level().random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(repellent.orElse(null), 1);
        }
        if (this.level().random.nextInt(5) == 1) {
            this.dropItemRand(ModItems.BATTLE_AXE.get(), 1);
        }
    }

    /**
     * {@code attackEntityAsMob} (:253-281): against the Ender Dragon an explosion source with 55 on the head (1/6) or
     * the body; against any other living entity 55 mob damage (x4 against the Kraken), then a push of 1.25 away and
     * 0.15 up (0.3 for players and removed entities), whether or not the hit landed. Always {@code true}.
     *
     * <p>PORT: {@code dragonPartBody} is private in 1.21.1 and reached through {@code getSubEntities()} (PitchBlack
     * precedent). {@code addVelocity} is {@code push} with {@code hurtMarked} (MonsterSupport/PitchBlack precedent).
     * PORT: {@code instanceof Kraken} is the registry id {@code the_kraken} (same set, the Kraken has no subclass).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 1.25;
        double inair = 0.15;
        float iskraken = 1.0f;
        if (par1Entity != null && par1Entity instanceof EnderDragon dr) {
            DamageSource var21 = null;
            var21 = this.damageSources().explosion(null, null);
            if (this.level().random.nextInt(6) == 1) {
                dr.hurt(dr.head, var21, 55.0f);
            } else {
                dr.hurt(dragonBody(dr), var21, 55.0f);
            }
        } else if (par1Entity != null && par1Entity instanceof LivingEntity) {
            if (isKraken(par1Entity)) {
                iskraken = 4.0f;
            }
            par1Entity.hurt(this.damageSources().mobAttack(this), iskraken * 55.0f);
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            par1Entity.hurtMarked = true;
        }
        return true;
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

    /** {@code instanceof Kraken} by registry id until W10 (see {@link #doHurtTarget}). */
    private static boolean isKraken(final Entity e) {
        return KRAKEN_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()));
    }

    /**
     * {@code attackEntityFrom} (:283-314): refused while {@code hurt_timer} runs and for {@code inWall}; un-sits and
     * takes off on the server; a hit from another Leon is refused; otherwise the hit, 15 ticks of immunity, and a
     * living attacker becomes the target and is walked at - except that a tame Leon hit by a player answers
     * {@code false} after the hit was applied (R18).
     *
     * <p>PORT: {@code setTarget(Entity)} set 1.7.10's old-AI {@code entityToAttack}, which no AI of an
     * {@code isAIEnabled} mob read; it has no 1.21.1 counterpart and is dropped.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        Entity e = null;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return ret;
        }
        if (!this.level().isClientSide) {
            this.setSitting(false);
        }
        if (!this.level().isClientSide) {
            this.setActivity(1);
        }
        e = par1DamageSource.getEntity();
        if (e != null && e instanceof Leon) {
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        this.hurt_timer = 15;
        if (e != null && e instanceof LivingEntity living && !this.level().isClientSide) {
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
     * {@code updateAITasks} (:316-324) with {@code updateAITick} (:358-366): the inherited step ({@code Animal}'s
     * {@code inLove} reset) unless ridden, then forget the target with 1/200.
     */
    @Override
    protected void customServerAiStep() {
        if (this.level().isClientSide) {
            return;
        }
        if (!this.isVehicle()) {
            super.customServerAiStep();
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setTarget(null);
        }
    }

    /**
     * {@code fly_with_rider} (:326-356): with 1/7, not on Peaceful, keep a living target or look for one; with a target
     * set {@code attacking} and strike within {@code 9 + width/2}; without one clear {@code attacking}.
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
                if (this.distanceToSqr(e) < (9.0f + e.getBbWidth() / 2.0f) * (9.0f + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                }
                return;
            }
            this.setAttacking(0);
        }
    }

    /**
     * {@code isSuitableTarget} (:368-408): not Peaceful, {@code PlayNicely == 0}, alive, not ignorable, visible, no Leon;
     * then any {@code EntityMob}, a survival player only while wild, and while wild anything attackable.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Leon) {
            return false;
        }
        // EntityMob is Monster (util.MyUtils reading).
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild && !this.isTame();
        }
        if (!this.isTame()) {
            if (MyUtils.isAttackableNonMob(par1EntityLiving)) {
                return true;
            }
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:410-427): the first suitable entity in {@code expand(20, 20, 20)}, nearest first. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0, 20.0, 20.0)));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    // doesEntityNotTriggerPressurePlate (:429-431) returns the default false.

    /**
     * {@code getCanSpawnHere} (:433-458) as the placement predicate: a "Leonopteryx" spawner allows it; otherwise 1/16 on
     * the world random, by day, no other Leon within {@code expand(48, 16, 48)}, and Y at least 50.
     *
     * <p>PORT: the spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} (catalogue 5.9, Mothra precedent).
     * The entity is not there yet, so its box is the type's spawn box at the block centre and "other" is every Leon.
     * Chunk-generation spawns skip the predicate (R23).
     */
    public static boolean checkLeonSpawnRules(final EntityType<Leon> type, final ServerLevelAccessor level,
                                              final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        final ServerLevel world = level.getLevel();
        if (world.random.nextInt(16) != 0) {
            return false;
        }
        if (!InsectSupport.isDaytime(world)) {
            return false;
        }
        final boolean noOther = level.getEntitiesOfClass(Leon.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(48.0, 16.0, 48.0)).isEmpty();
        return noOther && pos.getY() >= 50.0;
    }

    /** The whole rule is {@link #checkLeonSpawnRules}; asking it twice would square the 1/16 roll. */
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
     * {@code canSeeTarget} (:460-462): no block between a point 0.75 above the feet and the target. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded (Mothra precedent).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code setPositionAndRotation2} (:464-474): the vanilla interpolation ({@code super}) and a copy for the flight
     * interpolation in {@link #aiStep()}.
     */
    @Override
    public void lerpTo(final double par1, final double par3, final double par5, final float par7, final float par8,
                       final int par9) {
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
     * Rotation-only packets start from the pending target. 1.7.10 used the tracked server position; the last packet's
     * position is that value in both modes (W04 Elevator, W06 Ostrich).
     */
    @Override
    public double lerpTargetX() {
        return this.boatPosRotationIncrements > 0 ? this.boatX : super.lerpTargetX();
    }

    @Override
    public double lerpTargetY() {
        return this.boatPosRotationIncrements > 0 ? this.boatY : super.lerpTargetY();
    }

    @Override
    public double lerpTargetZ() {
        return this.boatPosRotationIncrements > 0 ? this.boatZ : super.lerpTargetZ();
    }

    /**
     * 1.7.10 {@code S19PacketEntityHeadLook} set {@code rotationYawHead} directly; 1.21.1 lerps it in
     * {@code LivingEntity.aiStep}, which a flying client Leon never reaches (W06 Ostrich precedent).
     */
    @Override
    public void lerpHeadTo(final float yaw, final int steps) {
        this.setYHeadRot(yaw);
    }

    // setVelocity (:476-479) only calls super.

    /**
     * {@code onUpdate} (:481-509): speed, the vanilla tick, the immunity countdown, the wing beat every 21 ticks in
     * flight, buoyancy in water; then on the server a tame, standing Leon more than 12 blocks from its owner takes off.
     *
     * <p>PORT: {@code "orespawn:MothraWings"} is {@link ModSounds#MOTHRA_WINGS} ({@code mothrawings}, R2).
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
                    ArthropodSupport.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 0.5f, 1.0f);
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
            if (e != null && this.distanceToSqr(e) > 144.0) {
                this.setActivity(1);
            }
        }
    }

    /**
     * {@code fly_without_rider} (:511-690): stuck detection, vertical damping by the waypoint height, a new waypoint with
     * 1/300, near the waypoint or when the owner is more than 12 blocks off; with 1/8 an attack run or a flight from
     * danger at low health; lift over ground below the flight path; steer towards the waypoint.
     *
     * <p>{@code (int)} casts of coordinates are {@link Mth#floor} (R20); {@code worldObj.rand} is the level random,
     * {@code moveForward} is {@code zza}. The motion is kept in locals and written back before every call that could
     * change it from outside (a hit's knockback) and before {@code move}.
     */
    private void fly_without_rider() {
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
        double obstruction_factor = 0.0;
        Vec3 m = this.getDeltaMovement();
        double motionX = m.x;
        double motionY = m.y;
        double motionZ = m.z;
        final double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        int toofar = 0;
        if (this.level().isClientSide) {
            return;
        }
        if (this.currentFlightTarget == null) {
            do_new = 1;
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
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
                this.setActivity(1);
                do_new = 1;
            }
        } else {
            this.stuck_count = 0;
            this.lastX = Mth.floor(this.getX());
            this.lastZ = Mth.floor(this.getZ());
        }
        // posY is a local: the write at :668 did not move the bounding box, but :671 reads it (class comment).
        double posY = this.getY();
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
                && this.level().random.nextInt(8) == 1) {
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
                    if (this.distanceToSqr(e) < (7.0f + e.getBbWidth() / 2.0f) * (7.0f + e.getBbWidth() / 2.0f)) {
                        this.setDeltaMovement(motionX, motionY, motionZ);
                        this.doHurtTarget(e);
                        m = this.getDeltaMovement();
                        motionX = m.x;
                        motionY = m.y;
                        motionZ = m.z;
                    }
                }
            } else {
                this.target_in_sight = false;
                this.setAttacking(this.flyaway = 0);
            }
        }
        if (InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(posY), Mth.floor(this.getZ())) < 4.1f) {
            do_new = 1;
        }
        if ((do_new != 0 && !this.target_in_sight) || (do_new != 0 && this.flyaway != 0)) {
            // for (Block bid = Blocks.stone; bid != Blocks.air && keep_trying != 0; --keep_trying) (:620)
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                int gox = Mth.floor(this.getX());
                int goy = Mth.floor(posY);
                int goz = Mth.floor(this.getZ());
                if (has_owner == 1 && this.unstick_timer == 0) {
                    gox = Mth.floor(ox);
                    goy = Mth.floor(oy);
                    goz = Mth.floor(oz);
                    if (this.owner_flying == 0) {
                        zdir = this.level().random.nextInt(12) + 6;
                        xdir = this.level().random.nextInt(12) + 6;
                    } else {
                        zdir = this.level().random.nextInt(8);
                        xdir = this.level().random.nextInt(8);
                    }
                } else {
                    zdir = this.level().random.nextInt(20) + 6;
                    xdir = this.level().random.nextInt(20) + 6;
                }
                if (this.level().random.nextInt(2) == 1) {
                    zdir = -zdir;
                }
                if (this.level().random.nextInt(2) == 1) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(gox + xdir, goy + this.level().random.nextInt(9 + this.owner_flying * 2) - 4, goz + zdir);
                bidIsAir = this.level().getBlockState(this.currentFlightTarget).isAir();
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                        this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
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
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = (float) (0.75 * speed_factor);
        this.setYRot(this.getYRot() + var5 / 5.0f);
        // moveEntity (:689): 1.21.1's move zeroes the blocked motion components like 1.7.10's did (W04 Elevator).
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
    }

    /**
     * {@code onLivingUpdate} (:692-911). {@link #always_do()} first; on the ground the vanilla update; in flight nothing
     * of {@code super} - no AI, despawn roll, gravity or collisions. The client interpolates a flying Leon itself; the
     * server marks the rider and runs the ridden flight or {@link #fly_without_rider()}.
     *
     * <p>{@code isDead} is {@code isRemoved()}.
     */
    @Override
    public void aiStep() {
        // :695-696: never read, but each draws from the entity random every tick, on both sides.
        @SuppressWarnings("unused")
        final double d6 = this.random.nextFloat() * 2.0f - 1.0f;
        @SuppressWarnings("unused")
        final double d7 = (this.random.nextInt(2) * 2 - 1) * 0.7;
        this.always_do();
        if (this.getActivity() == 0) {
            // EntityLiving.updateAITasks started with senses.clearSensingCache (see legacySenses).
            if (!this.isImmobile() && this.isEffectiveAi()) {
                this.legacySenses.tick();
            }
            super.aiStep();
        } else if (this.isRemoved()) {
            super.aiStep();
            return;
        }
        if (this.isRemoved()) {
            return;
        }
        final Entity rider = this.getFirstPassenger();
        if (this.level().isClientSide) {
            if (this.boatPosRotationIncrements > 0 && this.getActivity() != 0) {
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
                this.setYHeadRot(this.getYRot());
                --this.boatPosRotationIncrements;
            }
        } else {
            if (rider != null) {
                this.setBeingRidden(1);
            } else {
                this.setBeingRidden(0);
            }
            if (this.getActivity() != 0) {
                if (rider != null) {
                    this.riddenUpdate(rider);
                } else {
                    this.fly_without_rider();
                }
            }
        }
    }

    /**
     * The server branch of {@code onLivingUpdate} with a rider (:740-905): clamp the motion, hover over ground 1.55
     * below or sink, lift over obstacles ahead, follow the rider's yaw, climb on the fly-up key, accelerate or brake with
     * the forward key, move, damp, push the entities around, then the rider's own attack roll.
     */
    private void riddenUpdate(final Entity rider) {
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 1.15;
        double gh = 1.0;
        double pi = 3.1415926545;
        double deltav = 0.0;
        int dist = 2;
        // PORT: (EntityPlayer) this.riddenByEntity (:741) threw for any other rider; 1.21.1's /ride can seat a mob.
        // A non-player rider reads as no forward input (R18 case 1, W04 Elevator, W06 Ostrich).
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
        gh = 1.55;
        // posY is a local: the write at :759 did not move the bounding box, but :771 reads it (class comment).
        double posY = this.getY();
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
                bid = this.level().getBlockState(
                        new BlockPos(Mth.floor(this.getX() + dx), Mth.floor(posY) - k, Mth.floor(this.getZ() + dz)));
                if (!bid.isAir()) {
                    obstruction_factor += 0.05;
                }
            }
        }
        motionY += obstruction_factor * 0.07;
        posY += obstruction_factor * 0.07; // :778, read by nothing afterwards
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
        // relative_g = abs(relative_g) * velocity, reset above 50 (:805-808): never read again.
        this.setXRot(2.0f * (float) velocity);
        this.setRot(this.getYRot(), this.getXRot());
        this.setYHeadRot(this.getYRot());
        double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        // rr = atan2(rider.motionZ, rider.motionX) (:814) and rt = 0 (:817) are never read.
        final double rhm = Math.atan2(motionZ, motionX);
        final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        pi = 3.1415926545;
        deltav = 0.0;
        final float im = pp != null ? pp.zza : 0.0f; // moveForward
        // OreSpawnMain.flyup_keystate != 0 (:821): per rider (R15).
        if (RiderKeys.isFlyUp(rider)) {
            motionY += 0.035;
            motionY += velocity * 0.038;
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
                deltav = 0.028;
                if (max_speed > 1.0) {
                    deltav += 0.06;
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
        // moveEntity (:886): 1.21.1's move zeroes the blocked motion components like 1.7.10's did (W04 Elevator).
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
        final Vec3 moved = this.getDeltaMovement();
        this.setDeltaMovement(moved.x * 0.985, moved.y * 0.94, moved.z * 0.985);
        if (!this.level().isClientSide) {
            final List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(2.25, 2.0, 2.25));
            if (list != null && !list.isEmpty()) {
                for (int l = 0; l < list.size(); ++l) {
                    final Entity listEntity = list.get(l);
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
     * 1.7.10 {@code Entity.applyEntityCollision} ({@code sa.g(Lsa;)V}, javap on client-1.7.10.jar), called as
     * {@code self.applyEntityCollision(other)}: unless one rides the other, both are pushed apart by 0.05 along the
     * dominant axis, {@code self} away from {@code other} and {@code other} away from {@code self}.
     *
     * <p>PORT: 1.21.1 {@code Entity.push(Entity)} skips a vehicle and anything not pushable, so the ridden Leon itself
     * ({@code isPushable() == false}, a vehicle) would never be pushed back; 1.7.10 pushed it regardless. Written out.
     * {@code entityCollisionReduction} is 0 for every vanilla and OreSpawn entity and is left out; the minecart's own
     * override ({@code xl.g}) is not reproduced.
     */
    private static void applyEntityCollision(final Entity self, final Entity other) {
        if (other.getFirstPassenger() == self || other.getVehicle() == self) {
            return;
        }
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

    /**
     * {@code always_do} (:913-952), server only: a standing Leon takes off with 1/10 when it sees a target; heal 2 with
     * 1/250; unless sitting, follow a flying owner into the air, take off when the owner is more than 20 blocks away,
     * and with 1/50 (no target in sight, no rider) land (14/15) or take off (1/15).
     */
    public void always_do() {
        LivingEntity e = null;
        if (this.level().isClientSide) {
            return;
        }
        if (!this.isSitting() && this.getActivity() == 0 && !this.isVehicle() && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.level().random.nextInt(10) == 1) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setActivity(1);
            }
        }
        if (this.level().random.nextInt(250) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(2.0f);
        }
        if (this.isSitting()) {
            return;
        }
        this.owner_flying = 0;
        // PORT: (EntityPlayer) this.getOwner() (:933, :939) - the owner of a 1.21.1 tamable is a LivingEntity; any other
        // owner reads as not flying and is still measured.
        if (this.isTame() && this.getOwner() != null && !this.isVehicle() && !this.isSitting()) {
            if (this.getOwner() instanceof Player pl && pl.getAbilities().flying) {
                this.setActivity(this.owner_flying = 1);
            }
        }
        if (this.isTame() && this.getOwner() != null && !this.isSitting()) {
            final LivingEntity pl = this.getOwner();
            if (this.distanceToSqr(pl) > 400.0) {
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
    }

    /**
     * {@code updateRiderPosition} (:954-959): 0.65 along the look direction ({@code posX - f*sin(yaw)},
     * {@code posZ + f*cos(yaw)}), {@code posY + 3.75 + rider.getYOffset()}. A server-side 1.7.10 player had
     * {@code getYOffset() = -0.5}; 1.21.1 positions passengers by their feet (W04 Elevator, W06 Ostrich reading). A
     * non-player {@code /ride} passenger gets 0.
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
     * {@code playTameEffect} (:961-972): 20 hearts or smoke puffs in a 5-block cloud. Server calls draw from the random
     * and show nothing, as 1.7.10's server {@code spawnParticle} did; entity events 6 and 7 reach it on the client.
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
     * {@code interact} (:974-1091), in the original order, on both sides; no {@code super.interact}. A {@code true}
     * return is {@code sidedSuccess}. {@code func_152115_b} sets the owner UUID ({@code ""} clears it),
     * {@code func_152114_e} is "is owner" (catalogue 5.10). The 1.7.10 order around it (leash, then this, then the held
     * name tag) is restored by {@link LeonInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :975-979 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (!var2.isEmpty() && var2.is(Items.DIAMOND_BLOCK) && par1EntityPlayer.distanceToSqr(this) < 49.0) {
            if (!isRemote) {
                // PORT: setTamed + owner without TamableAnimal.tame's advancement trigger, as in Girlfriend/Hydrolisc.
                this.setTame(true, false);
                this.setOwnerUUID(par1EntityPlayer.getUUID());
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.heal(this.mygetMaxHealth() - this.getHealth());
            }
            consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (!this.isTame()) {
            if (!var2.isEmpty() && var2.is(Items.BEEF) && par1EntityPlayer.distanceToSqr(this) < 49.0) {
                if (!isRemote) {
                    if (this.level().random.nextInt(3) == 1) {
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
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
        } else {
            if (!this.isOwnedBy(par1EntityPlayer)) {
                return InteractionResult.PASS;
            }
            if (var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 49.0) {
                if (!isRemote) {
                    // PORT: 1.7.10 mountEntity replaced a current rider; 1.21.1 startRiding refuses while the seat is
                    // taken (Entity.canAddPassenger) - W06 Ostrich.
                    par1EntityPlayer.startRiding(this);
                    this.setActivity(1);
                    this.setSitting(false);
                }
                return success;
            }
            if (!var2.isEmpty() && var2.is(Items.BEEF) && par1EntityPlayer.distanceToSqr(this) < 49.0) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.mygetMaxHealth() > this.getHealth()) {
                    this.heal(this.mygetMaxHealth() - this.getHealth());
                }
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (!var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 49.0) {
                if (!isRemote) {
                    this.setTame(false, false);
                    this.setOwnerUUID(null);
                    this.spawnTamingParticles(false);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 49.0
                    && this.isOwnedBy(par1EntityPlayer)) {
                this.setCustomName(var2.getHoverName());
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (!var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 49.0 && !this.isVehicle()) {
                if (!this.isSitting()) {
                    this.setSitting(true);
                    this.setActivity(0);
                } else {
                    this.setSitting(false);
                    this.setActivity(0);
                }
                return success;
            }
        }
        return InteractionResult.PASS;
    }

    /** {@code --stackSize} unless {@code capabilities.isCreativeMode}; the emptied stack replaces the explicit {@code null}. */
    private static void consumeOne(final Player player, final ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    /** {@code isWheat} (:1093-1095): beef; overrides nothing in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.BEEF);
    }

    /**
     * 1.7.10 {@code EntityAnimal.isBreedingItem} default (wheat): Leon does not override it. Only {@code Animal.mobInteract}
     * and a breed goal ask it, and Leon has neither.
     */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code getAttacking} (:1097-1099). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:1101-1106): server only. */
    public void setAttacking(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getActivity} (:1108-1110). */
    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    /** {@code setActivity} (:1112-1117): server only. */
    public void setActivity(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code getBeingRidden} (:1119-1121). */
    public int getBeingRidden() {
        return this.entityData.get(DATA_BEING_RIDDEN);
    }

    /** {@code setBeingRidden} (:1123-1128): server only. */
    public void setBeingRidden(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_BEING_RIDDEN, par1);
    }

    /** {@code createChild} (:1130-1132). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /**
     * {@code canDespawn} (:1134-1136): not persistent, no rider, wild ({@code isPersistenceRequired} is checked by
     * {@code Mob.checkDespawn} before this is asked). {@code Animal} would answer {@code false}.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.isVehicle() && !this.isTame();
    }

    /**
     * {@code despawnEntity} ran inside {@code EntityLiving.updateAITasks}, which a flying Leon skips.
     *
     * <p>PORT (R18 case 3): 1.21.1 runs the despawn check from the level for every mob on every tick; in flight it is
     * skipped as in 1.7.10 ({@code Leon} is no {@code EntityMob}, so there is no peaceful removal to keep; W08 PitchBlack
     * precedent).
     */
    @Override
    public void checkDespawn() {
        if (this.getActivity() != 0) {
            return;
        }
        super.checkDespawn();
    }

    /** {@code writeEntityToNBT} (:1138-1142). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("LeonAttacking", this.getAttacking());
        par1NBTTagCompound.putInt("LeonActivity", this.getActivity());
    }

    /** {@code readEntityFromNBT} (:1144-1148). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setAttacking(par1NBTTagCompound.getInt("LeonAttacking"));
        this.setActivity(par1NBTTagCompound.getInt("LeonActivity"));
    }

    /** 1.7.10 {@code EntityTameable.setSitting}: one synced flag; 1.21.1 splits it into order and pose. */
    public void setSitting(final boolean sitting) {
        this.setOrderedToSit(sitting);
        this.setInSittingPose(sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * {@code onDeath}: no override in the original (Leon.java:20, {@code extends EntityTameable}), and 1.7.10
     * {@code EntityTameable} did not override it either, so the owner got no chat message when the Leon died.
     *
     * <p>PORT: R22 (addendum 2026-09-14, owner death message of tame OreSpawn animals 1:1 off) -
     * {@code TamableAnimal.die} in 1.21.1 sends {@code getCombatTracker().getDeathMessage()} to the owner. Java cannot
     * skip one super level, so this is {@code LivingEntity.die} line by line (NeoForge 21.1 sources) without the owner
     * message; the named-entity log line uses a logger of its own (EntityCannonFodder precedent).
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

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
