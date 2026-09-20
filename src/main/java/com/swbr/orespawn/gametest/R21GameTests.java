package com.swbr.orespawn.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.registry.ModItems;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;

/**
 * DECISIONS R21 on a real server: the Mining and Utopia decoration never writes outside the 1.21.1 write radius,
 * Mining decorates with the ported 1.7.10 Java code and no vanilla placed feature, Crystal and Chaos take their
 * noise from the {@code createState} seed, and the OreSpawn axes are {@code #minecraft:enchantable/sharp_weapon}.
 *
 * <p>The dimension levels come from {@link W05GameTests#dimensionLevel}: the vanilla {@code GameTestServer} never
 * turns datapack dimensions into levels, so that helper builds them the way {@code MinecraftServer.createLevels}
 * does.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class R21GameTests {

    private static final String ARENA = "arena";
    /** {@code WorldGenRegion.ensureCanWrite} (WorldGenRegion.java:260-261), logged through {@code Util.logAndPauseIfInIde}. */
    private static final String FAR_CHUNK = "Detected setBlock in a far chunk";
    private static final Pattern FAR_CHUNK_POS = Pattern.compile("far chunk \\[(-?\\d+), (-?\\d+)\\]");
    /** Half side of the tested field: 5x5 chunks. */
    private static final int FIELD = 2;
    /**
     * Half side of the area a FULL field touches: FULL needs INITIALIZE_LIGHT one chunk around, which needs
     * FEATURES there, which needs CARVERS one chunk further (ChunkPyramid.GENERATION_PYRAMID).
     */
    private static final int RING = FIELD + 2;

    private R21GameTests() {}

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path);
    }

    /**
     * R21 "Weit reichende Dekoration wird geprüft, nicht vermutet": a 5x5 field of {@code FULL} chunks in
     * {@code orespawn:mining} and one in {@code orespawn:utopia}, each on chunks no earlier run generated, with an
     * appender on the {@code net.minecraft.Util} logger for the duration. No {@code Detected setBlock in a far chunk}
     * may appear. Silence alone proves nothing, so the field must also show the decoration's own blocks: infested
     * stone (the Extreme Hills silverfish pass, only in {@code BiomeGenHills.decorate}) in Mining, and flowers or
     * short grass (only in {@code BiomeDecorator}) in both. The Mining biome must list no placed feature.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400)
    public static void miningAndUtopiaDecorateFiveByFiveFullChunksWithoutAFarChunkWrite(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        FarChunkAppender appender = FarChunkAppender.attach();
        try {
            ServerLevel mining = W05GameTests.dimensionLevel(helper, "mining");
            ChunkPos miningCentre = freshCentre(mining, new ChunkPos(2400, 2400));
            FieldCount miningCount = generateField(mining, miningCentre, failures);

            ServerLevel utopia = W05GameTests.dimensionLevel(helper, "utopia");
            ChunkPos utopiaCentre = freshCentre(utopia, new ChunkPos(-2400, 2400));
            FieldCount utopiaCount = generateField(utopia, utopiaCentre, failures);

            List<String> far = appender.messages();
            for (int i = 0; i < Math.min(far.size(), 8); i++) {
                failures.add(owner(far.get(i), miningCentre, utopiaCentre) + ": " + far.get(i));
            }
            if (far.size() > 8) {
                failures.add((far.size() - 8) + " more far chunk writes");
            }
            if (miningCount.infestedStone == 0) {
                failures.add("mining field " + miningCentre + ": no infested stone, BiomeGenHills.decorate did not run");
            }
            if (miningCount.flowers + miningCount.shortGrass == 0) {
                failures.add("mining field " + miningCentre + ": no flower and no short grass, BiomeDecorator did not run");
            }
            if (utopiaCount.flowers + utopiaCount.shortGrass == 0) {
                failures.add("utopia field " + utopiaCentre + ": no flower and no short grass, BiomeDecorator did not run");
            }

            RegistryAccess registries = helper.getLevel().getServer().registryAccess();
            Biome miningBiome = registries.registryOrThrow(Registries.BIOME)
                    .getHolderOrThrow(ResourceKey.create(Registries.BIOME, rl("mining"))).value();
            int features = miningBiome.getGenerationSettings().features().stream().mapToInt(HolderSet::size).sum();
            if (features != 0) {
                failures.add("orespawn:mining still lists " + features + " placed features");
            }
            OreSpawn.LOG.info("R21 field test: mining {} infested stone {}, flowers {}, short grass {}; utopia {} flowers {}, short grass {}; far chunk writes {}",
                    miningCentre, miningCount.infestedStone, miningCount.flowers, miningCount.shortGrass,
                    utopiaCentre, utopiaCount.flowers, utopiaCount.shortGrass, far.size());
        } finally {
            appender.detach();
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    /**
     * R21 "Ein Weltseed-Pfad": the Crystal and Chaos terrain of a chunk depends on the seed given to
     * {@code createState} and not on the {@code RandomState}. Same {@code createState} seed with a
     * {@code RandomState} of another seed gives the same blocks; another {@code createState} seed gives different
     * blocks. Three chunks each, so a void chunk of Chaos cannot hide the difference.
     */
    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void crystalAndChaosTakeTheWorldSeedFromCreateState(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        List<String> failures = new ArrayList<>();
        List<ChunkPos> chunks = List.of(new ChunkPos(0, 0), new ChunkPos(3, 0), new ChunkPos(0, 3));
        for (String id : List.of("crystal", "chaos")) {
            try {
                int otherRandomState = 0;
                int otherSeed = 0;
                for (ChunkPos pos : chunks) {
                    ChunkAccess reference = generate(helper, decodeStem(server, id), 203L, 203L, pos);
                    otherRandomState += differences(reference, generate(helper, decodeStem(server, id), 203L, 77L, pos));
                    otherSeed += differences(reference, generate(helper, decodeStem(server, id), 77L, 203L, pos));
                }
                if (otherRandomState != 0) {
                    failures.add(id + ": " + otherRandomState + " blocks change with the RandomState seed alone");
                }
                if (otherSeed == 0) {
                    failures.add(id + ": another createState seed generates the same blocks");
                }
            } catch (Exception e) {
                failures.add(id + ": " + e);
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    /**
     * R21 "Äxte gehören in {@code #minecraft:enchantable/sharp_weapon}": every OreSpawn {@code ItemAxe} (manifest
     * {@code superclass_chain}) is in the tag, and Sharpness accepts it the way the anvil asks
     * ({@code Enchantment.canEnchant}).
     */
    @GameTest(template = ARENA)
    public static void orespawnAxesTakeSharpnessLikeTheir1710ItemAxe(GameTestHelper helper) {
        Holder<Enchantment> sharpness = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.SHARPNESS);
        List<DeferredItem<? extends Item>> axes = List.of(ModItems.ULTIMATE_AXE, ModItems.EMERALD_AXE, ModItems.RUBY_AXE,
                ModItems.AMETHYST_AXE, ModItems.CRYSTALWOOD_AXE, ModItems.CRYSTALPINK_AXE, ModItems.CRYSTALSTONE_AXE,
                ModItems.TIGERSEYE_AXE);
        List<String> failures = new ArrayList<>();
        for (DeferredItem<? extends Item> axe : axes) {
            ItemStack stack = new ItemStack(axe.get());
            if (!stack.is(ItemTags.SHARP_WEAPON_ENCHANTABLE) || !sharpness.value().canEnchant(stack)) {
                failures.add(axe.getId() + ": sharp_weapon " + stack.is(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                        + ", Sharpness canEnchant " + sharpness.value().canEnchant(stack));
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers

    /** Counts of the blocks only the decoration places. */
    private static final class FieldCount {
        int infestedStone;
        int flowers;
        int shortGrass;
    }

    /** The first centre east of {@code start} whose whole touched area was never generated or loaded. */
    private static ChunkPos freshCentre(ServerLevel level, ChunkPos start) {
        ServerChunkCache source = level.getChunkSource();
        // The GameTest world persists between runs and every run uses up one area, so further rows are scanned too.
        for (int attempt = 0; attempt < 64 * 64; attempt++) {
            ChunkPos centre = new ChunkPos(start.x + (attempt % 64) * (2 * RING + 1), start.z + (attempt / 64) * (2 * RING + 1));
            if (isFresh(source, centre)) {
                return centre;
            }
        }
        throw new IllegalStateException("no ungenerated " + (2 * RING + 1) + "x" + (2 * RING + 1) + " chunk area east of "
                + start + " in " + level.dimension().location());
    }

    private static boolean isFresh(ServerChunkCache source, ChunkPos centre) {
        for (int dx = -RING; dx <= RING; dx++) {
            for (int dz = -RING; dz <= RING; dz++) {
                ChunkPos pos = new ChunkPos(centre.x + dx, centre.z + dz);
                if (source.getChunkNow(pos.x, pos.z) != null || source.chunkMap.read(pos).join().isPresent()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Loads the 5x5 field to {@code FULL} (decoration included, on the worker threads) and counts its blocks. */
    private static FieldCount generateField(ServerLevel level, ChunkPos centre, List<String> failures) {
        FieldCount count = new FieldCount();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -FIELD; dx <= FIELD; dx++) {
            for (int dz = -FIELD; dz <= FIELD; dz++) {
                LevelChunk chunk = level.getChunk(centre.x + dx, centre.z + dz);
                if (chunk.getPersistedStatus() != ChunkStatus.FULL) {
                    failures.add(level.dimension().location() + ": chunk " + chunk.getPos() + " is " + chunk.getPersistedStatus());
                }
                int bx = chunk.getPos().getMinBlockX();
                int bz = chunk.getPos().getMinBlockZ();
                for (int y = 0; y < 256; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            BlockState state = chunk.getBlockState(pos.set(bx + x, y, bz + z));
                            if (state.is(Blocks.INFESTED_STONE)) {
                                count.infestedStone++;
                            } else if (state.is(Blocks.DANDELION) || state.is(Blocks.POPPY)) {
                                count.flowers++;
                            } else if (state.is(Blocks.SHORT_GRASS)) {
                                count.shortGrass++;
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    /** Which field a far chunk message belongs to, from the chunk coordinates it names. */
    private static String owner(String message, ChunkPos mining, ChunkPos utopia) {
        Matcher matcher = FAR_CHUNK_POS.matcher(message);
        if (!matcher.find()) {
            return "unknown";
        }
        int x = Integer.parseInt(matcher.group(1));
        int z = Integer.parseInt(matcher.group(2));
        if (Math.abs(x - mining.x) <= RING + 2 && Math.abs(z - mining.z) <= RING + 2) {
            return "mining";
        }
        if (Math.abs(x - utopia.x) <= RING + 2 && Math.abs(z - utopia.z) <= RING + 2) {
            return "utopia";
        }
        return "elsewhere";
    }

    /** {@code data/orespawn/dimension/<id>.json} through {@code LevelStem.CODEC}; a fresh generator every call. */
    private static LevelStem decodeStem(MinecraftServer server, String id) throws IOException {
        JsonElement json;
        try (BufferedReader reader = server.getResourceManager().openAsReader(rl("dimension/" + id + ".json"))) {
            json = JsonParser.parseReader(reader);
        }
        DataResult<LevelStem> decoded = LevelStem.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, server.registryAccess()), json);
        return decoded.getOrThrow(message -> new IllegalStateException("dimension/" + id + ".json: " + message));
    }

    /** One proto chunk through {@code createState(stateSeed)} and {@code fillFromNoise} with a {@code RandomState} of another seed. */
    private static ChunkAccess generate(GameTestHelper helper, LevelStem stem, long stateSeed, long randomStateSeed, ChunkPos pos) {
        RegistryAccess registries = helper.getLevel().getServer().registryAccess();
        ChunkGenerator generator = stem.generator();
        RandomState randomState = RandomState.create(registries.asGetterLookup(), NoiseGeneratorSettings.OVERWORLD, randomStateSeed);
        generator.createState(registries.lookupOrThrow(Registries.STRUCTURE_SET), randomState, stateSeed);
        ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.EMPTY, LevelHeightAccessor.create(0, 256),
                registries.registryOrThrow(Registries.BIOME), null);
        return generator.fillFromNoise(Blender.empty(), randomState, helper.getLevel().structureManager(), chunk).join();
    }

    private static int differences(ChunkAccess a, ChunkAccess b) {
        int differences = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int bx = a.getPos().getMinBlockX();
        int bz = a.getPos().getMinBlockZ();
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    pos.set(bx + x, y, bz + z);
                    if (a.getBlockState(pos) != b.getBlockState(pos)) {
                        differences++;
                    }
                }
            }
        }
        return differences;
    }

    /**
     * Collects every {@code Detected setBlock in a far chunk} logged while attached. The message comes from
     * {@code Util.LOGGER} on the chunk worker threads, so the queue is concurrent. Attached to the logger config of
     * {@code net.minecraft.Util} in every log4j context (FML may run more than one), removed again in
     * {@link #detach}.
     */
    private static final class FarChunkAppender extends AbstractAppender {
        private final Queue<String> messages = new ConcurrentLinkedQueue<>();
        private final List<LoggerConfig> configs = new ArrayList<>();
        private final List<LoggerContext> contexts = new ArrayList<>();

        private FarChunkAppender() {
            super("orespawn-r21-far-chunk", null, null, true, new Property[0]);
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
}
