package com.swbr.orespawn.block.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.BlockPizza} (BlockPizza.java:13-122): a placed pizza with six
 * slices, eaten by right- <em>or</em> left-click. Block id {@code pizza}, item {@code pizza_item}
 * ({@link com.swbr.orespawn.item.crop.ItemPizza}).
 *
 * <p>Original values: {@code Material.cake}, {@code setTickRandomly(true)} without an
 * {@code updateTick} (:23-24), no hardness (0, breaks instantly), default stone step sound. The
 * metadata counted the eaten slices; here that is {@link #BITES} 0..5.
 */
public class BlockPizza extends Block {

    public static final MapCodec<BlockPizza> CODEC = simpleCodec(BlockPizza::new);

    /** Eaten slices, the block metadata of the original (0..5; 6 removes the block, :92-93). */
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 5);

    /** {@code Material.cake}, no hardness, stone step sound; tickRandomly is dropped because no
     *  {@code updateTick} exists (BlockPizza.java:23-24). */
    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            .instabreak()
            .sound(SoundType.STONE);

    /**
     * {@code setBlockBoundsBasedOnState} (:27-33): x from {@code (1 + 2*meta)/16} to 15/16, z from
     * 1/16 to 15/16, height 0.25. Also the selection box (:53-60).
     */
    private static final VoxelShape[] SHAPE_BY_BITE = shapes(4.0);

    /** {@code getCollisionBoundingBoxFromPool} (:41-47): the same box, 1/16 lower on top. */
    private static final VoxelShape[] COLLISION_BY_BITE = shapes(3.0);

    static VoxelShape[] shapes(final double height) {
        final VoxelShape[] shapes = new VoxelShape[6];
        for (int l = 0; l < 6; ++l) {
            shapes[l] = Block.box(1 + l * 2, 0.0, 1.0, 15.0, height, 15.0);
        }
        return shapes;
    }

    public BlockPizza(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected MapCodec<BlockPizza> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                  final CollisionContext context) {
        return SHAPE_BY_BITE[state.getValue(BITES)];
    }

    @Override
    protected VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                           final CollisionContext context) {
        return COLLISION_BY_BITE[state.getValue(BITES)];
    }

    /**
     * {@code onBlockActivated} (:79-82): eat a slice and report the click as handled, whatever was
     * in the hand and whether or not the player could eat.
     *
     * <p>PORT: 1.7.10 had one hand. 1.21.1 asks the block once per hand, main hand first; answering
     * here for every hand keeps the original "return true" semantics (the held item is never used
     * on the pizza) and prevents a second bite from the off hand.
     */
    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level,
                                              final BlockPos pos, final Player player, final InteractionHand hand,
                                              final BlockHitResult hitResult) {
        this.eatPizzaSlice(level, pos, state, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** {@code onBlockClicked} (:84-86): a left-click eats too - and, with hardness 0, then breaks the block. */
    @Override
    protected void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        this.eatPizzaSlice(level, pos, state, player);
    }

    /**
     * {@code eatPizzaSlice} (:88-99). Runs on both sides like the original (no {@code isRemote}
     * check there either): 4 hunger and 0.2 saturation, then the next slice or air. No sound, no
     * statistic, no game event.
     *
     * <p>{@code canEat(false)} (:89) was {@code (false || needFood()) && !capabilities.disableDamage} in
     * 1.7.10: a creative player never ate a slice, nor did a full one. PORT: 1.21.1's
     * {@code Player.canEat} is {@code abilities.invulnerable || flag || needsFood()} - true in creative -
     * so the original condition is written out instead of calling it.
     */
    private void eatPizzaSlice(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        if (player.getFoodData().needsFood() && !player.getAbilities().invulnerable) { // :89
            player.getFoodData().eat(4, 0.2f); // :90
            final int l = state.getValue(BITES) + 1; // :91
            if (l >= 6) { // :92
                level.removeBlock(pos, false); // :93 setBlockToAir
            } else {
                level.setBlock(pos, state.setValue(BITES, l), Block.UPDATE_CLIENTS); // :96 flag 2
            }
        }
    }

    /**
     * {@code canPlaceBlockAt} (:101-103) → {@code canBlockStay} (:111-113): the block below must be
     * a normal cube. Checked at placement only: the original's {@code onNeighborBlockChange}
     * (:105-109) carried the 1.6 {@code int} signature and never ran, so a pizza whose support is
     * removed stays where it is (verhalten/itemblock-02.md; R18). No {@code updateShape} here for
     * the same reason.
     *
     * <p>PORT: {@code isNormalCube} (opaque material, normal render, no redstone power) has no
     * exact 1.21.1 twin; {@code isSolidRender} (occluding full cube) is the closest.
     */
    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below());
    }

    // Drops: getItemDropped = pizza_item, quantityDropped = 0 (:115-121) → empty loot table
    // data/orespawn/loot_table/blocks/pizza.json. Pick-block yields pizza_item through the
    // BlockItem mapping, as Block.getItem did in 1.7.10.
}
