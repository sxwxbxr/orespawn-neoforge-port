package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.IslandGeometry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Port of {@code danger.orespawn.ModelIsland} (ModelIsland.java:7-71): three 8x8x8 cubes on one pivot, 64x32, shared
 * by {@code island} and {@code island_too} (manifest {@code used_by}); the geometry is the generated
 * {@link IslandGeometry}. Generic over the entity so both renderers can bake the same layer.
 *
 * <p>The constructor argument is ignored, {@code wingspeed} stays 1.0 (:14-15). {@code render()} (:35-60) overwrites
 * all nine angles from {@code f2} (age in ticks) every frame - a slowly tumbling three-colour marker - so the pose of
 * the previous frame never leaks; {@link #setupAnim} still resets first (R8). No GL state in the model; drawn
 * cutout without culling like the other OreSpawn models.
 */
public class IslandModel<T extends LivingEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "island"), "main");

    private final float wingspeed;
    private final ModelPart Shape1;
    private final ModelPart Shape2;
    private final ModelPart Shape3;

    /** {@code ModelIsland(float f)} (:14-33): {@code f} is unused. */
    public IslandModel(final ModelPart root, final float f) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = 1.0f;
        this.Shape1 = root.getChild(IslandGeometry.SHAPE1);
        this.Shape2 = root.getChild(IslandGeometry.SHAPE2);
        this.Shape3 = root.getChild(IslandGeometry.SHAPE3);
    }

    /** The angle writes of {@code render()} (:38-56). {@code setRotationAngles} (:68-70) only calls {@code super}. */
    @Override
    public void setupAnim(final T entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.Shape1.resetPose();
        this.Shape2.resetPose();
        this.Shape3.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 0.05f * this.wingspeed) * 3.1415927f;
        this.Shape1.xRot = newangle;
        newangle = Mth.cos(f2 * 0.051f * this.wingspeed) * 3.1415927f;
        this.Shape1.yRot = newangle;
        newangle = Mth.cos(f2 * 0.052f * this.wingspeed) * 3.1415927f;
        this.Shape1.zRot = newangle;
        newangle = Mth.cos(f2 * 0.053f * this.wingspeed) * 3.1415927f;
        this.Shape2.xRot = newangle;
        newangle = Mth.cos(f2 * 0.054f * this.wingspeed) * 3.1415927f;
        this.Shape2.yRot = newangle;
        newangle = Mth.cos(f2 * 0.055f * this.wingspeed) * 3.1415927f;
        this.Shape2.zRot = newangle;
        newangle = Mth.cos(f2 * 0.056f * this.wingspeed) * 3.1415927f;
        this.Shape3.xRot = newangle;
        newangle = Mth.cos(f2 * 0.057f * this.wingspeed) * 3.1415927f;
        this.Shape3.yRot = newangle;
        newangle = Mth.cos(f2 * 0.058f * this.wingspeed) * 3.1415927f;
        this.Shape3.zRot = newangle;
    }

    /** Draw order of {@code render()} (:57-59). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Shape1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Shape3.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
