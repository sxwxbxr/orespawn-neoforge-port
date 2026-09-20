package com.swbr.orespawn.world.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;

/**
 * The slice of the 1.7.10 {@code World} ({@code ahb}) that the populate step of the OreSpawn noise-terrain
 * dimensions reads and writes, on top of a 1.21.1 {@link WorldGenLevel}. No original class: this is the
 * seam between the ported vanilla 1.7.10 feature code ({@link WorldGenerator} and its subclasses,
 * {@link BiomeDecorator}) and the generation region. Utopia, VillageMania, Mining and the ore step of Chaos
 * all go through this one copy (DECISIONS R21).
 *
 * <p>Semantics, each checked against the reference jar with {@code javap -c}:
 * <ul>
 * <li>{@code getBlock} is air outside Y 0..255 and {@code setBlock} does nothing there, as in 1.7.10.</li>
 * <li>{@code getHeightValue} ({@code ahb.f(II)}) read the light-opacity height map of the chunk. PORT: the
 * nearest 1.21.1 map is {@link Heightmap.Types#MOTION_BLOCKING}; on freshly generated terrain (stone, dirt,
 * grass, water, sand, clay, ores, plants) both give the same height.</li>
 * <li>{@code getTopSolidOrLiquidBlock} ({@code ahb.i(II)}) scans down for the first block whose material
 * blocks movement and is not leaves - liquids do not block movement, so this is the sea floor.</li>
 * <li>Light. 1.7.10 populated chunks that had a sky light map but no spread light yet, so a position above
 * the height map read 15 and one below read 0. PORT: 1.21.1 lights chunks only after the feature step; the
 * port answers light questions from the same height map instead of the light engine.</li>
 * <li>Placing a falling block schedules its tick (delay 2), as {@code BlockFalling.onBlockAdded} did; a
 * 1.21.1 proto chunk does not call {@code onPlace}.</li>
 * <li>Placing leaves schedules a leaves tick (delay 1), see {@link #setBlock(int, int, int, BlockState, int)}.</li>
 * </ul>
 */
public final class LegacyWorld {

    /** 1.7.10 world floor and ceiling. */
    public static final int MIN_Y = 0;
    public static final int MAX_Y = 256;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final WorldGenLevel level;
    private final BlockState biomeTopBlock;

    /**
     * @param level         the generation region
     * @param biomeTopBlock {@code topBlock} of the dimension's only biome ({@code BiomeGenBase.topBlock})
     */
    public LegacyWorld(final WorldGenLevel level, final BlockState biomeTopBlock) {
        this.level = level;
        this.biomeTopBlock = biomeTopBlock;
    }

    public WorldGenLevel level() {
        return this.level;
    }

    /** {@code ahb.a(III)} ({@code getBlock}). */
    public BlockState getBlock(final int x, final int y, final int z) {
        if (y < MIN_Y || y >= MAX_Y) {
            return AIR;
        }
        return this.level.getBlockState(new BlockPos(x, y, z));
    }

