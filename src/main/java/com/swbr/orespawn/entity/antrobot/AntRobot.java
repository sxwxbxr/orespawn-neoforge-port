package com.swbr.orespawn.entity.antrobot;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.spiderrobot.RenderSpiderRobotInfo;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.AntRobot} (AntRobot.java:16-1225, verhalten/entity-04.md), registry id
 * {@code robot_red_ant} ("Robot Red Ant", OreSpawnMain.java:4163-4169, tracking 128/1/false). A hovering six-legged
 * combat mech: wild it attacks everything living nearby; once owned (kit or wrench, {@link #setOwned()}) it is a
 * vehicle that bites and stomps on its own while ridden. {@code extends EntityLiving}: no path finder, all movement
 * is motion written directly.
 *
 * <p>Values from {@code AntRobot_stats} (300/30/16, OreSpawnMain.java:6147): health and attack as attributes, the
 * defense through {@link LegacyArmor} (R5), {@code experienceValue = health / 2} (:47; {@code EntityLiving} reads
 * the field, unlike {@code EntityAnimal}, W04 lesson). Health stays below the 1024 clamp even at the config maximum
 * of 600, so no virtual health (R4).
 *
 * <h2>Where the physics run</h2>
 * As in the W04 {@code Elevator} and the W06 {@code Ostrich}: the original moved a ridden robot on the server only
 * and let the client interpolate ({@code setPositionAndRotation2}, :646-658). 1.21.1's
 * {@code tickRidden}/{@code getRiddenInput} run on the rider's client, so the port keeps the original split:
 * {@link #aiStep()} is the whole {@code onLivingUpdate} (:713-935), {@link #getControllingPassenger()} is
 * {@code null} so no client predicts the robot, and the rider input is {@code Player.zza}, which 1.21.1 sends to the
 * server every tick while riding ({@code LocalPlayer.tick} -> {@code ServerboundPlayerInputPacket}, W04 Elevator).
 * The Red Ant reads no fly-up key. With a rider {@code onLivingUpdate} does not call {@code super}; the port's
 * {@link #aiStep()} does the same, so neither the vanilla travel nor the goals run while ridden.
 *
 * <h2>Original quirks kept (R18)</h2>
 * <ul>
 *   <li>{@code this.posY += ...} on the server (:803, :816, :841) is a field write that {@code moveEntity} overwrote
 *       from the bounding box (W04 Elevator, bytecode {@code sa.d(DDD)V}). It only shifts the block lookups that
 *       follow in the same tick; here it is a local.</li>
 *   <li>{@code moveEntity} runs twice per tick while ridden (:922, :927), and a wild robot moves once in the vanilla
 *       update and once more at :927.</li>
 *   <li>{@code prevPosX/Y/Z} are overwritten after the vanilla update (:747-749).</li>
 *   <li>{@code setFire(0)} (:669) never extinguishes anything; the fire immunity is the entity type's.</li>
 *   <li>{@code owned} is saved but never synchronised; the client always sees 0 (only {@code interact} reads it
 *       there, and the server decides).</li>
 *   <li>The sensing cache is only cleared by the goal update (1.7.10 {@code updateAITasks}, 1.21.1
 *       {@code serverAiStep}); while ridden the line-of-sight answers of the last unridden tick stay cached.</li>
 * </ul>
 *
 * <p>The leg IK (:149-558) runs on the client in {@link #aiStep()} and fills {@link RenderSpiderRobotInfo}, read by
 * {@code AntRobotModel}. It reads blocks from the client level; no client class is touched.
 */
public class AntRobot extends Mob implements LegacyArmor {

    /** DataWatcher 20 (:585, :1151-1157): attacking 0/1, read by the model for jaws and antennae. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(AntRobot.class, EntityDataSerializers.INT);

    // Client-side interpolation state (:18-23), filled by lerpTo.
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private int playing;
    private float moveSpeed;
    /** Client-side leg state (plain data, no client imports). */
    private RenderSpiderRobotInfo renderdata;
    private int didonce;
    private int rideTicker;
    private int owned;

    /**
     * {@code AntRobot(World)} (:32-48). {@code setSize(2.75f, 1.25f)} is the entity type's size, {@code isImmuneToFire}
     * its {@code fireImmune()}. {@code GenericTargetSorter} (:43) is created but never used by this class and is not
     * carried over. {@code entityInit} (:580-586) calls {@code func_110163_bv} = {@code enablePersistence}.
     *
     * <p>{@code entityInit} also ran {@code initLegData}, but in 1.7.10 it ran inside the {@code Entity} constructor,
     * before the field initialisers replaced {@code renderdata} with a fresh object; the real initialisation is the
     * {@code didonce} call in {@link #updateLegs()}. The port starts from the fresh object as well.
     */
    public AntRobot(final EntityType<? extends AntRobot> type, final Level par1World) {
        super(type, par1World);
        this.playing = 0;
        this.moveSpeed = 0.3f;
        this.renderdata = new RenderSpiderRobotInfo();
        this.didonce = 0;
        this.rideTicker = 0;
        this.owned = 0;
        this.setPersistenceRequired();
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent).
        this.goalSelector.addGoal(1, new EntityAIWatchClosest(this, Player.class, 12.0f));
        this.goalSelector.addGoal(2, new EntityAILookIdle(this));
        this.xpReward = MobStats.AntRobot_stats().health() / 2;
        // PORT: applyEntityAttributes (:61-67) read AntRobot_stats during construction; the attribute supplier is
        // built before the config loads (R3), so the runtime values go in here, followed by full health (GiantRobot).
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) MobStats.AntRobot_stats().health());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.AntRobot_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code applyEntityAttributes} (:61-67) with the config defaults (manifest 300 / 0.3 / 30); see the constructor.
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} step height (W04 Elevator, javap on
     * {@code sv.<init>}); 1.21.1 defaults to 0.6. It matters for both {@code moveEntity} calls.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** {@code entityInit} (:580-586). */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code canDespawn} (:69-71). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code setOwned} (:73-75), called by the Red Ant Robot Kit and the wrench. */
    public void setOwned() {
        this.owned = 1;
    }

    /** {@code getOwned} (:77-79). */
    public int getOwned() {
        return this.owned;
    }

    /** {@code getTotalArmorValue} (:81-83). */
    public int getTotalArmorValue() {
        return MobStats.AntRobot_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    /** 1.21.1 runs this inside {@code Mob.serverAiStep}, after the goals and the navigation. */
    @Override
    protected void customServerAiStep() {
        this.updateAITasks();
    }

    /**
     * The own part of {@code updateAITasks} (:85-140). The early returns (dead, ridden) come before
     * {@code super.updateAITasks()} in the original; here they are implicit, because a removed entity does not tick
     * and a ridden one never reaches {@code serverAiStep} (see {@link #aiStep()}). They are kept for the reading.
     *
     * <p>PORT: runs before the move, look and jump controls of the same tick instead of after them (EntityCannonFodder
     * precedent).
     */
    protected void updateAITasks() {
        LivingEntity e = null;
        if (this.isRemoved()) {
            return;
        }
        if (this.getFirstPassenger() != null) {
            return;
        }
        if (this.owned == 0 && this.level().getDifficulty() != Difficulty.PEACEFUL) {
            if (this.level().random.nextInt(20) == 0) {
                this.feetFindSomethingToHit();
            }
            if (this.level().random.nextInt(150) == 0) {
                this.setTarget((LivingEntity) null);
            }
            e = this.getTarget();
            if (e != null && !e.isAlive()) {
                this.setTarget((LivingEntity) null);
                e = null;
            }
            if (e == null) {
                e = this.findSomethingToAttack(2.0f, false);
            }
            if (e != null) {
                this.lookAt(e, 10.0f, 10.0f);
                if (this.distanceToSqr(e) > 16.0) {
                    final double d1 = e.getZ() - this.getZ();
                    final double d2 = e.getX() - this.getX();
                    final double dd = Math.atan2(d1, d2);
                    this.goThisWay(0.2 * Math.cos(dd), 0.2 * Math.sin(dd));
                }
            } else {
                this.setAttacking(0);
            }
            if (e != null && this.level().random.nextInt(15) == 0) {
                e = this.getTarget();
                if (e == null) {
                    e = this.findSomethingToAttack(2.0f, true);
                }
                if (e != null) {
                    final float reach = 6.0f + e.getBbWidth() / 2.0f;
                    if (this.distanceToSqr(e) < reach * reach) {
                        this.setAttacking(1);
                        this.doHurtTarget(e);
                    } else {
                        this.setAttacking(0);
                    }
                } else {
                    this.setAttacking(0);
                }
            }
        }
    }

    // updateAITick (:142-147): returns while ridden, else EntityLiving's empty method. Unreachable while ridden
    // (updateAITasks returns first) and empty otherwise; nothing to port.

    /** {@code initLegData} (:149-223). */
    private void initLegData() {
        if (this.renderdata == null) {
            this.renderdata = new RenderSpiderRobotInfo();
        }
        for (int i = 0; i < 6; ++i) {
            this.renderdata.ycurrentangle[i] = 0.0f;
            this.renderdata.ywantedangle[i] = 0.0f;
            this.renderdata.ydisplayangle[i] = 0.0f;
            this.renderdata.yvelocity[i] = 0.0f;
            this.renderdata.ymid[i] = 0.0f;
            this.renderdata.yoff[i] = 0.0f;
            this.renderdata.yrange[i] = 0.0f;
            this.renderdata.udcurrentangle[i] = 0.0f;
            this.renderdata.udwantedangle[i] = 0.0f;
            this.renderdata.uddisplayangle[i] = 0.0f;
            this.renderdata.udvelocity[i] = 0.0f;
            this.renderdata.p1xangle[i] = 0.7853981633974483;
            this.renderdata.p2xangle[i] = 0.0;
            this.renderdata.p3xangle[i] = -0.7853981633974483;
            this.renderdata.pxvelocity[i] = 0.0f;
            this.renderdata.foot_xpos[i] = (float) this.getX();
            this.renderdata.foot_ypos[i] = (float) this.getY();
            this.renderdata.foot_zpos[i] = (float) this.getZ();
            this.renderdata.realposx[i] = 0.0f;
            this.renderdata.realposy[i] = 0.0f;
            this.renderdata.realposz[i] = 0.0f;
            this.renderdata.legoff[i] = 0.0f;
            this.renderdata.footup[i] = 1;
            this.renderdata.uppoint[i] = 0.0f;
            this.renderdata.footingticker[i] = 0;
            this.renderdata.gpcounter = 0;
            if (i == 0) {
                this.renderdata.legoff[i] = 0.75f;
                this.renderdata.ymid[i] = 0.0f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 1;
                this.renderdata.yoff[i] = -0.75f;
            }
            if (i == 1) {
                this.renderdata.legoff[i] = 0.75f;
                this.renderdata.ymid[i] = 3.1415927f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 0;
                this.renderdata.yoff[i] = -0.75f;
            }
            if (i == 2) {
                this.renderdata.legoff[i] = 1.0f;
                this.renderdata.ymid[i] = -0.7853982f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 3;
                this.renderdata.yoff[i] = -0.75f;
            }
            if (i == 3) {
                this.renderdata.legoff[i] = 1.0f;
                this.renderdata.ymid[i] = 3.9269907f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 2;
                this.renderdata.yoff[i] = -0.75f;
            }
            if (i == 4) {
                this.renderdata.legoff[i] = 1.15f;
                this.renderdata.ymid[i] = 0.7853982f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 5;
                this.renderdata.yoff[i] = -0.75f;
            }
            if (i == 5) {
                this.renderdata.legoff[i] = 1.15f;
                this.renderdata.ymid[i] = 2.3561945f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 4;
                this.renderdata.yoff[i] = -0.75f;
            }
        }
    }

    /** {@code getNewVelocity} (:225-267). */
    private float getNewVelocity(final float v, final float diff, float curval) {
        float tv = v;
        tv *= 18.0f;
        if (tv < 2.0f) {
            tv = 2.0f;
        }
        if (tv > 8.0f) {
            tv = 8.0f;
        }
        if (diff > 0.0f) {
            if (diff < 0.008726646259971648 * tv) {
                curval = 0.0f;
            } else {
                curval += (float) (0.004363323129985824 * tv);
                if (diff < 0.06981317007977318 * tv) {
                    curval = (float) (0.017453292519943295 * tv);
                }
                if (diff < 0.03490658503988659 * tv) {
                    curval = (float) (0.008726646259971648 * tv);
                }
                if (curval > 0.06981317007977318 * tv) {
                    curval = (float) (0.06981317007977318 * tv);
                }
            }
        } else if (diff > -0.008726646259971648 * tv) {
            curval = 0.0f;
        } else {
            curval -= (float) (0.004363323129985824 * tv);
            if (diff > -0.06981317007977318 * tv) {
                curval = -(float) (0.017453292519943295 * tv);
            }
            if (diff > -0.03490658503988659 * tv) {
                curval = -(float) (0.008726646259971648 * tv);
            }
            if (curval < -0.06981317007977318 * tv) {
                curval = -(float) (0.06981317007977318 * tv);
            }
        }
        return curval;
    }

    /** {@code updateLegs} (:269-449): client only. */
    public void updateLegs() {
        if (!this.level().isClientSide) {
            return;
        }
        float yaw = this.getYRot() % 360.0f;
        while (yaw < 0.0f) {
            yaw += 360.0f;
        }
        this.setYRot(yaw);
        final RenderSpiderRobotInfo renderdata = this.renderdata;
        ++renderdata.gpcounter;
        if (this.didonce == 0) {
            this.didonce = 1;
            this.initLegData();
        }
        final RenderSpiderRobotInfo r = this.renderdata;
        final double posX = this.getX();
        final double posY = this.getY();
        final double posZ = this.getZ();
        float d1 = (float) (this.xo - posX);
        float d2 = (float) (this.yo - posY);
        float d3 = (float) (this.zo - posZ);
        final float realv = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
        for (int i = 0; i < 6; ++i) {
            int fcount = 0;
            ++r.footingticker[i];
            r.realposx[i] = (float) (posX - r.legoff[i] * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + r.ymid[i]));
            r.realposz[i] = (float) (posZ + r.legoff[i] * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + r.ymid[i]));
            r.realposy[i] = (float) posY + r.yoff[i];
            final int it = r.footingticker[i] + r.footingticker[r.pairedwith[i]];
            if (it > 50 && r.footingticker[i] > r.footingticker[r.pairedwith[i]]) {
                r.footingticker[i] = 0;
            }
            d1 = r.realposx[i] - r.foot_xpos[i];
            d2 = r.realposy[i] - r.foot_ypos[i];
            d3 = r.realposz[i] - r.foot_zpos[i];
            float dd = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            dd *= 16.0f;
            float da = (float) (Math.abs(r.ycurrentangle[i] - (Math.toRadians(Mth.wrapDegrees((double) this.getYRot())) + r.ymid[i])) % 6.283185307179586);
            if (da > 3.141592653589793) {
                da -= (float) 6.283185307179586;
            }
            if (da < -3.141592653589793) {
                da += (float) 6.283185307179586;
            }
            da = Math.abs(da);
            if (dd > 144.0f || dd < 22.0f || da > Math.abs(r.yrange[i]) * 8.0f / 6.0f || Math.abs(r.udcurrentangle[i]) > 1.25
                    || r.footingticker[i] == 0) {
                this.findNewFooting(i);
                d1 = r.realposx[i] - r.foot_xpos[i];
                d2 = r.realposy[i] - r.foot_ypos[i];
                d3 = r.realposz[i] - r.foot_zpos[i];
                dd = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                dd *= 16.0f;
            }
            final float c1 = (float) (49.0 * Math.cos(r.p2xangle[i] - r.p1xangle[i]));
            final float c2 = 49.0f;
            final float c3 = (float) (49.0 * Math.cos(r.p2xangle[i] - r.p3xangle[i]));
            final float cc = c1 + c2 + c3;
            float diff = cc - dd;
            r.pxvelocity[i] = this.getNewVelocity(realv, (float) (diff * 3.141592653589793 / 360.0), r.pxvelocity[i]);
            if (r.pxvelocity[i] == 0.0f || Math.abs(diff) < 8.0f) {
                ++fcount;
            }
            r.p1xangle[i] += r.pxvelocity[i];
            r.p2xangle[i] = 0.0;
            r.p3xangle[i] = -r.p1xangle[i];
            if (r.uppoint[i] != 0.0f) {
                dd = (float) Math.atan2(dd, (r.realposy[i] - r.uppoint[i]) * 16.0);
            } else {
                dd = (float) Math.atan2(dd, (r.realposy[i] - r.foot_ypos[i]) * 16.0);
            }
            r.udwantedangle[i] = (float) (dd - 1.5707963267948966);
            while (r.udwantedangle[i] > 3.141592653589793) {
                r.udwantedangle[i] -= (float) 6.283185307179586;
            }
            while (r.udwantedangle[i] < -3.141592653589793) {
                r.udwantedangle[i] += (float) 6.283185307179586;
            }
            double rhm = r.udwantedangle[i];
            double rhdir = r.udcurrentangle[i];
            double rdv;
            for (rdv = (rhm - rhdir) % 6.283185307179586; rdv > 3.141592653589793; rdv -= 6.283185307179586) {
            }
            while (rdv < -3.141592653589793) {
                rdv += 6.283185307179586;
            }
            diff = (float) rdv;
            r.udvelocity[i] = this.getNewVelocity(realv * 2.0f, diff, r.udvelocity[i]);
            if (r.udvelocity[i] == 0.0f || Math.abs(diff) < 0.03490658503988659) {
                r.uppoint[i] = 0.0f;
                ++fcount;
            }
            for (rhdir += r.udvelocity[i]; rhdir > 3.141592653589793; rhdir -= 6.283185307179586) {
            }
            while (rhdir < -3.141592653589793) {
                rhdir += 6.283185307179586;
            }
            final float n6 = (float) rhdir;
            r.udcurrentangle[i] = n6;
            dd = n6;
            r.uddisplayangle[i] = dd;
            d1 = r.realposx[i] - r.foot_xpos[i];
            d3 = r.realposz[i] - r.foot_zpos[i];
            dd = (float) Math.atan2(d3, d1);
            final float n8 = dd;
            r.ywantedangle[i] = n8;
            rhm = n8;
            rhdir = r.ycurrentangle[i];
            rdv = (rhm - rhdir) % 6.283185307179586;
            if (rdv > 3.141592653589793) {
                rdv -= 6.283185307179586;
            }
            if (rdv < -3.141592653589793) {
                rdv += 6.283185307179586;
            }
            diff = (float) rdv;
            r.yvelocity[i] = this.getNewVelocity(realv, diff, r.yvelocity[i]);
            if (r.yvelocity[i] == 0.0f || Math.abs(diff) < 0.03490658503988659) {
                ++fcount;
            }
            r.ycurrentangle[i] += r.yvelocity[i];
            while (r.ycurrentangle[i] > 3.141592653589793) {
                r.ycurrentangle[i] -= (float) 6.283185307179586;
            }
            while (r.ycurrentangle[i] < -3.141592653589793) {
                r.ycurrentangle[i] += (float) 6.283185307179586;
            }
            for (dd = (float) (r.ycurrentangle[i] - Math.toRadians(Mth.wrapDegrees((double) this.getYRot())) - 1.5707963267948966);
                 dd > 3.141592653589793; dd -= (float) 6.283185307179586) {
            }
            while (dd < -3.141592653589793) {
                dd += (float) 6.283185307179586;
            }
            r.ydisplayangle[i] = dd;
            if (fcount == 3) {
                r.footup[i] = 0;
            }
        }
    }

    /**
     * {@code findNewFooting} (:451-558): search a solid block for foot {@code i} ahead of its hip, narrowing the reach
     * step by step; the default spot is half the reach, one block down.
     *
     * <p>{@code (int) fx + m} -> {@code Mth.floor(fx) + m} (R20). {@code getMaterial().isSolid()} -> the 1.21.1
     * legacy {@link BlockState#isSolid()} (PORT: shape-derived, the closest counterpart of 1.7.10's material flag).
     */
    private void findNewFooting(final int i) {
        final RenderSpiderRobotInfo r = this.renderdata;
        float f = 9.0f;
        int found = 0;
        float range = 0.0f;
        final double rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
        final double pi = 3.1415926545;
        r.footingticker[i] = 0;
        float d1 = (float) (this.getX() - this.xo);
        float d2 = (float) (this.getZ() - this.zo);
        final double rhm = Math.atan2(d2, d1);
        final double velocity = Math.sqrt(d1 * d1 + d2 * d2);
        double rdv = Math.abs(rhm - rhdir) % (pi * 2.0);
        if (rdv > pi) {
            rdv -= pi * 2.0;
        }
        rdv = Math.abs(rdv);
        if (Math.abs(velocity) < 0.01) {
            rdv = 0.0;
        }
        range = r.yrange[i];
        range *= 0.8f;
        if (Math.abs((this.yRotO - this.getYRot()) % 360.0f) > 0.75f) {
            range = 0.0f;
        }
        if (i >= 4) {
            f = 4.0f;
        }
        if (rdv > 1.5) {
            range = -range;
            f = 4.0f;
            if (i >= 4) {
                f = 9.0f;
            }
        }
        if (i == 0 || i == 1) {
            f = 6.0f;
        }
        final float deffx;
        float fx = deffx = (float) (r.realposx[i] - f / 2.0f * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + r.ymid[i]));
        final float deffz;
        float fz = deffz = (float) (r.realposz[i] + f / 2.0f * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + r.ymid[i]));
        final float deffy;
        float fy = deffy = r.realposy[i] - 1.0f;
        final float oldf = f;
        int span = 1;
        while (found == 0 && f > 2.5f) {
            fx = (float) (r.realposx[i] - f * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + r.ymid[i] - range));
            fz = (float) (r.realposz[i] + f * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (this.getYRot() + 90.0f))) + r.ymid[i] - range));
            fy = r.realposy[i];
            for (int j = 8; found == 0 && j > -9; --j) {
                for (int m = -span; found == 0 && m <= span; ++m) {
                    for (int n = -span; found == 0 && n <= span; ++n) {
                        final BlockPos bp = new BlockPos(Mth.floor(fx) + m, Mth.floor(fy) + j, Mth.floor(fz) + n);
                        final BlockState blk = this.level().getBlockState(bp);
                        if (!blk.isAir() && this.level().getBlockState(bp).isSolid()) {
                            d1 = r.realposx[i] - (fx + m);
                            final float d3 = r.realposy[i] - (fy + j + 1.0f);
                            d2 = r.realposz[i] - (fz + n);
                            float dd = (float) Math.sqrt(d1 * d1 + d3 * d3 + d2 * d2);
                            dd *= 16.0f;
                            if (dd <= 144.0f) {
                                fy += j + 1;
                                fx += m;
                                fz += n;
                                found = 1;
                                break;
                            }
                        }
                    }
                }
            }
            --f;
            if (f < 2.5f && range != 0.0f) {
                range = 0.0f;
                span = 3;
                f = oldf;
            }
        }
        if (found == 0) {
            fx = deffx;
            fy = deffy;
            fz = deffz;
        }
        final float sfx = r.foot_xpos[i];
        final float sfy = r.foot_ypos[i];
        final float sfz = r.foot_zpos[i];
        r.foot_xpos[i] = fx;
        r.foot_ypos[i] = fy;
        r.foot_zpos[i] = fz;
        if (r.footup[i] == 0) {
            r.footup[i] = 1;
            d1 = sfx - fx;
            final float d3 = sfy - fy;
            d2 = sfz - fz;
            float dd = (float) Math.sqrt(d1 * d1 + d3 * d3 + d2 * d2);
            dd *= 16.0f;
            d1 = (sfy + fy) / 2.0f;
            if (dd > 3.0f) {
                d1 += 0.3f;
            }
            if (dd > 24.0f) {
                d1 += 0.6f;
            }
            if (dd > 50.0f) {
                d1 += 0.6f;
            }
            r.uppoint[i] = d1;
        }
    }

    /** {@code shouldRiderSit} (:560-562). */
    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    // getTrackingRange 128 / getUpdateFrequency 10 / sendsVelocityUpdates true (:564-574) are not what FML used; the
    // entity type takes 128/1/false from registerModEntity (OreSpawnMain.java:4169).
    // canTriggerWalking (:576-578) returns true: step effects stay on, no NoStepTrigger (R20).

    /** {@code getRenderSpiderRobotInfo} (:588-590). */
    public RenderSpiderRobotInfo getRenderSpiderRobotInfo() {
        return this.renderdata;
    }

    /** {@code canBePushed} (:592-594). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code getMountedYOffset} (:596-598). */
    public double getMountedYOffset() {
        return 0.55 + Math.cos(this.rideTicker * 0.19f) * 0.02;
    }

    /**
     * {@code updateRiderPosition} (:600-606): 1.25 behind the centre with a wobble, at
     * {@code posY + getMountedYOffset() + rider.getYOffset()}. A server-side 1.7.10 player had {@code getYOffset()} =
     * -0.5, and 1.21.1 places passengers by their feet (W04 Elevator reading). A non-player {@code /ride} passenger
     * gets 0.
     */
    @Override
    protected void positionRider(final Entity passenger, final Entity.MoveFunction callback) {
        float f = -1.25f;
        f += (float) (Math.cos(this.rideTicker * 0.33f) * 0.05);
        final double yOffset = passenger instanceof Player ? -0.5 : 0.0;
        callback.accept(passenger,
                this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                this.getY() + this.getMountedYOffset() + yOffset,
                this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())));
    }

    /**
     * No rider controls the robot through 1.21.1's vehicle prediction - the server moves it (class comment).
     * {@code Mob} would otherwise hand control to a {@code Mob} rider.
     */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    /**
     * {@code attackEntityFrom} (:608-633): suffocation, cactus, fire, burning, magic and starvation do nothing. A
     * {@code Mob} attacker ({@code EntityLiving}; a player is none) becomes the target and is faced. Then
     * {@code super}.
     *
     * <p>{@code getDamageType()} is the message id in both versions; comparing {@link DamageSource#getMsgId()} keeps
     * the string test. PORT: 1.21.1 also gives {@code campfire} the id {@code inFire}, so a campfire is ignored too.
     */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        final String type = par1DamageSource.getMsgId();
        if (type.equals("inWall")) {
            return false;
        }
        if (type.equals("cactus")) {
            return false;
        }
        if (type.equals("inFire")) {
            return false;
        }
        if (type.equals("onFire")) {
            return false;
        }
        if (type.equals("magic")) {
            return false;
        }
        if (type.equals("starve")) {
            return false;
        }
        final Entity e = par1DamageSource.getEntity();
        if (e != null && e instanceof Mob) {
            this.setTarget((LivingEntity) e);
            this.lookAt(e, 20.0f, 20.0f);
        }
        return super.hurt(par1DamageSource, par2);
    }

    /** {@code fall} (:635-636): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:638-639): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /** {@code canBeCollidedWith} (:641-643). */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /**
     * 1.7.10's {@code moveEntity} had no block speed factor (W04 Elevator). A robot hovering above soul sand or honey
     * would otherwise be slowed by a block its box never touches. PORT: the vanilla travel of a wild robot loses the
     * factor as well.
     */
    @Override
    protected float getBlockSpeedFactor() {
        return 1.0f;
    }

    /**
     * {@code setPositionAndRotation2} (:645-658): interpolate over {@code steps + 8} ticks with a rider, else
     * {@code steps + 6}. No {@code super}: the vanilla lerp is not used by this entity.
     */
    @Override
    public void lerpTo(final double par1, final double par3, final double par5, final float par7, final float par8,
                       final int par9) {
        if (this.getFirstPassenger() != null) {
            this.boatPosRotationIncrements = par9 + 8;
        } else {
            this.boatPosRotationIncrements = par9 + 6;
        }
        this.boatX = par1;
        this.boatY = par3;
        this.boatZ = par5;
        this.boatYaw = par7;
        this.boatPitch = par8;
    }

    /** The pending interpolation target, for rotation-only packets (1.7.10 used the tracked position; W04 Elevator). */
    @Override
    public double lerpTargetX() {
        return this.boatPosRotationIncrements > 0 ? this.boatX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.boatPosRotationIncrements > 0 ? this.boatY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.boatPosRotationIncrements > 0 ? this.boatZ : this.getZ();
    }

    @Override
    public float lerpTargetYRot() {
        return this.getYRot();
    }

    @Override
    public float lerpTargetXRot() {
        return this.getXRot();
    }

    /** 1.7.10 {@code S19PacketEntityHeadLook} set the head yaw directly (Ostrich precedent); a ridden robot never
     *  reaches the vanilla head lerp in {@code LivingEntity.aiStep}. */
    @Override
    public void lerpHeadTo(final float yaw, final int steps) {
        this.setYHeadRot(yaw);
    }

    /** {@code setVelocity} (:660-665), client only: ignored while ridden. */
    @Override
    public void lerpMotion(final double par1, final double par3, final double par5) {
        if (this.getFirstPassenger() == null) {
            super.lerpMotion(par1, par3, par5);
        }
    }

    /**
     * {@code onUpdate} (:667-711): the living update, then the ridden stomp (1/50) and bite (1/9) on the server, then
     * the exhaust particles of both jets on both sides. The particle rolls use the world random on both sides as in
     * the original; {@code addParticle} does nothing on a server, as 1.7.10's {@code spawnParticle} did.
     */
    @Override
    public void tick() {
        super.tick();
        this.igniteForSeconds(0.0f); // setFire(0): never lowers the fire timer
        final Level world = this.level();
        final RandomSource rand = world.random;
        if (world.getDifficulty() != Difficulty.PEACEFUL && !world.isClientSide && this.getFirstPassenger() != null
                && rand.nextInt(50) == 0) {
            this.feetFindSomethingToHit();
        }
        if (world.getDifficulty() != Difficulty.PEACEFUL && !world.isClientSide && this.getFirstPassenger() != null
                && rand.nextInt(9) == 0) {
            LivingEntity e = null;
            e = this.findSomethingToAttack(1.0f, true);
            if (e != null) {
                final float reach = 6.0f + e.getBbWidth() / 2.0f;
                if (this.distanceToSqr(e) < reach * reach) {
                    this.setAttacking(1);
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        final float f = 4.0f;
        float dx = (float) (f * Math.cos(Math.toRadians(this.getYRot() - 80.0f)));
        float dz = (float) (f * Math.sin(Math.toRadians(this.getYRot() - 80.0f)));
        final float dx2 = (float) (f * Math.cos(Math.toRadians(this.getYRot() - 90.0f)));
        final float dz2 = (float) (f * Math.sin(Math.toRadians(this.getYRot() - 90.0f)));
        if (rand.nextInt(18) == 0) {
            this.jetParticle(ParticleTypes.FLAME, dx, dz, dx2, dz2, f, 10.0f);
        }
        if (rand.nextInt(7) == 0) {
            this.jetParticle(ParticleTypes.SMOKE, dx, dz, dx2, dz2, f, 10.0f);
        }
        if (rand.nextInt(16) == 0) {
            // "fireworksSpark"
            this.jetParticle(ParticleTypes.FIREWORK, dx, dz, dx2, dz2, f, 5.0f);
        }
        dx = (float) (f * Math.cos(Math.toRadians(this.getYRot() - 100.0f)));
        dz = (float) (f * Math.sin(Math.toRadians(this.getYRot() - 100.0f)));
        if (rand.nextInt(18) == 0) {
            this.jetParticle(ParticleTypes.FLAME, dx, dz, dx2, dz2, f, 10.0f);
        }
        if (rand.nextInt(7) == 0) {
            this.jetParticle(ParticleTypes.SMOKE, dx, dz, dx2, dz2, f, 10.0f);
        }
        if (rand.nextInt(16) == 0) {
            this.jetParticle(ParticleTypes.FIREWORK, dx, dz, dx2, dz2, f, 5.0f);
        }
    }

    /**
     * One {@code spawnParticle} call of :692-709: at {@code (posX + dx, posY + 0.5, posZ + dz)}, velocity
     * {@code dx2/f + jitter/20}, {@code jitter/ydiv}, {@code dz2/f + jitter/20}, the six world-random draws in the
     * argument order of the original.
     */
    private void jetParticle(final ParticleOptions type, final float dx, final float dz, final float dx2, final float dz2,
                             final float f, final float ydiv) {
        final RandomSource rand = this.level().random;
        final double vx = (double) (dx2 / f + (rand.nextFloat() - rand.nextFloat()) / 20.0f);
        final double vy = (double) ((rand.nextFloat() - rand.nextFloat()) / ydiv);
        final double vz = (double) (dz2 / f + (rand.nextFloat() - rand.nextFloat()) / 20.0f);
        this.level().addParticle(type, this.getX() + dx, this.getY() + 0.5, this.getZ() + dz, vx, vy, vz);
    }

    /**
     * {@code onLivingUpdate} (:713-935). Without a rider the vanilla living update runs first (on both sides, as in
     * 1.7.10), with a rider it does not. Then the motion clamps, the rider sound, and per side: the client hover
     * nudge, interpolation and leg IK; the server hover force, the rider steering and the double move.
     *
     * <p>PORT: the 1.7.10 client ran the vanilla {@code moveEntityWithHeading} for every living entity; 1.21.1's
     * {@code LivingEntity.travel} only moves an entity on the side that controls it, so an unridden client robot
     * skips that local prediction and only interpolates (R18 case 3, visual only).
     */
    @Override
    public void aiStep() {
        // :714-722; d6/d7 are never read but draw from the entity random every tick.
        final Vec3 m0 = this.getDeltaMovement();
        final double velocity = Math.sqrt(m0.x * m0.x + m0.z * m0.z);
        @SuppressWarnings("unused")
        final double d6 = this.random.nextFloat() * 2.0f - 1.0f;
        @SuppressWarnings("unused")
        final double d7 = (this.random.nextInt(2) * 2 - 1) * 0.7;
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.3;
        double gh = 1.75;
        int dist = 2;
        if (this.isRemoved()) {
            return;
        }
        if (this.getFirstPassenger() == null) {
            super.aiStep();
        }
        final Entity rider = this.getFirstPassenger();
        final Vec3 m = this.getDeltaMovement();
        double motionX = m.x;
        double motionY = m.y;
        double motionZ = m.z;
        if (motionY > 0.8500000238418579) {
            motionY = 0.8500000238418579;
        }
        if (motionY < -0.8500000238418579) {
            motionY = -0.8500000238418579;
        }
        if (motionX < -1.25) {
            motionX = -1.25;
        }
        if (motionX > 1.25) {
            motionX = 1.25;
        }
        if (motionZ < -1.25) {
            motionZ = -1.25;
        }
        if (motionZ > 1.25) {
            motionZ = 1.25;
        }
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.rideTicker += this.level().random.nextInt(3);
        if (this.playing > 0) {
            --this.playing;
        }
        if (rider != null && this.playing == 0 && this.level().random.nextInt(80) == 1) {
            this.playSoundAtEntity(ModSounds.ROBOTSPIDER.get(), 0.35f, 1.0f);
            this.playing = 125;
        }
        final double posX = this.getX();
        // posY is a local: see the class comment (field writes that moveEntity or setPosition consumed).
        double posY = this.getY();
        final double posZ = this.getZ();
        if (this.level().isClientSide) {
            if (rider == null) {
                BlockState bid = this.level().getBlockState(
                        new BlockPos(Mth.floor(posX), Mth.floor((float) posY - (float) gh + 1.0f), Mth.floor(posZ)));
                if (bid.isAir()) {
                    bid = this.level().getBlockState(new BlockPos(Mth.floor(posX), Mth.floor((float) posY - (float) gh), Mth.floor(posZ)));
                }
                if (isHoverGround(bid)) {
                    motionY += 0.12;
                    posY += 0.12; // read by setPosition below
                    this.boatY += 0.12;
                } else {
                    motionY -= 0.002;
                }
            }
            if (this.boatPosRotationIncrements > 0) {
                final double d8 = posX + (this.boatX - posX) / this.boatPosRotationIncrements;
                final double d9 = posY + (this.boatY - posY) / this.boatPosRotationIncrements;
                final double d10 = posZ + (this.boatZ - posZ) / this.boatPosRotationIncrements;
                this.setPos(d8, d9, d10);
                this.setXRot(this.getXRot() + (float) ((this.boatPitch - this.getXRot()) / this.boatPosRotationIncrements));
                double d11 = Mth.wrapDegrees(this.boatYaw - this.getYRot());
                if (rider != null) {
                    d11 = Mth.wrapDegrees(rider.getYRot() - (double) this.getYRot());
                }
                this.setYRot(this.getYRot() + (float) (d11 / this.boatPosRotationIncrements));
                this.setRot(this.getYRot(), this.getXRot());
                --this.boatPosRotationIncrements;
            } else {
                final double d8 = posX + motionX;
                final double d9 = posY + motionY;
                final double d10 = posZ + motionZ;
                this.setPos(d8, d9, d10);
                motionX *= 0.99;
                motionY *= 0.95;
                motionZ *= 0.99;
            }
            this.setDeltaMovement(motionX, motionY, motionZ);
            this.updateLegs();
            return;
        }

        // ---- Server (:797-934) ----
        if (rider != null) {
            gh = 2.25;
            final BlockState bid = this.level().getBlockState(
                    new BlockPos(Mth.floor(posX), Mth.floor((float) posY - (float) gh), Mth.floor(posZ)));
            if (isHoverGround(bid)) {
                motionY += 0.06;
                posY += 0.03; // :803, overwritten by moveEntity; shifts the lookups below
            } else {
                motionY -= 0.02;
            }
        } else {
            BlockState bid = this.level().getBlockState(
                    new BlockPos(Mth.floor(posX), Mth.floor((float) posY - (float) gh + 1.0f), Mth.floor(posZ)));
            if (bid.isAir()) {
                bid = this.level().getBlockState(new BlockPos(Mth.floor(posX), Mth.floor((float) posY - (float) gh), Mth.floor(posZ)));
            }
            if (isHoverGround(bid)) {
                motionY += 0.15;
                posY += 0.15; // :816, overwritten by moveEntity
                this.boatY += 0.15;
            } else {
                motionY -= 0.002;
            }
        }
        if (rider != null) {
            // PORT: (EntityPlayer) this.riddenByEntity (:824) threw for any other rider; 1.21.1's /ride can seat a
            // mob. A non-player rider reads as no forward input (R18 case 1, W04 Elevator).
            final Player pp = rider instanceof Player player ? player : null;
            obstruction_factor = 0.0;
            dist = 3;
            dist += (int) (velocity * 6.0);
            for (int k = 1; k < dist; ++k) {
                for (int i = 1; i < dist * 2; ++i) {
                    for (int j = -90; j <= 90; j += 30) {
                        final double dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f + j));
                        final double dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f + j));
                        // :833 (int)(posX + dx), (int) posY - k, (int)(posZ + dz) -> Mth.floor (R20).
                        final BlockState bid = this.level().getBlockState(
                                new BlockPos(Mth.floor(posX + dx), Mth.floor(posY) - k, Mth.floor(posZ + dz)));
                        if (isHoverGround(bid)) {
                            obstruction_factor += 0.02;
                        }
                    }
                }
            }
            motionY += obstruction_factor * 0.05;
            posY += obstruction_factor * 0.05; // :841, overwritten by moveEntity
            double d8 = rider.getYRot();
            d8 %= 360.0;
            while (d8 < 0.0) {
                d8 += 360.0;
            }
            double d9 = this.getYRot();
            d9 %= 360.0;
            while (d9 < 0.0) {
                d9 += 360.0;
            }
            relative_g = (d8 - d9) % 180.0;
            while (relative_g < 0.0) {
                relative_g += 180.0;
            }
            if (relative_g > 90.0) {
                relative_g -= 180.0;
            }
            if (velocity > 0.01) {
                d8 = 1.85 - velocity;
                d8 = Math.abs(d8);
                if (d8 < 0.01) {
                    d8 = 0.01;
                }
                if (d8 > 0.9) {
                    d8 = 0.9;
                }
                this.setYRot(rider.getYRot() + (float) (relative_g * d8));
            } else {
                this.setYRot(rider.getYRot());
            }
            // :864-867 relative_g = |relative_g| * velocity, zeroed above 50: never read.
            this.setXRot(0.0f);
            this.setRot(this.getYRot(), this.getXRot());
            double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
            // rr = atan2(rider.motionZ, rider.motionX) (:871) and rt = 0 (:874) are never read.
            final double rhm = Math.atan2(motionZ, motionX);
            final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
            final double pi = 3.1415926545;
            double deltav = 0.0;
            final float im = pp != null ? pp.zza : 0.0f; // moveForward
            double rdv = Math.abs(rhm - rhdir) % (pi * 2.0);
            if (rdv > pi) {
                rdv -= pi * 2.0;
            }
            rdv = Math.abs(rdv);
            if (Math.abs(newvelocity) < 0.01) {
                rdv = 0.0;
            }
            if (rdv > 1.5) {
                newvelocity = -newvelocity;
            }
            if (Math.abs(im) > 0.001f) {
                if (im > 0.0f) {
                    deltav = 0.05;
                } else {
                    max_speed = 0.25;
                    deltav = -0.05;
                }
                newvelocity += deltav;
                if (newvelocity >= 0.0) {
                    if (newvelocity > max_speed) {
                        newvelocity = max_speed;
                    }
                    motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
                    motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
                } else {
                    if (newvelocity < -max_speed) {
                        newvelocity = -max_speed;
                    }
                    newvelocity = -newvelocity;
                    motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * newvelocity;
                    motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * newvelocity;
                }
            } else if (newvelocity >= 0.0) {
                motionX = Math.cos(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 90.0f)) * newvelocity;
            } else {
                motionX = Math.cos(Math.toRadians(this.getYRot() + 270.0f)) * (newvelocity * -1.0);
                motionZ = Math.sin(Math.toRadians(this.getYRot() + 270.0f)) * (newvelocity * -1.0);
            }
            // moveEntity (:922): 1.21.1's move zeroes the blocked motion components like 1.7.10's did (W04 Elevator).
            Vec3 moved = this.moveEntity(motionX, motionY, motionZ);
            motionX = moved.x * 0.98;
            motionY = moved.y * 0.98;
            motionZ = moved.z * 0.98;
        }
        // moveEntity (:927).
        final Vec3 moved = this.moveEntity(motionX, motionY, motionZ);
        motionX = moved.x * 0.8;
        motionY = moved.y * 0.98;
        motionZ = moved.z * 0.8;
        this.setDeltaMovement(motionX, motionY, motionZ);
        if (rider != null && rider.isRemoved()) {
            rider.stopRiding();
        }
    }

    /** {@code moveEntity(mx, my, mz)}: motion in, the motion after collision out. */
    private Vec3 moveEntity(final double mx, final double my, final double mz) {
        this.setDeltaMovement(mx, my, mz);
        this.move(MoverType.SELF, new Vec3(mx, my, mz));
        return this.getDeltaMovement();
    }

    /**
     * {@code bid != air && bid != water && bid != flowing_water && bid != lava && bid != flowing_lava} (:764, :801,
     * :814, :834). The flowing variants are one block each in 1.21.1; {@code isAir()} covers cave and void air (R22).
     */
    private static boolean isHoverGround(final BlockState bid) {
        return !bid.isAir() && !bid.is(Blocks.WATER) && !bid.is(Blocks.LAVA);
    }

    /** {@code goThisWay} (:937-940). */
    public void goThisWay(final double mx, final double mz) {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(mx, m.y, mz);
    }

    // isAIEnabled (:942-944) = no rider: with a rider onLivingUpdate never reaches the AI; 1.21.1 has no old AI.

    /** {@code writeEntityToNBT} (:946-949). */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.addAdditionalSaveData(par1NBTTagCompound);
        par1NBTTagCompound.putInt("AntRobotOwned", this.owned);
    }

    /** {@code readEntityFromNBT} (:951-954). */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
        super.readAdditionalSaveData(par1NBTTagCompound);
        this.owned = par1NBTTagCompound.getInt("AntRobotOwned");
    }

    // getShadowSize (:956-958) = 0.95 is not the renderer's shadow; RenderAntRobot keeps 0.99 * 1.0 (manifest).

    /**
     * {@code interact} (:960-996). A wild robot swallows the click. An iron ingot within 5 blocks heals up to 100 on
     * the server and is used up (not in creative). A robot ridden by another player refuses; otherwise the player
     * mounts within 4 blocks with the mount sound. Always handled.
     *
     * <p>PORT: {@code Mob.interact} is final in 1.21.1 and applies a held name tag or lead before this method runs
     * (W04 Elevator, R18 case 3). {@code inventory.getCurrentItem()} is the selected main-hand slot; the
     * {@code stackSize <= 0} cleanup (:962-965) has no 1.21.1 case.
     */
    @Override
    protected InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        final boolean clientSide = this.level().isClientSide;
        final ItemStack var2 = par1EntityPlayer.getInventory().getSelected();
        if (this.owned == 0) {
            return InteractionResult.sidedSuccess(clientSide);
        }
        if (!var2.isEmpty() && var2.is(Items.IRON_INGOT) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
            if (!clientSide) {
                float f = this.getMaxHealth() - this.getHealth();
                if (f > 100.0f) {
                    f = 100.0f;
                }
                if (f > 0.0f) {
                    this.heal(f);
                }
            }
            if (!par1EntityPlayer.getAbilities().instabuild) {
                var2.shrink(1);
            }
            return InteractionResult.sidedSuccess(clientSide);
        }
        final Entity rider = this.getFirstPassenger();
        if (rider != null && rider instanceof Player && rider != par1EntityPlayer) {
            return InteractionResult.sidedSuccess(clientSide);
        }
        if (!clientSide && rider == null && par1EntityPlayer.distanceToSqr(this) < 16.0) {
            par1EntityPlayer.startRiding(this);
            this.playSoundAtEntity(ModSounds.ROBOTSPIDERMOUNT.get(), 0.45f, 1.0f);
        }
        return InteractionResult.sidedSuccess(clientSide);
    }

    /**
     * {@code feetFindSomethingToHit} (:998-1012): every suitable living entity in {@code expand(10, 8, 10)} is
     * stomped. {@code PlayNicely} disables it.
     */
    private void feetFindSomethingToHit() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(10.0, 8.0, 10.0));
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8 = null;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.feetisSuitableTarget(var8, false)) {
                this.feetattackEntityAsMob(var8);
            }
        }
    }

    /** {@code feetisSuitableTarget} (:1014-1052): 6 to 9 blocks away, no creative player. */
    private boolean feetisSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (par1EntityLiving instanceof AntRobot) {
            return false;
        }
        if (par1EntityLiving == this.getFirstPassenger()) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        final float d1 = (float) (par1EntityLiving.getX() - this.getX());
        final float d2 = (float) (par1EntityLiving.getY() - this.getY());
        final float d3 = (float) (par1EntityLiving.getZ() - this.getZ());
        final float dd = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
        if (dd > 9.0f) {
            return false;
        }
        if (dd < 6.0f) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return true;
    }

    /**
     * {@code feetattackEntityAsMob} (:1054-1069): {@code attack / 10} as mob damage, then knockback 0.6 horizontal and
     * 0.1 up (0.2 for a player or a removed entity) if the hit landed. {@code addVelocity} is {@code push}.
     */
    public boolean feetattackEntityAsMob(final Entity par1Entity) {
        boolean ret = false;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final double ks = 0.6;
            double inair = 0.1;
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            ret = par1Entity.hurt(this.damageSources().mobAttack(this), MobStats.AntRobot_stats().attack() / 10.0f);
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            if (ret) {
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
        }
        return ret;
    }

    /**
     * {@code findSomethingToAttack} (:1071-1086): the first suitable living entity in
     * {@code expand(12 * distmul, 12, 12 * distmul)}, unsorted. {@code PlayNicely} disables it.
     */
    @Nullable
    private LivingEntity findSomethingToAttack(final float distmul, final boolean dircheck) {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0 * distmul, 12.0, 12.0 * distmul));
        final Iterator<LivingEntity> var6 = var5.iterator();
        LivingEntity var8 = null;
        while (var6.hasNext()) {
            var8 = var6.next();
            if (this.isSuitableTarget(var8, dircheck)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * {@code isSuitableTarget} (:1088-1132): alive, no Ant Robot, not the rider, not ignorable, visible; with
     * {@code par2} within squared distance 36 always, else within 0.75 rad of {@code yaw + 90}; no creative player.
     */
    private boolean isSuitableTarget(@Nullable final LivingEntity par1EntityLiving, final boolean par2) {
        if (par1EntityLiving == null) {
            return false;
        }
        if (par1EntityLiving == this) {
            return false;
        }
        if (!par1EntityLiving.isAlive()) {
            return false;
        }
        if (par1EntityLiving instanceof AntRobot) {
            return false;
        }
        if (par1EntityLiving == this.getFirstPassenger()) {
            return false;
        }
        if (MyUtils.isIgnoreable(par1EntityLiving)) {
            return false;
        }
        if (!this.getSensing().hasLineOfSight(par1EntityLiving)) {
            return false;
        }
        if (par2) {
            final double rr = Math.atan2(par1EntityLiving.getZ() - this.getZ(), par1EntityLiving.getX() - this.getX());
            final double rhdir = Math.toRadians((this.getYRot() + 90.0f) % 360.0f);
            final double pi = 3.1415926545;
            double rdd = Math.abs(rr - rhdir) % (pi * 2.0);
            if (rdd > pi) {
                rdd -= pi * 2.0;
            }
            rdd = Math.abs(rdd);
            if (this.distanceToSqr(par1EntityLiving) < 36.0) {
                return true;
            }
            if (rdd > 0.75) {
                return false;
            }
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return true;
    }

    /**
     * {@code attackEntityAsMob} (:1134-1149), without {@code super}: {@code AntRobot_stats.attack} as mob damage,
     * knockback 0.7 horizontal and 0.1 up (0.2 for a player or a removed entity) if the hit landed.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        boolean ret = false;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final double ks = 0.7;
            double inair = 0.1;
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            ret = par1Entity.hurt(this.damageSources().mobAttack(this), (float) MobStats.AntRobot_stats().attack());
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            if (ret) {
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
        }
        return ret;
    }

    /** {@code getAttacking} (:1151-1153). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:1155-1157). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    // getDropItem (:1159-1161) returns null: no vanilla drop.

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropItemRand} (:1163-1171): an item entity one block up, x and z shifted by {@code OreSpawnRand} -1..+1,
     * spawned directly (no pickup delay, no drop capture), as in 1.7.10.
     */
    private ItemStack dropItemRand(final ItemLike index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        final ItemEntity var3 = new ItemEntity(this.level(),
                this.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2), this.getY() + 1.0,
                this.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2), is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /**
     * {@code dropFewItems} (:1173-1224): 7..13 rolls of world {@code rand(12)}: redstone, repeater, comparator,
     * redstone block (3 and 8), dispenser, sticky piston, piston, lever, light weighted pressure plate, iron ingot,
     * 11 nothing. Looting ignored.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        for (int i = 7 + this.level().random.nextInt(7), var4 = 0; var4 < i; ++var4) {
            final int var5 = this.level().random.nextInt(12);
            switch (var5) {
                case 0 -> this.dropItemRand(Items.REDSTONE, 1);
                case 1 -> this.dropItemRand(Items.REPEATER, 1);
                case 2 -> this.dropItemRand(Items.COMPARATOR, 1);
                case 3 -> this.dropItemRand(Blocks.REDSTONE_BLOCK, 1);
                case 4 -> this.dropItemRand(Blocks.DISPENSER, 1);
                case 5 -> this.dropItemRand(Blocks.STICKY_PISTON, 1);
                case 6 -> this.dropItemRand(Blocks.PISTON, 1);
                case 7 -> this.dropItemRand(Blocks.LEVER, 1);
                case 8 -> this.dropItemRand(Blocks.REDSTONE_BLOCK, 1);
                case 9 -> this.dropItemRand(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1);
                case 10 -> this.dropItemRand(Items.IRON_INGOT, 1);
                default -> {
                }
            }
        }
    }

    /**
     * {@code World.playSoundAtEntity(this, name, volume, pitch)}: broadcast from the server at the entity; on the
     * client it did nothing, as {@code Level.playSound(null, ...)} does. Category {@code master} from the 20.3 jar's
     * sounds.json (W04 Elevator).
     */
    private void playSoundAtEntity(final SoundEvent sound, final float volume, final float pitch) {
        this.level().playSound((Player) null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.MASTER, volume, pitch);
    }
}
