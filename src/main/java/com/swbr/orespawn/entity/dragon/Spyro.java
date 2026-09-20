package com.swbr.orespawn.entity.dragon;

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
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
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
 * Port of {@code danger.orespawn.Spyro} (Spyro.java:17-722), id {@code baby_dragon} ("Baby Dragon",
 * OreSpawnMain.java:3471, tracking 64/1/false). A tameable pet dragon on {@code EntityTameable}: it hunts monsters,
 * flies with its own motion steering on top of the vanilla movement, shoots small fireballs, bathes in lava and grows
 * into a {@link Dragon} at random or with a diamond, keeping its owner (verhalten/entity-12.md).
 *
 * <p>Unlike the Dragon, the Baby Dragon never bypasses the vanilla update: its flight ({@code activity} 2) is a motion
 * nudge written after each tick ({@code do_movement}), which gravity and the goals of the next tick work against.
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>With the fire put out it still shoots, 1 in 15 per chance (:617).</li>
 *   <li>A stranger's beef is eaten without effect (:213-246).</li>
 *   <li>{@code activity} 3 is tested (:629) but never set; the damping branch {@code * 0.61} (:470) is unreachable.</li>
 *   <li>{@code experienceValue = 35} (:57) is never read: 1.7.10 {@code EntityAnimal.getExperiencePoints} gives
 *       {@code 1 + rand.nextInt(3)}, as {@code Animal.getBaseExperienceReward} still does (W04 lesson).</li>
 *   <li>The field {@code activity} and DataWatcher 21 are written together on the server but read separately; the
 *       client keeps the field at 1 and reads the synced value.</li>
 * </ul>
 *
 * <p>Not carried over: {@code playTameEffect} is {@code EntityTameable}'s own, the same seven particles as 1.21.1's
 * {@code TamableAnimal.spawnTamingParticles}; {@code isAIEnabled} (:191) is the 1.21.1 default; {@code canBreatheUnderwater} (:195) is the
 * {@code minecraft:can_breathe_under_water} tag (final in 1.21.1, W04); {@code canTriggerWalking} and
 * {@code doesEntityNotTriggerPressurePlate} (:393, :403) keep the defaults, so no {@code NoStepTrigger} (R20);
 * {@code onLivingUpdate} (:483-485) only calls {@code super}.
 */
public class Spyro extends TamableAnimal implements AttackableNonMob, LegacyArmor {

    /** DataWatcher 20: SpyroFire (0 out, 1 lit), initial 1. */
    private static final EntityDataAccessor<Integer> DATA_SPYRO_FIRE = SynchedEntityData.defineId(Spyro.class, EntityDataSerializers.INT);
    /** DataWatcher 21: activity (1 ground, 2 flight), initial 1. */
    private static final EntityDataAccessor<Integer> DATA_ACTIVITY = SynchedEntityData.defineId(Spyro.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    public int activity;
    private int owner_flying;
    private boolean target_in_sight;
    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /**
     * {@code Spyro(World)} (:30-58) with {@code entityInit} (:68-75). {@code setSize(0.5f, 0.5f)} is the entity type's
     * size (R9); {@code fireResistance = 1000} is {@link #getFireImmuneTicks()}, {@code isImmuneToFire} the type's
     * {@code fireImmune()}.
     */
    public Spyro(final EntityType<? extends Spyro> type, final Level par1World) {
        super(type, par1World);
        this.activity = 1;
        this.owner_flying = 0;
        this.target_in_sight = false;
        this.moveSpeed = 0.3f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        this.moveSpeed = 0.3f;
        // getNavigator().setAvoidsWater(true) (:45): water is not walkable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // EntityMob.class is Monster.class (same reading as util.MyUtils).
        this.goalSelector.addGoal(2, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 0.30000001192092896, 0.4000000059604645));
        this.goalSelector.addGoal(3, new MyEntityAIFollowOwner(this, 1.15f, 12.0f, 2.0f));
        this.goalSelector.addGoal(4, new EntityAITempt(this, 1.25, stack -> stack.is(Items.BEEF), false));
        this.goalSelector.addGoal(5, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new MyEntityAIWander(this, 0.75f));
        this.goalSelector.addGoal(8, new EntityAILookIdle(this));
        // :55; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(9, new EntityAIMoveIndoors(this));
        this.TargetSorter = new GenericTargetSorter(this);
    }

