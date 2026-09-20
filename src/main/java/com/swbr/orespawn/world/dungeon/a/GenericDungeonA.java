package com.swbr.orespawn.world.dungeon.a;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.OreSpawnWorld;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.tree.LegacyRandom;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;

/**
 * Builder registration for the part of {@code danger.orespawn.GenericDungeon} whose methods start on lines 1-2400
 * (W13, dungeon-a). No original class: 1.7.10 called {@code OreSpawnMain.MyDungeon.makeXxx(world, x, y, z)} from
 * {@code OreSpawnWorld} and {@code DungeonSpawnerBlock}; the port registers each method under the builder id the W12
 * dispatcher already names ({@code OreSpawnWorld.MAKE_*}), replacing the placeholder.
 *
 * <p>Ported here, with their callers:
 * <table>
 * <tr><th>builder id</th><th>method</th><th>world generation</th><th>spawner block case</th></tr>
 * <tr><td>{@code make_dungeon}</td><td>{@link SmallDungeons#makeDungeon}</td><td>Utopia, Mining, Village (feature
 * path {@code addGenericDungeon}), Islands</td><td>21</td></tr>
 * <tr><td>{@code make_enormous_castle} variant 1</td><td>{@link EnormousCastle#makeEnormousCastle}</td><td>Islands</td>
 * <td>2</td></tr>
 * <tr><td>{@code make_rotator_station}</td><td>{@link SmallDungeons#makeRotatorStation}</td><td>Crystal</td><td>3</td></tr>
 * <tr><td>{@code make_bee_hive}</td><td>{@link Hives#makeBeeHive}</td><td>Mining</td><td>4</td></tr>
 * <tr><td>{@code make_haunted_house}</td><td>{@link SmallDungeons#makeHauntedHouse}</td><td>Overworld</td><td>5</td></tr>
 * <tr><td>{@code make_mantis_hive}</td><td>{@link Hives#makeMantisHive}</td><td>Overworld</td><td>6</td></tr>
 * <tr><td>{@code make_kyuubi_dungeon}</td><td>{@link NetherDungeons#makeKyuubiDungeon}</td><td>Mining</td><td>7</td></tr>
 * <tr><td>{@code make_small_bee_hive}</td><td>{@link Hives#makeSmallBeeHive}</td><td>Overworld</td><td>8</td></tr>
 * <tr><td>{@code make_shadow_dungeon}</td><td>{@link NetherDungeons#makeShadowDungeon}</td><td>Mining</td><td>9</td></tr>
 * <tr><td>{@code make_alien_wtf_dungeon}</td><td>{@link AlienAndKnightDungeons#makeAlienWTFDungeon}</td><td>Mining</td>
 * <td>10</td></tr>
 * <tr><td>{@code make_ender_knight_dungeon}</td><td>{@link AlienAndKnightDungeons#makeEnderKnightDungeon}</td>
 * <td>End, Mining</td><td>11</td></tr>
 * <tr><td>{@code make_play_pool}</td><td>{@link SmallDungeons#makePlayPool}</td><td>Overworld</td><td>12</td></tr>
 * <tr><td>{@code make_water_dragon_lair}</td><td>{@link SmallDungeons#makeWaterDragonLair}</td><td>Overworld</td>
 * <td>13</td></tr>
 * <tr><td>{@code make_cloud_shark_dungeon}</td><td>{@link SmallDungeons#makeCloudSharkDungeon}</td><td>Islands</td>
 * <td>14</td></tr>
 * <tr><td>{@code make_leaf_monster_dungeon}</td><td>{@link SmallDungeons#makeLeafMonsterDungeon}</td><td>Overworld</td>
 * <td>15</td></tr>
 * <tr><td>{@code make_mini_dungeon}</td><td>{@link SmallDungeons#makeMiniDungeon}</td><td>Islands</td><td>16</td></tr>
 * </table>
 *
 * <p>Placement (structure sets, spacing and separation in place of {@code recently_placed}, the placement boxes) is
 * W12's and unchanged: every structure above is already dispatched by {@code OreSpawnWorld} under these ids.
 */
public final class GenericDungeonA {

