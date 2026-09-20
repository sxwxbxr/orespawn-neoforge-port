package com.swbr.orespawn.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * The 1.7.10 cadence of {@code EntityAITasks.onUpdateTasks} (one {@code shouldExecute()} per three
 * ticks, none while running or blocked), checked on a bare tick counter without Minecraft on the
 * classpath. The selector cadence is the 1.21.1 one: {@code canUse()} at tick 0, 1 and then every
 * second tick (Mob.java:780).
 */
class LegacyAiTickTest {

    /** Multiples of three in {@code [0, last]}: what 1.7.10 evaluated over that span. */
    private static int expectedRolls(final int last) {
        return last / 3 + 1;
    }

    /** Sums the rolls of every selector pass in {@code [from, to]}, the way a never-blocked goal sees them. */
    private static int rollsOverSelectorPasses(final LegacyAiTick tick, final int from, final int to) {
        int rolls = 0;
        for (int now = from; now <= to; ++now) {
            if (now <= 1 || now % 2 == 0) {
                rolls += tick.dueRolls(now);
            }
        }
        return rolls;
    }

    @Test
    void normalCadenceRollsOncePerThreeTicks() {
        final LegacyAiTick tick = new LegacyAiTick();
        assertEquals(expectedRolls(300), rollsOverSelectorPasses(tick, 0, 300));
    }

    @Test
    void oddEntityIdShiftsThePassesButNotTheCount() {
        // (tickCount + id) % 2 == 0 with an odd id: passes at 0, 1, 3, 5, ...
        final LegacyAiTick tick = new LegacyAiTick();
        int rolls = 0;
        for (int now = 0; now <= 301; ++now) {
            if (now <= 1 || now % 2 == 1) {
                rolls += tick.dueRolls(now);
            }
        }
        assertEquals(expectedRolls(301), rolls);
    }

    @Test
    void ticksConsumedWhileBusyAreNotDeferred() {
        // MyEntityAIWanderALot calls dueRolls before its busy check, so a 600-tick fight yields the
        // same total as 600 idle ticks: the rolls were spent while busy, not stockpiled.
        final LegacyAiTick tick = new LegacyAiTick();
        rollsOverSelectorPasses(tick, 0, 600);
        assertEquals(0, tick.dueRolls(602), "no backlog after a busy stretch (602 % 3 == 2)");
        assertEquals(expectedRolls(1200) - expectedRolls(602), rollsOverSelectorPasses(tick, 604, 1200));
    }

    @Test
    void blockedStretchRollsOnlyTheCurrentTick() {
        // A follow-owner goal held MOVE from tick 10 to tick 310: canUse() was not called in between.
        final LegacyAiTick tick = new LegacyAiTick();
        rollsOverSelectorPasses(tick, 0, 10);
        // 310 % 3 == 1: the unblock tick is not an evaluation tick, so nothing rolls...
        assertEquals(0, tick.dueRolls(310));
        // ...and the next pass crosses 312, one roll. Cadence resumes from the unblock tick.
        assertEquals(1, tick.dueRolls(312));
        assertEquals(expectedRolls(600) - expectedRolls(312), rollsOverSelectorPasses(tick, 314, 600));
    }

    @Test
    void blockedStretchEndingOnAnEvaluationTickRollsOnce() {
        final LegacyAiTick tick = new LegacyAiTick();
        rollsOverSelectorPasses(tick, 0, 10);
        assertEquals(1, tick.dueRolls(312), "312 % 3 == 0: 1.7.10 asked in that same pass");
    }

    @Test
    void normalGapOfTwoIsNotABlock() {
        final LegacyAiTick tick = new LegacyAiTick();
        assertEquals(1, tick.dueRolls(0));
        assertEquals(0, tick.dueRolls(1));
        assertEquals(1, tick.dueRolls(3), "the gap from 1 to 3 is normal cadence and keeps tick 3");
        assertEquals(0, tick.dueRolls(5));
        assertEquals(1, tick.dueRolls(7), "crosses 6");
    }

    @Test
    void stopReArmsTheCurrentTickOnly() {
        final LegacyAiTick tick = new LegacyAiTick();
        rollsOverSelectorPasses(tick, 0, 10);
        // Ran from tick 10 to 100 (100 % 3 == 1): stop() then canUse() in the same selector pass.
        tick.skipWhileRunning(100);
        assertEquals(0, tick.dueRolls(100));
        assertEquals(1, tick.dueRolls(102));
        // Ran until an evaluation tick: rolls once in the pass that stopped it.
        tick.skipWhileRunning(300);
        assertEquals(1, tick.dueRolls(300));
    }

    @Test
    void tickCounterGoingBackwardsResyncsWithoutRolling() {
        final LegacyAiTick tick = new LegacyAiTick();
        rollsOverSelectorPasses(tick, 0, 30);
        assertEquals(0, tick.dueRolls(12));
        assertEquals(0, tick.dueRolls(14));
        assertEquals(1, tick.dueRolls(16), "crosses 15");
    }
}
