package com.swbr.orespawn.client.input;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.network.RiderControlMessage;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client tick listener that reports the fly-up key to the server (original: {@code RiderControl}).
 *
 * <p>Original {@code onTick} (RiderControl.java:19-31): read {@code KEY_FLY_UP.getIsKeyPressed()} as
 * 1 or 0, and only when that differs from the last value sent, send it and remember it. Every tick,
 * nothing else. The port keeps exactly that: one message per state change, never per tick.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID, value = Dist.CLIENT)
public final class RiderControl {

    /** Last key state sent to the server; original field {@code keystate}, start value 0 (line 15). */
    private static int keystate = 0;

    // PORT: the original had no phase check and ran in both tick phases (harmless because of the
    // change check); the port listens to Post only.
    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        int newkeystate = 0;
        if (KeyHandler.KEY_FLY_UP.isDown()) {
            newkeystate = 1;
        }
        if (keystate != newkeystate) {
            // PORT: the original called sendToServer unconditionally; in 1.21.1 that is an NPE
            // without a play connection (PacketDistributor.sendToServer requireNonNull's it). While
            // not connected the change is neither sent nor remembered, so it is sent once connected.
            if (Minecraft.getInstance().getConnection() == null) {
                return;
            }
            PacketDistributor.sendToServer(new RiderControlMessage(newkeystate));
            keystate = newkeystate;
        }
    }

    // PORT: the original never reset its remembered state; with the server value now living on a
    // ServerPlayer (fresh at 0 after every login and respawn, see network.RiderKeys) the client
    // forgets its last sent value at login, so a held key is announced again next tick.
    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        keystate = 0;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        keystate = 0;
    }

    // PORT (BUGHUNT2 2.7): Clone fires on respawn and on every dimension change. After a respawn the
    // server value is 0, after a dimension change the same ServerPlayer keeps its last value (1 if
    // the key was held into the portal). The loading screen releases all keys without a tick that
    // sends, so 0 here would swallow the release. -1 matches neither 0 nor 1: the next tick always
    // sends the real isDown() state.
    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        keystate = -1;
    }

    private RiderControl() {}
}
