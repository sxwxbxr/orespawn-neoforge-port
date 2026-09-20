package com.swbr.orespawn.entity.herbivore;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.portal.EntityAINearestAttackableTarget;
import com.swbr.orespawn.entity.portal.Termite;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.Peacock} (Peacock.java:16-235): {@code peacock}, a shy bird that fans its tail,
 * pecks Termites and now and then lays its own spawn egg ("Peacock", OreSpawnMain.java:3829, tracking 64/1/false).
 * Breeds with the Crystal Apple.
 *
 * <p>Values: health 15 (:108-110), speed 0.38 re-set every tick (:26, :67-68), attack attribute 4 (:56) but the
 * peck deals a fixed 6 (:145-148), hitbox 0.65 x 1.2 (:31), {@code fireResistance} 100 (:32).
 * {@code experienceValue = 8} (:33) is never read ({@code Animal.getBaseExperienceReward}, W04). No NBT of its own.
 *
 * <p>PORT (R18 case 4): the fan timer {@code blinker} (:63-82) ran independently on server and every client and was
 * never synchronised, so two players saw the tail open at different times. The timer runs on the server only and
 * the value is one {@code SynchedEntityData} int that {@code PeacockModel} reads (catalogue 5.11,
 * design-entities-04). It is not saved, like the original field.
 */
public class Peacock extends Animal {

    /** {@code blinker}, synchronised (class Javadoc). */
    private static final EntityDataAccessor<Integer> DATA_BLINKER = SynchedEntityData.defineId(Peacock.class, EntityDataSerializers.INT);

    private float moveSpeed;
    int my_blink;
    int blinkcount;
    int blinker;
    private GenericTargetSorter TargetSorter;

