package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DestructorPickupItemsSideButtonWidget extends AbstractYesNoSideButtonWidget {
    public DestructorPickupItemsSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, createTranslation("gui", "destructor.pickup_items"));
    }

    @Override
    protected int getXTexture() {
        return Boolean.TRUE.equals(property.getValue()) ? 64 : 80;
    }

    @Override
    protected int getYTexture() {
        return 0;
    }
}
