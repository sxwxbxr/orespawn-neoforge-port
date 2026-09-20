package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.RubberDuckyGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.pet.RubberDucky;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelRubberDucky} (ModelRubberDucky.java:7-130): 8 boxes - bottom, body, back, neck,
 * head, beak and two wings - 64x64 texture. Geometry from the generated {@link RubberDuckyGeometry}; the animation is
 * {@code render()} (:66-119): head and beak follow the look; the wings flap for one sway cycle when a roll at the
 * upward zero crossing of the sway says so (1 in 3 on the client world random, an evil duck also 1 in 2 and four times
 * as wide on the crossing frame), never while sitting. The flap decision lives in the entity's {@link RenderInfo}
 * ({@code ri1}), frame-driven, as in the original.
 *
 * <p>{@code setRotationAngles} (:127-129) only calls the empty {@code ModelBase} one.
 */
public class RubberDuckyModel extends EntityModel<RubberDucky> {

    /** Register with {@code RubberDuckyGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "rubber_ducky"), "main");

    /** {@code ModelRubberDucky(float f1)}: {@code wingspeed = f1} (:19-21); ClientProxyOreSpawn passes 1.0 (manifest). */
    private final float wingspeed;
    /** Every part in {@code render()} order (:111-118), which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart Lwing;
    private final ModelPart Rwing;

    public RubberDuckyModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[RubberDuckyGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(RubberDuckyGeometry.PARTS[i]);
        }
        this.head = root.getChild(RubberDuckyGeometry.HEAD);
        this.beak = root.getChild(RubberDuckyGeometry.BEAK);
        this.Lwing = root.getChild(RubberDuckyGeometry.LWING);
        this.Rwing = root.getChild(RubberDuckyGeometry.RWING);
    }

    /**
     * The angle writes of {@code render()} (:66-110). {@code c.worldObj.rand} is the client world's random here, as in
     * the original.
     */
    @Override
    public void setupAnim(final RubberDucky c, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float nextangle = 0.0f;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 2.3f * this.wingspeed) * 3.1415927f * 0.25f * f1;
        } else {
            newangle = 0.0f;
        }
        this.head.yRot = (float) Math.toRadians(f3) * 0.45f;
        this.beak.yRot = this.head.yRot;
        this.head.xRot = (float) Math.toRadians(f4) * 0.65f;
        this.beak.xRot = this.head.xRot;
        r = c.getRenderInfo();
        newangle = Mth.cos(f2 * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        nextangle = Mth.cos((f2 + 0.3f) * 1.0f * this.wingspeed) * 3.1415927f * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (c.level().random.nextInt(3) == 1) {
                r.ri1 = 1;
            }
            if (c.getKillCount() >= 5) {
                if (c.level().random.nextInt(2) == 1) {
                    r.ri1 = 1;
                }
                newangle *= 4.0f;
            }
        }
        if (r.ri1 == 0) {
            newangle = 0.0f;
        }
        if (c.isSitting()) {
            newangle = 0.0f;
        }
        newangle = Math.abs(newangle);
        this.Lwing.zRot = -newangle;
        this.Lwing.yRot = newangle / 2.0f;
        this.Rwing.zRot = newangle;
        this.Rwing.yRot = -newangle / 2.0f;
        c.setRenderInfo(r);
    }

    /** {@code render()} (:111-118). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
