package com.swbr.orespawn.config.stats;

import com.swbr.orespawn.config.OreSpawnConfig;
import java.util.List;
import java.util.Set;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The 102 OreSpawnMOBS switches ({@code *Enable}, {@code MothraPeaceful}) as
 * {@code OreSpawnMain.getMobs} left them after {@code disableAllMobs} (OreSpawnMain.java:6036-6137,
 * :1258-1260, :5804-5905).
 *
 * <p>{@code AllMobsDisable != 0} zeroed every switch in that list <em>except</em> three the author
 * forgot: {@code RockEnable}, {@code CricketEnable} and {@code FrogEnable} (verhalten/core-01a.md,
 * section 2.2). Crickets, frogs and rocks therefore keep spawning with all mobs disabled. That is
 * reproduced here, not corrected (DECISIONS R18). {@code CrabEnable} was zeroed twice; once is enough.
 *
 * <p>Read the switches through {@link #get} or {@link #enabled}; a direct {@code IntValue.get()}
 * bypasses AllMobsDisable. The {@code *_health/_attack/_defense} keys of the same category are
 * not switches - {@link MobStats} owns them.
 */
public final class MobSwitches {

    /** The switches {@code disableAllMobs()} does not touch (OreSpawnMain.java:5804-5905, grep). */
    private static final Set<String> SURVIVES_DISABLE_ALL = Set.of("RockEnable", "CricketEnable", "FrogEnable");

    private MobSwitches() {}

    /** The switch's value, or 0 when AllMobsDisable is set and the switch is on the disable list. */
    public static int get(ModConfigSpec.IntValue mobSwitch) {
        if (OreSpawnConfig.TWEAKS.AllMobsDisable.get() != 0 && !SURVIVES_DISABLE_ALL.contains(key(mobSwitch))) {
            return 0;
        }
        return mobSwitch.get();
    }

    /** {@code get(mobSwitch) != 0} - the form every spawn guard in the original used. */
    public static boolean enabled(ModConfigSpec.IntValue mobSwitch) {
        return get(mobSwitch) != 0;
    }

    private static String key(ModConfigSpec.IntValue value) {
        List<String> path = value.getPath();
        return path.get(path.size() - 1);
    }
}
