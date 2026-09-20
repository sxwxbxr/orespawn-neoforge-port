package com.swbr.orespawn.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.swbr.orespawn.OreSpawn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Port of {@code danger.orespawn.RenderSpinner}: a camera-facing 16x16 tile out of the 256x256
 * sprite atlas {@code spinners.png}, scaled to half size and rolled around the view axis by the
 * entity's pitch. Tile index = row * 16 + column (RenderSpinner.java:33-36).
 *
 * <p>Subclasses (RenderShoe, RenderItemUrchin, RenderCage in later waves) set
 * {@link #spinItemIconIndex} from the entity and then call {@code super.render(...)}, exactly as
 * their originals did with {@code doRender}. Default tile 160 is the empty critter cage.
 */
public class RenderSpinner<T extends Entity> extends EntityRenderer<T> {

    /** {@code new ResourceLocation("orespawn", "spinners.png")} (RenderSpinner.java:57); the asset
     *  generator moved the file to {@code textures/entity/} (manifest texture_map). */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/spinners.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    /** Atlas tile to draw; public so subclasses can set it per entity before rendering. */
    public int spinItemIconIndex = 160;

    public RenderSpinner(EntityRendererProvider.Context context) {
        super(context);
    }

    /** {@code doRender} (RenderSpinner.java:20-30). The translation to the entity position is done
     *  by the dispatcher in 1.21.1; {@code GL_RESCALE_NORMAL} is implicit in the normal transform. */
    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f); // :25
        // :27 - the original used the raw rotationPitch, not an interpolated value
        drawTile(poseStack, buffer.getBuffer(RENDER_TYPE), packedLight, this.spinItemIconIndex, entity.getXRot());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /** {@code func_77026_a} (RenderSpinner.java:32-50): a 1x1 quad from y -0.25 to 0.75, centred on x. */
    private void drawTile(PoseStack poseStack, VertexConsumer consumer, int packedLight, int index, float pitch) {
        float u0 = (index % 16 * 16 + 0) / 256.0f;  // :33
        float u1 = (index % 16 * 16 + 16) / 256.0f; // :34
        float v0 = (index / 16 * 16 + 0) / 256.0f;  // :35
        float v1 = (index / 16 * 16 + 16) / 256.0f; // :36
        float size = 1.0f;  // :37
        float offX = 0.5f;  // :38
        float offY = 0.25f; // :39
        // :40-41 glRotatef(180 - playerViewY, Y) then glRotatef(-playerViewX, X) is the 1.7.10
        // billboard; cameraOrientation() is the same rotation in 1.21.1 (cf. DragonFireballRenderer).
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch)); // :42
        PoseStack.Pose pose = poseStack.last();
        vertex(consumer, pose, packedLight, 0.0f - offX, 0.0f - offY, u0, v1);   // :45
        vertex(consumer, pose, packedLight, size - offX, 0.0f - offY, u1, v1);   // :46
        vertex(consumer, pose, packedLight, size - offX, size - offY, u1, v0);   // :47
        vertex(consumer, pose, packedLight, 0.0f - offX, size - offY, u0, v0);   // :48
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int packedLight,
                               float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0f)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0f, 1.0f, 0.0f); // :44 setNormal(0, 1, 0)
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE; // :52-54
    }
}
