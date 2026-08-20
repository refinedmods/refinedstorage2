package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.networking.NetworkMonitorDeviceCategory;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.Sprites;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.CustomButton;
import com.refinedmods.refinedstorage.common.support.widget.ExpandCollapseState;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

class NetworkMonitorDeviceCategoryWidget implements LayoutElement, Renderable, GuiEventListener, NarratableEntry {
    private static final int WIDTH = 64;
    private static final int EXPAND_COLLAPSE_WIDTH = 16;

    private static final int CATEGORY_HEIGHT = 18;
    private static final int CATEGORY_WIDTH = WIDTH - EXPAND_COLLAPSE_WIDTH;

    private static final int DEVICE_X_OFFSET = 6;
    private static final int DEVICE_WIDTH = WIDTH - DEVICE_X_OFFSET;
    private static final int DEVICE_HEIGHT = 18;

    private final NetworkMonitorDeviceCategory deviceCategory;
    private final DeviceCategoryButton deviceCategoryButton;
    private final List<NetworkMonitorDeviceWidget> deviceButtons;
    private final CustomButton expandCollapseButton;
    private final ExpandCollapseState expandCollapseState = new ExpandCollapseState();
    private final Consumer<NetworkMonitorDevice> deviceSelected;
    private final Predicate<NetworkMonitorDevice> deviceVisible;

    private int x;
    private int y;
    private boolean focused;
    private boolean outOfFrame;
    private boolean allowedByFiltering;
    private int visibleDeviceButtons;

    NetworkMonitorDeviceCategoryWidget(final int x, final int y, final NetworkMonitorDeviceCategory deviceCategory,
                                       final List<NetworkMonitorDevice> devices,
                                       final Runnable selected, final Consumer<NetworkMonitorDevice> deviceSelected,
                                       final Predicate<NetworkMonitorDevice> deviceVisible,
                                       final boolean expanded) {
        this.deviceCategory = deviceCategory;
        this.x = x;
        this.y = y;
        this.deviceCategoryButton = new DeviceCategoryButton(x, y, deviceCategory, devices, selected);
        this.deviceVisible = deviceVisible;
        this.expandCollapseButton = createExpandCollapseButton(x + deviceCategoryButton.getWidth(), y,
            expandCollapseState);
        this.deviceButtons = new ArrayList<>();
        this.deviceSelected = deviceSelected;
        for (final NetworkMonitorDevice device : devices) {
            addDevice(device);
        }
        if (expanded) {
            expand();
        }
    }

    private static CustomButton createExpandCollapseButton(final int x, final int y,
                                                           final ExpandCollapseState expandCollapseState) {
        return new CustomButton(x, y, EXPAND_COLLAPSE_WIDTH, CATEGORY_HEIGHT, Sprites.EXPAND, btn -> {
            final boolean expanding = expandCollapseState.toggle();
            btn.setSprites(expanding ? Sprites.COLLAPSE : Sprites.EXPAND);
        }, Component.empty());
    }

    NetworkMonitorDeviceCategory getDeviceCategory() {
        return deviceCategory;
    }

    void addDevice(final NetworkMonitorDevice device) {
        final int buttonY = y + (CATEGORY_HEIGHT * (visibleDeviceButtons + 1));
        deviceButtons.add(new NetworkMonitorDeviceWidget(
            x + DEVICE_X_OFFSET,
            buttonY,
            DEVICE_WIDTH,
            device,
            () -> deviceSelected.accept(device),
            deviceVisible.test(device)
        ));
    }

    boolean update() {
        return expandCollapseState.updateAnimation();
    }

    @Override
    public boolean isActive() {
        return !outOfFrame;
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY) {
        return isActive() && areCoordinatesInRectangle(mouseX, mouseY);
    }

    private boolean areCoordinatesInRectangle(final double mouseX, final double mouseY) {
        return mouseX >= (double) this.getX()
            && mouseY >= (double) this.getY()
            && mouseX < (double) this.getRight()
            && mouseY < (double) this.getBottom();
    }

    private int getRight() {
        return this.getX() + this.getWidth();
    }

    private int getBottom() {
        return this.getY() + this.getHeight();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(x, y, WIDTH, 18);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                   final float partialTicks) {
        if (outOfFrame) {
            return;
        }
        deviceCategoryButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        expandCollapseButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        if (!expandCollapseState.isExpanded()) {
            return;
        }
        extractExpandedState(graphics, mouseX, mouseY, partialTicks);
    }

