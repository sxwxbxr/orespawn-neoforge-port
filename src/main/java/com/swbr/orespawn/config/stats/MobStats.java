package com.swbr.orespawn.config.stats;

import com.swbr.orespawn.config.OreSpawnConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Port of {@code MobStats} (MobStats.java:5-7) together with the reader
 * {@code OreSpawnMain.get_mobstats} (OreSpawnMain.java:5738-5768).
 *
 * <p>The original filled 59 static holders once in preInit. Here every accessor reads the config
 * and applies the same clamps, in the same order, against the same defaults - the defaults are
 * the spec's own ({@code IntValue.getDefault()}), so the numbers live in one generated place.
 * A record per call is cheap; entities read this in their constructor or {@code finalizeSpawn},
 * not per tick (verhalten/core-02.md, MobStats).
 *
 * <p>Health above 1024 (TheKing 7000, TheQueen 6000, Mobzilla 4000, SpiderRobot 1500 by default;
 * Jeffery 1100 and Kraken 2000 through the x2 clamp) stays as the original number here. The
 * scaling to the attribute clamp is the job of {@code combat} (DECISIONS R4), not of this holder.
 */
public record MobStats(int health, int attack, int defense) {

    private static final OreSpawnConfig.Mobs C = OreSpawnConfig.MOBS;

    /**
     * {@code get_mobstats}: health and attack clamped to [default/2, default*2], defense to
     * [default-4, default+4] and then to [0, 22] (OreSpawnMain.java:5741-5766).
     */
    public static MobStats read(StatSource source, ModConfigSpec.IntValue healthValue,
            ModConfigSpec.IntValue attackValue, ModConfigSpec.IntValue defenseValue) {
        int health = healthValue.getDefault();
        int attack = attackValue.getDefault();
        int defense = defenseValue.getDefault();
        int h = source.read(healthValue);
        if (h < health / 2) {
            h = health / 2;
        }
        if (h > health * 2) {
            h = health * 2;
        }
        int a = source.read(attackValue);
        if (a < attack / 2) {
            a = attack / 2;
        }
        if (a > attack * 2) {
            a = attack * 2;
        }
        int d = source.read(defenseValue);
        if (d < defense - 4) {
            d = defense - 4;
        }
        if (d > defense + 4) {
            d = defense + 4;
        }
        if (d > 22) {
            d = 22;
        }
        if (d < 0) {
            d = 0;
        }
        return new MobStats(h, a, d);
    }

    /** {@code OreSpawnMain.Bee_stats}: prefix {@code Bee} (OreSpawnMain.java:6138). */
    public static MobStats Bee_stats(StatSource source) {
        return read(source,
                C.Bee_health,
                C.Bee_attack,
                C.Bee_defense);
    }

    /** Runtime form of {@link #Bee_stats(StatSource)}. */
    public static MobStats Bee_stats() {
        return Bee_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Mantis_stats}: prefix {@code Mantis} (OreSpawnMain.java:6139). */
    public static MobStats Mantis_stats(StatSource source) {
        return read(source,
                C.Mantis_health,
                C.Mantis_attack,
                C.Mantis_defense);
    }

    /** Runtime form of {@link #Mantis_stats(StatSource)}. */
    public static MobStats Mantis_stats() {
        return Mantis_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.HerculesBeetle_stats}: prefix {@code HerculesBeetle} (OreSpawnMain.java:6140). */
    public static MobStats HerculesBeetle_stats(StatSource source) {
        return read(source,
                C.HerculesBeetle_health,
                C.HerculesBeetle_attack,
                C.HerculesBeetle_defense);
    }

    /** Runtime form of {@link #HerculesBeetle_stats(StatSource)}. */
    public static MobStats HerculesBeetle_stats() {
        return HerculesBeetle_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Mothra_stats}: prefix {@code Mothra} (OreSpawnMain.java:6141). */
    public static MobStats Mothra_stats(StatSource source) {
        return read(source,
                C.Mothra_health,
                C.Mothra_attack,
                C.Mothra_defense);
    }

