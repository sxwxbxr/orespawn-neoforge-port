package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

/**
 * The PvP guard the Ultimate tools share verbatim: {@code onLeftClickEntity} of
 * {@code UltimateAxe} (UltimateAxe.java:40-53), {@code UltimatePickaxe} (:62-75),
 * {@code UltimateShovel} (:37-50) and {@code UltimateSword} (:130-141), and the same test inside
 * {@code UltimateSword.isSuitableTarget} (:181-191).
 *
 * <p>{@code OreSpawnMain.ultimate_sword_pvp} is {@code OreSpawnTWEAKS.UltimateSwordPvp}
 * (OreSpawnMain.java:1150, default 0), read unclamped.
 */
final class UltimatePvp {

    private UltimatePvp() {}

    /**
     * {@code entity != null && ultimate_sword_pvp == 0 && (player || Girlfriend || Boyfriend ||
     * tamed EntityTameable)}: with the guard active the attack is cancelled.
     */
    static boolean protects(Entity entity) {
        if (entity != null && OreSpawnConfig.TWEAKS.UltimateSwordPvp.get() == 0) {
            if (entity instanceof Player || entity instanceof Girlfriend || entity instanceof Boyfriend) {
                return true;
            }
            if (entity instanceof TamableAnimal t) {
                if (t.isTame()) {
                    return true;
                }
            }
        }
        return false;
    }
}