    private void extractExpandedState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                      final float partialTicks) {
        final int expandedHeight = getExpandedHeight();
        graphics.enableScissor(x, y + CATEGORY_HEIGHT, x + WIDTH, y + CATEGORY_HEIGHT + expandedHeight);
        graphics.verticalLine(x + 2, y + CATEGORY_HEIGHT - 1, y + getHeight(), 0xFF333333);
        for (final NetworkMonitorDeviceWidget deviceButton : deviceButtons) {
            graphics.horizontalLine(x + 2, x + 5, deviceButton.getY() + (CATEGORY_HEIGHT / 2) - 1, 0xFF333333);
            deviceButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        }
        graphics.disableScissor();
    }

    @Override
    public void setFocused(final boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    void setOutOfFrame(final boolean outOfFrame) {
        this.outOfFrame = outOfFrame;
    }

    void setAllowedByFiltering(final boolean allowedByFiltering) {
        this.allowedByFiltering = allowedByFiltering;
    }

    @Override
    public void setX(final int x) {
        this.x = x;
        deviceCategoryButton.setX(x);
        expandCollapseButton.setX(x + deviceCategoryButton.getWidth());
        deviceButtons.forEach(deviceButton -> deviceButton.setX(x + DEVICE_X_OFFSET));
    }

    @Override
    public void setY(final int y) {
        this.y = y;
        deviceCategoryButton.setY(y);
        expandCollapseButton.setY(y);
        relayoutDevices();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        if (!allowedByFiltering) {
            return 0;
        }
        return CATEGORY_HEIGHT + getExpandedHeight();
    }

    int getRows() {
        if (!allowedByFiltering) {
            return 0;
        }
        if (!expandCollapseState.isExpanded()) {
            return 1;
        }
        return 1 + visibleDeviceButtons;
    }

    private int getExpandedHeight() {
        return (int) ((double) (DEVICE_HEIGHT * visibleDeviceButtons) * expandCollapseState.getExpandedPercentage());
    }

    @Override
    public void visitWidgets(final Consumer<AbstractWidget> consumer) {
        if (outOfFrame) {
            return;
        }
        consumer.accept(deviceCategoryButton);
        consumer.accept(expandCollapseButton);
        if (expandCollapseState.isExpanded()) {
            for (final NetworkMonitorDeviceWidget deviceButton : deviceButtons) {
                consumer.accept(deviceButton);
            }
        }
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        return deviceCategoryButton.mouseClicked(event, doubleClick)
            || expandCollapseButton.mouseClicked(event, doubleClick)
            || mouseClickedOnDeviceButtons(event, doubleClick);
    }

    private boolean mouseClickedOnDeviceButtons(final MouseButtonEvent event, final boolean doubleClick) {
        return expandCollapseState.isExpanded()
            && deviceButtons.stream().anyMatch(deviceButton -> deviceButton.mouseClicked(event, doubleClick));
    }

    void onCurrentDeviceCategoryChanged(@Nullable final NetworkMonitorDeviceCategory currentDeviceCategory) {
        deviceCategoryButton.active = currentDeviceCategory != deviceCategory;
        deviceButtons.forEach(deviceButton -> deviceButton.active = true);
    }

    void onCurrentDeviceChanged(@Nullable final NetworkMonitorDevice currentDevice) {
        deviceCategoryButton.active = true;
        deviceButtons.forEach(deviceButton -> deviceButton.onCurrentDeviceChanged(currentDevice));
    }

    private void expand() {
        expandCollapseState.expand();
        expandCollapseButton.setSprites(Sprites.COLLAPSE);
    }

    boolean isExpanded() {
        return expandCollapseState.isExpanded();
    }

    void onDeviceAdded(final NetworkMonitorDeviceCategory deviceCategoryOfAddedDevice,
                       final NetworkMonitorDevice addedDevice) {
        if (deviceCategoryOfAddedDevice != deviceCategory) {
            return;
        }
        detectIfIconHasToBeAddedToCategoryIcons(addedDevice.item().value());
        addDevice(addedDevice);
    }

    private void detectIfIconHasToBeAddedToCategoryIcons(final Item addedIcon) {
        final boolean anyOtherDeviceWithThisIcon = deviceButtons.stream()
            .anyMatch(deviceButton -> deviceButton.hasIcon(addedIcon));
        if (!anyOtherDeviceWithThisIcon) {
            deviceCategoryButton.stacks.add(addedIcon.getDefaultInstance());
        }
    }

    void onDeviceRemoved(final NetworkMonitorDeviceCategory deviceCategoryOfRemovedDevice,
                         final NetworkMonitorDevice removedDevice) {
        if (deviceCategoryOfRemovedDevice != deviceCategory) {
            return;
        }
        final boolean removed = deviceButtons.removeIf(deviceButton -> deviceButton.is(removedDevice));
        if (removed) {
            detectIfIconHasToBeRemovedFromCategoryIcons(removedDevice.item().value());
            relayoutDevices();
        }
    }

    private void detectIfIconHasToBeRemovedFromCategoryIcons(final Item removedIcon) {
        final boolean anyOtherDeviceWithThisIcon = deviceButtons.stream()
            .anyMatch(deviceButton -> deviceButton.hasIcon(removedIcon));
        if (!anyOtherDeviceWithThisIcon) {
            deviceCategoryButton.stacks.clear();
            deviceCategoryButton.stacks.addAll(DeviceCategoryButton.createStacks(deviceButtons.stream()
                .map(NetworkMonitorDeviceWidget::getDevice)
                .toList()));
        }
    }

    private void relayoutDevices() {
        int deviceButtonY = y + CATEGORY_HEIGHT;
        visibleDeviceButtons = 0;
        for (final NetworkMonitorDeviceWidget deviceButton : deviceButtons) {
            deviceButton.setAllowedByFiltering(deviceVisible);
            if (!deviceButton.visible) {
                continue;
            }
            visibleDeviceButtons++;
            deviceButton.setY(deviceButtonY);
            deviceButtonY += CATEGORY_HEIGHT;
        }
    }

    void sortDevices(final Comparator<NetworkMonitorDevice> deviceSort) {
        deviceButtons.sort((a, b) -> deviceSort.compare(a.getDevice(), b.getDevice()));
        relayoutDevices();
    }

    private class DeviceCategoryButton extends AbstractButton {
        private static final long CYCLE_MS = 1000;

        private final TextMarquee text;
        private final Runnable selected;
        private final List<ItemStack> stacks;

        private long cycleStart = 0;
        private int currentCycle = 0;

        private DeviceCategoryButton(final int x, final int y, final NetworkMonitorDeviceCategory deviceCategory,
                                     final List<NetworkMonitorDevice> devices, final Runnable selected) {
            super(x, y, CATEGORY_WIDTH, CATEGORY_HEIGHT, Component.empty());
            this.text = new TextMarquee(
                createTranslation("gui", "network_monitor.device_category."
                    + deviceCategory.name().toLowerCase(Locale.ROOT)),
                CATEGORY_WIDTH - 16 - 4 - 4,
                0xFFFFFFFF,
                true,
                TextMarquee.Style.SMALL
            );
            this.selected = selected;
            this.stacks = createStacks(devices);
        }

        private static List<ItemStack> createStacks(final List<NetworkMonitorDevice> devices) {
            return devices.stream()
                .map(NetworkMonitorDevice::item)
                .map(Holder::value)
                .map(Item::getDefaultInstance)
                .collect(Collectors.toList());
        }

        @Override
        public void onPress(final InputWithModifiers inputWithModifiers) {
            selected.run();
        }

        @Override
        protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                       final float partialTicks) {
            final long now = System.currentTimeMillis();
            if (cycleStart == 0) {
                cycleStart = now;
            }
            if (now - cycleStart >= CYCLE_MS) {
                currentCycle++;
                cycleStart = now;
            }
            extractDefaultSprite(graphics);
            renderIcon(graphics);
            final int yOffset = SmallText.isSmall() ? 6 : 3;
            final int textX = getX() + 4 + 16;
            final int textY = getY() + yOffset;
            text.updateStateAndRender(graphics, textX, textY, Minecraft.getInstance().font, isHovered, partialTicks);
            if (isHovered) {
                graphics.setTooltipForNextFrame(createTranslation(
                    "gui",
                    "network_monitor.devices.energy_usage_per_tick",
                    Component.literal(format(getTotalEnergyUsage())).withColor(0xFFFFFFFF)
                ).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
            }
        }

        private long getTotalEnergyUsage() {
            return deviceButtons.stream()
                .map(NetworkMonitorDeviceWidget::getDevice)
                .mapToLong(NetworkMonitorDevice::energyUsage)
                .sum();
        }

        private void renderIcon(final GuiGraphicsExtractor graphics) {
            final int resourceX = getX() + 1;
            final int resourceY = getY() + 1;
            final ItemStack stack = stacks.get(currentCycle % stacks.size());
            graphics.fakeItem(stack, resourceX, resourceY);
            final boolean large = Minecraft.getInstance().isEnforceUnicode()
                || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
            ResourceSlotRendering.renderAmount(graphics, resourceX, resourceY,
                format(deviceButtons.size()), 0xFFFFFFFF, large);
        }

        @Override
        protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            // no op
        }
    }
}
