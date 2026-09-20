package com.swbr.orespawn.entity.boss.king;

import com.swbr.orespawn.entity.boss.RoyalCeiling;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.util.Royalty;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.TheKing} ("The King", TheKing.java:20-1127, verhalten/entity-01.md): the three-headed
 * royal dragon, {@code EntityMob}, registered 128/1/false (OreSpawnMain.java:3949). He flies through blocks with the
 * hand-written steering of {@code updateAITasks}, stomps, breathes fireball, thunderbolt and ice volleys, keeps a
 * separate invisible {@link KingHead} 30 blocks in front of him and ends, after {@link #setFree()}, in the "Ultimate
 * King" cut-scene that spams exploding {@link PurplePower} orbs without a cap (R18).
 *
 * <p><b>Numbers (R3, R4, R5).</b> The melee base is the field initialiser 250, not {@code TheKing_attack} (350): the
 * 1.7.10 constructor body ran after {@code applyEntityAttributes} and overwrote {@code attdam}. The phases, the stomps
 * and the armor read the config stats exactly where the original did. Health is virtual: the attribute holds
 * {@code min(TheKing_health, 1024)}, {@link #getOriginalMaxHealth()} is {@code TheKing_health}, every comparison and
 * {@code heal} below is in original units ({@link VirtualHealth#originalHealth}); {@code CombatEvents} scales the
 * damage after the 750 cap of {@link #hurt} and applies the 1.7.10 armor formula with {@link #getTotalArmorValue()}.
 *
 * <p><b>Order of the AI step.</b> 1.7.10 {@code EntityLiving.updateAITasks} ran senses, tasks, navigator and the
 * move/look/jump helpers <em>before</em> the override's own code; the move helper zeroed {@code moveForward}, which the
 * override then set to 1. 1.21.1 runs the controls after {@link #customServerAiStep()}, so {@link LegacyMoveControl}
 * does nothing in its own slot and is ticked at the start of {@code customServerAiStep} instead - otherwise the
 * vanilla {@code WAIT} operation would zero the forward push every tick. The look and jump controls keep their 1.21.1
 * slot (PORT: they only turn the head a tick later).
 *
 * <p><b>Not the original, and why.</b> The {@code PlayNicely} watcher is defined as 0 and set by the server (R18 case
 * 4: a watcher defined with the client's own config value is never corrected when both happen to equal the default).
 * {@code (int)} casts of coordinates are {@link Mth#floor} (R20). {@code Blocks.leaves} in {@link #MyCanSee} is the
 * vanilla leaves category (R22). No vanilla boss bar: the original had none ({@code TheKing} is no
 * {@code IBossDisplayData}); its health bar is the Girlfriend overlay of W11, which reads
 * {@code getHealth() / getMaxHealth()} - the same ratio in both scales (R4).
 */
public class TheKing extends Monster implements LegacyArmor, VirtualHealth, Royalty {

    /** DataWatcher 20: {@code attacking}, wing, claw, leg, tail and head amplitude of the model; purple orb output. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    /** DataWatcher 21: the server's {@code PlayNicely}, render scale and hitbox. */
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY = SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);
    /** DataWatcher 22: {@code isEnd}, the client's firework sparks. */
    private static final EntityDataAccessor<Integer> DATA_IS_END = SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.INT);

    /** {@code 3.1415926545} of {@code updateAITasks} (:338) - one digit off pi, kept. */
    private static final double PI = 3.1415926545;

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    @Nullable
    private LivingEntity rt;
    private double attdam;
    private int hurt_timer;
    private int homex;
    private int homez;
    private int stream_count;
    private int stream_count_l;
    private int stream_count_i;
    private int ticker;
    private int player_hit_count;
    private int backoff_timer;
    private int guard_mode;
    private volatile int head_found;
    private int wing_sound;
    private int large_unknown_detected;
    private int isEnd;
    private int endCounter;

    /** {@code TheKing(World)} (:42-79) with {@code applyEntityAttributes} (:81-87). */
    public TheKing(final EntityType<? extends TheKing> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.rt = null;
        this.attdam = 250.0;
        this.hurt_timer = 0;
        this.homex = 0;
        this.homez = 0;
        this.stream_count = 0;
        this.stream_count_l = 0;
        this.stream_count_i = 0;
        this.ticker = 0;
        this.player_hit_count = 0;
        this.backoff_timer = 0;
        this.guard_mode = 0;
        this.head_found = 0;
        this.wing_sound = 0;
        this.large_unknown_detected = 0;
        this.isEnd = 0;
        this.endCounter = 0;
        // setSize(22, 24) or (5.5, 6) by PlayNicely (:63-68): getDefaultDimensions from the watcher (R9).
        if (!par1World.isClientSide) {
            this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
            this.refreshDimensions();
        }
        // getNavigator().setAvoidsWater(false) (:69): a water path malus of 0 (W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 25000;
        // isImmuneToFire (:71) is fireImmune() of the entity type; fireResistance 5000 (:72) is getFireImmuneTicks().
        this.noPhysics = true;
        this.TargetSorter = new GenericTargetSorter(this);
        // renderDistanceWeight = 12 (:75): shouldRenderAtSqrDistance below draws him at any distance anyway.
        this.moveControl = new LegacyMoveControl(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EntityAILookIdle(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // applyEntityAttributes (:81-87): the supplier is built before the config loads (R3), so the runtime values go
        // in here (PitchBlack precedent). attackDamage takes TheKing_attack; the attdam field stays 250.
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.mygetMaxHealth()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.6200000047683716);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(MobStats.TheKing_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:81-87) with the manifest defaults; see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(7000.0))
                .add(Attributes.MOVEMENT_SPEED, 0.6200000047683716)
                .add(Attributes.ATTACK_DAMAGE, 350.0);
    }

    /** {@code entityInit} (:89-94). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
        builder.define(DATA_IS_END, 0);
    }

    /** {@code getPlayNicely} (:96-98). */
    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    /** The hitbox of {@code setSize} (:63-68). */
    @Override
    protected EntityDimensions getDefaultDimensions(final Pose pose) {
        if (this.getPlayNicely() == 0) {
            return EntityDimensions.scalable(22.0f, 24.0f);
        }
        return EntityDimensions.scalable(5.5f, 6.0f);
    }

    @Override
    public void onSyncedDataUpdated(final EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_PLAY_NICELY.equals(key)) {
            this.refreshDimensions();
        }
    }

    /** {@code isInRangeToRenderDist} (:100-103). */
    @Override
    public boolean shouldRenderAtSqrDistance(final double distance) {
        return true;
    }

    /** {@code canDespawn} (:110-112). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getAttacking} (:114-116). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:118-120). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getSoundVolume} (:122-124). */
    @Override
    protected float getSoundVolume() {
        return 1.35f;
    }

    /** {@code getSoundPitch} (:126-128): no random spread. */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:130-132). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KING_LIVING.get();
    }

    /** {@code getHurtSound} (:134-136). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.KING_HIT.get();
    }

    /** {@code getDeathSound} (:138-140). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TREX_DEATH.get();
    }

    /** {@code canBePushed} (:142-144). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code collideWithEntity} (:146-147) is empty. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:149-151), original units. */
    public int mygetMaxHealth() {
        return MobStats.TheKing_stats().health();
    }

    /** R4. */
    @Override
    public double getOriginalMaxHealth() {
        return this.mygetMaxHealth();
    }

    /** {@code fireResistance = 5000} (:72). */
    @Override
    protected int getFireImmuneTicks() {
        return 5000;
    }

    // getDropItem (:153-155, yellow flower) is never read: dropFewItems below does not call super.

    /**
     * {@code dropItemRand} (:157-160): x and z shifted by {@code OreSpawnRand} -19..19, twelve blocks up, spawned
     * directly. PORT: a block without an item gave {@code new ItemStack(null)} in 1.7.10; the empty stack is not
     * spawned here (an empty item entity removes itself on its first tick anyway).
     */
    private void dropItemRand(final Item index, final int par1) {
        final ItemStack stack = new ItemStack(index, par1);
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(20) - OreSpawn.OreSpawnRand.nextInt(20),
                this.getY() + 12.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(20) - OreSpawn.OreSpawnRand.nextInt(20),
                stack);
        if (stack.isEmpty()) {
            return;
        }
        this.level().addFreshEntity(var3);
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:162-209): "The Prince" ten blocks up, the Royal Guardian set and sword, then 150 random
     * items and 150 random blocks from the registries, one each.
     *
     * <p>The random pick is the original's: {@code 1 + world.rand.nextInt(count)} steps into the registry iterator.
     * PORT: {@code Item.itemRegistry}/{@code Block.blockRegistry} are {@link BuiltInRegistries#ITEM}/{@code BLOCK}, the
     * k-th entry is {@code byId(k - 1)}. The Prince is W10's {@code ThePrince}, written in parallel - looked up by its
     * registry id {@code orespawn:the_prince} (the original looked it up by name as well).
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        Item it = null;
        Block bl = null;
        spawnCreature(this.level(), THE_PRINCE, this.getX(), this.getY() + 10.0, this.getZ());
        this.dropItemRand(ModItems.ROYAL_CHEST.get(), 1);
        this.dropItemRand(ModItems.ROYAL_HELMET.get(), 1);
        this.dropItemRand(ModItems.ROYAL_LEGGINGS.get(), 1);
        this.dropItemRand(ModItems.ROYAL_BOOTS.get(), 1);
        this.dropItemRand(ModItems.ROYAL.get(), 1);
        final int icount = BuiltInRegistries.ITEM.size();
        int j = 0;
        while (j < 150) {
            final int k = 1 + this.level().random.nextInt(icount);
            it = BuiltInRegistries.ITEM.byId(k - 1);
            if (it != null) {
                ++j;
                this.dropItemRand(it, 1);
            }
        }
        final int bcount = BuiltInRegistries.BLOCK.size();
        j = 0;
        while (j < 150) {
            final int k = 1 + this.level().random.nextInt(bcount);
            bl = BuiltInRegistries.BLOCK.byId(k - 1);
            if (bl != null) {
                ++j;
                this.dropItemRand(bl.asItem(), 1);
            }
        }
    }

    /** Registry id of {@code "The Prince"} (W10, {@code ThePrince}). */
    private static final ResourceLocation THE_PRINCE = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_prince");

    /**
     * {@code onUpdate} (:215-247): the vanilla tick, the wing beat every 31 ticks (server), the damped climb, the
     * melee phases, and on the client the firework sparks of the Ultimate King.
     */
    @Override
    public void tick() {
        super.tick();
        ++this.wing_sound;
        if (this.wing_sound > 30) {
            if (!this.level().isClientSide) {
                this.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 1.75f, 0.75f);
            }
            this.wing_sound = 0;
        }
        this.noPhysics = true;
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        final float health = VirtualHealth.originalHealth(this);
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() * 2 / 3) {
            this.attdam = MobStats.TheKing_stats().attack() * 2;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 2) {
            this.attdam = MobStats.TheKing_stats().attack() * 4;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 4) {
            this.attdam = MobStats.TheKing_stats().attack() * 8;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 8) {
            this.attdam = MobStats.TheKing_stats().attack() * 16;
        }
        if (this.level().isClientSide) {
            final float f = 7.0f;
            this.isEnd = this.entityData.get(DATA_IS_END);
            if (this.isEnd != 0 && this.level().random.nextInt(3) == 1) {
                for (int i = 0; i < 10; ++i) {
                    final Vec3 mm = this.getDeltaMovement();
                    this.level().addParticle(ParticleTypes.FIREWORK,
                            this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                            this.getY() + 14.0,
                            this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())),
                            (this.level().random.nextGaussian() - this.level().random.nextGaussian()) / 4.0 + mm.x * 6.0,
                            (this.level().random.nextGaussian() - this.level().random.nextGaussian()) / 4.0,
                            (this.level().random.nextGaussian() - this.level().random.nextGaussian()) / 4.0 + mm.z * 6.0);
                }
            }
        }
    }

    /**
     * {@code attackEntityAsMob} (:249-283). A large living target (area above 30) that is no royalty, Mobzilla, its head,
     * a Nightmare or the Kraken loses half its health and takes {@code attdam * 10}; the Ender Dragon takes
     * {@code attdam} as an explosion on its head (1/6) or body; then mob damage {@code attdam}, and on a hit a push of
     * 3.3 away and 0.25..0.5 up (x1.5 for players and removed entities).
     *
     * <p>PORT: {@code attackEntityFromPart} is {@link EnderDragon#hurt(EnderDragonPart, DamageSource, float)}; an
     * explosion source without an entity is in {@code #minecraft:always_hurts_ender_dragons}
     * ({@code DamageTypeTagsProvider}: {@code is_explosion}), the 1.21.1 form of 1.7.10's {@code isExplosion()} branch
     * (catalogue 6.3). Godzilla, GodzillaHead and Kraken are W10 classes written in parallel: registry ids
     * {@code mobzilla}, {@code mobzilla_head}, {@code the_kraken} (none of them has a subclass in 20.2). The halving of a
     * virtual-health target is proportional and needs no conversion (R4).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final float s = par1Entity.getBbHeight() * par1Entity.getBbWidth();
            if (s > 30.0f && !MyUtils.isRoyalty(par1Entity) && !isOreSpawnType(par1Entity, "mobzilla")
                    && !isOreSpawnType(par1Entity, "mobzilla_head") && !(par1Entity instanceof PitchBlack)
                    && !isOreSpawnType(par1Entity, "the_kraken")) {
                final LivingEntity e = (LivingEntity) par1Entity;
                e.setHealth(e.getHealth() / 2.0f);
                e.hurt(this.damageSources().mobAttack(this), (float) this.attdam * 10.0f);
                this.large_unknown_detected = 1;
            }
        }
        if (par1Entity != null && par1Entity instanceof EnderDragon dr) {
            DamageSource var21 = null;
            var21 = this.damageSources().explosion(null, null);
            if (this.level().random.nextInt(6) == 1) {
                dr.hurt(dr.head, var21, (float) this.attdam);
            } else {
                dr.hurt(dragonBody(dr), var21, (float) this.attdam);
            }
        }
        final boolean var22 = par1Entity.hurt(this.damageSources().mobAttack(this), (float) this.attdam);
        if (var22) {
            final double ks = 3.3;
            double inair = 0.25;
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            inair += this.level().random.nextFloat() * 0.25f;
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 1.5;
            }
            // addVelocity: the accepted hit has already marked the target for a motion packet (LivingEntity.hurt).
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return var22;
    }

    /** {@code dragonPartBody}: the part named "body" (index 2 of {@code getSubEntities()}; PitchBlack precedent). */
    private static EnderDragonPart dragonBody(final EnderDragon dr) {
        for (final EnderDragonPart part : dr.getSubEntities()) {
            if ("body".equals(part.name)) {
                return part;
            }
        }
        return dr.getSubEntities()[2];
    }

    // canSeeTarget (:285-287) has no caller in the class.

    /** {@code tooFarFromHome} (:289-294): more than 120 blocks from the home column. */
    private boolean tooFarFromHome() {
        float d1 = (float) (this.getX() - this.homex);
        final float d2 = (float) (this.getZ() - this.homez);
        d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
        return d1 > 120.0f;
    }

    /** {@code msgToPlayers} (:296-307): every player in the box, nearest first. */
    private void msgToPlayers(final String s) {
        final List<Player> var5 = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        var5.sort(this.TargetSorter);
        for (final Player var8 : var5) {
            var8.sendSystemMessage(Component.literal(s));
        }
    }

    /** {@code findNearestPlayer} (:309-325). */
    @Nullable
    private Player findNearestPlayer() {
        final List<Player> var5 = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        var5.sort(this.TargetSorter);
        Player var8 = null;
        for (final Player var7 : var5) {
            var8 = var7;
            if (var8 != null) {
                break;
            }
        }
        return var8;
    }

    /**
     * {@code updateAITasks} (:327-691). Outside the cut-scene: the Ultimate bookkeeping, hurt timer, home column,
     * magazines, a new waypoint or (1 in {@code attrand}) the attack branch, the purple orbs of the Ultimate King, the
     * steering, regeneration and the 2000 floor. In the cut-scene ({@code isEnd == 1}): stand still, freeze the nearest
     * player at one health and count down to {@code isEnd = 2}.
     *
     * <p>{@code this.rand} is {@link #getRandom()}, {@code worldObj.rand} is {@code level().random}; {@code moveForward}
     * is {@code zza}; {@code isDead} is {@link #isRemoved()}.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int attrand = 5;
        int which = 0;
        LivingEntity e = null;
        LivingEntity f = null;
        double rr = 0.0;
        double rhdir = 0.0;
        double rdd = 0.0;
        final double pi = PI;
        double var1 = 0.0;
        double var2 = 0.0;
        double var3 = 0.0;
        float var4 = 0.0f;
        float var5 = 0.0f;
        Player p = null;
        if (this.isRemoved()) {
            return;
        }
        // super.updateAITasks() (:348): the tasks and navigator already ran in serverAiStep; the move helper runs here.
        ((LegacyMoveControl) this.moveControl).legacyTick();
        super.customServerAiStep();
        this.entityData.set(DATA_IS_END, this.isEnd);
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
        final Level world = this.level();
        if (this.isEnd != 1) {
            if (this.isEnd == 2) {
                this.hurt_timer = 10;
                this.player_hit_count = 0;
                this.stream_count = 10;
                this.stream_count_l = 10;
                this.stream_count_i = 10;
                attrand = 3;
                this.guard_mode = 0;
                this.large_unknown_detected = 1;
                if (this.backoff_timer > 0) {
                    --this.backoff_timer;
                }
            }
            if (this.hurt_timer > 0) {
                --this.hurt_timer;
            }
            if ((this.homex == 0 && this.homez == 0) || this.guard_mode == 0) {
                this.homex = Mth.floor(this.getX());
                this.homez = Mth.floor(this.getZ());
            }
            ++this.ticker;
            if (this.ticker > 30000) {
                this.ticker = 0;
            }
            if (this.ticker % 80 == 0) {
                this.stream_count = 10;
            }
            if (this.ticker % 90 == 0) {
                this.stream_count_l = 5;
            }
            if (this.ticker % 70 == 0) {
                this.stream_count_i = 8;
            }
            if (this.backoff_timer > 0) {
                --this.backoff_timer;
            }
            if (this.player_hit_count < 10 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth() / 2) {
                attrand = 3;
            }
            this.noPhysics = true;
            if (this.currentFlightTarget == null) {
                this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
            }
            if (this.tooFarFromHome() || world.random.nextInt(200) == 0
                    || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 9.1f) {
                zdir = world.random.nextInt(120);
                xdir = world.random.nextInt(120);
                if (world.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (world.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                int dist = 0;
                for (int i = -5; i <= 5; i += 5) {
                    for (int j = -5; j <= 5; j += 5) {
                        BlockState bid = this.blockAt(this.homex + j, Mth.floor(this.getY()), this.homez + i);
                        if (!bid.isAir()) {
                            for (int k = 1; k < 20; ++k) {
                                bid = this.blockAt(this.homex + j, Mth.floor(this.getY()) + k, this.homez + i);
                                ++dist;
                                if (bid.isAir()) {
                                    break;
                                }
                            }
                        } else {
                            for (int k = 1; k < 20; ++k) {
                                bid = this.blockAt(this.homex + j, Mth.floor(this.getY()) - k, this.homez + i);
                                --dist;
                                if (!bid.isAir()) {
                                    break;
                                }
                            }
                        }
                    }
                }
                dist = dist / 9 + 2;
                // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
                if (Mth.floor(this.getY() + dist) > RoyalCeiling.of(this.level())) {
                    dist = RoyalCeiling.of(this.level()) - Mth.floor(this.getY());
                }
                this.currentFlightTarget.set(this.homex + xdir, Mth.floor(this.getY() + dist), this.homez + zdir);
            } else if (world.random.nextInt(attrand) == 0) {
                e = this.rt;
                if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
                    e = null;
                }
                if (e != null && (e instanceof TheKing || e instanceof KingHead)) {
                    this.rt = null;
                    e = null;
                }
                if (e != null) {
                    float d1 = (float) (e.getX() - this.homex);
                    final float d2 = (float) (e.getZ() - this.homez);
                    d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
                    if (e.isRemoved() || world.random.nextInt(250) == 1 || (d1 > 128.0f && this.guard_mode == 1)) {
                        e = null;
                        this.rt = null;
                    }
                    if (e != null && !this.MyCanSee(e)) {
                        e = null;
                    }
                }
                f = this.findSomethingToAttack();
                if (this.head_found == 0) {
                    spawnCreature(world, ModEntities.KING_HEAD.get(), this.getX(), this.getY() + 20.0, this.getZ());
                }
                if (e == null) {
                    e = f;
                }
                if (e != null) {
                    this.setAttacking(1);
                    if (this.backoff_timer == 0) {
                        int dist = Mth.floor(e.getY() + e.getBbHeight() / 2.0f + 1.0);
                        // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
                        if (dist > RoyalCeiling.of(this.level())) {
                            dist = RoyalCeiling.of(this.level());
                        }
                        this.currentFlightTarget.set(Mth.floor(e.getX()), dist, Mth.floor(e.getZ()));
                        if (world.random.nextInt(70) == 1) {
                            this.backoff_timer = 80 + world.random.nextInt(80);
                        }
                    } else if (InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 9.1f) {
                        zdir = world.random.nextInt(20) + 30;
                        xdir = world.random.nextInt(20) + 30;
                        if (world.random.nextInt(2) == 0) {
                            zdir = -zdir;
                        }
                        if (world.random.nextInt(2) == 0) {
                            xdir = -xdir;
                        }
                        int dist = 0;
                        for (int i = -5; i <= 5; i += 5) {
                            for (int j = -5; j <= 5; j += 5) {
                                BlockState bid = this.blockAt(Mth.floor(e.getX()) + j, Mth.floor(this.getY()), Mth.floor(e.getZ()) + i);
                                if (!bid.isAir()) {
                                    for (int k = 1; k < 20; ++k) {
                                        bid = this.blockAt(Mth.floor(e.getX()) + j, Mth.floor(this.getY()) + k, Mth.floor(e.getZ()) + i);
                                        ++dist;
                                        if (bid.isAir()) {
                                            break;
                                        }
                                    }
                                } else {
                                    for (int k = 1; k < 20; ++k) {
                                        bid = this.blockAt(Mth.floor(e.getX()) + j, Mth.floor(this.getY()) - k, Mth.floor(e.getZ()) + i);
                                        --dist;
                                        if (!bid.isAir()) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        dist = dist / 9 + 2;
                        // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
                        if (Mth.floor(this.getY() + dist) > RoyalCeiling.of(this.level())) {
                            dist = RoyalCeiling.of(this.level()) - Mth.floor(this.getY());
                        }
                        this.currentFlightTarget.set(Mth.floor(e.getX()) + xdir, Mth.floor(this.getY() + dist), Mth.floor(e.getZ()) + zdir);
                    }
                    if (this.distanceToSqr(e) < 900.0) {
                        if (world.random.nextInt(2) == 1) {
                            this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 15.0, MobStats.TheKing_stats().attack() / 4, 0);
                        }
                        this.doHurtTarget(e);
                    }
                    final double dx = this.getX() + 20.0 * Math.sin(Math.toRadians(this.getYHeadRot()));
                    final double dz = this.getZ() - 20.0 * Math.cos(Math.toRadians(this.getYHeadRot()));
                    if (world.random.nextInt(3) == 1) {
                        this.doJumpDamage(dx, this.getY() + 10.0, dz, 15.0, MobStats.TheKing_stats().attack() / 2, 1);
                    }
                    if (this.getHorizontalDistanceSqToEntity(e) > 900.0) {
                        which = world.random.nextInt(3);
                        if (which == 0) {
                            if (this.stream_count > 0) {
                                this.setAttacking(1);
                                rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                                rhdir = Math.toRadians((this.getYHeadRot() + 90.0f) % 360.0f);
                                rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                                if (rdd > pi) {
                                    rdd -= pi * 2.0;
                                }
                                rdd = Math.abs(rdd);
                                if (rdd < 0.5) {
                                    this.firecanon(e);
                                }
                            }
                        } else if (which == 1) {
                            if (this.stream_count_l > 0) {
                                this.setAttacking(1);
                                rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                                rhdir = Math.toRadians((this.getYHeadRot() + 90.0f) % 360.0f);
                                rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                                if (rdd > pi) {
                                    rdd -= pi * 2.0;
                                }
                                rdd = Math.abs(rdd);
                                if (rdd < 0.5) {
                                    this.firecanonl(e);
                                }
                            }
                        } else if (this.stream_count_i > 0) {
                            this.setAttacking(1);
                            rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            rhdir = Math.toRadians((this.getYHeadRot() + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                this.firecanoni(e);
                            }
                        }
                    }
                } else {
                    this.setAttacking(0);
                    this.stream_count = 10;
                    this.stream_count_l = 5;
                    this.stream_count_i = 8;
                }
            }
            if (this.getAttacking() != 0 && this.isEnd == 2) {
                final double xzoff = 10.0;
                final double yoff = 14.0;
                final Entity ppwr = spawnCreature(world, ModEntities.PURPLE_POWER.get(),
                        this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())),
                        this.getY() + yoff,
                        this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())));
                if (ppwr != null) {
                    final PurplePower pwr = (PurplePower) ppwr;
                    final Vec3 km = this.getDeltaMovement();
                    pwr.setDeltaMovement(km.x * 3.0, pwr.getDeltaMovement().y, km.z * 3.0);
                    pwr.setPurpleType(10);
                }
            }
            var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
            var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
            var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
            final Vec3 motion = this.getDeltaMovement();
            final double motionX = motion.x + (Math.signum(var1) * 0.7 - motion.x) * 0.35;
            final double motionY = motion.y + (Math.signum(var2) * 0.69999 - motion.y) * 0.3;
            final double motionZ = motion.z + (Math.signum(var3) * 0.7 - motion.z) * 0.35;
            this.setDeltaMovement(motionX, motionY, motionZ);
            var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
            var5 = Mth.wrapDegrees(var4 - this.getYRot());
            this.zza = 1.0f;
            this.setYRot(this.getYRot() + var5 / 8.0f);
            if (world.random.nextInt(30) == 1 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth()) {
                this.heal(5.0f);
                if (this.large_unknown_detected != 0) {
                    this.heal(200.0f);
                }
            }
            if (this.player_hit_count < 10 && VirtualHealth.originalHealth(this) < 2000.0f) {
                this.heal(2000.0f - VirtualHealth.originalHealth(this));
            }
            return;
        }
        ++this.endCounter;
        this.noPhysics = true;
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.hurt_timer = 10;
        if (this.isRemoved()) {
            return;
        }
        p = this.findNearestPlayer();
        if (p != null) {
            this.lookAt(p, 10.0f, 10.0f);
            // PORT: nothing marks the player's motion or rotation for a packet - the 1.7.10 server did not send them
            // either (no velocityChanged, no rotation packet to the player himself); only the health reaches him.
            p.setDeltaMovement(0.0, 0.0, 0.0);
            final double dd0 = this.getX() - p.getX();
            final double dd2 = this.getZ() - p.getZ();
            final float f2 = (float) (Math.atan2(dd2, dd0) * 180.0 / 3.141592653589793) - 90.0f;
            p.setYRot(f2);
            p.setHealth(1.0f);
        }
        if (this.endCounter == 10) {
            this.msgToPlayers("The King: Enough of this charade. I am done. You have shown me what I wanted to know.");
            return;
        }
        if (this.endCounter == 80) {
            this.msgToPlayers("The King: That's right my little pet. It has all been a game. You never killed me. You can't.");
            return;
        }
        if (this.endCounter == 160) {
            this.msgToPlayers("The King: I am the one. The only. The many. I exist within both space and time. Everywhere and always.");
            return;
        }
        if (this.endCounter == 240) {
            this.msgToPlayers("The King: I used you to learn your ways, and I have reached my conclusion on your species.");
            return;
        }
        if (this.endCounter == 300) {
            this.msgToPlayers("The King: You have 10 seconds to run...");
            return;
        }
        if (this.endCounter == 320) {
            this.msgToPlayers("9.");
            return;
        }
        if (this.endCounter == 340) {
            this.msgToPlayers("8.");
            return;
        }
        if (this.endCounter == 360) {
            this.msgToPlayers("7.");
            return;
        }
        if (this.endCounter == 380) {
            this.msgToPlayers("6.");
            return;
        }
        if (this.endCounter == 400) {
            this.msgToPlayers("5.");
            return;
        }
        if (this.endCounter == 420) {
            this.msgToPlayers("4.");
            return;
        }
        if (this.endCounter == 440) {
            this.msgToPlayers("3.");
            return;
        }
        if (this.endCounter == 460) {
            this.msgToPlayers("2.");
            return;
        }
        if (this.endCounter == 480) {
            this.msgToPlayers("1.");
            return;
        }
        if (this.endCounter == 500) {
            this.msgToPlayers("The King: Prepare to die!");
            this.isEnd = 2;
        }
    }

    /** {@code getHorizontalDistanceSqToEntity} (:693-697). */
    private double getHorizontalDistanceSqToEntity(final Entity e) {
        final double d1 = e.getZ() - this.getZ();
        final double d2 = e.getX() - this.getX();
        return d1 * d1 + d2 * d2;
    }

    /**
     * {@code firecanon} (:699-728): 32 blocks ahead and 14 up, one really big fireball at the target's middle, then six
     * big ones (half of them small) with a random offset; one magazine round.
     */
    private void firecanon(final LivingEntity e) {
        final double yoff = 14.0;
        final double xzoff = 32.0;
        BetterFireball bf = null;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        if (this.stream_count > 0) {
            bf = new BetterFireball(this.level(), this, e.getX() - cx, e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff), e.getZ() - cz);
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setReallyBig();
            this.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(bf);
            for (int i = 0; i < 6; ++i) {
                final float r1 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final float r2 = 3.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final float r3 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                bf = new BetterFireball(this.level(), this, e.getX() - cx + r1, e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff) + r2, e.getZ() - cz + r3);
                bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
                bf.setPos(cx, this.getY() + yoff, cz);
                bf.setBig();
                if (this.level().random.nextInt(2) == 1) {
                    bf.setSmall();
                }
                this.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(bf);
            }
            --this.stream_count;
        }
    }

    /**
     * {@code firecanonl} (:730-762): three ThunderBolts without a thrower, aimed with lead {@code 0.2 x horizontal
     * distance}, velocity 1.4 and spread 4, then tripled. The offsets {@code r1..r3} are drawn and never used.
     */
    private void firecanonl(final LivingEntity e) {
        final double yoff = 14.0;
        final double xzoff = 32.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        if (this.stream_count_l > 0) {
            this.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            for (int i = 0; i < 3; ++i) {
                final float r1 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final float r2 = 3.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final float r3 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final ThunderBolt lb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                var3 = e.getX() - lb.getX();
                var4 = e.getY() + 0.25 - lb.getY();
                var5 = e.getZ() - lb.getZ();
                var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                lb.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 4.0f);
                lb.setDeltaMovement(lb.getDeltaMovement().scale(3.0));
                this.level().addFreshEntity(lb);
            }
            --this.stream_count_l;
        }
    }

    /** {@code firecanoni} (:764-797): five ice-making IceBalls, otherwise as {@link #firecanonl}. */
    private void firecanoni(final LivingEntity e) {
        final double yoff = 14.0;
        final double xzoff = 32.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        if (this.stream_count_i > 0) {
            this.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            for (int i = 0; i < 5; ++i) {
                final float r1 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final float r2 = 3.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final float r3 = 5.0f * (this.level().random.nextFloat() - this.level().random.nextFloat());
                final IceBall lb = new IceBall(this.level(), cx, this.getY() + yoff, cz);
                lb.setIceMaker(1);
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                var3 = e.getX() - lb.getX();
                var4 = e.getY() + 0.25 - lb.getY();
                var5 = e.getZ() - lb.getZ();
                var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                lb.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 4.0f);
                lb.setDeltaMovement(lb.getDeltaMovement().scale(3.0));
                this.level().addFreshEntity(lb);
            }
            --this.stream_count_i;
        }
    }

    // canTriggerWalking (:799-801) returns true: no NoStepTrigger (R20). doesEntityNotTriggerPressurePlate (:809-811)
    // returns false: the default of isIgnoringBlockTriggers.

    /** {@code fall} (:803-804) is empty. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:806-807) is empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code attackEntityFrom} (:813-855): refused during {@code hurt_timer}; capped at 750; {@code inWall} ignored; a
     * large attacker (area above 30, same exceptions as {@link #doHurtTarget}) deals a tenth and trips
     * {@code large_unknown_detected}; a small monster attacker (area below 3) is removed and the hit refused; cactus
     * does nothing. Otherwise the hit lands, a player hit counts, and a living non-royal attacker becomes {@code rt}
     * and the waypoint.
     *
     * <p>The amount given to {@code super} is in original units; {@code CombatEvents} scales it (R4) and applies the
     * 1.7.10 armor formula (R5). {@code setDead()} is {@link #discard()} on the attacker.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float dm = par2;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (dm > 750.0f) {
            dm = 750.0f;
        }
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof LivingEntity enl) {
            final float s = enl.getBbHeight() * enl.getBbWidth();
            if (s > 30.0f && !MyUtils.isRoyalty(enl) && !isOreSpawnType(enl, "mobzilla") && !isOreSpawnType(enl, "mobzilla_head")
                    && !(enl instanceof PitchBlack) && !isOreSpawnType(enl, "the_kraken")) {
                dm /= 10.0f;
                this.hurt_timer = 50;
                this.large_unknown_detected = 1;
            }
            if (e instanceof Monster && s < 3.0f) {
                e.discard();
                return false;
            }
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            this.hurt_timer = 20;
            ret = super.hurt(par1DamageSource, dm);
            if (e != null && e instanceof Player) {
                ++this.player_hit_count;
            }
            if (e != null && e instanceof LivingEntity && this.currentFlightTarget != null && !MyUtils.isRoyalty(e)) {
                this.rt = (LivingEntity) e;
                int dist = Mth.floor(e.getY());
                // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
                if (dist > RoyalCeiling.of(this.level())) {
                    dist = RoyalCeiling.of(this.level());
                }
                this.currentFlightTarget.set(Mth.floor(e.getX()), dist, Mth.floor(e.getZ()));
            }
        }
        return ret;
    }

    /** {@code getCanSpawnHere} (:857-859): always. */
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
     * {@code getTotalArmorValue} (:861-875): 25 once a large unknown was met; {@code defense + 1} below two thirds
     * health while fewer than ten player hits; otherwise {@code defense}. The {@code + 2} and {@code + 3} branches are
     * unreachable behind the first one, as in the original.
     */
    public int getTotalArmorValue() {
        if (this.large_unknown_detected != 0) {
            return 25;
        }
        final float health = VirtualHealth.originalHealth(this);
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() * 2 / 3) {
            return MobStats.TheKing_stats().defense() + 1;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 2) {
            return MobStats.TheKing_stats().defense() + 2;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 4) {
            return MobStats.TheKing_stats().defense() + 3;
        }
        return MobStats.TheKing_stats().defense();
    }

    /** R5. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code onStruckByLightning} (:877-878) is empty. */
    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightning) {
    }

    // initCreature (:880-881) is empty and overrides nothing that 1.21.1 calls.

    /**
     * {@code MyCanSee} (:883-941): a ray of its own from 22 blocks ahead of the body at seven eighths of its height to
     * the target's middle, at most one block per axis per step. Only water, {@code Blocks.leaves}, vines and air let it
     * through. {@code nblks *= (int) |d|} truncates, as in the original.
     *
     * <p>PORT (R22): {@code Blocks.leaves} is the leaves category, {@code #minecraft:leaves} without OreSpawn's own
     * leaves (MothSupport precedent); {@code flowing_water}/{@code water} are {@link Blocks#WATER} at any level.
     */
    public boolean MyCanSee(final LivingEntity e) {
        final double xzoff = 22.0;
        int nblks = 20;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + this.getBbHeight() * 7.0f / 8.0f);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 20.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 20.0);
        float dz = (float) ((e.getZ() - startz) / 20.0);
        if (Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks *= (int) Math.abs(dx);
            if (dx > 1.0f) {
                dx = 1.0f;
            }
            if (dx < -1.0f) {
                dx = -1.0f;
            }
        }
        if (Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks *= (int) Math.abs(dy);
            if (dy > 1.0f) {
                dy = 1.0f;
            }
            if (dy < -1.0f) {
                dy = -1.0f;
            }
        }
        if (Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks *= (int) Math.abs(dz);
            if (dz > 1.0f) {
                dz = 1.0f;
            }
            if (dz < -1.0f) {
                dz = -1.0f;
            }
        }
        for (int i = 0; i < nblks; ++i) {
            startx += dx;
            starty += dy;
            startz += dz;
            final BlockState bid = this.blockAt(Mth.floor(startx), Mth.floor(starty), Mth.floor(startz));
            if (!bid.is(Blocks.WATER) && !isLegacyLeaves(bid)) {
                if (!bid.is(Blocks.VINE)) {
                    if (!bid.isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** {@code Blocks.leaves} as a category (R22), without OreSpawn's own leaves. */
    private static boolean isLegacyLeaves(final BlockState state) {
        return state.is(BlockTags.LEAVES)
                && !OreSpawn.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    /**
     * {@code isSuitableTarget} (:943-1003). PORT: {@code EntityHorse} is {@link AbstractHorse} (R22: the horse
     * category, llamas and camels included); {@code EntityDragon} is {@link EnderDragon}; {@code EntityMob} is
     * {@link Monster}.
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
        if (par1EntityLiving instanceof KingHead) {
            this.head_found = 1;
            return false;
        }
        if (MyUtils.isRoyalty(par1EntityLiving)) {
            return false;
        }
        float d1 = (float) (par1EntityLiving.getX() - this.homex);
        final float d2 = (float) (par1EntityLiving.getZ() - this.homez);
        d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
        if (d1 > 144.0f) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (this.isEnd == 2) {
            if (par1EntityLiving instanceof Player p) {
                return !p.getAbilities().instabuild;
            }
            if (par1EntityLiving instanceof Girlfriend) {
                return true;
            }
            if (par1EntityLiving instanceof Boyfriend) {
                return true;
            }
            if (par1EntityLiving instanceof Villager) {
                return true;
            }
        }
        if (!this.MyCanSee(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof AbstractHorse) {
            return true;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof EnderDragon) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /**
     * {@code findSomethingToAttack} (:1005-1043): nothing under {@code PlayNicely} (and no head either); the Ultimate
     * King takes the nearest suitable player first; otherwise the first suitable living entity in 80/64/80, scanning on
     * until the head was seen.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            this.head_found = 1;
            return null;
        }
        if (this.isEnd == 2) {
            final List<Player> var5p = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(80.0, 64.0, 80.0));
            var5p.sort(this.TargetSorter);
            this.head_found = 1;
            for (final Player var4p : var5p) {
                if (this.isSuitableTarget(var4p, false)) {
                    return var4p;
                }
            }
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(80.0, 64.0, 80.0));
        var5.sort(this.TargetSorter);
        LivingEntity ret = null;
        this.head_found = 0;
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false) && ret == null) {
                ret = var8;
            }
            if (ret != null && this.head_found != 0) {
                break;
            }
        }
        return ret;
    }

    /** {@code setGuardMode} (:1045-1047): 1 keeps the home column where he was spawned (spawner block, tree top). */
    public void setGuardMode(final int i) {
        this.guard_mode = i;
    }

    /** {@code setFree} (:1049-1051): starts the Ultimate King cut-scene (called by ThePrinceAdult). */
    public void setFree() {
        this.isEnd = 1;
    }

    /** {@code writeEntityToNBT} (:1053-1061). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("KingHomeX", this.homex);
        par1NBTTagCompound.putInt("KingHomeZ", this.homez);
        par1NBTTagCompound.putInt("GuardMode", this.guard_mode);
        par1NBTTagCompound.putInt("PlayerHits", this.player_hit_count);
        par1NBTTagCompound.putInt("IsEnd", this.isEnd);
        par1NBTTagCompound.putInt("EndCounter", this.endCounter);
    }

    /** {@code readEntityFromNBT} (:1063-1071). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.homex = par1NBTTagCompound.getInt("KingHomeX");
        this.homez = par1NBTTagCompound.getInt("KingHomeZ");
        this.guard_mode = par1NBTTagCompound.getInt("GuardMode");
        this.player_hit_count = par1NBTTagCompound.getInt("PlayerHits");
        this.isEnd = par1NBTTagCompound.getInt("IsEnd");
        this.endCounter = par1NBTTagCompound.getInt("EndCounter");
    }

    /**
     * {@code spawnCreature} (:1073-1081): create, random yaw, add - no living sound, no {@code onSpawnWithEgg}.
     * The original looked the type up by its {@code registerModEntity} name.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, @Nullable final EntityType<?> par1, final double par2,
                                       final double par4, final double par6) {
        Entity var8 = null;
        if (par1 != null) {
            var8 = par1.create(par0World);
        }
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
        }
        return var8;
    }

    /** {@link #spawnCreature(Level, EntityType, double, double, double)} by registry id; an unknown id spawns nothing. */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final ResourceLocation par1, final double par2,
                                       final double par4, final double par6) {
        return spawnCreature(par0World, BuiltInRegistries.ENTITY_TYPE.getOptional(par1).orElse(null), par2, par4, par6);
    }

    /**
     * {@code doJumpDamage} (:1083-1126): every living entity in the box {@code dist} wide and 10 up and down, except
     * himself, royalty and the two ghosts, takes {@code damage / 2} as an explosion and {@code damage / 2} as a fall
     * (the second usually lands inside the first one's invulnerability window, catalogue 6.4), with an explosion sound;
     * {@code knock != 0} pushes 2.75 away and 0.65 up.
     */
    @Nullable
    private LivingEntity doJumpDamage(final double X, final double Y, final double Z, final double dist, final double damage,
                                      final int knock) {
        final net.minecraft.world.phys.AABB bb = new net.minecraft.world.phys.AABB(X - dist, Y - 10.0, Z - dist, X + dist, Y + 10.0, Z + dist);
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, bb);
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (var8 == null) {
                continue;
            }
            if (var8 == this) {
                continue;
            }
            if (!var8.isAlive()) {
                continue;
            }
            if (MyUtils.isRoyalty(var8)) {
                continue;
            }
            if (var8 instanceof Ghost) {
                continue;
            }
            if (var8 instanceof GhostSkelly) {
                continue;
            }
            DamageSource var9 = null;
            var9 = this.damageSources().explosion(null, null);
            var8.hurt(var9, (float) damage / 2.0f);
            var8.hurt(this.damageSources().fall(), (float) damage / 2.0f);
            this.playSoundAtEntity(var8, SoundEvents.GENERIC_EXPLODE.value(), 0.65f,
                    1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);
            if (knock == 0) {
                continue;
            }
            final double ks = 2.75;
            final double inair = 0.65;
            final float f3 = (float) Math.atan2(var8.getZ() - this.getZ(), var8.getX() - this.getX());
            var8.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
        }
        return null;
    }

    /**
     * {@code World.playSoundAtEntity(entity, name, volume, pitch)}: at the entity for every nearby player (PitchBlack
     * precedent), in this King's sound source.
     */
    private void playSoundAtEntity(final Entity at, final SoundEvent sound, final float volume, final float pitch) {
        this.level().playSound((Player) null, at.getX(), at.getY(), at.getZ(), sound, this.getSoundSource(), volume, pitch);
    }

    /** {@code worldObj.getBlock(x, y, z)}. */
    private BlockState blockAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /** {@code instanceof} for a class of another porter of this wave, by registry id. */
    static boolean isOreSpawnType(final Entity entity, final String id) {
        final ResourceLocation key = EntityType.getKey(entity.getType());
        return key.getNamespace().equals(OreSpawn.MOD_ID) && key.getPath().equals(id);
    }

    /**
     * 1.7.10 {@code EntityMoveHelper} ran inside {@code super.updateAITasks()}, before the override set
     * {@code moveForward}. Its slot after {@code customServerAiStep} does nothing; {@link #legacyTick()} runs the
     * vanilla tick where the original ran it.
     */
    static final class LegacyMoveControl extends MoveControl {

        LegacyMoveControl(final Mob mob) {
            super(mob);
        }

        /** The vanilla tick, called at the start of {@code customServerAiStep}. */
        void legacyTick() {
            super.tick();
        }

        @Override
        public void tick() {
        }
    }
}
