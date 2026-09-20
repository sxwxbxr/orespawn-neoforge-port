package com.swbr.orespawn.client.item.bertha;

import com.swbr.orespawn.entity.arrow.BerthaHit;
import com.swbr.orespawn.entity.arrow.IrukandjiArrow;
import com.swbr.orespawn.entity.arrow.UltimateArrow;
import com.swbr.orespawn.entity.arrow.UltimateFishHook;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Entity renderers of the four w04-bows-bertha entities. None of them had an OreSpawn renderer:
 *
 * <ul>
 *   <li>{@code UltimateArrow} - {@code RenderArrow} (manifest renderers; design-entities-06.md):
 *       the vanilla arrow with the vanilla arrow texture.</li>
 *   <li>{@code IrukandjiArrow} - <em>no</em> registration at all (catalogue 6.6). 1.7.10's
 *       {@code RenderManager.getEntityClassRenderObject} walked the superclass chain when a class had
 *       no renderer, so the Irukandji arrow was drawn by {@code RenderArrow} of its parent
 *       {@code EntityArrow} with {@code textures/entity/arrow.png}. OreSpawn ships no arrow-sheet
 *       texture for it - {@code irukandjiarrow.png} is the 16x16 inventory icon, not an entity
 *       texture - so the vanilla arrow texture is the original look, not a substitute.</li>
 *   <li>{@code UltimateFishHook} - {@code RenderFish} (manifest): the vanilla bobber and line;
 *       1.21.1's {@link FishingHookRenderer} finds the rod through {@code ItemAbilities.FISHING_ROD_CAST},
 *       which {@code item.bow.UltimateFishingRod} reports.</li>
 *   <li>{@code BerthaHit} - {@code RenderItemUrchin}, whose {@code doRender} returned at once for
 *       {@code instanceof BerthaHit} (RenderItemUrchin.java:9-10): nothing is drawn -
 *       {@link NoopRenderer}.</li>
 * </ul>
 *
 * <p>This class lives next to the Bertha item renderers only because that is the client path of its
 * porter; it has nothing item-specific and can move to {@code client.renderer} unchanged.
 */
public final class ArrowRenderers {

    private ArrowRenderers() {}

    /** {@code RenderingRegistry.registerEntityRenderingHandler} equivalents for the four types. */
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event,
                                         EntityType<UltimateArrow> ultimateArrow,
                                         EntityType<IrukandjiArrow> irukandjiArrow,
                                         EntityType<UltimateFishHook> ultimateFishHook,
                                         EntityType<BerthaHit> berthaHit) {
        event.<UltimateArrow>registerEntityRenderer(ultimateArrow, VanillaArrowRenderer::new);
        event.<IrukandjiArrow>registerEntityRenderer(irukandjiArrow, VanillaArrowRenderer::new);
        event.<FishingHook>registerEntityRenderer(ultimateFishHook, FishingHookRenderer::new);
        event.<BerthaHit>registerEntityRenderer(berthaHit, NoopRenderer::new);
    }

    /** 1.7.10 {@code RenderArrow}: the plain arrow texture, never the tipped one. */
    private static final class VanillaArrowRenderer<T extends AbstractArrow> extends ArrowRenderer<T> {

        VanillaArrowRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(T entity) {
            return TippableArrowRenderer.NORMAL_ARROW_LOCATION;
        }
    }
}
