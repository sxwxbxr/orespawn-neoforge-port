package com.swbr.orespawn.client.renderer.antrobot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.AntRobotModel;
import com.swbr.orespawn.entity.antrobot.AntRobot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderAntRobot} (RenderAntRobot.java:9-55), registered with the arguments of the
 * original ({@code new RenderAntRobot(new ModelAntRobot(1.0f), 0.99f, 1.0f)}, manifest):
 * {@code ctx -> new RenderAntRobot(ctx, 1.0f, 0.99f, 1.0f)}.
 *
 * <p>Both {@code doRender} overloads (:32-38) go to {@code renderAntRobot} (:22-30), which bypasses
 * {@code RendererLivingEntity.doRender} entirely: no body-yaw interpolation, no {@code preRenderCallback} scale, no
 * hurt or death tint, no death roll, no name tag, no leash, and no -1.5 block model offset - the model's origin sits
 * at the entity position with Y pointing down. Only the shadow (drawn by the render manager, 0.99) and fire remain.
 * The port overrides {@link #render} the same way and does not call {@code super}.
 *
 * <p>Texture {@code AntRobottexture.png}, lower-cased under {@code textures/entity/} by tools/assets.py.
 */
public class RenderAntRobot extends MobRenderer<AntRobot, AntRobotModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/antrobottexture.png");

    protected AntRobotModel model;
    private float scale;

    /**
     * {@code RenderAntRobot(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelAntRobot} constructor argument
     */
    public RenderAntRobot(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new AntRobotModel(context.bakeLayer(AntRobotModel.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /**
     * {@code renderAntRobot} (:22-30). The dispatcher has already translated to the entity ({@code glTranslatef}),
     * {@code entityYaw} is the interpolated yaw the 1.7.10 render manager passed as {@code par8}.
     *
     * <p>PORT: {@code RenderType.entityCutoutNoCull} as every model of this port; no overlay (the original drew no hurt
     * tint here) and full colour.
     */
    @Override
    public void render(final AntRobot par1EntityAntRobot, final float entityYaw, final float partialTicks, final PoseStack poseStack,
                       final MultiBufferSource buffer, final int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entityYaw));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.model.setupAnim(par1EntityAntRobot, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f);
        final VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(par1EntityAntRobot)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }

    /**
     * {@code preRenderCallback} -> {@code preRenderScale} (:40-46): {@code glScalef(scale, scale, scale)}. Never
     * reached in the original (the living render is bypassed) and never called by {@link #render} here.
     */
    @Override
    protected void scale(final AntRobot par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:48-50). */
    @Override
    public ResourceLocation getTextureLocation(final AntRobot entity) {
        return texture;
    }
}
