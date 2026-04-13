package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;

import java.util.function.Consumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;

interface PatternGridRenderer {
    default void addWidgets(final Consumer<AbstractWidget> widgets,
                            final Consumer<AbstractWidget> renderables) {
        // no op
    }

    default void tick() {
        // no op
    }

    default void render(final GuiGraphicsExtractor graphics,
                        final int mouseX,
                        final int mouseY,
                        final float partialTicks) {
        // no op
    }

    int getClearButtonX();

    int getClearButtonY();

    void renderBackground(GuiGraphicsExtractor graphics,
                          float partialTicks,
                          int mouseX,
                          int mouseY);

    default void renderTooltip(final Font font,
                               @Nullable final Slot hoveredSlot,
                               final GuiGraphicsExtractor graphics,
                               final int mouseX,
                               final int mouseY) {
        // no op
    }

    default void extractLabels(final GuiGraphicsExtractor graphics,
                               final Font font,
                               final int mouseX,
                               final int mouseY) {
        // no op
    }

    default boolean mouseClicked(final MouseButtonEvent e, final boolean doubleClick) {
        return false;
    }

    default void mouseMoved(final double mouseX, final double mouseY) {
        // no op
    }

    default boolean mouseReleased(final MouseButtonEvent e) {
        return false;
    }

    default boolean mouseScrolled(final double mouseX, final double mouseY, final double mouseZ, final double delta) {
        return false;
    }

    default void patternTypeChanged(final PatternType newPatternType) {
        // no op
    }

    default void fuzzyModeChanged(final boolean newFuzzyMode) {
        // no op
    }

    default boolean canInteractWithResourceSlot(final ResourceSlot resourceSlot,
                                                final double mouseX,
                                                final double mouseY) {
        return true;
    }
}
