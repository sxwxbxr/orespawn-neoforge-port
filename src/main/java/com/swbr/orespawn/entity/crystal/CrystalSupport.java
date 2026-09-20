package com.swbr.orespawn.entity.crystal;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Pieces the three crystal mobs ({@link Rotator}, {@link Vortex}, {@link DungeonBeast}) share. No original class: each
 * method stands for a statement block the originals repeated inline.
 */
final class CrystalSupport {

    private CrystalSupport() {
    }

    /**
     * An {@code instanceof} against an OreSpawn class that a parallel porter of W08 writes ({@code Irukandji},
     * {@code Skate}, {@code Urchin}, {@code CloudShark}, {@code TerribleTerror}, {@code LurkingTerror}, {@code Mothra},
     * {@code Brutalfly}, {@code Mantis}, {@code Rat}), by registry id.
     *
     * <p>PORT: none of these classes has a subclass in 20.2 (grep {@code extends <Class>} over {@code src-20.2}: no hit),
     * so comparing the registered type is the same test, and this package compiles before the classes exist (W07
     * precedent {@code DinoSupport.isOreSpawnType}). The integrator may switch to {@code instanceof}.
     */
    static boolean isOreSpawnType(final Entity entity, final String id) {
        final ResourceLocation key = EntityType.getKey(entity.getType());
        return key.getNamespace().equals(OreSpawn.MOD_ID) && key.getPath().equals(id);
    }

    /**
     * {@code canSeeTarget} (Rotator.java:144-146, Vortex.java:123-125): no block between a point 0.75 above the feet
     * and the target. 1.7.10 {@code rayTraceBlocks(a, b, false)} hit every block with a selection box, liquids excluded -
     * the outline shape (Bee, W07).
     */
    static boolean canSeeTarget(final Mob mob, final double pX, final double pY, final double pZ) {
        return mob.level().clip(new ClipContext(new Vec3(mob.getX(), mob.getY() + 0.75, mob.getZ()), new Vec3(pX, pY, pZ),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mob)).getType() == HitResult.Type.MISS;
    }

    /** {@code worldObj.getWorldTime() % 24000L < 12000L} (Rotator.java:137-139, Vortex.java:116-118). */
    static boolean isFirstHalfOfDay(final Level level) {
        return level.getDayTime() % 24000L < 12000L;
    }

    /**
     * The flight-target pick of {@code updateAITasks} (Rotator.java:161-175, Vortex.java:143-157): up to 50 tries at
     * x/z {@code +-(base + rand(spread))} and y {@code rand(6) - 3} around the floored position; a try is kept when the
     * block is air and visible. When all 50 fail, the last try stays.
     *
     * <p>{@code (int)} casts are {@code Mth.floor} (R20). PORT: {@code isAir()} also accepts cave and void air, which
     * 1.7.10 generated as plain air (Bee, W07).
     */
    static void pickFlightTarget(final Mob mob, final BlockPos.MutableBlockPos currentFlightTarget, final int spread,
                                 final int base) {
        int xdir;
        int zdir;
        int keep_trying = 50;
        // Block bid = Blocks.stone: "not air yet".
        boolean bidIsAir = false;
        while (!bidIsAir && keep_trying != 0) {
            zdir = mob.getRandom().nextInt(spread) + base;
            xdir = mob.getRandom().nextInt(spread) + base;
            if (mob.getRandom().nextInt(2) == 0) {
                zdir = -zdir;
            }
            if (mob.getRandom().nextInt(2) == 0) {
                xdir = -xdir;
            }
            currentFlightTarget.set(Mth.floor(mob.getX()) + xdir,
                    Mth.floor(mob.getY()) + mob.getRandom().nextInt(6) - 3,
                    Mth.floor(mob.getZ()) + zdir);
            bidIsAir = mob.level().getBlockState(currentFlightTarget).isAir();
            if (bidIsAir && !canSeeTarget(mob, currentFlightTarget.getX(), currentFlightTarget.getY(), currentFlightTarget.getZ())) {
                bidIsAir = false;
            }
            --keep_trying;
        }
    }

    /**
     * The steering tail of {@code updateAITasks} (Rotator.java:188-197, Vortex.java:175-184): motion towards the centre
     * of the flight target, horizontal {@code signum * 0.4} smoothed by 0.2, vertical {@code signum * 0.7} smoothed by
     * 0.2, yaw a quarter of the way to the flight direction, {@code moveForward = 0.75} ({@code zza}).
     */
    static void steer(final Mob mob, final BlockPos currentFlightTarget) {
        final double var1 = currentFlightTarget.getX() + 0.5 - mob.getX();
        final double var2 = currentFlightTarget.getY() + 0.1 - mob.getY();
        final double var3 = currentFlightTarget.getZ() + 0.5 - mob.getZ();
        final Vec3 m = mob.getDeltaMovement();
        final double motionX = m.x + (Math.signum(var1) * 0.4 - m.x) * 0.2;
        final double motionY = m.y + (Math.signum(var2) * 0.699999988079071 - m.y) * 0.20000000149011612;
        final double motionZ = m.z + (Math.signum(var3) * 0.4 - m.z) * 0.2;
        mob.setDeltaMovement(motionX, motionY, motionZ);
        final float var4 = (float) (Math.atan2(motionZ, motionX) * 180.0 / 3.141592653589793) - 90.0f;
        final float var5 = Mth.wrapDegrees(var4 - mob.getYRot());
        mob.zza = 0.75f;
        mob.setYRot(mob.getYRot() + var5 / 4.0f);
    }
}
