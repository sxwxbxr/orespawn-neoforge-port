package com.swbr.orespawn.client.renderer.herbivore;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.BaryonyxModel;
import com.swbr.orespawn.entity.herbivore.Baryonyx;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderBaryonyx} (RenderBaryonyx.java:10-53). ClientProxyOreSpawn registered it as
 * {@code new RenderBaryonyx(new ModelBaryonyx(0.25f), 1.0f, 1.0f)} (manifest {@code renderer_args}, {@code model_args}):
 * register with {@code ctx -> new RenderBaryonyx(ctx, 1.0f, 1.0f)}; the model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, halved for a baby ({@code preRenderScale}), one texture
 * ({@code Baryonyx.png}, lower-cased by the asset generator).
 */
public class RenderBaryonyx extends MobRenderer<Baryonyx, BaryonyxModel> {

    protected BaryonyxModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/baryonyx.png");

    /** {@code RenderBaryonyx(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderBaryonyx(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new BaryonyxModel(context.bakeLayer(BaryonyxModel.LAYER), 0.25f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale / 2)} for a child, else
     * {@code glScalef(scale)} - {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final Baryonyx par1Entity, final PoseStack poseStack, final float par2) {
        if (par1Entity != null && par1Entity.isBaby()) {
            poseStack.scale(this.scale / 2.0f, this.scale / 2.0f, this.scale / 2.0f);
            return;
        }
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Baryonyx entity) {
        return RenderBaryonyx.texture;
    }
}
