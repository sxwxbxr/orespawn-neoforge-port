package com.swbr.orespawn.item.magic;

import com.swbr.orespawn.item.enchant.PreEnchant;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.ItemRandomDungeon} (ItemRandomDungeon.java:15-61): {@code randomdungeon} "Random
 * Dungeon" (OreSpawnMain.java:1603), stack 1, tab Redstone (:21-22). Right-click places the Random Dungeon Spawner
 * ({@code dungeonspawner}, {@link com.swbr.orespawn.block.spawner.DungeonSpawnerBlock}) on top of the clicked block
 * (verhalten/itemblock-01.md, "ItemRandomDungeon").
 *
 * <p>Ground, checked in the 20.3 bytecode ({@code func_77648_a}, the same four {@code getstatic}s as the 20.2 source):
 * {@code Blocks.stone}, {@code cobblestone}, {@code grass} or {@code dirt}, and {@code clickedY >= 40}. Catalogue 6.8
 * ("needs a solid block") describes the block's own {@code canPlaceBlockAt}, which this item never asks: it writes
 * with {@code world.setBlock}. PORT (DECISIONS R22): stone is the category {@code #minecraft:base_stone_overworld},
 * grass and dirt {@code #minecraft:dirt}; cobblestone stays the named block.
 */
public class ItemRandomDungeon extends Item {

    /** {@code onCreated} (:25-27) and {@code onUsingTick} (:29-34): Fortune II, sentinel Fortune. */
    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(PreEnchant.entry(Enchantments.FORTUNE, 2));

    private final Supplier<? extends Block> dungeonSpawner;

    /**
     * {@code ItemRandomDungeon(id)} (:19-23): {@code maxStackSize = 1}; {@code rand = OreSpawnRand} is never read.
     *
     * @param dungeonSpawner {@code OreSpawnMain.MyDungeonSpawnerBlock} - {@code ModBlocks.DUNGEONSPAWNER}
     */
    public ItemRandomDungeon(final Supplier<? extends Block> dungeonSpawner, final Item.Properties props) {
        super(props.stacksTo(1));
        this.dungeonSpawner = dungeonSpawner;
    }

    /** {@code onCreated} (:25-27). */
    @Override
    public void onCraftedBy(final ItemStack stack, final Level level, final Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    /** {@code onUpdate} (:36-38) calls {@code onUsingTick} (:29-34): Fortune again when it is missing (R7). */
    @Override
    public void inventoryTick(final ItemStack stack, final Level level, final Entity entity, final int slotId,
                              final boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.FORTUNE, ENCHANTMENTS);
    }

    /** {@code var1 != stone && var1 != cobblestone && var1 != grass && var1 != dirt} negated (:42). */
    static boolean isDungeonGround(final BlockState var1) {
        return var1.is(BlockTags.BASE_STONE_OVERWORLD) || var1.is(Blocks.COBBLESTONE) || var1.is(BlockTags.DIRT);
    }

    /**
     * {@code onItemUse} (:40-55): ground check (:41-44), {@code clickedY < 40} refused (:45-47, absolute, as in the
     * original), server places the spawner one block up with flag 2 (:48-50), one item less outside creative (:51-53).
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level world = context.getLevel();
        final BlockPos clicked = context.getClickedPos();
        final BlockState var1 = world.getBlockState(clicked);
        if (!isDungeonGround(var1)) {
            return InteractionResult.PASS;
        }
        if (clicked.getY() < 40) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide) {
            world.setBlock(clicked.above(), this.dungeonSpawner.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        final Player par2EntityPlayer = context.getPlayer();
        // PORT: the player is nullable in 1.21.1; none counts as "not creative", as in ItemSpawnEgg.
        if (par2EntityPlayer == null || !par2EntityPlayer.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
