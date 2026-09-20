package com.swbr.orespawn.item.utility;

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
 * Port of {@code danger.orespawn.ZooCage} (ZooCage.java:12-71): five items of one class
 * (OreSpawnMain.java:1593-1597), stack 16, creative tab {@code tabDecorations} (:16-21):
 *
 * <table>
 * <tr><th>id</th><th>name</th><th>{@code cage_size}</th><th>half width {@code size/2+1}</th><th>outer</th><th>height</th></tr>
 * <tr><td>{@code zoo2}</td><td>Extra Small Zoo Cage</td><td>3</td><td>2</td><td>5x5</td><td>4</td></tr>
 * <tr><td>{@code zoo4}</td><td>Small Zoo Cage</td><td>5</td><td>3</td><td>7x7</td><td>5</td></tr>
 * <tr><td>{@code zoo6}</td><td>Medium Zoo Cage</td><td>9</td><td>5</td><td>11x11</td><td>7</td></tr>
 * <tr><td>{@code zoo8}</td><td>Large Zoo Cage</td><td>13</td><td>7</td><td>15x15</td><td>9</td></tr>
 * <tr><td>{@code zoo10}</td><td>Extra Large Zoo Cage</td><td>17</td><td>9</td><td>19x19</td><td>11</td></tr>
 * </table>
 *
 * A quartz floor and ceiling with glass walls around the player, everything inside cleared to
 * air. It overwrites <em>anything</em> - bedrock, chests, the block the player stands on - like
 * the original (verhalten/itemblock-02.md, ZooCage; DECISIONS R18).
 */
public class ZooCage extends Item {

    private final int cage_size;

    /** {@code ZooCage(id, j)} (:16-21): {@code maxStackSize = 16}, {@code cage_size = j}. */
    public ZooCage(final int cageSize, final Item.Properties props) {
        super(props.stacksTo(16));
        this.cage_size = cageSize;
    }

    public int getCageSize() {
        return this.cage_size;
    }

    /**
     * {@code onItemUse} (:23-65). Centre is the player's block (floor approximation from the
     * clicked block's sign, :30-38), the floor one below his feet. No direction check - the
     * clicked block only supplies the signs. Sound on both sides (:39), client done (:40-42).
     */
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
        final int length = this.cage_size / 2 + 1; // :29 width = height = length = size/2 + 1
        final int height = length;
        final int width = length;
        final int x = OneUse.legacyFloor(player.getX(), clicked.getX()); // :30-36
        final int y = Mth.floor(player.getY()) - 1; // :37 (int) posY -> Mth.floor (DECISIONS R20)
        final int z = OneUse.legacyFloor(player.getZ(), clicked.getZ()); // :38
        OneUse.playExplode(world, player, 1.0f, 1.5f); // :39
        if (world.isClientSide) { // :40-42
            return InteractionResult.sidedSuccess(true);
        }
        // :43-60 - world.setBlock(x, y, z, block) is flag 3.
        final BlockState quartz = Blocks.QUARTZ_BLOCK.defaultBlockState();
        final BlockState glass = Blocks.GLASS.defaultBlockState();
        final BlockState air = Blocks.AIR.defaultBlockState();
        for (int i = -width; i <= width; ++i) {
            for (int j = -length; j <= length; ++j) {
                for (int k = 0; k <= height + 1; ++k) {
                    if (k == height + 1) { // :46-47 ceiling
                        FastBlocks.setBlockFast(world, x + i, y + k, z + j, quartz, Block.UPDATE_ALL);
                    } else if (k == 0) { // :49-50 floor
                        FastBlocks.setBlockFast(world, x + i, y + k, z + j, quartz, Block.UPDATE_ALL);
                    } else if (i == width || j == length || i == -width || j == -length) { // :52-53 walls
                        FastBlocks.setBlockFast(world, x + i, y + k, z + j, glass, Block.UPDATE_ALL);
                    } else { // :55-56 inside
                        FastBlocks.setBlockFast(world, x + i, y + k, z + j, air, Block.UPDATE_ALL);
                    }
                }
            }
        }
        OneUse.consumeUnlessCreative(par1ItemStack, player); // :61-63
        return InteractionResult.sidedSuccess(false); // :64
    }
}
