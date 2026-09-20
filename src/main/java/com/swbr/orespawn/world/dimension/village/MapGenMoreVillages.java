package com.swbr.orespawn.world.dimension.village;

import com.swbr.orespawn.OreSpawn;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Port of {@code MapGenMoreVillages} (MapGenMoreVillages.java:6-38), the denser vanilla village generator of
 * VillageMania ({@code ChunkProviderOreSpawn3}, :49, :167, :280, :361; verhalten/world-02.md).
 *
 * <p>The class only overrode {@code canSpawnStructureAtCoords}: a 9-chunk grid instead of 32, minimum distance 7
 * instead of 8, salt 10387312, and the biome check computed but ignored (:33-34). Village size and style stayed
 * vanilla. In 1.21.1 that is data, not code:
 * <ul>
 * <li>{@code data/orespawn/worldgen/structure_set/village_mania.json}: {@code random_spread}, spacing 9,
 * separation 7, salt 10387312, spread type linear. {@code RandomSpreadStructurePlacement.getPotentialStructureChunk}
 * floor-divides the region, seeds {@code WorldgenRandom.setLargeFeatureWithSalt} with
 * {@code x * 341873128712 + z * 132897987541 + seed + salt} (WorldgenRandom.java:96-99) - the formula of 1.7.10
 * {@code World.setRandomSeed} - and draws {@code nextInt(spacing - separation)} for x, then z, on the same LCG.
 * The grid positions are therefore <em>identical</em> to 1.7.10 for the same seed; {@link #canSpawnStructureAtCoords}
 * keeps the original algorithm so a test can compare both.</li>
 * <li>The structure {@code orespawn:village_mania} is a copy of {@code minecraft:village_plains} whose biome list
 * is {@code orespawn:villages} alone ({@code BiomeManager.addVillageBiome}, WorldProviderOreSpawn3.java:31). It
 * must not be added to the vanilla tag {@code has_structure/village_plains}: the vanilla set (spacing 34) would
 * then run in this dimension too, and the dense set could reach the overworld plains.</li>
 * </ul>
 * PORT: 1.21.1 jigsaw villages are not the 1.7.10 {@code StructureVillagePieces}; they look different. Not
 * avoidable without own jigsaw pools (verhalten/world-02.md).
 */
public final class MapGenMoreVillages {

    public static final ResourceKey<Structure> STRUCTURE =
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "village_mania"));

    /** {@code field_82665_g} (:12): grid size in chunks. */
    public static final int SPACING = 9;
    /** {@code field_82666_h} (:13): minimum distance. */
    public static final int SEPARATION = 7;
    /** {@code setRandomSeed(var5, var6, 10387312)} (:27). */
    public static final int SALT = 10387312;

    private MapGenMoreVillages() {
    }

    /**
     * {@code canSpawnStructureAtCoords} (:16-37), with {@code World.setRandomSeed} written out. The biome check
     * {@code areBiomesViable} (:33) had no effect on the result and is not ported.
     */
    public static boolean canSpawnStructureAtCoords(final long worldSeed, int par1, int par2) {
        final int var3 = par1;
        final int var4 = par2;
        if (par1 < 0) {
            par1 -= SPACING - 1;
        }
        if (par2 < 0) {
            par2 -= SPACING - 1;
        }
        int var5 = par1 / SPACING;
        int var6 = par2 / SPACING;
        final Random var7 = new Random((long) var5 * 341873128712L + (long) var6 * 132897987541L + worldSeed + (long) SALT);
        var5 *= SPACING;
        var6 *= SPACING;
        var5 += var7.nextInt(SPACING - SEPARATION);
        var6 += var7.nextInt(SPACING - SEPARATION);
        return var3 == var5 && var4 == var6;
    }

    /**
     * The result of {@code villageGenerator.generateStructuresInChunk(world, rand, x, z)} (ChunkProviderOreSpawn3.java:280):
     * true when a village start's bounding box intersects the populate area {@code (x * 16 + 8 .. + 23)}.
     * 1.7.10 also placed the pieces there; in 1.21.1 {@code applyBiomeDecoration} does that per chunk.
     *
     * <p>A start that intersects the area is referenced by one of the four chunks under it. Starts are read only
     * within the region's structure-start radius; a village's bounding box is far smaller than that radius, so no
     * intersecting start is missed.
     *
     * <p>PORT: 1.7.10 skipped village starts with two or fewer components ({@code isSizeableStructure}); 1.21.1
     * has no such flag and counts every valid start.
     *
     * @param chunkX 1.7.10 chunk x of the populated chunk
     * @param chunkZ 1.7.10 chunk z of the populated chunk
     */
    public static boolean generateStructuresInChunk(final WorldGenLevel level, final StructureManager structureManager,
                                                    final int chunkX, final int chunkZ) {
        final Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        final Structure structure = registry.get(STRUCTURE);
        if (structure == null) {
            return false;
        }
        final int minX = (chunkX << 4) + 8;
        final int minZ = (chunkZ << 4) + 8;
        final int maxX = minX + 15;
        final int maxZ = minZ + 15;
        final LongSet seen = new LongOpenHashSet();
        for (int dx = 0; dx <= 1; ++dx) {
            for (int dz = 0; dz <= 1; ++dz) {
                final ChunkAccess chunk = level.getChunk(chunkX + dx, chunkZ + dz, ChunkStatus.STRUCTURE_REFERENCES);
                for (final long reference : chunk.getReferencesForStructure(structure)) {
                    if (!seen.add(reference)) {
                        continue;
                    }
                    final int startX = ChunkPos.getX(reference);
                    final int startZ = ChunkPos.getZ(reference);
                    if (!level.hasChunk(startX, startZ)) {
                        continue;
                    }
                    final StructureStart start = structureManager.getStartForStructure(
                            SectionPos.of(new ChunkPos(reference), level.getMinSection()), structure,
                            level.getChunk(startX, startZ, ChunkStatus.STRUCTURE_STARTS));
                    if (start != null && start.isValid() && start.getBoundingBox().intersects(minX, minZ, maxX, maxZ)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
