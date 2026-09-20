package com.swbr.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.config.stats.OreStats;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * {@code orespawn:legacy_ore_position}: one vein attempt of {@code OreSpawnWorld.generateOres} /
 * {@code generateNether}, drawn in the original order
 * <pre>{@code
 * randPosX = offset + chunkX + nextInt(spread);
 * randPosY = nextInt(y_bound) + y_offset;
 * randPosZ = offset + chunkZ + nextInt(spread);
 * if (randPosY <= maxdepth && randPosY >= mindepth) ...
 * }</pre>
 * The band is the stat's {@code mindepth..maxdepth} ({@code stat}), a literal {@code min_y..max_y} (ant trolls 5..50),
 * or none (Nether). A position outside the band yields nothing, exactly like the skipped {@code if}.
 *
 * <p>Y is absolute (DECISIONS R18, "Y-Bänder von Erzen und Features"); origin must be the chunk corner, which is what
 * a placed feature without {@code in_square} receives.
 */
public final class LegacyOrePlacement extends PlacementModifier {

    public static final MapCodec<LegacyOrePlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("offset").forGetter(p -> p.offset),
            Codec.INT.fieldOf("spread").forGetter(p -> p.spread),
            Codec.INT.fieldOf("y_bound").forGetter(p -> p.yBound),
            Codec.INT.optionalFieldOf("y_offset", 0).forGetter(p -> p.yOffset),
            LegacyOreStat.CODEC.optionalFieldOf("stat").forGetter(p -> p.stat),
            Codec.INT.optionalFieldOf("min_y").forGetter(p -> p.minY),
            Codec.INT.optionalFieldOf("max_y").forGetter(p -> p.maxY)
    ).apply(instance, LegacyOrePlacement::new));

    private final int offset;
    private final int spread;
    private final int yBound;
    private final int yOffset;
    private final Optional<LegacyOreStat> stat;
    private final Optional<Integer> minY;
    private final Optional<Integer> maxY;

    public LegacyOrePlacement(final int offset, final int spread, final int yBound, final int yOffset,
                              final Optional<LegacyOreStat> stat, final Optional<Integer> minY,
                              final Optional<Integer> maxY) {
        this.offset = offset;
        this.spread = spread;
        this.yBound = yBound;
        this.yOffset = yOffset;
        this.stat = stat;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public Stream<BlockPos> getPositions(final PlacementContext context, final RandomSource random, final BlockPos pos) {
        final int randPosX = this.offset + pos.getX() + random.nextInt(this.spread);
        final int randPosY = random.nextInt(this.yBound) + this.yOffset;
        final int randPosZ = this.offset + pos.getZ() + random.nextInt(this.spread);
        if (this.stat.isPresent()) {
            final OreStats stats = this.stat.get().get();
            if (randPosY > stats.maxdepth() || randPosY < stats.mindepth()) {
                return Stream.empty();
            }
        }
        if (this.maxY.isPresent() && randPosY > this.maxY.get()) {
            return Stream.empty();
        }
        if (this.minY.isPresent() && randPosY < this.minY.get()) {
            return Stream.empty();
        }
        return Stream.of(new BlockPos(randPosX, randPosY, randPosZ));
    }

    @Override
    public PlacementModifierType<?> type() {
        return OreSpawnFeatures.LEGACY_ORE_POSITION.get();
    }
}
