package com.swbr.orespawn.util;

/**
 * Marker for OreSpawn creatures that hostile target searches may attack although they are not
 * {@link net.minecraft.world.entity.monster.Monster monsters}: the mod-owned part of
 * {@code MyUtils.isAttackableNonMob} (MyUtils.java:13-15).
 *
 * <p>The original named {@code Mothra} (W08), {@code Leon}, {@code Dragon}, {@code Spyro},
 * {@code GammaMetroid}, {@code Cephadrome}, {@code WaterDragon} (all W09), {@code Girlfriend},
 * {@code Boyfriend} (W04) and {@code Stinky} (W08). Each of them implements this interface.
 *
 * <p>Not needed on royalty: {@link MyUtils#isAttackableNonMob} still calls {@link MyUtils#isRoyalty}
 * exactly as the original did. Vanilla monsters and villagers are matched by class inside
 * {@code MyUtils}, so they do not need a marker either.
 *
 * <p>Trap kept from the original: {@code Mothra} extends {@code EntityButterfly}, which is
 * {@link Ignoreable}. Mothra is therefore both ignorable and attackable; which one wins depends on the
 * order of checks in the mob that asks (verhalten/core-02.md, MyUtils).
 */
public interface AttackableNonMob {
}
