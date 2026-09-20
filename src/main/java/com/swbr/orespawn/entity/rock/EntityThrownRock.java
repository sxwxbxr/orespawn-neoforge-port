package com.swbr.orespawn.entity.rock;

import com.swbr.orespawn.entity.projectile.LegacyThrowable;
import com.swbr.orespawn.item.rock.ItemRock;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.EntityThrownRock} (EntityThrownRock.java:12-325), registry id
 * {@code entity_thrown_rock} (OreSpawnMain.java:3118-3120: tracking 64 / 1 / velocity updates).
 * A thrown rock of one of twelve types; the type decides damage, knockback and side effect on an
 * entity hit, and which item drops on a block hit (verhalten/entity-08.md):
 *
 * <table>
 * <tr><th>type</th><th>damage</th><th>ks</th><th>inair</th><th>extra</th><th>lines</th></tr>
 * <tr><td>1</td><td>2</td><td>0.1</td><td>0.025</td><td>-</td><td>:83-92</td></tr>
 * <tr><td>2</td><td>5</td><td>0.2</td><td>0.025</td><td>-</td><td>:93-102</td></tr>
 * <tr><td>3</td><td>5</td><td>0.2</td><td>0.025</td><td>fire 20 s</td><td>:103-113</td></tr>
 * <tr><td>4</td><td>5</td><td>0.2</td><td>0.025</td><td>Poison 100</td><td>:114-126</td></tr>
 * <tr><td>5</td><td>10</td><td>0.1</td><td>0.025</td><td>Slowness 100</td><td>:127-139</td></tr>
 * <tr><td>6</td><td>20</td><td>0.2</td><td>0.025</td><td>Weakness 100</td><td>:140-152</td></tr>
 * <tr><td>7</td><td>40</td><td>0.2</td><td>0.025</td><td>-</td><td>:153-162</td></tr>
 * <tr><td>8</td><td>40</td><td>0.5</td><td>0.055</td><td>explosion 2.1, fire</td><td>:163-173</td></tr>
 * <tr><td>9</td><td>150</td><td>0.2</td><td>0.025</td><td>fire 50 s, Weakness 100</td><td>:174-187</td></tr>
 * <tr><td>10</td><td>150</td><td>0.2</td><td>0.025</td><td>Poison 200, Weakness 100</td><td>:188-203</td></tr>
 * <tr><td>11</td><td>150</td><td>0.2</td><td>0.025</td><td>Slowness 200, Weakness 100</td><td>:204-219</td></tr>
 * <tr><td>12</td><td>250</td><td>0.2</td><td>0.025</td><td>Weakness 100, explosion 5.1, fire</td><td>:220-233</td></tr>
 * </table>
 *
 * <p>The type is DataWatcher slot 20 (:56) and is not saved to NBT: a rock that is reloaded with
 * the chunk is type 0 again - it then neither hurts nor drops (:81, :235). Kept (DECISIONS R18).
 * The 1.7.10 hitbox was the {@code EntityThrowable} default 0.25 x 0.25 (manifest {@code size} null).
 *
 * <p>Flight, launch and hit detection are the 1.7.10 {@code EntityThrowable} of {@link LegacyThrowable}
 * ({@code zk.h}): blocks traced with the selection box, so tall grass, flowers and torches stop a rock and
 * run the block branch; entity hits through {@code calculateIntercept}, so a rock thrown point-blank
 * hits a mob it already overlaps; no {@code checkInsideBlocks}. The thrower constructor and the
 * dispenser's {@code shoot} (spread 6.0) use the Gaussian {@code setThrowableHeading}.
 */
public class EntityThrownRock extends LegacyThrowable {

    /** DataWatcher slot 20 (:56): the rock type. */
    private static final EntityDataAccessor<Integer> ROCK_TYPE =
            SynchedEntityData.defineId(EntityThrownRock.class, EntityDataSerializers.INT);

    private int rock_type;
    private int myage;
    private float my_rotation;

    /** {@code EntityThrownRock(World)} (:18-23) - the registry factory. */
    public EntityThrownRock(final EntityType<? extends EntityThrownRock> type, final Level par1World) {
        super(type, par1World);
        this.rock_type = 0;
        this.myage = 0;
        this.my_rotation = 0.0f;
    }

