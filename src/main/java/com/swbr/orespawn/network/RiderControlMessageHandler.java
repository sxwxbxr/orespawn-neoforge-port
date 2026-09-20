package com.swbr.orespawn.network;

import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Server-side receiver of {@link RiderControlMessage} (original: {@code RiderControlMessageHandler}).
 *
 * <p>The original wrote the received value into the single static {@code OreSpawnMain.flyup_keystate}
 * (RiderControlMessageHandler.java:17) - one value for every player on the server, written on the
 * Netty thread. Per DECISIONS R15 the port stores it on the <em>sending</em> player instead, and per
 * STYLE.md the write happens on the server thread via {@link IPayloadContext#enqueueWork}.
 */
public final class RiderControlMessageHandler implements IPayloadHandler<RiderControlMessage> {

    @Override
    public void handle(RiderControlMessage message, IPayloadContext context) {
        // Original line 14-16: a message arriving on the client side is ignored. The payload is
        // registered server-bound only, so this cannot happen; the check stays for fidelity.
        if (context.flow() != PacketFlow.SERVERBOUND) {
            return;
        }
        // PORT: per player (R15) instead of the global OreSpawnMain.flyup_keystate, and on the
        // server thread instead of the Netty thread.
        context.enqueueWork(() -> RiderKeys.set(context.player(), message.keystate()));
    }
}
