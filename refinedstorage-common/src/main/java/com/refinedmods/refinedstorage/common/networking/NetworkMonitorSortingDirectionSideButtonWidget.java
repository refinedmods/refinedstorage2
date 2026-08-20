package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class NetworkMonitorSortingDirectionSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "network_monitor.sorting_direction");
    private static final List<MutableComponent> SUBTEXT_ASCENDING = List.of(
        createTranslation("gui", "network_monitor.sorting_direction.ascending").withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> SUBTEXT_DESCENDING = List.of(
        createTranslation("gui", "network_monitor.sorting_direction.descending").withStyle(ChatFormatting.GRAY));
    private static final Identifier ASCENDING =
        createIdentifier("widget/side_button/network_monitor/sorting_direction/ascending");
    private static final Identifier DESCENDING =
        createIdentifier("widget/side_button/network_monitor/sorting_direction/descending");

    private final NetworkMonitorContainerMenu menu;

    NetworkMonitorSortingDirectionSideButtonWidget(final NetworkMonitorContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final NetworkMonitorContainerMenu menu) {
        return btn -> menu.setSortingDirection(toggle(menu.getSortingDirection()));
    }

    private static NetworkMonitorSortingDirection toggle(final NetworkMonitorSortingDirection sortingDirection) {
        return sortingDirection == NetworkMonitorSortingDirection.ASCENDING
            ? NetworkMonitorSortingDirection.DESCENDING
            : NetworkMonitorSortingDirection.ASCENDING;
    }

    @Override
    protected Identifier getSprite() {
        return menu.getSortingDirection() == NetworkMonitorSortingDirection.ASCENDING ? ASCENDING : DESCENDING;
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
