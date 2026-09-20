package com.swbr.orespawn.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;

/**
 * Port of the 1.7.10 vanilla {@code EntityAILookIdle} ({@code vb} in {@code reference/jar/mcp/client-1.7.10.jar},
 * disassembled). 1.21.1's {@link RandomLookAroundGoal} is the same goal: chance 0.02, a random direction from
 * {@code 2 pi * nextDouble}, 20..39 ticks counted down before each look, mutex 3 (move and look).
 *
 * <p>PORT: the 0.02 chance is rolled once per 1.7.10 {@code shouldExecute} pass (every third tick) through
 * {@link LegacyAiTick}; 1.21.1 asks {@code canUse} every second tick and would look around 1.5 times as often
 * (W01 rule for goals with a chance per evaluation). Moved here from {@code Coin} so every W06 user shares it.
 */
public class EntityAILookIdle extends RandomLookAroundGoal {

    private final Mob idleEntity;
    private final LegacyAiTick legacyTick = new LegacyAiTick();

    public EntityAILookIdle(final Mob par1EntityLiving) {
        super(par1EntityLiving);
        this.idleEntity = par1EntityLiving;
    }

    @Override
    public boolean canUse() {
        // LegacyAiTick contract: take the due evaluations before anything can return.
        for (int rolls = this.legacyTick.dueRolls(this.idleEntity); rolls > 0; --rolls) {
            if (super.canUse()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void stop() {
        super.stop();
        this.legacyTick.skipWhileRunning(this.idleEntity);
    }
}
