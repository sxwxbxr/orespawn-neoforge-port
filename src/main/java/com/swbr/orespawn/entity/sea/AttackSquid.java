package com.swbr.orespawn.entity.sea;

import com.swbr.orespawn.entity.waterdragon.WaterDragon;
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
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.projectile.InkSack;
import com.swbr.orespawn.entity.projectile.WaterBall;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.AttackSquid} (AttackSquid.java:18-669), id {@code attack_squid} ("Attack Squid",
 * OreSpawnMain.java:3527, tracking 32/1/false). A hostile land-walking squid that bites, spits ink sacks and water
 * balls, dries out away from water and sometimes calls The Kraken when a player kills it; also the ammunition of the
 * SquidZooka, then short-lived and hostile to everything (verhalten/entity-04.md).
 *
 * <p>Values: {@code AttackSquid_stats} (10/8/0), speed 0.25 re-set every tick (:33, :73), {@code experienceValue} 15
 * (:41), {@code fireResistance} 3 (:42). State: DataWatcher 20 {@code attacking} (a {@code Byte} in the bytecode,
 * {@code javap}), NBT {@code WasShot}. No water breathing - it drowns like the original.
 *
 * <p>Not carried over: {@code getAttackStrength} 2 (:93-96, no caller), {@code getDropItem} fish (:129-131, shadowed
 * by {@code dropFewItems}), {@code initCreature} (:339-340, overrode nothing), {@code attackEntityAsMob} (:346-348,
 * only {@code super}), {@code onLivingUpdate} (:89-91, only {@code super}).
 */
