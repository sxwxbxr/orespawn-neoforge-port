package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.Robot4Geometry;
import com.swbr.orespawn.entity.robot.Robot4;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelRobot4} (ModelRobot4.java:7-520): the Robo-Warrior - digitigrade legs with
 * knee guards, a spined back, a shield on the right arm and a cannon on the left. Geometry (:67-353) is the
 * generated {@link Robot4Geometry}.
 *
 * <p>While {@code attacking} the shield arm swings and the cannon arm raises; the cannon parts are re-pivoted on
 * the end of the upper arm every frame (research 06, "hierarchy"). {@code wingspeed} 1.0 (manifest).
 * {@code resetPose()} at the start (R8).
 *
 * <p>The shield flag: the original model wrote DataWatcher 21 from the arm angle (:393-398). That is kept - it
 * sets only the client copy, which nothing on the server reads, so the shield never blocks a hit, exactly as in
 * 1.7.10 (R18, "Robot4-Schild").
 */
public class Robot4Model extends EntityModel<Robot4> {

    /** Register with {@code Robot4Geometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "robot4"), "main");

    private final ModelPart root;
    private final float wingspeed;
    /** Every part in the order {@code render()} drew them (:453-508), which is the constructor order. */
    private final ModelPart[] parts;
    private final ModelPart leftfootfront;
    private final ModelPart leftfootbase;
    private final ModelPart leftfootback;
    private final ModelPart leftfoottip;
    private final ModelPart leftshin;
    private final ModelPart leftcalf;
    private final ModelPart leftkneegaurd;
    private final ModelPart leftthigh;
    private final ModelPart rightfootfront;
    private final ModelPart rightfoottip;
    private final ModelPart rightfootbase;
    private final ModelPart rightfootback;
    private final ModelPart rightshin;
    private final ModelPart rightcalf;
    private final ModelPart rightkneegaurd;
    private final ModelPart rightthigh;
    private final ModelPart head;
    private final ModelPart rightsholder;
    private final ModelPart leftsholder;
    private final ModelPart rightsholdergaurd;
    private final ModelPart sheildbase;
    private final ModelPart sheildtip;
    private final ModelPart rightupperarm;
    private final ModelPart rightlowerarm;
    private final ModelPart sheildend;
    private final ModelPart leftupperarm;
    private final ModelPart sholdergaurdtip;
    private final ModelPart cannonbase;
    private final ModelPart cannonend;
    private final ModelPart leftcannonpiece;
    private final ModelPart topcannonpiece;
    private final ModelPart rightcannonpiece;
    private final ModelPart bottomcannonpiece;
    private final ModelPart glowycannonbit1;
    private final ModelPart glowycannonbit2;
    private final ModelPart glowycannonbit3;
    private final ModelPart glowycannonbit4;
    private final ModelPart glowycannonbit5;
    private final ModelPart cannonammo;

