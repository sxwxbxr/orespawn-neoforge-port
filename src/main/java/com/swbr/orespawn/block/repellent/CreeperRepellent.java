package com.swbr.orespawn.block.repellent;

import com.swbr.orespawn.entity.boss.king.PurplePower;
import com.swbr.orespawn.entity.portal.EntityAnt;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.CreeperRepellent} (CreeperRepellent.java:13-155), standing half: Creeper Repellent
 * ({@code creeperrepellent}, OreSpawnMain.java:1592, light 0.8 -> 12, legacy id +191), tab Redstone (:16). Wall half:
 * {@link CreeperRepellentWall} ({@code creeperrepellent_wall}, R18 torch ids). verhalten/itemblock-03.md.
 *
 * <p>A torch that every 10 ticks flings creepers, ants and PurplePower orbs within x/z +-20, y +-10 of its block
 * corner horizontally away, up to 8 blocks a tick at the torch and nothing at 20 blocks.
 *
 * <ul>
 *   <li>The tick chain (:54-71): {@code onBlockAdded} and {@code onNeighborBlockChange} schedule a tick in
 *       {@code tickRate} = 10; {@code updateTick} repels and schedules again. {@code BlockTorch}'s constructor set random
 *       ticks, and a random tick is {@code updateTick} too.</li>
 *   <li>All three overrides skip {@code super}: the torch never drops when its support goes (vanilla's
 *       {@code updateShape} removal is disabled here), and {@code onBlockAdded} sets no orientation. Placement still
 *       asks the vanilla support ({@code canPlaceBlockAt} -> {@code super}, :73-75).</li>
 *   <li>Original bug kept (R18, "CreeperRepellent-return"): a PurplePower of type 10 (the Ultimate King's exploding
 *       orb) {@code return}s from the whole scan, so every entity after it in the list is not pushed this tick.</li>
 * </ul>
 *
 * <p>PORT: a repellent written by world generation gets no {@code onPlace} in 1.21.1 ({@code WorldGenRegion} calls
 * none), 1.7.10's {@code setBlock} did; its chain starts with the first random tick instead.
 */
public class CreeperRepellent extends TorchBlock {

    /** {@code tickRate} (:54-56). */
    static final int TICK_RATE = 10;

    public CreeperRepellent(final Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /** {@code randomDisplayTick} (:19-52), standing branch (metadata 0 and 5): 0.21 above the flame point. */
    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
        final double var7 = pos.getX() + 0.5f;
        final double var8 = pos.getY() + 0.7f;
        final double var9 = pos.getZ() + 0.5f;
        spawnParticles(level, var7, var8 + 0.21, var9);
    }

    /** One {@code smoke}, {@code flame} and {@code reddust} without motion - every branch of {@code randomDisplayTick}. */
    static void spawnParticles(final Level world, final double x, final double y, final double z) {
        world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        world.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
    }

    /** {@code onBlockAdded} (:65-67). */
    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState,
                           final boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_RATE);
    }

    /** {@code onNeighborBlockChange} (:69-71): no support check, only the next tick. */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block neighborBlock,
                                   final BlockPos neighborPos, final boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_RATE);
    }

    /** The 1.7.10 torch did not break when its support went (see the class comment). */
    @Override
    protected BlockState updateShape(final BlockState state, final Direction direction, final BlockState neighborState,
                                     final LevelAccessor level, final BlockPos pos, final BlockPos neighborPos) {
        return state;
    }

    /** {@code setTickRandomly(true)} of {@code BlockTorch}. */
    @Override
    protected boolean isRandomlyTicking(final BlockState state) {
        return true;
    }

    /** {@code updateTick} (:58-63) as the scheduled tick. */
    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        updateTick(this, level, pos);
    }

    /** {@code updateTick} (:58-63) as the random tick. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        updateTick(this, level, pos);
    }

    /** {@code updateTick} (:58-63), shared with the wall half: repel, then schedule the next tick. */
    static void updateTick(final Block block, final ServerLevel par1World, final BlockPos pos) {
        if (!par1World.isClientSide) {
            findSomethingToRepell(par1World, pos.getX(), pos.getY(), pos.getZ());
            par1World.scheduleTick(pos, block, TICK_RATE);
        }
    }

    /**
     * {@code findSomethingToRepell} (:77-149): every living entity in the box; creepers, ants and PurplePower get
     * {@code f = clamp(20 - distance, 0, 20) * 0.4} added horizontally along {@code atan2(dx, dz)} (float), a type-10
     * PurplePower ends the scan (R18).
     *
     * <p>PORT: {@code motionX += ...} is {@code push(x, 0, z)}, which also raises {@code hasImpulse} so the tracker
     * sends the new motion; the three branches are ordered as in the original, so an entity matching two of them (none
     * does) would be pushed twice.
     */
    static void findSomethingToRepell(final Level par1World, final int par2, final int par3, final int par4) {
        final AABB bb = new AABB(par2 - 20.0, par3 - 10.0, par4 - 20.0, par2 + 20.0, par3 + 10.0, par4 + 20.0);
        final List<LivingEntity> var5 = par1World.getEntitiesOfClass(LivingEntity.class, bb);
        for (final LivingEntity var7 : var5) {
            if (var7 != null && var7 instanceof Creeper) {
                pushAway(var7, par2, par3, par4);
            }
            if (var7 != null && var7 instanceof EntityAnt) {
                pushAway(var7, par2, par3, par4);
            }
            if (var7 != null && var7 instanceof PurplePower p) {
                if (p.getPurpleType() == 10) {
                    return;
                }
                pushAway(var7, par2, par3, par4);
            }
        }
    }

    /** The push of each branch (:85-102, :105-122, :129-146). */
    private static void pushAway(final LivingEntity var7, final int par2, final int par3, final int par4) {
        final double d1 = var7.getX() - par2;
        final double d2 = var7.getY() - par3;
        final double d3 = var7.getZ() - par4;
        double f = d1 * d1 + d2 * d2 + d3 * d3;
        f = Math.sqrt(f);
        f = 20.0 - f;
        if (f > 20.0) {
            f = 20.0;
        }
        if (f < 0.0) {
            f = 0.0;
        }
        f *= 0.4;
        final double d4 = (float) Math.atan2(var7.getX() - par2, var7.getZ() - par4);
        var7.push(f * Math.sin(d4), 0.0, f * Math.cos(d4));
    }
}
