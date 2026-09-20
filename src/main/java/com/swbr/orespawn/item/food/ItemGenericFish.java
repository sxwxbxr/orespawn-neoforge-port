package com.swbr.orespawn.item.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemGenericFish}: an {@code ItemFood} that is <em>not</em>
 * always edible (no {@code setAlwaysEdible}, ItemGenericFish.java:10-12) and whose
 * {@code onFoodEaten} (:16-21) calls {@code super} and then, server side, with
 * {@code world.rand.nextInt(4) == 1} - one chance in four - adds Hunger for 20 ticks, amplifier
 * 0. Six instances (OreSpawnMain.java:1414-1419): {@code greenfish} 3/0.5, {@code bluefish}
 * 4/0.4, {@code pinkfish} 4/0.6, {@code rockfish} 3/0.7, {@code woodfish} 5/0.7,
 * {@code greyfish} 5/0.5. Tab Food (inherited from {@code ItemFood}, see {@link ItemPopcorn}).
 *
 * <p>The effect goes through {@code FoodProperties.effects()} with probability 0.25:
 * {@code LivingEntity.addEatEffect} rolls {@code random.nextFloat() < probability} server-side
 * only (LivingEntity.java:3568-3576) - the same 1/4 and the same {@code !world.isRemote} guard,
 * drawn from the entity's random instead of the world's.
 */
public class ItemGenericFish extends Item {

    /**
     * @param heal               1.7.10 {@code healAmount}
     * @param saturationModifier 1.7.10 {@code saturationModifier} (see {@link FoodValues})
     * @param props              properties from the registry holder
     */
    public ItemGenericFish(int heal, float saturationModifier, Item.Properties props) {
        super(props.food(FoodValues.of(heal, saturationModifier)
                .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20, 0), 0.25f)
                .build()));
    }
}