    /** Runtime form of {@link #Mothra_stats(StatSource)}. */
    public static MobStats Mothra_stats() {
        return Mothra_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Brutalfly_stats}: prefix {@code Brutalfly} (OreSpawnMain.java:6142). */
    public static MobStats Brutalfly_stats(StatSource source) {
        return read(source,
                C.Brutalfly_health,
                C.Brutalfly_attack,
                C.Brutalfly_defense);
    }

    /** Runtime form of {@link #Brutalfly_stats(StatSource)}. */
    public static MobStats Brutalfly_stats() {
        return Brutalfly_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Nastysaurus_stats}: prefix {@code Nastysaurus} (OreSpawnMain.java:6143). */
    public static MobStats Nastysaurus_stats(StatSource source) {
        return read(source,
                C.Nastysaurus_health,
                C.Nastysaurus_attack,
                C.Nastysaurus_defense);
    }

    /** Runtime form of {@link #Nastysaurus_stats(StatSource)}. */
    public static MobStats Nastysaurus_stats() {
        return Nastysaurus_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Pointysaurus_stats}: prefix {@code Pointysaurus} (OreSpawnMain.java:6144). */
    public static MobStats Pointysaurus_stats(StatSource source) {
        return read(source,
                C.Pointysaurus_health,
                C.Pointysaurus_attack,
                C.Pointysaurus_defense);
    }

    /** Runtime form of {@link #Pointysaurus_stats(StatSource)}. */
    public static MobStats Pointysaurus_stats() {
        return Pointysaurus_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Alosaurus_stats}: prefix {@code Alosaurus} (OreSpawnMain.java:6145). */
    public static MobStats Alosaurus_stats(StatSource source) {
        return read(source,
                C.Alosaurus_health,
                C.Alosaurus_attack,
                C.Alosaurus_defense);
    }

    /** Runtime form of {@link #Alosaurus_stats(StatSource)}. */
    public static MobStats Alosaurus_stats() {
        return Alosaurus_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.SpiderRobot_stats}: prefix {@code SpiderRobot} (OreSpawnMain.java:6146). */
    public static MobStats SpiderRobot_stats(StatSource source) {
        return read(source,
                C.SpiderRobot_health,
                C.SpiderRobot_attack,
                C.SpiderRobot_defense);
    }

    /** Runtime form of {@link #SpiderRobot_stats(StatSource)}. */
    public static MobStats SpiderRobot_stats() {
        return SpiderRobot_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.AntRobot_stats}: prefix {@code AntRobot} (OreSpawnMain.java:6147). */
    public static MobStats AntRobot_stats(StatSource source) {
        return read(source,
                C.AntRobot_health,
                C.AntRobot_attack,
                C.AntRobot_defense);
    }

    /** Runtime form of {@link #AntRobot_stats(StatSource)}. */
    public static MobStats AntRobot_stats() {
        return AntRobot_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Jeffery_stats}: prefix {@code Jeffery} (OreSpawnMain.java:6148). */
    public static MobStats Jeffery_stats(StatSource source) {
        return read(source,
                C.Jeffery_health,
                C.Jeffery_attack,
                C.Jeffery_defense);
    }

    /** Runtime form of {@link #Jeffery_stats(StatSource)}. */
    public static MobStats Jeffery_stats() {
        return Jeffery_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Hammerhead_stats}: prefix {@code Hammerhead} (OreSpawnMain.java:6149). */
    public static MobStats Hammerhead_stats(StatSource source) {
        return read(source,
                C.Hammerhead_health,
                C.Hammerhead_attack,
                C.Hammerhead_defense);
    }

    /** Runtime form of {@link #Hammerhead_stats(StatSource)}. */
    public static MobStats Hammerhead_stats() {
        return Hammerhead_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Molenoid_stats}: prefix {@code Molenoid} (OreSpawnMain.java:6150). */
    public static MobStats Molenoid_stats(StatSource source) {
        return read(source,
                C.Molenoid_health,
                C.Molenoid_attack,
                C.Molenoid_defense);
    }

