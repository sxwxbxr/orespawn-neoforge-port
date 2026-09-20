package com.swbr.orespawn.entity.cephadrome;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
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
 * Port of {@code danger.orespawn.Cephadrome} (Cephadrome.java:21-969), id {@code cephadrome} ("Cephadrome",
 * OreSpawnMain.java:3567, tracking 128/1/true). A flying hammerhead dragon that anyone can ride without taming: an
 * empty hand mounts it, the rider steers with the look direction and the forward key, the fly-up key climbs. It hunts
 * monsters, Mothra, the Ender Dragon and untamed Leons, Gamma Metroids and Water Dragons; players only after a hit
 * below 90 % health, from a Cephadrome spawner, or once after an unfed click (verhalten/entity-05.md).
 *
 * <p>Values (all literals in the class, no config stats): health 300, speed 0.25 re-set every tick (:57, :654),
 * attack 70 (x1.5 against the Kraken), armor 16, {@code experienceValue} 200 - an {@code EntityCreature}, not an
 * {@code EntityAnimal}, so the value is really given (W04 XP lesson checked) - {@code fireResistance} 100. State:
 * DataWatcher 20 {@code attacking}, 21 {@code activity} (0 ground AI, 1 ridden flight), both {@code Integer}; NBT
 * {@code CephaWasFed}, {@code CephaAttacking}, {@code CephaActivity}, {@code CephaHitByPlayer}, {@code CephaBadMood}.
 *
 * <h2>Where the flight runs</h2>
 * The original moved a ridden Cephadrome on the server only, inside {@code onLivingUpdate} (:708-862), without calling
 * {@code super.onLivingUpdate}, and let the client interpolate boat-style (:636-646, :693-707).
 *
 * <p>PORT: the wave plan named {@code tickRidden}/{@code getRiddenInput}. Both run inside {@code LivingEntity.travel},
 * which 1.21.1 calls on the side {@code isControlledByLocalInstance()} names - the rider's <b>client</b> for a player
 * rider - while the fly-up key state of R15 lives on the server ({@link RiderKeys}) and the entity pushing and the AI
 * call at the end of the flight are server work. So the port keeps the original split like the W04 {@code Elevator}
 * and the W06 {@code Ostrich}: {@link #aiStep()} is the whole {@code onLivingUpdate} and calls {@code super.aiStep()}
 * only where the original called {@code super.onLivingUpdate()}; {@code Mob.getControllingPassenger} hands control only
 * to a {@code Mob}, so no client predicts the flight. The rider input is read on the server from {@code Player.zza}:
 * checked in the 1.21.1 sources (catalogue 6.3), {@code LocalPlayer.tick} sends {@code ServerboundPlayerInputPacket}
 * with {@code xxa}/{@code zza} every tick while the player is a passenger (LocalPlayer.java:238-240), and
 * {@code ServerPlayer.setPlayerInput} writes them for a passenger (ServerPlayer.java:1247-1259).
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>{@code onUpdate} sets {@code wasfed = 1} every tick while {@code PlayNicely == 0} (:665-667): with the default
 *       config feeding never matters, and with {@code PlayNicely} set the unfed click marks the player as a target the
 *       target search never looks for.</li>
 *   <li>{@code hit_by_player} is permanent and the target filter does not exclude the rider, so a ridden Cephadrome can
 *       attack its own rider.</li>
 *   <li>After a hit every damage is ignored for 25 ticks, including stronger hits ({@code hurt_timer}, :437-442).</li>
 *   <li>For up to 30 ticks after mounting the ground AI keeps running, and for up to 30 ticks after dismounting the
 *       flight branch runs without a rider - no gravity, only the damping (:469-483, :708-862).</li>
 *   <li>The ridden flight calls {@code updateAITasks} at the end of {@code onLivingUpdate} on <b>both</b> sides
 *       (:863-865); the client copy decrements its timers, may heal its copy and may "attack" (a client hit does no
 *       damage, the knockback is corrected by the server). Kept.</li>
 *   <li>{@code this.posY += 0.1} (:728) and {@code this.posY += obstruction_factor * 0.09} (:747) are overwritten by
 *       {@code moveEntity}, which recomputes {@code posY} from the bounding box (W04 Elevator, W06 Ostrich). The first
 *       one is still read by the obstruction scan in the same tick, so the scan uses the raised height; see
 *       {@link #riddenUpdate}.</li>
 * </ul>
 *
 * <p>Not carried over: {@code getTrackingRange} 128, {@code getUpdateFrequency} 10, {@code sendsVelocityUpdates}
 * (:96-106) override nothing (the registration is 128/1/true); {@code isAIEnabled} (:165-167) is the 1.21.1 default;
 * {@code getDropItem} beef (:200-202) is dead because {@code dropFewItems} is overridden; {@code setVelocity} (:648-651)
 * only calls {@code super}; {@code canTriggerWalking} returns the default {@code true} (:114-116), so no
 * {@code NoStepTrigger} (R20); {@code boatYawHead} (:645) is written and never read.
 */
public class Cephadrome extends PathfinderMob implements AttackableNonMob, LegacyArmor {

    /** DataWatcher 20 (:120, :926-935): {@code attacking}, read by {@code CephadromeModel} for wings, tail and mouth. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.INT);
    /** DataWatcher 21 (:121, :937-946): {@code activity}, 0 ground AI, 1 ridden flight. */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY =
            SynchedEntityData.defineId(Cephadrome.class, EntityDataSerializers.INT);

    /**
     * {@code instanceof Kraken} (:422, :509). PORT: the Kraken is ported by W10 under the registry id
     * {@code the_kraken} (manifest, W08 hand-off); until the class exists the test compares that id, which is the same
     * set of entities (Kraken has no subclass in 20.2). The integrator of W10 may switch to {@code instanceof}.
     */
    private static final ResourceLocation KRAKEN = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_kraken");
    /**
     * {@code instanceof Leon} (:544). PORT: written in parallel by w09-leon; compared by registry id
     * ({@code leonopteryx}, manifest) so this package compiles on its own. No subclass in 20.2.
     */
    private static final ResourceLocation LEON = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "leonopteryx");
    /** {@code instanceof GammaMetroid} (:548), registry id {@code wtf} (manifest); same PORT as {@link #LEON}. */
    private static final ResourceLocation GAMMA_METROID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "wtf");
    /** {@code instanceof WaterDragon} (:552), registry id {@code water_dragon} (manifest); same PORT as {@link #LEON}. */
    private static final ResourceLocation WATER_DRAGON = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "water_dragon");

    // Client-side interpolation state (:23-29), filled by lerpTo.
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    @SuppressWarnings("unused")
    private double boatYawHead;
    @SuppressWarnings("unused")
    private int damage_counter;
    private int updateit;
    @SuppressWarnings("unused")
    private int color;
    @SuppressWarnings("unused")
    private int playing;
    private GenericTargetSorter TargetSorter;
    private RenderInfo renderdata;
    private int hurt_timer;
    private int wasfed;
    private int shouldattack;
    private int wing_sound;
    private int hit_by_player;
    private int badmood;
    private float moveSpeed;

    /**
     * {@code Cephadrome(World)} (:44-71) with {@code entityInit}'s {@link RenderInfo} reset (:124-134).
     * {@code setSize(2.5f, 2.25f)} (:58) is the entity type's size (R9); {@code setActivity(0)}/{@code setAttacking(0)}
     * (:122-123) are the synced defaults. Goals in the original priorities.
     */
    public Cephadrome(final EntityType<? extends Cephadrome> type, final Level par1World) {
        super(type, par1World);
        this.damage_counter = 100;
        this.updateit = 1;
        this.color = 1;
        this.playing = 0;
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.hurt_timer = 0;
        this.shouldattack = 0;
        this.wing_sound = 0;
        this.hit_by_player = 0;
        this.badmood = 0;
        this.moveSpeed = 0.25f;
        // PORT: getNavigator().setAvoidsWater(true) (:59) - water malus -1 (Hammerhead, W07).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 200;
        // PORT: EntityAISwimming (:63) -> FloatGoal, as in every earlier wave (W07 "Offen").
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 9.0f));
        this.goalSelector.addGoal(3, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) (:67) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
    }

    /**
     * {@code applyEntityAttributes} (:84-90): health {@code mygetMaxHealth()} 300, speed 0.25, attack 70; armor from
     * {@code getTotalArmorValue} 16 (:156-158, also {@link #getLegacyArmorValue()} for R5).
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, W06 Ostrich); it
     * matters for the ridden {@code moveEntity}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, 70.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code entityInit} (:118-135). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_ACTIVITY, 0);
    }

    /** {@code shouldRiderSit} (:92-94). */
    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    /** {@code fireResistance = 100} (:61). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code fall} (:108-109): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:111-112): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code mygetMaxHealth} (:137-139). */
    public int mygetMaxHealth() {
        return 300;
    }

    /** {@code getRenderInfo} (:141-143): the model's note pad (plain data, no client imports). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:145-154): copies the fields. */
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

    /** {@code getTotalArmorValue} (:156-158) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 16;
    }

    /** {@code jump} (:160-163): {@code super} assigns the jump speed, then 0.1 is added on top. */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.1, m.z);
    }

    /** {@code getLivingSound} (:169-174): wing beats with 1 in 6 (entity random) unless ridden. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getActivity() != 1 && this.random.nextInt(6) == 1) {
            return ModSounds.MOTHRA_WINGS.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:176-178). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:180-182). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:184-186). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:188-190). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code canBePushed} (:192-194). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code getMountedYOffset} (:196-198); overrides nothing in 1.21.1, read by {@link #positionRider}. */
    public double getMountedYOffset() {
        return 2.5;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:214-397), every item spread ±4 at y+1 on {@code OreSpawnRand} ({@code dropItemRand},
     * :204-212): 4-9 uranium nuggets, 4-9 titanium nuggets, then 1-5 rolls of {@code rand(20)} on the world random -
     * 0 ruby sword, 1 diamond, 2 thunder staff, 3 enchanted ruby sword, 4-7 enchanted ruby shovel/pickaxe/axe/hoe, 8-11
     * enchanted ruby armour, 12-17 ruby, 18-19 nothing.
     *
     * <p>The enchantment blocks are word for word the CaterKiller's (W08), so the shared W07 tables
     * ({@link ArthropodSupport#enchantSword} and siblings, Sharpness twice summed - PORT there) are used. PORT: the stack
     * is enchanted before the item entity is added, so the client receives it enchanted; the original added the entity
     * first and enchanted the returned stack. Both random streams keep their order ({@code OreSpawnRand} for the
     * offsets, the world random for the enchantments).
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        final Level world = this.level();
        for (int i = 4 + world.random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.URANIUM_NUGGET.get(), 1), 5);
        }
        for (int i = 4 + world.random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.TITANIUM_NUGGET.get(), 1), 5);
        }
        for (int i = 1 + world.random.nextInt(5), var4 = 0; var4 < i; ++var4) {
            final int var5 = world.random.nextInt(20);
            ItemStack is = null;
            switch (var5) {
                case 0:
                    is = new ItemStack(ModItems.RUBY_SWORD.get(), 1);
                    break;
                case 1:
                    is = new ItemStack(Items.DIAMOND, 1);
                    break;
                case 2:
                    is = new ItemStack(ModItems.THUNDER_STAFF.get(), 1);
                    break;
                case 3:
                    is = new ItemStack(ModItems.RUBY_SWORD.get(), 1);
                    ArthropodSupport.enchantSword(is, world);
                    break;
                case 4:
                    is = new ItemStack(ModItems.RUBY_SHOVEL.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                    break;
                case 5:
                    is = new ItemStack(ModItems.RUBY_PICKAXE.get(), 1);
                    ArthropodSupport.enchantPickaxe(is, world);
                    break;
                case 6:
                    is = new ItemStack(ModItems.RUBY_AXE.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                    break;
                case 7:
                    is = new ItemStack(ModItems.RUBY_HOE.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                    break;
                case 8:
                    is = new ItemStack(ModItems.RUBY_HELMET.get(), 1);
                    ArthropodSupport.enchantHelmet(is, world);
                    break;
                case 9:
                    is = new ItemStack(ModItems.RUBY_CHEST.get(), 1);
                    ArthropodSupport.enchantBody(is, world);
                    break;
                case 10:
                    is = new ItemStack(ModItems.RUBY_LEGGINGS.get(), 1);
                    ArthropodSupport.enchantBody(is, world);
                    break;
                case 11:
                    is = new ItemStack(ModItems.RUBY_BOOTS.get(), 1);
                    ArthropodSupport.enchantBoots(is, world);
                    break;
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                    is = new ItemStack(ModItems.RUBY.get(), 1);
                    break;
                default:
                    break;
            }
            if (is != null) {
                ArthropodSupport.dropItemRand(this, is, 5);
            }
        }
    }

    /** {@code getCephadromeHealth} (:399-401). */
    public int getCephadromeHealth() {
        return (int) this.getHealth();
    }

    /**
     * {@code attackEntityAsMob} (:403-433). Against the Ender Dragon 70 explosion damage on the head (1 in 6, world
     * random) or the body, always a hit. Against any other living entity mob damage 70 (105 against the Kraken) and,
     * <b>whether or not the hit landed</b>, a push of 2.5 away and 0.35 up (0.7 for a player or a dead entity). No
     * vanilla {@code super}: the weapon, fire aspect and the attack attribute are not used.
     *
     * <p>PORT: {@code DamageSource.setExplosionSource(null).setExplosion()} is {@code damageSources().explosion(null,
     * null)}; {@code EntityDragon.dragonPartBody} is private in 1.21.1 and reached through {@code getSubEntities()}
     * (PitchBlack precedent, W08). {@code addVelocity} is {@code push} with {@code hurtMarked}, so a server player
     * receives the impulse (MonsterSupport precedent, W07).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 2.5;
        double inair = 0.35;
        float iskraken = 1.0f;
        boolean ret = false;
        if (par1Entity != null && par1Entity instanceof EnderDragon dr) {
            DamageSource var21 = null;
            var21 = this.damageSources().explosion(null, null);
            if (this.level().random.nextInt(6) == 1) {
                dr.hurt(dr.head, var21, 70.0f);
            } else {
                dr.hurt(dragonBody(dr), var21, 70.0f);
            }
            ret = true;
        } else if (par1Entity != null && par1Entity instanceof LivingEntity) {
            if (isType(par1Entity, KRAKEN)) {
                iskraken = 1.5f;
            }
            ret = par1Entity.hurt(this.damageSources().mobAttack(this), iskraken * 70.0f);
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            par1Entity.hurtMarked = true;
        }
        return ret;
    }

    /** {@code dragonPartBody}: the part named "body" (index 2 of {@code getSubEntities()}), as in PitchBlack (W08). */
    private static EnderDragonPart dragonBody(final EnderDragon dr) {
        for (final EnderDragonPart part : dr.getSubEntities()) {
            if ("body".equals(part.name)) {
                return part;
            }
        }
        return dr.getSubEntities()[2];
    }

    /**
     * {@code attackEntityFrom} (:435-455): nothing at all while {@code hurt_timer} runs (answer {@code false}); cactus is
     * ignored; any other hit applies, starts the 25-tick immunity, and a living source becomes the target and is walked
     * to at 1.2 - then the answer is {@code true} even if {@code super} refused the damage. A player's hit that leaves
     * less than 90 % health sets {@code hit_by_player} for good.
     *
     * <p>PORT: {@code setAttackTarget(e)} and the old-AI {@code EntityCreature.setTarget(e)} ({@code entityToAttack})
     * are both {@link Mob#setTarget} in 1.21.1.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
            this.hurt_timer = 25;
            final Entity e = par1DamageSource.getEntity();
            if (e != null && e instanceof LivingEntity living) {
                this.setTarget(living);
                this.getNavigation().moveTo(e, 1.2);
                ret = true;
            }
            if (e != null && e instanceof Player && this.getHealth() < this.getMaxHealth() * 9.0f / 10.0f) {
                this.hit_by_player = 1;
            }
        }
        return ret;
    }

    /** {@code getHorizontalDistanceSqToEntity} (:457-461). */
    public double getHorizontalDistanceSqToEntity(final Entity par1Entity) {
        final double d0 = this.getX() - par1Entity.getX();
        final double d2 = this.getZ() - par1Entity.getZ();
        return d0 * d0 + d2 * d2;
    }

    /**
     * The ground half of {@code updateAITasks}: 1.7.10 {@code EntityLiving} called the override from
     * {@code onLivingUpdate}, 1.21.1 calls this hook from {@code Mob.serverAiStep} after the goals and the navigation.
     * Reached only through {@code super.aiStep()}, i.e. at activity 0 (see {@link #aiStep()}).
     */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:463-517): count down {@code updateit} and {@code hurt_timer}; every 30 ticks the server
     * syncs activity from the rider; heal 2 with 1 in 100; at activity 0 the vanilla AI; then with 1 in 7 (not peaceful)
     * keep a living target or search one, walk to it on the ground at 1.7 (reach 6, else 10), face it, set
     * {@code attacking} and strike within {@code (reach + width/2)^2} - horizontally only for the Kraken. No target
     * clears {@code attacking}.
     *
     * <p>PORT: {@code super.updateAITasks()} (:487-489) - the senses, target and task selectors, navigation and move/look
     * helpers - is {@code Mob.serverAiStep}, which is final in 1.21.1 and has already run when this is reached through
     * {@link #customServerAiStep()}. At activity 0 the goals therefore tick <em>before</em> the two counters, the
     * activity sync and the heal roll of the same tick, not after them; at activity 1 they do not tick, as in the
     * original.
     */
    public void updateAITasks() {
        LivingEntity e = null;
        double maxdist = 10.0;
        if (this.isRemoved()) {
            return;
        }
        if (this.updateit > 0) {
            --this.updateit;
        }
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.updateit <= 0 && !this.level().isClientSide) {
            this.updateit = 30;
            if (this.getFirstPassenger() != null) {
                this.setActivity(1);
            } else {
                this.setActivity(0);
            }
        }
        if (this.level().random.nextInt(100) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(2.0f);
        }
        // if (this.getActivity() == 0) super.updateAITasks(); - see the PORT note above.
        if (this.level().random.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                if (this.getActivity() == 0) {
                    this.getNavigation().moveTo(e, 1.7);
                    maxdist = 6.0;
                }
                this.lookAt(e, 10.0f, 10.0f);
                this.setAttacking(1);
                if (this.distanceToSqr(e) < (maxdist + e.getBbWidth() / 2.0f) * (maxdist + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                } else if (isType(e, KRAKEN)
                        && this.getHorizontalDistanceSqToEntity(e) < (maxdist + e.getBbWidth() / 2.0f) * (maxdist + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                }
            } else if (this.getAttacking() != 0) {
                this.setAttacking(0);
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:519-577) in the original order: not peaceful, alive, visible, no Cephadrome; any
     * monster, Mothra and the Ender Dragon; untamed Leon, Gamma Metroid and Water Dragon; a non-creative player only with
     * {@code hit_by_player}, {@code badmood} or a pending {@code shouldattack}, which the test consumes.
     *
     * <p>PORT: {@code EntityMob} is {@link Monster} (MyUtils reading). Leon, Gamma Metroid and Water Dragon are compared
     * by registry id (see {@link #LEON}); the original cast them to {@code EntityTameable}, here a type that turns out
     * not to be a {@link TamableAnimal} counts as untamed.
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
        if (par1EntityLiving instanceof Cephadrome) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof Mothra) {
            return true;
        }
        if (isType(par1EntityLiving, LEON)) {
            return !isTamed(par1EntityLiving);
        }
        if (isType(par1EntityLiving, GAMMA_METROID)) {
            return !isTamed(par1EntityLiving);
        }
        if (isType(par1EntityLiving, WATER_DRAGON)) {
            return !isTamed(par1EntityLiving);
        }
        if (par1EntityLiving instanceof EnderDragon) {
            return true;
        }
        if (!(par1EntityLiving instanceof Player)) {
            return false;
        }
        final Player p = (Player) par1EntityLiving;
        if (p.getAbilities().instabuild) {
            return false;
        }
        if (this.hit_by_player != 0) {
            return true;
        }
        if (this.badmood != 0) {
            return true;
        }
        if (this.shouldattack > 0) {
            this.shouldattack = 0;
            return true;
        }
        return false;
    }

    /** {@code ((EntityTameable) e).isTamed()} for the three tameable dragons of the target list. */
    private static boolean isTamed(final LivingEntity e) {
        return e instanceof TamableAnimal t && t.isTame();
    }

    /** {@code instanceof} for a class of a parallel or later wave, by registry id. */
    private static boolean isType(final Entity e, final ResourceLocation id) {
        return EntityType.getKey(e.getType()).equals(id);
    }

    /** {@code findSomethingToAttack} (:579-596): nothing with {@code PlayNicely}; the first suitable in {@code expand(16, 20, 16)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 20.0, 16.0,
                e -> this.isSuitableTarget(e, false));
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:598-634). A "Cephadrome" spawner allows it; otherwise daytime, y at
     * least 50, air in x/z -2..1 on y+1..4, and no other Cephadrome within {@code expand(16, 6, 16)}.
     *
     * <p>PORT: the spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9); its side effect
     * {@code badmood = 1} is set in {@link #checkSpawnRules}, which spawners ask with that type (Rotator precedent, W08).
     * A natural spawn next to a Cephadrome spawner therefore needs the normal rules and stays in a good mood. The
     * {@code (int)} casts of the scans are block positions ({@code Mth.floor}, R20). Chunk-generation spawns skip the
     * predicate centrally (R23).
     */
    public static boolean checkCephadromeSpawnRules(final EntityType<Cephadrome> type, final ServerLevelAccessor level,
                                                    final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (!ArthropodSupport.isAllAir(level, pos, -2, 2, 1, 5)) {
            return false;
        }
        return level.getEntitiesOfClass(Cephadrome.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 6.0, 16.0)).isEmpty();
    }

    /**
     * The whole {@code getCanSpawnHere} is {@link #checkCephadromeSpawnRules}; a spawner asks with
     * {@link MobSpawnType#SPAWNER}, which is the original's spawner branch that set {@code badmood = 1} (:607-609).
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (reason == MobSpawnType.SPAWNER) {
            this.badmood = 1;
        }
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code setPositionAndRotation2} (:636-646), client only: the vanilla interpolation target ({@code super}) plus the
     * boat-style copy that {@link #aiStep()} applies while flying.
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
     * 1.7.10 {@code S19PacketEntityHeadLook} set {@code rotationYawHead} directly. 1.21.1 stores it for
     * {@code LivingEntity.aiStep} to lerp, which a flying client Cephadrome never reaches (see {@link #aiStep()});
     * setting it here keeps the 1.7.10 behaviour (W06 Ostrich precedent).
     */
    @Override
    public void lerpHeadTo(final float yaw, final int steps) {
        this.setYHeadRot(yaw);
    }

    /**
     * {@code onUpdate} (:653-668), both sides: rewrite the speed, the base update, a wing beat (0.5) every 23 ticks while
     * flying (played by the server), and {@code wasfed = 1} while {@code PlayNicely} is off.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.getActivity() == 1) {
            ++this.wing_sound;
            if (this.wing_sound > 22) {
                if (!this.level().isClientSide) {
                    ArthropodSupport.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 0.5f, 1.0f);
                }
                this.wing_sound = 0;
            }
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.wasfed = 1;
        }
    }

    /**
     * {@code onLivingUpdate} (:670-866). At activity 0 the vanilla update (whose AI step runs {@link #updateAITasks()}).
     * Otherwise, without {@code super}: the client interpolates boat-style with the rider's yaw, the server runs the
     * flight ({@link #riddenUpdate}); then, on both sides, {@link #updateAITasks()} if the activity is 1. The activity is
     * read afresh at every check, as the original read the DataWatcher: a server Cephadrome whose AI switched it to 1
     * in {@code super} flies and runs the AI a second time in the same tick.
     */
    @Override
    public void aiStep() {
        // :673-674: never read, but each draws from the entity random every tick, on both sides.
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
                --this.boatPosRotationIncrements;
            }
        } else if (this.getActivity() != 0) {
            this.riddenUpdate();
        }
        if (this.getActivity() == 1) {
            this.updateAITasks();
        }
    }

    /**
     * The server branch of {@code onLivingUpdate} at activity != 0 (:708-862): with a rider clamp the motion, hover over
     * ground 1.55 below, rise over obstacles ahead, follow the rider's yaw, climb on the fly-up key, accelerate or brake
     * with the forward key; with or without a rider move, damp, and push the entities around.
     */
    private void riddenUpdate() {
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 1.15;
        double gh = 1.0;
        @SuppressWarnings("unused")
        double rt = 0.0;
        double pi = 3.1415926545;
        double deltav = 0.0;
        int dist = 2;
        final Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        final Entity rider = this.getFirstPassenger();
        if (rider != null) {
            // PORT: (EntityPlayer) this.riddenByEntity (:710) threw for any other rider; 1.21.1's /ride can seat a mob.
            // A non-player rider reads as no forward input (R18 case 1, W04 Elevator, W06 Ostrich).
            final Player pp = rider instanceof Player player ? player : null;
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
            // The posY this tick's scans see: :728 raises the field before the obstruction scan reads it (:740), and
            // moveEntity discards it afterwards (see the class comment).
            double posY = this.getY();
            // :725 (int) posX, (int)((float) posY - (float) gh), (int) posZ -> Mth.floor (DECISIONS R20).
            BlockState bid = this.level().getBlockState(
                    new BlockPos(Mth.floor(this.getX()), Mth.floor((float) posY - (float) gh), Mth.floor(this.getZ())));
            if (!bid.isAir()) {
                motionY += 0.07;
                posY += 0.1;
            } else {
                motionY -= 0.018;
            }
            obstruction_factor = 0.0;
            dist = 2;
            dist += (int) (velocity * 6.0);
            for (int k = 1; k < dist; ++k) {
                for (int i = 1; i < dist * 2; ++i) {
                    final double dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                    final double dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                    // :740 (int)(posX + dx), (int) posY - k, (int)(posZ + dz) -> Mth.floor (DECISIONS R20).
                    bid = this.level().getBlockState(
                            new BlockPos(Mth.floor(this.getX() + dx), Mth.floor(posY) - k, Mth.floor(this.getZ() + dz)));
                    if (!bid.isAir()) {
                        obstruction_factor += 0.04;
                    }
                }
            }
            motionY += obstruction_factor * 0.09;
            // this.posY += obstruction_factor * 0.09 (:747): overwritten by moveEntity, see the class comment.
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
            if (velocity > 0.1) {
                d8 = 1.5 - velocity;
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
            // :773-776: computed and never read.
            relative_g = Math.abs(relative_g) * velocity;
            if (relative_g > 50.0) {
                relative_g = 0.0;
            }
            if (motionY > 0.0) {
                this.setXRot(360.0f - 2.0f * (float) velocity);
            } else {
                this.setXRot(2.0f * (float) velocity);
            }
            this.setRot(this.getYRot(), this.getXRot());
            double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
            // rr = atan2(rider.motionZ, rider.motionX) (:785) is never read.
            final double rhm = Math.atan2(motionZ, motionX);
            final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
            rt = 0.0;
            pi = 3.1415926545;
            deltav = 0.0;
            final double im = pp != null ? pp.zza : 0.0f; // moveForward
            // OreSpawnMain.flyup_keystate != 0 (:792): per rider (R15).
            if (RiderKeys.isFlyUp(rider)) {
                motionY += 0.04;
                motionY += velocity * 0.05;
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
            if (Math.abs(im) > 0.0010000000474974513) {
                if (im > 0.0) {
                    deltav = 0.03;
                    if (max_speed > 0.85) {
                        deltav += 0.05;
                    }
                } else {
                    max_speed = 0.35;
                    deltav = -0.03;
                }
                newvelocity += deltav;
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
        }
        // moveEntity (:844): 1.21.1's move zeroes the blocked motion components like 1.7.10's did (W04 Elevator).
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
        final Vec3 moved = this.getDeltaMovement();
        motionX = moved.x;
        motionY = moved.y;
        motionZ = moved.z;
        motionX *= 0.985;
        motionY *= 0.94;
        motionZ *= 0.985;
        this.setDeltaMovement(motionX, motionY, motionZ);
        // :848 !this.worldObj.isRemote: always true in this branch.
        final List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(2.25, 2.0, 2.25));
        if (list != null && !list.isEmpty()) {
            for (int l = 0; l < list.size(); ++l) {
                final Entity listEntity = list.get(l);
                if (listEntity != this.getFirstPassenger() && !listEntity.isRemoved() && listEntity.isPushable()) {
                    this.legacyApplyEntityCollision(listEntity);
                }
            }
        }
        final Entity current = this.getFirstPassenger();
        if (current != null && current.isRemoved()) {
            current.stopRiding();
        }
    }

    /**
     * {@code listEntity.applyEntityCollision(this)} (:854), 1.7.10 {@code Entity.applyEntityCollision}: unless this
     * Cephadrome rides the other entity, both are pushed 0.05 (scaled by closeness) apart - the other entity away from
     * the Cephadrome, <b>and the Cephadrome away from it</b>.
     *
     * <p>PORT: 1.21.1 {@code Entity.push(Entity)} only moves an entity that is pushable and carries no passenger, so it
     * would never move the Cephadrome ({@code canBePushed} false, usually ridden); the 1.7.10 formula is written out.
     * {@code entityCollisionReduction} was 0. A minecart kept its own override in 1.7.10 as in 1.21.1
     * ({@code AbstractMinecart.push(Entity)}), which is called for it instead.
     */
    private void legacyApplyEntityCollision(final Entity listEntity) {
        if (listEntity instanceof AbstractMinecart) {
            listEntity.push(this);
            return;
        }
        if (this.getFirstPassenger() != listEntity && this.getVehicle() != listEntity) {
            double d0 = this.getX() - listEntity.getX();
            double d1 = this.getZ() - listEntity.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= 0.009999999776482582) {
                d2 = Math.sqrt(d2);
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
                listEntity.push(-d0, 0.0, -d1);
                this.push(d0, 0.0, d1);
            }
        }
    }

    /**
     * {@code updateRiderPosition} (:868-873): 0.75 forward along the yaw, {@code posY + 2.5 + rider.getYOffset()}.
     * A server-side 1.7.10 player had {@code getYOffset() = -0.5}; 1.21.1 positions passengers by their feet (same
     * reading as the W04 Elevator and the W06 Ostrich). A non-player {@code /ride} passenger gets 0.
     */
    @Override
    protected void positionRider(final Entity passenger, final Entity.MoveFunction callback) {
        final float f = 0.75f;
        final double yOffset = passenger instanceof Player ? -0.5 : 0.0;
        callback.accept(passenger,
                this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                this.getY() + this.getMountedYOffset() + yOffset,
                this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())));
    }

    /**
     * {@code playTameEffect} (:875-886): 20 hearts or smoke puffs in a 5-block cloud. Called by {@link #mobInteract} on
     * both sides; the server call spawns nothing, as 1.7.10's {@code spawnParticle} did not.
     */
    protected void playTameEffect(final boolean par1) {
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
     * {@code interact} (:888-924): raw beef, chicken or porkchop within squared distance 25 heals fully (server), marks it
     * fed, cancels a pending {@code shouldattack}, shows hearts and takes one item - and answers {@code false}. A seat
     * taken by another player swallows the click. An empty hand within 25 on the server: an unfed Cephadrome walks to
     * the player at 1.2 and marks it for one attack (answer {@code false}); a fed one takes the player up and becomes
     * unfed.
     *
     * <p>PORT: 1.7.10 had one hand and read {@code inventory.getCurrentItem()}; the off hand passes. {@code true} is
     * {@code sidedSuccess}, {@code false} is {@code PASS} (Camarasaurus precedent). {@code mountEntity} is
     * {@code startRiding}, which refuses while the seat is taken instead of replacing the rider (W06 Ostrich); only a
     * non-player passenger reaches that case. The 1.7.10 order ahead of the held item (a name tag never renames a
     * Cephadrome) is restored by {@link CephadromeInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        // :889-893 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (!var2.isEmpty() && (var2.is(Items.BEEF) || var2.is(Items.CHICKEN) || var2.is(Items.PORKCHOP))
                && par1EntityPlayer.distanceToSqr(this) < 25.0) {
            if (!isRemote) {
                this.heal(this.mygetMaxHealth() - this.getHealth());
            }
            this.wasfed = 1;
            this.shouldattack = 0;
            this.playTameEffect(true);
            if (!par1EntityPlayer.getAbilities().instabuild) {
                var2.shrink(1);
            }
            return InteractionResult.PASS;
        }
        final Entity rider = this.getFirstPassenger();
        if (rider != null && rider instanceof Player && rider != par1EntityPlayer) {
            return InteractionResult.sidedSuccess(isRemote);
        }
        if (var2.isEmpty() && par1EntityPlayer.distanceToSqr(this) < 25.0 && !isRemote) {
            if (this.wasfed == 0) {
                this.getNavigation().moveTo(par1EntityPlayer, 1.2);
                this.shouldattack = 1;
                return InteractionResult.PASS;
            }
            par1EntityPlayer.startRiding(this);
            this.wasfed = 0;
        }
        return InteractionResult.sidedSuccess(isRemote);
    }

    /** {@code getAttacking} (:926-928). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:930-935): server only. */
    public void setAttacking(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getActivity} (:937-939). */
    public int getActivity() {
        return this.entityData.get(DATA_ACTIVITY);
    }

    /** {@code setActivity} (:941-946): server only. */
    public void setActivity(final int par1) {
        if (this.level() != null && this.level().isClientSide) {
            return;
        }
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code canDespawn} (:948-950): not persistent and no rider. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.isVehicle();
    }

    /** {@code writeEntityToNBT} (:952-959). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("CephaWasFed", this.wasfed);
        par1NBTTagCompound.putInt("CephaAttacking", this.getAttacking());
        par1NBTTagCompound.putInt("CephaActivity", this.getActivity());
        par1NBTTagCompound.putInt("CephaHitByPlayer", this.hit_by_player);
        par1NBTTagCompound.putInt("CephaBadMood", this.badmood);
    }

    /** {@code readEntityFromNBT} (:961-968). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.wasfed = par1NBTTagCompound.getInt("CephaWasFed");
        this.hit_by_player = par1NBTTagCompound.getInt("CephaHitByPlayer");
        this.badmood = par1NBTTagCompound.getInt("CephaBadMood");
        this.setAttacking(par1NBTTagCompound.getInt("CephaAttacking"));
        this.setActivity(par1NBTTagCompound.getInt("CephaActivity"));
    }
}
