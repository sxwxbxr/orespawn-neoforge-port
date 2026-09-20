package com.swbr.orespawn.entity.aquatic;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.ai.LegacyPanic;
import com.swbr.orespawn.entity.ai.MyEntityAIWander;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.critter.Cricket;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import com.swbr.orespawn.entity.insect.Firefly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.portal.EntityAnt;
import com.swbr.orespawn.entity.worm.WormSmall;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Frog} (Frog.java:14-269), id {@code frog}: a hopping frog that eats small critters.
 * A sneaking player with an empty hand kisses it into a prince (Boyfriend) or a princess (Girlfriend).
 *
 * <p>Same simple name as {@code net.minecraft.world.entity.animal.frog.Frog}; the vanilla class is never imported here.
 *
 * <p>Not carried over: {@code experienceValue = 5} (:28, ignored by {@code EntityAnimal.getExperiencePoints}, 1-3 from
 * {@code Animal}); the unused locals of {@code updateAITasks} (:217-219). {@code canBreatheUnderwater} (:49-51) is the
 * tag {@code minecraft:can_breathe_under_water}; {@code canTriggerWalking} (:199-201) is the default.
 */
public class Frog extends Animal {

    /** DataWatcher 20: {@code singing}, read by {@code ModelFrog} to open the mouth (:46, :57-63). */
    private static final EntityDataAccessor<Integer> DATA_SINGING =
            SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);

    /**
     * PORT: 1.7.10 checked a spawn position per spawn list - water creatures in water, ambient creatures on solid
     * ground - and the frog is in both lists (manifest: river/swampland as {@code waterCreature}, river, jungle and
     * swampland as {@code ambient}). 1.21.1 has one placement type per entity type, so the frog accepts either
     * position. The biome entries themselves come with W12.
     */
    public static final SpawnPlacementType WATER_OR_GROUND = (level, pos, entityType) ->
            SpawnPlacementTypes.IN_WATER.isSpawnPositionOk(level, pos, entityType)
                    || SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, entityType);

    private final GenericTargetSorter TargetSorter;
    public double moveSpeed = 0.10000000149011612;
    private int singing = 0;
    private int jumpcount = 0;

    /**
     * Constructor (:21-34). {@code setSize(0.75f, 0.75f)} is the entity type's size. The tasks are
     * {@link #registerGoals()}.
     */
    public Frog(final EntityType<? extends Frog> type, final Level level) {
        super(type, level);
        this.moveSpeed = 0.10000000149011612;
        this.singing = 0;
        this.jumpcount = 0;
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: getNavigator().setAvoidsWater(false) (:30) is a water path malus of 0 (vanilla default 8).
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0.0f);
    }

    /** {@code applyEntityAttributes} (:36-42): health 8, speed {@code moveSpeed} 0.1, attack 0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /**
     * The tasks of the constructor (:31-33): 0 swimming, 1 panic 1.4, 2 wander 1.0.
     *
     * <p>Panic is the 1.7.10 {@code EntityAIPanic} ({@link LegacyPanic#legacyPanic}): revenge target or burning,
     * then a random spot, never water (W07 catch-up: the former inline goal kept 1.21.1's water search).
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, LegacyPanic.legacyPanic(this, 1.4));
        this.goalSelector.addGoal(2, new MyEntityAIWander(this, 1.0f));
    }

    /** {@code entityInit} (:44-47). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SINGING, 0);
    }

    /** {@code canDespawn} (:53-55): persistence is checked by {@code Mob.checkDespawn}; 1.21.1 Animal says no by default. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return true;
    }

    /** {@code getSinging} (:57-59). */
    public int getSinging() {
        return this.entityData.get(DATA_SINGING);
    }

    /** {@code setSinging} (:61-63). */
    public void setSinging(final int par1) {
        this.entityData.set(DATA_SINGING, par1);
    }

    /**
     * {@code jumpAround} (:65-73): up by 0.75-1.3, forward by 0.7-1.45 along the body yaw, on the world random.
     *
     * <p>PORT: {@code this.posY += 0.35} is not applied. 1.7.10 {@code Entity.moveEntity} rebuilt {@code posY} from the
     * bounding box at the end of every move, and this line never touched the box, so the next tick's move discarded it
     * (only one position packet carried it). In 1.21.1 the position is authoritative; a {@code setPos} would lift the
     * frog 0.35 blocks for good. {@code isAirBorne = true} is {@code hasImpulse}.
     */
    private void jumpAround() {
        final Vec3 motion = this.getDeltaMovement();
        final double motionY = motion.y + (0.75f + Math.abs(this.level().random.nextFloat() * 0.55f));
        final float f = 0.7f + Math.abs(this.level().random.nextFloat() * 0.75f);
        final float d = (float) Math.toRadians(this.getYRot());
        final double motionX = motion.x - f * Math.sin(d);
        final double motionZ = motion.z + f * Math.cos(d);
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
    }

    /**
     * {@code onUpdate} (:75-93): speed attribute first, then on the server the singing countdown and a random hop with
     * 1/70 once the 50-tick cooldown is over.
     */
    @Override
    public void tick() {
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.moveSpeed);
        super.tick();
        if (!this.level().isClientSide) {
            if (this.singing != 0) {
                --this.singing;
                if (this.singing <= 0) {
                    this.setSinging(0);
                }
            }
            if (this.jumpcount > 0) {
                --this.jumpcount;
            }
            if (this.jumpcount == 0 && this.level().random.nextInt(70) == 1) {
                this.jumpAround();
                this.jumpcount = 50;
            }
        }
    }

    /**
     * {@code interact} (:95-125): a sneaking player with nothing in hand kisses the frog. It vanishes on both sides
     * with an explosion sound at the player; the server spawns a Boyfriend as prince 1 or 2, or a Girlfriend as
     * princess 1 or 2, the client shows 16 puffs each of smoke, explosion and red dust. Always {@code false}.
     *
     * <p>PORT: 1.21.1 asks the main hand and then the off hand; 1.7.10 knew only the held item. Only the main-hand call
     * acts, so the kiss cannot run twice. {@code playSoundAtEntity} on the client was a no-op and on the server reached
     * every player nearby; {@code Level.playSound(null, ...)} does the same on each side. {@code "random.explode"} is
     * {@code entity.generic.explode}; {@code "explode"} particles are {@code poof}, {@code "reddust"} with zero
     * velocity is the default red dust.
     */
    @Override
    public InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (par1EntityPlayer != null && par1EntityPlayer.isShiftKeyDown() && par1EntityPlayer.getInventory().getSelected().isEmpty()) {
            final Level world = par1EntityPlayer.level();
            this.discard();
            world.playSound(null, par1EntityPlayer.getX(), par1EntityPlayer.getY(), par1EntityPlayer.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, world.random.nextFloat() * 0.2f + 0.9f);
            if (!world.isClientSide) {
                if (world.random.nextInt(2) == 0) {
                    Boyfriend ent = null;
                    ent = (Boyfriend) spawnCreature(world, ModEntities.BOYFRIEND.get(), this.getX(), this.getY() + 0.01, this.getZ());
                    if (ent != null) {
                        ent.setPrince(1 + world.random.nextInt(2));
                    }
                } else {
                    Girlfriend ent2 = null;
                    ent2 = (Girlfriend) spawnCreature(world, ModEntities.GIRLFRIEND.get(), this.getX(), this.getY() + 0.01, this.getZ());
                    if (ent2 != null) {
                        ent2.setPrincess(1 + world.random.nextInt(2));
                    }
                }
            } else {
                for (int var3 = 0; var3 < 16; ++var3) {
                    world.addParticle(ParticleTypes.SMOKE,
                            (double) ((float) this.getX() + world.random.nextFloat() - world.random.nextFloat()),
                            (double) ((float) this.getY() + world.random.nextFloat()),
                            (double) ((float) this.getZ() + world.random.nextFloat() - world.random.nextFloat()), 0.0, 0.0, 0.0);
                    world.addParticle(ParticleTypes.POOF,
                            (double) ((float) this.getX() + world.random.nextFloat() - world.random.nextFloat()),
                            (double) ((float) this.getY() + world.random.nextFloat()),
                            (double) ((float) this.getZ() + world.random.nextFloat() - world.random.nextFloat()), 0.0, 0.0, 0.0);
                    world.addParticle(DustParticleOptions.REDSTONE,
                            (double) ((float) this.getX() + world.random.nextFloat() - world.random.nextFloat()),
                            (double) ((float) this.getY() + world.random.nextFloat()),
                            (double) ((float) this.getZ() + world.random.nextFloat() - world.random.nextFloat()), 0.0, 0.0, 0.0);
                }
            }
        }
        return InteractionResult.PASS;
    }

    /** {@code mygetMaxHealth} (:131-133). */
    public int mygetMaxHealth() {
        return 8;
    }

    /**
     * {@code getLivingSound} (:135-143): on the server half the calls are silent; the other half start the 35-tick
     * singing that opens the mouth, and return {@code "orespawn:frog"}.
     *
     * <p>That event does not exist: the 20.3 jar ships {@code frog1.ogg} and {@code frog2.ogg}, but its
     * {@code sounds.json} has no {@code frog} entry, and 1.7.10 resolved sounds only through {@code sounds.json}. The
     * croak was silent in the original and stays silent; the singing side effect is kept.
     */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (!this.level().isClientSide) {
            if (this.level().random.nextInt(2) == 0) {
                return null;
            }
            this.setSinging(this.singing = 35);
        }
        return null;
    }

    /** {@code getHurtSound} (:145-147). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.SCORPION_HIT.get();
    }

    /** {@code getDeathSound} (:149-151). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BIG_SPLAT.get();
    }

    /** {@code getSoundVolume} (:153-155). */
    @Override
    protected float getSoundVolume() {
        return 0.7f;
    }

    /** {@code fall} and {@code updateFallState} (:157-161), both empty: no fall distance, no fall damage. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code playStepSound} (:163-164), empty. */
    @Override
    protected void playStepSound(final BlockPos pos, final BlockState state) {
    }

    /**
     * {@code dropItemRand} (:166-169): a fresh stack up to one block off on the shared {@code OreSpawnRand}, one block
     * up, added straight to the level (no pickup delay, not part of the captured drops).
     */
    private void dropItemRand(final Item index, final int par1) {
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems}, then {@code dropEquipment} ({@code Mob.dropCustomDeathLoot}).
     * The override below ignores both arguments, so looting is not looked up.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /** {@code dropFewItems} (:171-175): four slime balls. */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 0; i < 4; ++i) {
            this.dropItemRand(Items.SLIME_BALL, 1);
        }
    }

    /**
     * {@code attackEntityAsMob} (:177-183): 3.0 mob damage, and a heal of 1 when the target is dead.
     * {@code isDead} was set only when a living entity's 20-tick death animation ended ({@code onDeathUpdate}), and
     * 1.21.1 removes it at the same point, so {@code isRemoved()} keeps the original's reading: right after the bite
     * a freshly killed critter is not yet removed.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 3.0f);
        if (par1Entity.isRemoved()) {
            this.heal(1.0f);
        }
        return var4;
    }

    /** {@code attackEntityFrom} (:185-193): every hit on the server makes the frog hop, with a 25-tick cooldown. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        ret = super.hurt(par1DamageSource, par2);
        if (!this.level().isClientSide && this.jumpcount <= 0) {
            this.jumpAround();
            this.jumpcount = 25;
        }
        return ret;
    }

    /** {@code canSeeTarget} (:195-197), unused in the original: no block between a point 0.25 above the feet and the target. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.25, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /** {@code createChild} (:203-205). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /**
     * 1.7.10 {@code EntityAnimal.isBreedingItem} is wheat, but {@code interact} never reaches the animal's feeding code,
     * and {@link #mobInteract} does not either - nothing feeds a frog.
     */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** {@code findBuddies} (:207-210) around a spawn position, before the entity exists. */
    private static int findBuddies(final EntityType<?> type, final LevelAccessor level, final BlockPos pos) {
        final AABB box = type.getDimensions().makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                .inflate(20.0, 8.0, 20.0);
        return level.getEntitiesOfClass(Frog.class, box).size();
    }

    /**
     * {@code getCanSpawnHere} (:212-214) as the placement predicate: Y 50 or higher, daytime, in the Crystal dimension
     * only with 1/20, at most five frogs within 20/8/20. The chance is rolled here only; the instance checks answer
     * {@code true}, because the override dropped the {@code EntityLiving} collision and liquid test.
     */
    public static boolean checkFrogSpawnRules(final EntityType<Frog> type, final ServerLevelAccessor level,
                                              final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final ServerLevel world = level.getLevel();
        return pos.getY() >= 50.0 && InsectSupport.isDaytime(world)
                && (!world.dimension().equals(WorldProviderOreSpawn5.DIMENSION) || random.nextInt(20) == 1)
                && findBuddies(type, level, pos) <= 5;
    }

    /** See {@link #checkFrogSpawnRules}. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** See {@link #checkFrogSpawnRules}. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code updateAITasks} (:216-234): after the AI step, with 1/12 and not on Peaceful, walk to the nearest prey and
     * bite it when closer than {@code sqrt(6)}.
     *
     * <p>PORT: 1.7.10 ran this after the move helper of the same tick, 1.21.1 before the move control; the new path is
     * followed from this tick instead of the next.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.random.nextInt(12) == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.getNavigation().moveTo(e, 1.25);
                if (this.distanceToSqr(e) < 6.0) {
                    this.doHurtTarget(e);
                }
            }
        }
    }

    /**
     * {@code isSuitableTarget} (:236-238): alive, visible, and an ant (termites included), butterfly (moths included),
     * cricket, mosquito, firefly or small worm.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return this.level().getDifficulty() != Difficulty.PEACEFUL && par1EntityLiving != null && par1EntityLiving != this
                && par1EntityLiving.isAlive() && this.getSensing().hasLineOfSight(par1EntityLiving)
                && (par1EntityLiving instanceof EntityAnt || par1EntityLiving instanceof EntityButterfly
                        || par1EntityLiving instanceof Cricket || par1EntityLiving instanceof EntityMosquito
                        || par1EntityLiving instanceof Firefly || par1EntityLiving instanceof WormSmall);
    }

    /** {@code findSomethingToAttack} (:240-257): every living entity in {@code expand(8, 3, 8)}, nearest first. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0, 3.0, 8.0)));
        var5.sort(this.TargetSorter);
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8 = null;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * {@code spawnCreature} (:259-268): create, place with a random yaw, add and play the ambient sound.
     * PORT: the entity name of {@code EntityList.createEntityByName} is the {@link EntityType} here.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2, final double par4, final double par6) {
        Entity var8 = null;
        var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            // The original cast to EntityLiving unchecked; both callers spawn a Mob.
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
