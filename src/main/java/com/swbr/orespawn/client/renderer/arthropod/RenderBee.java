package com.swbr.orespawn.client.renderer.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.BeeModel;
import com.swbr.orespawn.entity.arthropod.Bee;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderBee} (RenderBee.java:9-49). ClientProxyOreSpawn registered it as
 * {@code new RenderBee(new ModelBee(2.0f), 0.9f, 1.1f)} (manifest): register with
 * {@code ctx -> new RenderBee(ctx, 0.9f, 1.1f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code Beetexture.png}, lower-cased).
 */
public class RenderBee extends MobRenderer<Bee, BeeModel> {

    protected BeeModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/beetexture.png");

    /** {@code RenderBee(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderBee(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new BeeModel(context.bakeLayer(BeeModel.LAYER), 2.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Bee par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Bee entity) {
        return RenderBee.texture;
    }
}
