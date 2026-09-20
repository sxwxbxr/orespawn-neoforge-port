package com.swbr.orespawn.entity.insect;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.NoStepTrigger;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.util.Ignoreable;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.Firefly} (Firefly.java:12-177), id {@code firefly}. A harmless night
 * {@code EntityAmbientCreature} whose tail light blinks ({@link #getBlink()}, drawn by
 * {@code ModelFirefly}); by day it vanishes without a trace. Drops Extreme Torches.
 *
 * <p>Not carried over: {@code getTexture(Firefly)} takes its argument only to ignore it - {@link #getTexture()};
 * {@code initCreature} (:167, empty).
 */
public class Firefly extends AmbientCreature implements Ignoreable, NoStepTrigger {

    /** {@code texture1} (:174-176), {@code Fireflytexture.png} lower-cased by the asset import. */
    private static final ResourceLocation texture1 =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/fireflytexture.png");

    int my_blink = 0;
    int blinker = 0;
    int myspace = 0; // :17, never read
    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    /**
     * Constructor (:20-30): the blink period 20..39 from the entity's own random, on both sides (the
     * client blinks with its own period, unsynchronised, as in 1.7.10). {@code setSize(0.4f, 0.8f)} is the
     * entity type's size; {@code renderDistanceWeight = 3.0} is {@link #shouldRenderAtSqrDistance}.
     */
    public Firefly(final EntityType<? extends Firefly> type, final Level level) {
        super(type, level);
        this.my_blink = 20 + this.random.nextInt(20);
        this.setPathfindingMalus(PathType.WATER, -1.0f); // setAvoidsWater(true), :28
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:32-38): health {@link #mygetMaxHealth()} = 1, speed 0.1, attack 0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.10000000149011612)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    /** {@code getTexture} (:40-42). */
    public ResourceLocation getTexture() {
        return texture1;
    }

    /** {@code getBlink} (:48-53): the lightmap block coordinate of the tail light, on for the first half period. */
    public float getBlink() {
        if (this.blinker < this.my_blink / 2) {
            return 240.0f;
        }
        return 0.0f;
    }

    /** {@code getSoundVolume} (:55-57). */
    @Override
    protected float getSoundVolume() {
        return 0.0f;
    }

    /** {@code getSoundPitch} (:59-61). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:63-65). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:67-69). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:71-73). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code canBePushed} (:75-77). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:79-80). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code collideWithNearbyEntities} (:82-83). */
    @Override
    protected void pushEntities() {
    }

    /** {@code mygetMaxHealth} (:85-87). */
    public int mygetMaxHealth() {
        return 1;
    }

    /**
     * {@code getDropItem} (:89-91) through 1.7.10 {@code EntityLiving.dropFewItems}: {@code rand(3)} Extreme
     * Torches, plus {@code rand(looting + 1)} when a player killed it with Looting, each as its own stack
     * (R10: code, not a loot table). {@code dropEquipment} followed in {@code onDeath} - that is the super
     * call.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        int looting = 0;
        if (damageSource.getEntity() instanceof Player killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        final Item item = ModItems.EXTREME_TORCH.get();
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
     * {@code onUpdate} (:97-115): damp, blink, and - unless persistent - in the day half of the clock
     * ({@code worldTime % 24000 <= 11000}) vanish with 1/500 per tick without dying, so without drops.
     *
     * <p>PORT: the vanishing roll runs on the server only. The original rolled it on both sides with each
     * side's world random, so a client could drop its copy while the server's firefly flew on (R18 case 4).
     */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.600000023841, 1.0));
        ++this.blinker;
        if (this.blinker > this.my_blink) {
            this.blinker = 0;
        }
        if (this.isPersistenceRequired()) {
            return;
        }
        long t = this.level().getDayTime();
        t %= 24000L;
        if (t > 11000L) {
            return;
        }
        if (!this.level().isClientSide && this.level().random.nextInt(500) == 1) {
            this.discard();
        }
    }

    /**
     * {@code updateAITasks} (:117-141): a new random air target x/z ±3, y -2..+1 with 1/40 or when within
     * {@code distSq < 2}; slow steering 0.2/0.7/0.2, yaw follows at a quarter.
     */
    @Override
    protected void customServerAiStep() {
        int keep_trying = 25;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()));
        }
        if (this.random.nextInt(40) == 0
                || InsectSupport.getDistanceSquared(this.currentFlightTarget, Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())) < 2.0f) {
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0;
                    bid = this.level().getBlockState(this.currentFlightTarget), --keep_trying) {
                this.currentFlightTarget.set(
                        Mth.floor(this.getX()) + this.random.nextInt(4) - this.random.nextInt(4),
                        Mth.floor(this.getY()) + this.random.nextInt(4) - 2,
                        Mth.floor(this.getZ()) + this.random.nextInt(4) - this.random.nextInt(4));
            }
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

    /** {@code canTriggerWalking} (:143-145) {@code false}; see {@link NoStepTrigger} (R20). */
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** {@code fall} and {@code updateFallState} (:147-151). */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code doesEntityNotTriggerPressurePlate} (:153-155). */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /**
     * {@code renderDistanceWeight = 3.0} (:29) in 1.7.10 {@code isInRangeToRenderDist}: the average edge
     * length × 64 × weight. 1.21.1's global view scale (the entity-distance slider) stays a factor on top.
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

    /** {@code getCanSpawnHere} (:157-160) on the instance: air, night, at most 10 fireflies around, Islands or Y ≥ 50. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        final BlockState bid = level.getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())));
        return bid.isAir() && !InsectSupport.isDaytime(this.level())
                && findBuddies(level, this.getBoundingBox()) <= 10
                && (this.level().dimension().equals(InsectSupport.DIMENSION_ISLANDS) || this.getY() >= 50.0);
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /**
     * {@code getCanSpawnHere} (:157-160) as the placement predicate. The buddy count uses the box the
     * firefly will occupy at the block centre, where natural spawning places it.
     */
    public static boolean checkFireflySpawnRules(final EntityType<Firefly> type, final ServerLevelAccessor level,
                                                 final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        final ServerLevel world = level.getLevel();
        return level.getBlockState(pos).isAir() && !InsectSupport.isDaytime(world)
                && findBuddies(level, type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) <= 10
                && (world.dimension().equals(InsectSupport.DIMENSION_ISLANDS) || pos.getY() >= 50.0);
    }

    /** {@code findBuddies} (:162-165): fireflies in {@code expand(20, 8, 20)}. */
    private static int findBuddies(final LevelAccessor level, final AABB boundingBox) {
        return level.getEntitiesOfClass(Firefly.class, boundingBox.inflate(20.0, 8.0, 20.0)).size();
    }

    /** {@code canDespawn} (:170-172): only by day; persistence is checked by {@code Mob.checkDespawn} first. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return InsectSupport.isDaytime(this.level());
    }
}
