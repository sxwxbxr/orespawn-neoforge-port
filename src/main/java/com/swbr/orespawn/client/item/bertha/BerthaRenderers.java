package com.swbr.orespawn.client.item.bertha;

import com.swbr.orespawn.client.model.BerthaModel;
import com.swbr.orespawn.client.model.HammyModel;
import com.swbr.orespawn.client.model.SliceModel;
import com.swbr.orespawn.client.model.geom.BerthaGeometry;
import com.swbr.orespawn.client.model.geom.HammyGeometry;
import com.swbr.orespawn.client.model.geom.SliceGeometry;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Wiring for the four Bertha-family item renderers - the successor of
 * {@code MinecraftForgeClient.registerItemRenderer(OreSpawnMain.MyBertha, new RenderBertha())} and
 * its siblings for {@code MySlice}, {@code MyRoyal} and {@code MyHammy} (ClientProxyOreSpawn.java,
 * item-renderer block; catalogue verhalten/itemblock-01.md "Bertha", Client). Same pattern as the
 * W03 {@code client.item.BigWeaponRenderers}: called from the hubs in {@code client.ClientSetup},
 * item holders passed in, renderer created lazily on first use because
 * {@code RegisterClientExtensionsEvent} fires before {@code Minecraft.entityModels} exists.
 */
public final class BerthaRenderers {

    private BerthaRenderers() {}

    /** The three layers (Slice and Royal share {@code ModelSlice}). */
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BerthaModel.LAYER, BerthaGeometry::createBodyLayer);
        event.registerLayerDefinition(SliceModel.LAYER, SliceGeometry::createBodyLayer);
        event.registerLayerDefinition(HammyModel.LAYER, HammyGeometry::createBodyLayer);
    }

    /** The four item renderers. */
    public static void registerItemExtensions(RegisterClientExtensionsEvent event,
                                              Holder<? extends Item> bertha,
                                              Holder<? extends Item> slice,
                                              Holder<? extends Item> royal,
                                              Holder<? extends Item> hammy) {
        event.registerItem(lazy(RenderBertha::new), bertha.value());
        event.registerItem(lazy(RenderSlice::new), slice.value());
        event.registerItem(lazy(RenderRoyal::new), royal.value());
        event.registerItem(lazy(RenderHammy::new), hammy.value());
    }

    private static IClientItemExtensions lazy(Supplier<? extends BlockEntityWithoutLevelRenderer> factory) {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = factory.get();
                }
                return this.renderer;
            }
        };
    }
}
