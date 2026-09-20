package com.swbr.orespawn.entity.ai;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.companion.Girlfriend;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Port of {@code danger.orespawn.MyEntityAIFollowOwner}: a copy of 1.7.10 {@code EntityAIFollowOwner}
 * that follows from half the distance underground or at night, stops on the owner's block column,
 * never follows a Girlfriend on Valentine's Day, and teleports onto the 5x5 ring around the owner
 * when no path exists at 12+ blocks (verhalten/core-02.md).
 *
 * <p>Users (speed, maxDist, minDist): Boyfriend and Girlfriend (1.4, 12, 1.5); Camarasaurus,
 * Chipmunk, GammaMetroid, Gazelle, Lizard, Ostrich, RubberDucky, WaterDragon (2.0, 10, 2.0); Dragon,
 * ThePrinceAdult, ThePrinceTeen (1.1, 12, 2.0); Leon (1.1, 16, 2.0); Spyro, Stinky, ThePrince,
 * ThePrincess (1.15, 12, 2.0); Hydrolisc (1.2, 10, 2.0); VelocityRaptor (1.5, 10, 2.0).
 *
 * <p>Vanilla {@link net.minecraft.world.entity.ai.goal.FollowOwnerGoal} has the same flags but
 * teleports through {@code TamableAnimal.shouldTryTeleportToOwner} with its own threshold and search
 * pattern. Field names: {@code field_75336_f} is the speed, {@code field_75343_h} the path
 * recalculation countdown, {@code field_75344_i} the saved avoids-water value (SRG names of
 * {@code EntityAIFollowOwner}, meaning from use).
 */
public class MyEntityAIFollowOwner extends Goal {

    private final TamableAnimal thePet;
    @Nullable
    private LivingEntity theOwner;
    Level theWorld;
    private final float speed;
    private final PathNavigation petPathfinder;
    private int recalcCountdown;
    float maxDist;
    float minDist;
    private float avoidsWater;

