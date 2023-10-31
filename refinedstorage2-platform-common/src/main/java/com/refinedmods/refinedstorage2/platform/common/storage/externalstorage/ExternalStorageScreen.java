package com.refinedmods.refinedstorage2.platform.common.storage.externalstorage;

import com.refinedmods.refinedstorage2.platform.common.storage.AbstractStorageScreen;
import com.refinedmods.refinedstorage2.platform.common.storage.AccessModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.storage.PrioritySideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FuzzyModeSideButtonWidget;

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
        addSideButton(new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            AbstractStorageScreen.ALLOW_FILTER_MODE_HELP,
            AbstractStorageScreen.BLOCK_FILTER_MODE_HELP
        ));
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
