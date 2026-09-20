package com.swbr.orespawn.client.renderer.robot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.Robot2Model;
import com.swbr.orespawn.entity.robot.Robot2;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRobot2} (RenderRobot2.java:9-49), registered with the model argument, shadow and scale
 * of the original ({@code new RenderRobot2(new ModelRobot2(1.0f), 1.0f, 1.0f)}, manifest): {@code ctx -> new RenderRobot2(ctx, 1.0f, 1.0f, 1.0f)}.
 *
 * <p>Texture {@code Robot2.png}, lower-cased under {@code textures/entity/} by tools/assets.py.
 */
public class RenderRobot2 extends MobRenderer<Robot2, Robot2Model> {

    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/robot2.png");

    protected Robot2Model model;
    private float scale;

    /**
     * {@code RenderRobot2(model, par2, par3)} (:15-20): shadow {@code par2 * par3}, scale {@code par3}.
     *
     * @param wingspeed the {@code ModelRobot2} constructor argument
     */
    public RenderRobot2(final EntityRendererProvider.Context context, final float wingspeed, final float par2, final float par3) {
        super(context, new Robot2Model(context.bakeLayer(Robot2Model.LAYER), wingspeed), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale} (:34-40): {@code glScalef(scale, scale, scale)}. */
    @Override
    protected void scale(final Robot2 par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture} (:42-44). */
    @Override
    public ResourceLocation getTextureLocation(final Robot2 entity) {
        return texture;
    }
}
