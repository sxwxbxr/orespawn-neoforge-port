package com.swbr.orespawn.client.renderer.vehicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.client.model.ElevatorModel;
import com.swbr.orespawn.entity.vehicle.Elevator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Port of {@code danger.orespawn.RenderElevator} (RenderElevator.java:10-49), the renderer of the
 * {@code hoverboard} entity. A plain {@code Render}, not {@code RenderLiving}: no hurt tint, no
 * body yaw, no {@code preRenderCallback}. The board wobbles like a vanilla boat when hit.
 *
 * <p>Net scale is 1.0: {@code glScalef(0.75)} is undone right away by {@code glScalef(1/0.75)}
 * (:32-34), so the deck is 0.5 x 0.0625 x 1.0 blocks with its top face at the entity's Y.
 */
public class RenderElevator extends EntityRenderer<Elevator> {

    protected final ElevatorModel modelElevator;

    /** {@code RenderElevator()} (:15-18): shadow 0.25, one {@code ModelElevator}. */
    public RenderElevator(final EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.25f;
        this.modelElevator = new ElevatorModel(context.bakeLayer(ElevatorModel.LAYER));
    }

    /**
     * {@code renderElevator} (:20-39). The {@code glTranslatef(x, y, z)} of :22 is done by the
     * dispatcher in 1.21.1, which also passes the same interpolated yaw
     * ({@code yRotO + (yRot - yRotO) * partial}) as 1.7.10's {@code RenderManager}.
     */
    @Override
    public void render(final Elevator par1EntityElevator, final float par8, final float par9,
                       final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - par8)); // :23
        final float f2 = par1EntityElevator.getTimeSinceHit() - par9; // :24
        float f3 = par1EntityElevator.getDamageTaken() - par9;        // :25
        if (f3 < 0.0f) {
            f3 = 0.0f;
        }
        if (f2 > 0.0f) {
            // :30 - the vanilla boat wobble
            poseStack.mulPose(Axis.XP.rotationDegrees(
                    Mth.sin(f2) * f2 * f3 / 10.0f * par1EntityElevator.getForwardDirection()));
        }
        final float f4 = 0.75f;
        poseStack.scale(f4, f4, f4);                            // :33
        poseStack.scale(1.0f / f4, 1.0f / f4, 1.0f / f4);       // :34
        poseStack.scale(-1.0f, -1.0f, 1.0f);                    // :36
        // :37 render(entity, 0, 0, -0.1, 0, 0, 0.0625) - the model reads none of the float arguments.
        this.modelElevator.setupAnim(par1EntityElevator, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f);
        final VertexConsumer consumer =
                buffer.getBuffer(this.modelElevator.renderType(this.getTextureLocation(par1EntityElevator)));
        this.modelElevator.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        // No super.render(): in 1.21.1 that draws the leash and the name tag, which a 1.7.10 Render
        // subclass never did (both lived in RenderLiving / RendererLivingEntity).
    }

    /** {@code getEntityTexture} (:45-48): one of {@code elevator1..10.png} by colour. */
    @Override
    public ResourceLocation getTextureLocation(final Elevator entity) {
        return entity.getTexture();
    }
}
