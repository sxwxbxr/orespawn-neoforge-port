package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemTomato} (ItemTomato.java:8-18): {@code tomato_seed} "Tomato",
 * hunger 4, saturation 0.55, plants {@code tomato_0} on farmland (OreSpawnMain.java:1581).
 * Behaviour is {@link ItemSeedFood}; creative tab {@code tabFood}.
 */
public class ItemTomato extends ItemSeedFood {

    /** {@code ItemTomato(id, hunger, saturation, crop, soil)} (ItemTomato.java:10-12). */
    public ItemTomato(final Block crop, final int hunger, final float saturation, final Item.Properties props) {
        super(crop, props.food(food(hunger, saturation)));
    }
}