public class AttackSquid extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(AttackSquid.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    @Nullable
    private LivingEntity buddy;
    private float moveSpeed;
    private int wasshot;
    private final SeaSupport.WaterScan scan = new SeaSupport.WaterScan();

    /** Constructor (:29-50). {@code setSize(1.0f, 1.25f)} is the entity type's size (R9). */
    public AttackSquid(final EntityType<? extends AttackSquid> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.buddy = null;
        this.moveSpeed = 0.25f;
        this.wasshot = 0;
        // PORT: getNavigator().setAvoidsWater(false) (:40) is a water path malus of 0 (Whale, W06).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 15;
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.AttackSquid_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:52-57) at registration time (R3); armor from {@code getTotalArmorValue} (:81-83). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.AttackSquid_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** The tasks of the constructor (:45-49), same priorities. */
    @Override
    protected void registerGoals() {
        // PORT: EntityAISwimming is FloatGoal, as in W01-W07 (W07 "Offen": no 1.7.10 port in entity.ai).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(2, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(3, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06; Hammerhead, W07).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code entityInit} (:59-62). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 3} (:42). */
    @Override
    protected int getFireImmuneTicks() {
        return 3;
    }

    /** {@code canDespawn} (:64-66). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code setWasShot} (:68-70): 250 AI ticks of life for a SquidZooka shot. */
    public void setWasShot() {
        this.wasshot = 250;
    }

    /** {@code onUpdate} (:72-75), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:77-79). */
    public int mygetMaxHealth() {
        return MobStats.AttackSquid_stats().health();
    }

    /** {@code getTotalArmorValue} (:81-83) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.AttackSquid_stats().defense();
    }

    /** {@code getLivingSound} (:98-100). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:102-104). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SQUID_HURT.get();
    }

    /** {@code getDeathSound} (:106-108). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SQUID_DEATH.get();
    }

    /** {@code getSoundVolume} (:121-123). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:125-127). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code spawnCreature} (:110-119): create by name, place with a random yaw on the world random, add, play the
     * living sound. PORT: {@code EntityList.createEntityByName} is an {@link EntityType}; a missing type (the Kraken
     * before W10) is the original's {@code null} - nothing drawn, nothing spawned.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, @Nullable final EntityType<?> par1, final double par2,
                                       final double par4, final double par6) {
        Entity var8 = null;
        if (par1 != null) {
            var8 = par1.create(par0World);
        }
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            // PORT: the original cast to EntityLiving unchecked; every name it passed was one.
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }

    /**
     * {@code dropFewItems} (:143-337): one {@code rand(50)} roll on the world random for the gold table, then 1..3 fish.
     * Every item is dropped by {@code dropItemRand} (spread 2) except the enchanted golden apple (spread 3).
     *
     * <p>PORT: {@code Items.fish} meta 0 is raw cod, {@code Items.dye} meta 0 the ink sac, {@code golden_apple} meta 1
     * the enchanted golden apple. Enchantment levels above the vanilla maximum stay as rolled
     * ({@link ArthropodSupport}'s blocks, identical to :160-307).
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        final Level level = this.level();
        ItemStack is;
        int var4 = level.random.nextInt(50);
        switch (var4) {
            case 0 -> ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.GOLD_NUGGET), 2);
            case 1 -> ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.GOLD_INGOT), 2);
            case 2 -> ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.GOLDEN_CARROT), 2);
            case 3 -> {
                is = SeaSupport.stack(Items.GOLDEN_SWORD);
                ArthropodSupport.enchantSword(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 4 -> {
                is = SeaSupport.stack(Items.GOLDEN_SHOVEL);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 5 -> {
                is = SeaSupport.stack(Items.GOLDEN_PICKAXE);
                ArthropodSupport.enchantPickaxe(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 6 -> {
                is = SeaSupport.stack(Items.GOLDEN_AXE);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 7 -> {
                is = SeaSupport.stack(Items.GOLDEN_HOE);
                ArthropodSupport.enchantTool(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 8 -> {
                is = SeaSupport.stack(Items.GOLDEN_HELMET);
                ArthropodSupport.enchantHelmet(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 9 -> {
                is = SeaSupport.stack(Items.GOLDEN_CHESTPLATE);
                ArthropodSupport.enchantBody(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 10 -> {
                is = SeaSupport.stack(Items.GOLDEN_LEGGINGS);
                ArthropodSupport.enchantBody(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 11 -> {
                is = SeaSupport.stack(Items.GOLDEN_BOOTS);
                ArthropodSupport.enchantBoots(is, level);
                ArthropodSupport.dropItemRand(this, is, 2);
            }
            case 12 -> ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.GOLDEN_APPLE), 2);
            case 13 -> ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.GOLD_BLOCK), 2);
            case 14 -> ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.ENCHANTED_GOLDEN_APPLE), 3);
            case 15, 16, 17 -> ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.INK_SAC), 2);
            default -> {
            }
        }
        int i;
        for (i = 1 + level.random.nextInt(3), var4 = 0; var4 < i; ++var4) {
            ArthropodSupport.dropItemRand(this, SeaSupport.stack(Items.COD), 2);
        }
    }

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code interact} (:342-344): false. */
    @Override
    protected InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /** {@code fall} (:350-355): no fall damage while a SquidZooka shot. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        if (this.wasshot != 0) {
            return false;
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    /**
     * {@code attackEntityFrom} (:357-391). Ignored while dead and for hits whose entity is an Attack Squid, a Water Ball
     * or a Water Dragon; a mob attacker becomes the target with a 1.2 path. Then the hit, and when it killed the squid:
     * outside the Crystal dimension, on the server, by a player, with 1 in 15, {@code KrakenEnable} set and not shot -
     * 1..3 Krakens at Y 170, x/z ±3.
     *
     * <p>PORT: {@code setTarget(e)} (the old-AI {@code entityToAttack}, :380) has no reader with AI tasks and is
     * dropped. {@code isDead} is {@link #isRemoved()}; {@code getHealth() <= 0 || isDead} also reads
     * {@link #isRemoved()}. {@code e instanceof EntityLiving} is {@link Mob}.
     *
     * <p>PORT: {@code The Kraken} by registry id ({@code the_kraken}, registered since W10); while an id is unknown the
     * spawn is the original's {@code null} entity (the position draws happen, the yaw draw does not).
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.isRemoved()) {
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof AttackSquid) {
            return false;
        }
        if (e != null && e instanceof WaterBall) {
            return false;
        }
        if (e != null && e instanceof WaterDragon) {
            return false;
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
            ret = true;
        }
        ret = super.hurt(par1DamageSource, par2);
        if ((this.getHealth() <= 0.0f || this.isRemoved()) && this.level().dimension() != WorldProviderOreSpawn5.DIMENSION
                && !this.level().isClientSide && e != null && e instanceof Player && this.level().random.nextInt(15) == 1
                && OreSpawnConfig.MOBS.KrakenEnable.get() != 0 && this.wasshot == 0) {
            final EntityType<?> kraken = SeaSupport.laterWaveType("the_kraken");
            for (int j = 1 + this.level().random.nextInt(3), i = 0; i < j; ++i) {
                final double x = this.getX() + this.level().random.nextInt(4) - this.level().random.nextInt(4);
                final double z = this.getZ() + this.level().random.nextInt(4) - this.level().random.nextInt(4);
                spawnCreature(this.level(), kraken, x, 170.0, z);
            }
        }
        return ret;
    }

    /**
     * {@code updateAITasks} (:476-540), after the goal selectors. A shot squid counts down and vanishes at 0. On land
     * with 1 in 10 search water in shells up to 11 (vertical cap 5) and walk there at 1.33; with none, lose 1 health
     * with 1 in 25 and vanish without drops at 0. With 1 in 10 look for a target: within {@code distSq 9} set
     * {@code attacking} and bite with (1/4 or 1/5), otherwise walk at 1.2 and maybe shoot; without a target follow the
     * buddy at 1.0.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.wasshot > 0) {
            --this.wasshot;
            if (this.wasshot == 0) {
                this.discard();
                return;
            }
        }
        if (!this.isInWater() && this.level().random.nextInt(10) == 0) {
            this.scan.search(this, 5);
            if (this.scan.closest < 99999) {
                this.getNavigation().moveTo((double) this.scan.tx, (double) (this.scan.ty - 1), (double) this.scan.tz, 1.33);
            } else {
                if (this.level().random.nextInt(25) == 1) {
                    SeaSupport.legacyHeal(this, -1.0f);
                }
                if (this.getHealth() <= 0.0f) {
                    this.discard();
                    return;
                }
            }
        }
        if (this.level().random.nextInt(10) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                if (this.distanceToSqr(e) < 9.0) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.2);
                    this.watercanon(e);
                }
            } else {
                if (this.buddy != null) {
                    this.getNavigation().moveTo(this.buddy, 1.0);
                }
                this.setAttacking(0);
            }
        }
    }

    /**
     * {@code watercanon} (:542-569): with 1 in 5, and of that 1 in 3 (entity random) an ink sack, else a water ball,
     * 1.2 in front of the head and 1.0 up, aimed 0.25 above the feet plus an arc of {@code sqrt(dx^2 + dz^2) * 0.2},
     * speed 1.4, spread 5. The x offset uses the head yaw and the z offset the body yaw, as in the original.
     *
     * <p>The projectiles use the position constructor, so they have no thrower - as in 1.7.10, where
     * {@code EntityThrowable(World, x, y, z)} left it {@code null}. {@code "random.bow"} is {@code entity.arrow.shoot}.
     */
    private void watercanon(final LivingEntity e) {
        final double yoff = 1.0;
        final double xzoff = 1.2;
        if (this.level().random.nextInt(5) == 1) {
            if (this.random.nextInt(3) == 1) {
                final InkSack var2 = new InkSack(this.level(), e.getX() - this.getX(), e.getY() + 0.75 - (this.getY() + yoff), e.getZ() - this.getZ());
                var2.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + yoff,
                        this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())), this.getYHeadRot(), this.getXRot());
                final double var3 = e.getX() - this.getX();
                final double var4 = e.getY() + 0.25 - var2.getY();
                final double var5 = e.getZ() - this.getZ();
                final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                var2.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 5.0f);
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(var2);
            } else {
                final WaterBall var7 = new WaterBall(this.level(), e.getX() - this.getX(), e.getY() + 0.75 - (this.getY() + yoff), e.getZ() - this.getZ());
                var7.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + yoff,
                        this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())), this.getYHeadRot(), this.getXRot());
                final double var3 = e.getX() - this.getX();
                final double var4 = e.getY() + 0.25 - var7.getY();
                final double var5 = e.getZ() - this.getZ();
                final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                var7.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 5.0f);
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(var7);
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:571-622), order kept: alive, visible; a player unless creative; Girlfriend, Boyfriend,
     * zombies, villagers, spiders, cave spiders are targets; ghosts are not; lizards are; another Attack Squid becomes
     * the buddy with 1 in 5 (world random) and is never a target; anything else only for a shot squid.
     *
     * <p>PORT: {@code EntityZombie} is {@link Zombie}, which in 1.21.1 also covers Husk, Drowned and Zombie Villager
     * (1.7.10's zombie villager was an {@code EntityZombie} too); {@code EntityPigZombie} was an {@code EntityZombie}
     * subclass and {@code ZombifiedPiglin} still is.
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
        if (par1EntityLiving instanceof Girlfriend) {
            return true;
        }
        if (par1EntityLiving instanceof Boyfriend) {
            return true;
        }
        if (par1EntityLiving instanceof Zombie) {
            return true;
        }
        if (par1EntityLiving instanceof Villager) {
            return true;
        }
        if (par1EntityLiving instanceof Spider) {
            return true;
        }
        if (par1EntityLiving instanceof CaveSpider) {
            return true;
        }
        if (par1EntityLiving instanceof Ghost) {
            return false;
        }
        if (par1EntityLiving instanceof GhostSkelly) {
            return false;
        }
        if (par1EntityLiving instanceof Lizard) {
            return true;
        }
        if (par1EntityLiving instanceof AttackSquid) {
            if (this.level().random.nextInt(5) == 1) {
                this.buddy = par1EntityLiving;
            }
            return false;
        }
        return this.wasshot != 0;
    }

    /**
     * {@code findSomethingToAttack} (:624-645): nothing with {@code PlayNicely}; a living attack target is kept;
     * otherwise it is cleared and the first suitable entity in {@code expand(10, 4, 10)} is taken. The list is
     * collected and sorted first, as in the original.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 4.0, 10.0));
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

    /** {@code getAttacking} (:647-649). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:651-653). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /** {@code writeEntityToNBT} (:655-658). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("WasShot", this.wasshot);
    }

    /** {@code readEntityFromNBT} (:660-663). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.wasshot = par1NBTTagCompound.getInt("WasShot");
    }

    /**
     * {@code getCanSpawnHere} (:665-668) as the placement predicate: Y 50 or higher and daytime.
     *
     * <p>PORT: the ignored {@code super.getCanSpawnHere()} only drew {@code isValidLightLevel}'s numbers from the
     * entity's own random; with no entity yet those draws are not reproduced.
     */
    public static boolean checkAttackSquidSpawnRules(final EntityType<AttackSquid> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(level.getLevel());
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkAttackSquidSpawnRules}; {@code Monster}'s light weight is not asked. */
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
