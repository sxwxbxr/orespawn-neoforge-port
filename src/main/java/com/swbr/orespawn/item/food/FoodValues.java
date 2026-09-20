package com.swbr.orespawn.item.food;

import net.minecraft.world.food.FoodProperties;

/**
 * The 1.7.10 {@code ItemFood(heal, saturationModifier, wolfFood)} triple as 1.21.1
 * {@link FoodProperties}.
 *
 * <p><b>The saturation formula did not change - the modifier is copied, not converted.</b>
 * Verified in both directions, not assumed:
 * <ul>
 *   <li>1.7.10 {@code FoodStats.addStats(int, float)} ({@code zr.a(IF)V} in
 *       {@code reference/jar/mcp/client-1.7.10.jar}, {@code javap -c}): {@code foodLevel =
 *       min(heal + foodLevel, 20)}, then {@code saturation = min(saturation + heal * mod * 2.0f,
 *       foodLevel)}.</li>
 *   <li>1.21.1 {@code FoodProperties.Builder.build()} stores
 *       {@code FoodConstants.saturationByModifier(nutrition, mod) = nutrition * mod * 2.0f}
 *       (FoodConstants.java:30-32) and {@code FoodData.add} clamps
 *       {@code saturation + that} to {@code [0, foodLevel]} (FoodData.java:20-23).</li>
 * </ul>
 * Same product, same cap. The eat duration is unchanged as well: 32 ticks in 1.7.10,
 * {@code eatSeconds = 1.6f} = 32 ticks here (FoodProperties.java:20, :48-50).
 *
 * <p>{@code wolfFood} is {@code false} for every OreSpawn food (manifest {@code ctor_args}),
 * which in 1.7.10 only kept the item out of {@code EntityWolf}'s breeding check; 1.21.1 decides
 * that through the {@code #minecraft:wolf_food} tag, which none of these items joins. Nothing
 * to carry over.
 */
final class FoodValues {

    private FoodValues() {}

    /**
     * @param heal               1.7.10 {@code healAmount} (half-drumsticks)
     * @param saturationModifier 1.7.10 {@code saturationModifier}, unchanged
     */
    static FoodProperties.Builder of(int heal, float saturationModifier) {
        return new FoodProperties.Builder().nutrition(heal).saturationModifier(saturationModifier);
    }
}
