package com.refinedmods.refinedstorage.platform.common.support.stretching;

import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class ScreenSizeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "screen_size");

    private static final MutableComponent SUBTEXT_STRETCH = createTranslation("gui", "screen_size.stretch");
    private static final MutableComponent SUBTEXT_SMALL = createTranslation("gui", "screen_size.small");
    private static final MutableComponent SUBTEXT_MEDIUM = createTranslation("gui", "screen_size.medium");
    private static final MutableComponent SUBTEXT_LARGE = createTranslation("gui", "screen_size.large");
    private static final MutableComponent SUBTEXT_EXTRA_LARGE = createTranslation("gui", "screen_size.extra_large");

    public ScreenSizeSideButtonWidget(final AbstractStretchingScreen<?> stretchingScreen) {
        super(createPressAction(stretchingScreen));
    }

    private static OnPress createPressAction(final AbstractStretchingScreen<?> stretchingScreen) {
        return btn -> {
            Platform.INSTANCE.getConfig().setScreenSize(Platform.INSTANCE.getConfig().getScreenSize().toggle());
            stretchingScreen.init();
        };
    }

    @Override
    protected int getXTexture() {
        final ScreenSize screenSize = Platform.INSTANCE.getConfig().getScreenSize();
        return switch (screenSize) {
            case STRETCH -> 64 + 48;
            case SMALL -> 64;
            case MEDIUM -> 64 + 16;
            case LARGE, EXTRA_LARGE -> 64 + 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 64;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        final ScreenSize screenSize = Platform.INSTANCE.getConfig().getScreenSize();
        return switch (screenSize) {
            case STRETCH -> SUBTEXT_STRETCH;
            case SMALL -> SUBTEXT_SMALL;
            case MEDIUM -> SUBTEXT_MEDIUM;
            case LARGE -> SUBTEXT_LARGE;
            case EXTRA_LARGE -> SUBTEXT_EXTRA_LARGE;
        };
    }
}
