package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CamarasaurusGeometry;
import com.swbr.orespawn.entity.herbivore.Camarasaurus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCamarasaurus} (ModelCamarasaurus.java:6-224): four body boxes, a four-link
 * tail, a three-link neck with two head boxes and eight leg boxes, 256x256 texture. Geometry from the generated
 * {@link CamarasaurusGeometry}; the animation is {@code render()} (:150-213):
 * <ul>
 *   <li>legs, gated on {@code f1 > 0.1};</li>
 *   <li>the tail wags with frequency <em>and</em> amplitude scaled by health over max health, still while sitting,
 *       each link's pivot hung onto the previous one;</li>
 *   <li>the neck and head follow the head yaw {@code f3} with growing factors, pivots chained the same way.</li>
 * </ul>
 * Reads {@link Camarasaurus#getCamarasaurusHealth()}, {@code getMaxHealth()} (synced health) and
 * {@link Camarasaurus#isSitting()} (the synced pose flag).
 *
 * <p>{@code f3} was {@code rotationYawHead - renderYawOffset} unwrapped; 1.21.1 passes it wrapped to
 * [-180, 180) (docs/research/06-models-design.md), which is the same value for every normal pose.
 */
public class CamarasaurusModel extends EntityModel<Camarasaurus> {

    /** Register with {@code CamarasaurusGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "camarasaurus"), "main");

    /** {@code ModelCamarasaurus(float f1)}: {@code wingspeed = f1} (:33-34); ClientProxyOreSpawn passes 0.65 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:193-213); it differs from the creation order in the legs. */
    private final ModelPart[] parts;
    private final ModelPart Tail0;
    private final ModelPart Tail1;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Neck1;
    private final ModelPart Neck2;
    private final ModelPart Neck3;
    private final ModelPart Head1;
    private final ModelPart Head2;
    private final ModelPart FLegupleft;
    private final ModelPart FLegdownleft;
    private final ModelPart FLegupright;
    private final ModelPart FLegdownright;
    private final ModelPart BLegupleft;
    private final ModelPart BLegdownleft;
    private final ModelPart BLegupright;
    private final ModelPart BLegdownright;

    public CamarasaurusModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        final String[] order = {
                CamarasaurusGeometry.BODY1, CamarasaurusGeometry.BODY2, CamarasaurusGeometry.BODY3, CamarasaurusGeometry.BODY4,
                CamarasaurusGeometry.TAIL0, CamarasaurusGeometry.NECK1, CamarasaurusGeometry.NECK2, CamarasaurusGeometry.NECK3,
                CamarasaurusGeometry.HEAD1, CamarasaurusGeometry.HEAD2, CamarasaurusGeometry.TAIL1, CamarasaurusGeometry.TAIL2,
                CamarasaurusGeometry.TAIL3, CamarasaurusGeometry.FLEGUPLEFT, CamarasaurusGeometry.FLEGDOWNLEFT,
                CamarasaurusGeometry.FLEGUPRIGHT, CamarasaurusGeometry.FLEGDOWNRIGHT, CamarasaurusGeometry.BLEGUPLEFT,
                CamarasaurusGeometry.BLEGDOWNRIGHT, CamarasaurusGeometry.BLEGUPRIGHT, CamarasaurusGeometry.BLEGDOWNLEFT
        };
        this.parts = new ModelPart[order.length];
        for (int i = 0; i < order.length; ++i) {
            this.parts[i] = root.getChild(order[i]);
        }
        this.Tail0 = root.getChild(CamarasaurusGeometry.TAIL0);
        this.Tail1 = root.getChild(CamarasaurusGeometry.TAIL1);
        this.Tail2 = root.getChild(CamarasaurusGeometry.TAIL2);
        this.Tail3 = root.getChild(CamarasaurusGeometry.TAIL3);
        this.Neck1 = root.getChild(CamarasaurusGeometry.NECK1);
        this.Neck2 = root.getChild(CamarasaurusGeometry.NECK2);
        this.Neck3 = root.getChild(CamarasaurusGeometry.NECK3);
        this.Head1 = root.getChild(CamarasaurusGeometry.HEAD1);
        this.Head2 = root.getChild(CamarasaurusGeometry.HEAD2);
        this.FLegupleft = root.getChild(CamarasaurusGeometry.FLEGUPLEFT);
        this.FLegdownleft = root.getChild(CamarasaurusGeometry.FLEGDOWNLEFT);
        this.FLegupright = root.getChild(CamarasaurusGeometry.FLEGUPRIGHT);
        this.FLegdownright = root.getChild(CamarasaurusGeometry.FLEGDOWNRIGHT);
        this.BLegupleft = root.getChild(CamarasaurusGeometry.BLEGUPLEFT);
        this.BLegdownleft = root.getChild(CamarasaurusGeometry.BLEGDOWNLEFT);
        this.BLegupright = root.getChild(CamarasaurusGeometry.BLEGUPRIGHT);
        this.BLegdownright = root.getChild(CamarasaurusGeometry.BLEGDOWNRIGHT);
    }

    /** The angle and pivot writes of {@code render()} (:154-192). */
    @Override
    public void setupAnim(final Camarasaurus entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final Camarasaurus c = entity;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        final float f3 = netHeadYaw;
        float hf = 0.0f;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.FLegupleft.xRot = newangle;
        this.FLegdownleft.xRot = newangle;
        this.FLegupright.xRot = -newangle;
        this.FLegdownright.xRot = -newangle;
        this.BLegupleft.xRot = -0.15f - newangle;
        this.BLegdownleft.xRot = -newangle;
        this.BLegupright.xRot = -0.15f + newangle;
        this.BLegdownright.xRot = newangle;
        hf = c.getCamarasaurusHealth() / c.getMaxHealth();
        newangle = Mth.cos(f2 * 1.5f * this.wingspeed * hf) * 3.1415927f * 0.25f * hf;
        if (c.isSitting()) {
            newangle = 0.0f;
        }
        this.Tail0.yRot = newangle * 0.25f;
        this.Tail1.z = this.Tail0.z + (float) Math.cos(this.Tail0.yRot) * 5.0f;
        this.Tail1.x = this.Tail0.x + (float) Math.sin(this.Tail0.yRot) * 5.0f;
        this.Tail1.yRot = newangle * 0.5f;
        this.Tail2.z = this.Tail1.z + (float) Math.cos(this.Tail1.yRot) * 8.0f;
        this.Tail2.x = this.Tail1.x + (float) Math.sin(this.Tail1.yRot) * 8.0f;
        this.Tail2.yRot = newangle * 0.75f;
        this.Tail3.z = this.Tail2.z + (float) Math.cos(this.Tail2.yRot) * 7.0f;
        this.Tail3.x = this.Tail2.x + (float) Math.sin(this.Tail2.yRot) * 7.0f;
        this.Tail3.yRot = newangle * 1.0f;
        this.Neck1.yRot = (float) Math.toRadians(f3) * 0.125f;
        this.Neck2.z = this.Neck1.z;
        this.Neck2.x = this.Neck1.x;
        this.Neck2.yRot = (float) Math.toRadians(f3) * 0.25f;
        this.Neck3.z = this.Neck2.z - (float) Math.cos(this.Neck2.yRot) * 6.0f;
        this.Neck3.x = this.Neck2.x - (float) Math.sin(this.Neck2.yRot) * 6.0f;
        this.Neck3.yRot = (float) Math.toRadians(f3) * 0.38f;
        this.Head1.z = this.Neck3.z - (float) Math.cos(this.Neck3.yRot) * 7.0f;
        this.Head1.x = this.Neck3.x - (float) Math.sin(this.Neck3.yRot) * 7.0f;
        this.Head1.yRot = (float) Math.toRadians(f3);
        this.Head2.z = this.Head1.z - (float) Math.cos(this.Head1.yRot) * 5.0f;
        this.Head2.x = this.Head1.x - (float) Math.sin(this.Head1.yRot) * 5.0f;
        this.Head2.yRot = (float) Math.toRadians(f3);
    }

    /** {@code render()} (:193-213). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
