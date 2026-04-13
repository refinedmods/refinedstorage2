package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.common.storage.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.AbstractFilterScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class DestructorScreen extends AbstractFilterScreen<DestructorContainerMenu> {
    public DestructorScreen(final DestructorContainerMenu menu,
                            final Inventory playerInventory,
                            final Component title) {
        super(menu, playerInventory, title, true);
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            createTranslation("gui", "destructor.filter_mode.allow.help"),
            createTranslation("gui", "destructor.filter_mode.block.help")
        ));
        addSideButton(new DestructorPickupItemsSideButtonWidget(
            getMenu().getProperty(ConstructorDestructorPropertyTypes.PICKUP_ITEMS)
        ));
    }
}
