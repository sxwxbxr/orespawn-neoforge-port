package com.swbr.orespawn.world.dungeon.c;

import com.swbr.orespawn.world.OreSpawnWorld;
import com.swbr.orespawn.world.dungeon.a.GenericDungeonA;
import com.swbr.orespawn.world.dungeon.b.GenericDungeonB3;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;

/**
 * Registers the builders of porter c under the {@code orespawn:make_*} ids that W12 left as placeholders
 * ({@code OreSpawnWorld.registerBuilders}); {@code registerBuilder} replaces a placeholder, and the placeholder loop
 * skips an id that already has a builder, so the call order does not matter. No original class: 1.7.10 called
 * {@code OreSpawnMain.MyDungeon.makeXxx(world, x, y, z)} directly.
 *
 * <p>Two ids are shared with the King half of the class:
 * <ul>
 * <li>{@code MAKE_ENORMOUS_CASTLE}: dungeon-a registers the dispatcher (variant 1 King) and hands variant 0 to
 * {@link GenericDungeonA#MAKE_ENORMOUS_CASTLE_Q}, which is registered here.</li>
 * <li>{@code MAKE_KING_ALTAR}: W12 rolls variant 1 for the Queen ({@code OreSpawnWorld.addKingAltar}, :2737); no other
 * porter registers the id, so the dispatching builder is registered here, variant 0 calling dungeon-b's
 * {@link GenericDungeonB3#makeKingAltar}.</li>
 * </ul>
 */
public final class GenericDungeonCBuilders {

    private GenericDungeonCBuilders() {
    }

    public static void bootstrap() {
        LegacyStructureRegistry.registerBuilder(GenericDungeonA.MAKE_ENORMOUS_CASTLE_Q,
                (world, random, x, y, z, variant) -> GenericDungeonQueen.makeEnormousCastleQ(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_KING_ALTAR, (world, random, x, y, z, variant) -> {
            // addKingAltar (OreSpawnWorld.java:2737): nextInt(2) == 0 ? makeKingAltar : makeQueenAltar, W12 variant 1 Queen.
            if (variant == 1) {
                GenericDungeonQueen.makeQueenAltar(world, random, x, y, z);
            } else {
                GenericDungeonB3.makeKingAltar(world, random, x, y, z);
            }
        });
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_CRYSTAL_BATTLE_TOWER,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeCrystalBattleTower(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_GIRLFRIEND_ISLAND,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeGirlfriendIsland(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_GREENHOUSE_DUNGEON,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeGreenhouseDungeon(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_MONSTER_ISLAND,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeMonsterIsland(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_NIGHTMARE_ROOKERY,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeNightmareRookery(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_STINKY_HOUSE,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeStinkyHouse(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_RUBBER_DUCKY_POND,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeRubberDuckyPond(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_WHITE_HOUSE,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeWhiteHouse(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_FROG_POND,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeFrogPond(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_PUMPKIN,
                (world, random, x, y, z, variant) -> GenericDungeonC.makePumpkin(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ROUND_ROTATOR,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeRoundRotator(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_RAINBOW,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeRainbow(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_SPIDER_HANGOUT,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeSpiderHangout(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_RED_ANT_HANGOUT,
                (world, random, x, y, z, variant) -> GenericDungeonC.makeRedAntHangout(world, random, x, y, z));
    }
}
