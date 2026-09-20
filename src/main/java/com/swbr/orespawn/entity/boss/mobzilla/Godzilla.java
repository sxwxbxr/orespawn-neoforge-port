package com.swbr.orespawn.entity.boss.mobzilla;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Godzilla} (Godzilla.java:20-1814), id {@code mobzilla} ("Mobzilla", OreSpawnMain.java:3703,
 * tracking 128/1/false). The 25-block ground boss: crushes blocks around its body and behind it, stamps landing rings,
 * swipes with its tail, fires fireball salvoes, calls lightning, pounces and heals fast (verhalten/entity-03.md).
 *
 * <p>Values: {@code Godzilla_stats} (4000/175/21), speed 0.75 re-set every tick (:37, :133), {@code experienceValue}
 * 10000 (:54), {@code fireResistance} 10000 (:63), {@code renderDistanceWeight} 12 (:65). Size 9.9 x 25, with
 * {@code PlayNicely} 2.475 x 6.25 (:47-52), see {@link #getDefaultDimensions}. DataWatcher 20 {@code attacking} (model), 21
 * {@code PlayNicely} (renderer scale). No NBT: {@code large_unknown_detected}, {@code hurt_timer}, {@code jump_timer},
 * {@code stream_count} and {@code ticker} are lost on reload, as in the original.
 *
 * <p>R4: 4000 health lies above the clamp of 1024; the attribute holds {@link LegacyCombatMath#attributeMaxHealth} and
 * {@link com.swbr.orespawn.combat.CombatEvents} scales every hit after the 750 cap of {@link #hurt} and every heal. R5:
 * {@link #getLegacyArmorValue} is the original {@code getTotalArmorValue} with its 25 branch.
 *
 * <p>Not carried over: {@code onLivingUpdate} (:140-142) and {@code isAIEnabled} (:126-128) change nothing;
 * {@code getDropItem} (:173-175) is dead because {@code dropFewItems} is overridden; {@code setTarget(e)} (:716, the
 * 1.7.10 {@code entityToAttack} of the old AI) had no reader for an AI-enabled mob.
 *
 * <p>PORT: no vanilla boss bar. 1.7.10 Godzilla did not implement {@code IBossDisplayData}; its bar was the
 * {@code GirlfriendOverlayGui} (W11), which reads {@code getHealth() / getMaxHealth()} - the same ratio in both scales.
 */
public class Godzilla extends Monster implements VirtualHealth, LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code GodzillaModel} for the tail, jaw and arm animation. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Godzilla.class, EntityDataSerializers.INT);
    /** DataWatcher 21: {@code PlayNicely}, re-sent every AI tick (:242), read by {@code RenderGodzilla}. */
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(Godzilla.class, EntityDataSerializers.INT);

    /** {@code setSize(2.475f, 6.25f)} (:51). */
    private static final EntityDimensions PLAY_NICELY_SIZE = EntityDimensions.scalable(2.475f, 6.25f);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    private int hurt_timer;
    private int jumped;
    private int jump_timer;
    private int ticker;
    private RenderInfo renderdata;
    private int stream_count;
    private MyEntityAIWanderALot wander;
    private int head_found;
    private int large_unknown_detected;
    /** The hitbox chosen in the constructor from {@code PlayNicely} (:47-52), server side. */
    private boolean smallSize;

    /** {@code Godzilla(World)} (:34-66) with {@code entityInit}'s {@link RenderInfo} reset (:79-89). */
    public Godzilla(final EntityType<? extends Godzilla> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.75f;
        this.hurt_timer = 0;
        this.jumped = 0;
        this.jump_timer = 0;
        this.ticker = 0;
        this.renderdata = new RenderInfo();
        this.stream_count = 8;
        this.wander = null;
        this.head_found = 0;
        this.large_unknown_detected = 0;
        // DataWatcher 21 starts at OreSpawnMain.PlayNicely (:78); the client receives the server's value.
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
        this.smallSize = OreSpawnConfig.TWEAKS.PlayNicely.get() != 0;
        this.refreshDimensions();
        // PORT: getNavigator().setAvoidsWater(true) (:53) - water malus -1 (Hammerhead, W07; CaterKiller, W08).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 10000;
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT (R18 case 3): EntityAIMoveThroughVillage (:56) has no 1.21.1 counterpart with village records; vanilla's
        // POI-based MoveThroughVillageGoal with the parameters of MonsterSupport.legacyMoveThroughVillage (W07).
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.wander = new MyEntityAIWanderALot(this, 15, 1.0);
        this.goalSelector.addGoal(2, this.wander);
        // EntityAIWatchClosest(this, EntityLiving.class, 50.0f) (:59): EntityLiving is Mob.
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Mob.class, 50.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Godzilla_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.mygetMaxHealth()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:68-73) at registration time (R3); MAX_HEALTH clamped per R4. */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Godzilla_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(stats.health()))
                .add(Attributes.MOVEMENT_SPEED, (double) 0.75f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:75-90). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
    }

    /** {@code getPlayNicely} (:92-94). */
    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    /** {@code getRenderInfo} (:96-98): the model's note pad (plain data, no client imports). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:100-109): copies the fields. */
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

    /**
     * {@code setSize} as dimensions (R9): 2.475 x 6.25 when {@code PlayNicely} was set at construction, otherwise the type's
     * 9.9 x 25.
     *
     * <p>PORT (R18 case 4): the client takes the synced DataWatcher 21 instead of its own config (CaterKiller precedent,
     * W08).
     */
    @Override
    protected EntityDimensions getDefaultDimensions(final Pose pose) {
        final boolean small = this.level().isClientSide ? this.getPlayNicely() != 0 : this.smallSize;
        if (small) {
            return PLAY_NICELY_SIZE;
        }
        return super.getDefaultDimensions(pose);
    }

    /** Refreshes the client hitbox when DataWatcher 21 arrives (see {@link #getDefaultDimensions}). */
    @Override
    public void onSyncedDataUpdated(final EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_PLAY_NICELY.equals(key) && this.level().isClientSide) {
            this.refreshDimensions();
        }
    }

    /** {@code canDespawn} (:111-113): only with {@code PlayNicely}; persistence is checked by {@code Mob.checkDespawn}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return OreSpawnConfig.TWEAKS.PlayNicely.get() != 0;
    }

    /** {@code mygetMaxHealth} (:115-117), in original units. */
    public int mygetMaxHealth() {
        return MobStats.Godzilla_stats().health();
    }

    /** R4: the original maximum. */
    @Override
    public double getOriginalMaxHealth() {
        return this.mygetMaxHealth();
    }

    /** {@code getTotalArmorValue} (:119-124) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        if (this.large_unknown_detected != 0) {
            return 25;
        }
        return MobStats.Godzilla_stats().defense();
    }

    /** {@code fireResistance = 10000} (:63); {@code isImmuneToFire} is {@code fireImmune()} on the type. */
    @Override
    protected int getFireImmuneTicks() {
        return 10000;
    }

    /**
     * {@code renderDistanceWeight = 12.0} (:65) in 1.7.10 {@code isInRangeToRenderDist}: the average edge length x 64 x
     * weight (Fairy precedent, W07).
     */
    @Override
    public boolean shouldRenderAtSqrDistance(final double distance) {
        double d0 = this.getBoundingBox().getSize();
        if (Double.isNaN(d0)) {
            d0 = 1.0;
        }
        d0 *= 64.0 * getViewScale() * 12.0;
        return distance < d0 * d0;
    }

    /**
     * {@code onUpdate} (:130-138), both sides: speed, the vanilla tick, and the path is dropped while airborne.
     * {@code isAirBorne} is {@code hasImpulse}: both are set by jumps and knockback and cleared by the entity tracker.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.hasImpulse) {
            this.getNavigation().stop();
        }
    }

    /** {@code fall} (:144-145): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:147-148): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code getLivingSound} (:150-155): 1 in 5 on the world random, otherwise silent. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(5) == 0) {
            return ModSounds.GODZILLA_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:157-159). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:161-163). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GODZILLA_DEATH.get();
    }

    /** {@code getSoundVolume} (:165-167). */
    @Override
    protected float getSoundVolume() {
        return 1.65f;
    }

    /** {@code getSoundPitch} (:169-171): fixed, no random variation. */
    @Override
    public float getVoicePitch() {
        return 1.1f;
    }

    /**
     * {@code jump} (:177-197): yaw and head yaw wrapped into 0..360, 0.45 upward, a forward shove of 0.2-0.65 along the head
     * yaw, airborne, path dropped.
     *
     * <p>PORT: {@code this.posY += 0.5} is left out. 1.7.10 {@code Entity.moveEntity} rebuilt {@code posY} from the bounding
     * box, which the field write had not moved, so the lift never took effect; 1.21.1's {@code move} keeps a raw position
     * change (EmperorScorpion, HerculesBeetle, SpitBug precedent, W07).
     */
    @Override
    public void jumpFromGround() {
        float yRot = this.getYRot();
        float yHeadRot = this.getYHeadRot();
        while (yRot < 0.0f) {
            yRot += 360.0f;
        }
        while (yHeadRot < 0.0f) {
            yHeadRot += 360.0f;
        }
        while (yRot > 360.0f) {
            yRot -= 360.0f;
        }
        while (yHeadRot > 360.0f) {
            yHeadRot -= 360.0f;
        }
        this.setYRot(yRot);
        this.setYHeadRot(yHeadRot);
        final Vec3 m = this.getDeltaMovement();
        final double motionY = m.y + 0.44999998807907104;
        final float f = 0.2f + Math.abs(this.level().random.nextFloat() * 0.45f);
        final double motionX = m.x + f * Math.cos(Math.toRadians(this.getYHeadRot() + 90.0f));
        final double motionZ = m.z + f * Math.sin(Math.toRadians(this.getYHeadRot() + 90.0f));
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
        this.getNavigation().stop();
    }

    /** {@code jumpAtEntity} (:199-212): 1.25 upward, face the target, a horizontal shove of 5 % of the distance. Same PORT as {@link #jumpFromGround}. */
    protected void jumpAtEntity(final LivingEntity e) {
        final Vec3 m = this.getDeltaMovement();
        final double motionY = m.y + 1.25;
        double d1 = e.getX() - this.getX();
        final double d2 = e.getZ() - this.getZ();
        final float d3 = (float) Math.atan2(d2, d1);
        final float f2 = (float) (d3 * 180.0 / 3.141592653589793) - 90.0f;
        this.setYRot(f2);
        d1 = Math.sqrt(d1 * d1 + d2 * d2);
        final double motionX = m.x + d1 * 0.05 * Math.cos(d3);
        final double motionZ = m.z + d1 * 0.05 * Math.sin(d3);
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
        this.getNavigation().stop();
    }

    /** {@code getHorizontalDistanceSqToEntity} (:214-218). */
    private double getHorizontalDistanceSqToEntity(final Entity e) {
        final double d1 = e.getZ() - this.getZ();
        final double d2 = e.getX() - this.getX();
        return d1 * d1 + d2 * d2;
    }

    /** {@code MygetDistanceSqToEntity} (:220-231): up to 20 blocks above counts as level, above that {@code dy - 10}. */
    public double MygetDistanceSqToEntity(final Entity par1Entity) {
        final double d0 = this.getX() - par1Entity.getX();
        double d2 = par1Entity.getY() - this.getY();
        final double d3 = this.getZ() - par1Entity.getZ();
        if (d2 > 0.0 && d2 < 20.0) {
            d2 = 0.0;
        }
        if (d2 > 20.0) {
            d2 -= 10.0;
        }
        return d0 * d0 + d2 * d2 + d3 * d3;
    }

    /**
     * {@code updateAITasks} (:233-405), after the goal selectors: DataWatcher 21, the tick counters, the landing rings, the
     * body and tail crush zones, the tail swipe, the target cycle with lightning, pounce, melee and cannon, and the heal.
     *
     * <p>PORT: {@code (int)} casts of coordinates are {@link Mth#floor} (R20). The {@code mobGriefing} rule is asked once
     * per zone through {@link EventHooks#canEntityGrief} (Chipmunk precedent, W06); 1.7.10 read the same rule per block,
     * which cannot change inside one tick. Positions in chunks that are not loaded are skipped (R18 case 1): 1.7.10's
     * {@code getBlock} answered air there without loading anything, while 1.21.1's {@code getBlockState} and
     * {@code setBlock} on a server level load the chunk synchronously.
     */
    @Override
    protected void customServerAiStep() {
        LivingEntity e = null;
        int xzrange = 9;
        if (this.isRemoved()) {
            return;
        }
        final Level world = this.level();
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
        super.customServerAiStep();
        ++this.ticker;
        if (this.ticker > 30000) {
            this.ticker = 0;
        }
        if (this.ticker % 100 == 0) {
            this.stream_count = 8;
        }
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.jump_timer > 0) {
            --this.jump_timer;
        }
        OreSpawn.godzilla_has_spawned = 1;
        if (world.random.nextInt(200) == 0) {
            this.setTarget(null);
        }
        final int playNicely = OreSpawnConfig.TWEAKS.PlayNicely.get();
        final int attack = MobStats.Godzilla_stats().attack();
        if (playNicely == 0) {
            final double motionY = this.getDeltaMovement().y;
            if (motionY < -0.95) {
                this.jumped = 1;
            }
            if (motionY < -1.5) {
                this.jumped = 2;
            }
            if (this.jumped != 0 && motionY > -0.1) {
                double df = 1.0;
                if (this.jumped == 2) {
                    df = 1.5;
                }
                this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 10.0, attack * df, 0);
                this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 15.0, attack / 2 * df, 0);
                this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 25.0, attack / 4 * df, 0);
                this.jumped = 0;
            }
        }
        xzrange = 12;
        if (this.getAttacking() != 0) {
            xzrange = 16;
        }
        int k = -3 + this.ticker % 30;
        if (playNicely == 0) {
            this.crushLayer(Mth.floor(this.getX()), Mth.floor(this.getY()) + k, Mth.floor(this.getZ()), xzrange, false, 0.0, 0.0);
        }
        final double dx = this.getX() + 16.0 * Math.sin(Math.toRadians(this.getYHeadRot()));
        final double dz = this.getZ() - 16.0 * Math.cos(Math.toRadians(this.getYHeadRot()));
        k = -3 + this.ticker % 12;
        if (playNicely == 0) {
            this.crushLayer(Mth.floor(dx), Mth.floor(this.getY()) + k, Mth.floor(dz), xzrange, true, dx, dz);
        }
        if (playNicely == 0 && k == 0) {
            this.doJumpDamage(dx, this.getY(), dz, 15.0, attack / 2, 1);
        }
        if (world.random.nextInt(5 - this.large_unknown_detected) == 1) {
            e = this.getTarget();
            if (playNicely != 0) {
                e = null;
            }
            if (e != null) {
                if (!e.isAlive()) {
                    this.setTarget(null);
                    e = null;
                } else if (e instanceof Godzilla || e instanceof GodzillaHead) {
                    this.setTarget(null);
                    e = null;
                }
            }
            if (e == null) {
                e = this.findSomethingToAttack();
                if (this.head_found == 0) {
                    spawnCreature(world, ModEntities.MOBZILLA_HEAD.get(), this.getX(), this.getY() + 20.0, this.getZ());
                }
            }
            if (e != null) {
                this.wander.setBusy(1);
                this.lookAt(e, 10.0f, 10.0f);
                if (world.random.nextInt(65) == 1 && this.MygetDistanceSqToEntity(e) > 300.0) {
                    this.doLightningAttack(e);
                } else if (world.random.nextInt(20 - this.large_unknown_detected * 5) == 1 && this.jump_timer == 0) {
                    this.jumpAtEntity(e);
                    this.jump_timer = 30;
                } else if (this.MygetDistanceSqToEntity(e) < 300.0f + e.getBbWidth() / 2.0f * (e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    this.getNavigation().moveTo(e, 1.0);
                    if (world.random.nextInt(4 - this.large_unknown_detected) == 0
                            || world.random.nextInt(3 - this.large_unknown_detected) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.0);
                    if (this.getHorizontalDistanceSqToEntity(e) > 625.0) {
                        if (this.stream_count > 0) {
                            this.setAttacking(1);
                            final double rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            final double rhdir = Math.toRadians((this.getYHeadRot() + 90.0f) % 360.0f);
                            final double pi = 3.1415926545;
                            double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                this.firecanon(e);
                            }
                        } else {
                            this.setAttacking(0);
                        }
                    } else {
                        this.setAttacking(0);
                    }
                }
            } else {
                this.setAttacking(0);
                this.wander.setBusy(0);
                this.stream_count = 8;
            }
        }
        if (world.random.nextInt(35) == 1 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth()) {
            // heal(5) in original units; CombatEvents.onHeal scales it (R4).
            this.heal(5.0f);
        }
    }

    /**
     * One crush layer (:285-303 for the body, :309-327 for the tail): every crushable block in x/z {@code ±xzrange} becomes
     * air (flag 3) with a 1 in 15 chance to drop its item; grass and farmland turn into dirt under {@code mobGriefing}.
     *
     * <p>PORT: the air left in place is not rewritten - 1.7.10's {@code setBlock} of air on air returned early without an
     * update, 1.21.1's does too - but its 1 in 15 roll and drop position are drawn like the original's.
     */
    private void crushLayer(final int cx, final int y, final int cz, final int xzrange, final boolean tail,
                            final double dx, final double dz) {
        final Level world = this.level();
        final boolean griefing = EventHooks.canEntityGrief(world, this);
        final BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int i = -xzrange; i <= xzrange; ++i) {
            for (int j = -xzrange; j <= xzrange; ++j) {
                at.set(cx + i, y, cz + j);
                if (!world.hasChunkAt(at)) {
                    continue;
                }
                final BlockState bid = world.getBlockState(at);
                if (this.isCrushable(bid, griefing)) {
                    if (!bid.isAir()) {
                        world.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (world.random.nextInt(15) == 1) {
                        if (tail) {
                            this.dropItemRandAt(bid.getBlock(), 1, dx, dz).spawn();
                        } else {
                            this.dropItemRand(bid.getBlock(), 1).spawn();
                        }
                    }
                } else {
                    if (bid.is(Blocks.GRASS_BLOCK) && griefing) {
                        world.setBlock(at, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (bid.is(Blocks.FARMLAND) && griefing) {
                        world.setBlock(at, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    /**
     * {@code spawnCreature} (:407-415): create, place with a random yaw on the world random and pitch 0, add. No ambient
     * sound, unlike {@code ItemSpawnEgg.spawnSomething}.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2, final double par4,
                                       final double par6) {
        Entity var8 = null;
        var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
        }
        return var8;
    }

    /**
     * {@code isSuitableTarget} (:417-465): alive, not ignoreable, seen; never Mobzilla or its head, creepers, zombies,
     * spiders, skeletons, ghosts, or creative players.
     *
     * <p>PORT (R22): {@code EntitySkeleton} covered every skeleton of 1.7.10 including the wither skeleton; 1.21.1 splits
     * the family, so it is {@link AbstractSkeleton} (skeleton, stray, wither skeleton, bogged). {@code EntityZombie} and
     * {@code EntitySpider} keep their 1.21.1 subclasses (husk, drowned, zombie villager, zombified piglin; cave spider).
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
        if (par1EntityLiving instanceof Godzilla) {
            return false;
        }
        if (par1EntityLiving instanceof GodzillaHead) {
            return false;
        }
        if (par1EntityLiving instanceof Creeper) {
            return false;
        }
        if (par1EntityLiving instanceof Zombie) {
            return false;
        }
        if (par1EntityLiving instanceof Spider) {
            return false;
        }
        if (par1EntityLiving instanceof AbstractSkeleton) {
            return false;
        }
        if (par1EntityLiving instanceof Ghost) {
            return false;
        }
        if (par1EntityLiving instanceof GhostSkelly) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code isVillagerTarget} (:467-469): a living, visible villager. */
    private boolean isVillagerTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return par1EntityLiving != null && par1EntityLiving != this && par1EntityLiving.isAlive()
                && this.getSensing().hasLineOfSight(par1EntityLiving) && par1EntityLiving instanceof Villager;
    }

    /**
     * {@code doJumpDamage} (:471-517): every living entity in x/z {@code ±dist}, y ±10 around the point (sorted, not
     * Mobzilla, its head or a ghost) takes half the damage as an explosion and half as a fall, hears an explosion, and
     * with {@code knock} is flung 3.5 away from Mobzilla's centre and 0.75 up.
     *
     * <p>PORT: {@code DamageSource.setExplosionSource(null).setExplosion()} is {@code damageSources().explosion(null, null)}
     * (Cephadrome, PitchBlack precedent). {@code addVelocity} is {@code push} with {@code hurtMarked}, so a server player
     * receives the impulse (MonsterSupport precedent, W07).
     */
    @Nullable
    private LivingEntity doJumpDamage(final double X, final double Y, final double Z, final double dist, final double damage,
                                      final int knock) {
        final AABB bb = new AABB(X - dist, Y - 10.0, Z - dist, X + dist, Y + 10.0, Z + dist);
        final List<LivingEntity> var5 = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, bb));
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
            if (var8 instanceof Godzilla) {
                continue;
            }
            if (var8 instanceof GodzillaHead) {
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
            ArthropodSupport.playSoundAtEntity(var8, SoundEvents.GENERIC_EXPLODE.value(), 0.85f,
                    1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
            if (knock == 0) {
                continue;
            }
            final double ks = 3.5;
            final double inair = 0.75;
            final float f3 = (float) Math.atan2(var8.getZ() - this.getZ(), var8.getX() - this.getX());
            var8.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            var8.hurtMarked = true;
        }
        return null;
    }

    /**
     * {@code findSomethingToAttack} (:519-552): nothing with {@code PlayNicely} (and then no head is spawned); otherwise the
     * sorted living entities in {@code expand(64, 40, 64)}, a visible villager first, else the first suitable one. Notes
     * whether a head is in range.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            this.head_found = 1;
            return null;
        }
        LivingEntity ret = null;
        int vf = 0;
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(64.0, 40.0, 64.0)));
        var5.sort(this.TargetSorter);
        this.head_found = 0;
        for (final LivingEntity var8 : var5) {
            if (var8 instanceof GodzillaHead) {
                this.head_found = 1;
            }
            if (vf == 0 && this.isVillagerTarget(var8, false)) {
                ret = var8;
                vf = 1;
            }
            if (ret == null && vf == 0 && this.isSuitableTarget(var8, false)) {
                ret = var8;
            }
        }
        return ret;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:554-589), without {@code super}. Dark enough ({@code EntityMob}), night, Y at
     * least 50, no Mobzilla since the server started, a 1 in 40 roll, an empty 17x17 column from y+5 to y+14, no other
     * Mobzilla in {@code expand(64, 16, 64)}; success sets {@code godzilla_has_spawned}.
     *
     * <p>PORT: {@code worldObj.rand} is the spawn's random; the air test is {@code isAir()} (R22: cave and void air are
     * air). {@code godzilla_has_spawned} stays a JVM-wide static that only a restart clears, as in the original.
     */
    public static boolean checkGodzillaSpawnRules(final EntityType<Godzilla> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (OreSpawn.godzilla_has_spawned != 0) {
            return false;
        }
        if (random.nextInt(40) != 1) {
            return false;
        }
        for (int k = -8; k <= 8; ++k) {
            for (int j = -8; j <= 8; ++j) {
                for (int i = 5; i < 15; ++i) {
                    final BlockState bid = level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + i, pos.getZ() + k));
                    if (!bid.isAir()) {
                        return false;
                    }
                }
            }
        }
        if (!level.getEntitiesOfClass(Godzilla.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(64.0, 16.0, 64.0)).isEmpty()) {
            return false;
        }
        if (!level.isClientSide()) {
            OreSpawn.godzilla_has_spawned = 1;
        }
        return true;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkGodzillaSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getAttacking} (:591-593). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:595-597). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * A drop whose position is already drawn. 1.7.10 created the {@code EntityItem} (drawing the position) before the
     * caller enchanted the returned stack; the port draws the position first, lets the caller enchant, then adds the
     * entity, so both random streams keep their order and the client receives the enchanted stack (R18 case 4, Basilisk
     * precedent W07).
     */
    private final class Drop {
        final ItemStack is;
        private final double x;
        private final double y;
        private final double z;

        Drop(final ItemStack is, final double x, final double y, final double z) {
            this.is = is;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Adds the item entity. PORT: 1.7.10's {@code EntityItem} took its toss motion from {@code Math.random}; the
         * explicit-motion constructor keeps it there, so the world random is not drawn twice more per drop. A block without
         * an item ({@code Item.getItemFromBlock} was {@code null}, e.g. air in the crush zone) made a stack with a
         * {@code null} item in 1.7.10; the port spawns nothing for it (R18 case 1).
         */
        void spawn() {
            if (this.is.isEmpty()) {
                return;
            }
            final Level world = Godzilla.this.level();
            final ItemEntity var3 = new ItemEntity(world, this.x, this.y, this.z, this.is,
                    (double) (float) (Math.random() * 0.20000000298023224 - 0.10000000149011612), 0.20000000298023224,
                    (double) (float) (Math.random() * 0.20000000298023224 - 0.10000000149011612));
            world.addFreshEntity(var3);
        }
    }

    /** {@code dropItemRand} (:599-607): ±9 around the body on {@code OreSpawnRand}, y +4..+13 on the world random. */
    private Drop dropItemRand(final ItemLike index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        final double x = this.getX() + OreSpawn.OreSpawnRand.nextInt(10) - OreSpawn.OreSpawnRand.nextInt(10);
        final double y = this.getY() + 4.0 + this.level().random.nextInt(10);
        final double z = this.getZ() + OreSpawn.OreSpawnRand.nextInt(10) - OreSpawn.OreSpawnRand.nextInt(10);
        return new Drop(is, x, y, z);
    }

    /** {@code dropItemRandAt} (:609-617): ±9 around (dx, dz) on {@code OreSpawnRand}, y +4..+9 on the world random. */
    private Drop dropItemRandAt(final ItemLike index, final int par1, final double dx, final double dz) {
        final ItemStack is = new ItemStack(index, par1);
        final double x = dx + OreSpawn.OreSpawnRand.nextInt(10) - OreSpawn.OreSpawnRand.nextInt(10);
        final double y = this.getY() + 4.0 + this.level().random.nextInt(6);
        final double z = dz + OreSpawn.OreSpawnRand.nextInt(10) - OreSpawn.OreSpawnRand.nextInt(10);
        return new Drop(is, x, y, z);
    }

    /**
     * {@code isCrushable} (:619-621): {@code mobGriefing}, and none of grass, dirt, stone, farmland, water, lava, bedrock,
     * obsidian, sand, gravel, the iron/diamond/emerald/gold blocks, netherrack, end stone, OreSpawn's amethyst, ruby, uranium
     * and titanium blocks, crystal stone and crystal grass.
     *
     * <p>PORT (R22): the categories cover their 1.21.1 members - stone is {@code #minecraft:base_stone_overworld} (granite,
     * diorite, andesite, tuff, deepslate), dirt is {@code #minecraft:dirt} (the grass block keeps its own branch in
     * {@link #crushLayer}, which it reaches because it is excluded here), sand is {@code #minecraft:sand}, netherrack is
     * {@code #minecraft:base_stone_nether} (basalt, blackstone). Named blocks stay single: bedrock, obsidian, gravel, end
     * stone, the four metal blocks. Water and lava are their source/flowing blocks, as {@code water}/{@code flowing_water}.
     */
    private boolean isCrushable(final BlockState bid, final boolean griefing) {
        return griefing && !bid.is(Blocks.GRASS_BLOCK) && !bid.is(BlockTags.DIRT) && !bid.is(BlockTags.BASE_STONE_OVERWORLD)
                && !bid.is(Blocks.FARMLAND) && !bid.is(Blocks.WATER) && !bid.is(Blocks.LAVA) && !bid.is(Blocks.BEDROCK)
                && !bid.is(Blocks.OBSIDIAN) && !bid.is(BlockTags.SAND) && !bid.is(Blocks.GRAVEL) && !bid.is(Blocks.IRON_BLOCK)
                && !bid.is(Blocks.DIAMOND_BLOCK) && !bid.is(Blocks.EMERALD_BLOCK) && !bid.is(Blocks.GOLD_BLOCK)
                && !bid.is(BlockTags.BASE_STONE_NETHER) && !bid.is(Blocks.END_STONE) && !bid.is(ModBlocks.BLOCKAMETHYST.get())
                && !bid.is(ModBlocks.BLOCKRUBY.get()) && !bid.is(ModBlocks.BLOCKURANIUM.get())
                && !bid.is(ModBlocks.BLOCKTITANIUM.get()) && !bid.is(ModBlocks.CRYSTALSTONE.get())
                && !bid.is(ModBlocks.CRYSTAL_GRASS.get());
    }

    /**
     * {@code firecanon} (:623-651): 22 blocks ahead of the body at y+19, one big {@link BetterFireball} at the target's middle
     * with the fuse sound, then five scattered ones (±5/±3/±5, half of them small) with the bow sound; one salvo off
     * {@code stream_count}.
     */
    private void firecanon(final LivingEntity e) {
        final double yoff = 19.0;
        final double xzoff = 22.0;
        BetterFireball bf = null;
        final Level world = this.level();
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        if (this.stream_count > 0) {
            bf = new BetterFireball(world, this, e.getX() - cx, e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff),
                    e.getZ() - cz);
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setBig();
            ArthropodSupport.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f,
                    1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            world.addFreshEntity(bf);
            for (int i = 0; i < 5; ++i) {
                final float r1 = 5.0f * (world.random.nextFloat() - world.random.nextFloat());
                final float r2 = 3.0f * (world.random.nextFloat() - world.random.nextFloat());
                final float r3 = 5.0f * (world.random.nextFloat() - world.random.nextFloat());
                bf = new BetterFireball(world, this, e.getX() - cx + r1,
                        e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff) + r2, e.getZ() - cz + r3);
                bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
                bf.setPos(cx, this.getY() + yoff, cz);
                if (world.random.nextInt(2) == 1) {
                    bf.setSmall();
                }
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f,
                        1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                world.addFreshEntity(bf);
            }
            --this.stream_count;
        }
    }

    /**
     * {@code attackEntityAsMob} (:653-688): a large living target ({@code height * width > 30}, not royalty, Mobzilla, its
     * head, the Nightmare or the Kraken) loses half its health and takes {@code attack * 10} mob damage, and Mobzilla notes a
     * large unknown; the Ender Dragon takes {@code attack / 2} explosion damage on the head (1 in 6) or the body; then the
     * vanilla hit, and on success a fling of 3.2 away and 0.3 up (doubled for players and removed entities).
     *
     * <p>PORT: {@code attackEntityFromPart(dragonPartBody, ...)} - the body part is private in 1.21.1 and reached through
     * {@code getSubEntities()} (PitchBlack precedent, W08); the explosion source passes
     * {@code #minecraft:always_hurts_ender_dragons}, which contains {@code #minecraft:is_explosion}
     * ({@code DamageTypeTagsProvider}:77, :90). {@code setHealth(getHealth() / 2)} halves virtual health too, because the
     * attribute scale is proportional (R4).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final int attack = MobStats.Godzilla_stats().attack();
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final float s = par1Entity.getBbHeight() * par1Entity.getBbWidth();
            if (s > 30.0f && !MyUtils.isRoyalty(par1Entity) && !(par1Entity instanceof Godzilla)
                    && !(par1Entity instanceof GodzillaHead) && !(par1Entity instanceof PitchBlack)
                    && !(par1Entity instanceof Kraken)) {
                final LivingEntity e = (LivingEntity) par1Entity;
                e.setHealth(e.getHealth() / 2.0f);
                e.hurt(this.damageSources().mobAttack(this), attack * 10.0f);
                this.large_unknown_detected = 1;
            }
        }
        if (par1Entity != null && par1Entity instanceof EnderDragon dr) {
            DamageSource var21 = null;
            var21 = this.damageSources().explosion(null, null);
            if (this.level().random.nextInt(6) == 1) {
                dr.hurt(dr.head, var21, attack / 2.0f);
            } else {
                dr.hurt(dragonBody(dr), var21, attack / 2.0f);
            }
        }
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                final double ks = 3.2;
                double inair = 0.3;
                final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
                if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                    inair *= 2.0;
                }
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
                par1Entity.hurtMarked = true;
            }
            return true;
        }
        return false;
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
     * {@code attackEntityFrom} (:690-721), in original units: nothing while {@code hurt_timer} runs; the hit capped at 750; a
     * large living attacker (same exceptions as {@link #doHurtTarget}) deals a tenth and marks a large unknown
     * ({@code hurt_timer = 50}, which the non-cactus branch overwrites with 20 right away); cactus is ignored; otherwise the
     * vanilla hit, 20 ticks of {@code hurt_timer}, and any living attacker but Mobzilla or its head becomes the target and is
     * walked to at 1.2.
     *
     * <p>R4 and R5 run inside {@code super.hurt} ({@code CombatEvents}), after the cap, as the ruling asks.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float dm = par2;
        float s = 0.0f;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (dm > 750.0f) {
            dm = 750.0f;
        }
        Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof LivingEntity enl) {
            s = enl.getBbHeight() * enl.getBbWidth();
            if (s > 30.0f && !MyUtils.isRoyalty(enl) && !(enl instanceof Godzilla) && !(enl instanceof GodzillaHead)
                    && !(enl instanceof PitchBlack) && !(enl instanceof Kraken)) {
                dm /= 10.0f;
                this.hurt_timer = 50;
                this.large_unknown_detected = 1;
            }
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, dm);
            this.hurt_timer = 20;
            e = par1DamageSource.getEntity();
            if (e != null && e instanceof LivingEntity living && !(e instanceof GodzillaHead) && !(e instanceof Godzilla)) {
                this.setTarget(living);
                this.getNavigation().moveTo(e, 1.2);
            }
        }
        return ret;
    }

    /** {@code onStruckByLightning} (:723-724): empty, no damage and no fire. */
    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightning) {
    }

    /**
     * {@code doLightningAttack} (:726-744): 100 mob damage, 5 seconds of fire, the particle loop, an explosion sound, an
     * explosion of 3 at the target (block damage under {@code mobGriefing}) and two lightning bolts, one at the target and
     * one 15 blocks above Mobzilla.
     *
     * <p>The particles are {@code World.spawnParticle} on the server, which 1.7.10's {@code WorldManager} ignored; 1.21.1's
     * {@code Level.addParticle} is empty on a server level as well. The calls stay so that their random draws happen in the
     * original order. PORT: {@code createExplosion(this, ..., mobGriefing)} is {@code explode} with
     * {@link Level.ExplosionInteraction#MOB}, which asks the same rule; {@code addWeatherEffect(new EntityLightningBolt)} is
     * an added {@code LightningBolt} (ThunderBolt precedent, W04).
     */
    private void doLightningAttack(@Nullable final LivingEntity e) {
        if (e == null) {
            return;
        }
        final Level world = this.level();
        final float var2 = 100.0f;
        e.hurt(this.damageSources().mobAttack(this), var2);
        e.igniteForSeconds(5.0f);
        for (int var3 = 0; var3 < 20; ++var3) {
            world.addParticle(ParticleTypes.SMOKE, e.getX() + this.random.nextFloat() - this.random.nextFloat(),
                    e.getY() + this.random.nextFloat() - this.random.nextFloat(), e.getZ() + this.random.nextFloat(), 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.LARGE_SMOKE, e.getX() + this.random.nextFloat() - this.random.nextFloat(),
                    e.getY() + this.random.nextFloat() - this.random.nextFloat(),
                    e.getZ() + this.random.nextFloat() - this.random.nextFloat(), 0.0, 0.0, 0.0);
            world.addParticle(ParticleTypes.FIREWORK, e.getX(), e.getY(), e.getZ(), world.random.nextGaussian(),
                    world.random.nextGaussian(), world.random.nextGaussian());
        }
        ArthropodSupport.playSoundAtEntity(e, SoundEvents.GENERIC_EXPLODE.value(), 0.5f,
                1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.5f);
        if (!world.isClientSide) {
            world.explode(this, e.getX(), e.getY(), e.getZ(), 3.0f, Level.ExplosionInteraction.MOB);
        }
        this.addLightning(e.getX(), e.getY() + 1.0, e.getZ());
        this.addLightning(this.getX(), this.getY() + 15.0, this.getZ());
    }

    /** {@code addWeatherEffect(new EntityLightningBolt(world, x, y, z))}: placed with yaw and pitch 0. */
    private void addLightning(final double x, final double y, final double z) {
        final LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(this.level());
        if (bolt != null) {
            bolt.moveTo(x, y, z, 0.0f, 0.0f);
            this.level().addFreshEntity(bolt);
        }
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    // ------------------------------------------------------------------ enchantment blocks of the drop table

    private void ench(final ItemStack is, final ResourceKey<Enchantment> key, final int lvl) {
        // PORT: 1.7.10 appended a second NBT entry for the second Sharpness roll; PreEnchant.add sums with an existing
        // entry, the form W07's boss tables use (ArthropodSupport.add). Levels above the vanilla maximum stay as rolled.
        PreEnchant.add(is, this.level(), key, lvl);
    }

    /** S (:774-797): seven rolls on the world random, Sharpness twice. */
    private void enchantSword(final ItemStack is) {
        final RandomSource rand = this.level().random;
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.SHARPNESS, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.BANE_OF_ARTHROPODS, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.KNOCKBACK, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.LOOTING, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(2) == 1) {
            this.ench(is, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.FIRE_ASPECT, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.SHARPNESS, 1 + rand.nextInt(5));
        }
    }

    /** T (:800-808): Unbreaking 1/2 (2-5), Efficiency 1/6 (1-5). */
    private void enchantTool(final ItemStack is) {
        final RandomSource rand = this.level().random;
        if (rand.nextInt(2) == 1) {
            this.ench(is, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.EFFICIENCY, 1 + rand.nextInt(5));
        }
    }

    /** P (:811-822): like T, plus Fortune 1/6 (1-5). */
    private void enchantPickaxe(final ItemStack is) {
        final RandomSource rand = this.level().random;
        if (rand.nextInt(2) == 1) {
            this.ench(is, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.EFFICIENCY, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.FORTUNE, 1 + rand.nextInt(5));
        }
    }

    /** The four protections of H and B, each 1/6 (1-5), in the original order. */
    private void enchantProtections(final ItemStack is, final RandomSource rand) {
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.BLAST_PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.FIRE_PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.PROJECTILE_PROTECTION, 1 + rand.nextInt(5));
        }
    }

    /**
     * H (:1013-1036): four protections, Unbreaking 1/2 (2-5), Respiration 1/6 (1 to {@code respirationBound}), Aqua Affinity
     * 1/6 (1-5). Only the diamond helmet (:864) rolls Respiration with {@code nextInt(2)}; every other helmet with 5.
     */
    private void enchantHelmet(final ItemStack is, final int respirationBound) {
        final RandomSource rand = this.level().random;
        this.enchantProtections(is, rand);
        if (rand.nextInt(2) == 1) {
            this.ench(is, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.RESPIRATION, 1 + rand.nextInt(respirationBound));
        }
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.AQUA_AFFINITY, 1 + rand.nextInt(5));
        }
    }

    /** B (:873-890): four protections, Unbreaking 1/2 (2-5). */
    private void enchantBody(final ItemStack is) {
        final RandomSource rand = this.level().random;
        this.enchantProtections(is, rand);
        if (rand.nextInt(2) == 1) {
            this.ench(is, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
    }

    /** F (:913-921): Feather Falling 1/6 (5-9), Unbreaking 1/2 (2-5). */
    private void enchantBoots(final ItemStack is) {
        final RandomSource rand = this.level().random;
        if (rand.nextInt(6) == 1) {
            this.ench(is, Enchantments.FEATHER_FALLING, 5 + rand.nextInt(5));
        }
        if (rand.nextInt(2) == 1) {
            this.ench(is, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
    }

    /** One drop with an enchantment block: position drawn, stack enchanted, entity added. */
    private void dropEnchanted(final ItemLike item, final java.util.function.Consumer<ItemStack> enchant) {
        final Drop d = this.dropItemRand(item, 1);
        enchant.accept(d.is);
        d.spawn();
    }

    /**
     * {@code dropFewItems} (:746-1813): an item frame, 50-79 Mobzilla scales, 100-259 beef, 50-109 bones, then 25-39 rolls of
     * {@code nextInt(80)} on the world random over the table in verhalten/entity-03.md (cases 72 and 76-79 drop nothing).
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        final Level world = this.level();
        this.dropItemRand(Items.ITEM_FRAME, 1).spawn();
        for (int var5 = 50 + world.random.nextInt(30), var6 = 0; var6 < var5; ++var6) {
            this.dropItemRand(ModItems.GODZILLA_SCALE.get(), 1).spawn();
        }
        for (int var5 = 100 + world.random.nextInt(160), var6 = 0; var6 < var5; ++var6) {
            this.dropItemRand(Items.BEEF, 1).spawn();
        }
        for (int var5 = 50 + world.random.nextInt(60), var6 = 0; var6 < var5; ++var6) {
            this.dropItemRand(Items.BONE, 1).spawn();
        }
        for (int i = 25 + world.random.nextInt(15), var6 = 0; var6 < i; ++var6) {
            final int var7 = world.random.nextInt(80);
            switch (var7) {
                case 0 -> this.dropItemRand(ModItems.ULTIMATE_SWORD.get(), 1).spawn();
                case 1 -> this.dropItemRand(Items.DIAMOND, 1).spawn();
                case 2 -> this.dropItemRand(Blocks.DIAMOND_BLOCK, 1).spawn();
                case 3 -> this.dropEnchanted(Items.DIAMOND_SWORD, this::enchantSword);
                case 4 -> this.dropEnchanted(Items.DIAMOND_SHOVEL, this::enchantTool);
                case 5 -> this.dropEnchanted(Items.DIAMOND_PICKAXE, this::enchantPickaxe);
                case 6 -> this.dropEnchanted(Items.DIAMOND_AXE, this::enchantTool);
                case 7 -> this.dropEnchanted(Items.DIAMOND_HOE, this::enchantTool);
                case 8 -> this.dropEnchanted(Items.DIAMOND_HELMET, is -> this.enchantHelmet(is, 2));
                case 9 -> this.dropEnchanted(Items.DIAMOND_CHESTPLATE, this::enchantBody);
                case 10 -> this.dropEnchanted(Items.DIAMOND_LEGGINGS, this::enchantBody);
                case 11 -> this.dropEnchanted(Items.DIAMOND_BOOTS, this::enchantBoots);
                case 12 -> this.dropItemRand(ModItems.ULTIMATE_BOW.get(), 1).spawn();
                case 13 -> this.dropItemRand(ModItems.ULTIMATE_AXE.get(), 1).spawn();
                case 14 -> this.dropItemRand(Items.IRON_INGOT, 1).spawn();
                case 15 -> this.dropItemRand(ModItems.ULTIMATE_PICKAXE.get(), 1).spawn();
                case 16 -> this.dropEnchanted(Items.IRON_SWORD, this::enchantSword);
                case 17 -> this.dropEnchanted(Items.IRON_SHOVEL, this::enchantTool);
                case 18 -> this.dropEnchanted(Items.IRON_PICKAXE, this::enchantPickaxe);
                case 19 -> this.dropEnchanted(Items.IRON_AXE, this::enchantTool);
                case 20 -> this.dropEnchanted(Items.IRON_HOE, this::enchantTool);
                case 21 -> this.dropEnchanted(Items.IRON_HELMET, is -> this.enchantHelmet(is, 5));
                case 22 -> this.dropEnchanted(Items.IRON_CHESTPLATE, this::enchantBody);
                case 23 -> this.dropEnchanted(Items.IRON_LEGGINGS, this::enchantBody);
                case 24 -> this.dropEnchanted(Items.IRON_BOOTS, this::enchantBoots);
                case 25 -> this.dropItemRand(ModItems.ULTIMATE_SHOVEL.get(), 1).spawn();
                case 26 -> this.dropItemRand(Blocks.IRON_BLOCK, 1).spawn();
                case 27 -> this.dropItemRand(Items.GOLD_NUGGET, 1).spawn();
                case 28 -> this.dropItemRand(Items.GOLD_INGOT, 1).spawn();
                case 29 -> this.dropItemRand(Items.GOLDEN_CARROT, 1).spawn();
                case 30 -> this.dropEnchanted(Items.GOLDEN_SWORD, this::enchantSword);
                case 31 -> this.dropEnchanted(Items.GOLDEN_SHOVEL, this::enchantTool);
                case 32 -> this.dropEnchanted(Items.GOLDEN_PICKAXE, this::enchantPickaxe);
                case 33 -> this.dropEnchanted(Items.GOLDEN_AXE, this::enchantTool);
                case 34 -> this.dropEnchanted(Items.GOLDEN_HOE, this::enchantTool);
                case 35 -> this.dropEnchanted(Items.GOLDEN_HELMET, is -> this.enchantHelmet(is, 5));
                case 36 -> this.dropEnchanted(Items.GOLDEN_CHESTPLATE, this::enchantBody);
                case 37 -> this.dropEnchanted(Items.GOLDEN_LEGGINGS, this::enchantBody);
                case 38 -> this.dropEnchanted(Items.GOLDEN_BOOTS, this::enchantBoots);
                case 39 -> this.dropItemRand(Items.GOLDEN_APPLE, 1).spawn();
                case 40 -> this.dropItemRand(Blocks.GOLD_BLOCK, 1).spawn();
                case 41 -> {
                    // new ItemStack(golden_apple, 1, 1) at ±2 x/z on OreSpawnRand, y+1 (:1267-1275).
                    final ItemStack is = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1);
                    final double x = this.getX() + OreSpawn.OreSpawnRand.nextInt(3) - OreSpawn.OreSpawnRand.nextInt(3);
                    final double y = this.getY() + 1.0;
                    final double z = this.getZ() + OreSpawn.OreSpawnRand.nextInt(3) - OreSpawn.OreSpawnRand.nextInt(3);
                    new Drop(is, x, y, z).spawn();
                }
                case 42 -> this.dropEnchanted(ModItems.EXPERIENCE_SWORD.get(), this::enchantSword);
                case 43 -> this.dropEnchanted(ModItems.EXPERIENCE_HELMET.get(), is -> this.enchantHelmet(is, 5));
                case 44 -> this.dropEnchanted(ModItems.EXPERIENCE_CHEST.get(), this::enchantBody);
                case 45 -> this.dropEnchanted(ModItems.EXPERIENCE_LEGGINGS.get(), this::enchantBody);
                case 46 -> this.dropEnchanted(ModItems.EXPERIENCE_BOOTS.get(), this::enchantBoots);
                case 47 -> this.dropEnchanted(ModItems.AMETHYST_SWORD.get(), this::enchantSword);
                case 48 -> this.dropEnchanted(ModItems.AMETHYST_SHOVEL.get(), this::enchantTool);
                case 49 -> this.dropEnchanted(ModItems.AMETHYST_PICKAXE.get(), this::enchantPickaxe);
                case 50 -> this.dropEnchanted(ModItems.AMETHYST_AXE.get(), this::enchantTool);
                case 51 -> this.dropEnchanted(ModItems.AMETHYST_HOE.get(), this::enchantTool);
                case 52 -> this.dropItemRand(ModBlocks.BLOCKAMETHYST.get(), 1).spawn();
                case 53 -> this.dropEnchanted(ModItems.AMETHYST_HELMET.get(), is -> this.enchantHelmet(is, 5));
                case 54 -> this.dropEnchanted(ModItems.AMETHYST_CHEST.get(), this::enchantBody);
                case 55 -> this.dropEnchanted(ModItems.AMETHYST_LEGGINGS.get(), this::enchantBody);
                case 56 -> this.dropEnchanted(ModItems.AMETHYST_BOOTS.get(), this::enchantBoots);
                case 57 -> this.dropEnchanted(ModItems.RUBY_HELMET.get(), is -> this.enchantHelmet(is, 5));
                case 58 -> this.dropEnchanted(ModItems.RUBY_CHEST.get(), this::enchantBody);
                case 59 -> this.dropEnchanted(ModItems.RUBY_LEGGINGS.get(), this::enchantBody);
                case 60 -> this.dropEnchanted(ModItems.RUBY_BOOTS.get(), this::enchantBoots);
                case 61 -> this.dropEnchanted(ModItems.RUBY_SWORD.get(), this::enchantSword);
                case 62 -> this.dropEnchanted(ModItems.RUBY_SHOVEL.get(), this::enchantTool);
                case 63 -> this.dropEnchanted(ModItems.RUBY_PICKAXE.get(), this::enchantPickaxe);
                case 64 -> this.dropEnchanted(ModItems.RUBY_AXE.get(), this::enchantTool);
                case 65 -> this.dropEnchanted(ModItems.RUBY_HOE.get(), this::enchantTool);
                case 66 -> this.dropItemRand(ModBlocks.BLOCKRUBY.get(), 1).spawn();
                case 67 -> this.dropEnchanted(ModItems.ULTIMATE_HELMET.get(), is -> this.enchantHelmet(is, 5));
                case 68 -> this.dropEnchanted(ModItems.ULTIMATE_CHEST.get(), this::enchantBody);
                case 69 -> this.dropEnchanted(ModItems.ULTIMATE_LEGGINGS.get(), this::enchantBody);
                case 70 -> this.dropEnchanted(ModItems.ULTIMATE_BOOTS.get(), this::enchantBoots);
                case 71 -> this.dropEnchanted(ModItems.ULTIMATE_SHOVEL.get(), this::enchantTool);
                case 73 -> this.dropEnchanted(ModItems.ULTIMATE_PICKAXE.get(), this::enchantPickaxe);
                case 74 -> this.dropEnchanted(ModItems.ULTIMATE_AXE.get(), this::enchantTool);
                case 75 -> this.dropEnchanted(ModItems.ULTIMATE_HOE.get(), this::enchantTool);
                default -> {
                }
            }
        }
    }

    /** {@code OreSpawnMain.Godzilla_stats.attack}, for GameTests and for the head. */
    public static int attackValue() {
        return MobStats.Godzilla_stats().attack();
    }

    /** The large-unknown flag (:32), exposed for GameTests only. */
    public int getLargeUnknownDetected() {
        return this.large_unknown_detected;
    }

    /** The hit timer (:24), exposed for GameTests only. */
    public int getHurtTimer() {
        return this.hurt_timer;
    }

    /** Clears the hit timer; for GameTests only, so a test need not wait 20 ticks between hits. */
    public void setHurtTimer(final int hurtTimer) {
        this.hurt_timer = hurtTimer;
    }
}
