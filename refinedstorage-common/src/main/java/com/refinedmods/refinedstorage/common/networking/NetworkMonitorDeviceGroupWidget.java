package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.support.ResourceSlotRendering;
import com.refinedmods.refinedstorage.common.support.Sprites;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.CustomButton;
import com.refinedmods.refinedstorage.common.support.widget.ExpandCollapseState;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

class NetworkMonitorDeviceGroupWidget implements LayoutElement, Renderable, GuiEventListener, NarratableEntry {
    private static final int WIDTH = 64;
    private static final int EXPAND_COLLAPSE_WIDTH = 16;
    private static final int GROUP_WIDTH = WIDTH - EXPAND_COLLAPSE_WIDTH;
    private static final int DEVICE_X_OFFSET = 6;
    private static final int DEVICE_WIDTH = WIDTH - DEVICE_X_OFFSET;
    private static final int GROUP_HEIGHT = 18;
    private static final int DEVICE_HEIGHT = 18;

    private final NetworkMonitorDeviceGroup deviceGroup;
    private final DeviceGroupButton deviceGroupButton;
    private final List<DeviceButton> deviceButtons;
    private final CustomButton expandCollapseButton;
    private final ExpandCollapseState expandCollapseState = new ExpandCollapseState();
    private final Consumer<NetworkMonitorDevice> deviceSelected;

    private int x;
    private int y;
    private boolean focused;
    private boolean visible;

    NetworkMonitorDeviceGroupWidget(final int x, final int y, final NetworkMonitorDeviceGroup deviceGroup,
                                    final Runnable selected, final Runnable onExpanded,
                                    final Consumer<NetworkMonitorDevice> deviceSelected,
                                    final boolean expanded) {
        this.deviceGroup = deviceGroup;
        this.x = x;
        this.y = y;
        this.deviceGroupButton = createDeviceGroupButton(x, y, deviceGroup, selected);
        this.expandCollapseButton = createExpandCollapseButton(x + deviceGroupButton.getWidth(), y, onExpanded,
            expandCollapseState);
        this.deviceButtons = new ArrayList<>();
        this.deviceSelected = deviceSelected;
        for (final NetworkMonitorDevice device : deviceGroup.devices()) {
            addDevice(device);
        }
        if (expanded) {
            expand();
        }
    }

    private static DeviceGroupButton createDeviceGroupButton(final int x, final int y,
                                                             final NetworkMonitorDeviceGroup deviceGroup,
                                                             final Runnable groupSelected) {
        return new DeviceGroupButton(x, y, deviceGroup, new TextMarquee(
            deviceGroup.type().name(),
            GROUP_WIDTH - 16 - 4 - 4,
            0xFFFFFFFF,
            true,
            TextMarquee.Style.SMALL
        ), groupSelected);
    }

    private static CustomButton createExpandCollapseButton(final int x, final int y, final Runnable expanded,
                                                           final ExpandCollapseState expandCollapseState) {
        return new CustomButton(x, y, EXPAND_COLLAPSE_WIDTH, GROUP_HEIGHT, Sprites.EXPAND, btn -> {
            final boolean expanding = expandCollapseState.toggle();
            if (expanding) {
                expanded.run();
            }
            btn.setSprites(expanding ? Sprites.COLLAPSE : Sprites.EXPAND);
        }, Component.empty());
    }

    UUID getDeviceGroupId() {
        return deviceGroup.id();
    }

    void addDevice(final NetworkMonitorDevice device) {
        final int buttonY = y + (GROUP_HEIGHT * (deviceButtons.size() + 1));
        deviceButtons.add(new DeviceButton(x + DEVICE_X_OFFSET, buttonY, new TextMarquee(
            device.name(),
            DEVICE_WIDTH - 16 - 4 - 4,
            0xFFFFFFFF,
            true,
            TextMarquee.Style.SMALL
        ), device, () -> deviceSelected.accept(device)));
    }

    boolean update() {
        return expandCollapseState.updateAnimation();
    }

