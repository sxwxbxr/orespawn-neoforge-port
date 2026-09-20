package com.swbr.orespawn.gametest;

import com.google.common.collect.ImmutableList;
import com.swbr.orespawn.OreSpawn;
import java.lang.reflect.Field;
import javax.annotation.Nullable;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import com.swbr.orespawn.world.OreSpawnWorld;
import com.swbr.orespawn.world.dungeon.GenericDungeonHelpers;
import com.swbr.orespawn.world.dungeon.a.GenericDungeonA;
import com.swbr.orespawn.world.structure.LegacyStructurePass;
import com.swbr.orespawn.world.structure.LegacyStructurePiece;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry.Placement;
import com.swbr.orespawn.world.structure.StructureRecording;
import com.swbr.orespawn.world.structure.StructureWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;

/**
 * Wave 13 integration: every GenericDungeon builder, as {@code OreSpawnWorld} places it, writes only inside its
 * placement box (catalogue 4.13, DECISIONS R12). A write outside the box is logged by the recording but never replayed,
 * so a box that is too small silently cuts the structure - exactly the class of error the circle builders of range c
 * showed at negative origins.
 *
 * <p>The boxes below are the extents OreSpawnWorld passes to {@code w13(...)} (which adds one block on every side); the
 * dispatcher's own origin offsets ({@code posY + dy}) are already part of the builder origin. Origins: one at positive
 * and one at negative block coordinates, each at chunk-local 0 and 15, three seeds.
 *
 * <p>Further tests of the wave (catalogue 4.13):
 * <ul>
 * <li>every placement once through {@code LegacyStructurePiece.postProcess} on a live level, nothing changed outside its
 * box;</li>
 * <li>five samples (King challenge castle, Inca Pyramid, Robot Lab, White House, Cloud Shark Dungeon) with the spawner
 * mob ids and counts of the original and chests with loot;</li>
 * <li>real world generation with structures switched on: spawn chunks of the overworld, the End and all six OreSpawn
 * dimensions, GenericDungeon starts of every structure set generated to FULL and compared with their recording, no
 * far chunk write;</li>
 * <li>no W13 TODO marker left in the sources.</li>
 * </ul>
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W13GameTests {

    private static final String ARENA = "arena";

    private W13GameTests() {}

    /** One placement of a builder: id, variant, origin Y, extents as in OreSpawnWorld. */
    private record Box(String id, int variant, int y, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        Placement at(int x, int z) {
            return this.at(x, this.y, z);
        }

        Placement at(int x, int y, int z) {
            return Placement.of(this.id, x, y, z, this.variant,
                    this.minX - 1, this.minY - 1, this.minZ - 1, this.maxX + 1, this.maxY + 1, this.maxZ + 1);
        }
    }

    private static final int GROUND = 64;

    private static final Box[] BOXES = {
            // overworldDungeon (OreSpawnWorld overworldWater / overworldGround / addFrogPond / addANest / addHauntedHouse)
            new Box(OreSpawnWorld.MAKE_PLAY_POOL, 0, GROUND, -1, 4, 16, 18, 0, 0),
            new Box(OreSpawnWorld.MAKE_WATER_DRAGON_LAIR, 0, GROUND, -10, 10, -1, 7, -10, 10),
            new Box(OreSpawnWorld.MAKE_GOLD_FISH_BOWL, 0, GROUND, -1, 5, 1, 8, -1, 5),
            new Box(OreSpawnWorld.MAKE_GIRLFRIEND_ISLAND, 0, GROUND, -5, 5, -1, 4, -3, 3),
            new Box(OreSpawnWorld.MAKE_MONSTER_ISLAND, 0, GROUND, -5, 5, -1, 4, -3, 3),
            new Box(OreSpawnWorld.MAKE_FROG_POND, 0, GROUND, -3, 3, 0, 2, -3, 3),
            new Box(OreSpawnWorld.MAKE_SMALL_BEE_HIVE, 0, GROUND, -3, 9, 1, 21, -3, 9),
            new Box(OreSpawnWorld.MAKE_MANTIS_HIVE, 0, GROUND, 0, 12, -6, 19, 0, 12),
            new Box(OreSpawnWorld.MAKE_HAUNTED_HOUSE, 0, GROUND, -3, 3, 0, 4, -3, 3),
            new Box(OreSpawnWorld.MAKE_LEAF_MONSTER_DUNGEON, 0, GROUND, -3, 6, -4, 16, -3, 6),
            new Box(OreSpawnWorld.MAKE_SPIT_BUG_LAIR, 0, GROUND, -8, 8, 0, 12, -8, 8),
            new Box(OreSpawnWorld.MAKE_IGLOO, 0, GROUND, -6, 6, 0, 5, -6, 6),
            new Box(OreSpawnWorld.MAKE_BOUNCY_CASTLE, 0, GROUND, -4, 4, 0, 4, -4, 4),
            new Box(OreSpawnWorld.MAKE_RUBBER_DUCKY_POND, 0, GROUND, -5, 6, 0, 6, -5, 5),
            // endDungeon
            new Box(OreSpawnWorld.MAKE_ENDER_KNIGHT_DUNGEON, 0, GROUND, 0, 12, 0, 5, -2, 6),
            new Box(OreSpawnWorld.MAKE_ENDER_REAPER_GRAVEYARD, 0, GROUND, 0, 10, -4, 4, 0, 12),
            new Box(OreSpawnWorld.MAKE_ENDER_DRAGON_HOSPITAL, 0, GROUND, -6, 9, 0, 9, 0, 9),
            new Box(OreSpawnWorld.MAKE_ENDER_CASTLE, 0, GROUND, -4, 26, 0, 16, -4, 26),
            // Utopia king altar, both variants
            new Box(OreSpawnWorld.MAKE_KING_ALTAR, 0, GROUND, -5, 55, -9, 58, -5, 55),
            new Box(OreSpawnWorld.MAKE_KING_ALTAR, 1, GROUND, -5, 55, -9, 58, -5, 55),
            // miningDungeon
            new Box(OreSpawnWorld.MAKE_KYUUBI_DUNGEON, 0, GROUND, 0, 34, -22, 5, -15, 14),
            new Box(OreSpawnWorld.MAKE_BEE_HIVE, 0, GROUND, 0, 9, -30, 0, 0, 9),
            new Box(OreSpawnWorld.MAKE_SHADOW_DUNGEON, 0, GROUND, 0, 18, -9, 9, 0, 18),
            new Box(OreSpawnWorld.MAKE_ALIEN_WTF_DUNGEON, 0, GROUND, -19, 17, -17, 2, -21, 15),
            new Box(OreSpawnWorld.MAKE_LEON_NEST, 0, GROUND, -10, 10, -10, 5, -10, 10),
            // villageDungeon
            new Box(OreSpawnWorld.MAKE_DAMSEL_IN_DISTRESS, 0, GROUND, -4, 4, 0, 9, -4, 4),
            new Box(OreSpawnWorld.MAKE_SPIDER_HANGOUT, 0, GROUND, 0, 19, -1, 19, 0, 19),
            new Box(OreSpawnWorld.MAKE_RED_ANT_HANGOUT, 0, GROUND, 0, 15, -1, 15, 0, 15),
            // islandsDungeon
            new Box(OreSpawnWorld.MAKE_RAINBOW, 0, 80, -14, 13, 26, 40, -3, 3),
            new Box(OreSpawnWorld.MAKE_ENORMOUS_CASTLE, 1, GROUND, -37, 55, -1, 80, -28, 55),
            new Box(OreSpawnWorld.MAKE_ENORMOUS_CASTLE, 0, GROUND, -37, 55, -1, 80, -28, 55),
            new Box(OreSpawnWorld.MAKE_DUNGEON, 0, GROUND, 0, 11, 0, 5, 0, 11),
            new Box(OreSpawnWorld.MAKE_INCA_PYRAMID, 0, GROUND, -10, 50, 0, 19, -10, 40),
            new Box(OreSpawnWorld.MAKE_ROBOT_LAB, 0, GROUND, -10, 19, 0, 43, -1, 48),
            new Box(OreSpawnWorld.MAKE_MINI_DUNGEON, 0, GROUND, -6, 9, 0, 11, 0, 9),
            new Box(OreSpawnWorld.MAKE_CEPHADROME_ALTAR, 0, GROUND, -4, 4, 0, 4, -4, 4),
            new Box(OreSpawnWorld.MAKE_GREENHOUSE_DUNGEON, 0, GROUND, 0, 22, 0, 13, -1, 14),
            new Box(OreSpawnWorld.MAKE_NIGHTMARE_ROOKERY, 0, GROUND, -6, 21, 0, 21, -53, 53),
            new Box(OreSpawnWorld.MAKE_STINKY_HOUSE, 0, GROUND, -5, 19, 1, 3, -4, 12),
            new Box(OreSpawnWorld.MAKE_WHITE_HOUSE, 0, GROUND, -5, 21, 0, 21, -15, 18),
            new Box(OreSpawnWorld.MAKE_PUMPKIN, 0, GROUND, 0, 13, 0, 17, 0, 11),
            new Box(OreSpawnWorld.MAKE_CLOUD_SHARK_DUNGEON, 0, 150, -1, 1, -1, 1, -1, 1),
            // crystalDungeon
            new Box(OreSpawnWorld.MAKE_ROTATOR_STATION, 0, GROUND, 0, 0, 4, 8, 0, 0),
            new Box(OreSpawnWorld.MAKE_URCHIN_SPAWNER, 0, GROUND, -17, 18, -1, 17, -17, 18),
            new Box(OreSpawnWorld.MAKE_CRYSTAL_HAUNTED_HOUSE, 0, GROUND, -3, 3, 0, 4, -3, 3),
            new Box(OreSpawnWorld.MAKE_ROUND_ROTATOR, 0, GROUND, -6, 7, 0, 12, 0, 0),
            new Box(OreSpawnWorld.MAKE_CRYSTAL_BATTLE_TOWER, 0, GROUND, -10, 11, 0, 23, -10, 11),
    };

    /**
     * Records every builder of {@link #BOXES} (all 45 ids, both castle and both altar variants) through
     * {@link StructureRecording#record} and counts logged writes outside its placement box. Also: every builder writes
     * at least one block, and the Queen castle id of dungeon-c is registered.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400)
    public static void everyGenericDungeonBuilderWritesOnlyInsideItsPlacementBox(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        List<String> failures = new ArrayList<>();
        if (LegacyStructureRegistry.builder(GenericDungeonA.MAKE_ENORMOUS_CASTLE_Q).isEmpty()) {
            failures.add("no builder " + GenericDungeonA.MAKE_ENORMOUS_CASTLE_Q);
        }
        int[][] origins = {{1_048_576 + 16 * 3, 1_048_576 + 16 * 5}, {-1_048_576 - 16 * 7, -1_048_576 - 16 * 2}};
        int[] locals = {0, 15};
        long runs = 0;
        for (Box box : BOXES) {
            Optional<LegacyStructureRegistry.Builder> builder = LegacyStructureRegistry.builder(box.id());
            if (builder.isEmpty()) {
                failures.add(box.id() + ": no builder");
                continue;
            }
            int worstOutside = 0;
            String worst = null;
            int minWrites = Integer.MAX_VALUE;
            int maxWrites = 0;
            for (int[] origin : origins) {
                for (int local : locals) {
                    for (long seed = 1; seed <= 3; seed++) {
                        Placement p = box.at(origin[0] + local, origin[1] + local);
                        long s = seed;
                        StructureRecording recording = StructureRecording.record(level, generator, p.box(), writer ->
                                builder.get().build(writer, new Random(s * 341873128712L + p.x()), p.x(), p.y(), p.z(),
                                        p.variant()));
                        runs++;
                        int outside = recording.writesOutside(p.box());
                        minWrites = Math.min(minWrites, recording.writes());
                        maxWrites = Math.max(maxWrites, recording.writes());
                        if (outside > worstOutside) {
                            worstOutside = outside;
                            worst = "origin " + p.x() + "," + p.y() + "," + p.z() + " seed " + seed;
                        }
                    }
                }
            }
            if (worstOutside != 0) {
                failures.add(box.id() + " v" + box.variant() + ": " + worstOutside + " writes outside the box at " + worst);
            }
            if (minWrites == 0) {
                failures.add(box.id() + " v" + box.variant() + ": a run wrote no block");
            }
            OreSpawn.LOG.info("W13 box test {} v{}: writes {}..{}, most outside {}", box.id(), box.variant(), minWrites,
                    maxWrites, worstOutside);
        }
        OreSpawn.LOG.info("W13 box test: {} recordings of {} placements", runs, BOXES.length);
        if (failures.isEmpty()) {
            helper.succeed();
        } else {
            helper.fail(String.join("; ", failures.size() > 16 ? failures.subList(0, 16) : failures));
        }
    }

    // =====================================================================================================
    // 2. every GenericDungeon structure placed once through its piece on a live level

    /** What one placement through {@link LegacyStructurePiece#postProcess} left in the level. */
    private record Placed(Placement placement, int chunks, int changedInside, int changedOutside, String outsideNote) {
    }

    /**
     * Places {@code box} once as an {@code orespawn:legacy} piece: a fresh far area of the live overworld, box minimum at
     * chunk-local 15 (so it crosses chunk borders), origin Y on the generator's surface for ground builds, every chunk
     * of the box post-processed in order, every second chunk through a piece reloaded from its own NBT (the path a
     * saved structure start takes). The window (box plus 3) and one chunk ring around it are loaded to {@code FULL}
     * before the snapshot, so no later feature step can write into it; everything runs inside one tick, so no block tick
     * changes it either. Outside the box only the block type counts (shape updates of neighbours change properties
     * on a live level, never the block).
     */
    private static Placed placeThroughPiece(ServerLevel level, Random pick, Box box, long seed) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        Placement rel = box.at(0, box.y(), 0);
        int spanX = rel.box().getXSpan();
        int spanZ = rel.box().getZSpan();
        ChunkPos area = freshArea(level, pick, spanX + 80, spanZ + 80);
        int x = area.getMinBlockX() + 32 + 15 - rel.box().minX();
        int z = area.getMinBlockZ() + 32 + 15 - rel.box().minZ();
        int y = box.y() == GROUND
                ? generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState)
                : box.y();
        Placement p = box.at(x, y, z);
        BoundingBox pb = p.box();
        BoundingBox window = new BoundingBox(pb.minX() - 3, Math.max(pb.minY() - 3, level.getMinBuildHeight()), pb.minZ() - 3,
                pb.maxX() + 3, Math.min(pb.maxY() + 3, level.getMaxBuildHeight() - 1), pb.maxZ() + 3);
        for (int cx = (window.minX() >> 4) - 1; cx <= (window.maxX() >> 4) + 1; cx++) {
            for (int cz = (window.minZ() >> 4) - 1; cz <= (window.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz);
            }
        }
        BlockState[] before = snapshot(level, window);

        StructureRecording.clearCache();
        LegacyStructurePiece piece = new LegacyStructurePiece(p, seed);
        LegacyStructurePiece reloaded = new LegacyStructurePiece(
                piece.createTag(StructurePieceSerializationContext.fromLevel(level)));
        int chunks = 0;
        for (int cx = pb.minX() >> 4; cx <= pb.maxX() >> 4; cx++) {
            for (int cz = pb.minZ() >> 4; cz <= pb.maxZ() >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                        chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
                ((chunks & 1) == 0 ? piece : reloaded).postProcess(level, level.structureManager(), generator,
                        RandomSource.create(chunk.toLong()), chunkBox, chunk, chunk.getMiddleBlockPosition(p.y()));
                chunks++;
            }
        }
        StructureRecording.clearCache();

        int inside = 0;
        int outside = 0;
        StringBuilder note = new StringBuilder();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int i = 0;
        for (int wx = window.minX(); wx <= window.maxX(); wx++) {
            for (int wz = window.minZ(); wz <= window.maxZ(); wz++) {
                for (int wy = window.minY(); wy <= window.maxY(); wy++, i++) {
                    BlockState now = level.getBlockState(pos.set(wx, wy, wz));
                    if (pb.isInside(wx, wy, wz)) {
                        if (now != before[i]) {
                            inside++;
                        }
                    } else if (!now.is(before[i].getBlock())) {
                        outside++;
                        if (outside <= 3) {
                            note.append(pos.toShortString()).append(' ').append(before[i]).append(" -> ").append(now).append("; ");
                        }
                    }
                }
            }
        }
        return new Placed(p, chunks, inside, outside, note.toString());
    }

    private static BlockState[] snapshot(ServerLevel level, BoundingBox window) {
        BlockState[] states = new BlockState[window.getXSpan() * window.getYSpan() * window.getZSpan()];
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int i = 0;
        for (int wx = window.minX(); wx <= window.maxX(); wx++) {
            for (int wz = window.minZ(); wz <= window.maxZ(); wz++) {
                for (int wy = window.minY(); wy <= window.maxY(); wy++, i++) {
                    states[i] = level.getBlockState(pos.set(wx, wy, wz));
                }
            }
        }
        return states;
    }

    private static void discardEntities(ServerLevel level, BoundingBox box) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, AABB.of(box).inflate(4.0), e -> !(e instanceof Player))) {
            entity.discard();
        }
    }

    /** Areas whose chunks (plus one around) were never loaded or saved, far out (as W13WriterGameTests.freshAreas). */
    private static ChunkPos freshArea(ServerLevel level, Random pick, int spanX, int spanZ) {
        ServerChunkCache source = level.getChunkSource();
        int chunksX = (spanX + 15) / 16 + 2;
        int chunksZ = (spanZ + 15) / 16 + 2;
        for (int attempt = 0; attempt <= 64; attempt++) {
            int cx = -118_000 + pick.nextInt(46_000);
            int cz = 72_000 + pick.nextInt(46_000);
            boolean fresh = true;
            for (int dx = -1; dx < chunksX && fresh; dx++) {
                for (int dz = -1; dz < chunksZ && fresh; dz++) {
                    if (source.getChunkNow(cx + dx, cz + dz) != null
                            || source.chunkMap.read(new ChunkPos(cx + dx, cz + dz)).join().isPresent()) {
                        fresh = false;
                    }
                }
            }
            if (fresh) {
                return new ChunkPos(cx, cz);
            }
        }
        throw new IllegalStateException("no fresh area of " + chunksX + "x" + chunksZ + " chunks found");
    }

    /**
     * Catalogue 4.13 "jede Struktur einmal platziert": all 47 placements of {@link #BOXES} (45 ids, both castle and both
     * altar variants), each once through {@link LegacyStructurePiece#postProcess} for every chunk of its box on a live
     * level. The build must change blocks inside the box and must not change a single block type in the three-block
     * shell around it.
     */
    @GameTest(template = ARENA, timeoutTicks = 6000)
    public static void everyGenericDungeonStructurePlacesOnceThroughItsPieceInsideItsBox(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long runSeed = System.nanoTime();
        Random pick = new Random(runSeed);
        List<String> failures = new ArrayList<>();
        for (int n = 0; n < BOXES.length; n++) {
            Box box = BOXES[n];
            long seed = runSeed + 7919L * n;
            try {
                Placed placed = placeThroughPiece(level, pick, box, seed);
                discardEntities(level, placed.placement().box());
                String name = box.id() + " v" + box.variant();
                if (placed.changedInside() == 0) {
                    failures.add(name + ": nothing changed inside " + placed.placement().box());
                }
                if (placed.changedOutside() != 0) {
                    failures.add(name + ": " + placed.changedOutside() + " blocks changed outside the box, " + placed.outsideNote());
                }
                OreSpawn.LOG.info("W13 placement {} at {},{},{} seed {}: {} chunks, {} blocks changed inside, {} outside",
                        name, placed.placement().x(), placed.placement().y(), placed.placement().z(), seed, placed.chunks(),
                        placed.changedInside(), placed.changedOutside());
            } catch (RuntimeException e) {
                OreSpawn.LOG.error("W13 placement {} v{} threw", box.id(), box.variant(), e);
                failures.add(box.id() + " v" + box.variant() + ": " + e);
            }
        }
        report(helper, failures);
    }

    // =====================================================================================================
    // 3. five samples: spawners with their original mob ids, chests with loot

    /** Expected spawners (id to min..max, every other id forbidden) and chests (min..max, how many must hold loot). */
    private record Sample(Box box, Map<String, int[]> spawners, int minSpawners, int minChests, int maxChests,
                          boolean everyChestHoldsLoot) {
    }

    private static Box box(String id, int variant) {
        for (Box box : BOXES) {
            if (box.id().equals(id) && box.variant() == variant) {
                return box;
            }
        }
        throw new IllegalArgumentException(id + " v" + variant);
    }

    private static Map<String, int[]> spawners(Object... idMinMax) {
        Map<String, int[]> map = new TreeMap<>();
        for (int i = 0; i < idMinMax.length; i += 3) {
            map.put((String) idMinMax[i], new int[] {(Integer) idMinMax[i + 1], (Integer) idMinMax[i + 2]});
        }
        return map;
    }

    /** The spawner's {@code SpawnData} entity id; empty when the spawner holds none. */
    private static String spawnerId(ServerLevel level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
            return "<no block entity>";
        }
        CompoundTag tag = spawner.saveWithoutMetadata(level.registryAccess());
        return tag.getCompound("SpawnData").getCompound("entity").getString("id");
    }

    /**
     * The five samples of the wave plan, each placed once through its piece: King challenge castle (variant 1), Inca
     * Pyramid, Robot Lab, White House, Cloud Shark Dungeon. Spawner ids and counts are read from the original
     * {@code GenericDungeon.java} (line numbers below) and mapped through the catalogue table
     * ({@link GenericDungeonHelpers#entityId}); an id whose entity is not registered yet (W09-W11) expects an empty
     * spawner, as the W12 convention says. Chests are counted and must hold loot.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400)
    public static void fiveSampleStructuresHoldTheirOriginalSpawnersAndChestLoot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long runSeed = System.nanoTime();
        Random pick = new Random(runSeed);
        int any = Integer.MAX_VALUE;
        List<Sample> samples = List.of(
                // makeEnormousCastle :223-417: 4x4 Terrible Terror (:285-305), 3 Emperor Scorpion (:306-321), level 1
                // always "Cloud Shark" x16 (:323, buildLevel :500-519); higher levels and addLevelDecorations
                // (:524-768) add their critters by the rolled level. fill_chests: four per decorated level.
                new Sample(box(OreSpawnWorld.MAKE_ENORMOUS_CASTLE, 1), spawners(
                        id("Terrible Terror"), 16, 16, id("Emperor Scorpion"), 3, 3, id("Cloud Shark"), 16, 16,
                        id("Lurking Terror"), 0, 16, id("Rotator"), 0, 16, id("Bee"), 0, 16, id("Mantis"), 0, 16,
                        id("Mothra"), 0, 16, id("Nightmare"), 0, any, id("Large Worm"), 0, any, id("Alosaurus"), 0, any,
                        id("Basilisk"), 0, any, id("Hammerhead"), 0, any, id("Hercules Beetle"), 0, any,
                        id("Jumpy Bug"), 0, any, id("T. Rex"), 0, any), 35, 4, any, false),
                // makeIncaPyramid :3737-4000: one Molenoid (:3983); makeincagraves(41x31) = 4 rows of 6 graves, each a
                // chest and a Ghost spawner on nextInt(3) == 1 (:4015-4082)
                new Sample(box(OreSpawnWorld.MAKE_INCA_PYRAMID, 0), spawners(
                        id("Molenoid"), 1, 1, id("Ghost"), 0, 24), 1, 24, 24, true),
                // makeRobotLab :4085-4130: six pillars + makerobotower's four (:4231-4234), one Robo-Sniper each
                // (makerobopillar :4152-4165); makeroboaltar two Robo-Pounder (:4285-4294); makerobotreasureroom one
                // Robo-Warrior and two chests (:4373-4388)
                new Sample(box(OreSpawnWorld.MAKE_ROBOT_LAB, 0), spawners(
                        id("Robo-Sniper"), 10, 10, id("Robo-Pounder"), 2, 2, id("Robo-Warrior"), 1, 1), 13, 2, 2, true),
                // makeWhiteHouse :5689-5732: four Criminal spawners, each over a chest
                new Sample(box(OreSpawnWorld.MAKE_WHITE_HOUSE, 0), spawners(id("Criminal"), 4, 4), 4, 4, 4, true),
                // makeCloudSharkDungeon :2086-2118: four Cloud Shark spawners around glowstone, one chest on top
                new Sample(box(OreSpawnWorld.MAKE_CLOUD_SHARK_DUNGEON, 0), spawners(id("Cloud Shark"), 4, 4), 4, 1, 1, true));

        List<String> failures = new ArrayList<>();
        for (int n = 0; n < samples.size(); n++) {
            Sample sample = samples.get(n);
            String name = sample.box().id() + " v" + sample.box().variant();
            long seed = runSeed + 104_729L * n;
            Placed placed;
            try {
                placed = placeThroughPiece(level, pick, sample.box(), seed);
            } catch (RuntimeException e) {
                OreSpawn.LOG.error("W13 sample {} threw", name, e);
                failures.add(name + ": " + e);
                continue;
            }
            BoundingBox pb = placed.placement().box();
            Map<String, Integer> found = new TreeMap<>();
            int spawnerCount = 0;
            int chests = 0;
            int chestsWithLoot = 0;
            int items = 0;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = pb.minX(); x <= pb.maxX(); x++) {
                for (int z = pb.minZ(); z <= pb.maxZ(); z++) {
                    for (int y = Math.max(pb.minY(), level.getMinBuildHeight()); y <= Math.min(pb.maxY(), level.getMaxBuildHeight() - 1); y++) {
                        BlockState state = level.getBlockState(pos.set(x, y, z));
                        if (state.is(Blocks.SPAWNER)) {
                            spawnerCount++;
                            found.merge(spawnerId(level, pos), 1, Integer::sum);
                        } else if (state.is(Blocks.CHEST) && level.getBlockEntity(pos) instanceof Container chest) {
                            chests++;
                            int here = 0;
                            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                                here += chest.getItem(slot).isEmpty() ? 0 : 1;
                            }
                            items += here;
                            chestsWithLoot += here > 0 ? 1 : 0;
                        }
                    }
                }
            }
            discardEntities(level, pb);

            // expected ids, with unregistered entities folded into "" (empty spawner)
            Map<String, int[]> expected = new TreeMap<>();
            for (Map.Entry<String, int[]> e : sample.spawners().entrySet()) {
                String key = StructureWriter.entityType(e.getKey()).isPresent() ? e.getKey() : "";
                int[] range = expected.computeIfAbsent(key, k -> new int[] {0, 0});
                range[0] += e.getValue()[0];
                range[1] = (int) Math.min(Integer.MAX_VALUE, (long) range[1] + e.getValue()[1]);
            }
            for (Map.Entry<String, Integer> e : found.entrySet()) {
                int[] range = expected.get(e.getKey());
                if (range == null) {
                    failures.add(name + ": " + e.getValue() + " spawners of unexpected id '" + e.getKey() + "'");
                } else if (e.getValue() < range[0] || e.getValue() > range[1]) {
                    failures.add(name + ": " + e.getValue() + " spawners of " + e.getKey() + ", expected " + range[0] + ".." + range[1]);
                }
            }
            for (Map.Entry<String, int[]> e : expected.entrySet()) {
                if (e.getValue()[0] > 0 && !found.containsKey(e.getKey())) {
                    failures.add(name + ": no spawner of " + e.getKey() + ", expected " + e.getValue()[0] + ".." + e.getValue()[1]);
                }
            }
            if (spawnerCount < sample.minSpawners()) {
                failures.add(name + ": " + spawnerCount + " spawners, expected at least " + sample.minSpawners());
            }
            if (chests < sample.minChests() || chests > sample.maxChests()) {
                failures.add(name + ": " + chests + " chests, expected " + sample.minChests() + ".." + sample.maxChests());
            }
            if (sample.everyChestHoldsLoot() ? chestsWithLoot != chests : chestsWithLoot == 0) {
                failures.add(name + ": " + chestsWithLoot + " of " + chests + " chests hold loot");
            }
            OreSpawn.LOG.info("W13 sample {} at {} seed {}: spawners {} {}, chests {} ({} with loot, {} stacks)", name,
                    placed.placement().x() + "," + placed.placement().y() + "," + placed.placement().z(), seed,
                    spawnerCount, found, chests, chestsWithLoot, items);
        }
        report(helper, failures);
    }

    private static String id(String legacyName) {
        return GenericDungeonHelpers.entityId(legacyName);
    }

    // =====================================================================================================
    // 4. real world generation in every dimension

    private static final String FAR_CHUNK = "Detected setBlock in a far chunk";

    /** Dimension to the structure sets of GenericDungeon builds that generate there. */
    private static Map<String, List<String>> dimensionSets() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("minecraft:overworld", List.of("overworld_dungeon"));
        map.put("minecraft:the_end", List.of("end_dungeon"));
        map.put("orespawn:utopia", List.of("utopia_king_altar"));
        map.put("orespawn:mining", List.of("mining_dungeon"));
        map.put("orespawn:village", List.of("village_dungeon"));
        map.put("orespawn:danger", List.of("islands_dungeon", "islands_cloud_shark"));
        map.put("orespawn:crystal", List.of("crystal_dungeon"));
        map.put("orespawn:chaos", List.of());
        return map;
    }

    private static Set<String> genericDungeonIds() {
        Set<String> ids = new HashSet<>();
        for (Box box : BOXES) {
            ids.add(box.id());
        }
        return ids;
    }

    /**
     * Catalogue 4.13 on the real generation path, with an appender on {@code net.minecraft.Util} for the whole test:
     * <ul>
     * <li>the spawn chunks (3x3 around the overworld spawn, around 0,0 elsewhere) of the overworld, the End and all six
     * OreSpawn dimensions reach {@code FULL};</li>
     * <li>for every structure set that carries GenericDungeon builds, the first potential chunk of its
     * {@code random_spread} placement in a fresh far region whose {@code Structure.generate} yields a GenericDungeon
     * piece is generated for real: every chunk of the piece box to {@code FULL}. The start must be stored in its chunk
     * with the same builder, origin and box, and replaying the piece's own recording over the generated world must
     * change nothing (DECISIONS R24: nothing generated later may overwrite a recorded write) - the structure is there,
     * complete, and no chunk seam or later decoration cut it;</li>
     * <li>no {@code Detected setBlock in a far chunk} appears.</li>
     * </ul>
     */
    @GameTest(template = ARENA, timeoutTicks = 12000)
    public static void spawnChunksAndGenericDungeonStructuresGenerateInEveryDimensionWithoutFarChunkWrites(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        MinecraftServer server = helper.getLevel().getServer();
        RegistryAccess registries = server.registryAccess();
        Set<String> dungeonIds = genericDungeonIds();
        Random pick = new Random(System.nanoTime());
        FarChunkAppender appender = FarChunkAppender.attach();
        Field generateStructures = declaredField(WorldOptions.class, "generateStructures");
        WorldOptions options = server.getWorldData().worldGenOptions();
        boolean structuresBefore = options.generateStructures();
        try {
            // The GameTest server builds its world with WorldOptions(0, false, false): no structure start is ever created
            // (ChunkStatusTasks.generateStructureStarts). Switched on for this test only.
            generateStructures.setBoolean(options, true);
            for (Pinned pinned : PINNED) {
                try {
                    generatePinned(helper, registries, pinned, failures);
                } catch (RuntimeException e) {
                    OreSpawn.LOG.error("W13 pinned case {} threw", pinned, e);
                    failures.add(pinned + ": " + e);
                }
            }
            for (Map.Entry<String, List<String>> entry : dimensionSets().entrySet()) {
                ResourceLocation dimId = ResourceLocation.parse(entry.getKey());
                ServerLevel level = dimId.getNamespace().equals(OreSpawn.MOD_ID)
                        ? W05GameTests.dimensionLevel(helper, dimId.getPath())
                        : server.getLevel(ResourceKey.create(Registries.DIMENSION, dimId));
                if (level == null) {
                    failures.add(entry.getKey() + ": level missing");
                    continue;
                }
                ServerLevel structureLevel = level;
                if (level == server.overworld() && !(level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator)) {
                    // The GameTest overworld is superflat, whose structure state holds only its preset's sets.
                    structureLevel = noiseOverworld(helper);
                }
                ChunkPos spawn = level == server.overworld() ? new ChunkPos(level.getSharedSpawnPos()) : new ChunkPos(0, 0);
                long start = System.nanoTime();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        LevelChunk chunk = level.getChunk(spawn.x + dx, spawn.z + dz);
                        if (chunk.getPersistedStatus() != ChunkStatus.FULL) {
                            failures.add(entry.getKey() + ": spawn chunk " + chunk.getPos() + " is " + chunk.getPersistedStatus());
                        }
                    }
                }
                OreSpawn.LOG.info("W13 dimension {}: spawn chunks around {} FULL in {} ms", entry.getKey(), spawn,
                        (System.nanoTime() - start) / 1_000_000);
                for (String setName : entry.getValue()) {
                    try {
                        generateOneStructure(structureLevel, registries, setName, dungeonIds, pick, failures);
                    } catch (RuntimeException e) {
                        OreSpawn.LOG.error("W13 structure set {} threw", setName, e);
                        failures.add(setName + ": " + e);
                    }
                }
            }
            List<String> far = appender.messages();
            for (int i = 0; i < Math.min(far.size(), 6); i++) {
                failures.add(far.get(i));
            }
            if (far.size() > 6) {
                failures.add((far.size() - 6) + " more far chunk writes");
            }
            OreSpawn.LOG.info("W13 dimension test: {} far chunk writes", far.size());
        } catch (IllegalAccessException e) {
            failures.add("cannot switch structure generation on: " + e);
        } finally {
            try {
                generateStructures.setBoolean(options, structuresBefore);
            } catch (IllegalAccessException e) {
                failures.add("cannot restore structure generation: " + e);
            }
            appender.detach();
        }
        report(helper, failures);
    }

    /**
     * Structures that once failed the comparison, generated again at their exact start chunk in a fresh level on every
     * run, so a position-dependent failure found by the random search cannot hide behind the next random pick. The
     * GameTest world seed is always 0 ({@code GameTestServer.WORLD_OPTIONS}), so start chunk and builder pin the build.
     *
     * <p>The copy runs under its own key, so {@code OreSpawnWorld.generate} (which switches on the dimension key) adds
     * no small decoration there; {@code LegacyStructurePass} and the populate of the generator run as in the original.
     *
     * @param dimension  {@code minecraft:the_end} or an OreSpawn dimension id
     * @param overlapped whether a second legacy piece overlaps the build (then the case also checks that it still does)
     */
    private record Pinned(String dimension, String setName, int x, int y, int z, String builder, boolean overlapped,
                          String found) {
    }

    private static final List<Pinned> PINNED = List.of(
            // 227 missing and 4 shape: an Ender Reaper Graveyard of the neighbouring start is written over the castle.
            new Pinned("minecraft:the_end", "end_dungeon", -1136972, 59, -696776, OreSpawnWorld.MAKE_ENDER_CASTLE, true,
                    "R24 verification run 2026-09-14 12:23"),
            // 9 missing ladder->air: ladders hanging on air in the log, popped by the neighbour updates of a live replay.
            new Pinned("orespawn:danger", "islands_dungeon", -180155, 7, -136736, OreSpawnWorld.MAKE_INCA_PYRAMID, false,
                    "R24 run 11, 2026-09-14 12:44"));

    private static void generatePinned(GameTestHelper helper, RegistryAccess registries, Pinned pinned, List<String> failures) {
        ServerLevel level = freshCopy(helper, pinned.dimension());
        StructureSet set = registries.registryOrThrow(Registries.STRUCTURE_SET)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, rl(pinned.setName())));
        Structure structure = set.structures().get(0).structure().value();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        BlockPos origin = new BlockPos(pinned.x(), pinned.y(), pinned.z());
        // The start chunk is the placement chunk the dispatcher rolled the origin from: at most a chunk away.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos cp = new ChunkPos((origin.getX() >> 4) + dx, (origin.getZ() >> 4) + dz);
                StructureStart start = structure.generate(registries, generator, generator.getBiomeSource(),
                        level.getChunkSource().randomState(), level.getServer().getStructureManager(),
                        level.getChunkSource().getGeneratorState().getLevelSeed(), cp, 0, level, structure.biomes()::contains);
                if (!start.isValid() || !(start.getPieces().get(0) instanceof LegacyStructurePiece piece)
                        || !piece.builder().equals(pinned.builder()) || !piece.origin().equals(origin)) {
                    continue;
                }
                if (!freshAround(level, piece.getBoundingBox())) {
                    failures.add(pinned + ": pinned region is not fresh");
                    return;
                }
                OreSpawn.LOG.info("W13 pinned {} at chunk {}: {} at {}", pinned.setName(), cp, piece.builder(),
                        origin.toShortString());
                verifyGenerated(level, generator, structure, cp, piece, failures);
                liveReplayCounterProbe(level, generator, piece);
                if (pinned.overlapped() && legacyPiecesInPassOrder(level, piece.getBoundingBox()).size() < 2) {
                    failures.add(pinned + ": no other legacy piece overlaps the pinned build any more");
                }
                return;
            }
        }
        failures.add(pinned + ": no start chunk around the origin yields this build any more");
    }

    /**
     * Counter-probe for the choice to compare against the logs instead of a replay: replays the piece on the live level
     * once more (neighbour updates included, as the comparison did before) and logs how many logged non-connection
     * positions then differ from the log. Logged, not failed: it documents why a live replay is no reference.
     */
    private static void liveReplayCounterProbe(ServerLevel level, ChunkGenerator generator, LegacyStructurePiece piece) {
        StructureRecording recording = piece.recording(level, generator);
        if (recording == null) {
            return;
        }
        BoundingBox box = piece.getBoundingBox();
        recording.replay(level, box);
        int differs = 0;
        Map<String, Integer> kinds = new TreeMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<BlockState> entry : recording.finalStates(box).long2ObjectEntrySet()) {
            BlockState logged = entry.getValue();
            Block block = logged.getBlock();
            if (block instanceof CrossCollisionBlock || block instanceof WallBlock || block instanceof StairBlock
                    || block instanceof RedStoneWireBlock) {
                continue;
            }
            BlockState live = level.getBlockState(pos.set(entry.getLongKey()));
            if (live != logged) {
                differs++;
                kinds.merge(block.builtInRegistryHolder().key().location().getPath() + "->"
                        + live.getBlock().builtInRegistryHolder().key().location().getPath(), 1, Integer::sum);
            }
        }
        discardEntities(level, box);
        OreSpawn.LOG.info("W13 pinned counter-probe {}: a live replay leaves {} logged positions different from the log {}",
                piece.builder(), differs, kinds);
    }

    /**
     * A fresh copy of a dimension (same dimension type, a newly decoded or built generator) under the key
     * {@code orespawn:w13_pinned_<path>}: what an earlier run saved under that key is deleted before it is created, so a
     * pinned position is generated from scratch every time. Never {@code Level.END}, so no dragon fight is attached.
     */
    private static ServerLevel freshCopy(GameTestHelper helper, String dimension) {
        MinecraftServer server = helper.getLevel().getServer();
        ResourceLocation source = ResourceLocation.parse(dimension);
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, rl("w13_pinned_" + source.getPath()));
        ServerLevel existing = server.getLevel(key);
        if (existing != null) {
            return existing;
        }
        RegistryAccess registries = server.registryAccess();
        LevelStem stem;
        if (source.getNamespace().equals(OreSpawn.MOD_ID)) {
            try {
                stem = W05GameTests.decodeStem(server, source.getPath());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else if (source.getPath().equals("the_end")) {
            stem = new LevelStem(
                    registries.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.END),
                    new NoiseBasedChunkGenerator(net.minecraft.world.level.biome.TheEndBiomeSource.create(
                            registries.lookupOrThrow(Registries.BIOME)),
                            registries.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(NoiseGeneratorSettings.END)));
        } else {
            throw new IllegalArgumentException("no fresh copy for " + dimension);
        }
        return createLevel(server, key, stem, true);
    }

    private static final ResourceKey<Level> NOISE_OVERWORLD = ResourceKey.create(Registries.DIMENSION, rl("w13_noise_overworld"));

    /**
     * A vanilla noise overworld (overworld dimension type, multi-noise overworld preset, overworld noise settings) as an
     * extra level, created the way W05GameTests.dimensionLevel creates the OreSpawn levels. The structure dispatchers do
     * not look at the dimension key, only at biomes, so {@code orespawn:overworld_dungeon} places there as in the
     * overworld of a normal world.
     */
    @SuppressWarnings("deprecation")
    private static ServerLevel noiseOverworld(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerLevel existing = server.getLevel(NOISE_OVERWORLD);
        if (existing != null) {
            return existing;
        }
        RegistryAccess registries = server.registryAccess();
        LevelStem stem = new LevelStem(
                registries.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(
                        registries.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                                .getHolderOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD)),
                        registries.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD)));
        return createLevel(server, NOISE_OVERWORLD, stem, false);
    }

    /** Creates and registers an extra level; {@code deleteSaved} first removes what an earlier run saved under the key. */
    @SuppressWarnings("deprecation")
    private static ServerLevel createLevel(MinecraftServer server, ResourceKey<Level> key, LevelStem stem, boolean deleteSaved) {
        try {
            LevelStorageSource.LevelStorageAccess storage = null;
            for (Field f : MinecraftServer.class.getDeclaredFields()) {
                if (f.getType() == LevelStorageSource.LevelStorageAccess.class) {
                    f.setAccessible(true);
                    storage = (LevelStorageSource.LevelStorageAccess) f.get(server);
                }
            }
            if (deleteSaved && storage != null) {
                Path dir = storage.getDimensionPath(key);
                if (Files.exists(dir)) {
                    try (Stream<Path> walk = Files.walk(dir)) {
                        for (Path file : walk.sorted(java.util.Comparator.reverseOrder()).toList()) {
                            Files.delete(file);
                        }
                    }
                }
            }
            WorldData data = server.getWorldData();
            ServerLevel overworld = server.overworld();
            ServerLevel level = new ServerLevel(server, Util.backgroundExecutor(), storage,
                    new DerivedLevelData(data, data.overworldData()), key, stem, NO_PROGRESS, data.isDebugWorld(),
                    BiomeManager.obfuscateSeed(data.worldGenOptions().seed()), ImmutableList.of(), false,
                    overworld.getRandomSequences());
            overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(level.getWorldBorder()));
            server.forgeGetWorldMap().put(key, level);
            server.markWorldsDirty();
            return level;
        } catch (ReflectiveOperationException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final ChunkProgressListener NO_PROGRESS = new ChunkProgressListener() {
        @Override
        public void updateSpawnPos(ChunkPos center) {
        }

        @Override
        public void onStatusChange(ChunkPos chunkPos, @Nullable ChunkStatus chunkStatus) {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    };

    private static Field declaredField(Class<?> owner, String name) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(owner.getName() + " has no field " + name, e);
        }
    }

    private static void generateOneStructure(ServerLevel level, RegistryAccess registries, String setName,
                                             Set<String> dungeonIds, Random pick, List<String> failures) {
        String dim = level.dimension().location().toString();
        StructureSet set = registries.registryOrThrow(Registries.STRUCTURE_SET)
                .getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, rl(setName)));
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        boolean possible = state.possibleStructureSets().stream().anyMatch(h -> h.value() == set);
        if (!possible) {
            failures.add(setName + ": not a possible structure set of " + dim);
            return;
        }
        if (!(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
            failures.add(setName + ": placement is " + set.placement().getClass().getSimpleName());
            return;
        }
        Structure structure = set.structures().get(0).structure().value();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        long seed = state.getLevelSeed();
        int spacing = placement.spacing();
        int baseX = (40_000 + pick.nextInt(60_000)) / spacing * (pick.nextBoolean() ? 1 : -1);
        int baseZ = (40_000 + pick.nextInt(60_000)) / spacing * (pick.nextBoolean() ? 1 : -1);
        long deadline = System.nanoTime() + 60_000_000_000L;
        int candidates = 0;
        int starts = 0;
        // up to three different builds per set (one for the King altar, whose build alone takes seconds)
        int want = setName.equals("utopia_king_altar") ? 1 : 3;
        Set<String> generated = new HashSet<>();
        Map<String, Integer> otherBuilders = new TreeMap<>();
        for (int n = 0; n < 20_000 && System.nanoTime() < deadline; n++) {
            // every 50 candidates a new far region, so one void or ocean stretch cannot starve the search
            if (n % 50 == 0) {
                baseX = (40_000 + pick.nextInt(60_000)) / spacing * (pick.nextBoolean() ? 1 : -1);
                baseZ = (40_000 + pick.nextInt(60_000)) / spacing * (pick.nextBoolean() ? 1 : -1);
            }
            ChunkPos cp = placement.getPotentialStructureChunk(seed, baseX + n % 10, baseZ + (n / 10) % 5);
            candidates++;
            StructureStart start = structure.generate(registries, generator, generator.getBiomeSource(), randomState,
                    level.getServer().getStructureManager(), seed, cp, 0, level, structure.biomes()::contains);
            if (!start.isValid()) {
                continue;
            }
            starts++;
            if (!(start.getPieces().get(0) instanceof LegacyStructurePiece piece) || !dungeonIds.contains(piece.builder())) {
                String other = start.getPieces().get(0) instanceof LegacyStructurePiece lp ? lp.builder()
                        : start.getPieces().get(0).getClass().getSimpleName();
                otherBuilders.merge(other, 1, Integer::sum);
                continue;
            }
            BoundingBox box = piece.getBoundingBox();
            if (generated.contains(piece.builder()) || !freshAround(level, box)) {
                continue;
            }
            verifyGenerated(level, generator, structure, cp, piece, failures);
            generated.add(piece.builder());
            OreSpawn.LOG.info("W13 structure set {} in {}: {} after {} candidates ({} starts, other builders {})", setName,
                    dim, piece.builder(), candidates, starts, otherBuilders);
            if (generated.size() >= want) {
                return;
            }
        }
        if (!generated.isEmpty()) {
            return;
        }
        failures.add(setName + " in " + dim + ": no GenericDungeon start in " + candidates + " candidates (" + starts
                + " starts, other builders " + otherBuilders + ")");
    }

    private static boolean freshAround(ServerLevel level, BoundingBox box) {
        ServerChunkCache source = level.getChunkSource();
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                if (source.getChunkNow(cx, cz) != null || source.chunkMap.read(new ChunkPos(cx, cz)).join().isPresent()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void verifyGenerated(ServerLevel level, ChunkGenerator generator, Structure structure, ChunkPos startChunk,
                                        LegacyStructurePiece expected, List<String> failures) {
        String name = expected.builder() + " v" + expected.variant() + " in " + level.dimension().location();
        BoundingBox box = expected.getBoundingBox();
        long started = System.nanoTime();
        // one ring more than the box, so no neighbour's feature step can still write into the box later
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                if (chunk.getPersistedStatus() != ChunkStatus.FULL) {
                    failures.add(name + ": chunk " + chunk.getPos() + " is " + chunk.getPersistedStatus());
                }
            }
        }
        long generatedMs = (System.nanoTime() - started) / 1_000_000;
        // What ChunkMap.prepareTickingChunk does once a player is near: the post-processing of the marked positions
        // (connection shapes). The test chunks never tick, so it is run here.
        int marked = 0;
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                LevelChunk chunk = level.getChunk(cx, cz);
                for (it.unimi.dsi.fastutil.shorts.ShortList list : chunk.getPostProcessing()) {
                    marked += list == null ? 0 : list.size();
                }
                chunk.postProcessGeneration();
            }
        }
        StructureStart stored = level.getChunk(startChunk.x, startChunk.z).getStartForStructure(structure);
        if (stored == null || !stored.isValid() || !(stored.getPieces().get(0) instanceof LegacyStructurePiece piece)) {
            LevelChunk sc = level.getChunk(startChunk.x, startChunk.z);
            List<String> keys = new ArrayList<>();
            sc.getAllStarts().forEach((k, v) -> keys.add(level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                    .getKey(k) + (v.isValid() ? "" : "(invalid)")));
            ChunkGeneratorStructureState gs = level.getChunkSource().getGeneratorState();
            boolean placementChunk = gs.possibleStructureSets().stream()
                    .filter(h -> h.value().structures().get(0).structure().value() == structure)
                    .anyMatch(h -> h.value().placement().isStructureChunk(gs, startChunk.x, startChunk.z));
            failures.add(name + ": no stored start in chunk " + startChunk + " (status " + sc.getPersistedStatus()
                    + ", starts " + keys + ", references " + sc.getAllReferences().size() + ", isStructureChunk "
                    + placementChunk + ", box " + box + ")");
            return;
        }
        if (!piece.builder().equals(expected.builder()) || !piece.origin().equals(expected.origin())
                || piece.variant() != expected.variant() || !piece.getBoundingBox().equals(box)) {
            failures.add(name + ": stored start differs: " + piece.builder() + " " + piece.origin() + " " + piece.getBoundingBox());
            return;
        }
        long pieceSeed = piece.createTag(StructurePieceSerializationContext.fromLevel(level)).getLong("Seed");
        LegacyStructureRegistry.Builder builder = LegacyStructureRegistry.builder(piece.builder()).orElseThrow();
        StructureRecording recording = StructureRecording.record(level, generator, box, writer ->
                builder.build(writer, new Random(pieceSeed), piece.origin().getX(), piece.origin().getY(),
                        piece.origin().getZ(), piece.variant()));
        BoundingBox clipped = new BoundingBox(box.minX(), Math.max(box.minY(), level.getMinBuildHeight()), box.minZ(),
                box.maxX(), Math.min(box.maxY(), level.getMaxBuildHeight() - 1), box.maxZ());
        // DECISIONS R24: the generated world must hold, at every position a legacy piece over the box writes, the state
        // the logs leave behind. More than one piece can cover the box (End starts lie one chunk apart): then the logs are
        // applied in the order of LegacyStructurePass, and a piece later in that order rightly wins where two overlap.
        // The expected states come straight from the logs, not from a replay on the live level: a replay adds neighbour
        // updates the generation never runs (a ladder the log leaves hanging on air pops off, a flower next to a
        // rewritten block breaks), and that is not what the world generated.
        List<LegacyStructurePiece> overPieces = legacyPiecesInPassOrder(level, clipped);
        it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<BlockState> wanted = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>();
        List<String> overlapping = new ArrayList<>();
        boolean expectedListed = false;
        for (LegacyStructurePiece over : overPieces) {
            overlapping.add(over.builder() + " v" + over.variant() + "@" + over.origin().toShortString());
            boolean isExpected = over.builder().equals(piece.builder()) && over.origin().equals(piece.origin())
                    && over.variant() == piece.variant() && over.getBoundingBox().equals(box);
            if (isExpected && !expectedListed) {
                expectedListed = true;
                wanted.putAll(recording.finalStates(clipped));
                continue;
            }
            long overSeed = over.createTag(StructurePieceSerializationContext.fromLevel(level)).getLong("Seed");
            LegacyStructureRegistry.Builder overBuilder = LegacyStructureRegistry.builder(over.builder()).orElseThrow();
            // cached: generation built the same key moments ago; the piece under test is rebuilt uncached above
            StructureRecording overRecording = StructureRecording.obtain(level, generator,
                    StructureRecording.Key.of(level, over.builder(), over.origin().getX(), over.origin().getY(),
                            over.origin().getZ(), over.variant(), overSeed, over.getBoundingBox()),
                    writer -> overBuilder.build(writer, new Random(overSeed), over.origin().getX(), over.origin().getY(),
                            over.origin().getZ(), over.variant()));
            wanted.putAll(overRecording.finalStates(clipped));
        }
        if (!expectedListed) {
            failures.add(name + ": the piece is not among the legacy pieces over its own box " + overlapping);
            return;
        }
        int missing = 0;
        int shape = 0;
        List<String> samples = new ArrayList<>();
        Map<String, Integer> kinds = new TreeMap<>();
        Map<String, Integer> unsupported = new TreeMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<BlockState> entry : wanted.long2ObjectEntrySet()) {
            pos.set(entry.getLongKey());
            BlockState generated = level.getBlockState(pos);
            BlockState logged = entry.getValue();
            Block block = logged.getBlock();
            boolean connection = block instanceof CrossCollisionBlock || block instanceof WallBlock
                    || block instanceof StairBlock || block instanceof RedStoneWireBlock;
            // The generated chunks ran LevelChunk.postProcessGeneration on the connection blocks: the logged state is the
            // unconnected default, the world holds it connected to its neighbours.
            BlockState want = connection ? Block.updateFromNeighbourShapes(logged, level, pos) : logged;
            if (!want.isAir() && !want.canSurvive(level, pos)) {
                // kept by generation (no neighbour updates), would pop off on a live replay: logged for the record
                unsupported.merge(block.builtInRegistryHolder().key().location().getPath(), 1, Integer::sum);
            }
            if (generated == want) {
                continue;
            }
            String kind;
            if (generated.getBlock() == block && connection) {
                shape++;
                kind = "shape ";
            } else {
                missing++;
                kind = "missing ";
            }
            if (samples.size() < 4) {
                samples.add(pos.toShortString() + ": " + generated + " vs " + want);
            }
            kinds.merge(kind + generated.getBlock().builtInRegistryHolder().key().location().getPath()
                    + (generated.getBlock() == block ? "(state)" : "->" + block.builtInRegistryHolder().key().location().getPath()),
                    1, Integer::sum);
        }
        String changedKinds = kinds + " " + samples;
        discardEntities(level, box);
        int inside = recording.writes() - recording.writesOutside(box);
        if (inside == 0) {
            failures.add(name + ": recording writes nothing inside " + box);
        }
        if (missing + shape != 0) {
            failures.add(name + ": generated world lacks " + missing + " recorded blocks and has " + shape
                    + " unconnected connection blocks (of " + inside + " writes): " + changedKinds);
        }
        OreSpawn.LOG.info("W13 legacy pieces over the box of {} in pass order: {}", name, overlapping);
        OreSpawn.LOG.info("W13 generated {} at {}: box {}, chunks FULL in {} ms, {} writes inside, {} positions post-processed, {} logged positions compared: {} missing, {} shape, logged blocks that cannot survive {} {}",
                name, piece.origin().toShortString(), box, generatedMs, inside, marked, wanted.size(), missing, shape,
                unsupported, changedKinds);
    }

    /**
     * Every {@code orespawn:legacy} piece whose box intersects {@code window}, in the order {@code LegacyStructurePass}
     * writes them: structures in its order, starts by start chunk, pieces in start order.
     */
    private static List<LegacyStructurePiece> legacyPiecesInPassOrder(ServerLevel level, BoundingBox window) {
        List<LegacyStructurePiece> result = new ArrayList<>();
        for (Structure structure : LegacyStructurePass.ordered(level.registryAccess().registryOrThrow(Registries.STRUCTURE))) {
            it.unimi.dsi.fastutil.longs.LongOpenHashSet seen = new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
            it.unimi.dsi.fastutil.longs.LongArrayList references = new it.unimi.dsi.fastutil.longs.LongArrayList();
            for (int cx = window.minX() >> 4; cx <= window.maxX() >> 4; cx++) {
                for (int cz = window.minZ() >> 4; cz <= window.maxZ() >> 4; cz++) {
                    for (long reference : level.getChunk(cx, cz, ChunkStatus.STRUCTURE_REFERENCES, true)
                            .getReferencesForStructure(structure)) {
                        if (seen.add(reference)) {
                            references.add(reference);
                        }
                    }
                }
            }
            LegacyStructurePass.sortByStartChunk(references);
            for (long reference : references) {
                StructureStart start = level.getChunk(ChunkPos.getX(reference), ChunkPos.getZ(reference),
                        ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(structure);
                if (start == null || !start.isValid()) {
                    continue;
                }
                for (var piece : start.getPieces()) {
                    if (piece instanceof LegacyStructurePiece legacy && piece.getBoundingBox().intersects(window)) {
                        result.add(legacy);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Collects every {@code Detected setBlock in a far chunk} logged while attached (as R21GameTests): attached to the
     * logger config of {@code net.minecraft.Util} in every log4j context, removed again in {@link #detach}.
     */
    private static final class FarChunkAppender extends AbstractAppender {
        private final Queue<String> messages = new ConcurrentLinkedQueue<>();
        private final List<LoggerConfig> configs = new ArrayList<>();
        private final List<LoggerContext> contexts = new ArrayList<>();

        private FarChunkAppender() {
            super("orespawn-w13-far-chunk", null, null, true, new Property[0]);
        }

        @Override
        public void append(LogEvent event) {
            String text = event.getMessage() == null ? null : event.getMessage().getFormattedMessage();
            if (text != null && text.contains(FAR_CHUNK)) {
                this.messages.add(text);
            }
        }

        static FarChunkAppender attach() {
            FarChunkAppender appender = new FarChunkAppender();
            appender.start();
            Set<LoggerContext> contexts = Collections.newSetFromMap(new IdentityHashMap<>());
            if (LogManager.getFactory() instanceof Log4jContextFactory factory) {
                contexts.addAll(factory.getSelector().getLoggerContexts());
            }
            contexts.add(LoggerContext.getContext(false));
            for (LoggerContext context : contexts) {
                LoggerConfig config = context.getConfiguration().getLoggerConfig(Util.class.getName());
                config.addAppender(appender, null, null);
                appender.configs.add(config);
                appender.contexts.add(context);
                context.updateLoggers();
            }
            return appender;
        }

        List<String> messages() {
            return new ArrayList<>(this.messages);
        }

        void detach() {
            for (LoggerConfig config : this.configs) {
                config.removeAppender(this.getName());
            }
            for (LoggerContext context : this.contexts) {
                context.updateLoggers();
            }
            this.stop();
        }
    }

    // =====================================================================================================
    // 5. no back-edge left

    /**
     * Catalogue 4.13 "alle Rückkanten geschlossen": no source or resource file of the mod still carries the W13 TODO
     * marker. The GameTest server runs in {@code run/gametest}; the project is two levels up.
     */
    @GameTest(template = ARENA)
    public static void noW13TodoRemainsInTheSources(GameTestHelper helper) {
        String marker = "TODO " + "W13";
        Path project = Paths.get("").toAbsolutePath().getParent().getParent();
        List<String> failures = new ArrayList<>();
        int files = 0;
        for (String root : List.of("src/main/java", "src/main/resources")) {
            Path dir = project.resolve(root);
            if (!Files.isDirectory(dir)) {
                failures.add("precondition: " + dir + " not found");
                continue;
            }
            try (Stream<Path> walk = Files.walk(dir)) {
                for (Path file : walk.filter(Files::isRegularFile).filter(f -> {
                    String n = f.getFileName().toString();
                    return n.endsWith(".java") || n.endsWith(".json") || n.endsWith(".md") || n.endsWith(".toml");
                }).toList()) {
                    files++;
                    List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).contains(marker)) {
                            failures.add(project.relativize(file) + ":" + (i + 1));
                        }
                    }
                }
            } catch (IOException e) {
                failures.add(root + ": " + e);
            }
        }
        if (files < 500) {
            failures.add("precondition: only " + files + " files scanned");
        }
        OreSpawn.LOG.info("W13 TODO scan: {} files, {} markers", files, failures.size());
        report(helper, failures);
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
    }

    private static void report(GameTestHelper helper, List<String> failures) {
        if (failures.size() > 12) {
            int more = failures.size() - 12;
            List<String> head = new ArrayList<>(failures.subList(0, 12));
            head.add(more + " more");
            failures = head;
        }
        String message = String.join("; ", failures);
        if (!failures.isEmpty()) {
            OreSpawn.LOG.warn("W13 failure detail: {}", message);
        }
        helper.assertTrue(failures.isEmpty(), message.length() > 700 ? message.substring(0, 700) + " ... (full text in the log)" : message);
        helper.succeed();
    }
}
