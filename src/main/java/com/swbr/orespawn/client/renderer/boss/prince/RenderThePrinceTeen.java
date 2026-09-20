package com.swbr.orespawn.client.renderer.boss.prince;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.ThePrinceTeenModel;
import com.swbr.orespawn.entity.boss.prince.ThePrinceTeen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderThePrinceTeen} (RenderThePrinceTeen.java:9-49), a {@code RenderLiving} with a
 * {@code ModelThePrinceTeen}. ClientProxyOreSpawn registered {@code new RenderThePrinceTeen(new ModelThePrinceTeen(0.65f),
 * 1.0f, 1.25f)} (manifest {@code renderer_args}, {@code model_args}): register with
 * {@code ctx -> new RenderThePrinceTeen(ctx, 1.0f, 1.25f)}; the model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}), one texture ({@code PrinceTeentexture.png},
 * lower-cased into {@code textures/entity/} by tools/assets.py). The model's blended membrane pass is
 * {@link MembraneLayer} (R8).
 */
public class RenderThePrinceTeen extends MobRenderer<ThePrinceTeen, ThePrinceTeenModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/princeteentexture.png");

    protected ThePrinceTeenModel model;
    private float scale;

    /** {@code RenderThePrinceTeen(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderThePrinceTeen(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new ThePrinceTeenModel(context.bakeLayer(ThePrinceTeenModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new MembraneLayer(this));
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale)};
     * {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final ThePrinceTeen par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final ThePrinceTeen entity) {
        return RenderThePrinceTeen.texture;
    }

    /**
     * The {@code GL_BLEND} block of {@code ModelThePrinceTeen.render()} (:870-884): the eight wing membranes with
     * {@code glColor4f(0.75, 0.75, 0.75, 0.55)}, as {@link RenderThePrince.WingLayer} (same PORT notes).
     */
    static final class MembraneLayer extends RenderLayer<ThePrinceTeen, ThePrinceTeenModel> {

        MembraneLayer(final RenderLayerParent<ThePrinceTeen, ThePrinceTeenModel> renderer) {
            super(renderer);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final ThePrinceTeen entity, final float limbSwing, final float limbSwingAmount, final float partialTick,
                           final float ageInTicks, final float netHeadYaw, final float headPitch) {
            if (entity.isInvisible()) {
                return;
            }
            this.getParentModel().renderWings(poseStack,
                    bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity))),
                    packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0f), RenderThePrince.WingLayer.WING_COLOR);
        }
    }
}
