package com.swbr.orespawn.item.companion;

import com.swbr.orespawn.entity.companion.Shoes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemShoes} (verhalten/itemblock-01.md): a throwable stack of 64 that
 * spawns a {@link Shoes} with this item's id. Five registrations (OreSpawnMain.java:1362-1366):
 * {@code redheels} 2, {@code blackheels} 3, {@code slippers} 4, {@code boots} 5, {@code gamecontroller} 6.
 *
 * <p>Catalogue 6.5 asked what spinner tile 6 shows: a black game controller, and tiles 2..5 are the
 * red heel, black heel, turquoise slipper and brown boot - so each item's flying shoe matches its icon
 * (checked against {@code spinners.png}). The icons themselves are one texture per item
 * ({@code registerIcons} used the unlocalized name), an {@code item/generated} model each.
 *
 * <p>Stack size 64 is the 1.21.1 default; {@code tabDecorations} is set at registration.
 */
public class ItemShoes extends Item {

    private int my_id;

    public ItemShoes(final int j, final Item.Properties properties) {
        super(properties);
        this.my_id = 0;
        this.my_id = j;
    }

    /**
     * {@code onItemRightClick} (ItemShoes.java:22-31). PORT: 1.7.10 had no off hand; the throw works
     * from either hand, like every 1.21.1 throwable item.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(final Level par2World, final Player par3EntityPlayer, final InteractionHand hand) {
        final ItemStack par1ItemStack = par3EntityPlayer.getItemInHand(hand);
        if (!par3EntityPlayer.getAbilities().instabuild) {
            par1ItemStack.shrink(1);
        }
        // "random.bow" is entity.arrow.shoot in 1.21.1; itemRand is the level random.
        par2World.playSound(null, par3EntityPlayer.getX(), par3EntityPlayer.getY(), par3EntityPlayer.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.NEUTRAL, 0.5f, 0.4f / (par2World.getRandom().nextFloat() * 0.4f + 0.8f));
        if (!par2World.isClientSide) {
            par2World.addFreshEntity(new Shoes(par2World, par3EntityPlayer, this.my_id));
        }
        return InteractionResultHolder.sidedSuccess(par1ItemStack, par2World.isClientSide());
    }
}
