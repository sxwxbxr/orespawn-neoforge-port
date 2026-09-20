package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.HydroliscGeometry;
import com.swbr.orespawn.entity.herbivore.Hydrolisc;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelHydrolisc} (ModelHydrolisc.java:6-362): three body boxes, three head boxes,
 * three head feathers, four legs of six segments each, a three-link tail and four spines, 64x128 texture.
 * Geometry from the generated {@link HydroliscGeometry}; the animation is {@code render()} (:258-351):
 * <ul>
 *   <li>the 24 leg segments on {@code rotateAngleX} with fixed knee angles, gated on {@code f1 > 0.1};</li>
 *   <li>the tail wags always, still while sitting, pivots chained;</li>
 *   <li>the head feathers flutter with frequency and amplitude scaled by health over max health.</li>
 * </ul>
 * Reads {@link Hydrolisc#getHydroHealth()}, {@code getMaxHealth()} (synced health) and {@link Hydrolisc#isSitting()}.
 */
public class HydroliscModel extends EntityModel<Hydrolisc> {

    /** Register with {@code HydroliscGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "hydrolisc"), "main");

    /** {@code ModelHydrolisc(float f1)}: {@code wingspeed = f1} (:52-53); ClientProxyOreSpawn passes 0.65 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:311-350), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart lf1;
    private final ModelPart lf2;
    private final ModelPart lf3;
    private final ModelPart lf4;
    private final ModelPart lf5;
    private final ModelPart lf6;
    private final ModelPart rf1;
    private final ModelPart rf2;
    private final ModelPart rf3;
    private final ModelPart rf4;
    private final ModelPart rf5;
    private final ModelPart rf6;
    private final ModelPart lb1;
    private final ModelPart lb2;
    private final ModelPart lb3;
    private final ModelPart lb4;
    private final ModelPart lb5;
    private final ModelPart lb6;
    private final ModelPart rb1;
    private final ModelPart rb2;
    private final ModelPart rb3;
    private final ModelPart rb4;
    private final ModelPart rb5;
    private final ModelPart rb6;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart feather1;
    private final ModelPart feather2;
    private final ModelPart feather3;

    public HydroliscModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[HydroliscGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(HydroliscGeometry.PARTS[i]);
        }
        this.lf1 = root.getChild(HydroliscGeometry.LF1);
        this.lf2 = root.getChild(HydroliscGeometry.LF2);
        this.lf3 = root.getChild(HydroliscGeometry.LF3);
        this.lf4 = root.getChild(HydroliscGeometry.LF4);
        this.lf5 = root.getChild(HydroliscGeometry.LF5);
        this.lf6 = root.getChild(HydroliscGeometry.LF6);
        this.rf1 = root.getChild(HydroliscGeometry.RF1);
        this.rf2 = root.getChild(HydroliscGeometry.RF2);
        this.rf3 = root.getChild(HydroliscGeometry.RF3);
        this.rf4 = root.getChild(HydroliscGeometry.RF4);
        this.rf5 = root.getChild(HydroliscGeometry.RF5);
        this.rf6 = root.getChild(HydroliscGeometry.RF6);
        this.lb1 = root.getChild(HydroliscGeometry.LB1);
        this.lb2 = root.getChild(HydroliscGeometry.LB2);
        this.lb3 = root.getChild(HydroliscGeometry.LB3);
        this.lb4 = root.getChild(HydroliscGeometry.LB4);
        this.lb5 = root.getChild(HydroliscGeometry.LB5);
        this.lb6 = root.getChild(HydroliscGeometry.LB6);
        this.rb1 = root.getChild(HydroliscGeometry.RB1);
        this.rb2 = root.getChild(HydroliscGeometry.RB2);
        this.rb3 = root.getChild(HydroliscGeometry.RB3);
        this.rb4 = root.getChild(HydroliscGeometry.RB4);
        this.rb5 = root.getChild(HydroliscGeometry.RB5);
        this.rb6 = root.getChild(HydroliscGeometry.RB6);
        this.tail1 = root.getChild(HydroliscGeometry.TAIL1);
        this.tail2 = root.getChild(HydroliscGeometry.TAIL2);
        this.tail3 = root.getChild(HydroliscGeometry.TAIL3);
        this.feather1 = root.getChild(HydroliscGeometry.FEATHER1);
        this.feather2 = root.getChild(HydroliscGeometry.FEATHER2);
        this.feather3 = root.getChild(HydroliscGeometry.FEATHER3);
    }

    /** The angle and pivot writes of {@code render()} (:262-310). */
    @Override
    public void setupAnim(final Hydrolisc entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final Hydrolisc c = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float hf = 0.0f;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lf1.xRot = newangle;
        this.lf2.xRot = newangle - 0.488f;
        this.lf3.xRot = newangle - 2.347f;
        this.lf4.xRot = newangle - 0.628f;
        this.lf5.xRot = newangle - 0.628f;
        this.lf6.xRot = newangle + 0.174f;
        this.rf1.xRot = -newangle;
        this.rf2.xRot = -newangle - 0.488f;
        this.rf3.xRot = -newangle - 2.347f;
        this.rf4.xRot = -newangle - 0.628f;
        this.rf5.xRot = -newangle - 0.628f;
        this.rf6.xRot = -newangle + 0.174f;
        this.lb1.xRot = -newangle;
        this.lb2.xRot = -newangle - 0.488f;
        this.lb3.xRot = -newangle - 2.347f;
        this.lb4.xRot = -newangle - 0.628f;
        this.lb5.xRot = -newangle - 0.628f;
        this.lb6.xRot = -newangle + 0.174f;
        this.rb1.xRot = newangle;
        this.rb2.xRot = newangle - 0.488f;
        this.rb3.xRot = newangle - 2.347f;
        this.rb4.xRot = newangle - 0.628f;
        this.rb5.xRot = newangle - 0.628f;
        this.rb6.xRot = newangle + 0.174f;
        newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        if (c.isSitting()) {
            newangle = 0.0f;
        }
        this.tail1.yRot = newangle * 0.25f;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 5.0f;
        this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 5.0f;
        this.tail2.yRot = newangle * 0.5f;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 8.0f;
        this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 8.0f;
        this.tail3.yRot = newangle * 0.75f;
        hf = c.getHydroHealth() / c.getMaxHealth();
        newangle = Mth.cos(f2 * 1.25f * this.wingspeed * hf) * 3.1415927f * 0.2f * hf;
        this.feather2.yRot = newangle;
        newangle = Mth.cos(f2 * 0.75f * this.wingspeed * hf) * 3.1415927f * 0.2f * hf;
        this.feather1.yRot = newangle - 0.9f;
        this.feather3.yRot = -newangle + 0.9f;
    }

    /** {@code render()} (:311-350). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
