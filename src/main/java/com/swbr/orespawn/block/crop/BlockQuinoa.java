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
 * Port of {@code danger.orespawn.BlockQuinoa} (BlockQuinoa.java:11-108): the tall crop of the
 * Crystal dimension, ids {@code quinoa_0} .. {@code quinoa_3} ("Quinoa Plant",
 * OreSpawnMain.java:1572-1575) = {@code MyQuinoaPlant1..4}. Planted by {@code quinoa}
 * ({@link com.swbr.orespawn.item.crop.ItemCornCob}).
 *
 * <p>Structure and the four-bit target-height re-roll are those of {@link BlockTomato} (R18). The
 * differences: Crystal Grass is a valid soil (:24); only Plant1 and Plant3 tick (:33-35); target
 * {@code 2 + rand(3)} = 2..4 (:40); threshold {@code 5 - myMaxHeight / 3} (:57); and ripening
 * touches only the ticking block itself, Plant1 → Plant3 → Plant4 (:63-69), so the lower
 * {@code quinoa_1} blocks of a stalk never ripen - only the tip becomes {@code quinoa_3}.
 */
public class BlockQuinoa extends Block {

    public static final MapCodec<BlockQuinoa> CODEC = simpleCodec(p -> new BlockQuinoa(0, p));

    public static final IntegerProperty AGE = BlockLettuce.AGE;

    public static final BlockBehaviour.Properties PROPERTIES = BlockLettuce.PROPERTIES;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    /** {@code MyQuinoaPlant1..4} by manifest id. */
    static final ResourceKey<Block>[] STAGES = BlockLettuce.stages("quinoa_");

    /** 0..3 = {@code MyQuinoaPlant1..4}. */
    private final int stage;

    public BlockQuinoa(final int stage, final BlockBehaviour.Properties properties) {
        super(properties);
        this.stage = stage;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public int getStage() {
        return this.stage;
    }

    @Override
    protected MapCodec<BlockQuinoa> codec() {
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

    /** {@code canPlaceBlockAt} (:22-25): below is a quinoa stage, grass, dirt, farmland or Crystal Grass. */
    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final BlockState bid = level.getBlockState(pos.below());
        return !bid.isAir() && (isQuinoa(bid) || CropBlocks.isGrassDirtOrFarmland(bid) || bid.is(CropBlocks.CRYSTAL_GRASS));
    }

    static boolean isQuinoa(final BlockState state) {
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

    /** {@code updateTick} (:27-79); see {@link BlockTomato#randomTick} for the shared steps. */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        int height = 1; // :28
        int dontGrow = 0; // :29
        if (this.stage != 0 && this.stage != 2) { // :33-35 - only Plant1 and Plant3 tick
            return;
        }
        int var7 = state.getValue(AGE); // :36
        int myMaxHeight = var7 >> 8; // :37 - four-bit metadata: always 0 (R18)
        var7 &= 0xFF; // :38
        if (myMaxHeight == 0) { // :39
            myMaxHeight = 2 + OreSpawn.OreSpawnRand.nextInt(3); // :40
        }
        BlockState bid = level.getBlockState(pos.above()); // :42
        if (bid.isAir()) { // :43
            for (int var8 = 1; var8 < 10; ++var8) { // :44
                bid = level.getBlockState(pos.below(var8)); // :45
                if (!isQuinoa(bid)) { // :46
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
            if (var7 >= 5 - myMaxHeight / 3) { // :57
                if (height < myMaxHeight) { // :58
                    level.setBlock(pos.above(), CropBlocks.block(STAGES[0]).defaultBlockState(), Block.UPDATE_CLIENTS); // :59
                    level.setBlock(pos, CropBlocks.block(STAGES[1]).defaultBlockState(), Block.UPDATE_CLIENTS); // :60
                } else {
                    bid = level.getBlockState(pos); // :63
                    if (bid.is(STAGES[0])) { // :64
                        level.setBlock(pos, CropBlocks.block(STAGES[2]).defaultBlockState(), Block.UPDATE_CLIENTS); // :65
                    } else if (bid.is(STAGES[2])) { // :67
                        level.setBlock(pos, CropBlocks.block(STAGES[3]).defaultBlockState(), Block.UPDATE_CLIENTS); // :68
                    }
                    bid = level.getBlockState(pos); // :70
                    level.setBlock(pos, bid.getBlock().defaultBlockState(), Block.UPDATE_CLIENTS); // :71 - age reset
                }
            } else {
                bid = level.getBlockState(pos); // :75
                level.setBlock(pos, bid.setValue(AGE, (var7 + 1) & 0xF), Block.UPDATE_CLIENTS); // :76 - four-bit nibble
            }
        }
    }

    /**
     * Pick-block: {@code quinoa}. {@code itemPicked}/{@code getSeedItem}/{@code getCropItem}
     * (:92-102) were dead; PORT: the seed item instead of {@code BlockReed}'s sugar cane, as the
     * catalogue's port note asks.
     */
    @Override
    public ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state) {
        return new ItemStack(CropBlocks.item("quinoa"));
    }

    // Drops (:81-90): quinoa, 3 + rand(3) for quinoa_3, nothing for the others →
    // data/orespawn/loot_table/blocks/quinoa_<n>.json.
}
