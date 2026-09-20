package com.swbr.orespawn.client.renderer.boss.prince;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.ThePrincessModel;
import com.swbr.orespawn.entity.boss.prince.ThePrincess;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderThePrincess} (RenderThePrincess.java:9-55), a {@code RenderLiving} with a
 * {@code ModelThePrincess}. ClientProxyOreSpawn registered {@code new RenderThePrincess(new ModelThePrincess(0.65f), 0.7f,
 * 0.7f)} (manifest {@code renderer_args}, {@code model_args}): register with
 * {@code ctx -> new RenderThePrincess(ctx, 0.7f, 0.7f)}; the model's constructor argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, scale {@code par3} ({@code preRenderScale}), texture by the synced attacking flag:
 * {@code ThePrincesstexture2.png} while attacking, else {@code ThePrincesstexture.png} (lower-cased into
 * {@code textures/entity/} by tools/assets.py). The model's blended pass (wings and power cubes) is {@link PowerLayer},
 * with the same texture (R8).
 */
public class RenderThePrincess extends MobRenderer<ThePrincess, ThePrincessModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/theprincesstexture.png");
    private static final ResourceLocation texture2 =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/theprincesstexture2.png");

    protected ThePrincessModel model;
    private float scale;

    /** {@code RenderThePrincess(model, par2, par3)} (:16-21): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderThePrincess(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new ThePrincessModel(context.bakeLayer(ThePrincessModel.LAYER), 0.65f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
        this.addLayer(new PowerLayer(this));
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:35-41): {@code glScalef(scale)};
     * {@code LivingEntityRenderer.scale()} sits at the same point of the transform chain (R8).
     */
    @Override
    protected void scale(final ThePrincess par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:43-49). */
    @Override
    public ResourceLocation getTextureLocation(final ThePrincess entity) {
        final ThePrincess t = entity;
        if (t.getAttacking() != 0) {
            return RenderThePrincess.texture2;
        }
        return RenderThePrincess.texture;
    }

    /**
     * The {@code GL_BLEND} block of {@code ModelThePrincess.render()} (:514-530): wings with
     * {@code glColor4f(0.75, 0.75, 0.75, 0.55)} and the full-bright power cubes, as {@link RenderThePrince.WingLayer}
     * (same PORT notes).
     */
    static final class PowerLayer extends RenderLayer<ThePrincess, ThePrincessModel> {

        PowerLayer(final RenderLayerParent<ThePrincess, ThePrincessModel> renderer) {
            super(renderer);
        }

        @Override
        public void render(final PoseStack poseStack, final MultiBufferSource bufferSource, final int packedLight,
                           final ThePrincess entity, final float limbSwing, final float limbSwingAmount, final float partialTick,
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