    /** Runtime form of {@link #Molenoid_stats(StatSource)}. */
    public static MobStats Molenoid_stats() {
        return Molenoid_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.TRex_stats}: prefix {@code TRex} (OreSpawnMain.java:6151). */
    public static MobStats TRex_stats(StatSource source) {
        return read(source,
                C.TRex_health,
                C.TRex_attack,
                C.TRex_defense);
    }

    /** Runtime form of {@link #TRex_stats(StatSource)}. */
    public static MobStats TRex_stats() {
        return TRex_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.BandP_stats}: prefix {@code BandP} (OreSpawnMain.java:6152). */
    public static MobStats BandP_stats(StatSource source) {
        return read(source,
                C.BandP_health,
                C.BandP_attack,
                C.BandP_defense);
    }

    /** Runtime form of {@link #BandP_stats(StatSource)}. */
    public static MobStats BandP_stats() {
        return BandP_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.CaterKiller_stats}: prefix {@code CaterKiller} (OreSpawnMain.java:6153). */
    public static MobStats CaterKiller_stats(StatSource source) {
        return read(source,
                C.CaterKiller_health,
                C.CaterKiller_attack,
                C.CaterKiller_defense);
    }

    /** Runtime form of {@link #CaterKiller_stats(StatSource)}. */
    public static MobStats CaterKiller_stats() {
        return CaterKiller_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Cryolophosaurus_stats}: prefix {@code Cryolophosaurus} (OreSpawnMain.java:6154). */
    public static MobStats Cryolophosaurus_stats(StatSource source) {
        return read(source,
                C.Cryolophosaurus_health,
                C.Cryolophosaurus_attack,
                C.Cryolophosaurus_defense);
    }

    /** Runtime form of {@link #Cryolophosaurus_stats(StatSource)}. */
    public static MobStats Cryolophosaurus_stats() {
        return Cryolophosaurus_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Rat_stats}: prefix {@code Rat} (OreSpawnMain.java:6155). */
    public static MobStats Rat_stats(StatSource source) {
        return read(source,
                C.Rat_health,
                C.Rat_attack,
                C.Rat_defense);
    }

    /** Runtime form of {@link #Rat_stats(StatSource)}. */
    public static MobStats Rat_stats() {
        return Rat_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Urchin_stats}: prefix {@code Urchin} (OreSpawnMain.java:6156). */
    public static MobStats Urchin_stats(StatSource source) {
        return read(source,
                C.Urchin_health,
                C.Urchin_attack,
                C.Urchin_defense);
    }

    /** Runtime form of {@link #Urchin_stats(StatSource)}. */
    public static MobStats Urchin_stats() {
        return Urchin_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Kyuubi_stats}: prefix {@code Kyuubi} (OreSpawnMain.java:6157). */
    public static MobStats Kyuubi_stats(StatSource source) {
        return read(source,
                C.Kyuubi_health,
                C.Kyuubi_attack,
                C.Kyuubi_defense);
    }

    /** Runtime form of {@link #Kyuubi_stats(StatSource)}. */
    public static MobStats Kyuubi_stats() {
        return Kyuubi_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.GammaMetroid_stats}: prefix {@code GammaMetroid} (OreSpawnMain.java:6158). */
    public static MobStats GammaMetroid_stats(StatSource source) {
        return read(source,
                C.GammaMetroid_health,
                C.GammaMetroid_attack,
                C.GammaMetroid_defense);
    }

    /** Runtime form of {@link #GammaMetroid_stats(StatSource)}. */
    public static MobStats GammaMetroid_stats() {
        return GammaMetroid_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Basilisk_stats}: prefix {@code Basilisk} (OreSpawnMain.java:6159). */
    public static MobStats Basilisk_stats(StatSource source) {
        return read(source,
                C.Basilisk_health,
                C.Basilisk_attack,
                C.Basilisk_defense);
    }

