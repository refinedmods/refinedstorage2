package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ScrollbarWidget extends AbstractWidget {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/widgets.png");
    private static final int SCROLLER_HEIGHT = 15;

    private static final int ANIMATION_SCROLL_DURATION_IN_TICKS = 10;
    private static final double ANIMATION_SCROLL_HEIGHT_IN_PIXELS = 30;

    private double offset;
    private double maxOffset;
    private boolean enabled = true;
    private boolean clicked;
    private boolean scrollAnimation;

    private int animationScrollDirection = 0;
    private double animationStartOffset;
    private double animationTickCounter;
    private int animationSpeed;

    public ScrollbarWidget(final int x, final int y, final int width, final int height) {
        super(x, y, width, height, Component.empty());
    }

    public boolean isScrollAnimation() {
        return scrollAnimation;
    }

    public void setScrollAnimation(final boolean scrollAnimation) {
        this.scrollAnimation = scrollAnimation;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        if (isAnimatingScroll()) {
            updateScrollingAnimation(partialTicks);
        }

        final int enabledU = clicked ? 220 : 232;
        final int u = enabled ? enabledU : 244;

        graphics.blit(
            TEXTURE,
            getX(),
            getY() + (int) ((float) offset / (float) maxOffset * (height - SCROLLER_HEIGHT)),
            u,
            0,
            12,
            15
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
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
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
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (clicked) {
            clicked = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollDelta) {
        if (enabled) {
            final int scrollDirection = Math.max(Math.min(-(int) scrollDelta, 1), -1);
            if (scrollAnimation) {
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
        }
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(final double offset) {
        this.offset = Math.min(Math.max(0, offset), maxOffset);
    }

    private void updateOffset(final double mouseY) {
        setOffset(Math.floor((mouseY - getY()) / (height - SCROLLER_HEIGHT) * maxOffset));
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // intentionally empty
    }
}
