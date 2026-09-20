package com.swbr.orespawn.client.renderer.critter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.CockateilModel;
import com.swbr.orespawn.entity.critter.Cockateil;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderCockateil} (RenderCockateil.java:9-45), a {@code RenderLiving} with a
 * {@code ModelCockateil}, used for both {@code Cockateil} and {@code RubyBird}. ClientProxyOreSpawn (:60-61):
 * {@code (new ModelCockateil(1.0f), 0.3f, 0.75f)} - register as {@code ctx -> new RenderCockateil(ctx, 0.3f,
 * 0.75f, 1.0f)}. Shadow {@code par2 * par3} (:14), scale {@code par3} (:33-39).
 *
 * <p>The texture is {@code Cockateil.getTexture()} (Cockateil.java:52-76, :263-268), read here from the synced
 * colour (catalogue: the choice belongs to the client renderer).
 */
public class RenderCockateil extends MobRenderer<Cockateil, CockateilModel> {

    // Bird1.png .. Bird6.png, lower-cased under textures/entity/ (manifest texture_map).
    private static final ResourceLocation texture1 = texture("bird1.png");
    private static final ResourceLocation texture2 = texture("bird2.png");
    private static final ResourceLocation texture3 = texture("bird3.png");
    private static final ResourceLocation texture4 = texture("bird4.png");
    private static final ResourceLocation texture5 = texture("bird5.png");
    private static final ResourceLocation texture6 = texture("bird6.png");

    protected CockateilModel model;
    private float scale = 1.0f;

    /** {@code RenderCockateil(model, par2, par3)} (:13-18) with the model's {@code wingspeed}. */
    public RenderCockateil(final EntityRendererProvider.Context context, final float par2, final float par3, final float wingspeed) {
        super(context, new CockateilModel(context.bakeLayer(CockateilModel.LAYER), wingspeed), par2 * par3);
        this.model = this.getModel();
        this.scale = par3;
    }

    private static ResourceLocation texture(final String file) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + file);
    }

    /** {@code preRenderCallback} → {@code preRenderScale} (:33-39). */
    @Override
    protected void scale(final Cockateil par1EntityLiving, final PoseStack poseStack, final float par2) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /**
     * {@code getEntityTexture} (:41-44) → {@code Cockateil.getTexture()}: colour 0..5 → {@code Bird1..6}.
     *
     * <p>PORT: the original returned {@code null} for any other colour (a {@code BirdType} from a foreign NBT
     * tag), which crashed the texture manager; 1.21.1 would crash the same way, so the first texture stands in
     * (R18 case 1).
     */
    @Override
    public ResourceLocation getTextureLocation(final Cockateil entity) {
        switch (entity.birdtype = entity.getBirdType()) {
            case 0:
                return texture1;
            case 1:
                return texture2;
            case 2:
                return texture3;
            case 3:
                return texture4;
            case 4:
                return texture5;
            case 5:
                return texture6;
            default:
                return texture1;
        }
    }
}
