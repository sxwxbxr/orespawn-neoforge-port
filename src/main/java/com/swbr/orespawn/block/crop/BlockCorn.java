package com.swbr.orespawn.block.crop;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.BlockCorn} (BlockCorn.java:11-102): tall corn in four block
 * stages, ids {@code corn_0} .. {@code corn_3} ("Corn Plant", OreSpawnMain.java:1567-1570) =
 * {@code MyCornPlant1..4}. Planted by {@code corn_seed} ({@link com.swbr.orespawn.item.crop.ItemCornCob}).
 *
 * <p>Structure and the four-bit target-height re-roll are those of {@link BlockTomato} (R18); the
 * numbers differ: target {@code 4 + rand(4)} = 4..7 (:40), threshold {@code 6 - myMaxHeight / 3}
 * (:57), and the ripening loop skips the tip and the base ({@code i = 1 .. myMaxHeight - 2}, :63).
 */
public class BlockCorn extends Block {

    public static final MapCodec<BlockCorn> CODEC = simpleCodec(p -> new BlockCorn(0, p));

    public static final IntegerProperty AGE = BlockLettuce.AGE;

    public static final BlockBehaviour.Properties PROPERTIES = BlockLettuce.PROPERTIES;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    /** {@code MyCornPlant1..4} by manifest id. */
    static final ResourceKey<Block>[] STAGES = BlockLettuce.stages("corn_");

    /** 0..3 = {@code MyCornPlant1..4}. */
    private final int stage;

    public BlockCorn(final int stage, final BlockBehaviour.Properties properties) {
        super(properties);
        this.stage = stage;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public int getStage() {
        return this.stage;
    }

    @Override
    protected MapCodec<BlockCorn> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                  final CollisionContext context) {
        return SHAPE;
    }

    /** {@code canPlaceBlockAt} (:22-25): below is a corn stage, grass, dirt or farmland. */
    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final BlockState bid = level.getBlockState(pos.below());
        return !bid.isAir() && (isCorn(bid) || CropBlocks.isGrassDirtOrFarmland(bid));
    }

    static boolean isCorn(final BlockState state) {
        return state.is(STAGES[0]) || state.is(STAGES[1]) || state.is(STAGES[2]) || state.is(STAGES[3]);
    }

    /** Vanilla {@code BlockReed.onNeighborBlockChange}; see {@link BlockLettuce#neighborChanged}. */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos,
                                   final Block neighborBlock, final BlockPos neighborPos, final boolean movedByPiston) {
        if (!state.canSurvive(level, pos)) {
            Block.dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    /** {@code updateTick} (:27-81); see {@link BlockTomato#randomTick} for the steps. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        int height = 1; // :28
        int dontGrow = 0; // :29
        if (this.stage != 0 && this.stage != 1) { // :33-35 - only Plant1 and Plant2 tick
            return;
        }
        int var7 = state.getValue(AGE); // :36
        int myMaxHeight = var7 >> 8; // :37 - four-bit metadata: always 0 (R18)
        var7 &= 0xFF; // :38
        if (myMaxHeight == 0) { // :39
            myMaxHeight = 4 + OreSpawn.OreSpawnRand.nextInt(4); // :40
        }
        BlockState bid = level.getBlockState(pos.above()); // :42
        if (bid.isAir()) { // :43
            for (int var8 = 1; var8 < 10; ++var8) { // :44
                bid = level.getBlockState(pos.below(var8)); // :45
                if (!isCorn(bid)) { // :46
                    break;
                }
                ++height; // :49
                if (bid.is(STAGES[2]) || bid.is(STAGES[3])) { // :50
                    dontGrow = 1;
                }
            }
            if (dontGrow != 0) { // :54
                myMaxHeight = height; // :55
            }
            if (var7 >= 6 - myMaxHeight / 3) { // :57
                if (height < myMaxHeight) { // :58
                    level.setBlock(pos.above(), CropBlocks.block(STAGES[0]).defaultBlockState(), Block.UPDATE_CLIENTS); // :59
                    level.setBlock(pos, CropBlocks.block(STAGES[1]).defaultBlockState(), Block.UPDATE_CLIENTS); // :60
                } else {
                    for (int i = 1; i < myMaxHeight - 1; ++i) { // :63 - neither the tip nor the base
                        final BlockPos below = pos.below(i);
                        bid = level.getBlockState(below); // :64
                        if (bid.is(STAGES[1])) { // :65
                            level.setBlock(below, CropBlocks.block(STAGES[2]).defaultBlockState(), Block.UPDATE_CLIENTS); // :66
                        } else if (bid.is(STAGES[2])) { // :68
                            level.setBlock(below, CropBlocks.block(STAGES[3]).defaultBlockState(), Block.UPDATE_CLIENTS); // :69
                        }
                    }
                    bid = level.getBlockState(pos); // :72
                    level.setBlock(pos, bid.getBlock().defaultBlockState(), Block.UPDATE_CLIENTS); // :73 - age reset
                }
            } else {
                bid = level.getBlockState(pos); // :77
                level.setBlock(pos, bid.setValue(AGE, (var7 + 1) & 0xF), Block.UPDATE_CLIENTS); // :78 - four-bit nibble
            }
        }
    }

    /**
     * Pick-block: {@code corn_seed}. {@code getItem(int, Random, int)} (:87-89) matched no vanilla
     * method and was dead, so the original handed out {@code BlockReed}'s sugar cane; PORT: the seed
     * item instead, as the catalogue's port note asks.
     */
    @Override
    public ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state) {
        return new ItemStack(CropBlocks.item("corn_seed"));
    }

    // Drops (:83-96): corn_seed, 1 + rand(2) for corn_3, nothing for the others →
    // data/orespawn/loot_table/blocks/corn_<n>.json.
}
