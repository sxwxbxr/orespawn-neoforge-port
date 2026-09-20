package com.swbr.orespawn.client.renderer.moth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.BrutalflyModel;
import com.swbr.orespawn.entity.moth.Brutalfly;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderBrutalfly} (RenderBrutalfly.java:9-81), a {@code RenderLiving}.
 * ClientProxyOreSpawn: {@code new RenderBrutalfly(new ModelBrutalfly(0.2f), 0.75f, 9.0f)} - register as
 * {@code ctx -> new RenderBrutalfly(ctx, 0.75f, 9.0f)}. Shadow {@code par2 * par3} = 6.75, scale 9.0, base texture
 * {@code Brutalflytexture.png}; the second render pass of {@code shouldRenderPass} (:43-70) is {@link OverlayPass}.
 */
public class RenderBrutalfly extends MobRenderer<Brutalfly, BrutalflyModel> {

    /** {@code overlay} (:78): {@code Brutalfly_overlay2.png}, the scrolling magma pattern. */
    private static final ResourceLocation overlay =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/brutalfly_overlay2.png");
    /** {@code texture} (:79): {@code Brutalflytexture.png}. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/brutalflytexture.png");

    protected BrutalflyModel model;
    private float scale = 1.0f;

    /** {@code RenderBrutalfly(model, par2, par3)} (:15-20). */
    public RenderBrutalfly(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new BrutalflyModel(context.bakeLayer(BrutalflyModel.LAYER), 0.2f), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new OverlayPass(this));
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)} (R8). */
    @Override
    protected void scale(final Brutalfly par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:72-74). */
    @Override
    public ResourceLocation getTextureLocation(final Brutalfly entity) {
        return texture;
    }

    /**
     * {@code shouldRenderPass} (:43-70) as a layer. Pass 1: the same model again with {@code Brutalfly_overlay2.png}, the
     * texture matrix translated by {@code (t·0.01, t·0.01)} with {@code t = ticksExisted + partialTicks}, colour 0.5,
     * lighting off, additive blend {@code (ONE, ONE)}, depth mask on. Pass 2 only restored that GL state.
     * {@code RenderType.energySwirl} is exactly this state set (vanilla's charged-creeper layer is the same 1.7.10 pass,
     * RenderButterfly W05), and {@code -8355712} is {@code 0xFF808080}, {@code glColor4f(0.5, 0.5, 0.5, 1)}.
     *
     * <p>{@code setRenderPassModel(this.model)} named the main model itself, whose angles the main pass has just set; it
     * is drawn again without a second {@code setupAnim} - the original's second {@code render()} computed the same angles.
     */
    public static class OverlayPass extends RenderLayer<Brutalfly, BrutalflyModel> {

        public OverlayPass(final RenderLayerParent<Brutalfly, BrutalflyModel> renderer) {
            super(renderer);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final Brutalfly par1EntityLiving, final float limbSwing, final float limbSwingAmount,
                           final float par3, final float ageInTicks, final float netHeadYaw, final float headPitch) {
            final float var4 = par1EntityLiving.tickCount + par3;
            final float var5 = var4 * 0.01f;
            final float var6 = var4 * 0.01f;
            // The texture repeats, so the offset modulo 1 samples the same texels with less float drift (vanilla does the same).
            final VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(overlay, var5 % 1.0f, var6 % 1.0f));
            this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -8355712);
        }
    }
}
