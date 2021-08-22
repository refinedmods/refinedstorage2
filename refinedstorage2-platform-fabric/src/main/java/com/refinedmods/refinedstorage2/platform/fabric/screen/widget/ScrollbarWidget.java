package com.refinedmods.refinedstorage2.platform.fabric.screen.widget;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ScrollbarWidget extends DrawableHelper implements Element, Drawable, Selectable {
    private static final Identifier TEXTURE = Rs2Mod.createIdentifier("textures/gui/widgets.png");
    private static final int SCROLLER_HEIGHT = 15;

    private static final int ANIMATION_SCROLL_DURATION_IN_TICKS = 10;
    private static final double ANIMATION_SCROLL_HEIGHT_IN_PIXELS = 30;

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private double offset;
    private double maxOffset;
    private boolean enabled = true;
    private boolean clicked;
    private boolean scrollAnimation;

    private int animationScrollDirection = 0;
    private double animationStartOffset;
    private double animationTickCounter;
    private int animationSpeed;

    public ScrollbarWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setScrollAnimation(boolean scrollAnimation) {
        this.scrollAnimation = scrollAnimation;
    }

    public boolean isScrollAnimation() {
        return scrollAnimation;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (isAnimatingScroll()) {
            updateScrollingAnimation(partialTicks);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int enabledU = clicked ? 220 : 232;
        int u = enabled ? enabledU : 244;

        drawTexture(matrixStack, x, y + (int) ((float) offset / (float) maxOffset * (height - SCROLLER_HEIGHT)), u, 0, 12, 15);
    }

    private boolean isAnimatingScroll() {
        return animationScrollDirection != 0;
    }

    private void updateScrollingAnimation(float partialTicks) {
        double absoluteAnimationProgress = animationTickCounter / ANIMATION_SCROLL_DURATION_IN_TICKS;
        double relativeAnimationProgress = easeOutQuint(absoluteAnimationProgress);

        double scrollHeight = ANIMATION_SCROLL_HEIGHT_IN_PIXELS + ((animationSpeed + 1) * 4D);
        double newOffset = animationStartOffset + (relativeAnimationProgress * scrollHeight * animationScrollDirection);
        setOffset(newOffset);

        animationTickCounter += partialTicks;

        if (absoluteAnimationProgress > 1) {
            animationStartOffset = 0;
            animationScrollDirection = 0;
            animationTickCounter = 0;
            animationSpeed = 0;
        }
    }

    private double easeOutQuint(double absoluteProgress) {
        return 1D - Math.pow(1D - absoluteProgress, 5D);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (clicked && mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height) {
            updateOffset(mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height) {
            updateOffset(mouseY);
            clicked = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (clicked) {
            clicked = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (enabled) {
            int scrollDirection = Math.max(Math.min(-(int) scrollDelta, 1), -1);
            if (scrollAnimation) {
                startScrollAnimation(scrollDirection);
            } else {
                setOffset(offset + scrollDirection);
            }
            return true;
        }
        return false;
    }

    private void startScrollAnimation(int scrollDirection) {
        if (isAnimatingScroll()) {
            animationSpeed++;
        } else {
            animationSpeed = 0;
        }
        animationStartOffset = offset;
        animationScrollDirection = scrollDirection;
        animationTickCounter = 0;
    }

    public void setMaxOffset(double maxOffset) {
        this.maxOffset = Math.max(0, maxOffset);
        if (this.offset > this.maxOffset) {
            this.offset = this.maxOffset;
        }
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = Math.min(Math.max(0, offset), maxOffset);
    }

    private void updateOffset(double mouseY) {
        setOffset(Math.floor((mouseY - y) / (height - SCROLLER_HEIGHT) * maxOffset));
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // intentionally empty
    }
}
