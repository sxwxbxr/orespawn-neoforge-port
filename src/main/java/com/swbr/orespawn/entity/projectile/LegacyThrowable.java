package com.swbr.orespawn.entity.projectile;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * The 1.7.10 vanilla {@code EntityThrowable} ({@code zk} in {@code reference/jar/mcp/client-1.7.10.jar}),
 * which LaserBall, Acid, IceBall, DeadIrukandji, WaterBall, InkSack, SunspotUrchin, ThunderBolt,
 * EntityThrownRock, BerthaHit and Shoes extended. No original OreSpawn class: it carries the vanilla numbers the catalogue marked as open
 * (README 6.4), read from the bytecode.
 *
 * <p>Why not 1.21.1's {@code ThrowableProjectile}: its tick differs in four ways that change play.
 * It traces blocks with the collision shape (1.7.10 used the selection box, so tall grass, torches
 * and portals stopped a throwable), excludes the owner until the box has left it (1.7.10: the
 * thrower for the first five ticks), detects entity hits on the client too (1.7.10: server only),
 * and 1.21.1's {@code AABB.clip} misses a ray that starts inside the target box. So this class
 * extends {@link Projectile} and ports {@code zk.h()} line by line:
 * <ul>
 *   <li>velocity {@code func_70182_d} = 1.5, pitch offset {@code func_70183_g} = 0, inaccuracy 1.0
 *       (zk.e, zk.f, zk constructor);</li>
 *   <li>gravity {@code func_70185_h} = 0.03, drag 0.99, in water 0.8 with four bubbles (zk.i, zk.h);</li>
 *   <li>heading spread {@code nextGaussian() * 0.0075 * inaccuracy} (zk.c(DDDFF));</li>
 *   <li>entity search box = bounding box expanded by the motion and by 1.0, target boxes grown by
 *       0.3, nearest intercept wins (zk.h).</li>
 * </ul>
 * {@code inGround} and {@code throwableShake} exist in zk but nothing in {@code zk.h()} ever sets
 * {@code inGround}; the dead branch is not carried over.
 */
public abstract class LegacyThrowable extends Projectile {

    /** {@code (double)(float)Math.PI}, the constant javac folded into zk's rotation code. */
    static final double LEGACY_PI = 3.1415927410125732D;

    /** zk field {@code at}: ticks since the throw; the thrower is ignored for the first five. */
    protected int ticksInAir;

    protected LegacyThrowable(EntityType<? extends LegacyThrowable> type, Level level) {
        super(type, level);
    }

    /** {@code EntityThrowable(World, double, double, double)}: position only, no motion. */
    protected LegacyThrowable(EntityType<? extends LegacyThrowable> type, Level level, double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    /** {@code EntityThrowable(World, EntityLivingBase)}: from the thrower's eyes along its look. */
    protected LegacyThrowable(EntityType<? extends LegacyThrowable> type, Level level, LivingEntity thrower) {
        this(type, level);
        this.setOwner(thrower);
        // PORT: posY + getEyeHeight() -> getEyeY(). The 1.7.10 server player returned a constant 1.62
        // (EntityPlayerMP, mw.g()); 1.21.1 lowers the eyes while sneaking.
        this.moveTo(thrower.getX(), thrower.getEyeY(), thrower.getZ(), thrower.getYRot(), thrower.getXRot());
        float yawRad = this.getYRot() / 180.0F * (float) Math.PI;
        float pitchRad = this.getXRot() / 180.0F * (float) Math.PI;
        double x = this.getX() - (double) (Mth.cos(yawRad) * 0.16F);
        double y = this.getY() - 0.10000000149011612D;
        double z = this.getZ() - (double) (Mth.sin(yawRad) * 0.16F);
        this.setPos(x, y, z);
        float f = 0.4F;
        double motionX = (double) (-Mth.sin(yawRad) * Mth.cos(pitchRad) * f);
        double motionZ = (double) (Mth.cos(yawRad) * Mth.cos(pitchRad) * f);
        double motionY = (double) (-Mth.sin((this.getXRot() + this.getThrowPitchOffset()) / 180.0F * (float) Math.PI) * f);
        this.setThrowableHeading(motionX, motionY, motionZ, this.getThrowVelocity(), 1.0F);
    }

    /** {@code func_70182_d}: launch speed of the thrower constructor. */
    protected float getThrowVelocity() {
        return 1.5F;
    }

    /** {@code func_70183_g}: pitch added to the thrower's look for the launch direction. */
    protected float getThrowPitchOffset() {
        return 0.0F;
    }

    /** {@code func_70185_h}: subtracted from the vertical motion each tick, after drag. */
    protected float getGravityVelocity() {
        return 0.03F;
    }

    /**
     * {@code setThrowableHeading} (zk.c(DDDFF)): normalise, add a Gaussian spread of 0.0075 per
     * point of inaccuracy, scale by the velocity, face the flight direction. Later waves call this
     * exactly where their originals did (Robot3.java:252, Dragon.java:1103, TheKing.java:750).
     */
    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        x /= (double) length;
        y /= (double) length;
        z /= (double) length;
        x += this.random.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        y += this.random.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        z += this.random.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        x *= (double) velocity;
        y *= (double) velocity;
        z *= (double) velocity;
        this.setDeltaMovement(x, y, z);
        float horizontal = (float) Math.sqrt(x * x + z * z);
        this.setYRot((float) (Math.atan2(x, z) * 180.0D / LEGACY_PI));
        this.yRotO = this.getYRot();
        this.setXRot((float) (Math.atan2(y, (double) horizontal) * 180.0D / LEGACY_PI));
        this.xRotO = this.getXRot();
        this.hasImpulse = true;
    }

