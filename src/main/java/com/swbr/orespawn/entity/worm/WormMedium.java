package com.swbr.orespawn.entity.worm;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.WormMedium} (WormMedium.java:13-283): {@code medium_worm} ("Medium Worm",
 * OreSpawnMain.java:3735, tracking 64/1/false), the middle stage. Same dig cycle as {@link WormSmall} (up 25..99,
 * down 100..249 ticks), but it stays buried while any Small Worm lives within 8 blocks, bites one tick in 15 and
 * takes boots, or leggings when there are no boots.
 *
 * <p>Values: {@code WormMedium_stats} (30/10/8), speed 0.1, hitbox 0.5 x 2.0, no XP ({@code experienceValue = 0},
 * {@code EntityMob}), no DataWatcher entry, no NBT, never despawns. Drops 2 rotten flesh and 2 leather as loose item
 * entities (R10). The raw {@code posY} handling is the one described in {@link WormSmall}.
 */
public class WormMedium extends Monster implements LegacyArmor, NoStepTrigger {

    public int upcount;
    public int downcount;
    /** A raw {@code posY +=} is waiting to be dropped by the next {@link #move} (see {@link WormSmall}). */
    private boolean legacyPosYPending;

    /** {@code WormMedium(World)} (:18-26). */
    public WormMedium(final EntityType<? extends WormMedium> type, final Level par1World) {
        super(type, par1World);
        this.applyEntityAttributes();
        this.setHealth(this.getMaxHealth());
        this.upcount = 0;
        this.downcount = 0;
        // setSize(0.5f, 2.0f) (:22) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:23) - water malus -1.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 0;
        this.noPhysics = true;
    }

