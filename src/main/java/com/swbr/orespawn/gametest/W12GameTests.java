package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.spawner.DungeonSpawnerBlock;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.item.magic.ItemMagicApple;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.world.OreSpawnWorld;
import com.swbr.orespawn.world.feature.LegacyMinableFeature;
import com.swbr.orespawn.world.feature.OreSpawnFeatures;
import com.swbr.orespawn.world.maze.BasiliskMaze;
import com.swbr.orespawn.world.maze.RubyBirdDungeon;
import com.swbr.orespawn.world.spawn.ConfigSpawnsBiomeModifier;
import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.LegacyStructure;
import com.swbr.orespawn.world.structure.LegacyStructurePiece;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry.Placement;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.tree.LegacyRandom;
import com.swbr.orespawn.world.tree.LegacyWriter;
import com.swbr.orespawn.world.tree.TreeBuilders;
import com.swbr.orespawn.world.tree.Trees;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Wave 12 on a real dedicated server: the tree, maze and dungeon builders of the shared structure infrastructure,
 * the features of this wave, the {@code orespawn:config_spawns} biome modifier, recipes, chest loot and the two items
 * that build structures at run time (catalogue README 4.13).
 *
 * <p>Three kinds of evidence for the builders:
 * <ul>
 * <li>{@link Recorder}: the ported original method runs against an in-memory world that remembers every write. Every
 * write must lie inside the {@link Placement} box the dispatcher hands to the structure piece (DECISIONS R12) - a
 * write outside is silently clipped in world generation and the structure would be cut.</li>
 * <li>A: the registered builder with an unclipped writer on the live level (what the original did). Its block count
 * must equal the recorder's, which ties the builder id to the original method.</li>
 * <li>B: the same placement as a {@link LegacyStructurePiece}, post-processed chunk by chunk with the clip world
 * generation uses. B must equal A block by block and chest by chest: no seams across chunk runs.</li>
 * </ul>
 * Every level test uses a random far area whose chunks were never generated; the GameTest world persists between
 * runs.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W12GameTests {

    private static final String ARENA = "arena";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    /** Overworld build height; the recorder drops writes outside it like the level does. */
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;
    /**
     * Probe origin of the in-memory builds, chunk aligned. The Magic Apple trees and WorldGenMinable add float offsets to
     * block coordinates ({@code (float) inx + 0.5f}, {@code (float) (x + 8) + sin * n / 8}): the result depends on the
     * magnitude of the coordinate, as in 1.7.10. Recorder, A and B therefore all lie in [2^20, 2^21) on both axes
     * (float spacing 1/8 there, the same everywhere in the range), and {@link #freshAreas} picks chunks inside it.
     */
    private static final int PROBE = 1_500_000;

    private W12GameTests() {}

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
    }

    // =====================================================================================================
    // In-memory world

    /** A {@link LegacyWriter} over an air world that remembers every write; semantics of a clipped writer over air. */
    private static final class Recorder implements LegacyWriter {
        final Long2ObjectOpenHashMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
        int writes;
        int spawners;
        int chests;
        int entities;
        /** Entities whose NBT, written right after {@code beforeAdd} as ProtoChunk.addEntity does, is persistent. */
        int persistentOnSave;
        /** When set, spawned types are created here and {@code beforeAdd} runs on them; never added. */
        @Nullable
        net.minecraft.server.level.ServerLevel level;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        @Override
        public BlockState getBlock(int x, int y, int z) {
            if (y < MIN_Y || y >= MAX_Y) {
                return AIR;
            }
            return this.blocks.getOrDefault(BlockPos.asLong(x, y, z), AIR);
        }

        private void put(int x, int y, int z, BlockState state) {
            if (y < MIN_Y || y >= MAX_Y) {
                return;
            }
            this.writes++;
            this.blocks.put(BlockPos.asLong(x, y, z), state);
            this.minX = Math.min(this.minX, x);
            this.minY = Math.min(this.minY, y);
            this.minZ = Math.min(this.minZ, z);
            this.maxX = Math.max(this.maxX, x);
            this.maxY = Math.max(this.maxY, y);
            this.maxZ = Math.max(this.maxZ, z);
        }

        @Override
        public void setBlockFast(int x, int y, int z, BlockState state) {
            this.put(x, y, z, state);
        }

        @Override
        public void setBlock(int x, int y, int z, BlockState state) {
            this.put(x, y, z, state);
        }

        @Override
        public void placeSpawner(int x, int y, int z, String entityId) {
            this.spawners++;
            this.put(x, y, z, Blocks.SPAWNER.defaultBlockState());
        }

        @Nullable
        @Override
        public Container placeChest(int x, int y, int z) {
            this.chests++;
            this.put(x, y, z, LegacyMeta.chest(-1));
            return new SimpleContainer(27);
        }

        @Nullable
        @Override
        public Entity spawnEntity(String entityId, double x, double y, double z, Random yawRand,
                                  Consumer<Entity> beforeAdd) {
            if (StructureWriter.entityType(entityId).isPresent()) {
                yawRand.nextFloat();
                this.entities++;
                if (this.level != null) {
                    Entity entity = StructureWriter.entityType(entityId).get().create(this.level);
                    if (entity != null) {
                        beforeAdd.accept(entity);
                        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
                        entity.saveWithoutId(tag);
                        if (tag.getBoolean("PersistenceRequired")) {
                            this.persistentOnSave++;
                        }
                        entity.discard();
                    }
                }
            }
            return null;
        }

        boolean wroteAnything() {
            return this.minX <= this.maxX;
        }

        int nonAir() {
            int count = 0;
            for (BlockState state : this.blocks.values()) {
                if (!state.isAir()) {
                    count++;
                }
            }
            return count;
        }

        int count(net.minecraft.world.level.block.Block block) {
            int count = 0;
            for (BlockState state : this.blocks.values()) {
                if (state.is(block)) {
                    count++;
                }
            }
            return count;
        }

        /** Writes outside {@code box}, as "count, first position". */
        String outside(BoundingBox box) {
            int count = 0;
            BlockPos first = null;
            for (Long2ObjectMap.Entry<BlockState> e : this.blocks.long2ObjectEntrySet()) {
                BlockPos pos = BlockPos.of(e.getLongKey());
                if (!box.isInside(pos)) {
                    count++;
                    if (first == null) {
                        first = pos;
                    }
                }
            }
            return count == 0 ? null : count + " writes outside " + box + ", first " + first.toShortString()
                    + ", extent " + this.minX + ".." + this.maxX + " / " + this.minY + ".." + this.maxY + " / "
                    + this.minZ + ".." + this.maxZ;
        }
    }

    /**
     * One builder of this wave: its registered id, the placement box its dispatcher uses, and the ported original
     * method as the builder lambda of {@link TreeBuilders#bootstrap} calls it.
     */
    private record Build(String name, Placement placement, Consumer<Recorder> original) {}

    /** The builders, all at origin (x, y, z) with structure seed {@code seed}; mirrors {@code TreeBuilders.bootstrap}. */
    private static List<Build> builds(int x, int z, long seed) {
        List<Build> builds = new ArrayList<>();
        for (int dir = 0; dir < 4; dir++) {
            final int d = dir;
            builds.add(new Build("wind_tree dir " + dir, TreeBuilders.windTree(x, 100, z, dir),
                    w -> Trees.WindTree(w, new Random(seed), x, 100, z, d, false)));
        }
        builds.add(new Build("sky_tree", TreeBuilders.skyTree(x, 120, z),
                w -> Trees.SkyTree(w, new Random(seed), x, 120, z, false)));
        builds.add(new Build("fairy_tree", TreeBuilders.fairyTree(x, 100, z),
                w -> Trees.FairyTree(w, new Random(seed), x, 100, z)));
        builds.add(new Build("fairy_castle_tree", TreeBuilders.fairyCastleTree(x, 100, z),
                w -> Trees.FairyCastleTree(w, new Random(seed), x, 100, z)));
        builds.add(new Build("basilisk_maze", TreeBuilders.basiliskMaze(x, 100, z), w -> {
            Random random = new Random(seed);
            BasiliskMaze.buildBasiliskMaze(w, random, new Random(random.nextLong()), x, 100, z);
        }));
        builds.add(new Build("magic_apple circular", TreeBuilders.magicAppleTree(x, 100, z, TreeBuilders.SHAPE_CIRCULAR,
                1, 6, false, TreeBuilders.BLOCKS_LOG_LEAVES_MOSSY), w -> {
            Random random = new Random(seed);
            ItemMagicApple.MakeBigCircularTree(w, random, x, 100, z, Blocks.OAK_LOG, Blocks.OAK_LEAVES,
                    Blocks.MOSSY_COBBLESTONE, 1, 6, false);
        }));
        builds.add(new Build("magic_apple round", TreeBuilders.magicAppleTree(x, 100, z, TreeBuilders.SHAPE_ROUND,
                2, 5, true, TreeBuilders.BLOCKS_LOG_LEAVES_MOSSY), w -> {
            Random random = new Random(seed);
            ItemMagicApple.MakeBigRoundTree(w, random, x, 100, z, Blocks.OAK_LOG, Blocks.OAK_LEAVES,
                    Blocks.MOSSY_COBBLESTONE, 2, 5);
        }));
        builds.add(new Build("magic_apple square", TreeBuilders.magicAppleTree(x, 100, z, TreeBuilders.SHAPE_SQUARE,
                0, 6, false, TreeBuilders.BLOCKS_LOG_APPLE_LEAVES_MOSSY), w -> {
            Random random = new Random(seed);
            ItemMagicApple.MakeBigSquareTree(w, random, random, x, 100, z, Blocks.OAK_LOG,
                    ModBlocks.LEAVES_APPLE.get(), Blocks.MOSSY_COBBLESTONE, 0, 6, false);
        }));
        return builds;
    }

    // =====================================================================================================
    // 1. every build stays inside its placement box

    /**
     * R12: a structure piece writes only inside its bounding box, so the box must hold every write of the original
     * method. Checked for every builder with 16 seeds, the Magic Apple trees additionally for every tree type,
     * radius 1..6 (world generation rolls 4..6, the item uses 6) and both critter switches, and the Ruby Bird dungeon
     * inside the 3x3-chunk feature window at the largest origin offset.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void everyBuilderWritesOnlyInsideItsPlacementBox(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        int runs = 0;
        for (long seed = 1; seed <= 16; seed++) {
            for (Build build : builds(1000, -2000, seed * 7919L)) {
                Recorder rec = new Recorder();
                build.original().accept(rec);
                runs++;
                if (!rec.wroteAnything()) {
                    failures.add(build.name() + " seed " + seed + ": wrote nothing");
                    continue;
                }
                String outside = rec.outside(build.placement().box());
                if (outside != null) {
                    failures.add(build.name() + " seed " + seed + ": " + outside);
                }
            }
        }
        int[] blockSets = {TreeBuilders.BLOCKS_LOG_LEAVES_MOSSY, TreeBuilders.BLOCKS_GOLD_EMERALD_DIAMOND};
        for (int shape = 0; shape < 3; shape++) {
            for (int treeType = -1; treeType <= 3; treeType++) {
                for (int radius = 1; radius <= 6; radius++) {
                    for (boolean bad : new boolean[] {false, true}) {
                        for (long seed = 1; seed <= 3; seed++) {
                            Placement p = TreeBuilders.magicAppleTree(0, 90, 0, shape, treeType, radius, bad, blockSets[(int) seed % 2]);
                            Recorder rec = new Recorder();
                            Random random = new Random(seed * 31L + radius);
                            switch (shape) {
                                case TreeBuilders.SHAPE_CIRCULAR -> ItemMagicApple.MakeBigCircularTree(rec, random, 0, 90, 0,
                                        Blocks.OAK_LOG, Blocks.OAK_LEAVES, Blocks.MOSSY_COBBLESTONE, treeType, radius, bad);
                                case TreeBuilders.SHAPE_ROUND -> ItemMagicApple.MakeBigRoundTree(rec, random, 0, 90, 0,
                                        Blocks.OAK_LOG, Blocks.OAK_LEAVES, Blocks.MOSSY_COBBLESTONE, treeType, radius);
                                default -> ItemMagicApple.MakeBigSquareTree(rec, random, random, 0, 90, 0,
                                        Blocks.GOLD_BLOCK, Blocks.EMERALD_BLOCK, Blocks.DIAMOND_BLOCK, treeType, radius, bad);
                            }
                            runs++;
                            String outside = rec.outside(p.box());
                            if (outside != null) {
                                failures.add("magic apple shape " + shape + " type " + treeType + " radius " + radius
                                        + " bad " + bad + " seed " + seed + ": " + outside);
                            }
                        }
                    }
                }
            }
        }
        // RubyBirdDungeonFeature: origin at chunk+7 (the largest offset OreSpawnWorld uses), extent +9.
        for (long seed = 1; seed <= 16; seed++) {
            Recorder rec = new Recorder();
            RubyBirdDungeon.makeDungeon(rec, new Random(seed), 16 * 3 + 7, 60, 16 * -5 + 7);
            BoundingBox window = new BoundingBox(16 * 2, MIN_Y, 16 * -6, 16 * 5 - 1, MAX_Y - 1, 16 * -3 - 1);
            String outside = rec.outside(window);
            if (outside != null) {
                failures.add("ruby bird dungeon seed " + seed + ": " + outside);
            }
        }
        OreSpawn.LOG.info("W12 box test: {} builder runs, {} failures", runs, failures.size());
        report(helper, failures);
    }

    // =====================================================================================================
    // 2. block counts of the original methods

    /**
     * One sample per builder, counted against what the original method writes (verhalten/world-04.md):
     * RubyBirdDungeon.java:31-80 writes exactly 10x5x10 positions, 190 of them air, one spawner and one chest; the
     * Wind Tree (Trees.java:41-75) a trunk of {@code nextInt(8) + 40} logs with a leaf on top; the Sky Tree
     * (Trees.java:95-120) logs from y to {@code nextInt(15) + 190} with four branches of {@code nextInt(10) + 25}; the
     * Fairy Tree (Trees.java:475-509) a 2x2x5 crystal trunk, a spawner at (x-1, y+1, z) and a chest at (x+2, y+1, z);
     * the Basilisk maze (BasiliskMaze.java:26-33, :426-472) a sandstone ring corner at (x-8, y, z-8) and a bedrock
     * shaft down to {@code y - depth + 1}.
     */
    @GameTest(template = ARENA)
    public static void builderSamplesWriteTheBlockCountsOfTheOriginalMethods(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        final int x = 500;
        final int z = 700;

        Recorder ruby = new Recorder();
        RubyBirdDungeon.makeDungeon(ruby, new Random(42L), x, 64, z);
        expect(failures, "ruby dungeon distinct positions", 500, ruby.blocks.size());
        expect(failures, "ruby dungeon air positions", 190, ruby.blocks.size() - ruby.nonAir());
        expect(failures, "ruby dungeon spawners", 1, ruby.spawners);
        expect(failures, "ruby dungeon chests", 1, ruby.chests);
        expect(failures, "ruby dungeon interior floor (mossy)", 64, countInterior(ruby, x, 64, z));
        if (!ruby.getBlock(x + 5, 65, z + 5).is(Blocks.SPAWNER) || !ruby.getBlock(x + 5, 65, z + 1).is(Blocks.CHEST)) {
            failures.add("ruby dungeon: spawner or chest not at (x+5, y+1, z+5) / (x+5, y+1, z+1)");
        }
        int shell = ruby.count(Blocks.COBBLESTONE) + ruby.count(Blocks.MOSSY_COBBLESTONE) + ruby.count(ModBlocks.ORERUBY.get());
        expect(failures, "ruby dungeon cobble/mossy/ruby ore (walls, ceiling, floor)", 308, shell);

        long windSeed = 1234L;
        Recorder wind = new Recorder();
        Trees.WindTree(wind, new Random(windSeed), x, 70, z, 0, false);
        int height = new Random(windSeed).nextInt(8) + 40;
        for (int j = 0; j < height; j++) {
            if (!wind.getBlock(x, 70 + j, z).is(Blocks.OAK_LOG)) {
                failures.add("wind tree: no log at trunk level " + j + " of " + height);
                break;
            }
        }
        if (!wind.getBlock(x, 70 + height, z).is(Blocks.OAK_LEAVES)) {
            failures.add("wind tree: no leaves on top of the " + height + " trunk");
        }
        if (wind.maxY != 70 + height || wind.minX != x || wind.minY != 70) {
            failures.add("wind tree dir 0: extent x from " + wind.minX + ", y " + wind.minY + ".." + wind.maxY
                    + ", expected x from " + x + ", y 70.." + (70 + height));
        }

        long skySeed = 99L;
        Recorder sky = new Recorder();
        Trees.SkyTree(sky, new Random(skySeed), x, 100, z, false);
        Random skyRandom = new Random(skySeed);
        int top = skyRandom.nextInt(15) + 190;
        int width = skyRandom.nextInt(10) + 25;
        int logs = 0;
        for (int j = 100; j <= top; j++) {
            if (sky.getBlock(x, j, z).is(ModBlocks.SKY_TREE_LOG.get())) {
                logs++;
            }
        }
        expect(failures, "sky tree trunk logs", top - 100 + 1, logs);
        for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            if (!sky.getBlock(x + (width - 1) * d[0], top, z + (width - 1) * d[1]).is(ModBlocks.SKY_TREE_LOG.get())) {
                failures.add("sky tree: no branch log at length " + (width - 1) + " towards " + d[0] + "," + d[1]);
            }
        }
        if (sky.maxX != x + width || sky.minX != x - width) {
            failures.add("sky tree: x extent " + sky.minX + ".." + sky.maxX + ", expected +-" + width + " (end leaf)");
        }

        Recorder fairy = new Recorder();
        Trees.FairyTree(fairy, new Random(5L), x, 80, z);
        int trunk = 0;
        for (int j = 1; j < 6; j++) {
            for (int i = 0; i < 2; i++) {
                for (int k = 0; k < 2; k++) {
                    if (fairy.getBlock(x + i, 80 + j, z + k).is(ModBlocks.CRYSTAL_TREE_LOG.get())) {
                        trunk++;
                    }
                }
            }
        }
        expect(failures, "fairy tree trunk (2x2x5)", 20, trunk);
        expect(failures, "fairy tree spawners", 1, fairy.spawners);
        expect(failures, "fairy tree chests", 1, fairy.chests);
        if (!fairy.getBlock(x - 1, 81, z).is(Blocks.SPAWNER) || !fairy.getBlock(x + 2, 81, z).is(Blocks.CHEST)) {
            failures.add("fairy tree: spawner or chest misplaced");
        }

        long mazeSeed = 77L;
        Recorder maze = new Recorder();
        maze.level = helper.getLevel();
        Random mazeRandom = new Random(mazeSeed);
        BasiliskMaze.buildBasiliskMaze(maze, mazeRandom, new Random(mazeRandom.nextLong()), x, 100, z);
        Random depthRandom = new Random(mazeSeed);
        depthRandom.nextLong();
        int depth = 20 + depthRandom.nextInt(10);
        if (!maze.getBlock(x - 8, 100, z - 8).is(Blocks.SANDSTONE)) {
            failures.add("basilisk maze: no sandstone ring corner at (x-8, y, z-8)");
        }
        for (int j = 8; j > -depth; j--) {
            if (!maze.getBlock(x, 100 + j, z).is(Blocks.BEDROCK) || !maze.getBlock(x + 3, 100 + j, z + 3).is(Blocks.BEDROCK)) {
                failures.add("basilisk maze (depth " + depth + "): shaft wall missing at y+" + j);
                break;
            }
        }
        if (maze.entities != 3) {
            failures.add("basilisk maze: " + maze.entities + " basilisks spawned, the original spawns 3 (:495-503)");
        }
        if (maze.persistentOnSave != 3) {
            // func_110163_bv (:408-423) must be set before addFreshEntity: in world generation ProtoChunk.addEntity
            // saves the entity at once, so a flag set afterwards is lost.
            failures.add("basilisk maze: " + maze.persistentOnSave + " of 3 basilisks persistent when saved on add");
        }
        report(helper, failures);
    }

    /** Mossy cobblestone at the 8x8 floor interior of a ruby dungeon. */
    private static int countInterior(Recorder rec, int x, int y, int z) {
        int count = 0;
        for (int i = 1; i < 9; i++) {
            for (int k = 1; k < 9; k++) {
                if (rec.getBlock(x + i, y, z + k).is(Blocks.MOSSY_COBBLESTONE)) {
                    count++;
                }
            }
        }
        return count;
    }

    // =====================================================================================================
    // 3. structure pieces: complete and seamless across chunk runs

    /**
     * R12, R18: every builder of this wave as a {@link LegacyStructurePiece}, post-processed chunk by chunk on the
     * live level (B), against the registered builder run once without a clip (A) and against the original method in
     * memory. A and the recorder must agree on the number of blocks; B must equal A in every block and every chest.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400)
    public static void structurePiecesBuildTheWholeStructureWithoutSeamsAcrossChunkRuns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        Random pick = new Random(System.nanoTime());
        long seed = pick.nextLong();
        for (Build probe : builds(PROBE, PROBE, seed)) {
            Recorder rec = new Recorder();
            probe.original().accept(rec);
            if (!rec.wroteAnything()) {
                failures.add(probe.name() + ": original wrote nothing");
                continue;
            }
            // Scan window: the recorder's extent, one block around, relative to origin (0, 0).
            int spanX = rec.maxX - rec.minX + 3;
            int spanZ = rec.maxZ - rec.minZ + 3;
            ChunkPos[] areas = freshAreas(level, pick, 2, spanX, spanZ);
            // Chunk-aligned shifts of the probe origin, so every float and double sum of the builders rounds the same
            // way in the recorder, in A and in B (all coordinates share one power-of-two range, see PROBE).
            int ax = PROBE + 16 * (areas[0].x - ((rec.minX - 1) >> 4));
            int az = PROBE + 16 * (areas[0].z - ((rec.minZ - 1) >> 4));
            int bx = PROBE + 16 * (areas[1].x - ((rec.minX - 1) >> 4));
            int bz = PROBE + 16 * (areas[1].z - ((rec.minZ - 1) >> 4));
            Build a = findBuild(builds(ax, az, seed), probe.name());
            Build b = findBuild(builds(bx, bz, seed), probe.name());

            Compare pre = compare(level, ax - PROBE + rec.minX - 1, rec.minY - 1, az - PROBE + rec.minZ - 1,
                    ax - PROBE + rec.maxX + 1, rec.maxY + 1, az - PROBE + rec.maxZ + 1, bx - ax, bz - az);
            if (pre.nonAirA != 0 || pre.blockDiffs != 0) {
                failures.add("precondition " + probe.name() + ": window not empty before building: A " + pre.nonAirA + ", diffs " + pre.blockDiffs + " " + pre.first);
            }
            // A: the registered builder, unclipped.
            LegacyStructureRegistry.Builder builder = LegacyStructureRegistry.builder(a.placement().builder()).orElse(null);
            if (builder == null) {
                failures.add(probe.name() + ": builder " + a.placement().builder() + " is not registered");
                continue;
            }
            Placement pa = a.placement();
            // Both A and B write without neighbour shape updates, as a generation region does: with them, a block the
            // build leaves unsupported (Fairy Castle torches on leaves) pops off or not depending on the order of the
            // later writes nearby, and that order differs between one unclipped run and chunk runs.
            builder.build(StructureWriter.withoutShapeUpdates(level, null), new Random(seed), pa.x(), pa.y(), pa.z(), pa.variant());

            // B: the piece, once per chunk its box meets inside the scan window.
            Placement pb = b.placement();
            LegacyStructurePiece piece = new LegacyStructurePiece(pb, seed);
            int wMinX = bx - PROBE + rec.minX - 1;
            int wMaxX = bx - PROBE + rec.maxX + 1;
            int wMinZ = bz - PROBE + rec.minZ - 1;
            int wMaxZ = bz - PROBE + rec.maxZ + 1;
            ChunkGenerator generator = level.getChunkSource().getGenerator();
            int chunkRuns = 0;
            for (int cx = Math.max(wMinX, pb.box().minX()) >> 4; cx <= Math.min(wMaxX, pb.box().maxX()) >> 4; cx++) {
                for (int cz = Math.max(wMinZ, pb.box().minZ()) >> 4; cz <= Math.min(wMaxZ, pb.box().maxZ()) >> 4; cz++) {
                    ChunkPos chunk = new ChunkPos(cx, cz);
                    level.getChunk(cx, cz);
                    BoundingBox chunkBox = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                            chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
                    // LegacyStructurePiece.postProcess is obtain + replay; the replay here skips the shape updates as A does
                    piece.recording(level, generator).replayWithoutShapeUpdates(level, chunkBox);
                    chunkRuns++;
                }
            }

            Compare cmp = compare(level, ax - PROBE + rec.minX - 1, rec.minY - 1, az - PROBE + rec.minZ - 1,
                    ax - PROBE + rec.maxX + 1, rec.maxY + 1, az - PROBE + rec.maxZ + 1, bx - ax, bz - az);
            OreSpawn.LOG.info("W12 piece test {} (seed {}): recorder {} blocks, A {} blocks, B differs in {} blocks and {} chests over {} chunk runs",
                    probe.name(), seed, rec.nonAir(), cmp.nonAirA, cmp.blockDiffs, cmp.chestDiffs, chunkRuns);
            if (cmp.nonAirA != rec.nonAir()) {
                StringBuilder diff = new StringBuilder();
                int n = 0;
                for (int x = rec.minX - 1; x <= rec.maxX + 1 && n < 6; x++) {
                    for (int z = rec.minZ - 1; z <= rec.maxZ + 1 && n < 6; z++) {
                        for (int y = rec.minY - 1; y <= rec.maxY + 1 && n < 6; y++) {
                            BlockState r = rec.getBlock(x, y, z);
                            BlockState l = level.getBlockState(new BlockPos(x + ax - PROBE, y, z + az - PROBE));
                            if (r.getBlock() != l.getBlock()) {
                                diff.append(" (").append(x).append(',').append(y).append(',').append(z).append(") rec ")
                                        .append(r.getBlock()).append(" live ").append(l.getBlock());
                                n++;
                            }
                        }
                    }
                }
                failures.add(probe.name() + " recorder vs A:" + diff);
                failures.add(probe.name() + ": registered builder placed " + cmp.nonAirA + " blocks, the original method "
                        + rec.nonAir());
            }
            if (cmp.blockDiffs != 0 || cmp.chestDiffs != 0) {
                failures.add(probe.name() + ": chunk runs differ from the unclipped build in " + cmp.blockDiffs
                        + " blocks and " + cmp.chestDiffs + " chests over " + chunkRuns + " chunks, first " + cmp.first);
            }
            discardEntities(level, pa.box());
            discardEntities(level, pb.box());
        }
        report(helper, failures);
    }

    private static Build findBuild(List<Build> builds, String name) {
        return builds.stream().filter(b -> b.name().equals(name)).findFirst().orElseThrow();
    }

    private static final class Compare {
        int nonAirA;
        int blockDiffs;
        int chestDiffs;
        String first = "-";
    }

    /** Block-by-block and chest-by-chest comparison of a window with the same window shifted by (dx, dz). */
    private static Compare compare(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                   int dx, int dz) {
        Compare cmp = new Compare();
        BlockPos.MutableBlockPos a = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos b = new BlockPos.MutableBlockPos();
        minY = Math.max(minY, level.getMinBuildHeight());
        maxY = Math.min(maxY, level.getMaxBuildHeight() - 1);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    a.set(x, y, z);
                    b.set(x + dx, y, z + dz);
                    BlockState sa = level.getBlockState(a);
                    BlockState sb = level.getBlockState(b);
                    if (!sa.isAir()) {
                        cmp.nonAirA++;
                    }
                    if (sa != sb) {
                        if (cmp.blockDiffs < 6) {
                            cmp.first = (cmp.blockDiffs == 0 ? "" : cmp.first + " | ") + "(" + (x - minX) + "," + y + ","
                                    + (z - minZ) + ") " + sa.getBlock() + " vs " + sb.getBlock();
                        }
                        cmp.blockDiffs++;
                        continue;
                    }
                    if (sa.hasBlockEntity()) {
                        BlockEntity ea = level.getBlockEntity(a);
                        BlockEntity eb = level.getBlockEntity(b);
                        if (ea instanceof Container ca && eb instanceof Container cb && !sameContents(ca, cb)) {
                            if (cmp.blockDiffs == 0 && cmp.chestDiffs == 0) {
                                cmp.first = "chest " + a.toShortString();
                            }
                            cmp.chestDiffs++;
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

    private static void discardEntities(ServerLevel level, BoundingBox box) {
        AABB aabb = AABB.of(box).inflate(4.0);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb, e -> !(e instanceof Player))) {
            entity.discard();
        }
    }

    // =====================================================================================================
    // 4. features of this wave

    /**
     * {@code orespawn:ruby_bird_dungeon} at the largest origin offset of its dispatchers (chunk+7) lands completely in
     * the 3x3-chunk window: the feature on the live level equals {@code RubyBirdDungeon.makeDungeon} unclipped with
     * the same random, 310 non-air blocks, chest included. {@code orespawn:legacy_minable} (WorldGenMinable) likewise
     * places the same vein through the feature clip as without one.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void rubyBirdDungeonAndMinableFeaturesPlaceCompletelyInsideTheFeatureWindow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        Random pick = new Random(System.nanoTime());
        ChunkGenerator generator = level.getChunkSource().getGenerator();

        long seed = pick.nextLong();
        ChunkPos[] areas = freshAreas(level, pick, 2, 32, 32);
        int ax = areas[0].getMinBlockX() + 7;
        int az = areas[0].getMinBlockZ() + 7;
        int bx = areas[1].getMinBlockX() + 7;
        int bz = areas[1].getMinBlockZ() + 7;
        RubyBirdDungeon.makeDungeon(LegacyWriter.live(level), new LegacyRandom(RandomSource.create(seed)), ax, 90, az);
        boolean placed = OreSpawnFeatures.RUBY_BIRD_DUNGEON.get().place(new FeaturePlaceContext<>(Optional.empty(), level,
                generator, RandomSource.create(seed), new BlockPos(bx, 90, bz), NoneFeatureConfiguration.INSTANCE));
        if (!placed) {
            failures.add("ruby_bird_dungeon feature reported nothing placed");
        }
        Compare ruby = compare(level, ax - 1, 89, az - 1, ax + 10, 95, az + 10, bx - ax, bz - az);
        expect(failures, "ruby dungeon non-air blocks (unclipped)", 310, ruby.nonAirA);
        if (ruby.blockDiffs != 0 || ruby.chestDiffs != 0) {
            failures.add("ruby_bird_dungeon feature differs from the unclipped build in " + ruby.blockDiffs + " blocks and "
                    + ruby.chestDiffs + " chests, first " + ruby.first);
        }
        if (!level.getBlockState(new BlockPos(bx + 5, 91, bz + 5)).is(Blocks.SPAWNER)) {
            failures.add("ruby_bird_dungeon feature: no spawner at origin + (5, 1, 5)");
        }

        // WorldGenMinable in a stone block, origin at the chunk corner as OreSpawnWorld.generateOres passes it.
        ChunkPos[] stone = freshAreas(level, pick, 2, 32, 32);
        long veinSeed = pick.nextLong();
        int[][] origins = new int[2][];
        for (int i = 0; i < 2; i++) {
            int ox = stone[i].getMinBlockX();
            int oz = stone[i].getMinBlockZ();
            origins[i] = new int[] {ox, oz};
            for (int x = ox - 4; x < ox + 24; x++) {
                for (int z = oz - 4; z < oz + 24; z++) {
                    for (int y = 90; y < 110; y++) {
                        level.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), 2);
                    }
                }
            }
        }
        BlockState ore = ModBlocks.ORERUBY.get().defaultBlockState();
        LegacyMinableFeature.generate(StructureWriter.unclipped(level), RandomSource.create(veinSeed), origins[0][0], 100,
                origins[0][1], ore, 40, LegacyMinableFeature.Target.STONE);
        LegacyMinableFeature.generate(StructureWriter.forFeature(level, stone[1].x, stone[1].z), RandomSource.create(veinSeed),
                origins[1][0], 100, origins[1][1], ore, 40, LegacyMinableFeature.Target.STONE);
        int ores = 0;
        for (int x = origins[0][0] - 4; x < origins[0][0] + 24; x++) {
            for (int z = origins[0][1] - 4; z < origins[0][1] + 24; z++) {
                for (int y = 90; y < 110; y++) {
                    if (level.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.ORERUBY.get())) {
                        ores++;
                    }
                }
            }
        }
        Compare vein = compare(level, origins[0][0] - 4, 90, origins[0][1] - 4, origins[0][0] + 23, 109, origins[0][1] + 23,
                origins[1][0] - origins[0][0], origins[1][1] - origins[0][1]);
        if (ores == 0) {
            failures.add("legacy_minable size 40 placed no ore in solid stone");
        }
        if (vein.blockDiffs != 0) {
            failures.add("legacy_minable through the feature clip differs from unclipped in " + vein.blockDiffs
                    + " blocks, first " + vein.first);
        }
        OreSpawn.LOG.info("W12 feature test: ruby dungeon {} blocks, vein {} ores", ruby.nonAirA, ores);
        report(helper, failures);
    }

    /** Every {@code orespawn:legacy} structure names a registered dispatcher, and the six W12 builders are registered. */
    @GameTest(template = ARENA)
    public static void legacyStructuresNameRegisteredDispatchersAndBuilders(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        Registry<Structure> structures = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        int legacy = 0;
        for (Map.Entry<ResourceKey<Structure>, Structure> e : structures.entrySet()) {
            if (e.getValue() instanceof LegacyStructure structure) {
                legacy++;
                if (LegacyStructureRegistry.dispatcher(structure.dispatcher()).isEmpty()) {
                    failures.add(e.getKey().location() + ": dispatcher " + structure.dispatcher() + " not registered");
                }
            }
        }
        if (legacy < 11) {
            failures.add("only " + legacy + " orespawn:legacy structures loaded");
        }
        for (String id : List.of(TreeBuilders.WIND_TREE, TreeBuilders.SKY_TREE, TreeBuilders.FAIRY_TREE,
                TreeBuilders.FAIRY_CASTLE_TREE, TreeBuilders.BASILISK_MAZE, TreeBuilders.MAGIC_APPLE_TREE,
                OreSpawnWorld.MAKE_DUNGEON, OreSpawnWorld.MAKE_KING_ALTAR)) {
            if (LegacyStructureRegistry.builder(id).isEmpty()) {
                failures.add("builder " + id + " not registered");
            }
        }
        report(helper, failures);
    }

    // =====================================================================================================
    // 5. natural spawns

    /**
     * {@code orespawn:config_spawns}: applied to the unmodified biome (NeoForge's original info), an entry appears
     * exactly when its {@code *Enable} switch is set, in the 1.21.1 biome biome_map.json maps its 1.7.10 biome to,
     * with the original weight and group size and in the category of the 1.7.10 list. Checked with Girlfriend
     * (OreSpawnMain.java:4237 plains, 4235 forest), Cricket surviving {@code AllMobsDisable} (R18), the extremeHills
     * entries in windswept_hills and in the Mining biome that shared the biome object, a W09 type that is skipped
     * without becoming a pig, and a biome the table does not name.
     */
    @GameTest(template = ARENA)
    public static void configSpawnsModifierAddsEntriesOnlyWithTheirEnableFlagInTheMappedBiomes(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        ConfigSpawnsBiomeModifier modifier = new ConfigSpawnsBiomeModifier(rl("spawns"));
        Holder<Biome> plains = biomes.getHolderOrThrow(Biomes.PLAINS);
        Holder<Biome> forest = biomes.getHolderOrThrow(Biomes.FOREST);
        Holder<Biome> hills = biomes.getHolderOrThrow(Biomes.WINDSWEPT_HILLS);
        Holder<Biome> desert = biomes.getHolderOrThrow(Biomes.DESERT);
        Holder<Biome> mining = biomes.getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl("mining")));
        Holder<Biome> islands = biomes.getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl("islands")));
        Holder<Biome> voidBiome = biomes.getHolderOrThrow(Biomes.THE_VOID);

        EntityType<?> girlfriend = ModEntities.GIRLFRIEND.get();
        EntityType<?> cricket = ModEntities.CRICKET.get();
        EntityType<?> butterfly = ModEntities.BUTTERFLY.get();

        ModConfigSpec.IntValue girlfriendEnable = OreSpawnConfig.MOBS.GirlfriendEnable;
        ModConfigSpec.IntValue allMobs = OreSpawnConfig.TWEAKS.AllMobsDisable;
        int savedGirlfriend = girlfriendEnable.get();
        int savedAll = allMobs.get();
        try {
            allMobs.set(0);
            girlfriendEnable.set(1);
            if (!has(added(modifier, plains, MobCategory.CREATURE), girlfriend, 5, 2, 3)) {
                failures.add("GirlfriendEnable=1: plains lacks girlfriend creature 5/2-3 (OreSpawnMain.java:4237)");
            }
            if (!has(added(modifier, forest, MobCategory.CREATURE), girlfriend, 10, 3, 6)) {
                failures.add("GirlfriendEnable=1: forest lacks girlfriend creature 10/3-6 (OreSpawnMain.java:4235)");
            }
            if (count(added(modifier, desert, MobCategory.CREATURE), girlfriend) != 0) {
                failures.add("girlfriend added to desert, which no addSpawn names");
            }
            girlfriendEnable.set(0);
            if (count(added(modifier, plains, MobCategory.CREATURE), girlfriend) != 0) {
                failures.add("GirlfriendEnable=0: girlfriend still added to plains");
            }
            if (!has(added(modifier, plains, MobCategory.AMBIENT), cricket, 3, 4, 8)) {
                failures.add("plains lacks cricket ambient 3/4-8 (OreSpawnMain.java:4614)");
            }
            allMobs.set(1);
            if (!has(added(modifier, plains, MobCategory.AMBIENT), cricket, 3, 4, 8)) {
                failures.add("AllMobsDisable=1 removed the cricket, which disableAllMobs never zeroed (R18)");
            }
            if (count(added(modifier, hills, MobCategory.AMBIENT), butterfly) != 0) {
                failures.add("AllMobsDisable=1: butterfly still added to windswept_hills");
            }
            allMobs.set(0);
            if (!has(added(modifier, hills, MobCategory.AMBIENT), butterfly, 5, 1, 2)) {
                failures.add("windswept_hills (extremeHills) lacks butterfly ambient 5/1-2 (OreSpawnMain.java:4297)");
            }
            if (!has(added(modifier, mining, MobCategory.AMBIENT), butterfly, 5, 1, 2)) {
                failures.add("orespawn:mining (shares extremeHills) lacks butterfly ambient 5/1-2");
            }
            List<MobSpawnSettings.SpawnerData> islandAmbient = added(modifier, islands, MobCategory.AMBIENT);
            if (count(islandAmbient, EntityType.PIG) != count(original(islands, MobCategory.AMBIENT), EntityType.PIG)) {
                failures.add("an OreSpawn entry turned into a pig in orespawn:islands");
            }
            // W09 registered orespawn:dragon: the entry that was skipped until then is live (BiomeGenUtopianPlains.java:84).
            if (OreSpawnConfig.MOBS.DragonEnable.get() != 0
                    && !has(islandAmbient, ModEntities.DRAGON.get(), 1, 1, 2)) {
                failures.add("orespawn:islands lacks dragon ambient 1/1-2 (BiomeGenUtopianPlains.java:84)");
            }
            for (MobCategory category : MobCategory.values()) {
                if (added(modifier, voidBiome, category).size() != original(voidBiome, category).size()) {
                    failures.add("the_void, which the table does not name, got " + category + " entries");
                }
            }
            // REMOVE phase and the others add nothing.
            ModifiableBiomeInfo.BiomeInfo.Builder builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(
                    plains.value().modifiableBiomeInfo().getOriginalBiomeInfo());
            modifier.modify(plains, BiomeModifier.Phase.REMOVE, builder);
            if (builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE).size()
                    != original(plains, MobCategory.CREATURE).size()) {
                failures.add("the modifier added entries outside Phase.ADD");
            }
        } finally {
            girlfriendEnable.set(savedGirlfriend);
            allMobs.set(savedAll);
        }
        // The live biome of this server start carries the entry when the switch was set at start.
        if (savedGirlfriend != 0 && savedAll == 0
                && plains.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap().stream()
                        .noneMatch(d -> d.type == girlfriend)) {
            failures.add("the live plains biome of this server has no girlfriend spawn: config_spawns was not applied at start");
        }
        report(helper, failures);
    }

    private static List<MobSpawnSettings.SpawnerData> original(Holder<Biome> biome, MobCategory category) {
        return new ArrayList<>(biome.value().modifiableBiomeInfo().getOriginalBiomeInfo().mobSpawnSettings()
                .getMobs(category).unwrap());
    }

    private static List<MobSpawnSettings.SpawnerData> added(ConfigSpawnsBiomeModifier modifier, Holder<Biome> biome,
                                                           MobCategory category) {
        ModifiableBiomeInfo.BiomeInfo.Builder builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(
                biome.value().modifiableBiomeInfo().getOriginalBiomeInfo());
        modifier.modify(biome, BiomeModifier.Phase.ADD, builder);
        return new ArrayList<>(builder.getMobSpawnSettings().getSpawner(category));
    }

    private static boolean has(List<MobSpawnSettings.SpawnerData> list, EntityType<?> type, int weight, int min, int max) {
        return list.stream().anyMatch(d -> d.type == type && d.getWeight().asInt() == weight && d.minCount == min
                && d.maxCount == max);
    }

    private static int count(List<MobSpawnSettings.SpawnerData> list, EntityType<?> type) {
        return (int) list.stream().filter(d -> d.type == type).count();
    }

    // =====================================================================================================
    // 6. recipes

    private record Sample(String type, int line, String result, int count, int height, float xp, String[] items) {}

    private static Sample shaped(int line, String result, int count, int height, String... grid) {
        return new Sample("shaped", line, result, count, height, 0.0F, grid);
    }

    private static Sample shapeless(int line, String result, int count, String... items) {
        return new Sample("shapeless", line, result, count, 0, 0.0F, items);
    }

    private static Sample smelting(int line, String result, int count, float xp, String input) {
        return new Sample("smelting", line, result, count, 0, xp, new String[] {input});
    }

    /**
     * 20 registrations of {@code OreSpawnMain.make_some_more_things}, taken from docs/catalog/recipes.json (evenly
     * spread over the ones not gated by {@code neoforge:item_exists}): 8 shaped, 4 shapeless, 4 shapeless
     * rehydrations (water bucket + dried spawn ore), 4 smelting. Grids are the 1.7.10 pattern padded to 3 wide;
     * wildcards and id lists use their first id.
     */
    private static final List<Sample> RECIPE_SAMPLES = List.of(
            shaped(2741, "orespawn:crystalfurnace", 1, 3, "orespawn:crystalstone", "orespawn:crystalstone", "orespawn:crystalstone", "orespawn:crystalstone", "", "orespawn:crystalstone", "orespawn:crystalstone", "orespawn:crystalstone", "orespawn:crystalstone"),
            shaped(2839, "orespawn:rosesword", 1, 3, "", "", "minecraft:poppy", "", "", "minecraft:poppy", "", "", "minecraft:stick"),
            shaped(2867, "orespawn:crystalwoodaxe", 1, 3, "orespawn:crystalplanks", "orespawn:crystalplanks", "", "orespawn:crystalplanks", "orespawn:crystalsticks", "", "", "orespawn:crystalsticks", ""),
            shaped(2904, "orespawn:crystalstonesword", 1, 3, "orespawn:crystalstone", "", "", "orespawn:crystalstone", "", "", "orespawn:crystalsticks", "", ""),
            shaped(2937, "orespawn:amethystshovel", 1, 3, "", "", "orespawn:amethyst", "", "", "minecraft:stick", "", "", "minecraft:stick"),
            shaped(3056, "orespawn:experiencetree_seed", 1, 3, "minecraft:experience_bottle", "minecraft:experience_bottle", "minecraft:experience_bottle", "minecraft:experience_bottle", "orespawn:appletree_seed", "minecraft:experience_bottle", "minecraft:experience_bottle", "minecraft:experience_bottle", "minecraft:experience_bottle"),
            shaped(4696, "orespawn:ruby_helmet", 1, 3, "orespawn:ruby", "orespawn:ruby", "orespawn:ruby", "orespawn:ruby", "", "orespawn:ruby", "", "", ""),
            shaped(4742, "orespawn:lapis_leggings", 1, 3, "minecraft:lapis_block", "minecraft:lapis_block", "minecraft:lapis_block", "minecraft:lapis_block", "", "minecraft:lapis_block", "minecraft:lapis_block", "", "minecraft:lapis_block"),
            shapeless(2545, "orespawn:oregodzilla", 1, "orespawn:oregodzillapart", "orespawn:oregodzillapart", "orespawn:oregodzillapart", "orespawn:oregodzillapart", "orespawn:oregodzillapart", "orespawn:oregodzillapart", "orespawn:oregodzillapart", "orespawn:oregodzillapart", "orespawn:oregodzillapart"),
            shapeless(2953, "orespawn:uranium_nugget", 9, "orespawn:ingoturanium"),
            shapeless(2982, "orespawn:popcorn_buttered_salted", 1, "orespawn:popcorn", "orespawn:salt", "orespawn:butter"),
            shapeless(3010, "orespawn:instantgarden", 1, "minecraft:redstone_block", "minecraft:wheat", "minecraft:gunpowder"),
            shapeless(2326, "minecraft:spider_spawn_egg", 1, "minecraft:water_bucket", "orespawn:orespider"),
            shapeless(2401, "orespawn:eggirongolem", 1, "minecraft:water_bucket", "orespawn:oreirongolem"),
            shapeless(2497, "orespawn:eggbee", 1, "minecraft:water_bucket", "orespawn:orebee"),
            shapeless(2590, "orespawn:eggurchin", 1, "minecraft:water_bucket", "orespawn:oreurchin"),
            smelting(2751, "orespawn:uranium_nugget", 1, 0.3F, "orespawn:oreuranium"),
            smelting(2759, "orespawn:corndog_cooked", 1, 0.4F, "orespawn:corndog_raw"),
            smelting(2767, "orespawn:cookedpeacock", 1, 0.4F, "orespawn:rawpeacock"),
            smelting(2773, "minecraft:cooked_cod", 1, 0.2F, "orespawn:pinkfish"));

    /**
     * The 20 samples through the server's {@code RecipeManager} the way a crafting table and a furnace ask: the
     * recipe found for the 1.7.10 grid yields the 1.7.10 result and count, a smelting recipe also its experience, and
     * a rehydration hands the empty bucket back.
     */
    @GameTest(template = ARENA)
    public static void twentyRecipeSamplesMatchRecipesJson(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        for (Sample s : RECIPE_SAMPLES) {
            String where = "OreSpawnMain.java:" + s.line() + " " + s.type() + " -> " + s.result();
            List<ItemStack> stacks = new ArrayList<>();
            boolean missing = false;
            for (String id : s.items()) {
                if (id.isEmpty()) {
                    stacks.add(ItemStack.EMPTY);
                    continue;
                }
                Optional<Item> item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id));
                if (item.isEmpty() || item.get() == Items.AIR) {
                    failures.add(where + ": ingredient " + id + " not registered");
                    missing = true;
                    break;
                }
                stacks.add(new ItemStack(item.get()));
            }
            if (missing) {
                continue;
            }
            ItemStack out;
            String recipeId;
            if (s.type().equals("smelting")) {
                SingleRecipeInput input = new SingleRecipeInput(stacks.get(0));
                Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
                if (recipe.isEmpty()) {
                    failures.add(where + ": no smelting recipe for " + s.items()[0]);
                    continue;
                }
                out = recipe.get().value().assemble(input, level.registryAccess());
                recipeId = recipe.get().id().toString();
                float xp = ((AbstractCookingRecipe) recipe.get().value()).getExperience();
                if (Math.abs(xp - s.xp()) > 1.0E-4F) {
                    failures.add(where + ": experience " + xp + ", recipes.json " + s.xp());
                }
            } else {
                int height = s.type().equals("shaped") ? s.height() : (stacks.size() + 2) / 3;
                while (stacks.size() < 3 * height) {
                    stacks.add(ItemStack.EMPTY);
                }
                CraftingInput input = CraftingInput.of(3, height, stacks);
                Optional<RecipeHolder<CraftingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
                if (recipe.isEmpty()) {
                    failures.add(where + ": no crafting recipe matches the 1.7.10 grid");
                    continue;
                }
                out = recipe.get().value().assemble(input, level.registryAccess());
                recipeId = recipe.get().id().toString();
                if (List.of(s.items()).contains("minecraft:water_bucket")) {
                    NonNullList<ItemStack> remaining = recipe.get().value().getRemainingItems(input);
                    if (remaining.stream().noneMatch(r -> r.is(Items.BUCKET))) {
                        failures.add(where + ": the water bucket is not handed back as an empty bucket");
                    }
                }
            }
            ResourceLocation got = BuiltInRegistries.ITEM.getKey(out.getItem());
            if (!got.toString().equals(s.result()) || out.getCount() != s.count()) {
                failures.add(where + ": " + recipeId + " gives " + out.getCount() + "x " + got + ", recipes.json "
                        + s.count() + "x " + s.result());
            } else if (!recipeId.startsWith(OreSpawn.MOD_ID + ":")) {
                failures.add(where + ": matched by " + recipeId + " instead of an orespawn recipe");
            }
        }
        report(helper, failures);
    }

    // =====================================================================================================
    // 7. chest loot

    /**
     * The three {@code ChestGenHooks} lists (OreSpawnMain.java:5051-5062) through the Global Loot Modifier: over 400
     * rolls of {@code minecraft:chests/simple_dungeon} an OreSpawn entry shows up in about
     * {@code 1 - (120/128)^8} = 40 % of the chests (accepted 110..215 of 400), and only ruby, amethyst or thunder
     * staff. Both pyramid tables also yield OreSpawn items; a chest table without a modifier yields none.
     */
    @GameTest(template = ARENA)
    public static void vanillaDungeonChestRollsContainOreSpawnLoot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);

        List<String> dungeonItems = List.of("orespawn:ruby", "orespawn:amethyst", "orespawn:thunderstaff");
        Map<String, Integer> seen = new HashMap<>();
        int chestsWithOreSpawn = rolls(level, BuiltInLootTables.SIMPLE_DUNGEON, params, 400, seen);
        for (String id : seen.keySet()) {
            if (!dungeonItems.contains(id)) {
                failures.add("simple_dungeon rolled " + id + ", not one of the WorldGenDungeons entries");
            }
        }
        if (chestsWithOreSpawn < 110 || chestsWithOreSpawn > 215) {
            failures.add("simple_dungeon: " + chestsWithOreSpawn + " of 400 chests with an OreSpawn item, expected about 161");
        }
        int desert = rolls(level, BuiltInLootTables.DESERT_PYRAMID, params, 400, new HashMap<>());
        int jungle = rolls(level, BuiltInLootTables.JUNGLE_TEMPLE, params, 400, new HashMap<>());
        if (desert == 0 || jungle == 0) {
            failures.add("pyramid chests without OreSpawn items over 400 rolls: desert " + desert + ", jungle " + jungle);
        }
        int control = rolls(level, BuiltInLootTables.IGLOO_CHEST, params, 200, new HashMap<>());
        if (control != 0) {
            failures.add("igloo chest, which no OreSpawn list names, rolled OreSpawn items in " + control + " chests");
        }
        OreSpawn.LOG.info("W12 loot test: simple_dungeon {} / 400 {}, desert {}, jungle {}", chestsWithOreSpawn, seen, desert, jungle);
        report(helper, failures);
    }

    private static int rolls(ServerLevel level, ResourceKey<LootTable> key, LootParams params, int n, Map<String, Integer> seen) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        int chests = 0;
        for (int i = 0; i < n; i++) {
            ObjectArrayList<ItemStack> items = table.getRandomItems(params, 7_000_000L + i * 104729L);
            boolean any = false;
            for (ItemStack stack : items) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id.getNamespace().equals(OreSpawn.MOD_ID)) {
                    any = true;
                    seen.merge(id.toString(), 1, Integer::sum);
                }
            }
            if (any) {
                chests++;
            }
        }
        return chests;
    }

    // =====================================================================================================
    // 8. the items

    /**
     * {@code ItemMagicApple.onItemUse} (:794-848) on the grass of a never generated area: the clicked block turns to
     * gold, the apple is used up, and a tree of logs, leaves and stair blocks stands above it.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void magicAppleOnGrassGrowsATree(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        Random pick = new Random(System.nanoTime());
        ChunkPos area = freshAreas(level, pick, 1, 16 * 16, 16 * 16)[0];
        int x = area.getMinBlockX() + 128;
        int z = area.getMinBlockZ() + 128;
        level.getChunk(x >> 4, z >> 4);
        int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        BlockPos clicked = new BlockPos(x, groundY, z);
        if (!level.getBlockState(clicked).is(Blocks.GRASS_BLOCK)) {
            level.setBlock(clicked, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
        }
        int baseline = countNonAir(level, x, groundY + 1, z, 70, 180);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack apple = new ItemStack(ModItems.MAGIC_APPLE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, apple);
        InteractionResult result = apple.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(clicked), Direction.UP, clicked, false)));
        if (!result.consumesAction()) {
            failures.add("magic apple on grass returned " + result);
        }
        if (!level.getBlockState(clicked).is(Blocks.GOLD_BLOCK)) {
            failures.add("clicked grass is " + level.getBlockState(clicked) + ", not gold");
        }
        if (!apple.isEmpty()) {
            failures.add("apple not used up in survival");
        }
        int logs = 0;
        int grown = countNonAir(level, x, groundY + 1, z, 70, 180) - baseline;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -70; dx <= 70; dx++) {
            for (int dz = -70; dz <= 70; dz++) {
                for (int y = groundY + 1; y < groundY + 180; y++) {
                    BlockState state = level.getBlockState(pos.set(x + dx, y, z + dz));
                    if (state.is(net.minecraft.tags.BlockTags.LOGS) || state.is(Blocks.GOLD_BLOCK) || state.is(Blocks.OBSIDIAN)) {
                        logs++;
                    }
                }
            }
        }
        if (grown < 50 || logs < 10) {
            failures.add("magic apple grew " + grown + " blocks with " + logs + " trunk blocks");
        }
        OreSpawn.LOG.info("W12 magic apple: {} blocks, {} trunk blocks at {}", grown, logs, clicked.toShortString());
        discardEntities(level, new BoundingBox(x - 70, groundY, z - 70, x + 70, groundY + 180, z + 70));
        report(helper, failures);
    }

    private static int countNonAir(ServerLevel level, int x, int y0, int z, int radius, int height) {
        int count = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int y = y0; y < Math.min(y0 + height, level.getMaxBuildHeight()); y++) {
                    if (!level.getBlockState(pos.set(x + dx, y, z + dz)).isAir()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * {@code ItemRandomDungeon.onItemUse} (:40-55) puts the Random Dungeon Spawner on dirt at Y >= 40 and schedules its
     * build in 400 ticks; {@code DungeonSpawnerBlock.updateTick} then removes itself and builds one of the fifty
     * structures. One roll must consume the spawner and build: since W13 all fifty cases have a builder.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void randomDungeonPlacesTheSpawnerWhichBuildsAStructure(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> failures = new ArrayList<>();
        Random pick = new Random(System.nanoTime());
        ChunkPos area = freshAreas(level, pick, 1, 16 * 8, 16 * 8)[0];
        int x = area.getMinBlockX() + 64;
        int z = area.getMinBlockZ() + 64;
        BlockPos ground = new BlockPos(x, 100, z);
        level.setBlock(ground, Blocks.DIRT.defaultBlockState(), 2);
        level.setBlock(ground.below(80), Blocks.DIRT.defaultBlockState(), 2);

        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack item = new ItemStack(ModItems.RANDOM_DUNGEON.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, item);
        InteractionResult refused = item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground.below(80)), Direction.UP, ground.below(80), false)));
        if (refused.consumesAction()) {
            failures.add("random dungeon accepted a click at Y " + ground.below(80).getY() + " (< 40 is refused, :45-47)");
        }
        InteractionResult result = item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false)));
        BlockPos spawnerPos = ground.above();
        if (!result.consumesAction() || !level.getBlockState(spawnerPos).is(ModBlocks.DUNGEONSPAWNER.get())) {
            failures.add("random dungeon on dirt at Y 100: " + result + ", block above is " + level.getBlockState(spawnerPos));
            report(helper, failures);
            return;
        }
        if (!item.isEmpty()) {
            failures.add("random dungeon item not used up in survival");
        }
        if (!level.getBlockTicks().hasScheduledTick(spawnerPos, ModBlocks.DUNGEONSPAWNER.get())) {
            failures.add("no scheduled build tick on the spawner (onBlockAdded, 400 ticks)");
        }
        DungeonSpawnerBlock block = ModBlocks.DUNGEONSPAWNER.get();
        int sx = spawnerPos.getX();
        int sy = spawnerPos.getY();
        int sz = spawnerPos.getZ();
        // Since W13 every one of the fifty cases builds (GenericDungeon through GenericDungeonA.buildLive), so one roll
        // must consume the spawner and leave a structure: more non-air blocks around the spawner than the dirt and the
        // spawner itself. The four W12 structures are still recognised by name for the log.
        int before = countNonAir(level, sx, sy - 45, sz, 60, 135);
        block.updateTick(level, sx, sy, sz);
        int after = countNonAir(level, sx, sy - 45, sz, 60, 135);
        String built = "a GenericDungeon structure";
        if (level.getBlockState(new BlockPos(sx + 5, sy + 1, sz + 5)).is(Blocks.SPAWNER)
                && level.getBlockState(new BlockPos(sx + 5, sy + 1, sz + 1)).is(Blocks.CHEST)) {
            built = "ruby bird dungeon";
        } else if (level.getBlockState(new BlockPos(sx - 8, sy, sz - 8)).is(Blocks.SANDSTONE)) {
            built = "basilisk maze";
        } else if (level.getBlockState(new BlockPos(sx, sy + 1, sz)).is(ModBlocks.CRYSTAL_TREE_LOG.get())) {
            built = "fairy tree or fairy castle tree";
        }
        if (level.getBlockState(spawnerPos).is(block)) {
            failures.add("the roll left the spawner in place");
        }
        if (after < before) {
            failures.add("the roll built nothing: " + before + " non-air blocks before, " + after + " after");
        }
        OreSpawn.LOG.info("W12 random dungeon: {}, non-air {} -> {}", built, before, after);
        discardEntities(level, new BoundingBox(sx - 60, sy - 45, sz - 60, sx + 70, sy + 90, sz + 70));
        report(helper, failures);
    }

    // =====================================================================================================
    // helpers

    /**
     * {@code count} areas of {@code spanX} x {@code spanZ} blocks (plus one chunk around) whose chunks were never loaded
     * or saved, at random far chunk coordinates.
     */
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

    private static void expect(List<String> failures, String what, int expected, int actual) {
        if (expected != actual) {
            failures.add(what + ": " + actual + ", expected " + expected);
        }
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
            OreSpawn.LOG.warn("W12 failure detail: {}", message);
        }
        // The GameTest framework writes the message into a lectern book, whose pages are limited.
        helper.assertTrue(failures.isEmpty(), message.length() > 700 ? message.substring(0, 700) + " ... (full text in the log)" : message);
        helper.succeed();
    }
}
