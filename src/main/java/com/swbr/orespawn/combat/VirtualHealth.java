package com.swbr.orespawn.combat;

import net.minecraft.world.entity.LivingEntity;

/**
 * Virtual health for entities whose original maximum lies above the 1.21.1 clamp of 1024
 * (docs/DECISIONS.md, R4): The King 7000, The Queen 6000, Mobzilla 4000, the three heads, Young
 * Adult Prince 3000, Young Prince and Robot Spider 1500 - and Kraken and Jeffery when the config
 * doubles them.
 *
 * <p>The contract for an implementing entity:
 * <ul>
 * <li>{@code createAttributes()} puts {@link LegacyCombatMath#attributeMaxHealth} of the original
 * value into {@code MAX_HEALTH}, never the original itself.</li>
 * <li>{@link #getOriginalMaxHealth()} returns the number the original class used - the config
 * value after its clamps, e.g. {@code TheKing_stats.health}.</li>
 * <li>The entity's own {@code hurt} override keeps working in original units (Godzilla caps at
 * 750, PurplePower at 10). {@link CombatEvents} scales the amount once, after {@code super.hurt}
 * has been reached, so every original cap has already run.</li>
 * <li>Code that reads or writes health directly - head mirrors, {@code setHealth(hp / 4 - 1)},
 * {@code heal(-1)}, robot kits, the girlfriend overlay - goes through the static helpers here and
 * stays in original units.</li>
 * </ul>
 *
 * <p>Boss bars need nothing: {@code getHealth() / getMaxHealth()} is the same ratio in both scales.
 */
public interface VirtualHealth {

    /**
     * The maximum health the original entity had, in original units. For most bosses that is the
     * config value; where code and config disagree, the code wins (R3).
     */
    double getOriginalMaxHealth();

    // ------------------------------------------------------------------ helpers for every caller

    /**
     * The original maximum of any living entity: the declared one for {@link VirtualHealth}
     * implementers, the attribute for everything else.
     */
    static double originalMaxHealth(LivingEntity entity) {
        return entity instanceof VirtualHealth virtual ? virtual.getOriginalMaxHealth() : entity.getMaxHealth();
    }

    /** The factor from original units to the stored attribute scale, {@code 1} for ordinary entities. */
    static float scale(LivingEntity entity) {
        return LegacyCombatMath.scale(originalMaxHealth(entity));
    }

    /** The entity's current health in original units - what an original comparison or display expects. */
    static float originalHealth(LivingEntity entity) {
        return LegacyCombatMath.toOriginalScale(entity.getHealth(), originalMaxHealth(entity));
    }

    /** An amount given in original units, converted to what {@code hurt}, {@code heal} or {@code setHealth} store. */
    static float toAttributeScale(LivingEntity entity, float originalAmount) {
        return LegacyCombatMath.toAttributeScale(originalAmount, originalMaxHealth(entity));
    }

    /** A stored health or damage amount, converted to original units. */
    static float toOriginalScale(LivingEntity entity, float attributeAmount) {
        return LegacyCombatMath.toOriginalScale(attributeAmount, originalMaxHealth(entity));
    }

    /**
     * {@code setHealth} with an argument in original units. Ports of {@code setHealth(x)} in the
     * original use this so that {@code setHealth(getHealth() / 4 - 1)} means the same thing.
     */
    static void setOriginalHealth(LivingEntity entity, float originalHealth) {
        entity.setHealth(toAttributeScale(entity, originalHealth));
    }

    /**
     * Copies one entity's health onto another in original units - the head entities mirror their
     * body this way. Both sides may or may not be virtual; the conversion is done per side.
     */
    static void mirrorHealth(LivingEntity from, LivingEntity to) {
        setOriginalHealth(to, originalHealth(from));
    }
}