    /** Runtime form of {@link #Basilisk_stats(StatSource)}. */
    public static MobStats Basilisk_stats() {
        return Basilisk_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.EmperorScorpion_stats}: prefix {@code EmperorScorpion} (OreSpawnMain.java:6160). */
    public static MobStats EmperorScorpion_stats(StatSource source) {
        return read(source,
                C.EmperorScorpion_health,
                C.EmperorScorpion_attack,
                C.EmperorScorpion_defense);
    }

    /** Runtime form of {@link #EmperorScorpion_stats(StatSource)}. */
    public static MobStats EmperorScorpion_stats() {
        return EmperorScorpion_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.TrooperBug_stats}: prefix {@code TrooperBug} (OreSpawnMain.java:6161). */
    public static MobStats TrooperBug_stats(StatSource source) {
        return read(source,
                C.TrooperBug_health,
                C.TrooperBug_attack,
                C.TrooperBug_defense);
    }

    /** Runtime form of {@link #TrooperBug_stats(StatSource)}. */
    public static MobStats TrooperBug_stats() {
        return TrooperBug_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.SpitBug_stats}: prefix {@code SpitBug} (OreSpawnMain.java:6162). */
    public static MobStats SpitBug_stats(StatSource source) {
        return read(source,
                C.SpitBug_health,
                C.SpitBug_attack,
                C.SpitBug_defense);
    }

    /** Runtime form of {@link #SpitBug_stats(StatSource)}. */
    public static MobStats SpitBug_stats() {
        return SpitBug_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Alien_stats}: prefix {@code Alien} (OreSpawnMain.java:6163). */
    public static MobStats Alien_stats(StatSource source) {
        return read(source,
                C.Alien_health,
                C.Alien_attack,
                C.Alien_defense);
    }

    /** Runtime form of {@link #Alien_stats(StatSource)}. */
    public static MobStats Alien_stats() {
        return Alien_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.WaterDragon_stats}: prefix {@code WaterDragon} (OreSpawnMain.java:6164). */
    public static MobStats WaterDragon_stats(StatSource source) {
        return read(source,
                C.WaterDragon_health,
                C.WaterDragon_attack,
                C.WaterDragon_defense);
    }

    /** Runtime form of {@link #WaterDragon_stats(StatSource)}. */
    public static MobStats WaterDragon_stats() {
        return WaterDragon_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.SeaMonster_stats}: prefix {@code SeaMonster} (OreSpawnMain.java:6165). */
    public static MobStats SeaMonster_stats(StatSource source) {
        return read(source,
                C.SeaMonster_health,
                C.SeaMonster_attack,
                C.SeaMonster_defense);
    }

    /** Runtime form of {@link #SeaMonster_stats(StatSource)}. */
    public static MobStats SeaMonster_stats() {
        return SeaMonster_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.SeaViper_stats}: prefix {@code SeaViper} (OreSpawnMain.java:6166). */
    public static MobStats SeaViper_stats(StatSource source) {
        return read(source,
                C.SeaViper_health,
                C.SeaViper_attack,
                C.SeaViper_defense);
    }

    /** Runtime form of {@link #SeaViper_stats(StatSource)}. */
    public static MobStats SeaViper_stats() {
        return SeaViper_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Robot2_stats}: prefix {@code Robot2} (OreSpawnMain.java:6167). */
    public static MobStats Robot2_stats(StatSource source) {
        return read(source,
                C.Robot2_health,
                C.Robot2_attack,
                C.Robot2_defense);
    }

    /** Runtime form of {@link #Robot2_stats(StatSource)}. */
    public static MobStats Robot2_stats() {
        return Robot2_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Robot3_stats}: prefix {@code Robot3} (OreSpawnMain.java:6168). */
    public static MobStats Robot3_stats(StatSource source) {
        return read(source,
                C.Robot3_health,
                C.Robot3_attack,
                C.Robot3_defense);
    }

    /** Runtime form of {@link #Robot3_stats(StatSource)}. */
    public static MobStats Robot3_stats() {
        return Robot3_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Robot4_stats}: prefix {@code Robot4} (OreSpawnMain.java:6169). */
    public static MobStats Robot4_stats(StatSource source) {
        return read(source,
                C.Robot4_health,
                C.Robot4_attack,
                C.Robot4_defense);
    }

