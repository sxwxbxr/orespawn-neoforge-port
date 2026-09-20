package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.ThePrinceTeenGeometry;
import com.swbr.orespawn.client.renderer.RenderInfo;
import com.swbr.orespawn.entity.boss.prince.ThePrinceTeen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelThePrinceTeen} (ModelThePrinceTeen.java:9-896): 71 boxes, 512x256 texture,
 * geometry from the generated {@link ThePrinceTeenGeometry}. The animation is {@code render()} (:445-806):
 * <ul>
 *   <li>two two-link wings with membranes flap slowly on the ground, harder in flight and while attacking; the outer
 *       bone hangs on the tip of the inner one (22 px);</li>
 *   <li>the three-link legs walk (quarter-phase knees), tuck back in flight, and curl their claws;</li>
 *   <li>a five-link tail with three spikes wags, still while sitting, faster while attacking;</li>
 *   <li>three three-link necks stretch with the synced {@code head1ext..head3ext}, the heads share the head yaw and
 *       pitch unevenly; in flight the yaw is a smoothed turn rate kept in the entity's {@link RenderInfo}
 *       ({@code rf1}); the jaws chew, faster while attacking.</li>
 * </ul>
 * Reads {@link ThePrinceTeen#getActivity()}, {@link ThePrinceTeen#getAttacking()}, the head extensions (all synced)
 * and {@link ThePrinceTeen#isSitting()}.
 *
 * <p>Rest pose every frame ({@code resetPose}, R8): every written value depends only on rest values or on values
 * written earlier in the same frame. The {@code rf1} smoothing runs once per rendered frame, as in 1.7.10
 * (frame-rate dependent in both). {@code f3} was unwrapped head yaw; 1.21.1 passes it wrapped to [-180, 180)
 * (docs/research/06-models-design.md), the same value for every normal pose.
 *
 * <p>The membrane pass (:870-884, {@code GL_BLEND} with {@code glColor4f(0.75, 0.75, 0.75, 0.55)}) is not drawn by
 * {@link #renderToBuffer}: the renderer's translucent layer calls {@link #renderWings} with its own buffer (R8).
 */
public class ThePrinceTeenModel extends EntityModel<ThePrinceTeen> {

    /** Register with {@code ThePrinceTeenGeometry::createBodyLayer}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_young_prince"), "main");

    /** {@code ModelThePrinceTeen(float f1)}: {@code wingspeed = f1}; ClientProxyOreSpawn passes 0.65 (manifest). */
    private final float wingspeed;
    private final ModelPart[] parts;
    private final ModelPart body;
    private final ModelPart leftleg1;
    private final ModelPart tail1;
    private final ModelPart leftleg2;
    private final ModelPart body2;
    private final ModelPart leftleg3;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart lclaw2;
    private final ModelPart lclaw4;
    private final ModelPart lclaw5;
    private final ModelPart lclaw6;
    private final ModelPart lclaw7;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart neck1;
    private final ModelPart neck3;
    private final ModelPart head3;
    private final ModelPart jaw1;
    private final ModelPart jaw5;
    private final ModelPart head7;
    private final ModelPart rightleg1;
    private final ModelPart rightleg2;
    private final ModelPart rightleg3;
    private final ModelPart rclaw2;
    private final ModelPart rclaw4;
    private final ModelPart rclaw5;
    private final ModelPart rclaw7;
    private final ModelPart rclaw6;
    private final ModelPart wing1;
    private final ModelPart wing2;
    private final ModelPart mem1;
    private final ModelPart mem2;
    private final ModelPart lshoulder;
    private final ModelPart rshoulder;
    private final ModelPart rwing1;
    private final ModelPart rmem1;
    private final ModelPart rwing2;
    private final ModelPart rmem2;
    private final ModelPart neck4;
    private final ModelPart neck5;
    private final ModelPart wing3;
    private final ModelPart mem3;
    private final ModelPart rwing3;
    private final ModelPart rmem3;
    private final ModelPart wing4;
    private final ModelPart mem4;
    private final ModelPart rwing4;
    private final ModelPart rmem4;
    private final ModelPart Tailspike1;
    private final ModelPart Tailspike2;
    private final ModelPart Tailspike3;
    private final ModelPart headfin;
    private final ModelPart backfin1;
    private final ModelPart backfin2;
    private final ModelPart neck3L;
    private final ModelPart neck4L;
    private final ModelPart neck3R;
    private final ModelPart neck4R;
    private final ModelPart neck5L;
    private final ModelPart neck5R;
    private final ModelPart jaw5L;
    private final ModelPart jaw5R;
    private final ModelPart head7L;
    private final ModelPart headfinL;
    private final ModelPart headfinR;
    private final ModelPart head7R;
    private final ModelPart jaw1L;
    private final ModelPart jaw1R;
    private final ModelPart head3L;
    private final ModelPart head3R;

    public ThePrinceTeenModel(final ModelPart root, final float f1) {
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.parts = new ModelPart[ThePrinceTeenGeometry.PARTS.length];
        for (int i = 0; i < ThePrinceTeenGeometry.PARTS.length; ++i) {
            this.parts[i] = root.getChild(ThePrinceTeenGeometry.PARTS[i]);
        }
        this.body = root.getChild(ThePrinceTeenGeometry.BODY);
        this.leftleg1 = root.getChild(ThePrinceTeenGeometry.LEFTLEG1);
        this.tail1 = root.getChild(ThePrinceTeenGeometry.TAIL1);
        this.leftleg2 = root.getChild(ThePrinceTeenGeometry.LEFTLEG2);
        this.body2 = root.getChild(ThePrinceTeenGeometry.BODY2);
        this.leftleg3 = root.getChild(ThePrinceTeenGeometry.LEFTLEG3);
        this.tail2 = root.getChild(ThePrinceTeenGeometry.TAIL2);
        this.tail3 = root.getChild(ThePrinceTeenGeometry.TAIL3);
        this.lclaw2 = root.getChild(ThePrinceTeenGeometry.LCLAW2);
        this.lclaw4 = root.getChild(ThePrinceTeenGeometry.LCLAW4);
        this.lclaw5 = root.getChild(ThePrinceTeenGeometry.LCLAW5);
        this.lclaw6 = root.getChild(ThePrinceTeenGeometry.LCLAW6);
        this.lclaw7 = root.getChild(ThePrinceTeenGeometry.LCLAW7);
        this.tail4 = root.getChild(ThePrinceTeenGeometry.TAIL4);
        this.tail5 = root.getChild(ThePrinceTeenGeometry.TAIL5);
        this.neck1 = root.getChild(ThePrinceTeenGeometry.NECK1);
        this.neck3 = root.getChild(ThePrinceTeenGeometry.NECK3);
        this.head3 = root.getChild(ThePrinceTeenGeometry.HEAD3);
        this.jaw1 = root.getChild(ThePrinceTeenGeometry.JAW1);
        this.jaw5 = root.getChild(ThePrinceTeenGeometry.JAW5);
        this.head7 = root.getChild(ThePrinceTeenGeometry.HEAD7);
        this.rightleg1 = root.getChild(ThePrinceTeenGeometry.RIGHTLEG1);
        this.rightleg2 = root.getChild(ThePrinceTeenGeometry.RIGHTLEG2);
        this.rightleg3 = root.getChild(ThePrinceTeenGeometry.RIGHTLEG3);
        this.rclaw2 = root.getChild(ThePrinceTeenGeometry.RCLAW2);
        this.rclaw4 = root.getChild(ThePrinceTeenGeometry.RCLAW4);
        this.rclaw5 = root.getChild(ThePrinceTeenGeometry.RCLAW5);
        this.rclaw7 = root.getChild(ThePrinceTeenGeometry.RCLAW7);
        this.rclaw6 = root.getChild(ThePrinceTeenGeometry.RCLAW6);
        this.wing1 = root.getChild(ThePrinceTeenGeometry.WING1);
        this.wing2 = root.getChild(ThePrinceTeenGeometry.WING2);
        this.mem1 = root.getChild(ThePrinceTeenGeometry.MEM1);
        this.mem2 = root.getChild(ThePrinceTeenGeometry.MEM2);
        this.lshoulder = root.getChild(ThePrinceTeenGeometry.LSHOULDER);
        this.rshoulder = root.getChild(ThePrinceTeenGeometry.RSHOULDER);
        this.rwing1 = root.getChild(ThePrinceTeenGeometry.RWING1);
        this.rmem1 = root.getChild(ThePrinceTeenGeometry.RMEM1);
        this.rwing2 = root.getChild(ThePrinceTeenGeometry.RWING2);
        this.rmem2 = root.getChild(ThePrinceTeenGeometry.RMEM2);
        this.neck4 = root.getChild(ThePrinceTeenGeometry.NECK4);
        this.neck5 = root.getChild(ThePrinceTeenGeometry.NECK5);
        this.wing3 = root.getChild(ThePrinceTeenGeometry.WING3);
        this.mem3 = root.getChild(ThePrinceTeenGeometry.MEM3);
        this.rwing3 = root.getChild(ThePrinceTeenGeometry.RWING3);
        this.rmem3 = root.getChild(ThePrinceTeenGeometry.RMEM3);
        this.wing4 = root.getChild(ThePrinceTeenGeometry.WING4);
        this.mem4 = root.getChild(ThePrinceTeenGeometry.MEM4);
        this.rwing4 = root.getChild(ThePrinceTeenGeometry.RWING4);
        this.rmem4 = root.getChild(ThePrinceTeenGeometry.RMEM4);
        this.Tailspike1 = root.getChild(ThePrinceTeenGeometry.TAILSPIKE1);
        this.Tailspike2 = root.getChild(ThePrinceTeenGeometry.TAILSPIKE2);
        this.Tailspike3 = root.getChild(ThePrinceTeenGeometry.TAILSPIKE3);
        this.headfin = root.getChild(ThePrinceTeenGeometry.HEADFIN);
        this.backfin1 = root.getChild(ThePrinceTeenGeometry.BACKFIN1);
        this.backfin2 = root.getChild(ThePrinceTeenGeometry.BACKFIN2);
        this.neck3L = root.getChild(ThePrinceTeenGeometry.NECK3_L);
        this.neck4L = root.getChild(ThePrinceTeenGeometry.NECK4_L);
        this.neck3R = root.getChild(ThePrinceTeenGeometry.NECK3_R);
        this.neck4R = root.getChild(ThePrinceTeenGeometry.NECK4_R);
        this.neck5L = root.getChild(ThePrinceTeenGeometry.NECK5_L);
        this.neck5R = root.getChild(ThePrinceTeenGeometry.NECK5_R);
        this.jaw5L = root.getChild(ThePrinceTeenGeometry.JAW5_L);
        this.jaw5R = root.getChild(ThePrinceTeenGeometry.JAW5_R);
        this.head7L = root.getChild(ThePrinceTeenGeometry.HEAD7_L);
        this.headfinL = root.getChild(ThePrinceTeenGeometry.HEADFIN_L);
        this.headfinR = root.getChild(ThePrinceTeenGeometry.HEADFIN_R);
        this.head7R = root.getChild(ThePrinceTeenGeometry.HEAD7_R);
        this.jaw1L = root.getChild(ThePrinceTeenGeometry.JAW1_L);
        this.jaw1R = root.getChild(ThePrinceTeenGeometry.JAW1_R);
        this.head3L = root.getChild(ThePrinceTeenGeometry.HEAD3_L);
        this.head3R = root.getChild(ThePrinceTeenGeometry.HEAD3_R);
    }

    /** The angle and pivot writes of {@code render()} (:446-806). */
    @Override
    public void setupAnim(final ThePrinceTeen entity, final float limbSwing, final float limbSwingAmount,
                          final float ageInTicks, final float netHeadYaw, final float headPitch) {
        for (final ModelPart part : this.parts) {
            part.resetPose();
        }
        final ThePrinceTeen c = entity;
        RenderInfo r = null;
        final float f1 = limbSwingAmount;
        final float f2 = ageInTicks;
        float f3 = netHeadYaw;
        final float f4 = headPitch;
        float newangle = 0.0f;
        float newangle2 = 0.0f;
        float rnewangle = 0.0f;
        float rnewangle2 = 0.0f;
        float clawangle = 0.0f;
        float tailspeed = 0.26f;
        float tailamp = 0.08f;
        final float pi4 = 0.7853982f;
        final int current_activity = c.getActivity();
        r = c.getRenderInfo();
        if (f1 > 0.1 && current_activity == 0) {
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.2f * f1;
        } else {
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.04f;
        }
        if (current_activity == 1) {
            newangle = Mth.cos(f2 * 1.4f * this.wingspeed) * 3.1415927f * 0.4f;
        }
        if (c.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 1.7f * this.wingspeed) * 3.1415927f * 0.4f;
        }
        this.wing1.zRot = newangle - 0.4f;
        this.wing2.zRot = newangle * 1.25f - 0.4f;
        this.wing3.zRot = newangle - 0.6f;
        this.wing4.zRot = newangle - 0.2f;
        this.wing2.y = this.wing1.y + (float) Math.sin(this.wing1.zRot) * 22.0f;
        this.wing2.x = this.wing1.x + (float) Math.cos(this.wing1.zRot) * 22.0f;
        this.mem1.zRot = this.wing1.zRot;
        this.mem2.zRot = this.wing2.zRot;
        this.mem3.zRot = this.wing3.zRot;
        this.mem4.zRot = this.wing4.zRot;
        this.mem2.y = this.wing2.y;
        this.mem2.x = this.wing2.x;
        this.rwing1.zRot = -newangle + 0.4f;
        this.rwing2.zRot = -newangle * 1.25f + 0.4f;
        this.rwing3.zRot = -newangle + 0.6f;
        this.rwing4.zRot = -newangle + 0.2f;
        this.rwing2.y = this.rwing1.y - (float) Math.sin(this.rwing1.zRot) * 22.0f;
        this.rwing2.x = this.rwing1.x - (float) Math.cos(this.rwing1.zRot) * 22.0f;
        this.rmem1.zRot = this.rwing1.zRot;
        this.rmem2.zRot = this.rwing2.zRot;
        this.rmem3.zRot = this.rwing3.zRot;
        this.rmem4.zRot = this.rwing4.zRot;
        this.rmem2.y = this.rwing2.y;
        this.rmem2.x = this.rwing2.x;
        if (f1 > 0.1) {
            newangle = Mth.cos(f2 * 0.55f * this.wingspeed) * 3.1415927f * 0.25f * f1;
            newangle2 = Mth.cos((float) (f2 * 0.55f * this.wingspeed + 1.5707963267948966)) * 3.1415927f * 0.25f * f1;
            rnewangle = newangle;
            rnewangle2 = newangle2;
            clawangle = 0.0f;
        } else {
            newangle = 0.0f;
            newangle2 = 0.0f;
            rnewangle = 0.0f;
            rnewangle2 = 0.0f;
            clawangle = 0.0f;
        }
        if (c.getAttacking() != 0) {
            newangle = Mth.cos(f2 * this.wingspeed) * 3.1415927f * 0.25f;
            newangle2 = Mth.cos((float) (f2 * this.wingspeed + 1.5707963267948966)) * 3.1415927f * 0.25f;
            rnewangle = newangle;
            rnewangle2 = newangle2;
            clawangle = 0.0f;
        }
        if (current_activity == 1 && c.getAttacking() == 0) {
            newangle = -0.5f;
            newangle2 = -1.25f;
            rnewangle = 0.5f;
            rnewangle2 = 1.25f;
        }
        if (current_activity == 1) {
            clawangle = -0.685f;
        }
        this.leftleg1.xRot = newangle - 0.575f;
        this.leftleg2.xRot = newangle + 0.977f;
        this.leftleg3.xRot = newangle2 - 0.523f;
        this.leftleg3.y = this.leftleg2.y + (float) Math.cos(this.leftleg2.xRot) * 14.0f + 6.0f;
        this.leftleg3.z = this.leftleg2.z + (float) Math.sin(this.leftleg2.xRot) * 14.0f;
        this.lclaw2.y = this.leftleg3.y + (float) Math.cos(this.leftleg3.xRot) * 17.0f;
        this.lclaw2.z = this.leftleg3.z + (float) Math.sin(this.leftleg3.xRot) * 17.0f - 1.0f;
        this.lclaw4.y = this.lclaw2.y;
        this.lclaw4.z = this.lclaw2.z;
        this.lclaw5.y = this.lclaw2.y;
        this.lclaw5.z = this.lclaw2.z;
        this.lclaw6.y = this.lclaw2.y;
        this.lclaw6.z = this.lclaw2.z;
        this.lclaw7.y = this.lclaw2.y;
        this.lclaw7.z = this.lclaw2.z;
        this.lclaw2.xRot = clawangle;
        this.lclaw4.xRot = clawangle;
        this.lclaw5.xRot = clawangle;
        this.lclaw6.xRot = clawangle;
        this.lclaw7.xRot = clawangle;
        this.rightleg1.xRot = -rnewangle - 0.575f;
        this.rightleg2.xRot = -rnewangle + 0.977f;
        this.rightleg3.xRot = -rnewangle2 - 0.523f;
        this.rightleg3.y = this.rightleg2.y + (float) Math.cos(this.rightleg2.xRot) * 14.0f + 5.0f;
        this.rightleg3.z = this.rightleg2.z + (float) Math.sin(this.rightleg2.xRot) * 14.0f;
        this.rclaw2.y = this.rightleg3.y + (float) Math.cos(this.rightleg3.xRot) * 17.0f;
        this.rclaw2.z = this.rightleg3.z + (float) Math.sin(this.rightleg3.xRot) * 17.0f - 1.0f;
        this.rclaw4.y = this.rclaw2.y;
        this.rclaw4.z = this.rclaw2.z;
        this.rclaw5.y = this.rclaw2.y;
        this.rclaw5.z = this.rclaw2.z;
        this.rclaw6.y = this.rclaw2.y;
        this.rclaw6.z = this.rclaw2.z;
        this.rclaw7.y = this.rclaw2.y;
        this.rclaw7.z = this.rclaw2.z;
        this.rclaw2.xRot = clawangle;
        this.rclaw4.xRot = clawangle;
        this.rclaw5.xRot = clawangle;
        this.rclaw6.xRot = clawangle;
        this.rclaw7.xRot = clawangle;
        if (c.getAttacking() != 0) {
            tailspeed = 0.56f;
            tailamp = 0.19f;
        }
        if (c.isSitting()) {
            tailamp = 0.0f;
        }
        this.tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp / 4.0f;
        this.tail2.z = this.tail1.z + (float) Math.cos(this.tail1.yRot) * 11.0f;
        this.tail2.x = this.tail1.x + (float) Math.sin(this.tail1.yRot) * 11.0f;
        this.tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - pi4) * 3.1415927f * tailamp;
        this.tail3.z = this.tail2.z + (float) Math.cos(this.tail2.yRot) * 9.0f;
        this.tail3.x = this.tail2.x + (float) Math.sin(this.tail2.yRot) * 9.0f;
        this.tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 2.0f * pi4) * 3.1415927f * tailamp;
        this.tail4.z = this.tail3.z + (float) Math.cos(this.tail3.yRot) * 9.0f;
        this.tail4.x = this.tail3.x + (float) Math.sin(this.tail3.yRot) * 9.0f;
        this.tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0f * pi4) * 3.1415927f * tailamp;
        newangle = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0f * pi4) * 3.1415927f * tailamp;
        newangle /= 2.0f;
        this.tail5.z = this.tail4.z + (float) Math.cos(this.tail4.yRot) * 9.0f;
        this.tail5.x = this.tail4.x + (float) Math.sin(this.tail4.yRot) * 9.0f;
        this.tail5.yRot = this.tail4.yRot + newangle;
        this.Tailspike1.z = this.tail5.z + (float) Math.cos(this.tail5.yRot) * 9.0f;
        this.Tailspike1.x = this.tail5.x + (float) Math.sin(this.tail5.yRot) * 9.0f;
        this.Tailspike2.z = this.tail5.z + (float) Math.cos(this.tail5.yRot) * 15.0f;
        this.Tailspike2.x = this.tail5.x + (float) Math.sin(this.tail5.yRot) * 15.0f;
        final float n = this.tail5.yRot + newangle * 2.0f / 3.0f;
        this.Tailspike2.yRot = n;
        this.Tailspike1.yRot = n;
        this.Tailspike3.z = this.Tailspike1.z + (float) Math.cos(this.Tailspike1.yRot) * 11.0f;
        this.Tailspike3.x = this.Tailspike1.x + (float) Math.sin(this.Tailspike1.yRot) * 11.0f;
        this.Tailspike3.yRot = this.Tailspike1.yRot + newangle * 3.0f / 2.0f;
        if (c.getActivity() == 1) {
            f3 = (c.yRotO - c.getYRot()) * 10.0f;
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
        }
        float h4;
        float h3;
        float h2 = h3 = (h4 = f3 * 2.0f / 3.0f);
        float d4;
        float d3;
        float d2 = d3 = (d4 = f4 * 2.0f / 3.0f);
        if (h3 < 0.0f) {
            h4 = (h2 = h3 / 2.0f);
            d4 = (d2 = d3 / 2.0f);
        } else {
            h3 = (h2 = h4 / 2.0f);
            d3 = (d2 = d4 / 2.0f);
        }
        this.head7.yRot = (float) Math.toRadians(h2);
        this.head3.yRot = (float) Math.toRadians(h2);
        this.headfin.yRot = (float) Math.toRadians(h2);
        this.jaw5.yRot = (float) Math.toRadians(h2);
        this.jaw1.yRot = (float) Math.toRadians(h2);
        this.neck3.yRot = (float) Math.toRadians(h2) / 8.0f;
        this.neck4.yRot = (float) Math.toRadians(h2) / 4.0f;
        this.neck5.yRot = (float) Math.toRadians(h2) / 2.0f;
        this.head7L.yRot = (float) Math.toRadians(h3);
        this.head3L.yRot = (float) Math.toRadians(h3);
        this.headfinL.yRot = (float) Math.toRadians(h3);
        this.jaw5L.yRot = (float) Math.toRadians(h3);
        this.jaw1L.yRot = (float) Math.toRadians(h3);
        this.neck3L.yRot = (float) Math.toRadians(h3) / 8.0f;
        this.neck4L.yRot = (float) Math.toRadians(h3) / 4.0f;
        this.neck5L.yRot = (float) Math.toRadians(h3) / 2.0f;
        this.head7R.yRot = (float) Math.toRadians(h4);
        this.head3R.yRot = (float) Math.toRadians(h4);
        this.headfinR.yRot = (float) Math.toRadians(h4);
        this.jaw5R.yRot = (float) Math.toRadians(h4);
        this.jaw1R.yRot = (float) Math.toRadians(h4);
        this.neck3R.yRot = (float) Math.toRadians(h4) / 8.0f;
        this.neck4R.yRot = (float) Math.toRadians(h4) / 4.0f;
        this.neck5R.yRot = (float) Math.toRadians(h4) / 2.0f;
        float Rjx;
        float Ljx;
        float jx = Ljx = (Rjx = 0.0f);
        if (c.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 0.9f * this.wingspeed) * 3.1415927f * 0.1f;
            Ljx = 0.25f + newangle;
            newangle = Mth.cos(f2 * 1.1f * this.wingspeed) * 3.1415927f * 0.1f;
            Rjx = 0.25f + newangle;
            newangle = Mth.cos(f2 * 1.3f * this.wingspeed) * 3.1415927f * 0.1f;
            jx = 0.25f + newangle;
        } else {
            newangle = Mth.cos(f2 * 0.25f * this.wingspeed) * 3.1415927f * 0.02f;
            Ljx = 0.1f + newangle;
            newangle = Mth.cos(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.02f;
            Rjx = 0.1f + newangle;
            newangle = Mth.cos(f2 * 0.35f * this.wingspeed) * 3.1415927f * 0.02f;
            jx = 0.1f + newangle;
        }
        this.head7.xRot = (float) Math.toRadians(d2);
        this.head3.xRot = (float) Math.toRadians(d2);
        this.headfin.xRot = (float) Math.toRadians(d2) + 0.5f;
        this.jaw5.xRot = (float) Math.toRadians(d2) + jx;
        this.jaw1.xRot = (float) Math.toRadians(d2) + jx;
        this.head7L.xRot = (float) Math.toRadians(d3);
        this.head3L.xRot = (float) Math.toRadians(d3);
        this.headfinL.xRot = (float) Math.toRadians(d3) + 0.5f;
        this.jaw5L.xRot = (float) Math.toRadians(d3) + Ljx;
        this.jaw1L.xRot = (float) Math.toRadians(d3) + Ljx;
        this.head7R.xRot = (float) Math.toRadians(d4);
        this.head3R.xRot = (float) Math.toRadians(d4);
        this.headfinR.xRot = (float) Math.toRadians(d4) + 0.5f;
        this.jaw5R.xRot = (float) Math.toRadians(d4) + Rjx;
        this.jaw1R.xRot = (float) Math.toRadians(d4) + Rjx;
        d3 = (float) c.getHead1Ext();
        d2 = (float) c.getHead2Ext();
        d4 = (float) c.getHead3Ext();
        this.neck3L.xRot = -(float) Math.toRadians(d3 / 3.0);
        this.neck4L.xRot = -(float) Math.toRadians(d3 * 2.0 / 3.0);
        this.neck5L.xRot = -(float) Math.toRadians(d3);
        this.neck3.xRot = -(float) Math.toRadians(d2 / 3.0);
        this.neck4.xRot = -(float) Math.toRadians(d2 * 2.0 / 3.0);
        this.neck5.xRot = -(float) Math.toRadians(d2);
        this.neck3R.xRot = -(float) Math.toRadians(d4 / 3.0);
        this.neck4R.xRot = -(float) Math.toRadians(d4 * 2.0 / 3.0);
        this.neck5R.xRot = -(float) Math.toRadians(d4);
        this.neck4.y = this.neck3.y + (float) Math.sin(this.neck3.xRot) * 9.0f;
        this.neck4.z = this.neck3.z - (float) Math.cos(this.neck3.xRot) * 9.0f;
        this.neck4.x = this.neck3.x - (float) Math.sin(this.neck3.yRot) * 9.0f * (float) Math.cos(this.neck3.xRot);
        this.neck5.y = this.neck4.y + (float) Math.sin(this.neck4.xRot) * 9.0f;
        this.neck5.z = this.neck4.z - (float) Math.cos(this.neck4.xRot) * 9.0f;
        this.neck5.x = this.neck4.x - (float) Math.sin(this.neck4.yRot) * 9.0f * (float) Math.cos(this.neck4.xRot);
        this.head7.y = this.neck5.y + (float) Math.sin(this.neck5.xRot) * 9.0f;
        final float y = this.head7.y;
        this.head3.y = y;
        this.jaw1.y = y;
        this.jaw5.y = y;
        this.headfin.y = y;
        this.head7.z = this.neck5.z - (float) Math.cos(this.neck5.xRot) * 9.0f;
        final float z = this.head7.z;
        this.head3.z = z;
        this.jaw1.z = z;
        this.jaw5.z = z;
        this.headfin.z = z;
        this.head7.x = this.neck5.x - (float) Math.sin(this.neck5.yRot) * 9.0f * (float) Math.cos(this.neck5.xRot);
        final float x = this.head7.x;
        this.head3.x = x;
        this.jaw1.x = x;
        this.jaw5.x = x;
        this.headfin.x = x;
        this.neck4L.y = this.neck3L.y + (float) Math.sin(this.neck3L.xRot) * 9.0f;
        this.neck4L.z = this.neck3L.z - (float) Math.cos(this.neck3L.xRot) * 9.0f;
        this.neck4L.x = this.neck3L.x - (float) Math.sin(this.neck3L.yRot) * 9.0f * (float) Math.cos(this.neck3L.xRot);
        this.neck5L.y = this.neck4L.y + (float) Math.sin(this.neck4L.xRot) * 9.0f;
        this.neck5L.z = this.neck4L.z - (float) Math.cos(this.neck4L.xRot) * 9.0f;
        this.neck5L.x = this.neck4L.x - (float) Math.sin(this.neck4L.yRot) * 9.0f * (float) Math.cos(this.neck4L.xRot);
        this.head7L.y = this.neck5L.y + (float) Math.sin(this.neck5L.xRot) * 9.0f;
        final float y2 = this.head7L.y;
        this.head3L.y = y2;
        this.jaw1L.y = y2;
        this.jaw5L.y = y2;
        this.headfinL.y = y2;
        this.head7L.z = this.neck5L.z - (float) Math.cos(this.neck5L.xRot) * 9.0f;
        final float z2 = this.head7L.z;
        this.head3L.z = z2;
        this.jaw1L.z = z2;
        this.jaw5L.z = z2;
        this.headfinL.z = z2;
        this.head7L.x = this.neck5L.x - (float) Math.sin(this.neck5L.yRot) * 9.0f * (float) Math.cos(this.neck5L.xRot);
        final float x2 = this.head7L.x;
        this.head3L.x = x2;
        this.jaw1L.x = x2;
        this.jaw5L.x = x2;
        this.headfinL.x = x2;
        this.neck4R.y = this.neck3R.y + (float) Math.sin(this.neck3R.xRot) * 9.0f;
        this.neck4R.z = this.neck3R.z - (float) Math.cos(this.neck3R.xRot) * 9.0f;
        this.neck4R.x = this.neck3R.x - (float) Math.sin(this.neck3R.yRot) * 9.0f * (float) Math.cos(this.neck3R.xRot);
        this.neck5R.y = this.neck4R.y + (float) Math.sin(this.neck4R.xRot) * 9.0f;
        this.neck5R.z = this.neck4R.z - (float) Math.cos(this.neck4R.xRot) * 9.0f;
        this.neck5R.x = this.neck4R.x - (float) Math.sin(this.neck4R.yRot) * 9.0f * (float) Math.cos(this.neck4R.xRot);
        this.head7R.y = this.neck5R.y + (float) Math.sin(this.neck5R.xRot) * 9.0f;
        final float y3 = this.head7R.y;
        this.head3R.y = y3;
        this.jaw1R.y = y3;
        this.jaw5R.y = y3;
        this.headfinR.y = y3;
        this.head7R.z = this.neck5R.z - (float) Math.cos(this.neck5R.xRot) * 9.0f;
        final float z3 = this.head7R.z;
        this.head3R.z = z3;
        this.jaw1R.z = z3;
        this.jaw5R.z = z3;
        this.headfinR.z = z3;
        this.head7R.x = this.neck5R.x - (float) Math.sin(this.neck5R.yRot) * 9.0f * (float) Math.cos(this.neck5R.xRot);
        final float x3 = this.head7R.x;
        this.head3R.x = x3;
        this.jaw1R.x = x3;
        this.jaw5R.x = x3;
        this.headfinR.x = x3;
        c.setRenderInfo(r);
    }

    /** The opaque part of {@code render()} (:807-869), in the original order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        this.body.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.body2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.leftleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lclaw7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.tail5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightleg1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightleg2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rightleg3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw7.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rclaw6.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.lshoulder.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rshoulder.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck5.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wing3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.wing4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rwing4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tailspike1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tailspike2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.Tailspike3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.headfin.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.backfin1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.backfin2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck3L.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck4L.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck3R.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck4R.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck5L.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.neck5R.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw5L.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw5R.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head7L.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.headfinL.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.headfinR.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head7R.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw1L.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.jaw1R.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head3L.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.head3R.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    /**
     * The blended part of {@code render()} (:870-884): the eight wing membranes. {@code color} carries
     * {@code glColor4f(0.75, 0.75, 0.75, 0.55)}; the buffer must be a translucent entity type.
     */
    public void renderWings(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                            final int packedOverlay, final int color) {
        this.mem1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.mem2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rmem1.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rmem2.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.mem3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rmem3.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.mem4.render(poseStack, buffer, packedLight, packedOverlay, color);
        this.rmem4.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
