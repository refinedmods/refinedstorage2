package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class NetworkMonitorSortingTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "network_monitor.sorting_type");
    private static final List<MutableComponent> SUBTEXT_NAME = List.of(
        createTranslation("gui", "network_monitor.sorting_type.name").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_ENERGY_USAGE = List.of(
        createTranslation("gui", "network_monitor.sorting_type.energy_usage").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_INSERT_PRIORITY = List.of(
        createTranslation("gui", "network_monitor.sorting_type.insert_priority").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_EXTRACT_PRIORITY = List.of(
        createTranslation("gui", "network_monitor.sorting_type.extract_priority").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier NAME = createIdentifier("widget/side_button/network_monitor/sorting_type/name");
    private static final Identifier ENERGY_USAGE =
        createIdentifier("widget/side_button/network_monitor/sorting_type/energy_usage");
    private static final Identifier INSERT_PRIORITY =
        createIdentifier("widget/side_button/network_monitor/sorting_type/insert_priority");
    private static final Identifier EXTRACT_PRIORITY =
        createIdentifier("widget/side_button/network_monitor/sorting_type/extract_priority");


    private final NetworkMonitorContainerMenu menu;

    NetworkMonitorSortingTypeSideButtonWidget(final NetworkMonitorContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final NetworkMonitorContainerMenu menu) {
        return btn -> menu.setSortingType(toggle(menu.getSortingType()));
    }

    private static NetworkMonitorSortingType toggle(final NetworkMonitorSortingType sortingType) {
        return switch (sortingType) {
            case NAME -> NetworkMonitorSortingType.ENERGY_USAGE;
            case ENERGY_USAGE -> NetworkMonitorSortingType.INSERT_PRIORITY;
            case INSERT_PRIORITY -> NetworkMonitorSortingType.EXTRACT_PRIORITY;
            case EXTRACT_PRIORITY -> NetworkMonitorSortingType.NAME;
        };
    }

    @Override
    protected Identifier getSprite() {
        return switch (menu.getSortingType()) {
            case NAME -> NAME;
            case ENERGY_USAGE -> ENERGY_USAGE;
            case INSERT_PRIORITY -> INSERT_PRIORITY;
            case EXTRACT_PRIORITY -> EXTRACT_PRIORITY;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (menu.getSortingType()) {
            case NAME -> SUBTEXT_NAME;
            case ENERGY_USAGE -> SUBTEXT_ENERGY_USAGE;
            case INSERT_PRIORITY -> SUBTEXT_INSERT_PRIORITY;
            case EXTRACT_PRIORITY -> SUBTEXT_EXTRACT_PRIORITY;
        };
    }
}
