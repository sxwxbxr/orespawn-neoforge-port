package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.KrakenGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.boss.kraken.Kraken;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelKraken} (ModelKraken.java:8-1486): 111 boxes on a 512x512 texture, geometry
 * {@link KrakenGeometry} (the constructor's final pivots, after its {@code rotationPointZ += 90} and
 * {@code rotationPointY += 30} passes, :683-1126). {@code wingspeed} is the constructor argument, 1.0
 * (ClientProxyOreSpawn.java:74, manifest {@code model_args}).
 *
 * <p>{@code render()} (:1129-1364) waves the two fins, swings the six tentacle chains ({@code dangle_tentacle},
 * :1376-1485: each segment's pivot sits 30 units along the previous segment, the angles are phase-shifted cosines),
 * hangs the suction cups on the tips of the two long arms, opens the beak and the teeth, and draws every part inside
 * {@code glRotatef(90, 1, 0, 0)} (:1248-1251, :1363) - the whole squid is turned about X.
 *
 * <p>PORT (R8): all angle and pivot writes are in {@link #setupAnim}, with {@code resetPose()} first; the rotation
 * and the draw calls, in the original order, are in {@link #renderToBuffer}. The {@code RenderInfo} the original kept
 * on the entity ({@code e.getRenderInfo()} / {@code setRenderInfo}, :1151, :1243) is client-only scratch space and
 * lives here per entity, weakly keyed (AlienModel precedent); a new record starts zeroed, as {@code entityInit} did.
 * Its random draws use the entity's (client) level random, as {@code e.worldObj.rand} did. Only {@code ri1} 1 and 3
 * open the beak; {@code ri2} is written and never read.
 */
public class KrakenModel extends EntityModel<Kraken> {

    /** Register with {@code KrakenGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_kraken"), "main");

    /** {@code ModelKraken(float f1)}: {@code wingspeed = f1} (:124-125). */
    private final float wingspeed;
    /** {@code Kraken.renderdata} per entity (see the class Javadoc). */
    private final Map<Kraken, RenderInfo> renderInfos = new WeakHashMap<>();

    private final ModelPart Lefteye;
    private final ModelPart Backbody;
    private final ModelPart Centerbody;
    private final ModelPart Head;
    private final ModelPart Sucktioncupleft;
    private final ModelPart Finright;
    private final ModelPart Tailbase1;
    private final ModelPart Tail2;
    private final ModelPart Tailtip;
    private final ModelPart Finleft;
    private final ModelPart Frontbody;
    private final ModelPart Mouth1;
    private final ModelPart Tent54;
    private final ModelPart Tent62;
    private final ModelPart Tent63;
    private final ModelPart Tent64;
    private final ModelPart Tent58;
    private final ModelPart Tent66;
    private final ModelPart Tent67;
    private final ModelPart Tent68;
    private final ModelPart Tent28;
    private final ModelPart Tent51;
    private final ModelPart Tent52;
    private final ModelPart Tent53;
    private final ModelPart Tent65;
    private final ModelPart Tent55;
    private final ModelPart Tent56;
    private final ModelPart Tent57;
    private final ModelPart Sucktioncupright;
    private final ModelPart Righteye;
    private final ModelPart Mouth2;
    private final ModelPart Mouth3;
    private final ModelPart Mouth4;
    private final ModelPart Mouth5;
    private final ModelPart Mouth6;
    private final ModelPart Mouth7;
    private final ModelPart Mouth8;
    private final ModelPart Tent61;
    private final ModelPart Tent38;
    private final ModelPart Tent22;
    private final ModelPart Tent23;
    private final ModelPart Tent24;
    private final ModelPart Tent25;
    private final ModelPart Tent26;
    private final ModelPart Tent27;
    private final ModelPart Tooth1;
    private final ModelPart Tent48;
    private final ModelPart Tent32;
    private final ModelPart Tent33;
    private final ModelPart Tent34;
    private final ModelPart Tent35;
    private final ModelPart Tent36;
    private final ModelPart Tent37;
    private final ModelPart Jet;
    private final ModelPart Tent41;
    private final ModelPart Tent42;
    private final ModelPart Tent43;
    private final ModelPart Tent44;
    private final ModelPart Tent45;
    private final ModelPart Tent46;
    private final ModelPart Tent47;
    private final ModelPart Tent21;
    private final ModelPart Tent11;
    private final ModelPart Tent12;
    private final ModelPart Tent13;
    private final ModelPart Tent14;
    private final ModelPart Tent15;
    private final ModelPart Tent16;
    private final ModelPart Tent31;
    private final ModelPart Tent18;
    private final ModelPart Tooth2;
    private final ModelPart Tooth3;
    private final ModelPart Tooth4;
    private final ModelPart Tooth5;
    private final ModelPart Tooth6;
    private final ModelPart Tooth7;
    private final ModelPart Tooth8;
    private final ModelPart Tooth9;
    private final ModelPart Tooth10;
    private final ModelPart Tooth11;
    private final ModelPart Tooth12;
    private final ModelPart Tooth13;
    private final ModelPart Tooth14;
    private final ModelPart Tooth15;
    private final ModelPart Tooth16;
    private final ModelPart Tooth17;
    private final ModelPart Tooth18;
    private final ModelPart Tooth19;
    private final ModelPart Tooth20;
    private final ModelPart Tooth21;
    private final ModelPart Tooth22;
    private final ModelPart Tooth23;
    private final ModelPart Tooth24;
    private final ModelPart Tooth25;
    private final ModelPart Tooth26;
    private final ModelPart Tooth27;
    private final ModelPart Tooth28;
    private final ModelPart Tooth29;
    private final ModelPart Tooth30;
    private final ModelPart Tooth31;
    private final ModelPart Tooth32;
    private final ModelPart Tooth33;
    private final ModelPart Tooth34;
    private final ModelPart Tooth35;
    private final ModelPart Tooth36;
    private final ModelPart Tooth37;
    private final ModelPart Tooth38;
    private final ModelPart Tooth39;
    private final ModelPart Tooth40;
    private final ModelPart Tooth41;
    private final ModelPart Tent17;
    /** Every part in the order {@code render()} draws it (:1252-1362). */
    private final ModelPart[] drawOrder;

    /** {@code ModelKraken(float f1)} (:123-1127). */
    public KrakenModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.Lefteye = root.getChild(KrakenGeometry.LEFTEYE);
        this.Backbody = root.getChild(KrakenGeometry.BACKBODY);
        this.Centerbody = root.getChild(KrakenGeometry.CENTERBODY);
        this.Head = root.getChild(KrakenGeometry.HEAD);
        this.Sucktioncupleft = root.getChild(KrakenGeometry.SUCKTIONCUPLEFT);
        this.Finright = root.getChild(KrakenGeometry.FINRIGHT);
        this.Tailbase1 = root.getChild(KrakenGeometry.TAILBASE1);
        this.Tail2 = root.getChild(KrakenGeometry.TAIL2);
        this.Tailtip = root.getChild(KrakenGeometry.TAILTIP);
        this.Finleft = root.getChild(KrakenGeometry.FINLEFT);
        this.Frontbody = root.getChild(KrakenGeometry.FRONTBODY);
        this.Mouth1 = root.getChild(KrakenGeometry.MOUTH1);
        this.Tent54 = root.getChild(KrakenGeometry.TENT54);
        this.Tent62 = root.getChild(KrakenGeometry.TENT62);
        this.Tent63 = root.getChild(KrakenGeometry.TENT63);
        this.Tent64 = root.getChild(KrakenGeometry.TENT64);
        this.Tent58 = root.getChild(KrakenGeometry.TENT58);
        this.Tent66 = root.getChild(KrakenGeometry.TENT66);
        this.Tent67 = root.getChild(KrakenGeometry.TENT67);
        this.Tent68 = root.getChild(KrakenGeometry.TENT68);
        this.Tent28 = root.getChild(KrakenGeometry.TENT28);
        this.Tent51 = root.getChild(KrakenGeometry.TENT51);
        this.Tent52 = root.getChild(KrakenGeometry.TENT52);
        this.Tent53 = root.getChild(KrakenGeometry.TENT53);
        this.Tent65 = root.getChild(KrakenGeometry.TENT65);
        this.Tent55 = root.getChild(KrakenGeometry.TENT55);
        this.Tent56 = root.getChild(KrakenGeometry.TENT56);
        this.Tent57 = root.getChild(KrakenGeometry.TENT57);
        this.Sucktioncupright = root.getChild(KrakenGeometry.SUCKTIONCUPRIGHT);
        this.Righteye = root.getChild(KrakenGeometry.RIGHTEYE);
        this.Mouth2 = root.getChild(KrakenGeometry.MOUTH2);
        this.Mouth3 = root.getChild(KrakenGeometry.MOUTH3);
        this.Mouth4 = root.getChild(KrakenGeometry.MOUTH4);
        this.Mouth5 = root.getChild(KrakenGeometry.MOUTH5);
        this.Mouth6 = root.getChild(KrakenGeometry.MOUTH6);
        this.Mouth7 = root.getChild(KrakenGeometry.MOUTH7);
        this.Mouth8 = root.getChild(KrakenGeometry.MOUTH8);
        this.Tent61 = root.getChild(KrakenGeometry.TENT61);
        this.Tent38 = root.getChild(KrakenGeometry.TENT38);
        this.Tent22 = root.getChild(KrakenGeometry.TENT22);
        this.Tent23 = root.getChild(KrakenGeometry.TENT23);
        this.Tent24 = root.getChild(KrakenGeometry.TENT24);
        this.Tent25 = root.getChild(KrakenGeometry.TENT25);
        this.Tent26 = root.getChild(KrakenGeometry.TENT26);
        this.Tent27 = root.getChild(KrakenGeometry.TENT27);
        this.Tooth1 = root.getChild(KrakenGeometry.TOOTH1);
        this.Tent48 = root.getChild(KrakenGeometry.TENT48);
        this.Tent32 = root.getChild(KrakenGeometry.TENT32);
        this.Tent33 = root.getChild(KrakenGeometry.TENT33);
        this.Tent34 = root.getChild(KrakenGeometry.TENT34);
        this.Tent35 = root.getChild(KrakenGeometry.TENT35);
        this.Tent36 = root.getChild(KrakenGeometry.TENT36);
        this.Tent37 = root.getChild(KrakenGeometry.TENT37);
        this.Jet = root.getChild(KrakenGeometry.JET);
        this.Tent41 = root.getChild(KrakenGeometry.TENT41);
        this.Tent42 = root.getChild(KrakenGeometry.TENT42);
        this.Tent43 = root.getChild(KrakenGeometry.TENT43);
        this.Tent44 = root.getChild(KrakenGeometry.TENT44);
        this.Tent45 = root.getChild(KrakenGeometry.TENT45);
        this.Tent46 = root.getChild(KrakenGeometry.TENT46);
        this.Tent47 = root.getChild(KrakenGeometry.TENT47);
        this.Tent21 = root.getChild(KrakenGeometry.TENT21);
        this.Tent11 = root.getChild(KrakenGeometry.TENT11);
        this.Tent12 = root.getChild(KrakenGeometry.TENT12);
        this.Tent13 = root.getChild(KrakenGeometry.TENT13);
        this.Tent14 = root.getChild(KrakenGeometry.TENT14);
        this.Tent15 = root.getChild(KrakenGeometry.TENT15);
        this.Tent16 = root.getChild(KrakenGeometry.TENT16);
        this.Tent31 = root.getChild(KrakenGeometry.TENT31);
        this.Tent18 = root.getChild(KrakenGeometry.TENT18);
        this.Tooth2 = root.getChild(KrakenGeometry.TOOTH2);
        this.Tooth3 = root.getChild(KrakenGeometry.TOOTH3);
        this.Tooth4 = root.getChild(KrakenGeometry.TOOTH4);
        this.Tooth5 = root.getChild(KrakenGeometry.TOOTH5);
        this.Tooth6 = root.getChild(KrakenGeometry.TOOTH6);
        this.Tooth7 = root.getChild(KrakenGeometry.TOOTH7);
        this.Tooth8 = root.getChild(KrakenGeometry.TOOTH8);
        this.Tooth9 = root.getChild(KrakenGeometry.TOOTH9);
        this.Tooth10 = root.getChild(KrakenGeometry.TOOTH10);
        this.Tooth11 = root.getChild(KrakenGeometry.TOOTH11);
        this.Tooth12 = root.getChild(KrakenGeometry.TOOTH12);
        this.Tooth13 = root.getChild(KrakenGeometry.TOOTH13);
        this.Tooth14 = root.getChild(KrakenGeometry.TOOTH14);
        this.Tooth15 = root.getChild(KrakenGeometry.TOOTH15);
        this.Tooth16 = root.getChild(KrakenGeometry.TOOTH16);
        this.Tooth17 = root.getChild(KrakenGeometry.TOOTH17);
        this.Tooth18 = root.getChild(KrakenGeometry.TOOTH18);
        this.Tooth19 = root.getChild(KrakenGeometry.TOOTH19);
        this.Tooth20 = root.getChild(KrakenGeometry.TOOTH20);
        this.Tooth21 = root.getChild(KrakenGeometry.TOOTH21);
        this.Tooth22 = root.getChild(KrakenGeometry.TOOTH22);
        this.Tooth23 = root.getChild(KrakenGeometry.TOOTH23);
        this.Tooth24 = root.getChild(KrakenGeometry.TOOTH24);
        this.Tooth25 = root.getChild(KrakenGeometry.TOOTH25);
        this.Tooth26 = root.getChild(KrakenGeometry.TOOTH26);
        this.Tooth27 = root.getChild(KrakenGeometry.TOOTH27);
        this.Tooth28 = root.getChild(KrakenGeometry.TOOTH28);
        this.Tooth29 = root.getChild(KrakenGeometry.TOOTH29);
        this.Tooth30 = root.getChild(KrakenGeometry.TOOTH30);
        this.Tooth31 = root.getChild(KrakenGeometry.TOOTH31);
        this.Tooth32 = root.getChild(KrakenGeometry.TOOTH32);
        this.Tooth33 = root.getChild(KrakenGeometry.TOOTH33);
        this.Tooth34 = root.getChild(KrakenGeometry.TOOTH34);
        this.Tooth35 = root.getChild(KrakenGeometry.TOOTH35);
        this.Tooth36 = root.getChild(KrakenGeometry.TOOTH36);
        this.Tooth37 = root.getChild(KrakenGeometry.TOOTH37);
        this.Tooth38 = root.getChild(KrakenGeometry.TOOTH38);
        this.Tooth39 = root.getChild(KrakenGeometry.TOOTH39);
        this.Tooth40 = root.getChild(KrakenGeometry.TOOTH40);
        this.Tooth41 = root.getChild(KrakenGeometry.TOOTH41);
        this.Tent17 = root.getChild(KrakenGeometry.TENT17);
        this.drawOrder = new ModelPart[] {
                this.Lefteye, this.Backbody, this.Centerbody, this.Head, this.Sucktioncupleft, this.Finright, this.Tailbase1, this.Tail2,
                this.Tailtip, this.Finleft, this.Frontbody, this.Mouth1, this.Tent54, this.Tent62, this.Tent63, this.Tent64,
                this.Tent58, this.Tent66, this.Tent67, this.Tent68, this.Tent28, this.Tent51, this.Tent52, this.Tent53,
                this.Tent65, this.Tent55, this.Tent56, this.Tent57, this.Sucktioncupright, this.Righteye, this.Mouth2, this.Mouth3,
                this.Mouth4, this.Mouth5, this.Mouth6, this.Mouth7, this.Mouth8, this.Tent61, this.Tent38, this.Tent22,
                this.Tent23, this.Tent24, this.Tent25, this.Tent26, this.Tent27, this.Tooth1, this.Tent48, this.Tent32,
                this.Tent33, this.Tent34, this.Tent35, this.Tent36, this.Tent37, this.Jet, this.Tent41, this.Tent42,
                this.Tent43, this.Tent44, this.Tent45, this.Tent46, this.Tent47, this.Tent21, this.Tent11, this.Tent12,
                this.Tent13, this.Tent14, this.Tent15, this.Tent16, this.Tent31, this.Tent18, this.Tooth2, this.Tooth3,
                this.Tooth4, this.Tooth5, this.Tooth6, this.Tooth7, this.Tooth8, this.Tooth9, this.Tooth10, this.Tooth11,
                this.Tooth12, this.Tooth13, this.Tooth14, this.Tooth15, this.Tooth16, this.Tooth17, this.Tooth18, this.Tooth19,
                this.Tooth20, this.Tooth21, this.Tooth22, this.Tooth23, this.Tooth24, this.Tooth25, this.Tooth26, this.Tooth27,
                this.Tooth28, this.Tooth29, this.Tooth30, this.Tooth31, this.Tooth32, this.Tooth33, this.Tooth34, this.Tooth35,
                this.Tooth36, this.Tooth37, this.Tooth38, this.Tooth39, this.Tooth40, this.Tooth41, this.Tent17 };
    }

    /**
     * The writes of {@code render()} (:1134-1247) in their order; {@code f2} is {@code ageInTicks}.
     * {@code setRotationAngles} (:1372-1374) only called the empty super method.
     */
    @Override
    public void setupAnim(final Kraken e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.drawOrder) {
            part.resetPose();
        }
        float newangle = 0.0f;
        float nextangle = 0.0f;
        this.Finright.zRot = Mth.cos(f2 * 0.43f * this.wingspeed) * 3.1415927f * 0.15f;
        this.Finleft.zRot = Mth.cos(f2 * 0.32f * this.wingspeed) * 3.1415927f * 0.14f;
        this.dangle_tentacle(f2, 5, e.getAttacking(), this.Tent51, this.Tent52, this.Tent53, this.Tent54, this.Tent55, this.Tent56, this.Tent57, this.Tent58);
        this.dangle_tentacle(f2, 6, e.getAttacking(), this.Tent61, this.Tent62, this.Tent63, this.Tent64, this.Tent65, this.Tent66, this.Tent67, this.Tent68);
        this.Sucktioncupleft.y = this.Tent58.y + (float) Math.sin(this.Tent58.xRot) * 30.0f * (float) Math.cos(this.Tent58.yRot);
        this.Sucktioncupleft.z = this.Tent58.z - (float) Math.cos(this.Tent58.xRot) * 30.0f * (float) Math.cos(this.Tent58.yRot);
        this.Sucktioncupleft.x = this.Tent58.x - (float) Math.sin(this.Tent58.yRot) * 30.0f * (float) Math.cos(this.Tent58.xRot);
        this.Sucktioncupleft.xRot = this.Tent58.xRot;
        this.Sucktioncupleft.yRot = this.Tent58.yRot;
        this.Sucktioncupright.y = this.Tent68.y + (float) Math.sin(this.Tent68.xRot) * 30.0f * (float) Math.cos(this.Tent68.yRot);
        this.Sucktioncupright.z = this.Tent68.z - (float) Math.cos(this.Tent68.xRot) * 30.0f * (float) Math.cos(this.Tent68.yRot);
        this.Sucktioncupright.x = this.Tent68.x - (float) Math.sin(this.Tent68.yRot) * 30.0f * (float) Math.cos(this.Tent68.xRot);
        this.Sucktioncupright.xRot = this.Tent68.xRot;
        this.Sucktioncupright.yRot = this.Tent68.yRot;
        final RenderInfo r = this.renderInfos.computeIfAbsent(e, k -> new RenderInfo());
        newangle = Mth.cos(f2 * 0.66f) * 3.1415927f * 0.15f;
        nextangle = Mth.cos((f2 + 0.1f) * 0.66f) * 3.1415927f * 0.15f;
        if (nextangle > 0.0f && newangle < 0.0f) {
            r.ri1 = 0;
            if (e.getAttacking() == 0) {
                r.ri1 = e.level().random.nextInt(10);
                r.ri2 = e.level().random.nextInt(15);
            } else {
                r.ri1 = e.level().random.nextInt(4);
                r.ri2 = e.level().random.nextInt(3);
            }
        }
        if (r.ri1 == 1 || r.ri1 == 3) {
            newangle = Mth.cos(f2 * 0.5f * this.wingspeed) * 3.1415927f * 0.015f;
        } else {
            newangle = 0.0f;
        }
        this.Mouth1.xRot = -0.38f + newangle;
        this.Mouth1.yRot = -0.38f + newangle;
        this.Mouth2.xRot = -0.38f + newangle;
        this.Mouth2.yRot = 0.38f - newangle;
        this.Mouth3.xRot = 0.38f - newangle;
        this.Mouth3.yRot = 0.38f - newangle;
        this.Mouth5.xRot = 0.38f - newangle;
        this.Mouth5.yRot = -0.38f + newangle;
        this.Mouth4.xRot = 0.38f - newangle;
        this.Mouth6.yRot = -0.38f + newangle;
        this.Mouth7.yRot = 0.38f - newangle;
        this.Mouth8.xRot = -0.38f + newangle;
        newangle *= 7.0f;
        this.Tooth2.xRot = -0.35f - newangle;
        this.Tooth3.xRot = -0.34f - newangle;
        this.Tooth4.xRot = -0.33f - newangle;
        this.Tooth5.xRot = -0.36f - newangle;
        this.Tooth6.xRot = -0.32f - newangle;
        this.Tooth11.yRot = 0.35f + newangle;
        this.Tooth12.yRot = 0.37f + newangle;
        this.Tooth13.yRot = 0.33f + newangle;
        this.Tooth14.yRot = 0.34f + newangle;
        this.Tooth15.yRot = 0.36f + newangle;
        this.Tooth16.yRot = 0.35f + newangle;
        this.Tooth17.yRot = 0.32f + newangle;
        this.Tooth22.xRot = 0.31f + newangle;
        this.Tooth23.xRot = 0.37f + newangle;
        this.Tooth24.xRot = 0.33f + newangle;
        this.Tooth25.xRot = 0.34f + newangle;
        this.Tooth26.xRot = 0.36f + newangle;
        this.Tooth27.xRot = 0.35f + newangle;
        this.Tooth31.yRot = -0.35f - newangle;
        this.Tooth32.yRot = -0.37f - newangle;
        this.Tooth33.yRot = -0.33f - newangle;
        this.Tooth34.yRot = -0.34f - newangle;
        this.Tooth35.yRot = -0.36f - newangle;
        this.Tooth36.yRot = -0.35f - newangle;
        this.Tooth37.yRot = -0.32f - newangle;
        this.Tooth7.xRot = -0.35f - newangle;
        this.Tooth7.yRot = 0.33f + newangle;
        this.Tooth8.xRot = -0.31f - newangle;
        this.Tooth8.yRot = 0.37f + newangle;
        this.Tooth9.xRot = -0.32f - newangle;
        this.Tooth9.yRot = 0.3f + newangle;
        this.Tooth10.xRot = -0.33f - newangle;
        this.Tooth10.yRot = 0.33f + newangle;
        this.Tooth18.xRot = 0.35f + newangle;
        this.Tooth18.yRot = 0.33f + newangle;
        this.Tooth19.xRot = 0.31f + newangle;
        this.Tooth19.yRot = 0.37f + newangle;
        this.Tooth20.xRot = 0.37f + newangle;
        this.Tooth20.yRot = 0.37f + newangle;
        this.Tooth21.xRot = 0.3f + newangle;
        this.Tooth21.yRot = 0.3f + newangle;
        this.Tooth28.xRot = 0.37f + newangle;
        this.Tooth28.yRot = -0.3f - newangle;
        this.Tooth29.xRot = 0.33f + newangle;
        this.Tooth29.yRot = -0.32f - newangle;
        this.Tooth30.xRot = 0.3f + newangle;
        this.Tooth30.yRot = -0.37f - newangle;
        // Tooth31 is written a second time here in the original (:1231-1232); the second yaw wins.
        this.Tooth31.xRot = 0.37f + newangle;
        this.Tooth31.yRot = -0.3f - newangle;
        this.Tooth38.xRot = -0.34f - newangle;
        this.Tooth38.yRot = -0.33f - newangle;
        this.Tooth39.xRot = -0.35f - newangle;
        this.Tooth39.yRot = -0.37f - newangle;
        this.Tooth40.xRot = -0.39f - newangle;
        this.Tooth40.yRot = -0.33f - newangle;
        this.Tooth41.xRot = -0.34f - newangle;
        this.Tooth41.yRot = -0.36f - newangle;
        this.Tooth1.xRot = -0.35f - newangle;
        this.Tooth1.yRot = -0.32f - newangle;
        // e.setRenderInfo(r) (:1243): the record is already the stored one.
        this.dangle_tentacle(f2, 1, 0, this.Tent11, this.Tent12, this.Tent13, this.Tent14, this.Tent15, this.Tent16, this.Tent17, this.Tent18);
        this.dangle_tentacle(f2, 2, 0, this.Tent21, this.Tent22, this.Tent23, this.Tent24, this.Tent25, this.Tent26, this.Tent27, this.Tent28);
        this.dangle_tentacle(f2, 3, 0, this.Tent31, this.Tent32, this.Tent33, this.Tent34, this.Tent35, this.Tent36, this.Tent37, this.Tent38);
        this.dangle_tentacle(f2, 4, 0, this.Tent41, this.Tent42, this.Tent43, this.Tent44, this.Tent45, this.Tent46, this.Tent47, this.Tent48);
    }

    /** The draw calls of {@code render()} (:1248-1363): {@code glRotatef(90, 1, 0, 0)}, then all 111 parts in order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        for (final ModelPart part : this.drawOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        poseStack.popPose();
    }

    /**
     * {@code dangle_tentacle} (:1376-1485): {@code dir} 1-4 are the short arms (own speeds, x or y offset), 5 and 6 the
     * long arms (speed 0.2, turned by -0.25, {@code s = 1} for 6), which lash faster and narrower while attacking.
     */
    private void dangle_tentacle(final float f2, final int dir, final int att, final ModelPart p1, final ModelPart p2, final ModelPart p3,
                                 final ModelPart p4, final ModelPart p5, final ModelPart p6, final ModelPart p7, final ModelPart p8) {
        final float pi4 = 0.314159f;
        final int dist = 30;
        float differ = 0.1f;
        float xoff = 0.0f;
        float ydiffer = 0.1f;
        float yoff = 0.0f;
        float s = -1.0f;
        float amp = 0.1f;
        if (dir == 1) {
            differ = 0.101f;
        }
        if (dir == 2) {
            differ = 0.097f;
        }
        if (dir == 3) {
            differ = 0.093f;
        }
        if (dir == 4) {
            differ = 0.087f;
        }
        if (dir == 1) {
            ydiffer = 0.102f;
        }
        if (dir == 2) {
            ydiffer = 0.098f;
        }
        if (dir == 3) {
            ydiffer = 0.092f;
        }
        if (dir == 4) {
            ydiffer = 0.088f;
        }
        if (dir == 2) {
            xoff = 0.26f;
        }
        if (dir == 3) {
            xoff = 0.26f;
        }
        if (dir == 1) {
            yoff = 0.44f;
        }
        if (dir == 4) {
            yoff = -0.44f;
        }
        if (dir == 5) {
            differ = 0.2f;
        }
        if (dir == 6) {
            differ = 0.2f;
        }
        if (dir == 5) {
            xoff = -0.25f;
        }
        if (dir == 6) {
            xoff = -0.25f;
        }
        if (dir == 6) {
            s = 1.0f;
        }
        if (att != 0) {
            if (dir == 5) {
                differ = 0.5f;
                amp = 0.03f;
                xoff = 0.0f;
            }
            if (dir == 6) {
                differ = 0.5f;
                amp = 0.03f;
                xoff = 0.0f;
            }
        }
        p1.xRot = xoff + s * Mth.cos(f2 * differ * this.wingspeed) * 3.1415927f * amp;
        p1.yRot = yoff - Mth.cos(f2 * ydiffer * this.wingspeed) * 3.1415927f * amp;
        follow(p1, p2, dist);
        p2.xRot = xoff / 2.0f + s * Mth.cos(f2 * differ * this.wingspeed - pi4) * 3.1415927f * amp;
        p2.yRot = yoff / 2.0f - Mth.cos(f2 * ydiffer * this.wingspeed - pi4) * 3.1415927f * amp;
        follow(p2, p3, dist);
        p3.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 2.0f * pi4) * 3.1415927f * amp;
        p3.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 2.0f * pi4) * 3.1415927f * amp;
        follow(p3, p4, dist);
        p4.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 3.0f * pi4) * 3.1415927f * amp;
        p4.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 3.0f * pi4) * 3.1415927f * amp;
        follow(p4, p5, dist);
        p5.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 4.0f * pi4) * 3.1415927f * amp;
        p5.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 4.0f * pi4) * 3.1415927f * amp;
        follow(p5, p6, dist);
        p6.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 5.0f * pi4) * 3.1415927f * amp;
        p6.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 5.0f * pi4) * 3.1415927f * amp;
        follow(p6, p7, dist);
        p7.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 6.0f * pi4) * 3.1415927f * amp;
        p7.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 6.0f * pi4) * 3.1415927f * amp;
        follow(p7, p8, dist);
        p8.xRot = s * Mth.cos(f2 * differ * this.wingspeed - 7.0f * pi4) * 3.1415927f * amp;
        p8.yRot = -Mth.cos(f2 * ydiffer * this.wingspeed - 7.0f * pi4) * 3.1415927f * amp;
    }

    /**
     * The three pivot lines {@code dangle_tentacle} repeats between two segments (:1450-1452 and six copies): the next
     * pivot sits {@code dist} units along the previous segment, written in the original order y, z, x.
     */
    private static void follow(final ModelPart prev, final ModelPart next, final int dist) {
        next.y = prev.y + (float) Math.sin(prev.xRot) * dist * (float) Math.cos(prev.yRot);
        next.z = prev.z - (float) Math.cos(prev.xRot) * dist * (float) Math.cos(prev.yRot);
        next.x = prev.x - (float) Math.sin(prev.yRot) * dist * (float) Math.cos(prev.xRot);
    }
}
