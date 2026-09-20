package com.swbr.orespawn.item.tool;

import com.swbr.orespawn.item.enchant.PreEnchant;
import com.swbr.orespawn.item.tool.OreSpawnTiers.Material;
import com.swbr.orespawn.item.utility.OneUse;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of {@code danger.orespawn.UltimateHoe} (UltimateHoe.java:14-75): an {@code ItemHoe} on
 * {@code toolULTIMATE}, stack 1, {@code setMaxDamage(3000)} (:19), tab Tools (:20); registered as
 * {@code ultimatehoe} without a harvest level (OreSpawnMain.java:1310).
 *
 * <ul>
 *   <li>{@code onCreated}: Efficiency II (:23-25); re-added by {@code onUsingTick}/{@code onUpdate}
 *       when missing (:27-36) - R7.</li>
 *   <li>{@code onItemUse} (:38-65) replaces the vanilla hoe entirely: tills a 3x3x3 cube of grass
 *       and dirt with air above into <em>wet</em> farmland (moisture 7), see {@link #useOn}.</li>
 *   <li>{@code getMaterialName} (:67-69) is dead.</li>
 * </ul>
 */
public class UltimateHoe extends HoeItem {

    private static final List<PreEnchant.Entry> ENCHANTMENTS = List.of(
            PreEnchant.entry(Enchantments.EFFICIENCY, 2));

    public UltimateHoe(Material material, Item.Properties props) {
        super(material.withUses(3000), props.attributes(OreSpawnTiers.hoe(material)));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        PreEnchant.addAll(stack, level, ENCHANTMENTS);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        PreEnchant.restore(stack, level, Enchantments.EFFICIENCY, ENCHANTMENTS);
    }

    /**
     * {@code onItemUse} (UltimateHoe.java:38-65), without {@code super}: the vanilla tilling of
     * paths does not happen with this hoe, as it did not in 1.7.10. The original fired no
     * {@code UseHoeEvent} either.
     *
     * <p>PORT: R22 - {@code Blocks.dirt} was the dirt category of 1.7.10 (coarse dirt and podzol
     * were its metadata 1 and 2). Both tests, on the clicked block and in the 3x3x3 cube, take
     * {@code OneUse.isDirt} - the shared 1.21.1 dirt category - plus the grass block.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        // canPlayerEdit (:39-41). Dispensers cannot use items on blocks, so player is never null here;
        // the check keeps the method total.
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }
        BlockState i1 = level.getBlockState(pos);
        boolean air = level.getBlockState(pos.above()).isAir();
        // side 0 = bottom (:44)
        if (context.getClickedFace() == Direction.DOWN || !air || !isGrassOrDirt(i1)) {
            return InteractionResult.PASS;
        }
        BlockState farmland = Blocks.FARMLAND.defaultBlockState();
        // playSoundEffect at the block centre, farmland break sound, (volume + 1) / 2, pitch * 0.8 (:47-48).
        SoundType sound = farmland.getSoundType();
        level.playSound(player, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f,
                sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0f) / 2.0f, sound.getPitch() * 0.8f);
        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }
        // j = x offset, k = z offset, l = y offset - nesting order as in the source (:52-62).
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
                    BlockPos p = pos.offset(j, l, k);
                    i1 = level.getBlockState(p);
                    air = level.getBlockState(p.above()).isAir();
                    if (air && isGrassOrDirt(i1)) {
                        // setBlock(..., farmland, 7, 2): metadata 7 = fully moist, flag 2 = client update only.
                        level.setBlock(p, farmland.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
                    }
                }
            }
        }
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        return InteractionResult.sidedSuccess(false);
    }

    /** {@code i1 == Blocks.grass || i1 == Blocks.dirt} (:44, :57). */
    private static boolean isGrassOrDirt(BlockState i1) {
        return i1.is(Blocks.GRASS_BLOCK) || OneUse.isDirt(i1);
    }
}
