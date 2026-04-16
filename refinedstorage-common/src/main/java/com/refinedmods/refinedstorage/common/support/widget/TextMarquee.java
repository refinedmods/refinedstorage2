package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TextMarquee {
    private final int maxWidth;
    private final int color;
    private final boolean dropShadow;
    private final boolean small;

    private Component text;
    private float offset;
    private float tickAccumulator;
    private State state = State.MOVING_LEFT;

    public TextMarquee(final Component text,
                       final int maxWidth,
                       final int color,
                       final boolean dropShadow,
                       final boolean small) {
        this.text = text;
        this.maxWidth = maxWidth;
        this.color = color;
        this.dropShadow = dropShadow;
        this.small = small;
    }

    public TextMarquee(final Component text, final int maxWidth) {
        this(text, maxWidth, -12566464, false, false);
    }

    public int getEffectiveWidth(final Font font) {
        return Math.min(maxWidth, font.width(text));
    }

    public void render(final GuiGraphicsExtractor graphics, final int x, final int y, final Font font,
                       final boolean hovering, final float partialTicks) {
        if (!hovering) {
            offset = 0;
            state = State.MOVING_LEFT;
            tickAccumulator = 0;
        }
        final int width = (int) (font.width(text) * (small ? SmallText.correctScale(SmallText.DEFAULT_SCALE) : 1F));
        if (width > maxWidth) {
            final int overflow = width - maxWidth;
            if (hovering) {
                updateMarquee(overflow, partialTicks);
            }
            graphics.enableScissor(x, y, x + maxWidth, y + font.lineHeight);
            if (small) {
                SmallText.render(
                    graphics,
                    font,
                    text.getVisualOrderText(),
                    x + (int) offset,
                    y,
                    color,
                    dropShadow,
                    SmallText.DEFAULT_SCALE
                );
            } else {
                graphics.text(font, text, x + (int) offset, y, color, dropShadow);
            }
            graphics.disableScissor();
        } else {
            if (small) {
                SmallText.render(
                    graphics,
                    font,
                    text.getVisualOrderText(),
                    x,
                    y,
                    color,
                    dropShadow,
                    SmallText.DEFAULT_SCALE
                );
            } else {
                graphics.text(font, text, x, y, color, dropShadow);
            }
        }
    }

    private void updateMarquee(final int overflow, final float partialTicks) {
        tickAccumulator += partialTicks;
        offset = state.updateOffset(offset, partialTicks);
        if (state.isTransition(offset, overflow, tickAccumulator)) {
            state = state.nextState(offset);
            tickAccumulator = 0;
            offset = state == State.MOVING_RIGHT ? -overflow : (state == State.MOVING_LEFT ? 0 : offset);
        }
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
}
