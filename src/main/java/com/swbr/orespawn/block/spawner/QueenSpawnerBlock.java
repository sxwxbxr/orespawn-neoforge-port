package com.swbr.orespawn.block.spawner;

import com.swbr.orespawn.block.plant.ReedLikePlant;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobSwitches;
import com.swbr.orespawn.entity.boss.queen.TheQueen;
import com.swbr.orespawn.registry.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code danger.orespawn.QueenSpawnerBlock} (QueenSpawnerBlock.java:13-90): The Queen Spawner Block
 * ({@code queenspawner}, OreSpawnMain.java:1602, light 0.9, legacy id +197, registered :1843 with an item block),
 * verhalten/itemblock-03.md. A one-shot {@code BlockReed} that summons The Queen eight blocks above itself in guard
 * mode and removes itself and the block above.
 *
 * <ul>
 *   <li>Bounds 0.125..0.875, full height (:16-17) - {@link ReedLikePlant#SHAPE}; random ticks (:18); tab Decorations
 *       (:19).</li>
 *   <li>Triggers, all ending in {@link #updateTick}: the tick scheduled 100 ticks after placement (:39-44), every random
 *       tick, a player breaking it (:46-48) and every neighbour change - 1.7.10's {@code BlockReed.onNeighborBlockChange}
 *       asked {@code canBlockStay}, which this class turned into the summon (:81-84).</li>
 *   <li>{@code canPlaceBlockAt} (:22-24): a solid block below. Drops {@code queenspawner} once (:61-67), loot table
 *       {@code blocks/queenspawner}; pick block gives sugar cane ({@code BlockReed.getItem}, {@link ReedLikePlant}).</li>
 * </ul>
 *
 * <p>PORT: {@code randomDisplayTick} (:26-37) began with a server branch that called {@code updateTick}; 1.7.10 only
 * ever called {@code randomDisplayTick} on the client, so that branch is dead and not ported.
 */
public class QueenSpawnerBlock extends ReedLikePlant {

    public QueenSpawnerBlock(final Properties properties) {
        super(properties);
    }

    /**
     * Registration properties. {@code BlockReed}'s constructor set {@code Material.plants} with hardness 0 and
     * {@code Block}'s default step sound, stone; {@code setLightLevel(0.9f)} (:1602) is {@code (int) (15 * 0.9f)} = 13.
     * Same as {@code DungeonSpawnerBlock}.
     */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.STONE).lightLevel(s -> 13);
    }

    /** {@code canPlaceBlockAt} (:22-24): {@code getMaterial().isSolid()} of the block below. */
    @Override
    @SuppressWarnings("deprecation") // BlockState.isSolid() is the 1.7.10 Material.isSolid()
    protected boolean mayPlaceOn(final BlockState soil) {
        return soil.isSolid();
    }

    /** {@code randomDisplayTick} (:26-37), client half: with 1/20 twenty firework sparks without motion. */
    @Override
    public void animateTick(final BlockState state, final Level par1World, final BlockPos pos, final RandomSource par5Random) {
        if (par1World.random.nextInt(20) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 20; ++j1) {
            par1World.addParticle(ParticleTypes.FIREWORK, pos.getX() + par1World.random.nextFloat(),
                    pos.getY() + (double) par1World.random.nextFloat(), pos.getZ() + par1World.random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }

    /** {@code onBlockAdded} (:39-44): the server schedules the summon in 100 ticks. */
    @Override
    protected void onPlace(final BlockState state, final Level world, final BlockPos pos, final BlockState oldState,
                           final boolean movedByPiston) {
        super.onPlace(state, world, pos, oldState, movedByPiston);
        if (world.isClientSide) {
            return;
        }
        world.scheduleTick(pos, this, 100);
    }

    /** {@code onBlockDestroyedByPlayer} (:46-48): {@code destroy} runs after a player removed the block, on both sides. */
    @Override
    public void destroy(final LevelAccessor level, final BlockPos pos, final BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof Level world) {
            this.updateTick(world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /** {@code updateTick} as the scheduled tick. */
    @Override
    protected void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /** {@code updateTick} as the random tick ({@code setTickRandomly(true)}, :18). */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * {@code canBlockStay} (:81-84) as 1.7.10's {@code BlockReed.onNeighborBlockChange} reached it: the summon, and the
     * block never drops. {@code canSurvive} keeps {@code canPlaceBlockAt} for placement, which is what 1.21.1's block
     * item asks.
     */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block neighborBlock,
                                   final BlockPos neighborPos, final boolean movedByPiston) {
        this.updateTick(level, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * {@code updateTick} (:50-59): on the server, with {@code TheQueenEnable}, The Queen at y + 8; then this block and the
     * one above become air (flag 2).
     *
     * <p>{@code TheQueenEnable} is read through {@link MobSwitches} ({@code AllMobsDisable} zeroed it in
     * {@code getMobs}).
     */
    public void updateTick(final Level par1World, final int par2, final int par3, final int par4) {
        if (par1World.isClientSide) {
            return;
        }
        if (MobSwitches.enabled(OreSpawnConfig.MOBS.TheQueenEnable)) {
            spawnTheQueen(par1World, par2, par3 + 8, par4);
        }
        par1World.setBlock(new BlockPos(par2, par3, par4), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        par1World.setBlock(new BlockPos(par2, par3 + 1, par4), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    /**
     * {@code spawnTheQueen} (:69-79): create, place at the block corner with a random yaw, add, play the living sound,
     * guard mode 1. No bad mood - that is only the Queen's tree (ItemMagicApple).
     */
    @Nullable
    public static Entity spawnTheQueen(final Level par0World, final double par2, final double par4, final double par6) {
        final TheQueen var8 = ModEntities.THE_QUEEN.get().create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            ((Mob) var8).playAmbientSound();
            var8.setGuardMode(1);
        }
        return var8;
    }
}
