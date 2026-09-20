package com.swbr.orespawn.entity;

import javax.annotation.Nullable;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.SpawnGroupData;

/**
 * The group data every OreSpawn {@link AgeableMob} passes to {@code AgeableMob.finalizeSpawn}. No original class: it
 * stands for what 1.7.10's {@code SpawnerAnimals} did not do.
 *
 * <p>PORT (DECISIONS R26, BUGHUNT2 2.5): 1.21.1 lets the second and later members of a natural spawn group (chunk
 * generation and the running spawner alike), and mobs of a spawner block, start as babies with 5 % chance
 * ({@code AgeableMob.finalizeSpawn}). 1.7.10's {@code EntityAgeable} and {@code EntityAnimal} had no
 * {@code onSpawnWithEgg} that set a growing age, so no OreSpawn animal was ever born small outside breeding - and a
 * baby dinosaur, bird or mount would stay persistent forever. Data with {@code shouldSpawnBaby = false} keeps that.
 * A group data handed in by the caller is left alone.
 */
public final class LegacyAgeable {

    private LegacyAgeable() {
    }

    /** {@code null} becomes {@code AgeableMobGroupData(false)}; anything else is returned unchanged. */
    public static SpawnGroupData noBabies(@Nullable final SpawnGroupData spawnGroupData) {
        return spawnGroupData == null ? new AgeableMob.AgeableMobGroupData(false) : spawnGroupData;
    }
}
