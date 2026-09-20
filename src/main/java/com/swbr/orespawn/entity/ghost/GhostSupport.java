package com.swbr.orespawn.entity.ghost;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * The two 1.7.10 idioms {@link Ghost} and {@link GhostSkelly} repeat inline. The originals were two separate
 * classes with identical code; they stay separate, only the translation is shared.
 */
final class GhostSupport {

    private GhostSupport() {}

    /**
     * The spawner half of {@code getCanSpawnHere} (Ghost.java:149-163, GhostSkelly.java:178-192): a mob spawner in
     * x/z -2..+1, y 0..+4 around the spawn position whose entity is {@code entityId}.
     *
     * <p>A placement predicate receives a {@link BlockPos}; the mob sits at the block centre on x/z, so the
     * original's {@code (int) posX} is {@code Mth.floor(x + 0.5)} (R20: floor, not truncation). 1.7.10
     * {@code getEntityNameToSpawn()} ("Ghost") is the spawner's {@code SpawnData.entity.id}
     * ("orespawn:ghost"). The original dereferenced the tile entity without a check (R18 case 1): a missing or
     * foreign block entity simply does not match (W04 companions, W05 flyers).
     */
    static boolean spawnerNearby(final ServerLevelAccessor level, final BlockPos pos, final String entityId) {
        final int posX = Mth.floor(pos.getX() + 0.5);
        final int posY = pos.getY();
        final int posZ = Mth.floor(pos.getZ() + 0.5);
        for (int k = -2; k < 2; ++k) {
            for (int j = -2; j < 2; ++j) {
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
     * 1.7.10 {@code World.findNearestEntityWithinAABB(EntityPlayer.class, box, entity)}: every player in the box
     * except {@code entity}, the last of equally near ones wins ({@code <=}).
     *
     * <p>PORT: {@code getEntitiesOfClass} skips spectators, a game mode 1.7.10 did not have.
     */
    @Nullable
    static Player findNearestPlayer(final Entity entity, final AABB box) {
        Player entity1 = null;
        double d0 = Double.MAX_VALUE;
        for (final Player entity2 : entity.level().getEntitiesOfClass(Player.class, box)) {
            if ((Entity) entity2 != entity) {
                final double d1 = entity.distanceToSqr(entity2);
                if (d1 <= d0) {
                    entity1 = entity2;
                    d0 = d1;
                }
            }
        }
        return entity1;
    }
}
