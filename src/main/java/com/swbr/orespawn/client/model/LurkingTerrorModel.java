package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.LurkingTerrorGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.terror.LurkingTerror;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelLurkingTerror} (ModelLurkingTerror.java:7-663): 59 boxes, 256x64 texture, geometry
 * from the generated {@link LurkingTerrorGeometry}. The animation is {@code render()} (:372-652): wings beat all the
 * time; legs twitch at random - once per cycle of {@code f2 * 0.7} a bit mask is rolled into the entity's
 * {@link RenderInfo} on the client world's random; the four jaws open and the tongue shoots out with a 1/20 roll per
 * cycle of {@code f2 * 0.9} or whenever the terror attacks (DataWatcher 20); thorax and abdomen breathe. No GL calls.
 */
public class LurkingTerrorModel extends EntityModel<LurkingTerror> {

    /** Register with {@code LurkingTerrorGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "lurking_terror"), "main");

    /** {@code wingspeed}: 1.0, the constructor takes no argument. */
    private final float wingspeed = 1.0f;
    /** Every part in {@code render()} order, which is the creation order. */
    private final ModelPart[] parts;
    private final ModelPart leg1;
    private final ModelPart leg1part2;
    private final ModelPart leg1part3;
    private final ModelPart leg2;
    private final ModelPart leg2part2;
    private final ModelPart leg2part3;
    private final ModelPart leg3;
    private final ModelPart leg3part2;
    private final ModelPart leg3part3;
    private final ModelPart leg4;
    private final ModelPart leg4part2;
    private final ModelPart leg4part3;
    private final ModelPart leg5;
    private final ModelPart leg5part2;
    private final ModelPart leg6;
    private final ModelPart leg6part2;
    private final ModelPart thorax;
    private final ModelPart abdomen;
    private final ModelPart jaw1;
    private final ModelPart jaw1part2;
    private final ModelPart[] jaw1teeth;
    private final ModelPart jaw2;
    private final ModelPart jaw2part2;
    private final ModelPart[] jaw2teeth;
    private final ModelPart jaw3;
    private final ModelPart jaw3part2;
    private final ModelPart[] jaw3teeth;
    private final ModelPart jaw4;
    private final ModelPart jaw4part2;
    private final ModelPart[] jaw4teeth;
    private final ModelPart tonguepart1;
    private final ModelPart tonguepart2;
    private final ModelPart tonguepart3;
    private final ModelPart wing_1;
    private final ModelPart wing_2;
    private final ModelPart wing_3;
    private final ModelPart wing_4;

