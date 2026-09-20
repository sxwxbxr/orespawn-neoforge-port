package com.swbr.orespawn.item.insectseed;

import com.swbr.orespawn.item.crop.ItemSeedFood;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemMosquitoSeed} (ItemMosquitoSeed.java:9-20): {@code mosquito_seed}
 * "Mosquito Plant", an {@code ItemSeeds} that plants {@code mosquito_plant} (OreSpawnMain.java:1555). Not
 * edible; creative tab {@code tabDecorations} (:13). Planting is {@link ItemSeedFood}'s, see
 * {@link ItemButterflySeed}.
 */
public class ItemMosquitoSeed extends ItemSeedFood {

    /** {@code ItemMosquitoSeed(id, crop, soil)} (:11-14). */
    public ItemMosquitoSeed(final Block crop, final Item.Properties props) {
        super(crop, props);
    }
}
