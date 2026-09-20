package com.swbr.orespawn.client.renderer.boss.prince;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.ThePrinceModel;
import com.swbr.orespawn.entity.boss.prince.ThePrince;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderThePrince} (RenderThePrince.java:9-49), a {@code RenderLiving} with a
 * {@code ModelThePrince}. ClientProxyOreSpawn registered {@code new RenderThePrince(new ModelThePrince(0.65f), 0.75f,
 * 0.75f)} (manifest {@code renderer_args}, {@code model_args}): register with
 * {@code ctx -> new RenderThePrince(ctx, 0.75f, 0.75f)}; the model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}), one texture
 * ({@code ThePrincetexture.png}, lower-cased into {@code textures/entity/} by tools/assets.py). The model's blended wing
 * pass is {@link WingLayer} (R8).
 */
public class RenderThePrince extends MobRenderer<ThePrince, ThePrinceModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/theprincetexture.png");

    protected ThePrinceModel model;
    private float scale;

    /** {@code RenderThePrince(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderThePrince(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new ThePrinceModel(context.bakeLayer(ThePrinceModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new WingLayer(this));
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)};
     * {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final ThePrince par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final ThePrince entity) {
        return RenderThePrince.texture;
    }

    /**
     * The {@code GL_BLEND} block of {@code ModelThePrince.render()} (:448-460): the wings with
     * {@code glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)} and {@code glColor4f(0.75, 0.75, 0.75, 0.55)}, drawn right after
     * the body with the same transform. That is {@link RenderType#entityTranslucent} with that colour (R8); the layer
     * runs inside the same pose as the model, after {@code setupAnim}.
     *
     * <p>PORT: the blended quads are sorted back to front with the other translucent geometry of the frame instead of
     * being drawn immediately after the body; a hurt flash uses the red overlay of the body pass. An invisible prince
     * draws no wings, as 1.7.10 drew no model.
     */
    static final class WingLayer extends RenderLayer<ThePrince, ThePrinceModel> {

        /** {@code glColor4f(0.75f, 0.75f, 0.75f, 0.55f)} as ARGB. */
        static final int WING_COLOR = (Math.round(0.55f * 255.0f) << 24) | (Math.round(0.75f * 255.0f) << 16)
                | (Math.round(0.75f * 255.0f) << 8) | Math.round(0.75f * 255.0f);

        WingLayer(final RenderLayerParent<ThePrince, ThePrinceModel> renderer) {
            super(renderer);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final ThePrince entity, final float limbSwing, final float limbSwingAmount, final float partialTick,
                           final float ageInTicks, final float netHeadYaw, final float headPitch) {
            if (entity.isInvisible()) {
                return;
            }
            this.getParentModel().renderWings(poseStack,
                    bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity))),
                    packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0f), WING_COLOR);
        }
    }
}
