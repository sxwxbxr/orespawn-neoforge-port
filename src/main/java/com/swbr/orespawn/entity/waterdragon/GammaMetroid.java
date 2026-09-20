package com.swbr.orespawn.entity.waterdragon;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import com.swbr.orespawn.util.MyUtils;
import com.swbr.orespawn.world.dimension.danger.WorldProviderOreSpawn4;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.GammaMetroid} (GammaMetroid.java:17-490), id {@code wtf} ("WTF?",
 * OreSpawnMain.java:3483, tracking 64/1/false). A tameable cave monster: wild it attacks players and animals (never
 * monsters), and it eats stone to heal (verhalten/entity-08.md).
 *
 * <p>Values: {@code GammaMetroid_stats} (100/10/12), speed 0.15 re-set every tick (:29, :34, :153-156),
 * {@code fireResistance} 1000 (:38). {@code experienceValue = 20} (:37) is never read: 1.7.10
 * {@code EntityAnimal.getExperiencePoints} returned {@code 1 + rand.nextInt(3)}, which is 1.21.1's
 * {@code Animal.getBaseExperienceReward} (W04 lesson). State: only the {@code EntityTameable} flags and owner;
 * {@code closest}/{@code tx}/{@code ty}/{@code tz} are transient.
 *
 * <p>Not carried over: {@code getDropItem} iron (:197-199, shadowed by {@code dropFewItems}); {@code isAIEnabled}
 * (:166-168); {@code onLivingUpdate} (:170-172, only {@code super}); {@code isWheat} (:483-485) is kept for the mapping.
 */
public class GammaMetroid extends TamableAnimal implements LegacyArmor, AttackableNonMob {

