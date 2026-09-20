package com.swbr.orespawn.entity.crystal;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.aquatic.Flounder;
import com.swbr.orespawn.entity.aquatic.Whale;
import com.swbr.orespawn.entity.cow.CrystalCow;
import com.swbr.orespawn.entity.herbivore.Peacock;
import com.swbr.orespawn.entity.insect.InsectSupport;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Vortex} (Vortex.java:15-352): {@code vortex}, the bodiless pair of eyes of the Crystal
 * and Chaos dimensions that pulls its target in (verhalten/entity-14.md). {@code EntityMob} without any task; flight,
 * pull and hit are written by hand in the AI step.
 *
 * <p>Values: health, attack and defense from {@link MobStats#Vortex_stats()} (150/26/10), speed 0.35 (:40), XP 200
 * (:31), fire immune (:32, entity type), {@code fireResistance} 250 (:33). Vertical motion is damped by 0.6 every tick
 * (:86), it heals 1 with 1/200 per tick (:104-106). After every hit it is {@code winded} for 20 ticks without pull
 * (:139-141, :208). No fall damage (:191-195), no pressure plates (:197-199), pushable but pushes nothing (:68-73);
 * {@code canTriggerWalking} returns {@code true} (:187-189), so no {@link com.swbr.orespawn.entity.NoStepTrigger} (R20).
 * No DataWatcher, no NBT; {@code winded}, {@code busy_fighting} and {@code was_spawnered} are volatile as in the original.
 *
 * <p>Not carried over: {@code getDropItem} (:349-351, {@code eggfairy}) is dead because {@code dropFewItems} is
 * overridden; {@code isAIEnabled} is the 1.21.1 default.
 */
public class Vortex extends Monster implements LegacyArmor {

    /**
     * PORT (R18 case 4): {@code busy_fighting} of the server, for the smoke cone. 1.7.10 ran the target search in
     * {@code onUpdate} on both sides (:88) and drew the smoke from the client's own result (:89-102); 1.21.1's
     * {@code Sensing} is never ticked on the client, so the server's result is synchronised instead. Not a DataWatcher
     * index of the original, not saved.
     */
    private static final EntityDataAccessor<Boolean> DATA_BUSY =
            SynchedEntityData.defineId(Vortex.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    private int winded;
    private int busy_fighting;
    private int was_spawnered;

    /** {@code Vortex(World)} (:23-35) with {@code applyEntityAttributes} (:37-42). */
    public Vortex(final EntityType<? extends Vortex> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.winded = 0;
        this.busy_fighting = 0;
        this.was_spawnered = 0;
        // setSize(2.0f, 4.0f) (:30) is the entity type's size (R9).
        this.xpReward = 200;
        // isImmuneToFire = true (:32) is EntityType.Builder.fireImmune(); fireResistance = 250 (:33) is getFireImmuneTicks.
        this.TargetSorter = new GenericTargetSorter(this);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3499999940395355);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Vortex_stats().attack());
        this.setHealth(this.getMaxHealth());
        // The forward push of the AI step must survive the move control (InsectSupport.LegacyMoveControl, W05).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:37-42): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3499999940395355)
                .add(Attributes.ATTACK_DAMAGE, 26.0);
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BUSY, false);
    }

    /** {@code fireResistance = 250} (:33). */
    @Override
    protected int getFireImmuneTicks() {
        return 250;
    }

    /**
     * {@code canDespawn} (:44-46): not while fighting or when it came from a spawner. The persistence term is
     * {@code Mob.checkDespawn}'s own test in 1.21.1.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return this.busy_fighting == 0 && this.was_spawnered == 0;
    }

    /** {@code getSoundVolume} (:48-50). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:52-54). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:56-58). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.VORTEXLIVE.get();
    }

    /** {@code getHurtSound} (:60-62): none. */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:64-66). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.VORTEXLIVE.get();
    }

    /** {@code canBePushed} (:68-70): always {@code true}. */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:72-73): pushes nothing. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:75-77). */
    public int mygetMaxHealth() {
        return MobStats.Vortex_stats().health();
    }

    /**
     * {@code onUpdate} (:83-121): after the vanilla tick, vertical motion times 0.6; the target search, and while it
     * finds one, 20 smoke particles per tick on the client; heal 1 with 1/200; by day, without persistence, fight or
     * spawner origin, gone with 1/500.
     *
     * <p>PORT (R18 case 4): search, heal and daytime removal run on the server only; the client reads
     * {@link #DATA_BUSY}. 1.7.10 rolled heal and removal on the client's world random as well, which changed only the
     * client copy (a health value the next sync overwrote, or a copy that vanished while the server one lived on). The
     * removal is {@code discard()}, no drops (catalogue).
     */
    @Override
    public void tick() {
        LivingEntity e = null;
        super.tick();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        final RandomSource wr = this.level().random;
        if (this.level().isClientSide) {
            if (this.entityData.get(DATA_BUSY)) {
                for (int i = 0; i < 20; ++i) {
                    double d = wr.nextDouble() * 3.5;
                    d *= d;
                    double dir = wr.nextDouble() * 2.0 * 3.141592653589793;
                    dir -= 3.141592653589793;
                    final double dx = Math.cos(dir) * d / 2.0;
                    final double dz = Math.sin(dir) * d / 2.0;
                    dir += 1.5707963267948966;
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX() + dx, this.getY() + 0.75 + d, this.getZ() + dz,
                            Math.cos(dir) * wr.nextFloat() / 4.0, (double) (wr.nextFloat() / 2.0f), Math.sin(dir) * wr.nextFloat() / 4.0);
                }
            }
            return;
        }
        this.busy_fighting = 0;
        e = this.findSomethingToAttack();
        if (e != null) {
            this.busy_fighting = 1;
        }
        this.entityData.set(DATA_BUSY, this.busy_fighting != 0);
        if (wr.nextInt(200) == 1) {
            this.heal(1.0f);
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
        if (CrystalSupport.isFirstHalfOfDay(this.level()) && wr.nextInt(500) == 1) {
            this.discard();
        }
    }

    /** {@code canSeeTarget} (:123-125). */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return CrystalSupport.canSeeTarget(this, pX, pY, pZ);
    }

    /**
     * {@code updateAITasks} (:127-185): {@code winded} counts down; a new flight target with 1/300 or when close; then
     * every tick a target check - the flight target is the target, within 9 blocks and not winded the target is pulled
     * towards the vortex, within {@code 4 + width/2} a hit with 1/8; then steer. {@code (int)} casts are
     * {@code Mth.floor} (R20); {@code this.rand} is the entity random.
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
        if (this.winded > 0) {
            --this.winded;
        }
        if (this.getRandom().nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.1f) {
            CrystalSupport.pickFlightTarget(this, this.currentFlightTarget, 14, 10);
        }
        e = this.findSomethingToAttack();
        if (e != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
            final double d = this.distanceToSqr(e);
            if (d < 81.0 && this.winded == 0) {
                final double a = Math.atan2(this.getZ() - e.getZ(), this.getX() - e.getX());
                double pm = 1.0;
                if (e instanceof Player) {
                    pm = 2.0;
                }
                e.push(Math.cos(a) * (10.0 - Math.sqrt(d)) * 0.10000000149011612, (10.0 - Math.sqrt(d)) * 0.05000000074505806 * pm,
                        Math.sin(a) * (10.0 - Math.sqrt(d)) * 0.10000000149011612);
                // PORT: 1.7.10 addVelocity only set isAirBorne; whether a player's client ever saw the pull without a
                // simultaneous hit is open (catalogue 6.x). The port sends the motion, as for every server impulse on a
                // player (catalogue: "Serverimpuls an Spieler braucht hurtMarked", Vortex named there).
                e.hurtMarked = true;
            }
            if (this.distanceToSqr(e) < (4.0f + e.getBbWidth() / 2.0f) * (4.0f + e.getBbWidth() / 2.0f) && this.getRandom().nextInt(8) == 2) {
                this.doHurtTarget(e);
            }
        }
        CrystalSupport.steer(this, this.currentFlightTarget);
    }

    /** {@code fall(float)} (:191-192) is empty: no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:194-195) is empty: the fall distance never grows. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:197-199). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** {@code attackEntityFrom} (:201-210): the attacker becomes the flight target; winded for 20 ticks either way. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        final Entity e = par1DamageSource.getEntity();
        ret = super.hurt(par1DamageSource, par2);
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()), Mth.floor(e.getZ()));
        }
        this.winded = 20;
        return ret;
    }

    /** {@code getTotalArmorValue} (:212-214), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Vortex_stats().defense();
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:216-260) - air in x/z -2..2 and y +1..+3, a valid light level, Y at
     * least 50, the second half of the day, 1/2, and no other vortex in the box grown by (20, 16, 20).
     *
     * <p>PORT: the "Vortex" spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9); its
     * side effect {@code was_spawnered = 1} is set in {@link #checkSpawnRules}, which spawners ask with that type. The
     * light test and the 1/2 roll use the spawn's random, because no entity exists yet (the original rolled the
     * entity's and the world's). The neighbour box is the one the new vortex would have at the spawn position.
     */
    public static boolean checkVortexSpawnRules(final EntityType<Vortex> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!ArthropodSupport.isAllAir(level, pos, -2, 3, 1, 4)) {
            return false;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (pos.getY() < 50.0) {
            return false;
        }
        if (CrystalSupport.isFirstHalfOfDay(level.getLevel())) {
            return false;
        }
        if (random.nextInt(2) != 1) {
            return false;
        }
        final AABB box = type.getDimensions().makeBoundingBox(Vec3.atBottomCenterOf(pos)).inflate(20.0, 16.0, 20.0);
        return level.getEntitiesOfClass(Vortex.class, box).isEmpty();
    }

    /**
     * The override replaced {@code EntityCreature}'s path-weight test, which 1.21.1 asks here; the predicate holds the rule.
     * A spawner asks with {@link MobSpawnType#SPAWNER}: that is the original's spawner branch, which marked the vortex.
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

    /**
     * {@code isSuitableTarget} (:262-286): alive, not ignoreable, visible, no creative player, and none of the listed
     * crystal, moth and sea creatures, in the original order. Every other living entity is a target, animals included.
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
        return !(par1EntityLiving instanceof Vortex) && !(par1EntityLiving instanceof Rotator)
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "mothra")
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "brutalfly")
                && !(par1EntityLiving instanceof Peacock) && !(par1EntityLiving instanceof CrystalCow)
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "irukandji")
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "skate")
                && !(par1EntityLiving instanceof Whale) && !(par1EntityLiving instanceof Flounder)
                && !CrystalSupport.isOreSpawnType(par1EntityLiving, "crystal_urchin");
    }

    /** {@code findSomethingToAttack} (:288-305): {@code expand(16, 10, 16)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 16.0, 10.0, 16.0, t -> this.isSuitableTarget(t, false));
    }

    /**
     * {@code dropItemRand} (:307-315): x/z up to five blocks off on the shared {@code OreSpawnRand}, y +1..+10 on the
     * world random, added straight to the level. Argument order as in the original: x, then y, then z.
     *
     * <p>PORT: the toss motion of {@code ItemEntity} is drawn from the level random, where 1.7.10's {@code EntityItem}
     * used {@code Math.random} (Whale, W06).
     */
    private ItemStack dropItemRand(final Item index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        final double x = this.getX() + OreSpawn.OreSpawnRand.nextInt(6) - OreSpawn.OreSpawnRand.nextInt(6);
        final double y = this.getY() + 1.0 + this.level().random.nextInt(10);
        final double z = this.getZ() + OreSpawn.OreSpawnRand.nextInt(6) - OreSpawn.OreSpawnRand.nextInt(6);
        final ItemEntity var3 = new ItemEntity(this.level(), x, y, z, is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /**
     * {@code dropFewItems} (:317-347), then {@code dropEquipment}: always a vortex eye and an item frame, then 5..11 rolls
     * of {@code rand(10)}. Looting and the recently-hit flag are ignored, as in the original.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropItemRand(ModItems.VORTEX_EYE.get(), 1);
        this.dropItemRand(Items.ITEM_FRAME, 1);
        final RandomSource rand = this.level().random;
        for (int i = 5 + rand.nextInt(7), var4 = 0; var4 < i; ++var4) {
            final int var5 = rand.nextInt(10);
            if (var5 == 0) {
                this.dropItemRand(Items.STICK, 1);
            }
            if (var5 == 1) {
                this.dropItemRand(ModItems.TIGERSEYE_INGOT.get(), 1);
            }
            if (var5 == 2) {
                this.dropItemRand(ModItems.CRYSTAL_PINK_INGOT.get(), 1);
            }
            if (var5 == 3) {
                this.dropItemRand(Items.IRON_INGOT, 1);
            }
            if (var5 == 4) {
                this.dropItemRand(ModItems.URANIUM_NUGGET.get(), 1);
            }
            if (var5 == 6) {
                this.dropItemRand(ModItems.TITANIUM_NUGGET.get(), 1);
            }
            if (var5 == 7) {
                this.dropItemRand(ModItems.DEAD_IRUKANDJI.get(), 1);
            }
            if (var5 == 8) {
                this.dropItemRand(ModItems.CRYSTALCOAL.get(), 1);
            }
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }
}
