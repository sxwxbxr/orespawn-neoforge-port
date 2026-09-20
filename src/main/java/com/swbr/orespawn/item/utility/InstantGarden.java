package com.swbr.orespawn.item.utility;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.InstantGarden} (InstantGarden.java:13-145): {@code instantgarden}
 * "Instant Survival Garden" (OreSpawnMain.java:1599), stack 16, creative tab {@code tabRedstone}
 * (:15-18). Clears a 15 wide, 18 long, 10 high volume in the clicked direction, lays a grass floor
 * and plants fifteen crop rows across it: radish, lettuce, carrots, water, potatoes, wheat,
 * tomato, water, corn, strawberry, sugar cane on sand, water, melon stems, grass on both edges.
 */
public class InstantGarden extends Item {

    /** {@code InstantGarden(id)} (:15-18): {@code maxStackSize = 16}. */
    public InstantGarden(final Item.Properties props) {
        super(props.stacksTo(16));
    }

    /**
     * {@code onItemUse} (:20-139). Direction as in {@link ItemMinersDream}: the clicked block must
     * share a row or column with the player and lie on exactly one axis away from him (:29-61).
     * The garden starts at the clicked block on the player's foot level.
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
        final int height = 10;
        final int width = 7;
        final int length = 18; // :26-28
        final int pposx = OneUse.legacyFloor(player.getX(), cposx); // :29-35
        final int pposy = Mth.floor(player.getY()); // :36 (int) posY -> Mth.floor (DECISIONS R20)
        final int pposz = OneUse.legacyFloor(player.getZ(), cposz); // :37
        if (cposx - pposx != 0 && cposz - pposz != 0) { // :38-40
            return InteractionResult.PASS;
        }
        final int x = cposx;
        final int y = pposy;
        final int z = cposz; // :41-43
        if (x - pposx < 0) { // :44-46
            deltax = -1;
        }
        if (x - pposx > 0) { // :47-49
            deltax = 1;
        }
        if (z - pposz < 0) { // :50-52
            deltaz = -1;
        }
        if (z - pposz > 0) { // :53-55
            deltaz = 1;
        }
        if (deltax == 0 && deltaz == 0) { // :56-58
            return InteractionResult.PASS;
        }
        if (deltax != 0 && deltaz != 0) { // :59-61
            return InteractionResult.PASS;
        }
        OneUse.playExplode(world, player, 1.0f, 1.5f); // :62
        if (world.isClientSide) { // :63-65
            return InteractionResult.sidedSuccess(true);
        }
        // Every write below is world.setBlock(..., 0, 2): clients updated, no neighbour
        // notification of any kind (:69 ff.) - the placed water stayed where it was put, a water
        // source next to the cleared volume did not flow in, sand or gravel over it did not fall.
        // PORT: flag 2 alone still runs neighbour *shape* updates in 1.21.1 (Level.setBlock,
        // "(flags & 16) == 0"), which schedule the liquid's flow tick (LiquidBlock.updateShape) and
        // drop the sand (FallingBlock.updateShape); UPDATE_KNOWN_SHAPE (16) turns them off and gives
        // the original result, as in ItemMinersDream.
        final int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        final BlockState air = Blocks.AIR.defaultBlockState();
        final BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState(); // :71 Blocks.grass
        final BlockState farmland = Blocks.FARMLAND.defaultBlockState();
        final BlockState water = Blocks.WATER.defaultBlockState();
        final BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        for (int i = 0; i < height; ++i) { // :66-75
            for (int k = 0; k < length; ++k) {
                for (int j = -width; j <= width; ++j) {
                    FastBlocks.setBlockFast(world, x + k * deltax + j * deltaz, y + i, z + k * deltaz + j * deltax, air, flags); // :69
                    if (i == 0) { // :70-72
                        FastBlocks.setBlockFast(world, x + k * deltax + j * deltaz, y + i - 1, z + k * deltaz + j * deltax, grass, flags);
                    }
                }
            }
        }
        for (int k = 1; k < length - 1; ++k) { // :76
            int i = 0; // :77 - the row counter, 0..14 over j = -7..7
            for (int j = -width; j <= width; ++j) { // :78
                final int bx = x + k * deltax + j * deltaz;
                final int bz = z + k * deltaz + j * deltax;
                if (i == 1) { // :79-82
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, ModBlocks.RADISH_PLANT.get().defaultBlockState(), flags); // MyRadishPlant
                }
                if (i == 2) { // :83-86
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, ModBlocks.LETTUCE_0.get().defaultBlockState(), flags); // MyLettucePlant1
                }
                if (i == 3) { // :87-90
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, Blocks.CARROTS.defaultBlockState(), flags);
                }
                if (i == 4) { // :91-94
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, water, flags);
                    FastBlocks.setBlockFast(world, bx, y - 2, bz, cobblestone, flags);
                }
                if (i == 5) { // :95-98
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, Blocks.POTATOES.defaultBlockState(), flags);
                }
                if (i == 6) { // :99-102
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, Blocks.WHEAT.defaultBlockState(), flags);
                }
                if (i == 7) { // :103-106
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, ModBlocks.TOMATO_0.get().defaultBlockState(), flags); // MyTomatoPlant1
                }
                if (i == 8) { // :107-110
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, water, flags);
                    FastBlocks.setBlockFast(world, bx, y - 2, bz, cobblestone, flags);
                }
                if (i == 9) { // :111-114
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, ModBlocks.CORN_0.get().defaultBlockState(), flags); // MyCornPlant1
                }
                if (i == 10) { // :115-118
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, ModBlocks.STRAWBERRY_PLANT.get().defaultBlockState(), flags); // MyStrawberryPlant
                }
                if (i == 11) { // :119-123
                    FastBlocks.setBlockFast(world, bx, y - 2, bz, cobblestone, flags);
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, Blocks.SAND.defaultBlockState(), flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, Blocks.SUGAR_CANE.defaultBlockState(), flags); // Blocks.reeds
                }
                if (i == 12) { // :124-127
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, water, flags);
                    FastBlocks.setBlockFast(world, bx, y - 2, bz, cobblestone, flags);
                }
                if (i == 13) { // :128-131
                    FastBlocks.setBlockFast(world, bx, y - 1, bz, farmland, flags);
                    FastBlocks.setBlockFast(world, bx, y, bz, Blocks.MELON_STEM.defaultBlockState(), flags);
                }
                ++i; // :132
            }
        }
        OneUse.consumeUnlessCreative(par1ItemStack, player); // :135-137
        return InteractionResult.sidedSuccess(false); // :138
    }
}
