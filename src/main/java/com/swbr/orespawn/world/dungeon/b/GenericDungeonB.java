package com.swbr.orespawn.world.dungeon.b;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.world.OreSpawnWorld;
import com.swbr.orespawn.world.structure.LegacyMeta;
import com.swbr.orespawn.world.structure.LegacyStructureRegistry;
import com.swbr.orespawn.world.structure.StructureWriter;
import com.swbr.orespawn.world.tree.LegacyRandom;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Port of the {@code GenericDungeon} methods whose bodies start on lines 2401-4800 (wave W13, porter
 * {@code w13-dungeon-b}): registration of their structure builders, the run-time entry points of
 * {@code DungeonSpawnerBlock}, and the small calls those methods made on the 1.7.10 world that have no
 * {@link StructureWriter} method of their own.
 *
 * <p>The block placement lives in three classes, split by original line range:
 * <ul>
 * <li>{@link GenericDungeonB1} (:2435-3216) - Gold Fish Bowl, Ender Reaper Graveyard (with {@code makeAGrave}),
 * Urchin Spawner, Spit Bug Lair, Igloo, Ender Dragon Hospital, Crystal Haunted House, Bouncy Castle;</li>
 * <li>{@link GenericDungeonB2} (:3218-4083) - Ender Castle (with {@code makeAColumn}), Damsel in Distress, Inca
 * Pyramid (with {@code makepoolalter}, {@code makeincagraves}, {@code makeincagrave});</li>
 * <li>{@link GenericDungeonB3} (:4085-4865) - Robot Lab (with its six {@code makerobo*} parts), King Altar (with
 * {@code makekingcolumn}, {@code makekingbackground}, {@code makekingcenteraltar}), Leonopteryx Nest, Cephadrome
 * Altar.</li>
 * </ul>
 *
 * <p>Every builder takes the writer and a {@link Random} instead of the {@code World}: {@code world.rand} of the
 * original is that random. In a structure piece it is the piece's seeded random (DECISIONS R18); at run time
 * ({@code DungeonSpawnerBlock}) it wraps {@code level.random} ({@link LegacyRandom}), as W12 did for
 * {@code RubyBirdDungeon}.
 *
 * <p>PORT: the three private helpers of the original these methods call - {@code FastSetBlock} (:219-221),
 * {@code getChestTileEntity} (:110-119), {@code getSpawnerTileEntity} (:121-130) - are one call each on
 * {@link StructureWriter} ({@code setBlock(x, y, z, state)}, {@code setChest}/{@code getTileEntity},
 * {@code setSpawner}); this package calls the writer directly instead of {@code GenericDungeonHelpers}, which was
 * written in parallel. {@code setThisBlock} is not used in this line range.
 */
public final class GenericDungeonB {

    private GenericDungeonB() {
    }

