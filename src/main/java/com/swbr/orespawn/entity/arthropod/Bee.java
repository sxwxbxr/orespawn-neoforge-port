package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.world.dimension.danger.WorldProviderOreSpawn4;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
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
 * Port of {@code danger.orespawn.Bee} (Bee.java:17-318): {@code bee}, the giant hostile bee that flies at players,
 * villagers and companions and stings with Poison (verhalten/entity-04.md). Registered as "Bee" 64/1/false,
 * {@code EntityMob} in {@code ambient} lists. It has no AI task at all: flight, target choice and sting are written by
 * hand in the AI step.
 *
 * <p>Values: health, attack and defense from {@link MobStats#Bee_stats()} (80/12/5), speed 0.32 once (:45), XP 25
 * (:36), {@code fireResistance} 5 (:38). No fall damage and no fall state (:220-224), still triggers pressure plates and
 * steps (:216-218, :226-228). Pushable, pushes nothing (:86-91). Vertical motion is damped by 0.6 every tick, and in
 * water it stings itself with 1/4 per tick (:125-131). DataWatcher 20 = attacking (faster abdomen), no NBT;
 * {@code currentFlightTarget} and {@code rt} are volatile.
 *
 * <p>Not carried over: {@code getDropItem} (:97-99) is dead because {@code dropFewItems} is overridden;
 * {@code initCreature} (:276-277) overrides nothing; {@code isAIEnabled} and {@code canDespawn} are the 1.21.1 defaults.
 */
public class Bee extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:51): the abdomen of {@code ModelBee} curls faster while it is 1. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(Bee.class, EntityDataSerializers.INT);

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    private int stuck_count;
    private int lastX;
    private int lastZ;
    /** The last living attacker (:24, :234); {@code EntityLivingBase} by every write. */
    @Nullable
    private Entity rt;

    /** {@code Bee(World)} (:26-40) with {@code applyEntityAttributes} (:42-47). */
    public Bee(final EntityType<? extends Bee> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.stuck_count = 0;
        this.lastX = 0;
        this.lastZ = 0;
        this.rt = null;
        // setSize(1.5f, 2.5f) (:34) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(false) (:35) is a water path malus of 0 (vanilla default 8, W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 25;
        // isImmuneToFire = false (:37) is the type default; fireResistance = 5 (:38) is getFireImmuneTicks.
        this.TargetSorter = new GenericTargetSorter(this);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3199999928474426);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.Bee_stats().attack());
        this.setHealth(this.getMaxHealth());
        // The forward push of the AI step must survive the move control (see InsectSupport.LegacyMoveControl, W05).
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:42-47): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3199999928474426)
                .add(Attributes.ATTACK_DAMAGE, 12.0);
    }

    /** {@code entityInit} (:49-52). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 5} (:38). */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /** {@code getAttacking} (:58-60). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:62-64). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getSoundVolume} (:66-68). */
    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    /** {@code getSoundPitch} (:70-72). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:74-76): {@code orespawn:Beebuzz}, registered lower-case. */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.BEEBUZZ.get();
    }

    /** {@code getHurtSound} (:78-80). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.DRAGONFLY_HURT.get();
    }

    /** {@code getDeathSound} (:82-84). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /**
     * {@code canBePushed} (:86-88): always {@code true}. 1.21.1's default also excludes spectators and climbing mobs,
     * and dead ones.
     */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:90-91): the bee pushes nothing. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:93-95). */
    public int mygetMaxHealth() {
        return MobStats.Bee_stats().health();
    }

    /** {@code dropItemRand} (:101-104): up to three blocks off on {@code OreSpawnRand}, one block up. */
    private void dropItemRand(final Item index, final int par1) {
        ArthropodSupport.dropItemRand(this, new ItemStack(index, par1), 4);
    }

    /**
     * {@code dropFewItems} (:106-119), then {@code dropEquipment}: 2-11 each of gold nuggets, butter candy, dandelions
     * ({@code yellow_flower}) and sugar. Looting and the recently-hit flag are ignored, as in the original.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        final RandomSource rand = this.level().random;
        for (int var4 = 2 + rand.nextInt(10), i = 0; i < var4; ++i) {
            this.dropItemRand(Items.GOLD_NUGGET, 1);
        }
        for (int var4 = 2 + rand.nextInt(10), i = 0; i < var4; ++i) {
            this.dropItemRand(ModItems.BUTTER_CANDY.get(), 1);
        }
        for (int var4 = 2 + rand.nextInt(10), i = 0; i < var4; ++i) {
            this.dropItemRand(Items.DANDELION, 1);
        }
        for (int var4 = 2 + rand.nextInt(10), i = 0; i < var4; ++i) {
            this.dropItemRand(Items.SUGAR, 1);
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code onUpdate} (:125-131): after the vanilla tick, vertical motion times 0.6; in water a self-sting with 1/4.
     *
     * <p>PORT (R18 case 4): the self-sting runs on the server only. 1.7.10 ran it on the client as well, where
     * {@code attackEntityFrom} returned early but the 1/3 Poison landed in the client's effect map; 1.21.1 never removes
     * a client-only effect, so it would stay on the bee in that client for good.
     */
    @Override
    public void tick() {
        super.tick();
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        if (!this.level().isClientSide && this.isInWater() && this.level().random.nextInt(4) == 1) {
            this.doHurtTarget(this);
        }
    }

    /**
     * {@code attackEntityAsMob} (:133-139): {@code Bee_stats.attack} as mob damage, no knockback or enchantments, and
     * Poison for 50 ticks with 1/3 on the world random.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), (float) MobStats.Bee_stats().attack());
        if (this.level().random.nextInt(3) == 1 && par1Entity != null) {
            // PORT (R18 case 1): the original cast to EntityLivingBase unchecked; every caller passes a living entity.
            if (par1Entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
            }
        }
        return var4;
    }

    /**
     * {@code canSeeTarget} (:141-143): no block between a point 0.75 above the feet and the target. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded - the outline shape
     * (Frog, GoldFish, W06).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:145-214): stuck detection, a new flight target when stuck, 1/300 or close, otherwise a 1/15
     * target check with a sting in reach; then steer towards the flight target. {@code (int)} casts are
     * {@code Mth.floor} (R20); {@code this.rand} is the entity random, {@code moveForward} is {@code zza}.
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
            // Block bid = Blocks.stone (:165): "not air yet".
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
                // PORT: isAir() also accepts cave and void air, which 1.7.10 generated as plain air.
                bidIsAir = bid.isAir();
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
                --keep_trying;
            }
        } else if (rand.nextInt(15) == 0) {
            LivingEntity e = null;
            e = (LivingEntity) this.rt;
            if (e != null && e.isRemoved()) {
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.setAttacking(1);
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 1, Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < 16.0) {
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
    }

    /** {@code fall(float)} (:220-221) is empty: no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /**
     * {@code updateFallState} (:223-224) is empty: the fall distance never grows, so neither {@code fall} nor the block's
     * {@code onFallenUpon} (farmland) ever runs, and there are no landing particles.
     */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code attackEntityFrom} (:230-238): a living attacker becomes {@code rt} and the flight target moves to it - on
     * the self-sting in water that is the bee itself.
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
     * Spawn predicate: {@code getCanSpawnHere} (:240-270) - always in the Islands dimension; otherwise air in x/z
     * -1..1, y +1..+4, Y at least 50 and daytime. No light test.
     *
     * <p>PORT: the "Bee" spawner scan (x/z -2..1, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9).
     */
    public static boolean checkBeeSpawnRules(final EntityType<Bee> type, final ServerLevelAccessor level,
                                             final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (level.getLevel().dimension().equals(WorldProviderOreSpawn4.DIMENSION)) {
            return true;
        }
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!ArthropodSupport.isAllAir(level, pos, -1, 2, 1, 5)) {
            return false;
        }
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(level.getLevel());
    }

    /** The override replaced {@code EntityCreature}'s path-weight test, which 1.21.1 asks here; the predicate holds the rule. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test, which 1.21.1 asks separately. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getTotalArmorValue} (:272-274), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Bee_stats().defense();
    }

    /**
     * {@code isSuitableTarget} (:279-300): alive, visible, not in water; a player unless creative; otherwise only a
     * villager, Girlfriend or Boyfriend.
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
        return par1EntityLiving instanceof Villager || par1EntityLiving instanceof Girlfriend || par1EntityLiving instanceof Boyfriend;
    }

    /** {@code findSomethingToAttack} (:302-317): {@code expand(10, 6, 10)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 10.0, 6.0, 10.0, t -> this.isSuitableTarget(t, false));
    }
}
