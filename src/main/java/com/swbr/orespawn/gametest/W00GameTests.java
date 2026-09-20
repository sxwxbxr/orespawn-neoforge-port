package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.EarlyConfig;
import com.swbr.orespawn.config.OreSpawnConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Wave 0: the generated foundation is actually live on a dedicated server.
 *
 * <p>Every later wave builds on these three facts, and each of them can fail silently: a
 * DeferredRegister that is never attached registers nothing, a spec that is never registered
 * returns defaults forever, and {@link EarlyConfig} reads a file whose path is assembled by hand.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public class W00GameTests {

    // Bare path: @GameTestHolder already supplies the namespace (see mods/armature).
    private static final String ARENA = "arena";

    /** Number of sound events in the original sounds.json (docs/catalog/manifest.json). */
    private static final int ORIGINAL_SOUND_EVENTS = 126;

    @GameTest(template = ARENA)
    public static void everyOriginalSoundEventIsRegistered(GameTestHelper helper) {
        long ours = BuiltInRegistries.SOUND_EVENT.keySet().stream()
                .filter(id -> id.getNamespace().equals(OreSpawn.MOD_ID))
                .count();
        if (ours != ORIGINAL_SOUND_EVENTS) {
            helper.fail("expected " + ORIGINAL_SOUND_EVENTS + " orespawn sound events, found " + ours);
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void commonConfigIsLoaded(GameTestHelper helper) {
        if (!OreSpawnConfig.SPEC.isLoaded()) {
            helper.fail("orespawn-common.toml was never loaded");
        }
        // The King's config default is 7000 health (jar: OreSpawnMain.getMobs).
        int health = OreSpawnConfig.MOBS.TheKing_health.get();
        if (health != 7000) {
            helper.fail("TheKing_health is " + health + ", the original default is 7000");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void earlyConfigAgreesWithTheSpec(GameTestHelper helper) {
        int early = EarlyConfig.get(OreSpawnConfig.WEAPONS.Ultimate_damage);
        int spec = OreSpawnConfig.WEAPONS.Ultimate_damage.get();
        if (early != spec) {
            helper.fail("EarlyConfig read Ultimate_damage = " + early + " but the spec holds " + spec);
        }
        helper.succeed();
    }
}
