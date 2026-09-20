package com.swbr.orespawn.client.renderer.terror;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.LurkingTerrorModel;
import com.swbr.orespawn.entity.terror.LurkingTerror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderLurkingTerror} (RenderLurkingTerror.java:9-49). ClientProxyOreSpawn registered
 * it as {@code new RenderLurkingTerror(new ModelLurkingTerror(), 0.45, 0.85)} (manifest): register with
 * {@code ctx -> new RenderLurkingTerror(ctx, 0.45f, 0.85f)}; the model is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code LurkingTerror.png}, lower-cased).
 */
public class RenderLurkingTerror extends MobRenderer<LurkingTerror, LurkingTerrorModel> {

    protected LurkingTerrorModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/lurkingterror.png");

    /** {@code RenderLurkingTerror(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderLurkingTerror(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new LurkingTerrorModel(context.bakeLayer(LurkingTerrorModel.LAYER)), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final LurkingTerror par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final LurkingTerror entity) {
        return RenderLurkingTerror.texture;
    }
}
