package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.controller.ControllerBlockEntity.NodeEnergyEntry;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class EnergyUsageListWidget extends AbstractWidget {
    private static final int ROW_HEIGHT = 18;
    private static final int ICON_SIZE = 16;
    public static final int SCROLLBAR_WIDTH = 12;
    private static final int PADDING = 2;
    private static final int ROW_HOVER_COLOR = 0x33000000;
    private static final int TEXT_COLOR = 0xFF404040;

    // Scrollbar is owned and rendered by the screen — we only read its offset.
    private final ScrollbarWidget scrollbar;
    private List<NodeEnergyEntry> entries = List.of();

    public EnergyUsageListWidget(final int x, final int y, final int w, final int h,
                                 final ScrollbarWidget scrollbar) {
        super(x, y, w, h, Component.empty());
        this.scrollbar = scrollbar;
    }

    public void setEntries(final List<NodeEnergyEntry> entries) {
        this.entries = entries;
        updateScrollbar();
    }

    private void updateScrollbar() {
        final int visibleRows = height / ROW_HEIGHT;
        final int overflowRows = Math.max(0, entries.size() - visibleRows);
        final int maxOffset = scrollbar.isSmoothScrolling()
                ? overflowRows * ROW_HEIGHT
                : overflowRows;
        scrollbar.setMaxOffset(maxOffset);
        scrollbar.setEnabled(maxOffset > 0);
    }

    @Override
    public void playDownSound(final SoundManager handler) {
    }

    @Override
    protected void extractWidgetRenderState(final GuiGraphicsExtractor graphics,
                                            final int mouseX,
                                            final int mouseY,
                                            final float partialTicks) {
        final Font font = Minecraft.getInstance().font;
        final int x = getX();
        final int y = getY();

        // The background, border, and scrollbar track groove are all baked into
        // controller.png — no fill() calls needed here. We only draw rows on top.
        final int clipRight = x + width - SCROLLBAR_WIDTH - 1;
        graphics.enableScissor(x + 1, y + 1, clipRight, y + height - 1);

        final int scrollOffset = scrollbar.isSmoothScrolling()
                ? (int) scrollbar.getOffset()
                : (int) scrollbar.getOffset() * ROW_HEIGHT;

        for (int i = 0; i < entries.size(); i++) {
            final int rowY = y + i * ROW_HEIGHT - scrollOffset;
            if (rowY + ROW_HEIGHT <= y || rowY >= y + height) {
                continue;
            }

            if (mouseX >= x + 1 && mouseX < clipRight && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                graphics.fill(x + 1, rowY, clipRight, rowY + ROW_HEIGHT, ROW_HOVER_COLOR);
            }

            final NodeEnergyEntry entry = entries.get(i);

            if (!entry.icon().isEmpty()) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + PADDING, rowY + (float) (ROW_HEIGHT - ICON_SIZE) / 2);
                graphics.item(entry.icon(), 0, 0);
                graphics.pose().popMatrix();
            }

            final int textX = x + PADDING + ICON_SIZE + PADDING;
            final int textY = rowY + (ROW_HEIGHT - font.lineHeight) / 2;
            final String countPrefix = entry.count() + "x ";
            final String usage = entry.usage() + " FE/t";
            final int usageWidth = font.width(usage);
            final int countWidth = font.width(countPrefix);
            final int nameMaxWidth = clipRight - PADDING - textX - countWidth - usageWidth - PADDING;

            String name = entry.translatedName();
            final boolean truncated = font.width(name) > nameMaxWidth;
            if (truncated) {
                while (font.width(name) > nameMaxWidth && name.length() > 2) {
                    name = name.substring(0, name.length() - 1);
                }
                name = name + "..";
            }

            graphics.text(font, countPrefix + name, textX, textY, TEXT_COLOR, false);
            graphics.text(font, usage, clipRight - usageWidth - PADDING, textY, TEXT_COLOR, false);

            if (truncated
                    && mouseX >= x + 1 && mouseX < clipRight
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                graphics.setComponentTooltipForNextFrame(
                        font,
                        List.of(Component.literal(entry.count() + "x " + entry.translatedName() + ": " + entry.usage() + " FE/t")),
                        mouseX,
                        mouseY
                );
            }
        }

        graphics.disableScissor();
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output) {
    }
}