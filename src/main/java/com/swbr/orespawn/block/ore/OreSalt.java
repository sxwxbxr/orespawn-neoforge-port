package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import com.swbr.orespawn.entity.portal.EntityAnt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code OreSalt} (OreSalt.java:12-37): Salt Ore, id {@code oresalt} (OreSpawnMain.java:1502).
 * Every ant that touches or walks on it takes 5 cactus damage (:21-31). Drops itself.
 */
public class OreSalt extends Block {

    public OreSalt(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.rock}, hardness 5, resistance 2 (:15-17). */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, Legacy.resistance(2.0f))
                .requiresCorrectToolForDrops();
    }

    /** {@code onEntityCollidedWithBlock} (:21-25). */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isAnt(entity)) {
            entity.hurt(level.damageSources().cactus(), 5.0f);
        }
    }

    /** {@code onEntityWalking} (:27-31); see {@link OreTitanium#stepOn} for the step-vs-tick note. */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (isAnt(entity)) {
            entity.hurt(level.damageSources().cactus(), 5.0f);
        }
    }

    /**
     * {@code entity instanceof EntityAnt} - the base class of Termite, Red, Rainbow and Unstable Ant
     * (OreSalt.java:21-31).
     */
    private static boolean isAnt(Entity entity) {
        return entity instanceof EntityAnt;
    }
}
