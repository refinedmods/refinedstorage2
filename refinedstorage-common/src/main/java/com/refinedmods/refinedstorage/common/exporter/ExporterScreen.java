package com.refinedmods.refinedstorage.common.exporter;

import com.refinedmods.refinedstorage.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.SchedulingModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExporterScreen extends AbstractFilterScreen<ExporterContainerMenu> {
    public ExporterScreen(final ExporterContainerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title, true);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            () -> FuzzyModeSideButtonWidget.Type.EXTRACTING_STORAGE_NETWORK
        ));
        addSideButton(new SchedulingModeSideButtonWidget(getMenu().getProperty(PropertyTypes.SCHEDULING_MODE)));
    }

    @Override
    protected void extractTooltip(final GuiGraphicsExtractor graphics, final int x, final int y) {
        if (renderExportingIndicators(font, graphics, leftPos, topPos, x, y, getMenu().getIndicators(),
            getMenu()::getIndicator)) {
            return;
        }
        super.extractTooltip(graphics, x, y);
    }
}
