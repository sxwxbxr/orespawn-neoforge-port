package com.swbr.orespawn.entity.worm;

import com.swbr.orespawn.OreSpawn;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Helpers the three worm classes share (no original class). Each method ports a piece of 1.7.10 code that
 * {@code WormSmall}, {@code WormMedium} and {@code WormLarge} repeat verbatim, so the three copies cannot drift.
 */
public final class WormSupport {

    private WormSupport() {}

    /**
     * {@code World.findNearestEntityWithinAABB(Class, AABB, Entity)}: every entity of the class (subclasses included)
     * in the box except {@code self}, nearest by squared distance; on a tie the later one wins ({@code <=}).
     */
    @Nullable
    public static <T extends Entity> T findNearestEntityWithinAABB(final Entity self, final Class<T> clazz, final AABB box) {
        final List<T> list = self.level().getEntitiesOfClass(clazz, box);
        T nearest = null;
        double d0 = Double.MAX_VALUE;
        for (final T entity2 : list) {
            if (entity2 != self) {
                final double d1 = self.distanceToSqr(entity2);
                if (d1 <= d0) {
                    nearest = entity2;
                    d0 = d1;
                }
            }
        }
        return nearest;
    }

    /**
     * {@code bid == Blocks.tallgrass}: 1.7.10's tall grass block with its metadata variants grass and fern.
     * {@code Blocks.deadbush} was a different block and does not count.
     */
    public static boolean isTallGrass(final BlockState bid) {
        return bid.is(Blocks.SHORT_GRASS) || bid.is(Blocks.FERN);
    }

    /**
     * {@code bid == Blocks.grass || bid == Blocks.dirt || bid == Blocks.stone}: the blocks a worm may dig through.
     * {@code Blocks.dirt} carried dirt, coarse dirt and podzol as metadata.
     *
     * <p>PORT (R22 with its 2026-09-14 addendum): grass and dirt are the category {@code #minecraft:dirt} as 1.21.1
     * ships it - rooted dirt, moss, mud, muddy mangrove roots and mycelium included (the grass block is in the tag);
     * {@code Blocks.stone} is the category "stone" as {@code #stone_ore_replaceables} plus
     * {@code #deepslate_ore_replaceables}, so a worm below Y 0 is not killed by deepslate that 1.7.10 never had.
     * The callers have no grass branch of their own (the tall grass test before them is a different block).
     */
    public static boolean isGrassDirtOrStone(final BlockState bid) {
        return bid.is(BlockTags.DIRT)
                || bid.is(BlockTags.STONE_ORE_REPLACEABLES) || bid.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }

    /**
     * 1.7.10 {@code Entity.applyEntityCollision} ({@code sa.g}, disassembled): the pushed entity is {@code self}.
     *
     * <p>PORT: 1.21.1's {@code Entity.push(Entity)} returns early when either side has {@code noPhysics}; the 1.7.10
     * method had no {@code noClip} test (only the riding checks), so a burrowed worm was still nudged by, and nudged,
     * whatever walked into it. The rest is the vanilla body: distance 0.01, inverse-distance scale capped at 1, 0.05.
     */
    public static void legacyPush(final Entity self, final Entity entity) {
        if (!self.isPassengerOfSameVehicle(entity)) {
            double d0 = entity.getX() - self.getX();
            double d1 = entity.getZ() - self.getZ();
            double d2 = Mth.absMax(d0, d1);
            if (d2 >= 0.01F) {
                d2 = Math.sqrt(d2);
                d0 /= d2;
                d1 /= d2;
                double d3 = 1.0 / d2;
                if (d3 > 1.0) {
                    d3 = 1.0;
                }
                d0 *= d3;
                d1 *= d3;
                d0 *= 0.05F;
                d1 *= 0.05F;
                if (!self.isVehicle() && self.isPushable()) {
                    self.push(-d0, 0.0, -d1);
                }
                if (!entity.isVehicle() && entity.isPushable()) {
                    entity.push(d0, 0.0, d1);
                }
            }
        }
    }

    /**
     * The theft every worm repeats (WormSmall.java:188-201, WormMedium.java:201-229, WormLarge.java:194-239): take the
     * piece off the player, damage it by {@code rest / divisor} (a rest at or below the divisor costs 1) and throw it
     * up to four blocks away, three blocks up, on the shared {@code OreSpawnRand}.
     *
     * <p>PORT: a piece that breaks from the damage is an empty stack. The original still spawned its item entity;
     * 1.21.1 discards an empty {@code ItemEntity} on its first tick ({@code ItemEntity.tick}), so the entity is built
     * (the constructor draws the same level random numbers) but not added.
     */
    public static void stealAndThrow(final Mob worm, final Player target, final EquipmentSlot slot, final ItemStack boots,
                                     final int divisor) {
        int bid = 0;
        target.setItemSlot(slot, ItemStack.EMPTY);
        bid = boots.getMaxDamage() - boots.getDamageValue();
        if (bid > divisor) {
            bid /= divisor;
        } else {
            bid = 1;
        }
        damageItem(boots, bid, worm);
        final ItemEntity var3 = new ItemEntity(worm.level(),
                worm.getX() + OreSpawn.OreSpawnRand.nextInt(5) - OreSpawn.OreSpawnRand.nextInt(5),
                worm.getY() + 3.0,
                worm.getZ() + OreSpawn.OreSpawnRand.nextInt(5) - OreSpawn.OreSpawnRand.nextInt(5),
                boots);
        if (!boots.isEmpty()) {
            worm.level().addFreshEntity(var3);
        }
    }

    /**
     * 1.7.10 {@code ItemStack.damageItem(amount, entity)} with the worm as the entity: no creative test (the worm is no
     * player), Unbreaking, and on breaking {@code renderBrokenItemStack} on the worm, which on a server is only its
     * sound ({@code sv.a(add)}: {@code "random.break"}, 0.8, {@code 0.8 + worldObj.rand.nextFloat() * 0.4}).
     *
     * <p>PORT: 1.21.1 breaks at {@code damage >= maxDamage}, 1.7.10 at {@code damage > maxDamage}; the durability
     * numbers of the 1.21.1 items are built for the new comparison. Unbreaking rolls on the level random, not the
     * worm's.
     */
    public static void damageItem(final ItemStack stack, final int amount, final LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            stack.hurtAndBreak(amount, serverLevel, entity, item -> entity.playSound(SoundEvents.ITEM_BREAK, 0.8f,
                    0.8f + entity.level().random.nextFloat() * 0.4f));
        }
    }
}
