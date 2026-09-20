package com.swbr.orespawn.menu;

import com.swbr.orespawn.block.entity.TileEntityCrystalFurnace;
import com.swbr.orespawn.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Port of {@code danger.orespawn.ContainerCrystalFurnace} (ContainerCrystalFurnace.java:9-122):
 * the menu of the Crystal Furnace. Slot layout (:18-28):
 *
 * <table>
 * <tr><th>index</th><th>slot</th><th>position</th></tr>
 * <tr><td>0</td><td>input</td><td>56, 17</td></tr>
 * <tr><td>1</td><td>fuel - a plain {@code Slot}, the original did not restrict it</td><td>56, 53</td></tr>
 * <tr><td>2</td><td>output, {@code SlotFurnace}</td><td>116, 35</td></tr>
 * <tr><td>3-29</td><td>player inventory 9-35</td><td>8 + j·18, 84 + i·18</td></tr>
 * <tr><td>30-38</td><td>hotbar 0-8</td><td>8 + i·18, 142</td></tr>
 * </table>
 *
 * <p>The three progress values (:31-68) are 1.21.1 data slots in the original order; the GUI
 * scale helpers of the tile entity (TileEntityCrystalFurnace.java:124-135) live here because the
 * client reads the synced copy, not the block entity. {@code OreSpawnGUIHandler} case 0
 * (OreSpawnGUIHandler.java:13-18) is the {@link ModMenus#CRYSTAL_FURNACE} type plus the
 * client constructor below.
 */
public class ContainerCrystalFurnace extends AbstractContainerMenu {

    private final Container furnace;
    private final ContainerData data;
    private final Level level;

    /** Server side: {@code new ContainerCrystalFurnace(player.inventory, tileEntity)} (:16). */
    public ContainerCrystalFurnace(int containerId, Inventory playerInventory, Container furnace, ContainerData data) {
        super(ModMenus.CRYSTAL_FURNACE.get(), containerId);
        checkContainerSize(furnace, 3);
        checkContainerDataCount(data, TileEntityCrystalFurnace.DATA_COUNT);
        this.furnace = furnace;
        this.data = data;
        this.level = playerInventory.player.level();
        this.addSlot(new Slot(furnace, 0, 56, 17));                                        // :18
        this.addSlot(new Slot(furnace, 1, 56, 53));                                        // :19
        this.addSlot(new SlotFurnace(playerInventory.player, furnace, 2, 116, 35));        // :20
        for (int i = 0; i < 3; ++i) {                                                      // :21-25
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {                                                      // :26-28
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
        this.addDataSlots(data); // :31-55 addCraftingToCrafters / detectAndSendChanges
    }

    /**
     * Client side, from the {@code BlockPos} the server wrote with
     * {@code player.openMenu(provider, pos)}. Like {@code OreSpawnGUIHandler.getClientGuiElement}
     * (:29-33) the client menu wraps the client's own copy of the tile entity when it is there, so
     * {@link #stillValid} runs the same distance check on both sides; the data comes from the
     * server's data slots (:58-68 updateProgressBar). PORT: if the client chunk has no such block
     * entity (the original returned {@code null} and would have crashed), an empty 3-slot
     * container stands in.
     */
    public ContainerCrystalFurnace(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory,
                playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof TileEntityCrystalFurnace furnace
                        ? furnace : new SimpleContainer(3),
                new SimpleContainerData(TileEntityCrystalFurnace.DATA_COUNT));
    }

    /** {@code canInteractWith} (:70-72) → {@code isUseableByPlayer}. */
    @Override
    public boolean stillValid(Player player) {
        return this.furnace.stillValid(player);
    }

    /** {@code FurnaceRecipes.smelting().getSmeltingResult(stack) != null} (:87). */
    private boolean canSmelt(ItemStack stack) {
        return this.level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), this.level)
                .isPresent();
    }

    /** {@code transferStackInSlot} (:74-121), the original slot ranges. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (index == 2) {                                                     // :80-85 output → player, from the end
                if (!this.moveItemStackTo(itemstack2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack2, itemstack);
            } else if (index != 1 && index != 0) {                                // :86-105 player slot
                if (this.canSmelt(itemstack2)) {                                  // :87-91 → input
                    if (!this.moveItemStackTo(itemstack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (TileEntityCrystalFurnace.isItemFuel(itemstack2)) {     // :92-96 → fuel
                    if (!this.moveItemStackTo(itemstack2, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 3 && index < 30) {                            // :97-101 main → hotbar
                    if (!this.moveItemStackTo(itemstack2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack2, 3, 30, false)) { // :102-104
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack2, 3, 39, false)) {         // :106-108 input/fuel → player
                return ItemStack.EMPTY;
            }
            if (itemstack2.isEmpty()) {                                           // :109-114
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack2.getCount() == itemstack.getCount()) {                  // :115-117
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack2);                                      // :118
        }
        return itemstack;
    }

    // ---------------------------------------------------------------------------------------------
    // What the GUI reads (TileEntityCrystalFurnace.java:124-139), from the synced data
    // ---------------------------------------------------------------------------------------------

    /** {@code getCookProgressScaled} (:125-127): {@code cookTime * n / 150}. */
    public int getCookProgressScaled(int scale) {
        return this.data.get(TileEntityCrystalFurnace.DATA_COOK_TIME) * scale / TileEntityCrystalFurnace.COOK_TIME;
    }

    /**
     * {@code getBurnTimeRemainingScaled} (:130-135): {@code burnTime * n / currentItemBurnTime},
     * and a zero divisor is first set to 150 - written back like the original wrote its field.
     */
    public int getBurnTimeRemainingScaled(int scale) {
        int currentItemBurnTime = this.data.get(TileEntityCrystalFurnace.DATA_CURRENT_ITEM_BURN_TIME);
        if (currentItemBurnTime == 0) {
            currentItemBurnTime = TileEntityCrystalFurnace.COOK_TIME;
            this.data.set(TileEntityCrystalFurnace.DATA_CURRENT_ITEM_BURN_TIME, currentItemBurnTime);
        }
        return this.data.get(TileEntityCrystalFurnace.DATA_BURN_TIME) * scale / currentItemBurnTime;
    }

    /** {@code isBurning} (:137-139). */
    public boolean isBurning() {
        return this.data.get(TileEntityCrystalFurnace.DATA_BURN_TIME) > 0;
    }

    /**
     * The 1.7.10 vanilla {@code SlotFurnace} the original put on slot 2 (:20): nothing may be
     * placed, and taking pays the smelting experience of the taken items - per item, from the
     * recipe whose result they are, with the fractional part rolled once. 1.21.1's
     * {@code FurnaceResultSlot} pays from the block entity's {@code recipesUsed} instead (all
     * smelts since the last take, whatever amount is taken) and unlocks recipe-book entries; the
     * original did neither, so this is the 1.7.10 slot, not the vanilla one. The 1.7.10 iron and
     * fish achievements have no counterpart and are dropped.
     */
    public static class SlotFurnace extends Slot {

        private final Player thePlayer;
        private int removeCount;

        public SlotFurnace(Player player, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.thePlayer = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            if (this.hasItem()) {
                this.removeCount += Math.min(amount, this.getItem().getCount());
            }
            return super.remove(amount);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            this.checkTakeAchievements(stack);
            super.onTake(player, stack);
        }

        @Override
        protected void onQuickCraft(ItemStack stack, int amount) {
            this.removeCount += amount;
            this.checkTakeAchievements(stack);
        }

        /** 1.7.10 {@code SlotFurnace.onCrafting(ItemStack)}. */
        @Override
        protected void checkTakeAchievements(ItemStack stack) {
            stack.onCraftedBy(this.thePlayer.level(), this.thePlayer, this.removeCount);
            if (this.thePlayer instanceof ServerPlayer serverPlayer) {
                int i = this.removeCount;
                float f = TileEntityCrystalFurnace.getSmeltingExperience(serverPlayer.level(), stack);
                if (f == 0.0f) {
                    i = 0;
                } else if (f < 1.0f) {
                    int j = Mth.floor(i * f);
                    if (j < Mth.ceil(i * f) && (float) Math.random() < i * f - j) {
                        ++j;
                    }
                    i = j;
                }
                if (i > 0) {
                    // The orbs were spawned at (posX, posY + 0.5, posZ); award() does the getXPSplit loop.
                    ExperienceOrb.award(serverPlayer.serverLevel(), serverPlayer.position().add(0.0, 0.5, 0.0), i);
                }
            }
            this.removeCount = 0;
            EventHooks.firePlayerSmeltedEvent(this.thePlayer, stack); // FMLCommonHandler.firePlayerSmeltedEvent
        }
    }
}
