package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractYesNoSideButtonWidget;

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
