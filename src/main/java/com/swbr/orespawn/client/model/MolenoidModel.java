package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.MolenoidGeometry;
import com.swbr.orespawn.entity.monster.Molenoid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelMolenoid} (ModelMolenoid.java:7-379): 37 boxes, 256x256 texture, geometry
 * {@link MolenoidGeometry}. {@code render()} (:240-368): arms dig (fast while attacking, else with the walk), legs
 * paddle with the walk, the six nose rays turn slowly. No GL calls.
 */
public class MolenoidModel extends EntityModel<Molenoid> {

    /** Register with {@code MolenoidGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "molenoid"), "main");

    /** {@code ModelMolenoid(float f1)}: {@code wingspeed = f1} (:49-50); ClientProxyOreSpawn passes 0.5 (manifest). */
    private final float wingspeed;

    private final ModelPart body;
    private final ModelPart shoulders;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart nosestar1;
    private final ModelPart nosestar2;
    private final ModelPart nosestar3;
    private final ModelPart larm;
    private final ModelPart lhand;
    private final ModelPart lclaw1;
    private final ModelPart lclaw2;
    private final ModelPart lclaw3;
    private final ModelPart lclaw4;
    private final ModelPart nosestar4;
    private final ModelPart nosestar5;
    private final ModelPart nosestar6;
    private final ModelPart butt;
    private final ModelPart tail;
    private final ModelPart lleg;
    private final ModelPart lfoot;
    private final ModelPart ltoe1;
    private final ModelPart ltoe2;
    private final ModelPart ltoe3;
    private final ModelPart ltoe4;
    private final ModelPart rarm;
    private final ModelPart rhand;
    private final ModelPart rclaw1;
    private final ModelPart rclaw2;
    private final ModelPart rclaw3;
    private final ModelPart rclaw4;
    private final ModelPart rleg;
    private final ModelPart rfoot;
    private final ModelPart rtoe1;
    private final ModelPart rtoe2;
    private final ModelPart rtoe3;
    private final ModelPart rtoe4;
    /** The draw order of {@code render()} (:331-367). */
    private final ModelPart[] drawOrder;

