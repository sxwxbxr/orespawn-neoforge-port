package com.swbr.orespawn.entity.aquatic;

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import com.swbr.orespawn.entity.LegacyAgeable;
import com.swbr.orespawn.entity.herbivore.HerbivoreSupport;
import com.swbr.orespawn.entity.insect.InsectSupport;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.GoldFish} (GoldFish.java:11-166), id {@code gold_fish}: a goldfish that swims through
 * the air. An {@code EntityAnimal} without tasks and without offspring; all movement is the flight code of
 * {@link #customServerAiStep}.
 *
 * <p>Not carried over: {@code experienceValue = 5} (:19) - 1.7.10 {@code EntityAnimal.getExperiencePoints} ignored it
 * and gave 1-3, which {@code Animal.getBaseExperienceReward} still does (W04); {@code attackDamage 1.0} is registered
 * but never used, as in the original; {@code canTriggerWalking} and {@code doesEntityNotTriggerPressurePlate}
 * (:127-139) return the defaults; {@code canBreatheUnderwater} (:163-165) is the entity type tag
 * {@code minecraft:can_breathe_under_water} (final in 1.21.1, W04).
 */
public class GoldFish extends Animal {

    @Nullable
    private BlockPos.MutableBlockPos currentFlightTarget = null;

    /**
     * Constructor (:15-22). {@code setSize(0.75f, 0.5f)} is the entity type's size; {@code isImmuneToFire = false}
     * is the builder without {@code fireImmune()}; {@code fireResistance = 5} is {@link #getFireImmuneTicks()}.
     */
    public GoldFish(final EntityType<? extends GoldFish> type, final Level level) {
        super(type, level);
        this.currentFlightTarget = null;
        this.moveControl = new InsectSupport.LegacyMoveControl(this);
    }

    /** {@code applyEntityAttributes} (:24-30): health {@link #mygetMaxHealth()} = 6, speed 0.22, attack 1.0. */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2199999988079071)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    /** {@code fireResistance = 5} (:21): ticks in fire before the fish catches fire. */
    @Override
    protected int getFireImmuneTicks() {
        return 5;
    }

    /**
     * {@code canDespawn} (:32-34): only at night. {@code isNoDespawnRequired()} is the persistence check
     * {@code Mob.checkDespawn} makes before asking; 1.21.1 {@code Animal} answers {@code false} by default.
     */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return !InsectSupport.isDaytime(this.level());
    }

    /** {@code getSoundVolume} (:36-38). */
    @Override
    protected float getSoundVolume() {
        return 0.45f;
    }

    /** {@code getSoundPitch} (:40-42). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code getLivingSound} (:44-46): {@code "splash"} has no namespace and no vanilla event - silent (R18). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:48-50): {@code "splash"}, silent (R18). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:52-54). */
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.LITTLE_SPLAT.get();
    }

    /** {@code canBePushed} (:56-58). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /** {@code collideWithEntity} (:60-61). */
    @Override
    protected void doPush(final Entity par1Entity) {
    }

    /** {@code mygetMaxHealth} (:63-65). */
    public int mygetMaxHealth() {
        return 6;
    }

    /** {@code onUpdate} (:71-74): the vertical motion is damped after every tick - the fish hovers. */
    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
    }

    /**
     * {@code canSeeTarget} (:76-78): no block between a point 0.75 above the feet and the target.
     * {@code rayTraceBlocks(a, b, false)} hit every block with a selection box and ignored liquids - the outline
     * shape without fluids.
     */
    public boolean canSeeTarget(final double pX, final double pY, final double pZ) {
        return this.level().clip(new ClipContext(new Vec3(this.getX(), this.getY() + 0.75, this.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    /**
     * {@code updateAITasks} (:80-125): pick a visible air block 5-9 blocks away (up to 50 tries) with 1/300 per tick or
     * once the target is reached, then steer towards it. Between Y 120 and 140 the height is free, below it climbs,
     * above it sinks.
     *
     * <p>PORT: 1.7.10 ran this after the move, look and jump helpers of the same tick; 1.21.1 runs
     * {@code customServerAiStep} before them. The idle move control that would clear {@code zza} is skipped by
     * {@link InsectSupport.LegacyMoveControl}, exactly as for the W05 flyers. All {@code (int)} coordinate casts are
     * {@code Mth.floor} (R20).
     */
    @Override
    protected void customServerAiStep() {
        int xdir = 1;
        int zdir = 1;
        int keep_trying = 50;
        int updown = 0;
        if (this.isRemoved()) {
            return;
        }
        super.customServerAiStep();
        final int posX = Mth.floor(this.getX());
        final int posY = Mth.floor(this.getY());
        final int posZ = Mth.floor(this.getZ());
        if (this.currentFlightTarget == null) {
            this.currentFlightTarget = new BlockPos.MutableBlockPos(posX, posY, posZ);
        }
        if (posY < 120) {
            updown = 2;
        }
        if (posY > 140) {
            updown = -2;
        }
        if (this.random.nextInt(300) == 0 || InsectSupport.getDistanceSquared(this.currentFlightTarget, posX, posY, posZ) < 2.1f) {
            // bid != Blocks.air: 1.7.10 had a single air block; isAir() also covers cave and void air.
            for (BlockState bid = Blocks.STONE.defaultBlockState(); !bid.isAir() && keep_trying != 0; --keep_trying) {
                zdir = this.random.nextInt(5) + 5;
                xdir = this.random.nextInt(5) + 5;
                if (this.random.nextInt(2) == 0) {
                    zdir = -zdir;
                }
                if (this.random.nextInt(2) == 0) {
                    xdir = -xdir;
                }
                this.currentFlightTarget.set(posX + xdir, posY + this.random.nextInt(11) - 5 + updown, posZ + zdir);
                bid = this.level().getBlockState(this.currentFlightTarget);
                if (bid.isAir() && !this.canSeeTarget(this.currentFlightTarget.getX(), this.currentFlightTarget.getY(),
                        this.currentFlightTarget.getZ())) {
                    bid = Blocks.STONE.defaultBlockState();
                }
            }
        }
        final double var1 = this.currentFlightTarget.getX() + 0.4 - this.getX();
        final double var2 = this.currentFlightTarget.getY() + 0.1 - this.getY();
        final double var3 = this.currentFlightTarget.getZ() + 0.4 - this.getZ();
        final Vec3 motion = this.getDeltaMovement();
        final double motionX = motion.x + (Math.signum(var1) * 0.4 - motion.x) * 0.3;
        final double motionY = motion.y + (Math.signum(var2) * 0.7 - motion.y) * 0.2;
        final double motionZ = motion.z + (Math.signum(var3) * 0.4 - motion.z) * 0.3;
        this.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - this.getYRot());
        this.zza = 0.75f;
        this.setYRot(this.getYRot() + var5 / 6.0f);
    }

    /** {@code fall} and {@code updateFallState} (:131-135), both empty: no fall distance, no fall damage. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * {@code getCanSpawnHere} (:141-143) is {@code true} without the {@code EntityLiving} collision and liquid test and
     * without the creature light rule - the instance checks of 1.21.1 answer the same.
     */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** See {@link #checkSpawnRules}. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code getCanSpawnHere} (:141-143) as the placement predicate: always. */
    public static boolean checkGoldFishSpawnRules(final EntityType<GoldFish> type, final ServerLevelAccessor level,
                                                  final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        return true;
    }

    /**
     * 1.7.10 {@code onDeath}: {@code dropFewItems(recentlyHit, looting)}, then {@code dropEquipment}
     * ({@code Mob.dropCustomDeathLoot}). Looting counts only for a player kill.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        int looting = 0;
        if (damageSource.getEntity() instanceof Player killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        this.dropFewItems(recentlyHit, looting);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * The inherited 1.7.10 {@code EntityLiving.dropFewItems} (bytecode {@code sw.b(ZI)V}): one {@link #getDropItem()}
     * roll, then {@code rand(3)} stacks of one, plus {@code rand(looting + 1)} when looting is above 0.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        final Item item = this.getDropItem();
        if (item != null) {
            int j = this.random.nextInt(3);
            if (par2 > 0) {
                j += this.random.nextInt(par2 + 1);
            }
            for (int k = 0; k < j; ++k) {
                this.spawnAtLocation(new ItemStack(item, 1));
            }
        }
    }

    /** {@code getDropItem} (:145-157): a gold block, a uranium nugget or a titanium nugget, on the world random. */
    @Nullable
    protected Item getDropItem() {
        final int i = this.level().random.nextInt(3);
        if (i == 0) {
            return Items.GOLD_BLOCK;
        }
        if (i == 1) {
            return ModItems.URANIUM_NUGGET.get();
        }
        if (i == 2) {
            return ModItems.TITANIUM_NUGGET.get();
        }
        return null;
    }

    /** {@code createChild} (:159-161). */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob otherParent) {
        return null;
    }

    /**
     * Not overridden in the original: 1.7.10 {@code EntityAnimal.isBreedingItem} is wheat. Feeding wheat still puts
     * the fish in love; with no mate goal and no child nothing follows, as before.
     */
    @Override
    public boolean isFood(final ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /**
     * No {@code interact} in GoldFish.java: 1.7.10 {@code EntityAnimal.interact}, which feeds only an adult of age 0.
     * 1.21.1 {@code Animal.mobInteract} would also use up wheat on a young fish (see
     * {@link HerbivoreSupport#legacyAnimalInteract}).
     */
    @Override
    public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
        return HerbivoreSupport.legacyAnimalInteract(this, player, hand);
    }

    /** No natural babies, as in 1.7.10: see {@link LegacyAgeable#noBabies} (R26). */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(final ServerLevelAccessor level, final DifficultyInstance difficulty,
                                        final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, LegacyAgeable.noBabies(spawnGroupData));
    }
}
