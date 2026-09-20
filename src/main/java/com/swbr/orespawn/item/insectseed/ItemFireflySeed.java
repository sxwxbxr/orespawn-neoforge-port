package com.swbr.orespawn.item.insectseed;

import com.swbr.orespawn.item.crop.ItemSeedFood;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemFireflySeed} (ItemFireflySeed.java:9-20): {@code firefly_seed}
 * "Firefly Plant", an {@code ItemSeeds} that plants {@code firefly_plant} (OreSpawnMain.java:1557). Not
 * edible; creative tab {@code tabDecorations} (:13). Planting is {@link ItemSeedFood}'s, see
 * {@link ItemButterflySeed}.
 */
public class ItemFireflySeed extends ItemSeedFood {

    /** {@code ItemFireflySeed(id, crop, soil)} (:11-14). */
    public ItemFireflySeed(final Block crop, final Item.Properties props) {
        super(crop, props);
    }
}