    // ------------------------------------------------------------------------------------------------------------
    // registration
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Replaces the W12 placeholders of this line range with the real builders (registering an id again replaces
     * the earlier builder). {@code MAKE_KING_ALTAR} is not registered here: its variant 1 is {@code makeQueenAltar}
     * (:5734, porter {@code w13-dungeon-c}); the integrator registers one dispatching builder for both.
     */
    public static void bootstrap() {
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_GOLD_FISH_BOWL,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeGoldFishBowl(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ENDER_REAPER_GRAVEYARD,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeEnderReaperGraveyard(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_URCHIN_SPAWNER,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeUrchinSpawner(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_SPIT_BUG_LAIR,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeSpitBugLair(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_IGLOO,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeIgloo(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ENDER_DRAGON_HOSPITAL,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeEnderDragonHospital(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_CRYSTAL_HAUNTED_HOUSE,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeCrystalHauntedHouse(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_BOUNCY_CASTLE,
                (world, random, x, y, z, variant) -> GenericDungeonB1.makeBouncyCastle(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ENDER_CASTLE,
                (world, random, x, y, z, variant) -> GenericDungeonB2.makeEnderCastle(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_DAMSEL_IN_DISTRESS,
                (world, random, x, y, z, variant) -> GenericDungeonB2.makeDamselInDistress(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_INCA_PYRAMID,
                (world, random, x, y, z, variant) -> GenericDungeonB2.makeIncaPyramid(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_ROBOT_LAB,
                (world, random, x, y, z, variant) -> GenericDungeonB3.makeRobotLab(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_LEON_NEST,
                (world, random, x, y, z, variant) -> GenericDungeonB3.makeLeonNest(world, random, x, y, z));
        LegacyStructureRegistry.registerBuilder(OreSpawnWorld.MAKE_CEPHADROME_ALTAR,
                (world, random, x, y, z, variant) -> GenericDungeonB3.makeCephadromeAltar(world, random, x, y, z));
        OreSpawn.LOG.debug("GenericDungeonB: 14 builders registered");
    }

    // ------------------------------------------------------------------------------------------------------------
    // run-time entry points (DungeonSpawnerBlock.updateTick, cases 17-20, 24-32, 34)
    // ------------------------------------------------------------------------------------------------------------

    private static StructureWriter live(final Level world) {
        return StructureWriter.unclipped(world);
    }

    private static Random rand(final Level world) {
        return new LegacyRandom(world.random);
    }

    /** {@code MyDungeon.makeGoldFishBowl(world, x, y, z)} on a live level (DungeonSpawnerBlock case 17). */
    public static void makeGoldFishBowl(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeGoldFishBowl(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 18. */
    public static void makeEnderReaperGraveyard(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeEnderReaperGraveyard(live(world), rand(world), x, y, z);
    }

    /** Not reachable from DungeonSpawnerBlock in the original; kept for symmetry with the world generator. */
    public static void makeUrchinSpawner(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeUrchinSpawner(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 19. */
    public static void makeSpitBugLair(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeSpitBugLair(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 20. */
    public static void makeIgloo(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeIgloo(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 24. */
    public static void makeEnderDragonHospital(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeEnderDragonHospital(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 25. */
    public static void makeCrystalHauntedHouse(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeCrystalHauntedHouse(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 26. */
    public static void makeBouncyCastle(final Level world, final int x, final int y, final int z) {
        GenericDungeonB1.makeBouncyCastle(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 27. */
    public static void makeEnderCastle(final Level world, final int x, final int y, final int z) {
        GenericDungeonB2.makeEnderCastle(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 28. */
    public static void makeDamselInDistress(final Level world, final int x, final int y, final int z) {
        GenericDungeonB2.makeDamselInDistress(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 29. */
    public static void makeIncaPyramid(final Level world, final int x, final int y, final int z) {
        GenericDungeonB2.makeIncaPyramid(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 30. */
    public static void makeRobotLab(final Level world, final int x, final int y, final int z) {
        GenericDungeonB3.makeRobotLab(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 31. */
    public static void makeKingAltar(final Level world, final int x, final int y, final int z) {
        GenericDungeonB3.makeKingAltar(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 32. */
    public static void makeLeonNest(final Level world, final int x, final int y, final int z) {
        GenericDungeonB3.makeLeonNest(live(world), rand(world), x, y, z);
    }

    /** DungeonSpawnerBlock case 34. */
    public static void makeCephadromeAltar(final Level world, final int x, final int y, final int z) {
        GenericDungeonB3.makeCephadromeAltar(live(world), rand(world), x, y, z);
    }

    // ------------------------------------------------------------------------------------------------------------
    // world calls without a writer method
    // ------------------------------------------------------------------------------------------------------------

    /** {@code FastSetBlock(world, x, y, z, block)} (:219-221) = {@code setBlockFast(block, 0, 2)}. */
    static void FastSetBlock(final StructureWriter world, final int ix, final int iy, final int iz, final BlockState id) {
        world.setBlock(ix, iy, iz, id, Block.UPDATE_CLIENTS);
    }

    /** {@code world.setBlock(x, y, z, block)}: the 1.7.10 three-argument form used flag 3. */
    static void setBlockNotify(final StructureWriter world, final int x, final int y, final int z, final BlockState state) {
        world.setBlock(x, y, z, state, Block.UPDATE_ALL);
    }

    /**
     * An OreSpawn block by registry path ({@code "blockeyeofender"}); air when it is not registered. For blocks
     * that are registered in every wave from W02 on (egg blocks, crystal blocks, torches); a block of a later wave
     * goes through {@link #optionalBlock}.
     */
    static BlockState block(final String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path)).defaultBlockState();
    }

    /** An OreSpawn block by registry path, empty while its wave is not ported. */
    static Optional<BlockState> optionalBlock(final String path) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path))
                .filter(b -> b != Blocks.AIR)
                .map(Block::defaultBlockState);
    }

    /**
     * {@code bid == Blocks.air} on a block read from the world. PORT: 1.7.10 had one air block; 1.21.1 splits it
     * into air, cave air and void air, all of which the original would have seen as {@code Blocks.air}.
     */
    static boolean isAir(final BlockState state) {
        return state.isAir();
    }

    /** {@code new ItemStack(item, count)}. */
    static ItemStack stack(final ItemLike item, final int count) {
        return new ItemStack(item, count);
    }

    /** {@code new ItemStack(OreSpawnMain.X, count)} by registry path; empty when the item is not registered yet. */
    static ItemStack stack(final String path, final int count) {
        final Optional<Item> item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, path));
        return item.filter(i -> i != Items.AIR).map(i -> new ItemStack(i, count)).orElse(ItemStack.EMPTY);
    }

    /**
     * {@code chest.setInventorySlotContents(slot, stack)} on the chest at the position, if it is there.
     * PORT: an empty stack (item of a wave that is not ported yet) leaves the slot as it is.
     */
    static void setSlot(final StructureWriter world, final int x, final int y, final int z, final int slot,
                        final ItemStack stack) {
        if (stack.isEmpty() || !world.contains(x, y, z)) {
            return;
        }
        if (world.getTileEntity(x, y, z) instanceof Container chest && slot < chest.getContainerSize()) {
            chest.setItem(slot, stack);
        }
    }

    /**
     * {@code Blocks.lit_redstone_lamp}: DECISIONS R18 keeps it as {@code redstone_lamp[lit=true]}; without power it
     * goes out on the first neighbour update, as the 1.7.10 lit lamp did.
     */
    static BlockState litRedstoneLamp() {
        return Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true);
    }

    /**
     * {@code Blocks.trapdoor} with metadata. Read from {@code BlockTrapDoor.func_150117_b} ({@code aoe.b(I)V},
     * client-1.7.10.jar, javap): bit 8 top half, bit 4 open, low bits the side the open door hugs - 0 z 1-f..1,
     * 1 z 0..f, 2 x 1-f..1, 3 x 0..f. Those are the open shapes of 1.21.1 facing north, south, west, east
     * ({@code TrapDoorBlock.*_OPEN_AABB}).
     */
    static BlockState trapdoor(final int meta) {
        final Direction facing = switch (meta & 3) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            default -> Direction.EAST;
        };
        return Blocks.OAK_TRAPDOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(TrapDoorBlock.OPEN, (meta & 4) != 0)
                .setValue(TrapDoorBlock.HALF, (meta & 8) != 0 ? Half.TOP : Half.BOTTOM);
    }

    /**
     * {@code ItemDoor.placeDoorBlock(world, x, y, z, dir, block)} ({@code ach.a(ahb, IIII, aji)V}, client-1.7.10.jar,
     * javap): the hinge goes right when only the left side holds a door of the same block, or when the right side has
     * more normal cubes than the left; lower half metadata {@code dir}, upper half {@code 8 | hinge}, both with
     * flag 2, then neighbour notifications.
     *
     * <p>Metadata to facing, read from {@code BlockDoor.func_150011_b} ({@code akn.b(I)V}): the closed door hugs
     * x 0..f for 0, z 0..f for 1, x 1-f..1 for 2, z 1-f..1 for 3 - the closed shapes of 1.21.1 facing east, south,
     * west, north ({@code DoorBlock.*_AABB}); the open shape with hinge bit 0 matches hinge left.
     *
     * <p>PORT: {@code Block.isNormalCube()} is {@code isRedstoneConductor}; the trailing
     * {@code notifyBlocksOfNeighborChange} calls are left to the level's own shape updates.
     */
    static void placeDoorBlock(final StructureWriter world, final int x, final int y, final int z, final int dir,
                               final Block block) {
        byte b0 = 0;
        byte b1 = 0;
        if (dir == 0) {
            b1 = 1;
        }
        if (dir == 1) {
            b0 = -1;
        }
        if (dir == 2) {
            b1 = -1;
        }
        if (dir == 3) {
            b0 = 1;
        }
        final int i1 = (isNormalCube(world, x - b0, y, z - b1) ? 1 : 0) + (isNormalCube(world, x - b0, y + 1, z - b1) ? 1 : 0);
        final int j1 = (isNormalCube(world, x + b0, y, z + b1) ? 1 : 0) + (isNormalCube(world, x + b0, y + 1, z + b1) ? 1 : 0);
        final boolean flag = world.is(x - b0, y, z - b1, block) || world.is(x - b0, y + 1, z - b1, block);
        final boolean flag1 = world.is(x + b0, y, z + b1, block) || world.is(x + b0, y + 1, z + b1, block);
        boolean flag2 = false;
        if (flag && !flag1) {
            flag2 = true;
        } else if (j1 > i1) {
            flag2 = true;
        }
        final Direction facing = switch (dir & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            default -> Direction.NORTH;
        };
        final BlockState lower = block.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HINGE, flag2 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        world.setBlock(x, y, z, lower, Block.UPDATE_CLIENTS);
        world.setBlock(x, y + 1, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
    }

    private static boolean isNormalCube(final StructureWriter world, final int x, final int y, final int z) {
        return world.getBlock(x, y, z).isRedstoneConductor(world.level(), new BlockPos(x, y, z));
    }

    /** {@code setBlockFast(block, meta, flags)} through {@link LegacyMeta#state}, for the families it knows. */
    static void setMeta(final StructureWriter world, final int x, final int y, final int z, final Block block, final int meta) {
        world.setBlock(x, y, z, LegacyMeta.state(block, meta), Block.UPDATE_CLIENTS);
    }
}
