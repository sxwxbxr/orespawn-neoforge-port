package com.swbr.orespawn.entity.moth;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.arthropod.Bee;
import com.swbr.orespawn.entity.dino.Cryolophosaurus;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.AttackableNonMob;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Mothra} (Mothra.java:17-512), id {@code mothra} ("Mothra", OreSpawnMain.java:3311,
 * tracking 128/1/false). The flying boss moth: a ten times scaled butterfly that circles high above the ground, shoots
 * fireballs at players and anything else alive, and drops twenty moths when it dies (verhalten/entity-10.md).
 *
 * <p>It is an {@link EntityButterfly} ({@code implements IMob}), and everything of the butterfly is inherited 1:1 (R18):
 * {@code butterfly_type} (DataWatcher 20, NBT {@code ButterflyType}), the butterfly's own flight step with its private
 * target and the vampire bite in the Islands dimension for type 1 (run first, then overwritten by Mothra's own steering),
 * {@code motionY *= 0.6} before Mothra damps once more, and the Chaos teleport on an empty-hand right click. The markers
 * come along: {@code Ignoreable} and {@code NoStepTrigger} from the butterfly (Mothra.java:260-262 returns {@code false}
 * as well), {@link AttackableNonMob} of its own (MyUtils.isAttackableNonMob).
 *
 * <p>Values: {@code Mothra_stats} (150/12/8) from {@link MobStats}, speed 0.35 (:39, :52), {@code experienceValue} 100
 * (:42) - {@code EntityAmbientCreature} is an {@code EntityLiving}, not an {@code EntityAnimal}, so the 1.7.10 XP is
 * {@code experienceValue} (W04 lesson checked). {@code isImmuneToFire} (:43) is the type's {@code fireImmune()},
 * {@code fireResistance} 500 (:44) {@link #getFireImmuneTicks}. {@code setSize(5.0f, 2.0f)} (:40) is the type's size.
 * No own NBT (:290-298), no own DataWatcher (:61-64); {@code currentFlightTarget} is volatile.
 *
 * <p>Not carried over: {@code canDespawn} (:56-59) is the butterfly's (1.21.1 default), {@code onLivingUpdate} (:66-68),
 * {@code isAIEnabled} (:121-124) and {@code initCreature} (:338-340) change nothing; {@code getMothraHealth} (:74-76) is
 * read by no class of 20.2 but kept for the overlay GUI.
 */
public class Mothra extends EntityButterfly implements Enemy, AttackableNonMob, LegacyArmor {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private int lastX;
    private int lastZ;
    private int lastY;
    private int stuck_count;
    private int wing_sound;
    private int health_ticker;
    private GenericTargetSorter TargetSorter;
    private float moveSpeed;

    /** {@code Mothra(World)} (:29-46), after the butterfly constructor (which rolls {@code butterfly_type}). */
    public Mothra(final EntityType<? extends Mothra> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.lastX = 0;
        this.lastZ = 0;
        this.lastY = 0;
        this.stuck_count = 0;
        this.wing_sound = 0;
        this.health_ticker = 100;
        this.TargetSorter = null;
        this.moveSpeed = 0.35f;
        // getNavigator().setAvoidsWater(true) (:41), as in the butterfly.
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 100;
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Mothra_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:48-54) at registration time (R3); armor from {@code getTotalArmorValue} (:70-72). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Mothra_stats(StatSource.EARLY);
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code EntityButterfly.getTexture}, arm {@code instanceof Mothra} (EntityButterfly.java:55-57): {@code eyemoth.png}. */
    @Override
    public ResourceLocation getTexture() {
        return texture5;
    }

    /** {@code RenderButterfly.shouldRenderPass}: Mothra always gets the creeper-armor pass. */
    @Override
    public boolean hasGlowPass() {
        return true;
    }

    /** {@code fireResistance = 500} (:44). */
    @Override
    protected int getFireImmuneTicks() {
        return 500;
    }

    /** {@code getTotalArmorValue} (:70-72) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Mothra_stats().defense();
    }

    /** {@code getMothraHealth} (:74-76). */
    public int getMothraHealth() {
        return (int) this.getHealth();
    }

    /** {@code getSoundVolume} (:78-81). */
    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    /** {@code getSoundPitch} (:83-86). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:88-91). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:93-96). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:98-101): {@code "random.explode"}. */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_EXPLODE.value();
    }

    /** {@code mygetMaxHealth} (:116-119). */
    @Override
    public int mygetMaxHealth() {
        return MobStats.Mothra_stats().health();
    }

    /**
     * {@code onUpdate} (:126-144), both sides: the butterfly tick (which damps {@code motionY} by 0.6 already), then 0.6
     * again; every 31st tick the wing sound on the server; {@code health_ticker} starts at 100, then +1 health every 200
     * ticks while below {@code mygetMaxHealth}.
     *
     * <p>PORT: {@code "orespawn:MothraWings"} is {@link ModSounds#MOTHRA_WINGS} ({@code mothrawings}, R2).
     */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        ++this.wing_sound;
        if (this.wing_sound > 30) {
            if (!this.level().isClientSide) {
                ArthropodSupport.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 1.0f, 1.0f);
            }
            this.wing_sound = 0;
        }
        --this.health_ticker;
        if (this.health_ticker <= 0) {
            if (this.getHealth() < this.mygetMaxHealth()) {
                this.heal(1.0f);
            }
            this.health_ticker = 200;
        }
    }

    /**
     * {@code canSeeTarget} (:146-148): no block between a point 0.75 above the feet and the target. 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded - the outline shape
     * (Bee, GoldFish).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:150-257): the butterfly step first (:159), then stuck detection; a new flight target when
     * stuck for more than 50 ticks, with 1/300 or when within 3 blocks - lowered when the ground is more than 10 blocks
     * below; otherwise with 1/10 (not Peaceful, {@code MothraPeaceful == 0}) the nearest visible survival player within
     * 25/20/25, or with 1/3 any suitable target, and a shot with 1/{@code shoot}. Then steer: ×0.30001 horizontally,
     * ×0.20001 vertically, a quarter of the yaw error.
     *
     * <p>{@code (int)} casts on coordinates are {@link Mth#floor} (R20); {@code this.rand} is the entity random,
     * {@code worldObj.rand} the level's, {@code moveForward} is {@code zza}.
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        int shoot = 3;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.lastX == Mth.floor(this.getX()) && this.lastY == Mth.floor(this.getY()) && this.lastZ == Mth.floor(this.getZ())) {
            ++this.stuck_count;
        } else {
            this.stuck_count = 0;
            this.lastX = Mth.floor(this.getX());
            this.lastY = Mth.floor(this.getY());
            this.lastZ = Mth.floor(this.getZ());
        }
        if (this.level().getDifficulty() == Difficulty.HARD) {
            shoot = 2;
        }
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.stuck_count > 50 || this.level().random.nextInt(300) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 9.0f) {
            int down = 0;
            int dist = 20;
            for (int i = -5; i <= 5; i += 5) {
                for (int j = -5; j <= 5; j += 5) {
                    int k = 1;
                    while (k < 20) {
                        if (!MothSupport.isAir(this.level(), Mth.floor(this.getX()) + j, Mth.floor(this.getY()) - k, Mth.floor(this.getZ()) + i)) {
                            if (k < dist) {
                                dist = k;
                                break;
                            }
                            break;
                        } else {
                            ++k;
                        }
                    }
                }
            }
            if (dist > 10) {
                down = dist - 10 + 1;
            }
            // Block bid = Blocks.stone (:199): "not air yet".
            for (boolean bidIsAir = false; !bidIsAir && keep_trying != 0; --keep_trying) {
                xdir = 1;
                zdir = 1;
                if (this.random.nextInt(2) == 0) {
                    xdir = -1;
                }
                if (this.random.nextInt(2) == 0) {
                    zdir = -1;
                }
                int newz = this.random.nextInt(20) + 8;
                newz *= zdir;
                int newx = this.random.nextInt(20) + 8;
                newx *= xdir;
                this.currentFlightTarget.set(Mth.floor(this.getX()) + newx, Mth.floor(this.getY()) + this.random.nextInt(7) - 1 - down,
                        Mth.floor(this.getZ()) + newz);
                bidIsAir = MothSupport.isAir(this.level(), this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                        this.currentFlightTarget.getZ());
                if (bidIsAir && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(), this.currentFlightTarget.getZ())) {
                    bidIsAir = false;
                }
            }
            this.stuck_count = 0;
        } else if (this.level().random.nextInt(10) == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL
                && OreSpawnConfig.MOBS.MothraPeaceful.get() == 0) {
            Player target = null;
            target = MothSupport.findNearestPlayer(this, 25.0, 20.0, 25.0);
            if (target != null) {
                if (!target.getAbilities().instabuild) {
                    if (this.getSensing().hasLineOfSight(target)) {
                        this.currentFlightTarget.set(Mth.floor(target.getX()), Mth.floor(target.getY()) + 4, Mth.floor(target.getZ()));
                        if (this.random.nextInt(shoot) == 0) {
                            this.attackWithSomething(target);
                        }
                    }
                } else {
                    target = null;
                }
            }
            if (target == null && this.level().random.nextInt(3) == 0) {
                LivingEntity e = null;
                e = this.findSomethingToAttack();
                if (e != null) {
                    this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 5, Mth.floor(e.getZ()));
                    if (this.level().random.nextInt(shoot) == 0) {
                        this.attackWithSomething(e);
                    }
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.5 - m.x) * 0.30001;
        final double motionY = m.y + (Math.signum(var2) * 0.7 - m.y) * 0.20001;
        final double motionZ = m.z + (Math.signum(var3) * 0.5 - m.z) * 0.30001;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 1.0f;
        this.setYRot(this.getYRot() + var5 / 4.0f);
    }

    /**
     * {@code attackEntityFrom} (:277-288): damage from another Mothra is refused; otherwise the hit, and any source entity
     * becomes the flight target, two blocks above it.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Mothra) {
            return false;
        }
        ret = super.hurt(par1DamageSource, par2);
        if (e != null && this.currentFlightTarget != null) {
            this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY()) + 2, Mth.floor(e.getZ()));
        }
        return ret;
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:300-336). A "Mothra" spawner allows it; otherwise Y at least 70, night,
     * air in x -3..2, z -4..3, y +1..+9, and no other Mothra within {@code expand(64, 32, 64)}.
     *
     * <p>PORT: the spawner scan (x/z -2..2, y +1..+3) is {@link MobSpawnType#SPAWNER} (catalogue 5.9). The entity is not
     * there yet, so its bounding box is the type's spawn box at the block centre and "other" is every Mothra.
     */
    public static boolean checkMothraSpawnRules(final EntityType<Mothra> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (pos.getY() < 70.0) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        for (int k = -4; k < 4; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 1; i < 10; ++i) {
                    if (!MothSupport.isAir(level, pos.getX() + j, pos.getY() + i, pos.getZ() + k)) {
                        return false;
                    }
                }
            }
        }
        return level.getEntitiesOfClass(Mothra.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(64.0, 32.0, 64.0)).isEmpty();
    }

    /**
     * The whole {@code getCanSpawnHere} is {@link #checkMothraSpawnRules}; the butterfly's instance rule (with its type-1
     * side effect) is overridden in the original and must not run.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** {@code dropFewItems} first (R10), then the equipment of {@code Mob}. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:347-368), each item spread ±7 at y+1 ({@code dropItemRand}, :342-345): an item frame, 20
     * {@code largeexplode} particles, 53 gold nuggets, 25 moth scales, 3 blaze rods, a nether star, then 20 moths at
     * +0.5/+1/+0.5 with their living sound ({@code spawnCreature}, :370-382).
     *
     * <p>PORT: the particles were spawned on the server, where 1.7.10 {@code World.spawnParticle} reaches no client - they
     * were never visible. Their three {@code rand.nextFloat()} draws each are kept so the entity random stays in step.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        ArthropodSupport.dropItemRand(this, new ItemStack(Items.ITEM_FRAME, 1), 8);
        for (int i = 0; i < 20; ++i) {
            this.random.nextFloat();
            this.random.nextFloat();
            this.random.nextFloat();
        }
        for (int var4 = 0; var4 < 53; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(Items.GOLD_NUGGET, 1), 8);
        }
        for (int var4 = 0; var4 < 25; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(ModItems.MOTH_SCALE.get(), 1), 8);
        }
        for (int var4 = 0; var4 < 3; ++var4) {
            ArthropodSupport.dropItemRand(this, new ItemStack(Items.BLAZE_ROD, 1), 8);
        }
        ArthropodSupport.dropItemRand(this, new ItemStack(Items.NETHER_STAR, 1), 8);
        for (int var4 = 0; var4 < 20; ++var4) {
            ItemSpawnEgg.spawnSomething(ModEntities.MOTH.get(), this.level(), this.getX() + 0.5, this.getY() + 1.0, this.getZ() + 0.5);
        }
    }

    /**
     * {@code attackWithSomething} (:384-430): nothing with {@code MothraPeaceful} or on Peaceful; otherwise the fireball
     * by difficulty ({@link MothSupport#fireAt}), then +1 health while below {@code mygetMaxHealth}.
     */
    private void attackWithSomething(final LivingEntity par1) {
        if (OreSpawnConfig.MOBS.MothraPeaceful.get() != 0) {
            return;
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        MothSupport.fireAt(this, par1);
        if (this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    /**
     * {@code isSuitableTarget} (:432-492): not on Peaceful; alive, not {@code isIgnoreable}, visible; never Mothra,
     * Brutalfly, Vortex, VelocityRaptor, Cryolophosaurus, TerribleTerror, LurkingTerror, CloudShark, Rotator, Bee or
     * Mantis; a player only out of creative mode; everything else alive is a target.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
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
        if (par1EntityLiving instanceof Mothra) {
            return false;
        }
        if (par1EntityLiving instanceof Brutalfly) {
            return false;
        }
        if (MothSupport.isType(par1EntityLiving, MothSupport.VORTEX)) {
            return false;
        }
        if (par1EntityLiving instanceof VelocityRaptor) {
            return false;
        }
        if (par1EntityLiving instanceof Cryolophosaurus) {
            return false;
        }
        if (MothSupport.isType(par1EntityLiving, MothSupport.TERRIBLE_TERROR)) {
            return false;
        }
        if (MothSupport.isType(par1EntityLiving, MothSupport.LURKING_TERROR)) {
            return false;
        }
        if (MothSupport.isType(par1EntityLiving, MothSupport.CLOUD_SHARK)) {
            return false;
        }
        if (MothSupport.isType(par1EntityLiving, MothSupport.ROTATOR)) {
            return false;
        }
        if (par1EntityLiving instanceof Bee) {
            return false;
        }
        if (MothSupport.isType(par1EntityLiving, MothSupport.MANTIS)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:494-511): nothing with {@code PlayNicely}; the first suitable entity in {@code expand(15, 20, 15)}, nearest first. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(15.0, 20.0, 15.0)));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }
}
