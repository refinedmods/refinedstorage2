package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ImporterScreen extends AbstractFilterScreen<ImporterContainerMenu> {
    public ImporterScreen(final ImporterContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FilterModeSideButtonWidget(getMenu().getProperty(PropertyTypes.FILTER_MODE)));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            FuzzyModeSideButtonWidget.Type.EXTRACTING_SOURCE
        ));
    }
}