    /** {@code ModelRobot4(float f1)} (:67-353). */
    public Robot4Model(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        this.wingspeed = f1;
        this.parts = new ModelPart[Robot4Geometry.PARTS.length];
        for (int i = 0; i < Robot4Geometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(Robot4Geometry.PARTS[i]);
        }
        this.leftfootfront = root.getChild(Robot4Geometry.LEFTFOOTFRONT);
        this.leftfootbase = root.getChild(Robot4Geometry.LEFTFOOTBASE);
        this.leftfootback = root.getChild(Robot4Geometry.LEFTFOOTBACK);
        this.leftfoottip = root.getChild(Robot4Geometry.LEFTFOOTTIP);
        this.leftshin = root.getChild(Robot4Geometry.LEFTSHIN);
        this.leftcalf = root.getChild(Robot4Geometry.LEFTCALF);
        this.leftkneegaurd = root.getChild(Robot4Geometry.LEFTKNEEGAURD);
        this.leftthigh = root.getChild(Robot4Geometry.LEFTTHIGH);
        this.rightfootfront = root.getChild(Robot4Geometry.RIGHTFOOTFRONT);
        this.rightfoottip = root.getChild(Robot4Geometry.RIGHTFOOTTIP);
        this.rightfootbase = root.getChild(Robot4Geometry.RIGHTFOOTBASE);
        this.rightfootback = root.getChild(Robot4Geometry.RIGHTFOOTBACK);
        this.rightshin = root.getChild(Robot4Geometry.RIGHTSHIN);
        this.rightcalf = root.getChild(Robot4Geometry.RIGHTCALF);
        this.rightkneegaurd = root.getChild(Robot4Geometry.RIGHTKNEEGAURD);
        this.rightthigh = root.getChild(Robot4Geometry.RIGHTTHIGH);
        this.head = root.getChild(Robot4Geometry.HEAD);
        this.rightsholder = root.getChild(Robot4Geometry.RIGHTSHOLDER);
        this.leftsholder = root.getChild(Robot4Geometry.LEFTSHOLDER);
        this.rightsholdergaurd = root.getChild(Robot4Geometry.RIGHTSHOLDERGAURD);
        this.sheildbase = root.getChild(Robot4Geometry.SHEILDBASE);
        this.sheildtip = root.getChild(Robot4Geometry.SHEILDTIP);
        this.rightupperarm = root.getChild(Robot4Geometry.RIGHTUPPERARM);
        this.rightlowerarm = root.getChild(Robot4Geometry.RIGHTLOWERARM);
        this.sheildend = root.getChild(Robot4Geometry.SHEILDEND);
        this.leftupperarm = root.getChild(Robot4Geometry.LEFTUPPERARM);
        this.sholdergaurdtip = root.getChild(Robot4Geometry.SHOLDERGAURDTIP);
        this.cannonbase = root.getChild(Robot4Geometry.CANNONBASE);
        this.cannonend = root.getChild(Robot4Geometry.CANNONEND);
        this.leftcannonpiece = root.getChild(Robot4Geometry.LEFTCANNONPIECE);
        this.topcannonpiece = root.getChild(Robot4Geometry.TOPCANNONPIECE);
        this.rightcannonpiece = root.getChild(Robot4Geometry.RIGHTCANNONPIECE);
        this.bottomcannonpiece = root.getChild(Robot4Geometry.BOTTOMCANNONPIECE);
        this.glowycannonbit1 = root.getChild(Robot4Geometry.GLOWYCANNONBIT1);
        this.glowycannonbit2 = root.getChild(Robot4Geometry.GLOWYCANNONBIT2);
        this.glowycannonbit3 = root.getChild(Robot4Geometry.GLOWYCANNONBIT3);
        this.glowycannonbit4 = root.getChild(Robot4Geometry.GLOWYCANNONBIT4);
        this.glowycannonbit5 = root.getChild(Robot4Geometry.GLOWYCANNONBIT5);
        this.cannonammo = root.getChild(Robot4Geometry.CANNONAMMO);
    }