    /** MyEntityAIFollowOwner.java:22-30. Mutex 3 = MOVE | LOOK. */
    public MyEntityAIFollowOwner(final TamableAnimal par1EntityTameable, final float par2, final float par3, final float par4) {
        this.thePet = par1EntityTameable;
        this.theWorld = par1EntityTameable.level();
        this.speed = par2;
        this.petPathfinder = par1EntityTameable.getNavigation();
        this.minDist = par4;
        this.maxDist = par3;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /**
     * MyEntityAIFollowOwner.java:32-39: owner present; not sitting; a Girlfriend never on Valentine's
     * Day; then ((y below 60 or night) and distance squared over (maxDist/2)^2) or distance squared
     * at least maxDist^2.
     */
    @Override
    public boolean canUse() {
        // PORT: 1.7.10 asked every 3 ticks, 1.21.1 every 2; no chance is rolled here, so the
        // cadence is not compensated (W01 precedent for goals without a per-evaluation chance).
        final LivingEntity var1 = this.thePet.getOwner();
        if (var1 == null) {
            return false;
        }
        this.theOwner = var1;
        // PORT: EntityTameable.isSitting() read the synced DataWatcher flag; that is isInSittingPose().
        // World.isDaytime() was `skylightSubtracted < 4` and nothing else (bytecode of ahb.w() in the
        // 1.7.10 client jar). That is `getSkyDarken() < 4`, not Level.isDay(): isDay() also demands a
        // dimension without fixed time and would call the End night, where 1.7.10 (celestial angle
        // 0.0) called it day. skyDarken is recomputed each server tick (ServerLevel.java:361) with the
        // same rain and thunder terms. posY is the feet position in both versions.
        return !this.thePet.isInSittingPose()
                && (!(this.thePet instanceof Girlfriend) || OreSpawn.valentines_day == 0)
                && ((this.thePet != null
                        && (this.thePet.getY() < 60.0 || !(this.thePet.level().getSkyDarken() < 4))
                        && this.thePet.distanceToSqr(var1) > this.maxDist / 2.0f * (this.maxDist / 2.0f))
                    || this.thePet.distanceToSqr(var1) >= this.maxDist * this.maxDist);
    }

    /**
     * MyEntityAIFollowOwner.java:41-56: not sitting; a path exists; stop on the owner's integer x/z
     * column within the open range of +-2 in y; otherwise distance squared over minDist^2.
     */
    @Override
    public boolean canContinueToUse() {
        if (this.thePet.isInSittingPose()) {
            return false;
        }
        // PORT: 1.21.1 asks this every second tick instead of every tick; not compensated.
        if (this.petPathfinder.isDone()) {
            return false;
        }
        if (this.thePet != null && this.thePet instanceof TamableAnimal) {
            final TamableAnimal gf = this.thePet;
            final LivingEntity var1 = gf.getOwner();
            // :51 (int) posX/posY/posZ -> Mth.floor (DECISIONS R20).
            if (var1 != null
                    && Mth.floor(gf.getZ()) == Mth.floor(var1.getZ())
                    && Mth.floor(gf.getX()) == Mth.floor(var1.getX())
                    && Mth.floor(gf.getY()) < Mth.floor(var1.getY()) + 2
                    && Mth.floor(gf.getY()) > Mth.floor(var1.getY()) - 2) {
                return false;
            }
        }
        return this.thePet.distanceToSqr(this.theOwner) > this.minDist * this.minDist;
    }

    /**
     * MyEntityAIFollowOwner.java:58-62. {@code getAvoidsWater()/setAvoidsWater(false)} is the water
     * path malus: saved, set to 0, restored - the same translation vanilla FollowOwnerGoal uses.
     */
    @Override
    public void start() {
        this.recalcCountdown = 0;
        this.avoidsWater = this.thePet.getPathfindingMalus(PathType.WATER);
        this.thePet.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    /** MyEntityAIFollowOwner.java:64-68. */
    @Override
    public void stop() {
        this.theOwner = null;
        this.petPathfinder.stop();
        this.thePet.setPathfindingMalus(PathType.WATER, this.avoidsWater);
    }

    /** The original's updateTask ran every tick; the 10-tick countdown (:72-73) is per tick. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * MyEntityAIFollowOwner.java:70-89: look at the owner; every 10 ticks re-path, and if that fails
     * at distance squared >= 144 teleport to the first free spot on the 5x5 ring (inner 3x3 excluded)
     * around the owner's feet.
     */
    @Override
    public void tick() {
        this.thePet.getLookControl().setLookAt(this.theOwner, 10.0f, (float) this.thePet.getMaxHeadXRot());
        if (!this.thePet.isInSittingPose() && --this.recalcCountdown <= 0) {
            this.recalcCountdown = 10;
            if (!this.petPathfinder.moveTo(this.theOwner, (double) this.speed) && this.thePet.distanceToSqr(this.theOwner) >= 144.0) {
                final int var1 = Mth.floor(this.theOwner.getX()) - 2;
                final int var2 = Mth.floor(this.theOwner.getZ()) - 2;
                final int var3 = Mth.floor(this.theOwner.getBoundingBox().minY);
                for (int var4 = 0; var4 <= 4; ++var4) {
                    for (int var5 = 0; var5 <= 4; ++var5) {
                        if ((var4 < 1 || var5 < 1 || var4 > 3 || var5 > 3)
                                && this.hasSolidTopSurface(var1 + var4, var3 - 1, var2 + var5)
                                && !this.isNormalCube(var1 + var4, var3, var2 + var5)
                                && !this.isNormalCube(var1 + var4, var3 + 1, var2 + var5)) {
                            this.thePet.moveTo((double) (var1 + var4 + 0.5f), (double) var3, (double) (var2 + var5 + 0.5f),
                                    this.thePet.getYRot(), this.thePet.getXRot());
                            this.petPathfinder.stop();
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * PORT: 1.7.10 {@code World.doesBlockHaveSolidTopSurface} (Forge: {@code isSideSolid(UP)}) is
     * {@code BlockState.isFaceSturdy(level, pos, Direction.UP)} - full-square sturdy top face.
     */
    private boolean hasSolidTopSurface(final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        return this.theWorld.getBlockState(pos).isFaceSturdy(this.theWorld, pos, Direction.UP);
    }

    /**
     * PORT: 1.7.10 {@code Block.isNormalCube()} (opaque, renders as normal block, no power) is
     * {@code BlockState.isRedstoneConductor(level, pos)} - the Mojang name of the same predicate.
     */
    private boolean isNormalCube(final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        return this.theWorld.getBlockState(pos).isRedstoneConductor(this.theWorld, pos);
    }
}
