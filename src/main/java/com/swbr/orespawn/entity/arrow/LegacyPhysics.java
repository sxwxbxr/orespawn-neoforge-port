package com.swbr.orespawn.entity.arrow;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The four 1.7.10 vanilla helpers that {@code UltimateArrow}, {@code IrukandjiArrow} and
 * {@code UltimateFishHook} call from their copied physics, rebuilt from the 1.7.10 client bytecode
 * ({@code reference/jar/mcp/client-1.7.10.jar}, class names from {@code joined.srg}). No original
 * class of its own. The 1.21.1 counterparts differ in ways that change hits:
 *
 * <ul>
 *   <li>{@link #calculateIntercept}: {@code AxisAlignedBB.func_72327_a} ({@code azt.a(azw, azw)})
 *       tests all six face planes and keeps the nearest crossing, so a segment that <em>starts
 *       inside</em> the box still hits at its exit face. 1.21.1 {@code AABB.clip} only accepts
 *       entering faces and misses that case - point-blank shots would pass through.</li>
 *   <li>{@link #isVecInside}: {@code func_72318_a} ({@code azt.a(azw)}) is strict on all six sides;
 *       1.21.1 {@code AABB.contains} includes the minimum.</li>
 *   <li>{@link #isAABBInMaterial}: {@code World.func_72830_b} ({@code ahb.b(azt, awt)}) with the
 *       liquid surface height; there is no 1.21.1 method with this box-and-height rule.</li>
 *   <li>{@link #isMaterialInBB}: {@code World.func_72875_a} ({@code ahb.a(azt, awt)}), any block of
 *       the material in the box, no height.</li>
 * </ul>
 */
public final class LegacyPhysics {

    private LegacyPhysics() {}

    /**
     * {@code AxisAlignedBB.calculateIntercept(from, to)}: the crossing of the segment with the
     * nearest face plane that lies within the face, or {@code null}.
     */
    @Nullable
    public static Vec3 calculateIntercept(AABB box, Vec3 from, Vec3 to) {
        Vec3 v3 = intermediateWithX(from, to, box.minX);
        Vec3 v4 = intermediateWithX(from, to, box.maxX);
        Vec3 v5 = intermediateWithY(from, to, box.minY);
        Vec3 v6 = intermediateWithY(from, to, box.maxY);
        Vec3 v7 = intermediateWithZ(from, to, box.minZ);
        Vec3 v8 = intermediateWithZ(from, to, box.maxZ);
        if (!isVecInYZ(box, v3)) {
            v3 = null;
        }
        if (!isVecInYZ(box, v4)) {
            v4 = null;
        }
        if (!isVecInXZ(box, v5)) {
            v5 = null;
        }
        if (!isVecInXZ(box, v6)) {
            v6 = null;
        }
        if (!isVecInXY(box, v7)) {
            v7 = null;
        }
        if (!isVecInXY(box, v8)) {
            v8 = null;
        }
        Vec3 v9 = null;
        v9 = nearer(from, v3, v9);
        v9 = nearer(from, v4, v9);
        v9 = nearer(from, v5, v9);
        v9 = nearer(from, v6, v9);
        v9 = nearer(from, v7, v9);
        v9 = nearer(from, v8, v9);
        return v9;
    }

    /** {@code AxisAlignedBB.isVecInside}: strictly inside on every axis. */
    public static boolean isVecInside(AABB box, Vec3 vec) {
        return vec.x > box.minX && vec.x < box.maxX
                && vec.y > box.minY && vec.y < box.maxY
                && vec.z > box.minZ && vec.z < box.maxZ;
    }

    /**
     * {@code World.isAABBInMaterial(box, material)} for a liquid: every block from
     * {@code floor(min)} to {@code floor(max + 1)} (exclusive) whose liquid is of the tag counts
     * when its surface {@code y + 1 - meta/8} (full block for meta 8 and above) reaches
     * {@code box.minY}.
     *
     * <p>1.7.10 metadata to 1.21.1 amount: source meta 0 is amount 8, flowing meta m (1..7) is
     * amount {@code 8 - m}, falling (meta 8+) is amount 8 - so the surface is {@code y + amount/8}
     * in every case.
     */
    public static boolean isAABBInMaterial(Level level, AABB bb, TagKey<Fluid> fluid) {
        int i = Mth.floor(bb.minX);
        int j = Mth.floor(bb.maxX + 1.0);
        int k = Mth.floor(bb.minY);
        int l = Mth.floor(bb.maxY + 1.0);
        int i1 = Mth.floor(bb.minZ);
        int j1 = Mth.floor(bb.maxZ + 1.0);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = i; x < j; ++x) {
            for (int y = k; y < l; ++y) {
                for (int z = i1; z < j1; ++z) {
                    FluidState state = level.getFluidState(pos.set(x, y, z));
                    if (state.is(fluid)) {
                        double d0 = y + state.getAmount() / 8.0;
                        if (d0 >= bb.minY) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** {@code World.isMaterialInBB(box, material)} for a liquid: any block of it in the box. */
    public static boolean isMaterialInBB(Level level, AABB bb, TagKey<Fluid> fluid) {
        int i = Mth.floor(bb.minX);
        int j = Mth.floor(bb.maxX + 1.0);
        int k = Mth.floor(bb.minY);
        int l = Mth.floor(bb.maxY + 1.0);
        int i1 = Mth.floor(bb.minZ);
        int j1 = Mth.floor(bb.maxZ + 1.0);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = i; x < j; ++x) {
            for (int y = k; y < l; ++y) {
                for (int z = i1; z < j1; ++z) {
                    if (level.getFluidState(pos.set(x, y, z)).is(fluid)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** {@code Vec3.getIntermediateWithXValue} ({@code azw.b}): threshold {@code 1.0000000116860974E-7}. */
    @Nullable
    private static Vec3 intermediateWithX(Vec3 from, Vec3 to, double x) {
        double d1 = to.x - from.x;
        double d2 = to.y - from.y;
        double d3 = to.z - from.z;
        if (d1 * d1 < 1.0000000116860974E-7) {
            return null;
        }
        double d4 = (x - from.x) / d1;
        return d4 >= 0.0 && d4 <= 1.0 ? new Vec3(from.x + d1 * d4, from.y + d2 * d4, from.z + d3 * d4) : null;
    }

    /** {@code Vec3.getIntermediateWithYValue} ({@code azw.c}). */
    @Nullable
    private static Vec3 intermediateWithY(Vec3 from, Vec3 to, double y) {
        double d1 = to.x - from.x;
        double d2 = to.y - from.y;
        double d3 = to.z - from.z;
        if (d2 * d2 < 1.0000000116860974E-7) {
            return null;
        }
        double d4 = (y - from.y) / d2;
        return d4 >= 0.0 && d4 <= 1.0 ? new Vec3(from.x + d1 * d4, from.y + d2 * d4, from.z + d3 * d4) : null;
    }

    /** {@code Vec3.getIntermediateWithZValue} ({@code azw.d}). */
    @Nullable
    private static Vec3 intermediateWithZ(Vec3 from, Vec3 to, double z) {
        double d1 = to.x - from.x;
        double d2 = to.y - from.y;
        double d3 = to.z - from.z;
        if (d3 * d3 < 1.0000000116860974E-7) {
            return null;
        }
        double d4 = (z - from.z) / d3;
        return d4 >= 0.0 && d4 <= 1.0 ? new Vec3(from.x + d1 * d4, from.y + d2 * d4, from.z + d3 * d4) : null;
    }

    /** {@code isVecInYZ}: inclusive bounds, {@code false} for null. */
    private static boolean isVecInYZ(AABB box, @Nullable Vec3 v) {
        return v != null && v.y >= box.minY && v.y <= box.maxY && v.z >= box.minZ && v.z <= box.maxZ;
    }

    /** {@code isVecInXZ}. */
    private static boolean isVecInXZ(AABB box, @Nullable Vec3 v) {
        return v != null && v.x >= box.minX && v.x <= box.maxX && v.z >= box.minZ && v.z <= box.maxZ;
    }

    /** {@code isVecInXY}. */
    private static boolean isVecInXY(AABB box, @Nullable Vec3 v) {
        return v != null && v.x >= box.minX && v.x <= box.maxX && v.y >= box.minY && v.y <= box.maxY;
    }

    /** The selection step: {@code candidate} replaces {@code best} only when strictly nearer. */
    @Nullable
    private static Vec3 nearer(Vec3 from, @Nullable Vec3 candidate, @Nullable Vec3 best) {
        if (candidate != null && (best == null || from.distanceToSqr(candidate) < from.distanceToSqr(best))) {
            return candidate;
        }
        return best;
    }
}
