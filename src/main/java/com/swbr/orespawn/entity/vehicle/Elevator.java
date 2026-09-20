package com.swbr.orespawn.entity.vehicle;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.Ignoreable;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Port of {@code danger.orespawn.Elevator} (Elevator.java:17-631), registry id {@code hoverboard}
 * ("Hoverboard", OreSpawnMain.java:3539/:3543: tracking 128, update frequency 1, velocity updates).
 * A rideable hover board placed by {@code ItemElevator}: it floats over any non-air block, the
 * rider steers with the look direction and accelerates with the forward key, the fly-up key
 * raises the top speed from 0.85 to 1.85, and a hard crash (horizontal collision above 0.75)
 * breaks it into sticks and two diamonds. Ten colours, cycled by right-clicking with the
 * Ultimate Sword. Every target search skips it ({@link Ignoreable}, MyUtils.java:17-19).
 *
 * <h2>Where the physics run</h2>
 * The original moved the board on the server only ({@code onUpdate}, :367-526); the client
 * interpolated towards the tracker packets (:327-366, {@code setPositionAndRotation2}) and, while a
 * local player rode, re-sent that player's look and movement input (:339-343,
 * {@code EntityClientPlayerMP} - a client class in common code that a dedicated server cannot load,
 * catalogue 5.3).
 *
 * <p>PORT: the wave plan named {@code tickRidden}/{@code getRiddenInput} for the riding code. Those
 * run inside {@code LivingEntity.travel}, which 1.21.1 calls on whichever side
 * {@code isControlledByLocalInstance()} names - for a player rider that is the rider's <b>client</b>,
 * with the server only accepting the reported position. The fly-up key state of R15 lives on the
 * server ({@code RiderKeys}, set by the payload), and the crash drops, grass trampling, explosion
 * roll and entity pushing of :371-525 are server work. So the port keeps the original split
 * instead: {@link #tick} is the whole {@code onUpdate} without {@code super} (as the original),
 * {@link #getControllingPassenger} is {@code null} so no client predicts the board, and the rider
 * input is read on the server from {@code Player.zza} and {@code getYRot()}. Vanilla 1.21.1 already
 * sends exactly what :341-342 sent by hand - {@code LocalPlayer.tick} sends
 * {@code ServerboundMovePlayerPacket.Rot} and {@code ServerboundPlayerInputPacket} every tick while
 * the player is a passenger (LocalPlayer.java:238-240), applied by
 * {@code ServerGamePacketListenerImpl} (:889-891) and {@code ServerPlayer.setPlayerInput}
 * (:1247-1259). Nothing is left to replace; the fly-up key comes from
 * {@link RiderKeys#isFlyUp(Entity)} (R15).
 *
 * <h2>1:1 details worth knowing</h2>
 * <ul>
 *   <li>{@code attackEntityFrom}, {@code writeEntityToNBT} and {@code readEntityFromNBT} never call
 *       {@code super}: health never drops, only the synced damage counter does, and nothing but
 *       {@code HoverColor} is saved (Entity base data aside).</li>
 *   <li>{@code onLivingpdate} (:225-230) is misspelt and overrides nothing; it never ran and is not
 *       ported.</li>
 *   <li>{@code getTrackingRange} 128 / {@code getUpdateFrequency} 10 (:118-124) were not used by the
 *       registration (entity-07.md); the entity type takes 128 / 1 / true from
 *       OreSpawnMain.java:3543.</li>
 *   <li>{@code damage_counter} (:28, :45) is written once and never read.</li>
 * </ul>
 */
public class Elevator extends Mob implements Ignoreable, NoStepTrigger {

    /** DataWatcher 22 (:142, :587-593): ticks since the last hit, default 0. */
    private static final EntityDataAccessor<Integer> TIME_SINCE_HIT =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    /** DataWatcher 23 (:143, :595-601): wobble direction, default 1. */
    private static final EntityDataAccessor<Integer> FORWARD_DIRECTION =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    /** DataWatcher 24 (:144, :579-585): damage taken, default 0. */
    private static final EntityDataAccessor<Float> DAMAGE_TAKEN =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.FLOAT);
    /** DataWatcher 20 (:145, :603-609): explosion countdown, default 0. */
    private static final EntityDataAccessor<Integer> EXPLODING =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);
    /** DataWatcher 21 (:146, :611-617): colour 1..10. The watcher starts at 0 (texture 1 by the
     *  {@code default} branch) while the field {@link #color} starts at 1 - as in the original. */
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(Elevator.class, EntityDataSerializers.INT);

    /** {@code texture1..10} (:32-41, :619-630), moved by the asset generator to {@code textures/entity/}. */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[10];

    static {
        for (int n = 1; n <= 10; n++) {
            TEXTURES[n - 1] = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/elevator" + n + ".png");
        }
    }

    // Client-side interpolation state (:19-27), filled by lerpTo / lerpMotion.
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    @SuppressWarnings("unused")
    private int damage_counter;
    private int exploding;
    private int color;
    private int playing;

    /**
     * {@code Elevator(World)} (:43-51): size 1.25 x 1.0 comes from the entity type;
     * {@code func_110163_bv} in {@code entityInit} (:147) is {@code enablePersistence}.
     */
    public Elevator(final EntityType<? extends Elevator> type, final Level par1World) {
        super(type, par1World);
        this.damage_counter = 100;
        this.exploding = 0;
        this.color = 1;
        this.playing = 0;
        this.setPersistenceRequired();
    }

    /** {@code Elevator(World, x, y, z)} (:53-62); {@code yOffset} is 0 for this entity. */
    public Elevator(final EntityType<? extends Elevator> type, final Level par1World,
                    final double par2, final double par4, final double par6) {
        this(type, par1World);
        this.setPos(par2, par4, par6);
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.xo = par2;
        this.yo = par4;
        this.zo = par6;
    }

    /** {@code getTexture} (:64-100): colour 1..10, anything else texture 1. */
    public ResourceLocation getTexture() {
        final int c = this.getColor();
        if (c >= 1 && c <= 10) {
            return TEXTURES[c - 1];
        }
        return TEXTURES[0];
    }

    /**
     * {@code applyEntityAttributes} (:102-108): health 60, speed 1.33 (float literal widened),
     * attack damage registered at 0.
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} constructor value
     * ({@code sv.<init>}: {@code ldc 0.5f; putfield W} = {@code stepHeight}, client-1.7.10.jar via
     * javap); 1.21.1 defaults living entities to 0.6.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 1.3300000429153442)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code canDespawn} (:110-112). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code shouldRiderSit} (:114-116): the rider stands on the board. */
    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    /** {@code fall} (:130-131): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:133-134): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * 1.7.10's {@code moveEntity} had no block speed factor; soul sand slowed an entity only from
     * {@code onEntityCollidedWithBlock}, i.e. while its box overlapped the block. 1.21.1's {@code move}
     * multiplies the motion by the factor of the block half a block below the feet, which a ridden board
     * hovering 0.25-1.25 above soul sand or honey would hit for half its hover band. Returning 1 keeps
     * {@code moveEntity}'s motion (the port's reading of :499).
     *
     * <p>PORT: the overlap slow-down of 1.7.10 soul sand is gone with it; 1.21.1 soul sand has no
     * {@code entityInside} to carry it, and the board's hover height keeps its box above the block.
     */
    @Override
    protected float getBlockSpeedFactor() {
        return 1.0f;
    }

    /**
     * {@code canTriggerWalking} (:136-138): no step sounds. The other half, no {@code onEntityWalking},
     * is {@link NoStepTrigger}: 1.21.1 calls {@code Block.stepOn} regardless of this value (R20).
     */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code entityInit} (:140-148), slots in the original order 22, 23, 24, 20, 21. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TIME_SINCE_HIT, 0);
        builder.define(FORWARD_DIRECTION, 1);
        builder.define(DAMAGE_TAKEN, 0.0f);
        builder.define(EXPLODING, 0);
        builder.define(COLOR, 0);
    }

    /** {@code canBePushed} (:150-152). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code getMountedYOffset} (:154-156). */
    public double getMountedYOffset() {
        return 0.5;
    }

    /**
     * No rider controls the board through 1.21.1's vehicle prediction - the server moves it
     * (see the class comment). {@code Mob} would otherwise hand control to a {@code Mob} rider.
     */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    /**
     * {@code attackEntityFrom} (:158-184), without {@code super}. A ridden board only takes player
     * hits; suffocation never counts. Each hit flips the wobble, adds {@code damage * 10}; a creative
     * player or more than 40 breaks it, dropping the item unless creative.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final boolean p = par1DamageSource.getEntity() instanceof Player;
        final Entity rider = this.getFirstPassenger();
        if (rider != null && !p) {
            return false;
        }
        if (par1DamageSource.is(DamageTypes.IN_WALL)) {
            return false;
        }
        if (!this.level().isClientSide && !this.isRemoved()) {
            this.setForwardDirection(-this.getForwardDirection());
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + par2 * 10.0f);
            this.markHurt();
            final boolean flag = par1DamageSource.getEntity() instanceof Player player
                    && player.getAbilities().instabuild;
            if (flag || this.getDamageTaken() > 40.0f) {
                if (rider != null) {
                    // riddenByEntity.mountEntity(this) (:174): in 1.7.10 mounting the entity one
                    // already rides just mounts it again (sa.a(Lsa;)V has no toggle, checked with
                    // javap), and startRiding on the current vehicle is a no-op in 1.21.1 too. The
                    // rider comes off through discard() below, as the original's did through setDead.
                    rider.startRiding(this);
                }
                if (!flag) {
                    this.spawnAtLocation(ModItems.ELEVATOR.get());
                }
                this.discard();
            }
            return true;
        }
        return true;
    }

    /**
     * {@code performHurtAnimation} (:186-191), the client-side boat prediction. Nothing in the port
     * sends the hurt animation for this entity - as in 1.7.10, the synced watchers carry the wobble.
     */
    @Override
    public void animateHurt(final float yaw) {
        this.setForwardDirection(-this.getForwardDirection());
        this.setTimeSinceHit(10);
        this.setDamageTaken(this.getDamageTaken() * 11.0f);
    }

    /** {@code canBeCollidedWith} (:193-195): can be targeted and hit. */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /**
     * {@code setPositionAndRotation2} (:197-213): 1.21.1 calls {@code lerpTo} with 3 steps for every
     * move and teleport packet, as 1.7.10 did. A ridden board interpolates over {@code steps + 8}
     * ticks, an empty one over 6.
     */
    @Override
    public void lerpTo(final double par1, final double par3, final double par5, final float par7, final float par8,
                       final int par9) {
        if (this.getFirstPassenger() != null) {
            this.boatPosRotationIncrements = par9 + 8;
        } else {
            this.boatPosRotationIncrements = 6;
        }
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
        this.setDeltaMovement(this.velocityX, this.velocityY, this.velocityZ);
    }

    /** The pending interpolation target, for rotation-only packets (1.7.10 used the tracked position). */
    @Override
    public double lerpTargetX() {
        return this.boatPosRotationIncrements > 0 ? this.boatX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.boatPosRotationIncrements > 0 ? this.boatY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.boatPosRotationIncrements > 0 ? this.boatZ : this.getZ();
    }

    /** 1.7.10 passed the current {@code rotationYaw}/{@code rotationPitch} when a packet had no rotation. */
    @Override
    public float lerpTargetYRot() {
        return this.getYRot();
    }

    @Override
    public float lerpTargetXRot() {
        return this.getXRot();
    }

    /** {@code setVelocity} (:215-223), client only. */
    @Override
    public void lerpMotion(final double par1, final double par3, final double par5) {
        this.setDeltaMovement(par1, par3, par5);
        this.velocityX = par1;
        this.velocityY = par3;
        this.velocityZ = par5;
    }

    /**
     * {@code onUpdate} (:232-527), without {@code super} like the original: no vanilla living tick,
     * no AI, no fluid pushing, no potion ticking, no void kill. 1.21.1 sets the previous position and
     * rotation before calling this ({@code setOldPosAndRot}), as 1.7.10's
     * {@code updateEntityWithOptionalForce} did.
     */
    @Override
    public void tick() {
        List<Entity> list = null;
        final Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        final RandomSource rand = this.getRandom();
        final RandomSource worldRand = this.level().random;
        final double velocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
        double d6 = rand.nextFloat() * 2.0f - 1.0f;
        double d7 = (rand.nextInt(2) * 2 - 1) * 0.7;
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.85;
        double gh = 0.75;
        int dist = 2;
        if (this.isRemoved()) {
            return;
        }
        this.hasImpulse = true;
        if (this.getTimeSinceHit() > 0) {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
        }
        if (this.getDamageTaken() > 0.0f) {
            this.setDamageTaken(this.getDamageTaken() - 1.0f);
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        final Entity rider = this.getFirstPassenger();
        final double posX = this.getX();
        // posY is a local: the original wrote the field without the bounding box on the server
        // (:374, :401) and moveEntity rebuilt it from the box (see the server branch).
        double posY = this.getY();
        final double posZ = this.getZ();

        // Exhaust and splash particles (:255-295), both sides; a server discards addParticle as 1.7.10 did.
        if (velocity > 0.15 && rider != null) {
            final double d8 = Math.cos(Math.toRadians(this.getYRot() + 270.0f));
            final double d9 = Math.sin(Math.toRadians(this.getYRot() + 270.0f));
            BlockState bid = Blocks.AIR.defaultBlockState();
            int i;
            for (i = 1; i < 10; ++i) {
                bid = this.level().getBlockState(legacyPos(posX, Mth.floor(posY) - i, posZ));
                if (!bid.isAir()) {
                    break;
                }
            }
            for (int j = 0; j < 1.0 + velocity * 10.0; ++j) {
                d6 = rand.nextFloat() * 2.0f - 1.0f;
                d7 = (rand.nextInt(2) * 2 - 1) * 0.7;
                if (rand.nextBoolean()) {
                    final double d10 = posX - d8 * d6 * 0.8 + d9 * d7;
                    final double d11 = posZ - d9 * d6 * 0.8 - d8 * d7;
                    if (rand.nextBoolean()) {
                        this.level().addParticle(ParticleTypes.SMOKE, d10, posY - 0.25, d11, motionX, motionY, motionZ);
                    } else {
                        this.level().addParticle(reddust(motionX, motionY, motionZ), d10, posY - 0.25, d11, 0.0, 0.0, 0.0);
                    }
                } else {
                    final double d10 = posX + d8 + d9 * d6 * 0.7;
                    final double d11 = posZ + d9 - d8 * d6 * 0.7;
                    if (rand.nextBoolean()) {
                        this.level().addParticle(ParticleTypes.SMOKE, d10, posY - 0.225, d11, motionX, motionY, motionZ);
                    } else {
                        this.level().addParticle(reddust(motionX, motionY, motionZ), d10, posY - 0.225, d11, 0.0, 0.0, 0.0);
                    }
                }
                // Blocks.water || Blocks.flowing_water: one block with a level in 1.21.1.
                if (bid.is(Blocks.WATER)) {
                    for (int k = 0; k < 5; ++k) {
                        this.level().addParticle(ParticleTypes.SPLASH, posX + rand.nextFloat(), posY - i + 1.25,
                                posZ + rand.nextFloat(), motionX / 2.0, motionY + velocity, motionZ / 2.0);
                    }
                }
            }
        }

        // Hover hum (:296-302).
        if (this.playing > 0) {
            --this.playing;
        }
        if (rider != null && this.playing == 0 && worldRand.nextInt(80) == 1) {
            // "orespawn:hover": category "master" in the 20.3 jar's sounds.json.
            this.playSoundAtEntity(rider, ModSounds.HOVER.get(), SoundSource.MASTER, 0.45f, 1.0f);
            this.playing = 55;
        }

        // Random engine explosion at speed (:303-326).
        if (!this.level().isClientSide) {
            if (this.exploding > 0) {
                --this.exploding;
            }
            if (this.exploding == 0 && velocity > 0.65 && worldRand.nextInt(20000) == 1) {
                this.exploding = 45;
                this.playing = 50;
            }
            this.setExploding(this.exploding);
        } else {
            this.exploding = this.getExploding();
        }
        if (this.getExploding() > 0 && rider != null) {
            if (worldRand.nextInt(10) == 1) {
                // PORT: "random.explode" took its category from vanilla's 1.7.10 sounds.json, which is
                // not on this machine; BLOCKS is not verified.
                this.playSoundAtEntity(rider, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.55f,
                        0.75f + worldRand.nextFloat());
            }
            for (int i = 0; i < 15; ++i) {
                // "explode" = poof, "largeexplode" = explosion (xSpeed is its size factor in both
                // versions), "largesmoke" = large_smoke; positions truncated with (int) as in :321-324.
                this.level().addParticle(ParticleTypes.POOF,
                        (double) (int) (posX + (worldRand.nextFloat() - worldRand.nextFloat()) * 4.0f),
                        (double) (int) (posY + (worldRand.nextFloat() - worldRand.nextFloat()) * 4.0f),
                        (double) (int) (posZ + (worldRand.nextFloat() - worldRand.nextFloat()) * 4.0f),
                        motionX, 0.0, motionZ);
                this.level().addParticle(ParticleTypes.EXPLOSION,
                        (double) (int) (posX + (worldRand.nextFloat() - worldRand.nextFloat()) * 2.0f),
                        (double) (int) (posY + (worldRand.nextFloat() - worldRand.nextFloat()) * 2.0f),
                        (double) (int) (posZ + (worldRand.nextFloat() - worldRand.nextFloat()) * 2.0f),
                        motionX, 0.0, motionZ);
                this.level().addParticle(ParticleTypes.SMOKE,
                        (double) (int) (posX + (worldRand.nextFloat() - worldRand.nextFloat()) * 5.0f),
                        (double) (int) (posY + (worldRand.nextFloat() - worldRand.nextFloat()) * 5.0f),
                        (double) (int) (posZ + (worldRand.nextFloat() - worldRand.nextFloat()) * 5.0f),
                        motionX, 0.0, motionZ);
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        (double) (int) (posX + (worldRand.nextFloat() - worldRand.nextFloat()) * 3.0f),
                        (double) (int) (posY + (worldRand.nextFloat() - worldRand.nextFloat()) * 3.0f),
                        (double) (int) (posZ + (worldRand.nextFloat() - worldRand.nextFloat()) * 3.0f),
                        motionX, 0.0, motionZ);
            }
        }

        if (this.level().isClientSide) {
            clientUpdate(rider, posX, posY, posZ, motionX, motionY, motionZ, gh);
            return;
        }

        // ---- Server (:367-526) ----
        if (rider != null) {
            gh = 1.25;
        }
        BlockState bid = this.level().getBlockState(legacyPos(posX, Mth.floor((float) posY - (float) gh), posZ));
        if (!bid.isAir()) {
            motionY += 0.06;
            // this.posY += 0.1 (:374): a field write that moveEntity overwrote from the bounding box at
            // its end (client-1.7.10.jar, sa.d(DDD)V, putfield t at pc 1340). It only moved the block
            // lookups below, never the board - so it stays a local here.
            posY += 0.1;
            // Blocks.tallgrass held shrub, grass and fern by metadata; 1.21.1 splits it into
            // short_grass and fern (same reading as UltimateSword). The setBlock height uses the raised
            // posY and a double cast, the lookup above a float cast - both as in :375-379.
            if (isTallgrass(bid) && rider != null && worldRand.nextInt(200) == 1 && this.mobGriefing()) {
                this.level().setBlock(legacyPos(posX, Mth.floor(posY - gh), posZ), Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
            if (bid.is(Blocks.GRASS_BLOCK) && rider != null && worldRand.nextInt(200) == 1 && this.mobGriefing()) {
                this.level().setBlock(legacyPos(posX, Mth.floor(posY - gh), posZ), Blocks.DIRT.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        } else {
            motionY -= 0.01;
        }
        if (rider != null) {
            // PORT: (EntityPlayer) this.riddenByEntity (:386) threw for any other rider; 1.21.1's /ride
            // can seat a mob. A non-player rider reads as no forward input (R18 case 1).
            final Player pp2 = rider instanceof Player player ? player : null;
            obstruction_factor = 0.0;
            dist = 3;
            dist += (int) (velocity * 8.0);
            for (int k = 1; k < dist; ++k) {
                for (int i = 1; i < dist * 2; ++i) {
                    final double dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f));
                    final double dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f));
                    bid = this.level().getBlockState(legacyPos(posX + dx, Mth.floor(posY) - k, posZ + dz));
                    if (!bid.isAir()) {
                        obstruction_factor += 0.05;
                    }
                }
            }
            motionY += obstruction_factor * 0.11;
            posY += obstruction_factor * 0.11; // :401, overwritten by moveEntity like :374
            double d8 = rider.getYRot();
            d8 %= 360.0;
            while (d8 < 0.0) {
                d8 += 360.0;
            }
            double d9 = this.getYRot();
            d9 %= 360.0;
            while (d9 < 0.0) {
                d9 += 360.0;
            }
            relative_g = (d8 - d9) % 180.0;
            while (relative_g < 0.0) {
                relative_g += 180.0;
            }
            if (relative_g > 90.0) {
                relative_g -= 180.0;
            }
            if (velocity > 0.01) {
                d8 = 1.85 - velocity;
                d8 = Math.abs(d8);
                if (d8 < 0.01) {
                    d8 = 0.01;
                }
                if (d8 > 0.9) {
                    d8 = 0.9;
                }
                this.setYRot(rider.getYRot() + (float) (relative_g * d8));
            } else {
                this.setYRot(rider.getYRot());
            }
            relative_g = Math.abs(relative_g) * velocity;
            if (relative_g > 50.0) {
                relative_g = 0.0;
            }
            this.setXRot(10.0f * (float) velocity);
            this.setRot(this.getYRot(), this.getXRot());
            double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
            if (this.exploding != 0) {
                newvelocity -= 0.05;
                if (newvelocity < 0.0) {
                    newvelocity = 0.0;
                }
            }
            // rr = atan2(rider.motionZ, rider.motionX) (:437) and rt = 0 (:440) are never read.
            final double rhm = Math.atan2(motionZ, motionX);
            final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
            final double pi = 3.1415926545;
            double deltav = 0.0;
            final float im = pp2 != null ? pp2.zza : 0.0f; // moveForward
            // OreSpawnMain.flyup_keystate != 0 (:444): per rider (R15).
            if (RiderKeys.isFlyUp(rider)) {
                ++max_speed;
            }
            double rdv = Math.abs(rhm - rhdir) % (pi * 2.0);
            if (rdv > pi) {
                rdv -= pi * 2.0;
            }
            rdv = Math.abs(rdv);
            if (Math.abs(newvelocity) < 0.01) {
                rdv = 0.0;
            }
            if (rdv > 1.5) {
                newvelocity = -newvelocity;
            }
            if (Math.abs(im) > 0.001f) {
                if (im > 0.0f) {
                    deltav = 0.025;
                    if (max_speed > 1.0) {
                        deltav += 0.15;
                    }
                } else {
                    max_speed = 0.35;
                    deltav = -0.02;
                }
                newvelocity += deltav;
                if (newvelocity >= 0.0) {
                    if (newvelocity > max_speed) {
                        newvelocity = max_speed;
                    }
                    motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
                    motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
                } else {
                    if (newvelocity < -max_speed) {
                        newvelocity = -max_speed;
                    }
                    newvelocity = -newvelocity;
                    motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * newvelocity;
                    motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * newvelocity;
                }
            } else if (newvelocity >= 0.0) {
                motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
            } else {
                motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * (newvelocity * -1.0);
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * (newvelocity * -1.0);
            }
        } else if (rider == null) {
            motionX = 0.0;
            motionZ = 0.0;
        }
        // moveEntity (:499): 1.21.1's move zeroes the blocked motion components like 1.7.10's did.
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
        final Vec3 moved = this.getDeltaMovement();
        motionX = moved.x;
        motionY = moved.y;
        motionZ = moved.z;
        if (this.horizontalCollision && velocity > 0.75) {
            this.discard();
            for (int p = worldRand.nextInt(10), k = 0; k < 6 + p; ++k) {
                this.spawnAtLocation(Items.STICK);
            }
            for (int k = 0; k < 2; ++k) {
                this.spawnAtLocation(Items.DIAMOND);
            }
        } else {
            motionX *= 0.98;
            motionY *= 0.94;
            motionZ *= 0.98;
        }
        this.setDeltaMovement(motionX, motionY, motionZ);
        list = this.level().getEntities(this, this.getBoundingBox().inflate(0.25, 0.0, 0.25));
        if (list != null && !list.isEmpty()) {
            for (int l = 0; l < list.size(); ++l) {
                final Entity entity = list.get(l);
                // canBePushed -> isPushable. PORT: LivingEntity.isPushable also excludes spectators and
                // entities on a ladder; 1.7.10's EntityLivingBase only checked !isDead.
                if (entity != rider && entity.isPushable() && !(entity instanceof Girlfriend)
                        && !(entity instanceof Boyfriend)) {
                    applyEntityCollision(entity);
                }
            }
        }
        if (rider != null && rider.isRemoved()) {
            rider.stopRiding();
        }
    }

    /**
     * {@code entity.applyEntityCollision(this)} (:519), the 1.7.10 {@code Entity} version
     * ({@code sa.g(Lsa;)V}, javap on client-1.7.10.jar): push {@code entity} away from the board and
     * the board away from {@code entity}, 0.05 per tick at contact. Written out because 1.21.1's
     * {@code push(Entity)} skips the side that is a vehicle - a ridden board would never take the
     * counter-push - and adds {@code noPhysics}, same-vehicle and sleeping checks the original lacked.
     *
     * <ul>
     *   <li>{@code EntityMinecart} is the only 1.7.10 class overriding the method (joined.srg, the
     *       {@code func_70108_f} lines); its 1.21.1 successor {@link AbstractMinecart#push(Entity)}
     *       takes that call.</li>
     *   <li>{@code MathHelper.sqrt_double} returned a {@code float} ({@code qh.a(D)F}).</li>
     *   <li>{@code d *= 1.0F - entityCollisionReduction} is left out: no class in the 1.7.10 jar writes
     *       the field on an entity (the only {@code putfield Y:F} is in the unrelated {@code blt}), so
     *       it was always {@code * 1.0}, an exact no-op.</li>
     * </ul>
     */
    private void applyEntityCollision(final Entity entity) {
        if (entity instanceof AbstractMinecart) {
            entity.push(this);
            return;
        }
        // p_70108_1_ = this board, this = entity: riddenByEntity != entity && ridingEntity != entity.
        if (this.getFirstPassenger() == entity || this.getVehicle() == entity) {
            return;
        }
        double d0 = this.getX() - entity.getX();
        double d1 = this.getZ() - entity.getZ();
        double d2 = Mth.absMax(d0, d1);
        if (d2 >= 0.009999999776482582) {
            d2 = (double) (float) Math.sqrt(d2);
            d0 /= d2;
            d1 /= d2;
            double d3 = 1.0 / d2;
            if (d3 > 1.0) {
                d3 = 1.0;
            }
            d0 *= d3;
            d1 *= d3;
            d0 *= 0.05000000074505806;
            d1 *= 0.05000000074505806;
            entity.push(-d0, 0.0, -d1); // addVelocity: motion += v, isAirBorne = true
            this.push(d0, 0.0, d1);
        }
    }

    /** The client half of {@code onUpdate} (:327-366). */
    private void clientUpdate(@Nullable final Entity rider, final double posX, double posY, final double posZ,
                              double motionX, double motionY, double motionZ, final double gh) {
        if (rider == null) {
            final BlockState bid = this.level().getBlockState(legacyPos(posX, Mth.floor((float) posY - (float) gh), posZ));
            if (!bid.isAir()) {
                motionY += 0.06;
                posY += 0.07; // unlike the server this one counts: setPosition below reads it
                this.boatY += 0.07;
            } else {
                motionY -= 0.003;
            }
        }
        // else if (riddenByEntity instanceof EntityClientPlayerMP) { send C05 look + C0C input } (:339-343):
        // PORT: LocalPlayer.tick sends both packets itself while riding (class comment); nothing to do.
        if (this.boatPosRotationIncrements > 0) {
            final double d8 = posX + (this.boatX - posX) / this.boatPosRotationIncrements;
            final double d9 = posY + (this.boatY - posY) / this.boatPosRotationIncrements;
            final double d12 = posZ + (this.boatZ - posZ) / this.boatPosRotationIncrements;
            this.setPos(d8, d9, d12);
            this.setXRot(this.getXRot() + (float) ((this.boatPitch - this.getXRot()) / this.boatPosRotationIncrements));
            double d13 = Mth.wrapDegrees(this.boatYaw - this.getYRot());
            if (rider != null) {
                d13 = Mth.wrapDegrees(rider.getYRot() - (double) this.getYRot());
            }
            this.setYRot(this.getYRot() + (float) (d13 / this.boatPosRotationIncrements));
            this.setRot(this.getYRot(), this.getXRot());
            --this.boatPosRotationIncrements;
        } else {
            final double d8 = posX + motionX;
            final double d9 = posY + motionY;
            final double d12 = posZ + motionZ;
            this.setPos(d8, d9, d12);
            motionX *= 0.99;
            motionY *= 0.95;
            motionZ *= 0.99;
        }
        this.setDeltaMovement(motionX, motionY, motionZ);
    }

    /**
     * {@code updateRiderPosition} (:529-533): {@code posY + getMountedYOffset() + rider.getYOffset()}.
     * A server-side 1.7.10 player had {@code yOffset} 0 ({@code EntityPlayerMP.<init>}) and
     * {@code EntityPlayer.getYOffset} = {@code yOffset - 0.5} (javap on client-1.7.10.jar), so the
     * rider's feet sit exactly on the board's Y; the client computed the same from its eye-level
     * {@code yOffset} 1.62. 1.21.1 positions passengers by their feet, so the server value is the one.
     *
     * <p>PORT: non-player riders did not exist for this entity in 1.7.10 (only {@code interact}
     * mounted it); a {@code /ride} passenger gets {@code yOffset} 0, i.e. half a block above the board.
     */
    @Override
    protected void positionRider(final Entity passenger, final Entity.MoveFunction callback) {
        final double yOffset = passenger instanceof Player ? -0.5 : 0.0;
        callback.accept(passenger, this.getX(), this.getY() + this.getMountedYOffset() + yOffset, this.getZ());
    }

    /** {@code writeEntityToNBT} (:535-537), without {@code super}. */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        par1NBTTagCompound.putInt("HoverColor", this.getColor());
    }

    /** {@code readEntityFromNBT} (:539-548), without {@code super}; colour clamped to 1..10. */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        this.color = par1NBTTagCompound.getInt("HoverColor");
        if (this.color < 1) {
            this.color = 1;
        }
        if (this.color > 10) {
            this.color = 10;
        }
        this.setColor(this.color);
    }

    // getShadowSize (:550-552) = 0.25: RenderElevator.shadowRadius.

    /**
     * {@code interact} (:554-577). Holding the Ultimate Sword within 4 blocks cycles the colour
     * 1..10 on the server; a board ridden by another player refuses; otherwise the player mounts.
     * Always "handled".
     *
     * <p>A second click by the rider does <b>not</b> dismount (verhalten/entity-07.md says it toggles; it
     * does not): 1.7.10 {@code Entity.mountEntity(sa)} and {@code EntityPlayer.mountEntity} ({@code yz})
     * only dismount for a {@code null} argument (javap on client-1.7.10.jar), and 1.21.1
     * {@code startRiding} returns early for the current vehicle (Entity.java:2103). Dismounting is by
     * sneaking in both versions ({@code Player.rideTick}).
     *
     * <p>PORT: {@code Mob.interact} is final in 1.21.1 and applies a held name tag before this method
     * runs; in 1.7.10 {@code interact} returned {@code true} first, so a name tag mounted the board
     * instead of naming it (R18 case 3).
     */
    @Override
    protected InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // inventory.getCurrentItem() (:555); the stackSize <= 0 cleanup (:556-559) has no 1.21.1 case,
        // an empty slot is ItemStack.EMPTY.
        final ItemStack var2 = par1EntityPlayer.getInventory().getSelected();
        final boolean clientSide = this.level().isClientSide;
        if (!var2.isEmpty() && var2.is(ModItems.ULTIMATE_SWORD.get()) && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            if (!clientSide) {
                ++this.color;
                if (this.color > 10) {
                    this.color = 1;
                }
                this.setColor(this.color);
            }
            return InteractionResult.sidedSuccess(clientSide);
        }
        final Entity rider = this.getFirstPassenger();
        if (rider != null && rider instanceof Player && rider != par1EntityPlayer) {
            return InteractionResult.sidedSuccess(clientSide);
        }
        if (!clientSide) {
            par1EntityPlayer.startRiding(this);
        }
        return InteractionResult.sidedSuccess(clientSide);
    }

    public void setDamageTaken(final float f) {
        this.entityData.set(DAMAGE_TAKEN, f);
    }

    public float getDamageTaken() {
        return this.entityData.get(DAMAGE_TAKEN);
    }

    public void setTimeSinceHit(final int par1) {
        this.entityData.set(TIME_SINCE_HIT, par1);
    }

    public int getTimeSinceHit() {
        return this.entityData.get(TIME_SINCE_HIT);
    }

    public void setForwardDirection(final int par1) {
        this.entityData.set(FORWARD_DIRECTION, par1);
    }

    public int getForwardDirection() {
        return this.entityData.get(FORWARD_DIRECTION);
    }

    public void setExploding(final int par1) {
        this.entityData.set(EXPLODING, par1);
    }

    public int getExploding() {
        return this.entityData.get(EXPLODING);
    }

    public void setColor(final int par1) {
        this.entityData.set(COLOR, par1);
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    /**
     * {@code World.playSoundAtEntity} (1.7.10): the server broadcast to players near the entity's feet;
     * the client call reached only {@code RenderGlobal.playSound}, an empty method (javap on
     * client-1.7.10.jar). {@code Level.playSound(null, ...)} behaves the same on both sides.
     */
    private void playSoundAtEntity(final Entity entity, final SoundEvent sound, final SoundSource source,
                                   final float volume, final float pitch) {
        this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), sound, source, volume, pitch);
    }

    private boolean mobGriefing() {
        return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    private static boolean isTallgrass(final BlockState state) {
        return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN);
    }

    /**
     * {@code (int) x, y, (int) z} of the original's world lookups (:261, :329, :371-379, :394).
     *
     * <p>PORT: every caller passes y through {@code Mth.floor}, not {@code (int)}. 1.7.10 worlds ended
     * at y = 0, where both casts agree, so the original never truncated a negative height; 1.21.1 goes
     * down to -64, and there {@code (int)} reads the block one above the one below the board. Found by
     * the W04 GameTest, which runs at y = -58: {@code (int)(-57.75)} is -57, the board's own air layer,
     * so an empty board never saw the floor and never hovered (R18 case 3). x and z use
     * {@code Mth.floor} as well since DECISIONS R20: {@code (int)} read the neighbouring column in the
     * negative half of the world.
     */
    private static BlockPos legacyPos(final double x, final int y, final double z) {
        return new BlockPos(Mth.floor(x), y, Mth.floor(z));
    }

    /**
     * {@code "reddust"} with the motion vector in the velocity slots: 1.7.10's
     * {@code EntityReddustFX} read those three values as red, green and blue (red 0 -> 1) and gave
     * the particle its own small random motion, which a dust particle with zero speed does as well.
     *
     * <p>PORT: 1.7.10's tessellator clamped vertex colours to 0..255, 1.21.1's {@code BufferBuilder}
     * writes {@code (byte)} and wraps around (BufferBuilder.java:153-159). A negative or large motion
     * component would turn into an arbitrary colour, so the inputs are clamped to 0..1. Exact below 0;
     * above 1 the original clamped after its random brightness factor, the port before it (R18 case 3).
     */
    private static DustParticleOptions reddust(final double motionX, final double motionY, final double motionZ) {
        float r = (float) motionX;
        final float g = (float) motionY;
        final float b = (float) motionZ;
        if (r == 0.0f) {
            r = 1.0f;
        }
        return new DustParticleOptions(
                new Vector3f(Mth.clamp(r, 0.0f, 1.0f), Mth.clamp(g, 0.0f, 1.0f), Mth.clamp(b, 0.0f, 1.0f)), 1.0f);
    }
}
