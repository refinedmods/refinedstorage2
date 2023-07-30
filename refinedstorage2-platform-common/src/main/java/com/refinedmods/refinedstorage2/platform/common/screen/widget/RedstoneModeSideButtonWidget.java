package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class RedstoneModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "redstone_mode");
    private static final MutableComponent SUBTEXT_IGNORE = createTranslation("gui", "redstone_mode.ignore");
    private static final MutableComponent SUBTEXT_HIGH = createTranslation("gui", "redstone_mode.high");
    private static final MutableComponent SUBTEXT_LOW = createTranslation("gui", "redstone_mode.low");

    private final ClientProperty<RedstoneMode> property;

    public RedstoneModeSideButtonWidget(final ClientProperty<RedstoneMode> property) {
        super(createPressAction(property));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<RedstoneMode> property) {
        return btn -> property.setValue(property.getValue().toggle());
    }

    @Override
    protected int getXTexture() {
        return switch (property.getValue()) {
            case IGNORE -> 0;
            case HIGH -> 16;
            case LOW -> 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 0;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return switch (property.getValue()) {
            case IGNORE -> SUBTEXT_IGNORE;
            case HIGH -> SUBTEXT_HIGH;
            case LOW -> SUBTEXT_LOW;
        };
    }
}