    /**
     * {@code applyEntityAttributes} (:60-66): health 200, speed 0.3, attack 5 (never read: {@link #doHurtTarget} strikes
     * with {@link #getAttackStrength} 4).
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, W06 Ostrich);
     * 1.21.1 defaults to 0.6.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code entityInit} (:68-75): 21 = activity 1, 20 = SpyroFire 1. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVITY, 1);
        builder.define(DATA_SPYRO_FIRE, 1);
    }

    /** {@code fireResistance = 1000} (:43). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code writeEntityToNBT} (:77-81). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("SpyroActivity", this.entityData.get(DATA_ACTIVITY));
        par1NBTTagCompound.putInt("SpyroFire", this.entityData.get(DATA_SPYRO_FIRE));
    }

    /** {@code readEntityFromNBT} (:83-88). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.activity = par1NBTTagCompound.getInt("SpyroActivity");
        this.entityData.set(DATA_ACTIVITY, this.activity);
        this.entityData.set(DATA_SPYRO_FIRE, par1NBTTagCompound.getInt("SpyroFire"));
    }

    /** {@code scan_it} (:90-171): one shell of the lava search cube, the nearest lava block wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                BlockState bid = this.level().getBlockState(new BlockPos(x + dx, y + i, z + j));
                if (DragonSupport.isLava(bid)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.level().getBlockState(new BlockPos(x - dx, y + i, z + j));
                if (DragonSupport.isLava(bid)) {
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
                BlockState bid = this.level().getBlockState(new BlockPos(x + i, y + dy, z + j));
                if (DragonSupport.isLava(bid)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                bid = this.level().getBlockState(new BlockPos(x + i, y - dy, z + j));
                if (DragonSupport.isLava(bid)) {
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
                BlockState bid = this.level().getBlockState(new BlockPos(x + i, y + j, z + dz));
                if (DragonSupport.isLava(bid)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                bid = this.level().getBlockState(new BlockPos(x + i, y + j, z - dz));
                if (DragonSupport.isLava(bid)) {
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

    /** {@code getActivity} (:173-176), read by {@code SpyroModel}. */
    public int getActivity() {
        final int i = this.entityData.get(DATA_ACTIVITY);
        return i;
    }

    /** {@code setActivity} (:178-181): field and DataWatcher, no side check. */
    public void setActivity(final int par1) {
        this.activity = par1;
        this.entityData.set(DATA_ACTIVITY, this.activity);
    }

    /** {@code getSpyroFire} (:183-185). */
    public int getSpyroFire() {
        return this.entityData.get(DATA_SPYRO_FIRE);
    }

    /** {@code setSpyroFire} (:187-189). */
    public void setSpyroFire(final int par1) {
        this.entityData.set(DATA_SPYRO_FIRE, par1);
    }

    /** {@code mygetMaxHealth} (:199-201). */
    public int mygetMaxHealth() {
        return 200;
    }

    /** {@code getSpyroHealth} (:203-205). */
    public int getSpyroHealth() {
        return (int) this.getHealth();
    }

