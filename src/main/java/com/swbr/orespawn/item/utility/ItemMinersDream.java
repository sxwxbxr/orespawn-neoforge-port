package com.swbr.orespawn.item.utility;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.common.Tags;

/**
 * Port of {@code danger.orespawn.ItemMinersDream} (ItemMinersDream.java:13-116): {@code minersdream}
 * "Miner's Dream" (OreSpawnMain.java:1588), stack 16, creative tab {@code tabRedstone} (:15-18).
 * Digs a tunnel 11 wide, 5 high and 64 long in the clicked direction. The original dug stone,
 * dirt, gravel, liquids, netherrack, end stone and crystal stone - all the rock of a 1.7.10 world -
 * and left the ores standing, that is the point of the item. The port digs every block except
 * {@code #c:ores} (DECISIONS R22). The ceiling gets roofed where it was open, and a torch stands
 * every five blocks on the centre line.
 *
 * <p>The config key {@code MinersDreamExpensive} (OreSpawnTWEAKS, default 0) does not touch this
 * class: {@code OreSpawnMain.java:3018-3025} chooses between the gunpowder and the TNT recipe by
 * it. That is a recipe condition for W12 (DECISIONS R14, recipes.json:8093 and :8128).
 */
public class ItemMinersDream extends Item {

    /**
     * {@code OreSpawnMain.DimensionID5}, the Crystal dimension ({@code orespawn:crystal}, DECISIONS
     * R13): the tunnel is roofed with crystal stone there instead of cobblestone (:81-86). Alias of
     * the W05 dimension holder, kept under this name for its callers.
     */
    public static final ResourceKey<Level> DIMENSION_CRYSTAL = com.swbr.orespawn.world.dimension.crystal.WorldProviderOreSpawn5.DIMENSION;

    /** {@code ItemMinersDream(id)} (:15-18): {@code maxStackSize = 16}. */
    public ItemMinersDream(final Item.Properties props) {
        super(props.stacksTo(16));
    }

    /** {@code bid == stone || dirt || gravel || water || lava || netherrack || end_stone || CrystalStone} (:72). */
    private static boolean isDiggable(final Level world, final BlockPos pos, final BlockState bid) {
        // PORT: R22 - the 1.7.10 list was every rock of its world ("rock away, ores stay"); in a
        // 1.21.1 world granite, diorite, andesite, tuff, deepslate, mud and friends stayed standing.
        // The tunnel now clears every block except #c:ores, with three exceptions so nothing
        // irreplaceable vanishes: unbreakable blocks (destroySpeed < 0, bedrock), blocks with a block
        // entity (chests, spawners - their content would be lost silently), and air, which there is
        // nothing to dig in (the original never wrote over it either). Liquids are dug as in the
        // original (water and lava have no block entity and a destroy speed of 100).
        // BUGHUNT2 2.3: OreSpawn's own ores are in #c:ores through data/c/tags/block/ores.json - the five
        // resource ores, the crystal ores, the four mob stones (crystalrat, crystalfairy, redanttroll,
        // termitetroll; the original left them standing, :72 names only CrystalStone) and the 119 dried eggs.
        return !bid.isAir()
                && !bid.is(Tags.Blocks.ORES)
                && bid.getDestroySpeed(world, pos) >= 0.0f
                && !bid.hasBlockEntity();
    }

    /** {@code bid == air || gravel || sand || water || lava} (:80): what the ceiling must be replaced over. */
    private static boolean needsRoof(final BlockState bid) {
        return OneUse.isAir(bid) || bid.is(Blocks.GRAVEL) || OneUse.isSand(bid)
                || OneUse.isWater(bid) || OneUse.isLava(bid);
    }

    /** {@code bid == stone || dirt || gravel || netherrack || end_stone || bedrock} (:99): floor that carries an Extreme Torch. */
    private static boolean carriesExtremeTorch(final Level world, final BlockPos pos, final BlockState bid) {
        // PORT: R22 - the 1.7.10 list was the rock floor of its world; the port takes every floor
        // with a sturdy top face. Crystal stone is left out because it has its own branch (:102-104,
        // the Crystal Torch) that the original reached only because crystal stone was not in :99.
        // Gravel was on the list but has a sturdy top face anyway.
        return !bid.is(ModBlocks.CRYSTALSTONE.get()) && bid.isFaceSturdy(world, pos, Direction.UP);
    }

