package com.swbr.orespawn.client.renderer.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.NastysaurusModel;
import com.swbr.orespawn.entity.dino.Nastysaurus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderNastysaurus} (RenderNastysaurus.java:10-49). ClientProxyOreSpawn.java:138
 * registered {@code new RenderNastysaurus(new ModelNastysaurus(0.65f), 1.0f, 1.5f)}: register with
 * {@code ctx -> new RenderNastysaurus(ctx, 1.0f, 1.5f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, {@code glScalef(scale)}, one texture ({@code Nastysaurustexture.png}, lower-cased by
 * the asset generator).
 */
public class RenderNastysaurus extends MobRenderer<Nastysaurus, NastysaurusModel> {

    protected NastysaurusModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/nastysaurustexture.png");

    /** {@code RenderNastysaurus(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderNastysaurus(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new NastysaurusModel(context.bakeLayer(NastysaurusModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale, scale, scale)} (R8). */
    @Override
    protected void scale(final Nastysaurus par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Nastysaurus entity) {
        return RenderNastysaurus.texture;
    }
}
