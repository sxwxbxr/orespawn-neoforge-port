package com.swbr.orespawn.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 1.7.10 vanilla helpers the projectile tick needs and 1.21.1 no longer has in the same form. No
 * original OreSpawn class; each method names the vanilla code it reproduces
 * ({@code reference/jar/mcp/client-1.7.10.jar}, names from {@code joined.srg}).
 */
public final class LegacyProjectiles {

    private LegacyProjectiles() {
    }

    /**
     * {@code KingHead} or {@code QueenHead}. PORT (R25, BUGHUNT2 2.6): 1.7.10 wrote the head position straight into
     * {@code posX/Y/Z}, so the server box stayed where the head spawned and the salvos fired from the mouth flew free.
     * The port moves the box with the head ({@code setPos}), which puts the muzzle of {@code firecanon},
     * {@code firecanonl} and {@code firecanoni} (32 ahead, 14 up) inside the head box (30 ahead, 12 up, 19.9 x 10).
     * A projectile that does not skip the head hits it on its first tick - on the exit face, since
     * {@link #calculateIntercept} also reports a segment that starts inside.
     */
    public static boolean isRoyalHead(Entity entity) {
        return entity instanceof com.swbr.orespawn.entity.boss.king.KingHead
                || entity instanceof com.swbr.orespawn.entity.boss.queen.QueenHead;
    }

    /**
     * {@link #isRoyalHead} or {@code GodzillaHead}: every head whose box the port moves (R25). Mobzilla's
     * {@code firecanon} muzzle (22 ahead, 19 up) lies inside the inflated box of its head (17 ahead, 16 up, 9.9 x 10)
     * the same way.
     */
    public static boolean isRelocatedHead(Entity entity) {
        return isRoyalHead(entity) || entity instanceof com.swbr.orespawn.entity.boss.mobzilla.GodzillaHead;
    }

    /**
     * {@code World.rayTraceBlocks(start, end, false)}: selection boxes, liquids ignored, null on a
     * miss. {@code OUTLINE} is the 1.21.1 counterpart of the 1.7.10 selection box, which stopped
     * throwables on tall grass, torches and portal blocks; {@code COLLIDER} would fly through them.
     */
    @Nullable
    public static BlockHitResult rayTraceBlocks(Entity entity, Vec3 from, Vec3 to) {
        BlockHitResult result = entity.level().clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
        return result.getType() == HitResult.Type.MISS ? null : result;
    }

    /**
     * {@code AxisAlignedBB.calculateIntercept} ({@code azt.a(azw, azw)}): the nearest crossing of the
     * segment with any of the six face planes that lies on the face. Unlike 1.21.1's
     * {@code AABB.clip}, which only tests the faces a ray enters, a segment that starts inside the
     * box still hits where it leaves - that is how a 1.7.10 projectile hit a target it already
     * overlapped. Returns only the point; no caller of this package reads the side.
     */
    @Nullable
    public static Vec3 calculateIntercept(AABB box, Vec3 start, Vec3 end) {
        Vec3 minX = intermediateWithX(start, end, box.minX);
        Vec3 maxX = intermediateWithX(start, end, box.maxX);
        Vec3 minY = intermediateWithY(start, end, box.minY);
        Vec3 maxY = intermediateWithY(start, end, box.maxY);
        Vec3 minZ = intermediateWithZ(start, end, box.minZ);
        Vec3 maxZ = intermediateWithZ(start, end, box.maxZ);
        if (!isVecInYZ(box, minX)) {
            minX = null;
        }
        if (!isVecInYZ(box, maxX)) {
            maxX = null;
        }
        if (!isVecInXZ(box, minY)) {
            minY = null;
        }
        if (!isVecInXZ(box, maxY)) {
            maxY = null;
        }
        if (!isVecInXY(box, minZ)) {
            minZ = null;
        }
        if (!isVecInXY(box, maxZ)) {
            maxZ = null;
        }
        Vec3 best = null;
        best = nearer(start, minX, best);
        best = nearer(start, maxX, best);
        best = nearer(start, minY, best);
        best = nearer(start, maxY, best);
        best = nearer(start, minZ, best);
        best = nearer(start, maxZ, best);
        return best;
    }

    /** {@code if (v != null && (best == null || start.squareDistanceTo(v) < start.squareDistanceTo(best))) best = v}. */
    @Nullable
    private static Vec3 nearer(Vec3 start, @Nullable Vec3 candidate, @Nullable Vec3 best) {
        if (candidate != null && (best == null || start.distanceToSqr(candidate) < start.distanceToSqr(best))) {
            return candidate;
        }
        return best;
    }

