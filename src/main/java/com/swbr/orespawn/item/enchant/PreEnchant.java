package com.swbr.orespawn.item.enchant;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * The pre-enchantment pattern of OreSpawn 1.7.10, ported per DECISIONS R7.
 *
 * <p>Twenty-nine original classes called {@code ItemStack.addEnchantment} at runtime (catalogue
 * README section 5.8). The tool and weapon classes all follow one shape: {@code onCreated} adds a
 * fixed set, and {@code onUsingTick} - called from {@code onUpdate} every inventory tick - reads one
 * <em>sentinel</em> enchantment with {@code EnchantmentHelper.getEnchantmentLevel} and, when it is
 * missing, adds the whole set again. The set is therefore not removable, and creative or loot
 * copies get it on their first tick.
 *
 * <p>Enchantments are a data-driven registry in 1.21.1, so the holders can only be resolved from a
 * {@link Level}; both {@code onCraftedBy} and {@code inventoryTick} have one.
 *
 * <p>Semantics kept from 1.7.10:
 * <ul>
 *   <li>{@code addEnchantment} appended a second NBT entry instead of raising the level, and the
 *       combat code summed the modifiers of every entry (R18, catalogue 5.8). {@link #add} therefore
 *       <em>adds</em> the level to whatever is already there instead of taking the maximum.</li>
 *   <li>Levels above the vanilla maximum are written as they are; {@code ItemEnchantments.Mutable}
 *       only caps at 255.</li>
 *   <li>The sentinel is read from the stack's own component
 *       ({@link EnchantmentHelper#getTagEnchantmentLevel}), as {@code getEnchantmentLevel} read the
 *       NBT list - not the gameplay view that NeoForge may extend.</li>
 * </ul>
 */
public final class PreEnchant {

    /** One {@code addEnchantment(enchantment, level)} call. */
    public record Entry(ResourceKey<Enchantment> key, int level) {}

    private PreEnchant() {}

    public static Entry entry(ResourceKey<Enchantment> key, int level) {
        return new Entry(key, level);
    }

    /** The holder for {@code key} in this level's registries. */
    public static Holder<Enchantment> holder(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    /** {@code EnchantmentHelper.getEnchantmentLevel(id, stack)}: the level on the stack, 0 when absent. */
    public static int level(ItemStack stack, Level level, ResourceKey<Enchantment> key) {
        return EnchantmentHelper.getTagEnchantmentLevel(holder(level, key), stack);
    }

    /** {@code stack.addEnchantment(enchantment, lvl)}: appends, i.e. sums with an existing entry. */
    public static void add(ItemStack stack, Level level, ResourceKey<Enchantment> key, int lvl) {
        Holder<Enchantment> holder = holder(level, key);
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(holder, mutable.getLevel(holder) + lvl));
    }

    /** The {@code onCreated} body: every entry in source order. */
    public static void addAll(ItemStack stack, Level level, List<Entry> entries) {
        for (Entry entry : entries) {
            add(stack, level, entry.key(), entry.level());
        }
    }

    /**
     * The {@code onUsingTick} body as called from {@code onUpdate}: when {@code sentinel} is
     * missing, add {@code entries} again.
     *
     * <p>PORT: runs on the server only. The original ran on both sides and the client copy was
     * overwritten by the next inventory sync anyway; in 1.21.1 the server's component change is
     * synced automatically, and a client must not rewrite stacks it does not own.
     *
     * @return whether the set was (re)applied
     */
    public static boolean restore(ItemStack stack, Level level, ResourceKey<Enchantment> sentinel, List<Entry> entries) {
        if (level.isClientSide) {
            return false;
        }
        if (level(stack, level, sentinel) <= 0) {
            addAll(stack, level, entries);
            return true;
        }
        return false;
    }
}
