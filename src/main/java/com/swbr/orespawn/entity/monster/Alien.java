package com.swbr.orespawn.entity.monster;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModSounds;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
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
 * Port of {@code danger.orespawn.Alien} (Alien.java:17-442), id {@code alien} ("Alien", OreSpawnMain.java:3535,
 * tracking 64/1/false). A very fast {@code EntityMob} that only hunts players and snuffs out torches
 * (verhalten/entity-04.md).
 *
 * <p>Values: health/attack/defense from {@code Alien_stats} (100/12/8), speed 0.65 re-set every tick (:33, :125),
 * {@code experienceValue} 100 (:41, the 1.7.10 {@code EntityLiving} rule, which is 1.21.1's {@code xpReward}),
 * {@code fireResistance} 30 (:42), {@code jumpMovementFactor} 0.6 (:44), jump +0.25 (:86-89), cactus immune (:211),
 * regeneration 1 in 40 per AI tick (:352-354). {@code hurt_timer} is only read and counted down, never set (:214,
 * :315-317) - kept.
 *
 * <p>State: DataWatcher 20 {@code attacking}, a {@code Byte} in the 20.3 bytecode ({@code javap} on
 * {@code Alien.class}, {@code Byte.valueOf} in {@code func_70088_a}); the decompile shows {@code int}. No NBT.
 *
 * <p>PORT: {@code RenderInfo renderdata} (:20, :65-76, :91-104) was client-side animation scratch space that only
 * {@code ModelAlien} read and wrote. {@code RenderInfo} lives in {@code client/}, which common code must not reach
 * (R1), so the per-entity record is kept by {@code AlienModel}, keyed by the entity; a new record is zeroed exactly
 * as {@code entityInit} zeroed it. {@code getRenderInfo}/{@code setRenderInfo} have no counterpart here.
 */
public class Alien extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code AlienModel} to fan out the crest. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(Alien.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private int hurt_timer;
    private double moveSpeed;
    private int closest;
    private int tx;
    private int ty;
    private int tz;

    /** {@code Alien(World)} (:28-53). {@code setSize(1.1f, 3.25f)} (:38) is the entity type's size (R9). */
    public Alien(final EntityType<? extends Alien> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.hurt_timer = 0;
        this.moveSpeed = 0.65;
        this.closest = 99999;
        this.tx = 0;
        this.ty = 0;
        this.tz = 0;
        // PORT: getNavigator().setAvoidsWater(true) (:39) - water is not pathable (malus -1), as Chipmunk (W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        // PORT: getNavigator().setBreakDoors(true) (:40) let the 1.7.10 path finder treat closed wooden doors as
        // passable; no door-breaking task was ever added, so the alien walks into the door. 1.21.1's
        // setCanOpenDoors is that path-finder switch, without adding BreakDoorGoal.
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.xpReward = 100;
        // fireResistance = 30 (:42) is getFireImmuneTicks; isImmuneToFire = false (:43) is the type default.
        // jumpMovementFactor = 0.6f (:44) is getFlyingSpeed.
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, MonsterSupport.legacyMoveThroughVillage(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 10, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has
        // not seen for 60 ticks (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // applyEntityAttributes (:55-60) read OreSpawnMain.Alien_stats in the constructor; the attribute set holds
        // the registration-time values, the runtime config is applied here, followed by full health.
        final MobStats stats = MobStats.Alien_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code applyEntityAttributes} (:55-60) at registration time (R3, {@link StatSource#EARLY}). The armor of
     * {@code getTotalArmorValue} (:106-108) is also the {@code ARMOR} base for the vanilla reduction when
     * {@code legacyArmorFormula} is off; with the switch on the R5 formula reads {@link #getLegacyArmorValue()}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Alien_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, 0.65)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:62-76): DataWatcher 20. The {@code renderdata} reset belongs to {@code AlienModel}. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 30} (:42). */
    @Override
    protected int getFireImmuneTicks() {
        return 30;
    }

    /** {@code canDespawn} (:78-80); persistence is checked by {@code Mob.checkDespawn} before this. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code mygetMaxHealth} (:82-84). */
    public int mygetMaxHealth() {
        return MobStats.Alien_stats().health();
    }

    /** {@code jump} (:86-89): the vanilla jump plus 0.25 upwards. */
    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.25, 0.0));
    }

    /**
     * {@code jumpMovementFactor = 0.6f} (:44). Checked in {@code client-1.7.10.jar}: {@code EntityLivingBase}
     * ({@code sv}) writes the field only in its constructor and reads it only in {@code moveEntityWithHeading}
     * ({@code e(FF)V}) as the airborne acceleration; only {@code EntityPlayer} resets it per tick. The 0.6 therefore
     * stays for the whole life of the alien - 1.21.1's airborne factor is {@code getFlyingSpeed}.
     */
    @Override
    protected float getFlyingSpeed() {
        return 0.6f;
    }

    /** {@code getTotalArmorValue} (:106-108) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Alien_stats().defense();
    }

    /**
     * {@code onLivingUpdate} (:114-122): after the vanilla step, on the client, 1 in 20 a lava drip 1.7..2.45 blocks
     * from the head along the head yaw at y+1.6 ({@code "dripLava"}). The distance is drawn before the chance, as in
     * the original.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            final float f = 1.7f + Math.abs(this.level().random.nextFloat() * 0.75f);
            if (this.level().random.nextInt(20) == 1) {
                this.level().addParticle(ParticleTypes.DRIPPING_LAVA,
                        this.getX() - f * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + 1.6,
                        this.getZ() + f * Math.cos(Math.toRadians(this.getYHeadRot())), 0.0, 0.0, 0.0);
            }
        }
    }

    /** {@code onUpdate} (:124-127), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
    }

    /** {@code getAlienHealth} (:129-131). */
    public int getAlienHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:133-138): 1 in 4 from the world random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.level().random.nextInt(4) == 0) {
            return ModSounds.ALIEN_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:140-142). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALIEN_HURT.get();
    }

    /** {@code getDeathSound} (:144-146). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALIEN_DEATH.get();
    }

    /** {@code getSoundVolume} (:148-150). */
    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    /** {@code getSoundPitch} (:152-154): a fixed pitch, no vanilla jitter. */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:156-158) spider_eye: unused, dropFewItems is overridden.

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:165-175): 5..10 spider eyes, 5..10 flint, one map, clock and compass, spread ±3, y+1. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var5 = 5 + this.level().random.nextInt(6), var6 = 0; var6 < var5; ++var6) {
            MonsterSupport.dropItemRand(this, Items.SPIDER_EYE, 1, 4, 1.0);
        }
        for (int var5 = 5 + this.level().random.nextInt(6), var6 = 0; var6 < var5; ++var6) {
            MonsterSupport.dropItemRand(this, Items.FLINT, 1, 4, 1.0);
        }
        // Items.map is the empty map (ItemEmptyMap); the filled one was Items.filled_map.
        MonsterSupport.dropItemRand(this, Items.MAP, 1, 4, 1.0);
        MonsterSupport.dropItemRand(this, Items.CLOCK, 1, 4, 1.0);
        MonsterSupport.dropItemRand(this, Items.COMPASS, 1, 4, 1.0);
    }

    /**
     * {@code attackEntityAsMob} (:180-207): the vanilla mob hit; on success poison 1 in 5 and knockback 1.1 / 0.1.
     * The poison duration is {@code var2 * 5}: 6, or 8 on EASY. The NORMAL and HARD branches sit inside the EASY branch
     * and can never be reached (:183-192) - kept 1:1 (R18).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                int var2 = 6;
                if (this.level().getDifficulty() == Difficulty.EASY) {
                    var2 = 8;
                    if (this.level().getDifficulty() == Difficulty.NORMAL) {
                        var2 = 10;
                    } else if (this.level().getDifficulty() == Difficulty.HARD) {
                        var2 = 12;
                    }
                }
                if (par1Entity instanceof LivingEntity && this.level().random.nextInt(5) == 1) {
                    ((LivingEntity) par1Entity).addEffect(new MobEffectInstance(MobEffects.POISON, var2 * 5, 0));
                }
                MonsterSupport.legacyKnockback(this, par1Entity, 1.1, 0.1);
            }
            return true;
        }
        return false;
    }

    /**
     * {@code attackEntityFrom} (:209-225): cactus is ignored; the hit counts only while {@code hurt_timer <= 0}
     * (always); a hit whose source entity is an {@code EntityLiving} - a {@link Mob}, not a player - makes that mob the
     * target and walks to it at 1.2, and the method then answers {@code true} whatever the damage did.
     *
     * <p>PORT: {@code setTarget(e)} (:220) set the old-AI {@code entityToAttack}, which a mob with
     * {@code isAIEnabled() == true} never read; it has no counterpart.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (par1DamageSource.is(DamageTypes.CACTUS)) {
            return false;
        }
        if (this.hurt_timer <= 0) {
            ret = super.hurt(par1DamageSource, par2);
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Mob) {
            this.setTarget((LivingEntity) e);
            this.getNavigation().moveTo(e, 1.2);
            ret = true;
        }
        return ret;
    }

    /**
     * {@code Blocks.torch} (standing and wall metadata, now two blocks) or {@code OreSpawnMain.ExtremeTorch} (both).
     * PORT: R22 (addendum 2026-09-14) - "torch" is a category, so the soul torch and soul wall torch (the same
     * {@code TorchBlock}/{@code WallTorchBlock} classes) count too; redstone torches stay out, as in the original.
     */
    private boolean isTorch(final int x, final int y, final int z) {
        final BlockState bid = this.level().getBlockState(new BlockPos(x, y, z));
        return bid.is(Blocks.TORCH) || bid.is(Blocks.WALL_TORCH)
                || bid.is(Blocks.SOUL_TORCH) || bid.is(Blocks.SOUL_WALL_TORCH)
                || bid.is(ModBlocks.EXTREME_TORCH.get())
                || bid.is(ModBlocks.EXTREME_TORCH_WALL.get());
    }

    /** {@code scan_it} (:227-308): one shell of the search cube, the nearest torch wins. */
    private boolean scan_it(final int x, final int y, final int z, final int dx, final int dy, final int dz) {
        int found = 0;
        for (int i = -dy; i <= dy; ++i) {
            for (int j = -dz; j <= dz; ++j) {
                if (this.isTorch(x + dx, y + i, z + j)) {
                    final int d = dx * dx + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + dx;
                        this.ty = y + i;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isTorch(x - dx, y + i, z + j)) {
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
                if (this.isTorch(x + i, y + dy, z + j)) {
                    final int d = dy * dy + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + dy;
                        this.tz = z + j;
                        ++found;
                    }
                }
                if (this.isTorch(x + i, y - dy, z + j)) {
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
                if (this.isTorch(x + i, y + j, z + dz)) {
                    final int d = dz * dz + j * j + i * i;
                    if (d < this.closest) {
                        this.closest = d;
                        this.tx = x + i;
                        this.ty = y + j;
                        this.tz = z + dz;
                        ++found;
                    }
                }
                if (this.isTorch(x + i, y + j, z - dz)) {
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
     * {@code updateAITasks} (:310-355), after the goal selectors: count {@code hurt_timer} down; 1 in 8 look for a
     * player, face it, attack within squared distance 16 with (1/4 or 1/5), and always walk to it at 1.2 - no player
     * clears {@code attacking}; otherwise 1 in 30 (entity random) with {@code PlayNicely == 0} search torches in shells of
     * radius 2..14 (every other shell from 10), walk to the nearest and, closer than squared 27 with mob griefing,
     * replace it by air with flag 2; then 1 in 40 heal 1 below the maximum.
     *
     * <p>PORT: {@code (int)} coordinates are {@code Mth.floor} (R20); {@code mobGriefing} is
     * {@link EventHooks#canEntityGrief}; 1.7.10 flag 2 is {@link HerbivoreSupport#LEGACY_FLAG_2} (W03 lesson). The
     * torch's {@code breakBlock} did nothing in 1.7.10, so nothing is run after the write.
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
        if (this.level().random.nextInt(8) == 0) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < 16.0) {
                    this.setAttacking(1);
                    if (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1) {
                        this.doHurtTarget(e);
                    }
                }
                this.getNavigation().moveTo(e, 1.2);
            } else {
                this.setAttacking(0);
            }
        } else if (this.random.nextInt(30) == 0 && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            this.closest = 99999;
            // :336-339: final boolean tx = false; tz = ty = tx = 0.
            this.tz = 0;
            this.ty = 0;
            this.tx = 0;
            for (int i = 2; i < 15 && !this.scan_it(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()), i, i, i); ++i) {
                if (i >= 10) {
                    ++i;
                }
            }
            if (this.closest < 99999) {
                this.getNavigation().moveTo((double) this.tx, (double) this.ty, (double) this.tz, 1.0);
                if (this.closest < 27 && EventHooks.canEntityGrief(this.level(), this)) {
                    this.level().setBlock(new BlockPos(this.tx, this.ty, this.tz), Blocks.AIR.defaultBlockState(),
                            HerbivoreSupport.LEGACY_FLAG_2);
                }
            }
        }
        if (this.level().random.nextInt(40) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    /** {@code isSuitableTarget} (:357-372): a living non-creative player, nothing else. */
    private boolean isSuitableTarget(@Nullable final LivingEntity var4, final boolean par2) {
        if (var4 == null) {
            return false;
        }
        if (var4 == this) {
            return false;
        }
        if (!var4.isAlive()) {
            return false;
        }
        if (var4 instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return false;
    }

    /**
     * {@code findSomethingToAttack} (:374-395): {@code PlayNicely} returns nothing; the box {@code expand(12, 4, 12)}
     * is sorted first, then a living attack target is kept, otherwise it is cleared and the first suitable entity wins.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 4.0, 12.0));
        var5.sort(this.TargetSorter);
        final LivingEntity e = this.getTarget();
        if (e != null && e.isAlive()) {
            return e;
        }
        this.setTarget((LivingEntity) null);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:397-399). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:401-403). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:405-441). A spawner allows the spawn; otherwise the monster light test,
     * then always in the Islands dimension, elsewhere only at y 50 or below with 3x3 air on y+1..3.
     *
     * <p>PORT: the spawner scan for an "Alien" spawner in x/z -3..2, y 0..4 is {@link MobSpawnType#SPAWNER} (catalogue
     * README 5.9) - a natural spawn next to such a spawner no longer passes on that ground alone.
     * {@code isValidLightLevel} (inherited {@code EntityMob.isValidLightLevel}, not overridden) is
     * {@link LegacyLightLevel#isValidLightLevel}, not {@code Monster.isDarkEnoughToSpawn}, which adds the dimension's
     * block-light limit and light test (W07 review, as Molenoid).
     * The whole rule is the placement predicate, so the random light test is rolled once per attempt as in 1.7.10;
     * {@link #checkSpawnRules} answers {@code true}. {@code (int) posX/posY/posZ} are the block position (R20).
     */
    public static boolean checkAlienSpawnRules(final EntityType<Alien> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (MonsterSupport.isDangerDimension(level.getLevel())) {
            return true;
        }
        if (pos.getY() > 50.0) {
            return false;
        }
        for (int k = -1; k < 2; ++k) {
            for (int j = -1; j < 2; ++j) {
                for (int i = 1; i < 4; ++i) {
                    if (!MonsterSupport.isAir(level, pos.getX() + j, pos.getY() + i, pos.getZ() + k)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkAlienSpawnRules}. */
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