    public MolenoidModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.body = root.getChild(MolenoidGeometry.BODY);
        this.shoulders = root.getChild(MolenoidGeometry.SHOULDERS);
        this.head1 = root.getChild(MolenoidGeometry.HEAD1);
        this.head2 = root.getChild(MolenoidGeometry.HEAD2);
        this.head3 = root.getChild(MolenoidGeometry.HEAD3);
        this.nosestar1 = root.getChild(MolenoidGeometry.NOSESTAR1);
        this.nosestar2 = root.getChild(MolenoidGeometry.NOSESTAR2);
        this.nosestar3 = root.getChild(MolenoidGeometry.NOSESTAR3);
        this.larm = root.getChild(MolenoidGeometry.LARM);
        this.lhand = root.getChild(MolenoidGeometry.LHAND);
        this.lclaw1 = root.getChild(MolenoidGeometry.LCLAW1);
        this.lclaw2 = root.getChild(MolenoidGeometry.LCLAW2);
        this.lclaw3 = root.getChild(MolenoidGeometry.LCLAW3);
        this.lclaw4 = root.getChild(MolenoidGeometry.LCLAW4);
        this.nosestar4 = root.getChild(MolenoidGeometry.NOSESTAR4);
        this.nosestar5 = root.getChild(MolenoidGeometry.NOSESTAR5);
        this.nosestar6 = root.getChild(MolenoidGeometry.NOSESTAR6);
        this.butt = root.getChild(MolenoidGeometry.BUTT);
        this.tail = root.getChild(MolenoidGeometry.TAIL);
        this.lleg = root.getChild(MolenoidGeometry.LLEG);
        this.lfoot = root.getChild(MolenoidGeometry.LFOOT);
        this.ltoe1 = root.getChild(MolenoidGeometry.LTOE1);
        this.ltoe2 = root.getChild(MolenoidGeometry.LTOE2);
        this.ltoe3 = root.getChild(MolenoidGeometry.LTOE3);
        this.ltoe4 = root.getChild(MolenoidGeometry.LTOE4);
        this.rarm = root.getChild(MolenoidGeometry.RARM);
        this.rhand = root.getChild(MolenoidGeometry.RHAND);
        this.rclaw1 = root.getChild(MolenoidGeometry.RCLAW1);
        this.rclaw2 = root.getChild(MolenoidGeometry.RCLAW2);
        this.rclaw3 = root.getChild(MolenoidGeometry.RCLAW3);
        this.rclaw4 = root.getChild(MolenoidGeometry.RCLAW4);
        this.rleg = root.getChild(MolenoidGeometry.RLEG);
        this.rfoot = root.getChild(MolenoidGeometry.RFOOT);
        this.rtoe1 = root.getChild(MolenoidGeometry.RTOE1);
        this.rtoe2 = root.getChild(MolenoidGeometry.RTOE2);
        this.rtoe3 = root.getChild(MolenoidGeometry.RTOE3);
        this.rtoe4 = root.getChild(MolenoidGeometry.RTOE4);
        this.drawOrder = new ModelPart[] {
                this.body, this.shoulders, this.head1, this.head2, this.head3, this.nosestar1, this.nosestar2, this.nosestar3,
                this.larm, this.lhand, this.lclaw1, this.lclaw2, this.lclaw3, this.lclaw4, this.nosestar4, this.nosestar5,
                this.nosestar6, this.butt, this.tail, this.lleg, this.lfoot, this.ltoe1, this.ltoe2, this.ltoe3, this.ltoe4,
                this.rarm, this.rhand, this.rclaw1, this.rclaw2, this.rclaw3, this.rclaw4, this.rleg, this.rfoot, this.rtoe1,
                this.rtoe2, this.rtoe3, this.rtoe4 };
    }

    /** The writes of {@code render()} (:244-330). */
    @Override
    public void setupAnim(final Molenoid e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.drawOrder) {
            part.resetPose();
        }
        float newangle = 0.0f;
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.25f;
        } else if (f1 > 0.1f) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.larm.yRot = newangle + 0.628f;
        this.lhand.z = this.larm.z - (float) Math.sin(this.larm.yRot) * 15.0f;
        this.lhand.x = this.larm.x + (float) Math.cos(this.larm.yRot) * 15.0f;
        this.lhand.yRot = newangle * 1.25f;
        this.lclaw1.z = this.lhand.z - (float) Math.sin(this.lhand.yRot) * 10.0f;
        this.lclaw1.x = this.lhand.x + (float) Math.cos(this.lhand.yRot) * 10.0f;
        this.lclaw1.yRot = newangle * 1.5f - 0.174f;
        this.lclaw2.z = this.lclaw1.z;
        this.lclaw2.x = this.lclaw1.x;
        this.lclaw2.yRot = this.lclaw1.yRot;
        this.lclaw3.z = this.lclaw1.z;
        this.lclaw3.x = this.lclaw1.x;
        this.lclaw3.yRot = this.lclaw1.yRot;
        this.lclaw4.z = this.lclaw1.z;
        this.lclaw4.x = this.lclaw1.x;
        this.lclaw4.yRot = this.lclaw1.yRot;
        this.rarm.yRot = newangle - 0.628f;
        this.rhand.z = this.rarm.z + (float) Math.sin(this.rarm.yRot) * 15.0f;
        this.rhand.x = this.rarm.x - (float) Math.cos(this.rarm.yRot) * 15.0f;
        this.rhand.yRot = newangle * 1.25f;
        this.rclaw1.z = this.rhand.z + (float) Math.sin(this.rhand.yRot) * 10.0f;
        this.rclaw1.x = this.rhand.x - (float) Math.cos(this.rhand.yRot) * 10.0f;
        this.rclaw1.yRot = newangle * 1.5f + 0.174f;
        this.rclaw2.z = this.rclaw1.z;
        this.rclaw2.x = this.rclaw1.x;
        this.rclaw2.yRot = this.rclaw1.yRot;
        this.rclaw3.z = this.rclaw1.z;
        this.rclaw3.x = this.rclaw1.x;
        this.rclaw3.yRot = this.rclaw1.yRot;
        this.rclaw4.z = this.rclaw1.z;
        this.rclaw4.x = this.rclaw1.x;
        this.rclaw4.yRot = this.rclaw1.yRot;
        if (f1 > 0.1f) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.lleg.yRot = -newangle + 0.628f;
        this.lfoot.z = this.lleg.z - (float) Math.sin(this.lleg.yRot) * 15.0f;
        this.lfoot.x = this.lleg.x + (float) Math.cos(this.lleg.yRot) * 15.0f;
        this.lfoot.yRot = -newangle * 1.25f;
        this.ltoe1.z = this.lfoot.z - (float) Math.sin(this.lfoot.yRot) * 10.0f;
        this.ltoe1.x = this.lfoot.x + (float) Math.cos(this.lfoot.yRot) * 10.0f;
        this.ltoe1.yRot = -newangle * 1.5f - 0.261f;
        this.ltoe2.z = this.ltoe1.z;
        this.ltoe2.x = this.ltoe1.x;
        this.ltoe2.yRot = this.ltoe1.yRot;
        this.ltoe3.z = this.ltoe1.z;
        this.ltoe3.x = this.ltoe1.x;
        this.ltoe3.yRot = this.ltoe1.yRot;
        this.ltoe4.z = this.ltoe1.z;
        this.ltoe4.x = this.ltoe1.x;
        this.ltoe4.yRot = this.ltoe1.yRot;
        this.rleg.yRot = -newangle - 0.628f;
        this.rfoot.z = this.rleg.z + (float) Math.sin(this.rleg.yRot) * 15.0f;
        this.rfoot.x = this.rleg.x - (float) Math.cos(this.rleg.yRot) * 15.0f;
        this.rfoot.yRot = -newangle * 1.25f;
        this.rtoe1.z = this.rfoot.z + (float) Math.sin(this.rfoot.yRot) * 10.0f;
        this.rtoe1.x = this.rfoot.x - (float) Math.cos(this.rfoot.yRot) * 10.0f;
        this.rtoe1.yRot = -newangle * 1.5f + 0.261f;
        this.rtoe2.z = this.rtoe1.z;
        this.rtoe2.x = this.rtoe1.x;
        this.rtoe2.yRot = this.rtoe1.yRot;
        this.rtoe3.z = this.rtoe1.z;
        this.rtoe3.x = this.rtoe1.x;
        this.rtoe3.yRot = this.rtoe1.yRot;
        this.rtoe4.z = this.rtoe1.z;
        this.rtoe4.x = this.rtoe1.x;
        this.rtoe4.yRot = this.rtoe1.yRot;
        newangle = Mth.cos(f2 * 0.1f * this.wingspeed) * 3.1415927f;
        this.nosestar1.zRot = newangle;
        this.nosestar2.zRot = newangle + 0.523f;
        this.nosestar3.zRot = newangle + 1.047f;
        this.nosestar4.zRot = newangle + 1.57f;
        this.nosestar5.zRot = newangle - 1.047f;
        this.nosestar6.zRot = newangle - 0.523f;
    }

    /** The draw calls of {@code render()} (:331-367). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.drawOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
