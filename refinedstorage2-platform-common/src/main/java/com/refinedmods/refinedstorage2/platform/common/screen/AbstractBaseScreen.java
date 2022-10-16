package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.ResourceAmountScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ResourceFilterButtonWidget;

import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

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
        tryAddResourceFilterButton();
    }

    private void tryAddResourceFilterButton() {
        if (!(menu instanceof AbstractResourceFilterContainerMenu resourceFilterMenu)) {
            return;
        }
        if (!isResourceFilterButtonVisible()) {
            return;
        }
        final ResourceFilterButtonWidget resourceFilterButton = new ResourceFilterButtonWidget(
            getResourceFilterButtonX(),
            topPos + 4,
            resourceFilterMenu
        );
        resourceFilterButton.active = isResourceFilterButtonActive();
        addRenderableWidget(resourceFilterButton);
    }

    protected int getResourceFilterButtonX() {
        return leftPos + imageWidth - ResourceFilterButtonWidget.WIDTH - 7;
    }

    protected boolean isResourceFilterButtonVisible() {
        return true;
    }

    protected boolean isResourceFilterButtonActive() {
        return true;
    }

    protected abstract ResourceLocation getTexture();

    @Override
    protected void renderBg(final PoseStack poseStack, final float delta, final int mouseX, final int mouseY) {
        prepareBackgroundShader(getTexture());

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);

        renderResourceFilterSlots(poseStack);
    }

    protected static void prepareBackgroundShader(final ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }

    @Override
    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    protected final void renderResourceFilterSlots(final PoseStack poseStack) {
        for (final Slot slot : menu.slots) {
            tryRenderResourceFilterSlot(poseStack, slot);
        }
    }

    private void tryRenderResourceFilterSlot(final PoseStack poseStack, final Slot slot) {
        if (!(slot instanceof ResourceFilterSlot resourceFilterSlot)) {
            return;
        }
        final FilteredResource filteredResource = resourceFilterSlot.getFilteredResource();
        if (filteredResource == null) {
            return;
        }
        renderResourceFilterSlot(
            poseStack,
            leftPos + slot.x,
            topPos + slot.y,
            getBlitOffset(),
            filteredResource,
            resourceFilterSlot.supportsAmount()
        );
    }

    private void renderResourceFilterSlot(final PoseStack poseStack,
                                          final int x,
                                          final int y,
                                          final int z,
                                          final FilteredResource filteredResource,
                                          final boolean supportsAmount) {
        filteredResource.render(poseStack, x, y, z);
        if (supportsAmount) {
            renderResourceFilterSlotAmount(poseStack, x, y, filteredResource);
        }
    }

    protected void renderResourceFilterSlotAmount(final PoseStack poseStack,
                                                  final int x,
                                                  final int y,
                                                  final FilteredResource filteredResource) {
        renderAmount(
            poseStack,
            x,
            y,
            filteredResource.getFormattedAmount(),
            Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15),
            true
        );
    }

    protected void renderAmount(final PoseStack poseStack,
                                final int x,
                                final int y,
                                final String amount,
                                final int color,
                                final boolean large) {
        poseStack.pushPose();
        // Large amounts overlap with the slot lines (see Minecraft behavior)
        poseStack.translate(x + (large ? 1D : 0D), y + (large ? 1D : 0D), 300D);
        if (!large) {
            poseStack.scale(0.5F, 0.5F, 1);
        }
        font.drawShadow(poseStack, amount, (float) (large ? 16 : 30) - font.width(amount), large ? 8 : 22, color);
        poseStack.popPose();
    }

    public void addSideButton(final AbstractSideButtonWidget button) {
        button.x = leftPos - button.getWidth() - 2;
        button.y = topPos + sideButtonY;

        sideButtonY += button.getHeight() + 2;

        addRenderableWidget(button);
    }

    protected final void setScissor(final int x, final int y, final int w, final int h) {
        if (minecraft == null) {
            return;
        }
        final double scale = minecraft.getWindow().getGuiScale();
        final int sx = (int) (x * scale);
        final int sy = (int) ((minecraft.getWindow().getGuiScaledHeight() - (y + h)) * scale);
        final int sw = (int) (w * scale);
        final int sh = (int) (h * scale);
        RenderSystem.enableScissor(sx, sy, sw, sh);
    }

    @Override
    protected void renderTooltip(final PoseStack poseStack, final int x, final int y) {
        super.renderTooltip(poseStack, x, y);
        if (minecraft != null
            && menu.getCarried().isEmpty()
            && hoveredSlot instanceof ResourceFilterSlot resourceFilterSlot) {
            final List<Component> lines = resourceFilterSlot.getTooltipLines(minecraft.player);
            if (!lines.isEmpty()) {
                this.renderComponentTooltip(poseStack, lines, x, y);
            }
        }
    }

    @Override
    protected void slotClicked(final Slot slot, final int slotId, final int mouseButton, final ClickType type) {
        if (!tryOpenResourceFilterAmountScreen(slot, type)) {
            super.slotClicked(slot, slotId, mouseButton, type);
        }
    }

    protected boolean tryOpenResourceFilterAmountScreen(final Slot slot, final ClickType type) {
        final boolean isFilterSlot = slot instanceof ResourceFilterSlot filterSlot
            && filterSlot.getFilteredResource() != null;
        final boolean doesFilterSlotSupportAmount = isFilterSlot && ((ResourceFilterSlot) slot).supportsAmount();
        final boolean isRegularClick = type != ClickType.QUICK_MOVE;
        final boolean isNotCarryingItem = getMenu().getCarried().isEmpty();
        final boolean canChangeAmount =
            isFilterSlot && doesFilterSlotSupportAmount && isRegularClick && isNotCarryingItem;
        if (canChangeAmount && minecraft != null) {
            minecraft.setScreen(new ResourceAmountScreen(this, playerInventory, ((ResourceFilterSlot) slot)));
        }
        return canChangeAmount;
    }
}
