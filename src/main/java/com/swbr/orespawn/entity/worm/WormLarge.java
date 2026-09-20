package com.swbr.orespawn.entity.worm;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.WormLarge} (WormLarge.java:16-379): {@code large_worm} ("Large Worm",
 * OreSpawnMain.java:3743, tracking 64/1/false), the boss of the worm family without a boss bar. On its first server
 * tick it brings 20 Small and 20 Medium Worms; it stays buried with {@code noClip} while any Medium Worm lives within
 * 8 blocks, then surfaces under the nearest player, walks after him and takes helmet or chestplate and the held item.
 *
 * <p>Values: {@code WormLarge_stats} (90/18/14), speed 0.2, hitbox 1.55 x 2.5, {@code experienceValue = 2050}
 * ({@code EntityMob} reads it), no DataWatcher entry, NBT {@code wormsSpawned}, never despawns. Drops (R10) are loose
 * item entities. The raw {@code posY} handling is the one described in {@link WormSmall}.
 *
 * <p><b>AI only above ground.</b> {@code updateAITasks} called {@code super.updateAITasks()} only without
 * {@code noClip} (:174-176), which skipped goals, navigation and the move, look and jump helpers together.
 * {@code Mob.serverAiStep} is final in 1.21.1, so each of those is gated instead: goals through
 * {@link WhileNotNoClip} (a running goal is frozen, not stopped, and no goal starts), navigation through
 * {@link #createNavigation}, the three controls through wrappers set in the constructor. Without that the path set
 * by {@code tryMoveToXYZ} while buried would drive the worm sideways through the ground.
 * PORT: the sensing cache is still cleared every tick; nothing reads it while the goals are frozen.
 */
public class WormLarge extends Monster implements LegacyArmor, NoStepTrigger {

    /** 1.7.10's spawner entity name {@code "Large Worm"} is the registry id (R2). */
    private static final String LARGE_WORM_ID = OreSpawn.MOD_ID + ":large_worm";

    private int wormsSpawned;
    /** A raw {@code posY +=} is waiting to be dropped by the next {@link #move} (see {@link WormSmall}). */
    private boolean legacyPosYPending;

    /** {@code WormLarge(World)} (:20-32). */
    public WormLarge(final EntityType<? extends WormLarge> type, final Level par1World) {
        super(type, par1World);
        this.applyEntityAttributes();
        this.setHealth(this.getMaxHealth());
        this.moveControl = new NoClipGatedMoveControl(this);
        this.lookControl = new NoClipGatedLookControl(this);
        this.jumpControl = new NoClipGatedJumpControl(this);
        this.wormsSpawned = 0;
        // setSize(1.55f, 2.5f) (:23) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:24) - water malus -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 2050;
        this.noPhysics = true;
        // Goals in the constructor on both sides, like the 1.7.10 constructor (W04/W05 precedent).
        // EntityAISwimming -> FloatGoal, as in every earlier wave.
        this.goalSelector.addGoal(0, new WhileNotNoClip(this, new FloatGoal(this)));
        // PORT: EntityAIMoveThroughVillage(1.0, false) walked the door list of a VillageCollection village, which
        // 1.21.1 does not have; the vanilla goal walks village POIs. onlyAtNight false, 4 blocks like the 1.7.10
        // "close enough" (squared distance 16), doors never opened while pathing (1.7.10 setBreakDoors(false)).
        this.goalSelector.addGoal(1, new WhileNotNoClip(this, new MoveThroughVillageGoal(this, 1.0, false, 4, () -> false)));
        this.goalSelector.addGoal(2, new WhileNotNoClip(this, new MyEntityAIWanderALot(this, 16, 1.0)));
        this.goalSelector.addGoal(3, new WhileNotNoClip(this, new EntityAIWatchClosest(this, Player.class, 8.0f)));
        this.goalSelector.addGoal(4, new WhileNotNoClip(this, new EntityAILookIdle(this)));
    }

    /** Attribute set with the config defaults; the constructor sets the bases from the loaded config (:34-39). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0)
                .add(Attributes.MOVEMENT_SPEED, 0.20000000298023224)
                .add(Attributes.ATTACK_DAMAGE, 18.0);
    }

    /** {@code applyEntityAttributes} (:34-39). */
    private void applyEntityAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.WormLarge_stats().attack());
    }

    /** The navigator of {@code super.updateAITasks()}, frozen while clipping (class Javadoc). */
    @Override
    protected PathNavigation createNavigation(final Level level) {
        return new NoClipGatedNavigation(this, level);
    }

    /** {@code canDespawn} (:45-47). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getSoundVolume} (:49-51). */
    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    /** {@code getSoundPitch} (:53-55). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:57-59). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:61-63). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.BIG_SPLAT.get();
    }

    /** {@code getDeathSound} (:65-67). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code canBePushed} (:69-71). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:73-74): empty. */
    @Override
    protected void doPush(final Entity entity) {
    }

    /** {@code collideWithNearbyEntities} (:76-77): empty. */
    @Override
    protected void pushEntities() {
    }

    /** Inherited {@code applyEntityCollision}; see {@link WormSupport#legacyPush}. */
    @Override
    public void push(final Entity entity) {
        WormSupport.legacyPush(this, entity);
    }

    /** {@code mygetMaxHealth} (:79-81). */
    public int mygetMaxHealth() {
        return MobStats.WormLarge_stats().health();
    }

    /** {@code getTotalArmorValue} (:83-85). */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.WormLarge_stats().defense();
    }

    /** {@code pointAtEntity} (:91-99). */
    public void pointAtEntity(final LivingEntity e) {
        final double d1 = e.getX() - this.getX();
        final double d2 = e.getZ() - this.getZ();
        final float d3 = (float) Math.atan2(d2, d1);
        final float n;
        final float f2 = n = (float) (d3 * 180.0 / 3.141592653589793) - 90.0f;
        this.setYHeadRot(n);
        this.setYRot(n);
    }

    /**
     * {@code onLivingUpdate} (:101-157): surface under a player once no Medium Worm is within 8 (physics back on as
     * soon as the feet are in air), otherwise dig down and die in anything but grass, dirt or stone; the brood on the
     * first server tick.
     *
     * <p>PORT (R18 case 4): the digging part ran on both sides in the original (only the brood checked
     * {@code isRemote}); as for {@link WormSmall}, it runs on the server only. {@code (int)} coordinates are
     * {@code Mth.floor} (R20).
     */
    @Override
    public void aiStep() {
        Player target = null;
        WormMedium worms = null;
        Entity newent = null;
        super.aiStep();
        if (this.level().isClientSide) {
            this.clientNoClipState();
            return;
        }
        worms = WormSupport.findNearestEntityWithinAABB(this, WormMedium.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        if (worms == null) {
            target = WormSupport.findNearestEntityWithinAABB(this, Player.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        }
        if ((worms == null && target != null) || OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            if (target != null) {
                this.pointAtEntity(target);
            }
            BlockState bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
            if (WormSupport.isTallGrass(bid)) {
                bid = Blocks.AIR.defaultBlockState();
            }
            if (!bid.isAir()) {
                this.addMotionY(0.25);
                this.legacyAddPosY(0.10000000149011612);
            } else {
                this.noPhysics = false;
            }
        } else {
            this.noPhysics = true;
            BlockState bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY() + 3.5), Mth.floor(this.getZ()));
            if (WormSupport.isTallGrass(bid)) {
                bid = Blocks.AIR.defaultBlockState();
            }
            if (!bid.isAir()) {
                this.addMotionY(0.10000000149011612);
                this.legacyAddPosY(0.05000000074505806);
                if (!WormSupport.isGrassDirtOrStone(bid)) {
                    this.discard();
                }
            }
        }
        if (this.noPhysics) {
            final Vec3 motion = this.getDeltaMovement();
            // motionY -= 0.01; motionX = 0.0; motionZ = 0.0; moveForward = 0.0f (:140-145).
            this.setDeltaMovement(0.0, motion.y - 0.01, 0.0);
            this.zza = 0.0f;
        }
        if (this.wormsSpawned != 0) {
            return;
        }
        this.wormsSpawned = 1;
        for (int i = 0; i < 20; ++i) {
            newent = spawnCreature(this.level(), ModEntities.SMALL_WORM.get(),
                    this.getX() + this.level().random.nextInt(6) - this.level().random.nextInt(6), this.getY(),
                    this.getZ() + this.level().random.nextInt(6) - this.level().random.nextInt(6));
            newent = spawnCreature(this.level(), ModEntities.MEDIUM_WORM.get(),
                    this.getX() + this.level().random.nextInt(5) - this.level().random.nextInt(5), this.getY(),
                    this.getZ() + this.level().random.nextInt(5) - this.level().random.nextInt(5));
        }
    }

    /**
     * The client half of {@code onLivingUpdate} (:106-130): only the {@code noClip} decision, which the overlay reads
     * ({@code GirlfriendOverlayGui} :200-206 shows the "Worm" bar only without {@code noClip}).
     *
     * <p>PORT (R18 case 4): the original ran the whole branch on the client, including the raw {@code posY} and motion
     * nudges; those stay server-side (the client position is the server's, lerped), so the client repeats only the
     * conditions that set or clear {@code noClip}. Without this the flag stays at the constructor's {@code true} on the
     * client and the "Worm" bar never shows.
     */
    private void clientNoClipState() {
        WormMedium worms = WormSupport.findNearestEntityWithinAABB(this, WormMedium.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        Player target = null;
        if (worms == null) {
            target = WormSupport.findNearestEntityWithinAABB(this, Player.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        }
        if ((worms == null && target != null) || OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            BlockState bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
            if (WormSupport.isTallGrass(bid)) {
                bid = Blocks.AIR.defaultBlockState();
            }
            if (bid.isAir()) {
                this.noPhysics = false;
            }
        } else {
            this.noPhysics = true;
        }
    }

    /** {@code onUpdate} (:159-165). */
    @Override
    public void tick() {
        if (this.isPersistenceRequired()) {
            this.noPhysics = false;
        }
        super.tick();
        final Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.85, motion.z);
    }

    /** Drops a pending raw {@code posY} offset the way 1.7.10's {@code moveEntity} did (see {@link WormSmall}). */
    @Override
    public void move(final MoverType type, final Vec3 pos) {
        if (this.legacyPosYPending) {
            this.legacyPosYPending = false;
            this.setPosRaw(this.getX(), this.getBoundingBox().minY, this.getZ());
        }
        super.move(type, pos);
    }

    /** {@code posY += d} without touching the bounding box. */
    private void legacyAddPosY(final double d) {
        this.setPosRaw(this.getX(), this.getY() + d, this.getZ());
        this.legacyPosYPending = true;
    }

    /** {@code motionY += d}. */
    private void addMotionY(final double d) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, d, 0.0));
    }

    /** {@code worldObj.getBlock(x, y, z)}. */
    private BlockState blockAt(final int x, final int y, final int z) {
        return this.level().getBlockState(new BlockPos(x, y, z));
    }

    /**
     * {@code updateAITasks} (:167-243) after the gated {@code super.updateAITasks()} (class Javadoc): nothing while a
     * Medium Worm is within 8; the nearest non-creative player within {@code expand(8, 6, 8)} is turned to and walked
     * at; one tick in ten within 3 blocks it bites, then one in four takes the helmet (or the chestplate without a
     * helmet) and, rolled apart, one in four the held item.
     *
     * <p>PORT: runs after the navigation tick and before the controls, as in {@link WormSmall#customServerAiStep}.
     */
    @Override
    protected void customServerAiStep() {
        Player target = null;
        WormMedium worms = null;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return;
        }
        worms = WormSupport.findNearestEntityWithinAABB(this, WormMedium.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        if (worms != null) {
            return;
        }
        target = WormSupport.findNearestEntityWithinAABB(this, Player.class, this.getBoundingBox().inflate(8.0, 6.0, 8.0));
        if (target != null && target.getAbilities().instabuild) {
            target = null;
        }
        if (target != null) {
            this.pointAtEntity(target);
            this.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
            if (this.level().random.nextInt(10) == 1 && this.distanceTo(target) < 3.0) {
                super.doHurtTarget(target);
                if (this.level().random.nextInt(4) == 1) {
                    // Player slots 4 and 3: armorInventory[3] helmet, armorInventory[2] chestplate.
                    ItemStack boots = target.getItemBySlot(EquipmentSlot.HEAD);
                    if (!boots.isEmpty()) {
                        WormSupport.stealAndThrow(this, target, EquipmentSlot.HEAD, boots, 10);
                    } else {
                        boots = target.getItemBySlot(EquipmentSlot.CHEST);
                        if (!boots.isEmpty()) {
                            WormSupport.stealAndThrow(this, target, EquipmentSlot.CHEST, boots, 10);
                        }
                    }
                }
                if (this.level().random.nextInt(4) == 1) {
                    // Player slot 0: inventory.getCurrentItem().
                    final ItemStack boots = target.getItemBySlot(EquipmentSlot.MAINHAND);
                    if (!boots.isEmpty()) {
                        WormSupport.stealAndThrow(this, target, EquipmentSlot.MAINHAND, boots, 10);
                    }
                }
            }
        }
    }

    /** {@code canTriggerWalking} (:245-247): no step sounds; the block side is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} (:249-253): only above ground. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        if (!this.noPhysics) {
            return super.causeFallDamage(fallDistance, multiplier, source);
        }
        return false;
    }

    /** {@code updateFallState} (:255-259): only above ground. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
        if (!this.noPhysics) {
            super.checkFallDamage(y, onGround, state, pos);
        }
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:261-263). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * Spawn predicate for {@code RegisterSpawnPlacementsEvent}: {@code getCanSpawnHere} (:265-311) before the entity
     * exists, on the type's box at the block centre where the mob will stand. It cannot remember a spawner hit; the
     * positioned instance asks again through {@link #checkSpawnRules} and sets {@code wormsSpawned} there.
     */
    public static boolean checkWormLargeSpawnRules(final EntityType<WormLarge> type, final ServerLevelAccessor level,
                                                   final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final double x = pos.getX() + 0.5;
        final double z = pos.getZ() + 0.5;
        return getCanSpawnHere(level, x, pos.getY(), z, type.getSpawnAABB(x, pos.getY(), z), null);
    }

    /** {@code getCanSpawnHere} (:265-311) on the positioned instance, asked by natural spawning and spawners. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return getCanSpawnHere(level, this.getX(), this.getY(), this.getZ(), this.getBoundingBox(), this);
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code getCanSpawnHere} (:265-311): a Large Worm spawner in x/z {@code -3..2}, y {@code 0..4} allows the spawn
     * and suppresses the brood; otherwise Y at least 50, no other Large Worm in {@code expand(32, 8, 32)}, a solid
     * 13 x 13 floor from y-2 down to y-8 and a clear 13 x 13 sky from y+2 up to y+8.
     *
     * <p>The spawner scan is {@link HerbivoreSupport#spawnerNearby}, the same box and the same loop order. The entity
     * list comes from the accessor, which during chunk generation lists no entities (Baryonyx precedent). All block
     * reads stay within one chunk of a spawn position inside the generating chunk, which the SPAWN step may read.
     */
    private static boolean getCanSpawnHere(final LevelAccessor level, final double posX, final double posY, final double posZ,
                                           final AABB boundingBox, @Nullable final WormLarge self) {
        final int x = Mth.floor(posX);
        final int y = Mth.floor(posY);
        final int z = Mth.floor(posZ);
        if (HerbivoreSupport.spawnerNearby(level, x, y, z, LARGE_WORM_ID)) {
            if (self != null) {
                self.wormsSpawned = 1;
            }
            return true;
        }
        if (posY < 50.0) {
            return false;
        }
        for (final WormLarge target : level.getEntitiesOfClass(WormLarge.class, boundingBox.inflate(32.0, 8.0, 32.0))) {
            if (target != self) {
                return false;
            }
        }
        for (int i = -6; i <= 6; ++i) {
            for (int j = -6; j <= 6; ++j) {
                for (int k = -2; k >= -8; --k) {
                    final BlockState bid = level.getBlockState(new BlockPos(x + i, y + k, z + j));
                    if (bid.isAir()) {
                        return false;
                    }
                }
            }
        }
        for (int i = -6; i <= 6; ++i) {
            for (int j = -6; j <= 6; ++j) {
                for (int k = 2; k <= 8; ++k) {
                    final BlockState bid = level.getBlockState(new BlockPos(x + i, y + k, z + j));
                    if (!bid.isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // initCreature (:313-314) is empty and never called in 1.7.10.

    /** {@code attackEntityFrom} (:316-323): immune to suffocation. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return ret;
        }
        ret = super.hurt(par1DamageSource, par2);
        return ret;
    }

    /** {@code writeEntityToNBT} (:325-328). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("wormsSpawned", this.wormsSpawned);
    }

    /** {@code readEntityFromNBT} (:330-333). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.wormsSpawned = par1NBTTagCompound.getInt("wormsSpawned");
    }

    /**
     * {@code spawnCreature(World, String, x, y, z)} (:335-343) with the entity name as its type: create, place with a
     * random yaw from the level random, add. No {@code finalizeSpawn}, like {@code EntityList.createEntityByName}.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2, final double par4,
                                       final double par6) {
        Entity var8 = null;
        var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
        }
        return var8;
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment roll ({@code Mob.dropCustomDeathLoot}).
     * {@code getDropItem} (:345-347) is unused because {@code dropFewItems} is overridden.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropItemRand} (:349-352): up to three blocks off on {@code OreSpawnRand}, 2.5..5.5 up on the level random. */
    private void dropItemRand(final Item index, final int par1) {
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                this.getY() + 2.5 + this.level().random.nextInt(4),
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(4) - OreSpawn.OreSpawnRand.nextInt(4),
                new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /**
     * {@code dropFewItems} (:354-378): worm tooth, item frame, 6 rotten flesh, 6 leather, 8 dirt, 16 gold nuggets,
     * 5 diamonds, 4 uranium and 4 titanium nuggets; looting and {@code recentlyHit} ignored.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        this.dropItemRand(ModItems.WORM_TOOTH.get(), 1);
        this.dropItemRand(Items.ITEM_FRAME, 1);
        for (int var4 = 0; var4 < 6; ++var4) {
            this.dropItemRand(Items.ROTTEN_FLESH, 1);
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            this.dropItemRand(Items.LEATHER, 1);
        }
        for (int var4 = 0; var4 < 8; ++var4) {
            this.dropItemRand(Items.DIRT, 1);
        }
        for (int var4 = 0; var4 < 16; ++var4) {
            this.dropItemRand(Items.GOLD_NUGGET, 1);
        }
        for (int var4 = 0; var4 < 5; ++var4) {
            this.dropItemRand(Items.DIAMOND, 1);
        }
        for (int var4 = 0; var4 < 4; ++var4) {
            this.dropItemRand(ModItems.URANIUM_NUGGET.get(), 1);
        }
        for (int var4 = 0; var4 < 4; ++var4) {
            this.dropItemRand(ModItems.TITANIUM_NUGGET.get(), 1);
        }
    }

    /**
     * A 1.7.10 task that is only asked while the worm is above ground. While clipping it neither starts nor stops, and
     * a running one is not ticked - the state {@code super.updateAITasks()} left it in when it was skipped.
     */
    private static final class WhileNotNoClip extends Goal {

        private final Mob worm;
        private final Goal goal;

        WhileNotNoClip(final Mob worm, final Goal goal) {
            this.worm = worm;
            this.goal = goal;
            this.setFlags(goal.getFlags());
        }

        @Override
        public boolean canUse() {
            return !this.worm.noPhysics && this.goal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.worm.noPhysics || this.goal.canContinueToUse();
        }

        @Override
        public boolean isInterruptable() {
            return this.goal.isInterruptable();
        }

        @Override
        public void start() {
            this.goal.start();
        }

        @Override
        public void stop() {
            this.goal.stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return this.goal.requiresUpdateEveryTick();
        }

        @Override
        public void tick() {
            if (!this.worm.noPhysics) {
                this.goal.tick();
            }
        }
    }

    /** {@code navigator.onUpdateNavigation()}, skipped while clipping. */
    private static final class NoClipGatedNavigation extends GroundPathNavigation {

        NoClipGatedNavigation(final Mob mob, final Level level) {
            super(mob, level);
        }

        @Override
        public void tick() {
            if (!this.mob.noPhysics) {
                super.tick();
            }
        }
    }

    /** {@code moveHelper.onUpdateMoveHelper()}, skipped while clipping. */
    private static final class NoClipGatedMoveControl extends MoveControl {

        NoClipGatedMoveControl(final Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (!this.mob.noPhysics) {
                super.tick();
            }
        }
    }

    /** {@code lookHelper.onUpdateLook()}, skipped while clipping. */
    private static final class NoClipGatedLookControl extends LookControl {

        NoClipGatedLookControl(final Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (!this.mob.noPhysics) {
                super.tick();
            }
        }
    }

    /** {@code jumpHelper.doJump()}, skipped while clipping. */
    private static final class NoClipGatedJumpControl extends JumpControl {

        private final Mob worm;

        NoClipGatedJumpControl(final Mob mob) {
            super(mob);
            this.worm = mob;
        }

        @Override
        public void tick() {
            if (!this.worm.noPhysics) {
                super.tick();
            }
        }
    }
}
