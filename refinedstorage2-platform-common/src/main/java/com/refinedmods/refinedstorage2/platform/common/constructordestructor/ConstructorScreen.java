package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.support.widget.SchedulingModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ConstructorScreen extends AbstractFilterScreen<ConstructorContainerMenu> {
    public ConstructorScreen(final ConstructorContainerMenu menu,
                             final Inventory playerInventory,
                             final Component text) {
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
        addSideButton(new ConstructorDropItemsSideButtonWidget(
            getMenu().getProperty(PropertyTypes.CONSTRUCTOR_DROP_ITEMS)
        ));
    }
}
