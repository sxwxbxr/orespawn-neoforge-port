package com.swbr.orespawn.item.insectseed;

import com.swbr.orespawn.item.crop.ItemSeedFood;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemMothSeed} (ItemMothSeed.java:9-20): {@code moth_seed} "Moth Plant",
 * an {@code ItemSeeds} that plants {@code moth_plant} (OreSpawnMain.java:1553). Not edible; creative tab
 * {@code tabDecorations} (:13). Planting is {@link ItemSeedFood}'s, see {@link ItemButterflySeed}.
 */
public class ItemMothSeed extends ItemSeedFood {

    /** {@code ItemMothSeed(id, crop, soil)} (:11-14). */
    public ItemMothSeed(final Block crop, final Item.Properties props) {
        super(crop, props);
    }
}
