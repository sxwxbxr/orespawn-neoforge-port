package com.swbr.orespawn.gametest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.ai.EntityAIAvoidEntity;
import com.swbr.orespawn.entity.ai.EntityAILookIdle;
import com.swbr.orespawn.entity.ai.EntityAITempt;
import com.swbr.orespawn.entity.ai.EntityAIWatchClosest;
import com.swbr.orespawn.entity.companion.Boyfriend;
import com.swbr.orespawn.entity.companion.Girlfriend;
import com.swbr.orespawn.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * W07 catch-up of the W06 fix wave (docs/port/W07.md, section "W06-Nachzug"): the W01-W06 mobs that still registered
 * 1.21.1 vanilla goals where the original used a 1.7.10 vanilla task.
 *
 * <p>Girlfriend and Boyfriend (W04) had {@code TemptGoal}, {@code LookAtPlayerGoal} and {@code RandomLookAroundGoal};
 * Flounder had {@code AvoidEntityGoal}; Flounder, Whale, Frog and RedCow had an inline {@code PanicGoal} that restored
 * the 1.7.10 trigger but kept 1.21.1's run to water.
 */
@GameTestHolder(OreSpawn.MOD_ID)
@PrefixGameTestTemplate(false)
public final class W07CrosscutGameTests {

    private static final String ARENA = "arena";

    private W07CrosscutGameTests() {}

    /**
     * Spawns with the registered goals intact but without AI ticks. Not {@code helper.spawnWithNoFreeWill}: that calls
     * {@code Mob.removeFreeWill}, which removes every goal ({@code removeAllGoals(goal -> true)}, Mob.java:1557-1559), so
     * a test that inspects the registered goals would never find one. {@code setNoAi(true)} makes
     * {@code isEffectiveAi()} false and {@code serverAiStep} (goal and target selectors) is skipped - the same set-up as
     * W05GameTests.
     */
    private static <E extends Mob> E spawnWithoutAi(final GameTestHelper helper, final EntityType<E> type, final BlockPos pos) {
        final E mob = helper.spawn(type, pos);
        mob.setNoAi(true);
        return mob;
    }

