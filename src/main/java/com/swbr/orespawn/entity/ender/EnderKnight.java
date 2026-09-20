package com.swbr.orespawn.entity.ender;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.LegacyLightLevel;
import com.swbr.orespawn.entity.insect.InsectSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.EnderKnight} (EnderKnight.java:16-282), id {@code ender_knight}: an Enderman with a
 * sword, {@code EntityMob} on the old AI path ({@link LegacyEntityMob}). Stares players down within 64, teleports
 * away by day, in water or fire and from projectiles, and drops Eyes of Ender or Ender Pearls.
 *
 * <p>Size 0.6 x 2.9 (:26) is the entity type's; {@code stepHeight = 1.0} (:27) is the {@code STEP_HEIGHT}
 * attribute. XP 5 from {@code EntityMob} ({@code Monster.xpReward}). {@code writeEntityToNBT}/{@code readEntityFromNBT}
 * (:42-48) only call super and are not overridden.
 */
public class EnderKnight extends LegacyEntityMob implements LegacyArmor {

    /**
     * {@code attackingSpeedBoostModifier} (:18-19, :278-281): +6.2 additive, not saved - a transient modifier. The
     * original UUID {@code 020E0DFB-87AE-4653-9556-831010E291A0} was shared with EnderReaper; so is this id.
     */
    private static final ResourceLocation attackingSpeedBoostModifierUUID =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ender_attacking_speed_boost");
    private static final AttributeModifier attackingSpeedBoostModifier =
            new AttributeModifier(attackingSpeedBoostModifierUUID, 6.199999809265137, AttributeModifier.Operation.ADD_VALUE);

    /** DataWatcher 18, int, "screaming" 0/1 (:39, :270-276). */
    private static final EntityDataAccessor<Integer> DATA_SCREAMING =
            SynchedEntityData.defineId(EnderKnight.class, EntityDataSerializers.INT);

    private int teleportDelay;
    private int stareTimer;
    @Nullable
    private Entity lastEntityToAttack;