    /** {@code ahb.c(III)} ({@code isAirBlock}): material air. */
    public boolean isAirBlock(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).isAir();
    }

    /** {@code World.setBlock(x, y, z, block, meta, 2)}: every populate write in these classes uses flag 2. */
    public boolean setBlock(final int x, final int y, final int z, final BlockState state) {
        return this.setBlock(x, y, z, state, Block.UPDATE_CLIENTS);
    }

    /**
     * {@code ahb.d(IIIBlock;II)} ({@code setBlock}); the flag bits mean the same in 1.21.1 ({@code FastBlocks}).
     *
     * <p>PORT, leaves: 1.7.10 leaves placed with metadata 0 carried no distance; they only started to decay after
     * a neighbouring log or leaf was removed and flagged them. A 1.21.1 leaves block placed at its default
     * distance 7 counts as decaying and would vanish on a random tick. The scheduled tick recomputes the
     * distance from the logs around it, which is what the first neighbour update does to 1.21.1 leaves; the
     * scheduled tick is saved with the chunk and runs once the chunk ticks. Leaves further than six blocks from
     * every log still decay then, which the 1.7.10 leaves did only after a neighbour changed.
     */
    public boolean setBlock(final int x, final int y, final int z, final BlockState state, final int flags) {
        if (y < MIN_Y || y >= MAX_Y) {
            return false;
        }
        final BlockPos pos = new BlockPos(x, y, z);
        final boolean changed = this.level.setBlock(pos, state, flags);
        if (changed && state.getBlock() instanceof FallingBlock) {
            this.level.scheduleTick(pos, state.getBlock(), 2);
        } else if (changed && state.getBlock() instanceof LeavesBlock) {
            this.level.scheduleTick(pos, state.getBlock(), 1);
        }
        return changed;
    }

    /** {@code ahb.f(III)} ({@code setBlockToAir}): {@code setBlock(x, y, z, air, 0, 3)}. */
    public boolean setBlockToAir(final int x, final int y, final int z) {
        return this.setBlock(x, y, z, AIR, Block.UPDATE_ALL);
    }

    /** Fluid tick for a placed source; see {@link WorldGenLiquids}. */
    public void scheduleFluidTick(final int x, final int y, final int z, final Fluid fluid, final int delay) {
        if (y >= MIN_Y && y < MAX_Y) {
            this.level.scheduleTick(new BlockPos(x, y, z), fluid, delay);
        }
    }

    /** {@code ahb.f(II)} ({@code getHeightValue}): first free Y above the height map. */
    public int getHeightValue(final int x, final int z) {
        return this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    /**
     * {@code ahb.i(II)} ({@code getTopSolidOrLiquidBlock}). The original started at the top of the highest
     * non-empty chunk section; everything above {@link Heightmap.Types#WORLD_SURFACE} is air and would be
     * skipped by the same test, so the scan starts there.
     */
    public int getTopSolidOrLiquidBlock(final int x, final int z) {
        for (int k = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z); k > 0; --k) {
            final BlockState block = this.getBlock(x, k, z);
            if (blocksMovement(block) && !isLeaves(block)) {
                return k + 1;
            }
        }
        return -1;
    }

    /** {@code ahb.j(III)} ({@code getFullBlockLightValue}), answered from the height map (class javadoc). */
    public int getFullBlockLightValue(final int x, final int y, final int z) {
        return y >= this.getHeightValue(x, z) ? 15 : 0;
    }

    /** {@code ahb.b(EnumSkyBlock.Sky, x, y, z) > 0}, answered from the height map (class javadoc). */
    public boolean hasSkyLight(final int x, final int y, final int z) {
        return y >= this.getHeightValue(x, z);
    }

    /** {@code ahb.a(II).topBlock}: every OreSpawn dimension has exactly one biome. */
    public BlockState getBiomeTopBlock(final int x, final int z) {
        return this.biomeTopBlock;
    }

    /**
     * {@code ahb.r(III)} ({@code isBlockFreezable}). {@code canBlockFreezeBody} returned {@code false} first
     * thing when the biome temperature at the position exceeded 0.15. {@code BiomeGenUtopianPlains} has 0.7,
     * Extreme Hills in the Mining dimension 0.8 (WorldProviderOreSpawn2.java:22), and
     * {@code getFloatTemperature} lowers either above Y 64 by at most {@code (4 + 255 - 64) * 0.05 / 30},
     * i.e. by 0.325 at the build limit - water in these dimensions never froze.
     */
    public boolean isBlockFreezable(final int x, final int y, final int z) {
        return false;
    }

    /** {@code Block.isOpaqueCube} of the block at the position. */
    public boolean isOpaqueCube(final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        return this.getBlock(x, y, z).isSolidRender(this.level, pos);
    }

    /**
     * {@code Block.isFullBlock} ({@code aji.j()}): the field {@code aji.q}, which the {@code Block} constructor
     * set from {@code isOpaqueCube()} ({@code aji.<init>} @47-52). Leaves stored {@code true} there, because
     * {@code BlockLeaves.isOpaqueCube} reads the fancy-graphics flag, which is still {@code false} while the
     * superclass constructor runs. PORT: {@code isSolidRender}, plus leaves, whose 1.21.1 shape does not
     * occlude.
     */
    public boolean isFullBlock(final int x, final int y, final int z) {
        final BlockState block = this.getBlock(x, y, z);
        return isLeaves(block) || block.isSolidRender(this.level, new BlockPos(x, y, z));
    }

    /** {@code BlockBush.canBlockStay} ({@code ajr.j}): grass, dirt or farmland below. Flowers and tall grass. */
    public boolean canBushStay(final int x, final int y, final int z) {
        final BlockState below = this.getBlock(x, y - 1, z);
        return below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) || below.is(Blocks.FARMLAND);
    }

    /**
     * {@code BlockMushroom.canBlockStay} ({@code amc.j}): mycelium or podzol (dirt metadata 2) below, otherwise
     * light below 13 and an opaque cube below.
     */
    public boolean canMushroomStay(final int x, final int y, final int z) {
        if (y < 0 || y >= 256) {
            return false;
        }
        final BlockState below = this.getBlock(x, y - 1, z);
        if (below.is(Blocks.MYCELIUM) || below.is(Blocks.PODZOL)) {
            return true;
        }
        return this.getFullBlockLightValue(x, y, z) < 13 && this.isOpaqueCube(x, y - 1, z);
    }

    /**
     * {@code BlockReed.canPlaceBlockAt} ({@code ane.c}, also its {@code canBlockStay}): sugar cane below, or grass,
     * dirt or sand below with water next to that block.
     */
    public boolean canReedStay(final int x, final int y, final int z) {
        final BlockState below = this.getBlock(x, y - 1, z);
        if (below.is(Blocks.SUGAR_CANE)) {
            return true;
        }
        if (!below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.DIRT) && !below.is(Blocks.SAND)) {
            return false;
        }
        return isWater(this.getBlock(x - 1, y - 1, z))
                || isWater(this.getBlock(x + 1, y - 1, z))
                || isWater(this.getBlock(x, y - 1, z - 1))
                || isWater(this.getBlock(x, y - 1, z + 1));
    }

    /**
     * {@code BlockPumpkin.canPlaceBlockAt} ({@code amw.c}): replaceable material here and a solid top surface
     * below. PORT: {@code World.doesBlockHaveSolidTopSurface} becomes {@code isFaceSturdy(UP)}.
     */
    public boolean canPumpkinPlace(final int x, final int y, final int z) {
        final BlockPos below = new BlockPos(x, y - 1, z);
        return this.getBlock(x, y, z).canBeReplaced()
                && this.getBlock(x, y - 1, z).isFaceSturdy(this.level, below, Direction.UP);
    }

    /** {@code Material.water}: only water blocks existed in 1.7.10, no waterlogging. */
    public static boolean isWater(final BlockState state) {
        return state.is(Blocks.WATER);
    }

    /** {@code Material.leaves}. */
    public static boolean isLeaves(final BlockState state) {
        return state.is(BlockTags.LEAVES);
    }

    /**
     * {@code Material.isSolid()}: false for air, liquids, plants, circuits, snow layers and portals. 1.21.1
     * keeps exactly this legacy flag on every block state.
     */
    @SuppressWarnings("deprecation")
    public static boolean isSolid(final BlockState state) {
        return state.isSolid();
    }

    /** {@code Material.isLiquid()}. */
    @SuppressWarnings("deprecation")
    public static boolean isLiquid(final BlockState state) {
        return state.liquid();
    }

    /** {@code Material.blocksMovement()}. */
    @SuppressWarnings("deprecation")
    public static boolean blocksMovement(final BlockState state) {
        return state.blocksMotion();
    }
}
