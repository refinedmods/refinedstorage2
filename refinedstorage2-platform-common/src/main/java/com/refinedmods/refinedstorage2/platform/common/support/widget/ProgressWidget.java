package com.refinedmods.refinedstorage2.platform.common.support.widget;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ProgressWidget extends AbstractWidget {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/widgets.png");

    private final DoubleSupplier progressSupplier;
    private final Supplier<List<Component>> tooltipSupplier;

    public ProgressWidget(final int x,
                          final int y,
                          final int width,
                          final int height,
                          final DoubleSupplier progressSupplier,
                          final Supplier<List<Component>> tooltipSupplier) {
        super(x, y, width, height, Component.empty());
        this.progressSupplier = progressSupplier;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void renderWidget(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        final int correctedHeight = (int) (progressSupplier.getAsDouble() * height);
        graphics.blit(TEXTURE, getX(), getY() + height - correctedHeight, 179, height - correctedHeight, width,
            correctedHeight);
        if (isHovered) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltipSupplier.get(), mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // intentionally empty
    }
}
