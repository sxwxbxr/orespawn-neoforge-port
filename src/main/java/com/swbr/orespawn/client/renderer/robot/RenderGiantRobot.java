package com.swbr.orespawn.client.renderer.robot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.GiantRobotModel;
import com.swbr.orespawn.entity.robot.GiantRobot;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderGiantRobot} (RenderGiantRobot.java:9-49), registered with the model argument, shadow and scale
 * of the original ({@code new RenderGiantRobot(new ModelGiantRobot(0.25f), 0.99f, 1.0f)}, manifest): {@code ctx -> new RenderGiantRobot(ctx, 0.25f, 0.99f, 1.0f)}.
 *
 * <p>Texture {@code GiantRobottexture.png}, lower-cased under {@code textures/entity/} by tools/assets.py.
 */
public class RenderGiantRobot extends MobRenderer<GiantRobot, GiantRobotModel> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/giantrobottexture.png");

    protected GiantRobotModel model;
    private float scale;

    /**
     * {@code RenderGiantRobot(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelGiantRobot} constructor argument
     */
    public RenderGiantRobot(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new GiantRobotModel(context.bakeLayer(GiantRobotModel.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale, scale, scale)}. */
    @Override
    protected void scale(final GiantRobot par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final GiantRobot entity) {
        return texture;
    }
}
