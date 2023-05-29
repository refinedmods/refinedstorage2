package com.refinedmods.refinedstorage2.platform.common.render.model;

import com.refinedmods.refinedstorage2.platform.common.render.FluidRenderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class AbstractFluidRenderer implements FluidRenderer {
    protected void render(final PoseStack poseStack,
                          final int x,
                          final int y,
                          final int packedRgb,
                          final TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());

        final int r = packedRgb >> 16 & 255;
        final int g = packedRgb >> 8 & 255;
        final int b = packedRgb & 255;

        final int slotXEnd = x + 16;
        final int slotYEnd = y + 16;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(poseStack.last().pose(), x, slotYEnd, 0)
            .uv(sprite.getU0(), sprite.getV1())
            .color(r, g, b, 255).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), slotXEnd, slotYEnd, 0)
            .uv(sprite.getU1(), sprite.getV1())
            .color(r, g, b, 255).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), slotXEnd, y, 0)
            .uv(sprite.getU1(), sprite.getV0())
            .color(r, g, b, 255).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), x, y, 0)
            .uv(sprite.getU0(), sprite.getV0())
            .color(r, g, b, 255).endVertex();
        tesselator.end();
    }
}
