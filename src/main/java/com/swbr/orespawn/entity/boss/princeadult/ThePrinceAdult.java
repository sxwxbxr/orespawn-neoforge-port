package com.swbr.orespawn.entity.boss.princeadult;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.boss.king.TheKing;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.dragon.Spyro;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.leon.Leon;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.portal.EntityAINearestAttackableTarget;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.entity.waterdragon.GammaMetroid;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.util.Royalty;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.ThePrinceAdult} (ThePrinceAdult.java:19-1467), id {@code the_young_adult_prince}
 * ("The Young Adult Prince", {@code registerModEntity(..., 128, 1, false)}). The last prince stage: a three-headed
 * dragon the size of half a King, tameable only through the Teen, rideable, and - with {@code FullPowerKingEnable} -
 * turned into a free, hostile {@link TheKing} after 288000 ground AI ticks (verhalten/entity-02.md).
 *
 * <h2>Values</h2>
 * Health 3000 ({@link #mygetMaxHealth()}) through virtual health (R4: attribute {@code 1024}, every hit and heal scaled
 * by {@code CombatEvents} after the original checks in {@link #hurt}), attack 100 (fixed in {@link #doHurtTarget}, x2
 * against the Kraken - the special factor at ThePrinceAdult.java:295, catalogue 6.5), armor 20 under the 1.7.10
 * formula (R5, {@link #getLegacyArmorValue()}), speed 0.36 re-set every tick, hitbox 6.25 x 10.25 (type size).
 * {@code fireResistance = 1000} is {@link #getFireImmuneTicks()}, {@code isImmuneToFire} the type's {@code fireImmune()}.
 * {@code experienceValue = 3000} (:75) is never read: 1.7.10 {@code EntityAnimal.getExperiencePoints} returns
 * {@code 1 + rand.nextInt(3)}, which {@code Animal.getBaseExperienceReward} still does (W04 lesson). The registration
 * 128/1/false wins over {@code getTrackingRange}/{@code getUpdateFrequency}/{@code sendsVelocityUpdates} (:117-127),
 * which override nothing in 1.7.10 (Leon precedent). No boss bar: the original neither implemented
 * {@code IBossDisplayData} nor called {@code BossStatus} in its renderer.
 *
 * <h2>Where the physics run</h2>
 * As in the W09 Leon: {@link #aiStep()} is the whole {@code onLivingUpdate}. On the ground the vanilla update runs;
 * in the air ({@code activity != 0}) the server steers the prince itself, ridden or unridden, and the client
 * interpolates. In flight {@code noClip} ({@link Entity#noPhysics}) is set, so the prince flies through walls.
 * The forward and strafe input of a rider is {@code Player.zza}/{@code xxa}, which 1.21.1 sends to the server every
 * tick while the player rides (W04 Elevator, catalogue 6.3); the fly-up key is per rider (R15).
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>A diamond block heals any prince to full and sets the grow counter to 288000, wild or not; it never tames.</li>
 *   <li>A diamond turns the prince back into a Teen; the owner of the Teen is the clicking owner.</li>
 *   <li>{@code attackEntityFrom} answers {@code false} for a tame prince hit by a player, after the hit was applied.</li>
 *   <li>The {@code BetterFireball}/{@code EntitySmallFireball} immunity (:334-341) needs a source whose entity is the
 *       fireball itself (catalogue 6.4); it is kept as written.</li>
 *   <li>Field writes to {@code posY} before {@code moveEntity} only affect later reads in the same step, never the
 *       position ({@code moveEntity} recomputed {@code posY} from the bounding box); they are locals here.</li>
 *   <li>On the client a diamond click falls through to the sit toggle (the teen branch is server only), as in 1.7.10.</li>
 * </ul>
 */
public class ThePrinceAdult extends TamableAnimal implements Royalty, LegacyArmor, VirtualHealth {

    /** DataWatcher 20 {@code attacking} (:174). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    /** DataWatcher 21 {@code activity}: 0 on the ground, otherwise flying (:175). */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    /** DataWatcher 24 {@code ThePrinceAdultFire}: 1 lit, 0 extinguished (:176, :183). */
    private static final EntityDataAccessor<Integer> DATA_FIRE = SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    /** DataWatcher 22 left head extension, 0..60 (:177). */
    private static final EntityDataAccessor<Integer> DATA_HEAD1_EXT = SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    /** DataWatcher 23 centre head extension, 0..60 (:178). */
    private static final EntityDataAccessor<Integer> DATA_HEAD2_EXT = SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);
    /** DataWatcher 25 right head extension, 0..60 (:179). */
    private static final EntityDataAccessor<Integer> DATA_HEAD3_EXT = SynchedEntityData.defineId(ThePrinceAdult.class, EntityDataSerializers.INT);

    /** {@code EntityList} name "The King" (:379). */
    private static final ResourceLocation THE_KING_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_king");
    /** {@code EntityList} name "The Young Prince" (:1232). */
    private static final ResourceLocation THE_YOUNG_PRINCE_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_young_prince");
    /** {@code OreSpawnMain.ThePrinceEgg} (:273, :287). */
    private static final ResourceLocation THE_PRINCE_EGG_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "eggtheprince");

    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    @SuppressWarnings("unused")
    private double boatYawHead;
    @SuppressWarnings("unused")
    private int updateit;
    @SuppressWarnings("unused")
    private int playing;
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
    private int growcounter;

    /**
     * The 1.7.10 {@code EntityLiving.senses} cache. {@code EntityLiving.updateAITasks} cleared it at its start, which a
     * flying prince never reaches, so in flight {@code isSuitableTarget} sees the answers cached on the ground.
     *
     * <p>PORT: {@code Mob.sensing} is private and cleared by the final {@code serverAiStep}; this cache is cleared in
     * {@link #aiStep()} under the original condition instead (W08 PitchBlack, W09 Leon precedent).
     */
    private final Sensing legacySenses;

    /** {@code ThePrinceAdult(World)} (:50-92). */
    public ThePrinceAdult(final EntityType<? extends ThePrinceAdult> type, final Level par1World) {
        super(type, par1World);
        this.legacySenses = new Sensing(this);
        this.updateit = 1;
        this.playing = 0;
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.hurt_timer = 0;
        this.wing_sound = 0;
        this.currentFlightTarget = null;
        this.target_in_sight = false;
        this.owner_flying = 0;
        this.flyaway = 0;
        this.moveSpeed = 0.36f;
        this.deltasmooth = 0.0f;
        this.which_attack = 0;
        this.fireballticker = 0;
        this.head1ext = 0;
        this.head2ext = 0;
        this.head3ext = 0;
        this.head1dir = 1;
        this.head2dir = 1;
        this.head3dir = 1;
        this.growcounter = 0;
        // setSize(6.25f, 10.25f) (:73) is the entity type's size (R9).
        // getNavigator().setAvoidsWater(true) (:74): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        // experienceValue = 3000 (:75): dead, see the class comment. fireResistance = 1000 (:76): getFireImmuneTicks().
        // isImmuneToFire = true (:77): EntityType.Builder.fireImmune() (registration).
        this.setSitting(false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 1.1f, 12.0f, 2.0f));
        this.goalSelector.addGoal(2, new EntityAITempt(this, 1.25, stack -> stack.is(Items.BEEF), false));
        this.goalSelector.addGoal(3, new MyEntityAIWander(this, 0.75f));
        // EntityLiving.class is Mob.class.
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Mob.class, 20.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.targetSelector.addGoal(1, new MobSelectorTarget(this));
        }
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06) and Leon (W09).
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        // riddenByEntity = null (:89): no passenger yet.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
    }

    /**
     * {@code EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, IMob.mobSelector)} (:86): the
     * W05 port of the 1.7.10 vanilla goal with {@code IMob.mobSelector}, i.e. {@code instanceof Enemy} (Leon precedent).
     */
    private static final class MobSelectorTarget extends EntityAINearestAttackableTarget<Mob> {
        MobSelectorTarget(final ThePrinceAdult prince) {
            super(prince, Mob.class, 0, true);
            this.targetConditions = TargetingConditions.forCombat().selector(e -> e instanceof Enemy);
        }
    }

    /**
     * {@code applyEntityAttributes} (:105-111): health {@code mygetMaxHealth()} = 3000 clamped to the attribute range
     * (R4), speed 0.36, attack 100. Armor 20 of {@code getTotalArmorValue} (:221-223) is also the {@code ARMOR} base
     * (Leon precedent); the R5 formula reads {@link #getLegacyArmorValue()}.
     *
     * <p>PORT: {@code applyEntityAttributes} ran inside the super constructor before {@code moveSpeed} was initialised,
     * so 1.7.10 briefly had speed 0 until the first {@code onUpdate} (:545); the base here is 0.36 from the start.
     * {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, W09 Leon).
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(3000.0))
                .add(Attributes.MOVEMENT_SPEED, (double) 0.36f)
                .add(Attributes.ATTACK_DAMAGE, 100.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** R4: the original maximum, {@code mygetMaxHealth()} (:198-200). */
    @Override
    public double getOriginalMaxHealth() {
        return this.mygetMaxHealth();
    }

    /** {@code fireResistance = 1000} (:76). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code getTotalArmorValue} (:221-223) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 20;
    }

    /**
     * {@code entityInit} (:172-196): attacking 0, activity 0, fire 1, the three head extensions 0; {@code setTamed(false)}
     * is the tamable default. The {@code RenderInfo} reset is the constructor's fresh instance.
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

    /** See {@link #legacySenses}. */
    @Override
    public Sensing getSensing() {
        return this.legacySenses != null ? this.legacySenses : super.getSensing();
    }

    /** {@code shouldRiderSit} (:113-115). */
    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    /** {@code getHead1Ext} (:129-131). */
    public int getHead1Ext() {
        return this.entityData.get(DATA_HEAD1_EXT);
    }

    /** {@code getHead2Ext} (:133-135). */
    public int getHead2Ext() {
        return this.entityData.get(DATA_HEAD2_EXT);
    }

    /** {@code getHead3Ext} (:137-139). */
    public int getHead3Ext() {
        return this.entityData.get(DATA_HEAD3_EXT);
    }

    /** {@code setHead1Ext} (:141-146): server only. */
    public void setHead1Ext(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_HEAD1_EXT, par1);
    }

    /** {@code setHead2Ext} (:148-153): server only. */
    public void setHead2Ext(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_HEAD2_EXT, par1);
    }

    /** {@code setHead3Ext} (:155-160): server only. */
    public void setHead3Ext(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_HEAD3_EXT, par1);
    }

    /** {@code fall} (:162-163): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:165-166): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    // canTriggerWalking (:168-170) returns true: no NoStepTrigger (R20).

    /** {@code mygetMaxHealth} (:198-200), in original units. */
    public int mygetMaxHealth() {
        return 3000;
    }

    /** {@code getThePrinceAdultHealth} (:202-204): the health in original units (R4). */
    public int getThePrinceAdultHealth() {
        return (int) VirtualHealth.originalHealth(this);
    }

    /** {@code getRenderInfo} (:206-208): plain data, no client imports. */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:210-219): copies the fields. */
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

    /** {@code jump} (:225-228): {@code super} first, then +0.35 on top of the jump speed. */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.35, m.z);
    }

    // isAIEnabled (:230-232): every 1.21.1 mob runs goals.
    // canBreatheUnderwater (:234-236): LivingEntity.canBreatheUnderwater is final in 1.21.1 and reads the entity type tag
    // #minecraft:can_breathe_under_water (registration, W06/W09 precedent).

    /** {@code getLivingSound} (:238-246): only flying and unridden. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        if (this.getActivity() == 1 && !this.isVehicle()) {
            return ModSounds.KING_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:248-250). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.KING_HIT.get();
    }

    /** {@code getDeathSound} (:252-254). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TREX_DEATH.get();
    }

    /** {@code getSoundVolume} (:256-258). */
    @Override
    protected float getSoundVolume() {
        return 0.85f;
    }

    /** {@code getSoundPitch} (:260-262). */
    @Override
    public float getVoicePitch() {
        return 1.1f;
    }

    /** {@code canBePushed} (:264-266). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code getMountedYOffset} (:268-270). */
    public double getMountedYOffset() {
        return 9.25;
    }

    /**
     * {@code dropItemRand} (:276-284): a single-item entity up to one block off on the shared {@code OreSpawnRand}, one
     * block up. An unregistered item (the Prince egg belongs to another porter of this wave) still draws the offsets.
     */
    private void dropItemRand(@Nullable final Item index, final int par1) {
        final double x = this.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2);
        final double y = this.getY() + 1.0;
        final double z = this.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2);
        if (index == null) {
            return;
        }
        final ItemEntity var3 = new ItemEntity(this.level(), x, y, z, new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}; {@code getDropItem} (:272-274) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:286-288): exactly one Prince egg. Looting ignored.
     *
     * <p>PORT: {@code OreSpawnMain.ThePrinceEgg} is resolved by its registry id {@code eggtheprince}, because its holder
     * is written by the prince porter of the same wave.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        this.dropItemRand(BuiltInRegistries.ITEM.getOptional(THE_PRINCE_EGG_ID).orElse(null), 1);
    }

    /**
     * {@code attackEntityAsMob} (:290-306): against any living entity 100 mob damage - twice that against the Kraken
     * (the special factor of :295-297) - then a push of 2.0 away and 0.2 up (0.4 for players and removed entities),
     * whether or not the hit landed. Always {@code true}.
     *
     * <p>PORT: {@code addVelocity} is {@code push} with {@code hurtMarked} (Leon precedent).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 2.0;
        double inair = 0.2;
        float iskraken = 1.0f;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            if (par1Entity instanceof Kraken) {
                iskraken = 2.0f;
            }
            par1Entity.hurt(this.damageSources().mobAttack(this), iskraken * 100.0f);
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            par1Entity.hurtMarked = true;
        }
        return true;
    }

    /**
     * {@code attackEntityFrom} (:308-360): refused while {@code hurt_timer} runs; cactus, fire, burning and lava are
     * ignored; a wall hit is ignored but makes the prince stand up and take off; otherwise it stands up and takes off,
     * fireball entities are removed, hits by another Young Adult Prince or a Spyro are refused; then the hit, 20 ticks of
     * immunity, and a living attacker becomes the target and is walked at - except that a tame prince hit by a player
     * answers {@code false} after the hit was applied (R18).
     *
     * <p>The hit goes through {@code super.hurt} in original units; {@code CombatEvents} scales it for the virtual health
     * (R4) and applies the 1.7.10 armor formula (R5).
     *
     * <p>PORT: {@code setTarget(Entity)} set 1.7.10's old-AI {@code entityToAttack}, which no AI of an
     * {@code isAIEnabled} mob read; it has no 1.21.1 counterpart and is dropped (Leon precedent).
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        Entity e = null;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (par1DamageSource.is(DamageTypes.CACTUS)) {
            return ret;
        }
        if (par1DamageSource.is(DamageTypes.IN_FIRE)) {
            return ret;
        }
        if (par1DamageSource.is(DamageTypes.ON_FIRE)) {
            return ret;
        }
        if (par1DamageSource.is(DamageTypes.LAVA)) {
            return ret;
        }
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            if (!this.level().isClientSide) {
                this.setSitting(false);
            }
            this.setActivity(1);
            return ret;
        }
        if (!this.level().isClientSide) {
            this.setSitting(false);
        }
        this.setActivity(1);
        e = par1DamageSource.getEntity();
        if (e != null && e instanceof BetterFireball) {
            e.discard();
            return ret;
        }
        if (e != null && e instanceof SmallFireball) {
            e.discard();
            return ret;
        }
        if (e != null && e instanceof ThePrinceAdult) {
            return false;
        }
        if (e != null && e instanceof Spyro) {
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        this.hurt_timer = 20;
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
     * {@code updateAITasks} (:362-387) with {@code updateAITick} (:445-450): the inherited step ({@code Animal}'s
     * {@code inLove} reset) unless ridden; then with 1/10 a standing, unridden prince that sees a target takes off; and
     * a tame, standing, unridden prince counts up to the King transformation while {@code FullPowerKingEnable} is set.
     */
    @Override
    protected void customServerAiStep() {
        LivingEntity e = null;
        if (this.level().isClientSide) {
            return;
        }
        if (!this.isVehicle()) {
            super.customServerAiStep();
        }
        if (!this.isSitting() && this.getActivity() == 0 && !this.isVehicle() && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.level().random.nextInt(10) == 1) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setActivity(1);
            } else {
                this.setAttacking(0);
            }
        }
        if (this.getActivity() == 0 && !this.isVehicle() && this.level().getDifficulty() != Difficulty.PEACEFUL && this.isTame()
                && OreSpawnConfig.TWEAKS.FullPowerKingEnable.get() != 0) {
            ++this.growcounter;
            if (this.growcounter > 288000) {
                Entity ent = null;
                ent = spawnCreature(this.level(), THE_KING_ID, this.getX(), this.getY(), this.getZ());
                if (ent != null) {
                    // PORT: (TheKing) ent threw for any other type; the id always names TheKing, so instanceof is the cast.
                    if (ent instanceof TheKing d) {
                        d.setFree();
                    }
                    this.discard();
                }
            }
        }
    }

    /**
     * {@code always_do} (:389-415), server only: heal 5 with 1/250, forget the target with 1/250; unless sitting, follow a
     * flying owner into the air, and with 1/50 (no target in sight, no rider) take off (1/15) or land (14/15).
     */
    public void always_do() {
        if (this.level().random.nextInt(250) == 1 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth()) {
            this.heal(5.0f);
        }
        if (this.level().random.nextInt(250) == 0) {
            this.setTarget(null);
        }
        if (this.isSitting()) {
            return;
        }
        this.owner_flying = 0;
        // PORT: (EntityPlayer) this.getOwner() (:402) - the owner of a 1.21.1 tamable is a LivingEntity; any other owner
        // reads as not flying (Leon precedent, R18 case 1).
        if (this.isTame() && this.getOwner() != null && !this.isVehicle() && !this.isSitting()) {
            if (this.getOwner() instanceof Player p && p.getAbilities().flying) {
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
     * {@code fly_with_rider} (:417-443): with 1/5, not on Peaceful, look for a target; strike within
     * {@code 10 + width/2}, otherwise shoot within 25 blocks out of water while the fire is lit; without one clear
     * {@code attacking}.
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
                if (this.distanceToSqr(e) < (10.0f + e.getBbWidth() / 2.0f) * (10.0f + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                } else if (this.distanceToSqr(e) < 625.0 && !this.isInWater() && this.getThePrinceAdultFire() != 0) {
                    this.shoot_something(e.getX(), e.getY(), e.getZ());
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:452-493): not Peaceful, alive, visible, no royal; then any {@code EntityMob}, Mothra and
     * the Kraken, and a wild Leon, Water Dragon or WTF?.
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
        // EntityMob is Monster (util.MyUtils reading).
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

    /** {@code findSomethingToAttack} (:495-512): the first suitable entity in {@code expand(32, 20, 32)}, nearest first. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(32.0, 20.0, 32.0)));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    // doesEntityNotTriggerPressurePlate (:514-516) returns the default false.

    /**
     * {@code getCanSpawnHere} (:518-520): never. 1.7.10 asked it for natural spawns and mob spawners; the prince has no
     * natural spawn, so the answer matters for spawners ({@code BaseSpawner} asks {@code checkSpawnRules}). Eggs,
     * commands and the Teen's growth do not ask, as in 1.7.10.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return false;
    }

    /**
     * {@code canSeeTarget} (:522-524): no block between a point 0.75 above the feet and the target. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded (Mothra/Leon precedent).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code setPositionAndRotation2} (:526-536): the vanilla interpolation ({@code super}) and a copy for the flight
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

    /** Rotation-only packets start from the pending target (W04 Elevator, W06 Ostrich, W09 Leon). */
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
     * {@code LivingEntity.aiStep}, which a flying client prince never reaches (W06 Ostrich precedent).
     */
    @Override
    public void lerpHeadTo(final float yaw, final int steps) {
        this.setYHeadRot(yaw);
    }

    // setVelocity (:538-541) only calls super.

    /**
     * {@code onUpdate} (:543-639): speed, the vanilla tick, {@code noClip} while flying; on the server the three heads
     * wander between 0 and 60; the immunity countdown, the wing beat every 31 ticks in flight, buoyancy in water; then
     * on the server a tame, standing prince more than 30 blocks from its owner takes off.
     *
     * <p>PORT: {@code noClip} is {@link Entity#noPhysics}. {@code "orespawn:MothraWings"} is {@link ModSounds#MOTHRA_WINGS}.
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
            if (this.wing_sound > 30) {
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
            if (e != null && this.distanceToSqr(e) > 900.0) {
                this.setActivity(1);
            }
        }
    }

    /**
     * {@code fly_without_rider} (:641-811): a waypoint at the feet when none exists; the owner more than 20 blocks off
     * resets the attack and stands the prince up; vertical damping by the waypoint height; a new waypoint with 1/300;
     * with 1/6 an attack run (strike within {@code 10 + width/2}, then break away 5-19 ticks; shoot within
     * {@code sqrt(600)} with 1/2) or a flight from danger below a quarter of the health while tame; a new waypoint
     * near the old one; lift over ground below the flight path; steer towards the waypoint.
     *
     * <p>{@code (int)} casts of coordinates are {@link Mth#floor} (R20); {@code worldObj.rand} is the level random,
     * {@code moveForward} is {@code zza}. The motion is kept in locals and written back before every call that could
     * change it from outside (a hit's knockback) and before {@code move}.
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
        Vec3 m = this.getDeltaMovement();
        double motionX = m.x;
        double motionY = m.y;
        double motionZ = m.z;
        final double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        int toofar = 0;
        if (this.currentFlightTarget == null) {
            do_new = 1;
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
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
        // posY is a local: the write at :789 did not move the bounding box, but :792 and :798 read it (class comment).
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
        if (this.flyaway > 0) {
            --this.flyaway;
        }
        if (toofar == 0 && this.flyaway == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL
                && this.level().random.nextInt(6) == 1) {
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
                    if (this.distanceToSqr(e) < (10.0f + e.getBbWidth() / 2.0f) * (10.0f + e.getBbWidth() / 2.0f)) {
                        this.setDeltaMovement(motionX, motionY, motionZ);
                        this.doHurtTarget(e);
                        m = this.getDeltaMovement();
                        motionX = m.x;
                        motionY = m.y;
                        motionZ = m.z;
                        this.flyaway = 5 + this.level().random.nextInt(15);
                        do_new = 1;
                    } else if (this.distanceToSqr(e) < 600.0 && !this.isInWater() && this.getThePrinceAdultFire() != 0
                            && this.level().random.nextInt(2) == 1) {
                        this.shoot_something(e.getX(), e.getY(), e.getZ());
                    }
                }
            } else {
                this.target_in_sight = false;
                this.setAttacking(this.flyaway = 0);
            }
        }
        if (InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(posY), Mth.floor(this.getZ())) < 2.1f) {
            do_new = 1;
        }
        if ((do_new != 0 && !this.target_in_sight) || (do_new != 0 && this.flyaway != 0)) {
            // for (Block bid = Blocks.stone; bid != Blocks.air && keep_trying != 0; --keep_trying) (:741)
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                int gox = Mth.floor(this.getX());
                int goy = Mth.floor(posY);
                int goz = Mth.floor(this.getZ());
                if (has_owner == 1) {
                    gox = Mth.floor(ox);
                    goy = Mth.floor(oy);
                    goz = Mth.floor(oz);
                    if (this.owner_flying == 0) {
                        zdir = this.level().random.nextInt(16) + 8;
                        xdir = this.level().random.nextInt(16) + 8;
                    } else {
                        zdir = this.level().random.nextInt(12);
                        xdir = this.level().random.nextInt(12);
                    }
                } else {
                    zdir = this.level().random.nextInt(15) + 20;
                    xdir = this.level().random.nextInt(15) + 20;
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
                // getDistanceSqToEntity (:798) read the posY written at :789.
                final double ddx = this.getX() - e.getX();
                final double ddy = posY - e.getY();
                final double ddz = this.getZ() - e.getZ();
                if (ddx * ddx + ddy * ddy + ddz * ddz > 64.0) {
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
        this.setYRot(this.getYRot() + var5 / 4.0f);
        // moveEntity (:810) with noClip set: 1.21.1 move with noPhysics offsets the position the same way.
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
    }

    /**
     * {@code onLivingUpdate} (:813-1104). On the ground the vanilla update; in flight nothing of {@code super} - no AI,
     * despawn roll, gravity or collisions. The client interpolates a flying prince itself; the server runs the ridden
     * flight or {@link #fly_without_rider()}, and {@link #always_do()} last, in both modes.
     *
     * <p>{@code isDead} is {@code isRemoved()}.
     */
    @Override
    public void aiStep() {
        // :816-817: never read, but each draws from the entity random every tick, on both sides.
        @SuppressWarnings("unused")
        final double d6 = this.random.nextFloat() * 2.0f - 1.0f;
        @SuppressWarnings("unused")
        final double d7 = (this.random.nextInt(2) * 2 - 1) * 0.7;
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
            if (this.getActivity() != 0) {
                if (this.fireballticker > 0) {
                    --this.fireballticker;
                }
                if (rider != null) {
                    this.riddenUpdate(rider);
                } else {
                    this.fly_without_rider();
                }
            }
            this.always_do();
        }
    }

    /**
     * The server branch of {@code onLivingUpdate} with a rider (:857-1097): clamp the motion, hover over ground 1.25
     * below or sink, lift over obstacles ahead, follow the rider's yaw, climb on the fly-up key, accelerate or brake with
     * the forward key, fire the three heads in turn on the strafe key, move, damp, push the entities around, then the
     * prince's own attack roll.
     */
    private void riddenUpdate(final Entity rider) {
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 1.05;
        double gh = 1.25;
        double pi = 3.1415926545;
        double deltav = 0.0;
        int dist = 2;
        // PORT: (EntityPlayer) this.riddenByEntity (:858) threw for any other rider; 1.21.1's /ride can seat a mob.
        // A non-player rider reads as no input (R18 case 1, W04 Elevator, W06 Ostrich, W09 Leon).
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
        gh = 1.25;
        // posY is a local: the writes at :876 and :895 did not move the bounding box, but the shots read them.
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
        // relative_g = abs(relative_g) * velocity, reset above 50 (:921-924): never read again.
        this.setXRot(2.0f * (float) velocity);
        this.setRot(this.getYRot(), this.getXRot());
        double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        // rr = atan2(rider.motionZ, rider.motionX) (:928) and rt = 0 (:931) are never read.
        final double rhm = Math.atan2(motionZ, motionX);
        final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
        pi = 3.1415926545;
        deltav = 0.0;
        final float im = pp != null ? pp.zza : 0.0f; // moveForward
        // OreSpawnMain.flyup_keystate != 0 (:935): per rider (R15).
        if (RiderKeys.isFlyUp(rider)) {
            motionY += 0.045;
            motionY += velocity * 0.066;
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
                deltav = 0.035;
                if (max_speed > 1.0) {
                    deltav += 0.07;
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
        // pp.moveStrafing (:1000) is xxa; a non-player rider never strafes.
        final float strafe = pp != null ? pp.xxa : 0.0f;
        if (this.fireballticker == 0 && pp != null && (strafe < -0.001f || strafe > 0.001f)) {
            double yoff = 9.5;
            final double xzoff = 14.5;
            ++this.which_attack;
            if (this.which_attack > 2) {
                this.which_attack = 0;
            }
            if (this.which_attack == 0) {
                yoff -= this.getHead1Ext() * 0.08f;
                double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot() - 10.0f));
                double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot() - 10.0f));
                final BetterFireball bf = new BetterFireball(this.level(), this, 0.0, 0.0, 0.0);
                bf.setBig();
                bf.setNotMe();
                bf.setPos(cx, posY + yoff, cz);
                cx = Math.cos(Math.toRadians(pp.getYHeadRot() + 90.0f));
                cz = Math.sin(Math.toRadians(pp.getYHeadRot() + 90.0f));
                final double cy = -Math.sin(Math.toRadians(pp.getXRot()));
                final double d12 = (float) Math.sqrt(cx * cx + cy * cy + cz * cz);
                bf.setAcceleration(cx / d12 * 0.1, cy / d12 * 0.1, cz / d12 * 0.1);
                bf.setDeltaMovement(motionX, motionY, motionZ);
                bf.setPos(bf.getX() - motionX * 3.0, bf.getY() - motionY * 3.0, bf.getZ() - motionZ * 3.0);
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(bf);
            }
            if (this.which_attack == 1) {
                yoff -= this.getHead3Ext() * 0.08f;
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
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.FIREWORK_ROCKET_LAUNCH, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(var2);
            }
            if (this.which_attack == 2) {
                yoff -= this.getHead2Ext() * 0.08f;
                final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
                final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
                final ThunderBolt lb = new ThunderBolt(this.level(), pp);
                lb.moveTo(cx, posY + yoff, cz, pp.getYRot() + 90.0f, pp.getXRot());
                final Vec3 lm = lb.getDeltaMovement();
                lb.setDeltaMovement(lm.x * 3.0, lm.y * 3.0, lm.z * 3.0);
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(lb);
            }
            this.fireballticker = 8;
        }
        // moveEntity (:1078) with noClip set: 1.21.1 move with noPhysics offsets the position the same way.
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
        final Vec3 moved = this.getDeltaMovement();
        this.setDeltaMovement(moved.x * 0.985, moved.y * 0.94, moved.z * 0.985);
        if (!this.level().isClientSide) {
            final List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(6.25, 10.0, 6.25));
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
     * 1.7.10 {@code Entity.applyEntityCollision} called as {@code self.applyEntityCollision(other)}: unless one rides
     * the other, both are pushed apart by 0.05 along the dominant axis (W09 Leon, written out the same way).
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
     * {@code updateRiderPosition} (:1106-1111): 4.65 along the look direction, {@code posY + 9.25 + rider.getYOffset()}.
     * A server-side 1.7.10 player had {@code getYOffset() = -0.5}; 1.21.1 positions passengers by their feet (W04
     * Elevator, W06 Ostrich, W09 Leon). A non-player {@code /ride} passenger gets 0.
     */
    @Override
    protected void positionRider(final Entity passenger, final Entity.MoveFunction callback) {
        final float f = 4.65f;
        final double yOffset = passenger instanceof Player ? -0.5 : 0.0;
        callback.accept(passenger,
                this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                this.getY() + this.getMountedYOffset() + yOffset,
                this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())));
    }

    /**
     * {@code playTameEffect} (:1113-1124): 20 hearts or smoke puffs in a 5-block cloud. Server calls draw from the random
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
     * {@code interact} (:1126-1274), in the original order, on both sides; no {@code super.interact}. A {@code true}
     * return is {@code sidedSuccess}. {@code func_152115_b} sets the owner UUID, {@code func_152114_e} is "is owner"
     * (catalogue 5.10). The 1.7.10 order around it (leash, then this, then the held name tag) is restored by
     * {@link ThePrinceAdultInteractEvents}. Health comparisons and heals are in original units (R4).
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :1128-1131 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (!var2.isEmpty() && var2.is(Items.DIAMOND_BLOCK) && par1EntityPlayer.distanceToSqr(this) < 36.0) {
            if (!isRemote) {
                this.heal(this.mygetMaxHealth() - VirtualHealth.originalHealth(this));
                this.growcounter = 288000;
            }
            consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame()) {
            if (!this.isOwnedBy(par1EntityPlayer)) {
                return InteractionResult.PASS;
            }
            if (var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 36.0) {
                if (!isRemote) {
                    // PORT: 1.7.10 mountEntity replaced a current rider; 1.21.1 startRiding refuses while the seat is
                    // taken (Entity.canAddPassenger) - W06 Ostrich, W09 Leon.
                    par1EntityPlayer.startRiding(this);
                    this.setActivity(1);
                    this.setSitting(false);
                }
                return success;
            }
            if (!var2.isEmpty() && var2.is(Items.BEEF) && par1EntityPlayer.distanceToSqr(this) < 36.0) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.mygetMaxHealth() > VirtualHealth.originalHealth(this)) {
                    this.heal(this.mygetMaxHealth() - VirtualHealth.originalHealth(this));
                }
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            // var2.getItem() instanceof ItemFood (:1175): an item with food properties.
            final FoodProperties food = var2.isEmpty() ? null : var2.getFoodProperties(this);
            if (!var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 36.0 && food != null) {
                if (!isRemote) {
                    if (this.mygetMaxHealth() > VirtualHealth.originalHealth(this)) {
                        // func_150905_g (getHealAmount) is FoodProperties.nutrition.
                        this.heal((float) (food.nutrition() * 10));
                    }
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (!var2.isEmpty() && var2.is(Items.ICE) && par1EntityPlayer.distanceToSqr(this) < 36.0) {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    this.setThePrinceAdultFire(0);
                    par1EntityPlayer.sendSystemMessage(Component.literal("Fireballs extinguished."));
                }
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (!var2.isEmpty() && var2.is(Items.FLINT_AND_STEEL) && par1EntityPlayer.distanceToSqr(this) < 36.0) {
                if (!isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                    this.setThePrinceAdultFire(1);
                    par1EntityPlayer.sendSystemMessage(Component.literal("Fireballs lit!"));
                }
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (!var2.isEmpty() && var2.is(Items.DIAMOND) && par1EntityPlayer.distanceToSqr(this) < 36.0 && !isRemote) {
                Entity ent = null;
                ent = spawnCreature(this.level(), THE_YOUNG_PRINCE_ID, this.getX(), this.getY(), this.getZ());
                if (ent != null) {
                    // PORT: (ThePrinceTeen) ent - the Teen is written by another porter of this wave; its setTamed and
                    // func_152115_b are the EntityTameable ones, i.e. TamableAnimal.setTame/setOwnerUUID. setTamed
                    // had no advancement side effect in 1.7.10, hence applyTamingSideEffects false.
                    if (ent instanceof TamableAnimal d && this.isTame()) {
                        d.setTame(true, false);
                        d.setOwnerUUID(par1EntityPlayer.getUUID());
                    }
                    this.discard();
                }
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 36.0
                    && this.isOwnedBy(par1EntityPlayer)) {
                this.setCustomName(var2.getHoverName());
                consumeOne(par1EntityPlayer, var2);
                return success;
            }
            if (!var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 36.0) {
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

    /** {@code isWheat} (:1276-1278): beef; overrides nothing in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.BEEF);
    }

    /**
     * 1.7.10 {@code EntityAnimal.isBreedingItem} default (wheat): the prince does not override it. Only
     * {@code Animal.mobInteract} and a breed goal ask it, and the prince has neither.
     */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code getAttacking} (:1280-1282). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:1284-1289): server only. */
    public void setAttacking(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getActivity} (:1291-1293). */
    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    /** {@code setActivity} (:1295-1300): server only. */
    public void setActivity(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code getThePrinceAdultFire} (:1302-1304). */
    public int getThePrinceAdultFire() {
        return this.entityData.get(DATA_FIRE);
    }

    /** {@code setThePrinceAdultFire} (:1306-1311): server only. */
    public void setThePrinceAdultFire(final int par1) {
        if (this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_FIRE, par1);
    }

    /**
     * {@code spawnCreature} (:1313-1322): create by {@code EntityList} name, place with a random yaw, add to the world,
     * play the living sound. No {@code finalizeSpawn} - the original had none (BlockExtremeTorch precedent).
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final ResourceLocation par1, final double par2,
                                       final double par4, final double par6) {
        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(par1).orElse(null);
        if (type == null) {
            return null;
        }
        final Entity var8 = type.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }

    /** {@code createChild} (:1324-1326). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /**
     * {@code canDespawn} (:1328-1330): not persistent, no rider, wild ({@code isPersistenceRequired} is checked by
     * {@code Mob.checkDespawn} before this is asked). {@code Animal} would answer {@code false}.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.isVehicle() && !this.isTame();
    }

    /**
     * {@code despawnEntity} ran inside {@code EntityLiving.updateAITasks}, which a flying prince skips.
     *
     * <p>PORT (R18 case 3): 1.21.1 runs the despawn check from the level for every mob on every tick; in flight it is
     * skipped as in 1.7.10 (W08 PitchBlack, W09 Leon precedent).
     */
    @Override
    public void checkDespawn() {
        if (this.getActivity() != 0) {
            return;
        }
        super.checkDespawn();
    }

    /** {@code writeEntityToNBT} (:1332-1338). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("ThePrinceAdultAttacking", this.getAttacking());
        par1NBTTagCompound.putInt("ThePrinceAdultActivity", this.getActivity());
        par1NBTTagCompound.putInt("ThePrinceAdultFire", this.getThePrinceAdultFire());
        par1NBTTagCompound.putInt("ThePrinceAdultGrow", this.growcounter);
    }

    /** {@code readEntityFromNBT} (:1340-1346). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setAttacking(par1NBTTagCompound.getInt("ThePrinceAdultAttacking"));
        this.setActivity(par1NBTTagCompound.getInt("ThePrinceAdultActivity"));
        this.setThePrinceAdultFire(par1NBTTagCompound.getInt("ThePrinceAdultFire"));
        this.growcounter = par1NBTTagCompound.getInt("ThePrinceAdultGrow");
    }

    /**
     * {@code shoot_something} (:1348-1390): one of three heads at random - fireball, thunder bolt or ice ball - and only
     * when the target lies within 0.5 rad of the facing.
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

    /** {@code firecanon} (:1392-1407): a big fireball 6 ahead and 3.5 up, spread 5/3/5. */
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
        ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        this.level().addFreshEntity(bf);
    }

    /**
     * {@code firecanonl} (:1409-1436): a thunder bolt 6 ahead and 3.5 up, thrown at the target at 1.4 with inaccuracy 4,
     * then tripled. The spread values {@code r1..r3} are drawn and never used, as in the original.
     */
    private void firecanonl(final double x, final double y, final double z) {
        final double yoff = 3.5;
        final double xzoff = 6.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        @SuppressWarnings("unused")
        final float r1 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        @SuppressWarnings("unused")
        final float r2 = 3.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        @SuppressWarnings("unused")
        final float r3 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
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

    /**
     * {@code firecanoni} (:1438-1466): an ice-making ice ball 6 ahead and 3.5 up, thrown at the target at 1.4 with
     * inaccuracy 4, then tripled. The spread values {@code r1..r3} are drawn and never used, as in the original.
     */
    private void firecanoni(final double x, final double y, final double z) {
        final double yoff = 3.5;
        final double xzoff = 6.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
        @SuppressWarnings("unused")
        final float r1 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        @SuppressWarnings("unused")
        final float r2 = 3.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
        @SuppressWarnings("unused")
        final float r3 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
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
        this.setOrderedToSit(sitting);
        this.setInSittingPose(sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * {@code onDeath}: no override in the original (ThePrinceAdult.java:19, {@code extends EntityTameable}), and 1.7.10
     * {@code EntityTameable} did not override it either, so the owner got no chat message.
     *
     * <p>PORT: R22 (owner death message of tame OreSpawn animals 1:1 off) - {@code TamableAnimal.die} in 1.21.1 sends
     * the death message to the owner. Java cannot skip one super level, so this is {@code LivingEntity.die} line by line
     * (NeoForge 21.1 sources) without the owner message (EntityCannonFodder, Leon precedent).
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
