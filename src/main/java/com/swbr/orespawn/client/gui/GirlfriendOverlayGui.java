package com.swbr.orespawn.client.gui;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.config.OreSpawnConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Port of {@code danger.orespawn.GirlfriendOverlayGui} (GirlfriendOverlayGui.java:14-411): the health bar at the top
 * of the screen for an owned companion or a big monster under the crosshair - and, by R25, the boss bar of The King,
 * The Queen and Mobzilla. The original subscribed to {@code RenderGameOverlayEvent} and drew after the hotbar
 * (ClientProxyOreSpawn.java:15); here it is a {@link LayeredDraw.Layer} (R15). Register with
 * {@code event.registerAbove(VanillaGuiLayers.HOTBAR, GirlfriendOverlayGui.ID, new GirlfriendOverlayGui(Minecraft.getInstance()))}
 * in {@code RegisterGuiLayersEvent}.
 *
 * <p>What it shows is decided by {@link GirlfriendOverlayTarget} (the 45-class chain and the ray); this class does the
 * early exits and the drawing.
 */
public class GirlfriendOverlayGui implements LayeredDraw.Layer {

    /** The layer id. */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "girlfriend_overlay");

    private final Minecraft mc;
    /** {@code orespawn:girlfriendgui.png} (:409), a 256x256 sheet: background at v 0, fill at v 5. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/girlfriendgui.png");

    public GirlfriendOverlayGui(final Minecraft mc) {
        this.mc = mc;
    }

    /**
     * {@code onRenderOverlay} (:24-406). PORT: the event filter {@code event.isCancelable() || type != HOTBAR}
     * (:25-27) is the registration above {@code VanillaGuiLayers.HOTBAR}.
     */
    @Override
    public void render(final GuiGraphics guiGraphics, final DeltaTracker deltaTracker) {
        final int u = 0;
        final int v = 0;
        String outstring = null;
        final int color = 16725044;
        final Font fr = this.mc.font;
        final int barWidth = 182;
        final int barHeight = 5;
        float gfHealth = 0.0f;
        Entity entity = null;
        Player player = null;
        if (this.mc.options.hideGui || this.mc.screen != null) {
            return;
        }
        player = this.mc.player;
        if (player == null) {
            return;
        }
        // PORT: OreSpawnMain.current_dimension and FastGraphicsLeaves (:45-51) are not written: both statics were
        // dropped in W01 (OreSpawn.java, "Not carried over"); dimension checks read level.dimension() and the fast
        // graphics texture swap has no 1.21.1 counterpart (R18).
        if (OreSpawnConfig.TWEAKS.GuiOverlayEnable.get() == 0) {
            return;
        }
        // mc.pointedEntity is crosshairPickEntity.
        entity = this.mc.crosshairPickEntity;
        if (entity == null) {
            entity = GirlfriendOverlayTarget.getPointedAtEntity(this.mc.level, player, 16.0);
            if (entity == null) {
                return;
            }
            if (!(entity instanceof LivingEntity)) {
                return;
            }
        }
        final GirlfriendOverlayTarget target = GirlfriendOverlayTarget.select(entity, player);
        if (target == null) {
            return;
        }
        outstring = target.name();
        gfHealth = target.ratio();
        // new ScaledResolution(mc, displayWidth, displayHeight).getScaledWidth() is the GUI-scaled width.
        final int width = guiGraphics.guiWidth();
        final int barWidthFilled = (int) (gfHealth * (barWidth + 1));
        final int x = width / 2 - barWidth / 2;
        int y = 25;
        // PORT: isInsideOfMaterial(Material.water) tested the eye position against the water surface, which is
        // 1.21.1's eye-in-fluid state; getTotalArmorValue() is the armor attribute.
        if (player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()) || player.getArmorValue() > 0) {
            y -= 10;
        }
        guiGraphics.drawString(fr, outstring, width / 2 - fr.width(outstring) / 2, y - 10, color, true);
        // bindTexture + glColor4f(1, 1, 1, 1) + drawTexturedModalRect (256x256 UV space) is blit with a white tint.
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(GirlfriendOverlayGui.texture, x, y, u, v, barWidth, barHeight);
        if (barWidthFilled > 0) {
            guiGraphics.blit(GirlfriendOverlayGui.texture, x, y, u, v + barHeight, barWidthFilled, barHeight);
        }
    }
}