    /**
     * The writes of {@code render()} (:355-452). {@code f1} limb swing amount, {@code f2} age in ticks, {@code f3}
     * head yaw minus body yaw in degrees.
     */
    @Override
    public void setupAnim(final Robot4 e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.15f * f1;
        } else {
            newangle = 0.0f;
        }
        this.leftfootfront.xRot = newangle;
        this.leftfootbase.xRot = newangle;
        this.leftfootback.xRot = newangle;
        this.leftfoottip.xRot = newangle;
        this.leftshin.xRot = newangle;
        this.leftcalf.xRot = newangle + 0.175f;
        this.leftkneegaurd.xRot = newangle + 0.63f;
        this.leftthigh.xRot = newangle - 0.175f;
        this.rightfootfront.xRot = -newangle;
        this.rightfoottip.xRot = -newangle;
        this.rightfootbase.xRot = -newangle;
        this.rightfootback.xRot = -newangle;
        this.rightshin.xRot = -newangle;
        this.rightcalf.xRot = -newangle + 0.175f;
        this.rightkneegaurd.xRot = -newangle + 0.63f;
        this.rightthigh.xRot = -newangle - 0.175f;
        this.head.yRot = (float) Math.toRadians(f3 / 1.5);
        final float amp = 0.7853982f;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos((float) Math.toRadians(f2 % 360.0f) * this.wingspeed * 6.0f) * amp;
            newangle = Math.abs(newangle);
            newangle += 0.75f;
        } else {
            newangle = 0.0f;
        }
        if (newangle > amp / 3.0) {
            e.setShielding(1);
        } else {
            e.setShielding(0);
        }
        this.rightsholder.xRot = -newangle;
        this.rightsholdergaurd.xRot = -newangle - 0.21f;
        this.sheildbase.xRot = -newangle + 1.047f;
        this.sheildtip.xRot = -newangle + 1.047f;
        this.rightupperarm.xRot = -newangle - 0.21f;
        this.rightlowerarm.xRot = -newangle + 1.047f;
        this.sheildend.xRot = -newangle + 1.04f;
        this.sholdergaurdtip.xRot = -newangle - 0.21f;
        if (e.getAttacking() != 0) {
            newangle = 0.85f;
        } else {
            newangle = 0.0f;
        }
        this.leftsholder.xRot = -newangle;
        this.leftupperarm.xRot = -newangle - 0.21f;
        this.cannonbase.xRot = -newangle - 0.7f;
        this.cannonend.xRot = -newangle - 0.7f;
        this.leftcannonpiece.xRot = -newangle - 0.7f;
        this.topcannonpiece.xRot = -newangle - 0.7f;
        this.rightcannonpiece.xRot = -newangle - 0.7f;
        this.bottomcannonpiece.xRot = -newangle - 0.7f;
        this.glowycannonbit1.xRot = -newangle + 0.17f;
        this.glowycannonbit2.xRot = -newangle + 0.17f;
        this.glowycannonbit3.xRot = -newangle + 0.08f;
        this.glowycannonbit4.xRot = -newangle + 0.08f;
        this.glowycannonbit5.xRot = -newangle;
        this.cannonammo.xRot = -newangle - 0.7f;
        final double newposy = (float) (this.leftsholder.y + Math.cos(this.leftupperarm.xRot) * 14.0);
        final double newposz = (float) (this.leftsholder.z + Math.sin(this.leftupperarm.xRot) * 14.0);
        this.cannonbase.y = (float) newposy;
        this.cannonbase.z = (float) newposz;
        this.cannonend.y = (float) newposy;
        this.cannonend.z = (float) newposz;
        this.leftcannonpiece.y = (float) newposy;
        this.leftcannonpiece.z = (float) newposz;
        this.topcannonpiece.y = (float) newposy;
        this.topcannonpiece.z = (float) newposz;
        this.rightcannonpiece.y = (float) newposy;
        this.rightcannonpiece.z = (float) newposz;
        this.bottomcannonpiece.y = (float) newposy;
        this.bottomcannonpiece.z = (float) newposz;
        this.glowycannonbit1.y = (float) newposy;
        this.glowycannonbit1.z = (float) newposz;
        this.glowycannonbit2.y = (float) newposy;
        this.glowycannonbit2.z = (float) newposz;
        this.glowycannonbit3.y = (float) newposy;
        this.glowycannonbit3.z = (float) newposz;
        this.glowycannonbit4.y = (float) newposy;
        this.glowycannonbit4.z = (float) newposz;
        this.glowycannonbit5.y = (float) newposy;
        this.glowycannonbit5.z = (float) newposz;
        this.cannonammo.y = (float) newposy;
        this.cannonammo.z = (float) newposz;
    }

    /** The {@code render(f5)} calls of {@code render()} (:453-508). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
