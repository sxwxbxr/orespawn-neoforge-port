package com.swbr.orespawn.client.item.squidzooka;

import com.swbr.orespawn.client.model.SquidZookaModel;
import com.swbr.orespawn.client.model.geom.SquidZookaGeometry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Wiring for the SquidZooka item renderer - the successor of
 * {@code MinecraftForgeClient.registerItemRenderer(OreSpawnMain.MySquidZooka, new RenderSquidZooka())}
 * (ClientProxyOreSpawn.java:152). Same pattern as {@code client.item.bertha.BerthaRenderers}: called from the hubs in
 * {@code client.ClientSetup}, the item holder passed in, the renderer created lazily on first use because
 * {@code RegisterClientExtensionsEvent} fires before {@code Minecraft.entityModels} exists.
 */
public final class SquidZookaRenderers {

    private SquidZookaRenderers() {}

    /** The model layer. */
    public static void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SquidZookaModel.LAYER, SquidZookaGeometry::createBodyLayer);
    }

    /** The item renderer. */
    public static void registerItemExtensions(final RegisterClientExtensionsEvent event, final Holder<? extends Item> squidZooka) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new RenderSquidZooka();
                }
                return this.renderer;
            }
        }, squidZooka.value());
    }
}
