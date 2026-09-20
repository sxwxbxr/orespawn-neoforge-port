package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.CephadromeGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.cephadrome.Cephadrome;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelCephadrome} (ModelCephadrome.java:7-528), geometry {@link CephadromeGeometry}
 * (512x256, 50 parts). {@code wingspeed} is the constructor argument, 0.55 in ClientProxyOreSpawn (manifest
 * {@code model_args}).
 *
 * <p>Animation from {@code render()} (:318-466, design-entities-02.md): on the ground the legs swing with the distance
 * moved in the last tick (not {@code limbSwing}), in flight they are drawn up ({@code newangle = 1}); the wings flap
 * in flight, rest or beat fast while attacking on the ground; the back fins breathe; the three tail segments sway about
 * Y with chained pivots and every tail fin and membrane follows {@code tailfin1}; the neck and head turn with half the
 * head yaw on the ground, or in flight with a smoothed turn rate kept in the entity's {@link RenderInfo}
 * ({@code rf1}), again with chained pivots, and hammer head and mouth follow the head; the mouth snaps while attacking.
 *
 * <p>No GL calls in the original, the membranes are ordinary zero-thickness boxes: {@link RenderType#entityCutoutNoCull}
 * (alpha-tested, no culling, as 1.7.10's living renderer). {@code setRotationAngles} (:525-527) only calls
 * {@code super} and changes nothing.
 */
public class CephadromeModel extends EntityModel<Cephadrome> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "cephadrome"), "main");

    private final float wingspeed;
    private final ModelPart leftfoot;
    private final ModelPart butt;
    private final ModelPart rightfoot;
    private final ModelPart topfin1;
    private final ModelPart topfin2;
    private final ModelPart topfin3;
    private final ModelPart topfin4;
    private final ModelPart leftshoulder;
    private final ModelPart lefwingfin1;
    private final ModelPart tailfin1;
    private final ModelPart tailmembrane2;
    private final ModelPart tailfin2;
    private final ModelPart tailfin4;
    private final ModelPart tailfin3;
    private final ModelPart tailmembrane1;
    private final ModelPart topmem1;
    private final ModelPart topmem2;
    private final ModelPart topmem3;
    private final ModelPart topmem4;
    private final ModelPart neck1;
    private final ModelPart body;
    private final ModelPart chest1;
    private final ModelPart leftleg1;
    private final ModelPart mouth;
    private final ModelPart neck2;
    private final ModelPart head;
    private final ModelPart hammerhead;
    private final ModelPart chest;
    private final ModelPart neck3;
    private final ModelPart tail1;
    private final ModelPart rightleg1;
    private final ModelPart leftleg2;
    private final ModelPart rightleg2;
    private final ModelPart body2;
    private final ModelPart leftleg3;
    private final ModelPart rightleg3;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tailmembrane3;
    private final ModelPart leftwingfin2;
    private final ModelPart leftwingfin3;
    private final ModelPart leftwingfin4;
    private final ModelPart leftwingmembrane;
    private final ModelPart rightshoulder;
    private final ModelPart rightwingfin1;
    private final ModelPart rightwingfin2;
    private final ModelPart rightwingfin3;
    private final ModelPart rightwingfin4;
    private final ModelPart rightwingmembrane;
    private final ModelPart hammerhead2;

    /** Every part {@link #setupAnim} writes, reset first (R8). */
    private final ModelPart[] animated;

    /** {@code ModelCephadrome(float f1)} (:61-316). */
    public CephadromeModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.leftfoot = root.getChild(CephadromeGeometry.LEFTFOOT);
        this.butt = root.getChild(CephadromeGeometry.BUTT);
        this.rightfoot = root.getChild(CephadromeGeometry.RIGHTFOOT);
        this.topfin1 = root.getChild(CephadromeGeometry.TOPFIN1);
        this.topfin2 = root.getChild(CephadromeGeometry.TOPFIN2);
        this.topfin3 = root.getChild(CephadromeGeometry.TOPFIN3);
        this.topfin4 = root.getChild(CephadromeGeometry.TOPFIN4);
        this.leftshoulder = root.getChild(CephadromeGeometry.LEFTSHOULDER);
        this.lefwingfin1 = root.getChild(CephadromeGeometry.LEFWINGFIN1);
        this.tailfin1 = root.getChild(CephadromeGeometry.TAILFIN1);
        this.tailmembrane2 = root.getChild(CephadromeGeometry.TAILMEMBRANE2);
        this.tailfin2 = root.getChild(CephadromeGeometry.TAILFIN2);
        this.tailfin4 = root.getChild(CephadromeGeometry.TAILFIN4);
        this.tailfin3 = root.getChild(CephadromeGeometry.TAILFIN3);
        this.tailmembrane1 = root.getChild(CephadromeGeometry.TAILMEMBRANE1);
        this.topmem1 = root.getChild(CephadromeGeometry.TOPMEM1);
        this.topmem2 = root.getChild(CephadromeGeometry.TOPMEM2);
        this.topmem3 = root.getChild(CephadromeGeometry.TOPMEM3);
        this.topmem4 = root.getChild(CephadromeGeometry.TOPMEM4);
        this.neck1 = root.getChild(CephadromeGeometry.NECK1);
        this.body = root.getChild(CephadromeGeometry.BODY);
        this.chest1 = root.getChild(CephadromeGeometry.CHEST1);
        this.leftleg1 = root.getChild(CephadromeGeometry.LEFTLEG1);
        this.mouth = root.getChild(CephadromeGeometry.MOUTH);
        this.neck2 = root.getChild(CephadromeGeometry.NECK2);
        this.head = root.getChild(CephadromeGeometry.HEAD);
        this.hammerhead = root.getChild(CephadromeGeometry.HAMMERHEAD);
        this.chest = root.getChild(CephadromeGeometry.CHEST);
        this.neck3 = root.getChild(CephadromeGeometry.NECK3);
        this.tail1 = root.getChild(CephadromeGeometry.TAIL1);
        this.rightleg1 = root.getChild(CephadromeGeometry.RIGHTLEG1);
        this.leftleg2 = root.getChild(CephadromeGeometry.LEFTLEG2);
        this.rightleg2 = root.getChild(CephadromeGeometry.RIGHTLEG2);
        this.body2 = root.getChild(CephadromeGeometry.BODY2);
        this.leftleg3 = root.getChild(CephadromeGeometry.LEFTLEG3);
        this.rightleg3 = root.getChild(CephadromeGeometry.RIGHTLEG3);
        this.tail2 = root.getChild(CephadromeGeometry.TAIL2);
        this.tail3 = root.getChild(CephadromeGeometry.TAIL3);
        this.tailmembrane3 = root.getChild(CephadromeGeometry.TAILMEMBRANE3);
        this.leftwingfin2 = root.getChild(CephadromeGeometry.LEFTWINGFIN2);
        this.leftwingfin3 = root.getChild(CephadromeGeometry.LEFTWINGFIN3);
        this.leftwingfin4 = root.getChild(CephadromeGeometry.LEFTWINGFIN4);
        this.leftwingmembrane = root.getChild(CephadromeGeometry.LEFTWINGMEMBRANE);
        this.rightshoulder = root.getChild(CephadromeGeometry.RIGHTSHOULDER);
        this.rightwingfin1 = root.getChild(CephadromeGeometry.RIGHTWINGFIN1);
        this.rightwingfin2 = root.getChild(CephadromeGeometry.RIGHTWINGFIN2);
        this.rightwingfin3 = root.getChild(CephadromeGeometry.RIGHTWINGFIN3);
        this.rightwingfin4 = root.getChild(CephadromeGeometry.RIGHTWINGFIN4);
        this.rightwingmembrane = root.getChild(CephadromeGeometry.RIGHTWINGMEMBRANE);
        this.hammerhead2 = root.getChild(CephadromeGeometry.HAMMERHEAD2);
        this.animated = new ModelPart[] {
            this.rightleg1, this.rightleg2, this.rightleg3, this.rightfoot,
            this.leftleg1, this.leftleg2, this.leftleg3, this.leftfoot,
            this.lefwingfin1, this.leftwingfin2, this.leftwingfin3, this.leftwingfin4, this.leftwingmembrane,
            this.rightwingfin1, this.rightwingfin2, this.rightwingfin3, this.rightwingfin4, this.rightwingmembrane,
            this.topfin1, this.topmem1, this.topfin2, this.topmem2, this.topfin3, this.topmem3, this.topfin4, this.topmem4,
            this.tail1, this.tail2, this.tail3,
            this.tailfin1, this.tailfin2, this.tailfin3, this.tailfin4,
            this.tailmembrane1, this.tailmembrane2, this.tailmembrane3,
            this.neck3, this.neck2, this.neck1, this.head, this.hammerhead, this.hammerhead2, this.mouth
        };
    }

    /**
     * The writes of {@code render()} (:320-466), in the original order. {@code f} limb swing, {@code f1} limb swing
     * amount, {@code f2} age in ticks, {@code f3} head yaw.
     */
    @Override
    public void setupAnim(final Cephadrome e, final float f, final float f1, final float f2, float f3, final float f4) {
        for (final ModelPart part : this.animated) {
            part.resetPose();
        }
        RenderInfo r = null;
        float newangle = 0.0f;
        float lspeed = 0.0f;
        final float pi4 = 0.7853982f;
        float tailspeed = 0.76f;
        float tailamp = 0.1f;
        r = e.getRenderInfo();
        if (f1 > 0.001) {
            lspeed = (float) ((e.xo - e.getX()) * (e.xo - e.getX()) + (e.zo - e.getZ()) * (e.zo - e.getZ()));
            lspeed = (float) Math.sqrt(lspeed);
            newangle = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * lspeed * 0.4f;
            if (newangle > 0.5) {
                newangle = 0.75f;
            }
            if (newangle < -0.5) {
                newangle = -0.75f;
            }
        } else {
            newangle = 0.0f;
        }
        if (e.getActivity() != 0) {
            newangle = 1.0f;
            this.rightleg1.xRot = -0.58f + newangle;
            this.rightleg2.xRot = 0.98f + newangle;
            this.rightleg3.xRot = -0.52f + newangle;
            this.rightfoot.xRot = newangle;
            this.leftleg1.xRot = -0.58f + newangle;
            this.leftleg2.xRot = 0.98f + newangle;
            this.leftleg3.xRot = -0.52f + newangle;
            this.leftfoot.xRot = newangle;
        } else {
            this.rightleg1.xRot = -0.58f + newangle;
            this.rightleg2.xRot = 0.98f + newangle;
            this.rightleg3.xRot = -0.52f + newangle;
            this.rightfoot.xRot = newangle;
            this.leftleg1.xRot = -0.58f - newangle;
            this.leftleg2.xRot = 0.98f - newangle;
            this.leftleg3.xRot = -0.52f - newangle;
            this.leftfoot.xRot = -newangle;
        }
        if (e.getActivity() != 0) {
            newangle = Mth.cos(f2 * 0.55f * this.wingspeed) * 3.1415927f * 0.28f;
        } else if (e.getAttacking() == 0) {
            newangle = -0.85f + Mth.cos(f2 * 0.2f * this.wingspeed) * 3.1415927f * 0.028f;
        } else {
            newangle = -0.65f + Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.068f;
        }
        this.lefwingfin1.zRot = newangle;
        this.leftwingfin2.zRot = newangle;
        this.leftwingfin3.zRot = newangle;
        this.leftwingfin4.zRot = newangle;
        this.leftwingmembrane.zRot = newangle;
        this.rightwingfin1.zRot = -newangle;
        this.rightwingfin2.zRot = -newangle;
        this.rightwingfin3.zRot = -newangle;
        this.rightwingfin4.zRot = -newangle;
        this.rightwingmembrane.zRot = -newangle;
        newangle = Mth.cos(f2 * 0.15f * this.wingspeed) * 3.1415927f * 0.05f;
        this.topfin1.xRot = -1.85f - Math.abs(newangle);
        this.topmem1.xRot = -0.26f - Math.abs(newangle);
        this.topfin2.xRot = -2.07f - Math.abs(newangle / 2.0f);
        this.topmem2.xRot = -0.52f - Math.abs(newangle / 2.0f);
        this.topfin3.xRot = -2.42f - Math.abs(newangle / 4.0f);
        this.topmem3.xRot = -0.89f - Math.abs(newangle / 4.0f);
        this.topfin4.xRot = -2.63f - Math.abs(newangle / 8.0f);
        this.topmem4.xRot = -1.11f - Math.abs(newangle / 8.0f);
        if (e.getActivity() == 0 && e.getAttacking() == 0) {
            tailspeed = 0.22f;
            tailamp = 0.03f;
        }
        this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * 0.04f;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 13.0f;
        this.tail2.x = this.tail1.x + 1.5f + (float) Math.sin(this.tail1.yRot) * 13.0f;
        this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - pi4) * 3.1415927f * tailamp;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 13.0f;
        this.tail3.x = this.tail2.x - 0.5f + (float) Math.sin(this.tail2.yRot) * 13.0f;
        this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 2.0f * pi4) * 3.1415927f * tailamp;
        this.tailfin1.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 10.0f;
        this.tailfin1.x = this.tail3.x - 1.0f + (float) Math.sin(this.tail3.yRot) * 10.0f;
        this.tailfin1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0f * pi4) * 3.1415927f * tailamp;
        this.tailfin2.z = this.tailfin1.z;
        this.tailfin2.x = this.tailfin1.x;
        this.tailfin2.yRot = this.tailfin1.yRot;
        this.tailfin3.z = this.tailfin1.z;
        this.tailfin3.x = this.tailfin1.x;
        this.tailfin3.yRot = this.tailfin1.yRot;
        this.tailfin4.z = this.tailfin1.z;
        this.tailfin4.x = this.tailfin1.x;
        this.tailfin4.yRot = this.tailfin1.yRot;
        this.tailmembrane1.z = this.tailfin1.z;
        this.tailmembrane1.x = this.tailfin1.x;
        this.tailmembrane1.yRot = this.tailfin1.yRot;
        this.tailmembrane2.z = this.tailfin1.z;
        this.tailmembrane2.x = this.tailfin1.x;
        this.tailmembrane2.yRot = this.tailfin1.yRot;
        this.tailmembrane3.z = this.tailfin1.z;
        this.tailmembrane3.x = this.tailfin1.x;
        this.tailmembrane3.yRot = this.tailfin1.yRot;
        if (e.getActivity() == 1) {
            f3 = (e.yRotO - e.getYRot()) * 10.0f;
            f3 = -f3;
            final RenderInfo renderInfo = r;
            renderInfo.rf1 += (f3 - r.rf1) / 50.0f;
            if (r.rf1 > 50.0f) {
                r.rf1 = 50.0f;
            }
            if (r.rf1 < -50.0f) {
                r.rf1 = -50.0f;
            }
            f3 = r.rf1;
        } else {
            f3 /= 2.0f;
        }
        this.neck3.yRot = (float) Math.toRadians(f3) * 0.125f;
        this.neck2.z = this.neck3.z - (float) Math.cos(this.neck3.yRot) * 14.0f;
        this.neck2.x = this.neck3.x + 0.5f - (float) Math.sin(this.neck3.yRot) * 14.0f;
        this.neck2.yRot = (float) Math.toRadians(f3) * 0.25f;
        this.neck1.z = this.neck2.z - (float) Math.cos(this.neck2.yRot) * 14.0f;
        this.neck1.x = this.neck2.x + 0.5f - (float) Math.sin(this.neck2.yRot) * 14.0f;
        this.neck1.yRot = (float) Math.toRadians(f3) * 0.5f;
        this.head.z = this.neck1.z - (float) Math.cos(this.neck1.yRot) * 8.0f;
        this.head.x = this.neck1.x - (float) Math.sin(this.neck1.yRot) * 8.0f;
        this.head.yRot = (float) Math.toRadians(f3) * 0.75f;
        this.hammerhead.z = this.head.z;
        this.hammerhead.x = this.head.x;
        this.hammerhead.yRot = this.head.yRot;
        this.hammerhead2.z = this.head.z;
        this.hammerhead2.x = this.head.x;
        this.hammerhead2.yRot = this.head.yRot;
        this.mouth.z = this.head.z;
        this.mouth.x = this.head.x;
        this.mouth.yRot = this.head.yRot;
        newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.14f;
        if (e.getAttacking() != 0) {
            this.mouth.xRot = -0.61f + newangle;
        } else {
            this.mouth.xRot = -0.87f;
        }
        e.setRenderInfo(r);
    }

    /** Draw order of {@code render()} (:467-516). */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.leftfoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.butt.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightfoot.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topfin3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topfin4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftshoulder.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lefwingfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailmembrane2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfin4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailfin3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailmembrane1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topmem1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topmem2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topmem3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.topmem4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.chest1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.mouth.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.hammerhead.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.chest.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tailmembrane3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwingfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwingfin3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwingfin4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftwingmembrane.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightshoulder.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwingfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwingfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwingfin3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwingfin4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightwingmembrane.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.hammerhead2.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
