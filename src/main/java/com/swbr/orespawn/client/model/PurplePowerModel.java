package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.PurplePowerGeometry;
import com.swbr.orespawn.entity.boss.king.PurplePower;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;

/**
 * Port of {@code danger.orespawn.ModelPurplePower} (ModelPurplePower.java:8-89): three flat bars (4, 8 and 14 px long),
 * 64x32 texture, geometry {@link PurplePowerGeometry}. Nothing is animated through the entity; {@code render()}
 * (:37-78) draws each bar six times with {@code rotateAngleZ} stepping by 60 degrees, a star per bar.
 *
 * <p>GL (:43-77), around the whole model: {@code glPushMatrix}, {@code glEnable(GL_NORMALIZE)},
 * {@code glEnable(GL_BLEND)}, {@code glBlendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)}, {@code glColor4f(0.75, 0.75, 0.75,
 * 0.55)} and the lightmap at 240/240. That is {@link RenderType#entityTranslucent} (R8) with {@link #PURPLE_COLOR} and
 * {@link LightTexture#FULL_BRIGHT} on every draw. Before and after each star the pose turns by a fresh random angle
 * from the world random - about X for bar 1, Y for bar 2, Z for bar 3. The turn after a star is not undone: both
 * {@code glRotatef} calls add, so bar 2 starts from twice the first angle, as in the original. Those turns are
 * {@link PoseStack} rotations inside one push/pop in {@link #renderToBuffer}.
 *
 * <p>The three random draws happen in {@link #renderToBuffer}, as {@code render()} drew them; {@link #setupAnim} only
 * remembers the entity's world random ({@code p.worldObj.rand}, :49, :58, :67).
 */
public class PurplePowerModel extends EntityModel<PurplePower> {

    /** Register with {@code PurplePowerGeometry::createBodyLayer} in {@code EntityRenderersEvent.RegisterLayerDefinitions}. */
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "purple_power"), "main");

    /** {@code glColor4f(0.75f, 0.75f, 0.75f, 0.55f)} (:47). */
    public static final int PURPLE_COLOR = FastColor.ARGB32.colorFromFloat(0.55f, 0.75f, 0.75f, 0.75f);

    /** {@code ModelPurplePower(float f1)}: {@code wingspeed = 1.0f; wingspeed = f1} (:16-17); unused by {@code render()}. */
    @SuppressWarnings("unused")
    private float wingspeed;

    private final ModelPart Shape1;
    private final ModelPart Shape2;
    private final ModelPart Shape3;

    /** {@code p.worldObj.rand} of the entity being drawn, set by {@link #setupAnim}. */
    @Nullable
    private RandomSource rand;

    /** {@code ModelPurplePower(float f1)} (:15-35); the renderer passes 1.0 (manifest {@code model_args}). */
    public PurplePowerModel(final ModelPart root, final float f1) {
        super(RenderType::entityTranslucent);
        this.wingspeed = 1.0f;
        this.wingspeed = f1;
        this.Shape1 = root.getChild(PurplePowerGeometry.SHAPE1);
        this.Shape2 = root.getChild(PurplePowerGeometry.SHAPE2);
        this.Shape3 = root.getChild(PurplePowerGeometry.SHAPE3);
    }

    /** {@code setRotationAngles} (:86-88) only calls {@code super}; the bars' Z angles are written while drawing. */
    @Override
    public void setupAnim(final PurplePower entity, final float f, final float f1, final float f2, final float f3, final float f4) {
        this.Shape1.resetPose();
        this.Shape2.resetPose();
        this.Shape3.resetPose();
        this.rand = entity.level().random;
    }

    /**
     * The draw part of {@code render()} (:43-77). The renderer's light and colour are replaced by the original's fixed
     * lightmap and colour; the hurt overlay stays.
     */
    @Override
    public void renderToBuffer(final PoseStack poseStack, final VertexConsumer buffer, final int packedLight,
                               final int packedOverlay, final int color) {
        final RandomSource random = this.rand != null ? this.rand : RandomSource.create();
        final int light = LightTexture.FULL_BRIGHT;
        float rf1 = 1.0f;
        float newangle = 0.0f;
        poseStack.pushPose();
        rf1 = random.nextFloat() * 360.0f;
        poseStack.mulPose(Axis.XP.rotationDegrees(rf1));
        for (int i = 0; i < 6; ++i) {
            this.Shape1.zRot = newangle;
            this.Shape1.render(poseStack, buffer, light, packedOverlay, PURPLE_COLOR);
            newangle += 1.0471976f;
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(rf1));
        newangle = 0.0f;
        rf1 = random.nextFloat() * 360.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rf1));
        for (int i = 0; i < 6; ++i) {
            this.Shape2.zRot = newangle;
            this.Shape2.render(poseStack, buffer, light, packedOverlay, PURPLE_COLOR);
            newangle += 1.0471976f;
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(rf1));
        newangle = 0.0f;
        rf1 = random.nextFloat() * 360.0f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(rf1));
        for (int i = 0; i < 6; ++i) {
            this.Shape3.zRot = newangle;
            this.Shape3.render(poseStack, buffer, light, packedOverlay, PURPLE_COLOR);
            newangle += 1.0471976f;
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(rf1));
        // glColor4f(1, 1, 1, 1), glDisable(GL_BLEND) (:75-76): render state, nothing to undo here.
        poseStack.popPose();
    }
}
