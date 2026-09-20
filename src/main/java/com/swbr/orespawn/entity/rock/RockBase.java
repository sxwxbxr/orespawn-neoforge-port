package com.swbr.orespawn.entity.rock;

import com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.item.rock.ItemRock;
import com.swbr.orespawn.item.utility.ItemMinersDream;
import com.swbr.orespawn.util.Ignoreable;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

/**
 * Port of {@code danger.orespawn.RockBase} (RockBase.java:10-268), registry id {@code rock}
 * ("Rock", OreSpawnMain.java:4077-4081: tracking 32 / 1 / no velocity updates). A rock lying on
 * the ground as a living entity without AI tasks: placed by {@link ItemRock}, hit to pick it up
 * again. Hitbox 0.25 x 0.15 (:19), fire immune (:20-21), armor 0 (:67-69), health
 * {@code 1 + type / 4} (:63-64, :136-137), never despawns (:182-184), no XP (:205-207), no vanilla
 * drops (:209-247 without {@code super}). Every target search skips it ({@link Ignoreable},
 * MyUtils.java:17-19).
 *
 * <p>Type 0 (a rock spawned by the world generator, OreSpawnWorld.java:2871-2892) rolls its type
 * on the first server tick: outside the Crystal dimension 1, then overwritten by 1/10 -> 2,
 * 1/20 -> 3, 1/30 -> 4, 1/40 -> 5, 1/50 -> 6, 1/100 -> 7, 1/200 -> 8, 1/500 -> 9, 1/500 -> 10,
 * 1/500 -> 11, 1/1000 -> 12; in Crystal 9, then 1/3 -> 10, 1/5 -> 11, 1/10 -> 12 (:87-135).
 *
 * <p>PORT: {@code fireResistance = 100000} (:20) has no 1.21.1 field; {@code isImmuneToFire}
 * (:21) is {@code fireImmune()} on the entity type and covers the effect. The NBT key is the
 * original's copy-paste {@code "ButterflyType"} (:261, :266).
 */
public class RockBase extends Mob implements Ignoreable, LegacyArmor {

    /** DataWatcher slot 20 (:29): the rock type. */
    private static final EntityDataAccessor<Integer> ROCK_TYPE =
            SynchedEntityData.defineId(RockBase.class, EntityDataSerializers.INT);

    public int rock_type;
    /** Remembered start position (:75-78); written once, never read - as in the original. */
    private double dx;
    private double dz;
    /**
     * {@code EntityLivingBase.isJumping} as the inherited 1.7.10 {@code EntityLiving.updateEntityActionState}
     * left it; see {@link #customServerAiStep()}. Not saved, as in 1.7.10.
     */
    private boolean legacyJumping;

    /** {@code RockBase(World)} (:16-25). */
    public RockBase(final EntityType<? extends RockBase> type, final Level par1World) {
        super(type, par1World);
        this.rock_type = 0;
        final double n = 0.0;
        this.dz = n;
        this.dx = n;
    }

