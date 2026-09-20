package com.swbr.orespawn.block.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockStrawberry} (BlockStrawberry.java:10-46), id
 * {@code strawberry_plant}. A vanilla {@code BlockCrops} with seed {@code strawberry_seed} and crop
 * {@code strawberry} (:31-37) and {@code quantityDropped = 1 + rand(5)} (:27-29): 1..5 seeds while
 * unripe, 1..5 strawberries plus the {@code 3 + fortune} seed bonus rolls at age 7 (loot table
 * {@code strawberry_plant.json}). Growth and soil rules are {@link CropBlock}'s.
 */
public class BlockStrawberry extends CropBlock {

    public static final MapCodec<BlockStrawberry> CODEC = simpleCodec(BlockStrawberry::new);

    public static final BlockBehaviour.Properties PROPERTIES = BlockRadish.PROPERTIES;

    public BlockStrawberry(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockStrawberry> codec() {
        return CODEC;
    }

    /** {@code func_149866_i} (:31-33): the seed, not the fruit. */
    @Override
    protected ItemLike getBaseSeedId() {
        return CropBlocks.item("strawberry_seed");
    }

    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(CropBlocks.CRYSTAL_GRASS);
    }
}
