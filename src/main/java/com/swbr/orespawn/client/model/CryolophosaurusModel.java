package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CryolophosaurusGeometry;
import com.swbr.orespawn.entity.dino.Cryolophosaurus;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCryolophosaurus} (ModelCryolophosaurus.java:7-188): 20 boxes, 128x128. Geometry
 * from the generated {@link CryolophosaurusGeometry}; the animation is {@code render()} (:138-177): legs and a jaw that
 * chews all the time (no DataWatcher read).
 */
public class CryolophosaurusModel extends EntityModel<Cryolophosaurus> {

    /** Register with {@code CryolophosaurusGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cryolophosaurus"), "main");

    /** {@code wingspeed = f1} (:35); ClientProxyOreSpawn passes 0.75 (ClientProxyOreSpawn.java:49). */
    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart jaw;
    private final ModelPart rightleg;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;
    private final ModelPart rightleg4;
    private final ModelPart leftleg;
    private final ModelPart leftleg2;
    private final ModelPart leftleg3;
    private final ModelPart leftleg4;

    public CryolophosaurusModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[CryolophosaurusGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(CryolophosaurusGeometry.PARTS[i]);
        }
        this.jaw = root.getChild(CryolophosaurusGeometry.JAW);
        this.rightleg = root.getChild(CryolophosaurusGeometry.RIGHTLEG);
        this.rightleg2 = root.getChild(CryolophosaurusGeometry.RIGHTLEG2);
        this.rightleg3 = root.getChild(CryolophosaurusGeometry.RIGHTLEG3);
        this.rightleg4 = root.getChild(CryolophosaurusGeometry.RIGHTLEG4);
        this.leftleg = root.getChild(CryolophosaurusGeometry.LEFTLEG);
        this.leftleg2 = root.getChild(CryolophosaurusGeometry.LEFTLEG2);
        this.leftleg3 = root.getChild(CryolophosaurusGeometry.LEFTLEG3);
        this.leftleg4 = root.getChild(CryolophosaurusGeometry.LEFTLEG4);
    }

    /** The angle writes of {@code render()} (:141-156). */
    @Override
    public void setupAnim(final Cryolophosaurus entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.rightleg.xRot = -0.2792527f + newangle;
        this.rightleg2.xRot = 0.384f + newangle;
        this.rightleg3.xRot = -0.68f + newangle;
        this.rightleg4.xRot = newangle;
        this.leftleg.xRot = -0.2792527f - newangle;
        this.leftleg2.xRot = 0.384f - newangle;
        this.leftleg3.xRot = -0.68f - newangle;
        this.leftleg4.xRot = -newangle;
        this.jaw.xRot = -1.15f + Mth.cos(f2 * 0.28f) * 3.1415927f * 0.1f;
    }

    /** {@code render()} (:157-176): all 20 parts. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
