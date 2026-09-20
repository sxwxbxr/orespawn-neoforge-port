package com.swbr.orespawn.entity.insect;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.util.Ignoreable;
import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.EntityButterfly} (EntityButterfly.java:16-318), id {@code butterfly}.
 * A fluttering {@code EntityAmbientCreature} and the portal to the Chaos dimension: a right click with
 * an empty hand sends the player there, and from Chaos back to the Overworld. Type 1 in the Islands
 * dimension is the vampire butterfly that nibbles players and horses. Superclass of
 * {@link EntityLunaMoth} and of Mothra (W08) - kept non-final, the per-kind differences are the public
 * hooks {@link #getTexture()} and {@link #hasGlowPass()}.
 *
 * <p>Not carried over: {@code attack_delay} (:28, never read) and {@code initCreature} (:294, empty).
 * {@code canDespawn = !isNoDespawnRequired()} (:92-94) is exactly 1.21.1's default persistence rule and
 * needs no override. {@code applyEntityAttributes} registered {@code attackDamage} (:50) because
 * {@code EntityAmbientCreature} lacked it - {@link #createAttributes()} does the same.
 */
public class EntityButterfly extends AmbientCreature implements Ignoreable, NoStepTrigger {

    /** DataWatcher 20: {@code butterfly_type} 0..3 (:89). */
    private static final EntityDataAccessor<Integer> DATA_BUTTERFLY_TYPE =
            SynchedEntityData.defineId(EntityButterfly.class, EntityDataSerializers.INT);

    // The nine textures of :307-317 (manifest texture_map, lower case under textures/entity/).
    public static final ResourceLocation texture1 = texture("butterfly.png");
    public static final ResourceLocation texture2 = texture("butterfly2.png");
    public static final ResourceLocation texture3 = texture("butterfly3.png");
    public static final ResourceLocation texture4 = texture("butterfly4.png");
    public static final ResourceLocation texture5 = texture("eyemoth.png");
    public static final ResourceLocation texture6 = texture("lunamoth.png");
    public static final ResourceLocation texture7 = texture("darkmoth.png");
    public static final ResourceLocation texture8 = texture("firemoth.png");
    public static final ResourceLocation texture9 = texture("vbutterfly1.png");

    private final GenericTargetSorter TargetSorter;
    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    /**
     * Constructor (:33-44). {@code setSize(0.4f, 0.4f)} is the entity type's size.
     *
     * <p>PORT: the type is rolled from {@code OreSpawnRand} on the server only. The original also rolled
     * it on the client and showed that private roll until the 25-tick watcher sync (:220-233) overwrote
     * it; with synced data the client shows the server's type from the first frame (R18 case 4,
     * catalogue verhalten/entity-07.md "der 25-Tick-Sync entfällt").
     */
    public EntityButterfly(final EntityType<? extends EntityButterfly> type, final Level level) {
        super(type, level);
        if (!level.isClientSide) {
            this.setButterflyType(OreSpawn.OreSpawnRand.nextInt(4));
        }
        // getNavigator().setAvoidsWater(true) (:42)
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.TargetSorter = new GenericTargetSorter(this);
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    private static ResourceLocation texture(final String file) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + file);
    }

    /** {@code applyEntityAttributes} (:46-52): health {@link #mygetMaxHealth()} = 2, speed 0.1, attack 0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code entityInit} (:87-90): DataWatcher 20 starts at 0, as the field was still 0 when Entity's constructor ran. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BUTTERFLY_TYPE, 0);
    }

    /** The public field {@code butterfly_type}. */
    public int getButterflyType() {
        return this.entityData.get(DATA_BUTTERFLY_TYPE);
    }

    public void setButterflyType(final int butterfly_type) {
        this.entityData.set(DATA_BUTTERFLY_TYPE, butterfly_type);
    }

    /**
     * {@code getTexture(EntityButterfly)} (:54-85), read by {@code RenderButterfly}. The
     * {@code instanceof Mothra} and {@code instanceof EntityLunaMoth} arms of the original are the
     * overrides in those subclasses; this is the butterfly arm. Type 1 in the Islands dimension is
     * {@code vbutterfly1.png} - the client knows its level's dimension.
     */
    public ResourceLocation getTexture() {
        if (this.getButterflyType() == 1) {
            if (this.level().dimension().equals(InsectSupport.DIMENSION_ISLANDS)) {
                return texture9;
            }
            return texture2;
        }
        if (this.getButterflyType() == 2) {
            return texture3;
        }
        if (this.getButterflyType() == 3) {
            return texture4;
        }
        return texture1;
    }

    /**
     * Whether {@code RenderButterfly.shouldRenderPass} drew the scrolling creeper-armor pass (:42-81):
     * Mothra always, a moth of {@code moth_type} 0, never a butterfly. Overridden by the subclasses.
     */
    public boolean hasGlowPass() {
        return false;
    }

    /** {@code getSoundVolume} (:96-98). */
    @Override
    protected float getSoundVolume() {
        return 0.0f;
    }

    /** {@code getSoundPitch} (:100-102). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:104-106). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:108-110). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:112-114). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:116-118): always, not only while alive. */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:120-121). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code collideWithNearbyEntities} (:123-124). */
    @Override
    protected void pushEntities() {
    }

    /** {@code mygetMaxHealth} (:126-128). */
    public int mygetMaxHealth() {
        return 2;
    }

    /**
     * {@code updateAITasks} (:134-168). Runs where 1.7.10 ran it, after the goal and navigation machinery
     * of the tick (the {@code super.updateAITasks()} of :139 is {@code Mob.serverAiStep} calling this).
     */
    @Override
    protected void customServerAiStep() {
        int keep_trying = 25;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            // R20: Mth.floor for the (int) casts of every coordinate below.
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.random.nextInt(100) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 4.0f) {
            // bid != Blocks.air: 1.7.10 had a single air block; isAir() also covers cave and void air.
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0;
                    bid = this.level().getBlockState(this.currentFlightTarget), --keep_trying) {
                this.currentFlightTarget.set(
                        Mth.floor(this.getX()) + this.random.nextInt(7) - this.random.nextInt(7),
                        Mth.floor(this.getY()) + this.random.nextInt(6) - 2,
                        Mth.floor(this.getZ()) + this.random.nextInt(7) - this.random.nextInt(7));
            }
        } else if (this.random.nextInt(10) == 0
                && this.level().dimension().equals(InsectSupport.DIMENSION_ISLANDS)
                && this.getButterflyType() == 1
                && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            LivingEntity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                this.currentFlightTarget.set(Mth.floor(e.getX()), Mth.floor(e.getY() + 1.0), Mth.floor(e.getZ()));
                if (this.distanceToSqr(e) < 6.0) {
                    this.doHurtTarget(e);
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.5 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.5 - motion.x) * 0.10000000149011612;
        final double motionY = motion.y + (Math.signum(var2) * 0.699999988079071 - motion.y) * 0.10000000149011612;
        final double motionZ = motion.z + (Math.signum(var3) * 0.5 - motion.z) * 0.10000000149011612;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.5f;
        this.setYRot(this.getYRot() + var5);
    }

    /**
     * {@code attackEntityAsMob} (:170-179): a coin flip on the shared {@code OreSpawnRand}, never on
     * Peaceful, then 1.0 mob damage. Replaces the vanilla attribute-based attack completely.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        if (OreSpawn.OreSpawnRand.nextInt(2) != 0) {
            return false;
        }
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        final boolean var4 = par1Entity.hurt(this.damageSources().mobAttack(this), 1.0f);
        return var4;
    }

    /**
     * {@code isSuitableTarget} (:181-202): alive, visible, a player out of creative mode or a horse.
     *
     * <p>PORT: 1.7.10 {@code EntityHorse} was one class for horse, donkey, mule, skeleton and zombie
     * horse; those five 1.21.1 classes are listed, the later llamas and camels are not.
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
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par1EntityLiving instanceof Player) {
            final Player p = (Player) par1EntityLiving;
            return !p.getAbilities().instabuild;
        }
        return par1EntityLiving instanceof Horse || par1EntityLiving instanceof Donkey || par1EntityLiving instanceof Mule
                || par1EntityLiving instanceof SkeletonHorse || par1EntityLiving instanceof ZombieHorse;
    }

    /** {@code findSomethingToAttack} (:204-218): every living entity in {@code expand(8, 5, 8)}, nearest first. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        final List<LivingEntity> var5 = new ArrayList<>(
                this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0, 5.0, 8.0)));
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
     * {@code onUpdate} (:220-233): damp the vertical motion every tick.
     *
     * <p>PORT: the {@code force_sync} counter that copied DataWatcher 20 every 25 ticks is gone; the
     * synced data accessor carries the type itself (see the constructor).
     */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6000000238418579, 1.0));
    }

    /**
     * {@code canTriggerWalking} (:235-237) {@code false}: no step sounds, no step events; the
     * {@link NoStepTrigger} marker keeps OreSpawn's {@code stepOn} blocks quiet as well (R20).
     */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} and {@code updateFallState} (:239-243), both empty: no fall distance, no fall damage. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:245-247). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * {@code interact} (:249-271): a server player with an empty hand travels to Chaos, or home to the
     * Overworld when already in Chaos. The client side answers {@code false} like the original (not an
     * {@code EntityPlayerMP}).
     *
     * <p>PORT: 1.21.1 asks the main hand and then the off hand; 1.7.10 knew only the held item. Only the
     * main-hand call acts, and it looks at the selected hotbar stack, so an empty off hand never sends a
     * player who holds something. An emptied stack reads as empty, which replaces the explicit
     * {@code stackSize <= 0} clean-up of :257-260.
     */
    @Override
    protected InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        if (par1EntityPlayer == null) {
            return InteractionResult.PASS;
        }
        if (!(par1EntityPlayer instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        final ItemStack var2 = par1EntityPlayer.getInventory().getSelected();
        if (!var2.isEmpty()) {
            return InteractionResult.PASS;
        }
        final ServerPlayer player = (ServerPlayer) par1EntityPlayer;
        if (!player.level().dimension().equals(InsectSupport.DIMENSION_CHAOS)) {
            this.travel(player, InsectSupport.DIMENSION_CHAOS);
        } else {
            this.travel(player, Level.OVERWORLD);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * {@code transferPlayerToDimension(player, dim, new OreSpawnTeleporter(worldServerForDimension(dim), dim, worldObj))}
     * (:265, :268). The landing search, the ±0.5 offset and the pets that travel along are the teleporter's
     * (w05-teleport-ants). One call site, so a different helper signature changes one line.
     */
    protected void travel(final ServerPlayer player, final ResourceKey<Level> dimension) {
        OreSpawnTeleporter.transferPlayerToDimension(player, dimension, this.level());
    }

    /**
     * {@code getCanSpawnHere} (:273-292) on the instance, which natural spawning and mob spawners both
     * ask - including its side effect: a {@code "Butterfly"} spawner nearby makes this a type-1 butterfly.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        final int posX = Mth.floor(this.getX());
        final int posY = Mth.floor(this.getY());
        final int posZ = Mth.floor(this.getZ());
        if (InsectSupport.spawnerNearby(level, posX, posY, posZ, BUTTERFLY_SPAWNER_ID)) {
            this.setButterflyType(1);
            return true;
        }
        final BlockState bid = level.getBlockState(new BlockPos(posX, posY, posZ));
        return bid.isAir() && InsectSupport.isDaytime(this.level())
                && (this.level().dimension().equals(InsectSupport.DIMENSION_ISLANDS) || this.getY() >= 50.0);
    }

    /**
     * The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test,
     * which 1.21.1 asks separately.
     */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code "Butterfly"} as a spawner's entity id. */
    private static final String BUTTERFLY_SPAWNER_ID = OreSpawn.MOD_ID + ":butterfly";

    /**
     * {@code getCanSpawnHere} (:273-292) as the placement predicate, which 1.21.1 asks before the entity
     * exists (natural spawning and spawners without custom rules). Same rule without the side effect; the
     * instance check above sets the type afterwards. The mob stands at the block centre, so the floored
     * position is the block position.
     */
    public static boolean checkButterflySpawnRules(final EntityType<EntityButterfly> type, final ServerLevelAccessor level,
                                                   final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (InsectSupport.spawnerNearby(level, pos.getX(), pos.getY(), pos.getZ(), BUTTERFLY_SPAWNER_ID)) {
            return true;
        }
        final ServerLevel world = level.getLevel();
        return level.getBlockState(pos).isAir() && InsectSupport.isDaytime(world)
                && (world.dimension().equals(InsectSupport.DIMENSION_ISLANDS) || pos.getY() >= 50.0);
    }

    /** {@code writeEntityToNBT} (:297-300). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("ButterflyType", this.getButterflyType());
    }

    /** {@code readEntityFromNBT} (:302-305). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.setButterflyType(par1NBTTagCompound.getInt("ButterflyType"));
    }
}
