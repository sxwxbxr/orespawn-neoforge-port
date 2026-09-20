package com.swbr.orespawn.world;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobSwitches;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.item.crop.ItemAppleSeed;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.dimension.chaos.WorldProviderOreSpawn6;
import com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5;
import com.swbr.orespawn.world.dimension.danger.WorldProviderOreSpawn4;
import com.swbr.orespawn.world.dimension.mining.WorldProviderOreSpawn2;
import com.swbr.orespawn.world.dimension.utopia.WorldProviderOreSpawn;
import com.swbr.orespawn.world.dimension.village.WorldProviderOreSpawn3;
import com.swbr.orespawn.world.gen.LegacyWorld;
import com.swbr.orespawn.world.gen.OreSpawnWorldOres;
import com.swbr.orespawn.world.maze.RubyBirdDungeon;
import com.swbr.orespawn.world.structure.LegacyBiomeNames;
import com.swbr.orespawn.world.structure.LegacyChain;
import com.swbr.orespawn.world.structure.LegacyStructureContext;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry.Placement;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.structure.SurfaceProbe;
import com.swbr.orespawn.world.tree.LegacyWriter;
import com.swbr.orespawn.world.tree.TreeBuilders;
import com.swbr.orespawn.world.tree.Trees;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Port of {@code danger.orespawn.OreSpawnWorld} (OreSpawnWorld.java:1-2897; verhalten/world-02.md), the only
 * {@code IWorldGenerator} of the mod: per-chunk decoration of the Overworld, the Nether, the End and the six OreSpawn
 * dimensions.
 *
 * <p><b>How the one 1.7.10 method became three 1.21.1 paths</b> (DECISIONS R12):
 * <ol>
 * <li><b>Small decoration</b> - plants, nests, rocks, crystal chests, spawners, lava and water, apple trees, Ruby Bird and
 * Generic dungeons - stays Java code in {@link #generate(WorldGenLevel, int, int)}, writing through a
 * {@link StructureWriter} clipped to the 3x3 chunks the decoration step may touch. It is called
 * <ul>
 * <li>for the Overworld, the Nether and the End by the feature {@code orespawn:orespawn_world} (biome modifiers
 * {@code orespawn:orespawn_world_overworld}, {@code _nether}, {@code _end}); the End has no small decoration
 * ({@code addEndAnts} is empty, :1555-1556), the feature is there for {@code LegacyStructurePass};</li>
 * <li>for the six OreSpawn dimensions by their chunk generators, after the ported 1.7.10 populate and the structure
 * pieces, with the chunk that is being decorated (FML ran the generator after {@code populate}, OreSpawnMain.java:5035).</li>
 * </ul>
 * Both end with {@code LegacyStructurePass}, which writes the structures below over everything else (DECISIONS R24).
 * The random is FML's generator random for the chunk ({@link OreSpawnWorldOres#fmlChunkRandom}); {@code world.rand}
 * becomes a second random seeded from the same chunk (DECISIONS R18).</li>
 * <li><b>Structures</b> - every build that set or read {@code recently_placed}, and every tree too big for the 3x3
 * window (Sky/Wind trees, the magic-apple giants, Fairy trees, the Basilisk maze) - are {@code orespawn:legacy}
 * structures. Their placement rules are the {@code dispatcher} methods below; the structure sets under
 * {@code data/orespawn/worldgen/structure_set/} replace the counter ({@link LegacyChain}).</li>
 * <li><b>Ores</b> of {@code generateOres} and {@code generateNether} are placed features with config-driven placement
 * modifiers ({@code data/orespawn/worldgen/placed_feature/ore_*.json}, {@code nether_*.json}). The Mining ores of
 * :44-64 run through {@link OreSpawnWorldOres#generateMining} at the start of the Mining branch, with the same random.</li>
 * </ol>
 *
 * <p><b>Structure sets</b> ({@code spacing}/{@code separation}, blocked chunks {@code B}, see {@link LegacyChain}):
 * <table>
 * <tr><th>set</th><th>dimension</th><th>original</th><th>spacing / separation</th><th>B</th></tr>
 * <tr><td>{@code overworld_dungeon}</td><td>Overworld</td><td>{@code generateSurface} :269-308</td><td>7 / 2</td><td>50</td></tr>
 * <tr><td>{@code end_dungeon}</td><td>End</td><td>{@code generateEnd} :215-230</td><td>1 / 0</td><td>0</td></tr>
 * <tr><td>{@code utopia_huge_tree}</td><td>Utopia</td><td>{@code addHugeTree} :1902-1957</td><td>1 / 0</td><td>0</td></tr>
 * <tr><td>{@code utopia_other_trees}</td><td>Utopia</td><td>{@code addOtherTrees} :2672-2722</td><td>1 / 0</td><td>0</td></tr>
 * <tr><td>{@code utopia_king_altar}</td><td>Utopia</td><td>{@code addKingAltar} :2724-2752</td><td>10 / 3</td><td>100</td></tr>
 * <tr><td>{@code mining_dungeon}</td><td>Mining</td><td>:65-91</td><td>7 / 2</td><td>50</td></tr>
 * <tr><td>{@code village_dungeon}</td><td>VillageMania</td><td>:108-116</td><td>7 / 2</td><td>50</td></tr>
 * <tr><td>{@code islands_dungeon}</td><td>Islands</td><td>:120-166</td><td>7 / 2</td><td>50</td></tr>
 * <tr><td>{@code islands_cloud_shark}</td><td>Islands</td><td>:167-170</td><td>1 / 0</td><td>0</td></tr>
 * <tr><td>{@code crystal_fairy_tree}</td><td>Crystal</td><td>{@code addFairyTree} :2046-2081</td><td>1 / 0</td><td>0</td></tr>
 * <tr><td>{@code crystal_dungeon}</td><td>Crystal</td><td>:179-184</td><td>7 / 2</td><td>50</td></tr>
 * </table>
 * A set with B = 0 is the original roll per chunk. A cell of 7x7 = 49 chunks stands for the 50 blocked chunks,
 * 10x10 for the altar's 100.
 *
 * <p><b>Dependencies between the paths.</b> Where the original let one call depend on another's result, the
 * structure is decided first (it is, in 1.21.1) and the decoration asks whether that structure started in its chunk
 * ({@link #startedHere}); a structure that depends on another structure re-evaluates that structure's rule, which is
 * deterministic per seed and chunk ({@link #memo}). The cases, each a PORT deviation:
 * <ul>
 * <li>Utopia (:30-35): the huge tree, the Sky/Wind trees and the altar are structures; the apple trees grow only when
 * none of them started in the chunk; the Sky/Wind trees only when no huge tree would place; the altar only when
 * neither tree structure would place. The original also blocked the other trees and the altar with a successful
 * apple tree; a structure cannot know that roll.</li>
 * <li>Mining (:65-91): the Generic dungeon runs unless {@code mining_dungeon} started in the chunk. The original also
 * skipped it when the 1/95 roll passed but the chosen build found no surface.</li>
 * <li>Crystal (:177-185): the Fairy tree rule is evaluated by the termite and Irukandji decoration and by
 * {@code crystal_dungeon} alike, so all three see its original answer, including the {@code true} without a tree
 * when no surface is found. {@code recently_placed == 0} in front of {@code addIrukandji} (:183) is dropped with the
 * counter.</li>
 * </ul>
 *
 * <p><b>Heights.</b> Surface scans in the Overworld start at {@code WORLD_SURFACE_WG} instead of Y 100/110/127/128
 * (DECISIONS R18); lower bounds stay. The Nether, the End and the OreSpawn dimensions keep the literal Y
 * (R21: terrain in 0..256).
 *
 * <p><b>Not ported:</b> {@code recently_placed} itself (DECISIONS R18, replaced as above); {@code playLivingSound} of a
 * rock spawned during generation (no listener, off-thread); the {@code IChunkProvider} parameters.
 */
public final class OreSpawnWorld {

    // -------------------------------------------------------------------------------------------------------
    // structure and builder ids
    // -------------------------------------------------------------------------------------------------------

    public static final String OVERWORLD_DUNGEON = "orespawn:overworld_dungeon";
    public static final String END_DUNGEON = "orespawn:end_dungeon";
    public static final String UTOPIA_HUGE_TREE = "orespawn:utopia_huge_tree";
    public static final String UTOPIA_OTHER_TREES = "orespawn:utopia_other_trees";
    public static final String UTOPIA_KING_ALTAR = "orespawn:utopia_king_altar";
    public static final String MINING_DUNGEON = "orespawn:mining_dungeon";
    public static final String VILLAGE_DUNGEON = "orespawn:village_dungeon";
    public static final String ISLANDS_DUNGEON = "orespawn:islands_dungeon";
    public static final String ISLANDS_CLOUD_SHARK = "orespawn:islands_cloud_shark";
    public static final String CRYSTAL_FAIRY_TREE = "orespawn:crystal_fairy_tree";
    public static final String CRYSTAL_DUNGEON = "orespawn:crystal_dungeon";

    /** Builder ids of {@code GenericDungeon.makeXxx} (W13), in the lower-snake form of the method name. */
    public static final String MAKE_DUNGEON = "orespawn:make_dungeon";
    public static final String MAKE_PLAY_POOL = "orespawn:make_play_pool";
    public static final String MAKE_WATER_DRAGON_LAIR = "orespawn:make_water_dragon_lair";
    public static final String MAKE_GOLD_FISH_BOWL = "orespawn:make_gold_fish_bowl";
    public static final String MAKE_GIRLFRIEND_ISLAND = "orespawn:make_girlfriend_island";
    public static final String MAKE_MONSTER_ISLAND = "orespawn:make_monster_island";
    public static final String MAKE_FROG_POND = "orespawn:make_frog_pond";
    public static final String MAKE_SMALL_BEE_HIVE = "orespawn:make_small_bee_hive";
    public static final String MAKE_MANTIS_HIVE = "orespawn:make_mantis_hive";
    public static final String MAKE_HAUNTED_HOUSE = "orespawn:make_haunted_house";
    public static final String MAKE_LEAF_MONSTER_DUNGEON = "orespawn:make_leaf_monster_dungeon";
    public static final String MAKE_SPIT_BUG_LAIR = "orespawn:make_spit_bug_lair";
    public static final String MAKE_IGLOO = "orespawn:make_igloo";
    public static final String MAKE_BOUNCY_CASTLE = "orespawn:make_bouncy_castle";
    public static final String MAKE_RUBBER_DUCKY_POND = "orespawn:make_rubber_ducky_pond";
    public static final String MAKE_ENDER_KNIGHT_DUNGEON = "orespawn:make_ender_knight_dungeon";
    public static final String MAKE_ENDER_REAPER_GRAVEYARD = "orespawn:make_ender_reaper_graveyard";
    public static final String MAKE_ENDER_DRAGON_HOSPITAL = "orespawn:make_ender_dragon_hospital";
    public static final String MAKE_ENDER_CASTLE = "orespawn:make_ender_castle";
    /** Variant 0 {@code makeKingAltar}, 1 {@code makeQueenAltar}. */
    public static final String MAKE_KING_ALTAR = "orespawn:make_king_altar";
    public static final String MAKE_KYUUBI_DUNGEON = "orespawn:make_kyuubi_dungeon";
    public static final String MAKE_BEE_HIVE = "orespawn:make_bee_hive";
    public static final String MAKE_SHADOW_DUNGEON = "orespawn:make_shadow_dungeon";
    public static final String MAKE_ALIEN_WTF_DUNGEON = "orespawn:make_alien_wtf_dungeon";
    public static final String MAKE_LEON_NEST = "orespawn:make_leon_nest";
    public static final String MAKE_DAMSEL_IN_DISTRESS = "orespawn:make_damsel_in_distress";
    public static final String MAKE_SPIDER_HANGOUT = "orespawn:make_spider_hangout";
    public static final String MAKE_RED_ANT_HANGOUT = "orespawn:make_red_ant_hangout";
    /** Variant 1 {@code makeEnormousCastle}, 0 {@code makeEnormousCastleQ} (the {@code nextInt(2) == 1} of :2343). */
    public static final String MAKE_ENORMOUS_CASTLE = "orespawn:make_enormous_castle";
    public static final String MAKE_INCA_PYRAMID = "orespawn:make_inca_pyramid";
    public static final String MAKE_ROBOT_LAB = "orespawn:make_robot_lab";
    public static final String MAKE_MINI_DUNGEON = "orespawn:make_mini_dungeon";
    public static final String MAKE_CEPHADROME_ALTAR = "orespawn:make_cephadrome_altar";
    public static final String MAKE_GREENHOUSE_DUNGEON = "orespawn:make_greenhouse_dungeon";
    public static final String MAKE_NIGHTMARE_ROOKERY = "orespawn:make_nightmare_rookery";
    public static final String MAKE_STINKY_HOUSE = "orespawn:make_stinky_house";
    public static final String MAKE_WHITE_HOUSE = "orespawn:make_white_house";
    public static final String MAKE_PUMPKIN = "orespawn:make_pumpkin";
    public static final String MAKE_RAINBOW = "orespawn:make_rainbow";
    public static final String MAKE_CLOUD_SHARK_DUNGEON = "orespawn:make_cloud_shark_dungeon";
    public static final String MAKE_ROTATOR_STATION = "orespawn:make_rotator_station";
    public static final String MAKE_URCHIN_SPAWNER = "orespawn:make_urchin_spawner";
    public static final String MAKE_CRYSTAL_HAUNTED_HOUSE = "orespawn:make_crystal_haunted_house";
    public static final String MAKE_ROUND_ROTATOR = "orespawn:make_round_rotator";
    public static final String MAKE_CRYSTAL_BATTLE_TOWER = "orespawn:make_crystal_battle_tower";

    /**
     * {@code RubyBirdDungeon.makeDungeon} as a structure builder (Islands, {@code addD4RubyDungeon}). The tree, maze and
     * Magic Apple builders and their boxes are {@link TreeBuilders} (w12-trees-mazes).
     */
    public static final String RUBY_BIRD_DUNGEON = "orespawn:ruby_bird_dungeon";

    /** {@code recently_placed = 50} (and a 7x7 cell). */
    private static final int BLOCK_50 = 50;
    private static final int CELL_50 = 7 * 7;
    /** {@code recently_placed = 100} of the altar (and a 10x10 cell). */
    private static final int BLOCK_100 = 100;
    private static final int CELL_100 = 10 * 10;
    /** Sets that never read the counter: one chunk per cell, nothing blocked. */
    private static final int CELL_1 = 1;

    /** Salt of the {@code world.rand} stand-in; any constant, it only separates the two sequences. */
    private static final long WORLD_RAND_SALT = 0x4F726553706177L;

    // -------------------------------------------------------------------------------------------------------
    // dimensions
    // -------------------------------------------------------------------------------------------------------

    /** {@code world.provider.dimensionId} of the branches of {@code generate}. */
    public enum Dim {
        OVERWORLD(true), NETHER(false), END(false), UTOPIA(false), MINING(false), VILLAGE(false), ISLANDS(false),
        CRYSTAL(false), CHAOS(false);

        /** Surface scans start at the heightmap (DECISIONS R18) instead of the literal Y (R21). */
        final boolean heightmap;

        Dim(final boolean heightmap) {
            this.heightmap = heightmap;
        }

        @Nullable
        public static Dim of(final ResourceKey<Level> key) {
            if (key.equals(Level.OVERWORLD)) {
                return OVERWORLD;
            }
            if (key.equals(Level.NETHER)) {
                return NETHER;
            }
            if (key.equals(Level.END)) {
                return END;
            }
            if (key.equals(WorldProviderOreSpawn.DIMENSION)) {
                return UTOPIA;
            }
            if (key.equals(WorldProviderOreSpawn2.DIMENSION)) {
                return MINING;
            }
            if (key.equals(WorldProviderOreSpawn3.DIMENSION)) {
                return VILLAGE;
            }
            if (key.equals(WorldProviderOreSpawn4.DIMENSION)) {
                return ISLANDS;
            }
            if (key.equals(WorldProviderOreSpawn5.DIMENSION)) {
                return CRYSTAL;
            }
            if (key.equals(WorldProviderOreSpawn6.DIMENSION)) {
                return CHAOS;
            }
            return null;
        }
    }

    private final WorldGenLevel level;
    private final StructureWriter world;
    private final Dim dim;
    /** {@code world.rand}. */
    private final Random worldRand;

    private OreSpawnWorld(final WorldGenLevel level, final StructureWriter world, final Dim dim, final Random worldRand) {
        this.level = level;
        this.world = world;
        this.dim = dim;
        this.worldRand = worldRand;
    }

    // -------------------------------------------------------------------------------------------------------
    // entry points
    // -------------------------------------------------------------------------------------------------------

    /**
     * {@code generate(random, chunkX, chunkZ, world, ...)} (:20-213) for the chunk {@code (chunkX, chunkZ)} being
     * decorated. Call from a feature placed at the chunk corner or from {@code ChunkGenerator.applyBiomeDecoration}.
     * Does nothing in a dimension the original did not know.
     *
     * @param chunkX chunk index, not block coordinate
     */
    public static void generate(final WorldGenLevel level, final int chunkX, final int chunkZ) {
        final Dim dim = Dim.of(level.getLevel().dimension());
        if (dim == null) {
            return;
        }
        // :21-23 world.isRemote: a generation region is always server side.
        final Random random = OreSpawnWorldOres.fmlChunkRandom(level.getSeed(), chunkX, chunkZ);
        final Random worldRand = new Random(
                OreSpawnWorldOres.fmlChunkRandom(level.getSeed(), chunkX, chunkZ).nextLong() ^ WORLD_RAND_SALT);
        final StructureWriter writer = StructureWriter.forFeature(level, chunkX, chunkZ);
        new OreSpawnWorld(level, writer, dim, worldRand).generate(random, chunkX, chunkZ);
    }

    /** Registers the dispatchers and builders of this class; reached from {@code OreSpawnStructures.init()}. */
    public static void bootstrap() {
        LegacyStructureRegistry.registerDispatcher(OVERWORLD_DUNGEON, OreSpawnWorld::overworldDungeon);
        LegacyStructureRegistry.registerDispatcher(END_DUNGEON, OreSpawnWorld::endDungeon);
        LegacyStructureRegistry.registerDispatcher(UTOPIA_HUGE_TREE, c -> hugeTree(c).map(List::of).orElse(List.of()));
        LegacyStructureRegistry.registerDispatcher(UTOPIA_OTHER_TREES, OreSpawnWorld::otherTrees);
        LegacyStructureRegistry.registerDispatcher(UTOPIA_KING_ALTAR, OreSpawnWorld::kingAltar);
        LegacyStructureRegistry.registerDispatcher(MINING_DUNGEON, OreSpawnWorld::miningDungeon);
        LegacyStructureRegistry.registerDispatcher(VILLAGE_DUNGEON, OreSpawnWorld::villageDungeon);
        LegacyStructureRegistry.registerDispatcher(ISLANDS_DUNGEON, OreSpawnWorld::islandsDungeon);
        LegacyStructureRegistry.registerDispatcher(ISLANDS_CLOUD_SHARK, OreSpawnWorld::islandsCloudShark);
        LegacyStructureRegistry.registerDispatcher(CRYSTAL_FAIRY_TREE, c -> fairyTree(c).placement().map(List::of).orElse(List.of()));
        LegacyStructureRegistry.registerDispatcher(CRYSTAL_DUNGEON, OreSpawnWorld::crystalDungeon);
        registerBuilders();
    }

    // -------------------------------------------------------------------------------------------------------
    // generate (:20-213)
    // -------------------------------------------------------------------------------------------------------

    private void generate(final Random random, final int chunkX, final int chunkZ) {
        final int bx = chunkX * 16;
        final int bz = chunkZ * 16;
        // :25-27 --recently_placed: replaced by the structure sets (class javadoc).
        switch (this.dim) {
            case UTOPIA -> {
                // :28-41
                this.generateSurface(random, bx, bz);
                // addHugeTree (:30) is the structure orespawn:utopia_huge_tree.
                if (!this.startedHere(UTOPIA_HUGE_TREE, chunkX, chunkZ)) {
                    // PORT: addAppleTrees ran first and blocked the other trees and the altar (:31); those are
                    // structures now and decided earlier, so the apple trees give way to them instead.
                    if (!this.startedHere(UTOPIA_OTHER_TREES, chunkX, chunkZ)
                            && !this.startedHere(UTOPIA_KING_ALTAR, chunkX, chunkZ)) {
                        this.addAppleTrees(random, bx, bz);
                    }
                    this.addVeggies(random, bx, bz);
                }
                boolean rbd = false;
                rbd = this.addRubyDungeon(random, bx, bz);
                if (!rbd) {
                    this.addGenericDungeon(random, bx, bz);
                }
            }
            case MINING -> {
                // :44-64 through the shared port (W05), same random, before everything else.
                OreSpawnWorldOres.generateMining(new LegacyWorld(this.level, Blocks.GRASS_BLOCK.defaultBlockState()),
                        random, chunkX, chunkZ);
                // :65-91: the 1/95 roll and its seven builds are orespawn:mining_dungeon.
                if (!this.startedHere(MINING_DUNGEON, chunkX, chunkZ)) {
                    this.addGenericDungeon(random, bx, bz);
                }
                this.addLavaAndWater(random, bx, bz);
                this.addAnts(random, bx, bz, 2);
                this.addAnts(random, bx, bz, 2);
                this.addMosquitos(random, bx, bz);
                this.addMosquitos(random, bx, bz);
                this.addVeggies(random, bx, bz);
                this.addRocks(random, bx, bz);
            }
            case VILLAGE -> {
                // :101-117
                if (MobSwitches.enabled(OreSpawnConfig.MOBS.MosquitoEnable)) {
                    // Original bug kept (DECISIONS R18): chunk indices instead of block coordinates (:103). The plants
                    // would land near the world origin; the writer drops what lies outside the decorated window, so
                    // only the chunks at the origin ever get one - the random numbers are drawn as before.
                    this.addMosquitos(random, chunkX, chunkZ);
                }
                this.addAnts(random, bx, bz, 4);
                this.addAppleTrees(random, bx, bz);
                this.addGenericDungeon(random, bx, bz);
                // :108-116 addDamselInDistress, addSpiderHangout, addRedAntHangout: orespawn:village_dungeon.
            }
            case ISLANDS -> {
                // :120-170 the D4 builds and the Cloud Shark are orespawn:islands_dungeon and _cloud_shark.
                this.addUnstableAnts(random, bx, bz);
                this.addIslands(random, bx, bz);
                this.addD4Rocks(random, bx, bz);
            }
            case CRYSTAL -> {
                // :176-190
                if (!this.fairyTreeClaims(chunkX, chunkZ)) {
                    this.addCrystalTermites(random, bx, bz);
                    // :179-182 the chain of five builds is orespawn:crystal_dungeon.
                    this.addIrukandji(random, bx, bz);
                }
                this.addCrystalChestsAndSpawners(random, bx, bz);
                if (this.worldRand.nextInt(4) == 1) {
                    this.addRocks(random, bx, bz);
                }
            }
            case CHAOS -> {
                // :192-197
                this.addButterfliesAndMoths(random, bx, bz);
                this.addVeggies(random, bx, bz);
                this.addAnts(random, bx, bz, 2);
            }
            case NETHER -> this.generateNether(random, bx, bz);
            case OVERWORLD -> {
                this.generateSurface(random, bx, bz);
                // generateOres (:205, :339-961): placed features orespawn:ore_*.
            }
            case END -> this.generateEnd(random, bx, bz);
        }
    }

    /** {@code generateEnd} (:215-230): {@code addEndAnts} is empty; the four builds are orespawn:end_dungeon. */
    private void generateEnd(final Random random, final int chunkX, final int chunkZ) {
        this.addEndAnts(random, chunkX, chunkZ);
    }

    /**
     * {@code generateNether} (:232-257) without the veins: {@code lavafoam} and {@code oreruby} are the placed features
     * {@code orespawn:nether_lavafoam} and {@code orespawn:nether_ruby}.
     */
    private void generateNether(final Random random, final int chunkX, final int chunkZ) {
        if (MobSwitches.enabled(OreSpawnConfig.MOBS.MosquitoEnable)) {
            this.addNetherMosquitos(random, chunkX, chunkZ);
        }
        this.addNetherAnts(random, chunkX, chunkZ);
    }

    /** {@code generateSurface} (:259-314); the dungeon part (:269-308) is orespawn:overworld_dungeon. */
    private void generateSurface(final Random random, final int chunkX, final int chunkZ) {
        this.addStrawberries(random, chunkX, chunkZ);
        this.addCorn(random, chunkX, chunkZ);
        this.addTomatoes(random, chunkX, chunkZ);
        this.addVeggies(random, chunkX, chunkZ);
        this.addButterfliesAndMoths(random, chunkX, chunkZ);
        if (MobSwitches.enabled(OreSpawnConfig.MOBS.MosquitoEnable)) {
            this.addMosquitos(random, chunkX, chunkZ);
        }
        this.addAnts(random, chunkX, chunkZ, 4);
        if (this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.RIVER)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.EXTREME_HILLS)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.DESERT)) {
            this.addRocks(random, chunkX, chunkZ);
        }
    }

    // -------------------------------------------------------------------------------------------------------
    // small decoration
    // -------------------------------------------------------------------------------------------------------

    /** Start of a downward surface scan: {@code WORLD_SURFACE_WG} in the Overworld (R18), the literal Y elsewhere (R21). */
    private int scanStart(final int posX, final int posZ, final int literal) {
        if (!this.dim.heightmap || !this.world.canRead(posX, posZ)) {
            return literal;
        }
        return this.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, posX, posZ);
    }

    private boolean isGrass(final int x, final int y, final int z) {
        return this.world.is(x, y, z, Blocks.GRASS_BLOCK);
    }

    /** {@code addStrawberries} (:963-978). */
    private void addStrawberries(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(20) != 0) {
            return;
        }
        if (this.dim == Dim.UTOPIA || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.FOREST)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.BIRCH_FOREST)) {
            for (int i = 0; i < 5; ++i) {
                final int posX = chunkX + random.nextInt(16);
                final int posZ = chunkZ + random.nextInt(16);
                for (int posY = this.scanStart(posX, posZ, 100); posY > 40 && this.world.isAirBlock(posX, posY, posZ); --posY) {
                    if (this.isGrass(posX, posY - 1, posZ)) {
                        this.world.setBlock(posX, posY, posZ, ModBlocks.STRAWBERRY_PLANT.get().defaultBlockState());
                        break;
                    }
                }
            }
        }
    }

    /** {@code addCorn} (:1023-1076). {@code DimensionID3} is dead there: VillageMania never calls generateSurface. */
    private void addCorn(final Random random, final int chunkX, final int chunkZ) {
        int is_all_air = 1;
        int nc = 6;
        if (random.nextInt(35) != 1) {
            return;
        }
        if (TweakStats.LessLag() == 1) {
            nc = 5;
        }
        if (TweakStats.LessLag() == 2) {
            nc = 3;
        }
        if (this.dim == Dim.UTOPIA || this.dim == Dim.VILLAGE || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.PLAINS)) {
            for (int j = 0; j < nc; ++j) {
                final int posX = chunkX + random.nextInt(16);
                final int posZ = chunkZ + random.nextInt(16);
                is_all_air = 1;
                int posY = this.scanStart(posX, posZ, 100);
                while (posY > 40 && this.world.isAirBlock(posX, posY, posZ)) {
                    if (this.isGrass(posX, posY - 1, posZ)) {
                        for (int i = 1; i < 10; ++i) {
                            if (!this.world.isAirBlock(posX, posY + i, posZ)) {
                                is_all_air = 0;
                            }
                        }
                        if (is_all_air == 0) {
                            break;
                        }
                        int corn_height = random.nextInt(5);
                        if (++corn_height == 1) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.CORN_0.get().defaultBlockState());
                        }
                        if (corn_height == 2) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.CORN_1.get().defaultBlockState());
                            this.world.setBlock(posX, posY + 1, posZ, ModBlocks.CORN_0.get().defaultBlockState());
                        }
                        if (corn_height > 2) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.CORN_1.get().defaultBlockState());
                            for (int i = 1; i < corn_height; ++i) {
                                this.world.setBlock(posX, posY + i, posZ, ModBlocks.CORN_3.get().defaultBlockState());
                            }
                            this.world.setBlock(posX, posY + corn_height, posZ, ModBlocks.CORN_0.get().defaultBlockState());
                            break;
                        }
                        break;
                    } else {
                        --posY;
                    }
                }
            }
        }
    }

    /** {@code addTomatoes} (:1078-1124). */
    private void addTomatoes(final Random random, final int chunkX, final int chunkZ) {
        int is_all_air = 1;
        if (random.nextInt(70) != 1) {
            return;
        }
        if (this.dim == Dim.UTOPIA || this.dim == Dim.VILLAGE || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.PLAINS)) {
            for (int j = 0; j < 5; ++j) {
                final int posX = chunkX + random.nextInt(16);
                final int posZ = chunkZ + random.nextInt(16);
                is_all_air = 1;
                int posY = this.scanStart(posX, posZ, 100);
                while (posY > 40 && this.world.isAirBlock(posX, posY, posZ)) {
                    if (this.isGrass(posX, posY - 1, posZ)) {
                        for (int i = 1; i < 10; ++i) {
                            if (!this.world.isAirBlock(posX, posY + i, posZ)) {
                                is_all_air = 0;
                            }
                        }
                        if (is_all_air == 0) {
                            break;
                        }
                        int corn_height = random.nextInt(3);
                        if (++corn_height == 1) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.TOMATO_0.get().defaultBlockState());
                        }
                        if (corn_height == 2) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.TOMATO_1.get().defaultBlockState());
                            this.world.setBlock(posX, posY + 1, posZ, ModBlocks.TOMATO_0.get().defaultBlockState());
                        }
                        if (corn_height > 2) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.TOMATO_2.get().defaultBlockState());
                            for (int i = 1; i < corn_height; ++i) {
                                this.world.setBlock(posX, posY + i, posZ, ModBlocks.TOMATO_3.get().defaultBlockState());
                            }
                            this.world.setBlock(posX, posY + corn_height, posZ, ModBlocks.TOMATO_0.get().defaultBlockState());
                            break;
                        }
                        break;
                    } else {
                        --posY;
                    }
                }
            }
        }
    }

    /** {@code addButterfliesAndMoths} (:1126-1157). */
    private void addButterfliesAndMoths(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(10 + TweakStats.LessLag() * 2) != 0) {
            return;
        }
        if (this.dim == Dim.UTOPIA || this.dim == Dim.CHAOS
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.FOREST)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.RIVER)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.JUNGLE)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.SWAMPLAND)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.BIRCH_FOREST)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.ROOFED_FOREST)) {
            for (int i = 0; i < 4; ++i) {
                final int posX = chunkX + random.nextInt(16);
                final int posZ = chunkZ + random.nextInt(16);
                int which = 0;
                int posY = this.scanStart(posX, posZ, 100);
                while (posY > 40 && this.world.isAirBlock(posX, posY, posZ)) {
                    if (this.isGrass(posX, posY - 1, posZ)) {
                        which = random.nextInt(3);
                        if (which == 0) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.BUTTERFLY_PLANT.get().defaultBlockState());
                            break;
                        }
                        if (which == 1) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.MOTH_PLANT.get().defaultBlockState());
                            break;
                        }
                        this.world.setBlock(posX, posY, posZ, ModBlocks.FIREFLY_PLANT.get().defaultBlockState());
                        break;
                    } else {
                        --posY;
                    }
                }
            }
        }
    }

    /** {@code addMosquitos} (:1455-1473). */
    private void addMosquitos(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(25 + TweakStats.LessLag() * 2) != 0) {
            return;
        }
        if ((this.dim == Dim.UTOPIA || this.dim == Dim.VILLAGE) && random.nextInt(3) != 0) {
            return;
        }
        if (this.dim == Dim.UTOPIA || this.dim == Dim.MINING || this.dim == Dim.VILLAGE
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.JUNGLE)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.SWAMPLAND)) {
            for (int i = 0; i < 2; ++i) {
                final int posX = chunkX + random.nextInt(16);
                final int posZ = chunkZ + random.nextInt(16);
                for (int posY = this.scanStart(posX, posZ, 100); posY > 40 && this.world.isAirBlock(posX, posY, posZ); --posY) {
                    if (this.isGrass(posX, posY - 1, posZ)) {
                        this.world.setBlock(posX, posY, posZ, ModBlocks.MOSQUITO_PLANT.get().defaultBlockState());
                        break;
                    }
                }
            }
        }
    }

    /**
     * {@code addNetherMosquitos} (:1475-1489). PORT: {@code Blocks.netherrack} is the category
     * {@code #minecraft:base_stone_nether} (DECISIONS R22), as for the Nether veins.
     */
    private void addNetherMosquitos(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(25) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 20; --posY) {
                if (this.world.isAirBlock(posX, posY, posZ) && this.isNetherrack(posX, posY - 1, posZ)) {
                    this.world.setBlock(posX, posY, posZ, ModBlocks.MOSQUITO_PLANT.get().defaultBlockState());
                    break;
                }
            }
        }
    }

    /** {@code addNetherAnts} (:1491-1508); netherrack as in {@link #addNetherMosquitos}. */
    private void addNetherAnts(final Random random, final int chunkX, final int chunkZ) {
        if (!MobSwitches.enabled(OreSpawnConfig.MOBS.RedAntEnable)) {
            return;
        }
        if (random.nextInt(25) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 90; posY > 20; --posY) {
                if (this.world.isAirBlock(posX, posY, posZ) && this.isNetherrack(posX, posY - 1, posZ)) {
                    this.world.setBlock(posX, posY - 1, posZ, ModBlocks.REDANTBLOCK.get().defaultBlockState());
                    break;
                }
            }
        }
    }

    private boolean isNetherrack(final int x, final int y, final int z) {
        return this.world.getBlock(x, y, z).is(BlockTags.BASE_STONE_NETHER);
    }

    /** {@code addAnts} (:1510-1553). */
    private void addAnts(final Random random, final int chunkX, final int chunkZ, int redfreq) {
        final boolean red = MobSwitches.enabled(OreSpawnConfig.MOBS.RedAntEnable);
        final boolean black = MobSwitches.enabled(OreSpawnConfig.MOBS.BlackAntEnable);
        final boolean rainbow = MobSwitches.enabled(OreSpawnConfig.MOBS.RainbowedAntEnable);
        final boolean unstable = MobSwitches.enabled(OreSpawnConfig.MOBS.UnstableAntEnable);
        if (!red && !black && !rainbow && !unstable) {
            return;
        }
        if (redfreq < 2) {
            redfreq = 2;
        }
        if (random.nextInt(30 + TweakStats.LessLag() * 4) != 0) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            int posY = this.scanStart(posX, posZ, 100);
            while (posY > 40 && this.world.isAirBlock(posX, posY, posZ)) {
                if (this.isGrass(posX, posY - 1, posZ)) {
                    if (random.nextInt(redfreq) == 0) {
                        final int which = random.nextInt(4);
                        if (which == 0 && red) {
                            this.world.setBlock(posX, posY - 1, posZ, ModBlocks.REDANTBLOCK.get().defaultBlockState());
                        }
                        if (which == 1 && rainbow) {
                            this.world.setBlock(posX, posY - 1, posZ, ModBlocks.RAINBOWANTBLOCK.get().defaultBlockState());
                        }
                        if (which == 2 && unstable) {
                            this.world.setBlock(posX, posY - 1, posZ, ModBlocks.UNSTABLEANTBLOCK.get().defaultBlockState());
                        }
                        if (which == 3 && MobSwitches.enabled(OreSpawnConfig.MOBS.TermiteEnable)) {
                            this.world.setBlock(posX, posY - 1, posZ, ModBlocks.TERMITEBLOCK.get().defaultBlockState());
                        }
                        break;
                    }
                    if (black) {
                        this.world.setBlock(posX, posY - 1, posZ, ModBlocks.ANTBLOCK.get().defaultBlockState());
                        break;
                    }
                    break;
                } else {
                    --posY;
                }
            }
        }
    }

    /** {@code addEndAnts} (:1555-1556): empty in the original. */
    private void addEndAnts(final Random random, final int chunkX, final int chunkZ) {
    }

    /** {@code addUnstableAnts} (:1622-1637). */
    private void addUnstableAnts(final Random random, final int chunkX, final int chunkZ) {
        if (!MobSwitches.enabled(OreSpawnConfig.MOBS.UnstableAntEnable)) {
            return;
        }
        if (random.nextInt(30) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 20; posY > 2 && this.world.isAirBlock(posX, posY, posZ); --posY) {
                if (this.isGrass(posX, posY - 1, posZ)) {
                    this.world.setBlock(posX, posY - 1, posZ, ModBlocks.UNSTABLEANTBLOCK.get().defaultBlockState());
                    break;
                }
            }
        }
    }

    /** {@code addCrystalTermites} (:1639-1656). */
    private void addCrystalTermites(final Random random, final int chunkX, final int chunkZ) {
        if (!MobSwitches.enabled(OreSpawnConfig.MOBS.TermiteEnable)) {
            return;
        }
        if (random.nextInt(40) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (this.world.isAirBlock(posX, posY, posZ) && this.world.is(posX, posY - 1, posZ, ModBlocks.CRYSTAL_GRASS.get())) {
                    this.world.setBlock(posX, posY - 1, posZ, ModBlocks.CRYSTALTERMITEBLOCK.get().defaultBlockState());
                    break;
                }
            }
        }
    }

    /** {@code addIrukandji} (:1757-1778). */
    private void addIrukandji(final Random random, final int chunkX, final int chunkZ) {
        if (!MobSwitches.enabled(OreSpawnConfig.MOBS.IrukandjiEnable)) {
            return;
        }
        if (random.nextInt(80) != 0) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (this.world.isAirBlock(posX, posY, posZ) && this.world.is(posX, posY - 1, posZ, Blocks.WATER)) {
                    // setEntityName("Irukandji")
                    this.world.setSpawner(posX, posY, posZ, "orespawn:irukandji");
                    return;
                }
            }
        }
    }

    /**
     * {@code addCrystalChestsAndSpawners} (:1780-1818). Up to three probes at Y 25 (the crystal maze level); the first
     * air probe decides. {@code Blocks.air} comparisons are {@code isAir} (1.21.1 cave air is air too).
     */
    private void addCrystalChestsAndSpawners(final Random random, final int chunkX, final int chunkZ) {
        int i = 0;
        while (i < 3) {
            final int posX = 1 + chunkX + random.nextInt(14);
            final int posZ = 1 + chunkZ + random.nextInt(14);
            final int posY = 25;
            if (this.world.isAirBlock(posX, posY, posZ)) {
                if (this.world.isAirBlock(posX + 1, posY, posZ)) {
                    this.addCrystalChest(posX, posY, posZ, 5);
                    break;
                }
                if (this.world.isAirBlock(posX - 1, posY, posZ)) {
                    this.addCrystalChest(posX, posY, posZ, 4);
                    break;
                }
                if (this.world.isAirBlock(posX, posY, posZ + 1)) {
                    this.addCrystalChest(posX, posY, posZ, 2);
                    break;
                }
                if (this.world.isAirBlock(posX, posY, posZ - 1)) {
                    this.addCrystalChest(posX, posY, posZ, 3);
                    break;
                }
                break;
            } else {
                ++i;
            }
        }
    }

    /** {@code addCrystalChest} (:1820-1845): 1/3 a chest (1-3 draws of the crystal list), else a spawner. */
    private void addCrystalChest(final int x, final int y, final int z, final int dir) {
        final int i = this.worldRand.nextInt(3);
        if (i == 0) {
            // setBlockFast(chest, 0, 2) + setBlockMetadataWithNotify(dir, 3)
            if (this.world.setChest(x, y, z, dir, null, 0, this.worldRand)) {
                final int draws = 1 + this.worldRand.nextInt(3);
                this.world.fillChest(x, y, z, Trees.CrystalChestContentsList, draws, this.worldRand);
            }
        } else {
            // setBlockFast(mob_spawner, 0, 2); the name is rolled only when the tile entity exists
            if (this.world.contains(x, y, z)) {
                final int t = this.worldRand.nextInt(2);
                if (t == 0) {
                    this.world.setSpawner(x, y, z, "orespawn:dungeon_beast");
                }
                if (t == 1) {
                    this.world.setSpawner(x, y, z, "orespawn:rat");
                }
            }
        }
    }

    /** {@code addIslands} (:1847-1859). */
    private void addIslands(final Random random, final int chunkX, final int chunkZ) {
        final int posX = 2 + chunkX + random.nextInt(12);
        final int posZ = 2 + chunkZ + random.nextInt(12);
        if (random.nextInt(10 + TweakStats.LessLag() * 2) != 1) {
            return;
        }
        for (int posY = 20; posY > 2 && this.world.isAirBlock(posX, posY, posZ); --posY) {
            if (this.isGrass(posX, posY - 1, posZ)) {
                this.world.setBlock(posX, posY, posZ, ModBlocks.ISLAND.get().defaultBlockState());
                break;
            }
        }
    }

    /**
     * {@code addAppleTrees} (:1861-1900). {@code ItemAppleSeed.makeTree} (W02) writes at most 6 blocks around
     * {@code 2 + nextInt(12)}, inside the window; the original's chunk reference is not needed for that.
     */
    private boolean addAppleTrees(final Random random, final int chunkX, final int chunkZ) {
        int freq = Math.abs(chunkX / 16) + Math.abs(chunkZ / 16);
        int howmany = 2;
        int which = 0;
        boolean added = false;
        freq %= 15;
        howmany += random.nextInt(2 + (15 - freq) / 2);
        which = random.nextInt(10);
        if (random.nextInt(15 + freq) != 0) {
            return false;
        }
        if (TweakStats.LessLag() == 1) {
            howmany /= 2;
        }
        if (TweakStats.LessLag() == 2) {
            howmany /= 4;
            if (howmany < 1) {
                return false;
            }
        }
        for (int i = 0; i < howmany; ++i) {
            final int posX = 2 + chunkX + random.nextInt(12);
            final int posZ = 2 + chunkZ + random.nextInt(12);
            for (int posY = this.scanStart(posX, posZ, 100); posY > 50 && this.world.isAirBlock(posX, posY, posZ); --posY) {
                if (this.isGrass(posX, posY - 1, posZ)) {
                    if (which < 8) {
                        ItemAppleSeed.makeTree(this.level, posX, posY - 1, posZ, ModBlocks.LEAVES_APPLE.get(), null);
                    }
                    if (which == 8) {
                        ItemAppleSeed.makeTree(this.level, posX, posY - 1, posZ, ModBlocks.LEAVES_CHERRY.get(), null);
                    }
                    if (which == 9) {
                        ItemAppleSeed.makeTree(this.level, posX, posY - 1, posZ, ModBlocks.LEAVES_PEACH.get(), null);
                    }
                    added = true;
                    break;
                }
            }
        }
        return added;
    }

    /** {@code addVeggies} (:1959-2009). */
    private void addVeggies(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(15) != 0) {
            return;
        }
        if (this.dim == Dim.UTOPIA || this.dim == Dim.MINING || this.dim == Dim.CHAOS
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.RIVER)
                || this.world.biomeIs(chunkX, chunkZ, LegacyBiomeNames.SWAMPLAND)) {
            for (int i = 0; i < 8; ++i) {
                final int posX = chunkX + random.nextInt(16);
                final int posZ = chunkZ + random.nextInt(16);
                int posY = this.scanStart(posX, posZ, 100);
                while (posY > 40 && this.world.isAirBlock(posX, posY, posZ)) {
                    if (this.isGrass(posX, posY - 1, posZ)) {
                        final int what = random.nextInt(6);
                        if (what == 0) {
                            this.world.setBlock(posX, posY, posZ, Blocks.CARROTS.defaultBlockState());
                            break;
                        }
                        if (what == 1) {
                            this.world.setBlock(posX, posY, posZ, Blocks.POTATOES.defaultBlockState());
                            break;
                        }
                        if (what == 2) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.RADISH_PLANT.get().defaultBlockState());
                            break;
                        }
                        if (what == 3) {
                            this.world.setBlock(posX, posY, posZ, ModBlocks.LETTUCE_0.get().defaultBlockState());
                            break;
                        }
                        if (what == 4) {
                            if (random.nextInt(10) == 0) {
                                this.world.setBlock(posX, posY, posZ, Blocks.MELON_STEM.defaultBlockState());
                                break;
                            }
                            break;
                        } else {
                            if (random.nextInt(50) == 1 && OreSpawnConfig.TWEAKS.DuplicatorTreeEnable.get() != 0) {
                                this.world.setBlock(posX, posY, posZ, ModBlocks.DUPLICATOR_TREE_LOG.get().defaultBlockState());
                                break;
                            }
                            break;
                        }
                    } else {
                        --posY;
                    }
                }
            }
        }
    }

    /**
     * {@code addRocks} (:2011-2027). PORT: {@code Blocks.sand} is the category {@code #minecraft:sand} (DECISIONS R22);
     * grass and crystal grass stay single blocks.
     */
    private void addRocks(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(5) != 0) {
            return;
        }
        if (!MobSwitches.enabled(OreSpawnConfig.MOBS.RockEnable)) {
            return;
        }
        for (int howmany = 3 + random.nextInt(10), i = 0; i < howmany; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = this.scanStart(posX, posZ, 110); posY > 40 && this.world.getBlock(posX, posY, posZ).isAir(); --posY) {
                final BlockState bid = this.world.getBlock(posX, posY - 1, posZ);
                if (bid.is(Blocks.GRASS_BLOCK) || bid.is(BlockTags.SAND) || bid.is(ModBlocks.CRYSTAL_GRASS.get())) {
                    this.spawnCreature("orespawn:rock", posX, posY, posZ);
                    break;
                }
            }
        }
    }

    /** {@code addD4Rocks} (:2029-2044). */
    private void addD4Rocks(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(7) != 0) {
            return;
        }
        if (!MobSwitches.enabled(OreSpawnConfig.MOBS.RockEnable)) {
            return;
        }
        for (int howmany = 3 + random.nextInt(10), i = 0; i < howmany; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            for (int posY = 20; posY > 5 && this.world.getBlock(posX, posY, posZ).isAir(); --posY) {
                if (this.isGrass(posX, posY - 1, posZ)) {
                    this.spawnCreature("orespawn:rock", posX, posY, posZ);
                    break;
                }
            }
        }
    }

    /**
     * {@code addRubyDungeon} (:2083-2098). The first lava block from Y 50 down to 6 is the origin;
     * {@code RubyBirdDungeon.makeDungeon} builds 10x5x10 from there, inside the window.
     */
    private boolean addRubyDungeon(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(15) != 0) {
            return false;
        }
        for (int i = 0; i < 8; ++i) {
            final int posX = chunkX + random.nextInt(8);
            final int posZ = chunkZ + random.nextInt(8);
            for (int posY = 50; posY > 5; --posY) {
                if (this.world.is(posX, posY, posZ, Blocks.LAVA)) {
                    RubyBirdDungeon.makeDungeon(LegacyWriter.of(this.world), this.worldRand, posX, posY, posZ);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@code addGenericDungeon} (:2100-2115): 12x6x12 at {@code chunk + nextInt(4)}, Y 5..44, no checks - inside the
     * window, so a direct call.
     */
    private boolean addGenericDungeon(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(16) != 0) {
            return false;
        }
        if (TweakStats.LessLag() == 1 && random.nextInt(2) != 0) {
            return false;
        }
        if (TweakStats.LessLag() == 2 && random.nextInt(4) != 0) {
            return false;
        }
        final int posX = chunkX + random.nextInt(4);
        final int posZ = chunkZ + random.nextInt(4);
        final int posY = 5 + random.nextInt(40);
        // addGenericDungeon (:2113): OreSpawnMain.MyDungeon.makeDungeon(world, posX, posY, posZ)
        com.swbr.orespawn.world.dungeon.a.SmallDungeons.makeDungeon(this.world, this.worldRand, posX, posY, posZ);
        return true;
    }

    /** {@code addLavaAndWater} (:2605-2670); flag 3 writes, the writer schedules the fluid tick. */
    private void addLavaAndWater(final Random random, final int chunkX, final int chunkZ) {
        if (random.nextInt(5) != 0) {
            return;
        }
        for (int i = 0; i < 6; ++i) {
            final int posX = chunkX + random.nextInt(16);
            final int posZ = chunkZ + random.nextInt(16);
            int posY = 128;
            while (posY > 75 && this.world.isAirBlock(posX, posY, posZ)) {
                if (this.isGrass(posX, posY - 1, posZ)) {
                    BlockState bid = this.world.getBlock(posX, posY - 2, posZ);
                    if (!isDirt(bid) && !isStone(bid)) {
                        break;
                    }
                    int air = 0;
                    int non_air = 0;
                    final int[][] ring = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    for (final int[] d : ring) {
                        bid = this.world.getBlock(posX + d[0], posY - 1, posZ + d[1]);
                        if (bid.isAir()) {
                            ++air;
                        }
                        if (isDirt(bid) || isStone(bid) || bid.is(Blocks.GRASS_BLOCK)) {
                            ++non_air;
                        }
                    }
                    if (air != 0 && non_air != 0) {
                        final int what = random.nextInt(2);
                        if (what == 0) {
                            this.world.setBlock(posX, posY, posZ, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                            this.world.setBlock(posX, posY - 1, posZ, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                            this.world.setBlock(posX, posY - 2, posZ, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            this.world.setBlock(posX, posY, posZ, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
                            this.world.setBlock(posX, posY - 1, posZ, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
                            this.world.setBlock(posX, posY - 2, posZ, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
                        }
                        return;
                    }
                    break;
                } else {
                    --posY;
                }
            }
        }
    }

    /** {@code Blocks.dirt} as the dirt category without the grass block, which has its own branch (DECISIONS R22). */
    private static boolean isDirt(final BlockState state) {
        return state.is(BlockTags.DIRT) && !state.is(Blocks.GRASS_BLOCK);
    }

    /** {@code Blocks.stone} as the overworld stone category (DECISIONS R22). */
    private static boolean isStone(final BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    /** {@code spawnCreature} (:2871-2892), with the original's half-block offset that skips 0 and pushes negatives out. */
    private void spawnCreature(final String entityId, double par2, final double par4, double par6) {
        if (par2 > 0.0) {
            par2 += 0.5;
        }
        if (par2 < 0.0) {
            par2 -= 0.5;
        }
        if (par6 > 0.0) {
            par6 += 0.5;
        }
        if (par6 < 0.0) {
            par6 -= 0.5;
        }
        final float yaw = this.worldRand.nextFloat() * 360.0f;
        this.world.spawnEntity(entityId, par2, par4 + 0.01, par6, yaw, 0.0f);
    }

    // -------------------------------------------------------------------------------------------------------
    // links between decoration and structures
    // -------------------------------------------------------------------------------------------------------

    /** Whether the structure {@code id} has its start in chunk {@code (chunkX, chunkZ)}. */
    private boolean startedHere(final String id, final int chunkX, final int chunkZ) {
        final Structure structure = this.level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.parse(id));
        if (structure == null) {
            return false;
        }
        final StructureStart start = this.level.getChunk(chunkX, chunkZ).getStartForStructure(structure);
        return start != null && start.isValid();
    }

    /** {@code addFairyTree(...)} returning {@code true} (:177), evaluated from the structure rule. */
    private boolean fairyTreeClaims(final int chunkX, final int chunkZ) {
        final ServerLevel server = this.level.getLevel();
        final Structure.GenerationContext context = new Structure.GenerationContext(
                this.level.registryAccess(),
                server.getChunkSource().getGenerator(),
                server.getChunkSource().getGenerator().getBiomeSource(),
                server.getChunkSource().randomState(),
                server.getStructureManager(),
                this.level.getSeed(),
                new ChunkPos(chunkX, chunkZ),
                this.level,
                biome -> true);
        return fairyTree(LegacyStructureContext.create(context, CRYSTAL_FAIRY_TREE)).claims();
    }

    // -------------------------------------------------------------------------------------------------------
    // structure rules
    // -------------------------------------------------------------------------------------------------------

    private static final Map<String, Object> MEMO = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<String, Object> eldest) {
            return this.size() > 4096;
        }
    };

    /**
     * A rule outcome that other rules and the decoration read again for the same world seed and chunk. The rules are
     * pure, so the memo only saves the base-column work.
     */
    @SuppressWarnings("unchecked")
    private static <T> T memo(final String id, final LegacyStructureContext c, final Supplier<T> rule) {
        final String key = id + '|' + c.generation().seed() + '|' + c.generation().chunkPos().toLong();
        synchronized (MEMO) {
            final Object known = MEMO.get(key);
            if (known != null) {
                return (T) known;
            }
        }
        final T value = rule.get();
        synchronized (MEMO) {
            MEMO.put(key, value);
        }
        return value;
    }

    /** Result of {@code addFairyTree}: its boolean and what it built. */
    private record FairyOutcome(boolean claims, Optional<Placement> placement) {
    }

    /** A W13 placement: the box of verhalten/world-01.md "Übersicht aller Strukturen" plus one block all round. */
    private static Placement w13(final String builder, final int x, final int y, final int z, final int variant,
                                 final int minDx, final int maxDx, final int minDy, final int maxDy,
                                 final int minDz, final int maxDz) {
        return Placement.of(builder, x, y, z, variant, minDx - 1, minDy - 1, minDz - 1, maxDx + 1, maxDy + 1, maxDz + 1);
    }

    private static List<Placement> one(final Optional<Placement> placement) {
        return placement.map(List::of).orElse(List.of());
    }

    // ---- Overworld: generateSurface :269-308 -------------------------------------------------------------

    /**
     * {@code generateSurface} :269-308: {@code world.rand.nextInt(6)} picks one of six water and pond builds, then the
     * chain nest - haunted house - leaf monster - spit bug - igloo - bouncy castle - duck pond runs until the first
     * success. Biomes are read at the chunk corner, as {@code getBiomeGenForCoords(chunkX, chunkZ)} did.
     *
     * <p>PORT: one build per cell; the rare case of a six-way build and a chain build in the same chunk
     * (about 1 in 185 000 chunks) is not reproduced.
     */
    private static List<Placement> overworldDungeon(final LegacyStructureContext c) {
        if (OreSpawnConfig.TWEAKS.DisableOverworldDungeons.get() != 0) {
            return List.of();
        }
        final SurfaceProbe p = c.probe();
        final int bx = c.chunkX();
        final int bz = c.chunkZ();
        final boolean ocean = p.biomeIs(bx, bz, LegacyBiomeNames.OCEAN);
        final boolean plains = p.biomeIs(bx, bz, LegacyBiomeNames.PLAINS);
        final double[] six = LegacyChain.exclusive(
                ocean ? LegacyChain.oneIn(350 * 6) : 0.0,
                ocean ? LegacyChain.oneIn(350 * 6) : 0.0,
                ocean ? LegacyChain.oneIn(350 * 6) : 0.0,
                ocean ? LegacyChain.oneIn(300 * 6) : 0.0,
                ocean ? LegacyChain.oneIn(300 * 6) : 0.0,
                plains ? LegacyChain.oneIn(350 * 6) : 0.0);
        final boolean nest = p.biomeIs(bx, bz, LegacyBiomeNames.FOREST) || p.biomeIs(bx, bz, LegacyBiomeNames.JUNGLE)
                || p.biomeIs(bx, bz, LegacyBiomeNames.BIRCH_FOREST);
        final boolean haunted = plains || p.biomeIs(bx, bz, LegacyBiomeNames.TAIGA)
                || p.biomeIs(bx, bz, LegacyBiomeNames.SWAMPLAND);
        final double[] chances = LegacyChain.then(six,
                nest ? LegacyChain.oneIn(230) : 0.0,
                haunted ? LegacyChain.oneIn(285) : 0.0,
                plains ? LegacyChain.oneIn(275) : 0.0,
                p.biomeIs(bx, bz, LegacyBiomeNames.SWAMPLAND) ? LegacyChain.oneIn(190) : 0.0,
                p.biomeIs(bx, bz, LegacyBiomeNames.ICE_PLAINS) ? LegacyChain.oneIn(220) : 0.0,
                p.biomeIs(bx, bz, LegacyBiomeNames.DESERT) ? LegacyChain.oneIn(230) : 0.0,
                plains ? LegacyChain.oneIn(275) : 0.0);
        final int k = LegacyChain.pick(c.random(), chances, CELL_50, BLOCK_50);
        return one(switch (k) {
            case 0 -> overworldWater(c, MAKE_PLAY_POOL, 0, -1, 4, 16, 18, 0, 0);
            case 1 -> overworldWater(c, MAKE_WATER_DRAGON_LAIR, -1, -10, 10, -1, 7, -10, 10);
            case 2 -> overworldWater(c, MAKE_GOLD_FISH_BOWL, -1, -1, 5, 1, 8, -1, 5);
            case 3 -> overworldWater(c, MAKE_GIRLFRIEND_ISLAND, -1, -5, 5, -1, 4, -3, 3);
            case 4 -> overworldWater(c, MAKE_MONSTER_ISLAND, -1, -5, 5, -1, 4, -3, 3);
            case 5 -> addFrogPond(c);
            case 6 -> addANest(c);
            case 7 -> addHauntedHouse(c);
            case 8 -> overworldGround(c, MAKE_LEAF_MONSTER_DUNGEON, 0, -3, 6, -4, 16, -3, 6);
            case 9 -> overworldGround(c, MAKE_SPIT_BUG_LAIR, 0, -8, 8, 0, 12, -8, 8);
            case 10 -> overworldGround(c, MAKE_IGLOO, -2, -6, 6, 0, 5, -6, 6);
            case 11 -> overworldGround(c, MAKE_BOUNCY_CASTLE, -1, -4, 4, 0, 4, -4, 4);
            case 12 -> overworldGround(c, MAKE_RUBBER_DUCKY_POND, 0, -5, 6, 0, 6, -5, 5);
            default -> Optional.<Placement>empty();
        });
    }

    /**
     * {@code addPlayPool} (:1159-1178), {@code addWaterDragonLair} (:1392), {@code addGoldFishBowl} (:1201),
     * {@code addGirlfriendIsland} (:1413), {@code addMonsterIsland} (:1434): 4 tries, Y 100→41 air over water, build at
     * {@code posY + dy}.
     */
    private static Optional<Placement> overworldWater(final LegacyStructureContext c, final String builder, final int dy,
                                                      final int minX, final int maxX, final int minY, final int maxY,
                                                      final int minZ, final int maxZ) {
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 4; ++i) {
            final int posX = c.chunkX() + random.nextInt(16);
            final int posZ = c.chunkZ() + random.nextInt(16);
            for (int posY = p.scanStart(posX, posZ, 100, true); posY > 40; --posY) {
                if (p.isAirBlock(posX, posY, posZ) && p.isWater(posX, posY - 1, posZ)) {
                    // OreSpawnMain.MyDungeon.<builder>(world, posX, posY + dy, posZ)
                    return Optional.of(w13(builder, posX, posY + dy, posZ, 0, minX, maxX, minY, maxY, minZ, maxZ));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * {@code addLeafMonster} (:1222), {@code addSpitBug} (:1266), {@code addIgloo} (:1288, over {@code Blocks.snow}),
     * {@code addBouncyCastle} (:1310, over sand), {@code addRubberDuckyPond} (:1244): 4 tries, Y 100→41 air over the
     * ground block, build at {@code posY + dy}. The ground block is {@link SurfaceProbe#isGround} (class javadoc there).
     */
    private static Optional<Placement> overworldGround(final LegacyStructureContext c, final String builder, final int dy,
                                                       final int minX, final int maxX, final int minY, final int maxY,
                                                       final int minZ, final int maxZ) {
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 4; ++i) {
            final int posX = c.chunkX() + random.nextInt(16);
            final int posZ = c.chunkZ() + random.nextInt(16);
            for (int posY = p.scanStart(posX, posZ, 100, true); posY > 40; --posY) {
                if (p.isAirBlock(posX, posY, posZ) && p.isGround(posX, posY - 1, posZ)) {
                    // OreSpawnMain.MyDungeon.<builder>(world, posX, posY + dy, posZ)
                    return Optional.of(w13(builder, posX, posY + dy, posZ, 0, minX, maxX, minY, maxY, minZ, maxZ));
                }
            }
        }
        return Optional.empty();
    }

    /** {@code addFrogPond} (:1180-1199): as {@link #overworldGround}, build at {@code posY - 1}. */
    private static Optional<Placement> addFrogPond(final LegacyStructureContext c) {
        // OreSpawnMain.MyDungeon.makeFrogPond(world, posX, posY - 1, posZ)
        return overworldGround(c, MAKE_FROG_POND, -1, -3, 3, 0, 2, -3, 3);
    }

    /** {@code addANest} (:999-1021): 5 tries, Y 128→41 while air, grass below; 50:50 small bee hive or mantis hive. */
    private static Optional<Placement> addANest(final LegacyStructureContext c) {
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 5; ++i) {
            final int posX = c.chunkX() + random.nextInt(16);
            final int posZ = c.chunkZ() + random.nextInt(16);
            for (int posY = p.scanStart(posX, posZ, 128, true); posY > 40 && p.isAirBlock(posX, posY, posZ); --posY) {
                if (p.isGround(posX, posY - 1, posZ)) {
                    if (random.nextInt(2) == 0) {
                        // OreSpawnMain.MyDungeon.makeSmallBeeHive(world, posX, posY, posZ)
                        return Optional.of(w13(MAKE_SMALL_BEE_HIVE, posX, posY, posZ, 0, -3, 9, 1, 21, -3, 9));
                    }
                    // OreSpawnMain.MyDungeon.makeMantisHive(world, posX, posY, posZ)
                    return Optional.of(w13(MAKE_MANTIS_HIVE, posX, posY, posZ, 0, 0, 12, -6, 19, 0, 12));
                }
            }
        }
        return Optional.empty();
    }

    /** {@code addHauntedHouse} (:980-997): 5 tries, Y 100→41 while air, grass below. */
    private static Optional<Placement> addHauntedHouse(final LegacyStructureContext c) {
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 5; ++i) {
            final int posX = c.chunkX() + random.nextInt(16);
            final int posZ = c.chunkZ() + random.nextInt(16);
            for (int posY = p.scanStart(posX, posZ, 100, true); posY > 40 && p.isAirBlock(posX, posY, posZ); --posY) {
                if (p.isGround(posX, posY - 1, posZ)) {
                    // OreSpawnMain.MyDungeon.makeHauntedHouse(world, posX, posY, posZ)
                    return Optional.of(w13(MAKE_HAUNTED_HOUSE, posX, posY, posZ, 0, -3, 3, 0, 4, -3, 3));
                }
            }
        }
        return Optional.empty();
    }

    // ---- End: generateEnd :215-230 ------------------------------------------------------------------------

    /**
     * {@code generateEnd} :217-229: {@code world.rand.nextInt(4)} picks Ender Knights (1/25), Ender Reapers (1/25),
     * the Ender Dragon hospital (1/25) or the Ender castle (1/50). No counter, so the set asks every chunk and the pick
     * is the original roll.
     */
    private static List<Placement> endDungeon(final LegacyStructureContext c) {
        final double[] chances = LegacyChain.exclusive(
                LegacyChain.oneIn(25 * 4), LegacyChain.oneIn(25 * 4), LegacyChain.oneIn(25 * 4), LegacyChain.oneIn(50 * 4));
        final int k = LegacyChain.pick(c.random(), chances, CELL_1, 0);
        return one(switch (k) {
            case 0 -> endBuild(c, MAKE_ENDER_KNIGHT_DUNGEON, false, 0, 12, 0, 5, -2, 6);
            case 1 -> endBuild(c, MAKE_ENDER_REAPER_GRAVEYARD, false, 0, 10, -4, 4, 0, 12);
            case 2 -> endBuild(c, MAKE_ENDER_DRAGON_HOSPITAL, false, -6, 9, 0, 9, 0, 9);
            case 3 -> endBuild(c, MAKE_ENDER_CASTLE, true, -4, 26, 0, 16, -4, 26);
            default -> Optional.<Placement>empty();
        });
    }

    /**
     * {@code addEndKnights} (:1558), {@code addEndReapers} (:1574), {@code addHospital} (:1590), {@code addEnderCastle}
     * (:1606): 3 tries, Y 90→11, air over end stone and {@code quickSpaceCheck} (12x12 on Y+4) or
     * {@code quickBigSpaceCheck} (30x30 on Y+8); build at Y.
     */
    private static Optional<Placement> endBuild(final LegacyStructureContext c, final String builder, final boolean big,
                                                final int minX, final int maxX, final int minY, final int maxY,
                                                final int minZ, final int maxZ) {
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 3; ++i) {
            final int posX = c.chunkX() + random.nextInt(16);
            final int posZ = c.chunkZ() + random.nextInt(16);
            for (int posY = 90; posY > 10; --posY) {
                if (p.isAirBlock(posX, posY, posZ) && p.is(posX, posY - 1, posZ, Blocks.END_STONE)
                        && (big ? quickBigSpaceCheck(p, posX, posY, posZ) : quickSpaceCheck(p, posX, posY, posZ))) {
                    // OreSpawnMain.MyDungeon.<builder>(world, posX, posY, posZ)
                    return Optional.of(w13(builder, posX, posY, posZ, 0, minX, maxX, minY, maxY, minZ, maxZ));
                }
            }
        }
        return Optional.empty();
    }

    // ---- Utopia -------------------------------------------------------------------------------------------


    /**
     * {@code addHugeTree} (:1902-1957). The rolled arguments of {@code MakeBigSquareTree}/{@code MakeBigCircularTree}/
     * {@code MakeBigRoundTree} go into the placement ({@link TreeBuilders#magicAppleTree}, which also owns the box).
     */
    private static Optional<Placement> hugeTree(final LegacyStructureContext c) {
        return memo(UTOPIA_HUGE_TREE, c, () -> {
            double chance = LegacyChain.oneIn(50);
            if (TweakStats.LessLag() == 1) {
                chance /= 2.0;
            }
            if (TweakStats.LessLag() == 2) {
                chance /= 4.0;
            }
            if (LegacyChain.pick(c.random(), new double[] {chance}, CELL_1, 0) != 0) {
                return Optional.<Placement>empty();
            }
            final SurfaceProbe p = c.probe();
            final Random random = c.random();
            for (int i = 0; i < 3; ++i) {
                final int posX = 4 + c.chunkX() + random.nextInt(8);
                final int posZ = 4 + c.chunkZ() + random.nextInt(8);
                for (int posY = 127; posY > 50; --posY) {
                    if (p.isAirBlock(posX, posY, posZ) && p.isGround(posX, posY - 1, posZ)) {
                        final int tree_type = random.nextInt(4);
                        int tree_radius = 6 - random.nextInt(2);
                        boolean no_critters = false;
                        boolean apple = false;
                        if (random.nextInt(100) > 25) {
                            no_critters = true;
                        }
                        final int rand_treetype = random.nextInt(100);
                        final Placement tree;
                        if (rand_treetype > 75) {
                            if (tree_type != 3 && random.nextInt(20) == 0) {
                                apple = true;
                            }
                            // MakeBigSquareTree(world, x, y - 1, z, Blocks.log, leaf_type, Blocks.mossy_cobblestone, ...)
                            tree = TreeBuilders.magicAppleTree(posX, posY - 1, posZ, TreeBuilders.SHAPE_SQUARE, tree_type,
                                    tree_radius, no_critters, apple ? TreeBuilders.BLOCKS_LOG_APPLE_LEAVES_MOSSY
                                            : TreeBuilders.BLOCKS_LOG_LEAVES_MOSSY);
                        } else if (rand_treetype == 0) {
                            tree_radius = 6;
                            no_critters = true;
                            // :1936-1941 Tree of Goodness or Queen's tree, tree_type -1
                            final int blocks = random.nextInt(2) == 0 ? TreeBuilders.BLOCKS_GOLD_EMERALD_DIAMOND
                                    : TreeBuilders.BLOCKS_OBSIDIAN_RUBY_AMETHYST;
                            tree = TreeBuilders.magicAppleTree(posX, posY - 1, posZ, TreeBuilders.SHAPE_SQUARE, -1,
                                    tree_radius, no_critters, blocks);
                        } else if (rand_treetype > 15) {
                            tree_radius = 6 - random.nextInt(3);
                            tree = TreeBuilders.magicAppleTree(posX, posY - 1, posZ, TreeBuilders.SHAPE_CIRCULAR,
                                    tree_type, tree_radius, no_critters, TreeBuilders.BLOCKS_LOG_LEAVES_MOSSY);
                        } else {
                            tree_radius = 6 - random.nextInt(3);
                            tree = TreeBuilders.magicAppleTree(posX, posY - 1, posZ, TreeBuilders.SHAPE_ROUND,
                                    tree_type, tree_radius, no_critters, TreeBuilders.BLOCKS_LOG_LEAVES_MOSSY);
                        }
                        return Optional.of(tree);
                    }
                }
            }
            return Optional.<Placement>empty();
        });
    }

    /**
     * {@code addOtherTrees} (:2672-2722), only in a chunk without a huge tree (:30-31). Up to four Wind trees or three
     * Sky trees, one placement each.
     */
    private static List<Placement> otherTrees(final LegacyStructureContext c) {
        return memo(UTOPIA_OTHER_TREES, c, () -> {
            if (hugeTree(LegacyStructureContext.create(c.generation(), UTOPIA_HUGE_TREE)).isPresent()) {
                return List.<Placement>of();
            }
            int nc = 5;
            int count = 0;
            double chance = LegacyChain.oneIn(30);
            if (TweakStats.LessLag() == 1) {
                chance /= 2.0;
                nc = 4;
            }
            if (TweakStats.LessLag() == 2) {
                chance /= 4.0;
                nc = 3;
            }
            if (LegacyChain.pick(c.random(), new double[] {chance}, CELL_1, 0) != 0) {
                return List.<Placement>of();
            }
            final SurfaceProbe p = c.probe();
            final Random random = c.random();
            final List<Placement> trees = new ArrayList<>();
            final int dir = 0;
            final int what = random.nextInt(2);
            for (int i = 0; i < nc; ++i) {
                final int posX = 3 + c.chunkX() + random.nextInt(10);
                final int posZ = 3 + c.chunkZ() + random.nextInt(10);
                int posY = 100;
                while (posY > 50 && p.isAirBlock(posX, posY, posZ)) {
                    if (p.isGround(posX, posY - 1, posZ)) {
                        ++count;
                        if (what == 0) {
                            // OreSpawnTrees.WindTree(world, posX, posY - 1, posZ, dir)
                            trees.add(TreeBuilders.windTree(posX, posY - 1, posZ, dir));
                            if (count >= 4) {
                                return trees;
                            }
                            break;
                        } else {
                            // OreSpawnTrees.SkyTree(world, posX, posY - 1, posZ)
                            trees.add(TreeBuilders.skyTree(posX, posY - 1, posZ));
                            if (count >= 3) {
                                return trees;
                            }
                            break;
                        }
                    } else {
                        --posY;
                    }
                }
            }
            return trees;
        });
    }

    /**
     * {@code addKingAltar} (:2724-2752), only where neither tree structure places (:30-32). 8 tries, Y 100→51 air over
     * grass; a failed {@code quickReallyBigSpaceCheck} (60x60 on Y+8 of {@code posY - 1}) ends the method. 50:50 King or
     * Queen altar at {@code posY - 1}.
     */
    private static List<Placement> kingAltar(final LegacyStructureContext c) {
        if (hugeTree(LegacyStructureContext.create(c.generation(), UTOPIA_HUGE_TREE)).isPresent()
                || !otherTrees(LegacyStructureContext.create(c.generation(), UTOPIA_OTHER_TREES)).isEmpty()) {
            return List.of();
        }
        if (LegacyChain.pick(c.random(), new double[] {LegacyChain.oneIn(2000)}, CELL_100, BLOCK_100) != 0) {
            return List.of();
        }
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 8; ++i) {
            final int posX = 3 + c.chunkX() + random.nextInt(10);
            final int posZ = 3 + c.chunkZ() + random.nextInt(10);
            int posY = 100;
            while (posY > 50) {
                if (p.isAirBlock(posX, posY, posZ) && p.isGround(posX, posY - 1, posZ)) {
                    if (!quickReallyBigSpaceCheck(p, posX, posY - 1, posZ)) {
                        return List.of();
                    }
                    // variant 0 OreSpawnMain.MyDungeon.makeKingAltar(world, posX, posY - 1, posZ),
                    // variant 1 makeQueenAltar(world, posX, posY - 1, posZ)
                    final int variant = random.nextInt(2) == 0 ? 0 : 1;
                    return List.of(w13(MAKE_KING_ALTAR, posX, posY - 1, posZ, variant, -5, 55, -9, 58, -5, 55));
                } else {
                    --posY;
                }
            }
        }
        return List.of();
    }

    // ---- Mining :65-91 ------------------------------------------------------------------------------------

    /** {@code recently_placed == 0 && nextInt(95) == 1}, then {@code nextInt(7)} (:65-88). */
    private static List<Placement> miningDungeon(final LegacyStructureContext c) {
        final double each = LegacyChain.oneIn(95 * 7);
        final int k = LegacyChain.pick(c.random(), LegacyChain.exclusive(each, each, each, each, each, each, each),
                CELL_50, BLOCK_50);
        return one(switch (k) {
            // addBasiliskMaze (:2754): OreSpawnMain.BMaze.buildBasiliskMaze(world, lowestX, lowestY - 2, lowestZ)
            case 0 -> miningLowest(c, false).map(q -> TreeBuilders.basiliskMaze(q[0], q[1] - 2, q[2]));
            // addKyuubiDungeon (:2787): OreSpawnMain.MyDungeon.makeKyuubiDungeon(world, lowestX, lowestY - 2, lowestZ)
            case 1 -> miningLowest(c, false).map(q -> w13(MAKE_KYUUBI_DUNGEON, q[0], q[1] - 2, q[2], 0, 0, 34, -22, 5, -15, 14));
            // addBeeHive (:2117): makeBeeHive(world, lowestX, lowestY + 3, lowestZ)
            case 2 -> miningLowest(c, true).map(q -> w13(MAKE_BEE_HIVE, q[0], q[1] + 3, q[2], 0, 0, 9, -30, 0, 0, 9));
            // addShadowDungeon (:2257): makeShadowDungeon(world, lowestX, lowestY, lowestZ)
            case 3 -> miningLowest(c, true).map(q -> w13(MAKE_SHADOW_DUNGEON, q[0], q[1], q[2], 0, 0, 18, -9, 9, 0, 18));
            // addAlienWTF (:2152): makeAlienWTFDungeon(world, lowestX, lowestY, lowestZ)
            case 4 -> miningLowest(c, true).map(q -> w13(MAKE_ALIEN_WTF_DUNGEON, q[0], q[1], q[2], 0, -19, 17, -17, 2, -21, 15));
            // addEnderKnight (:2187): makeEnderKnightDungeon(world, lowestX, lowestY, lowestZ)
            case 5 -> miningLowest(c, true).map(q -> w13(MAKE_ENDER_KNIGHT_DUNGEON, q[0], q[1], q[2], 0, 0, 12, 0, 5, -2, 6));
            // addLeonNest (:2222): makeLeonNest(world, highestX, highestY, highestZ)
            case 6 -> addLeonNest(c).map(q -> w13(MAKE_LEON_NEST, q[0], q[1], q[2], 0, -10, 10, -10, 5, -10, 10));
            default -> Optional.<Placement>empty();
        });
    }

    /**
     * The 6x6 grid scan of {@code addBeeHive}, {@code addAlienWTF}, {@code addEnderKnight}, {@code addShadowDungeon}
     * ({@code == grass}) and {@code addBasiliskMaze}, {@code addKyuubiDungeon} ({@code != air}): per column from Y 128
     * down to 31 the first block with air above; the lowest of them, if above 40.
     *
     * @return {@code {lowestX, lowestY, lowestZ}}
     */
    private static Optional<int[]> miningLowest(final LegacyStructureContext c, final boolean grass) {
        final SurfaceProbe p = c.probe();
        int lowestY = 128;
        int lowestX = c.chunkX();
        int lowestZ = c.chunkZ();
        int found = 0;
        for (int i = 0; i < 16; i += 3) {
            for (int j = 0; j < 16; j += 3) {
                final int posX = c.chunkX() + i;
                final int posZ = c.chunkZ() + j;
                int posY = 128;
                while (posY > 30) {
                    final boolean match = grass ? p.isGround(posX, posY, posZ) : !p.isAirBlock(posX, posY, posZ);
                    if (p.isAirBlock(posX, posY + 1, posZ) && match) {
                        if (posY < lowestY) {
                            lowestY = posY;
                            lowestX = posX;
                            lowestZ = posZ;
                            found = 1;
                            break;
                        }
                        break;
                    } else {
                        --posY;
                    }
                }
            }
        }
        if (found != 0 && lowestY > 40) {
            return Optional.of(new int[] {lowestX, lowestY, lowestZ});
        }
        return Optional.empty();
    }

    /** {@code addLeonNest} (:2222-2255): the highest grass with air above, Y 128 down to 81, if above 80. */
    private static Optional<int[]> addLeonNest(final LegacyStructureContext c) {
        final SurfaceProbe p = c.probe();
        int highestY = 30;
        int highestX = c.chunkX();
        int highestZ = c.chunkZ();
        int found = 0;
        for (int i = 0; i < 16; i += 3) {
            for (int j = 0; j < 16; j += 3) {
                final int posX = c.chunkX() + i;
                final int posZ = c.chunkZ() + j;
                int posY = 128;
                while (posY > 80) {
                    if (p.isAirBlock(posX, posY + 1, posZ) && p.isGround(posX, posY, posZ)) {
                        if (posY > highestY) {
                            highestY = posY + 1;
                            highestX = posX;
                            highestZ = posZ;
                            found = 1;
                            break;
                        }
                        break;
                    } else {
                        --posY;
                    }
                }
            }
        }
        if (found != 0 && highestY > 80) {
            return Optional.of(new int[] {highestX, highestY, highestZ});
        }
        return Optional.empty();
    }

    // ---- VillageMania :108-116 ----------------------------------------------------------------------------

    /** Damsel in distress (1/250), Spider hangout (1/350, {@code SpiderDriverEnable}), Red Ant hangout (1/250). */
    private static List<Placement> villageDungeon(final LegacyStructureContext c) {
        final double[] chances = {
                LegacyChain.oneIn(250),
                MobSwitches.enabled(OreSpawnConfig.MOBS.SpiderDriverEnable) ? LegacyChain.oneIn(350) : 0.0,
                LegacyChain.oneIn(250)};
        final int k = LegacyChain.pick(c.random(), chances, CELL_50, BLOCK_50);
        return one(switch (k) {
            // addDamselInDistress (:1332): makeDamselInDistress(world, posX, posY - 1, posZ)
            case 0 -> villageBuild(c, MAKE_DAMSEL_IN_DISTRESS, -4, 4, 0, 9, -4, 4);
            // addSpiderHangout (:1351): makeSpiderHangout(world, posX, posY - 1, posZ)
            case 1 -> villageBuild(c, MAKE_SPIDER_HANGOUT, 0, 19, -1, 19, 0, 19);
            // addRedAntHangout (:1373): makeRedAntHangout(world, posX, posY - 1, posZ)
            case 2 -> villageBuild(c, MAKE_RED_ANT_HANGOUT, 0, 15, -1, 15, 0, 15);
            default -> Optional.<Placement>empty();
        });
    }

    /** 4 tries, Y 100→41: air over grass and {@code quickSpaceCheck(posX, posY - 1, posZ)}; build at {@code posY - 1}. */
    private static Optional<Placement> villageBuild(final LegacyStructureContext c, final String builder,
                                                    final int minX, final int maxX, final int minY, final int maxY,
                                                    final int minZ, final int maxZ) {
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 4; ++i) {
            final int posX = c.chunkX() + random.nextInt(16);
            final int posZ = c.chunkZ() + random.nextInt(16);
            for (int posY = 100; posY > 40; --posY) {
                if (p.isAirBlock(posX, posY, posZ) && p.isGround(posX, posY - 1, posZ)
                        && quickSpaceCheck(p, posX, posY - 1, posZ)) {
                    return Optional.of(w13(builder, posX, posY - 1, posZ, 0, minX, maxX, minY, maxY, minZ, maxZ));
                }
            }
        }
        return Optional.empty();
    }

    // ---- Islands :120-170 ---------------------------------------------------------------------------------

    /**
     * {@code recently_placed == 0 && nextInt(100) == 0 && D4BigSpaceCheck(...)}, then {@code nextInt(19)} (:120-166): 3/19
     * castle, 4/19 generic dungeon, one each for the rest; every D4 build but the rainbow halves its chance with
     * {@code LessLag != 0} (the generic dungeon quarters it).
     */
    private static List<Placement> islandsDungeon(final LegacyStructureContext c) {
        final boolean lessLag = TweakStats.LessLag() != 0;
        final double base = LegacyChain.oneIn(100 * 19);
        final double half = lessLag ? base / 2.0 : base;
        final double[] chances = LegacyChain.exclusive(
                3 * half,                        // 0 addD4Castle
                4 * (lessLag ? base / 4.0 : base), // 1 addD4GenericDungeon
                half,                            // 2 addD4EnderCastle
                half,                            // 3 addD4IncaPyramid
                half,                            // 4 addD4RobotLab
                half,                            // 5 addD4Mini
                half,                            // 6 addD4RubyDungeon
                half,                            // 7 addD4CephadromeAltar
                half,                            // 8 addD4Greenhouse
                half,                            // 9 addD4NightmareRookery
                half,                            // 10 addD4StinkyHouse
                half,                            // 11 addD4WhiteHouse
                half,                            // 12 addPumpkin
                base);                           // 13 addD4Rainbow
        final int k = LegacyChain.pick(c.random(), chances, CELL_50, BLOCK_50);
        if (k < 0) {
            return List.of();
        }
        final SurfaceProbe p = c.probe();
        if (!d4BigSpaceCheck(p, c.chunkX(), 7, c.chunkZ())) {
            return List.of();
        }
        final Random random = c.random();
        if (k == 13) {
            // addD4Rainbow (:2580-2586); makeRainbow(world, posX, 70 + world.rand.nextInt(20), posZ)
            final int posX = 4 + c.chunkX() + random.nextInt(8);
            final int posZ = 4 + c.chunkZ() + random.nextInt(8);
            return List.of(w13(MAKE_RAINBOW, posX, 70 + random.nextInt(20), posZ, 0, -14, 13, 26, 40, -3, 3));
        }
        final int posX = c.chunkX() + random.nextInt(8);
        final int posZ = c.chunkZ() + random.nextInt(8);
        for (int posY = 20; posY > 4; --posY) {
            if (!p.is(posX, posY, posZ, Blocks.GRASS_BLOCK)) {
                continue;
            }
            return one(switch (k) {
                // addD4Castle (:2326): nextInt(2) == 1 ? makeEnormousCastle : makeEnormousCastleQ (world, posX, posY, posZ)
                case 0 -> p.areaIsAir(posX, posY + 18, posZ, -20, 33, -4, 33, 4)
                        ? Optional.of(w13(MAKE_ENORMOUS_CASTLE, posX, posY, posZ, random.nextInt(2) == 1 ? 1 : 0,
                                -37, 55, -1, 80, -28, 55))
                        : Optional.<Placement>empty();
                // addD4GenericDungeon (:2588): makeDungeon(world, posX, posY, posZ)
                case 1 -> Optional.of(w13(MAKE_DUNGEON, posX, posY, posZ, 0, 0, 11, 0, 5, 0, 11));
                // addD4EnderCastle (:2456): makeEnderCastle(world, posX, posY, posZ)
                case 2 -> p.areaIsAir(posX, posY + 18, posZ, -5, 25, -5, 25, 4)
                        ? Optional.of(w13(MAKE_ENDER_CASTLE, posX, posY, posZ, 0, -4, 26, 0, 16, -4, 26))
                        : Optional.<Placement>empty();
                // addD4IncaPyramid (:2481): makeIncaPyramid(world, posX, posY, posZ)
                case 3 -> p.areaIsAir(posX, posY + 18, posZ, -10, 50, -10, 40, 4)
                        ? Optional.of(w13(MAKE_INCA_PYRAMID, posX, posY, posZ, 0, -10, 50, 0, 19, -10, 40))
                        : Optional.<Placement>empty();
                // addD4RobotLab (:2506): makeRobotLab(world, posX, posY, posZ); check on Y+4 tolerates log, apple and scary leaves
                case 4 -> p.areaIsAir(posX, posY + 4, posZ, -5, 60, -5, 70, 4,
                        Blocks.OAK_LOG, ModBlocks.LEAVES_APPLE.get(), ModBlocks.LEAVES_SCARY.get())
                        ? Optional.of(w13(MAKE_ROBOT_LAB, posX, posY, posZ, 0, -10, 19, 0, 43, -1, 48))
                        : Optional.<Placement>empty();
                // addD4Mini (:2539): makeMiniDungeon(world, posX, posY, posZ)
                case 5 -> Optional.of(w13(MAKE_MINI_DUNGEON, posX, posY, posZ, 0, -6, 9, 0, 11, 0, 9));
                // addD4RubyDungeon (:2292): RubyBirdDungeon.makeDungeon(world, posX, posY, posZ), 10x5x10
                case 6 -> Optional.of(Placement.of(RUBY_BIRD_DUNGEON, posX, posY, posZ, 0, 0, 0, 0, 9, 4, 9));
                // addD4CephadromeAltar (:2309): makeCephadromeAltar(world, posX, posY, posZ)
                case 7 -> Optional.of(w13(MAKE_CEPHADROME_ALTAR, posX, posY, posZ, 0, -4, 4, 0, 4, -4, 4));
                // addD4Greenhouse (:2356): makeGreenhouseDungeon(world, posX, posY, posZ)
                case 8 -> p.areaIsAir(posX, posY + 18, posZ, -2, 25, -4, 25, 4)
                        ? Optional.of(w13(MAKE_GREENHOUSE_DUNGEON, posX, posY, posZ, 0, 0, 22, 0, 13, -1, 14))
                        : Optional.<Placement>empty();
                // addD4NightmareRookery (:2381): makeNightmareRookery(world, posX, posY, posZ)
                case 9 -> p.areaIsAir(posX, posY + 18, posZ, -5, 25, -4, 5, 4)
                        ? Optional.of(w13(MAKE_NIGHTMARE_ROOKERY, posX, posY, posZ, 0, -6, 21, 0, 21, -53, 53))
                        : Optional.<Placement>empty();
                // addD4StinkyHouse (:2406): makeStinkyHouse(world, posX, posY, posZ)
                case 10 -> p.areaIsAir(posX, posY + 18, posZ, -8, 20, -8, 20, 4)
                        ? Optional.of(w13(MAKE_STINKY_HOUSE, posX, posY, posZ, 0, -5, 19, 1, 3, -4, 12))
                        : Optional.<Placement>empty();
                // addD4WhiteHouse (:2431): makeWhiteHouse(world, posX, posY, posZ); the z < 300 check is
                // the original's (DECISIONS R18), sampled every 8 columns
                case 11 -> p.areaIsAir(posX, posY + 18, posZ, -20, 30, -20, 300, 8)
                        ? Optional.of(w13(MAKE_WHITE_HOUSE, posX, posY, posZ, 0, -5, 21, 0, 21, -15, 18))
                        : Optional.<Placement>empty();
                // addPumpkin (:2556): makePumpkin(world, posX, posY + 1, posZ)
                case 12 -> Optional.of(w13(MAKE_PUMPKIN, posX, posY + 1, posZ, 0, 0, 13, 0, 17, 0, 11));
                default -> Optional.<Placement>empty();
            });
        }
        return List.of();
    }

    /** {@code addD4CloudShark} (:2573-2578), {@code nextInt(300) == 0} (:167-170), no counter. */
    private static List<Placement> islandsCloudShark(final LegacyStructureContext c) {
        if (LegacyChain.pick(c.random(), new double[] {LegacyChain.oneIn(300)}, CELL_1, 0) != 0) {
            return List.of();
        }
        final Random random = c.random();
        final int posX = 4 + c.chunkX() + random.nextInt(8);
        final int posZ = 4 + c.chunkZ() + random.nextInt(8);
        // OreSpawnMain.MyDungeon.makeCloudSharkDungeon(world, posX, 150 + world.rand.nextInt(10), posZ)
        return List.of(w13(MAKE_CLOUD_SHARK_DUNGEON, posX, 150 + random.nextInt(10), posZ, 0, -1, 1, -1, 1, -1, 1));
    }

    // ---- Crystal :176-185 ---------------------------------------------------------------------------------

    /**
     * {@code addFairyTree} (:2046-2081): 1/5 at the chunk centre; Y 128→41 air over crystal grass; 17x17 air on that Y
     * and 5x5 crystal grass below or {@code false}; 4/5 {@code FairyTree(posY - 1)}, 1/5 {@code FairyCastleTree(posY)}.
     * {@code true} also when no surface is found.
     */
    private static FairyOutcome fairyTree(final LegacyStructureContext c) {
        return memo(CRYSTAL_FAIRY_TREE, c, () -> {
            final int posX = c.chunkX() + 8;
            final int posZ = c.chunkZ() + 8;
            if (LegacyChain.pick(c.random(), new double[] {LegacyChain.oneIn(5)}, CELL_1, 0) != 0) {
                return new FairyOutcome(false, Optional.empty());
            }
            final SurfaceProbe p = c.probe();
            for (int posY = 128; posY > 40; --posY) {
                if (p.isAirBlock(posX, posY, posZ) && p.isGround(posX, posY - 1, posZ)) {
                    if (!p.areaIsAir(posX, posY, posZ, -8, 9, -8, 9, 8)) {
                        return new FairyOutcome(false, Optional.empty());
                    }
                    for (int i = -2; i <= 2; i += 2) {
                        for (int j = -2; j <= 2; j += 2) {
                            if (!p.isGround(posX + i, posY - 1, posZ + j)) {
                                return new FairyOutcome(false, Optional.empty());
                            }
                        }
                    }
                    if (c.random().nextInt(5) != 1) {
                        // OreSpawnTrees.FairyTree(world, posX, posY - 1, posZ)
                        return new FairyOutcome(true, Optional.of(TreeBuilders.fairyTree(posX, posY - 1, posZ)));
                    }
                    // OreSpawnTrees.FairyCastleTree(world, posX, posY, posZ)
                    return new FairyOutcome(true, Optional.of(TreeBuilders.fairyCastleTree(posX, posY, posZ)));
                }
            }
            return new FairyOutcome(true, Optional.empty());
        });
    }

    /**
     * :178-184: only in a chunk the fairy tree does not claim; the chain rotator station (1/150, {@code RotatorEnable}) -
     * urchin spawner (1/180, {@code UrchinEnable}) - crystal haunted house (1/230) - round rotator (1/150,
     * {@code RotatorEnable}) - battle tower (1/280). Each: 3 tries, Y 100→51 air over crystal grass, build at Y.
     */
    private static List<Placement> crystalDungeon(final LegacyStructureContext c) {
        if (fairyTree(LegacyStructureContext.create(c.generation(), CRYSTAL_FAIRY_TREE)).claims()) {
            return List.of();
        }
        final boolean rotator = MobSwitches.enabled(OreSpawnConfig.MOBS.RotatorEnable);
        final double[] chances = {
                rotator ? LegacyChain.oneIn(150) : 0.0,
                MobSwitches.enabled(OreSpawnConfig.MOBS.UrchinEnable) ? LegacyChain.oneIn(180) : 0.0,
                LegacyChain.oneIn(230),
                rotator ? LegacyChain.oneIn(150) : 0.0,
                LegacyChain.oneIn(280)};
        final int k = LegacyChain.pick(c.random(), chances, CELL_50, BLOCK_50);
        return one(switch (k) {
            // addRotatorStation (:1658): makeRotatorStation(world, posX, posY, posZ)
            case 0 -> crystalBuild(c, MAKE_ROTATOR_STATION, 0, 0, 4, 8, 0, 0);
            // addUrchinSpawner (:1700): makeUrchinSpawner(world, posX, posY, posZ)
            case 1 -> crystalBuild(c, MAKE_URCHIN_SPAWNER, -17, 18, -1, 17, -17, 18);
            // addCrystalHauntedHouse (:1721): makeCrystalHauntedHouse(world, posX, posY, posZ)
            case 2 -> crystalBuild(c, MAKE_CRYSTAL_HAUNTED_HOUSE, -3, 3, 0, 4, -3, 3);
            // addRoundRotator (:1679): makeRoundRotator(world, posX, posY, posZ)
            case 3 -> crystalBuild(c, MAKE_ROUND_ROTATOR, -6, 7, 0, 12, 0, 0);
            // addCrystalBattleTower (:1739): makeCrystalBattleTower(world, posX, posY, posZ)
            case 4 -> crystalBuild(c, MAKE_CRYSTAL_BATTLE_TOWER, -10, 11, 0, 23, -10, 11);
            default -> Optional.<Placement>empty();
        });
    }

    private static Optional<Placement> crystalBuild(final LegacyStructureContext c, final String builder,
                                                    final int minX, final int maxX, final int minY, final int maxY,
                                                    final int minZ, final int maxZ) {
        final SurfaceProbe p = c.probe();
        final Random random = c.random();
        for (int i = 0; i < 3; ++i) {
            final int posX = c.chunkX() + random.nextInt(16);
            final int posZ = c.chunkZ() + random.nextInt(16);
            for (int posY = 100; posY > 50; --posY) {
                if (p.isAirBlock(posX, posY, posZ) && p.isGround(posX, posY - 1, posZ)) {
                    return Optional.of(w13(builder, posX, posY, posZ, 0, minX, maxX, minY, maxY, minZ, maxZ));
                }
            }
        }
        return Optional.empty();
    }

    // ---- space checks (:2820-2869) ------------------------------------------------------------------------

    /** {@code quickSpaceCheck} (:2820-2829): 12x12 air on Y+4, sampled every 3 columns. */
    private static boolean quickSpaceCheck(final SurfaceProbe p, final int posX, final int posY, final int posZ) {
        return p.areaIsAir(posX, posY + 4, posZ, -2, 10, -2, 10, 3);
    }

    /** {@code quickBigSpaceCheck} (:2831-2840): 30x30 air on Y+8, sampled every 4 columns. */
    private static boolean quickBigSpaceCheck(final SurfaceProbe p, final int posX, final int posY, final int posZ) {
        return p.areaIsAir(posX, posY + 8, posZ, -5, 25, -5, 25, 4);
    }

    /** {@code quickReallyBigSpaceCheck} (:2842-2851): 60x60 air on Y+8, sampled every 8 columns. */
    private static boolean quickReallyBigSpaceCheck(final SurfaceProbe p, final int posX, final int posY, final int posZ) {
        return p.areaIsAir(posX, posY + 8, posZ, -5, 55, -5, 55, 8);
    }

    /** {@code D4BigSpaceCheck} (:2853-2869): 65x55 on Y+4, log, apple and scary leaves tolerated; every 8 columns. */
    private static boolean d4BigSpaceCheck(final SurfaceProbe p, final int posX, final int posY, final int posZ) {
        return p.areaIsAir(posX, posY + 4, posZ, -25, 40, -25, 30, 8,
                Blocks.OAK_LOG, ModBlocks.LEAVES_APPLE.get(), ModBlocks.LEAVES_SCARY.get());
    }

    // -------------------------------------------------------------------------------------------------------
    // builders
    // -------------------------------------------------------------------------------------------------------

    private static void registerBuilders() {
        // Trees, BasiliskMaze and the Magic Apple giants (w12-trees-mazes).
        TreeBuilders.bootstrap();
        // addD4RubyDungeon (:2301): OreSpawnMain.RubyDungeon.makeDungeon(world, posX, posY, posZ)
        LegacyStructureRegistry.registerBuilder(RUBY_BIRD_DUNGEON,
                (world, random, x, y, z, variant) -> RubyBirdDungeon.makeDungeon(LegacyWriter.of(world), random, x, y, z));

        // GenericDungeon (W13): the three ported parts register one builder per id. dungeon-c also registers the Queen
        // castle (GenericDungeonA.MAKE_ENORMOUS_CASTLE_Q, reached from MAKE_ENORMOUS_CASTLE variant 0) and the
        // MAKE_KING_ALTAR dispatcher (variant 0 makeKingAltar, variant 1 makeQueenAltar).
        com.swbr.orespawn.world.dungeon.a.GenericDungeonA.bootstrap();
        com.swbr.orespawn.world.dungeon.b.GenericDungeonB.bootstrap();
        com.swbr.orespawn.world.dungeon.c.GenericDungeonCBuilders.bootstrap();
        // Fallback no-op for ids no W13 part registered; after the three bootstraps above it fills nothing, and a
        // non-zero count is logged as a warning.
        final String[] w13 = {
                MAKE_DUNGEON, MAKE_PLAY_POOL, MAKE_WATER_DRAGON_LAIR, MAKE_GOLD_FISH_BOWL, MAKE_GIRLFRIEND_ISLAND,
                MAKE_MONSTER_ISLAND, MAKE_FROG_POND, MAKE_SMALL_BEE_HIVE, MAKE_MANTIS_HIVE, MAKE_HAUNTED_HOUSE,
                MAKE_LEAF_MONSTER_DUNGEON, MAKE_SPIT_BUG_LAIR, MAKE_IGLOO, MAKE_BOUNCY_CASTLE, MAKE_RUBBER_DUCKY_POND,
                MAKE_ENDER_KNIGHT_DUNGEON, MAKE_ENDER_REAPER_GRAVEYARD, MAKE_ENDER_DRAGON_HOSPITAL, MAKE_ENDER_CASTLE,
                MAKE_KING_ALTAR, MAKE_KYUUBI_DUNGEON, MAKE_BEE_HIVE, MAKE_SHADOW_DUNGEON, MAKE_ALIEN_WTF_DUNGEON,
                MAKE_LEON_NEST, MAKE_DAMSEL_IN_DISTRESS, MAKE_SPIDER_HANGOUT, MAKE_RED_ANT_HANGOUT,
                MAKE_ENORMOUS_CASTLE, MAKE_INCA_PYRAMID, MAKE_ROBOT_LAB, MAKE_MINI_DUNGEON, MAKE_CEPHADROME_ALTAR,
                MAKE_GREENHOUSE_DUNGEON, MAKE_NIGHTMARE_ROOKERY, MAKE_STINKY_HOUSE, MAKE_WHITE_HOUSE, MAKE_PUMPKIN,
                MAKE_RAINBOW, MAKE_CLOUD_SHARK_DUNGEON, MAKE_ROTATOR_STATION, MAKE_URCHIN_SPAWNER,
                MAKE_CRYSTAL_HAUNTED_HOUSE, MAKE_ROUND_ROTATOR, MAKE_CRYSTAL_BATTLE_TOWER};
        int fallbacks = 0;
        for (final String id : w13) {
            if (LegacyStructureRegistry.builder(id).isEmpty()) {
                LegacyStructureRegistry.registerBuilder(id, (world, random, x, y, z, variant) -> {
                });
                ++fallbacks;
            }
        }
        if (fallbacks != 0) {
            OreSpawn.LOG.warn("OreSpawnWorld: {} GenericDungeon builder ids have no builder", fallbacks);
        }
    }

}
