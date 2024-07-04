package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.platform.common.support.widget.AbstractYesNoSideButtonWidget;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

class ConstructorDropItemsSideButtonWidget extends AbstractYesNoSideButtonWidget {
    ConstructorDropItemsSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, createTranslation("gui", "constructor.drop_items"));
    }

    @Override
    protected int getXTexture() {
        return Boolean.TRUE.equals(property.getValue()) ? 80 : 64;
    }

    @Override
    protected int getYTexture() {
        return 16;
    }
}
