package com.swbr.orespawn.combat;

/**
 * Port of the 1.7.10 {@code getTotalArmorValue()} override (docs/DECISIONS.md, R5).
 *
 * <p>Eighty-one original entity classes override that method, and several of them do not return a
 * constant: The King returns {@code defense + 1 .. + 3} depending on remaining health and player
 * hits, Godzilla returns 25 once a large unknown is detected, Girlfriend sums the armor she wears.
 * An attribute cannot express that, so the override survives as this interface. Entities that do
 * not implement it get {@code getArmorValue()} - the (unclamped for their range, all at or below
 * 26) {@code ARMOR} attribute.
 *
 * <p>The value is an {@code int}, exactly as in 1.7.10, where {@code 25 - getTotalArmorValue()} was
 * integer arithmetic.
 */
public interface LegacyArmor {

    /** The armor points the 1.7.10 formula sees for this entity right now, in original units. */
    int getLegacyArmorValue();
}
