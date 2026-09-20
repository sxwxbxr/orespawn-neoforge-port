package com.swbr.orespawn.entity.insect;

import com.swbr.orespawn.OreSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

/**
 * Helpers the four flyers of W05 ({@link EntityButterfly}, {@link EntityLunaMoth}, {@link EntityMosquito},
 * {@link Firefly}) and their spawn plants share. No original class: each piece stands for a 1.7.10 idiom
 * the originals repeated inline.
 */
public final class InsectSupport {

    /**
     * {@code OreSpawnMain.DimensionID4}, the Islands dimension ({@code orespawn:danger}, DECISIONS R13):
     * the vampire butterfly lives there and the spawn rules accept any height in it.
     * PORT: a local key like {@code OreSpawnLeaves.DIMENSION_ISLANDS}; switch to the dimension holder if
     * the W05 integrator introduces one.
     */
    public static final ResourceKey<Level> DIMENSION_ISLANDS = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "danger"));

    /** {@code OreSpawnMain.DimensionID6}, the Chaos dimension ({@code orespawn:chaos}), the butterfly's destination. */
    public static final ResourceKey<Level> DIMENSION_CHAOS = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "chaos"));

    private InsectSupport() {
    }

    /**
     * 1.7.10 {@code World.isDaytime()} = {@code skylightSubtracted < 4} and nothing else (same reading as
     * {@code MyEntityAIFollowOwner}, W04). {@code Level.isDay()} would additionally answer {@code false} in
     * every fixed-time dimension, which the original did not.
     */
    public static boolean isDaytime(final Level level) {
        return level.getSkyDarken() < 4;
    }

    /**
     * {@code ChunkCoordinates.getDistanceSquared(int, int, int)}: the three differences as {@code float},
     * squared and summed.
     */
    public static float getDistanceSquared(final BlockPos c, final int x, final int y, final int z) {
        final float f = (float) (c.getX() - x);
        final float f1 = (float) (c.getY() - y);
        final float f2 = (float) (c.getZ() - z);
        return f * f + f1 * f1 + f2 * f2;
    }

    /**
     * The spawner scan of {@code EntityButterfly.getCanSpawnHere} (EntityButterfly.java:274-289): a
     * {@code mob_spawner} within x/z -3..+2, y 0..+4 of the (already floored) position whose
     * {@code getEntityNameToSpawn()} is the given entity. 1.7.10 "Butterfly" is the spawner's
     * {@code SpawnData.entity.id} {@code "orespawn:butterfly"} today. The original dereferenced the tile
     * entity without a null check (R18 case 1): a missing or foreign block entity simply does not match.
     */
    public static boolean spawnerNearby(final LevelAccessor level, final int posX, final int posY, final int posZ,
                                        final String entityId) {
        for (int k = -3; k < 3; ++k) {
            for (int j = -3; j < 3; ++j) {
                for (int i = 0; i < 5; ++i) {
                    final BlockPos at = new BlockPos(posX + j, posY + i, posZ + k);
                    if (level.getBlockState(at).is(Blocks.SPAWNER)) {
                        final BlockEntity tileentitymobspawner = level.getBlockEntity(at);
                        if (tileentitymobspawner instanceof SpawnerBlockEntity spawner) {
                            final String s = spawner.getSpawner().save(new CompoundTag())
                                    .getCompound("SpawnData").getCompound("entity").getString("id");
                            if (s.equals(entityId)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * The move helper of a 1.7.10 flyer. {@code EntityLiving.updateAITasks} ran
     * {@code moveHelper.onUpdateMoveHelper()} (which zeroes {@code moveForward}) <em>before</em> the
     * subclass code that set {@code moveForward = 0.5f}, so the forward push reached
     * {@code moveEntityWithHeading}. 1.21.1 runs {@code customServerAiStep} first and
     * {@code MoveControl.tick} after it, whose idle branch sets {@code zza = 0} and would erase the push.
     *
     * <p>PORT: the idle ({@code WAIT}) branch is skipped; every other operation (a later subclass that
     * navigates, such as Mothra in W08) runs the vanilla control unchanged.
     */
    public static final class LegacyMoveControl extends MoveControl {

        public LegacyMoveControl(final Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.WAIT) {
                return;
            }
            super.tick();
        }
    }
}
