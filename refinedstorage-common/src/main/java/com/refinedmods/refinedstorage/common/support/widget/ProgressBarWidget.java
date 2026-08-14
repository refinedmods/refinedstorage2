package com.refinedmods.refinedstorage.common.support.widget;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class ProgressBarWidget extends AbstractWidget {
    private static final Identifier CONTAINER = createIdentifier("widget/progress_bar/container");
    private static final Identifier VERTICAL = createIdentifier("widget/progress_bar/vertical");
    private static final Identifier HORIZONTAL = createIdentifier("widget/progress_bar/horizontal");

    private final DoubleSupplier progressSupplier;
    @Nullable
    private final Supplier<List<Component>> tooltipSupplier;

    public ProgressBarWidget(final int x, final int y, final int width, final int height,
                             final DoubleSupplier progressSupplier,
                             @Nullable final Supplier<List<Component>> tooltipSupplier) {
        super(x, y, width, height, Component.empty());
        this.progressSupplier = progressSupplier;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void playDownSound(final SoundManager handler) {
        // intentionally empty
    }

    @Override
    protected void extractWidgetRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                            final float partialTicks) {
        if (height >= width) {
            renderVertical(graphics, getX(), getY(), width, height, progressSupplier.getAsDouble());
        } else {
            renderHorizontal(graphics, getX(), getY(), width, height, progressSupplier.getAsDouble());
        }
        if (tooltipSupplier != null && isHovered) {
            graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltipSupplier.get(), mouseX,
                mouseY);
        }
    }

    public static void renderVertical(final GuiGraphicsExtractor graphics, final int x,
                                      final int y, final int width, final int height,
                                      final double progress) {
        final int correctedHeight = (int) (progress * (height - 2));
        final int correctedY = y + height - correctedHeight - 1;
        final int u = 0;
        final int v = height - correctedHeight;
        graphics.blitSprite(GUI_TEXTURED, CONTAINER, x, y, width, height);
        graphics.blitSprite(
            GUI_TEXTURED, VERTICAL, width, height, u, v, x + 1, correctedY, width - 2, correctedHeight
        );
    }

    public static void renderHorizontal(final GuiGraphicsExtractor graphics, final int x, final int y,
                                        final int width, final int height, final double progress) {
        final int correctedWidth = (int) (progress * (width - 2));
        graphics.blitSprite(GUI_TEXTURED, CONTAINER, x, y, width, height);
        graphics.blitSprite(GUI_TEXTURED, HORIZONTAL, width, height - 2, 0, 0, x + 1, y + 1, correctedWidth,
            height - 2);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // intentionally empty
    }
}
