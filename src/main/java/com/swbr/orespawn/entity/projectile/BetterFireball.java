package com.swbr.orespawn.entity.projectile;

import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import com.swbr.orespawn.entity.moth.Mothra;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.joml.Vector3f;

/**
 * Port of {@code danger.orespawn.BetterFireball}, the fireball of the dragons, Mothra, Brutalfly,
 * Mobzilla and the royal family. The original was never registered; the port uses the id
 * {@code better_fireball} (catalogue 6.6, wave plan W04). Behaviour in verhalten/entity-05.md.
 *
 * <p><b>The double tick is reproduced on purpose</b> (catalogue README 4.5). The original extends
 * {@code EntityFireball} but redeclares {@code shootingEntity}, {@code ticksAlive},
 * {@code ticksInAir} and {@code accelerationX/Y/Z} (BetterFireball.java:15-25), then calls
 * {@code super.onUpdate()} (:111). The vanilla tick ({@code ze.h()} in client-1.7.10.jar) runs in
 * full on the shadowed fields, which stay 0 or null, before BetterFireball's own copy runs with its
 * filters and its acceleration. Each tick therefore moves the ball twice, damps it twice by 0.95 and
 * looks for hits twice - the second, unfiltered search can hit the shooter (Brutalfly's muzzle sits
 * inside its own hitbox). {@link #fireballTick()} is {@code ze.h()} on the {@code super*} fields,
 * {@link #tick()} is BetterFireball.java:90-214. Both hit searches may fire {@link #onImpact} in the
 * same tick, and a ball already discarded by the first finishes the second, as in 1.7.10.
 *
 * <p>Why {@link Projectile} and not {@code AbstractHurtingProjectile}: the vanilla pass of 1.21.1
 * accelerates along the current motion, while {@code ze.h()} adds a fixed acceleration vector (set
 * only by a punch, {@link #hurt}); and 1.21.1's {@code AABB.clip} misses rays that start inside the
 * target. Deflection and pick radius are ported from {@code EntityFireball} instead.
 *
 * <p>PORT, synchronisation (R18 case 4): 1.7.10 sent every {@code EntityFireball} subclass as a
 * vanilla large fireball with zero acceleration, so clients saw it stand still and jump every ten
 * ticks. The port has its own type and runs this same tick on the client; the two acceleration
 * vectors are synchronised so the client flight matches the server. {@code small} is not - the
 * 1.7.10 client drew every BetterFireball at large-fireball size, and so does the port.
 */
public class BetterFireball extends Projectile implements ItemSupplier {

