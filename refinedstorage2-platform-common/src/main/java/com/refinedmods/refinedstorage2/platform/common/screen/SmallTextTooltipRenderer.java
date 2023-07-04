package com.refinedmods.refinedstorage2.platform.common.screen;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

public class SmallTextTooltipRenderer {
    public static final SmallTextTooltipRenderer INSTANCE = new SmallTextTooltipRenderer();

    private SmallTextTooltipRenderer() {
    }

    public void render(@Nullable final Minecraft minecraft,
                       final Font font,
                       final GuiGraphics graphics,
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
        final int tooltipHeight = calculateHeight(lines, smallLines);
        final int tooltipX = calculateTooltipX(x, screenWidth, tooltipWidth);
        final int tooltipY = calculateTooltipY(y, screenHeight, tooltipHeight);
        render(font, graphics, lines, smallLines, smallTextScale, tooltipWidth, tooltipHeight, tooltipX, tooltipY);
    }

    private void render(final Font font,
                        final GuiGraphics graphics,
                        final List<? extends FormattedCharSequence> lines,
                        final List<? extends FormattedCharSequence> smallLines,
                        final float smallTextScale,
                        final int tooltipWidth,
                        final int tooltipHeight,
                        final int tooltipX,
                        final int tooltipY) {
        final PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        graphics.fillGradient(tooltipX - 3, tooltipY - 4,
            tooltipX + tooltipWidth + 3, tooltipY - 3, 400, -267386864, -267386864);
        graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 3,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, 400, -267386864, -267386864);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        graphics.fillGradient(tooltipX - 4, tooltipY - 3,
            tooltipX - 3, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        graphics.fillGradient(tooltipX + tooltipWidth + 3, tooltipY - 3,
            tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, 400, -267386864, -267386864);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3 + 1,
            tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        graphics.fillGradient(tooltipX + tooltipWidth + 2, tooltipY - 3 + 1,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, 400, 1347420415, 1344798847);
        graphics.fillGradient(tooltipX - 3, tooltipY - 3,
            tooltipX + tooltipWidth + 3, tooltipY - 3 + 1, 400, 1347420415, 1347420415);
        graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 2,
            tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3, 400, 1344798847, 1344798847);
        poseStack.popPose();
        poseStack.translate(0.0D, 0.0D, 400.0D);
        renderText(font, graphics, lines, smallLines, smallTextScale, tooltipX, tooltipY);
    }

    private void renderText(final Font font,
                            final GuiGraphics graphics,
                            final List<? extends FormattedCharSequence> lines,
                            final List<? extends FormattedCharSequence> smallLines,
                            final float smallTextScale,
                            final int tooltipX,
                            final int tooltipY) {
        int tooltipTextY = tooltipY;
        for (final FormattedCharSequence text : lines) {
            if (text != null) {
                graphics.drawString(font, text, tooltipX, tooltipTextY, 15728880);
            }
            tooltipTextY += 12;
        }

        final PoseStack poseStack = graphics.pose();
        for (final FormattedCharSequence smallLine : smallLines) {
            poseStack.pushPose();
            poseStack.scale(smallTextScale, smallTextScale, 1);

            final int x = (int) (tooltipX / smallTextScale);
            final int y = (int) (tooltipTextY / smallTextScale);
            graphics.drawString(font, smallLine, x, y, 15728880);

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


    private int calculateHeight(final List<? extends FormattedCharSequence> lines,
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
