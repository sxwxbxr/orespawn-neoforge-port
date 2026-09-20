package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.arthropod.Scorpion;
import com.swbr.orespawn.entity.dino.Alosaurus;
import com.swbr.orespawn.entity.ender.EnderKnight;
import com.swbr.orespawn.entity.fairy.Fairy;
import com.swbr.orespawn.entity.monster.Hammerhead;
import com.swbr.orespawn.entity.projectile.Acid;
import com.swbr.orespawn.entity.projectile.LaserBall;
import com.swbr.orespawn.entity.projectile.LegacyThrowable;
import com.swbr.orespawn.entity.robot.Robot2;
import com.swbr.orespawn.entity.robot.Robot3;
import com.swbr.orespawn.entity.robot.Robot5;
import com.swbr.orespawn.entity.worm.WormLarge;
import com.swbr.orespawn.entity.worm.WormMedium;
import com.swbr.orespawn.entity.worm.WormSmall;
import com.swbr.orespawn.item.spawnegg.ItemSpawnEgg;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModEntities;
import com.swbr.orespawn.registry.ModItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * W07 integration (DECISIONS R9, R10, R17) on a real server.
 *
 * <p>Every living W07 type spawns through its registry factory, has the original maximum health (manifest
 * {@code max_health}) and the manifest hitbox, and is still alive after 100 ticks with its AI running. <b>Each of these
 * tests is its own batch.</b> The GameTest server lays a batch out on a grid with a few blocks between the arenas, and
 * the W07 mobs hunt in boxes of 8 to 30 blocks (the Robo-Sniper shoots laser balls across 30): in one shared batch a
 * T. Rex would eat the neighbouring Cryolophosaurus, and the test would fail on original behaviour rather than on a
 * port error. Floor at helper y = 1, ground mobs at y = 2, flyers (Bee, Fairy) at y = 3; Jeffery (9.75 high) in
 * {@code arena_large}.
 *
 * <p>Wiring half: the back-edges the integrator closed - robots vanish plain laser balls (LaserBall.java:113-134), acid
 * does not hurt the bugs that spit it (:103-112), breaking Crystalized Fairies releases 1..6 fairies
 * (OreBasicStone.java:22-44), every W07 spawn egg names its type, and the Fairy Sword sits in the six {@code ItemSword}
 * enchantable tags (R20).
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W07GameTests {

    private static final String ARENA = "arena";
    private static final String ARENA_LARGE = "arena_large";
    /**
     * The wiring tests below fly laser balls and acid and release fairies; they run in a batch of their own so nothing
     * they launch can reach a test of the default batch.
     */
    private static final String WIRING = "w07_wiring";

    private W07GameTests() {}

    private static void spawnAndTick(GameTestHelper helper, EntityType<? extends LivingEntity> type, BlockPos pos, float maxHealth,
            float width, float height) {
        LivingEntity e = helper.spawn(type, pos);
        String name = type.toShortString();
        helper.assertTrue(e.getMaxHealth() == maxHealth, name + " max health " + e.getMaxHealth() + ", expected " + maxHealth);
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

    /**
     * Removes every non-player entity within 48 blocks of the arena - before the assertions, so a failing test cleans up
     * too. A GameTestHelper spawn is persistent, and a W07 mob with AI (or the Large Worm's brood of 40) would otherwise
     * stay in the world while later batches run on the same grid. Hygiene, not a proven fix: the default-batch failure
     * seen during the W07 integration had another cause (W02GameTests.rtpBlockRelocatesAServerPlayer, see there). Safe
     * only because each spawn-and-tick test is a batch of its own; 48 covers the Ender Knight's +-32 teleport.
     */
    private static void clearMobsAround(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds().inflate(48.0), entity -> !(entity instanceof Player))
                .forEach(Entity::discard);
    }

    private static void spawnAndTick(GameTestHelper helper, EntityType<? extends LivingEntity> type, int y, float maxHealth,
            float width, float height) {
        spawnAndTick(helper, type, new BlockPos(6, y, 6), maxHealth, width, height);
    }

    // ------------------------------------------------------------------ dinosaurs and basilisk (w07-dinos)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_alosaurus")
    public static void alosaurusSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ALOSAURUS.get(), 2, 110.0f, 1.9f, 3.6f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_cryolophosaurus")
    public static void cryolophosaurusSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CRYOLOPHOSAURUS.get(), 2, 10.0f, 0.75f, 0.75f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_basilisk")
    public static void basiliskSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.BASILISK.get(), 2, 200.0f, 1.6f, 3.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_t_rex")
    public static void tRexSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.T_REX.get(), 2, 160.0f, 2.0f, 4.2f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_nastysaurus")
    public static void nastysaurusSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.NASTYSAURUS.get(), 2, 200.0f, 2.2f, 4.6f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_pointysaurus")
    public static void pointysaurusSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.POINTYSAURUS.get(), 2, 80.0f, 2.9f, 2.9f);
    }

    // ------------------------------------------------------------------ arthropods (w07-arthropods)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_scorpion")
    public static void scorpionSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.SCORPION.get(), 2, 15.0f, 0.85f, 0.55f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_emperor_scorpion")
    public static void emperorScorpionSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.EMPEROR_SCORPION.get(), 2, 350.0f, 3.5f, 3.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_cave_fisher")
    public static void caveFisherSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CAVE_FISHER.get(), 2, 10.0f, 1.35f, 0.75f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_bee")
    public static void beeSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.BEE.get(), 3, 80.0f, 1.5f, 2.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_hercules_beetle")
    public static void herculesBeetleSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.HERCULES_BEETLE.get(), 2, 250.0f, 3.25f, 2.75f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_spit_bug")
    public static void spitBugSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.SPIT_BUG.get(), 2, 100.0f, 2.0f, 2.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_jumpy_bug")
    public static void jumpyBugSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.JUMPY_BUG.get(), 2, 200.0f, 3.0f, 3.5f);
    }

    // ------------------------------------------------------------------ monsters (w07-monsters)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_kyuubi")
    public static void kyuubiSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.KYUUBI.get(), 2, 125.0f, 0.5f, 1.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_alien")
    public static void alienSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ALIEN.get(), 2, 100.0f, 1.1f, 3.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_leaf_monster")
    public static void leafMonsterSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.LEAF_MONSTER.get(), 2, 6.0f, 1.0f, 2.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_molenoid")
    public static void molenoidSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.MOLENOID.get(), 2, 200.0f, 3.9f, 2.6f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_hammerhead")
    public static void hammerheadSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.HAMMERHEAD.get(), 2, 240.0f, 3.0f, 5.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_criminal")
    public static void criminalSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.CRIMINAL.get(), 2, 100.0f, 0.75f, 1.75f);
    }

    // ------------------------------------------------------------------ ender mobs and fairy (w07-ender-fairy)

    // Night batches. EnderKnight/EnderReaper.aiStep (EnderKnight.java:184-192) teleport x/z +-32 in daylight under open
    // sky, like 1.7.10 EntityEnderman - original behaviour. The GameTest level stands at day time 0 (skyDarken < 4), so
    // the first run lost both into chunks outside the test's ticking area ("ticked only 46 / 30 times"). Their spawn
    // rule is night-only anyway (checkEnderKnightSpawnRules: !isDaytime).

    private static final String ENDER_KNIGHT_NIGHT = "w07_ender_knight_night";
    private static final String ENDER_REAPER_NIGHT = "w07_ender_reaper_night";
    private static long savedDayTime;

    @BeforeBatch(batch = ENDER_KNIGHT_NIGHT)
    public static void startEnderKnightNight(ServerLevel level) {
        savedDayTime = level.getDayTime();
        level.setDayTime(18000L);
    }

    @AfterBatch(batch = ENDER_KNIGHT_NIGHT)
    public static void endEnderKnightNight(ServerLevel level) {
        level.setDayTime(savedDayTime);
    }

    @BeforeBatch(batch = ENDER_REAPER_NIGHT)
    public static void startEnderReaperNight(ServerLevel level) {
        savedDayTime = level.getDayTime();
        level.setDayTime(18000L);
    }

    @AfterBatch(batch = ENDER_REAPER_NIGHT)
    public static void endEnderReaperNight(ServerLevel level) {
        level.setDayTime(savedDayTime);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = ENDER_KNIGHT_NIGHT)
    public static void enderKnightSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ENDER_KNIGHT.get(), 2, 60.0f, 0.6f, 2.9f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = ENDER_REAPER_NIGHT)
    public static void enderReaperSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ENDER_REAPER.get(), 2, 90.0f, 0.7f, 2.9f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_fairy")
    public static void fairySpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.FAIRY.get(), 3, 40.0f, 0.4f, 0.8f);
    }

    // ------------------------------------------------------------------ robots (w07-robots)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_bomb_omb")
    public static void bombOmbSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.BOMB_OMB.get(), 2, 5.0f, 0.5f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_robo_pounder")
    public static void roboPounderSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ROBO_POUNDER.get(), 2, 200.0f, 3.0f, 6.2f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_robo_gunner")
    public static void roboGunnerSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ROBO_GUNNER.get(), 2, 80.0f, 2.5f, 5.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_robo_warrior")
    public static void roboWarriorSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ROBO_WARRIOR.get(), 2, 170.0f, 2.5f, 4.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_robo_sniper")
    public static void roboSniperSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.ROBO_SNIPER.get(), 2, 20.0f, 1.0f, 2.25f);
    }

    @GameTest(template = ARENA_LARGE, timeoutTicks = 140, batch = "w07_jeffery")
    public static void jefferySpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.JEFFERY.get(), new BlockPos(24, 2, 36), 550.0f, 3.0f, 9.75f);
    }

    // ------------------------------------------------------------------ worms (w07-worms)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_small_worm")
    public static void smallWormSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.SMALL_WORM.get(), 2, 10.0f, 0.25f, 1.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_medium_worm")
    public static void mediumWormSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.MEDIUM_WORM.get(), 2, 30.0f, 0.5f, 2.0f);
    }

    /** WormLarge.java: the first tick of a Large Worm not from a spawner hatches 20 Small and 20 Medium Worms. */
    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w07_large_worm")
    public static void largeWormSpawnsAndTicks(GameTestHelper helper) {
        spawnAndTick(helper, ModEntities.LARGE_WORM.get(), 2, 90.0f, 1.55f, 2.5f);
    }

    // ------------------------------------------------------------------ back-edges closed by the integrator

    /** Launch point five blocks west of the target column, 1.3 above the floor top (W04GameTests.launchPoint). */
    private static Vec3 launchPoint(GameTestHelper helper) {
        return helper.absoluteVec(new Vec3(4.5, 3.3, 6.5));
    }

    /** Straight east at speed 1 without spread. */
    private static <T extends LegacyThrowable> T launchEast(GameTestHelper helper, T projectile) {
        projectile.setThrowableHeading(1.0, 0.0, 0.0, 1.0F, 0.0F);
        helper.getLevel().addFreshEntity(projectile);
        return projectile;
    }

    /**
     * The projectile is gone, and it ended at the target rather than flying past it into the floor: without the second
     * check a miss would look exactly like an immunity.
     */
    private static void assertStoppedAt(GameTestHelper helper, LegacyThrowable projectile, Mob target, String what) {
        if (projectile.isAlive()) {
            throw new GameTestAssertException(what + " is still flying");
        }
        double x = helper.relativeVec(projectile.position()).x;
        double maxX = helper.relativeVec(new Vec3(target.getBoundingBox().maxX, 0.0, 0.0)).x;
        helper.assertTrue(x <= maxX + 1.5, what + " ended at relative x " + x + ", past " + target.getType().toShortString()
                + " (max x " + maxX + ") - it missed");
    }

    private static void assertNotHurt(GameTestHelper helper, Mob target, String what) {
        helper.assertTrue(target.getHealth() == target.getMaxHealth(),
                what + " hurt " + target.getType().toShortString() + ": health " + target.getHealth() + " of " + target.getMaxHealth());
    }

    private static void assertHurt(GameTestHelper helper, Mob target, String what) {
        if (target.getHealth() >= target.getMaxHealth()) {
            throw new GameTestAssertException(what + " has not hurt " + target.getType().toShortString() + " yet");
        }
    }

    /**
     * LaserBall.java:113-134: a plain laser ball vanishes on Robot2..Robot5 and Jeffery without damage. Acid skips that
     * branch (the immunity is guarded by {@code is_iceball == 0 && is_acid == 0}), so the acid shot afterwards hurts
     * the same robot - the control that the ball reached a living, hurtable target.
     */
    private static void robotIgnoresLaserBallButNotAcid(GameTestHelper helper, EntityType<? extends Mob> type) {
        Mob robot = helper.spawnWithNoFreeWill(type, new BlockPos(9, 2, 6));
        Vec3 start = launchPoint(helper);
        LaserBall ball = launchEast(helper, new LaserBall(helper.getLevel(), start.x, start.y, start.z));
        helper.startSequence()
                .thenWaitUntil(() -> assertStoppedAt(helper, ball, robot, "laser_ball"))
                .thenExecute(() -> {
                    assertNotHurt(helper, robot, "laser_ball");
                    launchEast(helper, new Acid(helper.getLevel(), start.x, start.y, start.z));
                })
                .thenWaitUntil(() -> assertHurt(helper, robot, "acid"))
                .thenExecute(robot::discard)
                .thenSucceed();
    }

    @GameTest(template = ARENA, timeoutTicks = 100, batch = WIRING)
    public static void roboPounderIgnoresLaserBallButNotAcid(GameTestHelper helper) {
        robotIgnoresLaserBallButNotAcid(helper, ModEntities.ROBO_POUNDER.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 100, batch = WIRING)
    public static void roboGunnerIgnoresLaserBallButNotAcid(GameTestHelper helper) {
        robotIgnoresLaserBallButNotAcid(helper, ModEntities.ROBO_GUNNER.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 100, batch = WIRING)
    public static void roboWarriorIgnoresLaserBallButNotAcid(GameTestHelper helper) {
        robotIgnoresLaserBallButNotAcid(helper, ModEntities.ROBO_WARRIOR.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 100, batch = WIRING)
    public static void roboSniperIgnoresLaserBallButNotAcid(GameTestHelper helper) {
        robotIgnoresLaserBallButNotAcid(helper, ModEntities.ROBO_SNIPER.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 100, batch = WIRING)
    public static void jefferyIgnoresLaserBallButNotAcid(GameTestHelper helper) {
        robotIgnoresLaserBallButNotAcid(helper, ModEntities.JEFFERY.get());
    }

    /**
     * LaserBall.java:103-112: acid vanishes on Jumpy Bugs and Spit Bugs without damage; a plain laser ball afterwards
     * hurts the same bug.
     */
    private static void bugIgnoresAcidButNotLaserBall(GameTestHelper helper, EntityType<? extends Mob> type) {
        Mob bug = helper.spawnWithNoFreeWill(type, new BlockPos(9, 2, 6));
        Vec3 start = launchPoint(helper);
        Acid acid = launchEast(helper, new Acid(helper.getLevel(), start.x, start.y, start.z));
        helper.startSequence()
                .thenWaitUntil(() -> assertStoppedAt(helper, acid, bug, "acid"))
                .thenExecute(() -> {
                    assertNotHurt(helper, bug, "acid");
                    launchEast(helper, new LaserBall(helper.getLevel(), start.x, start.y, start.z));
                })
                .thenWaitUntil(() -> assertHurt(helper, bug, "laser_ball"))
                .thenExecute(bug::discard)
                .thenSucceed();
    }

    @GameTest(template = ARENA, timeoutTicks = 100, batch = WIRING)
    public static void spitBugIgnoresAcidButNotLaserBall(GameTestHelper helper) {
        bugIgnoresAcidButNotLaserBall(helper, ModEntities.SPIT_BUG.get());
    }

    @GameTest(template = ARENA, timeoutTicks = 100, batch = WIRING)
    public static void jumpyBugIgnoresAcidButNotLaserBall(GameTestHelper helper) {
        bugIgnoresAcidButNotLaserBall(helper, ModEntities.JUMPY_BUG.get());
    }

    /** OreBasicStone.java:22-44 with ModBlocks.CRYSTALFAIRY: a player breaking the block releases 1 + nextInt(6) fairies. */
    @GameTest(template = ARENA, batch = WIRING)
    public static void breakingCrystalizedFairiesReleasesOneToSixFairies(GameTestHelper helper) {
        BlockPos rel = new BlockPos(6, 2, 6);
        helper.setBlock(rel, ModBlocks.CRYSTALFAIRY.get());
        BlockPos abs = helper.absolutePos(rel);
        BlockState state = helper.getLevel().getBlockState(abs);
        helper.assertTrue(state.is(ModBlocks.CRYSTALFAIRY.get()), "precondition: crystalfairy placed");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(Fairy.class, helper.getBounds()).isEmpty(), "precondition: no fairy");
        state.onDestroyedByPlayer(helper.getLevel(), abs, helper.makeMockPlayer(GameType.SURVIVAL), false,
                helper.getLevel().getFluidState(abs));
        helper.assertBlockPresent(Blocks.AIR, rel);
        List<Fairy> fairies = helper.getLevel().getEntitiesOfClass(Fairy.class, helper.getBounds());
        int released = fairies.size();
        fairies.forEach(Entity::discard); // no flyers with AI left behind for the next batch (see clearMobsAround)
        helper.assertTrue(released >= 1 && released <= 6, "breaking crystalfairy released " + released + " fairies, expected 1..6");
        helper.succeed();
    }

    /** Each W07 spawn egg (manifest kind spawn_egg) names its entity type; the Basilisk egg (eggbasilisc) was added in fix1 and is checked in Fix1GameTests. */
    @GameTest(template = ARENA, batch = WIRING)
    public static void everyW07SpawnEggNamesItsType(GameTestHelper helper) {
        Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> eggs = w07Eggs();
        helper.assertTrue(eggs.size() == 30, "expected 30 W07 eggs, listed " + eggs.size());
        eggs.forEach((egg, type) -> helper.assertTrue(egg.get().getType() == type.get(),
                egg.getId() + " spawns " + egg.get().getType().toShortString() + ", expected " + type.get().toShortString()));
        helper.succeed();
    }

    /** The 30 W07 eggs the W07 wave registered and the type each must spawn; eggbasilisc is covered by Fix1GameTests. */
    private static Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> w07Eggs() {
        Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> eggs = new LinkedHashMap<>();
        eggs.put(ModItems.EGG_ALOSAURUS, ModEntities.ALOSAURUS);
        eggs.put(ModItems.EGG_CRYOLOPHOSAURUS, ModEntities.CRYOLOPHOSAURUS);
        eggs.put(ModItems.EGG_TREX, ModEntities.T_REX);
        eggs.put(ModItems.EGG_NASTYSAURUS, ModEntities.NASTYSAURUS);
        eggs.put(ModItems.EGG_POINTYSAURUS, ModEntities.POINTYSAURUS);
        eggs.put(ModItems.EGG_SCORPION, ModEntities.SCORPION);
        eggs.put(ModItems.EGG_EMPEROR_SCORPION, ModEntities.EMPEROR_SCORPION);
        eggs.put(ModItems.EGG_CAVE_FISHER, ModEntities.CAVE_FISHER);
        eggs.put(ModItems.EGG_BEE, ModEntities.BEE);
        eggs.put(ModItems.EGG_HERCULES_BEETLE, ModEntities.HERCULES_BEETLE);
        eggs.put(ModItems.EGG_SPIT_BUG, ModEntities.SPIT_BUG);
        eggs.put(ModItems.EGG_TROOPER_BUG, ModEntities.JUMPY_BUG);
        eggs.put(ModItems.EGG_KYUUBI, ModEntities.KYUUBI);
        eggs.put(ModItems.EGG_ALIEN, ModEntities.ALIEN);
        eggs.put(ModItems.EGG_LEAF_MONSTER, ModEntities.LEAF_MONSTER);
        eggs.put(ModItems.EGG_MOLENOID, ModEntities.MOLENOID);
        eggs.put(ModItems.EGG_HAMMERHEAD, ModEntities.HAMMERHEAD);
        eggs.put(ModItems.EGG_CRIMINAL, ModEntities.CRIMINAL);
        eggs.put(ModItems.EGG_ENDER_KNIGHT, ModEntities.ENDER_KNIGHT);
        eggs.put(ModItems.EGG_ENDER_REAPER, ModEntities.ENDER_REAPER);
        eggs.put(ModItems.EGG_FAIRY, ModEntities.FAIRY);
        eggs.put(ModItems.EGG_ROBOT1, ModEntities.BOMB_OMB);
        eggs.put(ModItems.EGG_ROBOT2, ModEntities.ROBO_POUNDER);
        eggs.put(ModItems.EGG_ROBOT3, ModEntities.ROBO_GUNNER);
        eggs.put(ModItems.EGG_ROBOT4, ModEntities.ROBO_WARRIOR);
        eggs.put(ModItems.EGG_ROBOT5, ModEntities.ROBO_SNIPER);
        eggs.put(ModItems.EGG_JEFFERY, ModEntities.JEFFERY);
        eggs.put(ModItems.EGG_SMALL_WORM, ModEntities.SMALL_WORM);
        eggs.put(ModItems.EGG_MEDIUM_WORM, ModEntities.MEDIUM_WORM);
        eggs.put(ModItems.EGG_LARGE_WORM, ModEntities.LARGE_WORM);
        return eggs;
    }

    // ------------------------------------------------------------------ blows and knockback (one melee mob per worker)

    /**
     * One {@code LivingDamageEvent.Post} on a watched victim: who dealt it, how much landed, and both positions at that
     * moment - the event fires inside {@code actuallyHurt}, before {@code LivingEntity.hurt} and the attacker apply any
     * knockback, so the positions give the knockback direction.
     */
    private record Blow(DamageSource source, float amount, Vec3 attackerPos, Vec3 victimPos) {
        @Override
        public String toString() {
            return this.source.getMsgId() + " " + this.amount + " from " + this.source.getEntity();
        }
    }

    /** A laser ball as it joined the level, with its shooter's position at that moment. */
    private record Shot(Vec3 start, Vec3 motion, Vec3 shooterPos) {}

    private static final Map<UUID, List<Blow>> BLOWS = new ConcurrentHashMap<>();
    /** Victims hit for the first time whose next tick has not started yet. */
    private static final Set<UUID> AWAITING_KNOCKBACK = ConcurrentHashMap.newKeySet();
    /** The victim's motion at the start of its first tick after the first blow: knockback before any friction. */
    private static final Map<UUID, Vec3> KNOCKBACK = new ConcurrentHashMap<>();
    private static final Map<Mob, List<Shot>> SHOOTERS = new ConcurrentHashMap<>();
    private static final AtomicBoolean LISTENING = new AtomicBoolean();

    private static void listen() {
        if (LISTENING.compareAndSet(false, true)) {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDamageEvent.Post.class, W07GameTests::onDamagePost);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, EntityTickEvent.Pre.class, W07GameTests::onEntityTickPre);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, EntityJoinLevelEvent.class, W07GameTests::onEntityJoin);
        }
    }

    private static List<Blow> watchBlows(LivingEntity victim) {
        listen();
        List<Blow> blows = new CopyOnWriteArrayList<>();
        BLOWS.put(victim.getUUID(), blows);
        return blows;
    }

    private static void forget(Entity victim) {
        BLOWS.remove(victim.getUUID());
        AWAITING_KNOCKBACK.remove(victim.getUUID());
        KNOCKBACK.remove(victim.getUUID());
    }

    private static void onDamagePost(LivingDamageEvent.Post event) {
        List<Blow> blows = BLOWS.get(event.getEntity().getUUID());
        if (blows == null) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        blows.add(new Blow(event.getSource(), event.getNewDamage(), attacker == null ? Vec3.ZERO : attacker.position(),
                event.getEntity().position()));
        if (blows.size() == 1) {
            AWAITING_KNOCKBACK.add(event.getEntity().getUUID());
        }
    }

    private static void onEntityTickPre(EntityTickEvent.Pre event) {
        UUID id = event.getEntity().getUUID();
        if (AWAITING_KNOCKBACK.remove(id)) {
            KNOCKBACK.put(id, event.getEntity().getDeltaMovement());
        }
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        // Exact class: Acid, IceBall and the other subclasses are not what a robot fires.
        if (event.getEntity().getClass() != LaserBall.class || SHOOTERS.isEmpty()) {
            return;
        }
        Entity ball = event.getEntity();
        SHOOTERS.forEach((shooter, shots) -> {
            if (shooter.level() == event.getLevel() && shooter.distanceToSqr(ball) < 16.0) {
                shots.add(new Shot(ball.position(), ball.getDeltaMovement(), shooter.position()));
            }
        });
    }

    /** {@code Player.hurt}: mob damage scales with the difficulty (1.7.10 {@code EntityPlayer.attackEntityFrom} alike). */
    private static float scaledForPlayer(float amount, Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL -> 0.0F;
            case EASY -> Math.min(amount / 2.0F + 1.0F, amount);
            case NORMAL -> amount;
            case HARD -> amount * 3.0F / 2.0F;
        };
    }

    /** A punching bag that stands still, is no OreSpawn entity (R4 and R5 stay out) and flies with every knockback. */
    private static IronGolem dummy(GameTestHelper helper, BlockPos pos) {
        IronGolem golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, pos);
        golem.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        return golem;
    }

    /** Turns a freshly spawned mob to face +x (yaw -90), so a heading test does not first wait for a slow turn. */
    private static void faceEast(Mob mob) {
        mob.setYRot(-90.0F);
        mob.yRotO = -90.0F;
        mob.setYHeadRot(-90.0F);
        mob.setYBodyRot(-90.0F);
    }

    /**
     * Waits for the attacker's AI to strike {@code victim}, then checks the first landed blow: a {@code mob} damage
     * source from {@code attacker}, exactly {@code damage}, and a knockback of at least {@code minKnockback} blocks per
     * tick horizontally away from the attacker (vanilla 0.4 of {@code LivingEntity.hurt} plus the class's own push).
     * The spawn order matters only for the knockback read, and not even there: the victim's motion is read at the start
     * of its next tick, before its own travel applies friction.
     */
    private static void strikesWithDamageAndKnockback(GameTestHelper helper, Mob attacker, LivingEntity victim, List<Blow> blows,
            float damage, double minKnockback, Runnable cleanup) {
        String name = attacker.getType().toShortString();
        helper.startSequence()
                .thenWaitUntil(() -> {
                    if (blows.isEmpty()) {
                        throw new GameTestAssertException(name + " has not struck its target yet");
                    }
                    if (!KNOCKBACK.containsKey(victim.getUUID()) && !(victim instanceof Player)) {
                        throw new GameTestAssertException("the target of " + name + " has not ticked since the blow");
                    }
                })
                .thenExecute(() -> {
                    Blow first = blows.get(0);
                    Vec3 motion = KNOCKBACK.getOrDefault(victim.getUUID(), victim.getDeltaMovement());
                    cleanup.run();
                    helper.assertTrue(first.source().is(DamageTypes.MOB_ATTACK) && first.source().getEntity() == attacker,
                            name + ": first blow was " + first + ", expected a mob attack from " + name + " (all: " + blows + ")");
                    helper.assertTrue(Math.abs(first.amount() - damage) < 1.0e-3F,
                            name + " dealt " + first.amount() + ", expected its original " + damage + " (all: " + blows + ")");
                    Vec3 away = new Vec3(first.victimPos().x - first.attackerPos().x, 0.0, first.victimPos().z - first.attackerPos().z)
                            .normalize();
                    double along = motion.x * away.x + motion.z * away.z;
                    helper.assertTrue(along >= minKnockback, name + " knocked its target back at " + along
                            + " blocks a tick (motion " + motion + "), expected at least " + minKnockback);
                })
                .thenSucceed();
    }

    /** Removes the watchers and every non-player entity around the arena (see {@link #clearMobsAround}). */
    private static Runnable cleanupAfterBlow(GameTestHelper helper, LivingEntity victim) {
        return () -> {
            forget(victim);
            clearMobsAround(helper);
        };
    }

    /**
     * w07-dinos. Alosaurus.java:122-161: the bite reaches {@code 4 + width / 2} and deals the attack attribute
     * ({@code Alosaurus_stats} 18); {@code attackEntityAsMob} then pushes the victim 1.2 away (DinoSupport.legacyKnockback).
     */
    @GameTest(template = ARENA, timeoutTicks = 400, batch = "w07_melee_alosaurus")
    public static void alosaurusBitesForEighteenWithItsPush(GameTestHelper helper) {
        IronGolem golem = dummy(helper, new BlockPos(8, 2, 6));
        List<Blow> blows = watchBlows(golem);
        Alosaurus alosaurus = helper.spawn(ModEntities.ALOSAURUS.get(), new BlockPos(4, 2, 6));
        strikesWithDamageAndKnockback(helper, alosaurus, golem, blows, 18.0F, 0.9 * (0.4 + 1.2), cleanupAfterBlow(helper, golem));
    }

    /**
     * w07-arthropods. Scorpion.java:146-171, 181-232: an iron golem is a suitable target (no monster, no player); the
     * sting within squared distance 9 is the plain {@code EntityMob} hit, {@code Scorpion_stats} 4, vanilla knockback only.
     */
    @GameTest(template = ARENA, timeoutTicks = 400, batch = "w07_melee_scorpion")
    public static void scorpionStingsForFourWithKnockback(GameTestHelper helper) {
        IronGolem golem = dummy(helper, new BlockPos(8, 2, 6));
        List<Blow> blows = watchBlows(golem);
        Scorpion scorpion = helper.spawn(ModEntities.SCORPION.get(), new BlockPos(6, 2, 6));
        strikesWithDamageAndKnockback(helper, scorpion, golem, blows, 4.0F, 0.9 * 0.4, cleanupAfterBlow(helper, golem));
    }

    /**
     * w07-monsters. Hammerhead.java:140-154, 168-234: a Hammerhead hunts monsters, players and {@code isAttackableNonMob},
     * so the dummy is a ravager (a monster without armor). {@code Hammerhead_stats} 75, then knockback 1.1 / 0.85.
     */
    @GameTest(template = ARENA, timeoutTicks = 400, batch = "w07_melee_hammerhead")
    public static void hammerheadRamsForSeventyFiveWithItsPush(GameTestHelper helper) {
        Ravager ravager = helper.spawnWithNoFreeWill(EntityType.RAVAGER, new BlockPos(9, 2, 6));
        ravager.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        List<Blow> blows = watchBlows(ravager);
        Hammerhead hammerhead = helper.spawn(ModEntities.HAMMERHEAD.get(), new BlockPos(4, 2, 6));
        strikesWithDamageAndKnockback(helper, hammerhead, ravager, blows, 75.0F, 0.9 * (0.4 + 1.1), cleanupAfterBlow(helper, ravager));
    }

    /**
     * w07-robots. Robot2.java:269-335: facing the target ({@code rdd < 1.25}) within {@code 5 + width / 2}, the Robo-Pounder
     * hits with the plain {@code EntityMob} blow, {@code Robot2_stats} 22, vanilla knockback.
     */
    @GameTest(template = ARENA, timeoutTicks = 400, batch = "w07_melee_robo_pounder")
    public static void roboPounderPoundsForTwentyTwoWithKnockback(GameTestHelper helper) {
        IronGolem golem = dummy(helper, new BlockPos(9, 2, 6));
        List<Blow> blows = watchBlows(golem);
        Robot2 robot = helper.spawn(ModEntities.ROBO_POUNDER.get(), new BlockPos(5, 2, 6));
        faceEast(robot);
        strikesWithDamageAndKnockback(helper, robot, golem, blows, 22.0F, 0.9 * 0.4, cleanupAfterBlow(helper, golem));
    }

    /**
     * w07-ender-fairy. LegacyEntityMob (1.7.10 {@code EntityMob} old AI): a blow from any entity makes it
     * {@code entityToAttack}; closer than 2 with overlapping boxes the knight strikes ({@code attackEntity}),
     * {@code EnderKnight_stats} 12, vanilla knockback. Night batch: in daylight under open sky it teleports away.
     */
    @GameTest(template = ARENA, timeoutTicks = 400, batch = ENDER_KNIGHT_MELEE_NIGHT)
    public static void enderKnightStrikesItsAttackerForTwelveWithKnockback(GameTestHelper helper) {
        IronGolem golem = dummy(helper, new BlockPos(7, 2, 6));
        List<Blow> blows = watchBlows(golem);
        EnderKnight knight = helper.spawn(ModEntities.ENDER_KNIGHT.get(), new BlockPos(6, 2, 6));
        helper.assertTrue(knight.hurt(helper.getLevel().damageSources().mobAttack(golem), 1.0F), "precondition: the golem's blow hurt the knight");
        strikesWithDamageAndKnockback(helper, knight, golem, blows, 12.0F, 0.9 * 0.4, cleanupAfterBlow(helper, golem));
    }

    private static final String ENDER_KNIGHT_MELEE_NIGHT = "w07_melee_ender_knight_night";
    private static long savedMeleeDayTime;

    @BeforeBatch(batch = ENDER_KNIGHT_MELEE_NIGHT)
    public static void startEnderKnightMeleeNight(ServerLevel level) {
        savedMeleeDayTime = level.getDayTime();
        level.setDayTime(18000L);
    }

    @AfterBatch(batch = ENDER_KNIGHT_MELEE_NIGHT)
    public static void endEnderKnightMeleeNight(ServerLevel level) {
        level.setDayTime(savedMeleeDayTime);
    }

    /**
     * w07-worms. WormSmall.java:85-205: a non-creative player within 8 starts the dig cycle; while up, a player within
     * {@code expand(1.5, 4, 1.5)} is bitten one tick in 15 with the plain {@code EntityMob} blow, {@code WormSmall_stats} 3
     * (scaled by the difficulty, the player rule), vanilla knockback. The mock server player is a survival player: the
     * GameTest level defaults to creative, and the worm skips {@code instabuild} players. Its motion is not ticked away
     * (a mock player has no client moving it), so the knockback stays readable.
     */
    @SuppressWarnings("removal")
    @GameTest(template = ARENA, timeoutTicks = 1200, batch = "w07_melee_small_worm")
    public static void smallWormBitesAPlayerForThreeWithKnockback(GameTestHelper helper) {
        WormSmall worm = helper.spawn(ModEntities.SMALL_WORM.get(), new BlockPos(6, 2, 6));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(worm.getX() + 1.0, worm.getY(), worm.getZ(), 90.0F, 0.0F);
        helper.assertTrue(!player.getAbilities().instabuild && !player.getAbilities().invulnerable, "precondition: survival player");
        List<Blow> blows = watchBlows(player);
        float damage = scaledForPlayer(3.0F, helper.getLevel().getDifficulty());
        strikesWithDamageAndKnockback(helper, worm, player, blows, damage, 0.9 * 0.4, () -> {
            forget(player);
            player.remove(Entity.RemovalReason.DISCARDED);
            clearMobsAround(helper);
        });
    }

    // ------------------------------------------------------------------ robots that shoot

    /**
     * Robot3.java:208-265 / Robot5.java:180-239: once the head points at a suitable target ({@code rdd < 0.5}) the robot
     * fires a plain {@link LaserBall} from {@code xzoff} ahead of it at {@code yoff}, aimed at the target with an arc of
     * {@code 0.2} per horizontal block, speed 1.4 and spread 5. The shot is captured when it joins the level; then a
     * laser ball must hit the golem for its 16 (LaserBall.java:95-177).
     */
    private static void firesLaserBallsAt(GameTestHelper helper, Mob robot, IronGolem golem, double xzoff, double yoff) {
        String name = robot.getType().toShortString();
        List<Shot> shots = new CopyOnWriteArrayList<>();
        List<Blow> blows = watchBlows(golem);
        SHOOTERS.put(robot, shots);
        Runnable cleanup = () -> {
            SHOOTERS.remove(robot);
            forget(golem);
            clearMobsAround(helper);
        };
        helper.startSequence()
                .thenWaitUntil(() -> {
                    if (shots.isEmpty()) {
                        throw new GameTestAssertException(name + " has not fired a laser ball yet");
                    }
                })
                .thenExecute(() -> {
                    Shot shot = shots.get(0);
                    Vec3 offset = shot.start().subtract(shot.shooterPos());
                    double horizontal = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
                    Vec3 toGolem = new Vec3(golem.getX() - shot.start().x, 0.0, golem.getZ() - shot.start().z).normalize();
                    Vec3 flat = new Vec3(shot.motion().x, 0.0, shot.motion().z).normalize();
                    double speed = shot.motion().length();
                    String what = name + "'s laser ball (start offset " + offset + ", motion " + shot.motion() + ")";
                    if (Math.abs(horizontal - xzoff) > 0.05 || Math.abs(offset.y - yoff) > 0.05) {
                        cleanup.run();
                        helper.fail(what + " left " + horizontal + " ahead at height " + offset.y + ", expected " + xzoff + " at " + yoff);
                    }
                    if (flat.dot(toGolem) < 0.95 || Math.abs(speed - 1.4) > 0.25) {
                        cleanup.run();
                        helper.fail(what + " is not aimed at the golem at speed 1.4: heading dot " + flat.dot(toGolem) + ", speed " + speed);
                    }
                })
                .thenWaitUntil(() -> {
                    if (blows.stream().noneMatch(b -> b.source().getDirectEntity() instanceof LaserBall)) {
                        throw new GameTestAssertException("no laser ball of " + name + " has hit the golem yet (" + shots.size() + " fired)");
                    }
                })
                .thenExecute(() -> {
                    Blow hit = blows.stream().filter(b -> b.source().getDirectEntity() instanceof LaserBall).findFirst().orElseThrow();
                    cleanup.run();
                    helper.assertTrue(hit.source().is(DamageTypes.THROWN) && Math.abs(hit.amount() - 16.0F) < 1.0e-3F,
                            name + "'s laser ball hit the golem with " + hit + ", expected thrown 16");
                })
                .thenSucceed();
    }

    @GameTest(template = ARENA, timeoutTicks = 400, batch = "w07_shoot_robo_gunner")
    public static void roboGunnerFiresLaserBallsAtItsTarget(GameTestHelper helper) {
        IronGolem golem = dummy(helper, new BlockPos(11, 2, 6));
        Robot3 robot = helper.spawn(ModEntities.ROBO_GUNNER.get(), new BlockPos(3, 2, 6));
        faceEast(robot);
        firesLaserBallsAt(helper, robot, golem, 1.75, 3.0);
    }

    @GameTest(template = ARENA, timeoutTicks = 400, batch = "w07_shoot_robo_sniper")
    public static void roboSniperFiresLaserBallsAtItsTarget(GameTestHelper helper) {
        IronGolem golem = dummy(helper, new BlockPos(11, 2, 6));
        Robot5 robot = helper.spawn(ModEntities.ROBO_SNIPER.get(), new BlockPos(2, 2, 6));
        faceEast(robot);
        firesLaserBallsAt(helper, robot, golem, 1.6, 1.6);
    }

    // ------------------------------------------------------------------ the Large Worm's brood

    /**
     * WormLarge.java:101-157: on its first server tick a Large Worm that no spawner suppressed hatches 20 Small and 20
     * Medium Worms around itself, once ({@code wormsSpawned}). The count is taken the tick after the worm's first tick and
     * again 20 ticks later - a second brood would double it.
     */
    @GameTest(template = ARENA, timeoutTicks = 100, batch = "w07_large_worm_brood")
    public static void largeWormHatchesItsBroodOnItsFirstTick(GameTestHelper helper) {
        AABB area = helper.getBounds().inflate(16.0);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(WormSmall.class, area).isEmpty()
                && helper.getLevel().getEntitiesOfClass(WormMedium.class, area).isEmpty(), "precondition: no worms around");
        WormLarge worm = helper.spawn(ModEntities.LARGE_WORM.get(), new BlockPos(6, 2, 6));
        int[] firstCount = new int[2];
        helper.startSequence()
                .thenWaitUntil(() -> {
                    if (worm.tickCount < 1) {
                        throw new GameTestAssertException("the Large Worm has not ticked yet");
                    }
                })
                .thenExecute(() -> {
                    firstCount[0] = helper.getLevel().getEntities(ModEntities.SMALL_WORM.get(), area, e -> true).size();
                    firstCount[1] = helper.getLevel().getEntities(ModEntities.MEDIUM_WORM.get(), area, e -> true).size();
                })
                .thenIdle(20)
                .thenExecute(() -> {
                    int small = helper.getLevel().getEntities(ModEntities.SMALL_WORM.get(), area, e -> true).size();
                    int medium = helper.getLevel().getEntities(ModEntities.MEDIUM_WORM.get(), area, e -> true).size();
                    clearMobsAround(helper);
                    helper.assertTrue(firstCount[0] == 20 && firstCount[1] == 20, "after the first tick the brood was "
                            + firstCount[0] + " small and " + firstCount[1] + " medium worms, expected 20 and 20");
                    helper.assertTrue(small == 20 && medium == 20, "20 ticks later there were " + small + " small and "
                            + medium + " medium worms - the brood hatched again");
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ spawn eggs spawn

    /**
     * Every W07 egg spawns its entity through the item's own {@code useOn} (ItemSpawnEgg.java:23-35), at the clicked
     * block's centre 1.01 above it, and is used up in survival (W06GameTests pattern). Each spawned mob is discarded in
     * the same tick, before it can hunt or hatch a brood.
     */
    @GameTest(template = ARENA, batch = "w07_eggs")
    public static void everyW07SpawnEggSpawnsItsEntity(GameTestHelper helper) {
        List<Map.Entry<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>>> eggs = new ArrayList<>(w07Eggs().entrySet());
        BlockPos floor = helper.absolutePos(new BlockPos(6, 1, 6));
        AABB around = new AABB(floor).move(0.0, 1.0, 0.0).inflate(1.0);
        List<String> failures = new ArrayList<>();
        for (Map.Entry<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> c : eggs) {
            String id = c.getKey().getId().getPath();
            EntityType<?> type = c.getValue().get();
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(c.getKey().get()));
            ItemStack stack = player.getMainHandItem();
            Set<Entity> before = new HashSet<>(helper.getLevel().getEntities((Entity) null, around, e -> e.getType() == type));
            InteractionResult result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0.0, 0.5, 0.0), Direction.UP, floor, false)));
            List<Entity> spawned = helper.getLevel().getEntities((Entity) null, around, e -> e.getType() == type && !before.contains(e));
            if (spawned.size() != 1) {
                failures.add(id + " spawned " + spawned.size() + " " + EntityType.getKey(type));
            } else {
                Entity e = spawned.get(0);
                if (Math.abs(e.getX() - (floor.getX() + 0.5)) > 1.0e-6 || Math.abs(e.getY() - (floor.getY() + 1.01)) > 1.0e-6
                        || Math.abs(e.getZ() - (floor.getZ() + 0.5)) > 1.0e-6) {
                    failures.add(id + " spawned at " + helper.relativeVec(e.position()) + ", expected (6.5, 2.01, 6.5)");
                }
            }
            spawned.forEach(Entity::discard);
            if (!result.consumesAction() || !stack.isEmpty()) {
                failures.add(id + ": result " + result + ", " + stack.getCount() + " left in hand");
            }
        }
        clearMobsAround(helper);
        helper.assertTrue(eggs.size() == 30, "expected 30 W07 eggs, listed " + eggs.size());
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    /** R20: the Fairy Sword extends ItemSword in the original (FairySword.java:10) and takes the six sword tags. */
    @GameTest(template = ARENA, batch = WIRING)
    public static void fairySwordIsEnchantableLikeAnItemSword(GameTestHelper helper) {
        ItemStack sword = new ItemStack(ModItems.FAIRY_SWORD.get());
        for (TagKey<Item> tag : List.of(ItemTags.SWORD_ENCHANTABLE, ItemTags.SHARP_WEAPON_ENCHANTABLE, ItemTags.WEAPON_ENCHANTABLE,
                ItemTags.FIRE_ASPECT_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE)) {
            helper.assertTrue(sword.is(tag), "fairysword is missing from #" + tag.location());
        }
        helper.succeed();
    }

    /**
     * w07-fix. EnderKnight.java:235-249: only {@code EntityDamageSourceIndirect} triggers the teleport dodge; a 1.7.10
     * explosion never was one. An unowned TNT blast (direct = TNT, cause = null, not {@code isDirect()}) must hurt.
     */
    @GameTest(template = ARENA, timeoutTicks = 100, batch = ENDER_KNIGHT_NIGHT)
    public static void enderKnightTakesExplosionDamageInsteadOfTeleporting(GameTestHelper helper) {
        EnderKnight knight = helper.spawn(ModEntities.ENDER_KNIGHT.get(), new BlockPos(6, 2, 6));
        net.minecraft.world.entity.item.PrimedTnt tnt = new net.minecraft.world.entity.item.PrimedTnt(helper.getLevel(),
                knight.getX(), knight.getY(), knight.getZ(), null);
        DamageSource blast = helper.getLevel().damageSources().explosion(tnt, null);
        helper.assertFalse(blast.isDirect(), "precondition: the 1.21.1 TNT blast source is not direct");
        float before = knight.getHealth();
        helper.assertTrue(knight.hurt(blast, 4.0F), "the knight did not take the explosion damage");
        helper.assertTrue(knight.getHealth() < before, "health unchanged after the blast: " + knight.getHealth());
        helper.succeed();
    }

    /**
     * w07-fix. Fairy.java:99-108: the Forge {@code getDisplayName()} of the 20.3 jar is the plain username, so a team
     * prefix must not reach {@code MyOwner}, and the owner stays findable by name.
     */
    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void fairyStoresThePlainOwnerNameEvenOnAPrefixedTeam(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        net.minecraft.world.scores.Scoreboard board = helper.getLevel().getScoreboard();
        String teamName = "w07fairyteam";
        net.minecraft.world.scores.PlayerTeam team = board.getPlayerTeam(teamName);
        if (team == null) {
            team = board.addPlayerTeam(teamName);
        }
        team.setPlayerPrefix(net.minecraft.network.chat.Component.literal("[P]"));
        team.setPlayerSuffix(net.minecraft.network.chat.Component.literal("[S]"));
        board.addPlayerToTeam(player.getScoreboardName(), team);
        try {
            Fairy fairy = helper.spawn(ModEntities.FAIRY.get(), new BlockPos(6, 3, 6));
            fairy.setOwner(player);
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            fairy.addAdditionalSaveData(tag);
            helper.assertTrue(player.getGameProfile().getName().equals(tag.getString("MyOwner")),
                    "MyOwner is '" + tag.getString("MyOwner") + "', expected '" + player.getGameProfile().getName() + "'");
        } finally {
            board.removePlayerTeam(team);
            player.remove(Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }
}
