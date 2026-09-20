package com.swbr.orespawn.client.renderer.spiderrobot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.SpiderRobotModel;
import com.swbr.orespawn.entity.spiderrobot.SpiderRobot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderSpiderRobot} (RenderSpiderRobot.java:9-55), registered as
 * {@code new RenderSpiderRobot(new ModelSpiderRobot(1.0f), 0.99f, 1.0f)} (ClientProxyOreSpawn.java:144).
 *
 * <p>The class extends {@code RenderLiving} but overrides both {@code doRender} methods with its own
 * {@code renderSpiderRobot}, which never reaches {@code RendererLivingEntity.doRender}: no hurt tint, no death rotation,
 * no body yaw, no name tag, and {@code preRenderCallback}/{@code preRenderScale} (:41-47) are never called. The port is a
 * plain {@link EntityRenderer} for that reason.
 *
 * <p>The shadow is drawn: 1.7.10 {@code RenderManager.func_147939_a} calls {@code doRenderShadowAndFire} after
 * {@code doRender} for every renderer (javap {@code bnn.a(Lsa;DDDFFZ)Z}, pc 82), with the {@code shadowSize}
 * {@code par2 * par3} = 0.99 from the {@code RenderLiving} constructor. PORT: design/design-entities-05.md says "kein
 * Schatten"; the bytecode says otherwise, and the shadow stays.
 */
public class RenderSpiderRobot extends EntityRenderer<SpiderRobot> {

    /** {@code SpiderRobottexture.png}, moved by the asset generator to {@code textures/entity/}. */
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/spiderrobottexture.png");

    protected final SpiderRobotModel model;
    /** {@code scale} (:13, :18): only read by the never-called {@code preRenderScale}. */
    @SuppressWarnings("unused")
    private final float scale;

    /** {@code RenderSpiderRobot(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderSpiderRobot(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context);
        this.shadowRadius = par2 * par3;
        this.model = new SpiderRobotModel(context.bakeLayer(SpiderRobotModel.LAYER), 1.0f);
        this.scale = par3;
    }

    /**
     * {@code renderSpiderRobot} (:22-31). The {@code glTranslatef(x, y, z)} of :24 is done by the dispatcher, which
     * passes the same interpolated yaw as 1.7.10's {@code RenderManager}. No {@code -1.5} offset: model y = 0 is the
     * entity's feet and model -y points up after the flip.
     */
    @Override
    public void render(final SpiderRobot par1EntitySpiderRobot, final float par8, final float par9,
                       final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - par8)); // :25
        poseStack.scale(-1.0f, -1.0f, 1.0f);                        // :27
        // :28 model.render(entity, 0, 0, -0.1, 0, 0, 0.0625)
        this.model.setupAnim(par1EntitySpiderRobot, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f);
        final VertexConsumer consumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(par1EntitySpiderRobot)));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        // No super.render(): in 1.21.1 that draws the leash and the name tag, which this renderer never did.
    }

    /** {@code getEntityTexture} (:49-51). */
    @Override
    public ResourceLocation getTextureLocation(final SpiderRobot entity) {
        return texture;
    }
}
