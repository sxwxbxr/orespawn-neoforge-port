package com.swbr.orespawn.world.structure;

import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Port of the 1.7.10 vanilla {@code WeightedRandomChestContent} ({@code qx}) together with
 * {@code WeightedRandom.getRandomItem} ({@code qv}), the chest filler of GenericDungeon, Trees, BasiliskMaze,
 * RubyBirdDungeon and {@code OreSpawnWorld.addCrystalChest}.
 *
 * <p>Read from {@code client-1.7.10.jar} with {@code javap -c}:
 * <ul>
 * <li>{@code qx(Item, damage, min, max, weight)} wraps {@code new ItemStack(item, 1, damage)}; {@code qx(ItemStack,
 * min, max, weight)} keeps the stack.</li>
 * <li>{@code qx.a(Random, qx[], IInventory, int)} ({@code generateChestContents}): {@code count} times: pick an
 * entry with {@code qv.a(Random, qw[])}, then {@code n = min + rand.nextInt(max - min + 1)}; if the item's max
 * stack size is at least {@code n}, one copy of size {@code n} into slot {@code rand.nextInt(size)}, otherwise
 * {@code n} copies of size 1, each into its own {@code rand.nextInt(size)}. A slot is overwritten, not merged.</li>
 * <li>{@code qv.a(Random, qw[], int)}: throws on a total weight &lt;= 0; {@code j = rand.nextInt(total)}; walks the
 * array subtracting weights and returns the first entry that takes {@code j} below zero.</li>
 * </ul>
 * Forge 1.7.10 routed the stack generation through {@code ChestGenHooks.generateStacks} with the same draws and
 * split rule; no Forge jar lies in {@code reference/}, so that part is from its source, not bytecode.
 *
 * <p>PORT: items are looked up when the chest is filled, not when the list is built - registries are frozen
 * long after the static lists of the original were initialised. An entry whose item does not exist (an id of a
 * wave that is not ported yet, W09-W11) still takes part in the weighted draw and consumes its random numbers,
 * but places nothing. Damage values become nothing: 1.21.1 has no item metadata, the ported lists name the
 * flattened item instead.
 */
public final class WeightedRandomChestContent {

    private final Supplier<ItemStack> stack;
    private final int theMinimumChanceToGenerateItem;
    private final int theMaximumChanceToGenerateItem;
    private final int itemWeight;

    private WeightedRandomChestContent(final Supplier<ItemStack> stack, final int min, final int max, final int weight) {
        this.stack = stack;
        this.theMinimumChanceToGenerateItem = min;
        this.theMaximumChanceToGenerateItem = max;
        this.itemWeight = weight;
    }

    /** {@code new WeightedRandomChestContent(item, 0, min, max, weight)} for an item that exists now. */
    public static WeightedRandomChestContent of(final Supplier<? extends ItemLike> item, final int min, final int max,
                                                final int weight) {
        return new WeightedRandomChestContent(() -> new ItemStack(item.get()), min, max, weight);
    }

    /** As {@link #of(Supplier, int, int, int)} for a vanilla item. */
    public static WeightedRandomChestContent of(final ItemLike item, final int min, final int max, final int weight) {
        return new WeightedRandomChestContent(() -> new ItemStack(item), min, max, weight);
    }

    /**
     * An entry by registry id ({@code "orespawn:cageempty"}), resolved at fill time. Use it for items of waves that
     * are not ported yet; a missing id places nothing (class javadoc) and is marked {@code // PORT: TODO Wnn} at the
     * call site.
     */
    public static WeightedRandomChestContent byId(final String id, final int min, final int max, final int weight) {
        final ResourceLocation key = ResourceLocation.parse(id);
        return new WeightedRandomChestContent(() -> {
            final Optional<Item> item = BuiltInRegistries.ITEM.getOptional(key);
            return item.filter(i -> i != Items.AIR).map(ItemStack::new).orElse(ItemStack.EMPTY);
        }, min, max, weight);
    }

    /** {@code WeightedRandomChestContent.generateChestContents} ({@code qx.a}, MCP {@code func_76293_a}). */
    public static void generateChestContents(final Random random, final WeightedRandomChestContent[] contents,
                                             final Container inventory, final int count) {
        for (int i = 0; i < count; ++i) {
            final WeightedRandomChestContent entry = getRandomItem(random, contents);
            final int n = entry.theMinimumChanceToGenerateItem
                    + random.nextInt(entry.theMaximumChanceToGenerateItem - entry.theMinimumChanceToGenerateItem + 1);
            final ItemStack template = entry.stack.get();
            if (template.getMaxStackSize() >= n) {
                final ItemStack copy = template.copy();
                copy.setCount(n);
                setSlot(inventory, random.nextInt(inventory.getContainerSize()), copy);
            } else {
                for (int j = 0; j < n; ++j) {
                    final ItemStack copy = template.copy();
                    copy.setCount(1);
                    setSlot(inventory, random.nextInt(inventory.getContainerSize()), copy);
                }
            }
        }
    }

    /**
     * {@code IInventory.setInventorySlotContents}. PORT: an entry of a missing item (class javadoc) leaves the slot
     * as it was; the original could not name a missing item.
     */
    private static void setSlot(final Container inventory, final int slot, final ItemStack stack) {
        if (!stack.isEmpty()) {
            inventory.setItem(slot, stack);
        }
    }

    /** {@code WeightedRandom.getRandomItem(Random, Item[])} ({@code qv.a}). */
    public static WeightedRandomChestContent getRandomItem(final Random random, final WeightedRandomChestContent[] items) {
        int total = 0;
        for (final WeightedRandomChestContent item : items) {
            total += item.itemWeight;
        }
        if (total <= 0) {
            throw new IllegalArgumentException();
        }
        int j = random.nextInt(total);
        for (final WeightedRandomChestContent item : items) {
            j -= item.itemWeight;
            if (j < 0) {
                return item;
            }
        }
        // Unreachable with a positive total; the original returned null here and crashed on it.
        return items[items.length - 1];
    }

    public int weight() {
        return this.itemWeight;
    }

    public int min() {
        return this.theMinimumChanceToGenerateItem;
    }

    public int max() {
        return this.theMaximumChanceToGenerateItem;
    }
}
