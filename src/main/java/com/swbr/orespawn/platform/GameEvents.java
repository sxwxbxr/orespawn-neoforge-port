package com.swbr.orespawn.platform;

import com.swbr.orespawn.OreSpawn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Game-bus listeners shared by both sides.
 *
 * <p>Deliberately thin. {@code OreSpawnMain} owned no game-bus handler at all
 * (verhalten/core-01a.md, section 8); what lives here is the port's own lifecycle glue. The
 * damage listeners of DECISIONS R4 and R5 belong to {@code combat}, the rider payload handler
 * to {@code network} - neither is routed through this class.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID)
public final class GameEvents {

    private GameEvents() {}

    /**
     * The date check of the original ran while the mod loaded; R18 re-evaluates it at server start. NeoForge applies
     * biome modifiers before this event (ServerLifecycleHooks.java:97-98), so the spawn table evaluates the date
     * itself (world.spawn.SpawnTable.forServer); this call keeps valentines_day current for the rest of the code.
     */
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Holidays.evaluate();
    }
}
