package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.SkateGeometry;
import com.swbr.orespawn.entity.sea.Skate;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelSkate} (ModelSkate.java:7-62): a flat body, a long tail and a tail spine on a
 * 64x32 texture, geometry {@link SkateGeometry}. {@code wingspeed} is stored (1.0, ClientProxyOreSpawn :116) but not
 * read by {@code render()}. No GL calls.
 */
public class SkateModel extends EntityModel<Skate> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "skate"), "main");

    private final ModelPart body;
    private final ModelPart tail1;
    private final ModelPart Shape1;

    /** {@code ModelSkate(float f1)} (:14-...); {@code wingspeed} has no reader. */
    public SkateModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild(SkateGeometry.BODY);
        this.tail1 = root.getChild(SkateGeometry.TAIL1);
        this.Shape1 = root.getChild(SkateGeometry.SHAPE1);
    }

    /** The write of {@code render()}: the spine swings with the walk, or idles slowly. */
    @Override
    public void setupAnim(final Skate entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.Shape1.resetPose();
        float newangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 1.2f) * 3.1415927f * 0.15f * f1;
        } else {
            newangle = Mth.cos(f2 * 0.4f) * 3.1415927f * 0.05f;
        }
        this.Shape1.xRot = 0.785f + newangle;
    }

    /** Draw order of {@code render()}. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
