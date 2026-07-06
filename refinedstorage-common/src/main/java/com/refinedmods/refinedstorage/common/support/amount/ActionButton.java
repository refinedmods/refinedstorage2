package com.refinedmods.refinedstorage.common.support.amount;

import javax.annotation.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.support.Sprites.ICON_SIZE;

public class ActionButton extends Button {
    private static final int ICON_SPACING = 4;
    private static final int TEXT_MARGIN = 2;

    @Nullable
    private ActionIcon icon;

    ActionButton(final int x,
                 final int y,
                 final int width,
                 final int height,
                 final Component message,
                 final OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(final GuiGraphics graphics,
                                final int mouseX,
                                final int mouseY,
                                final float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (icon != null) {
            graphics.blitSprite(icon.getSprite(), getX() + ICON_SPACING, getY() + ICON_SPACING,
                ICON_SIZE, ICON_SIZE);
        }
    }

    @Override
    protected void renderScrollingString(final GuiGraphics graphics,
                                         final Font font,
                                         final int width,
                                         final int color) {
        final int offset = icon != null ? (ICON_SIZE + ICON_SPACING) : 0;
        final int left = this.getX() + TEXT_MARGIN + offset;
        final int right = this.getX() + this.getWidth() - TEXT_MARGIN;
        final int top = this.getY();
        final int bottom = this.getY() + this.getHeight();
        renderScrollingString(graphics, font, getMessage(), left, top, right, bottom, color);
    }

    public void setIcon(@Nullable final ActionIcon icon) {
        this.icon = icon;
    }
}
