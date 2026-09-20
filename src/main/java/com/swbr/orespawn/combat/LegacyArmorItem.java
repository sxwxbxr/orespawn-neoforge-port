package com.swbr.orespawn.combat;

/**
 * Marker for an armor item whose wearer gets the 1.7.10 armor formula (docs/DECISIONS.md, R5).
 *
 * <p>The item tag {@link LegacyArmorFormula#LEGACY_ARMOR} ({@code orespawn:legacy_armor}) does the
 * same job from data; W03 may use either. The interface exists so that a port of
 * {@code ItemOreSpawnArmor} can opt in without a datagen round trip, and so that the check works in
 * a unit test that has no tag registry.
 */
public interface LegacyArmorItem {
}
