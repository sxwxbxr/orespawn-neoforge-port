package com.swbr.orespawn.entity.ai;

import java.util.Comparator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Port of {@code danger.orespawn.MyEntityAINearestAttackableTargetSorter}: orders the candidates of
 * {@link MyEntityAINearestAttackableTarget} by squared distance, creepers counted as half as far.
 * No size weighting, unlike {@link GenericTargetSorter} (verhalten/core-02.md).
 *
 * <p>The 1.7.10 class was a raw {@code Comparator} that only ever compared entities.
 */
public class MyEntityAINearestAttackableTargetSorter implements Comparator<Entity> {

    private final Entity theEntity;
    /** Kept like the original (MyEntityAINearestAttackableTargetSorter.java:10); never read. */
    final MyEntityAINearestAttackableTarget parent;

    public MyEntityAINearestAttackableTargetSorter(final MyEntityAINearestAttackableTarget par1EntityAINearestAttackableTarget,
            final Entity par2Entity) {
        this.parent = par1EntityAINearestAttackableTarget;
        this.theEntity = par2Entity;
    }

    /** MyEntityAINearestAttackableTargetSorter.java:17-27. */
    public int compareDistanceSq(final Entity par1Entity, final Entity par2Entity) {
        double var3 = this.theEntity.distanceToSqr(par1Entity);
        if (par1Entity instanceof Creeper) {
            var3 /= 2.0;
        }
        double var4 = this.theEntity.distanceToSqr(par2Entity);
        if (par2Entity instanceof Creeper) {
            var4 /= 2.0;
        }
        return (var3 < var4) ? -1 : ((var3 > var4) ? 1 : 0);
    }

    @Override
    public int compare(final Entity par1Obj, final Entity par2Obj) {
        return this.compareDistanceSq(par1Obj, par2Obj);
    }
}
