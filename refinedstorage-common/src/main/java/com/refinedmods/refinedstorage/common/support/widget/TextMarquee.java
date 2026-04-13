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
    private int offset;
    private int stateTicks;
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
                       final boolean hovering) {
        if (!hovering) {
            offset = 0;
            state = State.MOVING_LEFT;
            stateTicks = 0;
        }
        final int width = (int) (font.width(text) * (small ? SmallText.correctScale(SmallText.DEFAULT_SCALE) : 1F));
        if (width > maxWidth) {
            final int overflow = width - maxWidth;
            if (hovering) {
                updateMarquee(overflow);
            }
            graphics.enableScissor(x, y, x + maxWidth, y + font.lineHeight);
            if (small) {
                SmallText.render(
                    graphics,
                    font,
                    text.getVisualOrderText(),
                    x + offset,
                    y,
                    color,
                    dropShadow,
                    SmallText.DEFAULT_SCALE
                );
            } else {
                graphics.text(font, text, x + offset, y, color, dropShadow);
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

    private void updateMarquee(final int overflow) {
        stateTicks++;
        if (stateTicks % state.ticks == 0) {
            offset = state.updateOffset(offset);
            state = state.nextState(offset, overflow);
            stateTicks = 0;
        }
    }

    public Component getText() {
        return text;
    }

    public void setText(final Component text) {
        this.text = text;
    }

    enum State {
        MOVING_LEFT(2),
        MOVING_RIGHT(2),
        PAUSE(30);

        private final int ticks;

        State(final int ticks) {
            this.ticks = ticks;
        }

        int updateOffset(final int currentOffset) {
            return switch (this) {
                case MOVING_LEFT -> currentOffset - 1;
                case MOVING_RIGHT -> currentOffset + 1;
                case PAUSE -> currentOffset;
            };
        }

        State nextState(final int currentOffset, final int overflow) {
            return switch (this) {
                case MOVING_LEFT -> currentOffset > -overflow ? MOVING_LEFT : PAUSE;
                case MOVING_RIGHT -> currentOffset < 0 ? MOVING_RIGHT : PAUSE;
                case PAUSE -> currentOffset < 0 ? MOVING_RIGHT : MOVING_LEFT;
            };
        }
    }
}
