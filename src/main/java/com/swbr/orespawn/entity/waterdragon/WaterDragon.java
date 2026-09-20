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
import com.swbr.orespawn.entity.ai.MyEntityAIFollowOwner;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.projectile.WaterBall;
import com.swbr.orespawn.entity.sea.AttackSquid;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.WaterDragon} (WaterDragon.java:19-798), id {@code water_dragon} ("Water Dragon",
 * OreSpawnMain.java:3515, tracking 64/1/false). A tameable amphibious dragon: wild it attacks players and animals, tame
 * only monsters; it bites, fires a stream of water balls with the odd small fireball, heals in water and slowly dries
 * out on land without water in reach (verhalten/entity-14.md).
 *
 * <p>Values: {@code WaterDragon_stats} (150/20/8), speed 0.55 in water and 0.25 on land re-set every tick (:175,
 * :206-213), {@code fireResistance} 3 (:46), immune to fire (:47, the entity type's {@code fireImmune()}), breathes under
 * water (:795-797, tag {@code minecraft:can_breathe_under_water}). {@code experienceValue = 100} (:45) is never read:
 * 1.7.10 {@code EntityAnimal.getExperiencePoints} returned {@code 1 + rand.nextInt(3)}, which is 1.21.1's
 * {@code Animal.getBaseExperienceReward} (W04 lesson). State: DataWatcher 20 {@code attacking} (int) and the
 * {@code EntityTameable} flags and owner; no NBT of its own.
 *
 * <p>Not carried over: the {@code RenderInfo renderdata} scratch fields (:22, :71-81, :183-196) - the model never reads
 * them; {@code getAttackStrength} (:220-232, no caller); {@code getDropItem} fish (:254-256, shadowed by
 * {@code dropFewItems}); {@code isAIEnabled} (:202-204); {@code isWheat} (:787-789) is kept for the mapping only.
 */
public class WaterDragon extends TamableAnimal implements LegacyArmor, AttackableNonMob {

    /** DataWatcher 20: {@code attacking} - 0 idle, 1 biting, 2 water cannon; read by {@code WaterDragonModel}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(WaterDragon.class, EntityDataSerializers.INT);

    /** The registered name the spawner scan of {@code getCanSpawnHere} compared ({@code "Water Dragon"}, R2). */
    static final String SPAWNER_ID = OreSpawn.MOD_ID + ":water_dragon";

    private GenericTargetSorter TargetSorter;
    private int stream_count;
    private int hurt_timer;
    private float moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** Constructor (:31-58). {@code setSize(1.25f, 1.9f)} (:43) is the entity type's size (R9). */
    public WaterDragon(final EntityType<? extends WaterDragon> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.stream_count = 0;
        this.hurt_timer = 0;
        this.moveSpeed = 0.25f;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        this.moveSpeed = 0.25f;
        // PORT: getNavigator().setAvoidsWater(false) (:44) is a water path malus of 0 (Whale, W06; Hydrolisc).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.WaterDragon_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:60-66) at registration time (R3); armor from {@code getTotalArmorValue} (:198-200). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.WaterDragon_stats(StatSource.EARLY);
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** The tasks of the constructor (:50-57), same priorities. */
    @Override
    protected void registerGoals() {
        // PORT: EntityAISwimming is FloatGoal, as in W01-W08.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIFollowOwner(this, 2.0f, 10.0f, 2.0f));
        // Items.fish with any metadata is HerbivoreSupport.isRawFish.
        this.goalSelector.addGoal(3, new EntityAITempt(this, 1.2000000476837158, HerbivoreSupport::isRawFish, false));
        this.goalSelector.addGoal(4, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(5, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code entityInit} (:68-82); the {@code renderdata} resets are dropped with the scratch fields. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 3} (:46). */
    @Override
    protected int getFireImmuneTicks() {
        return 3;
    }

    /**
     * {@code interact} (:84-164), in the original order; it ran on both sides, so does {@code mobInteract}. A
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
        // :85-89 stackSize <= 0 -> null: an empty stack is ItemStack.EMPTY in 1.21.1.
        final ItemStack var2 = par1EntityPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        // :90-92 super.interact: EntityAnimal breeding with the Crystal Apple.
        final InteractionResult bred = HerbivoreSupport.legacyAnimalInteract(this, par1EntityPlayer, hand);
        if (bred != InteractionResult.PASS) {
            return bred;
        }
        if (!var2.isEmpty() && HerbivoreSupport.isRawFish(var2) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
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
            // Consumed outside creative even on a stranger's tamed dragon (:118-124).
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

    /** {@code canDespawn} (:166-172): babies become persistent; adults despawn unless persistent or tamed. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired() && !this.isTame();
    }

    /** {@code onUpdate} (:174-177), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:179-181). */
    public int mygetMaxHealth() {
        return MobStats.WaterDragon_stats().health();
    }

    /** {@code getTotalArmorValue} (:198-200) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.WaterDragon_stats().defense();
    }

    /** {@code onLivingUpdate} (:206-214), both sides: the speed for the next tick follows the water. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater()) {
            this.moveSpeed = 0.55f;
        } else {
            this.moveSpeed = 0.25f;
        }
    }

    /** {@code getWaterDragonHealth} (:216-218), read by the Girlfriend overlay (W11). */
    public int getWaterDragonHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:234-236). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:238-240). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.WATERDRAGON_HURT.get();
    }

    /** {@code getDeathSound} (:242-244). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WATERDRAGON_DEATH.get();
    }

    /** {@code getSoundVolume} (:246-248). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:250-252): fixed 1.0, no baby pitch. */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code dropFewItems} (:268-447): always a Water Dragon scale, an item frame and 9..14 raw cod, then one
     * {@code rand(20)} roll on the world random for the table. Every item is dropped by {@code dropItemRand} (:258-266,
     * spread 2 on {@code OreSpawnRand}, one block up).
     *
     * <p>PORT: {@code Items.fish} meta 0 is raw cod. The enchantment blocks are {@link ArthropodSupport}'s, identical to
     * :291-437 (sword with Sharpness twice, summed into one entry; boots Feather Falling 5-9); levels above the vanilla
     * maximum stay as rolled. The Ultimate tools are dropped plain, as the original created them; their pre-enchantments
     * follow R7 on the first inventory tick.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        final Level level = this.level();
        ItemStack is;
        ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.WATER_DRAGON_SCALE.get(), 1), 2);
        ArthropodSupport.dropItemRand(this, new ItemStack(Items.ITEM_FRAME, 1), 2);
        int var6;
        for (int var5 = 9 + level.random.nextInt(6), var6b = 0; var6b < var5; ++var6b) {
            ArthropodSupport.dropItemRand(this, new ItemStack(Items.COD, 1), 2);
        }
        var6 = level.random.nextInt(20);
        switch (var6) {
            case 0 -> ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.ULTIMATE_AXE.get(), 1), 2);
            case 1 -> ArthropodSupport.dropItemRand(this, new ItemStack(Items.IRON_INGOT, 1), 2);
            case 2 -> ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.ULTIMATE_PICKAXE.get(), 1), 2);
            case 3 -> {
                is = new ItemStack(Items.IRON_SWORD, 1);
                ArthropodSupport.enchantSword(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 4 -> {
                is = new ItemStack(Items.IRON_SHOVEL, 1);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 5 -> {
                is = new ItemStack(Items.IRON_PICKAXE, 1);
                ArthropodSupport.enchantPickaxe(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 6 -> {
                is = new ItemStack(Items.IRON_AXE, 1);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 7 -> {
                is = new ItemStack(Items.IRON_HOE, 1);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 8 -> {
                is = new ItemStack(Items.IRON_HELMET, 1);
                ArthropodSupport.enchantHelmet(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 9 -> {
                is = new ItemStack(Items.IRON_CHESTPLATE, 1);
                ArthropodSupport.enchantBody(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 10 -> {
                is = new ItemStack(Items.IRON_LEGGINGS, 1);
                ArthropodSupport.enchantBody(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 11 -> {
                is = new ItemStack(Items.IRON_BOOTS, 1);
                ArthropodSupport.enchantBoots(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 12 -> ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.ULTIMATE_SHOVEL.get(), 1), 2);
            case 13 -> ArthropodSupport.dropItemRand(this, new ItemStack(Items.IRON_BLOCK, 1), 2);
            default -> {
            }
        }
    }

    /** {@code dropFewItems} first, then the equipment roll (R10); drops do not depend on {@code recentlyHit}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code attackEntityAsMob} (:449-464): a fixed {@code WaterDragon_stats.attack} as mob damage (no attribute, no
     * enchantments, no vanilla knockback), then for a living target a push of 1.1 away from the dragon and 0.14 up,
     * doubled for a dead entity or a player.
     *
     * <p>PORT: {@code addVelocity} is {@link Entity#push(double, double, double)}; a server player only receives it
     * through {@code hurtMarked}, set again here so it never depends on the damage path ({@code SeaSupport.legacyKnockback},
     * W08). {@code isDead} is {@link Entity#isRemoved()}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), (float) MobStats.WaterDragon_stats().attack());
        if (var4) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                final double ks = 1.1;
                double inair = 0.14;
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

    /**
     * {@code attackEntityFrom} (:466-497): cactus ignored; hits whose entity is a Water Dragon, an Attack Squid or a
     * Water Ball return {@code false} (the Water Ball branch is dead - a thrown hit names the thrower); a hit only counts
     * while {@code hurt_timer <= 0}, which then blocks every further hit for 10 AI ticks; a mob attacker (not a player)
     * becomes the target with a 1.2 path, even while blocked.
     *
     * <p>PORT: {@code setTarget(e)} (the old-AI {@code entityToAttack}, :493) has no reader with AI tasks and is dropped;
     * {@code e instanceof EntityLiving} is {@link Mob}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.CACTUS)) {
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof WaterDragon) {
            return false;
        }
        if (e != null && e instanceof AttackSquid) {
            return false;
        }
        if (e != null && e instanceof WaterBall) {
            return false;
        }
        if (this.hurt_timer <= 0) {
            ret = super.hurt(par1DamageSource, par2);
            this.hurt_timer = 10;
        }
        if (e != null && e instanceof Mob) {
            if (e instanceof AttackSquid) {
                return false;
            }
            if (e instanceof WaterDragon) {
                return false;
            }
            this.setTarget((LivingEntity) e);
            this.getNavigation().moveTo(e, 1.2);
        }
        return ret;
    }

    /** {@code scan_it} (:499-580): one shell of the search cube, nearest water block wins. */
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
     * in 1.21.1. Waterlogged blocks were no water block in 1.7.10 and are none here (Hydrolisc, SeaSupport).
     */
    private boolean isWaterAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z)).is(Blocks.WATER);
    }

    /**
     * {@code updateAITasks} (:582-647), after the goal selectors: count {@code hurt_timer} down; on land, not sitting,
     * with 1 in 25 search water in shells 1, 2, 3, 4, 5, 7, 9, 11 around y-1 (height capped at 10) and walk one block
     * below it at 1.33, or with none lose 1 health with 1 in 50 and vanish without drops at 0; forget the attack target
     * 1 in 200; not on Peaceful with 1 in 5 look for a target, face it, within {@code (4 + width/2)^2} set
     * {@code attacking} 1 and bite with (1/4 or 1/5), else walk at 1.0 and fire the water cannon; in water below full
     * health with 1 in 100 heal 1.
     *
     * <p>PORT (R18): {@code heal(-1.0f)} is {@code setHealth(health - 1)} while alive, because NeoForge's
     * {@code LivingEntity.heal} returns for amounts at or below 0 (Hydrolisc, SeaSupport.legacyHeal). {@code setDead} is
     * {@link #discard()}. {@code (int)} coordinates are {@code Mth.floor} (R20). {@code "splash"} has no namespace and
     * was silent in 1.7.10 (R18); its pitch still draws from the world random. {@code isDead} is {@link #isRemoved()}.
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
        if (!this.isInWater() && this.level().random.nextInt(25) == 0 && !this.isSitting()) {
            this.closest = 99999;
            // :592-595: final boolean tx = false; tz = ty = tx = 0.
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
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
                if (this.level().random.nextInt(50) == 1) {
                    final float f = this.getHealth();
                    if (f > 0.0f) {
                        this.setHealth(f + -1.0f);
                    }
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }
        if (this.level().random.nextInt(200) == 0) {
            this.setTarget(null);
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && this.level().random.nextInt(5) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (4.0f + e.getBbWidth() / 2.0f) * (4.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.0);
                    this.watercanon(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.level().random.nextInt(100) == 1 && this.isInWater() && this.getHealth() < this.mygetMaxHealth()) {
            // playSound("splash", 1.5f, rand * 0.2f + 0.9f): silent (R18), the pitch draw stays.
            final float splashPitch = this.level().random.nextFloat() * 0.2f + 0.9f;
            this.heal(1.0f);
        }
    }

    /**
     * {@code watercanon} (:649-677): while a stream is loaded set {@code attacking} 2; with 1 in 15 (entity random) a
     * small fireball from 1.5 in front of the head and 1.75 up with the dragon as shooter; always one water ball from
     * the same point, aimed 0.25 above the target's feet plus an arc of {@code sqrt(dx^2 + dz^2) * 0.2}, speed 1.4,
     * spread 5, one shot of the stream used; an empty stream sets {@code attacking} 0; an empty stream reloads 8 shots
     * with 1 in 4.
     *
     * <p>The fireball places its z offset by the head yaw, the water ball by the body yaw (:656 vs. :661), as in the
     * original. The water ball uses the position constructor, so it has no thrower - as in 1.7.10.
     * {@code "random.bow"} is {@code entity.arrow.shoot}.
     *
     * <p>PORT: the 1.7.10 {@code EntityFireball} constructor added {@code nextGaussian() * 0.4} to each direction
     * component on the fireball's own random before normalising; 1.21.1's {@link SmallFireball} takes the direction as
     * is, so the spread is drawn here from the dragon's random (Kyuubi, W07; MothSupport, W08). The flight follows the
     * 1.21.1 hurting-projectile physics; damage and entity fire are the vanilla small fireball in both versions.
     */
    private void watercanon(final LivingEntity e) {
        final double yoff = 1.75;
        final double xzoff = 1.5;
        if (this.stream_count > 0) {
            this.setAttacking(2);
            if (this.random.nextInt(15) == 1) {
                final double ax = e.getX() - this.getX() + this.random.nextGaussian() * 0.4;
                final double ay = e.getY() + 0.75 - (this.getY() + yoff) + this.random.nextGaussian() * 0.4;
                final double az = e.getZ() - this.getZ() + this.random.nextGaussian() * 0.4;
                final SmallFireball var2 = legacySmallFireball(this.level(), this, new Vec3(ax, ay, az));
                var2.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + yoff,
                        this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYHeadRot())), this.getYRot(), this.getXRot());
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(var2);
            }
            final WaterBall var3 = new WaterBall(this.level(), e.getX() - this.getX(), e.getY() + 0.75 - (this.getY() + yoff), e.getZ() - this.getZ());
            var3.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + yoff,
                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())), this.getYHeadRot(), this.getXRot());
            final double var4 = e.getX() - var3.getX();
            final double var5 = e.getY() + 0.25 - var3.getY();
            final double var6 = e.getZ() - var3.getZ();
            final float var7 = (float) Math.sqrt(var4 * var4 + var6 * var6) * 0.2f;
            var3.setThrowableHeading(var4, var5 + var7, var6, 1.4f, 5.0f);
            ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(var3);
            --this.stream_count;
        } else {
            this.setAttacking(0);
        }
        if (this.stream_count <= 0 && this.random.nextInt(4) == 1) {
            this.stream_count = 8;
        }
    }

    /**
     * A vanilla small fireball whose block hit lights air without the 1.21.1 mobGriefing gate, as 1.7.10
     * {@code EntitySmallFireball.onImpact} did.
     *
     * <p>PORT: the same anonymous subclass as {@code Kyuubi.shootFireball} (W07) and {@code MothSupport.legacySmallFireball}
     * (W08), both private there; a fireball reloaded from disk is a plain vanilla one again. R21: whoever moves the
     * shared projectile helpers to {@code entity.projectile} merges the three copies.
     */
    private static SmallFireball legacySmallFireball(final Level level, final LivingEntity owner, final Vec3 movement) {
        return new SmallFireball(level, owner, movement) {
            @Override
            protected void onHitBlock(final BlockHitResult result) {
                super.onHitBlock(result);
                if (!this.level().isClientSide) {
                    final BlockPos blockpos = result.getBlockPos().relative(result.getDirection());
                    if (this.level().isEmptyBlock(blockpos)) {
                        this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
                    }
                }
            }
        };
    }

    /**
     * {@code isSuitableTarget} (:679-710), order kept: not Peaceful, alive, visible, not a Water Dragon; any monster, also
     * when tame; tame - nothing else; a player unless creative; otherwise {@code MyUtils.isAttackableNonMob}.
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
        if (par1EntityLiving instanceof WaterDragon) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (this.isTame()) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /**
     * {@code findSomethingToAttack} (:712-737): nothing with {@code PlayNicely} or for a baby; the box
     * {@code expand(14, 4, 14)} is collected and sorted first; a living attack target is kept; otherwise it is cleared
     * and the first suitable entity is taken.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        if (this.isBaby()) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(14.0, 4.0, 14.0));
        var5.sort(this.TargetSorter);
        final LivingEntity e = this.getTarget();
        if (e != null && e.isAlive()) {
            return e;
        }
        this.setTarget(null);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:739-741). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:743-745). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:747-772). A spawner of {@code water_dragon} in x/z -3..2, y 0..4 allows
     * it; otherwise Y at least 50, daytime, and no other Water Dragon within {@code expand(16, 5, 16)}.
     *
     * <p>PORT: the spawner is read through {@link HerbivoreSupport#spawnerNearby} (StinkBug, W06) - 1.7.10
     * {@code MobSpawnerBaseLogic} asked {@code getCanSpawnHere} as well, so the scan is the original test for spawner
     * spawns too. The box is the spawn box of the type at the position ({@code SeaMonster}, W08); before an entity exists
     * there is no {@code this} to exclude. {@code isDaytime} is {@code skylightSubtracted < 4}. At chunk generation R23
     * skips this predicate.
     */
    public static boolean checkWaterDragonSpawnRules(final EntityType<WaterDragon> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (HerbivoreSupport.spawnerNearby(level, pos.getX(), pos.getY(), pos.getZ(), SPAWNER_ID)) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (!HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        return level.getEntitiesOfClass(WaterDragon.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 5.0, 16.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkWaterDragonSpawnRules}; {@code Animal}'s grass and light rule is not asked. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code createChild} (:774-776). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /**
     * {@code spawnBabyAnimal} (:778-785): a new Water Dragon; with a tame parent the code re-sets the parent's own owner
     * (a no-op) and marks the baby tame - <b>without an owner</b>. R18: 1:1, the baby cannot be commanded by anyone.
     */
    public WaterDragon spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        final WaterDragon w = (WaterDragon) this.getType().create(this.level());
        if (w != null && this.isTame()) {
            this.setOwnerUUID(this.getOwnerUUID());
            w.setTame(true, false);
        }
        return w;
    }

    /** {@code isWheat} (:787-789): raw fish; dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && HerbivoreSupport.isRawFish(par1ItemStack);
    }

    /** {@code isBreedingItem} (:791-793). */
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
     * {@code onDeath}: no override in the original, and 1.7.10 {@code EntityTameable} did not override it either, so
     * the owner got no chat message when the dragon died.
     *
     * <p>PORT: R22 (owner death message of tame OreSpawn animals 1:1 off) - {@code TamableAnimal.die} in 1.21.1 sends
     * the death message to the owner. Java cannot skip one super level, so this is {@code LivingEntity.die} line by line
     * (NeoForge 21.1 sources) without the owner message, as in {@code Hydrolisc}.
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
