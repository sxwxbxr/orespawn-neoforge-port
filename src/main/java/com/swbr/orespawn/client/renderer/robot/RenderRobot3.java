package com.swbr.orespawn.client.renderer.robot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.Robot3Model;
import com.swbr.orespawn.entity.robot.Robot3;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRobot3} (RenderRobot3.java:9-49), registered with the model argument, shadow and scale
 * of the original ({@code new RenderRobot3(new ModelRobot3(1.0f), 1.0f, 0.5f)}, manifest): {@code ctx -> new RenderRobot3(ctx, 1.0f, 1.0f, 0.5f)}.
 *
 * <p>Texture {@code Robot3.png}, lower-cased under {@code textures/entity/} by tools/assets.py.
 */
public class RenderRobot3 extends MobRenderer<Robot3, Robot3Model> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/robot3.png");

    protected Robot3Model model;
    private float scale;

    /**
     * {@code RenderRobot3(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelRobot3} constructor argument
     */
    public RenderRobot3(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new Robot3Model(context.bakeLayer(Robot3Model.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale, scale, scale)}. */
    @Override
    protected void scale(final Robot3 par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Robot3 entity) {
        return texture;
    }
}
