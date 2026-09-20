package com.swbr.orespawn.network;

import com.swbr.orespawn.OreSpawn;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-server message carrying the rider key state (original: {@code RiderControlMessage}).
 *
 * <p>One byte on the wire, exactly like the original: {@code toBytes} wrote {@code writeByte(keystate)},
 * {@code fromBytes} read {@code readUnsignedByte()} (RiderControlMessage.java:15-21). The value is
 * {@code 1} while the fly-up key is held and {@code 0} otherwise (RiderControl.java:21-25).
 *
 * <p>The original channel was named {@code "RiderControls"} (CommonProxyOreSpawn.java:24); that name
 * becomes the payload id {@code orespawn:rider_controls}.
 *
 * @param keystate 0 = released, 1 = fly-up key held
 */
public record RiderControlMessage(int keystate) implements CustomPacketPayload {

    public static final Type<RiderControlMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "rider_controls"));

    /** Byte in, unsigned byte out - the original {@code toBytes}/{@code fromBytes} pair. */
    public static final StreamCodec<ByteBuf, RiderControlMessage> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> buf.writeByte(message.keystate()),
            buf -> new RiderControlMessage(buf.readUnsignedByte()));

    // PORT: the original's hasChanged()/previous field was never called (grep) and is not ported.

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
