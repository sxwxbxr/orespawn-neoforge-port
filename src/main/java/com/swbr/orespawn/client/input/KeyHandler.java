package com.swbr.orespawn.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.swbr.orespawn.OreSpawn;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * The "OreSpawn UP/FAST" key (original: {@code KeyHandler}).
 *
 * <p>The original registered {@code new KeyBinding("OreSpawn UP/FAST", 56, "key.categories.orespawn")}
 * (KeyHandler.java:25) in its constructor. Key code 56 is an LWJGL 2 / DirectInput scan code; the
 * vanilla 1.7.10 client in {@code reference/jar/mcp/client-1.7.10.jar} binds forward=17 (W),
 * jump=57 (Space), sneak=42 (LShift), sprint=29 (LCtrl) in that same table, and 0x38 = 56 is
 * {@code DIK_LMENU}, the left Alt key. Its GLFW equivalent is {@link InputConstants#KEY_LALT} (342).
 *
 * <p>{@code onKeyInput} of the original was an empty listener (KeyHandler.java:20-22) and is not
 * ported; the polling happens in {@link RiderControl}.
 */
@EventBusSubscriber(modid = OreSpawn.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class KeyHandler {

    public static final String KEY_CATEGORY = "key.categories.orespawn";

    /**
     * Left Alt by default, like the original's code 56.
     *
     * <p>Plain constructor on purpose: the original binding had no conflict context or modifier,
     * and vanilla 1.21.1 already withholds key presses while a screen is open
     * ({@code KeyboardHandler.keyPress} only calls {@code KeyMapping.set(true)} with no screen,
     * {@code Minecraft.setScreen} calls {@code KeyMapping.releaseAll()}), which is the 1.7.10
     * behaviour of {@code Minecraft.runTick} as well.
     */
    // PORT: the name is a translation key ("key.orespawn.fly_up" -> "OreSpawn UP/FAST" in en_us.json)
    // instead of the original raw label; the displayed text stays identical.
    public static final KeyMapping KEY_FLY_UP =
            new KeyMapping("key.orespawn.fly_up", InputConstants.KEY_LALT, KEY_CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_FLY_UP);
    }

    private KeyHandler() {}
}