    /** Constructor (:24-28) plus the stat half of {@code applyEntityAttributes} (:30-35), read at runtime (R3). */
    public EnderKnight(final EntityType<? extends EnderKnight> type, final Level level) {
        super(type, level);
        final MobStats stats = MobStats.EnderKnight_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) stats.health());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code applyEntityAttributes} (:30-35) with the config defaults (60 / 0.32 / 12); the constructor overwrites
     * health and attack from {@code EnderKnight_stats}. {@code ARMOR} carries {@code getTotalArmorValue} (:215-217) for
     * the vanilla reduction when {@code legacyArmorFormula} is off; the R5 formula reads {@link #getLegacyArmorValue()}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    /** {@code entityInit} (:37-40). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCREAMING, 0);
    }

    /** {@code findPlayerToAttack} (:50-70). */
    @Nullable
    @Override
    protected Entity findPlayerToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final Player entityplayer = this.getClosestVulnerablePlayerToEntity(64.0);
        if (entityplayer != null) {
            if (this.shouldAttackPlayer(entityplayer)) {
                if (this.stareTimer == 0) {
                    // World.playSoundAtEntity(player, "mob.endermen.stare", 1, 1): at the player, heard by everyone near.
                    this.level().playSound(null, entityplayer.getX(), entityplayer.getY(), entityplayer.getZ(),
                            SoundEvents.ENDERMAN_STARE, this.getSoundSource(), 1.0f, 1.0f);
                }
                if (this.stareTimer++ == 5) {
                    this.stareTimer = 0;
                }
                this.setScreaming(true);
                return entityplayer;
            }
            this.stareTimer = 0;
            this.setScreaming(false);
        }
        return null;
    }

    /**
     * {@code shouldAttackPlayer} (:72-83): not with a pumpkin on the head; otherwise the player's look vector must
     * point at the knight's middle within {@code 1 - 0.025 / distance}, with a clear line of sight.
     *
     * <p>PORT: 1.7.10 {@code Blocks.pumpkin} was the carved, wearable pumpkin; in 1.21.1 that is
     * {@code carved_pumpkin}. NeoForge's {@code isEnderMask} hook is not asked - the original knew only the pumpkin.
     * The eye height is the pose-dependent 1.21.1 one (1.27 sneaking); {@code EntityPlayerMP.getEyeHeight} was a
     * constant 1.62 ({@code mw.g}). {@code canEntityBeSeen} is {@code hasLineOfSight} (collision shapes, 128 cap
     * beyond the 64 search range).
     */
    private boolean shouldAttackPlayer(final Player par1EntityPlayer) {
        final ItemStack itemstack = par1EntityPlayer.getInventory().armor.get(3);
        if (!itemstack.isEmpty() && itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
            return false;
        }
        final Vec3 vec3 = par1EntityPlayer.getViewVector(1.0f).normalize();
        Vec3 vec4 = new Vec3(this.getX() - par1EntityPlayer.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0f - (par1EntityPlayer.getY() + par1EntityPlayer.getEyeHeight()),
                this.getZ() - par1EntityPlayer.getZ());
        final double d0 = vec4.length();
        vec4 = vec4.normalize();
        final double d2 = vec3.dot(vec4);
        return d2 > 1.0 - 0.025 / d0 && par1EntityPlayer.hasLineOfSight(this);
    }

    /**
     * {@code onLivingUpdate} (:85-134), then {@code super.onLivingUpdate()} = {@link Monster#aiStep} and the old AI.
     *
     * <p>Runs on both sides like the original: the portal particles are client-only in effect
     * ({@code ServerLevel.addParticle} is empty, as {@code WorldServer.spawnParticle} was), and the wet/burning
     * teleport also moves the client copy until the server position arrives - 1.7.10 did the same.
     * {@code isWet()} is {@code isInWaterOrRain()} (water or rain, no bubble column).
     */
    @Override
    public void aiStep() {
        if (this.isInWaterOrRain()) {
            this.hurt(this.damageSources().drown(), 1.0f);
        }
        if (this.lastEntityToAttack != this.entityToAttack) {
            final AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeinstance.removeModifier(attackingSpeedBoostModifierUUID);
            if (this.entityToAttack != null) {
                attributeinstance.addTransientModifier(attackingSpeedBoostModifier);
            }
        }
        this.lastEntityToAttack = this.entityToAttack;
        for (int i = 0; i < 2; ++i) {
            this.level().addParticle(ParticleTypes.PORTAL,
                    this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * this.getBbHeight() - 0.25,
                    this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                    (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);
        }
        if (InsectSupport.isDaytime(this.level()) && !this.level().isClientSide) {
            final float f = this.getLightLevelDependentMagicValue();
            if (f > 0.5f && this.level().canSeeSky(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ())))
                    && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f) {
                this.entityToAttack = null;
                this.setScreaming(false);
                this.teleportRandomly();
            }
        }
        if (this.isInWaterOrRain() || this.isOnFire()) {
            this.setScreaming(false);
            this.teleportRandomly();
        }
        this.isJumping = false;
        if (this.entityToAttack != null) {
            this.lookAt(this.entityToAttack, 100.0f, 100.0f);
        }
        if (!this.level().isClientSide && this.isAlive()) {
            if (this.entityToAttack != null) {
                if (this.entityToAttack instanceof Player player && this.shouldAttackPlayer(player)) {
                    if (this.entityToAttack.distanceToSqr(this) < 16.0) {
                        this.teleportRandomly();
                    }
                    this.teleportDelay = 0;
                } else if (this.entityToAttack.distanceToSqr(this) > 256.0 && this.teleportDelay++ >= 30
                        && this.teleportToEntity(this.entityToAttack)) {
                    this.teleportDelay = 0;
                }
            } else {
                this.setScreaming(false);
                this.teleportDelay = 0;
            }
        }
        super.aiStep();
    }

    /** {@code teleportRandomly} (:136-141): x/z ±32, y -32..+31. */
    protected boolean teleportRandomly() {
        final double d0 = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        final double d2 = this.getY() + (this.random.nextInt(64) - 32);
        final double d3 = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.tryTeleportTo(d0, d2, d3);
    }

    /**
     * {@code teleportToEntity} (:143-151): 16 blocks along the line away from the entity's side, ±4 x/z, -8..+7 y.
     * The vertical term is {@code minY + height/2 - target.posY + target.eyeHeight} - the eye height is added, not
     * subtracted, as written.
     */
    protected boolean teleportToEntity(final Entity par1Entity) {
        Vec3 vec3 = new Vec3(this.getX() - par1Entity.getX(),
                this.getBoundingBox().minY + this.getBbHeight() / 2.0f - par1Entity.getY() + par1Entity.getEyeHeight(),
                this.getZ() - par1Entity.getZ());
        vec3 = vec3.normalize();
        final double d0 = 16.0;
        final double d2 = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - vec3.x * d0;
        final double d3 = this.getY() + (this.random.nextInt(16) - 8) - vec3.y * d0;
        final double d4 = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - vec3.z * d0;
        return this.tryTeleportTo(d2, d3, d4);
    }

    /**
     * {@code teleportTo} (:153-201): sink to the first movement-blocking block below, keep the spot only if the box is
     * free of collisions and liquid; then 128 portal particles along the way and {@code mob.endermen.portal} at the
     * start and at the knight.
     *
     * <p>PORT: {@code World.blockExists} is {@code Level.isLoaded} (loaded chunk and inside the build height), and the
     * sinking stops at {@code getMinBuildHeight()} instead of 0 (R18 case 3, the world reaches below 0 now). The
     * position is set with {@code setPos} on both sides like {@code setPosition}; {@code Entity.teleportTo} only
     * acts on a server level and the vanilla {@code randomTeleport} also stops navigation and broadcasts event 46,
     * neither of which the original did. No {@code EntityTeleportEvent}: the original posted none.
     *
     * <p>PORT: renamed from {@code teleportTo} - 1.21.1 {@code Entity.teleportTo(double, double, double)} is a
     * {@code void} method with the same parameters, which a {@code boolean} method cannot override.
     */
    protected boolean tryTeleportTo(final double par1, final double par3, final double par5) {
        final double d3 = this.getX();
        final double d4 = this.getY();
        final double d5 = this.getZ();
        final double posX = par1;
        double posY = par3;
        final double posZ = par5;
        boolean flag = false;
        final int i = Mth.floor(posX);
        int j = Mth.floor(posY);
        final int k = Mth.floor(posZ);
        if (this.level().isLoaded(new BlockPos(i, j, k))) {
            boolean flag2 = false;
            while (!flag2 && j > this.level().getMinBuildHeight()) {
                final BlockState l = this.level().getBlockState(new BlockPos(i, j - 1, k));
                if (!l.isAir() && l.blocksMotion()) {
                    flag2 = true;
                } else {
                    --posY;
                    --j;
                }
            }
            if (flag2) {
                this.setPos(posX, posY, posZ);
                if (this.level().noCollision(this, this.getBoundingBox()) && !this.level().containsAnyLiquid(this.getBoundingBox())) {
                    flag = true;
                }
            }
        }
        if (!flag) {
            this.setPos(d3, d4, d5);
            return false;
        }
        final short short1 = 128;
        for (int lx = 0; lx < short1; ++lx) {
            final double d6 = lx / (short1 - 1.0);
            final float f = (this.random.nextFloat() - 0.5f) * 0.2f;
            final float f2 = (this.random.nextFloat() - 0.5f) * 0.2f;
            final float f3 = (this.random.nextFloat() - 0.5f) * 0.2f;
            final double d7 = d3 + (this.getX() - d3) * d6 + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0;
            final double d8 = d4 + (this.getY() - d4) * d6 + this.random.nextDouble() * this.getBbHeight();
            final double d9 = d5 + (this.getZ() - d5) * d6 + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0;
            this.level().addParticle(ParticleTypes.PORTAL, d7, d8, d9, (double) f, (double) f2, (double) f3);
        }
        this.level().playSound(null, d3, d4, d5, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0f, 1.0f);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return true;
    }

    /** {@code getLivingSound} (:203-205). */
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isScreaming() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    /** {@code getHurtSound} (:207-209). */
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    /** {@code getDeathSound} (:211-213). */
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    /** {@code getTotalArmorValue} (:215-217) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.EnderKnight_stats().defense();
    }

    /** {@code getDropItem} (:219-224): with the world's random, 1 in 2 an Eye of Ender, else an Ender Pearl. */
    protected Item getDropItem() {
        if (this.level().random.nextInt(2) == 1) {
            return Items.ENDER_EYE;
        }
        return Items.ENDER_PEARL;
    }

    /**
     * {@code dropFewItems} (:226-233) called from {@code onDeath} with the killer's Looting (players only): one item
     * type per death, {@code rand(2 + looting)} single stacks (R10). The equipment drop of {@code onDeath} follows as
     * the super call.
     */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        int par2 = 0;
        if (damageSource.getEntity() instanceof Player killer) {
            par2 = EnchantmentHelper.getEnchantmentLevel(
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
        }
        final Item j = this.getDropItem();
        if (j != null) {
            for (int k = this.random.nextInt(2 + par2), l = 0; l < k; ++l) {
                this.spawnAtLocation(new ItemStack(j, 1));
            }
        }
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code attackEntityFrom} (:235-249): scream; indirect damage (projectiles, thrown potions, fireballs -
     * {@code EntityDamageSourceIndirect}, which is a source whose direct entity is not its cause) makes up to 16
     * teleport attempts, and a successful one cancels the damage.
     *
     * <p>PORT: 1.7.10 explosions were never {@code EntityDamageSourceIndirect} ({@code DamageSource.setExplosionSource}
     * builds a plain {@code DamageSource} or an {@code EntityDamageSource}), so the knight took blast damage. A 1.21.1
     * explosion source has the TNT or fireball as direct and its owner (or null) as cause and would fail
     * {@code isDirect()}; explosions are therefore excluded by {@code DamageTypeTags.IS_EXPLOSION}.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        if (this.isInvulnerable()) {
            return false;
        }
        this.setScreaming(true);
        if (!par1DamageSource.isDirect() && !par1DamageSource.is(DamageTypeTags.IS_EXPLOSION)) {
            for (int i = 0; i < 16; ++i) {
                if (this.teleportRandomly()) {
                    return true;
                }
            }
            return super.hurt(par1DamageSource, par2);
        }
        return super.hurt(par1DamageSource, par2);
    }

    /**
     * {@code getCanSpawnHere} (:251-268) as the placement predicate: a spawner of this mob always may; otherwise dark
     * enough, night and Y ≥ 30.
     *
     * <p>PORT: the spawner scan (x/z -3..+2, y 0..+4 for a spawner of "Ender Knight") is
     * {@code MobSpawnType.SPAWNER} (catalogue README 4.8). No peaceful check - the override dropped
     * {@code EntityMob}'s.
     */
    public static boolean checkEnderKnightSpawnRules(final EntityType<EnderKnight> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        return LegacyLightLevel.isValidLightLevel(level, pos, random) && !InsectSupport.isDaytime(level.getLevel()) && pos.getY() >= 30.0;
    }

    /** The whole of {@code getCanSpawnHere} is {@link #checkEnderKnightSpawnRules}; nothing to add on the instance. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code isScreaming} (:270-272). */
    public boolean isScreaming() {
        return this.entityData.get(DATA_SCREAMING) > 0;
    }

    /** {@code setScreaming} (:274-276). */
    public void setScreaming(final boolean par1) {
        this.entityData.set(DATA_SCREAMING, par1 ? 1 : 0);
    }
}
