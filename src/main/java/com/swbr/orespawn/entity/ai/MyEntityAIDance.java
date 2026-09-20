package com.swbr.orespawn.entity.ai;

import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.registry.ModBlocks;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Port of {@code danger.orespawn.MyEntityAIDance}: the Girlfriend's night dance on gem and metal
 * blocks, synchronised between neighbouring Girlfriends by the lowest entity id
 * (verhalten/core-02.md).
 *
 * <p>No mutex (the original called no {@code setMutexBits}), so the goal runs beside everything;
 * Girlfriend registers it at priority 3 and keeps it in a public field {@code Dance}, whose
 * {@link #is_dancing} silences the idle sounds (Girlfriend.java:130-131, :863-865).
 *
 * <p>The public fields keep their original names because Girlfriend and the neighbour sync read
 * them directly.
 */
public class MyEntityAIDance extends Goal {

    private final Girlfriend thePet;
    Level theWorld;
    public int ticker;
    public int dance_move;
    public int is_dancing;

    /** MyEntityAIDance.java:19-25. */
    public MyEntityAIDance(final Girlfriend par1EntityTameable) {
        this.ticker = 0;
        this.dance_move = 0;
        this.is_dancing = 0;
        this.thePet = par1EntityTameable;
        this.theWorld = par1EntityTameable.level();
        this.setFlags(EnumSet.noneOf(Goal.Flag.class));
    }

    /**
     * MyEntityAIDance.java:27-29: gold, diamond, emerald, ruby, amethyst, titanium, uranium blocks.
     */
    public boolean is_dance_block(final Block bid) {
        return bid == Blocks.GOLD_BLOCK || bid == Blocks.DIAMOND_BLOCK || bid == Blocks.EMERALD_BLOCK
                || bid == ModBlocks.BLOCKRUBY.get() || bid == ModBlocks.BLOCKAMETHYST.get()
                || bid == ModBlocks.BLOCKTITANIUM.get() || bid == ModBlocks.BLOCKURANIUM.get();
    }

    /**
     * The block one below the pet at the integer offset {@code (i, j)} (:45, :70, :103). PORT: the
     * original's {@code (int) posX/posY/posZ} casts are {@code Mth.floor} (DECISIONS R20): truncation
     * read the neighbouring block in the negative half of the world.
     */
    private Block blockBelow(final int i, final int j) {
        return this.theWorld.getBlockState(new BlockPos(Mth.floor(this.thePet.getX()) + i, Mth.floor(this.thePet.getY()) - 1,
                Mth.floor(this.thePet.getZ()) + j)).getBlock();
    }

    /**
     * MyEntityAIDance.java:31-54: not sitting; {@code worldTime % 24000} in [14000, 22000]; at least
     * one dance block in the 7x7 scan one block below.
     */
    @Override
    public boolean canUse() {
        // PORT: 1.7.10 asked every 3 ticks, 1.21.1 every 2; no chance rolled, not compensated.
        // EntityTameable.isSitting() read the synced DataWatcher flag; that is isInSittingPose().
        if (this.thePet.isInSittingPose()) {
            return false;
        }
        long t = this.theWorld.getDayTime();
        t %= 24000L;
        if (t < 14000L || t > 22000L) {
            return false;
        }
        int ic;
        int ix;
        int iz = ix = (ic = 0);
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                final Block bid = this.blockBelow(i, j);
                if (this.is_dance_block(bid)) {
                    ++ic;
                    ix += i;
                    iz += j;
                }
            }
        }
        return ic != 0;
    }

    /**
     * MyEntityAIDance.java:56-91: the same checks, then walk to the mean offset of the dance blocks
     * (fewer than 40) or, with one in three, to the own position; sets {@link #is_dancing}. Side
     * effects on every call, as in the original.
     */
    @Override
    public boolean canContinueToUse() {
        // PORT: 1.7.10 ran this every tick, 1.21.1 every second tick (Mob.serverAiStep). The
        // re-path and the one-in-three stop shuffle therefore happen half as often; not compensated
        // (same class of deviation as the wander goals of W01).
        if (this.thePet.isInSittingPose()) {
            return false;
        }
        long t = this.theWorld.getDayTime();
        t %= 24000L;
        if (t < 14000L || t > 22000L) {
            return false;
        }
        int ic;
        int ix;
        int iz = ix = (ic = 0);
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                final Block bid = this.blockBelow(i, j);
                if (this.is_dance_block(bid)) {
                    ++ic;
                    ix += i;
                    iz += j;
                }
            }
        }
        if (ic == 0) {
            return false;
        }
        ix /= ic;
        iz /= ic;
        // :84, :87 (int) casts -> Mth.floor (DECISIONS R20); moveTo turns the target into a BlockPos.
        if (ic < 40) {
            this.thePet.getNavigation().moveTo((double) (Mth.floor(this.thePet.getX()) + ix), (double) Mth.floor(this.thePet.getY()),
                    (double) (Mth.floor(this.thePet.getZ()) + iz), 1.0);
        } else if (this.theWorld.random.nextInt(3) == 1) {
            this.thePet.getNavigation().moveTo((double) Mth.floor(this.thePet.getX()), (double) Mth.floor(this.thePet.getY()),
                    (double) Mth.floor(this.thePet.getZ()), 1.0);
        }
        this.is_dancing = 1;
        return true;
    }

    /** MyEntityAIDance.java:93-118. */
    @Override
    public void start() {
        this.setSneaking(false);
        this.ticker = 0;
        this.dance_move = 0;
        this.is_dancing = 1;
        int ic;
        int ix;
        int iz = ix = (ic = 0);
        for (int i = -3; i < 4; ++i) {
            for (int j = -3; j < 4; ++j) {
                final Block bid = this.blockBelow(i, j);
                if (this.is_dance_block(bid)) {
                    ++ic;
                    ix += i;
                    iz += j;
                }
            }
        }
        if (ic > 0) {
            ix /= ic;
            iz /= ic;
            if (ic < 40) { // :115 (int) casts -> Mth.floor (DECISIONS R20)
                this.thePet.getNavigation().moveTo((double) (Mth.floor(this.thePet.getX()) + ix), (double) Mth.floor(this.thePet.getY()),
                        (double) (Mth.floor(this.thePet.getZ()) + iz), 1.0);
            }
        }
    }

    /** MyEntityAIDance.java:120-125. */
    @Override
    public void stop() {
        this.setSneaking(false);
        this.ticker = 0;
        this.dance_move = 0;
        this.is_dancing = 0;
    }

    /** The original's updateTask ran every tick; {@link #ticker} counts ticks of a 20-tick cycle. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * MyEntityAIDance.java:127-286. Constants {@code cycle = 20}, {@code halfc = 10},
     * {@code mover = 160}. First the sync with Girlfriends in the +-4/+-3/+-4 box: a dancing one with
     * a smaller id lends its {@code ticker} and {@code dance_move}. Then the ten figures, each ending
     * after {@code ticker > 160}.
     */
    @Override
    public void tick() {
        final int cycle = 20;
        final int halfc = cycle / 2;
        final int mover = cycle * 8;
        int tempid = this.thePet.getId();
        final AABB bb = new AABB(this.thePet.getX() - 4.0, this.thePet.getY() - 3.0, this.thePet.getZ() - 4.0,
                this.thePet.getX() + 4.0, this.thePet.getY() + 3.0, this.thePet.getZ() + 4.0);
        final List<Girlfriend> var5 = this.theWorld.getEntitiesOfClass(Girlfriend.class, bb);
        for (final Girlfriend var7 : var5) {
            if (var7.getId() < tempid) {
                if (var7.Dance.is_dancing == 1) {
                    this.ticker = var7.Dance.ticker;
                    this.dance_move = var7.Dance.dance_move;
                }
                tempid = var7.getId();
            }
        }
        ++this.ticker;
        if (this.dance_move == 0) {
            this.dance_move = 1 + this.theWorld.random.nextInt(10);
            final Vec3 dm = this.thePet.getDeltaMovement();
            this.thePet.setDeltaMovement(0.0, dm.y, 0.0);
            this.ticker = 0;
            this.setSneaking(false);
        }
        switch (this.dance_move) {
            case 1: {
                this.move_it(this.thePet, this.ticker, cycle, 0);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 2: {
                this.move_it(this.thePet, this.ticker, cycle, 1);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 3: {
                if (this.ticker % cycle < halfc) {
                    this.setSneaking(false);
                } else {
                    this.setSneaking(true);
                }
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 4: {
                if (this.ticker % halfc == 1) {
                    this.thePet.swing(InteractionHand.MAIN_HAND);
                    this.setMotionY(0.25);
                }
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 5: {
                if (this.ticker % halfc == 1) {
                    this.thePet.swing(InteractionHand.MAIN_HAND);
                }
                this.move_it(this.thePet, this.ticker, cycle, 0);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 6: {
                if (this.ticker % halfc == 1) {
                    this.thePet.swing(InteractionHand.MAIN_HAND);
                }
                this.move_it(this.thePet, this.ticker, cycle, 1);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 7: {
                if (this.ticker % cycle < halfc) {
                    this.setSneaking(false);
                } else {
                    this.setSneaking(true);
                }
                this.move_it(this.thePet, this.ticker, cycle, 0);
                this.move_it(this.thePet, this.ticker, cycle, 2);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 8: {
                if (this.ticker % cycle < halfc) {
                    this.setSneaking(false);
                } else {
                    this.setSneaking(true);
                }
                this.move_it(this.thePet, this.ticker, cycle, 1);
                this.move_it(this.thePet, this.ticker, cycle, 2);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 9: {
                if (this.ticker % cycle < halfc) {
                    this.setSneaking(false);
                } else {
                    this.setSneaking(true);
                }
                if (this.ticker % halfc == 1) {
                    this.thePet.swing(InteractionHand.MAIN_HAND);
                }
                this.move_it(this.thePet, this.ticker, cycle, 0);
                this.move_it(this.thePet, this.ticker, cycle, 3);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            case 10: {
                if (this.ticker % cycle < halfc) {
                    this.setSneaking(false);
                    this.setMotionY(0.25);
                } else {
                    this.setSneaking(true);
                }
                if (this.ticker % halfc == 1) {
                    this.thePet.swing(InteractionHand.MAIN_HAND);
                }
                this.move_it(this.thePet, this.ticker, cycle, 1);
                this.move_it(this.thePet, this.ticker, cycle, 3);
                if (this.ticker > mover) {
                    this.dance_move = 0;
                    break;
                }
                break;
            }
            default: {
                this.dance_move = 0;
                break;
            }
        }
    }

    /**
     * MyEntityAIDance.java:288-339: dir 0 pushes x, 1 pushes z (0.02 each), 2 turns the body, 3 the
     * head (10 degrees each). Signs flip for the second half of the 20-tick cycle, the yaw signs
     * flip again in the second half of each 10-tick half.
     */
    private void move_it(final TamableAnimal et, int t, final int cycle, final int dir) {
        float dirx = 0.0f;
        float dirz = 0.0f;
        float dirYaw = 0.0f;
        float dirYawH = 0.0f;
        switch (dir) {
            case 0: {
                dirx = 0.02f;
                dirz = 0.0f;
                dirYaw = 0.0f;
                dirYawH = 0.0f;
                break;
            }
            case 1: {
                dirx = 0.0f;
                dirz = 0.02f;
                dirYaw = 0.0f;
                dirYawH = 0.0f;
                break;
            }
            case 2: {
                dirx = 0.0f;
                dirz = 0.0f;
                dirYaw = 10.0f;
                dirYawH = 0.0f;
                break;
            }
            case 3: {
                dirx = 0.0f;
                dirz = 0.0f;
                dirYaw = 0.0f;
                dirYawH = 10.0f;
                break;
            }
        }
        t %= cycle;
        if (t >= cycle / 2) {
            dirx = -dirx;
            dirz = -dirz;
            dirYaw = -dirYaw;
            dirYawH = -dirYawH;
        }
        t %= cycle / 2;
        if (t >= cycle / 4) {
            dirYaw = -dirYaw;
            dirYawH = -dirYawH;
        }
        // PORT: motionX/motionZ/rotationYaw/rotationYawHead are the delta movement, yRot and
        // yHeadRot. BodyRotationControl and LookControl still rewrite yRot/yHeadRot every tick in
        // 1.21.1, so the turn may be invisible (catalogue risk note); the numbers are the original's.
        final Vec3 dm = et.getDeltaMovement();
        et.setDeltaMovement(dm.x + dirx, dm.y, dm.z + dirz);
        et.setYRot(et.getYRot() + dirYaw);
        et.yHeadRot += dirYawH;
    }

    /** {@code motionY = 0.25}. */
    private void setMotionY(final double y) {
        final Vec3 dm = this.thePet.getDeltaMovement();
        this.thePet.setDeltaMovement(dm.x, y, dm.z);
    }

    /**
     * PORT: 1.7.10 {@code setSneaking} set shared flag 1, which the biped model read for the crouch
     * pose. 1.21.1 keeps the flag ({@code setShiftKeyDown}) but the humanoid crouch rendering hangs
     * on {@code Pose.CROUCHING} ({@code Entity.isCrouching()}, HumanoidModel), and nothing sets a
     * mob's pose from the flag - so both are written. A mob's dimensions do not depend on its pose.
     * The pose is only swapped between STANDING and CROUCHING: 1.7.10 had no pose to clobber, and
     * writing STANDING over e.g. {@code Pose.DYING} would be a new effect.
     */
    private void setSneaking(final boolean sneak) {
        this.thePet.setShiftKeyDown(sneak);
        final Pose pose = this.thePet.getPose();
        if (pose == Pose.STANDING || pose == Pose.CROUCHING) {
            this.thePet.setPose(sneak ? Pose.CROUCHING : Pose.STANDING);
        }
    }
}
