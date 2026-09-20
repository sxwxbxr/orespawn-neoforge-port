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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.BlockDuctTape} (BlockDuctTape.java:13-147): a placed roll of duct
 * tape with six uses that repairs the held item by a sixth of its maximum durability per use, on
 * right- <em>or</em> left-click. Block id {@code ducttape}, item {@code ducttape_item}
 * ({@link com.swbr.orespawn.item.crop.ItemDuctTape}).
 *
 * <p>Original values: {@code Material.anvil} (immovable by pistons), {@code setTickRandomly(true)}
 * without an {@code updateTick} (:23-24), no hardness, default stone step sound. Shapes are those of
 * {@link BlockPizza} (:27-60).
 */
public class BlockDuctTape extends Block {

    public static final MapCodec<BlockDuctTape> CODEC = simpleCodec(BlockDuctTape::new);

    /** Uses so far, the block metadata of the original (0..5; 6 removes the block, :109-110). */
    public static final IntegerProperty USES = IntegerProperty.create("uses", 0, 5);

    /** {@code Material.anvil}: iron map colour, immovable ({@code setImmovableMobility}); no hardness. */
    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .instabreak()
            .sound(SoundType.STONE)
            .pushReaction(PushReaction.BLOCK);

    private static final VoxelShape[] SHAPE_BY_USE = BlockPizza.shapes(4.0);
    private static final VoxelShape[] COLLISION_BY_USE = BlockPizza.shapes(3.0);

    public BlockDuctTape(final BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(USES, 0));
    }

    @Override
    protected MapCodec<BlockDuctTape> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(USES);
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                  final CollisionContext context) {
        return SHAPE_BY_USE[state.getValue(USES)];
    }

    @Override
    protected VoxelShape getCollisionShape(final BlockState state, final BlockGetter level, final BlockPos pos,
                                           final CollisionContext context) {
        return COLLISION_BY_USE[state.getValue(USES)];
    }

    /**
     * {@code onBlockActivated} (:79-82). Answered for every hand so the click is always "handled"
     * as the original's {@code return true} - see {@link BlockPizza#useItemOn}. The item repaired is
     * the one in the main hand ({@code inventory.getCurrentItem()}, :90), whichever hand asks.
     */
    @Override
    protected ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level,
                                              final BlockPos pos, final Player player, final InteractionHand hand,
                                              final BlockHitResult hitResult) {
        this.eatDuctTapeSlice(level, pos, state, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** {@code onBlockClicked} (:84-86): a left-click repairs too - and then breaks the block (hardness 0). */
    @Override
    protected void attack(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        this.eatDuctTapeSlice(level, pos, state, player);
    }

    /**
     * {@code eatDuctTapeSlice} (:88-119), on both sides like the original:
     * <ol>
     *   <li>held stack (main hand) with a count of exactly 1 (:90-91);</li>
     *   <li>{@code cd = maxDamage / 6}, at least 1 (:92-98);</li>
     *   <li>only a damaged item is repaired (:99-100): damage minus {@code cd}, floor 0 (:101-107);</li>
     *   <li>then the next use, or air after the sixth (:108-114).</li>
     * </ol>
     * An undamaged item consumes nothing; an item without durability ({@code maxDamage == 0}) does
     * nothing at all.
     */
    private void eatDuctTapeSlice(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        if (player != null) { // :89
            final ItemStack var2 = player.getMainHandItem(); // :90
            if (!var2.isEmpty() && var2.getCount() == 1) { // :91
                int cd = var2.getMaxDamage(); // :92
                int fd = 0;
                if (cd > 0) { // :94
                    cd /= 6; // :95
                    if (cd < 1) {
                        cd = 1; // :96-98
                    }
                    fd = var2.getDamageValue(); // :99
                    if (fd > 0) { // :100
                        if (fd > cd) {
                            fd -= cd; // :101-102
                        } else {
                            fd = 0; // :104-105
                        }
                        var2.setDamageValue(fd); // :107
                        final int l = state.getValue(USES) + 1; // :108
                        if (l >= 6) { // :109
                            level.removeBlock(pos, false); // :110 setBlockToAir
                        } else {
                            level.setBlock(pos, state.setValue(USES, l), Block.UPDATE_CLIENTS); // :113 flag 2
                        }
                    }
                }
            }
        }
    }

    /**
     * {@code canPlaceBlockAt} (:121-123) → {@code canBlockStay} (:131-133): the block below must
     * have a solid material. Placement only - the {@code onNeighborBlockChange(…, int)} override
     * (:125-129) never ran in 1.7.10, so the roll stays when its support goes (R18); no
     * {@code updateShape} here either. {@code isSolid()} is what {@code CakeBlock} uses for the
     * same test.
     */
    @Override
    @SuppressWarnings("deprecation")
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    // Drops: getItemDropped = null, quantityDropped = 0 (:135-141) → empty loot table
    // data/orespawn/loot_table/blocks/ducttape.json. getItem (:143-146, pick-block) = ducttape_item,
    // which the BlockItem mapping of ItemDuctTape provides.
}
