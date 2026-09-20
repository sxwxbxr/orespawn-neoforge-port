package com.swbr.orespawn.client.renderer.rock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.entity.rock.EntityThrownRock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderThrownRock} (RenderThrownRock.java:10-113): the rock's own
 * 16x16 item icon as a camera-facing quad at half size, rolled in the view plane by the entity's
 * pitch - which {@code EntityThrownRock.tick} advances by 30 degrees per tick, so the rock spins.
 *
 * <p>Not {@code ThrownItemRenderer}, as the catalogue proposed (design-entities-03.md): that one
 * does not roll, and the spin is the only animation the thrown rock has. The renderer is
 * {@code RenderSpinner}'s drawing with the UV divisor of this class: {@code func_77026_a} divides by
 * 16.0 instead of 256.0 (:39-42), so tile 0 covers the whole texture, u and v 0..1.
 *
 * <p>Texture by type (:58-111), the item textures under {@code textures/item/}; type 0 and anything
 * unknown fall back to the small rock.
 */
public class RenderThrownRock extends EntityRenderer<EntityThrownRock> {

    private static final ResourceLocation texture1 = texture("rocksmall");
    private static final ResourceLocation texture2 = texture("rock");
    private static final ResourceLocation texture3 = texture("rockred");
    private static final ResourceLocation texture4 = texture("rockgreen");
    private static final ResourceLocation texture5 = texture("rockblue");
    private static final ResourceLocation texture6 = texture("rockpurple");
    private static final ResourceLocation texture7 = texture("rockspikey");
    private static final ResourceLocation texture8 = texture("rocktnt");
    private static final ResourceLocation texture9 = texture("rockcrystalred");
    private static final ResourceLocation texture10 = texture("rockcrystalgreen");
    private static final ResourceLocation texture11 = texture("rockcrystalblue");
    private static final ResourceLocation texture12 = texture("rockcrystaltnt");

    public RenderThrownRock(final EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * {@code doRender} (:26-36). The translation to the entity position is done by the dispatcher
     * in 1.21.1; {@code GL_RESCALE_NORMAL} (:30, :34) is implicit in the normal transform. The
     * original rolled by the raw {@code rotationPitch}, not an interpolated value (:33).
     */
    @Override
    public void render(final EntityThrownRock entity, final float entityYaw, final float partialTick,
                       final PoseStack poseStack, final MultiBufferSource buffer, final int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f); // :31
        final VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        this.renderEntity(poseStack, consumer, packedLight, 0, entity.getXRot()); // :33
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /** {@code func_77026_a} (MCP {@code renderEntity}, :38-56): a 1x1 quad from y -0.25 to 0.75, centred on x. */
    private void renderEntity(final PoseStack poseStack, final VertexConsumer consumer, final int packedLight,
                              final int par2, final float par3) {
        final float var3 = (par2 % 16 * 16 + 0) / 16.0f;  // :39
        final float var4 = (par2 % 16 * 16 + 16) / 16.0f; // :40
        final float var5 = (par2 / 16 * 16 + 0) / 16.0f;  // :41
        final float var6 = (par2 / 16 * 16 + 16) / 16.0f; // :42
        final float var7 = 1.0f;  // :43
        final float var8 = 0.5f;  // :44
        final float var9 = 0.25f; // :45
        // :46-47 glRotatef(180 - playerViewY, Y) then glRotatef(-playerViewX, X) is the 1.7.10
        // billboard; cameraOrientation() is the same rotation in 1.21.1 (as in RenderSpinner).
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotationDegrees(par3)); // :48
        final PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, packedLight, 0.0f - var8, 0.0f - var9, var3, var6); // :51
        vertex(consumer, pose, packedLight, var7 - var8, 0.0f - var9, var4, var6); // :52
        vertex(consumer, pose, packedLight, var7 - var8, var7 - var9, var4, var5); // :53
        vertex(consumer, pose, packedLight, 0.0f - var8, var7 - var9, var3, var5); // :54
    }

    private static void vertex(final VertexConsumer consumer, final PoseStack.Pose pose, final int packedLight,
                               final float x, final float y, final float u, final float v) {
        consumer.addVertex(pose, x, y, 0.0f)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0f, 1.0f, 0.0f); // :50 setNormal(0, 1, 0)
    }

    /** {@code getEntityTexture} (:58-97). */
    @Override
    public ResourceLocation getTextureLocation(final EntityThrownRock entity) {
        final EntityThrownRock r = entity;
        if (r.getRockType() == 1) {
            return texture1;
        }
        if (r.getRockType() == 2) {
            return texture2;
        }
        if (r.getRockType() == 3) {
            return texture3;
        }
        if (r.getRockType() == 4) {
            return texture4;
        }
        if (r.getRockType() == 5) {
            return texture5;
        }
        if (r.getRockType() == 6) {
            return texture6;
        }
        if (r.getRockType() == 7) {
            return texture7;
        }
        if (r.getRockType() == 8) {
            return texture8;
        }
        if (r.getRockType() == 9) {
            return texture9;
        }
        if (r.getRockType() == 10) {
            return texture10;
        }
        if (r.getRockType() == 11) {
            return texture11;
        }
        if (r.getRockType() == 12) {
            return texture12;
        }
        return texture1;
    }

    private static ResourceLocation texture(final String name) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/item/" + name + ".png");
    }
}
