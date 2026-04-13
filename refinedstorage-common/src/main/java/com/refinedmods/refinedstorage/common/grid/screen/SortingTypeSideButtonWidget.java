package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.GridSortingTypes;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class SortingTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.sorting.type");
    private static final List<MutableComponent> SUBTEXT_QUANTITY = List.of(
        createTranslation("gui", "grid.sorting.type.quantity").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_NAME = List.of(
        createTranslation("gui", "grid.sorting.type.name").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_ID = List.of(
        createTranslation("gui", "grid.sorting.type.id").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_LAST_MODIFIED = List.of(
        createTranslation("gui", "grid.sorting.type.last_modified").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier QUANTITY = createIdentifier("widget/side_button/grid/sorting_type/quantity");
    private static final Identifier NAME = createIdentifier("widget/side_button/grid/sorting_type/name");
    private static final Identifier ID = createIdentifier("widget/side_button/grid/sorting_type/id");
    private static final Identifier LAST_MODIFIED =
        createIdentifier("widget/side_button/grid/sorting_type/last_modified");

    private final AbstractGridContainerMenu menu;

    SortingTypeSideButtonWidget(final AbstractGridContainerMenu menu) {
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
    protected Identifier getSprite() {
        return switch (menu.getSortingType()) {
            case QUANTITY -> QUANTITY;
            case NAME -> NAME;
            case ID -> ID;
            case LAST_MODIFIED -> LAST_MODIFIED;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (menu.getSortingType()) {
            case QUANTITY -> SUBTEXT_QUANTITY;
            case NAME -> SUBTEXT_NAME;
            case ID -> SUBTEXT_ID;
            case LAST_MODIFIED -> SUBTEXT_LAST_MODIFIED;
        };
    }
}
