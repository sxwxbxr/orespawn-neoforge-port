package com.swbr.orespawn.entity.ai;

import java.util.Comparator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

/**
 * Port of {@code danger.orespawn.GenericTargetSorter}: orders candidate targets by squared distance,
 * with creepers and big creatures counted as closer than they are.
 *
 * <p>Used by 75 entity classes as {@code this.TargetSorter = new GenericTargetSorter(this)} followed
 * by {@code Collections.sort(list, TargetSorter)} (verhalten/core-02.md). The 1.7.10 class was a raw
 * {@code Comparator}; it only ever compared entities, so this one is a {@code Comparator<Entity>}.
 */
public class GenericTargetSorter implements Comparator<Entity> {

    private final Entity theEntity;

    public GenericTargetSorter(final Entity par2Entity) {
        this.theEntity = par2Entity;
    }

    /**
     * GenericTargetSorter.java:15-34. Distance squared, halved for creepers, divided by the hitbox
     * footprint ({@code height * width}) when that exceeds one. Smaller sorts first.
     */
    public int compareDistanceSq(final Entity par1Entity, final Entity par2Entity) {
        double weight = 0.0;
        double var3 = this.theEntity.distanceToSqr(par1Entity);
        if (par1Entity instanceof Creeper) {
            var3 /= 2.0;
        }
        weight = par1Entity.getBbHeight() * par1Entity.getBbWidth();
        if (weight > 1.0) {
            var3 /= weight;
        }
        double var4 = this.theEntity.distanceToSqr(par2Entity);
        if (par2Entity instanceof Creeper) {
            var4 /= 2.0;
        }
        weight = par2Entity.getBbHeight() * par2Entity.getBbWidth();
        if (weight > 1.0) {
            var4 /= weight;
        }
        return (var3 < var4) ? -1 : ((var3 > var4) ? 1 : 0);
    }

    @Override
    public int compare(final Entity par1Obj, final Entity par2Obj) {
        return this.compareDistanceSq(par1Obj, par2Obj);
    }
}
