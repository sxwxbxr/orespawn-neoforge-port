package com.swbr.orespawn.client.screen;

import com.swbr.orespawn.menu.ContainerCrystalFurnace;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Port of {@code danger.orespawn.CrystalFurnaceGUI} (CrystalFurnaceGUI.java:12-45): the Crystal
 * Furnace screen over vanilla's {@code textures/gui/container/furnace.png} (:43).
 *
 * <p>Labels (:22-26): the furnace's name - custom or {@code container.furnace}, which is the
 * menu title the block entity supplies - centred at y = 6, and {@code container.inventory} at
 * (8, ySize − 96 + 2); both in 4210752, which is {@code AbstractContainerScreen.renderLabels} with
 * the title x centred. Flame and arrow (:34-39) with the original scales 12 and 24: the 1.21.1
 * texture no longer carries them at (176, 0) and (176, 14), they are the sprites
 * {@code container/furnace/lit_progress} (14×14) and {@code burn_progress} (24×16); the cut-outs
 * are the original ones - the bottom {@code i1 + 2} rows of the flame, the left {@code i1 + 1}
 * columns of the arrow. No recipe book (the original had none).
 */
public class CrystalFurnaceGUI extends AbstractContainerScreen<ContainerCrystalFurnace> {

    private static final ResourceLocation FURNACE_GUI_TEXTURES = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation BURN_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");

    public CrystalFurnaceGUI(ContainerCrystalFurnace menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = this.imageWidth / 2 - this.font.width(this.title) / 2; // :24
        // titleLabelY 6 and inventoryLabelY imageHeight - 94 = ySize - 96 + 2 are the defaults (:24-25).
    }

    /** {@code drawGuiContainerBackgroundLayer} (:28-40). */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int k = this.leftPos; // (width - xSize) / 2
        int l = this.topPos;  // (height - ySize) / 2
        guiGraphics.blit(FURNACE_GUI_TEXTURES, k, l, 0, 0, this.imageWidth, this.imageHeight); // :33
        if (this.menu.isBurning()) {                                                         // :34
            int i1 = this.menu.getBurnTimeRemainingScaled(12);                               // :35
            // :36 drawTexturedModalRect(k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2)
            guiGraphics.blitSprite(LIT_PROGRESS_SPRITE, 14, 14, 0, 12 - i1, k + 56, l + 36 + 12 - i1, 14, i1 + 2);
        }
        int i1 = this.menu.getCookProgressScaled(24);                                        // :38
        // :39 drawTexturedModalRect(k + 79, l + 34, 176, 14, i1 + 1, 16)
        guiGraphics.blitSprite(BURN_PROGRESS_SPRITE, 24, 16, 0, 0, k + 79, l + 34, i1 + 1, 16);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