    /**
     * Routes 1.21.1's {@code shoot} (used by dispensers and {@code shootFromRotation}) through the
     * 1.7.10 heading, which spreads with a Gaussian instead of 1.21.1's triangle distribution.
     */
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        this.setThrowableHeading(x, y, z, velocity, inaccuracy);
    }

    /** {@code getThrower()}: the living owner, or null for dispensers and the coordinate constructor. */
    @Nullable
    public LivingEntity getThrower() {
        return this.getOwner() instanceof LivingEntity living ? living : null;
    }

    /** zk.c() is empty: no DataWatcher entries. */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    /** zk.a(double): average edge length x 4 x 64. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = this.getBoundingBox().getSize() * 4.0D;
        size *= 64.0D;
        return distance < size * size;
    }

    /** A 1.7.10 throwable that hit a portal block was put into it (zk.h, {@code setInPortal}). */
    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return true;
    }

    /**
     * Whether {@code candidate} may be picked by the entity search of {@link #tick}. Not in 1.7.10: every throwable
     * there took every entity with {@code canBeCollidedWith}. The hook exists for entities whose server box 1.7.10
     * never moved and the port does (R25): {@link ThunderBolt} and {@link IceBall} leave the King's and the Queen's
     * heads out, see {@link LegacyProjectiles#isRoyalHead}.
     */
    protected boolean canHitCandidate(Entity candidate) {
        return true;
    }

    /** {@code onImpact(MovingObjectPosition)}. Block hits arrive on both sides, entity hits on the server only. */
    protected abstract void onImpact(HitResult result);

    /** {@code EntityThrowable.onUpdate()} (zk.h). */
    @Override
    public void tick() {
        super.tick();
        ++this.ticksInAir;
        Vec3 from = this.position();
        Vec3 to = from.add(this.getDeltaMovement());
        HitResult hit = LegacyProjectiles.rayTraceBlocks(this, from, to);
        from = this.position();
        to = from.add(this.getDeltaMovement());
        if (hit != null) {
            to = hit.getLocation();
        }
        if (!this.level().isClientSide) {
            Entity found = null;
            // PORT: getEntities(Entity, AABB) skips spectators, which 1.7.10 did not have.
            List<Entity> candidates = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D));
            double nearest = 0.0D;
            LivingEntity thrower = this.getThrower();
            for (Entity candidate : candidates) {
                // PORT: canHitCandidate is not in zk.h(); it is true unless a subclass excludes a type (BUGHUNT2 2.6).
                if (candidate.isPickable() && (candidate != thrower || this.ticksInAir >= 5) && this.canHitCandidate(candidate)) {
                    Vec3 intercept = LegacyProjectiles.calculateIntercept(candidate.getBoundingBox().inflate((double) 0.3F), from, to);
                    if (intercept != null) {
                        double distance = from.distanceTo(intercept);
                        if (distance < nearest || nearest == 0.0D) {
                            found = candidate;
                            nearest = distance;
                        }
                    }
                }
            }
            if (found != null) {
                hit = new EntityHitResult(found);
            }
        }
        if (hit != null) {
            if (hit instanceof BlockHitResult blockHit && this.level().getBlockState(blockHit.getBlockPos()).is(Blocks.NETHER_PORTAL)) {
                this.setAsInsidePortal((NetherPortalBlock) Blocks.NETHER_PORTAL, blockHit.getBlockPos());
            } else if (!EventHooks.onProjectileImpact(this, hit)) {
                // PORT: NeoForge's ProjectileImpactEvent is posted so other mods can cancel a hit;
                // nothing listens in this mod. Projectile.onHit (vanilla block reactions, game events)
                // is not called - 1.7.10 had neither.
                this.onImpact(hit);
            }
        }
        Vec3 motion = this.getDeltaMovement();
        double x = this.getX() + motion.x;
        double y = this.getY() + motion.y;
        double z = this.getZ() + motion.z;
        float horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yRot = (float) (Math.atan2(motion.x, motion.z) * 180.0D / LEGACY_PI);
        float xRot = (float) (Math.atan2(motion.y, (double) horizontal) * 180.0D / LEGACY_PI);
        while (xRot - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }
        while (xRot - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }
        while (yRot - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }
        while (yRot - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }
        this.setXRot(this.xRotO + (xRot - this.xRotO) * 0.2F);
        this.setYRot(this.yRotO + (yRot - this.yRotO) * 0.2F);
        float drag = 0.99F;
        float gravity = this.getGravityVelocity();
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float offset = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, x - motion.x * (double) offset, y - motion.y * (double) offset,
                        z - motion.z * (double) offset, motion.x, motion.y, motion.z);
            }
            drag = 0.8F;
        }
        this.setDeltaMovement(motion.x * (double) drag, motion.y * (double) drag - (double) gravity, motion.z * (double) drag);
        this.setPos(x, y, z);
    }
}
