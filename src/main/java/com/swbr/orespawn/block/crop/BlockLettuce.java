package com.swbr.orespawn.block.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.BlockLettuce} (BlockLettuce.java:11-64): lettuce in four one-block
 * stages, ids {@code lettuce_0} .. {@code lettuce_3} ("Lettuce Plant", OreSpawnMain.java:1582-1585),
 * planted by {@code lettuce_seed} ({@link com.swbr.orespawn.item.crop.ItemLettuce}). The original
 * registered four instances of this class, one per stage ({@code MyLettucePlant1..4}); the port
 * constructs four blocks of this class with a {@code stage} index.
 *
 * <p>Base {@code BlockReed}: {@code Material.plants}, grass step sound, no collision box, cross
 * model; bounds 0.125..0.875 in x and z, full height (:14-15); random ticks (:16); no hardness.
 * The metadata is the age counter, kept as {@link #AGE} 0..15 (4 bits, as stored in 1.7.10).
 */
public class BlockLettuce extends Block {

    public static final MapCodec<BlockLettuce> CODEC = simpleCodec(p -> new BlockLettuce(0, p));

    /** The 4-bit block metadata: the age counter ({@code var7 & 0xFF}, :29-30). */
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;

    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.GRASS)
            .pushReaction(PushReaction.DESTROY);

    /** {@code setBlockBounds(0.125, 0, 0.125, 0.875, 1, 0.875)} (:14-15). */
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    /** {@code MyLettucePlant1..4} by manifest id. */
    static final ResourceKey<Block>[] STAGES = stages("lettuce_");

    @SuppressWarnings("unchecked")
    static ResourceKey<Block>[] stages(final String prefix) {
        final ResourceKey<Block>[] keys = new ResourceKey[4];
        for (int i = 0; i < 4; ++i) {
            keys[i] = CropBlocks.blockKey(prefix + i);
        }
        return keys;
    }

    /** 0..3 = {@code MyLettucePlant1..4}. */
    private final int stage;

    public BlockLettuce(final int stage, final BlockBehaviour.Properties properties) {
        super(properties);
        this.stage = stage;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public int getStage() {
        return this.stage;
    }

    @Override
    protected MapCodec<BlockLettuce> codec() {
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

    /** {@code canPlaceBlockAt} (:19-22): below is a lettuce stage, grass, dirt or farmland. */
    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final BlockState bid = level.getBlockState(pos.below());
        return !bid.isAir() && (isLettuce(bid) || CropBlocks.isGrassDirtOrFarmland(bid));
    }

    static boolean isLettuce(final BlockState state) {
        return state.is(STAGES[0]) || state.is(STAGES[1]) || state.is(STAGES[2]) || state.is(STAGES[3]);
    }

    /**
     * Vanilla {@code BlockReed.onNeighborBlockChange} → {@code checkBlockCoordValid}: a block whose
     * support went drops its items and becomes air. Only block updates (flag 1) triggered it in
     * 1.7.10, so this is {@code neighborChanged}, not {@code updateShape}.
     */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos,
                                   final Block neighborBlock, final BlockPos neighborPos, final boolean movedByPiston) {
        if (!state.canSurvive(level, pos)) {
            Block.dropResources(state, level, pos); // dropBlockAsItem(world, x, y, z, meta, 0)
            level.removeBlock(pos, false); // setBlockToAir
        }
    }

    /**
     * {@code updateTick} (:24-47), server only (:26-28): with {@code age >= 4} the stage advances
     * with age 0 (:31-42), otherwise the age counts up (:43-46). Five effective random ticks per
     * stage; {@code lettuce_3} stays where it is.
     */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        int var7 = state.getValue(AGE); // :29-30, var7 &= 0xFF
        if (var7 >= 4) { // :31
            if (this.stage == 0) {
                level.setBlock(pos, CropBlocks.block(STAGES[1]).defaultBlockState(), Block.UPDATE_CLIENTS); // :34
            } else if (this.stage == 1) {
                level.setBlock(pos, CropBlocks.block(STAGES[2]).defaultBlockState(), Block.UPDATE_CLIENTS); // :37
            } else if (this.stage == 2) {
                level.setBlock(pos, CropBlocks.block(STAGES[3]).defaultBlockState(), Block.UPDATE_CLIENTS); // :40
            }
        } else {
            // :45 - setBlock(x, y, z, bid, var7 + 1, 2); the metadata nibble kept four bits.
            level.setBlock(pos, state.setValue(AGE, (var7 + 1) & 0xF), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Pick-block. PORT: the original inherited {@code BlockReed.getItem} and handed out sugar cane
     * (verhalten/itemblock-02.md); the port hands out the planting item, as the catalogue's port
     * note asks for the sister crops.
     */
    @Override
    public ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state) {
        return new ItemStack(CropBlocks.item("lettuce_seed"));
    }

    // Drops (:49-58): lettuce_seed, 2 + rand(3) for lettuce_3, nothing for the others →
    // data/orespawn/loot_table/blocks/lettuce_<n>.json.
}
