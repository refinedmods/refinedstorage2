package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// A custom checkbox so that we can change the font color and size.
public class CheckboxWidget extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace(
        "widget/checkbox_selected_highlighted"
    );
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace(
        "widget/checkbox_selected"
    );
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace(
        "widget/checkbox_highlighted"
    );
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox");
    private static final int CHECKBOX_TEXT_SPACING = 4;

    private boolean selected;
    private final TextMarquee marquee;
    private final Size size;

    @Nullable
    private OnPressed onPressed;
    @Nullable
    private Component helpTooltip;

    public CheckboxWidget(final int x,
                          final int y,
                          final Component text,
                          final Font font,
                          final boolean selected,
                          final Size size) {
        this(x, y, size.widthHeight + CHECKBOX_TEXT_SPACING + font.width(text), text, font, selected, size);
    }

    public CheckboxWidget(final int x,
                          final int y,
                          final int maxWidth,
                          final Component text,
                          final Font font,
                          final boolean selected,
                          final Size size) {
        super(x, y, getWidth(maxWidth, text, font, size), size.widthHeight, text);
        this.marquee = new TextMarquee(text, maxWidth - CHECKBOX_TEXT_SPACING - size.widthHeight);
        this.selected = selected;
        this.size = size;
    }

    private static int getWidth(final int maxWidth, final Component text, final Font font, final Size size) {
        return Math.min(maxWidth, size.widthHeight + CHECKBOX_TEXT_SPACING + font.width(text));
    }

    public void setHelpTooltip(@Nullable final Component helpTooltip) {
        this.helpTooltip = helpTooltip;
    }

    public void setOnPressed(@Nullable final OnPressed onPressed) {
        this.onPressed = onPressed;
    }

    public void onPress() {
        this.selected = !this.selected;
        if (onPressed != null) {
            onPressed.onPressed(this, selected);
        }
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void updateWidgetNarration(final NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, createNarrationMessage());
        if (active) {
            if (isFocused()) {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (isHovered && helpTooltip != null && minecraft.screen instanceof AbstractBaseScreen<?> screen) {
            screen.setDeferredTooltip(List.of(HelpClientTooltipComponent.createAlwaysDisplayed(helpTooltip)));
        }
        RenderSystem.enableDepthTest();
        final Font font = minecraft.font;
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        final ResourceLocation sprite;
        if (selected) {
            sprite = isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            sprite = isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }
        graphics.blitSprite(sprite, getX(), getY(), size.widthHeight, size.widthHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        final int textX = getX() + size.widthHeight + CHECKBOX_TEXT_SPACING;
        final int textY = (getY() + (height >> 1)) - (9 >> 1);
        marquee.render(graphics, textX, textY, font, isHovered, partialTicks);
    }

    @FunctionalInterface
    public interface OnPressed {
        void onPressed(CheckboxWidget checkbox, boolean selected);
    }

    public enum Size {
        REGULAR(9 + 8),
        SMALL(9);

        private final int widthHeight;

        Size(final int widthHeight) {
            this.widthHeight = widthHeight;
        }
    }
}
