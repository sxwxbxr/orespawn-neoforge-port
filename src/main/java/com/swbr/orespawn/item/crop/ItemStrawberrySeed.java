package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemStrawberrySeed} (ItemStrawberrySeed.java:9-20):
 * {@code strawberry_seed} "Strawberry Plant", an {@code ItemSeeds} that plants
 * {@code strawberry_plant} on farmland (OreSpawnMain.java:1549). Not edible. Creative tab
 * {@code tabDecorations} (:13) - the only seed of this wave not on the food tab.
 *
 * <p>{@code ItemSeeds.onItemUse} is line for line {@code ItemSeedFood.onItemUse}, so the planting
 * comes from {@link ItemSeedFood}; without a food component the eating fallback never triggers.
 */
public class ItemStrawberrySeed extends ItemSeedFood {

    /** {@code ItemStrawberrySeed(id, crop, soil)} (ItemStrawberrySeed.java:11-14). */
    public ItemStrawberrySeed(final Block crop, final Item.Properties props) {
        super(crop, props);
    }
}
