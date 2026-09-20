package com.swbr.orespawn.block.island;

import com.swbr.orespawn.block.plant.ReedLikePlant;
import com.swbr.orespawn.config.stats.TweakStats;
import com.swbr.orespawn.registry.ModEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code danger.orespawn.IslandBlock} (IslandBlock.java:13-86): Island Block ({@code island},
 * OreSpawnMain.java:1591, legacy id +193, light 0.9; registered :1841). A {@code BlockReed} that, on a random tick,
 * releases one to three floating islands 12 to 75 blocks above itself and vanishes (verhalten/itemblock-03.md,
 * "IslandBlock").
 *
 * <ul>
 *   <li>Bounds 0.125..0.875, full height (:16-17) - {@link ReedLikePlant#SHAPE}; random ticks (:18); tab
 *       Decorations (:19).</li>
 *   <li>Placement and survival: a solid block below (:22-24); {@code getMaterial().isSolid()} is
 *       {@link BlockState#isSolid()}, as in RTPBlock and OreSpawnTeleporter.</li>
 *   <li>Drops one of itself (:73-79) - loot table {@code blocks/island}. Pick block is sugar cane, inherited from
 *       {@code BlockReed.getItem} (see {@link ReedLikePlant}).</li>
 * </ul>
 */
public class IslandBlock extends ReedLikePlant {

    public IslandBlock(final Properties properties) {
        super(properties);
    }

    /**
     * Registration properties. {@code BlockReed}'s constructor set {@code Material.plants} and left hardness 0 and
     * {@code Block}'s default step sound, stone (vanilla's own reeds got grass on their {@code Blocks} line, :1591
     * sets only the light); {@code setLightLevel(0.9f)} is {@code (int) (15 * 0.9f)} = 13.
     */
    public static BlockBehaviour.Properties originalProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak().sound(SoundType.STONE).lightLevel(s -> 13);
    }

    /** {@code canPlaceBlockAt} (:22-24): {@code getBlock(x, y - 1, z).getMaterial().isSolid()}. */
    @Override
    @SuppressWarnings("deprecation") // BlockState.isSolid() is the 1.7.10 Material.isSolid()
    protected boolean mayPlaceOn(final BlockState soil) {
        return soil.isSolid();
    }

    /** {@code randomDisplayTick} (:26-33): 1/20, twenty happy-villager particles; {@code par1World.rand}. */
    @Override
    public void animateTick(final BlockState state, final Level par1World, final BlockPos pos, final RandomSource par5Random) {
        final int par2 = pos.getX();
        final int par3 = pos.getY();
        final int par4 = pos.getZ();
        if (par1World.random.nextInt(20) != 1) {
            return;
        }
        for (int j1 = 0; j1 < 20; ++j1) {
            par1World.addParticle(ParticleTypes.HAPPY_VILLAGER, par2 + par1World.random.nextFloat(),
                    par3 + (double) par1World.random.nextFloat(), par4 + par1World.random.nextFloat(), 0.0, 0.0, 0.0);
        }
    }

    /**
     * {@code updateTick} (:35-71), random ticks only: 1-3 tries at height {@code 12 + rand(m)} (m 45/55/64 by
     * {@code IslandSizeFactor} 1/2/other); a try whose 21x21 layer is all air spawns an Island (1/25) or an IslandToo
     * at the block's integer coordinates. Then the block and the block above become air, flag 2.
     */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel par1World, final BlockPos pos, final RandomSource par5Random) {
        final int par2 = pos.getX();
        final int par3 = pos.getY();
        final int par4 = pos.getZ();
        int isok = 1;
        if (par1World.isClientSide) {
            return;
        }
        final int n = 1 + par1World.random.nextInt(3);
        int m = 64;
        if (TweakStats.IslandSizeFactor() == 2) {
            m = 55;
        }
        if (TweakStats.IslandSizeFactor() == 1) {
            m = 45;
        }
        for (int i = 0; i < n; ++i) {
            final int height = 12 + par1World.random.nextInt(m);
            isok = 1;
            for (int k = -10; k <= 10; ++k) {
                for (int j = -10; j <= 10; ++j) {
                    final BlockState bid = par1World.getBlockState(new BlockPos(par2 + j, par3 + height, par4 + k));
                    // bid != Blocks.air: isAir() covers cave and void air, one block in 1.7.10.
                    if (!bid.isAir()) {
                        isok = 0;
                        // Breaks only the inner loop, as in the original (:55-57).
                        break;
                    }
                }
            }
            if (isok != 0) {
                if (par1World.random.nextInt(25) == 1) {
                    spawnCreature(par1World, ModEntities.ISLAND.get(), par2, par3 + height, par4);
                } else {
                    spawnCreature(par1World, ModEntities.ISLAND_TOO.get(), par2, par3 + height, par4);
                }
            }
        }
        par1World.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        par1World.setBlock(new BlockPos(par2, par3 + 1, par4), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    /**
     * {@code spawnCreature} (:81-90): create by type (no {@code onSpawnWithEgg}), random yaw, add, living sound.
     * The original looked the entity up by its {@code registerModEntity} name ("Island", "IslandToo").
     */
    @Nullable
    public static Entity spawnCreature(final Level par0World, final EntityType<?> par1, final double par2,
                                       final double par4, final double par6) {
        Entity var8 = null;
        var8 = par1.create(par0World);
        if (var8 != null) {
            var8.moveTo(par2, par4, par6, par0World.random.nextFloat() * 360.0f, 0.0f);
            par0World.addFreshEntity(var8);
            if (var8 instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }
        return var8;
    }
}
