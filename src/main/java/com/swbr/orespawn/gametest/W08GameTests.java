package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.island.Island;
import com.swbr.orespawn.entity.island.IslandToo;
import com.swbr.orespawn.entity.moth.Brutalfly;
import com.swbr.orespawn.entity.moth.CaterKiller;
import com.swbr.orespawn.entity.nightmare.PitchBlack;
import com.swbr.orespawn.entity.projectile.InkSack;
import com.swbr.orespawn.entity.sea.AttackSquid;
import com.swbr.orespawn.entity.sea.Irukandji;
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
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * W08 integration (DECISIONS R4, R9, R17, R18) on a real server.
 *
 * <p>Every living W08 type except the two islands spawns through its registry factory, has the manifest hitbox on its
 * type, the original maximum health and is still alive after 100 ticks with its AI running. Each of these tests is its
 * own batch (W07 lesson: the mobs hunt across the grid). The water mobs spawn in a walled pool - the Irukandji has one
 * health point and dries out on land within a few hundred ticks, which is original behaviour, not a port error. The
 * Nightmare (PitchBlack) and the Crab roll their scale at spawn: their health and hitbox are checked against the rolled
 * scale, not a constant.
 *
 * <p>Behaviour half, from the wave plan (catalogue README 4.9): water mobs on land dry out (Irukandji dies, Attack Squid
 * loses health), IslandToo builds an island on its first tick, both islands move their blocks one cell (with health 20,
 * hitbox 0.5 and 100 ticks alive), a wounded CaterKiller becomes a Brutalfly and ten butterflies while one at
 * {@code max - 1} does not, the five Nightmare sizes have five hitboxes; wiring: every egg spawns its type through
 * {@code useOn}, the tags, and the ink sack passing through an Attack Squid (InkSack.java:58-60).
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W08GameTests {

    private static final String ARENA = "arena";
    private static final String WIRING = "w08_wiring";

    private W08GameTests() {}

    private static void clearMobsAround(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds().inflate(48.0), entity -> !(entity instanceof Player))
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

    /**
     * @param maxHealth expected maximum health, or a negative value when the entity rolls it (checked by the caller)
     * @param checkBox whether the spawned hitbox must equal the registered one
     */
    private static <T extends LivingEntity> T spawnAndTick(GameTestHelper helper, EntityType<T> type, BlockPos pos, float maxHealth,
            float width, float height, boolean checkBox) {
        T e = helper.spawn(type, pos);
        String name = type.toShortString();
        if (maxHealth >= 0.0f) {
            helper.assertTrue(e.getMaxHealth() == maxHealth, name + " max health " + e.getMaxHealth() + ", expected " + maxHealth);
        }
        helper.assertTrue(Math.abs(type.getWidth() - width) < 1.0e-4F && Math.abs(type.getHeight() - height) < 1.0e-4F,
                name + " is registered " + type.getWidth() + " x " + type.getHeight() + ", expected " + width + " x " + height);
        if (checkBox) {
            helper.assertTrue(Math.abs(e.getBbWidth() - width) < 1.0e-4F && Math.abs(e.getBbHeight() - height) < 1.0e-4F,
                    name + " spawned with a hitbox of " + e.getBbWidth() + " x " + e.getBbHeight() + ", expected " + width + " x " + height);
        }
        helper.runAfterDelay(100, () -> {
            boolean alive = e.isAlive();
            int ticks = e.tickCount;
            clearMobsAround(helper);
            helper.assertTrue(alive, name + " did not survive 100 ticks");
            helper.assertTrue(ticks >= 100, name + " ticked only " + ticks + " times");
            helper.succeed();
        });
        return e;
    }

    private static void land(GameTestHelper helper, EntityType<? extends LivingEntity> type, int y, float maxHealth, float w, float h) {
        spawnAndTick(helper, type, new BlockPos(6, y, 6), maxHealth, w, h, true);
    }

    private static void water(GameTestHelper helper, EntityType<? extends LivingEntity> type, float maxHealth, float w, float h) {
        pool(helper);
        spawnAndTick(helper, type, new BlockPos(6, 3, 6), maxHealth, w, h, true);
    }

    // ------------------------------------------------------------------ water mobs (w08-water)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_attack_squid")
    public static void attackSquidSpawnsAndTicks(GameTestHelper helper) {
        water(helper, ModEntities.ATTACK_SQUID.get(), 10.0f, 1.0f, 1.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_cloud_shark")
    public static void cloudSharkSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.CLOUD_SHARK.get(), 4, 15.0f, 1.0f, 0.75f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_irukandji")
    public static void irukandjiSpawnsAndTicks(GameTestHelper helper) {
        water(helper, ModEntities.IRUKANDJI.get(), 1.0f, 0.25f, 0.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_skate")
    public static void skateSpawnsAndTicks(GameTestHelper helper) {
        water(helper, ModEntities.SKATE.get(), 8.0f, 0.75f, 0.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_crystal_urchin")
    public static void crystalUrchinSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.CRYSTAL_URCHIN.get(), 2, 25.0f, 1.35f, 2.1f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_sea_monster")
    public static void seaMonsterSpawnsAndTicks(GameTestHelper helper) {
        water(helper, ModEntities.SEA_MONSTER.get(), 110.0f, 1.25f, 2.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_sea_viper")
    public static void seaViperSpawnsAndTicks(GameTestHelper helper) {
        water(helper, ModEntities.SEA_VIPER.get(), 160.0f, 1.5f, 2.5f);
    }

    // ------------------------------------------------------------------ terrors (w08-terrors)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_creeping_horror")
    public static void creepingHorrorSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.CREEPING_HORROR.get(), 2, 10.0f, 0.75f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_terrible_terror")
    public static void terribleTerrorSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.TERRIBLE_TERROR.get(), 3, 10.0f, 1.0f, 0.75f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_lurking_terror")
    public static void lurkingTerrorSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.LURKING_TERROR.get(), 2, 30.0f, 1.75f, 1.25f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_rat")
    public static void ratSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.RAT.get(), 2, 5.0f, 0.25f, 0.5f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_mantis")
    public static void mantisSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.MANTIS.get(), 2, 120.0f, 2.5f, 3.25f);
    }

    /** Crab.java: maximum health {@code (int)(PitchBlack_stats.health * getCrabScale())}, rolled per spawn. */
    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_crab")
    public static void crabSpawnsAndTicks(GameTestHelper helper) {
        pool(helper);
        var crab = spawnAndTick(helper, ModEntities.CRAB.get(), new BlockPos(6, 3, 6), -1.0f, 1.25f, 2.5f, false);
        helper.assertTrue(crab.getCrabScale() > 0.0f, "crab scale " + crab.getCrabScale());
        helper.assertTrue(crab.getMaxHealth() > 0.0f && crab.getMaxHealth() <= 1024.0f, "crab max health " + crab.getMaxHealth());
    }

    // ------------------------------------------------------------------ crystal mobs (w08-crystal-mobs)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_rotator")
    public static void rotatorSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.ROTATOR.get(), 3, 35.0f, 1.0f, 2.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_vortex")
    public static void vortexSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.VORTEX.get(), 3, 150.0f, 2.0f, 4.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_dungeon_beast")
    public static void dungeonBeastSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.DUNGEON_BEAST.get(), 2, 65.0f, 1.15f, 1.1f);
    }

    // ------------------------------------------------------------------ Triffid and Nightmare (w08-nightmare-triffid)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_triffid")
    public static void triffidSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.TRIFFID.get(), 2, 100.0f, 2.0f, 4.0f);
    }

    /** PitchBlack: health {@code PitchBlack_stats.health * scale}, clamped to 1024 (R4); hitbox 2.5 s x 3.5 s. */
    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_nightmare")
    public static void nightmareSpawnsAndTicks(GameTestHelper helper) {
        PitchBlack nightmare = spawnAndTick(helper, ModEntities.NIGHTMARE.get(), new BlockPos(6, 2, 6), -1.0f, 2.0f, 3.0f, false);
        float s = nightmare.getPitchBlackScale();
        helper.assertTrue(s > 0.0f, "nightmare scale " + s);
        float expected = Math.min(1024.0f, 250.0f * s);
        helper.assertTrue(Math.abs(nightmare.getMaxHealth() - expected) < 1.0f,
                "nightmare at scale " + s + " has max health " + nightmare.getMaxHealth() + ", expected " + expected);
    }

    // ------------------------------------------------------------------ Mothra, CaterKiller, Brutalfly (w08-mothra-caterkiller)

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_mothra")
    public static void mothraSpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.MOTHRA.get(), 4, 150.0f, 5.0f, 2.0f);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_cater_killer")
    public static void caterKillerSpawnsAndTicks(GameTestHelper helper) {
        CaterKiller cater = spawnAndTick(helper, ModEntities.CATER_KILLER.get(), new BlockPos(6, 2, 6), 450.0f, 2.9f, 4.6f, false);
        // CaterKiller setSize by PlayNicely: 2.9 x 4.6, or 1.45 x 2.3 when PlayNicely is set.
        boolean small = OreSpawnConfig.TWEAKS.PlayNicely.get() != 0;
        float w = small ? 1.45f : 2.9f;
        float h = small ? 2.3f : 4.6f;
        helper.assertTrue(Math.abs(cater.getBbWidth() - w) < 1.0e-4F && Math.abs(cater.getBbHeight() - h) < 1.0e-4F,
                "cater_killer (PlayNicely " + small + ") spawned " + cater.getBbWidth() + " x " + cater.getBbHeight() + ", expected " + w + " x " + h);
    }

    @GameTest(template = ARENA, timeoutTicks = 140, batch = "w08_brutalfly")
    public static void brutalflySpawnsAndTicks(GameTestHelper helper) {
        land(helper, ModEntities.BRUTALFLY.get(), 4, 110.0f, 5.0f, 2.0f);
    }

    /** CaterKiller.customServerAiStep (:457-468): wounded for more than 2400 ticks -> a Brutalfly four blocks up, the caterpillar is gone. */
    @GameTest(template = ARENA, timeoutTicks = 60, batch = "w08_cater_killer_brutalfly")
    public static void woundedCaterKillerBecomesABrutalfly(GameTestHelper helper) {
        CaterKiller cater = helper.spawn(ModEntities.CATER_KILLER.get(), new BlockPos(6, 2, 6));
        cater.setHealth(cater.getMaxHealth() - 10.0f);
        cater.setTicker(2400);
        helper.succeedWhen(() -> {
            List<Brutalfly> flies = helper.getLevel().getEntitiesOfClass(Brutalfly.class, helper.getBounds().inflate(16.0));
            helper.assertTrue(cater.isRemoved(), "the wounded CaterKiller is still there (ticker " + cater.getTicker() + ")");
            helper.assertTrue(flies.size() == 1, "expected one Brutalfly, found " + flies.size());
            int butterflies = helper.getLevel().getEntities(ModEntities.BUTTERFLY.get(), helper.getBounds().inflate(16.0), e -> true).size();
            helper.assertTrue(butterflies == 10, "expected ten butterflies (:462-464), found " + butterflies);
            clearMobsAround(helper);
        });
    }

    // ------------------------------------------------------------------ behaviour from the wave plan

    /**
     * Irukandji.java: on land, 1/10 a tick it looks for water within 5; none found, 1/25 it loses one health point through
     * {@code setHealth} (the 1.21.1 {@code heal} ignores negative amounts, catalogue 6.3) and is removed at 0. With one
     * health point the expected time is 250 ticks; 2400 leaves a failure chance of about e^-9.6.
     *
     * <p>W11 (W10 open point): the setup is the one of {@link #attackSquidLosesHealthOnLand}. The Irukandji runs the same
     * shell search ({@code SeaSupport.WaterScan}, shells up to 11, vertical cap 5) and wanders ({@code MyEntityAIWander}),
     * so a free one could reach a neighbour arena's pool and never dry out - a set-up flake, not chance. It now sits in
     * the glass cage in the middle of its arena, stray water in its reach is removed first, and it is persistent, so only
     * drying out can remove it: the test demands health 0 and no damage source, not just "gone".
     */
    @GameTest(template = ARENA, timeoutTicks = 2400, batch = "w08_dry_out")
    public static void irukandjiDriesOutOnLand(GameTestHelper helper) {
        int strayWater = waterInSquidReach(helper, true);
        squidCage(helper);
        Irukandji iru = helper.spawn(ModEntities.IRUKANDJI.get(), new BlockPos(6, 2, 6));
        iru.setPersistenceRequired();
        helper.succeedWhen(() -> {
            helper.assertTrue(iru.isRemoved() || !iru.isAlive(), "the Irukandji is still alive on land after " + iru.tickCount + " ticks at "
                    + iru.blockPosition() + " (" + waterInSquidReach(helper, false) + " water blocks in its search reach now, " + strayWater
                    + " removed before the start)");
            helper.assertTrue(iru.getHealth() <= 0.0f, "the Irukandji was removed with " + iru.getHealth() + " health, not dried out");
            helper.assertTrue(iru.getLastDamageSource() == null, "the Irukandji died from damage, not drying out: " + iru.getLastDamageSource());
            clearMobsAround(helper);
        });
    }

    /**
     * IslandToo.tick / create_island (IslandToo.java:47-97, 180-225): on its first tick the island rolls its size and builds
     * a grass top at {@code floor(Y) + depth} over a stone core whose bottom layer spans at least one block around the centre, and the block at its own position is cleared (:292). The island
     * sits 40 blocks above the arena and is removed afterwards, blocks included (it writes outside the arena: own batch).
     */
    @GameTest(template = ARENA, timeoutTicks = 40, batch = "w08_island")
    public static void islandTooBuildsAnIslandOnItsFirstTick(GameTestHelper helper) {
        IslandToo island = helper.spawn(ModEntities.ISLAND_TOO.get(), new BlockPos(6, 40, 6));
        helper.runAfterDelay(2, () -> {
            int depth = island.islandDepth();
            int width = island.islandWidth();
            int xoff = island.getX() < 0.0 ? 1 : 0;
            int zoff = island.getZ() < 0.0 ? 1 : 0;
            int cx = (int) island.getX() - xoff;
            int cy = Mth.floor(island.getY());
            int cz = (int) island.getZ() - zoff;
            BlockState top = helper.getLevel().getBlockState(new BlockPos(cx, cy + depth, cz));
            // The bottom layer k = 0 spans at least +-1 (il >= 1); its centre is cleared again (:292), so look one block east.
            BlockState below = helper.getLevel().getBlockState(new BlockPos(cx + 1, cy, cz));
            BlockState self = helper.getLevel().getBlockState(new BlockPos(cx, cy, cz));
            boolean removedOk = island.isAlive();
            island.discard();
            int r = Math.max(width, 13) + 2;
            BlockPos.betweenClosed(cx - r, cy - 1, cz - r, cx + r, cy + 12, cz + r)
                    .forEach(p -> helper.getLevel().setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16));
            helper.getLevel().getEntitiesOfClass(Entity.class, new AABB(cx - r, cy - 1, cz - r, cx + r, cy + 14, cz + r), e -> !(e instanceof Player))
                    .forEach(Entity::discard);
            helper.assertTrue(removedOk, "the island removed itself");
            helper.assertTrue(depth >= 1 && width >= 1, "island size " + width + " x " + depth);
            helper.assertTrue(top.is(Blocks.GRASS_BLOCK) || top.is(Blocks.WATER), "island top is " + top);
            helper.assertTrue(!below.isAir(), "island bottom layer next to its centre is air (depth " + depth + ")");
            helper.assertTrue(self.isAir(), "the island's own block is " + self);
            helper.succeed();
        });
    }

    /**
     * Counter-check of the Brutalfly condition (CaterKiller.java:457): {@code health + 1 < maxHealth}, strictly. At
     * {@code max - 1} the ticker neither counts nor fires, even when it already stands at 2400.
     */
    @GameTest(template = ARENA, timeoutTicks = 60, batch = "w08_cater_killer_unhurt")
    public static void barelyScratchedCaterKillerStaysACaterpillar(GameTestHelper helper) {
        CaterKiller cater = helper.spawn(ModEntities.CATER_KILLER.get(), new BlockPos(6, 2, 6));
        cater.setHealth(cater.getMaxHealth() - 1.0f);
        cater.setTicker(2400);
        helper.runAfterDelay(20, () -> {
            boolean removed = cater.isRemoved();
            int ticker = cater.getTicker();
            int flies = helper.getLevel().getEntitiesOfClass(Brutalfly.class, helper.getBounds().inflate(16.0)).size();
            clearMobsAround(helper);
            helper.assertFalse(removed, "a CaterKiller at max - 1 health turned into a Brutalfly");
            helper.assertTrue(ticker == 2400, "the ticker counted at max - 1 health: " + ticker);
            helper.assertTrue(flies == 0, "found " + flies + " Brutalflies");
            helper.succeed();
        });
    }

    /**
     * The block box the Attack Squid's water search can reach from anywhere inside {@link #squidCage}: shells up to 11
     * on x/z around the feet cell (5..7) and +-5 on y around one block below the feet (feet on the floor at y 2 or, mid-jump, y 3; the template's stone
     * floor is relative y 1, the structure sits one block above its structure block).
     * Relative x/z -6..18 stays inside this arena and the 5-block gap of the structure grid - the neighbour arenas of
     * earlier batches start at -6 (their glass ring) or further out.
     */
    private static final int SQUID_SCAN_MIN = -6;
    private static final int SQUID_SCAN_MAX = 18;

    /** Water blocks ({@code Blocks.WATER}, source or flowing - the scan's test) in the squid's reach. */
    private static int waterInSquidReach(GameTestHelper helper, boolean clear) {
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(SQUID_SCAN_MIN, -5, SQUID_SCAN_MIN, SQUID_SCAN_MAX, 8, SQUID_SCAN_MAX)) {
            if (helper.getBlockState(p).is(Blocks.WATER)) {
                ++count;
                if (clear) {
                    helper.setBlock(p.immutable(), Blocks.AIR);
                }
            }
        }
        return count;
    }

    /** Glass ring at x/z 4 and 8 on y 2..3 around the 3 x 3 floor cell (5..7) the squid is spawned in. */
    private static void squidCage(GameTestHelper helper) {
        for (int x = 4; x <= 8; ++x) {
            for (int z = 4; z <= 8; ++z) {
                if (x == 4 || x == 8 || z == 4 || z == 8) {
                    helper.setBlock(new BlockPos(x, 2, z), Blocks.GLASS);
                    helper.setBlock(new BlockPos(x, 3, z), Blocks.GLASS);
                }
            }
        }
    }

    /**
     * AttackSquid.java (updateAITasks, on land): with no water found by the shell search (11 on x/z, 5 on y), 1/10 x 1/25
     * a tick it loses one health point through {@code setHealth} (catalogue 6.3; the 1.21.1 {@code heal} ignores negative
     * amounts). Expected after 250 ticks; 2400 leaves a failure chance of about e^-9.6.
     *
     * <p>The flake of the W09 test run (10 of 10 health after 2401 ticks) was not chance but the test setup: the structure
     * grid keeps every earlier batch's arena ({@code StructureGridSpawner(pos, 8, false)} - no clearing between batches),
     * the pools of the W08/W09 water tests stay filled, and a free squid wanders ({@code MyEntityAIWanderALot} 16) until
     * a neighbour's water is within 11 blocks. From then on every search finds water and the original never dries it
     * out - correct behaviour, see {@link #attackSquidNextToWaterDoesNotDryOut}. Which neighbours hold water depends on
     * the batch order, hence the random position. The squid now sits in a glass cage in the middle of its arena, where
     * its reach cannot touch a neighbour arena, and stray water in that reach is removed first.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400, batch = "w08_dry_out_squid")
    public static void attackSquidLosesHealthOnLand(GameTestHelper helper) {
        int strayWater = waterInSquidReach(helper, true);
        squidCage(helper);
        AttackSquid squid = helper.spawn(ModEntities.ATTACK_SQUID.get(), new BlockPos(6, 2, 6));
        squid.setPersistenceRequired();
        float max = squid.getMaxHealth();
        helper.succeedWhen(() -> {
            helper.assertTrue(squid.getHealth() < max || squid.isRemoved(),
                    "the Attack Squid still has " + squid.getHealth() + " of " + max + " after " + squid.tickCount + " ticks on land at "
                            + squid.blockPosition() + " (" + waterInSquidReach(helper, false) + " water blocks in its search reach now, "
                            + strayWater + " removed before the start)");
            helper.assertTrue(squid.getLastDamageSource() == null, "the squid lost health through damage, not drying out: " + squid.getLastDamageSource());
            clearMobsAround(helper);
        });
    }

    /**
     * Counter-check and pinned reproduction of the old flake: AttackSquid.java updateAITasks only loses health in the
     * {@code closest == 99999} branch. One water source behind glass five blocks away (reached by shell 5) is found on
     * every search, the path to it fails at the glass, and the caged squid keeps full health. Deterministic: with water
     * in reach the original has no health loss at all.
     */
    @GameTest(template = ARENA, timeoutTicks = 700, batch = "w08_dry_out_squid_near_water")
    public static void attackSquidNextToWaterDoesNotDryOut(GameTestHelper helper) {
        waterInSquidReach(helper, true);
        squidCage(helper);
        BlockPos water = new BlockPos(6, 2, 11);
        for (BlockPos glass : List.of(water.west(), water.east(), water.north(), water.south(), water.above())) {
            helper.setBlock(glass, Blocks.GLASS);
        }
        helper.setBlock(water, Blocks.WATER);
        AttackSquid squid = helper.spawn(ModEntities.ATTACK_SQUID.get(), new BlockPos(6, 2, 6));
        squid.setPersistenceRequired();
        float max = squid.getMaxHealth();
        helper.runAfterDelay(600, () -> {
            float health = squid.getHealth();
            boolean removed = squid.isRemoved();
            boolean wet = squid.isInWater();
            int reach = waterInSquidReach(helper, false);
            clearMobsAround(helper);
            helper.setBlock(water, Blocks.AIR);
            helper.assertTrue(!wet, "the caged squid is in water");
            helper.assertTrue(reach == 1, "expected exactly the one water block in reach, found " + reach);
            helper.assertTrue(!removed && health == max, "the Attack Squid dried out with water in reach: " + health + " of " + max
                    + " after 600 ticks (removed " + removed + ")");
            helper.succeed();
        });
    }

    /**
     * PitchBlack.tick (:222): {@code setSize(2.5 * scale, 3.5 * scale)} every tick. The five Nightmare sizes 0.5 / 1 / 2 /
     * 3 / 4 (entityInit :73-117) get five different hitboxes on the server.
     */
    @GameTest(template = ARENA, timeoutTicks = 40, batch = "w08_nightmare_sizes")
    public static void nightmareSizesHaveDifferentHitboxes(GameTestHelper helper) {
        float[] scales = {0.5f, 1.0f, 2.0f, 3.0f, 4.0f};
        List<PitchBlack> nightmares = new ArrayList<>();
        for (int i = 0; i < scales.length; ++i) {
            PitchBlack n = helper.spawn(ModEntities.NIGHTMARE.get(), new BlockPos(2 + 2 * i, 2, 6));
            n.setNoAi(true);
            n.setPitchBlackScale(scales[i]);
            nightmares.add(n);
        }
        helper.runAfterDelay(3, () -> {
            List<String> failures = new ArrayList<>();
            Set<Float> widths = new HashSet<>();
            for (int i = 0; i < scales.length; ++i) {
                PitchBlack n = nightmares.get(i);
                float s = n.getPitchBlackScale();
                if (Math.abs(s - scales[i]) > 1.0e-4F) {
                    failures.add("scale " + scales[i] + " read back as " + s);
                }
                float w = 2.5f * scales[i];
                float h = 3.5f * scales[i];
                if (Math.abs(n.getBbWidth() - w) > 1.0e-4F || Math.abs(n.getBbHeight() - h) > 1.0e-4F
                        || Math.abs(n.getBoundingBox().getXsize() - w) > 1.0e-3 || Math.abs(n.getBoundingBox().getYsize() - h) > 1.0e-3) {
                    failures.add("scale " + scales[i] + ": " + n.getBbWidth() + " x " + n.getBbHeight() + " (box " + n.getBoundingBox()
                            + "), expected " + w + " x " + h);
                }
                widths.add(n.getBbWidth());
            }
            clearMobsAround(helper);
            helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
            helper.assertTrue(widths.size() == 5, "only " + widths.size() + " different hitbox widths");
            helper.succeed();
        });
    }

    /** Clears blocks and entities an island wrote 40 blocks above the arena. */
    private static void clearIsland(GameTestHelper helper, int cx, int cy, int cz, int r, int below, int above) {
        BlockPos.betweenClosed(cx - r, cy - below, cz - r, cx + r, cy + above, cz + r)
                .forEach(p -> helper.getLevel().setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16));
        helper.getLevel().getEntitiesOfClass(Entity.class, new AABB(cx - r, cy - below, cz - r, cx + r + 1, cy + above + 6, cz + r + 1),
                e -> !(e instanceof Player)).forEach(Entity::discard);
    }

    /** Highest X of {@code block} in the layer {@code y} around {@code (cx, cz)}, or {@code Integer.MIN_VALUE}. */
    private static int maxX(GameTestHelper helper, net.minecraft.world.level.block.Block block, int cx, int y, int cz, int r) {
        int best = Integer.MIN_VALUE;
        for (int x = cx - r; x <= cx + r; ++x) {
            for (int z = cz - r; z <= cz + r; ++z) {
                if (helper.getLevel().getBlockState(new BlockPos(x, y, z)).is(block)) {
                    best = Math.max(best, x);
                }
            }
        }
        return best;
    }

    /**
     * Island.tick / create_island / update_island (Island.java:43-91, 134-336): on its first tick the round island puts
     * mycelium on {@code floor(Y) + 1} and end stone (1/10 diamond ore) on {@code floor(Y)}; with the direction set to 0
     * and a speed of a whole block, the next 73-tick update moves the entity along +X, Z unchanged, and the mycelium disc
     * with it. The entity keeps 20 health (1.7.10 EntityLiving default), its 0.5 hitbox and ticks at least 100 times.
     */
    @GameTest(template = ARENA, timeoutTicks = 260, batch = "w08_island_round")
    public static void islandBuildsARoundIslandAndMovesIt(GameTestHelper helper) {
        Island island = helper.spawn(ModEntities.ISLAND.get(), new BlockPos(6, 40, 6));
        helper.assertTrue(island.getMaxHealth() == 20.0f, "island max health " + island.getMaxHealth());
        helper.assertTrue(Math.abs(island.getBbWidth() - 0.5f) < 1.0e-4F && Math.abs(island.getBbHeight() - 0.5f) < 1.0e-4F,
                "island hitbox " + island.getBbWidth() + " x " + island.getBbHeight());
        int[] before = new int[5]; // cy, radius, cz, mycelium maxX, start block x
        double[] start = new double[2];
        int scan = 16;
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    int cx = Mth.floor(island.getX());
                    int cy = Mth.floor(island.getY());
                    int cz = Mth.floor(island.getZ());
                    before[0] = cy;
                    before[1] = island.islandRadius();
                    before[2] = cz;
                    before[3] = maxX(helper, Blocks.MYCELIUM, cx, cy + 1, cz, scan);
                    before[4] = cx;
                    start[0] = island.getX();
                    start[1] = island.getZ();
                    int endStone = 0;
                    for (int x = cx - scan; x <= cx + scan; ++x) {
                        for (int z = cz - scan; z <= cz + scan; ++z) {
                            BlockState b = helper.getLevel().getBlockState(new BlockPos(x, cy, z));
                            if (b.is(Blocks.END_STONE) || b.is(Blocks.DIAMOND_ORE)) {
                                ++endStone;
                            }
                        }
                    }
                    island.setIslandMotion(0.0f, 1.0f);
                    if (before[3] == Integer.MIN_VALUE || endStone == 0 || !island.isAlive()) {
                        clearIsland(helper, cx, cy, cz, scan + 4, 8, 4);
                        helper.fail("no island: mycelium maxX " + before[3] + ", end stone " + endStone + ", alive " + island.isAlive()
                                + ", radius " + before[1] + ", depth " + island.islandDepth());
                    }
                })
                .thenWaitUntil(() -> helper.assertTrue(island.getX() != start[0], "the island has not moved yet"))
                .thenExecute(() -> {
                    double x = island.getX();
                    double z = island.getZ();
                    int cy = before[0];
                    int after = maxX(helper, Blocks.MYCELIUM, Mth.floor(x), cy + 1, before[2], scan);
                    if (!(x > start[0] && z == start[1] && after > before[3])) {
                        clearIsland(helper, before[4], cy, before[2], scan + 6, 8, 4);
                        helper.fail("island moved from " + start[0] + "/" + start[1] + " to " + x + "/" + z + ", mycelium maxX "
                                + before[3] + " -> " + after);
                    }
                })
                .thenWaitUntil(() -> helper.assertTrue(island.tickCount >= 100, "island ticked " + island.tickCount))
                .thenExecute(() -> {
                    boolean alive = island.isAlive();
                    clearIsland(helper, before[4], before[0], before[2], scan + 8, 8, 4);
                    helper.assertTrue(alive, "the island died within 100 ticks");
                })
                .thenSucceed();
    }

    /**
     * IslandToo.update_island (IslandToo.java:285-476), direction 2 (+X) at a whole block per 42-tick update: the square
     * grass top moves exactly one cell - the leading edge {@code newCx + width} becomes grass, the trailing edge
     * {@code oldCx - width} air. Health 20, hitbox 0.5, at least 100 ticks alive.
     *
     * <p>W11 (W10 open point), three set-up pins, none of which changes what is checked:
     * <ul>
     * <li>The air the island can write into is cleared before the spawn (largest island for the configured
     * {@code IslandSizeFactor}, one cell of travel, trees on top). The structure grid keeps earlier batches' blocks; an
     * end stone left over from {@link #islandBuildsARoundIslandAndMovesIt} in a leading lower layer makes the move
     * explode, and a non-air block on the top layer is skipped by the grass placement.</li>
     * <li>The direction is set again after every server tick until the move is seen. {@code tick} re-rolls {@code dir}
     * when its private {@code dirchange} (0..9999 after the build) runs out, and nothing outside the class can pin that
     * counter; the GameTest ticker runs after the level tick, so the entity always updates with direction 2.</li>
     * <li>After the checked move the speed is set to 0: the island stays inside the cleared box until the cleanup, and
     * the 100-tick life check no longer depends on where later random moves take it.</li>
     * </ul>
     * A random update before the first pin (the build tick can reach {@code ticker >= 42}) only moves the private
     * {@code myX/myZ} by at most {@code 1/40 * 4 * IslandSpeedFactor} (0.2 at the default 2, 0.5 at the clamp 5) from the
     * block centre, which cannot change the cell; the snapshot asserts the island still stands on its spawn centre.
     */
    @GameTest(template = ARENA, timeoutTicks = 260, batch = "w08_island_square")
    public static void islandTooMovesItsIsland(GameTestHelper helper) {
        BlockPos spawnRel = new BlockPos(6, 40, 6);
        BlockPos spawnAbs = helper.absolutePos(spawnRel);
        // Largest width 5 + nextInt(8 * IslandSizeFactor) (IslandToo.tick, the 1/40 branch), one cell of travel, margin.
        int clearR = 4 + 8 * com.swbr.orespawn.config.stats.TweakStats.IslandSizeFactor() + 3;
        // Depth at most 3 + 5 = 8; a small sky tree on the top layer stays below 16 more.
        BlockPos.betweenClosed(spawnAbs.getX() - clearR, spawnAbs.getY() - 2, spawnAbs.getZ() - clearR,
                        spawnAbs.getX() + clearR, spawnAbs.getY() + 24, spawnAbs.getZ() + clearR)
                .forEach(p -> {
                    if (!helper.getLevel().getBlockState(p).isAir()) {
                        helper.getLevel().setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
                    }
                });
        IslandToo island = helper.spawn(ModEntities.ISLAND_TOO.get(), spawnRel);
        helper.assertTrue(island.getMaxHealth() == 20.0f, "island_too max health " + island.getMaxHealth());
        helper.assertTrue(Math.abs(island.getBbWidth() - 0.5f) < 1.0e-4F && Math.abs(island.getBbHeight() - 0.5f) < 1.0e-4F,
                "island_too hitbox " + island.getBbWidth() + " x " + island.getBbHeight());
        int[] before = new int[5]; // cx, cy, cz, width, depth
        double[] start = new double[2];
        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    before[0] = (int) island.getX() - (island.getX() < 0.0 ? 1 : 0);
                    before[1] = Mth.floor(island.getY());
                    before[2] = (int) island.getZ() - (island.getZ() < 0.0 ? 1 : 0);
                    before[3] = island.islandWidth();
                    before[4] = island.islandDepth();
                    start[0] = island.getX();
                    start[1] = island.getZ();
                    island.setIslandMotion(2, 1.0f);
                    BlockState top = helper.getLevel().getBlockState(new BlockPos(before[0] + before[3], before[1] + before[4], before[2]));
                    boolean centred = start[0] == spawnAbs.getX() + 0.5 && start[1] == spawnAbs.getZ() + 0.5;
                    if (!centred || !(top.is(Blocks.GRASS_BLOCK) || top.is(Blocks.WATER))) {
                        clearIsland(helper, before[0], before[1], before[2], clearR + 2, 2, 24);
                        helper.fail("island_too top edge before the move is " + top + " (width " + before[3] + ", depth " + before[4]
                                + "), position " + start[0] + "/" + start[1] + " for the spawn cell " + spawnAbs);
                    }
                })
                .thenWaitUntil(() -> {
                    boolean moved = island.getX() != start[0];
                    if (!moved) {
                        // Pin the direction for the next entity tick; dirchange may have re-rolled it in this one.
                        island.setIslandMotion(2, 1.0f);
                    }
                    helper.assertTrue(moved, "the island has not moved yet");
                })
                .thenExecute(() -> {
                    island.setIslandMotion(2, 0.0f);
                    double x = island.getX();
                    int ncx = (int) x - (x < 0.0 ? 1 : 0);
                    int topY = before[1] + before[4];
                    BlockState lead = helper.getLevel().getBlockState(new BlockPos(ncx + before[3], topY, before[2]));
                    BlockState trail = helper.getLevel().getBlockState(new BlockPos(before[0] - before[3], topY, before[2]));
                    boolean ok = x > start[0] && island.getZ() == start[1] && ncx == before[0] + 1
                            && (lead.is(Blocks.GRASS_BLOCK) || lead.is(Blocks.WATER)) && trail.isAir();
                    if (!ok) {
                        clearIsland(helper, before[0], before[1], before[2], clearR + 2, 2, 24);
                        helper.fail("island_too moved " + start[0] + "/" + start[1] + " -> " + x + "/" + island.getZ() + " (cell " + before[0]
                                + " -> " + ncx + "), leading edge " + lead + ", trailing edge " + trail);
                    }
                })
                .thenWaitUntil(() -> helper.assertTrue(island.tickCount >= 100, "island_too ticked " + island.tickCount))
                .thenExecute(() -> {
                    boolean alive = island.isAlive();
                    clearIsland(helper, before[0], before[1], before[2], clearR + 2, 2, 24);
                    helper.assertTrue(alive, "island_too died within 100 ticks");
                })
                .thenSucceed();
    }

    // ------------------------------------------------------------------ wiring (integrator)

    private static Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> w08Eggs() {
        Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> eggs = new LinkedHashMap<>();
        eggs.put(ModItems.EGG_MOTHRA, ModEntities.MOTHRA);
        eggs.put(ModItems.EGG_ATTACK_SQUID, ModEntities.ATTACK_SQUID);
        eggs.put(ModItems.EGG_CREEPING_HORROR, ModEntities.CREEPING_HORROR);
        eggs.put(ModItems.EGG_TERRIBLE_TERROR, ModEntities.TERRIBLE_TERROR);
        eggs.put(ModItems.EGG_TRIFFID, ModEntities.TRIFFID);
        eggs.put(ModItems.EGG_NIGHTMARE, ModEntities.NIGHTMARE);
        eggs.put(ModItems.EGG_LURKING_TERROR, ModEntities.LURKING_TERROR);
        eggs.put(ModItems.EGG_CLOUD_SHARK, ModEntities.CLOUD_SHARK);
        eggs.put(ModItems.EGG_ROTATOR, ModEntities.ROTATOR);
        eggs.put(ModItems.EGG_DUNGEON_BEAST, ModEntities.DUNGEON_BEAST);
        eggs.put(ModItems.EGG_VORTEX, ModEntities.VORTEX);
        eggs.put(ModItems.EGG_MANTIS, ModEntities.MANTIS);
        eggs.put(ModItems.EGG_RAT, ModEntities.RAT);
        eggs.put(ModItems.EGG_IRUKANDJI, ModEntities.IRUKANDJI);
        eggs.put(ModItems.EGG_SKATE, ModEntities.SKATE);
        eggs.put(ModItems.EGG_URCHIN, ModEntities.CRYSTAL_URCHIN);
        eggs.put(ModItems.EGG_SEA_MONSTER, ModEntities.SEA_MONSTER);
        eggs.put(ModItems.EGG_SEA_VIPER, ModEntities.SEA_VIPER);
        eggs.put(ModItems.EGG_CATER_KILLER, ModEntities.CATER_KILLER);
        eggs.put(ModItems.EGG_BRUTALFLY, ModEntities.BRUTALFLY);
        eggs.put(ModItems.EGG_CRAB, ModEntities.CRAB);
        return eggs;
    }

    @GameTest(template = ARENA, batch = WIRING)
    public static void everyW08SpawnEggNamesItsType(GameTestHelper helper) {
        Map<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> eggs = w08Eggs();
        helper.assertTrue(eggs.size() == 21, "expected 21 W08 eggs, listed " + eggs.size());
        eggs.forEach((egg, type) -> helper.assertTrue(egg.get().getType() == type.get(),
                egg.getId() + " spawns " + egg.get().getType().toShortString() + ", expected " + type.get().toShortString()));
        helper.succeed();
    }

    /**
     * Every W08 egg spawns its entity through the item's own {@code useOn} (ItemSpawnEgg.java:23-35), at the clicked
     * block's centre 1.01 above it, and is used up in survival (W07 pattern). Each mob is discarded in the same tick.
     */
    @GameTest(template = ARENA, batch = "w08_eggs")
    public static void everyW08SpawnEggSpawnsItsEntity(GameTestHelper helper) {
        List<Map.Entry<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>>> eggs = new ArrayList<>(w08Eggs().entrySet());
        BlockPos floor = helper.absolutePos(new BlockPos(6, 1, 6));
        AABB around = new AABB(floor).move(0.0, 1.0, 0.0).inflate(1.0);
        List<String> failures = new ArrayList<>();
        for (Map.Entry<DeferredItem<ItemSpawnEgg>, Supplier<? extends EntityType<?>>> c : eggs) {
            String id = c.getKey().getId().getPath();
            EntityType<?> type = c.getValue().get();
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(c.getKey().get()));
            ItemStack stack = player.getMainHandItem();
            AABB search = around.inflate(8.0);
            Set<Entity> before = new HashSet<>(helper.getLevel().getEntities((Entity) null, search, e -> e.getType() == type));
            InteractionResult result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(floor).add(0.0, 0.5, 0.0), Direction.UP, floor, false)));
            List<Entity> spawned = helper.getLevel().getEntities((Entity) null, search, e -> e.getType() == type && !before.contains(e));
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
        helper.assertTrue(eggs.size() == 21, "expected 21 W08 eggs, listed " + eggs.size());
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }

    /** R20: RatSword extends ItemSword (six tags), ItemSquidZooka extends Item with durability (two tags); breathing tag. */
    @GameTest(template = ARENA, batch = WIRING)
    public static void w08TagsAreInPlace(GameTestHelper helper) {
        ItemStack sword = new ItemStack(ModItems.RAT_SWORD.get());
        for (TagKey<Item> tag : List.of(ItemTags.SWORD_ENCHANTABLE, ItemTags.SHARP_WEAPON_ENCHANTABLE, ItemTags.WEAPON_ENCHANTABLE,
                ItemTags.FIRE_ASPECT_ENCHANTABLE, ItemTags.DURABILITY_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE)) {
            helper.assertTrue(sword.is(tag), "ratsword is missing from #" + tag.location());
        }
        ItemStack zooka = new ItemStack(ModItems.SQUID_ZOOKA.get());
        helper.assertTrue(zooka.is(ItemTags.DURABILITY_ENCHANTABLE) && zooka.is(ItemTags.VANISHING_ENCHANTABLE)
                && !zooka.is(ItemTags.SWORD_ENCHANTABLE), "squidzookasmall tags");
        for (EntityType<?> type : List.of(ModEntities.CLOUD_SHARK.get(), ModEntities.SEA_MONSTER.get(), ModEntities.SEA_VIPER.get(),
                ModEntities.SKATE.get(), ModEntities.IRUKANDJI.get(), ModEntities.CRAB.get())) {
            helper.assertTrue(type.is(EntityTypeTags.CAN_BREATHE_UNDER_WATER), type.toShortString() + " cannot breathe under water");
        }
        helper.assertFalse(ModEntities.ATTACK_SQUID.get().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER), "attack_squid must drown (no override)");
        helper.assertTrue(ModBlocks.ISLAND.get().asItem() == ModItems.ISLAND.get(), "island block item");
        helper.succeed();
    }

    /**
     * InkSack.java:58-60: an ink sack hitting an Attack Squid returns before the damage. Counter-check in the same test: the
     * same drop onto a cow hurts it, so an unchanged squid is not just a miss.
     */
    @GameTest(template = ARENA, timeoutTicks = 60, batch = WIRING)
    public static void inkSackDoesNotHurtAnAttackSquid(GameTestHelper helper) {
        AttackSquid squid = helper.spawn(ModEntities.ATTACK_SQUID.get(), new BlockPos(3, 2, 6));
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(9, 2, 6));
        squid.setNoAi(true);
        cow.setNoAi(true);
        float squidBefore = squid.getHealth();
        float cowBefore = cow.getHealth();
        for (LivingEntity target : List.of(squid, cow)) {
            InkSack ink = new InkSack(helper.getLevel(), target.getX(), target.getY() + target.getBbHeight() + 1.0, target.getZ());
            ink.setDeltaMovement(0.0, -0.6, 0.0);
            helper.getLevel().addFreshEntity(ink);
        }
        helper.runAfterDelay(20, () -> {
            float squidAfter = squid.getHealth();
            float cowAfter = cow.getHealth();
            clearMobsAround(helper);
            helper.assertTrue(cowAfter < cowBefore, "counter-check: the ink sack did not hurt the cow (" + cowAfter + ")");
            helper.assertTrue(squidAfter == squidBefore, "attack squid health " + squidAfter + ", expected " + squidBefore);
            helper.succeed();
        });
    }
}
