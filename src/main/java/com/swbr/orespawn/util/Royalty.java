package com.swbr.orespawn.util;

/**
 * Marker for the royal family: what {@code MyUtils.isRoyalty} (MyUtils.java:9-11) tested with a chain of
 * {@code instanceof} checks.
 *
 * <p>The original named nine classes: {@code ThePrince}, {@code ThePrinceTeen}, {@code ThePrinceAdult},
 * {@code ThePrincess}, {@code TheKing}, {@code KingHead}, {@code TheQueen}, {@code QueenHead} and
 * {@code PurplePower} (all W10). Each of them implements this interface; subclasses inherit it the way
 * {@code instanceof} covered them in 1.7.10 (catalogue README 4.1, point 1). No entity type tag on purpose:
 * a tag would lose that subclass semantics.
 *
 * <p>Only {@link net.minecraft.world.entity.LivingEntity living entities} count, see
 * {@link MyUtils#isRoyalty(net.minecraft.world.entity.Entity)}.
 */
public interface Royalty {
}
