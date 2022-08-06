package com.refinedmods.refinedstorage2.platform.common.screen;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;

public class SmallTextTooltipRenderer extends GuiComponent {
    public static final SmallTextTooltipRenderer INSTANCE = new SmallTextTooltipRenderer();

    private SmallTextTooltipRenderer() {
    }

    public void render(@Nullable final Minecraft minecraft,
                       final Font font,
                       final PoseStack poseStack,
                       final List<? extends FormattedCharSequence> lines,
                       final List<? extends FormattedCharSequence> smallLines,
                       final int x,
                       final int y,
                       final int screenWidth,
                       final int screenHeight) {
        if (lines.isEmpty()) {
            return;
        }
        final float smallTextScale = (minecraft != null && minecraft.isEnforceUnicode()) ? 1F : 0.7F;
        final int tooltipWidth = calculateWidth(font, lines, smallLines, smallTextScale);
        final int tooltipHeight = calculateHeight(font, lines, smallLines);
        final int tooltipX = calculateTooltipX(x, screenWidth, tooltipWidth);
        final int tooltipY = calculateTooltipY(y, screenHeight, tooltipHeight);
        render(font, poseStack, lines, smallLines, smallTextScale, tooltipWidth, tooltipHeight, tooltipX, tooltipY);
    }

    private void render(final Font font,
                        final PoseStack poseStack,
                        final List<? extends FormattedCharSequence> lines,
                        final List<? extends FormattedCharSequence> smallLines,
                        final float smallTextScale,
                        final int tooltipWidth,
                        final int tooltipHeight,
                        final int tooltipX,
                        final int tooltipY) {
        poseStack.pushPose();
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        final Matrix4f matrix4f = poseStack.last().pose();
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 4,
            tooltipX + tooltipWidth + 3, tooltipY - 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY + tooltipHeight + 3,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 4, tooltipY - 3,
            tooltipX - 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX + tooltipWidth + 3, tooltipY - 3,
            tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3 + 1,
            tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY - 3,
            tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, 400, 1347420415, 1347420415);
        fillGradient(matrix4f, bufferBuilder, tooltipX - 3, tooltipY + tooltipHeight + 2,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, 1344798847, 1344798847);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        final MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(
            Tesselator.getInstance().getBuilder()
        );
        poseStack.translate(0.0D, 0.0D, 400.0D);

        renderText(font, poseStack, lines, smallLines, smallTextScale, tooltipX, tooltipY, matrix4f, immediate);

        immediate.endBatch();
        poseStack.popPose();
    }

    private void renderText(final Font font,
                            final PoseStack poseStack,
                            final List<? extends FormattedCharSequence> lines,
                            final List<? extends FormattedCharSequence> smallLines,
                            final float smallTextScale,
                            final int tooltipX,
                            final int tooltipY,
                            final Matrix4f matrix4f,
                            final MultiBufferSource.BufferSource immediate) {
        int tooltipTextY = tooltipY;
        for (final FormattedCharSequence text : lines) {
            if (text != null) {
                font.drawInBatch(text, tooltipX, tooltipTextY, -1, true, matrix4f, immediate, false, 0, 15728880);
            }
            tooltipTextY += 12;
        }

        for (final FormattedCharSequence smallLine : smallLines) {
            poseStack.pushPose();
            poseStack.scale(smallTextScale, smallTextScale, 1);

            final float x = tooltipX / smallTextScale;
            final float y = tooltipTextY / smallTextScale;
            font.drawInBatch(smallLine, x, y, -1, true, poseStack.last().pose(), immediate, false, 0, 15728880);

            poseStack.popPose();
            tooltipTextY += 9;
        }
    }

    private int calculateWidth(final Font font,
                               final List<? extends FormattedCharSequence> lines,
                               final List<? extends FormattedCharSequence> smallLines,
                               final float smallTextScale) {
        int tooltipWidth = 0;
        for (final FormattedCharSequence text : lines) {
            final int textWidth = font.width(text);
            if (textWidth > tooltipWidth) {
                tooltipWidth = textWidth;
            }
        }
        for (final FormattedCharSequence text : smallLines) {
            final int textWidth = (int) (font.width(text) * smallTextScale);
            if (textWidth > tooltipWidth) {
                tooltipWidth = textWidth;
            }
        }
        return tooltipWidth;
    }


    private int calculateHeight(final Font font,
                                final List<? extends FormattedCharSequence> lines,
                                final List<? extends FormattedCharSequence> smallLines) {
        int tooltipHeight = (lines.size() * 12);
        tooltipHeight += smallLines.size() * 9;
        return tooltipHeight - (Minecraft.getInstance().isEnforceUnicode() ? 0 : 3);
    }

    private int calculateTooltipX(final int x, final int screenWidth, final int tooltipWidth) {
        int tooltipX = x + 12;
        if (tooltipX + tooltipWidth > screenWidth) {
            tooltipX -= 28 + tooltipWidth;
        }
        return tooltipX;
    }

    private int calculateTooltipY(final int y, final int screenHeight, final int tooltipHeight) {
        int tooltipY = y - 12;
        if (tooltipY + tooltipHeight + 6 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 6;
        }
        return tooltipY;
    }
}
