package com.swbr.orespawn.loot;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.config.EarlyConfig;
import com.swbr.orespawn.config.OreSpawnConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe condition {@code orespawn:miners_dream_expensive}: true when
 * {@code OreSpawnMain.MinersDreamExpensive != 0}.
 *
 * <p>The original chose one of two Miner's Dream recipes while registering them
 * (OreSpawnMain.java:3018-3025): gunpowder when the key is 0, TNT otherwise. The generated recipes
 * {@code minersdream_from_gunpowder} (wrapped in {@code neoforge:not}) and
 * {@code minersdream_from_tnt} carry this condition, so exactly one of them loads - as in 1.7.10.
 *
 * <p>Readability, checked against NeoForge 21.1.248: COMMON configs are loaded in the mod-loading
 * task "Config loading" ({@code CommonModLoader.begin}, after "Registry initialization"), long
 * before any server exists; recipe conditions are evaluated when the server loads its datapacks.
 * The spec value is therefore always loaded here. {@link EarlyConfig} stays as a fallback for a
 * context without loaded configs (data generation skips "Config loading").
 *
 * <p>PORT: the original read the value once in preInit; a changed config file needed a restart.
 * Here, as for every runtime value (DECISIONS R3), NeoForge's file watcher plus {@code /reload}
 * picks up a changed value without one.
 */
public final class MinersDreamExpensiveCondition implements ICondition {

    public static final MinersDreamExpensiveCondition INSTANCE = new MinersDreamExpensiveCondition();
    public static final MapCodec<MinersDreamExpensiveCondition> CODEC = MapCodec.unit(INSTANCE).stable();

    private MinersDreamExpensiveCondition() {}

    @Override
    public boolean test(IContext context) {
        final int value = OreSpawnConfig.SPEC.isLoaded()
                ? OreSpawnConfig.TWEAKS.MinersDreamExpensive.get()
                : EarlyConfig.get(OreSpawnConfig.TWEAKS.MinersDreamExpensive);
        // OreSpawnMain.java:3018: if (OreSpawnMain.MinersDreamExpensive == 0) -> cheap recipe
        return value != 0;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return ModLootModifiers.MINERS_DREAM_EXPENSIVE.get();
    }

    @Override
    public String toString() {
        return "orespawn:miners_dream_expensive";
    }
}
