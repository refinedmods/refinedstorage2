package com.refinedmods.refinedstorage2.platform.common.exporter;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.SchedulingModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExporterScreen extends AbstractFilterScreen<ExporterContainerMenu> {
    public ExporterScreen(final ExporterContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            FuzzyModeSideButtonWidget.Type.EXTRACTING_STORAGE_NETWORK
        ));
        addSideButton(new SchedulingModeSideButtonWidget(getMenu().getProperty(PropertyTypes.SCHEDULING_MODE)));
    }
}
