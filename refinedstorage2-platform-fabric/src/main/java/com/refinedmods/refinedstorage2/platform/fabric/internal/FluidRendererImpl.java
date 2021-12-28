package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.platform.abstractions.FluidRenderer;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toFluidVariant;

public class FluidRendererImpl implements FluidRenderer {
    @Override
    public void render(PoseStack poseStack, int x, int y, int z, FluidResource fluidResource) {
        FluidVariant fluidVariant = toFluidVariant(fluidResource);
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        if (sprite != null) {
            renderFluidSprite(poseStack, x, y, z, fluidVariant, sprite);
        }
    }

    @Override
    public List<Component> getTooltip(FluidResource fluidResource) {
        return FluidVariantRendering.getTooltip(toFluidVariant(fluidResource), Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    private void renderFluidSprite(PoseStack poseStack, int x, int y, int z, FluidVariant fluidVariant, TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.atlas().getId());

        int packedRgb = FluidVariantRendering.getColor(fluidVariant);
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
