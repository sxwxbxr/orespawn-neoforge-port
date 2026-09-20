package com.swbr.orespawn.item.utility;

import com.swbr.orespawn.world.util.FastBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.ItemNetherLost} (ItemNetherLost.java:14-66): {@code netherlost}
 * "Nether Tracker" (OreSpawnMain.java:1389), stack 1, durability 3000 that nothing ever consumes,
 * creative tab {@code tabDecorations} (:16-20). Held in the main hand in the Nether it turns the
 * netherrack under the player into quartz blocks - a trail home. It carries Sharpness II from the
 * crafting table on (:22-24) and re-adds it whenever it is missing (:26-31), so in 1.7.10, where
 * Sharpness worked on any held item, it was a weapon on the side.
 */
public class ItemNetherLost extends Item {

    /** {@code ItemNetherLost(id)} (:16-20): {@code maxStackSize = 1}, {@code setMaxDamage(3000)}. */
    public ItemNetherLost(final Item.Properties props) {
        super(props.durability(3000));
    }

    /** {@code Enchantment.sharpness} as a holder of this level's registry (data-driven in 1.21.1, DECISIONS R7). */
    private static Holder<Enchantment> sharpness(final Level world) {
        return world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
    }

    /** {@code onCreated} (:22-24): Sharpness II on crafting. */
    @Override
    public void onCraftedBy(final ItemStack par1ItemStack, final Level par2World, final Player par3EntityPlayer) {
        par1ItemStack.enchant(sharpness(par2World), 2); // :23 addEnchantment
    }

    /**
     * {@code onUsingTick} (:26-31): Sharpness II back if the level is 0. Never reached through a
     * use action (no {@code getUseAnimation}), only from {@link #inventoryTick} as in the original.
     */
    private static void onUsingTick(final ItemStack stack, final Level world) {
        final int lvl = EnchantmentHelper.getItemEnchantmentLevel(sharpness(world), stack); // :27
        if (lvl <= 0) { // :28-30
            stack.enchant(sharpness(world), 2);
        }
    }

    /**
     * {@code onUpdate} (:33-56), every tick in an inventory. The original ran it on both sides;
     * the enchantment top-up came first and needed no world (:36-39).
     *
     * <p>PORT: both effects run on the server only. The stack's components and the block are
     * server-owned in 1.21.1 and reach the client through sync; a client-side write would only
     * make the two sides differ until the next sync (DECISIONS R18, case 4). The
     * {@code (int)} casts of the player position (:48, :50) are {@code Mth.floor} (DECISIONS R20):
     * truncation put the quartz one block off at negative coordinates. (R18's "ItemRock-Versatz" is
     * the missing +0.5 at coordinate 0 of the rock throw, not a cast.)
     */
    @Override
    public void inventoryTick(final ItemStack stack, final Level par2World, final Entity par3Entity, final int par4, final boolean par5) {
        if (par2World.isClientSide) {
            return;
        }
        onUsingTick(stack, par2World); // :36
        if (par3Entity != null && par3Entity instanceof LivingEntity e) { // :40-41
            if (e instanceof Player p) { // :42-43
                final ItemStack is = p.getMainHandItem(); // :44 getCurrentEquippedItem
                if (!is.isEmpty()) { // :45
                    final Item it = is.getItem(); // :46
                    if (it instanceof ItemNetherLost && par2World.dimension().equals(Level.NETHER)) { // :47 dimensionId == -1
                        final BlockPos under = new BlockPos(Mth.floor(p.getX()), Mth.floor(p.getY()) - 1, Mth.floor(p.getZ()));
                        final BlockState i = par2World.getBlockState(under); // :48
                        if (i.is(Blocks.NETHERRACK)) { // :49-51 world.setBlock(x, y, z, block) = flag 3
                            FastBlocks.setBlockFast(par2World, under.getX(), under.getY(), under.getZ(),
                                    Blocks.QUARTZ_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }
    }

    /** {@code getMaxItemUseDuration} (:58-60): 3000, without a use action it never counts down. */
    @Override
    public int getUseDuration(final ItemStack par1ItemStack, final LivingEntity entity) {
        return 3000;
    }
}
