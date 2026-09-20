package com.swbr.orespawn.network;

import com.swbr.orespawn.OreSpawn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Payload registration, the successor of {@code CommonProxyOreSpawn.registerNetworkStuff()}
 * (CommonProxyOreSpawn.java:24-26: channel {@code "RiderControls"}, discriminator 0, side SERVER).
 *
 * <p>Registered server-bound only, like the original {@code Side.SERVER} target: the client never
 * receives this payload, so {@link RiderControlMessageHandler} can only ever run on the server.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class OreSpawnNetwork {

    /** Bumped whenever a payload changes shape; NeoForge refuses mismatched clients. */
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
                .playToServer(RiderControlMessage.TYPE, RiderControlMessage.STREAM_CODEC,
                        new RiderControlMessageHandler());
    }

    private OreSpawnNetwork() {}
}
