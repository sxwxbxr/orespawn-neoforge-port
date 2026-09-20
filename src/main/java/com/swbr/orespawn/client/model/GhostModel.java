package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.GhostGeometry;
import com.swbr.orespawn.entity.ghost.Ghost;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelGhost} (ModelGhost.java:8-62): body and two swaying arms, geometry
 * {@link GhostGeometry} (64x64).
 *
 * <p>The whole model is drawn inside one GL blend block (:41-50): {@code glBlendFunc(SRC_ALPHA,
 * ONE_MINUS_SRC_ALPHA)} and {@code glColor4f(0.75, 0.75, 0.75, 0.25)}. That is {@link RenderType#entityTranslucent}
 * (R8; no culling, as 1.7.10's living renderer disabled it) with the fixed colour {@link #GHOST_COLOR} on every
 * part. The fixed colour replaces the renderer's: the original's {@code glColor4f} overwrote the 15 % alpha an
 * invisible mob gets for a player who can see it, exactly as {@code FireflyModel}'s tail light ignores it.
 * {@code GL_NORMALIZE} (2977) has no counterpart; 1.21.1 transforms normals with the pose's normal matrix.
 */
public class GhostModel extends EntityModel<Ghost> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "ghost"), "main");

    /** {@code glColor4f(0.75f, 0.75f, 0.75f, 0.25f)} (:45). */
    static final int GHOST_COLOR = FastColor.ARGB32.colorFromFloat(0.25f, 0.75f, 0.75f, 0.75f);

    private final ModelPart HeadAndBody;
    private final ModelPart LArm;
    private final ModelPart RArm;

    /** {@code ModelGhost()} (:14-32). */
    public GhostModel(final ModelPart root) {
        super(RenderType::entityTranslucent);
        this.HeadAndBody = root.getChild(GhostGeometry.HEAD_AND_BODY);
        this.LArm = root.getChild(GhostGeometry.LARM);
        this.RArm = root.getChild(GhostGeometry.RARM);
    }

    /** The writes of {@code render()} (:37-40); both angles of both arms are overwritten each frame. */
    @Override
    public void setupAnim(final Ghost entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.LArm.resetPose();
        this.RArm.resetPose();
        this.LArm.zRot = -0.33f + Mth.cos(f2 * 0.3f) * 3.1415927f * 0.05f;
        this.RArm.zRot = 0.33f + Mth.cos(f2 * 0.32f) * 3.1415927f * 0.05f;
        this.LArm.xRot = -0.33f + Mth.cos(f2 * 0.34f) * 3.1415927f * 0.05f;
        this.RArm.xRot = 0.33f + Mth.cos(f2 * 0.36f) * 3.1415927f * 0.05f;
    }

    /** Draw order of {@code render()} (:46-48), all parts in the blend pass. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.HeadAndBody.render(poseStack, buffer, packedLight, packedOverlay, GHOST_COLOR);
        this.LArm.render(poseStack, buffer, packedLight, packedOverlay, GHOST_COLOR);
        this.RArm.render(poseStack, buffer, packedLight, packedOverlay, GHOST_COLOR);
    }
}
