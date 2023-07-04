package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SortingDirectionSideButtonWidget extends AbstractSideButtonWidget {
    private final AbstractGridContainerMenu menu;
    private final Map<GridSortingDirection, List<Component>> tooltips = new EnumMap<>(GridSortingDirection.class);

    public SortingDirectionSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
        Arrays.stream(GridSortingDirection.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setSortingDirection(toggle(menu.getSortingDirection()));
    }

    private static GridSortingDirection toggle(final GridSortingDirection sortingDirection) {
        return sortingDirection == GridSortingDirection.ASCENDING
            ? GridSortingDirection.DESCENDING
            : GridSortingDirection.ASCENDING;
    }

    private List<Component> calculateTooltip(final GridSortingDirection type) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.sorting.direction"));
        lines.add(createTranslation(
            "gui",
            "grid.sorting.direction." + type.toString().toLowerCase(Locale.ROOT)
        ).withStyle(ChatFormatting.GRAY));
        return lines;
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
    protected List<Component> getSideButtonTooltip() {
        return tooltips.get(menu.getSortingDirection());
    }
}
