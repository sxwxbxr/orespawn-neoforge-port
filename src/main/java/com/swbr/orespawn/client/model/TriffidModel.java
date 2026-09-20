package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.TriffidGeometry;
import com.swbr.orespawn.entity.triffid.Triffid;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelTriffid} (ModelTriffid.java:8-1453): 178 boxes, 532x715 texture, geometry
 * {@link TriffidGeometry}. {@code render()} (:1087-1418) splits into {@link #setupAnim} (the four leaf chains through
 * the helpers {@code leafpartA..D}, the tongue chain {@code t15..t1}) and {@link #renderToBuffer}, which carries the one
 * GL transform of the model: {@code glRotatef(-90, 0, 1, 0)} around every draw call (:1234-1238, :1417) as a
 * {@link PoseStack} rotation (R8). {@code glEnable(GL_NORMALIZE)} (:1235) and the two zero translations have no effect
 * and no counterpart.
 *
 * <p>The leaves open with DataWatcher 21 ({@link Triffid#getOpenClosed()}), the tongue lashes with DataWatcher 20
 * ({@link Triffid#getAttacking()}). {@code RenderInfo} is not read by this model.
 */
public class TriffidModel extends EntityModel<Triffid> {

    /** Register with {@code TriffidGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "triffid"), "main");

    /** {@code ModelTriffid(float f1)}: {@code wingspeed = f1} (:191-192); ClientProxyOreSpawn.java:89 passes 1.0. */
    private final float wingspeed;

    private final ModelPart r9;
    private final ModelPart b14;
    private final ModelPart base;
    private final ModelPart b3;
    private final ModelPart l57;
    private final ModelPart l30;
    private final ModelPart b6;
    private final ModelPart b7;
    private final ModelPart b8;
    private final ModelPart b9;
    private final ModelPart b11;
    private final ModelPart b16;
    private final ModelPart h18;
    private final ModelPart b13;
    private final ModelPart b15;
    private final ModelPart h8;
    private final ModelPart h1;
    private final ModelPart h13;
    private final ModelPart h7;
    private final ModelPart h3;
    private final ModelPart h17;
    private final ModelPart h16;
    private final ModelPart h23;
    private final ModelPart h4;
    private final ModelPart h2;
    private final ModelPart h21;
    private final ModelPart h19;
    private final ModelPart h20;
    private final ModelPart b10;
    private final ModelPart b17;
    private final ModelPart h6;
    private final ModelPart h11;
    private final ModelPart h14;
    private final ModelPart h15;
    private final ModelPart h10;
    private final ModelPart h9;
    private final ModelPart h5;
    private final ModelPart h12;
    private final ModelPart c2;
    private final ModelPart c11;
    private final ModelPart c1;
    private final ModelPart c5;
    private final ModelPart c3;
    private final ModelPart c4;
    private final ModelPart c10;
    private final ModelPart c6;
    private final ModelPart c7;
    private final ModelPart c8;
    private final ModelPart c9;
    private final ModelPart b1;
    private final ModelPart l15;
    private final ModelPart b2;
    private final ModelPart l43;
    private final ModelPart l1;
    private final ModelPart l2;
    private final ModelPart l3;
    private final ModelPart leaf3;
    private final ModelPart l4;
    private final ModelPart l5;
    private final ModelPart l6;
    private final ModelPart l7;
    private final ModelPart l8;
    private final ModelPart l9;
    private final ModelPart l10;
    private final ModelPart l11;
    private final ModelPart l12;
    private final ModelPart l13;
    private final ModelPart l14;
    private final ModelPart b4;
    private final ModelPart l31;
    private final ModelPart l32;
    private final ModelPart leaf32;
    private final ModelPart l33;
    private final ModelPart l34;
    private final ModelPart l35;
    private final ModelPart l36;
    private final ModelPart l37;
    private final ModelPart l38;
    private final ModelPart l39;
    private final ModelPart l40;
    private final ModelPart l41;
    private final ModelPart l42;
    private final ModelPart l17;
    private final ModelPart l18;
    private final ModelPart l19;
    private final ModelPart l20;
    private final ModelPart l21;
    private final ModelPart l22;
    private final ModelPart l23;
    private final ModelPart l24;
    private final ModelPart l25;
    private final ModelPart l26;
    private final ModelPart l27;
    private final ModelPart l28;
    private final ModelPart l29;
    private final ModelPart b5;
    private final ModelPart l45;
    private final ModelPart l46;
    private final ModelPart l47;
    private final ModelPart l48;
    private final ModelPart l49;
    private final ModelPart leaf49;
    private final ModelPart l50;
    private final ModelPart l51;
    private final ModelPart l52;
    private final ModelPart l53;
    private final ModelPart l54;
    private final ModelPart l55;
    private final ModelPart l56;
    private final ModelPart h22;
    private final ModelPart t15;
    private final ModelPart t14;
    private final ModelPart t13;
    private final ModelPart t12;
    private final ModelPart t11;
    private final ModelPart t10;
    private final ModelPart t9;
    private final ModelPart t6;
    private final ModelPart t2;
    private final ModelPart t8;
    private final ModelPart t7;
    private final ModelPart t5;
    private final ModelPart t4;
    private final ModelPart t3;
    private final ModelPart t1;
    private final ModelPart r47;
    private final ModelPart r2;
    private final ModelPart r6;
    private final ModelPart r5;
    private final ModelPart r10;
    private final ModelPart r7;
    private final ModelPart r12;
    private final ModelPart r8;
    private final ModelPart r11;
    private final ModelPart r4;
    private final ModelPart r40;
    private final ModelPart r45;
    private final ModelPart r49;
    private final ModelPart r44;
    private final ModelPart root43;
    private final ModelPart r43;
    private final ModelPart r46;
    private final ModelPart r48;
    private final ModelPart r35;
    private final ModelPart r38;
    private final ModelPart r42;
    private final ModelPart r39;
    private final ModelPart r41;
    private final ModelPart r18;
    private final ModelPart r3;
    private final ModelPart r50;
    private final ModelPart r31;
    private final ModelPart r36;
    private final ModelPart r37;
    private final ModelPart r22;
    private final ModelPart r30;
    private final ModelPart r33;
    private final ModelPart r34;
    private final ModelPart r29;
    private final ModelPart r20;
    private final ModelPart r24;
    private final ModelPart r28;
    private final ModelPart r26;
    private final ModelPart r25;
    private final ModelPart r27;
    private final ModelPart r23;
    private final ModelPart r21;
    private final ModelPart r1;
    private final ModelPart r13;
    private final ModelPart r16;
    private final ModelPart r19;
    private final ModelPart r15;
    private final ModelPart r14;
    private final ModelPart r17;
    private final ModelPart r32;
    private final ModelPart l16;
    private final ModelPart l44;
    private final ModelPart root;
    /** The draw order of {@code render()} (:1239-1416). */
    private final ModelPart[] drawOrder;

    public TriffidModel(final ModelPart root, final float f1) {
        // RendererLivingEntity drew with back-face culling off and the alpha test on: cutout, no cull.
        super(RenderType::entityCutoutNoCull);
        this.wingspeed = f1;
        this.r9 = root.getChild(TriffidGeometry.R9);
        this.b14 = root.getChild(TriffidGeometry.B14);
        this.base = root.getChild(TriffidGeometry.BASE);
        this.b3 = root.getChild(TriffidGeometry.B3);
        this.l57 = root.getChild(TriffidGeometry.L57);
        this.l30 = root.getChild(TriffidGeometry.L30);
        this.b6 = root.getChild(TriffidGeometry.B6);
        this.b7 = root.getChild(TriffidGeometry.B7);
        this.b8 = root.getChild(TriffidGeometry.B8);
        this.b9 = root.getChild(TriffidGeometry.B9);
        this.b11 = root.getChild(TriffidGeometry.B11);
        this.b16 = root.getChild(TriffidGeometry.B16);
        this.h18 = root.getChild(TriffidGeometry.H18);
        this.b13 = root.getChild(TriffidGeometry.B13);
        this.b15 = root.getChild(TriffidGeometry.B15);
        this.h8 = root.getChild(TriffidGeometry.H8);
        this.h1 = root.getChild(TriffidGeometry.H1);
        this.h13 = root.getChild(TriffidGeometry.H13);
        this.h7 = root.getChild(TriffidGeometry.H7);
        this.h3 = root.getChild(TriffidGeometry.H3);
        this.h17 = root.getChild(TriffidGeometry.H17);
        this.h16 = root.getChild(TriffidGeometry.H16);
        this.h23 = root.getChild(TriffidGeometry.H23);
        this.h4 = root.getChild(TriffidGeometry.H4);
        this.h2 = root.getChild(TriffidGeometry.H2);
        this.h21 = root.getChild(TriffidGeometry.H21);
        this.h19 = root.getChild(TriffidGeometry.H19);
        this.h20 = root.getChild(TriffidGeometry.H20);
        this.b10 = root.getChild(TriffidGeometry.B10);
        this.b17 = root.getChild(TriffidGeometry.B17);
        this.h6 = root.getChild(TriffidGeometry.H6);
        this.h11 = root.getChild(TriffidGeometry.H11);
        this.h14 = root.getChild(TriffidGeometry.H14);
        this.h15 = root.getChild(TriffidGeometry.H15);
        this.h10 = root.getChild(TriffidGeometry.H10);
        this.h9 = root.getChild(TriffidGeometry.H9);
        this.h5 = root.getChild(TriffidGeometry.H5);
        this.h12 = root.getChild(TriffidGeometry.H12);
        this.c2 = root.getChild(TriffidGeometry.C2);
        this.c11 = root.getChild(TriffidGeometry.C11);
        this.c1 = root.getChild(TriffidGeometry.C1);
        this.c5 = root.getChild(TriffidGeometry.C5);
        this.c3 = root.getChild(TriffidGeometry.C3);
        this.c4 = root.getChild(TriffidGeometry.C4);
        this.c10 = root.getChild(TriffidGeometry.C10);
        this.c6 = root.getChild(TriffidGeometry.C6);
        this.c7 = root.getChild(TriffidGeometry.C7);
        this.c8 = root.getChild(TriffidGeometry.C8);
        this.c9 = root.getChild(TriffidGeometry.C9);
        this.b1 = root.getChild(TriffidGeometry.B1);
        this.l15 = root.getChild(TriffidGeometry.L15);
        this.b2 = root.getChild(TriffidGeometry.B2);
        this.l43 = root.getChild(TriffidGeometry.L43);
        this.l1 = root.getChild(TriffidGeometry.L1);
        this.l2 = root.getChild(TriffidGeometry.L2);
        this.l3 = root.getChild(TriffidGeometry.L3);
        this.leaf3 = root.getChild(TriffidGeometry.LEAF3);
        this.l4 = root.getChild(TriffidGeometry.L4);
        this.l5 = root.getChild(TriffidGeometry.L5);
        this.l6 = root.getChild(TriffidGeometry.L6);
        this.l7 = root.getChild(TriffidGeometry.L7);
        this.l8 = root.getChild(TriffidGeometry.L8);
        this.l9 = root.getChild(TriffidGeometry.L9);
        this.l10 = root.getChild(TriffidGeometry.L10);
        this.l11 = root.getChild(TriffidGeometry.L11);
        this.l12 = root.getChild(TriffidGeometry.L12);
        this.l13 = root.getChild(TriffidGeometry.L13);
        this.l14 = root.getChild(TriffidGeometry.L14);
        this.b4 = root.getChild(TriffidGeometry.B4);
        this.l31 = root.getChild(TriffidGeometry.L31);
        this.l32 = root.getChild(TriffidGeometry.L32);
        this.leaf32 = root.getChild(TriffidGeometry.LEAF32);
        this.l33 = root.getChild(TriffidGeometry.L33);
        this.l34 = root.getChild(TriffidGeometry.L34);
        this.l35 = root.getChild(TriffidGeometry.L35);
        this.l36 = root.getChild(TriffidGeometry.L36);
        this.l37 = root.getChild(TriffidGeometry.L37);
        this.l38 = root.getChild(TriffidGeometry.L38);
        this.l39 = root.getChild(TriffidGeometry.L39);
        this.l40 = root.getChild(TriffidGeometry.L40);
        this.l41 = root.getChild(TriffidGeometry.L41);
        this.l42 = root.getChild(TriffidGeometry.L42);
        this.l17 = root.getChild(TriffidGeometry.L17);
        this.l18 = root.getChild(TriffidGeometry.L18);
        this.l19 = root.getChild(TriffidGeometry.L19);
        this.l20 = root.getChild(TriffidGeometry.L20);
        this.l21 = root.getChild(TriffidGeometry.L21);
        this.l22 = root.getChild(TriffidGeometry.L22);
        this.l23 = root.getChild(TriffidGeometry.L23);
        this.l24 = root.getChild(TriffidGeometry.L24);
        this.l25 = root.getChild(TriffidGeometry.L25);
        this.l26 = root.getChild(TriffidGeometry.L26);
        this.l27 = root.getChild(TriffidGeometry.L27);
        this.l28 = root.getChild(TriffidGeometry.L28);
        this.l29 = root.getChild(TriffidGeometry.L29);
        this.b5 = root.getChild(TriffidGeometry.B5);
        this.l45 = root.getChild(TriffidGeometry.L45);
        this.l46 = root.getChild(TriffidGeometry.L46);
        this.l47 = root.getChild(TriffidGeometry.L47);
        this.l48 = root.getChild(TriffidGeometry.L48);
        this.l49 = root.getChild(TriffidGeometry.L49);
        this.leaf49 = root.getChild(TriffidGeometry.LEAF49);
        this.l50 = root.getChild(TriffidGeometry.L50);
        this.l51 = root.getChild(TriffidGeometry.L51);
        this.l52 = root.getChild(TriffidGeometry.L52);
        this.l53 = root.getChild(TriffidGeometry.L53);
        this.l54 = root.getChild(TriffidGeometry.L54);
        this.l55 = root.getChild(TriffidGeometry.L55);
        this.l56 = root.getChild(TriffidGeometry.L56);
        this.h22 = root.getChild(TriffidGeometry.H22);
        this.t15 = root.getChild(TriffidGeometry.T15);
        this.t14 = root.getChild(TriffidGeometry.T14);
        this.t13 = root.getChild(TriffidGeometry.T13);
        this.t12 = root.getChild(TriffidGeometry.T12);
        this.t11 = root.getChild(TriffidGeometry.T11);
        this.t10 = root.getChild(TriffidGeometry.T10);
        this.t9 = root.getChild(TriffidGeometry.T9);
        this.t6 = root.getChild(TriffidGeometry.T6);
        this.t2 = root.getChild(TriffidGeometry.T2);
        this.t8 = root.getChild(TriffidGeometry.T8);
        this.t7 = root.getChild(TriffidGeometry.T7);
        this.t5 = root.getChild(TriffidGeometry.T5);
        this.t4 = root.getChild(TriffidGeometry.T4);
        this.t3 = root.getChild(TriffidGeometry.T3);
        this.t1 = root.getChild(TriffidGeometry.T1);
        this.r47 = root.getChild(TriffidGeometry.R47);
        this.r2 = root.getChild(TriffidGeometry.R2);
        this.r6 = root.getChild(TriffidGeometry.R6);
        this.r5 = root.getChild(TriffidGeometry.R5);
        this.r10 = root.getChild(TriffidGeometry.R10);
        this.r7 = root.getChild(TriffidGeometry.R7);
        this.r12 = root.getChild(TriffidGeometry.R12);
        this.r8 = root.getChild(TriffidGeometry.R8);
        this.r11 = root.getChild(TriffidGeometry.R11);
        this.r4 = root.getChild(TriffidGeometry.R4);
        this.r40 = root.getChild(TriffidGeometry.R40);
        this.r45 = root.getChild(TriffidGeometry.R45);
        this.r49 = root.getChild(TriffidGeometry.R49);
        this.r44 = root.getChild(TriffidGeometry.R44);
        this.root43 = root.getChild(TriffidGeometry.ROOT43);
        this.r43 = root.getChild(TriffidGeometry.R43);
        this.r46 = root.getChild(TriffidGeometry.R46);
        this.r48 = root.getChild(TriffidGeometry.R48);
        this.r35 = root.getChild(TriffidGeometry.R35);
        this.r38 = root.getChild(TriffidGeometry.R38);
        this.r42 = root.getChild(TriffidGeometry.R42);
        this.r39 = root.getChild(TriffidGeometry.R39);
        this.r41 = root.getChild(TriffidGeometry.R41);
        this.r18 = root.getChild(TriffidGeometry.R18);
        this.r3 = root.getChild(TriffidGeometry.R3);
        this.r50 = root.getChild(TriffidGeometry.R50);
        this.r31 = root.getChild(TriffidGeometry.R31);
        this.r36 = root.getChild(TriffidGeometry.R36);
        this.r37 = root.getChild(TriffidGeometry.R37);
        this.r22 = root.getChild(TriffidGeometry.R22);
        this.r30 = root.getChild(TriffidGeometry.R30);
        this.r33 = root.getChild(TriffidGeometry.R33);
        this.r34 = root.getChild(TriffidGeometry.R34);
        this.r29 = root.getChild(TriffidGeometry.R29);
        this.r20 = root.getChild(TriffidGeometry.R20);
        this.r24 = root.getChild(TriffidGeometry.R24);
        this.r28 = root.getChild(TriffidGeometry.R28);
        this.r26 = root.getChild(TriffidGeometry.R26);
        this.r25 = root.getChild(TriffidGeometry.R25);
        this.r27 = root.getChild(TriffidGeometry.R27);
        this.r23 = root.getChild(TriffidGeometry.R23);
        this.r21 = root.getChild(TriffidGeometry.R21);
        this.r1 = root.getChild(TriffidGeometry.R1);
        this.r13 = root.getChild(TriffidGeometry.R13);
        this.r16 = root.getChild(TriffidGeometry.R16);
        this.r19 = root.getChild(TriffidGeometry.R19);
        this.r15 = root.getChild(TriffidGeometry.R15);
        this.r14 = root.getChild(TriffidGeometry.R14);
        this.r17 = root.getChild(TriffidGeometry.R17);
        this.r32 = root.getChild(TriffidGeometry.R32);
        this.l16 = root.getChild(TriffidGeometry.L16);
        this.l44 = root.getChild(TriffidGeometry.L44);
        this.root = root.getChild(TriffidGeometry.ROOT);
        this.drawOrder = new ModelPart[] {
                this.r9, this.b14, this.base, this.b3, this.l57, this.l30, this.b6, this.b7, this.b8, this.b9,
                this.b11, this.b16, this.h18, this.b13, this.b15, this.h8, this.h1, this.h13, this.h7, this.h3,
                this.h17, this.h16, this.h23, this.h4, this.h2, this.h21, this.h19, this.h20, this.b10, this.b17,
                this.h6, this.h11, this.h14, this.h15, this.h10, this.h9, this.h5, this.h12, this.c2, this.c11,
                this.c1, this.c5, this.c3, this.c4, this.c10, this.c6, this.c7, this.c8, this.c9, this.b1, this.l15,
                this.b2, this.l43, this.l1, this.l2, this.l3, this.leaf3, this.l4, this.l5, this.l6, this.l7,
                this.l8, this.l9, this.l10, this.l11, this.l12, this.l13, this.l14, this.b4, this.l31, this.l32,
                this.leaf32, this.l33, this.l34, this.l35, this.l36, this.l37, this.l38, this.l39, this.l40,
                this.l41, this.l42, this.l17, this.l18, this.l19, this.l20, this.l21, this.l22, this.l23, this.l24,
                this.l25, this.l26, this.l27, this.l28, this.l29, this.b5, this.l45, this.l46, this.l47, this.l48,
                this.l49, this.leaf49, this.l50, this.l51, this.l52, this.l53, this.l54, this.l55, this.l56,
                this.h22, this.t15, this.t14, this.t13, this.t12, this.t11, this.t10, this.t9, this.t6, this.t2,
                this.t8, this.t7, this.t5, this.t4, this.t3, this.t1, this.r47, this.r2, this.r6, this.r5, this.r10,
                this.r7, this.r12, this.r8, this.r11, this.r4, this.r40, this.r45, this.r49, this.r44, this.root43,
                this.r43, this.r46, this.r48, this.r35, this.r38, this.r42, this.r39, this.r41, this.r18, this.r3,
                this.r50, this.r31, this.r36, this.r37, this.r22, this.r30, this.r33, this.r34, this.r29, this.r20,
                this.r24, this.r28, this.r26, this.r25, this.r27, this.r23, this.r21, this.r1, this.r13, this.r16,
                this.r19, this.r15, this.r14, this.r17, this.r32, this.l16, this.l44, this.root };
    }

    /**
     * The writes of {@code render()} (:1091-1233), in their order, after {@code resetPose()} (R8). Parts the original
     * never moved ({@code l44} and {@code t15} pivots, {@code t1}/{@code t2} angles) keep their constructor pose, which
     * is what the original's untouched fields held.
     */
    @Override
    public void setupAnim(final Triffid e, final float f, final float f1, final float f2, final float f3, final float f4) {
        for (final ModelPart part : this.drawOrder) {
            part.resetPose();
        }
        float newangle = 0.0f;
        float delta = 0.0f;
        if (e.getOpenClosed() == 0) {
            newangle = 0.122522116f;
        } else {
            newangle = Mth.cos(f2 * 0.25f * this.wingspeed) * 3.1415927f * 0.039f;
        }
        this.l1.zRot = -0.95f + newangle;
        this.l1.y = (float) (10.0 - Math.cos(this.l1.zRot) * 5.0) + 3.0f;
        this.l1.x = (float) (-7.0 + Math.sin(this.l1.zRot) * 5.0) + 3.0f;
        this.leafpartA(newangle, this.l1, this.l2, 3);
        this.leafpartA(newangle, this.l2, this.l3, 4);
        this.leafpartA(newangle, this.l3, this.leaf3, 4);
        this.leafpartA(newangle, this.leaf3, this.l4, 4);
        this.leafpartA(newangle, this.l4, this.l5, 4);
        this.leafpartA(newangle, this.l5, this.l6, 6);
        this.leafpartA(newangle, this.l6, this.l7, 4);
        this.leafpartA(newangle, this.l7, this.l8, 4);
        this.leafpartA(newangle, this.l8, this.l9, 4);
        this.leafpartA(newangle, this.l9, this.l10, 4);
        this.leafpartA(newangle, this.l10, this.l11, 3);
        this.leafpartA(newangle, this.l11, this.l12, 3);
        this.leafpartA(newangle, this.l12, this.l13, 2);
        this.leafpartA(newangle, this.l13, this.l14, 2);
        this.leafpartA(newangle, this.l14, this.l15, 2);
        this.l31.zRot = 0.95f - newangle;
        this.l31.y = (float) (10.0 - Math.cos(this.l31.zRot) * 5.0) + 3.0f;
        this.l31.x = (float) (7.0 + Math.sin(this.l31.zRot) * 5.0) - 3.0f;
        this.leafpartC(-newangle, this.l31, this.l32, 3);
        this.leafpartC(-newangle, this.l32, this.leaf32, 3);
        this.leafpartC(-newangle, this.leaf32, this.l33, 3);
        this.leafpartC(-newangle, this.l33, this.l34, 4);
        this.leafpartC(-newangle, this.l34, this.l35, 4);
        this.leafpartC(-newangle, this.l35, this.l36, 5);
        this.leafpartC(-newangle, this.l36, this.l37, 4);
        this.leafpartC(-newangle, this.l37, this.l38, 4);
        this.leafpartC(-newangle, this.l38, this.l39, 4);
        this.leafpartC(-newangle, this.l39, this.l40, 3);
        this.leafpartC(-newangle, this.l40, this.l41, 3);
        this.leafpartC(-newangle, this.l41, this.l42, 2);
        this.leafpartC(-newangle, this.l42, this.l43, 1);
        this.l16.xRot = -0.75f - newangle;
        this.l16.y = (float) (10.0 + Math.cos(this.l16.xRot) * 5.0);
        this.l16.z = (float) (-9.0 - Math.sin(this.l16.xRot) * 5.0) - 3.0f;
        this.leafpartB(-newangle, this.l16, this.l17, 3);
        this.leafpartB(-newangle, this.l17, this.l18, 3);
        this.leafpartB(-newangle, this.l18, this.l19, 4);
        this.leafpartB(-newangle, this.l19, this.l20, 4);
        this.leafpartB(-newangle, this.l20, this.l21, 5);
        this.leafpartB(-newangle, this.l21, this.l22, 4);
        this.leafpartB(-newangle, this.l22, this.l23, 4);
        this.leafpartB(-newangle, this.l23, this.l24, 4);
        this.leafpartB(-newangle, this.l24, this.l25, 4);
        this.leafpartB(-newangle, this.l25, this.l26, 4);
        this.leafpartB(-newangle, this.l26, this.l27, 3);
        this.leafpartB(-newangle, this.l27, this.l28, 2);
        this.leafpartB(-newangle, this.l28, this.l29, 2);
        this.leafpartB(-newangle, this.l29, this.l30, 2);
        this.l44.xRot = 0.75f + newangle;
        this.leafpartD(newangle, this.l44, this.l45, 5);
        this.leafpartD(newangle, this.l45, this.l46, 4);
        this.leafpartD(newangle, this.l46, this.l47, 3);
        this.leafpartD(newangle, this.l47, this.l48, 4);
        this.leafpartD(newangle, this.l48, this.l49, 3);
        this.leafpartD(newangle, this.l49, this.leaf49, 5);
        this.leafpartD(newangle, this.leaf49, this.l50, 3);
        this.leafpartD(newangle, this.l50, this.l51, 3);
        this.leafpartD(newangle, this.l51, this.l52, 3);
        this.leafpartD(newangle, this.l52, this.l53, 3);
        this.leafpartD(newangle, this.l53, this.l54, 3);
        this.leafpartD(newangle, this.l54, this.l55, 2);
        this.leafpartD(newangle, this.l55, this.l56, 2);
        this.leafpartD(newangle, this.l56, this.l57, 2);
        if (e.getAttacking() != 0) {
            newangle = Mth.cos(f2 * 0.25f * this.wingspeed) * 3.1415927f * 0.5f;
            newangle = Math.abs(newangle);
        } else {
            newangle = 1.5707964f;
        }
        delta = -0.6f;
        this.t15.zRot = -newangle + delta;
        this.t14.zRot = newangle + delta;
        this.t14.y = (float) (this.t15.y - Math.sin(this.t15.zRot) * 6.0);
        this.t14.x = (float) (this.t15.x - Math.cos(this.t15.zRot) * 6.0);
        this.t13.zRot = -newangle + delta;
        this.t13.y = (float) (this.t14.y - Math.sin(this.t14.zRot) * 3.0);
        this.t13.x = (float) (this.t14.x - Math.cos(this.t14.zRot) * 3.0);
        this.t12.zRot = newangle + delta;
        this.t12.y = (float) (this.t13.y - Math.sin(this.t13.zRot) * 3.0);
        this.t12.x = (float) (this.t13.x - Math.cos(this.t13.zRot) * 3.0);
        this.t11.zRot = -newangle + delta;
        this.t11.y = (float) (this.t12.y - Math.sin(this.t12.zRot) * 3.0);
        this.t11.x = (float) (this.t12.x - Math.cos(this.t12.zRot) * 3.0);
        this.t10.zRot = newangle + delta;
        this.t10.y = (float) (this.t11.y - Math.sin(this.t11.zRot) * 3.0);
        this.t10.x = (float) (this.t11.x - Math.cos(this.t11.zRot) * 3.0);
        this.t9.zRot = -newangle + delta;
        this.t9.y = (float) (this.t10.y - Math.sin(this.t10.zRot) * 3.0);
        this.t9.x = (float) (this.t10.x - Math.cos(this.t10.zRot) * 3.0);
        this.t8.zRot = newangle + delta;
        this.t8.y = (float) (this.t9.y - Math.sin(this.t9.zRot) * 6.0);
        this.t8.x = (float) (this.t9.x - Math.cos(this.t9.zRot) * 6.0);
        this.t7.zRot = -newangle + delta;
        this.t7.y = (float) (this.t8.y - Math.sin(this.t8.zRot) * 6.0);
        this.t7.x = (float) (this.t8.x - Math.cos(this.t8.zRot) * 6.0);
        this.t6.zRot = newangle + delta;
        this.t6.y = (float) (this.t7.y - Math.sin(this.t7.zRot) * 6.0);
        this.t6.x = (float) (this.t7.x - Math.cos(this.t7.zRot) * 6.0);
        this.t5.zRot = -newangle + delta;
        this.t5.y = (float) (this.t6.y - Math.sin(this.t6.zRot) * 6.0);
        this.t5.x = (float) (this.t6.x - Math.cos(this.t6.zRot) * 6.0);
        this.t4.zRot = newangle + delta;
        this.t4.y = (float) (this.t5.y - Math.sin(this.t5.zRot) * 6.0);
        this.t4.x = (float) (this.t5.x - Math.cos(this.t5.zRot) * 6.0);
        this.t3.zRot = -newangle + delta;
        this.t3.y = (float) (this.t4.y - Math.sin(this.t4.zRot) * 6.0);
        this.t3.x = (float) (this.t4.x - Math.cos(this.t4.zRot) * 6.0);
        final float n = (float) (this.t3.y - Math.sin(this.t3.zRot) * 3.0);
        this.t1.y = n;
        this.t2.y = n;
        final float n2 = (float) (this.t3.x - Math.cos(this.t3.zRot) * 3.0);
        this.t1.x = n2;
        this.t2.x = n2;
        newangle = 0.0f;
        this.t3.yRot = newangle;
        this.t4.yRot = newangle;
        this.t5.yRot = newangle;
        this.t6.yRot = newangle;
        this.t7.yRot = newangle;
        this.t8.yRot = newangle;
        this.t9.yRot = newangle;
        this.t10.yRot = newangle;
        this.t11.yRot = newangle;
        this.t12.yRot = newangle;
        this.t13.yRot = newangle;
        this.t14.yRot = newangle;
        this.t15.yRot = newangle;
    }

    /** {@code leafpartA} (:1430-1434): the next leaf segment rolls further around Z. */
    private void leafpartA(final float newangle, final ModelPart l1, final ModelPart l2, final int j) {
        l2.zRot = l1.zRot + newangle;
        l2.y = (float) (l1.y - Math.cos(l1.zRot) * j);
        l2.x = (float) (l1.x + Math.sin(l1.zRot) * j);
    }

    /** {@code leafpartC} (:1436-1440): textually the same as {@code leafpartA}. */
    private void leafpartC(final float newangle, final ModelPart l1, final ModelPart l2, final int j) {
        l2.zRot = l1.zRot + newangle;
        l2.y = (float) (l1.y - Math.cos(l1.zRot) * j);
        l2.x = (float) (l1.x + Math.sin(l1.zRot) * j);
    }

    /** {@code leafpartB} (:1442-1446): the next leaf segment pitches further around X, towards -Z. */
    private void leafpartB(final float newangle, final ModelPart l1, final ModelPart l2, final int j) {
        l2.xRot = l1.xRot + newangle;
        l2.y = (float) (l1.y + Math.sin(l1.xRot) * j);
        l2.z = (float) (l1.z - Math.cos(l1.xRot) * j);
    }

    /** {@code leafpartD} (:1448-1452): the next leaf segment pitches further around X, towards +Z. */
    private void leafpartD(final float newangle, final ModelPart l1, final ModelPart l2, final int j) {
        l2.xRot = l1.xRot + newangle;
        l2.y = (float) (l1.y - Math.sin(l1.xRot) * j);
        l2.z = (float) (l1.z + Math.cos(l1.xRot) * j);
    }

    /**
     * The draw calls of {@code render()} (:1239-1416) at scale {@code f5 = 0.0625}, inside
     * {@code glPushMatrix(); glRotatef(-90, 0, 1, 0); ... glPopMatrix()} (:1234-1238, :1417).
     */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        for (final ModelPart part : this.drawOrder) {
            part.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        poseStack.popPose();
    }
}
