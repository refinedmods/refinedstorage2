package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.DestructorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.DestructorPickupItemsSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DestructorScreen extends AbstractFilterScreen<DestructorContainerMenu> {
    public DestructorScreen(final DestructorContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FilterModeSideButtonWidget(getMenu().getProperty(PropertyTypes.FILTER_MODE)));
        addSideButton(new DestructorPickupItemsSideButtonWidget(
            getMenu().getProperty(PropertyTypes.DESTRUCTOR_PICKUP_ITEMS)
        ));
    }
}
