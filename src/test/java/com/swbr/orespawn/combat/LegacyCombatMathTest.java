package com.swbr.orespawn.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The numbers behind docs/DECISIONS.md R4 and R5, checked without Minecraft on the classpath.
 * The GameTests of W01 check the same numbers end to end through {@code LivingEntity.hurt}.
 */
class LegacyCombatMathTest {

    private static final float EPS = 1e-4F;

    // ------------------------------------------------------------------ R4

    @Test
    void attributeMaxHealthClampsAt1024() {
        assertEquals(1024.0, LegacyCombatMath.attributeMaxHealth(7000));
        assertEquals(1024.0, LegacyCombatMath.attributeMaxHealth(1024));
        assertEquals(1000.0, LegacyCombatMath.attributeMaxHealth(1000));
        assertEquals(20.0, LegacyCombatMath.attributeMaxHealth(20));
    }

    @Test
    void onlyHealthAboveTheClampIsScaled() {
        assertTrue(LegacyCombatMath.needsScaling(1500));
        assertFalse(LegacyCombatMath.needsScaling(1024));
        assertFalse(LegacyCombatMath.needsScaling(1000));
        assertEquals(1.0F, LegacyCombatMath.scale(1024));
        assertEquals(1.0F, LegacyCombatMath.scale(20));
    }

    @Test
    void theKingTakesDamageAt1024Over7000() {
        // The W01 GameTest: damage on a 7000-health entity is scaled by 1024 / 7000.
        assertEquals(1024.0F / 7000.0F, LegacyCombatMath.scale(7000), EPS);
        assertEquals(100.0F * 1024.0F / 7000.0F, LegacyCombatMath.toAttributeScale(100.0F, 7000), EPS);
    }

    @Test
    void scalingRoundTripsToOriginalUnits() {
        double[] originals = {7000, 6000, 4000, 3000, 1500, 2000, 1100};
        for (double original : originals) {
            float stored = LegacyCombatMath.toAttributeScale(750.0F, original);
            assertEquals(750.0F, LegacyCombatMath.toOriginalScale(stored, original), 1e-2F, "original " + original);
        }
        // A full-health boss reads back as its original maximum.
        assertEquals(7000.0F, LegacyCombatMath.toOriginalScale(1024.0F, 7000), EPS);
    }

    @Test
    void fightLengthIsPreserved() {
        // The same fight in both scales must take the same number of hits. 45 per hit on 7000
        // does not land on an exact multiple, so float rounding cannot tip the count either way.
        double original = 7000;
        assertEquals(hitsToKill((float) original, 45.0F),
                hitsToKill((float) LegacyCombatMath.attributeMaxHealth(original),
                        LegacyCombatMath.toAttributeScale(45.0F, original)));
        assertEquals(156, hitsToKill((float) original, 45.0F));
    }

    private static int hitsToKill(float health, float perHit) {
        int hits = 0;
        while (health > 0.0F) {
            health -= perHit;
            hits++;
        }
        return hits;
    }

    // ------------------------------------------------------------------ R5

    @Test
    void armorFormulaMatches1710() {
        // damage * (25 - armor) / 25
        assertEquals(10.0F, LegacyCombatMath.damageAfterArmor(10.0F, 0), EPS);
        assertEquals(6.0F, LegacyCombatMath.damageAfterArmor(10.0F, 10), EPS);
        assertEquals(2.0F, LegacyCombatMath.damageAfterArmor(10.0F, 20), EPS);
        // The King at 21 armor: 16 % gets through, not the 80 %-capped vanilla value.
        assertEquals(1.6F, LegacyCombatMath.damageAfterArmor(10.0F, 21), EPS);
    }

    @Test
    void twentyFiveArmorBlocksEverything() {
        // The W01 GameTest: at armor 25 no blockable damage arrives.
        assertEquals(0.0F, LegacyCombatMath.damageAfterArmor(10.0F, 25), EPS);
        assertEquals(10.0F, LegacyCombatMath.armorReduction(10.0F, 25), EPS);
        assertTrue(LegacyCombatMath.blocksEverything(25));
        assertFalse(LegacyCombatMath.blocksEverything(24));
    }

    @Test
    void armorAboveTwentyFiveNeverHeals() {
        // Royal Guardian 42, Queen Scale 48: no cap in 1.7.10, but the remainder is clamped at zero.
        assertEquals(0.0F, LegacyCombatMath.damageAfterArmor(1000.0F, 42), EPS);
        assertEquals(0.0F, LegacyCombatMath.damageAfterArmor(1000.0F, 48), EPS);
        assertEquals(1000.0F, LegacyCombatMath.armorReduction(1000.0F, 48), EPS);
    }

    @Test
    void reductionPlusRemainderIsTheDamage() {
        for (int armor = 0; armor <= 30; armor++) {
            float damage = 37.5F;
            assertEquals(damage,
                    LegacyCombatMath.damageAfterArmor(damage, armor) + LegacyCombatMath.armorReduction(damage, armor),
                    EPS, "armor " + armor);
        }
    }
}
