package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.api.resource.repository.SortingDirection;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class SortingDirectionSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.sorting.direction");
    private static final List<MutableComponent> SUBTEXT_ASCENDING = List.of(
        createTranslation("gui", "grid.sorting.direction.ascending").withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> SUBTEXT_DESCENDING = List.of(
        createTranslation("gui", "grid.sorting.direction.descending").withStyle(ChatFormatting.GRAY));
    private static final Identifier ASCENDING =
        createIdentifier("widget/side_button/grid/sorting_direction/ascending");
    private static final Identifier DESCENDING =
        createIdentifier("widget/side_button/grid/sorting_direction/descending");

    private final AbstractGridContainerMenu menu;

    SortingDirectionSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setSortingDirection(toggle(menu.getSortingDirection()));
    }

    private static SortingDirection toggle(final SortingDirection sortingDirection) {
        return sortingDirection == SortingDirection.ASCENDING
            ? SortingDirection.DESCENDING
            : SortingDirection.ASCENDING;
    }

    @Override
    protected Identifier getSprite() {
        return menu.getSortingDirection() == SortingDirection.ASCENDING ? ASCENDING : DESCENDING;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (menu.getSortingDirection()) {
            case ASCENDING -> SUBTEXT_ASCENDING;
            case DESCENDING -> SUBTEXT_DESCENDING;
        };
    }
}
