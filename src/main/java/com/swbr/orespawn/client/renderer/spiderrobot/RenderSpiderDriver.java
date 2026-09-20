package com.swbr.orespawn.client.renderer.spiderrobot;

import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.spiderrobot.SpiderDriver;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderSpiderDriver} (RenderSpiderDriver.java:9-39): the vanilla spider renderer - model,
 * eye layer ({@code spider_eyes.png}), 180 degree death flip - with {@code spiderdriver.png}. Registered as
 * {@code new RenderSpiderDriver(new ModelSpider(), 0.5f)} (manifest {@code renderer_args}).
 *
 * <p>The constructor body is empty (:13-14): the arguments are ignored and the implicit {@code RenderSpider()} runs,
 * which builds its own {@code ModelSpider} and passes shadow 1.0 ({@code bov.<init>}, javap on client-1.7.10.jar:
 * {@code fconst_1}). 1.21.1's {@link SpiderRenderer} passes 0.8, so the shadow is set back to 1.0 here.
 */
public class RenderSpiderDriver extends SpiderRenderer<SpiderDriver> {

    /** {@code spiderdriver.png}, moved by the asset generator to {@code textures/entity/}. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/spiderdriver.png");

    /** {@code RenderSpiderDriver(ModelSpider, float)} (:13-14): both arguments unused. */
    public RenderSpiderDriver(final EntityRendererProvider.Context context, final float par2) {
        super(context);
        this.shadowRadius = 1.0f;
    }

    /** {@code getEntityTexture} / {@code getSpiderTextures} (:28-34). */
    @Override
    public ResourceLocation getTextureLocation(final SpiderDriver entity) {
        return texture;
    }
}
