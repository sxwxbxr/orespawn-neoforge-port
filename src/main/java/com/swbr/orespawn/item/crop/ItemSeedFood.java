package com.swbr.orespawn.item.crop;

import com.swbr.orespawn.block.crop.CropBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The planting behaviour the five seed items share: vanilla 1.7.10 {@code ItemSeedFood} (Forge
 * build) for {@link ItemRadish}, {@link ItemCornCob}, {@link ItemTomato} and {@link ItemLettuce},
 * and the identical {@code ItemSeeds.onItemUse} for {@link ItemStrawberrySeed}. None of the
 * OreSpawn subclasses added logic beyond their icon (ItemRadish.java:8-18 etc.); the whole
 * behaviour is the vanilla method, ported here once.
 *
 * <p>{@code onItemUse} (Forge 1.7.10 {@code ItemSeedFood}): only the top face; the player must be
 * allowed to edit the clicked block and the one above; the clicked block must sustain the crop
 * ({@code Block.canSustainPlant} with {@code EnumPlantType.Crop}: farmland - or Crystal Grass,
 * whose override said yes to everything, CrystalGrass.java:58-60) and the block above must be air.
 * Then {@code world.setBlock(x, y + 1, z, crop)} (flag 3), one seed less, {@code true}. No sound,
 * no placement check of the crop block itself.
 *
 * <p>The soil argument of the original constructors ({@code Blocks.farmland} or
 * {@code OreSpawnMain.CrystalGrass}) is not used by the Forge method - the soil decides through
 * {@code canSustainPlant} - and is therefore not carried.
 */
public class ItemSeedFood extends ItemNameBlockItem {

    /**
     * @param crop  the block planted ({@code MyRadishPlant}, {@code MyCornPlant1}, ...)
     * @param props item properties; {@code ItemFood} put every food on {@code tabFood}, which the
     *              registry holder adds through {@code ModCreativeTabs}
     */
    public ItemSeedFood(final Block crop, final Item.Properties props) {
        super(crop, props);
    }

    /** {@code ItemFood(hunger, saturation, wolfMeat = false)}: 32-tick eating, never always-edible. */
    public static FoodProperties food(final int hunger, final float saturation) {
        return new FoodProperties.Builder().nutrition(hunger).saturationModifier(saturation).build();
    }

    /**
     * Same shape as {@code BlockItem.useOn}: try to plant, and if that did not consume the click,
     * fall back to eating - 1.7.10 reached {@code ItemFood.onItemRightClick} the same way, through
     * {@code onItemUse} returning {@code false}.
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final InteractionResult planted = this.plant(context);
        if (!planted.consumesAction() && context.getItemInHand().has(DataComponents.FOOD)) {
            final InteractionResult eaten = super.use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
            return eaten == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : eaten;
        }
        return planted;
    }

    /** {@code ItemSeedFood.onItemUse}, both sides as in 1.7.10; {@code false} is {@link InteractionResult#PASS}. */
    protected InteractionResult plant(final UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) { // side != 1
            return InteractionResult.PASS;
        }
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final BlockPos above = pos.above();
        final ItemStack stack = context.getItemInHand();
        final Player player = context.getPlayer();
        // canPlayerEdit(x, y, z, side, stack) && canPlayerEdit(x, y + 1, z, side, stack).
        // PORT: the player is nullable in 1.21.1; without one there is nobody to refuse.
        if (player != null && !(player.mayUseItemAt(pos, Direction.UP, stack) && player.mayUseItemAt(above, Direction.UP, stack))) {
            return InteractionResult.PASS;
        }
        final BlockState soil = level.getBlockState(pos);
        if (this.canSustainPlant(soil, level, pos) && level.isEmptyBlock(above)) {
            level.setBlock(above, this.getBlock().defaultBlockState(), Block.UPDATE_ALL); // world.setBlock(x, y + 1, z, crop)
            stack.shrink(1); // --stackSize; creative mode restores the count afterwards, as it did
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    /**
     * Forge 1.7.10 {@code Block.canSustainPlant(world, x, y, z, UP, IPlantable)} for
     * {@code EnumPlantType.Crop}: {@code this == Blocks.farmland}; Crystal Grass overrode it to
     * {@code true}; any other block may say yes through the same hook, which is NeoForge's
     * {@code canSustainPlant} TriState today.
     */
    protected boolean canSustainPlant(final BlockState soil, final Level level, final BlockPos pos) {
        return soil.is(Blocks.FARMLAND)
                || soil.is(CropBlocks.CRYSTAL_GRASS)
                || soil.canSustainPlant(level, pos, Direction.UP, this.getBlock().defaultBlockState()).isTrue();
    }
}
