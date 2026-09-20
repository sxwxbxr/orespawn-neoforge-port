package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.LeafMonsterGeometry;
import com.swbr.orespawn.entity.monster.LeafMonster;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelLeafMonster} (ModelLeafMonster.java:7-95): five 16-unit leaf cubes, 128x128
 * texture, geometry {@link LeafMonsterGeometry}. {@code render()} (:45-84): while not attacking a compact pile (body
 * pivot at 16, arms at 8, all angles 0); while attacking an upright figure paddling its legs with the walk and flailing
 * its arms. No constructor argument, no GL calls.
 */
public class LeafMonsterModel extends EntityModel<LeafMonster> {

    /** Register with {@code LeafMonsterGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "leaf_monster"), "main");

    private final ModelPart body;
    private final ModelPart larm;
    private final ModelPart rarm;
    private final ModelPart lleg;
    private final ModelPart rleg;

    /** {@code ModelLeafMonster()} (:14-43). */
    public LeafMonsterModel(final ModelPart root) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull (leaf holes).
        super(RenderType::entityCutoutNoCull);
        this.body = root.getChild(LeafMonsterGeometry.BODY);
        this.larm = root.getChild(LeafMonsterGeometry.LARM);
        this.rarm = root.getChild(LeafMonsterGeometry.RARM);
        this.lleg = root.getChild(LeafMonsterGeometry.LLEG);
        this.rleg = root.getChild(LeafMonsterGeometry.RLEG);
    }

    /** The writes of {@code render()} (:49-78). */
    @Override
    public void setupAnim(final LeafMonster lm, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.body.resetPose();
        this.larm.resetPose();
        this.rarm.resetPose();
        this.lleg.resetPose();
        this.rleg.resetPose();
        if (lm.getAttacking() == 0) {
            this.body.y = 16.0f;
            this.rarm.y = 8.0f;
            this.larm.y = 8.0f;
            this.rarm.yRot = 0.0f;
            this.larm.yRot = 0.0f;
            this.rarm.xRot = 0.0f;
            this.larm.xRot = 0.0f;
            this.lleg.xRot = 0.0f;
            this.rleg.xRot = 0.0f;
        } else {
            this.body.y = 0.0f;
            this.rarm.y = -8.0f;
            this.larm.y = -8.0f;
            float newangle;
            if (f1 > 0.1) {
                newangle = Mth.cos(f2 * 0.95f) * 3.1415927f * 0.25f * f1;
            } else {
                newangle = 0.0f;
            }
            this.lleg.xRot = newangle;
            this.rleg.xRot = -newangle;
            newangle = Mth.cos(f2 * 0.7f) * 3.1415927f * 0.55f;
            this.rarm.yRot = -Math.abs(newangle);
            this.larm.yRot = Math.abs(newangle);
            this.rarm.xRot = -Math.abs(newangle);
            this.larm.xRot = -Math.abs(newangle);
        }
    }

    /** The draw calls of {@code render()} (:79-83). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.larm.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rarm.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lleg.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rleg.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
