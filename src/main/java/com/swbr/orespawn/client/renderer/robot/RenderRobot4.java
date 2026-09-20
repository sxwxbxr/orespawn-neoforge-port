package com.swbr.orespawn.client.renderer.robot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.Robot4Model;
import com.swbr.orespawn.entity.robot.Robot4;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRobot4} (RenderRobot4.java:9-49), registered with the model argument, shadow and scale
 * of the original ({@code new RenderRobot4(new ModelRobot4(1.0f), 1.0f, 1.0f)}, manifest): {@code ctx -> new RenderRobot4(ctx, 1.0f, 1.0f, 1.0f)}.
 *
 * <p>Texture {@code Robot4.png}, lower-cased under {@code textures/entity/} by tools/assets.py.
 */
public class RenderRobot4 extends MobRenderer<Robot4, Robot4Model> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/robot4.png");

    protected Robot4Model model;
    private float scale;

    /**
     * {@code RenderRobot4(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelRobot4} constructor argument
     */
    public RenderRobot4(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new Robot4Model(context.bakeLayer(Robot4Model.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale, scale, scale)}. */
    @Override
    protected void scale(final Robot4 par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Robot4 entity) {
        return texture;
    }
}
