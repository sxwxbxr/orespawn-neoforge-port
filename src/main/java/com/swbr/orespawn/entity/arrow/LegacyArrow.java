package com.swbr.orespawn.entity.arrow;

import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.vehicle.Elevator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * What {@code UltimateArrow} (UltimateArrow.java:15-323) and {@code IrukandjiArrow}
 * (IrukandjiArrow.java:15-317) share: both are {@code EntityArrow}s whose {@code onUpdate} is a
 * complete copy of the 1.7.10 vanilla arrow physics on <em>private</em> fields of their own. No
 * original class; the differences between the two copies are the abstract hooks, in the order
 * they appear in the source.
 *
 * <p><b>Field hiding is the point.</b> {@code xTile}, {@code inGround}, {@code ticksInAir} and
 * friends shadowed the private fields of {@code EntityArrow}. {@link #inGround} hides
 * {@code AbstractArrow.inGround} the same way on purpose: {@code AbstractArrow.playerTouch} reads
 * the parent field, which is never set, so these arrows can never be picked up - exactly as the
 * inherited {@code onCollideWithPlayer} read the never-set parent field in 1.7.10
 * (verhalten/entity-09.md, IrukandjiArrow "Interaktion"). For the same reason nothing here is saved:
 * {@code AbstractArrow} writes its own (unused) fields, and a reloaded stuck arrow falls again
 * (verhalten/entity-13.md, UltimateArrow "Zustand"). The crit flag is DataWatcher 16, which is
 * vanilla's {@code ID_FLAGS} bit 1 - {@link #setCritArrow} / {@link #isCritArrow}.
 *
 * <p>{@link #tick} never calls {@code super.tick()}: the original called
 * {@code super.onEntityUpdate()} ({@link #baseTick}) and then its own physics, skipping
 * {@code EntityArrow.onUpdate}. So 1.21.1's pierce, enchantment effects, projectile-impact event
 * and deflection do not apply either.
 */
public abstract class LegacyArrow extends AbstractArrow {


    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    /**
     * PORT: 1.7.10 read {@code getBlock(-1, -1, -1)} as air because y below 0 was outside the world;
     * in 1.21.1 that position is a real block. The sentinel is kept explicitly until the first
     * block hit sets the tile.
     */
    private boolean tileSet = false;
    /** Hides {@code AbstractArrow.inGround} - see the class comment. */
    private boolean inGround = false;
    private int ticksInGround;
    private int ticksInAir = 0;
    private int knockbackStrength;

    /** {@code (World)} (:27-36): the registry factory; size 0.5 comes from the entity type. */
    protected LegacyArrow(EntityType<? extends LegacyArrow> type, Level level) {
        super(type, level);
    }

    /**
     * {@code (World, double, double, double)} (:38-47) - {@code EntityArrow(World, x, y, z)}:
     * {@code setSize(0.5, 0.5); setPosition(x, y, z); yOffset = 0} (bytecode {@code zc.<init>}).
     */
    protected LegacyArrow(EntityType<? extends LegacyArrow> type, Level level, double x, double y, double z) {
        super(type, level);
        this.setPos(x, y, z);
    }

    /**
     * {@code (World, EntityLiving, EntityLivingBase, float, float)} (:49-58), the mob-shot arrow of
     * Girlfriend and Boyfriend - {@code EntityArrow(World, shooter, target, speed, spread)} line by
     * line from the bytecode: y at the shooter's eyes minus 0.1, one block towards the target, aimed
     * at a third of its height with {@code 0.2 * horizontal distance} of lift.
     *
     * <p>PORT: that constructor never called {@code setSize}, so a mob-shot arrow kept
     * {@code Entity}'s 0.6x1.8 box. The box only widens the candidate search; the hit test is the
     * flight segment against the targets' boxes, so the port's 0.5 box hits identically.
     */
    protected LegacyArrow(EntityType<? extends LegacyArrow> type, Level level, LivingEntity shooter, LivingEntity target,
                          float speed, float spread) {
        super(type, level);
        this.setOwner(shooter); // shootingEntity = shooter; canBePickedUp = 1 for players (AbstractArrow.setOwner)
        double posY = shooter.getY() + (double) shooter.getEyeHeight() - 0.10000000149011612;
        this.setPos(this.getX(), posY, this.getZ());
        double d0 = target.getX() - shooter.getX();
        double d1 = target.getBoundingBox().minY + (double) (target.getBbHeight() / 3.0f) - posY;
        double d2 = target.getZ() - shooter.getZ();
        double d3 = (float) Math.sqrt(d0 * d0 + d2 * d2); // MathHelper.sqrt_double returns float
        if (d3 < 1.0E-7) {
            return;
        }
        float f2 = (float) (Math.atan2(d2, d0) * 180.0 / 3.1415927410125732) - 90.0f;
        float f3 = (float) -(Math.atan2(d1, d3) * 180.0 / 3.1415927410125732);
        double d4 = d0 / d3;
        double d5 = d2 / d3;
        this.moveTo(shooter.getX() + d4, posY, shooter.getZ() + d5, f2, f3);
        float f4 = (float) d3 * 0.2f;
        this.shoot(d0, d1 + (double) f4, d2, speed, spread);
    }

    /**
     * {@code (World, EntityPlayer, float)} (:60-69) - {@code EntityArrow(World, shooter, speed)}:
     * at the eyes, 0.16 to the side and 0.1 down, along the view at {@code speed * 1.5} with spread
     * 1.0 (bytecode {@code zc.<init>(ahb, sv, float)}).
     */
    protected LegacyArrow(EntityType<? extends LegacyArrow> type, Level level, LivingEntity shooter, float speed) {
        super(type, level);
        this.setOwner(shooter);
        this.moveTo(shooter.getX(), shooter.getY() + (double) shooter.getEyeHeight(), shooter.getZ(),
                shooter.getYRot(), shooter.getXRot());
        double x = this.getX() - (double) (Mth.cos(this.getYRot() / 180.0f * 3.1415927f) * 0.16f);
        double y = this.getY() - 0.10000000149011612;
        double z = this.getZ() - (double) (Mth.sin(this.getYRot() / 180.0f * 3.1415927f) * 0.16f);
        this.setPos(x, y, z);
        double motionX = -Mth.sin(this.getYRot() / 180.0f * 3.1415927f) * Mth.cos(this.getXRot() / 180.0f * 3.1415927f);
        double motionZ = Mth.cos(this.getYRot() / 180.0f * 3.1415927f) * Mth.cos(this.getXRot() / 180.0f * 3.1415927f);
        double motionY = -Mth.sin(this.getXRot() / 180.0f * 3.1415927f);
        this.shoot(motionX, motionY, motionZ, speed * 1.5f, 1.0f);
    }

    /** The damage of an entity hit before the crit bonus; {@code speed} is the flight speed. */
    protected abstract float hitDamage(float speed);

    /** What the PvP guard does to a protected target after the hit sound. */
    protected abstract void onPvpProtected(LivingEntity target);

    /** The in-ground test: is the arrow still held by the block now at its tile? */
    protected abstract boolean isStillStuck(BlockState state);

    /** One tick in the ground, after the counter was raised. */
    protected abstract void tickInGround(int ticksInGround);

    /** A block hit set the tile; record what was hit. */
    protected abstract void onTileSet(BlockPos pos, BlockState state);

    /** The last step of a block hit. */
    protected abstract void afterBlockHit(BlockPos pos);

    /**
     * {@code setThrowableHeading} (:75-94): normalise, scale - <em>no</em> random spread, the
     * parameter is ignored - set motion and both rotations. The dispenser and every constructor go
     * through here.
     */
    @Override
    public void shoot(double par1, double par3, double par5, float par7, float par8) {
        float var9 = (float) Math.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= var9;
        par3 /= var9;
        par5 /= var9;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        this.setDeltaMovement(par1, par3, par5);
        float var10 = (float) Math.sqrt(par1 * par1 + par5 * par5);
        float n = (float) (Math.atan2(par1, par5) * 180.0 / 3.141592653589793);
        this.setYRot(n);
        this.yRotO = n;
        float n2 = (float) (Math.atan2(par3, var10) * 180.0 / 3.141592653589793);
        this.setXRot(n2);
        this.xRotO = n2;
        this.ticksInGround = 0;
    }

    /** {@code setKnockbackStrength} (:313-315): read on a hit, not a 1.21.1 enchantment effect. */
    public void setKnockbackStrength(int par1) {
        this.knockbackStrength = par1;
    }

    /** {@code setDamage} (:317-318): empty. */
    @Override
    public void setBaseDamage(double baseDamage) {
    }

    /** The inherited {@code onCollideWithPlayer} gave a vanilla arrow - unreachable, see class comment. */
    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    /** The block at the tile; air until a block hit set it (see {@link #tileSet}). */
    private BlockState tileState() {
        if (!this.tileSet) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.level().getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile));
    }

    /** {@code onUpdate} (UltimateArrow.java:96-311, IrukandjiArrow.java:96-305). */
    @Override
    public void tick() {
        this.baseTick(); // super.onEntityUpdate()
        Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            float var1 = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
            float n = (float) (Math.atan2(motionX, motionZ) * 180.0 / 3.141592653589793);
            this.setYRot(n);
            this.yRotO = n;
            float n2 = (float) (Math.atan2(motionY, var1) * 180.0 / 3.141592653589793);
            this.setXRot(n2);
            this.xRotO = n2;
        }
        BlockState var2 = this.tileState();
        if (!var2.isAir()) {
            BlockPos tile = new BlockPos(this.xTile, this.yTile, this.zTile);
            VoxelShape var3 = var2.getCollisionShape(this.level(), tile);
            if (!var3.isEmpty()) {
                Vec3 here = this.position();
                for (AABB box : var3.toAabbs()) {
                    if (LegacyPhysics.isVecInside(box.move(tile), here)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }
        if (this.shakeTime > 0) {
            --this.shakeTime;
        }
        if (this.inGround) {
            BlockState var4 = this.tileState();
            if (this.isStillStuck(var4)) {
                ++this.ticksInGround;
                this.tickInGround(this.ticksInGround);
            } else {
                this.inGround = false;
                motionX *= this.random.nextFloat() * 0.2f;
                motionY *= this.random.nextFloat() * 0.2f;
                motionZ *= this.random.nextFloat() * 0.2f;
                this.setDeltaMovement(motionX, motionY, motionZ);
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            }
            return;
        }
        ++this.ticksInAir;
        double posX = this.getX();
        double posY = this.getY();
        double posZ = this.getZ();
        Vec3 var6 = new Vec3(posX, posY, posZ);
        Vec3 var7 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        // rayTraceBlocks(a, b, true) = func_147447_a(a, b, true, false, false): liquids only as
        // sources (BlockLiquid.canCollideCheck: meta 0), blocks without collision box by their outline.
        BlockHitResult clip = this.level().clip(
                new ClipContext(var6, var7, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, this));
        HitResult var8 = clip.getType() == HitResult.Type.MISS ? null : clip;
        var6 = new Vec3(posX, posY, posZ);
        var7 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
        if (var8 != null) {
            var7 = var8.getLocation();
        }
        Entity var9 = null;
        List<Entity> var10 = this.level().getEntities(this,
                this.getBoundingBox().expandTowards(motionX, motionY, motionZ).inflate(1.0, 1.0, 1.0));
        double var11 = 0.0;
        Entity shootingEntity = this.getOwner();
        for (Entity var13 : var10) {
            if (var13.isPickable() && (var13 != shootingEntity || this.ticksInAir >= 5) && !(var13 instanceof Elevator)) {
                // riddenByEntity != null -> isVehicle().
                if (var13 instanceof Cephadrome && var13.isVehicle()) {
                    continue;
                }
                if (var13 instanceof Dragon && var13.isVehicle()) {
                    continue;
                }
                // PORT: EntityHorse covered horse, donkey, mule, zombie and skeleton horse; 1.21.1
                // AbstractHorse also covers the camel and llamas, which did not exist.
                if (var13 instanceof AbstractHorse c3 && c3.isVehicle()) {
                    continue;
                }
                float var14 = 0.3f;
                AABB var15 = var13.getBoundingBox().inflate(var14, var14, var14);
                Vec3 var16 = LegacyPhysics.calculateIntercept(var15, var6, var7);
                if (var16 != null) {
                    double var17 = var6.distanceTo(var16);
                    if (var17 < var11 || var11 == 0.0) {
                        var9 = var13;
                        var11 = var17;
                    }
                }
            }
        }
        if (var9 != null) {
            var8 = new EntityHitResult(var9);
        }
        if (var8 != null) {
            if (var8 instanceof EntityHitResult entityHitResult) {
                Entity entityHit = entityHitResult.getEntity();
                float var18 = (float) Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
                float var19 = this.hitDamage(var18);
                if (OreSpawnConfig.TWEAKS.UltimateSwordPvp.get() == 0) {
                    if (entityHit instanceof Player || entityHit instanceof Girlfriend || entityHit instanceof Boyfriend) {
                        LivingEntity e = (LivingEntity) entityHit;
                        this.playSound(SoundEvents.ARROW_HIT, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
                        this.onPvpProtected(e);
                        this.discard();
                        return;
                    }
                    if (entityHit instanceof TamableAnimal t) {
                        if (t.isTame()) {
                            this.playSound(SoundEvents.ARROW_HIT, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
                            this.onPvpProtected(t);
                            this.discard();
                            return;
                        }
                    }
                }
                if (this.isCritArrow()) {
                    var19 += this.random.nextInt((int) var19 / 2 + 2);
                }
                DamageSource var20;
                if (shootingEntity == null) {
                    var20 = this.damageSources().arrow(this, this);
                } else {
                    var20 = this.damageSources().arrow(this, shootingEntity);
                }
                if (this.isOnFire()) {
                    entityHit.igniteForSeconds(5.0f);
                }
                if (entityHit.hurt(var20, var19)) {
                    // EntityLiving is Mob: players are EntityLivingBase, not EntityLiving, so they
                    // neither count arrows nor take the Punch knockback here.
                    if (entityHit instanceof Mob var21) {
                        if (!this.level().isClientSide) {
                            var21.setArrowCount(var21.getArrowCount() + 1);
                        }
                        if (this.knockbackStrength > 0) {
                            float var22 = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
                            if (var22 > 0.0f) {
                                entityHit.push(motionX * this.knockbackStrength * 0.6000000238418579 / var22, 0.1,
                                        motionZ * this.knockbackStrength * 0.6000000238418579 / var22);
                            }
                        }
                        // Dead in the original too: inside the EntityLiving branch the target is never a player.
                        if (shootingEntity != null && entityHit != shootingEntity && entityHit instanceof Player
                                && shootingEntity instanceof ServerPlayer serverPlayer) {
                            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0f));
                        }
                    }
                    this.playSound(SoundEvents.ARROW_HIT, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
                    this.discard();
                } else {
                    motionX *= -0.10000000149;
                    motionY *= -0.10000000149;
                    motionZ *= -0.10000000149;
                    this.setYRot(this.getYRot() + 180.0f);
                    this.yRotO += 180.0f;
                    this.ticksInAir = 0;
                }
            } else {
                BlockHitResult blockHit = (BlockHitResult) var8;
                BlockPos tile = blockHit.getBlockPos();
                this.xTile = tile.getX();
                this.yTile = tile.getY();
                this.zTile = tile.getZ();
                this.tileSet = true;
                this.onTileSet(tile, this.level().getBlockState(tile));
                Vec3 hitVec = blockHit.getLocation();
                motionX = (float) (hitVec.x - posX);
                motionY = (float) (hitVec.y - posY);
                motionZ = (float) (hitVec.z - posZ);
                float var18 = (float) Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
                posX -= motionX / var18 * 0.05;
                posY -= motionY / var18 * 0.05;
                posZ -= motionZ / var18 * 0.05;
                this.playSound(SoundEvents.ARROW_HIT, 1.0f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
                this.inGround = true;
                this.shakeTime = 7;
                this.setCritArrow(false);
                this.afterBlockHit(tile);
            }
        }
        if (this.isCritArrow()) {
            for (int var12 = 0; var12 < 4; ++var12) {
                this.level().addParticle(ParticleTypes.CRIT, posX + motionX * var12 / 4.0, posY + motionY * var12 / 4.0,
                        posZ + motionZ * var12 / 4.0, -motionX, -motionY + 0.2, -motionZ);
            }
        }
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float var18 = (float) Math.sqrt(motionX * motionX + motionZ * motionZ);
        float rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180.0 / 3.141592653589793);
        float rotationPitch = (float) (Math.atan2(motionY, var18) * 180.0 / 3.141592653589793);
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
        float var23 = 0.99f;
        final float var14 = 0.05f;
        if (this.isInWater()) {
            for (int var24 = 0; var24 < 4; ++var24) {
                final float var22 = 0.25f;
                this.level().addParticle(ParticleTypes.BUBBLE, posX - motionX * var22, posY - motionY * var22,
                        posZ - motionZ * var22, motionX, motionY, motionZ);
            }
            var23 = 0.8f;
        }
        motionX *= var23;
        motionY *= var23;
        motionZ *= var23;
        motionY -= var14;
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.setPos(posX, posY, posZ);
        this.checkInsideBlocks(); // func_145775_I = doBlockCollisions
    }
}
