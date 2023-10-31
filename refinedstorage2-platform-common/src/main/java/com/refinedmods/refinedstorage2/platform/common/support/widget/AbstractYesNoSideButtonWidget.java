package com.refinedmods.refinedstorage2.platform.common.support.widget;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.NO;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.YES;

public abstract class AbstractYesNoSideButtonWidget extends AbstractSideButtonWidget {
    protected final ClientProperty<Boolean> property;
    private final MutableComponent title;

    protected AbstractYesNoSideButtonWidget(final ClientProperty<Boolean> property, final MutableComponent title) {
        super(createPressAction(property));
        this.property = property;
        this.title = title;
    }

    private static OnPress createPressAction(final ClientProperty<Boolean> property) {
        return btn -> property.setValue(!property.getValue());
    }

    @Override
    protected MutableComponent getTitle() {
        return title;
    }

    @Override
    protected MutableComponent getSubText() {
        return Boolean.TRUE.equals(property.getValue()) ? YES : NO;
    }
}
