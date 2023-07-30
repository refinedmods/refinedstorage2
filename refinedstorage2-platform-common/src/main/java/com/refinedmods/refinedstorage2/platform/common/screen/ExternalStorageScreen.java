package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.PrioritySideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ExternalStorageScreen extends AbstractFilterScreen<ExternalStorageContainerMenu> {
    private final Inventory playerInventory;

    public ExternalStorageScreen(final ExternalStorageContainerMenu menu,
                                 final Inventory inventory,
                                 final Component title) {
        super(menu, inventory, title);
        this.playerInventory = inventory;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FilterModeSideButtonWidget(getMenu().getProperty(PropertyTypes.FILTER_MODE)));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            FuzzyModeSideButtonWidget.Type.STORAGE
        ));
        addSideButton(new AccessModeSideButtonWidget(getMenu().getProperty(PropertyTypes.ACCESS_MODE)));
        addSideButton(new PrioritySideButtonWidget(
            getMenu().getProperty(PropertyTypes.PRIORITY),
            playerInventory,
            this
        ));
    }
}
