package com.swbr.orespawn.entity.terror;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.arthropod.Bee;
import com.swbr.orespawn.entity.ender.EnderReaper;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.monster.LeafMonster;
import com.swbr.orespawn.entity.rock.RockBase;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
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
 * Port of {@code danger.orespawn.LurkingTerror} (LurkingTerror.java:14-347), id {@code lurking_terror} ("Lurking
 * Terror", 64/1/false): a flat six-legged flyer without AI tasks, steered by motion vectors in the AI step
 * (verhalten/entity-10.md).
 *
 * <p>Values: {@code LurkingTerror_stats} (30/6/5), speed 0.25 (:36), XP 20 (:27), {@code fireResistance} 5 (:29). The
 * bite is a fixed 5.0 (:121); the attack stat only reaches the attribute. Vertical motion times 0.6 every tick
 * (:115-118), no fall damage (:187-191), steps and plates as usual (:183-185, :193-195; no {@code NoStepTrigger}, R20).
 * DataWatcher 20 = attacking (despawn guard and open jaws); no NBT. The {@link RenderInfo} is the client model's note
 * pad (:18, :43-54, :68-81), never synchronised or saved.
 */
public class LurkingTerror extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:42): 1 while it has a target; read by {@code canDespawn} and {@code ModelLurkingTerror}. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(LurkingTerror.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    private RenderInfo renderdata;

    /** {@code LurkingTerror(World)} (:20-31) with {@code applyEntityAttributes} (:33-38) and {@code entityInit} (:40-54). */
    public LurkingTerror(final EntityType<? extends LurkingTerror> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        // setSize(1.75f, 1.25f) (:25) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(false) (:26) is a water path malus of 0 (vanilla default 8, W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 20;
        // isImmuneToFire = false (:28) is the type default; fireResistance = 5 (:29) is getFireImmuneTicks.
        this.TargetSorter = new GenericTargetSorter(this);
        // entityInit (:43-53): the render info starts zeroed, which a fresh RenderInfo already is.
        final MobStats stats = MobStats.LurkingTerror_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
        // The forward push of the AI step must survive the move control (InsectSupport.LegacyMoveControl, Bee W07).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:33-38) at registration time (R3), armor from {@code getTotalArmorValue} (:107-109). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.LurkingTerror_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code entityInit} (:40-54): DataWatcher 20. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 5} (:29). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /** {@code canDespawn} (:56-58): not persistent and not attacking. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && this.getAttacking() == 0;
    }

    /** {@code getAttacking} (:60-62). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:64-66). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getRenderInfo} (:68-70). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:72-81). */
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

    /** {@code getSoundVolume} (:83-85). */
    @Override
    protected float getSoundVolume() {
        return 0.55f;
    }

    /** {@code getSoundPitch} (:87-89). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:91-93). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.LURKINGHORROR_LIVING.get();
    }

    /** {@code getHurtSound} (:95-97). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.LURKINGHORROR_HIT.get();
    }

    /** {@code getDeathSound} (:99-101). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LURKINGHORROR_DEAD.get();
    }

    /** {@code mygetMaxHealth} (:103-105). */
    public int mygetMaxHealth() {
        return MobStats.LurkingTerror_stats().health();
    }

    /** {@code getTotalArmorValue} (:107-109), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.LurkingTerror_stats().defense();
    }

    /** {@code onUpdate} (:115-118): after the vanilla tick, vertical motion times 0.6, both sides. */
    @Override
    public void tick() {
        super.tick();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
    }

    /** {@code attackEntityAsMob} (:120-123): a fixed 5.0 mob damage. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 5.0f);
        return var4;
    }

    /** {@code canSeeTarget} (:125-127), see {@code TerribleTerror.canSeeTarget}. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:129-181): a new flight target with 1/120 or when close (x/z ±2..11, y -2..+2, 50 tries),
     * otherwise a 1/9 target check that sets {@code attacking} and bites within squared 6; then steer.
     * {@code (int)} casts are {@code Mth.floor} (R20).
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
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        final RandomSource rand = this.getRandom();
        if (rand.nextInt(120) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            // for (Block bid = Blocks.stone; bid != Blocks.air && keep_trying != 0; --keep_trying) (:141)
            // PORT: isAir() also accepts cave and void air, which 1.7.10 generated as plain air.
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                zdir = rand.nextInt(10) + 2;
                xdir = rand.nextInt(10) + 2;
                if (rand.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (rand.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir, Mth.floor(this.getY()) + rand.nextInt(5) - 2, Mth.floor(this.getZ()) + zdir);
                final BlockState bid = this.level().getBlockState(this.currentFlightTarget);
                bidIsAir = bid.isAir();
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
            }
        } else if (rand.nextInt(9) == 0) {
            LivingEntity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.setAttacking(1);
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < 6.0) {
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.4 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.4 - this.getZ();
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.4 - m.x) * 0.30000000149011613;
        final double motionY = m.y + (Math.signum(var2) * 0.699999988079071 - m.y) * 0.20000000149011612;
        final double motionZ = m.z + (Math.signum(var3) * 0.4 - m.z) * 0.30000000149011613;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + var5 / 4.0f);
    }

    /** {@code fall(float)} (:187-188) is empty. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:190-191) is empty. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code attackEntityFrom} (:197-204): any source entity moves the flight target to its position. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final boolean ret = super.hurt(par1DamageSource, par2);
        final Entity e = par1DamageSource.getEntity();
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
        }
        return ret;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:206-237). A "Lurking Terror" spawner allows it; otherwise the monster
     * light test <em>and</em> daytime (dark places by day), 1/2 on the world random, in Chaos another 1/6, no other
     * Lurking Terror in {@code expand(32, 16, 32)} and y at least 10.
     *
     * <p>PORT: spawner scan → {@link MobSpawnType#SPAWNER} (catalogue 5.9); {@code isValidLightLevel} →
     * {@link LegacyLightLevel#isValidLightLevel} with the spawner's random, {@code worldObj.rand} → the level's random.
     * {@code findNearestEntityWithinAABB} ran on the not-yet-added entity, so the box is the type's spawn box at the
     * position (Molenoid, W07).
     */
    public static boolean checkLurkingTerrorSpawnRules(final EntityType<LurkingTerror> type, final ServerLevelAccessor level,
                                                       final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        LurkingTerror target = null;
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (!InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (level.getRandom().nextInt(2) != 1) {
            return false;
        }
        if (TerrorSupport.isChaos(level.getLevel()) && level.getRandom().nextInt(6) != 0) {
            return false;
        }
        target = level.getEntitiesOfClass(LurkingTerror.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(32.0, 16.0, 32.0)).stream().findFirst().orElse(null);
        return target == null && pos.getY() >= 10.0;
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkLurkingTerrorSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code isSuitableTarget} (:239-316): see {@code TerribleTerror.isSuitableTarget}; Triffid again tested twice. */
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
        if (par1EntityLiving instanceof LurkingTerror) {
            return false;
        }
        if (par1EntityLiving instanceof RockBase) {
            return false;
        }
        if (par1EntityLiving instanceof EnderReaper) {
            return false;
        }
        if (par1EntityLiving instanceof LeafMonster) {
            return false;
        }
        if (par1EntityLiving instanceof TerribleTerror) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "mothra")) {
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
        if (par1EntityLiving instanceof Mantis) {
            return false;
        }
        if (par1EntityLiving instanceof CreepingHorror) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "triffid")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "nightmare")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "dragon")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "island")) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "island_too")) {
            return false;
        }
        if (par1EntityLiving instanceof EntityButterfly) {
            return false;
        }
        if (par1EntityLiving instanceof Firefly) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "triffid")) {
            return false;
        }
        if (TerrorSupport.isCreative(par1EntityLiving)) {
            return false;
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:318-335): {@code expand(12, 8, 12)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 12.0, 8.0, 12.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getDropItem} (:337-346): one roll on the world random - beef, flint or feather. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(3);
        if (i == 0) {
            return Items.BEEF;
        }
        if (i == 1) {
            return Items.FLINT;
        }
        return Items.FEATHER;
    }

    /** 1.7.10 {@code onDeath}: the inherited {@code EntityLiving.dropFewItems} with {@link #getDropItem()} (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }
}
