package com.swbr.orespawn.world.gen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;

/**
 * The two {@code WorldProvider} overrides all six {@code WorldProviderOreSpawnN} classes share, as event
 * logic (no original class; the six providers are otherwise identical in these two methods,
 * verhalten/world-03.md "WorldProviderOreSpawn"). Each dimension's provider port subscribes and passes its own
 * level key.
 *
 * <p><b>{@code setWorldTime} (e.g. WorldProviderOreSpawn2.java:26-49, WorldProviderOreSpawn6.java:34-57) is not
 * ported, on purpose.</b> The override jumps every loaded world to the next multiple of 24000 when
 * {@code time % 24000 > 12000} and every player of the dimension is asleep; otherwise it calls
 * {@code super.setWorldTime}, which on the dimension's {@code DerivedWorldInfo} is an empty method.
 * Sleeping could not reach that branch: 1.7.10 {@code WorldServer.tick} (client-1.7.10.jar {@code mt.b()})
 * first sets the time to {@code i - i % 24000} - a multiple of 24000, so {@code time % 24000 > 12000} is false
 * and the call ends in the empty {@code super} - then wakes all players ({@code mt.d()}), and only after that
 * sets {@code time + 1}, when {@code areAllPlayersAsleep()} is already false. Sleeping in an OreSpawn dimension
 * therefore changed no clock, and 1.21.1 behaves the same without any handler: {@code ServerLevel.tick} calls
 * {@code setDayTime} on the derived level, and {@code DerivedLevelData.setDayTime} is empty.
 *
 * <p>PORT: the branch was reachable only by an external {@code setWorldTime} while every player of the
 * dimension was fully asleep - {@code /time set} or {@code /time add} with a night time, or another mod setting
 * the time. 1.21.1 routes none of these through the dimension: {@code TimeCommand} calls
 * {@code ServerLevel.setDayTime} on every level, which writes the level data directly and fires no event, and
 * there is no mixin to hook it (DECISIONS R1). A tick-by-tick clock-jump detector would guess at every external
 * caller and could not reproduce the order in which 1.7.10 visited the worlds, so the case is left out
 * (DECISIONS R18, Fall 3). The earlier port handled {@code SleepFinishedTimeEvent} here and thereby added a
 * night skip for all levels that the original never had.
 */
public final class LegacyWorldProvider {

    private LegacyWorldProvider() {
    }

    /**
     * {@code canRespawnHere() == true}: 1.7.10 Forge respawned a player in the dimension they died in, at
     * that dimension's bed (Forge kept one bed per dimension) or, without one, at its randomized world spawn
     * point.
     *
     * <p>PORT: 1.21.1 keeps a single respawn point. A player who dies here and whose respawn point is a valid
     * bed or anchor <em>in this dimension</em> is left to vanilla; everyone else who died here - no respawn
     * point, a bed in another dimension, or a missing bed - respawns at this dimension's adjusted shared
     * spawn ({@code DimensionTransition(ServerLevel, Entity, ...)}, the fuzzed top-block search of
     * {@code getRandomizedSpawnPoint}). Not rebuilt: a bed set here overwrites the overworld bed, where 1.7.10
     * kept both.
     */
    public static void canRespawnHere(final PlayerRespawnPositionEvent event, final ResourceKey<Level> dimension) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.level().dimension().equals(dimension)) {
            return;
        }
        final DimensionTransition current = event.getDimensionTransition();
        if (current.newLevel().dimension().equals(dimension) && !current.missingRespawnBlock()) {
            return;
        }
        final ServerLevel level = player.server.getLevel(dimension);
        if (level == null) {
            return;
        }
        event.setDimensionTransition(new DimensionTransition(level, player, current.postDimensionTransition()));
    }
}
