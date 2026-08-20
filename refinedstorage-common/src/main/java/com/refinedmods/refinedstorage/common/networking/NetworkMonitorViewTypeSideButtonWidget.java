package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class NetworkMonitorViewTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "network_monitor.view_type");
    private static final List<MutableComponent> SUBTEXT_ALL = List.of(
        createTranslation("gui", "network_monitor.view_type.all").withStyle(ChatFormatting.GRAY)
    );
    private static final EnumMap<NetworkMonitorDeviceCategory, List<MutableComponent>> SUBTEXT_CATEGORIES =
        new EnumMap<>(NetworkMonitorDeviceCategory.class);
    private static final MutableComponent EMPTY_WARNING = createTranslation("gui",
        "network_monitor.view_type.there_are_no_devices_of_this_category_in_the_storage_network");

    static {
        for (final NetworkMonitorDeviceCategory category : NetworkMonitorDeviceCategory.values()) {
            SUBTEXT_CATEGORIES.put(category, List.of(
                NetworkMonitorDevices.createDeviceCategoryTranslation(category).withStyle(ChatFormatting.GRAY)
            ));
        }
    }

    private static final Identifier ALL =
        createIdentifier("widget/side_button/network_monitor/view_type/all");
    private static final Identifier CATEGORY =
        createIdentifier("widget/side_button/network_monitor/view_type/category");

    private final NetworkMonitorContainerMenu menu;

    NetworkMonitorViewTypeSideButtonWidget(final NetworkMonitorContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final NetworkMonitorContainerMenu menu) {
        return btn -> menu.setViewType(toggle(menu.getViewType()));
    }

    @Nullable
    private static NetworkMonitorDeviceCategory toggle(@Nullable final NetworkMonitorDeviceCategory deviceCategory) {
        return switch (deviceCategory) {
            case null -> NetworkMonitorDeviceCategory.NETWORKING;
            case NETWORKING -> NetworkMonitorDeviceCategory.STORAGE;
            case STORAGE -> NetworkMonitorDeviceCategory.MONITORING;
            case MONITORING -> NetworkMonitorDeviceCategory.INPUT_AND_OUTPUT;
            case INPUT_AND_OUTPUT -> NetworkMonitorDeviceCategory.WIRELESS;
            case WIRELESS -> NetworkMonitorDeviceCategory.AUTOCRAFTING;
            case AUTOCRAFTING -> NetworkMonitorDeviceCategory.SECURITY;
            case SECURITY -> NetworkMonitorDeviceCategory.OTHER;
            case OTHER -> null;
        };
    }

    @Override
    protected Identifier getSprite() {
        if (menu.getViewType() == null) {
            return ALL;
        }
        return CATEGORY;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        final NetworkMonitorDeviceCategory viewType = menu.getViewType();
        if (viewType == null) {
            return SUBTEXT_ALL;
        }
        return SUBTEXT_CATEGORIES.get(viewType);
    }

    void setEmptyWarningVisible(final boolean visible) {
        if (visible) {
            setWarning(EMPTY_WARNING);
        } else {
            setWarning(null);
        }
    }
}
