package com.swbr.orespawn.client.renderer.robot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.Robot5Model;
import com.swbr.orespawn.entity.robot.Robot5;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRobot5} (RenderRobot5.java:9-49), registered with the model argument, shadow and scale
 * of the original ({@code new RenderRobot5(new ModelRobot5(1.0f), 0.5f, 1.0f)}, manifest): {@code ctx -> new RenderRobot5(ctx, 1.0f, 0.5f, 1.0f)}.
 *
 * <p>Texture {@code Robot5texture.png}, lower-cased under {@code textures/entity/} by tools/assets.py.
 */
public class RenderRobot5 extends MobRenderer<Robot5, Robot5Model> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/robot5texture.png");

    protected Robot5Model model;
    private float scale;

    /**
     * {@code RenderRobot5(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelRobot5} constructor argument
     */
    public RenderRobot5(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new Robot5Model(context.bakeLayer(Robot5Model.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale, scale, scale)}. */
    @Override
    protected void scale(final Robot5 par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Robot5 entity) {
        return texture;
    }
}