    /** Runtime form of {@link #Robot4_stats(StatSource)}. */
    public static MobStats Robot4_stats() {
        return Robot4_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Robot5_stats}: prefix {@code Robot5} (OreSpawnMain.java:6170). */
    public static MobStats Robot5_stats(StatSource source) {
        return read(source,
                C.Robot5_health,
                C.Robot5_attack,
                C.Robot5_defense);
    }

    /** Runtime form of {@link #Robot5_stats(StatSource)}. */
    public static MobStats Robot5_stats() {
        return Robot5_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Rotator_stats}: prefix {@code Rotator} (OreSpawnMain.java:6171). */
    public static MobStats Rotator_stats(StatSource source) {
        return read(source,
                C.Rotator_health,
                C.Rotator_attack,
                C.Rotator_defense);
    }

    /** Runtime form of {@link #Rotator_stats(StatSource)}. */
    public static MobStats Rotator_stats() {
        return Rotator_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Vortex_stats}: prefix {@code Vortex} (OreSpawnMain.java:6172). */
    public static MobStats Vortex_stats(StatSource source) {
        return read(source,
                C.Vortex_health,
                C.Vortex_attack,
                C.Vortex_defense);
    }

    /** Runtime form of {@link #Vortex_stats(StatSource)}. */
    public static MobStats Vortex_stats() {
        return Vortex_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.DungeonBeast_stats}: prefix {@code DungeonBeast} (OreSpawnMain.java:6173). */
    public static MobStats DungeonBeast_stats(StatSource source) {
        return read(source,
                C.DungeonBeast_health,
                C.DungeonBeast_attack,
                C.DungeonBeast_defense);
    }

    /** Runtime form of {@link #DungeonBeast_stats(StatSource)}. */
    public static MobStats DungeonBeast_stats() {
        return DungeonBeast_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Triffid_stats}: prefix {@code Triffid} (OreSpawnMain.java:6174). */
    public static MobStats Triffid_stats(StatSource source) {
        return read(source,
                C.Triffid_health,
                C.Triffid_attack,
                C.Triffid_defense);
    }

    /** Runtime form of {@link #Triffid_stats(StatSource)}. */
    public static MobStats Triffid_stats() {
        return Triffid_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.LurkingTerror_stats}: prefix {@code LurkingTerror} (OreSpawnMain.java:6175). */
    public static MobStats LurkingTerror_stats(StatSource source) {
        return read(source,
                C.LurkingTerror_health,
                C.LurkingTerror_attack,
                C.LurkingTerror_defense);
    }

    /** Runtime form of {@link #LurkingTerror_stats(StatSource)}. */
    public static MobStats LurkingTerror_stats() {
        return LurkingTerror_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.WormSmall_stats}: prefix {@code WormSmall} (OreSpawnMain.java:6176). */
    public static MobStats WormSmall_stats(StatSource source) {
        return read(source,
                C.WormSmall_health,
                C.WormSmall_attack,
                C.WormSmall_defense);
    }

    /** Runtime form of {@link #WormSmall_stats(StatSource)}. */
    public static MobStats WormSmall_stats() {
        return WormSmall_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.WormMedium_stats}: prefix {@code WormMedium} (OreSpawnMain.java:6177). */
    public static MobStats WormMedium_stats(StatSource source) {
        return read(source,
                C.WormMedium_health,
                C.WormMedium_attack,
                C.WormMedium_defense);
    }

    /** Runtime form of {@link #WormMedium_stats(StatSource)}. */
    public static MobStats WormMedium_stats() {
        return WormMedium_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.WormLarge_stats}: prefix {@code WormLarge} (OreSpawnMain.java:6178). */
    public static MobStats WormLarge_stats(StatSource source) {
        return read(source,
                C.WormLarge_health,
                C.WormLarge_attack,
                C.WormLarge_defense);
    }

