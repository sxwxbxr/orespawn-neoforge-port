package com.swbr.orespawn.gametest;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.ore.OreTitanium;
import com.swbr.orespawn.entity.rock.EntityThrownRock;
import com.swbr.orespawn.entity.vehicle.Elevator;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.world.dimension.chaos.ChunkProviderOreSpawn6;
import com.swbr.orespawn.world.dimension.crystal.ChunkProviderOreSpawn5;
import com.swbr.orespawn.world.dimension.danger.ChunkProviderOreSpawn4;
import com.swbr.orespawn.world.dimension.mining.ChunkProviderOreSpawn2;
import com.swbr.orespawn.world.dimension.utopia.ChunkProviderOreSpawn;
import com.swbr.orespawn.world.dimension.village.ChunkProviderOreSpawn3;
import com.swbr.orespawn.world.gen.MapGenStronghold;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * W05 (DECISIONS R13, R17, R20): the six dimensions decode, load as levels and generate; the Crystal maze
 * lies on Y 24-28; every portal creature takes a player into its dimension and home again under the
 * original conditions; the four flyers spawn and tick; and the R20 cross-cuts (NoStepTrigger, enchantable
 * tags, {@code Mth.floor} below Y 0) hold on a real server.
 *
 * <p><b>Why the dimension levels are built by the test.</b> The vanilla {@code GameTestServer} bakes the
 * FLAT preset against an <i>empty</i> {@code LEVEL_STEM} registry (GameTestServer.java:97-103), so datapack
 * dimensions never become levels there and {@code server.getLevel(orespawn:utopia)} is null whatever the
 * port does. {@link #dimensionLevel} therefore does what {@code MinecraftServer.createLevels} does for
 * every non-overworld stem (MinecraftServer.java:403-426): decode {@code data/orespawn/dimension/<id>.json}
 * with {@code LevelStem.CODEC}, build a {@code ServerLevel} with {@code DerivedLevelData}, the obfuscated
 * seed and the overworld's random sequences, and put it into the server's level map. The level then ticks
 * with the server and generates chunks through the real {@code ChunkMap} pipeline, decoration and structure
 * starts included. Only the protected {@code storageSource} needs reflection.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W05GameTests {

    private static final String ARENA = "arena";

    private W05GameTests() {}

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
    }

    // ------------------------------------------------------------------ dimensions

    /**
     * {@code data/orespawn/dimension/<id>.json} decodes with the registered generator codec, the
     * dimension type spans Y 0..256, the generator is the expected {@code ChunkProviderOreSpawnN}, its
     * terrain stage fills chunk 0,0 without throwing, and the same seed gives the same blocks twice
     * (R18: chunk generation is deterministic). Except Chaos, whose spawn column may be void
     * (verhalten/world-04.md), column 8,8 holds a non-air block.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void sixDimensionsDecodeAndGenerateTheirTerrain(GameTestHelper helper) {
        Map<String, Class<? extends ChunkGenerator>> expected = expectedGenerators();
        MinecraftServer server = helper.getLevel().getServer();
        RegistryAccess registries = server.registryAccess();
        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, Class<? extends ChunkGenerator>> entry : expected.entrySet()) {
            String id = entry.getKey();
            try {
                LevelStem stem = decodeStem(server, id);
                if (stem.type().value().minY() != 0 || stem.type().value().height() != 256) {
                    failures.add(id + ": dimension type spans " + stem.type().value().minY() + " + " + stem.type().value().height());
                }
                ChunkGenerator generator = stem.generator();
                if (generator.getClass() != entry.getValue()) {
                    failures.add(id + ": generator is " + generator.getClass().getName());
                    continue;
                }
                ChunkAccess first = generate(helper, registries, generator);
                ChunkAccess second = generate(helper, registries, generator);
                boolean solid = false;
                int differences = 0;
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (int y = 0; y < 256; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            pos.set(x, y, z);
                            if (first.getBlockState(pos) != second.getBlockState(pos)) {
                                differences++;
                            }
                        }
                    }
                    solid |= !first.getBlockState(pos.set(8, y, 8)).isAir();
                }
                if (differences != 0) {
                    failures.add(id + ": " + differences + " blocks differ between two runs with the same seed");
                }
                if (!solid && !"chaos".equals(id)) {
                    failures.add(id + ": column 8,8 of chunk 0,0 is all air");
                }
            } catch (Exception e) {
                failures.add(id + ": " + e);
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    /**
     * Each of the six ids is in every registry a dimension needs (generator codec, dimension type, the
     * biome of its fixed biome source), a {@code ServerLevel} for it loads, the 3x3 chunks around the
     * arena column generate to {@code FULL} through the real pipeline (noise, surface, carvers,
     * {@code applyBiomeDecoration}, structure starts, light) without an exception, and the level's
     * height is Y 0..256. Except Chaos (floating layers over the void), the centre chunk is not empty.
     */
    @GameTest(template = ARENA, timeoutTicks = 600)
    public static void sixDimensionLevelsLoadAndGenerateFullChunks(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        RegistryAccess registries = server.registryAccess();
        ChunkPos centre = new ChunkPos(helper.absolutePos(BlockPos.ZERO));
        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, Class<? extends ChunkGenerator>> entry : expectedGenerators().entrySet()) {
            String id = entry.getKey();
            ResourceLocation key = rl(id);
            if (!BuiltInRegistries.CHUNK_GENERATOR.containsKey(key)) {
                failures.add(id + ": no chunk generator codec " + key);
            }
            if (!registries.registryOrThrow(Registries.DIMENSION_TYPE).containsKey(key)) {
                failures.add(id + ": no dimension type " + key);
            }
            try {
                ServerLevel level = dimensionLevel(helper, id);
                if (server.getLevel(level.dimension()) != level) {
                    failures.add(id + ": the level is not reachable through server.getLevel");
                }
                if (level.getMinBuildHeight() != 0 || level.getHeight() != 256) {
                    failures.add(id + ": level spans " + level.getMinBuildHeight() + " + " + level.getHeight());
                }
                if (level.getChunkSource().getGenerator().getClass() != entry.getValue()) {
                    failures.add(id + ": level generator is " + level.getChunkSource().getGenerator().getClass().getName());
                }
                String biome = level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream()
                        .map(h -> h.unwrapKey().map(k -> k.location().toString()).orElse("?")).toList().toString();
                if (!biome.contains(OreSpawn.MOD_ID + ":")) {
                    failures.add(id + ": biome source holds " + biome);
                }
                int nonAir = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        LevelChunk chunk = level.getChunk(centre.x + dx, centre.z + dz);
                        if (chunk.getPersistedStatus() != ChunkStatus.FULL) {
                            failures.add(id + ": chunk " + chunk.getPos() + " is " + chunk.getPersistedStatus());
                        }
                        if (dx == 0 && dz == 0) {
                            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                            for (int y = 0; y < 256; y += 2) {
                                for (int x = 0; x < 16; x += 3) {
                                    for (int z = 0; z < 16; z += 3) {
                                        if (!chunk.getBlockState(pos.set(chunk.getPos().getMinBlockX() + x, y,
                                                chunk.getPos().getMinBlockZ() + z)).isAir()) {
                                            nonAir++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (nonAir == 0 && !"chaos".equals(id)) {
                    failures.add(id + ": centre chunk " + centre + " is all air");
                }
            } catch (Exception e) {
                failures.add(id + ": " + e);
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    /**
     * CrystalMaze.java:17-58 through {@code ChunkProviderOreSpawn5.provideChunk} (:211-212), on a
     * {@code FULL} chunk of the loaded Crystal level: {@code buildCrystalMaze(x, 25, z)} puts the bedrock
     * floor on Y 24 (one crystal stone hole), the walls on Y 25..27 ({@code b = 1}: bedrock, three high,
     * the same in every layer, the outer ring opened by {@code openCrystalMaze}) and the bedrock ceiling on
     * Y 28 (up to four crystal stone holes). Y 23 and Y 29 are not a bedrock plane - so the maze is not
     * shifted by {@code min_y} or by a row.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void crystalMazeLiesOnY24To28InAGeneratedCrystalChunk(GameTestHelper helper) {
        ServerLevel crystal = dimensionLevel(helper, "crystal");
        ChunkPos pos = new ChunkPos(helper.absolutePos(BlockPos.ZERO));
        List<String> failures = new ArrayList<>();
        // W07 finding: the test position is random per run, and in 1 of about 60 runs its chunk carried a brown mushroom
        // on the maze floor. That is original decoration, not a maze error: the decorator's unconditional mushroom
        // patches (BiomeDecorator.java, rand.nextInt(4) / nextInt(8), y up to twice the height) keep a mushroom wherever
        // BlockMushroom.canBlockStay holds - light below 13 and an opaque cube below (LegacyWorld.canMushroomStay) - and
        // the dark bedrock floor on Y 24 is one. crystal.json places them with the same rule (MushroomBlock.canSurvive).
        // So a mushroom on Y 25 above bedrock counts as corridor. The chunk that showed it (world seed 0 is fixed in
        // GameTestServer) is checked on every run and must hold at least one, so the exception is exercised, not assumed.
        ChunkPos mushroomChunk = new ChunkPos(-566658, -685935);
        int mushroomsInKnownChunk = 0;
        for (ChunkPos cp : List.of(pos, new ChunkPos(pos.x + 1, pos.z - 1), mushroomChunk)) {
            LevelChunk chunk = crystal.getChunk(cp.x, cp.z);
            int bx = cp.getMinBlockX();
            int bz = cp.getMinBlockZ();
            int[] bedrock = new int[256];
            int[] other = new int[256];
            int walls = 0;
            int floorMushrooms = 0;
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
            for (int y = 22; y <= 30; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState s = chunk.getBlockState(m.set(bx + x, y, bz + z));
                        if (s.is(Blocks.BEDROCK)) {
                            bedrock[y]++;
                        } else if (!s.is(ModBlocks.CRYSTALSTONE.get())) {
                            other[y]++;
                        }
                    }
                }
            }
            // Floor and ceiling: bedrock except the holes; nothing but bedrock and crystal stone.
            if (bedrock[24] < 255 || other[24] != 0) {
                failures.add(cp + " Y24: " + bedrock[24] + " bedrock, " + other[24] + " other");
            }
            if (bedrock[28] < 252 || other[28] != 0) {
                failures.add(cp + " Y28: " + bedrock[28] + " bedrock, " + other[28] + " other");
            }
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockState s25 = chunk.getBlockState(m.set(bx + x, 25, bz + z));
                    if ((s25.is(Blocks.BROWN_MUSHROOM) || s25.is(Blocks.RED_MUSHROOM))
                            && chunk.getBlockState(m.set(bx + x, 24, bz + z)).is(Blocks.BEDROCK)) {
                        floorMushrooms++;
                        s25 = Blocks.AIR.defaultBlockState();
                    }
                    // W12: OreSpawnWorld.addCrystalChestsAndSpawners (OreSpawnWorld.java:1780-1844) puts a chest or a
                    // spawner (Dungeon Beast / Rat) into a free corridor cell on Y 25 - original decoration, not maze.
                    if ((s25.is(Blocks.CHEST) || s25.is(Blocks.SPAWNER))
                            && chunk.getBlockState(m.set(bx + x, 24, bz + z)).is(Blocks.BEDROCK)) {
                        s25 = Blocks.AIR.defaultBlockState();
                    }
                    boolean ring = x == 0 || z == 0 || x == 15 || z == 15;
                    for (int y = 25; y <= 27; y++) {
                        BlockState s = y == 25 ? s25 : chunk.getBlockState(m.set(bx + x, y, bz + z));
                        if (!s.isAir() && !s.is(Blocks.BEDROCK)) {
                            failures.add(cp + " maze block at " + x + "," + y + "," + z + " is " + s);
                        } else if (ring && !s.isAir()) {
                            failures.add(cp + " outer ring at " + x + "," + y + "," + z + " is closed");
                        } else if (s != s25) {
                            failures.add(cp + " wall at " + x + "," + z + " differs between Y25 and Y" + y);
                        }
                    }
                    if (s25.is(Blocks.BEDROCK)) {
                        walls++;
                    }
                }
            }
            if (walls < 16) {
                failures.add(cp + ": only " + walls + " wall columns in Y25..27");
            }
            if (bedrock[23] > 64 || bedrock[29] > 64) {
                failures.add(cp + ": bedrock plane shifted, Y23 " + bedrock[23] + ", Y29 " + bedrock[29]);
            }
            if (cp.equals(mushroomChunk)) {
                mushroomsInKnownChunk = floorMushrooms;
            }
        }
        if (mushroomsInKnownChunk == 0) {
            failures.add(0, mushroomChunk + " holds no mushroom on the maze floor any more - the decoration exception is no longer exercised");
        }
        if (failures.size() > 8) {
            failures = new ArrayList<>(failures.subList(0, 8));
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    // ------------------------------------------------------------------ portal creatures

    /** EntityAnt.java:65-87: Brown Ant to Utopia and back, only with an empty hand. */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void brownAntTakesAnEmptyHandedPlayerToUtopiaAndBack(GameTestHelper helper) {
        portalRoundTrip(helper, ModEntities.ANT.get(), "utopia");
    }

    /** EntityRedAnt.java:59-77: Red Ant to the Mining dimension and back. */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void redAntTakesAnEmptyHandedPlayerToMiningAndBack(GameTestHelper helper) {
        portalRoundTrip(helper, ModEntities.RED_ANT.get(), "mining");
    }

    /** EntityRainbowAnt.java:34-52: Rainbow Ant to VillageMania and back. */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void rainbowAntTakesAnEmptyHandedPlayerToVillageManiaAndBack(GameTestHelper helper) {
        portalRoundTrip(helper, ModEntities.RAINBOW_ANT.get(), "village");
    }

    /** EntityUnstableAnt.java:34-52: Unstable Ant to Islands (orespawn:danger) and back. */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void unstableAntTakesAnEmptyHandedPlayerToIslandsAndBack(GameTestHelper helper) {
        portalRoundTrip(helper, ModEntities.UNSTABLE_ANT.get(), "danger");
    }

    /** EntityButterfly.java:249-271: Butterfly to Chaos and back. */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void butterflyTakesAnEmptyHandedPlayerToChaosAndBack(GameTestHelper helper) {
        portalRoundTrip(helper, ModEntities.BUTTERFLY.get(), "chaos");
    }

    /**
     * Termite.java:65-101: to the Crystal dimension only with an empty hand, an empty inventory and no
     * armour - each refusal leaves the player in the Overworld. Home again needs only the empty hand:
     * the player puts armour on in the Crystal dimension and still travels back.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void termiteRefusesArmourAndAFullInventoryAndTakesAnEmptyPlayerToCrystal(GameTestHelper helper) {
        ServerLevel crystal = dimensionLevel(helper, "crystal");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            Vec3 start = helper.absoluteVec(new Vec3(4.5, 2.0, 4.5));
            player.moveTo(start.x, start.y, start.z, 0.0F, 0.0F);
            Mob termite = helper.spawnWithNoFreeWill(ModEntities.TERMITE.get(), new BlockPos(4, 2, 6));

            player.getInventory().armor.set(2, new ItemStack(Items.IRON_CHESTPLATE));
            player.interactOn(termite, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.level() == helper.getLevel(), "the termite took a player wearing a chestplate to "
                    + player.level().dimension().location());

            player.getInventory().armor.set(2, ItemStack.EMPTY);
            player.getInventory().items.set(20, new ItemStack(Items.DIRT));
            player.interactOn(termite, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.level() == helper.getLevel(), "the termite took a player with dirt in the inventory");

            player.getInventory().items.set(20, ItemStack.EMPTY);
            player.getInventory().offhand.set(0, new ItemStack(Items.DIRT));
            player.interactOn(termite, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.level() == helper.getLevel(), "the termite took a player with dirt in the off hand");

            player.getInventory().offhand.set(0, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            player.interactOn(termite, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.level() == helper.getLevel(), "the termite took a player holding a stick");

            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.interactOn(termite, InteractionHand.MAIN_HAND);
            assertLanded(helper, player, crystal, true);

            player.getInventory().armor.set(2, new ItemStack(Items.IRON_CHESTPLATE));
            player.getInventory().items.set(20, new ItemStack(Items.DIRT));
            Mob there = spawnNextTo(ModEntities.TERMITE.get(), crystal, player);
            try {
                player.interactOn(there, InteractionHand.MAIN_HAND);
            } finally {
                there.discard();
            }
            helper.assertTrue(player.level() == helper.getLevel(), "the way home from Crystal with armour and an inventory "
                    + "ended in " + player.level().dimension().location() + ", expected the Overworld (only the hand is checked)");
        } finally {
            player.remove(Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    /**
     * The shared ant/butterfly contract: a held item refuses; an empty hand takes the player into the
     * dimension onto a solid block under two air blocks ({@code OreSpawnTeleporter.justPutMe}); the same
     * creature in that dimension takes the player back to the Overworld.
     */
    @SuppressWarnings("removal")
    private static void portalRoundTrip(GameTestHelper helper, EntityType<? extends Mob> type, String id) {
        ServerLevel target = dimensionLevel(helper, id);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            Vec3 start = helper.absoluteVec(new Vec3(4.5, 2.0, 4.5));
            player.moveTo(start.x, start.y, start.z, 0.0F, 0.0F);
            Mob here = helper.spawnWithNoFreeWill(type, new BlockPos(4, 2, 6));

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            player.interactOn(here, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.level() == helper.getLevel(), type.toShortString() + " took a player holding a stick to "
                    + player.level().dimension().location());

            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.interactOn(here, InteractionHand.MAIN_HAND);
            assertLanded(helper, player, target, !"chaos".equals(id));

            Mob there = spawnNextTo(type, target, player);
            try {
                player.interactOn(there, InteractionHand.MAIN_HAND);
            } finally {
                there.discard();
            }
            helper.assertTrue(player.level() == helper.getLevel(), type.toShortString() + " in " + id
                    + " sent the player to " + player.level().dimension().location() + ", expected the Overworld");
        } finally {
            player.remove(Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    private static Mob spawnNextTo(EntityType<? extends Mob> type, ServerLevel level, ServerPlayer player) {
        Mob mob = type.create(level);
        if (mob == null) {
            throw new IllegalStateException(type.toShortString() + " could not be created in " + level.dimension().location());
        }
        mob.moveTo(player.getX(), player.getY(), player.getZ(), 0.0F, 0.0F);
        mob.setNoAi(true);
        level.addFreshEntity(mob);
        return mob;
    }

    /**
     * The player is in {@code target}. With {@code onGround}: the searched column (the landing is
     * {@code posX +- 0.5}, OreSpawnTeleporter.java:120-128) has a solid block under the landing Y and air
     * one above, which is the {@code found = 1} exit of the search.
     */
    private static void assertLanded(GameTestHelper helper, ServerPlayer player, ServerLevel target, boolean onGround) {
        helper.assertTrue(player.level() == target, "the player is in " + player.level().dimension().location()
                + ", expected " + target.dimension().location());
        if (!onGround) {
            return;
        }
        int colX = player.getX() < 0.0 ? (int) (player.getX() + 0.5) : (int) (player.getX() - 0.5);
        int colZ = player.getZ() < 0.0 ? (int) (player.getZ() + 0.5) : (int) (player.getZ() - 0.5);
        int y = (int) player.getY();
        BlockState below = target.getBlockState(new BlockPos(colX, y - 1, colZ));
        BlockState above = target.getBlockState(new BlockPos(colX, y + 1, colZ));
        helper.assertTrue(below.isSolid() && above.isAir(), "landed in " + target.dimension().location() + " at "
                + player.position() + " over " + below + " under " + above + ", expected solid ground and air");
    }

    // ------------------------------------------------------------------ flyers

    /** The nine living W05 types spawn through their registry factory and are alive after 100 ticks. */
    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void livingW05EntitiesSpawnAndTickOneHundredTicks(GameTestHelper helper) {
        List<Entity> living = List.of(
                helper.spawn(ModEntities.ANT.get(), new BlockPos(2, 2, 2)),
                helper.spawn(ModEntities.RED_ANT.get(), new BlockPos(5, 2, 2)),
                helper.spawn(ModEntities.RAINBOW_ANT.get(), new BlockPos(8, 2, 2)),
                helper.spawn(ModEntities.UNSTABLE_ANT.get(), new BlockPos(11, 2, 2)),
                helper.spawn(ModEntities.TERMITE.get(), new BlockPos(2, 2, 6)),
                helper.spawn(ModEntities.BUTTERFLY.get(), new BlockPos(5, 3, 6)),
                helper.spawn(ModEntities.MOTH.get(), new BlockPos(8, 3, 6)),
                helper.spawn(ModEntities.MOSQUITO.get(), new BlockPos(11, 3, 6)),
                helper.spawn(ModEntities.FIREFLY.get(), new BlockPos(6, 3, 10)));
        helper.runAfterDelay(100, () -> {
            for (Entity e : living) {
                helper.assertTrue(e.isAlive(), e.getType().toShortString() + " did not survive 100 ticks");
                helper.assertTrue(e.tickCount >= 100, e.getType().toShortString() + " ticked only " + e.tickCount + " times");
            }
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------ R20

    /**
     * R20 {@code canTriggerWalking}: Elevator.java:136 answers {@code false}, so the hoverboard
     * ({@code NoStepTrigger}) landing on titanium ore does not run {@code onEntityWalking} and the ore does
     * not glow (OreTitanium.java:38-41, 48-52); a walking player landing on the same ore does. Both go
     * through the real {@code Entity.move}, which calls {@code stepOn} once the move ends on the ground.
     * The glow flag is the block singleton's field (R18), read by reflection because only the client's
     * {@code animateTick} consumes it.
     */
    @GameTest(template = ARENA)
    public static void hoverboardOnTitaniumOreDoesNotGlowItButAWalkingPlayerDoes(GameTestHelper helper) {
        BlockPos ore = new BlockPos(3, 1, 3);
        OreTitanium block = ModBlocks.ORETITANIUM.get();
        helper.setBlock(ore, block);
        Field glowing = declaredField(OreTitanium.class, "glowing");
        setBoolean(glowing, block, false);

        Elevator board = helper.spawn(ModEntities.HOVERBOARD.get(), new Vec3(3.5, 2.05, 3.5));
        board.move(MoverType.SELF, new Vec3(0.0, -0.2, 0.0));
        helper.assertTrue(board.onGround(), "the hoverboard did not land on the ore (y " + helper.relativeVec(board.position()).y
                + "), so stepOn was never asked");
        helper.assertTrue(!getBoolean(glowing, block), "the hoverboard made titanium ore glow (NoStepTrigger ignored)");
        board.discard();

        Player walker = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(new Vec3(3.5, 2.05, 3.5));
        walker.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        walker.move(MoverType.SELF, new Vec3(0.0, -0.2, 0.0));
        helper.assertTrue(walker.onGround(), "the player did not land on the ore");
        helper.assertTrue(getBoolean(glowing, block), "a walking player did not make titanium ore glow");
        setBoolean(glowing, block, false);
        walker.discard();
        helper.succeed();
    }

    /**
     * R20 enchantability at the enchanting table, one item per 1.7.10 base class: {@code ItemSword}
     * (Emerald Sword), {@code ItemTool} (Ruby Pickaxe), {@code ItemArmor} (Emerald Chestplate) and the
     * {@code Item}-based Ultimate Bow. The candidate set is exactly what the table filters -
     * {@code #minecraft:in_enchanting_table} through {@code getAvailableEnchantmentResults} (NeoForge
     * {@code isPrimaryItemFor}) - over every cost; the Ultimate Bow may be offered Unbreaking and nothing
     * else. Each item then goes through a real {@link EnchantmentMenu} with 32 bookshelves and lapis:
     * the third button is priced and the enchantments it applies come from the candidate set.
     */
    @GameTest(template = ARENA)
    public static void swordToolArmourAndUltimateBowAreEnchantableByTheirBaseClassTags(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HolderSet.Named<Enchantment> table = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getTag(EnchantmentTags.IN_ENCHANTING_TABLE).orElseThrow();
        List<String> failures = new ArrayList<>();

        check(failures, table, new ItemStack(ModItems.EMERALD_SWORD.get()),
                Set.of(Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK,
                        Enchantments.FIRE_ASPECT, Enchantments.LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.UNBREAKING),
                Set.of(Enchantments.EFFICIENCY, Enchantments.PROTECTION, Enchantments.POWER), false);
        check(failures, table, new ItemStack(ModItems.RUBY_PICKAXE.get()),
                Set.of(Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH, Enchantments.UNBREAKING),
                Set.of(Enchantments.SHARPNESS, Enchantments.PROTECTION, Enchantments.POWER), false);
        check(failures, table, new ItemStack(ModItems.EMERALD_CHEST.get()),
                Set.of(Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION,
                        Enchantments.PROJECTILE_PROTECTION, Enchantments.THORNS, Enchantments.UNBREAKING),
                Set.of(Enchantments.SHARPNESS, Enchantments.EFFICIENCY, Enchantments.POWER), false);
        check(failures, table, new ItemStack(ModItems.ULTIMATE_BOW.get()),
                Set.of(Enchantments.UNBREAKING), Set.of(), true);

        ItemStack bow = new ItemStack(ModItems.ULTIMATE_BOW.get());
        if (!bow.is(ItemTags.DURABILITY_ENCHANTABLE) || !bow.is(ItemTags.VANISHING_ENCHANTABLE) || bow.is(ItemTags.BOW_ENCHANTABLE)) {
            failures.add("ultimatebow tags: durability " + bow.is(ItemTags.DURABILITY_ENCHANTABLE) + ", vanishing "
                    + bow.is(ItemTags.VANISHING_ENCHANTABLE) + ", bow " + bow.is(ItemTags.BOW_ENCHANTABLE));
        }

        // The real table.
        BlockPos tableRel = new BlockPos(7, 2, 7);
        helper.setBlock(tableRel, Blocks.ENCHANTING_TABLE);
        BlockPos tableAbs = helper.absolutePos(tableRel);
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            level.setBlockAndUpdate(tableAbs.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (dx != 0 || dz != 0 || dy != 0) {
                        level.setBlockAndUpdate(tableAbs.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        // makeMockPlayer only overrides isCreative(); the menu asks experienceLevel and abilities.instabuild
        // (EnchantmentMenu.java:161-163), so the enchanter pays with real levels.
        Player enchanter = helper.makeMockPlayer(GameType.SURVIVAL);
        enchanter.experienceLevel = 100;
        for (ItemStack stack : List.of(new ItemStack(ModItems.EMERALD_SWORD.get()), new ItemStack(ModItems.RUBY_PICKAXE.get()),
                new ItemStack(ModItems.EMERALD_CHEST.get()), new ItemStack(ModItems.ULTIMATE_BOW.get()))) {
            String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            Set<ResourceKey<Enchantment>> offered = offered(table, stack);
            EnchantmentMenu menu = new EnchantmentMenu(0, enchanter.getInventory(), ContainerLevelAccess.create(level, tableAbs));
            menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
            menu.getSlot(0).set(stack);
            if (menu.costs[2] <= 0) {
                failures.add(name + ": the table priced the third option at " + menu.costs[2]);
                continue;
            }
            if (!menu.clickMenuButton(enchanter, 2)) {
                failures.add(name + ": the table refused the third option (cost " + menu.costs[2] + ")");
                continue;
            }
            ItemStack result = menu.getSlot(0).getItem();
            Set<ResourceKey<Enchantment>> applied = new HashSet<>();
            for (Holder<Enchantment> h : result.getEnchantments().keySet()) {
                applied.add(h.unwrapKey().orElseThrow());
            }
            if (applied.isEmpty() || !offered.containsAll(applied)) {
                failures.add(name + ": the table applied " + names(applied) + ", candidates " + names(offered));
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    private static Set<ResourceKey<Enchantment>> offered(HolderSet.Named<Enchantment> table, ItemStack stack) {
        Set<ResourceKey<Enchantment>> offered = new HashSet<>();
        for (int cost = 1; cost <= 100; cost++) {
            for (EnchantmentInstance instance : EnchantmentHelper.getAvailableEnchantmentResults(cost, stack, table.stream())) {
                offered.add(instance.enchantment.unwrapKey().orElseThrow());
            }
        }
        return offered;
    }

    private static void check(List<String> failures, HolderSet.Named<Enchantment> table, ItemStack stack,
                              Set<ResourceKey<Enchantment>> required, Set<ResourceKey<Enchantment>> forbidden, boolean exact) {
        String name = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (!stack.isEnchantable() || stack.getEnchantmentValue() <= 0) {
            failures.add(name + ": isEnchantable " + stack.isEnchantable() + ", enchantment value " + stack.getEnchantmentValue());
            return;
        }
        Set<ResourceKey<Enchantment>> offered = offered(table, stack);
        if (!offered.containsAll(required) || forbidden.stream().anyMatch(offered::contains) || (exact && !offered.equals(required))) {
            failures.add(name + ": offered " + names(offered) + ", required " + names(required)
                    + (exact ? " exactly" : ", forbidden " + names(forbidden)));
        }
    }

    private static String names(Set<ResourceKey<Enchantment>> keys) {
        return keys.stream().map(k -> k.location().getPath()).sorted().toList().toString();
    }

    /**
     * R20 {@code Mth.floor}: EntityThrownRock.java:299-301 read the block under the rock for its skip off
     * water. The arena lies below Y 0; the rock flies inside a water block row whose upper neighbour is
     * air. {@code Mth.floor(y)} reads the water and mirrors the falling motion; {@code (int) y} would read
     * the air row above and let the rock sink. One tick, driven by hand so no other tick interferes: water
     * drag 0.8 (LegacyThrowable.java:227-235) turns (1.0, -0.3, 0) into (0.8, -0.27, 0), inside the skip
     * window (-0.55, -0.15) with horizontal speed squared 0.64 above 0.5, so y becomes +0.2025.
     */
    @GameTest(template = ARENA)
    public static void thrownRockBelowYZeroReadsTheWaterRowItFliesIn(GameTestHelper helper) {
        for (int x = 4; x <= 8; x++) {
            helper.setBlock(new BlockPos(x, 2, 5), Blocks.WATER);
            helper.setBlock(new BlockPos(x, 3, 5), Blocks.AIR);
        }
        Vec3 start = helper.absoluteVec(new Vec3(4.5, 2.5, 5.5));
        helper.assertTrue(start.y < 0.0, "the arena is at y " + start.y + ", the test needs a negative y");
        helper.assertTrue((int) start.y != net.minecraft.util.Mth.floor(start.y), "(int) and floor agree at y " + start.y);
        EntityThrownRock rock = new EntityThrownRock(helper.getLevel(), start.x, start.y, start.z);
        rock.setRockType(1);
        rock.setDeltaMovement(1.0, -0.3, 0.0);
        rock.tick();
        Vec3 motion = rock.getDeltaMovement();
        helper.assertTrue(motion.y > 0.0, "the rock at y " + start.y + " did not skip off the water row: motion " + motion);
        helper.assertTrue(Math.abs(motion.y - 0.2025) < 0.01 && Math.abs(motion.x - 0.6) < 0.01,
                "skip motion " + motion + ", expected (0.6, 0.2025, 0) from the water drag and the 3/4 mirror");
        rock.discard();
        helper.succeed();
    }

    // ------------------------------------------------------------------ strongholds and the clock

    /**
     * ChunkProviderOreSpawn2.java:54 and ChunkProviderOreSpawn3.java:48 create the default 1.7.10
     * {@code MapGenStronghold}: three positions, distance 32, spread 3. Checked here:
     * <ul>
     * <li>The ring algorithm against values from an independent Python reimplementation of {@code aug.a(II)Z},
     * {@code WorldChunkManagerHell.findBiomePosition} and {@code java.util.Random} for three seeds.</li>
     * <li>In the Mining and VillageMania levels {@code orespawn:stronghold} has exactly one placement, the
     * {@link MapGenStronghold}, and the vanilla {@code minecraft:stronghold} (128 positions) has none.</li>
     * <li>The three ring chunks are structure chunks for the level seed.</li>
     * <li>The Eye of Ender search (tag {@code eye_of_ender_located}) through the generator answers with
     * {@code orespawn:stronghold} in the ring chunk nearest to the origin.</li>
     * </ul>
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void miningAndVillageManiaHaveTheThreeStrongholdRingsOfMapGenStronghold(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        Map<Long, List<ChunkPos>> expected = new LinkedHashMap<>();
        expected.put(0L, List.of(new ChunkPos(0, -46), new ChunkPos(50, 18), new ChunkPos(-37, 25)));
        expected.put(203L, List.of(new ChunkPos(0, -59), new ChunkPos(66, 25), new ChunkPos(-64, 45)));
        expected.put(-4172144997902289642L, List.of(new ChunkPos(-15, 35), new ChunkPos(-35, -55), new ChunkPos(59, -6)));
        MapGenStronghold reference = new MapGenStronghold(32.0, 3, 3);
        for (Map.Entry<Long, List<ChunkPos>> entry : expected.entrySet()) {
            List<ChunkPos> actual = reference.structureCoords(entry.getKey());
            if (!actual.equals(entry.getValue())) {
                failures.add("seed " + entry.getKey() + ": rings " + actual + ", 1.7.10 " + entry.getValue());
            }
        }

        MinecraftServer server = helper.getLevel().getServer();
        Registry<Structure> structures = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Holder<Structure> ours = structures.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE, rl("stronghold")));
        Holder<Structure> vanilla = structures.getHolderOrThrow(BuiltinStructures.STRONGHOLD);
        HolderSet<Structure> eyeTargets = structures.getTag(StructureTags.EYE_OF_ENDER_LOCATED).orElseThrow();
        if (!eyeTargets.contains(ours)) {
            failures.add("orespawn:stronghold is not in #minecraft:eye_of_ender_located");
        }
        for (String id : List.of("mining", "village")) {
            ServerLevel level = dimensionLevel(helper, id);
            ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
            List<StructurePlacement> placements = state.getPlacementsForStructure(ours);
            if (placements.size() != 1 || !(placements.get(0) instanceof MapGenStronghold stronghold)) {
                failures.add(id + ": placements of orespawn:stronghold are " + placements);
                continue;
            }
            if (!state.getPlacementsForStructure(vanilla).isEmpty()) {
                failures.add(id + ": the vanilla stronghold set still applies: " + state.getPlacementsForStructure(vanilla));
            }
            List<ChunkPos> rings = stronghold.structureCoords(state.getLevelSeed());
            if (!rings.equals(reference.structureCoords(state.getLevelSeed())) || rings.size() != 3) {
                failures.add(id + ": rings " + rings + " for seed " + state.getLevelSeed());
            }
            for (ChunkPos ring : rings) {
                if (!stronghold.isStructureChunk(state, ring.x, ring.z)) {
                    failures.add(id + ": ring chunk " + ring + " is no structure chunk");
                }
            }
            BlockPos from = new BlockPos(0, 64, 0);
            ChunkPos nearestRing = rings.stream()
                    .min(Comparator.comparingDouble(c -> stronghold.getLocatePos(c).distSqr(from))).orElseThrow();
            Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, eyeTargets, from, 100, false);
            if (found == null) {
                failures.add(id + ": the Eye of Ender search found nothing");
            } else if (!new ChunkPos(found.getFirst()).equals(nearestRing) || !found.getSecond().equals(ours)) {
                failures.add(id + ": the Eye of Ender search found " + found.getFirst() + " " + found.getSecond()
                        + ", nearest ring chunk is " + nearestRing);
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    /**
     * {@code WorldProviderOreSpawnN.setWorldTime} is not ported (see {@code LegacyWorldProvider}): sleeping
     * never reached its night skip in 1.7.10. Replays what {@code ServerLevel.tick} does when everyone in the
     * level sleeps (ServerLevel.java:348-353) for each of the six levels at overworld time 13000: the
     * {@code SleepFinishedTimeEvent} keeps its new time, {@code setDayTime} on the derived level changes
     * nothing, and the overworld clock stays at 13000.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void sleepingThroughTheNightInAnOreSpawnDimensionMovesNoClock(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerLevel overworld = server.overworld();
        long saved = overworld.getDayTime();
        List<String> failures = new ArrayList<>();
        try {
            for (String id : expectedGenerators().keySet()) {
                ServerLevel level = dimensionLevel(helper, id);
                overworld.setDayTime(13000L);
                long j = level.getDayTime() + 24000L;
                long dawn = j - j % 24000L;
                long newTime = EventHooks.onSleepFinished(level, dawn, level.getDayTime());
                level.setDayTime(newTime);
                if (newTime != dawn) {
                    failures.add(id + ": SleepFinishedTimeEvent changed the new time to " + newTime);
                }
                if (overworld.getDayTime() != 13000L) {
                    failures.add(id + ": the overworld clock moved to " + overworld.getDayTime());
                }
                if (level.getDayTime() != 13000L) {
                    failures.add(id + ": the dimension clock reads " + level.getDayTime());
                }
            }
        } finally {
            overworld.setDayTime(saved);
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    // ------------------------------------------------------------------ back-edges

    /** No {@code PORT: TODO} addressed to W05 is left anywhere under {@code src/} (same walk as W04). */
    @GameTest(template = ARENA)
    public static void noTodoForWaveFiveRemainsInTheSourceTree(GameTestHelper helper) {
        Path src = null;
        for (Path dir = Paths.get("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            if (Files.isDirectory(dir.resolve("src/main/java/com/swbr/orespawn"))) {
                src = dir.resolve("src");
                break;
            }
        }
        if (src == null) {
            helper.fail("source tree not found above " + Paths.get("").toAbsolutePath());
            return;
        }
        String marker = "TODO " + "W05"; // split so this file does not match itself
        List<String> found = new ArrayList<>();
        Path root = src;
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(f -> f.toString().matches(".*\\.(java|json|toml|mcmeta|md|txt|snbt)$")).toList()) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains(marker)) {
                        found.add(root.relativize(file) + ":" + (i + 1));
                    }
                }
            }
        } catch (IOException e) {
            helper.fail("reading the source tree failed: " + e);
        }
        if (!found.isEmpty()) {
            helper.fail(found.size() + " '" + marker + "' left: " + found);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers

    private static Map<String, Class<? extends ChunkGenerator>> expectedGenerators() {
        Map<String, Class<? extends ChunkGenerator>> expected = new LinkedHashMap<>();
        expected.put("utopia", ChunkProviderOreSpawn.class);
        expected.put("mining", ChunkProviderOreSpawn2.class);
        expected.put("village", ChunkProviderOreSpawn3.class);
        expected.put("danger", ChunkProviderOreSpawn4.class);
        expected.put("crystal", ChunkProviderOreSpawn5.class);
        expected.put("chaos", ChunkProviderOreSpawn6.class);
        return expected;
    }

    /** {@code data/orespawn/dimension/<id>.json} through {@code LevelStem.CODEC} over the server registries. */
    static LevelStem decodeStem(MinecraftServer server, String id) throws IOException {
        JsonElement json;
        try (BufferedReader reader = server.getResourceManager().openAsReader(rl("dimension/" + id + ".json"))) {
            json = JsonParser.parseReader(reader);
        }
        DataResult<LevelStem> decoded = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, server.registryAccess()), json);
        return decoded.getOrThrow(message -> new IllegalStateException("dimension/" + id + ".json: " + message));
    }

    /** One fresh proto chunk at 0,0 through {@code createState} and {@code fillFromNoise}, seed fixed. */
    private static ChunkAccess generate(GameTestHelper helper, RegistryAccess registries, ChunkGenerator generator) {
        long seed = 20_3L;
        RandomState randomState = RandomState.create(registries.asGetterLookup(), NoiseGeneratorSettings.OVERWORLD, seed);
        generator.createState(registries.lookupOrThrow(Registries.STRUCTURE_SET), randomState, seed);
        ProtoChunk chunk = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, LevelHeightAccessor.create(0, 256),
                registries.registryOrThrow(Registries.BIOME), null);
        return generator.fillFromNoise(Blender.empty(), randomState, helper.getLevel().structureManager(), chunk).join();
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

    /**
     * The level of {@code orespawn:<id>}, created the way {@code MinecraftServer.createLevels} creates every
     * non-overworld stem (see the class comment) the first time a test asks for it. All tests run on the
     * server thread, so the check-then-put needs no lock; {@code markWorldsDirty} makes the tick loop pick
     * the level up.
     */
    @SuppressWarnings("deprecation")
    static ServerLevel dimensionLevel(GameTestHelper helper, String id) {
        MinecraftServer server = helper.getLevel().getServer();
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, rl(id));
        ServerLevel existing = server.getLevel(key);
        if (existing != null) {
            return existing;
        }
        try {
            LevelStem stem = decodeStem(server, id);
            WorldData data = server.getWorldData();
            ServerLevel overworld = server.overworld();
            ServerLevel level = new ServerLevel(server, Util.backgroundExecutor(), storageSource(server),
                    new DerivedLevelData(data, data.overworldData()), key, stem, NO_PROGRESS, data.isDebugWorld(),
                    BiomeManager.obfuscateSeed(data.worldGenOptions().seed()), ImmutableList.of(), false,
                    overworld.getRandomSequences());
            overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(level.getWorldBorder()));
            server.forgeGetWorldMap().put(key, level);
            server.markWorldsDirty();
            return level;
        } catch (Exception e) {
            helper.fail("could not load the level " + key.location() + ": " + e);
            throw new IllegalStateException(e);
        }
    }

    private static LevelStorageSource.LevelStorageAccess storageSource(MinecraftServer server) throws ReflectiveOperationException {
        for (Field f : MinecraftServer.class.getDeclaredFields()) {
            if (f.getType() == LevelStorageSource.LevelStorageAccess.class) {
                f.setAccessible(true);
                return (LevelStorageSource.LevelStorageAccess) f.get(server);
            }
        }
        throw new NoSuchFieldException("MinecraftServer has no LevelStorageAccess field");
    }

    private static Field declaredField(Class<?> owner, String name) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(owner.getName() + " has no field " + name, e);
        }
    }

    private static boolean getBoolean(Field f, Object target) {
        try {
            return f.getBoolean(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setBoolean(Field f, Object target, boolean value) {
        try {
            f.setBoolean(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