    /** {@code EntityThrownRock(World, int)} (:25-30): the type argument is ignored, as in the original. No caller. */
    public EntityThrownRock(final Level par1World, final int par2) {
        this(ModEntities.ENTITY_THROWN_ROCK.get(), par1World);
    }

    /** {@code EntityThrownRock(World, EntityLivingBase)} (:32-37): thrown, type 0. */
    public EntityThrownRock(final Level par1World, final LivingEntity par2EntityLiving) {
        this(par1World, par2EntityLiving, 0);
    }

    /**
     * {@code EntityThrownRock(World, EntityLivingBase, int)} (:39-45): thrown by
     * {@code par2EntityLiving} with the given type. The {@code EntityThrowable(World, EntityLivingBase)}
     * super constructor places the rock 0.16 beside the thrower's eyes, 0.1 down, and launches it along
     * the view at velocity 1.5 with spread 1.0.
     */
    public EntityThrownRock(final Level par1World, final LivingEntity par2EntityLiving, final int par3) {
        super(ModEntities.ENTITY_THROWN_ROCK.get(), par1World, par2EntityLiving);
        this.rock_type = 0;
        this.myage = 0;
        this.my_rotation = 0.0f;
        this.rock_type = par3;
    }

    /** {@code EntityThrownRock(World, double, double, double)} (:47-52): the dispenser's rock, type 0. */
    public EntityThrownRock(final Level par1World, final double par2, final double par4, final double par6) {
        super(ModEntities.ENTITY_THROWN_ROCK.get(), par1World, par2, par4, par6);
        this.rock_type = 0;
        this.myage = 0;
        this.my_rotation = 0.0f;
    }

    /** {@code entityInit} (:54-57): slot 20, default 0. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        builder.define(ROCK_TYPE, 0);
    }

    /** {@code getRockType} (:59-61). */
    public int getRockType() {
        return this.entityData.get(ROCK_TYPE);
    }

    /** {@code setRockType} (:63-72): server only; writes the field and the watched slot. */
    public void setRockType(final int par1) {
        if (this.level() == null) {
            return;
        }
        if (this.level().isClientSide) {
            return;
        }
        this.rock_type = par1;
        this.entityData.set(ROCK_TYPE, par1);
    }

