package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FuzzyModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "fuzzy_mode");
    private static final MutableComponent SUBTEXT_ON = createTranslation("gui", "fuzzy_mode.on");
    private static final MutableComponent SUBTEXT_OFF = createTranslation("gui", "fuzzy_mode.off");

    private final ClientProperty<Boolean> property;

    public FuzzyModeSideButtonWidget(final ClientProperty<Boolean> property) {
        super(createPressAction(property));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<Boolean> property) {
        return btn -> property.setValue(!property.getValue());
    }

    @Override
    protected int getXTexture() {
        return Boolean.TRUE.equals(property.getValue()) ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 192;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return Boolean.TRUE.equals(property.getValue()) ? SUBTEXT_ON : SUBTEXT_OFF;
    }
}
