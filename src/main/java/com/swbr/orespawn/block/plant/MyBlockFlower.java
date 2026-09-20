package com.swbr.orespawn.block.plant;

import com.swbr.orespawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;

/**
 * Port of {@code danger.orespawn.MyBlockFlower}, one class behind eight flowers
 * (OreSpawnMain.java:1614-1617, :1623-1626; hardness 0, grass sound, tab Decorations):
 *
 * <table>
 * <tr><th>id</th><th>original field</th><th>at night (t &gt; 12000)</th><th>by day</th></tr>
 * <tr><td>{@code flower_pink}</td><td>{@code MyFlowerPinkBlock}</td><td>-&gt; {@code flower_black}</td><td>-</td></tr>
 * <tr><td>{@code flower_blue}</td><td>{@code MyFlowerBlueBlock}</td><td>-&gt; {@code flower_scary}</td><td>-</td></tr>
 * <tr><td>{@code flower_black}</td><td>{@code MyFlowerBlackBlock}</td><td>-</td><td>-&gt; {@code flower_pink}</td></tr>
 * <tr><td>{@code flower_scary}</td><td>{@code MyFlowerScaryBlock}</td><td>-</td><td>-&gt; {@code flower_blue}</td></tr>
 * <tr><td>{@code crystalflower_red}</td><td>{@code CrystalFlowerRedBlock}</td><td colspan="2">never changes</td></tr>
 * <tr><td>{@code crystalflower_green}</td><td>{@code CrystalFlowerGreenBlock}</td><td colspan="2">never changes</td></tr>
 * <tr><td>{@code crystalflower_blue}</td><td>{@code CrystalFlowerBlueBlock}</td><td colspan="2">never changes</td></tr>
 * <tr><td>{@code crystalflower_yellow}</td><td>{@code CrystalFlowerYellowBlock}</td><td colspan="2">never changes</td></tr>
 * </table>
 *
 * A {@code Block implements IPlantable} of {@code Material.plants} (not {@code BlockBush}), random
 * ticks, bounds 0.3..0.7 in x/z and 0.6 high, no collision, cross model (MyBlockFlower.java:17-23,
 * :76-90). Plant type {@code Plains} (:97-99), so Forge's default soil answer was grass, dirt or
 * farmland; Crystal Grass says yes to everything. {@code canPlaceBlockOn} (:33-35) was dead code.
 *
 * <p>{@code checkFlowerChange} (:37-70) runs on every random tick and every neighbour change: no
 * support - drop and air (flag 2); otherwise the day/night swap above with {@code setBlock} (flag 3).
 */
public class MyBlockFlower extends Block {

    /** Which of the eight {@code this == OreSpawnMain.X} identities this instance is. */
    public enum Kind {
        PINK, BLUE, BLACK, SCARY, CRYSTAL_RED, CRYSTAL_GREEN, CRYSTAL_BLUE, CRYSTAL_YELLOW
    }

    /** {@code f = 0.2}: {@code setBlockBounds(0.5 - f, 0, 0.5 - f, 0.5 + f, f * 3, 0.5 + f)} (MyBlockFlower.java:20-21). */
    private static final VoxelShape SHAPE = Block.box(4.8, 0.0, 4.8, 11.2, 9.6, 11.2);

    private final Kind kind;

    public MyBlockFlower(Kind kind, Properties properties) {
        super(properties.noCollission().pushReaction(PushReaction.DESTROY));
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** {@code setTickRandomly(true)} (MyBlockFlower.java:19). */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** {@code canPlaceBlockAt} = replaceable target (the placement context's job now) and {@code canBlockStay} (:29-31). */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.canBlockStay(state, level, pos);
    }

    /**
     * {@code canBlockStay}: {@code world.getBlock(x, y - 1, z).canSustainPlant(world, x, y - 1, z, UP, this)}
     * (MyBlockFlower.java:72-74). A soil with an opinion decides; otherwise Forge 1.7.10's answer for
     * plant type {@code Plains}: grass, dirt or farmland. PORT (R22): "dirt" is {@code #minecraft:dirt}
     * through {@link ReedLikePlant#isDirt}.
     */
    private boolean canBlockStay(BlockState state, LevelReader level, BlockPos pos) {
        final BlockPos below = pos.below();
        final BlockState soil = level.getBlockState(below);
        final TriState soilDecision = soil.canSustainPlant(level, below, Direction.UP, state);
        if (!soilDecision.isDefault()) {
            return soilDecision.isTrue();
        }
        return ReedLikePlant.isGrass(soil) || ReedLikePlant.isDirt(soil) || ReedLikePlant.isFarmland(soil);
    }

    /** {@code onNeighborBlockChange}: {@code super}, then {@code checkFlowerChange} (MyBlockFlower.java:37-40). */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        this.checkFlowerChange(state, level, pos);
    }

    /** {@code updateTick}: {@code checkFlowerChange} (MyBlockFlower.java:42-44). */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.checkFlowerChange(state, level, pos);
    }

    /** {@code checkFlowerChange} (MyBlockFlower.java:46-70). */
    protected final void checkFlowerChange(BlockState state, Level level, BlockPos pos) {
        if (!this.canBlockStay(state, level, pos)) {
            dropResources(state, level, pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            return;
        }
        long t = level.getDayTime();
        t %= 24000L;
        if (t > 12000L) {
            if (this.kind == Kind.PINK) {
                level.setBlock(pos, ModBlocks.FLOWER_BLACK.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            if (this.kind == Kind.BLUE) {
                level.setBlock(pos, ModBlocks.FLOWER_SCARY.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        } else {
            if (this.kind == Kind.BLACK) {
                level.setBlock(pos, ModBlocks.FLOWER_PINK.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            if (this.kind == Kind.SCARY) {
                level.setBlock(pos, ModBlocks.FLOWER_BLUE.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }
}
