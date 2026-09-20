package com.swbr.orespawn.client.renderer.terror;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.TerribleTerrorModel;
import com.swbr.orespawn.entity.terror.TerribleTerror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderTerribleTerror} (RenderTerribleTerror.java:9-49). ClientProxyOreSpawn registered
 * it as {@code new RenderTerribleTerror(new ModelTerribleTerror(), 0.45, 0.75)} (manifest): register with
 * {@code ctx -> new RenderTerribleTerror(ctx, 0.45f, 0.75f)}; the model is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3}, one texture ({@code TerribleTerror.png}, lower-cased).
 */
public class RenderTerribleTerror extends MobRenderer<TerribleTerror, TerribleTerrorModel> {

    protected TerribleTerrorModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/terribleterror.png");

    /** {@code RenderTerribleTerror(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderTerribleTerror(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new TerribleTerrorModel(context.bakeLayer(TerribleTerrorModel.LAYER)), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final TerribleTerror par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final TerribleTerror entity) {
        return RenderTerribleTerror.texture;
    }
}
