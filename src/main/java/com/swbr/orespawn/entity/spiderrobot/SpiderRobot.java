package com.swbr.orespawn.entity.spiderrobot;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyArmor;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobStats;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.entity.cannonfodder.CannonFodderSupport;
import com.swbr.orespawn.registry.ModSounds;
import com.swbr.orespawn.util.MyUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.SpiderRobot} (SpiderRobot.java:20-1166, verhalten/entity-12.md "SpiderRobot"),
 * registry id {@code robot_spider} ("Robot Spider", OreSpawnMain.java:4141/:4145, tracking 128 / 1 / no velocity).
 * A black-widow machine eleven blocks long on eight IK legs. Empty it only watches players; ridden - by a player or a
 * {@link SpiderDriver} - it stomps everything 12 to 18 blocks away and bites what stands in front of it.
 *
 * <h2>Health above 1024 (R4)</h2>
 * {@code SpiderRobot_stats.health} defaults to 1500. The attribute holds
 * {@link LegacyCombatMath#attributeMaxHealth}; {@link VirtualHealth} scales incoming damage and healing centrally,
 * and the two places that read or write health directly - the iron-ingot repair here, the kit and the wrench - go
 * through the {@link VirtualHealth} helpers in original units.
 *
 * <h2>Where the physics run</h2>
 * The original split is kept, as for the hoverboard ({@code Elevator}, W04): {@link #aiStep} is
 * {@code onLivingUpdate} - {@code super} only while nobody rides, then the robot's own motion code on the server
 * (with <b>two</b> {@code moveEntity} calls per tick while a player rides) and interpolation plus the leg IK on the
 * client. {@link #getControllingPassenger} is {@code null}, so no client predicts the robot and 1.21.1's
 * {@code travelRidden} never runs; the rider's input is read on the server from {@code Player.zza}.
 *
 * <p>PORT (R15): the client branch cast the rider to {@code EntityClientPlayerMP} and re-sent
 * {@code C05PacketPlayerLook} and {@code C0CPacketInput} every tick (:718-722) - a client class in common code that a
 * dedicated server cannot load. Nothing needs to replace it: {@code LocalPlayer.tick} sends
 * {@code ServerboundMovePlayerPacket.Rot} and {@code ServerboundPlayerInputPacket} itself every tick while the player
 * is a passenger (LocalPlayer.java:238-240), and {@code ServerPlayer.setPlayerInput} writes {@code xxa}/{@code zza}
 * for a passenger (ServerPlayer.java:1247-1259). The robot does not read {@code flyup_keystate}, so the W01 payload
 * ({@code RiderKeys}) has nothing to carry here. The cast also threw a {@code ClassCastException} on every client
 * that saw <em>another</em> player ride the robot ({@code EntityOtherPlayerMP}); that crash is gone with it
 * (R18 case 1).
 *
 * <h2>1:1 details worth knowing</h2>
 * <ul>
 *   <li>{@code writeEntityToNBT}/{@code readEntityFromNBT} are empty and call no {@code super} (:896-900): health,
 *       effects and attributes are not saved, a reloaded robot is back at full health.</li>
 *   <li>While ridden neither {@code super.onLivingUpdate} nor {@code updateAITasks} runs, so the sensing cache is
 *       never cleared: a target's visibility stays what it was the first time {@link #findSomethingToAttack} asked.
 *       1.21.1 clears it in {@code Mob.serverAiStep}, which is skipped the same way.</li>
 *   <li>The leg IK tramples tall grass and turns grass to dirt only in the <em>client</em> world (:416-425, inside
 *       {@code updateLegs}, which returns on the server) - a local ghost change, as in 1.7.10.</li>
 *   <li>{@code getTrackingRange} 128 / {@code getUpdateFrequency} 10 / {@code sendsVelocityUpdates} true (:543-553)
 *       were not used by the registration; the entity type takes 128 / 1 / false (Elevator precedent).</li>
 * </ul>
 */
public class SpiderRobot extends Mob implements LegacyArmor, VirtualHealth {

    /** DataWatcher 20 (:563, :1105-1111): {@code attacking}, read by the model for the jaw animation. */
    private static final EntityDataAccessor<Integer> DATA_ATTACKING =
            SynchedEntityData.defineId(SpiderRobot.class, EntityDataSerializers.INT);

    // Client-side interpolation state (:22-27), filled by lerpTo.
    private int boatPosRotationIncrements;
    private double boatX;
    private double boatY;
    private double boatZ;
    private double boatYaw;
    private double boatPitch;
    private int playing;
    @SuppressWarnings("unused")
    private GenericTargetSorter TargetSorter;
    private float moveSpeed;
    /** Leg state of the model (plain data, no client imports). */
    private RenderSpiderRobotInfo renderdata;
    private int didonce;
    private int rideTicker;

    /**
     * {@code SpiderRobot(World)} (:35-50). {@code setSize(3.25f, 2.25f)} (:43) and {@code isImmuneToFire} (:48) are the
     * entity type; {@code func_110163_bv} in {@code entityInit} (:561) is {@code enablePersistence}.
     *
     * <p>{@code entityInit} also called {@code initLegData} (:562). It ran inside the {@code Entity} constructor, before
     * the field initialisers of this class, which then replaced {@code renderdata} with a fresh, all-zero instance
     * (:40) and reset {@code didonce} to 0 (:41) - the call had no lasting effect, and the first client
     * {@link #updateLegs} initialises the legs at the real position. The port starts from that state directly.
     */
    public SpiderRobot(final EntityType<? extends SpiderRobot> type, final Level par1World) {
        super(type, par1World);
        this.playing = 0;
        this.TargetSorter = null;
        this.moveSpeed = 0.35f;
        this.renderdata = new RenderSpiderRobotInfo();
        this.didonce = 0;
        this.rideTicker = 0;
        this.TargetSorter = new GenericTargetSorter(this);
        // PORT: goals are added on both sides, as the 1.7.10 constructor did (W04 precedent).
        this.goalSelector.addGoal(0, new EntityAIWatchClosest(this, Player.class, 12.0f));
        this.goalSelector.addGoal(1, new EntityAILookIdle(this));
        this.xpReward = MobStats.SpiderRobot_stats().health() / 2;
        this.setPersistenceRequired();
        // PORT: applyEntityAttributes (:72-78) read SpiderRobot_stats during construction; the attribute supplier is
        // built before the config loads (R3), so the runtime values go in here, followed by the full health
        // (GiantRobot precedent). MAX_HEALTH takes the R4 clamp of the original value.
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LegacyCombatMath.attributeMaxHealth(this.getOriginalMaxHealth()));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) this.moveSpeed);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((double) MobStats.SpiderRobot_stats().attack());
        this.setHealth(this.getMaxHealth());
    }

    /**
     * {@code SpiderRobot(World, x, y, z)} (:61-70); {@code yOffset} is 0 for this entity. Unused in the original (grep),
     * kept for completeness.
     */
    public SpiderRobot(final EntityType<? extends SpiderRobot> type, final Level par1World,
                       final double par2, final double par4, final double par6) {
        this(type, par1World);
        this.setPos(par2, par4, par6);
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.xo = par2;
        this.yo = par4;
        this.zo = par6;
    }

    /**
     * {@code applyEntityAttributes} (:72-78) with the config defaults (manifest 1500 / 0.35 / 100); see the constructor.
     *
     * <p>PORT: {@code STEP_HEIGHT} 0.5 is the 1.7.10 {@code EntityLivingBase} value (Elevator precedent); 1.21.1
     * defaults living entities to 0.6.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LegacyCombatMath.attributeMaxHealth(1500.0))
                .add(Attributes.MOVEMENT_SPEED, (double) 0.35f)
                .add(Attributes.ATTACK_DAMAGE, 100.0)
                .add(Attributes.STEP_HEIGHT, 0.5);
    }

    /** R4: the original maximum, {@code SpiderRobot_stats.health}. */
    @Override
    public double getOriginalMaxHealth() {
        return MobStats.SpiderRobot_stats().health();
    }

    /** {@code canDespawn} (:80-82). */
    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer) {
        return false;
    }

    /** {@code getTotalArmorValue} (:84-86). */
    public int getTotalArmorValue() {
        return MobStats.SpiderRobot_stats().defense();
    }

    /** R5: the legacy armor formula reads {@link #getTotalArmorValue}. */
    @Override
    public int getLegacyArmorValue() {
        return this.getTotalArmorValue();
    }

    // updateAITasks (:88-96) and updateAITick (:98-103) return while ridden. Both only run inside
    // super.onLivingUpdate, which aiStep skips while ridden, so the guards are already in place; the isDead guard
    // cannot trigger inside LivingEntity.tick (aiStep only runs for an entity that is not removed).

    /** {@code initLegData} (:105-193). */
    private void initLegData() {
        if (this.renderdata == null) {
            this.renderdata = new RenderSpiderRobotInfo();
        }
        for (int i = 0; i < 8; ++i) {
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
                this.renderdata.legoff[i] = 1.25f;
                this.renderdata.ymid[i] = -0.32f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 1;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 1) {
                this.renderdata.legoff[i] = 1.25f;
                this.renderdata.ymid[i] = 3.4615927f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 0;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 2) {
                this.renderdata.legoff[i] = 2.0f;
                this.renderdata.ymid[i] = -1.0f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 3;
                this.renderdata.yoff[i] = -0.1f;
            }
            if (i == 3) {
                this.renderdata.legoff[i] = 2.0f;
                this.renderdata.ymid[i] = 4.1415925f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 2;
                this.renderdata.yoff[i] = -0.1f;
            }
            if (i == 4) {
                this.renderdata.legoff[i] = 1.75f;
                this.renderdata.ymid[i] = 0.62831855f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 5;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 5) {
                this.renderdata.legoff[i] = 1.75f;
                this.renderdata.ymid[i] = 2.5132742f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 4;
                this.renderdata.yoff[i] = -0.3f;
            }
            if (i == 6) {
                this.renderdata.legoff[i] = 3.4f;
                this.renderdata.ymid[i] = 1.05f;
                this.renderdata.yrange[i] = 0.2617994f;
                this.renderdata.pairedwith[i] = 7;
                this.renderdata.yoff[i] = -0.1f;
            }
            if (i == 7) {
                this.renderdata.legoff[i] = 3.4f;
                this.renderdata.ymid[i] = 2.0915928f;
                this.renderdata.yrange[i] = -0.2617994f;
                this.renderdata.pairedwith[i] = 6;
                this.renderdata.yoff[i] = -0.1f;
            }
        }
    }

    /** {@code getNewVelocity} (:195-237): the joint's angular velocity towards its wanted angle. */
    private float getNewVelocity(final float v, final float diff, float curval) {
        float tv = v;
        tv *= 8.0f;
        if (tv < 1.0f) {
            tv = 1.0f;
        }
        if (tv > 4.0f) {
            tv = 4.0f;
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

    /**
     * {@code updateLegs} (:239-428): the client-side leg IK, once per client tick from {@link #aiStep}. Returns on the
     * server. Block coordinates: {@code (int)} casts on the foot position are {@link Mth#floor} (R20).
     */
    public void updateLegs() {
        if (!this.level().isClientSide) {
            return;
        }
        this.setYRot(this.getYRot() % 360.0f);
        while (this.getYRot() < 0.0f) {
            this.setYRot(this.getYRot() + 360.0f);
        }
        final RenderSpiderRobotInfo renderdata = this.renderdata;
        ++renderdata.gpcounter;
        if (this.didonce == 0) {
            this.didonce = 1;
            this.initLegData();
        }
        float d1 = (float) (this.xo - this.getX());
        float d2 = (float) (this.yo - this.getY());
        float d3 = (float) (this.zo - this.getZ());
        final float realv = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
        final RenderSpiderRobotInfo r = this.renderdata;
        final float rotationYaw = this.getYRot();
        for (int i = 0; i < 8; ++i) {
            int fcount = 0;
            ++r.footingticker[i];
            r.realposx[i] = (float) (this.getX() - r.legoff[i] * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (rotationYaw + 90.0f))) + r.ymid[i]));
            r.realposz[i] = (float) (this.getZ() + r.legoff[i] * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (rotationYaw + 90.0f))) + r.ymid[i]));
            r.realposy[i] = (float) this.getY() + r.yoff[i];
            final int it = r.footingticker[i] + r.footingticker[r.pairedwith[i]];
            if (it > 50 && r.footingticker[i] > r.footingticker[r.pairedwith[i]]) {
                r.footingticker[i] = 0;
            }
            d1 = r.realposx[i] - r.foot_xpos[i];
            d2 = r.realposy[i] - r.foot_ypos[i];
            d3 = r.realposz[i] - r.foot_zpos[i];
            float dd = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
            dd *= 16.0f;
            float da = (float) (Math.abs(r.ycurrentangle[i] - (Math.toRadians(Mth.wrapDegrees((double) rotationYaw)) + r.ymid[i])) % 6.283185307179586);
            if (da > 3.141592653589793) {
                da -= (float) 6.283185307179586;
            }
            if (da < -3.141592653589793) {
                da += (float) 6.283185307179586;
            }
            da = Math.abs(da);
            if (dd > 294.0f || dd < 32.0f || da > Math.abs(r.yrange[i]) * 8.0f / 7.0f || Math.abs(r.udcurrentangle[i]) > 1.25 || r.footingticker[i] == 0) {
                this.findNewFooting(i);
                d1 = r.realposx[i] - r.foot_xpos[i];
                d2 = r.realposy[i] - r.foot_ypos[i];
                d3 = r.realposz[i] - r.foot_zpos[i];
                dd = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                dd *= 16.0f;
            }
            final float c1 = (float) (99.0 * Math.cos(r.p2xangle[i] - r.p1xangle[i]));
            final float c2 = 99.0f;
            final float c3 = (float) (99.0 * Math.cos(r.p2xangle[i] - r.p3xangle[i]));
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
            for (dd = (float) (r.ycurrentangle[i] - Math.toRadians(Mth.wrapDegrees((double) rotationYaw)) - 1.5707963267948966); dd > 3.141592653589793; dd -= (float) 6.283185307179586) {
            }
            while (dd < -3.141592653589793) {
                dd += (float) 6.283185307179586;
            }
            r.ydisplayangle[i] = dd;
            if (fcount == 3) {
                r.footup[i] = 0;
                final BlockPos foot = new BlockPos(Mth.floor(r.foot_xpos[i]), Mth.floor(r.foot_ypos[i]), Mth.floor(r.foot_zpos[i]));
                BlockState bid = this.level().getBlockState(foot);
                // Blocks.tallgrass: short grass and fern (CannonFodderSupport, W06). mobGriefing of the client world.
                if (CannonFodderSupport.isLegacyTallGrass(bid) && this.getFirstPassenger() != null && this.mobGriefing()) {
                    this.level().setBlock(foot, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
                // (int) foot_ypos - 1: floor(y) - 1 (R20).
                final BlockPos below = foot.below();
                bid = this.level().getBlockState(below);
                if (bid.is(Blocks.GRASS_BLOCK) && this.getFirstPassenger() != null && this.mobGriefing()) {
                    this.level().setBlock(below, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    /** {@code findNewFooting} (:430-537): the next foothold of leg {@code i}, searched ahead in the walking direction. */
    private void findNewFooting(final int i) {
        final RenderSpiderRobotInfo r = this.renderdata;
        final float rotationYaw = this.getYRot();
        float f = 16.0f;
        int found = 0;
        float range = 0.0f;
        final double rhdir = Math.toRadians((rotationYaw + 90.0f) % 360.0f);
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
        range *= 0.875f;
        if (Math.abs((this.yRotO - rotationYaw) % 360.0f) > 0.75f) {
            range = 0.0f;
        }
        if (i >= 4) {
            f = 10.0f;
        }
        if (rdv > 1.5) {
            range = -range;
            f = 10.0f;
            if (i >= 4) {
                f = 16.0f;
            }
        }
        final float deffx;
        float fx = deffx = (float) (r.realposx[i] - f / 2.0f * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (rotationYaw + 90.0f))) + r.ymid[i]));
        final float deffz;
        float fz = deffz = (float) (r.realposz[i] + f / 2.0f * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (rotationYaw + 90.0f))) + r.ymid[i]));
        final float deffy;
        float fy = deffy = r.realposy[i] - 1.0f;
        final float oldf = f;
        int span = 1;
        while (found == 0 && f > 3.5f) {
            fx = (float) (r.realposx[i] - f * Math.sin(Math.toRadians(Mth.wrapDegrees((double) (rotationYaw + 90.0f))) + r.ymid[i] - range));
            fz = (float) (r.realposz[i] + f * Math.cos(Math.toRadians(Mth.wrapDegrees((double) (rotationYaw + 90.0f))) + r.ymid[i] - range));
            fy = r.realposy[i];
            for (int j = 11; found == 0 && j > -14; --j) {
                for (int m = -span; found == 0 && m <= span; ++m) {
                    for (int n = -span; found == 0 && n <= span; ++n) {
                        // (int) fx + m etc.: floor (R20). Material.isSolid() is the 1.21.1 legacy "solid" flag.
                        final BlockState blk = this.level().getBlockState(new BlockPos(Mth.floor(fx) + m, Mth.floor(fy) + j, Mth.floor(fz) + n));
                        if (!blk.isAir() && blk.isSolid()) {
                            fy += j + 1;
                            fx += m;
                            fz += n;
                            found = 1;
                            break;
                        }
                    }
                }
            }
            if (found == 1) {
                d1 = r.realposx[i] - fx;
                final float d3 = r.realposy[i] - fy;
                d2 = r.realposz[i] - fz;
                float dd = (float) Math.sqrt(d1 * d1 + d3 * d3 + d2 * d2);
                dd *= 16.0f;
                if (dd > 294.0f) {
                    found = 0;
                }
            }
            --f;
            if (f < 3.5f && range != 0.0f) {
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
                ++d1;
            }
            if (dd > 48.0f) {
                d1 += 1.5f;
            }
            if (dd > 100.0f) {
                d1 += 1.5f;
            }
            r.uppoint[i] = d1;
        }
    }

    /** {@code shouldRiderSit} (:539-541). */
    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    /** {@code entityInit} (:559-564): DataWatcher 20. */
    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ATTACKING, 0);
    }

    /** {@code getRenderSpiderRobotInfo} (:566-568). */
    public RenderSpiderRobotInfo getRenderSpiderRobotInfo() {
        return this.renderdata;
    }

    /** {@code canBePushed} (:570-572). */
    @Override
    public boolean isPushable() {
        return false;
    }

    /** {@code getMountedYOffset} (:574-579): 2.0 for a Spider Driver, otherwise 2.625 with a slight bob. */
    public double getMountedYOffset() {
        final Entity rider = this.getFirstPassenger();
        if (rider != null && rider instanceof SpiderDriver) {
            return 2.0;
        }
        return 2.625 + Math.cos(this.rideTicker * 0.19f) * 0.02;
    }

    /**
     * {@code updateRiderPosition} (:581-587): three blocks behind the centre, against the facing, with a small sway;
     * {@code posY + getMountedYOffset() + rider.getYOffset()}. A server-side 1.7.10 player had {@code getYOffset()}
     * -0.5, every other rider (the Spider Driver: {@code EntitySpider} has no override, javap {@code yn}) 0 - the
     * Elevator reading, since 1.21.1 positions passengers by their feet.
     */
    @Override
    protected void positionRider(final Entity passenger, final Entity.MoveFunction callback) {
        float f = -3.0f;
        f += (float) (Math.cos(this.rideTicker * 0.33f) * 0.05);
        final double yOffset = passenger instanceof Player ? -0.5 : 0.0;
        callback.accept(passenger,
                this.getX() - f * Math.sin(Math.toRadians(this.getYRot())),
                this.getY() + this.getMountedYOffset() + yOffset,
                this.getZ() + f * Math.cos(Math.toRadians(this.getYRot())));
    }

    /**
     * No rider controls the robot through 1.21.1's vehicle prediction - the server moves it (see the class comment).
     * {@code Mob} would otherwise hand control to the Spider Driver.
     */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    /** {@code attackEntityFrom} (:589-591): suffocation, cactus, fire, magic and starvation do nothing. */
    @Override
    public boolean hurt(final DamageSource par1DamageSource, final float par2) {
        return !par1DamageSource.is(DamageTypes.IN_WALL) && !par1DamageSource.is(DamageTypes.CACTUS)
                && !par1DamageSource.is(DamageTypes.IN_FIRE) && !par1DamageSource.is(DamageTypes.ON_FIRE)
                && !par1DamageSource.is(DamageTypes.MAGIC) && !par1DamageSource.is(DamageTypes.STARVE)
                && super.hurt(par1DamageSource, par2);
    }

    /** {@code fall} (:593-594): no fall damage. */
    @Override
    public boolean causeFallDamage(final float fallDistance, final float multiplier, final DamageSource source) {
        return false;
    }

    /** {@code updateFallState} (:596-597): no fall bookkeeping. */
    @Override
    protected void checkFallDamage(final double y, final boolean onGround, final BlockState state, final BlockPos pos) {
    }

    /**
     * 1.7.10's {@code moveEntity} had no block speed factor (Elevator precedent): a robot hovering over soul sand or
     * honey keeps its motion.
     */
    @Override
    protected float getBlockSpeedFactor() {
        return 1.0f;
    }

    /** {@code canBeCollidedWith} (:599-601): can be targeted and hit. */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /**
     * {@code setPositionAndRotation2} (:603-616): a ridden robot interpolates over {@code steps + 8} ticks, an empty
     * one over {@code steps + 6}.
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

    /** The pending interpolation target, for rotation-only packets (1.7.10 used the tracked position). */
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

    /** {@code setVelocity} (:618-623), client only: ignored while ridden. */
    @Override
    public void lerpMotion(final double par1, final double par3, final double par5) {
        if (this.getFirstPassenger() == null) {
            super.lerpMotion(par1, par3, par5);
        }
    }

    /**
     * {@code onUpdate} (:625-656): {@code super} (which runs {@link #aiStep}), then the stomp (1/40) and the bite (1/15)
     * while ridden on the server, then the exhaust particles on both sides (a server discards them, as in 1.7.10).
     *
     * <p>{@code setFire(0)} is {@code igniteForTicks(0)} (Triffid precedent): it only raises negative fire ticks.
     */
    @Override
    public void tick() {
        super.tick();
        this.igniteForTicks(0);
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && !this.level().isClientSide && this.getFirstPassenger() != null
                && this.level().random.nextInt(40) == 0) {
            this.feetFindSomethingToHit();
        }
        if (this.level().getDifficulty() != Difficulty.PEACEFUL && !this.level().isClientSide && this.getFirstPassenger() != null
                && this.level().random.nextInt(15) == 0) {
            LivingEntity e = null;
            e = this.findSomethingToAttack();
            if (e != null) {
                final float reach = 12.0f + e.getBbWidth() / 2.0f;
                if (this.distanceToSqr(e) < reach * reach) {
                    this.setAttacking(1);
                    this.doHurtTarget(e);
                }
            } else {
                this.setAttacking(0);
            }
        }
        final float f = 8.0f;
        final float dx = (float) (f * Math.cos(Math.toRadians(this.getYRot() - 90.0f)));
        final float dz = (float) (f * Math.sin(Math.toRadians(this.getYRot() - 90.0f)));
        final Level world = this.level();
        if (world.random.nextInt(8) == 0) {
            world.addParticle(ParticleTypes.FLAME, this.getX() + dx, this.getY() + 2.0, this.getZ() + dz,
                    (double) (dx / f + (world.random.nextFloat() - world.random.nextFloat()) / 20.0f),
                    (double) ((world.random.nextFloat() - world.random.nextFloat()) / 10.0f),
                    (double) (dz / f + (world.random.nextFloat() - world.random.nextFloat()) / 20.0f));
        }
        if (world.random.nextInt(2) == 0) {
            world.addParticle(ParticleTypes.SMOKE, this.getX() + dx, this.getY() + 2.0, this.getZ() + dz,
                    (double) (dx / f + (world.random.nextFloat() - world.random.nextFloat()) / 20.0f),
                    (double) ((world.random.nextFloat() - world.random.nextFloat()) / 10.0f),
                    (double) (dz / f + (world.random.nextFloat() - world.random.nextFloat()) / 20.0f));
        }
        if (world.random.nextInt(10) == 0) {
            // "fireworksSpark" = firework.
            world.addParticle(ParticleTypes.FIREWORK, this.getX() + dx, this.getY() + 2.0, this.getZ() + dz,
                    (double) (dx / f + (world.random.nextFloat() - world.random.nextFloat()) / 20.0f),
                    (double) ((world.random.nextFloat() - world.random.nextFloat()) / 5.0f),
                    (double) (dz / f + (world.random.nextFloat() - world.random.nextFloat()) / 20.0f));
        }
    }

    /**
     * {@code onLivingUpdate} (:658-885). While nobody rides, {@code super} first (AI and the vanilla move); then the
     * clamps, the ride sound and the side split. See the class comment for the rider input.
     */
    @Override
    public void aiStep() {
        @SuppressWarnings("unused")
        final List<Entity> list = null;
        final Vec3 start = this.getDeltaMovement();
        final double velocity = Math.sqrt(start.x * start.x + start.z * start.z);
        // d6/d7 (:661-662) are never read; the entity random is drawn as in the original.
        @SuppressWarnings("unused")
        final double d6 = this.random.nextFloat() * 2.0f - 1.0f;
        @SuppressWarnings("unused")
        final double d7 = (this.random.nextInt(2) * 2 - 1) * 0.7;
        double obstruction_factor = 0.0;
        double relative_g = 0.0;
        double max_speed = 0.45;
        double gh = 1.55;
        int dist = 2;
        if (this.isRemoved()) {
            return;
        }
        if (this.getFirstPassenger() == null) {
            super.aiStep();
        }
        final Vec3 motion = this.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
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
        final Entity rider = this.getFirstPassenger();
        if (rider != null && this.playing == 0 && this.level().random.nextInt(80) == 1) {
            // "orespawn:robotspider": category "master" in the 20.3 jar's sounds.json.
            this.playSoundAtEntity(ModSounds.ROBOTSPIDER.get(), 0.45f, 1.0f);
            this.playing = 125;
        }
        // posY is a local, as in Elevator: the server's field writes (:753, :766, :791) were overwritten by moveEntity
        // from the bounding box and only moved the block lookups of the same tick; on the client setPosition reads it.
        double posY = this.getY();
        if (this.level().isClientSide) {
            if (rider == null) {
                BlockState bid = this.level().getBlockState(legacyPos(this.getX(), Mth.floor((float) posY - (float) gh + 1.0f), this.getZ()));
                if (bid.isAir()) {
                    bid = this.level().getBlockState(legacyPos(this.getX(), Mth.floor((float) posY - (float) gh), this.getZ()));
                }
                if (isFloor(bid)) {
                    motionY += 0.12;
                    posY += 0.12;
                    this.boatY += 0.12;
                } else {
                    motionY -= 0.002;
                }
            }
            // else if (riddenByEntity instanceof EntityPlayer) { EntityClientPlayerMP look + input packets } (:718-722):
            // PORT: LocalPlayer sends both itself while riding (class comment); nothing to do.
            if (this.boatPosRotationIncrements > 0) {
                final double d8 = this.getX() + (this.boatX - this.getX()) / this.boatPosRotationIncrements;
                final double d9 = posY + (this.boatY - posY) / this.boatPosRotationIncrements;
                final double d10 = this.getZ() + (this.boatZ - this.getZ()) / this.boatPosRotationIncrements;
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
                final double d8 = this.getX() + motionX;
                final double d9 = posY + motionY;
                final double d10 = this.getZ() + motionZ;
                this.setPos(d8, d9, d10);
                motionX *= 0.99;
                motionY *= 0.95;
                motionZ *= 0.99;
            }
            this.setDeltaMovement(motionX, motionY, motionZ);
            this.updateLegs();
            return;
        }

        // ---- Server (:747-884) ----
        if (rider != null) {
            gh = 4.25;
            final BlockState bid = this.level().getBlockState(legacyPos(this.getX(), Mth.floor((float) posY - (float) gh), this.getZ()));
            if (isFloor(bid)) {
                motionY += 0.06;
                posY += 0.03;
            } else {
                motionY -= 0.02;
            }
        } else {
            BlockState bid = this.level().getBlockState(legacyPos(this.getX(), Mth.floor((float) posY - (float) gh + 1.0f), this.getZ()));
            if (bid.isAir()) {
                bid = this.level().getBlockState(legacyPos(this.getX(), Mth.floor((float) posY - (float) gh), this.getZ()));
            }
            if (isFloor(bid)) {
                motionY += 0.15;
                posY += 0.15;
                this.boatY += 0.15;
            } else {
                motionY -= 0.002;
            }
        }
        if (rider != null && rider instanceof Player) {
            final Player pp2 = (Player) rider;
            obstruction_factor = 0.0;
            dist = 3;
            dist += (int) (velocity * 6.0);
            for (int k = 1; k < dist; ++k) {
                for (int i = 1; i < dist * 3; ++i) {
                    for (int j = -90; j <= 90; j += 30) {
                        final double dx = i * Math.cos(Math.toRadians(this.getYRot() + 90.0f + j));
                        final double dz = i * Math.sin(Math.toRadians(this.getYRot() + 90.0f + j));
                        // (int)(posX + dx), (int) posY - k, (int)(posZ + dz): floor (R20).
                        final BlockState bid = this.level().getBlockState(legacyPos(this.getX() + dx, Mth.floor(posY) - k, this.getZ() + dz));
                        if (isFloor(bid)) {
                            obstruction_factor += 0.03;
                        }
                    }
                }
            }
            motionY += obstruction_factor * 0.05;
            posY += obstruction_factor * 0.05;
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
            relative_g = Math.abs(relative_g) * velocity;
            if (relative_g > 50.0) {
                relative_g = 0.0;
            }
            this.setXRot(0.0f);
            this.setRot(this.getYRot(), this.getXRot());
            double newvelocity = Math.sqrt(motionX * motionX + motionZ * motionZ);
            // rr = atan2(rider.motionZ, rider.motionX) (:821) and rt = 0 (:824) are never read.
            final double rhm = Math.atan2(motionZ, motionX);
            final double rhdir = Math.toRadians((rider.getYRot() + 90.0f) % 360.0f);
            final double pi = 3.1415926545;
            double deltav = 0.0;
            final float im = pp2.zza; // moveForward, set by ServerPlayer.setPlayerInput
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
            // First moveEntity (:872); 1.21.1's move zeroes the blocked motion components like 1.7.10's did.
            this.setDeltaMovement(motionX, motionY, motionZ);
            this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
            final Vec3 moved = this.getDeltaMovement();
            motionX = moved.x * 0.98;
            motionY = moved.y * 0.98;
            motionZ = moved.z * 0.98;
        }
        // Second moveEntity (:877), for every rider and for none.
        this.setDeltaMovement(motionX, motionY, motionZ);
        this.move(MoverType.SELF, new Vec3(motionX, motionY, motionZ));
        final Vec3 moved = this.getDeltaMovement();
        this.setDeltaMovement(moved.x * 0.8, moved.y * 0.98, moved.z * 0.8);
        if (rider != null && rider.isRemoved()) {
            rider.stopRiding();
        }
    }

    /** {@code goThisWay} (:887-890): the Spider Driver's steering. */
    public void goThisWay(final double mx, final double mz) {
        final Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(mx, m.y, mz);
    }

    // isAIEnabled (:892-894) = riddenByEntity == null: see aiStep.

    /** {@code writeEntityToNBT} (:896-897): empty, without {@code super}. */
    @Override
    public void addAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
    }

    /** {@code readEntityFromNBT} (:899-900): empty, without {@code super}. */
    @Override
    public void readAdditionalSaveData(final CompoundTag par1NBTTagCompound) {
    }

    // getShadowSize (:902-904) = 0.95: the 1.7.10 Render drew the shadow from its own shadowSize (0.99, the renderer).

    /**
     * {@code interact} (:906-939). An iron ingot within 5 blocks repairs up to 100 missing health (original units, R4;
     * {@code CombatEvents.onHeal} scales the heal), one ingot outside creative. A robot ridden by another player
     * refuses; an empty one within 4 blocks takes the player. Always "handled".
     *
     * <p>PORT: {@code Mob.interact} is final in 1.21.1 and applies a held name tag or lead before this method runs
     * (Elevator precedent, R18 case 3).
     */
    @Override
    protected InteractionResult mobInteract(final Player par1EntityPlayer, final InteractionHand hand) {
        // inventory.getCurrentItem() (:907); the stackSize <= 0 cleanup (:908-911) has no 1.21.1 case.
        final ItemStack var2 = par1EntityPlayer.getInventory().getSelected();
        final boolean clientSide = this.level().isClientSide;
        if (!var2.isEmpty() && var2.is(Items.IRON_INGOT) && par1EntityPlayer.distanceToSqr(this) < 25.0) {
            if (!clientSide) {
                float f = (float) this.getOriginalMaxHealth() - VirtualHealth.originalHealth(this);
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
            this.playSoundAtEntity(ModSounds.ROBOTSPIDERMOUNT.get(), 0.65f, 1.0f);
        }
        return InteractionResult.sidedSuccess(clientSide);
    }

    /** {@code feetFindSomethingToHit} (:941-956): every suitable living entity in the box is stomped. */
    private void feetFindSomethingToHit() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0, 8.0, 20.0));
        for (final LivingEntity var8 : var5) {
            if (this.feetisSuitableTarget(var8, false)) {
                this.feetattackEntityAsMob(var8);
            }
        }
    }

    /**
     * {@code feetisSuitableTarget} (:958-998): alive, no spider of any kind, not the rider, 12..18 blocks away, no
     * creative player. {@code EntitySpider}/{@code EntityCaveSpider} are {@link Spider}/{@link CaveSpider}; the Spider
     * Driver is a {@code Spider} as well.
     */
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
        if (par1EntityLiving instanceof SpiderRobot) {
            return false;
        }
        if (par1EntityLiving instanceof Spider) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderDriver) {
            return false;
        }
        if (par1EntityLiving instanceof CaveSpider) {
            return false;
        }
        if (par1EntityLiving == this.getFirstPassenger()) {
            return false;
        }
        final float d1 = (float) (par1EntityLiving.getX() - this.getX());
        final float d2 = (float) (par1EntityLiving.getY() - this.getY());
        final float d3 = (float) (par1EntityLiving.getZ() - this.getZ());
        final float dd = (float) Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
        if (dd > 18.0f) {
            return false;
        }
        if (dd < 12.0f) {
            return false;
        }
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return true;
    }

    /** {@code feetattackEntityAsMob} (:1000-1015): {@code attack / 10}, knockback 0.6 and 0.1 up (doubled for players). */
    public boolean feetattackEntityAsMob(@Nullable final Entity par1Entity) {
        boolean ret = false;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final double ks = 0.6;
            double inair = 0.1;
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            ret = par1Entity.hurt(this.damageSources().mobAttack(this), MobStats.SpiderRobot_stats().attack() / 10.0f);
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            if (ret) {
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
        }
        return ret;
    }

    /** {@code findSomethingToAttack} (:1017-1033): the first suitable entity, unsorted. */
    @Nullable
    private LivingEntity findSomethingToAttack() {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(20.0, 12.0, 20.0));
        for (final LivingEntity var8 : var5) {
            if (this.isSuitableTarget(var8, false)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * {@code isSuitableTarget} (:1035-1086): alive, no spider, not the rider, not ignorable, visible; within six blocks
     * anything (creative players included), further out only inside 0.75 rad of the head and no creative player.
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
        if (par1EntityLiving instanceof SpiderRobot) {
            return false;
        }
        if (par1EntityLiving instanceof Spider) {
            return false;
        }
        if (par1EntityLiving instanceof SpiderDriver) {
            return false;
        }
        if (par1EntityLiving instanceof CaveSpider) {
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
        if (par1EntityLiving instanceof Player p) {
            return !p.getAbilities().instabuild;
        }
        return true;
    }

    /**
     * {@code attackEntityAsMob} (:1088-1103): the full attack, knockback 1.2 and 0.15 up (doubled for players). No
     * vanilla melee underneath - the original did not call {@code super}.
     */
    @Override
    public boolean doHurtTarget(final Entity par1Entity) {
        boolean ret = false;
        if (par1Entity != null && par1Entity instanceof LivingEntity) {
            final double ks = 1.2;
            double inair = 0.15;
            final float f3 = (float) Math.atan2(par1Entity.getZ() - this.getZ(), par1Entity.getX() - this.getX());
            ret = par1Entity.hurt(this.damageSources().mobAttack(this), (float) MobStats.SpiderRobot_stats().attack());
            if (par1Entity.isRemoved() || par1Entity instanceof Player) {
                inair *= 2.0;
            }
            if (ret) {
                par1Entity.push(Math.cos(f3) * ks, inair, Math.sin(f3) * ks);
            }
        }
        return ret;
    }

    /** {@code getAttacking} (:1105-1107). */
    public int getAttacking() {
        return this.entityData.get(DATA_ATTACKING);
    }

    /** {@code setAttacking} (:1109-1111). */
    public void setAttacking(final int par1) {
        this.entityData.set(DATA_ATTACKING, par1);
    }

    // getDropItem (:1113-1115) returns null: no EntityLiving drop.

    /** 1.7.10 {@code onDeath}: {@code dropFewItems}, then the equipment ({@code super}). R10: Java drops. */
    @Override
    protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource damageSource, final boolean recentlyHit) {
        this.dropFewItems(recentlyHit, 0);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    /**
     * {@code dropItemRand} (:1117-1125): an item entity one block up, x and z shifted by {@code OreSpawnRand} -1..+1,
     * spawned directly (no pickup delay), as {@code RobotSupport.dropItemRand} of W07.
     */
    private ItemStack dropItemRand(final ItemLike index, final int par1) {
        final ItemStack is = new ItemStack(index, par1);
        final ItemEntity var3 = new ItemEntity(this.level(), this.getX() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2),
                this.getY() + 1.0, this.getZ() + OreSpawn.OreSpawnRand.nextInt(2) - OreSpawn.OreSpawnRand.nextInt(2), is);
        this.level().addFreshEntity(var3);
        return is;
    }

    /**
     * {@code dropFewItems} (:1127-1174): 14-27 rolls of {@code rand(15)}: redstone, repeater, comparator, redstone block
     * (3 and 8), dispenser, sticky piston, piston, lever, light weighted pressure plate; 10-14 nothing. Looting ignored.
     */
    protected void dropFewItems(final boolean par1, final int par2) {
        @SuppressWarnings("unused")
        ItemStack is = null;
        for (int i = 14 + this.level().random.nextInt(14), var4 = 0; var4 < i; ++var4) {
            final int var5 = this.level().random.nextInt(15);
            switch (var5) {
                case 0 -> is = this.dropItemRand(Items.REDSTONE, 1);
                case 1 -> is = this.dropItemRand(Items.REPEATER, 1);
                case 2 -> is = this.dropItemRand(Items.COMPARATOR, 1);
                case 3 -> is = this.dropItemRand(Blocks.REDSTONE_BLOCK, 1);
                case 4 -> is = this.dropItemRand(Blocks.DISPENSER, 1);
                case 5 -> is = this.dropItemRand(Blocks.STICKY_PISTON, 1);
                case 6 -> is = this.dropItemRand(Blocks.PISTON, 1);
                case 7 -> is = this.dropItemRand(Blocks.LEVER, 1);
                case 8 -> is = this.dropItemRand(Blocks.REDSTONE_BLOCK, 1);
                case 9 -> is = this.dropItemRand(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, 1);
                default -> {
                }
            }
        }
    }

    /**
     * {@code World.playSoundAtEntity(this, name, volume, pitch)}: the server broadcast to players near the entity; the
     * 1.7.10 client call did nothing. {@code Level.playSound(null, ...)} behaves the same on both sides. Both robot
     * sounds are category "master" in the 20.3 jar's sounds.json.
     */
    private void playSoundAtEntity(final net.minecraft.sounds.SoundEvent sound, final float volume, final float pitch) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, SoundSource.MASTER, volume, pitch);
    }

    private boolean mobGriefing() {
        return this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    /**
     * {@code bid != air && bid != water && bid != flowing_water && bid != lava && bid != flowing_lava}: 1.21.1 has one
     * block each for still and flowing water and lava, and several air blocks.
     */
    private static boolean isFloor(final BlockState bid) {
        return !bid.isAir() && !bid.is(Blocks.WATER) && !bid.is(Blocks.LAVA);
    }

    /** {@code (int) x, y, (int) z} of the world lookups; x and z through {@link Mth#floor} (R20). */
    private static BlockPos legacyPos(final double x, final int y, final double z) {
        return new BlockPos(Mth.floor(x), y, Mth.floor(z));
    }
}
