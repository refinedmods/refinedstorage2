package com.refinedmods.refinedstorage2.platform.fabric.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class ScreenUtil {
    private static final List<String> VERSION_INFO_LINES = new ArrayList<>();

    private ScreenUtil() {
    }

    public static void drawVersionInformation(PoseStack matrixStack, Font textRenderer) {
        if (VERSION_INFO_LINES.isEmpty()) {
            loadVersionInformationLines();
        }

        int x = 5;
        int y = 5;

        for (String line : VERSION_INFO_LINES) {
            textRenderer.drawShadow(matrixStack, line, x, y, ChatFormatting.WHITE.getColor());
            y += 9;
        }
    }

    private static void loadVersionInformationLines() {
        VERSION_INFO_LINES.add("Refined Storage for Fabric");

        FabricLoader
                .getInstance()
                .getModContainer("refinedstorage2")
                .flatMap(ScreenUtil::getVersion)
                .ifPresent(version -> VERSION_INFO_LINES.add("v" + version));
    }

    private static Optional<String> getVersion(ModContainer platform) {
        String friendlyString = platform.getMetadata().getVersion().getFriendlyString();
        if ("${version}".equals(friendlyString)) {
            return Optional.empty();
        }
        return Optional.of(friendlyString);
    }

    public static void renderFluid(PoseStack matrices, int x, int y, int z, FluidVariant fluidVariant) {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        if (sprite != null) {
            renderFluidSprite(matrices, x, y, z, fluidVariant, sprite);
        }
    }

    private static void renderFluidSprite(PoseStack poseStack, int x, int y, int z, FluidVariant fluidVariant, TextureAtlasSprite sprite) {
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
        bufferBuilder
                .vertex(poseStack.last().pose(), x, slotYEnd, z)
                .uv(sprite.getU0(), sprite.getV1())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder
                .vertex(poseStack.last().pose(), slotXEnd, slotYEnd, z)
                .uv(sprite.getU1(), sprite.getV1())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder
                .vertex(poseStack.last().pose(), slotXEnd, y, z)
                .uv(sprite.getU1(), sprite.getV0())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder
                .vertex(poseStack.last().pose(), x, y, z)
                .uv(sprite.getU0(), sprite.getV0())
                .color(r, g, b, 255)
                .endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
}
