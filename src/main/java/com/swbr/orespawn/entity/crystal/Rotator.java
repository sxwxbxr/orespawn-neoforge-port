package com.swbr.orespawn.entity.crystal;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.arthropod.Bee;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.cow.CrystalCow;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.herbivore.Peacock;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.portal.Termite;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Rotator} (Rotator.java:15-327): {@code rotator}, the flying gyroscope of the Crystal
 * dimension (verhalten/entity-11.md). {@code EntityMob} without any task; flight, target choice and hit are written by hand
 * in the AI step, it circles its target 2.5 blocks off and despawns by day.
 *
 * <p>Values: health, attack and defense from {@link MobStats#Rotator_stats()} (35/10/8), speed 0.25 (:41), XP 35 (:31),
 * fire immune (:32, entity type), {@code fireResistance} 25 (:33). Vertical motion is damped by 0.6 every tick (:117). No
 * fall damage (:204-208), no pressure plates (:210-212), pushable but pushes nothing (:99-104); {@code canTriggerWalking}
 * returns {@code true} (:200-202), so no {@link com.swbr.orespawn.entity.NoStepTrigger} (R20). No DataWatcher, no NBT;
 * {@code busy_fighting} and {@code was_spawnered} are volatile as in the original.
 *
 * <p>The arrow immunity of {@code attackEntityFrom} (:217-219) tests {@code getEntity()}, the shooter, and therefore never
 * triggers - kept 1:1 (R18). The living sound {@code "vortexlive"} has no namespace (:88) and stays silent (R18).
 *
 * <p>Not carried over: {@code entityInit} (:45-58) only zeroes the render note pad; {@code isAIEnabled} is the 1.21.1
 * default.
 */
public class Rotator extends Monster implements LegacyArmor {

    /**
     * PORT (R18 case 4): the id of the target {@code findSomethingToAttack} found this tick on the server, -1 for none.
     * 1.7.10 ran the search in {@code onUpdate} on both sides (:122) and spawned the spark beam towards the client's own
     * result (:123-127); 1.21.1's {@code Sensing} is never ticked on the client, so the client search would read stale
     * sight results. The server's result is synchronised instead. Not a DataWatcher index of the original, not saved.
     */
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(Rotator.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    /** Client note pad of {@code ModelRotator} (:19). The port's model derives the spin from time instead (R18). */
    private RenderInfo renderdata;
    private int busy_fighting;
    private int was_spawnered;

    /** {@code Rotator(World)} (:23-36) with {@code applyEntityAttributes} (:38-43). */
    public Rotator(final EntityType<? extends Rotator> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.busy_fighting = 0;
        this.was_spawnered = 0;
        // setSize(1.0f, 2.0f) (:30) is the entity type's size (R9).
        this.xpReward = 35;
        // isImmuneToFire = true (:32) is EntityType.Builder.fireImmune(); fireResistance = 25 (:33) is getFireImmuneTicks.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.25);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Rotator_stats().attack());
        this.setHealth(this.getMaxHealth());
        // The forward push of the AI step must survive the move control (InsectSupport.LegacyMoveControl, W05).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:38-43): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 35.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TARGET_ID, -1);
    }

    /** {@code fireResistance = 25} (:33). */
    @Override
    protected int getFireImmuneTicks() {
        return 25;
    }

    /** {@code getRenderInfo} (:60-62). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:64-73). */
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

    /**
     * {@code canDespawn} (:75-77): not while fighting or when it came from a spawner. The persistence term is
     * {@code Mob.checkDespawn}'s own test in 1.21.1.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return this.busy_fighting == 0 && this.was_spawnered == 0;
    }

    /** {@code getSoundVolume} (:79-81). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:83-85). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:87-89): {@code "vortexlive"} without namespace resolved to nothing - silent (R18). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:91-93). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.GLASSHIT.get();
    }

    /** {@code getDeathSound} (:95-97). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.GLASSDEAD.get();
    }

    /** {@code canBePushed} (:99-101): always {@code true}. */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:103-104): pushes nothing. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:106-108). */
    public int mygetMaxHealth() {
        return MobStats.Rotator_stats().health();
    }

    /**
     * {@code onUpdate} (:114-142): after the vanilla tick, vertical motion times 0.6; client sparks with 1/10; the target
     * search with a spark beam towards the target; by day, without persistence, fight or spawner origin, gone with 1/400.
     *
     * <p>PORT (R18 case 4): the search, {@code busy_fighting} and the daytime removal run on the server only; the client
     * reads the target from {@link #DATA_TARGET_ID}. 1.7.10 rolled the removal on the client's world random as well and
     * could make a client copy vanish while the server one lived on. The removal is {@code discard()}, no drops (catalogue).
     */
    @Override
    public void tick() {
        LivingEntity e = null;
        super.tick();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        final RandomSource wr = this.level().random;
        if (this.level().isClientSide) {
            if (wr.nextInt(10) == 1) {
                this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.399999976158142, this.getZ(),
                        (double) ((wr.nextFloat() - wr.nextFloat()) / 4.0f), (double) ((wr.nextFloat() - wr.nextFloat()) / 4.0f),
                        (double) ((wr.nextFloat() - wr.nextFloat()) / 4.0f));
            }
            final Entity t = this.level().getEntity(this.entityData.get(DATA_TARGET_ID));
            if (t != null) {
                final double a = Math.atan2(t.getZ() - this.getZ(), t.getX() - this.getX());
                this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.399999976158142, this.getZ(),
                        Math.cos(a), (t.getY() - this.getY()) / 10.0, Math.sin(a));
            }
            return;
        }
        this.busy_fighting = 0;
        e = this.findSomethingToAttack();
        this.entityData.set(DATA_TARGET_ID, e != null ? e.getId() : -1);
        if (e != null) {
            this.busy_fighting = 1;
        }
        if (this.isPersistenceRequired()) {
            return;
        }
        if (this.busy_fighting != 0) {
            return;
        }
        if (this.was_spawnered != 0) {
            return;
        }
        if (CrystalSupport.isFirstHalfOfDay(this.level()) && wr.nextInt(400) == 1) {
            this.discard();
        }
    }

    /** {@code canSeeTarget} (:144-146). */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return CrystalSupport.canSeeTarget(this, pX, pY, pZ);
    }

    /**
     * {@code updateAITasks} (:148-198): a new flight target with 1/300 or when close; otherwise with 1/9 a target check -
     * the flight target moves 2.5 blocks beside the target, 90 degrees round, and it hits below distance squared 9; then
     * steer. {@code (int)} casts are {@code Mth.floor} (R20); {@code this.rand} is the entity random.
     */
    @Override
    protected void customServerAiStep() {
        LivingEntity e = null;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.getRandom().nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            CrystalSupport.pickFlightTarget(this, this.currentFlightTarget, 10, 8);
        } else if (this.getRandom().nextInt(9) == 2) {
            e = this.findSomethingToAttack();
            if (e != null) {
                double a = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                a += 1.5707963267948966;
                this.currentFlightTarget.set(Mth.floor(e.getX() + 2.5 * Math.cos(a)), Mth.floor(e.getY()), Mth.floor(e.getZ() + 2.5 * Math.sin(a)));
                if (this.distanceToSqr(e) < 9.0) {
                    this.doHurtTarget(e);
                }
            }
        }
        CrystalSupport.steer(this, this.currentFlightTarget);
    }

    /** {@code fall(float)} (:204-205) is empty: no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:207-208) is empty: the fall distance never grows. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:210-212). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * {@code attackEntityFrom} (:214-225): refuses when the source's entity is an arrow - which it never is, because
     * {@code getEntity()} is the shooter (R18, 1:1); then the flight target moves to the attacker.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof AbstractArrow) {
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
        }
        return ret;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:227-260) - a valid light level, air in x/z -1..1 and y +1..+2, and the
     * second half of the day ({@code worldTime % 24000 >= 12000}).
     *
     * <p>PORT: the "Rotator" spawner scan (x/z -2..2, y +1..+3) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9); its
     * side effect {@code was_spawnered = 1} is set in {@link #checkSpawnRules}, which spawners ask with that type.
     */
    public static boolean checkRotatorSpawnRules(final EntityType<Rotator> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (!ArthropodSupport.isAllAir(level, pos, -1, 2, 1, 3)) {
            return false;
        }
        return !CrystalSupport.isFirstHalfOfDay(level.getLevel());
    }

    /**
     * The override replaced {@code EntityCreature}'s path-weight test, which 1.21.1 asks here; the predicate holds the rule.
     * A spawner asks with {@link MobSpawnType#SPAWNER}: that is the original's spawner branch, which marked the rotator.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        if (reason == MobSpawnType.SPAWNER) {
            this.was_spawnered = 1;
        }
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getTotalArmorValue} (:262-264), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Rotator_stats().defense();
    }

    /**
     * {@code isSuitableTarget} (:266-290): alive, not ignoreable, visible, no creative player, and none of the crystal
     * and sea creatures of the list, in the original order.
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
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return !(par1EntityLiving instanceof Termite) && !(par1EntityLiving instanceof Vortex)
                && !(par1EntityLiving instanceof Rotator) && !(par1EntityLiving instanceof DungeonBeast)
                && !(par1EntityLiving instanceof Peacock) && !(par1EntityLiving instanceof CrystalCow)
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "irukandji")
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "skate")
                && !(par1EntityLiving instanceof Whale) && !(par1EntityLiving instanceof Flounder)
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "crystal_urchin")
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "terrible_terror")
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "lurking_terror")
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "cloud_shark")
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "mothra")
                && !(par1EntityLiving instanceof Bee)
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "mantis");
    }

    /** {@code findSomethingToAttack} (:292-309): {@code expand(12, 10, 12)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 12.0, 10.0, 12.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getDropItem} (:311-326): one roll of the world random per call. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(4);
        if (i == 0) {
            return ModItems.CRYSTAL_PINK_INGOT.get();
        }
        if (i == 1) {
            return ModItems.TIGERSEYE_INGOT.get();
        }
        if (i == 2) {
            return ModItems.CRYSTALCOAL.get();
        }
        if (i == 3) {
            return Items.IRON_INGOT;
        }
        return null;
    }

    /**
     * 1.7.10 {@code onDeath}: the inherited {@code EntityLiving.dropFewItems} with {@link #getDropItem()} (0..2 plus
     * {@code rand(looting + 1)}), then {@code dropEquipment} ({@code Mob.dropCustomDeathLoot}).
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        HerbivoreSupport.legacyDropFewItems(this, this.getDropItem(), HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }
}
