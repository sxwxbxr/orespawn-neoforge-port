package com.swbr.orespawn.client.renderer.island;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.IslandModel;
import com.swbr.orespawn.entity.island.Island;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderIsland} (RenderIsland.java:10-49). ClientProxyOreSpawn built it as
 * {@code new RenderIsland(new ModelIsland(1.0f), 0.25f, 1.0f)} (manifest {@code renderer_args}, {@code model_args});
 * the model is baked here, so the registration is {@code (ctx, 0.25f, 1.0f, 1.0f)}.
 *
 * <p>Shadow {@code par2 * par3} (:16); {@code preRenderScale} (:34-40) is {@link #scale}. The model and its layer
 * are shared with {@link RenderIslandToo}.
 */
public class RenderIsland extends MobRenderer<Island, IslandModel<Island>> {

    /** {@code Island.png} (:47), lower-cased by the asset generator. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/island.png");

    protected IslandModel<Island> model;
    private float scale = 1.0f;

    /** {@code RenderIsland(model, par2, par3)} (:15-20) with the model's (ignored) constructor argument. */
    public RenderIsland(final EntityRendererProvider.Context context, final float par2, final float par3, final float modelArg) {
        super(context, new IslandModel<>(context.bakeLayer(IslandModel.LAYER), modelArg), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Island par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Island entity) {
        return texture;
    }
}
