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
import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
 * Port of {@code danger.orespawn.Molenoid} (Molenoid.java:16-394), id {@code molenoid} ("Molenoid",
 * OreSpawnMain.java:3993, tracking 64/1/false). A huge star-nosed mole that digs through dirt, throws mole dirt at
 * its target and leaves a trail behind (verhalten/entity-10.md).
 *
 * <p>Values: {@code Molenoid_stats} (200/18/12), speed 0.35 re-set every tick (:24, :55), {@code experienceValue} 40
 * (:27), {@code fireResistance} 100 (:28), suffocation immune (:126). State: DataWatcher 20 {@code attacking}, a
 * {@code Byte} in the bytecode ({@code javap}, the decompile shows {@code int}). No NBT.
 */
public class Molenoid extends Monster implements LegacyArmor {

    /** DataWatcher 20: {@code attacking}, read by {@code MolenoidModel} for the fast digging arms. */
    private static final EntityDataAccessor<Byte> DATA_ATTACKING = SynchedEntityData.defineId(Molenoid.class, EntityDataSerializers.BYTE);

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;

    /** {@code Molenoid(World)} (:21-36). {@code setSize(3.9f, 2.6f)} (:25) is the entity type's size (R9). */
    public Molenoid(final EntityType<? extends Molenoid> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.35f;
        // PORT: getNavigator().setAvoidsWater(true) (:26) - water malus -1 (Chipmunk, W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 40;
        this.TargetSorter = new GenericTargetSorter(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, MonsterSupport.legacyMoveThroughVillage(this, 1.0));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 16, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        final MobStats stats = MobStats.Molenoid_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:38-43) at registration time (R3), armor from {@code getTotalArmorValue} (:63-65). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Molenoid_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:45-48). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, (byte) 0);
    }

    /** {@code fireResistance = 100} (:28). */
    @Override
    protected int getFireImmuneTicks() {
        return 100;
    }

    /** {@code canDespawn} (:50-52). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:54-57), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:59-61). */
    public int mygetMaxHealth() {
        return MobStats.Molenoid_stats().health();
    }

    /** {@code getTotalArmorValue} (:63-65) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Molenoid_stats().defense();
    }

    /** {@code getLivingSound} (:75-80): 1 in 3 from the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.random.nextInt(3) == 0) {
            return ModSounds.MOLENOID_LIVING.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:82-84). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.MOLENOID_HIT.get();
    }

    /** {@code getDeathSound} (:86-88). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MOLENOID_DEATH.get();
    }

    /** {@code getSoundVolume} (:90-92). */
    @Override
    protected float getSoundVolume() {
        return 1.1f;
    }

    /** {@code getSoundPitch} (:94-96). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    // getDropItem (:98-100) beef: unused, dropFewItems is overridden. interact (:121-123) false: the default.

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:107-116): nose, item frame, 10 gold nuggets, 6 beef, spread ±3, y+1. */
    protected void dropFewItems(final boolean par1, final int par2) {
        MonsterSupport.dropItemRand(this, ModItems.MOLENOID_NOSE.get(), 1, 4, 1.0);
        MonsterSupport.dropItemRand(this, Items.ITEM_FRAME, 1, 4, 1.0);
        for (int var4 = 0; var4 < 10; ++var4) {
            MonsterSupport.dropItemRand(this, Items.GOLD_NUGGET, 1, 4, 1.0);
        }
        for (int var4 = 0; var4 < 6; ++var4) {
            MonsterSupport.dropItemRand(this, Items.BEEF, 1, 4, 1.0);
        }
    }

    /** {@code attackEntityFrom} (:125-127): suffocation ({@code "inWall"}) is ignored. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        return !par1DamageSource.is(DamageTypes.IN_WALL) && super.hurt(par1DamageSource, par2);
    }

    /** {@code attackEntityAsMob} (:129-143): the vanilla hit, then knockback 0.8 / 0.1. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                MonsterSupport.legacyKnockback(this, par1Entity, 0.8, 0.1);
            }
            return true;
        }
        return false;
    }

    /** 1.7.10 {@code setBlock(x, y, z, block)}: flag 3. */
    private void setLegacyBlock(final int x, final int y, final int z, final BlockState state) {
        this.level().setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
    }

    /**
     * {@code updateAITasks} (:145-231), after the goal selectors:
     * <ol>
     * <li>1 in 4 look for a target. With one, face it; within squared {@code (6 + width/2)^2} set {@code attacking} and,
     * within squared 16 with (1/4 or 1/5), hit - otherwise, with {@code PlayNicely == 0}, drop 1..4 mole dirt blocks
     * on the first surface from target y+4 down to y-2 at target x/z ±2. Out of reach walk at 1.25. No target clears
     * {@code attacking}.</li>
     * <li>1 in 2 with the chance {@code 100 * min(speed, 0.35) / 0.35} percent, one mole dirt block 6 blocks from the
     * head yaw (±3) on the first surface from y+4 down to y-3 - the trail.</li>
     * <li>Every tick with {@code PlayNicely == 0}: 3 blocks the other way (±3), heights {@code dir..dir+2} (1, or 2 below a
     * higher target, 0 above a lower one): dirt, grass, gravel, sand and leaves become air with mob griefing, mole dirt
     * always.</li>
     * </ol>
     * The {@code worldObj.isRemote} return (:183-185) is implied by {@code customServerAiStep}.
     *
     * <p>PORT: {@code (int)} coordinates are {@code Mth.floor} (R20); {@code mobGriefing} is
     * {@link EventHooks#canEntityGrief}. PORT: R22 - dirt, sand, gravel and leaves are categories:
     * {@link CannonFodderSupport#isLegacyDirt} ({@code #minecraft:dirt}), {@link MonsterSupport#isLegacySand}
     * ({@code #minecraft:sand}), gravel plus suspicious gravel, and {@link MonsterSupport#isLegacyLeaves}
     * ({@code #minecraft:leaves} without OreSpawn's own leaves).
     */
    @Override
    protected void customServerAiStep() {
        LivingEntity e = null;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(4) == 0) {
            e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) < (6.0f + e.getBbWidth() / 2.0f) * (6.0f + e.getBbWidth() / 2.0f)) {
                    this.setAttacking(1);
                    if (this.distanceToSqr(e) < 16.0 && (this.level().random.nextInt(4) == 0 || this.level().random.nextInt(5) == 1)) {
                        this.doHurtTarget(e);
                    } else if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
                        for (int j = 1 + this.level().random.nextInt(4), k = 0; k < j; ++k) {
                            double dx = e.getX();
                            double dz = e.getZ();
                            dx += (this.level().random.nextFloat() - this.level().random.nextFloat()) * 2.0;
                            dz += (this.level().random.nextFloat() - this.level().random.nextFloat()) * 2.0;
                            for (int i = 4; i > -3; --i) {
                                if (MonsterSupport.isAir(this.level(), Mth.floor(dx), Mth.floor(e.getY()) + i + 1, Mth.floor(dz))
                                        && !MonsterSupport.isAir(this.level(), Mth.floor(dx), Mth.floor(e.getY()) + i, Mth.floor(dz))) {
                                    this.setLegacyBlock(Mth.floor(dx), Mth.floor(e.getY()) + i + 1, Mth.floor(dz),
                                            ModBlocks.MOLEDIRT.get().defaultBlockState());
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    this.getNavigation().moveTo(e, 1.25);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.level().random.nextInt(2) == 0) {
            double spd = 0.0;
            final Vec3 motion = this.getDeltaMovement();
            spd = motion.x * motion.x + motion.z * motion.z;
            spd = Math.sqrt(spd);
            if (spd > this.moveSpeed) {
                spd = this.moveSpeed;
            }
            final int odds = (int) (100.0 * spd / this.moveSpeed);
            if (odds > 0 && this.level().random.nextInt(100) < odds && OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
                double dx = this.getX() + 6.0 * Math.sin(Math.toRadians(this.getYHeadRot()));
                double dz = this.getZ() - 6.0 * Math.cos(Math.toRadians(this.getYHeadRot()));
                dx += (this.level().random.nextFloat() - this.level().random.nextFloat()) * 3.0;
                dz += (this.level().random.nextFloat() - this.level().random.nextFloat()) * 3.0;
                for (int l = 4; l > -4; --l) {
                    if (MonsterSupport.isAir(this.level(), Mth.floor(dx), Mth.floor(this.getY()) + l + 1, Mth.floor(dz))
                            && !MonsterSupport.isAir(this.level(), Mth.floor(dx), Mth.floor(this.getY()) + l, Mth.floor(dz))) {
                        this.setLegacyBlock(Mth.floor(dx), Mth.floor(this.getY()) + l + 1, Mth.floor(dz),
                                ModBlocks.MOLEDIRT.get().defaultBlockState());
                        break;
                    }
                }
            }
        }
        double dx = this.getX() - 3.0 * Math.sin(Math.toRadians(this.getYHeadRot()));
        double dz = this.getZ() + 3.0 * Math.cos(Math.toRadians(this.getYHeadRot()));
        dx += (this.level().random.nextFloat() - this.level().random.nextFloat()) * 3.0;
        dz += (this.level().random.nextFloat() - this.level().random.nextFloat()) * 3.0;
        int dir = 1;
        if (e != null) {
            if (Mth.floor(e.getY()) > Mth.floor(this.getY())) {
                dir = 2;
            }
            if (Mth.floor(e.getY()) < Mth.floor(this.getY())) {
                dir = 0;
            }
        }
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            for (int m = dir; m < dir + 3; ++m) {
                final int bx = Mth.floor(dx);
                final int by = Mth.floor(this.getY()) + m;
                final int bz = Mth.floor(dz);
                final BlockState bid = this.level().getBlockState(new BlockPos(bx, by, bz));
                if ((CannonFodderSupport.isLegacyDirt(bid) || bid.is(Blocks.GRASS_BLOCK) || bid.is(Blocks.GRAVEL) || bid.is(Blocks.SUSPICIOUS_GRAVEL)
                        || MonsterSupport.isLegacySand(bid) || MonsterSupport.isLegacyLeaves(bid))
                        && EventHooks.canEntityGrief(this.level(), this)) {
                    this.setLegacyBlock(bx, by, bz, Blocks.AIR.defaultBlockState());
                }
                if (bid.is(ModBlocks.MOLEDIRT.get())) {
                    this.setLegacyBlock(bx, by, bz, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /** {@code isSuitableTarget} (:233-258): visible through dirt, a non-creative player, any monster but a Molenoid, or {@code isAttackableNonMob}. */
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
        if (!this.MyCanSee(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof Molenoid) {
            return false;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /** {@code findSomethingToAttack} (:260-277): the first suitable entity in {@code expand(12, 6, 12)} by {@link GenericTargetSorter}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 6.0, 12.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code getAttacking} (:279-281). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:283-285). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, (byte) par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:287-325). A spawner allows it; otherwise the monster light test, y at
     * least 50, night, air in x/z -1..0 on y+1..3 and no other Molenoid within {@code expand(16, 8, 16)}.
     *
     * <p>PORT: spawner scan → {@link MobSpawnType#SPAWNER} (README 5.9); {@code isValidLightLevel} →
     * {@link LegacyLightLevel#isValidLightLevel} (not {@code Monster.isDarkEnoughToSpawn}, which adds the dimension's
     * block-light limit and light test); the whole rule lives here so the light test is rolled once per attempt
     * ({@link #checkSpawnRules} answers {@code true}). {@code findNearestEntityWithinAABB} ran on the not-yet-added
     * entity, so the box is the type's spawn box at the position and no self has to be excluded. During chunk
     * generation the accessor is a {@code WorldGenRegion} that lists no entities (Baryonyx, W06).
     */
    public static boolean checkMolenoidSpawnRules(final EntityType<Molenoid> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (HerbivoreSupport.isDaytime(level.getLevel())) {
            return false;
        }
        for (int k = -1; k < 1; ++k) {
            for (int j = -1; j < 1; ++j) {
                for (int i = 1; i < 4; ++i) {
                    if (!MonsterSupport.isAir(level, pos.getX() + j, pos.getY() + i, pos.getZ() + k)) {
                        return false;
                    }
                }
            }
        }
        return level.getEntitiesOfClass(Molenoid.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 8.0, 16.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkMolenoidSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code MyCanSee} (:327-393): a ray of 10 steps from 2 blocks ahead of the body yaw at y+1 towards the target's
     * middle, stretched when a step is longer than a block; air, mole dirt, dirt, grass, tall grass, sand and gravel
     * let it through. Float arithmetic as in the original.
     *
     * <p>PORT: {@code (int) startx} is {@code Mth.floor} (R20). {@code Blocks.tallgrass} is short grass and fern
     * ({@link CannonFodderSupport#isLegacyTallGrass}). PORT: R22 - dirt, sand and gravel are categories:
     * {@code Blocks.dirt} is {@code #minecraft:dirt} ({@link CannonFodderSupport#isLegacyDirt}), {@code Blocks.sand}
     * is {@code #minecraft:sand} ({@link MonsterSupport#isLegacySand}), {@code Blocks.gravel} gravel and suspicious
     * gravel.
     */
    public boolean MyCanSee(final LivingEntity e) {
        final double xzoff = 2.0;
        int nblks = 10;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + 1.0);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 10.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 10.0);
        float dz = (float) ((e.getZ() - startz) / 10.0);
        if (Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks *= (int) Math.abs(dx);
            if (dx > 1.0f) {
                dx = 1.0f;
            }
            if (dx < -1.0f) {
                dx = -1.0f;
            }
        }
        if (Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks *= (int) Math.abs(dy);
            if (dy > 1.0f) {
                dy = 1.0f;
            }
            if (dy < -1.0f) {
                dy = -1.0f;
            }
        }
        if (Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks *= (int) Math.abs(dz);
            if (dz > 1.0f) {
                dz = 1.0f;
            }
            if (dz < -1.0f) {
                dz = -1.0f;
            }
        }
        for (int i = 0; i < nblks; ++i) {
            startx += dx;
            starty += dy;
            startz += dz;
            final BlockState bid = this.level().getBlockState(new BlockPos(Mth.floor(startx), Mth.floor(starty), Mth.floor(startz)));
            if (!bid.isAir()) {
                if (!bid.is(ModBlocks.MOLEDIRT.get())) {
                    if (!CannonFodderSupport.isLegacyDirt(bid)) {
                        if (!bid.is(Blocks.GRASS_BLOCK)) {
                            if (!CannonFodderSupport.isLegacyTallGrass(bid)) {
                                if (!MonsterSupport.isLegacySand(bid)) {
                                    if (!bid.is(Blocks.GRAVEL) && !bid.is(Blocks.SUSPICIOUS_GRAVEL)) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
