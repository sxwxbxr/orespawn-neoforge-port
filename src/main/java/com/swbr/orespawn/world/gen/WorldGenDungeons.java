package com.swbr.orespawn.world.gen;

import com.swbr.orespawn.OreSpawn;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Port of the 1.7.10 vanilla {@code WorldGenDungeons} ({@code asd}, client-1.7.10.jar), used eight times per
 * populate by {@code ChunkProviderOreSpawn2} (ChunkProviderOreSpawn2.java:306-311) and {@code ChunkProviderOreSpawn3}
 * (ChunkProviderOreSpawn3.java:302-307). Room 5..7 x 4 x 5..7 of cobblestone with a mossy floor, one to five
 * wall openings required, up to two chests, one spawner.
 *
 * <p>PORT, three differences, all in the loot and spawner step:
 * <ul>
 * <li>The chest contents were rolled from {@code WorldGenDungeons.field_111189_a} plus a random enchanted book
 * with the populate random; the port sets the 1.21.1 loot table {@code minecraft:chests/simple_dungeon}
 * (DECISIONS R10: chest generation hooks become loot tables). Every random number the 1.7.10 filling drew is
 * therefore not drawn, and the rest of that chunk's decoration diverges from 1.7.10 after a chest.</li>
 * <li>The chest facing came from {@code BlockChest.func_149954_e} when the block was added; the port uses
 * {@link StructurePiece#reorient}, 1.21.1's successor of that rule (face away from the single solid neighbour).</li>
 * <li>The spawner mob comes from the vanilla {@code pickMobSpawner} in the reference jar ({@code nextInt(4)}:
 * skeleton, zombie, zombie, spider). Forge 1.7.10 routed it through {@code DungeonHooks} with the same weights
 * 100/200/100; that jar is not in the repository, so its random draw is not verified.</li>
 * </ul>
 */
public class WorldGenDungeons extends WorldGenerator {

    private static final BlockState MOSSY_COBBLESTONE = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
    private static final BlockState COBBLESTONE = Blocks.COBBLESTONE.defaultBlockState();

    @Override
    public boolean generate(final LegacyWorld world, final Random rand, final int x, final int y, final int z) {
        final int height = 3;
        final int radiusX = rand.nextInt(2) + 2;
        final int radiusZ = rand.nextInt(2) + 2;
        int openings = 0;
        for (int bx = x - radiusX - 1; bx <= x + radiusX + 1; ++bx) {
            for (int by = y - 1; by <= y + height + 1; ++by) {
                for (int bz = z - radiusZ - 1; bz <= z + radiusZ + 1; ++bz) {
                    final boolean solid = LegacyWorld.isSolid(world.getBlock(bx, by, bz));
                    if (by == y - 1 && !solid) {
                        return false;
                    }
                    if (by == y + height + 1 && !solid) {
                        return false;
                    }
                    if ((bx == x - radiusX - 1 || bx == x + radiusX + 1 || bz == z - radiusZ - 1 || bz == z + radiusZ + 1)
                            && by == y && world.isAirBlock(bx, by, bz) && world.isAirBlock(bx, by + 1, bz)) {
                        ++openings;
                    }
                }
            }
        }
        if (openings < 1 || openings > 5) {
            return false;
        }
        for (int bx = x - radiusX - 1; bx <= x + radiusX + 1; ++bx) {
            for (int by = y + height; by >= y - 1; --by) {
                for (int bz = z - radiusZ - 1; bz <= z + radiusZ + 1; ++bz) {
                    if (bx != x - radiusX - 1 && by != y - 1 && bz != z - radiusZ - 1 && bx != x + radiusX + 1
                            && by != y + height + 1 && bz != z + radiusZ + 1) {
                        world.setBlockToAir(bx, by, bz);
                    } else if (by >= 0 && !LegacyWorld.isSolid(world.getBlock(bx, by - 1, bz))) {
                        world.setBlockToAir(bx, by, bz);
                    } else if (LegacyWorld.isSolid(world.getBlock(bx, by, bz))) {
                        if (by == y - 1 && rand.nextInt(4) != 0) {
                            world.setBlock(bx, by, bz, MOSSY_COBBLESTONE);
                        } else {
                            world.setBlock(bx, by, bz, COBBLESTONE);
                        }
                    }
                }
            }
        }
        for (int chest = 0; chest < 2; ++chest) {
            for (int attempt = 0; attempt < 3; ++attempt) {
                final int cx = x + rand.nextInt(radiusX * 2 + 1) - radiusX;
                final int cy = y;
                final int cz = z + rand.nextInt(radiusZ * 2 + 1) - radiusZ;
                if (!world.isAirBlock(cx, cy, cz)) {
                    continue;
                }
                int solidNeighbours = 0;
                if (LegacyWorld.isSolid(world.getBlock(cx - 1, cy, cz))) {
                    ++solidNeighbours;
                }
                if (LegacyWorld.isSolid(world.getBlock(cx + 1, cy, cz))) {
                    ++solidNeighbours;
                }
                if (LegacyWorld.isSolid(world.getBlock(cx, cy, cz - 1))) {
                    ++solidNeighbours;
                }
                if (LegacyWorld.isSolid(world.getBlock(cx, cy, cz + 1))) {
                    ++solidNeighbours;
                }
                if (solidNeighbours != 1) {
                    continue;
                }
                final BlockPos chestPos = new BlockPos(cx, cy, cz);
                world.setBlock(cx, cy, cz, StructurePiece.reorient(world.level(), chestPos, Blocks.CHEST.defaultBlockState()));
                RandomizableContainer.setBlockEntityLootTable(world.level(), world.level().getRandom(), chestPos,
                        BuiltInLootTables.SIMPLE_DUNGEON);
                break;
            }
        }
        world.setBlock(x, y, z, Blocks.SPAWNER.defaultBlockState());
        final BlockPos spawnerPos = new BlockPos(x, y, z);
        if (world.level().getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(pickMobSpawner(rand), world.level().getRandom());
        } else {
            OreSpawn.LOG.error("Failed to fetch mob spawner entity at ({}, {}, {})", x, y, z);
        }
        return true;
    }

    /** {@code asd.a(Random)} ({@code pickMobSpawner}). */
    private static EntityType<?> pickMobSpawner(final Random rand) {
        final int i = rand.nextInt(4);
        return switch (i) {
            case 0 -> EntityType.SKELETON;
            case 3 -> EntityType.SPIDER;
            default -> EntityType.ZOMBIE;
        };
    }
}
