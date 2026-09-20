package com.swbr.orespawn.client.renderer.terror;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CrabModel;
import com.swbr.orespawn.entity.terror.Crab;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCrab} (RenderCrab.java:9-50). ClientProxyOreSpawn registered it as
 * {@code new RenderCrab(new ModelCrab(1.0f), 0.99, 1.0)} (manifest): register with
 * {@code ctx -> new RenderCrab(ctx, 0.99f, 1.0f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3} (not scaled with the crab, as in 1.7.10). {@code scale = par3} is kept but unused: the
 * model is scaled by the crab's own synchronised scale. One texture ({@code RobotCrabtexture.png}, lower-cased).
 */
public class RenderCrab extends MobRenderer<Crab, CrabModel> {

    protected CrabModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/robotcrabtexture.png");

    /** {@code RenderCrab(model, par2, par3)}: shadow {@code par2 * par3}. */
    public RenderCrab(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new CrabModel(context.bakeLayer(CrabModel.LAYER), 1.0f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(getCrabScale())} (R8). */
    @Override
    protected void scale(final Crab par1Entity, final PoseStack poseStack, final float par2) {
        final float pscale = par1Entity.getCrabScale();
        poseStack.scale(pscale, pscale, pscale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Crab entity) {
        return RenderCrab.texture;
    }
}
