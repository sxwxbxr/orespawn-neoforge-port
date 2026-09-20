package com.swbr.orespawn.entity.pet;

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
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.sea.AttackSquid;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
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
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Squid;
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
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.RubberDucky} (RubberDucky.java:16-561): {@code rubber_ducky} ("Rubber Ducky",
 * OreSpawnMain.java:4057, tracking 64/1/false), a tameable duck that seeks water and pecks squids. Killed by a player it
 * comes back angrier: the new duck inherits the kill count, and from 5 kills on it is evil (other texture, double
 * damage, attacks players). Tamed with raw fish, untamed with a dead bush, bred with the Crystal Apple
 * (verhalten/entity-11.md).
 *
 * <p>Values: health 5 (:152-154), armor 1 (:156-158), speed 0.22 re-set every tick (:43, :107), attack attribute 6
 * registered but unused - the peck is 1, from 5 kills 2 (:426-433), hitbox 0.33 x 0.5 (:44), {@code fireResistance} 3
 * (:47). {@code experienceValue = 15} (:46) is never read: 1.7.10 {@code EntityAnimal.getExperiencePoints} returns
 * {@code 1 + rand.nextInt(3)}, as 1.21.1's {@code Animal.getBaseExperienceReward} does (W04).
 *
 * <p>State: DataWatcher 22 kill count, 23 attacking (:72-73); NBT {@code Killcount} (:492-500). {@code died},
 * {@code buddy}, {@code closest}/{@code tx}/{@code ty}/{@code tz} are transient. {@code entityInit} flips a negative
 * {@code growingAge} (:75-77); at that point the age is always 0, so it does nothing and is left out.
 */
public class RubberDucky extends TamableAnimal implements LegacyArmor {

    /** DataWatcher 22: kill count (:73). */
    private static final EntityDataAccessor<Integer> DATA_KILLCOUNT = SynchedEntityData.defineId(RubberDucky.class, EntityDataSerializers.INT);
    /** DataWatcher 23: attacking (:72). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(RubberDucky.class, EntityDataSerializers.INT);
    /** The registered name "Rubber Ducky" of the spawner test is the registry id here (R2). */
    private static final String SPAWNER_ID = OreSpawn.MOD_ID + ":rubber_ducky";