    public LurkingTerrorModel(final ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.parts = new ModelPart[LurkingTerrorGeometry.PARTS.length];
        for (int i = 0; i < this.parts.length; ++i) {
            this.parts[i] = root.getChild(LurkingTerrorGeometry.PARTS[i]);
        }
        this.leg1 = root.getChild(LurkingTerrorGeometry.LEG1);
        this.leg1part2 = root.getChild(LurkingTerrorGeometry.LEG1PART2);
        this.leg1part3 = root.getChild(LurkingTerrorGeometry.LEG1PART3);
        this.leg2 = root.getChild(LurkingTerrorGeometry.LEG2);
        this.leg2part2 = root.getChild(LurkingTerrorGeometry.LEG2PART2);
        this.leg2part3 = root.getChild(LurkingTerrorGeometry.LEG2PART3);
        this.leg3 = root.getChild(LurkingTerrorGeometry.LEG3);
        this.leg3part2 = root.getChild(LurkingTerrorGeometry.LEG3PART2);
        this.leg3part3 = root.getChild(LurkingTerrorGeometry.LEG3PART3);
        this.leg4 = root.getChild(LurkingTerrorGeometry.LEG4);
        this.leg4part2 = root.getChild(LurkingTerrorGeometry.LEG4PART2);
        this.leg4part3 = root.getChild(LurkingTerrorGeometry.LEG4PART3);
        this.leg5 = root.getChild(LurkingTerrorGeometry.LEG5);
        this.leg5part2 = root.getChild(LurkingTerrorGeometry.LEG5PART2);
        this.leg6 = root.getChild(LurkingTerrorGeometry.LEG6);
        this.leg6part2 = root.getChild(LurkingTerrorGeometry.LEG6PART2);
        this.thorax = root.getChild(LurkingTerrorGeometry.THORAX);
        this.abdomen = root.getChild(LurkingTerrorGeometry.ABDOMEN);
        this.jaw1 = root.getChild(LurkingTerrorGeometry.JAW1);
        this.jaw1part2 = root.getChild(LurkingTerrorGeometry.JAW1PART2);
        this.jaw1teeth = new ModelPart[] {
                root.getChild(LurkingTerrorGeometry.JAW1TOOTH1), root.getChild(LurkingTerrorGeometry.JAW1TOOTH2),
                root.getChild(LurkingTerrorGeometry.JAW1TOOTH3), root.getChild(LurkingTerrorGeometry.JAW1TOOTH4),
                root.getChild(LurkingTerrorGeometry.JAW1TOOTH5), root.getChild(LurkingTerrorGeometry.JAW1TOOTH6)};
        this.jaw2 = root.getChild(LurkingTerrorGeometry.JAW2);
        this.jaw2part2 = root.getChild(LurkingTerrorGeometry.JAW2PART2);
        this.jaw2teeth = new ModelPart[] {
                root.getChild(LurkingTerrorGeometry.JAW2TOOTH1), root.getChild(LurkingTerrorGeometry.JAW2TOOTH2),
                root.getChild(LurkingTerrorGeometry.JAW2TOOTH3), root.getChild(LurkingTerrorGeometry.JAW2TOOTH4),
                root.getChild(LurkingTerrorGeometry.JAW2TOOTH5), root.getChild(LurkingTerrorGeometry.JAW2TOOTH6)};
        this.jaw3 = root.getChild(LurkingTerrorGeometry.JAW3);
        this.jaw3part2 = root.getChild(LurkingTerrorGeometry.JAW3PART2);
        this.jaw3teeth = new ModelPart[] {
                root.getChild(LurkingTerrorGeometry.JAW3TOOTH1), root.getChild(LurkingTerrorGeometry.JAW3TOOTH2),
                root.getChild(LurkingTerrorGeometry.JAW3TOOTH3), root.getChild(LurkingTerrorGeometry.JAW3TOOTH4),
                root.getChild(LurkingTerrorGeometry.JAW3TOOTH5), root.getChild(LurkingTerrorGeometry.JAW3TOOTH6)};
        this.jaw4 = root.getChild(LurkingTerrorGeometry.JAW4);
        this.jaw4part2 = root.getChild(LurkingTerrorGeometry.JAW4PART2);
        this.jaw4teeth = new ModelPart[] {
                root.getChild(LurkingTerrorGeometry.JAW4TOOTH1), root.getChild(LurkingTerrorGeometry.JAW4TOOTH2),
                root.getChild(LurkingTerrorGeometry.JAW4TOOTH3), root.getChild(LurkingTerrorGeometry.JAW4TOOTH4),
                root.getChild(LurkingTerrorGeometry.JAW4TOOTH5), root.getChild(LurkingTerrorGeometry.JAW4TOOTH6)};
        this.tonguepart1 = root.getChild(LurkingTerrorGeometry.TONGUEPART1);
        this.tonguepart2 = root.getChild(LurkingTerrorGeometry.TONGUEPART2);
        this.tonguepart3 = root.getChild(LurkingTerrorGeometry.TONGUEPART3);
        this.wing_1 = root.getChild(LurkingTerrorGeometry.WING_1);
        this.wing_2 = root.getChild(LurkingTerrorGeometry.WING_2);
        this.wing_3 = root.getChild(LurkingTerrorGeometry.WING_3);
        this.wing_4 = root.getChild(LurkingTerrorGeometry.WING_4);
    }