    /** Attribute set with the config defaults; the constructor sets the bases from the loaded config (:28-33). */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    /** {@code applyEntityAttributes} (:28-33). */
    private void applyEntityAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.WormMedium_stats().attack());
    }

    /** {@code canDespawn} (:39-41). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getSoundVolume} (:43-45). */
    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    /** {@code getSoundPitch} (:47-49). */
    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    /** {@code getLivingSound} (:51-53). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:55-57). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LITTLE_SPLAT.get();
    }

    /** {@code getDeathSound} (:59-61). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BIG_SPLAT.get();
    }

    /** {@code canBePushed} (:63-65). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:67-68): empty. */
    @Override
    protected void doPush(final Entity entity) {
    }

    /** {@code collideWithNearbyEntities} (:70-71): empty. */
    @Override
    protected void pushEntities() {
    }

    /** Inherited {@code applyEntityCollision}; see {@link WormSupport#legacyPush}. */
    @Override
    public void push(final Entity entity) {
        WormSupport.legacyPush(this, entity);
    }

    /** {@code mygetMaxHealth} (:73-75). */
    public int mygetMaxHealth() {
        return MobStats.WormMedium_stats().health();
    }

    /**
     * {@code onLivingUpdate} (:81-153): server side only in the original as well (:86-88). A Small Worm within 8 keeps
     * it buried; otherwise the nearest player (creative included) starts the cycle.
     */
    @Override
    public void aiStep() {
        BlockState bid = Blocks.AIR.defaultBlockState();
        Player target = null;
        WormSmall worms = null;
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        worms = WormSupport.findNearestEntityWithinAABB(this, WormSmall.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        if (worms == null) {
            target = WormSupport.findNearestEntityWithinAABB(this, Player.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        }
        if ((worms == null && target != null) || OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            if (this.upcount > 0) {
                --this.upcount;
                if (this.upcount == 0) {
                    this.downcount = 100 + this.level().random.nextInt(150);
                }
                if (target != null) {
                    this.pointAtEntity(target);
                }
                bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY() + 0.25), Mth.floor(this.getZ()));
                if (WormSupport.isTallGrass(bid)) {
                    bid = Blocks.AIR.defaultBlockState();
                }
                if (!bid.isAir()) {
                    if (!WormSupport.isGrassDirtOrStone(bid)) {
                        this.discard();
                    }
                    this.addMotionY(0.20000000298023224);
                    this.legacyAddPosY(0.10000000149011612);
                }
            } else {
                if (this.downcount > 0) {
                    --this.downcount;
                } else {
                    this.upcount = 25 + this.level().random.nextInt(75);
                }
                bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY()) + 3, Mth.floor(this.getZ()));
                if (WormSupport.isTallGrass(bid)) {
                    bid = Blocks.AIR.defaultBlockState();
                }
                if (!bid.isAir()) {
                    if (!WormSupport.isGrassDirtOrStone(bid)) {
                        this.discard();
                    }
                    this.addMotionY(0.10000000149011612);
                    this.legacyAddPosY(0.05000000074505806);
                }
            }
        } else {
            this.upcount = this.level().random.nextInt(50);
            this.downcount = 0;
            bid = this.blockAt(Mth.floor(this.getX()), Mth.floor(this.getY()) + 3, Mth.floor(this.getZ()));
            if (WormSupport.isTallGrass(bid)) {
                bid = Blocks.AIR.defaultBlockState();
            }
            if (!bid.isAir()) {
                if (!WormSupport.isGrassDirtOrStone(bid)) {
                    this.discard();
                }
                this.addMotionY(0.10000000149011612);
                this.legacyAddPosY(0.05000000074505806);
            }
        }
        final Vec3 motion = this.getDeltaMovement();
        // motionY -= 0.01; motionX = 0.0; motionZ = 0.0; moveForward = 0.0f (:149-152).
        this.setDeltaMovement(0.0, motion.y - 0.01, 0.0);
        this.zza = 0.0f;
    }

    /** {@code onUpdate} (:155-161). */
    @Override
    public void tick() {
        if (this.isPersistenceRequired()) {
            this.noPhysics = false;
        }
        super.tick();
        final Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, motion.y * 0.65, motion.z);
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

    /** {@code pointAtEntity} (:163-171). */
    public void pointAtEntity(final LivingEntity e) {
        final double d1 = e.getX() - this.getX();
        final double d2 = e.getZ() - this.getZ();
        final float d3 = (float) Math.atan2(d2, d1);
        final float n;
        final float f2 = n = (float) (d3 * 180.0 / 3.141592653589793) - 90.0f;
        this.setYHeadRot(n);
        this.setYRot(n);
    }

    /** {@code getTotalArmorValue} (:173-175). */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.WormMedium_stats().defense();
    }

    /**
     * {@code updateAITasks} (:177-234): nothing while a Small Worm is within 8; a non-creative player within
     * {@code expand(2.25, 8, 2.25)} is turned to, bitten one tick in 15 while up, and one bite in six takes the boots or,
     * without boots, the leggings.
     *
     * <p>PORT: runs after the navigation tick and before the controls, as in {@link WormSmall#customServerAiStep}.
     */
    @Override
    protected void customServerAiStep() {
        Player target = null;
        WormSmall worms = null;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return;
        }
        worms = WormSupport.findNearestEntityWithinAABB(this, WormSmall.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0));
        if (worms != null) {
            return;
        }
        target = WormSupport.findNearestEntityWithinAABB(this, Player.class, this.getBoundingBox().inflate(2.25, 8.0, 2.25));
        if (target != null && target.getAbilities().instabuild) {
            target = null;
        }
        if (target != null) {
            this.pointAtEntity(target);
            if (this.upcount > 0 && this.level().random.nextInt(15) == 1 && !target.getAbilities().instabuild) {
                super.doHurtTarget(target);
                if (this.level().random.nextInt(6) == 1) {
                    // Player slots 1 and 2: armorInventory[0] boots, armorInventory[1] leggings.
                    ItemStack boots = target.getItemBySlot(EquipmentSlot.FEET);
                    if (!boots.isEmpty()) {
                        WormSupport.stealAndThrow(this, target, EquipmentSlot.FEET, boots, 15);
                    } else {
                        boots = target.getItemBySlot(EquipmentSlot.LEGS);
                        if (!boots.isEmpty()) {
                            WormSupport.stealAndThrow(this, target, EquipmentSlot.LEGS, boots, 15);
                        }
                    }
                }
            }
        }
    }

    /** {@code canTriggerWalking} (:236-238): no step sounds; the block side is {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} (:240-241): empty. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:243-244): empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:246-248). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** {@code getCanSpawnHere} (:250-252): only at night; asked by spawners (no natural spawns in the manifest). */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return !HerbivoreSupport.isDaytime(this.level());
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    // initCreature (:254-255) is empty and never called in 1.7.10.

    /** {@code attackEntityFrom} (:257-264): immune to suffocation. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return ret;
        }
        ret = super.hurt(par1DamageSource, par2);
        return ret;
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment roll ({@code Mob.dropCustomDeathLoot}).
     * {@code getDropItem} (:266-268) is unused because {@code dropFewItems} is overridden.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropItemRand} (:270-273): a fresh stack up to two blocks off on the shared {@code OreSpawnRand}, 2.5..4.5
     * up on the level random, added straight to the level (no pickup delay, not part of the captured drops).
     */
    private void dropItemRand(final Item index, final int par1) {
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(3) - OreSpawn.OreSpawnRand.nextInt(3),
                this.getY() + 2.5 + this.level().random.nextInt(3),
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(3) - OreSpawn.OreSpawnRand.nextInt(3),
                new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /** {@code dropFewItems} (:275-282): 2 rotten flesh, 2 leather; looting and {@code recentlyHit} ignored. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var4 = 0; var4 < 2; ++var4) {
            this.dropItemRand(Items.ROTTEN_FLESH, 1);
        }
        for (int var4 = 0; var4 < 2; ++var4) {
            this.dropItemRand(Items.LEATHER, 1);
        }
    }
}
