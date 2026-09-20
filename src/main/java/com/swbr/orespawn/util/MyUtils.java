package com.swbr.orespawn.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;

/**
 * Port of {@code danger.orespawn.MyUtils}: the three classification predicates that the target
 * searches of dozens of mobs share.
 *
 * <p>The original tested {@code instanceof} against 31 later entity classes. Those chains are the
 * marker interfaces {@link Royalty}, {@link AttackableNonMob} and {@link Ignoreable}; every entity the
 * original named implements the matching one, and subclasses inherit it exactly like the original
 * {@code instanceof} did (catalogue README 4.1, point 1). The vanilla members of the chains stay
 * class checks here: {@code EntityMob} is {@link Monster} (not {@code Enemy}, which would be
 * {@code IMob}) and {@code EntityVillager} is {@link Villager}.
 */
public final class MyUtils {

    private MyUtils() {
    }

    /**
     * MyUtils.java:9-11. True for a living member of the royal family.
     */
    public static boolean isRoyalty(final Entity e) {
        return e instanceof LivingEntity && e instanceof Royalty;
    }

    /**
     * MyUtils.java:13-15. True for monsters, the mod's attackable non-monsters, royalty and villagers.
     * All terms of the original chain are side-effect free, so grouping the ten mod classes behind one
     * interface does not change the result.
     */
    public static boolean isAttackableNonMob(final LivingEntity par1EntityLiving) {
        return par1EntityLiving instanceof Monster
                || par1EntityLiving instanceof AttackableNonMob
                || isRoyalty(par1EntityLiving)
                || par1EntityLiving instanceof Villager;
    }

    /**
     * MyUtils.java:17-19. True for the small, harmless or helper creatures that target searches skip.
     */
    public static boolean isIgnoreable(final LivingEntity par1EntityLiving) {
        return par1EntityLiving instanceof Ignoreable;
    }
}
