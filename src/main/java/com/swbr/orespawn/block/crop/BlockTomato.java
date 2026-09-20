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
 * Port of {@code danger.orespawn.BlockTomato} (BlockTomato.java:11-110): a tomato stalk that grows
 * upwards block by block. Ids {@code tomato_0} .. {@code tomato_3} ("Tomato Plant",
 * OreSpawnMain.java:1577-1580) = {@code MyTomatoPlant1..4}: growing tip, unripe stalk, half ripe,
 * ripe. Planted by {@code tomato_seed} ({@link com.swbr.orespawn.item.crop.ItemTomato}).
 *
 * <p>Base {@code BlockReed} as for {@link BlockLettuce}; bounds and properties are the same
 * (:17-19).
 *
 * <p><b>Target height (DECISIONS R18, "Zielhöhe neu gewürfelt"):</b> the original tried to keep a
 * per-plant maximum height in {@code meta >> 8}. Block metadata was four bits, so the value never
 * reached the world and {@code myMaxHeight} came out as 0 on every tick and was re-rolled
 * ({@code 3 + rand(3)}, :39-41). The port stores exactly the four bits the world kept
 * ({@link #AGE} 0..15) and re-rolls the target every tick, so the ripening threshold keeps jumping
 * between 3 and 4 as it did. {@code myMaxHeight} was also a field of the block singleton (:13); it
 * is overwritten before any read, so a local is the same thing.
 */
public class BlockTomato extends Block {

    public static final MapCodec<BlockTomato> CODEC = simpleCodec(p -> new BlockTomato(0, p));

    /** The 4-bit metadata: the age counter ({@code var7 & 0xFF}, :38). */
    public static final IntegerProperty AGE = BlockLettuce.AGE;

    public static final BlockBehaviour.Properties PROPERTIES = BlockLettuce.PROPERTIES;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    /** {@code MyTomatoPlant1..4} by manifest id. */
    static final ResourceKey<Block>[] STAGES = BlockLettuce.stages("tomato_");

    /** 0..3 = {@code MyTomatoPlant1..4}. */
    private final int stage;

    public BlockTomato(final int stage, final BlockBehaviour.Properties properties) {
        super(properties);
        this.stage = stage;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public int getStage() {
        return this.stage;
    }

    @Override
    protected MapCodec<BlockTomato> codec() {
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

    /** {@code canPlaceBlockAt} (:22-25): below is a tomato stage, grass, dirt or farmland. */
    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final BlockState bid = level.getBlockState(pos.below());
        return !bid.isAir() && (isTomato(bid) || CropBlocks.isGrassDirtOrFarmland(bid));
    }

    static boolean isTomato(final BlockState state) {
        return state.is(STAGES[0]) || state.is(STAGES[1]) || state.is(STAGES[2]) || state.is(STAGES[3]);
    }

    /** Vanilla {@code BlockReed.onNeighborBlockChange}: drop and vanish when the support is gone; see {@link BlockLettuce#neighborChanged}. */
    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos,
                                   final Block neighborBlock, final BlockPos neighborPos, final boolean movedByPiston) {
        if (!state.canSurvive(level, pos)) {
            Block.dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    /**
     * {@code updateTick} (:27-81), server only (:30-32), only for Plant1 and Plant2 (:33-35).
     * <ol>
     *   <li>{@code myMaxHeight = meta >> 8} (always 0, see the class comment), {@code age = meta & 0xFF};
     *       0 → {@code 3 + OreSpawnRand.nextInt(3)} (:36-41).</li>
     *   <li>Only with air above (:42-43): {@code Height} = 1 + the run of tomato blocks below, up
     *       to nine; a Plant3 or Plant4 in that run pins {@code myMaxHeight = Height} (:44-56).</li>
     *   <li>{@code age >= 5 - myMaxHeight / 3} (:57): below target height, grow a Plant1 on top and
     *       become Plant2 (:58-61); otherwise ripen the stalk from here down for {@code myMaxHeight}
     *       blocks, Plant2 → Plant3 and Plant3 → Plant4, and reset this block's age (:62-74).</li>
     *   <li>Else age + 1 (:76-79).</li>
     * </ol>
     */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        int height = 1; // :28
        int dontGrow = 0; // :29
        if (this.stage != 0 && this.stage != 1) { // :33-35
            return;
        }
        int var7 = state.getValue(AGE); // :36
        int myMaxHeight = var7 >> 8; // :37 - four-bit metadata: always 0 (R18)
        var7 &= 0xFF; // :38
        if (myMaxHeight == 0) { // :39
            myMaxHeight = 3 + OreSpawn.OreSpawnRand.nextInt(3); // :40
        }
        BlockState bid = level.getBlockState(pos.above()); // :42
        if (bid.isAir()) { // :43
            for (int var8 = 1; var8 < 10; ++var8) { // :44
                bid = level.getBlockState(pos.below(var8)); // :45
                if (!isTomato(bid)) { // :46
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
                    // :59-60 - meta myMaxHeight << 8 == 0 in the world
                    level.setBlock(pos.above(), CropBlocks.block(STAGES[0]).defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(pos, CropBlocks.block(STAGES[1]).defaultBlockState(), Block.UPDATE_CLIENTS);
                } else {
                    for (int i = 0; i < myMaxHeight; ++i) { // :63
                        final BlockPos below = pos.below(i);
                        bid = level.getBlockState(below); // :64
                        if (bid.is(STAGES[1])) { // :65
                            level.setBlock(below, CropBlocks.block(STAGES[2]).defaultBlockState(), Block.UPDATE_CLIENTS); // :66
                        } else if (bid.is(STAGES[2])) { // :68
                            level.setBlock(below, CropBlocks.block(STAGES[3]).defaultBlockState(), Block.UPDATE_CLIENTS); // :69
                        }
                    }
                    // :72-73 - re-read (the loop may have changed this very block) and reset its age.
                    bid = level.getBlockState(pos);
                    level.setBlock(pos, bid.getBlock().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            } else {
                // :77-78 - setBlock(x, y, z, bid, myMaxHeight << 8 | var7 + 1, 2); the nibble kept four bits.
                bid = level.getBlockState(pos);
                level.setBlock(pos, bid.setValue(AGE, (var7 + 1) & 0xF), Block.UPDATE_CLIENTS);
            }
        }
    }

    /**
     * Pick-block: {@code tomato_seed}. The original's {@code itemPicked}/{@code getSeedItem}/
     * {@code getCropItem} (:94-104) matched no vanilla method and were dead; PORT: the seed item is
     * handed out instead of {@code BlockReed}'s sugar cane, as the catalogue's port note asks.
     */
    @Override
    public ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state) {
        return new ItemStack(CropBlocks.item("tomato_seed"));
    }

    // Drops (:83-92): tomato_seed, 2 + rand(4) for tomato_3, nothing for the others →
    // data/orespawn/loot_table/blocks/tomato_<n>.json.
}
