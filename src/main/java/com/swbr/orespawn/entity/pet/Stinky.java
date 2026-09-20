package com.swbr.orespawn.entity.pet;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.combat.LegacyArmor;
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
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Stinky} (Stinky.java:17-706): {@code stinky}, a tameable mini dragon ("Stinky",
 * OreSpawnMain.java:3933, tracking 64/1/false). Two movement modes: {@code activity} 1 walks with the task list,
 * {@code activity} 2 flies on hand-written motion while the whole 1.7.10 AI step of {@code EntityLiving} (senses, tasks,
 * navigator, move/look/jump helpers) is skipped (:505-507). Hunts monsters and Mothra, eats coal ore, drops an item by
 * skin colour. Tamed with raw beef, untamed with a dead bush. No riding (verhalten/entity-13.md).
 *
 * <p>Values: health 100 (:133-135), speed 0.3 re-set every living tick (:36, :381), attack attribute 10 registered but
 * the hit is a fixed 10 (:276-284), armor 6 (:231-233), hitbox 0.75 (:43, entity type), {@code fireResistance} 1000 and
 * fire immune (:45-46, {@code fireImmune()} on the type), breathes under water (:129-131,
 * {@code #minecraft:can_breathe_under_water}), no fall damage (:258-262), immune to cactus (:288).
 * {@code experienceValue = 35} (:59) is never read: 1.7.10 {@code EntityAnimal.getExperiencePoints} returns
 * {@code 1 + rand.nextInt(3)}, which 1.21.1's {@code Animal.getBaseExperienceReward} does as well (W04).
 *
 * <p>State: DataWatcher 20 {@code SpyroFire} (only saved), 21 {@code activity}, 22 skin 0..18; NBT {@code SpyroActivity},
 * {@code SpyroFire}, {@code StinkySkin} (:73-94). The server fields {@code activity} and {@code skin_color} are what the
 * behaviour reads; the watcher is what the client renders, as in the original.
 *
 * <p>{@code isDaytime} in the Nether (catalogue 6.4), checked in both bytecodes: 1.7.10 {@code WorldProviderHell}
 * ({@code aqp.a(JF)F}) returns the celestial angle 0.5, for which {@code World.calculateSkylightSubtracted}
 * ({@code ahb.a(F)I}) gives 11, and {@code WorldServer.tick} stores it; 1.21.1's Nether has {@code fixed_time} 18000,
 * {@code DimensionType.timeOfDay} 0.5, and {@code Level.updateSkyBrightness} (Level.java:850-854) also gives 11 on every
 * {@code ServerLevel} tick. {@code skylightSubtracted < 4} is false in both: the Nether spawn entries never passed
 * {@code getCanSpawnHere} in 1.7.10 and do not here - 1:1, no deviation. (Chunk-generation spawns skip the predicate,
 * R23.)
 */
public class Stinky extends TamableAnimal implements AttackableNonMob, LegacyArmor {

    /** DataWatcher 22: skin 0..18 (:73). */
    private static final EntityDataAccessor<Integer> DATA_SKIN = SynchedEntityData.defineId(Stinky.class, EntityDataSerializers.INT);
    /** DataWatcher 21: activity, 1 ground, 2 flight (:74). */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(Stinky.class, EntityDataSerializers.INT);
    /** DataWatcher 20: {@code SpyroFire}, a leftover of Spyro, only saved (:75). */
    private static final EntityDataAccessor<Integer> DATA_SPYRO_FIRE = SynchedEntityData.defineId(Stinky.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    public int activity;
    private int owner_flying;
    private float moveSpeed;
    private int skin_color;
    private int syncit;
    private int closest;
    private int tx;
    private int ty;
    private int tz;
    /**
     * The 1.7.10 {@code EntityLiving} sensing cache, cleared at the start of {@code super.updateAITasks()} - which a flying
     * Stinky never reaches, so a flight keeps the cache of the last ground tick. See {@link #getSensing()}.
     */
    private Sensing legacySenses;
    /**
     * {@code activity != 2} as {@code updateAITasks} read it before any of its own code ran (:505). Taken in
     * {@link #aiStep()} before the vanilla AI step; the wrapped tasks, the navigator and the helpers read it.
     */
    private boolean groundAi = true;

    /** {@code Stinky(World)} (:31-60). */
    public Stinky(final EntityType<? extends Stinky> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.activity = 1;
        this.owner_flying = 0;
        this.moveSpeed = 0.3f;
        this.skin_color = -1;
        this.syncit = 0;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        this.legacySenses = new Sensing(this);
        // setSize(0.75f, 0.75f) (:43) is the entity type's size (R9). fireResistance = 1000 (:45) is getFireImmuneTicks,
        // isImmuneToFire (:46) is fireImmune() on the type.
        this.moveSpeed = 0.3f;
        // PORT: getNavigator().setAvoidsWater(true) (:47) is a water path malus of -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        // PORT: the move, look and jump helpers ran inside super.updateAITasks() only, so not in flight.
        this.moveControl = new GroundMoveControl(this);
        this.lookControl = new GroundLookControl(this);
        this.jumpControl = new GroundJumpControl(this);
        // :49-57. Every task is wrapped: 1.7.10 ticked the list only while activity != 2 (:505-507).
        this.goalSelector.addGoal(1, new GroundTask(this, new FloatGoal(this)));
        this.goalSelector.addGoal(2, new GroundTask(this, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 0.30000001192092896, 0.4000000059604645)));
        this.goalSelector.addGoal(3, new GroundTask(this, new MyEntityAIFollowOwner(this, 1.15f, 12.0f, 2.0f)));
        this.goalSelector.addGoal(4, new GroundTask(this, new EntityAITempt(this, 1.25, stack -> stack.is(Items.BEEF), false)));
        this.goalSelector.addGoal(5, new GroundTask(this, LegacyPanic.legacyPanic(this, 1.5)));
        this.goalSelector.addGoal(6, new GroundTask(this, new EntityAIWatchClosest(this, Player.class, 6.0f)));
        this.goalSelector.addGoal(7, new GroundTask(this, new MyEntityAIWander(this, 0.75f)));
        this.goalSelector.addGoal(8, new GroundTask(this, new EntityAILookIdle(this)));
        // :57; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(9, new GroundTask(this, new EntityAIMoveIndoors(this)));
        this.TargetSorter = new GenericTargetSorter(this);
        // experienceValue = 35 (:59) is dead, see the class comment.
    }

    /** {@code applyEntityAttributes} (:62-68). The armor 6 of {@code getTotalArmorValue} (:231-233) is also the ARMOR base. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 6.0);
    }

    /** {@code entityInit} (:70-78): watchers 22 = 0, 21 = activity (1), 20 = 1; not sitting, not tamed (the defaults). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN, 0);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_SPYRO_FIRE, 1);
    }

    /** {@code writeEntityToNBT} (:80-85). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("SpyroActivity", this.entityData.get(DATA_ACTIVITY));
        par1NBTTagCompound.putInt("SpyroFire", this.entityData.get(DATA_SPYRO_FIRE));
        par1NBTTagCompound.putInt("StinkySkin", this.entityData.get(DATA_SKIN));
    }

    /** {@code readEntityFromNBT} (:87-94). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.activity = par1NBTTagCompound.getInt("SpyroActivity");
        this.entityData.set(DATA_ACTIVITY, this.activity);
        this.entityData.set(DATA_SPYRO_FIRE, par1NBTTagCompound.getInt("SpyroFire"));
        this.skin_color = par1NBTTagCompound.getInt("StinkySkin");
        this.entityData.set(DATA_SKIN, this.skin_color);
    }

    /** {@code getActivity} (:96-99): reads the watcher into the field, read by {@code StinkyModel}. */
    public int getActivity() {
        final int i = this.entityData.get(DATA_ACTIVITY);
        return this.activity = i;
    }

    /** {@code setActivity} (:101-104). */
    public void setActivity(final int par1) {
        this.activity = par1;
        this.entityData.set(DATA_ACTIVITY, par1);
    }

    /** {@code getSpyroFire} (:106-108). */
    public int getSpyroFire() {
        return this.entityData.get(DATA_SPYRO_FIRE);
    }

    /** {@code setSpyroFire} (:110-112). */
    public void setSpyroFire(final int par1) {
        this.entityData.set(DATA_SPYRO_FIRE, par1);
    }

    /** {@code getSkin} (:114-117): read by {@code RenderStinky}. */
    public int getSkin() {
        final int i = this.entityData.get(DATA_SKIN);
        return this.skin_color = i;
    }

    /** {@code setSkin} (:119-123): 0 first, then the value, which re-sends an unchanged skin in 1.7.10 as here. */
    public void setSkin(final int par1) {
        this.skin_color = par1;
        this.entityData.set(DATA_SKIN, 0);
        this.entityData.set(DATA_SKIN, par1);
    }

    // isAIEnabled (:125-127): every 1.21.1 mob runs goals.
    // canBreatheUnderwater (:129-131): final in 1.21.1, read from #minecraft:can_breathe_under_water.

    /** {@code mygetMaxHealth} (:133-135). */
    public int mygetMaxHealth() {
        return 100;
    }

    /** {@code fireResistance = 1000} (:45). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /**
     * {@code interact} (:137-205), in the original order; it ran on both sides, so does {@code mobInteract}. A
     * {@code true} return is {@code sidedSuccess}. {@code func_152115_b} sets the owner UUID ({@code ""} clears it),
     * {@code func_152114_e} is "is owner" (catalogue 5.10). The name-tag and lead order of 1.7.10
     * {@code interactFirst} is restored by {@link PetInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :138-142 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        if (!var2.isEmpty() && var2.is(Items.BEEF) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.level().random.nextInt(2) == 1) {
                        // PORT: setTamed + owner without TamableAnimal.tame's advancement trigger, as in Hydrolisc.
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
            // Consumed outside creative even on a stranger's tamed Stinky (:168-174).
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                this.setHealth((float) this.mygetMaxHealth());
                this.setOwnerUUID(null);
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && par1EntityPlayer.distanceToSqr(this) < 16.0 && this.isOwnedBy(par1EntityPlayer)) {
            if (!this.isSitting()) {
                this.setSitting(true);
                this.setActivity(1);
            } else {
                this.setSitting(false);
            }
            return success;
        }
        // :204 super.interact: EntityAnimal. isBreedingItem is not overridden (isWheat is a dead 1.6 name, :207-209), so
        // vanilla wheat makes a Stinky fall in love; createChild returns null, nothing is born (R18, 1:1).
        return HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, hand);
    }

    /** {@code isWheat} (:207-209): raw beef; dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.BEEF);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}, not overridden by Stinky: wheat. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(Items.WHEAT);
    }

    /** {@code canDespawn} (:211-213). 1.21.1 asks only a mob without the persistence flag. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.isTame();
    }

    /** {@code getLivingSound} (:215-217). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:219-221). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:223-225). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:227-229). */
    @Override
    protected float getSoundVolume() {
        return 0.6f;
    }

    /** {@code getTotalArmorValue} (:231-233) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 6;
    }

    /** {@code getDropItem} (:235-237): never used, {@code dropFewItems} is overridden. */
    protected Item getDropItem() {
        return Items.BEEF;
    }

    /** {@code dropFewItems} first, then the equipment roll. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:239-248): tamed only, 1..4 raw beef on the world random. */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.level().random.nextInt(4);
            ++var3;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.BEEF, 1));
            }
        }
    }

    /** {@code getSoundPitch} (:250-252). */
    protected float getSoundPitch() {
        return this.isBaby() ? ((this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.1f + 1.5f)
                : ((this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.1f + 1.0f);
    }

    /** 1.21.1 reads the living, hurt and death sound pitch from here. */
    @Override
    public float getVoicePitch() {
        return this.getSoundPitch();
    }

    // canTriggerWalking (:254-256) returns true: no NoStepTrigger (R20).
    // doesEntityNotTriggerPressurePlate (:264-266) returns false: the 1.21.1 default of isIgnoringBlockTriggers.

    /** {@code fall} (:258-259), empty. */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:261-262), empty: no fall distance, no landing particles. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:268-270) - daytime ({@code skylightSubtracted < 4}, see the class
     * comment for the Nether) and at most 2 Stinky in {@code expand(20, 10, 20)} ({@code findBuddies} :702-705). No
     * light, grass or collision test.
     *
     * <p>PORT: the buddy box is the type's spawn box at the position (Crab, Molenoid precedent).
     */
    public static boolean checkStinkySpawnRules(final EntityType<Stinky> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return HerbivoreSupport.isDaytime(level.getLevel())
                && level.getEntitiesOfClass(Stinky.class,
                        type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 10.0, 20.0)).size() <= 2;
    }

    /** {@code getCanSpawnHere} (:268-270) on the positioned instance. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return HerbivoreSupport.isDaytime(this.level()) && this.findBuddies() <= 2;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code createChild} (:272-274): no offspring. */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** {@code getAttackStrength} (:276-278). */
    public float getAttackStrength(final Entity par1Entity) {
        return 10.0f;
    }

    /** {@code attackEntityAsMob} (:280-284): a fixed 10 of mob damage, no knockback, no enchantments. */
    public boolean attackEntityAsMob(final Entity par1Entity) {
        final float var2 = this.getAttackStrength(par1Entity);
        final boolean var3 = par1Entity.hurt(this.damageSources().mobAttack(this), var2);
        return var3;
    }

    /** 1.21.1 name of {@code attackEntityAsMob}. */
    @Override
    public boolean doHurtTarget(final Entity target) {
        return this.attackEntityAsMob(target);
    }

    /** {@code attackEntityFrom} (:286-294): cactus does nothing; any other hit stands it up and starts a flight. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
            this.setSitting(false);
            this.setActivity(2);
        }
        return ret;
    }

    /** {@code canSeeTarget} (:296-298): no block between a point 0.75 above the feet and the target (PitchBlack precedent). */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code dropItemFront} (:300-304): a fresh item in front of the head, 0.9 up, added straight to the level.
     *
     * <p>PORT: the 1.7.10 {@code EntityItem} drew its initial motion from {@code Math.random}; 1.21.1's
     * {@code ItemEntity} draws it from the level random.
     */
    private void dropItemFront(final Item index, final int par1) {
        final float f = 0.75f + Math.abs(this.level().random.nextFloat() * 0.75f);
        final ItemEntity var3 = new ItemEntity(this.level(), this.getX() - f * Math.sin(Math.toRadians(this.getYHeadRot())),
                this.getY() + 0.9, this.getZ() + f * Math.cos(Math.toRadians(this.getYHeadRot())), new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /** {@code dropItemRear} (:306-310): a fresh item behind the body, 0.25 up. Same PORT note as {@link #dropItemFront}. */
    private void dropItemRear(final Item index, final int par1) {
        final float f = 0.55f + Math.abs(this.level().random.nextFloat() * 0.55f);
        final ItemEntity var3 = new ItemEntity(this.level(), this.getX() + f * Math.sin(Math.toRadians(this.getYHeadRot())),
                this.getY() + 0.25, this.getZ() - f * Math.cos(Math.toRadians(this.getYHeadRot())), new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /**
     * {@code onUpdate} (:312-378): the vanilla tick, then on the server 1 in 1750 a burp and one coal in front, 1 in 2000
     * a fart and one item behind by the server's {@code skin_color}.
     */
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.level().random.nextInt(1750) == 1) {
            this.playSound(SoundEvents.PLAYER_BURP, 1.0f, 1.0f);
            this.dropItemFront(Items.COAL, 1);
        }
        if (!this.level().isClientSide && this.level().random.nextInt(2000) == 2) {
            this.playSound(ModSounds.FART.get(), 1.0f, 1.5f);
            if (this.skin_color == 0) {
                this.dropItemRear(Items.BLAZE_POWDER, 1);
            }
            if (this.skin_color == 1) {
                this.dropItemRear(Items.ROTTEN_FLESH, 1);
            }
            if (this.skin_color == 2) {
                this.dropItemRear(Items.MELON_SEEDS, 1);
            }
            if (this.skin_color == 3) {
                this.dropItemRear(ModItems.URANIUM_NUGGET.get(), 1);
            }
            if (this.skin_color == 4) {
                this.dropItemRear(Items.WHEAT, 1);
            }
            if (this.skin_color == 5) {
                this.dropItemRear(Items.BRICK, 1);
            }
            if (this.skin_color == 6) {
                this.dropItemRear(Items.TORCH, 1);
            }
            if (this.skin_color == 7) {
                this.dropItemRear(Items.EMERALD, 1);
            }
            if (this.skin_color == 8) {
                this.dropItemRear(Items.GOLD_INGOT, 1);
            }
            if (this.skin_color == 9) {
                // Item.getItemFromBlock(Blocks.leaves) with damage 0: oak leaves.
                this.dropItemRear(Items.OAK_LEAVES, 1);
            }
            if (this.skin_color == 10) {
                this.dropItemRear(ModItems.TITANIUM_NUGGET.get(), 1);
            }
            if (this.skin_color == 11) {
                this.dropItemRear(ModItems.APPLETREE_SEED.get(), 1);
            }
            if (this.skin_color == 12) {
                this.dropItemRear(Items.DIAMOND, 1);
            }
            if (this.skin_color == 13) {
                this.dropItemRear(Items.SAND, 1);
            }
            if (this.skin_color == 14) {
                this.dropItemRear(Items.COBBLESTONE, 1);
            }
            if (this.skin_color == 15) {
                this.dropItemRear(Items.BONE, 1);
            }
            if (this.skin_color == 16) {
                this.dropItemRear(Items.STRING, 1);
            }
            if (this.skin_color == 17) {
                this.dropItemRear(ModItems.CHERRYTREE_SEED.get(), 1);
            }
            if (this.skin_color == 18) {
                this.dropItemRear(ModItems.PEACHTREE_SEED.get(), 1);
            }
        }
    }

    /**
     * The sensing cache of 1.7.10 {@code EntityLiving}; see {@link #legacySenses}.
     *
     * <p>PORT: {@code Mob.sensing} is private and cleared by the final {@code serverAiStep} on every tick; this cache is
     * cleared in {@link #aiStep()} under the original condition instead (PitchBlack precedent).
     */
    @Override
    public Sensing getSensing() {
        return this.legacySenses != null ? this.legacySenses : super.getSensing();
    }

    /**
     * {@code onLivingUpdate} (:380-413), both sides: speed, the vanilla living tick (which runs {@code updateAITasks}),
     * then buoyancy, the skin re-roll, the flight target, the 20-tick watcher round and the flight damping.
     *
     * <p>Before the vanilla step, where 1.7.10 {@code EntityLivingBase.onLivingUpdate} called {@code updateAITasks} (the
     * AI branch: not movement-blocked, server side), the head of {@code updateAITasks} runs: {@code isDead} return, the
     * 1 in 200 revenge reset (:502-504), and the {@code activity != 2} decision for {@code super.updateAITasks()} (:505),
     * which clears the senses. Nothing between here and the goal selector of the same tick draws from the world random
     * or changes the activity, so the order of draws is the original's.
     */
    @Override
    public void aiStep() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        if (!this.isImmobile() && this.isEffectiveAi() && !this.isRemoved()) {
            if (this.level().random.nextInt(200) == 1) {
                this.setLastHurtByMob(null);
            }
            this.groundAi = this.activity != 2;
            if (this.groundAi) {
                this.legacySenses.tick();
            }
        }
        super.aiStep();
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.07, 0.0));
        }
        if (!this.level().isClientSide && this.level().random.nextInt(2000) == 1) {
            final int i = this.level().random.nextInt(19);
            this.setSkin(i);
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.skin_color < 0) {
            this.skin_color = this.level().random.nextInt(19);
        }
        ++this.syncit;
        if (this.syncit > 20) {
            this.syncit = 0;
            if (this.level().isClientSide) {
                this.getActivity();
                this.getSkin();
            } else {
                int j = this.activity;
                this.setActivity(j);
                j = this.skin_color;
                this.setSkin(j);
            }
        }
        if (this.activity == 2) {
            final Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        }
    }

    /** {@code scan_it} (:415-496): one shell of the search cube, nearest coal ore wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isCoalOreAt(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isCoalOreAt(x - dx, y + i, z + j)) {
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
                if (this.isCoalOreAt(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isCoalOreAt(x + i, y - dy, z + j)) {
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
                if (this.isCoalOreAt(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isCoalOreAt(x + i, y + j, z - dz)) {
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
     * {@code bid == Blocks.coal_ore}.
     *
     * <p>PORT (R22, category as 1.21.1 ships it): 1.7.10 had one coal ore; 1.21.1 splits the same ore into stone and
     * deepslate, both in {@code #minecraft:coal_ores}. The tag is taken, so a Stinky below Y 0 still finds its food.
     */
    private boolean isCoalOreAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(BlockTags.COAL_ORES);
    }

    /**
     * {@code updateAITasks} (:498-539), after {@code super.updateAITasks()}: the head (dead check, revenge reset, ground
     * decision) ran in {@link #aiStep()}; the task list, navigator and helpers of {@code super} are the wrapped goals and
     * controls of the vanilla AI step.
     *
     * <p>PORT: {@code Mob.serverAiStep} is final; the move, look and jump helpers that 1.7.10 ran inside
     * {@code super.updateAITasks()} run after this method in 1.21.1 (PitchBlack, EntityCannonFodder precedent). They
     * still obey the ground decision of this tick's start, not an activity this method sets.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        if (this.groundAi) {
            // updateAITick inside super.updateAITasks(): EntityAnimal's love reset for a non-zero age, which is
            // Animal.customServerAiStep in 1.21.1.
            super.customServerAiStep();
        }
        if (this.level().random.nextInt(100) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
        if (!this.isSitting()) {
            if (this.activity == 0) {
                this.setActivity(1);
            }
            if (this.level().random.nextInt(100) == 1) {
                if (this.level().random.nextInt(20) == 1) {
                    this.setActivity(2);
                } else {
                    this.setActivity(1);
                }
            }
            this.owner_flying = 0;
            if (this.isTame() && this.getOwner() != null) {
                // (EntityPlayer) cast: the owner of a 1.21.1 TamableAnimal is always a player.
                if (this.getOwner() instanceof Player e && e.getAbilities().flying) {
                    this.owner_flying = 1;
                    this.setActivity(2);
                }
            }
            if (this.activity == 1 && this.isTame() && this.getOwner() != null) {
                final LivingEntity e2 = this.getOwner();
                if (this.distanceToSqr(e2) > 256.0) {
                    this.setActivity(2);
                }
            }
            this.do_movement();
        }
    }

    /** {@code do_movement} (:541-677). {@code (int)} casts of coordinates are {@code Mth.floor} (R20). */
    private void do_movement() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        int do_new = 0;
        double ox = 0.0;
        double oy = 0.0;
        double oz = 0.0;
        int has_owner = 0;
        LivingEntity e = null;
        final RandomSource rand = this.level().random;
        if (this.currentFlightTarget == null) {
            do_new = 1;
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.activity == 2 && rand.nextInt(300) == 0) {
            do_new = 1;
        }
        if (this.isTame() && this.getOwner() != null) {
            e = this.getOwner();
            has_owner = 1;
            ox = e.getX();
            oy = e.getY();
            oz = e.getZ();
            if (this.distanceToSqr(e) > 100.0) {
                do_new = 1;
            }
            if (this.owner_flying != 0 && this.distanceToSqr(e) > 36.0) {
                do_new = 1;
            }
        }
        if (rand.nextInt(7) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                if (this.isTame() && this.getHealth() / this.mygetMaxHealth() < 0.25f) {
                    this.setActivity(2);
                    do_new = 0;
                    this.currentFlightTarget.set(Mth.floor(this.getX() + (this.getX() - e.getX())), Mth.floor(this.getY() + 1.0),
                            Mth.floor(this.getZ() + (this.getZ() - e.getZ())));
                } else {
                    this.setActivity(2);
                    this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                    do_new = 0;
                    if (this.distanceToSqr(e) < (3.0f + e.getBbWidth() / 2.0f) * (3.0f + e.getBbWidth() / 2.0f)) {
                        this.attackEntityAsMob(e);
                    }
                }
            }
        }
        if (this.activity == 1) {
            if (rand.nextInt(50) == 0 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
                this.closest = 99999;
                // :592-595: final boolean tx = false; tz = ty = tx = 0.
                this.tz = 0;
                this.ty = 0;
                this.tx = 0;
                for (int i = 1; i < 9; ++i) {
                    int j = i;
                    if (j > 2) {
                        j = 2;
                    }
                    if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) + 1, Mth.floor(this.getZ()), i, j, i)) {
                        break;
                    }
                    if (i >= 4) {
                        ++i;
                    }
                }
                if (this.closest < 99999) {
                    this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.25);
                    if (this.closest < 12) {
                        // setBlock(air, 0, 2): no mobGriefing check in the original (catalogue 5.15), none here.
                        this.level().setBlock(new BlockPos(this.tx, this.ty, this.tz), Blocks.AIR.defaultBlockState(),
                                HerbivoreSupport.LEGACY_FLAG_2);
                        this.heal(1.0f);
                        this.playSound(SoundEvents.PLAYER_BURP, 0.5f, rand.nextFloat() * 0.2f + 1.5f);
                    }
                }
            }
            return;
        }
        if (InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            do_new = 1;
        }
        if (do_new != 0) {
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                int gox = Mth.floor(this.getX());
                int goy = Mth.floor(this.getY());
                int goz = Mth.floor(this.getZ());
                if (has_owner == 1) {
                    gox = Mth.floor(ox);
                    goy = Mth.floor(oy);
                    goz = Mth.floor(oz);
                    if (this.owner_flying == 0) {
                        zdir = rand.nextInt(4) + 6;
                        xdir = rand.nextInt(4) + 6;
                    } else {
                        zdir = rand.nextInt(8);
                        xdir = rand.nextInt(8);
                    }
                } else {
                    zdir = rand.nextInt(5) + 6;
                    xdir = rand.nextInt(5) + 6;
                }
                if (rand.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (rand.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(gox + xdir, goy + rand.nextInt(6 + this.owner_flying * 2) - 2, goz + zdir);
                // PORT: isAir() also accepts cave and void air, which 1.7.10 generated as plain air (Bee precedent).
                bidIsAir = this.level().getBlockState(this.currentFlightTarget).isAir();
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
            }
        }
        double speed_factor = 1.0;
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        if (this.owner_flying != 0) {
            speed_factor = 1.75;
            if (this.isTame() && this.getOwner() != null) {
                e = this.getOwner();
                if (this.distanceToSqr(e) > 49.0) {
                    speed_factor = 3.5;
                }
            }
        }
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.15 * speed_factor;
        final double motionY = m.y + (Math.signum(var2) * 0.7 - m.y) * 0.21 * speed_factor;
        final double motionZ = m.z + (Math.signum(var3) * 0.5 - m.z) * 0.15 * speed_factor;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.setZza((float) (0.75 * speed_factor));
        this.setYRot(this.getYRot() + var5 / 3.0f);
    }

    /**
     * {@code isSuitableTarget} (:679-681): not peaceful, alive, seen through the (possibly stale) sensing cache, and a
     * Mothra or an {@code EntityMob} ({@link Monster}, see {@code MyUtils}).
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return this.level().getDifficulty() != Difficulty.PEACEFUL && par1EntityLiving != null && par1EntityLiving != this
                && par1EntityLiving.isAlive() && this.getSensing().hasLineOfSight(par1EntityLiving)
                && (par1EntityLiving instanceof Mothra || par1EntityLiving instanceof Monster);
    }

    /** {@code findSomethingToAttack} (:683-700): the nearest suitable living entity in {@code expand(12, 6, 12)} with a free ray. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 6.0, 12.0));
        Collections.sort(var5, this.TargetSorter);
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8 = null;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.isSuitableTarget(var8, false) && this.canSeeTarget(var8.getX(), var8.getY(), var8.getZ())) {
                return var8;
            }
        }
        return null;
    }

    /** {@code findBuddies} (:702-705): Stinky in {@code expand(20, 10, 20)}. */
    private int findBuddies() {
        final List<Stinky> var5 = this.level().getEntitiesOfClass(Stinky.class, this.getBoundingBox().inflate(20.0, 10.0, 20.0));
        return var5.size();
    }

    /** 1.7.10 {@code EntityTameable.setSitting}: see {@link HerbivoreSupport#setSitting}. */
    public void setSitting(final boolean sitting) {
        HerbivoreSupport.setSitting(this, sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * The navigator of 1.7.10 {@code EntityLiving}, updated inside {@code super.updateAITasks()} only: a flying Stinky
     * keeps its path untouched until it lands.
     */
    @Override
    protected PathNavigation createNavigation(final Level level) {
        return new GroundPathNavigation(this, level) {
            @Override
            public void tick() {
                if (Stinky.this.groundAi) {
                    super.tick();
                }
            }
        };
    }

    /**
     * {@code onDeath}: no override in the original and none in 1.7.10 {@code EntityTameable}, so the owner got no chat
     * message.
     *
     * <p>PORT: R22 (owner death message of tame OreSpawn animals 1:1 off) - {@code TamableAnimal.die} sends it in 1.21.1.
     * This is {@code LivingEntity.die} line by line without the owner message (Hydrolisc precedent).
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

    /**
     * A task of the 1.7.10 list, which {@code updateAITasks} ticked only while {@code activity != 2} (:505-507). In the
     * air nothing of it is asked: {@code shouldExecute} is not evaluated (no random draws), a running task keeps running
     * without {@code updateTask} and without {@code continueExecuting}, and is asked again after landing - as the
     * 1.7.10 {@code EntityAITasks} would have been (PitchBlack precedent; its wrapper is package-private there).
     */
    static final class GroundTask extends Goal {

        private final Stinky owner;
        private final Goal task;

        GroundTask(final Stinky owner, final Goal task) {
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
            return this.owner.groundAi && this.task.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.owner.groundAi || this.task.canContinueToUse();
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
            if (this.owner.groundAi) {
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

        private final Stinky owner;

        GroundMoveControl(final Stinky owner) {
            super(owner);
            this.owner = owner;
        }

        @Override
        public void tick() {
            if (this.owner.groundAi) {
                super.tick();
            }
        }
    }

    /** {@code EntityLookHelper}: see {@link GroundMoveControl}. */
    static final class GroundLookControl extends LookControl {

        private final Stinky owner;

        GroundLookControl(final Stinky owner) {
            super(owner);
            this.owner = owner;
        }

        @Override
        public void tick() {
            if (this.owner.groundAi) {
                super.tick();
            }
        }
    }

    /** {@code EntityJumpHelper}: see {@link GroundMoveControl}. */
    static final class GroundJumpControl extends JumpControl {

        private final Stinky owner;

        GroundJumpControl(final Stinky owner) {
            super(owner);
            this.owner = owner;
        }

        @Override
        public void tick() {
            if (this.owner.groundAi) {
                super.tick();
            }
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
