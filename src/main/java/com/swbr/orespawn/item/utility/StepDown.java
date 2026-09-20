package com.swbr.orespawn.item.utility;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
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
 * Port of {@code danger.orespawn.StepDown} (StepDown.java:13-108): {@code step_down} "Stairs going
 * Down" (OreSpawnMain.java:1591), stack 16, creative tab {@code tabTools} (:15-18). The twin of
 * {@link StepUp} with the steps descending: cobblestone at {@code y - k - 1}, the torch at
 * {@code y - k}, and the client burst without the extra block of height (:79-81).
 */
public class StepDown extends Item {

    /** {@code StepDown(id)} (:15-18): {@code maxStackSize = 16}. */
    public StepDown(final Item.Properties props) {
        super(props.stacksTo(16));
    }

    /** {@code onItemUse} (:20-102); see {@link StepUp#useOn} for the shared part. */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final Player player = context.getPlayer();
        final ItemStack par1ItemStack = context.getItemInHand();
        final BlockPos clicked = context.getClickedPos();
        if (player == null) {
            // PORT: no player in 1.21.1 (dispenser-like callers); the original always had one.
            return InteractionResult.PASS;
        }
        final int length = 33; // :23
        final int x = clicked.getX();
        final int y = clicked.getY() + 1;
        final int z = clicked.getZ(); // :24-26
        final int[] delta = OneUse.stepDirection(player.getYHeadRot()); // :27-72
        final int deltax = delta[0];
        final int deltaz = delta[1];
        if (deltax == 0 && deltaz == 0) { // :73-75
            return InteractionResult.PASS;
        }
        OneUse.playExplode(world, player, 1.0f, 1.5f); // :76
        if (world.isClientSide) { // :77-84
            OneUse.stepParticles(world, x, y, z, 0.0f);
            return InteractionResult.sidedSuccess(true);
        }
        final BlockState cobblestone = Blocks.COBBLESTONE.defaultBlockState();
        for (int k = 1; k < length; ++k) { // :85
            final int bx = x + k * deltax;
            final int bz = z + k * deltaz;
            BlockState bid = world.getBlockState(new BlockPos(bx, y - k - 1, bz)); // :86
            if (!OneUse.isAir(bid)) { // :87-89
                break;
            }
            FastBlocks.setBlockFast(world, bx, y - k - 1, bz, cobblestone, Block.UPDATE_CLIENTS); // :90
            if ((k - 1) % 8 == 0) { // :91
                bid = world.getBlockState(new BlockPos(bx, y - k, bz)); // :92
                if (OneUse.isAir(bid)) { // :93-95
                    FastBlocks.setBlockFast(world, bx, y - k, bz, ModBlocks.EXTREME_TORCH.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        OneUse.consumeUnlessCreative(par1ItemStack, player); // :98-100
        return InteractionResult.sidedSuccess(false); // :101
    }
}