    private GenericTargetSorter TargetSorter;
    public boolean should_despawn;
    @Nullable
    private LivingEntity buddy;
    private float moveSpeed;
    private int killcount;
    private int died;
    /** Client-side animation scratch of {@code RubberDuckyModel} (:24); a plain data class, safe on the server. */
    private RenderInfo renderdata;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code RubberDucky(World)} (:30-60). */
    public RubberDucky(final EntityType<? extends RubberDucky> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.should_despawn = true;
        this.buddy = null;
        this.moveSpeed = 0.22f;
        this.killcount = 0;
        this.died = 0;
        this.renderdata = new RenderInfo();
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        this.moveSpeed = 0.22f;
        // setSize(0.33f, 0.5f) (:44) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(false) (:45) - a water path malus of 0 (vanilla default 8), as in Hydrolisc.
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        // experienceValue = 15 (:46) is dead; fireResistance = 3 (:47) is getFireImmuneTicks; isImmuneToFire = false
        // (:48) is the type default.
        this.renderdata = new RenderInfo();
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(1, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        // :54 - EntityAIMate a second time, as in the original.
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        // Items.fish with any metadata is HerbivoreSupport.isRawFish.
        this.goalSelector.addGoal(3, new EntityAITempt(this, 1.25, HerbivoreSupport::isRawFish, false));
        this.goalSelector.addGoal(4, new MyEntityAIWanderALot(this, 16, 1.0));
        // EntityLiving.class is Mob.
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Mob.class, 6.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as CaveFisher and Lizard.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:62-68). The armor 1 of {@code getTotalArmorValue} (:156-158) is also the ARMOR base. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.22f)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ARMOR, 1.0);
    }

    /** {@code entityInit} (:70-89): watchers 23 and 22 = 0; not sitting is the default; render data reset in the ctor. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_KILLCOUNT, 0);
    }

    /** {@code getRenderInfo} (:91-93). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:95-104). */
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

    /** {@code fireResistance = 3} (:47). */
    @Override
    protected int getFireImmuneTicks() {
        return 3;
    }

    /** {@code onUpdate} (:106-115), both sides: speed, the vanilla tick, buoyancy in water with a sink limit of -0.05. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.isInWater()) {
            final Vec3 m = this.getDeltaMovement();
            double motionY = m.y + 0.10000000149011612;
            if (motionY < -0.05000000074505806) {
                motionY = -0.05000000074505806;
            }
            this.setDeltaMovement(m.x, motionY, m.z);
        }
    }

    /**
     * {@code attackEntityFrom} (:117-150): any hit stands it up. When a player's hit killed it (first time only), the kill
     * count goes up and, below 10, one new duck with that count appears on the first air-over-ground spot of up to 20
     * random columns within 2 blocks.
     *
     * <p>{@code isDead} of 1.7.10 was only set by the removal; the dying duck is caught by {@code getHealth() <= 0} in both
     * versions. {@code (int)} casts of coordinates are {@code Mth.floor} (R20).
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        Entity w = null;
        w = par1DamageSource.getEntity();
        ret = super.hurt(par1DamageSource, par2);
        this.setSitting(false);
        if (!this.level().isClientSide && w != null && w instanceof Player && (this.isRemoved() || this.getHealth() <= 0.0f)
                && this.died == 0) {
            this.died = 1;
            this.setKillCount(++this.killcount);
            if (this.killcount < 10) {
                for (int m = 0; m < 20; ++m) {
                    int i = this.level().random.nextInt(3);
                    if (this.level().random.nextInt(2) == 1) {
                        i = -i;
                    }
                    int k = this.level().random.nextInt(3);
                    if (this.level().random.nextInt(2) == 1) {
                        k = -k;
                    }
                    for (int j = 3; j > -3; --j) {
                        final int x = Mth.floor(this.getX()) + i;
                        final int y = Mth.floor(this.getY()) + j;
                        final int z = Mth.floor(this.getZ()) + k;
                        // PORT: isAir() also accepts cave and void air, which 1.7.10 generated as plain air (Bee precedent).
                        if (this.level().getBlockState(new BlockPos(x, y + 1, z)).isAir()
                                && !this.level().getBlockState(new BlockPos(x, y, z)).isAir()) {
                            final Entity e = spawnCreature(this.level(), Mth.floor(this.getX()) + i + 1, y + 1, z);
                            if (e != null) {
                                final RubberDucky d = (RubberDucky) e;
                                d.setKillCount(this.killcount);
                            }
                            return ret;
                        }
                    }
                }
            }
        }
        return ret;
    }

    /** {@code mygetMaxHealth} (:152-154). */
    public int mygetMaxHealth() {
        return 5;
    }

    /** {@code getTotalArmorValue} (:156-158) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 1;
    }

    /** {@code fall} (:160-161), empty. */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:163-164), empty: no fall distance, no landing particles. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    // isAIEnabled (:166-168): every 1.21.1 mob runs goals.

    /**
     * {@code spawnCreature} (:170-179): {@code EntityList.createEntityByName("Rubber Ducky")} at the block corner, random
     * yaw, added to the world, then its living sound. No spawn-egg initialisation, as in the original.
     *
     * <p>PORT: the entity name is the registered type.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final double par2, final double par4, final double par6) {
        Entity var8 = null;
        var8 = ModEntities.RUBBER_DUCKY.get().create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            ((Mob) var8).playAmbientSound();
        }
        return var8;
    }

    /** {@code getLivingSound} (:181-186): 1 in 10 on the world random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(10) == 1) {
            return ModSounds.DUCK_HURT.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:188-190). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:192-194). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getSoundVolume} (:196-198). */
    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    /** {@code getSoundPitch} (:200-202); 1.21.1 reads the living, hurt and death sound pitch from here. */
    @Override
    public float getVoicePitch() {
        return 1.2f;
    }

    /** {@code getDropItem} (:204-212): 1 in 2 a feather, else 1 in 2 the duck's spawn egg, else nothing (world random). */
    @Nullable
    protected Item getDropItem() {
        if (this.level().random.nextInt(2) == 1) {
            return Items.FEATHER;
        }
        if (this.level().random.nextInt(2) == 1) {
            return ModItems.EGG_RUBBER_DUCKY.get();
        }
        return null;
    }

    /** The inherited 1.7.10 {@code EntityLiving.dropFewItems} with {@link #getDropItem()}, then the equipment roll. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code interact} (:214-283), in the original order; it ran on both sides, so does {@code mobInteract}. It returns
     * {@code true} for every click that reaches its end, so the 1.21.1 result is always {@code sidedSuccess}. The
     * name-tag and lead order of 1.7.10 {@code interactFirst} is restored by {@link PetInteractEvents}.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :215-219 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        // :220-222 super.interact: EntityAnimal breeding with the Crystal Apple.
        final InteractionResult bred = HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, hand);
        if (bred != InteractionResult.PASS) {
            return bred;
        }
        if (!var2.isEmpty() && HerbivoreSupport.isRawFish(var2) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(2) == 0) {
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
            // Consumed outside creative even on a stranger's tamed duck (:248-254).
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            if (!isRemote) {
                this.setTame(false, false);
                this.setOwnerUUID(null);
                this.spawnTamingParticles(false);
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isSitting() && this.getKillCount() < 5) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            return success;
        }
        return success;
    }

    /** {@code scan_it} (:285-366): one shell of the search cube, nearest water block wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isWaterAt(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWaterAt(x - dx, y + i, z + j)) {
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
                if (this.isWaterAt(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isWaterAt(x + i, y - dy, z + j)) {
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
                if (this.isWaterAt(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isWaterAt(x + i, y + j, z - dz)) {
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
     * {@code bid == Blocks.water || bid == Blocks.flowing_water}: both are the one liquid block {@code minecraft:water}
     * in 1.21.1. Waterlogged blocks were no water block in 1.7.10 and are none here (Hydrolisc precedent).
     */
    private boolean isWaterAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /**
     * {@code updateAITasks} (:368-424), after {@code super.updateAITasks()}: water search, kill count decay, regeneration,
     * the squid/player fight and the buddy.
     *
     * <p>PORT: {@code Mob.serverAiStep} is final; the move, look and jump helpers that 1.7.10 ran inside
     * {@code super.updateAITasks()} run after this method in 1.21.1 (EntityCannonFodder precedent).
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (!this.isInWater() && this.level().random.nextInt(50) == 0) {
            this.closest = 99999;
            // :375-378: final boolean tx = false; tz = ty = tx = 0.
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 14; ++i) {
                int j = i;
                if (j > 5) {
                    j = 5;
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
            }
        }
        if (this.killcount > 0 && this.level().random.nextInt(200) == 1) {
            this.setKillCount(--this.killcount);
        }
        if (this.getHealth() < this.mygetMaxHealth() && this.level().random.nextInt(300) == 1) {
            this.heal(1.0f);
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(5) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                if (this.distanceToSqr(e) < 12.0) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.attackEntityAsMob(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.2);
                }
            } else {
                if (this.buddy != null && !this.buddy.isRemoved() && this.level().random.nextInt(15) == 1) {
                    this.getNavigation().moveTo(this.buddy, 1.0);
                }
                this.setAttacking(0);
            }
        }
        if (this.buddy != null && !this.buddy.isRemoved() && this.level().random.nextInt(20) == 1) {
            this.getNavigation().moveTo(this.buddy, 1.0);
        }
    }

    /** {@code attackEntityAsMob} (:426-433): mob damage 1, from 5 kills 2; no knockback, no enchantments. */
    public boolean attackEntityAsMob(final Entity par1Entity) {
        float i = 1.0f;
        if (this.getKillCount() >= 5) {
            i = 2.0f;
        }
        final boolean flag = par1Entity.hurt(this.damageSources().mobAttack(this), i);
        return flag;
    }

    /** 1.21.1 name of {@code attackEntityAsMob}. */
    @Override
    public boolean doHurtTarget(final Entity target) {
        return this.attackEntityAsMob(target);
    }

    /**
     * {@code isSuitableTarget} (:435-465): squids of both kinds always; another duck is no target but becomes the buddy
     * with 1 in 10; a player in survival only from 5 kills. {@code EntitySquid} is {@link Squid} (the glow squid
     * included, which is a squid in 1.21.1).
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
        if (par1EntityLiving instanceof AttackSquid) {
            return true;
        }
        if (par1EntityLiving instanceof Squid) {
            return true;
        }
        if (par1EntityLiving instanceof RubberDucky && this.level().random.nextInt(10) == 1) {
            this.buddy = par1EntityLiving;
        }
        if (this.getKillCount() >= 5 && par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return false;
    }

    /**
     * {@code findSomethingToAttack} (:467-490): a live attack target first; otherwise the target and the buddy are
     * forgotten and the nearest suitable entity in {@code expand(8, 4, 8)} is taken. The list is gathered and sorted
     * before the target check, as in the original.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0, 4.0, 8.0));
        Collections.sort(var5, this.TargetSorter);
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8 = null;
        final LivingEntity e = this.getTarget();
        if (e != null && e.isAlive()) {
            return e;
        }
        this.setTarget(null);
        this.buddy = null;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code writeEntityToNBT} (:492-495). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("Killcount", this.killcount);
    }

    /** {@code readEntityFromNBT} (:497-500). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setKillCount(this.killcount = par1NBTTagCompound.getInt("Killcount"));
    }

    /** {@code getAttacking} (:502-504). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:506-508). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getKillCount} (:510-512): read by the renderer and the model. */
    public final int getKillCount() {
        return this.entityData.get(DATA_KILLCOUNT);
    }

    /** {@code setKillCount} (:514-517). */
    public final void setKillCount(final int par1) {
        this.entityData.set(DATA_KILLCOUNT, par1);
        this.killcount = par1;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:519-536) - a "Rubber Ducky" spawner in x/z -3..2, y 0..4 always allows
     * the spawn, otherwise Y at least 50 and daytime; no light or collision test.
     *
     * <p>PORT (R18 case 1): the original cast the tile entity without a null check; {@code HerbivoreSupport.spawnerNearby}
     * skips a spawner block without its block entity (StinkBug precedent).
     */
    public static boolean checkRubberDuckySpawnRules(final EntityType<RubberDucky> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (HerbivoreSupport.spawnerNearby(level, pos.getX(), pos.getY(), pos.getZ(), SPAWNER_ID)) {
            return true;
        }
        return pos.getY() >= 50.0 && HerbivoreSupport.isDaytime(level.getLevel());
    }

    /** {@code getCanSpawnHere} (:519-536) on the positioned instance; {@code (int)} casts are {@code Mth.floor} (R20). */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (HerbivoreSupport.spawnerNearby(level, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()), SPAWNER_ID)) {
            return true;
        }
        return this.getY() >= 50.0 && HerbivoreSupport.isDaytime(this.level());
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code canDespawn} (:538-544): a baby is made persistent and never despawns; an adult only without the persistence
     * flag, untamed and with {@code should_despawn} (always true in this class).
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired() && !this.isTame() && this.should_despawn;
    }

    /** {@code createChild} (:546-548). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:550-552): no owner (R18). */
    public RubberDucky spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.RUBBER_DUCKY.get().create(this.level());
    }

    /** {@code isWheat} (:554-556): raw fish; dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && HerbivoreSupport.isRawFish(par1ItemStack);
    }

    /** {@code isBreedingItem} (:558-560). */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
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

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
