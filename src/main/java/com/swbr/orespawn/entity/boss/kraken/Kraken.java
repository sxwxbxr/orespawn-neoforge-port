package com.swbr.orespawn.entity.boss.kraken;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.cannonfodder.Chipmunk;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.entity.dragon.Spyro;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.herbivore.StinkBug;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.leon.Leon;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.sea.AttackSquid;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Kraken} (Kraken.java:20-1245), id {@code the_kraken} ("The Kraken",
 * OreSpawnMain.java:3551, tracking 128/1/false). The hostile flying squid boss: it hovers 14 to 22 blocks above the
 * ground, grabs a player or a grounded mob, lifts it towards Y 200 and lets it fall (verhalten/entity-03.md).
 *
 * <p>Values: {@code Kraken_stats} (1000/40/10) from {@link MobStats}, speed 0.37 (:68), {@code experienceValue} 500
 * (:56), {@code fireResistance} 120 and {@code isImmuneToFire} (:57-58, the latter is {@code fireImmune()} on the
 * type). Size 4 x 15, with {@code PlayNicely} 1.3333334 x 5 (:49-54), see {@link #getDefaultDimensions}. DataWatcher 20
 * {@code attacking} (model: tentacles), 21 {@code PlayNicely} (renderer scale), both {@code Integer}. NBT: only
 * {@code LongEnough} (:172-180).
 *
 * <p>{@link VirtualHealth} (R4): the config clamp allows twice the default, 2000, above the attribute limit of 1024
 * (MobStats Javadoc, W01 hand-off). At the default of 1000 the scale is exactly 1. Every health comparison of the
 * original runs in original units.
 *
 * <p>R18, kept 1:1: a Kraken loaded without the {@code LongEnough} tag (a summon without NBT, an old save) reads 0 and
 * flees upwards at its next new flight target; {@code straight_down} starts at 1 and is cleared for good by the first
 * ground contact of the scan.
 *
 * <p>Not carried over: {@code getDropItem} quartz (:205-207) is dead because {@code dropFewItems} is overridden;
 * {@code onLivingUpdate} (:124-126), {@code isAIEnabled} (:120-122) change nothing; {@code interact} returning
 * {@code false} (:928-930) is the default {@code mobInteract}; {@code renderdata}/{@code getRenderInfo} (:23, :76-114)
 * is client-only scratch space and lives in {@code KrakenModel} (AlienModel precedent). {@code canTriggerWalking} is
 * not overridden, so no {@code NoStepTrigger} (R20).
 */
public class Kraken extends Monster implements LegacyArmor, VirtualHealth {

    /** DataWatcher 20: {@code attacking} (:74, :1215-1221), read by {@code KrakenModel}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Kraken.class, EntityDataSerializers.INT);
    /** DataWatcher 21: {@code PlayNicely} (:75, :967), read by {@code RenderKraken}. */
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY =
            SynchedEntityData.defineId(Kraken.class, EntityDataSerializers.INT);

    /** {@code setSize(1.3333334f, 5.0f)} (:53). */
    private static final EntityDimensions PLAY_NICELY_SIZE = EntityDimensions.scalable(1.3333334f, 5.0f);

    /**
     * {@code instanceof ThePrinceTeen} (:1164). PORT: the class is written in parallel by w10-prince; compared by
     * registry id ({@code the_young_prince}, manifest) so this package compiles on its own. ThePrinceTeen has no
     * subclass in 20.2, so the id names the same set. The integrator may switch to {@code instanceof}.
     */
    private static final ResourceLocation THE_PRINCE_TEEN = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_young_prince");
    /** {@code instanceof ThePrinceAdult} (:1168), id {@code the_young_adult_prince}; same PORT as {@link #THE_PRINCE_TEEN}. */
    private static final ResourceLocation THE_PRINCE_ADULT = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_young_adult_prince");

    private GenericTargetSorter TargetSorter;
    /** {@code ChunkCoordinates currentFlightTarget} (:24). */
    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    @Nullable
    private LivingEntity caught;
    private int newtarget;
    private int release;
    private int weather_set;
    private int long_enough;
    private int call_reinforcements;
    private boolean hit_by_player;
    private int straight_down;
    private int hurt_timer;
    /** The hitbox chosen in the constructor from {@code PlayNicely} (:49-54), server side. */
    private boolean smallSize;
    /** {@code posY +=} of the obstruction step was written without the box; see {@link #move}. */
    private boolean posWrittenRaw;

    /** {@code Kraken(World)} (:35-63). */
    public Kraken(final EntityType<? extends Kraken> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.currentFlightTarget = null;
        this.caught = null;
        this.newtarget = 0;
        this.release = 0;
        this.weather_set = 10;
        this.long_enough = 3600;
        this.call_reinforcements = 0;
        this.hit_by_player = false;
        this.straight_down = 1;
        this.hurt_timer = 0;
        // DataWatcher 21 starts at OreSpawnMain.PlayNicely (:75); the client receives the server's value.
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
        this.smallSize = OreSpawnConfig.TWEAKS.PlayNicely.get() != 0;
        this.refreshDimensions();
        // PORT: getNavigator().setAvoidsWater(false) (:55) is the 1.21.1 default; nothing to set.
        this.xpReward = 500;
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: the flight writes moveForward = 0.4 (:1076) after the 1.7.10 move helper had run; 1.21.1's MoveControl
        // runs after customServerAiStep and would zero zza in its idle branch (InsectSupport.LegacyMoveControl, W05).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
        this.goalSelector.addGoal(1, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; CaterKiller, W08).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        final MobStats stats = MobStats.Kraken_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.mygetMaxHealth()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3700000047683716);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:65-70) at registration time (R3); armor from {@code getTotalArmorValue} (:116-118). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Kraken_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(stats.health()))
                .add(Attributes.MOVEMENT_SPEED, 0.3700000047683716)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:72-87). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
    }

    /** {@code getPlayNicely} (:89-91). */
    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    /** {@code mygetMaxHealth} (:93-95), original units. */
    public int mygetMaxHealth() {
        return MobStats.Kraken_stats().health();
    }

    /** {@link VirtualHealth}: the original maximum is {@link #mygetMaxHealth()}. */
    @Override
    public double getOriginalMaxHealth() {
        return this.mygetMaxHealth();
    }

    /** {@code getKrakenHealth} (:97-99), original units (girlfriend overlay, W11). */
    public int getKrakenHealth() {
        return (int) VirtualHealth.originalHealth(this);
    }

    /** {@code getTotalArmorValue} (:116-118) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Kraken_stats().defense();
    }

    /**
     * {@code setSize} as dimensions (R9): 1.3333334 x 5 when {@code PlayNicely} was set at construction, otherwise the
     * type's 4 x 15.
     *
     * <p>PORT (R18 case 4): 1.7.10 read the client's own config in the client's constructor; the client takes the
     * synced DataWatcher 21 instead (CaterKiller precedent, W08).
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

    /** {@code fireResistance = 120} (:57). */
    @Override
    protected int getFireImmuneTicks() {
        return 120;
    }

    /**
     * {@code onUpdate} (:139-170), both sides, after the vanilla tick: the flight target is created 10 blocks below on
     * the first tick without one, otherwise {@code motionY} is damped (x0.72 below the target, x0.5 above). Without
     * {@code PlayNicely} the weather counter runs down and on the server, at 0, sets rain and thunder for 300 ticks
     * (switching them on if it was not raining) and restarts at 100.
     *
     * <p>{@code (int)} casts on coordinates are {@link Mth#floor} (R20). {@code WorldInfo} is the level's
     * {@link ServerLevelData}; in dimensions other than the overworld that is {@code DerivedLevelData}, whose setters
     * do nothing - as 1.7.10's {@code DerivedWorldInfo} did.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY() - 10.0), Mth.floor(this.getZ()));
        } else if (this.getY() < this.currentFlightTarget.getY()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.72, 1.0));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.5, 1.0));
        }
        if (this.weather_set > 0 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            --this.weather_set;
            if (this.weather_set == 0 && !this.level().isClientSide) {
                if (this.level().getLevelData() instanceof ServerLevelData worldinfo) {
                    if (!this.level().isRaining()) {
                        worldinfo.setRainTime(300);
                        worldinfo.setThunderTime(300);
                        worldinfo.setRaining(true);
                        worldinfo.setThundering(true);
                    } else {
                        worldinfo.setRainTime(300);
                        worldinfo.setThunderTime(300);
                    }
                }
                this.weather_set = 100;
            }
        }
    }

    /** {@code writeEntityToNBT} (:172-175). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("LongEnough", this.long_enough);
    }

    /** {@code readEntityFromNBT} (:177-180): a missing tag reads 0 (R18, see the class Javadoc). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.long_enough = par1NBTTagCompound.getInt("LongEnough");
    }

    /** {@code getLivingSound} (:182-187): 1 in 5 from the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(5) == 0) {
            return ModSounds.KRAKEN_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:189-191): none. */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:193-195). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:197-199). */
    @Override
    protected float getSoundVolume() {
        return 2.0f;
    }

    /** {@code getSoundPitch} (:201-203). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropItemRand} (:209-217): spread up to 7 blocks on {@code OreSpawnRand}, one block up. */
    private void dropItemRand(final ItemStack is) {
        ArthropodSupport.dropItemRand(this, is, 8);
    }

    /**
     * {@code dropFewItems} (:219-926): a Kraken tooth and an item frame; 120-279 ink sacs ({@code Items.dye} damage 0);
     * then 5-14 rolls of {@code rand(53)} on the world random over diamond, iron, gold, Experience and Amethyst gear,
     * the Ultimate weapons and tools, storage blocks, golden apples and an enchanted golden apple (spread 2, :736-742).
     *
     * <p>The enchantment blocks are the boss tables W07 shares ({@link ArthropodSupport#enchantSword} and siblings: same
     * rolls in the same order, Sharpness twice summed - PORT there). The diamond helmet rolls Respiration 1-2
     * ({@code enchantHelmet}); the iron, gold and Experience helmets roll Respiration 1-5 (:498, :668, :788), see
     * {@link #enchantHelmetRespiration5}. The stack is enchanted before the item entity is added (PORT in
     * {@link ArthropodSupport#dropItemRand}); both random streams keep their order.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        final Level world = this.level();
        this.dropItemRand(new ItemStack(ModItems.KRAKEN_TOOTH.get(), 1));
        this.dropItemRand(new ItemStack(Items.ITEM_FRAME, 1));
        for (int var5 = 120 + world.random.nextInt(160), var6 = 0; var6 < var5; ++var6) {
            // new ItemStack(Items.dye, 1, 0): damage 0 is the ink sac.
            this.dropItemRand(new ItemStack(Items.INK_SAC, 1));
        }
        for (int i = 5 + world.random.nextInt(10), var6 = 0; var6 < i; ++var6) {
            final int var7 = world.random.nextInt(53);
            ItemStack is = null;
            switch (var7) {
                case 0 -> is = new ItemStack(ModItems.ULTIMATE_SWORD.get(), 1);
                case 1 -> is = new ItemStack(Items.DIAMOND, 1);
                case 2 -> is = new ItemStack(Blocks.DIAMOND_BLOCK, 1);
                case 3 -> {
                    is = new ItemStack(Items.DIAMOND_SWORD, 1);
                    ArthropodSupport.enchantSword(is, world);
                }
                case 4 -> {
                    is = new ItemStack(Items.DIAMOND_SHOVEL, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 5 -> {
                    is = new ItemStack(Items.DIAMOND_PICKAXE, 1);
                    ArthropodSupport.enchantPickaxe(is, world);
                }
                case 6 -> {
                    is = new ItemStack(Items.DIAMOND_AXE, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 7 -> {
                    is = new ItemStack(Items.DIAMOND_HOE, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 8 -> {
                    is = new ItemStack(Items.DIAMOND_HELMET, 1);
                    ArthropodSupport.enchantHelmet(is, world);
                }
                case 9 -> {
                    is = new ItemStack(Items.DIAMOND_CHESTPLATE, 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 10 -> {
                    is = new ItemStack(Items.DIAMOND_LEGGINGS, 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 11 -> {
                    is = new ItemStack(Items.DIAMOND_BOOTS, 1);
                    ArthropodSupport.enchantBoots(is, world);
                }
                case 12 -> is = new ItemStack(ModItems.ULTIMATE_BOW.get(), 1);
                case 13 -> is = new ItemStack(ModItems.ULTIMATE_AXE.get(), 1);
                case 14 -> is = new ItemStack(Items.IRON_INGOT, 1);
                case 15 -> is = new ItemStack(ModItems.ULTIMATE_PICKAXE.get(), 1);
                case 16 -> {
                    is = new ItemStack(Items.IRON_SWORD, 1);
                    ArthropodSupport.enchantSword(is, world);
                }
                case 17 -> {
                    is = new ItemStack(Items.IRON_SHOVEL, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 18 -> {
                    is = new ItemStack(Items.IRON_PICKAXE, 1);
                    ArthropodSupport.enchantPickaxe(is, world);
                }
                case 19 -> {
                    is = new ItemStack(Items.IRON_AXE, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 20 -> {
                    is = new ItemStack(Items.IRON_HOE, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 21 -> {
                    is = new ItemStack(Items.IRON_HELMET, 1);
                    enchantHelmetRespiration5(is, world);
                }
                case 22 -> {
                    is = new ItemStack(Items.IRON_CHESTPLATE, 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 23 -> {
                    is = new ItemStack(Items.IRON_LEGGINGS, 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 24 -> {
                    is = new ItemStack(Items.IRON_BOOTS, 1);
                    ArthropodSupport.enchantBoots(is, world);
                }
                case 25 -> is = new ItemStack(ModItems.ULTIMATE_SHOVEL.get(), 1);
                case 26 -> is = new ItemStack(Blocks.IRON_BLOCK, 1);
                case 27 -> is = new ItemStack(Items.GOLD_NUGGET, 1);
                case 28 -> is = new ItemStack(Items.GOLD_INGOT, 1);
                case 29 -> is = new ItemStack(Items.GOLDEN_CARROT, 1);
                case 30 -> {
                    is = new ItemStack(Items.GOLDEN_SWORD, 1);
                    ArthropodSupport.enchantSword(is, world);
                }
                case 31 -> {
                    is = new ItemStack(Items.GOLDEN_SHOVEL, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 32 -> {
                    is = new ItemStack(Items.GOLDEN_PICKAXE, 1);
                    ArthropodSupport.enchantPickaxe(is, world);
                }
                case 33 -> {
                    is = new ItemStack(Items.GOLDEN_AXE, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 34 -> {
                    is = new ItemStack(Items.GOLDEN_HOE, 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 35 -> {
                    is = new ItemStack(Items.GOLDEN_HELMET, 1);
                    enchantHelmetRespiration5(is, world);
                }
                case 36 -> {
                    is = new ItemStack(Items.GOLDEN_CHESTPLATE, 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 37 -> {
                    is = new ItemStack(Items.GOLDEN_LEGGINGS, 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 38 -> {
                    is = new ItemStack(Items.GOLDEN_BOOTS, 1);
                    ArthropodSupport.enchantBoots(is, world);
                }
                case 39 -> is = new ItemStack(Items.GOLDEN_APPLE, 1);
                case 40 -> is = new ItemStack(Blocks.GOLD_BLOCK, 1);
                case 41 -> {
                    // new ItemStack(Items.golden_apple, 1, 1) in its own EntityItem, spread 2 (:736-742).
                    ArthropodSupport.dropItemRand(this, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1), 3);
                }
                case 42 -> {
                    is = new ItemStack(ModItems.EXPERIENCE_SWORD.get(), 1);
                    ArthropodSupport.enchantSword(is, world);
                }
                case 43 -> {
                    is = new ItemStack(ModItems.EXPERIENCE_HELMET.get(), 1);
                    enchantHelmetRespiration5(is, world);
                }
                case 44 -> {
                    is = new ItemStack(ModItems.EXPERIENCE_CHEST.get(), 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 45 -> {
                    is = new ItemStack(ModItems.EXPERIENCE_LEGGINGS.get(), 1);
                    ArthropodSupport.enchantBody(is, world);
                }
                case 46 -> {
                    is = new ItemStack(ModItems.EXPERIENCE_BOOTS.get(), 1);
                    ArthropodSupport.enchantBoots(is, world);
                }
                case 47 -> {
                    is = new ItemStack(ModItems.AMETHYST_SWORD.get(), 1);
                    ArthropodSupport.enchantSword(is, world);
                }
                case 48 -> {
                    is = new ItemStack(ModItems.AMETHYST_SHOVEL.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 49 -> {
                    is = new ItemStack(ModItems.AMETHYST_PICKAXE.get(), 1);
                    ArthropodSupport.enchantPickaxe(is, world);
                }
                case 50 -> {
                    is = new ItemStack(ModItems.AMETHYST_AXE.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 51 -> {
                    is = new ItemStack(ModItems.AMETHYST_HOE.get(), 1);
                    ArthropodSupport.enchantTool(is, world);
                }
                case 52 -> is = new ItemStack(ModBlocks.BLOCKAMETHYST.get(), 1);
                default -> {
                }
            }
            if (is != null) {
                this.dropItemRand(is);
            }
        }
    }

    /**
     * The iron, gold and Experience helmet blocks (:481-504, :651-674, :771-794): the four protections 1/6 (1-5),
     * Unbreaking 1/2 (2-5), Respiration 1/6 (<b>1-5</b>), Aqua Affinity 1/6 (1-5), all on the world random. The
     * diamond helmet (:315-338) rolls Respiration 1-2 and is {@link ArthropodSupport#enchantHelmet}.
     */
    private static void enchantHelmetRespiration5(final ItemStack is, final Level level) {
        final RandomSource rand = level.random;
        if (rand.nextInt(6) == 1) {
            PreEnchant.add(is, level, Enchantments.PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            PreEnchant.add(is, level, Enchantments.BLAST_PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            PreEnchant.add(is, level, Enchantments.FIRE_PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            PreEnchant.add(is, level, Enchantments.PROJECTILE_PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(2) == 1) {
            PreEnchant.add(is, level, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            PreEnchant.add(is, level, Enchantments.RESPIRATION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            PreEnchant.add(is, level, Enchantments.AQUA_AFFINITY, 1 + rand.nextInt(5));
        }
    }

    /**
     * {@code canDespawn} (:932-947) as the far-away rule: never when persistent; always once {@code long_enough} ran out;
     * above Y 150 when below half health. The third branch ({@code setDead} above Y 180 when {@code long_enough <= 0})
     * is unreachable behind the second and kept as written.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isPersistenceRequired()) {
            return false;
        }
        if (this.long_enough <= 0) {
            return true;
        }
        if (this.getY() > 150.0 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth() / 2) {
            return true;
        }
        if (this.getY() > 180.0 && this.long_enough <= 0) {
            this.discard();
            return true;
        }
        return false;
    }

    /**
     * {@code canSeeTarget} (:949-951): no block between a point 0.75 above the feet and the point. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded - the outline shape
     * (Mothra, GoldFish precedent).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private boolean isAirAt(final int x, final int y, final int z) {
        // PORT: getBlock(...) == Blocks.air; isAir() also accepts cave and void air, which 1.7.10 generated as plain air.
        return this.level().getBlockState(new BlockPos(x, y, z)).isAir();
    }

    /**
     * {@code updateAITasks} (:953-1100), after the goal selectors:
     * <ol>
     *   <li>{@code hurt_timer} and {@code long_enough} count down, DataWatcher 21 = {@code PlayNicely}, lightning 16
     *       blocks below with 1/400 without {@code PlayNicely};</li>
     *   <li>a new flight target when asked for, with 1/250 or within 9.1 (squared): ground scan 30 blocks down, up to 50
     *       tries at ±12..17 (straight down while {@code straight_down}), a target 30 higher when fleeing, and once, when
     *       hit by a player below an eighth of its health above Y 130, ten more Krakens at Y 170;</li>
     *   <li>otherwise, without prey, with 1/8 and without {@code PlayNicely}: the nearest player in 25/40/25 (not creative,
     *       seen) or, without one, with 1/2 a suitable target in 20/40/20 - flight target 15 above it and a grab;</li>
     *   <li>carrying: towards Y 200, the prey copies motion, position 15 below and yaw, a bite with 1/50, released at
     *       {@code release} or with 1/250;</li>
     *   <li>steering towards the target, yaw a fifth of the way, {@code moveForward 0.4}, and the obstruction push.</li>
     * </ol>
     * {@code (int)} casts on coordinates are {@link Mth#floor} (R20); {@code this.rand} is the entity random,
     * {@code worldObj.rand} the level's, {@code moveForward} is {@code zza}.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        final Level world = this.level();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if (this.long_enough > 0) {
            --this.long_enough;
        }
        this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
        if (world.random.nextInt(400) == 1 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            // addWeatherEffect(new EntityLightningBolt(worldObj, posX, posY - 16, posZ)); server only here.
            final LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
            if (bolt != null) {
                bolt.moveTo(this.getX(), this.getY() - 16.0, this.getZ(), 0.0f, 0.0f);
                world.addFreshEntity(bolt);
            }
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.newtarget != 0 || this.random.nextInt(250) == 1
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 9.1f) {
            this.newtarget = 0;
            int ground_dist;
            for (ground_dist = 0; ground_dist < 31; ++ground_dist) {
                if (!this.isAirAt(Mth.floor(this.getX()), Mth.floor(this.getY()) - ground_dist, Mth.floor(this.getZ()))) {
                    this.straight_down = 0;
                    break;
                }
            }
            ground_dist = 20 - ground_dist;
            // Block bid = Blocks.stone (:985): "not air yet".
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                zdir = world.random.nextInt(6) + 12;
                xdir = world.random.nextInt(6) + 12;
                if (world.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (world.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                if (this.straight_down != 0) {
                    zdir = 0;
                    xdir = 0;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir, Mth.floor(this.getY()) + ground_dist + this.random.nextInt(9) - 6,
                        Mth.floor(this.getZ()) + zdir);
                bidIsAir = this.isAirAt(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ());
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
            }
            final float health = VirtualHealth.originalHealth(this);
            if (this.long_enough <= 0 || (this.getY() < 200.0 && health < this.mygetMaxHealth() / 4)) {
                this.currentFlightTarget.set(this.currentFlightTarget.getX(), this.currentFlightTarget.getY() + 30, this.currentFlightTarget.getZ());
                if (this.hit_by_player && this.call_reinforcements == 0 && health < this.mygetMaxHealth() / 8 && this.getY() > 130.0) {
                    this.call_reinforcements = 1;
                    for (int i = 0; i < 10; ++i) {
                        // spawnCreature(worldObj, "The Kraken", ...) (:1008, :128-137): create, random yaw, add, living sound.
                        final double sx = this.getX() + world.random.nextInt(10) - world.random.nextInt(10);
                        final double sz = this.getZ() + world.random.nextInt(10) - world.random.nextInt(10);
                        ItemSpawnEgg.spawnSomething(this.getType(), world, sx, 170.0, sz);
                    }
                }
            }
        } else if (this.caught == null && world.random.nextInt(8) == 1 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            Player target = null;
            target = findNearestPlayer(this, 25.0, 40.0, 25.0);
            if (target != null) {
                if (!target.getAbilities().instabuild) {
                    if (this.getSensing().hasLineOfSight(target)) {
                        this.currentFlightTarget.set(Mth.floor(target.getX()), Mth.floor(target.getY()) + 15, Mth.floor(target.getZ()));
                        this.attackWithSomething(target);
                    }
                } else {
                    target = null;
                }
            }
            if (target == null && world.random.nextInt(2) == 0) {
                LivingEntity e = null;
                e = this.findSomethingToAttack();
                if (e != null) {
                    this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 15, Mth.floor(e.getZ()));
                    this.attackWithSomething(e);
                }
            }
        }
        if (this.caught != null) {
            if (!this.caught.isRemoved()) {
                this.currentFlightTarget.set(Mth.floor(this.getX()), 200, Mth.floor(this.getZ()));
                if (this.getY() > 190.0) {
                    this.release = 1;
                }
                final Vec3 m = this.getDeltaMovement();
                double caughtMotionY = m.y;
                // PORT (R18 case 3): 1.7.10 wrote the prey's posX/posY/posZ without its bounding box, and the prey's
                // next moveEntity rebuilt the position from the box - for a mob the write was only seen until then, and
                // the copied motion (plus the lift below) did the carrying. 1.21.1 moves from the position, not the box,
                // so there is no write that is undone by the next move; setPos makes the prey really hang 15 blocks
                // below the Kraken. For a player the server position is then corrected on the client by the movement
                // check, where 1.7.10 reset to the last accepted packet.
                if (this.getY() - this.caught.getY() > 16.0) {
                    caughtMotionY += 0.25;
                }
                this.caught.setDeltaMovement(m.x, caughtMotionY, m.z);
                this.caught.setPos(this.getX(), this.getY() - 15.0, this.getZ());
                this.caught.setYRot(this.getYRot());
                if (world.random.nextInt(50) == 1) {
                    this.doHurtTarget(this.caught);
                }
                if (this.release != 0 || world.random.nextInt(250) == 1) {
                    this.caught = null;
                    this.newtarget = 1;
                    this.release = 0;
                    this.setAttacking(0);
                }
            } else {
                this.caught = null;
                this.newtarget = 1;
                this.release = 0;
                this.setAttacking(0);
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.3 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.3 - this.getZ();
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.45 - m.x) * 0.15;
        double motionY = m.y + (Math.signum(var2) * 0.70999 - m.y) * 0.202;
        final double motionZ = m.z + (Math.signum(var3) * 0.45 - m.z) * 0.15;
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.4f;
        if (Math.abs(motionX) + Math.abs(motionZ) < 0.15) {
            var5 = 0.0f;
        }
        this.setYRot(this.getYRot() + var5 / 5.0f);
        double obstruction_factor = 0.0;
        double dx = 0.0;
        double dz = 0.0;
        final int dist = 10;
        for (int k = -20; k < 18; k += 2) {
            for (int i = 1; i < dist; i += 2) {
                dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                if (!this.isAirAt(Mth.floor(this.getX() + dx), Mth.floor(this.getY()) + k, Mth.floor(this.getZ() + dz))) {
                    obstruction_factor += 0.1;
                }
            }
        }
        motionY += obstruction_factor * 0.08;
        this.setDeltaMovement(motionX, motionY, motionZ);
        // posY += obstruction_factor * 0.08 (:1096): written without the box, see move().
        this.setPosRaw(this.getX(), this.getY() + obstruction_factor * 0.08, this.getZ());
        this.posWrittenRaw = true;
        if (this.getY() > 256.0 && !this.isPersistenceRequired()) {
            this.discard();
        }
    }

    /**
     * 1.7.10 {@code Entity.moveEntity} took the position from the bounding box; the {@code posY} write of the
     * obstruction step (:1096) left the box alone and was therefore undone by the move that followed in the same tick
     * (LeafMonster precedent, W07). The box position is restored before moving, as {@code moveEntity} did.
     */
    @Override
    public void move(final MoverType type, final Vec3 pos) {
        if (this.posWrittenRaw) {
            this.posWrittenRaw = false;
            final AABB bb = this.getBoundingBox();
            this.setPosRaw((bb.minX + bb.maxX) / 2.0, bb.minY, (bb.minZ + bb.maxZ) / 2.0);
        }
        super.move(type, pos);
    }

    /**
     * {@code World.findNearestEntityWithinAABB(EntityPlayer.class, boundingBox.expand(x, y, z), self)}: every player in
     * the box except {@code self}, the nearest by squared distance; {@code <=} keeps the last of equally near ones
     * (MothSupport.findNearestPlayer, W08, which is package-private).
     */
    @Nullable
    private static Player findNearestPlayer(final Entity self, final double x, final double y, final double z) {
        final List<Player> var4 = self.level().getEntitiesOfClass(Player.class, self.getBoundingBox().inflate(x, y, z));
        Player var5 = null;
        double var6 = Double.MAX_VALUE;
        for (final Player var9 : var4) {
            if (var9 == self) {
                continue;
            }
            final double var10 = self.distanceToSqr(var9);
            if (var10 > var6) {
                continue;
            }
            var5 = var9;
            var6 = var10;
        }
        return var5;
    }

    /**
     * {@code attackWithSomething} (:1102-1114): with no prey yet, grab the entity when the squared distance from a point
     * 15 blocks above it is below 30.
     */
    private void attackWithSomething(final LivingEntity par1) {
        if (this.caught != null) {
            return;
        }
        double dist = (this.getX() - par1.getX()) * (this.getX() - par1.getX());
        dist += (this.getZ() - par1.getZ()) * (this.getZ() - par1.getZ());
        dist += (this.getY() - par1.getY() - 15.0) * (this.getY() - par1.getY() - 15.0);
        if (dist < 30.0) {
            this.caught = par1;
            this.release = 0;
            this.setAttacking(1);
        }
    }

    private static boolean isType(final Entity e, final ResourceLocation id) {
        return EntityType.getKey(e.getType()).equals(id);
    }

    /**
     * {@code isSuitableTarget} (:1116-1173): alive, not ignoreable, seen; a player neither creative nor flying; anything
     * else only on the ground or in water, never a squid, Attack Squid, Kraken or Spyro; Dragon, Cephadrome, Leon and the
     * two older princes only while nobody rides them; never a chicken, Chipmunk, Stink Bug or Mothra.
     * {@code riddenByEntity == null} is {@code !isVehicle()}.
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
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild && !p.getAbilities().flying;
        }
        if (!par1EntityLiving.onGround() && !par1EntityLiving.isInWater()) {
            return false;
        }
        if (par1EntityLiving instanceof Squid) {
            return false;
        }
        if (par1EntityLiving instanceof AttackSquid) {
            return false;
        }
        if (par1EntityLiving instanceof Kraken) {
            return false;
        }
        if (par1EntityLiving instanceof Spyro) {
            return false;
        }
        if (par1EntityLiving instanceof Dragon c) {
            return !c.isVehicle();
        }
        if (par1EntityLiving instanceof Cephadrome c2) {
            return !c2.isVehicle();
        }
        if (par1EntityLiving instanceof Leon c3) {
            return !c3.isVehicle();
        }
        if (isType(par1EntityLiving, THE_PRINCE_TEEN)) {
            return !par1EntityLiving.isVehicle();
        }
        if (isType(par1EntityLiving, THE_PRINCE_ADULT)) {
            return !par1EntityLiving.isVehicle();
        }
        return !(par1EntityLiving instanceof Chicken) && !(par1EntityLiving instanceof Chipmunk)
                && !(par1EntityLiving instanceof StinkBug) && !(par1EntityLiving instanceof Mothra);
    }

    /** {@code findSomethingToAttack} (:1175-1192): nothing with {@code PlayNicely}; the first suitable one in {@code expand(20, 40, 20)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 20.0, 40.0, 20.0, e -> this.isSuitableTarget(e, false));
    }

    /** {@code onStruckByLightning} (:1194-1195): its own bolts do nothing to it. */
    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightning) {
    }

    /**
     * {@code attackEntityFrom} (:1197-1213): a player source while above a quarter of the health marks
     * {@code hit_by_player} and moves the flight target 15 above the attacker - before the timer check, so also for
     * refused hits. Then a hit is refused while {@code hurt_timer} runs; otherwise the timer restarts at 30, the hit
     * lands and with 1/2 the prey is released.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final Entity e = par1DamageSource.getEntity();
        boolean ret = false;
        if (this.currentFlightTarget != null && e != null && e instanceof Player
                && VirtualHealth.originalHealth(this) > this.mygetMaxHealth() / 4) {
            this.hit_by_player = true;
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 15, Mth.floor(e.getZ()));
        }
        if (this.hurt_timer > 0) {
            return false;
        }
        this.hurt_timer = 30;
        ret = super.hurt(par1DamageSource, par2);
        if (this.level().random.nextInt(2) == 1) {
            this.release = 1;
        }
        return ret;
    }

    /** {@code getAttacking} (:1215-1217). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:1219-1221). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code fall} (:1223-1224): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:1226-1227): no fall distance is collected. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:1229-1244): Y at least 50, and only air or tall grass in x -1..0,
     * z -1..1, y +1..+5.
     *
     * <p>PORT: {@code Blocks.tallgrass} is short grass and fern (MothSupport.isLegacyTallGrass, W08); {@code (int) posX}
     * of the block-centred spawn position is the block coordinate (R20).
     */
    public static boolean checkKrakenSpawnRules(final EntityType<Kraken> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (pos.getY() < 50.0) {
            return false;
        }
        for (int k = -1; k < 2; ++k) {
            for (int j = -1; j < 1; ++j) {
                for (int i = 1; i < 6; ++i) {
                    final BlockState bid = level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + i, pos.getZ() + k));
                    if (!bid.isAir() && !bid.is(Blocks.SHORT_GRASS) && !bid.is(Blocks.FERN)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkKrakenSpawnRules}; EntityMob's light test is overridden away. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
