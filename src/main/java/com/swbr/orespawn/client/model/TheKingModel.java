package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.TheKingGeometry;
import com.swbr.orespawn.entity.boss.king.TheKing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelTheKing} (ModelTheKing.java:8-1616): 119 boxes, 2048x2048 texture, geometry
 * {@link TheKingGeometry}. {@code render()} (:733-1206) beats the six-finger wings, opens the claws and swings the legs
 * while {@code TheKing.getAttacking() != 0}, waves the seven-segment tail and moves the three heads through
 * {@code moveLeftHead}, {@code moveCenterHead} and {@code moveRightHead} (:1218-1615).
 *
 * <p>R8: every write to {@code rotateAngle*}/{@code rotationPoint*} is in {@link #setupAnim}, transcribed statement by
 * statement (decompiler temporaries included), after {@code resetPose()} of all parts. The original read no field
 * that it had not written earlier in the same frame, so the reset changes no pose.
 *
 * <p>GL (:1189-1205): after the 109 opaque parts, the ten wing membranes ({@code Lwing2/4/6/8/10},
 * {@code Rwing2/4/6/8/10}) inside {@code glEnable(GL_BLEND)}, {@code glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)} and
 * {@code glColor4f(0.75, 0.75, 0.75, 0.55)}. {@link #renderToBuffer} draws only the opaque parts into the renderer's
 * buffer ({@code entityCutoutNoCull}, the {@link EntityModel} default); the membranes are {@link #renderWings}, drawn
 * by {@code RenderTheKing.WingPass} into {@link RenderType#entityTranslucent} with {@link #WING_COLOR}. The fixed
 * colour replaces the renderer's, as the original {@code glColor4f} did. {@code GL_NORMALIZE} (2977) has no
 * counterpart; 1.21.1 transforms normals with the pose's normal matrix.
 *
 * <p>PORT: 1.21.1 sorts translucent geometry per buffer, so the membranes are drawn after all opaque geometry of the
 * frame instead of right after the body - the same result for a blend over an opaque background.
 *
 * <p>{@code wingspeed} is the constructor argument, 0.65 from ClientProxyOreSpawn (manifest {@code model_args}).
 */
public class TheKingModel extends EntityModel<TheKing> {

    /** Register with {@code TheKingGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "the_king"), "main");

    /** {@code glColor4f(0.75f, 0.75f, 0.75f, 0.55f)} (:1193). */
    public static final int WING_COLOR = FastColor.ARGB32.colorFromFloat(0.55f, 0.75f, 0.75f, 0.75f);

    /** {@code ModelTheKing(float f1)}: {@code wingspeed = 1.0f; wingspeed = f1} (:132-133). */
    private float wingspeed;
    private final ModelPart LCClaw1;
    private final ModelPart LThigh;
    private final ModelPart LUpperLeg;
    private final ModelPart TailTip;
    private final ModelPart Tail1;
    private final ModelPart Tail2;
    private final ModelPart Tail3;
    private final ModelPart Tail4;
    private final ModelPart Tail5;
    private final ModelPart Tail6;
    private final ModelPart Tail7;
    private final ModelPart Body1;
    private final ModelPart Chest;
    private final ModelPart NeckC1;
    private final ModelPart LLowerLeg;
    private final ModelPart LFoot;
    private final ModelPart LLClaw1;
    private final ModelPart LRClaw1;
    private final ModelPart LCClaw2;
    private final ModelPart LLClaw2;
    private final ModelPart TailSpike;
    private final ModelPart LRClaw2;
    private final ModelPart LClawRear;
    private final ModelPart NeckL1;
    private final ModelPart NeckR1;
    private final ModelPart RThigh;
    private final ModelPart RUpperLeg;
    private final ModelPart RLowerLeg;
    private final ModelPart RFoot;
    private final ModelPart RClawRear;
    private final ModelPart RLClaw1;
    private final ModelPart RCClaw1;
    private final ModelPart RRClaw1;
    private final ModelPart RLClaw2;
    private final ModelPart RCClaw2;
    private final ModelPart RRClaw2;
    private final ModelPart NeckL2;
    private final ModelPart NeckC2;
    private final ModelPart NeckR2;
    private final ModelPart NeckL3;
    private final ModelPart NeckC3;
    private final ModelPart NeckR3;
    private final ModelPart NeckL4;
    private final ModelPart LHead1;
    private final ModelPart LHead2;
    private final ModelPart LHead3;
    private final ModelPart LJaw1;
    private final ModelPart LJaw2;
    private final ModelPart LJaw3;
    private final ModelPart LTooth1;
    private final ModelPart LTooth2;
    private final ModelPart LTooth3;
    private final ModelPart LTooth4;
    private final ModelPart NeckC4;
    private final ModelPart NeckR4;
    private final ModelPart CHead1;
    private final ModelPart RHead1;
    private final ModelPart CHead2;
    private final ModelPart RHead2;
    private final ModelPart CHead3;
    private final ModelPart RHead3;
    private final ModelPart CJaw1;
    private final ModelPart CJaw2;
    private final ModelPart CJaw3;
    private final ModelPart RJaw1;
    private final ModelPart RJaw2;
    private final ModelPart RJaw3;
    private final ModelPart CTooth3;
    private final ModelPart CTooth4;
    private final ModelPart CTooth1;
    private final ModelPart CTooth2;
    private final ModelPart RTooth3;
    private final ModelPart RTooth4;
    private final ModelPart RTooth1;
    private final ModelPart RTooth2;
    private final ModelPart LLEye;
    private final ModelPart LREye;
    private final ModelPart CLEye;
    private final ModelPart CREye;
    private final ModelPart RLEye;
    private final ModelPart RREye;
    private final ModelPart LHeadMane;
    private final ModelPart CHeadMane;
    private final ModelPart RHeadMane;
    private final ModelPart LLNoseSpike;
    private final ModelPart LRNoseSpike;
    private final ModelPart CLNoseSpike;
    private final ModelPart CRNoseSpike;
    private final ModelPart RLNoseSpike;
    private final ModelPart RRNoseSpike;
    private final ModelPart Back1;
    private final ModelPart Back2;
    private final ModelPart Lwing1;
    private final ModelPart Lwing2;
    private final ModelPart Lwing3;
    private final ModelPart Lwing4;
    private final ModelPart Lwing5;
    private final ModelPart Lwing6;
    private final ModelPart Lwing7;
    private final ModelPart Lwing9;
    private final ModelPart Lwing8;
    private final ModelPart Lwing10;
    private final ModelPart Rwing1;
    private final ModelPart Rwing2;
    private final ModelPart Rwing3;
    private final ModelPart Rwing4;
    private final ModelPart Rwing5;
    private final ModelPart Rwing6;
    private final ModelPart Rwing7;
    private final ModelPart Rwing8;
    private final ModelPart Rwing9;
    private final ModelPart Rwing10;
    private final ModelPart TailTip2;
    private final ModelPart Ridge1;
    private final ModelPart Ridge2;
    private final ModelPart Ridge3;
    private final ModelPart Ridge4;
    private final ModelPart Ridge5;
    private final ModelPart Ridge6;

    /** Every part, for {@code resetPose()}. */
    private final ModelPart[] allParts;
    /** The opaque draw order of {@code render()} (:1080-1188). */
    private final ModelPart[] opaqueOrder;
    /** The blend block of {@code render()} (:1194-1203). */
    private final ModelPart[] wingOrder;

    /** {@code ModelTheKing(float f1)} (:131-731); the boxes themselves are {@link TheKingGeometry}. */
    public TheKingModel(final ModelPart root, final float f1) {
        super();
        this.wingspeed = 1.0f;
        this.wingspeed = f1;
        this.LCClaw1 = root.getChild(TheKingGeometry.LCCLAW1);
        this.LThigh = root.getChild(TheKingGeometry.LTHIGH);
        this.LUpperLeg = root.getChild(TheKingGeometry.LUPPER_LEG);
        this.TailTip = root.getChild(TheKingGeometry.TAIL_TIP);
        this.Tail1 = root.getChild(TheKingGeometry.TAIL1);
        this.Tail2 = root.getChild(TheKingGeometry.TAIL2);
        this.Tail3 = root.getChild(TheKingGeometry.TAIL3);
        this.Tail4 = root.getChild(TheKingGeometry.TAIL4);
        this.Tail5 = root.getChild(TheKingGeometry.TAIL5);
        this.Tail6 = root.getChild(TheKingGeometry.TAIL6);
        this.Tail7 = root.getChild(TheKingGeometry.TAIL7);
        this.Body1 = root.getChild(TheKingGeometry.BODY1);
        this.Chest = root.getChild(TheKingGeometry.CHEST);
        this.NeckC1 = root.getChild(TheKingGeometry.NECK_C1);
        this.LLowerLeg = root.getChild(TheKingGeometry.LLOWER_LEG);
        this.LFoot = root.getChild(TheKingGeometry.LFOOT);
        this.LLClaw1 = root.getChild(TheKingGeometry.LLCLAW1);
        this.LRClaw1 = root.getChild(TheKingGeometry.LRCLAW1);
        this.LCClaw2 = root.getChild(TheKingGeometry.LCCLAW2);
        this.LLClaw2 = root.getChild(TheKingGeometry.LLCLAW2);
        this.TailSpike = root.getChild(TheKingGeometry.TAIL_SPIKE);
        this.LRClaw2 = root.getChild(TheKingGeometry.LRCLAW2);
        this.LClawRear = root.getChild(TheKingGeometry.LCLAW_REAR);
        this.NeckL1 = root.getChild(TheKingGeometry.NECK_L1);
        this.NeckR1 = root.getChild(TheKingGeometry.NECK_R1);
        this.RThigh = root.getChild(TheKingGeometry.RTHIGH);
        this.RUpperLeg = root.getChild(TheKingGeometry.RUPPER_LEG);
        this.RLowerLeg = root.getChild(TheKingGeometry.RLOWER_LEG);
        this.RFoot = root.getChild(TheKingGeometry.RFOOT);
        this.RClawRear = root.getChild(TheKingGeometry.RCLAW_REAR);
        this.RLClaw1 = root.getChild(TheKingGeometry.RLCLAW1);
        this.RCClaw1 = root.getChild(TheKingGeometry.RCCLAW1);
        this.RRClaw1 = root.getChild(TheKingGeometry.RRCLAW1);
        this.RLClaw2 = root.getChild(TheKingGeometry.RLCLAW2);
        this.RCClaw2 = root.getChild(TheKingGeometry.RCCLAW2);
        this.RRClaw2 = root.getChild(TheKingGeometry.RRCLAW2);
        this.NeckL2 = root.getChild(TheKingGeometry.NECK_L2);
        this.NeckC2 = root.getChild(TheKingGeometry.NECK_C2);
        this.NeckR2 = root.getChild(TheKingGeometry.NECK_R2);
        this.NeckL3 = root.getChild(TheKingGeometry.NECK_L3);
        this.NeckC3 = root.getChild(TheKingGeometry.NECK_C3);
        this.NeckR3 = root.getChild(TheKingGeometry.NECK_R3);
        this.NeckL4 = root.getChild(TheKingGeometry.NECK_L4);
        this.LHead1 = root.getChild(TheKingGeometry.LHEAD1);
        this.LHead2 = root.getChild(TheKingGeometry.LHEAD2);
        this.LHead3 = root.getChild(TheKingGeometry.LHEAD3);
        this.LJaw1 = root.getChild(TheKingGeometry.LJAW1);
        this.LJaw2 = root.getChild(TheKingGeometry.LJAW2);
        this.LJaw3 = root.getChild(TheKingGeometry.LJAW3);
        this.LTooth1 = root.getChild(TheKingGeometry.LTOOTH1);
        this.LTooth2 = root.getChild(TheKingGeometry.LTOOTH2);
        this.LTooth3 = root.getChild(TheKingGeometry.LTOOTH3);
        this.LTooth4 = root.getChild(TheKingGeometry.LTOOTH4);
        this.NeckC4 = root.getChild(TheKingGeometry.NECK_C4);
        this.NeckR4 = root.getChild(TheKingGeometry.NECK_R4);
        this.CHead1 = root.getChild(TheKingGeometry.CHEAD1);
        this.RHead1 = root.getChild(TheKingGeometry.RHEAD1);
        this.CHead2 = root.getChild(TheKingGeometry.CHEAD2);
        this.RHead2 = root.getChild(TheKingGeometry.RHEAD2);
        this.CHead3 = root.getChild(TheKingGeometry.CHEAD3);
        this.RHead3 = root.getChild(TheKingGeometry.RHEAD3);
        this.CJaw1 = root.getChild(TheKingGeometry.CJAW1);
        this.CJaw2 = root.getChild(TheKingGeometry.CJAW2);
        this.CJaw3 = root.getChild(TheKingGeometry.CJAW3);
        this.RJaw1 = root.getChild(TheKingGeometry.RJAW1);
        this.RJaw2 = root.getChild(TheKingGeometry.RJAW2);
        this.RJaw3 = root.getChild(TheKingGeometry.RJAW3);
        this.CTooth3 = root.getChild(TheKingGeometry.CTOOTH3);
        this.CTooth4 = root.getChild(TheKingGeometry.CTOOTH4);
        this.CTooth1 = root.getChild(TheKingGeometry.CTOOTH1);
        this.CTooth2 = root.getChild(TheKingGeometry.CTOOTH2);
        this.RTooth3 = root.getChild(TheKingGeometry.RTOOTH3);
        this.RTooth4 = root.getChild(TheKingGeometry.RTOOTH4);
        this.RTooth1 = root.getChild(TheKingGeometry.RTOOTH1);
        this.RTooth2 = root.getChild(TheKingGeometry.RTOOTH2);
        this.LLEye = root.getChild(TheKingGeometry.LLEYE);
        this.LREye = root.getChild(TheKingGeometry.LREYE);
        this.CLEye = root.getChild(TheKingGeometry.CLEYE);
        this.CREye = root.getChild(TheKingGeometry.CREYE);
        this.RLEye = root.getChild(TheKingGeometry.RLEYE);
        this.RREye = root.getChild(TheKingGeometry.RREYE);
        this.LHeadMane = root.getChild(TheKingGeometry.LHEAD_MANE);
        this.CHeadMane = root.getChild(TheKingGeometry.CHEAD_MANE);
        this.RHeadMane = root.getChild(TheKingGeometry.RHEAD_MANE);
        this.LLNoseSpike = root.getChild(TheKingGeometry.LLNOSE_SPIKE);
        this.LRNoseSpike = root.getChild(TheKingGeometry.LRNOSE_SPIKE);
        this.CLNoseSpike = root.getChild(TheKingGeometry.CLNOSE_SPIKE);
        this.CRNoseSpike = root.getChild(TheKingGeometry.CRNOSE_SPIKE);
        this.RLNoseSpike = root.getChild(TheKingGeometry.RLNOSE_SPIKE);
        this.RRNoseSpike = root.getChild(TheKingGeometry.RRNOSE_SPIKE);
        this.Back1 = root.getChild(TheKingGeometry.BACK1);
        this.Back2 = root.getChild(TheKingGeometry.BACK2);
        this.Lwing1 = root.getChild(TheKingGeometry.LWING1);
        this.Lwing2 = root.getChild(TheKingGeometry.LWING2);
        this.Lwing3 = root.getChild(TheKingGeometry.LWING3);
        this.Lwing4 = root.getChild(TheKingGeometry.LWING4);
        this.Lwing5 = root.getChild(TheKingGeometry.LWING5);
        this.Lwing6 = root.getChild(TheKingGeometry.LWING6);
        this.Lwing7 = root.getChild(TheKingGeometry.LWING7);
        this.Lwing9 = root.getChild(TheKingGeometry.LWING9);
        this.Lwing8 = root.getChild(TheKingGeometry.LWING8);
        this.Lwing10 = root.getChild(TheKingGeometry.LWING10);
        this.Rwing1 = root.getChild(TheKingGeometry.RWING1);
        this.Rwing2 = root.getChild(TheKingGeometry.RWING2);
        this.Rwing3 = root.getChild(TheKingGeometry.RWING3);
        this.Rwing4 = root.getChild(TheKingGeometry.RWING4);
        this.Rwing5 = root.getChild(TheKingGeometry.RWING5);
        this.Rwing6 = root.getChild(TheKingGeometry.RWING6);
        this.Rwing7 = root.getChild(TheKingGeometry.RWING7);
        this.Rwing8 = root.getChild(TheKingGeometry.RWING8);
        this.Rwing9 = root.getChild(TheKingGeometry.RWING9);
        this.Rwing10 = root.getChild(TheKingGeometry.RWING10);
        this.TailTip2 = root.getChild(TheKingGeometry.TAIL_TIP2);
        this.Ridge1 = root.getChild(TheKingGeometry.RIDGE1);
        this.Ridge2 = root.getChild(TheKingGeometry.RIDGE2);
        this.Ridge3 = root.getChild(TheKingGeometry.RIDGE3);
        this.Ridge4 = root.getChild(TheKingGeometry.RIDGE4);
        this.Ridge5 = root.getChild(TheKingGeometry.RIDGE5);
        this.Ridge6 = root.getChild(TheKingGeometry.RIDGE6);
        this.allParts = new ModelPart[] {
                this.LCClaw1,
                this.LThigh,
                this.LUpperLeg,
                this.TailTip,
                this.Tail1,
                this.Tail2,
                this.Tail3,
                this.Tail4,
                this.Tail5,
                this.Tail6,
                this.Tail7,
                this.Body1,
                this.Chest,
                this.NeckC1,
                this.LLowerLeg,
                this.LFoot,
                this.LLClaw1,
                this.LRClaw1,
                this.LCClaw2,
                this.LLClaw2,
                this.TailSpike,
                this.LRClaw2,
                this.LClawRear,
                this.NeckL1,
                this.NeckR1,
                this.RThigh,
                this.RUpperLeg,
                this.RLowerLeg,
                this.RFoot,
                this.RClawRear,
                this.RLClaw1,
                this.RCClaw1,
                this.RRClaw1,
                this.RLClaw2,
                this.RCClaw2,
                this.RRClaw2,
                this.NeckL2,
                this.NeckC2,
                this.NeckR2,
                this.NeckL3,
                this.NeckC3,
                this.NeckR3,
                this.NeckL4,
                this.LHead1,
                this.LHead2,
                this.LHead3,
                this.LJaw1,
                this.LJaw2,
                this.LJaw3,
                this.LTooth1,
                this.LTooth2,
                this.LTooth3,
                this.LTooth4,
                this.NeckC4,
                this.NeckR4,
                this.CHead1,
                this.RHead1,
                this.CHead2,
                this.RHead2,
                this.CHead3,
                this.RHead3,
                this.CJaw1,
                this.CJaw2,
                this.CJaw3,
                this.RJaw1,
                this.RJaw2,
                this.RJaw3,
                this.CTooth3,
                this.CTooth4,
                this.CTooth1,
                this.CTooth2,
                this.RTooth3,
                this.RTooth4,
                this.RTooth1,
                this.RTooth2,
                this.LLEye,
                this.LREye,
                this.CLEye,
                this.CREye,
                this.RLEye,
                this.RREye,
                this.LHeadMane,
                this.CHeadMane,
                this.RHeadMane,
                this.LLNoseSpike,
                this.LRNoseSpike,
                this.CLNoseSpike,
                this.CRNoseSpike,
                this.RLNoseSpike,
                this.RRNoseSpike,
                this.Back1,
                this.Back2,
                this.Lwing1,
                this.Lwing2,
                this.Lwing3,
                this.Lwing4,
                this.Lwing5,
                this.Lwing6,
                this.Lwing7,
                this.Lwing9,
                this.Lwing8,
                this.Lwing10,
                this.Rwing1,
                this.Rwing2,
                this.Rwing3,
                this.Rwing4,
                this.Rwing5,
                this.Rwing6,
                this.Rwing7,
                this.Rwing8,
                this.Rwing9,
                this.Rwing10,
                this.TailTip2,
                this.Ridge1,
                this.Ridge2,
                this.Ridge3,
                this.Ridge4,
                this.Ridge5,
                this.Ridge6 };
        this.opaqueOrder = new ModelPart[] {
                this.LCClaw1,
                this.LThigh,
                this.LUpperLeg,
                this.TailTip,
                this.Tail1,
                this.Tail2,
                this.Tail3,
                this.Tail4,
                this.Tail5,
                this.Tail6,
                this.Tail7,
                this.Body1,
                this.Chest,
                this.NeckC1,
                this.LLowerLeg,
                this.LFoot,
                this.LLClaw1,
                this.LRClaw1,
                this.LCClaw2,
                this.LLClaw2,
                this.TailSpike,
                this.LRClaw2,
                this.LClawRear,
                this.NeckL1,
                this.NeckR1,
                this.RThigh,
                this.RUpperLeg,
                this.RLowerLeg,
                this.RFoot,
                this.RClawRear,
                this.RLClaw1,
                this.RCClaw1,
                this.RRClaw1,
                this.RLClaw2,
                this.RCClaw2,
                this.RRClaw2,
                this.NeckL2,
                this.NeckC2,
                this.NeckR2,
                this.NeckL3,
                this.NeckC3,
                this.NeckR3,
                this.NeckL4,
                this.LHead1,
                this.LHead2,
                this.LHead3,
                this.LJaw1,
                this.LJaw2,
                this.LJaw3,
                this.LTooth1,
                this.LTooth2,
                this.LTooth3,
                this.LTooth4,
                this.NeckC4,
                this.NeckR4,
                this.CHead1,
                this.RHead1,
                this.CHead2,
                this.RHead2,
                this.CHead3,
                this.RHead3,
                this.CJaw1,
                this.CJaw2,
                this.CJaw3,
                this.RJaw1,
                this.RJaw2,
                this.RJaw3,
                this.CTooth3,
                this.CTooth4,
                this.CTooth1,
                this.CTooth2,
                this.RTooth3,
                this.RTooth4,
                this.RTooth1,
                this.RTooth2,
                this.LLEye,
                this.LREye,
                this.CLEye,
                this.CREye,
                this.RLEye,
                this.RREye,
                this.LHeadMane,
                this.CHeadMane,
                this.RHeadMane,
                this.LLNoseSpike,
                this.LRNoseSpike,
                this.CLNoseSpike,
                this.CRNoseSpike,
                this.RLNoseSpike,
                this.RRNoseSpike,
                this.Back1,
                this.Back2,
                this.Lwing1,
                this.Lwing3,
                this.Lwing5,
                this.Lwing7,
                this.Lwing9,
                this.Rwing1,
                this.Rwing3,
                this.Rwing5,
                this.Rwing7,
                this.Rwing9,
                this.TailTip2,
                this.Ridge1,
                this.Ridge2,
                this.Ridge3,
                this.Ridge4,
                this.Ridge5,
                this.Ridge6 };
        this.wingOrder = new ModelPart[] {
                this.Lwing2,
                this.Lwing4,
                this.Lwing6,
                this.Lwing8,
                this.Lwing10,
                this.Rwing2,
                this.Rwing4,
                this.Rwing6,
                this.Rwing8,
                this.Rwing10 };
    }

    /**
     * The writes of {@code render()} (:734-1079), {@code f2} being {@code ageInTicks}; the three head helpers run last.
     * {@code super.render} and {@code setRotationAngles} (:748-749) do nothing in {@code ModelBase}.
     */
    @Override
    public void setupAnim(final TheKing entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.allParts) {
            part.resetPose();
        }
        float newangle = 0.0f;
        final TheKing k = entity;
        float tailspeed = 0.26f;
        float tailamp = 0.08f;
        final float pi4 = 0.7853982f;
        float Lheadlr = 0.0f;
        float Lheadud = 0.0f;
        float Ljawangle = 0.0f;
        float Cheadlr = 0.0f;
        float Cheadud = 0.0f;
        float Cjawangle = 0.0f;
        float Rheadlr = 0.0f;
        float Rheadud = 0.0f;
        float Rjawangle = 0.0f;
        if (k.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.21f;
        }
        else {
            newangle = Mth.cos(f2 * 0.35f * this.wingspeed) * 3.1415927f * 0.15f;
        }
        this.Lwing1.zRot = newangle;
        this.Lwing2.zRot = newangle;
        this.Lwing3.zRot = newangle * 5.0f / 3.0f;
        this.Lwing3.y = this.Lwing1.y + (float)Math.sin(this.Lwing1.zRot) * 84.0f;
        this.Lwing3.x = this.Lwing1.x + (float)Math.cos(this.Lwing1.zRot) * 84.0f;
        this.Lwing4.zRot = this.Lwing3.zRot;
        this.Lwing4.y = this.Lwing3.y;
        this.Lwing4.x = this.Lwing3.x;
        this.Lwing5.zRot = newangle * 7.0f / 3.0f;
        this.Lwing5.y = this.Lwing3.y + (float)Math.sin(this.Lwing3.zRot) * 184.0f;
        this.Lwing5.x = this.Lwing3.x + (float)Math.cos(this.Lwing3.zRot) * 184.0f;
        this.Lwing6.zRot = this.Lwing5.zRot;
        this.Lwing6.y = this.Lwing5.y;
        this.Lwing6.x = this.Lwing5.x;
        final ModelPart lwing7 = this.Lwing7;
        final ModelPart lwing8 = this.Lwing9;
        final float rotationPointY = this.Lwing3.y;
        lwing8.y = rotationPointY;
        lwing7.y = rotationPointY;
        final ModelPart lwing9 = this.Lwing7;
        final ModelPart lwing10 = this.Lwing9;
        final float rotationPointX = this.Lwing3.x;
        lwing10.x = rotationPointX;
        lwing9.x = rotationPointX;
        final ModelPart lwing11 = this.Lwing8;
        final ModelPart lwing12 = this.Lwing10;
        final float rotationPointY2 = this.Lwing3.y;
        lwing12.y = rotationPointY2;
        lwing11.y = rotationPointY2;
        final ModelPart lwing13 = this.Lwing8;
        final ModelPart lwing14 = this.Lwing10;
        final float rotationPointX2 = this.Lwing3.x;
        lwing14.x = rotationPointX2;
        lwing13.x = rotationPointX2;
        final ModelPart lwing15 = this.Lwing7;
        final ModelPart lwing16 = this.Lwing8;
        final float n = 0.261f + this.Lwing3.zRot;
        lwing16.zRot = n;
        lwing15.zRot = n;
        final ModelPart lwing17 = this.Lwing9;
        final ModelPart lwing18 = this.Lwing10;
        final float n2 = -0.261f + this.Lwing3.zRot;
        lwing18.zRot = n2;
        lwing17.zRot = n2;
        this.Rwing1.zRot = -newangle;
        this.Rwing2.zRot = -newangle;
        this.Rwing3.zRot = -newangle * 5.0f / 3.0f;
        this.Rwing3.y = this.Rwing1.y - (float)Math.sin(this.Rwing1.zRot) * 84.0f;
        this.Rwing3.x = this.Rwing1.x - (float)Math.cos(this.Rwing1.zRot) * 84.0f;
        this.Rwing4.zRot = this.Rwing3.zRot;
        this.Rwing4.y = this.Rwing3.y;
        this.Rwing4.x = this.Rwing3.x;
        this.Rwing5.zRot = -newangle * 7.0f / 3.0f;
        this.Rwing5.y = this.Rwing3.y - (float)Math.sin(this.Rwing3.zRot) * 184.0f;
        this.Rwing5.x = this.Rwing3.x - (float)Math.cos(this.Rwing3.zRot) * 184.0f;
        this.Rwing6.zRot = this.Rwing5.zRot;
        this.Rwing6.y = this.Rwing5.y;
        this.Rwing6.x = this.Rwing5.x;
        final ModelPart rwing7 = this.Rwing7;
        final ModelPart rwing8 = this.Rwing9;
        final float rotationPointY3 = this.Rwing3.y;
        rwing8.y = rotationPointY3;
        rwing7.y = rotationPointY3;
        final ModelPart rwing9 = this.Rwing7;
        final ModelPart rwing10 = this.Rwing9;
        final float rotationPointX3 = this.Rwing3.x;
        rwing10.x = rotationPointX3;
        rwing9.x = rotationPointX3;
        final ModelPart rwing11 = this.Rwing8;
        final ModelPart rwing12 = this.Rwing10;
        final float rotationPointY4 = this.Rwing3.y;
        rwing12.y = rotationPointY4;
        rwing11.y = rotationPointY4;
        final ModelPart rwing13 = this.Rwing8;
        final ModelPart rwing14 = this.Rwing10;
        final float rotationPointX4 = this.Rwing3.x;
        rwing14.x = rotationPointX4;
        rwing13.x = rotationPointX4;
        final ModelPart rwing15 = this.Rwing7;
        final ModelPart rwing16 = this.Rwing8;
        final float n3 = -0.261f + this.Rwing3.zRot;
        rwing16.zRot = n3;
        rwing15.zRot = n3;
        final ModelPart rwing17 = this.Rwing9;
        final ModelPart rwing18 = this.Rwing10;
        final float n4 = 0.261f + this.Rwing3.zRot;
        rwing18.zRot = n4;
        rwing17.zRot = n4;
        if (k.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.25f;
        }
        else {
            newangle = 0.0f;
        }
        final ModelPart lClawRear = this.LClawRear;
        final ModelPart rClawRear = this.RClawRear;
        final float n5 = -0.925f + newangle;
        rClawRear.xRot = n5;
        lClawRear.xRot = n5;
        this.LLClaw1.xRot = 0.384f - newangle;
        this.LLClaw2.xRot = 0.645f - newangle;
        this.LCClaw1.xRot = 0.384f - newangle;
        this.LCClaw2.xRot = 0.645f - newangle;
        this.LRClaw1.xRot = 0.384f - newangle;
        this.LRClaw2.xRot = 0.645f - newangle;
        this.RLClaw1.xRot = 0.384f - newangle;
        this.RLClaw2.xRot = 0.645f - newangle;
        this.RCClaw1.xRot = 0.384f - newangle;
        this.RCClaw2.xRot = 0.645f - newangle;
        this.RRClaw1.xRot = 0.384f - newangle;
        this.RRClaw2.xRot = 0.645f - newangle;
        if (k.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 0.6f * this.wingspeed) * 3.1415927f * 0.45f;
        }
        else {
            newangle = 0.0f;
        }
        final ModelPart lThigh = this.LThigh;
        final ModelPart lUpperLeg = this.LUpperLeg;
        final float n6 = 0.785f + newangle / 4.0f;
        lUpperLeg.xRot = n6;
        lThigh.xRot = n6;
        final ModelPart lLowerLeg = this.LLowerLeg;
        final ModelPart lFoot = this.LFoot;
        final float n7 = -0.628f + newangle / 2.0f;
        lFoot.xRot = n7;
        lLowerLeg.xRot = n7;
        final ModelPart lLowerLeg2 = this.LLowerLeg;
        final ModelPart lFoot2 = this.LFoot;
        final float n8 = this.LUpperLeg.y + (float)Math.cos(this.LUpperLeg.xRot) * 50.0f;
        lFoot2.y = n8;
        lLowerLeg2.y = n8;
        final ModelPart lLowerLeg3 = this.LLowerLeg;
        final ModelPart lFoot3 = this.LFoot;
        final float n9 = this.LUpperLeg.z + (float)Math.sin(this.LUpperLeg.xRot) * 50.0f;
        lFoot3.z = n9;
        lLowerLeg3.z = n9;
        final ModelPart llClaw1 = this.LLClaw1;
        final ModelPart llClaw2 = this.LLClaw2;
        final float n10 = this.LLowerLeg.y + (float)Math.cos(this.LLowerLeg.xRot - 0.1f) * 66.0f;
        llClaw2.y = n10;
        llClaw1.y = n10;
        final ModelPart llClaw3 = this.LLClaw1;
        final ModelPart llClaw4 = this.LLClaw2;
        final float n11 = this.LLowerLeg.z + (float)Math.sin(this.LLowerLeg.xRot - 0.1f) * 66.0f;
        llClaw4.z = n11;
        llClaw3.z = n11;
        final ModelPart lcClaw1 = this.LCClaw1;
        final ModelPart lcClaw2 = this.LCClaw2;
        final float rotationPointY5 = this.LLClaw1.y;
        lcClaw2.y = rotationPointY5;
        lcClaw1.y = rotationPointY5;
        final ModelPart lrClaw1 = this.LRClaw1;
        final ModelPart lrClaw2 = this.LRClaw2;
        final float rotationPointY6 = this.LLClaw1.y;
        lrClaw2.y = rotationPointY6;
        lrClaw1.y = rotationPointY6;
        final ModelPart lcClaw3 = this.LCClaw1;
        final ModelPart lcClaw4 = this.LCClaw2;
        final float rotationPointZ = this.LLClaw1.z;
        lcClaw4.z = rotationPointZ;
        lcClaw3.z = rotationPointZ;
        final ModelPart lrClaw3 = this.LRClaw1;
        final ModelPart lrClaw4 = this.LRClaw2;
        final float rotationPointZ2 = this.LLClaw1.z;
        lrClaw4.z = rotationPointZ2;
        lrClaw3.z = rotationPointZ2;
        this.LClawRear.y = this.LLowerLeg.y + (float)Math.cos(this.LLowerLeg.xRot + 0.15f) * 66.0f;
        this.LClawRear.z = this.LLowerLeg.z + (float)Math.sin(this.LLowerLeg.xRot + 0.15f) * 66.0f;
        final ModelPart rThigh = this.RThigh;
        final ModelPart rUpperLeg = this.RUpperLeg;
        final float n12 = 0.785f - newangle / 4.0f;
        rUpperLeg.xRot = n12;
        rThigh.xRot = n12;
        final ModelPart rLowerLeg = this.RLowerLeg;
        final ModelPart rFoot = this.RFoot;
        final float n13 = -0.628f - newangle / 2.0f;
        rFoot.xRot = n13;
        rLowerLeg.xRot = n13;
        final ModelPart rLowerLeg2 = this.RLowerLeg;
        final ModelPart rFoot2 = this.RFoot;
        final float n14 = this.RUpperLeg.y + (float)Math.cos(this.RUpperLeg.xRot) * 50.0f;
        rFoot2.y = n14;
        rLowerLeg2.y = n14;
        final ModelPart rLowerLeg3 = this.RLowerLeg;
        final ModelPart rFoot3 = this.RFoot;
        final float n15 = this.RUpperLeg.z + (float)Math.sin(this.RUpperLeg.xRot) * 50.0f;
        rFoot3.z = n15;
        rLowerLeg3.z = n15;
        final ModelPart rlClaw1 = this.RLClaw1;
        final ModelPart rlClaw2 = this.RLClaw2;
        final float n16 = this.RLowerLeg.y + (float)Math.cos(this.RLowerLeg.xRot - 0.1f) * 66.0f;
        rlClaw2.y = n16;
        rlClaw1.y = n16;
        final ModelPart rlClaw3 = this.RLClaw1;
        final ModelPart rlClaw4 = this.RLClaw2;
        final float n17 = this.RLowerLeg.z + (float)Math.sin(this.RLowerLeg.xRot - 0.1f) * 66.0f;
        rlClaw4.z = n17;
        rlClaw3.z = n17;
        final ModelPart rcClaw1 = this.RCClaw1;
        final ModelPart rcClaw2 = this.RCClaw2;
        final float rotationPointY7 = this.RLClaw1.y;
        rcClaw2.y = rotationPointY7;
        rcClaw1.y = rotationPointY7;
        final ModelPart rrClaw1 = this.RRClaw1;
        final ModelPart rrClaw2 = this.RRClaw2;
        final float rotationPointY8 = this.RLClaw1.y;
        rrClaw2.y = rotationPointY8;
        rrClaw1.y = rotationPointY8;
        final ModelPart rcClaw3 = this.RCClaw1;
        final ModelPart rcClaw4 = this.RCClaw2;
        final float rotationPointZ3 = this.RLClaw1.z;
        rcClaw4.z = rotationPointZ3;
        rcClaw3.z = rotationPointZ3;
        final ModelPart rrClaw3 = this.RRClaw1;
        final ModelPart rrClaw4 = this.RRClaw2;
        final float rotationPointZ4 = this.RLClaw1.z;
        rrClaw4.z = rotationPointZ4;
        rrClaw3.z = rotationPointZ4;
        this.RClawRear.y = this.RLowerLeg.y + (float)Math.cos(this.RLowerLeg.xRot + 0.15f) * 66.0f;
        this.RClawRear.z = this.RLowerLeg.z + (float)Math.sin(this.RLowerLeg.xRot + 0.15f) * 66.0f;
        if (k.getAttacking() != 0) {
            tailspeed = 0.56f;
            tailamp = 0.19f;
        }
        this.Tail1.yRot = Mth.cos(f2 * tailspeed * this.wingspeed) * 3.1415927f * tailamp / 2.0f;
        final ModelPart ridge4 = this.Ridge4;
        final ModelPart ridge5 = this.Ridge5;
        final float rotateAngleY = this.Tail1.yRot;
        ridge5.yRot = rotateAngleY;
        ridge4.yRot = rotateAngleY;
        this.Tail2.z = this.Tail1.z + (float)Math.cos(this.Tail1.yRot) * 54.0f;
        this.Tail2.x = this.Tail1.x - 1.0f + (float)Math.sin(this.Tail1.yRot) * 54.0f;
        this.Tail2.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - pi4) * 3.1415927f * tailamp;
        this.Tail3.z = this.Tail2.z + (float)Math.cos(this.Tail2.yRot) * 42.0f;
        this.Tail3.x = this.Tail2.x + (float)Math.sin(this.Tail2.yRot) * 42.0f;
        this.Tail3.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 2.0f * pi4) * 3.1415927f * tailamp;
        this.Tail4.z = this.Tail3.z + (float)Math.cos(this.Tail3.yRot) * 41.0f;
        this.Tail4.x = this.Tail3.x + (float)Math.sin(this.Tail3.yRot) * 41.0f;
        this.Tail4.yRot = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0f * pi4) * 3.1415927f * tailamp;
        newangle = Mth.cos(f2 * tailspeed * this.wingspeed - 3.0f * pi4) * 3.1415927f * tailamp;
        newangle /= 2.0f;
        this.Tail5.z = this.Tail4.z + (float)Math.cos(this.Tail4.yRot) * 34.0f;
        this.Tail5.x = this.Tail4.x + (float)Math.sin(this.Tail4.yRot) * 34.0f;
        this.Tail5.yRot = this.Tail4.yRot + newangle;
        this.Tail6.z = this.Tail5.z + (float)Math.cos(this.Tail5.yRot) * 34.0f;
        this.Tail6.x = this.Tail5.x + (float)Math.sin(this.Tail5.yRot) * 34.0f;
        this.Tail6.yRot = this.Tail5.yRot + newangle;
        final ModelPart tail7 = this.Tail7;
        final ModelPart ridge6 = this.Ridge6;
        final float n18 = this.Tail6.z + (float)Math.cos(this.Tail6.yRot) * 40.0f;
        ridge6.z = n18;
        tail7.z = n18;
        final ModelPart tail8 = this.Tail7;
        final ModelPart ridge7 = this.Ridge6;
        final float n19 = this.Tail6.x + (float)Math.sin(this.Tail6.yRot) * 40.0f;
        ridge7.x = n19;
        tail8.x = n19;
        final ModelPart tail9 = this.Tail7;
        final ModelPart ridge8 = this.Ridge6;
        final float n20 = this.Tail6.yRot + newangle;
        ridge8.yRot = n20;
        tail9.yRot = n20;
        final ModelPart tailTip = this.TailTip;
        final ModelPart tailTip2 = this.TailTip2;
        final float n21 = this.Tail7.z + (float)Math.cos(this.Tail7.yRot) * 43.0f;
        tailTip2.z = n21;
        tailTip.z = n21;
        final ModelPart tailTip3 = this.TailTip;
        final ModelPart tailTip4 = this.TailTip2;
        final float n22 = this.Tail7.x + (float)Math.sin(this.Tail7.yRot) * 43.0f;
        tailTip4.x = n22;
        tailTip3.x = n22;
        final ModelPart tailTip5 = this.TailTip;
        final ModelPart tailTip6 = this.TailTip2;
        final float n23 = this.Tail7.yRot + newangle;
        tailTip6.yRot = n23;
        tailTip5.yRot = n23;
        this.TailSpike.z = this.TailTip.z + (float)Math.cos(this.TailTip.yRot) * 58.0f;
        this.TailSpike.x = this.TailTip.x + (float)Math.sin(this.TailTip.yRot) * 58.0f;
        this.TailSpike.yRot = this.TailTip.yRot + newangle;
        if (k.getAttacking() != 0) {
            Lheadlr = Mth.sin(f2 * 0.3f * this.wingspeed) * 3.1415927f * 0.25f;
            Lheadud = Mth.sin(f2 * 0.2f * this.wingspeed) * 3.1415927f * 0.25f;
            Ljawangle = Mth.sin(f2 * 0.85f * this.wingspeed) * 3.1415927f * 0.12f;
            Rheadlr = Mth.sin(f2 * 0.32f * this.wingspeed) * 3.1415927f * 0.25f;
            Rheadud = Mth.sin(f2 * 0.21f * this.wingspeed) * 3.1415927f * 0.25f;
            Rjawangle = Mth.sin(f2 * 0.95f * this.wingspeed) * 3.1415927f * 0.12f;
            Cheadlr = Mth.sin(f2 * 0.28f * this.wingspeed) * 3.1415927f * 0.25f;
            Cheadud = Mth.sin(f2 * 0.19f * this.wingspeed) * 3.1415927f * 0.25f;
            Cjawangle = Mth.sin(f2 * 0.75f * this.wingspeed) * 3.1415927f * 0.12f;
            Ljawangle += 0.5f;
            Ljawangle += Lheadud;
            Cjawangle += 0.5f;
            Cjawangle += Cheadud;
            Rjawangle += 0.5f;
            Rjawangle += Rheadud;
        }
        else {
            Lheadlr = Mth.sin(f2 * 0.17f * this.wingspeed) * 3.1415927f * 0.08f;
            Lheadud = Mth.sin(f2 * 0.13f * this.wingspeed) * 3.1415927f * 0.1f;
            Ljawangle = Mth.sin(f2 * 0.45f * this.wingspeed) * 3.1415927f * 0.04f;
            Rheadlr = Mth.sin(f2 * 0.19f * this.wingspeed) * 3.1415927f * 0.08f;
            Rheadud = Mth.sin(f2 * 0.12f * this.wingspeed) * 3.1415927f * 0.1f;
            Rjawangle = Mth.sin(f2 * 0.55f * this.wingspeed) * 3.1415927f * 0.04f;
            Cheadlr = Mth.sin(f2 * 0.13f * this.wingspeed) * 3.1415927f * 0.08f;
            Cheadud = Mth.sin(f2 * 0.08f * this.wingspeed) * 3.1415927f * 0.1f;
            Cjawangle = Mth.sin(f2 * 0.65f * this.wingspeed) * 3.1415927f * 0.04f;
            Ljawangle += 0.25f;
            Ljawangle += Lheadud;
            Cjawangle += 0.25f;
            Cjawangle += Cheadud;
            Rjawangle += 0.25f;
            Rjawangle += Rheadud;
        }
        if (Lheadlr > Cheadlr) {
            Lheadlr = Cheadlr;
        }
        if (Rheadlr < Cheadlr) {
            Rheadlr = Cheadlr;
        }
        this.moveLeftHead(Lheadlr, Lheadud, Ljawangle);
        this.moveCenterHead(Cheadlr, Cheadud, Cjawangle);
        this.moveRightHead(Rheadlr, Rheadud, Rjawangle);
    }

    /** The opaque draw calls of {@code render()} (:1080-1188), in the original order. */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        for (final ModelPart part : this.opaqueOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
    }

    /**
     * The blend block of {@code render()} (:1189-1205): {@code glPushMatrix} without a transform, the ten membranes in
     * {@link #WING_COLOR}. The caller passes a translucent buffer.
     */
    public void renderWings(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                            final int packedOverlay) {
        poseStack.pushPose();
        for (final ModelPart part : this.wingOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, WING_COLOR);
        }
        poseStack.popPose();
    }

    private void moveLeftHead(final float Lheadlr, final float Lheadud, final float Ljawangle) {
        this.LJaw1.xRot = Ljawangle;
        this.LJaw2.xRot = Ljawangle;
        this.LJaw3.xRot = Ljawangle;
        this.LTooth1.xRot = Ljawangle;
        this.LTooth2.xRot = Ljawangle;
        this.LTooth3.xRot = Ljawangle;
        this.LTooth4.xRot = Ljawangle;
        this.NeckL1.yRot = Lheadlr * 0.125f;
        this.NeckL2.z = this.NeckL1.z - (float)Math.cos(this.NeckL1.yRot) * 20.0f;
        this.NeckL2.x = this.NeckL1.x - (float)Math.sin(this.NeckL1.yRot) * 20.0f;
        this.NeckL2.yRot = Lheadlr * 0.25f;
        this.NeckL3.z = this.NeckL2.z - (float)Math.cos(this.NeckL2.yRot) * 36.0f;
        this.NeckL3.x = this.NeckL2.x - (float)Math.sin(this.NeckL2.yRot) * 36.0f;
        this.NeckL3.yRot = Lheadlr * 0.38f;
        this.NeckL4.z = this.NeckL3.z - (float)Math.cos(this.NeckL3.yRot) * 36.0f;
        this.NeckL4.x = this.NeckL3.x - (float)Math.sin(this.NeckL3.yRot) * 36.0f;
        this.NeckL4.yRot = Lheadlr * 0.5f;
        this.LHead1.z = this.NeckL4.z - (float)Math.cos(this.NeckL4.yRot) * 36.0f;
        this.LHead1.x = this.NeckL4.x - (float)Math.sin(this.NeckL4.yRot) * 36.0f;
        this.LHead1.yRot = Lheadlr;
        this.LHead2.yRot = Lheadlr;
        this.LHead2.z = this.LHead1.z;
        this.LHead2.x = this.LHead1.x;
        this.LHead3.yRot = Lheadlr;
        this.LHead3.z = this.LHead1.z;
        this.LHead3.x = this.LHead1.x;
        this.LHeadMane.yRot = Lheadlr;
        this.LHeadMane.z = this.LHead1.z;
        this.LHeadMane.x = this.LHead1.x;
        this.LLEye.yRot = Lheadlr;
        this.LLEye.z = this.LHead1.z;
        this.LLEye.x = this.LHead1.x;
        this.LREye.yRot = Lheadlr;
        this.LREye.z = this.LHead1.z;
        this.LREye.x = this.LHead1.x;
        this.LLNoseSpike.yRot = 0.244f + Lheadlr;
        this.LLNoseSpike.z = this.LHead1.z;
        this.LLNoseSpike.x = this.LHead1.x;
        this.LRNoseSpike.yRot = -0.261f + Lheadlr;
        this.LRNoseSpike.z = this.LHead1.z;
        this.LRNoseSpike.x = this.LHead1.x;
        this.LJaw1.yRot = Lheadlr;
        this.LJaw1.z = this.NeckL4.z - (float)Math.cos(this.NeckL4.yRot) * 37.0f;
        this.LJaw1.x = this.NeckL4.x - (float)Math.sin(this.NeckL4.yRot) * 37.0f;
        this.LJaw2.yRot = Lheadlr;
        this.LJaw2.z = this.LJaw1.z;
        this.LJaw2.x = this.LJaw1.x;
        this.LJaw3.yRot = Lheadlr;
        this.LJaw3.z = this.LJaw1.z;
        this.LJaw3.x = this.LJaw1.x;
        this.LTooth1.yRot = Lheadlr;
        this.LTooth1.z = this.LJaw1.z;
        this.LTooth1.x = this.LJaw1.x;
        this.LTooth2.yRot = Lheadlr;
        this.LTooth2.z = this.LJaw1.z;
        this.LTooth2.x = this.LJaw1.x;
        this.LTooth3.yRot = Lheadlr;
        this.LTooth3.z = this.LJaw1.z;
        this.LTooth3.x = this.LJaw1.x;
        this.LTooth4.yRot = Lheadlr;
        this.LTooth4.z = this.LJaw1.z;
        this.LTooth4.x = this.LJaw1.x;
        this.NeckL1.xRot = Lheadud * 0.125f;
        this.NeckL2.y = this.NeckL1.y + (float)Math.sin(this.NeckL1.xRot) * 20.0f;
        this.NeckL2.z = this.NeckL1.z + (this.NeckL2.z - this.NeckL1.z) * (float)Math.cos(this.NeckL1.xRot);
        this.NeckL2.x = this.NeckL1.x + (this.NeckL2.x - this.NeckL1.x) * (float)Math.cos(this.NeckL1.xRot);
        this.NeckL2.xRot = Lheadud * 0.25f;
        this.NeckL3.y = this.NeckL2.y + (float)Math.sin(this.NeckL2.xRot) * 36.0f;
        this.NeckL3.z = this.NeckL2.z + (this.NeckL3.z - this.NeckL2.z) * (float)Math.cos(this.NeckL2.xRot);
        this.NeckL3.x = this.NeckL2.x + (this.NeckL3.x - this.NeckL2.x) * (float)Math.cos(this.NeckL2.xRot);
        this.NeckL3.xRot = Lheadud * 0.38f;
        this.NeckL4.y = this.NeckL3.y + (float)Math.sin(this.NeckL3.xRot) * 36.0f;
        this.NeckL4.z = this.NeckL3.z + (this.NeckL4.z - this.NeckL3.z) * (float)Math.cos(this.NeckL3.xRot);
        this.NeckL4.x = this.NeckL3.x + (this.NeckL4.x - this.NeckL3.x) * (float)Math.cos(this.NeckL3.xRot);
        this.NeckL4.xRot = Lheadud * 0.5f;
        this.LHead1.y = this.NeckL4.y + (float)Math.sin(this.NeckL4.xRot) * 36.0f;
        this.LHead1.z = this.NeckL4.z + (this.LHead1.z - this.NeckL4.z) * (float)Math.cos(this.NeckL4.xRot);
        this.LHead1.x = this.NeckL4.x + (this.LHead1.x - this.NeckL4.x) * (float)Math.cos(this.NeckL4.xRot);
        this.LHead1.xRot = Lheadud;
        this.LHead2.xRot = Lheadud;
        this.LHead2.z = this.LHead1.z;
        this.LHead2.x = this.LHead1.x;
        this.LHead2.y = this.LHead1.y;
        this.LHead3.xRot = Lheadud;
        this.LHead3.z = this.LHead1.z;
        this.LHead3.x = this.LHead1.x;
        this.LHead3.y = this.LHead1.y;
        this.LHeadMane.xRot = 0.384f + Lheadud;
        this.LHeadMane.z = this.LHead1.z;
        this.LHeadMane.x = this.LHead1.x;
        this.LHeadMane.y = this.LHead1.y;
        this.LLEye.xRot = Lheadud;
        this.LLEye.z = this.LHead1.z;
        this.LLEye.x = this.LHead1.x;
        this.LLEye.y = this.LHead1.y;
        this.LREye.xRot = Lheadud;
        this.LREye.z = this.LHead1.z;
        this.LREye.x = this.LHead1.x;
        this.LREye.y = this.LHead1.y;
        this.LLNoseSpike.xRot = 0.244f + Lheadud;
        this.LLNoseSpike.z = this.LHead1.z;
        this.LLNoseSpike.x = this.LHead1.x;
        this.LLNoseSpike.y = this.LHead1.y;
        this.LRNoseSpike.xRot = 0.261f + Lheadud;
        this.LRNoseSpike.z = this.LHead1.z;
        this.LRNoseSpike.x = this.LHead1.x;
        this.LRNoseSpike.y = this.LHead1.y;
        this.LJaw1.y = this.LHead1.y + (float)Math.cos(this.LHead1.xRot) * 14.0f;
        this.LJaw1.z = this.NeckL4.z + (this.LJaw1.z - this.NeckL4.z) * (float)Math.cos(this.NeckL4.xRot);
        final ModelPart lJaw1 = this.LJaw1;
        lJaw1.z += (float)Math.sin(this.LHead1.xRot) * 14.0f;
        this.LJaw1.x = this.NeckL4.x + (this.LJaw1.x - this.NeckL4.x) * (float)Math.cos(this.NeckL4.xRot);
        this.LJaw2.z = this.LJaw1.z;
        this.LJaw2.x = this.LJaw1.x;
        this.LJaw2.y = this.LJaw1.y;
        this.LJaw3.z = this.LJaw1.z;
        this.LJaw3.x = this.LJaw1.x;
        this.LJaw3.y = this.LJaw1.y;
        this.LTooth1.z = this.LJaw1.z;
        this.LTooth1.x = this.LJaw1.x;
        this.LTooth1.y = this.LJaw1.y;
        this.LTooth2.z = this.LJaw1.z;
        this.LTooth2.x = this.LJaw1.x;
        this.LTooth2.y = this.LJaw1.y;
        this.LTooth3.z = this.LJaw1.z;
        this.LTooth3.x = this.LJaw1.x;
        this.LTooth3.y = this.LJaw1.y;
        this.LTooth4.z = this.LJaw1.z;
        this.LTooth4.x = this.LJaw1.x;
        this.LTooth4.y = this.LJaw1.y;
    }
    
    private void moveCenterHead(final float Cheadlr, final float Cheadud, final float Cjawangle) {
        this.CJaw1.xRot = Cjawangle;
        this.CJaw2.xRot = Cjawangle;
        this.CJaw3.xRot = Cjawangle;
        this.CTooth1.xRot = Cjawangle;
        this.CTooth2.xRot = Cjawangle;
        this.CTooth3.xRot = Cjawangle;
        this.CTooth4.xRot = Cjawangle;
        this.NeckC1.yRot = Cheadlr * 0.125f;
        this.NeckC2.z = this.NeckC1.z - (float)Math.cos(this.NeckC1.yRot) * 20.0f;
        this.NeckC2.x = this.NeckC1.x - (float)Math.sin(this.NeckC1.yRot) * 20.0f;
        this.NeckC2.yRot = Cheadlr * 0.25f;
        this.NeckC3.z = this.NeckC2.z - (float)Math.cos(this.NeckC2.yRot) * 36.0f;
        this.NeckC3.x = this.NeckC2.x - (float)Math.sin(this.NeckC2.yRot) * 36.0f;
        this.NeckC3.yRot = Cheadlr * 0.38f;
        this.NeckC4.z = this.NeckC3.z - (float)Math.cos(this.NeckC3.yRot) * 36.0f;
        this.NeckC4.x = this.NeckC3.x - (float)Math.sin(this.NeckC3.yRot) * 36.0f;
        this.NeckC4.yRot = Cheadlr * 0.5f;
        this.CHead1.z = this.NeckC4.z - (float)Math.cos(this.NeckC4.yRot) * 36.0f;
        this.CHead1.x = this.NeckC4.x - (float)Math.sin(this.NeckC4.yRot) * 36.0f;
        this.CHead1.yRot = Cheadlr;
        this.CHead2.yRot = Cheadlr;
        this.CHead2.z = this.CHead1.z;
        this.CHead2.x = this.CHead1.x;
        this.CHead3.yRot = Cheadlr;
        this.CHead3.z = this.CHead1.z;
        this.CHead3.x = this.CHead1.x;
        this.CHeadMane.yRot = Cheadlr;
        this.CHeadMane.z = this.CHead1.z;
        this.CHeadMane.x = this.CHead1.x;
        this.CLEye.yRot = Cheadlr;
        this.CLEye.z = this.CHead1.z;
        this.CLEye.x = this.CHead1.x;
        this.CREye.yRot = Cheadlr;
        this.CREye.z = this.CHead1.z;
        this.CREye.x = this.CHead1.x;
        this.CLNoseSpike.yRot = 0.244f + Cheadlr;
        this.CLNoseSpike.z = this.CHead1.z;
        this.CLNoseSpike.x = this.CHead1.x;
        this.CRNoseSpike.yRot = -0.261f + Cheadlr;
        this.CRNoseSpike.z = this.CHead1.z;
        this.CRNoseSpike.x = this.CHead1.x;
        this.CJaw1.yRot = Cheadlr;
        this.CJaw1.z = this.NeckC4.z - (float)Math.cos(this.NeckC4.yRot) * 37.0f;
        this.CJaw1.x = this.NeckC4.x - (float)Math.sin(this.NeckC4.yRot) * 37.0f;
        this.CJaw2.yRot = Cheadlr;
        this.CJaw2.z = this.CJaw1.z;
        this.CJaw2.x = this.CJaw1.x;
        this.CJaw3.yRot = Cheadlr;
        this.CJaw3.z = this.CJaw1.z;
        this.CJaw3.x = this.CJaw1.x;
        this.CTooth1.yRot = Cheadlr;
        this.CTooth1.z = this.CJaw1.z;
        this.CTooth1.x = this.CJaw1.x;
        this.CTooth2.yRot = Cheadlr;
        this.CTooth2.z = this.CJaw1.z;
        this.CTooth2.x = this.CJaw1.x;
        this.CTooth3.yRot = Cheadlr;
        this.CTooth3.z = this.CJaw1.z;
        this.CTooth3.x = this.CJaw1.x;
        this.CTooth4.yRot = Cheadlr;
        this.CTooth4.z = this.CJaw1.z;
        this.CTooth4.x = this.CJaw1.x;
        this.NeckC1.xRot = Cheadud * 0.125f;
        this.NeckC2.y = this.NeckC1.y + (float)Math.sin(this.NeckC1.xRot) * 20.0f;
        this.NeckC2.z = this.NeckC1.z + (this.NeckC2.z - this.NeckC1.z) * (float)Math.cos(this.NeckC1.xRot);
        this.NeckC2.x = this.NeckC1.x + (this.NeckC2.x - this.NeckC1.x) * (float)Math.cos(this.NeckC1.xRot);
        this.NeckC2.xRot = Cheadud * 0.25f;
        this.NeckC3.y = this.NeckC2.y + (float)Math.sin(this.NeckC2.xRot) * 36.0f;
        this.NeckC3.z = this.NeckC2.z + (this.NeckC3.z - this.NeckC2.z) * (float)Math.cos(this.NeckC2.xRot);
        this.NeckC3.x = this.NeckC2.x + (this.NeckC3.x - this.NeckC2.x) * (float)Math.cos(this.NeckC2.xRot);
        this.NeckC3.xRot = Cheadud * 0.38f;
        this.NeckC4.y = this.NeckC3.y + (float)Math.sin(this.NeckC3.xRot) * 36.0f;
        this.NeckC4.z = this.NeckC3.z + (this.NeckC4.z - this.NeckC3.z) * (float)Math.cos(this.NeckC3.xRot);
        this.NeckC4.x = this.NeckC3.x + (this.NeckC4.x - this.NeckC3.x) * (float)Math.cos(this.NeckC3.xRot);
        this.NeckC4.xRot = Cheadud * 0.5f;
        this.CHead1.y = this.NeckC4.y + (float)Math.sin(this.NeckC4.xRot) * 36.0f;
        this.CHead1.z = this.NeckC4.z + (this.CHead1.z - this.NeckC4.z) * (float)Math.cos(this.NeckC4.xRot);
        this.CHead1.x = this.NeckC4.x + (this.CHead1.x - this.NeckC4.x) * (float)Math.cos(this.NeckC4.xRot);
        this.CHead1.xRot = Cheadud;
        this.CHead2.xRot = Cheadud;
        this.CHead2.z = this.CHead1.z;
        this.CHead2.x = this.CHead1.x;
        this.CHead2.y = this.CHead1.y;
        this.CHead3.xRot = Cheadud;
        this.CHead3.z = this.CHead1.z;
        this.CHead3.x = this.CHead1.x;
        this.CHead3.y = this.CHead1.y;
        this.CHeadMane.xRot = 0.384f + Cheadud;
        this.CHeadMane.z = this.CHead1.z;
        this.CHeadMane.x = this.CHead1.x;
        this.CHeadMane.y = this.CHead1.y;
        this.CLEye.xRot = Cheadud;
        this.CLEye.z = this.CHead1.z;
        this.CLEye.x = this.CHead1.x;
        this.CLEye.y = this.CHead1.y;
        this.CREye.xRot = Cheadud;
        this.CREye.z = this.CHead1.z;
        this.CREye.x = this.CHead1.x;
        this.CREye.y = this.CHead1.y;
        this.CLNoseSpike.xRot = 0.244f + Cheadud;
        this.CLNoseSpike.z = this.CHead1.z;
        this.CLNoseSpike.x = this.CHead1.x;
        this.CLNoseSpike.y = this.CHead1.y;
        this.CRNoseSpike.xRot = 0.261f + Cheadud;
        this.CRNoseSpike.z = this.CHead1.z;
        this.CRNoseSpike.x = this.CHead1.x;
        this.CRNoseSpike.y = this.CHead1.y;
        this.CJaw1.y = this.CHead1.y + (float)Math.cos(this.CHead1.xRot) * 14.0f;
        this.CJaw1.z = this.NeckC4.z + (this.CJaw1.z - this.NeckC4.z) * (float)Math.cos(this.NeckC4.xRot);
        final ModelPart cJaw1 = this.CJaw1;
        cJaw1.z += (float)Math.sin(this.CHead1.xRot) * 14.0f;
        this.CJaw1.x = this.NeckC4.x + (this.CJaw1.x - this.NeckC4.x) * (float)Math.cos(this.NeckC4.xRot);
        this.CJaw2.z = this.CJaw1.z;
        this.CJaw2.x = this.CJaw1.x;
        this.CJaw2.y = this.CJaw1.y;
        this.CJaw3.z = this.CJaw1.z;
        this.CJaw3.x = this.CJaw1.x;
        this.CJaw3.y = this.CJaw1.y;
        this.CTooth1.z = this.CJaw1.z;
        this.CTooth1.x = this.CJaw1.x;
        this.CTooth1.y = this.CJaw1.y;
        this.CTooth2.z = this.CJaw1.z;
        this.CTooth2.x = this.CJaw1.x;
        this.CTooth2.y = this.CJaw1.y;
        this.CTooth3.z = this.CJaw1.z;
        this.CTooth3.x = this.CJaw1.x;
        this.CTooth3.y = this.CJaw1.y;
        this.CTooth4.z = this.CJaw1.z;
        this.CTooth4.x = this.CJaw1.x;
        this.CTooth4.y = this.CJaw1.y;
    }
    
    private void moveRightHead(final float Rheadlr, final float Rheadud, final float Rjawangle) {
        this.RJaw1.xRot = Rjawangle;
        this.RJaw2.xRot = Rjawangle;
        this.RJaw3.xRot = Rjawangle;
        this.RTooth1.xRot = Rjawangle;
        this.RTooth2.xRot = Rjawangle;
        this.RTooth3.xRot = Rjawangle;
        this.RTooth4.xRot = Rjawangle;
        this.NeckR1.yRot = Rheadlr * 0.125f;
        this.NeckR2.z = this.NeckR1.z - (float)Math.cos(this.NeckR1.yRot) * 20.0f;
        this.NeckR2.x = this.NeckR1.x - (float)Math.sin(this.NeckR1.yRot) * 20.0f;
        this.NeckR2.yRot = Rheadlr * 0.25f;
        this.NeckR3.z = this.NeckR2.z - (float)Math.cos(this.NeckR2.yRot) * 36.0f;
        this.NeckR3.x = this.NeckR2.x - (float)Math.sin(this.NeckR2.yRot) * 36.0f;
        this.NeckR3.yRot = Rheadlr * 0.38f;
        this.NeckR4.z = this.NeckR3.z - (float)Math.cos(this.NeckR3.yRot) * 36.0f;
        this.NeckR4.x = this.NeckR3.x - (float)Math.sin(this.NeckR3.yRot) * 36.0f;
        this.NeckR4.yRot = Rheadlr * 0.5f;
        this.RHead1.z = this.NeckR4.z - (float)Math.cos(this.NeckR4.yRot) * 36.0f;
        this.RHead1.x = this.NeckR4.x - (float)Math.sin(this.NeckR4.yRot) * 36.0f;
        this.RHead1.yRot = Rheadlr;
        this.RHead2.yRot = Rheadlr;
        this.RHead2.z = this.RHead1.z;
        this.RHead2.x = this.RHead1.x;
        this.RHead3.yRot = Rheadlr;
        this.RHead3.z = this.RHead1.z;
        this.RHead3.x = this.RHead1.x;
        this.RHeadMane.yRot = Rheadlr;
        this.RHeadMane.z = this.RHead1.z;
        this.RHeadMane.x = this.RHead1.x;
        this.RLEye.yRot = Rheadlr;
        this.RLEye.z = this.RHead1.z;
        this.RLEye.x = this.RHead1.x;
        this.RREye.yRot = Rheadlr;
        this.RREye.z = this.RHead1.z;
        this.RREye.x = this.RHead1.x;
        this.RLNoseSpike.yRot = 0.244f + Rheadlr;
        this.RLNoseSpike.z = this.RHead1.z;
        this.RLNoseSpike.x = this.RHead1.x;
        this.RRNoseSpike.yRot = -0.261f + Rheadlr;
        this.RRNoseSpike.z = this.RHead1.z;
        this.RRNoseSpike.x = this.RHead1.x;
        this.RJaw1.yRot = Rheadlr;
        this.RJaw1.z = this.NeckR4.z - (float)Math.cos(this.NeckR4.yRot) * 37.0f;
        this.RJaw1.x = this.NeckR4.x - (float)Math.sin(this.NeckR4.yRot) * 37.0f;
        this.RJaw2.yRot = Rheadlr;
        this.RJaw2.z = this.RJaw1.z;
        this.RJaw2.x = this.RJaw1.x;
        this.RJaw3.yRot = Rheadlr;
        this.RJaw3.z = this.RJaw1.z;
        this.RJaw3.x = this.RJaw1.x;
        this.RTooth1.yRot = Rheadlr;
        this.RTooth1.z = this.RJaw1.z;
        this.RTooth1.x = this.RJaw1.x;
        this.RTooth2.yRot = Rheadlr;
        this.RTooth2.z = this.RJaw1.z;
        this.RTooth2.x = this.RJaw1.x;
        this.RTooth3.yRot = Rheadlr;
        this.RTooth3.z = this.RJaw1.z;
        this.RTooth3.x = this.RJaw1.x;
        this.RTooth4.yRot = Rheadlr;
        this.RTooth4.z = this.RJaw1.z;
        this.RTooth4.x = this.RJaw1.x;
        this.NeckR1.xRot = Rheadud * 0.125f;
        this.NeckR2.y = this.NeckR1.y + (float)Math.sin(this.NeckR1.xRot) * 20.0f;
        this.NeckR2.z = this.NeckR1.z + (this.NeckR2.z - this.NeckR1.z) * (float)Math.cos(this.NeckR1.xRot);
        this.NeckR2.x = this.NeckR1.x + (this.NeckR2.x - this.NeckR1.x) * (float)Math.cos(this.NeckR1.xRot);
        this.NeckR2.xRot = Rheadud * 0.25f;
        this.NeckR3.y = this.NeckR2.y + (float)Math.sin(this.NeckR2.xRot) * 36.0f;
        this.NeckR3.z = this.NeckR2.z + (this.NeckR3.z - this.NeckR2.z) * (float)Math.cos(this.NeckR2.xRot);
        this.NeckR3.x = this.NeckR2.x + (this.NeckR3.x - this.NeckR2.x) * (float)Math.cos(this.NeckR2.xRot);
        this.NeckR3.xRot = Rheadud * 0.38f;
        this.NeckR4.y = this.NeckR3.y + (float)Math.sin(this.NeckR3.xRot) * 36.0f;
        this.NeckR4.z = this.NeckR3.z + (this.NeckR4.z - this.NeckR3.z) * (float)Math.cos(this.NeckR3.xRot);
        this.NeckR4.x = this.NeckR3.x + (this.NeckR4.x - this.NeckR3.x) * (float)Math.cos(this.NeckR3.xRot);
        this.NeckR4.xRot = Rheadud * 0.5f;
        this.RHead1.y = this.NeckR4.y + (float)Math.sin(this.NeckR4.xRot) * 36.0f;
        this.RHead1.z = this.NeckR4.z + (this.RHead1.z - this.NeckR4.z) * (float)Math.cos(this.NeckR4.xRot);
        this.RHead1.x = this.NeckR4.x + (this.RHead1.x - this.NeckR4.x) * (float)Math.cos(this.NeckR4.xRot);
        this.RHead1.xRot = Rheadud;
        this.RHead2.xRot = Rheadud;
        this.RHead2.z = this.RHead1.z;
        this.RHead2.x = this.RHead1.x;
        this.RHead2.y = this.RHead1.y;
        this.RHead3.xRot = Rheadud;
        this.RHead3.z = this.RHead1.z;
        this.RHead3.x = this.RHead1.x;
        this.RHead3.y = this.RHead1.y;
        this.RHeadMane.xRot = 0.384f + Rheadud;
        this.RHeadMane.z = this.RHead1.z;
        this.RHeadMane.x = this.RHead1.x;
        this.RHeadMane.y = this.RHead1.y;
        this.RLEye.xRot = Rheadud;
        this.RLEye.z = this.RHead1.z;
        this.RLEye.x = this.RHead1.x;
        this.RLEye.y = this.RHead1.y;
        this.RREye.xRot = Rheadud;
        this.RREye.z = this.RHead1.z;
        this.RREye.x = this.RHead1.x;
        this.RREye.y = this.RHead1.y;
        this.RLNoseSpike.xRot = 0.244f + Rheadud;
        this.RLNoseSpike.z = this.RHead1.z;
        this.RLNoseSpike.x = this.RHead1.x;
        this.RLNoseSpike.y = this.RHead1.y;
        this.RRNoseSpike.xRot = 0.261f + Rheadud;
        this.RRNoseSpike.z = this.RHead1.z;
        this.RRNoseSpike.x = this.RHead1.x;
        this.RRNoseSpike.y = this.RHead1.y;
        this.RJaw1.y = this.RHead1.y + (float)Math.cos(this.RHead1.xRot) * 14.0f;
        this.RJaw1.z = this.NeckR4.z + (this.RJaw1.z - this.NeckR4.z) * (float)Math.cos(this.NeckR4.xRot);
        final ModelPart rJaw1 = this.RJaw1;
        rJaw1.z += (float)Math.sin(this.RHead1.xRot) * 14.0f;
        this.RJaw1.x = this.NeckR4.x + (this.RJaw1.x - this.NeckR4.x) * (float)Math.cos(this.NeckR4.xRot);
        this.RJaw2.z = this.RJaw1.z;
        this.RJaw2.x = this.RJaw1.x;
        this.RJaw2.y = this.RJaw1.y;
        this.RJaw3.z = this.RJaw1.z;
        this.RJaw3.x = this.RJaw1.x;
        this.RJaw3.y = this.RJaw1.y;
        this.RTooth1.z = this.RJaw1.z;
        this.RTooth1.x = this.RJaw1.x;
        this.RTooth1.y = this.RJaw1.y;
        this.RTooth2.z = this.RJaw1.z;
        this.RTooth2.x = this.RJaw1.x;
        this.RTooth2.y = this.RJaw1.y;
        this.RTooth3.z = this.RJaw1.z;
        this.RTooth3.x = this.RJaw1.x;
        this.RTooth3.y = this.RJaw1.y;
        this.RTooth4.z = this.RJaw1.z;
        this.RTooth4.x = this.RJaw1.x;
        this.RTooth4.y = this.RJaw1.y;
    }
}
