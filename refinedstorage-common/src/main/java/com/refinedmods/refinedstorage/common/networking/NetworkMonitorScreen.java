package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
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
import java.util.List;

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
    private ScrollbarWidget deviceGroupsScrollbar;
    @Nullable
    private SearchFieldWidget searchField;
    private List<ClientTooltipComponent> detailsTooltip = Collections.emptyList();
    @Nullable
    private NetworkMonitorNetworkWidget networkWidget;
    private boolean showNetworkStatistics;

    private final List<NetworkMonitorDeviceGroupWidget> deviceGroupWidgets = new ArrayList<>();

    public NetworkMonitorScreen(final NetworkMonitorContainerMenu menu,
                                final Inventory playerInventory,
                                final Component title) {
        super(menu, playerInventory, title, 193, 80);
    }

    @Override
    protected void init() {
        super.init();
        initDeviceGroups();
        initDetails();
        getExclusionZones().add(new Rect2i(
            leftPos - DEVICES_SPRITE_WIDTH,
            topPos,
            DEVICES_SPRITE_WIDTH,
            DEVICES_TOP_HEIGHT + (getVisibleRows() * ROW_SIZE) + DEVICES_BOTTOM_HEIGHT
        ));
        addSideButton(new RedstoneModeSideButtonWidget(menu.getProperty(PropertyTypes.REDSTONE_MODE)));
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
        addSideButton(new AutoSelectedSideButtonWidget(searchField));
        addRenderableWidget(new SearchIconWidget(
            leftPos - DEVICES_SPRITE_WIDTH + 11,
            topPos + 5,
            () -> SEARCH_HELP,
            searchField
        ));
    }

    private void initDeviceGroups() {
        final NetworkMonitorDeviceGroupWidget previouslyExpanded = clearDeviceGroups();
        this.deviceGroupsScrollbar = new ScrollbarWidget(leftPos - 13, getDeviceGroupWidgetY(),
            ScrollbarWidget.Type.NORMAL, (getVisibleRows() * ROW_SIZE) - 2);
        deviceGroupsScrollbar.setListener(this::onScrolledDeviceGroups);
        int y = getDeviceGroupWidgetY();
        this.networkWidget = new NetworkMonitorNetworkWidget(getDeviceGroupWidgetX(), y,
            () -> menu.setCurrentDeviceGroup(null), networkWidget != null && networkWidget.active);
        addWidget(networkWidget);
        detectWhetherNetworkWidgetIsHiddenOrOutOfFrame();
        y += networkWidget.getHeight();
        for (int i = 0; i < menu.getDeviceGroups().size(); ++i) {
            final NetworkMonitorDeviceGroup deviceGroup = menu.getDeviceGroups().get(i);
            final boolean expanded = previouslyExpanded != null
                && previouslyExpanded.getDeviceGroup().id().equals(deviceGroup.id());
            final NetworkMonitorDeviceGroupWidget deviceGroupWidget = addDeviceGroupWithoutRelayout(deviceGroup, y,
                expanded);
            detectWhetherDeviceWidgetIsHiddenOrOutOfFrame(deviceGroupWidget);
            y += deviceGroupWidget.getHeight();
        }
        updateDeviceGroupsScrollbar();
        loadCurrentDeviceGroupAndDevice();
    }

    private void loadCurrentDeviceGroupAndDevice() {
        final NetworkMonitorDevice currentDevice = menu.getCurrentDevice();
        if (currentDevice != null) {
            onCurrentDeviceChanged(currentDevice);
            return;
        }
        final NetworkMonitorDeviceGroup currentDeviceGroup = menu.getCurrentDeviceGroup();
        onCurrentDeviceGroupChanged(currentDeviceGroup);
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

    private void detectWhetherNetworkWidgetIsHiddenOrOutOfFrame() {
        if (networkWidget == null) {
            return;
        }
        if (!menu.isActive() || menu.isSearching()) {
            networkWidget.setOutOfFrame(false);
            networkWidget.visible = false;
            return;
        }
        final int minY = getDeviceGroupWidgetY();
        final int y = networkWidget.getY();
        final int height = networkWidget.getHeight();
        networkWidget.setOutOfFrame(y < minY - height || y > minY + (getVisibleRows() * ROW_SIZE));
        networkWidget.visible = true;
    }

    private void detectWhetherDeviceWidgetIsHiddenOrOutOfFrame(final NetworkMonitorDeviceGroupWidget widget) {
        if (!menu.isActive() || !menu.isVisible(widget.getDeviceGroup())) {
            widget.setAllowedByFiltering(false);
            widget.setOutOfFrame(true);
            return;
        }
        final int minY = getDeviceGroupWidgetY();
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
        if (needsRelayout) {
            relayoutDeviceGroups();
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
    public void onCurrentDeviceChanged(@Nullable final NetworkMonitorDevice device) {
        if (networkWidget != null) {
            networkWidget.active = device != null;
        }
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.onCurrentDeviceChanged(device);
        }
    }

    @Override
    public void onDeviceGroupAdded(final NetworkMonitorDeviceGroup deviceGroup) {
        addDeviceGroupWithoutRelayout(deviceGroup, 0, false);
        relayoutDeviceGroups();
    }

    private NetworkMonitorDeviceGroupWidget addDeviceGroupWithoutRelayout(final NetworkMonitorDeviceGroup deviceGroup,
                                                                          final int y,
                                                                          final boolean expanded) {
        final NetworkMonitorDeviceGroupWidget widget = new NetworkMonitorDeviceGroupWidget(
            getDeviceGroupWidgetX(),
            y,
            deviceGroup,
            () -> menu.setCurrentDeviceGroup(deviceGroup),
            device -> menu.setCurrentDevice(deviceGroup, device),
            menu::isVisible,
            expanded
        );
        deviceGroupWidgets.add(addWidget(widget));
        return widget;
    }

    @Override
    public void onDeviceGroupRemoved(final NetworkMonitorDeviceGroup deviceGroup) {
        final boolean needsRelayout = deviceGroupWidgets.removeIf(deviceGroupWidget -> {
            final boolean removed = deviceGroupWidget.onDeviceGroupRemoved(deviceGroup);
            if (removed) {
                removeWidget(deviceGroupWidget);
            }
            return removed;
        });
        if (needsRelayout) {
            relayoutDeviceGroups();
        }
    }

    @Override
    public void onDeviceAdded(final NetworkMonitorDeviceGroup deviceGroup, final NetworkMonitorDevice device) {
        boolean needsRelayout = false;
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            needsRelayout |= deviceGroupWidget.onDeviceAdded(deviceGroup, device);
        }
        if (needsRelayout) {
            relayoutDeviceGroups();
        }
    }

    @Override
    public void onDeviceRemoved(final NetworkMonitorDeviceGroup deviceGroup, final NetworkMonitorDevice device) {
        boolean needsRelayout = false;
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            needsRelayout |= deviceGroupWidget.onDeviceRemoved(deviceGroup, device);
        }
        if (needsRelayout) {
            relayoutDeviceGroups();
        }
    }

    @Override
    public void onDetailsChanged(@Nullable final NetworkMonitorDeviceGroup deviceGroup,
                                 @Nullable final NetworkMonitorDevice device,
                                 @Nullable final NetworkNodeDetails details) {
        if (scrollbar == null) {
            return;
        }
        if (details != null) {
            showNetworkStatistics = false;
            final NetworkNodeDetailsRenderer renderer = RefinedStorageClientApi.INSTANCE
                .getNetworkNodeDetailsRenderer(details.getClass());
            scrollbar.resetSmoothScrolling();
            updateScrollbarRows(renderer.getRows(details));
        } else if (device == null && deviceGroup == null) {
            showNetworkStatistics = true;
            scrollbar.setSmoothScrolling(true);
            updateScrollbarBasedOnStretchedHeight(getNetworkStatisticsHeight());
        } else {
            showNetworkStatistics = false;
            scrollbar.resetSmoothScrolling();
            updateScrollbarRows(0);
        }
        scrollbar.setOffset(0);
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        relayoutDeviceGroups();
    }

    private void relayoutDeviceGroups() {
        if (deviceGroupsScrollbar == null) {
            return;
        }
        final int scrollOffset = deviceGroupsScrollbar.isSmoothScrolling()
            ? (int) deviceGroupsScrollbar.getOffset()
            : (int) deviceGroupsScrollbar.getOffset() * ROW_SIZE;
        int y = getDeviceGroupWidgetY() - scrollOffset;
        if (networkWidget != null) {
            networkWidget.setY(y);
            detectWhetherNetworkWidgetIsHiddenOrOutOfFrame();
            y += networkWidget.getHeight();
        }
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.setY(y);
            detectWhetherDeviceWidgetIsHiddenOrOutOfFrame(deviceGroupWidget);
            y += deviceGroupWidget.getHeight();
        }
        updateDeviceGroupsScrollbar();
    }

    private void updateDeviceGroupsScrollbar() {
        if (deviceGroupsScrollbar == null) {
            return;
        }
        if (!menu.isActive()) {
            deviceGroupsScrollbar.setEnabled(false);
            deviceGroupsScrollbar.setMaxOffset(0);
            return;
        }
        final int maxOffset = getDeviceGroupsScrollbarMaxOffset();
        deviceGroupsScrollbar.setEnabled(maxOffset > 0);
        deviceGroupsScrollbar.setMaxOffset(maxOffset);
    }

    private int getDeviceGroupsScrollbarMaxOffset() {
        if (deviceGroupsScrollbar == null) {
            return 0;
        }
        if (deviceGroupsScrollbar.isSmoothScrolling()) {
            final int totalHeight = deviceGroupWidgets.stream()
                .mapToInt(NetworkMonitorDeviceGroupWidget::getHeight).sum();
            final int networkHeight = networkWidget != null ? networkWidget.getHeight() : 0;
            return totalHeight + networkHeight - (getVisibleRows() * ROW_SIZE);
        }
        final int totalRows = deviceGroupWidgets.stream().mapToInt(NetworkMonitorDeviceGroupWidget::getRows).sum();
        final int networkRow = networkWidget != null && networkWidget.visible ? 1 : 0;
        return totalRows + networkRow - getVisibleRows();
    }

    private void onScrolledDeviceGroups(final double value) {
        if (deviceGroupsScrollbar == null) {
            return;
        }
        final int scrollOffset = deviceGroupsScrollbar.isSmoothScrolling()
            ? (int) value
            : (int) value * ROW_SIZE;
        int y = getDeviceGroupWidgetY() - scrollOffset;
        if (networkWidget != null) {
            networkWidget.setY(y);
            detectWhetherNetworkWidgetIsHiddenOrOutOfFrame();
            y += networkWidget.getHeight();
        }
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.setY(y);
            detectWhetherDeviceWidgetIsHiddenOrOutOfFrame(deviceGroupWidget);
            y += deviceGroupWidget.getHeight();
        }
    }

    private void initDetails() {
        menu.setListener(this);
        onDetailsChanged(menu.getCurrentDeviceGroup(), menu.getCurrentDevice(), menu.getCurrentDetails());
    }

    void onSearchTextChanged(final String text) {
        menu.onSearchTextChanged(text);
        relayoutDeviceGroups();
    }

    @Override
    protected int getSideButtonX() {
        return leftPos + imageWidth + 2;
    }

    @Override
    public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                final float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);
        if (deviceGroupsScrollbar != null) {
            deviceGroupsScrollbar.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
        if (searchField != null) {
            searchField.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        extractDeviceGroups(graphics, mouseX, mouseY, partialTicks);
    }

    private void extractDeviceGroups(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
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
        extractDeviceGroupsContents(graphics, mouseX, mouseY, partialTicks);
    }

    private void extractDeviceGroupsContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                             final float partialTicks) {
        final int x = getDeviceGroupWidgetX();
        final int y = getDeviceGroupWidgetY();
        graphics.enableScissor(x, y, x + 64, y + (getVisibleRows() * ROW_SIZE) - 2);
        if (networkWidget != null) {
            networkWidget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
        for (final NetworkMonitorDeviceGroupWidget deviceGroupWidget : deviceGroupWidgets) {
            deviceGroupWidget.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
        graphics.disableScissor();
    }

    private int getDeviceGroupWidgetY() {
        return topPos + DEVICES_TOP_HEIGHT + 1;
    }

    private int getDeviceGroupWidgetX() {
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
        if (deviceGroupsScrollbar != null && deviceGroupsScrollbar.mouseClicked(e, doubleClick)) {
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
        if (deviceGroupsScrollbar != null) {
            deviceGroupsScrollbar.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (deviceGroupsScrollbar != null && deviceGroupsScrollbar.mouseReleased(event)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        final boolean didTaskButtonsScrollbar = deviceGroupsScrollbar != null
            && isHoveringOverDeviceGroupButton(x, y)
            && deviceGroupsScrollbar.mouseScrolled(x, y, scrollX, scrollY);
        return didTaskButtonsScrollbar || super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private boolean isHoveringOverDeviceGroupButton(final double mouseX, final double mouseY) {
        final int x = getDeviceGroupWidgetX();
        final int y = getDeviceGroupWidgetY();
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
