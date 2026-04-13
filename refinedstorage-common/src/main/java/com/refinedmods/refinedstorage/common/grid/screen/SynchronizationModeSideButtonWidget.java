package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class SynchronizationModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.synchronization_mode");

    private final AbstractGridContainerMenu menu;

    SynchronizationModeSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.toggleSynchronizer();
    }

    @Override
    protected Identifier getSprite() {
        return menu.getSynchronizer().getSprite();
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return List.of(menu.getSynchronizer().getTitle().withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected Component getHelpText() {
        return menu.getSynchronizer().getHelpText();
    }
}
