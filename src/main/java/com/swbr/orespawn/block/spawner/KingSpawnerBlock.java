package com.swbr.orespawn.block.spawner;

import com.swbr.orespawn.block.plant.ReedLikePlant;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobSwitches;
import com.swbr.orespawn.entity.boss.king.TheKing;
import com.swbr.orespawn.registry.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code danger.orespawn.KingSpawnerBlock} (KingSpawnerBlock.java:13-90): The King Spawner Block
 * ({@code kingspawner}, OreSpawnMain.java:1601, legacy id +195, light 0.9; registered :1842). A one-shot
 * {@code BlockReed} that summons The King eight blocks above itself in guard mode and vanishes together with the block
 * above (verhalten/itemblock-03.md, "KingSpawnerBlock").
 *
 * <p>Every trigger of the original ends in {@link #updateTick}:
 * <ul>
 *   <li>{@code onBlockAdded} (:39-44): a tick 100 ticks after placement ({@link #onPlace}, {@link #tick});</li>
 *   <li>a random tick ({@code setTickRandomly(true)}, :18; {@link #randomTick});</li>
 *   <li>{@code onBlockDestroyedByPlayer} (:46-48): {@link #destroy}, which 1.21.1 calls at the same point - after the
 *       block was removed and before the drop is harvested, so the block item still drops (:61-67);</li>
 *   <li>{@code canBlockStay} (:81-84), which {@code BlockReed.onNeighborBlockChange} asked on every neighbour change
 *       and which always answered {@code true}: {@link #neighborChanged} runs the tick and never drops the block.</li>
 * </ul>
 * The {@code !isRemote} branch of {@code randomDisplayTick} (:27-30) is dead in 1.7.10 (only the client world calls
 * it) and in 1.21.1 ({@code animateTick} is client-only) alike.
 *
 * <p>Bounds 0.125..0.875 (:16-17) and the reed half (no collision, pick block sugar cane) are {@link ReedLikePlant};
 * {@code canPlaceBlockAt} (:22-24) is {@link #mayPlaceOn}. The block item and its tab (Decorations, :19) are registered
 * with the item holder.
 */
public class KingSpawnerBlock extends ReedLikePlant {

    public KingSpawnerBlock(final Properties properties) {
        super(properties);
    }

    /**
     * Registration properties. {@code BlockReed}'s constructor set {@code Material.plants}, hardness 0 and
     * {@code Block}'s default step sound, stone; {@code setLightLevel(0.9f)} (:1601) is {@code (int) (15 * 0.9f)} = 13.
     * Same as {@code IslandBlock} and {@code DungeonSpawnerBlock}.
     */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.STONE).lightLevel(s -> 13);
    }

    /** {@code canPlaceBlockAt} (:22-24): {@code getBlock(x, y - 1, z).getMaterial().isSolid()}. */
    @Override
    @SuppressWarnings("deprecation") // BlockState.isSolid() is the 1.7.10 Material.isSolid()
    protected boolean mayPlaceOn(final BlockState soil) {
        return soil.isSolid();
    }

    /** {@code randomDisplayTick} (:26-37), client half: 1/20, twenty firework sparks at rest; {@code par1World.rand}. */
    @Override
    public void animateTick(final BlockState state, final Level par1World, final BlockPos pos, final RandomSource par5Random) {
        final int par2 = pos.getX();
        final int par3 = pos.getY();
        final int par4 = pos.getZ();
        if (!par1World.isClientSide) {
            return; // :27-30, unreachable
        }
        if (par1World.random.nextInt(20) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 20; ++j1) {
            par1World.addParticle(ParticleTypes.FIREWORK, par2 + par1World.random.nextFloat(),
                    par3 + (double) par1World.random.nextFloat(), par4 + par1World.random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }

    /** {@code onBlockAdded} (:39-44): the server schedules the summoning in 100 ticks. */
    @Override
    protected void onPlace(final BlockState state, final Level world, final BlockPos pos, final BlockState oldState,
                           final boolean movedByPiston) {
        super.onPlace(state, world, pos, oldState, movedByPiston);
        if (world.isClientSide) {
            return;
        }
        world.scheduleTick(pos, this, 100);
    }

    /** {@code onBlockDestroyedByPlayer} (:46-48). */
    @Override
    public void destroy(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof Level world) {
            this.updateTick(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /** {@code updateTick} as the scheduled tick of {@code onBlockAdded}. */
    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /** {@code updateTick} as the random tick. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * {@code BlockReed.onNeighborBlockChange} -> {@code checkBlockCoordValid} -> {@code canBlockStay} (:81-84): the
     * summoning, then {@code true}, so nothing drops. Replaces the drop-and-remove of {@link ReedLikePlant}.
     */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block neighborBlock,
                                   final BlockPos neighborPos, final boolean movedByPiston) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * {@code updateTick} (:50-59): with {@code TheKingEnable} The King eight blocks above the block corner, then this
     * block and the one above become air with flag 2.
     *
     * <p>Flag 2 is {@code UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE}: 1.7.10 flag 2 sent the change without any neighbour
     * update, and 1.21.1 runs shape updates unless {@code UPDATE_KNOWN_SHAPE} is set (W03 precedent).
     * {@code TheKingEnable} is read through {@link MobSwitches} so that {@code AllMobsDisable} zeroes it, as
     * {@code disableAllMobs} did.
     */
    public void updateTick(final Level par1World, final int par2, final int par3, final int par4) {
        if (par1World.isClientSide) {
            return;
        }
        if (MobSwitches.enabled(OreSpawnConfig.MOBS.TheKingEnable)) {
            spawnTheKing(par1World, par2, par3 + 8, par4);
        }
        par1World.setBlock(new BlockPos(par2, par3, par4), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        par1World.setBlock(new BlockPos(par2, par3 + 1, par4), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    /**
     * {@code spawnTheKing} (:69-79): create "The King", random yaw, add, living sound, guard mode 1 (his home column
     * stays at the spawner).
     */
    @Nullable
    public static Entity spawnTheKing(final Level par0World, final double par2, final double par4, final double par6) {
        TheKing var8 = null;
        var8 = ModEntities.THE_KING.get().create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            var8.playAmbientSound();
            var8.setGuardMode(1);
        }
        return var8;
    }
}
