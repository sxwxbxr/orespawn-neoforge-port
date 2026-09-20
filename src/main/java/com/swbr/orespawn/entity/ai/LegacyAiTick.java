package com.swbr.orespawn.entity.ai;

import net.minecraft.world.entity.Mob;

/**
 * Reproduces the cadence at which 1.7.10 evaluated {@code shouldExecute()}.
 *
 * <p>In 1.7.10 {@code EntityAITasks.onUpdateTasks()} asked idle tasks {@code shouldExecute()} only
 * every third tick: {@code tickRate = 3} and {@code if (tickCount++ % tickRate == 0)} (verified in
 * {@code reference/jar/mcp/client-1.7.10.jar}, class {@code uj}, field {@code f}, via joined.srg
 * {@code field_75779_e = tickRate}). A goal that rolls {@code nextInt(90)} therefore fired on average
 * every 270 ticks, and one rolling {@code nextInt(30)} every 90 ticks.
 *
 * <p>1.21.1 calls {@link net.minecraft.world.entity.ai.goal.Goal#canUse()} every second tick instead
 * ({@code Mob.serverAiStep}, Mob.java:779-792: {@code (tickCount + id) % 2}, then
 * {@code GoalSelector.tick()}). A goal that keeps the original chance would fire 1.5 times as often.
 * Vanilla papers over this with {@code reducedTickDelay}, which halves the divisor and is not 1:1
 * either.
 *
 * <p>This helper counts how many multiples of three the mob's {@link Mob#tickCount} has crossed since
 * the goal last looked, so the goal can roll its chance exactly that many times. Two kinds of ticks
 * must not count, because 1.7.10 did not ask {@code shouldExecute()} in them either:
 *
 * <ul>
 * <li><b>Ticks the goal spent running.</b> Call {@link #skipWhileRunning(Mob)} from {@code stop()}.
 * <li><b>Ticks the goal was blocked.</b> 1.7.10 ran {@code canUse(entry) && shouldExecute()}, so a
 * task whose mutex clashed with a running higher-priority task was never asked. 1.21.1 does the same
 * (GoalSelector.java:98-101: {@code goalCanBeReplacedForAllFlags(...) && canUse()}, and nothing at
 * all while a flag is disabled, e.g. {@code MOVE} for a mob steered by another mob, Mob.java:1401)
 * - but the tick counter keeps running. The helper detects such a stretch by the gap since the last
 * call: normal cadence is at most {@link #SELECTOR_PERIOD} ticks, a larger gap means at least one
 * selector pass skipped this goal. The backlog is then dropped and only the current tick counts,
 * which is what 1.7.10 did when the blocking task reset: the next {@code % 3 == 0} pass on or after
 * the reset tick asked {@code shouldExecute()} again.
 * </ul>
 *
 * <p>Ticks in which the goal <em>was</em> asked but returned before its roll (the {@code busy} switch
 * of {@code MyEntityAIWanderALot}) are the goal's own business: it must call {@link #dueRolls(Mob)}
 * before that early return so the ticks are consumed without a roll, as the original consumed them.
 *
 * <p>Reusable by every later goal that ports a per-evaluation chance (MyEntityAITarget and friends in
 * W04).
 */
public final class LegacyAiTick {

    /** {@code EntityAITasks.tickRate} in 1.7.10. */
    public static final int TICK_RATE = 3;

    /**
     * Largest gap in ticks between two {@code canUse()} calls of a goal that is neither running nor
     * blocked: {@code GoalSelector.tick()} runs every second tick (Mob.java:780).
     */
    public static final int SELECTOR_PERIOD = 2;

    /** The last tick already accounted for; {@code -1} so that tick 0 rolls, like the original. */
    private int lastSeenTick = -1;

    /** The tick of the last {@link #dueRolls} call, to tell a blocked stretch from normal cadence. */
    private int lastCallTick = -1;

    /**
     * Number of 1.7.10 {@code shouldExecute()} evaluations due since the last call: the multiples of
     * {@link #TICK_RATE} in {@code (lastSeenTick, mob.tickCount]}, or in the current tick alone when
     * the goal was blocked in between.
     */
    public int dueRolls(final Mob mob) {
        return this.dueRolls(mob.tickCount);
    }

    /**
     * {@link #dueRolls(Mob)} on a bare tick counter, so the arithmetic can be unit-tested without
     * Minecraft on the classpath.
     */
    public int dueRolls(final int now) {
        final int sinceLastCall = now - this.lastCallTick;
        this.lastCallTick = now;
        if (now <= this.lastSeenTick) {
            // Tick counter went backwards (should not happen; tickCount is not persisted, the goal
            // object dies with its entity). Resync instead of rolling a huge backlog.
            this.lastSeenTick = now;
            return 0;
        }
        if (sinceLastCall > SELECTOR_PERIOD) {
            // At least one selector pass skipped this goal: a higher-priority goal held one of its
            // flags, or the flag was disabled. 1.7.10 asked nothing in those ticks either.
            this.lastSeenTick = now - 1;
        }
        final int rolls = Math.floorDiv(now, TICK_RATE) - Math.floorDiv(this.lastSeenTick, TICK_RATE);
        this.lastSeenTick = now;
        return rolls;
    }

    /**
     * Call from {@code Goal.stop()}. Discards the ticks the goal spent running and re-arms the current
     * tick, because 1.7.10 asked {@code shouldExecute()} again in the very same pass in which it reset
     * a task (EntityAITasks.onUpdateTasks, the {@code % tickRate == 0} branch).
     */
    public void skipWhileRunning(final Mob mob) {
        this.skipWhileRunning(mob.tickCount);
    }

    /** {@link #skipWhileRunning(Mob)} on a bare tick counter, for the unit test. */
    public void skipWhileRunning(final int now) {
        this.lastSeenTick = now - 1;
        this.lastCallTick = now;
    }
}
