package com.swbr.orespawn.world.structure;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * 1.7.10 block metadata to 1.21.1 {@link BlockState}: the conversion every ported builder needs when the
 * original wrote {@code setBlock(x, y, z, block, meta, flags)} (GenericDungeon, Trees, BasiliskMaze,
 * RubyBirdDungeon, OreSpawnWorld). No original class; catalogue 6.4 ("Meta-Konvention von Fackel, Hebel,
 * Knopf, Treppe und Kolben").
 *
 * <p>Every convention below is read from the bytecode of {@code reference/jar/mcp/client-1.7.10.jar} with
 * {@code javap -c} (class names from {@code joined.srg}):
 * <table>
 * <tr><th>1.7.10</th><th>bytecode</th><th>meta</th></tr>
 * <tr><td>{@code BlockTorch} ({@code aoc})</td><td>{@code onBlockPlaced} {@code a(ahb,IIIIFFFI)I}: side 1 → 5,
 * side 2 (solid at z+1) → 4, side 3 (z-1) → 3, side 4 (x+1) → 2, side 5 (x-1) → 1; bounds {@code a(ahb,III,azw,azw)}:
 * meta 1 hugs x 0..0.3, meta 2 x 0.7..1, meta 3 z 0..0.3, meta 4 z 0.7..1</td><td>1 wall, points east; 2 west;
 * 3 south; 4 north; anything else standing</td></tr>
 * <tr><td>{@code BlockStairs} ({@code ans})</td><td>{@code onBlockPlacedBy}: yaw quadrant 0/1/2/3 → meta 2/1/3/0
 * with bit 4 kept; collision {@code f(ahl,III)}: meta 0 raises the half x 0.5..1, bit 4 swaps the halves</td>
 * <td>low two bits: 0 east, 1 west, 2 south, 3 north (the tall back side); bit 4 upside down</td></tr>
 * <tr><td>{@code BlockLever} ({@code alv})</td><td>{@code onBlockPlaced}: side 0 → 0, 1 → 5, 2 → 4, 3 → 3, 4 → 2,
 * 5 → 1; {@code onBlockPlacedBy}: floor 5 becomes 6 and ceiling 0 becomes 7 on an odd yaw quadrant</td>
 * <td>1-4 wall as the torch; 5 floor along z, 6 floor along x; 7 ceiling along z, 0 ceiling along x; bit 8
 * powered</td></tr>
 * <tr><td>{@code BlockButton} ({@code ajs})</td><td>{@code onBlockPlaced}: side 2 → 4, 3 → 3, 4 → 2, 5 → 1</td>
 * <td>1-4 wall as the torch (no floor or ceiling buttons in 1.7.10); bit 8 pressed</td></tr>
 * <tr><td>{@code BlockPistonBase} ({@code app})</td><td>{@code b(I)I} = {@code meta & 7};
 * {@code determineOrientation} returns the {@code Facing} index 0-5</td><td>0 down, 1 up, 2 north, 3 south,
 * 4 west, 5 east; bit 8 extended</td></tr>
 * <tr><td>{@code BlockChest} ({@code ajx})</td><td>{@code onBlockPlacedBy}: yaw quadrant 0/1/2/3 → 2/5/3/4</td>
 * <td>{@code Facing} index of the front: 2 north, 3 south, 4 west, 5 east. Ladder, furnace, dispenser and
 * ender chest share the index</td></tr>
 * </table>
 *
 * <p>The {@code Facing} index order (down, up, north, south, west, east) is also the 1.21.1
 * {@link Direction#from3DDataValue} order.
 */
public final class LegacyMeta {

    private LegacyMeta() {
    }

    /**
     * The state {@code setBlock(block, meta)} would have produced, for the block families above; any other
     * block keeps its default state (metadata that meant nothing or means a different block now needs an
     * explicit helper such as {@link #wool}).
     */
    public static BlockState state(final Block block, final int meta) {
        if (block instanceof StairBlock) {
            return stairs(block, meta);
        }
        if (block instanceof LeverBlock) {
            return lever(meta);
        }
        if (block instanceof ButtonBlock) {
            return button(block, meta);
        }
        if (block instanceof PistonBaseBlock) {
            return piston(block, meta);
        }
        if (block instanceof BaseTorchBlock && !(block instanceof WallTorchBlock)) {
            return torch(block, meta);
        }
        if (block instanceof RotatedPillarBlock) {
            return pillar(block, meta);
        }
        if (block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof AbstractFurnaceBlock
                || block instanceof LadderBlock || block instanceof WallSignBlock) {
            return horizontalFacing(block, meta);
        }
        if (block instanceof DispenserBlock) {
            return block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.from3DDataValue(meta & 7));
        }
        if (block instanceof CarvedPumpkinBlock) {
            return pumpkin(block, meta);
        }
        if (block == Blocks.PUMPKIN) {
            // The 1.7.10 pumpkin always had a face; the 1.13 flattening turned it into carved_pumpkin[facing].
            return pumpkin(Blocks.CARVED_PUMPKIN, meta);
        }
        return block.defaultBlockState();
    }

    /**
     * {@code BlockTorch} metadata: 1 east, 2 west, 3 south, 4 north on the wall; everything else (5, and 0 which
     * {@code onBlockAdded} re-placed) standing.
     *
     * <p>The wall variant: vanilla {@code torch}/{@code redstone_torch}/{@code soul_torch} → their
     * {@code *_wall_torch}; OreSpawn torches → {@code <id>_wall} (DECISIONS R18, Fackel-Ids).
     */
    public static BlockState torch(final Block standing, final int meta) {
        final Direction facing = wallFacing(meta);
        if (facing == null) {
            return standing.defaultBlockState();
        }
        return wallTorchOf(standing)
                .map(wall -> wall.defaultBlockState().hasProperty(WallTorchBlock.FACING)
                        ? wall.defaultBlockState().setValue(WallTorchBlock.FACING, facing)
                        : wall.defaultBlockState())
                .orElseGet(standing::defaultBlockState);
    }

    /** {@code BlockStairs} metadata: low bits 0 east, 1 west, 2 south, 3 north; bit 4 top half. */
    public static BlockState stairs(final Block stairs, final int meta) {
        final Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return stairs.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
    }

    /**
     * {@code BlockLever} metadata. The 1.8 names of the eight orientations (down_x, east, west, south, north,
     * up_z, up_x, down_z) carry over to the flattened lever as face plus facing.
     */
    public static BlockState lever(final int meta) {
        final int o = meta & 7;
        BlockState state = Blocks.LEVER.defaultBlockState();
        final Direction wall = wallFacing(o);
        if (wall != null) {
            state = state.setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL)
                    .setValue(FaceAttachedHorizontalDirectionalBlock.FACING, wall);
        } else if (o == 5 || o == 6) {
            state = state.setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.FLOOR)
                    .setValue(FaceAttachedHorizontalDirectionalBlock.FACING, o == 5 ? Direction.NORTH : Direction.EAST);
        } else {
            state = state.setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.CEILING)
                    .setValue(FaceAttachedHorizontalDirectionalBlock.FACING, o == 7 ? Direction.NORTH : Direction.EAST);
        }
        return state.setValue(LeverBlock.POWERED, (meta & 8) != 0);
    }

    /** {@code BlockButton} metadata: 1 east, 2 west, 3 south, 4 north; bit 8 pressed. */
    public static BlockState button(final Block button, final int meta) {
        final Direction wall = wallFacing(meta & 7);
        BlockState state = button.defaultBlockState()
                .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.WALL);
        if (wall != null) {
            state = state.setValue(FaceAttachedHorizontalDirectionalBlock.FACING, wall);
        }
        // PORT: a 1.7.10 button with metadata 0, 5, 6 or 7 had no valid orientation (it popped off on the next
        // neighbour update); the port keeps the default wall facing for it.
        return state.setValue(ButtonBlock.POWERED, (meta & 8) != 0);
    }

    /** {@code BlockPistonBase} metadata: {@code Facing} index in the low three bits, bit 8 extended. */
    public static BlockState piston(final Block piston, final int meta) {
        return piston.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.from3DDataValue(meta & 7))
                .setValue(PistonBaseBlock.EXTENDED, (meta & 8) != 0);
    }

    /**
     * Chest, ender chest, furnace, ladder and every other block with a horizontal front: metadata 2-5 is the
     * {@code Facing} index. PORT: metadata 0 and 1 had no front in 1.7.10; they become south, the orientation the
     * 1.7.10 chest renderer used for a chest without rotation (not checked in bytecode).
     */
    public static BlockState horizontalFacing(final Block block, final int meta) {
        final Direction facing = meta >= 2 && meta <= 5 ? Direction.from3DDataValue(meta) : Direction.SOUTH;
        return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    /**
     * {@code BlockPumpkin} ({@code amw}, also the jack o'lantern): {@code onBlockPlacedBy} stores
     * {@code floor(yaw * 4 / 360 + 2.5) & 3}, {@code getIcon} shows the face on side 3/4/2/5 for metadata 0/1/2/3 -
     * south, west, north, east, the 1.21.1 {@link Direction#from2DDataValue} order.
     */
    public static BlockState pumpkin(final Block pumpkin, final int meta) {
        return pumpkin.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.from2DDataValue(meta & 3));
    }

    /** The chest a builder writes with {@code setBlockMetadataWithNotify(x, y, z, meta, 3)}. */
    public static BlockState chest(final int meta) {
        return horizontalFacing(Blocks.CHEST, meta);
    }

    /** {@code BlockRotatedPillar}/{@code BlockLog}: bits 4/8 the axis (0 y, 4 x, 8 z, 12 bark all round → y). */
    public static BlockState pillar(final Block block, final int meta) {
        final int axis = meta & 12;
        final Direction.Axis a = axis == 4 ? Direction.Axis.X : axis == 8 ? Direction.Axis.Z : Direction.Axis.Y;
        return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, a);
    }

    /** {@code Blocks.log} metadata 0-3 (oak, spruce, birch, jungle) plus the axis bits. */
    public static BlockState log(final int meta) {
        final Block block = switch (meta & 3) {
            case 1 -> Blocks.SPRUCE_LOG;
            case 2 -> Blocks.BIRCH_LOG;
            case 3 -> Blocks.JUNGLE_LOG;
            default -> Blocks.OAK_LOG;
        };
        return pillar(block, meta);
    }

    /** {@code Blocks.planks} metadata 0-5 (oak, spruce, birch, jungle, acacia, dark oak). */
    public static BlockState planks(final int meta) {
        return (switch (meta & 7) {
            case 1 -> Blocks.SPRUCE_PLANKS;
            case 2 -> Blocks.BIRCH_PLANKS;
            case 3 -> Blocks.JUNGLE_PLANKS;
            case 4 -> Blocks.ACACIA_PLANKS;
            case 5 -> Blocks.DARK_OAK_PLANKS;
            default -> Blocks.OAK_PLANKS;
        }).defaultBlockState();
    }

    /** {@code Blocks.wool} metadata: the dye id, whose order 1.21.1 kept (0 white … 14 red, 15 black). */
    public static BlockState wool(final int meta) {
        return colored("wool", meta);
    }

    /** {@code Blocks.carpet} metadata, as wool. */
    public static BlockState carpet(final int meta) {
        return colored("carpet", meta);
    }

    /** {@code Blocks.stained_glass} metadata, as wool. */
    public static BlockState stainedGlass(final int meta) {
        return colored("stained_glass", meta);
    }

    /** {@code Blocks.stained_hardened_clay} metadata, as wool; the block is called terracotta now. */
    public static BlockState stainedClay(final int meta) {
        return colored("terracotta", meta);
    }

    private static BlockState colored(final String suffix, final int meta) {
        final DyeColor color = DyeColor.byId(meta & 15);
        final ResourceLocation id = ResourceLocation.withDefaultNamespace(color.getName() + "_" + suffix);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR).defaultBlockState();
    }

    /** Torch, lever and button wall metadata 1-4; {@code null} for everything else. */
    private static Direction wallFacing(final int meta) {
        return switch (meta) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.WEST;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.NORTH;
            default -> null;
        };
    }

    private static Optional<Block> wallTorchOf(final Block standing) {
        if (standing == Blocks.TORCH) {
            return Optional.of(Blocks.WALL_TORCH);
        }
        if (standing == Blocks.REDSTONE_TORCH) {
            return Optional.of(Blocks.REDSTONE_WALL_TORCH);
        }
        if (standing == Blocks.SOUL_TORCH) {
            return Optional.of(Blocks.SOUL_WALL_TORCH);
        }
        final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(standing);
        return BuiltInRegistries.BLOCK.getOptional(id.withSuffix("_wall"));
    }
}
