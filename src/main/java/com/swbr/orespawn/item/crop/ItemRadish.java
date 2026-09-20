package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemRadish} (ItemRadish.java:8-18): an {@code ItemSeedFood} that
 * plants its crop and can be eaten. Two items of this class exist (OreSpawnMain.java:1559, :1565):
 *
 * <table>
 * <tr><th>id</th><th>name</th><th>hunger</th><th>saturation</th><th>plants</th><th>soil</th></tr>
 * <tr><td>{@code radish}</td><td>Radish</td><td>2</td><td>0.45</td><td>{@code radish_plant}</td><td>farmland</td></tr>
 * <tr><td>{@code rice}</td><td>Rice</td><td>5</td><td>0.65</td><td>{@code rice_plant}</td><td>Crystal Grass</td></tr>
 * </table>
 *
 * The class added nothing but its icon; behaviour is {@link ItemSeedFood}. Creative tab
 * {@code tabFood} (from {@code ItemFood}).
 */
public class ItemRadish extends ItemSeedFood {

    /**
     * {@code ItemRadish(id, hunger, saturation, crop, soil)} (ItemRadish.java:10-12) without the
     * numeric id and the unused soil.
     */
    public ItemRadish(final Block crop, final int hunger, final float saturation, final Item.Properties props) {
        super(crop, props.food(food(hunger, saturation)));
    }
}
