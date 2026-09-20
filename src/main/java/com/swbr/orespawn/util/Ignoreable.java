package com.swbr.orespawn.util;

/**
 * Marker for creatures that target searches skip: {@code MyUtils.isIgnoreable} (MyUtils.java:17-19).
 *
 * <p>The original named {@code RockBase} (W04), {@code EntityAnt}, {@code EntityButterfly},
 * {@code EntityMosquito}, {@code Firefly}, {@code Termite} (all W05), {@code Dragonfly}, {@code Cricket},
 * {@code Cockateil}, {@code Ghost}, {@code GhostSkelly} (all W06) and {@code Elevator} (W04). Each of them
 * implements this interface, and their subclasses inherit it like the original {@code instanceof}
 * checks did: {@code EntityRedAnt}, {@code EntityRainbowAnt}, {@code EntityUnstableAnt} and
 * {@code Termite} through {@code EntityAnt}; {@code EntityLunaMoth} and {@code Mothra} through
 * {@code EntityButterfly}; {@code RubyBird} through {@code Cockateil}.
 *
 * <p>The spelling "Ignoreable" is the original's.
 */
public interface Ignoreable {
}
