package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.StinkBugGeometry;
import com.swbr.orespawn.entity.herbivore.StinkBug;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelStinkBug} (ModelStinkBug.java:6-557): 50 boxes - body, head, jaw, two
 * feelers, six legs with feet, a raised tail with 22 knobs and ten back knobs - 64x32 texture. Geometry from the
 * generated {@link StinkBugGeometry}; the animation is {@code render()} (:418-546).
 *
 * <p>Original quirk kept (R18): the decompiled {@code render()} aliases {@code f2 = this.f3}, so the feet
 * {@code f1} and {@code f3} swing forward, {@code f2}, {@code f4} and {@code f6} backward, and {@code f5} never
 * moves (:423-436). The local names {@code ff*} are the render arguments, {@code f1..f6} the foot parts.
 */
public class StinkBugModel extends EntityModel<StinkBug> {

    /** Register with {@code StinkBugGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "stink_bug"), "main");

    /** {@code ModelStinkBug(float ff1)}: {@code wingspeed = ff1} (:62-63); ClientProxyOreSpawn passes 0.75 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:497-546), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart f1;
    private final ModelPart f2;
    private final ModelPart f3;
    private final ModelPart f4;
    private final ModelPart f6;
    private final ModelPart b9;
    private final ModelPart b10;
    private final ModelPart jaw;
    private final ModelPart h1;
    private final ModelPart h2;
    private final ModelPart tail;
    private final ModelPart[] tailKnobs;

    public StinkBugModel(final ModelPart root, final float ff1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = ff1;
        this.parts = new ModelPart[StinkBugGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(StinkBugGeometry.PARTS[i]);
        }
        this.f1 = root.getChild(StinkBugGeometry.F1);
        this.f2 = root.getChild(StinkBugGeometry.F2);
        this.f3 = root.getChild(StinkBugGeometry.F3);
        this.f4 = root.getChild(StinkBugGeometry.F4);
        this.f6 = root.getChild(StinkBugGeometry.F6);
        this.b9 = root.getChild(StinkBugGeometry.B9);
        this.b10 = root.getChild(StinkBugGeometry.B10);
        this.jaw = root.getChild(StinkBugGeometry.JAW);
        this.h1 = root.getChild(StinkBugGeometry.H1);
        this.h2 = root.getChild(StinkBugGeometry.H2);
        this.tail = root.getChild(StinkBugGeometry.TAIL);
        // t1..t22 in the order the original assigned them (:447-496); every one gets tail.rotateAngleX.
        final String[] knobs = {
                StinkBugGeometry.T5, StinkBugGeometry.T4, StinkBugGeometry.T3, StinkBugGeometry.T2, StinkBugGeometry.T1,
                StinkBugGeometry.T10, StinkBugGeometry.T9, StinkBugGeometry.T8, StinkBugGeometry.T7, StinkBugGeometry.T6,
                StinkBugGeometry.T15, StinkBugGeometry.T14, StinkBugGeometry.T13, StinkBugGeometry.T12, StinkBugGeometry.T11,
                StinkBugGeometry.T20, StinkBugGeometry.T19, StinkBugGeometry.T18, StinkBugGeometry.T17, StinkBugGeometry.T16,
                StinkBugGeometry.T22, StinkBugGeometry.T21
        };
        this.tailKnobs = new ModelPart[knobs.length];
        for (int i = 0; i < knobs.length; ++i) {
            this.tailKnobs[i] = root.getChild(knobs[i]);
        }
    }

    /** The angle writes of {@code render()} (:421-496). */
    @Override
    public void setupAnim(final StinkBug entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float ff1 = limbSwingAmount;
        final float ff2 = ageInTicks;
        float newangle = 0.0f;
        newangle = Mth.sin(ff2 * 3.1f * this.wingspeed) * 3.1415927f * 0.3f * ff1;
        // :423-429: locals f1 = this.f1, f2 = this.f3, f3 = this.f3 - this.f3 is written twice.
        this.f3.xRot = newangle;
        this.f3.xRot = newangle;
        this.f1.xRot = newangle;
        // :430-436: locals f4 = this.f2, f5 = this.f4, f6 = this.f6 - no local points at this.f5, it stays in its rest pose.
        this.f6.xRot = -newangle;
        this.f4.xRot = -newangle;
        this.f2.xRot = -newangle;
        newangle = Mth.sin(ff2 * 0.4f * this.wingspeed) * 3.1415927f * 0.2f;
        this.b9.zRot = newangle;
        this.b10.zRot = -newangle;
        newangle = Mth.sin(ff2 * 0.2f * this.wingspeed) * 3.1415927f * 0.04f;
        this.jaw.xRot = 0.18f + newangle;
        this.h1.xRot = 0.52f + Mth.sin(ff2 * 0.4f * this.wingspeed) * 3.1415927f * 0.15f;
        this.h1.yRot = -0.3f + Mth.sin(ff2 * 0.43f * this.wingspeed) * 3.1415927f * 0.15f;
        this.h2.xRot = 0.52f + Mth.sin(ff2 * 0.46f * this.wingspeed) * 3.1415927f * 0.15f;
        this.h2.yRot = 0.3f + Mth.sin(ff2 * 0.49f * this.wingspeed) * 3.1415927f * 0.15f;
        this.tail.xRot = -0.2f + Mth.sin(ff2 * 0.1f * this.wingspeed) * 3.1415927f * 0.1f;
        for (final ModelPart knob : this.tailKnobs) {
            knob.xRot = this.tail.xRot;
        }
    }

    /** {@code render()} (:497-546). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
