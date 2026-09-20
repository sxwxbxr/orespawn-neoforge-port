package com.swbr.orespawn.entity.fairy;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Fairy} (Fairy.java:16-350), id {@code fairy}: a glowing {@code EntityAmbientCreature}
 * that flits around, hunts {@code EntityMob}s within 8 and, when summoned by the Fairy Sword, follows its owner.
 * Nine texture variants, drops Crystal Torches.
 *
 * <p>Original bugs kept 1:1 (R18): the type is written as {@code FairyType} but read as {@code fairyType} (:204, :213),
 * so every fairy comes back from disk as type 0; {@code writeEntityToNBT} turns a missing owner into the string
 * {@code "null"} in memory (:200-202), so an unowned fairy stops despawning after its first save until it is loaded
 * again. Each side rolls its own {@code fairy_type} in
 * the constructor and the client adopts the server's only at the first 10-tick sync (:178-187).
 *
 * <p>Not carried over: {@code initCreature} (:332-333, empty); {@code isAIEnabled} (:167-169) is the 1.21.1 default.
 */
public class Fairy extends AmbientCreature implements NoStepTrigger, LegacyArmor {

    /** {@code texture0..8} (:18-26, :339-349), {@code fairytexture.png} and {@code fairytexture2..9.png}. */
    private static final ResourceLocation texture0 = texture("fairytexture.png");
    private static final ResourceLocation texture1 = texture("fairytexture2.png");
    private static final ResourceLocation texture2 = texture("fairytexture3.png");
    private static final ResourceLocation texture3 = texture("fairytexture4.png");
    private static final ResourceLocation texture4 = texture("fairytexture5.png");
    private static final ResourceLocation texture5 = texture("fairytexture6.png");
    private static final ResourceLocation texture6 = texture("fairytexture7.png");
    private static final ResourceLocation texture7 = texture("fairytexture8.png");
    private static final ResourceLocation texture8 = texture("fairytexture9.png");

    /** DataWatcher 20, int, {@code fairy_type} (:94-97). Defined while the field is still 0, as {@code entityInit} ran. */
    private static final EntityDataAccessor<Integer> DATA_FAIRY_TYPE =
            SynchedEntityData.defineId(Fairy.class, EntityDataSerializers.INT);

    int my_blink = 0;
    int blinker = 0;
    int myspace = 0; // :29, never read
    public int fairy_type = 0;
    private int force_sync = 10;
    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;
    @Nullable
    private String myowner = null;
    private GenericTargetSorter TargetSorter = null;

