package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.combat.LegacyCombatMath;
import com.swbr.orespawn.combat.VirtualHealth;
import com.swbr.orespawn.entity.antrobot.AntRobot;
import com.swbr.orespawn.entity.spiderrobot.SpiderRobot;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import com.swbr.orespawn.entity.dragon.Dragon;
import com.swbr.orespawn.entity.dragon.Spyro;
import com.swbr.orespawn.entity.leon.Leon;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.network.RiderControlMessage;
import com.swbr.orespawn.network.RiderControlMessageHandler;
import com.swbr.orespawn.network.RiderKeys;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * W09 integration (DECISIONS R4, R9, R17, R20) on a real server.
 *
 * <p>Every W09 type spawns through its registry factory, carries the manifest hitbox on its type and on the spawned
 * entity, the original maximum health where it is a constant, and is still alive after 100 ticks with its AI running
 * (one batch each: flyers and hunters cross the grid). Water Dragon and Rubber Ducky spawn in a walled pool - the Water
 * Dragon dries out on land by design. Wiring: every W09 egg names its type, the tags, and the robot kit round trip
 * (kit durability in original units, wrench packing the robot back) - the R4/R5 test on a real {@link VirtualHealth}
 * entity that W01 and FIX1 were waiting for.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W09GameTests {

    private static final String ARENA = "arena";
    private static final String WIRING = "w09_wiring";

    private W09GameTests() {}

    private static void clearMobsAround(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds().inflate(64.0), entity -> !(entity instanceof Player))
                .forEach(Entity::discard);
    }

    /** Glass ring at x/z 0 and 12, water in x/z 1..11, both on y 2..6. */
    private static void pool(GameTestHelper helper) {
        for (int x = 0; x <= 12; ++x) {
            for (int z = 0; z <= 12; ++z) {
                boolean wall = x == 0 || z == 0 || x == 12 || z == 12;
                for (int y = 2; y <= 6; ++y) {
                    helper.setBlock(new BlockPos(x, y, z), wall ? Blocks.GLASS : Blocks.WATER);
                }
            }
        }
    }

    /** @param maxHealth expected maximum health (attribute), or a negative value to skip the check */
    private static <T extends LivingEntity> void spawnAndTick(GameTestHelper helper, EntityType<T> type, BlockPos pos, float maxHealth,
            float width, float height) {
        T e = helper.spawn(type, pos);
        String name = type.toShortString();
        if (maxHealth >= 0.0f) {
            helper.assertTrue(e.getMaxHealth() == maxHealth, name + " max health " + e.getMaxHealth() + ", expected " + maxHealth);
        }
        helper.assertTrue(Math.abs(type.getWidth() - width) < 1.0e-4F && Math.abs(type.getHeight() - height) < 1.0e-4F,
                name + " is registered " + type.getWidth() + " x " + type.getHeight() + ", expected " + width + " x " + height);
        helper.assertTrue(Math.abs(e.getBbWidth() - width) < 1.0e-4F && Math.abs(e.getBbHeight() - height) < 1.0e-4F,
                name + " spawned with a hitbox of " + e.getBbWidth() + " x " + e.getBbHeight() + ", expected " + width + " x " + height);
        helper.runAfterDelay(100, () -> {
            boolean alive = e.isAlive();
            int ticks = e.tickCount;
            clearMobsAround(helper);
            helper.assertTrue(alive, name + " did not survive 100 ticks");
            helper.assertTrue(ticks >= 100, name + " ticked only " + ticks + " times");
            helper.succeed();
        });
    }

    private static <T extends LivingEntity> void land(GameTestHelper helper, EntityType<T> type, float maxHealth, float w, float h) {
        spawnAndTick(helper, type, new BlockPos(6, 2, 6), maxHealth, w, h);
    }

    private static <T extends LivingEntity> void water(GameTestHelper helper, EntityType<T> type, float maxHealth, float w, float h) {
        pool(helper);
        spawnAndTick(helper, type, new BlockPos(6, 3, 6), maxHealth, w, h);
    }

    // ------------------------------------------------------------------ spawn and tick

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_dragon")
    public static void dragonSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.DRAGON.get(), 200.0f, 1.5f, 1.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_baby_dragon")
    public static void babyDragonSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.BABY_DRAGON.get(), 200.0f, 0.5f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_cephadrome")
    public static void cephadromeSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.CEPHADROME.get(), 300.0f, 2.5f, 2.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_leonopteryx")
    public static void leonopteryxSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.LEONOPTERYX.get(), 250.0f, 3.5f, 8.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_wtf")
    public static void gammaMetroidSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.WTF.get(), 100.0f, 1.5f, 1.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_water_dragon")
    public static void waterDragonSpawnsAndTicks(GameTestHelper helper) {
        water(helper, ModEntities.WATER_DRAGON.get(), 150.0f, 1.25f, 1.9f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_stinky")
    public static void stinkySpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.STINKY.get(), 100.0f, 0.75f, 0.75f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_rubber_ducky")
    public static void rubberDuckySpawnsAndTicks(GameTestHelper helper) {
        water(helper, ModEntities.RUBBER_DUCKY.get(), 5.0f, 0.33f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_robot_red_ant")
    public static void robotRedAntSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.ROBOT_RED_ANT.get(), 300.0f, 2.75f, 1.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_robot_spider")
    public static void robotSpiderSpawnsAndTicks(GameTestHelper helper) {
        // R4: 1500 in original units, the attribute carries the clamped value.
        SpiderRobot robot = helper.spawn(ModEntities.ROBOT_SPIDER.get(), new BlockPos(6, 2, 6));
        double original = VirtualHealth.originalMaxHealth(robot);
        float attribute = robot.getMaxHealth();
        float originalHealth = VirtualHealth.originalHealth(robot);
        robot.discard();
        helper.assertTrue(original == 1500.0, "robot_spider original max health " + original + ", expected 1500");
        helper.assertTrue(Math.abs(attribute - LegacyCombatMath.attributeMaxHealth(1500.0)) < 1.0e-3,
                "robot_spider attribute max health " + attribute + ", expected " + LegacyCombatMath.attributeMaxHealth(1500.0));
        helper.assertTrue(Math.abs(originalHealth - 1500.0f) < 0.5f, "robot_spider spawns with " + originalHealth + " original health");
        land(helper, ModEntities.ROBOT_SPIDER.get(), -1.0f, 3.25f, 2.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w09_spider_driver")
    public static void spiderDriverSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.SPIDER_DRIVER.get(), 16.0f, 1.4f, 0.9f);
    }

    // ------------------------------------------------------------------ wiring

    private static Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> w09Eggs() {
        Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> eggs = new LinkedHashMap<>();
        eggs.put(ModItems.EGG_SPYRO, ModEntities.BABY_DRAGON);
        eggs.put(ModItems.EGG_DRAGON, ModEntities.DRAGON);
        eggs.put(ModItems.EGG_CEPHADROME, ModEntities.CEPHADROME);
        eggs.put(ModItems.EGG_LEON, ModEntities.LEONOPTERYX);
        eggs.put(ModItems.EGG_GAMMA_METROID, ModEntities.WTF);
        eggs.put(ModItems.EGG_WATER_DRAGON, ModEntities.WATER_DRAGON);
        eggs.put(ModItems.EGG_STINKY, ModEntities.STINKY);
        eggs.put(ModItems.EGG_RUBBER_DUCKY, ModEntities.RUBBER_DUCKY);
        eggs.put(ModItems.EGG_ANT_ROBOT, ModEntities.ROBOT_RED_ANT);
        eggs.put(ModItems.EGG_SPIDER_ROBOT, ModEntities.ROBOT_SPIDER);
        eggs.put(ModItems.EGG_SPIDER_DRIVER, ModEntities.SPIDER_DRIVER);
        return eggs;
    }

    @GameTest(template = ARENA, batch = WIRING)
    public static void everyW09SpawnEggNamesItsType(GameTestHelper helper) {
        Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> eggs = w09Eggs();
        eggs.forEach((egg, type) -> helper.assertTrue(egg.get().getType() == type.get(),
                egg.getId() + " spawns " + egg.get().getType().toShortString() + ", expected " + type.get().toShortString()));
        helper.succeed();
    }

    /** R20 (kits and wrench: durability and vanishing only), the breathing tag and the arthropod tag of the driver. */
    @GameTest(template = ARENA, batch = WIRING)
    public static void w09TagsAreInPlace(GameTestHelper helper) {
        for (DeferredItem<?> item : List.of(ModItems.SPIDER_ROBOT_KIT, ModItems.ANT_ROBOT_KIT, ModItems.WRENCH)) {
            ItemStack stack = new ItemStack(item.get());
            helper.assertTrue(stack.is(ItemTags.DURABILITY_ENCHANTABLE) && stack.is(ItemTags.VANISHING_ENCHANTABLE)
                    && !stack.is(ItemTags.WEAPON_ENCHANTABLE), item.getId() + " tags");
        }
        for (EntityType<?> type : List.of(ModEntities.DRAGON.get(), ModEntities.BABY_DRAGON.get(), ModEntities.WATER_DRAGON.get(),
                ModEntities.STINKY.get())) {
            helper.assertTrue(type.is(EntityTypeTags.CAN_BREATHE_UNDER_WATER), type.toShortString() + " cannot breathe under water");
        }
        helper.assertFalse(ModEntities.RUBBER_DUCKY.get().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER), "rubber_ducky has no override");
        helper.assertTrue(ModEntities.SPIDER_DRIVER.get().is(EntityTypeTags.ARTHROPOD), "spider_driver is not an arthropod");
        helper.succeed();
    }

    /**
     * ItemSpiderRobotKit.onItemUse (:24-50): a Spider Robot Kit with damage 500 unpacks a robot with 1000 of 1500 health
     * in original units (R4 - the attribute is clamped), and ItemWrench.onLeftClickEntity (:19-76) packs it back into a
     * kit whose damage is the missing health. A Red Ant kit marks its robot as owned.
     */
    @GameTest(template = ARENA, batch = "w09_robot_kits")
    public static void robotKitAndWrenchRoundTrip(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        BlockPos floor = helper.absolutePos(new BlockPos(6, 1, 6));
        AABB search = new AABB(floor).inflate(8.0);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        ItemStack kit = new ItemStack(ModItems.SPIDER_ROBOT_KIT.get());
        int maxDamage = kit.getMaxDamage();
        kit.setDamageValue(500);
        player.setItemInHand(InteractionHand.MAIN_HAND, kit);
        kit.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).add(0.0, 0.5, 0.0), Direction.UP, floor, false)));
        List<SpiderRobot> robots = helper.getLevel().getEntitiesOfClass(SpiderRobot.class, search);
        if (robots.size() != 1) {
            failures.add("spider kit spawned " + robots.size() + " robots");
        } else {
            SpiderRobot robot = robots.get(0);
            double max = VirtualHealth.originalMaxHealth(robot);
            float health = VirtualHealth.originalHealth(robot);
            if (max != maxDamage) {
                failures.add("robot original max " + max + " != kit durability " + maxDamage);
            }
            if (Math.abs(health - (maxDamage - 500)) > 0.5f) {
                failures.add("robot original health " + health + ", expected " + (maxDamage - 500));
            }
            if (!kit.isEmpty()) {
                failures.add("survival kit not used up");
            }
            ItemStack wrench = new ItemStack(ModItems.WRENCH.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
            boolean handled = wrench.getItem().onLeftClickEntity(wrench, player, robot);
            if (!handled || robot.isAlive()) {
                failures.add("wrench did not pack the robot (handled " + handled + ", alive " + robot.isAlive() + ")");
            }
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, search,
                    i -> i.getItem().is(ModItems.SPIDER_ROBOT_KIT.get()));
            if (drops.size() != 1) {
                failures.add("wrench dropped " + drops.size() + " spider kits");
            } else if (Math.abs(drops.get(0).getItem().getDamageValue() - 500) > 1) {
                failures.add("packed kit damage " + drops.get(0).getItem().getDamageValue() + ", expected 500");
            }
            if (wrench.getDamageValue() != 2) {
                failures.add("wrench damage " + wrench.getDamageValue() + ", expected 2");
            }
        }

        ItemStack antKit = new ItemStack(ModItems.ANT_ROBOT_KIT.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, antKit);
        antKit.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).add(0.0, 0.5, 0.0), Direction.UP, floor, false)));
        List<AntRobot> ants = helper.getLevel().getEntitiesOfClass(AntRobot.class, search);
        if (ants.size() != 1) {
            failures.add("ant kit spawned " + ants.size() + " robots");
        } else {
            AntRobot ant = ants.get(0);
            if (ant.getOwned() == 0) {
                failures.add("ant kit robot is not owned");
            }
            if (Math.abs(ant.getHealth() - ant.getMaxHealth()) > 0.5f) {
                failures.add("fresh ant kit robot health " + ant.getHealth() + " of " + ant.getMaxHealth());
            }
        }
        clearMobsAround(helper);
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    // ------------------------------------------------------------------ taming, riding, fly-up (R15, R17)

    /**
     * A real {@link ServerPlayer} in the level: {@code TamableAnimal.isOwnedBy} resolves the owner through
     * {@code level.getPlayerByUUID}, which never finds {@code makeMockPlayer}. The helper's player is creative, so the
     * taming food is never used up and the loop can click as often as the original's chance needs.
     */
    @SuppressWarnings("removal")
    private static ServerPlayer ownerNear(GameTestHelper helper, BlockPos relative) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(relative));
        player.moveTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    private static void release(GameTestHelper helper, ServerPlayer player) {
        player.stopRiding();
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /** The W01 path: {@link RiderControlMessageHandler} as the network layer calls it for this player. */
    private static void sendFlyUp(Player player, int keystate) {
        new RiderControlMessageHandler().handle(new RiderControlMessage(keystate), new IPayloadContext() {
            @Override
            public ICommonPacketListener listener() {
                throw new UnsupportedOperationException("no connection in a GameTest");
            }

            @Override
            public Player player() {
                return player;
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
            public void handle(CustomPacketPayload payload) {}

            @Override
            public void finishCurrentTask(ConfigurationTask.Type type) {}
        });
    }

    /** Right clicks with {@code food} until {@code tamed} holds; the originals roll 1 in 2, 3 or 5 per click. */
    private static int clickUntil(ServerPlayer player, Entity target, ItemStack food, BooleanSupplier tamed) {
        player.setItemInHand(InteractionHand.MAIN_HAND, food);
        for (int i = 1; i <= 200; ++i) {
            player.interactOn(target, InteractionHand.MAIN_HAND);
            if (tamed.getAsBoolean()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Mounted with an empty hand, then 40 ticks hovering with the key released and 40 ticks with the key held through
     * the rider payload: the mount must end clearly higher than it hovered.
     */
    private static void riseWithFlyUp(GameTestHelper helper, ServerPlayer player, Entity mount, String name) {
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.interactOn(mount, InteractionHand.MAIN_HAND);
        if (player.getVehicle() != mount) {
            Entity vehicle = player.getVehicle();
            release(helper, player);
            clearMobsAround(helper);
            helper.fail(name + ": an empty-hand click did not seat the player (vehicle " + vehicle + ")");
            return;
        }
        sendFlyUp(player, 0);
        double[] y = new double[2];
        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> y[0] = mount.getY())
                .thenIdle(40)
                .thenExecute(() -> {
                    y[1] = mount.getY();
                    sendFlyUp(player, 1);
                })
                .thenIdle(40)
                .thenExecute(() -> {
                    double released = y[1] - y[0];
                    double held = mount.getY() - y[1];
                    boolean seated = player.getVehicle() == mount;
                    boolean keyed = RiderKeys.isFlyUp(player);
                    release(helper, player);
                    clearMobsAround(helper);
                    helper.assertTrue(keyed, name + ": the payload did not set the fly-up state");
                    helper.assertTrue(seated, name + ": the rider fell off during the flight");
                    helper.assertTrue(held > released + 1.0, name + " climbed " + held + " blocks in 40 ticks with fly-up held and "
                            + released + " with it released");
                })
                .thenSucceed();
    }

    /** Dragon.interact (:1193-1240): raw beef tames with 1 in 5; the owner's empty hand mounts; fly-up climbs. */
    @GameTest(template = ARENA, timeoutTicks = 200, batch = "w09_ride_dragon")
    public static void dragonTamesWithBeefCarriesItsOwnerAndClimbs(GameTestHelper helper) {
        Dragon dragon = helper.spawn(ModEntities.DRAGON.get(), new BlockPos(6, 2, 6));
        ServerPlayer player = ownerNear(helper, new BlockPos(6, 2, 8));
        int clicks = clickUntil(player, dragon, new ItemStack(Items.BEEF, 64), dragon::isTame);
        if (clicks < 0 || !dragon.isOwnedBy(player)) {
            release(helper, player);
            clearMobsAround(helper);
            helper.fail("dragon not tamed by beef (clicks " + clicks + ", owner " + dragon.getOwnerUUID() + ")");
            return;
        }
        riseWithFlyUp(helper, player, dragon, "dragon");
    }

    /** Leon.interact (:974-1040): raw beef tames with 1 in 3 within 7 blocks; the owner's empty hand mounts; fly-up climbs. */
    @GameTest(template = ARENA, timeoutTicks = 200, batch = "w09_ride_leon")
    public static void leonTamesWithBeefCarriesItsOwnerAndClimbs(GameTestHelper helper) {
        Leon leon = helper.spawn(ModEntities.LEONOPTERYX.get(), new BlockPos(6, 2, 6));
        ServerPlayer player = ownerNear(helper, new BlockPos(6, 2, 10));
        int clicks = clickUntil(player, leon, new ItemStack(Items.BEEF, 64), leon::isTame);
        if (clicks < 0 || !leon.isOwnedBy(player)) {
            release(helper, player);
            clearMobsAround(helper);
            helper.fail("leon not tamed by beef (clicks " + clicks + ", owner " + leon.getOwnerUUID() + ")");
            return;
        }
        riseWithFlyUp(helper, player, leon, "leonopteryx");
    }

    /** Leon.interact (:980-989): a diamond block tames at once. */
    @GameTest(template = ARENA, batch = "w09_ride_leon_diamond")
    public static void leonTamesWithADiamondBlockAtOnce(GameTestHelper helper) {
        Leon leon = helper.spawn(ModEntities.LEONOPTERYX.get(), new BlockPos(6, 2, 6));
        ServerPlayer player = ownerNear(helper, new BlockPos(6, 2, 10));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_BLOCK));
        player.interactOn(leon, InteractionHand.MAIN_HAND);
        boolean owned = leon.isTame() && leon.isOwnedBy(player);
        release(helper, player);
        clearMobsAround(helper);
        helper.assertTrue(owned, "one diamond block did not tame the leonopteryx");
        helper.succeed();
    }

    /**
     * Cephadrome.interact (:888-924): the Cephadrome is never tamed - raw beef feeds it, and a fed one takes a player
     * with an empty hand up.
     */
    @GameTest(template = ARENA, timeoutTicks = 200, batch = "w09_ride_cephadrome")
    public static void cephadromeCarriesAPlayerAfterBeefAndClimbs(GameTestHelper helper) {
        Cephadrome cepha = helper.spawn(ModEntities.CEPHADROME.get(), new BlockPos(6, 2, 6));
        ServerPlayer player = ownerNear(helper, new BlockPos(6, 2, 9));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BEEF, 8));
        player.interactOn(cepha, InteractionHand.MAIN_HAND);
        riseWithFlyUp(helper, player, cepha, "cephadrome");
    }

    /**
     * WaterDragon.interact (:84-164): raw fish tames with 1 in 3. The original has no mount branch - the owner's
     * empty-hand click toggles sitting and does not seat the player.
     */
    @GameTest(template = ARENA, batch = "w09_tame_water_dragon")
    public static void waterDragonTamesWithFishAndSitsInsteadOfCarrying(GameTestHelper helper) {
        pool(helper);
        WaterDragon wd = helper.spawn(ModEntities.WATER_DRAGON.get(), new BlockPos(6, 3, 6));
        ServerPlayer player = ownerNear(helper, new BlockPos(6, 7, 8));
        int clicks = clickUntil(player, wd, new ItemStack(Items.COD, 64), wd::isTame);
        boolean owned = clicks > 0 && wd.isOwnedBy(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        boolean satBefore = wd.isSitting();
        player.interactOn(wd, InteractionHand.MAIN_HAND);
        boolean satAfter = wd.isSitting();
        Entity vehicle = player.getVehicle();
        release(helper, player);
        clearMobsAround(helper);
        helper.assertTrue(owned, "water dragon not tamed by cod (clicks " + clicks + ")");
        helper.assertTrue(vehicle == null, "water dragon seated its owner, the original has no mount branch");
        helper.assertTrue(satBefore != satAfter, "the owner's empty-hand click did not toggle sitting");
        helper.succeed();
    }

    /**
     * Spyro.interact (:207-300): raw beef tames with 1 in 2; the owner's diamond grows the Baby Dragon into a Dragon owned
     * by the same player, and Dragon.interact shrinks it back the same way.
     */
    @GameTest(template = ARENA, batch = "w09_spyro_grows")
    public static void spyroGrowsIntoADragonKeepingItsOwner(GameTestHelper helper) {
        List<String> failures = new ArrayList<>();
        Spyro spyro = helper.spawn(ModEntities.BABY_DRAGON.get(), new BlockPos(6, 2, 6));
        ServerPlayer player = ownerNear(helper, new BlockPos(6, 2, 8));
        AABB search = helper.getBounds().inflate(4.0);
        int clicks = clickUntil(player, spyro, new ItemStack(Items.BEEF, 64), spyro::isTame);
        if (clicks < 0 || !spyro.isOwnedBy(player)) {
            failures.add("baby dragon not tamed by beef (clicks " + clicks + ")");
        } else {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND, 4));
            player.interactOn(spyro, InteractionHand.MAIN_HAND);
            List<Dragon> dragons = helper.getLevel().getEntitiesOfClass(Dragon.class, search, Entity::isAlive);
            if (spyro.isAlive()) {
                failures.add("the diamond left the baby dragon in place");
            }
            if (dragons.size() != 1) {
                failures.add("the diamond made " + dragons.size() + " dragons");
            } else {
                Dragon dragon = dragons.get(0);
                if (!dragon.isTame() || !dragon.isOwnedBy(player)) {
                    failures.add("the grown dragon lost its owner (tame " + dragon.isTame() + ", owner " + dragon.getOwnerUUID() + ")");
                }
                player.interactOn(dragon, InteractionHand.MAIN_HAND);
                List<Spyro> babies = helper.getLevel().getEntitiesOfClass(Spyro.class, search, Entity::isAlive);
                if (dragon.isAlive() || babies.size() != 1) {
                    failures.add("the dragon's diamond gave " + babies.size() + " baby dragons (dragon alive " + dragon.isAlive() + ")");
                } else if (!babies.get(0).isTame() || !babies.get(0).isOwnedBy(player)) {
                    failures.add("the shrunk baby dragon lost its owner");
                }
            }
        }
        release(helper, player);
        clearMobsAround(helper);
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }
}
