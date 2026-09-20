package com.swbr.orespawn.block.ore;

import com.swbr.orespawn.block.misc.Legacy;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

/**
 * Port of {@code OreBasicStone} (OreBasicStone.java:12-78), five blocks:
 * <ul>
 *   <li>{@code crystalstone} Kyanite (OreSpawnMain.java:1526, {@code (…, 2.0f, 10.0f)}), no critter</li>
 *   <li>{@code crystalrat} Crystalized Rats (:1537, {@code 2.5f, 14.0f}), {@code 1 + nextInt(10)} Rats</li>
 *   <li>{@code crystalfairy} Crystalized Fairies (:1538), {@code 1 + nextInt(6)} Fairies</li>
 *   <li>{@code redanttroll} Red Ant Troll Block (:1539), {@code 15 + nextInt(6)} Red Ants</li>
 *   <li>{@code termitetroll} Termite Troll Block (:1540), {@code 15 + nextInt(6)} Termites</li>
 * </ul>
 * The original chose the branch with {@code this == OreSpawnMain.CrystalRat} etc. (:23-42); here each
 * registration passes its critter and the {@code base + nextInt(bound)} pair. No chance roll, no config
 * check, creative mode included - as written.
 *
 * <p>PORT (R18 case 3): {@code isOpaqueCube} and {@code renderAsNormalBlock} returned
 * {@code current_dimension == DimensionID5} (:50-56), a client static W01 dropped - the block was a
 * normal opaque cube inside the Crystal dimension, where it lives, and see-through elsewhere; on a
 * dedicated server it was never "normal". A block's occlusion cannot depend on the viewer's dimension in
 * 1.21.1, so the Crystal-dimension state is the one kept: a plain opaque cube. The constructor-time
 * light opacity 0 of the original (see {@code NonOpaqueBlock}) is dropped with it, because the light
 * engine ignores it for occluding blocks. {@code isBlockSolidOnSide} (:46-48) was dead code.
 */
public class OreBasicStone extends Block {

    @Nullable
    private final Supplier<EntityType<? extends Mob>> critter;
    private final int base;
    private final int bound;

    /**
     * @param critter what the block releases when a player breaks it, or {@code null} for Kyanite
     * @param base    the constant part of the count
     * @param bound   the {@code nextInt} bound of the count
     */
    public OreBasicStone(BlockBehaviour.Properties properties, @Nullable Supplier<EntityType<? extends Mob>> critter,
            int base, int bound) {
        super(properties);
        this.critter = critter;
        this.base = base;
        this.bound = bound;
    }

    /** Kyanite: no critter, no spawn (:22-44 has no branch for it). */
    public OreBasicStone(BlockBehaviour.Properties properties) {
        this(properties, null, 0, 1);
    }

    /** Constructor {@code (int, float f1, float f2)} (:14-20): {@code Material.rock}, hardness, resistance, no random ticks. */
    public static BlockBehaviour.Properties originalProperties(float f1, float f2) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(f1, Legacy.resistance(f2))
                .requiresCorrectToolForDrops();
    }

    /**
     * {@code onBlockDestroyedByPlayer} (:22-44). The block is air before the spawn loop, as in 1.7.10
     * ({@code removedByPlayer} ran first); server side only, both game modes.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (!level.isClientSide && this.critter != null) {
            RandomSource rand = level.random;
            for (int num = this.base + rand.nextInt(this.bound), i = 0; i < num; ++i) {
                spawnCreature(level, this.critter.get(),
                        pos.getX() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 0.2,
                        pos.getY() + 0.01,
                        pos.getZ() + 0.5 + (rand.nextFloat() - rand.nextFloat()) * 0.2);
            }
        }
        return removed;
    }

    /**
     * {@code spawnCreature} (:58-72): create by type, random yaw, add to the level, ambient sound. No
     * {@code finalizeSpawn} - the original did not call {@code onSpawnWithEgg} either. Public static like
     * the original.
     */
    @Nullable
    public static Entity spawnCreature(Level level, EntityType<? extends Mob> type, double x, double y, double z) {
        Mob entity = type.create(level);
        if (entity != null) {
            entity.moveTo(x, y, z, level.random.nextFloat() * 360.0f, 0.0f);
            level.addFreshEntity(entity);
            entity.playAmbientSound();
        }
        return entity;
    }
}