    /** Runtime form of {@link #WormLarge_stats(StatSource)}. */
    public static MobStats WormLarge_stats() {
        return WormLarge_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.EnderKnight_stats}: prefix {@code EnderKnight} (OreSpawnMain.java:6179). */
    public static MobStats EnderKnight_stats(StatSource source) {
        return read(source,
                C.EnderKnight_health,
                C.EnderKnight_attack,
                C.EnderKnight_defense);
    }

    /** Runtime form of {@link #EnderKnight_stats(StatSource)}. */
    public static MobStats EnderKnight_stats() {
        return EnderKnight_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.EnderReaper_stats}: prefix {@code EnderReaper} (OreSpawnMain.java:6180). */
    public static MobStats EnderReaper_stats(StatSource source) {
        return read(source,
                C.EnderReaper_health,
                C.EnderReaper_attack,
                C.EnderReaper_defense);
    }

    /** Runtime form of {@link #EnderReaper_stats(StatSource)}. */
    public static MobStats EnderReaper_stats() {
        return EnderReaper_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Irukandji_stats}: prefix {@code Irukandji} (OreSpawnMain.java:6181). */
    public static MobStats Irukandji_stats(StatSource source) {
        return read(source,
                C.Irukandji_health,
                C.Irukandji_attack,
                C.Irukandji_defense);
    }

    /** Runtime form of {@link #Irukandji_stats(StatSource)}. */
    public static MobStats Irukandji_stats() {
        return Irukandji_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.AttackSquid_stats}: prefix {@code AttackSquid} (OreSpawnMain.java:6182). */
    public static MobStats AttackSquid_stats(StatSource source) {
        return read(source,
                C.AttackSquid_health,
                C.AttackSquid_attack,
                C.AttackSquid_defense);
    }

    /** Runtime form of {@link #AttackSquid_stats(StatSource)}. */
    public static MobStats AttackSquid_stats() {
        return AttackSquid_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.CaveFisher_stats}: prefix {@code CaveFisher} (OreSpawnMain.java:6183). */
    public static MobStats CaveFisher_stats(StatSource source) {
        return read(source,
                C.CaveFisher_health,
                C.CaveFisher_attack,
                C.CaveFisher_defense);
    }

    /** Runtime form of {@link #CaveFisher_stats(StatSource)}. */
    public static MobStats CaveFisher_stats() {
        return CaveFisher_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.CloudShark_stats}: prefix {@code CloudShark} (OreSpawnMain.java:6184). */
    public static MobStats CloudShark_stats(StatSource source) {
        return read(source,
                C.CloudShark_health,
                C.CloudShark_attack,
                C.CloudShark_defense);
    }

    /** Runtime form of {@link #CloudShark_stats(StatSource)}. */
    public static MobStats CloudShark_stats() {
        return CloudShark_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.CreepingHorror_stats}: prefix {@code CreepingHorror} (OreSpawnMain.java:6185). */
    public static MobStats CreepingHorror_stats(StatSource source) {
        return read(source,
                C.CreepingHorror_health,
                C.CreepingHorror_attack,
                C.CreepingHorror_defense);
    }

    /** Runtime form of {@link #CreepingHorror_stats(StatSource)}. */
    public static MobStats CreepingHorror_stats() {
        return CreepingHorror_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Godzilla_stats}: prefix {@code Mobzilla} (OreSpawnMain.java:6186). */
    public static MobStats Godzilla_stats(StatSource source) {
        return read(source,
                C.Mobzilla_health,
                C.Mobzilla_attack,
                C.Mobzilla_defense);
    }

    /** Runtime form of {@link #Godzilla_stats(StatSource)}. */
    public static MobStats Godzilla_stats() {
        return Godzilla_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Kraken_stats}: prefix {@code Kraken} (OreSpawnMain.java:6187). */
    public static MobStats Kraken_stats(StatSource source) {
        return read(source,
                C.Kraken_health,
                C.Kraken_attack,
                C.Kraken_defense);
    }

