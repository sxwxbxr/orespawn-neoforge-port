package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.TerribleTerrorGeometry;
import com.swbr.orespawn.entity.terror.TerribleTerror;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelTerribleTerror} (ModelTerribleTerror.java:7-230): 21 boxes, 119x72 texture,
 * geometry from the generated {@link TerribleTerrorGeometry}. The animation is {@code render()}: flapping wings, a
 * snapping jaw, running legs and a four-segment tail whose pivots follow the segment before. No GL calls.
 */
public class TerribleTerrorModel extends EntityModel<TerribleTerror> {

    /** Register with {@code TerribleTerrorGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "terrible_terror"), "main");

    /** {@code wingspeed}: 1.0, the constructor takes no argument. */
    private final float wingspeed = 1.0f;
    /** Every part in {@code render()} order, which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart jaw;
    private final ModelPart wing1;
    private final ModelPart wing2;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart fl11;
    private final ModelPart fl12;
    private final ModelPart fl21;
    private final ModelPart fl22;
    private final ModelPart bl21;
    private final ModelPart bl22;
    private final ModelPart bl11;
    private final ModelPart bl12;

    public TerribleTerrorModel(final ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[TerribleTerrorGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(TerribleTerrorGeometry.PARTS[i]);
        }
        this.jaw = root.getChild(TerribleTerrorGeometry.JAW);
        this.wing1 = root.getChild(TerribleTerrorGeometry.WING1);
        this.wing2 = root.getChild(TerribleTerrorGeometry.WING2);
        this.tail1 = root.getChild(TerribleTerrorGeometry.TAIL1);
        this.tail2 = root.getChild(TerribleTerrorGeometry.TAIL2);
        this.tail3 = root.getChild(TerribleTerrorGeometry.TAIL3);
        this.tail4 = root.getChild(TerribleTerrorGeometry.TAIL4);
        this.fl11 = root.getChild(TerribleTerrorGeometry.FL11);
        this.fl12 = root.getChild(TerribleTerrorGeometry.FL12);
        this.fl21 = root.getChild(TerribleTerrorGeometry.FL21);
        this.fl22 = root.getChild(TerribleTerrorGeometry.FL22);
        this.bl21 = root.getChild(TerribleTerrorGeometry.BL21);
        this.bl22 = root.getChild(TerribleTerrorGeometry.BL22);
        this.bl11 = root.getChild(TerribleTerrorGeometry.BL11);
        this.bl12 = root.getChild(TerribleTerrorGeometry.BL12);
    }

    /** The writes of {@code render()} (ModelTerribleTerror.java:152-208). */
    @Override
    public void setupAnim(final TerribleTerror entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f;
        this.wing1.zRot = -2.0f + newangle;
        this.wing2.zRot = 2.0f - newangle;
        newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.1f;
        this.jaw.xRot = Math.abs(newangle);
        newangle = Mth.cos(f2 * 1.25f) * 3.1415927f * 0.35f;
        this.fl21.xRot = 0.349f + newangle;
        this.fl22.xRot = -0.296f + newangle;
        this.bl21.xRot = -0.349f - newangle;
        this.bl22.xRot = 0.174f - newangle;
        this.fl11.xRot = 0.349f - newangle;
        this.fl12.xRot = -0.296f - newangle;
        this.bl11.xRot = -0.349f + newangle;
        this.bl12.xRot = 0.174f + newangle;
        newangle = Mth.cos(f2 * 0.71f * this.wingspeed) * 3.1415927f * 0.1f;
        this.tail1.xRot = newangle;
        newangle = Mth.cos(f2 * 0.77f * this.wingspeed) * 3.1415927f * 0.1f;
        this.tail1.yRot = newangle;
        float dist = 6.0f;
        dist *= (float) Math.cos(this.tail1.xRot);
        this.tail2.y = (float) (this.tail1.y - Math.sin(this.tail1.xRot) * dist);
        this.tail2.x = (float) (this.tail1.x + Math.sin(this.tail1.yRot) * dist);
        newangle = Mth.cos(f2 * 0.81f * this.wingspeed) * 3.1415927f * 0.15f;
        this.tail2.xRot = newangle;
        newangle = Mth.cos(f2 * 0.87f * this.wingspeed) * 3.1415927f * 0.15f;
        this.tail2.yRot = newangle;
        dist = 6.0f;
        dist *= (float) Math.cos(this.tail2.xRot);
        final float n = (float) (this.tail2.y - Math.sin(this.tail2.xRot) * dist);
        this.tail4.y = n;
        this.tail3.y = n;
        final float n2 = (float) (this.tail2.x + Math.sin(this.tail2.yRot) * dist);
        this.tail4.x = n2;
        this.tail3.x = n2;
        newangle = Mth.cos(f2 * 0.91f * this.wingspeed) * 3.1415927f * 0.2f;
        this.tail4.xRot = newangle;
        this.tail3.xRot = newangle;
        newangle = Mth.cos(f2 * 0.97f * this.wingspeed) * 3.1415927f * 0.2f;
        this.tail4.yRot = newangle;
        this.tail3.yRot = newangle;
    }

    /** {@code render()} (:209-229): every part once, in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
