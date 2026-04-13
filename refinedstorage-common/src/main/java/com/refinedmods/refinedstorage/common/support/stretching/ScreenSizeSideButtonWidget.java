package com.refinedmods.refinedstorage.common.support.stretching;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ScreenSizeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "screen_size");
    private static final List<MutableComponent> SUBTEXT_STRETCH = List.of(
        createTranslation("gui", "screen_size.stretch").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_SMALL = List.of(
        createTranslation("gui", "screen_size.small").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_MEDIUM = List.of(
        createTranslation("gui", "screen_size.medium").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_LARGE = List.of(
        createTranslation("gui", "screen_size.large").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_EXTRA_LARGE = List.of(
        createTranslation("gui", "screen_size.extra_large").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier STRETCH = createIdentifier("widget/side_button/screen_size/stretch");
    private static final Identifier SMALL = createIdentifier("widget/side_button/screen_size/small");
    private static final Identifier MEDIUM = createIdentifier("widget/side_button/screen_size/medium");
    private static final Identifier EXTRA_LARGE = createIdentifier("widget/side_button/screen_size/extra_large");

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
    protected Identifier getSprite() {
        final ScreenSize screenSize = Platform.INSTANCE.getConfig().getScreenSize();
        return switch (screenSize) {
            case STRETCH -> STRETCH;
            case SMALL -> SMALL;
            case MEDIUM -> MEDIUM;
            case LARGE, EXTRA_LARGE -> EXTRA_LARGE;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
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
