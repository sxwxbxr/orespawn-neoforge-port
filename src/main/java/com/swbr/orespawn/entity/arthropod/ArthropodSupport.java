package com.swbr.orespawn.entity.arthropod;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.entity.ai.GenericTargetSorter;
import com.swbr.orespawn.item.enchant.PreEnchant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * 1.7.10 idioms the seven W07 arthropods ({@link Scorpion}, {@link EmperorScorpion}, {@link CaveFisher}, {@link Bee},
 * {@link HerculesBeetle}, {@link SpitBug}, {@link TrooperBug}) repeat inline. No original class: every method stands for
 * a piece of {@code EntityMob}, {@code World} or a block of statements the originals copied between each other.
 */
public final class ArthropodSupport {

    /** {@code orespawn:ender_knight} (manifest), see {@link #isEnderKnight}. */
    public static final ResourceLocation ENDER_KNIGHT = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ender_knight");
    /** {@code orespawn:ender_reaper} (manifest), see {@link #isEnderReaper}. */
    public static final ResourceLocation ENDER_REAPER = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ender_reaper");

    private ArthropodSupport() {}

    /**
     * {@code instanceof EnderKnight}. PORT: the class is written by a parallel porter of the same wave
     * ({@code entity.ender}); the test compares the registry id so this package compiles on its own. EnderKnight has no
     * subclass in 20.2, so the id is the same set of entities. The integrator may switch to {@code instanceof}.
     */
    public static boolean isEnderKnight(final Entity e) {
        return EntityType.getKey(e.getType()).equals(ENDER_KNIGHT);
    }

    /** {@code instanceof EnderReaper}; same PORT note as {@link #isEnderKnight}. */
    public static boolean isEnderReaper(final Entity e) {
        return EntityType.getKey(e.getType()).equals(ENDER_REAPER);
    }

