package com.swbr.orespawn.entity;

/**
 * Marker for entities whose original overrides {@code canTriggerWalking()} with {@code false}
 * (DECISIONS R20). {@link com.swbr.orespawn.block.misc.Legacy#wasWalking} returns {@code false} for
 * them, so they fire no OreSpawn {@code stepOn} effect, as 1.7.10 {@code Entity.moveEntity} skipped
 * {@code Block.onEntityWalking} for them.
 *
 * <p>Why a marker: 1.21.1 calls {@code Block.stepOn} on every ground {@code move} regardless of
 * {@code getMovementEmission()}, and that method is protected, so a block cannot ask the entity. No
 * access transformer (R1); the pattern follows the {@code MyUtils} markers ({@code Ignoreable}).
 * Entities keep their {@code getMovementEmission()} override for the step sounds as well.
 *
 * <p>Originals returning {@code false} (reference/src-20.2): {@code Elevator} (W04, Elevator.java:136),
 * {@code EntityButterfly} (:235, inherited by {@code EntityLunaMoth}), {@code EntityMosquito} (:122),
 * {@code Firefly} (:143) (W05), {@code Brutalfly}, {@code Fairy}, {@code Ghost}, {@code GhostSkelly},
 * {@code GodzillaHead}, {@code KingHead}, {@code Mothra}, {@code PurplePower}, {@code WormLarge},
 * {@code WormMedium}, {@code WormSmall} (later waves). {@code EntityAnt} returns {@code true} and so do
 * its subclasses; they do not implement this.
 */
public interface NoStepTrigger {
}
