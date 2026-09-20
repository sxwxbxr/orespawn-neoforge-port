package com.swbr.orespawn.client.renderer.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.SpitBugModel;
import com.swbr.orespawn.entity.arthropod.SpitBug;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderSpitBug} (RenderSpitBug.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderSpitBug(new ModelSpitBug(0.55f), 0.55f, 0.75f)} (manifest): register with
 * {@code ctx -> new RenderSpitBug(ctx, 0.55f, 0.75f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code BlisterBug.png}, lower-cased).
 */
public class RenderSpitBug extends MobRenderer<SpitBug, SpitBugModel> {

    protected SpitBugModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/blisterbug.png");

    /** {@code RenderSpitBug(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderSpitBug(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new SpitBugModel(context.bakeLayer(SpitBugModel.LAYER), 0.55f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final SpitBug par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final SpitBug entity) {
        return RenderSpitBug.texture;
    }
}
