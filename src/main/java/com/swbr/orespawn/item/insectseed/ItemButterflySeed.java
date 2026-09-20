package com.swbr.orespawn.item.insectseed;

import com.swbr.orespawn.item.crop.ItemSeedFood;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemButterflySeed} (ItemButterflySeed.java:9-20): {@code butterfly_seed}
 * "Butterfly Plant", an {@code ItemSeeds} that plants {@code butterfly_plant} (OreSpawnMain.java:1551).
 * Not edible; creative tab {@code tabDecorations} (:13).
 *
 * <p>{@code ItemSeeds.onItemUse} is line for line {@code ItemSeedFood.onItemUse}, so the planting comes
 * from {@link ItemSeedFood} as for {@code ItemStrawberrySeed} (W02); without a food component the eating
 * fallback never triggers. The soil argument ({@code Blocks.farmland}) was not read by the Forge method.
 */
public class ItemButterflySeed extends ItemSeedFood {

    /** {@code ItemButterflySeed(id, crop, soil)} (:11-14). */
    public ItemButterflySeed(final Block crop, final Item.Properties props) {
        super(crop, props);
    }
}
