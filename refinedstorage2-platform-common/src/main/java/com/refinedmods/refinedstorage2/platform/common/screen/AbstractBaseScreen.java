package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.SlotTooltip;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.ResourceAmountScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

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
        renderResourceSlots(graphics);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    protected final void renderResourceSlots(final GuiGraphics graphics) {
        if (!(menu instanceof AbstractResourceContainerMenu resourceContainerMenu)) {
            return;
        }
        for (final ResourceSlot slot : resourceContainerMenu.getResourceSlots()) {
            tryRenderResourceSlot(graphics, slot);
        }
    }

    private void tryRenderResourceSlot(final GuiGraphics graphics, final ResourceSlot slot) {
        final ResourceAmountTemplate<?> resourceAmount = slot.getResourceAmount();
        if (resourceAmount == null) {
            return;
        }
        renderResourceSlot(
            graphics,
            leftPos + slot.x,
            topPos + slot.y,
            resourceAmount,
            slot.shouldRenderAmount()
        );
    }

    private <R> void renderResourceSlot(final GuiGraphics graphics,
                                        final int x,
                                        final int y,
                                        final ResourceAmountTemplate<R> resourceAmount,
                                        final boolean renderAmount) {
        final ResourceRendering<R> rendering = PlatformApi.INSTANCE.getResourceRendering(
            resourceAmount.getResource()
        );
        rendering.render(resourceAmount.getResource(), graphics, x, y);
        if (renderAmount) {
            renderResourceSlotAmount(graphics, x, y, resourceAmount.getAmount(), rendering);
        }
    }

    private <R> void renderResourceSlotAmount(final GuiGraphics graphics,
                                              final int x,
                                              final int y,
                                              final long amount,
                                              final ResourceRendering<R> rendering) {
        renderAmount(
            graphics,
            x,
            y,
            rendering.getDisplayedAmount(amount),
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
        poseStack.translate(x + (large ? 1D : 0D), y + (large ? 1D : 0D), 199);
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
        if (hoveredSlot instanceof SlotTooltip slotTooltip) {
            final List<ClientTooltipComponent> tooltip = slotTooltip.getTooltip(menu.getCarried());
            if (!tooltip.isEmpty()) {
                Platform.INSTANCE.renderTooltip(graphics, tooltip, x, y);
                return;
            }
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (hoveredSlot instanceof ResourceSlot resourceSlot
            && !resourceSlot.supportsItemSlotInteractions()
            && !resourceSlot.isDisabled()
            && getMenu() instanceof AbstractResourceContainerMenu containerMenu) {
            if (!tryOpenResourceAmountScreen(resourceSlot)) {
                containerMenu.sendResourceSlotChange(hoveredSlot.index, clickedButton == 1);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private boolean tryOpenResourceAmountScreen(final ResourceSlot slot) {
        final boolean isFilterSlot = slot.getResourceAmount() != null;
        final boolean canModifyAmount = isFilterSlot && slot.canModifyAmount();
        final boolean isNotTryingToRemoveFilter = !hasShiftDown();
        final boolean isNotCarryingItem = getMenu().getCarried().isEmpty();
        final boolean canOpen =
            isFilterSlot && canModifyAmount && isNotTryingToRemoveFilter && isNotCarryingItem;
        if (canOpen && minecraft != null) {
            minecraft.setScreen(new ResourceAmountScreen(this, playerInventory, slot));
        }
        return canOpen;
    }

    @Nullable
    public ResourceTemplate<?> getHoveredResource() {
        return hoveredSlot instanceof ResourceSlot resourceSlot && resourceSlot.getResourceAmount() != null
            ? resourceSlot.getResourceAmount().getResourceTemplate()
            : null;
    }
}
