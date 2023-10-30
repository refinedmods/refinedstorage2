package com.refinedmods.refinedstorage2.platform.common.screen.amount;

import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.DetectorModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DetectorScreen extends AbstractSingleAmountScreen<DetectorContainerMenu> {
    public DetectorScreen(final DetectorContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text, menu.getAmount(), 0);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            FuzzyModeSideButtonWidget.Type.GENERIC
        ));
        addSideButton(new DetectorModeSideButtonWidget(getMenu().getProperty(PropertyTypes.DETECTOR_MODE)));
    }
}