    /**
     * Builder id of {@code makeEnormousCastleQ} (GenericDungeon.java:6421, dungeon-c). W12 dispatches both castles
     * under {@code make_enormous_castle} (variant 1 King, variant 0 Queen); the King half is ported here and hands the
     * Queen variant to this id, so the Queen builder can be registered on its own without replacing the King.
     */
    public static final String MAKE_ENORMOUS_CASTLE_Q = "orespawn:make_enormous_castle_q";

    private GenericDungeonA() {
    }

    /**
     * Registers the builders of this part. Call after {@code OreSpawnWorld.bootstrap()} (whose placeholders fill only
     * empty ids, so the order does not matter either way) and before the server starts. Idempotent.
     */
    public static void bootstrap() {
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_DUNGEON,
                (world, random, x, y, z, variant) -> SmallDungeons.makeDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ENORMOUS_CASTLE, (world, random, x, y, z, variant) -> {
            // addD4Castle (OreSpawnWorld.java:2343): nextInt(2) == 1 ? makeEnormousCastle : makeEnormousCastleQ.
            if (variant == 1) {
                EnormousCastle.makeEnormousCastle(world, random, x, y, z);
            } else {
                final Optional<LegacyStructureRegistry.Builder> queen = LegacyStructureRegistry.builder(MAKE_ENORMOUS_CASTLE_Q);
                if (queen.isPresent()) {
                    queen.get().build(world, random, x, y, z, variant);
                } else {
                    // GenericDungeonCBuilders.bootstrap registers makeEnormousCastleQ here; missing only if that call is removed.
                    OreSpawn.LOG.warn("GenericDungeon: no builder {} for the Queen castle", MAKE_ENORMOUS_CASTLE_Q);
                }
            }
        });
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ROTATOR_STATION,
                (world, random, x, y, z, variant) -> SmallDungeons.makeRotatorStation(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_BEE_HIVE,
                (world, random, x, y, z, variant) -> Hives.makeBeeHive(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_HAUNTED_HOUSE,
                (world, random, x, y, z, variant) -> SmallDungeons.makeHauntedHouse(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_MANTIS_HIVE,
                (world, random, x, y, z, variant) -> Hives.makeMantisHive(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_KYUUBI_DUNGEON,
                (world, random, x, y, z, variant) -> NetherDungeons.makeKyuubiDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_SMALL_BEE_HIVE,
                (world, random, x, y, z, variant) -> Hives.makeSmallBeeHive(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_SHADOW_DUNGEON,
                (world, random, x, y, z, variant) -> NetherDungeons.makeShadowDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ALIEN_WTF_DUNGEON,
                (world, random, x, y, z, variant) -> AlienAndKnightDungeons.makeAlienWTFDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ENDER_KNIGHT_DUNGEON,
                (world, random, x, y, z, variant) -> AlienAndKnightDungeons.makeEnderKnightDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_PLAY_POOL,
                (world, random, x, y, z, variant) -> SmallDungeons.makePlayPool(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_WATER_DRAGON_LAIR,
                (world, random, x, y, z, variant) -> SmallDungeons.makeWaterDragonLair(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_CLOUD_SHARK_DUNGEON,
                (world, random, x, y, z, variant) -> SmallDungeons.makeCloudSharkDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_LEAF_MONSTER_DUNGEON,
                (world, random, x, y, z, variant) -> SmallDungeons.makeLeafMonsterDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_MINI_DUNGEON,
                (world, random, x, y, z, variant) -> SmallDungeons.makeMiniDungeon(world, random, x, y, z));
    }

    /**
     * Runs a registered GenericDungeon builder on a live level, the way {@code DungeonSpawnerBlock.updateTick} called
     * {@code OreSpawnMain.MyDungeon.makeXxx(world, x, y, z)}: unclipped, with {@code world.rand}. Works for every
     * builder id, including those of dungeon-b and dungeon-c once they are registered.
     *
     * @return whether a builder was registered under the id
     */
    public static boolean buildLive(final ServerLevel level, final String builderId, final int x, final int y,
                                    final int z, final int variant) {
        final Optional<LegacyStructureRegistry.Builder> builder = LegacyStructureRegistry.builder(builderId);
        builder.ifPresent(b -> b.build(StructureWriter.unclipped(level), new LegacyRandom(level.random), x, y, z, variant));
        return builder.isPresent();
    }
}
