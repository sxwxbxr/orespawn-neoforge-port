package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.VelocityRaptorGeometry;
import com.swbr.orespawn.entity.rider.VelocityRaptor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelVelocityRaptor} (ModelVelocityRaptor.java:7-325), geometry
 * {@link VelocityRaptorGeometry} (128x128, 34 parts). {@code wingspeed} is the constructor argument, 1.25 in
 * ClientProxyOreSpawn (manifest {@code model_args}).
 *
 * <p>Animation from {@code render()} (:228-275): hind legs swing with {@code limbSwingAmount} above 0.1; the head
 * feathers and tail feathers flutter scaled by the health fraction ({@code getVHealth() / getMaxHealth()}), the tail
 * feathers stop while sitting; the arms and their feathers wave slowly. The two hat parts show the battle mob's
 * activation (1 = brim, 2 = brim and crown). No GL calls: {@link RenderType#entityCutoutNoCull}.
 */
public class VelocityRaptorModel extends EntityModel<VelocityRaptor> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "velocity_raptor"), "main");

    private final float wingspeed;
    private final ModelPart hf3;
    private final ModelPart hf4;
    private final ModelPart hf2;
    private final ModelPart hf1;
    private final ModelPart lff2;
    private final ModelPart lff1;
    private final ModelPart lff3;
    private final ModelPart rff2;
    private final ModelPart rff3;
    private final ModelPart rff1;
    private final ModelPart tf4;
    private final ModelPart tf1;
    private final ModelPart Shape1;
    private final ModelPart neck;
    private final ModelPart head1;
    private final ModelPart lf1;
    private final ModelPart lf2;
    private final ModelPart head2;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart bl1;
    private final ModelPart br1;
    private final ModelPart bl2;
    private final ModelPart br2;
    private final ModelPart bl3;
    private final ModelPart br3;
    private final ModelPart rf1;
    private final ModelPart rf2;
    private final ModelPart tf2;
    private final ModelPart tf3;
    private final ModelPart bl4;
    private final ModelPart br4;
    private final ModelPart Hat1;
    private final ModelPart Hat2;

    /** Every part {@link #setupAnim} writes, reset first (R8). */
    private final ModelPart[] animated;

    /** {@code c.get_is_activated()} read in {@code render()} (:308-313), carried from {@link #setupAnim}. */
    private int activated = 0;

    /** {@code ModelVelocityRaptor(float f1)} (:45-220). */
    public VelocityRaptorModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.hf3 = root.getChild(VelocityRaptorGeometry.HF3);
        this.hf4 = root.getChild(VelocityRaptorGeometry.HF4);
        this.hf2 = root.getChild(VelocityRaptorGeometry.HF2);
        this.hf1 = root.getChild(VelocityRaptorGeometry.HF1);
        this.lff2 = root.getChild(VelocityRaptorGeometry.LFF2);
        this.lff1 = root.getChild(VelocityRaptorGeometry.LFF1);
        this.lff3 = root.getChild(VelocityRaptorGeometry.LFF3);
        this.rff2 = root.getChild(VelocityRaptorGeometry.RFF2);
        this.rff3 = root.getChild(VelocityRaptorGeometry.RFF3);
        this.rff1 = root.getChild(VelocityRaptorGeometry.RFF1);
        this.tf4 = root.getChild(VelocityRaptorGeometry.TF4);
        this.tf1 = root.getChild(VelocityRaptorGeometry.TF1);
        this.Shape1 = root.getChild(VelocityRaptorGeometry.SHAPE1);
        this.neck = root.getChild(VelocityRaptorGeometry.NECK);
        this.head1 = root.getChild(VelocityRaptorGeometry.HEAD1);
        this.lf1 = root.getChild(VelocityRaptorGeometry.LF1);
        this.lf2 = root.getChild(VelocityRaptorGeometry.LF2);
        this.head2 = root.getChild(VelocityRaptorGeometry.HEAD2);
        this.tail1 = root.getChild(VelocityRaptorGeometry.TAIL1);
        this.tail2 = root.getChild(VelocityRaptorGeometry.TAIL2);
        this.bl1 = root.getChild(VelocityRaptorGeometry.BL1);
        this.br1 = root.getChild(VelocityRaptorGeometry.BR1);
        this.bl2 = root.getChild(VelocityRaptorGeometry.BL2);
        this.br2 = root.getChild(VelocityRaptorGeometry.BR2);
        this.bl3 = root.getChild(VelocityRaptorGeometry.BL3);
        this.br3 = root.getChild(VelocityRaptorGeometry.BR3);
        this.rf1 = root.getChild(VelocityRaptorGeometry.RF1);
        this.rf2 = root.getChild(VelocityRaptorGeometry.RF2);
        this.tf2 = root.getChild(VelocityRaptorGeometry.TF2);
        this.tf3 = root.getChild(VelocityRaptorGeometry.TF3);
        this.bl4 = root.getChild(VelocityRaptorGeometry.BL4);
        this.br4 = root.getChild(VelocityRaptorGeometry.BR4);
        this.Hat1 = root.getChild(VelocityRaptorGeometry.HAT1);
        this.Hat2 = root.getChild(VelocityRaptorGeometry.HAT2);
        this.animated = new ModelPart[] {
            this.bl1, this.bl2, this.bl3, this.bl4, this.br1, this.br2, this.br3, this.br4,
            this.hf1, this.hf2, this.hf3, this.hf4,
            this.lf1, this.lf2, this.lff1, this.lff2, this.lff3,
            this.rf1, this.rf2, this.rff1, this.rff2, this.rff3,
            this.tf1, this.tf2, this.tf3, this.tf4
        };
    }

    /** The writes of {@code render()} (:228-275), in the original order. */
    @Override
    public void setupAnim(final VelocityRaptor c, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.animated) {
            part.resetPose();
        }
        float hf = 0.0f;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.bl1.xRot = newangle;
        this.bl2.xRot = newangle + 0.488f;
        this.bl3.xRot = newangle;
        this.bl4.xRot = newangle + 0.628f;
        this.br1.xRot = -newangle;
        this.br2.xRot = -newangle + 0.488f;
        this.br3.xRot = -newangle;
        this.br4.xRot = -newangle + 0.628f;
        hf = c.getVHealth() / c.getMaxHealth();
        newangle = Mth.cos(f2 * 1.25f * this.wingspeed * hf) * 3.1415927f * 0.1f * hf;
        this.hf1.yRot = newangle;
        this.hf2.yRot = -newangle;
        this.hf3.yRot = newangle;
        this.hf4.yRot = -newangle;
        newangle = Mth.cos(f2 * 0.3f) * 3.1415927f * 0.05f;
        this.lf1.xRot = newangle + 0.279f;
        this.lf2.xRot = newangle - 0.436f;
        this.lff1.xRot = newangle - 0.279f;
        this.lff2.xRot = newangle - 0.453f;
        this.lff3.xRot = newangle - 1.047f;
        this.rf1.xRot = -newangle + 0.279f;
        this.rf2.xRot = -newangle - 0.436f;
        this.rff1.xRot = -newangle - 0.279f;
        this.rff2.xRot = -newangle - 0.453f;
        this.rff3.xRot = -newangle - 1.047f;
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f;
        this.lff1.yRot = newangle;
        this.lff2.yRot = -newangle;
        this.lff3.yRot = newangle;
        this.rff1.yRot = -newangle;
        this.rff2.yRot = newangle;
        this.rff3.yRot = -newangle;
        if (c.isSitting()) {
            newangle = 0.0f;
        } else {
            newangle = Mth.cos(f2 * 1.4f * this.wingspeed * hf) * 3.1415927f * 0.25f * hf;
        }
        this.tf1.zRot = newangle;
        this.tf2.zRot = -newangle;
        this.tf3.zRot = newangle;
        this.tf4.zRot = -newangle;
        this.activated = c.get_is_activated();
    }

    /** Draw order of {@code render()} (:276-313); the hats by activation. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.hf3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.hf4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.hf2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.hf1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tf1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tf2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tf3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tf4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lf1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lf2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lff2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lff1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lff3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rf1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rf2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rff2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rff3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rff1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.bl1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.bl2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.bl3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.bl4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.br1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.br2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.br3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.br4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        // c instanceof EntityCannonFodder is always true here.
        if (this.activated != 0) {
            this.Hat1.render(poseStack, buffer, packedLight, packedOverlay, color);
            if (this.activated > 1) {
                this.Hat2.render(poseStack, buffer, packedLight, packedOverlay, color);
            }
        }
    }
}
