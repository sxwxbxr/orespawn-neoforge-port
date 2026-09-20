package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.EasterBunnyGeometry;
import com.swbr.orespawn.entity.easter.EasterBunny;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelEasterBunny} (ModelEasterBunny.java:7-146): 13 boxes, 64x128 texture. Geometry
 * from the generated {@link EasterBunnyGeometry}; the animation is {@code render()} (:96-135): hind legs and feet
 * swing in opposite phase while walking, the ears twitch - with the walk, or a small idle twitch when standing.
 * {@code setRotationAngles} (:143-145) only calls the empty {@code ModelBase} method; the head does not follow the
 * look.
 */
public class EasterBunnyModel extends EntityModel<EasterBunny> {

    /** Register with {@code EasterBunnyGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "easter_bunny"), "main");

    /** {@code ModelEasterBunny(float f1)}: {@code wingspeed = f1} (:25-26); ClientProxyOreSpawn passes 0.55 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:122-134), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart lfoot;
    private final ModelPart lleg;
    private final ModelPart lear;
    private final ModelPart rleg;
    private final ModelPart rfoot;
    private final ModelPart rear;

    public EasterBunnyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[EasterBunnyGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(EasterBunnyGeometry.PARTS[i]);
        }
        this.lfoot = root.getChild(EasterBunnyGeometry.LFOOT);
        this.lleg = root.getChild(EasterBunnyGeometry.LLEG);
        this.lear = root.getChild(EasterBunnyGeometry.LEAR);
        this.rleg = root.getChild(EasterBunnyGeometry.RLEG);
        this.rfoot = root.getChild(EasterBunnyGeometry.RFOOT);
        this.rear = root.getChild(EasterBunnyGeometry.REAR);
    }

    /**
     * The angle writes of {@code render()} (:100-121). {@code f1} is the limb swing amount, {@code f2} the age in
     * ticks plus partial tick, as {@code RendererLivingEntity} handed them over.
     */
    @Override
    public void setupAnim(final EasterBunny entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.6f * this.wingspeed) * 3.1415927f * 0.15f * f1;
            newangle2 = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f * f1;
        } else {
            newangle = 0.0f;
            newangle2 = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.01f;
        }
        final float n = newangle;
        this.lfoot.xRot = n;
        this.lleg.xRot = n;
        final float n2 = -newangle;
        this.rfoot.xRot = n2;
        this.rleg.xRot = n2;
        // -0.226 and -0.418, not the constructor's -0.2268928 and -0.418879 (:120-121).
        this.lear.xRot = -0.226f + newangle2;
        this.rear.xRot = -0.418f - newangle2;
    }

    /** {@code render()} (:122-134): body, tail, lfoot, lleg, upperbody, head, nose, lear, lpaw, rleg, rfoot, rear, rpaw. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