    /**
     * The air scans of {@code getCanSpawnHere}: every block with x offset {@code j} and z offset {@code k} in
     * {@code [from, to)} and y offset {@code i} in {@code [yFrom, yTo)} must be {@code Blocks.air}.
     *
     * <p>PORT: {@code isAir()} also accepts cave and void air, which 1.7.10 generated as plain air.
     */
    public static boolean isAllAir(final LevelAccessor level, final BlockPos pos, final int from, final int to,
                                   final int yFrom, final int yTo) {
        for (int k = from; k < to; ++k) {
            for (int j = from; j < to; ++j) {
                for (int i = yFrom; i < yTo; ++i) {
                    if (!level.getBlockState(new BlockPos(pos.getX() + j, pos.getY() + i, pos.getZ() + k)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@code findSomethingToAttack}: nothing while {@code PlayNicely} is set; otherwise every living entity in the
     * bounding box grown by {@code (x, y, z)}, nearest first by {@link GenericTargetSorter}, and the first that passes
     * the class's own {@code isSuitableTarget}.
     */
    @Nullable
    public static LivingEntity findSomethingToAttack(final Mob self, final GenericTargetSorter sorter, final double x,
                                                     final double y, final double z, final Predicate<LivingEntity> suitable) {
        if (OreSpawnConfig.TWEAKS.PlayNicely.get() != 0) {
            return null;
        }
        final List<LivingEntity> var5 = new ArrayList<>(
                self.level().getEntitiesOfClass(LivingEntity.class, self.getBoundingBox().inflate(x, y, z)));
        var5.sort(sorter);
        for (final LivingEntity var8 : var5) {
            if (suitable.test(var8)) {
                return var8;
            }
        }
        return null;
    }

    /**
     * {@code World.playSoundAtEntity(entity, name, volume, pitch)}: heard by every player nearby at the entity's feet.
     * The category is the entity's, as in {@code CompanionSupport.playSoundAtEntity} (W04).
     */
    public static void playSoundAtEntity(final Entity entity, final SoundEvent sound, final float volume, final float pitch) {
        entity.level().playSound((Player) null, entity.getX(), entity.getY(), entity.getZ(), sound, entity.getSoundSource(),
                volume, pitch);
    }

    /**
     * The {@code dropItemRand} of the originals: an item entity up to {@code spread - 1} blocks off on the shared
     * {@code OreSpawnRand}, one block up, added straight to the level (no pickup delay).
     *
     * <p>The originals created the {@code EntityItem} first and enchanted the returned stack afterwards; here the caller
     * enchants first and drops second, so the client receives the enchanted stack with the spawn packet. Both RNG
     * streams keep their order ({@code OreSpawnRand} for the offset, the world random for the enchantments). PORT: the
     * toss motion of {@code ItemEntity} is drawn from the level random, where 1.7.10's {@code EntityItem} used
     * {@code Math.random} (same as {@code Whale.dropItemRand}, W06).
     */
    public static void dropItemRand(final Mob mob, final ItemStack is, final int spread) {
        final ItemEntity var3 = new ItemEntity(mob.level(),
                mob.getX() + OreSpawn.OreSpawnRand.nextInt(spread) - OreSpawn.OreSpawnRand.nextInt(spread),
                mob.getY() + 1.0,
                mob.getZ() + OreSpawn.OreSpawnRand.nextInt(spread) - OreSpawn.OreSpawnRand.nextInt(spread),
                is);
        mob.level().addFreshEntity(var3);
    }

    /**
     * {@code is.addEnchantment(ench, lvl)} through W03's {@link PreEnchant#add}, which sums with an existing entry.
     * PORT: 1.7.10 appended a second NBT entry for the second Sharpness roll, whose damage bonus added to the first
     * one; one entry with the summed level is the closest 1.21.1 form. Levels above the vanilla maximum stay as rolled.
     */
    private static void add(final ItemStack is, final Level level, final ResourceKey<Enchantment> key, final int lvl) {
        PreEnchant.add(is, level, key, lvl);
    }

    /** The sword block of the boss drop tables: seven independent rolls on the world random, Sharpness twice. */
    public static void enchantSword(final ItemStack is, final Level level) {
        final RandomSource rand = level.random;
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.SHARPNESS, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.BANE_OF_ARTHROPODS, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.KNOCKBACK, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.LOOTING, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(2) == 1) {
            add(is, level, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.FIRE_ASPECT, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.SHARPNESS, 1 + rand.nextInt(5));
        }
    }

    /** The shovel, axe and hoe blocks: Unbreaking 1/2 (2-5), Efficiency 1/6 (1-5). */
    public static void enchantTool(final ItemStack is, final Level level) {
        final RandomSource rand = level.random;
        if (rand.nextInt(2) == 1) {
            add(is, level, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.EFFICIENCY, 1 + rand.nextInt(5));
        }
    }

    /** The pickaxe block: Unbreaking 1/2 (2-5), Efficiency 1/6 (1-5), Fortune 1/6 (1-5). */
    public static void enchantPickaxe(final ItemStack is, final Level level) {
        final RandomSource rand = level.random;
        if (rand.nextInt(2) == 1) {
            add(is, level, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.EFFICIENCY, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.FORTUNE, 1 + rand.nextInt(5));
        }
    }

    /** The helmet block: four protections 1/6 (1-5), Unbreaking 1/2 (2-5), Respiration 1/6 (1-2), Aqua Affinity 1/6 (1-5). */
    public static void enchantHelmet(final ItemStack is, final Level level) {
        final RandomSource rand = level.random;
        enchantProtections(is, level, rand);
        if (rand.nextInt(2) == 1) {
            add(is, level, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.RESPIRATION, 1 + rand.nextInt(2));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.AQUA_AFFINITY, 1 + rand.nextInt(5));
        }
    }

    /** The chestplate and leggings blocks: four protections 1/6 (1-5), Unbreaking 1/2 (2-5). */
    public static void enchantBody(final ItemStack is, final Level level) {
        final RandomSource rand = level.random;
        enchantProtections(is, level, rand);
        if (rand.nextInt(2) == 1) {
            add(is, level, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
    }

    /** The boots block: Feather Falling 1/6 (5-9), Unbreaking 1/2 (2-5). */
    public static void enchantBoots(final ItemStack is, final Level level) {
        final RandomSource rand = level.random;
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.FEATHER_FALLING, 5 + rand.nextInt(5));
        }
        if (rand.nextInt(2) == 1) {
            add(is, level, Enchantments.UNBREAKING, 2 + rand.nextInt(4));
        }
    }

    /** Protection, Blast Protection, Fire Protection, Projectile Protection, each 1/6 (1-5), in this order. */
    private static void enchantProtections(final ItemStack is, final Level level, final RandomSource rand) {
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.BLAST_PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.FIRE_PROTECTION, 1 + rand.nextInt(5));
        }
        if (rand.nextInt(6) == 1) {
            add(is, level, Enchantments.PROJECTILE_PROTECTION, 1 + rand.nextInt(5));
        }
    }
}
