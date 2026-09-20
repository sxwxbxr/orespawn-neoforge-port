package com.swbr.orespawn.block.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * What OreSpawn's saplings inherited from vanilla 1.7.10 {@code BlockReed} and did not override
 * ({@code BlockCrystalPlant}, {@code BlockExperiencePlant}; the crop classes of the other W02 porter
 * have the same parent). 1.21.1 has no reed base class worth reusing, so the inherited half is here:
 *
 * <ul>
 *   <li>bounds 0.125..0.875 in x/z, full height, set again in each subclass constructor
 *       (BlockCrystalPlant.java:15-16, BlockExperiencePlant.java:14-15);</li>
 *   <li>no collision box ({@code getCollisionBoundingBoxFromPool} null), cross render type,
 *       {@code Material.plants} with grass step sound and "destroyed by piston" mobility;</li>
 *   <li>{@code onNeighborBlockChange} -> {@code checkBlockCoordValid}: if {@code canBlockStay} fails
 *       the block drops itself with its metadata and becomes air;</li>
 *   <li>{@code canBlockStay} is {@code canPlaceBlockAt}, which every subclass overrides with its own
 *       ground list ({@link #mayPlaceOn});</li>
 *   <li>{@code getItem} - the pick-block item - is <em>sugar cane</em>. The subclasses' {@code idPicked},
 *       {@code getSeedItem} and {@code getCropItem} carry 1.6 signatures and were never called
 *       (verhalten/itemblock-02.md). Kept 1:1 (DECISIONS R18): pick block on an OreSpawn sapling
 *       yields sugar cane.</li>
 * </ul>
 */
public abstract class ReedLikePlant extends Block {

    /** {@code setBlockBounds(0.5 - 0.375, 0, 0.5 - 0.375, 0.5 + 0.375, 1, 0.5 + 0.375)}. */
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    protected ReedLikePlant(Properties properties) {
        super(properties.noCollission().pushReaction(PushReaction.DESTROY));
    }

    /** {@code Blocks.grass} of 1.7.10. */
    protected static boolean isGrass(BlockState soil) {
        return soil.is(Blocks.GRASS_BLOCK);
    }

    /**
     * {@code Blocks.dirt} of 1.7.10, which held dirt, coarse dirt and podzol as metadata 0-2.
     *
     * <p>PORT (R22 with its 2026-09-14 addendum): the whole {@code #minecraft:dirt} tag, mycelium, moss,
     * mud and rooted dirt included. Every caller ({@code BlockCrystalPlant}, {@code BlockExperiencePlant},
     * {@code MyBlockFlower}) tests {@link #isGrass} in the same disjunction, so the grass block inside the
     * tag changes no result.
     */
    protected static boolean isDirt(BlockState soil) {
        return soil.is(BlockTags.DIRT);
    }

    /** {@code Blocks.farmland} of 1.7.10. */
    protected static boolean isFarmland(BlockState soil) {
        return soil.is(Blocks.FARMLAND);
    }

    /** The subclass's {@code canPlaceBlockAt}: the block below, already fetched. */
    protected abstract boolean mayPlaceOn(BlockState soil);

    /** {@code setTickRandomly(true)} in both subclass constructors. */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** {@code canBlockStay} = {@code canPlaceBlockAt}: {@code bid != air && (...)}. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        final BlockState bid = level.getBlockState(pos.below());
        return !bid.isAir() && this.mayPlaceOn(bid);
    }

    /** {@code BlockReed.onNeighborBlockChange} -> {@code checkBlockCoordValid}: drop and {@code setBlockToAir}. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!state.canSurvive(level, pos)) {
            dropResources(state, level, pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** {@code BlockReed.getItem} -> {@code Items.reeds}; see the class comment. */
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(Items.SUGAR_CANE);
    }
}
