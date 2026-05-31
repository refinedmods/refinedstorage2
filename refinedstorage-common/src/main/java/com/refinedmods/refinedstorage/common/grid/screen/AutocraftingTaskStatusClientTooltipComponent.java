package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.api.autocrafting.status.TaskStatus;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

class AutocraftingTaskStatusClientTooltipComponent implements ClientTooltipComponent {
    private final List<TaskStatus> statuses;

    AutocraftingTaskStatusClientTooltipComponent(final List<TaskStatus> statuses) {
        this.statuses = statuses;
    }

    @Override
    public void extractText(final GuiGraphicsExtractor graphics, final Font font, final int x, final int y) {
        int yy = y;
        for (final TaskStatus status : statuses) {
            graphics.text(font, status.info().id().toString(), x, yy, 0xFFFFFFFF);
            yy += font.lineHeight;
        }
    }

    @Override
    public int getHeight(final Font font) {
        return 100;
    }

    @Override
    public int getWidth(final Font font) {
        return 100;
    }
}
