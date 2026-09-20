package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemLettuce} (ItemLettuce.java:8-18): {@code lettuce_seed}
 * "Lettuce", hunger 3, saturation 0.45, plants {@code lettuce_0} on farmland
 * (OreSpawnMain.java:1586). Behaviour is {@link ItemSeedFood}; creative tab {@code tabFood}.
 */
public class ItemLettuce extends ItemSeedFood {

    /** {@code ItemLettuce(id, hunger, saturation, crop, soil)} (ItemLettuce.java:10-12). */
    public ItemLettuce(final Block crop, final int hunger, final float saturation, final Item.Properties props) {
        super(crop, props.food(food(hunger, saturation)));
    }
}
