package com.swbr.orespawn.entity.robot;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.Robot2} ("Robo-Pounder", Robot2.java:16-438, verhalten/entity-11.md): the big
 * melee robot that smashes the ground under its target and tears up the terrain around itself.
 *
 * <p>Health, attack and defense from {@code Robot2_stats} (200/22/18, :46-48, :76-78, :95-97), speed 0.3 set every
 * tick (:28, :71-74), {@code experienceValue} 100 (:31), {@code fireResistance} 200 (:32), fire immune (:33, the
 * entity type), hitbox 3.0 x 6.2 (:29).
 *
 * <p>Original behaviour kept (R18): {@code just_for_fun} only counts down in ticks where {@code attacking} was 0,
 * so a rampage lasts much longer than 50 ticks; the {@code +0.25} jump boost is overwritten by {@code super}; only a
 * {@code Mob} attacker (not a player) is turned on directly; Laser Balls never hurt it (in {@code LaserBall}).
 */
public class Robot2 extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by the model for the arm swings. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(Robot2.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side scratch data of ModelRobot2 ({@code ri1} = which arms pound). */
    private RenderInfo renderdata;
    private int just_for_fun;
    private float moveSpeed;

    /** {@code Robot2(World)} (:23-42). */
    public Robot2(final EntityType<? extends Robot2> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.just_for_fun = 0;
        this.moveSpeed = 0.3f;
        // setSize(3.0f, 6.2f) (:29) is the entity type's size.
        // PORT: getNavigator().setAvoidsWater(true) (:30) - water is impassable for the path finder, malus -1 (W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 100;
        // fireResistance = 200 (:32) is getFireImmuneTicks; isImmuneToFire (:33) is EntityType.Builder.fireImmune().
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MyEntityAIWanderALot(this, 14, 1.0));
        // PORT: EntityAIMoveThroughVillage(0.9, false) walked through 1.7.10 village doors; the 1.21.1 goal walks towards
        // unvisited village POIs (distance 4, no door handling).
        this.goalSelector.addGoal(2, new MoveThroughVillageGoal(this, 0.8999999761581421, false, 4, () -> false));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal forgets an unseen target
        // after 60 ticks (Lizard precedent).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // PORT: applyEntityAttributes (:44-49) read Robot2_stats during construction; the attribute supplier is built
        // before the config loads (R3), so the runtime values go in here, followed by the full health the 1.7.10
        // EntityLivingBase constructor set (Girlfriend precedent).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Robot2_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:44-49) with the config defaults (manifest 200 / 0.3 / 22); see the constructor. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 22.0);
    }

    /** {@code entityInit} (:51-65). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    // canDespawn (:67-69) = !isNoDespawnRequired(): the Mob default.

    /** {@code onUpdate} (:71-74). */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:76-78). */
    public int mygetMaxHealth() {
        return MobStats.Robot2_stats().health();
    }

    /** {@code getRenderInfo} (:80-82). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:84-93). */
    public void setRenderInfo(final RenderInfo r) {
        this.renderdata.rf1 = r.rf1;
        this.renderdata.rf2 = r.rf2;
        this.renderdata.rf3 = r.rf3;
        this.renderdata.rf4 = r.rf4;
        this.renderdata.ri1 = r.ri1;
        this.renderdata.ri2 = r.ri2;
        this.renderdata.ri3 = r.ri3;
        this.renderdata.ri4 = r.ri4;
    }

    /** {@code getTotalArmorValue} (:95-97). */
    public int getTotalArmorValue() {
        return MobStats.Robot2_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code fireResistance = 200} (:32). */
    @Override
    protected int getFireImmuneTicks() {
        return 200;
    }

    // onLivingUpdate (:103-105) only called super.

    /** {@code jump} (:107-110): the added 0.25 is overwritten by the jump speed {@code super} assigns (Ostrich precedent). */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y + 0.25, m.z);
        super.jumpFromGround();
    }

    /** {@code getLivingSound} (:112-117): one call in four. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(4) == 0) {
            return ModSounds.ROBOT_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:119-121). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ROBOT_HURT.get();
    }

    /** {@code getDeathSound} (:123-125). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ROBOT_DEATH.get();
    }

    /** {@code getSoundVolume} (:127-129). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:131-133). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:135-137, iron block) is never read: dropFewItems below does not call super.

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:149-202): 2-9 iron blocks, 5-10 iron ingots, then 5-14 rolls of {@code rand(15)} on the
     * redstone table. Looting is ignored, as in the original.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var5 = 2 + this.level().random.nextInt(8), var6 = 0; var6 < var5; ++var6) {
            RobotSupport.dropItemRand(this, Blocks.IRON_BLOCK, 1);
        }
        for (int var5 = 5 + this.level().random.nextInt(6), var6 = 0; var6 < var5; ++var6) {
            RobotSupport.dropItemRand(this, Items.IRON_INGOT, 1);
        }
        for (int i = 5 + this.level().random.nextInt(10), var6 = 0; var6 < i; ++var6) {
            final int var7 = this.level().random.nextInt(15);
            RobotSupport.dropRedstoneRoll(this, var7);
        }
    }

    // interact (:204-206) returned false: Mob.mobInteract already passes. attackEntityAsMob (:208-210) only called
    // super, which is Mob.doHurtTarget.

    /**
     * The seven blocks {@code destroyBlock} and {@code destroyNearbyBlocks} leave alone (:217-237, :249-255).
     *
     * <p>PORT: {@code Blocks.quartz_block} carried the chiseled and pillar variants as metadata; they are their own
     * blocks in 1.21.1 and are spared too (R18, metadata variants map to the separate types).
     * {@code Blocks.mob_spawner} is {@code SPAWNER}; {@code Blocks.chest} does not include the trapped chest, which
     * was a separate block in 1.7.10 as well.
     */
    private static boolean isSpared(final BlockState bid) {
        if (bid.is(Blocks.OBSIDIAN)) {
            return true;
        }
        if (bid.is(Blocks.BEDROCK)) {
            return true;
        }
        if (bid.is(Blocks.QUARTZ_BLOCK) || bid.is(Blocks.CHISELED_QUARTZ_BLOCK) || bid.is(Blocks.QUARTZ_PILLAR)) {
            return true;
        }
        if (bid.is(Blocks.SPAWNER)) {
            return true;
        }
        if (bid.is(Blocks.REDSTONE_BLOCK)) {
            return true;
        }
        if (bid.is(Blocks.IRON_BLOCK)) {
            return true;
        }
        return bid.is(Blocks.CHEST);
    }

    /**
     * {@code destroyBlock} (:212-241): the block under the target, x and z shifted by up to one block, becomes air
     * without drops.
     *
     * <p>PORT: {@code (int)} coordinates are {@code Mth.floor} (R20). {@code Blocks.air} is {@code isAir()};
     * {@code mobGriefing} is asked through {@link EventHooks#canEntityGrief} (W06 precedent). {@code setBlock} with
     * flag 3 as {@code World.setBlock(x, y, z, block)}.
     */
    protected void destroyBlock(final LivingEntity e) {
        final double x = e.getX() + this.level().random.nextFloat() - this.level().random.nextFloat();
        final double y = e.getY() - 1.0;
        final double z = e.getZ() + this.level().random.nextFloat() - this.level().random.nextFloat();
        final BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        final BlockState bid = this.level().getBlockState(pos);
        if (isSpared(bid)) {
            return;
        }
        if (!bid.isAir() && EventHooks.canEntityGrief(this.level(), this)) {
            this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** {@code destroyNearbyBlocks} (:243-267): 50 random blocks in x/z +-6.5 and y +0.1..+8.6; PORT as {@link #destroyBlock}. */
    protected void destroyNearbyBlocks() {
        for (int i = 0; i < 50; ++i) {
            final double x = this.getX() + this.level().random.nextFloat() * 6.5 - this.level().random.nextFloat() * 6.5;
            final double y = this.getY() + 0.1 + this.level().random.nextFloat() * 8.5;
            final double z = this.getZ() + this.level().random.nextFloat() * 6.5 - this.level().random.nextFloat() * 6.5;
            final BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
            final BlockState bid = this.level().getBlockState(pos);
            if (!isSpared(bid)) {
                if (!bid.isAir() && EventHooks.canEntityGrief(this.level(), this)) {
                    this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    /** 1.21.1 runs this after the goal selectors and the navigation (Mob.serverAiStep); see {@link #updateAITasks}. */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * {@code updateAITasks} (:269-335). {@code super.updateAITasks()} (:273) is the goal, navigation and control tick
     * that {@code Mob.serverAiStep} runs around {@code customServerAiStep}.
     *
     * <p>PORT: the OreSpawn part therefore runs before the move, look and jump controls of the same tick instead of
     * after them (EntityCannonFodder precedent). {@code faceEntity} is {@link Mob#lookAt}, {@code setAttackTarget}
     * is {@link Mob#setTarget}, {@code tryMoveToEntityLiving} is {@code getNavigation().moveTo}.
     */
    protected void updateAITasks() {
        // isDead is the removed flag, not the health.
        if (this.isRemoved()) {
            return;
        }
        if (this.level().random.nextInt(6) == 1 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            LivingEntity e = null;
            if (this.level().random.nextInt(50) == 1) {
                this.setTarget((LivingEntity) null);
            }
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget((LivingEntity) null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                // The heading is taken from rotationYaw before faceEntity turns the body (:288-295).
                final double rdd = RobotSupport.headingDifference(this, e, this.getYRot());
                this.lookAt(e, 10.0f, 10.0f);
                if (rdd < 1.25) {
                    final float reach = 5.0f + e.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(e) < reach * reach) {
                        this.setAttacking(1);
                        if (this.level().random.nextInt(5) == 0 || this.level().random.nextInt(6) == 1) {
                            this.doHurtTarget(e);
                            for (int i = 0; i < 6; ++i) {
                                this.destroyBlock(e);
                            }
                        }
                        this.destroyNearbyBlocks();
                    }
                } else {
                    this.setAttacking(0);
                }
                this.getNavigation().moveTo(e, 1.0);
            } else {
                this.setAttacking(0);
            }
        }
        if (this.getAttacking() == 0 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            if (this.level().random.nextInt(450) == 1) {
                this.just_for_fun = 50;
            }
            if (this.just_for_fun > 0) {
                --this.just_for_fun;
            }
            if (this.just_for_fun > 0) {
                this.setAttacking(1);
                if (this.level().random.nextInt(3) == 1) {
                    this.destroyNearbyBlocks();
                }
            } else {
                this.setAttacking(0);
            }
        }
    }

    /**
     * {@code attackEntityFrom} (:337-350): cactus does nothing; otherwise the hit lands and an {@code EntityLiving}
     * attacker - a {@code Mob}, never a player - becomes the target and is walked at.
     *
     * <p>PORT: {@code setTarget(Entity)} set the old-AI {@code entityToAttack}, which an AI-enabled mob never read;
     * it has no counterpart.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            ret = super.hurt(par1DamageSource, par2);
            final Entity e = par1DamageSource.getEntity();
            if (e != null && e instanceof Mob) {
                this.setTarget((LivingEntity) e);
                this.getNavigation().moveTo(e, 1.2);
            }
            return ret;
        }
        return false;
    }

    /** {@code findSomethingToAttack} (:381-394), box 14/3/14. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return RobotSupport.findSomethingToAttack(this, this.TargetSorter, 14.0, 3.0, 14.0);
    }

    /** {@code getAttacking} (:396-398). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:400-402). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * {@code getCanSpawnHere} (:404-437) as the placement predicate: a "Robo-Pounder" spawner allows it; otherwise
     * y at least 50, night, a clear column 1..5 above, valid light.
     *
     * <p>PORT: the spawner is recognised by {@link MobSpawnType#SPAWNER} (wave rule W07), not by scanning x/z -3..2,
     * y 0..4 for a spawner block naming this mob. A spawner of this type only spawns this type, so every spawner
     * spawn passes here; in 1.7.10 a robot placed outside that scan box (up to 4 blocks away, or below the spawner)
     * fell through to the night and light test, and a natural spawn next to such a spawner passed.
     */
    public static boolean checkRobot2SpawnRules(final EntityType<Robot2> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (!RobotSupport.columnIsClear(level, pos, 6)) {
            return false;
        }
        return LegacyLightLevel.isValidLightLevel(level, pos, random);
    }

    /** The whole rule is the placement predicate; the override did not call {@code EntityMob.getCanSpawnHere}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override also dropped {@code EntityLiving}'s collision and liquid test (Ghost precedent). */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }
}
