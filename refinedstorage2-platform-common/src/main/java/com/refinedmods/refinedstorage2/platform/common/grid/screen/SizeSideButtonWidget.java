package com.refinedmods.refinedstorage2.platform.common.grid.screen;

import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SizeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.size");

    private static final MutableComponent SUBTEXT_STRETCH = createTranslation("gui", "grid.size.stretch");
    private static final MutableComponent SUBTEXT_SMALL = createTranslation("gui", "grid.size.small");
    private static final MutableComponent SUBTEXT_MEDIUM = createTranslation("gui", "grid.size.medium");
    private static final MutableComponent SUBTEXT_LARGE = createTranslation("gui", "grid.size.large");
    private static final MutableComponent SUBTEXT_EXTRA_LARGE = createTranslation("gui", "grid.size.extra_large");

    private final AbstractGridContainerMenu menu;

    public SizeSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setSize(menu.getSize().toggle());
    }

    @Override
    protected int getXTexture() {
        return switch (menu.getSize()) {
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
        return switch (menu.getSize()) {
            case STRETCH -> SUBTEXT_STRETCH;
            case SMALL -> SUBTEXT_SMALL;
            case MEDIUM -> SUBTEXT_MEDIUM;
            case LARGE -> SUBTEXT_LARGE;
            case EXTRA_LARGE -> SUBTEXT_EXTRA_LARGE;
        };
    }
}
