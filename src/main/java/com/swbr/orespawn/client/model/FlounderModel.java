package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.FlounderGeometry;
import com.swbr.orespawn.entity.aquatic.Flounder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelFlounder} (ModelFlounder.java:7-94): six one-pixel plates on a 64x32 texture.
 * The geometry (:16-49) is the generated {@link FlounderGeometry}.
 *
 * <p>{@code render()} (:51-83): while swimming the side fins flap and the tail beats with the limb swing amount; at
 * rest the fins are still and the tail waves slowly (R8).
 */
public class FlounderModel extends EntityModel<Flounder> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "flounder"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart rfin;
    private final ModelPart lfin;

    /** {@code ModelFlounder()} (:16-49). */
    public FlounderModel(final ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild(FlounderGeometry.BODY);
        this.head = root.getChild(FlounderGeometry.HEAD);
        this.tail1 = root.getChild(FlounderGeometry.TAIL1);
        this.tail2 = root.getChild(FlounderGeometry.TAIL2);
        this.rfin = root.getChild(FlounderGeometry.RFIN);
        this.lfin = root.getChild(FlounderGeometry.LFIN);
    }

    /** The angle writes of {@code render()} (:54-76). */
    @Override
    public void setupAnim(final Flounder entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.lfin.resetPose();
        this.rfin.resetPose();
        this.tail1.resetPose();
        this.tail2.resetPose();
        float newangle;
        float newangle2;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.3f) * 3.1415927f * 0.25f * f1;
            newangle2 = Mth.cos(f2 * 1.7f) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
            newangle2 = 0.0f;
        }
        this.lfin.zRot = newangle;
        this.rfin.zRot = newangle2;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.2f) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = Mth.cos(f2 * 0.7f) * 3.1415927f * 0.05f;
        }
        final float n = newangle;
        this.tail2.xRot = n;
        this.tail1.xRot = n;
    }

    /** Draw order of {@code render()} (:77-82). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lfin.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
