package com.swbr.orespawn.client.renderer.boss.queen;

import com.swbr.orespawn.entity.boss.queen.QueenHead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;

/**
 * Port of {@code danger.orespawn.RenderQueenHead} (RenderQueenHead.java:8-32): every method is empty and the texture is
 * {@code null}, so the head is never drawn - {@link NoopRenderer}. The {@code ModelTheQueen} and shadow arguments of the
 * original constructor (manifest {@code (None, 0.0f, 0.0f)}) were never used: shadow 0.
 * Register as {@code RenderQueenHead::new}.
 */
public class RenderQueenHead extends NoopRenderer<QueenHead> {

    public RenderQueenHead(final EntityRendererProvider.Context context) {
        super(context);
    }
}
