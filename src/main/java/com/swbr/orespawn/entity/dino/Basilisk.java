package com.swbr.orespawn.entity.dino;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Basilisk} (Basilisk.java:18-474): {@code basilisk} (OreSpawnMain.java:3407, tracking
 * 64/1/false; {@code addSpawn} type ambient, :4537-4540). A giant serpent whose gaze slows everything it picks as a
 * target, whose bite poisons, and which ignores every hit for 30 AI ticks after one landed. {@code EntityMob} is
 * {@link Monster}.
 *
 * <p>Values: health, attack and armor from {@code Basilisk_stats} (200/24/15 by default), speed 0.4 re-set every tick
 * (:28, :59), hitbox 1.6 x 3.5 (:29, entity type), {@code experienceValue = 150} (:30, {@code xpReward}),
 * {@code fireResistance = 2000} (:31) and {@code isImmuneToFire} (:32, the entity type's {@code fireImmune()}).
 * DataWatcher 20 {@code attacking} (:51). {@code hurt_timer} is not saved, as in the original.
 *
 * <p>Not carried over: {@code isAIEnabled}; {@code interact} returning {@code false} (:306-308); the unused
 * {@code getDropItem} (:117-119). The constructor does not avoid water (no {@code setAvoidsWater}).
 */
public class Basilisk extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking} (:51, :430-436). */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Basilisk.class, EntityDataSerializers.INT);

    private final GenericTargetSorter TargetSorter;
    private int hurt_timer;
    private float moveSpeed;

    /** {@code Basilisk(World)} (:24-40). Goals on both sides, as the 1.7.10 constructor added them. */
    public Basilisk(final EntityType<? extends Basilisk> type, final Level par1World) {
        super(type, par1World);
        this.hurt_timer = 0;
        this.moveSpeed = 0.4f;
        // setSize(1.6f, 3.5f) (:29) is the entity type's size (R9).
        this.xpReward = 150;
        // fireResistance = 2000 (:31) is getFireImmuneTicks(); isImmuneToFire = true (:32) is EntityType.Builder.fireImmune().
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(this, 1.0, false) (:35) -> MoveThroughVillageGoal over village POIs, see Alosaurus.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 20, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) (:39) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard precedent, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        DinoSupport.applyStats(this, MobStats.Basilisk_stats());
    }

    /** {@code applyEntityAttributes} (:42-47) with the manifest defaults; the constructor applies the configured stats. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.4f)
                .add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.ARMOR, 15.0);
    }

    /** {@code entityInit} (:49-52). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 2000} (:31). */
    @Override
    protected int getFireImmuneTicks() {
        return 2000;
    }

    /** {@code canDespawn} (:54-56): {@code !isNoDespawnRequired()}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:58-61), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:63-65). */
    public int mygetMaxHealth() {
        return MobStats.Basilisk_stats().health();
    }

    /** {@code getTotalArmorValue} (:67-69) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Basilisk_stats().defense();
    }

    /**
     * {@code jump} (:75-78): {@code motionY += 0.25}, then the vanilla jump - which sets the vertical speed outright, in
     * 1.7.10 ({@code motionY = 0.42}) as in 1.21.1. The extra 0.25 is overwritten in both; kept for the mapping.
     */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
        super.jumpFromGround();
    }

    /** {@code onLivingUpdate} (:80-88), both sides: 1 in 200 heals one point. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isRemoved()) {
            return;
        }
        if (this.random.nextInt(200) == 0) {
            this.heal(1.0f);
        }
    }

    /** {@code getBasiliskHealth} (:90-92). */
    public int getBasiliskHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:94-99). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(2) == 0) {
            return ModSounds.BASILISK_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:101-103). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:105-107). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EMPERORSCORPION_DEATH.get();
    }

    /** {@code getSoundVolume} (:109-111). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:113-115). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code Mob.dropCustomDeathLoot}). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropItemRand(item, n)} (:121-129): the stack is returned so the caller can enchant it. */
    private ItemStack dropItemRand(final Item index, final int par1) {
        return DinoSupport.dropItemRand(this, new ItemStack(index, par1), 1.0);
    }

    /** {@code addEnchantment} on a drop: appends, i.e. sums with an existing entry (PreEnchant, R7/R18). */
    private void ench(final ItemStack is, final net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key,
                      final int lvl) {
        PreEnchant.add(is, this.level(), key, lvl);
    }

    /**
     * {@code dropFewItems} (:131-304): a basilisk scale and an item frame, 12..17 emeralds, 8..12 raw chicken, then
     * 3..7 rolls of {@code rand(15)} for emeralds, an emerald block or randomly enchanted emerald gear (world random).
     *
     * <p>PORT: the original spawned each gear item first and enchanted the stack afterwards. 1.21.1 pairs a new item
     * entity with nearby clients inside {@code addFreshEntity} and does not resend a stack whose components change later,
     * so clients would see an unenchanted item (R18 case 4). The stack is enchanted first and spawned afterwards; the
     * enchantment rolls and the spawn offsets use different generators ({@code level.random} and {@code OreSpawnRand}),
     * so no roll changes its source.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        ItemStack is = null;
        this.dropItemRand(ModItems.BASILISK_SCALE.get(), 1);
        this.dropItemRand(Items.ITEM_FRAME, 1);
        for (int i = 12 + this.level().random.nextInt(6), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.EMERALD, 1);
        }
        for (int i = 8 + this.level().random.nextInt(5), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(Items.CHICKEN, 1);
        }
        final RandomSource r = this.level().random;
        for (int i = 3 + r.nextInt(5), var4 = 0; var4 < i; ++var4) {
            final int var5 = r.nextInt(15);
            switch (var5) {
                case 1: {
                    is = this.dropItemRand(Items.EMERALD, 1);
                    break;
                }
                case 2: {
                    is = this.dropItemRand(Items.EMERALD_BLOCK, 1);
                    break;
                }
                case 3: {
                    is = new ItemStack(ModItems.EMERALD_SWORD.get(), 1);
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.SHARPNESS, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.BANE_OF_ARTHROPODS, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.KNOCKBACK, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.LOOTING, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.FIRE_ASPECT, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.SHARPNESS, 1 + r.nextInt(5));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 4: {
                    is = new ItemStack(ModItems.EMERALD_SHOVEL.get(), 1);
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.EFFICIENCY, 1 + r.nextInt(5));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 5: {
                    is = new ItemStack(ModItems.EMERALD_PICKAXE.get(), 1);
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.EFFICIENCY, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.FORTUNE, 1 + r.nextInt(5));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 6: {
                    is = new ItemStack(ModItems.EMERALD_AXE.get(), 1);
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.EFFICIENCY, 1 + r.nextInt(5));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 7: {
                    is = new ItemStack(ModItems.EMERALD_HOE.get(), 1);
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.EFFICIENCY, 1 + r.nextInt(5));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 8: {
                    is = new ItemStack(ModItems.EMERALD_HELMET.get(), 1);
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.BLAST_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.FIRE_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.PROJECTILE_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.RESPIRATION, 1 + r.nextInt(2));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.AQUA_AFFINITY, 1 + r.nextInt(5));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 9: {
                    is = new ItemStack(ModItems.EMERALD_CHEST.get(), 1);
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.BLAST_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.FIRE_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.PROJECTILE_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 10: {
                    is = new ItemStack(ModItems.EMERALD_LEGGINGS.get(), 1);
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.BLAST_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.FIRE_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.PROJECTILE_PROTECTION, 1 + r.nextInt(5));
                    }
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                case 11: {
                    is = new ItemStack(ModItems.EMERALD_BOOTS.get(), 1);
                    if (r.nextInt(6) == 1) {
                        this.ench(is, Enchantments.FEATHER_FALLING, 5 + r.nextInt(5));
                    }
                    if (r.nextInt(2) == 1) {
                        this.ench(is, Enchantments.UNBREAKING, 2 + r.nextInt(4));
                    }
                    DinoSupport.dropItemRand(this, is, 1.0);
                    break;
                }
                default:
                    break;
            }
        }
    }

    /**
     * {@code attackEntityAsMob} (:310-337): the {@code EntityMob} bite; on a hit, poison with 1 in 3 for 8/10/12/14 seconds
     * (peaceful, easy, normal, hard), then 1.5 away and 0.15 up (0.3 for players and removed targets).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                int var2 = 8;
                if (this.level().getDifficulty() == Difficulty.EASY) {
                    var2 = 10;
                }
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    var2 = 12;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    var2 = 14;
                }
                if (this.level().random.nextInt(3) == 0) {
                    ((LivingEntity) par1Entity).addEffect(new MobEffectInstance(MobEffects.POISON, var2 * 20, 0));
                }
                DinoSupport.legacyKnockback(this, par1Entity, 1.5, 0.15);
            }
            return true;
        }
        return false;
    }

    /**
     * {@code attackEntityFrom} (:339-345): while {@code hurt_timer} runs every hit is refused; otherwise the timer restarts
     * at 30 and the hit goes to super. The timer counts down in {@link #customServerAiStep}. Runs on the client too, like
     * the original, where the timer then never counts down - the client's return value is not used.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        if (this.hurt_timer > 0) {
            return false;
        }
        this.hurt_timer = 30;
        return super.hurt(par1DamageSource, par2);
    }

    /**
     * {@code updateAITasks} (:347-379): count {@code hurt_timer} down; with 1 in 5 (world random) find the nearest suitable
     * target, face it, bite within {@code (6 + width / 2)} with {@code rand(3) == 0 || rand(4) == 1}, else walk at 1.25 -
     * and in both cases slow it (Slowness amplifier 5, 100 ticks) however far away it is. Then 1 in 75 heals one point
     * below the configured maximum.
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
        if (this.level().random.nextInt(5) == 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (6.0f + e.getBbWidth() / 2.0f) * (6.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(3) == 0 || this.level().random.nextInt(4) == 1) {
                        this.doHurtTarget(e);
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.25);
                }
                // if (e instanceof EntityLivingBase) - always true for the list type.
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 5));
            } else {
                this.setAttacking(0);
            }
        }
        if (this.level().random.nextInt(75) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    /** {@code isSuitableTarget} (:381-411). */
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
        if (par1EntityLiving instanceof Basilisk) {
            return false;
        }
        // PORT: instanceof LeafMonster (:401) by registry id - W07 monster porter (DinoSupport.isOreSpawnType).
        if (DinoSupport.isOreSpawnType(par1EntityLiving, "leaf_monster")) {
            return false;
        }
        if (par1EntityLiving instanceof Player && DinoSupport.isCreativePlayer(par1EntityLiving)) {
            return false;
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:413-428): {@code PlayNicely} off, every living entity in {@code expand(24, 7, 24)}, sorted. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(24.0, 7.0, 24.0)));
        var5.sort(this.TargetSorter);
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:430-432). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:434-436). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: the entity-free part of {@code getCanSpawnHere} (:438-473) - spawner, night, air in the 3x3 column
     * x/z -1..1 at y+1..4, no other basilisk in {@code expand(20, 6, 20)}. No height limit in this class.
     *
     * <p>PORT: the light roll runs once, in {@link #checkSpawnRules}; the spawner block scan for "Basilisk" is
     * {@code MobSpawnType.SPAWNER} (see Alosaurus).
     */
    public static boolean checkBasiliskSpawnRules(final EntityType<Basilisk> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (!DinoSupport.airAround(level, pos.getX(), pos.getY(), pos.getZ(), -1, 2, -1, 2, 1, 5)) {
            return false;
        }
        return level.getEntitiesOfClass(Basilisk.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(20.0, 6.0, 20.0)).isEmpty();
    }

    /** {@code getCanSpawnHere} (:438-473) on the positioned instance, in the original order. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (reason == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, BlockPos.containing(this.getX(), this.getBoundingBox().minY, this.getZ()),
                this.random)) {
            return false;
        }
        if (HerbivoreSupport.isDaytime(this.level())) {
            return false;
        }
        if (!DinoSupport.airAround(level, DinoSupport.floor(this.getX()), DinoSupport.floor(this.getY()),
                DinoSupport.floor(this.getZ()), -1, 2, -1, 2, 1, 5)) {
            return false;
        }
        return level.getEntitiesOfClass(Basilisk.class, this.getBoundingBox().inflate(20.0, 6.0, 20.0),
                other -> other != this).isEmpty();
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
