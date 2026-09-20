package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.GhostSkellyGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.ghost.GhostSkelly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelGhostSkelly} (ModelGhostSkelly.java:8-156): a stick body in a shirt, a
 * pumpkin head with stem, two long arms with sleeves and chains, geometry {@link GhostSkellyGeometry} (128x64).
 *
 * <p>Arms, sleeves and chains sway together per side; the pumpkin head spins one full turn and back on a slow
 * cosine, but only in cycles where a 1/3 roll came up when the phase wrapped (the {@code rf2}/{@code ri2} note pad in
 * the entity's {@link RenderInfo}). The roll uses the client world's random and is frame-driven, as in the original.
 *
 * <p>Like {@link GhostModel} the whole model is one blend pass with {@code glColor4f(0.75, 0.75, 0.75, 0.25)}
 * (:128-144): {@link RenderType#entityTranslucent} with {@link GhostModel#GHOST_COLOR} on every part.
 */
public class GhostSkellyModel extends EntityModel<GhostSkelly> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ghost_pumpkin_skelly"), "main");

    private final ModelPart body;
    private final ModelPart shirt;
    private final ModelPart head;
    private final ModelPart stem;
    private final ModelPart rarm;
    private final ModelPart larm;
    private final ModelPart rsleeve;
    private final ModelPart lsleeve;
    private final ModelPart lchains;
    private final ModelPart rchains;

    /** {@code ModelGhostSkelly()} (:21-74). */
    public GhostSkellyModel(final ModelPart root) {
        super(RenderType::entityTranslucent);
        this.body = root.getChild(GhostSkellyGeometry.BODY);
        this.shirt = root.getChild(GhostSkellyGeometry.SHIRT);
        this.head = root.getChild(GhostSkellyGeometry.HEAD);
        this.stem = root.getChild(GhostSkellyGeometry.STEM);
        this.rarm = root.getChild(GhostSkellyGeometry.RARM);
        this.larm = root.getChild(GhostSkellyGeometry.LARM);
        this.rsleeve = root.getChild(GhostSkellyGeometry.RSLEEVE);
        this.lsleeve = root.getChild(GhostSkellyGeometry.LSLEEVE);
        this.lchains = root.getChild(GhostSkellyGeometry.LCHAINS);
        this.rchains = root.getChild(GhostSkellyGeometry.RCHAINS);
    }

    /** The writes of {@code render()} (:83-127). */
    @Override
    public void setupAnim(final GhostSkelly e, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.larm.resetPose();
        this.lsleeve.resetPose();
        this.lchains.resetPose();
        this.rarm.resetPose();
        this.rsleeve.resetPose();
        this.rchains.resetPose();
        this.head.resetPose();
        float newangle = 0.0f;
        float newrf1 = 0.0f;
        RenderInfo r = null;
        r = e.getRenderInfo();
        final float rotateAngleZ = Mth.cos(f2 * 0.2f) * 3.1415927f * 0.05f;
        this.lchains.zRot = rotateAngleZ;
        this.lsleeve.zRot = rotateAngleZ;
        this.larm.zRot = rotateAngleZ;
        final float rotateAngleZ2 = Mth.cos(f2 * 0.22f) * 3.1415927f * 0.05f;
        this.rchains.zRot = rotateAngleZ2;
        this.rsleeve.zRot = rotateAngleZ2;
        this.rarm.zRot = rotateAngleZ2;
        final float rotateAngleY = Mth.cos(f2 * 0.24f) * 3.1415927f * 0.05f;
        this.lchains.yRot = rotateAngleY;
        this.lsleeve.yRot = rotateAngleY;
        this.larm.yRot = rotateAngleY;
        final float rotateAngleY2 = Mth.cos(f2 * 0.26f) * 3.1415927f * 0.05f;
        this.rchains.yRot = rotateAngleY2;
        this.rsleeve.yRot = rotateAngleY2;
        this.rarm.yRot = rotateAngleY2;
        newangle = Mth.cos(f2 * 0.05f) * 3.1415927f * 2.0f;
        newrf1 = f2 * 0.05f % 6.2831855f;
        newrf1 = Math.abs(newrf1);
        if (newrf1 < r.rf2) {
            r.ri2 = 0;
            if (e.level().random.nextInt(3) == 1) {
                final RenderInfo renderInfo = r;
                renderInfo.ri2 |= 0x1;
            }
        }
        r.rf2 = newrf1;
        if ((r.ri2 & 0x1) == 0x0) {
            newangle = 0.0f;
        }
        this.head.yRot = newangle;
        e.setRenderInfo(r);
    }

    /** Draw order of {@code render()} (:133-142), all parts in the blend pass. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final int c = GhostModel.GHOST_COLOR;
        this.body.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.shirt.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.stem.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.rarm.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.larm.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.rsleeve.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.lsleeve.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.lchains.render(poseStack, buffer, packedLight, packedOverlay, c);
        this.rchains.render(poseStack, buffer, packedLight, packedOverlay, c);
    }
}
