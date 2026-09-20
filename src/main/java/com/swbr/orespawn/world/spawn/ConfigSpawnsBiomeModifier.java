package com.swbr.orespawn.world.spawn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Biome modifier type {@code orespawn:config_spawns} (DECISIONS R11): the port of every
 * {@code EntityRegistry.addSpawn} call in {@code OreSpawnMain.make_some_more_things} (OreSpawnMain.java:4181-4641)
 * and of the spawn lists of {@code BiomeGenUtopianPlains} (constructor :10-56, {@code setIslandCreatures} :62-119,
 * {@code setCrystalCreatures} :121-183, {@code setVillageCreatures} :192-252, {@code setChaosCreatures} :254-436)
 * plus the dino list {@code ChunkProviderOreSpawn2.getPossibleCreatures} (:349-413).
 *
 * <p>Table keys are biome ids or biome tags ({@code "#minecraft:is_nether"}, DECISIONS R26); a biome takes the
 * entries of its id and of every tag it is in.
 *
 * <p>JSON: {@code data/orespawn/neoforge/biome_modifier/config_spawns.json},
 * {@code {"type": "orespawn:config_spawns", "table": "orespawn:spawns"}}. The table is a generated data file
 * ({@link SpawnTable}); a datapack JSON alone cannot read the {@code *Enable} switches or the calendar, which is
 * why this is Java at all.
 *
 * <p>1.7.10 built the lists once: {@code addSpawn} while the mod loaded, the biome lists when a
 * {@code WorldProvider} was created. 1.21.1 rebuilds biomes on every server start, and so does this
 * modifier - the switches and the date are read at that moment (R18), never per tick.
 *
 * <p>The category of each entry is that of the 1.7.10 list, not the entity type's own category (R18,
 * biome_map.json): {@code spawnableCaveCreatureList} is {@code AMBIENT}, even for Chaos' T-Rex.
 *
 * <p>What the modifier does not do: the OreSpawn dimension biomes keep their vanilla base lists from the biome
 * JSON (W05), Islands, Crystal and Chaos ship those lists empty because their setters cleared them
 * ({@code spawnableCreatureList = new ArrayList()}, :63-70, :122-129, :255-262). Spawn placement predicates
 * ({@code getCanSpawnHere}) are registered per entity by their waves; {@link ChunkGenerationSpawns} handles the
 * chunk generation pass.
 *
 * @param table id of {@code data/<ns>/spawn_table/<path>.json}
 */
public record ConfigSpawnsBiomeModifier(ResourceLocation table) implements BiomeModifier {

    /** Registered as {@code orespawn:config_spawns}; {@link #codec()} must return this very instance. */
    public static final MapCodec<ConfigSpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("table").forGetter(ConfigSpawnsBiomeModifier::table)
    ).apply(i, ConfigSpawnsBiomeModifier::new));

    @Override
    public void modify(final Holder<Biome> biome, final Phase phase, final ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) {
            return;
        }
        if (biome.unwrapKey().isEmpty()) {
            return;
        }
        final SpawnTable spawnTable = SpawnTable.forServer(ServerLifecycleHooks.getCurrentServer(), this.table);
        final MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
        // fix2 (BUGHUNT2 2.2, R26): the biome's id key plus every tag key it is in (hell = #minecraft:is_nether),
        // merged by the R18 rule when several apply (SpawnTable.entries(Holder)).
        for (final SpawnTable.Entry entry : spawnTable.entries(biome)) {
            if (!entry.active()) {
                continue;
            }
            final EntityType<?> type = entry.resolve();
            if (type == null) {
                // Every table entry resolves since W11; an unknown id (entry.todo() names a wave) is still
                // skipped and logged once per server start by SpawnTable.
                continue;
            }
            if (type.getCategory() == MobCategory.MISC) {
                // PORT: SpawnerData replaces a MISC type with EntityType.PIG (MobSpawnSettings.java:162). No
                // spawnable OreSpawn entity is MISC today; the guard keeps a wrong registration from spawning pigs.
                continue;
            }
            spawns.addSpawn(entry.category(), new MobSpawnSettings.SpawnerData(type, entry.weight(), entry.min(), entry.max()));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
