package com.swbr.orespawn.entity.critter;

import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.Ignoreable;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
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
 * Port of {@code danger.orespawn.Cockateil} (Cockateil.java:13-270), id {@code bird} ("Bird",
 * OreSpawnMain.java:3491-3495, tracking 32/1/false). A small passive {@code EntityAnimal} in six colours
 * that flies from waypoint to waypoint without navigation (catalogue 5.3). Superclass of {@link RubyBird}.
 *
 * <p>Values: health {@link #mygetMaxHealth()} 2, speed 0.33, attack 1.0 (never used - {@code getAttackStrength}
 * :152-154 has no caller), hitbox 0.5 (entity type). {@code experienceValue = 2} (:39) is never read:
 * {@code EntityAnimal.getExperiencePoints} ({@code wf.e}) returns {@code 1 + rand(3)}, which is 1.21.1's
 * inherited {@code Animal.getBaseExperienceReward} (W04 lesson). No breeding ({@code createChild} null).
 *
 * <p>Not carried over: {@code getTexture()} (:52-76) - the texture choice lives in {@code RenderCockateil};
 * {@code initCreature} (:245, empty); {@code isAIEnabled} (always on in 1.21.1);
 * {@code doesEntityNotTriggerPressurePlate = false} and {@code canTriggerWalking = true} (:225-231) are the
 * 1.21.1 defaults.
 */
public class Cockateil extends Animal implements Ignoreable {

    /** DataWatcher 22: {@code birdtype} 0..5 (:81, :88-94). */
    private static final EntityDataAccessor<Integer> DATA_BIRD_TYPE =
            SynchedEntityData.defineId(Cockateil.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;
    /** The public cache {@code birdtype}; the synced value is {@link #getBirdType()}. */
    public int birdtype;
    /** Set by any player hit (:131-137), not saved - exactly like the original field. */
    private boolean killedByPlayer = false;
    private int stuck_count = 0;
    private int lastX = 0;
    private int lastZ = 0;
    /**
     * The field initializer must stay explicit: it runs after {@code Entity}'s constructor and therefore
     * after {@code RubyBird.defineSynchedData} called {@link #setFlyUp()}, resetting the value to 0 exactly
     * as the 1.7.10 initializer undid {@code RubyBird.entityInit} (verhalten/entity-11.md, "Befund setFlyUp").
     */
    private int flyup = 0;

    /**
     * Constructor (:29-43). {@code setSize(0.5f, 0.5f)} is the entity type's size.
     *
     * <p>PORT: the colour {@code rand(6)} of {@code entityInit} (:80) is rolled on the server only and stored
     * in the synced accessor, which starts at 0. 1.7.10 sent every watched value in the spawn packet; 1.21.1
     * sends only values that differ from the defined default, so a default rolled on each side would leave the
     * client with its own colour (R18 case 4).
     */
    public Cockateil(final EntityType<? extends Cockateil> type, final Level par1World) {
        super(type, par1World);
        if (!par1World.isClientSide) {
            this.setBirdType(this.birdtype = this.random.nextInt(6));
        }
        // getNavigator().setAvoidsWater(true) (:38): water is impassable for the path finder.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        // isImmuneToFire = false (:40) is the default; fireResistance = 2 (:41) is getFireImmuneTicks.
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:45-51). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33000001311302185)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    /** {@code entityInit} (:78-82): DataWatcher 22, see the constructor for the roll. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BIRD_TYPE, 0);
    }

    /** {@code fireResistance = 2} (:41): ticks in fire before the entity ignites. */
    @Override
    protected int getFireImmuneTicks() {
        return 2;
    }

    /** {@code getBirdType} (:88-90). */
    public int getBirdType() {
        return this.entityData.get(DATA_BIRD_TYPE);
    }

    /** {@code setBirdType} (:92-94). */
    public void setBirdType(final int par1) {
        this.entityData.set(DATA_BIRD_TYPE, par1);
    }

    /**
     * {@code canDespawn} (:84-86): {@code !isNoDespawnRequired()}. {@code EntityAnimal.canDespawn} was
     * {@code false} ({@code wf.v}), as 1.21.1's {@code Animal.removeWhenFarAway} is; persistence is checked
     * by {@code Mob.checkDespawn} before this is asked.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code getSoundVolume} (:96-98). */
    @Override
    protected float getSoundVolume() {
        return 0.55f;
    }

    /** {@code getSoundPitch} (:100-102). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:104-109): only by day and without rain. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (InsectSupport.isDaytime(this.level()) && !this.level().isRaining()) {
            return ModSounds.BIRDS.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:111-113). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code getDeathSound} (:115-117). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DUCK_HURT.get();
    }

    /** {@code canBePushed} (:119-121). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code mygetMaxHealth} (:123-125). */
    public int mygetMaxHealth() {
        return 2;
    }

