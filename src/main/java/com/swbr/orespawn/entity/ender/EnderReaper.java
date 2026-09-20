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
 * Port of {@code danger.orespawn.EnderReaper} (EnderReaper.java:16-290), id {@code ender_reaper}: the stronger
 * EnderKnight with a scythe, {@code EntityMob} on the old AI path ({@link LegacyEntityMob}). The code is the
 * EnderKnight one except for the size 0.7 x 2.9 (:26), speed 0.37 (:33), player search 81 (:54), the Eye-of-Ender-only
 * drop (:215-217) and the spawn rule that wants no other reaper within 16/8/16 (:273-275). Kept as its own full copy
 * like the original, so each file maps to one original class.
 *
 * <p>{@code stepHeight = 1.0} (:27) is the {@code STEP_HEIGHT} attribute; XP 5 from {@code EntityMob}.
 */
public class EnderReaper extends LegacyEntityMob implements LegacyArmor {

    /**
     * {@code attackingSpeedBoostModifier} (:18-19, :286-289): +6.2 additive, not saved; the same UUID as EnderKnight in
     * the original, the same id here.
     */
    private static final ResourceLocation attackingSpeedBoostModifierUUID =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ender_attacking_speed_boost");
    private static final AttributeModifier attackingSpeedBoostModifier =
            new AttributeModifier(attackingSpeedBoostModifierUUID, 6.199999809265137, AttributeModifier.Operation.ADD_VALUE);

    /** DataWatcher 18, int, "screaming" 0/1 (:39, :278-284). */
    private static final EntityDataAccessor<Integer> DATA_SCREAMING =
            SynchedEntityData.defineId(EnderReaper.class, EntityDataSerializers.INT);

    private int teleportDelay;
    private int stareTimer;
    @Nullable
    private Entity lastEntityToAttack;

    /** Constructor (:24-28) plus the stat half of {@code applyEntityAttributes} (:30-35), read at runtime (R3). */
    public EnderReaper(final EntityType<? extends EnderReaper> type, final Level level) {
        super(type, level);
        final MobStats stats = MobStats.EnderReaper_stats();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) stats.health());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) stats.attack());
        this.getAttribute(Attributes.ARMOR).setBaseValue((double) stats.defense());
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code applyEntityAttributes} (:30-35) with the config defaults (90 / 0.37 / 18); the constructor overwrites
     * health and attack from {@code EnderReaper_stats}. {@code ARMOR} = {@code getTotalArmorValue} (:219-221) for the
     * vanilla reduction; the R5 formula reads {@link #getLegacyArmorValue()}.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 90.0)
                .add(Attributes.MOVEMENT_SPEED, 0.37)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    /** {@code entityInit} (:37-40). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SCREAMING, 0);
    }

    /** {@code findPlayerToAttack} (:50-70), search range 81. */
    @Nullable
    @Override
    protected Entity findPlayerToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final Player entityplayer = this.getClosestVulnerablePlayerToEntity(81.0);
        if (entityplayer != null) {
            if (this.shouldAttackPlayer(entityplayer)) {
                if (this.stareTimer == 0) {
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
     * {@code shouldAttackPlayer} (:72-83). PORT: carved pumpkin, pose-dependent eye height and
     * {@code hasLineOfSight} as in {@link EnderKnight}.
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

    /** {@code onLivingUpdate} (:85-134); see {@link EnderKnight#aiStep()} for the side notes. */
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

    /** {@code teleportRandomly} (:136-141). */
    protected boolean teleportRandomly() {
        final double d0 = this.getX() + (this.random.nextDouble() - 0.5) * 64.0;
        final double d2 = this.getY() + (this.random.nextInt(64) - 32);
        final double d3 = this.getZ() + (this.random.nextDouble() - 0.5) * 64.0;
        return this.tryTeleportTo(d0, d2, d3);
    }

    /** {@code teleportToEntity} (:143-151), eye height added as written. */
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

    /** {@code teleportTo} (:153-201), renamed like {@link EnderKnight#tryTeleportTo}; PORT notes there. */
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

    /** {@code getDropItem} (:215-217). */
    protected Item getDropItem() {
        return Items.ENDER_EYE;
    }

    /** {@code getTotalArmorValue} (:219-221) for the R5 formula. */
    @Override
    public int getLegacyArmorValue() {
        return MobStats.EnderReaper_stats().defense();
    }

    /** {@code dropFewItems} (:223-230): {@code rand(2 + looting)} Eyes of Ender (R10). */
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

    /** {@code attackEntityFrom} (:232-246); indirect damage as in {@link EnderKnight#hurt}. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        if (this.isInvulnerable()) {
            return false;
        }
        this.setScreaming(true);
        if (!par1DamageSource.isDirect()) {
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
     * {@code getCanSpawnHere} (:248-276) as the placement predicate: a spawner may always; otherwise dark enough, night,
     * Y ≥ 30 and no other reaper in {@code boundingBox.expand(16, 8, 16)} - the box the reaper will occupy at the block
     * centre, where natural spawning places it. PORT: the spawner scan is {@code MobSpawnType.SPAWNER}.
     */
    public static boolean checkEnderReaperSpawnRules(final EntityType<EnderReaper> type, final ServerLevelAccessor level,
                                                     final MobSpawnType spawnType, final BlockPos pos, final RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER) {
            return true;
        }
        if (!LegacyLightLevel.isValidLightLevel(level, pos, random)) {
            return false;
        }
        if (InsectSupport.isDaytime(level.getLevel())) {
            return false;
        }
        if (pos.getY() < 30.0) {
            return false;
        }
        return level.getEntitiesOfClass(EnderReaper.class,
                type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).inflate(16.0, 8.0, 16.0)).isEmpty();
    }

    /** The whole of {@code getCanSpawnHere} is {@link #checkEnderReaperSpawnRules}; nothing to add on the instance. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return true;
    }

    /** The override of {@code getCanSpawnHere} dropped {@code EntityLiving}'s collision and liquid test. */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code isScreaming} (:278-280). */
    public boolean isScreaming() {
        return this.entityData.get(DATA_SCREAMING) > 0;
    }

    /** {@code setScreaming} (:282-284). */
    public void setScreaming(final boolean par1) {
        this.entityData.set(DATA_SCREAMING, par1 ? 1 : 0);
    }
}
