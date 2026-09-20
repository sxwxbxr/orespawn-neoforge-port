package com.swbr.orespawn.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * What the three 1.7.10 {@code IItemRenderer}s of this wave (RenderBattleAxe, RenderChainsaw,
 * RenderQueenBattleAxe) had in common, as a {@link BlockEntityWithoutLevelRenderer}
 * (DECISIONS R8). Each original was the same 75 lines with different numbers:
 * {@code handleRenderType} accepted only {@code EQUIPPED} and {@code EQUIPPED_FIRST_PERSON}
 * (:18-30), {@code shouldUseRenderHelper} always said {@code true} (:32-34), and
 * {@code renderItem} chose {@code renderSwordF5} for the third person and {@code renderSword}
 * for the first (:36-47). Both did {@code glPushMatrix -> glRotatef... -> glScalef(scale) ->
 * glTranslatef(x, y, z) -> bindTexture -> model.render() -> glPopMatrix}. The subclasses keep
 * exactly those two methods; the shared tail is {@link #renderModel}.
 *
 * <p><b>Where the hand matrix went.</b> In 1.7.10 the renderer drew inside the hand matrix of
 * {@code RenderPlayer} (third person, "3D block" branch because {@code shouldUseRenderHelper}
 * was true: {@code glTranslatef(-0.0625, 0.4375, 0.0625); glTranslatef(0, 0.1875, -0.3125);
 * glRotatef(20, X); glRotatef(45, Y); glScalef(-0.375, -0.375, 0.375)}) and of
 * {@code ItemRenderer} (first person, at rest: {@code glTranslatef(0.56, -0.65, -0.72);
 * glRotatef(45, Y); glScalef(0.4)}) - both read from the 1.7.10 client bytecode
 * (reference/jar/mcp/client-1.7.10.jar, classes {@code bop} and {@code bly}). 1.21.1 puts its
 * own hand frame in front of the item model and then applies the model's {@code display}
 * transform; the difference between the two frames is expressed as that {@code display}
 * transform in {@code models/item/bigweapon_hand.json} (third person: rotation XYZ
 * 70/-45/0, translation 0/2/0, scale 0.375; first person: translation 0/-2.08/0, scale 0.4 -
 * the 45 degree yaw is already part of the 1.21.1 first-person frame). What arrives here is
 * therefore the frame the original renderer started in.
 *
 * <p><b>Two mechanical differences.</b> {@code ItemRenderer.render} translates by
 * {@code (-0.5, -0.5, -0.5)} after the display transform before it calls a custom renderer
 * (ItemRenderer.java:124); the first thing done here is to undo that. And
 * {@link net.minecraft.client.model.geom.ModelPart#render} divides every coordinate by 16,
 * whereas the originals rendered with {@code f5 = 1.0} - one model unit per block; the last
 * thing done before the model is a scale by 16.
 *
 * <p>Only the in-hand contexts reach this renderer: the item model is a
 * {@code neoforge:separate_transforms} model whose GUI/ground/fixed base is the flat
 * {@code *small} sprite, exactly the {@code handleRenderType == false} fallback of the original.
 *
 * <p>Construction is lazy (see {@link BigWeaponRenderers}): {@code RegisterClientExtensionsEvent}
 * fires from {@code ClientModLoader.begin} (Minecraft.java:492) before {@code entityModels}
 * exists (:525), so the model is baked on first use and again after a resource reload.
 *
 * @param <M> the weapon model
 */
public abstract class BigWeaponRenderer<M extends Model> extends BlockEntityWithoutLevelRenderer {

    private final ResourceLocation texture;
    private M model;

    protected BigWeaponRenderer(ResourceLocation texture) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.texture = texture;
    }

    /** Bakes the weapon model from its registered layer. */
    protected abstract M bake(EntityModelSet models);

    /** {@code renderSword(x, y, z, scale)} - the first-person pose, rotations then {@link #renderModel}. */
    protected abstract void renderSword(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);

    /** {@code renderSwordF5(x, y, z, scale)} - the third-person pose, rotations then {@link #renderModel}. */
    protected abstract void renderSwordF5(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);

    /** Hook for models with animation state; called with the baked model right before it is drawn. */
    protected void animate(M model) {}

    /** The vanilla renderer re-bakes its shield and trident here; this one only drops the cached model. */
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.model = null;
    }

    /** {@code renderItem(type, item, data...)} (each Render*.java:36-47). */
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (this.model == null) {
            this.model = bake(Minecraft.getInstance().getEntityModels());
        }
        poseStack.pushPose();
        // Back to the origin of the display transform, i.e. the 1.7.10 hand matrix (ItemRenderer.java:124).
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            // PORT: 1.7.10 had no off-hand. The display transform already mirrors the frame the
            // vanilla way; mirroring x here turns the right-hand pose into its mirror image.
            poseStack.scale(-1.0f, 1.0f, 1.0f);
        }
        if (displayContext.firstPerson()) {
            renderSword(poseStack, buffer, packedLight, packedOverlay);   // EQUIPPED_FIRST_PERSON
        } else {
            renderSwordF5(poseStack, buffer, packedLight, packedOverlay); // EQUIPPED
        }
        poseStack.popPose();
    }

    /**
     * The shared tail of {@code renderSword}/{@code renderSwordF5}: {@code glScalef(scale)},
     * {@code glTranslatef(x, y, z)}, {@code bindTexture(texture)}, {@code model.render()}. The
     * translation is applied after the scale, as in the original - it is in scaled model units.
     * No enchantment glint: the 1.7.10 third-person 3D branch and the {@code IItemRenderer}
     * path drew none, whatever the stack's enchantments.
     */
    protected void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
                               float x, float y, float z, float scale) {
        poseStack.scale(scale, scale, scale);
        poseStack.translate(x, y, z);
        poseStack.scale(16.0f, 16.0f, 16.0f); // ModelPart renders in 1/16; the original used f5 = 1.0
        animate(this.model);
        VertexConsumer consumer = buffer.getBuffer(this.model.renderType(this.texture));
        this.model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay);
    }
}
