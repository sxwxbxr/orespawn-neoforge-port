package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.portal.EntityAnt;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.Ignoreable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Dragonfly} (Dragonfly.java:12-214), id {@code dragonfly} ("Dragonfly",
 * OreSpawnMain.java:3435-3439, tracking 64/1/false). A flying {@code EntityAnimal} hunter of small flyers,
 * ants and (config) horses, waypoint flight without navigation (catalogue 5.3).
 *
 * <p>Values: health 10, speed 0.33, attack attribute 2.0 (the bite hard-codes 2.0 as well), hitbox 1.5 x 0.5
 * (entity type). {@code experienceValue = 5} (:23) is never read ({@code EntityAnimal.getExperiencePoints} is
 * {@code 1 + rand(3)}, inherited). No DataWatcher, no NBT, no breeding.
 *
 * <p>Not carried over: {@code entityInit} (:40-42, only the super call); {@code initCreature} (:171, empty);
 * {@code getNavigator().setAvoidsWater(false)} (:22, the default); {@code canTriggerWalking = true} and
 * {@code doesEntityNotTriggerPressurePlate = false} (:144-156, the defaults); {@code isAIEnabled}.
 */
public class Dragonfly extends Animal implements Ignoreable {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;
    private final GenericTargetSorter TargetSorter;

    /** Constructor (:16-27). {@code setSize(1.5f, 0.5f)} is the entity type's size. */
    public Dragonfly(final EntityType<? extends Dragonfly> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = new GenericTargetSorter(this);
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:29-35). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33000001311302185)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    /** {@code fireResistance = 5} (:25). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /** {@code canDespawn} (:44-46): {@code !isNoDespawnRequired()}; {@code Animal} would answer {@code false}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code getSoundVolume} (:48-50). */
    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    /** {@code getSoundPitch} (:52-54). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:56-58). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DRAGONFLY_LIVING.get();
    }

    /** {@code getHurtSound} (:60-62). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DRAGONFLY_HURT.get();
    }

    /** {@code getDeathSound} (:64-66). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DRAGONFLY_DEATH.get();
    }

    /** {@code canBePushed} (:68-70). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:72-73), empty. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:75-77). */
    public int mygetMaxHealth() {
        return 10;
    }

    /** {@code onUpdate} (:83-86), both sides: {@code motionY *= 0.6} after the move. */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
    }

    /** {@code attackEntityAsMob} (:88-91): 2.0 mob damage, no knockback, no enchantments. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 2.0f);
        return var4;
    }

    /** {@code canSeeTarget} (:93-95): ray from 0.25 above the feet. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return CritterSupport.canSeeTarget(this, 0.25, pX, pY, pZ);
    }

    /**
     * {@code updateAITasks} (:97-142): a new waypoint with 1 in 300 or closer than {@code distSq 2.1};
     * <em>otherwise</em> 1 in 12 (not on Peaceful) a prey search that moves the waypoint onto the prey and
     * bites within {@code distSq 6}. Steer 0.5/0.3 horizontally, 0.7/0.2 vertically, yaw a quarter.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            // R20: Mth.floor for the (int) casts.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.random.nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0; --keep_trying) {
                zdir = this.random.nextInt(5) + 5;
                xdir = this.random.nextInt(5) + 5;
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir,
                        Mth.floor(this.getY()) + this.random.nextInt(5) - 2,
                        Mth.floor(this.getZ()) + zdir);
                bid = this.level().getBlockState(this.currentFlightTarget);
                if (bid.isAir() && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bid = Blocks.STONE.defaultBlockState();
                }
            }
        } else if (this.random.nextInt(12) == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < 6.0) {
                    this.doHurtTarget(e);
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.5 - motion.x) * 0.30000000149011613;
        final double motionY = motion.y + (Math.signum(var2) * 0.699999988079071 - motion.y) * 0.20000000149011612;
        final double motionZ = motion.z + (Math.signum(var3) * 0.5 - motion.z) * 0.30000000149011613;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + var5 / 4.0f);
    }

    /** {@code fall} and {@code updateFallState} (:148-152), both empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code attackEntityFrom} (:158-165): after the damage, fly to where the attacker stands. The client
     * never made a waypoint (its hurt returns early in 1.21.1 anyway), so only the server steers.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final boolean ret = super.hurt(par1DamageSource, par2);
        final Entity e = par1DamageSource.getEntity();
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
        }
        return ret;
    }

    /** {@code getCanSpawnHere} (:167-169): Y 50 and above, by day. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0 && InsectSupport.isDaytime(this.level());
    }

    /** PORT: the collision and liquid half of 1.7.10's {@code getCanSpawnHere}, which the override replaced. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:167-169) as the placement predicate. */
    public static boolean checkDragonflySpawnRules(final EntityType<Dragonfly> type, final ServerLevelAccessor level,
                                                   final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(level.getLevel());
    }

    /**
     * {@code isSuitableTarget} (:174-176): not on Peaceful, alive, visible, and an ant, butterfly, bird,
     * mosquito, firefly - or a horse while {@code DragonflyHorseFriendly} is 0. No players.
     *
     * <p>PORT: 1.7.10 {@code EntityHorse} was one class for horse, donkey, mule, skeleton and zombie horse;
     * those five 1.21.1 classes are listed, the later llamas and camels are not (same reading as
     * {@code EntityButterfly}, W05).
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return this.level().getDifficulty() != Difficulty.PEACEFUL
                && par1EntityLiving != null
                && par1EntityLiving != this
                && par1EntityLiving.isAlive()
                && this.getSensing().hasLineOfSight(par1EntityLiving)
                && (par1EntityLiving instanceof EntityAnt
                || par1EntityLiving instanceof EntityButterfly
                || par1EntityLiving instanceof Cockateil
                || par1EntityLiving instanceof EntityMosquito
                || par1EntityLiving instanceof Firefly
                || (isHorse(par1EntityLiving) && OreSpawnConfig.TWEAKS.DragonflyHorseFriendly.get() == 0));
    }

    private static boolean isHorse(final LivingEntity e) {
        return e instanceof Horse || e instanceof Donkey || e instanceof Mule || e instanceof SkeletonHorse || e instanceof ZombieHorse;
    }

    /** {@code findSomethingToAttack} (:178-195): {@code PlayNicely} off, every living entity in {@code expand(10, 6, 10)}, nearest first. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 6.0, 10.0)));
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

    /** {@code getDropItem} (:197-209): {@code rand(6)} on the world random - gold nugget, uranium or titanium nugget, else nothing. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(6);
        if (i == 0) {
            return Items.GOLD_NUGGET;
        }
        if (i == 1) {
            return ModItems.URANIUM_NUGGET.get();
        }
        if (i == 2) {
            return ModItems.TITANIUM_NUGGET.get();
        }
        return null;
    }

    /** {@code getDropItem} through vanilla {@code dropFewItems}; equipment afterwards (super). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, this.getDropItem());
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat. */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code createChild} (:211-213). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
