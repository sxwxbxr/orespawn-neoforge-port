package com.swbr.orespawn.block.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.BlockRice} (BlockRice.java:10-46), id {@code rice_plant}. Line for
 * line {@link BlockRadish} with {@code rice} as seed and crop item (:31-37): 2..5 rice per harvest
 * (:27-29), ripe or not (R18), plus the {@code 3 + fortune} bonus rolls at age 7 (loot table
 * {@code rice_plant.json}).
 *
 * <p>The item plants it on Crystal Grass only ({@code new ItemRadish(…, MyRicePlant, CrystalGrass)},
 * OreSpawnMain.java:1565; verhalten/itemblock-03.md); the block itself also stands on farmland like
 * every {@code BlockCrops}.
 */
public class BlockRice extends CropBlock {

    public static final MapCodec<BlockRice> CODEC = simpleCodec(BlockRice::new);

    public static final BlockBehaviour.Properties PROPERTIES = BlockRadish.PROPERTIES;

    public BlockRice(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockRice> codec() {
        return CODEC;
    }

    /** {@code func_149866_i} (:31-33). */
    @Override
    protected ItemLike getBaseSeedId() {
        return CropBlocks.item("rice");
    }

    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(CropBlocks.CRYSTAL_GRASS);
    }
}
