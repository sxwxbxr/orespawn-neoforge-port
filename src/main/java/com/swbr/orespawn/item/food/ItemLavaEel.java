package com.swbr.orespawn.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemLavaEel}: an always-edible {@code ItemFood}
 * (ItemLavaEel.java:14) whose {@code onFoodEaten} (:17-22) calls {@code super} and then, server
 * side, adds Fire Resistance for 600 ticks, amplifier 0 - unconditionally. One instance:
 * {@code lavaeel} "Lava Eel", 2/0.6 (OreSpawnMain.java:1373). Tab Food (inherited from
 * {@code ItemFood}, see {@link ItemPopcorn}).
 *
 * <p>The effect goes through {@code FoodProperties.effects()}, which
 * {@code LivingEntity.addEatEffect} applies server-side only (LivingEntity.java:3568-3576),
 * matching the original {@code !world.isRemote} guard.
 */
public class ItemLavaEel extends Item {

    /**
     * @param heal               1.7.10 {@code healAmount}
     * @param saturationModifier 1.7.10 {@code saturationModifier} (see {@link FoodValues})
     * @param props              properties from the registry holder
     */
    public ItemLavaEel(int heal, float saturationModifier, Item.Properties props) {
        super(props.food(FoodValues.of(heal, saturationModifier)
                .alwaysEdible()
                .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0), 1.0f)
                .build()));
    }
}
