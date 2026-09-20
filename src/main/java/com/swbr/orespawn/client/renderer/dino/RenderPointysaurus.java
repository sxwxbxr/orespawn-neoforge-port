package com.swbr.orespawn.client.renderer.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.PointysaurusModel;
import com.swbr.orespawn.entity.dino.Pointysaurus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderPointysaurus} (RenderPointysaurus.java:10-49). ClientProxyOreSpawn.java:139
 * registered {@code new RenderPointysaurus(new ModelPointysaurus(1.0f), 1.0f, 1.0f)}: register with
 * {@code ctx -> new RenderPointysaurus(ctx, 1.0f, 1.0f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, {@code glScalef(scale)}, one texture ({@code Pointysaurustexture.png}, lower-cased by
 * the asset generator).
 */
public class RenderPointysaurus extends MobRenderer<Pointysaurus, PointysaurusModel> {

    protected PointysaurusModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/pointysaurustexture.png");

    /** {@code RenderPointysaurus(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderPointysaurus(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new PointysaurusModel(context.bakeLayer(PointysaurusModel.LAYER), 1.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale, scale, scale)} (R8). */
    @Override
    protected void scale(final Pointysaurus par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Pointysaurus entity) {
        return RenderPointysaurus.texture;
    }
}
