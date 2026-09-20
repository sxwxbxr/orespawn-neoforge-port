package com.swbr.orespawn.entity.arrow;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.UltimateFishHook} (UltimateFishHook.java:19-483), registry id
 * {@code ultimate_fish_hook} (OreSpawnMain.java:3060, {@code registerGlobalEntityID} only; the
 * vanilla tracker handled it as an {@code EntityFishHook}: 64 blocks, every 5 ticks, with velocity,
 * bytecode {@code mn.a(sa)}). The fireproof bobber of the Ultimate Fishing Rod: fishes in water
 * <em>and lava</em>, with its own loot (verhalten/entity-13.md).
 *
 * <p>It extends {@link FishingHook} because {@code Player.fishing} is typed that way and
 * {@code FishingHookRenderer} draws the line from it. Every piece of vanilla logic is overridden:
 * {@link #tick} and {@link #retrieve} are the 1.7.10 {@code onUpdate} and
 * {@code func_146034_e} (MCP {@code handleHookRetraction}), on fields of this class - as the
 * original shadowed the private fields of {@code EntityFishHook}. What {@code FishingHook} still
 * contributes: {@code setOwner} sets {@code player.fishing}, {@code remove} clears it
 * ({@code setDead} :469-474), the spawn packet carries the angler, and the client kills a hook
 * without a player owner.
 *
 * <p>SRG names from {@code reference/jar/mcp/fields.csv}: {@code field_146037_g/h/i} xTile/yTile/
 * zTile, {@code field_146046_j} inTile, {@code field_146051_au} inGround, {@code field_146049_av}
 * ticksInGround, {@code field_146047_aw} ticksInAir, {@code field_146055_aB}
 * fishPosRotationIncrements, {@code field_146056_aC..aG} fishX/Y/Z/Yaw/Pitch,
 * {@code field_146061_aH/aI/aJ} clientMotionX/Y/Z, {@code field_146042_b} angler,
 * {@code field_146044_a} shake, {@code field_146043_c} caughtEntity.
 */
public class UltimateFishHook extends FishingHook {

    private int xTile;
    private int yTile;
    private int zTile;
    @Nullable
    private Block inTile;
    private boolean inGround;
    private int ticksInGround;
    private int ticksInAir;
    private int fish_on_hook;
    private int fish_wait_time;
    private int ticks_catchable;
    private float fish_direction;
    /** {@code field_146043_c}: public in the original. */
    @Nullable
    public Entity caughtEntity;
    private int fishPosRotationIncrements;
    private double fishX;
    private double fishY;
    private double fishZ;
    private double fishYaw;
    private double fishPitch;
    private double clientMotionX;
    private double clientMotionY;
    private double clientMotionZ;
    /** {@code field_146044_a}: a public field of {@code EntityFishHook}. */
    private int shake;
    /** {@code fishing_in_lava} (:50): set to 0, never read. */
    @SuppressWarnings("unused")
    private int fishing_in_lava;

    /**
     * {@code UltimateFishHook(World)} (:52-62) - the registry factory. Size 0.25, fire immunity
     * ({@code isImmuneToFire}) and {@code ignoreFrustumCheck} ({@code noCulling}, set by
     * {@code FishingHook}) come from the entity type.
     *
     * <p>PORT: {@code fireResistance = 3000} (:60, :71, :95, :100) has no 1.21.1 field; with fire
     * immunity it had no effect anyway.
     */
    public UltimateFishHook(EntityType<? extends UltimateFishHook> type, Level level) {
        super(type, level);
        this.fishing_in_lava = 0;
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
    }

    /**
     * {@code UltimateFishHook(World, EntityPlayer)} (:75-97): the rod's cast. Eye height is the
     * literal 1.62 of the original ({@code posY + 1.62 - yOffset}, and a server player's
     * {@code yOffset} was 0), not the sneaking-aware 1.21.1 value.
     *
     * <p>PORT: the client constructor (:64-73) has no successor: 1.21.1 builds the client copy from
     * the spawn packet, and {@code FishingHook.recreateFromPacket} sets the angler.
     */
    public UltimateFishHook(Level level, Player par2EntityPlayer) {
        this(ModEntities.ULTIMATE_FISH_HOOK.get(), level);
        this.setOwner(par2EntityPlayer); // angler = player; angler.fishEntity = this
        this.moveTo(par2EntityPlayer.getX(), par2EntityPlayer.getY() + 1.62, par2EntityPlayer.getZ(),
                par2EntityPlayer.getYRot(), par2EntityPlayer.getXRot());
        double posX = this.getX() - (double) (Mth.cos(this.getYRot() / 180.0f * 3.1415927f) * 0.16f);
        double posY = this.getY() - 0.10000000149011612;
        double posZ = this.getZ() - (double) (Mth.sin(this.getYRot() / 180.0f * 3.1415927f) * 0.16f);
        this.setPos(posX, posY, posZ);
        final float f = 0.4f;
        double motionX = -Mth.sin(this.getYRot() / 180.0f * 3.1415927f) * Mth.cos(this.getXRot() / 180.0f * 3.1415927f) * f;
        double motionZ = Mth.cos(this.getYRot() / 180.0f * 3.1415927f) * Mth.cos(this.getXRot() / 180.0f * 3.1415927f) * f;
        double motionY = -Mth.sin(this.getXRot() / 180.0f * 3.1415927f) * f;
        this.handleHookCasting(motionX, motionY, motionZ, 1.5f, 1.0f);
    }

    /** {@code func_146035_c} = handleHookCasting (:104-126): gaussian spread 0.0075. */
    public void handleHookCasting(double p_146035_1_, double p_146035_3_, double p_146035_5_, float p_146035_7_, float p_146035_8_) {
        float f2 = (float) Math.sqrt(p_146035_1_ * p_146035_1_ + p_146035_3_ * p_146035_3_ + p_146035_5_ * p_146035_5_);
        p_146035_1_ /= f2;
        p_146035_3_ /= f2;
        p_146035_5_ /= f2;
        p_146035_1_ += this.random.nextGaussian() * 0.007499999832361937 * p_146035_8_;
        p_146035_3_ += this.random.nextGaussian() * 0.007499999832361937 * p_146035_8_;
        p_146035_5_ += this.random.nextGaussian() * 0.007499999832361937 * p_146035_8_;
        p_146035_1_ *= p_146035_7_;
        p_146035_3_ *= p_146035_7_;
        p_146035_5_ *= p_146035_7_;
        this.setDeltaMovement(p_146035_1_, p_146035_3_, p_146035_5_);
        float f3 = (float) Math.sqrt(p_146035_1_ * p_146035_1_ + p_146035_5_ * p_146035_5_);
        float n = (float) (Math.atan2(p_146035_1_, p_146035_5_) * 180.0 / 3.141592653589793);
        this.setYRot(n);
        this.yRotO = n;
        float n2 = (float) (Math.atan2(p_146035_3_, f3) * 180.0 / 3.141592653589793);
        this.setXRot(n2);
        this.xRotO = n2;
        this.ticksInGround = 0;
    }

    /** {@code isInRangeToRenderDist} (:128-133). */
    @Override
    public boolean shouldRenderAtSqrDistance(double par1) {
        double d1 = this.getBoundingBox().getSize() * 4.0;
        d1 *= 64.0;
        return par1 < d1 * d1;
    }

    /** {@code setPositionAndRotation2} (:135-146): store the interpolation target. */
    @Override
    public void lerpTo(double par1, double par3, double par5, float par7, float par8, int par9) {
        this.fishX = par1;
        this.fishY = par3;
        this.fishZ = par5;
        this.fishYaw = par7;
        this.fishPitch = par8;
        this.fishPosRotationIncrements = par9;
        this.setDeltaMovement(this.clientMotionX, this.clientMotionY, this.clientMotionZ);
    }

    /** {@code setVelocity} (:148-156). */
    @Override
    public void lerpMotion(double par1, double par3, double par5) {
        this.clientMotionX = par1;
        this.clientMotionY = par3;
        this.clientMotionZ = par5;
        this.setDeltaMovement(par1, par3, par5);
    }

    /**
     * {@code onUpdate} (:158-372). The original called neither {@code super.onUpdate()} nor
     * {@code onEntityUpdate()}, so neither {@code super.tick()} nor {@code baseTick()} runs here.
     */
    @Override
    public void tick() {
        if (this.fishPosRotationIncrements > 0) {
            final double d7 = this.getX() + (this.fishX - this.getX()) / this.fishPosRotationIncrements;
            final double d8 = this.getY() + (this.fishY - this.getY()) / this.fishPosRotationIncrements;
            final double d9 = this.getZ() + (this.fishZ - this.getZ()) / this.fishPosRotationIncrements;
            final double d10 = Mth.wrapDegrees(this.fishYaw - this.getYRot());
            float rotationYaw = this.getYRot() + (float) (d10 / this.fishPosRotationIncrements);
            float rotationPitch = this.getXRot() + (float) ((this.fishPitch - this.getXRot()) / this.fishPosRotationIncrements);
            --this.fishPosRotationIncrements;
            this.setPos(d7, d8, d9);
            this.setYRot(rotationYaw % 360.0f); // setRotation(yaw, pitch)
            this.setXRot(rotationPitch % 360.0f);
            return;
        }
        Player angler = this.getPlayerOwner();
        if (!this.level().isClientSide) {
            if (angler == null) {
                // PORT: a hook loaded from disk or summoned has no angler; the original dereferenced
                // it and crashed the server tick (R18 case 1).
                this.discard();
                return;
            }
            // getCurrentEquippedItem is the main hand. PORT: 1.7.10 had no off-hand; kept main-hand
            // only, so a hook cast from the off-hand is removed on its first server tick.
            final ItemStack itemstack = angler.getMainHandItem();
            if (angler.isRemoved() || !angler.isAlive() || itemstack.isEmpty()
                    || itemstack.getItem() != ModItems.ULTIMATE_FISHING_ROD.get() || this.distanceToSqr(angler) > 1024.0) {
                this.discard(); // setDead + angler.fishEntity = null (FishingHook.remove)
                return;
            }
            if (this.caughtEntity != null) {
                if (!this.caughtEntity.isRemoved()) {
                    // PORT: the original wrote posX/posY/posZ without setPosition, leaving the bounding
                    // box behind; 1.21.1 has no position without its box.
                    this.setPos(this.caughtEntity.getX(),
                            this.caughtEntity.getBoundingBox().minY + this.caughtEntity.getBbHeight() * 0.8,
                            this.caughtEntity.getZ());
                    return;
                }
                this.caughtEntity = null;
            }
        }
        if (this.shake > 0) {
            --this.shake;
        }
        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        if (this.inGround) {
            // xTile/inTile are never assigned by onUpdate: inTile stays null, the test is always false
            // and the hook bounces off instead of sticking (kept, R18).
            if (this.level().getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
                ++this.ticksInGround;
                if (this.ticksInGround == 1200) {
                    this.discard();
                }
                return;
            }
            this.inGround = false;
            motionX *= this.random.nextFloat() * 0.2f;
            motionY *= this.random.nextFloat() * 0.2f;
            motionZ *= this.random.nextFloat() * 0.2f;
            this.setDeltaMovement(motionX, motionY, motionZ);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        } else {
            ++this.ticksInAir;
        }
        Vec3 vec31 = new Vec3(this.getX(), this.getY(), this.getZ());
        Vec3 vec32 = new Vec3(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);
        // rayTraceBlocks(a, b) = func_147447_a(a, b, false, false, false): no liquids, outline shapes.
        BlockHitResult clip = this.level().clip(new ClipContext(vec31, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
        HitResult movingobjectposition = clip.getType() == HitResult.Type.MISS ? null : clip;
        vec31 = new Vec3(this.getX(), this.getY(), this.getZ());
        vec32 = new Vec3(this.getX() + motionX, this.getY() + motionY, this.getZ() + motionZ);
        if (movingobjectposition != null) {
            vec32 = movingobjectposition.getLocation();
        }
        Entity entity = null;
        final List<Entity> list = this.level().getEntities(this,
                this.getBoundingBox().expandTowards(motionX, motionY, motionZ).inflate(1.0, 1.0, 1.0));
        double d11 = 0.0;
        for (final Entity entity2 : list) {
            if (entity2.isPickable() && (entity2 != angler || this.ticksInAir >= 5)) {
                final float f = 0.3f;
                final AABB axisalignedbb = entity2.getBoundingBox().inflate(f, f, f);
                final Vec3 movingobjectposition2 = LegacyPhysics.calculateIntercept(axisalignedbb, vec31, vec32);
                if (movingobjectposition2 != null) {
                    final double d12 = vec31.distanceTo(movingobjectposition2);
                    if (d12 < d11 || d11 == 0.0) {
                        entity = entity2;
                        d11 = d12;
                    }
                }
            }
        }
        if (entity != null) {
            movingobjectposition = new EntityHitResult(entity);
        }
        if (movingobjectposition != null) {
            if (movingobjectposition instanceof EntityHitResult entityHit) {
                // DamageSource.causeThrownDamage(this, angler): direct this, cause angler.
                if (entityHit.getEntity().hurt(this.damageSources().thrown(this, angler), 0.0f)) {
                    this.caughtEntity = entityHit.getEntity();
                }
            } else {
                this.inGround = true;
            }
        }
        if (this.inGround) {
            return;
        }
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
        motion = this.getDeltaMovement(); // moveEntity zeroed the motion on colliding axes
        motionX = motion.x;
        motionY = motion.y;
        motionZ = motion.z;
        final float f2 = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
        float rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0 / 3.141592653589793);
        float rotationPitch = (float) (Math.atan2(motionY, f2) * 180.0 / 3.141592653589793);
        while (rotationPitch - this.xRotO < -180.0f) {
            this.xRotO -= 360.0f;
        }
        while (rotationPitch - this.xRotO >= 180.0f) {
            this.xRotO += 360.0f;
        }
        while (rotationYaw - this.yRotO < -180.0f) {
            this.yRotO -= 360.0f;
        }
        while (rotationYaw - this.yRotO >= 180.0f) {
            this.yRotO += 360.0f;
        }
        this.setXRot(this.xRotO + (rotationPitch - this.xRotO) * 0.2f);
        this.setYRot(this.yRotO + (rotationYaw - this.yRotO) * 0.2f);
        float f3 = 0.92f;
        if (this.onGround() || this.horizontalCollision) {
            f3 = 0.5f;
        }
        final byte b0 = 5;
        double d13 = 0.0;
        AABB boundingBox = this.getBoundingBox();
        for (int j = 0; j < b0; ++j) {
            final double d14 = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * (j + 0) / b0 - 0.125 + 0.125;
            final double d15 = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * (j + 1) / b0 - 0.125 + 0.125;
            final AABB axisalignedbb2 = new AABB(boundingBox.minX, d14, boundingBox.minZ, boundingBox.maxX, d15, boundingBox.maxZ);
            if (LegacyPhysics.isAABBInMaterial(this.level(), axisalignedbb2, FluidTags.WATER)) {
                d13 += 1.0 / b0;
            }
            if (LegacyPhysics.isAABBInMaterial(this.level(), axisalignedbb2, FluidTags.LAVA)) {
                d13 += 1.0 / b0;
            }
        }
        if (!this.level().isClientSide && d13 > 0.0) {
            final ServerLevel worldserver = (ServerLevel) this.level();
            int k = 1;
            BlockPos above = new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()) + 1, Mth.floor(this.getZ()));
            if (this.random.nextFloat() < 0.25f && this.level().isRainingAt(above)) { // canLightningStrikeAt
                k = 2;
            }
            if (this.random.nextFloat() < 0.5f && !this.level().canSeeSky(above)) {
                --k;
            }
            if (this.fish_on_hook > 0) {
                --this.fish_on_hook;
                if (this.fish_on_hook <= 0) {
                    this.fish_wait_time = 0;
                    this.ticks_catchable = 0;
                }
            } else if (this.ticks_catchable > 0) {
                this.ticks_catchable -= k;
                if (this.ticks_catchable <= 0) {
                    motionY -= 0.20000000298023224;
                    // PORT: "random.splash" is entity.fishing_bobber.splash in 1.21.1's sound map.
                    this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
                    final float f4 = (float) Mth.floor(this.getBoundingBox().minY);
                    worldserver.sendParticles(ParticleTypes.BUBBLE, this.getX(), f4 + 1.0f, this.getZ(),
                            (int) (1.0f + this.getBbWidth() * 20.0f), this.getBbWidth(), 0.0, this.getBbWidth(), 0.20000000298023224);
                    // "wake" is the 1.21.1 FISHING particle.
                    worldserver.sendParticles(ParticleTypes.FISHING, this.getX(), f4 + 1.0f, this.getZ(),
                            (int) (1.0f + this.getBbWidth() * 20.0f), this.getBbWidth(), 0.0, this.getBbWidth(), 0.20000000298023224);
                    this.fish_on_hook = Mth.nextInt(this.random, 10, 30);
                } else {
                    this.fish_direction += (float) (this.random.nextGaussian() * 4.0);
                    final float f4 = this.fish_direction * 0.017453292f;
                    final float f5 = Mth.sin(f4);
                    final float f6 = Mth.cos(f4);
                    final double d16 = this.getX() + f5 * this.ticks_catchable * 0.1f;
                    final double d17 = Mth.floor(this.getBoundingBox().minY) + 1.0f;
                    final double d18 = this.getZ() + f6 * this.ticks_catchable * 0.1f;
                    if (this.random.nextFloat() < 0.15f) {
                        worldserver.sendParticles(ParticleTypes.BUBBLE, d16, d17 - 0.10000000149011612, d18, 1, f5, 0.1, f6, 0.0);
                    }
                    final float f7 = f5 * 0.04f;
                    final float f8 = f6 * 0.04f;
                    worldserver.sendParticles(ParticleTypes.FISHING, d16, d17, d18, 0, f8, 0.01, -f7, 1.0);
                    worldserver.sendParticles(ParticleTypes.FISHING, d16, d17, d18, 0, -f8, 0.01, f7, 1.0);
                }
            } else if (this.fish_wait_time > 0) {
                this.fish_wait_time -= k;
                float f4 = 0.15f;
                if (this.fish_wait_time < 20) {
                    f4 += (float) ((20 - this.fish_wait_time) * 0.05);
                } else if (this.fish_wait_time < 40) {
                    f4 += (float) ((40 - this.fish_wait_time) * 0.02);
                } else if (this.fish_wait_time < 60) {
                    f4 += (float) ((60 - this.fish_wait_time) * 0.01);
                }
                if (this.random.nextFloat() < f4) {
                    final float f5 = Mth.nextFloat(this.random, 0.0f, 360.0f) * 0.017453292f;
                    final float f6 = Mth.nextFloat(this.random, 25.0f, 60.0f);
                    final double d16 = this.getX() + Mth.sin(f5) * f6 * 0.1f;
                    final double d17 = Mth.floor(this.getBoundingBox().minY) + 1.0f;
                    final double d18 = this.getZ() + Mth.cos(f5) * f6 * 0.1f;
                    worldserver.sendParticles(ParticleTypes.SPLASH, d16, d17, d18, 2 + this.random.nextInt(2),
                            0.10000000149011612, 0.0, 0.10000000149011612, 0.0);
                }
                if (this.fish_wait_time <= 0) {
                    this.fish_direction = Mth.nextFloat(this.random, 0.0f, 360.0f);
                    this.ticks_catchable = Mth.nextInt(this.random, 100, 200);
                }
            } else {
                this.fish_wait_time = Mth.nextInt(this.random, 50, 300);
                this.fish_wait_time -= lureLevel(angler) * 20 * 5;
            }
            if (this.fish_on_hook > 0) {
                motionY -= this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat() * 0.2;
            }
        }
        final double d12 = d13 * 2.0 - 1.0;
        motionY += 0.03999999910593033 * d12;
        if (d13 > 0.0) {
            f3 *= (float) 0.9;
            motionY *= 0.8;
        }
        motionX *= f3;
        motionY *= f3;
        motionZ *= f3;
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.setPos(this.getX(), this.getY(), this.getZ());
    }

    /**
     * {@code writeEntityToNBT} (:374-381). PORT: the block is written as its registry id instead of
     * a numeric id.
     */
    @Override
    public void addAdditionalSaveData(CompoundTag par1NBTTagCompound) {
        par1NBTTagCompound.putShort("xTile", (short) this.xTile);
        par1NBTTagCompound.putShort("yTile", (short) this.yTile);
        par1NBTTagCompound.putShort("zTile", (short) this.zTile);
        par1NBTTagCompound.putString("inTile", this.inTile == null ? "" : BuiltInRegistries.BLOCK.getKey(this.inTile).toString());
        par1NBTTagCompound.putByte("shake", (byte) this.shake);
        par1NBTTagCompound.putByte("inGround", (byte) (this.inGround ? 1 : 0));
    }

    /** {@code readEntityFromNBT} (:383-390); an unknown or empty block id reads as air, like id 0. */
    @Override
    public void readAdditionalSaveData(CompoundTag par1NBTTagCompound) {
        this.xTile = par1NBTTagCompound.getShort("xTile");
        this.yTile = par1NBTTagCompound.getShort("yTile");
        this.zTile = par1NBTTagCompound.getShort("zTile");
        ResourceLocation id = ResourceLocation.tryParse(par1NBTTagCompound.getString("inTile"));
        this.inTile = id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(id);
        this.shake = par1NBTTagCompound.getByte("shake") & 0xFF;
        this.inGround = par1NBTTagCompound.getByte("inGround") == 1;
    }

    /**
     * {@code func_146034_e} = handleHookRetraction (:397-437): pull a caught entity (3 durability),
     * land a catch with 1-6 XP (1), or report a grounded hook (2). The rod applies the result.
     *
     * <p>The pull on a caught entity only changes its server-side motion, as in the original - a
     * hooked player is not moved on his own client (no velocity packet).
     */
    @Override
    public int retrieve(ItemStack stack) {
        if (this.level().isClientSide) {
            return 0;
        }
        Player angler = this.getPlayerOwner();
        if (angler == null) {
            // PORT: unreachable through the rod (it passes its own player); guarded instead of an NPE.
            this.discard();
            return 0;
        }
        byte b0 = 0;
        if (this.caughtEntity != null) {
            final double d0 = angler.getX() - this.getX();
            final double d2 = angler.getY() - this.getY();
            final double d3 = angler.getZ() - this.getZ();
            final double d4 = (float) Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
            final double d5 = 0.1;
            this.caughtEntity.setDeltaMovement(this.caughtEntity.getDeltaMovement().add(
                    d0 * d5, d2 * d5 + (float) Math.sqrt(d4) * 0.08, d3 * d5));
            b0 = 3;
        } else if (this.fish_on_hook > 0) {
            final ItemEntity entityitem = new ItemEntity(this.level(), this.getX(), this.getY() + 1.25, this.getZ(), this.getFishingLoot(angler));
            final double d6 = angler.getX() - this.getX();
            final double d7 = angler.getY() - this.getY();
            final double d8 = angler.getZ() - this.getZ();
            final double d9 = (float) Math.sqrt(d6 * d6 + d7 * d7 + d8 * d8);
            final double d10 = 0.1;
            // PORT: entityitem.fireResistance = 3000 (:426) only delayed catching fire; the damage
            // from lava or fire that destroys an item is the same without it.
            entityitem.setDeltaMovement(d6 * d10, d7 * d10 + (float) Math.sqrt(d9) * 0.08, d8 * d10);
            this.level().addFreshEntity(entityitem);
            angler.level().addFreshEntity(new ExperienceOrb(angler.level(), angler.getX(), angler.getY() + 0.5,
                    angler.getZ() + 0.5, this.random.nextInt(6) + 1));
            b0 = 1;
        }
        if (this.inGround) {
            b0 = 2;
        }
        this.discard(); // setDead + angler.fishEntity = null
        return b0;
    }

    /**
     * {@code func_146033_f} (:439-467): lava always gives lava fish; otherwise junk, treasure, and
     * half vanilla fish, half OreSpawn fish.
     *
     * <p>PORT: 1.21.1 has no "junk fished" and "treasure fished" statistics
     * ({@code StatList.field_151183_A}, {@code field_151184_B}); those two {@code addStat} calls are
     * gone, {@code fishCaughtStat} stays.
     */
    private ItemStack getFishingLoot(Player angler) {
        float f = this.level().random.nextFloat();
        final int i = luckOfTheSeaLevel(angler);
        final int j = lureLevel(angler);
        float f2 = 0.1f - i * 0.025f - j * 0.01f;
        float f3 = 0.05f + i * 0.01f - j * 0.01f;
        f2 = Mth.clamp(f2, 0.0f, 1.0f);
        f3 = Mth.clamp(f3, 0.0f, 1.0f);
        // :447 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20: the truncation read the neighbouring block below 0).
        final Block bid = this.level().getBlockState(new BlockPos(Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()))).getBlock();
        // handleLavaMovement: isMaterialInBB(boundingBox.expand(-0.1, -0.4, -0.1), lava) (bytecode sa.P).
        if (LegacyPhysics.isMaterialInBB(this.level(), this.getBoundingBox().inflate(-0.1, -0.4, -0.1), FluidTags.LAVA)
                || bid == Blocks.LAVA) { // lava and flowing_lava are one block now
            angler.awardStat(Stats.FISH_CAUGHT, 1);
            return pick(this.random, lavaFish()).getItemStack(this.random, this.level());
        }
        if (f < f2) {
            return pick(this.random, junk()).getItemStack(this.random, this.level());
        }
        f -= f2;
        if (f < f3) {
            return pick(this.random, treasure()).getItemStack(this.random, this.level());
        }
        final float f4 = this.level().random.nextFloat();
        angler.awardStat(Stats.FISH_CAUGHT, 1);
        if (f4 < 0.5f) {
            return pick(this.random, vanillaFish()).getItemStack(this.random, this.level());
        }
        return pick(this.random, orespawnFish()).getItemStack(this.random, this.level());
    }

    /** {@code EnchantmentHelper.func_151386_g(angler)}: Luck of the Sea on the held item (bytecode {@code afv.g}). */
    private static int luckOfTheSeaLevel(Player angler) {
        return PreEnchant.level(angler.getMainHandItem(), angler.level(), Enchantments.LUCK_OF_THE_SEA);
    }

    /** {@code EnchantmentHelper.func_151387_h(angler)}: Lure on the held item (bytecode {@code afv.h}). */
    private static int lureLevel(Player angler) {
        return PreEnchant.level(angler.getMainHandItem(), angler.level(), Enchantments.LURE);
    }

    /**
     * {@code WeightedRandomFishable} (bytecode {@code xf}): a stack, a weight, an optional random
     * wear ({@code func_150709_a}, setMaxDamagePercent) and random enchanting ({@code func_150707_a}).
     */
    private record Fishable(Supplier<ItemStack> stack, int weight, float maxDamagePercent, boolean enchantable) {

        Fishable(Supplier<ItemStack> stack, int weight) {
            this(stack, weight, 0.0f, false);
        }

        /**
         * {@code func_150708_a} (xf.a(Random)): copy; with wear, {@code i = (int)(percent * maxDamage)},
         * {@code j = maxDamage - rand(rand(i) + 1)} clamped to 1..i as the damage; with enchanting,
         * {@code EnchantmentHelper.addRandomEnchantment(rand, stack, 30)}.
         *
         * <p>PORT: {@code addRandomEnchantment} is {@code EnchantmentHelper.enchantItem} over the
         * {@code #minecraft:in_enchanting_table} enchantments - the 1.7.10 table selection had no
         * treasure enchantments. In 1.21.1 a book becomes a <em>new</em> enchanted-book stack, so
         * the return value is used; the original converted the stack in place.
         */
        ItemStack getItemStack(RandomSource rand, Level level) {
            ItemStack base = this.stack.get();
            ItemStack itemstack = base.copy();
            if (this.maxDamagePercent > 0.0f) {
                int i = (int) (this.maxDamagePercent * base.getMaxDamage());
                int j = itemstack.getMaxDamage() - rand.nextInt(rand.nextInt(i) + 1);
                if (j > i) {
                    j = i;
                }
                if (j < 1) {
                    j = 1;
                }
                itemstack.setDamageValue(j);
            }
            if (this.enchantable) {
                itemstack = EnchantmentHelper.enchantItem(rand, itemstack, 30, level.registryAccess(),
                        level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.IN_ENCHANTING_TABLE));
            }
            return itemstack;
        }
    }

    /** {@code WeightedRandom.getRandomItem(rand, collection)} (bytecode {@code qv.a}). */
    private static Fishable pick(RandomSource rand, List<Fishable> list) {
        int total = 0;
        for (Fishable f : list) {
            total += f.weight();
        }
        int j = rand.nextInt(total);
        for (Fishable f : list) {
            j -= f.weight();
            if (j < 0) {
                return f;
            }
        }
        return list.get(list.size() - 1); // unreachable: the weights sum to total
    }

    private static Supplier<ItemStack> of(Item item) {
        return () -> new ItemStack(item);
    }

    /** {@code field_146039_d} (:477): junk. {@code Items.potionitem} meta 0 is the water bottle; {@code dye} meta 0 is the ink sac. */
    private static List<Fishable> junk() {
        return List.of(
                new Fishable(of(Items.LEATHER_BOOTS), 10, 0.9f, false),
                new Fishable(of(Items.LEATHER), 10),
                new Fishable(of(Items.BONE), 10),
                new Fishable(() -> PotionContents.createItemStack(Items.POTION, Potions.WATER), 10),
                new Fishable(of(Items.STRING), 5),
                new Fishable(of(Items.FISHING_ROD), 2, 0.9f, false),
                new Fishable(of(Items.BOWL), 10),
                new Fishable(of(Items.STICK), 5),
                new Fishable(() -> new ItemStack(Items.INK_SAC, 10), 1),
                new Fishable(of(Items.TRIPWIRE_HOOK), 10),
                new Fishable(of(Items.ROTTEN_FLESH), 10));
    }

    /** {@code field_146041_e} (:478): treasure. */
    private static List<Fishable> treasure() {
        return List.of(
                new Fishable(of(Items.LILY_PAD), 1),
                new Fishable(of(Items.NAME_TAG), 1),
                new Fishable(of(Items.SADDLE), 1),
                new Fishable(of(Items.BOW), 1, 0.25f, true),
                new Fishable(of(Items.FISHING_ROD), 1, 0.25f, true),
                new Fishable(of(Items.BOOK), 1, 0.0f, true));
    }

    /** {@code field_146036_f} (:479): cod, salmon, clownfish (tropical fish), pufferfish. */
    private static List<Fishable> vanillaFish() {
        return List.of(
                new Fishable(of(Items.COD), 60),
                new Fishable(of(Items.SALMON), 25),
                new Fishable(of(Items.TROPICAL_FISH), 2),
                new Fishable(of(Items.PUFFERFISH), 13));
    }

    /**
     * {@code orespawn_lava_fish} (:480). PORT: {@code sunspoturchin} is registered by the throwables
     * port of this wave under a holder name this class cannot know; it is looked up by its manifest
     * id when the catch is made.
     */
    private static List<Fishable> lavaFish() {
        return List.of(
                new Fishable(() -> new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "sunspoturchin"))), 25),
                new Fishable(() -> new ItemStack(ModItems.LAVA_EEL.get()), 10),
                new Fishable(() -> new ItemStack(ModItems.SUN_FISH.get()), 15),
                new Fishable(() -> new ItemStack(ModItems.SPARK_FISH.get()), 10),
                new Fishable(() -> new ItemStack(ModItems.FIRE_FISH.get()), 15));
    }

    /** {@code orespawn_fish} (:481). */
    private static List<Fishable> orespawnFish() {
        return List.of(
                new Fishable(() -> new ItemStack(ModItems.BLUE_FISH.get()), 25),
                new Fishable(() -> new ItemStack(ModItems.PINK_FISH.get()), 10),
                new Fishable(() -> new ItemStack(ModItems.ROCK_FISH.get()), 15),
                new Fishable(() -> new ItemStack(ModItems.WOOD_FISH.get()), 10),
                new Fishable(() -> new ItemStack(ModItems.GREY_FISH.get()), 15));
    }
}
