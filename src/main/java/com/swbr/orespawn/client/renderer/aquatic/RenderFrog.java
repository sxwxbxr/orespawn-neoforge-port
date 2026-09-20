package com.swbr.orespawn.client.renderer.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.FrogModel;
import com.swbr.orespawn.entity.aquatic.Frog;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderFrog} (RenderFrog.java:9-49). ClientProxyOreSpawn built it as
 * {@code new RenderFrog(new ModelFrog(1.0f), 0.35f, 1.0f)} (ClientProxyOreSpawn.java:142); the model is baked here,
 * so the registration is {@code (ctx, 0.35f, 1.0f, 1.0f)}.
 *
 * <p>Shadow {@code par2 * par3} (:16); {@code preRenderScale} (:34-40) is {@link #scale}.
 */
public class RenderFrog extends MobRenderer<Frog, FrogModel> {

    /** {@code Frogtexture.png} (:47), lower-cased by the asset generator. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/frogtexture.png");

    protected FrogModel model;
    private float scale = 1.0f;

    /** {@code RenderFrog(model, par2, par3)} (:15-20) with the model's {@code wingspeed}. */
    public RenderFrog(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new FrogModel(context.bakeLayer(FrogModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40). */
    @Override
    protected void scale(final Frog par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Frog entity) {
        return texture;
    }
}
