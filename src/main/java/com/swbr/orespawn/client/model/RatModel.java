package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.RatGeometry;
import com.swbr.orespawn.entity.terror.Rat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelRat} (ModelRat.java:7-138): 12 boxes, 64x64 texture, geometry from the generated
 * {@link RatGeometry}. The animation is {@code render()} (:90-124): legs only while walking faster than 0.1, a two-part
 * tail that lashes while the rat attacks (DataWatcher 20). No GL calls.
 */
public class RatModel extends EntityModel<Rat> {

    /** Register with {@code RatGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "rat"), "main");

    /** {@code ModelRat(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 1.0 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order, which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart lfleg;
    private final ModelPart rfleg;
    private final ModelPart lrleg;
    private final ModelPart rrleg;

    public RatModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[RatGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(RatGeometry.PARTS[i]);
        }
        this.tail1 = root.getChild(RatGeometry.TAIL1);
        this.tail2 = root.getChild(RatGeometry.TAIL2);
        this.lfleg = root.getChild(RatGeometry.LFLEG);
        this.rfleg = root.getChild(RatGeometry.RFLEG);
        this.lrleg = root.getChild(RatGeometry.LRLEG);
        this.rrleg = root.getChild(RatGeometry.RRLEG);
    }

    /** The writes of {@code render()} (:94-112). */
    @Override
    public void setupAnim(final Rat r, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.rfleg.xRot = newangle;
        this.lfleg.xRot = -newangle;
        this.rrleg.xRot = -newangle;
        this.lrleg.xRot = newangle;
        if (r.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.25f;
        } else {
            newangle = Mth.cos(f2 * 0.4f * this.wingspeed) * 3.1415927f * 0.05f;
        }
        this.tail1.yRot = newangle * 0.5f;
        this.tail2.yRot = newangle * 1.25f;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 9.0f;
        this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 9.0f;
    }

    /** {@code render()} (:113-124): every part once, in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
