package com.swbr.orespawn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.geom.ChainsawGeometry;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.ModelChainsaw} (ModelChainsaw.java:5-244): the 8-box chainsaw
 * held by the Chainsaw item. The geometry (:41-82) is the generated {@link ChainsawGeometry};
 * this class bakes it and ports the animation of {@code render()} (:85-105) and the six
 * {@code renderTooth*} methods (:113-243).
 *
 * <p><b>What the original animated.</b> The nose sprocket {@code blade2} turned by
 * {@code 0.10471975} rad (6 degrees) per rendered frame and snapped back to 0 past 2 pi
 * (:93-97). The single {@code tooth} box was drawn six times per frame, each with its own
 * {@code toothposN}/{@code toothdirN} pair (:15-26): along the top edge ({@code rotationPointY
 * = -2}) a tooth moved from {@code z = -5} towards the tip in 0.5-unit steps until
 * {@code toothpos > 21}, then along the bottom edge ({@code rotationPointY = 3}) back to 0 - a
 * chain of six red teeth running round the bar. Start values 0/7/14 forward and 20/13/6
 * backward (:29-40).
 *
 * <p><b>Time binding (DECISIONS R18, "ModelRotator/ModelChainsaw an Systemzeit gebunden").</b>
 * The original advanced its state once per {@code render()} call, so the speed depended on the
 * frame rate and on how many chainsaws were on screen. The port derives the same positions
 * from client game time plus partial tick: {@link #frames(long, float)} converts ticks to
 * "original frames" at {@link #FRAMES_PER_TICK}, and the pose of every part is a pure function
 * of that value, so all chainsaws share one phase and run at one speed.
 *
 * <p>Units as in {@link BattleAxeModel}: the original rendered with {@code f5 = 1.0} (:86), the
 * caller scales by 16 to undo {@link ModelPart}'s division.
 */
public class ChainsawModel extends Model {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "chainsaw"), "main");

    // PORT: the original stepped once per rendered frame (ModelChainsaw.java:94, :117, :126). The
    // port steps per tick; three original frames per tick reproduce the look of a 60 fps client,
    // the frame rate the animation was tuned at in practice. Any other rate is equally "original".
    public static final float FRAMES_PER_TICK = 3.0f;

    /** {@code blade2.rotateAngleX += 0.10471975511965977} per frame (ModelChainsaw.java:94). */
    private static final double SPROCKET_STEP = 0.10471975511965977;
    private static final double TWO_PI = 6.283185307179586; // :95
    /** A tooth needs 43 frames for the top edge (positions 0 .. 21 in 0.5 steps, :115-121) and
     *  43 for the bottom edge back (:124-130); the turning positions 21 and 0 are each drawn twice. */
    private static final float TOOTH_HALF = 43.0f;
    private static final float TOOTH_PERIOD = 2.0f * TOOTH_HALF;
    /** Start phases of the six teeth, from {@code toothposN}/{@code toothdirN} (:29-40): forward
     *  {@code pos / 0.5}, backward {@code 43 + (21 - pos) / 0.5}. */
    private static final float[] TOOTH_PHASE = {0.0f, 14.0f, 28.0f, 45.0f, 59.0f, 73.0f};
    /** Sprocket 60 frames, teeth 86 frames: the whole animation repeats every 2580 frames. */
    private static final long PERIOD_FRAMES = 2580L;

    private final ModelPart engine;
    private final ModelPart handle1;
    private final ModelPart handle2;
    private final ModelPart handle3;
    private final ModelPart muffler;
    private final ModelPart blade1;
    private final ModelPart blade2;
    private final ModelPart tooth;

    /** Animation time in original frames, set by {@link #setupAnim(float)} before each render. */
    private float frames;

    public ChainsawModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.engine = root.getChild(ChainsawGeometry.ENGINE);
        this.handle1 = root.getChild(ChainsawGeometry.HANDLE1);
        this.handle2 = root.getChild(ChainsawGeometry.HANDLE2);
        this.handle3 = root.getChild(ChainsawGeometry.HANDLE3);
        this.muffler = root.getChild(ChainsawGeometry.MUFFLER);
        this.blade1 = root.getChild(ChainsawGeometry.BLADE1);
        this.blade2 = root.getChild(ChainsawGeometry.BLADE2);
        this.tooth = root.getChild(ChainsawGeometry.TOOTH);
    }

    /**
     * Converts client time to the frame counter the original animation ran on. The modulo keeps
     * the value small enough for {@code float} arithmetic over long sessions; the animation is
     * periodic in {@link #PERIOD_FRAMES}, so nothing is lost.
     */
    public static float frames(long gameTime, float partialTick) {
        long periodTicks = (long) (PERIOD_FRAMES / FRAMES_PER_TICK);
        long ticks = Math.floorMod(gameTime, periodTicks);
        return ((float) ticks + partialTick) * FRAMES_PER_TICK;
    }

    /** Stores the animation time; the poses are computed in {@link #renderToBuffer}. */
    public void setupAnim(float frames) {
        this.frames = frames;
    }

    /** {@code render()} (ModelChainsaw.java:85-105): six teeth, then the sprocket step, then the body. */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        for (float phase : TOOTH_PHASE) {                       // renderTooth .. renderTooth5 (:87-92)
            placeTooth(this.frames + phase);
            this.tooth.render(poseStack, buffer, packedLight, packedOverlay, color);
        }
        // :93-97 - the increment ran before blade2 was drawn, so frame n showed step * (n + 1);
        // past 2 pi the original reset to exactly 0, which the modulo reproduces to within one step.
        this.blade2.xRot = (float) ((SPROCKET_STEP * (this.frames + 1.0f)) % TWO_PI);
        this.engine.render(poseStack, buffer, packedLight, packedOverlay, color);   // :98
        this.handle1.render(poseStack, buffer, packedLight, packedOverlay, color);  // :99
        this.handle2.render(poseStack, buffer, packedLight, packedOverlay, color);  // :100
        this.handle3.render(poseStack, buffer, packedLight, packedOverlay, color);  // :101
        this.muffler.render(poseStack, buffer, packedLight, packedOverlay, color);  // :102
        this.blade1.render(poseStack, buffer, packedLight, packedOverlay, color);   // :103
        this.blade2.render(poseStack, buffer, packedLight, packedOverlay, color);   // :104
    }

    /**
     * One {@code renderToothN} body (ModelChainsaw.java:113-133) as a function of its phase:
     * frames 0..42 on the top edge moving outward, 43..85 on the bottom edge moving back.
     * {@code rotationPointX} was never touched (stays 0 from the constructor, :79).
     */
    private void placeTooth(float phaseFrames) {
        float phase = Mth.positiveModulo(phaseFrames, TOOTH_PERIOD);
        float pos;
        if (phase < TOOTH_HALF) {
            this.tooth.y = -2.0f;                              // :115
            pos = Math.min(0.5f * phase, 21.0f);               // :117-119
        } else {
            this.tooth.y = 3.0f;                               // :124
            pos = Math.max(21.0f - 0.5f * (phase - TOOTH_HALF), 0.0f); // :126-128
        }
        this.tooth.z = -5.0f - pos;                            // :116 / :125
    }
}
