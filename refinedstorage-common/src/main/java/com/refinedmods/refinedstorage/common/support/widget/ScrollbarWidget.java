package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.Platform;

import java.util.function.DoubleConsumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class ScrollbarWidget extends AbstractWidget {
    private static final int SCROLLER_HEIGHT = 15;

    private static final int ANIMATION_SCROLL_DURATION_IN_TICKS = 10;
    private static final double ANIMATION_SCROLL_HEIGHT_IN_PIXELS = 30;

    private final boolean smoothScrolling;
    private final Type type;

    private double offset;
    private double maxOffset;
    private boolean enabled = true;
    private boolean clicked;

    private int animationScrollDirection = 0;
    private double animationStartOffset;
    private double animationTickCounter;
    private int animationSpeed;
    @Nullable
    private DoubleConsumer listener;

    public ScrollbarWidget(final int x, final int y, final Type type, final int height) {
        super(x, y, type.width, height, Component.empty());
        this.smoothScrolling = Platform.INSTANCE.getConfig().isSmoothScrolling();
        this.type = type;
    }

    public void setListener(@Nullable final DoubleConsumer listener) {
        this.listener = listener;
    }

    public boolean isSmoothScrolling() {
        return smoothScrolling;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    private Identifier getTexture() {
        if (!enabled) {
            return type.disabledTexture;
        }
        return clicked ? type.clickedTexture : type.texture;
    }

    @Override
    public void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                         final float partialTicks) {
        if (isAnimatingScroll()) {
            updateScrollingAnimation(partialTicks);
        }
        graphics.blitSprite(
            GUI_TEXTURED,
            getTexture(),
            getX(),
            getY() + (int) ((float) offset / (float) maxOffset * (height - SCROLLER_HEIGHT)),
            type.width,
            SCROLLER_HEIGHT
        );
    }

    private boolean isAnimatingScroll() {
        return animationScrollDirection != 0;
    }

    private void updateScrollingAnimation(final float partialTicks) {
        final double absoluteAnimationProgress = animationTickCounter / ANIMATION_SCROLL_DURATION_IN_TICKS;
        final double relativeAnimationProgress = easeOutQuint(absoluteAnimationProgress);

        final double scrollHeight = ANIMATION_SCROLL_HEIGHT_IN_PIXELS + ((animationSpeed + 1) * 4D);
        final double newOffset = animationStartOffset
            + (relativeAnimationProgress * scrollHeight * animationScrollDirection);
        setOffset(newOffset);

        animationTickCounter += partialTicks;

        if (absoluteAnimationProgress > 1) {
            animationStartOffset = 0;
            animationScrollDirection = 0;
            animationTickCounter = 0;
            animationSpeed = 0;
        }
    }

    private static double easeOutQuint(final double absoluteProgress) {
        return 1D - Math.pow(1D - absoluteProgress, 5D);
    }

    @Override
    public void mouseMoved(final double mouseX, final double mouseY) {
        final boolean inBounds = mouseX >= getX()
            && mouseY >= getY()
            && mouseX <= getX() + width
            && mouseY <= getY() + height;
        if (clicked && inBounds) {
            updateOffset(mouseY);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (!isActive()) {
            return false;
        }
        final int button = event.button();
        final double mouseX = event.x();
        final double mouseY = event.y();
        final boolean inBounds = mouseX >= getX()
            && mouseY >= getY()
            && mouseX <= getX() + width
            && mouseY <= getY() + height;
        if (button == 0 && inBounds) {
            updateOffset(mouseY);
            clicked = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (clicked) {
            clicked = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (enabled) {
            // TODO: Clamp
            final int scrollDirection = Math.max(Math.min(-(int) scrollY, 1), -1);
            if (smoothScrolling) {
                startScrollAnimation(scrollDirection);
            } else {
                setOffset(offset + scrollDirection);
            }
            return true;
        }
        return false;
    }

    private void startScrollAnimation(final int scrollDirection) {
        if (isAnimatingScroll()) {
            animationSpeed++;
        } else {
            animationSpeed = 0;
        }
        animationStartOffset = offset;
        animationScrollDirection = scrollDirection;
        animationTickCounter = 0;
    }

    public void setMaxOffset(final double maxOffset) {
        this.maxOffset = Math.max(0, maxOffset);
        if (this.offset > this.maxOffset) {
            this.offset = this.maxOffset;
            if (listener != null) {
                listener.accept(this.offset);
            }
        }
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(final double offset) {
        // TODO: Clamp
        this.offset = Math.min(Math.max(0, offset), maxOffset);
        if (listener != null) {
            listener.accept(this.offset);
        }
    }

    private void updateOffset(final double mouseY) {
        setOffset(Math.floor((mouseY - SCROLLER_HEIGHT / 2.0 - getY()) / (height - SCROLLER_HEIGHT) * maxOffset));
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // intentionally empty
    }

    public enum Type {
        NORMAL(
            createIdentifier("widget/scrollbar"),
            createIdentifier("widget/scrollbar_clicked"),
            createIdentifier("widget/scrollbar_disabled"),
            12
        ),
        SMALL(
            createIdentifier("widget/small_scrollbar"),
            createIdentifier("widget/small_scrollbar_clicked"),
            createIdentifier("widget/small_scrollbar_disabled"),
            7
        );

        private final Identifier texture;
        private final Identifier clickedTexture;
        private final Identifier disabledTexture;
        private final int width;

        Type(final Identifier texture,
             final Identifier clickedTexture,
             final Identifier disabledTexture,
             final int width) {
            this.texture = texture;
            this.clickedTexture = clickedTexture;
            this.disabledTexture = disabledTexture;
            this.width = width;
        }
    }
}
