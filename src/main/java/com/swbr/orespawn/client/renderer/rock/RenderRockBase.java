package com.swbr.orespawn.client.renderer.rock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.swbr.orespawn.OreSpawn;
import com.swbr.orespawn.client.model.RockBaseModel;
import com.swbr.orespawn.entity.rock.RockBase;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port of {@code danger.orespawn.RenderRockBase} (RenderRockBase.java:9-109), a {@code RenderLiving}
 * and therefore a {@link MobRenderer} (name tag and leash as in 1.7.10). Registered as
 * {@code new RenderRockBase(new ModelRockBase(1.0f), 0.0f, 1.0f)} (manifest {@code renderer_args}):
 * shadow {@code 0.0 * 1.0}, scale 1.0.
 *
 * <p>Texture by type (:53-108; files moved to {@code textures/entity/} and lower-cased by the asset
 * generator): 1, 2, 7 and anything else {@code rocktexture}; 3 red, 4 green, 5 blue, 6 purple,
 * 8 tnt; 9 {@code rockcrystaltexture}, 10 crystal green, 11 crystal blue, 12 crystal tnt.
 */
public class RenderRockBase extends MobRenderer<RockBase, RockBaseModel> {

    private static final ResourceLocation texture1 = texture("rocktexture");
    private static final ResourceLocation texture2 = texture("rocktexture");
    private static final ResourceLocation texture3 = texture("rockredtexture");
    private static final ResourceLocation texture4 = texture("rockgreentexture");
    private static final ResourceLocation texture5 = texture("rockbluetexture");
    private static final ResourceLocation texture6 = texture("rockpurpletexture");
    private static final ResourceLocation texture7 = texture("rocktexture");
    private static final ResourceLocation texture8 = texture("rocktnttexture");
    private static final ResourceLocation texture9 = texture("rockcrystaltexture");
    private static final ResourceLocation texture10 = texture("rockcrystalgreentexture");
    private static final ResourceLocation texture11 = texture("rockcrystalbluetexture");
    private static final ResourceLocation texture12 = texture("rockcrystaltnttexture");

    private float scale;

    /** {@code RenderRockBase(ModelRockBase, float, float)} (:26-31): shadow {@code par2 * par3}, scale {@code par3}. */
    public RenderRockBase(final EntityRendererProvider.Context context, final float par2, final float par3) {
        super(context, new RockBaseModel(context.bakeLayer(RockBaseModel.LAYER), 1.0f), par2 * par3);
        this.scale = 1.0f;
        this.scale = par3;
    }

    /** {@code preRenderCallback} / {@code preRenderScale} (:45-51). */
    @Override
    protected void scale(final RockBase entity, final PoseStack poseStack, final float partialTick) {
        poseStack.scale(this.scale, this.scale, this.scale);
    }

    /**
     * The crystal pass of {@code ModelRockBase.render} (:175-194) needs a blending buffer; the model
     * receives only the one buffer opened here, so the translucent type is chosen here for types
     * 9-12 (DECISIONS R8). Invisible and glowing rocks keep the vanilla choice.
     */
    @Nullable
    @Override
    protected RenderType getRenderType(final RockBase entity, final boolean bodyVisible, final boolean translucent,
                                       final boolean glowing) {
        if (bodyVisible && !translucent && RockBaseModel.isCrystal(entity.getRockType())) {
            return RenderType.entityTranslucent(this.getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    /** {@code getEntityTexture} (:53-93). */
    @Override
    public ResourceLocation getTextureLocation(final RockBase entity) {
        final int i = entity.getRockType();
        if (i == 1) {
            return texture1;
        }
        if (i == 2) {
            return texture2;
        }
        if (i == 3) {
            return texture3;
        }
        if (i == 4) {
            return texture4;
        }
        if (i == 5) {
            return texture5;
        }
        if (i == 6) {
            return texture6;
        }
        if (i == 7) {
            return texture7;
        }
        if (i == 8) {
            return texture8;
        }
        if (i == 9) {
            return texture9;
        }
        if (i == 10) {
            return texture10;
        }
        if (i == 11) {
            return texture11;
        }
        if (i == 12) {
            return texture12;
        }
        return texture1;
    }

    private static ResourceLocation texture(final String name) {
        return ResourceLocation.fromNamespaceAndPath(OreSpawn.MOD_ID, "textures/entity/" + name + ".png");
    }
}
