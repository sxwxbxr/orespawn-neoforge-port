package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BeeGeometry;
import com.swbr.orespawn.entity.arthropod.Bee;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelBee} (ModelBee.java:7-245): 23 boxes, 256x256 texture, geometry from the
 * generated {@link BeeGeometry}. The animation is {@code render()} (:156-210): beating wings, pincers, feelers, and an
 * abdomen chain that curls faster while the bee attacks (DataWatcher 20).
 */
public class BeeModel extends EntityModel<Bee> {

    /** Register with {@code BeeGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "bee"), "main");

    /** {@code ModelBee(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 2.0 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:211-233), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart sting;
    private final ModelPart abdomnem1;
    private final ModelPart abdomnem2;
    private final ModelPart abdomnem3;
    private final ModelPart abdomnem4;
    private final ModelPart abdomnem5;
    private final ModelPart wingRight;
    private final ModelPart wingLeft;
    private final ModelPart ra1;
    private final ModelPart la1;
    private final ModelPart la2;
    private final ModelPart ra2;
    private final ModelPart ra3;
    private final ModelPart la3;
    private final ModelPart leftPom;
    private final ModelPart rightPom;
    private final ModelPart leftPincerExtra;
    private final ModelPart leftPincerMain;
    private final ModelPart rightPincerMain;
    private final ModelPart rightPincerExtra;

    public BeeModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[BeeGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(BeeGeometry.PARTS[i]);
        }
        this.sting = root.getChild(BeeGeometry.STING);
        this.abdomnem1 = root.getChild(BeeGeometry.ABDOMNEM1);
        this.abdomnem2 = root.getChild(BeeGeometry.ABDOMNEM2);
        this.abdomnem3 = root.getChild(BeeGeometry.ABDOMNEM3);
        this.abdomnem4 = root.getChild(BeeGeometry.ABDOMNEM4);
        this.abdomnem5 = root.getChild(BeeGeometry.ABDOMNEM5);
        this.wingRight = root.getChild(BeeGeometry.WING_RIGHT);
        this.wingLeft = root.getChild(BeeGeometry.WING_LEFT);
        this.ra1 = root.getChild(BeeGeometry.RA1);
        this.la1 = root.getChild(BeeGeometry.LA1);
        this.la2 = root.getChild(BeeGeometry.LA2);
        this.ra2 = root.getChild(BeeGeometry.RA2);
        this.ra3 = root.getChild(BeeGeometry.RA3);
        this.la3 = root.getChild(BeeGeometry.LA3);
        this.leftPom = root.getChild(BeeGeometry.LEFT_POM);
        this.rightPom = root.getChild(BeeGeometry.RIGHT_POM);
        this.leftPincerExtra = root.getChild(BeeGeometry.LEFT_PINCER_EXTRA);
        this.leftPincerMain = root.getChild(BeeGeometry.LEFT_PINCER_MAIN);
        this.rightPincerMain = root.getChild(BeeGeometry.RIGHT_PINCER_MAIN);
        this.rightPincerExtra = root.getChild(BeeGeometry.RIGHT_PINCER_EXTRA);
    }

    /** The writes of {@code render()} (:157-210). */
    @Override
    public void setupAnim(final Bee b, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.3f;
        this.wingLeft.zRot = -1.745f - newangle;
        this.wingRight.zRot = 1.754f + newangle;
        newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.1f;
        this.leftPincerMain.yRot = -0.274f + newangle;
        this.leftPincerExtra.yRot = -0.274f + newangle;
        this.rightPincerMain.yRot = 0.274f - newangle;
        this.rightPincerExtra.yRot = 0.274f - newangle;
        newangle = Mth.cos(f2 * 0.21f * this.wingspeed) * 3.1415927f * 0.06f;
        this.la1.xRot = 0.261f + newangle;
        this.la2.xRot = 0.436f + newangle;
        this.la3.xRot = 0.611f + newangle;
        this.leftPom.xRot = newangle;
        newangle = Mth.cos(f2 * 0.27f * this.wingspeed) * 3.1415927f * 0.06f;
        this.ra1.xRot = 0.261f + newangle;
        this.ra2.xRot = 0.436f + newangle;
        this.ra3.xRot = 0.611f + newangle;
        this.rightPom.xRot = newangle;
        newangle = Mth.cos(f2 * 0.31f * this.wingspeed) * 3.1415927f * 0.06f;
        this.la1.zRot = newangle;
        this.la2.zRot = newangle;
        this.la3.zRot = newangle;
        this.leftPom.zRot = newangle;
        newangle = Mth.cos(f2 * 0.37f * this.wingspeed) * 3.1415927f * 0.06f;
        this.ra1.zRot = newangle;
        this.ra2.zRot = newangle;
        this.ra3.zRot = newangle;
        this.rightPom.zRot = newangle;
        if (b.getAttacking() == 0) {
            newangle = Mth.cos(f2 * 0.021f * this.wingspeed) * 3.1415927f * 0.023f;
        } else {
            newangle = Mth.cos(f2 * 0.11f * this.wingspeed) * 3.1415927f * 0.055f;
        }
        this.abdomnem5.xRot = 1.099f + newangle;
        this.abdomnem4.xRot = this.abdomnem5.xRot + newangle - 0.35f;
        this.abdomnem4.y = (float) (this.abdomnem5.y + Math.cos(this.abdomnem5.xRot) * 10.0);
        this.abdomnem4.z = (float) (this.abdomnem5.z + Math.sin(this.abdomnem5.xRot) * 10.0);
        this.abdomnem3.xRot = this.abdomnem4.xRot + newangle - 0.35f;
        this.abdomnem3.y = (float) (this.abdomnem4.y + Math.cos(this.abdomnem4.xRot) * 10.0);
        this.abdomnem3.z = (float) (this.abdomnem4.z + Math.sin(this.abdomnem4.xRot) * 10.0);
        this.abdomnem2.xRot = this.abdomnem3.xRot + newangle - 0.35f;
        this.abdomnem2.y = (float) (this.abdomnem3.y + Math.cos(this.abdomnem3.xRot) * 6.0);
        this.abdomnem2.z = (float) (this.abdomnem3.z + Math.sin(this.abdomnem3.xRot) * 6.0);
        this.abdomnem1.xRot = this.abdomnem2.xRot + newangle - 0.35f;
        this.abdomnem1.y = (float) (this.abdomnem2.y + Math.cos(this.abdomnem2.xRot) * 5.0);
        this.abdomnem1.z = (float) (this.abdomnem2.z + Math.sin(this.abdomnem2.xRot) * 5.0);
        this.sting.xRot = this.abdomnem1.xRot + newangle - 0.35f;
        this.sting.y = (float) (this.abdomnem1.y + Math.cos(this.abdomnem1.xRot) * 7.0);
        this.sting.z = 1.0f + (float) (this.abdomnem1.z + Math.sin(this.abdomnem1.xRot) * 7.0);
    }

    /** {@code render()} (:211-233). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
