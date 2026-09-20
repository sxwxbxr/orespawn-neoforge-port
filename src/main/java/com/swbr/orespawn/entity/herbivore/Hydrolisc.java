package com.swbr.orespawn.entity.herbivore;

import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIMoveIndoors;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.Hydrolisc} (Hydrolisc.java:14-379): {@code hydrolisc}, a tameable amphibious pet
 * that seeks water to heal and feeds its owner its own life ("Hydrolisc", OreSpawnMain.java:3423, tracking
 * 64/1/false). Tamed with raw fish, untamed with a dead bush, bred with the Crystal Apple, never despawns.
 *
 * <p>Values: health 100 (:218-220), speed 0.25 re-set every tick (:24, :59-62), attack 1 registered but never used
 * (:55-56), armor 10 (:163-165), every hit capped at 10 (:354-362), hitbox 0.5 (:29), {@code fireResistance} 100
 * (:30), breathes under water (:214-216, {@code #minecraft:can_breathe_under_water}). {@code experienceValue = 5}
 * (:43) is never read ({@code Animal.getBaseExperienceReward}, W04). State: only the {@code EntityTameable} flags
 * and owner; {@code closest}/{@code tx}/{@code ty}/{@code tz} are transient.
 *
 * <p>{@code getCanSpawnHere} is not overridden: the 1.7.10 {@code EntityAnimal} rule (grass below, light above 8,
 * path weight, free space) is 1.21.1's {@code Animal.checkAnimalSpawnRules} plus the inherited instance checks.
 */
public class Hydrolisc extends TamableAnimal implements LegacyArmor {

    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Hydrolisc(World)} (:22-44). */
    public Hydrolisc(final EntityType<? extends Hydrolisc> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.25f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // setSize(0.5f, 0.5f) (:29) is the entity type's size (R9); fireResistance = 100 (:30) is getFireImmuneTicks().
        // PORT: getNavigator().setAvoidsWater(false) (:31) - a water path malus of 0 (vanilla default 8), as in
        // MyEntityAIFollowOwner and Girlfriend.
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.setSitting(false);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(3, new MyEntityAIFollowOwner(this, 1.2f, 10.0f, 2.0f));
        // Items.fish with any metadata is HerbivoreSupport.isRawFish.
        this.goalSelector.addGoal(4, new EntityAITempt(this, 1.25, HerbivoreSupport::isRawFish, false));
        this.goalSelector.addGoal(5, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(8, new EntityAILookIdle(this));
        // :42; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(9, new EntityAIMoveIndoors(this));
        // experienceValue = 5 (:43) is dead.
    }

    // entityInit (:46-49) repeats setSitting(false); the flags are not built at that point in 1.21.1, the
    // constructor sets it.

    /**
     * {@code applyEntityAttributes} (:51-57). The armor 10 of {@code getTotalArmorValue} (:163-165) is also the
     * {@code ARMOR} base, so the vanilla reduction sees it when {@code legacyArmorFormula} is off; with the switch on
     * (default) the R5 formula reads {@link #getLegacyArmorValue()}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.ARMOR, 10.0);
    }

    /** {@code fireResistance = 100} (:30). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code onUpdate} (:59-62), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code scan_it} (:64-145): one shell of the search cube, nearest water block wins. */
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
     * in 1.21.1. Waterlogged blocks were no water block in 1.7.10 and are none here.
     */
    private boolean isWaterAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /** {@code fall} (:147-161). */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        return HerbivoreSupport.legacyFall(this, par1);
    }

    /** {@code getTotalArmorValue} (:163-165) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 10;
    }

    /**
     * {@code updateAITick} (:167-208): the inherited step first; forget the revenge target 1 in 200; not sitting and
     * (1 in 20 while hurt or 1 in 100), search water in shells of radius 1, 2, 3, 4, 5, 7, 9 around y-1 (height capped
     * at 4), walk to one block below it and, when already in water, heal 1; then, tamed, 1 in 10 give the owner 1
     * health for 1 of its own while above 20.
     *
     * <p>PORT (R18): {@code heal(-1.0f)} was the life transfer. NeoForge's {@code LivingEntity.heal} returns for any
     * amount at or below 0 after {@code EventHooks.onLivingHeal} (LivingEntity.java:1117-1123), so the effect of the
     * 1.7.10 method - {@code if (health > 0) setHealth(health + amount)} - is written out with {@code setHealth}.
     *
     * <p>The water sound {@code "splash"} has no namespace and was silent in 1.7.10 (R18: stays silent); its pitch
     * argument still drew from the world's random and still does. {@code (int)} coordinates are {@code Mth.floor}
     * (R20).
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        if (!this.isSitting()
                && ((this.level().random.nextInt(20) == 0 && this.getHydroHealth() < this.getMaxHealth())
                    || this.level().random.nextInt(100) == 0)) {
            this.closest = 99999;
            // :177-180: final boolean tx = false; tz = ty = tx = 0.
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 11; ++i) {
                int j = i;
                if (j > 4) {
                    j = 4;
                }
                if (this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()) - 1, Mth.floor(this.getZ()), i, j, i)) {
                    break;
                }
                if (i >= 5) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) (this.ty - 1), (double) this.tz, 1.0);
                if (this.isInWater()) {
                    this.heal(1.0f);
                    // playSound("splash", 1.0f, pitch): silent (R18), the random draw of the pitch stays.
                    final float splashPitch = this.level().random.nextFloat() * 0.2f + 0.9f;
                }
            }
        }
        if (this.level().random.nextInt(10) == 0 && this.isTame()) {
            final LivingEntity e = this.getOwner();
            if (e != null && e.getHealth() < e.getMaxHealth() && this.getHydroHealth() > 20) {
                e.heal(1.0f);
                // this.heal(-1.0f) - see the method Javadoc.
                final float f = this.getHealth();
                if (f > 0.0f) {
                    this.setHealth(f + -1.0f);
                }
            }
        }
    }

    // isAIEnabled (:210-212): every 1.21.1 mob runs goals.
    // canBreatheUnderwater (:214-216): final in 1.21.1, read from #minecraft:can_breathe_under_water.

    /** {@code mygetMaxHealth} (:218-220). */
    public int mygetMaxHealth() {
        return 100;
    }

    /** {@code onLivingUpdate} (:222-227): buoyancy in water, both sides. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04, 0.0));
        }
    }

    /** {@code getHydroHealth} (:229-231), read by {@code HydroliscModel}. */
    public int getHydroHealth() {
        return (int) this.getHealth();
    }

    /**
     * {@code interact} (:233-313), in the original order; it ran on both sides, so does {@code mobInteract}. A
     * {@code true} return is {@code sidedSuccess}. {@code func_152115_b} sets the owner UUID ({@code ""} clears it),
     * {@code func_152114_e} is "is owner" (catalogue 5.10).
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :234-238 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        // :239-241 super.interact: EntityAnimal breeding with the Crystal Apple.
        final InteractionResult bred = HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, hand);
        if (bred != InteractionResult.PASS) {
            return bred;
        }
        if (!var2.isEmpty() && HerbivoreSupport.isRawFish(var2) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(2) == 0) {
                        // PORT: setTamed + owner without TamableAnimal.tame's advancement trigger, as in Girlfriend.
                        this.setTame(true, false);
                        this.setOwnerUUID(par1EntityPlayer.getUUID());
                        this.spawnTamingParticles(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        this.heal(this.getMaxHealth() - this.getHealth());
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
                if (this.getMaxHealth() > this.getHealth()) {
                    this.heal(this.getMaxHealth() - this.getHealth());
                }
            }
            // Consumed outside creative even on a stranger's tamed Hydrolisc (:267-273).
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
        return InteractionResult.PASS;
    }

    /** {@code isWheat} (:315-317): raw fish; dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && HerbivoreSupport.isRawFish(par1ItemStack);
    }

    /** {@code getLivingSound} (:319-321). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:323-325). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRYO_HURT.get();
    }

    /** {@code getDeathSound} (:327-329). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:331-333). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code dropFewItems} first, then the equipment roll; {@code getDropItem} (:335-337) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:339-348): tamed only, 2..6 raw cod ({@code Items.fish} meta 0). */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.COD, 1));
            }
        }
    }

    /** {@code getSoundPitch} (:350-352). */
    protected float getSoundPitch() {
        return this.isBaby() ? ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.5f)
                : ((this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f);
    }

    /** 1.21.1 reads the living, hurt and death sound pitch from here. */
    @Override
    public float getVoicePitch() {
        return this.getSoundPitch();
    }

    /**
     * {@code attackEntityFrom} (:354-362): every hit capped at 10. The cap lands before the R5 armor replacement, like
     * the 1.7.10 cap before {@code damageEntity}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float p2 = par2;
        if (p2 > 10.0f) {
            p2 = 10.0f;
        }
        ret = super.hurt(par1DamageSource, p2);
        return ret;
    }

    /** {@code canDespawn} (:364-366). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code createChild} (:368-370). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:372-374): no owner (R18). */
    public Hydrolisc spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.HYDROLISC.get().create(this.level());
    }

    /** {@code isBreedingItem} (:376-378). */
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
     * {@code onDeath}: no override in the original (Hydrolisc.java:14, {@code extends EntityTameable}), and 1.7.10
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