    /**
     * {@code attackEntityFrom} (:131-137): any hit by a player marks the bird, even one the super call then
     * rejects; the ruby drop reads the mark.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Player) {
            this.killedByPlayer = true;
        }
        return super.hurt(par1DamageSource, par2);
    }

    /**
     * {@code onUpdate} (:139-150), both sides: after the move, damp the vertical motion - 0.7 below the
     * waypoint, 0.5 at or above it. The client never picks waypoints, so its target stays where it was made.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.currentFlightTarget == null) {
            // R20: Mth.floor for the (int) casts.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        } else if (this.getY() < this.currentFlightTarget.getY()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.7, 1.0));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.5, 1.0));
        }
    }

    /** {@code setFlyUp} (:156-158). */
    public void setFlyUp() {
        this.flyup = 2;
    }

    /** {@code fall} and {@code updateFallState} (:160-164), both empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code canSeeTarget} (:166-168): ray from 0.75 above the feet. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return CritterSupport.canSeeTarget(this, 0.75, pX, pY, pZ);
    }

    /**
     * {@code updateAITasks} (:170-223), after the goal and navigation machinery of the tick. In Islands the
     * waypoints reach two blocks higher; a bird that sits on the same column for more than 40 ticks, 1 in
     * 250, or one closer than {@code distSq 4.1} picks a new air waypoint it can see (35 tries).
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 35;
        int stayup = 0;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().dimension().equals(CritterSupport.DIMENSION_ISLANDS)) {
            stayup = 2;
        }
        if (this.lastX == Mth.floor(this.getX()) && this.lastZ == Mth.floor(this.getZ())) {
            ++this.stuck_count;
        } else {
            this.stuck_count = 0;
            this.lastX = Mth.floor(this.getX());
            this.lastZ = Mth.floor(this.getZ());
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.stuck_count > 40 || this.random.nextInt(250) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 4.1f) {
            // bid != Blocks.air: 1.7.10 had one air block; isAir() also covers cave and void air.
            BlockState bid = Blocks.STONE.defaultBlockState();
            this.stuck_count = 0;
            while (!bid.isAir() && keep_trying != 0) {
                zdir = this.random.nextInt(8) + 5 - this.flyup * 2;
                xdir = this.random.nextInt(8) + 5 - this.flyup * 2;
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir,
                        Mth.floor(this.getY()) + this.random.nextInt(9 + stayup) - 5 + this.flyup,
                        Mth.floor(this.getZ()) + zdir);
                bid = this.level().getBlockState(this.currentFlightTarget);
                if (bid.isAir() && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bid = Blocks.STONE.defaultBlockState();
                }
                --keep_trying;
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.3 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.3 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.3 - motion.x) * 0.25;
        final double motionY = motion.y + (Math.signum(var2) * 0.699999 - motion.y) * 0.200000001;
        final double motionZ = motion.z + (Math.signum(var3) * 0.3 - motion.z) * 0.25;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.8f;
        this.setYRot(this.getYRot() + var5 / 3.0f);
    }

    /**
     * {@code getCanSpawnHere} (:233-235) on the instance: by day, and in Islands or at Y 50 and above. No
     * super call, so no grass, light or collision test.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return InsectSupport.isDaytime(this.level())
                && (this.level().dimension().equals(CritterSupport.DIMENSION_ISLANDS) || this.getY() >= 50.0);
    }

    /** PORT: the collision and liquid half of 1.7.10's {@code getCanSpawnHere}, which the override replaced. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:233-235) as the placement predicate. */
    public static boolean checkBirdSpawnRules(final EntityType<Cockateil> type, final ServerLevelAccessor level,
                                              final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final ServerLevel world = level.getLevel();
        return InsectSupport.isDaytime(world)
                && (world.dimension().equals(CritterSupport.DIMENSION_ISLANDS) || pos.getY() >= 50.0);
    }

    /**
     * {@code getDropItem} (:237-243): a feather, or - for colour 5 hit by a player, 1 in 3 on the world
     * random - a ruby.
     */
    @Nullable
    protected Item getDropItem() {
        this.birdtype = this.getBirdType();
        if (this.birdtype == 5 && this.killedByPlayer && this.level().random.nextInt(3) == 1) {
            return ModItems.RUBY.get();
        }
        return Items.FEATHER;
    }

    /** {@code getDropItem} through vanilla {@code dropFewItems}; equipment afterwards (super). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        CritterSupport.dropFewItems(this, level, damageSource, this.getDropItem());
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** 1.7.10 {@code EntityAnimal.isBreedingItem}: wheat (feeding still sets love mode, nothing is born). */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code createChild} (:248-250). */
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

    /** {@code writeEntityToNBT} (:252-255). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("BirdType", this.getBirdType());
    }

    /** {@code readEntityFromNBT} (:257-260). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setBirdType(this.birdtype = par1NBTTagCompound.getInt("BirdType"));
    }
}