    @Override
    public boolean isActive() {
        return visible;
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
        if (!visible) {
            return;
        }
        deviceGroupButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        expandCollapseButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        if (!expandCollapseState.isExpanded()) {
            return;
        }
        extractExpandedState(graphics, mouseX, mouseY, partialTicks);
    }

    private void extractExpandedState(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                      final float partialTicks) {
        final int expandedHeight = getExpandedHeight();
        graphics.enableScissor(x, y + GROUP_HEIGHT, x + WIDTH, y + GROUP_HEIGHT + expandedHeight);
        graphics.verticalLine(x + 2, y + GROUP_HEIGHT - 1, y + getHeight(), 0xFF333333);
        for (final DeviceButton deviceButton : deviceButtons) {
            graphics.horizontalLine(x + 2, x + 5, deviceButton.getY() + (GROUP_HEIGHT / 2) - 1, 0xFF333333);
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

    void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public void setX(final int x) {
        this.x = x;
        deviceGroupButton.setX(x);
        expandCollapseButton.setX(x + deviceGroupButton.getWidth());
        deviceButtons.forEach(deviceButton -> deviceButton.setX(x + DEVICE_X_OFFSET));
    }

    @Override
    public void setY(final int y) {
        this.y = y;
        deviceGroupButton.setY(y);
        expandCollapseButton.setY(y);
        relayoutDeviceButtons();
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
        return GROUP_HEIGHT + getExpandedHeight();
    }

    private int getExpandedHeight() {
        return (int) ((double) (DEVICE_HEIGHT * deviceButtons.size()) * expandCollapseState.getExpandedPercentage());
    }

    @Override
    public void visitWidgets(final Consumer<AbstractWidget> consumer) {
        if (!visible) {
            return;
        }
        consumer.accept(deviceGroupButton);
        consumer.accept(expandCollapseButton);
        if (expandCollapseState.isExpanded()) {
            for (final DeviceButton deviceButton : deviceButtons) {
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
        return deviceGroupButton.mouseClicked(event, doubleClick)
            || expandCollapseButton.mouseClicked(event, doubleClick)
            || mouseClickedOnDeviceButtons(event, doubleClick);
    }

    private boolean mouseClickedOnDeviceButtons(final MouseButtonEvent event, final boolean doubleClick) {
        return expandCollapseState.isExpanded()
            && deviceButtons.stream().anyMatch(deviceButton -> deviceButton.mouseClicked(event, doubleClick));
    }

    boolean onCurrentDeviceGroupChanged(final NetworkMonitorDeviceGroup currentDeviceGroup) {
        final boolean isMyDeviceGroup = deviceGroup.id().equals(currentDeviceGroup.id());
        deviceGroupButton.active = !isMyDeviceGroup;
        deviceButtons.forEach(deviceButton -> deviceButton.active = true);
        if (!isMyDeviceGroup) {
            return collapse();
        }
        return false;
    }

    void onCurrentDeviceChanged(final NetworkMonitorDevice currentDevice) {
        deviceGroupButton.active = true;
        deviceButtons.forEach(deviceButton ->
            deviceButton.active = !deviceButton.device.id().equals(currentDevice.id()));
    }

    boolean onDeviceGroupExpanded(final NetworkMonitorDeviceGroup expandedDeviceGroup) {
        final boolean isMyDeviceGroup = deviceGroup.id().equals(expandedDeviceGroup.id());
        if (!isMyDeviceGroup) {
            return collapse();
        }
        expand();
        return true;
    }

    private boolean collapse() {
        final boolean didCollapse = expandCollapseState.collapse();
        expandCollapseButton.setSprites(Sprites.EXPAND);
        return didCollapse;
    }

    private void expand() {
        expandCollapseState.expand();
        expandCollapseButton.setSprites(Sprites.COLLAPSE);
    }

    boolean isExpanded() {
        return expandCollapseState.isExpanded();
    }

    boolean onDeviceAdded(final NetworkMonitorDeviceGroup deviceGroupOfAddedDevice,
                          final NetworkMonitorDevice addedDevice) {
        if (!deviceGroupOfAddedDevice.id().equals(deviceGroup.id())) {
            return false;
        }
        addDevice(addedDevice);
        return expandCollapseState.isExpanded();
    }

    boolean onDeviceGroupRemoved(final NetworkMonitorDeviceGroup removedDeviceGroup) {
        return deviceGroup.id().equals(removedDeviceGroup.id());
    }

    boolean onDeviceRemoved(final NetworkMonitorDeviceGroup deviceGroupOfRemovedDevice,
                            final NetworkMonitorDevice removedDevice) {
        final boolean isMyDeviceGroup = deviceGroupOfRemovedDevice.id().equals(deviceGroup.id());
        if (!isMyDeviceGroup) {
            return false;
        }
        final boolean removed = deviceButtons.removeIf(deviceButton ->
            deviceButton.device.id().equals(removedDevice.id()));
        if (removed) {
            relayoutDeviceButtons();
        }
        return removed;
    }

    private void relayoutDeviceButtons() {
        int deviceButtonY = y + GROUP_HEIGHT;
        for (final DeviceButton deviceButton : deviceButtons) {
            deviceButton.setY(deviceButtonY);
            deviceButtonY += GROUP_HEIGHT;
        }
    }

    private static class DeviceGroupButton extends AbstractButton {
        private final NetworkMonitorDeviceGroup deviceGroup;
        private final TextMarquee text;
        private final Runnable selected;
        private final ItemStack stack;

        private DeviceGroupButton(final int x, final int y, final NetworkMonitorDeviceGroup deviceGroup,
                                  final TextMarquee text, final Runnable selected) {
            super(x, y, GROUP_WIDTH, GROUP_HEIGHT, Component.empty());
            this.deviceGroup = deviceGroup;
            this.text = text;
            this.selected = selected;
            this.stack = deviceGroup.type().icon().value().getDefaultInstance();
        }

        @Override
        public void onPress(final InputWithModifiers inputWithModifiers) {
            selected.run();
        }

        @Override
        protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                       final float partialTicks) {
            extractDefaultSprite(graphics);
            renderIcon(graphics);
            final int yOffset = SmallText.isSmall() ? 6 : 3;
            final int textX = getX() + 4 + 16;
            final int textY = getY() + yOffset;
            text.updateStateAndRender(graphics, textX, textY, Minecraft.getInstance().font, isHovered, partialTicks);
        }

        private void renderIcon(final GuiGraphicsExtractor graphics) {
            final int resourceX = getX();
            final int resourceY = getY() - 1;
            graphics.fakeItem(stack, resourceX, resourceY);
            final boolean large = Minecraft.getInstance().isEnforceUnicode()
                || Platform.INSTANCE.getConfig().getGrid().isLargeFont();
            ResourceSlotRendering.renderAmount(graphics, resourceX, resourceY,
                format(deviceGroup.devices().size()), 0xFFFFFFFF, large);
        }

        @Override
        protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            // no op
        }
    }

    private static class DeviceButton extends AbstractButton {
        private final NetworkMonitorDevice device;
        private final TextMarquee text;
        private final Runnable selected;

        private DeviceButton(final int x, final int y, final TextMarquee text, final NetworkMonitorDevice device,
                             final Runnable selected) {
            super(x, y, DEVICE_WIDTH, DEVICE_HEIGHT, Component.empty());
            this.text = text;
            this.device = device;
            this.selected = selected;
        }

        @Override
        public void onPress(final InputWithModifiers inputWithModifiers) {
            selected.run();
        }

        @Override
        protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                       final float partialTicks) {
            extractDefaultSprite(graphics);
            renderIcon(graphics);
            final int yOffset = SmallText.isSmall() ? 6 : 3;
            final int textX = getX() + 4 + 16;
            final int textY = getY() + yOffset;
            text.updateStateAndRender(graphics, textX, textY, Minecraft.getInstance().font, isHovered, partialTicks);
        }

        private void renderIcon(final GuiGraphicsExtractor graphics) {
            final int resourceX = getX();
            final int resourceY = getY() - 1;
            graphics.fakeItem(device.icon(), resourceX, resourceY);
        }

        @Override
        protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
            // no op
        }
    }
}
