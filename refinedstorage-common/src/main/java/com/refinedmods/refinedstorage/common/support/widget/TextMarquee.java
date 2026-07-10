package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TextMarquee {
    private final int maxWidth;
    private final int color;
    private final boolean dropShadow;
    private final Style style;

    private Component text;
    private float offset;
    private float tickAccumulator;
    private State state = State.MOVING_LEFT;

    public TextMarquee(final Component text,
                       final int maxWidth,
                       final int color,
                       final boolean dropShadow,
                       final Style style) {
        this.text = text;
        this.maxWidth = maxWidth;
        this.color = color;
        this.dropShadow = dropShadow;
        this.style = style;
    }

    public TextMarquee(final Component text, final int maxWidth) {
        this(text, maxWidth, -12566464, false, Style.NORMAL);
    }

    private int calculateWidth(final Font font) {
        final boolean small = style == Style.SMALL || style == Style.SMALL_SLOT;
        final float scale = small ? SmallText.correctScale(SmallText.DEFAULT_SCALE) : 1F;
        return (int) (font.width(text) * scale);
    }

    public int getEffectiveWidth(final Font font) {
        return Math.min(maxWidth, calculateWidth(font));
    }

    public void resetState() {
        offset = 0;
        state = State.MOVING_LEFT;
        tickAccumulator = 0;
    }

    public void updateStateAndRender(final GuiGraphicsExtractor graphics, final int x, final int y, final Font font,
                                     final boolean hovering, final float partialTicks) {
        if (!hovering) {
            resetState();
        }
        final int width = calculateWidth(font);
        int correctedOffset = 0;
        final boolean overflow = width > maxWidth;
        // TODO: autocraftable tooltip does not resize in small/large text.
        if (overflow) {
            final int overflowWidth = width - maxWidth;
            if (hovering) {
                updateMarquee(overflowWidth, partialTicks);
            }
            final boolean slot = style == Style.SMALL_SLOT || style == Style.NORMAL_SLOT;
            graphics.enableScissor(x, y, x + maxWidth, y + (slot ? maxWidth : font.lineHeight));
            correctedOffset = (int) offset;
        }
        switch (style) {
            case SMALL -> SmallText.render(
                graphics,
                font,
                text.getVisualOrderText(),
                x + correctedOffset,
                y,
                color,
                dropShadow,
                SmallText.DEFAULT_SCALE
            );
            case SMALL_SLOT -> renderSlot(graphics, x + correctedOffset, y, false, font, !overflow);
            case NORMAL_SLOT -> renderSlot(graphics, x + correctedOffset, y, true, font, !overflow);
            case NORMAL -> graphics.text(font, text, x + correctedOffset, y, color, dropShadow);
        }
        if (overflow) {
            graphics.disableScissor();
        }
    }

    private void renderSlot(final GuiGraphicsExtractor graphics, final int x, final int y, final boolean large,
                            final Font font, final boolean alignRight) {
        graphics.pose().pushMatrix();
        // Large amounts overlap with the slot lines (see Minecraft behavior)
        graphics.pose().translate(x + (large ? 1F : 0F), y + (large ? 1F : 0F));
        if (!large) {
            graphics.pose().scale(0.5F, 0.5F);
        }
        int offsetX = 0;
        if (alignRight) {
            offsetX = (large ? 16 : 30) - font.width(text);
        }
        graphics.text(font, text, offsetX, large ? 8 : 22, color, true);
        graphics.pose().popMatrix();
    }

    private void updateMarquee(final int overflow, final float partialTicks) {
        tickAccumulator += partialTicks;
        offset = state.updateOffset(offset, partialTicks);
        if (state.isTransition(offset, overflow, tickAccumulator)) {
            state = state.nextState(offset);
            tickAccumulator = 0;
            offset = getUpdatedOffset(overflow);
        }
    }

    private float getUpdatedOffset(final int overflow) {
        if (state == State.MOVING_RIGHT) {
            return -overflow;
        } else if (state == State.MOVING_LEFT) {
            return 0;
        }
        return offset;
    }

    public Component getText() {
        return text;
    }

    public void setText(final Component text) {
        this.text = text;
    }

    enum State {
        MOVING_LEFT(1.5F),
        MOVING_RIGHT(1.5F),
        PAUSE(30F);

        private final float value;

        State(final float value) {
            this.value = value;
        }

        float updateOffset(final float currentOffset, final float partialTicks) {
            return switch (this) {
                case MOVING_LEFT -> currentOffset - value * partialTicks;
                case MOVING_RIGHT -> currentOffset + value * partialTicks;
                case PAUSE -> currentOffset;
            };
        }

        boolean isTransition(final float currentOffset, final int overflow, final float accumulator) {
            return switch (this) {
                case MOVING_LEFT -> currentOffset <= -overflow;
                case MOVING_RIGHT -> currentOffset >= 0;
                case PAUSE -> accumulator >= value;
            };
        }

        State nextState(final float currentOffset) {
            return switch (this) {
                case MOVING_LEFT, MOVING_RIGHT -> PAUSE;
                case PAUSE -> currentOffset < 0 ? MOVING_RIGHT : MOVING_LEFT;
            };
        }
    }

    public enum Style {
        NORMAL,
        NORMAL_SLOT,
        SMALL,
        SMALL_SLOT
    }
}