    /** Runtime form of {@link #Kraken_stats(StatSource)}. */
    public static MobStats Kraken_stats() {
        return Kraken_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.LeafMonster_stats}: prefix {@code LeafMonster} (OreSpawnMain.java:6188). */
    public static MobStats LeafMonster_stats(StatSource source) {
        return read(source,
                C.LeafMonster_health,
                C.LeafMonster_attack,
                C.LeafMonster_defense);
    }

    /** Runtime form of {@link #LeafMonster_stats(StatSource)}. */
    public static MobStats LeafMonster_stats() {
        return LeafMonster_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.PitchBlack_stats}: prefix {@code Nightmare} (OreSpawnMain.java:6189). */
    public static MobStats PitchBlack_stats(StatSource source) {
        return read(source,
                C.Nightmare_health,
                C.Nightmare_attack,
                C.Nightmare_defense);
    }

    /** Runtime form of {@link #PitchBlack_stats(StatSource)}. */
    public static MobStats PitchBlack_stats() {
        return PitchBlack_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Scorpion_stats}: prefix {@code Scorpion} (OreSpawnMain.java:6190). */
    public static MobStats Scorpion_stats(StatSource source) {
        return read(source,
                C.Scorpion_health,
                C.Scorpion_attack,
                C.Scorpion_defense);
    }

    /** Runtime form of {@link #Scorpion_stats(StatSource)}. */
    public static MobStats Scorpion_stats() {
        return Scorpion_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Skate_stats}: prefix {@code Skate} (OreSpawnMain.java:6191). */
    public static MobStats Skate_stats(StatSource source) {
        return read(source,
                C.Skate_health,
                C.Skate_attack,
                C.Skate_defense);
    }

    /** Runtime form of {@link #Skate_stats(StatSource)}. */
    public static MobStats Skate_stats() {
        return Skate_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.TerribleTerror_stats}: prefix {@code TerribleTerror} (OreSpawnMain.java:6192). */
    public static MobStats TerribleTerror_stats(StatSource source) {
        return read(source,
                C.TerribleTerror_health,
                C.TerribleTerror_attack,
                C.TerribleTerror_defense);
    }

    /** Runtime form of {@link #TerribleTerror_stats(StatSource)}. */
    public static MobStats TerribleTerror_stats() {
        return TerribleTerror_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.TheKing_stats}: prefix {@code TheKing} (OreSpawnMain.java:6193). */
    public static MobStats TheKing_stats(StatSource source) {
        return read(source,
                C.TheKing_health,
                C.TheKing_attack,
                C.TheKing_defense);
    }

    /** Runtime form of {@link #TheKing_stats(StatSource)}. */
    public static MobStats TheKing_stats() {
        return TheKing_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.TheQueen_stats}: prefix {@code TheQueen} (OreSpawnMain.java:6194). */
    public static MobStats TheQueen_stats(StatSource source) {
        return read(source,
                C.TheQueen_health,
                C.TheQueen_attack,
                C.TheQueen_defense);
    }

    /** Runtime form of {@link #TheQueen_stats(StatSource)}. */
    public static MobStats TheQueen_stats() {
        return TheQueen_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Leon_stats}: prefix {@code Leonopteryx} (OreSpawnMain.java:6195). */
    public static MobStats Leon_stats(StatSource source) {
        return read(source,
                C.Leonopteryx_health,
                C.Leonopteryx_attack,
                C.Leonopteryx_defense);
    }

    /** Runtime form of {@link #Leon_stats(StatSource)}. */
    public static MobStats Leon_stats() {
        return Leon_stats(StatSource.RUNTIME);
    }

    /** {@code OreSpawnMain.Crab_stats}: prefix {@code Crab} (OreSpawnMain.java:6196). */
    public static MobStats Crab_stats(StatSource source) {
        return read(source,
                C.Crab_health,
                C.Crab_attack,
                C.Crab_defense);
    }

    /** Runtime form of {@link #Crab_stats(StatSource)}. */
    public static MobStats Crab_stats() {
        return Crab_stats(StatSource.RUNTIME);
    }
}
