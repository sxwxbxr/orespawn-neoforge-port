package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CockateilGeometry;
import com.swbr.orespawn.entity.critter.Cockateil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCockateil} (ModelCockateil.java:7-160): body, head, beak, three crest
 * feathers, two two-part wings, two legs and three tail feathers, geometry {@link CockateilGeometry} (64x32).
 * Shared by {@code Cockateil} and {@code RubyBird}. {@code wingspeed} is the constructor argument, 1.0 in
 * ClientProxyOreSpawn (manifest {@code model_args}). No GL calls.
 */
public class CockateilModel extends EntityModel<Cockateil> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cockateil"), "main");

    private final float wingspeed;
    private final ModelPart Body;
    private final ModelPart Head;
    private final ModelPart Beak;
    private final ModelPart LowerBeak;
    private final ModelPart feather2;
    private final ModelPart feather1;
    private final ModelPart feather3;
    private final ModelPart tailfeather1;
    private final ModelPart rwing1;
    private final ModelPart lwing1;
    private final ModelPart leg;
    private final ModelPart otherleg;
    private final ModelPart lwing2;
    private final ModelPart rwing2;
    private final ModelPart tailfeather2;
    private final ModelPart tailfeather3;

    /** {@code ModelCockateil(float f1)} (:26-109). */
    public CockateilModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Body = root.getChild(CockateilGeometry.BODY);
        this.Head = root.getChild(CockateilGeometry.HEAD);
        this.Beak = root.getChild(CockateilGeometry.BEAK);
        this.LowerBeak = root.getChild(CockateilGeometry.LOWER_BEAK);
        this.feather2 = root.getChild(CockateilGeometry.FEATHER2);
        this.feather1 = root.getChild(CockateilGeometry.FEATHER1);
        this.feather3 = root.getChild(CockateilGeometry.FEATHER3);
        this.tailfeather1 = root.getChild(CockateilGeometry.TAILFEATHER1);
        this.rwing1 = root.getChild(CockateilGeometry.RWING1);
        this.lwing1 = root.getChild(CockateilGeometry.LWING1);
        this.leg = root.getChild(CockateilGeometry.LEG);
        this.otherleg = root.getChild(CockateilGeometry.OTHERLEG);
        this.lwing2 = root.getChild(CockateilGeometry.LWING2);
        this.rwing2 = root.getChild(CockateilGeometry.RWING2);
        this.tailfeather2 = root.getChild(CockateilGeometry.TAILFEATHER2);
        this.tailfeather3 = root.getChild(CockateilGeometry.TAILFEATHER3);
    }

    /**
     * The writes of {@code render()} (:114-130), time-driven only ({@code f2} = age in ticks). The crest
     * feathers keep their rest {@code xRot}; {@code resetPose} restores every animated part first (R8).
     */
    @Override
    public void setupAnim(final Cockateil entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.lwing1.resetPose();
        this.lwing2.resetPose();
        this.rwing1.resetPose();
        this.rwing2.resetPose();
        this.tailfeather1.resetPose();
        this.tailfeather2.resetPose();
        this.tailfeather3.resetPose();
        this.feather1.resetPose();
        this.feather2.resetPose();
        this.feather3.resetPose();
        float newangle = 0.0f;
        newangle = Mth.cos(f2 * 1.5f * this.wingspeed) * 3.1415927f * 0.35f;
        this.lwing1.zRot = -1.5f + newangle;
        this.lwing2.zRot = newangle;
        this.rwing1.zRot = 1.5f - newangle;
        this.rwing2.zRot = -newangle;
        newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.1f;
        this.tailfeather1.xRot = newangle;
        this.tailfeather2.xRot = newangle;
        this.tailfeather3.xRot = newangle;
        newangle = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.08f;
        this.feather1.zRot = newangle;
        newangle = Mth.cos(f2 * 1.2f * this.wingspeed) * 3.1415927f * 0.08f;
        this.feather2.zRot = newangle;
        newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.08f;
        this.feather3.zRot = newangle;
    }

    /** Draw order of {@code render()} (:131-146). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.Body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Beak.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.LowerBeak.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.feather2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.feather1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.feather3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfeather1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lwing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.otherleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfeather2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfeather3.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
