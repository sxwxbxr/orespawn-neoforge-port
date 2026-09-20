package com.swbr.orespawn.client.renderer.dino;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CryolophosaurusModel;
import com.swbr.orespawn.entity.dino.Cryolophosaurus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCryolophosaurus} (RenderCryolophosaurus.java:10-49). ClientProxyOreSpawn.java:49
 * registered {@code new RenderCryolophosaurus(new ModelCryolophosaurus(0.75f), 0.75f, 0.5f)}: register with
 * {@code ctx -> new RenderCryolophosaurus(ctx, 0.75f, 0.5f)}; the model argument is fixed here.
 *
 * <p>Shadow {@code par2 * par3}, {@code glScalef(scale)}, one texture ({@code cryolophosaurus.png}).
 */
public class RenderCryolophosaurus extends MobRenderer<Cryolophosaurus, CryolophosaurusModel> {

    protected CryolophosaurusModel model;
    private float scale;
    private static final ResourceLocation texture =
            ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/cryolophosaurus.png");

    /** {@code RenderCryolophosaurus(model, par2, par3)}: shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderCryolophosaurus(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new CryolophosaurusModel(context.bakeLayer(CryolophosaurusModel.LAYER), 0.75f), par2 * par3);
        this.scale = 1.0f;
        this.model = this.getModel();
        this.scale = par3;
    }

    /** {@code preRenderCallback} -> {@code preRenderScale}: {@code glScalef(scale, scale, scale)} (R8). */
    @Override
    protected void scale(final Cryolophosaurus par1Entity, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /** {@code getEntityTexture}. */
    @Override
    public ResourceLocation getTextureLocation(final Cryolophosaurus entity) {
        return RenderCryolophosaurus.texture;
    }
}
