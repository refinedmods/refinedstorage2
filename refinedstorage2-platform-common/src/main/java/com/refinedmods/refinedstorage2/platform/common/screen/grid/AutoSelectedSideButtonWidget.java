package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.NO;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.YES;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class AutoSelectedSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.auto_selected");

    private final AbstractGridContainerMenu menu;

    public AutoSelectedSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setAutoSelected(!menu.isAutoSelected());
    }

    @Override
    protected int getXTexture() {
        return menu.isAutoSelected() ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 96;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return menu.isAutoSelected() ? YES : NO;
    }
}
