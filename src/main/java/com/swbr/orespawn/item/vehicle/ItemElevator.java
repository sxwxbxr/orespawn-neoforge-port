package com.swbr.orespawn.item.vehicle;

import com.swbr.orespawn.entity.vehicle.Elevator;
import com.swbr.orespawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ItemElevator} (ItemElevator.java:11-35), registry id
 * {@code elevator} ("Hoverboard", OreSpawnMain.java:1566, legacy id 9235, tabTransport).
 * Right-clicking a block places a {@link Elevator} (entity id {@code hoverboard}).
 *
 * <p>Stack size 1 (:14). The icon is {@code textures/item/elevator.png} via
 * {@code models/item/elevator.json}; {@code registerIcons} (:31-34) has no 1.21.1 counterpart.
 */
public class ItemElevator extends Item {

    /** {@code ItemElevator(int)} (:13-16): {@code maxStackSize = 1}; the tab is set by the registry holder. */
    public ItemElevator(final Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    /**
     * {@code onItemUse} (:18-29): on the server create a {@code "Hoverboard"}, place it at
     * {@code (x + 0.5, y + 1.2, z + 0.5)} of the clicked block regardless of the clicked face, with a
     * random yaw and pitch 0, spawn it and use up one item unless the player is in creative mode.
     * The client returns {@code true} without doing anything.
     */
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level par3World = context.getLevel();
        if (par3World.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        final BlockPos pos = context.getClickedPos();
        final Elevator elevator = ModEntities.HOVERBOARD.get().create(par3World);
        if (elevator == null) {
            // PORT: the original cast EntityList.createEntityByName without a null check (:22-23);
            // EntityType.create is @Nullable (a disabled feature flag), so a null bails out instead
            // of throwing (R18 case 1).
            return InteractionResult.CONSUME;
        }
        // (double)(par4 + 0.5f): int + float stays float arithmetic, as in the original (:23).
        elevator.moveTo((double) (pos.getX() + 0.5f), (double) (pos.getY() + 1.2f), (double) (pos.getZ() + 0.5f),
                par3World.random.nextFloat() * 360.0f, 0.0f);
        par3World.addFreshEntity(elevator);
        final Player par2EntityPlayer = context.getPlayer();
        // PORT: UseOnContext.getPlayer() is @Nullable (a block-placing machine); the original always had
        // a player. Without one the item is used up like a survival player's (R18 case 1).
        if (par2EntityPlayer == null || !par2EntityPlayer.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        // PORT: the original returned true on the server too; CONSUME is the server half of
        // sidedSuccess - the client already swung the arm, a server SUCCESS would swing it again.
        return InteractionResult.CONSUME;
    }
}
