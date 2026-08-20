package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

class NetworkMonitorDeviceWidget extends AbstractButton {
    static final int WIDTH = 64;

    private final NetworkMonitorDevice device;
    private final TextMarquee text;
    private final Runnable selected;
    private final ItemStack stack;

    private boolean outOfFrame;
    private boolean allowedByFiltering;

    NetworkMonitorDeviceWidget(final int x, final int y, final int width, final NetworkMonitorDevice device,
                               final Runnable selected, final boolean allowedByFiltering) {
        super(x, y, width, NetworkMonitorDeviceGroupWidget.DEVICE_HEIGHT,
            Component.empty());
        this.text = new TextMarquee(
            device.name(),
            width - 16 - 4 - 4,
            0xFFFFFFFF,
            true,
            TextMarquee.Style.SMALL
        );
        this.device = device;
        this.selected = selected;
        this.stack = device.item().value().getDefaultInstance();
        setAllowedByFiltering(allowedByFiltering);
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

    boolean isSelected() {
        return !active;
    }

    void setOutOfFrame(final boolean outOfFrame) {
        this.outOfFrame = outOfFrame;
    }

    void setAllowedByFiltering(final boolean allowedByFiltering) {
        this.allowedByFiltering = allowedByFiltering;
        this.visible = allowedByFiltering;
    }

    void setAllowedByFiltering(final Predicate<NetworkMonitorDevice> filter) {
        setAllowedByFiltering(filter.test(device));
    }

    void onCurrentDeviceChanged(@Nullable final NetworkMonitorDevice currentDevice) {
        this.active = currentDevice == null || !is(currentDevice);
    }

    NetworkMonitorDevice getDevice() {
        return device;
    }

    boolean hasIcon(final Item icon) {
        return device.item().value().equals(icon);
    }

    boolean is(final NetworkMonitorDevice otherDevice) {
        return this.device.id().equals(otherDevice.id());
    }

    @Override
    public void onPress(final InputWithModifiers inputWithModifiers) {
        selected.run();
    }

    @Override
    public int getHeight() {
        if (!allowedByFiltering) {
            return 0;
        }
        return super.getHeight();
    }

    @Override
    protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                   final float partialTicks) {
        if (outOfFrame) {
            return;
        }
        extractDefaultSprite(graphics);
        renderIcon(graphics);
        final int yOffset = SmallText.isSmall() ? 6 : 3;
        final int textX = getX() + 4 + 16;
        final int textY = getY() + yOffset;
        text.updateStateAndRender(graphics, textX, textY, Minecraft.getInstance().font, isHovered, partialTicks);
        if (isHovered) {
            final List<Component> tooltipLines = new ArrayList<>();
            tooltipLines.add(createTranslation(
                "gui",
                "network_monitor.devices.energy_usage_per_tick",
                Component.literal(format(device.energyUsage())).withColor(0xFFFFFFFF)
            ).withStyle(ChatFormatting.GRAY));
            device.insertPriority().ifPresent(insertPriority -> tooltipLines.add(createTranslation(
                "gui",
                "network_monitor.devices.insert_priority",
                Component.literal(String.valueOf(insertPriority)).withColor(0xFFFFFFFF)
            ).withStyle(ChatFormatting.GRAY)));
            device.extractPriority().ifPresent(extractPriority -> tooltipLines.add(createTranslation(
                "gui",
                "network_monitor.devices.extract_priority",
                Component.literal(String.valueOf(extractPriority)).withColor(0xFFFFFFFF)
            ).withStyle(ChatFormatting.GRAY)));
            graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltipLines, mouseX, mouseY);
        }
    }

    private void renderIcon(final GuiGraphicsExtractor graphics) {
        final int resourceX = getX() + 1;
        final int resourceY = getY() + 1;
        graphics.fakeItem(stack, resourceX, resourceY);
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput narrationElementOutput) {
        // no op
    }
}
