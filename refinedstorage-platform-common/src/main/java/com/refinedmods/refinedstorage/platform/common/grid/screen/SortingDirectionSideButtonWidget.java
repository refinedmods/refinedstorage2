package com.refinedmods.refinedstorage.platform.common.grid.screen;

import com.refinedmods.refinedstorage.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

class SortingDirectionSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.sorting.direction");
    private static final MutableComponent SUBTEXT_ASCENDING =
        createTranslation("gui", "grid.sorting.direction.ascending");
    private static final MutableComponent SUBTEXT_DESCENDING =
        createTranslation("gui", "grid.sorting.direction.descending");

    private final AbstractGridContainerMenu menu;

    SortingDirectionSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setSortingDirection(toggle(menu.getSortingDirection()));
    }

    private static GridSortingDirection toggle(final GridSortingDirection sortingDirection) {
        return sortingDirection == GridSortingDirection.ASCENDING
            ? GridSortingDirection.DESCENDING
            : GridSortingDirection.ASCENDING;
    }

    @Override
    protected int getXTexture() {
        return menu.getSortingDirection() == GridSortingDirection.ASCENDING ? 0 : 16;
    }

    @Override
    protected int getYTexture() {
        return 16;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return switch (menu.getSortingDirection()) {
            case ASCENDING -> SUBTEXT_ASCENDING;
            case DESCENDING -> SUBTEXT_DESCENDING;
        };
    }
}
