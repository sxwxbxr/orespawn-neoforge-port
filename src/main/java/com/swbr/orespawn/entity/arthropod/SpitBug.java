package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.ai.MyEntityAIWanderALot;
import com.swbr.orespawn.entity.herbivore.Hydrolisc;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.projectile.Acid;
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
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.SpitBug} (SpitBug.java:17-415): {@code spit_bug}, the swamp beetle that leaps at its
 * prey and spits streams of Acid, and the helper the Jumpy Bug calls in (verhalten/entity-12.md). Registered as
 * "Spit Bug" 64/1/false, {@code EntityMob} in an {@code ambient} list.
 *
 * <p>Values: health, attack and defense from {@link MobStats#SpitBug_stats()} (100/10/12), speed 0.33 re-set every tick
 * (:32, :78), XP 50 (:36), {@code fireResistance} 75 (:37), not fire immune (:38). After every accepted hit 15 ticks
 * without damage ({@code hurt_timer}, :218-223); cactus and fall damage are ignored. Heals 1 with 1/150 per AI tick.
 * While airborne the path is dropped (:80-82). DataWatcher 20 = attacking (jaws), no NBT; {@code hurt_timer} and
 * {@code stream_count} are not saved.
 *
 * <p>Not carried over: {@code force_sync} (:21, :30, :63) is never read; {@code getDropItem} (:162-174) is dead because
 * {@code dropFewItems} is overridden; {@code initCreature} (:192-193) overrides nothing; {@code interact} (:195-197) is
 * {@code Mob.mobInteract}'s {@code PASS}; {@code isAIEnabled}, {@code onLivingUpdate}, {@code canDespawn} are the 1.21.1
 * defaults.
 */
public class SpitBug extends Monster implements LegacyArmor {

    /** DataWatcher 20 (:51): jaws of {@code ModelSpitBug}; also set while spitting. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SpitBug.class, EntityDataSerializers.INT);

    private GenericTargetSorter TargetSorter;
    /** Client-side note pad (:52-62); {@code ModelSpitBug} does not use it. Never synchronised or saved. */
    private RenderInfo renderdata;
    private int hurt_timer;
    private float moveSpeed;
    private int stream_count;

    /** {@code SpitBug(World)} (:26-47) with {@code entityInit} (:49-64) and {@code applyEntityAttributes} (:66-71). */
    public SpitBug(final EntityType<? extends SpitBug> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.renderdata = new RenderInfo();
        this.hurt_timer = 0;
        this.moveSpeed = 0.33f;
        this.stream_count = 0;
        // setSize(2.0f, 2.0f) (:34) is the entity type's size (R9).
        // PORT: getNavigator().setAvoidsWater(true) (:35) is a water path malus of -1 (W06 precedent).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 50;
        // fireResistance = 75 (:37) is getFireImmuneTicks; isImmuneToFire = false (:38) is the type default.
        this.TargetSorter = new GenericTargetSorter(this);
        this.renderdata = new RenderInfo();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.SpitBug_stats().attack());
        this.setHealth(this.getMaxHealth());
        // PORT: goals are added here on both sides, as the 1.7.10 constructor did (Girlfriend, W04).
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // PORT: EntityAIMoveThroughVillage(0.9, false) walked to the doors of a 1.7.10 village; villages are POI-based in
        // 1.21.1, so the vanilla MoveThroughVillageGoal (not only at night, POI distance 4 as the zombie, no doors) stands in.
        this.goalSelector.addGoal(1, new MoveThroughVillageGoal(this, 0.8999999761581421, false, 4, () -> false));
        this.goalSelector.addGoal(2, new MyEntityAIWanderALot(this, 14, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWatchClosest(this, Player.class, 10.0f));
        this.goalSelector.addGoal(4, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops a target it has not seen
        // for 60 ticks (TargetGoal.mustSee is fixed to true there) - same note as Lizard (W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** {@code applyEntityAttributes} (:66-71): the attribute set; the constructor writes the config values. */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.33f)
                .add(Attributes.ATTACK_DAMAGE, 10.0);
    }

    /** {@code entityInit} (:49-51). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code fireResistance = 75} (:37). */
    @Override
    protected int getFireImmuneTicks() {
        return 75;
    }

    /** {@code onUpdate} (:77-83), both sides: while {@code isAirBorne} ({@code hasImpulse}) the path is dropped. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
        if (this.hasImpulse) {
            this.getNavigation().stop();
        }
    }

    /** {@code mygetMaxHealth} (:85-87). */
    public int mygetMaxHealth() {
        return MobStats.SpitBug_stats().health();
    }

    /** {@code getRenderInfo} (:89-91). */
    public RenderInfo getRenderInfo() {
        return this.renderdata;
    }

    /** {@code setRenderInfo} (:93-102). */
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

    /** {@code getTotalArmorValue} (:104-106), R5. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.SpitBug_stats().defense();
    }

    /**
     * {@code jump} (:116-123): no vanilla jump, only 0.75 upward and a forward shove of 0.2-0.65 along the head yaw on
     * the world random. {@code isAirBorne} is {@code hasImpulse}.
     *
     * <p>PORT: {@code this.posY += 0.75} is left out. 1.7.10 {@code Entity.moveEntity} rebuilt {@code posY} from the
     * unmoved bounding box later in the same {@code onLivingUpdate}, so the write never took effect (Frog, Cricket, W06).
     */
    @Override
    public void jumpFromGround() {
        final Vec3 m = this.getDeltaMovement();
        final double motionY = m.y + 0.75;
        final float f = 0.2f + Math.abs(this.level().random.nextFloat() * 0.45f);
        final double motionX = m.x - f * Math.sin(Math.toRadians(this.getYHeadRot()));
        final double motionZ = m.z + f * Math.cos(Math.toRadians(this.getYHeadRot()));
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
    }

    /** {@code jumpAtEntity} (:125-133): 0.75 upward and a shove of 0.2-0.45 towards the target; same PORT as {@link #jumpFromGround}. */
    protected void jumpAtEntity(final LivingEntity e) {
        final Vec3 m = this.getDeltaMovement();
        final double motionY = m.y + 0.75;
        final float f = 0.2f + Math.abs(this.level().random.nextFloat() * 0.25f);
        final float d = (float) Math.atan2(e.getX() - this.getX(), e.getZ() - this.getZ());
        final double motionX = m.x + f * Math.sin(d);
        final double motionZ = m.z + f * Math.cos(d);
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
    }

    /** {@code getSpitBugHealth} (:135-137). */
    public int getSpitBugHealth() {
        return (int) this.getHealth();
    }

    /** {@code getLivingSound} (:139-144): clatter with 1/4 on the entity random. */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(4) == 0) {
            return ModSounds.CLATTER.get();
        }
        return null;
    }

    /** {@code getHurtSound} (:146-148). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.CRUNCH.get();
    }

    /** {@code getDeathSound} (:150-152). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.EMPERORSCORPION_DEATH.get();
    }

    /** {@code getSoundVolume} (:154-156). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:158-160). */
    @Override
    public float getVoicePitch() {
        return 1.5f;
    }

    /** {@code dropItemRand} (:176-184): up to two blocks off on {@code OreSpawnRand}, one block up. */
    private void dropItemRand(final ItemStack is) {
        ArthropodSupport.dropItemRand(this, is, 3);
    }

    /** {@code dropFewItems} (:186-190), then {@code dropEquipment}: 1-3 amethysts. Looting is ignored. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        for (int i = 1 + this.level().random.nextInt(3), var4 = 0; var4 < i; ++var4) {
            this.dropItemRand(new ItemStack(ModItems.AMETHYST.get(), 1));
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code attackEntityAsMob} (:199-214): the vanilla mob hit, then a shove of 0.5 away and 0.1 up (0.2 against players
     * and removed targets).
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final double ks = 0.5;
        double inair = 0.1;
        if (super.doHurtTarget(par1Entity)) {
            if (par1Entity != null && par1Entity instanceof LivingEntity) {
                final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
                if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                    inair *= 2.0;
                }
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
            return true;
        }
        return false;
    }

    /**
     * {@code attackEntityFrom} (:216-233): refused while {@code hurt_timer} runs; cactus and fall are ignored; every other
     * hit restarts the 15-tick window, and a mob attacker ({@code EntityLiving}, which 1.7.10 players were not) becomes
     * the target at once and makes the call report {@code true} whatever the damage did.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS) && !par1DamageSource.is(DamageTypes.FALL)) {
            ret = super.hurt(par1DamageSource, par2);
            this.hurt_timer = 15;
            final Entity e = par1DamageSource.getEntity();
            if (e != null && e instanceof Mob) {
                this.setTarget((LivingEntity) e);
                // setTarget(e) (:227) wrote EntityCreature.entityToAttack, which only the pre-task AI read.
                this.getNavigation().moveTo(e, 1.2);
                ret = true;
            }
        }
        return ret;
    }

    /** {@code updateAITasks} (:235-281). */
    @Override
    protected void customServerAiStep() {
        LivingEntity e = null;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        final RandomSource rand = this.level().random;
        if (rand.nextInt(5) == 0) {
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget(null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack();
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (rand.nextInt(15) == 1 && !this.hasImpulse) {
                    this.jumpAtEntity(e);
                } else if (this.distanceToSqr(e) < 9.0) {
                    this.setAttacking(1);
                    if (rand.nextInt(6) == 0 || rand.nextInt(7) == 1) {
                        this.doHurtTarget(e);
                        if (!this.level().isClientSide) {
                            if (rand.nextInt(3) != 1) {
                                ArthropodSupport.playSoundAtEntity(e, ModSounds.CLATTER.get(), 1.0f, 1.0f);
                            }
                        }
                    }
                } else if (!this.hasImpulse) {
                    this.getNavigation().moveTo(e, 0.5);
                    this.watercanon(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        if (rand.nextInt(150) == 1 && this.getHealth() < this.mygetMaxHealth()) {
            this.heal(1.0f);
        }
    }

    /**
     * {@code watercanon} (:283-305): while shots are left, an {@link Acid} 1.5 above and 1.5 in front, aimed at the
     * target with speed 1.1 and spread 6; empty, a 1/7 reload to eight shots (entity random).
     *
     * <p>Original quirks kept (R18): the first constructor puts the acid at the distance vector instead of a position,
     * which the next line overwrites; the x offset follows the head yaw, the z offset the body yaw.
     */
    private void watercanon(final LivingEntity e) {
        final double yoff = 1.5;
        final double xzoff = 1.5;
        if (this.stream_count > 0) {
            this.setAttacking(1);
            final Acid var2 = new Acid(this.level(), e.getX() - this.getX(), e.getY() + 0.75 - (this.getY() + yoff), e.getZ() - this.getZ());
            var2.moveTo(this.getX() - xzoff * Math.sin(Math.toRadians(this.getYHeadRot())), this.getY() + yoff,
                    this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())), this.getYHeadRot(), this.getXRot());
            final double var3 = e.getX() - var2.getX();
            final double var4 = e.getY() + 0.25 - var2.getY();
            final double var5 = e.getZ() - var2.getZ();
            // MathHelper.sqrt_double is (float) Math.sqrt of the double.
            final float var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
            var2.setThrowableHeading(var3, var4 + var6, var5, 1.1f, 6.0f);
            ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(var2);
            --this.stream_count;
        } else {
            this.setAttacking(0);
        }
        if (this.stream_count <= 0 && this.getRandom().nextInt(7) == 1) {
            this.stream_count = 8;
        }
    }

    /** {@code isSuitableTarget} (:307-352), checks in the original order. */
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
        if (ArthropodSupport.isEnderReaper(par1EntityLiving)) {
            return false;
        }
        if (ArthropodSupport.isEnderKnight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof EnderMan) {
            return false;
        }
        if (par1EntityLiving instanceof Hydrolisc) {
            return false;
        }
        if (par1EntityLiving instanceof Creeper) {
            return false;
        }
        if (par1EntityLiving instanceof SpitBug) {
            return false;
        }
        if (par1EntityLiving instanceof TrooperBug) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:354-371): {@code expand(12, 7, 12)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        return ArthropodSupport.findSomethingToAttack(this, this.TargetSorter, 12.0, 7.0, 12.0, t -> this.isSuitableTarget(t, false));
    }

    /** {@code getAttacking} (:373-375). */
    public final int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:377-379). */
    public final void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /**
     * Spawn predicate: {@code getCanSpawnHere} (:381-414) - by day only when {@code rand(20) <= 1}, a valid light level,
     * and air in x/z -2..1, y +1..+3.
     *
     * <p>PORT: the "Spit Bug" spawner scan (x/z -3..2, y 0..4) is {@link MobSpawnType#SPAWNER} here (catalogue 5.9); the
     * day roll uses the spawner's random.
     */
    public static boolean checkSpitBugSpawnRules(final EntityType<SpitBug> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (InsectSupport.isDaytime(level.getLevel()) && random.nextInt(20) > 1) {
            return false;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        return ArthropodSupport.isAllAir(level, pos, -2, 2, 1, 4);
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
}
