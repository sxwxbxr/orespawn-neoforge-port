package com.swbr.orespawn.entity.terror;

import com.swbr.orespawn.entity.pet.RubberDucky;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.cannonfodder.Lizard;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Crab} (Crab.java:18-516), id {@code crab} ("Crab", 64/1/false): a robot crab of three
 * random sizes that dries out on land and heals in water (verhalten/entity-06.md, design-entities-02.md).
 *
 * <p>Values, all tied to the crab scale (DataWatcher 21 = scale x 100, NBT {@code Fscale}):
 * <ul>
 *   <li>scale rolled in {@code entityInit}: 0.25; {@code rand(4) == 1} 0.5; then independently {@code rand(8) == 2} 1.0
 *       (:63-80); a "Crab" spawner makes it 0.35 (:488-490);</li>
 *   <li>maximum health {@code (int)(PitchBlack_stats.health * scale)} - the Nightmare's stat, not {@code Crab_stats}
 *       (:127-129); fixed in the constructor, so the spawner's 0.35 never changes it (bug kept, R18);</li>
 *   <li>attack attribute and hit {@code Crab_stats.attack * scale} (:56, :201), armor
 *       {@code Crab_stats.defense + (int)(2 * scale)} (:131-133), speed {@code 0.55 * scale} on land and
 *       {@code 0.95 * scale} in water, set every tick (:115-122);</li>
 *   <li>XP 150 and {@code fireResistance} 30 from the constructor (:40-41), which runs after {@code entityInit}'s
 *       {@code 400 * t} / {@code 10 * t}; after loading from NBT {@code 400 * scale} / {@code 10 * scale} (:102-103).</li>
 * </ul>
 *
 * <p>Eight ticks of own invulnerability after an accepted hit (:220-223), cactus immune (:216-218), breathes under water
 * (:513-515; the {@code can_breathe_under_water} entity tag, W06 precedent). DataWatcher 20 = attacking.
 *
 * <p>Not carried over: {@code getLivingSound}/{@code getDeathSound} return {@code null} (:147-157, silent);
 * {@code getDropItem} (:167-169) is dead because {@code dropFewItems} is overridden; {@code findBuddies} is the spawn
 * rule's; {@code initCreature} (:193-194) overrides nothing; {@code interact} (:196-198) returns {@code false};
 * {@code onLivingUpdate} (:139-141) only calls {@code super}.
 */
