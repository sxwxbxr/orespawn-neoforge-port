package com.swbr.orespawn.world.dungeon.c;

import com.swbr.orespawn.world.structure.StructureWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Port of the 1.7.10 vanilla {@code ItemDoor.placeDoorBlock(World, x, y, z, side, Block)} ({@code ach.a}, MCP
 * {@code func_150924_a}) for the GenericDungeon builders of porter c (Greenhouse :5168-5169, White House :5587).
 *
 * <p>Read from {@code reference/jar/mcp/client-1.7.10.jar} with {@code javap -c}:
 * <ul>
 * <li>side 0 → (dx, dz) = (0, 1), 1 → (-1, 0), 2 → (0, -1), 3 → (1, 0);</li>
 * <li>{@code i1} = solid normal cubes ({@code Block.isNormalCube}, {@code aji.r}) at (x - dx, y, z - dz) and one above,
 * {@code j1} the same at (x + dx, z + dz); {@code flag} / {@code flag1} = the same door block on either side;</li>
 * <li>mirrored hinge if {@code flag && !flag1}, otherwise if {@code j1 > i1};</li>
 * <li>{@code setBlock(x, y, z, block, side, 2)}, {@code setBlock(x, y + 1, z, block, 8 | mirrored, 2)}, then
 * {@code notifyBlocksOfNeighborChange} on both halves.</li>
 * </ul>
 *
 * <p>Lower-half metadata to facing, from {@code BlockDoor.func_150011_b} ({@code akn.b(I)V}, the bounds of a closed
 * door): meta 0 fills x 0..0.1875 (the west edge), 1 z 0..0.1875 (north edge), 2 the east edge, 3 the south edge. A
 * closed 1.21.1 {@link DoorBlock} of facing east/south/west/north occupies the west/north/east/south edge
 * ({@code DoorBlock.EAST_AABB} etc.), so meta 0/1/2/3 is east/south/west/north. Upper bit 1 = right hinge.
 *
 * <p>PORT: the two {@code notifyBlocksOfNeighborChange} calls are not ported. The writer has no neighbour-update
 * path, and in a generation region there is no live neighbour to notify; the only effect in 1.7.10 was a power check
 * of nearby doors and redstone, of which the builders place none next to the door.
 */
final class LegacyDoor {

    private LegacyDoor() {
    }

    static void placeDoorBlock(final StructureWriter world, final int x, final int y, final int z, final int side,
                               final Block block) {
        int b0 = 0;
        int b1 = 0;
        if (side == 0) {
            b1 = 1;
        }
        if (side == 1) {
            b0 = -1;
        }
        if (side == 2) {
            b1 = -1;
        }
        if (side == 3) {
            b0 = 1;
        }
        final int i1 = (isNormalCube(world.getBlock(x - b0, y, z - b1)) ? 1 : 0)
                + (isNormalCube(world.getBlock(x - b0, y + 1, z - b1)) ? 1 : 0);
        final int j1 = (isNormalCube(world.getBlock(x + b0, y, z + b1)) ? 1 : 0)
                + (isNormalCube(world.getBlock(x + b0, y + 1, z + b1)) ? 1 : 0);
        final boolean flag = world.is(x - b0, y, z - b1, block) || world.is(x - b0, y + 1, z - b1, block);
        final boolean flag1 = world.is(x + b0, y, z + b1, block) || world.is(x + b0, y + 1, z + b1, block);
        boolean flag2 = false;
        if (flag && !flag1) {
            flag2 = true;
        } else if (j1 > i1) {
            flag2 = true;
        }
        final Direction facing = facing(side);
        final DoorHingeSide hinge = flag2 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
        // PORT: 1.7.10 kept the hinge only in the upper half; a 1.21.1 door carries it in both halves.
        final BlockState lower = block.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, hinge)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        world.setBlock(x, y, z, lower, Block.UPDATE_CLIENTS);
        world.setBlock(x, y + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
    }

    /** Lower door metadata 0-3 as the facing of the closed door (class javadoc). */
    static Direction facing(final int meta) {
        return switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    /**
     * {@code Block.isNormalCube()}: opaque material, renders as a normal block, no power source. The 1.21.1 counterpart
     * is the redstone-conductor predicate (full collision cube, {@code never} for redstone blocks and glass), asked
     * without a level because the 1.7.10 check was a property of the block alone.
     */
    private static boolean isNormalCube(final BlockState state) {
        return state.isRedstoneConductor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }
}