    @Nullable
    private static Goal goalAt(final Mob mob, final int priority, final Class<? extends Goal> type) {
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            if (wrapped.getPriority() == priority && type.isInstance(wrapped.getGoal())) {
                return wrapped.getGoal();
            }
        }
        return null;
    }

    /**
     * The 1.21.1 goals that have a 1.7.10 port in {@code entity.ai}. Exact classes: {@link EntityAILookIdle} extends
     * {@link RandomLookAroundGoal} and must not count.
     */
    private static boolean isVanillaGoalWithALegacyPort(final Goal goal) {
        final Class<?> type = goal.getClass();
        return type == TemptGoal.class || type == LookAtPlayerGoal.class || type == RandomLookAroundGoal.class
                || type == RandomStrollGoal.class || type == AvoidEntityGoal.class;
    }

    // ------------------------------------------------------------------ tempt cadence of the companions

    /**
     * Girlfriend.java:128-137 / Boyfriend.java:106-113: tempt at 2, watch closest at 7, look idle at 9 are the 1.7.10
     * ports, and the tempt goal the entity registered calms down for 100 evaluations on the 1.7.10 cadence - ready
     * exactly 300 ticks after its reset, as {@code W06GameTests.temptCalmsDownForAHundredEvaluationsOnTheLegacyCadence}
     * proves for the cow (1.21.1 {@code TemptGoal}: 100). The goal is driven by hand the way {@code GoalSelector} drives
     * it. Then the off hand: 1.7.10 read the held item only. {@code otherBait} proves the item match of the original
     * ({@code Item.getItemFromBlock(Blocks.red_flower)} covered every metadata).
     */
    @SuppressWarnings("removal")
    private static void temptCalmsDownOnTheLegacyCadence(final GameTestHelper helper, final Mob mob, final String name,
                                                         final Item bait, final Item otherBait) {
        final Goal found = goalAt(mob, 2, EntityAITempt.class);
        helper.assertTrue(found != null, name + " has no EntityAITempt at priority 2");
        helper.assertTrue(goalAt(mob, 7, EntityAIWatchClosest.class) != null, name + " has no EntityAIWatchClosest at priority 7");
        helper.assertTrue(goalAt(mob, 9, EntityAILookIdle.class) != null, name + " has no EntityAILookIdle at priority 9");
        for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
            helper.assertFalse(isVanillaGoalWithALegacyPort(wrapped.getGoal()), name + " still registers "
                    + wrapped.getGoal().getClass().getSimpleName() + " at priority " + wrapped.getPriority());
        }
        final EntityAITempt tempt = (EntityAITempt) found;
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.moveTo(mob.getX() + 1.0, mob.getY(), mob.getZ(), 0.0F, 0.0F);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bait));
            final int start = 3000;
            mob.tickCount = start;
            tempt.stop();
            int readyAt = -1;
            for (int t = start; t <= start + 400; t += 2) {
                mob.tickCount = t;
                if (tempt.canUse()) {
                    readyAt = t;
                    break;
                }
            }
            helper.assertTrue(readyAt == start + 300, name + "'s tempt goal was ready "
                    + (readyAt < 0 ? "not within 400 ticks" : (readyAt - start) + " ticks") + " after its reset, expected 300");

            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(bait));
            mob.tickCount = readyAt + 2;
            tempt.canUse();
            mob.tickCount = readyAt + 4;
            helper.assertFalse(tempt.canUse(), "bait in the off hand tempted the " + name + "; EntityAITempt read the held item only");
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(otherBait));
            mob.tickCount = readyAt + 6;
            helper.assertTrue(tempt.canUse(), otherBait + " in the main hand did not tempt the " + name + " after the calm-down");
        } finally {
            player.remove(Entity.RemovalReason.DISCARDED);
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void girlfriendTemptCalmsDownForAHundredEvaluationsOnTheLegacyCadence(final GameTestHelper helper) {
        final Girlfriend girlfriend = spawnWithoutAi(helper, ModEntities.GIRLFRIEND.get(), new BlockPos(6, 2, 6));
        temptCalmsDownOnTheLegacyCadence(helper, girlfriend, "girlfriend", Items.POPPY, Items.OXEYE_DAISY);
    }

    @GameTest(template = ARENA)
    public static void boyfriendTemptCalmsDownForAHundredEvaluationsOnTheLegacyCadence(final GameTestHelper helper) {
        final Boyfriend boyfriend = spawnWithoutAi(helper, ModEntities.BOYFRIEND.get(), new BlockPos(6, 2, 6));
        temptCalmsDownOnTheLegacyCadence(helper, boyfriend, "boyfriend", Items.COOKED_BEEF, Items.COOKED_BEEF);
    }

    // ------------------------------------------------------------------ EntityAIPanic never runs to water

    /**
     * 1.7.10 {@code EntityAIPanic.shouldExecute} ({@code uz.a}, disassembled): revenge target or burning, then
     * {@code RandomPositionGenerator.findRandomTarget(entity, 5, 4)} - no water search. 1.21.1 {@code PanicGoal.canUse}
     * runs a burning mob to water within 5 blocks before it looks for a random spot.
     *
     * <p>The mob is restricted to radius 0, so {@code DefaultRandomPos.getPos} rejects every spot ({@code isRestricted})
     * and only the water search can make the goal start. A burning mob with water three blocks away: the registered
     * panic goal must not start. Control against "silence is no proof": the former inline goal (1.21.1 water search
     * plus the 1.7.10 trigger) starts under the same conditions.
     */
    private static <E extends PathfinderMob> void burningPanicDoesNotRunToWater(final GameTestHelper helper,
                                                                              final EntityType<E> type, final int priority) {
        final E mob = spawnWithoutAi(helper, type, new BlockPos(4, 2, 6));
        helper.setBlock(new BlockPos(7, 2, 6), Blocks.WATER);
        try {
            burningPanicChecks(helper, type, mob, priority);
        } finally {
            // The checks run within one tick. Remove the source and the mob again: a water source left in an open arena
            // flows over the edge for the rest of the batch, and the test must leave nothing behind for later batches.
            helper.setBlock(new BlockPos(7, 2, 6), Blocks.AIR);
            mob.discard();
        }
        helper.succeed();
    }

    private static <E extends PathfinderMob> void burningPanicChecks(final GameTestHelper helper, final EntityType<E> type,
                                                                     final E mob, final int priority) {
        final String name = type.toShortString();
        final Goal found = goalAt(mob, priority, PanicGoal.class);
        helper.assertTrue(found != null, name + " has no panic goal at priority " + priority);
        mob.restrictTo(mob.blockPosition(), 0);
        mob.setRemainingFireTicks(100);
        helper.assertTrue(mob.isOnFire(), name + " is not burning; the test cannot trigger the panic");
        helper.assertTrue(mob.getLastHurtByMob() == null, name + " has a revenge target");
        final PanicGoal formerInlineGoal = new PanicGoal(mob, 1.0) {
            @Override
            protected boolean shouldPanic() {
                return this.mob.getLastHurtByMob() != null || this.mob.isOnFire();
            }
        };
        helper.assertTrue(formerInlineGoal.canUse(), "control: 1.21.1's water search did not find the water next to the "
                + name + ", the test would prove nothing");
        helper.assertFalse(found.canUse(), name + "'s panic goal ran a burning mob to water; 1.7.10 EntityAIPanic only picks a random spot");
    }

    @GameTest(template = ARENA)
    public static void burningRedCowDoesNotPanicTowardsWater(final GameTestHelper helper) {
        burningPanicDoesNotRunToWater(helper, ModEntities.APPLE_COW.get(), 1);
    }

    @GameTest(template = ARENA)
    public static void burningWhaleDoesNotPanicTowardsWater(final GameTestHelper helper) {
        burningPanicDoesNotRunToWater(helper, ModEntities.WHALE.get(), 4);
    }

    @GameTest(template = ARENA)
    public static void burningFlounderDoesNotPanicTowardsWater(final GameTestHelper helper) {
        burningPanicDoesNotRunToWater(helper, ModEntities.FLOUNDER.get(), 4);
    }

    @GameTest(template = ARENA)
    public static void burningFrogDoesNotPanicTowardsWater(final GameTestHelper helper) {
        burningPanicDoesNotRunToWater(helper, ModEntities.FROG.get(), 1);
    }

    // ------------------------------------------------------------------ no vanilla goal with a legacy port anywhere

    /**
     * Flounder.java:35: {@code EntityAIAvoidEntity(EntityPlayer, 8.0, 1.0, 1.4)} is the 1.7.10 port at priority 3.
     * Then every OreSpawn mob type: none registers {@code TemptGoal}, {@code LookAtPlayerGoal},
     * {@code RandomLookAroundGoal}, {@code RandomStrollGoal} or {@code AvoidEntityGoal} directly, because each has a
     * 1.7.10 port in {@code entity.ai} and every OreSpawn mob is a 1.7.10 original. The entities are created, not
     * added to the level; goals are registered in the {@code Mob} constructor on the server.
     */
    @GameTest(template = ARENA)
    public static void noOreSpawnMobRegistersAVanillaGoalThatHasALegacyPort(final GameTestHelper helper) {
        final Mob flounder = spawnWithoutAi(helper, ModEntities.FLOUNDER.get(), new BlockPos(6, 2, 6));
        helper.assertTrue(goalAt(flounder, 3, EntityAIAvoidEntity.class) != null, "flounder has no EntityAIAvoidEntity at priority 3");

        final List<String> failures = new ArrayList<>();
        int mobs = 0;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            final ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id == null || !OreSpawn.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            final Entity entity;
            try {
                entity = type.create(helper.getLevel());
            } catch (RuntimeException e) {
                failures.add(id + ": constructor threw " + e);
                continue;
            }
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            ++mobs;
            for (WrappedGoal wrapped : mob.goalSelector.getAvailableGoals()) {
                if (isVanillaGoalWithALegacyPort(wrapped.getGoal())) {
                    failures.add(id + ": " + wrapped.getGoal().getClass().getSimpleName() + " at priority " + wrapped.getPriority());
                }
            }
        }
        OreSpawn.LOG.info("W07 crosscut: inspected the goals of {} OreSpawn mob types", mobs);
        helper.assertTrue(mobs > 0, "no OreSpawn mob type found in the registry");
        helper.assertTrue(failures.isEmpty(), String.join("; ", failures));
        helper.succeed();
    }
}
