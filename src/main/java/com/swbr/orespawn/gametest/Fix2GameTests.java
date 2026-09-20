package com.swbr.orespawn.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.entity.TileEntityCrystalFurnace;
import com.swbr.orespawn.block.ore.OreGenericEgg;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.boss.RoyalCeiling;
import com.swbr.orespawn.entity.boss.king.KingHead;
import com.swbr.orespawn.entity.boss.king.TheKing;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ChunkPos;
import com.swbr.orespawn.entity.projectile.BetterFireball;
import com.swbr.orespawn.entity.projectile.IceBall;
import com.swbr.orespawn.entity.projectile.ThunderBolt;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import com.swbr.orespawn.entity.rider.VelocityRaptorSpeed;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.world.dimension.ChunkTerrainCache;
import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import com.swbr.orespawn.world.spawn.ConfigSpawnsBiomeModifier;
import com.swbr.orespawn.world.spawn.SpawnTable;
import com.swbr.orespawn.world.structure.LegacyBiomeNames;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * The fix2 wave (docs/port/BUGHUNT2.md section 2, DECISIONS R26): one test per fixed finding, each failing on the code
 * before the fix. Entity and player tests run in batches of their own and remove what they created.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class Fix2GameTests {

    private static final String ARENA = "arena";
    private static final String ARENA_LARGE = "arena_large";

    private Fix2GameTests() {
    }

    // ------------------------------------------------------------------ 2.1 client-start

    /**
     * BUGHUNT2 2.1: every {@code BlockReed} subclass takes the biome grass colour like sugar cane. Block colours are
     * client-only, so the test pins the other half of the fix, the model: {@code tinted_cross} carries the
     * {@code tintindex} the colour handler needs. Before the fix the 21 models were {@code block/cross}.
     */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void blockReedPlantsAreGrassTinted(GameTestHelper helper) {
        List<String> ids = new ArrayList<>();
        for (String crop : List.of("corn", "tomato", "lettuce", "quinoa")) {
            for (int i = 0; i < 4; i++) {
                ids.add(crop + "_" + i);
            }
        }
        ids.addAll(List.of("experiencesapling", "island", "kingspawner", "queenspawner", "dungeonspawner"));
        List<String> failures = new ArrayList<>();
        for (String id : ids) {
            String path = "/assets/orespawn/models/block/" + id + ".json";
            try (InputStream in = Fix2GameTests.class.getResourceAsStream(path)) {
                if (in == null) {
                    failures.add(path + " missing");
                    continue;
                }
                JsonObject model = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                String parent = model.has("parent") ? model.get("parent").getAsString() : "-";
                if (!parent.equals("minecraft:block/tinted_cross")) {
                    failures.add(id + " parent " + parent);
                }
            } catch (java.io.IOException e) {
                failures.add(path + ": " + e);
            }
        }
        report(helper, failures);
    }

    // ------------------------------------------------------------------ 2.2 worldgen-dimensions

    /**
     * BUGHUNT2 2.2: the return search reads the height of a column whose chunk was never loaded. {@code Level.getHeight}
     * answered {@code getMinBuildHeight()} there, and the player landed on Y -63; {@code searchTop} now loads the chunk.
     * The superflat GameTest overworld has its surface at the first air above the grass.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, batch = "fix2_teleporter")
    public static void teleporterReturnFromNeverLoadedChunkLandsOnSurface(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int x = 20000;
        int z = 20000;
        helper.assertFalse(level.hasChunk(x >> 4, z >> 4), "precondition: the chunk at " + x + "," + z + " is already loaded");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.moveTo(x + 0.5, 100.0, z + 0.5, 0.0F, 0.0F);
            new OreSpawnTeleporter(level, Level.OVERWORLD, level).justPutMe(player);
            int surface = level.getChunk(x >> 4, z >> 4).getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15) + 1;
            BlockPos feet = player.blockPosition();
            helper.assertTrue(player.getY() > level.getMinBuildHeight() + 2,
                    "the return landed at Y " + player.getY() + ", in the bottom of the world");
            helper.assertTrue(feet.getY() == surface, "the return landed at Y " + feet.getY() + ", expected the surface Y " + surface);
            helper.assertTrue(level.getBlockState(feet.below()).isSolid() && level.getBlockState(feet).isAir()
                    && level.getBlockState(feet.above()).isAir(), "no free two-block spot on a solid floor at " + feet);
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /**
     * BUGHUNT2 2.2 / R26: {@code hell} is {@code #minecraft:is_nether}. Kyuubi and Stinky (OreSpawnMain.java:4462, :4465)
     * reach all five Nether biomes; before the fix only {@code nether_wastes} had them.
     */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void netherTagBiomesGetKyuubiAndStinky(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        for (ResourceKey<Biome> biome : List.of(Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY, Biomes.CRIMSON_FOREST,
                Biomes.WARPED_FOREST, Biomes.BASALT_DELTAS)) {
            expectAdded(helper, failures, biome, Set.of("orespawn:kyuubi", "orespawn:stinky"));
        }
        report(helper, failures);
    }

    /**
     * BUGHUNT2 2.2 / R26: the biomes 1.21.1 split off the 1.7.10 ocean, deep ocean, swamp and jungle carry their parent's
     * spawns (OreSpawnMain.java:4366, :4489, :4506-4531); the frozen oceans, separate in 1.7.10 already, get nothing.
     */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void splitOceanSwampJungleBiomesGetTheirParentSpawns(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        Set<String> ocean = Set.of("orespawn:water_dragon", "orespawn:sea_monster", "orespawn:sea_viper", "orespawn:crab",
                "orespawn:attack_squid", "orespawn:lizard");
        expectAdded(helper, failures, Biomes.WARM_OCEAN, ocean);
        expectAdded(helper, failures, Biomes.LUKEWARM_OCEAN, ocean);
        expectAdded(helper, failures, Biomes.COLD_OCEAN, ocean);
        expectAdded(helper, failures, Biomes.DEEP_COLD_OCEAN, Set.of("orespawn:whale"));
        expectAdded(helper, failures, Biomes.DEEP_LUKEWARM_OCEAN, Set.of("orespawn:whale"));
        expectAdded(helper, failures, Biomes.MANGROVE_SWAMP, Set.of("orespawn:hydrolisc", "orespawn:water_dragon"));
        expectAdded(helper, failures, Biomes.BAMBOO_JUNGLE, Set.of("orespawn:hydrolisc", "orespawn:basilisk", "orespawn:bird"));
        ServerLevel level = helper.getLevel();
        SpawnTable table = SpawnTable.forServer(level.getServer(), spawnsId());
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> frozen : List.of(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN)) {
            List<SpawnTable.Entry> entries = table.entries(biomes.getHolderOrThrow(frozen));
            if (!entries.isEmpty()) {
                failures.add(frozen.location() + " has " + entries.size() + " OreSpawn entries, expected none");
            }
        }
        report(helper, failures);
    }

    /** BUGHUNT2 2.2 / R26: the {@code legacy_name} tags follow the same map as the spawn table. */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void legacyNameTagsCoverSplitBiomes(GameTestHelper helper) {
        Registry<Biome> biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        List<String> failures = new ArrayList<>();
        Map<ResourceKey<Biome>, Boolean> ocean = new LinkedHashMap<>();
        ocean.put(Biomes.OCEAN, true);
        ocean.put(Biomes.WARM_OCEAN, true);
        ocean.put(Biomes.LUKEWARM_OCEAN, true);
        ocean.put(Biomes.COLD_OCEAN, true);
        ocean.put(Biomes.DEEP_OCEAN, false);
        ocean.put(Biomes.FROZEN_OCEAN, false);
        ocean.forEach((key, expected) -> {
            if (biomes.getHolderOrThrow(key).is(LegacyBiomeNames.OCEAN) != expected) {
                failures.add(key.location() + " in legacy_name/ocean should be " + expected);
            }
        });
        if (!biomes.getHolderOrThrow(Biomes.MANGROVE_SWAMP).is(LegacyBiomeNames.SWAMPLAND)) {
            failures.add("mangrove_swamp not in legacy_name/swampland");
        }
        if (!biomes.getHolderOrThrow(Biomes.BAMBOO_JUNGLE).is(LegacyBiomeNames.JUNGLE)) {
            failures.add("bamboo_jungle not in legacy_name/jungle");
        }
        report(helper, failures);
    }

    /**
     * R26 (getBaseColumn cache): the cache must not change a block. For the Utopia, Mining, Crystal and Chaos generators
     * a column is read cold, again from the cache, and a third time after 40 other chunks pushed its chunk out of the
     * 32-chunk cache (recomputed): all three are block for block the same. The cache itself loads each chunk once.
     */
    @GameTest(template = ARENA, timeoutTicks = 1200, batch = "fix2_base_column")
    public static void baseColumnCacheReturnsSameBlocks(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        AtomicInteger loads = new AtomicInteger();
        ChunkTerrainCache<int[]> cache = new ChunkTerrainCache<>(4);
        ChunkTerrainCache.Loader<int[]> loader = (cx, cz) -> {
            loads.incrementAndGet();
            return new int[] {cx, cz};
        };
        int[] first = cache.get(1, 2, loader);
        if (cache.get(1, 2, loader) != first || loads.get() != 1) {
            failures.add("ChunkTerrainCache loaded a cached chunk again (" + loads.get() + " loads)");
        }
        for (int i = 10; i < 15; i++) {
            cache.get(i, 0, loader);
        }
        cache.get(1, 2, loader);
        if (loads.get() != 7) {
            failures.add("ChunkTerrainCache kept more than its capacity: " + loads.get() + " loads, expected 7");
        }
        for (String dim : List.of("utopia", "mining", "crystal", "chaos")) {
            ServerLevel level = W05GameTests.dimensionLevel(helper, dim);
            ChunkGenerator generator = level.getChunkSource().getGenerator();
            RandomState random = level.getChunkSource().randomState();
            int baseX = 64000;
            int baseZ = 64000 + dim.hashCode() % 1000 * 16;
            for (int c = 0; c < 16; c += 5) {
                int x = baseX + c;
                int z = baseZ + (15 - c);
                NoiseColumn cold = generator.getBaseColumn(x, z, level, random);
                NoiseColumn cached = generator.getBaseColumn(x, z, level, random);
                compareColumns(failures, dim + " cached " + x + "," + z, level, cold, cached);
                for (int i = 1; i <= 40; i++) {
                    generator.getBaseColumn(baseX + i * 16, baseZ - 4096, level, random);
                }
                NoiseColumn recomputed = generator.getBaseColumn(x, z, level, random);
                compareColumns(failures, dim + " recomputed " + x + "," + z, level, cold, recomputed);
            }
        }
        report(helper, failures);
    }

    private static void compareColumns(List<String> failures, String what, ServerLevel level, NoiseColumn a, NoiseColumn b) {
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            if (a.getBlock(y) != b.getBlock(y)) {
                failures.add(what + ": Y " + y + " " + a.getBlock(y) + " vs " + b.getBlock(y));
                return;
            }
        }
    }

    // ------------------------------------------------------------------ 2.3 items-tools-armor

    /**
     * BUGHUNT2 2.3: the Miner's Dream leaves OreSpawn's own ores standing ({@code #c:ores}), as ItemMinersDream.java:72
     * did. Before the fix ruby, crystal coal, tiger's eye, the mob stones and the egg ores were dug away.
     */
    @GameTest(template = ARENA_LARGE, timeoutTicks = 200, batch = "fix2_miners_dream")
    public static void minersDreamKeepsOreSpawnOres(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        List<Supplier<? extends Block>> tagged = new ArrayList<>(List.<Supplier<? extends Block>>of(ModBlocks.ORESALT, ModBlocks.OREURANIUM,
                ModBlocks.ORETITANIUM, ModBlocks.OREAMETHYST, ModBlocks.ORERUBY, ModBlocks.CRYSTALCOAL, ModBlocks.CRYSTALCRYSTAL,
                ModBlocks.TIGERSEYE, ModBlocks.CRYSTALRAT, ModBlocks.CRYSTALFAIRY, ModBlocks.REDANTTROLL, ModBlocks.TERMITETROLL));
        for (String id : OreGenericEgg.DRIED_EGG_IDS) {
            tagged.add(ModBlocks.DRIED_EGGS.get(id));
        }
        for (Supplier<? extends Block> block : tagged) {
            if (!block.get().defaultBlockState().is(Tags.Blocks.ORES)) {
                failures.add(BuiltInRegistries.BLOCK.getKey(block.get()) + " not in #c:ores");
            }
        }
        for (int x = 18; x <= 30; x++) {
            for (int z = 3; z <= 70; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                for (int y = 2; y <= 6; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
                helper.setBlock(new BlockPos(x, 7, z), Blocks.STONE);
            }
        }
        Map<BlockPos, Block> kept = new LinkedHashMap<>();
        kept.put(new BlockPos(23, 3, 10), ModBlocks.ORERUBY.get());
        kept.put(new BlockPos(25, 4, 15), ModBlocks.CRYSTALCOAL.get());
        kept.put(new BlockPos(21, 2, 25), ModBlocks.TIGERSEYE.get());
        kept.put(new BlockPos(27, 5, 35), ModBlocks.CRYSTALRAT.get());
        kept.put(new BlockPos(19, 3, 45), ModBlocks.DRIED_EGGS.get("orespider").get());
        kept.put(new BlockPos(24, 6, 55), ModBlocks.OREAMETHYST.get());
        kept.put(new BlockPos(22, 4, 30), Blocks.DIAMOND_ORE);
        kept.forEach(helper::setBlock);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos feet = helper.absolutePos(new BlockPos(24, 2, 2));
        player.setPos(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
        ItemStack stack = new ItemStack(ModItems.MINERS_DREAM.get(), 16);
        BlockPos target = helper.absolutePos(new BlockPos(24, 1, 3));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        InteractionResult result = stack.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(target).add(0, 0.5, 0), Direction.UP, target, false)));
        helper.assertTrue(result == InteractionResult.CONSUME, "minersdream returned " + result);
        kept.forEach((rel, block) -> {
            if (!helper.getBlockState(rel).is(block)) {
                failures.add(BuiltInRegistries.BLOCK.getKey(block) + " at " + rel + " was dug: " + helper.getBlockState(rel));
            }
        });
        if (!helper.getBlockState(new BlockPos(24, 4, 12)).isAir()) {
            failures.add("the stone in the tunnel was not dug: " + helper.getBlockState(new BlockPos(24, 4, 12)));
        }
        report(helper, failures);
    }

    /**
     * BUGHUNT2 2.3: {@code Blocks.red_flower} with wildcard meta (OreSpawnMain.java:2837-2839) is every small flower but
     * the dandelion. Before the fix cornflower and lily of the valley gave nothing.
     */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void roseSwordAcceptsCornflowerAndLilyOfTheValley(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        for (Item flower : List.of(Items.POPPY, Items.CORNFLOWER, Items.LILY_OF_THE_VALLEY)) {
            Item out = craft(helper, 1, 3, List.of(new ItemStack(flower), new ItemStack(flower), new ItemStack(Items.STICK)));
            if (out != ModItems.ROSE_SWORD.get()) {
                failures.add(BuiltInRegistries.ITEM.getKey(flower) + " gave " + out + ", expected rosesword");
            }
        }
        Item dandelion = craft(helper, 1, 3, List.of(new ItemStack(Items.DANDELION), new ItemStack(Items.DANDELION),
                new ItemStack(Items.STICK)));
        if (dandelion == ModItems.ROSE_SWORD.get()) {
            failures.add("dandelion gave a rosesword");
        }
        report(helper, failures);
    }

    /** BUGHUNT2 2.3: {@code 'W', Blocks.planks} with wildcard meta (OreSpawnMain.java:5034) is every plank but R18's crystal planks. */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void elevatorAcceptsCherryAndCrimsonPlanks(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        for (Item plank : List.of(Items.OAK_PLANKS, Items.CHERRY_PLANKS, Items.CRIMSON_PLANKS, ModBlocks.CRYSTAL_PLANKS.get().asItem())) {
            ItemStack w = new ItemStack(plank);
            Item out = craft(helper, 3, 2, List.of(w.copy(), w.copy(), w.copy(), new ItemStack(Items.DIAMOND),
                    new ItemStack(Items.REDSTONE), new ItemStack(Items.DIAMOND)));
            boolean expected = plank != ModBlocks.CRYSTAL_PLANKS.get().asItem();
            if ((out == ModItems.ELEVATOR.get()) != expected) {
                failures.add(BuiltInRegistries.ITEM.getKey(plank) + " gave " + out + (expected ? ", expected elevator" : ", expected nothing"));
            }
        }
        report(helper, failures);
    }

    private static Item craft(GameTestHelper helper, int width, int height, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(width, height, items);
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(r -> r.value().assemble(input, helper.getLevel().registryAccess()).getItem()).orElse(Items.AIR);
    }

    // ------------------------------------------------------------------ 2.4 blocks-crops

    /**
     * BUGHUNT2 2.4: the RTP block's teleport survives the move handler. A survival player's move packet onto the block
     * runs {@code stepOn} inside {@code handleMovePlayer}; a teleport there was undone right after ("moved wrongly!").
     * The test drives the real handler, then looks two ticks later.
     */
    @GameTest(template = ARENA, batch = "fix2_rtp")
    public static void rtpBlockTeleportSurvivesTheMoveHandler(GameTestHelper helper) {
        BlockPos rel = new BlockPos(3, 1, 3);
        helper.setBlock(rel, ModBlocks.BLOCKTELEPORT.get());
        helper.setBlock(rel.west(), Blocks.STONE);
        BlockPos abs = helper.absolutePos(rel);
        for (int dx = -23; dx <= 23; dx++) {
            for (int dz = -23; dz <= 23; dz++) {
                if (Math.abs(dx) >= 9 && Math.abs(dz) >= 9) {
                    helper.getLevel().setBlockAndUpdate(abs.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(abs.offset(dx, 0, dz), Blocks.AIR.defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(abs.offset(dx, 1, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
        @SuppressWarnings("removal")
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ServerGamePacketListenerImpl connection = player.connection;
        double top = abs.getY() + 1.0;
        connection.teleport(abs.getX() - 0.5, top, abs.getZ() + 0.5, 0.0F, 0.0F);
        try {
            Field awaiting = ServerGamePacketListenerImpl.class.getDeclaredField("awaitingTeleport");
            awaiting.setAccessible(true);
            connection.handleAcceptTeleportPacket(new ServerboundAcceptTeleportationPacket(awaiting.getInt(connection)));
        } catch (ReflectiveOperationException e) {
            helper.getLevel().getServer().getPlayerList().remove(player);
            helper.fail("cannot acknowledge the teleport: " + e);
            return;
        }
        // One block east onto the RTP block, slightly into it so the move collides downwards and runs stepOn.
        connection.handleMovePlayer(new ServerboundMovePlayerPacket.Pos(abs.getX() + 0.5, top - 0.01, abs.getZ() + 0.5, true));
        helper.runAfterDelay(2, () -> {
            try {
                int dx = Math.abs(player.getBlockX() - abs.getX());
                int dz = Math.abs(player.getBlockZ() - abs.getZ());
                helper.assertTrue(dx >= 9 && dx <= 23 && dz >= 9 && dz <= 23,
                        "the player stands " + dx + "," + dz + " from the RTP block, expected 9..23 on each axis");
            } finally {
                helper.getLevel().getServer().getPlayerList().remove(player);
            }
            helper.succeed();
        });
    }

    /**
     * BUGHUNT2 2.4: the seven OreSpawn {@code BlockCrops} keep dry farmland under them ({@code #minecraft:maintains_farmland}),
     * as Forge 1.7.10's farmland did for every Crop plant; the BlockReed plants (Beach) do not.
     */
    @GameTest(template = ARENA, batch = "fix2_farmland")
    public static void oreSpawnCropsKeepFarmlandWet(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        List<DeferredBlock<?>> crops = List.of(ModBlocks.RADISH_PLANT, ModBlocks.STRAWBERRY_PLANT, ModBlocks.RICE_PLANT,
                ModBlocks.BUTTERFLY_PLANT, ModBlocks.MOTH_PLANT, ModBlocks.MOSQUITO_PLANT, ModBlocks.FIREFLY_PLANT);
        for (DeferredBlock<?> crop : crops) {
            if (!crop.get().defaultBlockState().is(BlockTags.MAINTAINS_FARMLAND)) {
                failures.add(crop.getId() + " not in #minecraft:maintains_farmland");
            }
        }
        if (ModBlocks.CORN_0.get().defaultBlockState().is(BlockTags.MAINTAINS_FARMLAND)) {
            failures.add("corn_0 is in #minecraft:maintains_farmland");
        }
        BlockPos farm = new BlockPos(3, 1, 3);
        helper.setBlock(farm, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 0));
        helper.setBlock(farm.above(), ModBlocks.RADISH_PLANT.get());
        BlockPos abs = helper.absolutePos(farm);
        ServerLevel level = helper.getLevel();
        if (level.isRainingAt(abs.above())) {
            failures.add("precondition: it rains on the farmland");
        }
        for (int i = 0; i < 20; i++) {
            BlockState state = level.getBlockState(abs);
            if (!state.is(Blocks.FARMLAND)) {
                break;
            }
            state.randomTick(level, abs, level.random);
        }
        if (!helper.getBlockState(farm).is(Blocks.FARMLAND)) {
            failures.add("the farmland under a radish turned into " + helper.getBlockState(farm));
        }
        report(helper, failures);
    }

    /**
     * BUGHUNT2 2.4 / R26: the mined sky tree log notifies its neighbours (a rail on it drops), the recursive trunk blocks
     * are silent (a torch on one stays, leaves next to one keep their distance). Before the fix the mined block was
     * silent and the recursive ones ran shape updates - the other way round.
     */
    @GameTest(template = ARENA, batch = "fix2_sky_tree")
    public static void skyTreeCrownFloatsButRailDrops(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        BlockPos bottom = new BlockPos(3, 2, 3);
        for (int i = 0; i < 3; i++) {
            helper.setBlock(bottom.above(i), ModBlocks.SKY_TREE_LOG.get());
        }
        BlockPos topPos = bottom.above(2);
        helper.setBlock(topPos.above(), Blocks.RAIL);
        BlockPos leaves = bottom.north();
        helper.setBlock(leaves, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, false).setValue(LeavesBlock.DISTANCE, 1));
        BlockPos torch = bottom.above().east();
        helper.setBlock(torch, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos absTop = helper.absolutePos(topPos);
        BlockState state = helper.getLevel().getBlockState(absTop);
        state.onDestroyedByPlayer(helper.getLevel(), absTop, player, true, helper.getLevel().getFluidState(absTop));
        helper.runAfterDelay(5, () -> {
            for (int i = 0; i < 3; i++) {
                if (!helper.getBlockState(bottom.above(i)).isAir()) {
                    failures.add("trunk block " + bottom.above(i) + " is still " + helper.getBlockState(bottom.above(i)));
                }
            }
            if (helper.getBlockState(topPos.above()).is(Blocks.RAIL)) {
                failures.add("the rail on the mined log still floats");
            }
            BlockState leafState = helper.getBlockState(leaves);
            if (!leafState.is(Blocks.OAK_LEAVES) || leafState.getValue(LeavesBlock.DISTANCE) != 1) {
                failures.add("the leaves next to a recursive trunk block got a shape update: " + leafState);
            }
            if (!helper.getBlockState(torch).is(Blocks.WALL_TORCH)) {
                failures.add("the torch on a recursive trunk block dropped: " + helper.getBlockState(torch));
            }
            clearItems(helper);
            report(helper, failures);
        });
    }

    /**
     * BUGHUNT2 2.4: {@code Material.wood} burns 300 ticks in the crystal furnace (TileEntityCrystalFurnace.java:231-233);
     * the 1.21.1 members of that category (BASS instrument and ignited by lava) are fuel, wooden slabs 150.
     */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void crystalFurnaceBurnsModernWoodBlocks(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        Map<Item, Integer> expected = new LinkedHashMap<>();
        for (Item item : List.of(Items.BARREL, Items.LECTERN, Items.COMPOSTER, Items.LOOM, Items.CARTOGRAPHY_TABLE,
                Items.FLETCHING_TABLE, Items.SMITHING_TABLE, Items.CHISELED_BOOKSHELF, Items.BAMBOO_MOSAIC,
                Items.BAMBOO_MOSAIC_STAIRS, Items.BAMBOO_BLOCK, Items.MANGROVE_ROOTS, Items.BEEHIVE, Items.BEE_NEST,
                Items.CAMPFIRE, Items.WHITE_BANNER)) {
            expected.put(item, 300);
        }
        expected.put(Items.BAMBOO_MOSAIC_SLAB, 150);
        for (Item item : List.of(Items.CRIMSON_PLANKS, Items.OAK_SIGN, Items.OAK_DOOR, Items.OAK_BUTTON)) {
            expected.put(item, 0);
        }
        expected.forEach((item, ticks) -> {
            int got = TileEntityCrystalFurnace.getItemBurnTime(new ItemStack(item));
            if (got != ticks) {
                failures.add(BuiltInRegistries.ITEM.getKey(item) + " burns " + got + ", expected " + ticks);
            }
        });
        report(helper, failures);
    }

    // ------------------------------------------------------------------ 2.5 land-mobs

    /**
     * BUGHUNT2 2.5 / R26: a natural spawn group of 100 has no baby. {@code finalizeSpawn} is chained with the group data
     * each call returns, as {@code NaturalSpawner} does; before the fix every later member was a baby with 5 %.
     */
    @SuppressWarnings({"deprecation", "OverrideOnly"})
    @GameTest(template = ARENA, batch = "fix2_babies")
    public static void naturalGroupSpawnsHaveNoBabies(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        List<Supplier<? extends EntityType<? extends Mob>>> types = List.<Supplier<? extends EntityType<? extends Mob>>>of(ModEntities.GIRLFRIEND, ModEntities.APPLE_COW,
                ModEntities.BARYONYX, ModEntities.VELOCITY_RAPTOR, ModEntities.ANT, ModEntities.WHALE);
        for (Supplier<? extends EntityType<? extends Mob>> type : types) {
            SpawnGroupData group = null;
            int babies = 0;
            for (int i = 0; i < 100; i++) {
                Mob mob = type.get().create(level);
                if (mob == null) {
                    failures.add(type.get() + " could not be created");
                    break;
                }
                mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
                group = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, group);
                if (mob instanceof AgeableMob ageable && ageable.isBaby()) {
                    babies++;
                }
                mob.discard();
            }
            if (babies > 0) {
                failures.add(BuiltInRegistries.ENTITY_TYPE.getKey(type.get()) + ": " + babies + " babies in a group of 100");
            }
        }
        report(helper, failures);
    }

    /**
     * BUGHUNT2 2.5 / R26: the Velocity Raptor's player speed lasts until the next sprint toggle, like the client-side
     * {@code setBaseValue} of VelocityRaptor.java:313 did. Before the fix the modifier stayed for the session.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, batch = "fix2_raptor")
    public static void velocityRaptorSpeedDropsOnSprintToggle(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        VelocityRaptor raptor = helper.spawn(ModEntities.VELOCITY_RAPTOR.get(), new BlockPos(3, 2, 3));
        try {
            Vec3 at = helper.absoluteVec(new Vec3(4.5, 2.0, 3.5));
            player.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            raptor.setTame(true, false);
            raptor.setOwnerUUID(player.getUUID());
            InteractionResult result = raptor.mobInteract(player, InteractionHand.MAIN_HAND);
            helper.assertTrue(result.consumesAction(), "the sit toggle returned " + result);
            var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            helper.assertTrue(speed.getModifier(VelocityRaptorSpeed.ID) != null, "the sit toggle set no speed modifier");
            helper.assertTrue(Math.abs(speed.getValue() - 0.3) < 1.0E-6, "speed with the raptor modifier is " + speed.getValue());
            helper.assertFalse(VelocityRaptorSpeed.expireIfSprintChanged(player), "the modifier dropped without a sprint toggle");
            player.setSprinting(true);
            helper.assertTrue(VelocityRaptorSpeed.expireIfSprintChanged(player), "the sprint toggle did not remove the modifier");
            player.setSprinting(false);
            helper.assertTrue(speed.getModifier(VelocityRaptorSpeed.ID) == null, "the speed modifier is still there");
            helper.assertTrue(Math.abs(speed.getValue() - 0.1) < 1.0E-6, "speed after the sprint toggle is " + speed.getValue());
        } finally {
            raptor.discard();
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ 2.6 bosses-cages

    /**
     * BUGHUNT2 2.6: a ThunderBolt or IceBall leaving the King's head box (where R25 puts the head and firecanonl starts
     * the salvo) flies on. Before the fix the exit face was an intercept, the head royalty and the bolt discarded.
     */
    @GameTest(template = ARENA, batch = "fix2_royal_salvo")
    public static void royalSalvoLeavesTheHeadBox(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        ServerLevel level = helper.getLevel();
        Vec3 headPos = sectionAligned(helper.absoluteVec(new Vec3(4.5, 40.0, 4.5)), 2.5, 1.0);
        KingHead head = ModEntities.KING_HEAD.get().create(level);
        head.moveTo(headPos.x, headPos.y, headPos.z, 0.0F, 0.0F);
        level.addFreshEntity(head);
        List<Entity> shots = new ArrayList<>();
        try {
            double edge = head.getBoundingBox().maxX + 0.3;
            double y = head.getBoundingBox().getCenter().y;
            ThunderBolt bolt = new ThunderBolt(level, edge - 1.0, y, headPos.z);
            IceBall ice = new IceBall(level, edge - 1.0, y, headPos.z + 1.0);
            for (Entity shot : List.of(bolt, ice)) {
                shot.setDeltaMovement(3.0, 0.0, 0.0);
                level.addFreshEntity(shot);
                shots.add(shot);
            }
            for (Entity shot : shots) {
                shot.tick();
                if (!shot.isAlive()) {
                    failures.add(shot.getType().toShortString() + " ended on the King's own head box");
                }
            }
        } finally {
            shots.forEach(Entity::discard);
            head.discard();
        }
        report(helper, failures);
    }

    /**
     * BUGHUNT2 4.5 (fix2-mobs): BetterFireball's unfiltered {@code ze.h()} pass skips the relocated heads of the King, the
     * Queen and Mobzilla. Before the fix a fireball starting in the head box exploded at the muzzle in its first tick.
     */
    @GameTest(template = ARENA, batch = "fix2_royal_fireball")
    public static void royalFireballSkipsOwnHead(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        ServerLevel level = helper.getLevel();
        List<Supplier<? extends EntityType<? extends Mob>>> heads = List.<Supplier<? extends EntityType<? extends Mob>>>of(ModEntities.KING_HEAD, ModEntities.QUEEN_HEAD,
                ModEntities.MOBZILLA_HEAD);
        Mob shooter = EntityType.PIG.create(level);
        Vec3 shooterPos = helper.absoluteVec(new Vec3(2.5, 2.0, 2.5));
        shooter.moveTo(shooterPos.x, shooterPos.y, shooterPos.z, 0.0F, 0.0F);
        level.addFreshEntity(shooter);
        try {
            int i = 0;
            for (Supplier<? extends EntityType<? extends Mob>> type : heads) {
                Vec3 headPos = sectionAligned(helper.absoluteVec(new Vec3(4.5, 40.0 + 32.0 * i++, 4.5)), 2.5, 1.0);
                Mob head = type.get().create(level);
                head.moveTo(headPos.x, headPos.y, headPos.z, 0.0F, 0.0F);
                level.addFreshEntity(head);
                BetterFireball ball = new BetterFireball(level, shooter, 1.0, 0.0, 0.0);
                try {
                    double edge = head.getBoundingBox().maxX + 0.3;
                    double y = head.getBoundingBox().getCenter().y;
                    ball.moveTo(edge - 1.0, y, headPos.z, 0.0F, 0.0F);
                    ball.setDeltaMovement(3.0, 0.0, 0.0);
                    level.addFreshEntity(ball);
                    ball.tick();
                    if (!ball.isAlive()) {
                        failures.add("a fireball ended on the box of " + BuiltInRegistries.ENTITY_TYPE.getKey(type.get()));
                    }
                } finally {
                    ball.discard();
                    head.discard();
                }
            }
        } finally {
            shooter.discard();
        }
        report(helper, failures);
    }

    /**
     * BUGHUNT2 2.6, end to end: a real King (AI off, so he stays put) with his head where R25 puts it fires
     * {@code firecanonl} at a cow 40 blocks away, the distance the ranged attack needs (above 30 horizontally). The cow
     * stands in front of a stone wall; the salvo must explode there and call down lightning. Before the fix every bolt
     * crossed the head box's exit face and was discarded on the royalty: no lightning anywhere.
     */
    @GameTest(template = ARENA, timeoutTicks = 100, batch = "fix2_king_salvo")
    public static void kingThunderSalvoReachesTargetFortyBlocksAway(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // The head feet (King Y + 12) and the King's X sit near the low end of one entity section, see sectionAligned.
        Vec3 kingPos = sectionAligned(helper.absoluteVec(new Vec3(4.5, 60.0, 4.5)), 1.5, 5.0);
        // The framework forces only the structure's chunks; bolts outside entity-ticking chunks never move.
        List<ChunkPos> forced = new ArrayList<>();
        java.util.Set<Long> before = new HashSet<>(level.getForcedChunks());
        for (int cx = (Mth.floor(kingPos.x) >> 4) - 1; cx <= ((Mth.floor(kingPos.x) + 40) >> 4); cx++) {
            for (int cz = (Mth.floor(kingPos.z) >> 4) - 1; cz <= ((Mth.floor(kingPos.z) + 48) >> 4); cz++) {
                if (!before.contains(ChunkPos.asLong(cx, cz))) {
                    level.setChunkForced(cx, cz, true);
                    forced.add(new ChunkPos(cx, cz));
                }
            }
        }
        TheKing king = ModEntities.THE_KING.get().create(level);
        king.moveTo(kingPos.x, kingPos.y, kingPos.z, 0.0F, 0.0F);
        king.setYHeadRot(0.0F);
        king.setNoAi(true);
        level.addFreshEntity(king);
        KingHead head = ModEntities.KING_HEAD.get().create(level);
        head.moveTo(kingPos.x, kingPos.y + 12.0, kingPos.z + 30.0, 0.0F, 0.0F);
        level.addFreshEntity(head);
        // yRot 0 faces +Z: the muzzle is 32 ahead and 14 up. The target is 40 blocks from the King (24 east, 32 ahead) at
        // muzzle height, so the bolts leave the 19.9-wide head box through its east face long before they reach the cow;
        // straight ahead at 40 the cow's box would overlap the head box and be hit first either way.
        Vec3 targetPos = new Vec3(kingPos.x + 24.0, kingPos.y + 14.0, kingPos.z + 32.0);
        Cow cow = EntityType.COW.create(level);
        cow.moveTo(targetPos.x, targetPos.y, targetPos.z, 0.0F, 0.0F);
        cow.setNoAi(true);
        cow.setNoGravity(true);
        level.addFreshEntity(cow);
        BlockPos wallCenter = BlockPos.containing(targetPos.x + 2.0, targetPos.y, targetPos.z);
        // The aim leads by 0.2 x 24 = 4.8 blocks upwards and spreads with inaccuracy 4: the wall reaches well above.
        for (int dz = -8; dz <= 8; dz++) {
            for (int dy = -6; dy <= 16; dy++) {
                level.setBlockAndUpdate(wallCenter.offset(0, dy, dz), Blocks.STONE.defaultBlockState());
            }
        }
        AABB around = new AABB(targetPos, targetPos).inflate(8.0, 18.0, 8.0);
        List<ThunderBolt> bolts = new ArrayList<>();
        AtomicInteger lightning = new AtomicInteger();
        Runnable cleanup = () -> {
            bolts.forEach(Entity::discard);
            level.getEntitiesOfClass(LightningBolt.class, around.inflate(4.0)).forEach(Entity::discard);
            king.discard();
            head.discard();
            cow.discard();
            for (BlockPos p : BlockPos.betweenClosed(wallCenter.offset(-8, -10, -12), wallCenter.offset(8, 20, 12))) {
                BlockState s = level.getBlockState(p);
                if (s.is(Blocks.STONE) || s.is(BlockTags.FIRE) || s.is(Blocks.ICE)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, around.inflate(10.0)).forEach(Entity::discard);
            forced.forEach(c -> level.setChunkForced(c.x, c.z, false));
        };
        helper.runAfterDelay(3, () -> {
            try {
                Field streams = TheKing.class.getDeclaredField("stream_count_l");
                streams.setAccessible(true);
                streams.setInt(king, 5);
                java.lang.reflect.Method fire = TheKing.class.getDeclaredMethod("firecanonl", net.minecraft.world.entity.LivingEntity.class);
                fire.setAccessible(true);
                fire.invoke(king, cow);
            } catch (ReflectiveOperationException e) {
                cleanup.run();
                helper.fail("cannot fire the King's thunder salvo: " + e);
                return;
            }
            bolts.addAll(level.getEntitiesOfClass(ThunderBolt.class, new AABB(kingPos, kingPos).inflate(40.0)));
            if (bolts.size() != 3) {
                cleanup.run();
                List<String> seen = new ArrayList<>();
                bolts.forEach(b -> seen.add(b.getId() + "@" + b.position() + " age " + b.tickCount));
                helper.fail("firecanonl fired " + bolts.size() + " bolts, expected 3: " + seen + " king " + king.getId() + " " + king.position());
                return;
            }
        });
        // Registered here, not inside the task above: GameTestInfo iterates its task map while running a task, and a
        // task added during that iteration can make the iterator run an entry twice.
        helper.onEachTick(() -> lightning.addAndGet(level.getEntitiesOfClass(LightningBolt.class, around).size()));
        helper.runAfterDelay(23, () -> {
            List<String> failures = new ArrayList<>();
            try {
                if (lightning.get() == 0) {
                    List<String> ends = new ArrayList<>();
                    for (ThunderBolt bolt : bolts) {
                        ends.add(String.format("%s at dx %.2f dy %.2f dz %.2f", bolt.isAlive() ? "alive" : bolt.getRemovalReason(), bolt.getX() - kingPos.x,
                                bolt.getY() - kingPos.y, bolt.getZ() - kingPos.z));
                    }
                    failures.add("no lightning at the wall behind the target 40 blocks away; bolts " + ends + ", King at dy " + (king.getY() - kingPos.y));
                }
                if (!head.isAlive() || head.distanceToSqr(kingPos.x, kingPos.y + 12.0, kingPos.z + 30.0) > 1.0) {
                    failures.add("the King's head was not in front of him during the salvo: " + head.position());
                }
            } finally {
                cleanup.run();
            }
            report(helper, failures);
        });
    }

    /**
     * BUGHUNT2 2.6 / R26, on the King himself: outside the OreSpawn dimensions a hit from an attacker at Y 280 makes the
     * King's waypoint Y 280 (TheKing.hurt, the {@code rt} branch). Before the fix the literal 230 capped it.
     */
    @GameTest(template = ARENA, batch = "fix2_king_ceiling")
    public static void kingWaypointAboveY230InTheOverworld(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Vec3 kingPos = helper.absoluteVec(new Vec3(4.5, 30.0, 4.5));
        TheKing king = ModEntities.THE_KING.get().create(level);
        king.moveTo(kingPos.x, kingPos.y, kingPos.z, 0.0F, 0.0F);
        king.setNoAi(true);
        level.addFreshEntity(king);
        Cow attacker = EntityType.COW.create(level);
        attacker.moveTo(kingPos.x + 8.0, 280.0, kingPos.z, 0.0F, 0.0F);
        attacker.setNoAi(true);
        attacker.setNoGravity(true);
        level.addFreshEntity(attacker);
        try {
            Field target = TheKing.class.getDeclaredField("currentFlightTarget");
            target.setAccessible(true);
            target.set(king, new BlockPos.MutableBlockPos(Mth.floor(kingPos.x), Mth.floor(kingPos.y), Mth.floor(kingPos.z)));
            helper.assertTrue(level.getMaxBuildHeight() > 300, "precondition: the overworld build height is " + level.getMaxBuildHeight());
            // The waypoint is set whether or not the armor lets the damage through (TheKing.hurt, after super.hurt).
            king.hurt(level.damageSources().mobAttack(attacker), 1.0F);
            BlockPos waypoint = (BlockPos) target.get(king);
            helper.assertTrue(waypoint.getY() == 280, "the King's waypoint after a hit from Y 280 is Y " + waypoint.getY() + ", expected 280");
        } catch (ReflectiveOperationException e) {
            helper.fail("cannot read the King's waypoint: " + e);
        } finally {
            king.discard();
            attacker.discard();
        }
        helper.succeed();
    }

    /** BUGHUNT2 2.6 / R26: the King's and Queen's Y ceiling is 230 in OreSpawn dimensions, build height - 26 elsewhere. */
    @GameTest(template = ARENA, batch = "fix2_static")
    public static void royalCeilingFollowsBuildHeight(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel();
        ServerLevel utopia = W05GameTests.dimensionLevel(helper, "utopia");
        helper.assertTrue(RoyalCeiling.of(overworld) == overworld.getMaxBuildHeight() - 26,
                "overworld ceiling " + RoyalCeiling.of(overworld) + ", expected " + (overworld.getMaxBuildHeight() - 26));
        helper.assertTrue(RoyalCeiling.of(overworld) == 294, "overworld ceiling " + RoyalCeiling.of(overworld) + ", expected 294");
        helper.assertTrue(RoyalCeiling.of(utopia) == 230, "utopia ceiling " + RoyalCeiling.of(utopia) + ", expected 230");
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Moves {@code v} to offset {@code xOff}/{@code yOff} inside its 16-block entity section (Z unchanged). A projectile
     * finds its candidates through {@code getEntities}, which only walks the sections within 2 blocks of its search box:
     * a 19.9-wide head whose centre lies in the neighbouring section is not found at all, hit or not. Without the
     * alignment the head tests would pass on the unfixed code for some grid positions.
     */
    private static Vec3 sectionAligned(Vec3 v, double xOff, double yOff) {
        return new Vec3(Math.floor(v.x / 16.0) * 16.0 + xOff, Math.floor(v.y / 16.0) * 16.0 + yOff, v.z);
    }

    private static net.minecraft.resources.ResourceLocation spawnsId() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "spawns");
    }

    /** Applies config_spawns to {@code biome} with every switch its entries use set to 1 and checks the types were added. */
    private static void expectAdded(GameTestHelper helper, List<String> failures, ResourceKey<Biome> key, Set<String> types) {
        ServerLevel level = helper.getLevel();
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> biome = biomes.getHolderOrThrow(key);
        SpawnTable table = SpawnTable.forServer(level.getServer(), spawnsId());
        Map<String, ModConfigSpec.IntValue> switches = mobSwitches();
        Map<String, Integer> saved = new HashMap<>();
        for (SpawnTable.Entry entry : table.entries(biome)) {
            for (String flag : entry.flags()) {
                ModConfigSpec.IntValue value = switches.get(flag);
                if (value != null && !saved.containsKey(flag)) {
                    saved.put(flag, value.get());
                }
            }
        }
        ModConfigSpec.IntValue allMobs = OreSpawnConfig.TWEAKS.AllMobsDisable;
        int savedAll = allMobs.get();
        try {
            allMobs.set(0);
            saved.keySet().forEach(flag -> switches.get(flag).set(1));
            ModifiableBiomeInfo.BiomeInfo original = biome.value().modifiableBiomeInfo().getOriginalBiomeInfo();
            ModifiableBiomeInfo.BiomeInfo.Builder builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(original);
            new ConfigSpawnsBiomeModifier(spawnsId()).modify(biome, BiomeModifier.Phase.ADD, builder);
            Set<String> added = new HashSet<>();
            for (MobCategory category : MobCategory.values()) {
                for (MobSpawnSettings.SpawnerData data : builder.getMobSpawnSettings().getSpawner(category)) {
                    added.add(BuiltInRegistries.ENTITY_TYPE.getKey(data.type).toString());
                }
            }
            for (String type : types) {
                if (!added.contains(type)) {
                    failures.add(key.location() + " got no " + type);
                }
            }
        } finally {
            saved.forEach((flag, value) -> switches.get(flag).set(value));
            allMobs.set(savedAll);
        }
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

    private static void clearItems(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, helper.getBounds().inflate(8.0))
                .forEach(Entity::discard);
    }

    private static void report(GameTestHelper helper, List<String> failures) {
        if (failures.isEmpty()) {
            helper.succeed();
            return;
        }
        List<String> head = failures.size() > 12 ? new ArrayList<>(failures.subList(0, 12)) : failures;
        if (failures.size() > 12) {
            head.add((failures.size() - 12) + " more");
        }
        helper.fail(String.join("; ", head));
    }
}
