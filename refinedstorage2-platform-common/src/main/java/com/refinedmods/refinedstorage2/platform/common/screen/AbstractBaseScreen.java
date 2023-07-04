package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.upgrade.ApplicableUpgrade;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.UpgradeSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.ResourceAmountScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractBaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private final Inventory playerInventory;
    private int sideButtonY;

    protected AbstractBaseScreen(final T menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.playerInventory = playerInventory;
        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
    }

    @Override
    protected void init() {
        clearWidgets();
        super.init();
        sideButtonY = 6;
    }

    protected abstract ResourceLocation getTexture();

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        graphics.blit(getTexture(), x, y, 0, 0, imageWidth, imageHeight);

        renderResourceFilterSlots(graphics);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    protected final void renderResourceFilterSlots(final GuiGraphics graphics) {
        for (final Slot slot : menu.slots) {
            tryRenderResourceFilterSlot(graphics, slot);
        }
    }

    private void tryRenderResourceFilterSlot(final GuiGraphics graphics, final Slot slot) {
        if (!(slot instanceof ResourceFilterSlot resourceFilterSlot)) {
            return;
        }
        final FilteredResource<?> filteredResource = resourceFilterSlot.getFilteredResource();
        if (filteredResource == null) {
            return;
        }
        renderResourceFilterSlot(
            graphics,
            leftPos + slot.x,
            topPos + slot.y,
            filteredResource,
            resourceFilterSlot.supportsAmount()
        );
    }

    private void renderResourceFilterSlot(final GuiGraphics graphics,
                                          final int x,
                                          final int y,
                                          final FilteredResource<?> filteredResource,
                                          final boolean supportsAmount) {
        filteredResource.render(graphics, x, y);
        if (supportsAmount) {
            renderResourceFilterSlotAmount(graphics, x, y, filteredResource);
        }
    }

    protected void renderResourceFilterSlotAmount(final GuiGraphics graphics,
                                                  final int x,
                                                  final int y,
                                                  final FilteredResource<?> filteredResource) {
        renderAmount(
            graphics,
            x,
            y,
            filteredResource.getDisplayedAmount(),
            Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15),
            true
        );
    }

    protected void renderAmount(final GuiGraphics graphics,
                                final int x,
                                final int y,
                                final String amount,
                                final int color,
                                final boolean large) {
        final PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        // Large amounts overlap with the slot lines (see Minecraft behavior)
        poseStack.translate(x + (large ? 1D : 0D), y + (large ? 1D : 0D), 300D);
        if (!large) {
            poseStack.scale(0.5F, 0.5F, 1);
        }
        graphics.drawString(font, amount, (large ? 16 : 30) - font.width(amount), large ? 8 : 22, color, true);
        poseStack.popPose();
    }

    public void addSideButton(final AbstractSideButtonWidget button) {
        button.setX(leftPos - button.getWidth() - 2);
        button.setY(topPos + sideButtonY);

        sideButtonY += button.getHeight() + 2;

        addRenderableWidget(button);
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (menu.getCarried().isEmpty() && hoveredSlot instanceof ResourceFilterSlot resourceFilterSlot) {
            final List<Component> lines = resourceFilterSlot.getTooltip();
            if (!lines.isEmpty()) {
                graphics.renderComponentTooltip(font, lines, x, y);
            }
        } else if (menu.getCarried().isEmpty()
            && hoveredSlot instanceof UpgradeSlot upgradeSlot
            && !hoveredSlot.hasItem()) {
            final List<Component> lines = getUpgradeSlotTooltip(upgradeSlot);
            graphics.renderComponentTooltip(font, lines, x, y);
        } else {
            super.renderTooltip(graphics, x, y);
        }
    }

    private List<Component> getUpgradeSlotTooltip(final UpgradeSlot upgradeSlot) {
        final List<Component> lines = new ArrayList<>();
        lines.add(PlatformApi.INSTANCE.createTranslation(
            "gui",
            "applicable_upgrades"
        ).withStyle(ChatFormatting.WHITE));
        for (final ApplicableUpgrade applicableUpgrade : upgradeSlot.getApplicableUpgrades()) {
            final Item upgradeItem = applicableUpgrade.itemSupplier().get();
            final MutableComponent name = upgradeItem.getName(new ItemStack(upgradeItem)).copy();
            final MutableComponent amount = Component.literal("(" + applicableUpgrade.maxAmount() + ")");
            lines.add(name.append(" ").append(amount).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (hoveredSlot instanceof ResourceFilterSlot
            && getMenu() instanceof AbstractResourceFilterContainerMenu containerMenu) {
            if (!tryOpenResourceFilterAmountScreen(hoveredSlot)) {
                containerMenu.sendResourceFilterSlotChange(hoveredSlot.index, clickedButton == 1);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    protected boolean tryOpenResourceFilterAmountScreen(final Slot slot) {
        final boolean isFilterSlot = slot instanceof ResourceFilterSlot filterSlot
            && filterSlot.getFilteredResource() != null;
        final boolean doesFilterSlotSupportAmount = isFilterSlot && ((ResourceFilterSlot) slot).supportsAmount();
        final boolean isNotTryingToRemoveFilter = !hasShiftDown();
        final boolean isNotCarryingItem = getMenu().getCarried().isEmpty();
        final boolean canChangeAmount =
            isFilterSlot && doesFilterSlotSupportAmount && isNotTryingToRemoveFilter && isNotCarryingItem;
        if (canChangeAmount && minecraft != null) {
            minecraft.setScreen(new ResourceAmountScreen(this, playerInventory, (ResourceFilterSlot) slot));
        }
        return canChangeAmount;
    }

    @Nullable
    public FilteredResource<?> getFilteredResource() {
        return hoveredSlot instanceof ResourceFilterSlot resourceFilterSlot
            ? resourceFilterSlot.getFilteredResource()
            : null;
    }
}
