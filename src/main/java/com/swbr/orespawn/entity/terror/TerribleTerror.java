package com.swbr.orespawn.entity.terror;

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
 * Port of {@code danger.orespawn.TerribleTerror} (TerribleTerror.java:14-288), id {@code terrible_terror} ("Terrible
 * Terror", 64/1/false): a small flying dragon without any AI task; flight, target choice and bite are written by hand in
 * the AI step (verhalten/entity-13.md).
 *
 * <p>Values: {@code TerribleTerror_stats} (10/5/3), speed 0.1 (:34), XP 10 (:25, {@code EntityMob}: the value itself),
 * {@code fireResistance} 5 (:27). The bite is a fixed 5.0 (:80) - the attack stat only reaches the attribute. Vertical
 * motion is damped by 0.6 every tick (:74-77), no fall damage (:142-146), still triggers steps and plates (:138-140,
 * :148-150; no {@code NoStepTrigger}, R20). No DataWatcher, no NBT; {@code currentFlightTarget} is volatile.
 *
 * <p>Despawns only by day (:38-40). {@code isAIEnabled} (:70-72) is the 1.21.1 default.
 */
public class TerribleTerror extends Monster implements LegacyArmor {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;

    /** {@code TerribleTerror(World)} (:19-29) with {@code applyEntityAttributes} (:31-36). */
    public TerribleTerror(final EntityType<? extends TerribleTerror> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        // setSize(1.0f, 0.75f) (:23) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(false) (:24) is a water path malus of 0 (vanilla default 8, W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 10;
        // isImmuneToFire = false (:26) is the type default; fireResistance = 5 (:27) is getFireImmuneTicks.
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.TerribleTerror_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.10000000149011612);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
        // The forward push of the AI step must survive the move control (InsectSupport.LegacyMoveControl, Bee W07).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:31-36) at registration time (R3), armor from {@code getTotalArmorValue} (:66-68). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.TerribleTerror_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code fireResistance = 5} (:27). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /** {@code canDespawn} (:38-40): not persistent and daytime ({@code World.isDaytime}). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired() && InsectSupport.isDaytime(this.level());
    }

    /** {@code getSoundVolume} (:42-44). */
    @Override
    protected float getSoundVolume() {
        return 0.45f;
    }

    /** {@code getSoundPitch} (:46-48). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:50-52). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.TERRIBLETERROR_LIVING.get();
    }

    /** {@code getHurtSound} (:54-56). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.TERRIBLETERROR_HIT.get();
    }

    /** {@code getDeathSound} (:58-60). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TERRIBLETERROR_DEAD.get();
    }

    /** {@code mygetMaxHealth} (:62-64). */
    public int mygetMaxHealth() {
        return MobStats.TerribleTerror_stats().health();
    }

    /** {@code getTotalArmorValue} (:66-68), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.TerribleTerror_stats().defense();
    }

    /** {@code onUpdate} (:74-77): after the vanilla tick, vertical motion times 0.6 - on both sides, as in 1.7.10. */
    @Override
    public void tick() {
        super.tick();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
    }

    /** {@code attackEntityAsMob} (:79-82): a fixed 5.0 mob damage, no knockback, no enchantments. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 5.0f);
        return var4;
    }

    /**
     * {@code canSeeTarget} (:84-86): no block between a point 0.75 above the feet and the target. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded - the outline shape
     * (Bee, W07).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:88-136): a new flight target with 1/100 or when close (x/z ±5..9, y -2..+2, 50 tries,
     * air and visible), otherwise a 1/9 target check with a bite within squared 6; then steer towards the flight target.
     * {@code (int)} casts are {@code Mth.floor} (R20); {@code this.rand} is the entity random, {@code moveForward} is
     * {@code zza}.
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
        if (rand.nextInt(100) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            // for (Block bid = Blocks.stone; bid != Blocks.air && keep_trying != 0; --keep_trying) (:100)
            // PORT: isAir() also accepts cave and void air, which 1.7.10 generated as plain air.
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                zdir = rand.nextInt(5) + 5;
                xdir = rand.nextInt(5) + 5;
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
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < 6.0) {
                    this.doHurtTarget(e);
                }
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

    /** {@code fall(float)} (:142-143) is empty: no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:145-146) is empty: the fall distance never grows, no landing effects. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code attackEntityFrom} (:152-159): any source entity moves the flight target to its position. */
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
     * Spawn predicate: {@code getCanSpawnHere} (:161-178). A "Terrible Terror" spawner allows it; otherwise the monster
     * light test, night, and the Chaos dimension or y at most 40.
     *
     * <p>PORT: the spawner scan (x/z -2..1, y 0..4) is {@link MobSpawnType#SPAWNER} (catalogue 5.9);
     * {@code isValidLightLevel} is {@link LegacyLightLevel#isValidLightLevel} with the spawner's random.
     */
    public static boolean checkTerribleTerrorSpawnRules(final EntityType<TerribleTerror> type, final ServerLevelAccessor level,
                                                        final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        return LegacyLightLevel.isValidLightLevel(level, pos, random) && !InsectSupport.isDaytime(level.getLevel())
                && (TerrorSupport.isChaos(level.getLevel()) || pos.getY() <= 40.0);
    }

    /** The whole {@code getCanSpawnHere} is {@link #checkTerribleTerrorSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code isSuitableTarget} (:180-257): alive, visible, not one of the flying or monstrous OreSpawn kinds listed, not a
     * creative player. {@code MyUtils.isIgnoreable} is not asked. Triffid is tested twice in the original; the second
     * test is dead and kept.
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
        if (par1EntityLiving instanceof RockBase) {
            return false;
        }
        if (par1EntityLiving instanceof TerribleTerror) {
            return false;
        }
        if (par1EntityLiving instanceof EnderReaper) {
            return false;
        }
        if (TerrorSupport.isType(par1EntityLiving, "mothra")) {
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
        if (par1EntityLiving instanceof Mantis) {
            return false;
        }
        if (par1EntityLiving instanceof LeafMonster) {
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

    /** {@code findSomethingToAttack} (:259-276): {@code expand(12, 8, 12)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 12.0, 8.0, 12.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getDropItem} (:278-287): one roll on the world random - rotten flesh, emerald or feather. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(3);
        if (i == 0) {
            return Items.ROTTEN_FLESH;
        }
        if (i == 1) {
            return Items.EMERALD;
        }
        return Items.FEATHER;
    }

    /**
     * 1.7.10 {@code onDeath}: the inherited {@code EntityLiving.dropFewItems} with {@link #getDropItem()} (0..2 plus
     * {@code rand(looting + 1)}), then {@code dropEquipment} (R10).
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }
}
