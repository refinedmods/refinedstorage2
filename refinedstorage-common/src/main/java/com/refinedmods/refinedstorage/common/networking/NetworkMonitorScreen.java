package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;
import com.refinedmods.refinedstorage.common.api.networking.NetworkNodeDetailsRenderer;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.AutoSelectedSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.History;
import com.refinedmods.refinedstorage.common.support.widget.ProgressBarWidget;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.ScrollbarWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchFieldWidget;
import com.refinedmods.refinedstorage.common.support.widget.SearchIconWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createStoredWithCapacityTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class NetworkMonitorScreen extends AbstractStretchingScreen<NetworkMonitorContainerMenu>
    implements NetworkMonitorListener {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/network_monitor.png");
    private static final Identifier DEVICES = createIdentifier("network_monitor/devices");
    private static final Component SEARCH_HELP = createTranslation("gui", "network_monitor.search_help");
    private static final Component ENERGY = createTranslation("gui",
        "network_monitor.network.energy");
    private static final Component STORAGE_DISKS_AND_BLOCKS = createTranslation("gui",
        "network_monitor.network.storage_disks_and_blocks");

    private static final int DEVICES_TOP_HEIGHT = 19;
    private static final int DEVICES_BOTTOM_HEIGHT = 7;
    private static final int DEVICES_SPRITE_WIDTH = 91;
    private static final int DEVICES_SPRITE_HEIGHT = 46;
    private static final List<String> SEARCH_FIELD_HISTORY = new ArrayList<>();
    private static final int NETWORK_STATISTICS_PADDING = 4;

    @Nullable
    private ScrollbarWidget devicesScrollbar;
    @Nullable
    private SearchFieldWidget searchField;
    private List<ClientTooltipComponent> detailsTooltip = Collections.emptyList();
    @Nullable
    private NetworkMonitorNetworkWidget networkWidget;
    @Nullable
    private NetworkMonitorViewTypeSideButtonWidget viewTypeSideButtonWidget;
    private boolean showNetworkStatistics;

    private final List<NetworkMonitorDeviceGroupWidget> deviceGroupWidgets = new ArrayList<>();
    private final List<NetworkMonitorDeviceCategoryWidget> deviceCategoryWidgets = new ArrayList<>();
    private final List<NetworkMonitorDeviceWidget> deviceWidgets = new ArrayList<>();

    public NetworkMonitorScreen(final NetworkMonitorContainerMenu menu, final Inventory playerInventory,
                                final Component title) {
        super(menu, playerInventory, title, 193, 80);
    }

    @Override
    protected void init() {
        super.init();

        initDevices();
        initDetails();

        getExclusionZones().add(new Rect2i(
            leftPos - DEVICES_SPRITE_WIDTH,
            topPos,
            DEVICES_SPRITE_WIDTH,
            DEVICES_TOP_HEIGHT + (getVisibleRows() * ROW_SIZE) + DEVICES_BOTTOM_HEIGHT
        ));
    }

    @Override
    protected void initStretching(final int rows, final int topHeight) {
        super.initStretching(rows, topHeight);

        if (searchField == null) {
            searchField = new SearchFieldWidget(
                font,
                leftPos - DEVICES_SPRITE_WIDTH + 27,
                topPos + 6 + 1,
                62 - 6,
                new History(SEARCH_FIELD_HISTORY)
            );
        } else {
            searchField.setX(leftPos - DEVICES_SPRITE_WIDTH + 27);
            searchField.setY(topPos + 6 + 1);
        }
        searchField.setResponder(this::onSearchTextChanged);
        addWidget(searchField);

        addSideButton(new RedstoneModeSideButtonWidget(menu.getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new NetworkMonitorGroupTypeSideButtonWidget(getMenu()));
        viewTypeSideButtonWidget = new NetworkMonitorViewTypeSideButtonWidget(getMenu());
        addSideButton(viewTypeSideButtonWidget);
        addSideButton(new NetworkMonitorSortingDirectionSideButtonWidget(getMenu()));
        addSideButton(new NetworkMonitorSortingTypeSideButtonWidget(getMenu()));
        addSideButton(new AutoSelectedSideButtonWidget(searchField));

        addRenderableWidget(new SearchIconWidget(
            leftPos - DEVICES_SPRITE_WIDTH + 11,
            topPos + 5,
            () -> SEARCH_HELP,
            searchField
        ));
    }

    private void initDevices() {
        final NetworkMonitorDeviceGroupWidget previouslyExpandedDeviceGroup = clearDeviceGroups();
        final NetworkMonitorDeviceCategoryWidget previouslyExpandedDeviceCategory = clearDeviceCategories();
        final NetworkMonitorDeviceWidget previouslySelectedDevice = clearDevices();
        this.devicesScrollbar = new ScrollbarWidget(leftPos - 13, getDevicesY(),
            ScrollbarWidget.Type.NORMAL, (getVisibleRows() * ROW_SIZE) - 2);
        devicesScrollbar.setListener(this::onScrolledDevices);
        int y = getDevicesY();
        y += addNetworkWidget(y);
        addDeviceWidgets(previouslyExpandedDeviceGroup, previouslyExpandedDeviceCategory, previouslySelectedDevice, y);
        updateDevicesScrollbar();
        loadCurrentDevice();
    }

    private int addNetworkWidget(final int y) {
        this.networkWidget = new NetworkMonitorNetworkWidget(getDevicesX(), y,
            this::onClickOnNetwork, networkWidget != null && networkWidget.active);
        addWidget(networkWidget);
        detectWhetherNetworkWidgetIsHiddenOrOutOfFrame();
        return networkWidget.getHeight();
    }

    private void onClickOnNetwork() {
        switch (menu.getGroupType()) {
            case DEVICE_TYPE -> menu.setCurrentDeviceGroup(null);
            case DEVICE_CATEGORY -> menu.setCurrentDeviceCategory(null);
            case NONE -> menu.setCurrentDevice(null, null, null);
        }
    }

    private void addDeviceWidgets(@Nullable final NetworkMonitorDeviceGroupWidget previouslyExpandedDeviceGroup,
                                  @Nullable final NetworkMonitorDeviceCategoryWidget previouslyExpandedDeviceCategory,
                                  @Nullable final NetworkMonitorDeviceWidget previouslySelectedDevice,
                                  final int startY) {
        int deviceGroupY = startY;
        int deviceCategoryY = startY;
        int deviceY = startY;

        final Map<NetworkMonitorDeviceCategory, List<NetworkMonitorDevice>> deviceGroupsByCategory =
            new TreeMap<>(menu.getDeviceCategorySorter());
        final List<NetworkMonitorDevice> devices = new ArrayList<>();

        // TODO: Sort type button
        // TODO: Sorting direction

        final Comparator<NetworkMonitorDevice> deviceSort = menu.getDeviceSorter();

        for (final NetworkMonitorDeviceGroup deviceGroup : menu.getDeviceGroups().stream()
            .sorted(menu.getDeviceGroupSorter()).toList()) {
            final NetworkMonitorDeviceCategory deviceCategory = RefinedStorageApi.INSTANCE
                .getNetworkMonitorDeviceCategory(deviceGroup.type());
            deviceGroupsByCategory.computeIfAbsent(deviceCategory, k -> new ArrayList<>())
                .addAll(deviceGroup.devices());

            final NetworkMonitorDeviceGroupWidget deviceGroupWidget = addDeviceGroupWithoutRelayoutOrSort(deviceGroup,
                deviceGroupY, previouslyExpandedDeviceGroup != null && previouslyExpandedDeviceGroup.is(deviceGroup));
            deviceGroupWidget.sortDevices(deviceSort);
            detectWhetherDeviceGroupWidgetIsHiddenOrOutOfFrame(deviceGroupWidget);
            deviceGroupY += deviceGroupWidget.getHeight();

            devices.addAll(deviceGroup.devices());
        }

        for (final NetworkMonitorDevice device : devices.stream().sorted(deviceSort).toList()) {
            final NetworkMonitorDeviceWidget deviceWidget = addDeviceWithoutRelayoutOrSort(device,
                deviceY, previouslySelectedDevice != null && previouslySelectedDevice.is(device));
            detectWhetherDeviceWidgetIsHiddenOrOutOfFrame(deviceWidget);
            deviceY += deviceWidget.getHeight();
        }

        for (final Map.Entry<NetworkMonitorDeviceCategory, List<NetworkMonitorDevice>> entry
            : deviceGroupsByCategory.entrySet()) {
            final NetworkMonitorDeviceCategory deviceCategory = entry.getKey();
            final List<NetworkMonitorDevice> devicesInCategory = entry.getValue();
            final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget = addDeviceCategoryWithoutRelayoutOrSort(
                deviceCategory, devicesInCategory, deviceCategoryY, previouslyExpandedDeviceCategory != null
                    && previouslyExpandedDeviceCategory.getDeviceCategory() == deviceCategory);
            deviceCategoryWidget.sortDevices(deviceSort);
            detectWhetherDeviceCategoryWidgetIsHiddenOrOutOfFrame(deviceCategoryWidget);
            deviceCategoryY += deviceCategoryWidget.getHeight();
        }
    }

    private void initDetails() {
        menu.setListener(this);
        loadCurrentDetails();
    }

    private void loadCurrentDevice() {
        final NetworkMonitorDevice currentDevice = menu.getCurrentDevice();
        if (currentDevice != null) {
            onCurrentDeviceChanged(currentDevice);
            return;
        }
        final NetworkMonitorDeviceGroup currentDeviceGroup = menu.getCurrentDeviceGroup();
        if (currentDeviceGroup != null) {
            onCurrentDeviceGroupChanged(currentDeviceGroup);
            return;
        }
        final NetworkMonitorDeviceCategory currentDeviceCategory = menu.getCurrentDeviceCategory();
        if (currentDeviceCategory != null) {
            onCurrentDeviceCategoryChanged(currentDeviceCategory);
        }
    }

    private void loadCurrentDetails() {
        onDetailsChanged(menu.getCurrentDeviceGroup(), menu.getCurrentDeviceCategory(),
            menu.getCurrentDevice(), menu.getCurrentDetails());
    }

    @Nullable
    private NetworkMonitorDeviceGroupWidget clearDeviceGroups() {
        final NetworkMonitorDeviceGroupWidget expanded = deviceGroupWidgets.stream()
            .filter(NetworkMonitorDeviceGroupWidget::isExpanded)
            .findFirst()
            .orElse(null);
        deviceGroupWidgets.clear();
        return expanded;
    }

    @Nullable
    private NetworkMonitorDeviceCategoryWidget clearDeviceCategories() {
        final NetworkMonitorDeviceCategoryWidget expanded = deviceCategoryWidgets.stream()
            .filter(NetworkMonitorDeviceCategoryWidget::isExpanded)
            .findFirst()
            .orElse(null);
        deviceCategoryWidgets.clear();
        return expanded;
    }

    @Nullable
    private NetworkMonitorDeviceWidget clearDevices() {
        final NetworkMonitorDeviceWidget selected = deviceWidgets.stream()
            .filter(NetworkMonitorDeviceWidget::isSelected)
            .findFirst()
            .orElse(null);
        deviceWidgets.clear();
        return selected;
    }

    private void detectWhetherNetworkWidgetIsHiddenOrOutOfFrame() {
        if (networkWidget == null) {
            return;
        }
        if (!menu.isActive() || menu.isSearching()) {
            networkWidget.setOutOfFrame(false);
            networkWidget.visible = false;
            return;
        }
        final int minY = getDevicesY();
        final int y = networkWidget.getY();
        final int height = networkWidget.getHeight();
        networkWidget.setOutOfFrame(y < minY - height || y > minY + (getVisibleRows() * ROW_SIZE));
        networkWidget.visible = true;
    }

    private void detectWhetherDeviceGroupWidgetIsHiddenOrOutOfFrame(final NetworkMonitorDeviceGroupWidget widget) {
        if (!menu.isActive()
            || !menu.isVisible(widget.getDeviceGroup())
            || menu.getGroupType() != NetworkMonitorGroupType.DEVICE_TYPE) {
            widget.setAllowedByFiltering(false);
            widget.setOutOfFrame(true);
            return;
        }
        final int minY = getDevicesY();
        final int y = widget.getY();
        final int height = widget.getHeight();
        widget.setAllowedByFiltering(true);
        widget.setOutOfFrame(y < minY - height || y > minY + (getVisibleRows() * ROW_SIZE));
    }

    private void detectWhetherDeviceCategoryWidgetIsHiddenOrOutOfFrame(
        final NetworkMonitorDeviceCategoryWidget widget
    ) {
        if (!menu.isActive()
            || !menu.isVisible(widget.getDeviceCategory())
            || menu.getGroupType() != NetworkMonitorGroupType.DEVICE_CATEGORY) {
            widget.setAllowedByFiltering(false);
            widget.setOutOfFrame(true);
            return;
        }
        final int minY = getDevicesY();
        final int y = widget.getY();
        final int height = widget.getHeight();
        widget.setAllowedByFiltering(true);
        widget.setOutOfFrame(y < minY - height || y > minY + (getVisibleRows() * ROW_SIZE));
    }

    private void detectWhetherDeviceWidgetIsHiddenOrOutOfFrame(final NetworkMonitorDeviceWidget widget) {
        if (!menu.isActive()
            || !menu.isVisible(widget.getDevice())
            || menu.getGroupType() != NetworkMonitorGroupType.NONE) {
            widget.setAllowedByFiltering(false);
            widget.setOutOfFrame(true);
            return;
        }
        final int minY = getDevicesY();
        final int y = widget.getY();
        final int height = widget.getHeight();
        widget.setAllowedByFiltering(true);
        widget.setOutOfFrame(y < minY - height || y > minY + (getVisibleRows() * ROW_SIZE));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        boolean needsRelayout = false;
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            needsRelayout |= deviceGroupWidget.update();
        }
        for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
            needsRelayout |= deviceCategoryWidget.update();
        }
        if (needsRelayout) {
            relayoutDevices();
        }
        if (viewTypeSideButtonWidget != null) {
            viewTypeSideButtonWidget.setEmptyWarningVisible(getMenu().isEmptyDeviceCategoryWarningVisible());
        }
    }

    @Override
    public void onCurrentDeviceGroupChanged(@Nullable final NetworkMonitorDeviceGroup deviceGroup) {
        if (networkWidget != null) {
            networkWidget.active = deviceGroup != null;
        }
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.onCurrentDeviceGroupChanged(deviceGroup);
        }
    }

    @Override
    public void onCurrentDeviceCategoryChanged(@Nullable final NetworkMonitorDeviceCategory deviceCategory) {
        if (networkWidget != null) {
            networkWidget.active = deviceCategory != null;
        }
        for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
            deviceCategoryWidget.onCurrentDeviceCategoryChanged(deviceCategory);
        }
    }

    @Override
    public void onCurrentDeviceChanged(@Nullable final NetworkMonitorDevice device) {
        if (networkWidget != null) {
            networkWidget.active = device != null;
        }
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.onCurrentDeviceChanged(device);
        }
        for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
            deviceCategoryWidget.onCurrentDeviceChanged(device);
        }
        for (final NetworkMonitorDeviceWidget deviceWidget : deviceWidgets) {
            deviceWidget.onCurrentDeviceChanged(device);
        }
    }

    @Override
    public void onDeviceGroupAdded(final NetworkMonitorDeviceGroup deviceGroup) {
        addDeviceGroupWithoutRelayoutOrSort(deviceGroup, 0, false);
        sortDeviceGroups();
        relayoutDevices();
    }

    private NetworkMonitorDeviceGroupWidget addDeviceGroupWithoutRelayoutOrSort(
        final NetworkMonitorDeviceGroup deviceGroup,
        final int y,
        final boolean expanded
    ) {
        final NetworkMonitorDeviceGroupWidget widget = new NetworkMonitorDeviceGroupWidget(
            getDevicesX(),
            y,
            deviceGroup,
            () -> menu.setCurrentDeviceGroup(deviceGroup),
            device -> menu.setCurrentDevice(deviceGroup, null, device),
            menu::isVisible,
            expanded
        );
        deviceGroupWidgets.add(addWidget(widget));
        return widget;
    }

    private void sortDeviceGroups() {
        final Comparator<NetworkMonitorDeviceGroup> deviceGroupSort = menu.getDeviceGroupSorter();
        deviceGroupWidgets.sort((widget1, widget2) -> {
            final NetworkMonitorDeviceGroup group1 = widget1.getDeviceGroup();
            final NetworkMonitorDeviceGroup group2 = widget2.getDeviceGroup();
            return deviceGroupSort.compare(group1, group2);
        });
        final Comparator<NetworkMonitorDevice> deviceSort = menu.getDeviceSorter();
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.sortDevices(deviceSort);
        }
    }

    private NetworkMonitorDeviceCategoryWidget addDeviceCategoryWithoutRelayoutOrSort(
        final NetworkMonitorDeviceCategory deviceCategory,
        final List<NetworkMonitorDevice> devices,
        final int y,
        final boolean expanded
    ) {
        final NetworkMonitorDeviceCategoryWidget widget = new NetworkMonitorDeviceCategoryWidget(
            getDevicesX(),
            y,
            deviceCategory,
            devices,
            () -> menu.setCurrentDeviceCategory(deviceCategory),
            device -> menu.setCurrentDevice(null, deviceCategory, device),
            menu::isVisible,
            expanded
        );
        deviceCategoryWidgets.add(addWidget(widget));
        return widget;
    }

    private void sortDeviceCategories() {
        final Comparator<NetworkMonitorDeviceCategory> deviceCategorySort = menu.getDeviceCategorySorter();
        deviceCategoryWidgets.sort((widget1, widget2) -> {
            final NetworkMonitorDeviceCategory category1 = widget1.getDeviceCategory();
            final NetworkMonitorDeviceCategory category2 = widget2.getDeviceCategory();
            return deviceCategorySort.compare(category1, category2);
        });
        final Comparator<NetworkMonitorDevice> deviceSort = menu.getDeviceSorter();
        for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
            deviceCategoryWidget.sortDevices(deviceSort);
        }
    }

    private NetworkMonitorDeviceWidget addDeviceWithoutRelayoutOrSort(final NetworkMonitorDevice device,
                                                                      final int y,
                                                                      final boolean selected) {
        final NetworkMonitorDeviceWidget widget = new NetworkMonitorDeviceWidget(
            getDevicesX(),
            y,
            NetworkMonitorDeviceWidget.WIDTH,
            device,
            () -> menu.setCurrentDevice(null, null, device),
            menu.isVisible(device)
        );
        widget.active = !selected;
        deviceWidgets.add(addWidget(widget));
        return widget;
    }

    private void sortDevices() {
        final Comparator<NetworkMonitorDevice> deviceSort = menu.getDeviceSorter();
        deviceWidgets.sort((widget1, widget2) -> {
            final NetworkMonitorDevice device1 = widget1.getDevice();
            final NetworkMonitorDevice device2 = widget2.getDevice();
            return deviceSort.compare(device1, device2);
        });
    }

    @Override
    public void onDeviceGroupRemoved(final NetworkMonitorDeviceGroup deviceGroup) {
        final boolean needsRelayout = deviceGroupWidgets.removeIf(deviceGroupWidget -> {
            final boolean removed = deviceGroupWidget.is(deviceGroup);
            if (removed) {
                removeWidget(deviceGroupWidget);
            }
            return removed;
        });
        if (needsRelayout) {
            relayoutDevices();
        }
    }

    @Override
    public void onDeviceCategoryAdded(final NetworkMonitorDeviceCategory deviceCategory) {
        addDeviceCategoryWithoutRelayoutOrSort(deviceCategory, List.of(), 0, false);
        sortDeviceCategories();
        relayoutDevices();
    }

    @Override
    public void onDeviceCategoryRemoved(final NetworkMonitorDeviceCategory deviceCategory) {
        final boolean needsRelayout = deviceCategoryWidgets.removeIf(deviceCategoryWidget -> {
            final boolean removed = deviceCategoryWidget.getDeviceCategory() == deviceCategory;
            if (removed) {
                removeWidget(deviceCategoryWidget);
            }
            return removed;
        });
        if (needsRelayout) {
            relayoutDevices();
        }
    }

    @Override
    public void onDeviceAdded(final NetworkMonitorDeviceGroup deviceGroup,
                              final NetworkMonitorDeviceCategory deviceCategory,
                              final NetworkMonitorDevice device) {
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.onDeviceAdded(deviceGroup, device);
        }
        for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
            deviceCategoryWidget.onDeviceAdded(deviceCategory, device);
        }
        addDeviceWithoutRelayoutOrSort(device, 0, false);
        resort();
    }

    @Override
    public void onDeviceRemoved(final NetworkMonitorDeviceGroup deviceGroup,
                                final NetworkMonitorDeviceCategory deviceCategory,
                                final NetworkMonitorDevice device) {
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.onDeviceRemoved(deviceGroup, device);
        }
        for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
            deviceCategoryWidget.onDeviceRemoved(deviceCategory, device);
        }
        deviceWidgets.removeIf(deviceWidget -> {
            final boolean removed = deviceWidget.is(device);
            if (removed) {
                removeWidget(deviceWidget);
            }
            return removed;
        });
        resort();
    }

    @Override
    public void onDetailsChanged(@Nullable final NetworkMonitorDeviceGroup deviceGroup,
                                 @Nullable final NetworkMonitorDeviceCategory deviceCategory,
                                 @Nullable final NetworkMonitorDevice device,
                                 @Nullable final NetworkNodeDetails details) {
        if (details != null) {
            showNetworkStatistics = false;
            final NetworkNodeDetailsRenderer renderer = RefinedStorageClientApi.INSTANCE
                .getNetworkNodeDetailsRenderer(details.getClass());
            updateScrollbarRows(renderer.getRows(details));
        } else if (deviceGroup == null && deviceCategory == null && device == null && menu.isActive()) {
            showNetworkStatistics = true;
            updateScrollbarContentHeight(getNetworkStatisticsHeight());
        } else {
            showNetworkStatistics = false;
            updateScrollbarRows(0);
        }
        resetScrollbarOffset();
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        relayoutDevices();
    }

    @Override
    public void onGroupTypeChanged(final NetworkMonitorGroupType groupType) {
        relayoutDevices();
    }

    @Override
    public void onViewTypeChanged(@Nullable final NetworkMonitorDeviceCategory viewType) {
        relayoutDevices();
    }

    @Override
    public void onSortingTypeChanged(final NetworkMonitorSortingType sortingType) {
        resort();
    }

    @Override
    public void onSortingDirectionChanged(final NetworkMonitorSortingDirection sortingDirection) {
        resort();
    }

    private void resort() {
        sortDeviceGroups();
        sortDeviceCategories();
        sortDevices();
        relayoutDevices();
    }

    private void relayoutDevices() {
        if (devicesScrollbar == null) {
            return;
        }
        final int scrollOffset = devicesScrollbar.isSmoothScrolling()
            ? (int) devicesScrollbar.getOffset()
            : (int) devicesScrollbar.getOffset() * ROW_SIZE;
        int y = getDevicesY() - scrollOffset;
        if (networkWidget != null) {
            networkWidget.setY(y);
            detectWhetherNetworkWidgetIsHiddenOrOutOfFrame();
            y += networkWidget.getHeight();
        }
        relayoutDeviceGroups(y);
        relayoutDeviceCategories(y);
        relayoutDevices(y);
        updateDevicesScrollbar();
    }

    private void relayoutDevices(final int startY) {
        int y = startY;
        for (final NetworkMonitorDeviceWidget deviceWidget : deviceWidgets) {
            deviceWidget.setY(y);
            detectWhetherDeviceWidgetIsHiddenOrOutOfFrame(deviceWidget);
            y += deviceWidget.getHeight();
        }
    }

    private void relayoutDeviceGroups(final int startY) {
        int y = startY;
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.setY(y);
            detectWhetherDeviceGroupWidgetIsHiddenOrOutOfFrame(deviceGroupWidget);
            y += deviceGroupWidget.getHeight();
        }
    }

    private void relayoutDeviceCategories(final int startY) {
        int y = startY;
        for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
            deviceCategoryWidget.setY(y);
            detectWhetherDeviceCategoryWidgetIsHiddenOrOutOfFrame(deviceCategoryWidget);
            y += deviceCategoryWidget.getHeight();
        }
    }

    private void updateDevicesScrollbar() {
        if (devicesScrollbar == null) {
            return;
        }
        if (!menu.isActive()) {
            devicesScrollbar.setEnabled(false);
            devicesScrollbar.setMaxOffset(0);
            return;
        }
        final int maxOffset = getDevicesScrollbarMaxOffset();
        devicesScrollbar.setEnabled(maxOffset > 0);
        devicesScrollbar.setMaxOffset(maxOffset);
    }

    private int getDevicesScrollbarMaxOffset() {
        if (devicesScrollbar == null) {
            return 0;
        }
        if (devicesScrollbar.isSmoothScrolling()) {
            final int totalHeight = switch (menu.getGroupType()) {
                case DEVICE_TYPE -> deviceGroupWidgets.stream()
                    .mapToInt(NetworkMonitorDeviceGroupWidget::getHeight).sum();
                case DEVICE_CATEGORY -> deviceCategoryWidgets.stream()
                    .mapToInt(NetworkMonitorDeviceCategoryWidget::getHeight).sum();
                case NONE -> deviceWidgets.stream()
                    .mapToInt(NetworkMonitorDeviceWidget::getHeight).sum();
            };
            final int networkHeight = networkWidget != null ? networkWidget.getHeight() : 0;
            return totalHeight + networkHeight - (getVisibleRows() * ROW_SIZE);
        }
        final int totalRows = switch (menu.getGroupType()) {
            case DEVICE_TYPE -> deviceGroupWidgets.stream().mapToInt(NetworkMonitorDeviceGroupWidget::getRows).sum();
            case DEVICE_CATEGORY -> deviceCategoryWidgets.stream().mapToInt(NetworkMonitorDeviceCategoryWidget::getRows)
                .sum();
            case NONE -> deviceWidgets.size();
        };
        final int networkRow = networkWidget != null && networkWidget.visible ? 1 : 0;
        return totalRows + networkRow - getVisibleRows();
    }

    private void onScrolledDevices(final double value) {
        if (devicesScrollbar == null) {
            return;
        }
        final int scrollOffset = devicesScrollbar.isSmoothScrolling()
            ? (int) value
            : (int) value * ROW_SIZE;
        int y = getDevicesY() - scrollOffset;
        if (networkWidget != null) {
            networkWidget.setY(y);
            detectWhetherNetworkWidgetIsHiddenOrOutOfFrame();
            y += networkWidget.getHeight();
        }
        switch (menu.getGroupType()) {
            case DEVICE_TYPE -> relayoutDeviceGroups(y);
            case DEVICE_CATEGORY -> relayoutDeviceCategories(y);
            case NONE -> relayoutDevices(y);
        }
    }

    void onSearchTextChanged(final String text) {
        menu.onSearchTextChanged(text);
        relayoutDevices();
    }

    @Override
    protected int getSideButtonX() {
        return leftPos + imageWidth + 2;
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
        if (devicesScrollbar != null) {
            devicesScrollbar.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
        if (searchField != null) {
            searchField.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        extractDevices(graphics, mouseX, mouseY, partialTicks);
    }

    private void extractDevices(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        graphics.blitSprite(GUI_TEXTURED, DEVICES, DEVICES_SPRITE_WIDTH, DEVICES_SPRITE_HEIGHT,
            0, 0,
            x - DEVICES_SPRITE_WIDTH + 4, y,
            DEVICES_SPRITE_WIDTH, DEVICES_TOP_HEIGHT);
        for (int i = 0; i < getVisibleRows(); ++i) {
            int textureY = DEVICES_TOP_HEIGHT + 1;
            if (i == 0) {
                textureY = DEVICES_TOP_HEIGHT;
            } else if (i == getVisibleRows() - 1) {
                textureY = DEVICES_TOP_HEIGHT + 2;
            }
            graphics.blitSprite(GUI_TEXTURED, DEVICES, DEVICES_SPRITE_WIDTH, DEVICES_SPRITE_HEIGHT,
                0, textureY,
                x - DEVICES_SPRITE_WIDTH + 4, y + DEVICES_TOP_HEIGHT + (i * ROW_SIZE),
                DEVICES_SPRITE_WIDTH, ROW_SIZE);
        }
        graphics.blitSprite(GUI_TEXTURED, DEVICES, DEVICES_SPRITE_WIDTH, DEVICES_SPRITE_HEIGHT,
            0, DEVICES_TOP_HEIGHT + 18 + 2,
            x - DEVICES_SPRITE_WIDTH + 4, y + DEVICES_TOP_HEIGHT + (getVisibleRows() * ROW_SIZE),
            DEVICES_SPRITE_WIDTH, DEVICES_BOTTOM_HEIGHT);
        extractDevicesContents(graphics, mouseX, mouseY, partialTicks);
    }

    private void extractDevicesContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                        final float partialTicks) {
        final int x = getDevicesX();
        final int y = getDevicesY();
        graphics.enableScissor(x, y, x + 64, y + (getVisibleRows() * ROW_SIZE) - 2);
        if (networkWidget != null) {
            networkWidget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
        switch (menu.getGroupType()) {
            case DEVICE_TYPE -> {
                for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
                    deviceGroupWidget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
                }
            }
            case DEVICE_CATEGORY -> {
                for (final NetworkMonitorDeviceCategoryWidget deviceCategoryWidget : deviceCategoryWidgets) {
                    deviceCategoryWidget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
                }
            }
            case NONE -> {
                for (final NetworkMonitorDeviceWidget deviceWidget : deviceWidgets) {
                    deviceWidget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
                }
            }
        }
        graphics.disableScissor();
    }

    private int getDevicesY() {
        return topPos + DEVICES_TOP_HEIGHT + 1;
    }

    private int getDevicesX() {
        return leftPos - DEVICES_SPRITE_WIDTH + 4 + 8;
    }

    @Override
    protected void renderRows(final GuiGraphicsExtractor graphics, final int x, final int y, final int topHeight,
                              final int rows, final int mouseX, final int mouseY, final float partialTicks) {
        final int detailsY = y + topHeight - getScrollbarOffset();
        final int detailsX = x + 7;
        if (showNetworkStatistics) {
            this.detailsTooltip = Collections.emptyList();
            renderNetworkStatistics(graphics, detailsX, detailsY);
            return;
        }
        final NetworkNodeDetails details = menu.getCurrentDetails();
        if (details == null) {
            return;
        }
        final NetworkNodeDetailsRenderer renderer = RefinedStorageClientApi.INSTANCE
            .getNetworkNodeDetailsRenderer(details.getClass());
        this.detailsTooltip = renderer.render(details, graphics, detailsX, detailsY, mouseX, mouseY);
    }

    private int getNetworkStatisticsHeight() {
        final NetworkMonitorNetworkStatistics networkStatistics = menu.getLastNetworkStatistics();
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        final int lineHeight = (int) (font.lineHeight * scale) + NETWORK_STATISTICS_PADDING;
        return 5 // padding on top
            + lineHeight // energy title
            + lineHeight // energy stored and capacity
            + lineHeight // energy usage
            + 16 + NETWORK_STATISTICS_PADDING // energy progress bar
            + lineHeight // devices
            + lineHeight // empty space
            + lineHeight // storage title
            + (lineHeight + 16 + NETWORK_STATISTICS_PADDING) * networkStatistics.storageStatistics().size()
            - NETWORK_STATISTICS_PADDING; // remove last padding because we don't need it after the last progress bar
    }

    private void renderNetworkStatistics(final GuiGraphicsExtractor graphics, final int x, final int startY) {
        final NetworkMonitorNetworkStatistics networkStatistics = menu.getLastNetworkStatistics();
        int y = startY + 5;
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        final int lineHeight = (int) (font.lineHeight * scale) + NETWORK_STATISTICS_PADDING;
        y = renderEnergyUsageAndStored(graphics, x, networkStatistics, y, scale, lineHeight);
        y = renderAmountOfDevices(graphics, x, networkStatistics, y, scale, lineHeight);
        y += lineHeight;
        renderStorageStatistics(graphics, x, y, scale, lineHeight, networkStatistics);
    }

    private int renderEnergyUsageAndStored(final GuiGraphicsExtractor graphics, final int x,
                                           final NetworkMonitorNetworkStatistics networkStatistics,
                                           final int startY, final float scale, final int lineHeight) {
        int y = startY;
        SmallText.render(graphics, font, ENERGY.getVisualOrderText(), x + 4, y, 0xFF404040, false,
            scale);
        y += lineHeight;
        final Component storedAndCapacity = createStoredWithCapacityTranslation(
            networkStatistics.energyStored(), networkStatistics.energyCapacity(), networkStatistics.energyPct(),
            0xFF404040
        ).withColor(0xFF404040);
        final Component energyUsage = createTranslation("gui", "network_monitor.network.energy.usage_per_tick",
            format(networkStatistics.energyUsage()));
        SmallText.render(graphics, font, storedAndCapacity.getVisualOrderText(), x + 4 + 4, y, 0xFF404040,
            false, scale);
        y += lineHeight;
        SmallText.render(graphics, font, energyUsage.getVisualOrderText(), x + 4 + 4, y, 0xFF404040, false, scale);
        y += lineHeight;
        ProgressBarWidget.renderHorizontal(graphics, x + 4 + 4, y, 150, 16, networkStatistics.energyPct());
        y += 16 + NETWORK_STATISTICS_PADDING;
        return y;
    }

    private int renderAmountOfDevices(final GuiGraphicsExtractor graphics, final int x,
                                      final NetworkMonitorNetworkStatistics networkStatistics, final int startY,
                                      final float scale,
                                      final int lineHeight) {
        int y = startY;
        final Component text = createTranslation("gui", "network_monitor.network.energy.amount_of_devices",
            format(networkStatistics.amountOfDevices()));
        SmallText.render(graphics, font, text.getVisualOrderText(), x + 4 + 4, y, 0xFF404040, false, scale);
        y += lineHeight;
        return y;
    }

    private void renderStorageStatistics(final GuiGraphicsExtractor graphics, final int x,
                                         final int startY, final float scale, final int lineHeight,
                                         final NetworkMonitorNetworkStatistics networkStatistics) {
        int y = startY;
        SmallText.render(graphics, font, STORAGE_DISKS_AND_BLOCKS.getVisualOrderText(), x + 4, y, 0xFF404040, false,
            scale);
        y += lineHeight;
        for (final NetworkMonitorNetworkStatistics.StorageStatistics storage : networkStatistics.storageStatistics()) {
            y = renderStorageStatistics(graphics, x, scale, lineHeight, networkStatistics, storage, y);
        }
    }

    private int renderStorageStatistics(final GuiGraphicsExtractor graphics,
                                        final int x, final float scale, final int lineHeight,
                                        final NetworkMonitorNetworkStatistics networkStatistics,
                                        final NetworkMonitorNetworkStatistics.StorageStatistics storage,
                                        final int startY) {
        int y = startY;
        final StorageType storageType = storage.type();
        SmallText.render(graphics, font, storageType.getName().getVisualOrderText(), x + 4 + 4, y, 0xFF404040,
            false, scale);
        final double pct = networkStatistics.storageTypePct(storageType);
        final Component storedAndCapacity = createStoredWithCapacityTranslation(
            networkStatistics.stored(storageType),
            networkStatistics.capacity(storageType),
            pct,
            0xFF404040
        ).withColor(0xFF404040);
        SmallText.render(graphics, font, storedAndCapacity.getVisualOrderText(),
            x + 4 + 153 - (int) (font.width(storedAndCapacity) * scale),
            y, 0xFF404040, false, scale);
        y += lineHeight;
        ProgressBarWidget.renderHorizontal(graphics, x + 4 + 4, y, 150, 16, pct);
        y += 16 + NETWORK_STATISTICS_PADDING;
        return y;
    }

    @Override
    protected void extractTooltip(final GuiGraphicsExtractor graphics, final int x, final int y) {
        if (!detailsTooltip.isEmpty()) {
            graphics.tooltip(font, detailsTooltip, x, y, DefaultTooltipPositioner.INSTANCE, null);
            return;
        }
        super.extractTooltip(graphics, x, y);
    }

    @Override
    protected void renderStretchingBackground(final GuiGraphicsExtractor graphics, final int x, final int y,
                                              final int rows) {
        for (int row = 0; row < rows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == rows - 1) {
                textureY = 55;
            }
            graphics.blit(GUI_TEXTURED, getTexture(), x, y + (ROW_SIZE * row), 0, textureY, imageWidth, ROW_SIZE,
                256, 256);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent e, final boolean doubleClick) {
        if (devicesScrollbar != null && devicesScrollbar.mouseClicked(e, doubleClick)) {
            return true;
        }
        if (searchField != null && searchField.mouseClicked(e, doubleClick)) {
            return true;
        }
        return super.mouseClicked(e, doubleClick);
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        return (searchField != null && searchField.charTyped(event)) || super.charTyped(event);
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (searchField != null && searchField.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (devicesScrollbar != null) {
            devicesScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (devicesScrollbar != null && devicesScrollbar.mouseReleased(event)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (devicesScrollbar != null && isHoveringOverDevices(x, y)) {
            return devicesScrollbar.mouseScrolled(x, y, scrollX, scrollY);
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private boolean isHoveringOverDevices(final double mouseX, final double mouseY) {
        final int x = getDevicesX();
        final int y = getDevicesY();
        return isHovering(x - leftPos, y - topPos,
            80 - 1, getVisibleRows() * ROW_SIZE - 1,
            mouseX, mouseY);
    }

    @Override
    protected int getScrollPanePadding() {
        return 4;
    }

    @Override
    protected int getBottomHeight() {
        return 7;
    }

    @Override
    protected int getBottomV() {
        return 73;
    }

    @Override
    public void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, -12566464, false);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
