package com.swbr.orespawn.block.workshop;

import com.mojang.serialization.MapCodec;
import com.swbr.orespawn.block.entity.TileEntityCrystalFurnace;
import com.swbr.orespawn.block.misc.Legacy;
import com.swbr.orespawn.registry.ModBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Port of {@code danger.orespawn.CrystalFurnace} (CrystalFurnace.java:19-221): the Crystal Furnace,
 * {@code crystalfurnace}. The original was two {@code BlockContainer}s sharing that id -
 * {@code CrystalFurnaceBlock} (off, OreSpawnMain.java:1533, legacy id +212, tab Decorations) and
 * {@code CrystalFurnaceOnBlock} (on, :1534, +213, light 0.6, no tab) - swapped by
 * {@code updateFurnaceBlockState} (:71-87) with the {@code keepFurnaceInventory} flag holding the
 * tile entity across the swap. DECISIONS R2 makes that <b>one</b> block with
 * {@link BlockStateProperties#LIT}; the flag becomes the {@code state.is(newState.getBlock())}
 * test in {@link #onRemove}, which is what the flag protected against.
 *
 * <p>Both variants used {@code crystalfurnace_front_off} as the front texture (:67 - the ternary
 * has the same string in both branches); {@code crystalfurnace_front_on.png} sits in the jar and
 * is never loaded. 1:1 (DECISIONS R18): the {@code lit=true} blockstate points at the same model.
 *
 * <p>Not opaque, not a normal cube (:42-48): {@link Legacy#notANormalCube} plus the light and
 * support-shape overrides below, as for the W02 crystal blocks.
 */
public class CrystalFurnace extends BaseEntityBlock {

    public static final MapCodec<CrystalFurnace> CODEC = simpleCodec(CrystalFurnace::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    /** {@code furnaceRand} (:21, :31), the block's own source for the break-drop scatter. */
    private final RandomSource furnaceRand = RandomSource.create();

    public CrystalFurnace(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    /**
     * {@code Material.rock}, hardness 2, resistance 10 (:30, :38-39; OreSpawnMain.java:1533-1534
     * {@code (…, 2.0f, 10.0f)}), light 0.6 on the lit variant only (:36). Rock needed a pickaxe
     * in 1.7.10 ({@code Material.rock.setRequiresTool()}), any level - the {@code mineable/pickaxe}
     * tag without a {@code needs_*_tool} tag says the same (W02 convention).
     */
    public static BlockBehaviour.Properties originalProperties() {
        int lit = Legacy.light(0.6f); // 9
        return Legacy.notANormalCube(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0f, Legacy.resistance(10.0f))
                .lightLevel(state -> state.getValue(LIT) ? lit : 0)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<CrystalFurnace> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    /** {@code BaseEntityBlock} defaults to INVISIBLE; the furnace is a model like any other block. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // --- isOpaqueCube / renderAsNormalBlock false (:42-48), see block.tree.CrystalCube for the table ---

    /** {@code isNormalCube() == false}: nothing attaches to its faces. */
    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    /** {@code lightOpacity} was 0 because {@code isOpaqueCube()} was false at construction. */
    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    // ---------------------------------------------------------------------------------------------
    // Orientation (:89-115, :156-173)
    // ---------------------------------------------------------------------------------------------

    /**
     * {@code onBlockPlacedBy} (:156-169): {@code l = floor(yaw * 4 / 360 + 0.5) & 3}, then
     * 0 → meta 2 (north), 1 → 5 (east), 2 → 3 (south), 3 → 4 (west) - the front faces the placer.
     * {@code Direction.fromYRot} is the same quantisation ({@code floor(yaw / 90 + 0.5) & 3} →
     * south, west, north, east) and {@code getOpposite()} the same table, so the vanilla placement
     * idiom is the original formula. The custom name (:170-172) reaches the block entity through
     * {@code BlockItem.updateCustomBlockEntityTag} → {@code BaseContainerBlockEntity} (CUSTOM_NAME
     * component); no override needed.
     *
     * <p>PORT: {@code onBlockAdded} → {@code setDefaultDirection} (:89-115) ran on every
     * {@code setBlock} first and was then overwritten by the yaw. Its only lasting effect was on
     * placements without a placer (GenericDungeon.java:3056); those callers take
     * {@link #defaultDirection} instead of a nested {@code setBlock} inside {@code onPlace}, which
     * in 1.21.1 would swallow the outer call's neighbour notifications.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * {@code setDefaultDirection} (:94-115) for placements without a placer: the front turns away
     * from the single full-cube neighbour on an axis - meta 3 (south) when only the north
     * neighbour is full, 2 (north) for south, 5 (east) for west, 4 (west) for east; default 3
     * (south). {@code func_149730_j} = {@code isFullBlock} ({@code Block.isOpaqueCube()} in
     * 1.7.10, MCP {@code fields.csv}) → {@code isSolidRender}. Later checks override earlier ones,
     * as in the original.
     */
    public static BlockState defaultDirection(LevelReader level, BlockPos pos, BlockState state) {
        boolean north = level.getBlockState(pos.north()).isSolidRender(level, pos.north()); // l  = z - 1
        boolean south = level.getBlockState(pos.south()).isSolidRender(level, pos.south()); // i1 = z + 1
        boolean west = level.getBlockState(pos.west()).isSolidRender(level, pos.west());    // j1 = x - 1
        boolean east = level.getBlockState(pos.east()).isSolidRender(level, pos.east());    // k1 = x + 1
        Direction facing = Direction.SOUTH; // b0 = 3
        if (north && !south) {
            facing = Direction.SOUTH; // 3
        }
        if (south && !north) {
            facing = Direction.NORTH; // 2
        }
        if (west && !east) {
            facing = Direction.EAST; // 5
        }
        if (east && !west) {
            facing = Direction.WEST; // 4
        }
        return state.setValue(FACING, facing);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ---------------------------------------------------------------------------------------------
    // Interaction (:117-126)
    // ---------------------------------------------------------------------------------------------

    /** {@code onBlockActivated} (:117-126): the server opens GUI 0 if the tile entity exists; always "handled". */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof TileEntityCrystalFurnace furnace) {
            player.openMenu(furnace, pos);
        }
        return InteractionResult.CONSUME;
    }

    // ---------------------------------------------------------------------------------------------
    // Particles (:128-154), client only
    // ---------------------------------------------------------------------------------------------

    /**
     * {@code randomDisplayTick} (:128-154) of the lit variant. Kept as written (DECISIONS R18):
     * where vanilla's furnace pushes the particles 0.52 out of the front face, the original draws
     * <em>both</em> offsets from {@code nextFloat() * 0.6 - 0.3} (:135-136), so smoke and flame
     * appear within 0.3 of the block centre - mostly inside the block. No crackle sound.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        Direction l = state.getValue(FACING);
        float f = pos.getX() + 0.5f;
        float f2 = pos.getY() + 0.0f + random.nextFloat() * 6.0f / 16.0f;
        float f3 = pos.getZ() + 0.5f;
        float f4 = random.nextFloat() * 0.6f - 0.3f;
        float f5 = random.nextFloat() * 0.6f - 0.3f;
        double x;
        double z;
        if (l == Direction.WEST) {        // meta 4 (:137-140)
            x = f - f4;
            z = f3 + f5;
        } else if (l == Direction.EAST) { // meta 5 (:141-144)
            x = f + f4;
            z = f3 + f5;
        } else if (l == Direction.NORTH) { // meta 2 (:145-148)
            x = f + f5;
            z = f3 - f4;
        } else {                           // meta 3, south (:149-152)
            x = f + f5;
            z = f3 + f4;
        }
        level.addParticle(ParticleTypes.SMOKE, x, f2, z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x, f2, z, 0.0, 0.0, 0.0);
    }

    // ---------------------------------------------------------------------------------------------
    // Breaking (:175-208) and comparator (:210-216)
    // ---------------------------------------------------------------------------------------------

    /**
     * {@code breakBlock} (:175-208): unless the block is only being swapped (the
     * {@code keepFurnaceInventory} case - here a LIT change, {@code state.is(newState.getBlock())}),
     * throw every slot out in pieces of {@code nextInt(21) + 10} at a random point 0.1-0.9 inside
     * the block with a Gaussian × 0.05 motion (+0.2 up), then update comparators. Written out
     * rather than {@code Containers.dropContents}, whose scatter (0.125-0.875, triangular motion)
     * differs. No experience is paid here - the original tracked none (1:1).
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.is(newState.getBlock())) {
            return; // :176 keepFurnaceInventory: the LIT swap keeps the block entity
        }        if (level.getBlockEntity(pos) instanceof TileEntityCrystalFurnace furnace) {
            for (int j1 = 0; j1 < furnace.getContainerSize(); ++j1) {
                ItemStack itemstack = furnace.getItem(j1);
                if (itemstack.isEmpty()) {
                    continue;
                }
                float f = this.furnaceRand.nextFloat() * 0.8f + 0.1f;  // :182
                float f2 = this.furnaceRand.nextFloat() * 0.8f + 0.1f; // :183
                float f3 = this.furnaceRand.nextFloat() * 0.8f + 0.1f; // :184
                while (!itemstack.isEmpty()) {                          // :185
                    int k1 = this.furnaceRand.nextInt(21) + 10;         // :186
                    if (k1 > itemstack.getCount()) {                    // :187-189
                        k1 = itemstack.getCount();
                    }
                    ItemStack piece = itemstack.split(k1);              // :190-195 same item, count k1, NBT copied
                    ItemEntity entityitem = new ItemEntity(level, pos.getX() + f, pos.getY() + f2, pos.getZ() + f3, piece);
                    float f4 = 0.05f;                                   // :196
                    entityitem.setDeltaMovement(
                            (float) this.furnaceRand.nextGaussian() * f4,        // :197
                            (float) this.furnaceRand.nextGaussian() * f4 + 0.2f, // :198
                            (float) this.furnaceRand.nextGaussian() * f4);       // :199
                    level.addFreshEntity(entityitem);                   // :200
                }
            }
            level.updateNeighbourForOutputSignal(pos, this); // :204 func_147453_f
        }
        super.onRemove(state, level, pos, newState, movedByPiston); // :207 removes the block entity
    }

    /** {@code hasComparatorInputOverride} (:210-212). */
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    /** {@code getComparatorInputOverride} (:214-216): {@code Container.calcRedstoneFromInventory}. */
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    // ---------------------------------------------------------------------------------------------
    // Block entity (:218-220)
    // ---------------------------------------------------------------------------------------------

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityCrystalFurnace(pos, state);
    }

    /** Server ticker only; see {@link TileEntityCrystalFurnace#serverTick}. */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.CRYSTAL_FURNACE.get(), TileEntityCrystalFurnace::serverTick);
    }
}
