package com.swbr.orespawn.registry;

import com.swbr.orespawn.OreSpawn;
import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Data attachments - state the port keeps on entities it does not own.
 *
 * <p>The original had none; it kept the rider key state in one static field
 * ({@code OreSpawnMain.flyup_keystate}, OreSpawnMain.java:32, :6205) that every player's payload
 * overwrote. DECISIONS R15 moves that to a per-player attachment, which W01's network porter
 * registers here.
 */
public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, OreSpawn.MOD_ID);

    /**
     * Per-player fly-up key state, 0 released / 1 held (DECISIONS R15). Replaces the global
     * {@code OreSpawnMain.flyup_keystate}. Runtime only on purpose: no serializer, so it is neither
     * saved to disk nor copied on respawn - a fresh ServerPlayer starts at 0 like the original
     * static field, and the client re-announces its key after login/respawn
     * ({@code client.input.RiderControl}). Read and written through {@code network.RiderKeys}.
     */
    public static final Supplier<AttachmentType<Integer>> FLY_UP_KEYSTATE = ATTACHMENT_TYPES.register(
            "fly_up_keystate", () -> AttachmentType.<Integer>builder(() -> 0).build());

    private ModAttachments() {}

    /** Loads this class so every holder above is registered. */
    public static void init() {}
}
