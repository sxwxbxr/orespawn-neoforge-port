package com.swbr.orespawn.client.renderer.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.AlosaurusModel;
import com.swbr.orespawn.entity.dino.Alosaurus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderAlosaurus} (RenderAlosaurus.java:10-49). ClientProxyOreSpawn.java:46 registered
 * {@code new RenderAlosaurus(new ModelAlosaurus(0.22f), 1.0f, 1.0f)}: register with
 * {@code ctx -> new RenderAlosaurus(ctx, 1.0f, 1.0f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, {@code glScalef(scale)} in {@code preRenderCallback}, one texture
 * ({@code alosaurus.png}).
 */
public class RenderAlosaurus extends MobRenderer<Alosaurus, AlosaurusModel> {

    protected AlosaurusModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/alosaurus.png");

    /** {@code RenderAlosaurus(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderAlosaurus(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new AlosaurusModel(context.bakeLayer(AlosaurusModel.LAYER), 0.22f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale, scale, scale)} (R8). */
    @Override
    protected void scale(final Alosaurus par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Alosaurus entity) {
        return RenderAlosaurus.texture;
    }
}
