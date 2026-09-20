package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemPizza} (ItemPizza.java:12-69): {@code pizza_item} "Pizza!",
 * stack size 1, creative tab {@code tabFood} (OreSpawnMain.java:1289), places the
 * {@link com.swbr.orespawn.block.crop.BlockPizza} block {@code pizza}.
 *
 * <p>{@code onItemUse} (:20-63) was a copy of vanilla {@code ItemReed.onItemUse}: offset by the
 * clicked side unless the clicked block is vine, tall grass or dead bush; {@code canPlayerEdit};
 * {@code canPlaceEntityOnSide} (replaceable target, entity collision, the block's own
 * {@code canPlaceBlockAt}); {@code setBlock} with flag 3; {@code onBlockPlacedBy};
 * the block's step sound at volume {@code (v + 1) / 2} and pitch {@code p * 0.8}; stack - 1. That is
 * {@code BlockItem.place} in 1.21.1 step for step, including the sound formula, so nothing is
 * overridden here.
 *
 * <p>PORT, two edges of the copied vanilla code that {@code BlockPlaceContext} handles differently:
 * (1) the original tested the snow <em>block</em> instead of the snow layer (:22, unlike
 * {@link ItemDuctTape}), which made placing against a snow block fail; 1.21.1 places on top of it.
 * (2) 1.7.10 played the block's <em>step</em> sound, 1.21.1 the place sound - both are the stone
 * sound type.
 */
public class ItemPizza extends BlockItem {

    /** {@code ItemPizza(id, block)} (ItemPizza.java:16-18) plus {@code setMaxStackSize(1)} (OreSpawnMain.java:1289). */
    public ItemPizza(final Block pizza, final Item.Properties props) {
        super(pizza, props.stacksTo(1));
    }
}
