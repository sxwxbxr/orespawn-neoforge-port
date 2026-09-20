package com.swbr.orespawn.entity.boss;

import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The Y ceiling of the flight and attack targets of {@code TheKing} and {@code TheQueen}. No original class: both
 * originals wrote the literal {@code 230} (TheKing.java:429-430, :466-467, :508-509, :848-849; TheQueen.java:522-523,
 * :595-596, :637-638, :836-837), 26 blocks under the 1.7.10 build height of 256.
 *
 * <p>PORT (DECISIONS R26, R21): in the six OreSpawn dimensions (Y 0-256, terrain from the ported generators) the
 * literal stays. Everywhere else the ceiling keeps its distance to the top instead: {@code getMaxBuildHeight() - 26},
 * which is 294 in the overworld. A cap of 230 there would park the boss below a player building upwards, out of its
 * 30-block melee reach.
 */
public final class RoyalCeiling {

    /** {@code 230}, the original literal. */
    public static final int LEGACY_CEILING = 230;
    /** {@code 256 - 230}: the air the literal left under the 1.7.10 build height. */
    public static final int HEADROOM = 26;

    private RoyalCeiling() {}

    /** The ceiling for a boss in {@code level}. */
    public static int of(final Level level) {
        final ResourceKey<Level> dim = level.dimension();
        if (dim.equals(OreSpawnTeleporter.UTOPIA) || dim.equals(OreSpawnTeleporter.MINING) || dim.equals(OreSpawnTeleporter.VILLAGE)
                || dim.equals(OreSpawnTeleporter.DANGER) || dim.equals(OreSpawnTeleporter.CRYSTAL) || dim.equals(OreSpawnTeleporter.CHAOS)) {
            return LEGACY_CEILING;
        }
        return level.getMaxBuildHeight() - HEADROOM;
    }
}
