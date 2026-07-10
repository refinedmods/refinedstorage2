package com.refinedmods.refinedstorage.common.support.amount;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.Sprites.ICON_SIZE;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class ActionButton extends Button {
    private static final int ICON_SPACING = 4;

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
    protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                   final float partialTicks) {
        extractDefaultSprite(graphics);
        if (icon != null) {
            graphics.blitSprite(GUI_TEXTURED, icon.getSprite(), getX() + ICON_SPACING, getY() + ICON_SPACING,
                ICON_SIZE, ICON_SIZE);
        }
        extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }

    @Override
    public void extractScrollingStringOverContents(final ActiveTextCollector output, final Component message,
                                                   final int margin) {
        final int offset = icon != null ? (ICON_SIZE + ICON_SPACING) : 0;
        final int left = this.getX() + margin + offset;
        final int right = this.getX() + this.getWidth() - margin;
        final int top = this.getY();
        final int bottom = this.getY() + this.getHeight();
        output.acceptScrollingWithDefaultCenter(message, left, right, top, bottom);
    }

    public void setIcon(@Nullable final ActionIcon icon) {
        this.icon = icon;
    }
}
