package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class NetworkMonitorGroupTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "network_monitor.group_type");
    private static final List<MutableComponent> SUBTEXT_DEVICE_TYPE = List.of(
        createTranslation("gui", "network_monitor.group_type.device_type").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_DEVICE_CATEGORY = List.of(
        createTranslation("gui", "network_monitor.group_type.device_category").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_NONE = List.of(
        createTranslation("gui", "network_monitor.group_type.none").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier DEVICE_TYPE =
        createIdentifier("widget/side_button/network_monitor/group_type/device_type");
    private static final Identifier DEVICE_CATEGORY =
        createIdentifier("widget/side_button/network_monitor/group_type/device_category");
    private static final Identifier NONE =
        createIdentifier("widget/side_button/network_monitor/group_type/none");

    private final NetworkMonitorContainerMenu menu;

    NetworkMonitorGroupTypeSideButtonWidget(final NetworkMonitorContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final NetworkMonitorContainerMenu menu) {
        return btn -> menu.setGroupType(toggle(menu.getGroupType()));
    }

    private static NetworkMonitorGroupType toggle(final NetworkMonitorGroupType groupType) {
        return switch (groupType) {
            case DEVICE_TYPE -> NetworkMonitorGroupType.DEVICE_CATEGORY;
            case DEVICE_CATEGORY -> NetworkMonitorGroupType.NONE;
            case NONE -> NetworkMonitorGroupType.DEVICE_TYPE;
        };
    }

    @Override
    protected Identifier getSprite() {
        return switch (menu.getGroupType()) {
            case DEVICE_TYPE -> DEVICE_TYPE;
            case DEVICE_CATEGORY -> DEVICE_CATEGORY;
            case NONE -> NONE;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (menu.getGroupType()) {
            case DEVICE_TYPE -> SUBTEXT_DEVICE_TYPE;
            case DEVICE_CATEGORY -> SUBTEXT_DEVICE_CATEGORY;
            case NONE -> SUBTEXT_NONE;
        };
    }
}
