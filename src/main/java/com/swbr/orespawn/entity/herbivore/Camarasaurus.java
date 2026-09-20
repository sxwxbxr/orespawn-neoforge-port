package com.swbr.orespawn.entity.herbivore;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Camarasaurus} (Camarasaurus.java:14-356): {@code camarasaurus}, a tameable
 * long-necked plant eater ("Camarasaurus", OreSpawnMain.java:3415, tracking 64/1/false). Tamed with a red apple,
 * bred with the Crystal Apple, eats leaves and plants.
 *
 * <p>Values: health 20 (:215-217), speed 0.2 re-set every tick (:24, :64-67), attack 1 registered but never used
 * (:52), hitbox 0.5 x 1.2 (:29), {@code fireResistance} 100 (:31). {@code experienceValue = 5} (:34) is never read
 * ({@code Animal.getBaseExperienceReward}, W04). State: only the {@code EntityTameable} flags and owner, no NBT of
 * its own. {@code attackEntityFrom} (:327-331) only calls {@code super} and needs no override.
 *
 * <p>Original bugs kept (R18): the offspring has no owner; untaming is impossible.
 */
public class Camarasaurus extends TamableAnimal {

    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Camarasaurus(World)} (:22-45). */
    public Camarasaurus(final EntityType<? extends Camarasaurus> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.2f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // setSize(0.5f, 1.2f) (:29) is the entity type's size (R9).
        this.moveSpeed = 0.2f;
        // fireResistance = 100 (:31) is getFireImmuneTicks().
        // PORT: getNavigator().setAvoidsWater(true) (:32) - a water path malus of -1; MyEntityAIFollowOwner lifts it
        // while following, like the 1.7.10 goal did.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setSitting(false);
        // experienceValue = 5 (:34) is dead.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        this.goalSelector.addGoal(3, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(4, new EntityAITempt(this, 1.2000000476837158, stack -> stack.is(Items.APPLE), false));
        this.goalSelector.addGoal(5, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(6, new EntityAIWatchClosest(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(8, new EntityAILookIdle(this));
        // :44; homes instead of village doors (R18, see EntityAIMoveIndoors).
        this.goalSelector.addGoal(9, new EntityAIMoveIndoors(this));
    }

    /** {@code applyEntityAttributes} (:47-53). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.2f)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    // entityInit (:55-58) repeats setSitting(false); the flags are not built at that point in 1.21.1, the
    // constructor sets it.

    /** {@code fireResistance = 100} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** Spawn predicate: {@code getCanSpawnHere} (:60-62), Y at least 50 and day. */
    public static boolean checkCamarasaurusSpawnRules(final EntityType<Camarasaurus> type, final ServerLevelAccessor level,
                                                      final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && HerbivoreSupport.isDaytime(level.getLevel());
    }

    /** {@code getCanSpawnHere} (:60-62) on the positioned instance. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0 && HerbivoreSupport.isDaytime(this.level());
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code onUpdate} (:64-67), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code fall} (:69-83). */
    @Override
    public boolean causeFallDamage(final float par1, final float multiplier, final DamageSource source) {
        return HerbivoreSupport.legacyFall(this, par1);
    }

    /** {@code scan_it} (:85-166): one shell of the search cube, nearest plant wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isPlantAt(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isPlantAt(x - dx, y + i, z + j)) {
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
                if (this.isPlantAt(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isPlantAt(x + i, y - dy, z + j)) {
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
                if (this.isPlantAt(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isPlantAt(x + i, y + j, z - dz)) {
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
     * {@code bid == Blocks.leaves || vine || tallgrass || cactus || double_plant} (:90-91), compared by block, so every
     * metadata counted. PORT: the flattened 1.21.1 blocks of those ids - {@code tallgrass} is short grass and fern
     * (its unused meta 0 "shrub" flattened into dead bush, which would add every desert dead bush, so it is left out);
     * {@code double_plant} is sunflower, lilac, tall grass, large fern, rose bush and peony. PORT: R22 - {@code leaves}
     * is the category, see {@link #isLegacyLeaves}.
     */
    private boolean isPlantAt(final int x, final int y, final int z) {
        final BlockState bid = this.level().getBlockState(new BlockPos(x, y, z));
        return isLegacyLeaves(bid)
                || bid.is(Blocks.VINE)
                || bid.is(Blocks.SHORT_GRASS) || bid.is(Blocks.FERN)
                || bid.is(Blocks.CACTUS)
                || bid.is(Blocks.SUNFLOWER) || bid.is(Blocks.LILAC) || bid.is(Blocks.TALL_GRASS) || bid.is(Blocks.LARGE_FERN)
                || bid.is(Blocks.ROSE_BUSH) || bid.is(Blocks.PEONY);
    }

    /**
     * {@code Blocks.leaves} (:90-91): one block with oak, spruce, birch and jungle as metadata 0-3; acacia and dark oak
     * were the separate {@code Blocks.leaves2}.
     *
     * <p>PORT (R22): "leaves" is a category, so every block of {@code #minecraft:leaves} counts - acacia, dark oak,
     * azalea, mangrove and cherry included. OreSpawn's own leaves are excluded: the port adds them to that tag
     * ({@code data/minecraft/tags/block/leaves.json}), but they were separate blocks the original never compared with
     * {@code Blocks.leaves} - the same reading as {@code MonsterSupport.isLegacyLeaves}.
     */
    private static boolean isLegacyLeaves(final BlockState state) {
        return state.is(BlockTags.LEAVES)
                && !OreSpawn.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }

    /**
     * {@code updateAITick} (:168-205): forget the revenge target 1 in 200; then, not sitting and (1 in 20 while hurt
     * or 1 in 250) with {@code PlayNicely} 0, search plants like the Baryonyx searches grass, walk there and within
     * squared distance 12 remove it (mob griefing only) and heal 1 with a burp.
     *
     * <p>PORT: {@code mobGriefing} is {@link EventHooks#canEntityGrief}; {@code (int)} coordinates are
     * {@code Mth.floor} (R20); {@code isSitting()} is the synced pose flag.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
        if (!this.isSitting()
                && ((this.level().random.nextInt(20) == 0 && this.getCamarasaurusHealth() < this.mygetMaxHealth())
                    || this.level().random.nextInt(250) == 0)
                && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.closest = 99999;
            // :178-181: final boolean tx = false; tz = ty = tx = 0.
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 1; i < 11; ++i) {
                int j = i;
                if (j > 2) {
                    j = 2;
                }
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
                    if (EventHooks.canEntityGrief(this.level(), this)) {
                        final BlockPos eaten = new BlockPos(this.tx, this.ty, this.tz);
                        final BlockState plant = this.level().getBlockState(eaten);
                        this.level().setBlock(eaten, Blocks.AIR.defaultBlockState(), HerbivoreSupport.LEGACY_FLAG_2);
                        // Blocks.leaves was a BlockLeaves; its breakBlock ran whatever the flags (see LEGACY_FLAG_2).
                        // Vine, tall grass, cactus and double plant had no breakBlock of their own.
                        if (isLegacyLeaves(plant)) {
                            HerbivoreSupport.legacyLeavesBreakBlock(this.level(), eaten);
                        }
                    }
                    this.heal(1.0f);
                    this.playSound(SoundEvents.PLAYER_BURP, 1.0f, this.level().random.nextFloat() * 0.2f + 0.9f);
                }
            }
        }
    }

    // isAIEnabled (:207-209): every 1.21.1 mob runs goals. canBreatheUnderwater (:211-213) false is the default.

    /** {@code mygetMaxHealth} (:215-217). */
    public int mygetMaxHealth() {
        return 20;
    }

    /** {@code getCamarasaurusHealth} (:219-221), read by {@code CamarasaurusModel}. */
    public int getCamarasaurusHealth() {
        return (int) this.getHealth();
    }

    /**
     * {@code interact} (:223-287), in the original order; it ran on both sides, so does {@code mobInteract}. A
     * {@code true} return is {@code sidedSuccess}. {@code func_152115_b} sets the owner UUID, {@code func_152114_e}
     * is "is owner" (catalogue 5.10, as in Girlfriend).
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // PORT: 1.7.10 had one hand and read inventory.getCurrentItem(); the off hand does nothing.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final boolean isRemote = this.level().isClientSide;
        final InteractionResult success = InteractionResult.sidedSuccess(isRemote);
        // :224-228 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        // :229-231 super.interact: EntityAnimal breeding with the Crystal Apple.
        final InteractionResult bred = HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, hand);
        if (bred != InteractionResult.PASS) {
            return bred;
        }
        if (!var2.isEmpty() && var2.is(Items.APPLE) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isTame()) {
                if (!isRemote) {
                    if (this.random.nextInt(2) == 0) {
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
        if (this.isTame() && !var2.isEmpty() && var2.is(Items.NAME_TAG) && par1EntityPlayer.distanceToSqr(this) < 16.0
                && this.isOwnedBy(par1EntityPlayer)) {
            this.setCustomName(var2.getHoverName());
            HerbivoreSupport.consumeOne(par1EntityPlayer, var2);
            return success;
        }
        if (this.isTame() && this.isOwnedBy(par1EntityPlayer) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!this.isSitting()) {
                this.setSitting(true);
            } else {
                this.setSitting(false);
            }
            return success;
        }
        return InteractionResult.PASS;
    }

    /** {@code getLivingSound} (:289-294): silent either way. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isSitting()) {
            return null;
        }
        return null;
    }

    /** {@code getHurtSound} (:296-298). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRYO_HURT.get();
    }

    /** {@code getDeathSound} (:300-302). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CRYO_DEATH.get();
    }

    /** {@code getSoundVolume} (:304-306). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code dropFewItems} first, then the equipment roll; {@code getDropItem} (:308-310) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:312-321): tamed only, 2..6 poppies ({@code red_flower} meta 0). */
    protected void dropFewItems(final boolean par1, final int par2) {
        int var3 = 0;
        if (this.isTame()) {
            var3 = this.random.nextInt(5);
            var3 += 2;
            for (int var4 = 0; var4 < var3; ++var4) {
                this.spawnAtLocation(new ItemStack(Items.POPPY, 1));
            }
        }
    }

    /** {@code getSoundPitch} (:323-325). */
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
     * {@code canDespawn} (:333-339): a baby becomes persistent and never despawns; an adult only when not persistent
     * and not tamed.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired() && !this.isTame();
    }

    /** {@code createChild} (:341-343). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:345-347): no owner (R18). */
    public Camarasaurus spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.CAMARASAURUS.get().create(this.level());
    }

    /** {@code isWheat} (:349-351): dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:353-355). */
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
     * {@code onDeath}: no override in the original (Camarasaurus.java:14, {@code extends EntityTameable}), and 1.7.10
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
