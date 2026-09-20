package com.swbr.orespawn.item.crop;

import com.swbr.orespawn.block.crop.CropBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Port of {@code danger.orespawn.ItemAppleSeed} (ItemAppleSeed.java:13-125): a seed that grows a
 * fruit tree on the spot. Three items of this class, all stack 16, creative tab
 * {@code tabDecorations} (:16-17):
 *
 * <table>
 * <tr><th>id</th><th>name</th><th>leaves</th><th>source</th></tr>
 * <tr><td>{@code appletree_seed}</td><td>Apple Tree Seed</td><td>{@code leaves_apple}</td><td>OreSpawnMain.java:1606</td></tr>
 * <tr><td>{@code cherrytree_seed}</td><td>Cherry Pit</td><td>{@code leaves_cherry}</td><td>:1621</td></tr>
 * <tr><td>{@code peachtree_seed}</td><td>Peach Pit</td><td>{@code leaves_peach}</td><td>:1622</td></tr>
 * </table>
 *
 * <p>The tree shape lives in {@link #makeTree}, which {@code OreSpawnWorld.java:1886-1892} also
 * calls with a reference chunk during world generation (W12). The leaves blocks belong to
 * w02-trees-crystal ({@code BlockAppleLeaves}, {@code BlockScaryLeaves}) and are looked up by
 * their manifest ids.
 */
public class ItemAppleSeed extends Item {

    /** Which of the three seeds this is - the original compared {@code this} with the static fields (:26-34). */
    public enum Kind {
        APPLE("leaves_apple"),
        CHERRY("leaves_cherry"),
        PEACH("leaves_peach");

        private final ResourceKey<Block> leaves;

        Kind(final String leavesId) {
            this.leaves = CropBlocks.blockKey(leavesId);
        }

        public ResourceKey<Block> leaves() {
            return this.leaves;
        }
    }

    public static final ResourceKey<Block> APPLE_LEAVES = Kind.APPLE.leaves();
    public static final ResourceKey<Block> CHERRY_LEAVES = Kind.CHERRY.leaves();
    public static final ResourceKey<Block> PEACH_LEAVES = Kind.PEACH.leaves();

    private final Kind kind;

    /** {@code ItemAppleSeed(id)} (:15-18): {@code maxStackSize = 16}. */
    public ItemAppleSeed(final Kind kind, final Item.Properties props) {
        super(props.stacksTo(16));
        this.kind = kind;
    }

    public Kind getKind() {
        return this.kind;
    }

    /**
     * {@code onItemUse} (:20-40). Server: the clicked block must be grass, dirt or farmland, else
     * {@code false} (:21-25); then the tree of this seed's kind (:26-34). Both sides: one seed
     * less outside creative mode (:36-38), {@code true} (:39). The client never checks the soil.
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        if (!world.isClientSide) { // :21
            final BlockState bid = world.getBlockState(pos); // :22
            if (!CropBlocks.isGrassDirtOrFarmland(bid)) { // :23-25
                return InteractionResult.PASS;
            }
            makeTree(world, pos.getX(), pos.getY(), pos.getZ(), CropBlocks.block(this.kind.leaves()), null); // :26-34
        }
        final ItemStack stack = context.getItemInHand();
        final Player player = context.getPlayer();
        // PORT: the player is nullable in 1.21.1; none counts as "not creative", as in ItemSpawnEgg.
        if (player == null || !player.getAbilities().instabuild) { // :36
            stack.shrink(1); // :37
        }
        return InteractionResult.sidedSuccess(world.isClientSide); // :39
    }

    /**
     * {@code makeTree(world, x, y, z, leaves, chunk)} (:42-119), public because the world generator
     * shares it. Soil check again (:43-46); shape by leaves kind (:47-71):
     *
     * <table>
     * <tr><th></th><th>apple</th><th>peach</th><th>cherry</th></tr>
     * <tr><td>h1 trunk height (excl.)</td><td>12</td><td>10</td><td>8</td></tr>
     * <tr><td>h2 branch level 1</td><td>6</td><td>5</td><td>3</td></tr>
     * <tr><td>h3 branch level 2</td><td>9</td><td>7</td><td>5</td></tr>
     * <tr><td>h4 first leaf level</td><td>6</td><td>5</td><td>3</td></tr>
     * <tr><td>h5 leaf end (excl.)</td><td>14</td><td>12</td><td>10</td></tr>
     * <tr><td>w1 branch length 1 (excl.)</td><td>5</td><td>4</td><td>3</td></tr>
     * <tr><td>w2 branch length 2 (excl.)</td><td>3</td><td>2</td><td>1</td></tr>
     * </table>
     *
     * Trunk of {@code log} meta 0 (oak, vertical) from y+1 below h1 with flag 2 (:72-74); branch
     * crosses of the same log at y+h2 and y+h3 through {@code setBlockSuperFast} (:75-98); leaves
     * from h4 below h5 with half-width 6, 5 above level 8, 4 above level 10, one less for anything
     * but apple, written only into air (:99-118). Nothing is checked for space, and nothing above
     * the world's top is written ({@link FastBlocks}).
     *
     * @param chunk the reference chunk of the world generator, {@code null} from the item
     */
    public static void makeTree(final LevelAccessor world, final int x, final int y, final int z,
                                final Block blkid, @Nullable final ChunkAccess chunk) {
        BlockState bid = world.getBlockState(new BlockPos(x, y, z)); // :43
        if (!CropBlocks.isGrassDirtOrFarmland(bid)) { // :44-46
            return;
        }
        int h1 = 12;
        int h2 = 6;
        int h3 = 9;
        int h4 = 6;
        int h5 = 14;
        int w1 = 5;
        int w2 = 3; // :47-53
        final BlockState leavesKind = blkid.defaultBlockState();
        if (leavesKind.is(PEACH_LEAVES)) { // :54-62
            h1 = 10;
            h2 = 5;
            h3 = 7;
            h4 = 5;
            h5 = 12;
            w1 = 4;
            w2 = 2;
        }
        if (leavesKind.is(CHERRY_LEAVES)) { // :63-71
            h1 = 8;
            h2 = 3;
            h3 = 5;
            h4 = 3;
            h5 = 10;
            w1 = 3;
            w2 = 1;
        }
        // :73 - Blocks.log meta 0: oak, axis y. The branches used the same state, so they are
        // vertical-axis logs lying sideways, as they were.
        final BlockState log = Blocks.OAK_LOG.defaultBlockState();
        for (int j = 1; j < h1; ++j) { // :72-74 world.setBlock(..., 2)
            FastBlocks.setBlockFast(world, x, y + j, z, log, Block.UPDATE_CLIENTS);
        }
        for (int j = 1; j < w1; ++j) { // :75-77
            FastBlocks.setBlockSuperFast(world, x + j, y + h2, z, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int j = 1; j < w1; ++j) { // :78-80
            FastBlocks.setBlockSuperFast(world, x - j, y + h2, z, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int j = 1; j < w1; ++j) { // :81-83
            FastBlocks.setBlockSuperFast(world, x, y + h2, z + j, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int j = 1; j < w1; ++j) { // :84-86
            FastBlocks.setBlockSuperFast(world, x, y + h2, z - j, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int j = 1; j < w2; ++j) { // :87-89
            FastBlocks.setBlockSuperFast(world, x + j, y + h3, z, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int j = 1; j < w2; ++j) { // :90-92
            FastBlocks.setBlockSuperFast(world, x - j, y + h3, z, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int j = 1; j < w2; ++j) { // :93-95
            FastBlocks.setBlockSuperFast(world, x, y + h3, z + j, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int j = 1; j < w2; ++j) { // :96-98
            FastBlocks.setBlockSuperFast(world, x, y + h3, z - j, log, Block.UPDATE_CLIENTS, chunk);
        }
        for (int i = h4; i < h5; ++i) { // :99
            int width = 6; // :100
            if (i > 8) {
                width = 5; // :101-103
            }
            if (i > 10) {
                width = 4; // :104-106
            }
            if (!leavesKind.is(APPLE_LEAVES)) { // :107-109
                --width;
            }
            for (int j = -width; j <= width; ++j) { // :110
                for (int k = -width; k <= width; ++k) { // :111
                    final BlockPos leafPos = new BlockPos(x + k, y + i, z + j);
                    // PORT: the original read through world.getBlock, which answered air outside
                    // 0..255; the port reads through the level, which answers air (void) there too.
                    bid = world.isOutsideBuildHeight(leafPos) ? Blocks.AIR.defaultBlockState() : world.getBlockState(leafPos); // :112
                    if (bid.isAir()) { // :113 bid == Blocks.air
                        FastBlocks.setBlockSuperFast(world, x + k, y + i, z + j, leavesKind, Block.UPDATE_CLIENTS, chunk); // :114
                    }
                }
            }
        }
    }
}