public class Crab extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:61): {@code ModelCrab} opens eyes, mouth and claws while it is 1. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Crab.class, EntityDataSerializers.INT);
    /** DataWatcher 21 (:62, :86-96): the crab scale times 100 as an int. */
    private static final EntityDataAccessor<Integer> DATA_SCALE =
            SynchedEntityData.defineId(Crab.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    private int hurt_timer;
    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;
    /** {@code Entity.fireResistance}, written by the constructor and {@code readEntityFromNBT}. */
    private int fireResistance;
    /**
     * Which {@code setSize} was the last one: 0 the constructor's 1.25 x 2.5 (the type's size, R9), 1 the NBT load's
     * {@code 3.75 * scale x 3.5 * scale} (:101), 2 {@code onUpdate}'s {@code 2.5 * scale x 3.5 * scale} (:124).
     */
    private int sizeMode;
    /** The scale {@link #sizeMode} was computed with. */
    private float sizeScale;

    /** {@code Crab(World)} (:28-50), after {@code entityInit} ran in the Entity constructor. */
    public Crab(final EntityType<? extends Crab> type, final Level par1World) {
        super(type, par1World);
        // entityInit (:59-84) ran inside the Entity constructor, before everything below.
        this.rollScale();
        this.TargetSorter = null;
        this.hurt_timer = 0;
        this.moveSpeed = 0.55f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        this.moveSpeed = 0.55f;
        // setSize(1.25f, 2.5f) (:38) is the entity type's size (R9) and overwrites entityInit's setSize (:83).
        this.sizeMode = 0;
        // PORT: getNavigator().setAvoidsWater(false) (:39) is a water path malus of 0 (vanilla default 8, W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 150;
        this.fireResistance = 30;
        // isImmuneToFire = false (:42) is the type default.
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 10.0f));
        // EntityLiving.class is Mob in 1.21.1.
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // applyEntityAttributes (:52-57) ran in the EntityLivingBase constructor, after entityInit rolled the scale.
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) (this.moveSpeed * this.getCrabScale()));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) (MobStats.Crab_stats().attack() * this.getCrabScale()));
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) this.getLegacyArmorValue());
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code applyEntityAttributes} (:52-57) at registration time (R3) for scale 1; every instance writes its own scaled
     * values in the constructor.
     */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats pitchBlack = MobStats.PitchBlack_stats(StatSource.EARLY);
        final MobStats stats = MobStats.Crab_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) pitchBlack.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.55f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) (stats.defense() + 2));
    }

    /**
     * {@code entityInit} (:59-84), watcher part: DataWatcher 20 and 21, both defined as 0. The scale roll of the same
     * method is {@link #rollScale()}, called first thing in the constructor.
     *
     * <p>PORT: the roll cannot go into the definition. {@code ServerEntity} sends only values that differ from the
     * defined one when a client starts tracking the entity ({@code getNonDefaultValues}); a rolled value defined as the
     * default would never reach the client, which rolls its own. 1.7.10 sent every watched object in the spawn packet.
     * Every rolled scale is non-zero, so it always differs from the default and always reaches the client.
     */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_SCALE, 0);
    }

    /**
     * The scale roll of {@code entityInit} (:64-80) on the world random, written with {@code setCrabScale(t)} (:80).
     *
     * <p>Its writes of {@code experienceValue}, {@code fireResistance} and {@code setSize} are overwritten by the
     * constructor right after (see the class comment) and are not repeated here. The {@code worldObj == null} branch
     * with {@code OreSpawnRand} stays for the letter of the original; a 1.21.1 entity always has its level here.
     */
    private void rollScale() {
        float t = 0.25f;
        if (this.level() != null) {
            if (this.level().random.nextInt(4) == 1) {
                t = 0.5f;
            }
            if (this.level().random.nextInt(8) == 2) {
                t = 1.0f;
            }
        } else {
            if (OreSpawn.OreSpawnRand.nextInt(4) == 1) {
                t = 0.5f;
            }
            if (OreSpawn.OreSpawnRand.nextInt(8) == 2) {
                t = 1.0f;
            }
        }
        this.setCrabScale(t);
    }

    /** {@code getCrabScale} (:86-90). */
    public float getCrabScale() {
        final int i = this.entityData.get(DATA_SCALE);
        final float f = (float) i;
        return f / 100.0f;
    }

    /** {@code setCrabScale} (:92-96). */
    public void setCrabScale(final float par1) {
        final float f = par1 * 100.0f;
        final int i = (int) f;
        this.entityData.set(DATA_SCALE, i);
    }

    /**
     * {@code setSize} as dimensions (R9): see {@link #sizeMode}.
     *
     * <p>PORT: when the box grows, 1.21.1's {@code refreshDimensions} moves the crab to a free spot
     * ({@code fudgePositionAfterSizeChange}); 1.7.10 {@code setSize} instead ran {@code moveEntity(old - new, 0,
     * old - new)} on the server. Both only nudge the entity out of blocks.
     */
    @Override
    protected EntityDimensions getDefaultDimensions(final Pose pose) {
        if (this.sizeMode == 1) {
            final float s = this.getCrabScale();
            return EntityDimensions.scalable(3.75f * s, 3.5f * s);
        }
        if (this.sizeMode == 2) {
            final float s = this.getCrabScale();
            return EntityDimensions.scalable(2.5f * s, 3.5f * s);
        }
        return super.getDefaultDimensions(pose);
    }

    /** {@code setSize(w, h)}: switch to {@code mode} and refresh when mode or scale changed. */
    private void setSizeMode(final int mode) {
        final float s = this.getCrabScale();
        if (this.sizeMode != mode || this.sizeScale != s) {
            this.sizeMode = mode;
            this.sizeScale = s;
            this.refreshDimensions();
        }
    }

    /** {@code readEntityFromNBT} (:98-104). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setCrabScale(par1NBTTagCompound.getFloat("Fscale"));
        this.setSizeMode(1);
        this.xpReward = (int) (400.0f * this.getCrabScale());
        this.fireResistance = (int) (10.0f * this.getCrabScale());
    }

    /** {@code writeEntityToNBT} (:106-109). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putFloat("Fscale", this.getCrabScale());
    }

    /** {@code fireResistance} (:41, :103). */
    @Override
    protected int getFireImmuneTicks() {
        return this.fireResistance;
    }

    /** {@code canDespawn} (:111-113): not persistent. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:115-125): water speed, the vanilla tick, then the per-tick {@code setSize}. Both sides. */
    @Override
    public void tick() {
        if (this.isInWater()) {
            this.moveSpeed = 0.95f;
        } else {
            this.moveSpeed = 0.55f;
        }
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) (this.moveSpeed * this.getCrabScale()));
        super.tick();
        this.setSizeMode(2);
    }

    /** {@code mygetMaxHealth} (:127-129): the Nightmare's health stat times the scale. */
    public int mygetMaxHealth() {
        return (int) (MobStats.PitchBlack_stats().health() * this.getCrabScale());
    }

    /** {@code getTotalArmorValue} (:131-133), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Crab_stats().defense() + (int) (2.0f * this.getCrabScale());
    }

    /** {@code getCrabHealth} (:143-145). */
    public int getCrabHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:147-149): none. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:151-153). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LEAVES_HIT.get();
    }

    /** {@code getDeathSound} (:155-157): none. */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code getSoundVolume} (:159-161). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:163-165): {@code 2.0 - 0.3 / scale}. */
    @Override
    public float getVoicePitch() {
        return 2.0f - 0.3f * (1.0f / this.getCrabScale());
    }

    /** {@code dropItemRand} (:171-179): up to one block off on {@code OreSpawnRand}, one block up. */
    private ItemStack dropItemRand(final Item index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        ArthropodSupport.dropItemRand(this, is, 2);
        return is;
    }

    /**
     * {@code dropFewItems} (:181-191), then {@code dropEquipment}: {@code (4 + rand(8)) * (int) scale} raw crab meat, at
     * least one - so only scale 1.0 drops more than one.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        int var5 = 4 + this.level().random.nextInt(8);
        var5 *= (int) this.getCrabScale();
        if (var5 < 1) {
            var5 = 1;
        }
        for (int var6 = 0; var6 < var5; ++var6) {
            this.dropItemRand(ModItems.CRAB_MEAT.get(), 1);
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code attackEntityAsMob} (:200-212): {@code Crab_stats.attack * scale} mob damage; on a hit a push of
     * {@code 1.15 * scale} away and {@code 0.48 * scale} up, doubled up for a removed entity or a player.
     *
     * <p>PORT: {@code addVelocity} is {@link Entity#push(double, double, double)}; a server player only receives the
     * impulse through {@code hurtMarked} (MonsterSupport.legacyKnockback, W07).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), MobStats.Crab_stats().attack() * this.getCrabScale());
        if (var4 && par1Entity != null && par1Entity instanceof LivingEntity) {
            final double ks = 1.15 * this.getCrabScale();
            double inair = 0.48 * this.getCrabScale();
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            par1Entity.hurtMarked = true;
        }
        return var4;
    }

    /**
     * {@code attackEntityFrom} (:214-233): cactus is ignored; damage only goes through while {@code hurt_timer} is 0,
     * which then blocks the next 8 AI ticks; an attacking {@code EntityLiving} (a {@link Mob}, not a player) other than
     * a crab becomes the target and is walked to at 1.2 - even when the hit was blocked.
     *
     * <p>PORT: {@code setTarget(e)} (:229) wrote {@code EntityCreature.entityToAttack}, which the task AI of this class
     * never reads; 1.21.1 has no such field.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.CACTUS)) {
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (this.hurt_timer <= 0) {
            ret = super.hurt(par1DamageSource, par2);
            this.hurt_timer = 8;
        }
        if (e != null && e instanceof Mob) {
            if (e instanceof Crab) {
                return false;
            }
            this.setTarget((LivingEntity) e);
            this.getNavigation().moveTo(e, 1.2);
        }
        return ret;
    }

    /** {@code Blocks.water || Blocks.flowing_water}: both are the one water block in 1.21.1. */
    private boolean isWater(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /**
     * {@code scan_it} (:235-316): the six faces of the box {@code (x ± dx, y ± dy, z ± dz)}; every water block closer
     * (by the face distance {@code dx² + j² + i²}) than {@link #closest} becomes the goal.
     */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isWater(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWater(x - dx, y + i, z + j)) {
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
                if (this.isWater(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWater(x + i, y - dy, z + j)) {
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
                if (this.isWater(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isWater(x + i, y + j, z - dz)) {
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

    /**
     * {@code updateAITasks} (:318-398), after the goals:
     * <ol>
     * <li>count {@code hurt_timer} down;</li>
     * <li>out of water with 1/25: shell scan for water, radius 1..11 (from 5 in steps of two), height up to 10; walk to
     * the nearest at 1.33 (one below it) or, without water, dry out with 1/100 by {@code scale} health and vanish at 0
     * without death or drops ({@code setDead});</li>
     * <li>1/5: 1/100 drop the target, drop a dead one, else search; face it; within squared
     * {@code (6 + width/2)² * scale} set {@code attacking} and hit with {@code rand(4) == 0 || rand(5) == 1} plus a
     * scorpion sound at the target, otherwise walk to it at 1.0; no target clears {@code attacking};</li>
     * <li>1/120 in water and hurt: {@code splash} (namespace-less, silent - R18) and heal {@code 4 * scale}.</li>
     * </ol>
     * {@code (int)} casts are {@code Mth.floor} (R20).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (!this.isInWater() && this.level().random.nextInt(25) == 0) {
            this.closest = 99999;
            final boolean tx = false;
            this.tz = (tx ? 1 : 0);
            this.ty = (tx ? 1 : 0);
            this.tx = (tx ? 1 : 0);
            for (int i = 1; i < 12; ++i) {
                int j = i;
                if (j > 10) {
                    j = 10;
                }
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 5) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) (this.ty - 1), (double) this.tz, 1.33);
            } else {
                if (this.level().random.nextInt(100) == 1) {
                    // heal(-1.0f * scale): PORT (R18) through setHealth, see TerrorSupport.legacyHeal.
                    TerrorSupport.legacyHeal(this, -1.0f * this.getCrabScale());
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }
        if (this.level().random.nextInt(5) == 1) {
            LivingEntity e = null;
            if (this.level().random.nextInt(100) == 1) {
                this.setTarget((LivingEntity) null);
            }
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget((LivingEntity) null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (6.0f + e.getBbWidth() / 2.0f) * (6.0f + e.getBbWidth() / 2.0f) * this.getCrabScale()) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.doHurtTarget(e);
                        if (!this.level().isClientSide) {
                            if (this.level().random.nextInt(3) == 1) {
                                ArthropodSupport.playSoundAtEntity(e, ModSounds.SCORPION_ATTACK.get(), 0.75f, 1.5f);
                            } else {
                                ArthropodSupport.playSoundAtEntity(e, ModSounds.SCORPION_LIVING.get(), 0.75f, 1.5f);
                            }
                        }
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.0);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.level().random.nextInt(120) == 1 && this.isInWater() && this.getHealth() < this.mygetMaxHealth()) {
            // this.playSound("splash", 1.5f, rand * 0.2f + 0.9f): no namespace, silent as in the original (R18). The
            // pitch roll on the world random still happens.
            final float unusedPitch = this.level().random.nextFloat() * 0.2f + 0.9f;
            this.heal(4.0f * this.getCrabScale());
        }
    }

    /**
     * {@code isSuitableTarget} (:400-440): alive and visible; a player unless creative; not a crab; any monster, lizard,
     * rubber ducky, villager, Girlfriend or Boyfriend, otherwise {@code MyUtils.isAttackableNonMob}.
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
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof Crab) {
            return false;
        }
        // EntityMob: every OreSpawn and vanilla monster (Molenoid, W07).
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof Lizard) {
            return true;
        }
        if (par1EntityLiving instanceof RubberDucky) {
            return true;
        }
        if (par1EntityLiving instanceof Villager) {
            return true;
        }
        if (par1EntityLiving instanceof Girlfriend) {
            return true;
        }
        if (par1EntityLiving instanceof Boyfriend) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /**
     * {@code findSomethingToAttack} (:442-464): nothing with {@code PlayNicely}; a living current target first (and a
     * dead one cleared); otherwise the first suitable entity in {@code expand(16, 6, 16)}. The original sorted the list
     * before the target test; neither step has a side effect, so the order does not show.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final LivingEntity e = this.getTarget();
        if (e != null && e.isAlive()) {
            return e;
        }
        this.setTarget((LivingEntity) null);
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 6.0, 16.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:466-468). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:470-472). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:479-511). A "Crab" spawner allows it (and sets the scale, see
     * {@link #checkSpawnRules}); otherwise y at least 50 and daytime; in Crystal additionally 1/40 on the world random and
     * at most 3 crabs in {@code expand(24, 8, 24)} ({@code findBuddies} :474-477).
     *
     * <p>PORT: spawner scan (x/z -3..2, y 0..4) → {@link MobSpawnType#SPAWNER} (catalogue 5.9); the buddy box is the
     * type's spawn box at the position (Molenoid, W07).
     */
    public static boolean checkCrabSpawnRules(final EntityType<Crab> type, final ServerLevelAccessor level,
                                              final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (!InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (TerrorSupport.isCrystal(level.getLevel())) {
            if (level.getRandom().nextInt(40) != 1) {
                return false;
            }
            if (level.getEntitiesOfClass(Crab.class,
                    type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(24.0, 8.0, 24.0)).size() > 3) {
                return false;
            }
        }
        return true;
    }

    /**
     * The spawner branch of {@code getCanSpawnHere} set the scale on the entity it was asked for (:489):
     * {@code setCrabScale(0.35f)}. This is the instance check a spawner asks the created crab
     * ({@code EventHooks.checkSpawnPositionSpawner}); the rest of the rule is {@link #checkCrabSpawnRules}. Health, XP and
     * the attack attribute keep the rolled scale, as in the original.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (reason == MobSpawnType.SPAWNER) {
            this.setCrabScale(0.35f);
        }
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
