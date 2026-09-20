package com.swbr.orespawn.world.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Block and biome reads for a structure dispatcher ({@link LegacyStructureRegistry.Dispatcher}), before any block of
 * the chunk exists. The 1.7.10 placement rules read the finished, decorated world; a 1.21.1 structure start is
 * decided at {@code STRUCTURE_STARTS}, where only the generator's <em>base column</em> is known
 * ({@code ChunkGenerator.getBaseColumn}: terrain and fluid, no surface blocks, no trees, no other structures).
 *
 * <p>PORT (DECISIONS R18 case 3), the substitutions every dispatcher makes:
 * <ul>
 * <li>{@code getBlock(x, y, z)} → the base column; {@code isAirBlock} → air in the base column.</li>
 * <li>Surface blocks do not exist yet: {@code == Blocks.grass}, {@code sand}, {@code snow}, {@code end_stone} and
 * {@code CrystalGrass} become {@link #isGround} (any solid, non-fluid block, the exact block where the generator
 * already writes it - Islands grass, End stone), {@code == Blocks.water} becomes {@link #isWater}.</li>
 * <li>Air checks over large areas ({@code quickSpaceCheck} and relatives, up to 60x60 columns) are sampled on a
 * grid ({@link #areaIsAir}); every base column of the OreSpawn generators computes a whole chunk of terrain.</li>
 * <li>{@code getBiomeGenForCoords(x, z)} → the noise biome at the column's surface.</li>
 * </ul>
 * Columns are cached for the lifetime of one probe (one {@code findGenerationPoint} call).
 */
public final class SurfaceProbe {

    private final Structure.GenerationContext context;
    private final Long2ObjectOpenHashMap<NoiseColumn> columns = new Long2ObjectOpenHashMap<>();

    public SurfaceProbe(final Structure.GenerationContext context) {
        this.context = context;
    }

    public Structure.GenerationContext context() {
        return this.context;
    }

    public int minY() {
        return this.context.heightAccessor().getMinBuildHeight();
    }

    public int maxY() {
        return this.context.heightAccessor().getMaxBuildHeight();
    }

    private NoiseColumn column(final int x, final int z) {
        final long key = ChunkPos.asLong(x, z);
        NoiseColumn column = this.columns.get(key);
        if (column == null) {
            column = this.context.chunkGenerator().getBaseColumn(x, z, this.context.heightAccessor(),
                    this.context.randomState());
            this.columns.put(key, column);
        }
        return column;
    }

    /** {@code World.getBlock} on the base column. */
    public BlockState getBlock(final int x, final int y, final int z) {
        if (y < this.minY() || y >= this.maxY()) {
            return Blocks.AIR.defaultBlockState();
        }
        return this.column(x, z).getBlock(y);
    }

    /** {@code World.isAirBlock} on the base column. */
    public boolean isAirBlock(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).isAir();
    }

    /** The substitute for a surface block comparison ({@code grass}, {@code sand}, {@code snow}, {@code end_stone}, crystal grass). */
    public boolean isGround(final int x, final int y, final int z) {
        return isGround(this.getBlock(x, y, z));
    }

    /** The substitute for {@code == Blocks.water}. */
    public boolean isWater(final int x, final int y, final int z) {
        return this.getBlock(x, y, z).getFluidState().is(FluidTags.WATER);
    }

    /** {@code == block} where the generator really writes that block into its base column (Islands grass, End stone). */
    public boolean is(final int x, final int y, final int z, final Block block) {
        return this.getBlock(x, y, z).is(block);
    }

    public static boolean isGround(final BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }

    /**
     * The first Y above the base column ({@code WORLD_SURFACE_WG}); where a vanilla-world rule started a downward
     * surface scan at a literal Y (100, 110, 127, 128), DECISIONS R18 starts it here instead.
     */
    public int surfaceY(final int x, final int z) {
        for (int y = this.maxY() - 1; y >= this.minY(); --y) {
            if (!this.getBlock(x, y, z).isAir()) {
                return y + 1;
            }
        }
        return this.minY();
    }

    /**
     * Start of a downward surface scan: the literal Y of the original in the OreSpawn dimensions (DECISIONS R21),
     * {@link #surfaceY} in vanilla worlds (R18).
     */
    public int scanStart(final int x, final int z, final int literal, final boolean heightmap) {
        return heightmap ? this.surfaceY(x, z) : literal;
    }

    /** {@code getBiomeGenForCoords(x, z)}: the noise biome at the column's surface. */
    public Holder<Biome> getBiome(final int x, final int z) {
        final int y = this.surfaceY(x, z);
        return this.context.biomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                QuartPos.fromBlock(z), this.context.randomState().sampler());
    }

    /** {@code getBiomeGenForCoords(x, z).biomeName.equals(name)}, the name mapped to a tag ({@link LegacyBiomeNames}). */
    public boolean biomeIs(final int x, final int z, final TagKey<Biome> legacyName) {
        return this.getBiome(x, z).is(legacyName);
    }

    /**
     * {@code for (i = x0; i < x1; ++i) for (k = z0; k < z1; ++k) if (getBlock(posX + i, y, posZ + k) != air) fail}.
     *
     * <p>PORT: sampled every {@code step} columns, always including the far edges (see class javadoc).
     *
     * @param tolerated blocks the original let pass besides air ({@code Blocks.log}, apple and scary leaves in
     *                  {@code D4BigSpaceCheck}); matched against the base column, where they never occur
     */
    public boolean areaIsAir(final int posX, final int y, final int posZ, final int x0, final int x1, final int z0,
                             final int z1, final int step, final Block... tolerated) {
        for (int i = x0; i < x1; i = next(i, x1, step)) {
            for (int k = z0; k < z1; k = next(k, z1, step)) {
                final BlockState bid = this.getBlock(posX + i, y, posZ + k);
                if (!bid.isAir() && !isAny(bid, tolerated)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int next(final int current, final int end, final int step) {
        if (current == end - 1) {
            return end;
        }
        return Math.min(current + step, end - 1);
    }

    private static boolean isAny(final BlockState state, final Block[] blocks) {
        for (final Block block : blocks) {
            if (state.is(block)) {
                return true;
            }
        }
        return false;
    }
}
