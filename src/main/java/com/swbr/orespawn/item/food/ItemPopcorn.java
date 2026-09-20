package com.swbr.orespawn.item.food;

import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemPopcorn}: an {@code ItemFood} with no extra behaviour
 * (ItemPopcorn.java:9-11) - not always edible, no effect. The original used the class for 15
 * foods (OreSpawnMain.java:1504-1519, :1535-1536; manifest class {@code ItemPopcorn}):
 *
 * <pre>
 * popcorn 1/0.5   popcorn_buttered 2/0.6   popcorn_buttered_salted 3/0.75   popcorn_bag 10/1.25
 * butter 1/0.5    corndog_cooked 16/2.5    corndog_raw 4/0.6                bacon 8/1.0
 * crabmeat 4/0.25 cheese 4/0.5             salad 10/0.95                    blt_sandwich 12/0.95
 * crabbypatty 16/2.35                      cookedpeacock 12/1.4             rawpeacock 6/0.7
 * </pre>
 *
 * <p>Neither Garden Salad nor A Crabby Patty returns a bowl or anything else in the original,
 * so there is no {@code usingConvertsTo}. The tab is Food: 1.7.10 {@code ItemFood}'s constructor
 * called {@code setCreativeTab(CreativeTabs.tabFood)} ({@code acx.<init>(IFZ)V} in the mapped
 * client jar, {@code abt.h} = {@code field_78039_h} = {@code tabFood}); the registry holder files
 * every instance under {@code OriginalTab.FOOD}.
 */
public class ItemPopcorn extends Item {

    /**
     * @param heal               1.7.10 {@code healAmount}
     * @param saturationModifier 1.7.10 {@code saturationModifier} (see {@link FoodValues})
     * @param props              properties from the registry holder
     */
    public ItemPopcorn(int heal, float saturationModifier, Item.Properties props) {
        super(props.food(FoodValues.of(heal, saturationModifier).build()));
    }
}
