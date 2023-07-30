package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSortingTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SortingTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.sorting.type");
    private static final MutableComponent SUBTEXT_QUANTITY = createTranslation("gui", "grid.sorting.type.quantity");
    private static final MutableComponent SUBTEXT_NAME = createTranslation("gui", "grid.sorting.type.name");
    private static final MutableComponent SUBTEXT_ID = createTranslation("gui", "grid.sorting.type.id");
    private static final MutableComponent SUBTEXT_LAST_MODIFIED =
        createTranslation("gui", "grid.sorting.type.last_modified");

    private final AbstractGridContainerMenu menu;

    public SortingTypeSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setSortingType(toggle(menu.getSortingType()));
    }

    private static GridSortingTypes toggle(final GridSortingTypes sortingType) {
        return switch (sortingType) {
            case QUANTITY -> GridSortingTypes.NAME;
            case NAME -> GridSortingTypes.ID;
            case ID -> GridSortingTypes.LAST_MODIFIED;
            case LAST_MODIFIED -> GridSortingTypes.QUANTITY;
        };
    }

    @Override
    protected int getXTexture() {
        return switch (menu.getSortingType()) {
            case QUANTITY -> 0;
            case NAME -> 16;
            case ID -> 32;
            case LAST_MODIFIED -> 48;
        };
    }

    @Override
    protected int getYTexture() {
        return menu.getSortingType() == GridSortingTypes.LAST_MODIFIED ? 48 : 32;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return switch (menu.getSortingType()) {
            case QUANTITY -> SUBTEXT_QUANTITY;
            case NAME -> SUBTEXT_NAME;
            case ID -> SUBTEXT_ID;
            case LAST_MODIFIED -> SUBTEXT_LAST_MODIFIED;
        };
    }
}