    private static final EntityDataAccessor<Vector3f> DATA_ACCELERATION =
            SynchedEntityData.defineId(BetterFireball.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Vector3f> DATA_SUPER_ACCELERATION =
            SynchedEntityData.defineId(BetterFireball.class, EntityDataSerializers.VECTOR3);
    /** {@code setSmall()}: {@code setSize(0.3125f, 0.3125f)} (BetterFireball.java:87). */
    private static final EntityDimensions SMALL = EntityDimensions.scalable(0.3125F, 0.3125F);

    // --- EntityFireball's fields as the vanilla pass sees them (ze: a, at, au, b, c, d). Only a punch
    // --- (hurt) ever changes them. ze's xTile/inGround branch is unreachable: nothing sets inGround.
    @Nullable
    private LivingEntity superShootingEntity;
    private int superTicksInAir;
    private double superAccelerationX;
    private double superAccelerationY;
    private double superAccelerationZ;

    // --- BetterFireball's own fields (BetterFireball.java:15-28).
    @Nullable
    public LivingEntity shootingEntity;
    private int ticksAlive;
    private int ticksInAir = 0;
    private double accelerationX;
    private double accelerationY;
    private double accelerationZ;
    /** {@code field_92012_e}: explosion strength, 1 / 2 big / 4 really big. */
    private int explosionPower = 1;
    private int notme = 0;
    private boolean small = false;
    /** See {@link #readAdditionalSaveData}. */
    private boolean discardOnFirstTick;
    private final ItemStack renderStack = new ItemStack(Items.FIRE_CHARGE);

    /** Registry factory; {@code BetterFireball(World)} with its 1x1 size from the entity type. */
    public BetterFireball(EntityType<? extends BetterFireball> type, Level level) {
        super(type, level);
    }

    /**
     * {@code BetterFireball(World, EntityLivingBase, double, double, double)} (:47-71): at the shooter's
     * feet, no motion, acceleration = direction normalised x 0.1. A zero direction gives NaN, as in
     * the original; Dragon.java:1041-1053 then sets the acceleration itself.
     */
    public BetterFireball(Level level, LivingEntity shooter, double dx, double dy, double dz) {
        this(ModEntities.BETTER_FIREBALL.get(), level);
        this.shootingEntity = shooter;
        // PORT: also the 1.21.1 owner, so ProjectileImpactEvent listeners see a shooter; the logic reads
        // shootingEntity, which is not saved - after a reload the ball is unattributed, as in 1.7.10.
        this.setOwner(shooter);
        this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
        this.setPos(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(0.0, 0.0, 0.0);
        final double var9 = (double) (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        this.setAcceleration(dx / var9 * 0.1, dy / var9 * 0.1, dz / var9 * 0.1);
    }

    public void setNotMe() {
        this.notme = 1;
    }

    public void setBig() {
        this.explosionPower = 2;
    }

    public void setReallyBig() {
        this.explosionPower = 4;
    }

    public void setSmall() {
        this.small = true;
        // PORT: setSize at runtime -> getDimensions + refreshDimensions (R9); 1.21.1 keeps the box centred.
        this.refreshDimensions();
    }

    /**
     * PORT: the original's public {@code accelerationX/Y/Z}. Callers that assigned the fields
     * (Dragon.java:1051-1053, :1077-1079) call this instead, so the value reaches the client before
     * the ball is added to the level.
     */
    public void setAcceleration(double x, double y, double z) {
        this.accelerationX = x;
        this.accelerationY = y;
        this.accelerationZ = z;
        this.pushAcceleration();
    }

    public double getAccelerationX() {
        return this.accelerationX;
    }

    public double getAccelerationY() {
        return this.accelerationY;
    }

    public double getAccelerationZ() {
        return this.accelerationZ;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // BetterFireball.entityInit() is empty (:44-45); the two entries are the port's sync (see class doc).
        builder.define(DATA_ACCELERATION, new Vector3f());
        builder.define(DATA_SUPER_ACCELERATION, new Vector3f());
    }

    private void pushAcceleration() {
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_ACCELERATION,
                    new Vector3f((float) this.accelerationX, (float) this.accelerationY, (float) this.accelerationZ));
            this.entityData.set(DATA_SUPER_ACCELERATION,
                    new Vector3f((float) this.superAccelerationX, (float) this.superAccelerationY, (float) this.superAccelerationZ));
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.level().isClientSide) {
            if (DATA_ACCELERATION.equals(key)) {
                Vector3f a = this.entityData.get(DATA_ACCELERATION);
                this.accelerationX = a.x();
                this.accelerationY = a.y();
                this.accelerationZ = a.z();
            } else if (DATA_SUPER_ACCELERATION.equals(key)) {
                Vector3f a = this.entityData.get(DATA_SUPER_ACCELERATION);
                this.superAccelerationX = a.x();
                this.superAccelerationY = a.y();
                this.superAccelerationZ = a.z();
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.small ? SMALL : super.getDimensions(pose);
    }

    /** {@code EntityFireball.getMotionFactor()} = 0.95 (ze.e()). */
    protected float getMotionFactor() {
        return 0.95F;
    }

    /** BetterFireball.java:90-214. */
    @Override
    public void tick() {
        if (this.discardOnFirstTick) {
            this.discard();
            return;
        }
        if (this.ticksAlive >= 600 || this.ticksInAir >= 600) {
            this.discard();
            return;
        }
        if (!this.level().isClientSide && ((this.shootingEntity != null && this.shootingEntity.isRemoved())
                || !LegacyProjectiles.blockExists(this.level(), this.getX(), this.getY(), this.getZ()))) {
            this.discard();
            return;
        }
        this.fireballTick(); // super.onUpdate()
        this.igniteForSeconds(1.0F);
        // inGround is never set (:113-122): the else branch is the only live one.
        ++this.ticksInAir;
        Vec3 var15 = this.position();
        Vec3 var16 = var15.add(this.getDeltaMovement());
        HitResult var17 = LegacyProjectiles.rayTraceBlocks(this, var15, var16);
        var15 = this.position();
        var16 = var15.add(this.getDeltaMovement());
        if (var17 != null) {
            var16 = var17.getLocation();
        }
        Entity var18 = null;
        List<Entity> var19 = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0, 1.0, 1.0));
        double var20 = 0.0;
        final float var22 = 0.3F;
        for (Entity var21 : var19) {
            if (this.shootingEntity == var21) {
                var17 = null;
                break;
            }
            if (var21 instanceof BetterFireball) {
                var17 = null;
                break;
            }
            if (var21 instanceof com.swbr.orespawn.entity.boss.mobzilla.GodzillaHead) { // :147-150
                var17 = null;
                break;
            }
            if (MyUtils.isRoyalty(var21)) {
                var17 = null;
                break;
            }
            if (this.notme != 0 && (var21 instanceof Player || var21 instanceof Dragon || var21 instanceof Mothra)) { // :155-158
                var17 = null;
                break;
            }
            if (var21.isPickable() && (var21 != this.shootingEntity || this.ticksInAir >= 25)) {
                Vec3 var30 = LegacyProjectiles.calculateIntercept(var21.getBoundingBox().inflate((double) var22), var15, var16);
                if (var30 != null) {
                    double var23 = var15.distanceTo(var30);
                    if (var23 < var20 || var20 == 0.0) {
                        var18 = var21;
                        var20 = var23;
                    }
                }
            }
        }
        if (var18 != null) {
            var17 = new EntityHitResult(var18);
        }
        if (var17 != null && !EventHooks.onProjectileImpact(this, var17)) {
            this.onImpact(var17);
        }
        this.advance(Math.PI);
        float var25 = this.getMotionFactor();
        Vec3 motion = this.getDeltaMovement();
        if (this.isInWater()) {
            for (int var31 = 0; var31 < 4; ++var31) {
                final float var26 = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - motion.x * var26, this.getY() - motion.y * var26,
                        this.getZ() - motion.z * var26, motion.x, motion.y, motion.z);
            }
            var25 = 0.8F;
        }
        this.setDeltaMovement((motion.x + this.accelerationX) * var25, (motion.y + this.accelerationY) * var25,
                (motion.z + this.accelerationZ) * var25);
        this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
    }

