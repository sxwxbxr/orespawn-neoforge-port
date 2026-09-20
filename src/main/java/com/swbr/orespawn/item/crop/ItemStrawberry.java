package com.swbr.orespawn.item.crop;

import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemStrawberry} (ItemStrawberry.java:7-17): plain
 * {@code ItemFood(hunger, saturation, wolfMeat = false)} with no effect and no other logic. Three
 * items of this class (OreSpawnMain.java:1547, :1560, :1561):
 *
 * <table>
 * <tr><th>id</th><th>name</th><th>hunger</th><th>saturation</th></tr>
 * <tr><td>{@code strawberry}</td><td>Strawberry</td><td>2</td><td>0.65</td></tr>
 * <tr><td>{@code cherries}</td><td>Cherries</td><td>3</td><td>0.45</td></tr>
 * <tr><td>{@code peach}</td><td>Peach</td><td>4</td><td>0.55</td></tr>
 * </table>
 *
 * Creative tab {@code tabFood} (from {@code ItemFood}).
 */
public class ItemStrawberry extends Item {

    /** {@code ItemStrawberry(id, hunger, saturation, wolfMeat)} (ItemStrawberry.java:9-11). */
    public ItemStrawberry(final int hunger, final float saturation, final Item.Properties props) {
        super(props.food(ItemSeedFood.food(hunger, saturation)));
    }
}
