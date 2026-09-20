package com.swbr.orespawn.item.utility;

import com.swbr.orespawn.registry.ModItems;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.InstantShelter} (InstantShelter.java:14-137): {@code instantshelter}
 * "Instant Survival Shelter" (OreSpawnMain.java:1598), stack 16, creative tab {@code tabRedstone}
 * (:16-19). One click builds a 7x7 hut of five levels around the <em>player</em> - cobblestone
 * floor, plank walls with a glass row, plank roof, a door hole in the wall the player clicked
 * towards - and furnishes it with a furnace, a crafting table and a stocked chest.
 */
public class InstantShelter extends Item {

    /** {@code InstantShelter(id)} (:16-19): {@code maxStackSize = 16}. */
    public InstantShelter(final Item.Properties props) {
        super(props.stacksTo(16));
    }

    /**
     * {@code onItemUse} (:21-131). The clicked block must share a row or column with the player
     * and lie on exactly one axis away from him; that axis is the door side and {@code stuffdir}
     * the facing of furnace and chest (:40-67). Centre is the player, floor level is one below his
     * feet (:43-45, :68-69). Sound on both sides, then the client is done (:70-73).
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final Player player = context.getPlayer();
        final ItemStack par1ItemStack = context.getItemInHand();
        final BlockPos clicked = context.getClickedPos();
        final int cposx = clicked.getX();
        final int cposz = clicked.getZ();
        if (player == null) {
            // PORT: no player in 1.21.1 (dispenser-like callers); the original always had one.
            return InteractionResult.PASS;
        }
        int deltax = 0;
        int deltaz = 0;
        int stuffdir = 0;
        final int width;
        final int length = width = 3;
        final int height = 3; // :28-30
        final int pposx = OneUse.legacyFloor(player.getX(), cposx); // :31-37
        final int pposy = Mth.floor(player.getY()); // :38 (int) -> Mth.floor (DECISIONS R20) - server posY is the feet in 1.7.10 as in 1.21.1
        final int pposz = OneUse.legacyFloor(player.getZ(), cposz); // :39
        if (cposx - pposx != 0 && cposz - pposz != 0) { // :40-42
            return InteractionResult.PASS;
        }
        int x = cposx;
        final int y = pposy - 1;
        int z = cposz; // :43-45
        if (x - pposx < 0) { // :46-49
            deltax = -1;
            stuffdir = 3;
        }
        if (x - pposx > 0) { // :50-53
            deltax = 1;
            stuffdir = 2;
        }
        if (z - pposz < 0) { // :54-57
            deltaz = -1;
            stuffdir = 5;
        }
        if (z - pposz > 0) { // :58-61
            deltaz = 1;
            stuffdir = 4;
        }
        if (deltax == 0 && deltaz == 0) { // :62-64
            return InteractionResult.PASS;
        }
        if (deltax != 0 && deltaz != 0) { // :65-67
            return InteractionResult.PASS;
        }
        x = pposx;
        z = pposz; // :68-69
        OneUse.playExplode(world, player, 1.0f, 1.5f); // :70
        if (world.isClientSide) { // :71-73
            return InteractionResult.sidedSuccess(true);
        }
        // :74-99 - world.setBlock(x, y, z, block) is flag 3 (neighbour + client updates).
        // PORT: planks meta 0 -> oak planks.
        final BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
        final BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        final BlockState glass = Blocks.GLASS.defaultBlockState();
        final BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = -width; i <= width; ++i) {
            for (int j = -length; j <= length; ++j) {
                for (int k = 0; k <= height + 1; ++k) {
                    if (k == height + 1) { // :77-79 roof
                        FastBlocks.setBlockFast(world, x + i, y + k, z + j, planks, Block.UPDATE_ALL);
                    } else if (k == 0) { // :80-82 floor
                        FastBlocks.setBlockFast(world, x + i, y + k, z + j, cobblestone, Block.UPDATE_ALL);
                    } else if (i == width || j == length || i == -width || j == -length) { // :83 perimeter
                        if (k == height) { // :84-86 glass row
                            FastBlocks.setBlockFast(world, x + i, y + k, z + j, glass, Block.UPDATE_ALL);
                        } else if ((k == 1 || k == 2) && i == deltax * width && j == deltaz * length) { // :87-89 door
                            FastBlocks.setBlockFast(world, x + i, y + k, z + j, air, Block.UPDATE_ALL);
                        } else { // :90-92
                            FastBlocks.setBlockFast(world, x + i, y + k, z + j, planks, Block.UPDATE_ALL);
                        }
                    } else { // :94-96 interior
                        FastBlocks.setBlockFast(world, x + i, y + k, z + j, air, Block.UPDATE_ALL);
                    }
                }
            }
        }
        // :100-109 furniture: the row (x + i*dx + j*dz, y + 1, z + i*dz + j*dx) with j = length - 1,
        // i.e. against the side wall to the right of the door, i = 2, 1, 0. stuffdir 2..5 is the
        // 1.7.10 block metadata north/south/west/east = Direction.from3DDataValue(2..5), which
        // faces the row into the room; the original wrote it after the block
        // (setBlockMetadataWithNotify, flag 3), here it is part of the state.
        final Direction facing = Direction.from3DDataValue(stuffdir);
        int i = 2;
        final int k = 1;
        final int j = length - 1;
        FastBlocks.setBlockFast(world, x + i * deltax + j * deltaz, y + k, z + i * deltaz + j * deltax,
                Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, facing), Block.UPDATE_ALL); // :103-104
        i = 1;
        FastBlocks.setBlockFast(world, x + i * deltax + j * deltaz, y + k, z + i * deltaz + j * deltax,
                Blocks.CRAFTING_TABLE.defaultBlockState(), Block.UPDATE_ALL); // :105-106
        i = 0;
        final BlockPos chestPos = new BlockPos(x + i * deltax + j * deltaz, y + k, z + i * deltaz + j * deltax);
        FastBlocks.setBlockFast(world, chestPos.getX(), chestPos.getY(), chestPos.getZ(),
                Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), Block.UPDATE_ALL); // :107-109
        // :110-126 - the chest's block entity exists as soon as the state is set (LevelChunk creates
        // it for an EntityBlock), as it did after setBlock in 1.7.10.
        if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) { // :110-111
            chest.setItem(0, new ItemStack(Items.COMPASS)); // :112
            chest.setItem(1, new ItemStack(Items.MAP)); // :113 - Items.map was the empty map
            chest.setItem(2, new ItemStack(Items.PORKCHOP, 8)); // :114
            chest.setItem(3, new ItemStack(Items.TORCH, 32)); // :115
            chest.setItem(4, new ItemStack(Items.COAL, 16)); // :116
            chest.setItem(5, new ItemStack(Items.RED_BED)); // :117 - PORT: 1.7.10 had one bed; the red one is its texture
            chest.setItem(6, new ItemStack(Items.RED_BED)); // :118
            chest.setItem(7, new ItemStack(Items.OAK_DOOR)); // :119 - PORT: wooden_door -> oak door
            chest.setItem(8, new ItemStack(Items.IRON_PICKAXE)); // :120
            chest.setItem(9, new ItemStack(Items.IRON_SWORD)); // :121
            chest.setItem(10, new ItemStack(Items.IRON_AXE)); // :122
            chest.setItem(11, new ItemStack(Items.BUCKET)); // :123
            chest.setItem(12, new ItemStack(ModItems.ORESALT.get(), 4)); // :124 MyOreSaltBlock
            chest.setItem(13, new ItemStack(Items.CHEST)); // :125
        }
        OneUse.consumeUnlessCreative(par1ItemStack, player); // :127-129
        return InteractionResult.sidedSuccess(false); // :130
    }
}
