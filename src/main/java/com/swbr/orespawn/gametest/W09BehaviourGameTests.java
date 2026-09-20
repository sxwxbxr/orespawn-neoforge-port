package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.pet.RubberDucky;
import com.swbr.orespawn.entity.waterdragon.WaterDragon;
import com.swbr.orespawn.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * W09 behaviour tests the porters proposed (docs/port/W09.md, "nicht umgesetzt") that the W09 test wave left open.
 *
 * <p>Already covered in {@link W09GameTests} and therefore not repeated here: taming with the original item and mounting
 * plus climbing through the W01 rider payload (Dragon, Leon, Cephadrome), the Water Dragon's missing mount branch, the
 * Dragon/Spyro diamond transform with owner transfer, and the Spider Robot Kit / Wrench round trip with stored health.
 *
 * <p>Here: the Water Dragon dries out away from water (WaterDragon.java updateAITasks :590-619), and the Rubber Ducky
 * comes back when a player kills it (RubberDucky.java attackEntityFrom :117-146).
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W09BehaviourGameTests {

    private static final String ARENA = "arena";

    private W09BehaviourGameTests() {}

    private static void clearMobsAround(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(Entity.class, helper.getBounds().inflate(64.0), entity -> !(entity instanceof Player))
                .forEach(Entity::discard);
    }

    // ------------------------------------------------------------------ Water Dragon dries out

    /** Glass floor of the raised cage grid (relative y); the dragons stand one block above it. */
    private static final int PLATFORM_Y = 20;
    /** Cage centres on x and z: interiors 1..3, 5..7, 9..11 between glass walls on 0, 4, 8, 12. */
    private static final int[] CAGE_CENTRES = {2, 6, 10};

    /**
     * Glass floor x/z 0..12 on {@link #PLATFORM_Y}, walls two blocks high on every x or z of 0, 4, 8 and 12.
     *
     * <p>Why raised: the Water Dragon's water search reaches 11 blocks on x/z and 10 on y (WaterDragon.java :596-607).
     * From a cage near the arena edge that reaches the neighbour arenas of earlier batches, which the structure grid keeps
     * ({@code StructureGridSpawner(pos, 8, false)}) with their pools still filled - the cause of the Attack Squid flake in
     * the W09 run. Twenty blocks up, y 10..31 lies above every template (arena 11, arena_large 12 high).
     */
    private static void cageGrid(GameTestHelper helper, boolean place) {
        for (int x = 0; x <= 12; ++x) {
            for (int z = 0; z <= 12; ++z) {
                helper.setBlock(new BlockPos(x, PLATFORM_Y, z), place ? Blocks.GLASS : Blocks.AIR);
                if (x % 4 == 0 || z % 4 == 0) {
                    helper.setBlock(new BlockPos(x, PLATFORM_Y + 1, z), place ? Blocks.GLASS : Blocks.AIR);
                    helper.setBlock(new BlockPos(x, PLATFORM_Y + 2, z), place ? Blocks.GLASS : Blocks.AIR);
                }
            }
        }
    }

    /** {@code Blocks.WATER} (the scan's test) the caged dragons can reach: cells 1..11 +-11, origin y 20..21 +-10. */
    private static int waterInDragonReach(GameTestHelper helper, boolean clear) {
        int count = 0;
        for (BlockPos p : BlockPos.betweenClosed(-10, PLATFORM_Y - 11, -10, 22, PLATFORM_Y + 12, 22)) {
            if (helper.getBlockState(p).is(Blocks.WATER)) {
                ++count;
                if (clear) {
                    helper.setBlock(p.immutable(), Blocks.AIR);
                }
            }
        }
        return count;
    }

    /**
     * WaterDragon.java updateAITasks (:590-619): out of water, not sitting, with 1 in 25 a tick it searches water in shells
     * up to 11 and, finding none, loses one health point with 1 in 50 ({@code heal(-1)}, i.e. {@code setHealth}, catalogue
     * 6.3). That is 1 in 1250 per dragon per tick - too slow for one dragon, so nine caged dragons run in parallel: the
     * first loss is expected after about 140 ticks, and none in 2400 ticks has a chance of e^-17.
     */
    @GameTest(template = ARENA, timeoutTicks = 2400, batch = "w09b_water_dragon_dry_out")
    public static void waterDragonDriesOutOnLand(GameTestHelper helper) {
        int strayWater = waterInDragonReach(helper, true);
        cageGrid(helper, true);
        List<WaterDragon> dragons = new ArrayList<>();
        for (int cx : CAGE_CENTRES) {
            for (int cz : CAGE_CENTRES) {
                WaterDragon wd = helper.spawn(ModEntities.WATER_DRAGON.get(), new BlockPos(cx, PLATFORM_Y + 1, cz));
                wd.setPersistenceRequired();
                dragons.add(wd);
            }
        }
        float max = dragons.get(0).getMaxHealth();
        helper.succeedWhen(() -> {
            WaterDragon dried = null;
            for (WaterDragon wd : dragons) {
                if (wd.getHealth() < max || wd.isRemoved()) {
                    dried = wd;
                    break;
                }
            }
            helper.assertTrue(dried != null, "none of the " + dragons.size() + " caged Water Dragons lost health after "
                    + dragons.get(0).tickCount + " ticks on land (" + waterInDragonReach(helper, false) + " water blocks in reach now, "
                    + strayWater + " removed before the start; sitting " + dragons.get(0).isSitting() + ", in water "
                    + dragons.get(0).isInWater() + ")");
            helper.assertTrue(dried.getLastDamageSource() == null,
                    "the Water Dragon lost health through damage, not drying out: " + dried.getLastDamageSource());
            helper.assertTrue(dried.getHealth() >= max - 2.0f || dried.isRemoved(),
                    "a Water Dragon lost " + (max - dried.getHealth()) + " health at once, drying out takes one point per loss");
            clearMobsAround(helper);
            cageGrid(helper, false);
        });
    }

    // ------------------------------------------------------------------ Rubber Ducky respawn

    /** Rubber Duckies other than {@code except} within the test area. */
    private static List<RubberDucky> otherDuckies(GameTestHelper helper, RubberDucky except) {
        return helper.getLevel().getEntitiesOfClass(RubberDucky.class, helper.getBounds().inflate(8.0), d -> d != except);
    }

    /**
     * RubberDucky.java attackEntityFrom (:117-146): killed by a player, the ducky counts {@code killcount + 1} and, below
     * 10, spawns a new Rubber Ducky carrying that count on the first cell of 20 tries within +-2 that has air above a
     * solid block (y +3..-2), at {@code x + i + 1}. On the arena's stone floor (relative y 1) the first try hits at
     * {@code j = -1}: the new ducky stands on the floor at y 2, x within -1..+3 and z within +-2 of the dead one.
     */
    @GameTest(template = ARENA, batch = "w09b_rubber_ducky_respawn")
    public static void rubberDuckyKilledByAPlayerComesBackWithKillcountPlusOne(GameTestHelper helper) {
        RubberDucky ducky = helper.spawn(ModEntities.RUBBER_DUCKY.get(), new BlockPos(6, 2, 6));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        int bx = Mth.floor(ducky.getX());
        int by = Mth.floor(ducky.getY());
        int bz = Mth.floor(ducky.getZ());
        ducky.hurt(helper.getLevel().damageSources().playerAttack(player), 100.0f);
        boolean dead = ducky.getHealth() <= 0.0f || ducky.isRemoved();
        int killcount = ducky.getKillCount();
        List<RubberDucky> others = otherDuckies(helper, ducky);
        clearMobsAround(helper);
        helper.assertTrue(dead, "100 player damage did not kill the Rubber Ducky (health " + ducky.getHealth() + ")");
        helper.assertTrue(killcount == 1, "the dead ducky's killcount is " + killcount + ", expected 1");
        helper.assertTrue(others.size() == 1, "a player kill spawned " + others.size() + " Rubber Duckies, expected 1");
        RubberDucky next = others.get(0);
        helper.assertTrue(next.getKillCount() == 1, "the new ducky carries killcount " + next.getKillCount() + ", expected 1");
        int nx = Mth.floor(next.getX()) - bx;
        int ny = Mth.floor(next.getY()) - by;
        int nz = Mth.floor(next.getZ()) - bz;
        helper.assertTrue(nx >= -1 && nx <= 3 && nz >= -2 && nz <= 2 && ny == 0,
                "the new ducky is at offset " + nx + "/" + ny + "/" + nz + ", expected x -1..3, y 0, z -2..2");
        helper.succeed();
    }

    /** RubberDucky.java :121-122: the tenth player kill ({@code killcount} 9 -> 10) is final. */
    @GameTest(template = ARENA, batch = "w09b_rubber_ducky_tenth_kill")
    public static void rubberDuckyStaysDeadAfterTheTenthKill(GameTestHelper helper) {
        RubberDucky ducky = helper.spawn(ModEntities.RUBBER_DUCKY.get(), new BlockPos(6, 2, 6));
        ducky.setKillCount(9);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ducky.hurt(helper.getLevel().damageSources().playerAttack(player), 100.0f);
        boolean dead = ducky.getHealth() <= 0.0f || ducky.isRemoved();
        int killcount = ducky.getKillCount();
        int others = otherDuckies(helper, ducky).size();
        clearMobsAround(helper);
        helper.assertTrue(dead, "100 player damage did not kill the Rubber Ducky");
        helper.assertTrue(killcount == 10, "killcount " + killcount + ", expected 10");
        helper.assertTrue(others == 0, "the tenth kill still spawned " + others + " Rubber Duckies");
        helper.succeed();
    }

    /** RubberDucky.java :120: only a player kill counts ({@code w instanceof EntityPlayer}); other deaths stay final. */
    @GameTest(template = ARENA, batch = "w09b_rubber_ducky_no_player")
    public static void rubberDuckyKilledWithoutAPlayerStaysDead(GameTestHelper helper) {
        RubberDucky ducky = helper.spawn(ModEntities.RUBBER_DUCKY.get(), new BlockPos(6, 2, 6));
        ducky.hurt(helper.getLevel().damageSources().generic(), 100.0f);
        boolean dead = ducky.getHealth() <= 0.0f || ducky.isRemoved();
        int killcount = ducky.getKillCount();
        int others = otherDuckies(helper, ducky).size();
        clearMobsAround(helper);
        helper.assertTrue(dead, "100 generic damage did not kill the Rubber Ducky");
        helper.assertTrue(killcount == 0, "killcount " + killcount + " after a non-player death, expected 0");
        helper.assertTrue(others == 0, "a non-player death spawned " + others + " Rubber Duckies");
        helper.succeed();
    }
}
