package com.swbr.orespawn.entity.terror;

import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.arthropod.Bee;
import com.swbr.orespawn.entity.critter.Cockateil;
import com.swbr.orespawn.entity.fairy.Fairy;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Mantis} (Mantis.java:16-386), id {@code mantis} ("Mantis", 64/1/false): a giant
 * praying mantis that flies at monsters, players and small flyers; no AI tasks, everything is hand-written flight in
 * the AI step (verhalten/entity-10.md).
 *
 * <p>Values: {@code Mantis_stats} (120/16/10), speed 0.32 (:44), XP 100 (:35), {@code fireResistance} 5 (:37). The hit
 * is the inherited {@code EntityMob.attackEntityAsMob} with the attack attribute (= {@link Monster#doHurtTarget}).
 * Regenerates 1 with 1/100 per AI tick (:208-210). Vertical motion times 0.6 (:129), no fall damage, pushable but pushes
 * nothing (:85-90), steps and plates as usual (no {@code NoStepTrigger}, R20). DataWatcher 20 = attacking; no NBT,
 * {@code rt} and the flight target are volatile.
 *
 * <p><b>Original bug kept (R18):</b> in water it hits itself with 1/20 per tick (:130-132). That hit makes itself
 * {@code rt} (:227-233), and the attack branch prefers a living {@code rt} without any suitability test (:180-191) at
 * distance 0 - so the mantis keeps attacking itself with 1/8 per AI tick, braked only by the invulnerability ticks,
 * until another attacker takes over {@code rt} or it dies.
 *
 * <p>Not carried over: {@code getDropItem} (:97-99) is dead because {@code dropFewItems} is overridden;
 * {@code initCreature} (:280-281) overrides nothing; {@code isAIEnabled} is the default.
 */
public class Mantis extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:50): the forelegs of {@code ModelMantis} strike while it is 1. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Mantis.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    private int stuck_count;
    private int lastX;
    private int lastZ;
    /** The last living attacker (:22, :231); {@code EntityLivingBase} by every write. */
    @Nullable
    private Entity rt;

    /** {@code Mantis(World)} (:24-38) with {@code applyEntityAttributes} (:40-45). */
    public Mantis(final EntityType<? extends Mantis> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.stuck_count = 0;
        this.lastX = 0;
        this.lastZ = 0;
        this.rt = null;
        // setSize(2.5f, 3.25f) (:32) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(false) (:33) is a water path malus of 0 (vanilla default 8, W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 100;
        // isImmuneToFire = false (:36) is the type default; fireResistance = 5 (:37) is getFireImmuneTicks.
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Mantis_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3199999928474426);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
        // The forward push of the AI step must survive the move control (InsectSupport.LegacyMoveControl, Bee W07).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:40-45) at registration time (R3), armor from {@code getTotalArmorValue} (:276-278). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Mantis_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, 0.3199999928474426)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:47-50). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 5} (:37). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /** {@code canDespawn} (:52-54): not persistent. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code getAttacking} (:56-58). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:60-62). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getSoundVolume} (:64-66). */
    @Override
    protected float getSoundVolume() {
        return 0.35f;
    }

    /** {@code getSoundPitch} (:68-70). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:72-74): {@code orespawn:Beebuzz}, registered lower-case. */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BEEBUZZ.get();
    }

    /** {@code getHurtSound} (:76-78). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DRAGONFLY_HURT.get();
    }

    /** {@code getDeathSound} (:80-82). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code canBePushed} (:84-86): always {@code true}. */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:88-89): the mantis pushes nothing. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:91-93). */
    public int mygetMaxHealth() {
        return MobStats.Mantis_stats().health();
    }

    /** {@code dropItemRand} (:99-102): up to four blocks off on {@code OreSpawnRand}, one block up. */
    private void dropItemRand(final Item index, final int par1) {
        ArthropodSupport.dropItemRand(this, new ItemStack(index, par1), 5);
    }

    /**
     * {@code dropFewItems} (:104-120), then {@code dropEquipment}: two mantis claws, an item frame, 2-11 gold nuggets,
     * 1-3 uranium and titanium nuggets, 2-4 diamonds. Looting and the recently-hit flag are ignored, as in the original.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        final RandomSource rand = this.level().random;
        this.dropItemRand(ModItems.MANTIS_CLAW.get(), 1);
        this.dropItemRand(ModItems.MANTIS_CLAW.get(), 1);
        this.dropItemRand(Items.ITEM_FRAME, 1);
        for (int var4 = 2 + rand.nextInt(10), i = 0; i < var4; ++i) {
            this.dropItemRand(Items.GOLD_NUGGET, 1);
        }
        for (int var4 = 1 + rand.nextInt(3), i = 0; i < var4; ++i) {
            this.dropItemRand(ModItems.URANIUM_NUGGET.get(), 1);
        }
        for (int var4 = 1 + rand.nextInt(3), i = 0; i < var4; ++i) {
            this.dropItemRand(ModItems.TITANIUM_NUGGET.get(), 1);
        }
        for (int var4 = 2 + rand.nextInt(3), i = 0; i < var4; ++i) {
            this.dropItemRand(Items.DIAMOND, 1);
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code onUpdate} (:126-132): after the vanilla tick, vertical motion times 0.6; in water it hits itself with 1/20
     * (the inherited {@code attackEntityAsMob}, R18 bug kept).
     *
     * <p>The self-hit runs on both sides as in 1.7.10: on the client {@code hurt} returns {@code false} before anything
     * happens, in 1.7.10 as in 1.21.1, and {@link Monster#doHurtTarget} has no client-side effect besides that call.
     */
    @Override
    public void tick() {
        super.tick();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        if (this.isInWater() && this.level().random.nextInt(20) == 1) {
            this.doHurtTarget(this);
        }
    }

    /** {@code canSeeTarget} (:134-136), see {@code TerribleTerror.canSeeTarget}. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:138-211): stuck detection, a new flight target when stuck, 1/300 or close (x/z ±4..12,
     * y -3..+2, 50 tries), otherwise a 1/8 target check - {@code rt} first while it lives, else a search - with a hit
     * within squared {@code 5 + width/2}; then steer, then 1/100 heal 1. {@code (int)} casts are {@code Mth.floor}
     * (R20).
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
        final RandomSource rand = this.getRandom();
        if (this.stuck_count > 50 || rand.nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            // Block bid = Blocks.stone (:158): "not air yet". PORT: isAir() also accepts cave and void air.
            boolean bidIsAir = false;
            this.stuck_count = 0;
            while (!bidIsAir && keep_trying != 0) {
                zdir = rand.nextInt(9) + 4;
                xdir = rand.nextInt(9) + 4;
                if (rand.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (rand.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir, Mth.floor(this.getY()) + rand.nextInt(6) - 3, Mth.floor(this.getZ()) + zdir);
                final BlockState bid = this.level().getBlockState(this.currentFlightTarget);
                bidIsAir = bid.isAir();
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
                --keep_trying;
            }
        } else if (rand.nextInt(8) == 0) {
            LivingEntity e = null;
            e = (LivingEntity) this.rt;
            // PORT: EntityLivingBase.isDead is isRemoved() (Bee, W07).
            if (e != null && e.isRemoved()) {
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.setAttacking(1);
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 1, Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < (5.0f + e.getBbWidth() / 2.0f) * (5.0f + e.getBbWidth() / 2.0f)) {
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.30000000149011613;
        final double motionY = m.y + (Math.signum(var2) * 0.699999988079071 - m.y) * 0.20000000149011612;
        final double motionZ = m.z + (Math.signum(var3) * 0.5 - m.z) * 0.30000000149011613;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + var5 / 4.0f);
        if (this.level().random.nextInt(100) == 1) {
            this.heal(1.0f);
        }
    }

    /** {@code fall(float)} (:217-218) is empty. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:220-221) is empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code attackEntityFrom} (:227-235): a living attacker becomes {@code rt} and the flight target moves to it - on the
     * self-hit in water that is the mantis itself (R18 bug kept).
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final boolean ret = super.hurt(par1DamageSource, par2);
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof LivingEntity && this.currentFlightTarget != null) {
            this.rt = e;
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
        }
        return ret;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:237-275). A "Mantis" spawner allows it; otherwise air in x/z -2..1,
     * y +1..+5, in Chaos 1/6 on the world random, y at least 50, daytime and no other Mantis in
     * {@code expand(32, 16, 32)}. No light test.
     *
     * <p>PORT: the spawner scan (x/z -2..2, y 1..3) is {@link MobSpawnType#SPAWNER} (catalogue 5.9); the Mantis search box
     * is the type's spawn box at the position (Molenoid, W07).
     */
    public static boolean checkMantisSpawnRules(final EntityType<Mantis> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!ArthropodSupport.isAllAir(level, pos, -2, 2, 1, 6)) {
            return false;
        }
        if (TerrorSupport.isChaos(level.getLevel()) && level.getRandom().nextInt(6) != 0) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (!InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        return level.getEntitiesOfClass(Mantis.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(32.0, 16.0, 32.0)).isEmpty();
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkMantisSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getTotalArmorValue} (:276-278), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Mantis_stats().defense();
    }

    /**
     * {@code isSuitableTarget} (:284-366): alive, visible, not in water; a player unless creative; not the listed water,
     * flying and fellow insect kinds; otherwise any monster, butterfly, cockatiel or fairy, or
     * {@code MyUtils.isAttackableNonMob}. The second player test (:357-362) is unreachable and kept.
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
        if (par1EntityLiving.isInWater()) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof Mantis) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "irukandji")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "skate")) {
            return false;
        }
        if (par1EntityLiving instanceof Flounder) {
            return false;
        }
        if (par1EntityLiving instanceof Whale) {
            return false;
        }
        // PORT: EntitySquid is Squid; 1.21.1's GlowSquid extends it and did not exist in 1.7.10.
        if (par1EntityLiving instanceof Squid) {
            return false;
        }
        if (par1EntityLiving instanceof WaterDragon) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "attack_squid")) {
            return false;
        }
        if (par1EntityLiving instanceof TerribleTerror) {
            return false;
        }
        if (par1EntityLiving instanceof LurkingTerror) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "cloud_shark")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "rotator")) {
            return false;
        }
        if (par1EntityLiving instanceof Bee) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "mothra")) {
            return false;
        }
        // EntityMob: every OreSpawn and vanilla monster (Molenoid, W07).
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return true;
        }
        if (par1EntityLiving instanceof Cockateil) {
            return true;
        }
        if (par1EntityLiving instanceof Fairy) {
            return true;
        }
        if (par1EntityLiving instanceof Player p2 && !p2.getAbilities().instabuild) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /** {@code findSomethingToAttack} (:368-385): {@code expand(16, 8, 16)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 8.0, 16.0, t -> this.isSuitableTarget(t, false));
    }
}
