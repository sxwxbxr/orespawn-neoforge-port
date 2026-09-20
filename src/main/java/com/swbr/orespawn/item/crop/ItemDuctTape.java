package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemDuctTape} (ItemDuctTape.java:12-69): {@code ducttape_item}
 * "Duct Tape!", stack size 1, creative tab {@code tabTools} (OreSpawnMain.java:1291), places the
 * {@link com.swbr.orespawn.block.crop.BlockDuctTape} block {@code ducttape}.
 *
 * <p>{@code onItemUse} (:20-63) is the {@code ItemReed} copy described at {@link ItemPizza}, here
 * with the correct snow-layer test (:22) and the block's <em>place</em> sound
 * ({@code stepSound.func_150496_b}, :58) - exactly what {@code BlockItem.place} does in 1.21.1, so
 * nothing is overridden.
 */
public class ItemDuctTape extends BlockItem {

    /** {@code ItemDuctTape(id, block)} (ItemDuctTape.java:16-18) plus {@code setMaxStackSize(1)} (OreSpawnMain.java:1291). */
    public ItemDuctTape(final Block ductTape, final Item.Properties props) {
        super(ductTape, props.stacksTo(1));
    }
}