    /** {@code applyEntityAttributes} (:32-34): only the {@code EntityLiving} defaults (max health 20, follow range 16). */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes();
    }

    /** {@code entityInit} (:27-30): slot 20, default 0. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROCK_TYPE, 0);
    }

    /**
     * {@code attackEntityFrom} (:36-45): suffocation does nothing; a living attacker pops
     * ({@code random.pop} 0.75 / 2.25); then the normal hit.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final Entity e = par1DamageSource.getEntity(); // :37
        if (par1DamageSource.is(DamageTypes.IN_WALL)) { // :38-40 "inWall"
            return false;
        }
        if (e != null && e instanceof LivingEntity) { // :41-43
            this.playSound(SoundEvents.ITEM_PICKUP, 0.75f, 2.25f);
        }
        return super.hurt(par1DamageSource, par2); // :44
    }

    /** {@code getRockType} (:47-49). */
    public int getRockType() {
        return this.entityData.get(ROCK_TYPE);
    }

    /** {@code setRockType} (:51-59): server only. */
    public void setRockType(final int par1) {
        if (this.level() == null) {
            return;
        }
        if (this.level().isClientSide) {
            return;
        }
        this.entityData.set(ROCK_TYPE, par1);
    }

    /** {@code placeRock} (:61-65): type, then max health and health {@code 1 + type / 4} (integer division). */
    public void placeRock(final int par1) {
        this.setRockType(this.rock_type = par1);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1 + this.rock_type / 4);
        this.setHealth(1 + this.rock_type / 4);
    }

    /** {@code getTotalArmorValue} (:67-69). */
    @Override
    public int getLegacyArmorValue() {
        return 0;
    }

    /** {@code fall} (:71-72): empty, no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /**
     * {@code onUpdate} (:74-156): remember the start (:75-78), tick (:79), no rotation (:80-83),
     * client reads the type (:84-86), server rolls type 0 (:87-138) and syncs (:139-141), client
     * particles for the crystals at 1/20 per tick (:142-155).
     */
    @Override
    public void tick() {
        if (this.dx == 0.0 && this.dz == 0.0) { // :75-78
            this.dx = this.getX();
            this.dz = this.getZ();
        }
        super.tick(); // :79
        this.setXRot(0.0f); // :80
        final float n = 0.0f;
        this.yHeadRot = n; // :82
        this.setYRot(n); // :83
        if (this.level().isClientSide) { // :84-86
            this.rock_type = this.getRockType();
        }
        if (!this.level().isClientSide && this.rock_type == 0) { // :87
            if (!this.level().dimension().equals(WorldProviderOreSpawn5.DIMENSION)) { // :88 dimensionId != DimensionID5 (Crystal)
                this.rock_type = 1; // :89
                if (this.level().random.nextInt(10) == 0) { // :90-92
                    this.rock_type = 2;
                }
                if (this.level().random.nextInt(20) == 0) { // :93-95
                    this.rock_type = 3;
                }
                if (this.level().random.nextInt(30) == 0) { // :96-98
                    this.rock_type = 4;
                }
                if (this.level().random.nextInt(40) == 0) { // :99-101
                    this.rock_type = 5;
                }
                if (this.level().random.nextInt(50) == 0) { // :102-104
                    this.rock_type = 6;
                }
                if (this.level().random.nextInt(100) == 0) { // :105-107
                    this.rock_type = 7;
                }
                if (this.level().random.nextInt(200) == 0) { // :108-110
                    this.rock_type = 8;
                }
                if (this.level().random.nextInt(500) == 0) { // :111-113
                    this.rock_type = 9;
                }
                if (this.level().random.nextInt(500) == 0) { // :114-116
                    this.rock_type = 10;
                }
                if (this.level().random.nextInt(500) == 0) { // :117-119
                    this.rock_type = 11;
                }
                if (this.level().random.nextInt(1000) == 0) { // :120-122
                    this.rock_type = 12;
                }
            } else { // :124-135
                this.rock_type = 9;
                if (this.level().random.nextInt(3) == 0) {
                    this.rock_type = 10;
                }
                if (this.level().random.nextInt(5) == 0) {
                    this.rock_type = 11;
                }
                if (this.level().random.nextInt(10) == 0) {
                    this.rock_type = 12;
                }
            }
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1 + this.rock_type / 4); // :136
            this.setHealth(1 + this.rock_type / 4); // :137
        }
        if (!this.level().isClientSide) { // :139-141
            this.setRockType(this.rock_type);
        }
        if (this.level().isClientSide) { // :142-155
            final Level w = this.level();
            if (this.rock_type == 9 && w.random.nextInt(20) == 0) { // :143-145 "flame"
                w.addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f,
                        w.random.nextFloat() / 10.0f,
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f);
            }
            if (this.rock_type == 10 && w.random.nextInt(20) == 0) { // :146-148 "happyVillager"
                w.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.25, this.getZ(),
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f,
                        w.random.nextFloat() / 2.0f,
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f);
            }
            if (this.rock_type == 11 && w.random.nextInt(20) == 0) { // :149-151 "smoke"
                w.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(),
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f,
                        w.random.nextFloat() / 10.0f,
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f);
            }
            if (this.rock_type == 12 && w.random.nextInt(20) == 0) { // :152-154 "fireworksSpark"
                w.addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() + 0.25, this.getZ(),
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f,
                        w.random.nextFloat() / 5.0f,
                        (w.random.nextFloat() - w.random.nextFloat()) / 60.0f);
            }
        }
    }

    /**
     * The inherited tail of 1.7.10 {@code EntityLiving.updateEntityActionState} (the "old AI" every
     * {@code EntityLiving} ran while {@code isAIEnabled()} was false, which RockBase never
     * overrides; {@code sw.bq()V}, read with javap): in water or lava {@code isJumping =
     * rand.nextFloat() < 0.8F}, and <em>nothing resets it</em> out of the liquid. A rock that
     * leaves water with the flag set keeps hopping on land until it enters a liquid again. The rest
     * of that method only turned the body, which {@link #tick()} zeroes, and despawned, which
     * {@link #removeWhenFarAway} forbids.
     *
     * <p>PORT: 1.21.1's {@code JumpControl.tick} clears {@code jumping} every server tick right after
     * this hook, so the sticky flag lives in {@link #legacyJumping} and is handed to the jump control
     * each tick. The jump itself (0.42 on ground, +0.04 in a liquid) is the vanilla one in both
     * versions.
     */
    @Override
    protected void customServerAiStep() {
        if (this.isInWater() || this.isInLava()) {
            this.legacyJumping = this.random.nextFloat() < 0.8f;
        }
        if (this.legacyJumping) {
            this.getJumpControl().jump();
        }
    }

    /** {@code getLivingSound} (:158-160). */
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    /** {@code getHurtSound} (:162-164). */
    @Nullable
    @Override
    protected SoundEvent getHurtSound(final DamageSource damageSource) {
        return null;
    }

    /** {@code getDeathSound} (:166-168). */
    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    /** {@code getSoundVolume} (:170-172). */
    @Override
    protected float getSoundVolume() {
        return 0.65f;
    }

    /** {@code getSoundPitch} (:174-176). */
    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    /** {@code canDespawn} (:182-184). {@code getDropItem} (:178-180) returned null: no loot table exists for {@code rock}. */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getCanSpawnHere} (:186-188): only y >= 50. */
    @Override
    public boolean checkSpawnRules(final LevelAccessor level, final MobSpawnType reason) {
        return this.getY() >= 50.0;
    }

    /**
     * PORT: 1.21.1 splits the 1.7.10 {@code getCanSpawnHere} into {@code checkSpawnRules} and this
     * collision/liquid check. The original override replaced the whole method, collision test
     * included, so spawners only asked for y >= 50; this keeps that.
     */
    @Override
    public boolean checkSpawnObstruction(final LevelReader level) {
        return true;
    }

    /** {@code canBeCollidedWith} (:190-192). */
    @Override
    public boolean isPickable() {
        return true;
    }

    /** {@code canBePushed} (:194-196). */
    @Override
    public boolean isPushable() {
        return true;
    }

    /**
     * {@code performHurtAnimation} (:198-203): hurt time and attacked yaw zeroed. In 1.7.10 that
     * method only served the animation packet; the red flash of a normal hit came from entity status
     * 2 ({@code EntityLivingBase.handleHealthUpdate}, which sets {@code hurtTime = 10} itself,
     * {@code sv.a(B)V} read with javap) and still showed. 1.21.1 has the same split:
     * {@code animateHurt} for the animation packet, {@code handleDamageEvent} for status 2 - so only
     * this one is emptied and the flash stays, as in the original.
     */
    @Override
    public void animateHurt(final float yaw) {
    }

    /** {@code onDeathUpdate} (:205-207): gone at once, no death animation, no XP. */
    @Override
    protected void tickDeath() {
        this.remove(Entity.RemovalReason.KILLED);
    }

    /**
     * {@code onDeath} (:209-247), without {@code super}: dead at once, then one item of the rock's
     * type (:211-246). No loot table, no XP, no {@code doMobLoot} check and no death event - the
     * 1.7.10 override skipped Forge's {@code onLivingDeath} hook in {@code EntityLivingBase.onDeath}
     * too.
     */
    @Override
    public void die(final DamageSource par1DamageSource) {
        this.remove(Entity.RemovalReason.KILLED); // :210 setDead()
        final ItemRock drop = ItemRock.byType(this.rock_type); // :211-246, one arm per type
        if (drop != null) {
            this.dropItemRand(drop, 1);
        }
    }

    /**
     * {@code dropItemRand} (:249-257): one item entity at {@code x +- 1/3}, {@code y + 0.25},
     * {@code z +- 1/3} from the shared {@code OreSpawnRand}; the item entity's own random start
     * motion (+-0.1, 0.2, +-0.1) is the same in both versions.
     */
    private ItemStack dropItemRand(final ItemLike index, final int par1) {
        ItemEntity var3 = null;
        final ItemStack is = new ItemStack(index, par1);
        var3 = new ItemEntity(this.level(),
                this.getX() + (OreSpawn.OreSpawnRand.nextFloat() - OreSpawn.OreSpawnRand.nextFloat()) / 3.0f,
                this.getY() + 0.25,
                this.getZ() + (OreSpawn.OreSpawnRand.nextFloat() - OreSpawn.OreSpawnRand.nextFloat()) / 3.0f,
                is);
        if (var3 != null) {
            this.level().addFreshEntity(var3);
        }
        return is;
    }

    /** {@code writeEntityToNBT} (:259-262). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("ButterflyType", this.rock_type);
    }

    /** {@code readEntityFromNBT} (:264-267). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.rock_type = par1NBTTagCompound.getInt("ButterflyType");
    }
}
