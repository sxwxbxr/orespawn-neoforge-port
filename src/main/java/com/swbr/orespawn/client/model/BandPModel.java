package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BandPGeometry;
import com.swbr.orespawn.entity.monster.BandP;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelBandP} (ModelBandP.java:7-103): 7 boxes, 64x128 texture, geometry
 * {@link BandPGeometry}. {@code render()} (:59-92): legs and belly with the walk, idle belly breathing and arm sway,
 * head yaw and pitch. The variant ({@code getWhat}) is not read, as in the original. No GL calls.
 */
public class BandPModel extends EntityModel<BandP> {

    /** Register with {@code BandPGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "criminal"), "main");

    /** {@code ModelBandP(float f1)}: {@code wingspeed = f1} (:19-20); ClientProxyOreSpawn passes 0.4 (manifest). */
    private final float wingspeed;

    private final ModelPart belly;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart lleg;
    private final ModelPart rleg;
    private final ModelPart larm;
    private final ModelPart rarm;

    public BandPModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.belly = root.getChild(BandPGeometry.BELLY);
        this.chest = root.getChild(BandPGeometry.CHEST);
        this.head = root.getChild(BandPGeometry.HEAD);
        this.lleg = root.getChild(BandPGeometry.LLEG);
        this.rleg = root.getChild(BandPGeometry.RLEG);
        this.larm = root.getChild(BandPGeometry.LARM);
        this.rarm = root.getChild(BandPGeometry.RARM);
    }

    /** The writes of {@code render()} (:63-84); {@code f4} is the head pitch. */
    @Override
    public void setupAnim(final BandP e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.belly.resetPose();
        this.chest.resetPose();
        this.head.resetPose();
        this.lleg.resetPose();
        this.rleg.resetPose();
        this.larm.resetPose();
        this.rarm.resetPose();
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float newangle3 = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
            newangle2 = Mth.cos(f2 * 2.6f * this.wingspeed) * 3.1415927f * 0.025f * f1;
            newangle3 = newangle;
        } else {
            newangle = 0.0f;
            newangle2 = Mth.cos(f2 * 0.6f * this.wingspeed) * 3.1415927f * 0.005f;
            newangle3 = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.02f;
        }
        this.lleg.xRot = newangle;
        this.rleg.xRot = -newangle;
        this.belly.xRot = 0.07f + newangle2;
        this.larm.xRot = -newangle3;
        this.rarm.xRot = newangle3;
        this.belly.yRot = -newangle / 2.0f;
        this.head.yRot = (float) Math.toRadians(f3);
        this.head.xRot = (float) Math.toRadians(f4);
    }

    /** The draw calls of {@code render()} (:85-91). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.belly.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.chest.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.larm.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rarm.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
