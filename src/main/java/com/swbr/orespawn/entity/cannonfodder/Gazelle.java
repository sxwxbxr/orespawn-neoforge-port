package com.swbr.orespawn.entity.cannonfodder;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Gazelle} (Gazelle.java:15-412, verhalten/entity-08.md): a shy, tameable
 * antelope of the Utopia plains that grazes grass, strawberries, potatoes and carrots. Not a battle mob -
 * it extends {@code EntityTameable} directly - but it shares the battle mobs' package, helpers and click order.
 *
 * <p>Health 15 (:241-243), speed 0.3 (:33 overwrites the 0.2 of :26; catalogue 6.6), attack attribute 0,
 * hitbox 0.6 x 1.8 (:32), {@code fireResistance} 100 (:34), damage capped at 10 while tame (:372-380), never
 * despawns (:393-395). {@code experienceValue = 5} (:37) is dead, see {@link Chipmunk}.
 *
 * <p>Original bug kept (R18, "Gazelle findet sich selbst"): {@link #findBuddy} sorts the gazelles in the box by
 * distance and returns the first - the calling gazelle itself, at distance 0 - so the one-in-250 walk to "the
 * nearest gazelle" goes to its own position.
 */
public class Gazelle extends TamableAnimal {

    private float moveSpeed;
    private GenericTargetSorter TargetSorter;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Gazelle(World)} (:24-50). */
    public Gazelle(final EntityType<? extends Gazelle> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.2f;
        this.TargetSorter = null;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // setSize(0.6f, 1.8f) (:32) is the entity type size.
        this.moveSpeed = 0.3f;
        // fireResistance = 100 (:34): getFireImmuneTicks.
        // PORT: getNavigator().setAvoidsWater(true) (:35) - water impassable, malus -1 (Termite, W05).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        // experienceValue = 5 (:37): never read.
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        // EntityMob is Monster (as in MyUtils); the vanilla 1.7.10 goal, not MyEntityAIAvoidEntity.
        this.goalSelector.addGoal(3, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.7000000476837158));
        this.goalSelector.addGoal(4, new EntityAITempt(this, 1.2000000476837158, s -> s.is(Items.APPLE), false));
        this.goalSelector.addGoal(5, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(6, new EntityAIAvoidEntity(this, Player.class, 12.0f, 1.0, 2.0));
        this.goalSelector.addGoal(7, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(9, new EntityAILookIdle(this));
        // homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(10, new EntityAIMoveIndoors(this));
    }

    /**
     * {@code applyEntityAttributes} (:52-58): health 15, speed, attack 0.0. PORT: the original ran it while
     * {@code moveSpeed} was still 0 and {@code onUpdate} set 0.3 before the first move; the supplier carries 0.3.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    // entityInit (:60-63) called setSitting(false) before the flags existed; the constructor repeats it.

    /** {@code onUpdate} (:65-68). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** The five food blocks of {@code scan_it} (:75, :86, :101, :112, :127, :138). */
    private static boolean isFood(final BlockState bid) {
        return bid.is(ModBlocks.STRAWBERRY_PLANT.get()) || bid.is(Blocks.POTATOES) || bid.is(Blocks.CARROTS)
                || CannonFodderSupport.isLegacyTallGrass(bid) || CannonFodderSupport.isLegacyDoublePlant(bid);
    }

    private boolean isFoodAt(final int x, final int y, final int z) {
        return isFood(this.level().getBlockState(new BlockPos(x, y, z)));
    }

    /** {@code scan_it} (:70-151): the six faces of the shell, keeping the food block closest by squared face distance. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isFoodAt(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isFoodAt(x - dx, y + i, z + j)) {
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
                if (this.isFoodAt(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isFoodAt(x + i, y - dy, z + j)) {
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
                if (this.isFoodAt(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isFoodAt(x + i, y + j, z - dz)) {
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
     * {@code fall} (:153-167): damage {@code ceil(distance - 3)}, the sound chosen before the cap of 2 (see
     * {@link Chipmunk#causeFallDamage}).
     */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        float i = (float) Mth.ceil(par1 - 3.0f);
        if (i > 0.0f) {
            if (i > 3.0f) {
                this.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0f, 1.0f);
            } else {
                this.playSound(SoundEvents.GENERIC_SMALL_FALL, 1.0f, 1.0f);
            }
            if (i > 2.0f) {
                i = 2.0f;
            }
            this.hurt(this.damageSources().fall(), i);
            return true;
        }
        return false;
    }

    /** {@code fireResistance = 100} (:34). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /**
     * {@code updateAITick} (:169-217). 1.21.1 calls {@code customServerAiStep} where 1.7.10 called
     * {@code updateAITick} (after goals and navigation, before the controls).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob((LivingEntity) null);
        }
        if (!this.isSitting()) {
            if (((this.level().random.nextInt(30) == 0 && this.getGazelleHealth() < this.mygetMaxHealth()) || this.level().random.nextInt(750) == 1)
                    && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
                this.closest = 99999;
                final boolean tx = false;
                this.tz = (tx ? 1 : 0);
                this.ty = (tx ? 1 : 0);
                this.tx = (tx ? 1 : 0);
                for (int i = 1; i < 11; ++i) {
                    int j = i;
                    if (j > 2) {
                        j = 2;
                    }
                    // :188 (int) posX, (int) posY + 1, (int) posZ -> Mth.floor (DECISIONS R20).
                    if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) + 1, Mth.floor(this.getZ()), i, j, i)) {
                        break;
                    }
                    if (i >= 6) {
                        ++i;
                    }
                }
                if (this.closest < 99999) {
                    this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                    if (this.closest < 12) {
                        // PORT: mobGriefing through EventHooks.canEntityGrief (Termite, W05). Flag 2 without the
                        // shape updates: the other half of an eaten double plant stays, as in 1.7.10.
                        if (EventHooks.canEntityGrief(this.level(), this)) {
                            this.level().setBlock(new BlockPos(this.tx, this.ty, this.tz), Blocks.AIR.defaultBlockState(),
                                    CannonFodderSupport.LEGACY_FLAG_2);
                        }
                        this.heal(1.0f);
                        this.playSound(SoundEvents.PLAYER_BURP, 1.0f, this.level().random.nextFloat() * 0.2f + 0.9f);
                    }
                }
            }
            if (this.level().random.nextInt(250) == 1) {
                final Gazelle buddy = this.findBuddy();
                if (buddy != null) {
                    this.getNavigation().moveTo(buddy.getX(), buddy.getY(), buddy.getZ(), 0.5);
                }
            }
        }
        if (this.level().random.nextInt(250) == 0) {
            this.heal(1.0f);
        }
        // super.updateAITick(): EntityAnimal clears inLove when the age is not 0.
        super.customServerAiStep();
    }

    /** {@code findBuddy} (:219-231): the first gazelle by distance - itself (R18, see class comment). */
    @Nullable
    private Gazelle findBuddy() {
        final List<Gazelle> var5 = this.level().getEntitiesOfClass(Gazelle.class, this.getBoundingBox().inflate(16.0, 6.0, 16.0));
        var5.sort(this.TargetSorter);
        final Iterator<Gazelle> var6 = var5.iterator();
        Gazelle var8 = null;
        if (var6.hasNext()) {
            var8 = var6.next();
            return var8;
        }
        return null;
    }

    // isAIEnabled (:233-235); canBreatheUnderwater false (:237-239): the default.

    /** {@code mygetMaxHealth} (:241-243). */
    public int mygetMaxHealth() {
        return 15;
    }

    /** {@code getGazelleHealth} (:245-247). */
    public int getGazelleHealth() {
        return (int) this.getHealth();
    }

    /**
     * Entry point of a right click. PORT: 1.7.10 had one hand; the off hand does nothing. The original's
     * {@code true} is {@code sidedSuccess}. {@link CannonFodderInteractEvents} restores the click order.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        return this.legacyInteract(par1EntityPlayer) ? InteractionResult.sidedSuccess(this.level().isClientSide) : InteractionResult.PASS;
    }

    /** {@code interact} (:249-329): breeding, apple, dead bush, name tag, sitting. */
    protected boolean legacyInteract(final Player par1EntityPlayer) {
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        // super.interact: EntityAnimal breeding with the Crystal Apple.
        if (CannonFodderSupport.animalInteract(this, par1EntityPlayer)) {
            return true;
        }
        final boolean isRemote = this.level().isClientSide;
        if (!var2.isEmpty() && var2.is(Items.APPLE) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(2) == 0) {
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
            } else if (this.isOwnedBy(par1EntityPlayer)) {
                if (isRemote) {
                    this.spawnTamingParticles(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                if (this.mygetMaxHealth() > this.getHealth()) {
                    this.heal(this.mygetMaxHealth() - this.getHealth());
                }
            }
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                // func_152115_b("")
                this.setOwnerUUID(null);
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            // setCustomNameTag(getDisplayName()): an unnamed tag names it "Name Tag"; no persistence.
            this.setCustomName(var2.getHoverName());
            CannonFodderSupport.consumeOne(par1EntityPlayer, var2);
            return true;
        }
        if (this.isTame() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isSitting()) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            return true;
        }
        return false;
    }

    /** {@code getLivingSound} (:331-336): none. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        return null;
    }

    /** {@code getHurtSound} (:338-340). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SCORPION_HIT.get();
    }

    /** {@code getDeathSound} (:342-344). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:346-348). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code getDropItem} (:350-352). */
    protected Item getDropItem() {
        return Items.BEEF;
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems}, then {@code dropEquipment} ({@code Mob.dropCustomDeathLoot}).
     * The rare-drop roll after it called the empty {@code dropRareDrop}; only its random draw is not repeated.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, CannonFodderSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:354-366): tame 2-6 poppies, wild the vanilla beef roll. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.POPPY, 1));
            }
        } else {
            CannonFodderSupport.vanillaDropFewItems(this, this.getDropItem(), par2);
        }
    }

    /** {@code getSoundPitch} (:368-370): the spread is 0.1, not vanilla's 0.2. */
    @Override
    public float getVoicePitch() {
        return this.isBaby() ? ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f)
                : ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f);
    }

    /**
     * {@code attackEntityFrom} (:372-380): a tame gazelle takes at most 10 per hit. The cap lands before the R5
     * armour replacement, like the 1.7.10 cap before {@code damageEntity}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float p2 = par2;
        if (this.isTame() && p2 > 10.0f) {
            p2 = 10.0f;
        }
        ret = super.hurt(par1DamageSource, p2);
        return ret;
    }

    /**
     * {@code getCanSpawnHere} (:382-391) on the instance: y 50..100 and dirt, grass or tall grass below. The
     * override dropped EntityLiving's collision and liquid test, so {@link #checkSpawnObstruction} answers
     * {@code true}.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (this.getY() < 50.0) {
            return false;
        }
        if (this.getY() > 100.0) {
            return false;
        }
        // :389 (int) posX, (int) posY - 1, (int) posZ -> Mth.floor (DECISIONS R20).
        final BlockState bid = level.getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ())));
        return isSpawnBlock(bid);
    }

    /** See {@link #checkSpawnRules}. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:382-391) as the placement predicate; the mob stands on the block centre. */
    public static boolean checkGazelleSpawnRules(final EntityType<Gazelle> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (pos.getY() < 50.0) {
            return false;
        }
        if (pos.getY() > 100.0) {
            return false;
        }
        return isSpawnBlock(level.getBlockState(pos.below()));
    }

    /** {@code bid == Blocks.dirt || bid == Blocks.grass || bid == Blocks.tallgrass} (:390). */
    private static boolean isSpawnBlock(final BlockState bid) {
        return CannonFodderSupport.isLegacyDirt(bid) || bid.is(Blocks.GRASS_BLOCK) || CannonFodderSupport.isLegacyTallGrass(bid);
    }

    /** {@code canDespawn} (:393-395). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code createChild} / {@code spawnBabyAnimal} (:397-403): a new gazelle. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    @Nullable
    public Gazelle spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        final Entity baby = this.getType().create(this.level());
        return baby instanceof Gazelle gazelle ? gazelle : null;
    }

    /** {@code isWheat} (:405-407): no caller, dead; kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:409-411): the Crystal Apple. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
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
     * {@code onDeath}: no override in the original (Gazelle.java:15, {@code extends EntityTameable}), and 1.7.10
     * {@code EntityTameable} did not override it either, so the owner got no chat message when the animal died.
     *
     * <p>PORT: R22 (addendum 2026-09-14, owner death message of tame OreSpawn animals 1:1 off) -
     * {@code TamableAnimal.die} in 1.21.1 sends {@code getCombatTracker().getDeathMessage()} to the owner. Java cannot
     * skip one super level, so this is {@code LivingEntity.die} line by line (NeoForge 21.1 sources) without the
     * owner message; the named-entity log line uses a logger of its own instead of {@code LivingEntity}'s private one.
     */
    @Override
    public void die(final net.minecraft.world.damagesource.DamageSource damageSource) {
        if (net.neoforged.neoforge.common.CommonHooks.onLivingDeath(this, damageSource)) {
            return;
        }
        if (!this.isRemoved() && !this.dead) {
            final net.minecraft.world.entity.Entity entity = damageSource.getEntity();
            final net.minecraft.world.entity.LivingEntity livingentity = this.getKillCredit();
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
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverlevel) {
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
