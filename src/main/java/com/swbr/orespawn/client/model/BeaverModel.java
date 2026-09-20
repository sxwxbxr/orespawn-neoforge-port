package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.BeaverGeometry;
import com.swbr.orespawn.entity.herbivore.Beaver;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelBeaver} (ModelBeaver.java:6-111): head, nose, teeth, body, flat tail and
 * four feet, 64x32 texture. Geometry from the generated {@link BeaverGeometry}; the animation is {@code render()}
 * (:72-99): feet diagonally opposed, teeth gnawing and a slow tail wag, the last two also standing still.
 */
public class BeaverModel extends EntityModel<Beaver> {

    /** Register with {@code BeaverGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "beaver"), "main");

    /** {@code ModelBeaver(float f1)}: {@code wingspeed = f1} (:21-22); ClientProxyOreSpawn passes 0.5 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:91-99), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart teeth;
    private final ModelPart tail;
    private final ModelPart rff;
    private final ModelPart lff;
    private final ModelPart rrf;
    private final ModelPart lrf;

    public BeaverModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[BeaverGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(BeaverGeometry.PARTS[i]);
        }
        this.teeth = root.getChild(BeaverGeometry.TEETH);
        this.tail = root.getChild(BeaverGeometry.TAIL);
        this.rff = root.getChild(BeaverGeometry.RFF);
        this.lff = root.getChild(BeaverGeometry.LFF);
        this.rrf = root.getChild(BeaverGeometry.RRF);
        this.lrf = root.getChild(BeaverGeometry.LRF);
    }

    /** The angle writes of {@code render()} (:76-90). No {@code f1 > 0.1} gate in this model. */
    @Override
    public void setupAnim(final Beaver entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 3.7f * this.wingspeed) * 3.1415927f * 0.45f * f1;
        this.lrf.xRot = newangle;
        this.rff.xRot = newangle;
        this.rrf.xRot = -newangle;
        this.lff.xRot = -newangle;
        newangle = Mth.cos(f2 * 2.7f * this.wingspeed) * 3.1415927f * 0.25f;
        this.teeth.xRot = newangle;
        newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.05f;
        this.tail.xRot = newangle;
    }

    /** {@code render()} (:91-99). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
