package com.refinedmods.refinedstorage.common.support.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class SmallTextClientTooltipComponent implements ClientTooltipComponent {
    private final Component text;

    public SmallTextClientTooltipComponent(final Component text) {
        this.text = text;
    }

    @Override
    public void extractText(final GuiGraphicsExtractor graphics, final Font font, final int x, final int y) {
        SmallText.render(graphics, font, text.getVisualOrderText(), x, y, -1, true, SmallText.TOOLTIP_SCALE);
    }

    @Override
    public int getHeight(final Font font) {
        return 9;
    }

    @Override
    public int getWidth(final Font font) {
        return (int) (font.width(text) * SmallText.correctScale(SmallText.TOOLTIP_SCALE));
    }
}