    /**
     * {@code interact} (:207-338), both sides like the original. A {@code true} answer is {@code sidedSuccess}. The
     * click order around it (name tag, a stranger's lead) is restored by {@link DragonInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final InteractionResult result = this.legacyInteract(par1EntityPlayer);
        return result;
    }

    /**
     * The body of {@code interact(EntityPlayer)} (:207-338); the last line, {@code super.interact}, is 1.7.10
     * {@code EntityAnimal.interact} ({@link HerbivoreSupport#legacyAnimalInteract}: wheat makes it fall in love, which
     * never leads anywhere). {@code func_152114_e} is "is owner".
     */
    InteractionResult legacyInteract(final Player par1EntityPlayer) {
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :208-212 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getMainHandItem();
        if (!var2.isEmpty() && var2.is(Items.BEEF) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.level().random.nextInt(2) == 1) {
                        // PORT: setTamed + owner without TamableAnimal.tame's advancement trigger, as in Girlfriend.
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
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                this.setHealth((float) this.mygetMaxHealth());
                this.setOwnerUUID(DragonSupport.parseOwner("")); // func_152115_b("")
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.ICE) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(0);
                par1EntityPlayer.sendSystemMessage(Component.literal("Baby Dragon fireballs extinguished."));
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DIAMOND) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer) && !isRemote) {
            Dragon d = null;
            d = DragonSupport.spawnCreature(this.level(), ModEntities.DRAGON.get(), this.getX(), this.getY(), this.getZ());
            if (d != null) {
                if (this.isTame()) {
                    d.setTame(true, false);
                    d.setOwnerUUID(DragonSupport.parseOwner(par1EntityPlayer.getUUID().toString()));
                }
                this.discard();
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.FLINT_AND_STEEL) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.spawnTamingParticles(true);
                this.level().broadcastEntityEvent(this, (byte) 6);
                this.setSpyroFire(1);
                par1EntityPlayer.sendSystemMessage(Component.literal("Baby Dragon fireballs lit!"));
            }
            // --stackSize on a damageable item: the whole flint and steel goes (R18).
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            this.setCustomName(var2.getHoverName());
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && par1EntityPlayer.distanceToSqr(this) < 16.0 && this.isOwnedBy(par1EntityPlayer)) {
            if (!this.isSitting()) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            return success;
        }
        return HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, InteractionHand.MAIN_HAND);
    }

    /** {@code isWheat} (:340-342): overrides nothing in 1.7.10; dead, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.BEEF);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}, not overridden: wheat, read by the {@code super.interact} step. */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(Items.WHEAT);
    }

    /** {@code canDespawn} (:344-346): only when not persistent and wild. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && !this.isTame();
    }

    /** {@code getTotalArmorValue} (:348-350) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 5;
    }

    /** {@code getLivingSound} (:352-360): a roar only in flight and not sitting. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        if (this.getActivity() != 2) {
            return null;
        }
        return ModSounds.ROAR.get();
    }

    /** {@code getHurtSound} (:362-364). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:366-368). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:370-372). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}; {@code getDropItem} (:374-376) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:378-387): tamed only, 1-4 raw beef ({@code dropItem}). */
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

    /** {@code getSoundPitch} (:389-391), from the world random. */
    protected float getSoundPitch() {
        return this.isBaby() ? ((this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.1f + 1.5f)
                : ((this.level().random.nextFloat() - this.level().random.nextFloat()) * 0.1f + 1.0f);
    }

    /** 1.21.1 reads the living, hurt and death sound pitch from here. */
    @Override
    public float getVoicePitch() {
        return this.getSoundPitch();
    }

    /** {@code fall} (:397-398): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:400-401): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** Spawn predicate: {@code getCanSpawnHere} (:407-409), by day and Y at least 50. */
    public static boolean checkSpyroSpawnRules(final EntityType<Spyro> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return HerbivoreSupport.isDaytime(level.getLevel()) && pos.getY() >= 50.0;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkSpyroSpawnRules}; {@code EntityAnimal}'s grass test was overridden. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code createChild} (:411-413). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** {@code getAttackStrength} (:415-417). */
    public float getAttackStrength(final Entity par1Entity) {
        return 4.0f;
    }

    /** {@code attackEntityAsMob} (:419-423): 4 as mob damage, nothing else. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final float var2 = this.getAttackStrength(par1Entity);
        final boolean var3 = par1Entity.hurt(this.damageSources().mobAttack(this), var2);
        return var3;
    }

    /**
     * {@code attackEntityFrom} (:425-431): only cactus is ignored. PORT: {@code getDamageType()} is
     * {@link DamageSource#getMsgId()}, the same string.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.getMsgId().equals("cactus")) {
            ret = super.hurt(par1DamageSource, par2);
        }
        return ret;
    }

    /**
     * {@code canSeeTarget} (:433-435): {@code rayTraceBlocks(from, to, false) == null} from 0.75 above the feet. 1.7.10
     * traced blocks by their selection box and ignored liquids ({@code ClipContext.Block.OUTLINE}, W06 GoldFish).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code onUpdate} (:437-481): the speed attribute, the vanilla update, buoyancy in water; then on the server the
     * 1 in 100000 growth into a Dragon (not while persistent), the flight damping, take-off when a tamed pet is more than
     * 16 blocks from its owner, and the flight steering.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.isInWater()) {
            final Vec3 m = this.getDeltaMovement();
            this.setDeltaMovement(m.x, m.y + 0.07, m.z);
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.currentFlightTarget == null) {
            // :447 (int) casts -> Mth.floor (DECISIONS R20), here and below.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.level().random.nextInt(100000) == 1 && !this.isPersistenceRequired()) {
            Dragon d = null;
            d = DragonSupport.spawnCreature(this.level(), ModEntities.DRAGON.get(), this.getX(), this.getY(), this.getZ());
            if (d != null) {
                if (this.isTame()) {
                    d.setTame(true, false);
                    d.setOwnerUUID(this.getOwnerUUID()); // func_152115_b(func_152113_b())
                }
                this.discard();
                return;
            }
        }
        if (this.activity == 2) {
            final Vec3 m = this.getDeltaMovement();
            if (this.getY() < this.currentFlightTarget.getY() + 2.0) {
                this.setDeltaMovement(m.x, m.y * 0.7, m.z);
            } else if (this.getY() > this.currentFlightTarget.getY() - 2.0) {
                this.setDeltaMovement(m.x, m.y * 0.5, m.z);
            } else {
                this.setDeltaMovement(m.x, m.y * 0.61, m.z);
            }
        }
        if (this.activity == 1 && this.isTame() && this.getOwner() != null) {
            final LivingEntity e = this.getOwner();
            if (this.distanceToSqr(e) > 256.0) {
                this.setActivity(2);
            }
        }
        this.do_movement();
    }

    /**
     * {@code updateAITick} (:487-551) followed by the tail of {@code updateAITasks} (:553-561): the animal tick only on
     * the ground, forget the revenge target with 1 in 200, then (not sitting) the lava search with 1 in 20, the random
     * take-off (1 in 100, then 1 in 8) and the flying owner; finally heal 1 with 1 in 100.
     *
     * <p>PORT: 1.21.1 {@code Mob.serverAiStep} is final; the original healed after the move, look and jump helpers, here
     * right before them. None of the three draws a random number.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        int fly = 0;
        if (this.activity == 1) {
            super.customServerAiStep();
        }
        if (!this.isRemoved()) {
            if (this.level().random.nextInt(200) == 1) {
                this.setLastHurtByMob(null);
            }
            if (!this.level().isClientSide && !this.isSitting()) {
                if (this.activity == 0) {
                    this.activity = 1;
                }
                if (this.level().random.nextInt(20) == 0) {
                    this.closest = 99999;
                    this.tz = 0;
                    this.ty = 0;
                    this.tx = 0;
                    for (int i = 1; i < 11; ++i) {
                        int j = i;
                        if (j > 4) {
                            j = 4;
                        }
                        // :516 (int) posX, (int) posY - 1, (int) posZ -> Mth.floor (DECISIONS R20).
                        if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()), i, j, i)) {
                            break;
                        }
                        if (i >= 6) {
                            ++i;
                        }
                    }
                    if (this.closest < 99999) {
                        this.setActivity(1);
                        this.getNavigation().moveTo((double) this.tx, (double) (this.ty - 1), (double) this.tz, 1.0);
                        if (this.isInLava()) {
                            this.heal(1.0f);
                            // playSound("splash", ...) (:528): an event without namespace, silent (R18); its pitch is still drawn.
                            this.level().random.nextFloat();
                        }
                    }
                }
                if (this.level().random.nextInt(100) == 1 && !this.target_in_sight) {
                    this.activity = 1;
                    if (this.level().random.nextInt(8) == 1) {
                        this.activity = 2;
                    }
                    this.setActivity(this.activity);
                }
                this.owner_flying = 0;
                if (this.isTame() && this.getOwner() != null) {
                    final Player e = (Player) this.getOwner();
                    if (e.getAbilities().flying) {
                        fly = 1;
                    }
                    if (fly == 1) {
                        this.owner_flying = 1;
                        this.setActivity(2);
                    }
                }
            }
        }
        // updateAITasks (:553-561): super.updateAITasks() has just run; its early isDead return is the one above.
        if (this.level().random.nextInt(100) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    /**
     * {@code do_movement} (:563-687), server, after the vanilla update: hunt or flee, pick a visible air waypoint around
     * the owner or itself, and nudge the motion towards it. Nothing happens while sitting or on the ground.
     */
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
        if (this.currentFlightTarget == null) {
            do_new = 1;
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.isSitting()) {
            return;
        }
        if (this.activity == 1) {
            return;
        }
        if (this.getActivity() == 2 && this.level().random.nextInt(300) == 0) {
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
        if (this.level().random.nextInt(6) == 1 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            e = this.findSomethingToAttack();
            if (e != null) {
                if (this.isTame() && this.getHealth() / this.mygetMaxHealth() < 0.25f) {
                    this.setActivity(2);
                    this.target_in_sight = false;
                    do_new = 0;
                    this.currentFlightTarget.set(Mth.floor(this.getX() + (this.getX() - e.getX())), Mth.floor(this.getY() + 1.0),
                            Mth.floor(this.getZ() + (this.getZ() - e.getZ())));
                } else {
                    this.setActivity(2);
                    this.target_in_sight = true;
                    this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                    this.getNavigation().moveTo(e, 1.25);
                    do_new = 0;
                    if (this.distanceToSqr(e) < (3.0f + e.getBbWidth() / 2.0f) * (3.0f + e.getBbWidth() / 2.0f)) {
                        this.doHurtTarget(e);
                    } else if (this.distanceToSqr(e) < 64.0 && !this.isInWater()
                            && ((this.getSpyroFire() == 1 && this.level().random.nextInt(10) == 0) || this.level().random.nextInt(15) == 1)) {
                        final SmallFireball var2 = DragonSupport.legacySmallFireball(this, e.getX() - this.getX(),
                                e.getY() + 0.25 - (this.getY() + 1.25), e.getZ() - this.getZ());
                        var2.moveTo(this.getX(), this.getY() + 1.25, this.getZ(), this.getYRot(), this.getXRot());
                        DragonSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f,
                                1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                        this.level().addFreshEntity(var2);
                    }
                }
            } else {
                this.target_in_sight = false;
            }
        }
        // ChunkCoordinates.getDistanceSquared(int, int, int) is a float of the int differences.
        final int cdx = this.currentFlightTarget.getX() - Mth.floor(this.getX());
        final int cdy = this.currentFlightTarget.getY() - Mth.floor(this.getY());
        final int cdz = this.currentFlightTarget.getZ() - Mth.floor(this.getZ());
        if ((float) (cdx * cdx + cdy * cdy + cdz * cdz) < 2.1f && this.getActivity() != 3) {
            do_new = 1;
        }
        if (do_new != 0 && !this.target_in_sight) {
            for (boolean airFound = false; !airFound && keep_trying != 0; --keep_trying) {
                int gox = Mth.floor(this.getX());
                int goy = Mth.floor(this.getY());
                int goz = Mth.floor(this.getZ());
                if (has_owner == 1) {
                    gox = Mth.floor(ox);
                    goy = Mth.floor(oy);
                    goz = Mth.floor(oz);
                    if (this.owner_flying == 0) {
                        zdir = this.level().random.nextInt(4) + 6;
                        xdir = this.level().random.nextInt(4) + 6;
                    } else {
                        zdir = this.level().random.nextInt(6);
                        xdir = this.level().random.nextInt(6);
                    }
                } else {
                    zdir = this.level().random.nextInt(5) + 6;
                    xdir = this.level().random.nextInt(5) + 6;
                }
                if (this.level().random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.level().random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(gox + xdir, goy + this.level().random.nextInt(9 + this.owner_flying * 2) - 4, goz + zdir);
                airFound = this.level().getBlockState(this.currentFlightTarget).isAir();
                if (airFound && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                        this.currentFlightTarget.getZ())) {
                    airFound = false;
                }
            }
        }
        double speed_factor = 0.5;
        final double var3 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var4 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var5 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
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
        double motionX = m.x;
        double motionY = m.y;
        double motionZ = m.z;
        motionX += (Math.signum(var3) * 0.5 - motionX) * 0.15 * speed_factor;
        motionY += (Math.signum(var4) * 0.7 - motionY) * 0.21 * speed_factor;
        motionZ += (Math.signum(var5) * 0.5 - motionZ) * 0.15 * speed_factor;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var6 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var7 = Mth.wrapDegrees(var6 - this.getYRot());
        this.setZza((float) (0.75 * speed_factor));
        this.setYRot(this.getYRot() + var7 / 3.0f);
    }

    /**
     * {@code isSuitableTarget} (:689-691): monsters and Mothra in sight, never another Baby Dragon. {@code EntityMob} is
     * {@link Monster} (util.MyUtils).
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return this.level().getDifficulty() != Difficulty.PEACEFUL && par1EntityLiving != null && par1EntityLiving != this
                && par1EntityLiving.isAlive() && this.getSensing().hasLineOfSight(par1EntityLiving)
                && !(par1EntityLiving instanceof Spyro)
                && (par1EntityLiving instanceof Mothra || par1EntityLiving instanceof Monster);
    }

    /**
     * {@code findSomethingToAttack} (:693-710): nothing under {@code PlayNicely}; else the first suitable one by the
     * sorter in ±12/6/12 that {@link #canSeeTarget} at its feet as well.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 6.0, 12.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false) && this.canSeeTarget(var8.getX(), var8.getY(), var8.getZ())) {
                return var8;
            }
        }
        return null;
    }

    /** 1.7.10 {@code EntityTameable.setSitting}: one synced flag; 1.21.1 splits it into order and pose. */
    public void setSitting(final boolean sitting) {
        HerbivoreSupport.setSitting(this, sitting);
    }

    /** 1.7.10 {@code EntityTameable.isSitting}: the synced flag, i.e. the pose. */
    public boolean isSitting() {
        return this.isInSittingPose();
    }

    /**
     * {@code onDeath}: not overridden, and 1.7.10 {@code EntityTameable} sent the owner no chat message.
     *
     * <p>PORT: R22 (owner death message of tame OreSpawn animals 1:1 off) - {@code TamableAnimal.die} in 1.21.1 sends
     * {@code getCombatTracker().getDeathMessage()} to the owner. Java cannot skip one super level, so this is
     * {@code LivingEntity.die} line by line (NeoForge 21.1 sources) without the owner message, as in
     * {@code Camarasaurus} (fix1).
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
