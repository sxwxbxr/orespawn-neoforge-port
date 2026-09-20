package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.MosquitoGeometry;
import com.swbr.orespawn.entity.insect.EntityMosquito;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelMosquito} (ModelMosquito.java:7-68): a one-pixel body with two
 * wing panels per side, geometry {@link MosquitoGeometry}. The boxes were added while the model's texture
 * size was 32x32, so the later {@code setTextureSize(64, 32)} of every part (:20 etc.) never reached
 * their UVs; the generated layer uses 32x32.
 *
 * <p>{@code render()} (:45-57) draws the body, then writes the flap angle ({@code cos(f2 * 3)}, three
 * times the butterfly's beat) and draws the wings - the writes move to {@link #setupAnim} (R8).
 */
public class MosquitoModel extends EntityModel<EntityMosquito> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "mosquito"), "main");

    private final ModelPart body;
    private final ModelPart leftwing1;
    private final ModelPart rightwing1;
    private final ModelPart leftwing2;
    private final ModelPart rightwing2;

    /** {@code ModelMosquito()} (:15-43). */
    public MosquitoModel(final ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild(MosquitoGeometry.BODY);
        this.leftwing1 = root.getChild(MosquitoGeometry.LEFTWING1);
        this.rightwing1 = root.getChild(MosquitoGeometry.RIGHTWING1);
        this.leftwing2 = root.getChild(MosquitoGeometry.LEFTWING2);
        this.rightwing2 = root.getChild(MosquitoGeometry.RIGHTWING2);
    }

    /** The angle writes of {@code render()} (:49-52). */
    @Override
    public void setupAnim(final EntityMosquito entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.rightwing1.resetPose();
        this.rightwing2.resetPose();
        this.leftwing1.resetPose();
        this.leftwing2.resetPose();
        this.rightwing1.zRot = Mth.cos(f2 * 3.0f) * 3.1415927f * 0.25f;
        this.rightwing2.zRot = this.rightwing1.zRot;
        this.leftwing1.zRot = -this.rightwing1.zRot;
        this.leftwing2.zRot = -this.rightwing1.zRot;
    }

    /** Draw order of {@code render()} (:48-56). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