    private static ResourceLocation texture(final String file) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + file);
    }

    /**
     * Constructor (:36-56): blink period 20..39 from the entity's random, type 0..8 from the world's random,
     * {@code setAvoidsWater(true)}, the target sorter. {@code setSize(0.4f, 0.8f)} is the entity type's size;
     * {@code renderDistanceWeight = 3.0} is {@link #shouldRenderAtSqrDistance}; the two tasks are in
     * {@link #registerGoals()}.
     */
    public Fairy(final EntityType<? extends Fairy> type, final Level level) {
        super(type, level);
        this.my_blink = 20 + this.random.nextInt(20);
        if (level != null) {
            this.fairy_type = level.random.nextInt(9);
        }
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
        this.TargetSorter = new GenericTargetSorter(this);
    }

    /** Tasks (:53-54): 0 watch the closest {@code EntityLiving} within 8, 1 look idle - the 1.7.10 goal ports (W06). */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new EntityAIWatchClosest(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(1, new EntityAILookIdle(this));
    }

    /**
     * {@code applyEntityAttributes} (:58-64): health {@link #mygetMaxHealth()} = 40, speed 0.1, attack 3.0 (registered
     * here, but the hit itself deals 2.0, :117-123). {@code ARMOR} 4 mirrors {@code getTotalArmorValue} (:125-127).
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 4.0);
    }

    /** {@code getTexture(Fairy)} (:66-92): the argument is ignored, the variant is this fairy's {@code fairy_type}. */
    public ResourceLocation getTexture(final Fairy a) {
        if (this.fairy_type == 8) {
            return Fairy.texture8;
        }
        if (this.fairy_type == 7) {
            return Fairy.texture7;
        }
        if (this.fairy_type == 6) {
            return Fairy.texture6;
        }
        if (this.fairy_type == 5) {
            return Fairy.texture5;
        }
        if (this.fairy_type == 4) {
            return Fairy.texture4;
        }
        if (this.fairy_type == 3) {
            return Fairy.texture3;
        }
        if (this.fairy_type == 2) {
            return Fairy.texture2;
        }
        if (this.fairy_type == 1) {
            return Fairy.texture1;
        }
        return Fairy.texture0;
    }

    /** {@code entityInit} (:94-97). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FAIRY_TYPE, 0);
    }

    /**
     * {@code setOwner} (:99-108): only a player, remembered by {@code getDisplayName()}.
     *
     * <p>PORT: the 20.3 jar calls the Forge-added {@code EntityPlayer.getDisplayName()} (no SRG name), which returns
     * the {@code NameFormat} event result - by default the plain username without team formatting. 1.21.1's
     * {@code getDisplayName()} adds team prefix/suffix, so the plain profile name is stored instead; it matches the
     * lookup in {@link #getPlayerEntityByName}.
     */
    public void setOwner(@Nullable final LivingEntity e) {
        Player p = null;
        if (e != null && e instanceof Player) {
            p = (Player) e;
            final String s = p.getGameProfile().getName();
            if (s != null) {
                this.myowner = s;
            }
        }
    }

    /** {@code getBlink} (:110-115): the lightmap block coordinate of the body, on for the first half period. */
    public float getBlink() {
        if (this.blinker < this.my_blink / 2) {
            return 240.0f;
        }
        return 0.0f;
    }

    /** {@code attackEntityAsMob} (:117-123): a flat 2.0 mob hit, nothing on Peaceful. */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 2.0f);
        return var4;
    }

    /** {@code getTotalArmorValue} (:125-127) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return 4;
    }

    /** {@code getSoundVolume} (:129-131). */
    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    /** {@code getSoundPitch} (:133-135), a constant. */
    @Override
    public float getVoicePitch() {
        return 1.7f;
    }

    /** {@code getLivingSound} (:137-139). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /**
     * {@code getHurtSound} (:141-143) names {@code orespawn:rat_hit}, an event the jar's {@code sounds.json} does not
     * define (the rat's is {@code rathit}) - silent in 1.7.10, silent here (R18, unknown sound names).
     */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:145-147). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.BIG_SPLAT.get();
    }

    /** {@code canBePushed} (:149-151). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:153-154). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code collideWithNearbyEntities} (:156-157). */
    @Override
    protected void pushEntities() {
    }

    /** {@code mygetMaxHealth} (:159-161). */
    public int mygetMaxHealth() {
        return 40;
    }

    /**
     * {@code getDropItem} (:163-165) = the Crystal Torch item, through the inherited 1.7.10
     * {@code EntityLiving.dropFewItems}: {@code rand(3)} torches, plus {@code rand(looting + 1)} when a player killed
     * it with Looting, each as its own stack (R10). The equipment drop of {@code onDeath} is the super call.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        int looting = 0;
        if (damageSource.getEntity() instanceof Player killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        final Item item = ModItems.CRYSTAL_TORCH.get();
        int j = this.random.nextInt(3);
        if (looting > 0) {
            j += this.random.nextInt(looting + 1);
        }
        for (int k = 0; k < j; ++k) {
            this.spawnAtLocation(new ItemStack(item, 1));
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code onUpdate} (:171-196): damp the fall, blink, sync the type every 10 ticks (the client reads, the server
     * writes), and at night ({@code worldTime % 24000 >= 12000}) sparkle below the glowing fairy on the client with
     * 1 in 5 per tick.
     */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.600000023841, 1.0));
        ++this.blinker;
        if (this.blinker > this.my_blink) {
            this.blinker = 0;
        }
        --this.force_sync;
        if (this.force_sync < 0) {
            this.force_sync = 10;
            if (this.level().isClientSide) {
                this.fairy_type = this.entityData.get(DATA_FAIRY_TYPE);
            } else {
                this.entityData.set(DATA_FAIRY_TYPE, this.fairy_type);
            }
        }
        long t = this.level().getDayTime();
        t %= 24000L;
        if (t < 12000L) {
            return;
        }
        if (this.level().isClientSide && this.level().random.nextInt(5) == 0 && this.getBlink() > 1.0f) {
            final RandomSource rand = this.level().random;
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() - 0.15000000596046448, this.getZ(),
                    (double) ((rand.nextFloat() - rand.nextFloat()) / 8.0f), (double) (-rand.nextFloat() / 8.0f),
                    (double) ((rand.nextFloat() - rand.nextFloat()) / 8.0f));
        }
    }

    /** {@code writeEntityToNBT} (:198-205), including the in-memory {@code "null"} owner. */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        if (this.myowner == null) {
            this.myowner = "null";
        }
        par1NBTTagCompound.putString("MyOwner", this.myowner);
        par1NBTTagCompound.putInt("FairyType", this.fairy_type);
    }

    /** {@code readEntityFromNBT} (:207-214): reads {@code fairyType}, which is never written - always 0 (R18, 1:1). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.myowner = par1NBTTagCompound.getString("MyOwner");
        if (this.myowner != null && this.myowner.equals("null")) {
            this.myowner = null;
        }
        this.fairy_type = par1NBTTagCompound.getInt("fairyType");
    }

    /**
     * {@code isSuitableTarget} (:216-218): not on Peaceful, alive, seen, and an {@code EntityMob} - in 1.21.1 a
     * {@link Monster}. PORT: 1.21.1 draws the hierarchy slightly differently (Slime, Ghast and Phantom were never
     * {@code EntityMob} and are no {@code Monster} either; Silverfish, Endermite, Blaze, Witch, Wither are both).
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        return this.level().getDifficulty() != Difficulty.PEACEFUL && par1EntityLiving != null && par1EntityLiving != this
                && par1EntityLiving.isAlive() && this.getSensing().hasLineOfSight(par1EntityLiving)
                && par1EntityLiving instanceof Monster;
    }

    /** {@code findSomethingToAttack} (:220-237): living entities within 8, sorted by {@link GenericTargetSorter}. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0, 8.0, 8.0)));
        var5.sort(this.TargetSorter);
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /** {@code canSeeTarget} (:239-241): no block between a point 0.25 above the feet and the target. */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.25, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:243-303) after the task update ({@code super.updateAITasks}): a new visible air target
     * with 1/200 or when within {@code distSq < 2.5}; otherwise with 1/12 hunt a monster (strike within
     * {@code distSq < 6}); otherwise follow the owner (move to them beyond 8, teleport beyond 16). Heal 1 with 1/250,
     * then steer 0.2/0.7/0.2 towards the target with the yaw following at a quarter. All rolls use the world's random.
     * {@code (int)} coordinate casts are {@code Mth.floor} (R20).
     */
    @Override
    protected void customServerAiStep() {
        int keep_trying = 25;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        final RandomSource rand = this.level().random;
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (rand.nextInt(200) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.5f) {
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0; --keep_trying) {
                int zdir = rand.nextInt(8);
                int xdir = rand.nextInt(8);
                if (rand.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (rand.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(Mth.floor(this.getX()) + xdir, Mth.floor(this.getY()) + rand.nextInt(5) - 2,
                        Mth.floor(this.getZ()) + zdir);
                bid = this.level().getBlockState(this.currentFlightTarget);
                if (bid.isAir() && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                        this.currentFlightTarget.getZ())) {
                    bid = Blocks.STONE.defaultBlockState();
                }
            }
        } else if (rand.nextInt(12) == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < 6.0) {
                    this.doHurtTarget(e);
                }
            }
        } else if (this.myowner != null) {
            final Player p = this.getPlayerEntityByName(this.myowner);
            if (p != null) {
                if (this.distanceToSqr(p) > 64.0) {
                    this.currentFlightTarget.set(Mth.floor(p.getX()) + rand.nextInt(3) - rand.nextInt(3), Mth.floor(p.getY() + 1.0),
                            Mth.floor(p.getZ()) + rand.nextInt(3) - rand.nextInt(3));
                }
                if (this.distanceToSqr(p) > 256.0) {
                    this.setPos(p.getX() + rand.nextFloat() - rand.nextFloat(), p.getY(), p.getZ() + rand.nextFloat() - rand.nextFloat());
                }
            }
        }
        if (rand.nextInt(250) == 1) {
            this.heal(1.0f);
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.2 - motion.x) * 0.1;
        final double motionY = motion.y + (Math.signum(var2) * 0.699999988079071 - motion.y) * 0.1;
        final double motionZ = motion.z + (Math.signum(var3) * 0.2 - motion.z) * 0.1;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.2f;
        this.setYRot(this.getYRot() + var5 / 4.0f);
    }

    /**
     * {@code World.getPlayerEntityByName} ({@code ahb.a(String)}): the first player of this world whose command-sender
     * name - the plain account name - equals the string.
     */
    @Nullable
    private Player getPlayerEntityByName(final String name) {
        for (final Player player : this.level().players()) {
            if (name.equals(player.getGameProfile().getName())) {
                return player;
            }
        }
        return null;
    }

    /** {@code canTriggerWalking} (:305-307) {@code false}; see {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} and {@code updateFallState} (:309-313). */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:315-317). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * {@code renderDistanceWeight = 3.0} (:52) in 1.7.10 {@code isInRangeToRenderDist}: the average edge length x 64 x
     * weight. 1.21.1's global view scale stays a factor on top.
     */
    @Override
    public boolean shouldRenderAtSqrDistance(final double distance) {
        double d0 = this.getBoundingBox().getSize();
        if (Double.isNaN(d0)) {
            d0 = 1.0;
        }
        d0 *= 64.0 * getViewScale() * 3.0;
        return distance < d0 * d0;
    }

    /**
     * {@code getCanSpawnHere} (:319-330) as the placement predicate: at least 6 air blocks in the 3x3 at foot height and
     * Y ≥ 50. No spawner exception in this class.
     */
    public static boolean checkFairySpawnRules(final EntityType<Fairy> type, final ServerLevelAccessor level,
                                               final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        int sc = 0;
        for (int k = -1; k <= 1; ++k) {
            for (int j = -1; j <= 1; ++j) {
                final BlockState bid = level.getBlockState(new BlockPos(pos.getX() + j, pos.getY(), pos.getZ() + k));
                if (bid.isAir()) {
                    ++sc;
                }
            }
        }
        return sc >= 6 && pos.getY() >= 50.0;
    }

    /** The whole of {@code getCanSpawnHere} is {@link #checkFairySpawnRules}; nothing to add on the instance. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code canDespawn} (:335-337); persistence is checked by {@code Mob.checkDespawn} first. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return this.myowner == null;
    }
}
