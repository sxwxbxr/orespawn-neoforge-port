package com.swbr.orespawn.network;

import com.swbr.orespawn.registry.ModAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Per-player replacement for the original global {@code OreSpawnMain.flyup_keystate}.
 *
 * <p>The original kept one static {@code int} for the whole server (OreSpawnMain.java:32, reset to 0
 * in the static initialiser at :6205) and every rideable entity read it in its movement code:
 * {@code Cephadrome.java:792}, {@code Dragon.java:971}, {@code Elevator.java:444}, {@code Leon.java:821},
 * {@code Ostrich.java:460}, {@code ThePrinceAdult.java:935}, {@code ThePrinceTeen.java:959}. In
 * multiplayer that meant one player's key lifted every other player's mount. DECISIONS R15 rules that
 * the state lives on the player who sent it; single-player behaves identically.
 *
 * <p>Mounts call {@link #isFlyUp(Entity)} with their controlling passenger where the original read
 * {@code OreSpawnMain.flyup_keystate != 0}.
 *
 * <p>The value is a runtime-only attachment: not saved to disk and not carried across a respawn, so a
 * fresh {@code ServerPlayer} always starts at 0 - the same starting value as the original static field.
 * The client re-announces its key state after login and respawn (see {@code client.input.RiderControl}).
 */
public final class RiderKeys {

    /** The raw key state of a player: 0 released, 1 held (the original {@code int} value). */
    public static int keystate(Player player) {
        return player.getData(ModAttachments.FLY_UP_KEYSTATE);
    }

    /**
     * {@code true} while the given rider holds the fly-up key.
     *
     * <p>Accepts whatever {@code getControllingPassenger()} returns: a {@code null} or non-player
     * rider reads as released, which matches the original's {@code instanceof EntityPlayer} guard
     * around the movement blocks.
     */
    public static boolean isFlyUp(@Nullable Entity rider) {
        return rider instanceof Player player && keystate(player) != 0;
    }

    /** Server-side write, called by {@link RiderControlMessageHandler} on the server thread. */
    public static void set(Player player, int keystate) {
        player.setData(ModAttachments.FLY_UP_KEYSTATE, keystate);
    }

    private RiderKeys() {}
}
