package com.swbr.orespawn.entity.boss.queen;

import com.swbr.orespawn.entity.boss.RoyalCeiling;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.arthropod.ArthropodSupport;
import com.swbr.orespawn.entity.boss.king.PurplePower;
import com.swbr.orespawn.entity.boss.king.TheKing;
import com.swbr.orespawn.entity.ghost.Ghost;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.TheQueen} (TheQueen.java:20-1076), id {@code the_queen} ("The Queen",
 * OreSpawnMain.java:3963, {@code registerModEntity(..., 128, 1, false)}), verhalten/entity-01.md "TheQueen".
 *
 * <p>The three-headed endgame boss: an {@code EntityMob} that flies through blocks ({@code noClip}) around its home,
 * neutral while {@code mood == 0} (terraforms, spawns butterflies and birds, follows a nearby King) and angry after any
 * hit (PurplePower salvos, fireballs, thunder bolts, stomps, and a heal block on its melee victim).
 *
 * <h2>Values</h2>
 * Health {@code TheQueen_health} (6000) as virtual health (R4), speed 0.62, {@code attackDamage} attribute
 * {@code TheQueen_attack}; the melee base {@link #attdam} is 250 from the field initialiser, which ran after
 * {@code applyEntityAttributes} had set it to the config value (:48 after :87, R3), and grows with lost health up to
 * {@code attack * 1000} (:209-220). Armor from {@link #getTotalArmorValue()} (R5). XP 25000 ({@code EntityMob} returns
 * {@code experienceValue}, not the animal 1-3 of W04). Fire resistance 5000 ticks, fire immune (type), no fall damage,
 * not pushable, lightning has no effect.
 *
 * <h2>Where the original steps run</h2>
 * {@code updateAITasks} (:294-710) is {@link #customServerAiStep()}: in 1.7.10 it ran after {@code super.updateAITasks}
 * had cleared the senses and ticked tasks, navigator and helpers; 1.21.1 runs sensing, goals and navigation before it
 * and the move/look/jump controls after it. The motion written here is applied by the vanilla travel step of the same
 * tick ({@code moveEntityWithHeading} in 1.7.10), {@code onUpdate}'s tail (:200-228) is {@link #tick()} after
 * {@code super.tick()}.
 *
 * <p>PORT: 1.7.10's {@code EntityMoveHelper} zeroed {@code moveForward} inside {@code super.updateAITasks}, before
 * this class set it to 0.75; 1.21.1's {@code MoveControl} zeroes {@code zza} after {@code customServerAiStep}. The
 * Queen has no task that moves her, so {@link IdleMoveControl} skips that write and the 0.75 reaches travel like in
 * the original.
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>Armor: the +3 and +5 branches of {@code getTotalArmorValue} are unreachable behind the +2 branch.</li>
 *   <li>Explosions heal her by half the uncapped damage, and are refused.</li>
 *   <li>Any accepted call of {@code attackEntityFrom} makes her angry, a cactus prick or a PurplePower too.</li>
 *   <li>A {@code TheKing} found while happy sets {@code guard_mode = 0} for good.</li>
 *   <li>{@code mood} and {@code attack_level} are not saved; the NBT home keys are the King's ({@code KingHomeX}).</li>
 * </ul>
 */
public class TheQueen extends Monster implements LegacyArmor, VirtualHealth, com.swbr.orespawn.util.Royalty {

    /** DataWatcher 20: {@code attacking} (:94), wing, claw, leg, tail and head amplitude in the model. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING = SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
    /** DataWatcher 21: {@code PlayNicely} (:95, refreshed every 10 ticks :474), renderer scale. */
    private static final EntityDataAccessor<Integer> DATA_PLAY_NICELY = SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
    /** DataWatcher 22: {@code mood} (:96, :475), texture and {@link #isHappy()}. */
    private static final EntityDataAccessor<Integer> DATA_MOOD = SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);
    /** DataWatcher 23: {@code attack_level} as "power" (:97, :476), power cubes and particles. */
    private static final EntityDataAccessor<Integer> DATA_POWER = SynchedEntityData.defineId(TheQueen.class, EntityDataSerializers.INT);

    /** {@code EntityList.createEntityByName} targets of other porters, resolved by registry id at spawn time. */
    private static final ResourceLocation PURPLE_POWER_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "purple_power");
    private static final ResourceLocation THE_PRINCESS_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_princess");
    private static final ResourceLocation THE_PRINCE_EGG_ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "eggtheprince");

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget;
    private GenericTargetSorter TargetSorter;
    @Nullable
    private LivingEntity rt;
    private double attdam;
    private int hurt_timer;
    private int homex;
    private int homez;
    private int stream_count;
    private int stream_count_l;
    private int ticker;
    private int player_hit_count;
    private int backoff_timer;
    private int guard_mode;
    private volatile int head_found;
    private int wing_sound;
    private int attack_level;
    /** The melee victim whose healing is blocked ({@code ev}) and the health it may not exceed ({@code evh}, original units). */
    @Nullable
    private LivingEntity ev;
    private float evh;
    private int mood;
    private int always_mad;

    /** {@code TheQueen(World)} (:43-81) with {@code applyEntityAttributes} (:83-89). */
    public TheQueen(final EntityType<? extends TheQueen> type, final Level par1World) {
        super(type, par1World);
        this.currentFlightTarget = null;
        this.TargetSorter = null;
        this.rt = null;
        // applyEntityAttributes (:83-89) ran inside the EntityLivingBase constructor and set attdam to the config attack;
        // the field initialiser attdam = 250.0 (:48) ran after it and wins (R3, verhalten/entity-01.md).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.mygetMaxHealth()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.6200000047683716);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(MobStats.TheQueen_stats().attack());
        this.setHealth(this.getMaxHealth());
        this.attdam = 250.0;
        this.hurt_timer = 0;
        this.homex = 0;
        this.homez = 0;
        this.stream_count = 0;
        this.stream_count_l = 0;
        this.ticker = 0;
        this.player_hit_count = 0;
        this.backoff_timer = 0;
        this.guard_mode = 0;
        this.head_found = 0;
        this.wing_sound = 0;
        this.attack_level = 1;
        this.ev = null;
        this.evh = 0.0f;
        this.mood = 0;
        this.always_mad = 0;
        // setSize(22, 24) or (5.5, 6) by PlayNicely (:65-70): getDefaultDimensions, read like the original from the
        // local config on each side.
        this.refreshDimensions();
        // getNavigator().setAvoidsWater(false) (:71): water malus 0 (W04 precedent).
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.xpReward = 25000;
        // isImmuneToFire = true (:73): fireImmune() on the type; fireResistance = 5000 (:74): getFireImmuneTicks().
        this.noPhysics = true;
        this.TargetSorter = new GenericTargetSorter(this);
        // renderDistanceWeight = 12.0 (:76): superseded by shouldRenderAtSqrDistance() = true.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EntityAILookIdle(this));
        // PORT: EntityAIHurtByTarget(creature, false) did not check sight; HurtByTargetGoal drops an unseen target after
        // 60 ticks (Leon/Lizard precedent). The Queen never reads getTarget() for her attacks.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.moveControl = new IdleMoveControl(this);
        if (!par1World.isClientSide) {
            // PORT: entityInit wrote PlayNicely and attack_level as the watched values. A value defined as the default
            // is never sent to a tracking client (PitchBlack precedent), so they are defined 0 and set here.
            this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
            this.entityData.set(DATA_MOOD, this.mood);
            this.entityData.set(DATA_POWER, this.attack_level);
        }
    }

    /**
     * {@code applyEntityAttributes} (:83-89) with the config defaults (manifest: 6000 clamped to 1024 per R4, 0.62,
     * 225); the constructor writes the runtime config values.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(6000.0))
                .add(Attributes.MOVEMENT_SPEED, 0.6200000047683716)
                .add(Attributes.ATTACK_DAMAGE, 225.0);
    }

    /** {@code entityInit} (:91-98): four ints. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
        builder.define(DATA_PLAY_NICELY, 0);
        builder.define(DATA_MOOD, 0);
        builder.define(DATA_POWER, 1);
    }

    /** R4: {@code mygetMaxHealth()} in original units. */
    @Override
    public double getOriginalMaxHealth() {
        return this.mygetMaxHealth();
    }

    /** The hitbox of {@code setSize} (:65-70), R9. */
    @Override
    protected EntityDimensions getDefaultDimensions(final Pose pose) {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() == 0) {
            return EntityDimensions.scalable(22.0f, 24.0f);
        }
        return EntityDimensions.scalable(5.5f, 6.0f);
    }

    /** {@code fireResistance = 5000} (:74). */
    @Override
    protected int getFireImmuneTicks() {
        return 5000;
    }

    /** {@code getPlayNicely} (:100-102). */
    public int getPlayNicely() {
        return this.entityData.get(DATA_PLAY_NICELY);
    }

    /** {@code getIsHappy} (:104-106). */
    public int getIsHappy() {
        return this.entityData.get(DATA_MOOD);
    }

    /** {@code isInRangeToRenderDist} (:108-111). */
    @Override
    public boolean shouldRenderAtSqrDistance(final double par1) {
        return true;
    }

    /** {@code isInRangeToRenderVec3D} (:113-116). */
    @Override
    public boolean shouldRender(final double x, final double y, final double z) {
        return true;
    }

    /** {@code canDespawn} (:118-120). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getAttacking} (:122-124). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:126-128). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    /** {@code getPower} (:130-132). */
    public int getPower() {
        return this.entityData.get(DATA_POWER);
    }

    /** {@code setPower} (:134-136). */
    public void setPower(final int par1) {
        this.entityData.set(DATA_POWER, par1);
    }

    /** {@code getSoundVolume} (:138-140). */
    @Override
    protected float getSoundVolume() {
        return 1.35f;
    }

    /** {@code getSoundPitch} (:142-144): fixed 1.0, no random pitch. */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:146-148). */
    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.KING_LIVING.get();
    }

    /** {@code getHurtSound} (:150-152). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return ModSounds.KING_HIT.get();
    }

    /** {@code getDeathSound} (:154-156). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TREX_DEATH.get();
    }

    /** {@code canBePushed} (:158-160). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code collideWithEntity} (:162-163) is empty. */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:165-167). */
    public int mygetMaxHealth() {
        return MobStats.TheQueen_stats().health();
    }

    // getDropItem (:169-171, yellow_flower) is never read: dropFewItems does not call super.

    /** {@code dropItemRand} (:173-176): +-19 on the shared OreSpawnRand, 12 blocks up. */
    private void dropItemRand(@Nullable final Item index, final int par1) {
        final double x = this.getX() + OreSpawn.OreSpawnRand.nextInt(20) - OreSpawn.OreSpawnRand.nextInt(20);
        final double y = this.getY() + 12.0;
        final double z = this.getZ() + OreSpawn.OreSpawnRand.nextInt(20) - OreSpawn.OreSpawnRand.nextInt(20);
        if (index == null) {
            // PORT: an item of a porter whose registration is missing; the OreSpawnRand draws still happen.
            return;
        }
        final ItemEntity var3 = new ItemEntity(this.level(), x, y, z, new ItemStack(index, par1));
        this.level().addFreshEntity(var3);
    }

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropFewItems} (:178-188): Royal Guardian Sword, a Prince egg, The Princess 10 blocks up, and 56 times Queen
     * Scale, beef, bone and rotten flesh.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        this.dropItemRand(ModItems.ROYAL.get(), 1);
        this.dropItemRand(BuiltInRegistries.ITEM.getOptional(THE_PRINCE_EGG_ID).orElse(null), 1);
        spawnCreature(this.level(), THE_PRINCESS_ID, this.getX(), this.getY() + 10.0, this.getZ());
        for (int i = 0; i < 56; ++i) {
            this.dropItemRand(ModItems.QUEEN_SCALE.get(), 1);
            this.dropItemRand(Items.BEEF, 1);
            this.dropItemRand(Items.BONE, 1);
            this.dropItemRand(Items.ROTTEN_FLESH, 1);
        }
    }

    // isAIEnabled (:190-192): every 1.21.1 mob runs its goals.

    /** {@code isHappy} (:194-196): the watched mood. */
    public boolean isHappy() {
        return this.getIsHappy() == 0;
    }

    /**
     * {@code onUpdate} (:198-229): the vanilla tick (with the AI step and the travel), the wing beat every 31 ticks,
     * {@code noClip}, vertical damping, the melee phases by lost health and the client sparks above power 800.
     * Health comparisons are in original units (R4).
     */
    @Override
    public void tick() {
        super.tick();
        ++this.wing_sound;
        if (this.wing_sound > 30) {
            if (!this.level().isClientSide) {
                ArthropodSupport.playSoundAtEntity(this, ModSounds.MOTHRA_WINGS.get(), 1.75f, 0.75f);
            }
            this.wing_sound = 0;
        }
        this.noPhysics = true;
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6, m.z);
        final float health = VirtualHealth.originalHealth(this);
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() * 3 / 4) {
            this.attdam = MobStats.TheQueen_stats().attack() * 20;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 2) {
            this.attdam = MobStats.TheQueen_stats().attack() * 100;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 3) {
            this.attdam = MobStats.TheQueen_stats().attack() * 500;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 4) {
            this.attdam = MobStats.TheQueen_stats().attack() * 1000;
        }
        if (this.level().isClientSide && this.getPower() > 800) {
            final float f = 7.0f;
            final net.minecraft.util.RandomSource wr = this.level().random;
            if (wr.nextInt(4) == 1) {
                for (int i = 0; i < 10; ++i) {
                    final Vec3 mm = this.getDeltaMovement();
                    this.level().addParticle(ParticleTypes.FIREWORK,
                            this.getX() - f * Math.sin(Math.toRadians(this.getYRot())), this.getY() + 14.0,
                            this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())),
                            (wr.nextGaussian() - wr.nextGaussian()) / 5.0 + mm.x * 3.0,
                            (wr.nextGaussian() - wr.nextGaussian()) / 5.0,
                            (wr.nextGaussian() - wr.nextGaussian()) / 5.0 + mm.z * 3.0);
                }
            }
        }
    }

    /**
     * {@code attackEntityAsMob} (:231-281): remember the victim and block its healing (a big victim loses a quarter of
     * its health plus {@code attdam}), hit an Ender Dragon part with an explosion source, then {@code attdam} mob damage
     * with a push of 2.75 away and 0.2..0.45 up (x1.5 for players and removed entities) on a hit.
     *
     * <p>PORT: {@code setHealth}/{@code getHealth} of the victim are in its original units ({@link VirtualHealth}), so a
     * King victim is blocked at the same health as in 1.7.10. {@code setDead()} of a victim at 0 is {@link #removeVictim}.
     * {@code dragonPartBody} is private in 1.21.1 and reached through {@code getSubEntities()}; the explosion source
     * passes {@code EnderDragon.hurt(part, ...)} through {@code #minecraft:always_hurts_ender_dragons} (catalogue 6.3,
     * PitchBlack/Leon precedent). {@code addVelocity} is {@code push} with {@code hurtMarked}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (par1Entity != null && par1Entity instanceof LivingEntity && !this.level().isClientSide) {
            final LivingEntity e = (LivingEntity) par1Entity;
            if (!e.isRemoved()) {
                if (this.ev == e) {
                    if (this.evh < VirtualHealth.originalHealth(e)) {
                        VirtualHealth.setOriginalHealth(e, this.evh);
                    }
                } else {
                    this.ev = e;
                }
                if (e.getBbWidth() * e.getBbHeight() > 30.0f) {
                    VirtualHealth.setOriginalHealth(e, VirtualHealth.originalHealth(e) * 3.0f / 4.0f);
                    e.hurt(this.damageSources().mobAttack(this), (float) this.attdam);
                }
                this.evh = VirtualHealth.originalHealth(e);
                if (this.evh <= 0.0f) {
                    removeVictim(this.ev);
                }
            } else {
                this.ev = null;
                this.evh = 0.0f;
            }
        }
        if (par1Entity != null && par1Entity instanceof EnderDragon dr) {
            DamageSource var21 = null;
            var21 = this.damageSources().explosion(null, null);
            if (this.level().random.nextInt(6) == 1) {
                dr.hurt(dr.head, var21, (float) this.attdam);
            } else {
                dr.hurt(dragonBody(dr), var21, (float) this.attdam);
            }
        }
        final boolean var22 = par1Entity.hurt(this.damageSources().mobAttack(this), (float) this.attdam);
        if (var22) {
            final double ks = 2.75;
            double inair = 0.2;
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            inair += this.level().random.nextFloat() * 0.25f;
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 1.5;
            }
            par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            par1Entity.hurtMarked = true;
        }
        return var22;
    }

    /**
     * {@code setDead()} on the blocked victim (:249, :330).
     *
     * <p>PORT (R18 case 1): {@code discard()} does not remove a player cleanly in 1.21.1 (no death, the connection keeps
     * the removed entity), so a player is killed with {@code kill()} instead; every other victim is discarded as in the
     * original, without death or drops.
     */
    private static void removeVictim(final LivingEntity victim) {
        if (victim instanceof Player) {
            victim.kill();
        } else {
            victim.discard();
        }
    }

    /** {@code dragonPartBody}: the part named "body" (index 2 of {@code getSubEntities()}). */
    private static EnderDragonPart dragonBody(final EnderDragon dr) {
        for (final EnderDragonPart part : dr.getSubEntities()) {
            if ("body".equals(part.name)) {
                return part;
            }
        }
        return dr.getSubEntities()[2];
    }

    /**
     * {@code canSeeTarget} (:283-285): no block between a point 8.75 above the feet and the target; 1.7.10
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded (Mothra precedent).
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 8.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /** {@code tooFarFromHome} (:287-292): more than 120 blocks horizontally. */
    private boolean tooFarFromHome() {
        float d1 = (float) (this.getX() - this.homex);
        final float d2 = (float) (this.getZ() - this.homez);
        d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
        return d1 > 120.0f;
    }

    /**
     * {@code updateAITasks} (:294-710). {@code (int)} casts of coordinates are {@link Mth#floor} (R20); {@code worldObj.rand}
     * is the level random, {@code moveForward} is {@code zza}; health in original units (R4).
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int attrand = 5;
        int which = 0;
        LivingEntity e = null;
        LivingEntity f = null;
        double rr = 0.0;
        double rhdir = 0.0;
        double rdd = 0.0;
        final double pi = 3.1415926545;
        double var1 = 0.0;
        double var2 = 0.0;
        double var3 = 0.0;
        float var4 = 0.0f;
        float var5 = 0.0f;
        final double xzoff = 8.0;
        final double yoff = 14.0;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        final net.minecraft.util.RandomSource wr = this.level().random;
        if (this.ev != null) {
            if (this.distanceToSqr(this.ev) < 2000.0 && !this.ev.isRemoved()) {
                if (this.evh < VirtualHealth.originalHealth(this.ev)) {
                    VirtualHealth.setOriginalHealth(this.ev, this.evh);
                } else {
                    this.evh = VirtualHealth.originalHealth(this.ev);
                }
                if (this.evh <= 0.0f) {
                    removeVictim(this.ev);
                }
            } else {
                this.ev = null;
                this.evh = 0.0f;
            }
        }
        if (this.attack_level > 1000) {
            if (this.mood == 1) {
                int j = 15;
                if (this.player_hit_count < 10) {
                    j = 45;
                }
                for (int i = 0; i < j; ++i) {
                    final Entity ppwr = spawnCreature(this.level(), PURPLE_POWER_ID,
                            this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot())), this.getY() + yoff,
                            this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot())));
                    if (ppwr != null) {
                        final Vec3 pm = ppwr.getDeltaMovement();
                        final Vec3 qm = this.getDeltaMovement();
                        ppwr.setDeltaMovement(qm.x * 3.0, pm.y, qm.z * 3.0);
                    }
                }
            } else {
                if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    this.terraform(wr);
                }
                for (int m = 0; m < 10; ++m) {
                    final int i = wr.nextInt(15) - wr.nextInt(15);
                    final int k = wr.nextInt(15) - wr.nextInt(15);
                    final int j = wr.nextInt(20);
                    final BlockState bid = this.level().getBlockState(
                            new BlockPos(Mth.floor(this.getX()) + i, Mth.floor(this.getY()) + j, Mth.floor(this.getZ()) + k));
                    if (bid.isAir()) {
                        if (wr.nextInt(2) == 0) {
                            spawnCreature(this.level(), ModEntities.BUTTERFLY.get(), this.getX() + i, this.getY() + j, this.getZ() + k);
                        } else {
                            spawnCreature(this.level(), ModEntities.BIRD.get(), this.getX() + i, this.getY() + j, this.getZ() + k);
                        }
                    }
                }
            }
            this.attack_level = 1;
        }
        if (this.attack_level > 1) {
            --this.attack_level;
        }
        if (this.hurt_timer > 0) {
            --this.hurt_timer;
        }
        if ((this.homex == 0 && this.homez == 0) || this.guard_mode == 0) {
            this.homex = Mth.floor(this.getX());
            this.homez = Mth.floor(this.getZ());
        }
        if (VirtualHealth.originalHealth(this) > this.mygetMaxHealth() - 2 && wr.nextInt(500) == 1) {
            this.mood = 0;
        }
        if (this.always_mad != 0) {
            this.mood = 1;
        }
        if (this.mood == 0) {
            this.attack_level += 10;
        }
        ++this.ticker;
        if (this.ticker > 30000) {
            this.ticker = 0;
        }
        if (this.ticker % 60 == 0) {
            this.stream_count = 10;
        }
        if (this.ticker % 70 == 0) {
            this.stream_count_l = 6;
        }
        if (this.ticker % 10 == 0) {
            this.entityData.set(DATA_PLAY_NICELY, OreSpawnConfig.TWEAKS.PlayNicely.get());
            this.entityData.set(DATA_MOOD, this.mood);
            this.setPower(this.attack_level);
        }
        if (this.backoff_timer > 0) {
            --this.backoff_timer;
        }
        if (this.player_hit_count < 10 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth() / 2) {
            attrand = 3;
        }
        this.noPhysics = true;
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.tooFarFromHome() || wr.nextInt(200) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 9.1f) {
            zdir = wr.nextInt(120);
            xdir = wr.nextInt(120);
            if (wr.nextInt(2) == 0) {
                zdir = -zdir;
            }
            if (wr.nextInt(2) == 0) {
                xdir = -xdir;
            }
            int dist = this.heightProbe(this.homex, this.homez);
            dist = dist / 9 + 2;
            // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
            if (Mth.floor(this.getY() + dist) > RoyalCeiling.of(this.level())) {
                dist = RoyalCeiling.of(this.level()) - Mth.floor(this.getY());
            }
            this.currentFlightTarget.set(this.homex + xdir, Mth.floor(this.getY() + dist), this.homez + zdir);
            if (this.mood == 0) {
                final List<TheKing> kinglist = new ArrayList<>(
                        this.level().getEntitiesOfClass(TheKing.class, this.getBoundingBox().inflate(64.0, 32.0, 64.0)));
                kinglist.sort(this.TargetSorter);
                if (!kinglist.isEmpty()) {
                    final TheKing var7 = kinglist.get(0);
                    this.guard_mode = 0;
                    zdir = wr.nextInt(16);
                    xdir = wr.nextInt(16);
                    if (wr.nextInt(2) == 0) {
                        zdir = -zdir;
                    }
                    if (wr.nextInt(2) == 0) {
                        xdir = -xdir;
                    }
                    this.currentFlightTarget.set(Mth.floor(var7.getX()) + xdir,
                            Mth.floor(var7.getY() + (wr.nextInt(8) - wr.nextInt(8))), Mth.floor(var7.getZ()) + zdir);
                }
            }
        } else if (wr.nextInt(attrand) == 0) {
            e = this.rt;
            if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0 || this.isHappy()) {
                e = null;
            }
            if (e != null && (e instanceof TheQueen || e instanceof QueenHead)) {
                this.rt = null;
                e = null;
            }
            if (e != null) {
                float d1 = (float) (e.getX() - this.homex);
                final float d2 = (float) (e.getZ() - this.homez);
                d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
                if (e.isRemoved() || wr.nextInt(450) == 1 || (d1 > 128.0f && this.guard_mode == 1)) {
                    e = null;
                    this.rt = null;
                }
                if (e != null && !this.MyCanSee(e)) {
                    e = null;
                }
            }
            f = this.findSomethingToAttack();
            if (this.head_found == 0 && this.mood == 1) {
                spawnCreature(this.level(), ModEntities.QUEEN_HEAD.get(), this.getX(), this.getY() + 20.0, this.getZ());
            }
            if (e == null) {
                e = f;
            }
            if (e != null) {
                final float d1 = e.getBbWidth() * e.getBbHeight();
                if (this.attack_level < 1000) {
                    this.attack_level += 15;
                    if (VirtualHealth.originalHealth(this) < this.mygetMaxHealth() / 2) {
                        this.attack_level += 15;
                    }
                    if (d1 > 50.0f) {
                        this.attack_level += 15;
                    }
                    if (d1 > 100.0f) {
                        this.attack_level += 15;
                    }
                    if (d1 > 200.0f) {
                        this.attack_level += 25;
                    }
                }
                this.setAttacking(1);
                if (this.backoff_timer == 0) {
                    int dist = Mth.floor(e.getY() + e.getBbHeight() / 2.0f + 1.0);
                    // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
                    if (dist > RoyalCeiling.of(this.level())) {
                        dist = RoyalCeiling.of(this.level());
                    }
                    this.currentFlightTarget.set(Mth.floor(e.getX()), dist, Mth.floor(e.getZ()));
                    if (wr.nextInt(50) == 1) {
                        this.backoff_timer = 90 + wr.nextInt(90);
                    }
                } else if (InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 9.1f) {
                    zdir = wr.nextInt(20) + 30;
                    xdir = wr.nextInt(20) + 30;
                    if (wr.nextInt(2) == 0) {
                        zdir = -zdir;
                    }
                    if (wr.nextInt(2) == 0) {
                        xdir = -xdir;
                    }
                    int dist = this.heightProbe(Mth.floor(e.getX()), Mth.floor(e.getZ()));
                    dist = dist / 9 + 2;
                    // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
                    if (Mth.floor(this.getY() + dist) > RoyalCeiling.of(this.level())) {
                        dist = RoyalCeiling.of(this.level()) - Mth.floor(this.getY());
                    }
                    this.currentFlightTarget.set(Mth.floor(e.getX()) + xdir, Mth.floor(this.getY() + dist), Mth.floor(e.getZ()) + zdir);
                }
                if (this.distanceToSqr(e) < 900.0) {
                    if (wr.nextInt(2) == 1) {
                        this.doJumpDamage(this.getX(), this.getY(), this.getZ(), 15.0, MobStats.TheQueen_stats().attack() / 4, 0);
                    }
                    this.doHurtTarget(e);
                }
                final double dx = this.getX() + 20.0 * Math.sin(Math.toRadians(this.getYRot()));
                final double dz = this.getZ() - 20.0 * Math.cos(Math.toRadians(this.getYRot()));
                if (wr.nextInt(3) == 1) {
                    this.doJumpDamage(dx, this.getY() + 10.0, dz, 15.0, MobStats.TheQueen_stats().attack() / 2, 1);
                }
                if (this.getHorizontalDistanceSqToEntity(e) > 900.0) {
                    which = wr.nextInt(2);
                    if (which == 0) {
                        if (this.stream_count > 0) {
                            this.setAttacking(1);
                            rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                            rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                            rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                            if (rdd > pi) {
                                rdd -= pi * 2.0;
                            }
                            rdd = Math.abs(rdd);
                            if (rdd < 0.5) {
                                this.firecanon(e);
                            }
                        }
                    } else if (this.stream_count_l > 0) {
                        this.setAttacking(1);
                        rr = Math.atan2(e.getZ() - this.getZ(), e.getX() - this.getX());
                        rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
                        rdd = Math.abs(rr - rhdir) % (pi * 2.0);
                        if (rdd > pi) {
                            rdd -= pi * 2.0;
                        }
                        rdd = Math.abs(rdd);
                        if (rdd < 0.5) {
                            this.firecanonl(e);
                        }
                    }
                }
            } else {
                this.setAttacking(0);
                this.stream_count = 10;
                this.stream_count_l = 6;
            }
        }
        var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 m = this.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.65 - m.x) * 0.35;
        final double motionY = m.y + (Math.signum(var2) * 0.69999 - m.y) * 0.3;
        final double motionZ = m.z + (Math.signum(var3) * 0.65 - m.z) * 0.35;
        this.setDeltaMovement(motionX, motionY, motionZ);
        var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + var5 / 8.0f);
        if (wr.nextInt(32) == 1 && VirtualHealth.originalHealth(this) < this.mygetMaxHealth()) {
            this.heal(5.0f);
            if (this.player_hit_count < 10) {
                this.heal(50.0f);
            }
        }
        if (this.player_hit_count < 10 && VirtualHealth.originalHealth(this) < 2000.0f) {
            this.heal(2000.0f - VirtualHealth.originalHealth(this));
        }
    }

    /**
     * The block count of :498-520 (home) and :613-635 (target): for the 3x3 grid of columns 5 apart around
     * {@code (cx, cz)} at the Queen's height, count up to the first air (solid start) or down to the first non-air (air
     * start).
     */
    private int heightProbe(final int cx, final int cz) {
        int dist = 0;
        final int py = Mth.floor(this.getY());
        for (int i = -5; i <= 5; i += 5) {
            for (int j = -5; j <= 5; j += 5) {
                BlockState bid = this.level().getBlockState(new BlockPos(cx + j, py, cz + i));
                if (!bid.isAir()) {
                    for (int k = 1; k < 20; ++k) {
                        bid = this.level().getBlockState(new BlockPos(cx + j, py + k, cz + i));
                        ++dist;
                        if (bid.isAir()) {
                            break;
                        }
                    }
                } else {
                    for (int k = 1; k < 20; ++k) {
                        bid = this.level().getBlockState(new BlockPos(cx + j, py - k, cz + i));
                        --dist;
                        if (!bid.isAir()) {
                            break;
                        }
                    }
                }
            }
        }
        return dist;
    }

    /**
     * The happy terraforming of :353-426, under {@code mobGriefing}: 25 random columns within +-24, scanned upwards from
     * 20 below.
     *
     * <p>PORT (R22): {@code Blocks.dirt}, {@code stone} and {@code sand} are the 1.21.1 categories
     * {@code #minecraft:dirt}, {@code #minecraft:base_stone_overworld} and {@code #minecraft:sand}; grass keeps its own
     * branch first. {@code lava} and {@code flowing_lava} are the lava source and a flowing lava state; {@code water} and
     * {@code flowing_water} with metadata 0 are both the water source state. {@code setBlock} with flag 3, no survival
     * check, as the original.
     */
    private void terraform(final net.minecraft.util.RandomSource wr) {
        final Level world = this.level();
        final int bx = Mth.floor(this.getX());
        final int by = Mth.floor(this.getY());
        final int bz = Mth.floor(this.getZ());
        int which;
        for (int m = 0; m < 25; ++m) {
            final int i = wr.nextInt(25) - wr.nextInt(25);
            final int k = wr.nextInt(25) - wr.nextInt(25);
            int j = -20;
            while (j < 20) {
                final BlockPos at = new BlockPos(bx + i, by + j, bz + k);
                final BlockPos above = at.above();
                final BlockState bid = world.getBlockState(at);
                if (bid.is(Blocks.GRASS_BLOCK)) {
                    if (!world.getBlockState(above).isAir()) {
                        break;
                    }
                    which = wr.nextInt(8);
                    if (which == 0) {
                        world.setBlock(above, Blocks.POPPY.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (which == 1) {
                        world.setBlock(above, Blocks.DANDELION.defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (which == 2) {
                        world.setBlock(above, ModBlocks.FLOWER_BLUE.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (which == 3) {
                        world.setBlock(above, ModBlocks.FLOWER_PINK.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (which == 4) {
                        world.setBlock(above, ModBlocks.CRYSTAL_FLOWER_RED.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (which == 5) {
                        world.setBlock(above, ModBlocks.CRYSTAL_FLOWER_GREEN.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (which == 6) {
                        world.setBlock(above, ModBlocks.CRYSTAL_FLOWER_BLUE.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                    if (which == 7) {
                        world.setBlock(above, ModBlocks.CRYSTAL_FLOWER_YELLOW.get().defaultBlockState(), Block.UPDATE_ALL);
                        break;
                    }
                    break;
                } else {
                    if (bid.is(BlockTags.DIRT) && world.getBlockState(above).isAir()) {
                        world.setBlock(at, Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        break;
                    }
                    if (bid.is(BlockTags.BASE_STONE_OVERWORLD) && world.getBlockState(above).isAir()) {
                        world.setBlock(above, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                        break;
                    }
                    if (bid.is(BlockTags.SAND) && world.getBlockState(above).isAir()) {
                        if (wr.nextInt(2) == 0) {
                            world.setBlock(above, Blocks.CACTUS.defaultBlockState(), Block.UPDATE_ALL);
                            break;
                        }
                        world.setBlock(at, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                        break;
                    } else {
                        if (bid.is(Blocks.LAVA) && bid.getFluidState().isSource() && world.getBlockState(above).isAir()) {
                            world.setBlock(at, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                            break;
                        }
                        if (bid.is(Blocks.LAVA) && !bid.getFluidState().isSource() && world.getBlockState(above).isAir()) {
                            world.setBlock(at, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                            break;
                        }
                        if (bid.isAir() && j > 0) {
                            break;
                        }
                        ++j;
                    }
                }
            }
        }
    }

    /** {@code getHorizontalDistanceSqToEntity} (:712-716). */
    private double getHorizontalDistanceSqToEntity(final Entity e) {
        final double d1 = e.getZ() - this.getZ();
        final double d2 = e.getX() - this.getX();
        return d1 * d1 + d2 * d2;
    }

    /**
     * {@code firecanon} (:718-747): 32 blocks ahead, 14 up, one really big fireball at the target's middle and six big
     * (half of them small) ones scattered by +-5/+-3/+-5.
     *
     * <p>{@code "random.fuse"} is {@link SoundEvents#TNT_PRIMED}, {@code "random.bow"} {@link SoundEvents#ARROW_SHOOT}
     * (MothSupport precedent).
     */
    private void firecanon(final LivingEntity e) {
        final double yoff = 14.0;
        final double xzoff = 32.0;
        BetterFireball bf = null;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        final net.minecraft.util.RandomSource wr = this.level().random;
        if (this.stream_count > 0) {
            bf = new BetterFireball(this.level(), this, e.getX() - cx, e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff), e.getZ() - cz);
            bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
            bf.setPos(cx, this.getY() + yoff, cz);
            bf.setReallyBig();
            ArthropodSupport.playSoundAtEntity(this, SoundEvents.TNT_PRIMED, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            this.level().addFreshEntity(bf);
            for (int i = 0; i < 6; ++i) {
                final float r1 = 5.0f * (wr.nextFloat() - wr.nextFloat());
                final float r2 = 3.0f * (wr.nextFloat() - wr.nextFloat());
                final float r3 = 5.0f * (wr.nextFloat() - wr.nextFloat());
                bf = new BetterFireball(this.level(), this, e.getX() - cx + r1,
                        e.getY() + e.getBbHeight() / 2.0f - (this.getY() + yoff) + r2, e.getZ() - cz + r3);
                bf.moveTo(cx, this.getY() + yoff, cz, this.getYRot(), 0.0f);
                bf.setPos(cx, this.getY() + yoff, cz);
                bf.setBig();
                if (wr.nextInt(2) == 1) {
                    bf.setSmall();
                }
                ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
                this.level().addFreshEntity(bf);
            }
            --this.stream_count;
        }
    }

    /** {@code firecanonl} (:749-778): three thunder bolts from 32 blocks ahead, 14 up, at triple speed. */
    private void firecanonl(final LivingEntity e) {
        final double yoff = 14.0;
        final double xzoff = 32.0;
        double var3 = 0.0;
        double var4 = 0.0;
        double var5 = 0.0;
        float var6 = 0.0f;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        if (this.stream_count_l > 0) {
            ArthropodSupport.playSoundAtEntity(this, SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
            for (int i = 0; i < 3; ++i) {
                final ThunderBolt lb = new ThunderBolt(this.level(), cx, this.getY() + yoff, cz);
                lb.moveTo(cx, this.getY() + yoff, cz, 0.0f, 0.0f);
                var3 = e.getX() - lb.getX();
                var4 = e.getY() + 0.25 - lb.getY();
                var5 = e.getZ() - lb.getZ();
                // MathHelper.sqrt_double: (float) Math.sqrt(double).
                var6 = (float) Math.sqrt(var3 * var3 + var5 * var5) * 0.2f;
                lb.setThrowableHeading(var3, var4 + var6, var5, 1.4f, 4.0f);
                final Vec3 lm = lb.getDeltaMovement();
                lb.setDeltaMovement(lm.x * 3.0, lm.y * 3.0, lm.z * 3.0);
                this.level().addFreshEntity(lb);
            }
            --this.stream_count_l;
        }
    }

    // canTriggerWalking (:780-782) returns true: no NoStepTrigger (R20).

    /** {@code fall} (:784-785): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:787-788): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    // doesEntityNotTriggerPressurePlate (:790-792) returns the default false.

    /**
     * {@code attackEntityFrom} (:794-843): refused during {@code hurt_timer}; capped at 750; {@code inWall} refused; then
     * angry; an explosion heals half its uncapped amount and is refused; PurplePower as attacker refused; a small
     * {@code EntityMob} attacker (footprint below 3) is removed and refused; a cactus is refused after all that;
     * otherwise the hit, 20 ticks of immunity, the player counter and the attacker as the next flight target.
     *
     * <p>The cap and everything above run in original units; {@code CombatEvents} scales the capped amount once
     * {@code super.hurt} is reached (R4). PORT: {@code isExplosion()} is {@code #minecraft:is_explosion}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        boolean ret = false;
        float dm = par2;
        if (this.hurt_timer > 0) {
            return false;
        }
        if (dm > 750.0f) {
            dm = 750.0f;
        }
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        this.mood = 1;
        if (par1DamageSource.is(DamageTypeTags.IS_EXPLOSION)) {
            float s = VirtualHealth.originalHealth(this);
            s += par2 / 2.0f;
            if (s > this.getOriginalMaxHealth()) {
                s = (float) this.getOriginalMaxHealth();
            }
            VirtualHealth.setOriginalHealth(this, s);
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof LivingEntity) {
            if (e instanceof PurplePower) {
                return false;
            }
            final float s2 = e.getBbHeight() * e.getBbWidth();
            if (e instanceof Monster && s2 < 3.0f) {
                e.discard();
                return false;
            }
        }
        if (!par1DamageSource.is(DamageTypes.CACTUS)) {
            this.hurt_timer = 20;
            ret = super.hurt(par1DamageSource, dm);
            if (e != null && e instanceof Player) {
                ++this.player_hit_count;
            }
            if (e != null && e instanceof LivingEntity living && this.currentFlightTarget != null && !MyUtils.isRoyalty(e)) {
                this.rt = living;
                int dist = Mth.floor(e.getY());
                // PORT: 230 only in the OreSpawn dimensions, see RoyalCeiling (R26).
                if (dist > RoyalCeiling.of(this.level())) {
                    dist = RoyalCeiling.of(this.level());
                }
                this.currentFlightTarget.set(Mth.floor(e.getX()), dist, Mth.floor(e.getZ()));
            }
        }
        return ret;
    }

    /** {@code getCanSpawnHere} (:845-847): always; no natural spawns exist (manifest). */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getTotalArmorValue} (:849-860) in original health units; +3 and +5 unreachable (R18). */
    public int getTotalArmorValue() {
        final float health = VirtualHealth.originalHealth(this);
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() * 2 / 3) {
            return MobStats.TheQueen_stats().defense() + 2;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 2) {
            return MobStats.TheQueen_stats().defense() + 3;
        }
        if (this.player_hit_count < 10 && health < this.mygetMaxHealth() / 3) {
            return MobStats.TheQueen_stats().defense() + 5;
        }
        return MobStats.TheQueen_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** {@code onStruckByLightning} (:862-863) is empty. */
    @Override
    public void thunderHit(final ServerLevel level, final LightningBolt lightning) {
    }

    // initCreature (:865-866) is empty and has no 1.21.1 caller.

    /**
     * {@code MyCanSee} (:868-922): march 20 steps (scaled by the longest axis) from 10 blocks ahead, 14 up, towards the
     * target's middle; anything but air blocks the view. Float arithmetic as the original; {@code (int)} of the float
     * coordinates is {@link Mth#floor} (R20).
     */
    public boolean MyCanSee(final LivingEntity e) {
        final double xzoff = 10.0;
        int nblks = 20;
        final double cx = this.getX() - xzoff * Math.sin(Math.toRadians(this.getYRot()));
        final double cz = this.getZ() + xzoff * Math.cos(Math.toRadians(this.getYRot()));
        float startx = (float) cx;
        float starty = (float) (this.getY() + 14.0);
        float startz = (float) cz;
        float dx = (float) ((e.getX() - startx) / 20.0);
        float dy = (float) ((e.getY() + e.getBbHeight() / 2.0f - starty) / 20.0);
        float dz = (float) ((e.getZ() - startz) / 20.0);
        if (Math.abs(dx) > 1.0) {
            dy /= Math.abs(dx);
            dz /= Math.abs(dx);
            nblks *= (int) Math.abs(dx);
            if (dx > 1.0f) {
                dx = 1.0f;
            }
            if (dx < -1.0f) {
                dx = -1.0f;
            }
        }
        if (Math.abs(dy) > 1.0) {
            dx /= Math.abs(dy);
            dz /= Math.abs(dy);
            nblks *= (int) Math.abs(dy);
            if (dy > 1.0f) {
                dy = 1.0f;
            }
            if (dy < -1.0f) {
                dy = -1.0f;
            }
        }
        if (Math.abs(dz) > 1.0) {
            dy /= Math.abs(dz);
            dx /= Math.abs(dz);
            nblks *= (int) Math.abs(dz);
            if (dz > 1.0f) {
                dz = 1.0f;
            }
            if (dz < -1.0f) {
                dz = -1.0f;
            }
        }
        for (int i = 0; i < nblks; ++i) {
            startx += dx;
            starty += dy;
            startz += dz;
            final BlockState bid = this.level().getBlockState(new BlockPos(Mth.floor(startx), Mth.floor(starty), Mth.floor(startz)));
            if (!bid.isAir()) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@code isSuitableTarget} (:924-969): alive, not self; a QueenHead marks the head as found; no royalty, nothing
     * farther than 144 from home, nothing ignorable, only what the senses see; survival players, horses, monsters, the
     * Ender Dragon and the attackable non-monsters.
     *
     * <p>{@code EntityHorse} is {@link AbstractHorse} (all 1.7.10 horse, donkey, mule and undead horse variants),
     * {@code EntityMob} is {@link Monster}.
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
        if (par1EntityLiving instanceof QueenHead) {
            this.head_found = 1;
            return false;
        }
        if (MyUtils.isRoyalty(par1EntityLiving)) {
            return false;
        }
        float d1 = (float) (par1EntityLiving.getX() - this.homex);
        final float d2 = (float) (par1EntityLiving.getZ() - this.homez);
        d1 = (float) Math.sqrt(d1 * d1 + d2 * d2);
        if (d1 > 144.0f) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        if (par1EntityLiving instanceof AbstractHorse) {
            return true;
        }
        if (par1EntityLiving instanceof Monster) {
            return true;
        }
        if (par1EntityLiving instanceof EnderDragon) {
            return true;
        }
        return MyUtils.isAttackableNonMob(par1EntityLiving);
    }

    /**
     * {@code findSomethingToAttack} (:971-994): nothing while PlayNicely or happy (and the head counts as found); else
     * the nearest suitable entity in {@code expand(80, 60, 80)}, the scan continuing until the head was seen too.
     */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0 || this.isHappy()) {
            this.head_found = 1;
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(80.0, 60.0, 80.0)));
        var5.sort(this.TargetSorter);
        LivingEntity ret = null;
        this.head_found = 0;
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false) && ret == null) {
                ret = var8;
            }
            if (ret != null && this.head_found != 0) {
                break;
            }
        }
        return ret;
    }

    /** {@code setGuardMode} (:996-998). */
    public void setGuardMode(final int i) {
        this.guard_mode = i;
    }

    /** {@code setBadMood} (:1000-1002). */
    public void setBadMood(final int i) {
        this.always_mad = i;
    }

    /** {@code writeEntityToNBT} (:1004-1011). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("KingHomeX", this.homex);
        par1NBTTagCompound.putInt("KingHomeZ", this.homez);
        par1NBTTagCompound.putInt("GuardMode", this.guard_mode);
        par1NBTTagCompound.putInt("PlayerHits", this.player_hit_count);
        par1NBTTagCompound.putInt("MeanMode", this.always_mad);
    }

    /** {@code readEntityFromNBT} (:1013-1020). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.homex = par1NBTTagCompound.getInt("KingHomeX");
        this.homez = par1NBTTagCompound.getInt("KingHomeZ");
        this.guard_mode = par1NBTTagCompound.getInt("GuardMode");
        this.player_hit_count = par1NBTTagCompound.getInt("PlayerHits");
        this.always_mad = par1NBTTagCompound.getInt("MeanMode");
    }

    /**
     * {@code spawnCreature} (:1022-1030) by registry id: create, random yaw, add. No {@code finalizeSpawn} and no living
     * sound - the original had neither. An id of a porter not yet registered spawns nothing, like an unknown
     * {@code EntityList} name.
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final ResourceLocation par1, final double par2, final double par4,
                                       final double par6) {
        final EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(par1).orElse(null);
        if (type == null) {
            return null;
        }
        return spawnCreature(par0World, type, par2, par4, par6);
    }

    /** {@code spawnCreature} (:1022-1030) for a type this port holds directly. */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2, final double par4,
                                       final double par6) {
        final Entity var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
        }
        return var8;
    }

    /**
     * {@code doJumpDamage} (:1032-1075): every living non-royal, non-ghost entity in a box of +-dist and +-10 takes
     * half the damage as an explosion and half as a fall, hears an explosion, and with {@code knock != 0} is flung 2.75
     * away from the Queen and 0.65 up. The second hit meets the 10-tick immunity of the first unless it is larger - in
     * both versions (catalogue 6.4, same-tick hits).
     *
     * <p>PORT: {@code DamageSource.setExplosionSource(null).setExplosion()} is {@code explosion(null, null)};
     * {@code DamageSource.fall} is {@code fall()}; {@code "random.explode"} is {@code GENERIC_EXPLODE}.
     */
    @Nullable
    private LivingEntity doJumpDamage(final double X, final double Y, final double Z, final double dist, final double damage,
                                      final int knock) {
        final AABB bb = new AABB(X - dist, Y - 10.0, Z - dist, X + dist, Y + 10.0, Z + dist);
        final List<LivingEntity> var5 = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, bb));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (var8 == null) {
                continue;
            }
            if (var8 == this) {
                continue;
            }
            if (!var8.isAlive()) {
                continue;
            }
            if (MyUtils.isRoyalty(var8)) {
                continue;
            }
            if (var8 instanceof Ghost) {
                continue;
            }
            if (var8 instanceof GhostSkelly) {
                continue;
            }
            DamageSource var9 = null;
            var9 = this.damageSources().explosion(null, null);
            var8.hurt(var9, (float) damage / 2.0f);
            var8.hurt(this.damageSources().fall(), (float) damage / 2.0f);
            ArthropodSupport.playSoundAtEntity(var8, SoundEvents.GENERIC_EXPLODE.value(), 0.65f,
                    1.0f + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5f);
            if (knock == 0) {
                continue;
            }
            final double ks = 2.75;
            final double inair = 0.65;
            final float f3 = (float) Math.atan2(var8.getZ() - this.getZ(), var8.getX() - this.getX());
            var8.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            var8.hurtMarked = true;
        }
        return null;
    }

    /**
     * {@code EntityMoveHelper} of a mob without a moving task: it only ever zeroed {@code moveForward}, and did so before
     * {@code updateAITasks} wrote 0.75. See the class comment.
     */
    static final class IdleMoveControl extends MoveControl {

        IdleMoveControl(final TheQueen owner) {
            super(owner);
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.WAIT) {
                super.tick();
            }
        }
    }
}
