package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ConstructorDropItemsSideButtonWidget extends AbstractYesNoSideButtonWidget {
    public ConstructorDropItemsSideButtonWidget(final ClientProperty<Boolean> property) {
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