    /** {@code Vec3.getIntermediateWithXValue} ({@code azw.b(azw, double)}). */
    @Nullable
    private static Vec3 intermediateWithX(Vec3 from, Vec3 to, double x) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        if (dx * dx < 1.0000000116860974E-7D) {
            return null;
        }
        double t = (x - from.x) / dx;
        return t >= 0.0D && t <= 1.0D ? new Vec3(from.x + dx * t, from.y + dy * t, from.z + dz * t) : null;
    }

    /** {@code Vec3.getIntermediateWithYValue} ({@code azw.c(azw, double)}). */
    @Nullable
    private static Vec3 intermediateWithY(Vec3 from, Vec3 to, double y) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        if (dy * dy < 1.0000000116860974E-7D) {
            return null;
        }
        double t = (y - from.y) / dy;
        return t >= 0.0D && t <= 1.0D ? new Vec3(from.x + dx * t, from.y + dy * t, from.z + dz * t) : null;
    }

    /** {@code Vec3.getIntermediateWithZValue} ({@code azw.d(azw, double)}). */
    @Nullable
    private static Vec3 intermediateWithZ(Vec3 from, Vec3 to, double z) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        if (dz * dz < 1.0000000116860974E-7D) {
            return null;
        }
        double t = (z - from.z) / dz;
        return t >= 0.0D && t <= 1.0D ? new Vec3(from.x + dx * t, from.y + dy * t, from.z + dz * t) : null;
    }

    private static boolean isVecInYZ(AABB box, @Nullable Vec3 v) {
        return v != null && v.y >= box.minY && v.y <= box.maxY && v.z >= box.minZ && v.z <= box.maxZ;
    }

    private static boolean isVecInXZ(AABB box, @Nullable Vec3 v) {
        return v != null && v.x >= box.minX && v.x <= box.maxX && v.z >= box.minZ && v.z <= box.maxZ;
    }

    private static boolean isVecInXY(AABB box, @Nullable Vec3 v) {
        return v != null && v.x >= box.minX && v.x <= box.maxX && v.y >= box.minY && v.y <= box.maxY;
    }

    /**
     * {@code World.createExplosion(this, x, y, z, strength, mobGriefing)} for an exploder that is neither
     * living nor primed TNT (LaserBall.java:173, ThunderBolt.java:45). 1.7.10
     * {@code Explosion.getExplosivePlacedBy} ({@code agw.c}) returned null for such an exploder, so the blast
     * was a plain unattributed {@code "explosion"}: no kill credit or XP for the thrower, no retaliation
     * against him, and TNT it lit had no placer.
     *
     * <p>PORT: 1.21.1 takes the indirect source of a projectile exploder from its owner
     * ({@code Explosion.getIndirectSourceEntityInternal}); passing {@code this} would turn the blast into
     * {@code player_explosion} credited to the thrower, set {@code lastHurtByMob}, own the TNT it lights and
     * set the player flag of block drops. The exploder is therefore passed as null. The two other things the
     * 1.7.10 exploder did change nothing here: its block-resistance hooks were the {@code Entity} defaults,
     * which the null-source calculator uses as well, and it was left out of its own blast - every caller
     * discards the projectile in the same impact, and {@code Entity.hurt} on a projectile only marks it.
     * {@code MOB} interaction with a null source reads the {@code mobGriefing} rule
     * ({@code EventHooks.canEntityGrief}), the flag the original passed.
     */
    public static void explodeUnattributed(Level level, double x, double y, double z, float strength) {
        level.explode(null, x, y, z, strength, Level.ExplosionInteraction.MOB);
    }

    /** {@code MovingObjectPosition.entityHit}. */
    @Nullable
    public static Entity entityHit(HitResult result) {
        return result instanceof EntityHitResult entityResult ? entityResult.getEntity() : null;
    }

    /**
     * {@code World.blockExists((int) x, (int) y, (int) z)}: the block position, then a loaded chunk.
     * PORT: the casts are {@code Mth.floor} (DECISIONS R20: truncation named the neighbouring block in
     * the negative half); the 1.7.10 height test {@code 0 <= y < 256} is the level's build height here.
     */
    public static boolean blockExists(Level level, double x, double y, double z) {
        BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        return level.isInWorldBounds(pos) && level.hasChunkAt(pos);
    }

    /**
     * {@code Blocks.fire} as the originals placed it. {@code BaseFireBlock.getState} adds the side
     * attachments a 1.7.10 fire derived while rendering, but returns soul fire on soul soil, which
     * 1.7.10 did not have; there plain fire's default state is what placement would give anyway,
     * because the soul block below is sturdy.
     */
    public static BlockState fireState(Level level, BlockPos pos) {
        BlockState state = BaseFireBlock.getState(level, pos);
        return state.is(Blocks.FIRE) ? state : Blocks.FIRE.defaultBlockState();
    }

    /**
     * The 1.7.10 particle {@code "reddust"} with three velocity arguments. {@code EntityReddustFX}
     * reads them as a colour, not as motion: red 0 becomes 1, each channel is multiplied by the
     * same per-particle randomisation 1.21.1's dust uses, and the Tessellator clamped the vertex
     * colour to 0..255. The laser trail's Gaussian/10 values therefore draw mostly dark specks, the
     * urchin's random floats random colours. PORT: the clamp moves before the randomisation, so a
     * channel above 1 can render up to 20 percent dimmer than 1.7.10 did.
     */
    public static ParticleOptions reddust(double red, double green, double blue) {
        float r = (float) red;
        float g = (float) green;
        float b = (float) blue;
        if (r == 0.0F) {
            r = 1.0F;
        }
        return new DustParticleOptions(new Vector3f(Mth.clamp(r, 0.0F, 1.0F), Mth.clamp(g, 0.0F, 1.0F), Mth.clamp(b, 0.0F, 1.0F)), 1.0F);
    }
}
