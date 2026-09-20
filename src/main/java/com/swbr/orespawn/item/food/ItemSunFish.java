package com.swbr.orespawn.item.food;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

/**
 * Port of {@code danger.orespawn.ItemSunFish}: an always-edible {@code ItemFood}
 * (ItemSunFish.java:14) whose {@code onFoodEaten} (:17-42) first calls {@code super} and then,
 * server-side only, applies potion effects chosen by an identity check against the six
 * {@code OreSpawnMain} fields the class was instantiated for. The port replaces the identity
 * check with a constructor parameter ({@link Variant}); the effects are the same instances in the
 * same order and reach the player through {@link FoodProperties#effects()}, which
 * {@code LivingEntity.addEatEffect} applies server-side only (LivingEntity.java:3568-3576) -
 * the same {@code !world.isRemote} guard the original had.
 *
 * <p>Six instances (OreSpawnMain.java:1372, :1511, :1512, :1514, :1562, :1563):
 * {@code sunfish} 6/0.6, {@code buttercandy} 4/0.5, {@code cookedbacon} 14/1.5,
 * {@code cookedcrabmeat} 6/0.75, {@code crystalapple} 5/0.85, {@code heart} 8/0.95.
 * The tab is Food (inherited from {@code ItemFood}, see {@link ItemPopcorn}).
 */
public class ItemSunFish extends Item {

    /**
     * Which {@code OreSpawnMain} field the original compared {@code this} against, with the
     * effects that branch added. Durations in ticks, then amplifier, exactly as in the source.
     */
    public enum Variant {
        /** {@code MySunFish} (ItemSunFish.java:19-21). */
        SUN_FISH(List.of(
                () -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0))),
        /** {@code MyButterCandy} (:22-25). */
        BUTTER_CANDY(List.of(
                () -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2000, 0),
                () -> new MobEffectInstance(MobEffects.JUMP, 2000, 0))),
        /** {@code MyBacon} (:26-29). */
        BACON(List.of(
                () -> new MobEffectInstance(MobEffects.REGENERATION, 2000, 0),
                () -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2000, 0))),
        /** {@code MyCrabMeat}: no branch in {@code onFoodEaten}, so no effect - only always edible. */
        CRAB_MEAT(List.of()),
        /** {@code MyCrystalApple} (:30-33). */
        CRYSTAL_APPLE(List.of(
                () -> new MobEffectInstance(MobEffects.REGENERATION, 3000, 0),
                () -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3000, 0))),
        /** {@code MyLove} (:34-41). */
        LOVE(List.of(
                () -> new MobEffectInstance(MobEffects.REGENERATION, 6000, 3),
                () -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2),
                () -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 2),
                () -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1),
                () -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5000, 0),
                () -> new MobEffectInstance(MobEffects.JUMP, 5000, 0)));

        private final List<Supplier<MobEffectInstance>> effects;

        Variant(List<Supplier<MobEffectInstance>> effects) {
            this.effects = effects;
        }

        /** The effects this branch of {@code onFoodEaten} added, in source order. */
        public List<Supplier<MobEffectInstance>> effects() {
            return effects;
        }
    }

    private final Variant variant;

    /**
     * @param variant            the {@code OreSpawnMain} field this instance stood for
     * @param heal               1.7.10 {@code healAmount}
     * @param saturationModifier 1.7.10 {@code saturationModifier} (see {@link FoodValues})
     * @param props              properties from the registry holder
     */
    public ItemSunFish(Variant variant, int heal, float saturationModifier, Item.Properties props) {
        super(props.food(food(variant, heal, saturationModifier)));
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    private static FoodProperties food(Variant variant, int heal, float saturationModifier) {
        // setAlwaysEdible() (ItemSunFish.java:14): edible at full hunger, like the original.
        FoodProperties.Builder builder = FoodValues.of(heal, saturationModifier).alwaysEdible();
        for (Supplier<MobEffectInstance> effect : variant.effects()) {
            // Probability 1: the original applied the branch's effects unconditionally.
            builder.effect(effect, 1.0f);
        }
        return builder.build();
    }
}
