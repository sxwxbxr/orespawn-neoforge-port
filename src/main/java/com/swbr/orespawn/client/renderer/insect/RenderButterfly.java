package com.swbr.orespawn.client.renderer.insect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.client.model.ButterflyModel;
import com.swbr.orespawn.entity.insect.EntityButterfly;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderButterfly} (RenderButterfly.java:9-91), a {@code RenderLiving}
 * with a {@code ModelButterfly}. ClientProxyOreSpawn builds it as {@code (new ModelButterfly(w), shadow, scale)};
 * the model is baked here because 1.21.1 needs the context for it:
 * butterfly {@code (ctx, 0.3f, 1.0f, 1.0f)}, moth {@code (ctx, 0.4f, 1.5f, 0.75f)} (manifest
 * {@code renderer_args}/{@code model_args}); Mothra (W08) registers its own instance.
 *
 * <p>Shadow {@code par2 * par3} (:16); {@code preRenderScale} {@code glScalef(scale)} (:34-40) is
 * {@link #scale}; the texture comes from the entity (:83-86). The second render pass of
 * {@code shouldRenderPass} (:42-81) is {@link GlowPass}.
 */
public class RenderButterfly<T extends EntityButterfly> extends MobRenderer<T, ButterflyModel<T>> {

    /** {@code texture} (:88-90): the vanilla charged-creeper swirl. */
    private static final ResourceLocation texture =
            ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");

    protected ButterflyModel<T> model;
    private float scale = 1.0f;

    /** {@code RenderButterfly(model, par2, par3)} (:15-20) with the model's {@code wingspeed}. */
    public RenderButterfly(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new ButterflyModel<>(context.bakeLayer(ButterflyModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new GlowPass<>(this));
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:34-40), after the body rotation as in 1.7.10. */
    @Override
    protected void scale(final T par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:83-86). */
    @Override
    public ResourceLocation getTextureLocation(final T entity) {
        return entity.getTexture();
    }

    /**
     * {@code shouldRenderPass} (:42-81) as a layer. Pass 1, for Mothra and the type-0 moth
     * ({@link EntityButterfly#hasGlowPass()}): the same model again with {@code creeper_armor.png}, the
     * texture matrix translated by {@code (t·0.01, t·0.01)} with {@code t = ticksExisted + partialTicks},
     * colour 0.5, lighting off, additive blend {@code (ONE, ONE)}. Pass 2 only restored that GL state.
     * {@code RenderType.energySwirl} is exactly this state set - vanilla's charged-creeper layer is the
     * same 1.7.10 pass - and {@code -8355712} is {@code 0xFF808080}, {@code glColor4f(0.5, 0.5, 0.5, 1)}.
     *
     * <p>{@code setRenderPassModel(this.model)} named the main model itself, whose angles the main pass
     * has just set; it is drawn again without a second {@code setupAnim}.
     */
    public static class GlowPass<T extends EntityButterfly> extends RenderLayer<T, ButterflyModel<T>> {

        public GlowPass(final RenderLayerParent<T, ButterflyModel<T>> renderer) {
            super(renderer);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final T par1EntityLiving, final float limbSwing, final float limbSwingAmount,
                           final float par3, final float ageInTicks, final float netHeadYaw, final float headPitch) {
            if (!par1EntityLiving.hasGlowPass()) {
                return;
            }
            final float var4 = par1EntityLiving.tickCount + par3;
            final float var5 = var4 * 0.01f;
            final float var6 = var4 * 0.01f;
            // The texture repeats, so the offset modulo 1 samples the same texels with less float drift (vanilla does the same).
            final VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(texture, var5 % 1.0f, var6 % 1.0f));
            this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -8355712);
        }
    }
}
