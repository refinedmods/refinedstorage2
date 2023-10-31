package com.refinedmods.refinedstorage2.platform.common.support.tooltip;

import com.refinedmods.refinedstorage2.platform.common.support.TextureIds;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class MouseWithIconClientTooltipComponent implements ClientTooltipComponent {
    private final Type type;
    private final IconRenderer iconRenderer;
    @Nullable
    private final String amount;

    public MouseWithIconClientTooltipComponent(final Type type,
                                               final IconRenderer iconRenderer,
                                               @Nullable final String amount) {
        this.type = type;
        this.iconRenderer = iconRenderer;
        this.amount = amount;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth(final Font font) {
        return 9 + 4 + 18;
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        graphics.blit(TextureIds.ICONS, x + type.leftPad, y, type.x, type.y, type.width, type.height);
        iconRenderer.render(graphics, x + 9 + 4, y);
        if (amount != null) {
            final PoseStack poseStack = graphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, 200.0F);
            graphics.drawString(font, amount, x + 9 + 4 + 16 - font.width(amount), y + 9, 16777215, true);
            poseStack.popPose();
        }
    }

    @FunctionalInterface
    public interface IconRenderer {
        void render(GuiGraphics graphics, int x, int y);
    }

    public enum Type {
        LEFT(247, 0, 180, 9, 13),
        RIGHT(238, 2, 180, 9, 13);

        private final int x;
        private final int leftPad;
        private final int y;
        private final int width;
        private final int height;

        Type(final int x, final int leftPad, final int y, final int width, final int height) {
            this.x = x;
            this.leftPad = leftPad;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