    /**
     * {@code onItemUse} (:20-110). The clicked block must share a row or column with the player
     * and lie on exactly one axis away from him (:30-62); the tunnel starts at the clicked block on
     * the player's foot level. Sound on both sides, then the client is done (:63-66).
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
        final int height = 5;
        final int width = 5;
        final int length = 64;
        final int torches = 5;
        int solid_count = 0; // :25-29
        final int pposx = OneUse.legacyFloor(player.getX(), cposx); // :30-36
        final int pposy = Mth.floor(player.getY()); // :37 (int) posY -> Mth.floor (DECISIONS R20)
        final int pposz = OneUse.legacyFloor(player.getZ(), cposz); // :38
        if (cposx - pposx != 0 && cposz - pposz != 0) { // :39-41
            return InteractionResult.PASS;
        }
        final int x = cposx;
        final int y = pposy;
        final int z = cposz; // :42-44
        if (x - pposx < 0) { // :45-47
            deltax = -1;
        }
        if (x - pposx > 0) { // :48-50
            deltax = 1;
        }
        if (z - pposz < 0) { // :51-53
            deltaz = -1;
        }
        if (z - pposz > 0) { // :54-56
            deltaz = 1;
        }
        if (deltax == 0 && deltaz == 0) { // :57-59
            return InteractionResult.PASS;
        }
        if (deltax != 0 && deltaz != 0) { // :60-62
            return InteractionResult.PASS;
        }
        OneUse.playExplode(world, player, 1.0f, 1.5f); // :63
        if (world.isClientSide) { // :64-66
            return InteractionResult.sidedSuccess(true);
        }
        // Every write is world.setBlock(..., 0, 2): clients updated, no neighbour notification of
        // any kind (:73, :82, :85, :92, :100, :103) - a water or lava source in the tunnel wall
        // stayed still, sand whose support was dug hung in the air, torches and rails on a dug
        // block kept floating. PORT: flag 2 alone still runs neighbour *shape* updates in 1.21.1
        // (Level.setBlock, "(flags & 16) == 0"), and those are what schedule the liquid's flow
        // tick (LiquidBlock.updateShape), drop the sand (FallingBlock.updateShape) and pop the
        // torch; UPDATE_KNOWN_SHAPE (16) turns them off and gives the original result.
        final int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        final BlockState air = Blocks.AIR.defaultBlockState();
        final BlockState roof = world.dimension().equals(DIMENSION_CRYSTAL) // :81
                ? ModBlocks.CRYSTALSTONE.get().defaultBlockState()
                : Blocks.COBBLESTONE.defaultBlockState();
        for (int i = 0; i < height; ++i) { // :67
            for (int k = 0; k < length; ++k) { // :68
                solid_count = 0; // :69
                for (int j = -width; j <= width; ++j) { // :70
                    final int bx = x + k * deltax + j * deltaz;
                    final int bz = z + k * deltaz + j * deltax;
                    final BlockPos dig = new BlockPos(bx, y + i, bz);
                    BlockState bid = world.getBlockState(dig); // :71
                    if (isDiggable(world, dig, bid)) { // :72-74
                        FastBlocks.setBlockFast(world, bx, y + i, bz, air, flags);
                    }
                    if (i == height - 1) { // :75
                        bid = world.getBlockState(new BlockPos(bx, y + i + 1, bz)); // :76
                        if (!OneUse.isAir(bid)) { // :77-79
                            ++solid_count;
                        }
                        if (needsRoof(bid)) { // :80-87
                            FastBlocks.setBlockFast(world, bx, y + i + 1, bz, roof, flags);
                        }
                    }
                }
                if (i == height - 1 && solid_count == 0) { // :90-94 - open sky stays open
                    for (int j = -width; j <= width; ++j) {
                        FastBlocks.setBlockFast(world, x + k * deltax + j * deltaz, y + i + 1, z + k * deltaz + j * deltax, air, flags);
                    }
                }
            }
        }
        for (int k = 0; k < length; k += torches) { // :97
            final int bx = x + k * deltax;
            final int bz = z + k * deltaz;
            final BlockPos floor = new BlockPos(bx, y - 1, bz);
            final BlockState bid = world.getBlockState(floor); // :98
            if (carriesExtremeTorch(world, floor, bid) && OneUse.isAir(world.getBlockState(new BlockPos(bx, y, bz)))) { // :99-101
                FastBlocks.setBlockFast(world, bx, y, bz, ModBlocks.EXTREME_TORCH.get().defaultBlockState(), flags);
            }
            if (bid.is(ModBlocks.CRYSTALSTONE.get()) && OneUse.isAir(world.getBlockState(new BlockPos(bx, y, bz)))) { // :102-104
                FastBlocks.setBlockFast(world, bx, y, bz, ModBlocks.CRYSTAL_TORCH.get().defaultBlockState(), flags);
            }
        }
        OneUse.consumeUnlessCreative(par1ItemStack, player); // :106-108
        return InteractionResult.sidedSuccess(false); // :109
    }
}
