package com.swbr.orespawn.combat;

/**
 * The two pieces of 1.7.10 combat arithmetic this port has to reproduce (docs/DECISIONS.md, R4 and
 * R5), as plain static methods without a single Minecraft import so that unit tests and GameTests
 * can check the numbers directly.
 *
 * <p>Nothing in here decides <em>when</em> a formula applies - that is {@link LegacyArmorFormula}
 * and {@link CombatEvents}. This class only knows how to compute.
 */
public final class LegacyCombatMath {

    /**
     * The clamp on {@code Attributes.MAX_HEALTH} in 1.21.1 ({@code Attributes.java}: the range is
     * {@code 1.0 .. 1024.0}). Anything above it is silently cut off, not rejected.
     */
    public static final double MAX_ATTRIBUTE_HEALTH = 1024.0;

    /**
     * The divisor of the 1.7.10 armor formula ({@code EntityLivingBase.applyArmorCalculations}:
     * {@code damage * (25 - armor) / 25}). Twenty-five armor points block everything.
     */
    public static final int ARMOR_DIVIDER = 25;

    private LegacyCombatMath() {}

    // ------------------------------------------------------------------ R4: virtual health

    /**
     * The value to put into {@code Attributes.MAX_HEALTH} for an entity whose original maximum is
     * {@code originalMaxHealth}: the original if it fits, otherwise the clamp.
     */
    public static double attributeMaxHealth(double originalMaxHealth) {
        return Math.min(originalMaxHealth, MAX_ATTRIBUTE_HEALTH);
    }

    /**
     * {@return whether an entity with this original maximum needs virtual health at all} A
     * maximum at or below the clamp is stored 1:1 and scaled by exactly 1.
     */
    public static boolean needsScaling(double originalMaxHealth) {
        return originalMaxHealth > MAX_ATTRIBUTE_HEALTH;
    }

    /**
     * The factor between the original health scale and the attribute scale: {@code 1024 / original}
     * for entities above the clamp, {@code 1} for everything else. Every original damage number,
     * heal number and {@code setHealth} argument is multiplied by it on the way into the entity.
     */
    public static float scale(double originalMaxHealth) {
        return needsScaling(originalMaxHealth) ? (float) (MAX_ATTRIBUTE_HEALTH / originalMaxHealth) : 1.0F;
    }

    /**
     * A damage or heal amount in original units, converted to the attribute scale. Applied
     * <em>after</em> all original per-hit caps (Godzilla's 750, PurplePower's 10, ...), which live
     * in the entity classes and see original numbers.
     */
    public static float toAttributeScale(float originalAmount, double originalMaxHealth) {
        return originalAmount * scale(originalMaxHealth);
    }

    /**
     * A health value in the attribute scale, converted back to original units - what a head mirror,
     * a robot kit or a boss bar text should display, and what every original comparison such as
     * {@code getHealth() < maxHealth * 2 / 3} should see.
     */
    public static float toOriginalScale(float attributeAmount, double originalMaxHealth) {
        return needsScaling(originalMaxHealth)
                ? (float) (attributeAmount * originalMaxHealth / MAX_ATTRIBUTE_HEALTH)
                : attributeAmount;
    }

    // ------------------------------------------------------------------ R5: 1.7.10 armor formula

    /**
     * Damage left after the 1.7.10 armor calculation: {@code damage * (25 - armor) / 25}, never
     * below zero.
     *
     * <p>1.7.10 had no cap on {@code armor}; with 26 points the product went negative and
     * {@code damageEntity} clamped it with {@code Math.max(damage - absorption, 0)}. The result is
     * the same as clamping here: at 25 points and above no blockable damage gets through.
     */
    public static float damageAfterArmor(float damage, int armor) {
        // PORT: 1.7.10 clamped the negative product only after subtracting absorption, which let
        // armor above 25 *raise* the absorption amount by the negative remainder. That side effect
        // is a vanilla-1.7.10 quirk on top of the OreSpawn numbers and is not reproduced: the
        // damage that reaches the entity is zero either way.
        float remaining = damage * (float) (ARMOR_DIVIDER - armor) / (float) ARMOR_DIVIDER;
        return Math.max(remaining, 0.0F);
    }

    /**
     * The amount the 1.7.10 armor formula takes off {@code damage} - the number NeoForge's
     * {@code DamageContainer} wants as the {@code ARMOR} reduction.
     */
    public static float armorReduction(float damage, int armor) {
        return damage - damageAfterArmor(damage, armor);
    }

    /** {@return whether {@code armor} points block every blockable hit under the 1.7.10 formula} */
    public static boolean blocksEverything(int armor) {
        return armor >= ARMOR_DIVIDER;
    }
}
