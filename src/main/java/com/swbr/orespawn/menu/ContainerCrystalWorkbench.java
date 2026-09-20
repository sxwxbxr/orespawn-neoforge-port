package com.swbr.orespawn.menu;

import com.swbr.orespawn.registry.ModBlocks;
import com.swbr.orespawn.registry.ModMenus;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Port of {@code danger.orespawn.ContainerCrystalWorkbench} (ContainerCrystalWorkbench.java:9-104):
 * a 3×3 crafting grid over the vanilla recipe manager, the 1.7.10 {@code ContainerWorkbench}
 * with its own block check. Slot layout (:25-38), identical to vanilla's:
 *
 * <table>
 * <tr><th>index</th><th>slot</th><th>position</th></tr>
 * <tr><td>0</td><td>result, {@code SlotCrafting}</td><td>124, 35</td></tr>
 * <tr><td>1-9</td><td>matrix</td><td>30 + i·18, 17 + l·18</td></tr>
 * <tr><td>10-36</td><td>player inventory 9-35</td><td>8 + i·18, 84 + l·18</td></tr>
 * <tr><td>37-45</td><td>hotbar 0-8</td><td>8 + l·18, 142</td></tr>
 * </table>
 *
 * <p>Not a subclass of {@code CraftingMenu}: its constructor fixes {@code MenuType.CRAFTING}, and
 * the client picks the screen by menu type. Not a {@code RecipeBookMenu} either - the original
 * had no recipe book (1.7.10 had none), and the screen draws none. The result-slot refresh is the
 * vanilla helper written out ({@code CraftingMenu.slotChangedCraftingGrid} is package-private
 * to subclasses). {@code OreSpawnGUIHandler} case 1 (OreSpawnGUIHandler.java:19-21) is the
 * {@link ModMenus#CRYSTAL_WORKBENCH} type plus the client constructor below.
 */
public class ContainerCrystalWorkbench extends AbstractContainerMenu {

    public final CraftingContainer craftMatrix = new TransientCraftingContainer(this, 3, 3); // :19
    public final ResultContainer craftResult = new ResultContainer();                       // :20
    private final ContainerLevelAccess access;                                              // :21-24 worldObj, posX/Y/Z
    private final Player player;

    /** Server side: {@code new ContainerCrystalWorkbench(player.inventory, world, x, y, z)} (:18). */
    public ContainerCrystalWorkbench(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenus.CRYSTAL_WORKBENCH.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;
        this.addSlot(new ResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 124, 35)); // :25
        for (int l = 0; l < 3; ++l) {                                                                        // :26-30
            for (int i1 = 0; i1 < 3; ++i1) {
                this.addSlot(new Slot(this.craftMatrix, i1 + l * 3, 30 + i1 * 18, 17 + l * 18));
            }
        }
        for (int l = 0; l < 3; ++l) {                                                                        // :31-35
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
            }
        }
        for (int l = 0; l < 9; ++l) {                                                                        // :36-38
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 142));
        }
        // PORT: :39 onCraftMatrixChanged(craftMatrix) in the constructor is dropped. The matrix is
        // empty here, so the result was always null; in 1.21.1 the refresh sends a set-slot packet,
        // which before the open-screen packet would arrive for a menu the client does not have yet.
    }

    /**
     * Client side, from the {@code BlockPos} the server wrote. The original client container also
     * held world and position (OreSpawnGUIHandler.java:35-37) and ran {@link #stillValid} against
     * the client world; {@code ContainerLevelAccess.create} on the client level keeps that.
     */
    public ContainerCrystalWorkbench(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    /**
     * {@code onCraftMatrixChanged} (:42-44): {@code CraftingManager.findMatchingRecipe} into the
     * result slot. The body is 1.21.1's {@code CraftingMenu.slotChangedCraftingGrid}: the server
     * resolves the recipe, respects the result container's recipe-lock rule
     * ({@code ResultContainer.setRecipeUsed}, i.e. {@code doLimitedCrafting}) and pushes slot 0
     * to the client itself.
     */
    @Override
    public void slotsChanged(Container inventory) {
        this.access.execute((level, pos) -> slotChangedCraftingGrid(this, level, this.player, this.craftMatrix, this.craftResult));
    }

    private static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level level, Player player,
            CraftingContainer craftSlots, ResultContainer resultSlots) {
        if (!level.isClientSide) {
            CraftingInput craftinginput = craftSlots.asCraftInput();
            ServerPlayer serverplayer = (ServerPlayer) player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<RecipeHolder<CraftingRecipe>> optional = level.getServer()
                    .getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, craftinginput, level);
            if (optional.isPresent()) {
                RecipeHolder<CraftingRecipe> recipeholder = optional.get();
                CraftingRecipe craftingrecipe = recipeholder.value();
                if (resultSlots.setRecipeUsed(level, serverplayer, recipeholder)) {
                    ItemStack itemstack1 = craftingrecipe.assemble(craftinginput, level.registryAccess());
                    if (itemstack1.isItemEnabled(level.enabledFeatures())) {
                        itemstack = itemstack1;
                    }
                }
            }
            resultSlots.setItem(0, itemstack);
            menu.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, itemstack));
        }
    }

    /**
     * {@code onContainerClosed} (:46-56): on the server every matrix slot is <em>dropped</em>
     * ({@code dropPlayerItemWithRandomChoice(stack, false)}), not handed back to the inventory as
     * 1.21.1's {@code clearContainer} would do. {@code super.removed} handles the carried stack.
     */
    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            if (!level.isClientSide) {
                for (int i = 0; i < 9; ++i) {
                    ItemStack itemstack = this.craftMatrix.removeItemNoUpdate(i);
                    if (!itemstack.isEmpty()) {
                        player.drop(itemstack, false, false);
                    }
                }
            }
        });
    }

    /** {@code canInteractWith} (:58-60): the block is still a Crystal Workbench and within 8 blocks of its centre. */
    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) -> level.getBlockState(pos).is(ModBlocks.CRYSTALWORKBENCH.get())
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
    }

    /** {@code transferStackInSlot} (:62-99), the original slot ranges. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (index == 0) {                                                       // :68-73 result → player, from the end
                if (!this.moveItemStackTo(itemstack2, 10, 46, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack2, itemstack);
            } else if (index >= 10 && index < 37) {                                 // :74-78 main → hotbar
                if (!this.moveItemStackTo(itemstack2, 37, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 37 && index < 46) {                                 // :79-83 hotbar → main
                if (!this.moveItemStackTo(itemstack2, 10, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack2, 10, 46, false)) {          // :84-86 matrix → player
                return ItemStack.EMPTY;
            }
            if (itemstack2.isEmpty()) {                                             // :87-92
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack2.getCount() == itemstack.getCount()) {                    // :93-95
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack2);                                        // :96
        }
        return itemstack;
    }

    /** {@code func_94530_a} = {@code canMergeSlot} (:101-103): never the result slot. */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.craftResult && super.canTakeItemForPickAll(stack, slot);
    }
}
