package com.swbr.orespawn.client.screen;

import com.swbr.orespawn.menu.ContainerCrystalWorkbench;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Port of {@code danger.orespawn.CrystalWorkbenchGUI} (CrystalWorkbenchGUI.java:13-37): the
 * Crystal Workbench screen over vanilla's {@code textures/gui/container/crafting_table.png}
 * (:35). Labels (:21-24): {@code container.crafting} - the menu title the block supplies - at
 * (28, 6) and {@code container.inventory} at (8, ySize − 96 + 2), both 4210752; that is
 * {@code AbstractContainerScreen.renderLabels} with the title x at 28 (vanilla's own screen uses
 * 29). No recipe book and no recipe-book button (the original had none), so the texture's
 * left edge stays as it was.
 */
public class CrystalWorkbenchGUI extends AbstractContainerScreen<ContainerCrystalWorkbench> {

    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    public CrystalWorkbenchGUI(ContainerCrystalWorkbench menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 28; // :22
    }

    /** {@code drawGuiContainerBackgroundLayer} (:26-32). */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(CRAFTING_TABLE_GUI_TEXTURES, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
