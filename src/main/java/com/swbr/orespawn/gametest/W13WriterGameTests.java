package com.swbr.orespawn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.world.feature.LegacyCountPlacement;
import com.swbr.orespawn.world.feature.LegacyOreStat;
import com.swbr.orespawn.world.spawn.ConfigSpawnsBiomeModifier;
import com.swbr.orespawn.world.spawn.SpawnTable;
import com.swbr.orespawn.world.structure.LegacyStructurePiece;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry.Placement;
import com.swbr.orespawn.world.structure.StructureRecording;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.WeightedRandomChestContent;
import com.swbr.orespawn.world.tree.TreeBuilders;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Wave 13, the shared writer (DECISIONS R23) and the catalogue 4.13 GameTests W12 left open.
 *
 * <ul>
 * <li>A W12 structure (Basilisk maze: blocks 33 down and 64 along, its basilisks) and a tree (Fairy tree: chest and
 * spawner) as {@link LegacyStructurePiece}s over a field of at least 3x3 chunks: chunk by chunk in shuffled order with
 * the recording cache cleared before every chunk (so every chunk builds again), against one replay of the whole box and
 * against the registered builder run once unclipped on the live level. Blocks, chest contents, spawner data and
 * entities must agree, and nothing may land outside the piece box.</li>
 * <li>A synthetic builder through the {@link StructureWriter} calls GenericDungeon uses, whose placement and random
 * draws depend on reads across chunk borders and on chest rolls: seamless through {@link StructureRecording}, built once
 * for all its chunks, blind to a foreign block in the live world - and, as a check that the comparison can see a seam
 * at all, visibly seamed through the old per-chunk writer.</li>
 * <li>Ore veins with {@code LessOre} 0 and 1, and the {@code *Enable} switches of {@code orespawn:config_spawns} for
 * every entry of the spawn table.</li>
 * </ul>
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W13WriterGameTests {

    private static final String ARENA = "arena";

    private W13WriterGameTests() {}

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
    }

    // =====================================================================================================
    // 1. a W12 structure and a tree: no seam across the chunk field

    /**
     * R23 with two registered W12 builders. U: the builder once through {@link StructureWriter#unclipped} on the live
     * level (the original). S: the piece replayed once with a clip holding its whole box. C: the piece post-processed
     * for every chunk of its box, shuffled, cache cleared before each chunk. C must equal S and U block by block, chest
     * by chest, spawner by spawner and in entities; C must not write outside the piece box; the recording itself logs
     * no write outside the box.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400)
    public static void structureAndTreeReplayWithoutSeamsAcrossTheChunkField(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        Random pick = new Random(System.nanoTime());
        long seed = pick.nextLong();
        ChunkGenerator generator = level.getChunkSource().getGenerator();

        record Probe(String name, java.util.function.BiFunction<Integer, Integer, Placement> at) {}
        // Y 230: above overworld terrain, so the live world and the base terrain the recording reads agree (air).
        List<Probe> probes = List.of(
                new Probe("basilisk_maze", (x, z) -> TreeBuilders.basiliskMaze(x, 230, z)),
                new Probe("fairy_tree", (x, z) -> TreeBuilders.fairyTree(x, 230, z)));

        for (Probe probe : probes) {
            Placement rel = probe.at().apply(0, 0);
            int relMinX = rel.box().minX();
            int relMinZ = rel.box().minZ();
            int spanX = rel.box().maxX() - relMinX + 1;
            int spanZ = rel.box().maxZ() - relMinZ + 1;
            ChunkPos[] areas = airAreas(level, pick, 3, spanX + 48, spanZ + 48, relMinX, relMinZ, probe.at(), failures,
                    probe.name());
            if (areas == null) {
                continue;
            }
            Placement pu = placementIn(areas[0], relMinX, relMinZ, probe.at());
            Placement ps = placementIn(areas[1], relMinX, relMinZ, probe.at());
            Placement pc = placementIn(areas[2], relMinX, relMinZ, probe.at());

            int chunksX = (pc.box().maxX() >> 4) - (pc.box().minX() >> 4) + 1;
            int chunksZ = (pc.box().maxZ() >> 4) - (pc.box().minZ() >> 4) + 1;
            if (chunksX < 3 || chunksZ < 3) {
                failures.add(probe.name() + ": box spans only " + chunksX + "x" + chunksZ + " chunks, test needs 3x3");
            }

            LegacyStructureRegistry.Builder builder = LegacyStructureRegistry.builder(pu.builder()).orElse(null);
            if (builder == null) {
                failures.add(probe.name() + ": builder " + pu.builder() + " not registered");
                continue;
            }
            // U
            builder.build(StructureWriter.unclipped(level), new Random(seed), pu.x(), pu.y(), pu.z(), pu.variant());
            // S
            StructureRecording.clearCache();
            new LegacyStructurePiece(ps, seed).postProcess(level, level.structureManager(), generator,
                    RandomSource.create(1L), ps.box(), new ChunkPos(ps.x() >> 4, ps.z() >> 4), new BlockPos(ps.x(), ps.y(), ps.z()));
            // C
            List<ChunkPos> chunks = new ArrayList<>();
            for (int cx = pc.box().minX() >> 4; cx <= pc.box().maxX() >> 4; cx++) {
                for (int cz = pc.box().minZ() >> 4; cz <= pc.box().maxZ() >> 4; cz++) {
                    chunks.add(new ChunkPos(cx, cz));
                    level.getChunk(cx, cz);
                }
            }
            Collections.shuffle(chunks, pick);
            for (ChunkPos chunk : chunks) {
                StructureRecording.clearCache();
                BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                        chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
                new LegacyStructurePiece(pc, seed).postProcess(level, level.structureManager(), generator,
                        RandomSource.create(chunk.toLong()), chunkBox, chunk, chunk.getMiddleBlockPosition(pc.y()));
            }

            BoundingBox window = inflate(pc.box(), 2);
            int dSX = ps.x() - pc.x();
            int dSZ = ps.z() - pc.z();
            int dUX = pu.x() - pc.x();
            int dUZ = pu.z() - pc.z();
            Compare cs = compare(level, window, dSX, dSZ, null);
            Compare cu = compare(level, window, dUX, dUZ, null);
            if (cs.nonAir == 0) {
                failures.add(probe.name() + ": chunk runs placed nothing");
            }
            if (cs.diffs() != 0) {
                failures.add(probe.name() + ": chunk runs vs one replay: " + cs);
            }
            if (cu.diffs() != 0) {
                failures.add(probe.name() + ": chunk runs vs unclipped builder: " + cu);
            }
            int outside = nonAirOutside(level, window, pc.box());
            if (outside != 0) {
                failures.add(probe.name() + ": " + outside + " non-air blocks around the piece box after the chunk runs");
            }
            StructureRecording recording = StructureRecording.record(level, generator, pc.box(), writer ->
                    builder.build(writer, new Random(seed), pc.x(), pc.y(), pc.z(), pc.variant()));
            if (recording.writesOutside(pc.box()) != 0) {
                failures.add(probe.name() + ": recording logs " + recording.writesOutside(pc.box()) + " writes outside "
                        + pc.box());
            }
            String entC = entitySummary(level, pc.box());
            String entS = entitySummary(level, ps.box());
            String entU = entitySummary(level, pu.box());
            if (!entC.equals(entS) || !entC.equals(entU)) {
                failures.add(probe.name() + ": entities differ, chunk runs " + entC + ", replay " + entS + ", unclipped " + entU);
            }
            if (recording.entities() != entityCount(level, pc.box())) {
                failures.add(probe.name() + ": recording holds " + recording.entities() + " entities, chunk runs placed "
                        + entityCount(level, pc.box()));
            }
            OreSpawn.LOG.info("W13 writer test {}: {} writes, {} block entities, {} entities; {} chunks; {} non-air; entities {}",
                    probe.name(), recording.writes(), recording.blockEntities(), recording.entities(), chunks.size(),
                    cs.nonAir, entC);
            discardEntities(level, pu.box());
            discardEntities(level, ps.box());
            discardEntities(level, pc.box());
        }
        StructureRecording.clearCache();
        report(helper, failures);
    }

    private static Placement placementIn(ChunkPos area, int relMinX, int relMinZ,
                                         java.util.function.BiFunction<Integer, Integer, Placement> at) {
        // Box minimum at chunk-local 15 of the area's second chunk: the box crosses as many chunk borders as it can.
        int x = area.getMinBlockX() + 16 + 15 - relMinX;
        int z = area.getMinBlockZ() + 16 + 15 - relMinZ;
        return at.apply(x, z);
    }

    /** Fresh areas whose piece window (box plus 2) is air in the live world. */
    private static ChunkPos[] airAreas(ServerLevel level, Random pick, int count, int spanX, int spanZ, int relMinX,
                                       int relMinZ, java.util.function.BiFunction<Integer, Integer, Placement> at,
                                       List<String> failures, String name) {
        ChunkPos[] areas = new ChunkPos[count];
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; ; attempt++) {
                if (attempt >= 8) {
                    failures.add("precondition " + name + ": no air window found");
                    return null;
                }
                ChunkPos area = freshAreas(level, pick, 1, spanX, spanZ)[0];
                BoundingBox window = inflate(placementIn(area, relMinX, relMinZ, at).box(), 2);
                if (nonAirOutside(level, window, null) == 0) {
                    areas[i] = area;
                    break;
                }
            }
        }
        return areas;
    }

    // =====================================================================================================
    // 2. reads and chest rolls through the StructureWriter calls GenericDungeon uses

    private static final WeightedRandomChestContent[] PROBE_CHEST = {
            WeightedRandomChestContent.of(Items.DIAMOND, 1, 3, 5),
            WeightedRandomChestContent.of(Items.IRON_INGOT, 1, 8, 10),
            WeightedRandomChestContent.of(Items.STICK, 1, 64, 20)};

    /**
     * A 48x4x48 build at a 3x3 chunk field whose every step depends on what it read: row by row, a column continues a
     * glass run only if the block to its -x side (the neighbouring chunk at x 16 and 32) is glass, draws for it, and
     * rolls a chest or a spawner at intervals. One probe read at a position the build never writes adds a draw when it
     * is not air.
     */
    private static void probeBuild(StructureWriter world, long seed, int ox, int oy, int oz) {
        Random r = new Random(seed);
        if (!world.isAirBlock(ox + 20, oy + 3, oz + 20)) {
            r.nextInt(3);
        }
        for (int k = 0; k < 48; k++) {
            for (int i = 0; i < 48; i++) {
                BlockState west = world.getBlock(ox + i - 1, oy, oz + k);
                if (i != 0 && !west.is(Blocks.GLASS)) {
                    continue;
                }
                world.setBlock(ox + i, oy, oz + k, r.nextInt(9) != 0 ? Blocks.GLASS.defaultBlockState()
                        : Blocks.STONE.defaultBlockState());
                if ((i + 3 * k) % 7 == 0) {
                    world.setChest(ox + i, oy + 1, oz + k, 3, PROBE_CHEST, 4, r);
                } else if ((i + k) % 13 == 0) {
                    world.setSpawner(ox + i, oy + 1, oz + k, "minecraft:zombie");
                }
            }
        }
    }

    /**
     * R23 on a builder that reads across chunk borders and rolls chests. S: one replay of the whole box. C: every chunk,
     * reverse order, with a foreign glass block placed in the live world first at the probe position. D: the same build
     * the W12 way, directly through {@link StructureWriter#forPiece} once per chunk in reverse order. C must equal S
     * (the foreign block aside) and the build must run once for all nine chunks; D must differ from S, else the
     * comparison would not see a seam.
     */
    @GameTest(template = ARENA, timeoutTicks = 1200)
    public static void readsAndChestRollsDoNotDependOnTheChunkBeingGenerated(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        Random pick = new Random(System.nanoTime());
        long seed = pick.nextLong();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        final int oy = 300;

        ChunkPos[] areas = freshAreas(level, pick, 3, 48, 48);
        int[] runs = new int[1];
        BoundingBox[] boxes = new BoundingBox[3];
        StructureRecording.Key[] keys = new StructureRecording.Key[3];
        for (int a = 0; a < 3; a++) {
            int ox = areas[a].getMinBlockX();
            int oz = areas[a].getMinBlockZ();
            boxes[a] = new BoundingBox(ox, oy, oz, ox + 47, oy + 3, oz + 47);
            keys[a] = StructureRecording.Key.of(level, "orespawn:w13_writer_probe", ox, oy, oz, 0, seed, boxes[a]);
            for (int cx = 0; cx < 3; cx++) {
                for (int cz = 0; cz < 3; cz++) {
                    level.getChunk(areas[a].x + cx, areas[a].z + cz);
                }
            }
        }
        StructureRecording.clearCache();

        // S: one replay with the whole box.
        final int sx = areas[0].getMinBlockX();
        final int sz = areas[0].getMinBlockZ();
        StructureRecording.postProcess(level, generator, boxes[0], keys[0], w -> {
            runs[0]++;
            probeBuild(w, seed, sx, oy, sz);
        });

        // C: foreign block first, then nine chunks in reverse order from one cached recording.
        final int cxo = areas[1].getMinBlockX();
        final int czo = areas[1].getMinBlockZ();
        BlockPos foreign = new BlockPos(cxo + 20, oy + 3, czo + 20);
        level.setBlock(foreign, Blocks.GLASS.defaultBlockState(), 2);
        int before = runs[0];
        for (int cx = 2; cx >= 0; cx--) {
            for (int cz = 2; cz >= 0; cz--) {
                ChunkPos chunk = new ChunkPos(areas[1].x + cx, areas[1].z + cz);
                StructureRecording.postProcess(level, generator, chunkBox(level, chunk), keys[1], w -> {
                    runs[0]++;
                    probeBuild(w, seed, cxo, oy, czo);
                });
            }
        }
        if (runs[0] - before != 1) {
            failures.add("the probe build ran " + (runs[0] - before) + " times for nine chunks of one structure, expected once");
        }

        // D: the W12 way.
        final int dxo = areas[2].getMinBlockX();
        final int dzo = areas[2].getMinBlockZ();
        for (int cx = 2; cx >= 0; cx--) {
            for (int cz = 2; cz >= 0; cz--) {
                ChunkPos chunk = new ChunkPos(areas[2].x + cx, areas[2].z + cz);
                probeBuild(StructureWriter.forPiece(level, chunkBox(level, chunk)).narrowed(boxes[2]), seed, dxo, oy, dzo);
            }
        }

        BoundingBox window = inflate(boxes[1], 1);
        Compare cs = compare(level, window, sx - cxo, sz - czo, foreign);
        Compare ds = compare(level, inflate(boxes[2], 1), sx - dxo, sz - dzo, null);
        if (cs.nonAir < 48) {
            failures.add("probe placed only " + cs.nonAir + " blocks");
        }
        if (cs.chests == 0 || cs.spawners == 0) {
            failures.add("probe placed " + cs.chests + " chests and " + cs.spawners + " spawners, needs both");
        }
        if (cs.diffs() != 0) {
            failures.add("recorded chunk runs vs one replay: " + cs);
        }
        if (ds.diffs() == 0) {
            failures.add("the per-chunk W12 writer produced no seam either: the comparison cannot see one");
        }
        if (!level.getBlockState(foreign).is(Blocks.GLASS)) {
            failures.add("foreign block at the probe position was overwritten");
        }
        OreSpawn.LOG.info("W13 writer probe: {} non-air, {} chests, {} spawners; recorded C vs S {}; per-chunk D vs S {}",
                cs.nonAir, cs.chests, cs.spawners, cs.diffs(), ds.diffs());
        StructureRecording.clearCache();
        report(helper, failures);
    }

    private static BoundingBox chunkBox(ServerLevel level, ChunkPos chunk) {
        return new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
    }

    // =====================================================================================================
    // 3. ore veins with LessOre

    /**
     * {@code orespawn:legacy_count} of every OreSpawn placed feature, over 200 seeds with {@code LessOre} 0 and 1: the
     * divisor shapes give {@code c0 / d}, {@code requires_no_less_ore} gives 0 (and the stat's rate at 0), the rest is
     * unchanged; the attempt positions with LessOre 1 are a prefix of those with LessOre 0. Then Titanium veins placed
     * into the same stone cuboid in two areas: every ore of the LessOre-1 run is an ore of the LessOre-0 run.
     */
    @GameTest(template = ARENA, timeoutTicks = 1200)
    public static void oreVeinsFollowLessOre(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        Registry<PlacedFeature> placedFeatures = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        ModConfigSpec.IntValue lessOre = OreSpawnConfig.TWEAKS.LessOre;
        ModConfigSpec.IntValue lessLag = OreSpawnConfig.TWEAKS.LessLag;
        int savedLessOre = lessOre.get();
        int savedLessLag = lessLag.get();
        int checkedFeatures = 0;
        try {
            lessLag.set(0);
            Map<ResourceLocation, Resource> files = level.getServer().getResourceManager().listResources(
                    "worldgen/placed_feature", id -> id.getNamespace().equals(OreSpawn.MOD_ID) && id.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> file : new TreeMap<>(files).entrySet()) {
                JsonObject json;
                try (Reader reader = file.getValue().openAsReader()) {
                    json = JsonParser.parseReader(reader).getAsJsonObject();
                } catch (IOException e) {
                    failures.add(file.getKey() + ": unreadable");
                    continue;
                }
                if (!json.has("placement") || json.getAsJsonArray("placement").isEmpty()) {
                    continue;
                }
                JsonObject count = json.getAsJsonArray("placement").get(0).getAsJsonObject();
                if (!"orespawn:legacy_count".equals(count.get("type").getAsString())) {
                    continue;
                }
                String path = file.getKey().getPath();
                ResourceLocation id = rl(path.substring("worldgen/placed_feature/".length(), path.length() - ".json".length()));
                PlacedFeature placed = placedFeatures.get(id);
                if (placed == null || !(placed.placement().get(0) instanceof LegacyCountPlacement modifier)) {
                    failures.add(id + ": not loaded with a legacy_count modifier first");
                    continue;
                }
                checkedFeatures++;
                int divisor = intOr(count, "less_ore_divisor", 1);
                boolean requiresNoLessOre = count.has("requires_no_less_ore") && count.get("requires_no_less_ore").getAsBoolean();
                int rate = count.has("stat") ? statRate(count.get("stat").getAsString()) : Integer.MIN_VALUE;
                int base = intOr(count, "base", 0);
                int extra = intOr(count, "extra", 0);
                int bonus = count.has("bonus_one_in") ? intOr(count, "bonus", 0) : 0;
                int shorter = 0;
                for (int s = 0; s < 200; s++) {
                    lessOre.set(0);
                    int c0 = modifier.count(RandomSource.create(s));
                    List<BlockPos> p0 = attempts(level, generator, placed, s);
                    lessOre.set(1);
                    int c1 = modifier.count(RandomSource.create(s));
                    List<BlockPos> p1 = attempts(level, generator, placed, s);
                    int low = rate != Integer.MIN_VALUE ? rate : base;
                    boolean off = rate != Integer.MIN_VALUE && rate <= 0;
                    if (off ? c0 != 0 : (c0 < low || c0 > low + Math.max(0, extra - 1) + bonus)) {
                        failures.add(id + " seed " + s + ": LessOre 0 count " + c0 + " outside " + low + ".." + (low + extra - 1 + bonus));
                    }
                    if (requiresNoLessOre && !off && c0 != rate) {
                        failures.add(id + " seed " + s + ": requires_no_less_ore count " + c0 + ", expected the rate " + rate);
                    }
                    int expected = requiresNoLessOre ? 0 : (divisor > 1 ? c0 / divisor : c0);
                    if (c1 != expected) {
                        failures.add(id + " seed " + s + ": LessOre 1 count " + c1 + ", expected " + expected + " from " + c0);
                    }
                    if (p1.size() > p0.size() || !p0.subList(0, p1.size()).equals(p1)) {
                        failures.add(id + " seed " + s + ": LessOre-1 attempts are not a prefix of the LessOre-0 attempts");
                    }
                    if (p1.size() < p0.size()) {
                        shorter++;
                    }
                }
                if ((requiresNoLessOre || divisor > 1) && (rate == Integer.MIN_VALUE || rate > 0) && shorter == 0) {
                    failures.add(id + ": LessOre 1 never removed an attempt in 200 seeds");
                }
            }
            if (checkedFeatures < 10) {
                failures.add("only " + checkedFeatures + " placed features with legacy_count found");
            }
            veins(level, generator, placedFeatures, lessOre, failures);
        } finally {
            lessOre.set(savedLessOre);
            lessLag.set(savedLessLag);
        }
        report(helper, failures);
    }

    private static void veins(ServerLevel level, ChunkGenerator generator, Registry<PlacedFeature> placedFeatures,
                              ModConfigSpec.IntValue lessOre, List<String> failures) {
        PlacedFeature titanium = placedFeatures.get(rl("ore_titanium"));
        Block ore = BuiltInRegistries.BLOCK.get(rl("oretitanium"));
        if (titanium == null || ore == Blocks.AIR) {
            failures.add("ore_titanium or oretitanium missing");
            return;
        }
        var stats = LegacyOreStat.TITANIUM.get();
        int minY = Math.max(level.getMinBuildHeight(), stats.mindepth() - 6);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, stats.maxdepth() + 6);
        Random pick = new Random(System.nanoTime());
        ChunkPos[] areas = freshAreas(level, pick, 2, 48, 48);
        BlockPos[] origins = new BlockPos[2];
        for (int a = 0; a < 2; a++) {
            ChunkPos center = new ChunkPos(areas[a].x + 1, areas[a].z + 1);
            origins[a] = new BlockPos(center.getMinBlockX(), 0, center.getMinBlockZ());
            fillStone(level, origins[a], minY, maxY);
        }
        long found = -1;
        int oresA = 0;
        for (long s = 0; s < 64 && found < 0; s++) {
            lessOre.set(0);
            titanium.place(level, generator, RandomSource.create(s), origins[0]);
            oresA = countOres(level, origins[0], minY, maxY, ore);
            if (oresA > 0) {
                found = s;
            }
        }
        if (found < 0) {
            failures.add("ore_titanium placed no ore in a stone cuboid in 64 seeds");
            return;
        }
        lessOre.set(1);
        titanium.place(level, generator, RandomSource.create(found), origins[1]);
        int oresB = countOres(level, origins[1], minY, maxY, ore);
        int notInA = 0;
        BlockPos.MutableBlockPos a = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos b = new BlockPos.MutableBlockPos();
        for (int dx = -16; dx < 32; dx++) {
            for (int dz = -16; dz < 32; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    b.set(origins[1].getX() + dx, y, origins[1].getZ() + dz);
                    a.set(origins[0].getX() + dx, y, origins[0].getZ() + dz);
                    if (level.getBlockState(b).is(ore) && !level.getBlockState(a).is(ore)) {
                        notInA++;
                    }
                }
            }
        }
        if (notInA != 0 || oresB > oresA) {
            failures.add("titanium seed " + found + ": LessOre 1 placed " + oresB + " ores, " + notInA
                    + " of them not in the LessOre-0 vein of " + oresA);
        }
        OreSpawn.LOG.info("W13 LessOre veins: titanium seed {}: LessOre 0 {} ores, LessOre 1 {} ores", found, oresA, oresB);
    }

    private static void fillStone(ServerLevel level, BlockPos origin, int minY, int maxY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -16; dx < 32; dx++) {
            for (int dz = -16; dz < 32; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    level.setBlock(pos.set(origin.getX() + dx, y, origin.getZ() + dz), Blocks.STONE.defaultBlockState(),
                            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }
    }

    private static int countOres(ServerLevel level, BlockPos origin, int minY, int maxY, Block ore) {
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -16; dx < 32; dx++) {
            for (int dz = -16; dz < 32; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    if (level.getBlockState(pos.set(origin.getX() + dx, y, origin.getZ() + dz)).is(ore)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /** The positions the placement modifiers hand to the feature for one chunk, drawn as {@code PlacedFeature} draws them. */
    private static List<BlockPos> attempts(ServerLevel level, ChunkGenerator generator, PlacedFeature placed, long seed) {
        PlacementContext context = new PlacementContext(level, generator, Optional.empty());
        RandomSource random = RandomSource.create(seed);
        Stream<BlockPos> stream = Stream.of(new BlockPos(1_600_000, 0, 1_600_000));
        for (PlacementModifier modifier : placed.placement()) {
            stream = stream.flatMap(pos -> modifier.getPositions(context, random, pos));
        }
        return stream.toList();
    }

    private static int intOr(JsonObject json, String key, int fallback) {
        JsonElement e = json.get(key);
        return e == null ? fallback : e.getAsInt();
    }

    private static int statRate(String name) {
        for (LegacyOreStat stat : LegacyOreStat.values()) {
            if (stat.getSerializedName().equals(name)) {
                return stat.get().rate();
            }
        }
        throw new IllegalStateException("unknown ore stat " + name);
    }

    // =====================================================================================================
    // 4. config_spawns: the enable flags of every table entry

    /**
     * Every entry of {@code orespawn:spawns} in every biome the registry holds: with all its switches set and
     * {@code AllMobsDisable} 0 it is added (date guards as at this server start); with any one of its switches 0 it is
     * not; with {@code AllMobsDisable} 1 only entries whose switches are all Rock, Cricket or Frog remain. Counted as
     * (type, category, weight, min, max) against the unmodified biome, so two entries of one type are told apart.
     */
    @GameTest(template = ARENA, timeoutTicks = 1200)
    public static void configSpawnsAddsEveryEntryOnlyWithAllItsEnableFlags(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        ResourceLocation tableId = rl("spawns");
        ConfigSpawnsBiomeModifier modifier = new ConfigSpawnsBiomeModifier(tableId);
        SpawnTable table = SpawnTable.forServer(level.getServer(), tableId);
        Map<String, ModConfigSpec.IntValue> switches = mobSwitches();
        ModConfigSpec.IntValue allMobs = OreSpawnConfig.TWEAKS.AllMobsDisable;

        Set<String> used = new HashSet<>();
        for (ResourceLocation biome : table.biomes()) {
            for (SpawnTable.Entry entry : table.entries(biome)) {
                used.addAll(entry.flags());
            }
        }
        Map<String, Integer> saved = new HashMap<>();
        for (String flag : used) {
            ModConfigSpec.IntValue value = switches.get(flag);
            if (value == null) {
                failures.add("spawn table flag " + flag + " is no OreSpawnMOBS key");
                continue;
            }
            saved.put(flag, value.get());
        }
        int savedAll = allMobs.get();
        int biomesChecked = 0;
        int flagChecks = 0;
        try {
            allMobs.set(0);
            saved.keySet().forEach(flag -> switches.get(flag).set(1));
            for (ResourceLocation biomeId : new java.util.TreeSet<>(table.biomes())) {
                Optional<Holder.Reference<Biome>> holder = biomes.getHolder(ResourceKey.create(Registries.BIOME, biomeId));
                if (holder.isEmpty()) {
                    continue;
                }
                biomesChecked++;
                List<SpawnTable.Entry> entries = table.entries(biomeId);
                checkBiome(failures, modifier, holder.get(), entries, switches, "all switches 1");
                Set<String> flags = new java.util.TreeSet<>();
                entries.forEach(e -> flags.addAll(e.flags()));
                for (String flag : flags) {
                    if (!saved.containsKey(flag)) {
                        continue;
                    }
                    switches.get(flag).set(0);
                    checkBiome(failures, modifier, holder.get(), entries, switches, flag + " 0");
                    switches.get(flag).set(1);
                    flagChecks++;
                }
                allMobs.set(1);
                checkBiome(failures, modifier, holder.get(), entries, switches, "AllMobsDisable 1");
                allMobs.set(0);
            }
        } finally {
            saved.forEach((flag, value) -> switches.get(flag).set(value));
            allMobs.set(savedAll);
        }
        if (biomesChecked < 20) {
            failures.add("only " + biomesChecked + " table biomes found in the registry");
        }
        OreSpawn.LOG.info("W13 config_spawns: {} biomes, {} single-switch checks", biomesChecked, flagChecks);
        report(helper, failures);
    }

    private static final Set<String> SURVIVES_ALL_MOBS_DISABLE = Set.of("RockEnable", "CricketEnable", "FrogEnable");

    private static void checkBiome(List<String> failures, ConfigSpawnsBiomeModifier modifier, Holder<Biome> biome,
                                   List<SpawnTable.Entry> entries, Map<String, ModConfigSpec.IntValue> switches,
                                   String condition) {
        boolean allMobsDisabled = OreSpawnConfig.TWEAKS.AllMobsDisable.get() != 0;
        Map<MobCategory, Map<String, Integer>> expected = new HashMap<>();
        for (SpawnTable.Entry entry : entries) {
            EntityType<?> type = entry.resolve();
            if (type == null || type.getCategory() == MobCategory.MISC) {
                continue;
            }
            boolean on = true;
            for (String flag : entry.flags()) {
                ModConfigSpec.IntValue value = switches.get(flag);
                if (value == null || value.get() == 0 || (allMobsDisabled && !SURVIVES_ALL_MOBS_DISABLE.contains(flag))) {
                    on = false;
                }
            }
            if (entry.date() == SpawnTable.DateGuard.HALLOWEEN && OreSpawn.halloween == 0) {
                on = false;
            }
            if (entry.date() == SpawnTable.DateGuard.EASTER && OreSpawn.easter_day == 0) {
                on = false;
            }
            expected.computeIfAbsent(entry.category(), c -> new HashMap<>())
                    .merge(tuple(type, entry.weight(), entry.min(), entry.max()), on ? 1 : 0, Integer::sum);
        }
        ModifiableBiomeInfo.BiomeInfo.Builder builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(
                biome.value().modifiableBiomeInfo().getOriginalBiomeInfo());
        modifier.modify(biome, BiomeModifier.Phase.ADD, builder);
        for (Map.Entry<MobCategory, Map<String, Integer>> byCategory : expected.entrySet()) {
            MobCategory category = byCategory.getKey();
            Map<String, Integer> added = tuples(builder.getMobSpawnSettings().getSpawner(category));
            Map<String, Integer> original = tuples(biome.value().modifiableBiomeInfo().getOriginalBiomeInfo()
                    .mobSpawnSettings().getMobs(category).unwrap());
            for (Map.Entry<String, Integer> e : byCategory.getValue().entrySet()) {
                int got = added.getOrDefault(e.getKey(), 0) - original.getOrDefault(e.getKey(), 0);
                if (got != e.getValue()) {
                    failures.add(biome.unwrapKey().map(k -> k.location().toString()).orElse("?") + " " + category + " "
                            + e.getKey() + " with " + condition + ": added " + got + ", expected " + e.getValue());
                }
            }
        }
    }

    private static String tuple(EntityType<?> type, int weight, int min, int max) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type) + " " + weight + "/" + min + "-" + max;
    }

    private static Map<String, Integer> tuples(List<MobSpawnSettings.SpawnerData> list) {
        Map<String, Integer> map = new HashMap<>();
        for (MobSpawnSettings.SpawnerData data : list) {
            map.merge(tuple(data.type, data.getWeight().asInt(), data.minCount, data.maxCount), 1, Integer::sum);
        }
        return map;
    }

    private static Map<String, ModConfigSpec.IntValue> mobSwitches() {
        Map<String, ModConfigSpec.IntValue> map = new HashMap<>();
        for (Field field : OreSpawnConfig.Mobs.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == ModConfigSpec.IntValue.class) {
                try {
                    map.put(field.getName(), (ModConfigSpec.IntValue) field.get(OreSpawnConfig.MOBS));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return map;
    }

    // =====================================================================================================
    // helpers

    private static final class Compare {
        int nonAir;
        int blocks;
        int containers;
        int spawnerData;
        int chests;
        int spawners;
        String first = "-";

        int diffs() {
            return this.blocks + this.containers + this.spawnerData;
        }

        /** Called after a counter went up; keeps the first difference. */
        void note(String what) {
            if (diffs() == 1) {
                this.first = what;
            }
        }

        @Override
        public String toString() {
            return this.blocks + " blocks, " + this.containers + " chests, " + this.spawnerData + " spawners differ; first "
                    + this.first;
        }
    }

    /** The window against the same window shifted by (dx, dz): states, container contents, spawner data. */
    private static Compare compare(ServerLevel level, BoundingBox window, int dx, int dz, BlockPos skip) {
        Compare cmp = new Compare();
        BlockPos.MutableBlockPos a = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos b = new BlockPos.MutableBlockPos();
        int minY = Math.max(window.minY(), level.getMinBuildHeight());
        int maxY = Math.min(window.maxY(), level.getMaxBuildHeight() - 1);
        for (int x = window.minX(); x <= window.maxX(); x++) {
            for (int z = window.minZ(); z <= window.maxZ(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    a.set(x, y, z);
                    if (a.equals(skip)) {
                        continue;
                    }
                    b.set(x + dx, y, z + dz);
                    BlockState sa = level.getBlockState(a);
                    BlockState sb = level.getBlockState(b);
                    if (!sa.isAir()) {
                        cmp.nonAir++;
                    }
                    if (sa != sb) {
                        cmp.blocks++;
                        cmp.note(a.toShortString() + " " + sa + " vs " + sb);
                        continue;
                    }
                    if (!sa.hasBlockEntity()) {
                        continue;
                    }
                    BlockEntity ea = level.getBlockEntity(a);
                    BlockEntity eb = level.getBlockEntity(b);
                    if (ea instanceof Container ca && eb instanceof Container cb) {
                        cmp.chests++;
                        if (!sameContents(ca, cb)) {
                            cmp.containers++;
                            cmp.note("chest " + a.toShortString());
                        }
                    } else if (ea instanceof SpawnerBlockEntity pa && eb instanceof SpawnerBlockEntity pb) {
                        cmp.spawners++;
                        CompoundTag ta = pa.getSpawner().save(new CompoundTag());
                        CompoundTag tb = pb.getSpawner().save(new CompoundTag());
                        if (!Objects.equals(ta.get("SpawnData"), tb.get("SpawnData"))) {
                            cmp.spawnerData++;
                            cmp.note("spawner " + a.toShortString() + " " + ta.get("SpawnData") + " vs " + tb.get("SpawnData"));
                        }
                    }
                }
            }
        }
        return cmp;
    }

    private static boolean sameContents(Container a, Container b) {
        if (a.getContainerSize() != b.getContainerSize()) {
            return false;
        }
        for (int i = 0; i < a.getContainerSize(); i++) {
            if (!ItemStack.matches(a.getItem(i), b.getItem(i))) {
                return false;
            }
        }
        return true;
    }

    /** Non-air blocks in {@code window} outside {@code inner}. */
    /**
     * Non-air blocks of {@code window} outside {@code inner}; {@code inner == null} counts the whole window. (An
     * inverted {@code BoundingBox} as the empty box throws in a dev run: {@code BoundingBox.<init>} checks
     * {@code SharedConstants.IS_RUNNING_IN_IDE}.)
     */
    private static int nonAirOutside(ServerLevel level, BoundingBox window, BoundingBox inner) {
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = Math.max(window.minY(), level.getMinBuildHeight());
        int maxY = Math.min(window.maxY(), level.getMaxBuildHeight() - 1);
        for (int x = window.minX(); x <= window.maxX(); x++) {
            for (int z = window.minZ(); z <= window.maxZ(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    if ((inner == null || !inner.isInside(x, y, z)) && !level.getBlockState(pos.set(x, y, z)).isAir()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static BoundingBox inflate(BoundingBox box, int n) {
        return new BoundingBox(box.minX() - n, box.minY() - n, box.minZ() - n, box.maxX() + n, box.maxY() + n, box.maxZ() + n);
    }

    private static List<Entity> entities(ServerLevel level, BoundingBox box) {
        return level.getEntitiesOfClass(Entity.class, AABB.of(box).inflate(1.0), e -> !(e instanceof Player));
    }

    private static int entityCount(ServerLevel level, BoundingBox box) {
        return entities(level, box).size();
    }

    /** Entity types with counts and how many are persistent, sorted. */
    private static String entitySummary(ServerLevel level, BoundingBox box) {
        Map<String, int[]> map = new TreeMap<>();
        for (Entity entity : entities(level, box)) {
            int[] c = map.computeIfAbsent(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), k -> new int[2]);
            c[0]++;
            if (entity instanceof Mob mob && mob.isPersistenceRequired()) {
                c[1]++;
            }
        }
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> sb.append(k).append(' ').append(v[0]).append('/').append(v[1]).append(' '));
        return sb.toString().trim();
    }

    private static void discardEntities(ServerLevel level, BoundingBox box) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, AABB.of(box).inflate(4.0), e -> !(e instanceof Player))) {
            entity.discard();
        }
    }

    /** As W12GameTests.freshAreas: areas whose chunks (plus one around) were never loaded or saved, far out. */
    private static ChunkPos[] freshAreas(ServerLevel level, Random pick, int count, int spanX, int spanZ) {
        ServerChunkCache source = level.getChunkSource();
        int chunksX = (spanX + 15) / 16 + 2;
        int chunksZ = (spanZ + 15) / 16 + 2;
        ChunkPos[] areas = new ChunkPos[count];
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; ; attempt++) {
                if (attempt > 64) {
                    throw new IllegalStateException("no fresh area of " + chunksX + "x" + chunksZ + " chunks found");
                }
                int cx = 72_000 + pick.nextInt(46_000);
                int cz = 72_000 + pick.nextInt(46_000);
                boolean fresh = true;
                for (int dx = -1; dx < chunksX && fresh; dx++) {
                    for (int dz = -1; dz < chunksZ && fresh; dz++) {
                        ChunkPos pos = new ChunkPos(cx + dx, cz + dz);
                        if (source.getChunkNow(pos.x, pos.z) != null || source.chunkMap.read(pos).join().isPresent()) {
                            fresh = false;
                        }
                    }
                }
                if (fresh) {
                    areas[i] = new ChunkPos(cx, cz);
                    break;
                }
            }
        }
        return areas;
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
            OreSpawn.LOG.warn("W13 writer failure detail: {}", message);
        }
        helper.assertTrue(failures.isEmpty(), message.length() > 700 ? message.substring(0, 700) + " ... (full text in the log)" : message);
        helper.succeed();
    }
}
