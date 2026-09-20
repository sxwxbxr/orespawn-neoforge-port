package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code OreRuby} (OreRuby.java:12-43): Ruby Ore, id {@code oreruby} (OreSpawnMain.java:1520).
 *
 * <p>Drops are the loot table {@code orespawn:blocks/oreruby}: {@code ruby} times {@code 1 + nextInt(2)}
 * ({@code quantityDroppedWithBonus}, :31-33 - the fortune level was ignored, so no {@code apply_bonus}),
 * and the ore itself under Silk Touch ({@code canSilkHarvest} was true for a normal block).
 * {@code quantityDropped(Random) = 1} (:35-37) was never reached in 1.7.10.
 */
public class OreRuby extends Block {

    public OreRuby(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code Material.rock}, hardness 10, resistance 4 (:15-17). */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(10.0f, Legacy.resistance(4.0f))
                .requiresCorrectToolForDrops();
    }

    /**
     * {@code dropBlockAsItemWithChance} (:21-25): always {@code 5 + nextInt(5) + nextInt(5)} experience,
     * at any height, on every drop path including explosions. Silk Touch skipped the method in 1.7.10
     * ({@code canSilkHarvest} true for a normal cube); {@code stack} carries that here.
     *
     * <p>PORT: {@code dropExperience} is ignored - see {@link Legacy#dropXpOnBlockBreak}.
     */
    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        int j1 = 5 + level.random.nextInt(5) + level.random.nextInt(5);
        Legacy.dropXpOnBlockBreak(this, level, pos, stack, j1);
    }
}
