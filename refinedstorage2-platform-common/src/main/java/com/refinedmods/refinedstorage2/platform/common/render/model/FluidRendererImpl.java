package com.refinedmods.refinedstorage2.platform.common.render.model;

import com.refinedmods.refinedstorage2.platform.common.render.FluidRenderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class FluidRendererImpl implements FluidRenderer {
    protected void render(PoseStack poseStack, int x, int y, int z, int packedRgb, TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.atlas().getId());

        int r = (packedRgb >> 16 & 255);
        int g = (packedRgb >> 8 & 255);
        int b = (packedRgb & 255);

        int slotXEnd = x + 16;
        int slotYEnd = y + 16;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferBuilder.vertex(poseStack.last().pose(), x, slotYEnd, z).uv(sprite.getU0(), sprite.getV1()).color(r, g, b, 255).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), slotXEnd, slotYEnd, z).uv(sprite.getU1(), sprite.getV1()).color(r, g, b, 255).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), slotXEnd, y, z).uv(sprite.getU1(), sprite.getV0()).color(r, g, b, 255).endVertex();
        bufferBuilder.vertex(poseStack.last().pose(), x, y, z).uv(sprite.getU0(), sprite.getV0()).color(r, g, b, 255).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
}
