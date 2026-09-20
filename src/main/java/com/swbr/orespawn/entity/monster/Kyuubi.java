package com.swbr.orespawn.entity.monster;

import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.config.stats.StatSource;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWander;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Kyuubi} (Kyuubi.java:14-225), id {@code kyuubi} ("Kyuubi", OreSpawnMain.java:3511,
 * tracking 64/1/false). A nine-tailed fire fox of the Nether that shoots small fireballs at non-monsters and hurts
 * itself in water (verhalten/entity-09.md).
 *
 * <p>Values: {@code Kyuubi_stats} (125/10/10), speed 0.25 re-set every tick (:22, :54), {@code experienceValue} 30
 * (:25), {@code fireResistance} 1000 (:26), fire immune (:27, the entity type's {@code fireImmune()}).
 * {@code getAttackStrength} = 3 (:86-88) is a dead 1.6 leftover. No DataWatcher entries (:38-40), no NBT.
 */
public class Kyuubi extends Monster implements LegacyArmor {

    private GenericTargetSorter TargetSorter;
    private float moveSpeed;

    /** {@code Kyuubi(World)} (:19-36). {@code setSize(0.5f, 1.25f)} (:23) is the entity type's size (R9). */
    public Kyuubi(final EntityType<? extends Kyuubi> type, final Level par1World) {
        super(type, par1World);
        this.TargetSorter = null;
        this.moveSpeed = 0.25f;
        // PORT: getNavigator().setAvoidsWater(true) (:24) - water malus -1 (Chipmunk, W06).
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.xpReward = 30;
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // EntityAIPanic: while a revenge target is set or it burns (LegacyPanic.legacyPanic). A fire immune entity
        // never counts as burning, in 1.7.10 (Entity.isBurning) as in 1.21.1 (isOnFire).
        this.goalSelector.addGoal(1, LegacyPanic.legacyPanic(this, 1.350000023841858));
        this.goalSelector.addGoal(2, MonsterSupport.legacyMoveThroughVillage(this, 1.0));
        this.goalSelector.addGoal(3, new EntityAIWander(this, 1.0));
        this.goalSelector.addGoal(4, new EntityAIWatchClosest(this, Player.class, 10.0f));
        this.goalSelector.addGoal(5, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight (Lizard, W06).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.TargetSorter = new GenericTargetSorter(this);
        final MobStats stats = MobStats.Kyuubi_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) this.mygetMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /** {@code applyEntityAttributes} (:42-47) at registration time (R3), armor from {@code getTotalArmorValue} (:62-64). */
    public static AttributeSupplier.Builder createAttributes() {
        final MobStats stats = MobStats.Kyuubi_stats(StatSource.EARLY);
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, (double) stats.health())
                .add(Attributes.MOVEMENT_SPEED, (double) 0.25f)
                .add(Attributes.ATTACK_DAMAGE, (double) stats.attack())
                .add(Attributes.ARMOR, (double) stats.defense());
    }

    /** {@code fireResistance = 1000} (:26). */
    @Override
    protected int getFireImmuneTicks() {
        return 1000;
    }

    /** {@code canDespawn} (:49-51). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !this.isPersistenceRequired();
    }

    /** {@code onUpdate} (:53-56), both sides. */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        super.tick();
    }

    /** {@code mygetMaxHealth} (:58-60). */
    public int mygetMaxHealth() {
        return MobStats.Kyuubi_stats().health();
    }

    /** {@code getTotalArmorValue} (:62-64) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.Kyuubi_stats().defense();
    }

    /**
     * {@code updateAITasks} (:131-152) ran {@code setRevengeTarget(null)} with 1 in 200 <em>before</em>
     * {@code super.updateAITasks()}, i.e. before the target and goal selectors. 1.21.1's {@code serverAiStep} is final
     * and calls {@code customServerAiStep} after the selectors, so the roll runs here at the head of {@code aiStep},
     * under the same conditions as {@code serverAiStep} (server AI, not dead). PORT: it now precedes the lerp and
     * motion damping of the tick instead of following them; only the order of world-random draws within the tick moves.
     *
     * <p>Then {@code onLivingUpdate} (:70-84), both sides: 1 in 10 red dust and lava particles at y+2 and
     * {@code setFire(5)}; in water additionally {@code attackEntityAsMob(this)} - the Kyuubi hits itself with its own
     * attack value - and smoke. {@code World.spawnParticle} did nothing on the server, so the particles are client-side.
     *
     * <p>{@code setFire(5)} → {@code igniteForSeconds(5)}: without visible effect, because a fire immune entity is never
     * "burning" (1.7.10 {@code isBurning}, 1.21.1 {@code isOnFire}); the fire ticks still count down as in 1.7.10.
     * {@code "reddust"} with zero velocity was the default red dust, {@link DustParticleOptions#REDSTONE} (W02).
     */
    @Override
    public void aiStep() {
        if (!this.isImmobile() && this.isEffectiveAi() && !this.isRemoved()) {
            if (this.level().random.nextInt(200) == 1) {
                this.setLastHurtByMob((LivingEntity) null);
            }
        }
        super.aiStep();
        if (this.level().random.nextInt(10) == 1) {
            if (this.level().isClientSide) {
                this.level().addParticle(DustParticleOptions.REDSTONE, this.getX(), this.getY() + 2.0, this.getZ(), 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY() + 2.0, this.getZ(), 0.0, 0.0, 0.0);
            }
            this.igniteForSeconds(5.0f);
            if (this.isInWater()) {
                // PORT: Mob.doHurtTarget(this) is the 1.7.10 EntityMob.attackEntityAsMob(this): mob damage of the own
                // ATTACK_DAMAGE with enchantment and knockback bonuses (none). On the client the hurt call returns false,
                // as attackEntityFrom did there.
                this.doHurtTarget(this);
                if (this.level().isClientSide) {
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.75, this.getZ(), 0.0, 0.0, 0.0);
                    this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.75, this.getZ(), 0.0, 0.0, 0.0);
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 2.0, this.getZ(), 0.0, 0.0, 0.0);
                    this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.0, this.getZ(), 0.0, 0.0, 0.0);
                }
            }
        }
    }

    /** {@code getAttackStrength} (:86-88): dead in 1.7.10, kept for the mapping. */
    public int getAttackStrength(final LivingEntity par1Entity) {
        return 3;
    }

    /** {@code getLivingSound} (:90-92). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KYUUBI_LIVING.get();
    }

    /** {@code getHurtSound} (:94-96). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.ALO_HURT.get();
    }

    /** {@code getDeathSound} (:98-100). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALO_DEATH.get();
    }

    /** {@code getSoundVolume} (:102-104). */
    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    /** {@code getSoundPitch} (:106-108). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /**
     * {@code getDropItem} (:110-122): gold, uranium or titanium nugget, or nothing. Dead code: {@code dropFewItems} is
     * overridden and never asks it. Kept for the mapping; nothing calls it.
     */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(6);
        if (i == 0) {
            return Items.GOLD_NUGGET;
        }
        if (i == 1) {
            return ModItems.URANIUM_NUGGET.get();
        }
        if (i == 2) {
            return ModItems.TITANIUM_NUGGET.get();
        }
        return null;
    }

    // initCreature (:124-125) overrode nothing; interact (:127-129) false is the default.

    /**
     * {@code updateAITasks} (:131-152) after the selectors (the revenge roll is at the head of {@link #aiStep}): 1 in 10
     * look for a non-monster, face it and walk to it at 1.25; within squared 64 with (1/6 or 1/8) shoot a small fireball
     * from y+1.25 at the target's y+0.75 with the bow sound.
     *
     * <p>PORT: the 1.7.10 {@code EntityFireball} constructor added {@code nextGaussian() * 0.4} to each acceleration
     * component on the fireball's own random, normalised it and scaled by 0.1. 1.21.1's {@link SmallFireball} takes a
     * direction and applies no spread (the blaze adds its own), so the gaussian spread is drawn here from the Kyuubi's
     * random. The flight then follows the 1.21.1 hurting-projectile physics (acceleration along the current motion
     * instead of a fixed acceleration vector); damage and entity fire are the vanilla small fireball in both versions.
     *
     * <p>PORT: the 1.7.10 {@code EntitySmallFireball.onImpact} lit any air block it hit without a mobGriefing check;
     * 1.21.1 skips that for a {@code Mob} owner when griefing is denied. {@link #shootFireball} re-lights the block
     * after the vanilla hit, so mobGriefing=false no longer stops it. A fireball reloaded from disk is a plain vanilla
     * one again (the anonymous subclass is not serialised) and follows the 1.21.1 rule.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.level().random.nextInt(10) == 1) {
            final LivingEntity e = this.findSomethingToAttack();
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                this.getNavigation().moveTo(e, 1.25);
                if (this.distanceToSqr(e) < 64.0 && (this.random.nextInt(6) == 0 || this.random.nextInt(8) == 1)) {
                    final double ax = e.getX() - this.getX() + this.random.nextGaussian() * 0.4;
                    final double ay = e.getY() + 0.75 - (this.getY() + 1.25) + this.random.nextGaussian() * 0.4;
                    final double az = e.getZ() - this.getZ() + this.random.nextGaussian() * 0.4;
                    final SmallFireball var2 = shootFireball(this.level(), this, new Vec3(ax, ay, az));
                    var2.moveTo(this.getX(), this.getY() + 1.25, this.getZ(), this.getYRot(), this.getXRot());
                    this.playSound(SoundEvents.ARROW_SHOOT, 0.75f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                    this.level().addFreshEntity(var2);
                }
            }
        }
    }

    /** A vanilla small fireball whose block hit lights air without the 1.21.1 mobGriefing gate (1.7.10 onImpact). */
    private static SmallFireball shootFireball(final Level level, final LivingEntity owner, final Vec3 movement) {
        return new SmallFireball(level, owner, movement) {
            @Override
            protected void onHitBlock(final BlockHitResult result) {
                super.onHitBlock(result);
                if (!this.level().isClientSide) {
                    final BlockPos blockpos = result.getBlockPos().relative(result.getDirection());
                    if (this.level().isEmptyBlock(blockpos)) {
                        this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
                    }
                }
            }
        };
    }

    /**
     * {@code isSuitableTarget} (:154-184): living, not ignoreable, visible, no monster, no zombie pigman, no creative
     * player - players, animals, villagers and the mod's non-monsters.
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
        if (par1EntityLiving instanceof Monster) {
            return false;
        }
        // EntityPigZombie is a ZombifiedPiglin; being a Monster it never gets here, in 1.7.10 as now.
        if (par1EntityLiving instanceof ZombifiedPiglin) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            if (p.getAbilities().instabuild) {
                return false;
            }
        }
        return true;
    }

    /** {@code findSomethingToAttack} (:186-203): the first suitable entity in {@code expand(12, 4, 12)}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12.0, 4.0, 12.0));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** Spawn predicate: {@code getCanSpawnHere} (:205-207) is always {@code true} - no light test, no spawner scan. */
    public static boolean checkKyuubiSpawnRules(final EntityType<Kyuubi> type, final ServerLevelAccessor level,
                                                final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }

    /** {@code getCanSpawnHere} (:205-207). */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code dropFewItems} first, then the equipment roll (R10). */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, HerbivoreSupport.lootingLevel(level, damageSource));
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:214-224): 10 coal, 3 redstone blocks, 4 quartz blocks, spread ±3, y+1. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int var4 = 0; var4 < 10; ++var4) {
            MonsterSupport.dropItemRand(this, Items.COAL, 1, 4, 1.0);
        }
        for (int var4 = 0; var4 < 3; ++var4) {
            MonsterSupport.dropItemRand(this, Items.REDSTONE_BLOCK, 1, 4, 1.0);
        }
        for (int var4 = 0; var4 < 4; ++var4) {
            MonsterSupport.dropItemRand(this, Items.QUARTZ_BLOCK, 1, 4, 1.0);
        }
    }
}
