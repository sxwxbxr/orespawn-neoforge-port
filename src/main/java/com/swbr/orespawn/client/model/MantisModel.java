package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.MantisGeometry;
import com.swbr.orespawn.entity.terror.Mantis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelMantis} (ModelMantis.java:7-315): 36 boxes, 256x256 texture, geometry from the
 * generated {@link MantisGeometry}. The animation is {@code render()} (:234-290): beating fore and hind wings and two
 * three-segment raptorial forelegs whose pivots follow the segment before; they strike while the mantis attacks
 * (DataWatcher 20). No GL calls.
 */
public class MantisModel extends EntityModel<Mantis> {

    /** Register with {@code MantisGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "mantis"), "main");

    /** {@code ModelMantis(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 2.0 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order, which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart lfwing;
    private final ModelPart rfwing;
    private final ModelPart lrwing;
    private final ModelPart rrwing;
    private final ModelPart larm1;
    private final ModelPart larm2;
    private final ModelPart larm3;
    private final ModelPart rarm1;
    private final ModelPart rarm2;
    private final ModelPart rarm3;

    public MantisModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[MantisGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(MantisGeometry.PARTS[i]);
        }
        this.lfwing = root.getChild(MantisGeometry.LFWING);
        this.rfwing = root.getChild(MantisGeometry.RFWING);
        this.lrwing = root.getChild(MantisGeometry.LRWING);
        this.rrwing = root.getChild(MantisGeometry.RRWING);
        this.larm1 = root.getChild(MantisGeometry.LARM1);
        this.larm2 = root.getChild(MantisGeometry.LARM2);
        this.larm3 = root.getChild(MantisGeometry.LARM3);
        this.rarm1 = root.getChild(MantisGeometry.RARM1);
        this.rarm2 = root.getChild(MantisGeometry.RARM2);
        this.rarm3 = root.getChild(MantisGeometry.RARM3);
    }

    /** The writes of {@code render()} (:239-270). */
    @Override
    public void setupAnim(final Mantis b, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.25f;
        this.lfwing.zRot = -0.698f - newangle;
        this.rfwing.zRot = 0.698f + newangle;
        newangle = Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.35f;
        this.lrwing.zRot = -0.349f + newangle;
        this.rrwing.zRot = 0.349f - newangle;
        float a1;
        if (b.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.051f * this.wingspeed) * 3.1415927f * 0.013f;
            a1 = -0.2f;
        } else {
            newangle = Mth.cos(f2 * 0.51f * this.wingspeed) * 3.1415927f * 0.25f;
            a1 = -0.698f;
        }
        this.larm1.xRot = a1 + newangle;
        this.larm2.z = (float) (this.larm1.z + 1.0f + Math.sin(this.larm1.xRot) * 22.0);
        this.larm2.y = (float) (this.larm1.y + Math.cos(this.larm1.xRot) * 22.0);
        this.larm2.xRot = -a1 - newangle;
        this.larm3.z = (float) (this.larm2.z + 1.0f - Math.sin(this.larm2.xRot) * 17.0);
        this.larm3.y = (float) (this.larm2.y - Math.cos(this.larm2.xRot) * 17.0);
        this.larm3.xRot = a1 + newangle;
        this.rarm1.xRot = a1 - newangle;
        this.rarm2.z = (float) (this.rarm1.z + 1.0f + Math.sin(this.rarm1.xRot) * 22.0);
        this.rarm2.y = (float) (this.rarm1.y + Math.cos(this.rarm1.xRot) * 22.0);
        this.rarm2.xRot = -a1 + newangle;
        this.rarm3.z = (float) (this.rarm2.z + 1.0f - Math.sin(this.rarm2.xRot) * 17.0);
        this.rarm3.y = (float) (this.rarm2.y - Math.cos(this.rarm2.xRot) * 17.0);
        this.rarm3.xRot = a1 - newangle;
    }

    /** {@code render()} (:271-306): every part once, in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
