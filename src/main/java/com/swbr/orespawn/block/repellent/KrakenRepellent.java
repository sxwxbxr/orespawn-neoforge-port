package com.swbr.orespawn.block.repellent;

import com.swbr.orespawn.entity.boss.kraken.Kraken;
import com.swbr.orespawn.entity.portal.EntityAnt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;

/**
 * Port of {@code danger.orespawn.KrakenRepellent} (KrakenRepellent.java:12-130), standing half: Kraken Repellent
 * ({@code krakenrepellent}, OreSpawnMain.java:1590, light 0.8 -> 12, legacy block id +190), tab Redstone (:14-16). Wall
 * half: {@link KrakenRepellentWall} ({@code krakenrepellent_wall}, DECISIONS R18 "Fackel-Ids"). Recipe
 * {@code data/orespawn/recipe/krakenrepellent.json} (W12).
 *
 * <p>Every 10 ticks ({@code tickRate}, :53-55) the server pushes every Kraken and every ant in the box x ±20,
 * y -10..+40, z ±20 around the block corner horizontally away from the block (:76-124); the Kraken's distance is
 * measured from 15 blocks below its feet, where its prey hangs.
 *
 * <p>The tick chain overrides {@code updateTick}, {@code onBlockAdded} and {@code onNeighborBlockChange} of
 * {@code BlockTorch} without calling {@code super} (:57-70), which also removed the torch's "drop when the support is
 * gone" check: the repellent stays where it is (itemblock-03.md). Kept 1:1 ({@link #updateShape}); placement still
 * asks vanilla's {@code canSurvive}, as {@code canPlaceBlockAt} called {@code super} (:72-74). {@code BlockTorch} ticks
 * randomly (its constructor), and a random tick ran the same {@code updateTick}: an extra push and a re-schedule -
 * hence {@link #originalProperties()} with {@code randomTicks()} and {@link #randomTick} calling the same body
 * (1.21.1's default {@code randomTick} is empty).
 */
public class KrakenRepellent extends TorchBlock {

    /** {@code tickRate(World)} (:53-55). */
    static final int TICK_RATE = 10;

    public KrakenRepellent(final Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    /**
     * The properties of the original: {@code BlockTorch} is {@code Material.circuits} (no push mobility), hardness 0,
     * random ticks; light from {@code setLightLevel(0.8f)} (:1590) is {@code (int) (15 * 0.8)} = 12; the step sound is
     * {@code Block}'s default stone (ModBlocks torch note, W02). Shared by the wall half.
     */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(s -> 12).sound(SoundType.STONE)
                .pushReaction(PushReaction.DESTROY).randomTicks();
    }

    /** {@code randomDisplayTick} (:18-51), standing branch (metadata 0 and 5): 0.21 above the flame point. */
    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
        final double var7 = pos.getX() + 0.5f;
        final double var8 = pos.getY() + 0.7f;
        final double var9 = pos.getZ() + 0.5f;
        spawnParticles(level, var7, var8 + 0.21, var9);
    }

    /** {@code smoke}, {@code flame}, {@code reddust} without velocity - every branch of {@code randomDisplayTick}. */
    static void spawnParticles(final Level world, final double x, final double y, final double z) {
        world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        world.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
    }

    /** {@code updateTick} (:57-62): repel, then schedule the next tick. Scheduled and random ticks run on the server only. */
    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        repelTick(this, level, pos);
    }

    /** A random tick of {@code BlockTorch} ran {@code updateTick} (:57-62) as well. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        repelTick(this, level, pos);
    }

    /** {@code onBlockAdded} (:64-66). */
    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_RATE);
    }

    /** {@code onNeighborBlockChange} (:68-70), without the torch's support check. */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block neighborBlock,
                                   final BlockPos neighborPos, final boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_RATE);
    }

    /**
     * PORT: 1.21.1 drops a torch whose support is gone in {@code updateShape}; the original's
     * {@code onNeighborBlockChange} override without {@code super} never ran that check, so the block keeps its state.
     */
    @Override
    protected BlockState updateShape(final BlockState state, final Direction direction, final BlockState neighborState,
                                     final LevelAccessor level, final BlockPos pos, final BlockPos neighborPos) {
        return state;
    }

    /** The body of {@code updateTick} (:57-62), shared with the wall half. */
    static void repelTick(final Block block, final ServerLevel level, final BlockPos pos) {
        findSomethingToRepell(level, pos.getX(), pos.getY(), pos.getZ());
        level.scheduleTick(pos, block, TICK_RATE);
    }

    /**
     * {@code findSomethingToRepell} (:76-124): every living entity in {@code (x - 20, y - 10, z - 20)} to
     * {@code (x + 20, y + 40, z + 20)}. A Kraken (distance from its feet minus 15 in Y) and an ant ({@code EntityAnt}
     * and its subclasses, plain distance) get {@code f = clamp(20 - d, 0, 20) * 0.4} added to their horizontal motion
     * along the float-cast angle {@code atan2(dx, dz)} away from the block corner. The two checks are independent
     * {@code if}s, as in the original.
     *
     * <p>PORT: {@code motionX += ...} is {@code setDeltaMovement}. The original set no {@code velocityChanged}; neither
     * does the port (only mobs are pushed, no player).
     */
    static void findSomethingToRepell(final Level par1World, final int par2, final int par3, final int par4) {
        final AABB bb = new AABB(par2 - 20.0, par3 - 10.0, par4 - 20.0, par2 + 20.0, par3 + 40.0, par4 + 20.0);
        for (final LivingEntity var7 : par1World.getEntitiesOfClass(LivingEntity.class, bb)) {
            if (var7 != null && var7 instanceof Kraken) {
                final double d1 = var7.getX() - par2;
                final double d2 = var7.getY() - 15.0 - par3;
                final double d3 = var7.getZ() - par4;
                push(var7, par2, par4, d1, d2, d3);
            }
            if (var7 != null && var7 instanceof EntityAnt) {
                final double d1 = var7.getX() - par2;
                final double d2 = var7.getY() - par3;
                final double d3 = var7.getZ() - par4;
                push(var7, par2, par4, d1, d2, d3);
            }
        }
    }

    /** The push block both branches repeat (:86-101, :104-121). */
    private static void push(final LivingEntity var7, final int par2, final int par4, final double d1, final double d2, final double d3) {
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
        var7.setDeltaMovement(var7.getDeltaMovement().add(f * Math.sin(d4), 0.0, f * Math.cos(d4)));
    }
}
