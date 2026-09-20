package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.cannonfodder.Chipmunk;
import com.swbr.orespawn.entity.cannonfodder.EntityCannonFodder;
import com.swbr.orespawn.entity.cow.RedCow;
import com.swbr.orespawn.entity.herbivore.Beaver;
import com.swbr.orespawn.entity.rider.Ostrich;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.network.RiderControlMessage;
import com.swbr.orespawn.network.RiderControlMessageHandler;
import com.swbr.orespawn.network.RiderKeys;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.world.gen.BiomeGenHills;
import com.swbr.orespawn.world.gen.LegacyWorld;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * W06 (DECISIONS R9, R10, R15, R17, R21) on a real server.
 *
 * <p>Integration half: every living W06 type spawns through its registry factory, has the original maximum health
 * and the manifest hitbox, and is still alive after 100 ticks. One test per type, not one shared arena as in W05:
 * several W06 mobs hunt each other (Frog eats Cricket, Dragonfly bites butterflies, the cannon-fodder family fights
 * by hat colour), so a shared arena could fail on original behaviour rather than on a port error. Floor at helper
 * y = 1 (W03GameTests), ground mobs stand at y = 2, flyers start at y = 3.
 *
 * <p>Behaviour half (test wave): the RedCow family breeds with wheat into its own type, Camarasaurus and Ostrich are
 * tamed with apples and bred with the Crystal Apple, battle mob hats split teams, an Ostrich takes an empty-handed
 * rider and leaps on that rider's fly-up state, every W06 spawn egg spawns its entity, one drop sample per porter
 * matches its behaviour chapter, the 1.7.10 experience rules hold, and the Mining decoration draws the original
 * feature counts.
 *
 * <p>Expected health: manifest {@code max_health} (from {@code applyEntityAttributes}); the four cows have
 * none and inherit {@code EntityCow} 10. Expected hitbox: manifest {@code size}; the cows inherit
 * {@code EntityCow} 0.9 x 1.3.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W06GameTests {

    private static final String ARENA = "arena";

    private W06GameTests() {}

    private static void spawnAndTick(GameTestHelper helper, EntityType<? extends LivingEntity> type, int y, float maxHealth,
            float width, float height) {
        LivingEntity e = helper.spawn(type, new BlockPos(6, y, 6));
        String name = type.toShortString();
        helper.assertTrue(e.getMaxHealth() == maxHealth, name + " max health " + e.getMaxHealth() + ", expected " + maxHealth);
        helper.assertTrue(Math.abs(type.getWidth() - width) < 1.0e-4F && Math.abs(type.getHeight() - height) < 1.0e-4F,
                name + " is registered " + type.getWidth() + " x " + type.getHeight() + ", expected " + width + " x " + height);
        helper.assertTrue(Math.abs(e.getBbWidth() - width) < 1.0e-4F && Math.abs(e.getBbHeight() - height) < 1.0e-4F,
                name + " spawned with a hitbox of " + e.getBbWidth() + " x " + e.getBbHeight() + ", expected " + width + " x " + height);
        helper.runAfterDelay(100, () -> {
            helper.assertTrue(e.isAlive(), name + " did not survive 100 ticks");
            helper.assertTrue(e.tickCount >= 100, name + " ticked only " + e.tickCount + " times");
            helper.succeed();
        });
    }

    // ------------------------------------------------------------------ cows and water animals (w06-cows-fish)

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void appleCowSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.APPLE_COW.get(), 2, 10.0f, 0.9f, 1.3f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void goldenAppleCowSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.GOLDEN_APPLE_COW.get(), 2, 10.0f, 0.9f, 1.3f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void enchantedGoldenAppleCowSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), 2, 10.0f, 0.9f, 1.3f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void crystalAppleCowSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CRYSTAL_APPLE_COW.get(), 2, 10.0f, 0.9f, 1.3f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void goldFishSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.GOLD_FISH.get(), 3, 6.0f, 0.75f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void flounderSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.FLOUNDER.get(), 2, 5.0f, 0.55f, 0.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void whaleSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.WHALE.get(), 2, 100.0f, 1.5f, 2.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void frogSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.FROG.get(), 2, 8.0f, 0.75f, 0.75f);
    }

    // ------------------------------------------------------------------ critters (w06-birds-bugs)

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void dragonflySpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.DRAGONFLY.get(), 3, 10.0f, 1.5f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void birdSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.BIRD.get(), 3, 2.0f, 0.5f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void rubyBirdSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.RUBY_BIRD.get(), 3, 2.0f, 0.5f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void tShirtSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.T_SHIRT.get(), 3, 1.0f, 4.0f, 4.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void cliffRacerSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CLIFF_RACER.get(), 3, 5.0f, 0.75f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void coinSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.COIN.get(), 3, 1.0f, 1.5f, 1.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void cricketSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CRICKET.get(), 2, 3.0f, 0.1f, 0.1f);
    }

    // ------------------------------------------------------------------ herbivores (w06-herbivores)

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void camarasaurusSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CAMARASAURUS.get(), 2, 20.0f, 0.5f, 1.2f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void hydroliscSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.HYDROLISC.get(), 2, 100.0f, 0.5f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void baryonyxSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.BARYONYX.get(), 2, 40.0f, 1.5f, 2.8f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void stinkBugSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.STINK_BUG.get(), 2, 5.0f, 0.55f, 0.55f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void cassowarySpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CASSOWARY.get(), 2, 10.0f, 0.5f, 1.2f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void beaverSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.BEAVER.get(), 2, 15.0f, 0.6f, 0.8f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void peacockSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.PEACOCK.get(), 2, 15.0f, 0.65f, 1.2f);
    }

    // ------------------------------------------------------------------ cannon fodder, riders, ghosts

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void lizardSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.LIZARD.get(), 2, 30.0f, 1.5f, 1.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void chipmunkSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CHIPMUNK.get(), 2, 5.0f, 0.35f, 0.35f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void gazelleSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.GAZELLE.get(), 2, 15.0f, 0.6f, 1.8f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void velocityRaptorSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.VELOCITY_RAPTOR.get(), 2, 10.0f, 0.5f, 0.6f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void ostrichSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.OSTRICH.get(), 2, 25.0f, 0.85f, 2.1f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void ghostSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.GHOST.get(), 3, 2.0f, 0.5f, 1.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140)
    public static void ghostPumpkinSkellySpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.GHOST_PUMPKIN_SKELLY.get(), 3, 5.0f, 1.5f, 2.0f);
    }

    // ------------------------------------------------------------------ taming and breeding

    /** Puts the mock player one block east of {@code target} with {@code held} in the main hand. */
    private static void hold(Player player, Entity target, ItemStack held) {
        player.moveTo(target.getX() + 1.0, target.getY(), target.getZ(), 0.0F, 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
    }

    /** Waiting condition: a baby of exactly {@code type} is in the arena, and no baby of any other type. */
    private static void expectBaby(GameTestHelper helper, EntityType<?> type) {
        List<Entity> babies = helper.getLevel().getEntities((Entity) null, helper.getBounds(),
                e -> e instanceof AgeableMob mob && mob.isBaby());
        List<String> wrong = babies.stream().filter(e -> e.getType() != type).map(e -> e.getType().toShortString()).toList();
        if (!wrong.isEmpty()) {
            helper.fail(type.toShortString() + " parents produced a baby of " + wrong);
        }
        if (babies.isEmpty()) {
            throw new GameTestAssertException(type.toShortString() + " pair has no baby yet");
        }
    }

    /**
     * RedCow.java:21-27 and the three subclasses: {@code createChild} returns the cow's own class. The breeding item is
     * 1.7.10 {@code EntityCow}'s wheat (entity-06.md CrystalCow "Weizen-Zucht"); a Crystal Apple starts no love mode.
     */
    private static void breedsWithWheatIntoItsOwnType(GameTestHelper helper, EntityType<? extends RedCow> type) {
        RedCow first = helper.spawn(type, new BlockPos(5, 2, 6));
        RedCow second = helper.spawn(type, new BlockPos(7, 2, 6));
        String name = type.toShortString();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        hold(player, first, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        first.interact(player, InteractionHand.MAIN_HAND);
        helper.assertFalse(first.isInLove(), name + " fell in love with a Crystal Apple; EntityCow breeds with wheat only");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, name + " ate the Crystal Apple");
        hold(player, first, new ItemStack(Items.WHEAT));
        first.interact(player, InteractionHand.MAIN_HAND);
        hold(player, second, new ItemStack(Items.WHEAT));
        second.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(first.isInLove() && second.isInLove(), name + " did not fall in love with wheat: "
                + first.isInLove() + ", " + second.isInLove());
        helper.assertTrue(player.getMainHandItem().isEmpty(), name + " did not use up the wheat");
        helper.succeedWhen(() -> expectBaby(helper, type));
    }

    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void appleCowBreedsWithWheatIntoAnAppleCow(GameTestHelper helper) {
        breedsWithWheatIntoItsOwnType(helper, ModEntities.APPLE_COW.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void goldenAppleCowBreedsWithWheatIntoAGoldenAppleCow(GameTestHelper helper) {
        breedsWithWheatIntoItsOwnType(helper, ModEntities.GOLDEN_APPLE_COW.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void enchantedGoldenAppleCowBreedsWithWheatIntoItsOwnType(GameTestHelper helper) {
        breedsWithWheatIntoItsOwnType(helper, ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 400)
    public static void crystalAppleCowBreedsWithWheatIntoACrystalAppleCow(GameTestHelper helper) {
        breedsWithWheatIntoItsOwnType(helper, ModEntities.CRYSTAL_APPLE_COW.get());
    }

    /**
     * Camarasaurus.java:223-287 / Ostrich.java:156-249: an apple tames a wild one with 1/2 and is used up either way;
     * {@code isBreedingItem} is the Crystal Apple (Camarasaurus.java:353-355, Ostrich.java:586-588), so wheat starts no
     * love mode, two Crystal Apples do, and the pair gets a baby of its own type. 64 apple clicks all failing has a
     * probability of 2^-64.
     */
    private static <T extends TamableAnimal> void tamesWithApplesAndBreedsWithCrystalApples(GameTestHelper helper, EntityType<T> type) {
        T first = helper.spawn(type, new BlockPos(4, 2, 6));
        T second = helper.spawn(type, new BlockPos(7, 2, 6));
        String name = type.toShortString();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        hold(player, second, new ItemStack(Items.WHEAT));
        second.interact(player, InteractionHand.MAIN_HAND);
        helper.assertFalse(second.isInLove(), name + " fell in love with wheat; its breeding item is the Crystal Apple");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, name + " ate the wheat");

        hold(player, first, new ItemStack(Items.APPLE, 64));
        int clicks = 0;
        while (!first.isTame() && clicks < 64) {
            first.interact(player, InteractionHand.MAIN_HAND);
            clicks++;
        }
        helper.assertTrue(first.isTame(), name + " was not tamed by 64 apples");
        helper.assertTrue(player.getUUID().equals(first.getOwnerUUID()), name + " is owned by " + first.getOwnerUUID()
                + ", expected the player who fed the apple");
        helper.assertTrue(player.getMainHandItem().getCount() == 64 - clicks, name + " used " + (64 - player.getMainHandItem().getCount())
                + " apples in " + clicks + " clicks, expected one per click");
        helper.assertFalse(first.isInLove(), name + " fell in love with an apple");
        helper.assertFalse(second.isTame(), name + " a second, unfed one is tame");

        hold(player, first, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        first.interact(player, InteractionHand.MAIN_HAND);
        hold(player, second, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        second.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(first.isInLove() && second.isInLove(), name + " did not fall in love with the Crystal Apple: tamed "
                + first.isInLove() + ", wild " + second.isInLove());
        helper.assertTrue(player.getMainHandItem().isEmpty(), name + " did not use up the Crystal Apple");
        helper.succeedWhen(() -> expectBaby(helper, type));
    }

    @GameTest(template = ARENA, timeoutTicks = 500)
    public static void camarasaurusIsTamedWithApplesAndBredWithCrystalApples(GameTestHelper helper) {
        tamesWithApplesAndBreedsWithCrystalApples(helper, ModEntities.CAMARASAURUS.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 500)
    public static void ostrichIsTamedWithApplesAndBredWithCrystalApples(GameTestHelper helper) {
        tamesWithApplesAndBreedsWithCrystalApples(helper, ModEntities.OSTRICH.get());
    }

    // ------------------------------------------------------------------ battle mob hats

    private record Blow(LivingEntity victim, @Nullable Entity attacker) {}

    private static final Map<UUID, List<Blow>> WATCHED = new ConcurrentHashMap<>();
    private static final AtomicBoolean LISTENING = new AtomicBoolean();

    private static void watch(List<? extends LivingEntity> victims, List<Blow> into) {
        if (LISTENING.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDamageEvent.Post.class, W06GameTests::onDamagePost);
        }
        victims.forEach(v -> WATCHED.put(v.getUUID(), into));
    }

    private static void onDamagePost(LivingDamageEvent.Post event) {
        List<Blow> blows = WATCHED.get(event.getEntity().getUUID());
        if (blows != null) {
            blows.add(new Blow(event.getEntity(), event.getSource().getEntity()));
        }
    }

    /** Hat item click, then an empty-hand click by the same owner: EntityCannonFodder.java:75-96, 97-162, 194-207. */
    private static Chipmunk battleMob(GameTestHelper helper, Player owner, BlockPos pos, Item hat, int colour) {
        Chipmunk mob = helper.spawn(ModEntities.CHIPMUNK.get(), pos);
        hold(owner, mob, new ItemStack(hat));
        mob.interact(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(mob.getHatColor() == colour && mob.get_is_activated() == 1 && mob.isTame(),
                "a chipmunk fed " + hat + " has hat " + mob.getHatColor() + ", activation " + mob.get_is_activated()
                        + ", tame " + mob.isTame() + "; expected hat " + colour + ", activation 1, tame");
        helper.assertTrue(owner.getMainHandItem().isEmpty(), "the hat item " + hat + " was not used up");
        hold(owner, mob, ItemStack.EMPTY);
        mob.interact(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(mob.get_is_activated() == 2, "the owner's second click left activation at " + mob.get_is_activated());
        return mob;
    }

    private static boolean isCrossTeam(Blow blow) {
        return blow.attacker() instanceof EntityCannonFodder a && blow.victim() instanceof EntityCannonFodder v
                && a.getHatColor() != 0 && v.getHatColor() != 0 && a.getHatColor() != v.getHatColor();
    }

    /**
     * EntityCannonFodder.isSuitableTarget (:280-317): an activated battle mob attacks another battle mob only when that
     * one wears a hat of a different colour. Two red (carrot), one green (quinoa) and one blue (potato) chipmunk, all
     * activated by one owner, and a wild hatless chipmunk in the middle. Blows between hats must happen; after 40 more
     * ticks no blow may have gone from one red to the other or onto the hatless one.
     */
    @GameTest(template = ARENA, timeoutTicks = 500)
    public static void battleMobHatsSplitTeams(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.SURVIVAL);
        Chipmunk redA = battleMob(helper, owner, new BlockPos(3, 2, 3), Items.CARROT, 1);
        Chipmunk redB = battleMob(helper, owner, new BlockPos(3, 2, 9), Items.CARROT, 1);
        Chipmunk green = battleMob(helper, owner, new BlockPos(9, 2, 3), ModItems.QUINOA.get(), 2);
        Chipmunk blue = battleMob(helper, owner, new BlockPos(9, 2, 9), Items.POTATO, 3);
        Chipmunk wild = helper.spawn(ModEntities.CHIPMUNK.get(), new BlockPos(6, 2, 6));
        List<Chipmunk> all = List.of(redA, redB, green, blue, wild);
        List<Blow> blows = new CopyOnWriteArrayList<>();
        watch(all, blows);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    if (blows.stream().noneMatch(W06GameTests::isCrossTeam)) {
                        throw new GameTestAssertException("no battle mob has struck one with another hat yet (blows: " + blows.size() + ")");
                    }
                })
                .thenIdle(40)
                .thenExecute(() -> {
                    all.forEach(c -> WATCHED.remove(c.getUUID()));
                    List<String> wrong = new ArrayList<>();
                    for (Blow blow : blows) {
                        if (!(blow.attacker() instanceof EntityCannonFodder attacker)) {
                            continue;
                        }
                        if (blow.victim() == wild) {
                            wrong.add("hat " + attacker.getHatColor() + " struck the hatless chipmunk");
                        } else if (blow.victim() instanceof EntityCannonFodder victim && victim.getHatColor() == attacker.getHatColor()) {
                            wrong.add("hat " + attacker.getHatColor() + " struck its own team");
                        }
                    }
                    helper.assertTrue(wrong.isEmpty(), String.join("; ", wrong));
                    helper.assertTrue(wild.get_is_activated() == 0 && wild.getHatColor() == 0, "the wild chipmunk took a hat");
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ ostrich rider

    /** A server-bound payload context for {@link RiderControlMessageHandler}: work runs at once (as in W04GameTests). */
    private static IPayloadContext serverbound(Player sender) {
        return new IPayloadContext() {
            @Override
            public ICommonPacketListener listener() {
                throw new UnsupportedOperationException("no connection in a GameTest");
            }

            @Override
            public Player player() {
                return sender;
            }

            @Override
            public CompletableFuture<Void> enqueueWork(Runnable task) {
                task.run();
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
                return CompletableFuture.completedFuture(task.get());
            }

            @Override
            public PacketFlow flow() {
                return PacketFlow.SERVERBOUND;
            }

            @Override
            public void handle(CustomPacketPayload payload) {
                throw new UnsupportedOperationException("no connection in a GameTest");
            }

            @Override
            public void finishCurrentTask(ConfigurationTask.Type type) {
                throw new UnsupportedOperationException("no configuration phase in a GameTest");
            }
        };
    }

    /** The fly-up key through the payload's stream codec and handler, as the client sends it. */
    private static void sendFlyUp(Player sender, int keystate) {
        ByteBuf wire = Unpooled.buffer();
        RiderControlMessage.STREAM_CODEC.encode(wire, new RiderControlMessage(keystate));
        new RiderControlMessageHandler().handle(RiderControlMessage.STREAM_CODEC.decode(wire), serverbound(sender));
    }

    /**
     * Ostrich.java:156-249 and :396-536. A player with something in hand does not mount a wild ostrich, an empty hand
     * does. With the rider's fly-up key down ({@code RiderControlMessage(1)}, per rider, R15) the ostrich leaps once
     * ({@code motionY += 1 + velocity * 6}, :460-466) and lands again; while the key stays down it does not leap again
     * ({@code didjump = 20} counts down only while released); after 20 released ticks the key leaps again. The other
     * ostrich, whose rider never pressed the key, stays on the floor. The rider sits 0.15 behind the ostrich's centre
     * and 0.9 above its feet (:539-544).
     */
    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void ostrichTakesAnEmptyHandedRiderAndLeapsOnItsRidersFlyUpState(GameTestHelper helper) {
        Ostrich plain = helper.spawn(ModEntities.OSTRICH.get(), new BlockPos(3, 2, 3));
        Ostrich boosted = helper.spawn(ModEntities.OSTRICH.get(), new BlockPos(9, 2, 3));
        Player plainRider = helper.makeMockPlayer(GameType.SURVIVAL);
        Player boostedRider = helper.makeMockPlayer(GameType.SURVIVAL);

        hold(plainRider, plain, new ItemStack(Items.STICK));
        plain.interact(plainRider, InteractionHand.MAIN_HAND);
        helper.assertFalse(plainRider.isPassenger(), "a player holding a stick mounted a wild ostrich");
        hold(plainRider, plain, ItemStack.EMPTY);
        plain.interact(plainRider, InteractionHand.MAIN_HAND);
        hold(boostedRider, boosted, ItemStack.EMPTY);
        boosted.interact(boostedRider, InteractionHand.MAIN_HAND);
        helper.assertTrue(plain.getFirstPassenger() == plainRider && boosted.getFirstPassenger() == boostedRider,
                "an empty-handed click did not mount: " + plain.getFirstPassenger() + ", " + boosted.getFirstPassenger());

        sendFlyUp(boostedRider, 1);
        helper.assertTrue(RiderKeys.isFlyUp(boostedRider) && !RiderKeys.isFlyUp(plainRider),
                "the payload set fly-up on the wrong rider: sender " + RiderKeys.isFlyUp(boostedRider) + ", other " + RiderKeys.isFlyUp(plainRider));

        double[] plainMax = {0.0};
        double[] boostedMax = {0.0};
        double[] heldMax = {0.0};
        double[] againMax = {0.0};
        helper.startSequence()
                .thenExecuteFor(30, () -> {
                    plainMax[0] = Math.max(plainMax[0], helper.relativeVec(plain.position()).y);
                    boostedMax[0] = Math.max(boostedMax[0], helper.relativeVec(boosted.position()).y);
                })
                .thenExecute(() -> {
                    helper.assertTrue(boostedMax[0] > 3.5, "with fly-up the ostrich rose only to relative y " + boostedMax[0]
                            + " (feet start at 2.0)");
                    helper.assertTrue(plainMax[0] < 2.5, "without fly-up the ostrich rose to relative y " + plainMax[0]);
                    boosted.positionRider(boostedRider);
                    Vec3 expected = new Vec3(boosted.getX(), boosted.getY() + 0.9, boosted.getZ() - 0.15);
                    helper.assertTrue(boostedRider.position().distanceTo(expected) < 1.0e-6,
                            "the rider sits at " + boostedRider.position().subtract(boosted.position()) + " from the ostrich, expected (0, 0.9, -0.15)");
                })
                .thenExecuteFor(20, () -> heldMax[0] = Math.max(heldMax[0], helper.relativeVec(boosted.position()).y))
                .thenExecute(() -> {
                    helper.assertTrue(heldMax[0] < 2.5, "with the key still held the ostrich leapt again to relative y " + heldMax[0]);
                    sendFlyUp(boostedRider, 0);
                })
                .thenIdle(22)
                .thenExecute(() -> sendFlyUp(boostedRider, 1))
                .thenExecuteFor(25, () -> {
                    againMax[0] = Math.max(againMax[0], helper.relativeVec(boosted.position()).y);
                    plainMax[0] = Math.max(plainMax[0], helper.relativeVec(plain.position()).y);
                })
                .thenExecute(() -> {
                    helper.assertTrue(againMax[0] > 3.5, "released and pressed again, the ostrich rose only to relative y " + againMax[0]);
                    helper.assertTrue(plainMax[0] < 2.5, "the ostrich of the rider without fly-up rose to relative y " + plainMax[0]);
                    helper.assertTrue(plain.getFirstPassenger() == plainRider && boosted.getFirstPassenger() == boostedRider,
                            "a rider fell off");
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ spawn eggs

    private record EggCase(DeferredItem<ItemSpawnEgg> egg, EntityType<?> type) {}

    /**
     * Every W06 egg in the manifest (28; {@code RubyBird} has none) spawns its entity through the item's own
     * {@code useOn}, one block and a hair above the clicked block's centre, and is used up in survival.
     */
    @GameTest(template = ARENA)
    public static void everyWaveSixSpawnEggSpawnsItsEntity(GameTestHelper helper) {
        List<EggCase> eggs = List.of(
                new EggCase(ModItems.EGG_RED_COW, ModEntities.APPLE_COW.get()),
                new EggCase(ModItems.EGG_GOLD_COW, ModEntities.GOLDEN_APPLE_COW.get()),
                new EggCase(ModItems.EGG_ENCHANTED_COW, ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get()),
                new EggCase(ModItems.EGG_CRYSTAL_COW, ModEntities.CRYSTAL_APPLE_COW.get()),
                new EggCase(ModItems.EGG_GOLD_FISH, ModEntities.GOLD_FISH.get()),
                new EggCase(ModItems.EGG_FLOUNDER, ModEntities.FLOUNDER.get()),
                new EggCase(ModItems.EGG_WHALE, ModEntities.WHALE.get()),
                new EggCase(ModItems.EGG_FROG, ModEntities.FROG.get()),
                new EggCase(ModItems.EGG_DRAGONFLY, ModEntities.DRAGONFLY.get()),
                new EggCase(ModItems.EGG_COCKATEIL, ModEntities.BIRD.get()),
                new EggCase(ModItems.EGG_TSHIRT, ModEntities.T_SHIRT.get()),
                new EggCase(ModItems.EGG_CLIFF_RACER, ModEntities.CLIFF_RACER.get()),
                new EggCase(ModItems.EGG_COIN, ModEntities.COIN.get()),
                new EggCase(ModItems.EGG_CRICKET, ModEntities.CRICKET.get()),
                new EggCase(ModItems.EGG_CAMARASAURUS, ModEntities.CAMARASAURUS.get()),
                new EggCase(ModItems.EGG_HYDROLISC, ModEntities.HYDROLISC.get()),
                new EggCase(ModItems.EGG_BARYONYX, ModEntities.BARYONYX.get()),
                new EggCase(ModItems.EGG_STINK_BUG, ModEntities.STINK_BUG.get()),
                new EggCase(ModItems.EGG_CASSOWARY, ModEntities.CASSOWARY.get()),
                new EggCase(ModItems.EGG_BEAVER, ModEntities.BEAVER.get()),
                new EggCase(ModItems.EGG_PEACOCK, ModEntities.PEACOCK.get()),
                new EggCase(ModItems.EGG_LIZARD, ModEntities.LIZARD.get()),
                new EggCase(ModItems.EGG_CHIPMUNK, ModEntities.CHIPMUNK.get()),
                new EggCase(ModItems.EGG_GAZELLE, ModEntities.GAZELLE.get()),
                new EggCase(ModItems.EGG_VELOCITY_RAPTOR, ModEntities.VELOCITY_RAPTOR.get()),
                new EggCase(ModItems.EGG_OSTRICH, ModEntities.OSTRICH.get()),
                new EggCase(ModItems.EGG_GHOST, ModEntities.GHOST.get()),
                new EggCase(ModItems.EGG_GHOST_SKELLY, ModEntities.GHOST_PUMPKIN_SKELLY.get()));
        BlockPos floor = helper.absolutePos(new BlockPos(6, 1, 6));
        AABB around = new AABB(floor).move(0.0, 1.0, 0.0).inflate(1.0);
        List<String> failures = new ArrayList<>();
        for (EggCase c : eggs) {
            String id = c.egg().getId().getPath();
            if (c.egg().get().getType() != c.type()) {
                failures.add(id + " carries " + EntityType.getKey(c.egg().get().getType()) + ", expected " + EntityType.getKey(c.type()));
                continue;
            }
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(c.egg().get()));
            ItemStack stack = player.getMainHandItem();
            Set<Entity> before = new HashSet<>(helper.getLevel().getEntities((Entity) null, around, e -> e.getType() == c.type()));
            InteractionResult result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0.0, 0.5, 0.0), Direction.UP, floor, false)));
            List<Entity> spawned = helper.getLevel().getEntities((Entity) null, around,
                    e -> e.getType() == c.type() && !before.contains(e));
            if (spawned.size() != 1) {
                failures.add(id + " spawned " + spawned.size() + " " + EntityType.getKey(c.type()));
            } else {
                Entity e = spawned.get(0);
                if (Math.abs(e.getX() - (floor.getX() + 0.5)) > 1.0e-6 || Math.abs(e.getY() - (floor.getY() + 1.01)) > 1.0e-6
                        || Math.abs(e.getZ() - (floor.getZ() + 0.5)) > 1.0e-6) {
                    failures.add(id + " spawned at " + helper.relativeVec(e.position()) + ", expected (6.5, 2.01, 6.5)");
                }
                e.discard();
            }
            if (!result.consumesAction() || !stack.isEmpty()) {
                failures.add(id + ": result " + result + ", " + stack.getCount() + " left in hand");
            }
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    // ------------------------------------------------------------------ experience

    private record XpCase(EntityType<? extends LivingEntity> type, int min, int max) {}

    /**
     * Every W06 class but the ghosts extends 1.7.10 {@code EntityAnimal}, whose {@code getExperiencePoints}
     * ({@code wf.e}) ignores {@code experienceValue} and returns {@code 1 + rand.nextInt(3)} (W04 lesson): 200 draws in
     * 1..3 with all three seen. Ghost and GhostSkelly extend {@code EntityAmbientCreature}; {@code EntityLiving.e(yz)}
     * ({@code sw.e}, javap) returns {@code experienceValue} plus 1-3 per equipped item - none - so exactly 5
     * (Ghost.java:21) and 10 (GhostSkelly.java:23).
     */
    @GameTest(template = ARENA)
    public static void waveSixMobsGiveTheir1710Experience(GameTestHelper helper) {
        List<XpCase> cases = List.of(
                new XpCase(ModEntities.APPLE_COW.get(), 1, 3), new XpCase(ModEntities.GOLDEN_APPLE_COW.get(), 1, 3),
                new XpCase(ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), 1, 3), new XpCase(ModEntities.CRYSTAL_APPLE_COW.get(), 1, 3),
                new XpCase(ModEntities.GOLD_FISH.get(), 1, 3), new XpCase(ModEntities.FLOUNDER.get(), 1, 3),
                new XpCase(ModEntities.WHALE.get(), 1, 3), new XpCase(ModEntities.FROG.get(), 1, 3),
                new XpCase(ModEntities.DRAGONFLY.get(), 1, 3), new XpCase(ModEntities.BIRD.get(), 1, 3),
                new XpCase(ModEntities.RUBY_BIRD.get(), 1, 3), new XpCase(ModEntities.T_SHIRT.get(), 1, 3),
                new XpCase(ModEntities.CLIFF_RACER.get(), 1, 3), new XpCase(ModEntities.COIN.get(), 1, 3),
                new XpCase(ModEntities.CRICKET.get(), 1, 3), new XpCase(ModEntities.CAMARASAURUS.get(), 1, 3),
                new XpCase(ModEntities.HYDROLISC.get(), 1, 3), new XpCase(ModEntities.BARYONYX.get(), 1, 3),
                new XpCase(ModEntities.STINK_BUG.get(), 1, 3), new XpCase(ModEntities.CASSOWARY.get(), 1, 3),
                new XpCase(ModEntities.BEAVER.get(), 1, 3), new XpCase(ModEntities.PEACOCK.get(), 1, 3),
                new XpCase(ModEntities.LIZARD.get(), 1, 3), new XpCase(ModEntities.CHIPMUNK.get(), 1, 3),
                new XpCase(ModEntities.GAZELLE.get(), 1, 3), new XpCase(ModEntities.VELOCITY_RAPTOR.get(), 1, 3),
                new XpCase(ModEntities.OSTRICH.get(), 1, 3), new XpCase(ModEntities.GHOST.get(), 5, 5),
                new XpCase(ModEntities.GHOST_PUMPKIN_SKELLY.get(), 10, 10));
        List<String> failures = new ArrayList<>();
        for (XpCase c : cases) {
            LivingEntity mob = helper.spawn(c.type(), new BlockPos(6, 2, 6));
            Set<Integer> seen = new TreeSet<>();
            for (int i = 0; i < 200; i++) {
                seen.add(mob.getExperienceReward(helper.getLevel(), null));
            }
            Set<Integer> expected = new TreeSet<>();
            for (int v = c.min(); v <= c.max(); v++) {
                expected.add(v);
            }
            if (!seen.equals(expected)) {
                failures.add(c.type().toShortString() + " gave " + seen + ", expected " + expected);
            }
            mob.discard();
        }
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    // ------------------------------------------------------------------ drops

    /** An allowed item and its count range per death; an item not listed must not drop at all. */
    private record Range(Item item, int min, int max) {}

    /** Kills {@code mob} with the generic kill (no player, looting 0) and returns and removes the item entities it left. */
    private static Map<Item, Integer> killAndCollect(GameTestHelper helper, Mob mob) {
        AABB box = mob.getBoundingBox().inflate(6.0);
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, box).forEach(Entity::discard);
        mob.kill();
        Map<Item, Integer> drops = new LinkedHashMap<>();
        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, box)) {
            if (!item.isRemoved()) {
                drops.merge(item.getItem().getItem(), item.getItem().getCount(), Integer::sum);
                item.discard();
            }
        }
        return drops;
    }

    /**
     * {@code trials} deaths of fresh mobs of {@code type}: every drop is one of {@code ranges} with a count inside its
     * range; {@code total} (if not negative) is the exact number of items per death; {@code mustSee} lists counts that
     * have to appear at least once over all deaths.
     */
    private static <T extends Mob> void checkDrops(GameTestHelper helper, List<String> failures, EntityType<T> type, String what,
            int trials, Consumer<T> setup, List<Range> ranges, int total, Map<Item, Set<Integer>> mustSee) {
        Map<Item, Set<Integer>> seen = new HashMap<>();
        int before = failures.size();
        for (int t = 0; t < trials && failures.size() - before < 8; t++) {
            T mob = helper.spawn(type, new BlockPos(6, 2, 6));
            setup.accept(mob);
            Map<Item, Integer> drops = killAndCollect(helper, mob);
            for (Map.Entry<Item, Integer> drop : drops.entrySet()) {
                if (ranges.stream().noneMatch(r -> r.item() == drop.getKey())) {
                    failures.add(what + " dropped " + drop.getValue() + " " + drop.getKey());
                }
            }
            for (Range r : ranges) {
                int n = drops.getOrDefault(r.item(), 0);
                seen.computeIfAbsent(r.item(), k -> new TreeSet<>()).add(n);
                if (n < r.min() || n > r.max()) {
                    failures.add(what + " dropped " + n + " " + r.item() + ", expected " + r.min() + ".." + r.max());
                }
            }
            int sum = drops.values().stream().mapToInt(Integer::intValue).sum();
            if (total >= 0 && sum != total) {
                failures.add(what + " dropped " + sum + " items " + drops + ", expected exactly " + total);
            }
        }
        for (Map.Entry<Item, Set<Integer>> must : mustSee.entrySet()) {
            Set<Integer> got = seen.getOrDefault(must.getKey(), Set.of());
            if (!got.containsAll(must.getValue())) {
                failures.add(what + ": counts of " + must.getKey() + " over " + trials + " deaths were " + got + ", expected to include " + must.getValue());
            }
        }
    }

    private static Set<Integer> counts(int min, int max) {
        Set<Integer> s = new TreeSet<>();
        for (int i = min; i <= max; i++) {
            s.add(i);
        }
        return s;
    }

    private static <T extends Mob> Consumer<T> wild() {
        return m -> {};
    }

    private static <T extends TamableAnimal> Consumer<T> tamed() {
        return m -> m.setTame(true, false);
    }

    /**
     * w06-cows-fish. entity-06.md CrystalCow and the cow sources (RedCow.java:14-19, GoldCow.java:14-21,
     * EnchantedCow.java:16-29, CrystalCow.java:14-21) on top of 1.7.10 {@code EntityCow.dropFewItems}
     * ({@code rand(3)} leather, {@code rand(3) + 1} beef); entity-14.md Whale: 20..44 raw fish (cod). Looting 0,
     * not burning. Uniform single rolls (p = 1/3) must show every value in 60 deaths.
     */
    @GameTest(template = ARENA)
    public static void cowFamilyAndWhaleDropWhatTheirChaptersSay(GameTestHelper helper) {
        List<String> f = new ArrayList<>();
        Range leather = new Range(Items.LEATHER, 0, 2);
        Range beef = new Range(Items.BEEF, 1, 3);
        Map<Item, Set<Integer>> cowSeen = Map.of(Items.LEATHER, counts(0, 2), Items.BEEF, counts(1, 3));
        checkDrops(helper, f, ModEntities.APPLE_COW.get(), "apple_cow", 60, wild(),
                List.of(new Range(Items.APPLE, 0, 2), leather, beef), -1,
                Map.of(Items.APPLE, counts(0, 2), Items.LEATHER, counts(0, 2), Items.BEEF, counts(1, 3)));
        checkDrops(helper, f, ModEntities.GOLDEN_APPLE_COW.get(), "golden_apple_cow", 60, wild(),
                List.of(new Range(Items.APPLE, 0, 4), new Range(Items.GOLDEN_APPLE, 1, 1), leather, beef), -1, cowSeen);
        checkDrops(helper, f, ModEntities.ENCHANTED_GOLDEN_APPLE_COW.get(), "enchanted_golden_apple_cow", 60, wild(),
                List.of(new Range(Items.APPLE, 0, 5), new Range(Items.GOLDEN_APPLE, 2, 2), new Range(Items.ENCHANTED_GOLDEN_APPLE, 1, 1),
                        leather, beef), -1, cowSeen);
        checkDrops(helper, f, ModEntities.CRYSTAL_APPLE_COW.get(), "crystal_apple_cow", 60, wild(),
                List.of(new Range(ModItems.CRYSTAL_APPLE.get(), 0, 2), new Range(Items.APPLE, 1, 3), leather, beef), -1,
                Map.of(ModItems.CRYSTAL_APPLE.get(), counts(0, 2), Items.APPLE, counts(1, 3), Items.LEATHER, counts(0, 2), Items.BEEF, counts(1, 3)));
        checkDrops(helper, f, ModEntities.WHALE.get(), "whale", 20, wild(), List.of(new Range(Items.COD, 20, 44)), -1, Map.of());
        helper.assertTrue(f.isEmpty(), String.join("; ", f));
        helper.succeed();
    }

    /**
     * w06-birds-bugs. entity-06.md Coin: exactly one item, {@code rand(10)} over diamond, uranium nugget, titanium
     * nugget, emerald, the emerald axe, shovel, pickaxe and hoe, the Coin egg, or (9) the emerald sword. 300 deaths
     * must show all ten (missing one by chance: about 2e-13).
     */
    @GameTest(template = ARENA)
    public static void coinDropsExactlyOneOfItsTenPrizes(GameTestHelper helper) {
        List<String> f = new ArrayList<>();
        List<Item> prizes = List.of(Items.DIAMOND, ModItems.URANIUM_NUGGET.get(), ModItems.TITANIUM_NUGGET.get(), Items.EMERALD,
                ModItems.EMERALD_AXE.get(), ModItems.EMERALD_SHOVEL.get(), ModItems.EMERALD_PICKAXE.get(), ModItems.EMERALD_HOE.get(),
                ModItems.EGG_COIN.get(), ModItems.EMERALD_SWORD.get());
        List<Range> ranges = prizes.stream().map(i -> new Range(i, 0, 1)).toList();
        Map<Item, Set<Integer>> mustSee = new HashMap<>();
        prizes.forEach(i -> mustSee.put(i, Set.of(1)));
        checkDrops(helper, f, ModEntities.COIN.get(), "coin", 300, wild(), ranges, 1, mustSee);
        helper.assertTrue(f.isEmpty(), String.join("; ", f));
        helper.succeed();
    }

    /**
     * w06-herbivores. Camarasaurus.java:312-321 (entity-05.md): wild nothing, tamed 2..6 poppies; 100 tamed deaths must
     * show all five counts.
     */
    @GameTest(template = ARENA)
    public static void camarasaurusDropsPoppiesOnlyWhenTamed(GameTestHelper helper) {
        List<String> f = new ArrayList<>();
        checkDrops(helper, f, ModEntities.CAMARASAURUS.get(), "wild camarasaurus", 40, wild(), List.of(), 0, Map.of());
        checkDrops(helper, f, ModEntities.CAMARASAURUS.get(), "tamed camarasaurus", 100, tamed(),
                List.of(new Range(Items.POPPY, 2, 6)), -1, Map.of(Items.POPPY, counts(2, 6)));
        helper.assertTrue(f.isEmpty(), String.join("; ", f));
        helper.succeed();
    }

    /**
     * w06-cannonfodder. Chipmunk.java:216-232 (entity-06.md): tamed 2..6 poppies, wild the 1.7.10
     * {@code EntityLiving.dropFewItems} roll of wheat, {@code rand(3)} = 0..2.
     */
    @GameTest(template = ARENA)
    public static void chipmunkDropsWheatWildAndPoppiesTamed(GameTestHelper helper) {
        List<String> f = new ArrayList<>();
        checkDrops(helper, f, ModEntities.CHIPMUNK.get(), "wild chipmunk", 100, wild(),
                List.of(new Range(Items.WHEAT, 0, 2)), -1, Map.of(Items.WHEAT, counts(0, 2)));
        checkDrops(helper, f, ModEntities.CHIPMUNK.get(), "tamed chipmunk", 100, tamed(),
                List.of(new Range(Items.POPPY, 2, 6)), -1, Map.of(Items.POPPY, counts(2, 6)));
        helper.assertTrue(f.isEmpty(), String.join("; ", f));
        helper.succeed();
    }

    /**
     * w06-riders-ghosts. Ostrich.java:270-286 (entity-10.md): tamed 2..6 poppies, wild the vanilla roll of feathers
     * ({@code rand(3)} = 0..2).
     */
    @GameTest(template = ARENA)
    public static void ostrichDropsFeathersWildAndPoppiesTamed(GameTestHelper helper) {
        List<String> f = new ArrayList<>();
        checkDrops(helper, f, ModEntities.OSTRICH.get(), "wild ostrich", 100, wild(),
                List.of(new Range(Items.FEATHER, 0, 2)), -1, Map.of(Items.FEATHER, counts(0, 2)));
        checkDrops(helper, f, ModEntities.OSTRICH.get(), "tamed ostrich", 100, tamed(),
                List.of(new Range(Items.POPPY, 2, 6)), -1, Map.of(Items.POPPY, counts(2, 6)));
        helper.assertTrue(f.isEmpty(), String.join("; ", f));
        helper.succeed();
    }

    // ------------------------------------------------------------------ R21: Mining decoration counts

    /** One recorded draw of a {@link RecordingRandom}. */
    private record Draw(char kind, int bound, double value) {
        @Override
        public String toString() {
            return kind == 'I' ? "I" + bound + "=" + (int) value : String.valueOf(kind);
        }
    }

    /**
     * {@link Random} that records every draw a generator makes through its public methods. {@code java.util.Random}
     * implements {@code nextInt(int)}, {@code nextFloat}, {@code nextDouble} and {@code nextLong} on {@code next(int)},
     * never on each other, so each call is recorded exactly once.
     */
    private static final class RecordingRandom extends Random {
        final List<Draw> draws = new ArrayList<>();

        RecordingRandom(long seed) {
            super(seed);
        }

        @Override
        public int nextInt(int bound) {
            int v = super.nextInt(bound);
            this.draws.add(new Draw('I', bound, v));
            return v;
        }

        @Override
        public int nextInt() {
            int v = super.nextInt();
            this.draws.add(new Draw('U', 0, v));
            return v;
        }

        @Override
        public float nextFloat() {
            float v = super.nextFloat();
            this.draws.add(new Draw('F', 0, v));
            return v;
        }

        @Override
        public double nextDouble() {
            double v = super.nextDouble();
            this.draws.add(new Draw('D', 0, v));
            return v;
        }

        @Override
        public long nextLong() {
            long v = super.nextLong();
            this.draws.add(new Draw('L', 0, v));
            return v;
        }

        @Override
        public boolean nextBoolean() {
            boolean v = super.nextBoolean();
            this.draws.add(new Draw('B', 0, v ? 1 : 0));
            return v;
        }
    }

    /**
     * One {@code WorldGenMinable(size)} call at {@code draws[i]}: the position draws with {@code bounds}, then
     * {@code nextFloat} (angle), two {@code nextInt(3)} (end heights) and {@code size + 1} {@code nextDouble} (one
     * ellipsoid each). Returns the index after it, or -1 after adding a failure.
     */
    private static int expectVein(List<Draw> draws, int i, int[] bounds, int size, String what, List<String> failures) {
        List<String> expected = new ArrayList<>();
        for (int b : bounds) {
            expected.add("I" + b);
        }
        expected.add("F");
        expected.add("I3");
        expected.add("I3");
        for (int n = 0; n <= size; n++) {
            expected.add("D");
        }
        for (int k = 0; k < expected.size(); k++) {
            int at = i + k;
            String got = at < 0 || at >= draws.size() ? "end" : (draws.get(at).kind() == 'I' ? "I" + draws.get(at).bound() : String.valueOf(draws.get(at).kind()));
            if (!got.equals(expected.get(k))) {
                failures.add(what + ": draw " + at + " is " + got + ", expected " + expected.get(k) + " (vein of " + size + " with position bounds "
                        + java.util.Arrays.toString(bounds) + ")");
                return -1;
            }
        }
        return i + expected.size();
    }

    private static boolean isInt(Draw d, int bound) {
        return d.kind() == 'I' && d.bound() == bound;
    }

    /** First centre east of {@code start} whose 7x7 chunk area was never generated or loaded (as R21GameTests). */
    private static ChunkPos freshCentre(ServerLevel level, ChunkPos start, int ring) {
        ServerChunkCache source = level.getChunkSource();
        // The GameTest world persists between runs and every run uses up one area, so rows further south are scanned
        // too once the first row east of start is used up.
        for (int attempt = 0; attempt < 64 * 64; attempt++) {
            ChunkPos centre = new ChunkPos(start.x + (attempt % 64) * (2 * ring + 1), start.z + (attempt / 64) * (2 * ring + 1));
            boolean fresh = true;
            for (int dx = -ring; dx <= ring && fresh; dx++) {
                for (int dz = -ring; dz <= ring && fresh; dz++) {
                    ChunkPos pos = new ChunkPos(centre.x + dx, centre.z + dz);
                    fresh = source.getChunkNow(pos.x, pos.z) == null && source.chunkMap.read(pos).join().isEmpty();
                }
            }
            if (fresh) {
                return centre;
            }
        }
        throw new IllegalStateException("no fresh chunk area east or south of " + start);
    }

    /**
     * DECISIONS R21 "Dekoration als portierter 1.7.10-Java-Code", counted.
     *
     * <p>Real pipeline: a fresh 3x3 field of {@code FULL} chunks in {@code orespawn:mining}. Emerald ore only comes from
     * a chunk's own populate ({@code aid.a}: {@code 3 + nextInt(6)} tries at {@code chunk + nextInt(16)}, Y
     * {@code nextInt(28) + 4}), so every chunk holds at most 8, all in Y 4..31; the silverfish veins
     * ({@code WorldGenMinable(monster_egg, 8)} at Y {@code nextInt(64)}) reach at most Y 64. Both must occur in the field.
     *
     * <p>Exact counts: {@link BiomeGenHills#decorate} once more on the centre chunk with a {@link RecordingRandom}. The draw
     * sequence starts with the ore pass of {@code aia.a()} - bytecode {@code aia.<init>}/{@code aia.a()}: 20 dirt (0..256,
     * vein 32), 10 gravel (0..256, 32), 20 coal (0..128, 16), 20 iron (0..64, 8), 2 gold (0..32, 8), 8 redstone (0..16, 7),
     * 1 diamond (0..16, 7), 1 lapis ({@code nextInt(16) + nextInt(16)}, 6) - and ends with {@code aid.a}: one
     * {@code nextInt(6)} = r, {@code 3 + r} emerald tries of (16, 28, 16), then 7 silverfish veins of 8 at (16, 64, 16).
     * None of these draws depends on the world, so the pattern is exact.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400)
    public static void miningDecorationDrawsTheOriginalFeatureCounts(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        ServerLevel mining = W05GameTests.dimensionLevel(helper, "mining");
        ChunkPos centre = freshCentre(mining, new ChunkPos(-2400, -2400), 3);

        // Emerald ore also comes from the terrain step: ChunkProviderOreSpawn2.java:203-206 runs ChunkOreGenerator with
        // the OreSpawn Emerald_stats (OreSpawnMain default rate 4, clump 6, Y 0..40), as the original provider did. A
        // per-chunk bound or Y band for the aid.a emeralds is therefore not observable in the finished chunk; the field
        // only has to show that both passes left their blocks. The exact counts come from the recorded draws below.
        int emeralds = 0;
        int infested = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                LevelChunk chunk = mining.getChunk(centre.x + dx, centre.z + dz);
                int bx = chunk.getPos().getMinBlockX();
                int bz = chunk.getPos().getMinBlockZ();
                for (int y = 0; y < 256; y++) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            BlockState state = chunk.getBlockState(pos.set(bx + x, y, bz + z));
                            if (state.is(Blocks.EMERALD_ORE)) {
                                emeralds++;
                            } else if (state.is(Blocks.INFESTED_STONE)) {
                                infested++;
                            }
                        }
                    }
                }
            }
        }
        if (emeralds == 0) {
            failures.add("no emerald ore in the 3x3 field around " + centre + ": BiomeGenHills.decorate did not run");
        }
        if (infested == 0) {
            failures.add("no infested stone in the 3x3 field around " + centre + ": the silverfish pass did not run");
        }

        RecordingRandom rand = new RecordingRandom(mining.getSeed() ^ centre.toLong());
        BiomeGenHills.decorate(new LegacyWorld(mining, Blocks.GRASS_BLOCK.defaultBlockState()), rand,
                centre.getMinBlockX(), centre.getMinBlockZ());
        List<Draw> draws = rand.draws;

        int[][] ores = {{20, 256, 32}, {10, 256, 32}, {20, 128, 16}, {20, 64, 8}, {2, 32, 8}, {8, 16, 7}, {1, 16, 7}};
        String[] oreNames = {"dirt", "gravel", "coal", "iron", "gold", "redstone", "diamond"};
        int i = 0;
        for (int o = 0; o < ores.length && i >= 0; o++) {
            for (int n = 0; n < ores[o][0] && i >= 0; n++) {
                i = expectVein(draws, i, new int[] {16, ores[o][1], 16}, ores[o][2], oreNames[o] + " vein " + (n + 1), failures);
            }
        }
        if (i >= 0) {
            i = expectVein(draws, i, new int[] {16, 16, 16, 16}, 6, "lapis vein", failures);
        }
        int orePassEnd = i;

        int silverfishStart = draws.size() - 7 * 15;
        for (int v = 0; v < 7; v++) {
            if (expectVein(draws, silverfishStart + 15 * v, new int[] {16, 64, 16}, 8, "silverfish vein " + (v + 1), failures) < 0) {
                break;
            }
        }
        int emeraldTries = -1;
        for (int k = 3; k <= 8; k++) {
            int p = silverfishStart - 3 * k - 1;
            if (p < 0 || !isInt(draws.get(p), 6) || (int) draws.get(p).value() != k - 3) {
                continue;
            }
            boolean triples = true;
            for (int t = 0; t < k; t++) {
                int q = p + 1 + 3 * t;
                triples &= isInt(draws.get(q), 16) && isInt(draws.get(q + 1), 28) && isInt(draws.get(q + 2), 16);
            }
            if (triples) {
                emeraldTries = k;
                if (orePassEnd > p) {
                    failures.add("the emerald count draw " + p + " lies inside the ore pass ending at " + orePassEnd);
                }
            }
        }
        if (emeraldTries < 0) {
            int from = Math.max(0, silverfishStart - 30);
            failures.add("no nextInt(6) = r followed by 3 + r emerald tries (16, 28, 16) before the silverfish pass; draws "
                    + from + ".." + silverfishStart + ": " + draws.subList(from, Math.max(from, silverfishStart)));
        }
        OreSpawn.LOG.info("W06 mining counts: field {} emerald ores {}, infested stone {}; recorded {} draws, ore pass ends at {}, emerald tries {}",
                centre, emeralds, infested, draws.size(), orePassEnd, emeraldTries);
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    // ------------------------------------------------------------------ back-edges

    // ------------------------------------------------------------------ fix wave (review findings)

    /**
     * 1.7.10 {@code EntityAITempt} ({@code vk}, disassembled): {@code resetTask} sets 100 evaluations of calm-down, one
     * evaluation every third tick, so a player holding wheat tempts again 300 ticks after the reset (1.21.1
     * {@code TemptGoal}: 100). The goal is driven by hand the way {@code GoalSelector} drives it - tick counter set,
     * {@code canUse()} on every second tick. Then the off hand: 1.7.10 read only the held item.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA)
    public static void temptCalmsDownForAHundredEvaluationsOnTheLegacyCadence(GameTestHelper helper) {
        RedCow cow = helper.spawnWithNoFreeWill(ModEntities.APPLE_COW.get(), new BlockPos(6, 2, 6));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.moveTo(cow.getX() + 1.0, cow.getY(), cow.getZ(), 0.0F, 0.0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
            EntityAITempt tempt = new EntityAITempt(cow, 1.25, stack -> stack.is(Items.WHEAT), false);
            int start = 3000;
            cow.tickCount = start;
            tempt.stop();
            int readyAt = -1;
            for (int t = start; t <= start + 400; t += 2) {
                cow.tickCount = t;
                if (tempt.canUse()) {
                    readyAt = t;
                    break;
                }
            }
            helper.assertTrue(readyAt == start + 300, "the tempt goal was ready "
                    + (readyAt < 0 ? "not within 400 ticks" : (readyAt - start) + " ticks") + " after its reset, expected 300");

            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.WHEAT));
            cow.tickCount = readyAt + 2;
            tempt.canUse();
            cow.tickCount = readyAt + 4;
            helper.assertFalse(tempt.canUse(), "wheat in the off hand tempted the cow; EntityAITempt read the held item only");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
            cow.tickCount = readyAt + 6;
            helper.assertTrue(tempt.canUse(), "wheat in the main hand no longer tempted the cow after the calm-down");
        } finally {
            player.remove(Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    /**
     * Whale.java and Flounder.java have no {@code interact}: 1.7.10 {@code EntityAnimal.interact} ({@code wf.a(yz)})
     * fed only an animal of age 0. A calf keeps its Crystal Apple and its age; an adult falls in love and eats it.
     */
    private static void calfIsNotFedItsBreedingItem(GameTestHelper helper, EntityType<? extends Animal> type) {
        String name = type.toShortString();
        Animal adult = helper.spawnWithNoFreeWill(type, new BlockPos(3, 2, 6));
        Animal calf = helper.spawnWithNoFreeWill(type, new BlockPos(9, 2, 6));
        calf.setAge(-24000);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        hold(player, calf, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        InteractionResult result = calf.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().getCount() == 1, name + " calf ate the Crystal Apple");
        helper.assertTrue(calf.getAge() == -24000, name + " calf aged to " + calf.getAge() + " from a Crystal Apple");
        helper.assertFalse(result.consumesAction(), name + " calf consumed the click: " + result);
        hold(player, adult, new ItemStack(ModItems.CRYSTAL_APPLE.get()));
        adult.interact(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(adult.isInLove(), name + " adult did not fall in love with a Crystal Apple");
        helper.assertTrue(player.getMainHandItem().isEmpty(), name + " adult did not eat the Crystal Apple");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void whaleCalfIsNotFedACrystalApple(GameTestHelper helper) {
        calfIsNotFedItsBreedingItem(helper, ModEntities.WHALE.get());
    }

    @GameTest(template = ARENA)
    public static void youngFlounderIsNotFedACrystalApple(GameTestHelper helper) {
        calfIsNotFedItsBreedingItem(helper, ModEntities.FLOUNDER.get());
    }

    /**
     * Beaver.java:162-185, 216-218: the logs go with {@code setBlock(..., air, 0, 2)}, and 1.7.10 still ran
     * {@code BlockLog.breakBlock} on each ({@code alx.a}, disassembled), marking the leaves within 4 for decay. A crown
     * whose trunk the beaver fells must not keep its distance to a log that is gone: every leaf reaches distance 7
     * (or has already decayed). Without the port the leaves stay at distance 1 for ever.
     */
    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void leavesOfATreeTheBeaverFellsStartToDecay(GameTestHelper helper) {
        Beaver beaver = helper.spawnWithNoFreeWill(ModEntities.BEAVER.get(), new BlockPos(1, 2, 1));
        for (int y = 2; y <= 4; y++) {
            helper.setBlock(new BlockPos(6, y, 6), Blocks.OAK_LOG);
        }
        List<BlockPos> crown = List.of(new BlockPos(5, 4, 6), new BlockPos(7, 4, 6), new BlockPos(6, 4, 5),
                new BlockPos(6, 4, 7), new BlockPos(6, 5, 6), new BlockPos(5, 5, 6), new BlockPos(7, 5, 6));
        for (BlockPos rel : crown) {
            int distance = rel.getY() == 5 && rel.getX() != 6 ? 2 : 1;
            helper.setBlock(rel, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE, distance));
        }
        // Placing the crown schedules leaf ticks of its own for the next tick; felling in the same tick would let those
        // recompute the distances and the test would pass without the port. So the beaver fells once they are done.
        helper.runAfterDelay(10, () -> {
            BlockPos cut = helper.absolutePos(new BlockPos(6, 2, 6));
            beaver.fellAt(cut.getX(), cut.getY(), cut.getZ(), 0);
            for (int y = 2; y <= 4; y++) {
                helper.assertBlockNotPresent(Blocks.OAK_LOG, new BlockPos(6, y, 6));
            }
        });
        helper.succeedWhen(() -> {
            if (helper.getBlockState(new BlockPos(6, 4, 6)).is(Blocks.OAK_LOG)) {
                throw new GameTestAssertException("the beaver has not felled the tree yet");
            }
            for (BlockPos rel : crown) {
                BlockState leaf = helper.getBlockState(rel);
                if (leaf.is(Blocks.OAK_LEAVES) && leaf.getValue(LeavesBlock.DISTANCE) != 7) {
                    throw new GameTestAssertException("leaf at " + rel + " still has distance " + leaf.getValue(LeavesBlock.DISTANCE)
                            + " after its trunk was felled");
                }
            }
        });
    }

    /**
     * The wave is closed only when no {@code TODO} addressed to W06 is left anywhere under {@code src/} (W04GameTests
     * pattern; the marker is split so this file does not match itself).
     */
    @GameTest(template = ARENA)
    public static void noTodoForWaveSixRemainsInTheSourceTree(GameTestHelper helper) {
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
        String marker = "TODO " + "W06";
        List<String> found = new ArrayList<>();
        Path root = src;
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(f -> f.toString().matches(".*\\.(java|json|toml|mcmeta|md|txt|snbt)$")).toList()) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (int n = 0; n < lines.size(); n++) {
                    if (lines.get(n).contains(marker)) {
                        found.add(root.relativize(file) + ":" + (n + 1));
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
}
