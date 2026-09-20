package com.swbr.orespawn.entity.portal;

import com.swbr.orespawn.entity.ai.LegacyAiTick;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * Port of the vanilla 1.7.10 goal {@code net.minecraft.entity.ai.EntityAINearestAttackableTarget}
 * ({@code vo} in client-1.7.10.jar, disassembled) in its {@code (creature, class, chance, checkSight)}
 * form, used by {@link EntityRedAnt} (chance 4) and {@link Termite} (chance 6). Not OreSpawn's own
 * {@code MyEntityAINearestAttackableTarget}. No original OreSpawn class; it lives here because this wave
 * is the first user.
 *
 * <p>{@code shouldExecute} ({@code vo.a}): {@code rand.nextInt(chance) != 0} gives up; otherwise every
 * entity of the class in {@code boundingBox.expand(followRange, 4, followRange)} that passes
 * {@code isSuitableTarget}, sorted by distance, the nearest one wins.
 *
 * <p>PORT: two differences of 1.21.1's {@link NearestAttackableTargetGoal} are undone. Its random
 * interval runs every second tick after {@code reducedTickDelay}; here the chance is rolled at the
 * 1.7.10 cadence of one evaluation per three ticks through {@link LegacyAiTick}. And for players it
 * picks the nearest within the follow range in all directions with visibility scaling; here the 1.7.10
 * box with 4 blocks up and down is searched, without a range test inside it.
 */
public class EntityAINearestAttackableTarget<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    private final Class<T> targetClass;
    private final int targetChance;
    private final LegacyAiTick legacyTick = new LegacyAiTick();

    public EntityAINearestAttackableTarget(final Mob par1EntityCreature, final Class<T> par2Class, final int par3,
                                           final boolean par4) {
        super(par1EntityCreature, par2Class, 0, par4, false, null);
        this.targetClass = par2Class;
        this.targetChance = par3;
        // isSuitableTarget: alive, attackable, not a creative player, in sight when checkSight is set.
        final TargetingConditions conditions = TargetingConditions.forCombat();
        this.targetConditions = par4 ? conditions : conditions.ignoreLineOfSight();
    }

    @Override
    public boolean canUse() {
        final int rolls = this.legacyTick.dueRolls(this.mob);
        if (rolls == 0) {
            return false;
        }
        if (this.targetChance > 0) {
            boolean hit = false;
            for (int left = rolls; left > 0; --left) {
                if (this.mob.getRandom().nextInt(this.targetChance) == 0) {
                    hit = true;
                }
            }
            if (!hit) {
                return false;
            }
        }
        this.findTarget();
        return this.target != null;
    }

    /** The 1.7.10 box and the distance sorter ({@code EntityAINearestAttackableTargetSorter}): first nearest wins. */
    @Override
    protected void findTarget() {
        final double d0 = this.getFollowDistance();
        final List<T> var3 = this.mob.level().getEntitiesOfClass(this.targetClass, this.getTargetSearchArea(d0),
                candidate -> this.targetConditions.test(this.mob, candidate));
        T nearest = null;
        double best = 0.0;
        for (final T candidate : var3) {
            final double d = this.mob.distanceToSqr(candidate);
            if (nearest == null || d < best) {
                nearest = candidate;
                best = d;
            }
        }
        this.target = nearest;
    }

    @Override
    public void stop() {
        super.stop();
        this.legacyTick.skipWhileRunning(this.mob);
    }
}
