package com.swbr.orespawn.gametest;

import com.mojang.authlib.GameProfile;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.cannonfodder.Lizard;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.entity.herbivore.Beaver;
import com.swbr.orespawn.entity.portal.Termite;
import com.swbr.orespawn.entity.worm.WormSmall;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.world.dimension.OreSpawnTeleporter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The fix1 wave (docs/port/BUGHUNT.md, DECISIONS R22 with its 2026-09-14 addendum): one test per fixed finding, each
 * built around the repro the bug hunt gave. Entity tests run in batches of their own and remove what they spawned,
 * so a mob with AI or a mock player does not leak into the next batch on the same grid (see W07GameTests).
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class Fix1GameTests {

    private static final String ARENA = "arena";
    private static final String ARENA_LARGE = "arena_large";

    private Fix1GameTests() {
    }

    // ------------------------------------------------------------------ registrations

    /**
     * OreSpawnMain.java:5188, :5199, :5273: egggirlfriend, eggbasilisc and eggboyfriend exist, name their types and
     * spawn them through the ported spawn path ({@code ItemSpawnEgg.spawnSomething}, the entry point of {@code useOn}).
     */
    @GameTest(template = ARENA, batch = "fix1_wiring")
    public static void missingSpawnEggsAreRegistered(GameTestHelper helper) {
        Map<String, Supplier<? extends EntityType<?>>> eggs = new LinkedHashMap<>();
        eggs.put("egggirlfriend", ModEntities.GIRLFRIEND);
        eggs.put("eggbasilisc", ModEntities.BASILISK);
        eggs.put("eggboyfriend", ModEntities.BOYFRIEND);
        BlockPos floor = helper.absolutePos(new BlockPos(6, 1, 6));
        eggs.forEach((id, type) -> {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, id));
            helper.assertTrue(item instanceof ItemSpawnEgg, "orespawn:" + id + " is " + item + ", expected a spawn egg");
            helper.assertTrue(((ItemSpawnEgg) item).getType() == type.get(),
                    "orespawn:" + id + " spawns " + ((ItemSpawnEgg) item).getType().toShortString() + ", expected " + type.get().toShortString());
            helper.assertTrue(ItemSpawnEgg.all().contains(item), "orespawn:" + id + " is not in ItemSpawnEgg.all() (tab, dispenser)");
            Entity spawned = ItemSpawnEgg.spawnSomething(type.get(), helper.getLevel(),
                    floor.getX() + 0.5, floor.getY() + 1.01, floor.getZ() + 0.5);
            try {
                helper.assertTrue(spawned != null && spawned.getType() == type.get() && spawned.isAlive()
                        && spawned.level() == helper.getLevel(), "orespawn:" + id + " spawned " + spawned + ", expected a live "
                        + type.get().toShortString() + " in the level");
            } finally {
                if (spawned != null) {
                    spawned.discard();
                }
            }
        });
        helper.succeed();
    }

    /** {@code TileEntityFurnace.getItemBurnTime}: every {@code Material.wood} block burned 300 ticks (R22). */
    @GameTest(template = ARENA, batch = "fix1_wiring")
    public static void woodBlocksBurnInTheVanillaFurnace(GameTestHelper helper) {
        for (String id : List.of("crystalplanks", "crystaltreelog", "skytreelog", "duplicatortreelog", "crystalworkbench")) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, id));
            helper.assertTrue(item != Items.AIR, "orespawn:" + id + " is not registered");
            int burn = new ItemStack(item).getBurnTime(RecipeType.SMELTING);
            helper.assertTrue(burn == 300, "orespawn:" + id + " burns " + burn + " ticks, expected 300");
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ gear

    /**
     * BUGHUNT "Miner's Dream": since R22 the tunnel clears granite, diorite, andesite, tuff and deepslate, and an
     * Extreme Torch stands on a deepslate floor. Ores ({@code #c:ores}, a deepslate ore included), bedrock
     * ({@code destroySpeed < 0}) and a chest (block entity) stay where they were.
     */
    @GameTest(template = ARENA_LARGE, timeoutTicks = 200, batch = "fix1_miners_dream")
    public static void minersDreamDigsModernRockAndLeavesOresBedrockAndChests(GameTestHelper helper) {
        List<Block> rock = List.of(Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.TUFF, Blocks.DEEPSLATE);
        for (int x = 18; x <= 30; x++) {
            for (int z = 3; z <= 70; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.DEEPSLATE);
                for (int y = 2; y <= 6; y++) {
                    helper.setBlock(new BlockPos(x, y, z), rock.get(Math.floorMod(z + y, rock.size())));
                }
                helper.setBlock(new BlockPos(x, 7, z), Blocks.STONE);
            }
        }
        Map<BlockPos, Block> kept = new LinkedHashMap<>();
        kept.put(new BlockPos(24, 3, 20), Blocks.IRON_ORE);
        kept.put(new BlockPos(22, 4, 40), Blocks.DEEPSLATE_DIAMOND_ORE);
        kept.put(new BlockPos(20, 2, 30), Blocks.BEDROCK);
        kept.put(new BlockPos(26, 2, 50), Blocks.CHEST);
        kept.forEach(helper::setBlock);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos feet = helper.absolutePos(new BlockPos(24, 2, 2));
        player.setPos(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
        ItemStack stack = new ItemStack(ModItems.MINERS_DREAM.get(), 16);
        InteractionResult result = useOn(helper, player, stack, hitTop(helper.absolutePos(new BlockPos(24, 1, 3))));
        helper.assertTrue(result == InteractionResult.CONSUME, "minersdream returned " + result);
        int torches = 0;
        for (int k = 0; k < 64; k++) {
            int z = 3 + k;
            for (int j = -5; j <= 5; j++) {
                int x = 24 + j;
                for (int y = 2; y <= 6; y++) {
                    BlockPos rel = new BlockPos(x, y, z);
                    BlockState s = helper.getBlockState(rel);
                    Block expected = kept.get(rel);
                    if (expected != null) {
                        helper.assertTrue(s.is(expected), expected + " at " + rel + " was replaced by " + s);
                    } else if (j == 0 && y == 2 && k % 5 == 0) {
                        helper.assertTrue(s.is(ModBlocks.EXTREME_TORCH.get()), "no Extreme Torch on the deepslate floor at " + rel + ": " + s);
                        torches++;
                    } else {
                        helper.assertTrue(s.isAir(), "tunnel block at " + rel + " is " + s + ", expected air");
                    }
                }
            }
        }
        helper.assertTrue(torches == 13, "found " + torches + " Extreme Torches, expected 13");
        helper.succeed();
    }

    /**
     * BUGHUNT "Chainsaw": mining one cherry log clears the cherry trunk and its leaves in the 11x16x11 box
     * ({@code #minecraft:logs}, {@code #minecraft:leaves}); a stone block in the same box stays.
     */
    @GameTest(template = ARENA, batch = "fix1_chainsaw")
    public static void chainsawClearCutsACherryTree(GameTestHelper helper) {
        List<BlockPos> tree = new ArrayList<>();
        for (int y = 2; y <= 6; y++) {
            BlockPos log = new BlockPos(6, y, 6);
            helper.setBlock(log, Blocks.CHERRY_LOG);
            tree.add(log);
        }
        for (int x = 4; x <= 8; x++) {
            for (int z = 4; z <= 8; z++) {
                for (int y = 5; y <= 7; y++) {
                    BlockPos leaf = new BlockPos(x, y, z);
                    if (x == 6 && z == 6 && y <= 6) {
                        continue;
                    }
                    helper.setBlock(leaf, Blocks.CHERRY_LEAVES);
                    tree.add(leaf);
                }
            }
        }
        BlockPos stone = new BlockPos(3, 2, 3);
        helper.setBlock(stone, Blocks.STONE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack chainsaw = new ItemStack(ModItems.CHAINSAW.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, chainsaw);
        BlockPos mined = helper.absolutePos(new BlockPos(6, 2, 6));
        chainsaw.getItem().mineBlock(chainsaw, helper.getLevel(), helper.getLevel().getBlockState(mined), mined, player);
        try {
            for (BlockPos rel : tree) {
                helper.assertTrue(helper.getBlockState(rel).isAir(), "the chainsaw left " + helper.getBlockState(rel) + " at " + rel);
            }
            helper.assertTrue(helper.getBlockState(stone).is(Blocks.STONE), "the chainsaw cut stone: " + helper.getBlockState(stone));
        } finally {
            helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(8.0)).forEach(Entity::discard);
        }
        helper.succeed();
    }

    /**
     * BUGHUNT "Big Hammer": BigHammer.java:36-38, {@code getMaxItemUseDuration} = 3000 on an {@code ItemSword} - a
     * right-click starts the block and a blockable 10-point hit leaves {@code 20 - (1 + 10) * 0.5}.
     */
    @GameTest(template = ARENA, batch = "fix1_big_hammer")
    public static void bigHammerBlocks(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack hammer = new ItemStack(ModItems.BIG_HAMMER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
        helper.assertTrue(hammer.getUseAnimation() == UseAnim.BLOCK && hammer.getUseDuration(player) == 3000,
                "bighammer uses " + hammer.getUseAnimation() + " for " + hammer.getUseDuration(player) + " ticks, expected BLOCK for 3000");
        helper.assertTrue(hammer.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.CONSUME
                && player.isUsingItem(), "right-click with the bighammer did not start the block");
        DamageSource cactus = helper.getLevel().damageSources().cactus();
        helper.assertTrue(player.hurt(cactus, 10.0F), "hurt() was refused");
        float expected = 20.0F - (1.0F + 10.0F) * 0.5F;
        helper.assertTrue(Math.abs(player.getHealth() - expected) < 0.05F,
                "blocking bighammer: health " + player.getHealth() + " after 10 cactus damage, expected " + expected);
        helper.succeed();
    }

    /**
     * BUGHUNT "Instant Garden" (InstantGarden.java:69-131, flag 2): a water source enclosed next to the garden volume
     * does not flow in once the garden clears its side, and concrete powder resting on a block the garden clears
     * stays in the air. The probes are placed ten ticks earlier so their own {@code onPlace} ticks have run.
     */
    @GameTest(template = ARENA_LARGE, timeoutTicks = 200, batch = "fix1_instant_garden")
    public static void instantGardenNextToWaterStaysDry(GameTestHelper helper) {
        BlockPos water = new BlockPos(16, 2, 10);    // j = -8, one block outside the volume (x 17..31)
        BlockPos wall = water.east();                // inside the volume, cleared by the garden
        BlockPos powder = new BlockPos(24, 12, 10);  // above the volume's top row (y 11)
        for (BlockPos stone : List.of(water.west(), water.north(), water.south(), water.above(), wall, powder.below())) {
            helper.setBlock(stone, Blocks.STONE);
        }
        helper.setBlock(water, Blocks.WATER);
        helper.setBlock(powder, Blocks.WHITE_CONCRETE_POWDER);
        helper.runAfterDelay(10, () -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos feet = helper.absolutePos(new BlockPos(24, 2, 3));
            player.setPos(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
            ItemStack stack = new ItemStack(ModItems.INSTANT_GARDEN.get(), 16);
            InteractionResult result = useOn(helper, player, stack, hitTop(helper.absolutePos(new BlockPos(24, 1, 4))));
            helper.assertTrue(result == InteractionResult.CONSUME, "instantgarden returned " + result);
            helper.assertTrue(helper.getBlockState(wall).isAir(), "precondition: the garden did not clear " + wall);
            helper.runAfterDelay(40, () -> {
                BlockState source = helper.getBlockState(water);
                helper.assertTrue(source.is(Blocks.WATER) && source.getFluidState().getAmount() == 8, "the water source changed: " + source);
                helper.assertTrue(helper.getBlockState(wall).isAir(), "water flowed into the garden at " + wall + ": " + helper.getBlockState(wall));
                helper.assertTrue(helper.getBlockState(powder).is(Blocks.WHITE_CONCRETE_POWDER) && helper.getBlockState(powder.below()).isAir(),
                        "the concrete powder over the garden fell: " + helper.getBlockState(powder) + " over " + helper.getBlockState(powder.below()));
                helper.succeed();
            });
        });
    }

    // ------------------------------------------------------------------ plants

    /**
     * BUGHUNT "soil": ItemExperienceTreeSeed.java:22 and BlockCorn.java:24 accept the whole {@code #minecraft:dirt}
     * tag (R22 addendum) - the seed plants its sapling on moss and on mud, corn survives on both; stone stays refused.
     */
    @GameTest(template = ARENA, batch = "fix1_soil")
    public static void seedsArePlaceableOnMossAndMud(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (Block soil : List.of(Blocks.MOSS_BLOCK, Blocks.MUD)) {
            BlockPos ground = soil == Blocks.MUD ? new BlockPos(3, 1, 3) : new BlockPos(8, 1, 3);
            helper.setBlock(ground, soil);
            ItemStack seed = new ItemStack(ModItems.EXPERIENCETREE_SEED.get());
            InteractionResult result = useOn(helper, player, seed, hitTop(helper.absolutePos(ground)));
            helper.assertTrue(result == InteractionResult.CONSUME, "experiencetree_seed on " + soil + " returned " + result);
            helper.assertTrue(helper.getBlockState(ground.above()).is(ModBlocks.EXPERIENCE_SAPLING.get()),
                    "experiencetree_seed on " + soil + " placed " + helper.getBlockState(ground.above()));
            helper.assertTrue(ModBlocks.CORN_0.get().defaultBlockState().canSurvive(helper.getLevel(), helper.absolutePos(ground.above())),
                    "corn cannot stand on " + soil);
        }
        BlockPos stone = new BlockPos(6, 1, 9);
        ItemStack seed = new ItemStack(ModItems.EXPERIENCETREE_SEED.get());
        helper.assertTrue(useOn(helper, player, seed, hitTop(helper.absolutePos(stone))) == InteractionResult.PASS,
                "experiencetree_seed was accepted on the stone floor");
        helper.succeed();
    }

    // ------------------------------------------------------------------ animals

    /**
     * BUGHUNT "worms": WormSmall.java:103-138 - a worm with no player near reads the block two above its feet; mud is
     * {@code #minecraft:dirt} and the worm keeps digging, while glass still removes it on the first tick (the control
     * that proves the branch runs). Created through the type, not {@code helper.spawn}, which makes the worm
     * persistent and turns its clipping off (WormSmall.tick).
     */
    @GameTest(template = ARENA, timeoutTicks = 100, batch = "fix1_worm")
    public static void wormSurvivesDiggingMud(GameTestHelper helper) {
        for (int y = 2; y <= 6; y++) {
            helper.setBlock(new BlockPos(3, y, 3), Blocks.MUD);
            helper.setBlock(new BlockPos(9, y, 9), Blocks.GLASS);
        }
        WormSmall inMud = addWorm(helper, new BlockPos(3, 2, 3));
        WormSmall inGlass = addWorm(helper, new BlockPos(9, 2, 9));
        helper.runAfterDelay(40, () -> {
            boolean mudAlive = inMud.isAlive();
            Entity.RemovalReason mudReason = inMud.getRemovalReason();
            int ticks = inMud.tickCount;
            Entity.RemovalReason glassReason = inGlass.getRemovalReason();
            inMud.discard();
            inGlass.discard();
            helper.assertTrue(glassReason == Entity.RemovalReason.DISCARDED, "control: the worm in glass was not removed ("
                    + glassReason + ")");
            helper.assertTrue(mudAlive && ticks >= 40, "the worm in mud is gone after " + ticks + " ticks (" + mudReason + ")");
            helper.succeed();
        });
    }

    private static WormSmall addWorm(GameTestHelper helper, BlockPos rel) {
        WormSmall worm = ModEntities.SMALL_WORM.get().create(helper.getLevel());
        if (worm == null) {
            throw new IllegalStateException("small_worm could not be created");
        }
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(rel));
        worm.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
        helper.getLevel().addFreshEntity(worm);
        return worm;
    }

    /**
     * BUGHUNT "Lizard": Lizard.java:48, {@code EntityAITempt ... Items.dye} - white dye ({@code #c:dyes}) tempts; a
     * stick does not. The registered goal at priority 3 is driven by hand as in W07CrosscutGameTests.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, batch = "fix1_lizard")
    public static void lizardFollowsWhiteDye(GameTestHelper helper) {
        Lizard lizard = spawnWithoutAi(helper, ModEntities.LIZARD.get(), new BlockPos(6, 2, 6));
        EntityAITempt tempt = temptAt(helper, lizard, 3);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.moveTo(lizard.getX() + 1.0, lizard.getY(), lizard.getZ(), 0.0F, 0.0F);
            expectTempt(helper, lizard, tempt, player, new ItemStack(Items.STICK), false, 0);
            expectTempt(helper, lizard, tempt, player, new ItemStack(Items.WHITE_DYE), true, 100);
        } finally {
            leave(helper, player);
            lizard.discard();
        }
        helper.succeed();
    }

    /**
     * BUGHUNT "Girlfriend": Girlfriend.java:129, {@code Blocks.red_flower} with every metadata - a cornflower
     * ({@code #minecraft:small_flowers}) tempts; the dandelion, which had its own {@code yellow_flower} branch, does not.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, batch = "fix1_girlfriend_tempt")
    public static void girlfriendIsTemptedByACornflower(GameTestHelper helper) {
        Girlfriend girlfriend = spawnWithoutAi(helper, ModEntities.GIRLFRIEND.get(), new BlockPos(6, 2, 6));
        EntityAITempt tempt = temptAt(helper, girlfriend, 2);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.moveTo(girlfriend.getX() + 1.0, girlfriend.getY(), girlfriend.getZ(), 0.0F, 0.0F);
            expectTempt(helper, girlfriend, tempt, player, new ItemStack(Items.DANDELION), false, 0);
            expectTempt(helper, girlfriend, tempt, player, new ItemStack(Items.CORNFLOWER), true, 100);
        } finally {
            leave(helper, player);
            girlfriend.discard();
        }
        helper.succeed();
    }

    private static <E extends Mob> E spawnWithoutAi(GameTestHelper helper, EntityType<E> type, BlockPos pos) {
        E mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        return mob;
    }

    private static EntityAITempt temptAt(GameTestHelper helper, Mob mob, int priority) {
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            Goal goal = wrapped.getGoal();
            if (wrapped.getPriority() == priority && goal instanceof EntityAITempt tempt) {
                return tempt;
            }
        }
        helper.fail(mob.getType().toShortString() + " has no EntityAITempt at priority " + priority);
        throw new IllegalStateException();
    }

    /** Asks the goal on every second tick over 30 ticks, the way {@code GoalSelector} does; ten due evaluations. */
    private static void expectTempt(GameTestHelper helper, Mob mob, EntityAITempt tempt, Player player, ItemStack held,
                                    boolean tempted, int startTick) {
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        boolean used = false;
        for (int t = startTick; t <= startTick + 30 && !used; t += 2) {
            mob.tickCount = t;
            used = tempt.canUse();
        }
        helper.assertTrue(used == tempted, held + " in the main hand " + (used ? "tempted" : "did not tempt") + " the "
                + mob.getType().toShortString());
    }

    /**
     * BUGHUNT "Termite": Termite.java:125-127 - a termite penned in by cherry planks ({@code #minecraft:planks}) eats
     * one of them. The eat roll is 1 in 200 per tick; 2400 ticks leave a chance of about 6e-6 that it never comes.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400, batch = "fix1_termite")
    public static void termiteEatsCherryPlanks(GameTestHelper helper) {
        List<BlockPos> planks = ring(6, 6, 2, 2, 3);
        planks.forEach(pos -> helper.setBlock(pos, Blocks.CHERRY_PLANKS));
        Termite termite = helper.spawn(ModEntities.TERMITE.get(), new BlockPos(6, 2, 6));
        helper.onEachTick(() -> {
            for (BlockPos pos : planks) {
                if (!helper.getBlockState(pos).is(Blocks.CHERRY_PLANKS)) {
                    clearMobs(helper);
                    helper.succeed();
                    return;
                }
            }
        });
        helper.assertTrue(termite.isAlive(), "precondition: the termite is alive");
    }

    /**
     * BUGHUNT "Beaver": Beaver.java:66 - a hurt beaver (the 1 in 30 roll) penned in by spruce fences
     * ({@code #minecraft:wooden_fences}) fells them. 2400 ticks leave no realistic chance of missing the roll.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400, batch = "fix1_beaver")
    public static void beaverFellsASpruceFence(GameTestHelper helper) {
        List<BlockPos> fences = ring(6, 6, 2, 2, 2);
        fences.forEach(pos -> helper.setBlock(pos, Blocks.SPRUCE_FENCE));
        Beaver beaver = helper.spawn(ModEntities.BEAVER.get(), new BlockPos(6, 2, 6));
        beaver.setHealth(5.0F);
        helper.onEachTick(() -> {
            for (BlockPos pos : fences) {
                if (!helper.getBlockState(pos).is(Blocks.SPRUCE_FENCE)) {
                    clearMobs(helper);
                    helper.succeed();
                    return;
                }
            }
        });
    }

    /** The square ring at Chebyshev distance {@code radius} around (cx, cz), rows {@code y0..y1}. */
    private static List<BlockPos> ring(int cx, int cz, int radius, int y0, int y1) {
        List<BlockPos> out = new ArrayList<>();
        for (int y = y0; y <= y1; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    if (Math.max(Math.abs(x - cx), Math.abs(z - cz)) == radius) {
                        out.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return out;
    }

    private static void clearMobs(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds().inflate(16.0), e -> !(e instanceof Player))
                .forEach(Entity::discard);
    }

    // ------------------------------------------------------------------ companions

    /**
     * BUGHUNT "owner death message" (R22 addendum): 1.7.10 {@code EntityTameable} sent the owner nothing when a tame
     * animal died. A tame vanilla wolf is the control that proves the capture works: its owner does get the line.
     */
    @GameTest(template = ARENA, batch = "fix1_death_message")
    public static void tamedGirlfriendDeathSendsNoOwnerMessage(GameTestHelper helper) {
        List<Component> messages = new ArrayList<>();
        ServerPlayer owner = recordingPlayer(helper, messages);
        try {
            owner.moveTo(helper.absoluteVec(new Vec3(1.5, 2.0, 1.5)));
            Wolf wolf = helper.spawn(EntityType.WOLF, new BlockPos(3, 2, 3));
            wolf.tame(owner);
            messages.clear();
            wolf.kill();
            helper.assertTrue(messages.size() == 1, "control: the tame wolf's owner got " + messages + ", expected one death message");

            for (EntityType<? extends TamableAnimal> type : List.<EntityType<? extends TamableAnimal>>of(
                    ModEntities.GIRLFRIEND.get(), ModEntities.BOYFRIEND.get())) {
                TamableAnimal animal = helper.spawn(type, new BlockPos(6, 2, 6));
                animal.tame(owner);
                helper.assertTrue(animal.getOwner() == owner, "precondition: " + type.toShortString() + " is not owned");
                messages.clear();
                // Girlfriend.attackEntityFrom caps every hit at 10 (:1078-1115), so one blow must be enough.
                animal.setHealth(1.0F);
                animal.kill();
                helper.assertTrue(animal.isDeadOrDying(), "precondition: " + type.toShortString() + " did not die");
                helper.assertTrue(messages.isEmpty(), "the owner of a tame " + type.toShortString() + " got " + messages);
            }
        } finally {
            leave(helper, owner);
            clearMobs(helper);
        }
        helper.succeed();
    }

    /** {@code makeMockServerPlayerInLevel} with the system chat recorded instead of sent. */
    private static ServerPlayer recordingPlayer(GameTestHelper helper, List<Component> messages) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "fix1-owner"), false);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }

            @Override
            public void sendSystemMessage(Component component, boolean bypassHiddenChat) {
                messages.add(component);
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }

    private static void leave(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    // ------------------------------------------------------------------ teleporter

    /**
     * BUGHUNT "return to the Overworld" (R22): outside the six OreSpawn dimensions {@code justPutMe} starts each
     * column at {@code WORLD_SURFACE} + 1. A stone pillar up to Y 195 at the player's column is the peak above Y 181:
     * the player lands on top of it at Y 196, not in the column below the literal 180 (which is solid all the way and
     * would send the search to a neighbouring column).
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, batch = "fix1_teleporter")
    public static void teleporterReturnLandsOnAPeakAboveY181(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 start = helper.absoluteVec(new Vec3(6.5, 2.0, 6.5));
        player.moveTo(start.x, start.y, start.z, 0.0F, 0.0F);
        int cx = (int) player.getX();
        int cz = (int) player.getZ();
        int floor = helper.absolutePos(new BlockPos(6, 1, 6)).getY();
        int top = 195;
        try {
            helper.assertTrue(floor < 170, "precondition: the arena floor lies at Y " + floor);
            for (int y = floor; y <= top; y++) {
                level.setBlock(new BlockPos(cx, y, cz), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            new OreSpawnTeleporter(level, level.dimension(), level).justPutMe(player);
            double expectedX = cx < 0 ? cx - 0.5 : cx + 0.5;
            double expectedZ = cz < 0 ? cz - 0.5 : cz + 0.5;
            helper.assertTrue(player.level() == level && player.getY() == top + 1 && player.getX() == expectedX
                            && player.getZ() == expectedZ,
                    "the return landed at " + player.position() + ", expected (" + expectedX + ", " + (top + 1) + ", " + expectedZ
                            + ") on the pillar");
        } finally {
            for (int y = floor + 1; y <= top; y++) {
                level.setBlock(new BlockPos(cx, y, cz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            leave(helper, player);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ shared helpers

    private static BlockHitResult hitTop(BlockPos abs) {
        return new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
    }

    private static InteractionResult useOn(GameTestHelper helper, Player player, ItemStack stack, BlockHitResult hit) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit));
    }
}