    /** The registered name the spawner scan of {@code getCanSpawnHere} compared ({@code "WTF?"}, R2). */
    static final String SPAWNER_ID = OreSpawn.MOD_ID + ":wtf";

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** Constructor (:26-48). {@code setSize(1.5f, 1.5f)} (:35) is the entity type's size (R9). */
    public GammaMetroid(final EntityType<? extends GammaMetroid> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.15f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        this.moveSpeed = 0.15f;
        // PORT: getNavigator().setAvoidsWater(true) (:36) is a water path malus of -1 (W06 precedent, Scorpion W07).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.GammaMetroid_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:50-56) at registration time (R3); armor from {@code getTotalArmorValue} (:162-164). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.GammaMetroid_stats(StatSource.EARLY);
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.15f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** The tasks of the constructor (:40-47), same priorities. */
    @Override
    protected void registerGoals() {
        // PORT: EntityAISwimming is FloatGoal, as in W01-W08.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new EntityAITempt(this, 1.2000000476837158, GammaMetroid::isIronIngot, false));
        this.goalSelector.addGoal(4, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code Items.iron_ingot}. */
    private static boolean isIronIngot(final ItemStack stack) {
        return stack.is(Items.IRON_INGOT);
    }

    /** {@code fireResistance = 1000} (:38). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code canDespawn} (:58-64): babies become persistent; adults despawn unless tamed or persistent. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isTame() && !this.isPersistenceRequired();
    }

    /**
     * {@code attackEntityAsMob} (:66-69): a fixed {@code GammaMetroid_stats.attack} as mob damage - no attribute, no
     * enchantments, no vanilla knockback.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), (float) MobStats.GammaMetroid_stats().attack());
        return var4;
    }

    /**
     * {@code interact} (:71-151), in the original order; it ran on both sides, so does {@code mobInteract}. A
     * {@code true} return is {@code sidedSuccess}. {@code func_152115_b} sets the owner UUID ({@code ""} clears it),
     * {@code func_152114_e} is "is owner" (catalogue 5.10). The name tag and a stranger's lead reach this method first
     * through {@link WaterDragonInteractEvents}, as in 1.7.10.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :72-76 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        // :77-79 super.interact: EntityAnimal breeding with the Crystal Apple.
        final InteractionResult bred = HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, hand);
        if (bred != InteractionResult.PASS) {
            return bred;
        }
        if (!var2.isEmpty() && var2.is(Items.IRON_INGOT) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(3) == 0) {
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
            // Consumed outside creative even on a stranger's tamed WTF? (:105-111).
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.DEAD_BUSH) && par1EntityPlayer.distanceToSqr(this) < 25.0
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
        if (this.isTame() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
            if (!this.isSitting()) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            return success;
        }
        return InteractionResult.PASS;
    }

    /** {@code onUpdate} (:153-156), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:158-160). */
    public int mygetMaxHealth() {
        return MobStats.GammaMetroid_stats().health();
    }

    /** {@code getTotalArmorValue} (:162-164) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.GammaMetroid_stats().defense();
    }

    /** {@code getLivingSound} (:174-179): 1 in 5 on the world random, otherwise silent. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(5) == 1) {
            return ModSounds.WTF_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:181-183). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:185-187). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:189-191). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:193-195): fixed 1.0, no baby pitch. */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code dropItemRand} (:201-204): {@code new ItemStack(index, par1, 0)} one block up, {@code OreSpawnRand.nextInt(4)
     * - OreSpawnRand.nextInt(4)} off on x and z, added straight to the level.
     *
     * <p>PORT: the toss motion of {@code ItemEntity} is drawn from the level random, where 1.7.10's {@code EntityItem}
     * used {@code Math.random} ({@code ArthropodSupport.dropItemRand}, W07).
     */
    private void dropItemRand(final Item index, final int par1) {
        ArthropodSupport.dropItemRand(this, new ItemStack(index, par1), 4);
    }

    /** {@code dropFewItems} (:206-213): 5..14 gold nuggets and 6..15 iron ingots, both counts on {@code OreSpawnRand}. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 5 + OreSpawn.OreSpawnRand.nextInt(10), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.GOLD_NUGGET, 1);
        }
        for (int i = 6 + OreSpawn.OreSpawnRand.nextInt(10), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.IRON_INGOT, 1);
        }
    }

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code updateAITick} (:434-468) followed by {@code updateAITasks} (:215-234). 1.7.10 ran {@code updateAITick} inside
     * {@code super.updateAITasks()}, after the selectors and the navigation; 1.21.1 calls {@link #customServerAiStep} at
     * that point, so both bodies run here in that order: first the inherited step ({@code EntityAnimal.updateAITick},
     * i.e. {@code Animal.customServerAiStep}), then the stone meal, then the attack.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        this.updateAITick();
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(5) == 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) <= 9.0) {
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.25);
                }
            }
        }
    }

    /**
     * The body of {@code updateAITick} (:434-468) after its {@code super} call: (1 in 20 while hurt, or 1 in 100),
     * {@code PlayNicely} off and not sitting - search stone in shells of radius 1, 2, 3, 4, 6 around y+1 (height capped at
     * 2), walk there at 1.0 and, within squared distance 12 of the shell origin, remove the block (mobGriefing only),
     * heal 1 and burp.
     *
     * <p>PORT (R22): {@code Blocks.stone} was the one natural rock of 1.7.10; the port matches the category
     * {@code #minecraft:base_stone_overworld} (stone, granite, diorite, andesite, tuff, deepslate), as
     * {@code UltimatePickaxe} and {@code ItemMagicApple} do. The {@code mobGriefing} gamerule string is
     * {@link EventHooks#canEntityGrief}, which reads the same rule; flag 2 is {@link HerbivoreSupport#LEGACY_FLAG_2}
     * (W03 lesson). {@code (int)} coordinates are {@code Mth.floor} (R20). {@code "random.burp"} is
     * {@code entity.player.burp}.
     */
    private void updateAITick() {
        if (((this.level().random.nextInt(20) == 0 && this.getHealth() < this.mygetMaxHealth()) || this.level().random.nextInt(100) == 0)
                && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0 && !this.isSitting()) {
            this.closest = 99999;
            // :441-444: final boolean tx = false; tz = ty = tx = 0.
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 6; ++i) {
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
                this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                if (this.closest < 12) {
                    if (EventHooks.canEntityGrief(this.level(), this)) {
                        this.level().setBlock(new BlockPos(this.tx, this.ty, this.tz), Blocks.AIR.defaultBlockState(),
                                HerbivoreSupport.LEGACY_FLAG_2);
                    }
                    this.heal(1.0f);
                    this.playSound(SoundEvents.PLAYER_BURP, 0.5f, this.level().random.nextFloat() * 0.2f + 1.5f);
                }
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:236-272), order kept: not Peaceful, alive, not ignorable, visible, not another WTF?, no
     * monster, nothing while tame, a player unless creative; everything else is a target.
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof GammaMetroid) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return false;
        }
        if (this.isTame()) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@code findSomethingToAttack} (:274-294): nothing with {@code PlayNicely} or for a baby; the first suitable entity
     * of the sorted box {@code expand(10, 3, 10)}. Unlike the Water Dragon it keeps no attack target.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        if (this.isBaby()) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 3.0, 10.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:313-349). A spawner of {@code wtf} in x/z -3..2, y 0..4 allows it;
     * otherwise the monster light test must pass; in the Islands dimension ({@code DimensionID4}, R13
     * {@code orespawn:danger}) that is enough; elsewhere Y must be 50 or lower and the 2x2 column from y+1 to y+3 air.
     *
     * <p>PORT: the spawner is read through {@link HerbivoreSupport#spawnerNearby} (StinkBug, W06); 1.7.10
     * {@code MobSpawnerBaseLogic} asked {@code getCanSpawnHere} too. {@code isValidLightLevel} (:296-311) is the
     * {@code EntityMob} rule and lies once in {@link LegacyLightLevel} (R21); it draws from the spawn's random, because
     * no entity exists yet. {@code bid != Blocks.air} is {@code !isAir()} (cave and void air count as air). At chunk
     * generation R23 skips this predicate.
     */
    public static boolean checkGammaMetroidSpawnRules(final EntityType<GammaMetroid> type, final ServerLevelAccessor level,
                                                      final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (HerbivoreSupport.spawnerNearby(level, pos.getX(), pos.getY(), pos.getZ(), SPAWNER_ID)) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (level.getLevel().dimension() == WorldProviderOreSpawn4.DIMENSION) {
            return true;
        }
        if (pos.getY() > 50.0) {
            return false;
        }
        for (int k = -1; k < 1; ++k) {
            for (int j = -1; j < 1; ++j) {
                for (int i = 1; i < 4; ++i) {
                    if (!level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + i, pos.getZ() + k)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkGammaMetroidSpawnRules}; {@code Animal}'s grass and light rule is not asked. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code scan_it} (:351-432): one shell of the search cube, nearest stone block wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isStoneAt(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isStoneAt(x - dx, y + i, z + j)) {
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
                if (this.isStoneAt(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isStoneAt(x + i, y - dy, z + j)) {
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
                if (this.isStoneAt(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isStoneAt(x + i, y + j, z - dz)) {
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

    /** {@code bid == Blocks.stone}; see {@link #updateAITick()} for the R22 category. */
    private boolean isStoneAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(BlockTags.BASE_STONE_OVERWORLD);
    }

    /** {@code createChild} (:470-472). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /**
     * {@code spawnBabyAnimal} (:474-481): a new WTF?; with a tame parent the code re-sets the parent's own owner (a
     * no-op) and marks the baby tame - <b>without an owner</b>. R18: 1:1, the baby cannot be commanded by anyone.
     */
    public GammaMetroid spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        final GammaMetroid w = (GammaMetroid) this.getType().create(this.level());
        if (w != null && this.isTame()) {
            this.setOwnerUUID(this.getOwnerUUID());
            w.setTame(true, false);
        }
        return w;
    }

    /** {@code isWheat} (:483-485): iron ingots; dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.IRON_INGOT);
    }

    /** {@code isBreedingItem} (:487-489). */
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
     * {@code onDeath}: no override in the original, and 1.7.10 {@code EntityTameable} did not override it either.
     *
     * <p>PORT: R22 (owner death message of tame OreSpawn animals 1:1 off) - {@code LivingEntity.die} line by line
     * without {@code TamableAnimal}'s owner message, as in {@code Hydrolisc} and {@link WaterDragon#die}.
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