    /**
     * The writes of {@code render()} (:380-592). {@code e.worldObj.rand} is the client world's random here, as in the
     * original (CaveFisherModel, W07).
     */
    @Override
    public void setupAnim(final LurkingTerror e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        final float legspeed = 0.7f;
        final float mouthspeed = 0.9f;
        RenderInfo r = null;
        r = e.getRenderInfo();
        newangle = f2 * legspeed * this.wingspeed % 6.2831855f;
        newangle = Math.abs(newangle);
        if (newangle < r.rf1) {
            r.ri1 = 0;
            if (e.level().random.nextInt(3) == 1) {
                r.ri1 |= 0x1;
            }
            if (e.level().random.nextInt(3) == 1) {
                r.ri1 |= 0x2;
            }
            if (e.level().random.nextInt(4) == 1) {
                r.ri1 |= 0x4;
            }
            if (e.level().random.nextInt(4) == 1) {
                r.ri1 |= 0x8;
            }
            if (e.level().random.nextInt(6) == 1) {
                r.ri1 |= 0x10;
            }
            if (e.level().random.nextInt(6) == 1) {
                r.ri1 |= 0x20;
            }
        }
        r.rf1 = newangle;
        newangle = f2 * mouthspeed * this.wingspeed % 6.2831855f;
        newangle = Math.abs(newangle);
        if (newangle < r.rf2) {
            r.ri2 = 0;
            if (e.level().random.nextInt(20) == 1) {
                r.ri2 |= 0x1;
            }
            if (e.getAttacking() != 0) {
                r.ri2 = 1;
            }
        }
        r.rf2 = newangle;
        newangle = 0.0f;
        if ((r.ri1 & 0x1) != 0x0) {
            newangle = Mth.sin(f2 * legspeed * this.wingspeed) * 3.1415927f * 0.25f;
        }
        final float n = 0.191f + newangle;
        this.leg2part2.zRot = n;
        this.leg2.zRot = n;
        this.leg2part3.zRot = 0.675f + newangle;
        newangle = 0.0f;
        if ((r.ri1 & 0x2) != 0x0) {
            newangle = Mth.sin(f2 * legspeed * this.wingspeed) * 3.1415927f * 0.25f;
        }
        final float n2 = -0.191f + newangle;
        this.leg1part2.zRot = n2;
        this.leg1.zRot = n2;
        this.leg1part3.zRot = -0.675f + newangle;
        newangle = 0.0f;
        if ((r.ri1 & 0x4) != 0x0) {
            newangle = Mth.sin(f2 * legspeed * this.wingspeed) * 3.1415927f * 0.15f;
        }
        final float n3 = 0.191f + newangle;
        this.leg4part2.zRot = n3;
        this.leg4.zRot = n3;
        this.leg4part3.zRot = 0.675f + newangle;
        newangle = 0.0f;
        if ((r.ri1 & 0x8) != 0x0) {
            newangle = Mth.sin(f2 * legspeed * this.wingspeed) * 3.1415927f * 0.15f;
        }
        final float n4 = -0.191f + newangle;
        this.leg3part2.zRot = n4;
        this.leg3.zRot = n4;
        this.leg3part3.zRot = -0.675f + newangle;
        newangle = 0.0f;
        if ((r.ri1 & 0x10) != 0x0) {
            newangle = Mth.sin(f2 * legspeed * this.wingspeed) * 3.1415927f * 0.1f;
        }
        final float n5 = -0.34f + newangle;
        this.leg6part2.zRot = n5;
        this.leg6.zRot = n5;
        newangle = 0.0f;
        if ((r.ri1 & 0x20) != 0x0) {
            newangle = Mth.sin(f2 * legspeed * this.wingspeed) * 3.1415927f * 0.1f;
        }
        final float n6 = 0.34f + newangle;
        this.leg5part2.zRot = n6;
        this.leg5.zRot = n6;
        newangle = 0.0f;
        if ((r.ri2 & 0x1) != 0x0) {
            newangle = Mth.sin(f2 * mouthspeed * this.wingspeed) * 3.1415927f * 0.35f;
            newangle = Math.abs(newangle);
        }
        this.jaw1.yRot = newangle;
        this.jaw1part2.yRot = newangle;
        for (final ModelPart tooth : this.jaw1teeth) {
            tooth.yRot = newangle;
        }
        this.jaw2.yRot = -newangle;
        this.jaw2part2.yRot = -newangle;
        for (final ModelPart tooth : this.jaw2teeth) {
            tooth.yRot = -newangle;
        }
        this.jaw3.xRot = -newangle;
        this.jaw3part2.xRot = -newangle;
        for (final ModelPart tooth : this.jaw3teeth) {
            tooth.xRot = -newangle;
        }
        this.jaw4.xRot = newangle;
        this.jaw4part2.xRot = newangle;
        for (final ModelPart tooth : this.jaw4teeth) {
            tooth.xRot = newangle;
        }
        this.tonguepart3.xRot = 0.0f;
        this.tonguepart2.xRot = 0.0f;
        this.tonguepart1.xRot = 0.0f;
        this.tonguepart3.yRot = 0.0f;
        this.tonguepart2.yRot = 0.0f;
        this.tonguepart1.yRot = 0.0f;
        this.tonguepart3.zRot = 0.0f;
        this.tonguepart2.zRot = 0.0f;
        this.tonguepart1.zRot = 0.0f;
        this.tonguepart3.x = this.tonguepart2.x;
        this.tonguepart1.x = this.tonguepart2.x;
        this.tonguepart3.y = this.tonguepart2.y;
        this.tonguepart1.y = this.tonguepart2.y;
        this.tonguepart1.z = this.tonguepart2.z - newangle * 5.0f;
        this.tonguepart3.z = this.tonguepart2.z - newangle * 10.0f;
        newangle = Mth.sin(f2 * 0.1f * this.wingspeed) * 3.1415927f * 0.06f;
        this.thorax.xRot = newangle;
        this.abdomen.y = (float) (this.thorax.y - Math.sin(newangle) * 14.0);
        newangle = Mth.cos(f2 * 1.4f * this.wingspeed) * 3.1415927f * 0.2f;
        this.wing_1.xRot = 0.455f + newangle;
        this.wing_2.xRot = 0.455f + newangle;
        this.wing_3.xRot = 0.455f - newangle;
        this.wing_4.xRot = 0.455f - newangle;
        e.setRenderInfo(r);
    }

    /** {@code render()} (:593-651): every part once, in creation order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.parts) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }
}
