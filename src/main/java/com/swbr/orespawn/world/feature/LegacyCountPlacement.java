package com.swbr.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.config.stats.OreStats;
import com.swbr.orespawn.config.stats.TweakStats;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * {@code orespawn:legacy_count}: the number of vein attempts of one ore block in {@code OreSpawnWorld.generateOres}
 * (OreSpawnWorld.java:339-961) and {@code generateNether} (:232-257), read from the COMMON config at placement time
 * (DECISIONS R12: "Raten über einen eigenen Placement-Modifier aus der COMMON-Config").
 *
 * <p>The four shapes of the original, in its order of random draws:
 * <table>
 * <tr><th>original</th><th>JSON</th></tr>
 * <tr><td>{@code if (S.rate > 0) { patchy = S.rate + nextInt(e); if (nextInt(20) == 0) patchy += 30; if (LessOre != 0) patchy /= 3; }}
 * (SpawnOres :340-347)</td><td>{@code stat, extra: e, bonus_one_in: 20, bonus: 30, less_ore_divisor: 3}</td></tr>
 * <tr><td>{@code if (S.rate > 0) { patchy = S.rate + nextInt(e); if (LessOre != 0) patchy /= 3; }} (Uranium, Titanium,
 * Amethyst, Salt)</td><td>{@code stat, extra: e, less_ore_divisor: 3}</td></tr>
 * <tr><td>{@code patchy = b + nextInt(e); if (LessOre != 0) patchy /= d;} (ant trolls :846-869, Nether :237-256)</td>
 * <td>{@code base: b, extra: e, less_ore_divisor: d}</td></tr>
 * <tr><td>{@code if (LessOre == 0) { if (S.rate > 0) for (i < S.rate) }} (diamond ... block ruby :889-960)</td>
 * <td>{@code stat, requires_no_less_ore: true}</td></tr>
 * <tr><td>{@code if (S.rate > 0) for (patchyy = S.rate + nextInt(5); ...)} (overworld ruby :870-888)</td>
 * <td>{@code stat, extra: 5}</td></tr>
 * </table>
 */
public final class LegacyCountPlacement extends PlacementModifier {

    public static final MapCodec<LegacyCountPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LegacyOreStat.CODEC.optionalFieldOf("stat").forGetter(p -> p.stat),
            Codec.INT.optionalFieldOf("base", 0).forGetter(p -> p.base),
            Codec.INT.optionalFieldOf("extra", 0).forGetter(p -> p.extra),
            Codec.INT.optionalFieldOf("bonus_one_in", 0).forGetter(p -> p.bonusOneIn),
            Codec.INT.optionalFieldOf("bonus", 0).forGetter(p -> p.bonus),
            Codec.INT.optionalFieldOf("less_ore_divisor", 1).forGetter(p -> p.lessOreDivisor),
            Codec.BOOL.optionalFieldOf("requires_no_less_ore", false).forGetter(p -> p.requiresNoLessOre)
    ).apply(instance, LegacyCountPlacement::new));

    private final Optional<LegacyOreStat> stat;
    private final int base;
    private final int extra;
    private final int bonusOneIn;
    private final int bonus;
    private final int lessOreDivisor;
    private final boolean requiresNoLessOre;

    public LegacyCountPlacement(final Optional<LegacyOreStat> stat, final int base, final int extra, final int bonusOneIn,
                                final int bonus, final int lessOreDivisor, final boolean requiresNoLessOre) {
        this.stat = stat;
        this.base = base;
        this.extra = extra;
        this.bonusOneIn = bonusOneIn;
        this.bonus = bonus;
        this.lessOreDivisor = lessOreDivisor;
        this.requiresNoLessOre = requiresNoLessOre;
    }

    /** The attempt count for one chunk, drawn in the original order. */
    public int count(final RandomSource random) {
        final int lessOre = TweakStats.LessOre();
        if (this.requiresNoLessOre && lessOre != 0) {
            return 0;
        }
        int patchy = this.base;
        if (this.stat.isPresent()) {
            final OreStats stats = this.stat.get().get();
            if (stats.rate() <= 0) {
                return 0;
            }
            patchy = stats.rate();
        }
        if (this.extra > 0) {
            patchy += random.nextInt(this.extra);
        }
        if (this.bonusOneIn > 0 && random.nextInt(this.bonusOneIn) == 0) {
            patchy += this.bonus;
        }
        if (lessOre != 0 && this.lessOreDivisor > 1) {
            patchy /= this.lessOreDivisor;
        }
        return patchy;
    }

    @Override
    public Stream<BlockPos> getPositions(final PlacementContext context, final RandomSource random, final BlockPos pos) {
        return IntStream.range(0, this.count(random)).mapToObj(i -> pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return OreSpawnFeatures.LEGACY_COUNT.get();
    }
}
