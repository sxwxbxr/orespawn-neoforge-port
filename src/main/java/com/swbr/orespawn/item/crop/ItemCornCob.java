package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port of {@code danger.orespawn.ItemCornCob} (ItemCornCob.java:8-18). Two items of this class
 * (OreSpawnMain.java:1571, :1576):
 *
 * <table>
 * <tr><th>id</th><th>name</th><th>hunger</th><th>saturation</th><th>plants</th><th>soil</th></tr>
 * <tr><td>{@code corn_seed}</td><td>Corn</td><td>6</td><td>0.75</td><td>{@code corn_0}</td><td>farmland</td></tr>
 * <tr><td>{@code quinoa}</td><td>Quinoa</td><td>7</td><td>0.85</td><td>{@code quinoa_0}</td><td>Crystal Grass</td></tr>
 * </table>
 *
 * Behaviour is {@link ItemSeedFood}; creative tab {@code tabFood}.
 */
public class ItemCornCob extends ItemSeedFood {

    /** {@code ItemCornCob(id, hunger, saturation, crop, soil)} (ItemCornCob.java:10-12). */
    public ItemCornCob(final Block crop, final int hunger, final float saturation, final Item.Properties props) {
        super(crop, props.food(food(hunger, saturation)));
    }
}
