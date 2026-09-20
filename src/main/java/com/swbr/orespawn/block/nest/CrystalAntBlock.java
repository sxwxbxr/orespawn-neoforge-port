package com.swbr.orespawn.block.nest;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.block.misc.Legacy;
import com.swbr.orespawn.block.misc.NonOpaqueBlock;
import com.swbr.orespawn.config.OreSpawnConfig;
import com.swbr.orespawn.config.stats.MobSwitches;
import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code danger.orespawn.CrystalAntBlock} (CrystalAntBlock.java:19-120): {@code crystaltermiteblock}
 * Crystal Termite Nest (OreSpawnMain.java:5946, registered :1850, +121, {@code CreativeTabs.tabBlock}).
 * The Crystal dimension's termite nest: every random tick without rain and with air on top it
 * releases 2 to 7 Termites while {@code TermiteEnable} is on. No population cap.
 *
 * <p>The tick copies AntBlock's identity chain (:57-83). Only its {@code this == CrystalTermiteBlock}
 * branch can match: {@code MyAntBlock}, {@code MyRedAntBlock}, {@code MyUnstableAntBlock} and
 * {@code TermiteBlock} are {@code AntBlock} instances, and the Rainbow Ant {@code else} comes after the
 * matching branch. The port keeps that one branch.
 *
 * <p>{@code Block(Material.grass)}, no hardness (0, breaks at once), stone sound (the {@code Block}
 * default), {@code isOpaqueCube} and {@code renderAsNormalBlock} false (:106-112): light passes
 * ({@link NonOpaqueBlock}), no face culling, nothing spawns on top, no redstone, no suffocation
 * ({@link Legacy#notANormalCube}); cutout rendering per DECISIONS R18. No colour override, so no tint.
 * Drops itself (:91-93).
 */
public class CrystalAntBlock extends NonOpaqueBlock {

    public CrystalAntBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** {@code CrystalAntBlock(int)} (:21-24): Material grass, {@code setTickRandomly(true)}. */
    public static BlockBehaviour.Properties originalProperties() {
        return Legacy.notANormalCube(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .instabreak()
                .randomTicks());
    }

    /** {@code updateTick} (:48-89), server only, the live branch (:78-82). */
    @Override
    protected void randomTick(final BlockState state, final ServerLevel par1World, final BlockPos pos, final RandomSource par5Random) {
        int howmany = 0;
        if (!par1World.isClientSide) {
            if (par1World.isRaining()) {
                return;
            }
            // PORT: "bid == Blocks.air" is isAir(), as in AntBlock.
            final BlockState bid = par1World.getBlockState(pos.above());
            if (bid.isAir()) {
                howmany = OreSpawn.OreSpawnRand.nextInt(6) + 2;
                for (int i = 0; i < howmany; ++i) {
                    if (MobSwitches.enabled(OreSpawnConfig.MOBS.TermiteEnable)) {
                        AntBlock.spawnCreature(par1World, ModEntities.TERMITE.get(),
                                pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
                    }
                }
            }
        }
    }
}
