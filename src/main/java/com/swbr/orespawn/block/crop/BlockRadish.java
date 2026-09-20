package com.swbr.orespawn.block.crop;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * Port of {@code danger.orespawn.BlockRadish} (BlockRadish.java:10-46), id {@code radish_plant}.
 * A vanilla {@code BlockCrops} whose seed and crop item are both {@code radish}
 * ({@link com.swbr.orespawn.item.crop.ItemRadish}).
 *
 * <p>Growth, light and soil rules are the vanilla ones and come from {@link CropBlock}; the two
 * differences of the original are the drops and the four textures (see the loot table and the
 * blockstate file). {@code quantityDropped} is {@code 2 + rand(4)} (:27-29) and, because seed and
 * crop are the same item (:31-37), an <em>unripe</em> radish already yields 2..5 radishes - kept
 * 1:1 (R18, "unreife Ernte"). Ripe plants add {@code 3 + fortune} rolls of {@code rand(15) <= 7}
 * (vanilla {@code BlockCrops}, catalogue itemblock-02/03), written as {@code set_count 0} followed by
 * {@code apply_bonus binomial_with_bonus_count extra 3, probability 8/15} in
 * {@code data/orespawn/loot_table/blocks/radish_plant.json}. The {@code set_count 0} matters:
 * {@code binomial_with_bonus_count} only <em>adds</em> to the entry's count, and an entry starts at 1 -
 * without it the ripe plant handed out one seed too many (found by the W02 GameTests).
 */
public class BlockRadish extends CropBlock {

    public static final MapCodec<BlockRadish> CODEC = simpleCodec(BlockRadish::new);

    /** {@code BlockCrops}: {@code Material.plants}, no hardness, grass step sound, random ticks. */
    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.GRASS)
            .pushReaction(PushReaction.DESTROY);

    public BlockRadish(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockRadish> codec() {
        return CODEC;
    }

    /** {@code func_149866_i} (getSeedItem, :31-33) - the pick-block item. */
    @Override
    protected ItemLike getBaseSeedId() {
        return CropBlocks.item("radish");
    }

    /**
     * Farmland (vanilla {@code BlockCrops}) or Crystal Grass, whose {@code canSustainPlant} said yes
     * to everything (CrystalGrass.java:58-60) and is where {@code rice} planted the sister crop.
     */
    @Override
    protected boolean mayPlaceOn(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || state.is(CropBlocks.CRYSTAL_GRASS);
    }
}
