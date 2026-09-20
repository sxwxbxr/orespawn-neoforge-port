package com.swbr.orespawn.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * The nine {@code ChestGenHooks.getInfo(...).addItem(new WeightedRandomChestContent(...))} calls of
 * {@code OreSpawnMain.make_some_more_things} (OreSpawnMain.java:5051-5062), as a Global Loot Modifier
 * (DECISIONS R10). One JSON instance per 1.7.10 chest category under
 * {@code data/orespawn/loot_modifiers/}.
 *
 * <p>In 1.7.10 the OreSpawn entries went into the same weighted list as the vanilla chest content,
 * and {@code WeightedRandomChestContent.generateChestContents} drew {@code count} times from that
 * list ({@code WeightedRandom.getRandomItem}: {@code j = rand.nextInt(total); j -= weight; if j < 0}).
 * The per-chest chance of an OreSpawn item therefore depends on the list's total weight and the
 * number of draws. This modifier replays exactly those extra draws: {@code draws_min..draws_max}
 * draws, each a {@code nextInt(total_weight)} walk over the OreSpawn entries; a roll that lands in
 * the vanilla part of the old list adds nothing, because the 1.21.1 table already generated its own
 * vanilla content. Totals and draw counts are read from the 1.7.10 bytecode, see the JSON files and
 * {@code docs/port/W12.md}.
 *
 * <p>PORT: 1.7.10 put each drawn stack into a random slot of the 27-slot chest and could overwrite
 * an earlier stack; 1.21.1's {@code RandomizableContainer} fill distributes the generated list
 * without overwriting. An OreSpawn item that 1.7.10 wrote and then overwrote survives here - a
 * slightly higher effective chance (R18 case 3: the slot writer is vanilla code).
 */
public final class LegacyChestContentModifier extends LootModifier {

    /** One {@code WeightedRandomChestContent(stack, min, max, weight)} (parameter order in core-01b). */
    public record Entry(ResourceLocation item, int min, int max, int weight) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Entry::item),
                ExtraCodecs.POSITIVE_INT.fieldOf("min").forGetter(Entry::min),
                ExtraCodecs.POSITIVE_INT.fieldOf("max").forGetter(Entry::max),
                ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(Entry::weight)
        ).apply(i, Entry::new));
    }

    public static final MapCodec<LegacyChestContentModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(m -> m.conditions),
            ExtraCodecs.POSITIVE_INT.fieldOf("draws_min").forGetter(m -> m.drawsMin),
            ExtraCodecs.POSITIVE_INT.fieldOf("draws_max").forGetter(m -> m.drawsMax),
            ExtraCodecs.POSITIVE_INT.fieldOf("total_weight").forGetter(m -> m.totalWeight),
            Entry.CODEC.listOf().fieldOf("entries").forGetter(m -> m.entries)
    ).apply(i, LegacyChestContentModifier::new));

    private final int drawsMin;
    private final int drawsMax;
    private final int totalWeight;
    private final List<Entry> entries;

    public LegacyChestContentModifier(LootItemCondition[] conditions, int drawsMin, int drawsMax, int totalWeight,
            List<Entry> entries) {
        super(conditions);
        this.drawsMin = drawsMin;
        this.drawsMax = drawsMax;
        this.totalWeight = totalWeight;
        this.entries = List.copyOf(entries);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        final RandomSource random = context.getRandom();
        // Vanilla draw counts: WorldGenDungeons 8 (bipush 8), the pyramids 2 + rand.nextInt(5).
        final int draws = drawsMin < drawsMax ? drawsMin + random.nextInt(drawsMax - drawsMin + 1) : drawsMin;
        for (int d = 0; d < draws; d++) {
            // WeightedRandom.getRandomItem(Random, Collection, int)
            int j = random.nextInt(totalWeight);
            for (final Entry entry : entries) {
                j -= entry.weight();
                if (j < 0) {
                    add(generatedLoot, entry, random);
                    break;
                }
            }
        }
        return generatedLoot;
    }

    /** {@code WeightedRandomChestContent.generateChestContent}: {@code min + rand.nextInt(max - min + 1)} items. */
    private static void add(ObjectArrayList<ItemStack> generatedLoot, Entry entry, RandomSource random) {
        // An id that does not resolve (an item of a later wave) adds nothing; the total weight stays the
        // original's so the other entries keep their 1.7.10 chance. antrobotkit/spiderrobotkit resolve since W09.
        final Item item = BuiltInRegistries.ITEM.getOptional(entry.item()).orElse(null);
        if (item == null) {
            return;
        }
        final int count = entry.min() >= entry.max() ? entry.min() : entry.min() + random.nextInt(entry.max() - entry.min() + 1);
        generatedLoot.add(new ItemStack(item, count));
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.LEGACY_CHEST_CONTENT.get();
    }
}
