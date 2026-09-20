package com.swbr.orespawn.item.bow;

import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemIrukandjiArrow} (ItemIrukandjiArrow.java:8-18), registry id
 * {@code irukandjiarrow} ("Irukandji Arrow", OreSpawnMain.java:1413). Ammunition of the
 * {@link SkateBow}; tab Combat (:11), stack 64. No behaviour of its own: shooting is in
 * {@code SkateBow}, the dispenser in {@link MyDispenserBehaviorArrow}, the drop of a stuck arrow in
 * {@code entity.arrow.IrukandjiArrow}.
 *
 * <p>Not an {@code ArrowItem}: the original was a plain {@code Item}, and vanilla bows and
 * crossbows must not accept it as ammunition.
 */
public class ItemIrukandjiArrow extends Item {

    public ItemIrukandjiArrow(Item.Properties props) {
        super(props);
    }
}