    /**
     * {@code EntityFireball.onUpdate()} ({@code ze.h()}), run on the fields BetterFireball shadows: no
     * acceleration and no shooter unless a punch set them, no filters, the shooter test only for the
     * first 25 ticks.
     */
    private void fireballTick() {
        if (!this.level().isClientSide && ((this.superShootingEntity != null && this.superShootingEntity.isRemoved())
                || !LegacyProjectiles.blockExists(this.level(), this.getX(), this.getY(), this.getZ()))) {
            this.discard();
            return;
        }
        super.tick(); // Entity.onUpdate()
        this.igniteForSeconds(1.0F);
        ++this.superTicksInAir;
        Vec3 from = this.position();
        Vec3 to = from.add(this.getDeltaMovement());
        HitResult hit = LegacyProjectiles.rayTraceBlocks(this, from, to);
        from = this.position();
        to = from.add(this.getDeltaMovement());
        if (hit != null) {
            to = hit.getLocation();
        }
        Entity found = null;
        List<Entity> candidates = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D, 1.0D, 1.0D));
        double nearest = 0.0D;
        for (Entity candidate : candidates) {
            // PORT (BUGHUNT2 4.5, R25): the heads of the King, the Queen and Mobzilla are no candidates here. ze.h() has
            // no filter, but 1.7.10 left the head boxes where the heads spawned; the port moves them to the mouth, where
            // firecanon starts every ball inside the head box. Without the skip each royal and Mobzilla fireball would
            // explode on the muzzle in its first tick. BetterFireball's own pass (tick) already skips them.
            if (candidate.isPickable() && (candidate != this.superShootingEntity || this.superTicksInAir >= 25)
                    && !LegacyProjectiles.isRelocatedHead(candidate)) {
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
        if (hit != null && !EventHooks.onProjectileImpact(this, hit)) {
            this.onImpact(hit);
        }
        this.advance(LegacyThrowable.LEGACY_PI);
        float factor = this.getMotionFactor();
        Vec3 motion = this.getDeltaMovement();
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                final float offset = 0.25F;
                this.level().addParticle(ParticleTypes.BUBBLE, this.getX() - motion.x * offset, this.getY() - motion.y * offset,
                        this.getZ() - motion.z * offset, motion.x, motion.y, motion.z);
            }
            factor = 0.8F;
        }
        this.setDeltaMovement((motion.x + this.superAccelerationX) * factor, (motion.y + this.superAccelerationY) * factor,
                (motion.z + this.superAccelerationZ) * factor);
        this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
    }

    /**
     * The shared tail of both passes before the motion update: {@code pos += motion}, then the
     * fireball rotation ({@code atan2(z, x) + 90}, {@code atan2(horizontal, y) - 90}) eased by 0.2.
     * {@code pi} is the constant each class compiled: {@code (float)Math.PI} in ze, {@code Math.PI}
     * in BetterFireball. The original set the position fields here and the bounding box only with
     * the final {@code setPosition}; no box test sits in between, so moving here is the same.
     */
    private void advance(double pi) {
        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        float horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yRot = (float) (Math.atan2(motion.z, motion.x) * 180.0 / pi) + 90.0F;
        float xRot = (float) (Math.atan2((double) horizontal, motion.y) * 180.0 / pi) - 90.0F;
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
    }

    /** BetterFireball.java:216-284; server only, like the original. */
    protected void onImpact(HitResult result) {
        if (this.level().isClientSide) {
            return;
        }
        Entity entityHit = LegacyProjectiles.entityHit(result);
        if (entityHit != null) {
            if (entityHit instanceof BetterFireball) {
                return;
            }
            if (entityHit instanceof Mothra) { // :222-224
                return;
            }
            if (this.notme != 0 && (entityHit instanceof Dragon || entityHit instanceof Player)) { // :225-228
                this.discard();
                return;
            }
            final Entity e = entityHit;
            if (e instanceof Mob el) {
                // :232. setHealth(getHealth()/2) halves virtual health too, because the attribute scale is proportional (R4).
                if (el.getBbWidth() * el.getBbHeight() > 30.0F && !MyUtils.isRoyalty(el) && !(el instanceof PitchBlack)
                        && !(el instanceof com.swbr.orespawn.entity.boss.mobzilla.Godzilla)
                        && !(el instanceof com.swbr.orespawn.entity.boss.mobzilla.GodzillaHead)
                        && !(el instanceof com.swbr.orespawn.entity.boss.kraken.Kraken)) {
                    el.setHealth(el.getHealth() / 2.0F);
                }
            }
            if (!this.small) {
                entityHit.hurt(this.fireballDamage(), 10.0F);
                entityHit.igniteForSeconds(5.0F);
            } else {
                entityHit.hurt(this.fireballDamage(), 5.0F);
                entityHit.igniteForSeconds(5.0F);
            }
        } else if (result instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos().relative(blockHit.getDirection());
            if (this.level().isEmptyBlock(pos)) {
                this.level().setBlockAndUpdate(pos, LegacyProjectiles.fireState(this.level(), pos));
            }
        }
        if (!this.small) {
            // newExplosion(null, x, y, z, power, flaming = true, smoking = mobGriefing): with a null source
            // MOB interaction reads the mobGriefing rule, and fire is placed either way - in both versions.
            this.level().explode(null, this.getX(), this.getY(), this.getZ(), (float) this.explosionPower, true, Level.ExplosionInteraction.MOB);
        }
        this.discard();
    }

    /** {@code DamageSource.causeFireballDamage(this, shootingEntity)}: "onFire" without a shooter. */
    private DamageSource fireballDamage() {
        return this.shootingEntity == null
                ? this.damageSources().source(DamageTypes.UNATTRIBUTED_FIREBALL, this)
                : this.damageSources().source(DamageTypes.FIREBALL, this, this.shootingEntity);
    }

    /**
     * {@code EntityFireball.attackEntityFrom} (ze.a(ro, float)): a hit by anything with a source entity
     * turns the ball along that entity's look, sets the vanilla-pass acceleration to look x 0.1 and
     * makes a living attacker the vanilla-pass shooter. BetterFireball's own acceleration and shooter
     * stay untouched, so a punched ball keeps both pushes.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        this.markHurt();
        Entity attacker = source.getEntity();
        if (attacker != null) {
            Vec3 look = attacker.getLookAngle();
            this.setDeltaMovement(look);
            this.superAccelerationX = look.x * 0.1;
            this.superAccelerationY = look.y * 0.1;
            this.superAccelerationZ = look.z * 0.1;
            if (attacker instanceof LivingEntity living) {
                this.superShootingEntity = living;
            }
            this.pushAcceleration();
            return true;
        }
        return false;
    }

    /** {@code EntityFireball.canBeCollidedWith()} = true (ze.R()). */
    @Override
    public boolean isPickable() {
        return true;
    }

    /** {@code EntityFireball.getCollisionBorderSize()} = 1.0 (ze.af()). */
    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    /** {@code EntityFireball.getBrightness()} = 1.0. */
    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    /** ze.a(double): average edge length x 4 x 64. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = this.getBoundingBox().getSize() * 4.0D;
        size *= 64.0D;
        return distance < size * size;
    }

    /** The client draws it as the vanilla large fireball did: a full-bright fire charge. */
    @Override
    public ItemStack getItem() {
        return this.renderStack;
    }

    /**
     * {@code EntityFireball.writeEntityToNBT} keeps the motion as "direction"; BetterFireball adds
     * "ExplosionPower" (:286-289). Acceleration, {@code small} and {@code notme} were never saved: a
     * reloaded ball drifts to a stop. PORT: the always-unused xTile/yTile/zTile/inTile/inGround keys
     * are not written.
     */
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        Vec3 motion = this.getDeltaMovement();
        tag.put("direction", this.newDoubleList(motion.x, motion.y, motion.z));
        tag.putInt("ExplosionPower", this.explosionPower);
    }

    /**
     * Without "direction" the original called {@code setDead()} while loading. PORT: the port discards
     * on the first tick instead, because removing an entity before the level has attached it leaves
     * it in the entity sections.
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("direction", Tag.TAG_LIST)) {
            ListTag direction = tag.getList("direction", Tag.TAG_DOUBLE);
            this.setDeltaMovement(direction.getDouble(0), direction.getDouble(1), direction.getDouble(2));
        } else {
            this.discardOnFirstTick = true;
        }
        if (tag.contains("ExplosionPower")) {
            this.explosionPower = tag.getInt("ExplosionPower");
        }
    }
}
