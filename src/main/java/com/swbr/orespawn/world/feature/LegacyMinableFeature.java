package com.swbr.orespawn.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.structure.StructureWriter;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * {@code orespawn:legacy_minable}: {@code new WorldGenMinable(block, size[, target]).generate(world, random, x, y, z)}
 * as {@code OreSpawnWorld.generateOres} and {@code generateNether} call it, on a 1.21.1 generation region.
 *
 * <p>The vein algorithm is the 1.7.10 vanilla {@code WorldGenMinable} ({@code ase}), the same code as the shared port
 * {@code world.gen.WorldGenMinable} and {@code ChunkOreGenerator.generateBlockOre}; this copy exists because those
 * two read and write through {@code LegacyWorld} (Y clipped to 0..255) or a single chunk, while the overworld runs
 * from -64 to 320 and its veins cross into the neighbour chunks as they did in 1.7.10.
 *
 * <p>PORT, target block (DECISIONS R18 and R22): {@code Blocks.stone} is a category - {@code stone} replaces
 * {@code #minecraft:stone_ore_replaceables} and {@code #minecraft:deepslate_ore_replaceables} with the same block (the
 * original has no deepslate texture); {@code netherrack} replaces {@code #minecraft:base_stone_nether}.
 *
 * <p>{@code spawn_ores: true} is the {@code SpawnOres} branch (:353-786): the block is rolled per attempt, 7 in 104
 * one of the seven rare spawn ores, otherwise one of the 98 in switch order; the ids are the ones
 * {@code ChunkOreGenerator} already maps.
 */
public final class LegacyMinableFeature extends Feature<LegacyMinableFeature.Config> {

    /** The seven rare spawn ores of the {@code nextInt(104) < 7} branch, in switch order (:356-385). */
    private static final String[] RARE_SPAWN_ORES = {
            "orebrutalfly", "orenastysaurus", "orepointysaurus", "orecricket", "orefrog", "orespiderdriver",
            "orecrab"};

    /** The 98 spawn ores of the {@code nextInt(98)} branch, in switch order (:389-784). */
    private static final String[] SPAWN_ORES = {
            "orespider", "orebat", "orecow", "orepig", "oresquid", "orechicken", "orecreeper", "oreskeleton",
            "orezombie", "oreslime", "oreghast", "orezombiepigman", "oreenderman", "orecavespider",
            "oresilverfish", "oremagmacube", "orewitch", "oresheep", "orewolf", "oremooshroom", "oreocelot",
            "oreblaze", "orewitherskeleton", "oreenderdragon", "oresnowgolem", "oreirongolem", "orewitherboss",
            "oregirlfriend", "oreredcow", "oregoldcow", "oreenchantedcow", "oremothra", "orealosaurus",
            "orecryolophosaurus", "orecamarasaurus", "orevelocityraptor", "orehydrolisc", "orebasilisc",
            "oredragonfly", "oreemperorscorpion", "orescorpion", "orecavefisher", "orespyro", "orebaryonyx",
            "oregammametroid", "orecockateil", "orekyuubi", "orealien", "oreattacksquid", "orewaterdragon",
            "orekraken", "orelizard", "orecephadrome", "oredragon", "orebee", "orehorse", "oretrooper",
            "orespit", "orestink", "oreostrich", "oregazelle", "orechipmunk", "orecreepinghorror",
            "oreterribleterror", "orecliffracer", "oretriffid", "orenightmare", "orelurkingterror",
            "oregodzillapart", "oregodzilla", "oresmallworm", "oremediumworm", "orelargeworm", "orecassowary",
            "orecloudshark", "oregoldfish", "oreleafmonster", "oretshirt", "oreenderknight", "oreenderreaper",
            "orebeaver", "oretrex", "orehercules", "oremantis", "orestinky", "oreboyfriend", "orethekingpart",
            "oreeasterbunny", "orecaterkiller", "oremolenoid", "oreseamonster", "oreseaviper", "oreleon",
            "orehammerhead", "orerubberducky", "orevillager", "orecriminal", "orethequeenpart"};

    /** What the vein replaces. */
    public enum Target implements StringRepresentable {
        STONE("stone"),
        NETHERRACK("netherrack");

        public static final Codec<Target> CODEC = StringRepresentable.fromEnum(Target::values);

        private final String name;

        Target(final String name) {
            this.name = name;
        }

        boolean test(final BlockState state) {
            if (this == NETHERRACK) {
                return state.is(BlockTags.BASE_STONE_NETHER);
            }
            return state.is(BlockTags.STONE_ORE_REPLACEABLES) || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    /**
     * @param state     the ore block; ignored with {@code spawn_ores}
     * @param size      {@code numberOfBlocks} when no {@code size_stat} is given
     * @param sizeStat  read {@code clumpsize} of this stat at placement time
     */
    public record Config(Optional<BlockState> state, int size, Optional<LegacyOreStat> sizeStat, Target target,
                         boolean spawnOres) implements FeatureConfiguration {

        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.optionalFieldOf("state").forGetter(Config::state),
                Codec.INT.optionalFieldOf("size", 0).forGetter(Config::size),
                LegacyOreStat.CODEC.optionalFieldOf("size_stat").forGetter(Config::sizeStat),
                Target.CODEC.optionalFieldOf("target", Target.STONE).forGetter(Config::target),
                Codec.BOOL.optionalFieldOf("spawn_ores", false).forGetter(Config::spawnOres)
        ).apply(instance, Config::new));
    }

    public LegacyMinableFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(final FeaturePlaceContext<Config> context) {
        final Config config = context.config();
        final RandomSource random = context.random();
        final BlockPos origin = context.origin();
        final int size = config.sizeStat().map(s -> s.get().clumpsize()).orElse(config.size());
        final BlockState block;
        if (config.spawnOres()) {
            // :353-386 / :388-785
            if (random.nextInt(104) < 7) {
                block = spawnOre(RARE_SPAWN_ORES[random.nextInt(7)]);
            } else {
                block = spawnOre(SPAWN_ORES[random.nextInt(98)]);
            }
        } else if (config.state().isPresent()) {
            block = config.state().get();
        } else {
            return false;
        }
        final StructureWriter world = StructureWriter.forFeature(context.level(), origin.getX() >> 4, origin.getZ() >> 4);
        return generate(world, random, origin.getX(), origin.getY(), origin.getZ(), block, size, config.target());
    }

    private static BlockState spawnOre(final String id) {
        final var egg = ModBlocks.DRIED_EGGS.get(id);
        if (egg == null) {
            OreSpawn.LOG.warn("legacy_minable: spawn ore {} is not registered", id);
            return Blocks.STONE.defaultBlockState();
        }
        return egg.get().defaultBlockState();
    }

    /** {@code WorldGenMinable.generate} ({@code ase.a(ahb, Random, III)}). */
    public static boolean generate(final StructureWriter world, final RandomSource rand, final int x, final int y,
                                   final int z, final BlockState minableBlock, final int numberOfBlocks,
                                   final Target target) {
        final float angle = rand.nextFloat() * (float) Math.PI;
        final double x1 = (double) ((float) (x + 8) + Mth.sin(angle) * (float) numberOfBlocks / 8.0F);
        final double x2 = (double) ((float) (x + 8) - Mth.sin(angle) * (float) numberOfBlocks / 8.0F);
        final double z1 = (double) ((float) (z + 8) + Mth.cos(angle) * (float) numberOfBlocks / 8.0F);
        final double z2 = (double) ((float) (z + 8) - Mth.cos(angle) * (float) numberOfBlocks / 8.0F);
        final double y1 = (double) (y + rand.nextInt(3) - 2);
        final double y2 = (double) (y + rand.nextInt(3) - 2);
        for (int l = 0; l <= numberOfBlocks; ++l) {
            final double cx = x1 + (x2 - x1) * (double) l / (double) numberOfBlocks;
            final double cy = y1 + (y2 - y1) * (double) l / (double) numberOfBlocks;
            final double cz = z1 + (z2 - z1) * (double) l / (double) numberOfBlocks;
            final double size = rand.nextDouble() * (double) numberOfBlocks / 16.0;
            final double rxz = (double) (Mth.sin((float) l * (float) Math.PI / (float) numberOfBlocks) + 1.0F) * size + 1.0;
            final double ry = (double) (Mth.sin((float) l * (float) Math.PI / (float) numberOfBlocks) + 1.0F) * size + 1.0;
            final int minX = Mth.floor(cx - rxz / 2.0);
            final int minY = Mth.floor(cy - ry / 2.0);
            final int minZ = Mth.floor(cz - rxz / 2.0);
            final int maxX = Mth.floor(cx + rxz / 2.0);
            final int maxY = Mth.floor(cy + ry / 2.0);
            final int maxZ = Mth.floor(cz + rxz / 2.0);
            for (int bx = minX; bx <= maxX; ++bx) {
                final double dx = ((double) bx + 0.5 - cx) / (rxz / 2.0);
                if (dx * dx < 1.0) {
                    for (int by = minY; by <= maxY; ++by) {
                        final double dy = ((double) by + 0.5 - cy) / (ry / 2.0);
                        if (dx * dx + dy * dy < 1.0) {
                            for (int bz = minZ; bz <= maxZ; ++bz) {
                                final double dz = ((double) bz + 0.5 - cz) / (rxz / 2.0);
                                if (dx * dx + dy * dy + dz * dz < 1.0 && target.test(world.getBlock(bx, by, bz))) {
                                    world.setBlock(bx, by, bz, minableBlock);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