    /** {@code Peacock(World)} (:24-49). */
    public Peacock(final EntityType<? extends Peacock> type, final Level par1World) {
        super(type, par1World);
        this.moveSpeed = 0.38f;
        this.my_blink = 0;
        this.blinkcount = 0;
        this.blinker = 0;
        this.TargetSorter = null;
        // setSize(0.65f, 1.2f) (:31) is the entity type's size (R9); fireResistance = 100 (:32) is
        // getFireImmuneTicks(); experienceValue = 8 (:33) is dead.
        this.my_blink = 20 + this.random.nextInt(50);
        this.blinkcount = 0;
        this.blinker = 0;
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: getNavigator().setAvoidsWater(true) (:38) - a water path malus of -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(2, new EntityAIAvoidEntity(this, Monster.class, 8.0f, 1.0, 1.399999976158142));
        this.goalSelector.addGoal(3, new EntityAIAvoidEntity(this, Player.class, 12.0f, 1.2000000476837158, 1.600000023841858));
        this.goalSelector.addGoal(4, LegacyPanic.legacyPanic(this, 1.5));
        this.goalSelector.addGoal(5, new MyEntityAIWander(this, 1.0f));
        this.goalSelector.addGoal(6, new EntityAILookIdle(this));
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            // Only state: no melee goal reads the target (verhalten/entity-10.md).
            this.targetSelector.addGoal(1, new EntityAINearestAttackableTarget<>(this, Termite.class, 6, true));
        }
    }

    /** {@code applyEntityAttributes} (:51-57). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.38f)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    /** {@code entityInit} (:59-61) plus the synchronised {@code blinker}. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BLINKER, 0);
    }

    /** {@code fireResistance = 100} (:32). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code getBlink} (:63-65), read by {@code PeacockModel}. */
    public int getBlink() {
        return this.entityData.get(DATA_BLINKER);
    }

    /** {@code onUpdate} (:67-82): speed, then the fan timer (server only, class Javadoc). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (!this.level().isClientSide) {
            ++this.blinkcount;
            if (this.blinkcount > this.my_blink) {
                this.blinkcount = 0;
                if (this.blinker > 0) {
                    this.blinker = 0;
                    this.my_blink = 50 + this.level().random.nextInt(300);
                } else {
                    this.blinker = 1;
                    this.my_blink = 25 + this.level().random.nextInt(100);
                }
                this.entityData.set(DATA_BLINKER, this.blinker);
            }
        }
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:84-98) - air in x/z -1..0 at y+1..y+2, day time 0..12000,
     * Y 50..100 and at most 2 peacocks in {@code expand(16, 10, 16)} of the spawn box (the new mob was not in the
     * world yet in 1.7.10 either). PORT: {@code bid != Blocks.air} is {@code !isAir()}, which also accepts cave and
     * void air - 1.7.10 had no other air; the entity list comes from the accessor (empty in a {@code WorldGenRegion}).
     */
    public static boolean checkPeacockSpawnRules(final EntityType<Peacock> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return canSpawnHere(level, level.getLevel(), pos.getX(), pos.getY(), pos.getZ(),
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
    }

    /** {@code getCanSpawnHere} (:84-98) on the positioned instance; {@code (int)} casts are {@code Mth.floor} (R20). */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return canSpawnHere(level, this.level(), Mth.floor(this.getX()), this.getY(), Mth.floor(this.getZ()), this.getBoundingBox());
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    private static boolean canSpawnHere(final LevelAccessor level, final Level world, final int posX, final double posY,
                                        final int posZ, final AABB box) {
        for (int k = -1; k < 1; ++k) {
            for (int j = -1; j < 1; ++j) {
                for (int i = 1; i < 3; ++i) {
                    if (!level.getBlockState(new BlockPos(posX + j, Mth.floor(posY) + i, posZ + k)).isAir()) {
                        return false;
                    }
                }
            }
        }
        long t = world.getDayTime();
        t %= 24000L;
        return t <= 12000L && posY >= 50.0 && posY <= 100.0
                && level.getEntitiesOfClass(Peacock.class, box.inflate(16.0, 10.0, 16.0)).size() <= 2;
    }

    // isAIEnabled (:100-102): every 1.21.1 mob runs goals. canBreatheUnderwater (:104-106) false is the default.

    /** {@code mygetMaxHealth} (:108-110). */
    public int mygetMaxHealth() {
        return 15;
    }

    /** {@code getLivingSound} (:112-117): one call in eight from the world's random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(8) != 1) {
            return null;
        }
        return ModSounds.PEACOCKLIVE.get();
    }

    /** {@code getHurtSound} (:119-121). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.PEACOCKHIT.get();
    }

    /** {@code getDeathSound} (:123-125). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PEACOCKDEAD.get();
    }

    /** {@code getSoundVolume} (:127-129). */
    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** {@code dropFewItems} first, then the equipment roll; {@code getDropItem} (:131-133) is unused. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:135-143): one raw peacock, another 1 in 3, a feather 1 in 2 (world's random). */
    protected void dropFewItems(final boolean par1, final int par2) {
        this.spawnAtLocation(new ItemStack(ModItems.RAW_PEACOCK.get(), 1));
        if (this.level().random.nextInt(3) == 1) {
            this.spawnAtLocation(new ItemStack(ModItems.RAW_PEACOCK.get(), 1));
        }
        if (this.level().random.nextInt(2) == 1) {
            this.spawnAtLocation(new ItemStack(ModItems.PEACOCK_FEATHER.get(), 1));
        }
    }

    /** {@code attackEntityAsMob} (:145-148): a fixed 6 as mob damage, no knockback, no enchantments. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 6.0f);
        return var4;
    }

    /** {@code LayAnEgg} (:150-158): the shared {@code OreSpawnRand} for x/z, one block up. */
    private ItemStack LayAnEgg(final Item index, final int par1) {
        ItemEntity var3 = null;
        final ItemStack is = new ItemStack(index, par1);
        var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2), is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /**
     * {@code updateAITasks} (:160-182): forget the revenge target 1 in 200, the whole AI step, then - not on
     * Peaceful - 1 in 10 look for a Termite and peck it within squared distance 4 or walk to it at 1.2, and 1 in 5000
     * lay 1..3 Peacock spawn eggs.
     *
     * <p>PORT: {@code Mob.serverAiStep} is final in 1.21.1; its hook is {@code customServerAiStep}, which runs after
     * the goal selectors and the navigation, before the move, look and jump controls. The revenge reset that stood
     * before {@code super.updateAITasks()} therefore lands after this tick's goal selection, and the peck and egg
     * code runs before the controls instead of after them.
     */
    @Override
    protected void customServerAiStep() {
        if (this.level().random.nextInt(200) == 1) {
            this.setLastHurtByMob(null);
        }
        super.customServerAiStep();
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        if (this.level().random.nextInt(10) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                if (this.distanceToSqr(e) < 4.0) {
                    this.doHurtTarget(e);
                } else {
                    this.getNavigation().moveTo(e, 1.2);
                }
            }
        }
        if (this.level().random.nextInt(5000) == 1) {
            this.LayAnEgg(ModItems.EGG_PEACOCK.get(), 1 + this.level().random.nextInt(3));
        }
    }

    /** {@code isSuitableTarget} (:184-186). */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return this.level().getDifficulty() != Difficulty.PEACEFUL && par1EntityLiving != null && par1EntityLiving != this
                && par1EntityLiving.isAlive() && this.getSensing().hasLineOfSight(par1EntityLiving)
                && par1EntityLiving instanceof Termite;
    }

    /** {@code findSomethingToAttack} (:188-205): {@code expand(10, 2, 10)}, sorted by {@link GenericTargetSorter}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 2.0, 10.0)));
        var5.sort(this.TargetSorter);
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8 = null;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code canDespawn} (:207-213): babies become persistent, adults despawn unless persistent. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        if (this.isBaby()) {
            this.setPersistenceRequired();
            return false;
        }
        return !this.isPersistenceRequired();
    }

    /** {@code createChild} (:215-217). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return this.spawnBabyAnimal(otherParent);
    }

    /** {@code spawnBabyAnimal} (:219-221). */
    public Peacock spawnBabyAnimal(final AgeableMob par1EntityAgeable) {
        return ModEntities.PEACOCK.get().create(this.level());
    }

    /** {@code isWheat} (:223-225): dead in 1.7.10, kept for the mapping. */
    public boolean isWheat(final ItemStack par1ItemStack) {
        return par1ItemStack != null && par1ItemStack.is(Items.APPLE);
    }

    /** {@code isBreedingItem} (:227-229). */
    @Override
    public boolean isFood(final ItemStack par1ItemStack) {
        return par1ItemStack.is(ModItems.CRYSTAL_APPLE.get());
    }

    /** 1.7.10 {@code EntityAnimal.interact}: breeding only, no baby feeding. */
    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return HerbivoreSupport.legacyAnimalInteract(this, player, hand);
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