    /**
     * {@code onImpact} (:74-296). Server only (:75-80). An entity hit counts only with a thrower
     * and only against something other than the thrower (:81-83); a block hit (or an entity hit
     * without thrower, see below) breaks glass in the 3x3x3 cube around the hit and drops the
     * rock's item (:235-294). Always dead afterwards (:295). A thrower hit by his own rock (possible
     * once the rock left him) matches the first branch but none of the type arms: no damage, no
     * drop - in both versions.
     *
     * <p>PORT: the original cast the thrower to {@code EntityPlayer} for the damage source
     * (:84 and eleven siblings); only players throw rocks, so the cast never failed. A non-player
     * owner (nothing in this mod creates one) gets the generic thrown source instead of a crash
     * (R18 case 1).
     *
     * <p>Kept (R18): a dispenser rock has no thrower, so its entity hit falls into the block branch,
     * where {@code MovingObjectPosition.blockX/Y/Z} of an entity hit are 0 (:237-239): it clears
     * glass around the world origin and drops its item where it is. An {@code EntityHitResult} has
     * no block position; the port uses (0, 0, 0), as the original did.
     */
    @Override
    protected void onImpact(final HitResult par1MovingObjectPosition) {
        if (this.isRemoved()) { // :75-77
            return;
        }
        if (this.level().isClientSide) { // :78-80
            return;
        }
        final Entity entityHit = par1MovingObjectPosition instanceof EntityHitResult ehr ? ehr.getEntity() : null;
        final Entity thrower = this.getOwner();
        if (entityHit != null && thrower != null) { // :81
            final Entity e = entityHit;
            final DamageSource source = thrower instanceof Player player
                    ? this.damageSources().playerAttack(player) // DamageSource.causePlayerDamage((EntityPlayer) getThrower())
                    : this.damageSources().thrown(this, thrower); // PORT, see above
            if (this.rock_type == 1 && e != thrower) { // :83-92
                strike(e, thrower, source, 2.0f, 0.1, 0.025);
            }
            if (this.rock_type == 2 && e != thrower) { // :93-102
                strike(e, thrower, source, 5.0f, 0.2, 0.025);
            }
            if (this.rock_type == 3 && e != thrower) { // :103-113
                strike(e, thrower, source, 5.0f, 0.2, 0.025);
                e.igniteForSeconds(20); // :112 setFire(20)
            }
            if (this.rock_type == 4 && e != thrower) { // :114-126
                strike(e, thrower, source, 5.0f, 0.2, 0.025);
                if (e instanceof LivingEntity living) { // :123-125
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                }
            }
            if (this.rock_type == 5 && e != thrower) { // :127-139
                strike(e, thrower, source, 10.0f, 0.1, 0.025);
                if (e instanceof LivingEntity living) { // :136-138
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                }
            }
            if (this.rock_type == 6 && e != thrower) { // :140-152
                strike(e, thrower, source, 20.0f, 0.2, 0.025);
                if (e instanceof LivingEntity living) { // :149-151
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }
            }
            if (this.rock_type == 7 && e != thrower) { // :153-162
                strike(e, thrower, source, 40.0f, 0.2, 0.025);
            }
            if (this.rock_type == 8 && e != thrower) { // :163-173
                strike(e, thrower, source, 40.0f, 0.5, 0.055);
                // :172 newExplosion(null, x, y + 0.25, z, 2.1f, flaming = true, smoking = mobGriefing).
                // ExplosionInteraction.MOB with a null source reads the same gamerule for block damage
                // (EventHooks.canEntityGrief); the fire pass runs regardless of it, as in 1.7.10.
                this.level().explode(null, e.getX(), e.getY() + 0.25, e.getZ(), 2.1f, true, Level.ExplosionInteraction.MOB);
            }
            if (this.rock_type == 9 && e != thrower) { // :174-187
                strike(e, thrower, source, 150.0f, 0.2, 0.025);
                e.igniteForSeconds(50); // :183 setFire(50)
                if (e instanceof LivingEntity living) { // :184-186
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }
            }
            if (this.rock_type == 10 && e != thrower) { // :188-203
                strike(e, thrower, source, 150.0f, 0.2, 0.025);
                if (e instanceof LivingEntity living) { // :197-199
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
                }
                if (e instanceof LivingEntity living) { // :200-202
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }
            }
            if (this.rock_type == 11 && e != thrower) { // :204-219
                strike(e, thrower, source, 150.0f, 0.2, 0.025);
                if (e instanceof LivingEntity living) { // :213-215
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
                }
                if (e instanceof LivingEntity living) { // :216-218
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }
            }
            if (this.rock_type == 12 && e != thrower) { // :220-233
                strike(e, thrower, source, 250.0f, 0.2, 0.025);
                if (e instanceof LivingEntity living) { // :229-231
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                }
                // :232 newExplosion(null, x, y + 0.25, z, 5.1f, true, mobGriefing)
                this.level().explode(null, e.getX(), e.getY() + 0.25, e.getZ(), 5.1f, true, Level.ExplosionInteraction.MOB);
            }
        } else if (this.rock_type != 0) { // :235
            int played = 0;
            final int x;
            final int y;
            final int z;
            if (par1MovingObjectPosition instanceof BlockHitResult bhr) { // :237-239
                x = bhr.getBlockPos().getX();
                y = bhr.getBlockPos().getY();
                z = bhr.getBlockPos().getZ();
            } else {
                // 1.7.10 MovingObjectPosition(Entity) left blockX/Y/Z at 0 - see the class comment.
                x = 0;
                y = 0;
                z = 0;
            }
            for (int i = -1; i <= 1; ++i) { // :240-255
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        final BlockPos at = new BlockPos(x + i, y + j, z + k);
                        final BlockState bid = this.level().getBlockState(at);
                        // :244 glass || glass_pane || glass - stained glass was its own block in 1.7.10 too
                        if (bid.is(Blocks.GLASS) || bid.is(Blocks.GLASS_PANE) || bid.is(Blocks.GLASS)) {
                            if (!this.level().isClientSide) { // :245-247, setBlock(x, y, z, air) = flag 3
                                this.level().setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            }
                            if (played == 0) { // :248-251
                                // playSoundEffect(x, y, z, "orespawn:glassdead", 1, 1). The sounds.json
                                // entry has no category, which 1.7.10 files under MASTER.
                                this.level().playSound(null, x, y, z, ModSounds.GLASSDEAD.get(), SoundSource.MASTER, 1.0f, 1.0f);
                                ++played;
                            }
                        }
                    }
                }
            }
            if (!this.level().isClientSide) { // :256-293 dropItem(My*Rock, 1) per type
                final ItemRock drop = ItemRock.byType(this.rock_type);
                if (drop != null) {
                    this.spawnAtLocation(new ItemStack(drop, 1), 0.0f); // Entity.dropItem -> entityDropItem(stack, 0.0f)
                }
            }
        }
        this.discard(); // :295 setDead()
    }

    /**
     * The block every type repeats (:84-91 and siblings): the hit, then a push of {@code ks}
     * along the thrower-to-target angle and {@code inair} upwards - doubled when the target is
     * already gone (a living target never is right after the hit: its death takes twenty ticks
     * in 1.7.10 and 1.21.1 alike, so the doubling only reached non-living targets).
     *
     * <p>No explicit velocity packet: a successful {@code hurt} marks the target
     * ({@code LivingEntity.markHurt}, the 1.7.10 {@code setBeenAttacked}), and the tracker sends the
     * pushed motion at the end of the tick in both versions.
     */
    private static void strike(final Entity e, final Entity thrower, final DamageSource source,
                               final float damage, final double ks, double inair) {
        e.hurt(source, damage); // attackEntityFrom(causePlayerDamage(thrower), damage)
        final float f3 = (float) Math.atan2(e.getZ() - thrower.getZ(), e.getX() - thrower.getX());
        if (e.isRemoved()) { // isDead
            inair *= 2.0;
        }
        e.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks); // addVelocity
    }

    /**
     * {@code onUpdate} (:298-324): 30 degrees of pitch per tick, both sides (:303-307); dead after
     * 1000 ticks (:308-311); type sync (:312-317); a skip off water when falling between
     * -0.15 and -0.55 with squared horizontal speed above 0.5 - vertical speed mirrored at three
     * quarters, horizontal speed times three quarters (:318-323).
     *
     * <p>PORT: the {@code (int) posX/posY/posZ} casts (:299-301) are {@code Mth.floor} (DECISIONS R20):
     * truncation read the neighbouring block in the negative half of the world. PORT:
     * {@code Blocks.water} was the still-water block, which in
     * 1.7.10 also held every settled flowing block; 1.21.1 has one water block, so freshly flowing
     * water skips too.
     */
    @Override
    public void tick() {
        final int x = Mth.floor(this.getX()); // :299 (int), R20
        final int y = Mth.floor(this.getY()); // :300 (int), R20
        final int z = Mth.floor(this.getZ()); // :301 (int), R20
        super.tick(); // :302
        this.my_rotation += 30.0f; // :303
        this.my_rotation %= 360.0f; // :304
        final float my_rotation = this.my_rotation;
        this.xRotO = my_rotation; // :306 prevRotationPitch
        this.setXRot(my_rotation); // :307 rotationPitch
        ++this.myage; // :308
        if (this.myage > 1000) { // :309-311
            this.discard();
        }
        if (this.level().isClientSide) { // :312-317
            this.rock_type = this.getRockType();
        } else {
            this.setRockType(this.rock_type);
        }
        final BlockState bid = this.level().getBlockState(new BlockPos(x, y, z)); // :318
        final Vec3 motion = this.getDeltaMovement();
        if (bid.is(Blocks.WATER) && motion.y < -0.15000000596046448 && motion.y > -0.550000011920929
                && (float) (motion.x * motion.x + motion.z * motion.z) > 0.5f) { // :319
            this.setDeltaMovement(motion.x * 3.0 / 4.0, -(motion.y * 3.0 / 4.0), motion.z * 3.0 / 4.0); // :320-322
        }
    }
}
