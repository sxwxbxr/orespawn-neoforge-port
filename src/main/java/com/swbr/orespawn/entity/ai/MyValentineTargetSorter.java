package com.swbr.orespawn.entity.ai;

import java.util.Comparator;
import net.minecraft.world.entity.Entity;

/**
 * Port of {@code danger.orespawn.MyValentineTargetSorter}: pure squared distance, ascending, no
 * creeper or size weighting (verhalten/core-02.md).
 *
 * <p>The 1.7.10 class was a raw {@code Comparator} that only ever compared entities.
 */
public class MyValentineTargetSorter implements Comparator<Entity> {

    private final Entity theEntity;
    /** Kept like the original (MyValentineTargetSorter.java:9); never read. */
    final MyValentineTarget parent;

    public MyValentineTargetSorter(final MyValentineTarget par1EntityAINearestAttackableTarget, final Entity par2Entity) {
        this.parent = par1EntityAINearestAttackableTarget;
        this.theEntity = par2Entity;
    }

    /** MyValentineTargetSorter.java:16-20. */
    public int compareDistanceSq(final Entity par1Entity, final Entity par2Entity) {
        final double var3 = this.theEntity.distanceToSqr(par1Entity);
        final double var4 = this.theEntity.distanceToSqr(par2Entity);
        return (var3 < var4) ? -1 : ((var3 > var4) ? 1 : 0);
    }

    @Override
    public int compare(final Entity par1Obj, final Entity par2Obj) {
        return this.compareDistanceSq(par1Obj, par2Obj);
    }
}
