package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CliffRacerGeometry;
import com.swbr.orespawn.entity.critter.CliffRacer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCliffRacer} (ModelCliffRacer.java:7-92): body, fin, two wings, tail,
 * tail end, head and beak, geometry {@link CliffRacerGeometry} (64x64). {@code wingspeed} 1.0 in
 * ClientProxyOreSpawn. No GL calls. {@code RWing} reaches one pixel past the texture (uv 39 + 26 = 65); the
 * normalised UV wraps in 1.21.1 as it did in 1.7.10 (docs/research/06-models-design.md).
 */
public class CliffRacerModel extends EntityModel<CliffRacer> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cliff_racer"), "main");

    private final float wingspeed;
    private final ModelPart Body;
    private final ModelPart Fins;
    private final ModelPart LWing;
    private final ModelPart RWing;
    private final ModelPart Tail;
    private final ModelPart TailEnd;
    private final ModelPart Head;
    private final ModelPart Beak;

    /** {@code ModelCliffRacer(float f1)} (:18-62). */
    public CliffRacerModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Body = root.getChild(CliffRacerGeometry.BODY);
        this.Fins = root.getChild(CliffRacerGeometry.FINS);
        this.LWing = root.getChild(CliffRacerGeometry.LWING);
        this.RWing = root.getChild(CliffRacerGeometry.RWING);
        this.Tail = root.getChild(CliffRacerGeometry.TAIL);
        this.TailEnd = root.getChild(CliffRacerGeometry.TAIL_END);
        this.Head = root.getChild(CliffRacerGeometry.HEAD);
        this.Beak = root.getChild(CliffRacerGeometry.BEAK);
    }

    /** The writes of {@code render()} (:68-72): a constant wing beat. */
    @Override
    public void setupAnim(final CliffRacer entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.LWing.resetPose();
        this.RWing.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.25f;
        this.LWing.zRot = newangle;
        this.RWing.zRot = -newangle;
    }

    /** Draw order of {@code render()} (:73-80). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Fins.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LWing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.RWing.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tail.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.TailEnd.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Beak.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
